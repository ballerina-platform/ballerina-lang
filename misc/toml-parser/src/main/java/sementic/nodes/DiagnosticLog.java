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
package sementic.nodes;

import sementic.tools.DiagnosticCode;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Diagnostic logger.
 *
 * @since 0.94
 */
public class DiagnosticLog {

    private static DiagnosticLog instance = null;

    private static ResourceBundle messages =
            ResourceBundle.getBundle("compiler", Locale.getDefault());

    private static final String errMsgKeyPrefix = "error" + ".";
    private static final String warningMsgKeyPrefix = "warning" + ".";
    private static final String noteMsgKeyPrefix = "note" + ".";

    private int errorCount;
    private List<Diagnostic> diagnostics;

    private DiagnosticLog() {
        this.diagnostics = new ArrayList<>();
        ;
    }

    public static DiagnosticLog getInstance() {
        if (instance == null) {
            instance = new DiagnosticLog();
        }
        return instance;
    }

    // Please node that, this method exist only to report syntax errors coming from the new parser
    // We will remove this method once we merge diagnostic reporting approaches in the new parser and semantic analyzer
    public void error(DiagnosticPos pos, String message, DiagnosticCode code) {
        reportDiagnostic(new TomlDiagnostic(Diagnostic.Kind.ERROR, pos, code, message));
    }

    public void error(DiagnosticPos pos, DiagnosticCode code, Object... args) {
        String msg = formatMessage(errMsgKeyPrefix, code, args);
        reportDiagnostic(new TomlDiagnostic(Diagnostic.Kind.ERROR, pos, code, msg));
    }

    public void warning(DiagnosticPos pos, DiagnosticCode code, Object... args) {
        String msg = formatMessage(warningMsgKeyPrefix, code, args);
        reportDiagnostic(new TomlDiagnostic(Diagnostic.Kind.WARNING, pos, code, msg));
    }

    public void note(DiagnosticPos pos, DiagnosticCode code, Object... args) {
        String msg = formatMessage(noteMsgKeyPrefix, code, args);
        reportDiagnostic(new TomlDiagnostic(Diagnostic.Kind.NOTE, pos, code, msg));
    }

    public void logDiagnostic(Diagnostic.Kind kind, Diagnostic.DiagnosticPosition pos, CharSequence message) {
        reportDiagnostic(new TomlDiagnostic(kind, (DiagnosticPos) pos, message.toString()));
    }

    // private methods

    private String formatMessage(String prefix, DiagnosticCode code, Object[] args) {
        String msgKey = messages.getString(prefix + code.getValue());
        return MessageFormat.format(msgKey, args);
    }

    protected void reportDiagnostic(TomlDiagnostic diagnostic) {
        if (diagnostic.kind == Diagnostic.Kind.ERROR) {
        }
        addDiagnostic(diagnostic);
    }

    public void addDiagnostic(Diagnostic diagnostic) {
        this.diagnostics.add(diagnostic);
        if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
            this.errorCount++;
        }
        Collections.sort(diagnostics);
    }

    public int getErrorCount() {

        return errorCount;
    }

    public boolean hasErrors() {
        return this.errorCount > 0;
    }

    public List<Diagnostic> getDiagnostics() {

        return diagnostics;
    }
}
