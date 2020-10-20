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

package org.ballerinalang.debugadapter.evaluation;

import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.engine.JvmStaticMethod;
import org.ballerinalang.debugadapter.evaluation.engine.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.VariableFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.ballerinalang.debugadapter.variable.VariableUtils.removeRedundantQuotes;

/**
 * Debug expression evaluation utils.
 */
public class EvaluationUtils {


    // Helper classes
    public static final String B_TYPE_CHECKER_CLASS = "io.ballerina.runtime.TypeChecker";
    private static final String B_STRING_UTILS_CLASS = "io.ballerina.runtime.api.StringUtils";
    public static final String JAVA_BOOLEAN_CLASS = "java.lang.Boolean";
    public static final String JAVA_LONG_CLASS = "java.lang.Long";
    public static final String JAVA_DOUBLE_CLASS = "java.lang.Double";
    public static final String JAVA_LANG_CLASS = "java.lang.Class";
    public static final String JAVA_OBJECT_CLASS = "java.lang.Object";
    // Helper methods
    public static final String GET_TYPEDESC_METHOD = "getTypedesc";
    public static final String VALUE_OF_METHOD = "valueOf";
    public static final String REF_EQUAL_METHOD = "isReferenceEqual";
    public static final String VALUE_EQUAL_METHOD = "isEqual";
    private static final String FROM_STRING_METHOD = "fromString";
    private static final String FOR_NAME_METHOD = "forName";

    // Misc
    public static final String STRAND_VAR_NAME = "__strand";

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
            args.add(evaluationContext.getClassLoader());
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

    public static BExpressionValue make(SuspendedContext context, boolean val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, byte val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, char val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, short val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, int val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, long val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, float val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, double val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, String val) throws EvaluationException {
        val = removeRedundantQuotes(val);
        Value bStringVal = getAsBString(context, val);
        return new BExpressionValue(context, bStringVal);
    }

    /**
     * Converts the user given string literal into an {@link io.ballerina.runtime.api.values.BString} instance.
     *
     * @param context suspended debug context
     * @param val     string value
     * @return {@link io.ballerina.runtime.api.values.BString} instance
     */
    private static Value getAsBString(SuspendedContext context, String val) throws EvaluationException {
        List<ReferenceType> cls = context.getAttachedVm().classesByName(B_STRING_UTILS_CLASS);
        if (cls.isEmpty()) {
            cls = Collections.singletonList(loadClass(context, B_STRING_UTILS_CLASS, FROM_STRING_METHOD));
        }
        List<Method> methods = cls.get(0).methodsByName(FROM_STRING_METHOD);
        if (methods.isEmpty()) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Error " +
                    "occurred when trying to load required methods to execute the function: " + FROM_STRING_METHOD));
        }
        JvmStaticMethod jvmStaticMethod = new JvmStaticMethod(context, cls.get(0), methods.get(0), null,
                Collections.singletonList(context.getAttachedVm().mirrorOf(val)));
        return jvmStaticMethod.invoke();
    }

    private static boolean compare(List<String> list1, List<String> list2) {
        return list1.size() == list2.size() && IntStream.range(0, list1.size()).allMatch(i ->
                list1.get(i).equals(list2.get(i)));
    }
}
