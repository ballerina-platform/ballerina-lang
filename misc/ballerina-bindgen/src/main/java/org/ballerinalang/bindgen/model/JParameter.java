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
package org.ballerinalang.bindgen.model;

import org.ballerinalang.bindgen.utils.BindgenEnv;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_NILLABLE_STRING;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_NILLABLE_STRING_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_RESERVED_WORDS;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenConstants.EXCEPTION_CLASS_PREFIX;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getBallerinaHandleType;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getBallerinaParamType;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getPrimitiveArrayType;

/**
 * Class for storing specific parameter details of a Java method used for Ballerina bridge code generation.
 *
 * @since 1.2.0
 */
public class JParameter {

    private BindgenEnv env;
    private String type;
    private String externalType;
    private String shortTypeName;
    private String componentType;
    private String fieldName;

    private Class<?> parentClass;
    private Class<?> parameterClass;

    private Boolean isObj = false;
    private Boolean isString = false;
    private Boolean isObjArray = false;
    private Boolean isOptional = false;
    private boolean modulesFlag;
    private Boolean isStringArray = false;
    private Boolean isPrimitiveArray = false;

    JParameter(Class<?> parameterClass, Class<?> parentClass, BindgenEnv env) {
        this.env = env;
        this.parameterClass = parameterClass;
        this.parentClass = parentClass;
        type = parameterClass.getName();
        shortTypeName = getBallerinaParamType(parameterClass, env);
        modulesFlag = env.getModulesFlag();

        // Append the exception class prefix in front of bindings generated for Java exceptions.
        try {
            if (this.getClass().getClassLoader().loadClass(Exception.class.getCanonicalName())
                    .isAssignableFrom(parameterClass)) {
                shortTypeName = EXCEPTION_CLASS_PREFIX + shortTypeName;
            }
        } catch (ClassNotFoundException ignore) {
            // Silently ignore this assignment if the class is not found.
        }

        if (!parameterClass.isPrimitive()) {
            isObj = true;
        }

        if (env.isOptionalTypes() || env.isOptionalParamTypes()) {
            isOptional = true;
        }

        if (parameterClass.equals(String.class)) {
            isString = true;
            if (isOptional) {
                shortTypeName = BALLERINA_NILLABLE_STRING;
            } else {
                shortTypeName = BALLERINA_STRING;
            }
        } else if (parameterClass.equals(String[].class)) {
            isStringArray = true;
            if (isOptional) {
                shortTypeName = BALLERINA_NILLABLE_STRING_ARRAY;
            } else {
                shortTypeName = BALLERINA_STRING_ARRAY;
            }
            componentType = String.class.getName();
        } else {
            if (!parameterClass.isPrimitive()) {
                if (parameterClass.isArray()) {
                    setArrayAttributes(parameterClass);
                } else {
                    if (modulesFlag) {
                        shortTypeName = getPackageAlias(shortTypeName, parameterClass);
                    }
                    String paramType = parameterClass.getName();
                    if (!env.getAllJavaClasses().contains(paramType)) {
                        env.setClassListForLooping(paramType);
                    }
                }
            }
        }
        externalType = getBallerinaHandleType(env, parameterClass);
        fieldName = "arg";
    }

    JParameter(Parameter parameter, Class<?> parentClass, BindgenEnv env) {
        this(parameter.getType(), parentClass, env);
        List<String> reservedWords = Arrays.asList(BALLERINA_RESERVED_WORDS);
        fieldName = parameter.getName();
        if (reservedWords.contains(fieldName)) {
            fieldName = "'" + fieldName;
        }
    }

    private void setArrayAttributes(Class<?> parameterClass) {
        Class<?> component = parameterClass.getComponentType();
        componentType = component.getTypeName();
        if (!parameterClass.getComponentType().isPrimitive()) {
            isObjArray = true;
            isObj = false;
            if (modulesFlag) {
                shortTypeName = getPackageAlias(shortTypeName, component);
            }
            String componentClass = parameterClass.getComponentType().getName();
            if (!env.getAllJavaClasses().contains(componentClass)) {
                env.setClassListForLooping(componentClass);
            }
        } else {
            shortTypeName = getPrimitiveArrayType(type);
            isPrimitiveArray = true;
        }
    }

    private String getPackageAlias(String shortTypeName, Class<?> parameterClass) {
        if (parameterClass.getPackage() != parentClass.getPackage()) {
            return parameterClass.getPackageName().replace(".", "") + ":" + shortTypeName;
        }
        return shortTypeName;
    }

    Boolean isObjArrayParam() {
        return this.isObjArray;
    }

    public String getComponentType() {
        return componentType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Boolean getIsObj() {
        return isObj;
    }

    public Boolean getIsString() {
        return isString;
    }

    public Boolean isOptional() {
        return isOptional;
    }

    Boolean getIsPrimitiveArray() {
        return isPrimitiveArray;
    }

    public String getExternalType() {
        return externalType;
    }

    Boolean getIsStringArray() {
        return isStringArray;
    }

    public String getType() {
        return type;
    }

    public String getShortTypeName() {
        return shortTypeName;
    }

    public Boolean isArray() {
        return isObjArray || isStringArray || isPrimitiveArray;
    }

    public Class<?> getParameterClass() {
        return parameterClass;
    }
}
