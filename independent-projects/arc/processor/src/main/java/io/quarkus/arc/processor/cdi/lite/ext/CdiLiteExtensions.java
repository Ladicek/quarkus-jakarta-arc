package io.quarkus.arc.processor.cdi.lite.ext;

import io.quarkus.arc.InjectableContext;
import io.quarkus.arc.processor.BeanProcessor;
import io.quarkus.arc.processor.ContextConfigurator;
import io.quarkus.arc.processor.ContextRegistrar;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jboss.jandex.IndexView;

public class CdiLiteExtensions {
    private final CdiLiteExtUtil util = new CdiLiteExtUtil();
    private final AllAnnotationOverlays annotationOverlays = new AllAnnotationOverlays();
    private final MessagesImpl messages = new MessagesImpl();

    private List<ContextBuilderImpl> contexts;

    public void runDiscovery(org.jboss.jandex.IndexView applicationIndex, Set<String> additionalClasses) {
        CdiLiteExtDiscoveryProcessor discovery = new CdiLiteExtDiscoveryProcessor(util, applicationIndex, additionalClasses,
                messages);
        discovery.run();
        contexts = discovery.contexts;
    }

    public void runEnhancement(org.jboss.jandex.IndexView beanArchiveIndex, BeanProcessor.Builder builder) {
        if (contexts != null) {
            for (ContextBuilderImpl context : contexts) {
                if (context.implementationClass == null) {
                    // TODO proper diagnostics
                    throw new IllegalArgumentException("Context implementation class not set");
                }

                builder.addContextRegistrar(new ContextRegistrar() {
                    @Override
                    public void register(RegistrationContext registrationContext) {
                        // TODO this is quite weird, maybe redesign the ContextBuilder API?

                        Class<? extends Annotation> scopeAnnotation = context.scopeAnnotation;
                        if (scopeAnnotation == null) {
                            try {
                                scopeAnnotation = context.implementationClass.newInstance().getScope();
                            } catch (ReflectiveOperationException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        Class<? extends InjectableContext> contextClass = (Class<? extends InjectableContext>) context.implementationClass;

                        ContextConfigurator config = registrationContext.configure(scopeAnnotation).contextClass(contextClass);
                        if (context.isNormal != null) {
                            config.normal(context.isNormal);
                        }
                        config.done();
                    }
                });
            }
        }

        new CdiLiteExtEnhancementProcessor(util, beanArchiveIndex, builder, annotationOverlays, messages).run();
    }

    public void runSynthesis() {
        // TODO
    }

    public void runValidation(IndexView beanArchiveIndex, Collection<io.quarkus.arc.processor.BeanInfo> allBeans,
            Collection<io.quarkus.arc.processor.ObserverInfo> allObservers) {
        new CdiLiteExtValidationProcessor(util, beanArchiveIndex, annotationOverlays, allBeans, allObservers, messages).run();
    }

    public List<Throwable> getValidationErrors() {
        return Collections.unmodifiableList(messages.errors);
    }
}
