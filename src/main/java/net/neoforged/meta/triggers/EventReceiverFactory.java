package net.neoforged.meta.triggers;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import net.neoforged.meta.config.trigger.CommonEventReceiverProperties;
import net.neoforged.meta.config.trigger.GitHubWorkflowTriggerProperties;
import net.neoforged.meta.db.MinecraftVersion;
import net.neoforged.meta.db.NeoForgeVersion;
import net.neoforged.meta.db.event.Event;
import net.neoforged.meta.db.event.NewComponentVersionEvent;
import net.neoforged.meta.triggers.delivery.EventDeliveryStrategy;
import net.neoforged.meta.triggers.delivery.GitHubWorkflowDeliveryStrategy;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class EventReceiverFactory {
    private final PayloadFactory payloadFactory;

    public EventReceiverFactory(PayloadFactory payloadFactory) {
        this.payloadFactory = payloadFactory;
    }

    public EventReceiver createGitHubWorkflow(String id, GitHubWorkflowTriggerProperties properties) {
        var strategy = new GitHubWorkflowDeliveryStrategy(properties, payloadFactory);
        return createReceiver(id, properties, strategy);
    }

    public EventReceiver createReceiver(String id,
                                        CommonEventReceiverProperties properties,
                                        EventDeliveryStrategy strategy) {
        return new EventReceiver(
                id,
                properties.isCompactEvents(),
                properties.getMaxBatchSize(),
                eventFilter(properties),
                strategy
        );
    }

    private static PredicateSpecification<Event> eventFilter(CommonEventReceiverProperties properties) {
        return (from, cb) -> {
            var filteredComponents = new ArrayList<>(properties.getComponents());
            var predicates = new ArrayList<Predicate>();

            // Use treat() to downcast Event to NewComponentVersionEvent to access groupId/artifactId
            // Since we use a single table mapping strategy, and we have to use an Entity here, we just use the new event to access the fields.
            var componentVersionFrom = cb.treat(from, NewComponentVersionEvent.class);
            Path<String> groupIdAttr = componentVersionFrom.get("groupId");
            Path<String> artifactIdAttr = componentVersionFrom.get("artifactId");

            if (filteredComponents.contains("*:*")) {
                return null; // Unrestricted component filter.
            }

            for (var component : filteredComponents) {
                // Build a disjunction of all the filtered components
                var parts = component.split(":", 2);
                var groupId = parts[0];
                var artifactId = parts[1];
                if (groupId.equals("*")) {
                    predicates.add(cb.equal(artifactIdAttr, artifactId));
                } else if (artifactId.equals("*")) {
                    predicates.add(cb.equal(groupIdAttr, groupId));
                } else {
                    predicates.add(cb.and(
                            cb.equal(groupIdAttr, groupId),
                            cb.equal(artifactIdAttr, artifactId)
                    ));
                }
            }

            if (properties.isNeoforgeVersions()) {
                predicates.add(cb.and(
                        cb.equal(groupIdAttr, NeoForgeVersion.GROUP_ID),
                        cb.or(
                                cb.equal(artifactIdAttr, NeoForgeVersion.FORGE_ARTIFACT_ID),
                                cb.equal(artifactIdAttr, NeoForgeVersion.NEOFORGE_ARTIFACT_ID)
                        )
                ));
            }

            if (properties.isMinecraftVersions()) {
                predicates.add(cb.and(
                        cb.equal(groupIdAttr, MinecraftVersion.MINECRAFT_GROUP_ID),
                        cb.equal(artifactIdAttr, MinecraftVersion.MINECRAFT_ARTIFACT_ID)
                ));
            }

            return predicates.isEmpty() ? null : cb.or(predicates);
        };
    }
}
