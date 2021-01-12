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
uniform int  numDirectionalLights;

//Point Light Sources
uniform vec3 lightPosition[maxPointLights];
uniform vec3 lightColor[maxPointLights];
uniform float lightIntensity[maxPointLights];
uniform int  numPointLights;

//Skybox and nearest Reflection probe
uniform samplerCube nearestProbe;
uniform samplerCube skybox;

uniform float mat_m;
uniform float mat_r;

layout( location = 0 ) out vec4 gl_FragColor;

void main(void){
    gl_FragColor = vec4(passNormal, 1);
}