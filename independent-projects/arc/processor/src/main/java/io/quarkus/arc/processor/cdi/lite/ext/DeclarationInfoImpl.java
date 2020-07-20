package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.declarations.DeclarationInfo;

// TODO all subclasses must have equals, hashCode and perhaps also their own toString (though the current one is fine)
abstract class DeclarationInfoImpl<JandexDeclaration extends org.jboss.jandex.AnnotationTarget> implements DeclarationInfo {
    final org.jboss.jandex.IndexView jandexIndex;
    final JandexDeclaration jandexDeclaration;

    DeclarationInfoImpl(org.jboss.jandex.IndexView jandexIndex, JandexDeclaration jandexDeclaration) {
        this.jandexIndex = jandexIndex;
        this.jandexDeclaration = jandexDeclaration;
    }

    static DeclarationInfo fromJandexDeclaration(org.jboss.jandex.IndexView jandexIndex,
            org.jboss.jandex.AnnotationTarget jandexDeclaration) {
        switch (jandexDeclaration.kind()) {
            case CLASS:
                return new ClassInfoImpl(jandexIndex, jandexDeclaration.asClass());
            case METHOD:
                return new MethodInfoImpl(jandexIndex, jandexDeclaration.asMethod());
            case METHOD_PARAMETER:
                return new ParameterInfoImpl(jandexIndex, jandexDeclaration.asMethodParameter().method(),
                        jandexDeclaration.asMethodParameter().position());
            case FIELD:
                return new FieldInfoImpl(jandexIndex, jandexDeclaration.asField());
            default:
                throw new IllegalStateException("Unknown declaration " + jandexDeclaration);
        }
    }

    @Override
    public String toString() {
        return jandexDeclaration.toString();
    }
}
