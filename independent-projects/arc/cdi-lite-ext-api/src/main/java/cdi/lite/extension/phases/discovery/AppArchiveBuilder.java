package cdi.lite.extension.phases.discovery;

public interface AppArchiveBuilder {
    void add(Class<?> clazz);

    void addWithSubclasses(Class<?> clazz);
}
