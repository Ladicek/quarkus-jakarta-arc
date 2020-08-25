package io.quarkus.arc.processor.cdi.lite.ext;

import io.quarkus.arc.processor.AnnotationsTransformer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

// other constraints:
// - Key must have equals/hashCode
// - JandexDeclaration must be a Jandex declaration for which Arc supports annotation transformations
abstract class AbstractAnnotationTransformations<Key, JandexDeclaration extends org.jboss.jandex.AnnotationTarget>
        implements AnnotationsTransformer {
    private final org.jboss.jandex.AnnotationTarget.Kind kind;
    private final Map<Key, List<Consumer<TransformationContext>>> transformations = new HashMap<>();

    AbstractAnnotationTransformations(org.jboss.jandex.AnnotationTarget.Kind kind) {
        this.kind = kind;
    }

    void add(JandexDeclaration jandexDeclaration, Consumer<TransformationContext> transformation) {
        Key key = extractKey(jandexDeclaration);
        transformations.computeIfAbsent(key, ignored -> new ArrayList<>()).add(transformation);
    }

    @Override
    public boolean appliesTo(org.jboss.jandex.AnnotationTarget.Kind kind) {
        return this.kind == kind;
    }

    @Override
    public void transform(TransformationContext ctx) {
        JandexDeclaration jandexDeclaration = extractJandexDeclaration(ctx);
        Key key = extractKey(jandexDeclaration);
        transformations.getOrDefault(key, Collections.emptyList())
                .forEach(it -> it.accept(ctx));
    }

    protected abstract JandexDeclaration extractJandexDeclaration(TransformationContext ctx);

    protected abstract Key extractKey(JandexDeclaration jandexDeclaration);
}
