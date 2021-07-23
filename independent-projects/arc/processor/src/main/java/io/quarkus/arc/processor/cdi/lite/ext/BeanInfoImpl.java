package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.Collection;
import java.util.stream.Collectors;
import javax.enterprise.inject.build.compatible.spi.BeanInfo;
import javax.enterprise.inject.build.compatible.spi.DisposerInfo;
import javax.enterprise.inject.build.compatible.spi.InjectionPointInfo;
import javax.enterprise.inject.build.compatible.spi.ScopeInfo;
import javax.enterprise.inject.build.compatible.spi.StereotypeInfo;
import javax.enterprise.lang.model.AnnotationInfo;
import javax.enterprise.lang.model.declarations.ClassInfo;
import javax.enterprise.lang.model.declarations.FieldInfo;
import javax.enterprise.lang.model.declarations.MethodInfo;
import javax.enterprise.lang.model.types.Type;
import org.jboss.jandex.IndexView;

class BeanInfoImpl implements BeanInfo<Object> {
    private final IndexView jandexIndex;
    private final AllAnnotationOverlays annotationOverlays;
    private final io.quarkus.arc.processor.BeanInfo arcBeanInfo;

    BeanInfoImpl(IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            io.quarkus.arc.processor.BeanInfo arcBeanInfo) {
        this.jandexIndex = jandexIndex;
        this.annotationOverlays = annotationOverlays;
        this.arcBeanInfo = arcBeanInfo;
    }

    @Override
    public ScopeInfo scope() {
        return new ScopeInfoImpl(jandexIndex, annotationOverlays, arcBeanInfo.getScope());
    }

    @Override
    public Collection<Type> types() {
        return arcBeanInfo.getTypes()
                .stream()
                .map(it -> TypeImpl.fromJandexType(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<AnnotationInfo> qualifiers() {
        return arcBeanInfo.getQualifiers()
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }

    @Override
    public ClassInfo<?> declaringClass() {
        // TODO getImplClass or getBeanClass?
        return new ClassInfoImpl(jandexIndex, annotationOverlays, arcBeanInfo.getImplClazz());
    }

    @Override
    public boolean isClassBean() {
        return arcBeanInfo.isClassBean();
    }

    @Override
    public boolean isProducerMethod() {
        return arcBeanInfo.isProducerMethod();
    }

    @Override
    public boolean isProducerField() {
        return arcBeanInfo.isProducerField();
    }

    @Override
    public boolean isSynthetic() {
        return arcBeanInfo.isSynthetic();
    }

    @Override
    public MethodInfo<?> producerMethod() {
        if (arcBeanInfo.isProducerMethod()) {
            return new MethodInfoImpl(jandexIndex, annotationOverlays, arcBeanInfo.getTarget().get().asMethod());
        } else {
            throw new IllegalStateException("Not a producer method: " + arcBeanInfo);
        }
    }

    @Override
    public FieldInfo<?> producerField() {
        if (arcBeanInfo.isProducerField()) {
            return new FieldInfoImpl(jandexIndex, annotationOverlays, arcBeanInfo.getTarget().get().asField());
        } else {
            throw new IllegalStateException("Not a producer field: " + arcBeanInfo);
        }
    }

    @Override
    public boolean isAlternative() {
        return arcBeanInfo.isAlternative();
    }

    @Override
    public int priority() {
        // TODO default value?
        return arcBeanInfo.getAlternativePriority() != null ? arcBeanInfo.getAlternativePriority() : 0;
    }

    @Override
    public String getName() {
        return arcBeanInfo.getName();
    }

    @Override
    public DisposerInfo disposer() {
        io.quarkus.arc.processor.DisposerInfo disposer = arcBeanInfo.getDisposer();
        return disposer != null ? new DisposerInfoImpl(jandexIndex, annotationOverlays, disposer) : null;
    }

    @Override
    public Collection<StereotypeInfo> stereotypes() {
        return arcBeanInfo.getStereotypes()
                .stream()
                .map(it -> new StereotypeInfoImpl(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<InjectionPointInfo> injectionPoints() {
        return arcBeanInfo.getAllInjectionPoints()
                .stream()
                .map(it -> new InjectionPointInfoImpl(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return arcBeanInfo.toString();
    }
}
