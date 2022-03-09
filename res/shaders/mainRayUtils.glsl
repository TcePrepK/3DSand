struct Ray {
    vec3 pos;
    vec3 dir;
    vec3 color;
};

struct HitRecord {
    vec3 position;
    uvec3 hitVoxel;
    vec3 normal;
    float distance;
    bool light;
    int id;
};

struct WorldBox {
    float firstHitTime;
    float secondHitTime;
    float timeDistance;
};
WorldBox world = WorldBox(0, 0, 0);

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

bool closeEnough(float n, float v, float e) {
    return abs(n - v) < e;
}

vec2 randState = gl_FragCoord.xy * randVector2D;
vec2 rand2D() {
    randState.x = fract(sin(dot(randState.xy, vec2(12.9898, 78.233))) * 43758.5453);
    randState.y = fract(sin(dot(randState.xy, vec2(12.9898, 78.233))) * 43758.5453);

    return randState * 2.0 - 1.0;
}

//ivec2 intTexture(sampler2D image, vec2 position) {
//    return ivec2(texture(image, position) * 255);
//}
//
//ivec3 intTexture(sampler3D image, vec3 position) {
//    return ivec3(texture(image, position) * 255);
//}

bool inBounds(vec3 v, vec3 s) {
    return !(v.x < 0 || v.x >= s.x || v.y < 0 || v.y >= s.y || v.z < 0 || v.z >= s.z);
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

    vec3 result = vec3(cos(phi) * r, sin(phi) * r, u);

    if (result.x == 0) {
        result.x = 0.001;
    }
    if (result.y == 0) {
        result.y = 0.001;
    }
    if (result.z == 0) {
        result.z = 0.001;
    }

    return normalize(result);
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

bool testForBorder(Ray ray, float distance, vec3 size) {
    const vec3 pos = fract(at(ray, distance) / size);
    const float e = pow(distance, 0.5) / 500;
    const bool closeX = closeEnough(pos.x, 0, e);
    const bool closeY = closeEnough(pos.y, 0, e);
    const bool closeZ = closeEnough(pos.z, 0, e);
    return ((closeX && closeY) || (closeX && closeZ) || (closeY && closeZ));
}

void DDAStep(ivec3 stepDir, vec3 tS, inout uvec3 gridCoords, inout vec3 tV, out float dist, out int idx) {
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

void DDA(inout Ray ray, inout HitRecord record) {
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

    uvec3 gridCoords = uvec3(floor(at(ray, record.distance + off)));

    vec3 tV = rayInverse * (vec3(gridCoords + voxExit) - ray.pos);
    vec3 tS = rayInverse * vec3(stepDir);

    int hitId = 0;
    ray.color = vec3(1);
    while (record.distance < world.secondHitTime - off) {
        if (!inBounds(gridCoords, vec3(32 * chunkScale.x))) {
            break;
        }

        ivec3 chunkPos = ivec3(gridCoords / 32);
        uint chunkIDX = chunkPos.x + (chunkPos.y * chunkScale.x) + (chunkPos.z * chunkScale.x * chunkScale.y);
        vec3 chunkOffset = gridCoords % 32 / 32.0;

        hitId = int(texture(sampler3D(chunkBuffer.textures[chunkIDX]), chunkOffset).r * 255);

        if (hitId != 0) {
            if (hitId == 1) {
                record.light = true;
            }

            break;
        }

        DDAStep(stepDir, tS, gridCoords, tV, record.distance, idx);

        //        int bitmask = int(texture(bitmaskTexture, texturePos).r * 255);
        //        if (bitmask == 0) {
        //            if (isRenderingBitmask) {
        //                if (testForBorder(ray, record.distance, stepDir * 4)) {
        //                    ray.color = vec3(0);
        //                } else if (ray.color != vec3(0)) {
        //                    ray.color = vec3(0.1, 2, 0.1);
        //                }
        //            }
        //
        //            vec3 currPos = at(ray, record.distance);
        //            const vec3 distToBorder = rayInverse * (voxExit * 4 - (fract(currPos) + (gridCoords % 4)));
        //            const float time = min(min(distToBorder.x, distToBorder.y), distToBorder.z);
        //            idx = time == distToBorder.x ? 0 : time == distToBorder.y ? 1 : 2;
        //
        //            record.distance += time + off;
        //            currPos = at(ray, record.distance);
        //            gridCoords = ivec3(floor(currPos));
        //            tV = record.distance + rayInverse * (voxExit - fract(currPos));
        //        } else {
        //            if (isRenderingBitmask && testForBorder(ray, record.distance, stepDir * 4)) {
        //                ray.color = vec3(0);
        //            }
        //
        //            hitId = int(texture(worldTexture, texturePos).r * 255);
        //            if (hitId != 0) {
        //                if (hitId == 1) {
        //                    record.light = true;
        //                }
        //
        //                break;
        //            }
        //
        //            DDAStep(stepDir, tS, gridCoords, tV, record.distance, idx);
        //        }
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

        ray.color *= cubeColor;
    }
}

HitRecord PrimaryDDA(inout Ray ray) {
    HitRecord record = HitRecord(vec3(0), ivec3(0), vec3(0), 0, false, 0);
    DDA(ray, record);

    outDepth = record.distance / maxDistance;

    return record;
}

HitRecord FinderDDA(inout Ray ray) {
    HitRecord record = HitRecord(vec3(0), ivec3(0), vec3(0), 0, false, 0);
    DDA(ray, record);

    return record;
}

bool LightDDA(inout Ray ray, inout HitRecord record) {
    DDA(ray, record);
    return record.light;
}

HitRecord ColorDDA(inout Ray ray) {
    HitRecord record = PrimaryDDA(ray);
    if (record.light) {
        return record;
    }

    if (!isPathTracing) {
        ray.color = abs(record.hitVoxel / textureScale - 0.5) * 2;
        return record;
    }

    vec3 offHitPoint = record.position + record.normal / 100;

    vec3 randDir = getNewDirection();
    float product = dot(randDir, record.normal);
    if (product < 0) {
        randDir = normalize(-randDir);
        product *= -1;
    }

    HitRecord lightRecord = HitRecord(vec3(0), ivec3(0), vec3(0), 0, false, 0);
    Ray lightRay = Ray(offHitPoint, randDir, vec3(0));
    if (LightDDA(lightRay, lightRecord)) {
        ray.color *= getSkyColor(randDir);

        return record;
    }

    ray.color = vec3(0);
    return record;
}