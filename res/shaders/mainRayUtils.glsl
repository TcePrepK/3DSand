struct Ray {
    vec3 pos;
    vec3 dir;
    vec3 color;
    bool lightRay;
};

struct HitRecord {
    vec3 position;
    ivec3 hitVoxel;
    vec3 normal;
    float distance;
    bool light;
    int id;
};

struct WorldBox {
    float firstHitTime;
    float secondHitTime;
    float timeDistance;
} world;

float clamp(float v, float min, float max) {
    if (v < min) return min;
    if (v > max) return max;
    return v;
}

float map(float n, float start1, float stop1, float start2, float stop2) {
    const float newval = (n - start1) / (stop1 - start1) * (stop2 - start2) + start2;
    if (start2 < stop2) {
        return clamp(newval, start2, stop2);
    } else {
        return clamp(newval, stop2, start2);
    }
}

vec3 at(vec3 pos, vec3 dir, float time) {
    return pos + dir * time;
}

vec3 at(Ray ray, float time) {
    return ray.pos + ray.dir * time;
}

bool inBounds(vec2 texturePos) {
    return (texturePos.x > 0 && texturePos.y > 0 && texturePos.x < 1 && texturePos.y < 1);
}

bool inBounds(vec3 texturePos) {
    return (texturePos.x > 0 && texturePos.y > 0 && texturePos.z > 0 && texturePos.x < 1 && texturePos.y < 1 && texturePos.z < 1);
}

bool inBounds(float height) {
    return (height < chunkScale.y || height >= 0);
}

vec2 randState = gl_FragCoord.xy * randVector2D;
vec2 rand2D() {
    randState.x = fract(sin(dot(randState.xy, vec2(12.9898, 78.233))) * 43758.5453);
    randState.y = fract(sin(dot(randState.xy, vec2(12.9898, 78.233))) * 43758.5453);

    return randState * 2.0 - 1.0;
}

ivec2 intTexture(sampler2D image, vec2 position) {
    return ivec2(texture(image, position) * 255);
}

ivec3 intTexture(sampler3D image, vec3 position) {
    return ivec3(texture(image, position) * 255);
}

vec3 getSkyColor(vec3 dir) {
    float time = 0.5 * (dir.y + 1);
    return vec3(0.5, 0.7, 1) * time + (1 - time);
}

vec3 randomizeNormal(vec3 normal) {
    return normalize(vec3(normal.xy + rand2D(), normal.z));
}

vec3 getNewDirection() {
    vec2 rng = rand2D();
    float v = rng.x;
    float u = rng.y;

    float r = sqrt(1 - u * u);
    float phi = 2 * 3.1415 * v;

    return normalize(vec3(cos(phi) * r, sin(phi) * r, u));
}

bool between2Points1D(int v, int min, int max) {
    int finalMin = min < max ? min : max;
    int finalMax = min < max ? max : min;
    if (v < finalMin || v > finalMax) {
        return false;
    }

    return true;
}

vec2 AABB(vec3 rayPos, vec3 rayDir, vec3 boxMin, vec3 boxMax) {
    vec3 tMin = (boxMin - rayPos) / rayDir;
    vec3 tMax = (boxMax - rayPos) / rayDir;
    vec3 t1 = min(tMin, tMax);
    vec3 t2 = max(tMin, tMax);
    float tNear = max(max(t1.x, t1.y), t1.z);
    float tFar = min(min(t2.x, t2.y), t2.z);
    return vec2(tNear, tFar);
}

void DDAStep(ivec3 stepDir, vec3 tS, in out ivec3 gridCoords, in out vec3 tV, out float dist, out int idx) {
    dist = min(min(tV.x, tV.y), tV.z);
    idx = dist == tV.x ? 0 : dist == tV.y ? 1 : 2;

    if (idx == 0) {
        gridCoords.x += stepDir.x;
        tV.x += tS.x;
    } else if (idx == 1) {
        gridCoords.y += stepDir.y;
        tV.y += tS.y;
    } else {
        gridCoords.z += stepDir.z;
        tV.z += tS.z;
    }
}

void DDA(in out Ray ray, in out HitRecord record) {
    const float off = 0.001;

    const bvec3 dirSign = greaterThanEqual(ray.dir, vec3(0));

    const ivec3 stepDir = mix(ivec3(-1), ivec3(1), dirSign);
    const ivec3 voxExit = mix(ivec3(0), ivec3(1), dirSign);
    const vec3 rayInverse = 1 / ray.dir;

    // Calculating world
    vec3 tMin = (0 - ray.pos) / ray.dir;
    vec3 tMax = (textureScale - ray.pos) / ray.dir;
    vec3 t1 = min(tMin, tMax);
    vec3 t2 = max(tMin, tMax);
    float closestTime = max(max(t1.x, t1.y), t1.z);
    float fartestTime = min(min(t2.x, t2.y), t2.z);
    int idx = closestTime == t1.x ? 0 : closestTime == t1.y ? 1 : 2;

    if (closestTime < 0) {
        closestTime = 0;
    }

    if (fartestTime <= closestTime) {
        fartestTime = closestTime = maxDistance;
    }

    if (fartestTime - closestTime > maxDistance) {
        fartestTime = closestTime + maxDistance;
    }

    record.distance = closestTime;

    world.firstHitTime = closestTime;
    world.secondHitTime = fartestTime;

    ivec3 gridCoords = ivec3(floor(at(ray, record.distance)));

    vec3 tV = rayInverse * (vec3(gridCoords + voxExit) - ray.pos);
    vec3 tS = rayInverse * vec3(stepDir);

    //    int idx = fartestTime == t2.x ? 0 : fartestTime == t2.y ? 1 : 2;
    int hitId = 0;
    while (record.distance < world.secondHitTime) {
        vec3 texturePos = gridCoords / textureScale;
        int bitmask = intTexture(bitmaskTexture, texturePos).r;
        if (bitmask == 0) {
            vec3 currPos = at(ray, record.distance);
            const vec3 distToBorder = rayInverse * (voxExit * 4 - (fract(currPos) + (gridCoords % 4)));
            const float time = min(min(distToBorder.x, distToBorder.y), distToBorder.z);
            idx = time == distToBorder.x ? 0 : time == distToBorder.y ? 1 : 2;

            record.distance += time + off;
            currPos = at(ray, record.distance);
            gridCoords = ivec3(floor(currPos));
            tV = record.distance + rayInverse * (voxExit - fract(currPos));
        } else {
            hitId = intTexture(worldTexture, texturePos).r;
            if (hitId != 0) {
                if (hitId == 1) {
                    record.light = true;
                }

                break;
            }

            DDAStep(stepDir, tS, gridCoords, tV, record.distance, idx);
        }
    }

    if (hitId == 0) {
        record.light = true;
    }

    if (hitId != 0) {
        if (idx == 0) {
            record.normal.x = -stepDir.x;
        } else if (idx == 1) {
            record.normal.y = -stepDir.y;
        } else {
            record.normal.z = -stepDir.z;
        }
    }

    record.position = at(ray, record.distance);
    record.hitVoxel = gridCoords;

    const vec3 skyColor = getSkyColor(ray.dir);
    if (hitId == 0) {
        ray.color = skyColor;
        record.light = true;
    } else {
        vec3 cubeColor = vec3(0);
        if (hitId >= 2) {
            //            cubeColor = vec3(0.65, 0.4, 0.3);
            cubeColor = vec3(0.5, 0.2, 0.5);
        } else {
            //            cubeColor = vec3(1);
            cubeColor = abs(gridCoords / textureScale - 0.5) * 2;
        }

        ray.color = cubeColor;
    }
}

HitRecord PrimaryDDA(in out Ray ray) {
    HitRecord record;
    DDA(ray, record);

    outDepth = record.distance / maxDistance;

    return record;
}

HitRecord FinderDDA(in out Ray ray, float distance) {
    HitRecord record = HitRecord(vec3(0), ivec3(0), vec3(0), distance, false, 0);
    DDA(ray, record);

    return record;
}

bool LightDDA(in out Ray ray, in out HitRecord record) {
    DDA(ray, record);
    return record.light;
}

HitRecord ColorDDA(in out Ray ray) {
    HitRecord record = PrimaryDDA(ray);
    if (record.light) {
        return record;
    }

    if (!isPathTracing) {
        ray.color = abs(record.hitVoxel / textureScale - 0.5) * 2;
        return record;
    }

    vec3 offHitPoint = record.position + record.normal / 100;
    vec3 fakeColor = vec3(0);

    vec3 randDir = getNewDirection();
    float product = dot(randDir, record.normal);
    if (product < 0) {
        randDir = normalize(-randDir);
        product *= -1;
    }

    HitRecord lightRecord = HitRecord(vec3(0), ivec3(0), vec3(0), 0, false, 0);
    Ray lightRay = Ray(offHitPoint, randDir, vec3(0), true);
    if (LightDDA(lightRay, lightRecord)) {
        ray.color *= getSkyColor(randDir);
        //        outLight = lightRecord.distance / maxDistance;

        return record;
    }

    //    int iter = 0;
    //    vec3 bounceDir = ray.dir;
    //    HitRecord bounceRecord = HitRecord(lightRecord.normal, lightRecord.position, 0, false, 0);
    //    while (++iter <= 20) {
    //        bounceDir = reflect(bounceDir, bounceRecord.normal);
    //        Ray bounceRay = Ray(bounceRecord.position + bounceRecord.normal / 100, bounceDir, vec3(0));
    //
    //        if (LightDDA(bounceRay, bounceRecord)) {
    //            break;
    //        }
    //    }
    //
    //    ray.color *= getSkyColor(bounceDir) / iter;
    //
    //    vec3 offPosition = lightRecord.position + lightRecord.normal / 100;
    //    Ray bounceRay = Ray(offPosition, reflect(fakeRay.dir, lightRecord.normal), vec3(0));
    //    if (LightDDA(fakeRay, lightRecord)) {
    //        ray.color *= getSkyColor(fakeDir) / 2;
    //        return;
    //    }

    ray.color = fakeColor;
    return record;
}