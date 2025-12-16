package net.neoforged.meta.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventReceiverStateDao extends JpaRepository<EventReceiverState, String> {
}
