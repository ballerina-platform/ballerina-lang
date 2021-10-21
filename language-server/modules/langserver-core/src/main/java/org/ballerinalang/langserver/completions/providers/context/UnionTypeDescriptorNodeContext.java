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
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion Provider for {@link UnionTypeDescriptorNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class UnionTypeDescriptorNodeContext extends AbstractCompletionProvider<UnionTypeDescriptorNode> {

    public UnionTypeDescriptorNodeContext() {
        super(UnionTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, UnionTypeDescriptorNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode refNode = ((QualifiedNameReferenceNode) nodeAtCursor);
            List<Symbol> typesInModule = QNameReferenceUtil.getTypesInModule(context, refNode);
            completionItems.addAll(this.getCompletionItemList(typesInModule, context));
        } else {
            completionItems.addAll(this.getTypeDescContextItems(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, UnionTypeDescriptorNode node,
                     List<LSCompletionItem> completionItems) {
        for (LSCompletionItem lsCItem : completionItems) {
            String sortText = SortingUtil.genSortTextForTypeDescContext(context, lsCItem);
            lsCItem.getCompletionItem().setSortText(sortText);
        }
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, UnionTypeDescriptorNode node) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        Token pipeToken = node.pipeToken();
        int cursor = context.getCursorPositionInTree();
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            /*
            Handle the following. When the cursor is at this position we should not consider the context
            as the union type descriptor context
            1)  mod1:<cursor>
    
                Foo|error r1;
            2) mod1:<cursor> F | int a = 12;
             */
            Node colonToken = ((QualifiedNameReferenceNode) nodeAtCursor).colon();
            MinutiaeList trailingMinutiae = colonToken.trailingMinutiae();
            
            if (!trailingMinutiae.isEmpty() && cursor == colonToken.textRange().endOffset()
                    && cursor < pipeToken.textRange().endOffset()) {
                /*
                Pipe token is considered in order to consider the following. This should be in the union type desc
                eg: 
                (1) int | mod1:<cursor>
                    Test | int x = 12;
                 */
                return false;
            }
        }
        /*
          Validation added for
          function foo() {
             i<cursor>
             int value5 = 12;
          }
          This will recover as <code>i MISSING[|] int value5 = 12;</code>
         */
        return !pipeToken.isMissing() && cursor > pipeToken.textRange().startOffset();
    }
}
