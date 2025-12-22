package net.neoforged.meta.triggers;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import net.neoforged.meta.config.trigger.CommonEventReceiverProperties;
import net.neoforged.meta.db.EventReceiverState;
import net.neoforged.meta.db.EventReceiverStateDao;
import net.neoforged.meta.db.event.Event;
import net.neoforged.meta.db.event.EventDao;
import net.neoforged.meta.db.event.ModifiedComponentVersionEvent;
import net.neoforged.meta.db.event.NewComponentVersionEvent;
import net.neoforged.meta.db.event.RemovedComponentVersionEvent;
import net.neoforged.meta.db.event.SoftwareComponentVersionEvent;
import net.neoforged.meta.triggers.delivery.EventDeliveryStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(initializers = EventDeliveryControllerTest.Initializer.class)
@ActiveProfiles("test")
@MockitoSettings
class EventDeliveryControllerTest {
    static final String GROUP_ID = "somegroup";
    static final String ARTIFACT_ID = "someartifact";
    @TempDir
    static Path tempDir;

    @Autowired
    EventReceiverFactory receiverFactory;
    @Autowired
    EventReceiverStateStore stateStore;
    @Autowired
    EventReceiverStateDao receiverStateDao;
    @Autowired
    EventDao eventDao;

    @Captor
    ArgumentCaptor<List<Event>> events;

    final EventDeliveryStrategy deliveryStrategy1;
    EventReceiver receiver1;
    @Autowired
    private TransactionTemplate transactionTemplate;

    EventDeliveryControllerTest() {
        deliveryStrategy1 = mock(EventDeliveryStrategy.class);
        when(deliveryStrategy1.getMaxBatchSize()).thenReturn(null);
    }

    @BeforeEach
    void cleanUp() {
        eventDao.deleteAll();
        receiverStateDao.deleteAll();

        var properties1 = new CommonEventReceiverProperties();
        properties1.getComponents().add("somegroup:someartifact");
        receiver1 = receiverFactory.createReceiver("receiver1", properties1, deliveryStrategy1);
    }

    @Test
    void testNewEventReceiverWithoutEventsInDatabase() {
        var triggerController = createController(receiver1);
        triggerController.runIteration(true);

        // If no events are present, the receiver should not receive anything, even if it previously has not received anything
        verify(deliveryStrategy1, never()).sendEvents(any());

        // There should still be a state record indicating nothing was synchronized
        var state = stateStore.getById(receiver1.getId());
        assertNotNull(state, "state should be saved, even if no events exist");
        assertNull(state.getHighestEventIdSeen());
    }

    @Test
    void testNewEventReceiverWithoutMatchingEventsInDatabase() {
        // Create an event, but one that does not match the filter on the receiver
        var event = new FakeEvent();
        event.setCreated(Instant.now());
        event.setExternalId(UUID.randomUUID());
        eventDao.save(event);

        var triggerController = createController(receiver1);
        triggerController.runIteration(true);

        // If no events match, the receiver should not receive anything
        verify(deliveryStrategy1, never()).sendEvents(any());
        // There should still be a state record that indicates the event was seen by the receiver (just nothing was sent)
        var state = stateStore.getById(receiver1.getId());
        assertNotNull(state, "state should be saved, even if no matching events exist and were sent");
        assertEquals(event.getId(), state.getHighestEventIdSeen(), "even if the event wasn't sent, it's id should be the last event seen");
    }

    @Test
    void testNewEventReceiverWithMatchingEvents() {
        var event = newVersionEvent(GROUP_ID, ARTIFACT_ID, "1.2.3");

        var triggerController = createController(receiver1);
        triggerController.runIteration(true);

        // Validate the expected event was sent
        verify(deliveryStrategy1).sendEvents(events.capture());
        assertThat(events.getValue()).extracting(Event::getId).containsExactly(event.getId());

        // Check that the state recorded it saw the event
        var state = stateStore.getById(receiver1.getId());
        assertNotNull(state, "state should be saved");
        assertEquals(event.getId(), state.getHighestEventIdSeen(), "the last seen event should be the one we created");

        // Run the iteration again to ensure it does not resend
        clearInvocations(deliveryStrategy1);
        triggerController.runIteration(true);
        verify(deliveryStrategy1, never()).sendEvents(any());
    }

    @Test
    void testReceivesAllEventTypes() {
        var event1 = newVersionEvent(GROUP_ID, ARTIFACT_ID, "1.2.3");
        var event2 = modifiedVersionEvent(GROUP_ID, ARTIFACT_ID, "1.2.4");
        var event3 = removedVersionEvent(GROUP_ID, ARTIFACT_ID, "1.2.5");

        var triggerController = createController(receiver1);
        triggerController.runIteration(true);

        // Validate the expected event was sent
        verify(deliveryStrategy1).sendEvents(events.capture());
        assertThat(events.getValue()).extracting(Event::getId)
                .containsExactly(event1.getId(), event2.getId(), event3.getId());

        // Check that the state recorded it saw the event
        var state = stateStore.getById(receiver1.getId());
        assertNotNull(state, "state should be saved");
        assertEquals(event3.getId(), state.getHighestEventIdSeen(), "should be the last event that we created");

        // Run the iteration again to ensure it does not resend
        clearInvocations(deliveryStrategy1);
        triggerController.runIteration(true);
        verify(deliveryStrategy1, never()).sendEvents(any());
    }

    @Test
    void testNewEventsButReceiverIsPausedIndefinitely() {
        var event = newVersionEvent(GROUP_ID, ARTIFACT_ID, "1.2.3");

        stateStore.pause(receiver1.getId(), null);

        var triggerController = createController(receiver1);
        triggerController.runIteration(true);

        // Validate nothing was sent
        verify(deliveryStrategy1, never()).sendEvents(any());
        // Check that the state also was not updated so it will be sent later
        var state = stateStore.getById(receiver1.getId());
        assertNotNull(state, "state should be saved");
        assertEquals(EventReceiverState.DeliveryState.NEVER, state.getDeliveryState());
        assertNull(state.getHighestEventIdSeen(), "when the receiver is paused, no events should be recorded");
        assertNull(state.getLastFailure());
        assertNull(state.getLastSuccess());

        // Run the iteration again to ensure it does not resend
        clearInvocations(deliveryStrategy1);
        triggerController.runIteration(true);
        verify(deliveryStrategy1, never()).sendEvents(any());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            CCC, 2
            CUD, 2
            CDU, 2
            CUU, 0
            """)
    void testEventCompaction(String typeList, int expectedEventIndex) {
        var storedEvents = typeList.chars().mapToObj(type -> switch (type) {
            case 'C' -> newVersionEvent(GROUP_ID, ARTIFACT_ID, "1");
            case 'U' -> modifiedVersionEvent(GROUP_ID, ARTIFACT_ID, "1");
            case 'D' -> removedVersionEvent(GROUP_ID, ARTIFACT_ID, "1");
            default -> throw new IllegalArgumentException();
        }).toList();
        var expectedEventId = storedEvents.get(expectedEventIndex).getId();

        var triggerController = createController(receiver1);
        triggerController.runIteration(true);

        // Validate the expected event was sent
        verify(deliveryStrategy1).sendEvents(events.capture());
        assertThat(events.getValue()).extracting(Event::getId).containsExactly(expectedEventId);
    }

    private NewComponentVersionEvent newVersionEvent(String groupId, String artifactId, String version) {
        var event = new NewComponentVersionEvent();
        applyComponentVersion(event, groupId, artifactId, version);
        eventDao.save(event);
        return event;
    }

    private ModifiedComponentVersionEvent modifiedVersionEvent(String groupId, String artifactId, String version) {
        var event = new ModifiedComponentVersionEvent();
        applyComponentVersion(event, groupId, artifactId, version);
        eventDao.save(event);
        return event;
    }

    private RemovedComponentVersionEvent removedVersionEvent(String groupId, String artifactId, String version) {
        var event = new RemovedComponentVersionEvent();
        applyComponentVersion(event, groupId, artifactId, version);
        eventDao.save(event);
        return event;
    }

    private static void applyComponentVersion(SoftwareComponentVersionEvent event, String groupId, String artifactId, String version) {
        event.setCreated(Instant.now());
        event.setExternalId(UUID.randomUUID());
        event.setGroupId(groupId);
        event.setArtifactId(artifactId);
        event.setVersion(version);
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(Map.of("meta-api.data-directory", tempDir.toAbsolutePath().toString())).applyTo(context);
        }
    }

    private EventDeliveryController createController(EventReceiver... receivers) {
        return new EventDeliveryController(new EventReceivers(Arrays.asList(receivers)), stateStore, eventDao);
    }
}

@Entity
@DiscriminatorValue("fake")
class FakeEvent extends Event {
}
