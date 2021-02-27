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

import java.nio.file.Path;

/**
 * This class represent the results of the emit operation.
 *
 * @since 2.0.0
 */
public class EmitResult {

    private final boolean success;
    private final DiagnosticResult diagnostics;
    private final Path generatedArtifact;

    EmitResult(boolean success, DiagnosticResult diagnostics, Path generatedArtifact) {
        this.success = success;
        this.diagnostics = diagnostics;
        this.generatedArtifact = generatedArtifact;
    }

    public boolean successful() {
        return success;
    }

    public DiagnosticResult diagnostics() {
        return diagnostics;
    }

    public Path generatedArtifactPath() {
        return generatedArtifact;
    }
}
