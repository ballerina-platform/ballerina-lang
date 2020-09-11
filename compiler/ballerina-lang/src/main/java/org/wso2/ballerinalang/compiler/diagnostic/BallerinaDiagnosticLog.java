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
package org.wso2.ballerinalang.compiler.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition;
import org.ballerinalang.util.diagnostic.Diagnostic.Kind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Logger class for logging various compiler diagnostics.
 * 
 * @since 2.0.0
 */
public class BallerinaDiagnosticLog implements DiagnosticLog {

    private static final CompilerContext.Key<BallerinaDiagnosticLog> DIAGNOSTIC_LOG_KEY = new CompilerContext.Key<>();
    private static final String ERROR_PREFIX = "error.";
    private static final String WARNING_PREFIX = "warning.";
    private static final String NOTE_PREFIX = "note.";
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("compiler", Locale.getDefault());

    private int errorCount = 0;
    private PackageCache packageCache;
    private boolean isMute = false;
    private PrintStream console = System.err;

    private BallerinaDiagnosticLog(CompilerContext context) {
        context.put(DIAGNOSTIC_LOG_KEY, this);
        this.packageCache = PackageCache.getInstance(context);
    }

    public static BallerinaDiagnosticLog getInstance(CompilerContext context) {
        BallerinaDiagnosticLog dLogger = context.get(DIAGNOSTIC_LOG_KEY);
        if (dLogger == null) {
            dLogger = new BallerinaDiagnosticLog(context);
        }

        return dLogger;
    }

    /**
     * Log an error.
     * 
     * @param pos Position of the error in the source code.
     * @param code Error code
     * @param args Parameters associated with the error
     */
    public void error(DiagnosticPos pos, DiagnosticCode code, Object... args) {
        String msg = formatMessage(ERROR_PREFIX, code, args);
        reportDiagnostic(pos, msg, DiagnosticSeverity.ERROR);
    }

    /**
     * Log a warning.
     * 
     * @param pos Position of the warning in the source code.
     * @param code Error code
     * @param args Parameters associated with the error
     */
    public void warning(DiagnosticPos pos, DiagnosticCode code, Object... args) {
        String msg = formatMessage(WARNING_PREFIX, code, args);
        reportDiagnostic(pos, msg, DiagnosticSeverity.WARNING);
    }

    /**
     * Log an info.
     * 
     * @param pos Position of the info in the source code.
     * @param code Error code
     * @param args Parameters associated with the info
     */
    public void note(DiagnosticPos pos, DiagnosticCode code, Object... args) {
        String msg = formatMessage(NOTE_PREFIX, code, args);
        reportDiagnostic(pos, msg, DiagnosticSeverity.INFO);
    }

    /**
     * Get the number of error logged in this logger.
     *
     * @return Number of errors logged.
     */
    public int errorCount() {
        return this.errorCount;
    }

    /**
     * Reset error count.
     */
    public void resetErrorCount() {
        this.errorCount = 0;
    }

    /**
     * Mute the logger. This will stop reporting the diagnostic.
     * However it will continue to keep track of the number of errors.
     */
    public void mute() {
        this.isMute = true;
    }

    /**
     * Unmute the logger. This will start reporting the diagnostic.
     */
    public void unmute() {
        this.isMute = false;
    }

    /**
     * Logs a message of the specified {@link DiagnosticSeverity} at the {@link Diagnostic.DiagnosticPosition}.
     *
     * @param severity the severity of the diagnostic
     * @param pos the position of the source code element.
     * @param message the message
     */
    public void logDiagnostic(DiagnosticSeverity severity, Diagnostic.DiagnosticPosition pos, CharSequence message) {
        this.reportDiagnostic((DiagnosticPos) pos, message.toString(), severity);
    }

    @Override
    public void logDiagnostic(Kind kind, DiagnosticPosition pos, CharSequence message) {
        DiagnosticSeverity severity;
        switch (kind) {
            case ERROR:
                severity = DiagnosticSeverity.ERROR;
                break;
            case WARNING:
                severity = DiagnosticSeverity.WARNING;
                break;
            case NOTE:
            default:
                severity = DiagnosticSeverity.INFO;
                break;
        }

        reportDiagnostic((DiagnosticPos) pos, message.toString(), severity);
    }

    // private helper methods

    private String formatMessage(String prefix, DiagnosticCode code, Object[] args) {
        String msgKey = MESSAGES.getString(prefix + code.getValue());
        return MessageFormat.format(msgKey, args);
    }

    protected void reportDiagnostic(DiagnosticPos pos, String msg, DiagnosticSeverity severity) {
        if (severity == DiagnosticSeverity.ERROR) {
            this.errorCount++;
        }

        if (this.isMute) {
            return;
        }

        BallerinaDiagnosticLocation diagnosticLocation =
                new BallerinaDiagnosticLocation(pos.getSource().cUnitName, pos.sLine, pos.eLine, pos.sCol, pos.eCol);
        BallerinaDiagnostic diagnostic = new BallerinaDiagnostic(diagnosticLocation, msg, severity);

        storeDiagnosticInPackage(pos.src.pkgID, diagnostic);

        switch (severity) {
            case ERROR:
                console.println("error: " + pos + " " + diagnostic.message());
                break;
            case WARNING:
                console.println("warning: " + pos + " " + diagnostic.message());
                break;
            case INTERNAL:
            case HINT:
            case INFO:
            default:
                break;
        }
    }

    private void storeDiagnosticInPackage(PackageID pkgId, BallerinaDiagnostic diagnostic) {
        BLangPackage pkgNode = this.packageCache.get(pkgId);
        pkgNode.addDiagnostic(diagnostic);
    }
}
