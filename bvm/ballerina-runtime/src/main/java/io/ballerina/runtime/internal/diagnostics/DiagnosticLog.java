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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.ballerina.runtime.internal.diagnostics.DiagnosticSeverity.ERROR;
import static io.ballerina.runtime.internal.diagnostics.DiagnosticSeverity.WARNING;

/**
 * A diagnostic log manages the runtime diagnostics.
 *
 * @since 2.0.0
 */
public class DiagnosticLog {

    private List<Diagnostic> diagnosticList = new LinkedList<>();

    private int errorCount = 0;

    private int warnCount = 0;

    public void error(String message) {
        errorCount += 1;
        diagnosticList.add(new Diagnostic(ERROR, message));
    }

    public void warn(String message) {
        warnCount += 1;
        diagnosticList.add(new Diagnostic(WARNING, message));
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getWarningCount() {
        return warnCount;
    }

    public List<Diagnostic> getDiagnosticList() {
        return new ArrayList<>(diagnosticList);
    }
}
