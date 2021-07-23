package io.quarkus.arc.test.cdi.lite.ext;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.arc.test.ArcTestContainer;
import java.lang.annotation.Retention;
import java.util.concurrent.atomic.AtomicInteger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.build.compatible.spi.BeanInfo;
import javax.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import javax.enterprise.inject.build.compatible.spi.ExactType;
import javax.enterprise.inject.build.compatible.spi.ObserverInfo;
import javax.enterprise.inject.build.compatible.spi.Processing;
import javax.enterprise.inject.build.compatible.spi.SubtypesOf;
import javax.enterprise.inject.build.compatible.spi.Types;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ProcessingTest {
    @RegisterExtension
    public ArcTestContainer container = ArcTestContainer.builder()
            .beanClasses(MyQualifier.class, MyService.class, MyFooService.class, MyBarServiceProducer.class)
            .buildCompatibleExtensions(MyExtension.class)
            .build();

    @Test
    public void test() {
        assertEquals(2, MyExtension.beanCounter.get());
        assertEquals(1, MyExtension.beanMyQualifierCounter.get());
        assertEquals(1, MyExtension.observerQualifierCounter.get());
    }

    public static class MyExtension implements BuildCompatibleExtension {
        static final AtomicInteger beanCounter = new AtomicInteger();
        static final AtomicInteger beanMyQualifierCounter = new AtomicInteger();
        static final AtomicInteger observerQualifierCounter = new AtomicInteger();

        @Processing
        @SubtypesOf(type = MyService.class)
        public void processBean(BeanInfo<?> bean) {
            beanCounter.incrementAndGet();

            if (bean.qualifiers().stream().anyMatch(it -> it.name().equals(MyQualifier.class.getName()))) {
                beanMyQualifierCounter.incrementAndGet();
            }
        }

        @Processing
        @ExactType(type = Object.class)
        public void processObserver(ObserverInfo<?> observer, Types types) {
            if (observer.declaringClass().superInterfaces().contains(types.of(MyService.class))) {
                observerQualifierCounter.addAndGet(observer.qualifiers().size());
            }
        }
    }

    // ---

    @Qualifier
    @Retention(RUNTIME)
    public @interface MyQualifier {
    }

    public interface MyService {
        String hello();
    }

    @Singleton
    public static class MyFooService implements MyService {
        @Override
        public String hello() {
            return "foo";
        }

        void init(@Observes @Initialized(ApplicationScoped.class) Object event) {
        }
    }

    // intentionally not a bean, to test that producer-based bean is processed
    public static class MyBarService implements MyService {
        @Override
        public String hello() {
            return "bar";
        }
    }

    @Singleton
    public static class MyBarServiceProducer {
        @Produces
        @Singleton
        @MyQualifier
        // must _not_ return `MyService`, because `@SubtypesOf` wouldn't catch that
        // (we currently pretend that subtyping isn't reflexive)
        public MyBarService produce() {
            return new MyBarService();
        }
    }
}
