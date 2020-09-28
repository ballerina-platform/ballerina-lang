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
import io.ballerinalang.compiler.syntax.tree.ResourceAccessorDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STResourceAccessorDefinitionNode extends STNode {
    public final STNode metadata;
    public final STNode resourceKeyword;
    public final STNode functionKeyword;
    public final STNode accessorName;
    public final STNode relativeResourcePath;
    public final STNode functionSignature;
    public final STNode functionBody;

    STResourceAccessorDefinitionNode(
            STNode metadata,
            STNode resourceKeyword,
            STNode functionKeyword,
            STNode accessorName,
            STNode relativeResourcePath,
            STNode functionSignature,
            STNode functionBody) {
        this(
                metadata,
                resourceKeyword,
                functionKeyword,
                accessorName,
                relativeResourcePath,
                functionSignature,
                functionBody,
                Collections.emptyList());
    }

    STResourceAccessorDefinitionNode(
            STNode metadata,
            STNode resourceKeyword,
            STNode functionKeyword,
            STNode accessorName,
            STNode relativeResourcePath,
            STNode functionSignature,
            STNode functionBody,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION, diagnostics);
        this.metadata = metadata;
        this.resourceKeyword = resourceKeyword;
        this.functionKeyword = functionKeyword;
        this.accessorName = accessorName;
        this.relativeResourcePath = relativeResourcePath;
        this.functionSignature = functionSignature;
        this.functionBody = functionBody;

        addChildren(
                metadata,
                resourceKeyword,
                functionKeyword,
                accessorName,
                relativeResourcePath,
                functionSignature,
                functionBody);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STResourceAccessorDefinitionNode(
                this.metadata,
                this.resourceKeyword,
                this.functionKeyword,
                this.accessorName,
                this.relativeResourcePath,
                this.functionSignature,
                this.functionBody,
                diagnostics);
    }

    public STResourceAccessorDefinitionNode modify(
            STNode metadata,
            STNode resourceKeyword,
            STNode functionKeyword,
            STNode accessorName,
            STNode relativeResourcePath,
            STNode functionSignature,
            STNode functionBody) {
        if (checkForReferenceEquality(
                metadata,
                resourceKeyword,
                functionKeyword,
                accessorName,
                relativeResourcePath,
                functionSignature,
                functionBody)) {
            return this;
        }

        return new STResourceAccessorDefinitionNode(
                metadata,
                resourceKeyword,
                functionKeyword,
                accessorName,
                relativeResourcePath,
                functionSignature,
                functionBody,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ResourceAccessorDefinitionNode(this, position, parent);
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
