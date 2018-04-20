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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import org.ballerinalang.swagger.exception.BallerinaOpenApiException;

import java.util.AbstractMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Wraps the {@link PathItem} from swagger models to provide an iterable object model
 * for operations.
 *
 * @since 0.967.0
 */
public class BallerinaPath implements BallerinaSwaggerObject<BallerinaPath, PathItem> {
    private String ref;
    private String summary;
    private String description;
    private Set<Map.Entry<String, BallerinaOperation>> operations;

    @Override
    public BallerinaPath buildContext(PathItem item, OpenAPI openAPI) throws BallerinaOpenApiException {
        this.ref = item.get$ref();
        this.summary = item.getSummary();
        this.description = item.getDescription();
        this.operations = new LinkedHashSet<>();
        Map.Entry<String, BallerinaOperation> entry;
        BallerinaOperation operation;

        // Swagger PathItem object doesn't provide a iterable structure for operations
        // Therefore we have to manually check if each http verb exists
        if (item.getGet() != null) {
            operation = new BallerinaOperation().buildContext(item.getGet(), openAPI);
            entry = new AbstractMap.SimpleEntry<>("get", operation);
            operations.add(entry);
        }
        if (item.getPut() != null) {
            operation = new BallerinaOperation().buildContext(item.getPut(), openAPI);
            entry = new AbstractMap.SimpleEntry<>("put", operation);
            operations.add(entry);
        }
        if (item.getPost() != null) {
            operation = new BallerinaOperation().buildContext(item.getPost(), openAPI);
            entry = new AbstractMap.SimpleEntry<>("post", operation);
            operations.add(entry);
        }
        if (item.getDelete() != null) {
            operation = new BallerinaOperation().buildContext(item.getDelete(), openAPI);
            entry = new AbstractMap.SimpleEntry<>("delete", operation);
            operations.add(entry);
        }
        if (item.getOptions() != null) {
            operation = new BallerinaOperation().buildContext(item.getOptions(), openAPI);
            entry = new AbstractMap.SimpleEntry<>("options", operation);
            operations.add(entry);
        }
        if (item.getHead() != null) {
            operation = new BallerinaOperation().buildContext(item.getHead(), openAPI);
            entry = new AbstractMap.SimpleEntry<>("head", operation);
            operations.add(entry);
        }
        if (item.getPatch() != null) {
            operation = new BallerinaOperation().buildContext(item.getPatch(), openAPI);
            entry = new AbstractMap.SimpleEntry<>("patch", operation);
            operations.add(entry);
        }
        if (item.getTrace() != null) {
            operation = new BallerinaOperation().buildContext(item.getTrace(), openAPI);
            entry = new AbstractMap.SimpleEntry<>("trace", operation);
            operations.add(entry);
        }

        return this;
    }

    @Override
    public BallerinaPath buildContext(PathItem item) throws BallerinaOpenApiException {
        return buildContext(item, null);
    }

    @Override
    public BallerinaPath getDefaultValue() {
        return null;
    }

    public String getRef() {
        return ref;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public Set<Map.Entry<String, BallerinaOperation>> getOperations() {
        return operations;
    }
}
