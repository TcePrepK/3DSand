#version 450 core

uniform mat4 mvpMatrix;
uniform vec3 topLeftCorner;
uniform vec3 xIncrement;
uniform vec3 yIncrement;
uniform int screenWidth;
uniform int screenHeight;
uniform vec3 cameraPos;

uniform vec3 textureScale;
uniform vec3 chunkScale;

uniform sampler3D worldTexture;

out vec4 out_color;

const int maxDist = 500;

#include /shaders/rayUtils.glsl

void main(void) {
    vec3 rayDir = normalize(topLeftCorner + xIncrement * gl_FragCoord.x + yIncrement * gl_FragCoord.y);

    Ray ray = Ray(cameraPos, rayDir, vec3(0));
    ColorDDA(ray);

    out_color = vec4(ray.color, 1);
}