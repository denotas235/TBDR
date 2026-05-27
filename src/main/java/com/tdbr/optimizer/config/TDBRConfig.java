package com.tdbr.optimizer.config;

public class TDBRConfig {
    public boolean enableParticleCulling = true;
    public boolean enableEarlyZ = true;
    public boolean enableOverdrawReduction = true;
    public boolean logExtensions = false;
    
    public static TDBRConfig CONFIG = new TDBRConfig();
    
    public static void load() {
        // Stub - config carregada com defaults
    }
}
