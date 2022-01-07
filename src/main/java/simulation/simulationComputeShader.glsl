#version 450 core

layout(local_size_x = 1, local_size_y = 1, local_size_z = 1) in;
layout(rgba32f, binding = 0) uniform image3D inputWorld;
layout(rgba32f, binding = 1) uniform image3D outputWorld;
layout(r32ui, binding = 2) uniform uimage3D worldLockBuffer;

uniform vec3 textureScale;
uniform float maxIter;

struct Rule {
    float inputs[27];
    float outputs[27];
};

struct RuleOffset {
    uint start;
    uint count;
};

layout(std430, binding = 0) readonly buffer RuleData {
    uint ruleCount;
    Rule ruleArray[];
};

layout(std430, binding = 1) readonly buffer OffsetData {
    RuleOffset voxelRuleOffsets[];
};

bool fractalTest(ivec3 gridCoords) {
    vec3 pos = gridCoords / textureScale;
    if (pos.x <= 0 || pos.x >= 1 || pos.y <= 0 || pos.y >= 1 || pos.z <= 0 || pos.z >= 1) {
        return false;
    }

    int iter = 0;
    ivec3 voxel = ivec3(floor(pos * 3 - 1));
    while (iter <= maxIter) {
        ivec3 absVoxel = abs(voxel);
        if (absVoxel.x + absVoxel.y + absVoxel.z <= 1) {
            return false;
        }

        iter ++;

        float power = pow(3, iter);
        vec3 location = floor(pos * power);
        voxel = ivec3(mod(location, 3) - 1);
    }

    return true;
}

bool isLocked(ivec3 pixel) {
    return imageAtomicCompSwap(worldLockBuffer, pixel, 0, 1) == 1 ? true : false;
}

void setLocked(ivec3 pixel) {
    imageStore(worldLockBuffer, pixel, uvec4(1));
}

int getVoxelID(ivec3 pixel) {
    return int(imageLoad(inputWorld, pixel).r);
}

void setVoxelID(ivec3 pixel, int id) {
    imageStore(outputWorld, pixel, vec4(id));
    setLocked(pixel);
}

void skipVoxel() {
    const ivec3 pixel = ivec3(gl_GlobalInvocationID);
    setVoxelID(pixel, getVoxelID(pixel));
}

void main() {
    const ivec3 pixel = ivec3(gl_GlobalInvocationID);
    if (fractalTest(pixel)) {
        setVoxelID(pixel, 2);
    }
    //    if (isLocked(pixel)) {
    //        return;
    //    }
    //
    //    const int voxelID = getVoxelID(pixel);
    //    if (voxelID == 0) {
    //        return;
    //    }
    //
    //    const ivec3 downPixel = pixel - ivec3(0, 1, 0);
    //    if (isLocked(downPixel)) {
    //        skipVoxel();
    //        return;
    //    }
    //
    //    const int downVoxelID = getVoxelID(downPixel);
    //    if (downVoxelID != 0) {
    //        skipVoxel();
    //        return;
    //    }
    //
    //    setVoxelID(pixel, downVoxelID);
    //    setVoxelID(downPixel, voxelID);

    //    RuleOffset ruleOffset = OffsetData.voxelRuleOffsets[voxelID];
    //    for (int i = 0; i < ruleOffset.count; i++) {
    //        Rule rule = RuleData.ruleArray[ruleOffset.start + i];
    //
    //        // apply rule
    //    }
}

//void main() {
//    const uint x = gl_GlobalInvocationID.x;
//    const uint y = gl_GlobalInvocationID.y;
//    const uint z = gl_GlobalInvocationID.z;
//    const ivec3 pixel = ivec3(x, y, z);
//
//    bool alive = imageLoad(img_input, pixel).r > 0;
//
//    int aliveNeighbors = 0;
//    for (int i = -1; i < 2; i++) {
//        for (int j = -1; j < 2; j++) {
//            for (int k = -1; k < 2; k++) {
//                if (i == 0 && j == 0 && k == 0) {
//                    continue;
//                }
//
//                aliveNeighbors += int(imageLoad(img_input, ivec3(x + i, y + j, z + k)).r);
//            }
//        }
//    }
//
//    const int rule0 = 5;
//    const int rule1 = 5;
//    const int rule2 = 5;
//    const int rule3 = 6;
//
//    int nextState = 0;
//    if (alive && (aliveNeighbors == rule0 || aliveNeighbors == rule1)) {
//        nextState = 1;
//    } else if (!alive && (aliveNeighbors == rule2 || aliveNeighbors == rule3)) {
//        nextState = 1;
//    }
//
//    imageStore(img_output, pixel, vec4(nextState));
//}

