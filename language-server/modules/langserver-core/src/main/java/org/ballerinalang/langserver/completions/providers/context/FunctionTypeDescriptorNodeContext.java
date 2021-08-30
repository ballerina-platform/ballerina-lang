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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.FunctionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link FunctionTypeDescriptorNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class FunctionTypeDescriptorNodeContext extends AbstractCompletionProvider<FunctionTypeDescriptorNode> {

    public FunctionTypeDescriptorNodeContext() {
        super(FunctionTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, FunctionTypeDescriptorNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (onSuggestionsAfterQualifiers(context, node)) {
            /*
             * Covers the following
             * type T <qualifier(s)> <cursor>
             * Currently the qualifier can be isolated/transactional.
             */
            NodeList<Token> qualifiers = node.qualifierList();
            List<Token> otherQuals = node.leadingInvalidTokens();
            Token lastQualifier;
            if (qualifiers.size() > 0) {
                lastQualifier = qualifiers.get(qualifiers.size() - 1);
            } else {
                lastQualifier = otherQuals.get(otherQuals.size() - 1);
            }
            Set<SyntaxKind> qualKinds = qualifiers.stream().map(Token::kind).collect(Collectors.toSet());
            qualKinds.addAll(otherQuals.stream().map(Token::kind).collect(Collectors.toSet()));
            completionItems.addAll(getCompletionItemsOnQualifiers(qualKinds, lastQualifier, context));
        } else if (this.withinParameterContext(context, node)) {
            /*
            Covers the completions when the cursor is within the parameter context
             */
            if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
                List<Symbol> typesInModule = QNameReferenceUtil.getTypesInModule(context,
                        ((QualifiedNameReferenceNode) nodeAtCursor));
                completionItems.addAll(this.getCompletionItemList(typesInModule, context));
            } else {
                completionItems.addAll(this.getTypeDescContextItems(context));
            }
        } else if (this.withinReturnKWContext(context, node)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_RETURNS.get()));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private boolean withinParameterContext(BallerinaCompletionContext context, FunctionTypeDescriptorNode node) {
        if (node.functionSignature().isEmpty()) {
            return false;
        }
        FunctionSignatureNode functionSignatureNode = node.functionSignature().get();
        int txtPosInTree = context.getCursorPositionInTree();
        TextRange openParanRange = functionSignatureNode.openParenToken().textRange();
        TextRange closeParanRange = functionSignatureNode.closeParenToken().textRange();

        return openParanRange.endOffset() <= txtPosInTree && txtPosInTree <= closeParanRange.startOffset();
    }

    private boolean withinReturnKWContext(BallerinaCompletionContext context, FunctionTypeDescriptorNode node) {
        if (node.functionSignature().isEmpty()) {
            return false;
        }
        FunctionSignatureNode functionSignatureNode = node.functionSignature().get();
        int txtPosInTree = context.getCursorPositionInTree();
        TextRange closeParanRange = functionSignatureNode.closeParenToken().textRange();
        Optional<ReturnTypeDescriptorNode> returnTypeDescNode = functionSignatureNode.returnTypeDesc();

        return closeParanRange.startOffset() <= txtPosInTree && (!returnTypeDescNode.isPresent()
                || returnTypeDescNode.get().returnsKeyword().isMissing());
    }

    private boolean onSuggestionsAfterQualifiers(BallerinaCompletionContext context, FunctionTypeDescriptorNode node) {
        int cursor = context.getCursorPositionInTree();
        NodeList<Token> qualifiers = node.qualifierList();
        Token functionKeyword = node.functionKeyword();

        Token lastQualifier;
        if (qualifiers.isEmpty()) {
            List<Token> otherQuals = node.leadingInvalidTokens();
            if (otherQuals.size() > 0) {
                lastQualifier = otherQuals.get(otherQuals.size() - 1);
            } else {
                return false;
            }
        } else {
            lastQualifier = qualifiers.get(qualifiers.size() - 1);
        }
        return cursor > lastQualifier.textRange().endOffset()
                && (functionKeyword.isMissing() || cursor < functionKeyword.textRange().startOffset());
    }
}
