#version 320 es
precision highp float;

extension GL_EXT_shader_framebuffer_fetch : require;
layout(framebuffer_fetch) uniform;

in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D uTexture;

void main() {
    // Sample da textura
    vec4 texColor = texture(uTexture, vTexCoord);

    // Usa FBFetch para acessar o framebuffer anterior
    vec4 previousColor = framebufferFetchTexel(vTexCoord, 0);

    // Combina as cores (exemplo: bloom simples)
    fragColor = mix(texColor, previousColor * 1.2, 0.5);
}