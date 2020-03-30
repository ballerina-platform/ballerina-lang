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

public class ReturnStatement extends Statement{
    private Token returnToken;
    private Node actionOrExpr;
    private Token semicolonToken;

    public ReturnStatement(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token returnToken() {
        if (returnToken != null) {
            return returnToken;
        }

        returnToken = createToken(0);
        return returnToken;
    }

    public Node actionOrExpr() {
        if (actionOrExpr != null) {
            return actionOrExpr;
        }

        actionOrExpr = createToken(1);
        return actionOrExpr;
    }

    public Token semicolonToken() {
        if (semicolonToken != null) {
            return semicolonToken;
        }

        semicolonToken = createToken(2);
        return semicolonToken;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return returnToken();
            case 1:
                return actionOrExpr();
            case 2:
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
