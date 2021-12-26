#version 450 core

layout(local_size_x = 1, local_size_y = 1, local_size_z = 1) in;
layout(rgba32f, binding = 0) uniform image3D img_input;
layout(rgba32f, binding = 1) uniform image3D img_output;

void main() {
    const uint x = gl_GlobalInvocationID.x;
    const uint y = gl_GlobalInvocationID.y;
    const uint z = gl_GlobalInvocationID.z;
    const ivec3 pixel = ivec3(x, y, z);

    bool alive = imageLoad(img_input, pixel).r > 0;

    int aliveNeighbors = 0;
    for (int i = -1; i < 2; i++) {
        for (int j = -1; j < 2; j++) {
            for (int k = -1; k < 2; k++) {
                if (i == 0 && j == 0 && k == 0) {
                    continue;
                }

                aliveNeighbors += int(imageLoad(img_input, ivec3(x + i, y + j, z + k)).r);
            }
        }
    }

    int nextState = alive ? 1 : 0;
    if (alive && (aliveNeighbors == 2 || aliveNeighbors == 3)) {
        nextState = 1;
    } else if (!alive && aliveNeighbors == 3) {
        nextState = 1;
    } else {
        nextState = 0;
    }

    imageStore(img_output, pixel, vec4(nextState));
}

