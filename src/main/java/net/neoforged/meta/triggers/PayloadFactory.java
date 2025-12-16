package net.neoforged.meta.triggers;

import net.neoforged.meta.db.MinecraftVersion;
import net.neoforged.meta.db.NeoForgeVersion;
import net.neoforged.meta.db.event.Event;
import net.neoforged.meta.db.event.ModifiedComponentVersionEvent;
import net.neoforged.meta.db.event.NewComponentVersionEvent;
import net.neoforged.meta.db.event.RemovedComponentVersionEvent;
import net.neoforged.meta.db.event.SoftwareComponentVersionEvent;
import net.neoforged.meta.generated.model.EventPayload;
import net.neoforged.meta.generated.model.EventsPayload;
import net.neoforged.meta.generated.model.MinecraftVersionChangesPayload;
import net.neoforged.meta.generated.model.ModifiedSoftwareComponentVersionEventPayload;
import net.neoforged.meta.generated.model.NeoForgeVersionChangesPayload;
import net.neoforged.meta.generated.model.NewSoftwareComponentVersionEventPayload;
import net.neoforged.meta.generated.model.RemovedSoftwareComponentVersionEventPayload;
import net.neoforged.meta.generated.model.SoftwareComponentChangesPayload;
import net.neoforged.meta.generated.model.SoftwareComponentsChangesPayload;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Creates the models used for webhook payloads from events.
 */
@Component
public class PayloadFactory {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Interpolate payload placeholders.
     */
    public String interpolatePayloads(String template, List<Event> events) {
        var result = new StringBuilder();

        var matcher = PLACEHOLDER.matcher(template);
        while (matcher.find()) {
            var placeholder = matcher.group(1);
            var replacement = switch (placeholder) {
                case "EVENTS" -> mapper.writeValueAsString(createEventsPayload(events));
                case "SOFTWARE_COMPONENTS_CHANGES" ->
                        mapper.writeValueAsString(createSoftwareComponentsChanges(events));
                case "NEOFORGE_VERSION_CHANGES" -> mapper.writeValueAsString(createNeoForgeVersionChanges(events));
                case "MINECRAFT_VERSION_CHANGES" -> mapper.writeValueAsString(createMinecraftVersionChanges(events));
                default ->
                        throw new IllegalArgumentException("Misconfigured payload input. Unknown placeholder: " + placeholder);
            };

            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public EventsPayload createEventsPayload(List<Event> events) {
        return new EventsPayload(events.stream().map(this::createEventPayload).toList());
    }

    public EventPayload createEventPayload(Event event) {
        var payload = switch (event) {
            case NewComponentVersionEvent srcEvent -> {
                var result = new NewSoftwareComponentVersionEventPayload();
                result.setGroupId(srcEvent.getGroupId());
                result.setArtifactId(srcEvent.getArtifactId());
                result.setVersion(srcEvent.getVersion());
                yield result;
            }
            case ModifiedComponentVersionEvent srcEvent -> {
                var result = new ModifiedSoftwareComponentVersionEventPayload();
                result.setGroupId(srcEvent.getGroupId());
                result.setArtifactId(srcEvent.getArtifactId());
                result.setVersion(srcEvent.getVersion());
                yield result;
            }
            case RemovedComponentVersionEvent srcEvent -> {
                var result = new RemovedSoftwareComponentVersionEventPayload();
                result.setGroupId(srcEvent.getGroupId());
                result.setArtifactId(srcEvent.getArtifactId());
                result.setVersion(srcEvent.getVersion());
                yield result;
            }
            default -> throw new IllegalStateException("Unsupported event type: " + event);
        };

        payload.setId(event.getExternalId());
        payload.setCreated(event.getCreated().atOffset(ZoneOffset.UTC));
        return payload;
    }

    public SoftwareComponentsChangesPayload createSoftwareComponentsChanges(List<Event> events) {
        var result = new HashMap<String, SoftwareComponentChangesPayload>();
        for (var event : events) {
            if (event instanceof SoftwareComponentVersionEvent componentEvent) {
                var key = componentEvent.getGroupId() + ":" + componentEvent.getArtifactId();
                var version = componentEvent.getVersion();
                var componentPayload = result.computeIfAbsent(key, _ -> new SoftwareComponentChangesPayload());
                componentPayload.setGroupId(componentEvent.getGroupId());
                componentPayload.setArtifactId(componentEvent.getArtifactId());
                switch (componentEvent) {
                    case NewComponentVersionEvent _ -> componentPayload.addNewVersionsItem(version);
                    case ModifiedComponentVersionEvent _ -> componentPayload.addModifiedVersionsItem(version);
                    case RemovedComponentVersionEvent _ -> componentPayload.addRemovedVersionsItem(version);
                    default -> {
                    }
                }
            }
        }

        return new SoftwareComponentsChangesPayload(new ArrayList<>(result.values()));
    }

    public NeoForgeVersionChangesPayload createNeoForgeVersionChanges(List<Event> events) {
        var result = new NeoForgeVersionChangesPayload();
        for (var event : events) {
            if (event instanceof SoftwareComponentVersionEvent componentEvent && NeoForgeVersion.isNeoForgeGA(componentEvent.getGroupId(), componentEvent.getArtifactId())) {
                var version = componentEvent.getVersion();
                switch (componentEvent) {
                    case NewComponentVersionEvent _ -> result.addNewVersionsItem(version);
                    case ModifiedComponentVersionEvent _ -> result.addModifiedVersionsItem(version);
                    case RemovedComponentVersionEvent _ -> result.addRemovedVersionsItem(version);
                    default -> {
                    }
                }
            }
        }

        return result;
    }

    public MinecraftVersionChangesPayload createMinecraftVersionChanges(List<Event> events) {
        var result = new MinecraftVersionChangesPayload();
        for (var event : events) {
            if (event instanceof SoftwareComponentVersionEvent componentEvent && MinecraftVersion.isMinecraftGA(componentEvent.getGroupId(), componentEvent.getArtifactId())) {
                var version = componentEvent.getVersion();
                switch (componentEvent) {
                    case NewComponentVersionEvent _ -> result.addNewVersionsItem(version);
                    case ModifiedComponentVersionEvent _ -> result.addModifiedVersionsItem(version);
                    case RemovedComponentVersionEvent _ -> result.addRemovedVersionsItem(version);
                    default -> {
                    }
                }
            }
        }

        return result;
    }
}
