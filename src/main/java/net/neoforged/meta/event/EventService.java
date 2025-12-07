package net.neoforged.meta.event;

import net.neoforged.meta.db.event.EventDao;
import net.neoforged.meta.db.event.NewComponentVersionEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.UUID;

@Service
public class EventService {
    private final EventDao dao;
    private final TransactionTemplate transactionTemplate;

    public EventService(EventDao dao, PlatformTransactionManager transactionManager) {
        this.dao = dao;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
    }

    public void newComponentVersion(String groupId, String artifactId, String version) {
        var event = new NewComponentVersionEvent();
        event.setGroupId(groupId);
        event.setArtifactId(artifactId);
        event.setVersion(version);
        event.setCreated(Instant.now());
        event.setExternalId(UUID.randomUUID());
        transactionTemplate.executeWithoutResult(ignored -> {
            dao.saveAndFlush(event);
        });
    }
}
