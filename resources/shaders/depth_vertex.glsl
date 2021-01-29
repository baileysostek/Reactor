#version 400

//Set prescision
precision highp int;
precision highp float;
precision highp sampler2D;

// Inputs
layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec3 vNormal;
layout(location = 2) in vec3 vTangent;
layout(location = 3) in vec3 vBitangent;
layout(location = 4) in vec2 vTexture;

layout(location = 5) in vec4 transform_0;
layout(location = 6) in vec4 transform_1;
layout(location = 7) in vec4 transform_2;
layout(location = 8) in vec4 transform_3;

//Uniform variables
uniform mat4 lightSpaceMatrix; // objects transform in space


//Main function to run
void main(){
    //reconstruct our IN matricies
    mat4 model = mat4(
    transform_0,
    transform_1,
    transform_2,
    transform_3
    );

    gl_Position = lightSpaceMatrix * model * vec4(vPosition.xyz, 1.0);
}