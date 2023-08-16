/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.xmltorecordconverter.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.Objects;

/**
 * Contains diagnostic messages of the XML to Record converter.
 *
 * @since 2201.7.2
 */
public class DiagnosticMessage {

    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;
    private final Object[] args;

    private DiagnosticMessage(String code, String description, DiagnosticSeverity severity, Object[] args) {
        this.code = code;
        this.description = description;
        this.severity = severity;
        this.args = args;
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

    public Object[] getArgs() {
        return Objects.requireNonNullElse(this.args, new Object[0]).clone();
    }

    public static DiagnosticMessage xmlToRecordConverter100(Object[] args) {
        return new DiagnosticMessage("XML_TO_RECORD_CONVERTER_100",
                "XML parser configuration error: Unable to properly configure.", DiagnosticSeverity.ERROR, args);
    }

    public static DiagnosticMessage xmlToRecordConverter101(Object[] args) {
        return new DiagnosticMessage("XML_TO_RECORD_CONVERTER_101",
                "Error occurred while parsing the XML text.", DiagnosticSeverity.ERROR, args);
    }

    public static DiagnosticMessage xmlToRecordConverter102(Object[] args) {
        return new DiagnosticMessage("XML_TO_RECORD_CONVERTER_102",
                "Provided input is invalid.", DiagnosticSeverity.ERROR, args);
    }

    public static DiagnosticMessage xmlToRecordConverter103(Object[] args) {
        return new DiagnosticMessage("XML_TO_RECORD_CONVERTER_103",
                "Error occurred while formatting the Ballerina syntax tree.", DiagnosticSeverity.ERROR, args);
    }
}
