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

import org.ballerinalang.langserver.compiler.LSContext;
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
public class BallerinaCodeActionRouter implements CodeActionRouter {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getBallerinaCodeActions(CodeActionNodeType nodeType, LSContext context,
                                                    List<Diagnostic> diagnostics) {
        BallerinaCodeActionProviderFactory codeActionProvider = BallerinaCodeActionProviderFactory.getInstance();
        codeActionProvider.initiate();
        List<CodeAction> codeActions = new ArrayList<>();

        if (nodeType != null) {
            Map<CodeActionNodeType, List<BallerinaCodeActionProvider>> nodeBasedProviders =
                    BallerinaCodeActionProviderFactory.getNodeBasedProviders();
            if (nodeBasedProviders.containsKey(nodeType)) {
                nodeBasedProviders.get(nodeType).forEach(ballerinaCodeAction -> {
                    codeActions.addAll(ballerinaCodeAction.getCodeActions(nodeType, context, null));
                });

            }
        }
        if (diagnostics != null && diagnostics.size() > 0) {
            BallerinaCodeActionProviderFactory.getDiagnosticsBasedProviders().forEach(ballerinaCodeAction -> {
                List<CodeAction> codeActionList = ballerinaCodeAction.getCodeActions(nodeType, context, diagnostics);
                if (codeActionList != null) {
                    codeActions.addAll(codeActionList);
                }
            });
        }
        return codeActions;
    }
}
