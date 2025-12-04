package net.neoforged.meta.api;

import net.neoforged.meta.db.MinecraftVersionDao;
import net.neoforged.meta.generated.model.MinecraftVersionDetails;
import net.neoforged.meta.generated.model.MinecraftVersionSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@RestController
public class VersionsController implements net.neoforged.meta.generated.api.VersionsApi {

    private final MinecraftVersionDao minecraftVersionDao;

    public VersionsController(MinecraftVersionDao minecraftVersionDao) {
        this.minecraftVersionDao = minecraftVersionDao;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<MinecraftVersionSummary>> getMinecraftVersions() {
        var result = new ArrayList<MinecraftVersionSummary>();
        for (var version : minecraftVersionDao.findAll()) {
            var summary = new MinecraftVersionSummary();
            summary.setVersion(version.getVersion());
            summary.setReleased(version.getReleased().atOffset(ZoneOffset.UTC));
            summary.setType(version.getType());
            result.add(summary);
        }

        return ResponseEntity.ok(result);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<MinecraftVersionDetails> getMinecraftVersionDetails(String versionId) {
        var version = minecraftVersionDao.getByVersion(versionId);
        if (version == null) {
            return ResponseEntity.notFound().build();
        }

        var details = new MinecraftVersionDetails();
        details.setVersion(version.getVersion());
        details.setType(version.getType());
        details.setReleased(version.getReleased().atOffset(ZoneOffset.UTC));
        return ResponseEntity.ok(details);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getMinecraftVersionManifest(String versionId) {
        var version = minecraftVersionDao.getByVersion(versionId);
        if (version == null) {
            return ResponseEntity.notFound().build();
        }

        var manifest = version.getManifest();
        if (manifest == null) {
            return ResponseEntity.notFound().build();
        }

        // If there's a source URL, redirect to it; otherwise return the content directly
        if (manifest.getSourceUrl() != null) {
            return ResponseEntity.status(307).location(URI.create(manifest.getSourceUrl())).build();
        } else {
            return ResponseEntity.ok(manifest.getContent());
        }
    }
}
