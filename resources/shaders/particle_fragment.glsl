
#version 410

in vec4 passColor;
in vec2 passCoords;

uniform sampler2D textureID;

void main(void){
    vec4 albedo = texture(textureID, passCoords);
    gl_FragColor = vec4(passColor.xyz, 1);
}