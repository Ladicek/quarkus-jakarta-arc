package cdi.lite.extension.phases.discovery;

public interface AppArchiveBuilder {
    void add(String fullyQualifiedClassName);

    // TODO adds the type itself or not?
    void addSubtypesOf(String fullyQualifiedClassName);
}
