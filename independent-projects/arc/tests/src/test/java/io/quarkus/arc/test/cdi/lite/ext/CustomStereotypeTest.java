package io.quarkus.arc.test.cdi.lite.ext;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.arc.test.ArcTestContainer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations;
import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

// TODO migrated to CDI TCK
public class CustomStereotypeTest {
    @RegisterExtension
    public ArcTestContainer container = ArcTestContainer.builder()
            .additionalClasses(MyAnnotation.class, MyService.class)
            .buildCompatibleExtensions(MyExtension.class)
            .build();

    @Test
    public void test() {
        InstanceHandle<MyService> bean = Arc.container().select(MyService.class).getHandle();
        assertEquals(ApplicationScoped.class, bean.getBean().getScope());
        assertEquals("Hello!", bean.get().hello());
    }

    public static class MyExtension implements BuildCompatibleExtension {
        @Discovery
        public void discovery(MetaAnnotations meta, ScannedClasses scan) {
            scan.add("io.quarkus.arc.test.cdi.lite.ext.CustomStereotypeTest$MyService");

            ClassConfig cfg = meta.addStereotype(MyAnnotation.class);
            cfg.addAnnotation(ApplicationScoped.class);
        }
    }

    // ---

    @Retention(RetentionPolicy.RUNTIME)
    @interface MyAnnotation {
    }

    @MyAnnotation
    static class MyService {
        String hello() {
            return "Hello!";
        }
    }
}
