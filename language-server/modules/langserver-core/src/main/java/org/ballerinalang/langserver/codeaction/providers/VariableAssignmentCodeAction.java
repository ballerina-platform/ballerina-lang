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
import org.ballerinalang.langserver.codeaction.BallerinaCodeActionProvider;
import org.ballerinalang.langserver.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Code Action provider for variable assignment.
 *
 * @since 1.1.0
 */
@JavaSPIService("org.ballerinalang.langserver.codeaction.BallerinaCodeActionProvider")
public class VariableAssignmentCodeAction implements BallerinaCodeActionProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                           List<Diagnostic> diagnostics) {
        List<CodeAction> actions = new ArrayList<>();
        WorkspaceDocumentManager documentManager = lsContext.get(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(lsContext.get(ExecuteCommandKeys.FILE_URI_KEY));
        LSDocument document = null;
        try {
            document = documentManager.getLSDocument(filePath.get());
        } catch (WorkspaceDocumentException e) {
            // ignore
        }

        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.getMessage().toLowerCase(Locale.ROOT).contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED)) {
                actions.addAll(CommandUtil.getVariableAssignmentCommand(document, diagnostic, lsContext));
            }
        }
        return actions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNodeBased() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeActionNodeType> getCodeActionNodeTypes() {
        return null;
    }
}
