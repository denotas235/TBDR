#version 320 es

// Habilita a extensão Pixel Local Storage nativa para chips gráficos mobile
#extension GL_EXT_shader_pixel_local_storage : require

// Média precisão para cores e texturas economiza registradores na arquitetura Valhall
precision mediump float;

in vec2 vTexCoord;
out vec4 fragColor;

// Definição do bloco PLS oficial: Aloca armazenamento de pixel de 8 bits por canal (rgba8)
// O hardware amarra automaticamente esta variável ao pixel atual na Thread de execução!
layout(rgba8) __pixel_local_inoutEXT PLSStorage {
    vec4 localColor;
} pls;

uniform sampler2D uTexture;

void main() {
    // 1. Amostra a textura do bloco do Minecraft
    vec4 texColor = texture(uTexture, vTexCoord);
    
    // 2. Armazena o resultado diretamente no cache local de blocos da GPU (Sem tocar na RAM)
    pls.localColor = texColor;
    
    // 3. Aplica o multiplicador de brilho e envia a cor final para a saída de renderização
    fragColor = pls.localColor * 1.1;
}
