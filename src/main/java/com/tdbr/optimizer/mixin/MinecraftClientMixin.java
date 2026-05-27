package com.tdbr.optimizer.mixin;

import com.tdbr.optimizer.renderer.TDBRDetector;
import com.tdbr.optimizer.renderer.extensions.GLESSExtensionBridge;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    private static boolean tdbrInitialized = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(boolean tick, CallbackInfo ci) {
        if (!tdbrInitialized) {
            TDBRDetector.detect();
            GLESSExtensionBridge.tryLoadCriticalPointers();
            com.tdbr.optimizer.texture.TDBRASTCTextureLoader.init();
            tdbrInitialized = true;
        }
    }
}
