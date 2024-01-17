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
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TupleTypeDescriptorNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion Provider for {@link TupleTypeDescriptorNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class TupleTypeDescriptorNodeContext extends AbstractCompletionProvider<TupleTypeDescriptorNode> {
    public TupleTypeDescriptorNodeContext() {
        super(TupleTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, TupleTypeDescriptorNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            List<Symbol> typesInModule = QNameRefCompletionUtil.getTypesInModule(context,
                    (QualifiedNameReferenceNode) nodeAtCursor);
            completionItems.addAll(this.getCompletionItemList(typesInModule, context));
        } else {
            completionItems.addAll(this.getTypeDescContextItems(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context,
                     TupleTypeDescriptorNode node,
                     List<LSCompletionItem> completionItems) {
        for (LSCompletionItem lsCItem : completionItems) {
            String sortText = SortingUtil.genSortTextForTypeDescContext(context, lsCItem);
            lsCItem.getCompletionItem().setSortText(sortText);
        }
    }
}
