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

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
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
public class STClassDefinitionNode extends STModuleMemberDeclarationNode {
    public final STNode metadata;
    public final STNode visibilityQualifier;
    public final STNode classTypeQualifiers;
    public final STNode classKeyword;
    public final STNode className;
    public final STNode openBrace;
    public final STNode members;
    public final STNode closeBrace;

    STClassDefinitionNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode classTypeQualifiers,
            STNode classKeyword,
            STNode className,
            STNode openBrace,
            STNode members,
            STNode closeBrace) {
        this(
                metadata,
                visibilityQualifier,
                classTypeQualifiers,
                classKeyword,
                className,
                openBrace,
                members,
                closeBrace,
                Collections.emptyList());
    }

    STClassDefinitionNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode classTypeQualifiers,
            STNode classKeyword,
            STNode className,
            STNode openBrace,
            STNode members,
            STNode closeBrace,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.CLASS_DEFINITION, diagnostics);
        this.metadata = metadata;
        this.visibilityQualifier = visibilityQualifier;
        this.classTypeQualifiers = classTypeQualifiers;
        this.classKeyword = classKeyword;
        this.className = className;
        this.openBrace = openBrace;
        this.members = members;
        this.closeBrace = closeBrace;

        addChildren(
                metadata,
                visibilityQualifier,
                classTypeQualifiers,
                classKeyword,
                className,
                openBrace,
                members,
                closeBrace);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STClassDefinitionNode(
                this.metadata,
                this.visibilityQualifier,
                this.classTypeQualifiers,
                this.classKeyword,
                this.className,
                this.openBrace,
                this.members,
                this.closeBrace,
                diagnostics);
    }

    public STClassDefinitionNode modify(
            STNode metadata,
            STNode visibilityQualifier,
            STNode classTypeQualifiers,
            STNode classKeyword,
            STNode className,
            STNode openBrace,
            STNode members,
            STNode closeBrace) {
        if (checkForReferenceEquality(
                metadata,
                visibilityQualifier,
                classTypeQualifiers,
                classKeyword,
                className,
                openBrace,
                members,
                closeBrace)) {
            return this;
        }

        return new STClassDefinitionNode(
                metadata,
                visibilityQualifier,
                classTypeQualifiers,
                classKeyword,
                className,
                openBrace,
                members,
                closeBrace,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ClassDefinitionNode(this, position, parent);
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
