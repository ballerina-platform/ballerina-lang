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
package org.ballerinalang.formatter.core;

import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerinalang.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerinalang.compiler.syntax.tree.ChildNodeList;
import io.ballerinalang.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerinalang.compiler.syntax.tree.Minutiae;
import io.ballerinalang.compiler.syntax.tree.MinutiaeList;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.syntax.tree.Token;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.ballerinalang.compiler.syntax.tree.AbstractNodeFactory.createWhitespaceMinutiae;

/**
 * Class that contains the util functions used by the formatting tree modifier.
 */
class FormatterUtils {

    private FormatterUtils() {

    }

    private static final String LINE_SEPARATOR = "line.separator";

    /**
     * Get the node position.
     *
     * @param node node
     * @return node position
     */
    static DiagnosticPos getPosition(Node node) {
        if (node == null) {
            return null;
        }
        LineRange range = node.lineRange();
        LinePosition startPos = range.startLine();
        LinePosition endPos = range.endLine();
        int startOffset = startPos.offset();
        if (node.kind() == SyntaxKind.FUNCTION_DEFINITION || node.kind() == SyntaxKind.TYPE_DEFINITION ||
                node.kind() == SyntaxKind.CONST_DECLARATION || node.kind() == SyntaxKind.OBJECT_TYPE_DESC ||
                node.kind() == SyntaxKind.MATCH_STATEMENT) {
            startOffset = (startOffset / 4) * 4;
        }
        return new DiagnosticPos(null, startPos.line() + 1, endPos.line() + 1,
                startOffset, endPos.offset());
    }

    // TODO: Use a generic way to get the parent node using querying.
    static <T extends Node> Node getParent(T node, SyntaxKind syntaxKind) {
        Node parent = node.parent();
        if (parent == null) {
            parent = node;
        }
        Node grandParent = parent.parent();
        SyntaxKind parentKind = parent.kind();
        if (parentKind == SyntaxKind.MODULE_VAR_DECL) {
            if (grandParent != null && grandParent.kind() == SyntaxKind.MODULE_PART &&
                    syntaxKind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                return null;
            }
            return parent;
        }
        if (parentKind == SyntaxKind.FUNCTION_CALL && grandParent != null &&
                grandParent.kind() == SyntaxKind.PANIC_STATEMENT) {
            return null;
        }
        if (parentKind == SyntaxKind.FUNCTION_DEFINITION ||
                parentKind == SyntaxKind.IF_ELSE_STATEMENT ||
                parentKind == SyntaxKind.LOCAL_TYPE_DEFINITION_STATEMENT ||
                parentKind == SyntaxKind.WHILE_STATEMENT ||
                parentKind == SyntaxKind.DO_STATEMENT ||
                parentKind == SyntaxKind.LOCK_STATEMENT ||
                parentKind == SyntaxKind.CONST_DECLARATION ||
                parentKind == SyntaxKind.METHOD_DECLARATION ||
                parentKind == SyntaxKind.TYPE_DEFINITION ||
                parentKind == SyntaxKind.CLASS_DEFINITION) {
            return parent;
        }
        if (parentKind == SyntaxKind.MATCH_CLAUSE && grandParent != null &&
                grandParent.kind() == SyntaxKind.MATCH_STATEMENT) {
            int previousToken = getChildLocation((NonTerminalNode) parent, node) - 1;
            if (previousToken > 0 && node.parent().children().size() > previousToken &&
                    node.parent().children().get(previousToken).kind() == SyntaxKind.PIPE_TOKEN) {
                return null;
            }
            return grandParent;
        }
        if (syntaxKind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            if (parentKind == SyntaxKind.REQUIRED_PARAM ||
                    parentKind == SyntaxKind.POSITIONAL_ARG ||
                    parentKind == SyntaxKind.BINARY_EXPRESSION ||
                    parentKind == SyntaxKind.BRACED_EXPRESSION ||
                    parentKind == SyntaxKind.RETURN_STATEMENT ||
                    parentKind == SyntaxKind.REMOTE_METHOD_CALL_ACTION ||
                    parentKind == SyntaxKind.FIELD_ACCESS ||
                    (parentKind == SyntaxKind.FUNCTION_CALL && grandParent != null &&
                            (grandParent.kind() == SyntaxKind.ASSIGNMENT_STATEMENT ||
                            grandParent.kind() == SyntaxKind.CHECK_EXPRESSION))) {
                if (parentKind == SyntaxKind.FIELD_ACCESS &&
                        ((FieldAccessExpressionNode) parent).expression() == node && grandParent != null &&
                        grandParent.kind() == SyntaxKind.ASSIGNMENT_STATEMENT && grandParent.parent() != null &&
                        grandParent.parent().kind() == SyntaxKind.BLOCK_STATEMENT) {
                    return getParent(grandParent.parent(), syntaxKind);
                }
                return null;
            }
            return getParent(parent, syntaxKind);
        }
        if (syntaxKind == SyntaxKind.STRING_TYPE_DESC &&
                parentKind == SyntaxKind.RECORD_FIELD && grandParent != null &&
                grandParent.kind() == SyntaxKind.RECORD_TYPE_DESC) {
            return getParent(parent, syntaxKind);
        }
        if (syntaxKind == SyntaxKind.OBJECT_CONSTRUCTOR &&
                parentKind == SyntaxKind.LOCAL_VAR_DECL) {
            return parent;
        }
        if (syntaxKind == SyntaxKind.ON_FAIL_CLAUSE && (parentKind == SyntaxKind.MATCH_STATEMENT ||
                parentKind == SyntaxKind.FOREACH_STATEMENT)) {
            return parent;
        }
        if (parentKind == SyntaxKind.ON_FAIL_CLAUSE) {
            if (syntaxKind == SyntaxKind.VAR_TYPE_DESC) {
                return null;
            }
            return parent;
        }
        if (parentKind == SyntaxKind.SERVICE_DECLARATION ||
                parentKind == SyntaxKind.BINARY_EXPRESSION) {
            if (syntaxKind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                return null;
            }
            return parent;
        }
        if (parentKind == SyntaxKind.REQUIRED_PARAM || parentKind == SyntaxKind.TYPE_TEST_EXPRESSION) {
            return null;
        }
        if (parentKind == SyntaxKind.OBJECT_TYPE_DESC) {
            if (grandParent != null && grandParent.kind() == SyntaxKind.RETURN_TYPE_DESCRIPTOR) {
                return grandParent.parent().parent();
            } else if (grandParent != null && grandParent.kind() == SyntaxKind.TYPE_DEFINITION) {
                return getParent(parent, syntaxKind);
            } else {
                return parent;
            }
        }
        if (parentKind == SyntaxKind.OBJECT_CONSTRUCTOR && grandParent != null &&
                grandParent.kind() == SyntaxKind.LOCAL_VAR_DECL) {
            return grandParent;
        }
        if (parentKind == SyntaxKind.TYPE_CAST_PARAM && grandParent != null &&
                grandParent.kind() == SyntaxKind.TYPE_CAST_EXPRESSION) {
            return null;
        }
        if (grandParent != null) {
            return getParent(parent, syntaxKind);
        }
        return null;
    }

    static int getIndentation(Node node, int indentation, FormattingOptions formattingOptions) {
        if (node == null) {
            return indentation;
        }
        if (node.parent() != null) {
            SyntaxKind parentKind = node.parent().kind();
            if (parentKind == SyntaxKind.BLOCK_STATEMENT ||
                    parentKind == SyntaxKind.FUNCTION_BODY_BLOCK ||
                    parentKind == SyntaxKind.LIST_CONSTRUCTOR ||
                    parentKind == SyntaxKind.MATCH_STATEMENT ||
                    parentKind == SyntaxKind.TYPE_DEFINITION ||
                    parentKind == SyntaxKind.METHOD_DECLARATION ||
                    parentKind == SyntaxKind.MAPPING_CONSTRUCTOR ||
                    parentKind == SyntaxKind.CLASS_DEFINITION) {
                indentation += formattingOptions.getTabSize();
                Node grandParent = node.parent().parent();
                if (grandParent != null && grandParent.kind() == SyntaxKind.DO_STATEMENT) {
                    indentation -= formattingOptions.getTabSize();
                }
            }
        }
        return getIndentation(node.parent(), indentation, formattingOptions);
    }

    private static MinutiaeList getCommentMinutiae(MinutiaeList minutiaeList, boolean isLeading) {
        MinutiaeList minutiaes = AbstractNodeFactory.createEmptyMinutiaeList();
        for (int i = 0; i < minutiaeList.size(); i++) {
            if (minutiaeList.get(i).kind() == SyntaxKind.COMMENT_MINUTIAE) {
                if (i > 0) {
                    minutiaes = minutiaes.add(minutiaeList.get(i - 1));
                }
                minutiaes = minutiaes.add(minutiaeList.get(i));
                if ((i + 1) < minutiaeList.size() && isLeading) {
                    minutiaes = minutiaes.add(minutiaeList.get(i + 1));
                }
            }
        }
        return minutiaes;
    }

    private static String getWhiteSpaces(int column, int newLines) {
        StringBuilder whiteSpaces = new StringBuilder();
        for (int i = 0; i <= (newLines - 1); i++) {
            whiteSpaces.append(System.getProperty(LINE_SEPARATOR));
        }
        for (int i = 0; i <= (column - 1); i++) {
            whiteSpaces.append(" ");
        }
        return whiteSpaces.toString();
    }

    /**
     * Initialize the token with empty minutiae lists.
     *
     * @param node node
     * @return token with empty minutiae
     */
    static <T extends Token> Token getToken(T node) {
        if (node == null) {
            return node;
        }
        MinutiaeList leadingMinutiaeList = AbstractNodeFactory.createEmptyMinutiaeList();
        MinutiaeList trailingMinutiaeList = AbstractNodeFactory.createEmptyMinutiaeList();
        if (node.containsLeadingMinutiae()) {
            leadingMinutiaeList = getCommentMinutiae(node.leadingMinutiae(), true);
        }
        if (node.containsTrailingMinutiae()) {
            trailingMinutiaeList = getCommentMinutiae(node.trailingMinutiae(), false);
        }
        return node.modify(leadingMinutiaeList, trailingMinutiaeList);
    }

    static boolean isInLineRange(Node node, LineRange lineRange) {
        if (lineRange == null) {
            return true;
        }
        int nodeStartLine = node.lineRange().startLine().line();
        int nodeStartOffset = node.lineRange().startLine().offset();
        int nodeEndLine = node.lineRange().endLine().line();
        int nodeEndOffset = node.lineRange().endLine().offset();

        int startLine = lineRange.startLine().line();
        int startOffset = lineRange.startLine().offset();
        int endLine = lineRange.endLine().line();
        int endOffset = lineRange.endLine().offset();

        if (nodeStartLine >= startLine && nodeEndLine <= endLine) {
            if (nodeStartLine == startLine || nodeEndLine == endLine) {
                return nodeStartOffset >= startOffset && nodeEndOffset <= endOffset;
            }
            return true;
        }
        return false;
    }

    /**
     * Update the minutiae and return the token.
     *
     * @param token            token
     * @param leadingSpaces    leading spaces
     * @param trailingSpaces   trailing spaces
     * @param leadingNewLines  leading new lines
     * @param trailingNewLines trailing new lines
     * @return updated token
     */
    static Token formatToken(Token token, int leadingSpaces, int trailingSpaces, int leadingNewLines,
                             int trailingNewLines) {
        if (token == null) {
            return token;
        }
        MinutiaeList newLeadingMinutiaeList = preserveComments(token.leadingMinutiae(), trailingNewLines)
                .add(createWhitespaceMinutiae(getWhiteSpaces(leadingSpaces, leadingNewLines)));
        MinutiaeList newTrailingMinutiaeList = preserveComments(token.trailingMinutiae(), trailingNewLines)
                .add(createWhitespaceMinutiae(getWhiteSpaces(trailingSpaces, trailingNewLines)));

        return token.modify(newLeadingMinutiaeList, newTrailingMinutiaeList);
    }

    private static MinutiaeList preserveComments(MinutiaeList minutiaeList, int trailingNewLines) {
        MinutiaeList minutiaes = AbstractNodeFactory.createEmptyMinutiaeList();
        if (minutiaeList.size() > 0) {
            int count = commentCount(minutiaeList);
            if (count > 0) {
                int processedCount = 0;
                for (int i = 0; i < minutiaeList.size(); i++) {
                    Minutiae minutiae = minutiaeList.get(i);
                    minutiaes = minutiaes.add(minutiae);
                    if (minutiae.kind() == SyntaxKind.COMMENT_MINUTIAE) {
                        processedCount++;
                        if (processedCount == count) {
                            if (trailingNewLines == 0) {
                                minutiaes = minutiaes.add(AbstractNodeFactory
                                        .createEndOfLineMinutiae(System.getProperty(LINE_SEPARATOR)));
                            }
                            break;
                        }
                    }
                }
            }
        }
        return minutiaes;
    }

    private static int commentCount(MinutiaeList minutiaeList) {
        int count = 0;
        for (int i = 0; i < minutiaeList.size(); i++) {
            if (minutiaeList.get(i).kind() == SyntaxKind.COMMENT_MINUTIAE) {
                count++;
            }
        }
        return count;
    }

    private static int leadingNewLines(NonTerminalNode parent, Token node) {
        int count = 0;
        if (parent == null) {
            return count;
        }
        int childLocation = getChildLocation(parent, node);
        if (parent.children().size() <= childLocation + 1) {
            return count;
        }
        Token nextToken = getFirstToken(parent.children().get(childLocation + 1));
        if (nextToken != null && nextToken.containsLeadingMinutiae()) {
            MinutiaeList minutiaes = nextToken.leadingMinutiae();
            if (commentCount(minutiaes) > 0) {
                for (Minutiae minutiae : minutiaes) {
                    if (minutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE) {
                        count++;
                    } else {
                        break;
                    }
                }
            }
        }
        return count;
    }

    static int getTrailingNewLines(NonTerminalNode node, Token token) {
        int leadingCount;
        leadingCount = leadingNewLines(node, token);
        if (leadingCount == 0) {
            return 2;
        } else if (leadingCount == 1) {
            return 1;
        } else {
            return 0;
        }
    }

    private static Token getFirstToken(Node node) {
        if (node instanceof Token) {
            return (Token) node;
        }
        NonTerminalNode parent = (NonTerminalNode) node;
        return getFirstToken(parent.children().get(0));
    }

    static int getChildLocation(NonTerminalNode parent, Node child) {
        if (parent == null || child == null) {
            return -1;
        }
        for (int i = 0; i < parent.children().size(); i++) {
            if (parent.children().get(i).equals(child)) {
                return i;
            }
        }
        return -1;
    }

    private static int regexCount(String context, String pattern) {
        Matcher matcher = Pattern.compile(String.valueOf(pattern)).matcher(context);
        int response = 0;
        while (matcher.find()) {
            response++;
        }
        return response;
    }

    private static Token getStartingToken(Node node) {
        if (node instanceof Token) {
            return (Token) node;
        }
        ChildNodeList childNodeList = ((NonTerminalNode) node).children();
        return getStartingToken(childNodeList.get(0));
    }

    private static Token getEndingToken(Node node) {
        if (node instanceof Token) {
            return (Token) node;
        }
        ChildNodeList childNodeList = ((NonTerminalNode) node).children();
        return getStartingToken(childNodeList.get(childNodeList.size() - 1));
    }

    static boolean preserveNewLine(NonTerminalNode node) {
        ArrayList<SyntaxKind> endTokens = new ArrayList<>(
                Arrays.asList(
                        SyntaxKind.CLOSE_BRACE_TOKEN,
                        SyntaxKind.CLOSE_BRACE_PIPE_TOKEN,
                        SyntaxKind.CLOSE_BRACKET_TOKEN,
                        SyntaxKind.CLOSE_PAREN_TOKEN));
        boolean preserve = false;
        MinutiaeList nodeEnd = getEndingToken(node).trailingMinutiae();
        if (nodeEnd.toString().contains(System.getProperty(LINE_SEPARATOR))) {
            int childIndex = getChildLocation(node.parent(), node);
            if (childIndex != -1) {
                Node nextNode = node.parent().children().get(childIndex + 1);
                if (nextNode != null && !endTokens.contains(nextNode.kind())) {
                    MinutiaeList siblingStart = getStartingToken(nextNode).leadingMinutiae();
                    int newLines = regexCount(nodeEnd.toString(), System.getProperty(LINE_SEPARATOR));
                    if (siblingStart.toString().contains(System.getProperty(LINE_SEPARATOR)) || newLines > 1) {
                        preserve = true;
                    }
                }
            }
        }
        return preserve;
    }

    private static ArrayList<NonTerminalNode> nestedIfBlock(NonTerminalNode node) {
        NonTerminalNode parent = node.parent();
        ArrayList<NonTerminalNode> nestedParent = new ArrayList<>();
        if (parent == null) {
            return new ArrayList<>(0);
        }
        while (parent != null) {
            if (parent.kind() == (node.kind())) {
                nestedParent.add(parent);
            }
            parent = parent.parent();
        }
        return nestedParent;
    }

    /**
     * return the indented start column.
     *
     * @param node       node
     * @param addSpaces  add spaces or not
     * @return start position
     */
    static int getStartColumn(Node node, boolean addSpaces, FormattingOptions formattingOptions) {
        Node parent;
        if (node.kind() == SyntaxKind.IF_ELSE_STATEMENT) {
            Indentation indent = getIfElseParent((NonTerminalNode) node);
            parent = indent.getParent();
            addSpaces = indent.getAddSpaces();
        } else if (node.kind() == SyntaxKind.BLOCK_STATEMENT) {
            Indentation indent = getBlockParent(node);
            parent = indent.getParent();
            addSpaces = indent.getAddSpaces();
        } else {
            parent = getParent(node, node.kind());
        }
        if (parent != null) {
            int indentation = 0;
            if (addSpaces) {
                indentation = (FormatterUtils.getIndentation(node, 0, formattingOptions));
            }
            return getPosition(parent).sCol + indentation;
        }
        return 0;
    }

    private static Indentation getIfElseParent(NonTerminalNode node) {
        NonTerminalNode parent = node.parent();
        if (parent == null) {
            parent = node;
        }
        if (parent.kind() == SyntaxKind.FUNCTION_DEFINITION) {
            return new Indentation(parent, true);
        } else if (parent.parent() != null) {
            return getIfElseParent(parent);
        }
        return new Indentation(null, false);
    }

    private static Indentation getBlockParent(Node node) {
        Node parent = node.parent();
        if (parent == null) {
            parent = node;
        }
        ArrayList<SyntaxKind> parentWithSpaces = new ArrayList<>(
                Arrays.asList(
                        SyntaxKind.WHILE_STATEMENT,
                        SyntaxKind.LOCK_STATEMENT,
                        SyntaxKind.ON_FAIL_CLAUSE,
                        SyntaxKind.FUNCTION_DEFINITION));
        ArrayList<SyntaxKind> parentWithoutSpaces = new ArrayList<>(
                Arrays.asList(
                        SyntaxKind.NAMED_WORKER_DECLARATION,
                        SyntaxKind.LOCAL_VAR_DECL,
                        SyntaxKind.MATCH_CLAUSE,
                        SyntaxKind.DO_STATEMENT,
                        SyntaxKind.FOREACH_STATEMENT));

        if (parentWithSpaces.contains(parent.kind())) {
            return new Indentation(parent, true);
        }
        if (parent.kind() == SyntaxKind.IF_ELSE_STATEMENT) {
            ArrayList nestedBlock = nestedIfBlock((NonTerminalNode) parent);
            if (!nestedBlock.isEmpty()) {
                NonTerminalNode nestedIfParent = (NonTerminalNode) nestedBlock.get(0);
                return new Indentation((nestedIfParent != null) ? nestedIfParent : parent, false);
            }
            return new Indentation(parent, false);
        }
        if (parentWithoutSpaces.contains(parent.kind())) {
            return new Indentation(parent, false);
        }
        if (parent.parent() != null) {
            return getBlockParent(parent);
        }
        return new Indentation(null, false);
    }

    static boolean addNewTrailingLine(NonTerminalNode parent, NonTerminalNode node) {
        if (parent == null) {
            return true;
        }
        int childLocation = getChildLocation(parent, node);
        if (parent.children().size() > childLocation + 1) {
            Token nextToken = getFirstToken(parent.children().get(childLocation + 1));
            if (nextToken != null && nextToken.containsLeadingMinutiae()) {
                return (nextToken.leadingMinutiae().get(0).kind() != SyntaxKind.END_OF_LINE_MINUTIAE);
            }
        }
        return true;
    }

    /**
     * Converts the syntax tree into source code, remove superfluous spaces and newlines at the ending and returns it
     * as a string.
     *
     * @param syntaxTree       syntaxTree
     * @return source code as a string
     */
    public static String toFormattedSourceCode(SyntaxTree syntaxTree) {
        return syntaxTree.toSourceCode().trim() +
                System.getProperty(LINE_SEPARATOR);
    }

    private static final class Indentation {
        private final Node parent;
        private final boolean addSpaces;

        private Indentation(Node parent, boolean addSpaces) {
            this.parent = parent;
            this.addSpaces = addSpaces;
        }

        private Node getParent() {
            return parent;
        }

        private boolean getAddSpaces() {
            return addSpaces;
        }
    }
}
