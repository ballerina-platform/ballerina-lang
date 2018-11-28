/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class to hold operation categories when we have multiple methods per one ballerina resource
 *
 * @since 0.984.0
 */
public class OperationCategory {
    private List<String> methods;
    private String resourceName;
    private List<Map.Entry<String, BallerinaOperation>> operations;

    public OperationCategory(String resourceName) {
        this.operations = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.resourceName = resourceName;
    }


    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public List<Map.Entry<String, BallerinaOperation>> getOperations() {
        return operations;
    }

    public void setOperations(List<Map.Entry<String, BallerinaOperation>> operations) {
        this.operations = operations;
    }

    public void addOperation(Map.Entry<String, BallerinaOperation> operation) {
        this.operations.add(operation);
    }

    public void addMethod(String method) {
        this.methods.add(method);
    }
}
