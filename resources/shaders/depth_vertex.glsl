#version 430

//Set prescision
precision highp int;
precision highp float;
precision highp sampler2D;

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
uniform int numBones;
layout(std430, binding = 0) buffer Bones{
    mat4 boneTransforms[];
} bonesLocal;

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

//TBN matrix
out mat3 TBN;

//Main function to run
void main(){
    //reconstruct our IN matricies
    mat4 transform = mat4(
        transform_0,
        transform_1,
        transform_2,
        transform_3
    );

    //Create a mat4 we will use for mesh deformation
    mat4 boneTransform;
    //IF we have bones in this model
    if(numBones > 0){
        //Calculate the bone transform
        boneTransform =
        (bonesLocal.boneTransforms[uint(boneIndices.x) + (numBones * gl_InstanceID)] * boneWeights.x) + //Bone weight 1
        (bonesLocal.boneTransforms[uint(boneIndices.y) + (numBones * gl_InstanceID)] * boneWeights.y) + //Bone weight 2
        (bonesLocal.boneTransforms[uint(boneIndices.z) + (numBones * gl_InstanceID)] * boneWeights.z);  //Bone weight 3
    }else{
        // Just upload an identiy matrix here for the bone transform so we will have no influence over the model geometry when we dont have an animated model.
        boneTransform = mat4(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        );
    }

    vec4 worldPosition = transform * boneTransform * vec4(vPosition.xyz, 1.0);

    gl_Position = view * worldPosition;
}