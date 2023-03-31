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

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.FunctionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles the completions for the {@link io.ballerina.compiler.syntax.tree.RequiredParameterNode}.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class RequiredParameterNodeContext extends AbstractCompletionProvider<RequiredParameterNode> {

    public RequiredParameterNodeContext() {
        super(RequiredParameterNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, RequiredParameterNode node)
            throws LSCompletionException {

        List<LSCompletionItem> completionItems = new ArrayList<>();
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
              covers the following
              function (int,int,..) re<cursor>
            */
            if (node.typeName() instanceof FunctionTypeDescriptorNode && withinParamName(node, context)) {
                Optional<FunctionSignatureNode> functionSignature =
                        ((FunctionTypeDescriptorNode) node.typeName()).functionSignature();
                if (functionSignature.isPresent() && functionSignature.get().returnTypeDesc().isEmpty()) {
                    completionItems.add(new SnippetCompletionItem(context, Snippet.KW_RETURNS.get()));
                }
            } else if (!withinParamName(node, context)) {
                /*
                Covers the Following
                (1) function(T<cursor>)
                */
                completionItems.addAll(this.getTypeDescContextItems(context));
            }
        }
        this.sort(context, node, completionItems);
        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, RequiredParameterNode node,
                     List<LSCompletionItem> completionItems) {
        completionItems.forEach(completionItem -> {
            String sortText = SortingUtil.genSortTextForTypeDescContext(context, completionItem);
            completionItem.getCompletionItem().setSortText(sortText);
        });
    }

    private boolean withinParamName(RequiredParameterNode node, BallerinaCompletionContext context) {
        int cursor = context.getCursorPositionInTree();
        Optional<Token> paramName = node.paramName();
        return paramName.isPresent() && !paramName.get().isMissing()
                && paramName.get().textRange().startOffset() <= cursor
                && cursor <= paramName.get().textRange().endOffset();
    }
}
