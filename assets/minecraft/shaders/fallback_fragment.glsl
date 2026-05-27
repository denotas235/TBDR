#version 320 es
precision highp float;

in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D uTexture;

void main() {
    // Fallback simples: apenas sample da textura
    fragColor = texture(uTexture, vTexCoord);
}