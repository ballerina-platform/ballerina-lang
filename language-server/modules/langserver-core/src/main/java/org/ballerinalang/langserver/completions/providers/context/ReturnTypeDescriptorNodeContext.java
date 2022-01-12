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
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
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
 * Completion provider for {@link ReturnTypeDescriptorNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ReturnTypeDescriptorNodeContext extends AbstractCompletionProvider<ReturnTypeDescriptorNode> {

    public ReturnTypeDescriptorNodeContext() {
        super(ReturnTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ReturnTypeDescriptorNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
            /*
            Covers the following cases.
            (1) function test() returns moduleName:<cursor>
            (2) function test() returns moduleName:F<cursor>
            */
            List<Symbol> typesInModule = QNameReferenceUtil.getTypesInModule(context, qNameRef);
            completionItems.addAll(this.getCompletionItemList(typesInModule, context));
        } else {
            /*
            Covers the following cases.
            (1) function test() returns <cursor>
            (2) function test() returns i<cursor>
            */
            completionItems.addAll(this.getTypeDescContextItems(context));
            completionItems.addAll(this.getTypeQualifierItems(context));
        }
        this.sort(context, node, completionItems);
        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ReturnTypeDescriptorNode node) {
        return !node.returnsKeyword().isMissing();
    }

    @Override
    public void sort(BallerinaCompletionContext context, ReturnTypeDescriptorNode node,
                     List<LSCompletionItem> completionItems) {
        completionItems.forEach(completionItem -> {
            String sortText = SortingUtil.genSortTextForTypeDescContext(context, completionItem);
            completionItem.getCompletionItem().setSortText(sortText);
        });
    }
}
