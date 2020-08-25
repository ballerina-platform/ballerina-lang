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

import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;

import java.lang.reflect.Field;

/**
 * Java interop validation class for both field access and method invocations.
 *
 * @since 1.2.0
 */
public class InteropValidator {

    private ClassLoader classLoader;
    private boolean isEntryModuleValidation;
    JMethodResolver methodResolver;

    public InteropValidator(ClassLoader classLoader, SymbolTable symbolTable) {

        this.classLoader = classLoader;
        this.methodResolver = new JMethodResolver(classLoader, symbolTable);
    }

    /**
     * Method that validates Java interop functions and link them with Java methods.
     *
     * @param methodValidationRequest the methodValidationRequest
     * @return validated and linked java method representation
     */
    JMethod validateAndGetJMethod(InteropValidationRequest.MethodValidationRequest methodValidationRequest) {
        // Populate JMethodRequest from the BValue
        JMethodRequest jMethodRequest = JMethodRequest.build(methodValidationRequest, classLoader,
                isEntryModuleValidation);
        return this.methodResolver.resolve(jMethodRequest);
    }

    /**
     * Method that check Java interop function has caller.
     *
     * @param className Java interop class name
     * @param methodName Java interop method name
     * @param bFunctionType Java interop function type
     * @return whether we need to pass the callenv arg or not.
     */
    public boolean checkCallerEnvParam(String className, String methodName,
                                       BInvokableType bFunctionType) {

        Class<?> clazz = JInterop.loadClass(className, classLoader);
        return this.methodResolver.checkCallerEnvParam(clazz, methodName, bFunctionType);
    }

    /**
     * Method that validates Java interop functions and link them with Java fields.
     *
     * @param fieldValidationRequest the fieldValidationRequest
     * @return validated and linked java field representation
     */
    JavaField validateAndGetJField(InteropValidationRequest.FieldValidationRequest fieldValidationRequest) {
        // 1) Load Java class  - validate
        JFieldMethod method = fieldValidationRequest.fieldMethod;
        String className = fieldValidationRequest.klass;
        Class clazz = JInterop.loadClass(className, classLoader);

        // 2) Load Java method details - use the method kind in the request - validate kind and the existence of the
        // method. Possible there may be more than one methods for the given kind and the name
        String fieldName = fieldValidationRequest.name;
        JavaField javaField;
        try {
            Field field = clazz.getField(fieldName);
            javaField = new JavaField(method, field);
        } catch (NoSuchFieldException e) {
            throw new JInteropException(DiagnosticCode.FIELD_NOT_FOUND, "No such field '" + fieldName +
                    "' found in class '" + className + "'");
        }

        return javaField;
    }

    boolean isEntryModuleValidation() {

        return isEntryModuleValidation;
    }

    public void setEntryModuleValidation(boolean entryModuleValidation) {

        isEntryModuleValidation = entryModuleValidation;
    }
}
