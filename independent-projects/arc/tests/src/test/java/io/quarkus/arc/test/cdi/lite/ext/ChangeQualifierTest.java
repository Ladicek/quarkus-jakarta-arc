package io.quarkus.arc.test.cdi.lite.ext;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cdi.lite.extension.BuildCompatibleExtension;
import cdi.lite.extension.phases.Enhancement;
import cdi.lite.extension.phases.enhancement.ClassEntrypoint;
import cdi.lite.extension.phases.enhancement.ExactType;
import cdi.lite.extension.phases.enhancement.FieldEntrypoint;
import io.quarkus.arc.Arc;
import io.quarkus.arc.test.ArcTestContainer;
import java.lang.annotation.Retention;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ChangeQualifierTest {
    @RegisterExtension
    public ArcTestContainer container = ArcTestContainer.builder()
            .beanClasses(MyExtension.class, MyQualifier.class, MyService.class, MyFooService.class, MyBarService.class,
                    MyServiceConsumer.class,
                    Enhancement.class)
            .build();

    @Test
    public void test() {
        MyServiceConsumer myServiceConsumer = Arc.container().select(MyServiceConsumer.class).get();
        assertTrue(myServiceConsumer.myService instanceof MyBarService);
    }

    public static class MyExtension implements BuildCompatibleExtension {
        @Enhancement
        public void configure(
                @ExactType(type = MyFooService.class, annotatedWith = Singleton.class) ClassEntrypoint foo,
                @ExactType(type = MyBarService.class, annotatedWith = Singleton.class) ClassEntrypoint bar,
                @ExactType(type = MyServiceConsumer.class, annotatedWith = Inject.class) FieldEntrypoint service) {

            foo.configure(clazz -> {
                System.out.println("????????? MyFooService class " + clazz.annotations());
            });
            bar.configure(clazz -> {
                System.out.println("????????? MyBarService class " + clazz.annotations());
            });
            service.configure(field -> {
                if ("myService".equals(field.name())) {
                    System.out.println("????????? MyServiceConsumer.myService field " + field.annotations());
                }
            });

            foo.removeAnnotation(ann -> ann.name().equals(MyQualifier.class.getName()));
            bar.addAnnotation(MyQualifier.class);
            service.configure(field -> {
                if ("myService".equals(field.name())) {
                    field.addAnnotation(MyQualifier.class);
                }
            });

            foo.configure(clazz -> {
                System.out.println("????????? MyFooService class " + clazz.annotations());
            });
            bar.configure(clazz -> {
                System.out.println("????????? MyBarService class " + clazz.annotations());
            });
            service.configure(field -> {
                if ("myService".equals(field.name())) {
                    System.out.println("????????? MyServiceConsumer.myService field " + field.annotations());
                }
            });
        }
    }

    // ---

    @Qualifier
    @Retention(RUNTIME)
    @interface MyQualifier {
    }

    interface MyService {
        String hello();
    }

    @Singleton
    @MyQualifier
    static class MyFooService implements MyService {
        private final String value = "foo";

        @Override
        public String hello() {
            return value;
        }
    }

    @Singleton
    static class MyBarService implements MyService {
        private static final String VALUE = "bar";

        @Override
        public String hello() {
            return VALUE;
        }
    }

    @Singleton
    static class MyServiceConsumer {
        @Inject
        MyService myService;
    }
}
