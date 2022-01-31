package io.quarkus.arc.test.cdi.lite.ext;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import io.quarkus.arc.test.ArcTestContainer;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

// TODO migrated to CDI Lang Model TCK
public class PackageTest {
    @RegisterExtension
    public ArcTestContainer container = ArcTestContainer.builder()
            .beanClasses(MyService.class)
            .buildCompatibleExtensions(MyExtension.class)
            .build();

    @Test
    public void trigger() {
        assertIterableEquals(Arrays.asList(PackageTest.class.getPackage().getName(),
                MyPackageAnnotation.class.getName(), "my package"), MyExtension.values);
    }

    public static class MyExtension implements BuildCompatibleExtension {
        private static final List<String> values = new ArrayList<>();

        @Enhancement(types = MyService.class)
        public void third(ClassConfig clazz) {
            values.add(clazz.info().packageInfo().name());
            values.add(clazz.info().packageInfo().annotation(MyPackageAnnotation.class).name());
            values.add(clazz.info().packageInfo().annotation(MyPackageAnnotation.class).value().asString());
        }
    }

    @Singleton
    static class MyService {
    }
}
