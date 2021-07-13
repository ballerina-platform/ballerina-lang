/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link ImplicitNewExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ImplicitNewExpressionNodeContext extends AbstractCompletionProvider<ImplicitNewExpressionNode> {

    public ImplicitNewExpressionNodeContext() {
        super(ImplicitNewExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ImplicitNewExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (this.withinArgs(context, node)) {
            /*
            Covers
            lhs = new(<cursor>)
             */
            completionItems.addAll(this.getCompletionsWithinArgs(context));
        } else {
            /*
            Supports the following
            (1) lhs = new <cursor>
            */
            Optional<ClassSymbol> classSymbol = getClassSymbol(context);
            completionItems.addAll(this.getModuleCompletionItems(context));
            classSymbol.ifPresent(symbol -> completionItems.add(this.getExplicitNewCompletionItem(symbol, context)));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private Optional<ClassSymbol> getClassSymbol(BallerinaCompletionContext context) {
        Optional<TypeSymbol> contextType = context.getContextType();
        if (contextType.isEmpty()) {
            return Optional.empty();
        }
        TypeSymbol rawType = CommonUtil.getRawType(contextType.get());
        if (rawType.kind() == SymbolKind.CLASS) {
            return Optional.of((ClassSymbol) rawType);
        }

        return Optional.empty();
    }

    private boolean withinArgs(BallerinaCompletionContext context, ImplicitNewExpressionNode node) {
        if (node.parenthesizedArgList().isEmpty()) {
            return false;
        }
        ParenthesizedArgList parenthesizedArgList = node.parenthesizedArgList().get();
        int cursor = context.getCursorPositionInTree();

        return cursor > parenthesizedArgList.openParenToken().textRange().startOffset()
                && cursor < parenthesizedArgList.closeParenToken().textRange().endOffset();
    }

    private List<LSCompletionItem> getCompletionsWithinArgs(BallerinaCompletionContext ctx) {
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        if (QNameReferenceUtil.onQualifiedNameIdentifier(ctx, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            return this.getCompletionItemList(QNameReferenceUtil.getExpressionContextEntries(ctx, qNameRef), ctx);
        }

        return this.expressionCompletions(ctx);
    }
}
