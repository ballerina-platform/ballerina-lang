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

import io.ballerina.xmltorecordconverter.XMLToRecordResponse;

import java.util.List;

/**
 * Util methods for XML to record converter diagnostics.
 *
 * @since 2201.7.2
 */
public class DiagnosticUtils {

    private DiagnosticUtils() {}

    public static XMLToRecordResponse getDiagnosticResponse(List<DiagnosticMessage> diagnosticMessages,
                                                            XMLToRecordResponse response) {
        List<XMLToRecordConverterDiagnostic> diagnostics = response.getDiagnostics();
        for (DiagnosticMessage message : diagnosticMessages) {
            XMLToRecordConverterDiagnostic diagnostic = new XMLToRecordConverterDiagnostic(
                    message.getCode(), message.getDescription(), message.getSeverity(), null, message.getArgs());
            diagnostics.add(diagnostic);
        }
        return response;
    }
}
