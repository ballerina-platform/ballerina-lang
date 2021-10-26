/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.toml.validator;

import io.ballerina.toml.syntax.tree.AbstractNodeFactory;
import io.ballerina.toml.syntax.tree.BoolLiteralNode;
import io.ballerina.toml.syntax.tree.KeyNode;
import io.ballerina.toml.syntax.tree.KeyValueNode;
import io.ballerina.toml.syntax.tree.Minutiae;
import io.ballerina.toml.syntax.tree.MinutiaeList;
import io.ballerina.toml.syntax.tree.NodeFactory;
import io.ballerina.toml.syntax.tree.NumericLiteralNode;
import io.ballerina.toml.syntax.tree.StringLiteralNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.TableArrayNode;
import io.ballerina.toml.syntax.tree.TableNode;
import io.ballerina.toml.syntax.tree.Token;

/**
 * Responsible for generating sample toml syntax tree nodes for BoilerplateGenerator.
 *
 * @since v2.0.0
 */
public class SampleNodeGenerator {

    public static KeyValueNode createStringKV(String key, String value, String description) {
        KeyNode keyNode = getKeyNode(key);
        Token assign = getAssignToken();
        Token startingDoubleQuote = NodeFactory.createToken(SyntaxKind.DOUBLE_QUOTE_TOKEN);
        MinutiaeList endingMinList = getEndingMinList(description);
        Token endingDoubleQuote = NodeFactory
                .createToken(SyntaxKind.DOUBLE_QUOTE_TOKEN, NodeFactory.createEmptyMinutiaeList(), endingMinList);
        StringLiteralNode stringLiteralNode =
                NodeFactory.createStringLiteralNode(startingDoubleQuote, NodeFactory.createIdentifierToken(value),
                        endingDoubleQuote);
        return NodeFactory.createKeyValueNode(keyNode, assign, stringLiteralNode);
    }

    public static KeyValueNode createNumericKV(String key, String value, String description) {
        KeyNode keyNode = getKeyNode(key);
        Token assign = getAssignToken();
        MinutiaeList endingMinList = getEndingMinList(description);
        NumericLiteralNode numericLiteralNode = NodeFactory.createNumericLiteralNode(SyntaxKind.FLOAT, null,
                NodeFactory.createIdentifierToken(value, NodeFactory.createEmptyMinutiaeList(), endingMinList));
        return NodeFactory.createKeyValueNode(keyNode, assign, numericLiteralNode);
    }

    public static KeyValueNode createBooleanKV(String key, Boolean value, String description) {
        KeyNode keyNode = getKeyNode(key);
        Token assign = getAssignToken();
        MinutiaeList endingMinList = getEndingMinList(description);
        SyntaxKind kind;
        if (value) {
            kind = SyntaxKind.TRUE_KEYWORD;
        } else {
            kind = SyntaxKind.FALSE_KEYWORD;
        }

        BoolLiteralNode boolLiteralNode = NodeFactory.createBoolLiteralNode(
                NodeFactory.createToken(kind, NodeFactory.createEmptyMinutiaeList(), endingMinList));
        return NodeFactory.createKeyValueNode(keyNode, assign, boolLiteralNode);
    }

    public static TableNode createTable(String key, String description) {
        KeyNode keyNode = getKeyNode(key);
        return NodeFactory.createTableNode(NodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN,
                getLeadingTableMinList(description), NodeFactory.createEmptyMinutiaeList()), keyNode,
                NodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN, AbstractNodeFactory.createEmptyMinutiaeList()
                        , getEndingMinList(null)), NodeFactory.createEmptyNodeList());
    }

    public static TableArrayNode createTableArray(String key, String description) {
        KeyNode keyNode = getKeyNode(key);
        return NodeFactory.createTableArrayNode(NodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN,
                getLeadingTableMinList(description), NodeFactory.createEmptyMinutiaeList()),
                NodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN), keyNode,
                NodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN),
                NodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN, AbstractNodeFactory.createEmptyMinutiaeList()
                        , getEndingMinList(null)), NodeFactory.createEmptyNodeList());
    }

    private static MinutiaeList getLeadingTableMinList(String comment) {
        MinutiaeList minutiaeList = NodeFactory.createMinutiaeList();
        if (comment != null) {
            minutiaeList = minutiaeList.add(NodeFactory.createCommentMinutiae(" # " + comment));
            minutiaeList = minutiaeList.add(NodeFactory.createWhitespaceMinutiae("\n"));
        }
        return minutiaeList;
    }

    private static MinutiaeList getEndingMinList(String comment) {
        Minutiae newLineMinutiae = NodeFactory.createWhitespaceMinutiae("\n");
        MinutiaeList endingMinList = NodeFactory.createMinutiaeList();
        if (comment != null) {
            endingMinList = endingMinList.add(NodeFactory.createCommentMinutiae(" # " + comment));
        }
        endingMinList = endingMinList.add(newLineMinutiae);
        return endingMinList;
    }

    private static Token getAssignToken() {
        MinutiaeList whitespaceMinList = NodeFactory.createMinutiaeList(NodeFactory.createWhitespaceMinutiae(" "));
        return NodeFactory.createToken(SyntaxKind.EQUAL_TOKEN, whitespaceMinList, whitespaceMinList);
    }

    private static KeyNode getKeyNode(String key) {
        return NodeFactory.createKeyNode(NodeFactory.createSeparatedNodeList(NodeFactory.createIdentifierToken(key)));
    }
}
