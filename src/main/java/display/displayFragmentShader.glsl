#version 450 core

in vec2 textureCoords;

uniform sampler2D renderedTexture;

out vec4 out_color;

void main(void) {
    out_color = texture(renderedTexture, textureCoords);
}