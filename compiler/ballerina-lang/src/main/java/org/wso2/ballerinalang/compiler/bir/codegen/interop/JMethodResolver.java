/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.codegen.interop;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BHandle;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BXML;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.ballerinalang.util.diagnostic.DiagnosticCode.CLASS_NOT_FOUND;
import static org.ballerinalang.util.diagnostic.DiagnosticCode.OVERLOADED_METHODS;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.J_BOOLEAN_OBJ_TNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.J_DOUBLE_OBJ_TNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.J_INTEGER_OBJ_TNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.J_LONG_OBJ_TNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.J_OBJECT_TNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.J_PRIMITIVE_BOOLEAN_TNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.J_PRIMITIVE_BYTE_TNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.J_PRIMITIVE_CHAR_TNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.J_PRIMITIVE_DOUBLE_TNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.J_PRIMITIVE_FLOAT_TNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.J_PRIMITIVE_INT_TNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.J_PRIMITIVE_LONG_TNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.J_PRIMITIVE_SHORT_TNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.J_STRING_TNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.JInterop.J_VOID_TNAME;

/**
 * Responsible for resolving a Java method for a given {@code JMethodResolverRequest}.
 *
 * @since 1.2.0
 */
class JMethodResolver {

    private ClassLoader classLoader;
    private SymbolTable symbolTable;
    private final BType[] definedReadOnlyMemberTypes;

    JMethodResolver(ClassLoader classLoader, SymbolTable symbolTable) {

        this.classLoader = classLoader;
        this.symbolTable = symbolTable;
        this.definedReadOnlyMemberTypes = new BType[]{
                symbolTable.nilType,
                symbolTable.booleanType,
                symbolTable.intType,
                symbolTable.signed8IntType,
                symbolTable.signed16IntType,
                symbolTable.signed32IntType,
                symbolTable.unsigned32IntType,
                symbolTable.unsigned16IntType,
                symbolTable.unsigned8IntType,
                symbolTable.floatType,
                symbolTable.decimalType,
                symbolTable.stringType,
                symbolTable.charStringType
        };
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
        jMethods = resolveByParamCount(jMethods, jMethodRequest);

        // 4) If the above list is zero then throw an error
        if (jMethods.isEmpty()) {
            throw getMethodNotFoundError(jMethodRequest.kind, jMethodRequest.declaringClass,
                                         jMethodRequest.methodName, jMethodRequest.bFuncParamCount);
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
                .map(executable -> JMethod.build(kind, executable, null))
                .collect(Collectors.toList());
    }

    private List<JMethod> resolveByParamCount(List<JMethod> jMethods, JMethodRequest jMethodRequest) {
        return jMethods.stream()
                .filter(jMethod -> hasEqualParamCounts(jMethodRequest, jMethod))
                .collect(Collectors.toList());
    }

    private boolean hasEqualParamCounts(JMethodRequest jMethodRequest, JMethod jMethod) {
        int expectedCount = getBFuncParamCount(jMethodRequest, jMethod);
        int count = jMethod.getParamTypes().length;
        if (count == expectedCount) {
            return true;
        } else if (count == expectedCount + 1) {
            // This is for object interop functions when self is passed as a parameter
            if (jMethodRequest.receiverType != null) {
                jMethod.setReceiverType(jMethodRequest.receiverType);
                return true;
            } else if (jMethod.isStatic()) {
                return jMethod.isBalEnvAcceptingMethod();
            }
        }
        return false;
    }

    private JMethod resolve(JMethodRequest jMethodRequest, List<JMethod> jMethods) {
        boolean noConstraints = noConstraintsSpecified(jMethodRequest.paramTypeConstraints);
        if (jMethods.size() == 1 && noConstraints) {
            return jMethods.get(0);
        } else if (noConstraints) {
            Optional<JMethod> covariantRetTypeMethod = findCovariantReturnTypeMethod(jMethods);
            if (covariantRetTypeMethod.isPresent()) {
                return covariantRetTypeMethod.get();
            }

            int paramCount = jMethods.get(0).getParamTypes().length;
            throw getOverloadedMethodExistError(jMethodRequest.kind, jMethodRequest.declaringClass,
                    jMethodRequest.methodName, paramCount);
        }

        JMethod jMethod = resolveExactMethod(jMethodRequest.declaringClass, jMethodRequest.methodName,
                jMethodRequest.kind, jMethodRequest.paramTypeConstraints, jMethodRequest.receiverType);
        if (jMethod == JMethod.NO_SUCH_METHOD) {
            return resolveMatchingMethod(jMethodRequest, jMethods);
        }
        return jMethod;
    }

    private Optional<JMethod> findCovariantReturnTypeMethod(List<JMethod> jMethods) {
        for (int i = 0; i < jMethods.size(); i++) {
            for (int k = i + 1; k < jMethods.size(); k++) {
                JMethod ithMethod = jMethods.get(i);
                JMethod kthMethod = jMethods.get(k);

                if (ithMethod.getReturnType().isAssignableFrom(kthMethod.getReturnType()) ||
                        kthMethod.getReturnType().isAssignableFrom(ithMethod.getReturnType())) {
                    if (ithMethod.getParamTypes().length != kthMethod.getParamTypes().length) {
                        // This occurs when there are static methods and instance methods and the static method
                        // has one more parameter than the instance method. Also this occurs when an interop
                        // method in an object maps to instance methods of which one accepting self and another
                        // that doesn't.
                        throw new JInteropException(
                                OVERLOADED_METHODS, "Overloaded methods cannot be differentiated. Please specify the " +
                                "parameterTypes for each parameter in 'paramTypes' field in the annotation");
                    }
                    return Optional.of(ithMethod);
                }
            }
        }
        return Optional.empty();
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
                    .loadClass(BError.class.getCanonicalName())
                    .isAssignableFrom(((Method) method).getReturnType()) ||
                    this.classLoader.loadClass(Object.class.getCanonicalName())
                            .isAssignableFrom(((Method) method).getReturnType()));
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new JInteropException(CLASS_NOT_FOUND, e.getMessage(), e);
        }

        if ((throwsCheckedException && !jMethodRequest.returnsBErrorType) ||
                (jMethodRequest.returnsBErrorType && !throwsCheckedException && !returnsErrorValue)) {
            throw new JInteropException(DiagnosticCode.METHOD_SIGNATURE_DOES_NOT_MATCH,
                    "No such Java method '" + jMethodRequest.methodName + "' which throws checked exception " +
                            "found in class '" + jMethodRequest.declaringClass + "'");
        }
    }

    private void validateArgumentTypes(JMethodRequest jMethodRequest, JMethod jMethod) {

        Class<?>[] jParamTypes = jMethod.getParamTypes();
        BType[] bParamTypes = jMethodRequest.bParamTypes;
        int bParamCount = bParamTypes.length;
        int i = 0;
        int j = 0;
        if (jMethod.getReceiverType() != null) {
            Class<?> jParamType = jParamTypes[0];
            BType bParamType = jMethod.getReceiverType();
            if (!isValidParamBType(jParamTypes[0], bParamType, false, jMethodRequest.restParamExist)) {
                throw getNoSuchMethodError(jMethodRequest.methodName, jParamType, bParamType,
                                           jMethodRequest.declaringClass);
            }
            bParamCount = bParamCount + 1;
            j++;
        }

        if (jMethod.isInstanceMethod()) {
            if (bParamCount != jParamTypes.length + 1) {
                throw getParamCountMismatchError(jMethodRequest);
            }

            BType receiverType = bParamTypes[0];
            boolean isLastParam = bParamTypes.length == 1;
            if (!isValidParamBType(jMethodRequest.declaringClass, receiverType, isLastParam,
                    jMethodRequest.restParamExist)) {
                throw getNoSuchMethodError(jMethodRequest.methodName, jParamTypes[0], receiverType,
                                           jMethodRequest.declaringClass);
            }
            i++;
        } else if (jMethod.isBalEnvAcceptingMethod()) {
            j++;
        } else if (bParamCount != jParamTypes.length) {
            throw getParamCountMismatchError(jMethodRequest);
        }

        for (int k = j; k < jParamTypes.length; i++, k++) {
            BType bParamType = bParamTypes[i];
            Class<?> jParamType = jParamTypes[k];
            boolean isLastPram = jParamTypes.length == k + 1;
            if (!isValidParamBType(jParamType, bParamType, isLastPram, jMethodRequest.restParamExist)) {
                throw getNoSuchMethodError(jMethodRequest.methodName, jParamType, bParamType,
                        jMethodRequest.declaringClass);
            }
        }
    }

    private void validateReturnTypes(JMethodRequest jMethodRequest, JMethod jMethod) {

        Class<?> jReturnType = jMethod.getReturnType();
        BType bReturnType = jMethodRequest.bReturnType;
        if (!isValidReturnBType(jReturnType, bReturnType, jMethodRequest) &&
                !(jMethod.isBalEnvAcceptingMethod() && jReturnType.equals(void.class))) {
            throw new JInteropException(DiagnosticCode.METHOD_SIGNATURE_DOES_NOT_MATCH,
                                        "Incompatible return type for method '" + jMethodRequest.methodName +
                                                "' in class '" +
                                                jMethodRequest.declaringClass.getName() + "': Java type '" +
                                                jReturnType.getName() +
                                                "' will not be matched to ballerina type '" +
                                                (bReturnType.tag == TypeTags.FINITE ? bReturnType.tsymbol.name.value :
                                                        bReturnType) + "'");
        }
    }

    private boolean isValidParamBType(Class<?> jType, BType bType, boolean isLastParam, boolean restParamExist) {

        try {
            String jTypeName = jType.getTypeName();
            switch (bType.tag) {
                case TypeTags.ANY:
                case TypeTags.ANYDATA:
                    if (jTypeName.equals(J_STRING_TNAME)) {
                        return false;
                    }
                    return !jType.isPrimitive();
                case TypeTags.HANDLE:
                    return !jType.isPrimitive();
                case TypeTags.NIL:
                    return jTypeName.equals(J_VOID_TNAME);
                case TypeTags.INT:
                case TypeTags.SIGNED32_INT:
                case TypeTags.SIGNED16_INT:
                case TypeTags.SIGNED8_INT:
                case TypeTags.UNSIGNED32_INT:
                case TypeTags.UNSIGNED16_INT:
                case TypeTags.UNSIGNED8_INT:
                case TypeTags.BYTE:
                case TypeTags.FLOAT:
                    if (jTypeName.equals(J_OBJECT_TNAME)) {
                        return true;
                    }

                    if (TypeTags.isIntegerTypeTag(bType.tag) && jTypeName.equals(J_LONG_OBJ_TNAME)) {
                        return true;
                    }

                    if (bType.tag == TypeTags.BYTE && jTypeName.equals(J_INTEGER_OBJ_TNAME)) {
                        return true;
                    }

                    if (bType.tag == TypeTags.FLOAT && jTypeName.equals(J_DOUBLE_OBJ_TNAME)) {
                        return true;
                    }

                    return jType.isPrimitive() && (jTypeName.equals(J_PRIMITIVE_INT_TNAME) ||
                            jTypeName.equals(J_PRIMITIVE_BYTE_TNAME) || jTypeName.equals(J_PRIMITIVE_SHORT_TNAME) ||
                            jTypeName.equals(J_PRIMITIVE_LONG_TNAME) || jTypeName.equals(J_PRIMITIVE_CHAR_TNAME) ||
                            jTypeName.equals(J_PRIMITIVE_FLOAT_TNAME) || jTypeName.equals(J_PRIMITIVE_DOUBLE_TNAME));
                case TypeTags.BOOLEAN:
                    if (jTypeName.equals(J_OBJECT_TNAME) || jTypeName.equals(J_BOOLEAN_OBJ_TNAME)) {
                        return true;
                    }
                    return jType.isPrimitive() && jTypeName.equals(J_PRIMITIVE_BOOLEAN_TNAME);
                case TypeTags.DECIMAL:
                    return this.classLoader.loadClass(BDecimal.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.STRING:
                case TypeTags.CHAR_STRING:
                    return this.classLoader.loadClass(BString.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.MAP:
                case TypeTags.RECORD:
                    return this.classLoader.loadClass(BMap.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.JSON:
                    return jTypeName.equals(J_OBJECT_TNAME);
                case TypeTags.OBJECT:
                    return this.classLoader.loadClass(BObject.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.ERROR:
                    return this.classLoader.loadClass(BError.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.XML:
                case TypeTags.XML_ELEMENT:
                case TypeTags.XML_PI:
                case TypeTags.XML_COMMENT:
                case TypeTags.XML_TEXT:
                    return this.classLoader.loadClass(BXML.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.TUPLE:
                case TypeTags.ARRAY:
                    return isValidListType(jType, isLastParam, restParamExist);
                case TypeTags.UNION:
                    if (jTypeName.equals(J_OBJECT_TNAME)) {
                        return true;
                    }

                    Set<BType> members = ((BUnionType) bType).getMemberTypes();
                    // for method arguments, all ballerina member types should be assignable to java-type.
                    for (BType member : members) {
                        if (!isValidParamBType(jType, member, isLastParam, restParamExist)) {
                            return false;
                        }
                    }
                    return true;
                case TypeTags.READONLY:
                    return jTypeName.equals(J_OBJECT_TNAME);
                case TypeTags.INTERSECTION:
                    return isValidParamBType(jType, ((BIntersectionType) bType).effectiveType, isLastParam,
                            restParamExist);
                case TypeTags.FINITE:
                    if (jTypeName.equals(J_OBJECT_TNAME)) {
                        return true;
                    }

                    Set<BLangExpression> valueSpace = ((BFiniteType) bType).getValueSpace();
                    for (Iterator<BLangExpression> iterator = valueSpace.iterator(); iterator.hasNext(); ) {
                        BLangExpression value = iterator.next();
                        if (!isValidParamBType(jType, value.type, isLastParam, restParamExist)) {
                            return false;
                        }
                    }
                    return true;
                case TypeTags.FUNCTION_POINTER:
                case TypeTags.INVOKABLE:
                    return this.classLoader.loadClass(BFunctionPointer.class
                            .getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.FUTURE:
                    return this.classLoader.loadClass(BFuture.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.TYPEDESC:
                    return this.classLoader.loadClass(BTypedesc.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.STREAM:
                    return this.classLoader.loadClass(BStream.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.TABLE:
                    return this.classLoader.loadClass(BTable.class.getCanonicalName()).isAssignableFrom(jType);
                default:
                    return false;
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new JInteropException(CLASS_NOT_FOUND, e.getMessage(), e);
        }
    }

    private boolean isValidReturnBType(Class<?> jType, BType bType, JMethodRequest jMethodRequest) {

        try {
            String jTypeName = jType.getTypeName();
            switch (bType.tag) {
                case TypeTags.ANY:
                case TypeTags.ANYDATA:
                    if (jTypeName.equals(J_STRING_TNAME)) {
                        return false;
                    }
                    return !jType.isPrimitive();
                case TypeTags.HANDLE:
                    return !jType.isPrimitive();
                case TypeTags.NIL:
                    return jTypeName.equals(J_VOID_TNAME);
                case TypeTags.INT:
                case TypeTags.SIGNED32_INT:
                case TypeTags.SIGNED16_INT:
                case TypeTags.SIGNED8_INT:
                case TypeTags.UNSIGNED32_INT:
                case TypeTags.UNSIGNED16_INT:
                case TypeTags.UNSIGNED8_INT:
                    if (jTypeName.equals(J_OBJECT_TNAME)) {
                        return true;
                    }

                    if (jType.isPrimitive()) {
                        return (jTypeName.equals(J_PRIMITIVE_INT_TNAME) || jTypeName.equals(J_PRIMITIVE_BYTE_TNAME) ||
                                jTypeName.equals(J_PRIMITIVE_SHORT_TNAME) || jTypeName.equals(J_PRIMITIVE_LONG_TNAME) ||
                                jTypeName.equals(J_PRIMITIVE_CHAR_TNAME));
                    } else {
                        return jTypeName.equals(J_LONG_OBJ_TNAME);
                    }
                case TypeTags.BYTE:
                    if (jTypeName.equals(J_OBJECT_TNAME)) {
                        return true;
                    }

                    if (jType.isPrimitive()) {
                        return jTypeName.equals(J_PRIMITIVE_BYTE_TNAME);
                    } else {
                        return jTypeName.equals(J_INTEGER_OBJ_TNAME);
                    }
                case TypeTags.FLOAT:
                    if (jTypeName.equals(J_OBJECT_TNAME)) {
                        return true;
                    }

                    if (jType.isPrimitive()) {
                        return (jTypeName.equals(J_PRIMITIVE_INT_TNAME) || jTypeName.equals(J_PRIMITIVE_BYTE_TNAME) ||
                                jTypeName.equals(J_PRIMITIVE_SHORT_TNAME) || jTypeName.equals(J_PRIMITIVE_LONG_TNAME) ||
                                jTypeName.equals(J_PRIMITIVE_CHAR_TNAME) || jTypeName.equals(J_PRIMITIVE_FLOAT_TNAME) ||
                                jTypeName.equals(J_PRIMITIVE_DOUBLE_TNAME));
                    } else {
                        return jTypeName.equals(J_DOUBLE_OBJ_TNAME);
                    }
                case TypeTags.BOOLEAN:
                    if (jTypeName.equals(J_OBJECT_TNAME) || jTypeName.equals(J_BOOLEAN_OBJ_TNAME)) {
                        return true;
                    }
                    return jType.isPrimitive() && jTypeName.equals(J_PRIMITIVE_BOOLEAN_TNAME);
                case TypeTags.DECIMAL:
                    return this.classLoader.loadClass(BDecimal.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.STRING:
                case TypeTags.CHAR_STRING:
                    return this.classLoader.loadClass(BString.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.MAP:
                case TypeTags.RECORD:
                    return this.classLoader.loadClass(BMap.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.JSON:
                    if (jTypeName.equals(J_OBJECT_TNAME)) {
                        return true;
                    }

                    for (BType member : getJSONMemberTypes()) {
                        if (isValidReturnBType(jType, member, jMethodRequest)) {
                            return true;
                        }
                    }

                    return false;
                case TypeTags.OBJECT:
                    return this.classLoader.loadClass(BObject.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.ERROR:
                    return this.classLoader.loadClass(BError.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.XML:
                case TypeTags.XML_ELEMENT:
                case TypeTags.XML_PI:
                case TypeTags.XML_COMMENT:
                case TypeTags.XML_TEXT:
                    return this.classLoader.loadClass(BXML.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.TUPLE:
                case TypeTags.ARRAY:
                    return isValidListType(jType, true, jMethodRequest.restParamExist);
                case TypeTags.UNION:
                    if (jTypeName.equals(J_OBJECT_TNAME)) {
                        return true;
                    }

                    Set<BType> members = ((BUnionType) bType).getMemberTypes();
                    // for method return, java-type should be matched to at-least one of the ballerina member types.
                    for (BType member : members) {
                        if (isValidReturnBType(jType, member, jMethodRequest)) {
                            return true;
                        }
                    }
                    return false;
                case TypeTags.READONLY:
                    return isReadOnlyCompatibleReturnType(jType, jMethodRequest);
                case TypeTags.INTERSECTION:
                    return isValidReturnBType(jType, ((BIntersectionType) bType).effectiveType, jMethodRequest);
                case TypeTags.FINITE:
                    if (jTypeName.equals(J_OBJECT_TNAME)) {
                        return true;
                    }

                    Set<BLangExpression> valueSpace = ((BFiniteType) bType).getValueSpace();
                    for (BLangExpression value : valueSpace) {
                        if (isValidReturnBType(jType, value.type, jMethodRequest)) {
                            return true;
                        }
                    }
                    return false;
                case TypeTags.FUNCTION_POINTER:
                case TypeTags.INVOKABLE:
                    return this.classLoader.loadClass(BFunctionPointer.class.getCanonicalName())
                            .isAssignableFrom(jType);
                case TypeTags.FUTURE:
                    return this.classLoader.loadClass(BFuture.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.TYPEDESC:
                    return this.classLoader.loadClass(BTypedesc.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.STREAM:
                    return this.classLoader.loadClass(BStream.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.TABLE:
                    return this.classLoader.loadClass(BTable.class.getCanonicalName()).isAssignableFrom(jType);
                default:
                    return false;
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new JInteropException(CLASS_NOT_FOUND, e.getMessage(), e);
        }
    }

    private boolean isValidListType(Class<?> jType, boolean isLastParam, boolean restParamExists)
            throws ClassNotFoundException {
        if (isLastParam && restParamExists) {
            return jType.isArray();
        }
        return this.classLoader.loadClass(BArray.class.getCanonicalName()).isAssignableFrom(jType);
    }

    private BType[] getJSONMemberTypes() {
        // TODO can't we use a static instance of this?
        return new BType[]{
                this.symbolTable.nilType, this.symbolTable.stringType, this.symbolTable.intType,
                this.symbolTable.floatType, this.symbolTable.booleanType, this.symbolTable.mapJsonType,
                this.symbolTable.arrayJsonType};
    }

    private boolean isReadOnlyCompatibleReturnType(Class<?> jType, JMethodRequest jMethodRequest)
            throws ClassNotFoundException {
        if (jType.getTypeName().equals(J_OBJECT_TNAME)) {
            return true;
        }

        for (BType member : definedReadOnlyMemberTypes) {
            if (isValidReturnBType(jType, member, jMethodRequest)) {
                return true;
            }
        }

        return isAssignableFrom(BError.class, jType) ||
                isAssignableFrom(BFunctionPointer.class, jType) ||
                isAssignableFrom(BObject.class, jType) ||
                isAssignableFrom(BTypedesc.class, jType) ||
                isAssignableFrom(BHandle.class, jType) ||
                isAssignableFrom(BXML.class, jType) ||
                this.isValidListType(jType, true, jMethodRequest.restParamExist) ||
                isAssignableFrom(BMap.class, jType) ||
                isAssignableFrom(BTable.class, jType);
    }

    private boolean isAssignableFrom(Class<?> targetType, Class<?> jType) throws ClassNotFoundException {
        return this.classLoader.loadClass(targetType.getCanonicalName()).isAssignableFrom(jType);
    }

    private JMethod resolveExactMethod(Class<?> clazz, String name, JMethodKind kind,
                                       ParamTypeConstraint[] constraints,
                                       BType receiverType) {

        Class<?>[] paramTypes = new Class<?>[constraints.length];
        for (int constraintIndex = 0; constraintIndex < constraints.length; constraintIndex++) {
            paramTypes[constraintIndex] = constraints[constraintIndex].get();
        }

        Executable executable = (kind == JMethodKind.CONSTRUCTOR) ? resolveConstructor(clazz, paramTypes) :
                resolveMethod(clazz, name, paramTypes);
        if (executable == null) {
            executable = tryResolveExactWithBalEnv(paramTypes, clazz, name);
        }
        if (executable != null) {
            return JMethod.build(kind, executable, receiverType);
        } else {
            return JMethod.NO_SUCH_METHOD;
        }
    }

    private Executable tryResolveExactWithBalEnv(Class<?>[] paramTypes, Class<?> clazz, String name) {
        Class<?>[] paramTypesWithBalEnv = new Class<?>[paramTypes.length + 1];
        System.arraycopy(paramTypes, 0, paramTypesWithBalEnv, 1, paramTypes.length);
        paramTypesWithBalEnv[0] = Environment.class;
        return resolveMethod(clazz, name, paramTypesWithBalEnv);
    }

    private JMethod resolveMatchingMethod(JMethodRequest jMethodRequest, List<JMethod> jMethods) {

        ParamTypeConstraint[] constraints = jMethodRequest.paramTypeConstraints;
        int constraintsSize;
        int paramTypesInitialIndex;
        if (jMethodRequest.receiverType != null) {
            constraintsSize = constraints.length + 1;
            paramTypesInitialIndex = 1;
        } else {
            constraintsSize = constraints.length;
            paramTypesInitialIndex = 0;
        }
        List<JMethod> resolvedJMethods = new ArrayList<>();
        if (constraints.length > 0) {
            for (JMethod jMethod : jMethods) {
                boolean resolved = true;
                Class<?>[] formalParamTypes = jMethod.getParamTypes();

                // skip if the given constraint params are not of the same size as method's params
                if (constraintsSize != formalParamTypes.length) {
                    continue;
                }

                for (int paramIndex = paramTypesInitialIndex, constraintIndex = 0; paramIndex < formalParamTypes.length;
                     paramIndex++, constraintIndex++) {
                    Class<?> formalParamType = formalParamTypes[paramIndex];
                    if (formalParamType.isAssignableFrom(constraints[constraintIndex].get())) {
                        continue;
                    }
                    resolved = false;
                    break;
                }
                if (resolved) {
                    resolvedJMethods.add(jMethod);
                }
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

    private List<Executable> getExecutables(Class<?> clazz, String methodName, JMethodKind kind) {

        return kind == JMethodKind.CONSTRUCTOR ? Arrays.asList(clazz.getConstructors()) :
                Arrays.stream(clazz.getMethods())
                        .filter(method -> method.getName().equals(methodName))
                        .collect(Collectors.toList());
    }

    private boolean noConstraintsSpecified(ParamTypeConstraint[] constraints) {
        if (constraints == null) {
            return true;
        }
        if (constraints.length == 0) {
            return false;
        }
        for (ParamTypeConstraint constraint : constraints) {
            if (constraint != ParamTypeConstraint.NO_CONSTRAINT) {
                return false;
            }
        }
        return true;
    }

    private int getBFuncParamCount(JMethodRequest jMethodRequest, JMethod jMethod) {

        int bFuncParamCount = jMethodRequest.bFuncParamCount;
        if (jMethodRequest.kind == JMethodKind.METHOD) {
            boolean isStaticMethod = jMethod.isStatic();
            // Remove the receiver parameter in instance methods.
            bFuncParamCount = isStaticMethod ? bFuncParamCount : bFuncParamCount - 1;
        }
        return bFuncParamCount;
    }

    private JInteropException getMethodNotFoundError(JMethodKind kind, Class<?> declaringClass, String methodName) {

        if (kind == JMethodKind.CONSTRUCTOR) {
            return new JInteropException(DiagnosticCode.CONSTRUCTOR_NOT_FOUND,
                                         "No such public constructor found in class '" + declaringClass + "'");
        } else {
            return new JInteropException(DiagnosticCode.METHOD_NOT_FOUND,
                                         "No such public method '" + methodName + "' found in class '" +
                                                 declaringClass + "'");
        }
    }

    private JInteropException getMethodNotFoundError(JMethodKind kind,
                                                     Class<?> declaringClass,
                                                     String methodName,
                                                     int paramCount) {

        if (kind == JMethodKind.CONSTRUCTOR) {
            return new JInteropException(DiagnosticCode.CONSTRUCTOR_NOT_FOUND,
                    "No such public constructor with '" + paramCount +
                            "' parameter(s) found in class '" + declaringClass + "'");
        } else {
            return new JInteropException(DiagnosticCode.METHOD_NOT_FOUND,
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
            return new JInteropException(DiagnosticCode.CONSTRUCTOR_NOT_FOUND,
                    "No such public constructor that matches with parameter types '" + paramTypesSig +
                            "' found in class '" + declaringClass + "'");
        } else {
            return new JInteropException(DiagnosticCode.METHOD_NOT_FOUND,
                    "No such public method '" + methodName + "' that matches with parameter types '" +
                            paramTypesSig + "' found in class '" + declaringClass + "'");
        }
    }

    private JInteropException getOverloadedMethodExistError(JMethodKind kind,
                                                            Class<?> declaringClass,
                                                            String methodName,
                                                            int paramCount) {

        if (kind == JMethodKind.CONSTRUCTOR) {
            return new JInteropException(OVERLOADED_METHODS,
                    "Overloaded constructors with '" + paramCount + "' parameter(s) in class '" +
                            declaringClass + "', please specify class names for each parameter " +
                            "in 'paramTypes' field in the annotation");
        } else {
            return new JInteropException(OVERLOADED_METHODS,
                    "Overloaded methods '" + methodName + "' with '" + paramCount +
                            "' parameter(s) in class '" + declaringClass +
                            "', please specify class names for each parameter " +
                            "with 'paramTypes' field in the annotation");
        }
    }

    private JInteropException getAmbiguousOverloadedMethodExistsError(JMethodKind kind,
                                                                      Class<?> declaringClass,
                                                                      String methodName,
                                                                      ParamTypeConstraint[] constraints) {

        String paramTypesSig = getParamTypesAsString(constraints);
        if (kind == JMethodKind.CONSTRUCTOR) {
            return new JInteropException(OVERLOADED_METHODS,
                    "More than one public constructors that match with the parameter types '" +
                            paramTypesSig + "' found in class '" + declaringClass + "'");
        } else {
            return new JInteropException(OVERLOADED_METHODS,
                    "More than one public methods '" + methodName +
                            "' that match with the parameter types '" + paramTypesSig +
                            "' found in class '" + declaringClass + "'");
        }
    }

    private String getParamTypesAsString(ParamTypeConstraint[] constraints) {

        StringJoiner stringJoiner = new StringJoiner(",", "(", ")");
        for (ParamTypeConstraint paramTypeConstraint : constraints) {
            stringJoiner.add(paramTypeConstraint.get().getName());
        }
        return stringJoiner.toString();
    }

    private JInteropException getNoSuchMethodError(String methodName, Class<?> jType, BType bType,
                                                   Class<?> declaringClass) {

        return new JInteropException(DiagnosticCode.METHOD_SIGNATURE_DOES_NOT_MATCH,
                "Incompatible param type for method '" + methodName + "' in class '" + declaringClass.getName() +
                        "': Java type '" + jType.getName() + "' will not be matched to ballerina type '" +
                        (bType.tag == TypeTags.FINITE ? bType.tsymbol.name.value : bType) + "'");
    }

    private JInteropException getParamCountMismatchError(JMethodRequest jMethodRequest) {

        return new JInteropException(DiagnosticCode.METHOD_SIGNATURE_DOES_NOT_MATCH,
                "Parameter count does not match with Java method '" + jMethodRequest.methodName + "' found in class '" +
                        jMethodRequest.declaringClass.getName() + "'");
    }
}
