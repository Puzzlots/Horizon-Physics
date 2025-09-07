package me.zombii.horizon.util;

import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

// Native Library Loader adapted from Nab138's https://github.com/Mr-Zombii/CosmicPhysics/blob/main/src/main/java/me/nabdev/physicsmod/utils/NativeLibraryLoader.java
public final class NativeLibraryLoader {
    public static final Logger logger = Logger.getLogger(NativeLibraryLoader.class.getName());

    public static boolean loadLibbulletjme(boolean isClient, String buildType, String flavor) {
        assert buildType.equals("Debug") || buildType.equals("Release") : buildType;

        assert flavor.equals("Sp") || flavor.equals("SpMt") || flavor.equals("SpMtQuickprof") || flavor.equals("SpQuickprof") || flavor.equals("Dp") || flavor.equals("DpMt") : flavor;

        Platform platform = JmeSystem.getPlatform();
        Platform.Os os = platform.getOs();
        String name = switch (os) {
            case Android -> "Android_x86_64" + buildType + flavor + "_libbulletjme.so";
            case Linux -> "Linux64" + buildType + flavor + "_libbulletjme.so";
            case MacOS -> "MacOSX64" + buildType + flavor + "_libbulletjme.dylib";
            case Windows -> "Windows64" + buildType + flavor + "_bulletjme.dll";
            default -> throw new RuntimeException("platform = " + platform);
        };


        boolean success = false;
        try {
            // have to use a stream
            InputStream in = NativeLibraryLoader.class.getClassLoader().getResourceAsStream("natives/" + name);
            // always write to different location
            File fileOut = new File(System.getProperty("java.io.tmpdir") + "/horizon/natives/" + (isClient ? "client/" : "server/") + name);
            logger.info("Writing native to: " + fileOut.getAbsolutePath());
            OutputStream out = FileUtils.openOutputStream(fileOut);
            assert in != null;
            IOUtils.copy(in, out);
            in.close();
            out.close();
            System.load(fileOut.toString());
            success = true;
            fileOut.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Failed to load native library: " + name);
        }

        return success;
    }
}