struct Ray {
    vec3 pos;
    vec3 dir;
    vec3 color;
};

vec3 at(Ray ray, float time) {
    return ray.pos + ray.dir * time;
}

bool inBounds(vec3 texturePos) {
    return (texturePos.x >= 0 && texturePos.y >= 0 && texturePos.z >= 0 && texturePos.x < 1 && texturePos.y < 1 && texturePos.z < 1);
}

bool inBounds(float height) {
    return (height < chunkScale.y || height >= 0);
}

vec2 randState = gl_FragCoord.xy;
vec2 rand2D() {
    randState.x = fract(sin(dot(randState.xy, vec2(12.9898, 78.233))) * 43758.5453);
    randState.y = fract(sin(dot(randState.xy, vec2(12.9898, 78.233))) * 43758.5453);

    return randState * 2.0 - 1.0;
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

void DDA(in out Ray ray, out vec3 hitPoint, out vec3 hitNormal, out bool reflected, out bool hitLight) {
    bvec3 dirSign = greaterThanEqual(ray.dir, vec3(0));

    ivec3 stepDir = mix(ivec3(-1), ivec3(1), dirSign);
    ivec3 voxExit = mix(ivec3(0), ivec3(1), dirSign);

    vec3 rayInverse = 1 / ray.dir;

    ivec3 gridCoords = ivec3(floor(ray.pos));

    vec3 tV = rayInverse * (vec3(gridCoords + voxExit) - ray.pos);
    vec3 tS = rayInverse * vec3(stepDir);

    float dist = 0;
    int idx = 0;

    bool colorize = true;
    int hitId = 0;

    reflected = false;
    hitLight = false;
    hitNormal = vec3(0);
    while (dist < maxDist) {
        if (inBounds(gridCoords.y)) {
            vec3 offGridCoords = vec3(gridCoords.x + textureScale.x / 2, gridCoords.y, gridCoords.z + textureScale.z / 2);
            vec3 texturePos = offGridCoords / textureScale;
            if (inBounds(texturePos)) {
                hitId = int(texture(worldTexture, texturePos).r * 256);
                if (hitId != 0) {
                    if (hitId == 1) {
                        hitLight = true;
                    }

                    break;
                }
            }
        }

        DDAStep(stepDir, tS, gridCoords, tV, dist, idx);
    }

    if (idx == 0) {
        hitNormal.x = -stepDir.x;
    } else if (idx == 1) {
        hitNormal.y = -stepDir.y;
    } else {
        hitNormal.z = -stepDir.z;
    }

    hitPoint = at(ray, dist);

    if (!colorize) {
        return;
    }

    vec3 skyColor = getSkyColor(ray.dir);
    if (hitId == 0) {
        ray.color = skyColor;
        hitLight = true;
    } else {
        vec3 cubeColor = vec3(0);
        if (hitId == 2) {
            cubeColor = vec3(0.65, 0.4, 0.3);
        } else {
            cubeColor = vec3(1);
        }

        float x = dist / maxDist;
        float visibility = exp(-pow(x * 1.2, 9.0));

        ray.color = mix(skyColor, cubeColor, visibility);
    }
}

bool LightDDA(Ray ray) {
    vec3 hitPoint = vec3(0);
    vec3 hitNormal = vec3(0);
    bool reflected = false;
    bool hitLight = false;
    DDA(ray, hitPoint, hitNormal, reflected, hitLight);

    return hitLight;
}

void DDA(in out Ray ray, out bool reflected, out bool hitLight) {
    vec3 hitPoint = vec3(0);
    vec3 hitNormal = vec3(0);
    DDA(ray, hitPoint, hitNormal, reflected, hitLight);
}

void ColorDDA(in out Ray ray) {
    vec3 hitPoint = vec3(0);
    vec3 hitNormal = vec3(0);
    bool reflected = false;
    bool hitLight = false;

    DDA(ray, hitPoint, hitNormal, reflected, hitLight);
    if (hitLight) {
        return;
    }

    //    float product = dot(hitNormal, vec3(0, 1, 0));
    //    ray.color = vec3(product);
    //    return;

    //    int iter = 0;
    //    vec3 fakeDir = ray.dir;
    //    vec3 fakeHit = hitPoint;
    //    vec3 fakeHitNormal = hitNormal;
    //    while (++iter <= 20) {
    //        vec3 fakeNormal = normalize(vec3(fakeHitNormal.xy + rand2D(), fakeHitNormal.z));
    //        fakeDir = reflect(fakeDir, fakeNormal);
    //        Ray fakeRay = Ray(hitPoint + fakeHitNormal / 100, fakeDir, vec3(0));
    //
    //        DDA(fakeRay, reflected, hitLight);
    //
    //        if (hitLight) {
    //            break;
    //        }
    //    }

    int rayAmount = 4;
    vec3 offHitPoint = hitPoint + hitNormal / 100;
    vec3 fakeColor = vec3(0);
    for (int i = 0; i < rayAmount; i++) {
        vec3 fakeDir = getNewDirection();
        float product = dot(fakeDir, hitNormal);
        if (product < 0) {
            fakeDir = normalize(-fakeDir);
            product *= -1;
        }

        Ray fakeRay = Ray(offHitPoint, fakeDir, vec3(0));
        if (LightDDA(fakeRay)) {
            fakeColor += ray.color * getSkyColor(fakeDir) / rayAmount * product;
        }
    }

    ray.color = fakeColor;
}

//bool ReflectionDDA(in out Ray ray) {
//    vec3 hitPoint = vec3(0);
//    vec3 hitNormal = vec3(0);
//    bool reflected = false;
//
//    DDA(ray, hitPoint, hitNormal, reflected);
//    if (reflected) {
//        vec3 tempDir = ray.dir;
//        int hitNum = 0;
//        while (reflected && hitNum++ < 5) {
//            tempDir = reflect(tempDir, hitNormal);
//            Ray tempRay = Ray(hitPoint + hitNormal / 100, tempDir, vec3(0));
//
//            DDA(tempRay, hitPoint, hitNormal, reflected);
//
//            ray.color += tempRay.color;
//        }
//
//        ray.color /= hitNum + 1;
//    }
//
//    return true;
//}