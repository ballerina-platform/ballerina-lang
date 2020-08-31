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
package org.ballerinalang.openapi.validator;

import org.ballerinalang.util.diagnostic.Diagnostic;

import java.util.HashMap;
import java.util.Map;

/**
 * Summarized details of a Ballerina resource to be validated against OpenAPI document.
 */
public class ResourcePathSummary {
    private String path;
    private Diagnostic.DiagnosticPosition pathPosition;
    private Map<String, ResourceMethod> methods;

    public ResourcePathSummary() {
        this.methods = new HashMap<>();
        this.path = null;
        this.pathPosition = null;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, ResourceMethod> getMethods() {
        return methods;
    }

    public void addMethod(String method, ResourceMethod resourceMethod) {
        this.methods.put(method, resourceMethod);
    }

    public boolean isMethodAvailable(String method) {
        boolean isAvailable = false;
        Map<String, ResourceMethod> methods = this.methods;
        for (Map.Entry<String, ResourceMethod> m : methods.entrySet()) {
            if (m.getKey().equals(method)) {
                isAvailable = true;
                break;
            }
        }
        return isAvailable;
    }
    public Diagnostic.DiagnosticPosition getPathPosition() {
        return pathPosition;
    }

    public void setPathPosition(Diagnostic.DiagnosticPosition pathPosition) {
        this.pathPosition = pathPosition;
    }
}
