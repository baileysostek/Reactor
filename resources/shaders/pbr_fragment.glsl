#version 400
// Fragment shader which takes interpolated colours and uses them to set the final fragment colour.
// Floating point values in fragment shaders must have a precision set.
// This can be done globally (as done here) or per variable.

//Set prescision
precision highp int;
precision highp float;
precision highp sampler2D;

#define maxLights 8
#define maxPointLights 4
#define specularStrength 0.1
#define PI 3.14159265359

// Inputs
in vec3 passNormal;
in vec2 passCoords;
in vec3 passCamPos;
in vec3 WorldPos;
in vec4 passPosLightSpace[maxLights];

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

//Point Light Sources
uniform vec3 lightPosition[maxPointLights];
uniform vec3 lightColor[maxPointLights];
uniform float lightIntensity[maxPointLights];

//Skybox and nearest Reflection probe
uniform samplerCube nearestProbe;
uniform samplerCube skybox;

uniform float mat_m;
uniform float mat_r;

layout( location = 0 ) out vec4 gl_FragColor;

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

//PBR
vec3 fresnelSchlick(float cosTheta, vec3 F0)
{
    return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0);
}

vec3 fresnelSchlickRoughness(float cosTheta, vec3 F0, float roughness)
{
    return F0 + (max(vec3(1.0 - roughness), F0) - F0) * pow(1.0 - cosTheta, 5.0);
}

float DistributionGGX(vec3 N, vec3 H, float roughness)
{
    float a      = roughness*roughness;
    float a2     = a*a;
    float NdotH  = max(dot(N, H), 0.0);
    float NdotH2 = NdotH*NdotH;

    float num   = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = PI * denom * denom;

    return num / denom;
}

float GeometrySchlickGGX(float NdotV, float roughness)
{
    float r = (roughness + 1.0);
    float k = (r*r) / 8.0;

    float num   = NdotV;
    float denom = NdotV * (1.0 - k) + k;

    return num / denom;
}
float GeometrySmith(vec3 N, vec3 V, vec3 L, float roughness)
{
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float ggx2  = GeometrySchlickGGX(NdotV, roughness);
    float ggx1  = GeometrySchlickGGX(NdotL, roughness);

    return ggx1 * ggx2;
}

void main(void){
    //Sample the Albedo texture
    vec4 albedo    = texture(textureID, passCoords);
    float metallic = texture(metallicID, passCoords).r;
    float ao       = texture(ambientOcclusionID, passCoords).r;
    float roughness= texture(roughnessID, passCoords).r;

    metallic  = mat_m;
    roughness = mat_r;

//    albedo.xyz = mix(albedo.xyz, getReflection(), metallic);

    //If this is a transparent pixel, dont do anything
    if(albedo.a < 0.5){
        discard;
    }

    vec3 N = normalize(passNormal);
    vec3 V = normalize(passCamPos - WorldPos);

    vec3 F0 = vec3(0.04);
    F0      = mix(F0, albedo.xyz, metallic);

    vec3 Lo = vec3(0.0);
    //Point lights
    for(int i = 0; i < maxPointLights; i++){
        vec3 L = normalize(lightPosition[i] - WorldPos);
        vec3 H = normalize(V + L);

        float cosTheta = dot(normalize(lightPosition[i]), N);

        float distance    = length(lightPosition[i] - WorldPos) / lightIntensity[i];
        float attenuation = 1.0 / (distance * distance);
        vec3 radiance     = lightColor[i] * attenuation;

        float NDF = DistributionGGX(N, H, roughness);
        float G   = GeometrySmith(N, V, L, roughness);
        vec3 F  = fresnelSchlick(max(dot(H, V), 0.0), F0);

        vec3 kS = F;
        vec3 kD = vec3(1.0) - kS;
        kD *= 1.0 - metallic;

        vec3 numerator    = NDF * G * F;
        float denominator = maxPointLights * max(dot(N, V), 0.0) * max(dot(N, L), 0.0);
        vec3 specular     = (numerator / max(denominator, 0.001));

        float NdotL = max(dot(N, L), 0.0);
        Lo += (kD * albedo.xyz / PI + specular) * radiance * NdotL;
    }

    //Directional lights
    for(int i = 0; i < maxLights; i++){
        vec3 L = normalize(sunAngle[i] * -1);
        vec3 H = normalize(V + L);

        float cosTheta = dot(normalize(sunAngle[i] * -1), N);

        float distance    = length(sunAngle[i] * -1);
        float attenuation = 1.0 / (distance * distance);
        vec3 radiance     = sunColor[i] * attenuation;

        float NDF = DistributionGGX(N, H, roughness);
        float G   = GeometrySmith(N, V, L, roughness);
        vec3 F  = fresnelSchlick(max(dot(H, V), 0.0), F0);

        vec3 kS = F;
        vec3 kD = vec3(1.0) - kS;
        kD *= 1.0 - metallic;

        vec3 numerator    = NDF * G * F;
        float denominator = maxLights * max(dot(N, V), 0.0) * max(dot(N, L), 0.0);
        vec3 specular     = (numerator / max(denominator, 0.001));

        float NdotL = max(dot(N, L), 0.0);
        Lo += (kD * albedo.xyz / PI + specular) * radiance * NdotL;
        }

    // ambient
    vec3 ambient = vec3(0.15) * albedo.xyz * ao;
    vec3 color   = ambient + Lo;

    color = color / (color + vec3(1.0));
    color = pow(color, vec3(1.0/2.2));

    gl_FragColor = vec4(color, 1);

}