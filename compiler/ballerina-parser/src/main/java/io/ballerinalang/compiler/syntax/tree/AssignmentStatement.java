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

public class AssignmentStatement extends Statement {
    private Token variableName;
    private Token equalsToken;
    private Node expression;
    private Token semicolonToken;

    public AssignmentStatement(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token variableName() {
        if (variableName != null) {
            return variableName;
        }

        variableName = createToken(0);
        return variableName;
    }

    public Token equalsToken() {
        if (equalsToken != null) {
            return equalsToken;
        }

        equalsToken = createToken(1);
        return equalsToken;
    }

    public Node expression() {
        if (expression != null) {
            return expression;
        }

        expression = node.childInBucket(2).createFacade(getChildPosition(2), this);
        childBuckets[2] = expression;
        return expression;
    }

    public Token semicolonToken() {
        if (semicolonToken != null) {
            return semicolonToken;
        }

        semicolonToken = createToken(3);
        return semicolonToken;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return variableName();
            case 1:
                return equalsToken();
            case 2:
                return expression();
            case 3:
                return semicolonToken();
        }
        return null;
    }

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SyntaxNodeTransformer<T> visitor) {
        return visitor.transform(this);
    }
}
