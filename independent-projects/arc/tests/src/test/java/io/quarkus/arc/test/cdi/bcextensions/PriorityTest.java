package io.quarkus.arc.test.cdi.bcextensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.LinkedHashSet;
import java.util.List;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.Registration;
import jakarta.enterprise.inject.build.compatible.spi.Validation;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.test.ArcTestContainer;

public class PriorityTest {
    @RegisterExtension
    public static ArcTestContainer container = ArcTestContainer.builder()
            .buildCompatibleExtensions(new MyExtension())
            .build();

    @AfterAll
    public static void cleanup() {
        System.clearProperty("arc.test.executed");
    }

    @Test
    public void test() {
        assertEquals("yes", System.getProperty("arc.test.executed"));
    }

    public static class MyExtension implements BuildCompatibleExtension {
        private static final LinkedHashSet<String> invocations = new LinkedHashSet<>();

        @Discovery
        @Priority(10)
        public void first() {
            invocations.add("1");
        }

        @Discovery
        @Priority(20)
        public void second() {
            invocations.add("2");
        }

        @Enhancement(types = Object.class, withSubtypes = true)
        @Priority(15)
        public void third(ClassConfig clazz) {
            invocations.add("3");
        }

        @Registration(types = Object.class)
        @Priority(5)
        public void fourth(BeanInfo clazz) {
            invocations.add("4");
        }

        @Validation
        public void fifth() {
            invocations.add("5");
        }

        @Validation
        @Priority(100_000)
        public void sixth() {
            invocations.add("6");
        }

        @Validation
        @Priority(1_000_000)
        public void test() {
            assertIterableEquals(List.of("1", "2", "3", "4", "5", "6"), MyExtension.invocations);

            System.setProperty("arc.test.executed", "yes");
        }
    }
}
