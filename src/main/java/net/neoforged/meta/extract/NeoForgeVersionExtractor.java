package net.neoforged.meta.extract;

import net.neoforged.meta.manifests.installer.InstallerProfile;
import net.neoforged.meta.manifests.version.MinecraftVersionManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.zip.ZipFile;

public final class NeoForgeVersionExtractor {
    private static final Logger logger = LoggerFactory.getLogger(NeoForgeVersionExtractor.class);

    private NeoForgeVersionExtractor() {
    }

    public record Metadata(
            String minecraftVersion,
            String launcherProfileId,
            String launcherProfile,
            String installerProfile
    ) {
    }

    public static Metadata extract(byte[] installerJarContent) {
        Path installer = null;
        try {
            installer = Files.createTempFile("installer", "zip");
            Files.write(installer, installerJarContent);

            String installerProfileText, versionManifestText;
            InstallerProfile installerProfile;
            MinecraftVersionManifest versionManifest;
            try (var zf = new ZipFile(installer.toFile())) {
                installerProfileText = readEntryAsString(zf, "install_profile.json");
                installerProfile = InstallerProfile.from(installerProfileText);
                versionManifestText = readEntryAsString(zf, installerProfile.getJson());
                versionManifest = MinecraftVersionManifest.from(versionManifestText);
            }

            return new Metadata(
                    installerProfile.getMinecraft(),
                    versionManifest.id(),
                    versionManifestText,
                    installerProfileText
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (installer != null) {
                try {
                    Files.deleteIfExists(installer);
                } catch (IOException e) {
                    logger.error("Failed to delete temporary file {}", installer, e);
                }
            }
        }
    }

    private static String readEntryAsString(ZipFile zf, String name) throws IOException {
        // Strip leading slash
        if (name.startsWith("/")) {
            name = name.substring(1);
        }

        var installProfile = zf.getEntry(name);
        if (installProfile == null) {
            throw new IllegalStateException("Required entry " + name + " is missing.");
        }

        try (var input = zf.getInputStream(installProfile)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
