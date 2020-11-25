package io.quarkus.arc.test.cdi.lite.ext;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cdi.lite.extension.AppDeployment;
import cdi.lite.extension.BuildCompatibleExtension;
import cdi.lite.extension.Messages;
import cdi.lite.extension.phases.Synthesis;
import cdi.lite.extension.phases.Validation;
import cdi.lite.extension.phases.synthesis.SyntheticComponents;
import cdi.lite.extension.phases.synthesis.SyntheticObserver;
import io.quarkus.arc.Arc;
import io.quarkus.arc.test.ArcTestContainer;
import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.EventContext;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class SyntheticObserverTest {
    @RegisterExtension
    public ArcTestContainer container = ArcTestContainer.builder()
            .beanClasses(MyQualifier.class, MyService.class)
            .buildCompatibleExtensions(MyExtension.class)
            .build();

    @Test
    public void test() {
        MyService myService = Arc.container().select(MyService.class).get();
        myService.fireEvent();

        // expects 3 items:
        // - Hello World: unqualified event observed by unqualified observer
        // - Hello @MyQualifier SynObserver: qualified event observed by qualified observer
        // - Hello @MyQualifier SynObserver: qualified event observed by unqualified observer -- TODO is this OK?
        assertEquals(3, MyObserver.observed.size());
    }

    public static class MyExtension implements BuildCompatibleExtension {
        @Synthesis
        public void synthesise(SyntheticComponents syn) {
            syn.addObserver()
                    .type(MyEvent.class)
                    .observeWith(MyObserver.class);

            syn.addObserver()
                    .type(MyEvent.class)
                    .qualifier(MyQualifier.class)
                    .observeWith(MyObserver.class);
        }

        @Validation
        public void validate(AppDeployment deployment, Messages messages) {
            deployment.observers().forEach(observer -> {
                messages.info("observer has type " + observer.observedType(), observer);
            });
        }
    }

    // ---

    @Qualifier
    @Retention(RUNTIME)
    @interface MyQualifier {
    }

    static class MyEvent {
        final String payload;

        MyEvent(String payload) {
            this.payload = payload;
        }
    }

    @Singleton
    static class MyService {
        @Inject
        Event<MyEvent> unqualifiedEvent;

        @Inject
        @MyQualifier
        Event<MyEvent> qualifiedEvent;

        void fireEvent() {
            unqualifiedEvent.fire(new MyEvent("Hello World"));
            qualifiedEvent.fire(new MyEvent("Hello @MyQualifier SynObserver"));
        }
    }

    static class MyObserver implements SyntheticObserver<MyEvent> {
        static final List<String> observed = new ArrayList<>();

        @Override
        public void observe(EventContext<MyEvent> event) {
            String payload = event.getEvent().payload;

            System.out.println("observed " + payload);
            observed.add(payload);
        }
    }
}
