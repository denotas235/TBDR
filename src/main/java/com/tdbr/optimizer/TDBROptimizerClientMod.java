package com.tdbr.optimizer;

import com.tdbr.optimizer.command.TDBRCommand;
import com.tdbr.optimizer.config.TDBRConfig;
import com.tdbr.optimizer.maliopt.MaliOptJNA;
import com.tdbr.optimizer.renderer.TDBRDetector;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TDBROptimizerClientMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("tdbr-optimizer");
    
    public static boolean PLS_AVAILABLE = false;
    public static boolean ASTC_AVAILABLE = false;
    public static TDBRConfig CONFIG = TDBRConfig.CONFIG;

    @Override
    public void onInitializeClient() {
        LOGGER.info("TDBR Optimizer v1.0.0 - Inicializando...");
        
        TDBRConfig.load();
        TDBRDetector.detect();
        
        ASTC_AVAILABLE = false;
        PLS_AVAILABLE = false;
        
        LOGGER.info("ASTC: {}", ASTC_AVAILABLE ? "suportado" : "nao suportado");
        LOGGER.info("PLS: {}", PLS_AVAILABLE ? "suportado" : "nao suportado - modo compatibilidade");
        
        TDBRCommand.register();
    }
}
