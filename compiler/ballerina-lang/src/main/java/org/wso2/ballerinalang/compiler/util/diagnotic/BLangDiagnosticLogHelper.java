/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.util.diagnotic;

import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * A diagnostic log helper that holds different {@link BLangDiagnosticLog} implementations to alternate between
 * logging to the console and just incrementing an error count.
 *
 * @since 1.2.0
 */
public class BLangDiagnosticLogHelper {

    private final BLangDiagnosticLog consoleDLog;
    private final ErrorCountingBLangDiagnosticLog nonConsoleDLog;

    private BLangDiagnosticLog currentLog;

    private static final CompilerContext.Key<BLangDiagnosticLogHelper> DIAGNOSTIC_LOG_HELPER_KEY =
            new CompilerContext.Key<>();

    public static BLangDiagnosticLogHelper getInstance(CompilerContext context) {
        BLangDiagnosticLogHelper dLogHelper = context.get(DIAGNOSTIC_LOG_HELPER_KEY);
        if (dLogHelper == null) {
            dLogHelper = new BLangDiagnosticLogHelper(context);
        }

        return dLogHelper;
    }

    private BLangDiagnosticLogHelper(CompilerContext context) {
        context.put(DIAGNOSTIC_LOG_HELPER_KEY, this);

        this.consoleDLog = BLangDiagnosticLog.getInstance(context);
        this.currentLog = this.consoleDLog;
        this.nonConsoleDLog = new ErrorCountingBLangDiagnosticLog();
    }

    public void error(DiagnosticPos pos, DiagnosticCode code, Object... args) {
        this.currentLog.error(pos, code, args);
    }

    public void warning(DiagnosticPos pos, DiagnosticCode code, Object... args) {
        this.currentLog.warning(pos, code, args);
    }

    public void note(DiagnosticPos pos, DiagnosticCode code, Object... args) {
        this.currentLog.note(pos, code, args);
    }

    public BLangDiagnosticLog getCurrentLog() {
        return this.currentLog;
    }

    public void setCurrentLog(BLangDiagnosticLog bLangDiagnosticLog) {
        this.currentLog = bLangDiagnosticLog;
    }

    public void setNonConsoleDLog() {
        currentLog = nonConsoleDLog;
    }

    public int getErrorCount() {
        return currentLog.errorCount;
    }

    public void resetErrorCount() {
        currentLog.errorCount = 0;
    }

    /**
     * An error counting diagnostic log implementation used for type checking without logging errors to the console.
     *
     * @since 1.2.0
     */
    static class ErrorCountingBLangDiagnosticLog extends BLangDiagnosticLog {

        @Override
        protected void reportDiagnostic(BDiagnostic diagnostic) {
            if (diagnostic.kind == Diagnostic.Kind.ERROR) {
                errorCount++;
            }
        }
    }
}
