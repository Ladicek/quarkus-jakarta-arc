package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.Objects;
import javax.enterprise.lang.model.declarations.ClassInfo;
import javax.enterprise.lang.model.declarations.FieldInfo;
import javax.enterprise.lang.model.declarations.MethodInfo;
import javax.enterprise.lang.model.declarations.RecordComponentInfo;
import javax.enterprise.lang.model.types.Type;
import org.jboss.jandex.DotName;

class RecordComponentInfoImpl extends DeclarationInfoImpl<org.jboss.jandex.RecordComponentInfo> implements RecordComponentInfo {
    // only for equals/hashCode
    private final DotName className;
    private final String name;

    public RecordComponentInfoImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            org.jboss.jandex.RecordComponentInfo recordComponentInfo) {
        super(jandexIndex, annotationOverlays, recordComponentInfo);
        this.className = recordComponentInfo.declaringClass().name();
        this.name = recordComponentInfo.name();
    }

    @Override
    public String name() {
        return jandexDeclaration.name();
    }

    @Override
    public Type type() {
        return TypeImpl.fromJandexType(jandexIndex, annotationOverlays, jandexDeclaration.type());
    }

    @Override
    public FieldInfo field() {
        return new FieldInfoImpl(jandexIndex, annotationOverlays, jandexDeclaration.field());
    }

    @Override
    public MethodInfo accessor() {
        return new MethodInfoImpl(jandexIndex, annotationOverlays, jandexDeclaration.accessor());
    }

    @Override
    public ClassInfo declaringRecord() {
        return new ClassInfoImpl(jandexIndex, annotationOverlays, jandexDeclaration.declaringClass());
    }

    @Override
    AnnotationsOverlay<?, org.jboss.jandex.RecordComponentInfo> annotationsOverlay() {
        // TODO no need just yet
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RecordComponentInfoImpl))
            return false;
        RecordComponentInfoImpl that = (RecordComponentInfoImpl) o;
        return Objects.equals(className, that.className)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, name);
    }
}
