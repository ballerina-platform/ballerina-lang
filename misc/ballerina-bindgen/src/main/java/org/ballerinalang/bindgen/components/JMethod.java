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
import java.util.Locale;

import static org.ballerinalang.bindgen.command.BindingsGenerator.allJavaClasses;
import static org.ballerinalang.bindgen.command.BindingsGenerator.classListForLooping;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_RESERVED_WORDS;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenConstants.METHOD_INTEROP_TYPE;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getBallerinaHandleType;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getBallerinaParamType;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isStaticMethod;

/**
 * Class for storing details pertaining to a specific Java method used for Ballerina bridge code generation.
 */
public class JMethod {

    private boolean isStatic;
    private boolean params = true;
    private boolean noParams = true;
    private boolean hasReturn = false;
    private boolean objectReturn = false;
    private boolean objectMethod = false;
    private boolean reservedWord = false;
    private boolean isArrayReturn = false;
    private boolean exceptionTypes = false;
    private boolean handleException = false;
    private boolean isStringReturn = false;
    private boolean hasPrimitiveParam = false;
    private boolean isStringArrayReturn = false;

    private String methodName;
    private String returnType;
    private String externalType;
    private String shortClassName;
    private String javaMethodName;
    private String methodClassName;
    private String interopType = METHOD_INTEROP_TYPE;

    private List<JParameter> parameters = new ArrayList<>();
    private StringBuilder paramTypes = new StringBuilder();

    JMethod(Method m) {

        javaMethodName = m.getName();
        methodName = m.getName();
        shortClassName = m.getDeclaringClass().getSimpleName();
        if (m.getDeclaringClass().equals(Object.class)) {
            methodClassName = m.getDeclaringClass().getName();
            objectMethod = true;
        }
        Class returnTypeClass = m.getReturnType();
        if (!returnTypeClass.equals(Void.TYPE)) {
            hasReturn = true;
            externalType = getBallerinaHandleType(returnTypeClass);
            returnType = getBallerinaParamType(returnTypeClass);
            if (returnTypeClass.isArray()) {
                isArrayReturn = true;
                if (returnTypeClass.getComponentType().isPrimitive()) {
                    objectReturn = false;
                }
            } else if (returnTypeClass.isPrimitive()) {
                objectReturn = false;
            } else if (returnType.equals(BALLERINA_STRING)) {
                isStringReturn = true;
            } else if (returnType.equals(BALLERINA_STRING_ARRAY)) {
                objectReturn = true;
                isArrayReturn = true;
                isStringArrayReturn = true;
            } else {
                objectReturn = true;
            }

        }
        isStatic = isStaticMethod(m);
        setParameters(m.getParameters());
        if (m.getExceptionTypes().length > 0) {
            exceptionTypes = true;
            handleException = true;
        }
        if (!parameters.isEmpty()) {
            JParameter lastParam = parameters.get(parameters.size() - 1);
            lastParam.setHasNext(false);
        } else {
            noParams = false;
        }
        List<String> reservedWords = Arrays.asList(BALLERINA_RESERVED_WORDS);
        if (reservedWords.contains(methodName)) {
            reservedWord = true;
        }
        if (objectReturn && !allJavaClasses.contains(returnTypeClass.getName())) {
            if (isArrayReturn) {
                classListForLooping.add(returnTypeClass.getComponentType().getName());
            } else {
                classListForLooping.add(returnTypeClass.getName());
            }
        }
    }

    private void setParameters(Parameter[] paramArr) {

        for (Parameter param : paramArr) {
            paramTypes.append(param.getType().getSimpleName().toLowerCase(Locale.ENGLISH));
            JParameter parameter = new JParameter(param);
            parameters.add(parameter);
            if (parameter.getIsPrimitiveArray()) {
                hasPrimitiveParam = true;
                exceptionTypes = true;
            }
            if (parameter.isObjArrayParam()) {
                this.exceptionTypes = true;
            }
        }
    }

    public String getJavaMethodName() {

        return javaMethodName;
    }

    public String getParamTypes() {

        return paramTypes.toString();
    }

    public Boolean getHasReturn() {

        return hasReturn;
    }

    public Boolean getExceptionTypes() {

        return exceptionTypes;
    }

    public Boolean getIsStringReturn() {

        return isStringReturn;
    }

    public Boolean getHasPrimitiveParam() {

        return hasPrimitiveParam;
    }

    public String getReturnType() {

        return returnType;
    }

    public void setParams(boolean params) {

        this.params = params;
    }

    public String getMethodName() {

        return methodName;
    }

    public void setMethodName(String methodName) {

        this.methodName = methodName;
    }

    public void setObjectReturn(boolean objectReturn) {

        this.objectReturn = objectReturn;
    }

    public void setIsArrayReturn(boolean arrayReturn) {

        isArrayReturn = arrayReturn;
    }

    public void setIsStringArrayReturn(boolean stringArrayReturn) {

        isStringArrayReturn = stringArrayReturn;
    }
}
