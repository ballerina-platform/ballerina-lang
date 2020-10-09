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

import static io.ballerina.toml.syntax.tree.SyntaxKind.CLOSE_BRACKET_TOKEN;
import static io.ballerina.toml.syntax.tree.SyntaxKind.DEC_INT;
import static io.ballerina.toml.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.toml.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.toml.syntax.tree.SyntaxKind.FLOAT;
import static io.ballerina.toml.syntax.tree.SyntaxKind.OPEN_BRACKET_TOKEN;
import static io.ballerina.toml.syntax.tree.SyntaxKind.SINGLE_QUOTE_TOKEN;
import static io.ballerina.toml.syntax.tree.SyntaxKind.STRING_LITERAL;

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
            case OPEN_BRACKET_TOKEN:
                return parseTableOrArrays();
            case IDENTIFIER_LITERAL:
            case SINGLE_QUOTE_TOKEN:
            case DOUBLE_QUOTE_TOKEN:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_INT_TOKEN:
            case DECIMAL_FLOAT_TOKEN:
                return parseKeyValue();
            default:
                recover(nextToken, ParserRuleContext.TOP_LEVEL_NODE);

                return parseTopLevelNode();
        }
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
        List<STNode> fields = parseTableEntries();
        endContext();
        return STNodeFactory.createTableArrayNode(firstOpenBracket, secondOpenBracket,
                identifierToken, firstCloseBracket, secondCloesBracket, STNodeFactory.createNodeList(fields));
    }

    private STNode parseTableOrArrays() {
        STToken token = peek(2);
        if (token.kind == OPEN_BRACKET_TOKEN) {
            return parseArrayOfTables();
        } else {
            return parseTable();
        }
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
        endContext();
        return STNodeFactory.createKeyValueNode(identifier, equals, value);
    }

    /**
     * Parses Key Node. A Key Node can be either one or many the following forms
     * UNQUOTED_KEY_TOKEN (Regular Keys) |
     * TRUE/FALSE KEYWORD |
     * STRING_LITERAL (Quoted Keys)
     *
     * @return KeyNodeList
     */
    private STNode parseKeys() {
        startContext(ParserRuleContext.KEY_LIST);
        STToken token = peek();

        if (isEndOfKeyList(token)) {
            STNode values = STNodeFactory.createEmptyNodeList();
            endContext();
            return values;
        }

        STNode firstKey = parseSingleKey();
        if (firstKey == null) {
            return STNodeFactory.createEmptyNodeList();
        }

        return parseKeyList(firstKey);
    }

    private boolean isEndOfKeyList(STToken token) {
        return token.kind == EQUAL_TOKEN || token.kind == EOF_TOKEN || token.kind == CLOSE_BRACKET_TOKEN; //TODO revisit
    }

    private STNode parseSingleKey() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case DECIMAL_INT_TOKEN:
                return STNodeFactory.createNumericLiteralNode(DEC_INT, consume());
            case DECIMAL_FLOAT_TOKEN: //TODO signed numeric
                return STNodeFactory.createNumericLiteralNode(FLOAT, consume());
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
                return STNodeFactory.createBoolLiteralNode(consume());
            case IDENTIFIER_LITERAL:
                return STNodeFactory.createIdentifierLiteralNode(consume());
            case DOUBLE_QUOTE_TOKEN:
            case TRIPLE_DOUBLE_QUOTE_TOKEN:
            case SINGLE_QUOTE_TOKEN:
                return parseStringValue();
            case EQUAL_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.KEY_ENTRY);
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
        switch (peek().kind) {
            case DOT_TOKEN:
                return parseDot();
            case DOUBLE_QUOTE_TOKEN:
            case SINGLE_QUOTE_TOKEN:
                // null marks the end of values
                return null;
            default:
                recover(peek(), ParserRuleContext.KEYS_END);
                return parseValueEnd();
        }
    }

    public static boolean isKey(STToken token) { //change in error handler
        switch (token.kind) {
            case IDENTIFIER_LITERAL:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
                return true;
            default:
                return isNumberValidKey(token);
        }
    }

    public static boolean isNumberValidKey(STToken token) {
        if (token.kind == SyntaxKind.DECIMAL_INT_TOKEN || token.kind == SyntaxKind.DECIMAL_FLOAT_TOKEN) {
            return !(token.text().startsWith("+") || token.text().startsWith("-"));
        }
        return false;
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
     * STRING_LITERAL (Single Line String) |
     * ML_STRING_LITERAL (Multiline String) |
     * DECIMAL_INT_TOKEN (Decimal Integer) |
     * DECIMAL_FLOAT_TOKEN (Float) |
     * BOOLEAN |
     * BASIC_LITERAL (Recovery purposes)
     * Arrays
     *
     * @return ValueNode
     */
    private STNode parseValue() {
        STToken token = peek();
        switch (token.kind) {
            case DOUBLE_QUOTE_TOKEN:
            case TRIPLE_DOUBLE_QUOTE_TOKEN:
                return parseStringValue();
            case DECIMAL_INT_TOKEN:
                return STNodeFactory.createNumericLiteralNode(DEC_INT, consume());
            case DECIMAL_FLOAT_TOKEN:
                return STNodeFactory.createNumericLiteralNode(FLOAT, consume());
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
                return STNodeFactory.createBoolLiteralNode(consume());
            case OPEN_BRACKET_TOKEN:
                return parseArray();
            default:
                recover(token, ParserRuleContext.VALUE);
                return parseValue();
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
     * Parse Double quote token.
     *
     * @return Double quote token
     */
    private STNode parseDoubleQuoteToken(ParserRuleContext ctx) {
        STToken token = peek();
        if (token.kind == SyntaxKind.DOUBLE_QUOTE_TOKEN || token.kind == SyntaxKind.TRIPLE_DOUBLE_QUOTE_TOKEN
                || token.kind == SINGLE_QUOTE_TOKEN) {
            return consume();
        } else {
            recover(token, ctx);
            return parseDoubleQuoteToken(ctx);
        }
    }

    private STNode parseStringContent() {
        STToken nextToken = peek();
        if (nextToken.kind == STRING_LITERAL) {
          return consume();
        } else {
            recover(nextToken, ParserRuleContext.STRING_LITERAL);
            return parseStringContent();
        }
    }


    /**
     * Parsing Array Value. Array is surrounded by Single brackets.Array can contains any basic values and other arrays.
     * * Basic Values
     * * STRING_LITERAL (Single Line String) |
     * * ML_STRING_LITERAL (Multiline String) |
     * * DECIMAL_INT_TOKEN (Decimal Integer) |
     * * DECIMAL_FLOAT_TOKEN (Float) |
     * * TRUE/FALSE KEYWORD |
     * * BASIC_LITERAL (Recovery purposes)
     * * Arrays
     *
     * @return ArrayNode
     */
    private STNode parseArray() {
        startContext(ParserRuleContext.TOML_ARRAY);
        STNode openBracket = parseOpenBracket(ParserRuleContext.ARRAY_VALUE_LIST_START);
        STNode values = parseArrayValues();
        STNode closeBracket = parseCloseBracket(ParserRuleContext.ARRAY_VALUE_LIST_END);
        endContext();
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
                return parseComma();
            case CLOSE_BRACKET_TOKEN:
                // null marks the end of values
                return null;
            default:
                recover(peek(), ParserRuleContext.ARRAY_VALUE_END);
                return parseValueEnd();
        }
    }

    private STNode parseArrayValue() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.DOUBLE_QUOTE_TOKEN || nextToken.kind == SyntaxKind.TRIPLE_DOUBLE_QUOTE_TOKEN) {
            return parseStringValue();
        } else if (isBasicValue(nextToken)) {
            return parseValue();
        } else if (nextToken.kind == OPEN_BRACKET_TOKEN) {
            return parseArray();
        } else if (nextToken.kind == CLOSE_BRACKET_TOKEN) {
            return null;
        } else {
            recover(peek(), ParserRuleContext.ARRAY_VALUE_START);
            return parseArrayValue();
        }
    }

    private STNode parseDot() {
        STToken token = peek();
        if (token.kind == SyntaxKind.DOT_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.DOT);
            return parseDot();
        }
    }

    private STNode parseComma() {
        STToken token = peek();
        if (token.kind == SyntaxKind.COMMA_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.COMMA);
            return parseComma();
        }
    }

    private static boolean isBasicValue(STToken token) {
        return token.kind == SyntaxKind.DECIMAL_INT_TOKEN ||
                token.kind == SyntaxKind.DECIMAL_FLOAT_TOKEN ||
                token.kind == SyntaxKind.TRUE_KEYWORD ||
                token.kind == SyntaxKind.FALSE_KEYWORD ||
                token.kind == SyntaxKind.BASIC_LITERAL;
    }
}
