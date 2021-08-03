package io.quarkus.arc.processor.cdi.lite.ext;

import java.lang.annotation.Annotation;
import javax.enterprise.inject.build.compatible.spi.AnnotationBuilder;
import javax.enterprise.inject.build.compatible.spi.AnnotationBuilderFactory;
import javax.enterprise.lang.model.declarations.ClassInfo;
import org.jboss.jandex.DotName;

public class AnnotationBuilderFactoryImpl implements AnnotationBuilderFactory {
    private static org.jboss.jandex.IndexView beanArchiveIndex;
    private static AllAnnotationOverlays annotationOverlays;

    static void init(org.jboss.jandex.IndexView beanArchiveIndex, AllAnnotationOverlays annotationOverlays) {
        AnnotationBuilderFactoryImpl.beanArchiveIndex = beanArchiveIndex;
        AnnotationBuilderFactoryImpl.annotationOverlays = annotationOverlays;
    }

    static void reset() {
        AnnotationBuilderFactoryImpl.beanArchiveIndex = null;
        AnnotationBuilderFactoryImpl.annotationOverlays = null;
    }

    @Override
    public AnnotationBuilder create(Class<? extends Annotation> annotationType) {
        if (beanArchiveIndex == null || annotationOverlays == null) {
            throw new IllegalStateException("Can't create AnnotationBuilder right now");
        }

        DotName jandexAnnotationName = DotName.createSimple(annotationType.getName());
        return new AnnotationBuilderImpl(beanArchiveIndex, annotationOverlays, jandexAnnotationName);
    }

    @Override
    public AnnotationBuilder create(ClassInfo<?> annotationType) {
        if (beanArchiveIndex == null || annotationOverlays == null) {
            throw new IllegalStateException("Can't create AnnotationBuilder right now");
        }

        DotName jandexAnnotationName = ((ClassInfoImpl) annotationType).jandexDeclaration.name();
        return new AnnotationBuilderImpl(beanArchiveIndex, annotationOverlays, jandexAnnotationName);
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
