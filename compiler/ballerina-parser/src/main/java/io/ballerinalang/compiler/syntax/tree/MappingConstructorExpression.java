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
public class MappingConstructorExpression extends NonTerminalNode {

    private Token openBrace;
    private Node fields;
    private Token closeBrace;

    public MappingConstructorExpression(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Node openBrace() {
        if (openBrace != null) {
            return openBrace;
        }

        openBrace = createToken(0);
        return openBrace;
    }

    public Node fields() {
        if (fields != null) {
            return fields;
        }

        fields = node.childInBucket(1).createFacade(getChildPosition(1), this);
        childBuckets[1] = fields;
        return fields;
    }

    public Token closeBrace() {
        if (closeBrace != null) {
            return closeBrace;
        }

        closeBrace = createToken(2);
        return this.closeBrace;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return openBrace();
            case 1:
                return fields();
            case 2:
                return closeBrace();
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
