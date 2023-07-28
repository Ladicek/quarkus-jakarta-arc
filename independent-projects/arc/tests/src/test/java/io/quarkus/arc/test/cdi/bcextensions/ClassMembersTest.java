package io.quarkus.arc.test.cdi.bcextensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.Validation;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.Arc;
import io.quarkus.arc.test.ArcTestContainer;

public class ClassMembersTest {
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
        static ClassInfo clazz;

        @Enhancement(types = MyService.class)
        public void service(ClassInfo clazz) {
            MyExtension.clazz = clazz;
        }

        @Validation
        public void test() {
            assertNotNull(MyExtension.clazz);
            assertEquals(3, MyExtension.clazz.methods().size());
            assertEquals(2, MyExtension.clazz.superInterfacesDeclarations().get(0).methods().size());

            System.setProperty("arc.test.executed", "yes");
        }
    }

    // ---

    interface A {
        void foo();
    }

    interface B {
        void foo();
    }

    interface C extends A, B {
    }

    @Singleton
    static class MyService implements C {
        @Override
        public void foo() {
        }
    }
}
