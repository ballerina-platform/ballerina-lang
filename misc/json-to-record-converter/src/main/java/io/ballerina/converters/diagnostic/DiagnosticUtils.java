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

import io.ballerina.converters.JsonToRecordResponse;

import java.util.List;

/**
 * Util methods for JSON to record direct converter diagnostics.
 *
 * @since 2201.2.0
 */
public class DiagnosticUtils {

    public static JsonToRecordResponse getDiagnosticResponse(List<DiagnosticMessage> diagnosticMessages,
                                                             JsonToRecordResponse response) {
        List<JsonToRecordDirectConverterDiagnostic> diagnostics = response.getDiagnostics();
        for (DiagnosticMessage message : diagnosticMessages) {
            JsonToRecordDirectConverterDiagnostic diagnostic = new JsonToRecordDirectConverterDiagnostic(
                    message.getCode(), message.getDescription(), message.getSeverity(), null, message.getArgs());
            diagnostics.add(diagnostic);
        }
        return response;
    }

    public static String transformJsonSyntaxErrorMessage(String jsonSyntaxErrorMessage) {
        String[] splitMessage = jsonSyntaxErrorMessage.split(":");
        return splitMessage[1].trim();
    }
}
