package org.jboss.jandex;

public class HackAnnotationValue extends AnnotationValue {
    private final AnnotationValue delegate;

    public HackAnnotationValue(AnnotationValue delegate) {
        super(delegate.name());
        this.delegate = delegate;
    }

    @Override
    public Object value() {
        return delegate.value();
    }

    @Override
    public Kind kind() {
        return delegate.kind();
    }

    // make org.jboss.jandex.AnnotationValue.asArray() public?
    public AnnotationValue[] asArray() {
        return delegate.asArray();
    }
}
