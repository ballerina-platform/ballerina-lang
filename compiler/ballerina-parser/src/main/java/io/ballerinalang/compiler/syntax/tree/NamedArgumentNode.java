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

public class NamedArgumentNode extends FunctionArgumentNode {

    private Token leadingComma;
    private Node argName;
    private Token equalsToken;
    private Node expression;

    public NamedArgumentNode(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token leadingComma() {
        if (leadingComma != null) {
            return leadingComma;
        }

        leadingComma = createToken(0);
        return leadingComma;
    }

    public Node argumentName() {
        if (argName != null) {
            return argName;
        }

        argName = node.childInBucket(1).createFacade(getChildPosition(1), this);
        childBuckets[1] = argName;
        return argName;
    }

    public Token equalsToken() {
        if (equalsToken != null) {
            return equalsToken;
        }

        equalsToken = createToken(2);
        return equalsToken;
    }

    public Node expression() {
        if (expression != null) {
            return expression;
        }

        expression = node.childInBucket(3).createFacade(getChildPosition(3), this);
        childBuckets[3] = expression;
        return this.expression;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return leadingComma();
            case 1:
                return argumentName();
            case 2:
                return equalsToken();
            case 3:
                return expression();
        }
        return null;
    }
}
