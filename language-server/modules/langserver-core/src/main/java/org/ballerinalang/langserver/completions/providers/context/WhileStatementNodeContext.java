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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link WhileStatementNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class WhileStatementNodeContext extends AbstractCompletionProvider<WhileStatementNode> {

    public WhileStatementNodeContext() {
        super(WhileStatementNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, WhileStatementNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
            List<Symbol> symbols = QNameRefCompletionUtil.getExpressionContextEntries(context, qNameRef);
            completionItems.addAll(this.getCompletionItemList(symbols, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, WhileStatementNode node) {
        int cursor = context.getCursorPositionInTree();
        BlockStatementNode whileBody = node.whileBody();
        Token whileKeyword = node.whileKeyword();
        
        /*
        Validates the following
        eg: 1) while <cursor>
            2) while <cursor>{
         */
        return !whileKeyword.isMissing() && cursor >= whileKeyword.textRange().endOffset() + 1
                && ((whileBody.isMissing() || cursor < whileBody.openBraceToken().textRange().endOffset()));
    }

    @Override
    public void sort(BallerinaCompletionContext context, WhileStatementNode node,
                     List<LSCompletionItem> completionItems) {
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (semanticModel.isEmpty()) {
            super.sort(context, node, completionItems);
            return;
        }
        TypeSymbol booleanTypeSymbol = semanticModel.get().types().BOOLEAN;
        for (LSCompletionItem completionItem : completionItems) {
            completionItem.getCompletionItem()
                    .setSortText(SortingUtil.genSortTextByAssignability(context, completionItem, booleanTypeSymbol));
        }
    }
}
