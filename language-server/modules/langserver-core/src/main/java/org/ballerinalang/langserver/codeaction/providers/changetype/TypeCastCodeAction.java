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
package org.ballerinalang.langserver.codeaction.providers.changetype;

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code Action for incompatible types.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class TypeCastCodeAction extends AbstractCodeActionProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    CodeActionContext context) {
        if (!(diagnostic.getMessage().contains(CommandConstants.INCOMPATIBLE_TYPES))) {
            return Collections.emptyList();
        }
        PositionDetails positionDetails = context.positionDetails();
        if (positionDetails.matchedNode().kind() != SyntaxKind.LOCAL_VAR_DECL &&
                positionDetails.matchedNode().kind() != SyntaxKind.MODULE_VAR_DECL) {
            return Collections.emptyList();
        }
        Optional<ExpressionNode> initializer = getInitializer(positionDetails.matchedNode());
        if (initializer.isEmpty() || initializer.get().kind() == SyntaxKind.TYPE_CAST_EXPRESSION) {
            return Collections.emptyList();
        }
        Position editPos = CommonUtil.toPosition(initializer.get().lineRange().startLine());
        List<TextEdit> edits = new ArrayList<>();
        Optional<String> typeName = CodeActionUtil.getPossibleType(positionDetails.matchedExprType(), edits, context);
        if (typeName.isEmpty()) {
            return Collections.emptyList();
        }
        String editText = "<" + typeName.get() + "> ";
        edits.add(new TextEdit(new Range(editPos, editPos), editText));
        String commandTitle = CommandConstants.ADD_TYPE_CAST_TITLE;
        return Collections.singletonList(createQuickFixCodeAction(commandTitle, edits, context.fileUri()));
    }

    private Optional<ExpressionNode> getInitializer(Node node) {
        if (node.kind() == SyntaxKind.LOCAL_VAR_DECL) {
            return ((VariableDeclarationNode) node).initializer();
        } else if (node.kind() == SyntaxKind.MODULE_VAR_DECL) {
            return ((ModuleVariableDeclarationNode) node).initializer();
        } else {
            return Optional.empty();
        }
    }
}
