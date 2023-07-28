package io.quarkus.arc.test.validation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.test.ArcTestContainer;

public class InitializerMethodMarkedProducerTest {
    @RegisterExtension
    public static ArcTestContainer container = ArcTestContainer.builder().beanClasses(Producers.class).shouldFail().build();

    @Test
    public void testFailure() {
        Throwable error = container.getFailure();
        assertNotNull(error);
        assertTrue(error instanceof DefinitionException);
    }

    @ApplicationScoped
    static class Producers {
        @Inject
        @Produces
        @ApplicationScoped
        MyBean produce() {
            return new MyBean();
        }
    }

    static class MyBean {
    }
}
