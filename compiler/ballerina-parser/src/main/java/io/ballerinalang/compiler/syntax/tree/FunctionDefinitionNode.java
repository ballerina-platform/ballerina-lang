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

public class FunctionDefinitionNode extends ModuleMemberDeclaration {

    private Token visibilityQual;
    private Token functionKeyword;
    private Token functionName;
    private Token openParenToken;
    private Token closeParenToken;
    private BlockStatement functionBody;

    public FunctionDefinitionNode(STNode node, int position, NonTerminalNode parent) {
        super(node, position, parent);
    }

    public Token visibilityQualifier() {
        if (visibilityQual != null) {
            return visibilityQual;
        }

        visibilityQual = createToken(0);
        return visibilityQual;
    }

    public Token functionKeyword() {
        if (functionKeyword != null) {
            return functionKeyword;
        }

        functionKeyword = createToken(1);
        return functionKeyword;
    }

    public Token functionName() {
        if (functionName != null) {
            return functionName;
        }

        functionName = createToken(2);
        return functionName;
    }

    public Token openParenToken() {
        if (openParenToken != null) {
            return openParenToken;
        }

        openParenToken = createToken(3);
        return openParenToken;
    }

    public Token closeParenToken() {
        if (closeParenToken != null) {
            return closeParenToken;
        }

        closeParenToken = createToken(4);
        return closeParenToken;
    }

    public BlockStatement functionBody() {
        if (functionBody != null) {
            return functionBody;
        }

        functionBody = (BlockStatement) node.childInBucket(5).createFacade(getChildPosition(5), this);
        childBuckets[5] = functionBody;
        return functionBody;
    }

    public Node childInBucket(int bucket) {
        switch (bucket) {
            case 0:
                return visibilityQualifier();
            case 1:
                return functionKeyword();
            case 2:
                return functionName();
            case 3:
                return openParenToken();
            case 4:
                return closeParenToken();
            case 5:
                return functionBody();
        }
        return null;
    }
}
