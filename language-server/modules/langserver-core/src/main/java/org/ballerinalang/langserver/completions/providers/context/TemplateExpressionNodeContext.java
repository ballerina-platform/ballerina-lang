/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.InterpolationNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ReFlagExpressionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SnippetBlock;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Completion provider for {@link TemplateExpressionNode}.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class TemplateExpressionNodeContext extends AbstractCompletionProvider<TemplateExpressionNode> {

    public TemplateExpressionNodeContext() {
        super(TemplateExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, TemplateExpressionNode node)
            throws LSCompletionException {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (node.kind() == SyntaxKind.REGEX_TEMPLATE_EXPRESSION) {
            completionItems.addAll(this.getRegexCompletions(nodeAtCursor, context));
        }

        Optional<InterpolationNode> interpolationNode = findInterpolationNode(nodeAtCursor, node);
        if (interpolationNode.isEmpty() || !this.isWithinInterpolation(context, node)) {
            return completionItems;
        }
        
        // If the node at cursor is an interpolation, show expression suggestions
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> moduleContent =
                    QNameRefCompletionUtil.getModuleContent(context, qNameRef, this.symbolFilterPredicate());
            completionItems.addAll(this.getCompletionItemList(moduleContent, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context));
        }
        SyntaxKind interpolationParent = interpolationNode.get().parent().kind();
        this.sort(context, node, completionItems, interpolationParent);

        return completionItems;
    }

    private List<LSCompletionItem> getRegexCompletions(NonTerminalNode nodeAtCursor, BallerinaCompletionContext ctx) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (nodeAtCursor.kind() == SyntaxKind.RE_LITERAL_CHAR_DOT_OR_ESCAPE
                && nodeAtCursor.toSourceCode().equals(SyntaxKind.BACK_SLASH_TOKEN.stringValue())) {
            // Eg: re `\<cursor>`
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_DIGIT.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_NON_DIGIT.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_WHITESPACE.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_NON_WHITESPACE.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_ALPHA_NUMERIC.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_NON_ALPHA_NUMERIC.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_RETURN.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_NEWLINE.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_TAB.get()));
        } else if (isFlagExpr(nodeAtCursor, ctx)) {
            // Eg: re `(?<cursor>)`
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_MULTILINE_FLAG.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_DOT_ALL_FLAG.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_IGNORE_CASE_FLAG.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_COMMENT_FLAG.get()));
        } else {
            // Eg: re `<cursor>`, re `(<cursor>)`
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.DEF_PARANTHESIS.get()));
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.DEF_SQUARE_BRACKET.get()));
        } 
            return completionItems;
    }

    private boolean isFlagExpr(NonTerminalNode nodeAtCursor, BallerinaCompletionContext ctx) {
        return nodeAtCursor.kind() == SyntaxKind.RE_FLAG_EXPR 
                && ((ReFlagExpressionNode) nodeAtCursor).questionMark().position() + 1 == ctx.getCursorPositionInTree();
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, TemplateExpressionNode node) {
        return node.textRange().startOffset() <= context.getCursorPositionInTree() 
                && context.getCursorPositionInTree() <= node.textRange().endOffset();
    }

    /**
     * Finds an {@link InterpolationNode} which is/is a parent of the cursor node.
     *
     * @param cursorNode Node at cursor
     * @param node       Template expression node
     * @return Optional interpolation node
     */
    private Optional<InterpolationNode> findInterpolationNode(NonTerminalNode cursorNode, TemplateExpressionNode node) {
        // We know that the template expression node is definitely a parent of the node at the cursor
        while (cursorNode.kind() != node.kind()) {
            if (cursorNode.kind() == SyntaxKind.INTERPOLATION) {
                return Optional.of((InterpolationNode) cursorNode);
            }

            cursorNode = cursorNode.parent();
        }

        return Optional.empty();
    }

    private boolean isWithinInterpolation(BallerinaCompletionContext context, TemplateExpressionNode node) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        Optional<InterpolationNode> interpolationNode = this.findInterpolationNode(nodeAtCursor, node);
        int cursor = context.getCursorPositionInTree();
        // Check if cursor is within the interpolation start and end tokens. Ex: 
        // 1. `some text ${..<cursor>..} other text`
        if (interpolationNode.isEmpty()) {
            return false;
        }
        Token startToken = interpolationNode.get().interpolationStartToken();
        Token endToken = interpolationNode.get().interpolationEndToken();
        return !startToken.isMissing() && startToken.textRange().endOffset() <= cursor
                && (endToken.isMissing() || cursor <= endToken.textRange().startOffset());
    }

    @Override
    public void sort(BallerinaCompletionContext context, TemplateExpressionNode node,
                     List<LSCompletionItem> completionItems, Object... interpolationParent) {
        if (interpolationParent.length == 0 || !(interpolationParent[0] instanceof SyntaxKind)) {
            throw new RuntimeException("Invalid sorting meta data provided");
        }
        /*
        Sorting order will give the highest priority to the symbols.
        Symbols which has a resolving type of boolean, int, float, decimal and string will get the highest priority.
         */
        for (LSCompletionItem lsCItem : completionItems) {
            String sortText;
            if (lsCItem.getType() != LSCompletionItem.CompletionItemType.SYMBOL
                    || ((SymbolCompletionItem) lsCItem).getSymbol().isEmpty()) {
                sortText = SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem, 1));
            } else {
                Symbol symbol = ((SymbolCompletionItem) lsCItem).getSymbol().get();
                Optional<TypeSymbol> typeSymbol = SymbolUtil.getTypeDescriptor(symbol);
                if (typeSymbol.isEmpty()) {
                    // Added for safety, and should not hit this point
                    sortText = SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem, 1));
                } else {
                    /*
                    Here the sort text is three-fold.
                    First we will assign the highest priority (Symbols over the others such as keywords),
                    then we sort with the resolved type,
                    Then we again append the sorting among the symbols (ex: functions over variable).
                     */
                    sortText = SortingUtil.genSortText(1)
                            + this.getSortTextForResolvedType(typeSymbol.get(), (SyntaxKind) interpolationParent[0])
                            + SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem));
                }
            }

            lsCItem.getCompletionItem().setSortText(sortText);
        }
    }

    private Predicate<Symbol> symbolFilterPredicate() {
        return CommonUtil.getVariableFilterPredicate()
                .or(symbol -> symbol.kind() == SymbolKind.FUNCTION
                        && !symbol.getName().orElse("").equals(Names.ERROR.getValue()));
    }

    private TypeSymbol getResolvedType(TypeSymbol typeSymbol) {
        TypeSymbol resolvedType;
        if (typeSymbol.typeKind() == TypeDescKind.FUNCTION) {
            resolvedType = ((FunctionTypeSymbol) typeSymbol).returnTypeDescriptor().orElse(typeSymbol);
        } else {
            resolvedType = typeSymbol;
        }

        return CommonUtil.getRawType(resolvedType);
    }

    private String getSortTextForResolvedType(TypeSymbol typeSymbol, SyntaxKind interpolationParent) {
        TypeSymbol resolvedType = this.getResolvedType(typeSymbol);
        TypeDescKind typeKind = resolvedType.typeKind();
        
        // Note: The following logic can be simplified. Although, kept it as it is in order to improve the
        // readability and maintainability over the changes 
        switch (interpolationParent) {
            case STRING_TEMPLATE_EXPRESSION:
                if (typeKind == TypeDescKind.BOOLEAN || typeKind == TypeDescKind.INT
                        || typeKind == TypeDescKind.FLOAT || typeKind == TypeDescKind.DECIMAL
                        || typeKind == TypeDescKind.STRING) {
                    return SortingUtil.genSortText(1);
                }
                break;
            case XML_ATTRIBUTE:
                if (typeKind == TypeDescKind.BOOLEAN || typeKind == TypeDescKind.INT
                        || typeKind == TypeDescKind.FLOAT || typeKind == TypeDescKind.DECIMAL) {
                    return SortingUtil.genSortText(1);
                }
                break;
            case XML_ELEMENT:
                if (typeKind == TypeDescKind.XML || typeKind == TypeDescKind.XML_COMMENT
                        || typeKind == TypeDescKind.XML_ELEMENT || typeKind == TypeDescKind.XML_TEXT
                        || typeKind == TypeDescKind.XML_PROCESSING_INSTRUCTION) {
                    return SortingUtil.genSortText(1);
                }
                if (typeKind == TypeDescKind.BOOLEAN || typeKind == TypeDescKind.INT
                        || typeKind == TypeDescKind.FLOAT || typeKind == TypeDescKind.DECIMAL) {
                    return SortingUtil.genSortText(2);
                }
                break;
            default:
                break;
        }
        return SortingUtil.genSortText(3);
    }
    
    private enum RegexSnippet {
        
        DEF_DIGIT(new SnippetBlock("d", "d", "d", ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_NON_DIGIT(new SnippetBlock("D", "D", "D", ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_WHITESPACE(new SnippetBlock("s", "s", "s", ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_NON_WHITESPACE(new SnippetBlock("S", "S", "S", ItemResolverConstants.SNIPPET_TYPE, 
                SnippetBlock.Kind.SNIPPET)),

        DEF_ALPHA_NUMERIC(new SnippetBlock("w", "w", "w", ItemResolverConstants.SNIPPET_TYPE, 
                SnippetBlock.Kind.SNIPPET)),

        DEF_NON_ALPHA_NUMERIC(new SnippetBlock("W", "W", "W", ItemResolverConstants.SNIPPET_TYPE, 
                SnippetBlock.Kind.SNIPPET)),

        DEF_RETURN(new SnippetBlock("r", "r", "r", ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_NEWLINE(new SnippetBlock("n", "n", "n", ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_TAB(new SnippetBlock("t", "t", "t", ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_MULTILINE_FLAG(new SnippetBlock("m", "m", "m", ItemResolverConstants.SNIPPET_TYPE, 
                SnippetBlock.Kind.SNIPPET)),

        DEF_DOT_ALL_FLAG(new SnippetBlock("s", "s", "s", ItemResolverConstants.SNIPPET_TYPE, 
                SnippetBlock.Kind.SNIPPET)),

        DEF_IGNORE_CASE_FLAG(new SnippetBlock("i", "i", "i", ItemResolverConstants.SNIPPET_TYPE, 
                SnippetBlock.Kind.SNIPPET)),

        DEF_COMMENT_FLAG(new SnippetBlock("x", "x", "x", ItemResolverConstants.SNIPPET_TYPE, 
                SnippetBlock.Kind.SNIPPET));

        private final SnippetBlock snippetBlock;

        RegexSnippet(SnippetBlock snippetBlock) {
            this.snippetBlock = snippetBlock;
            this.snippetBlock.setId(name());
        }

        /**
         * Get the SnippetBlock.
         *
         * @return {@link SnippetBlock} SnippetBlock
         */
        public SnippetBlock get() {
            return this.snippetBlock;
        }
    }
}
