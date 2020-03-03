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

import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;

// function foo(int k) returns int = a => a;
// function foo(int k) returns int {}
// function foo(int k) returns int = extern
public class STFunctionDefinition extends STNode {
    // TODO metadata goes here
    public final STToken visibilityQual;
    public final STToken functionKeyword;
    public final STToken functionName;
    public final STToken openParenToken;
    public final STToken closeParenToken;
    public final STBlockStatement functionBody;

    public STFunctionDefinition(STToken visibilityQual,
                                STToken functionKeyword,
                                STToken functionName,
                                STToken openParanToken,
                                STToken closeParanToken,
                                STBlockStatement functionBody) {

        super(SyntaxKind.FUNCTION_DEFINITION);
        this.visibilityQual = visibilityQual;
        this.functionKeyword = functionKeyword;
        this.functionName = functionName;
        this.openParenToken = openParanToken;
        this.closeParenToken = closeParanToken;
        this.functionBody = functionBody;

        this.bucketCount = 6;
        this.childBuckets = new STNode[this.bucketCount];
        this.addChildNode(visibilityQual, 0);
        this.addChildNode(functionKeyword, 1);
        this.addChildNode(functionName, 2);
        this.addChildNode(openParanToken, 3);
        this.addChildNode(closeParanToken, 4);
        this.addChildNode(functionBody, 5);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new FunctionDefinitionNode(this, position, parent);
    }
}
