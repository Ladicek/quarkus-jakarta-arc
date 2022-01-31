package io.quarkus.arc.processor;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

import io.quarkus.arc.impl.ComputingCache;
import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.FieldCreator;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import jakarta.enterprise.util.AnnotationLiteral;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

/**
 * Generates annotation literal classes that can be used to represent annotation instances at runtime.
 * The classes may either be shared (one class for each annotation type, constructor accepts values
 * of all annotation members) or one-off (one class for each annotation instance, zero-parameter
 * constructor, annotation members are hard-coded to return specific values).
 * <p>
 * This construct is thread-safe.
 */
// TODO rename to AnnotationLiteralGenerator, most likely (or AnnotationLiterals?)
public class AnnotationLiteralProcessor extends AbstractGenerator {
    private static final Logger LOGGER = Logger.getLogger(AnnotationLiteralProcessor.class);

    private static final String ANNOTATION_LITERAL_SUFFIX = "_AnnotationLiteral";
    private static final String SHARED_SUFFIX = "_Shared";

    private final ComputingCache<CacheKey, AnnotationLiteralClassData> cache; // null if sharing is disabled
    private final IndexView beanArchiveIndex;

    AnnotationLiteralProcessor(boolean generateSources, boolean shared, IndexView beanArchiveIndex,
            Predicate<DotName> applicationClassPredicate) {
        super(generateSources);

        ComputingCache<CacheKey, AnnotationLiteralClassData> cache = null;
        if (shared) {
            cache = new ComputingCache<>(key -> new AnnotationLiteralClassData(
                    generateSharedName(key.annotationName()),
                    applicationClassPredicate.test(key.annotationName()),
                    key.annotationClass));
        }
        this.cache = cache;
        this.beanArchiveIndex = beanArchiveIndex;
    }

    /**
     * If annotation literal class sharing is enabled, creator of this {@code AnnotationLiteralProcessor}
     * must call this method at an appropriate point in time and write the result to an appropriate output.
     * If not, the bytecode sequences generated using {@link #create(BytecodeCreator, ClassOutput, ClassInfo,
     * AnnotationInstance, String) create()} will refer to non-existing classes.
     *
     * @param existingClasses names of classes that already exist and should not be generated again
     * @return the generated classes, never {@code null}
     */
    Collection<ResourceOutput.Resource> generate(Set<String> existingClasses) {
        if (cache == null || cache.isEmpty()) {
            return Collections.emptyList();
        }

        List<ResourceOutput.Resource> resources = new ArrayList<>();
        cache.forEachExistingValue(literal -> {
            ResourceClassOutput classOutput = new ResourceClassOutput(literal.isApplicationClass, generateSources);
            createSharedAnnotationLiteralClass(classOutput, literal, existingClasses);
            resources.addAll(classOutput.getResources());
        });
        return resources;
    }

    /**
     * Generates a bytecode sequence to create an instance of given annotation type, such that
     * the annotation members have the same values as the given annotation instance.
     * An implementation of the annotation type will be generated automatically, subclassing
     * the {@code AnnotationLiteral} class. Therefore, we also call it the annotation literal class.
     * <p>
     * If annotation literal class sharing is disabled, this method call will generate the class
     * into the {@code classOutput}. If annotation literal class sharing is enabled, the creator
     * of this {@code AnnotationLiteralProcessor} is responsible for generating the classes later
     * (see {@link #generate(Set) generate()}).
     *
     * @param bytecode will receive the bytecode sequence for instantiating the annotation literal class
     *        as a sequence of {@link BytecodeCreator} method calls
     * @param classOutput will receive the annotation literal class bytecode, if sharing is disabled
     * @param annotationClass the annotation type
     * @param annotationInstance the annotation instance; must match the {@code annotationClass}
     * @param targetPackage package in which the annotation literal class will be generated, if sharing is disabled
     * @return an annotation literal instance result handle
     */
    public ResultHandle create(BytecodeCreator bytecode, ClassOutput classOutput, ClassInfo annotationClass,
            AnnotationInstance annotationInstance, String targetPackage) {
        Objects.requireNonNull(annotationClass, "Annotation class not available: " + annotationInstance);
        if (cache != null) {
            AnnotationLiteralClassData literal = cache.getValue(new CacheKey(annotationClass));

            ResultHandle[] constructorParameters = new ResultHandle[literal.annotationMembers().size()];

            int constructorParameterIndex = 0;
            for (MethodInfo annotationMember : literal.annotationMembers()) {
                AnnotationValue value = annotationInstance.value(annotationMember.name());
                if (value == null) {
                    value = annotationMember.defaultValue();
                }
                if (value == null) {
                    throw new IllegalStateException(String.format(
                            "Value is not set for %s.%s(). Most probably an older version of Jandex was used to index an application dependency. Make sure that Jandex 2.1+ is used.",
                            annotationMember.declaringClass().name(), annotationMember.name()));
                }
                ResultHandle retValue = loadValue(bytecode, literal, annotationMember, value, classOutput);
                constructorParameters[constructorParameterIndex] = retValue;

                constructorParameterIndex++;
            }

            return bytecode.newInstance(MethodDescriptor.ofConstructor(literal.generatedClassName,
                    literal.annotationMembers().stream().map(m -> m.returnType().name().toString()).toArray()),
                    constructorParameters);
        } else {
            Objects.requireNonNull(classOutput);
            Objects.requireNonNull(targetPackage);
            String literalClassName = generateOneoffName(targetPackage,
                    DotNames.simpleName(annotationClass),
                    Hashes.sha1(annotationInstance.toString()));
            AnnotationLiteralClassData literal = new AnnotationLiteralClassData(literalClassName, false, annotationClass);
            createOneoffAnnotationLiteralClass(classOutput, literal, annotationInstance);
            return bytecode.newInstance(MethodDescriptor.ofConstructor(literalClassName));
        }
    }

    /**
     * Based on given {@code literal} data, generates an annotation literal class into the given {@code classOutput}.
     * Does nothing if {@code existingClasses} indicates that the class to be generated already exists.
     * <p>
     * The generated annotation literal class is supposed to be shared. That is, it has a constructor
     * that accepts values of all annotation members.
     *
     * @param classOutput the output to which the class is written
     * @param literal data about the annotation literal class to be generated
     * @param existingClasses set of existing classes that shouldn't be generated again
     */
    private void createSharedAnnotationLiteralClass(ClassOutput classOutput, AnnotationLiteralClassData literal,
            Set<String> existingClasses) {
        String generatedName = literal.generatedClassName.replace('.', '/');
        if (existingClasses.contains(generatedName)) {
            return;
        }

        ClassCreator classCreator = ClassCreator.builder()
                .classOutput(classOutput)
                .className(generatedName)
                .superClass(AnnotationLiteral.class)
                .interfaces(literal.annotationName().toString())
                .signature(annotationLiteralClassSignature(literal))
                .build();

        MethodCreator constructor = classCreator.getMethodCreator(Methods.INIT, "V",
                literal.annotationMembers().stream().map(m -> m.returnType().name().toString()).toArray());
        constructor.invokeSpecialMethod(MethodDescriptor.ofConstructor(AnnotationLiteral.class), constructor.getThis());

        int constructorParameterIndex = 0;
        for (MethodInfo annotationMember : literal.annotationMembers()) {
            String type = annotationMember.returnType().name().toString();
            // field
            classCreator.getFieldCreator(annotationMember.name(), type).setModifiers(ACC_PRIVATE | ACC_FINAL);

            // constructor: param -> field
            constructor.writeInstanceField(
                    FieldDescriptor.of(classCreator.getClassName(), annotationMember.name(), type),
                    constructor.getThis(), constructor.getMethodParam(constructorParameterIndex));

            // annotation member method implementation
            MethodCreator value = classCreator.getMethodCreator(annotationMember.name(), type).setModifiers(ACC_PUBLIC);
            value.returnValue(value.readInstanceField(
                    FieldDescriptor.of(classCreator.getClassName(), annotationMember.name(), type), value.getThis()));

            constructorParameterIndex++;
        }
        constructor.returnValue(null);

        generateStaticFieldsWithDefaultValues(classCreator, literal.annotationMembers());

        classCreator.close();
        LOGGER.debugf("Shared annotation literal class generated: %s", literal.generatedClassName);
    }

    /**
     * Based on given {@code literal} data, generates an annotation literal class into the given {@code classOutput}.
     * <p>
     * The generated annotation literal class is supposed to be one-off. That is, it has a zero-parameter constructor
     * and all annotation member methods return annotation values obtained from given {@code annotationInstance}.
     *
     * @param classOutput the output to which the class is written
     * @param literal data about the annotation literal class to be generated
     * @param annotationInstance the annotation instance whose values should the generated class return
     */
    private void createOneoffAnnotationLiteralClass(ClassOutput classOutput, AnnotationLiteralClassData literal,
            AnnotationInstance annotationInstance) {

        String generatedName = literal.generatedClassName.replace('.', '/');

        ClassCreator classCreator = ClassCreator.builder()
                .classOutput(classOutput)
                .className(generatedName)
                .superClass(AnnotationLiteral.class)
                .interfaces(literal.annotationName().toString())
                .signature(annotationLiteralClassSignature(literal))
                .build();

        for (MethodInfo annotationMember : literal.annotationMembers()) {
            MethodCreator valueMethod = classCreator.getMethodCreator(MethodDescriptor.of(annotationMember));
            AnnotationValue value = annotationInstance.value(annotationMember.name());
            if (value == null) {
                value = annotationMember.defaultValue();
            }
            if (value == null) {
                throw new IllegalStateException(String.format(
                        "Value is not set for %s.%s(). Most probably an older version of Jandex was used to index an application dependency. Make sure that Jandex 2.1+ is used.",
                        annotationMember.declaringClass().name(), annotationMember.name()));
            }
            valueMethod.returnValue(loadValue(valueMethod, literal, annotationMember, value, classOutput));
        }

        generateStaticFieldsWithDefaultValues(classCreator, literal.annotationMembers());

        classCreator.close();
        LOGGER.debugf("One-off annotation literal class generated: %s", literal.generatedClassName);
    }

    private static String annotationLiteralClassSignature(AnnotationLiteralClassData literal) {
        // Ljakarta/enterprise/util/AnnotationLiteral<Lcom/foo/MyQualifier;>;Lcom/foo/MyQualifier;
        return String.format("Ljakarta/enterprise/util/AnnotationLiteral<L%1$s;>;L%1$s;",
                literal.annotationClass.name().toString().replace('.', '/'));
    }

    private static boolean returnsClassOrClassArray(MethodInfo annotationMember) {
        boolean returnsClass = DotNames.CLASS.equals(annotationMember.returnType().name());
        boolean returnsClassArray = annotationMember.returnType().kind() == Type.Kind.ARRAY
                && DotNames.CLASS.equals(annotationMember.returnType().asArrayType().component().name());
        return returnsClass || returnsClassArray;
    }

    private static String defaultValueStaticFieldName(MethodInfo annotationMember) {
        return annotationMember.name() + "_default_value";
    }

    /**
     * Generates {@code public static final} fields for all the annotation members
     * that provide a default value and are of a class or class array type.
     * Also generates a static initializer that assigns the default value of those
     * annotation members to the generated fields.
     *
     * @param classCreator the class to which the fields and the static initializer should be added
     * @param annotationMembers the full set of annotation members of an annotation type
     */
    private static void generateStaticFieldsWithDefaultValues(ClassCreator classCreator, List<MethodInfo> annotationMembers) {
        List<MethodInfo> defaultOfClassType = new ArrayList<>();
        for (MethodInfo annotationMember : annotationMembers) {
            if (annotationMember.defaultValue() != null && returnsClassOrClassArray(annotationMember)) {
                defaultOfClassType.add(annotationMember);
            }
        }

        if (defaultOfClassType.isEmpty()) {
            return;
        }

        MethodCreator staticConstructor = classCreator.getMethodCreator(Methods.CLINIT, void.class);
        staticConstructor.setModifiers(ACC_STATIC);

        for (MethodInfo annotationMember : defaultOfClassType) {
            String type = annotationMember.returnType().name().toString();
            AnnotationValue defaultValue = annotationMember.defaultValue();

            FieldCreator fieldCreator = classCreator.getFieldCreator(defaultValueStaticFieldName(annotationMember), type);
            fieldCreator.setModifiers(ACC_PUBLIC | ACC_STATIC | ACC_FINAL);

            if (defaultValue.kind() == AnnotationValue.Kind.ARRAY) {
                Type[] clazzArray = defaultValue.asClassArray();
                ResultHandle array = staticConstructor.newArray(type, clazzArray.length);
                for (int i = 0; i < clazzArray.length; ++i) {
                    staticConstructor.writeArrayValue(array, staticConstructor.load(i),
                            staticConstructor.loadClassFromTCCL(clazzArray[i].name().toString()));
                }
                staticConstructor.writeStaticField(fieldCreator.getFieldDescriptor(), array);
            } else {
                staticConstructor.writeStaticField(fieldCreator.getFieldDescriptor(),
                        staticConstructor.loadClassFromTCCL(defaultValue.asClass().name().toString()));

            }
        }

        staticConstructor.returnValue(null);
    }

    /**
     * Generates a bytecode sequence to load given annotation member value.
     *
     * @param bytecode will receive the bytecode sequence for loading the annotation member value
     *        as a sequence of {@link BytecodeCreator} method calls
     * @param literal data about the annotation literal class currently being generated
     * @param annotationMember the annotation member whose value we're loading
     * @param annotationMemberValue the annotation member value we're loading
     * @param classOutput will receive annotation literal class bytecode, if sharing is disabled
     *        and the annotation member value is a nested annotation (for which we need to generate another literal class)
     * @return an annotation member value result handle
     */
    private ResultHandle loadValue(BytecodeCreator bytecode, AnnotationLiteralClassData literal,
            MethodInfo annotationMember, AnnotationValue annotationMemberValue, ClassOutput classOutput) {
        ResultHandle retValue;
        switch (annotationMemberValue.kind()) {
            case BOOLEAN:
                retValue = bytecode.load(annotationMemberValue.asBoolean());
                break;
            case BYTE:
                retValue = bytecode.load(annotationMemberValue.asByte());
                break;
            case SHORT:
                retValue = bytecode.load(annotationMemberValue.asShort());
                break;
            case INTEGER:
                retValue = bytecode.load(annotationMemberValue.asInt());
                break;
            case LONG:
                retValue = bytecode.load(annotationMemberValue.asLong());
                break;
            case FLOAT:
                retValue = bytecode.load(annotationMemberValue.asFloat());
                break;
            case DOUBLE:
                retValue = bytecode.load(annotationMemberValue.asDouble());
                break;
            case CHARACTER:
                retValue = bytecode.load(annotationMemberValue.asChar());
                break;
            case STRING:
                retValue = bytecode.load(annotationMemberValue.asString());
                break;
            case ENUM:
                retValue = bytecode.readStaticField(FieldDescriptor.of(annotationMemberValue.asEnumType().toString(),
                        annotationMemberValue.asEnum(), annotationMemberValue.asEnumType().toString()));
                break;
            case CLASS:
                if (annotationMemberValue.equals(annotationMember.defaultValue())) {
                    retValue = bytecode.readStaticField(FieldDescriptor.of(literal.generatedClassName,
                            defaultValueStaticFieldName(annotationMember),
                            annotationMember.returnType().name().toString()));
                } else {
                    retValue = bytecode.loadClassFromTCCL(annotationMemberValue.asClass().name().toString());
                }
                break;
            case NESTED:
                AnnotationInstance nestedAnnotation = annotationMemberValue.asNested();
                DotName annotationName = nestedAnnotation.name();
                ClassInfo annotationClass = beanArchiveIndex.getClassByName(annotationName);
                if (annotationClass == null) {
                    throw new IllegalStateException("Class of nested annotation " + nestedAnnotation + " missing");
                }
                retValue = create(bytecode, classOutput, annotationClass, nestedAnnotation,
                        Types.getPackageName(annotationName.toString()));
                break;
            case ARRAY:
                retValue = loadArrayValue(bytecode, literal, annotationMember, annotationMemberValue, classOutput);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported value: " + annotationMemberValue);
        }
        return retValue;
    }

    /**
     * Generates a bytecode sequence to load given array-typed annotation member value.
     *
     * @param bytecode will receive the bytecode sequence for loading the annotation member value
     *        as a sequence of {@link BytecodeCreator} method calls
     * @param literal data about the annotation literal class currently being generated
     * @param annotationMember the annotation member whose value we're loading
     * @param annotationMemberValue the annotation member value we're loading
     * @param classOutput will receive annotation literal class bytecode, if sharing is disabled
     *        and the annotation member value is a nested annotation (for which we need to generate another literal class)
     * @return an annotation member value result handle
     */
    private ResultHandle loadArrayValue(BytecodeCreator bytecode, AnnotationLiteralClassData literal,
            MethodInfo annotationMember, AnnotationValue annotationMemberValue, ClassOutput classOutput) {
        ResultHandle retValue;
        AnnotationValue.Kind componentKind = annotationMemberValue.componentKind();
        switch (componentKind) {
            case BOOLEAN:
                boolean[] booleanArray = annotationMemberValue.asBooleanArray();
                retValue = bytecode.newArray(componentType(annotationMember), booleanArray.length);
                for (int i = 0; i < booleanArray.length; i++) {
                    bytecode.writeArrayValue(retValue, i, bytecode.load(booleanArray[i]));
                }
                break;
            case BYTE:
                byte[] byteArray = annotationMemberValue.asByteArray();
                retValue = bytecode.newArray(componentType(annotationMember), byteArray.length);
                for (int i = 0; i < byteArray.length; i++) {
                    bytecode.writeArrayValue(retValue, i, bytecode.load(byteArray[i]));
                }
                break;
            case SHORT:
                short[] shortArray = annotationMemberValue.asShortArray();
                retValue = bytecode.newArray(componentType(annotationMember), shortArray.length);
                for (int i = 0; i < shortArray.length; i++) {
                    bytecode.writeArrayValue(retValue, i, bytecode.load(shortArray[i]));
                }
                break;
            case INTEGER:
                int[] intArray = annotationMemberValue.asIntArray();
                retValue = bytecode.newArray(componentType(annotationMember), intArray.length);
                for (int i = 0; i < intArray.length; i++) {
                    bytecode.writeArrayValue(retValue, i, bytecode.load(intArray[i]));
                }
                break;
            case LONG:
                long[] longArray = annotationMemberValue.asLongArray();
                retValue = bytecode.newArray(componentType(annotationMember), longArray.length);
                for (int i = 0; i < longArray.length; i++) {
                    bytecode.writeArrayValue(retValue, i, bytecode.load(longArray[i]));
                }
                break;
            case FLOAT:
                float[] floatArray = annotationMemberValue.asFloatArray();
                retValue = bytecode.newArray(componentType(annotationMember), floatArray.length);
                for (int i = 0; i < floatArray.length; i++) {
                    bytecode.writeArrayValue(retValue, i, bytecode.load(floatArray[i]));
                }
                break;
            case DOUBLE:
                double[] doubleArray = annotationMemberValue.asDoubleArray();
                retValue = bytecode.newArray(componentType(annotationMember), doubleArray.length);
                for (int i = 0; i < doubleArray.length; i++) {
                    bytecode.writeArrayValue(retValue, i, bytecode.load(doubleArray[i]));
                }
                break;
            case CHARACTER:
                char[] charArray = annotationMemberValue.asCharArray();
                retValue = bytecode.newArray(componentType(annotationMember), charArray.length);
                for (int i = 0; i < charArray.length; i++) {
                    bytecode.writeArrayValue(retValue, i, bytecode.load(charArray[i]));
                }
                break;
            case STRING:
                String[] stringArray = annotationMemberValue.asStringArray();
                retValue = bytecode.newArray(componentType(annotationMember), stringArray.length);
                for (int i = 0; i < stringArray.length; i++) {
                    bytecode.writeArrayValue(retValue, i, bytecode.load(stringArray[i]));
                }
                break;
            case ENUM:
                String[] enumArray = annotationMemberValue.asEnumArray();
                DotName[] enumTypeArray = annotationMemberValue.asEnumTypeArray();
                retValue = bytecode.newArray(componentType(annotationMember), enumArray.length);
                for (int i = 0; i < enumArray.length; i++) {
                    ResultHandle enumValue = bytecode.readStaticField(FieldDescriptor.of(
                            enumTypeArray[i].toString(), enumArray[i], enumTypeArray[i].toString()));
                    bytecode.writeArrayValue(retValue, i, enumValue);
                }
                break;
            case CLASS:
                if (annotationMemberValue.equals(annotationMember.defaultValue())) {
                    retValue = bytecode.readStaticField(FieldDescriptor.of(literal.generatedClassName,
                            defaultValueStaticFieldName(annotationMember),
                            annotationMember.returnType().name().toString()));
                } else {
                    Type[] classArray = annotationMemberValue.asClassArray();
                    retValue = bytecode.newArray(componentType(annotationMember), classArray.length);
                    for (int i = 0; i < classArray.length; i++) {
                        bytecode.writeArrayValue(retValue, i, bytecode.loadClassFromTCCL(classArray[i].name().toString()));
                    }
                }
                break;
            case NESTED:
                AnnotationInstance[] nestedArray = annotationMemberValue.asNestedArray();
                retValue = bytecode.newArray(componentType(annotationMember), nestedArray.length);
                for (int i = 0; i < nestedArray.length; i++) {
                    AnnotationInstance nestedAnnotation = nestedArray[i];
                    DotName annotationName = nestedAnnotation.name();
                    ClassInfo annotationClass = beanArchiveIndex.getClassByName(annotationName);
                    if (annotationClass == null) {
                        throw new IllegalStateException("Class of nested annotation " + nestedAnnotation + " missing");
                    }
                    ResultHandle nestedAnnotationValue = create(bytecode, classOutput, annotationClass,
                            nestedAnnotation, Types.getPackageName(annotationName.toString()));

                    bytecode.writeArrayValue(retValue, i, nestedAnnotationValue);
                }
                break;
            case UNKNOWN: // empty array
                DotName componentName = componentTypeName(annotationMember);
                // Use empty array constants for common component kinds
                if (PrimitiveType.BOOLEAN.name().equals(componentName)) {
                    retValue = bytecode.readStaticField(FieldDescriptors.ANNOTATION_LITERALS_EMPTY_BOOLEAN_ARRAY);
                } else if (PrimitiveType.BYTE.name().equals(componentName)) {
                    retValue = bytecode.readStaticField(FieldDescriptors.ANNOTATION_LITERALS_EMPTY_BYTE_ARRAY);
                } else if (PrimitiveType.SHORT.name().equals(componentName)) {
                    retValue = bytecode.readStaticField(FieldDescriptors.ANNOTATION_LITERALS_EMPTY_SHORT_ARRAY);
                } else if (PrimitiveType.INT.name().equals(componentName)) {
                    retValue = bytecode.readStaticField(FieldDescriptors.ANNOTATION_LITERALS_EMPTY_INT_ARRAY);
                } else if (PrimitiveType.LONG.name().equals(componentName)) {
                    retValue = bytecode.readStaticField(FieldDescriptors.ANNOTATION_LITERALS_EMPTY_LONG_ARRAY);
                } else if (PrimitiveType.FLOAT.name().equals(componentName)) {
                    retValue = bytecode.readStaticField(FieldDescriptors.ANNOTATION_LITERALS_EMPTY_FLOAT_ARRAY);
                } else if (PrimitiveType.DOUBLE.name().equals(componentName)) {
                    retValue = bytecode.readStaticField(FieldDescriptors.ANNOTATION_LITERALS_EMPTY_DOUBLE_ARRAY);
                } else if (PrimitiveType.CHAR.name().equals(componentName)) {
                    retValue = bytecode.readStaticField(FieldDescriptors.ANNOTATION_LITERALS_EMPTY_CHAR_ARRAY);
                } else if (DotNames.STRING.equals(componentName)) {
                    retValue = bytecode.readStaticField(FieldDescriptors.ANNOTATION_LITERALS_EMPTY_STRING_ARRAY);
                } else if (DotNames.CLASS.equals(componentName)) {
                    retValue = bytecode.readStaticField(FieldDescriptors.ANNOTATION_LITERALS_EMPTY_CLASS_ARRAY);
                } else {
                    retValue = bytecode.newArray(componentName.toString(), bytecode.load(0));
                }
                break;
            default:
                // at this point, the only possible componend kind is "array"
                throw new IllegalStateException("Array component kind is " + componentKind + ", this should never happen");
        }
        return retValue;
    }

    private static String componentType(MethodInfo method) {
        return componentTypeName(method).toString();
    }

    private static DotName componentTypeName(MethodInfo method) {
        ArrayType arrayType = method.returnType().asArrayType();
        return arrayType.component().name();
    }

    private static String generateSharedName(DotName annotationName) {
        // when the annotation is a java.lang annotation we need to use a different package in which to generate the literal
        // otherwise a security exception will be thrown when the literal is loaded
        String nameToUse = isJavaLang(annotationName.toString())
                ? AbstractGenerator.DEFAULT_PACKAGE + annotationName.withoutPackagePrefix()
                : annotationName.toString();

        // com.foo.MyQualifier -> com.foo.MyQualifier_Shared_AnnotationLiteral
        return nameToUse + SHARED_SUFFIX + ANNOTATION_LITERAL_SUFFIX;
    }

    private static String generateOneoffName(String targetPackage, String simpleName, String hash) {
        String nameToUse = isJavaLang(targetPackage)
                ? AbstractGenerator.DEFAULT_PACKAGE + "." + simpleName
                : targetPackage + "." + simpleName;

        // com.foo.MyQualifier -> com.foo.MyQualifier_somehashvalue_AnnotationLiteral
        return nameToUse + hash + ANNOTATION_LITERAL_SUFFIX;
    }

    private static boolean isJavaLang(String s) {
        return s.startsWith("java.lang");
    }

    private static class CacheKey {
        final ClassInfo annotationClass;

        CacheKey(ClassInfo annotationClass) {
            this.annotationClass = annotationClass;
        }

        DotName annotationName() {
            return annotationClass.name();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(annotationClass.name(), cacheKey.annotationClass.name());
        }

        @Override
        public int hashCode() {
            return Objects.hash(annotationClass.name());
        }
    }

    private static class AnnotationLiteralClassData {
        /**
         * Name of the generated annotation literal class.
         */
        final String generatedClassName;
        /**
         * Whether the generated annotation literal class is an application class.
         * Only used when sharing is enabled.
         */
        final boolean isApplicationClass;

        /**
         * The annotation type. The generated annotation literal class will implement this interface
         * (and extend {@code AnnotationLiteral<this interface>}). The process that generates
         * the annotation literal class may consult this, for example, to know the set of annotation members.
         */
        final ClassInfo annotationClass;

        AnnotationLiteralClassData(String generatedClassName, boolean isApplicationClass, ClassInfo annotationClass) {
            this.generatedClassName = generatedClassName;
            this.isApplicationClass = isApplicationClass;
            this.annotationClass = annotationClass;
        }

        DotName annotationName() {
            return annotationClass.name();
        }

        List<MethodInfo> annotationMembers() {
            List<MethodInfo> result = new ArrayList<>();
            for (MethodInfo method : annotationClass.methods()) {
                if (!method.name().equals(Methods.CLINIT) && !method.name().equals(Methods.INIT)) {
                    result.add(method);
                }
            }
            return result;
        }
    }
}
