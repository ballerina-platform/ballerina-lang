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

import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DefaultDiagnosticListener;

import java.util.Locale;
import java.util.ResourceBundle;

public class BallerinaDiagnosticLog {
    private static final CompilerContext.Key<BallerinaDiagnosticLog> DIAGNOSTIC_LOG_KEY =
            new CompilerContext.Key<>();

    private static final String errMsgKeyPrefix = "error" + ".";
    private static final String warningMsgKeyPrefix = "warning" + ".";
    private static final String noteMsgKeyPrefix = "note" + ".";

    private static ResourceBundle messages =
            ResourceBundle.getBundle("compiler", Locale.getDefault());

    private int errorCount = 0;

    private BallerinaDiagnosticListener diagnosticListener;
    private PackageCache packageCache;

    private BallerinaDiagnosticLog(CompilerContext context) {
        context.put(DIAGNOSTIC_LOG_KEY, this);

        this.packageCache = PackageCache.getInstance(context);
        this.diagnosticListener = context.get(BallerinaDiagnosticListener.class);
        if (this.diagnosticListener == null) {
            this.diagnosticListener = new BallerinaDiagnosticListener();
        }
    }

    public static BallerinaDiagnosticLog getInstance(CompilerContext context) {
        BallerinaDiagnosticLog dLogger = context.get(DIAGNOSTIC_LOG_KEY);
        if (dLogger == null) {
            dLogger = new BallerinaDiagnosticLog(context);
        }

        return dLogger;
    }


}
