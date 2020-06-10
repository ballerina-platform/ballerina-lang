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

import io.ballerinalang.compiler.syntax.tree.EnumDeclarationNode;
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
public class STEnumDeclarationNode extends STModuleMemberDeclarationNode {
    public final STNode metadata;
    public final STNode qualifier;
    public final STNode enumKeywordToken;
    public final STNode identifier;
    public final STNode openBraceToken;
    public final STNode enumMemberList;
    public final STNode closeBraceToken;

    STEnumDeclarationNode(
            STNode metadata,
            STNode qualifier,
            STNode enumKeywordToken,
            STNode identifier,
            STNode openBraceToken,
            STNode enumMemberList,
            STNode closeBraceToken) {
        this(
                metadata,
                qualifier,
                enumKeywordToken,
                identifier,
                openBraceToken,
                enumMemberList,
                closeBraceToken,
                Collections.emptyList());
    }

    STEnumDeclarationNode(
            STNode metadata,
            STNode qualifier,
            STNode enumKeywordToken,
            STNode identifier,
            STNode openBraceToken,
            STNode enumMemberList,
            STNode closeBraceToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.ENUM_DECLARATION, diagnostics);
        this.metadata = metadata;
        this.qualifier = qualifier;
        this.enumKeywordToken = enumKeywordToken;
        this.identifier = identifier;
        this.openBraceToken = openBraceToken;
        this.enumMemberList = enumMemberList;
        this.closeBraceToken = closeBraceToken;

        addChildren(
                metadata,
                qualifier,
                enumKeywordToken,
                identifier,
                openBraceToken,
                enumMemberList,
                closeBraceToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STEnumDeclarationNode(
                this.metadata,
                this.qualifier,
                this.enumKeywordToken,
                this.identifier,
                this.openBraceToken,
                this.enumMemberList,
                this.closeBraceToken,
                diagnostics);
    }

    public STEnumDeclarationNode modify(
            STNode metadata,
            STNode qualifier,
            STNode enumKeywordToken,
            STNode identifier,
            STNode openBraceToken,
            STNode enumMemberList,
            STNode closeBraceToken) {
        if (checkForReferenceEquality(
                metadata,
                qualifier,
                enumKeywordToken,
                identifier,
                openBraceToken,
                enumMemberList,
                closeBraceToken)) {
            return this;
        }

        return new STEnumDeclarationNode(
                metadata,
                qualifier,
                enumKeywordToken,
                identifier,
                openBraceToken,
                enumMemberList,
                closeBraceToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new EnumDeclarationNode(this, position, parent);
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
