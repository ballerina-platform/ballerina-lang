/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
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
 * Completion provider for the {@link ResourcePathParameterNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ResourcePathParameterNodeContext extends AbstractCompletionProvider<ResourcePathParameterNode> {
    public ResourcePathParameterNodeContext() {
        super(ResourcePathParameterNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ResourcePathParameterNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (!this.onTypeDescContext(context, node)) {
            return completionItems;
        }
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> typesInModule = QNameRefCompletionUtil.getTypesInModule(context, qNameRef);
            completionItems.addAll(this.getCompletionItemList(typesInModule, context));
        } else {
            completionItems.addAll(this.getTypeDescContextItems(context));
        }
        // Sorting will be adhered to the type descriptor context. Any changes to include other completion items 
        // rather than type descriptor completion items, will have to be done along with the sorting changes
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, ResourcePathParameterNode node,
                     List<LSCompletionItem> completionItems) {
        for (LSCompletionItem lsCItem : completionItems) {
            String sortText = SortingUtil.genSortTextForTypeDescContext(context, lsCItem);
            lsCItem.getCompletionItem().setSortText(sortText);
        }
    }

    private boolean onTypeDescContext(BallerinaCompletionContext context, ResourcePathParameterNode node) {
        int cursor = context.getCursorPositionInTree();
        TypeDescriptorNode typeDescriptor = node.typeDescriptor();
        /*
        Following case will also be avoided with this check
        [int.<cursor> a]
         */
        return typeDescriptor.isMissing() || cursor <= typeDescriptor.textRange().endOffset();
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ResourcePathParameterNode node) {
        int cursor = context.getCursorPositionInTree();
        Token openBracketToken = node.openBracketToken();
        Token closeBracketToken = node.closeBracketToken();

        return !openBracketToken.isMissing() && !closeBracketToken.isMissing()
                && cursor > openBracketToken.textRange().startOffset()
                && cursor < closeBracketToken.textRange().endOffset();
    }
}
