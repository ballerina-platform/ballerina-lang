/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.providers.imports;

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Code Action to remove import module.
 *
 * @since 2201.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class RemoveImportCodeAction extends AbstractCodeActionProvider {
    public static final String NAME = "remove import";
    public static final Set<String> DIAGNOSTIC_CODES = Set.of("BCE2002", "BCE2004");

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code()) 
                && CodeActionNodeValidator.validate(context.nodeAtCursor());
    }

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails, 
                                                    CodeActionContext context) {
        ImportDeclarationNode importDeclNode = positionDetails.matchedNode().kind() == SyntaxKind.IMPORT_DECLARATION 
                ? (ImportDeclarationNode) positionDetails.matchedNode() 
                : (ImportDeclarationNode) positionDetails.matchedNode().parent();
        if (importDeclNode.orgName().isEmpty() || context.currentDocument().isEmpty()) {
            return Collections.emptyList();
        }
        Range range = CommonUtil.toRange(importDeclNode.textRange().startOffset(), 
                importDeclNode.textRangeWithMinutiae().endOffset(), (context.currentDocument().get().textDocument()));
        List<TextEdit> edits = List.of(new TextEdit(range, ""));
        String moduleName =  importDeclNode.prefix().isPresent() ? importDeclNode.prefix().get().prefix().toString() 
                : importDeclNode.orgName().get() 
                + importDeclNode.moduleName().stream().map(Node::toString).collect(Collectors.joining("."));
        String commandTitle = String.format(CommandConstants.REMOVE_IMPORT, moduleName);
        return List.of(createCodeAction(commandTitle, edits, context.fileUri(),
                CodeActionKind.QuickFix));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
