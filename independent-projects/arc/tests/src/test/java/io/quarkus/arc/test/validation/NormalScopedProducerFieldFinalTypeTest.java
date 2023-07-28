package io.quarkus.arc.test.validation;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.test.ArcTestContainer;

public class NormalScopedProducerFieldFinalTypeTest extends AbstractNormalScopedFinalTest {
    @RegisterExtension
    public static ArcTestContainer container = ArcTestContainer.builder()
            .shouldFail()
            .beanClasses(FieldProducerWithFinalClass.class, FinalFoo.class)
            .build();

    @Override
    protected ArcTestContainer getContainer() {
        return container;
    }
}
