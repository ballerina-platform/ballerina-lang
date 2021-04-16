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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ErrorConstructorExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ErrorConstructorExpressionNodeContext extends AbstractCompletionProvider<ErrorConstructorExpressionNode> {

    public ErrorConstructorExpressionNodeContext() {
        super(ErrorConstructorExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context,
                                                 ErrorConstructorExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (withinArgs(context, node)) {
            /*
            Covers the following
            eg: 1. error(<cursor>)
                2. error ErrorType(<cursor>)
             */
            completionItems.addAll(getCompletionWithinArgs(context));
        } else if (withinTypeRefContext(context, node)) {
            /*
            Covers the following
            eg: 1. error <cursor>
                2. error E<cursor>
                3. error mod:<cursor>
                4. error E<cursor>rror();
             */
            completionItems.addAll(this.getErrorTypeRefCompletions(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private boolean withinTypeRefContext(BallerinaCompletionContext context, ErrorConstructorExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        Token errorKeyword = node.errorKeyword();
        Optional<TypeDescriptorNode> typeDescNode = node.typeReference();

        return cursor >= errorKeyword.textRange().endOffset() + 1
                && (typeDescNode.isEmpty() || cursor <= typeDescNode.get().textRange().endOffset());
    }

    private List<LSCompletionItem> getCompletionWithinArgs(BallerinaCompletionContext ctx) {
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        if (this.onQualifiedNameIdentifier(ctx, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            return this.getCompletionItemList(QNameReferenceUtil.getExpressionContextEntries(ctx, qNameRef), ctx);
        }

        return this.expressionCompletions(ctx);
    }

    private List<LSCompletionItem> getErrorTypeRefCompletions(BallerinaCompletionContext ctx) {
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        if (this.onQualifiedNameIdentifier(ctx, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(ctx, qNameRef,
                    SymbolUtil.isOfType(TypeDescKind.ERROR));

            return this.getCompletionItemList(moduleContent, ctx);
        }

        List<Symbol> errorTypes = ctx.visibleSymbols(ctx.getCursorPosition()).stream()
                .filter(SymbolUtil.isOfType(TypeDescKind.ERROR)
                        .and(symbol -> !symbol.getName().orElse("").equals(Names.ERROR.getValue())))
                .collect(Collectors.toList());
        List<LSCompletionItem> completionItems = this.getCompletionItemList(errorTypes, ctx);
        completionItems.addAll(this.getModuleCompletionItems(ctx));

        return completionItems;
    }

    private boolean withinArgs(BallerinaCompletionContext context, ErrorConstructorExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        Token openParenToken = node.openParenToken();
        Token closeParenToken = node.closeParenToken();

        return !openParenToken.isMissing() && !closeParenToken.isMissing()
                && cursor >= openParenToken.textRange().endOffset()
                && cursor <= closeParenToken.textRange().startOffset();
    }
}
