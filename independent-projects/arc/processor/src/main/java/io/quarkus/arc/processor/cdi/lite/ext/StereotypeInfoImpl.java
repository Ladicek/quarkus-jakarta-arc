package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.beans.ScopeInfo;
import cdi.lite.extension.beans.StereotypeInfo;
import cdi.lite.extension.model.AnnotationInfo;
import java.util.Collection;
import java.util.stream.Collectors;
import org.jboss.jandex.IndexView;

class StereotypeInfoImpl implements StereotypeInfo {
    private final IndexView jandexIndex;
    private final AllAnnotationOverlays annotationOverlays;
    private final io.quarkus.arc.processor.StereotypeInfo arcStereotype;

    StereotypeInfoImpl(IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            io.quarkus.arc.processor.StereotypeInfo arcStereotype) {
        this.jandexIndex = jandexIndex;
        this.annotationOverlays = annotationOverlays;
        this.arcStereotype = arcStereotype;
    }

    @Override
    public ScopeInfo defaultScope() {
        return new ScopeInfoImpl(jandexIndex, annotationOverlays, arcStereotype.getDefaultScope());
    }

    @Override
    public Collection<AnnotationInfo> interceptorBindings() {
        return arcStereotype.getInterceptorBindings()
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAlternative() {
        return arcStereotype.isAlternative();
    }

/*
    @Override
    public int priority() {
        // TODO default value?
        return arcStereotype.getAlternativePriority() != null ? arcStereotype.getAlternativePriority() : 0;
    }
*/

    @Override
    public boolean isNamed() {
        return arcStereotype.isNamed();
    }
}
