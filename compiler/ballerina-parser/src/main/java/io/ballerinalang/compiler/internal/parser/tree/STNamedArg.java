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

import io.ballerinalang.compiler.syntax.tree.NamedArgumentNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;

public class STNamedArg extends STNode {

    public final STNode leadingComma;
    public final STNode argName;
    public final STNode equalsToken;
    public final STNode expression;

    public STNamedArg(STNode leadingComma,
                      STNode variableName,
                      STNode equalsToken,
                      STNode expression) {
        super(SyntaxKind.NAMED_ARG);
        this.leadingComma = leadingComma;
        this.argName = variableName;
        this.equalsToken = equalsToken;
        this.expression = expression;

        this.bucketCount = 4;
        this.childBuckets = new STNode[this.bucketCount];
        this.addChildNode(leadingComma, 0);
        this.addChildNode(variableName, 1);
        this.addChildNode(equalsToken, 2);
        this.addChildNode(expression, 3);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new NamedArgumentNode(this, position, parent);
    }
}
