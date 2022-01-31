#version 450 core

in vec2 textureCoords;

uniform sampler2D renderedTexture;

out vec4 outColor;

void main(void) {
    outColor = texture(renderedTexture, textureCoords);
}