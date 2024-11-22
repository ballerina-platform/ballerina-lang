/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
package org.ballerinalang.langserver.extensions.ballerina.runner;

import org.eclipse.lsp4j.Diagnostic;

import java.util.List;
import java.util.Map;

/**
 * {@link BallerinaRunnerService} diagnostics api response.
 *
 * @since 2201.11.0
 */
public class ProjectDiagnosticsResponse {

    private Map<String, List<Diagnostic>> errorDiagnosticMap;

    public Map<String, List<Diagnostic>> getErrorDiagnosticMap() {
        return errorDiagnosticMap;
    }

    public void setErrorDiagnosticMap(Map<String, List<Diagnostic>> errorDiagnosticMap) {
        this.errorDiagnosticMap = errorDiagnosticMap;
    }
}
