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
package io.ballerina.projects;

import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the 'Ballerina.toml' file in a package.
 *
 * @since 2.0.0
 */
public class BallerinaToml extends TomlDocument {

    private DiagnosticResult diagnostics;

    private BallerinaToml(Path filePath) {
        super(filePath);
    }

    public static BallerinaToml from(Path filePath) {
        return new BallerinaToml(filePath);
    }

    public DiagnosticResult diagnostics() {
        if (diagnostics != null) {
            return diagnostics;
        }

        List<Diagnostic> diagnosticList = new ArrayList<>();
        syntaxTree().diagnostics().forEach(diagnosticList::add);
        diagnostics = new DefaultDiagnosticResult(diagnosticList);
        return diagnostics;
    }
}
