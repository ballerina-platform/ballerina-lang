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
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenConstants.HANDLE;
import static org.ballerinalang.bindgen.utils.BindgenUtils.balType;

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
    private Boolean notLast = true;
    private Boolean isString = false;
    private Boolean isObjArray = false;
    private Boolean isStringArray = false;

    Boolean isPrimitiveArray = false;

    JParameter(Parameter parameter) {

        Class parameterClass = parameter.getType();
        if (parameterClass.isPrimitive()) {
            this.shortTypeName = balType(parameterClass.getSimpleName());
        } else {
            this.isObj = true;
            this.shortTypeName = parameterClass.getSimpleName();
        }
        if (parameterClass.getName().equals("java.lang.String")) {
            this.type = parameterClass.getName();
            this.isString = true;
            this.externalType = "handle";
            this.shortTypeName = "string";
        } else if (parameterClass.getName().equals("[Ljava.lang.String;")) {
            this.type = parameterClass.getName();
            this.isString = true;
            this.isStringArray = true;
            this.externalType = HANDLE;
            this.shortTypeName = BALLERINA_STRING_ARRAY;
        } else {
            this.type = parameterClass.getName();
            if (!parameterClass.isPrimitive()) {
                if (parameterClass.isArray()) {
                    Class component = parameterClass.getComponentType();
                    this.componentType = component.getTypeName();
                    if (!parameterClass.getComponentType().isPrimitive()) {
                        this.isObjArray = true;
                        this.isObj = false;
                        String componentType = parameterClass.getComponentType().getName();
                        if (!allJavaClasses.contains(componentType)) {
                            classListForLooping.add(componentType);
                        }
                    } else {
                        this.isPrimitiveArray = true;
                    }

                } else {
                    String paramType = parameterClass.getCanonicalName();
                    if (!allJavaClasses.contains(paramType)) {
                        classListForLooping.add(paramType);
                    }
                }
            }
            this.externalType = balType(parameterClass.getSimpleName());
        }
        this.fieldName = parameter.getName();
    }

    void setLastParam() {

        this.notLast = false;
    }

    public Boolean isObjArrayParam() {

        return this.isObjArray;
    }
}
