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

import io.ballerina.compiler.syntax.tree.DocumentationReferenceNode;
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
public class STDocumentationReferenceNode extends STDocumentationNode {
    public final STNode referenceType;
    public final STNode startBacktick;
    public final STNode backtickContent;
    public final STNode endBacktick;

    STDocumentationReferenceNode(
            STNode referenceType,
            STNode startBacktick,
            STNode backtickContent,
            STNode endBacktick) {
        this(
                referenceType,
                startBacktick,
                backtickContent,
                endBacktick,
                Collections.emptyList());
    }

    STDocumentationReferenceNode(
            STNode referenceType,
            STNode startBacktick,
            STNode backtickContent,
            STNode endBacktick,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.DOCUMENTATION_REFERENCE, diagnostics);
        this.referenceType = referenceType;
        this.startBacktick = startBacktick;
        this.backtickContent = backtickContent;
        this.endBacktick = endBacktick;

        addChildren(
                referenceType,
                startBacktick,
                backtickContent,
                endBacktick);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STDocumentationReferenceNode(
                this.referenceType,
                this.startBacktick,
                this.backtickContent,
                this.endBacktick,
                diagnostics);
    }

    public STDocumentationReferenceNode modify(
            STNode referenceType,
            STNode startBacktick,
            STNode backtickContent,
            STNode endBacktick) {
        if (checkForReferenceEquality(
                referenceType,
                startBacktick,
                backtickContent,
                endBacktick)) {
            return this;
        }

        return new STDocumentationReferenceNode(
                referenceType,
                startBacktick,
                backtickContent,
                endBacktick,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new DocumentationReferenceNode(this, position, parent);
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
