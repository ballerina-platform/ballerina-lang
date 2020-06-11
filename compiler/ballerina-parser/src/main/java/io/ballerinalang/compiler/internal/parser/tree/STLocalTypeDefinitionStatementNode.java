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

import io.ballerinalang.compiler.syntax.tree.LocalTypeDefinitionStatementNode;
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
public class STLocalTypeDefinitionStatementNode extends STStatementNode {
    public final STNode annotations;
    public final STNode typeKeyword;
    public final STNode typeName;
    public final STNode typeDescriptor;
    public final STNode semicolonToken;

    STLocalTypeDefinitionStatementNode(
            STNode annotations,
            STNode typeKeyword,
            STNode typeName,
            STNode typeDescriptor,
            STNode semicolonToken) {
        this(
                annotations,
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken,
                Collections.emptyList());
    }

    STLocalTypeDefinitionStatementNode(
            STNode annotations,
            STNode typeKeyword,
            STNode typeName,
            STNode typeDescriptor,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.LOCAL_TYPE_DEFINITION_STATEMENT, diagnostics);
        this.annotations = annotations;
        this.typeKeyword = typeKeyword;
        this.typeName = typeName;
        this.typeDescriptor = typeDescriptor;
        this.semicolonToken = semicolonToken;

        addChildren(
                annotations,
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STLocalTypeDefinitionStatementNode(
                this.annotations,
                this.typeKeyword,
                this.typeName,
                this.typeDescriptor,
                this.semicolonToken,
                diagnostics);
    }

    public STLocalTypeDefinitionStatementNode modify(
            STNode annotations,
            STNode typeKeyword,
            STNode typeName,
            STNode typeDescriptor,
            STNode semicolonToken) {
        if (checkForReferenceEquality(
                annotations,
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken)) {
            return this;
        }

        return new STLocalTypeDefinitionStatementNode(
                annotations,
                typeKeyword,
                typeName,
                typeDescriptor,
                semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new LocalTypeDefinitionStatementNode(this, position, parent);
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
