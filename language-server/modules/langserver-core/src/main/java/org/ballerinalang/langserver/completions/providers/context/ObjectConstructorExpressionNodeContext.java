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
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ObjectConstructorExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ObjectConstructorExpressionNodeContext
        extends ObjectBodiedNodeContextProvider<ObjectConstructorExpressionNode> {

    public ObjectConstructorExpressionNodeContext() {
        super(ObjectConstructorExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext ctx, ObjectConstructorExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        ResolvedContext resolvedContext;
        if (this.onSuggestObjectOnly(ctx, node)) {
            completionItems.addAll(Arrays.asList(
                    new SnippetCompletionItem(ctx, Snippet.KW_OBJECT.get()),
                    new SnippetCompletionItem(ctx, Snippet.EXPR_OBJECT_CONSTRUCTOR.get())
            ));
            resolvedContext = ResolvedContext.OTHER;
        } else if (this.onSuggestTypeReferences(ctx, node)) {
            completionItems.addAll(this.getTypeReferenceCompletions(ctx));
            resolvedContext = ResolvedContext.TYPE_REF;
        } else {
            completionItems.addAll(this.getBodyContextItems(ctx, node));
            resolvedContext = ResolvedContext.OBJECT_CONSTRUCTOR_BODY;
        }
        this.sort(ctx, node, completionItems, resolvedContext);

        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, ObjectConstructorExpressionNode node,
                     List<LSCompletionItem> completionItems, Object... metaData) {
        if (metaData == null || metaData.length != 1) {
            // Added for safety. Should not come to this point
            super.sort(context, node, completionItems, metaData);
            return;
        }
        switch ((ResolvedContext) metaData[0]) {
            case TYPE_REF:
                for (LSCompletionItem lsCItem : completionItems) {
                    String sortText = SortingUtil.genSortTextForTypeDescContext(context, lsCItem);
                    lsCItem.getCompletionItem().setSortText(sortText);
                }
                break;
            case OBJECT_CONSTRUCTOR_BODY:
            case OTHER:
                super.sort(context, node, completionItems);
        }
    }

    private List<LSCompletionItem> getTypeReferenceCompletions(BallerinaCompletionContext ctx) {
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        Predicate<Symbol> predicate = symbol -> symbol.kind() == SymbolKind.CLASS
                || (symbol.kind() == SymbolKind.TYPE_DEFINITION
                && CommonUtil.getRawType(((TypeDefinitionSymbol) symbol)
                .typeDescriptor()).typeKind() == TypeDescKind.OBJECT);
        
        if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            return this.getCompletionItemList(QNameRefCompletionUtil.getModuleContent(ctx, qNameRef, predicate), ctx);
        }
        List<Symbol> visibleSymbols = ctx.visibleSymbols(ctx.getCursorPosition());
        List<Symbol> objectEntries = visibleSymbols.stream()
                .filter(predicate)
                .collect(Collectors.toList());

        List<LSCompletionItem> completionItems = this.getCompletionItemList(objectEntries, ctx);
        completionItems.addAll(this.getModuleCompletionItems(ctx));

        return completionItems;
    }

    private boolean onSuggestObjectOnly(BallerinaCompletionContext context, ObjectConstructorExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        NodeList<Token> qualifiers = node.objectTypeQualifiers();

        if (qualifiers.isEmpty()) {
            return false;
        }
        Token objectKeyword = node.objectKeyword();
        Token lastQualifier = qualifiers.get(qualifiers.size() - 1);

        return cursor > lastQualifier.textRange().endOffset()
                && (objectKeyword.isMissing() || cursor < objectKeyword.textRange().startOffset());
    }

    private boolean onSuggestTypeReferences(BallerinaCompletionContext context, ObjectConstructorExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        Token objectKeyword = node.objectKeyword();
        Token openBrace = node.openBraceToken();

        return !objectKeyword.isMissing() && cursor > objectKeyword.textRange().startOffset()
                && (openBrace.isMissing() || cursor < openBrace.textRange().endOffset());
    }
    
    private enum ResolvedContext {
        TYPE_REF,
        OBJECT_CONSTRUCTOR_BODY,
        OTHER
    }
}
