package com.tdbr.optimizer.mixin;

import com.tdbr.optimizer.TDBROptimizerClientMod;
import com.tdbr.optimizer.maliopt.MaliOptJNA;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    private static boolean maliScanDone = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if (!maliScanDone) {
            MaliOptJNA.loadAndScan();
            maliScanDone = true;
        }
        if (!TDBROptimizerClientMod.PLS_AVAILABLE) return;
    }
}
