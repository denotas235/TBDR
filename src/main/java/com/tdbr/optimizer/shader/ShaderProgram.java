package com.tdbr.optimizer.shader;

import com.tdbr.optimizer.TDBROptimizerClientMod;
import com.tdbr.optimizer.detector.TDBRDetector;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ShaderProgram {
    private final int programId;
    private boolean linked = false;
    private boolean compiled = false;

    public ShaderProgram() {
        this.programId = GL20.glCreateProgram();
        if (programId == 0) {
            TDBROptimizerClientMod.LOGGER.error("Falha ao criar programa de shader");
        }
    }

    public boolean attachShader(int type, String filename) {
        int shaderId = GL20.glCreateShader(type);
        if (shaderId == 0) {
            TDBROptimizerClientMod.LOGGER.error("Falha ao criar shader: {}", filename);
            return false;
        }

        String shaderCode = readShader(filename);
        if (shaderCode.isEmpty()) {
            TDBROptimizerClientMod.LOGGER.error("Shader vazio ou não encontrado: {}", filename);
            GL20.glDeleteShader(shaderId);
            return false;
        }

        // Adiciona extensões necessárias com base nas capacidades
        if (type == GL20.GL_FRAGMENT_SHADER) {
            if (TDBRDetector.isPLSSupported()) {
                shaderCode = "#extension GL_EXT_shader_pixel_local_storage : require\n" + shaderCode;
            } else if (TDBRDetector.isFBFetchSupported()) {
                shaderCode = "#extension GL_EXT_shader_framebuffer_fetch : require\n" + shaderCode;
            }
        } else if (type == GL20.GL_VERTEX_SHADER) {
            shaderCode = "#version 320 es\n" + shaderCode;
        }

        GL20.glShaderSource(shaderId, shaderCode);
        GL20.glCompileShader(shaderId);

        // Verifica erros de compilação
        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            String error = GL20.glGetShaderInfoLog(shaderId);
            TDBROptimizerClientMod.LOGGER.error("Erro ao compilar shader {}: {}", filename, error);
            GL20.glDeleteShader(shaderId);
            return false;
        }

        GL20.glAttachShader(programId, shaderId);
        compiled = true;
        return true;
    }

    public boolean link() {
        if (!compiled) {
            TDBROptimizerClientMod.LOGGER.error("Não é possível linkar: shaders não compilados");
            return false;
        }

        GL20.glLinkProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            String error = GL20.glGetProgramInfoLog(programId);
            TDBROptimizerClientMod.LOGGER.error("Erro ao linkar shader program: {}", error);
            return false;
        }
        linked = true;
        return true;
    }

    public void use() {
        if (linked) {
            GL20.glUseProgram(programId);
        } else {
            TDBROptimizerClientMod.LOGGER.warn("Tentativa de usar shader não linkado");
        }
    }

    public void cleanup() {
        if (programId != 0) {
            GL20.glDeleteProgram(programId);
        }
    }

    private String readShader(String filename) {
        try (InputStream stream = getClass().getResourceAsStream("/assets/minecraft/shaders/" + filename)) {
            if (stream == null) {
                TDBROptimizerClientMod.LOGGER.error("Shader não encontrado: {}", filename);
                return "";
            }
            return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            TDBROptimizerClientMod.LOGGER.error("Falha ao ler shader {}: {}", filename, e.getMessage());
            return "";
        }
    }
}