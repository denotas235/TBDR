package com.tdbr.optimizer.shader;
import net.minecraft.client.gl.ShaderProgram;
public class TDBRShaderManager {
    public static ShaderProgram TDBR_TRANSLUCENT_SHADER = null;
    public static ShaderProgram getShader(String name) { return TDBR_TRANSLUCENT_SHADER; }
}
