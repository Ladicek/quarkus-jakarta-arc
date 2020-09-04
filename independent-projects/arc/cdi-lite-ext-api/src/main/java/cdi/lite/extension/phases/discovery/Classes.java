package cdi.lite.extension.phases.discovery;

public interface Classes {
    void add(Class<?> clazz);

    // TODO only if ClassInfo/World/etc. is available in @Discovery
    //void add(ClassInfo<?> clazz);
}
