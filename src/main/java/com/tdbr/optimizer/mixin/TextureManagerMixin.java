package com.tdbr.optimizer.mixin;

import com.tdbr.optimizer.texture.TDBRASTCTextureLoader;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public class TextureManagerMixin {

    @Inject(method = "registerTexture", at = @At("HEAD"), cancellable = true)
    private void onRegisterTexture(Identifier id, AbstractTexture texture, CallbackInfo ci) {
        if (TDBRASTCTextureLoader.tryLoadASTC((TextureManager) (Object) this, id)) {
            ci.cancel();
        }
    }
}
