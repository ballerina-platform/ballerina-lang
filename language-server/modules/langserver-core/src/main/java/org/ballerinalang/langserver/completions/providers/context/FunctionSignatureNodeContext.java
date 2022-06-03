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

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link FunctionSignatureNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class FunctionSignatureNodeContext extends AbstractCompletionProvider<FunctionSignatureNode> {

    public FunctionSignatureNodeContext() {
        super(FunctionSignatureNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, FunctionSignatureNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (withinReturnTypeDescContext(context, node)) {
            var returnTypeDesc = node.returnTypeDesc();
            if (returnTypeDesc.isEmpty() || returnTypeDesc.get().returnsKeyword().isMissing()) {
                /*
                Covers the following cases.
                (1) function test() <cursor>
                (2) var lambda = function() r<cursor>
                */
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_RETURNS.get()));
            } else {
                /*
                Covers the following cases.
                (1) function test() returns <cursor>
                */
                completionItems.addAll(CompletionUtil.route(context, returnTypeDesc.get()));
            }
        } else if (this.withinParameterContext(context, node)) {
            NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
            if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
                /*
                Covers the Following
                (1) function(mod:<cursor>)
                (2) function(mod:T<cursor>)
                 */
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
                completionItems.addAll(this.getCompletionItemList(QNameRefCompletionUtil
                        .getTypesInModule(context, qNameRef), context));
            } else {
                /*
                Covers the Following
                (1) function(<cursor>)
                (2) function(T arg1,<cursor>)
                 */
                completionItems.addAll(this.getTypeDescContextItems(context));
            }
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private boolean withinReturnTypeDescContext(BallerinaCompletionContext context, FunctionSignatureNode node) {
        int cursor = context.getCursorPositionInTree();
        Token closeParenToken = node.closeParenToken();

        return cursor > closeParenToken.textRange().startOffset();
    }

    private boolean withinParameterContext(BallerinaCompletionContext context, FunctionSignatureNode node) {
        int cursor = context.getCursorPositionInTree();
        int openParan = node.openParenToken().textRange().endOffset();
        int closeParan = node.closeParenToken().textRange().startOffset();

        return openParan <= cursor && cursor <= closeParan;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, FunctionSignatureNode node) {
        // If the signature belongs to the function type descriptor, we skip this resolver
        return !node.openParenToken().isMissing() && !node.closeParenToken().isMissing()
                && node.parent().kind() != SyntaxKind.FUNCTION_TYPE_DESC;
    }

    @Override
    public void sort(BallerinaCompletionContext context, FunctionSignatureNode node,
                     List<LSCompletionItem> completionItems) {
        if (withinParameterContext(context, node)) {
            completionItems.forEach(completionItem -> {
                String sortText = SortingUtil.genSortTextForTypeDescContext(context, completionItem);
                completionItem.getCompletionItem().setSortText(sortText);
            });
        }
    }
}
