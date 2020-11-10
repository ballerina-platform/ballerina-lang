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
package org.ballerinalang.langserver.codeaction.providers;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.impl.CreateFunctionTestCodeAction;
import org.ballerinalang.langserver.codeaction.impl.CreateServiceTestCodeAction;
import org.ballerinalang.langserver.codeaction.impl.NodeBasedCodeAction;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.LSCodeActionProviderException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Code Action provider for create variable command.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class CreateTestCodeActionProvider extends AbstractCodeActionProvider {
    public CreateTestCodeActionProvider() {
        super(Arrays.asList(CodeActionNodeType.FUNCTION, CodeActionNodeType.OBJECT));
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionNodeType nodeType, LSContext context,
                                                    List<Diagnostic> allDiagnostics) {
        List<CodeAction> actions = new ArrayList<>();
        NodeBasedCodeAction createFunctionTestCodeAction = new CreateFunctionTestCodeAction();
        NodeBasedCodeAction createServiceTestCodeAction = new CreateServiceTestCodeAction();
        try {
            switch (nodeType.name()) {
                case CommonKeys.FUNCTION_KEYWORD_KEY:
                    actions.addAll(createFunctionTestCodeAction.get(nodeType, allDiagnostics, context));
                    break;
                case CommonKeys.SERVICE_KEYWORD_KEY:
                    actions.addAll(createServiceTestCodeAction.get(nodeType, allDiagnostics, context));
                    break;
                default:
            }
        } catch (LSCodeActionProviderException e) {
            // ignore
        }
        return actions;
    }
}
