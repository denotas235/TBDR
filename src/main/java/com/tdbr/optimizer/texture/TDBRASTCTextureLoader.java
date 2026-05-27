package com.tdbr.optimizer.texture;

import com.tdbr.optimizer.TDBROptimizerClientMod;
import com.tdbr.optimizer.renderer.TDBRDetector;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TDBRASTCTextureLoader {
    private static final Map<String, String> astcCacheMap = new HashMap<>();

    public static void init() {
        if (!TDBRDetector.HAS_ASTC_LDR && !TDBRDetector.HAS_ASTC_HDR) {
            TDBROptimizerClientMod.LOGGER.info("ASTC nao suportado pelo hardware. Pulando loader.");
            return;
        }

        try {
            InputStream is = TDBRASTCTextureLoader.class.getResourceAsStream("/assets/minecraft/astc_cache/metadata.txt");
            if (is == null) {
                TDBROptimizerClientMod.LOGGER.warn("metadata.txt do ASTC nao encontrado no cache!");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || !line.contains(":")) continue;

                    String[] parts = line.split(":", 2);
                    astcCacheMap.put(parts[0], parts[1]);
                }
            }
            TDBROptimizerClientMod.LOGGER.info("ASTC Loader ativo. " + astcCacheMap.size() + " texturas mapeadas no cache.");
        } catch (Exception e) {
            TDBROptimizerClientMod.LOGGER.error("Erro ao carregar metadados do cache ASTC", e);
        }
    }

    public static String getBlockSize(String filename) {
        return astcCacheMap.get(filename);
    }

    public static boolean tryLoadASTC(TextureManager manager, Identifier id) {
        if (!TDBRDetector.HAS_ASTC_LDR && !TDBRDetector.HAS_ASTC_HDR) return false;

        String path = id.getPath();
        if (!path.endsWith(".png")) return false;
        
        String filename = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));

        if (astcCacheMap.containsKey(filename)) {
            ASTCTexture astcTex = new ASTCTexture(id); 
            manager.registerTexture(id, astcTex);
            return true; 
        }
        return false;
    }
}
