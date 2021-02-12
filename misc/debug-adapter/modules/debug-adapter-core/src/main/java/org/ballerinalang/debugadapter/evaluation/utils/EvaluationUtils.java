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

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.engine.GeneratedStaticMethod;
import org.ballerinalang.debugadapter.evaluation.engine.RuntimeInstanceMethod;
import org.ballerinalang.debugadapter.evaluation.engine.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.VariableFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Debug expression evaluation utils.
 *
 * @since 2.0.0
 */
public class EvaluationUtils {

    // Helper classes
    public static final String B_UNARY_EXPR_HELPER_CLASS = "ballerina.debugger_helpers.1_0_0.unary";
    public static final String B_BINARY_EXPR_HELPER_CLASS = "ballerina.debugger_helpers.1_0_0.binary";
    public static final String B_TYPE_CHECKER_CLASS = "io.ballerina.runtime.internal.TypeChecker";
    public static final String B_TYPE_CREATOR_CLASS = "io.ballerina.runtime.api.creators.TypeCreator";
    public static final String B_STRING_UTILS_CLASS = "io.ballerina.runtime.api.utils.StringUtils";
    public static final String B_TYPE_UTILS_CLASS = "io.ballerina.runtime.api.utils.TypeUtils";
    public static final String B_XML_FACTORY_CLASS = "io.ballerina.runtime.internal.XmlFactory";
    public static final String B_DECIMAL_VALUE_CLASS = "io.ballerina.runtime.internal.values.DecimalValue";
    public static final String B_XML_VALUE_CLASS = "io.ballerina.runtime.internal.values.XmlValue";
    public static final String B_STRING_CLASS = "io.ballerina.runtime.api.values.BString";
    public static final String FROM_STRING_CLASS = "org.ballerinalang.langlib.xml.FromString";
    public static final String B_TYPE_CLASS = "io.ballerina.runtime.api.types.Type";
    public static final String B_TYPE_ARRAY_CLASS = "io.ballerina.runtime.api.types.Type[]";
    public static final String JAVA_OBJECT_CLASS = "java.lang.Object";
    public static final String JAVA_STRING_CLASS = "java.lang.String";
    private static final String B_LINK_CLASS = "io.ballerina.runtime.api.values.BLink";
    private static final String B_ERROR_VALUE_CLASS = "io.ballerina.runtime.internal.values.ErrorValue";
    private static final String JAVA_BOOLEAN_CLASS = "java.lang.Boolean";
    private static final String JAVA_LONG_CLASS = "java.lang.Long";
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
    public static final String GET_TYPEDESC_METHOD = "getTypedesc";
    public static final String CHECK_IS_TYPE_METHOD = "checkIsType";
    public static final String CHECK_CAST_METHOD = "checkCast";
    public static final String CREATE_UNION_TYPE_METHOD = "createUnionType";
    public static final String VALUE_OF_METHOD = "valueOf";
    public static final String VALUE_FROM_STRING_METHOD = "fromString";
    public static final String REF_EQUAL_METHOD = "isReferenceEqual";
    public static final String VALUE_EQUAL_METHOD = "isEqual";
    public static final String XML_CONCAT_METHOD = "concatenate";
    public static final String XML_FROM_STRING_METHOD = "fromString";
    private static final String B_STRING_CONCAT_METHOD = "concat";
    static final String FROM_STRING_METHOD = "fromString";
    private static final String FOR_NAME_METHOD = "forName";
    private static final String GET_STRING_VALUE_METHOD = "getStringValue";
    // Misc
    public static final String STRAND_VAR_NAME = "__strand";
    public static final String REST_ARG_IDENTIFIER = "...";

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
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Error " +
                    "occurred when trying to load JVM util function: " + methodName));
        }
        methods = methods.stream().filter(method -> method.isPublic() && method.isStatic() &&
                compare(method.argumentTypeNames(), argTypeNames)).collect(Collectors.toList());
        if (methods.size() != 1) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Error " +
                    "occurred when trying to load JVM util function: " + methodName));
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
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Error " +
                    "occurred when trying to load JVM util function: " + methodName));
        }
        methods = methods.stream().filter(method -> method.isPublic() && method.isStatic())
                .collect(Collectors.toList());
        if (methods.size() != 1) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Error " +
                    "occurred when trying to load JVM util function: " + methodName));
        }
        return new GeneratedStaticMethod(context, classesRef.get(0), methods.get(0));
    }

    public static ReferenceType loadClass(SuspendedContext evaluationContext, String qName, String methodName)
            throws EvaluationException {
        try {
            ClassType classType = (ClassType) evaluationContext.getAttachedVm().classesByName(JAVA_LANG_CLASS).get(0);
            if (classType == null) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "jvm " +
                        "class instance for the function invocation couldn't be loaded due to an internal error."));
            }
            Method forNameMethod = null;
            List<Method> methods = classType.methodsByName(FOR_NAME_METHOD);
            for (Method method : methods) {
                if (method.argumentTypeNames().size() == 3) {
                    forNameMethod = method;
                }
            }
            if (forNameMethod == null) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "jvm " +
                        "class instance for the function invocation couldn't be loaded due to an internal error."));
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
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Error " +
                    "occurred when trying to load required classes to execute the function: " + methodName));
        }
    }

    /**
     * As some of the the JVM runtime util method accepts only the sub classes of @{@link java.lang.Object},
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
     * As some of the the JVM runtime util method accepts only the sub classes of @{@link java.lang.Object},
     * java primitive types need to be converted into their wrapper implementations.
     *
     * @param variable ballerina variable instance.
     * @return value as an instance of {@code com.sun.jdi.Value} instance.
     */
    public static Value getValueAsObject(SuspendedContext context, BVariable variable) throws EvaluationException {
        RuntimeStaticMethod method;
        List<String> methodArgTypeNames = new ArrayList<>();
        switch (variable.getBType()) {
            case BOOLEAN:
                methodArgTypeNames.add("boolean");
                method = getRuntimeMethod(context, JAVA_BOOLEAN_CLASS, VALUE_OF_METHOD, methodArgTypeNames);
                method.setArgValues(Collections.singletonList(variable.getJvmValue()));
                return method.invoke();
            case INT:
                methodArgTypeNames.add("long");
                method = getRuntimeMethod(context, JAVA_LONG_CLASS, VALUE_OF_METHOD, methodArgTypeNames);
                method.setArgValues(Collections.singletonList(variable.getJvmValue()));
                return method.invoke();
            case FLOAT:
                methodArgTypeNames.add("double");
                method = getRuntimeMethod(context, JAVA_DOUBLE_CLASS, VALUE_OF_METHOD, methodArgTypeNames);
                method.setArgValues(Collections.singletonList(variable.getJvmValue()));
                return method.invoke();
            default:
                return variable.getJvmValue();
        }
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
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Error " +
                        "occurred when trying to load required methods to execute BString concatenation"));
            }
            RuntimeInstanceMethod concatMethod = new RuntimeInstanceMethod(context, result, method.get(0));
            concatMethod.setArgValues(Collections.singletonList(bStrings[i]));
            result = concatMethod.invoke();
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
        return runtimeMethod.invoke();
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
        return fromStringMethod.invoke();
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
}
