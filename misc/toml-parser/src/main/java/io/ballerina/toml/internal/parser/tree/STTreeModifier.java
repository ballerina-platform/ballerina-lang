/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.toml.internal.parser.tree;


/**
 * Produces a new tree by doing a depth-first traversal of the internal tree.
 * <p>
 * This is a generated class.
 *
 * @since 2.0.0
 */
public abstract class STTreeModifier extends STNodeTransformer<STNode> {

    @Override
    public STDocumentNode transform(
            STDocumentNode documentNode) {
        STNode members = modifyNode(documentNode.members);
        STNode eofToken = modifyNode(documentNode.eofToken);
        return documentNode.modify(
                members,
                eofToken);
    }

    @Override
    public STTableNode transform(
            STTableNode tableNode) {
        STNode openBracket = modifyNode(tableNode.openBracket);
        STNode identifier = modifyNode(tableNode.identifier);
        STNode closeBracket = modifyNode(tableNode.closeBracket);
        STNode fields = modifyNode(tableNode.fields);
        return tableNode.modify(
                openBracket,
                identifier,
                closeBracket,
                fields);
    }

    @Override
    public STTableArrayNode transform(
            STTableArrayNode tableArrayNode) {
        STNode firstOpenBracket = modifyNode(tableArrayNode.firstOpenBracket);
        STNode secondOpenBracket = modifyNode(tableArrayNode.secondOpenBracket);
        STNode identifier = modifyNode(tableArrayNode.identifier);
        STNode firstCloseBracket = modifyNode(tableArrayNode.firstCloseBracket);
        STNode secondCloseBracket = modifyNode(tableArrayNode.secondCloseBracket);
        STNode fields = modifyNode(tableArrayNode.fields);
        return tableArrayNode.modify(
                firstOpenBracket,
                secondOpenBracket,
                identifier,
                firstCloseBracket,
                secondCloseBracket,
                fields);
    }

    @Override
    public STKeyValueNode transform(
            STKeyValueNode keyValueNode) {
        STNode identifier = modifyNode(keyValueNode.identifier);
        STNode assign = modifyNode(keyValueNode.assign);
        STNode value = modifyNode(keyValueNode.value);
        return keyValueNode.modify(
                identifier,
                assign,
                value);
    }

    @Override
    public STArrayNode transform(
            STArrayNode arrayNode) {
        STNode openBracket = modifyNode(arrayNode.openBracket);
        STNode values = modifyNode(arrayNode.values);
        STNode closeBracket = modifyNode(arrayNode.closeBracket);
        return arrayNode.modify(
                openBracket,
                values,
                closeBracket);
    }

    @Override
    public STStringLiteralNode transform(
            STStringLiteralNode stringLiteralNode) {
        STNode startDoubleQuote = modifyNode(stringLiteralNode.startDoubleQuote);
        STNode content = modifyNode(stringLiteralNode.content);
        STNode endDoubleQuote = modifyNode(stringLiteralNode.endDoubleQuote);
        return stringLiteralNode.modify(
                startDoubleQuote,
                content,
                endDoubleQuote);
    }

    @Override
    public STNumericLiteralNode transform(
            STNumericLiteralNode numericLiteralNode) {
        STNode value = modifyNode(numericLiteralNode.value);
        return numericLiteralNode.modify(
                numericLiteralNode.kind,
                value);
    }

    @Override
    public STBoolLiteralNode transform(
            STBoolLiteralNode boolLiteralNode) {
        STNode value = modifyNode(boolLiteralNode.value);
        return boolLiteralNode.modify(
                value);
    }

    @Override
    public STIdentifierLiteralNode transform(
            STIdentifierLiteralNode identifierLiteralNode) {
        STNode value = modifyNode(identifierLiteralNode.value);
        return identifierLiteralNode.modify(
                value);
    }

    // Tokens

    public STToken transform(STToken token) {
        return token;
    }

    public STIdentifierToken transform(STIdentifierToken identifier) {
        return identifier;
    }

    public STLiteralValueToken transform(STLiteralValueToken literalValueToken) {
        return literalValueToken;
    }

    public STDocumentationLineToken transform(STDocumentationLineToken documentationLineToken) {
        return documentationLineToken;
    }

    public STMissingToken transform(STMissingToken missingToken) {
        return missingToken;
    }

    // Misc

    public STNode transform(STNodeList nodeList) {
        return transformSyntaxNode(nodeList);
    }

    @Override
    protected STNode transformSyntaxNode(STNode node) {
        return node;
    }

    protected <T extends STNode> T modifyNode(T node) {
        if (node == null) {
            return null;
        }
        // TODO
        return (T) node.apply(this);
    }
}

