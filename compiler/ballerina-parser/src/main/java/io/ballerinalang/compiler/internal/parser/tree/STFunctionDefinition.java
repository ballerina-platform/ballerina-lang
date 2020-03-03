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
    public final STNode visibilityQual;
    public final STNode functionKeyword;
    public final STNode functionName;
    public final STNode openParenToken;
    public final STNode parameters;
    public final STNode closeParenToken;
    public final STNode returnTypeDesc; 
    public final STNode functionBody;

    public STFunctionDefinition(STNode visibilityQual,
                                STNode functionKeyword,
                                STNode functionName,
                                STNode openParanToken,
                                STNode parameters,
                                STNode closeParanToken,
                                STNode returnTypeDesc,
                                STNode functionBody) {

        super(SyntaxKind.FUNCTION_DEFINITION);
        this.visibilityQual = visibilityQual;
        this.functionKeyword = functionKeyword;
        this.functionName = functionName;
        this.openParenToken = openParanToken;
        this.parameters = parameters;
        this.closeParenToken = closeParanToken;
        this.returnTypeDesc = returnTypeDesc;
        this.functionBody = functionBody;

        this.bucketCount = 8;
        this.childBuckets = new STNode[this.bucketCount];
        this.addChildNode(visibilityQual, 0);
        this.addChildNode(functionKeyword, 1);
        this.addChildNode(functionName, 2);
        this.addChildNode(openParanToken, 3);
        this.addChildNode(parameters, 4);
        this.addChildNode(closeParanToken, 5);
        this.addChildNode(returnTypeDesc, 6);
        this.addChildNode(functionBody, 7);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new FunctionDefinitionNode(this, position, parent);
    }
}
