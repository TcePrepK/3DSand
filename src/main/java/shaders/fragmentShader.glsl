#version 450 core

uniform mat4 mvpMatrix;
uniform vec3 topLeftCorner;
uniform vec3 xIncrement;
uniform vec3 yIncrement;
uniform vec2 resolution;
uniform vec3 cameraPos;
uniform vec2 rVector2D;
uniform vec2 colorWeights;

uniform vec3 textureScale;
uniform vec3 chunkScale;

uniform sampler2D oldDisplay;
uniform sampler3D worldTexture;

const int maxDist = 500;

#include /shaders/rayUtils.glsl

void main(void) {
    vec2 texturePos = gl_FragCoord.xy / resolution;
    vec4 oldColor = texture(oldDisplay, texturePos);

    vec3 rayDir = normalize(topLeftCorner + (gl_FragCoord.x * xIncrement) + (gl_FragCoord.y * yIncrement));

    Ray ray = Ray(cameraPos, rayDir, vec3(0));
    ColorDDA(ray);

    gl_FragColor = oldColor * colorWeights.x + vec4(ray.color, 1) * colorWeights.y;

    //    float x = gl_FragCoord.x / screenWidth;
    //    float y = gl_FragCoord.y / screenHeight;
    //    out_color = vec4(x, y, 1, 1);
}