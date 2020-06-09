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

import io.ballerinalang.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STFunctionSignatureNode extends STNode {
    public final STNode openParenToken;
    public final STNode parameters;
    public final STNode closeParenToken;
    public final STNode returnTypeDesc;

    STFunctionSignatureNode(
            STNode openParenToken,
            STNode parameters,
            STNode closeParenToken,
            STNode returnTypeDesc) {
        this(
                openParenToken,
                parameters,
                closeParenToken,
                returnTypeDesc,
                Collections.emptyList());
    }

    STFunctionSignatureNode(
            STNode openParenToken,
            STNode parameters,
            STNode closeParenToken,
            STNode returnTypeDesc,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.FUNCTION_SIGNATURE, diagnostics);
        this.openParenToken = openParenToken;
        this.parameters = parameters;
        this.closeParenToken = closeParenToken;
        this.returnTypeDesc = returnTypeDesc;

        addChildren(
                openParenToken,
                parameters,
                closeParenToken,
                returnTypeDesc);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STFunctionSignatureNode(
                this.openParenToken,
                this.parameters,
                this.closeParenToken,
                this.returnTypeDesc,
                diagnostics);
    }

    public STFunctionSignatureNode modify(
            STNode openParenToken,
            STNode parameters,
            STNode closeParenToken,
            STNode returnTypeDesc) {
        if (checkForReferenceEquality(
                openParenToken,
                parameters,
                closeParenToken,
                returnTypeDesc)) {
            return this;
        }

        return new STFunctionSignatureNode(
                openParenToken,
                parameters,
                closeParenToken,
                returnTypeDesc,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new FunctionSignatureNode(this, position, parent);
    }

    @Override
    public void accept(STNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(STNodeTransformer<T> transformer) {
        return transformer.transform(this);
    }
}
