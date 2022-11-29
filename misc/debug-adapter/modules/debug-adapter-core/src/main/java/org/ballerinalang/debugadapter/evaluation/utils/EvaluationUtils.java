/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation.utils;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvocationException;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.identifier.Utils;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.IdentifierModifier;
import org.ballerinalang.debugadapter.evaluation.engine.NameBasedTypeResolver;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.GeneratedStaticMethod;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeInstanceMethod;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.LocalVariableProxyImpl;
import org.ballerinalang.debugadapter.utils.PackageUtils;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.DebugVariableException;
import org.ballerinalang.debugadapter.variable.IndexedCompoundVariable;
import org.ballerinalang.debugadapter.variable.JVMValueType;
import org.ballerinalang.debugadapter.variable.VariableFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.CLASS_LOADING_FAILED;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.HELPER_UTIL_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.NAME_REF_RESOLVING_ERROR;
import static org.ballerinalang.debugadapter.evaluation.IdentifierModifier.encodeModuleName;
import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_FILE_EXT;
import static org.ballerinalang.debugadapter.utils.PackageUtils.INIT_CLASS_NAME;

/**
 * Debug expression evaluation utils.
 *
 * @since 2.0.0
 */
public class EvaluationUtils {

    // Debugger runtime helper classes
    private static final String DEBUGGER_HELPER_PREFIX = "ballerina.debugger_helpers.1.";
    public static final String B_UNARY_EXPR_HELPER_CLASS = DEBUGGER_HELPER_PREFIX + "unary_operations";
    public static final String B_ARITHMETIC_EXPR_HELPER_CLASS = DEBUGGER_HELPER_PREFIX + "arithmetic_operations";
    public static final String B_RELATIONAL_EXPR_HELPER_CLASS = DEBUGGER_HELPER_PREFIX + "relational_operations";
    public static final String B_BITWISE_EXPR_HELPER_CLASS = DEBUGGER_HELPER_PREFIX + "bitwise_operations";
    public static final String B_SHIFT_EXPR_HELPER_CLASS = DEBUGGER_HELPER_PREFIX + "shift_operations";
    public static final String B_LOGICAL_EXPR_HELPER_CLASS = DEBUGGER_HELPER_PREFIX + "logical_operations";
    public static final String B_RANGE_EXPR_HELPER_CLASS = DEBUGGER_HELPER_PREFIX + "range_operations";
    public static final String B_UTILS_HELPER_CLASS = DEBUGGER_HELPER_PREFIX + "utils";
    public static final String B_DEBUGGER_RUNTIME_CLASS = "org.ballerinalang.debugadapter.runtime.DebuggerRuntime";
    public static final String B_DEBUGGER_RUNTIME_UTILS_CLASS = "org.ballerinalang.debugadapter.runtime.VariableUtils";

    // Ballerina runtime helper classes
    private static final String RUNTIME_HELPER_PREFIX = "io.ballerina.runtime.";
    public static final String B_TYPE_CHECKER_CLASS = RUNTIME_HELPER_PREFIX + "internal.TypeChecker";
    public static final String B_TYPE_CREATOR_CLASS = RUNTIME_HELPER_PREFIX + "api.creators.TypeCreator";
    public static final String B_TYPE_CONVERTER_CLASS = RUNTIME_HELPER_PREFIX + "internal.TypeConverter";
    public static final String B_VALUE_CREATOR_CLASS = RUNTIME_HELPER_PREFIX + "api.creators.ValueCreator";
    public static final String B_STRING_UTILS_CLASS = RUNTIME_HELPER_PREFIX + "api.utils.StringUtils";
    public static final String B_TYPE_UTILS_CLASS = RUNTIME_HELPER_PREFIX + "api.utils.TypeUtils";
    public static final String B_XML_FACTORY_CLASS = RUNTIME_HELPER_PREFIX + "internal.XmlFactory";
    public static final String B_DECIMAL_VALUE_CLASS = RUNTIME_HELPER_PREFIX + "internal.values.DecimalValue";
    public static final String B_XML_CLASS = RUNTIME_HELPER_PREFIX + "api.values.BXml";
    public static final String B_XML_VALUE_CLASS = RUNTIME_HELPER_PREFIX + "internal.values.XmlValue";
    public static final String B_XML_SEQUENCE_CLASS = RUNTIME_HELPER_PREFIX + "api.values.BXmlSequence";
    public static final String B_STRING_CLASS = RUNTIME_HELPER_PREFIX + "api.values.BString";
    public static final String B_STRING_ARRAY_CLASS = B_STRING_CLASS + "[]";
    public static final String B_OBJECT_CLASS = RUNTIME_HELPER_PREFIX + "api.values.BObject";
    public static final String B_TYPE_CLASS = RUNTIME_HELPER_PREFIX + "api.types.Type";
    public static final String B_VALUE_ARRAY_CLASS = RUNTIME_HELPER_PREFIX + "api.values.BValue[]";
    public static final String B_TYPE_ARRAY_CLASS = RUNTIME_HELPER_PREFIX + "api.types.Type[]";
    public static final String B_SCHEDULER_CLASS = RUNTIME_HELPER_PREFIX + "internal.scheduling.Scheduler";
    public static final String B_STRAND_CLASS = RUNTIME_HELPER_PREFIX + "internal.scheduling.Strand";
    private static final String B_LINK_CLASS = RUNTIME_HELPER_PREFIX + "api.values.BLink";
    private static final String B_ERROR_VALUE_CLASS = RUNTIME_HELPER_PREFIX + "internal.values.ErrorValue";

    // Java runtime helper classes
    public static final String JAVA_OBJECT_CLASS = "java.lang.Object";
    public static final String JAVA_LANG_CLASSLOADER = "java.lang.ClassLoader";
    public static final String JAVA_OBJECT_ARRAY_CLASS = JAVA_OBJECT_CLASS + "[]";
    public static final String JAVA_STRING_CLASS = "java.lang.String";
    public static final String JAVA_LONG_CLASS = "java.lang.Long";
    private static final String JAVA_BOOLEAN_CLASS = "java.lang.Boolean";
    private static final String JAVA_INT_CLASS = "java.lang.Integer";
    private static final String JAVA_FLOAT_CLASS = "java.lang.Float";
    private static final String JAVA_DOUBLE_CLASS = "java.lang.Double";
    private static final String JAVA_LANG_CLASS = "java.lang.Class";

    // Helper methods
    public static final String B_ADD_METHOD = "add";
    public static final String B_SUB_METHOD = "subtract";
    public static final String B_MUL_METHOD = "multiply";
    public static final String B_DIV_METHOD = "divide";
    public static final String B_MOD_METHOD = "modulus";
    public static final String B_LT_METHOD = "lessThan";
    public static final String B_LT_EQUALS_METHOD = "lessThanOrEquals";
    public static final String B_GT_METHOD = "greaterThan";
    public static final String B_GT_EQUALS_METHOD = "greaterThanOrEquals";
    public static final String B_BITWISE_AND_METHOD = "bitwiseAND";
    public static final String B_BITWISE_OR_METHOD = "bitwiseOR";
    public static final String B_BITWISE_XOR_METHOD = "bitwiseXOR";
    public static final String B_LEFT_SHIFT_METHOD = "leftShift";
    public static final String B_SIGNED_RIGHT_SHIFT_METHOD = "signedRightShift";
    public static final String B_UNSIGNED_RIGHT_SHIFT_METHOD = "unsignedRightShift";
    public static final String B_LOGICAL_AND_METHOD = "logicalAND";
    public static final String B_LOGICAL_OR_METHOD = "logicalOR";
    public static final String B_UNARY_PLUS_METHOD = "unaryPlus";
    public static final String B_UNARY_MINUS_METHOD = "unaryMinus";
    public static final String B_UNARY_INVERT_METHOD = "unaryInvert";
    public static final String B_UNARY_NOT_METHOD = "unaryNot";
    public static final String B_GET_TRAP_RESULT_METHOD = "getTrapResult";
    public static final String GET_ANNOT_VALUE_METHOD = "getAnnotationValue";
    public static final String GET_TYPEDESC_METHOD = "getTypedesc";
    public static final String CHECK_IS_TYPE_METHOD = "checkIsType";
    public static final String CHECK_CAST_METHOD = "checkCast";
    public static final String CREATE_UNION_TYPE_METHOD = "createUnionType";
    public static final String CREATE_ARRAY_TYPE_METHOD = "createArrayType";
    public static final String CREATE_DECIMAL_VALUE_METHOD = "createDecimalValue";
    public static final String CREATE_XML_ITEM = "createXmlItem";
    public static final String CREATE_XML_VALUE_METHOD = "createXmlValue";
    public static final String CREATE_OBJECT_VALUE_METHOD = "createObjectValue";
    public static final String CREATE_ERROR_VALUE_METHOD = "createErrorValue";
    public static final String CREATE_TYPEDESC_VALUE_METHOD = "createTypedescValue";
    public static final String VALUE_OF_METHOD = "valueOf";
    public static final String VALUE_FROM_STRING_METHOD = "fromString";
    public static final String REF_EQUAL_METHOD = "isReferenceEqual";
    public static final String VALUE_EQUAL_METHOD = "isEqual";
    public static final String XML_CONCAT_METHOD = "concatenate";
    public static final String STRING_TO_XML_METHOD = "stringToXml";
    public static final String INVOKE_OBJECT_METHOD_ASYNC = "invokeObjectMethod";
    public static final String INVOKE_FUNCTION_ASYNC = "invokeFunction";
    public static final String CLASSLOAD_AND_INVOKE_METHOD = "classloadAndInvokeFunction";
    public static final String CREATE_INT_RANGE_METHOD = "createIntRange";
    public static final String GET_REST_ARG_ARRAY_METHOD = "getRestArgArray";
    public static final String GET_XML_FILTER_RESULT_METHOD = "getXMLFilterResult";
    public static final String GET_XML_STEP_RESULT_METHOD = "getXMLStepResult";
    public static final String GET_BMAP_TYPE_METHOD = "getBMapType";
    public static final String GET_STRING_AT_METHOD = "getStringAt";
    static final String FROM_STRING_METHOD = "fromString";
    private static final String B_STRING_CONCAT_METHOD = "concat";
    private static final String FOR_NAME_METHOD = "forName";
    private static final String GET_STRING_VALUE_METHOD = "getStringValue";
    private static final String INT_VALUE_METHOD = "intValue";
    private static final String LONG_VALUE_METHOD = "longValue";
    private static final String FLOAT_VALUE_METHOD = "floatValue";
    private static final String DOUBLE_VALUE_METHOD = "doubleValue";

    // Misc
    public static final String SELF_VAR_NAME = "self";
    public static final String STRAND_VAR_NAME = "__strand";
    public static final String REST_ARG_IDENTIFIER = "...";
    public static final String MODULE_NAME_SEPARATOR = ".";
    public static final String MODULE_VERSION_SEPARATOR_REGEX = "\\.";
    public static final String MODULE_NAME_SEPARATOR_REGEX = "\\.";

    private EvaluationUtils() {
    }

    /**
     * Loads and returns Ballerina JVM runtime method instance for a given qualified class name + method name.
     *
     * @param context      suspended context
     * @param qClassName   qualified name of the class to be loaded
     * @param methodName   name of the method to be loaded
     * @param argTypeNames argument type names
     * @return corresponding Ballerina JVM runtime method instance
     */
    public static RuntimeStaticMethod getRuntimeMethod(SuspendedContext context, String qClassName, String methodName,
                                                       List<String> argTypeNames) throws EvaluationException {
        // Search within loaded classes in JVM.
        List<ReferenceType> classesRef = context.getAttachedVm().classesByName(qClassName);
        // Tries to load the required class instance using "java.lang.Class.forName()" method.
        if (classesRef == null || classesRef.isEmpty()) {
            classesRef = Collections.singletonList(loadClass(context, qClassName, methodName));
        }
        List<Method> methods = classesRef.get(0).methodsByName(methodName);
        if (methods == null || methods.isEmpty()) {
            throw createEvaluationException(HELPER_UTIL_NOT_FOUND, methodName);
        }
        methods = methods.stream().filter(method -> method.isPublic() && method.isStatic() &&
                compare(method.argumentTypeNames(), argTypeNames)).collect(Collectors.toList());
        if (methods.size() != 1) {
            throw createEvaluationException(HELPER_UTIL_NOT_FOUND, methodName);
        }
        return new RuntimeStaticMethod(context, classesRef.get(0), methods.get(0));
    }

    public static GeneratedStaticMethod getGeneratedMethod(SuspendedContext context, String qClassName,
                                                           String methodName) throws EvaluationException {
        // Search within loaded classes in JVM.
        List<ReferenceType> classesRef = context.getAttachedVm().classesByName(qClassName);
        // Tries to load the required class instance using "java.lang.Class.forName()" method.
        if (classesRef == null || classesRef.isEmpty()) {
            classesRef = Collections.singletonList(loadClass(context, qClassName, methodName));
        }
        List<Method> methods = classesRef.get(0).methodsByName(methodName);
        if (methods == null || methods.isEmpty()) {
            throw createEvaluationException(HELPER_UTIL_NOT_FOUND, methodName);
        }
        methods = methods.stream()
                .filter(method -> method.isPublic() && method.isStatic())
                .collect(Collectors.toList());

        if (methods.size() != 1) {
            throw createEvaluationException(HELPER_UTIL_NOT_FOUND, methodName);
        }
        return new GeneratedStaticMethod(context, classesRef.get(0), methods.get(0));
    }

    public static ReferenceType loadClass(SuspendedContext evaluationContext, String qName, String methodName)
            throws EvaluationException {
        try {
            ClassType classType = (ClassType) evaluationContext.getAttachedVm().classesByName(JAVA_LANG_CLASS).get(0);
            if (classType == null) {
                throw createEvaluationException(CLASS_LOADING_FAILED, methodName);
            }
            Method forNameMethod = null;
            List<Method> methods = classType.methodsByName(FOR_NAME_METHOD);
            for (Method method : methods) {
                if (method.argumentTypeNames().size() == 3) {
                    forNameMethod = method;
                }
            }
            if (forNameMethod == null) {
                throw createEvaluationException(CLASS_LOADING_FAILED, methodName);
            }

            // Do not use unmodifiable lists because the list will be modified by JPDA.
            List<Value> args = new ArrayList<>();
            args.add(evaluationContext.getAttachedVm().mirrorOf(qName));
            args.add(evaluationContext.getAttachedVm().mirrorOf(true));
            args.add(evaluationContext.getDebuggeeClassLoader());
            Value classReference = classType.invokeMethod(evaluationContext.getOwningThread().getThreadReference(),
                    forNameMethod, args, ObjectReference.INVOKE_SINGLE_THREADED);
            return ((ClassObjectReference) classReference).reflectedType();
        } catch (Exception e) {
            throw createEvaluationException(CLASS_LOADING_FAILED, methodName);
        }
    }

    /**
     * Converts java primitive types into their wrapper implementations, as some of the the JVM runtime util methods
     * accepts only the sub classes of @{@link java.lang.Object}.
     */
    public static List<Value> getAsObjects(SuspendedContext context, List<Value> argValueList)
            throws EvaluationException {
        List<Value> boxedValues = new ArrayList<>();
        for (Value value : argValueList) {
            boxedValues.add(getValueAsObject(context, value));
        }
        return boxedValues;
    }

    /**
     * As some of the JVM runtime util method accepts only the sub classes of @{@link java.lang.Object},
     * java primitive types need to be converted into their wrapper implementations.
     *
     * @param value JDI value instance.
     * @return value as an instance of {@code com.sun.jdi.Value} instance.
     */
    public static Value getValueAsObject(SuspendedContext context, Value value) throws EvaluationException {
        BVariable bVar = VariableFactory.getVariable(context, value);
        if (value instanceof ObjectReference) {
            return value;
        }
        return getValueAsObject(context, bVar);
    }

    /**
     * Coverts an object of a java wrapper type to its corresponding java primitive value.
     */
    public static Value unboxValue(SuspendedContext context, Value value) {
        try {
            if (!(value instanceof ObjectReference)) {
                return value;
            }

            String typeName = value.type().name();
            List<Method> method;
            switch (typeName) {
                case JAVA_INT_CLASS:
                    method = ((ObjectReference) value).referenceType().methodsByName(INT_VALUE_METHOD);
                    break;
                case JAVA_LONG_CLASS:
                    method = ((ObjectReference) value).referenceType().methodsByName(LONG_VALUE_METHOD);
                    break;
                case JAVA_FLOAT_CLASS:
                    method = ((ObjectReference) value).referenceType().methodsByName(FLOAT_VALUE_METHOD);
                    break;
                case JAVA_DOUBLE_CLASS:
                    method = ((ObjectReference) value).referenceType().methodsByName(DOUBLE_VALUE_METHOD);
                    break;
                default:
                    return value;
            }

            if (method.size() != 1) {
                return value;
            }
            RuntimeInstanceMethod unboxingMethod = new RuntimeInstanceMethod(context, value, method.get(0));
            unboxingMethod.setArgValues(new ArrayList<>());
            return unboxingMethod.invokeSafely();
        } catch (EvaluationException e) {
            return value;
        }
    }

    /**
     * As some of the JVM runtime util method accepts only the sub classes of @{@link java.lang.Object},
     * java primitive types need to be converted into their wrapper implementations.
     *
     * @param variable ballerina variable instance.
     * @return value as an instance of {@code com.sun.jdi.Value} instance.
     */
    public static Value getValueAsObject(SuspendedContext context, BVariable variable) throws EvaluationException {
        RuntimeStaticMethod method;
        List<String> methodArgTypeNames = new ArrayList<>();
        Value jvmValue = variable.getJvmValue();
        switch (variable.getBType()) {
            case BOOLEAN:
                if (jvmValue instanceof BooleanValue) {
                    methodArgTypeNames.add(JVMValueType.BOOLEAN.getString());
                    method = getRuntimeMethod(context, JAVA_BOOLEAN_CLASS, VALUE_OF_METHOD, methodArgTypeNames);
                    method.setArgValues(Collections.singletonList(jvmValue));
                    return method.invokeSafely();
                } else {
                    return jvmValue;
                }
            case INT:
                if (jvmValue instanceof IntegerValue) {
                    methodArgTypeNames.add(JVMValueType.INT.getString());
                    method = getRuntimeMethod(context, JAVA_INT_CLASS, VALUE_OF_METHOD, methodArgTypeNames);
                    method.setArgValues(Collections.singletonList(jvmValue));
                    return method.invokeSafely();
                } else if (jvmValue instanceof LongValue) {
                    methodArgTypeNames.add(JVMValueType.LONG.getString());
                    method = getRuntimeMethod(context, JAVA_LONG_CLASS, VALUE_OF_METHOD, methodArgTypeNames);
                    method.setArgValues(Collections.singletonList(jvmValue));
                    return method.invokeSafely();
                } else {
                    return jvmValue;
                }
            case FLOAT:
                jvmValue = variable.getJvmValue();
                if (jvmValue instanceof FloatValue) {
                    methodArgTypeNames.add(JVMValueType.FLOAT.getString());
                    method = getRuntimeMethod(context, JAVA_FLOAT_CLASS, VALUE_OF_METHOD, methodArgTypeNames);
                    method.setArgValues(Collections.singletonList(variable.getJvmValue()));
                    return method.invokeSafely();
                } else if (jvmValue instanceof DoubleValue) {
                    methodArgTypeNames.add(JVMValueType.DOUBLE.getString());
                    method = getRuntimeMethod(context, JAVA_DOUBLE_CLASS, VALUE_OF_METHOD, methodArgTypeNames);
                    method.setArgValues(Collections.singletonList(variable.getJvmValue()));
                    return method.invokeSafely();
                } else {
                    return jvmValue;
                }
            default:
                return variable.getJvmValue();
        }
    }

    /**
     * Check whether a given value belongs to the given type.
     *
     * @param sourceVal  value to check the type
     * @param targetType type to be test against
     * @return true if the value belongs to the given type, false otherwise
     */
    public static boolean checkIsType(SuspendedContext context, Value sourceVal, Value targetType)
            throws EvaluationException {
        List<String> methodArgTypeNames = new ArrayList<>();
        methodArgTypeNames.add(JAVA_OBJECT_CLASS);
        methodArgTypeNames.add(B_TYPE_CLASS);
        RuntimeStaticMethod method = getRuntimeMethod(context, B_TYPE_CHECKER_CLASS, CHECK_IS_TYPE_METHOD,
                methodArgTypeNames);

        List<Value> methodArgs = new ArrayList<>();
        methodArgs.add(getValueAsObject(context, sourceVal));
        methodArgs.add(targetType);
        method.setArgValues(methodArgs);
        return Boolean.parseBoolean(new BExpressionValue(context, method.invokeSafely()).getStringValue());
    }

    /**
     * Concatenates multiple BString values and returns the result an instance of BString.
     *
     * @param bStrings input BString instances.
     * @return concatenated string result.
     */
    public static Value concatBStrings(SuspendedContext context, Value... bStrings) throws EvaluationException {
        Value result = bStrings[0];
        List<Method> method = ((ObjectReference) result).referenceType().methodsByName(B_STRING_CONCAT_METHOD);
        for (int i = 1; i < bStrings.length; i++) {
            if (method.size() != 1) {
                throw createEvaluationException("Error occurred when trying to load required methods to execute " +
                        "BString concatenation");
            }
            RuntimeInstanceMethod concatMethod = new RuntimeInstanceMethod(context, result, method.get(0));
            concatMethod.setArgValues(Collections.singletonList(bStrings[i]));
            result = concatMethod.invokeSafely();
            method = ((ObjectReference) result).referenceType().methodsByName("concat");
        }
        return result;
    }

    /**
     * Returns the human-readable string value of a given Ballerina value in direct style.
     *
     * @param value Ballerina value instance.
     * @return human-readable string value of Ballerina values with direct style.
     */
    public static Value getStringValue(SuspendedContext context, Value value) throws EvaluationException {
        List<String> argTypeNames = new ArrayList<>();
        argTypeNames.add(JAVA_OBJECT_CLASS);
        argTypeNames.add(B_LINK_CLASS);
        RuntimeStaticMethod runtimeMethod = getRuntimeMethod(context, B_STRING_UTILS_CLASS, GET_STRING_VALUE_METHOD,
                argTypeNames);
        List<Value> args = new ArrayList<>();
        args.add(EvaluationUtils.getValueAsObject(context, value));
        args.add(null);
        runtimeMethod.setArgValues(args);
        return runtimeMethod.invokeSafely();
    }

    /**
     * Converts the user given string literal into an {@link io.ballerina.runtime.api.values.BString} instance.
     *
     * @param context suspended debug context
     * @param val     string value
     * @return {@link io.ballerina.runtime.api.values.BString} instance
     */
    public static Value getAsBString(SuspendedContext context, String val) throws EvaluationException {
        return getAsBString(context, context.getAttachedVm().mirrorOf(val));
    }

    /**
     * Converts the user given string literal into an {@link io.ballerina.runtime.api.values.BString} instance.
     *
     * @param context   suspended debug context
     * @param stringRef JDI value reference of the string
     * @return {@link io.ballerina.runtime.api.values.BString} instance
     */
    public static Value getAsBString(SuspendedContext context, StringReference stringRef) throws EvaluationException {
        List<String> argTypeNames = Collections.singletonList(JAVA_STRING_CLASS);
        RuntimeStaticMethod fromStringMethod = getRuntimeMethod(context, B_STRING_UTILS_CLASS, FROM_STRING_METHOD,
                argTypeNames);
        fromStringMethod.setArgValues(Collections.singletonList(stringRef));
        return fromStringMethod.invokeSafely();
    }

    /**
     * Returns the fully-qualified generated java class name for the source file, which includes the given symbol.
     *
     * @param symbol source symbol
     * @return the fully-qualified generated java class name for the source file, which includes the given symbol
     */
    public static String constructQualifiedClassName(Symbol symbol) {
        String className = symbol.getLocation().orElseThrow().lineRange().filePath().replaceAll(BAL_FILE_EXT + "$", "");
        if (symbol.getModule().isEmpty()) {
            return className;
        }

        ModuleID moduleMeta = symbol.getModule().get().id();
        // for ballerina single source files, the package name will be "." and therefore,
        // qualified class name ::= <file_name>
        if (moduleMeta.packageName().equals(".")) {
            return className;
        }

        // for ballerina package source files,
        // qualified class name ::= <package_name>.<module_name>.<package_major_version>.<file_name>
        return new StringJoiner(".")
                .add(encodeModuleName(moduleMeta.orgName()))
                .add(encodeModuleName(moduleMeta.moduleName()))
                .add(moduleMeta.version().split(MODULE_VERSION_SEPARATOR_REGEX)[0])
                .add(className)
                .toString();
    }

    /**
     * Converts the user given string literal into a {@link com.sun.jdi.StringReference} instance.
     *
     * @param context suspended debug context
     * @param val     string value
     * @return {@link com.sun.jdi.StringReference} instance
     */
    public static Value getAsJString(SuspendedContext context, String val) throws EvaluationException {
        return context.getAttachedVm().mirrorOf(val);
    }

    /**
     * Checks if a given invocation exception is an instance of {@link io.ballerina.runtime.api.values.BError} and if
     * so, returns it as a JDI value instance.
     */
    public static Optional<Value> getBError(Exception e) {
        if (!(e instanceof InvocationException)) {
            return Optional.empty();
        }
        String typeName = ((InvocationException) e).exception().referenceType().name();
        if (typeName.equals(B_ERROR_VALUE_CLASS)) {
            return Optional.ofNullable(((InvocationException) e).exception());
        }
        return Optional.empty();
    }

    private static boolean compare(List<String> list1, List<String> list2) {
        return list1.size() == list2.size() && IntStream.range(0, list1.size()).allMatch(i ->
                list1.get(i).equals(list2.get(i)));
    }

    /**
     * This util is used as a workaround till the ballerina identifier encoding/decoding mechanisms get fixed.
     * Todo - remove
     */
    public static String modifyName(String identifier) {
        return Utils.decodeIdentifier(IdentifierModifier.encodeIdentifier(identifier,
                IdentifierModifier.IdentifierType.OTHER));
    }

    /**
     * Returns runtime value of the Ballerina value reference for the given name. For that, this method searches for a
     * match according to the below order.
     *
     * <ul>
     * <li> variables defined both local and global scopes
     * <li> types defined int both local and global scopes
     * </ul>
     *
     * @param context suspended context
     * @param name    name of the variable to be retrieved
     * @return the JDI value instance of the Ballerina variable
     */
    public static Value fetchNameReferenceValue(EvaluationContext context, String name) throws EvaluationException {
        Optional<BExpressionValue> variableReferenceValue = fetchVariableReferenceValue(context, name);
        if (variableReferenceValue.isPresent()) {
            return variableReferenceValue.get().getJdiValue();
        }

        NameBasedTypeResolver typeResolver = new NameBasedTypeResolver(context);
        try {
            List<Value> resolvedTypes = typeResolver.resolve(name);
            if (resolvedTypes.size() != 1) {
                throw createEvaluationException(NAME_REF_RESOLVING_ERROR, name);
            }
            Value type = resolvedTypes.get(0);
            List<String> argTypeNames = new LinkedList<>();
            argTypeNames.add(B_TYPE_CLASS);
            RuntimeStaticMethod createTypeDescMethod = getRuntimeMethod(context.getSuspendedContext(),
                    B_VALUE_CREATOR_CLASS, CREATE_TYPEDESC_VALUE_METHOD, argTypeNames);
            List<Value> argValues = new LinkedList<>();
            argValues.add(type);
            createTypeDescMethod.setArgValues(argValues);
            return createTypeDescMethod.invokeSafely();
        } catch (EvaluationException e) {
            throw createEvaluationException(NAME_REF_RESOLVING_ERROR, name);
        }
    }

    /**
     * Returns runtime value of the Ballerina variable value reference for the given name.
     *
     * @param context suspended context
     * @param name    name of the variable to be retrieved
     * @return the JDI value instance of the Ballerina variable
     */
    public static Optional<BExpressionValue> fetchVariableReferenceValue(EvaluationContext context, String name)
            throws EvaluationException {
        Optional<BExpressionValue> bExpressionValue = searchLocalVariables(context.getSuspendedContext(), name);
        if (bExpressionValue.isPresent()) {
            return bExpressionValue;
        }

        bExpressionValue = searchGlobalVariables(context.getSuspendedContext(), name);
        return bExpressionValue;
    }

    /**
     * Returns runtime value of the matching local variable, for the given name.
     *
     * @param context       suspended context
     * @param nameReference name of the variable to be retrieved
     * @return the JDI value instance of the local variable
     */
    private static Optional<BExpressionValue> searchLocalVariables(SuspendedContext context, String nameReference) {
        try {
            LocalVariableProxyImpl jvmVar = context.getFrame().visibleVariableByName(nameReference);
            if (jvmVar != null) {
                return Optional.of(new BExpressionValue(context, context.getFrame().getValue(jvmVar)));
            }

            // As all the ballerina variables which are being used inside lambda functions are converted into maps
            // during the runtime code generation, such local variables should be accessed in a different manner.
            List<LocalVariableProxyImpl> lambdaParamMaps = context.getFrame().visibleVariables().stream()
                    .filter(org.ballerinalang.debugadapter.variable.VariableUtils::isLambdaParamMap)
                    .collect(Collectors.toList());

            Optional<Value> localVariableMatch = lambdaParamMaps.stream()
                    .map(localVariableProxy -> {
                        try {
                            Value varValue = context.getFrame().getValue(localVariableProxy);
                            BVariable mapVar = VariableFactory.getVariable(context, varValue);
                            if (mapVar == null || mapVar.getBType() != BVariableType.MAP
                                    || !(mapVar instanceof IndexedCompoundVariable)) {
                                return null;
                            }
                            return ((IndexedCompoundVariable) mapVar).getChildByName(nameReference);
                        } catch (JdiProxyException | DebugVariableException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .findAny();

            if (localVariableMatch.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new BExpressionValue(context, localVariableMatch.get()));
        } catch (JdiProxyException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns runtime value of the matching global variable, for the given name.
     *
     * @param context       suspended context
     * @param nameReference name of the variable to be retrieved
     * @return the JDI value instance of the global variable
     */
    private static Optional<BExpressionValue> searchGlobalVariables(SuspendedContext context, String nameReference) {
        String classQName = PackageUtils.getQualifiedClassName(context, INIT_CLASS_NAME);
        return getFieldValue(context, classQName, nameReference);
    }

    private static Optional<BExpressionValue> getFieldValue(SuspendedContext context, String qualifiedClassName,
                                                            String fieldName) {
        List<ReferenceType> classesRef = context.getAttachedVm().classesByName(qualifiedClassName);
        // Tries to load the required class instance using "java.lang.Class.forName()" method.
        if (classesRef == null || classesRef.isEmpty()) {
            try {
                classesRef = Collections.singletonList(loadClass(context, qualifiedClassName, ""));
            } catch (EvaluationException e) {
                return Optional.empty();
            }
        }
        if (classesRef.size() != 1) {
            return Optional.empty();
        }

        Field field = classesRef.get(0).fieldByName(fieldName);
        if (field == null) {
            return Optional.empty();
        }
        return Optional.of(new BExpressionValue(context, classesRef.get(0).getValue(field)));
    }
}
