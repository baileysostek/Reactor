#version 410

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec4 translation;
layout(location = 3) in vec4 color;
layout(location = 4) in vec3 scale;
layout(location = 5) in vec2 tCoords;

uniform mat4 view;
uniform mat4 projection;
uniform int rotateToFaceCamera;

out vec4 passColor;
out vec2 passCoords;
out vec3 passNormal;

void main(void){
    vec3 CameraRight_worldspace = vec3(view[0][0], view[1][0], view[2][0]);
    vec3 CameraUp_worldspace    = vec3(view[0][1], view[1][1], view[2][1]);
    vec3 vertexPosition_worldspace =
    translation.xyz
    + CameraRight_worldspace * (position.x * scale.x)
    + CameraUp_worldspace * (position.y * scale.y);

    vec4 worldPosition;

    if (false){
        worldPosition = vec4(vertexPosition_worldspace, 1.0);
    } else {
        worldPosition = (vec4(2) * translation) + vec4(position * scale, 1.0);
    }

    gl_Position = projection * view * worldPosition;
	passColor = color;

    passCoords = tCoords;

    passNormal = normal;
}