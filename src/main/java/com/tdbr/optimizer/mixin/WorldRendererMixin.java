package com.tdbr.optimizer.mixin;

import com.tdbr.optimizer.TDBROptimizerClientMod;
import com.tdbr.optimizer.renderer.TDBRDetector;
import com.tdbr.optimizer.renderer.TDBRRendererHooks;
import com.tdbr.optimizer.shadow.TDBRShadowMultiview;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(net.minecraft.client.render.RenderTickCounter tickCounter, boolean renderBlockOutline,
            Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager,
            Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        TDBRShadowMultiview.onBeginRenderPass(camera);
    }

    @Inject(method = "renderLayer", at = @At("HEAD"))
    private void onRenderLayerHead(RenderLayer renderLayer, double x, double y, double z, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (!TDBRDetector.IS_TDBR_ARCHITECTURE) return;
        String layerName = renderLayer.toString();
        if (layerName.contains("solid") || layerName.contains("cutout")) {
            if (TDBROptimizerClientMod.CONFIG.enableEarlyZ) {
                TDBRRendererHooks.applyEarlyZOptimizations();
            }
        }
        if (layerName.contains("translucent")) {
            TDBRRendererHooks.applyTransparentPassOptimizations();
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", shift = At.Shift.AFTER))
    private void onPostSetupTerrain(RenderTickCounter tickCounter, boolean renderBlockOutline,
            Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager,
            Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        if (TDBRDetector.IS_TDBR_ARCHITECTURE && TDBROptimizerClientMod.CONFIG.enableOverdrawReduction) {
            org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
            org.lwjgl.opengl.GL11.glDepthFunc(org.lwjgl.opengl.GL11.GL_LEQUAL);
        }
    }
}
