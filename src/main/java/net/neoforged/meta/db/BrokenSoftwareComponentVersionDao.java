package net.neoforged.meta.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BrokenSoftwareComponentVersionDao extends JpaRepository<BrokenSoftwareComponentVersion, Long> {

    // Recover all versions of a component that are currently broken
    @Query("select bv.id, bv.version, bv.created, bv.lastAttempt, bv.retry from BrokenSoftwareComponentVersion bv where bv.groupId = :groupId and bv.artifactId = :artifactId")
    List<BrokenVersionSummary> getBrokenVersions(String groupId, String artifactId);

    @Modifying
    @Query("update BrokenSoftwareComponentVersion set retry = false, attempts = attempts + 1 where id = :id")
    void incrementRetryCount(long id);

    record BrokenVersionSummary(long id, String version, Instant created, Instant lastAttempt, boolean retry) {
    }

}
