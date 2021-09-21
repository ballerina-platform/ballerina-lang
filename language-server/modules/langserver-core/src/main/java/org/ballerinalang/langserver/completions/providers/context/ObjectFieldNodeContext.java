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

import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link ObjectFieldNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ObjectFieldNodeContext extends AbstractCompletionProvider<ObjectFieldNode> {

    public ObjectFieldNodeContext() {
        super(ObjectFieldNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ObjectFieldNode node)
            throws LSCompletionException {
        
        if (this.onExpressionContext(context, node)) {
            List<LSCompletionItem> completionItems = this.getExpressionContextCompletions(context);
            this.sort(context, node, completionItems);

            return completionItems;
        }
        /*
        If the cursor is at the following position, we route to the parent since it is a common and ideal place.
        Eg:
        (1). object {
                i<cursor>
                table<Country> <cursor>
            }
        (2). public class {
                i<cursor>
                table<Country> <cursor>
            }
         Return from here, since the sorting will be handled by the parent.
         */
        return CompletionUtil.route(context, node.typeName());
    }

    private List<LSCompletionItem> getExpressionContextCompletions(BallerinaCompletionContext ctx) {
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            return this.getCompletionItemList(QNameReferenceUtil.getExpressionContextEntries(ctx, qNameRef), ctx);
        }
        List<LSCompletionItem> completionItems = new ArrayList<>(this.expressionCompletions(ctx));
        Optional<TypeSymbol> contextType = ctx.getContextType();
        Optional<TypeSymbol> rawContextType = contextType.map(CommonUtil::getRawType);
        if (rawContextType.isPresent() && rawContextType.get().kind() == SymbolKind.CLASS) {
            LSCompletionItem implicitNewCompletionItem =
                    this.getImplicitNewCItemForClass((ClassSymbol) rawContextType.get(), ctx);
            completionItems.add(implicitNewCompletionItem);
        }

        return completionItems;
    }

    private boolean onExpressionContext(BallerinaCompletionContext context, ObjectFieldNode node) {
        int cursor = context.getCursorPositionInTree();
        Optional<Token> equalsToken = node.equalsToken();

        return equalsToken.isPresent() && equalsToken.get().textRange().endOffset() <= cursor;
    }

    @Override
    public void sort(BallerinaCompletionContext context, ObjectFieldNode node,
                     List<LSCompletionItem> completionItems) {
        Optional<TypeSymbol> typeSymbolAtCursor = context.getContextType();
        if (typeSymbolAtCursor.isEmpty()) {
            super.sort(context, node, completionItems);
            return;
        }
        TypeSymbol symbol = typeSymbolAtCursor.get();
        for (LSCompletionItem completionItem : completionItems) {
            completionItem.getCompletionItem()
                    .setSortText(SortingUtil.genSortTextByAssignability(context, completionItem, symbol));
        }
    }
}
