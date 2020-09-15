#version 400
// Fragment shader which takes interpolated colours and uses them to set the final fragment colour.
// Floating point values in fragment shaders must have a precision set.
// This can be done globally (as done here) or per variable.

//Set prescision
precision highp int;
precision highp float;
precision highp sampler2D;

// Inputs
in vec3 passNormal;
in vec3 passCamPos;
in vec3 cameraDir;
in vec2 passCoords;

uniform sampler2D texureID;
uniform vec3 sunAngle;


float dotProduct(vec3 posI, vec3 posJ){
    return (posI.x * posJ.x) + (posI.y * posJ.y) + (posI.z * posJ.z);
}

layout( location = 0 ) out vec4 gl_FragColor;

void main(void){
//    vec2 normal = gl_FragCoord.xy / gl_FragCoord.w;
//    out_Color = vec4((sin(gl_FragCoord.x / 10.0) + 1.0) / 2.0, (sin(gl_FragCoord.y / 10.0) + 1.0) / 2.0, 1.0, 1.0);

    gl_FragColor = vec4(dotProduct(passNormal, sunAngle * -1) * vec3(1), 1);


//    gl_FragColor = vec4(passCoords, 0, 1);


    vec4 textureColor = texture(texureID, passCoords);

    if(textureColor.a < 0.5){
        discard;
    }

//    textureColor.xyz = textureColor.xyz * dotProduct(passNormal, sunAngle);



//    gl_FragColor = textureColor;

}