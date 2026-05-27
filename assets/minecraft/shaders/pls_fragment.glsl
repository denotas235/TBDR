#version 320 es
precision highp float;

extension GL_EXT_shader_pixel_local_storage : require;

layout(pixel_local_storage) buffer PLSBuffer {
    vec4 data[];
};

in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform ivec2 uTextureSize;

void main() {
    ivec2 pixelCoord = ivec2(gl_FragCoord.xy);
    int index = pixelCoord.y * uTextureSize.x + pixelCoord.x;

    // Armazena a cor no PLS
    data[index] = texture(uTexture, vTexCoord);

    // Aplica um efeito simples (exemplo: brilho)
    fragColor = data[index] * 1.1;
}