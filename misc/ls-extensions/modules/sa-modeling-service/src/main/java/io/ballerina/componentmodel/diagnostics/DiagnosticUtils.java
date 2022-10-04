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

import io.ballerina.componentmodel.ComponentModelingServiceResponse;

import java.util.List;

/**
 * Provides util functions for diagnostics.
 */
public class DiagnosticUtils {

    public static List<ComponentModelingDiagnostics> getDiagnosticResponse(List<DiagnosticMessage> diagnosticMessages,
                                                                           ComponentModelingServiceResponse response) {

        List<ComponentModelingDiagnostics> diagnostics = response.getDiagnostics();
        for (DiagnosticMessage message : diagnosticMessages) {
            ComponentModelingDiagnostics diagnostic = new ComponentModelingDiagnostics(
                    message.getCode(), message.getDescription(), message.getSeverity(), null, null);
            diagnostics.add(diagnostic);
        }
        return diagnostics;
    }
}
