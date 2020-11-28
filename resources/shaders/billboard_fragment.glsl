#version 410
layout(location = 0) in vec4 passColor;

in vec2 passCoords;

//Texture Units
uniform sampler2D textureID;

void main(void){
    gl_FragColor = vec4(1, 0, 0, 1);
}