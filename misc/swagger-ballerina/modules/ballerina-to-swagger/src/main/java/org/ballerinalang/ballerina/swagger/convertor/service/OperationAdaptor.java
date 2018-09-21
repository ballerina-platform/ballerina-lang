/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.ballerina.swagger.convertor.service;

import io.swagger.models.Operation;

/**
 * This class will hold operation details specific to HTTP operation.
 */
public class OperationAdaptor {

    Operation operation;
    String path;
    String httpOperation;

    OperationAdaptor() {
        this.operation = new Operation();
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (!path.startsWith("/")) {
            this.path = "/" + path;
        } else {
            this.path = path;
        }
    }

    public String getHttpOperation() {
        return httpOperation;
    }

    public void setHttpOperation(String httpOperation) {
        this.httpOperation = httpOperation;
    }

}
