#version 450 core

uniform vec2 resolution;
uniform sampler2D renderedTexture;

out vec4 outColor;

void main(void) {
    vec2 pixelPosition = gl_FragCoord.xy / resolution;
    pixelPosition.y = 1 - pixelPosition.y;

    outColor = texture(renderedTexture, pixelPosition);
}