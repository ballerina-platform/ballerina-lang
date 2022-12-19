/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.architecturemodelgeneratorplugin.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * Model Generator compiler plugin diagnostic messages.
 *
 * @since 2201.4.0
 */
public enum DiagnosticMessage {
    ERROR_100("MODEL_GENERATOR_PLUGIN_ERROR_100",
            "Failed to write the generated Component Model JSON to the target directory: {0}.",
            DiagnosticSeverity.ERROR);

    private final String code;
    private final String messageFormat;
    private final DiagnosticSeverity severity;

    DiagnosticMessage(String code, String messageFormat, DiagnosticSeverity severity) {
        this.code = code;
        this.messageFormat = messageFormat;
        this.severity = severity;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessageFormat() {
        return this.messageFormat;
    }

    public DiagnosticSeverity getSeverity() {
        return this.severity;
    }
}
