/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider;
import org.ballerinalang.langserver.compiler.LSClientLogger;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the Code Action router.
 *
 * @since 1.1.1
 */
public class CodeActionRouter {

    /**
     * Returns a list of supported code actions.
     *
     * @param nodeType           code action node type
     * @param context            ls context
     * @param diagnosticsOfRange list of diagnostics of the cursor range
     * @param allDiagnostics     list of all diagnostics
     * @return list of code actions
     */
    public static List<CodeAction> getBallerinaCodeActions(CodeActionNodeType nodeType, LSContext context,
                                                           List<Diagnostic> diagnosticsOfRange,
                                                           List<Diagnostic> allDiagnostics) {
        List<CodeAction> codeActions = new ArrayList<>();
        CodeActionProvidersHolder codeActionProvidersHolder = CodeActionProvidersHolder.getInstance();
        if (nodeType != null) {
            Map<CodeActionNodeType, List<LSCodeActionProvider>> nodeBasedProviders =
                    codeActionProvidersHolder.getNodeBasedProviders();
            if (nodeBasedProviders.containsKey(nodeType)) {
                nodeBasedProviders.get(nodeType).forEach(provider -> {
                    try {
                        List<CodeAction> codeActionList = provider.getNodeBasedCodeActions(nodeType, context,
                                                                                           allDiagnostics);
                        if (codeActionList != null) {
                            codeActions.addAll(codeActionList);
                        }
                    } catch (Exception e) {
                        String msg = "CodeAction '" + provider.getClass().getSimpleName() + "' failed!";
                        LSClientLogger.logError(msg, e, null, (Position) null);
                    }
                });
            }
        }
        if (diagnosticsOfRange != null && diagnosticsOfRange.size() > 0) {
            codeActionProvidersHolder.getDiagnosticsBasedProviders().forEach(provider -> {
                try {
                    List<CodeAction> codeActionList = provider.getDiagBasedCodeActions(nodeType, context,
                                                                                       diagnosticsOfRange,
                                                                                       allDiagnostics);
                    if (codeActionList != null) {
                        codeActions.addAll(codeActionList);
                    }
                } catch (Exception e) {
                    String msg = "CodeAction '" + provider.getClass().getSimpleName() + "' failed!";
                    LSClientLogger.logError(msg, e, null, (Position) null);
                }
            });
        }
        return codeActions;
    }
}
