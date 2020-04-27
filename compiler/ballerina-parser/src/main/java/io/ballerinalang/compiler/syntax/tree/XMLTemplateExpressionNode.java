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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;

/**
 * This is a generated syntax tree node.
 *
 * @since 1.3.0
 */
public class XMLTemplateExpressionNode extends ExpressionNode {

    public XMLTemplateExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token xmlKeyword() {
        return childInBucket(0);
    }

    public Token startBacktick() {
        return childInBucket(1);
    }

    public NodeList<XMLItemNode> content() {
        return new NodeList<>(childInBucket(2));
    }

    public Token endBacktick() {
        return childInBucket(3);
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
                "xmlKeyword",
                "startBacktick",
                "content",
                "endBacktick"};
    }

    public XMLTemplateExpressionNode modify(
            Token xmlKeyword,
            Token startBacktick,
            NodeList<XMLItemNode> content,
            Token endBacktick) {
        if (checkForReferenceEquality(
                xmlKeyword,
                startBacktick,
                content.underlyingListNode(),
                endBacktick)) {
            return this;
        }

        return NodeFactory.createXMLTemplateExpressionNode(
                xmlKeyword,
                startBacktick,
                content,
                endBacktick);
    }
}
