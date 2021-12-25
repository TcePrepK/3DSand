#version 450 core

in vec2 position;

uniform mat4 viewMatrix;
uniform vec2 displayRes;
uniform vec2 viewportRes;
uniform vec3 lookFrom;
uniform vec3 lookTo;

out vec2 resolution;
out vec3 topLeftCorner;
out vec3 xIncrement;
out vec3 yIncrement;
out vec3 cameraPos;

void main(void) {
    gl_Position = vec4(position, 0.0, 1.0);
    cameraPos = lookFrom;
    resolution = displayRes;

    vec3 cameraDirection = normalize(lookTo - lookFrom);
    vec3 camRightVector = vec3(viewMatrix[0][0], viewMatrix[1][0], viewMatrix[2][0]);
    vec3 camUpVector = vec3(viewMatrix[0][1], viewMatrix[1][1], viewMatrix[2][1]);
    topLeftCorner = (cameraDirection - camRightVector) * (viewportRes.x / 2) + (camUpVector * (viewportRes.y / 2));
    xIncrement = (camRightVector * viewportRes.x) / displayRes.x;
    yIncrement = (camUpVector * -viewportRes.y) / displayRes.y;
}