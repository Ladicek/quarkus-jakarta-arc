package io.quarkus.arc.test.cdi.lite.ext;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.arc.Arc;
import io.quarkus.arc.test.ArcTestContainer;
import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.inject.build.compatible.spi.BeanInfo;
import javax.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import javax.enterprise.inject.build.compatible.spi.ClassConfig;
import javax.enterprise.inject.build.compatible.spi.Discovery;
import javax.enterprise.inject.build.compatible.spi.Enhancement;
import javax.enterprise.inject.build.compatible.spi.ExactType;
import javax.enterprise.inject.build.compatible.spi.FieldConfig;
import javax.enterprise.inject.build.compatible.spi.Messages;
import javax.enterprise.inject.build.compatible.spi.Processing;
import javax.enterprise.inject.build.compatible.spi.ScannedClasses;
import javax.enterprise.inject.build.compatible.spi.SubtypesOf;
import javax.enterprise.inject.build.compatible.spi.Validation;
import javax.enterprise.lang.model.declarations.ClassInfo;
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
        private final List<ClassInfo> classes = new ArrayList<>();
        private final List<BeanInfo> beans = new ArrayList<>();

        @Discovery
        public void services(ScannedClasses classes, Messages messages) {
            classes.add(MyFooService.class.getName());
            classes.add(MyBarService.class.getName());
            classes.add(MyBazService.class.getName());
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

        @Enhancement
        @SubtypesOf(type = MyService.class)
        public void rememberClasses(ClassConfig clazz) {
            classes.add(clazz);
        }

        @Processing
        @ExactType(type = MyService.class)
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
