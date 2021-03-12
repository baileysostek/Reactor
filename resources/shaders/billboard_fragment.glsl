
#version 410

in vec3 passColor;
in vec2 passCoords;

uniform sampler2D textureID;

void main(void){
    vec4 albedo = texture(textureID, passCoords);
    gl_FragColor = vec4(albedo.rgb * passColor, albedo.a);
}