package io.quarkus.arc.test.injection.constructornoinject;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.test.ArcTestContainer;

public class ObservesAsyncParamConstructorTest {

    @RegisterExtension
    public static ArcTestContainer container = ArcTestContainer.builder()
            .beanClasses(MyBean.class)
            .shouldFail()
            .build();

    @Test
    public void testInjection() {
        Throwable error = container.getFailure();
        assertNotNull(error);
        assertTrue(error instanceof DefinitionException);
        assertTrue(error.getMessage().contains("Bean constructor must not have an @ObservesAsync parameter"));
    }

    @Dependent
    static class MyBean {
        @Inject
        public MyBean(@ObservesAsync String ignored) {
        }
    }
}
