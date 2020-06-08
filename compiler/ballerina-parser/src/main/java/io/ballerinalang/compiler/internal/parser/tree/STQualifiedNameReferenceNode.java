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
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STQualifiedNameReferenceNode extends STNameReferenceNode {
    public final STNode modulePrefix;
    public final STNode colon;
    public final STNode identifier;

    STQualifiedNameReferenceNode(
            STNode modulePrefix,
            STNode colon,
            STNode identifier) {
        this(
                modulePrefix,
                colon,
                identifier,
                Collections.emptyList());
    }

    STQualifiedNameReferenceNode(
            STNode modulePrefix,
            STNode colon,
            STNode identifier,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.QUALIFIED_NAME_REFERENCE, diagnostics);
        this.modulePrefix = modulePrefix;
        this.colon = colon;
        this.identifier = identifier;

        addChildren(
                modulePrefix,
                colon,
                identifier);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STQualifiedNameReferenceNode(
                this.modulePrefix,
                this.colon,
                this.identifier,
                diagnostics);
    }

    public STQualifiedNameReferenceNode modify(
            STNode modulePrefix,
            STNode colon,
            STNode identifier) {
        if (checkForReferenceEquality(
                modulePrefix,
                colon,
                identifier)) {
            return this;
        }

        return new STQualifiedNameReferenceNode(
                modulePrefix,
                colon,
                identifier,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new QualifiedNameReferenceNode(this, position, parent);
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
