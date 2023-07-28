package io.quarkus.arc.test.validation;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.test.ArcTestContainer;

public class NormalScopedProducerFieldWrongConstructorTest extends AbstractNormalScopedFinalTest {
    @RegisterExtension
    public static ArcTestContainer container = ArcTestContainer.builder()
            .shouldFail()
            .beanClasses(FieldProducerWithWrongConstructor.class, WrongConstructorFoo.class)
            .build();

    @Override
    protected ArcTestContainer getContainer() {
        return container;
    }
}
