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

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link TypeCastExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class TypeCastExpressionNodeContext extends AbstractCompletionProvider<TypeCastExpressionNode> {
    public TypeCastExpressionNodeContext() {
        super(TypeCastExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, TypeCastExpressionNode node)
            throws LSCompletionException {
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);
        if (this.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            return this.getCompletionItemList(QNameReferenceUtil.getTypesInModule(context, qNameRef), context);
        }
        List<LSCompletionItem> completionItems = new ArrayList<>(this.getModuleCompletionItems(context));
        completionItems.addAll(this.getTypeItems(context));

        return completionItems;
    }

    @Override
    public boolean onPreValidation(LSContext context, TypeCastExpressionNode node) {
        /*
        Resolves the completions within the type cast expression only if the cursor is within the <>. Otherwise will be
        handled by the parent context. Because the expressions are to be suggested at the expression context.
         */
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        int gtTokenEnd = node.gtToken().textRange().endOffset();

        return cursor <= gtTokenEnd;
    }
}
