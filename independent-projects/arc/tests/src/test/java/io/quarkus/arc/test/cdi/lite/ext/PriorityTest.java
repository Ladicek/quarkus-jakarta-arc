package io.quarkus.arc.test.cdi.lite.ext;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import io.quarkus.arc.test.ArcTestContainer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import javax.enterprise.inject.build.compatible.spi.BeanInfo;
import javax.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import javax.enterprise.inject.build.compatible.spi.ClassConfig;
import javax.enterprise.inject.build.compatible.spi.Discovery;
import javax.enterprise.inject.build.compatible.spi.Enhancement;
import javax.enterprise.inject.build.compatible.spi.ExactType;
import javax.enterprise.inject.build.compatible.spi.ExtensionPriority;
import javax.enterprise.inject.build.compatible.spi.Processing;
import javax.enterprise.inject.build.compatible.spi.SubtypesOf;
import javax.enterprise.inject.build.compatible.spi.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class PriorityTest {
    @RegisterExtension
    public ArcTestContainer container = ArcTestContainer.builder()
            .buildCompatibleExtensions(MyExtension.class)
            .build();

    @Test
    public void trigger() {
        assertIterableEquals(Arrays.asList("1", "2", "3", "4", "5", "6"), MyExtension.invocations);
    }

    public static class MyExtension implements BuildCompatibleExtension {
        private static final LinkedHashSet<String> invocations = new LinkedHashSet<>();

        @Discovery
        @ExtensionPriority(10)
        public void first() {
            invocations.add("1");
        }

        @Discovery
        @ExtensionPriority(20)
        public void second() {
            invocations.add("2");
        }

        @Enhancement
        @SubtypesOf(type = Object.class)
        @ExtensionPriority(15)
        public void third(ClassConfig clazz) {
            invocations.add("3");
        }

        @Processing
        @ExactType(type = Object.class)
        @ExtensionPriority(5)
        public void fourth(BeanInfo clazz) {
            invocations.add("4");
        }

        @Validation
        public void fifth() {
            invocations.add("5");
        }

        @Validation
        @ExtensionPriority(100_000)
        public void sixth() {
            invocations.add("6");
        }
    }
}
