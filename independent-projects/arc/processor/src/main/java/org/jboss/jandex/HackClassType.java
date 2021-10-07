package org.jboss.jandex;

public class HackClassType {
    // make org.jboss.jandex.Type.copyType() public?
    public static ClassType create(DotName name, Type copyAnnotationsFrom) {
        return new ClassType(name, copyAnnotationsFrom.annotationArray());
    }
}
