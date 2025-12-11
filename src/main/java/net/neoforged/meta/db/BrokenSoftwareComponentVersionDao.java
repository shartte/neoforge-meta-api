package net.neoforged.meta.db;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BrokenSoftwareComponentVersionDao extends JpaRepository<BrokenSoftwareComponentVersion, Long> {

    @Query("select bv.id, bv.version, bv.created, bv.lastAttempt, bv.retry from BrokenSoftwareComponentVersion bv where bv.groupId = :groupId and bv.artifactId = :artifactId")
    List<BrokenVersionSummary> findSummariesByGA(String groupId, String artifactId);

    @Query("select bv from BrokenSoftwareComponentVersion bv where bv.groupId = :groupId and bv.artifactId = :artifactId and bv.version = :version")
    @Nullable
    BrokenSoftwareComponentVersion findByGAV(String groupId, String artifactId, String version);

    @Modifying
    @Query("update BrokenSoftwareComponentVersion set retry = false, attempts = attempts + 1 where id = :id")
    void incrementRetryCount(long id);

    record BrokenVersionSummary(long id, String version, Instant created, Instant lastAttempt, boolean retry) {
    }

}
