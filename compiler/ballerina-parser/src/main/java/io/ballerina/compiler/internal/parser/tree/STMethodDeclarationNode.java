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

import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
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
public class STMethodDeclarationNode extends STNode {
    public final STNode metadata;
    public final STNode qualifierList;
    public final STNode functionKeyword;
    public final STNode methodName;
    public final STNode relativeResourcePath;
    public final STNode methodSignature;
    public final STNode semicolon;

    STMethodDeclarationNode(
            SyntaxKind kind,
            STNode metadata,
            STNode qualifierList,
            STNode functionKeyword,
            STNode methodName,
            STNode relativeResourcePath,
            STNode methodSignature,
            STNode semicolon) {
        this(
                kind,
                metadata,
                qualifierList,
                functionKeyword,
                methodName,
                relativeResourcePath,
                methodSignature,
                semicolon,
                Collections.emptyList());
    }

    STMethodDeclarationNode(
            SyntaxKind kind,
            STNode metadata,
            STNode qualifierList,
            STNode functionKeyword,
            STNode methodName,
            STNode relativeResourcePath,
            STNode methodSignature,
            STNode semicolon,
            Collection<STNodeDiagnostic> diagnostics) {
        super(kind, diagnostics);
        this.metadata = metadata;
        this.qualifierList = qualifierList;
        this.functionKeyword = functionKeyword;
        this.methodName = methodName;
        this.relativeResourcePath = relativeResourcePath;
        this.methodSignature = methodSignature;
        this.semicolon = semicolon;

        addChildren(
                metadata,
                qualifierList,
                functionKeyword,
                methodName,
                relativeResourcePath,
                methodSignature,
                semicolon);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STMethodDeclarationNode(
                this.kind,
                this.metadata,
                this.qualifierList,
                this.functionKeyword,
                this.methodName,
                this.relativeResourcePath,
                this.methodSignature,
                this.semicolon,
                diagnostics);
    }

    public STMethodDeclarationNode modify(
            SyntaxKind kind,
            STNode metadata,
            STNode qualifierList,
            STNode functionKeyword,
            STNode methodName,
            STNode relativeResourcePath,
            STNode methodSignature,
            STNode semicolon) {
        if (checkForReferenceEquality(
                metadata,
                qualifierList,
                functionKeyword,
                methodName,
                relativeResourcePath,
                methodSignature,
                semicolon)) {
            return this;
        }

        return new STMethodDeclarationNode(
                kind,
                metadata,
                qualifierList,
                functionKeyword,
                methodName,
                relativeResourcePath,
                methodSignature,
                semicolon,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new MethodDeclarationNode(this, position, parent);
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
