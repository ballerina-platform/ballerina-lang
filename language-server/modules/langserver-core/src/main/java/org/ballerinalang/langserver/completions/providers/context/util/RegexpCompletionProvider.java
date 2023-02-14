package org.ballerinalang.langserver.completions.providers.context.util;

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ReBracedQuantifierNode;
import io.ballerina.compiler.syntax.tree.ReFlagExpressionNode;
import io.ballerina.compiler.syntax.tree.ReFlagsOnOffNode;
import io.ballerina.compiler.syntax.tree.ReQuantifierNode;
import io.ballerina.compiler.syntax.tree.ReUnicodeGeneralCategoryNode;
import io.ballerina.compiler.syntax.tree.ReUnicodePropertyEscapeNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.context.TemplateExpressionNodeContext.RegexTemplateNodeFinder;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SnippetBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Util to generate completions for regexp.
 *
 * @since 2201.5.0
 */
public class RegexpCompletionProvider {

    private static final List<String> wordSeparatorArray = Arrays.asList("`", "~", "!", "@", "#", "$", "%", "^", "&",
            "*", "(", ")", "-", "=", "+", "[", "{", "]", "}", "\\", "|", ";", ":", "'", "\"", ",", ".", "<", ">", "/",
            "?");

    private static final List<String> reFlags = Arrays.asList("m", "i", "s", "x");

    public static List<LSCompletionItem> getRegexCompletions(NonTerminalNode nodeAtCursor,
                                                             BallerinaCompletionContext ctx,
                                                             RegexTemplateNodeFinder nodeFinder) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (nodeAtCursor.kind() == SyntaxKind.LIST) {
            nodeAtCursor.parent().accept(nodeFinder);
        } else {
            nodeAtCursor.accept(nodeFinder);
        }
        String resolvedWord = null;

        if (nodeAtCursor.kind() == SyntaxKind.RE_QUOTE_ESCAPE
                && nodeAtCursor.toSourceCode().equals(SyntaxKind.BACK_SLASH_TOKEN.stringValue())) {
            // Eg: re `\<cursor>`
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_DIGIT.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_NON_DIGIT.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_WHITESPACE.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_NON_WHITESPACE.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_ALPHA_NUMERIC.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_NON_ALPHA_NUMERIC.get()));

            // controls escapes 
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_RETURN.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_NEWLINE.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_TAB.get()));

            // syntax chars
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_ASSERTION_START.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_ASSERTION_END.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_BACKSLASH_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_DOT_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_QUANTIFIER_ASTERISK_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_QUANTIFIER_PLUS_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_QUANTIFIER_QUESTION_MARK_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_LEFT_BRACE_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_RIGHT_BRACE_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_LEFT_SQUARE_BRACE_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_RIGHT_SQUARE_BRACE_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_LEFT_CURLY_BRACE_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_RIGHT_CURLY_BRACE_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_PIPE_CHAR.get()));

            // unicode property chars
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_UNICODE_PROPERTY_CHAR.snippetBlock));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_NON_UNICODE_PROPERTY_CHAR.snippetBlock));

        } else if (isFlagExpr(nodeAtCursor, ctx)) {
            // Eg: re `(?<cursor>)`
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_MULTILINE_FLAG.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_DOT_ALL_FLAG.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_IGNORE_CASE_FLAG.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_COMMENT_FLAG.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexSnippet.DEF_COLON_CHAR.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexSnippet.DEF_MINUS_CHAR.get()));
            return completionItems;
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
                    RegexpCompletionProvider.RegexSnippet.DEF_UNICODE_SCRIPT_PROPERTY_KEY.snippetBlock));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_PROPERTY_KEY.snippetBlock));
            addUnicodeGeneralCategoryMajorSnnipets(ctx, completionItems);
            return completionItems;
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
            return completionItems;
        } else if (isReAtom(nodeAtCursor, ctx)) {
            // Eg: re `a<cursor>`, re `abc<cursor>`, re `[a-z]<cursor>`, re `(abc<cursor>def)`, re `(abc)<cursor>`, 
            // re `\w<cursor>`
            String snippet = resolveWordForTheGivenCursorPosition(ctx, nodeFinder);
            resolvedWord = snippet;

            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock("*", snippet + "*", snippet + "*",
                            ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)));
            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock("+", snippet + "+", snippet + "+",
                            ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)));
            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock("?", snippet + "?", snippet + "?",
                            ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)));
            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock("{}", snippet + "{}", snippet + "{${1}}",
                            ItemResolverConstants.SNIPPET_TYPE, SnippetBlock.Kind.SNIPPET)));
        } else if (nodeAtCursor.kind() == SyntaxKind.RE_CHARACTER_CLASS
                && nodeAtCursor.textRange().startOffset() + 1 == ctx.getCursorPositionInTree()) {
            // Eg: re `[<cursor>]`
            completionItems.add(new SnippetCompletionItem(ctx, new SnippetBlock("^", "^", "^",
                    ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)));
            return completionItems;
        } else if (isCommaPresentInReBracedQuantifierNode(nodeAtCursor, ctx)) {
            String snippet = nodeAtCursor.toSourceCode();
            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock(",", snippet + ",", snippet + ",",
                            ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)));
        } else if (nodeAtCursor.kind() == SyntaxKind.RE_QUANTIFIER
                && ((ReQuantifierNode) nodeAtCursor).nonGreedyChar().isEmpty()) {
            completionItems.add(new SnippetCompletionItem(ctx, new SnippetBlock("?", "?", "?",
                    ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)));
        } else if (nodeAtCursor.kind() == SyntaxKind.RE_BRACED_QUANTIFIER
                && nodeAtCursor.textRange().endOffset() == ctx.getCursorPositionInTree()) {
            completionItems.add(new SnippetCompletionItem(ctx, new SnippetBlock("?", "?", "?",
                    ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)));
        } else if (isCursorAfterReFlag(nodeAtCursor)) {
            // Eg: re `(?i<cursor>)`, re `(?i-m<cursor>)`
            String snippet = resolveWordForTheGivenCursorPosition(ctx, nodeFinder);
            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock(":", snippet + ":", snippet + ":",
                            ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)));
            if (((ReFlagsOnOffNode) nodeAtCursor.parent().parent()).rhsReFlags().isEmpty()) {
                // Eg: re `(?i<cursor>)`
                completionItems.add(new SnippetCompletionItem(ctx,
                        new SnippetBlock("-", snippet + "-", snippet + "-",
                                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)));
            }
            addReFlags(nodeAtCursor, ctx, completionItems, snippet);
            return completionItems;
        } else if (isCursorAfterReOnOffFlagMinusPosition(nodeAtCursor)) {
            // Eg: re `(?i-<cursor>)`, re `(?-<cursor>)`
            String snippet = resolveWordForTheGivenCursorPosition(ctx, nodeFinder);
            addReFlags(nodeAtCursor, ctx, completionItems, snippet);
            completionItems.add(new SnippetCompletionItem(ctx, RegexSnippet.DEF_COLON_CHAR.get()));
            return completionItems;
        } else if (nodeAtCursor.kind() == SyntaxKind.REGEX_TEMPLATE_EXPRESSION) {
            // Eg: re `<cursor>`, re `<cursor>()`, re `<cursor>ABC`
            completionItems.add(new SnippetCompletionItem(ctx,
                    Snippet.DEF_PARANTHESIS.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    Snippet.DEF_SQUARE_BRACKET.get()));
            completionItems.add(new SnippetCompletionItem(ctx,
                    RegexpCompletionProvider.RegexSnippet.DEF_ASSERTION_START.get()));
        }
        if (nodeFinder.getTemplateExpressionNode() != null
                && nodeFinder.getTemplateExpressionNode().textRange().endOffset() - 1
                == ctx.getCursorPositionInTree()
                && nodeAtCursor.kind() != SyntaxKind.RE_QUOTE_ESCAPE) {
            // Eg: re `abc<cursor>`,

            if (resolvedWord == null) {
                resolvedWord = resolveWordForTheGivenCursorPosition(ctx, nodeFinder);
            }
            completionItems.add(new SnippetCompletionItem(ctx,
                    new SnippetBlock("$", resolvedWord + "$", resolvedWord + "$",
                            ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)));
        }
        return completionItems;
    }

    private static void addReFlags(NonTerminalNode nodeAtCursor, BallerinaCompletionContext ctx,
                                   List<LSCompletionItem> completionItems, String snippet) {
        String reFlagsSource = nodeAtCursor.parent().parent().toSourceCode();
        for (String flag : reFlags) {
            if (!reFlagsSource.contains(flag)) {
                completionItems.add(new SnippetCompletionItem(ctx,
                        new SnippetBlock(flag, snippet + flag, snippet + flag,
                                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)));
            }
        }
    }

    private static String resolveWordForTheGivenCursorPosition(BallerinaCompletionContext ctx,
                                                               RegexTemplateNodeFinder nodeFinder) {
        String symbol = "";
        TemplateExpressionNode templateExpressionNode = nodeFinder.getTemplateExpressionNode();
        if (templateExpressionNode != null) {
            if (templateExpressionNode.startBacktick().textRange().endOffset()
                    == templateExpressionNode.endBacktick().textRange().startOffset()) {
                return symbol;
            }
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

    private static boolean isCursorAfterReFlag(NonTerminalNode nodeAtCursor) {
        return nodeAtCursor.parent().kind() == SyntaxKind.RE_FLAGS;
    }

    private static boolean isCursorAfterReOnOffFlagMinusPosition(NonTerminalNode nodeAtCursor) {
        return nodeAtCursor.kind() == SyntaxKind.RE_FLAGS_ON_OFF
                && ((ReFlagsOnOffNode) nodeAtCursor).minusToken().isPresent();
    }

    private static boolean isCommaPresentInReBracedQuantifierNode(NonTerminalNode nodeAtCursor,
                                                                  BallerinaCompletionContext ctx) {
        return nodeAtCursor.parent().kind() == SyntaxKind.RE_BRACED_QUANTIFIER &&
                ((ReBracedQuantifierNode) nodeAtCursor.parent()).commaToken().isEmpty()
                && ((ReBracedQuantifierNode) nodeAtCursor.parent())
                .leastTimesMatchedDigit().size() + 1 <= ctx.getCursorPositionInTree();
    }

    private static void addUnicodeGeneralCategoryMajorSnnipets(BallerinaCompletionContext ctx,
                                                               List<LSCompletionItem> completionItems) {
        completionItems.add(new SnippetCompletionItem(ctx,
                RegexpCompletionProvider.RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_LETTER.snippetBlock));
        completionItems.add(new SnippetCompletionItem(ctx,
                RegexpCompletionProvider.RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_MARK.snippetBlock));
        completionItems.add(new SnippetCompletionItem(ctx,
                RegexpCompletionProvider.RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_NUMBER.snippetBlock));
        completionItems.add(new SnippetCompletionItem(ctx,
                RegexpCompletionProvider.RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_SYMBOL.snippetBlock));
        completionItems.add(new SnippetCompletionItem(ctx,
                RegexpCompletionProvider.RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION.snippetBlock));
        completionItems.add(new SnippetCompletionItem(ctx,
                RegexpCompletionProvider.RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR.snippetBlock));
        completionItems.add(new SnippetCompletionItem(ctx,
                RegexpCompletionProvider.RegexSnippet.DEF_UNICODE_GENERAL_CATEGORY_OTHER.snippetBlock));
    }

    private static void addUnicodeGeneralCategorySnippets(ReUnicodeGeneralCategoryNode unicodeGeneralCategoryNode,
                                                          BallerinaCompletionContext ctx,
                                                          List<LSCompletionItem> completionItems) {
        switch (unicodeGeneralCategoryNode.reUnicodeGeneralCategoryName().toSourceCode()) {
            case "L":
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_LETTER_UPPERCASE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_LETTER_LOWERCASE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_LETTER_TITLECASE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_LETTER_MODIFIER.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_LETTER_OTHER.snippetBlock));
                break;
            case "M":
                completionItems.add(new SnippetCompletionItem(ctx, Snippet.DEF_SQUARE_BRACKET.get()));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_MARK_NON_SPACING.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_MARK_SPACING.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_MARK_NON_ENCLOSING.snippetBlock));
                break;
            case "N":
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_NUMBER_DECIMAL_DIGIT.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_NUMBER_LETTER.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_NUMBER_OTHER.snippetBlock));
                break;
            case "S":
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_MATH.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_CURRENCY.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_MODIFIER.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_OTHER.snippetBlock));
                break;
            case "P":
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_CONNECTOR.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_DASH.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_OPEN.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_CLOSE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_INITIAL_QUOTE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_FINAL_QUOTE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_OTHER.snippetBlock));
                break;
            case "Z":
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR_SPACE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR_LINE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR_PARAGRAPH.snippetBlock));
                break;
            case "C":
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_OTHER_CONTROL.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_OTHER_FORMAT.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_OTHER_PRIVATE_USE.snippetBlock));
                completionItems.add(new SnippetCompletionItem(ctx,
                        RegexpCompletionProvider.RegexSnippet
                                .DEF_UNICODE_GENERAL_CATEGORY_OTHER_NOT_ASSIGNED.snippetBlock));
                break;
            default:
                break;
        }
    }

    private static boolean isReAtom(NonTerminalNode nodeAtCursor, BallerinaCompletionContext ctx) {
        return nodeAtCursor.kind() == SyntaxKind.RE_CHARACTER_CLASS
                && nodeAtCursor.textRange().endOffset() == ctx.getCursorPositionInTree()
                || nodeAtCursor.kind() == SyntaxKind.RE_LITERAL_CHAR_DOT_OR_ESCAPE
                || nodeAtCursor.kind() == SyntaxKind.RE_SIMPLE_CHAR_CLASS_ESCAPE
                || nodeAtCursor.kind() == SyntaxKind.RE_QUOTE_ESCAPE
                || nodeAtCursor.kind() == SyntaxKind.RE_CAPTURING_GROUP;
    }

    private static boolean isFlagExpr(NonTerminalNode nodeAtCursor, BallerinaCompletionContext ctx) {
        return nodeAtCursor.kind() == SyntaxKind.RE_FLAG_EXPR
                && ((ReFlagExpressionNode) nodeAtCursor).questionMark().position() + 1 == ctx.getCursorPositionInTree();
    }

    private static boolean isUnicodePropertyEscapeNode(NonTerminalNode nodeAtCursor, BallerinaCompletionContext ctx) {
        return nodeAtCursor.kind() == SyntaxKind.RE_UNICODE_PROPERTY_ESCAPE
                && !((ReUnicodePropertyEscapeNode) nodeAtCursor).openBraceToken().isMissing()
                && !((ReUnicodePropertyEscapeNode) nodeAtCursor).closeBraceToken().isMissing()
                && ((ReUnicodePropertyEscapeNode) nodeAtCursor).openBraceToken().textRange().endOffset()
                == ctx.getCursorPositionInTree();
    }

    private enum RegexSnippet {

        DEF_DIGIT(new SnippetBlock("d", "d", "d", ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_NON_DIGIT(new SnippetBlock("D", "D", "D", ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_WHITESPACE(new SnippetBlock("s", "s", "s", ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_NON_WHITESPACE(new SnippetBlock("S", "S", "S", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_ALPHA_NUMERIC(new SnippetBlock("w", "w", "w", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_NON_ALPHA_NUMERIC(new SnippetBlock("W", "W", "W", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_RETURN(new SnippetBlock("r", "r", "r", ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_NEWLINE(new SnippetBlock("n", "n", "n", ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_TAB(new SnippetBlock("t", "t", "t", ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_MULTILINE_FLAG(new SnippetBlock("m", "m", "m", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_DOT_ALL_FLAG(new SnippetBlock("s", "s", "s", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_IGNORE_CASE_FLAG(new SnippetBlock("i", "i", "i", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_COMMENT_FLAG(new SnippetBlock("x", "x", "x", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_ASSERTION_START(new SnippetBlock("^", "^", "^", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_ASSERTION_END(new SnippetBlock("$", "$", "$", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_BACKSLASH_CHAR(new SnippetBlock("\\", "\\", "\\", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_DOT_CHAR(new SnippetBlock(".", ".", ".", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_QUANTIFIER_ASTERISK_CHAR(new SnippetBlock("*", "*", "*", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_QUANTIFIER_PLUS_CHAR(new SnippetBlock("+", "+", "+", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_QUANTIFIER_QUESTION_MARK_CHAR(new SnippetBlock("?", "?", "?", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_COLON_CHAR(new SnippetBlock(":", ":", ":", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_MINUS_CHAR(new SnippetBlock("-", "-", "-", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_LEFT_BRACE_CHAR(new SnippetBlock("(", "(", "(", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_RIGHT_BRACE_CHAR(new SnippetBlock(")", ")", ")", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_LEFT_SQUARE_BRACE_CHAR(new SnippetBlock("[", "[", "[", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_RIGHT_SQUARE_BRACE_CHAR(new SnippetBlock("]", "]", "]", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_LEFT_CURLY_BRACE_CHAR(new SnippetBlock("{", "{", "{", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_RIGHT_CURLY_BRACE_CHAR(new SnippetBlock("}", "}", "}", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_PIPE_CHAR(new SnippetBlock("|", "|", "|", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_PROPERTY_CHAR(new SnippetBlock("p", "p", "p", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_NON_UNICODE_PROPERTY_CHAR(new SnippetBlock("P", "P", "P", ItemResolverConstants.VALUE_TYPE,
                SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_SCRIPT_PROPERTY_KEY(new SnippetBlock("sc=", "sc=", "sc=",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_PROPERTY_KEY(new SnippetBlock("gc=", "gc=", "gc=",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_LETTER(new SnippetBlock("L", "L", "L",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_LETTER_UPPERCASE(new SnippetBlock("u", "Lu", "Lu",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_LETTER_LOWERCASE(new SnippetBlock("l", "Ll", "Ll",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_LETTER_TITLECASE(new SnippetBlock("t", "Lt", "Lt",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_LETTER_MODIFIER(new SnippetBlock("m", "Lm", "Lm",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_LETTER_OTHER(new SnippetBlock("o", "Lo", "Lo",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_MARK(new SnippetBlock("M", "M", "M",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_MARK_NON_SPACING(new SnippetBlock("n", "Mn", "Mn",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_MARK_SPACING(new SnippetBlock("c", "Mc", "Mc",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_MARK_NON_ENCLOSING(new SnippetBlock("e", "Me", "Me",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_NUMBER(new SnippetBlock("N", "N", "N",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_NUMBER_DECIMAL_DIGIT(new SnippetBlock("d", "Nd", "Nd",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_NUMBER_LETTER(new SnippetBlock("l", "Nl", "Nl",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_NUMBER_OTHER(new SnippetBlock("o", "No", "No",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_SYMBOL(new SnippetBlock("S", "S", "S",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_MATH(new SnippetBlock("m", "Sm", "Sm",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_CURRENCY(new SnippetBlock("c", "Sc", "Sc",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_MODIFIER(new SnippetBlock("k", "Sk", "Sk",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_SYMBOL_OTHER(new SnippetBlock("o", "So", "So",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION(new SnippetBlock("P", "P", "P",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_CONNECTOR(new SnippetBlock("c", "Pc", "Pc",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_DASH(new SnippetBlock("d", "Pd", "Pd",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_OPEN(new SnippetBlock("s", "Ps", "Ps",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_CLOSE(new SnippetBlock("e", "Pe", "Pe",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_INITIAL_QUOTE(new SnippetBlock("i", "Pi", "Pi",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_FINAL_QUOTE(new SnippetBlock("f", "Pf", "Pf",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_PUNCTUATION_OTHER(new SnippetBlock("o", "Po", "Po",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR(new SnippetBlock("Z", "Z", "Z",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR_SPACE(new SnippetBlock("s", "Zs", "Zs",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR_LINE(new SnippetBlock("l", "Zl", "Zl",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_SEPARATOR_PARAGRAPH(new SnippetBlock("p", "Zp", "Zp",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_OTHER(new SnippetBlock("C", "C", "C",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_OTHER_CONTROL(new SnippetBlock("c", "Cc", "Cc",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_OTHER_FORMAT(new SnippetBlock("f", "Cf", "Cf",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_OTHER_PRIVATE_USE(new SnippetBlock("o", "Co", "Co",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE)),

        DEF_UNICODE_GENERAL_CATEGORY_OTHER_NOT_ASSIGNED(new SnippetBlock("n", "Cn", "Cn",
                ItemResolverConstants.VALUE_TYPE, SnippetBlock.Kind.VALUE));

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
