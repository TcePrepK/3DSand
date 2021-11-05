struct Ray {
    vec3 pos;
    vec3 dir;
    vec3 color;
    bool lightRay;
};

struct HitRecord {
    vec3 position;
    vec3 normal;
    float distance;
    bool light;
    int id;
};

int base3(int num) {
    float ret = 0;
    int factor = 1;
    while (num > 0) {
        ret += num % 3 * factor;
        num /= 3;
        factor *= 10;
    }

    return int(ret);
}

vec3 at(Ray ray, float time) {
    return ray.pos + ray.dir * time;
}

bool inBounds(vec2 texturePos) {
    return (texturePos.x >= 0 && texturePos.y >= 0 && texturePos.x < 1 && texturePos.y < 1);
}

bool inBounds(vec3 texturePos) {
    return (texturePos.x >= 0 && texturePos.y >= 0 && texturePos.z >= 0 && texturePos.x < 1 && texturePos.y < 1 && texturePos.z < 1);
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

int DDAIdGetter(ivec3 gridCoords) {
    //    vec3 texturePos = vec3(gridCoords.x + textureScale.x / 2, gridCoords.y, gridCoords.z + textureScale.z / 2) / textureScale;
    //    if (inBounds(texturePos)) {
    //        return int(texture(worldTexture, texturePos).r * 256);
    //    } else {
    //        return 0;
    //    }

    //    if (gridCoords.y != 0) {
    //        return 0;
    //    }


    // Classic Mandel Bulb

    //    vec3 pos = gridCoords / textureScale;
    //    if (pos.x < -2 || pos.x > 2 || pos.y < -2 || pos.y > 2 || pos.z < -2 || pos.z > 2) {
    //        return 0;
    //    }
    //
    //    int iter = 0;
    //    int maxIter = 8;
    //    vec3 Z = vec3(pos);
    //    vec3 C = vec3(Z);
    //    int n = 8;
    //    while (iter < maxIter) {
    //        float cr = length(Z);
    //        float a1 = atan(Z.y, Z.x);
    //        float a2 = atan(Z.z, length(Z.xy));
    //
    //        float r = pow(cr, n);
    //        a1 = a1 * n;
    //        a2 = a2 * n;
    //
    //        Z.x = r * cos(a2) * cos(a1);
    //        Z.y = r * cos(a2) * sin(a1);
    //        Z.z = r * sin(a2);
    //        Z += C;
    //
    //        if (cr >= 2) {
    //            return 0;
    //        }
    //
    //        iter++;
    //    }

    // Strange Mandel Bulb

    //    vec3 pos = gridCoords / textureScale;
    //    if (pos.x < -2 || pos.x > 2 || pos.y < -2 || pos.y > 2 || pos.z < -2 || pos.z > 2) {
    //        return 0;
    //    }
    //
    //    int iter = 0;
    //    int maxIter = 8;
    //    vec4 Z = vec4(pos, 1);
    //    vec4 C = vec4(Z);
    //    while (iter < maxIter) {
    //        float zx = pow(Z.x, 2) - pow(Z.y, 2) - pow(Z.z, 2) - pow(Z.w, 2);
    //        float zy = 2 * Z.x * Z.y;
    //        float zz = 2 * Z.x * Z.z;
    //        float zw = 2 * Z.x * Z.w;
    //
    //        Z = vec4(zx, zy, zz, zw) + C;
    //
    //        if (length(Z) >= 2) {
    //            return 0;
    //        }
    //
    //        iter++;
    //    }

    // Bulb that goes through time

    //    vec3 pos = gridCoords / textureScale;
    //    if (pos.x < -2 || pos.x > 2 || pos.y < -2 || pos.y > 2 || pos.z < -2 || pos.z > 2) {
    //        return 0;
    //    }
    //
    //    int iter = 0;
    //    int maxIter = 8;
    //    vec4 Z = vec4(pos, wFactor);
    //    vec4 C = vec4(0.7, Z.y, 0, 0);
    //    int n = 8;
    //    while (iter < maxIter) {
    //        float d = length(Z);
    //        float phi = atan(Z.y, Z.x);
    //        float theta = atan(Z.z, length(Z.xy));
    //        float rho = atan(Z.w, length(Z.xyz));
    //
    //        d = pow(d, n);
    //        theta *= n;
    //        phi *= n;
    //        rho *= n;
    //
    //        float o = d * cos(rho);
    //        float r = o * cos(theta);
    //
    //        Z.x = cos(phi) * r;
    //        Z.y = sin(phi) * r;
    //        Z.z = o * sin(theta);
    //        Z.w = d * sin(rho);
    //        Z += C;
    //
    //        if (d >= 2) {
    //            return 0;
    //        }
    //
    //        iter++;
    //    }

    // Sponge

    vec3 grid = gridCoords + textureScale / 2;
    vec3 pos = grid / textureScale;
    if (pos.x < 0 || pos.x >= 1 || pos.y < 0 || pos.y >= 1 || pos.z < 0 || pos.z >= 1) {
        return 0;
    }

    int maxIter = 4;
    int iter = 0;
    ivec3 voxel = ivec3(floor(pos * 3 - 1));
    while (iter < maxIter) {
        ivec3 absVoxel = abs(voxel);
        if (absVoxel.x + absVoxel.y + absVoxel.z <= 1) {
            return 0;
        }

        float oldPower = pow(3, iter + 1);
        ivec3 oldLocation = ivec3(floor(pos * oldPower) - int((oldPower - 1) / 2));
        iter++;

        float power = pow(3, iter + 1);
        voxel = ivec3(floor(pos * power) - int((power - 1) / 2)) - ivec3(oldLocation * 3);
    }

    return 2;
}

void DDA(in out Ray ray, in out HitRecord record) {
    bvec3 dirSign = greaterThanEqual(ray.dir, vec3(0));

    ivec3 stepDir = mix(ivec3(-1), ivec3(1), dirSign);
    ivec3 voxExit = mix(ivec3(0), ivec3(1), dirSign);

    vec3 rayInverse = 1 / ray.dir;

    ivec3 gridCoords = ivec3(floor(ray.pos));

    vec3 tV = rayInverse * (vec3(gridCoords + voxExit) - ray.pos);
    vec3 tS = rayInverse * vec3(stepDir);

    int idx = 0;

    bool colorize = true;
    int hitId = 0;
    while (record.distance < maxDist) {
        if (inBounds(gridCoords.y)) {
            hitId = DDAIdGetter(gridCoords);
            if (hitId != 0) {
                if (hitId == 1) {
                    record.light = true;
                }

                break;
            }
        } else if (ray.lightRay) {
            hitId = 0;
            break;
        }

        DDAStep(stepDir, tS, gridCoords, tV, record.distance, idx);
    }

    if (idx == 0) {
        record.normal.x = -stepDir.x;
    } else if (idx == 1) {
        record.normal.y = -stepDir.y;
    } else {
        record.normal.z = -stepDir.z;
    }

    record.position = at(ray, record.distance);

    if (!colorize) {
        return;
    }

    vec3 skyColor = getSkyColor(ray.dir);
    if (hitId == 0) {
        ray.color = skyColor;
        record.light = true;
    } else {
        vec3 cubeColor = vec3(0);
        if (hitId == 2) {
            cubeColor = vec3(0.65, 0.4, 0.3);
        } else {
            cubeColor = vec3(1);
        }

        float x = record.distance / maxDist;
        float visibility = exp(-pow(x * 1.2, 9.0));

        ray.color = mix(skyColor, cubeColor, visibility);
    }
}

HitRecord primaryDDA(in out Ray ray) {
    vec2 pixelPosition = gl_FragCoord.xy / resolution;

    vec3 position = vec3(0);
    vec3 normal = vec3(0);
    float distance = texture(oldDepthAttachment, pixelPosition).r * maxDist;
    bool light = false;
    int id = 0;

    HitRecord record = HitRecord(position, normal, distance, light, id);
    DDA(ray, record);

    return record;
}

bool LightDDA(in out Ray ray, in out HitRecord record) {
    DDA(ray, record);

    return record.light;
}

void ColorDDA(in out Ray ray) {
    HitRecord record = primaryDDA(ray);
    //    ray.color = vec3(record.distance / maxDist);
    //    return;

    outDepth = vec3(record.distance / maxDist);
    if (record.light) {
        return;
    }

    //    float product = dot(record.normal, vec3(0, 1, 0));
    //    ray.color = vec3(product);
    //    return;

    //    ray.color = record.position;

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

    vec3 offHitPoint = record.position + record.normal / 100;
    vec3 fakeColor = vec3(0);

    //    int rayAmount = 1;
    //    for (int i = 0; i < rayAmount; i++) {
    //        vec3 fakeDir = getNewDirection();
    //        float product = dot(fakeDir, record.normal);
    //        if (product < 0) {
    //            fakeDir = normalize(-fakeDir);
    //            product *= -1;
    //        }
    //
    //        Ray fakeRay = Ray(offHitPoint, fakeDir, vec3(0));
    //        if (LightDDA(fakeRay)) {
    //            fakeColor += ray.color * getSkyColor(fakeDir) / rayAmount;
    //        }
    //    }

    vec3 randDir = getNewDirection();
    float product = dot(randDir, record.normal);
    if (product < 0) {
        randDir = normalize(-randDir);
        product *= -1;
    }

    HitRecord lightRecord = HitRecord(vec3(0), vec3(0), 0, false, 0);
    Ray lightRay = Ray(offHitPoint, randDir, vec3(0), true);
    if (LightDDA(lightRay, lightRecord)) {
        ray.color *= getSkyColor(randDir);
        return;
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
}