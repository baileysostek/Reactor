#version 410
layout(location = 0) in vec4 passColor;

void main(void){
    gl_FragColor = vec4(normalize(passColor.xyz), 1);
}