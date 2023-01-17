package io.quarkus.arc.test.cdi.build.compatible.extensions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.FieldConfig;
import jakarta.enterprise.inject.build.compatible.spi.Messages;
import jakarta.enterprise.inject.build.compatible.spi.Registration;
import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;
import jakarta.enterprise.inject.build.compatible.spi.Validation;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.Arc;
import io.quarkus.arc.test.ArcTestContainer;

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
        private final List<ClassInfo> classes = new ArrayList<>();
        private final List<BeanInfo> beans = new ArrayList<>();

        @Discovery
        public void services(ScannedClasses classes, Messages messages) {
            classes.add(MyFooService.class.getName());
            classes.add(MyBarService.class.getName());
            classes.add(MyBazService.class.getName());
            messages.info("discovery complete");
        }

        @Enhancement(types = MyFooService.class)
        public void foo(ClassConfig clazz, Messages messages) {
            messages.info("before enhancement: " + clazz.info().annotations(), clazz.info());
            clazz.removeAnnotation(ann -> ann.name().equals(MyQualifier.class.getName()));
            messages.info("after enhancement: " + clazz.info().annotations(), clazz.info());
        }

        @Enhancement(types = MyBarService.class)
        public void bar(ClassConfig clazz, Messages messages) {
            messages.info("before enhancement: " + clazz.info().annotations(), clazz.info());
            clazz.addAnnotation(MyQualifier.class);
            messages.info("after enhancement: " + clazz.info().annotations(), clazz.info());
        }

        @Enhancement(types = MyServiceConsumer.class)
        public void service(FieldConfig field, Messages messages) {
            if ("myService".equals(field.info().name())) {
                messages.info("before enhancement: " + field.info().annotations(), field.info());
                field.addAnnotation(MyQualifier.class);
                messages.info("after enhancement: " + field.info().annotations(), field.info());
            }
        }

        @Enhancement(types = MyService.class, withSubtypes = true)
        public void rememberClasses(ClassConfig clazz) {
            classes.add(clazz.info());
        }

        @Registration(types = MyService.class)
        public void rememberBeans(BeanInfo bean) {
            beans.add(bean);
        }

        @Validation
        public void validate(Messages messages) {
            for (ClassInfo clazz : classes) {
                messages.info("class has annotations " + clazz.annotations(), clazz);
            }

            for (BeanInfo bean : beans) {
                messages.info("bean has types " + bean.types(), bean);
            }
        }
    }

    // ---

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
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
