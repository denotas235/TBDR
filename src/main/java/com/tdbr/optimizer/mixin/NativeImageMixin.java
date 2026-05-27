package com.tdbr.optimizer.mixin;

import com.tdbr.optimizer.renderer.TDBRDetector;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

@Mixin(NativeImage.class)
public class NativeImageMixin {

    @Inject(method = "read(Ljava/io/InputStream;)Lnet/minecraft/client/texture/NativeImage;", at = @At("HEAD"), cancellable = true)
    private static void onRead(InputStream stream, CallbackInfoReturnable<NativeImage> cir) throws IOException {
        if (!TDBRDetector.HAS_ASTC_LDR && !TDBRDetector.HAS_ASTC_HDR) return;
        InputStream s = stream.markSupported() ? stream : new BufferedInputStream(stream);
        s.mark(16);
        byte[] magic = new byte[4];
        if (s.read(magic) == 4) {
            if ((magic[0] & 0xFF) == 0x13 && (magic[1] & 0xFF) == 0xAB &&
                (magic[2] & 0xFF) == 0xA1 && (magic[3] & 0xFF) == 0x5C) {
                cir.setReturnValue(null);
                return;
            }
        }
        s.reset();
    }
}
