package io.quarkus.arc.test.cdi.lite.ext;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cdi.lite.extension.Extension;
import cdi.lite.extension.ExtensionPriority;
import cdi.lite.extension.WithAnnotations;
import cdi.lite.extension.World;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.declarations.ParameterInfo;
import cdi.lite.extension.phases.enhancement.ClassConfig;
import cdi.lite.extension.phases.enhancement.FieldConfig;
import io.quarkus.arc.Arc;
import io.quarkus.arc.test.ArcTestContainer;
import java.lang.annotation.Retention;
import java.util.Collection;
import java.util.stream.Collectors;
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
                    Extension.class)
            .build();

    @Test
    public void test() {
        MyServiceConsumer myServiceConsumer = Arc.container().select(MyServiceConsumer.class).get();
        assertTrue(myServiceConsumer.myService instanceof MyBarService);
    }

    public static class MyExtension {
        @Extension
        @ExtensionPriority(0)
        public void configure(ClassConfig<MyFooService> foo, ClassConfig<MyBarService> bar,
                Collection<FieldConfig<MyServiceConsumer>> service) {

            System.out.println("????????? " + foo.annotations());
            System.out.println("????????? " + bar.annotations());
            System.out.println("????????? " + service.stream()
                    .filter(it -> "myService".equals(it.name()))
                    .flatMap(it -> it.annotations().stream())
                    .collect(Collectors.toSet()));

            foo.removeAnnotation(ann -> ann.declaration().name().equals(MyQualifier.class.getName()));
            bar.addAnnotation(MyQualifier.class);
            service.stream()
                    .filter(it -> "myService".equals(it.name()))
                    .forEach(it -> {
                        it.addAnnotation(MyQualifier.class);
                    });

            System.out.println("????????? " + foo.annotations());
            System.out.println("????????? " + bar.annotations());
            System.out.println("????????? " + service.stream()
                    .filter(it -> "myService".equals(it.name()))
                    .flatMap(it -> it.annotations().stream())
                    .collect(Collectors.toSet()));
        }

        @Extension
        public void test(Collection<ClassConfig<? extends MyService>> upperBound,
                Collection<ClassConfig<? super MyService>> lowerBound,
                Collection<ClassConfig<MyService>> single,
                Collection<ClassConfig<?>> all,
                Collection<ClassInfo<?>> allAgain,
                Collection<MethodInfo<? super MyService>> methods,
                Collection<FieldInfo<? extends MyService>> fields,
                Collection<ParameterInfo<MyExtension>> parameters,
                ClassInfo<MyFooService> singleAgain,
                @WithAnnotations(Inject.class) Collection<FieldInfo<?>> fieldsWithAnnotation,
                @WithAnnotations(Extension.class) Collection<MethodInfo<?>> methodsWithAnnotation,
                World world) {

            System.out.println("!!! upper bound");
            upperBound.forEach(System.out::println);

            System.out.println("!!! lower bound");
            lowerBound.forEach(System.out::println);

            System.out.println("!!! single");
            single.forEach(System.out::println);

            System.out.println("!!! all");
            all.forEach(System.out::println);

            System.out.println("!!! all again");
            allAgain.forEach(System.out::println);

            System.out.println("!!! methods");
            methods.forEach(System.out::println);

            System.out.println("!!! fields");
            fields.forEach(System.out::println);

            System.out.println("!!! parameters");
            parameters.forEach(System.out::println);

            System.out.println("!!! single again");
            System.out.println(singleAgain);
            System.out.println(singleAgain.name());
            System.out.println(singleAgain.simpleName());
            System.out.println(singleAgain.packageInfo());
            System.out.println(singleAgain.superClass());
            singleAgain.superInterfaces().forEach(System.out::println);
            singleAgain.constructors().forEach(System.out::println);
            singleAgain.methods().forEach(System.out::println);
            singleAgain.fields().forEach(System.out::println);

            System.out.println("!!! fields with annotation");
            fieldsWithAnnotation.forEach(System.out::println);

            System.out.println("!!! methods with annotation");
            methodsWithAnnotation.forEach(System.out::println);

            System.out.println("!!! world");
            world.classes()
                    .subtypeOf(MyService.class)
                    .annotatedWith(Singleton.class)
                    .stream()
                    .forEach(System.out::println);

            System.out.println("!!! world seeing changed annotations");
            world.classes()
                    .annotatedWith(MyQualifier.class)
                    .stream()
                    .forEach(System.out::println);
            world.fields()
                    .annotatedWith(MyQualifier.class)
                    .stream()
                    .forEach(System.out::println);
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
