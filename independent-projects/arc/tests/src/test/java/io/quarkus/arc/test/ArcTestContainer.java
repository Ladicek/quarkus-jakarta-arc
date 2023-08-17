package io.quarkus.arc.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;

import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.DynamicTestInvocationContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcInitConfig;
import io.quarkus.arc.ComponentsProvider;
import io.quarkus.arc.ResourceReferenceProvider;
import io.quarkus.arc.processor.AlternativePriorities;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.arc.processor.BeanArchives;
import io.quarkus.arc.processor.BeanDeploymentValidator;
import io.quarkus.arc.processor.BeanInfo;
import io.quarkus.arc.processor.BeanProcessor;
import io.quarkus.arc.processor.BeanRegistrar;
import io.quarkus.arc.processor.BytecodeTransformer;
import io.quarkus.arc.processor.ContextRegistrar;
import io.quarkus.arc.processor.InjectionPointsTransformer;
import io.quarkus.arc.processor.InterceptorBindingRegistrar;
import io.quarkus.arc.processor.ObserverRegistrar;
import io.quarkus.arc.processor.ObserverTransformer;
import io.quarkus.arc.processor.QualifierRegistrar;
import io.quarkus.arc.processor.ResourceOutput;
import io.quarkus.arc.processor.StereotypeRegistrar;
import io.quarkus.arc.processor.bcextensions.ExtensionsEntryPoint;

/**
 * JUnit5 extension for ArC bootstrap/shutdown.
 * Must be used via {@code @RegisterExtension static} fields in tests.
 * <p>
 * It bootstraps ArC before each test method and shuts down afterwards.
 * Leverages root {@code ExtensionContext.Store} to store some state.
 */
public class ArcTestContainer
        implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback, InvocationInterceptor {

    // our specific namespace for storing anything into ExtensionContext.Store
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ArcTestContainer.class);

    // Strings used as keys in ExtensionContext.Store
    private static final String KEY_OLD_TCCL = "arcExtensionOldTccl";
    private static final String KEY_TEST_INSTANCES = "arcExtensionTestInstanceStack";
    private static final String KEY_FAILURE = "arcExtensionFailure";

    private static final String TARGET_TEST_CLASSES = "target/test-classes";

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<Class<?>> resourceReferenceProviders;
        private final List<Class<?>> beanClasses;
        private final List<Class<?>> additionalClasses;
        private final List<Class<? extends Annotation>> resourceAnnotations;
        private final List<BeanRegistrar> beanRegistrars;
        private final List<ObserverRegistrar> observerRegistrars;
        private final List<ContextRegistrar> contextRegistrars;
        private final List<QualifierRegistrar> qualifierRegistrars;
        private final List<InterceptorBindingRegistrar> interceptorBindingRegistrars;
        private final List<StereotypeRegistrar> stereotypeRegistrars;
        private final List<AnnotationsTransformer> annotationsTransformers;
        private final List<InjectionPointsTransformer> injectionsPointsTransformers;
        private final List<ObserverTransformer> observerTransformers;
        private final List<BeanDeploymentValidator> beanDeploymentValidators;
        private boolean shouldFail = false;
        private boolean removeUnusedBeans = false;
        private final List<Predicate<BeanInfo>> exclusions;
        private AlternativePriorities alternativePriorities;
        private boolean transformUnproxyableClasses = false;
        private final List<BuildCompatibleExtension> buildCompatibleExtensions;
        private boolean strictCompatibility = false;

        public Builder() {
            resourceReferenceProviders = new ArrayList<>();
            beanClasses = new ArrayList<>();
            additionalClasses = new ArrayList<>();
            resourceAnnotations = new ArrayList<>();
            beanRegistrars = new ArrayList<>();
            observerRegistrars = new ArrayList<>();
            contextRegistrars = new ArrayList<>();
            qualifierRegistrars = new ArrayList<>();
            interceptorBindingRegistrars = new ArrayList<>();
            stereotypeRegistrars = new ArrayList<>();
            annotationsTransformers = new ArrayList<>();
            injectionsPointsTransformers = new ArrayList<>();
            observerTransformers = new ArrayList<>();
            beanDeploymentValidators = new ArrayList<>();
            exclusions = new ArrayList<>();
            buildCompatibleExtensions = new ArrayList<>();
        }

        public Builder resourceReferenceProviders(Class<?>... resourceReferenceProviders) {
            Collections.addAll(this.resourceReferenceProviders, resourceReferenceProviders);
            return this;
        }

        public Builder beanClasses(Class<?>... beanClasses) {
            Collections.addAll(this.beanClasses, beanClasses);
            return this;
        }

        public Builder additionalClasses(Class<?>... additionalClasses) {
            Collections.addAll(this.additionalClasses, additionalClasses);
            return this;
        }

        @SafeVarargs
        public final Builder resourceAnnotations(Class<? extends Annotation>... resourceAnnotations) {
            Collections.addAll(this.resourceAnnotations, resourceAnnotations);
            return this;
        }

        public Builder beanRegistrars(BeanRegistrar... registrars) {
            Collections.addAll(this.beanRegistrars, registrars);
            return this;
        }

        public Builder observerRegistrars(ObserverRegistrar... registrars) {
            Collections.addAll(this.observerRegistrars, registrars);
            return this;
        }

        public Builder contextRegistrars(ContextRegistrar... registrars) {
            Collections.addAll(this.contextRegistrars, registrars);
            return this;
        }

        public Builder annotationsTransformers(AnnotationsTransformer... transformers) {
            Collections.addAll(this.annotationsTransformers, transformers);
            return this;
        }

        public Builder injectionPointsTransformers(InjectionPointsTransformer... transformers) {
            Collections.addAll(this.injectionsPointsTransformers, transformers);
            return this;
        }

        public Builder observerTransformers(ObserverTransformer... transformers) {
            Collections.addAll(this.observerTransformers, transformers);
            return this;
        }

        public Builder qualifierRegistrars(QualifierRegistrar... registrars) {
            Collections.addAll(this.qualifierRegistrars, registrars);
            return this;
        }

        public Builder interceptorBindingRegistrars(InterceptorBindingRegistrar... registrars) {
            Collections.addAll(this.interceptorBindingRegistrars, registrars);
            return this;
        }

        public Builder stereotypeRegistrars(StereotypeRegistrar... registrars) {
            Collections.addAll(this.stereotypeRegistrars, registrars);
            return this;
        }

        public Builder beanDeploymentValidators(BeanDeploymentValidator... validators) {
            Collections.addAll(this.beanDeploymentValidators, validators);
            return this;
        }

        public Builder removeUnusedBeans(boolean value) {
            this.removeUnusedBeans = value;
            return this;
        }

        public Builder addRemovalExclusion(Predicate<BeanInfo> exclusion) {
            this.exclusions.add(exclusion);
            return this;
        }

        public Builder shouldFail() {
            this.shouldFail = true;
            return this;
        }

        public Builder alternativePriorities(AlternativePriorities priorities) {
            this.alternativePriorities = priorities;
            return this;
        }

        public Builder transformUnproxyableClasses(boolean transformUnproxyableClasses) {
            this.transformUnproxyableClasses = transformUnproxyableClasses;
            return this;
        }

        public final Builder buildCompatibleExtensions(BuildCompatibleExtension... extensions) {
            Collections.addAll(this.buildCompatibleExtensions, extensions);
            return this;
        }

        public Builder strictCompatibility(boolean strictCompatibility) {
            this.strictCompatibility = strictCompatibility;
            return this;
        }

        public ArcTestContainer build() {
            return new ArcTestContainer(this);
        }

    }

    private final List<Class<?>> resourceReferenceProviders;

    private final List<Class<?>> beanClasses;
    private final List<Class<?>> additionalClasses;

    private final List<Class<? extends Annotation>> resourceAnnotations;

    private final List<BeanRegistrar> beanRegistrars;
    private final List<ObserverRegistrar> observerRegistrars;
    private final List<ContextRegistrar> contextRegistrars;
    private final List<QualifierRegistrar> qualifierRegistrars;
    private final List<InterceptorBindingRegistrar> interceptorBindingRegistrars;
    private final List<StereotypeRegistrar> stereotypeRegistrars;
    private final List<AnnotationsTransformer> annotationsTransformers;
    private final List<InjectionPointsTransformer> injectionPointsTransformers;
    private final List<ObserverTransformer> observerTransformers;
    private final List<BeanDeploymentValidator> beanDeploymentValidators;

    private final boolean shouldFail;
    private final AtomicReference<Throwable> buildFailure;

    private final boolean removeUnusedBeans;
    private final List<Predicate<BeanInfo>> exclusions;

    private final AlternativePriorities alternativePriorities;

    private final boolean transformUnproxyableClasses;

    private final List<BuildCompatibleExtension> buildCompatibleExtensions;

    private final boolean strictCompatibility;

    public ArcTestContainer(Class<?>... beanClasses) {
        this.resourceReferenceProviders = Collections.emptyList();
        this.beanClasses = Arrays.asList(beanClasses);
        this.additionalClasses = Collections.emptyList();
        this.resourceAnnotations = Collections.emptyList();
        this.beanRegistrars = Collections.emptyList();
        this.observerRegistrars = Collections.emptyList();
        this.contextRegistrars = Collections.emptyList();
        this.interceptorBindingRegistrars = Collections.emptyList();
        this.stereotypeRegistrars = Collections.emptyList();
        this.qualifierRegistrars = Collections.emptyList();
        this.annotationsTransformers = Collections.emptyList();
        this.injectionPointsTransformers = Collections.emptyList();
        this.observerTransformers = Collections.emptyList();
        this.beanDeploymentValidators = Collections.emptyList();
        this.buildFailure = new AtomicReference<Throwable>(null);
        this.shouldFail = false;
        this.removeUnusedBeans = false;
        this.exclusions = Collections.emptyList();
        this.alternativePriorities = null;
        this.transformUnproxyableClasses = false;
        this.buildCompatibleExtensions = Collections.emptyList();
        this.strictCompatibility = false;
    }

    public ArcTestContainer(Builder builder) {
        this.resourceReferenceProviders = builder.resourceReferenceProviders;
        this.beanClasses = builder.beanClasses;
        this.additionalClasses = builder.additionalClasses;
        this.resourceAnnotations = builder.resourceAnnotations;
        this.beanRegistrars = builder.beanRegistrars;
        this.observerRegistrars = builder.observerRegistrars;
        this.contextRegistrars = builder.contextRegistrars;
        this.qualifierRegistrars = builder.qualifierRegistrars;
        this.interceptorBindingRegistrars = builder.interceptorBindingRegistrars;
        this.stereotypeRegistrars = builder.stereotypeRegistrars;
        this.annotationsTransformers = builder.annotationsTransformers;
        this.injectionPointsTransformers = builder.injectionsPointsTransformers;
        this.observerTransformers = builder.observerTransformers;
        this.beanDeploymentValidators = builder.beanDeploymentValidators;
        this.buildFailure = new AtomicReference<Throwable>(null);
        this.shouldFail = builder.shouldFail;
        this.removeUnusedBeans = builder.removeUnusedBeans;
        this.exclusions = builder.exclusions;
        this.alternativePriorities = builder.alternativePriorities;
        this.transformUnproxyableClasses = builder.transformUnproxyableClasses;
        this.buildCompatibleExtensions = builder.buildCompatibleExtensions;
        this.strictCompatibility = builder.strictCompatibility;
    }

    // set up a classloader, which is per top-level test class
    @Override
    public void beforeAll(ExtensionContext context) {
        if (context.getRequiredTestClass().isAnnotationPresent(Nested.class)) {
            return;
        }

        initTestInstanceStack(context);

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        context.getRoot().getStore(NAMESPACE).put(KEY_OLD_TCCL, tccl);

        ArcTestClassLoader newTccl = init(context);
        Thread.currentThread().setContextClassLoader(newTccl);

        Throwable failure = context.getRoot().getStore(NAMESPACE).get(KEY_FAILURE, Throwable.class);
        if (failure != null) {
            try {
                Class<?> atcClass = newTccl.loadClass(ArcTestContainer.class.getName());
                Class<?> clazz = newTccl.loadClass(context.getRequiredTestClass().getName());
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(RegisterExtension.class) && field.getType().equals(atcClass)) {
                        field.setAccessible(true);
                        Object atc = field.get(null); // we know it's `static`
                        Method setFailure = atcClass.getDeclaredMethod("setFailure", Throwable.class);
                        setFailure.setAccessible(true);
                        setFailure.invoke(atc, failure);
                        break;
                    }
                }
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    // start ArC, which is per test method
    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        // prevent non-`static` declaration of `@RegisterExtension ArcTestContainer`
        if (!(Thread.currentThread().getContextClassLoader() instanceof ArcTestClassLoader)) {
            throw new IllegalStateException("The `@RegisterExtension` field of type `ArcTestContainer` must be `static` in "
                    + extensionContext.getRequiredTestClass());
        }

        if (extensionContext.getRoot().getStore(NAMESPACE).get(KEY_FAILURE) != null) {
            return;
        }

        try {
            Class<?> clazz = ArcTestClassLoader.inTCCL().loadClass(ArcTestContainer.class.getName());
            Method method = clazz.getDeclaredMethod("tcclBeforeEach", boolean.class);
            method.setAccessible(true);
            method.invoke(null, strictCompatibility);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    // shut down Arc
    @Override
    public void afterEach(ExtensionContext extensionContext) {
        if (extensionContext.getRoot().getStore(NAMESPACE).get(KEY_FAILURE) != null) {
            return;
        }

        try {
            Class<?> clazz = ArcTestClassLoader.inTCCL().loadClass(ArcTestContainer.class.getName());
            Method method = clazz.getDeclaredMethod("tcclAfterEach");
            method.setAccessible(true);
            method.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    // tear down the shared class loader
    @Override
    public void afterAll(ExtensionContext context) {
        popTestInstance(context);

        if (context.getRequiredTestClass().isAnnotationPresent(Nested.class)) {
            return;
        }

        context.getRoot().getStore(NAMESPACE).remove(KEY_FAILURE);

        destroyTestInstanceStack(context);

        ClassLoader oldTccl = context.getRoot().getStore(NAMESPACE).remove(KEY_OLD_TCCL, ClassLoader.class);
        Thread.currentThread().setContextClassLoader(oldTccl);
    }

    private static void tcclBeforeEach(boolean strictCompatibility) {
        Arc.initialize(ArcInitConfig.builder().setStrictCompatibility(strictCompatibility).build());
    }

    private static void tcclAfterEach() {
        Arc.shutdown();
    }

    @Override
    public void interceptBeforeAllMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {

        if (invocationContext.getExecutable().getParameterCount() != 0) {
            throw new UnsupportedOperationException("@BeforeAll method must have no parameter");
        }

        ArcTestClassLoader cl = ArcTestClassLoader.inTCCL();
        Class<?> clazz = cl.loadClass(invocationContext.getTargetClass().getName());
        Method method = findZeroParamMethod(clazz, invocationContext.getExecutable().getName());
        method.setAccessible(true);
        method.invoke(null);
        invocation.skip();
    }

    @Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation,
            ReflectiveInvocationContext<Constructor<T>> invocationContext,
            ExtensionContext extensionContext) throws Throwable {

        ArcTestClassLoader cl = ArcTestClassLoader.inTCCL();

        Class<?>[] parameterTypes = invocationContext.getExecutable().getParameterTypes();
        Class<?>[] translatedParameterTypes = new Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            translatedParameterTypes[i] = cl.loadClass(parameterTypes[i].getName());
        }

        Object[] arguments = new Object[translatedParameterTypes.length];
        for (int i = 0; i < translatedParameterTypes.length; i++) {
            arguments[i] = findTestInstanceOnStack(extensionContext, translatedParameterTypes[i]);
        }

        Class<?> clazz = cl.loadClass(invocationContext.getTargetClass().getName());
        Constructor<?> ctor = clazz.getDeclaredConstructor(translatedParameterTypes);
        ctor.setAccessible(true);
        Object testInstance = ctor.newInstance(arguments);
        pushTestInstance(extensionContext, testInstance);
        return invocation.proceed();
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {

        if (invocationContext.getExecutable().getParameterCount() != 0) {
            throw new UnsupportedOperationException("@BeforeEach method must have no parameter");
        }

        Object testInstance = topTestInstanceOnStack(extensionContext);
        Class<?> clazz = testInstance.getClass();
        Method method = findZeroParamMethod(clazz, invocationContext.getExecutable().getName());
        method.setAccessible(true);
        method.invoke(testInstance);
        invocation.skip();
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {

        if (invocationContext.getExecutable().getParameterCount() != 0) {
            throw new UnsupportedOperationException("@Test method must have no parameter");
        }

        Object testInstance = topTestInstanceOnStack(extensionContext);
        Class<?> clazz = testInstance.getClass();
        Method method = findZeroParamMethod(clazz, invocationContext.getExecutable().getName());
        method.setAccessible(true);
        method.invoke(testInstance);
        invocation.skip();
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void interceptDynamicTest(Invocation<Void> invocation, DynamicTestInvocationContext invocationContext,
            ExtensionContext extensionContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {

        if (invocationContext.getExecutable().getParameterCount() != 0) {
            throw new UnsupportedOperationException("@AfterEach method must have no parameter");
        }

        Object testInstance = topTestInstanceOnStack(extensionContext);
        Class<?> clazz = testInstance.getClass();
        Method method = findZeroParamMethod(clazz, invocationContext.getExecutable().getName());
        method.setAccessible(true);
        method.invoke(testInstance);
        invocation.skip();
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {

        if (invocationContext.getExecutable().getParameterCount() != 0) {
            throw new UnsupportedOperationException("@AfterAll method must have no parameter");
        }

        ArcTestClassLoader cl = ArcTestClassLoader.inTCCL();
        Class<?> clazz = cl.loadClass(invocationContext.getTargetClass().getName());
        Method method = findZeroParamMethod(clazz, invocationContext.getExecutable().getName());
        method.setAccessible(true);
        method.invoke(null);
        invocation.skip();
    }

    private static Method findZeroParamMethod(Class<?> clazz, String name) throws NoSuchMethodException {
        if (clazz == null) {
            throw new NoSuchMethodException(name);
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (name.equals(method.getName()) && method.getParameterCount() == 0) {
                return method;
            }
        }
        return findZeroParamMethod(clazz.getSuperclass(), name);
    }

    private static void initTestInstanceStack(ExtensionContext context) {
        context.getRoot().getStore(NAMESPACE).put(KEY_TEST_INSTANCES, new ArrayDeque<>());
    }

    private static void pushTestInstance(ExtensionContext context, Object testInstance) {
        Deque<Object> stack = context.getRoot().getStore(NAMESPACE).get(KEY_TEST_INSTANCES, Deque.class);
        stack.push(testInstance);
    }

    private static void popTestInstance(ExtensionContext context) {
        Deque<Object> stack = context.getRoot().getStore(NAMESPACE).get(KEY_TEST_INSTANCES, Deque.class);
        stack.pop();
    }

    private static Object topTestInstanceOnStack(ExtensionContext context) {
        Deque<Object> stack = context.getRoot().getStore(NAMESPACE).get(KEY_TEST_INSTANCES, Deque.class);
        return stack.peek();
    }

    private static Object findTestInstanceOnStack(ExtensionContext context, Class<?> clazz) {
        Deque<Object> stack = context.getRoot().getStore(NAMESPACE).get(KEY_TEST_INSTANCES, Deque.class);
        for (Object obj : stack) {
            if (clazz.equals(obj.getClass())) {
                return obj;
            }
        }
        return null;
    }

    private static void destroyTestInstanceStack(ExtensionContext context) {
        context.getRoot().getStore(NAMESPACE).remove(KEY_TEST_INSTANCES);
    }

    /**
     * In case the test is expected to fail, this method will return a {@link Throwable} that caused it.
     */
    public Throwable getFailure() {
        return buildFailure.get();
    }

    void setFailure(Throwable failure) {
        buildFailure.set(failure);
    }

    private ArcTestClassLoader init(ExtensionContext context) {
        // retrieve test class from extension context
        Class<?> testClass = context.getRequiredTestClass();

        // Build index
        IndexView immutableBeanArchiveIndex;
        try {
            immutableBeanArchiveIndex = BeanArchives.buildImmutableBeanArchiveIndex(index(beanClasses));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create index", e);
        }

        IndexView applicationIndex;
        if (additionalClasses.isEmpty()) {
            applicationIndex = null;
        } else {
            try {
                applicationIndex = index(additionalClasses);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create index", e);
            }
        }

        ExtensionsEntryPoint buildCompatibleExtensions = new ExtensionsEntryPoint(this.buildCompatibleExtensions);

        {
            IndexView overallIndex = applicationIndex != null
                    ? CompositeIndex.create(immutableBeanArchiveIndex, applicationIndex)
                    : immutableBeanArchiveIndex;
            Set<String> additionalClasses = new HashSet<>();
            buildCompatibleExtensions.runDiscovery(overallIndex, additionalClasses);
            Index additionalIndex = null;
            try {
                Set<Class<?>> additionalClassObjects = new HashSet<>();
                for (String additionalClass : additionalClasses) {
                    additionalClassObjects.add(ArcTestContainer.class.getClassLoader().loadClass(additionalClass));
                }
                additionalIndex = index(additionalClassObjects);
            } catch (IOException | ClassNotFoundException e) {
                throw new IllegalStateException("Failed to create index", e);
            }
            immutableBeanArchiveIndex = CompositeIndex.create(immutableBeanArchiveIndex, additionalIndex);
        }

        ClassLoader old = Thread.currentThread().getContextClassLoader();

        try {
            String arcContainerAbsolutePath = ArcTestContainer.class.getClassLoader()
                    .getResource(ArcTestContainer.class.getName().replace(".", "/") + ".class").getFile();
            int targetClassesIndex = arcContainerAbsolutePath.indexOf(TARGET_TEST_CLASSES);
            String testClassesRootPath = arcContainerAbsolutePath.substring(0, targetClassesIndex);
            File generatedSourcesDirectory = new File("target/generated-arc-sources");
            File testOutputDirectory = new File(testClassesRootPath + TARGET_TEST_CLASSES);
            File componentsProviderFile = new File(generatedSourcesDirectory + "/" + nameToPath(testClass.getPackage()
                    .getName()), ComponentsProvider.class.getSimpleName());

            File resourceReferenceProviderFile = new File(generatedSourcesDirectory + "/" + nameToPath(testClass.getPackage()
                    .getName()), ResourceReferenceProvider.class.getSimpleName());

            if (!resourceReferenceProviders.isEmpty()) {
                try {
                    resourceReferenceProviderFile.getParentFile()
                            .mkdirs();
                    Files.write(resourceReferenceProviderFile.toPath(), resourceReferenceProviders.stream()
                            .map(c -> c.getName())
                            .collect(Collectors.toList()));
                } catch (IOException e) {
                    throw new IllegalStateException("Error generating resource reference providers", e);
                }
            }

            BeanProcessor.Builder builder = BeanProcessor.builder()
                    .setName(testClass.getName().replace('.', '_'))
                    .setImmutableBeanArchiveIndex(immutableBeanArchiveIndex)
                    .setComputingBeanArchiveIndex(BeanArchives.buildComputingBeanArchiveIndex(getClass().getClassLoader(),
                            new ConcurrentHashMap<>(), immutableBeanArchiveIndex))
                    .setApplicationIndex(applicationIndex)
                    .setBuildCompatibleExtensions(buildCompatibleExtensions)
                    .setStrictCompatibility(strictCompatibility);
            if (!resourceAnnotations.isEmpty()) {
                builder.addResourceAnnotations(resourceAnnotations.stream()
                        .map(c -> DotName.createSimple(c.getName()))
                        .collect(Collectors.toList()));
            }
            beanRegistrars.forEach(builder::addBeanRegistrar);
            observerRegistrars.forEach(builder::addObserverRegistrar);
            contextRegistrars.forEach(builder::addContextRegistrar);
            qualifierRegistrars.forEach(builder::addQualifierRegistrar);
            interceptorBindingRegistrars.forEach(builder::addInterceptorBindingRegistrar);
            stereotypeRegistrars.forEach(builder::addStereotypeRegistrar);
            annotationsTransformers.forEach(builder::addAnnotationTransformer);
            injectionPointsTransformers.forEach(builder::addInjectionPointTransformer);
            observerTransformers.forEach(builder::addObserverTransformer);
            beanDeploymentValidators.forEach(builder::addBeanDeploymentValidator);
            builder.setOutput(new ResourceOutput() {

                @Override
                public void writeResource(Resource resource) throws IOException {
                    switch (resource.getType()) {
                        case JAVA_CLASS:
                            resource.writeTo(testOutputDirectory);
                            break;
                        case SERVICE_PROVIDER:
                            if (resource.getName()
                                    .endsWith(ComponentsProvider.class.getName())) {
                                componentsProviderFile.getParentFile()
                                        .mkdirs();
                                try (FileOutputStream out = new FileOutputStream(componentsProviderFile)) {
                                    out.write(resource.getData());
                                }
                            }
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                }
            });
            builder.setRemoveUnusedBeans(removeUnusedBeans);
            for (Predicate<BeanInfo> exclusion : exclusions) {
                builder.addRemovalExclusion(exclusion);
            }
            builder.setAlternativePriorities(alternativePriorities);
            builder.setTransformUnproxyableClasses(transformUnproxyableClasses);

            BeanProcessor beanProcessor = builder.build();

            List<BytecodeTransformer> bytecodeTransformers = new ArrayList<>();
            try {
                beanProcessor.process(bytecodeTransformers::add);
            } catch (IOException e) {
                throw new IllegalStateException("Error generating resources", e);
            }

            Map<String, byte[]> transformedClasses = new HashMap<>();
            Path transformedClassesDirectory = new File(
                    testClassesRootPath + "target/transformed-classes/" + beanProcessor.getName()).toPath();
            Files.createDirectories(transformedClassesDirectory);
            if (!bytecodeTransformers.isEmpty()) {
                Map<String, List<BiFunction<String, ClassVisitor, ClassVisitor>>> map = bytecodeTransformers.stream()
                        .collect(Collectors.groupingBy(BytecodeTransformer::getClassToTransform,
                                Collectors.mapping(BytecodeTransformer::getVisitorFunction, Collectors.toList())));

                for (Map.Entry<String, List<BiFunction<String, ClassVisitor, ClassVisitor>>> entry : map.entrySet()) {
                    String className = entry.getKey();
                    List<BiFunction<String, ClassVisitor, ClassVisitor>> transformations = entry.getValue();

                    String classFileName = className.replace('.', '/') + ".class";
                    byte[] bytecode;
                    try (InputStream in = old.getResourceAsStream(classFileName)) {
                        if (in == null) {
                            throw new IOException("Resource not found: " + classFileName);
                        }
                        bytecode = in.readAllBytes();
                    }
                    ClassReader reader = new ClassReader(bytecode);
                    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                    ClassVisitor visitor = writer;
                    for (BiFunction<String, ClassVisitor, ClassVisitor> transformation : transformations) {
                        visitor = transformation.apply(className, visitor);
                    }
                    reader.accept(visitor, 0);
                    bytecode = writer.toByteArray();
                    transformedClasses.put(className, bytecode);

                    // these files are not used, we only create them for debugging purposes
                    // the `/` and `$` chars in the path/name are replaced with `_` so that
                    // IntelliJ doesn't treat the files as duplicates of classes it already knows
                    Path classFile = transformedClassesDirectory.resolve(
                            classFileName.replace('/', '_').replace('$', '_'));
                    Files.write(classFile, bytecode);
                }
            }

            return new ArcTestClassLoader(old, transformedClasses, componentsProviderFile,
                    resourceReferenceProviders.isEmpty() ? null : resourceReferenceProviderFile);
        } catch (Throwable e) {
            if (shouldFail) {
                context.getRoot().getStore(NAMESPACE).put(KEY_FAILURE, e);
            } else {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
        return new ArcTestClassLoader(old, null, null, null);
    }

    private Index index(Iterable<Class<?>> classes) throws IOException {
        Indexer indexer = new Indexer();
        Set<String> packages = new HashSet<>();
        for (Class<?> clazz : classes) {
            packages.add(clazz.getPackageName());
            try (InputStream stream = ArcTestContainer.class.getClassLoader()
                    .getResourceAsStream(clazz.getName().replace('.', '/') + ".class")) {
                indexer.index(stream);
            }
        }
        for (String pkg : packages) {
            try (InputStream stream = ArcTestContainer.class.getClassLoader()
                    .getResourceAsStream(pkg.replace('.', '/') + "/package-info.class")) {
                if (stream != null) {
                    indexer.index(stream);
                }
            }
        }
        return indexer.complete();
    }

    private String nameToPath(String packName) {
        return packName.replace('.', '/');
    }

}
