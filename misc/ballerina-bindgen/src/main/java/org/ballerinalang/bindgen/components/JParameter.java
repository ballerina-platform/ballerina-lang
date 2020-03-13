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
    private Boolean isStringArray = false;

    Boolean isPrimitiveArray = false;

    JParameter(Parameter p) {

        Class paramType = p.getType();
        if (paramType.getName().equals("java.lang.String")) {
            this.type = paramType.getName();
            this.isString = true;
            this.externalType = "handle";
            this.shortTypeName = "string";
        } else if (paramType.getName().equals("[Ljava.lang.String;")) {
            this.type = paramType.getName();
            this.isString = true;
            this.isStringArray = true;
            this.externalType = HANDLE;
            this.shortTypeName = BALLERINA_STRING_ARRAY;
        } else {
            this.type = paramType.getName();
            if (paramType.getClassLoader() == "".getClass().getClassLoader() && !paramType.isPrimitive()) {
                if (paramType.isArray()) {
                    Class component = p.getType().getComponentType();
                    this.componentType = component.getTypeName();
                    if (!paramType.getComponentType().isPrimitive()) {
                        if (!paramType.getComponentType().isEnum() && !allJavaClasses.contains(paramType
                                .getComponentType().getName())) {
                            classListForLooping.add(paramType.getComponentType().getName());
                        }
                    } else {
                        this.isPrimitiveArray = true;
                    }

                } else {
                    if (!paramType.isEnum() && !allJavaClasses.contains(paramType.getCanonicalName())) {
                        classListForLooping.add(paramType.getCanonicalName());
                    }
                }
            }
            this.externalType = balType(p.getType().getSimpleName());
            if (p.getType().isPrimitive()) {
                this.shortTypeName = balType(p.getType().getSimpleName());
            } else {
                this.isObj = true;
                this.shortTypeName = p.getType().getSimpleName();
            }
        }
        this.fieldName = p.getName();
    }

    void setLastParam() {

        this.notLast = false;
    }
}
