package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.World;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.types.Type;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jboss.jandex.DotName;

class WorldImpl implements World {
    private final org.jboss.jandex.IndexView jandexIndex;

    WorldImpl(org.jboss.jandex.IndexView jandexIndex) {
        this.jandexIndex = jandexIndex;
    }

    @Override
    public ClassQuery classes() {
        return new ClassQueryImpl();
    }

    @Override
    public MethodQuery constructors() {
        return new MethodQueryImpl(true);
    }

    @Override
    public MethodQuery methods() {
        return new MethodQueryImpl(false);
    }

    @Override
    public FieldQuery fields() {
        return new FieldQueryImpl();
    }

    private class ClassQueryImpl implements ClassQuery {
        private Set<DotName> requiredJandexClasses;
        private Set<DotName> requiredJandexAnnotations;

        @Override
        public ClassQuery exactly(Class<?> clazz) {
            if (requiredJandexClasses == null) {
                requiredJandexClasses = new HashSet<>();
            }

            requiredJandexClasses.add(DotName.createSimple(clazz.getName()));

            return this;
        }

        @Override
        public ClassQuery exactly(ClassInfo<?> clazz) {
            if (requiredJandexClasses == null) {
                requiredJandexClasses = new HashSet<>();
            }

            requiredJandexClasses.add(((ClassInfoImpl) clazz).jandexDeclaration.name());

            return this;
        }

        @Override
        public ClassQuery subtypeOf(Class<?> clazz) {
            if (requiredJandexClasses == null) {
                requiredJandexClasses = new HashSet<>();
            }

            // TODO getAllKnown* is not reflexive
            if (clazz.isInterface()) {
                jandexIndex.getAllKnownImplementors(DotName.createSimple(clazz.getName()))
                        .stream()
                        .map(org.jboss.jandex.ClassInfo::name)
                        .forEach(requiredJandexClasses::add);
            } else {
                jandexIndex.getAllKnownSubclasses(DotName.createSimple(clazz.getName()))
                        .stream()
                        .map(org.jboss.jandex.ClassInfo::name)
                        .forEach(requiredJandexClasses::add);
            }

            return this;
        }

        @Override
        public ClassQuery subtypeOf(ClassInfo<?> clazz) {
            if (requiredJandexClasses == null) {
                requiredJandexClasses = new HashSet<>();
            }

            // TODO getAllKnown* is not reflexive
            if (clazz.isInterface()) {
                jandexIndex.getAllKnownImplementors(((ClassInfoImpl) clazz).jandexDeclaration.name())
                        .stream()
                        .map(org.jboss.jandex.ClassInfo::name)
                        .forEach(requiredJandexClasses::add);
            } else {
                jandexIndex.getAllKnownSubclasses(((ClassInfoImpl) clazz).jandexDeclaration.name())
                        .stream()
                        .map(org.jboss.jandex.ClassInfo::name)
                        .forEach(requiredJandexClasses::add);
            }

            return this;
        }

        @Override
        public ClassQuery supertypeOf(Class<?> clazz) {
            if (requiredJandexClasses == null) {
                requiredJandexClasses = new HashSet<>();
            }

            DotName name = DotName.createSimple(clazz.getName());
            addSuperClassesToRequiredClassesSet(name);

            return this;
        }

        @Override
        public ClassQuery supertypeOf(ClassInfo<?> clazz) {
            if (requiredJandexClasses == null) {
                requiredJandexClasses = new HashSet<>();
            }

            DotName name = ((ClassInfoImpl) clazz).jandexDeclaration.name();
            addSuperClassesToRequiredClassesSet(name);

            return this;
        }

        private void addSuperClassesToRequiredClassesSet(DotName name) {
            while (name != null) {
                org.jboss.jandex.ClassInfo jandexClass = jandexIndex.getClassByName(name);
                if (jandexClass != null) {
                    requiredJandexClasses.add(jandexClass.name());
                    name = jandexClass.superName();
                } else {
                    // should report an error here
                    name = null;
                }
            }
        }

        @Override
        public ClassQuery annotatedWith(Class<? extends Annotation> annotationType) {
            if (requiredJandexAnnotations == null) {
                requiredJandexAnnotations = new HashSet<>();
            }

            requiredJandexAnnotations.add(DotName.createSimple(annotationType.getName()));

            return this;
        }

        @Override
        public ClassQuery annotatedWith(ClassInfo<?> annotationType) {
            if (requiredJandexAnnotations == null) {
                requiredJandexAnnotations = new HashSet<>();
            }

            requiredJandexAnnotations.add(((ClassInfoImpl) annotationType).jandexDeclaration.name());

            return this;
        }

        @Override
        public Collection<ClassInfo<?>> find() {
            return stream().collect(Collectors.toList());
        }

        @Override
        public Stream<ClassInfo<?>> stream() {
            if (requiredJandexClasses != null && requiredJandexAnnotations != null) {
                return requiredJandexClasses.stream()
                        .map(jandexIndex::getClassByName)
                        .filter(jandexClass -> {
                            for (DotName requiredJandexAnnotation : requiredJandexAnnotations) {
                                if (jandexClass.classAnnotation(requiredJandexAnnotation) != null) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .map(it -> new ClassInfoImpl(jandexIndex, it));
            } else if (requiredJandexClasses != null) {
                return requiredJandexClasses.stream()
                        .map(jandexIndex::getClassByName)
                        .map(it -> new ClassInfoImpl(jandexIndex, it));
            } else if (requiredJandexAnnotations != null) {
                Stream<ClassInfo<?>> result = null;
                for (DotName requiredJandexAnnotation : requiredJandexAnnotations) {
                    Stream<ClassInfo<?>> partialResult = jandexIndex.getAnnotations(requiredJandexAnnotation)
                            .stream()
                            .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.CLASS)
                            .map(it -> it.target().asClass())
                            .map(it -> new ClassInfoImpl(jandexIndex, it));
                    if (result == null) {
                        result = partialResult;
                    } else {
                        result = Stream.concat(result, partialResult);
                    }
                }
                return result == null ? Stream.empty() : result.distinct();
            } else {
                return jandexIndex.getKnownClasses()
                        .stream()
                        .map(it -> new ClassInfoImpl(jandexIndex, it));
            }
        }
    }

    private class MethodQueryImpl implements MethodQuery {
        private final Predicate<String> nameFilter;
        private Stream<ClassInfo<?>> requiredDeclarationSites; // elements not guaranteed to be distinct!
        private Set<org.jboss.jandex.Type> requiredJandexReturnTypes;
        private Set<DotName> requiredJandexAnnotations;

        MethodQueryImpl(boolean constructors) {
            this.nameFilter = constructors ? MethodPredicates.IS_CONSTRUCTOR : MethodPredicates.IS_METHOD;
        }

        @Override
        public MethodQuery declaredOn(ClassQuery classes) {
            if (requiredDeclarationSites == null) {
                requiredDeclarationSites = classes.stream();
            } else {
                requiredDeclarationSites = Stream.concat(requiredDeclarationSites, classes.stream());
            }

            return this;
        }

        @Override
        public MethodQuery withReturnType(Type type) {
            if (requiredJandexReturnTypes == null) {
                requiredJandexReturnTypes = new HashSet<>();
            }

            requiredJandexReturnTypes.add(((TypeImpl<?>) type).jandexType);

            return this;
        }

        @Override
        public MethodQuery annotatedWith(Class<? extends Annotation> annotationType) {
            if (requiredJandexAnnotations == null) {
                requiredJandexAnnotations = new HashSet<>();
            }

            requiredJandexAnnotations.add(DotName.createSimple(annotationType.getName()));

            return this;
        }

        @Override
        public MethodQuery annotatedWith(ClassInfo<?> annotationType) {
            if (requiredJandexAnnotations == null) {
                requiredJandexAnnotations = new HashSet<>();
            }

            requiredJandexAnnotations.add(((ClassInfoImpl) annotationType).jandexDeclaration.name());

            return this;
        }

        @Override
        public Collection<MethodInfo<?>> find() {
            return stream().collect(Collectors.toList());
        }

        @Override
        public Stream<MethodInfo<?>> stream() {
            if (requiredDeclarationSites != null && requiredJandexReturnTypes != null && requiredJandexAnnotations != null) {
                return requiredDeclarationSites
                        .flatMap(it -> it.methods().stream())
                        .filter(it -> nameFilter.test(it.name()))
                        .filter(it -> requiredJandexReturnTypes.contains(((MethodInfoImpl) it).jandexDeclaration.returnType()))
                        .filter(it -> {
                            for (DotName requiredJandexAnnotation : requiredJandexAnnotations) {
                                if (((MethodInfoImpl) it).jandexDeclaration.hasAnnotation(requiredJandexAnnotation)) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .distinct()
                        .map(Function.identity());
            } else if (requiredDeclarationSites != null && requiredJandexReturnTypes != null) {
                return requiredDeclarationSites
                        .flatMap(it -> it.methods().stream())
                        .filter(it -> nameFilter.test(it.name()))
                        .filter(it -> requiredJandexReturnTypes.contains(((MethodInfoImpl) it).jandexDeclaration.returnType()))
                        .distinct()
                        .map(Function.identity());
            } else if (requiredDeclarationSites != null && requiredJandexAnnotations != null) {
                return requiredDeclarationSites
                        .flatMap(it -> it.methods().stream())
                        .filter(it -> nameFilter.test(it.name()))
                        .filter(it -> {
                            for (DotName requiredJandexAnnotation : requiredJandexAnnotations) {
                                if (((FieldInfoImpl) it).jandexDeclaration.hasAnnotation(requiredJandexAnnotation)) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .distinct()
                        .map(Function.identity());
            } else if (requiredDeclarationSites != null) {
                return requiredDeclarationSites
                        .flatMap(it -> it.methods().stream())
                        .filter(it -> nameFilter.test(it.name()))
                        .distinct()
                        .map(Function.identity());
            } else if (requiredJandexReturnTypes != null && requiredJandexAnnotations != null) {
                return jandexIndex.getKnownClasses()
                        .stream()
                        .flatMap(it -> it.methods().stream())
                        .filter(it -> nameFilter.test(it.name()))
                        .filter(it -> requiredJandexReturnTypes.contains(it.returnType()))
                        .filter(it -> {
                            for (DotName requiredJandexAnnotation : requiredJandexAnnotations) {
                                if (it.hasAnnotation(requiredJandexAnnotation)) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .map(it -> new MethodInfoImpl(jandexIndex, it));
            } else if (requiredJandexReturnTypes != null) {
                return jandexIndex.getKnownClasses()
                        .stream()
                        .flatMap(it -> it.methods().stream())
                        .filter(it -> nameFilter.test(it.name()))
                        .filter(it -> requiredJandexReturnTypes.contains(it.returnType()))
                        .map(it -> new MethodInfoImpl(jandexIndex, it));
            } else if (requiredJandexAnnotations != null) {
                Stream<MethodInfo<?>> result = null;
                for (DotName requiredJandexAnnotation : requiredJandexAnnotations) {
                    Stream<MethodInfo<?>> partialResult = jandexIndex.getAnnotations(requiredJandexAnnotation)
                            .stream()
                            .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD)
                            .map(it -> it.target().asMethod())
                            .filter(it -> nameFilter.test(it.name()))
                            .map(it -> new MethodInfoImpl(jandexIndex, it));
                    if (result == null) {
                        result = partialResult;
                    } else {
                        result = Stream.concat(result, partialResult);
                    }
                }
                return result == null ? Stream.empty() : result.distinct();
            } else {
                return jandexIndex.getKnownClasses()
                        .stream()
                        .flatMap(it -> it.methods().stream())
                        .filter(it -> nameFilter.test(it.name()))
                        .map(it -> new MethodInfoImpl(jandexIndex, it));
            }
        }
    }

    private class FieldQueryImpl implements FieldQuery {
        private Stream<ClassInfo<?>> requiredDeclarationSites; // elements not guaranteed to be distinct!
        private Set<org.jboss.jandex.Type> requiredJandexTypes;
        private Set<DotName> requiredJandexAnnotations;

        @Override
        public FieldQuery declaredOn(ClassQuery classes) {
            if (requiredDeclarationSites == null) {
                requiredDeclarationSites = classes.stream();
            } else {
                requiredDeclarationSites = Stream.concat(requiredDeclarationSites, classes.stream());
            }

            return this;
        }

        @Override
        public FieldQuery ofType(Type type) {
            if (requiredJandexTypes == null) {
                requiredJandexTypes = new HashSet<>();
            }

            requiredJandexTypes.add(((TypeImpl<?>) type).jandexType);

            return this;
        }

        @Override
        public FieldQuery annotatedWith(Class<? extends Annotation> annotationType) {
            if (requiredJandexAnnotations == null) {
                requiredJandexAnnotations = new HashSet<>();
            }

            requiredJandexAnnotations.add(DotName.createSimple(annotationType.getName()));

            return this;
        }

        @Override
        public FieldQuery annotatedWith(ClassInfo<?> annotationType) {
            if (requiredJandexAnnotations == null) {
                requiredJandexAnnotations = new HashSet<>();
            }

            requiredJandexAnnotations.add(((ClassInfoImpl) annotationType).jandexDeclaration.name());

            return this;
        }

        @Override
        public Collection<FieldInfo<?>> find() {
            return stream().collect(Collectors.toList());
        }

        @Override
        public Stream<FieldInfo<?>> stream() {
            if (requiredDeclarationSites != null && requiredJandexTypes != null && requiredJandexAnnotations != null) {
                return requiredDeclarationSites
                        .flatMap(it -> it.fields().stream())
                        .filter(it -> requiredJandexTypes.contains(((FieldInfoImpl) it).jandexDeclaration.type()))
                        .filter(it -> {
                            for (DotName requiredJandexAnnotation : requiredJandexAnnotations) {
                                if (((FieldInfoImpl) it).jandexDeclaration.hasAnnotation(requiredJandexAnnotation)) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .distinct()
                        .map(Function.identity());
            } else if (requiredDeclarationSites != null && requiredJandexTypes != null) {
                return requiredDeclarationSites
                        .flatMap(it -> it.fields().stream())
                        .filter(it -> requiredJandexTypes.contains(((FieldInfoImpl) it).jandexDeclaration.type()))
                        .distinct()
                        .map(Function.identity());
            } else if (requiredDeclarationSites != null && requiredJandexAnnotations != null) {
                return requiredDeclarationSites
                        .flatMap(it -> it.fields().stream())
                        .filter(it -> {
                            for (DotName requiredJandexAnnotation : requiredJandexAnnotations) {
                                if (((FieldInfoImpl) it).jandexDeclaration.hasAnnotation(requiredJandexAnnotation)) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .distinct()
                        .map(Function.identity());
            } else if (requiredDeclarationSites != null) {
                return requiredDeclarationSites
                        .flatMap(it -> it.fields().stream())
                        .distinct()
                        .map(Function.identity());
            } else if (requiredJandexTypes != null && requiredJandexAnnotations != null) {
                return jandexIndex.getKnownClasses()
                        .stream()
                        .flatMap(it -> it.fields().stream())
                        .filter(it -> requiredJandexTypes.contains(it.type()))
                        .filter(it -> {
                            for (DotName requiredJandexAnnotation : requiredJandexAnnotations) {
                                if (it.hasAnnotation(requiredJandexAnnotation)) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .map(it -> new FieldInfoImpl(jandexIndex, it));
            } else if (requiredJandexTypes != null) {
                return jandexIndex.getKnownClasses()
                        .stream()
                        .flatMap(it -> it.fields().stream())
                        .filter(it -> requiredJandexTypes.contains(it.type()))
                        .map(it -> new FieldInfoImpl(jandexIndex, it));
            } else if (requiredJandexAnnotations != null) {
                Stream<FieldInfo<?>> result = null;
                for (DotName requiredJandexAnnotation : requiredJandexAnnotations) {
                    Stream<FieldInfo<?>> partialResult = jandexIndex.getAnnotations(requiredJandexAnnotation)
                            .stream()
                            .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.FIELD)
                            .map(it -> it.target().asField())
                            .map(it -> new FieldInfoImpl(jandexIndex, it));
                    if (result == null) {
                        result = partialResult;
                    } else {
                        result = Stream.concat(result, partialResult);
                    }
                }
                return result == null ? Stream.empty() : result.distinct();
            } else {
                return jandexIndex.getKnownClasses()
                        .stream()
                        .flatMap(it -> it.fields().stream())
                        .map(it -> new FieldInfoImpl(jandexIndex, it));
            }
        }
    }
}
