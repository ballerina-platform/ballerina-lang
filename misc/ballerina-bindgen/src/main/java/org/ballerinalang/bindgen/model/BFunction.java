/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bindgen.model;

import org.ballerinalang.bindgen.utils.BindgenEnv;

import java.util.LinkedList;
import java.util.List;

/**
 * The class that holds the mapping for a function definition in the Ballerina bridge code generation.
 *
 * @since 2.0.0
 */
public abstract class BFunction {

    private final BFunctionKind functionType;
    private final BindgenEnv env;
    private String externalFunctionName;
    private String externalReturnType;
    private String functionName;
    private Class<?> declaringClass;
    private List<JParameter> parameters = new LinkedList<>();
    private final List<JError> throwables = new LinkedList<>();
    private boolean isStatic = true;
    private String returnType;
    private String errorType = null;

    BFunction(BFunctionKind functionType, BindgenEnv env) {
        this.functionType = functionType;
        this.env = env;
    }

    public BFunctionKind getKind() {
        return functionType;
    }

    public BindgenEnv getEnv() {
        return env;
    }

    void setExternalFunctionName(String name) {
        this.externalFunctionName = name;
    }

    public String getExternalFunctionName() {
        return externalFunctionName;
    }

    void setFunctionName(String name) {
        this.functionName = name;
    }

    public String getFunctionName() {
        return functionName;
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    void setDeclaringClass(Class<?> declaringClass) {
        this.declaringClass = declaringClass;
    }

    public List<JParameter> getParameters() {
        return parameters;
    }

    void setParameters(List<JParameter> parameters) {
        this.parameters = parameters;
    }

    public List<JError> getThrowables() {
        return throwables;
    }

    void setThrowable(JError throwable) {
        this.throwables.add(throwable);
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public String getExternalReturnType() {
        return externalReturnType;
    }

    void setExternalReturnType(String externalReturnType) {
        this.externalReturnType = externalReturnType;
    }

    public String getReturnType() {
        return returnType;
    }

    void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getErrorType() {
        return errorType;
    }

    void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    /**
     * The enum that stores the function kind in the Ballerina bridge code generation.
     */
    public enum BFunctionKind {
        METHOD("java:Method"),
        CONSTRUCTOR("java:Constructor"),
        FIELD_GET("java:FieldGet"),
        FIELD_SET("java:FieldSet");

        private final String value;

        BFunctionKind(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
}
