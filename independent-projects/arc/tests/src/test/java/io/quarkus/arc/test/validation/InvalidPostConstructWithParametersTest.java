package io.quarkus.arc.test.validation;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.DefinitionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.Unremovable;
import io.quarkus.arc.test.ArcTestContainer;

public class InvalidPostConstructWithParametersTest {

    @RegisterExtension
    public ArcTestContainer container = ArcTestContainer.builder().beanClasses(InvalidBean.class).shouldFail().build();

    @Test
    public void testFailure() {
        Throwable error = container.getFailure();
        assertNotNull(error);
        assertInstanceOf(DefinitionException.class, error);
        assertTrue(error.getMessage().contains(
                "@PostConstruct lifecycle callback method declared in a target class must have zero parameters"));
        assertTrue(error.getMessage().contains("invalid(java.lang.String ignored)"));
        assertTrue(error.getMessage().contains("InvalidPostConstructWithParametersTest$InvalidBean"));
    }

    @ApplicationScoped
    @Unremovable
    public static class InvalidBean {

        @PostConstruct
        public void invalid(String ignored) {

        }
    }
}
