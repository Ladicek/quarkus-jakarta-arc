package io.quarkus.arc.deployment;

import io.quarkus.arc.processor.cdi.lite.ext.ExtensionsEntryPoint;
import io.quarkus.builder.item.SimpleBuildItem;

public final class CdiLiteBuildItem extends SimpleBuildItem {
    public final ExtensionsEntryPoint extensions = new ExtensionsEntryPoint();
}
