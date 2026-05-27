package com.tdbr.optimizer.detector;

import com.tdbr.optimizer.TDBROptimizerClientMod;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TDBRDetector {
    private static final Logger LOGGER = TDBROptimizerClientMod.LOGGER;
    private static boolean initialized = false;
    private static String gpuName;
    private static Set<String> glExtensions = new HashSet<>();
    private static Set<String> eglExtensions = new HashSet<>();

    // Carrega a biblioteca nativa
    static {
        try {
            System.loadLibrary("maliopt"); // Carrega libmaliopt.so
            LOGGER.info("Biblioteca nativa libmaliopt.so carregada com sucesso");
        } catch (UnsatisfiedLinkError e) {
            LOGGER.error("Falha ao carregar libmaliopt.so: {}. Usando detecção de fallback.", e.getMessage());
            // Fallback: Assume Mali-G52 se não conseguir carregar a biblioteca
            gpuName = "Mali-G52 MC2";
            glExtensions.addAll(Arrays.asList(
                "GL_EXT_shader_framebuffer_fetch",
                "GL_KHR_texture_compression_astc_ldr",
                "GL_ARM_shader_framebuffer_fetch",
                "GL_ARM_shader_framebuffer_fetch_raw_explicit",
                "GL_EXT_texture_compression_astc_decode_mode",
                "GL_OES_texture_storage",
                "GL_EXT_discard_framebuffer",
                "GL_EXT_buffer_storage"
            ));
            initialized = true;
        }
    }

    // Métodos nativos (JNI)
    private native static String getGLExtensions();
    private native static String getEGLExtensions();
    private native static String getGPUName();

    public static void init() {
        if (initialized) return;

        try {
            gpuName = getGPUName();
            String glExt = getGLExtensions();
            String eglExt = getEGLExtensions();

            if (glExt != null && !glExt.isEmpty()) {
                glExtensions.addAll(Arrays.asList(glExt.split(" ")));
            }
            if (eglExt != null && !eglExt.isEmpty()) {
                eglExtensions.addAll(Arrays.asList(eglExt.split(" ")));
            }

            LOGGER.info("=== Detecção de Hardware (TBDR) ===");
            LOGGER.info("GPU: {}", gpuName);
            LOGGER.info("Extensões OpenGL ES detectadas: {}", glExtensions.size());
            LOGGER.info("Extensões EGL detectadas: {}", eglExtensions.size());

            // Loga todas as extensões (para debug)
            LOGGER.info("--- Extensões OpenGL ES ---");
            glExtensions.forEach(ext -> LOGGER.info("  - {}", ext));
            LOGGER.info("--- Extensões EGL ---");
            eglExtensions.forEach(ext -> LOGGER.info("  - {}", ext));

            initialized = true;
        } catch (Exception e) {
            LOGGER.error("Falha na detecção de hardware: {}", e.getMessage());
        }
    }

    public static String getGPUName() {
        return gpuName != null ? gpuName : "Unknown";
    }

    public static boolean isPLSSupported() {
        return glExtensions.contains("GL_EXT_shader_pixel_local_storage") ||
               glExtensions.contains("GL_ARM_shader_framebuffer_fetch");
    }

    public static boolean isASTCSupported() {
        return glExtensions.contains("GL_KHR_texture_compression_astc_ldr") ||
               glExtensions.contains("GL_OES_texture_compression_astc");
    }

    public static boolean isFBFetchSupported() {
        return glExtensions.contains("GL_EXT_shader_framebuffer_fetch") ||
               glExtensions.contains("GL_ARM_shader_framebuffer_fetch") ||
               glExtensions.contains("GL_ARM_shader_framebuffer_fetch_raw_explicit");
    }

    public static boolean isTBDRSupported() {
        return gpuName != null && gpuName.toLowerCase().contains("mali");
    }

    public static boolean isMaliG52() {
        return gpuName != null && gpuName.toLowerCase().contains("mali-g52");
    }

    public static boolean isMaliG57() {
        return gpuName != null && gpuName.toLowerCase().contains("mali-g57");
    }

    public static Set<String> getGLExtensions() {
        return new HashSet<>(glExtensions);
    }

    public static Set<String> getEGLExtensions() {
        return new HashSet<>(eglExtensions);
    }
}