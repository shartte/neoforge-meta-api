package net.neoforged.meta.db.event;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventDao extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    @Query("select max(id) from Event")
    @Nullable
    Long getHighestEventId();
}
