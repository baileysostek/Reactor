
#version 410

in vec4 passColor;
in vec2 passCoords;
in vec3 passNormal;

uniform sampler2D textureID;

void main(void){
    vec4 albedo = texture(textureID, passCoords);
    gl_FragColor = vec4(passColor.xyz * dot(passNormal, vec3(0, 1, 0)), 1);
}