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
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;

public class STMethodCallExpression extends STExpression {

    STNode expression;
    STNode dotToken;
    STNode methodName;
    STNode openParen;
    STNode arguments;
    STNode closeParen;

    public STMethodCallExpression(STNode expression,
                                  STNode dotToken,
                                  STNode methodName,
                                  STNode openParen,
                                  STNode arguments,
                                  STNode closeParen) {
        super(SyntaxKind.METHOD_CALL);
        this.expression = expression;
        this.dotToken = dotToken;
        this.methodName = methodName;
        this.openParen = openParen;
        this.arguments = arguments;
        this.closeParen = closeParen;

        this.bucketCount = 6;
        this.childBuckets = new STNode[this.bucketCount];
        this.addChildNode(expression, 0);
        this.addChildNode(dotToken, 1);
        this.addChildNode(methodName, 2);
        this.addChildNode(openParen, 3);
        this.addChildNode(arguments, 4);
        this.addChildNode(closeParen, 5);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return null;
    }
}
