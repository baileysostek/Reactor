#version 400
// Fragment shader which takes interpolated colours and uses them to set the final fragment colour.
// Floating point values in fragment shaders must have a precision set.
// This can be done globally (as done here) or per variable.

//Set prescision
precision highp int;
precision highp float;
precision highp sampler2D;

#define maxLights 4
#define specularStrength 0.5

// Inputs
in vec3 passNormal;
in vec2 passCoords;
in vec3 passCamPos;
in vec3 passFragPos;
in vec4 passPosLightSpace[maxLights];

//Reflection normal.
in vec3 passReflectNormal;

//Texture Unit 1
uniform sampler2D textureID;
uniform sampler2D normalID;
uniform sampler2D metallicID;
uniform sampler2D roughnessID;
uniform sampler2D ambientOcclusionID;

uniform sampler2D shadowMap[maxLights];

uniform samplerCube nearestProbe;
uniform samplerCube skybox;

uniform vec3 sunAngle[maxLights];
uniform vec3 sunColor[maxLights];

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
    vec3 viewDir = normalize((passCamPos) - passFragPos);

    vec3 totalDiffuse  = vec3(0);
    vec3 totalSpecular = vec3(0);
    //Directional lights
    for(int i = 0; i < maxLights; i++){
        vec3 lightDir   = normalize(sunAngle[i] * -1);
        vec3 reflectDir = reflect(lightDir, passNormal);

        float diffuse  = clamp(dotProduct(passNormal, lightDir), 0, 1);
        float specular = pow(clamp(dotProduct(viewDir, reflectDir), 0, 1), 265);

        float shadow = ShadowCalculation(i);    
        vec3 lightColor = ((1.0 - shadow) * sunColor[i]);

        totalDiffuse  += (lightColor * diffuse);
        totalSpecular += (lightColor * specularStrength * specular);
    }

    vec3 albedoReflect = mix(albedo.xyz, getReflection(), texture(metallicID, passCoords).r);
    vec3 lighting = (ambient + totalSpecular + clamp(totalDiffuse, 0, 1)) * albedoReflect;

    gl_FragColor = vec4(lighting , 1);

}