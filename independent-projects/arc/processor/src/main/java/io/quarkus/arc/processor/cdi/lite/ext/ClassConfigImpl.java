package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.configs.ClassConfig;
import cdi.lite.extension.model.declarations.ClassInfo;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.arc.processor.BeanProcessor;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.function.Predicate;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

class ClassConfigImpl extends ClassInfoImpl implements ClassConfig<Object> {
    private final BeanProcessor.Builder builder;

    ClassConfigImpl(org.jboss.jandex.IndexView jandexIndex, org.jboss.jandex.ClassInfo jandexDeclaration,
            BeanProcessor.Builder builder) {
        super(jandexIndex, jandexDeclaration);
        this.builder = builder;
    }

    // TODO we should only create one `AnnotationsTransformer` for all `ClassConfig`s
    //  it would contain a `Map` keyed by the class `DotName`, value being a `List` of transformations

    @Override
    public void addAnnotation(Class<? extends Annotation> clazz, AnnotationAttribute... attributes) {
        builder.addAnnotationTransformer(new AnnotationsTransformer() {
            @Override
            public boolean appliesTo(AnnotationTarget.Kind kind) {
                return kind == org.jboss.jandex.AnnotationTarget.Kind.CLASS;
            }

            @Override
            public void transform(TransformationContext ctx) {
                if (ctx.getTarget().asClass().name().equals(jandexDeclaration.name())) {
                    AnnotationValue[] jandexAnnotationAttributes = Arrays.stream(attributes)
                            .map(it -> ((AnnotationAttributeImpl) it).jandexAnnotationAttribute)
                            .toArray(AnnotationValue[]::new);
                    ctx.transform().add(clazz, jandexAnnotationAttributes).done();
                }
            }
        });
    }

    @Override
    public void addAnnotation(ClassInfo<?> clazz, AnnotationAttribute... attributes) {
        builder.addAnnotationTransformer(new AnnotationsTransformer() {
            @Override
            public boolean appliesTo(AnnotationTarget.Kind kind) {
                return kind == org.jboss.jandex.AnnotationTarget.Kind.CLASS;
            }

            @Override
            public void transform(TransformationContext ctx) {
                if (ctx.getTarget().asClass().name().equals(jandexDeclaration.name())) {
                    DotName jandexName = ((ClassInfoImpl) clazz).jandexDeclaration.name();
                    AnnotationValue[] jandexAnnotationAttributes = Arrays.stream(attributes)
                            .map(it -> ((AnnotationAttributeImpl) it).jandexAnnotationAttribute)
                            .toArray(AnnotationValue[]::new);
                    ctx.transform().add(jandexName, jandexAnnotationAttributes).done();
                }
            }
        });
    }

    @Override
    public void addAnnotation(AnnotationInfo annotation) {
        builder.addAnnotationTransformer(new AnnotationsTransformer() {
            @Override
            public boolean appliesTo(AnnotationTarget.Kind kind) {
                return kind == org.jboss.jandex.AnnotationTarget.Kind.CLASS;
            }

            @Override
            public void transform(TransformationContext ctx) {
                if (ctx.getTarget().asClass().name().equals(jandexDeclaration.name())) {
                    ctx.transform().add(((AnnotationInfoImpl) annotation).jandexAnnotation).done();
                }
            }
        });
    }

    @Override
    public void removeAnnotation(Predicate<AnnotationInfo> predicate) {
        builder.addAnnotationTransformer(new AnnotationsTransformer() {
            @Override
            public boolean appliesTo(AnnotationTarget.Kind kind) {
                return kind == org.jboss.jandex.AnnotationTarget.Kind.CLASS;
            }

            @Override
            public void transform(TransformationContext ctx) {
                if (ctx.getTarget().asClass().name().equals(jandexDeclaration.name())) {
                    ctx.transform().remove(new Predicate<AnnotationInstance>() {
                        @Override
                        public boolean test(AnnotationInstance annotationInstance) {
                            return predicate.test(new AnnotationInfoImpl(jandexIndex, annotationInstance));
                        }
                    }).done();
                }
            }
        });
    }
}
