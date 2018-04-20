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

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.callbacks.Callback;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.ballerinalang.swagger.exception.BallerinaOpenApiException;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wraps the {@link Operation} from swagger models to provide iterable child models.
 *
 * @since 0.967.0
 */
public class BallerinaOperation implements BallerinaSwaggerObject<BallerinaOperation, Operation> {
    private List<String> tags;
    private String summary;
    private String description;
    private ExternalDocumentation externalDocs;
    private String operationId;
    private List<BallerinaParameter> parameters;
    private RequestBody requestBody;
    private Set<Map.Entry<String, ApiResponse>> responses;
    private Set<Map.Entry<String, Callback>> callbacks;
    private List<SecurityRequirement> security;

    public BallerinaOperation() {
        this.responses = new LinkedHashSet<>();
        this.callbacks = new LinkedHashSet<>();
    }

    @Override
    public BallerinaOperation buildContext(Operation operation, OpenAPI openAPI) throws BallerinaOpenApiException {
        if (operation == null) {
            return getDefaultValue();
        }
        this.tags = operation.getTags();
        this.summary = operation.getSummary();
        this.description = operation.getDescription();
        this.externalDocs = operation.getExternalDocs();
        this.requestBody = operation.getRequestBody();
        this.security = operation.getSecurity();
        this.operationId = operation.getOperationId();
        this.parameters = new ArrayList<>();

        if (operation.getResponses() != null) {
            operation.getResponses()
                    .forEach((name, response) -> responses.add(new AbstractMap.SimpleEntry<>(name, response)));
        }
        if (operation.getCallbacks() != null) {
            operation.getCallbacks()
                    .forEach((name, callback) -> callbacks.add(new AbstractMap.SimpleEntry<>(name, callback)));
        }
        if (operation.getParameters() != null) {
            for (Parameter parameter : operation.getParameters()) {
                this.parameters.add(new BallerinaParameter().buildContext(parameter, openAPI));
            }
        }

        return this;
    }

    @Override
    public BallerinaOperation buildContext(Operation operation) throws BallerinaOpenApiException {
        return buildContext(operation, null);
    }

    @Override
    public BallerinaOperation getDefaultValue() {
        return new BallerinaOperation();
    }

    public List<String> getTags() {
        return tags;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public ExternalDocumentation getExternalDocs() {
        return externalDocs;
    }

    public String getOperationId() {
        return operationId;
    }

    public List<BallerinaParameter> getParameters() {
        return parameters;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public Set<Map.Entry<String, ApiResponse>> getResponses() {
        return responses;
    }

    public Set<Map.Entry<String, Callback>> getCallbacks() {
        return callbacks;
    }

    public List<SecurityRequirement> getSecurity() {
        return security;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
}
