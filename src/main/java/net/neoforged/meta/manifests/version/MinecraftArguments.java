package net.neoforged.meta.manifests.version;

import java.util.List;

public record MinecraftArguments(List<UnresolvedArgument> game, List<UnresolvedArgument> jvm) {
}
