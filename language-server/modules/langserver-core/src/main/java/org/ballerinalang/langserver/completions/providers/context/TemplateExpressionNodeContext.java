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
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ReBracedQuantifierNode;
import io.ballerina.compiler.syntax.tree.ReFlagExpressionNode;
import io.ballerina.compiler.syntax.tree.ReFlagsOnOffNode;
import io.ballerina.compiler.syntax.tree.ReQuantifierNode;
import io.ballerina.compiler.syntax.tree.ReUnicodeGeneralCategoryNode;
import io.ballerina.compiler.syntax.tree.ReUnicodePropertyEscapeNode;
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
import java.util.Arrays;
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

    private final List<String> wordSeparatorArray = Arrays.asList("`", "~", "!", "@", "#", "$", "%", "^", "&", "*",
            "(", ")", "-", "=", "+", "[", "{", "]", "}", "\\", "|", ";", ":", "'", "\"", ",", ".", "<", ">", "/", "?");

    private final List<String> reFlags = Arrays.asList("m", "i", "s", "x");

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
        RegexTemplateNodeFinder nodeFinder = new RegexTemplateNodeFinder();
        nodeAtCursor.accept(nodeFinder);
        String resolvedWord = null;

        if (nodeAtCursor.kind() == SyntaxKind.RE_QUOTE_ESCAPE
                && nodeAtCursor.toSourceCode().equals(SyntaxKind.BACK_SLASH_TOKEN.stringValue())) {
            // Eg: re `\<cursor>`
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_DIGIT.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_NON_DIGIT.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_WHITESPACE.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_NON_WHITESPACE.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_ALPHA_NUMERIC.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_NON_ALPHA_NUMERIC.get()));

            // controls escapes 
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_RETURN.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_NEWLINE.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_TAB.get()));

            // syntax chars
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_ASSERTION_START.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_ASSERTION_END.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_BACKSLASH_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_DOT_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_QUANTIFIER_ASTERISK_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_QUANTIFIER_PLUS_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_QUANTIFIER_QUESTION_MARK_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_LEFT_BRACE_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_RIGHT_BRACE_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_LEFT_SQUARE_BRACE_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_RIGHT_SQUARE_BRACE_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_LEFT_CURLY_BRACE_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_RIGHT_CURLY_BRACE_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_PIPE_CHAR.get()));

            // unicode property chars
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_UNICODE_PROPERTY_CHAR.snippetBlock));
            completionItems.add(
                    new SnippetCompletionItem(ctx, RegexSnippet.DEF_NON_UNICODE_PROPERTY_CHAR.snippetBlock));

        } else if (isFlagExpr(nodeAtCursor, ctx)) {
            // Eg: re `(?<cursor>)`
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_MULTILINE_FLAG.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_DOT_ALL_FLAG.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_IGNORE_CASE_FLAG.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_COMMENT_FLAG.get()));
        } else if (nodeAtCursor.kind() == SyntaxKind.RE_QUOTE_ESCAPE && nodeAtCursor.toSourceCode().equals("\\p")) {
            // Eg: re `\p<cursor>`
            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock("{}", "p{}", "p{${1}}",
                            ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)));
        } else if (nodeAtCursor.kind() == SyntaxKind.RE_QUOTE_ESCAPE && nodeAtCursor.toSourceCode().equals("\\P")) {
            // Eg: re `\P<cursor>`
            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock("{}", "P{}", "P{${1}}",
                            ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)));
        } else if (isUnicodePropertyEscapeNode(nodeAtCursor, ctx)) {
            // Eg: re `\P{<cursor>}`
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexSnippet.DEF_UNICODE_SCRIPT_PROPERTY_KEY.snippetBlock));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_PROPERTY_KEY.snippetBlock));
            addUnicodeGeneralCategoryMajorSnnipets(ctx, completionItems);
        } else if (nodeAtCursor.kind() == SyntaxKind.RE_UNICODE_GENERAL_CATEGORY) {
            ReUnicodeGeneralCategoryNode unicodeGeneralCategoryNode = (ReUnicodeGeneralCategoryNode) nodeAtCursor;
            if (unicodeGeneralCategoryNode.categoryStart().isPresent() &&
                    unicodeGeneralCategoryNode.reUnicodeGeneralCategoryName().isMissing()) {
                // Eg: re `\p{gc=<cursor>}`
                addUnicodeGeneralCategoryMajorSnnipets(ctx, completionItems);
            } else {
                if (unicodeGeneralCategoryNode.reUnicodeGeneralCategoryName().textRange().endOffset()
                        == ctx.getCursorPositionInTree()) {
                    // Eg: `\p{gc=L<cursor>}`, re `\p{gc=M<cursor>}`, re `\p{L<cursor>}`, re `\p{M<cursor>}`
                    addUnicodeGeneralCategorySnippets(unicodeGeneralCategoryNode, ctx, completionItems);
                }
            }
        } else if (isReAtom(nodeAtCursor, ctx)) {
            // Eg: re `a<cursor>`, re `abc<cursor>`, re `[a-z]<cursor>`, re `(abc<cursor>def)`, re `(abc)<cursor>`, 
            // re `\w<cursor>`
            String snippet = resolveWordForTheGivenCursorPosition(ctx, nodeFinder);
            resolvedWord = snippet;

            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock("*", snippet + "*", snippet + "*",
                            ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)));
            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock("+", snippet + "+", snippet + "+",
                            ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)));
            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock("?", snippet + "?", snippet + "?",
                            ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)));
            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock("{}", snippet + "{}", snippet + "{${1}}",
                            ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)));
        } else if (nodeAtCursor.kind() == SyntaxKind.RE_CHARACTER_CLASS
                && nodeAtCursor.textRange().startOffset() + 1 == ctx.getCursorPositionInTree()) {
            // Eg: re `[<cursor>]`
            completionItems.add(new SnippetCompletionItem(ctx, new SnippetBlock("^", "^", "^",
                    ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)));
        } else if (isCommaPresentInReBracedQuantifierNode(nodeAtCursor, ctx)) {
            String snippet = nodeAtCursor.toSourceCode();
            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock(",", snippet + ",", snippet + ",",
                            ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)));
        } else if (nodeAtCursor.kind() == SyntaxKind.RE_QUANTIFIER
                && ((ReQuantifierNode) nodeAtCursor).nonGreedyChar().isEmpty()) {
            completionItems.add(new SnippetCompletionItem(ctx, new SnippetBlock("?", "?", "?",
                    ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)));
        } else if (nodeAtCursor.kind() == SyntaxKind.RE_BRACED_QUANTIFIER
                && nodeAtCursor.textRange().endOffset() == ctx.getCursorPositionInTree()) {
            completionItems.add(new SnippetCompletionItem(ctx, new SnippetBlock("?", "?", "?",
                    ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)));
        } else if (isCursorAfterReFlag(nodeAtCursor, ctx)) {
            // Eg: re `(?i<cursor>)`, re `(?i-m<cursor>)`
            String snippet = nodeAtCursor.toSourceCode().substring(0, 1);
            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock(":", snippet + ":", snippet + ":",
                            ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)));
            if (((ReFlagsOnOffNode) nodeAtCursor.parent().parent()).rhsReFlags().isEmpty()) {
                // Eg: re `(?i<cursor>)`
                completionItems.add(new SnippetCompletionItem(ctx,
                        new SnippetBlock("-", snippet + "-", snippet + "-",
                                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)));
            }
        } else if (isCursorAfterReOnOffFlagMinusPosition(nodeAtCursor, ctx)) {
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_MULTILINE_FLAG.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_DOT_ALL_FLAG.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_IGNORE_CASE_FLAG.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_COMMENT_FLAG.get()));
        } else if (nodeAtCursor.kind() == SyntaxKind.REGEX_TEMPLATE_EXPRESSION) {
            // Eg: re `<cursor>`, re `<cursor>()`, re `<cursor>ABC`
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.DEF_PARANTHESIS.get()));
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.DEF_SQUARE_BRACKET.get()));
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_ASSERTION_START.get()));
        }
        if (nodeFinder.getTemplateExpressionNode() != null
                && nodeFinder.getTemplateExpressionNode().textRange().endOffset() - 1
                == ctx.getCursorPositionInTree()
                && nodeAtCursor.kind() != SyntaxKind.RE_QUOTE_ESCAPE) {
            // Eg: re `abc<cursor>`

            if (resolvedWord == null) {
                resolvedWord = resolveWordForTheGivenCursorPosition(ctx, nodeFinder);
            }
            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock("$", resolvedWord + "$", resolvedWord + "$",
                            ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)));
        }
        return completionItems;
    }

    private String resolveWordForTheGivenCursorPosition(BallerinaCompletionContext ctx,
                                                        RegexTemplateNodeFinder nodeFinder) {
        String symbol = "";
        TemplateExpressionNode templateExpressionNode = nodeFinder.getTemplateExpressionNode();
        if (templateExpressionNode != null) {
            // Eg: re `(abc<cursor>)`
            int startIndex = templateExpressionNode.textRange().startOffset(); // 100
            int currentPosition = ctx.getCursorPositionInTree(); // 108
            int expressionStartIndex = templateExpressionNode.startBacktick().textRange().startOffset()
                    - templateExpressionNode.textRange().startOffset(); // 103 - 100 -> 3
            int subStrLength = currentPosition - startIndex; // 8 -> "re `abc"
            // (abc
            String subStr = templateExpressionNode.toSourceCode().substring(expressionStartIndex + 1, subStrLength);
            int len = subStr.length() - 1; // 3
            String currentChar = subStr.substring(len, len + 1); // c
            boolean isCurrentCharAWordSeparator = wordSeparatorArray.contains(currentChar);
            while (!isCurrentCharAWordSeparator && len != 0) {
                len--;
                currentPosition--;
                currentChar = subStr.substring(len, len + 1);
                isCurrentCharAWordSeparator = wordSeparatorArray.contains(currentChar);
            }
            symbol = subStr.substring(len); // abc
            if (isCurrentCharAWordSeparator) {
                symbol = subStr.substring(len + 1);
            }
        }
        return symbol;
    }

    private boolean isCursorAfterReFlag(NonTerminalNode nodeAtCursor, BallerinaCompletionContext ctx) {
        return nodeAtCursor.parent().kind() == SyntaxKind.RE_FLAGS
                && nodeAtCursor.textRange().startOffset() + 1 == ctx.getCursorPositionInTree()
                && reFlags.contains(nodeAtCursor.toSourceCode().substring(0, 1));
    }

    private boolean isCursorAfterReOnOffFlagMinusPosition(NonTerminalNode nodeAtCursor,
                                                          BallerinaCompletionContext ctx) {
        return nodeAtCursor.kind() == SyntaxKind.RE_FLAGS_ON_OFF &&
                ((ReFlagsOnOffNode) nodeAtCursor).minusToken().isPresent()
                && nodeAtCursor.textRange().startOffset() + 2 <= ctx.getCursorPositionInTree();
    }

    private boolean isCommaPresentInReBracedQuantifierNode(NonTerminalNode nodeAtCursor,
                                                           BallerinaCompletionContext ctx) {
        return nodeAtCursor.parent().kind() == SyntaxKind.RE_BRACED_QUANTIFIER &&
                ((ReBracedQuantifierNode) nodeAtCursor.parent()).commaToken().isEmpty()
                && ((ReBracedQuantifierNode) nodeAtCursor.parent())
                .leastTimesMatchedDigit().size() + 1 <= ctx.getCursorPositionInTree();
    }

    private void addUnicodeGeneralCategoryMajorSnnipets(BallerinaCompletionContext ctx,
                                                        List<LSCompletionItem> completionItems) {
        completionItems.add(new SnippetCompletionItem(ctx,
                RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_LETTER.snippetBlock));
        completionItems.add(new SnippetCompletionItem(ctx,
                RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_MARK.snippetBlock));
        completionItems.add(new SnippetCompletionItem(ctx,
                RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_NUMBER.snippetBlock));
        completionItems.add(new SnippetCompletionItem(ctx,
                RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_SYMBOL.snippetBlock));
        completionItems.add(new SnippetCompletionItem(ctx,
                RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION.snippetBlock));
        completionItems.add(new SnippetCompletionItem(ctx,
                RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR.snippetBlock));
        completionItems.add(new SnippetCompletionItem(ctx,
                RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_OTHER.snippetBlock));
    }

    private void addUnicodeGeneralCategorySnippets(ReUnicodeGeneralCategoryNode unicodeGeneralCategoryNode,
                                                   BallerinaCompletionContext ctx,
                                                   List<LSCompletionItem> completionItems) {
        switch (unicodeGeneralCategoryNode.reUnicodeGeneralCategoryName().toSourceCode()) {
            case "L":
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_LETTER_UPPERCASE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_LETTER_LOWERCASE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_LETTER_TITLECASE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_LETTER_MODIFIER.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_LETTER_OTHER.snippetBlock));
                break;
            case "M":
                completionItems.add(new SnippetCompletionItem(ctx, Snippet.DEF_SQUARE_BRACKET.get()));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_MARK_NON_SPACING.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_MARK_SPACING.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_MARK_NON_ENCLOSING.snippetBlock));
                break;
            case "N":
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_NUMBER_DECIMAL_DIGIT.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_NUMBER_LETTER.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_NUMBER_OTHER.snippetBlock));
                break;
            case "S":
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_MATH.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_CURRENCY.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_MODIFIER.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_OTHER.snippetBlock));
                break;
            case "P":
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_CONNECTOR.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_DASH.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_OPEN.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_CLOSE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_INITIAL_QUOTE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_FINAL_QUOTE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_OTHER.snippetBlock));
                break;
            case "Z":
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR_SPACE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR_LINE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR_PARAGRAPH.snippetBlock));
                break;
            case "C":
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_OTHER_CONTROL.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_OTHER_FORMAT.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_OTHER_PRIVATE_USE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_OTHER_NOT_ASSIGNED.snippetBlock));
                break;
            default:
                break;
        }
    }

    private boolean isReAtom(NonTerminalNode nodeAtCursor, BallerinaCompletionContext ctx) {
        return nodeAtCursor.kind() == SyntaxKind.RE_CHARACTER_CLASS
                && nodeAtCursor.textRange().endOffset() == ctx.getCursorPositionInTree()
                || nodeAtCursor.kind() == SyntaxKind.RE_LITERAL_CHAR_DOT_OR_ESCAPE
                || nodeAtCursor.kind() == SyntaxKind.RE_SIMPLE_CHAR_CLASS_ESCAPE
                || nodeAtCursor.kind() == SyntaxKind.RE_QUOTE_ESCAPE
                || nodeAtCursor.kind() == SyntaxKind.RE_CAPTURING_GROUP;
    }

    private boolean isFlagExpr(NonTerminalNode nodeAtCursor, BallerinaCompletionContext ctx) {
        return nodeAtCursor.kind() == SyntaxKind.RE_FLAG_EXPR
                && ((ReFlagExpressionNode) nodeAtCursor).questionMark().position() + 1 == ctx.getCursorPositionInTree();
    }

    private boolean isUnicodePropertyEscapeNode(NonTerminalNode nodeAtCursor, BallerinaCompletionContext ctx) {
        return nodeAtCursor.kind() == SyntaxKind.RE_UNICODE_PROPERTY_ESCAPE
                && !((ReUnicodePropertyEscapeNode) nodeAtCursor).openBraceToken().isMissing()
                && !((ReUnicodePropertyEscapeNode) nodeAtCursor).closeBraceToken().isMissing()
                && ((ReUnicodePropertyEscapeNode) nodeAtCursor).openBraceToken().textRange().endOffset()
                == ctx.getCursorPositionInTree();
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
                SnippetBlock.Kind.SNIPPET)),

        DEF_ASSERTION_START(new SnippetBlock("^", "^", "^", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_ASSERTION_END(new SnippetBlock("$", "$", "$", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_BACKSLASH_CHAR(new SnippetBlock("\\", "\\", "\\", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_DOT_CHAR(new SnippetBlock(".", ".", ".", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_QUANTIFIER_ASTERISK_CHAR(new SnippetBlock("*", "*", "*", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_QUANTIFIER_PLUS_CHAR(new SnippetBlock("+", "+", "+", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_QUANTIFIER_QUESTION_MARK_CHAR(new SnippetBlock("?", "?", "?", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_LEFT_BRACE_CHAR(new SnippetBlock("(", "(", "(", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_RIGHT_BRACE_CHAR(new SnippetBlock(")", ")", ")", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_LEFT_SQUARE_BRACE_CHAR(new SnippetBlock("[", "[", "[", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_RIGHT_SQUARE_BRACE_CHAR(new SnippetBlock("]", "]", "]", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_LEFT_CURLY_BRACE_CHAR(new SnippetBlock("{", "{", "{", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_RIGHT_CURLY_BRACE_CHAR(new SnippetBlock("}", "}", "}", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_PIPE_CHAR(new SnippetBlock("|", "|", "|", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_PROPERTY_CHAR(new SnippetBlock("p", "p", "p", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_NON_UNICODE_PROPERTY_CHAR(new SnippetBlock("P", "P", "P", ItemResolverConstants.SNIPPET_TYPE,
                SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_SCRIPT_PROPERTY_KEY(new SnippetBlock("sc=", "sc=", "sc=",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_PROPERTY_KEY(new SnippetBlock("gc=", "gc=", "gc=",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_LETTER(new SnippetBlock("L", "L", "L",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_LETTER_UPPERCASE(new SnippetBlock("u", "Lu", "Lu",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_LETTER_LOWERCASE(new SnippetBlock("l", "Ll", "Ll",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_LETTER_TITLECASE(new SnippetBlock("t", "Lt", "Lt",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_LETTER_MODIFIER(new SnippetBlock("m", "Lm", "Lm",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_LETTER_OTHER(new SnippetBlock("o", "Lo", "Lo",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_MARK(new SnippetBlock("M", "M", "M",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_MARK_NON_SPACING(new SnippetBlock("n", "Mn", "Mn",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_MARK_SPACING(new SnippetBlock("c", "Mc", "Mc",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_MARK_NON_ENCLOSING(new SnippetBlock("e", "Me", "Me",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_NUMBER(new SnippetBlock("N", "N", "N",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_NUMBER_DECIMAL_DIGIT(new SnippetBlock("d", "Nd", "Nd",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_NUMBER_LETTER(new SnippetBlock("l", "Nl", "Nl",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_NUMBER_OTHER(new SnippetBlock("o", "No", "No",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_SYMBOL(new SnippetBlock("S", "S", "S",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_MATH(new SnippetBlock("m", "Sm", "Sm",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_CURRENCY(new SnippetBlock("c", "Sc", "Sc",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_MODIFIER(new SnippetBlock("k", "Sk", "Sk",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_OTHER(new SnippetBlock("o", "So", "So",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION(new SnippetBlock("P", "P", "P",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_CONNECTOR(new SnippetBlock("c", "Pc", "Pc",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_DASH(new SnippetBlock("d", "Pd", "Pd",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_OPEN(new SnippetBlock("s", "Ps", "Ps",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_CLOSE(new SnippetBlock("e", "Pe", "Pe",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_INITIAL_QUOTE(new SnippetBlock("i", "Pi", "Pi",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_FINAL_QUOTE(new SnippetBlock("f", "Pf", "Pf",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_OTHER(new SnippetBlock("o", "Po", "Po",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR(new SnippetBlock("Z", "Z", "Z",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR_SPACE(new SnippetBlock("s", "Zs", "Zs",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR_LINE(new SnippetBlock("l", "Zl", "Zl",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR_PARAGRAPH(new SnippetBlock("p", "Zp", "Zp",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_OTHER(new SnippetBlock("C", "C", "C",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_OTHER_CONTROL(new SnippetBlock("c", "Cc", "Cc",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_OTHER_FORMAT(new SnippetBlock("f", "Cf", "Cf",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_OTHER_PRIVATE_USE(new SnippetBlock("o", "Co", "Co",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)),

        DEF_UNICODE_GENERAL_CATEGORY_OTHER_NOT_ASSIGNED(new SnippetBlock("n", "Cn", "Cn",
                ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET));

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

    static class RegexTemplateNodeFinder extends NodeVisitor {

        private TemplateExpressionNode templateExpressionNode = null;

        @Override
        public void visit(TemplateExpressionNode templateExpressionNode) {
            this.templateExpressionNode = templateExpressionNode;
        }

        public TemplateExpressionNode getTemplateExpressionNode() {
            return templateExpressionNode;
        }

        @Override
        protected void visitSyntaxNode(Node node) {
            // Do an early exit if the result is already found.
            if (templateExpressionNode != null) {
                return;
            }
            node.parent().accept(this);
        }
    }
}
