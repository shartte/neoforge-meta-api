package net.neoforged.meta.db.event;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("modified_component_version")
public class ModifiedComponentVersionEvent extends SoftwareComponentVersionEvent {
}
