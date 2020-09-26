#version 400
// Fragment shader which takes interpolated colours and uses them to set the final fragment colour.
// Floating point values in fragment shaders must have a precision set.
// This can be done globally (as done here) or per variable.

//Set prescision
precision highp int;
precision highp float;
precision highp sampler2D;

#define maxLights 4

// Inputs
in vec3 passNormal;
in vec3 passCamPos;
in vec2 passCoords;
in vec4 passPosLightSpace[maxLights];

uniform sampler2D texureID;
uniform sampler2D shadowMap[maxLights];

uniform vec3 sunAngle[maxLights];
uniform vec3 sunColor[maxLights];

layout( location = 0 ) out vec4 gl_FragColor;


float dotProduct(vec3 posI, vec3 posJ){
    return (posI.x * posJ.x) + (posI.y * posJ.y) + (posI.z * posJ.z);
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

    vec4 albedo = texture(texureID, passCoords);
    if(albedo.a < 0.5){
        discard;
    }

    // ambient
    vec3 ambient = 0.15 * albedo.xyz;

    vec3 totalDiffuse = vec3(0);
    for(int i = 0; i < maxLights; i++){
        float diffuse = clamp(dotProduct(passNormal, sunAngle[i] * -1), 0, 1);
        float shadow = ShadowCalculation(i);
        totalDiffuse += ((1.0 - shadow) * sunColor[i] * (diffuse));
    }

    vec3 lighting = (ambient + totalDiffuse) * albedo.xyz;

    gl_FragColor = vec4(lighting, 1);

}