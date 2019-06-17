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

import org.ballerinalang.util.diagnostic.Diagnostic;

import java.util.ArrayList;
import java.util.List;

/**
 * Summarized details of a Ballerina resource to be validated against OpenAPI document.
 */
class ResourceSummary {
    private Diagnostic.DiagnosticPosition resourcePosition;
    private String path;
    private Diagnostic.DiagnosticPosition pathPosition;
    private List<String> methods;
    private Diagnostic.DiagnosticPosition methodsPosition;

    ResourceSummary() {
        this.methods = new ArrayList<>();
        this.path = null;
        this.resourcePosition = null;
        this.pathPosition = null;
        this.methodsPosition = null;
    }

    Diagnostic.DiagnosticPosition getResourcePosition() {
        return resourcePosition;
    }

    void setResourcePosition(Diagnostic.DiagnosticPosition position) {
        this.resourcePosition = position;
    }

    String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    List<String> getMethods() {
        return methods;
    }

    void setMethods(List<String> methods) {
        this.methods = methods;
    }

    void addMethod(String method) {
        this.methods.add(method);
    }

    boolean isMethodAvailable(String method) {
        boolean isAvailable = false;
        for (String m : this.methods) {
            if (m.equals(method)) {
                isAvailable = true;
                break;
            }
        }
        return isAvailable;
    }

    boolean isPathAvailable(String path) {
        return this.path.equals(path);
    }

    Diagnostic.DiagnosticPosition getPathPosition() {
        return pathPosition;
    }

    void setPathPosition(Diagnostic.DiagnosticPosition pathPosition) {
        this.pathPosition = pathPosition;
    }

    Diagnostic.DiagnosticPosition getMethodsPosition() {
        return methodsPosition;
    }

    void setMethodsPosition(Diagnostic.DiagnosticPosition methodsPosition) {
        this.methodsPosition = methodsPosition;
    }
}
