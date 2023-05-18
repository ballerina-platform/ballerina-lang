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
import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.NamedArgCompletionItem;
import org.ballerinalang.langserver.completions.builder.NamedArgCompletionItemBuilder;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ErrorConstructorExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ErrorConstructorExpressionNodeContext extends
        AbstractCompletionProvider<ErrorConstructorExpressionNode> {

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
            completionItems.addAll(getCompletionWithinArgs(context, node));
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

    private List<LSCompletionItem> getCompletionWithinArgs(BallerinaCompletionContext ctx,
                                                           ErrorConstructorExpressionNode node) {

        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(ctx, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            return this.getCompletionItemList(QNameRefCompletionUtil.getExpressionContextEntries(ctx, qNameRef), ctx);
        }

        completionItems.addAll(this.expressionCompletions(ctx));
        completionItems.addAll(this.getNamedArgExpressionCompletionItems(ctx, node));
        completionItems.addAll(this.actionKWCompletions(ctx));
        return completionItems;
    }

    private List<LSCompletionItem> getErrorTypeRefCompletions(BallerinaCompletionContext ctx) {

        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(ctx, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> moduleContent = QNameRefCompletionUtil.getModuleContent(ctx, qNameRef,
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

    @Override
    public void sort(BallerinaCompletionContext context, ErrorConstructorExpressionNode node,
                     List<LSCompletionItem> completionItems) {

        if (!withinArgs(context, node) || context.currentSemanticModel().isEmpty()) {
            super.sort(context, node, completionItems);
            return;
        }

        Types types = context.currentSemanticModel().get().types();

        if (isInErrorMessageArgContext(context, node)) {
            /*
            Covers the following.
            error(<cursor>,)
            */
            for (LSCompletionItem completionItem : completionItems) {
                completionItem.getCompletionItem()
                        .setSortText(SortingUtil.genSortTextByAssignability(context, completionItem, types.STRING));
            }
            return;
        }

        if (isInErrorCauseArgContext(context, node) || node.typeReference().isPresent()) {
            /*
            Covers the following.
            error(arg1, <cursor>)
            */
            UnionTypeSymbol optionalErrorTypeSymbol = types.builder().UNION_TYPE
                    .withMemberTypes(types.ERROR, types.NIL).build();
            for (LSCompletionItem completionItem : completionItems) {
                String sortText;
                if (completionItem.getType() == LSCompletionItem.CompletionItemType.NAMED_ARG) {
                    if (isInErrorCauseArgContext(context, node)) {
                        sortText = SortingUtil.genSortText(1)
                                + SortingUtil.genSortText(SortingUtil.toRank(context, completionItem));
                    } else {
                        sortText = SortingUtil.genSortText(1);
                    }
                } else {
                    sortText = SortingUtil.genSortTextByAssignability(context, completionItem, optionalErrorTypeSymbol);
                }
                completionItem.getCompletionItem().setSortText(sortText);
            }
            return;
        }

        super.sort(context, node, completionItems);
    }

    private boolean withinArgs(BallerinaCompletionContext context, ErrorConstructorExpressionNode node) {

        int cursor = context.getCursorPositionInTree();
        Token openParenToken = node.openParenToken();
        Token closeParenToken = node.closeParenToken();

        return !openParenToken.isMissing() && !closeParenToken.isMissing()
                && cursor >= openParenToken.textRange().endOffset()
                && cursor <= closeParenToken.textRange().startOffset();
    }

    private boolean isInErrorMessageArgContext(BallerinaCompletionContext context,
                                               ErrorConstructorExpressionNode node) {

        if (!withinArgs(context, node)) {
            return false;
        }
        if (node.arguments().isEmpty()) {
            return true;
        }
        FunctionArgumentNode firstNode = node.arguments().get(0);
        TextRange textRange = firstNode.textRange();
        int cursor = context.getCursorPositionInTree();
        return cursor < textRange.startOffset() || (firstNode.kind() == SyntaxKind.POSITIONAL_ARG
                && textRange.startOffset() <= cursor && cursor <= textRange.endOffset());
    }

    private boolean isInErrorCauseArgContext(BallerinaCompletionContext context,
                                             ErrorConstructorExpressionNode node) {

        if (!withinArgs(context, node) || node.arguments().isEmpty() || node.arguments().size() == 1) {
            return false;
        }
        FunctionArgumentNode secondArgNode = node.arguments().get(1);
        TextRange textRange = secondArgNode.textRange();
        int cursor = context.getCursorPositionInTree();
        return cursor < textRange.startOffset() || (secondArgNode.kind() == SyntaxKind.POSITIONAL_ARG
                && textRange.startOffset() <= cursor && cursor <= textRange.endOffset());
    }

    private List<LSCompletionItem> getNamedArgExpressionCompletionItems(BallerinaCompletionContext context,
                                                                        ErrorConstructorExpressionNode node) {

        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (semanticModel.isEmpty()) {
            return completionItems;
        }

        Optional<TypeSymbol> detailTypeDesc = Optional.empty();
        if (context.currentSemanticModel().isPresent() && context.currentDocument().isPresent()) {
            LinePosition linePosition = node.location().lineRange().endLine();
            detailTypeDesc = context.currentSemanticModel().get()
                    .expectedType(context.currentDocument().get(), linePosition);
        }

        if (detailTypeDesc.isEmpty()) {
            return completionItems;
        }
        TypeSymbol rawType = CommonUtil.getRawType(detailTypeDesc.get());
        if (rawType.typeKind() != TypeDescKind.RECORD) {
            return completionItems;
        }
        Map<String, RecordFieldSymbol> fields = ((RecordTypeSymbol) rawType).fieldDescriptors();
        if (fields.isEmpty()) {
            return completionItems;
        }
        List<String> existingNamedArgs = node.arguments().stream().filter(arg -> arg.kind() == SyntaxKind.NAMED_ARG)
                .map(arg -> ((NamedArgumentNode) arg).argumentName().name().text()).collect(Collectors.toList());

        fields.entrySet().forEach(field -> {
            Optional<String> fieldName = field.getValue().getName();
            if (fieldName.isEmpty() || fieldName.get().isEmpty() || existingNamedArgs.contains(fieldName.get())) {
                return;
            }
            TypeSymbol fieldType = field.getValue().typeDescriptor();
            CompletionItem completionItem = NamedArgCompletionItemBuilder.build(fieldName.get(), fieldType);
            completionItems.add(new NamedArgCompletionItem(context, completionItem, Either.forRight(field.getValue())));
        });

        return completionItems;
    }
}
