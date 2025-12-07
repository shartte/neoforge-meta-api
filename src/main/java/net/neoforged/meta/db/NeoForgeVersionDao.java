package net.neoforged.meta.db;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NeoForgeVersionDao extends org.springframework.data.jpa.repository.JpaRepository<NeoForgeVersion, Long> {
    /**
     * Finds the latest NeoForge version for each Minecraft version using a subquery.
     * This efficiently retrieves all latest versions in a single database query.
     * @return List of the latest NeoForge version for each Minecraft version
     */
    @Query("""
        SELECT nf FROM NeoForgeVersion nf
        WHERE (nf.minecraftVersion, nf.released) IN (
            SELECT nf2.minecraftVersion, MAX(nf2.released)
            FROM NeoForgeVersion nf2
            GROUP BY nf2.minecraftVersion
        )
        """)
    List<NeoForgeVersion> findLatestForAllMinecraftVersions();
}
