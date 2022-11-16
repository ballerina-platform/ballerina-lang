/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.GroupingKeyVarDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.4.0
 */
public class STGroupingKeyVarDeclarationNode extends STNode {
    public final STNode typeDescriptor;
    public final STNode simpleBindingPattern;
    public final STNode equalsToken;
    public final STNode expression;

    STGroupingKeyVarDeclarationNode(
            STNode typeDescriptor,
            STNode simpleBindingPattern,
            STNode equalsToken,
            STNode expression) {
        this(
                typeDescriptor,
                simpleBindingPattern,
                equalsToken,
                expression,
                Collections.emptyList());
    }

    STGroupingKeyVarDeclarationNode(
            STNode typeDescriptor,
            STNode simpleBindingPattern,
            STNode equalsToken,
            STNode expression,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.GROUPING_KEY_VAR_DECLARATION, diagnostics);
        this.typeDescriptor = typeDescriptor;
        this.simpleBindingPattern = simpleBindingPattern;
        this.equalsToken = equalsToken;
        this.expression = expression;

        addChildren(
                typeDescriptor,
                simpleBindingPattern,
                equalsToken,
                expression);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STGroupingKeyVarDeclarationNode(
                this.typeDescriptor,
                this.simpleBindingPattern,
                this.equalsToken,
                this.expression,
                diagnostics);
    }

    public STGroupingKeyVarDeclarationNode modify(
            STNode typeDescriptor,
            STNode simpleBindingPattern,
            STNode equalsToken,
            STNode expression) {
        if (checkForReferenceEquality(
                typeDescriptor,
                simpleBindingPattern,
                equalsToken,
                expression)) {
            return this;
        }

        return new STGroupingKeyVarDeclarationNode(
                typeDescriptor,
                simpleBindingPattern,
                equalsToken,
                expression,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new GroupingKeyVarDeclarationNode(this, position, parent);
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
