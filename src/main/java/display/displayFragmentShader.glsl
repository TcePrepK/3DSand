#version 450 core

in vec2 textureCoords;

uniform sampler2D renderedTexture;

void main(void) {
    gl_FragColor = texture(renderedTexture, textureCoords);
}