#version 410

layout(location = 0) in vec3 position;
layout(location = 1) in vec4 translation;
layout(location = 2) in vec4 color;

uniform mat4 view;
uniform mat4 projection;

out vec4 passColor;

void main(void){
	vec4 worldPosition = vec4(translation.xyz + position, 1.0);
    gl_Position = projection * view * worldPosition;
	passColor = color;
}