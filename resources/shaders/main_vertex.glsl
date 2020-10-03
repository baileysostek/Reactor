#version 400

//Set prescision
precision highp int;
precision highp float;
precision highp sampler2D;

#define maxLights 25

// Inputs
in vec4 vPosition;
in vec3 vNormal;
in vec2 vTexture;

//Uniform variables
uniform mat4 transformation; // objects transform in space
uniform mat4 view;           // Cameras position in space
uniform vec3 cameraPos;      // Cameras position in worldSpace
uniform mat4 perspective;    //Perspective of this world

uniform vec2 t_offset;    //texture offset
uniform vec2 t_scale;     //texture scale

//Lighting
uniform mat4 lightSpaceMatrix[maxLights];

// Outputs
out vec3 passNormal;
out vec3 passCamPos;
out vec2 passCoords;
out vec3 passFragPos;

//Reflection
out vec3 passReflectNormal;

//Lighting
out vec4[maxLights] passPosLightSpace;

//Main function to run
void main(){
    //Transdform the normnal vectors of this model by its transform.
    vec4 offsetNormal = transformation *  vec4(vNormal.xyz, 1.0);
    vec4 worldOffset = transformation * vec4(0, 0, 0, 1);
    passNormal = normalize((vec3(offsetNormal) / offsetNormal.w) - (worldOffset.xyz)/worldOffset.w);

    vec4 worldPosition = transformation * vec4(vPosition.xyz, 1.0);
    passFragPos = worldPosition.xyz;

    passCoords = vTexture;

    //Camera Direction
    passCamPos = cameraPos;

    passReflectNormal = reflect(normalize(worldPosition.xyz - (cameraPos * -1)), passNormal);

    for(int i = 0; i < maxLights; i++){
        passPosLightSpace[i] = lightSpaceMatrix[i] * worldPosition;
    }

    gl_Position = perspective * view * worldPosition;
}