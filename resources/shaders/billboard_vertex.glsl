#version 410

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 translation;
layout(location = 2) in vec2 vTexture;

uniform mat4 view;
uniform mat4 projection;

out vec2 passCoords;

void main(void){

    vec3 scale = vec3(1);

    //Maths to make this face the camera
	vec3 CameraRight_worldspace = vec3(view[0][0], view[1][0], view[2][0]);
    vec3 CameraUp_worldspace    = vec3(view[0][1], view[1][1], view[2][1]);
//    vec3 vertexPosition_worldspace =
//        translation.xyz
//        + CameraRight_worldspace * position.x * scale.x
//        + CameraUp_worldspace * position.y * scale.y;

    vec3 vertexPosition_worldspace = translation + (position * scale);

	vec4 worldPosition = vec4(vertexPosition_worldspace, 1.0);

	//Get GL position
    gl_Position = projection * view * worldPosition;

    //Pass Texture Coordinates
	passCoords = vTexture;
}