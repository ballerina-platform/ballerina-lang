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
package org.wso2.ballerinalang.compiler.util.diagnotic;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Diagnostic logger.
 *
 * @since 0.94
 */
public class BLangDiagnosticLog implements DiagnosticLog {

    private static final CompilerContext.Key<BLangDiagnosticLog> DIAGNOSTIC_LOG_KEY =
            new CompilerContext.Key<>();

    private static ResourceBundle messages =
            ResourceBundle.getBundle("compiler", Locale.getDefault());

    private static final String errMsgKeyPrefix = "error" + ".";
    private static final String warningMsgKeyPrefix = "warning" + ".";
    private static final String noteMsgKeyPrefix = "note" + ".";

    public int errorCount = 0;

    private DiagnosticListener listener;
    private PackageCache pkgCache;

    public static BLangDiagnosticLog getInstance(CompilerContext context) {
        BLangDiagnosticLog dLogger = context.get(DIAGNOSTIC_LOG_KEY);
        if (dLogger == null) {
            dLogger = new BLangDiagnosticLog(context);
        }

        return dLogger;
    }

    private BLangDiagnosticLog(CompilerContext context) {
        context.put(DIAGNOSTIC_LOG_KEY, this);

        this.pkgCache = PackageCache.getInstance(context);
        this.listener = context.get(DiagnosticListener.class);
        if (this.listener == null) {
            this.listener = new DefaultDiagnosticListener();
        }
    }

    public void error(DiagnosticPos pos, DiagnosticCode code, Object... args) {
        String msg = formatMessage(errMsgKeyPrefix, code, args);
        reportDiagnostic(new BDiagnostic(Diagnostic.Kind.ERROR, pos, code, msg));
    }

    public void warning(DiagnosticPos pos, DiagnosticCode code, Object... args) {
        String msg = formatMessage(warningMsgKeyPrefix, code, args);
        reportDiagnostic(new BDiagnostic(Diagnostic.Kind.WARNING, pos, code, msg));
    }

    public void note(DiagnosticPos pos, DiagnosticCode code, Object... args) {
        String msg = formatMessage(noteMsgKeyPrefix, code, args);
        reportDiagnostic(new BDiagnostic(Diagnostic.Kind.NOTE, pos, code, msg));
    }

    @Override
    public void logDiagnostic(Diagnostic.Kind kind, Diagnostic.DiagnosticPosition pos, CharSequence message) {
        reportDiagnostic(new BDiagnostic(kind, (DiagnosticPos) pos, message.toString()));
    }


    // private methods

    private String formatMessage(String prefix, DiagnosticCode code, Object[] args) {
        String msgKey = messages.getString(prefix + code.getValue());
        return MessageFormat.format(msgKey, args);
    }

    private void reportDiagnostic(BDiagnostic diagnostic) {
        if (diagnostic.kind == Diagnostic.Kind.ERROR) {
            errorCount++;
        }
        storeDiagnosticInPackage(diagnostic.pos.src.pkgID, diagnostic);

        // Notify the listener
        this.listener.received(diagnostic);
    }

    private void storeDiagnosticInPackage(PackageID pkgId, BDiagnostic diagnostic) {
        BLangPackage pkgNode = this.pkgCache.get(pkgId);
        pkgNode.diagCollector.addDiagnostic(diagnostic);
    }
}
