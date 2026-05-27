package com.tdbr.optimizer.maliopt;

import com.sun.jna.Library;
import com.sun.jna.Native;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;

public class MaliOptJNA {
    private static final Logger LOGGER = LoggerFactory.getLogger("tdbr-optimizer");

    public interface MaliOptLib extends Library {
        void run_maliopt_full_scan();
    }

    private static MaliOptLib lib = null;

    public static void loadAndScan() {
        if (lib != null) {
            LOGGER.info("[MaliOpt] Scan ja executado.");
            return;
        }
        File tempLib = extractLibrary();
        if (tempLib == null) return;
        try {
            lib = Native.load(tempLib.getAbsolutePath(), MaliOptLib.class);
            LOGGER.info("[MaliOpt] Biblioteca carregada via JNA. Executando scan...");
            lib.run_maliopt_full_scan();
            LOGGER.info("[MaliOpt] Scan completo. Verifique logcat.");
        } catch (Exception e) {
            LOGGER.error("[MaliOpt] Falha", e);
            lib = null;
        }
    }

    private static File extractLibrary() {
        String libName = "libMaliOpt.so";
        InputStream in = MaliOptJNA.class.getResourceAsStream("/lib/aarch64/" + libName);
        if (in == null) {
            LOGGER.error("[MaliOpt] {} nao encontrada no .jar", libName);
            return null;
        }
        try {
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "tdbr-native");
            tempDir.mkdirs();
            File tempLib = new File(tempDir, libName);
            tempLib.deleteOnExit();
            try (OutputStream out = new FileOutputStream(tempLib)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            }
            in.close();
            tempLib.setExecutable(true, false);
            return tempLib;
        } catch (IOException e) {
            LOGGER.error("[MaliOpt] Erro ao extrair", e);
            return null;
        }
    }
}
