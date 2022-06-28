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

import java.util.Arrays;

/**
 * Contains diagnostic messages of the JSON to Record converter.
 *
 * @since 2201.2.0
 */
public class DiagnosticMessages {

    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;
    private final Object[] args;

    private DiagnosticMessages(String code, String description, DiagnosticSeverity severity, Object[] args) {
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
        return this.args.clone();
    }

    public static DiagnosticMessages jsonToRecordConverter100(Object[] args) {
        if (args != null) {
            return new DiagnosticMessages("JSON_TO_RECORD_CONVERTER_100",
                    String.format("Provided JSON is invalid : %s", DiagnosticUtils.
                            transformJsonSyntaxErrorMessage((String) args[0])), DiagnosticSeverity.ERROR,
                    Arrays.copyOfRange(args, 1, args.length));
        }
        return new DiagnosticMessages("JSON_TO_RECORD_CONVERTER_100",
                 "Provided JSON is invalid", DiagnosticSeverity.ERROR, null);
    }

    public static DiagnosticMessages jsonToRecordConverter101(Object[] args) {
        return new DiagnosticMessages("JSON_TO_RECORD_CONVERTER_101",
                "Provided JSON is unsupported. It may be null or have missing types", DiagnosticSeverity.ERROR, args);
    }

    public static DiagnosticMessages jsonToRecordConverter102(Object[] args) {
        return new DiagnosticMessages("JSON_TO_RECORD_CONVERTER_102",
                "Error occurred while formatting the Ballerina syntax tree", DiagnosticSeverity.ERROR, args);
    }
}
