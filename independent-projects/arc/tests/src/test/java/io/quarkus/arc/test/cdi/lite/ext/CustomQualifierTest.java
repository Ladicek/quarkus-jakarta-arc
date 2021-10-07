package io.quarkus.arc.test.cdi.lite.ext;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.arc.Arc;
import io.quarkus.arc.test.ArcTestContainer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import javax.enterprise.inject.build.compatible.spi.ClassConfig;
import javax.enterprise.inject.build.compatible.spi.Discovery;
import javax.enterprise.inject.build.compatible.spi.MetaAnnotations;
import javax.enterprise.inject.build.compatible.spi.ScannedClasses;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

// TODO migrated to CDI TCK
public class CustomQualifierTest {
    @RegisterExtension
    public ArcTestContainer container = ArcTestContainer.builder()
            .additionalClasses(MyAnnotation.class, MyAnnotationLiteral.class, MyService.class, MyServiceFoo.class,
                    MyServiceBar.class)
            .buildCompatibleExtensions(MyExtension.class)
            .build();

    @Test
    public void test() {
        MyService myService = Arc.container().select(MyService.class, new MyAnnotationLiteral("something")).get();
        assertEquals("bar", myService.hello());
    }

    public static class MyExtension implements BuildCompatibleExtension {
        @Discovery
        public void discovery(MetaAnnotations meta, ScannedClasses scan) {
            scan.add("io.quarkus.arc.test.cdi.lite.ext.CustomQualifierTest$MyServiceFoo");
            scan.add("io.quarkus.arc.test.cdi.lite.ext.CustomQualifierTest$MyServiceBar");

            ClassConfig cfg = meta.addQualifier(MyAnnotation.class);
            cfg.methods()
                    .stream()
                    .filter(it -> "value".equals(it.info().name()))
                    .forEach(it -> it.addAnnotation(Nonbinding.class));
        }
    }

    // ---

    @Retention(RetentionPolicy.RUNTIME)
    @interface MyAnnotation {
        String value();
    }

    static class MyAnnotationLiteral extends AnnotationLiteral<MyAnnotation> implements MyAnnotation {
        private final String value;

        MyAnnotationLiteral(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }

    interface MyService {
        String hello();
    }

    @Singleton
    static class MyServiceFoo implements MyService {
        @Override
        public String hello() {
            return "foo";
        }
    }

    @Singleton
    @MyAnnotation("this should be ignored, the value member should be treated as @Nonbinding")
    static class MyServiceBar implements MyService {
        @Override
        public String hello() {
            return "bar";
        }
    }
}
