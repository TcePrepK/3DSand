bool Mandelbulb_Classic(ivec3 gridCoords) {
    vec3 pos = gridCoords / textureScale;
    if (pos.x < -2 || pos.x > 2 || pos.y < -2 || pos.y > 2 || pos.z < -2 || pos.z > 2) {
        return false;
    }

    int iter = 0;
    int maxIter = 8;
    vec3 Z = vec3(pos);
    vec3 C = vec3(Z);
    int n = 8;
    while (iter < maxIter) {
        const float cr = length(Z);
        float a1 = atan(Z.y, Z.x);
        float a2 = atan(Z.z, length(Z.xy));

        const float r = pow(cr, n);
        a1 = a1 * n;
        a2 = a2 * n;

        Z.x = r * cos(a2) * cos(a1);
        Z.y = r * cos(a2) * sin(a1);
        Z.z = r * sin(a2);
        Z += C;

        if (cr >= 2) {
            return false;
        }

        iter++;
    }

    return true;
}

bool Mandelbulb_Strange(ivec3 gridCoords) {
    vec3 pos = gridCoords / textureScale;
    if (pos.x < -2 || pos.x > 2 || pos.y < -2 || pos.y > 2 || pos.z < -2 || pos.z > 2) {
        return false;
    }

    int iter = 0;
    int maxIter = 8;
    vec4 Z = vec4(pos, 0);
    vec4 C = vec4(Z);
    while (iter < maxIter) {
        float zx = pow(Z.x, 2) - pow(Z.y, 2) - pow(Z.z, 2) - pow(Z.w, 2);
        float zy = 2 * Z.x * Z.y;
        float zz = 2 * Z.x * Z.z;
        float zw = 2 * Z.x * Z.w;

        Z = vec4(zx, zy, zz, zw) + C;

        if (length(Z) >= 2) {
            return false;
        }

        iter++;
    }

    return true;
}

bool Mandelbulb_4D(ivec3 gridCoords) {
    vec3 pos = gridCoords / textureScale;
    if (pos.x < -2 || pos.x > 2 || pos.y < -2 || pos.y > 2 || pos.z < -2 || pos.z > 2) {
        return false;
    }

    int iter = 0;
    int maxIter = 8;
    vec4 Z = vec4(pos, wFactor);
    vec4 C = vec4(Z);
    int n = 8;
    while (iter < maxIter) {
        float d = length(Z);
        float phi = atan(Z.y, Z.x);
        float theta = atan(Z.z, length(Z.xy));
        float rho = atan(Z.w, length(Z.xyz));

        d = pow(d, n);
        theta *= n;
        phi *= n;
        rho *= n;

        float o = d * cos(rho);
        float r = o * cos(theta);

        Z.x = cos(phi) * r;
        Z.y = sin(phi) * r;
        Z.z = o * sin(theta);
        Z.w = d * sin(rho);
        Z += C;

        if (d >= 2) {
            return false;
        }

        iter++;
    }

    return true;
}

bool Menger_Sponge(ivec3 gridCoords) {
    vec3 pos = gridCoords / textureScale + vec3(0.5);
    if (pos.x < 0 || pos.x >= 1 || pos.y < 0 || pos.y >= 1 || pos.z < 0 || pos.z >= 1) {
        return false;
    }

    int maxIter = 4;
    int iter = 0;
    ivec3 voxel = ivec3(floor(pos * 3 - 1));
    while (iter <= maxIter) {
        ivec3 absVoxel = abs(voxel);
        if (absVoxel.x + absVoxel.y + absVoxel.z <= 1) {
            return false;
        }

        iter++;

        float power = pow(3, iter);
        vec3 location = floor(pos * power);
        voxel = ivec3(mod(location, 3) - 1);
    }

    return true;
}

bool close(float v, float m) {
    return (v - 0.01 < m && v + 0.01 > m);
}

bool Triangle(ivec3 gridCoords) {
    vec3 pos = gridCoords / textureScale + vec3(0.5);
    if (pos.x <= 0 || pos.x >= 1 || pos.y <= 0 || pos.y >= 1 || pos.z <= 0 || pos.z >= 1) {
        return false;
    }

    int maxIter = 2;
    int iter = 0;
    while (iter <= maxIter) {
        bool closeX = close(pos.x, 0) || close(pos.x, 1);
        bool closeY = close(pos.y, 0) || close(pos.y, 1);
        bool closeZ = close(pos.z, 0) || close(pos.z, 1);
        if ((closeX && (closeY || closeZ)) || (closeZ && (closeX || closeY))) {
            return true;
        }

        pos /= 2;

        iter++;
    }

    return false;
}