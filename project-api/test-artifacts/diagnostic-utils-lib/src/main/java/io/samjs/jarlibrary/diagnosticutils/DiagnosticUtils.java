/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.samjs.jarlibrary.diagnosticutils;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

/**
 * A dummy diagnostic related util class.
 *
 * @since 2.0.0
 */
public final class DiagnosticUtils {

    private DiagnosticUtils() {
    }

    public static Diagnostic createDiagnostic(String code,
                                              String messageFormat,
                                              Location location,
                                              DiagnosticSeverity severity,
                                              Object... args) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(code, messageFormat, severity);
        return DiagnosticFactory.createDiagnostic(diagnosticInfo, location, args);
    }
}
