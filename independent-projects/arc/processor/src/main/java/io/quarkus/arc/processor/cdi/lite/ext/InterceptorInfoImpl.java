package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.Collection;
import java.util.stream.Collectors;
import javax.enterprise.inject.build.compatible.spi.InterceptorInfo;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.lang.model.AnnotationInfo;

class InterceptorInfoImpl extends BeanInfoImpl implements InterceptorInfo {
    private final io.quarkus.arc.processor.InterceptorInfo arcInterceptorInfo;

    InterceptorInfoImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            io.quarkus.arc.processor.InterceptorInfo arcInterceptorInfo) {
        super(jandexIndex, annotationOverlays, arcInterceptorInfo);
        this.arcInterceptorInfo = arcInterceptorInfo;
    }

    @Override
    public Integer priority() {
        return arcInterceptorInfo.getPriority();
    }

    @Override
    public Collection<AnnotationInfo> interceptorBindings() {
        return arcInterceptorInfo.getBindings()
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean intercepts(InterceptionType interceptionType) {
        return arcInterceptorInfo.intercepts(interceptionType);
    }
}
