package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.beans.BeanInfo;
import cdi.lite.extension.model.beans.DisposerInfo;
import cdi.lite.extension.model.beans.InjectionPointInfo;
import cdi.lite.extension.model.beans.ScopeInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.types.Type;
import java.util.Collection;
import java.util.stream.Collectors;
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
        // TODO what if none?
        return new DisposerInfoImpl(jandexIndex, annotationOverlays, arcBeanInfo.getDisposer());
    }

    @Override
    public Collection<InjectionPointInfo> injectionPoints() {
        return arcBeanInfo.getAllInjectionPoints()
                .stream()
                .map(it -> new InjectionPointInfoImpl(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }
}
