#version 320 es

// Habilita a extensão de leitura de buffer local nativa para GPUs móveis
#extension GL_EXT_shader_framebuffer_fetch : require

// Média precisão para cores e texturas economiza bateria e largura de banda na Mali
precision mediump float;

in vec2 vTexCoord;

// Na extensão GL_EXT_shader_framebuffer_fetch, a saída age como 'inout' automático.
// Ler fragColor traz o pixel anterior do tile; escrever atualiza-o sem tocar na RAM!
out inout vec4 fragColor;

uniform sampler2D uTexture;

void main() {
    vec4 texColor = texture(uTexture, vTexCoord);
    
    // Captura o pixel que já estava renderizado atrás (Evita sobreposição cega / Overdraw)
    vec4 previousColor = fragColor;
    
    // Executa a mistura matemática otimizada (Multiplicação leve e interpolação linear)
    fragColor = mix(texColor, previousColor * 1.2, 0.5);
}
