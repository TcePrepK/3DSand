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
uniform sampler2D oldNormalAttachment;

const int maxDist = 500;
const int maxFrameCount = 255;
const bool renderingFractal = true;

layout (location = 0) out vec3 outColor;
layout (location = 1) out float outDepth;
layout (location = 2) out vec3 outRayDir;
layout (location = 3) out float outFrameCount;
layout (location = 4) out vec3 outNormal;

#include /shaders/fractals.glsl
#include /shaders/rayUtils.glsl

void resetPixel(Ray ray, HitRecord record) {
    const vec3 gridPosition = record.position;
    vec4 screenPos = oldMVPMatrix * vec4(gridPosition, 1);
    screenPos /= screenPos.w;
    vec2 screenPixelPos = screenPos.xy * 0.5 + 0.5;
    screenPixelPos.y = 1 - screenPixelPos.y;

    const vec3 oldRayDir = texture(oldRayDirAttachment, screenPixelPos).rgb;
    const vec3 oldNormal = texture(oldNormalAttachment, screenPixelPos).rgb;

    //    if (oldNormal != vec3(0) && oldNormal == record.normal) {
    if (oldNormal != vec3(0)) {
        Ray oldRay = Ray(oldCameraPos, oldRayDir, vec3(0), false);
        HitRecord oldRecord = FinderDDA(oldRay, 0);

        const float threshold = 0.1 * outDepth;
        const float dist = length(oldRecord.position - record.position);
        if (dist < threshold) {
            const float dotNormal = dot(oldNormal, record.normal);
            const float normalWeight = map(dotNormal, -0.5, 1, 0, 1);
            const float distWeight = map(dist, 0, threshold, 1, 0);

            const float weight = normalWeight * distWeight;

            const vec3 oldColor = texture(oldColorAttachment, screenPixelPos).rgb;
            outColor = (weight * oldColor) + ((1 - weight) * ray.color);
            outFrameCount = outFrameCount * weight;

            return;
        }
    }

    outFrameCount = 0;
    outColor = ray.color;
}

void main(void) {
    vec2 pixelPosition = gl_FragCoord.xy / resolution;
    float frameCount = int(texture(frameCountAttachment, pixelPosition).r * (maxFrameCount + 1));
    outFrameCount = (frameCount + 1.0) / maxFrameCount;

    vec3 rayDir = normalize(topLeftCorner + (gl_FragCoord.x * xIncrement) + (gl_FragCoord.y * yIncrement));
    Ray ray = Ray(cameraPos, rayDir, vec3(0), false);
    outRayDir = rayDir;
    HitRecord record = ColorDDA(ray);
    outNormal = record.normal;

    const vec2 offset = rand2D() / outDepth / 200;
    const vec3 additionColor = texture(oldColorAttachment, pixelPosition + offset).rgb;
    const vec3 oldColor = texture(oldColorAttachment, pixelPosition).rgb;

    vec2 colorWeight = vec2(frameCount / (frameCount + 1.0), 1 / (frameCount + 1.0));
    outColor = (oldColor * colorWeight.x) + (ray.color * colorWeight.y);

    if (resetEverything) {
        resetPixel(ray, record);
    }
}