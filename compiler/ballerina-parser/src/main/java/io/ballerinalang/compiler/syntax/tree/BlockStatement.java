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

public class BlockStatement extends NonTerminalNode {
    private Token openBraceToken;
    private NodeList<Statement> statements;
    private Token closeBraceToken;

    public BlockStatement(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token openBraceToken() {
        if (openBraceToken != null) {
            return openBraceToken;
        }

        openBraceToken = createToken(0);
        return openBraceToken;
    }

    public NodeList<Statement> statements() {
        if (statements != null) {
            return statements;
        }

        statements = createListNode(1);
        return statements;
    }

    public Token closeBraceToken() {
        if (closeBraceToken != null) {
            return closeBraceToken;
        }

        closeBraceToken = createToken(2);
        return closeBraceToken;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return openBraceToken();
            case 1:
                return statements();
            case 2:
                return closeBraceToken();
        }
        return null;
    }
}
