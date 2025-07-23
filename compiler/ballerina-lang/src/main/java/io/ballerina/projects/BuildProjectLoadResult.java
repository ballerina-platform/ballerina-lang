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

import io.ballerina.projects.directory.BuildProject;

import java.util.Objects;

public class BuildProjectLoadResult extends ProjectLoadResult {

    public BuildProjectLoadResult(BuildProject project, DiagnosticResult diagnostics) {
        super(project, diagnostics);
    }

    public BuildProject project() {
        return (BuildProject) project;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BuildProjectLoadResult) obj;
        return Objects.equals(this.project, that.project) &&
                Objects.equals(this.diagnostics, that.diagnostics);
    }
}
