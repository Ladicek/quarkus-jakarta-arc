package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.Set;
import javax.enterprise.inject.build.compatible.spi.AppArchiveBuilder;
import org.jboss.jandex.DotName;

class AppArchiveBuilderImpl implements AppArchiveBuilder {
    private final org.jboss.jandex.IndexView index;
    private final Set<String> classes;

    AppArchiveBuilderImpl(org.jboss.jandex.IndexView index, Set<String> classes) {
        this.index = index;
        this.classes = classes;
    }

    @Override
    public void add(String fullyQualifiedClassName) {
        classes.add(fullyQualifiedClassName);
    }

    @Override
    public void addSubtypesOf(String fullyQualifiedClassName) {
        DotName className = DotName.createSimple(fullyQualifiedClassName);
        for (org.jboss.jandex.ClassInfo subtype : index.getAllKnownImplementors(className)) {
            classes.add(subtype.name().toString());
        }
        for (org.jboss.jandex.ClassInfo subtype : index.getAllKnownSubclasses(className)) {
            classes.add(subtype.name().toString());
        }
    }
}
