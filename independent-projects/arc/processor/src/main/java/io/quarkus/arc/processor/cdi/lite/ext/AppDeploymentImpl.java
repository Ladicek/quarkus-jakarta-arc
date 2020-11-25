package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.AppDeployment;
import cdi.lite.extension.beans.BeanInfo;
import cdi.lite.extension.beans.ObserverInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.Type;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

class AppDeploymentImpl implements AppDeployment {
    private final IndexView beanArchiveIndex;
    private final AllAnnotationOverlays annotationOverlays;
    private final Collection<io.quarkus.arc.processor.BeanInfo> beans;
    private final Collection<io.quarkus.arc.processor.ObserverInfo> observers;

    AppDeploymentImpl(IndexView beanArchiveIndex, AllAnnotationOverlays annotationOverlays,
            Collection<io.quarkus.arc.processor.BeanInfo> beans, Collection<io.quarkus.arc.processor.ObserverInfo> observers) {
        this.beanArchiveIndex = beanArchiveIndex;
        this.annotationOverlays = annotationOverlays;
        this.beans = beans;
        this.observers = observers;
    }

    @Override
    public BeanQuery beans() {
        return new BeanQueryImpl();
    }

    @Override
    public ObserverQuery observers() {
        return new ObserverQueryImpl();
    }

    class BeanQueryImpl implements BeanQuery {
        private Set<DotName> requiredScopeAnnotations;
        private Set<DotName> requiredBeanTypes;
        private Set<DotName> requiredQualifiers;
        private Set<DotName> requiredDeclaringClasses;

        @Override
        public BeanQuery scope(Class<? extends Annotation> scopeAnnotation) {
            if (requiredScopeAnnotations == null) {
                requiredScopeAnnotations = new HashSet<>();
            }

            requiredScopeAnnotations.add(DotName.createSimple(scopeAnnotation.getName()));
            return this;
        }

        @Override
        public BeanQuery scope(ClassInfo<?> scopeAnnotation) {
            if (requiredScopeAnnotations == null) {
                requiredScopeAnnotations = new HashSet<>();
            }

            requiredScopeAnnotations.add(((ClassInfoImpl) scopeAnnotation).jandexDeclaration.name());
            return this;
        }

        @Override
        public BeanQuery type(Class<?> beanType) {
            if (requiredBeanTypes == null) {
                requiredBeanTypes = new HashSet<>();
            }

            requiredBeanTypes.add(DotName.createSimple(beanType.getName()));
            return this;
        }

        @Override
        public BeanQuery type(ClassInfo<?> beanType) {
            if (requiredBeanTypes == null) {
                requiredBeanTypes = new HashSet<>();
            }

            requiredBeanTypes.add(((ClassInfoImpl) beanType).jandexDeclaration.name());
            return this;
        }

        @Override
        public BeanQuery type(Type beanType) {
            if (requiredBeanTypes == null) {
                requiredBeanTypes = new HashSet<>();
            }

            requiredBeanTypes.add(((TypeImpl<?>) beanType).jandexType.name());
            return this;
        }

        @Override
        public BeanQuery qualifier(Class<? extends Annotation> qualifierAnnotation) {
            if (requiredQualifiers == null) {
                requiredQualifiers = new HashSet<>();
            }

            requiredQualifiers.add(DotName.createSimple(qualifierAnnotation.getName()));
            return this;
        }

        @Override
        public BeanQuery qualifier(ClassInfo<?> qualifierAnnotation) {
            if (requiredQualifiers == null) {
                requiredQualifiers = new HashSet<>();
            }

            requiredQualifiers.add(((ClassInfoImpl) qualifierAnnotation).jandexDeclaration.name());
            return this;
        }

        @Override
        public BeanQuery declaringClass(Class<?> declaringClass) {
            if (requiredDeclaringClasses == null) {
                requiredDeclaringClasses = new HashSet<>();
            }

            requiredDeclaringClasses.add(DotName.createSimple(declaringClass.getName()));
            return this;
        }

        @Override
        public BeanQuery declaringClass(ClassInfo<?> declaringClass) {
            if (requiredDeclaringClasses == null) {
                requiredDeclaringClasses = new HashSet<>();
            }

            requiredDeclaringClasses.add(((ClassInfoImpl) declaringClass).jandexDeclaration.name());
            return this;
        }

        @Override
        public void forEach(Consumer<BeanInfo<?>> consumer) {
            stream().forEach(consumer);
        }

        @Override
        public void ifNone(Runnable runnable) {
            if (stream().count() == 0) {
                runnable.run();
            }
        }

        Stream<BeanInfo<?>> stream() {
            Stream<io.quarkus.arc.processor.BeanInfo> result = beans.stream();

            if (requiredScopeAnnotations != null) {
                result = result.filter(bean -> requiredScopeAnnotations.contains(bean.getScope().getDotName()));
            }

            if (requiredBeanTypes != null) {
                result = result.filter(bean -> bean.getTypes()
                        .stream()
                        .map(org.jboss.jandex.Type::name)
                        .anyMatch(typeName -> requiredBeanTypes.contains(typeName)));
            }

            if (requiredQualifiers != null) {
                result = result.filter(bean -> bean.getQualifiers()
                        .stream()
                        .map(org.jboss.jandex.AnnotationInstance::name)
                        .anyMatch(qualifierName -> requiredQualifiers.contains(qualifierName)));
            }

            if (requiredDeclaringClasses != null) {
                result = result.filter(bean -> requiredDeclaringClasses.contains(bean.getBeanClass()));
            }

            return result.map(it -> new BeanInfoImpl(beanArchiveIndex, annotationOverlays, it));
        }
    }

    class ObserverQueryImpl implements ObserverQuery {
        private Set<DotName> requiredObservedTypes;
        private Set<DotName> requiredQualifiers;
        private Set<DotName> requiredDeclaringClasses;

        @Override
        public ObserverQuery observedType(Class<?> observedType) {
            if (requiredObservedTypes == null) {
                requiredObservedTypes = new HashSet<>();
            }

            requiredObservedTypes.add(DotName.createSimple(observedType.getName()));
            return this;
        }

        @Override
        public ObserverQuery observedType(ClassInfo<?> observedType) {
            if (requiredObservedTypes == null) {
                requiredObservedTypes = new HashSet<>();
            }

            requiredObservedTypes.add(((ClassInfoImpl) observedType).jandexDeclaration.name());
            return this;
        }

        @Override
        public ObserverQuery observedType(Type observedType) {
            if (requiredObservedTypes == null) {
                requiredObservedTypes = new HashSet<>();
            }

            requiredObservedTypes.add(((TypeImpl<?>) observedType).jandexType.name());
            return this;
        }

        @Override
        public ObserverQuery qualifier(Class<? extends Annotation> qualifierAnnotation) {
            if (requiredQualifiers == null) {
                requiredQualifiers = new HashSet<>();
            }

            requiredQualifiers.add(DotName.createSimple(qualifierAnnotation.getName()));
            return this;
        }

        @Override
        public ObserverQuery qualifier(ClassInfo<?> qualifierAnnotation) {
            if (requiredQualifiers == null) {
                requiredQualifiers = new HashSet<>();
            }

            requiredQualifiers.add(((ClassInfoImpl) qualifierAnnotation).jandexDeclaration.name());
            return this;
        }

        @Override
        public ObserverQuery declaringClass(Class<?> declaringClass) {
            if (requiredDeclaringClasses == null) {
                requiredDeclaringClasses = new HashSet<>();
            }

            requiredDeclaringClasses.add(DotName.createSimple(declaringClass.getName()));
            return this;
        }

        @Override
        public ObserverQuery declaringClass(ClassInfo<?> declaringClass) {
            if (requiredDeclaringClasses == null) {
                requiredDeclaringClasses = new HashSet<>();
            }

            requiredDeclaringClasses.add(((ClassInfoImpl) declaringClass).jandexDeclaration.name());
            return this;
        }

        @Override
        public void forEach(Consumer<ObserverInfo<?>> consumer) {
            stream().forEach(consumer);
        }

        @Override
        public void ifNone(Runnable runnable) {
            if (stream().count() == 0) {
                runnable.run();
            }
        }

        Stream<ObserverInfo<?>> stream() {
            Stream<io.quarkus.arc.processor.ObserverInfo> result = observers.stream();

            if (requiredObservedTypes != null) {
                result = result.filter(observer -> requiredObservedTypes.contains(observer.getObservedType().name()));
            }

            if (requiredQualifiers != null) {
                result = result.filter(observer -> observer.getQualifiers()
                        .stream()
                        .map(org.jboss.jandex.AnnotationInstance::name)
                        .anyMatch(qualifierName -> requiredQualifiers.contains(qualifierName)));
            }

            if (requiredDeclaringClasses != null) {
                result = result.filter(observer -> requiredDeclaringClasses.contains(observer.getBeanClass()));
            }

            return result.map(it -> new ObserverInfoImpl(beanArchiveIndex, annotationOverlays, it));
        }
    }
}
