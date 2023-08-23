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

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
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
public class STImportDeclarationNode extends STNode {
    public final STNode importKeyword;
    public final STNode orgName;
    public final STNode moduleName;
    public final STNode prefix;
    public final STNode semicolon;

    STImportDeclarationNode(
            STNode importKeyword,
            STNode orgName,
            STNode moduleName,
            STNode prefix,
            STNode semicolon) {
        this(
                importKeyword,
                orgName,
                moduleName,
                prefix,
                semicolon,
                Collections.emptyList());
    }

    STImportDeclarationNode(
            STNode importKeyword,
            STNode orgName,
            STNode moduleName,
            STNode prefix,
            STNode semicolon,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.IMPORT_DECLARATION, diagnostics);
        this.importKeyword = importKeyword;
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.prefix = prefix;
        this.semicolon = semicolon;

        addChildren(
                importKeyword,
                orgName,
                moduleName,
                prefix,
                semicolon);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STImportDeclarationNode(
                this.importKeyword,
                this.orgName,
                this.moduleName,
                this.prefix,
                this.semicolon,
                diagnostics);
    }

    public STImportDeclarationNode modify(
            STNode importKeyword,
            STNode orgName,
            STNode moduleName,
            STNode prefix,
            STNode semicolon) {
        if (checkForReferenceEquality(
                importKeyword,
                orgName,
                moduleName,
                prefix,
                semicolon)) {
            return this;
        }

        return new STImportDeclarationNode(
                importKeyword,
                orgName,
                moduleName,
                prefix,
                semicolon,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ImportDeclarationNode(this, position, parent);
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
