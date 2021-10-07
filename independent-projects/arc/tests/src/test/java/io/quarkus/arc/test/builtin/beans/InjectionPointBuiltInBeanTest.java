package io.quarkus.arc.test.builtin.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.arc.Arc;
import io.quarkus.arc.test.ArcTestContainer;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class InjectionPointBuiltInBeanTest {
    @RegisterExtension
    public ArcTestContainer container = ArcTestContainer.builder()
            .beanClasses(MyProducer.class, MyService.class)
            .additionalClasses(MyPojo.class)
            .build();

    @Test
    public void test() {
        MyService bean = Arc.container().select(MyService.class).get();
        assertEquals("Hello MyService.pojo|MyProducer.produce()", bean.hello());
    }

    // ---

    @Singleton
    static class MyProducer {
        @Produces
        @Dependent
        public MyPojo produce(InjectionPoint injectionPoint, Instance<Object> lookup) {
            Field field = ((AnnotatedField<?>) injectionPoint.getAnnotated()).getJavaMember();
            String f = field.getDeclaringClass().getSimpleName() + "." + field.getName();

            // producer method parameters are injection points, so looking up `InjectionPoint` from `lookup`
            // must return the injection point corresponding to the `lookup` producer method parameter
            InjectionPoint instanceInjectionPoint = lookup.select(InjectionPoint.class).get();
            Parameter parameter = ((AnnotatedParameter<?>) instanceInjectionPoint.getAnnotated()).getJavaParameter();
            Executable method = parameter.getDeclaringExecutable();
            String m = method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()";

            return new MyPojo(f + "|" + m);
        }
    }

    @Singleton
    static class MyService {
        @Inject
        MyPojo pojo;

        String hello() {
            return pojo.hello();
        }
    }

    static class MyPojo {
        private final String hello;

        MyPojo(String hello) {
            this.hello = hello;
        }

        public String hello() {
            return "Hello " + hello;
        }
    }
}
