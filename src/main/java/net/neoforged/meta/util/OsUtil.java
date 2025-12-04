package net.neoforged.meta.util;

public final class OsUtil {
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    private OsUtil() {
    }

    public static boolean isWindows() {
        return OS_NAME.contains("win");
    }

    public static boolean isMac() {
        return OS_NAME.contains("mac") || OS_NAME.contains("darwin");
    }

    public static boolean isLinux() {
        return OS_NAME.contains("nux") || OS_NAME.contains("nix");
    }
}
