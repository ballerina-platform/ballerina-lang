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

import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
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
public class STModuleVariableDeclarationNode extends STModuleMemberDeclarationNode {
    public final STNode metadata;
    public final STNode finalKeyword;
    public final STNode typedBindingPattern;
    public final STNode equalsToken;
    public final STNode initializer;
    public final STNode semicolonToken;

    STModuleVariableDeclarationNode(
            STNode metadata,
            STNode finalKeyword,
            STNode typedBindingPattern,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {
        this(
                metadata,
                finalKeyword,
                typedBindingPattern,
                equalsToken,
                initializer,
                semicolonToken,
                Collections.emptyList());
    }

    STModuleVariableDeclarationNode(
            STNode metadata,
            STNode finalKeyword,
            STNode typedBindingPattern,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.MODULE_VAR_DECL, diagnostics);
        this.metadata = metadata;
        this.finalKeyword = finalKeyword;
        this.typedBindingPattern = typedBindingPattern;
        this.equalsToken = equalsToken;
        this.initializer = initializer;
        this.semicolonToken = semicolonToken;

        addChildren(
                metadata,
                finalKeyword,
                typedBindingPattern,
                equalsToken,
                initializer,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STModuleVariableDeclarationNode(
                this.metadata,
                this.finalKeyword,
                this.typedBindingPattern,
                this.equalsToken,
                this.initializer,
                this.semicolonToken,
                diagnostics);
    }

    public STModuleVariableDeclarationNode modify(
            STNode metadata,
            STNode finalKeyword,
            STNode typedBindingPattern,
            STNode equalsToken,
            STNode initializer,
            STNode semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                finalKeyword,
                typedBindingPattern,
                equalsToken,
                initializer,
                semicolonToken)) {
            return this;
        }

        return new STModuleVariableDeclarationNode(
                metadata,
                finalKeyword,
                typedBindingPattern,
                equalsToken,
                initializer,
                semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ModuleVariableDeclarationNode(this, position, parent);
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
