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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Completion Provider for {@link TypeDefinitionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class TypeDefinitionNodeContext extends AbstractCompletionProvider<TypeDefinitionNode> {
    public TypeDefinitionNodeContext() {
        super(TypeDefinitionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, TypeDefinitionNode node)
            throws LSCompletionException {
        if (this.onTypeNameContext(context, node)) {
            return new ArrayList<>();
        }
        List<LSCompletionItem> completionItems = typeDescriptorCItems(context, node);
        this.sort(context, node, completionItems);
        
        return completionItems;
    }

    private List<LSCompletionItem> typeDescriptorCItems(BallerinaCompletionContext context, TypeDefinitionNode node) {
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            QualifiedNameReferenceNode nameRef
                    = (QualifiedNameReferenceNode) context.getNodeAtCursor();
            return this.getCompletionItemList(QNameReferenceUtil.getTypesInModule(context, nameRef), context);
        }
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (onSuggestionsAfterQualifiers(context, node.typeDescriptor())) {
            /*
             * Covers the following
             * type T <qualifier(s)> x<cursor>
             * Currently the qualifier can be isolated/transactional.
             */
            completionItems.addAll(getCompletionItemsOnQualifiers(node.typeDescriptor(), context));
            return completionItems;
        } else {
            completionItems.addAll(this.getTypeDescContextItems(context));
            completionItems.addAll(this.getTypeQualifierItems(context));
        }
        return completionItems;
    }

    private List<LSCompletionItem> getTypeQualifierItems(BallerinaCompletionContext context) {
        // Note: here we do not add the service type qualifier since it is being added via getTypeItems call.
        return Arrays.asList(
                new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()),
                new SnippetCompletionItem(context, Snippet.KW_CLIENT.get()),
                new SnippetCompletionItem(context, Snippet.KW_TRANSACTIONAL.get()));
    }

    private boolean onTypeNameContext(BallerinaCompletionContext context, TypeDefinitionNode node) {
        int cursor = context.getCursorPositionInTree();
        Token typeKeyword = node.typeKeyword();
        Token typeName = node.typeName();
        Node descriptor = node.typeDescriptor();

        if (typeKeyword.isMissing()) {
            return false;
        }

        return cursor > typeKeyword.textRange().endOffset()
                && ((typeName.isMissing() && (descriptor.isMissing()
                || cursor < descriptor.textRange().startOffset()))
                || cursor <= typeName.textRange().endOffset());
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, TypeDefinitionNode node) {
        TextRange typeKWRange = node.typeKeyword().textRange();
        int cursorPosition = context.getCursorPositionInTree();

        return typeKWRange.endOffset() < cursorPosition;
    }

    @Override
    public void sort(BallerinaCompletionContext context, TypeDefinitionNode node, List<LSCompletionItem> lsCItems) {
        for (LSCompletionItem lsCItem : lsCItems) {
            CompletionItem completionItem = lsCItem.getCompletionItem();
            completionItem.setSortText(SortingUtil.genSortTextForTypeDescContext(context, lsCItem));
        }
    }
}
