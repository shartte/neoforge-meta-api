package net.neoforged.meta.util;

public enum OsType {
    WINDOWS,
    MAC,
    LINUX;

    public static OsType current() {
        if (OsUtil.isWindows()) {
            return WINDOWS;
        } else if (OsUtil.isMac()) {
            return MAC;
        } else if (OsUtil.isLinux()) {
            return LINUX;
        }
        throw new UnsupportedOperationException("Unknown OS: " + System.getProperty("os.name"));
    }
}
