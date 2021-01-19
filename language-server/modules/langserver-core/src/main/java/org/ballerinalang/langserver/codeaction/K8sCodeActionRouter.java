/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.codeaction.extensions.K8sDiagnosticsBasedCodeAction;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Code Action Router for Kubernetes.toml.
 *
 * @since 2.0.0
 */
public class K8sCodeActionRouter {

    /**
     * Returns a list of supported code actions.
     *
     * @param ctx {@link CodeActionContext}
     * @return list of code actions
     */
    public static List<CodeAction> getAvailableCodeActions(CodeActionContext ctx) {
        List<CodeAction> codeActions = new ArrayList<>();
        List<Diagnostic> cursorDiagnostics = ctx.cursorDiagnostics();
        if (!cursorDiagnostics.isEmpty()) {
            for (Diagnostic diagnostic : cursorDiagnostics) {
                codeActions.addAll(handleDiagnostics(diagnostic, ctx));
            }
        }
        return codeActions;
    }

    private static List<CodeAction> handleDiagnostics(Diagnostic diagnostic, CodeActionContext ctx) {
        for (K8sDiagnosticsBasedCodeAction action : K8sCodeActionHolder.getAllCodeActions()) {
            if (action.validate(diagnostic, ctx)) {
                return action.handle(diagnostic, ctx);
            }
        }
        return Collections.emptyList();
    }
}
