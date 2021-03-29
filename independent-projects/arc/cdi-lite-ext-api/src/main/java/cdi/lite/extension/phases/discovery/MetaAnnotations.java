package cdi.lite.extension.phases.discovery;

import cdi.lite.extension.phases.enhancement.ClassConfig;
import java.lang.annotation.Annotation;
import java.util.function.Consumer;

public interface MetaAnnotations {
    void addQualifier(Class<? extends Annotation> annotation, Consumer<ClassConfig<?>> config);

    void addInterceptorBinding(Class<? extends Annotation> annotation, Consumer<ClassConfig<?>> config);

    void addStereotype(Class<? extends Annotation> annotation, Consumer<ClassConfig<?>> config);

    // includes defining the scope annotation
    ContextBuilder addContext();
}
