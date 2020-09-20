#version 400

//Set prescision
precision highp int;
precision highp float;
precision highp sampler2D;

// Inputs
in vec4 vPosition;
in vec3 vNormal;
in vec2 vTexture;

//Uniform variables
uniform mat4 transformation; // objects transform in space
uniform mat4 view;           // Cameras position in space
//uniform vec3 inverseCamera;  // Cameras position in space
uniform mat4 perspective;    //Perspective of this world

uniform vec2 t_offset;    //texture offset
uniform vec2 t_scale;     //texture scale

//Lighting
uniform mat4 lightSpaceMatrix;

// Outputs
out vec3 passNormal;
out vec3 passCamPos;
out vec2 passCoords;

//Lighting
out vec4 passPosLightSpace;

//Main function to run
void main(){
    //Transdform the normnal vectors of this model by its transform.
    vec4 offsetNormal = transformation *  vec4(vNormal.xyz, 1.0);
    vec4 worldOffset = transformation * vec4(0, 0, 0, 1);
    passNormal = normalize((vec3(offsetNormal) / offsetNormal.w) - (worldOffset.xyz)/worldOffset.w);

    vec4 worldPosition = transformation * vec4(vPosition.xyz, 1.0);

    passCoords = vTexture;

    //Camera Direction
    passCamPos = (view * vec4(0, 0, 0, 1.0)).xyz;
//    vec3 delta = ((offsetCameraPos.xyz / offsetCameraPos.w) * vec3(-1, -1, -1)) - (worldPosition.xyz / worldPosition.w);
//    vec3 normalCamera = delta;
//    cameraDir = inverseCamera;

    passPosLightSpace = lightSpaceMatrix * worldPosition;
    gl_Position = perspective * view * worldPosition;
}