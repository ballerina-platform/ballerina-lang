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

import io.ballerina.compiler.syntax.tree.ModuleXMLNamespaceDeclarationNode;
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
public class STModuleXMLNamespaceDeclarationNode extends STModuleMemberDeclarationNode {
    public final STNode xmlnsKeyword;
    public final STNode namespaceuri;
    public final STNode asKeyword;
    public final STNode namespacePrefix;
    public final STNode semicolonToken;

    STModuleXMLNamespaceDeclarationNode(
            STNode xmlnsKeyword,
            STNode namespaceuri,
            STNode asKeyword,
            STNode namespacePrefix,
            STNode semicolonToken) {
        this(
                xmlnsKeyword,
                namespaceuri,
                asKeyword,
                namespacePrefix,
                semicolonToken,
                Collections.emptyList());
    }

    STModuleXMLNamespaceDeclarationNode(
            STNode xmlnsKeyword,
            STNode namespaceuri,
            STNode asKeyword,
            STNode namespacePrefix,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.MODULE_XML_NAMESPACE_DECLARATION, diagnostics);
        this.xmlnsKeyword = xmlnsKeyword;
        this.namespaceuri = namespaceuri;
        this.asKeyword = asKeyword;
        this.namespacePrefix = namespacePrefix;
        this.semicolonToken = semicolonToken;

        addChildren(
                xmlnsKeyword,
                namespaceuri,
                asKeyword,
                namespacePrefix,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STModuleXMLNamespaceDeclarationNode(
                this.xmlnsKeyword,
                this.namespaceuri,
                this.asKeyword,
                this.namespacePrefix,
                this.semicolonToken,
                diagnostics);
    }

    public STModuleXMLNamespaceDeclarationNode modify(
            STNode xmlnsKeyword,
            STNode namespaceuri,
            STNode asKeyword,
            STNode namespacePrefix,
            STNode semicolonToken) {
        if (checkForReferenceEquality(
                xmlnsKeyword,
                namespaceuri,
                asKeyword,
                namespacePrefix,
                semicolonToken)) {
            return this;
        }

        return new STModuleXMLNamespaceDeclarationNode(
                xmlnsKeyword,
                namespaceuri,
                asKeyword,
                namespacePrefix,
                semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ModuleXMLNamespaceDeclarationNode(this, position, parent);
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
