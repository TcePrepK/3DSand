#version 450 core

uniform mat4 mvpMatrix;
uniform vec3 topLeftCorner;
uniform vec3 xIncrement;
uniform vec3 yIncrement;
uniform vec2 resolution;
uniform vec3 cameraPos;
uniform vec2 randVector2D;
uniform vec2 colorWeights;
uniform vec3 textureScale;
uniform vec3 chunkScale;

layout (location = 0) uniform sampler3D worldTexture;
layout (location = 1) uniform sampler2D oldColorAttachment;
layout (location = 2) uniform sampler2D oldDepthAttachment;

const int maxDist = 500;

layout (location = 0) out vec3 outColor;
layout (location = 1) out vec3 outDepth;

#include /shaders/rayUtils.glsl

void main(void) {
    vec2 pixelPosition = gl_FragCoord.xy / resolution;
    vec4 oldColor = texture(oldColorAttachment, pixelPosition);

    vec3 rayDir = normalize(topLeftCorner + (gl_FragCoord.x * xIncrement) + (gl_FragCoord.y * yIncrement));

    Ray ray = Ray(cameraPos, rayDir, vec3(0));
    ColorDDA(ray);

    outColor = oldColor.rgb * colorWeights.x + ray.color * colorWeights.y;

    //    float x = gl_FragCoord.x / screenWidth;
    //    float y = gl_FragCoord.y / screenHeight;
    //    out_color = vec4(x, y, 1, 1);
}