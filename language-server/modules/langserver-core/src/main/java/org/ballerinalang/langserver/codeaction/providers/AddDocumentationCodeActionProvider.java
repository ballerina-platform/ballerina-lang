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
import org.ballerinalang.langserver.codeaction.impl.AddAllDocumentationCodeAction;
import org.ballerinalang.langserver.codeaction.impl.AddDocumentationCodeAction;
import org.ballerinalang.langserver.codeaction.impl.NodeBasedCodeAction;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.LSCodeActionProviderException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Code Action provider for adding documentation.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddDocumentationCodeActionProvider extends AbstractCodeActionProvider {
    public AddDocumentationCodeActionProvider() {
        super(Arrays.asList(CodeActionNodeType.FUNCTION,
                            CodeActionNodeType.OBJECT,
                            CodeActionNodeType.CLASS,
                            CodeActionNodeType.SERVICE,
                            CodeActionNodeType.RESOURCE,
                            CodeActionNodeType.RECORD,
                            CodeActionNodeType.OBJECT_FUNCTION,
                            CodeActionNodeType.CLASS_FUNCTION));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionNodeType nodeType, LSContext context,
                                                    List<Diagnostic> allDiagnostics) {
        NodeBasedCodeAction addAllDocumentationCodeAction = new AddAllDocumentationCodeAction();
        NodeBasedCodeAction addDocumentationCodeAction = new AddDocumentationCodeAction();

        List<CodeAction> actions = new ArrayList<>();
        try {
            actions.addAll(addDocumentationCodeAction.get(nodeType, allDiagnostics, context));
            actions.addAll(addAllDocumentationCodeAction.get(nodeType, allDiagnostics, context));
        } catch (LSCodeActionProviderException e) {
            // ignore
        }
        return actions;
    }
}
