package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.beans.ScopeInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import org.jboss.jandex.IndexView;

class ScopeInfoImpl implements ScopeInfo {
    private final org.jboss.jandex.IndexView jandexIndex;
    private final AllAnnotationOverlays annotationOverlays;
    private final io.quarkus.arc.processor.ScopeInfo arcScopeInfo;

    ScopeInfoImpl(IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            io.quarkus.arc.processor.ScopeInfo arcScopeInfo) {
        this.jandexIndex = jandexIndex;
        this.annotationOverlays = annotationOverlays;
        this.arcScopeInfo = arcScopeInfo;
    }

    @Override
    public ClassInfo<?> annotation() {
        org.jboss.jandex.ClassInfo jandexClass = jandexIndex.getClassByName(arcScopeInfo.getDotName());
        return new ClassInfoImpl(jandexIndex, annotationOverlays, jandexClass);
    }

    @Override
    public boolean isNormal() {
        return arcScopeInfo.isNormal();
    }
}
