#version 400
// Fragment shader which takes interpolated colours and uses them to set the final fragment colour.
// Floating point values in fragment shaders must have a precision set.
// This can be done globally (as done here) or per variable.

//Set prescision
precision highp int;
precision highp float;
precision highp sampler2D;

#define maxLights 25
#define maxPointLights 25
#define specularStrength 0.1
#define PI 3.14159

// Inputs
in vec3 passNormal;
in vec2 passCoords;
in vec3 passCamPos;
in vec3 WorldPos;
in vec4 passPosLightSpace[maxLights];

in mat3 passTBN;

//Reflection normal.
in vec3 passReflectNormal;

//Texture Units
uniform sampler2D textureID;
uniform sampler2D normalID;
uniform sampler2D metallicID;
uniform sampler2D roughnessID;
uniform sampler2D ambientOcclusionID;

//Shadow casting light sources
uniform sampler2D shadowMap[maxLights];
uniform vec3 sunAngle[maxLights];
uniform vec3 sunColor[maxLights];
uniform int  numDirectionalLights;

//Point Light Sources
uniform vec3 lightPosition[maxPointLights];
uniform vec3 lightColor[maxPointLights];
uniform float lightIntensity[maxPointLights];
uniform int  numPointLights;


//Skybox and nearest Reflection probe
uniform samplerCube nearestProbe;
uniform samplerCube skybox;

layout( location = 0 ) out vec4 gl_FragColor;

float dotProduct(vec3 posI, vec3 posJ){
    return (posI.x * posJ.x) + (posI.y * posJ.y) + (posI.z * posJ.z);
}

vec3 getReflection(){
    return texture(skybox, passReflectNormal).xyz;
}

float ShadowCalculation(int index)
{
    vec3 pos = passPosLightSpace[index].xyz * 0.5 + 0.5;
    if(pos.z > 1.0){
        pos.z = 1.0;
    }
    float depth = texture(shadowMap[index], pos.xy).r;
    float bias = 0.005f;

    return (depth + bias) < pos.z ? 1.0 : 0.0;
}

float DistributionGGX(vec3 N, vec3 H, float a)
{
    float a2     = a*a;
    float NdotH  = max(dot(N * -1, H), 0.0);
    float NdotH2 = NdotH*NdotH;

    float nom    = a2;
    float denom  = (NdotH2 * (a2 - 1.0) + 1.0);
    denom        = PI * denom * denom;

    return nom / denom;
}

void main(void){
    //Sample the Albedo texture
    vec4 albedo = texture(textureID, passCoords);
    //If this is a transparent pixel, dont do anything
    if(albedo.a < 0.5){
        discard;
    }

    // ambient
    vec3 ambient = 0.15 * albedo.xyz;

    // ray to camera from frag
    vec3 viewDir = normalize((passCamPos * -1));
//    vec3 viewDir2 = normalize((passCamPos * -1) - passFragPos);

    //Recalculate the surface normal
    vec4 normalTexture = 2.0 * texture(normalID, passCoords, -1.0) -1.0;
    vec3 surfaceNormal = passTBN * normalize(normalTexture.xyz);

    vec3 totalDiffuse  = vec3(0);
    vec3 totalSpecular = vec3(0);

//    for(int i = 0; i < numPointLights; i++){
//        vec3 lightDir      = normalize(lightPosition[i] - WorldPos);
//        vec3 reflectDir    = reflect(lightDir, surfaceNormal);
//        vec3 halfwayDir    = normalize(lightDir + viewDir);
//
//        float diffuse  = clamp(dotProduct(surfaceNormal, lightDir * -1), 0, 1);
//        float specular = pow(clamp(dotProduct(viewDir, reflectDir), 0, 1), 32);
//
//        float shadow = ShadowCalculation(i);
//        vec3 thislightColor = ((1.0 - shadow) * lightColor[i]);
//
//        totalDiffuse  += (thislightColor * diffuse);
//        totalSpecular += (thislightColor * specularStrength * specular);
//    }


    //Directional lights
    for(int i = 0; i < numDirectionalLights; i++){
        vec3 lightDir      = normalize(sunAngle[i]);
        vec3 reflectDir    = reflect(lightDir, surfaceNormal);
        vec3 halfwayDir    = normalize(lightDir + viewDir);

        float diffuse  = clamp(dotProduct(surfaceNormal, lightDir * -1), 0, 1);
        float specular = pow(clamp(dotProduct(viewDir, reflectDir), 0, 1), 32);

        float shadow = ShadowCalculation(i);
        vec3 thislightColor = ((1.0 - shadow) * sunColor[i]);

        totalDiffuse  += (thislightColor * diffuse);
        totalSpecular += (thislightColor * specularStrength * specular);

    }

    vec3 albedoReflect = mix(albedo.xyz, getReflection(), texture(metallicID, passCoords).r);
    vec3 lighting = (ambient + totalSpecular + clamp(totalDiffuse, 0, 1)) * albedoReflect;

    gl_FragColor = vec4(lighting, 1);

}