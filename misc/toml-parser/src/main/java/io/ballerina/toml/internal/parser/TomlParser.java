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
import static io.ballerina.toml.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.toml.syntax.tree.SyntaxKind.OPEN_BRACKET_TOKEN;

/**
 * A LL(k) recursive-descent parser for TOML.
 *
 * @since 0.1.0
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

        return STNodeFactory.createModulePartNode(
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
                return processOpenBracket();
            case UNQUOTED_KEY_TOKEN:
            case STRING_LITERAL:
            case BOOLEAN:
            case DEC_INT:
            case FLOAT:
                return parseKeyValue();
            default:
                STToken token = peek();
                recover(token, ParserRuleContext.TOP_LEVEL_NODE);

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
        STNode firstOpenBracket = parseOpenBracket();
        STNode secondOpenBracket = parseOpenBracket();
        STNode identifierToken = parseKey();
        STNode firstCloseBracket = parseCloseBracket();
        STNode secondCloesBracket = parseCloseBracket();
        List<STNode> fields = parseKeyValues();
        endContext();
        return STNodeFactory.createTableArrayNode(firstOpenBracket, secondOpenBracket,
                identifierToken, firstCloseBracket, secondCloesBracket, STNodeFactory.createNodeList(fields));
    }

    private STNode processOpenBracket() {
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
        STNode openBracket = parseOpenBracket();
        STNode identifierToken = parseKey();
        STNode closedBracket = parseCloseBracket();
        List<STNode> fields = parseKeyValues();
        endContext();
        return STNodeFactory.createTableNode(openBracket, identifierToken, closedBracket,
                STNodeFactory.createNodeList(fields));
    }

    private List<STNode> parseKeyValues() {
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

    private STNode parseOpenBracket() {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_BRACKET_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.OPEN_BRACKET);
            return parseOpenBracket();
        }
    }

    private STNode parseCloseBracket() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CLOSE_BRACKET_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.CLOSE_BRACKET);
            return parseCloseBracket();
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
        STNode identifier = parseKey();
        STNode equals = parseEquals();
        STNode value = parseValue();
        endContext();
        return STNodeFactory.createKeyValue(identifier, equals, value);
    }

    /**
     * Parses Key Node. A Key Node can be either one of the following forms
     * UNQUOTED_KEY_TOKEN (Regular Keys) |
     * BOOLEAN (True | False keyword) |
     * STRING_LITERAL (Quoted Keys)
     *
     * @return KeyNode
     */
    private STNode parseKey() {
        STToken token = peek();
        if (isKey(token)) {
            return consume();
        } else {
            recover(token, ParserRuleContext.KEY);
            return parseKey();
        }
    }

    public static boolean isKey(STToken token) {
        switch (token.kind) {
            case UNQUOTED_KEY_TOKEN:
            case BOOLEAN:
            case STRING_LITERAL:
                return true;
            default:
                return isNumberValidKey(token);
        }
    }

    public static boolean isNumberValidKey(STToken token) {
        if (isBasicValue(token)) {
            if (token.kind == SyntaxKind.DEC_INT || token.kind == SyntaxKind.FLOAT) {
                return !(token.text().startsWith("+") || token.text().startsWith("-"));
            }
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
     * DEC_INT (Decimal Integer) |
     * FLOAT (Float) |
     * BOOLEAN |
     * BASIC_LITERAL (Recovery purposes)
     * Arrays
     *
     * @return ValueNode
     */
    private STNode parseValue() {
        STToken token = peek();
        if (isBasicValue(token)) {
            return STNodeFactory.createBasicValueNode(token.kind, consume());
        } else if (token.kind == OPEN_BRACKET_TOKEN) {
            return parseArray();
        } else {
            recover(token, ParserRuleContext.VALUE);
            return parseValue();
        }
    }

    /**
     * Parsing Array Value. Array is surrounded by Single brackets.Array can contains any basic values and other arrays.
     * * Basic Values
     * * STRING_LITERAL (Single Line String) |
     * * ML_STRING_LITERAL (Multiline String) |
     * * DEC_INT (Decimal Integer) |
     * * FLOAT (Float) |
     * * BOOLEAN |
     * * BASIC_LITERAL (Recovery purposes)
     * * Arrays
     *
     * @return ArrayNode
     */
    private STNode parseArray() {
        startContext(ParserRuleContext.TOML_ARRAY);
        STNode openBracket = parseOpenBracket();
        STNode values = parseArrayValues();
        STNode closeBracket = parseCloseBracket();
        endContext();
        return STNodeFactory.createArray(openBracket, values, closeBracket);
    }

    private STNode parseArrayValues() {
        startContext(ParserRuleContext.ARG_LIST);
        STToken token = peek();

        if (token.kind == SyntaxKind.CLOSE_BRACKET_TOKEN || token.kind == EOF_TOKEN) {
            STNode args = STNodeFactory.createEmptyNodeList();
            endContext();
            return args;
        }

        STNode firstArg = parseArgument();
        if (firstArg == null) {
            return STNodeFactory.createEmptyNodeList();
        }

        return parseArgList(firstArg);
    }

    private STNode parseArgList(STNode firstArg) {
        ArrayList<STNode> argsList = new ArrayList<>();
        argsList.add(firstArg);

        STToken nextToken = peek();
        while (!(nextToken.kind == SyntaxKind.CLOSE_BRACKET_TOKEN || nextToken.kind == EOF_TOKEN)) {
            STNode argEnd = parseArgEnd();
            if (argEnd == null) {
                // null marks the end of args
                break;
            }

            STNode curArg = parseArgument();
            if (curArg != null) {
                argsList.add(argEnd);
                argsList.add(curArg);
            }

            nextToken = peek();
        }
        return STNodeFactory.createNodeList(argsList);
    }

    private STNode parseArgEnd() {
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACKET_TOKEN:
                // null marks the end of args
                return null;
            default:
                recover(peek(), ParserRuleContext.ARG_END);
                return parseArgEnd();
        }
    }

    private STNode parseArgument() {
        STToken nextToken = peek();
        if (isBasicValue(nextToken)) {
            return parseValue();
        } else if (nextToken.kind == OPEN_BRACKET_TOKEN) {
            return parseArray();
        } else if (nextToken.kind == CLOSE_BRACKET_TOKEN) {
            return null;
        } else {
            recover(peek(), ParserRuleContext.ARG_START);
            return parseArgument();
        }
    }

    private STNode parseComma() {
        STToken token = peek();
        if (token.kind == SyntaxKind.COMMA_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.COMMA);
            return parseEquals();
        }
    }

    private static boolean isBasicValue(STToken token) {
        return token.kind == SyntaxKind.STRING_LITERAL ||
                token.kind == SyntaxKind.ML_STRING_LITERAL ||
                token.kind == SyntaxKind.DEC_INT ||
                token.kind == SyntaxKind.FLOAT ||
                token.kind == SyntaxKind.BOOLEAN ||
                token.kind == SyntaxKind.BASIC_LITERAL;
    }
}
