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

public class CompoundAssignmentOperator extends Statement {
    private Token binaryOperator;
    private Token equalsToken;

    public CompoundAssignmentOperator(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token binaryOperator() {
        if (binaryOperator != null) {
            return binaryOperator;
        }

        binaryOperator = createToken(0);
        return binaryOperator;
    }

    public Token equalsToken() {
        if (equalsToken != null) {
            return equalsToken;
        }

        equalsToken = createToken(1);
        return equalsToken;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return binaryOperator();
            case 1:
                return equalsToken();
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
