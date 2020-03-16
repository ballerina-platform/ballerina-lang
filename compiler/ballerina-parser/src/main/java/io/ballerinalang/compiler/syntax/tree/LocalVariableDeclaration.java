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

public class LocalVariableDeclaration extends Statement {
    private Node typeName;
    private Token variableName;
    private Token equalsToken;
    private Node initializer;
    private Token semicolonToken;

    public LocalVariableDeclaration(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Node typeName() {
        if (typeName != null) {
            return typeName;
        }

        typeName = node.childInBucket(0).createFacade(getChildPosition(0), this);
        childBuckets[0] = typeName;
        return typeName;
    }

    public Token variableName() {
        if (variableName != null) {
            return variableName;
        }

        variableName = createToken(1);
        return variableName;
    }

    public Token equalsToken() {
        if (equalsToken != null) {
            return equalsToken;
        }

        equalsToken = createToken(2);
        return equalsToken;
    }

    public Node initializer() {
        if (initializer != null) {
            return initializer;
        }

        initializer = node.childInBucket(3).createFacade(getChildPosition(3), this);
        childBuckets[3] = initializer;
        return initializer;
    }

    public Token semicolonToken() {
        if (semicolonToken != null) {
            return semicolonToken;
        }

        semicolonToken = createToken(4);
        return semicolonToken;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return typeName();
            case 1:
                return variableName();
            case 2:
                return equalsToken();
            case 3:
                return initializer();
            case 4:
                return semicolonToken();
        }
        return null;
    }
}
