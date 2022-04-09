#version 450 core
#extension GL_ARB_bindless_texture : require

in vec2 resolution;
in vec3 topLeftCorner;
in vec3 xIncrement;
in vec3 yIncrement;
in vec3 cameraPos;

uniform mat4 oldMVPMatrix;
uniform mat4 oldViewMatrix;
uniform vec3 oldCameraPos;

uniform vec2 randVector2D;
uniform vec3 sunPosition;
uniform vec3 textureScale;

uniform bool isPathTracing;
uniform bool isRenderingBitmask;
uniform int bitmaskSize;
uniform int lightBounceAmount;

uniform ivec3 chunkScale;
int chunkAmount = chunkScale.x * chunkScale.y * chunkScale.z;

layout(std430, binding = 0) readonly buffer ChunkBuffer {
    uvec2 textures[];
}chunkBuffer;

layout(std430, binding = 1) readonly buffer BitmaskBuffer {
    uvec2 textures[];
}bitmaskBuffer;

uniform sampler2D colorAttachment;
uniform sampler2D depthAttachment;
uniform sampler2D rayDirAttachment;
uniform sampler2D frameCountAttachment;
uniform sampler2D normalAttachment;
uniform sampler2D lightAttachment;

const float maxDistance = 500;
const int maxFrameCount = 255;

layout (location = 0) out vec3 outColor;
layout (location = 1) out float outDepth;
layout (location = 2) out vec3 outRayDir;
layout (location = 3) out float outFrameCount;
layout (location = 4) out vec3 outNormal;
layout (location = 5) out vec3 outLight;

//#include /shaders/mainFractals.glsl
#include /shaders/mainRayUtils.glsl

void applyFog(Ray ray, HitRecord record);
int calculatePixelFrame(Ray ray, HitRecord record, vec2 oldScreenPixelPos, int frameCount);

void pathTracing(void) {
    const vec2 pixelPosition = gl_FragCoord.xy / resolution;
    int frameCount = int(texture(frameCountAttachment, pixelPosition).r * (maxFrameCount + 1.0));

    //    const vec2 offset = rand2D() / 20.0;
    const vec2 offset = vec2(0);
    const vec2 targetPixel = gl_FragCoord.xy + offset;
    const vec3 rayDir = normalize(topLeftCorner + (targetPixel.x * xIncrement) + (targetPixel.y * yIncrement));

    Ray ray = Ray(cameraPos, rayDir, vec3(0));
    outRayDir = rayDir;
    HitRecord record = ColorDDA(ray);
    outNormal = record.normal;

    // Reprojection
    vec4 screenPos = oldMVPMatrix * vec4(record.position, 1);
    screenPos /= screenPos.w;
    vec2 oldScreenPixelPos = screenPos.xy * 0.5 + 0.5;
    oldScreenPixelPos.y = 1 - oldScreenPixelPos.y;

    // Calculate frame count
    frameCount = calculatePixelFrame(ray, record, oldScreenPixelPos, frameCount);
    // Calculate frame count

    const vec3 oldColor = texture(colorAttachment, oldScreenPixelPos).rgb;
    const vec2 colorWeight = vec2(frameCount / (frameCount + 1.0), 1 / (frameCount + 1.0));
    // Reprojection

    // Calculating outputs
    outColor = (oldColor * colorWeight.x) + (ray.color * colorWeight.y);
    outFrameCount = frameCount / float(maxFrameCount);
    // Calculating outputs

    // Fog
    //    applyFog(ray, record);
    // Fog
}

void primaryTracing(void) {
    const vec2 pixelPosition = gl_FragCoord.xy / resolution;
    const vec3 rayDir = normalize(topLeftCorner + (gl_FragCoord.x * xIncrement) + (gl_FragCoord.y * yIncrement));

    Ray ray = Ray(cameraPos, rayDir, vec3(0));
    ColorDDA(ray);
    outColor = ray.color;

    // Fog
    //    applyFog(ray, record);
    // Fog
}

void main(void) {
    if (isPathTracing) {
        pathTracing();
    } else {
        primaryTracing();
    }
}

int calculatePixelFrame(Ray ray, HitRecord record, vec2 oldScreenPixelPos, int frameCount) {
    if (record.normal == vec3(0)) {
        return 0;
    }

    const vec3 oldNormal = texture(normalAttachment, oldScreenPixelPos).rgb;
    if (oldNormal == vec3(0)) {
        return 0;
    }

    const float dotNormal = dot(oldNormal, record.normal);
    const float normalWeight = map(dotNormal, -0.5, 1, 0, 1);
    if (normalWeight == 0) {
        return 0;
    }

    const vec3 oldRayDir = texture(rayDirAttachment, oldScreenPixelPos).rgb;
    Ray oldRay = Ray(oldCameraPos, oldRayDir, vec3(0));
    HitRecord oldRecord = FinderDDA(oldRay);

    //    const float threshold = 0.1 * outDepth;
    const float threshold = length(2 / resolution * outDepth * maxDistance);
    const float dist = length(oldRecord.position - record.position);
    if (dist >= threshold) {
        return 0;
    }

    const float distWeight = map(dist, 0, threshold, 1, 0);
    const float weight = distWeight * normalWeight;

    return int(round(frameCount * weight)) + 1;
}

void applyFog(Ray ray, HitRecord record) {
    const float x = record.distance / maxDistance;
    const float visibility = exp(-pow(x * 1.2, 5.0));
    const vec3 skyColor = getSkyColor(ray.dir);

    outColor = mix(skyColor, outColor, visibility);
}