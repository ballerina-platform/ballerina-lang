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

import java.lang.reflect.Parameter;

import static org.ballerinalang.bindgen.command.BindingsGenerator.allJavaClasses;
import static org.ballerinalang.bindgen.command.BindingsGenerator.classListForLooping;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getBallerinaHandleType;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getBallerinaParamType;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getPrimitiveArrayType;

/**
 * Class for storing specific parameter details of a Java method used for Ballerina bridge code generation.
 */
public class JParameter {

    private String type;
    private String shortTypeName;
    private String componentType;
    private String externalType;
    private String fieldName;

    private Boolean isObj = false;
    private Boolean hasNext = true;
    private Boolean isString = false;
    private Boolean isObjArray = false;
    private Boolean isStringArray = false;
    private Boolean isPrimitiveArray = false;

    JParameter(Class parameterClass) {

        type = parameterClass.getName();
        shortTypeName = getBallerinaParamType(parameterClass);
        if (!parameterClass.isPrimitive()) {
            isObj = true;
        }
        if (parameterClass.getName().equals("java.lang.String")) {
            this.isString = true;
            this.shortTypeName = BALLERINA_STRING;
        } else if (parameterClass.getName().equals("[Ljava.lang.String;")) {
            this.isString = true;
            this.isStringArray = true;
            this.shortTypeName = BALLERINA_STRING_ARRAY;
        } else {
            if (!parameterClass.isPrimitive()) {
                if (parameterClass.isArray()) {
                    Class component = parameterClass.getComponentType();
                    this.componentType = component.getTypeName();
                    if (!parameterClass.getComponentType().isPrimitive()) {
                        this.isObjArray = true;
                        this.isObj = false;
                        String componentClass = parameterClass.getComponentType().getName();
                        if (!allJavaClasses.contains(componentClass)) {
                            classListForLooping.add(componentClass);
                        }
                    } else {
                        shortTypeName = getPrimitiveArrayType(type);
                        this.isPrimitiveArray = true;
                    }

                } else {
                    String paramType = parameterClass.getName();
                    if (!allJavaClasses.contains(paramType)) {
                        classListForLooping.add(paramType);
                    }
                }
            }
        }
        this.externalType = getBallerinaHandleType(parameterClass);
        fieldName = "arg";
    }

    JParameter(Parameter parameter) {

        this(parameter.getType());
        this.fieldName = parameter.getName();

    }

    void setHasNext(boolean hasNext) {

        this.hasNext = hasNext;
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

    public Boolean getIsObjArray() {

        return isObjArray;
    }

    public Boolean getIsPrimitiveArray() {

        return isPrimitiveArray;
    }

    public Boolean getHasNext() {

        return hasNext;
    }
}
