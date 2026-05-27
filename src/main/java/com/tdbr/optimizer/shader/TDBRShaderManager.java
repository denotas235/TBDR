package com.tdbr.optimizer.shader;

import com.tdbr.optimizer.TDBROptimizerClientMod;
import com.tdbr.optimizer.detector.TDBRDetector;
import org.lwjgl.opengl.GL20;

import java.util.HashMap;
import java.util.Map;

public class TDBRShaderManager {
    private static final Map<String, ShaderProgram> shaders = new HashMap<>();
    private static boolean initialized = false;

    public static void register() {
        if (initialized) return;
        initialized = true;

        TDBROptimizerClientMod.LOGGER.info("Registrando shaders...");

        // Seleciona o shader com base nas capacidades
        String fragmentShader;
        if (TDBRDetector.isPLSSupported()) {
            fragmentShader = "pls_fragment.glsl";
            TDBROptimizerClientMod.LOGGER.info("Usando shader PLS (Mali-G57+)");
        } else if (TDBRDetector.isFBFetchSupported()) {
            fragmentShader = "fbfetch_fragment.glsl";
            TDBROptimizerClientMod.LOGGER.info("Usando shader FBFetch (Mali-G52)");
        } else {
            fragmentShader = "fallback_fragment.glsl";
            TDBROptimizerClientMod.LOGGER.warn("Usando shader fallback (nenhuma extensão avançada disponível)");
        }

        // Cria o programa de shader
        ShaderProgram program = new ShaderProgram();
        boolean vertexOk = program.attachShader(GL20.GL_VERTEX_SHADER, "vertex.glsl");
        boolean fragmentOk = program.attachShader(GL20.GL_FRAGMENT_SHADER, fragmentShader);

        if (vertexOk && fragmentOk) {
            if (program.link()) {
                shaders.put("default", program);
                TDBROptimizerClientMod.LOGGER.info("Shader registrado com sucesso: {}", fragmentShader);
            } else {
                TDBROptimizerClientMod.LOGGER.error("Falha ao linkar shader");
            }
        } else {
            TDBROptimizerClientMod.LOGGER.error("Falha ao carregar shaders");
        }
    }

    public static ShaderProgram getShader(String name) {
        return shaders.get(name);
    }

    public static void cleanup() {
        shaders.values().forEach(ShaderProgram::cleanup);
        shaders.clear();
        initialized = false;
    }
}