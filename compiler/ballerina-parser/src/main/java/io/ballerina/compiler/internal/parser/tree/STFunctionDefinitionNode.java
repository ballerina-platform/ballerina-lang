/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
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
public class STFunctionDefinitionNode extends STModuleMemberDeclarationNode {
    public final STNode metadata;
    public final STNode qualifierList;
    public final STNode functionKeyword;
    public final STNode functionName;
    public final STNode relativeResourcePath;
    public final STNode functionSignature;
    public final STNode functionBody;

    STFunctionDefinitionNode(
            SyntaxKind kind,
            STNode metadata,
            STNode qualifierList,
            STNode functionKeyword,
            STNode functionName,
            STNode relativeResourcePath,
            STNode functionSignature,
            STNode functionBody) {
        this(
                kind,
                metadata,
                qualifierList,
                functionKeyword,
                functionName,
                relativeResourcePath,
                functionSignature,
                functionBody,
                Collections.emptyList());
    }

    STFunctionDefinitionNode(
            SyntaxKind kind,
            STNode metadata,
            STNode qualifierList,
            STNode functionKeyword,
            STNode functionName,
            STNode relativeResourcePath,
            STNode functionSignature,
            STNode functionBody,
            Collection<STNodeDiagnostic> diagnostics) {
        super(kind, diagnostics);
        this.metadata = metadata;
        this.qualifierList = qualifierList;
        this.functionKeyword = functionKeyword;
        this.functionName = functionName;
        this.relativeResourcePath = relativeResourcePath;
        this.functionSignature = functionSignature;
        this.functionBody = functionBody;

        addChildren(
                metadata,
                qualifierList,
                functionKeyword,
                functionName,
                relativeResourcePath,
                functionSignature,
                functionBody);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STFunctionDefinitionNode(
                this.kind,
                this.metadata,
                this.qualifierList,
                this.functionKeyword,
                this.functionName,
                this.relativeResourcePath,
                this.functionSignature,
                this.functionBody,
                diagnostics);
    }

    public STFunctionDefinitionNode modify(
            SyntaxKind kind,
            STNode metadata,
            STNode qualifierList,
            STNode functionKeyword,
            STNode functionName,
            STNode relativeResourcePath,
            STNode functionSignature,
            STNode functionBody) {
        if (checkForReferenceEquality(
                metadata,
                qualifierList,
                functionKeyword,
                functionName,
                relativeResourcePath,
                functionSignature,
                functionBody)) {
            return this;
        }

        return new STFunctionDefinitionNode(
                kind,
                metadata,
                qualifierList,
                functionKeyword,
                functionName,
                relativeResourcePath,
                functionSignature,
                functionBody,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new FunctionDefinitionNode(this, position, parent);
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
