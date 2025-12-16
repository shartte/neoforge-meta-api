package net.neoforged.meta.db.event;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("removed_component_version")
public class RemovedComponentVersionEvent extends SoftwareComponentVersionEvent {
}
