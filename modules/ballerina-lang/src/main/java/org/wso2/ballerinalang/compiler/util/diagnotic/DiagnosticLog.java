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

import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * Diagnostic logger.
 *
 * @since 0.94
 */
public class DiagnosticLog {
    private static final CompilerContext.Key<DiagnosticLog> DIAGNOSTIC_LOG_KEY =
            new CompilerContext.Key<>();

    private DiagnosticListener listener;

    public static DiagnosticLog getInstance(CompilerContext context) {
        DiagnosticLog dLogger = context.get(DIAGNOSTIC_LOG_KEY);
        if (dLogger == null) {
            dLogger = new DiagnosticLog(context);
        }

        return dLogger;
    }

    private DiagnosticLog(CompilerContext context) {
        context.put(DIAGNOSTIC_LOG_KEY, this);

        listener = context.get(DiagnosticListener.class);
        if (listener == null) {
            listener = new DefaultDiagnosticListener();
        }
    }

    public void error(DiagnosticPos pos, String key, Object... args) {

        // TODO generate the proper message with args
        String msg = key;
        BDiagnostic diagnostic = new BDiagnostic(
                Diagnostic.Kind.ERROR, pos.src, pos, msg);

        listener.received(diagnostic);
    }

    public void warning(DiagnosticPos pos, String key, Object... args) {
    }

    public void note(DiagnosticPos pos, String key, Object... args) {
    }
}
