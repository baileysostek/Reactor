#version 400

const int MAX_JOINTS = 50;
const int MAX_WEIGHTS = 3;
const int maxLights = 8;

// Inputs
layout(location =  0) in vec4 vPosition;
layout(location =  1) in vec3 vNormal;
layout(location =  2) in vec3 vTangent;
layout(location =  3) in vec3 vBitangent;
layout(location =  4) in vec2 vTexture;
layout(location =  5) in vec3 boneIndices;
layout(location =  6) in vec3 boneWeights;

layout(location =  7) in vec4 transform_0;
layout(location =  8) in vec4 transform_1;
layout(location =  9) in vec4 transform_2;
layout(location = 10) in vec4 transform_3;


//Uniform variables
uniform mat4 view;           // Cameras position in space
uniform vec3 cameraPos;      // Cameras position in worldSpace
uniform mat4 perspective;    //Perspective of this world

//Lighting
uniform mat4 lightSpaceMatrix[maxLights];

//Bones
uniform mat4 jointTransforms[MAX_JOINTS];

// Outputs
out vec3 passNormal;
out vec3 passWeights;
out vec3 passIndices;
out vec3 passCamPos;
out vec2 passCoords;
out vec3 WorldPos;

//Reflection
out vec3 passReflectNormal;

//Lighting
out vec4[maxLights] passPosLightSpace;

//Main function to run
void main(){
    //reconstruct our IN matricies
    mat4 transform = mat4(
        transform_0,
        transform_1,
        transform_2,
        transform_3
    );

    //Calculate a transform in space representing this vertex's bone deformation as a sum of 3 parts whos weights add to 1./
    mat4 boneTransform = (jointTransforms[uint(boneIndices.x)] * boneWeights.x) + (jointTransforms[uint(boneIndices.y)] * boneWeights.y) + (jointTransforms[uint(boneIndices.z)] * boneWeights.z);

    //Transdform the normnal vectors of this model by its transform.
    vec4 offsetNormal = transform * vec4(mat3(boneTransform) * vNormal.xyz, 1.0);
    vec4 worldOffset = transform * vec4(0, 0, 0, 1);
    passNormal = normalize((vec3(offsetNormal) / offsetNormal.w) - (worldOffset.xyz)/worldOffset.w);

    vec4 worldPosition = transform * boneTransform * vec4(vPosition.xyz, 1.0);
    WorldPos = worldPosition.xyz;

    passCoords = vTexture;

    passWeights = boneWeights;
    passIndices = boneIndices;

    //Camera Direction
    passCamPos = cameraPos * -1;

    passReflectNormal = reflect(normalize(worldPosition.xyz - (cameraPos * -1)), passNormal);

    for(int i = 0; i < maxLights; i++){
        passPosLightSpace[i] = lightSpaceMatrix[i] * worldPosition;
    }

    gl_Position = perspective * view * worldPosition;
}