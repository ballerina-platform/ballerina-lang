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
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.CompleteExpressionValidator;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.context.util.ModulePartNodeContextUtil;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ModuleVariableDeclarationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ModuleVariableDeclarationNodeContext extends
        NodeWithRHSInitializerProvider<ModuleVariableDeclarationNode> {

    public ModuleVariableDeclarationNodeContext() {
        super(ModuleVariableDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext ctx, ModuleVariableDeclarationNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        ResolvedContext resolvedContext;
        if (node.initializer().isPresent() && this.withinInitializerContext(ctx, node)) {
            completionItems.addAll(this.initializerContextCompletions(ctx, node.initializer().get()));
            resolvedContext = ResolvedContext.INITIALIZER;
        } else if (this.onServiceTypeDescriptorContext(ctx, node)) {
            /*
            Covers the following cases
            Eg
            (1) service m<cursor>
            (2) isolated service m<cursor>
            (3) service <cursor>
            (4) isolated service <cursor>
             */
            if (QNameRefCompletionUtil.onQualifiedNameIdentifier(ctx, ctx.getNodeAtCursor())) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) ctx.getNodeAtCursor();
                List<Symbol> moduleContent = QNameRefCompletionUtil.getModuleContent(ctx, qNameRef,
                        ModulePartNodeContextUtil.serviceTypeDescPredicate());
                completionItems.addAll(this.getCompletionItemList(moduleContent, ctx));
            } else {
                List<Symbol> objectSymbols = ModulePartNodeContextUtil.serviceTypeDescContextSymbols(ctx);
                completionItems.addAll(this.getCompletionItemList(objectSymbols, ctx));
                completionItems.addAll(this.getModuleCompletionItems(ctx));
                completionItems.add(new SnippetCompletionItem(ctx, Snippet.KW_ON.get()));
                completionItems.addAll(this.getCompletionItemsOnQualifiers(node, ctx));
            }
            resolvedContext = ResolvedContext.SERVICE_TYPEDESC;
        } else if (withinServiceOnKeywordContext(ctx, node)) {
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.KW_ON.get()));
            resolvedContext = ResolvedContext.SERVICE_TYPEDESC;
        } else if (onSuggestionsAfterQualifiers(ctx, node) &&
                !QNameRefCompletionUtil.onQualifiedNameIdentifier(ctx, ctx.getNodeAtCursor())) {
            /*
                Covers the following.
                (1) <qualifier> <cursor>
                currently the qualifier can be isolated/transactional/client/configurable.
                (2) <qualifier> x<cursor>
                currently the qualifier can be isolated/transactional/client/configurable.
            */
            completionItems.addAll(this.getCompletionItemsOnQualifiers(node, ctx));
            resolvedContext = ResolvedContext.ON_QUALIFIER;
        } else {
            // Type descriptor completions are suggested via the ModulePartNodeContext.
            return CompletionUtil.route(ctx, node.parent());
        }
        this.sort(ctx, node, completionItems, resolvedContext);
        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, ModuleVariableDeclarationNode node,
                     List<LSCompletionItem> completionItems, Object... metaData) {
        ResolvedContext resolvedContext = (ResolvedContext) metaData[0];

        boolean onQualifiedNameIdentifier = QNameRefCompletionUtil
                .onQualifiedNameIdentifier(context, context.getNodeAtCursor());
        boolean hasConfigurableQualifier = hasConfigurableQualifier(context, node);
        if (resolvedContext != ResolvedContext.ON_QUALIFIER && onQualifiedNameIdentifier && hasConfigurableQualifier) {
            resolvedContext = ResolvedContext.ON_QUALIFIER;
        }

        if (resolvedContext == ResolvedContext.INITIALIZER) {
            // Calls the NodeWithRHSInitializerProvider's sorting logic to 
            // make it consistent throughout the implementation
            super.sort(context, node, completionItems);
            return;
        }

        if (resolvedContext == ResolvedContext.ON_QUALIFIER) {
            if (hasConfigurableQualifier) {
                SortingUtil.sortCompletionsAfterConfigurableQualifier(
                        context, completionItems, onQualifiedNameIdentifier);
                return;
            }
            SortingUtil.toDefaultSorting(context, completionItems);
            return;
        }

        // Captures the ResolvedContext.SERVICE_TYPEDESC
        for (LSCompletionItem lsCItem : completionItems) {
            String sortingText;
            if (lsCItem.getType() != LSCompletionItem.CompletionItemType.SNIPPET) {
                sortingText = SortingUtil.genSortText(1);
            } else {
                sortingText = SortingUtil.genSortText(2)
                        + SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem));
            }
            lsCItem.getCompletionItem().setSortText(sortingText);
        }
    }

    @Override
    protected List<LSCompletionItem> getCompletionItemsOnQualifiers(Node node, BallerinaCompletionContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>(super.getCompletionItemsOnQualifiers(node, context));
        Optional<Token> lastQualifier = CommonUtil.getLastQualifier(context, node);
        if (lastQualifier.isEmpty()) {
            return completionItems;
        }
        Set<SyntaxKind> qualKinds = CommonUtil.getQualifiersOfNode(context, node)
                .stream().map(Node::kind).collect(Collectors.toSet());
        switch (lastQualifier.get().kind()) {
            case PUBLIC_KEYWORD:
                completionItems.addAll(getTypeDescContextItems(context));
                List<Snippet> snippets = Arrays.asList(
                        Snippet.KW_TYPE, Snippet.KW_ISOLATED,
                        Snippet.KW_FINAL, Snippet.KW_CONST, Snippet.KW_LISTENER, Snippet.KW_CLIENT,
                        Snippet.KW_VAR, Snippet.KW_ENUM, Snippet.KW_XMLNS, Snippet.KW_CLASS,
                        Snippet.KW_TRANSACTIONAL, Snippet.DEF_FUNCTION, Snippet.DEF_EXPRESSION_BODIED_FUNCTION,
                        Snippet.DEF_MAIN_FUNCTION, Snippet.KW_CONFIGURABLE, Snippet.DEF_ANNOTATION,
                        Snippet.DEF_RECORD, Snippet.STMT_NAMESPACE_DECLARATION,
                        Snippet.DEF_OBJECT_SNIPPET, Snippet.DEF_CLASS, Snippet.DEF_ENUM, Snippet.DEF_CLOSED_RECORD,
                        Snippet.DEF_ERROR_TYPE, Snippet.DEF_TABLE_TYPE_DESC, Snippet.DEF_TABLE_WITH_KEY_TYPE_DESC,
                        Snippet.DEF_STREAM, Snippet.DEF_SERVICE_COMMON
                );
                snippets.forEach(snippet -> completionItems.add(new SnippetCompletionItem(context, snippet.get())));
                return completionItems;
            case SERVICE_KEYWORD:
            case CLIENT_KEYWORD:
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CLASS.get()));
                completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_CLASS.get()));
                break;
            case ISOLATED_KEYWORD:
                if (qualKinds.contains(SyntaxKind.TRANSACTIONAL_KEYWORD)) {
                    completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION.get()));
                    completionItems.add(new SnippetCompletionItem(context, 
                            Snippet.DEF_EXPRESSION_BODIED_FUNCTION.get()));
                    break;
                }
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CLASS.get()));
                completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_CLASS.get()));
                if (!qualKinds.contains(SyntaxKind.SERVICE_KEYWORD) &&
                        !qualKinds.contains(SyntaxKind.CLIENT_KEYWORD)) {
                    /*
                        Covers the following.
                        isolated <cursor>
                     */
                    completionItems.addAll(this.getTypeDescContextItems(context));
                }
                break;
            case TRANSACTIONAL_KEYWORD:
                completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION.get()));
                completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_EXPRESSION_BODIED_FUNCTION.get()));
                break;
            case CONFIGURABLE_KEYWORD:
                completionItems.addAll(this.getTypeDescContextItems(context));
                break;
            default:
        }
        return completionItems;
    }

    private boolean withinServiceOnKeywordContext(BallerinaCompletionContext ctx,
                                                  ModuleVariableDeclarationNode node) {
        List<String> leadingInvalidTokens = node.leadingInvalidTokens().stream()
                .map(Token::text)
                .collect(Collectors.toList());
        boolean onServiceContext = leadingInvalidTokens.contains(SyntaxKind.SERVICE_KEYWORD.stringValue());
        CompleteExpressionValidator expressionValidator = new CompleteExpressionValidator();
        int cursor = ctx.getCursorPositionInTree();
        TypeDescriptorNode typeDesc = node.typedBindingPattern().typeDescriptor();
        boolean completeTypeDesc = typeDesc.apply(expressionValidator);

        return onServiceContext && completeTypeDesc && cursor > typeDesc.textRange().endOffset();
    }

    private boolean onServiceTypeDescriptorContext(BallerinaCompletionContext ctx, ModuleVariableDeclarationNode node) {
        List<String> leadingInvalidTokens = node.leadingInvalidTokens().stream()
                .map(Token::text)
                .collect(Collectors.toList());
        boolean onServiceContext = leadingInvalidTokens.contains(SyntaxKind.SERVICE_KEYWORD.stringValue());
        CompleteExpressionValidator expressionValidator = new CompleteExpressionValidator();
        int cursor = ctx.getCursorPositionInTree();
        TypeDescriptorNode typeDesc = node.typedBindingPattern().typeDescriptor();
        boolean completeTypeDesc = typeDesc.apply(expressionValidator);

        return onServiceContext && (!completeTypeDesc || cursor <= typeDesc.textRange().endOffset());
    }

    private boolean withinInitializerContext(BallerinaCompletionContext context, ModuleVariableDeclarationNode node) {
        if (node.equalsToken().isEmpty()) {
            return false;
        }
        int textPosition = context.getCursorPositionInTree();
        int equalTokenEndOffset = node.equalsToken().get().textRange().endOffset();
        int semicolonTokenStartOffset = node.semicolonToken().textRange().startOffset();
        return equalTokenEndOffset <= textPosition && textPosition <= semicolonTokenStartOffset;
    }

    private boolean hasConfigurableQualifier(BallerinaCompletionContext context, ModuleVariableDeclarationNode node) {
        Optional<Token> lastQualifier = CommonUtil.getLastQualifier(context, node);
        return lastQualifier.isPresent() && lastQualifier.get().kind().equals(SyntaxKind.CONFIGURABLE_KEYWORD);
    }

    enum ResolvedContext {
        INITIALIZER,
        SERVICE_TYPEDESC,
        ON_QUALIFIER
    }
}
