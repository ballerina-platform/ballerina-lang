/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.nativeimpl.jvm.interop;

import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.jvm.values.XMLValue;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.J_BOOLEAN_OBJ_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.J_DOUBLE_OBJ_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.J_INTEGER_OBJ_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.J_LONG_OBJ_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.J_OBJECT_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.J_PRIMITIVE_BOOLEAN_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.J_PRIMITIVE_BYTE_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.J_PRIMITIVE_CHAR_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.J_PRIMITIVE_DOUBLE_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.J_PRIMITIVE_FLOAT_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.J_PRIMITIVE_INT_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.J_PRIMITIVE_LONG_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.J_PRIMITIVE_SHORT_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInterop.J_VOID_TNAME;
import static org.ballerinalang.nativeimpl.jvm.interop.JInteropException.CLASS_NOT_FOUND_REASON;
import static org.ballerinalang.nativeimpl.jvm.interop.JInteropException.OVERLOADED_METHODS_REASON;

/**
 * Responsible for resolving a Java method for a given {@code JMethodResolverRequest}.
 *
 * @since 1.0.0
 */
class JMethodResolver {

    private ClassLoader classLoader;

    JMethodResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    JMethod resolve(JMethodRequest jMethodRequest) {
        // 1) Get java methods (that matches with the method name) or constructor list
        List<JMethod> jMethods = resolveByMethodName(jMethodRequest.declaringClass,
                jMethodRequest.methodName, jMethodRequest.kind);

        // 2) If the above list is zero then throw an error
        if (jMethods.isEmpty()) {
            throw getMethodNotFoundError(jMethodRequest.kind, jMethodRequest.declaringClass, jMethodRequest.methodName);
        }

        // 3) Filter out the constructors or methods that have the same number of
        //      parameters as the number of constraints
        int paramCount = getBFuncParamCount(jMethodRequest, jMethods);
        jMethods = resolveByParamCount(jMethods, paramCount);

        // 4) If the above list is zero then throw an error
        if (jMethods.isEmpty()) {
            throw getMethodNotFoundError(jMethodRequest.kind, jMethodRequest.declaringClass,
                    jMethodRequest.methodName, paramCount);
        }

        // 5) Now resolve the most specific method using the constraints.
        JMethod jMethod = resolve(jMethodRequest, jMethods);

        validateMethodSignature(jMethodRequest, jMethod);

        return jMethod;
    }

    private List<JMethod> resolveByMethodName(Class<?> declaringClass,
                                              String methodName,
                                              JMethodKind kind) {
        return getExecutables(declaringClass, methodName, kind)
                .stream()
                .map(executable -> JMethod.build(kind, executable))
                .collect(Collectors.toList());
    }

    private List<JMethod> resolveByParamCount(List<JMethod> jMethods, int paramCount) {
        return jMethods.stream()
                .filter(jMethod -> jMethod.getParamTypes().length == paramCount)
                .collect(Collectors.toList());
    }

    private JMethod resolve(JMethodRequest jMethodRequest, List<JMethod> jMethods) {
        boolean noConstraints = noConstraintsSpecified(jMethodRequest.paramTypeConstraints);
        if (jMethods.size() == 1 && noConstraints) {
            return jMethods.get(0);
        } else if (noConstraints) {
            int paramCount = jMethods.get(0).getParamTypes().length;
            throw getOverloadedMethodExistError(jMethodRequest.kind, jMethodRequest.declaringClass,
                    jMethodRequest.methodName, paramCount);
        }

        JMethod jMethod = resolveExactMethod(jMethodRequest.declaringClass, jMethodRequest.methodName,
                jMethodRequest.kind, jMethodRequest.paramTypeConstraints);
        if (jMethod == JMethod.NO_SUCH_METHOD) {
            return resolveMatchingMethod(jMethodRequest, jMethods);
        }
        return jMethod;
    }

    private void validateMethodSignature(JMethodRequest jMethodRequest, JMethod jMethod) {
        validateExceptionTypes(jMethodRequest, jMethod);

        validateArgumentTypes(jMethodRequest, jMethod);

        validateReturnTypes(jMethodRequest, jMethod);
    }

    private void validateExceptionTypes(JMethodRequest jMethodRequest, JMethod jMethod) {
        Executable method = jMethod.getMethod();
        boolean throwsCheckedException = false;
        boolean returnsErrorValue;
        try {
            for (Class<?> exceptionType : method.getExceptionTypes()) {
                if (!this.classLoader.loadClass(RuntimeException.class.getCanonicalName())
                        .isAssignableFrom(exceptionType)) {
                    throwsCheckedException = true;
                    break;
                }
            }
            returnsErrorValue = method instanceof Method && (this.classLoader
                    .loadClass(ErrorValue.class.getCanonicalName())
                    .isAssignableFrom(((Method) method).getReturnType()) ||
                    this.classLoader.loadClass(Object.class.getCanonicalName())
                            .isAssignableFrom(((Method) method).getReturnType()));
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new JInteropException(CLASS_NOT_FOUND_REASON, e.getMessage(), e);
        }

        if ((throwsCheckedException && !jMethodRequest.returnsBErrorType) ||
                (jMethodRequest.returnsBErrorType && !throwsCheckedException && !returnsErrorValue)) {
            throw new JInteropException(JInteropException.METHOD_SIGNATURE_NOT_MATCH_REASON,
                    "No such Java method '" + jMethodRequest.methodName + "' which throws checked exception " +
                            "found in class '" + jMethodRequest.declaringClass + "'");
        }
    }

    private void validateArgumentTypes(JMethodRequest jMethodRequest, JMethod jMethod) {
        Class<?>[] jParamTypes = jMethod.getParamTypes();
        BType[] bParamTypes = jMethodRequest.bParamTypes;
        int i = 0;
        if (jMethod.isInstanceMethod() && bParamTypes.length > 0) {
            if (!isValidExpectedBType(jMethodRequest.declaringClass, bParamTypes[i], jMethodRequest)) {
                throw new JInteropException(JInteropException.METHOD_SIGNATURE_NOT_MATCH_REASON,
                        "No such Java method '" + jMethodRequest.methodName +
                                "' with method argument type '" + jParamTypes[i] + "' found in class '" +
                                jMethodRequest.declaringClass + "'");
            }
            i = 1;
        }

        for (int j = 0; j < jParamTypes.length; i++, j++) {
            if (!isValidExpectedBType(jParamTypes[j], bParamTypes[i], jMethodRequest)) {
                throw new JInteropException(JInteropException.METHOD_SIGNATURE_NOT_MATCH_REASON,
                        "No such Java method '" + jMethodRequest.methodName +
                                "' with method argument type '" + jParamTypes[j] + "' found in class '" +
                                jMethodRequest.declaringClass + "'");
            }
        }
    }

    private void validateReturnTypes(JMethodRequest jMethodRequest, JMethod jMethod) {
        Class<?> jReturnType = jMethod.getReturnType();
        BType bReturnType = jMethodRequest.bReturnType;
        if (!isValidExpectedBType(jReturnType, bReturnType, jMethodRequest)) {
            throw new JInteropException(JInteropException.METHOD_SIGNATURE_NOT_MATCH_REASON,
                    "No such Java method '" + jMethodRequest.methodName +
                            "' with method return type '" + jReturnType + "' found in class '" +
                            jMethodRequest.declaringClass + "'");
        }
    }

    private boolean isValidExpectedBType(Class<?> jParamType, BType bParamType, JMethodRequest jMethodRequest) {
        try {
            String jParamTypeName = jParamType.getTypeName();
            switch (bParamType.getTag()) {
                case TypeTags.HANDLE_TAG:
                case TypeTags.ANY_TAG:
                case TypeTags.ANYDATA_TAG:
                    return !jParamType.isPrimitive();
                case TypeTags.NULL_TAG:
                    return jParamTypeName.equals(J_VOID_TNAME);
                case TypeTags.INT_TAG:
                case TypeTags.BYTE_TAG:
                case TypeTags.FLOAT_TAG:
                    if (jParamTypeName.equals(J_OBJECT_TNAME)) {
                        return true;
                    }

                    if (bParamType.getTag() == TypeTags.INT_TAG && jParamTypeName.equals(J_LONG_OBJ_TNAME)) {
                        return true;
                    }

                    if (bParamType.getTag() == TypeTags.BYTE_TAG && jParamTypeName.equals(J_INTEGER_OBJ_TNAME)) {
                        return true;
                    }

                if (bParamType.getTag() == TypeTags.FLOAT_TAG && jParamTypeName.equals(J_DOUBLE_OBJ_TNAME)) {
                    return true;
                }

                return jParamType.isPrimitive() &&
                            (jParamTypeName.equals(J_PRIMITIVE_INT_TNAME) ||
                                    jParamTypeName.equals(J_PRIMITIVE_BYTE_TNAME) ||
                                    jParamTypeName.equals(J_PRIMITIVE_SHORT_TNAME) ||
                                    jParamTypeName.equals(J_PRIMITIVE_LONG_TNAME) ||
                                    jParamTypeName.equals(J_PRIMITIVE_CHAR_TNAME) ||
                                    jParamTypeName.equals(J_PRIMITIVE_FLOAT_TNAME) ||
                                    jParamTypeName.equals(J_PRIMITIVE_DOUBLE_TNAME));
                case TypeTags.BOOLEAN_TAG:
                    if (jParamTypeName.equals(J_OBJECT_TNAME) || jParamTypeName.equals(J_BOOLEAN_OBJ_TNAME)) {
                        return true;
                    }
                    return jParamType.isPrimitive() && jParamTypeName.equals(J_PRIMITIVE_BOOLEAN_TNAME);
                case TypeTags.DECIMAL_TAG:
                    return this.classLoader.loadClass(BigDecimal.class.getCanonicalName()).isAssignableFrom(jParamType);
                case TypeTags.STRING_TAG:
                    return this.classLoader.loadClass(String.class.getCanonicalName()).isAssignableFrom(jParamType);
                case TypeTags.MAP_TAG:
                case TypeTags.RECORD_TYPE_TAG:
                case TypeTags.JSON_TAG:
                    return this.classLoader.loadClass(MapValue.class.getCanonicalName()).isAssignableFrom(jParamType);
                case TypeTags.OBJECT_TYPE_TAG:
                case TypeTags.SERVICE_TAG:
                    return this.classLoader.loadClass(ObjectValue.class.getCanonicalName())
                            .isAssignableFrom(jParamType);
                case TypeTags.ERROR_TAG:
                    return this.classLoader.loadClass(ErrorValue.class.getCanonicalName()).isAssignableFrom(jParamType);
                case TypeTags.STREAM_TAG:
                    return this.classLoader.loadClass(StreamValue.class.getCanonicalName())
                            .isAssignableFrom(jParamType);
                case TypeTags.TABLE_TAG:
                    return this.classLoader.loadClass(TableValue.class.getCanonicalName()).isAssignableFrom(jParamType);
                case TypeTags.XML_TAG:
                    return this.classLoader.loadClass(XMLValue.class.getCanonicalName()).isAssignableFrom(jParamType);
                case TypeTags.TUPLE_TAG:
                case TypeTags.ARRAY_TAG:
                    if (jMethodRequest.restParamExist) {
                        return jParamType.isArray();
                    }
                    return this.classLoader.loadClass(ArrayValue.class.getCanonicalName()).isAssignableFrom(jParamType);
                case TypeTags.UNION_TAG:
                    List<BType> members = ((BUnionType) bParamType).getMemberTypes();
                    for (BType member : members) {
                        if (isValidExpectedBType(jParamType, member, jMethodRequest)) {
                            return true;
                        }
                    }
                    return !jParamType.isPrimitive();
            }
            return false;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new JInteropException(CLASS_NOT_FOUND_REASON, e.getMessage(), e);
        }
    }

    private JMethod resolveExactMethod(Class clazz, String name, JMethodKind kind, ParamTypeConstraint[] constraints) {
        Class<?>[] paramTypes = new Class<?>[constraints.length];
        for (int constraintIndex = 0; constraintIndex < constraints.length; constraintIndex++) {
            paramTypes[constraintIndex] = constraints[constraintIndex].get();
        }

        Executable executable = (kind == JMethodKind.CONSTRUCTOR) ? resolveConstructor(clazz, paramTypes) :
                resolveMethod(clazz, name, paramTypes);
        if (executable != null) {
            return JMethod.build(kind, (kind == JMethodKind.CONSTRUCTOR) ? resolveConstructor(clazz, paramTypes) :
                    resolveMethod(clazz, name, paramTypes));
        } else {
            return JMethod.NO_SUCH_METHOD;
        }
    }

    private JMethod resolveMatchingMethod(JMethodRequest jMethodRequest, List<JMethod> jMethods) {
        ParamTypeConstraint[] constraints = jMethodRequest.paramTypeConstraints;
        List<JMethod> resolvedJMethods = new ArrayList<>();
        for (JMethod jMethod : jMethods) {
            boolean resolved = true;
            Class<?>[] formalParamTypes = jMethod.getParamTypes();
            for (int paramIndex = 0; paramIndex < formalParamTypes.length; paramIndex++) {
                Class<?> formalParamType = formalParamTypes[paramIndex];
                if (formalParamType.isAssignableFrom(constraints[paramIndex].get())) {
                    continue;
                }
                resolved = false;
                break;
            }
            if (resolved) {
                resolvedJMethods.add(jMethod);
            }
        }

        if (resolvedJMethods.isEmpty()) {
            throw getMethodNotFoundError(jMethodRequest.kind, jMethodRequest.declaringClass,
                    jMethodRequest.methodName, constraints);
        } else if (resolvedJMethods.size() > 1) {
            throw getAmbiguousOverloadedMethodExistsError(jMethodRequest.kind, jMethodRequest.declaringClass,
                    jMethodRequest.methodName, constraints);
        } else {
            return resolvedJMethods.get(0);
        }
    }

    private Executable resolveConstructor(Class<?> clazz, Class<?>... paramTypes) {
        try {
            return clazz.getConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Executable resolveMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(name, paramTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private List<Executable> getExecutables(Class clazz, String methodName, JMethodKind kind) {
        return kind == JMethodKind.CONSTRUCTOR ? Arrays.asList(clazz.getConstructors()) :
                Arrays.stream(clazz.getMethods())
                        .filter(method -> method.getName().equals(methodName))
                        .collect(Collectors.toList());
    }

    private boolean noConstraintsSpecified(ParamTypeConstraint[] constraints) {
        for (ParamTypeConstraint constraint : constraints) {
            if (constraint != ParamTypeConstraint.NO_CONSTRAINT) {
                return false;
            }
        }
        return true;
    }

    private int getBFuncParamCount(JMethodRequest jMethodRequest, List<JMethod> jMethods) {
        int bFuncParamCount = jMethodRequest.bFuncParamCount;
        if (jMethodRequest.kind == JMethodKind.METHOD) {
            boolean isStaticMethod = jMethods.get(0).isStatic();
            // Remove the receiver parameter in instance methods.
            bFuncParamCount = isStaticMethod ? bFuncParamCount : bFuncParamCount - 1;
        }
        return bFuncParamCount;
    }

    private JInteropException getMethodNotFoundError(JMethodKind kind,
                                                     Class<?> declaringClass,
                                                     String methodName) {
        if (kind == JMethodKind.CONSTRUCTOR) {
            return new JInteropException(JInteropException.CONSTRUCTOR_NOT_FOUND_REASON,
                    "No such public constructor found in class '" + declaringClass + "'");
        } else {
            return new JInteropException(JInteropException.METHOD_NOT_FOUND_REASON,
                    "No such public method '" + methodName + "' found in class '" + declaringClass + "'");
        }
    }

    private JInteropException getMethodNotFoundError(JMethodKind kind,
                                                     Class<?> declaringClass,
                                                     String methodName,
                                                     int paramCount) {
        if (kind == JMethodKind.CONSTRUCTOR) {
            return new JInteropException(JInteropException.CONSTRUCTOR_NOT_FOUND_REASON,
                    "No such public constructor with '" + paramCount +
                            "' parameter(s) found in class '" + declaringClass + "'");
        } else {
            return new JInteropException(JInteropException.METHOD_NOT_FOUND_REASON,
                    "No such public method '" + methodName + "' with '" + paramCount +
                            "' parameter(s) found in class '" + declaringClass + "'");
        }
    }

    private JInteropException getMethodNotFoundError(JMethodKind kind,
                                                     Class<?> declaringClass,
                                                     String methodName,
                                                     ParamTypeConstraint[] constraints) {
        String paramTypesSig = getParamTypesAsString(constraints);
        if (kind == JMethodKind.CONSTRUCTOR) {
            return new JInteropException(JInteropException.CONSTRUCTOR_NOT_FOUND_REASON,
                    "No such public constructor that matches with parameter types '" + paramTypesSig +
                            "' found in class '" + declaringClass + "'");
        } else {
            return new JInteropException(JInteropException.METHOD_NOT_FOUND_REASON,
                    "No such public method '" + methodName + "' that matches with parameter types '" +
                            paramTypesSig + "' found in class '" + declaringClass + "'");
        }
    }

    private JInteropException getOverloadedMethodExistError(JMethodKind kind,
                                                            Class<?> declaringClass,
                                                            String methodName,
                                                            int paramCount) {
        if (kind == JMethodKind.CONSTRUCTOR) {
            return new JInteropException(OVERLOADED_METHODS_REASON,
                    "Overloaded constructors with '" + paramCount + "' parameter(s) in class '" +
                            declaringClass + "', please specify class names for each parameter " +
                            "in 'paramTypes' field in the annotation");
        } else {
            return new JInteropException(OVERLOADED_METHODS_REASON,
                    "Overloaded methods '" + methodName + "' with '" + paramCount + "' parameter(s) in class '" +
                            declaringClass + "', please specify class names for each parameter " +
                            "with 'paramTypes' field in the annotation");
        }
    }

    private JInteropException getAmbiguousOverloadedMethodExistsError(JMethodKind kind,
                                                                      Class<?> declaringClass,
                                                                      String methodName,
                                                                      ParamTypeConstraint[] constraints) {
        String paramTypesSig = getParamTypesAsString(constraints);
        if (kind == JMethodKind.CONSTRUCTOR) {
            return new JInteropException(OVERLOADED_METHODS_REASON,
                    "More than one public constructors that match with the parameter types '" + paramTypesSig +
                            "' found in class '" + declaringClass + "'");
        } else {
            return new JInteropException(OVERLOADED_METHODS_REASON,
                    "More than one public methods '" + methodName + "' that match with the parameter types '" +
                            paramTypesSig + "' found in class '" + declaringClass + "'");
        }
    }

    private String getParamTypesAsString(ParamTypeConstraint[] constraints) {
        StringJoiner stringJoiner = new StringJoiner(",", "(", ")");
        for (ParamTypeConstraint paramTypeConstraint : constraints) {
            stringJoiner.add(paramTypeConstraint.get().getName());
        }
        return stringJoiner.toString();
    }
}
