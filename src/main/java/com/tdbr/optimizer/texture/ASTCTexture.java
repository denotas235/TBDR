package com.tdbr.optimizer.texture;

import com.tdbr.optimizer.TDBROptimizerClientMod;
import com.tdbr.optimizer.renderer.TDBRDetector;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ASTCTexture extends AbstractTexture {

    public static final int GL_COMPRESSED_RGBA_ASTC_4x4_KHR = 0x93B0;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR = 0x93D0;

    private final Identifier sourceId;
    private int blockX = 4, blockY = 4;
    private int width, height;

    public ASTCTexture(Identifier id) { this.sourceId = id; }

    @Override
    public void load(ResourceManager manager) throws IOException {
        Identifier astcId = Identifier.of(sourceId.getNamespace(), 
            sourceId.getPath().replace(".png", ".astc"));
        if (!manager.getResource(astcId).isPresent()) {
            throw new IOException("Arquivo ASTC nao encontrado: " + astcId);
        }
        try (InputStream stream = manager.getResource(astcId).get().getInputStream()) {
            loadASTC(stream);
        }
    }

    private void loadASTC(InputStream stream) throws IOException {
        byte[] header = new byte[16];
        if (stream.read(header) != 16) throw new IOException("Header ASTC invalido");
        if ((header[0] & 0xFF) != 0x13 || (header[1] & 0xFF) != 0xAB ||
            (header[2] & 0xFF) != 0xA1 || (header[3] & 0xFF) != 0x5C) {
            throw new IOException("Magic number ASTC invalido");
        }
        blockX = header[4] & 0xFF;
        blockY = header[5] & 0xFF;
        width = (header[7] & 0xFF) | ((header[8] & 0xFF) << 8) | ((header[9] & 0xFF) << 16);
        height = (header[10] & 0xFF) | ((header[11] & 0xFF) << 8) | ((header[12] & 0xFF) << 16);
        int blockSize = ((width + blockX - 1) / blockX) * ((height + blockY - 1) / blockY) * 16;
        byte[] data = new byte[blockSize];
        int read = stream.read(data);
        if (read != blockSize) throw new IOException("Dados ASTC incompletos");
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.bindTexture();
        int format = GL_COMPRESSED_RGBA_ASTC_4x4_KHR;
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, format, width, height, 0, 
            GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        TDBROptimizerClientMod.LOGGER.info("ASTC carregado: {} ({}x{}, blocks {}x{})", 
            sourceId, width, height, blockX, blockY);
    }
}
