package com.tdbr.optimizer.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.tdbr.optimizer.detector.TDBRDetector;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public abstract class GlStateManagerMixin {
    
    // Injeta no final do método de inicialização do estado de renderização vanilla do Minecraft
    @Inject(method = "initBackendSystem", at = @At("TAIL"), remap = false)
    private static void onInitRenderer(CallbackInfo ci) {
        // Inicializa o detector dinâmico agora que a Render Thread e o contexto gráfico estão ativos!
        TDBRDetector.init();

        // Se estiver rodando na GPU alvo Mali-G52 (Tecno KH7)
        if (TDBRDetector.isMaliG52()) {
            // Força a ativação do teste de profundidade em hardware estável
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            
            // Ativa o descarte agressivo de faces escondidas para maximizar o Tile Cache móvel
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(GL11.GL_BACK);
        }
    }
}
