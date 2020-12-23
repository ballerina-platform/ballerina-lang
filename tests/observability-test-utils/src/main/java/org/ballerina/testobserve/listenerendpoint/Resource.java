/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerina.testobserve.listenerendpoint;

import io.ballerina.runtime.api.types.ResourceFunctionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BObject;

import java.util.Objects;

import static io.ballerina.runtime.api.constants.TypeConstants.STRING_TNAME;
import static org.ballerina.testobserve.listenerendpoint.Constants.CALLER_TYPE_NAME;
import static org.ballerina.testobserve.listenerendpoint.Constants.TEST_OBSERVE_PACKAGE;

/**
 * Holds information related to a Ballerina service resource.
 */
public class Resource {
    private final BObject serviceObject;
    private final ResourceFunctionType resourceFunctionType;
    private final String basePath;

    public Resource(BObject serviceObject, ResourceFunctionType resourceFunctionType, String basePath) {
        this.serviceObject = serviceObject;
        this.resourceFunctionType = resourceFunctionType;
        this.basePath = basePath;

        // Validate resource
        int paramCount = this.getParamTypes().length;
        String resourcePath = this.getResourcePath();
        if (paramCount > 2) {
            throw new IllegalArgumentException("Invalid number of arguments in resource function "
                    + resourcePath + ". Expected a maximum of 2 arguments, but found " + paramCount);
        }
        if (paramCount >= 1) {
            Type paramOneType = this.getParamTypes()[0];
            if (!Objects.equals(paramOneType.getPackage(), TEST_OBSERVE_PACKAGE)
                    || !Objects.equals(paramOneType.getName(), CALLER_TYPE_NAME)) {
                throw new IllegalArgumentException("Invalid first parameter in the resource function "
                        + resourcePath + ". Expected parameter of type " + TEST_OBSERVE_PACKAGE + "/"
                        + CALLER_TYPE_NAME + ", but found " + paramOneType.getPackage() + "/"
                        + paramOneType.getName());
            }
        }
        if (paramCount >= 2) {
            Type paramTwoType = this.getParamTypes()[1];
            if (!Objects.equals(paramTwoType.getPackage().toString(), "null")
                    || !Objects.equals(paramTwoType.getName(), STRING_TNAME)) {
                throw new IllegalArgumentException("Invalid second parameter in the resource function "
                        + resourcePath + ". Expected parameter of type \"" + STRING_TNAME + "\", but found \""
                        + paramTwoType.getName() + "\"");
            }
        }
    }

    public BObject getServiceObject() {
        return this.serviceObject;
    }

    public String getResourceFunctionName() {
        return this.resourceFunctionType.getName();
    }

    public String getAccessor() {
        return this.resourceFunctionType.getAccessor();
    }

    public String getResourcePath() {
        return Utils.normalizeResourcePath(basePath)
                + Utils.normalizeResourcePath(String.join("/", this.resourceFunctionType.getResourcePath()));
    }

    public Type[] getParamTypes() {
        return this.resourceFunctionType.getParameterTypes();
    }

    public Type getReturnType() {
        return this.resourceFunctionType.getReturnType();
    }
}
