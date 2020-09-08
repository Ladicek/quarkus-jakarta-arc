package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.beans.DisposerInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.declarations.ParameterInfo;
import org.jboss.jandex.IndexView;

class DisposerInfoImpl implements DisposerInfo {
    private final org.jboss.jandex.IndexView jandexIndex;
    private final AllAnnotationOverlays annotationOverlays;
    private final io.quarkus.arc.processor.DisposerInfo arcDisposerInfo;

    DisposerInfoImpl(IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            io.quarkus.arc.processor.DisposerInfo arcDisposerInfo) {
        this.jandexIndex = jandexIndex;
        this.annotationOverlays = annotationOverlays;
        this.arcDisposerInfo = arcDisposerInfo;
    }

    @Override
    public MethodInfo<?> disposerMethod() {
        org.jboss.jandex.MethodInfo jandexMethod = arcDisposerInfo.getDisposerMethod();
        return new MethodInfoImpl(jandexIndex, annotationOverlays, jandexMethod);
    }

    @Override
    public ParameterInfo<?> disposedParameter() {
        org.jboss.jandex.MethodParameterInfo jandexParameter = arcDisposerInfo.getDisposedParameter();
        return new ParameterInfoImpl(jandexIndex, annotationOverlays, jandexParameter.method(), jandexParameter.position());
    }
}
