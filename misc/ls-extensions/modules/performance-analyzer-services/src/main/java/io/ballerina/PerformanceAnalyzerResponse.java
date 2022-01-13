/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina;

import com.google.gson.JsonObject;
import org.eclipse.lsp4j.Range;

/**
 * Response format for performance analyzer request.
 *
 * @since 2.0.0
 */
public class PerformanceAnalyzerResponse {

    private Range resourcePos;
    private String type;
    private String message;
    private String name;
    private JsonObject endpoints;
    private JsonObject actionInvocations;

    public PerformanceAnalyzerResponse(Range resourcePos, String type, String message,
                                       String name, JsonObject endpoints, JsonObject actionInvocations) {

        this.resourcePos = resourcePos;
        this.type = type;
        this.message = message;
        this.name = name;
        this.endpoints = endpoints;
        this.actionInvocations = actionInvocations;
    }

    public PerformanceAnalyzerResponse() {

    }

    public Range getResourcePos() {

        return resourcePos;
    }

    public void setResourcePos(Range resourcePos) {

        this.resourcePos = resourcePos;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public JsonObject getEndpoints() {

        return endpoints;
    }

    public void setEndpoints(JsonObject endpoints) {

        this.endpoints = endpoints;
    }

    public JsonObject getActionInvocations() {

        return actionInvocations;
    }

    public void setActionInvocations(JsonObject actionInvocations) {

        this.actionInvocations = actionInvocations;
    }
}
