package io.quarkus.arc.deployment;

import io.quarkus.arc.processor.cdi.lite.ext.CdiLiteExtensions;
import io.quarkus.builder.item.SimpleBuildItem;

public final class CdiLiteBuildItem extends SimpleBuildItem {
    public final CdiLiteExtensions extensions = new CdiLiteExtensions();
}
