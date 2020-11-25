package io.quarkus.arc.test.cdi.lite.ext;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cdi.lite.extension.AppArchive;
import cdi.lite.extension.AppDeployment;
import cdi.lite.extension.BuildCompatibleExtension;
import cdi.lite.extension.Messages;
import cdi.lite.extension.phases.Discovery;
import cdi.lite.extension.phases.Enhancement;
import cdi.lite.extension.phases.Validation;
import cdi.lite.extension.phases.discovery.AppArchiveBuilder;
import cdi.lite.extension.phases.enhancement.ClassConfig;
import cdi.lite.extension.phases.enhancement.ExactType;
import cdi.lite.extension.phases.enhancement.FieldConfig;
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
            .beanClasses(MyQualifier.class, MyService.class, MyServiceConsumer.class)
            .additionalClasses(MyFooService.class, MyBarService.class, MyBazService.class)
            .buildCompatibleExtensions(MyExtension.class)
            .build();

    @Test
    public void test() {
        MyServiceConsumer myServiceConsumer = Arc.container().select(MyServiceConsumer.class).get();
        assertTrue(myServiceConsumer.myService instanceof MyBarService);
    }

    public static class MyExtension implements BuildCompatibleExtension {
        @Discovery
        public void services(AppArchiveBuilder app, Messages messages) {
            app.addSubtypesOf(MyService.class.getName());
            messages.info("discovery complete");
        }

        @Enhancement
        @ExactType(type = MyFooService.class, annotatedWith = Singleton.class)
        public void foo(ClassConfig clazz, Messages messages) {
            messages.info("before enhancement: " + clazz.annotations(), clazz);
            clazz.removeAnnotation(ann -> ann.name().equals(MyQualifier.class.getName()));
            messages.info("after enhancement: " + clazz.annotations(), clazz);
        }

        @Enhancement
        @ExactType(type = MyBarService.class, annotatedWith = Singleton.class)
        public void bar(ClassConfig clazz, Messages messages) {
            messages.info("before enhancement: " + clazz.annotations(), clazz);
            clazz.addAnnotation(MyQualifier.class);
            messages.info("after enhancement: " + clazz.annotations(), clazz);
        }

        @Enhancement
        @ExactType(type = MyServiceConsumer.class, annotatedWith = Inject.class)
        public void service(FieldConfig field, Messages messages) {
            if ("myService".equals(field.name())) {
                messages.info("before enhancement: " + field.annotations(), field);
                field.addAnnotation(MyQualifier.class);
                messages.info("after enhancement: " + field.annotations(), field);
            }
        }

        @Validation
        public void validate(AppArchive archive, AppDeployment deployment, Messages messages) {
            archive.classes().subtypeOf(MyService.class).forEach(clazz -> {
                messages.info("class has annotations " + clazz.annotations(), clazz);
            });

            deployment.beans().type(MyService.class).forEach(bean -> {
                messages.info("bean has types " + bean.types(), bean);
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
    static class MyBazService implements MyService {
        @Override
        public String hello() {
            throw new UnsupportedOperationException();
        }
    }

    @Singleton
    static class MyServiceConsumer {
        @Inject
        MyService myService;
    }
}
