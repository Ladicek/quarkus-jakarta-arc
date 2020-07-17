package io.quarkus.arc.test.cdi.lite.ext;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cdi.lite.extension.LiteExtension;
import cdi.lite.extension.TypeConfigurator;
import io.quarkus.arc.Arc;
import io.quarkus.arc.test.ArcTestContainer;
import java.lang.annotation.Retention;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ChangeQualifierTest {
    @RegisterExtension
    public ArcTestContainer container = ArcTestContainer.builder()
            .beanClasses(MyExtension.class, MyQualifier.class, MyService.class, MyFooService.class, MyBarService.class,
                    MyServiceConsumer.class)
            .build();

    @Test
    public void testObserved() {
        MyServiceConsumer myServiceConsumer = Arc.container().select(MyServiceConsumer.class).get();
        assertTrue(myServiceConsumer.myService instanceof MyBarService);
    }

    public static class MyExtension {
        @LiteExtension
        public void configureAnnotations(TypeConfigurator<MyFooService> foo,
                TypeConfigurator<MyBarService> bar) {

            foo.removeAnnotation(ann -> ann.name().toString().equals(MyQualifier.class.getName()));
            bar.addAnnotation(MyQualifier.class);
        }

        @LiteExtension
        public void test(Collection<TypeConfigurator<? extends MyService>> upperBound,
                Collection<TypeConfigurator<? super MyService>> lowerBound,
                Collection<TypeConfigurator<MyService>> single,
                Collection<TypeConfigurator<?>> all) {

            System.out.println("!!! upper bound");
            upperBound.stream().map(TypeConfigurator::type).forEach(System.out::println);

            System.out.println("!!! lower bound");
            lowerBound.stream().map(TypeConfigurator::type).forEach(System.out::println);

            System.out.println("!!! single");
            single.stream().map(TypeConfigurator::type).forEach(System.out::println);

            System.out.println("!!! all");
            all.stream().map(TypeConfigurator::type).forEach(System.out::println);
        }
    }

    // ---

    @Qualifier
    @Retention(RUNTIME)
    @interface MyQualifier {
    }

    interface MyService {
    }

    @Singleton
    @MyQualifier
    static class MyFooService implements MyService {
    }

    @Singleton
    static class MyBarService implements MyService {
    }

    @Singleton
    static class MyServiceConsumer {
        @Inject
        @MyQualifier
        MyService myService;
    }

}
