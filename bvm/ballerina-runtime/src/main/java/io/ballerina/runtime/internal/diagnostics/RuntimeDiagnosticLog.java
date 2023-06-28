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
package io.ballerina.runtime.internal.diagnostics;

import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A diagnostic log manages the runtime diagnostics.
 *
 * @since 2.0.0
 */
public class RuntimeDiagnosticLog {

    private List<RuntimeDiagnostic> diagnosticList = new LinkedList<>();

    private int errorCount = 0;

    private int warnCount = 0;

    public void error(ErrorCodes errorCode, String location, Object... args) {
        errorCount += 1;
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(errorCode.diagnosticId(), errorCode.messageKey(),
                                                           DiagnosticSeverity.ERROR);
        diagnosticList.add(new RuntimeDiagnostic(diagnosticInfo, location, args));
    }

    public void warn(ErrorCodes errorCode, String location, Object... args) {
        warnCount += 1;
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(errorCode.diagnosticId(), errorCode.messageKey(),
                                                           DiagnosticSeverity.WARNING);
        diagnosticList.add(new RuntimeDiagnostic(diagnosticInfo, location, args));
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getWarningCount() {
        return warnCount;
    }

    public List<RuntimeDiagnostic> getDiagnosticList() {
        return new ArrayList<>(diagnosticList);
    }
}
