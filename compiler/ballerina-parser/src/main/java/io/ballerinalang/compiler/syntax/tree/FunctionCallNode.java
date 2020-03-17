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

public class FunctionCallNode extends ExpressionTree {

    private Node functionName;
    private Token openParenToken;
    private Node arguments;
    private Token closeParenToken;

    public FunctionCallNode(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Node functionName() {
        if (functionName != null) {
            return functionName;
        }

        functionName = node.childInBucket(0).createFacade(getChildPosition(0), this);
        childBuckets[0] = functionName;
        return functionName;
    }

    public Token openParenToken() {
        if (openParenToken != null) {
            return openParenToken;
        }

        openParenToken = createToken(1);
        return openParenToken;
    }

    public Node parameters() {
        if (arguments != null) {
            return arguments;
        }

        arguments = createListNode(2);
        return this.arguments;
    }

    public Token closeParenToken() {
        if (closeParenToken != null) {
            return closeParenToken;
        }

        closeParenToken = createToken(3);
        return closeParenToken;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return functionName();
            case 1:
                return openParenToken();
            case 2:
                return parameters();
            case 3:
                return closeParenToken();
        }
        return null;
    }
}
