/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.toml.semantic.diagnostics;

import io.ballerina.toml.internal.diagnostics.DiagnosticErrorCode;
import io.ballerina.toml.internal.diagnostics.DiagnosticMessageHelper;
import io.ballerina.tools.diagnostics.DiagnosticInfo;

/**
 * Diagnostic logger.
 *
 * @since 2.0.0
 */
public class DiagnosticLog {

    private static DiagnosticLog instance;

    private DiagnosticLog() {
    }

    public static synchronized DiagnosticLog getInstance() {
        if (instance == null) {
            instance = new DiagnosticLog();
        }
        return instance;
    }

    public TomlDiagnostic error(TomlNodeLocation location, DiagnosticErrorCode code) {
        DiagnosticInfo info = new DiagnosticInfo(code.diagnosticId(), code.messageKey(), code.severity());
        return new TomlDiagnostic(location, info , DiagnosticMessageHelper.getDiagnosticMessage(code));
    }
}
