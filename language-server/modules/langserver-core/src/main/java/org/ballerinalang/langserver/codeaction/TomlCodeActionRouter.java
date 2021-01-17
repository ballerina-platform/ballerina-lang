/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import org.ballerinalang.langserver.commons.CodeActionContext;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Code Action Router for Kubernetes.toml.
 *
 * @since 2.0.0
 */
public class TomlCodeActionRouter {

    private final List<K8sDiagnosticsBasedCodeAction> actionProviders;

    public TomlCodeActionRouter() {
        this.actionProviders = new ArrayList<>();
        ServiceLoader.load(K8sDiagnosticsBasedCodeAction.class).forEach(actionProviders::add);
    }

    /**
     * Returns a list of supported code actions.
     *
     * @param ctx {@link CodeActionContext}
     * @return list of code actions
     */
    public List<CodeAction> getAvailableCodeActions(CodeActionContext ctx) {
        List<CodeAction> codeActions = new ArrayList<>();
        List<Diagnostic> cursorDiagnostics = ctx.cursorDiagnostics();
        if (cursorDiagnostics != null && !cursorDiagnostics.isEmpty()) {
            for (Diagnostic diagnostic : cursorDiagnostics) {
                codeActions.addAll(handleDiagnostics(diagnostic, ctx));
            }
        }
        return codeActions;
    }

    private List<CodeAction> handleDiagnostics(Diagnostic diagnostic, CodeActionContext ctx) {
        for (K8sDiagnosticsBasedCodeAction action : actionProviders) {
            if (action.validate(diagnostic, ctx)) {
                return action.handle(diagnostic, ctx);
            }
        }
        return new ArrayList<>();
    }
}
