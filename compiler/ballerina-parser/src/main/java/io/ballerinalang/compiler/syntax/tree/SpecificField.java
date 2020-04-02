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
 * @since 1.3.0
 */
public class SpecificField extends NonTerminalNode {

    private Token leadingComma;
    private Node fieldName;
    private Token colonToken;
    private Node valueExpr;

    public SpecificField(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token leadingComma() {
        if (leadingComma != null) {
            return leadingComma;
        }

        leadingComma = createToken(0);
        return leadingComma;
    }

    public Node fieldName() {
        if (fieldName != null) {
            return fieldName;
        }

        fieldName = node.childInBucket(1).createFacade(getChildPosition(1), this);
        childBuckets[1] = fieldName;
        return fieldName;
    }

    public Token colonToken() {
        if (colonToken != null) {
            return colonToken;
        }

        colonToken = createToken(2);
        return colonToken;
    }

    public Node valueExpr() {
        if (valueExpr != null) {
            return valueExpr;
        }

        valueExpr = node.childInBucket(3).createFacade(getChildPosition(3), this);
        childBuckets[3] = valueExpr;
        return valueExpr;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return leadingComma();
            case 1:
                return fieldName();
            case 2:
                return colonToken();
            case 3:
                return valueExpr();
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
