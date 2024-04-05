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
import io.ballerina.runtime.api.values.BRegexpValue;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BXml;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import static org.ballerinalang.util.diagnostic.DiagnosticErrorCode.CLASS_NOT_FOUND;
import static org.ballerinalang.util.diagnostic.DiagnosticErrorCode.NO_CLASS_DEF_FOUND;
import static org.ballerinalang.util.diagnostic.DiagnosticErrorCode.OVERLOADED_METHODS;
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

    private final ClassLoader classLoader;
    private final SymbolTable symbolTable;
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
            if (jMethodRequest.kind == JMethodKind.CONSTRUCTOR) {
                throw new JInteropException(DiagnosticErrorCode.CONSTRUCTOR_NOT_FOUND,
                        "No such public constructor found in class '" + jMethodRequest.declaringClass.getName() + "'");
            } else {
                throw new JInteropException(DiagnosticErrorCode.METHOD_NOT_FOUND,
                        "No such public method '" + jMethodRequest.methodName + "' found in class '" +
                                jMethodRequest.declaringClass.getName() + "'");
            }
        }

        // 3) Filter out the constructors or methods that have the same number of
        //      parameters as the number of constraints
        jMethods = resolveByParamCount(jMethods, jMethodRequest);

        // 4) If the above list is zero then throw an error
        if (jMethods.isEmpty()) {
            throwMethodNotFoundError(jMethodRequest);
        }

        // 5) Now resolve the most specific method using the constraints.
        JMethod jMethod = resolve(jMethodRequest, jMethods);

        validateMethodSignature(jMethodRequest, jMethod);

        return jMethod;
    }

    private List<JMethod> resolveByMethodName(Class<?> declaringClass,
                                              String methodName,
                                              JMethodKind kind) {
        List<JMethod> list = new ArrayList<>();
        for (Executable executable : getExecutables(declaringClass, methodName, kind)) {
            JMethod build = JMethod.build(kind, executable, null);
            list.add(build);
        }
        return list;
    }

    private List<JMethod> resolveByParamCount(List<JMethod> jMethods, JMethodRequest jMethodRequest) {
        List<JMethod> list = new ArrayList<>();
        for (JMethod jMethod : jMethods) {
            if (hasEqualParamCounts(jMethodRequest, jMethod)
                    || hasEquivalentPathAndFunctionParamCount(jMethodRequest, jMethod)
                    || hasEquivalentPathParamCount(jMethodRequest, jMethod)
                    || hasEquivalentFunctionParamCount(jMethodRequest, jMethod)) {
                list.add(jMethod);
            }
        }
        return list;
    }

    private boolean hasEqualParamCounts(JMethodRequest jMethodRequest, JMethod jMethod) {
        int expectedCount = getBFuncParamCount(jMethodRequest, jMethod);
        int count = jMethod.getParamTypes().length;
        if (count == expectedCount) {
            return true;
        } else if (count == expectedCount + 1) {
            // This is for object interop functions when self is passed as a parameter
            if (jMethod.isBalEnvAcceptingMethod()) {
                return true;
            }
            jMethod.setReceiverType(jMethodRequest.receiverType);
            return jMethodRequest.receiverType != null;
        } else if (count == expectedCount + 2) {
            // This is for object interop functions when both BalEnv and self is passed as parameters.
            if (jMethodRequest.receiverType != null) {
                jMethod.setReceiverType(jMethodRequest.receiverType);
            }
            return jMethod.isBalEnvAcceptingMethod();
        }
        return false;
    }

    private boolean hasEquivalentPathAndFunctionParamCount(JMethodRequest jMethodRequest, JMethod jMethod) {
        Class<?>[] paramTypes = jMethod.getParamTypes();
        int count = paramTypes.length;
        if (jMethodRequest.receiverType == null || jMethodRequest.pathParamCount == 0 || count < 3 || count > 4) {
            return false;
        }
        if (!isParamAssignableToBArray(paramTypes[count - 1]) || !isParamAssignableToBArray(paramTypes[count - 2])
                || isFirstPathParamARestParam(jMethodRequest, jMethod)
                || isFirstFunctionParamARestParam(jMethodRequest, jMethod)) {
            return false;
        }
        if (count == 3) {
            // This is for object interop functions when self is passed as a parameter
            // Expected jMethod parameters are [BObject, BArray, BArray].
            jMethod.setReceiverType(jMethodRequest.receiverType);
            return true;
        }
        // This is for object interop functions when both BalEnv and self is passed as parameters along with
        // bundled path parameters and function parameters.
        // Expected jMethod parameters are [Environment, BObject, BArray, BArray].
        jMethod.setReceiverType(jMethodRequest.receiverType);
        return jMethod.isBalEnvAcceptingMethod();
    }

    private boolean hasEquivalentPathParamCount(JMethodRequest jMethodRequest, JMethod jMethod) {
        if (jMethodRequest.receiverType == null || jMethodRequest.pathParamCount == 0
                || isFirstPathParamARestParam(jMethodRequest, jMethod)) {
            return false;
        }
        Class<?>[] paramTypes = jMethod.getParamTypes();
        int count = paramTypes.length;
        int reducedParamCount = getBundledPathParamCount(jMethodRequest, jMethod);
        if (count < reducedParamCount || count > reducedParamCount + 2) {
            return false;
        }
        if (count == reducedParamCount && paramTypes.length > 0 && isParamAssignableToBArray(paramTypes[0])) {
            return true;
        } else if ((count == (reducedParamCount + 1)) && paramTypes.length > 1 &&
                isParamAssignableToBArray(paramTypes[1])) {
            // This is for object interop functions when self is passed as a parameter
            jMethod.setReceiverType(jMethodRequest.receiverType);
            return true;
        } else if ((count == (reducedParamCount + 2)) && paramTypes.length > 2 &&
                isParamAssignableToBArray(paramTypes[2])) {
            // This is for object interop functions when both BalEnv and self is passed as parameters.
            jMethod.setReceiverType(jMethodRequest.receiverType);
            return jMethod.isBalEnvAcceptingMethod();
        }
        return false;
    }

    private boolean hasEquivalentFunctionParamCount(JMethodRequest jMethodRequest, JMethod jMethod) {
        // Currently, this is only applicable for resource and remote methods which have empty path parameters.
        // If path parameters are present, this will be handled by 'hasEquivalentPathAndFunctionParamCount' method
        // by bundling both.
        if (jMethodRequest.receiverType == null || jMethodRequest.pathParamCount != 0
                || jMethodRequest.bParamTypes.length == 0) {
            return false;
        }
        Class<?>[] paramTypes = jMethod.getParamTypes();
        boolean isFirstParamServiceObject = jMethodRequest.bParamTypes[0].tag == TypeTags.SERVICE;
        // Get the count of parameters of the resource/remote methods by excluding the receiver type.
        int count = isFirstParamServiceObject ? paramTypes.length - 1 : paramTypes.length;
        int reducedParamCount = isFirstParamServiceObject ? 2 : 1;
        if (count < reducedParamCount || count > reducedParamCount + 2) {
            return false;
        }
        if (!isParamAssignableToBArray(paramTypes[count - 1])
                || isFirstFunctionParamARestParam(jMethodRequest, jMethod)) {
            return false;
        }
        if (count == reducedParamCount) {
            return true;
        } else if (count == (reducedParamCount + 1)) {
            // This is for object interop functions when self is passed as a parameter
            jMethod.setReceiverType(jMethodRequest.receiverType);
            return true;
        }
        // This is for object interop functions when both BalEnv and self is passed as parameters.
        jMethod.setReceiverType(jMethodRequest.receiverType);
        return jMethod.isBalEnvAcceptingMethod();
    }

    private boolean isParamAssignableToBArray(Class<?> paramType) {
        try {
            return this.classLoader.loadClass(BArray.class.getCanonicalName()).isAssignableFrom(paramType);
        } catch (ClassNotFoundException e) {
            throw new JInteropException(CLASS_NOT_FOUND, e.getMessage(), e);
        }
    }

    private JMethod resolve(JMethodRequest jMethodRequest, List<JMethod> jMethods) {
        boolean noConstraints = noConstraintsSpecified(jMethodRequest.paramTypeConstraints);
        if (jMethods.size() == 1 && noConstraints) {
            return jMethods.get(0);
        } else if (noConstraints) {
            if (areAllMethodsOverridden(jMethods, jMethodRequest.declaringClass)) {
                return jMethods.get(0);
            }
            throwOverloadedMethodError(jMethodRequest, jMethods.get(0).getParamTypes().length);
        }

        JMethod jMethod = resolveExactMethod(jMethodRequest.declaringClass, jMethodRequest.methodName,
                jMethodRequest.kind, jMethodRequest.paramTypeConstraints, jMethodRequest.receiverType);
        if (jMethod == JMethod.NO_SUCH_METHOD) {
            return resolveMatchingMethod(jMethodRequest, jMethods);
        }
        return jMethod;
    }

    private boolean areAllMethodsOverridden(List<JMethod> jMethods, Class<?> clazz) {
        if (jMethods.get(0).getKind() == JMethodKind.CONSTRUCTOR) {
            return false;
        }
        for (int i = 0; i < jMethods.size(); i++) {
            Method method1 = (Method) jMethods.get(i).getMethod();
            for (int k = i + 1; k < jMethods.size(); k++) {
                Method method2 = (Method) jMethods.get(k).getMethod();
                if (!isOverridden(method1, method2, clazz)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isOverridden(Method method1, Method method2, Class<?> clazz) {
        if (method1.getParameterCount() != method2.getParameterCount()) {
            // This occurs when there are static methods and instance methods, and the static method has one more
            // parameter than the instance method. Additionally, this occurs when an interop method in an object
            // maps to instance methods, one accepting `self` and another that doesn't.
            throw new JInteropException(OVERLOADED_METHODS, "Overloaded methods cannot be differentiated. " +
                    "Please specify the parameter types for each parameter in 'paramTypes' field in the annotation");
        }
        // Returns false if return types are not covariant
        Method currentMethod;
        Method otherMethod;
        if (method2.getReturnType().isAssignableFrom(method1.getReturnType())) {
            currentMethod = method1;
            otherMethod = method2;
        } else if (method1.getReturnType().isAssignableFrom(method2.getReturnType())) {
            currentMethod = method2;
            otherMethod = method1;
        } else {
            return false;
        }

        try {
            Method superMethod = clazz.getSuperclass()
                    .getDeclaredMethod(currentMethod.getName(), currentMethod.getParameterTypes());
            return Arrays.equals(superMethod.getParameterTypes(), otherMethod.getParameterTypes()) &&
                    superMethod.getReturnType().equals(otherMethod.getReturnType());
        } catch (NoSuchMethodException e) {
            return false;
        }
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

        BType returnType = jMethodRequest.bReturnType;
        String expectedRetTypeName;
        if (throwsCheckedException && !jMethodRequest.returnsBErrorType) {
            expectedRetTypeName = getExpectedReturnType(returnType);
            throw new JInteropException(DiagnosticErrorCode.METHOD_SIGNATURE_DOES_NOT_MATCH,
                    "Incompatible ballerina return type for Java method '" + jMethodRequest.methodName + "' which " +
                            "throws checked exception found in class '" + jMethodRequest.declaringClass.getName() +
                            "': expected '" + expectedRetTypeName + "', found '" + returnType + "'");
        } else if (jMethodRequest.returnsBErrorType && !throwsCheckedException && !returnsErrorValue) {
            String errorMsgPart;
            if (returnType instanceof BUnionType bUnionReturnType) {
                BType modifiedRetType = BUnionType.create(null, getNonErrorMembers(bUnionReturnType));
                errorMsgPart = "expected '" + modifiedRetType + "', found '" + returnType + "'";
            } else {
                errorMsgPart = "no return type expected but found '" + returnType + "'";
            }
            throw new JInteropException(DiagnosticErrorCode.METHOD_SIGNATURE_DOES_NOT_MATCH,
                    "Incompatible ballerina return type for Java method '" + jMethodRequest.methodName + "' which " +
                            "throws 'java.lang.RuntimeException' found in class '" +
                            jMethodRequest.declaringClass.getName() + "': " + errorMsgPart);
        }
    }

    private String getExpectedReturnType(BType retType) {
        if (retType.tag == TypeTags.NIL || (retType instanceof BTypeReferenceType &&
                ((BTypeReferenceType) retType).referredType.tag == TypeTags.ERROR)) {
            return "error";
        } else if (retType instanceof BUnionType bUnionReturnType) {
            BType modifiedRetType = BUnionType.create(null, getNonErrorMembers(bUnionReturnType));
            return modifiedRetType + "|error";
        } else {
            return retType + "|error";
        }
    }

    private LinkedHashSet<BType> getNonErrorMembers(BUnionType bUnionReturnType) {
        LinkedHashSet<BType> memTypes = new LinkedHashSet<>();
        for (BType bType : bUnionReturnType.getMemberTypes()) {
            if (bType.tag != TypeTags.ERROR && !(bType instanceof BTypeReferenceType &&
                    ((BTypeReferenceType) bType).referredType.tag == TypeTags.ERROR)) {
                memTypes.add(bType);
            }
        }
        return memTypes;
    }

    private void validateArgumentTypes(JMethodRequest jMethodRequest, JMethod jMethod) {

        Class<?>[] jParamTypes = jMethod.getParamTypes();
        // Bundle path parameters into an anydata array and the rest of the function parameters into an any array
        // if the resolved Java method accepts a BArray parameter for path and function parameters.
        if (hasEquivalentPathAndFunctionParamCount(jMethodRequest, jMethod)) {
            bundleBothPathAndFunctionParameter(jMethodRequest, jMethod);
        } else if (hasEquivalentPathParamCount(jMethodRequest, jMethod)) {
            bundlePathParams(jMethodRequest, jMethod);
        } else if (hasEquivalentFunctionParamCount(jMethodRequest, jMethod)) {
            bundleFunctionParams(jMethodRequest, jMethod);
        }
        BType[] bParamTypes = jMethodRequest.bParamTypes;
        int bParamCount = bParamTypes.length;
        int i = 0;
        int j = 0;
        // Validate receiver type
        if (jMethod.getReceiverType() != null) {
            Class<?> jParamType;
            if (jMethod.isBalEnvAcceptingMethod()) {
                jParamType = jParamTypes[1];
            } else {
                jParamType = jParamTypes[0];
            }
            BType bParamType = jMethod.getReceiverType();
            if (!isValidParamBType(jParamType, bParamType, false, jMethodRequest.restParamExist)) {
                throwNoSuchMethodError(jMethodRequest.methodName, jParamType, bParamType,
                                           jMethodRequest.declaringClass);
            }
            bParamCount = bParamCount + 1;
            j++;
        }

        if (jMethod.isInstanceMethod()) {
            if (bParamCount != jParamTypes.length + 1) {
                if (jMethod.isBalEnvAcceptingMethod()) {
                    j++;
                } else {
                    throwParamCountMismatchError(jMethodRequest);
                }
            }

            int receiverIndex = 0;
            if (!jMethodRequest.pathParamSymbols.isEmpty()) {
                receiverIndex = jMethodRequest.pathParamCount;
            }
            BType receiverType = bParamTypes[receiverIndex];
            if (receiverType.tag != TypeTags.HANDLE) {
                throwMethodNotFoundError(jMethodRequest);
            }
            for (int k = receiverIndex; k < bParamTypes.length - 1; k++) {
                bParamTypes[k] = bParamTypes[k + 1];
            }
            BType[] bParamTypesWithoutReceiver = new BType[bParamTypes.length - 1];
            System.arraycopy(bParamTypes, 0, bParamTypesWithoutReceiver, 0, bParamTypesWithoutReceiver.length);
            bParamTypes = bParamTypesWithoutReceiver;
        } else if (bParamCount != jParamTypes.length) {
            if (jMethod.isBalEnvAcceptingMethod()) {
                j++;
            } else {
                throwParamCountMismatchError(jMethodRequest);
            }
        }

        for (int k = j; k < jParamTypes.length; i++, k++) {
            BType bParamType = bParamTypes[i];
            Class<?> jParamType = jParamTypes[k];
            boolean isLastPram = jParamTypes.length == k + 1;
            if (!isValidParamBType(jParamType, bParamType, isLastPram, jMethodRequest.restParamExist)) {
                throwNoSuchMethodError(jMethodRequest.methodName, jParamType, bParamType,
                        jMethodRequest.declaringClass);
            }
        }
    }

    private void bundlePathParams(JMethodRequest jMethodRequest, JMethod jMethod) {
        List<BVarSymbol> pathParamSymbols = jMethodRequest.pathParamSymbols;
        if (pathParamSymbols.isEmpty()) {
            return;
        }
        List<BType> paramTypes = new ArrayList<>(Arrays.asList(jMethodRequest.bParamTypes));
        int initialPathParamIndex = paramTypes.indexOf(pathParamSymbols.get(0).type);
        for (BVarSymbol param : pathParamSymbols) {
            paramTypes.remove(param.type);
        }
        paramTypes.add(initialPathParamIndex, new BArrayType(symbolTable.anydataType));
        jMethodRequest.bParamTypes = paramTypes.toArray(new BType[0]);
        jMethodRequest.bFuncParamCount = jMethodRequest.bFuncParamCount - pathParamSymbols.size() + 1;
        jMethodRequest.pathParamCount = 1;
        jMethod.hasBundledPathParams = true;
    }

    private void bundleFunctionParams(JMethodRequest jMethodRequest, JMethod jMethod) {
        List<BType> paramTypes = new ArrayList<>(Arrays.asList(jMethodRequest.bParamTypes));
        if (jMethodRequest.bFuncParamCount > jMethodRequest.pathParamCount) {
            paramTypes.subList(jMethodRequest.pathParamCount, jMethodRequest.bFuncParamCount).clear();
        }
        paramTypes.add(new BArrayType(symbolTable.anyType));
        jMethodRequest.bParamTypes = paramTypes.toArray(new BType[0]);
        jMethodRequest.bFuncParamCount = jMethodRequest.pathParamCount + 1;
        jMethod.hasBundledFunctionParams = true;
    }

    private void bundleBothPathAndFunctionParameter(JMethodRequest jMethodRequest, JMethod jMethod) {
        List<BType> paramTypes = new ArrayList<>();
        paramTypes.add(new BArrayType(symbolTable.anydataType));
        paramTypes.add(new BArrayType(symbolTable.anyType));
        jMethodRequest.bParamTypes = paramTypes.toArray(new BType[0]);
        jMethodRequest.bFuncParamCount = 2;
        jMethodRequest.pathParamCount = 1;
        jMethod.hasBundledFunctionParams = true;
        jMethod.hasBundledPathParams = true;
    }

    private void validateReturnTypes(JMethodRequest jMethodRequest, JMethod jMethod) {

        Class<?> jReturnType = jMethod.getReturnType();
        BType bReturnType = jMethodRequest.bReturnType;
        if (!isValidReturnBType(jReturnType, bReturnType, jMethodRequest) &&
                !(jMethod.isBalEnvAcceptingMethod() && jReturnType.equals(void.class))) {
            throw new JInteropException(DiagnosticErrorCode.METHOD_SIGNATURE_DOES_NOT_MATCH,
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
        bType = JvmCodeGenUtil.getImpliedType(bType);
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
                case TypeTags.READONLY:
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
                    return this.classLoader.loadClass(BXml.class.getCanonicalName()).isAssignableFrom(jType);
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
                case TypeTags.FINITE:
                    if (jTypeName.equals(J_OBJECT_TNAME)) {
                        return true;
                    }

                    Set<BLangExpression> valueSpace = ((BFiniteType) bType).getValueSpace();
                    for (BLangExpression value : valueSpace) {
                        if (!isValidParamBType(jType, value.getBType(), isLastParam, restParamExist)) {
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
                case TypeTags.REGEXP:
                    return this.classLoader.loadClass(BRegexpValue.class.getCanonicalName()).isAssignableFrom(jType);
                default:
                    return false;
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new JInteropException(CLASS_NOT_FOUND, e.getMessage(), e);
        }
    }

    private boolean isValidReturnBType(Class<?> jType, BType bType, JMethodRequest jMethodRequest) {

        LinkedHashSet<Class<?>> visitedSet = new LinkedHashSet<>();
        return isValidReturnBType(jType, bType, jMethodRequest, visitedSet);
    }

    private boolean isValidReturnBType(Class<?> jType, BType bType, JMethodRequest jMethodRequest,
                                       LinkedHashSet<Class<?>> visitedSet) {
        bType = JvmCodeGenUtil.getImpliedType(bType);
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

                    if (!visitedSet.add(jType)) {
                        return true;
                    }

                    return isValidReturnBType(jType, symbolTable.jsonType, jMethodRequest, visitedSet);
                case TypeTags.OBJECT:
                    return this.classLoader.loadClass(BObject.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.ERROR:
                    return this.classLoader.loadClass(BError.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.XML:
                case TypeTags.XML_ELEMENT:
                case TypeTags.XML_PI:
                case TypeTags.XML_COMMENT:
                case TypeTags.XML_TEXT:
                    return this.classLoader.loadClass(BXml.class.getCanonicalName()).isAssignableFrom(jType);
                case TypeTags.TUPLE:
                case TypeTags.ARRAY:
                    return isValidListType(jType, true, jMethodRequest.restParamExist);
                case TypeTags.UNION:
                    if (jTypeName.equals(J_OBJECT_TNAME)) {
                        return true;
                    }

                    if (!visitedSet.add(jType)) {
                        return true;
                    }

                    Set<BType> members = ((BUnionType) bType).getMemberTypes();
                    // for method return, java-type should be matched to at-least one of the ballerina member types.
                    for (BType member : members) {
                        if (isValidReturnBType(jType, member, jMethodRequest, visitedSet)) {
                            return true;
                        }
                    }
                    return false;
                case TypeTags.READONLY:
                    return isReadOnlyCompatibleReturnType(jType, jMethodRequest);
                case TypeTags.FINITE:
                    if (jTypeName.equals(J_OBJECT_TNAME)) {
                        return true;
                    }

                    Set<BLangExpression> valueSpace = ((BFiniteType) bType).getValueSpace();
                    for (BLangExpression value : valueSpace) {
                        if (isValidReturnBType(jType, value.getBType(), jMethodRequest, visitedSet)) {
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
                isAssignableFrom(BXml.class, jType) ||
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
            if (kind == JMethodKind.CONSTRUCTOR) {
                return JMethod.build(kind, executable, null);
            }
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
        String paramTypesSig = getParamTypesAsString(constraints);
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
            if (jMethodRequest.kind == JMethodKind.CONSTRUCTOR) {
                throw new JInteropException(DiagnosticErrorCode.CONSTRUCTOR_NOT_FOUND,
                        "No such public constructor that matches with parameter types '" + paramTypesSig +
                                "' found in class '" + jMethodRequest.declaringClass.getName() + "'");
            } else {
                throw new JInteropException(DiagnosticErrorCode.METHOD_NOT_FOUND,
                        "No such public method '" + jMethodRequest.methodName + "' that matches with parameter types " +
                                "'" + paramTypesSig + "' found in class '" + jMethodRequest.declaringClass.getName() +
                                "'");
            }
        } else if (resolvedJMethods.size() > 1) {
            if (jMethodRequest.kind == JMethodKind.CONSTRUCTOR) {
                throw new JInteropException(OVERLOADED_METHODS,
                        "More than one public constructors that match with the parameter types '" +
                                paramTypesSig + "' found in class '" + jMethodRequest.declaringClass + "'");
            } else {
                throw new JInteropException(OVERLOADED_METHODS,
                        "More than one public methods '" + jMethodRequest.methodName +
                                "' that match with the parameter types '" + paramTypesSig +
                                "' found in class '" + jMethodRequest.declaringClass + "'");
            }
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

        if (kind == JMethodKind.CONSTRUCTOR) {
            if (Modifier.isAbstract(clazz.getModifiers())) {
                throw new JInteropException(DiagnosticErrorCode.INSTANTIATION_ERROR,
                        "'" + clazz.getName() + "' is abstract, and cannot be instantiated");
            }
            return Arrays.asList(getConstructors(clazz));
        } else {
            List<Executable> list = new ArrayList<>();
            for (Method method : getMethods(clazz)) {
                if (method.getName().equals(methodName)) {
                    list.add(method);
                }
            }
            return list;
        }
    }

    private Method[] getMethods(Class<?> clazz) {
        try {
            return clazz.getMethods();
        } catch (NoClassDefFoundError e) {
            throw new JInteropException(NO_CLASS_DEF_FOUND, "Class definition '" + e.getMessage() + "' not found");
        }
    }

    private Constructor<?>[] getConstructors(Class<?> clazz) {
        try {
            return clazz.getConstructors();
        } catch (NoClassDefFoundError e) {
            throw new JInteropException(NO_CLASS_DEF_FOUND, "Class definition '" + e.getMessage() + "' not found");
        }
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

    private int getBundledPathParamCount(JMethodRequest jMethodRequest, JMethod jMethod) {
        return getBFuncParamCount(jMethodRequest, jMethod) - jMethodRequest.pathParamCount + 1;
    }

    private boolean isFirstPathParamARestParam(JMethodRequest jMethodRequest, JMethod jMethod) {
        if (jMethodRequest.kind != JMethodKind.METHOD) {
            return false;
        }
        return jMethod.isStatic() ? jMethodRequest.bParamTypes[0].tag == TypeTags.ARRAY :
                jMethodRequest.bParamTypes[1].tag == TypeTags.ARRAY &&
                        jMethodRequest.bParamTypes[0].tag == TypeTags.HANDLE;
    }

    private boolean isFirstFunctionParamARestParam(JMethodRequest jMethodRequest, JMethod jMethod) {
        return jMethod.isStatic() ? jMethodRequest.bParamTypes[jMethodRequest.pathParamCount].tag == TypeTags.ARRAY :
                jMethodRequest.bParamTypes[jMethodRequest.pathParamCount + 1].tag == TypeTags.ARRAY &&
                        jMethodRequest.bParamTypes[0].tag == TypeTags.HANDLE;
    }

    private String getParamTypesAsString(ParamTypeConstraint[] constraints) {

        StringJoiner stringJoiner = new StringJoiner(",", "(", ")");
        for (ParamTypeConstraint paramTypeConstraint : constraints) {
            stringJoiner.add(paramTypeConstraint.get().getName());
        }
        return stringJoiner.toString();
    }

    private void throwMethodNotFoundError(JMethodRequest jMethodRequest) throws JInteropException {

        if (jMethodRequest.kind == JMethodKind.CONSTRUCTOR) {
            throw new JInteropException(DiagnosticErrorCode.CONSTRUCTOR_NOT_FOUND,
                    "No such public constructor with '" + jMethodRequest.bFuncParamCount +
                            "' parameter(s) found in class '" + jMethodRequest.declaringClass.getName() + "'");
        } else {
            if (jMethodRequest.bFuncParamCount == 0 || jMethodRequest.bParamTypes[0].tag != TypeTags.HANDLE) {
                throw new JInteropException(DiagnosticErrorCode.METHOD_NOT_FOUND,
                        "No such public static method '" + jMethodRequest.methodName + "' with '" +
                                jMethodRequest.bFuncParamCount +
                                "' parameter(s) found in class '" + jMethodRequest.declaringClass.getName() + "'");
            } else {
                throw new JInteropException(DiagnosticErrorCode.METHOD_NOT_FOUND,
                        "No such public method '" + jMethodRequest.methodName + "' with '" +
                                jMethodRequest.bFuncParamCount +
                                "' parameter(s) found in class '" + jMethodRequest.declaringClass.getName() + "'");
            }
        }
    }

    private void throwNoSuchMethodError(String methodName, Class<?> jType, BType bType,
                                                   Class<?> declaringClass) throws JInteropException {

        throw new JInteropException(DiagnosticErrorCode.METHOD_SIGNATURE_DOES_NOT_MATCH,
                "Incompatible param type for method '" + methodName + "' in class '" + declaringClass.getName() +
                        "': Java type '" + jType.getName() + "' will not be matched to ballerina type '" +
                        (bType.tag == TypeTags.FINITE ? bType.tsymbol.name.value : bType) + "'");
    }

    private void throwParamCountMismatchError(JMethodRequest jMethodRequest) throws JInteropException {

        throw new JInteropException(DiagnosticErrorCode.METHOD_SIGNATURE_DOES_NOT_MATCH,
                "Parameter count does not match with Java method '" + jMethodRequest.methodName +
                        "' found in class '" + jMethodRequest.declaringClass.getName() + "'");
    }

    private void throwOverloadedMethodError(JMethodRequest jMethodRequest, int paramCount) throws JInteropException {
        if (jMethodRequest.kind == JMethodKind.CONSTRUCTOR) {
            throw new JInteropException(OVERLOADED_METHODS, "Overloaded constructors with '" + paramCount +
                    "' parameter(s) in class '" + jMethodRequest.declaringClass.getName() +
                    "', please specify the parameter types for each parameter in 'paramTypes' field in the annotation");
        } else {
            throw new JInteropException(OVERLOADED_METHODS, "Overloaded methods '" + jMethodRequest.methodName +
                    "' with '" + paramCount + "' parameter(s) in class '" + jMethodRequest.declaringClass.getName() +
                    "', please specify the parameter types for each parameter in 'paramTypes' field in the annotation");
        }
    }
}
