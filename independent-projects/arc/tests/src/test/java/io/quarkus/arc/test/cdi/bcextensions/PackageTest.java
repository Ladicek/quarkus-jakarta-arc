package io.quarkus.arc.test.cdi.bcextensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.Validation;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.PackageInfo;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.Arc;
import io.quarkus.arc.test.ArcTestContainer;

public class PackageTest {
    @RegisterExtension
    public static ArcTestContainer container = ArcTestContainer.builder()
            .beanClasses(MyService.class)
            .buildCompatibleExtensions(new MyExtension())
            .build();

    @AfterAll
    public static void cleanup() {
        System.clearProperty("arc.test.executed");
    }

    @Test
    public void test() {
        MyService myService = Arc.container().select(MyService.class).get();
        assertNotNull(myService);

        assertEquals("yes", System.getProperty("arc.test.executed"));
    }

    public static class MyExtension implements BuildCompatibleExtension {
        private static final List<String> values = new ArrayList<>();

        @Enhancement(types = MyService.class)
        public void third(ClassInfo clazz) {
            PackageInfo pkg = clazz.packageInfo();
            values.add(pkg.name());
            values.add(pkg.annotation(MyPackageAnnotation.class).name());
            values.add(pkg.annotation(MyPackageAnnotation.class).value().asString());
        }

        @Validation
        public void test() {
            assertIterableEquals(List.of(PackageTest.class.getPackage().getName(),
                    MyPackageAnnotation.class.getName(), "my package"), MyExtension.values);

            System.setProperty("arc.test.executed", "yes");
        }
    }

    @Singleton
    static class MyService {
    }
}
