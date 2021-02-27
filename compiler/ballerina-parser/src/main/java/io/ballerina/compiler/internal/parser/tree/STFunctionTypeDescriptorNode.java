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
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.FunctionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STFunctionTypeDescriptorNode extends STTypeDescriptorNode {
    public final STNode qualifierList;
    public final STNode functionKeyword;
    public final STNode functionSignature;

    STFunctionTypeDescriptorNode(
            STNode qualifierList,
            STNode functionKeyword,
            STNode functionSignature) {
        this(
                qualifierList,
                functionKeyword,
                functionSignature,
                Collections.emptyList());
    }

    STFunctionTypeDescriptorNode(
            STNode qualifierList,
            STNode functionKeyword,
            STNode functionSignature,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.FUNCTION_TYPE_DESC, diagnostics);
        this.qualifierList = qualifierList;
        this.functionKeyword = functionKeyword;
        this.functionSignature = functionSignature;

        addChildren(
                qualifierList,
                functionKeyword,
                functionSignature);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STFunctionTypeDescriptorNode(
                this.qualifierList,
                this.functionKeyword,
                this.functionSignature,
                diagnostics);
    }

    public STFunctionTypeDescriptorNode modify(
            STNode qualifierList,
            STNode functionKeyword,
            STNode functionSignature) {
        if (checkForReferenceEquality(
                qualifierList,
                functionKeyword,
                functionSignature)) {
            return this;
        }

        return new STFunctionTypeDescriptorNode(
                qualifierList,
                functionKeyword,
                functionSignature,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new FunctionTypeDescriptorNode(this, position, parent);
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
