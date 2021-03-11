
#version 410

in vec2 passCoords;

uniform sampler2D textureID;

void main(void){
    vec4 albedo = texture(textureID, passCoords);
    gl_FragColor = albedo;
}