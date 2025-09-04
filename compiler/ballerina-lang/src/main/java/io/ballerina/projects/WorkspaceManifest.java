/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package io.ballerina.projects;

import java.nio.file.Path;
import java.util.List;

public class WorkspaceManifest {
    private final DiagnosticResult diagnostics;
    private final List<Path> packages;

    public WorkspaceManifest(List<Path> packages, DiagnosticResult diagnostics) {
        this.packages = packages;
        this.diagnostics = diagnostics;
    }

    public static WorkspaceManifest from(List<Path> packages, DiagnosticResult diagnostics) {
        return new WorkspaceManifest(packages, diagnostics);
    }

    public List<Path> packages() {
        return packages;
    }

    public DiagnosticResult diagnostics() {
        return diagnostics;
    }
}
