package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.declarations.PackageInfo;
import cdi.lite.extension.model.types.Type;
import cdi.lite.extension.model.types.TypeVariable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jboss.jandex.DotName;

class ClassInfoImpl extends DeclarationInfoImpl<org.jboss.jandex.ClassInfo> implements ClassInfo<Object> {
    // only for equals/hashCode
    private final DotName name;

    ClassInfoImpl(org.jboss.jandex.IndexView jandexIndex, org.jboss.jandex.ClassInfo jandexDeclaration) {
        super(jandexIndex, jandexDeclaration);
        this.name = jandexDeclaration.name();
    }

    @Override
    public String name() {
        return jandexDeclaration.name().toString();
    }

    @Override
    public String simpleName() {
        return jandexDeclaration.simpleName();
    }

    @Override
    public PackageInfo packageInfo() {
        String fqn = jandexDeclaration.name().toString();
        int lastDot = fqn.lastIndexOf('.');
        return new PackageInfoImpl(fqn.substring(0, lastDot));
    }

    @Override
    public List<TypeVariable> typeParameters() {
        return jandexDeclaration.typeParameters()
                .stream()
                .map(it -> TypeImpl.fromJandexType(jandexIndex, it))
                .filter(Type::isTypeVariable) // not necessary, just as a precaution
                .map(Type::asTypeVariable) // not necessary, just as a precaution
                .collect(Collectors.toList());
    }

    @Override
    public Type superClass() {
        return TypeImpl.fromJandexType(jandexIndex, jandexDeclaration.superClassType());
    }

    @Override
    public ClassInfo<?> superClassDeclaration() {
        return new ClassInfoImpl(jandexIndex, jandexIndex.getClassByName(jandexDeclaration.superName()));
    }

    @Override
    public List<Type> superInterfaces() {
        return jandexDeclaration.interfaceTypes()
                .stream()
                .map(it -> TypeImpl.fromJandexType(jandexIndex, it))
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassInfo<?>> superInterfacesDeclarations() {
        return jandexDeclaration.interfaceNames()
                .stream()
                .map(it -> new ClassInfoImpl(jandexIndex, jandexIndex.getClassByName(it)))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPlainClass() {
        // TODO there must be a better way
        return !isInterface() && !isEnum() && !isAnnotation();
    }

    @Override
    public boolean isInterface() {
        return Modifier.isInterface(jandexDeclaration.flags());
    }

    @Override
    public boolean isEnum() {
        return jandexDeclaration.isEnum();
    }

    @Override
    public boolean isAnnotation() {
        return jandexDeclaration.isAnnotation();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(jandexDeclaration.flags());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(jandexDeclaration.flags());
    }

    @Override
    public int modifiers() {
        return jandexDeclaration.flags();
    }

    @Override
    public Collection<MethodInfo<Object>> constructors() {
        return jandexDeclaration.methods()
                .stream()
                .filter(MethodPredicates.IS_CONSTRUCTOR_JANDEX)
                .map(it -> new MethodInfoImpl(jandexIndex, it))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<MethodInfo<Object>> methods() {
        return jandexDeclaration.methods()
                .stream()
                .filter(MethodPredicates.IS_METHOD_JANDEX)
                .map(it -> new MethodInfoImpl(jandexIndex, it))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<FieldInfo<Object>> fields() {
        return jandexDeclaration.fields()
                .stream()
                .map(it -> new FieldInfoImpl(jandexIndex, it))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return jandexDeclaration.classAnnotation(DotName.createSimple(annotationType.getName())) != null;
    }

    @Override
    public AnnotationInfo annotation(Class<? extends Annotation> annotationType) {
        return new AnnotationInfoImpl(jandexIndex,
                jandexDeclaration.classAnnotation(DotName.createSimple(annotationType.getName())));
    }

    @Override
    public Collection<AnnotationInfo> repeatableAnnotation(Class<? extends Annotation> annotationType) {
        return jandexDeclaration.classAnnotationsWithRepeatable(DotName.createSimple(annotationType.getName()), jandexIndex)
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, it))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations() {
        return jandexDeclaration.classAnnotations()
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, it))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ClassInfoImpl classInfo = (ClassInfoImpl) o;
        return Objects.equals(name, classInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
