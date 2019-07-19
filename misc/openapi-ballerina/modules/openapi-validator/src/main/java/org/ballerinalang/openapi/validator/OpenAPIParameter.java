/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.openapi.validator;

import io.swagger.v3.oas.models.parameters.Parameter;

/**
 * Container to hold the OpenAPI parameter details needed for the validator plugin.
 */
class OpenAPIParameter {
    private String name;
    private String paramType;
    private String type;
    private Parameter parameter;

    OpenAPIParameter() {
        name = null;
        paramType = null;
        type = null;
        parameter = null;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    void setParamType(String paramType) {
        this.paramType = paramType;
    }

    void setType(String type) {
        this.type = type;
    }

    Parameter getParameter() {
        return parameter;
    }

    void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    boolean isTypeAvailableAsRef() {
        return parameter.getSchema().get$ref() != null;
    }

    String getLocalRef() {
        String componentName = null;
        String ref = parameter.getSchema().get$ref();
        if (ref != null && ref.startsWith("#")) {
            String[] splitRef = ref.split("/");
            componentName = splitRef[splitRef.length - 1];
        }
        return componentName;
    }

    public String getParamType() {
        return paramType;
    }

    public String getType() {
        return type;
    }
}
