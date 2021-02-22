package cdi.lite.extension.phases.discovery;

public interface AppArchiveBuilder {
    void add(String fullyQualifiedClassName);

    // TODO adds the type itself or not?
    // TODO looks like it can't be implemented on top of Portable Extensions, so maybe remove?
    void addSubtypesOf(String fullyQualifiedClassName);
}
