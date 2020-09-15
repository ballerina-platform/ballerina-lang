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

package internal.parser;

import internal.parser.tree.STLiteralValueToken;
import internal.parser.tree.STNode;
import internal.parser.tree.STNodeFactory;
import internal.parser.tree.STToken;
import syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.List;

import static syntax.tree.SyntaxKind.DOUBLE_OPEN_BRACKET_TOKEN;
import static syntax.tree.SyntaxKind.EOF_TOKEN;
import static syntax.tree.SyntaxKind.OPEN_BRACKET_TOKEN;

/**
 * A LL(k) recursive-descent parser for TOML.
 *
 * @since 1.2.0
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
        startContext(ParserRuleContext.TOP_LEVEL_NODE);
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
        endContext();

        return STNodeFactory.createModulePartNode(
                STNodeFactory.createNodeList(topLevelNodes), eof);
    }

    /**
     * Parse top level node given the next token kind and the modifier that precedes it.
     *
     * @return Parsed top-level node
     */
    private STNode parseTopLevelNode() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case EOF_TOKEN:
                return null;
            case OPEN_BRACKET_TOKEN:
                return parseTable();
            case DOUBLE_OPEN_BRACKET_TOKEN:
                return parseArrayOfTables();
            case UNQUOTED_KEY_TOKEN:
            case STRING_LITERAL:
            case BOOLEAN:
            case DEC_INT:
            case FLOAT://todo check +/- ?
                return parseKeyValue();
            default:
                STToken token = peek();
                recover(token, ParserRuleContext.TOP_LEVEL_NODE);

                return parseTopLevelNode();
        }
    }

    private STNode parseArrayOfTables() {
        startContext(ParserRuleContext.TOML_TABLE_ARRAY);
        STNode openBracket = parseDoubleOpenBracket();
        STNode identifierToken = parseKey();
        STNode closedBracket = parseDoubleCloseBracket();
        List<STNode> fields = parseKeyValues();
        endContext();
        return STNodeFactory.createTableArrayNode(openBracket,
                identifierToken, closedBracket, STNodeFactory.createNodeList(fields));
    }

    private STNode parseDoubleOpenBracket() {
        STToken token = peek();
        if (token.kind == SyntaxKind.DOUBLE_OPEN_BRACKET_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.DOUBLE_OPEN_BRACKET);
            return parseDoubleOpenBracket();
        }
    }

    private STNode parseDoubleCloseBracket() {
        STToken token = peek();
        if (token.kind == SyntaxKind.DOUBLE_CLOSE_BRACKET_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.DOUBLE_CLOSE_BRACKET);
            return parseDoubleCloseBracket();
        }
    }

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
        while (!(nextNode.kind == OPEN_BRACKET_TOKEN || nextNode.kind == DOUBLE_OPEN_BRACKET_TOKEN ||
                nextNode.kind == EOF_TOKEN)) {
            STNode stNode = parseKeyValue();
            fields.add(stNode);
            nextNode = peek();
        }
        return fields;
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

    private STNode parseKeyValue() {
        startContext(ParserRuleContext.KEY_VALUE_PAIR);
        STNode identifier = parseKey();
        STNode equals = parseEquals();
        STNode value = parseValue();
        endContext();
        return STNodeFactory.createKeyValue(identifier, equals, value);
    }

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
        if (token instanceof STLiteralValueToken) {
            if (token.kind == SyntaxKind.DEC_INT ||
                    token.kind == SyntaxKind.FLOAT) {
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

    private STNode parseValue() {
        STToken token = peek();
        if (isValue(token)) {
            return STNodeFactory.createBasicValueNode(token.kind, consume());
//            STToken consume = consume();
//            return STNodeFactory.createBasicValueNode(consume.kind,consume);
        } else if ((token.kind == OPEN_BRACKET_TOKEN)) {
            return parseArray();
        } else {
            recover(token, ParserRuleContext.VALUE);
            return parseValue();
        }
    }

    private STNode parseArray() {
        STNode openBracket = parseOpenBracket();
        STNode values = parseArrayValues();
        STNode closeBracket = parseCloseBracket();
        return STNodeFactory.createArray(openBracket, values, closeBracket);
    }

    private STNode parseArrayValues() {
        STToken token = peek();

        if (token.kind == SyntaxKind.CLOSE_BRACKET_TOKEN || token.kind == EOF_TOKEN) {
            STNode args = STNodeFactory.createEmptyNodeList();
            endContext();
            return args;
        }

        STNode firstArg = parseArgument();
        STNode argsList = parseArgList(firstArg);

        return argsList;
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
            argsList.add(argEnd);
            argsList.add(curArg);

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
        if (isValue(nextToken)) {
            return parseValue();
        } else if (nextToken.kind == OPEN_BRACKET_TOKEN) {
            return parseArray();
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

    private boolean isValue(STToken token) {
        return token.kind == SyntaxKind.STRING_LITERAL ||
                token.kind == SyntaxKind.ML_STRING_LITERAL ||
                token.kind == SyntaxKind.DEC_INT ||
                token.kind == SyntaxKind.FLOAT ||
                token.kind == SyntaxKind.BOOLEAN ||
                token.kind == SyntaxKind.BASIC_LITERAL;
    }
}
