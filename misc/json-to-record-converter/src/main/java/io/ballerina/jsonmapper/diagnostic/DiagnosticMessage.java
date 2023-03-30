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

package io.ballerina.jsonmapper.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.Arrays;
import java.util.Objects;

/**
 * Contains diagnostic messages of the JSON to Record converter.
 *
 * @since 2201.2.0
 */
public final class DiagnosticMessage {

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

    public static DiagnosticMessage jsonToRecordConverter100(Object[] args) {
        if (args != null) {
            return new DiagnosticMessage("JSON_TO_RECORD_CONVERTER_100",
                    String.format("Provided JSON is invalid : %s", DiagnosticUtils.
                            extractJsonSyntaxErrorMessage((String) args[0])), DiagnosticSeverity.ERROR,
                    Arrays.copyOfRange(args, 1, args.length));
        }
        return new DiagnosticMessage("JSON_TO_RECORD_CONVERTER_100",
                 "Provided JSON is invalid.", DiagnosticSeverity.ERROR, null);
    }

    public static DiagnosticMessage jsonToRecordConverter101(Object[] args) {
        return new DiagnosticMessage("JSON_TO_RECORD_CONVERTER_101",
                "Provided JSON is unsupported. It may be null or have missing types.", DiagnosticSeverity.ERROR, args);
    }

    public static DiagnosticMessage jsonToRecordConverter102(Object[] args) {
        return new DiagnosticMessage("JSON_TO_RECORD_CONVERTER_102",
                "Error occurred while formatting the Ballerina syntax tree.", DiagnosticSeverity.ERROR, args);
    }

    public static DiagnosticMessage jsonToRecordConverter103(Object[] args) {
        return new DiagnosticMessage("JSON_TO_RECORD_CONVERTER_103",
                "There is no JSON entry could be found for the field name %s.",
                DiagnosticSeverity.WARNING, args);
    }

    public static DiagnosticMessage jsonToRecordConverter104(Object[] args) {
        if (args != null) {
            return new DiagnosticMessage("JSON_TO_RECORD_CONVERTER_104",
                    String.format("Provided record name ''%s'' conflicts with the other generated records. " +
                            "Consider providing a different name.", args[0]), DiagnosticSeverity.ERROR,
                    Arrays.copyOfRange(args, 1, args.length));
        }
        return new DiagnosticMessage("JSON_TO_RECORD_CONVERTER_104",
                "Provided record name conflicts with the other generated records. " +
                        "Consider providing a different name.", DiagnosticSeverity.ERROR, null);
    }

    public static DiagnosticMessage jsonToRecordConverter105(Object[] args) {
        if (args != null) {
            return new DiagnosticMessage("JSON_TO_RECORD_CONVERTER_105",
                    String.format("Provided record name ''%s'' conflicts with already existing records. " +
                            "Consider providing a different name.", args[0]), DiagnosticSeverity.ERROR,
                    Arrays.copyOfRange(args, 1, args.length));
        }
        return new DiagnosticMessage("JSON_TO_RECORD_CONVERTER_105",
                "Provided record name conflicts with already existing records. " +
                        "Consider providing a different name.", DiagnosticSeverity.ERROR, null);
    }

    public static DiagnosticMessage jsonToRecordConverter106(Object[] args) {
        if (args != null && args.length >= 2) {
            return new DiagnosticMessage("JSON_TO_RECORD_CONVERTER_106",
                    String.format("The record name ''%s'' is renamed as ''%s''. " +
                            "Consider rename it back to a meaningful name.", args[0], args[1]), DiagnosticSeverity.INFO,
                    Arrays.copyOfRange(args, 2, args.length));
        }
        return new DiagnosticMessage("JSON_TO_RECORD_CONVERTER_106",
                "Few of the records are renamed. " +
                        "Consider rename it back to a meaningful name.", DiagnosticSeverity.INFO, null);
    }
}
