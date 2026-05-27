package com.tdbr.optimizer.texture;
import org.lwjgl.system.MemoryUtil;

import com.tdbr.optimizer.TDBROptimizerClientMod;
import com.tdbr.optimizer.maliopt.MaliOptJNA;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import java.io.InputStream;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;

public class ASTCTexture extends AbstractTexture {
    private final Identifier originalId;

    public ASTCTexture(Identifier originalId) {
        this.originalId = originalId;
    }

    @Override
    public void load(ResourceManager manager) {
        // Converte o caminho original do PNG para o nosso arquivo correspondente no cache ASTC
        Identifier astcId = Identifier.of(originalId.getNamespace(),
                originalId.getPath().replace(".png", ".astc"));

        // Extrai o nome puro para consultar o tamanho do bloco no metadados da EngenhariaSonora
        String path = originalId.getPath();
        String filename = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
        String blockSize = TDBRASTCTextureLoader.getBlockSize(filename);

        // Define o ID Hexadecimal do formato de compressão com base na extensão GL_KHR_texture_compression_astc_ldr
        int glFormat = 0x93D0; // Padrão sRGB 4x4
        if ("5x5".equals(blockSize)) glFormat = 0x93D2;
        else if ("6x6".equals(blockSize)) glFormat = 0x93D4;
        else if ("8x8".equals(blockSize)) glFormat = 0x93D7;

        try {
            var resource = MinecraftClient.getInstance().getResourceManager().getResource(astcId);
            if (resource.isPresent()) {
                try (InputStream is = resource.get().getInputStream()) {
                    // Descobre o tamanho total do arquivo e subtrai os 16 bytes do cabeçalho
                    byte[] allBytes = is.readAllBytes();
                    int dataLength = allBytes.length - 16;

                    if (dataLength <= 0) {
                        TDBROptimizerClientMod.LOGGER.error("Arquivo ASTC corrompido ou vazio: " + astcId);
                        return;
                    }

                    // Lê a largura e altura nativas codificadas diretamente nos bytes do cabeçalho ASTC
                    int width = ((allBytes[7] & 0xFF) << 16) | ((allBytes[6] & 0xFF) << 8) | (allBytes[5] & 0xFF);
                    int height = ((allBytes[10] & 0xFF) << 16) | ((allBytes[9] & 0xFF) << 8) | (allBytes[8] & 0xFF);

                    // Transfere apenas os dados puros (pulando os 16 bytes iniciais) para um ByteBuffer direto da LWJGL
                    ByteBuffer buffer = BufferUtils.createByteBuffer(dataLength);
                    buffer.put(allBytes, 16, dataLength);
                    buffer.flip();

                    // Sincroniza a chamada com a Thread de Renderização do OpenGL ES
                    final int finalGlFormat = glFormat;
                    final int finalWidth = width;
                    final int finalHeight = height;
                    final int finalDataLength = dataLength;

                    RenderSystem.recordRenderCall(() -> {
                        bindTexture();
                        
                        // Configura os filtros ideais para a TBDR da Mali trabalhar em blocos de textura
                        GlStateManager._texParameter(3553, 10241, 9728); // GL_TEXTURE_MIN_FILTER -> GL_NEAREST
                        GlStateManager._texParameter(3553, 10240, 9728); // GL_TEXTURE_MAG_FILTER -> GL_NEAREST
                        
                        // Faz o upload direto dos bytes comprimidos para a VRAM da GPU Mali via OpenGL ES
                        org.lwjgl.opengles.GLES20.glCompressedTexImage2D(
                            3553,               // GL_TEXTURE_2D
                            0,                  // Mipmap level 0
                            finalGlFormat,      // Formato Hexadecimal da compressão ASTC
                            finalWidth,         // Largura real extraída
                            finalHeight,        // Altura real extraída
                            0,                  // Border
                            finalDataLength,    // Tamanho dos dados puros
    MemoryUtil.memAddress(buffer)              // O buffer de bytes alinhado
                        );
                    });
                }
            }
        } catch (Exception e) {
            TDBROptimizerClientMod.LOGGER.error("Erro fatal ao efetuar o upload nativo da textura ASTC: " + originalId, e);
        }
    }
}
