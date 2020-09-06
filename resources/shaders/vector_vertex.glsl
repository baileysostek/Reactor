#version 400 core

in vec3 position;
in vec3 color;

out vec3 passColor;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main(void){
    gl_Position =  projectionMatrix * viewMatrix * vec4(position, 1.0);
    passColor = color;
}
