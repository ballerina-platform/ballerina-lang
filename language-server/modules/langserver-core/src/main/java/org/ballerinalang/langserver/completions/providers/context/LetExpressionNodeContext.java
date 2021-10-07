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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the completions for {@link AssignmentStatementNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class LetExpressionNodeContext extends AbstractCompletionProvider<LetExpressionNode> {

    public LetExpressionNodeContext() {
        super(LetExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, LetExpressionNode node) throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

        if (withinTypeDescContext(context, node)) {
            completionItems.addAll(this.getTypeDescContextItems(context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        } else if (node.inKeyword().isMissing()) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_IN.get()));
        } else if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            /*
            Covers the cases where the cursor is within the expression context
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> exprEntries = QNameReferenceUtil.getExpressionContextEntries(context, qNameRef);

            completionItems.addAll(this.getCompletionItemList(exprEntries, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private boolean withinTypeDescContext(BallerinaCompletionContext context, LetExpressionNode node) {
        /*
        Check whether the following is satisfied
        (1) let <cursor>typedesc ...
        (2) let <cursor>
        (3) let t<cursor>
         */
        SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations = node.letVarDeclarations();
        LetVariableDeclarationNode letVariableDeclarationNode = letVarDeclarations.get(letVarDeclarations.size() - 1);
        
        TypedBindingPatternNode typedBindingPatternNode = letVariableDeclarationNode.typedBindingPattern();
        if (typedBindingPatternNode.isMissing() || typedBindingPatternNode.typeDescriptor().isMissing()) {
            return false;
        }
        TypeDescriptorNode typeDescriptorNode = typedBindingPatternNode.typeDescriptor();
        LinePosition typeDescEnd = typeDescriptorNode.lineRange().endLine();
        Position cursor = context.getCursorPosition();
        return (cursor.getLine() < typeDescEnd.line())
                || (cursor.getLine() == typeDescEnd.line() && cursor.getCharacter() <= typeDescEnd.offset());
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, LetExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        LetVariableDeclarationNode letVariableDeclarationNode = node.letVarDeclarations().
                get(node.letVarDeclarations().size() - 1);
        return !letVariableDeclarationNode.equalsToken().isMissing() 
                && letVariableDeclarationNode.equalsToken().textRange().startOffset() < cursor;
    }
}
