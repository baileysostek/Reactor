#version 400

//Set prescision
precision highp int;
precision highp float;
precision highp sampler2D;

// Inputs
in vec4 vPosition;

//Uniform variables
uniform mat4 lightSpaceMatrix; // objects transform in space
uniform mat4 model; // objects transform in space


//Main function to run
void main(){
    gl_Position = lightSpaceMatrix * model * vec4(vPosition.xyz, 1.0);
}