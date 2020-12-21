#version 400 core

in vec3 passColor;
in mat4 projMatrixInv;
in mat4 viewMatrixInv;

uniform sampler2D depthTexture;
uniform sampler2D sceneTexture;

uniform vec2 screenSize;

out vec4 out_Color;

vec2 textureSampler(){
    vec2 samplePosition =  gl_FragCoord.xy /screenSize;

    samplePosition.y = 1 - samplePosition.y;

    return samplePosition;
}

void main(void){
    out_Color = vec4(passColor, 1.0);
}
