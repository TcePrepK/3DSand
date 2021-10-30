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
    vec3 rayDir = normalize(topLeftCorner + (gl_FragCoord.x * xIncrement) + ((screenHeight - gl_FragCoord.y) * yIncrement));

    Ray ray = Ray(cameraPos, rayDir, vec3(0));
    ColorDDA(ray);

    out_color = vec4(ray.color, 1);

    //    float x = gl_FragCoord.x / screenWidth;
    //    float y = gl_FragCoord.y / screenHeight;
    //    out_color = vec4(x, y, 1, 1);
}