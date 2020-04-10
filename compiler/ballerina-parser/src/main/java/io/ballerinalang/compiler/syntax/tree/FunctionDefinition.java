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
public class FunctionDefinition extends ModuleMemberDeclaration {

    public FunctionDefinition(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Metadata metadata() {
        return childInBucket(0);
    }

    public Token visibilityQualifier() {
        return childInBucket(1);
    }

    public Token functionKeyword() {
        return childInBucket(2);
    }

    public Identifier functionName() {
        return childInBucket(3);
    }

    public Token openParenToken() {
        return childInBucket(4);
    }

    public NodeList<Parameter> parameters() {
        return new NodeList<>(childInBucket(5));
    }

    public Token closeParenToken() {
        return childInBucket(6);
    }

    public Node returnTypeDesc() {
        return childInBucket(7);
    }

    public BlockStatement functionBody() {
        return childInBucket(8);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(NodeTransformer<T> visitor) {
        return visitor.transform(this);
    }

    public FunctionDefinition modify(
            Metadata metadata,
            Token visibilityQualifier,
            Token functionKeyword,
            Identifier functionName,
            Token openParenToken,
            NodeList<Parameter> parameters,
            Token closeParenToken,
            Node returnTypeDesc,
            BlockStatement functionBody) {
        if (checkForReferenceEquality(
                metadata,
                visibilityQualifier,
                functionKeyword,
                functionName,
                openParenToken,
                parameters.underlyingListNode(),
                closeParenToken,
                returnTypeDesc,
                functionBody)) {
            return this;
        }

        return NodeFactory.createFunctionDefinition(
                metadata,
                visibilityQualifier,
                functionKeyword,
                functionName,
                openParenToken,
                parameters,
                closeParenToken,
                returnTypeDesc,
                functionBody);
    }
}
