package io.quarkus.arc.processor.cdi.lite.ext;

import io.quarkus.arc.processor.AnnotationsTransformer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jboss.jandex.AnnotationTarget;

abstract class AbstractAnnotationTransformations<Key> implements AnnotationsTransformer {
    private final org.jboss.jandex.AnnotationTarget.Kind kind;
    private final Map<Key, List<Consumer<TransformationContext>>> transformations = new HashMap<>();

    AbstractAnnotationTransformations(AnnotationTarget.Kind kind) {
        this.kind = kind;
    }

    void add(Key key, Consumer<TransformationContext> transformation) {
        transformations.computeIfAbsent(key, ignored -> new ArrayList<>()).add(transformation);
    }

    @Override
    public boolean appliesTo(org.jboss.jandex.AnnotationTarget.Kind kind) {
        return this.kind == kind;
    }

    @Override
    public void transform(TransformationContext ctx) {
        Key key = extractKey(ctx);
        transformations.getOrDefault(key, Collections.emptyList())
                .forEach(it -> it.accept(ctx));
    }

    protected abstract Key extractKey(TransformationContext ctx);
}
