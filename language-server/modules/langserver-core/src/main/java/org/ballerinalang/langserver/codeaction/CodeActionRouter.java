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
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

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
     * @param nodeType    code action node type
     * @param context     ls context
     * @param diagnostics list of diagnostics
     * @return list of code actions
     */
    public static List<CodeAction> getBallerinaCodeActions(CodeActionNodeType nodeType, LSContext context,
                                                    List<Diagnostic> diagnostics) {
        List<CodeAction> codeActions = new ArrayList<>();
        CodeActionProvidersHolder codeActionProvidersHolder = CodeActionProvidersHolder.getInstance();
        if (nodeType != null) {
            Map<CodeActionNodeType, List<LSCodeActionProvider>> nodeBasedProviders =
                    codeActionProvidersHolder.getNodeBasedProviders();
            if (nodeBasedProviders.containsKey(nodeType)) {
                nodeBasedProviders.get(nodeType).forEach(ballerinaCodeAction -> {
                    codeActions.addAll(ballerinaCodeAction.getCodeActions(nodeType, context, null));
                });
            }
        }
        if (diagnostics != null && diagnostics.size() > 0) {
            codeActionProvidersHolder.getDiagnosticsBasedProviders().forEach(ballerinaCodeAction -> {
                List<CodeAction> codeActionList = ballerinaCodeAction.getCodeActions(nodeType, context, diagnostics);
                if (codeActionList != null) {
                    codeActions.addAll(codeActionList);
                }
            });
        }
        return codeActions;
    }
}
