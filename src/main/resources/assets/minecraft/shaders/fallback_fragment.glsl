#version 320 es

// Média precisão poupa largura de banda e mantém os 60 FPS estáveis em qualquer GPU
precision mediump float;

in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D uTexture;

void main() {
    // Amostragem direta e limpa da textura vanilla
    fragColor = texture(uTexture, vTexCoord);
}
