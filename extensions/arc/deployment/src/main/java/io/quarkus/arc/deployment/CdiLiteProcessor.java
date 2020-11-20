package io.quarkus.arc.deployment;

import io.quarkus.deployment.annotations.BuildStep;

public class CdiLiteProcessor {
    @BuildStep
    CdiLiteBuildItem enable() {
        return new CdiLiteBuildItem();
    }
}
