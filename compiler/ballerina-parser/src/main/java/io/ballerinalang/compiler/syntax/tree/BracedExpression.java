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
 * Represents a braced expression in Ballerina.
 *
 * @since 1.3.0
 */
public class BracedExpression extends NonTerminalNode {
    private Token openParen;
    private Node expression;
    private Token closeParen;

    public BracedExpression(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token openParen() {
        if (openParen != null) {
            return openParen;
        }

        openParen = createToken(0);
        return openParen;
    }

    public Node expression() {
        if (expression != null) {
            return expression;
        }

        expression = node.childInBucket(1).createFacade(getChildPosition(1), this);
        childBuckets[1] = expression;
        return expression;
    }

    public Token closeParen() {
        if (closeParen != null) {
            return closeParen;
        }

        closeParen = createToken(2);
        return closeParen;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return openParen();
            case 1:
                return expression();
            case 2:
                return closeParen();
        }
        return null;
    }

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SyntaxNodeTransformer<T> transformer) {
        return transformer.transform(this);
    }
}
