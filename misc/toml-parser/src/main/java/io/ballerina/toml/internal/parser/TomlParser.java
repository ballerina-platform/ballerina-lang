/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.toml.internal.parser;

import io.ballerina.toml.internal.parser.tree.STNode;
import io.ballerina.toml.internal.parser.tree.STNodeFactory;
import io.ballerina.toml.internal.parser.tree.STToken;
import io.ballerina.toml.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.toml.syntax.tree.SyntaxKind.BINARY_INT;
import static io.ballerina.toml.syntax.tree.SyntaxKind.CLOSE_BRACKET_TOKEN;
import static io.ballerina.toml.syntax.tree.SyntaxKind.DEC_INT;
import static io.ballerina.toml.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.toml.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.toml.syntax.tree.SyntaxKind.FLOAT;
import static io.ballerina.toml.syntax.tree.SyntaxKind.HEX_INT;
import static io.ballerina.toml.syntax.tree.SyntaxKind.NEWLINE;
import static io.ballerina.toml.syntax.tree.SyntaxKind.OCT_INT;
import static io.ballerina.toml.syntax.tree.SyntaxKind.OPEN_BRACKET_TOKEN;
import static io.ballerina.toml.syntax.tree.SyntaxKind.SINGLE_QUOTE_TOKEN;
import static io.ballerina.toml.syntax.tree.SyntaxKind.TRIPLE_DOUBLE_QUOTE_TOKEN;
import static io.ballerina.toml.syntax.tree.SyntaxKind.TRIPLE_SINGLE_QUOTE_TOKEN;

/**
 * A LL(k) recursive-descent parser for TOML.
 *
 * @since 2.0.0
 */
public class TomlParser extends AbstractParser {

    public TomlParser(AbstractTokenReader tokenReader) {
        super(tokenReader, new TomlParserErrorHandler(tokenReader));
    }

    /**
     * Parse a given input and returns the AST. Starts parsing from the top of a compilation unit.
     *
     * @return Parsed node
     */
    @Override
    public STNode parse() {
        List<STNode> topLevelNodes = new ArrayList<>();
        STToken token = peek();

        while (token.kind != SyntaxKind.EOF_TOKEN) {
            STNode decl = parseTopLevelNode();
            if (decl == null) {
                break;
            }

            topLevelNodes.add(decl);
            token = peek();
        }

        STToken eof = consume();
        return STNodeFactory.createDocumentNode(
                STNodeFactory.createNodeList(topLevelNodes), eof);
    }

    /**
     * Parse top level node given the next token kind and the modifier that precedes it.
     *
     * @return Parsed top-level node
     */
    private STNode parseTopLevelNode() {
        startContext(ParserRuleContext.TOP_LEVEL_NODE);
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case EOF_TOKEN:
                return null;
            case NEWLINE:
                consume(); //New line is only used for validations. Should be ignored
                return parseTopLevelNode();
            case OPEN_BRACKET_TOKEN:
                if (peek(2).kind == OPEN_BRACKET_TOKEN) {
                    return parseArrayOfTables();
                }
                return parseTable();
            case IDENTIFIER_LITERAL:
            case SINGLE_QUOTE_TOKEN:
            case DOUBLE_QUOTE_TOKEN:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_INT_TOKEN:
            case DECIMAL_FLOAT_TOKEN:
                int lookahead = 1;
                STToken peekToken = peek(lookahead);
                while (!isEndOfStatement(peekToken)) {
                    if (peekToken.kind == CLOSE_BRACKET_TOKEN) {
                        if (peek(lookahead + 1).kind == CLOSE_BRACKET_TOKEN) {
                            return parseArrayOfTables();
                        }
                        return parseTable();
                    }
                    lookahead += 1;
                    peekToken = peek(lookahead);
                }
                return parseKeyValue();
            default:
                recover(nextToken, ParserRuleContext.TOP_LEVEL_NODE);
                return parseTopLevelNode();
        }
    }

    private boolean isEndOfStatement(STToken nextToken) {
        return nextToken.kind == SyntaxKind.EQUAL_TOKEN || nextToken.kind == SyntaxKind.NEWLINE
                || nextToken.kind == EOF_TOKEN;
    }

    /**
     * Parsing Array of Tables. Array of Table is a identifier surrounded by Double Open and Close Brackets.
     * Ex - [[ Identifier ]]
     *
     * @return TableArrayNode
     */
    private STNode parseArrayOfTables() {
        startContext(ParserRuleContext.TOML_TABLE_ARRAY);
        STNode firstOpenBracket = parseOpenBracket(ParserRuleContext.ARRAY_TABLE_FIRST_START);
        STNode secondOpenBracket = parseOpenBracket(ParserRuleContext.ARRAY_TABLE_SECOND_START);
        STNode identifierToken = parseKeys();
        STNode firstCloseBracket = parseCloseBracket(ParserRuleContext.ARRAY_TABLE_FIRST_END);
        STNode secondCloesBracket = parseCloseBracket(ParserRuleContext.ARRAY_TABLE_SECOND_END);
        STNode newline = parseNewlines();
        if (newline.hasDiagnostics()) {
            secondCloesBracket = SyntaxErrors.addSyntaxDiagnostics(secondCloesBracket, newline.diagnostics());
        }
        List<STNode> fields = parseTableEntries();
        endContext();
        return STNodeFactory.createTableArrayNode(firstOpenBracket, secondOpenBracket,
                identifierToken, firstCloseBracket, secondCloesBracket, STNodeFactory.createNodeList(fields));
    }

    /**
     * Parsing TOML Table. Table is a identifier surrounded by Open and Close Brackets.
     * Ex - [ Identifier ]
     *
     * @return TableNode
     */
    private STNode parseTable() {
        startContext(ParserRuleContext.TOML_TABLE);
        STNode openBracket = parseOpenBracket(ParserRuleContext.TABLE_START);
        STNode identifierToken = parseKeys();
        STNode closedBracket = parseCloseBracket(ParserRuleContext.TABLE_END);
        STNode newLine = parseNewlines();
        if (newLine.hasDiagnostics()) {
            closedBracket = SyntaxErrors.addSyntaxDiagnostics(closedBracket, newLine.diagnostics());
        }
        List<STNode> fields = parseTableEntries();
        endContext();
        return STNodeFactory.createTableNode(openBracket, identifierToken, closedBracket,
                STNodeFactory.createNodeList(fields));
    }

    private List<STNode> parseTableEntries() {
        List<STNode> fields = new ArrayList<>();
        STToken nextNode = peek();
        while (!isNextTokenArray(nextNode)) {
            STNode stNode = parseKeyValue();
            fields.add(stNode);
            nextNode = peek();
        }
        return fields;
    }

    private boolean isNextTokenArray(STToken nextNode) {
        return nextNode.kind == OPEN_BRACKET_TOKEN ||
                nextNode.kind == EOF_TOKEN;
    }

    private STNode parseOpenBracket(ParserRuleContext ctx) {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_BRACKET_TOKEN) {
            return consume();
        } else {
            recover(token, ctx);
            return parseOpenBracket(ctx);
        }
    }

    private STNode parseCloseBracket(ParserRuleContext ctx) {
        STToken token = peek();
        if (token.kind == SyntaxKind.CLOSE_BRACKET_TOKEN) {
            return consume();
        } else {
            recover(token, ctx);
            return parseCloseBracket(ctx);
        }
    }

    /**
     * Parsing Toml Key Value Pair. Key Value pair is constructed with Key Node, equals token and Value Node.
     * Ex - Key = Value
     *
     * @return KeyValueNode
     */
    private STNode parseKeyValue() {
        startContext(ParserRuleContext.KEY_VALUE_PAIR);
        STNode identifier = parseKeys();
        STNode equals = parseEquals();
        STNode value = parseValue();
        STNode newLine = parseNewlines();
        if (newLine.hasDiagnostics()) {
            value = SyntaxErrors.addSyntaxDiagnostics(value, newLine.diagnostics());
        }
        endContext();
        return STNodeFactory.createKeyValueNode(identifier, equals, value);
    }

    /**
     * Pares new lines.
     * New lines are appended to trivia from lexer. These tokens are only used for validations purposes.
     */
    private STNode parseNewlines() {
        STToken token = peek();
        if (token.kind == EOF_TOKEN) {
            return token;
        }

        if (!isNewline(token.kind)) {
            recover(peek(), ParserRuleContext.NEWLINE);
            return parseNewlines();
        }
        STToken recentNewline = token;
        while (isNewline(token.kind)) {
            recentNewline = consume();
            token = peek();
        }
        return recentNewline;
    }

    private boolean isNewline(SyntaxKind kind) {
        return kind == SyntaxKind.NEWLINE;
    }

    /**
     * Parses Key Node. A Key Node can be either one or many the following forms
     * UNQUOTED_KEY_TOKEN (Regular Keys) |
     * TRUE/FALSE KEYWORD |
     * STRING_LITERAL_TOKEN (Quoted Keys)
     *
     * @return KeyNodeList
     */
    private STNode parseKeys() {
        startContext(ParserRuleContext.KEY_LIST);

        STNode firstKey = parseSingleKey();
        if (firstKey == null) {
            recover(peek(), ParserRuleContext.KEY_VALUE_PAIR);
            firstKey = parseIdentifierLiteral();
        }

        return parseKeyList(firstKey);
    }

    private boolean isEndOfKeyList(STToken token) {
        return token.kind == EQUAL_TOKEN || token.kind == EOF_TOKEN || token.kind == CLOSE_BRACKET_TOKEN; //TODO revisit
    }

    private STNode parseIdentifierLiteral() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_LITERAL) {
            return STNodeFactory.createIdentifierLiteralNode(consume());
        } else {
            recover(token, ParserRuleContext.IDENTIFIER_LITERAL);
            return parseIdentifierLiteral();
        }
    }

    private STNode parseSingleKey() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case DECIMAL_INT_TOKEN:
            case DECIMAL_FLOAT_TOKEN:
                return parseNumericalNode();
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
                return parseBoolean();
            case IDENTIFIER_LITERAL:
                return parseIdentifierLiteral();
            case DOUBLE_QUOTE_TOKEN:
                return parseStringValue();
            case SINGLE_QUOTE_TOKEN:
                return parseLiteralStringValue();
            case EQUAL_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.KEY_START);
                return parseSingleKey();
        }
    }

    private STNode parseKeyList(STNode firstKey) {
        ArrayList<STNode> keyList = new ArrayList<>();
        keyList.add(firstKey);

        STToken nextToken = peek();
        while (!(isEndOfKeyList(nextToken))) {
            STNode keysEnd = parseKeyEnd();
            if (keysEnd == null) {
                // null marks the end of values
                break;
            }

            STNode curKey = parseSingleKey();
            if (curKey != null) {
                keyList.add(keysEnd);
                keyList.add(curKey);
            }

            nextToken = peek();
        }
        endContext();
        return STNodeFactory.createNodeList(keyList);
    }

    private STNode parseKeyEnd() {
        STToken token = peek();
        switch (token.kind) {
            case DOT_TOKEN:
                return consume();
            case EQUAL_TOKEN:
            case CLOSE_BRACKET_TOKEN:
                // null marks the end of values
                return null;
            default:
                recover(token, ParserRuleContext.KEY_END);
                return parseKeyEnd();
        }
    }

    private STNode parseEquals() {
        STToken token = peek();
        if (token.kind == SyntaxKind.EQUAL_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.ASSIGN_OP);
            return parseEquals();
        }
    }

    /**
     * Parses Value Node. A Value Node can be either one of the following forms
     * Basic Values
     * STRING_LITERAL_TOKEN (Single Line String) |
     * ML_STRING_LITERAL_TOKEN (Multiline String) |
     * DECIMAL_INT_TOKEN (Decimal Integer) |
     * DECIMAL_FLOAT_TOKEN (Float) |
     * BOOLEAN |
     * MISSING_VALUE_TOKEN (Recovery purposes)
     * Arrays
     *
     * @return ValueNode
     */
    private STNode parseValue() {
        STToken token = peek();
        switch (token.kind) {
            case DOUBLE_QUOTE_TOKEN:
                return parseStringValue();
            case TRIPLE_DOUBLE_QUOTE_TOKEN:
                return parseMultilineStringValue();
            case SINGLE_QUOTE_TOKEN:
                return parseLiteralStringValue();
            case TRIPLE_SINGLE_QUOTE_TOKEN:
                return parseMultilineLiteralStringValue();
            case DECIMAL_INT_TOKEN:
            case DECIMAL_FLOAT_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case OCTAL_INTEGER_LITERAL_TOKEN:
            case BINARY_INTEGER_LITERAL_TOKEN:
            case PLUS_TOKEN:
            case MINUS_TOKEN:
                return parseNumericalNode();
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
                return parseBoolean();
            case OPEN_BRACKET_TOKEN:
                return parseArray();
            default:
                recover(token, ParserRuleContext.VALUE);
                return parseValue();
        }
    }

    private STNode parseNumericalNode() {
        STNode sign = parseSign();
        STNode token = parseNumericalToken();
        SyntaxKind kind = getNumericLiteralKind(token.kind);
        return STNodeFactory.createNumericLiteralNode(kind, sign, token);
    }

    private SyntaxKind getNumericLiteralKind(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case DECIMAL_INT_TOKEN:
                return DEC_INT;
            case HEX_INTEGER_LITERAL_TOKEN:
                return HEX_INT;
            case OCTAL_INTEGER_LITERAL_TOKEN:
                return OCT_INT;
            case BINARY_INTEGER_LITERAL_TOKEN:
                return BINARY_INT;
            default:
                return FLOAT;
        }
    }

    private STNode parseNumericalToken() {
        STToken token = peek();
        if (isNumerticalLiteral(token)) {
            return consume();
        } else {
            recover(token, ParserRuleContext.NUMERICAL_LITERAL);
            return parseNumericalToken();
        }
    }

    private boolean isNumerticalLiteral(STToken token) {
        switch (token.kind) {
            case DECIMAL_INT_TOKEN:
            case DECIMAL_FLOAT_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case OCTAL_INTEGER_LITERAL_TOKEN:
            case BINARY_INTEGER_LITERAL_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode parseSign() {
        STToken token = peek();
        if (token.kind == SyntaxKind.MINUS_TOKEN || token.kind == SyntaxKind.PLUS_TOKEN) {
            return consume();
        }
        return STNodeFactory.createEmptyNode();
    }

    private STNode parseBoolean() {
        STToken token = peek();
        if (token.kind == SyntaxKind.TRUE_KEYWORD || token.kind == SyntaxKind.FALSE_KEYWORD) {
            return STNodeFactory.createBoolLiteralNode(consume());
        } else {
            recover(token, ParserRuleContext.BOOLEAN_LITERAL);
            return parseBoolean();
        }
    }

    /**
     * Parse String Value.
     *
     * @return String Literal Node.
     */
    private STNode parseStringValue() {
        STNode startingDoubleQuote = parseDoubleQuoteToken(ParserRuleContext.STRING_START);
        STNode content = parseStringContent();
        STNode endingDoubleQuote = parseDoubleQuoteToken(ParserRuleContext.STRING_END);
        return STNodeFactory.createStringLiteralNode(startingDoubleQuote, content, endingDoubleQuote);
    }

    /**
     * Parse Multiline String Value.
     *
     * @return String Multiline String Literal Node.
     */
    private STNode parseMultilineStringValue() {
        STNode startingDoubleQuote = parseTripleDoubleQuoteToken(ParserRuleContext.MULTILINE_STRING_START);
        STNode content = parseMultilineStringContent();
        STNode endingDoubleQuote = parseTripleDoubleQuoteToken(ParserRuleContext.MULTILINE_STRING_END);
        return STNodeFactory.createStringLiteralNode(startingDoubleQuote, content, endingDoubleQuote);
    }

    private STNode parseMultilineStringContent() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.IDENTIFIER_LITERAL) {
            return consume();
        }
        if (nextToken.kind == TRIPLE_DOUBLE_QUOTE_TOKEN) {
            return STNodeFactory.createEmptyNode();
        }
        recover(nextToken, ParserRuleContext.MULTILINE_STRING_CONTENT);
        return parseMultilineStringContent();
    }

    /**
     * Parse Literal String Value.
     *
     * @return String Literal Node.
     */
    private STNode parseLiteralStringValue() {
        STNode startingDoubleQuote = parseSingleQuoteToken(ParserRuleContext.LITERAL_STRING_START);
        STNode content = parseLiteralStringContent();
        STNode endingDoubleQuote = parseSingleQuoteToken(ParserRuleContext.LITERAL_STRING_END);
        return STNodeFactory.createLiteralStringLiteralNode(startingDoubleQuote, content, endingDoubleQuote);
    }

    private STNode parseLiteralStringContent() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.IDENTIFIER_LITERAL) {
            return consume();
        }
        if (nextToken.kind == SINGLE_QUOTE_TOKEN) {
            return STNodeFactory.createEmptyNode();
        }
        recover(nextToken, ParserRuleContext.LITERAL_STRING_CONTENT);
        return parseLiteralStringContent();
    }

    /**
     * Parse Multiline Literal String Value.
     *
     * @return Multiline String Literal Node.
     */
    private STNode parseMultilineLiteralStringValue() {
        STNode startingDoubleQuote = parseTripleSingleQuoteToken(ParserRuleContext.MULTILINE_LITERAL_STRING_START);
        STNode content = parseMultilineLiteralStringContent();
        STNode endingDoubleQuote = parseTripleSingleQuoteToken(ParserRuleContext.MULTILINE_LITERAL_STRING_END);
        return STNodeFactory.createLiteralStringLiteralNode(startingDoubleQuote, content, endingDoubleQuote);
    }

    private STNode parseMultilineLiteralStringContent() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.IDENTIFIER_LITERAL) {
            return consume();
        }
        if (nextToken.kind == TRIPLE_SINGLE_QUOTE_TOKEN) {
            return STNodeFactory.createEmptyNode();
        }
        recover(nextToken, ParserRuleContext.MULTILINE_LITERAL_STRING_CONTENT);
        return parseMultilineLiteralStringContent();
    }

    /**
     * Parse Single quote token.
     *
     * @return Single quote token
     */
    private STNode parseSingleQuoteToken(ParserRuleContext ctx) {
        STToken token = peek();
        if (token.kind == SINGLE_QUOTE_TOKEN) {
            return consume();
        } else {
            recover(token, ctx);
            return parseSingleQuoteToken(ctx);
        }
    }

    /**
     * Parse Single quote token.
     *
     * @return Single quote token
     */
    private STNode parseTripleSingleQuoteToken(ParserRuleContext ctx) {
        STToken token = peek();
        if (token.kind == TRIPLE_SINGLE_QUOTE_TOKEN) {
            return consume();
        } else {
            recover(token, ctx);
            return parseTripleSingleQuoteToken(ctx);
        }
    }

    /**
     * Parse Double quote token.
     *
     * @return Double quote token
     */
    private STNode parseDoubleQuoteToken(ParserRuleContext ctx) {
        STToken token = peek();
        if (token.kind == SyntaxKind.DOUBLE_QUOTE_TOKEN) {
            return consume();
        } else {
            recover(token, ctx);
            return parseDoubleQuoteToken(ctx);
        }
    }

    /**
     * Parse Triple Double quote token.
     *
     * @return Triple Double quote token
     */
    private STNode parseTripleDoubleQuoteToken(ParserRuleContext ctx) {
        STToken token = peek();
        if (token.kind == SyntaxKind.TRIPLE_DOUBLE_QUOTE_TOKEN) {
            return consume();
        } else {
            recover(token, ctx);
            return parseTripleDoubleQuoteToken(ctx);
        }
    }

    private STNode parseStringContent() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.IDENTIFIER_LITERAL) {
            return consume();
        }
        if (nextToken.kind == SyntaxKind.DOUBLE_QUOTE_TOKEN) {
            return STNodeFactory.createEmptyNode();
        }
        recover(nextToken, ParserRuleContext.STRING_CONTENT);
        return parseStringContent();
    }

    /**
     * Parsing Array Value. Array is surrounded by Single brackets.Array can contains any basic values and other arrays.
     * * Basic Values
     * * STRING_LITERAL_TOKEN (Single Line String) |
     * * ML_STRING_LITERAL_TOKEN (Multiline String) |
     * * DECIMAL_INT_TOKEN (Decimal Integer) |
     * * DECIMAL_FLOAT_TOKEN (Float) |
     * * TRUE/FALSE KEYWORD |
     * * MISSING_VALUE_TOKEN (Recovery purposes)
     * * Arrays
     *
     * @return ArrayNode
     */
    private STNode parseArray() {
        STNode openBracket = parseOpenBracket(ParserRuleContext.ARRAY_VALUE_LIST_START);
        STNode values = parseArrayValues();
        STNode closeBracket = parseCloseBracket(ParserRuleContext.ARRAY_VALUE_LIST_END);
        return STNodeFactory.createArrayNode(openBracket, values, closeBracket);
    }

    private STNode parseArrayValues() {
        startContext(ParserRuleContext.ARRAY_VALUE_LIST);
        STToken token = peek();

        if (isEndOfArray(token)) {
            STNode values = STNodeFactory.createEmptyNodeList();
            endContext();
            return values;
        }

        STNode firstValue = parseArrayValue();
        if (firstValue == null) {
            return STNodeFactory.createEmptyNodeList();
        }

        return parseArrayValues(firstValue);
    }

    private boolean isEndOfArray(STToken token) {
        return token.kind == SyntaxKind.CLOSE_BRACKET_TOKEN || token.kind == EOF_TOKEN;
    }

    private STNode parseArrayValues(STNode firstValue) {
        ArrayList<STNode> valuesList = new ArrayList<>();
        valuesList.add(firstValue);

        STToken nextToken = peek();
        while (!(isEndOfArray(nextToken))) {
            STNode valueEnd = parseValueEnd();
            if (valueEnd == null) {
                // null marks the end of values
                break;
            }

            STNode curValue = parseArrayValue();
            if (curValue != null) {
                valuesList.add(valueEnd);
                valuesList.add(curValue);
            }

            nextToken = peek();
        }
        endContext();
        return STNodeFactory.createNodeList(valuesList);
    }

    private STNode parseValueEnd() {
        switch (peek().kind) {
            case COMMA_TOKEN:
                return consume();
            case CLOSE_BRACKET_TOKEN:
                // null marks the end of values
                return null;
            case NEWLINE:
                consume();
                return parseValueEnd();
            default:
                recover(peek(), ParserRuleContext.ARRAY_VALUE_END);
                return parseValueEnd();
        }
    }

    private STNode parseArrayValue() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case DOUBLE_QUOTE_TOKEN:
                return parseStringValue();
            case TRIPLE_DOUBLE_QUOTE_TOKEN:
                return parseMultilineStringValue();
            case SINGLE_QUOTE_TOKEN:
                return parseLiteralStringValue();
            case TRIPLE_SINGLE_QUOTE_TOKEN:
                return parseMultilineLiteralStringValue();
            case DECIMAL_INT_TOKEN:
            case DECIMAL_FLOAT_TOKEN:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
                return parseValue();
            case OPEN_BRACKET_TOKEN:
                return parseArray();
            case CLOSE_BRACKET_TOKEN:
                return null;
            case NEWLINE:
                consume();
                return parseArrayValue();
            default:
                recover(peek(), ParserRuleContext.ARRAY_VALUE_START);
                return parseArrayValue();
        }
    }
}
