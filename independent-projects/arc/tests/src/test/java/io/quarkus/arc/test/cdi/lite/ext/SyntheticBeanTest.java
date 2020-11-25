package io.quarkus.arc.test.cdi.lite.ext;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cdi.lite.extension.AppDeployment;
import cdi.lite.extension.BuildCompatibleExtension;
import cdi.lite.extension.Messages;
import cdi.lite.extension.phases.Synthesis;
import cdi.lite.extension.phases.Validation;
import cdi.lite.extension.phases.synthesis.SyntheticBeanCreator;
import cdi.lite.extension.phases.synthesis.SyntheticBeanDisposer;
import cdi.lite.extension.phases.synthesis.SyntheticComponents;
import io.quarkus.arc.Arc;
import io.quarkus.arc.test.ArcTestContainer;
import java.lang.annotation.Retention;
import java.util.Map;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class SyntheticBeanTest {
    @RegisterExtension
    public ArcTestContainer container = ArcTestContainer.builder()
            .beanClasses(MyQualifier.class, MyService.class)
            .buildCompatibleExtensions(MyExtension.class)
            .build();

    @Test
    public void test() {
        MyService myService = Arc.container().select(MyService.class).get();
        assertEquals("Hello World", myService.unqualified.data);
        assertEquals("Hello @MyQualifier SynBean", myService.qualified.data);
    }

    public static class MyExtension implements BuildCompatibleExtension {
        @Synthesis
        public void synthesise(SyntheticComponents syn) {
            syn.addBean(MyPojo.class)
                    .type(MyPojo.class)
                    .withParam("name", "World")
                    .createWith(MyPojoCreator.class)
                    .disposeWith(MyPojoDisposer.class);

            syn.addBean(MyPojo.class)
                    .type(MyPojo.class)
                    .qualifier(MyQualifier.class)
                    .withParam("name", "SynBean")
                    .createWith(MyPojoCreator.class)
                    .disposeWith(MyPojoDisposer.class);
        }

        @Validation
        public void validate(AppDeployment deployment, Messages messages) {
            deployment.beans().type(MyPojo.class).forEach(bean -> {
                messages.info("bean has types " + bean.types(), bean);
            });
        }
    }

    // ---

    @Qualifier
    @Retention(RUNTIME)
    @interface MyQualifier {
    }

    @Singleton
    static class MyService {
        @Inject
        MyPojo unqualified;

        @Inject
        @MyQualifier
        MyPojo qualified;
    }

    static class MyPojo {
        final String data;

        MyPojo(String data) {
            this.data = data;
        }
    }

    public static class MyPojoCreator implements SyntheticBeanCreator<MyPojo> {
        @Override
        public MyPojo create(CreationalContext<MyPojo> creationalContext, InjectionPoint injectionPoint,
                Map<String, Object> params) {
            String name = (String) params.get("name");

            if (injectionPoint.getQualifiers().stream().anyMatch(it -> it.annotationType().equals(MyQualifier.class))) {
                return new MyPojo("Hello @MyQualifier " + name);
            }

            return new MyPojo("Hello " + name);
        }
    }

    public static class MyPojoDisposer implements SyntheticBeanDisposer<MyPojo> {
        @Override
        public void dispose(MyPojo instance, CreationalContext<MyPojo> creationalContext, Map<String, Object> params) {
            System.out.println("disposing " + instance.data);
        }
    }
}
