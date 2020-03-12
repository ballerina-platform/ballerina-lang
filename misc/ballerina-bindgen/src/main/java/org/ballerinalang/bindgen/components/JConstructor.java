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

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.bindgen.utils.BindgenConstants.CONSTRUCTOR_INTEROP_TYPE;

/**
 * Class for storing details pertaining to a specific Java constructor used for Ballerina bridge code generation.
 */
public class JConstructor implements Cloneable {

    String constructorName;

    private String interopType;
    private String shortClassName;
    private String initObjectName;
    private String externalFunctionName;
    public Boolean exceptionTypes = false;
    private List<JParameter> parameters = new ArrayList<>();

    JConstructor(Constructor c) {

        this.shortClassName = c.getDeclaringClass().getSimpleName();
        this.constructorName = c.getName();
        for (Parameter param : c.getParameters()) {
            parameters.add(new JParameter(param));
        }
        if (!parameters.isEmpty()) {
            JParameter lastParam = parameters.get(parameters.size() - 1);
            lastParam.setLastParam();
        }
        if (c.getExceptionTypes().length > 0) {
            this.exceptionTypes = true;
        }
        this.interopType = CONSTRUCTOR_INTEROP_TYPE;
        this.initObjectName = "_" + Character.toLowerCase(this.shortClassName.charAt(0))
                + this.shortClassName.substring(1);
    }

    void setConstructorName(String name) {

        this.constructorName = name;
    }

    void setExternalFunctionName(String name) {

        this.externalFunctionName = name;
    }

    protected Object clone() throws CloneNotSupportedException {

        return super.clone();
    }
}
