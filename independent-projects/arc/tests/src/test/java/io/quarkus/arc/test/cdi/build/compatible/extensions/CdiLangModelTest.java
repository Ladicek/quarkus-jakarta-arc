package io.quarkus.arc.test.cdi.build.compatible.extensions;

import java.io.IOException;
import java.lang.annotation.Annotation;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

import org.jboss.cdi.lang.model.tck.LangModelVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.test.ArcTestContainer;

public class CdiLangModelTest {
    // the lang model TCK classes are on classpath, so no need to add them to the container in any special way

    @RegisterExtension
    public ArcTestContainer container = ArcTestContainer.builder()
            .buildCompatibleExtensions(LangModelVerifierExtension.class)
            .build();

    @Test
    public void test() throws IOException {
    }

    public static class LangModelVerifierExtension implements BuildCompatibleExtension {
        @Enhancement(types = LangModelVerifier.class, withAnnotations = Annotation.class)
        public void run(ClassInfo clazz) {
            LangModelVerifier.verify(clazz);
        }
    }
}
