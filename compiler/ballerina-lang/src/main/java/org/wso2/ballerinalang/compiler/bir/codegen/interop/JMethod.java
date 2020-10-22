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
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.util.diagnostic.DiagnosticCode.CLASS_NOT_FOUND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;

/**
 * Represents a java method in this implementation.
 *
 * @since 1.2.0
 */
class JMethod {

    static final JMethod NO_SUCH_METHOD = new JMethod(null, null, null);
    public static final String BAL_ENV_CANONICAL_NAME = Environment.class.getCanonicalName();

    JMethodKind kind;
    private Executable method;
    private BType receiverType;

    private JMethod(JMethodKind kind, Executable executable, BType receiverType) {

        this.kind = kind;
        this.method = executable;
        this.receiverType = receiverType;
    }

    static JMethod build(JMethodKind kind, Executable executable, BType receiverType) {

        return new JMethod(kind, executable, receiverType);
    }


    String getClassName() {

        return method.getDeclaringClass().getName();
    }

    boolean isDeclaringClassInterface() {

        return this.method.getDeclaringClass().isInterface();
    }

    boolean isStatic() {

        return Modifier.isStatic(method.getModifiers());
    }

    boolean isInstanceMethod() {

        return !isStatic() && !(method instanceof Constructor);
    }

    String getName() {

        if (kind == JMethodKind.CONSTRUCTOR) {
            return JVM_INIT_METHOD;
        } else {
            return method.getName();
        }
    }

    JMethodKind getKind() {

        return kind;
    }

    Executable getMethod() {

        return method;
    }

    String getSignature() {

        if (kind == JMethodKind.CONSTRUCTOR) {
            return JInterop.getMethodSig(void.class, method.getParameterTypes());
        } else {
            return JInterop.getMethodSig(getReturnType(), method.getParameterTypes());
        }
    }

    Class<?>[] getParamTypes() {
        return method.getParameterTypes();
    }

    Class<?> getReturnType() {

        if (kind == JMethodKind.CONSTRUCTOR) {
            return method.getDeclaringClass();
        } else {
            return ((Method) method).getReturnType();
        }
    }

    public BType getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(BType receiverType) {
        this.receiverType = receiverType;
    }

    BArray getExceptionTypes(ClassLoader classLoader) {

        List<Class> checkedExceptions = new ArrayList<>();
        try {
            Class<?> runtimeException = classLoader.loadClass(RuntimeException.class.getCanonicalName());
            for (Class<?> exceptionType : method.getExceptionTypes()) {
                if (!runtimeException.isAssignableFrom(exceptionType)) {
                    checkedExceptions.add(exceptionType);
                }
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new JInteropException(CLASS_NOT_FOUND, e.getMessage(), e);
        }

        BArray arrayValue = ValueCreator
                .createArrayValue(TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING), checkedExceptions.size());
        int i = 0;
        for (Class<?> exceptionType : checkedExceptions) {
            arrayValue.add(i++, exceptionType.getName().replace(".", "/"));
        }
        return arrayValue;
    }

    Class<?>[] getExceptionTypes() {

        List<Class<?>> checkedExceptions = new ArrayList<>();
        try {
            Class<?> runtimeException = ClassLoader.getSystemClassLoader().
                    loadClass(RuntimeException.class.getCanonicalName());
            for (Class<?> exceptionType : method.getExceptionTypes()) {
                if (!runtimeException.isAssignableFrom(exceptionType)) {
                    checkedExceptions.add(exceptionType);
                }
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new JInteropException(CLASS_NOT_FOUND, e.getMessage(), e);
        }
        return checkedExceptions.toArray(new Class[0]);
    }

    public boolean isBalEnvAcceptingMethod() {
        Class<?>[] paramTypes = getParamTypes();
        return paramTypes.length > 0 && paramTypes[0].getCanonicalName().equals(BAL_ENV_CANONICAL_NAME);
    }
}
