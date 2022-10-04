/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.componentmodel.diagnostics;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * Diagnostic message format for component model generation errors.
 */
public class DiagnosticMessage {

    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;

    private DiagnosticMessage(String code, String description, DiagnosticSeverity severity) {

        this.code = code;
        this.description = description;
        this.severity = severity;
    }

    public String getCode() {

        return code;
    }

    public String getDescription() {

        return description;
    }

    public DiagnosticSeverity getSeverity() {

        return severity;
    }

    public static DiagnosticMessage componentModellingService001(String projectPath) {

        return new DiagnosticMessage(String.format("Ballerina project not found in the path : %s"), projectPath,
                DiagnosticSeverity.ERROR);
    }
}
