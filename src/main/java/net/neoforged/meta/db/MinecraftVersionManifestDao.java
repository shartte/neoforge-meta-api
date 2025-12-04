package net.neoforged.meta.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MinecraftVersionManifestDao extends JpaRepository<MinecraftVersionManifest, Long> {
    MinecraftVersionManifest findByMinecraftVersion(MinecraftVersion minecraftVersion);
}
