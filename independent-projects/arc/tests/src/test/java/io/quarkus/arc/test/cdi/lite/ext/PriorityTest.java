package io.quarkus.arc.test.cdi.lite.ext;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import cdi.lite.extension.BuildCompatibleExtension;
import cdi.lite.extension.ExtensionPriority;
import cdi.lite.extension.phases.Discovery;
import cdi.lite.extension.phases.Enhancement;
import cdi.lite.extension.phases.Validation;
import io.quarkus.arc.test.ArcTestContainer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class PriorityTest {
    @RegisterExtension
    public ArcTestContainer container = ArcTestContainer.builder()
            .buildCompatibleExtensions(MyExtension.class)
            .build();

    @Test
    public void trigger() {
        assertIterableEquals(Arrays.asList("1", "2", "3", "4", "5"), MyExtension.invocations);
    }

    public static class MyExtension implements BuildCompatibleExtension {
        private static final List<String> invocations = new ArrayList<>();

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
        @ExtensionPriority(15)
        public void third() {
            invocations.add("3");
        }

        @Validation
        public void fourth() {
            invocations.add("4");
        }

        @Validation
        @ExtensionPriority(100_000)
        public void fifth() {
            invocations.add("5");
        }
    }
}
