package net.neoforged.meta.db.event;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("new_component_version")
public class NewComponentVersionEvent extends SoftwareComponentVersionEvent {
}
