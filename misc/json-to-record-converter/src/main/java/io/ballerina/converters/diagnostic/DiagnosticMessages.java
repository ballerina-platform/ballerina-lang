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

package io.ballerina.converters.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * Contains diagnostic messages of the JSON to Record converter.
 *
 * @since 2.0.0
 */
public enum DiagnosticMessages {

    JSON_TO_RECORD_CONVERTER_100("JSON_TO_RECORD_CONVERTER_100", "Provided JSON is has syntax errors and invalid", DiagnosticSeverity.ERROR),
    JSON_TO_RECORD_CONVERTER_101("JSON_TO_RECORD_CONVERTER_101", "Provided JSON is unsupported. It may be null or have missing types", DiagnosticSeverity.ERROR),
    JSON_TO_RECORD_CONVERTER_102("JSON_TO_RECORD_CONVERTER_102", "Error occurred while formatting the Ballerina syntax tree", DiagnosticSeverity.ERROR);
    private final String code;
    private String description;
    private DiagnosticSeverity severity;

    DiagnosticMessages(String code, String description, DiagnosticSeverity severity) {
        this.code = code;
        this.description = description;
        this.severity = severity;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public DiagnosticSeverity getSeverity() {
        return this.severity;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSeverity(DiagnosticSeverity severity) {
        this.severity = severity;
    }
}
