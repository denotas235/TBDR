package com.tdbr.optimizer.renderer;

public class TDBRDetector {
    public static boolean IS_TDBR_ARCHITECTURE = false;
    public static boolean HAS_ASTC_LDR = false;
    public static boolean HAS_ASTC_HDR = false;
    public static boolean HAS_PIXEL_LOCAL_STORAGE = false;
    public static boolean HAS_MULTIVIEW = false;
    public static boolean HAS_MULTIVIEW2 = false;
    public static boolean HAS_FRAMEBUFFER_FETCH = false;
    
    public static void detect() {
        IS_TDBR_ARCHITECTURE = false;
        HAS_ASTC_LDR = false;
        HAS_ASTC_HDR = false;
        HAS_PIXEL_LOCAL_STORAGE = false;
        HAS_MULTIVIEW = false;
        HAS_MULTIVIEW2 = false;
        HAS_FRAMEBUFFER_FETCH = false;
    }
}
