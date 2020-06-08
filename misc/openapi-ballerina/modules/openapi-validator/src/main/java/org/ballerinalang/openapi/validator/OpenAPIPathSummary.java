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

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Summary of the OpenAPI documentation for a API path.
 */
class OpenAPIPathSummary {
    private String path;
    private List<String> availableOperations;
    private Map<String, Operation> operations;

    OpenAPIPathSummary() {
        this.availableOperations = new ArrayList<>();
        this.operations = new HashMap<>();
        this.path = null;
    }

    String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    List<String> getAvailableOperations() {
        return availableOperations;
    }

    void addOperation(String method, Operation operation) {
        this.operations.put(method, operation);
    }

    void addAvailableOperation(String operation) {
        this.availableOperations.add(operation);
    }

    boolean hasTags(List<String> tags, String method) {
        Operation operation = operations.get(method);
        if (operation == null) {
            return false;
        }
        return !Collections.disjoint(tags, operation.getTags());
    }

    boolean hasOperations(List<String> operationslist, String method) {
        Operation operation = operations.get(method);
        if (operation == null) {
            return false;
        }
        return operationslist.contains(operation.getOperationId());

    }

    List<OpenAPIParameter> getParamNamesForOperation(String operation) {
        List<OpenAPIParameter> paramNames = new ArrayList<>();
        for (Map.Entry<String, Operation> entry : this.operations.entrySet()) {
            if (entry.getKey().equals(operation)
                    && entry.getValue() != null
                    && entry.getValue().getParameters() != null) {
                for (Parameter parameter : entry.getValue().getParameters()) {
                    if (parameter.getIn() != null && parameter.getIn().equals(Constants.PATH)) {
                        OpenAPIParameter openAPIParameter = new OpenAPIParameter();
                        openAPIParameter.setName(parameter.getName());
                        openAPIParameter.setParamType(Constants.PATH);
                        openAPIParameter.setParameter(parameter);

                        if (parameter.getSchema() != null) {
                            Schema schema = parameter.getSchema();
                            String type = schema.getType();
                            openAPIParameter.setType(type);
                        }

                        paramNames.add(openAPIParameter);
                    }
                }
                break;
            }
        }
        return paramNames;
    }

    Map<String, Schema> getRequestBodyForOperation(String operation) {
        Map<String, Schema> requestBodySchemas = new HashMap<>();
        for (Map.Entry<String, Operation> entry : this.operations.entrySet()) {
            if (entry.getKey().equals(operation)) {
                if (entry.getValue().getRequestBody() != null) {
                    Content content = entry.getValue().getRequestBody().getContent();
                    for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
                        requestBodySchemas.put(mediaTypeEntry.getKey(), mediaTypeEntry.getValue().getSchema());
                    }
                }
                break;
            }
        }
        return requestBodySchemas;
    }
}
