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
package org.ballerinalang.bindgen.components;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.bindgen.command.BindingsGenerator.allJavaClasses;
import static org.ballerinalang.bindgen.command.BindingsGenerator.classListForLooping;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_RESERVED_WORDS;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenConstants.HANDLE;
import static org.ballerinalang.bindgen.utils.BindgenConstants.METHOD_INTEROP_TYPE;
import static org.ballerinalang.bindgen.utils.BindgenUtils.balType;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isStaticMethod;

/**
 * Class for storing details pertaining to a specific Java method used for Ballerina bridge code generation.
 */
public class JMethod {

    public String methodName;
    public Boolean params = true;

    private Boolean isStatic;
    private Boolean isInstance;
    private Boolean noParams = true;
    private Boolean noReturn = true;
    private Boolean hasReturn = false;
    private Boolean objectReturn = false;
    private Boolean reservedWord = false;
    private Boolean isArrayReturn = false;
    private Boolean noReservedWord = true;
    private Boolean exceptionTypes = false;
    private Boolean handleException = false;
    private Boolean isStringReturn = false;
    private Boolean hasPrimitiveParam = false;

    private String methodPrefix;
    private String returnType;
    private String interopType;
    private String externalType;
    private String javaMethodName;

    private List<JParameter> parameters = new ArrayList<>();

    JMethod(Method m) {

        this.javaMethodName = m.getName();
        this.methodName = m.getName();
        this.methodPrefix = m.getDeclaringClass().getSimpleName();
        if (!m.getReturnType().equals(Void.TYPE)) {
            if (m.getReturnType().isArray()) {
                this.isArrayReturn = true;
            }
            this.externalType = balType(m.getReturnType().getSimpleName());
            this.returnType = this.externalType;
            if (this.returnType.equals(HANDLE)) {
                this.objectReturn = true;
                this.returnType = m.getReturnType().getSimpleName();
            }
            if (m.getReturnType().isArray() && m.getReturnType().getComponentType().isPrimitive()) {
                this.objectReturn = false;
            }
            this.hasReturn = true;
            this.noReturn = false;
            if (this.returnType.equals(BALLERINA_STRING)) {
                this.isStringReturn = true;
                this.externalType = HANDLE;
            } else if (this.returnType.equals(BALLERINA_STRING_ARRAY)) {
                this.isArrayReturn = true;
                this.externalType = HANDLE;
            }
        }
        this.isInstance = !isStaticMethod(m);
        this.isStatic = isStaticMethod(m);
        for (Parameter param : m.getParameters()) {
            JParameter parameter = new JParameter(param);
            this.parameters.add(parameter);
            if (parameter.isPrimitiveArray) {
                this.hasPrimitiveParam = true;
                this.exceptionTypes = true;
            }
            if (parameter.isObjArrayParam()) {
                this.exceptionTypes = true;
            }
        }
        if (m.getExceptionTypes().length > 0) {
            this.exceptionTypes = true;
            handleException = true;
        }
        if (!this.parameters.isEmpty()) {
            JParameter lastParam = this.parameters.get(this.parameters.size() - 1);
            lastParam.setLastParam();
        } else {
            this.noParams = false;
        }
        this.interopType = METHOD_INTEROP_TYPE;
        List<String> reservedWords = Arrays.asList(BALLERINA_RESERVED_WORDS);
        if (reservedWords.contains(methodName)) {
            this.reservedWord = true;
            this.noReservedWord = false;
        }
        if (objectReturn && !allJavaClasses.contains(m.getReturnType().getName())) {
            if (isArrayReturn) {
                classListForLooping.add(m.getReturnType().getComponentType().getCanonicalName());
            } else {
                classListForLooping.add(m.getReturnType().getCanonicalName());
            }
        }
    }
}
