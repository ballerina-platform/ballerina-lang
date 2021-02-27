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
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class XMLNamePatternChainingNode extends NonTerminalNode {

    public XMLNamePatternChainingNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token startToken() {
        return childInBucket(0);
    }

    public SeparatedNodeList<Node> xmlNamePattern() {
        return new SeparatedNodeList<>(childInBucket(1));
    }

    public Token gtToken() {
        return childInBucket(2);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(NodeTransformer<T> visitor) {
        return visitor.transform(this);
    }

    @Override
    protected String[] childNames() {
        return new String[]{
                "startToken",
                "xmlNamePattern",
                "gtToken"};
    }

    public XMLNamePatternChainingNode modify(
            Token startToken,
            SeparatedNodeList<Node> xmlNamePattern,
            Token gtToken) {
        if (checkForReferenceEquality(
                startToken,
                xmlNamePattern.underlyingListNode(),
                gtToken)) {
            return this;
        }

        return NodeFactory.createXMLNamePatternChainingNode(
                startToken,
                xmlNamePattern,
                gtToken);
    }

    public XMLNamePatternChainingNodeModifier modify() {
        return new XMLNamePatternChainingNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLNamePatternChainingNodeModifier {
        private final XMLNamePatternChainingNode oldNode;
        private Token startToken;
        private SeparatedNodeList<Node> xmlNamePattern;
        private Token gtToken;

        public XMLNamePatternChainingNodeModifier(XMLNamePatternChainingNode oldNode) {
            this.oldNode = oldNode;
            this.startToken = oldNode.startToken();
            this.xmlNamePattern = oldNode.xmlNamePattern();
            this.gtToken = oldNode.gtToken();
        }

        public XMLNamePatternChainingNodeModifier withStartToken(
                Token startToken) {
            Objects.requireNonNull(startToken, "startToken must not be null");
            this.startToken = startToken;
            return this;
        }

        public XMLNamePatternChainingNodeModifier withXmlNamePattern(
                SeparatedNodeList<Node> xmlNamePattern) {
            Objects.requireNonNull(xmlNamePattern, "xmlNamePattern must not be null");
            this.xmlNamePattern = xmlNamePattern;
            return this;
        }

        public XMLNamePatternChainingNodeModifier withGtToken(
                Token gtToken) {
            Objects.requireNonNull(gtToken, "gtToken must not be null");
            this.gtToken = gtToken;
            return this;
        }

        public XMLNamePatternChainingNode apply() {
            return oldNode.modify(
                    startToken,
                    xmlNamePattern,
                    gtToken);
        }
    }
}
