#version 400

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
uniform mat4 perspective;    //Perspective of this world

//Bones
uniform mat4 jointTransforms[MAX_JOINTS];

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
    vec4 worldPosition = transform * boneTransform * vec4(vPosition.xyz, 1.0);

    gl_Position = view * worldPosition;
}