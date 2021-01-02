#version 400

//Set prescision
precision highp int;
precision highp float;
precision highp sampler2D;

#define maxLights 25

#define maxJoints 50
#define maxWeights 3

// Inputs
layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec3 vNormal;
layout(location = 2) in vec3 vTangent;
layout(location = 3) in vec3 vBitangent;
layout(location = 4) in vec2 vTexture;
layout(location = 5) in mat4 transform;

//in ivec3 jointIndices;
//in vec3 weights;

//Uniform variables
uniform mat4 view;           // Cameras position in space
uniform vec3 cameraPos;      // Cameras position in worldSpace
uniform mat4 perspective;    //Perspective of this world

uniform vec2 t_offset;    //texture offset
uniform vec2 t_scale;     //texture scale

//Lighting
uniform mat4 lightSpaceMatrix[maxLights];

uniform mat4 boneTransforms[maxJoints];

// Outputs
out vec3 passNormal;
out vec3 passCamPos;
out vec2 passCoords;
out vec3 WorldPos;

out mat3 passTBN;

//Reflection
out vec3 passReflectNormal;

//Lighting
out vec4[maxLights] passPosLightSpace;

//Main function to run
void main(){
    //Transdform the normnal vectors of this model by its transform.
    vec4 offsetNormal = transform *  vec4(vNormal.xyz, 1.0);
    vec4 worldOffset = transform * vec4(0, 0, 0, 1);
    passNormal = normalize((vec3(offsetNormal) / offsetNormal.w) - (worldOffset.xyz)/worldOffset.w);

    vec4 offsetTangent = transform *  vec4(vTangent.xyz, 1.0);
    vec3 passTangent = normalize((vec3(offsetTangent) / offsetTangent.w) - (worldOffset.xyz)/worldOffset.w);

    vec4 offsetBitangent = transform *  vec4(vBitangent.xyz, 1.0);
    vec3 passBitangent = normalize((vec3(offsetBitangent) / offsetBitangent.w) - (worldOffset.xyz)/worldOffset.w);

    mat3 tbnMatrix = mat3(
        passTangent.x, passBitangent.x, passNormal.x,
        passTangent.y, passBitangent.y, passNormal.y,
        passTangent.z, passBitangent.z, passNormal.z
    );

    passTBN = transpose(tbnMatrix);

    vec4 worldPosition = transform * vec4(vPosition.xyz, 1.0);
    WorldPos = worldPosition.xyz;

    passCoords = vTexture;

    //Camera Direction
    passCamPos = cameraPos;

    passReflectNormal = reflect(normalize(worldPosition.xyz - (cameraPos * -1)), passNormal);

    for(int i = 0; i < maxLights; i++){
        passPosLightSpace[i] = lightSpaceMatrix[i] * worldPosition;
    }

    gl_Position = perspective * view * worldPosition;
}