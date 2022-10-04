/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.componentmodel;

import com.google.gson.JsonObject;
import io.ballerina.componentmodel.diagnostics.ComponentModelingDiagnostics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Response format for component model request.
 */
public class ComponentModelingServiceResponse {

    private Map<String, JsonObject> componentModels = new HashMap<>();

    private List<ComponentModelingDiagnostics> diagnostics = new ArrayList<>();

    public Map<String, JsonObject> getComponentModels() {

        return componentModels;
    }

    public void setComponentModels(Map<String, JsonObject> componentModels) {

        this.componentModels = componentModels;
    }

    public List<ComponentModelingDiagnostics> getDiagnostics() {

        return diagnostics;
    }

    public void setDiagnostics(List<ComponentModelingDiagnostics> diagnostics) {

        this.diagnostics = diagnostics;
    }

    public void addcomponentModel(String key, JsonObject jsonObject) {

        componentModels.put(key, jsonObject);
    }

    public void addDiagnostics(List<ComponentModelingDiagnostics> componentModelingDiagnostics) {

        this.diagnostics.addAll(componentModelingDiagnostics);
    }
}
