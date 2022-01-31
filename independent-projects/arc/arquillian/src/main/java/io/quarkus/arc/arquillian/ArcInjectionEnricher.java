package io.quarkus.arc.arquillian;

import io.quarkus.arc.ArcContainer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;

public class ArcInjectionEnricher implements TestEnricher {
    @Inject
    private Instance<ArcContainer> runningArc;

    @Override
    public void enrich(Object testCase) {
        // not needed, we make the test class a bean and look it up from the container,
        // and Arquillian won't invoke this anyway (because we don't use the "local" protocol)
    }

    @Override
    public Object[] resolve(Method method) {
        Parameter[] parameters = method.getParameters();
        Object[] result = new Object[parameters.length];

        boolean hasNonArquillianDataProvider = false;
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation.annotationType().getName().equals("org.testng.annotations.Test")) {
                try {
                    Method dataProviderMember = annotation.annotationType().getDeclaredMethod("dataProvider");
                    String value = dataProviderMember.invoke(annotation).toString();
                    hasNonArquillianDataProvider = !value.equals("") && !value.equals("ARQUILLIAN_DATA_PROVIDER");
                    break;
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }
        if (hasNonArquillianDataProvider) {
            return result;
        }

        ArcContainer arcContainer = runningArc.get();
        for (int i = 0; i < parameters.length; i++) {
            // TODO are qualifiers needed?
            //result[i] = arcContainer.select(parameters[i].getType(), getQualifiers(parameters[i]));
            result[i] = arcContainer.select(parameters[i].getType());
        }
        return result;
    }

    /*
     * private static Annotation[] getQualifiers(Parameter parameter) {
     * return Arrays.stream(parameter.getAnnotations())
     * .filter(ArcInjectionEnricher::isQualifier)
     * .toArray(Annotation[]::new);
     * }
     * 
     * private static boolean isQualifier(Annotation annotation) {
     * for (Annotation metaAnnotation : annotation.annotationType().getAnnotations()) {
     * if ("javax.inject.Qualifier".equals(metaAnnotation.annotationType().getName()))
     * return true;
     * }
     * }
     * return false;
     * }
     */
}
