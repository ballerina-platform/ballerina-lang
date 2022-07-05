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

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.bindgen.utils.BindgenEnv;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getBallerinaHandleType;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getBallerinaParamType;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isStaticField;

/**
 * Class for storing details pertaining to a specific Java field used for Ballerina bridge code generation.
 *
 * @since 1.2.0
 */
public class JField extends BFunction {

    private String fieldName;
    private String fieldType;
    private String fieldMethodName;

    private boolean isArray;
    private boolean isStatic;
    private boolean isString;
    private boolean isStringArray;
    private boolean isObject = true;
    private boolean isObjectArray;
    private boolean returnError = false;
    private boolean javaArraysModule = false;

    private JParameter fieldObj;

    JField(Field field, BFunction.BFunctionKind fieldKind, BindgenEnv env, JClass jClass) {
        super(fieldKind, env);
        Class type = field.getType();
        fieldType = getBallerinaParamType(type, env.getAliases());
        isStatic = isStaticField(field);
        super.setStatic(isStatic);
        fieldName = field.getName();
        fieldObj = new JParameter(type, jClass.getCurrentClass(), env);
        setDeclaringClass(jClass.getCurrentClass());
        if (type.isPrimitive() || type.equals(String.class)) {
            isObject = false;
        }
        if (fieldType.equals(BALLERINA_STRING)) {
            isString = true;
        }
        if (fieldType.equals(BALLERINA_STRING_ARRAY)) {
            isStringArray = true;
        }
        if (type.isArray()) {
            isArray = true;
            returnError = true;
            if (!type.getComponentType().isPrimitive()) {
                isObject = false;
                if (type.getComponentType().equals(String.class)) {
                    isStringArray = true;
                } else {
                    isObjectArray = true;
                }
            }
            javaArraysModule = true;
        }

        if (fieldKind == BFunctionKind.FIELD_GET) {
            fieldMethodName = "get" + StringUtils.capitalize(fieldName);
        } else if (fieldKind == BFunctionKind.FIELD_SET) {
            setParameters(Collections.singletonList(fieldObj));
            fieldMethodName = "set" + StringUtils.capitalize(fieldName);
        }
        setExternalReturnType(getBallerinaHandleType(type));
        setExternalFunctionName(jClass.getCurrentClass().getName().replace(".", "_")
                .replace("$", "_") + "_" + fieldMethodName);
        setReturnType(fieldType);

        if (isStatic) {
            super.setFunctionName(jClass.getShortClassName() + "_" + fieldMethodName);
        } else {
            super.setFunctionName(fieldMethodName);
        }
    }

    public boolean isString() {
        return isString;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public String getFieldName() {
        return fieldName;
    }

    boolean requireJavaArrays() {
        return javaArraysModule;
    }

    public String getFunctionReturnType() {
        StringBuilder returnString = new StringBuilder();
        if (super.getKind() == BFunctionKind.FIELD_GET) {
            returnString.append(fieldObj.getShortTypeName());
            if (isString || isStringArray) {
                returnString.append("?");
            }
            if (returnError) {
                returnString.append("|error");
            }
        }
        return returnString.toString();
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isObject() {
        return isObject;
    }

    public boolean isStringArray() {
        return isStringArray;
    }

    public String getFieldType() {
        return fieldType;
    }

    public boolean isObjectArray() {
        return isObjectArray;
    }

    public String getReturnShortName() {
        return fieldObj.getShortTypeName();
    }
}
