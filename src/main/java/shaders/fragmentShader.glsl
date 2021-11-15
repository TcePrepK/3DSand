#version 450 core

in vec2 resolution;
in vec3 topLeftCorner;
in vec3 xIncrement;
in vec3 yIncrement;
in vec3 cameraPos;

uniform mat4 oldMVPMatrix;
uniform mat4 oldViewMatrix;

uniform vec2 randVector2D;
uniform vec3 textureScale;
uniform vec3 chunkScale;
uniform float wFactor;
uniform bool resetEverything;
uniform vec3 oldCameraPos;

uniform sampler3D worldTexture;
uniform sampler2D oldColorAttachment;
uniform sampler2D oldDepthAttachment;
uniform sampler2D oldRayDirAttachment;
uniform sampler2D frameCountAttachment;

const int maxDist = 500;
const int maxFrameCount = 255;
const bool renderingFractal = true;

layout (location = 0) out vec3 outColor;
layout (location = 1) out float outDepth;
layout (location = 2) out vec3 outRayDir;
layout (location = 3) out float outFrameCount;

#include /shaders/fractals.glsl
#include /shaders/rayUtils.glsl

void resetPixel(Ray ray, HitRecord record) {
    vec4 screenPos = oldMVPMatrix * vec4(record.position, 1);
    screenPos /= screenPos.w;
    vec2 screenPixelPos = screenPos.xy * 0.5 + 0.5;
    screenPixelPos.y = 1 - screenPixelPos.y;

    vec3 oldRayDir = texture(oldRayDirAttachment, screenPixelPos).rgb;
    float distance = texture(oldDepthAttachment, screenPixelPos).r;
    Ray oldRay = Ray(oldCameraPos, oldRayDir, vec3(0), false);
    HitRecord oldRecord = FinderDDA(oldRay, distance);

    if (length(oldRecord.position - record.position) < 0.01 * outDepth && oldRecord.normal == record.normal) {
        outColor = texture(oldColorAttachment, screenPixelPos).rgb;
    } else {
        outFrameCount = 0;
        outColor = ray.color;
    }
}


void main(void) {
    vec2 pixelPosition = gl_FragCoord.xy / resolution;
    vec4 oldColor = texture(oldColorAttachment, pixelPosition);
    float frameCount = int(texture(frameCountAttachment, pixelPosition).r * (maxFrameCount + 1));
    outFrameCount = (frameCount + 1.0) / maxFrameCount;

    vec3 primaryDir = normalize(topLeftCorner + (gl_FragCoord.x * xIncrement) + (gl_FragCoord.y * yIncrement));
    Ray primaryRay = Ray(cameraPos, primaryDir, vec3(0), false);
    DepthDDA(primaryRay);
    outRayDir = primaryDir;

    vec2 offset = rand2D() / 2;
    vec3 rayDir = normalize(topLeftCorner + ((gl_FragCoord.x + offset.x) * xIncrement) + ((gl_FragCoord.y + offset.y) * yIncrement));
    Ray ray = Ray(cameraPos, rayDir, vec3(0), false);
    HitRecord record = ColorDDA(ray);

    vec2 colorWeight = vec2(frameCount / (frameCount + 1.0), 1 / (frameCount + 1.0));

    outColor = oldColor.rgb * colorWeight.x + ray.color * colorWeight.y;
    if (resetEverything) {
        resetPixel(ray, record);
    }
}