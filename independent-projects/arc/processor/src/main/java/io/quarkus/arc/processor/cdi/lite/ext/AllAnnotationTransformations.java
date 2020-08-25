package io.quarkus.arc.processor.cdi.lite.ext;

import io.quarkus.arc.processor.BeanProcessor;

class AllAnnotationTransformations {
    final ClassAnnotationTransformations classes = new ClassAnnotationTransformations();
    final MethodAnnotationTransformations methods = new MethodAnnotationTransformations();
    final FieldAnnotationTransformations fields = new FieldAnnotationTransformations();

    void register(BeanProcessor.Builder builder) {
        builder.addAnnotationTransformer(classes);
        builder.addAnnotationTransformer(methods);
        builder.addAnnotationTransformer(fields);
    }
}
