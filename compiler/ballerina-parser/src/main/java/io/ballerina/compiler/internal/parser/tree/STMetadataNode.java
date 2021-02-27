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

import io.ballerina.compiler.syntax.tree.MetadataNode;
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
public class STMetadataNode extends STNode {
    public final STNode documentationString;
    public final STNode annotations;

    STMetadataNode(
            STNode documentationString,
            STNode annotations) {
        this(
                documentationString,
                annotations,
                Collections.emptyList());
    }

    STMetadataNode(
            STNode documentationString,
            STNode annotations,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.METADATA, diagnostics);
        this.documentationString = documentationString;
        this.annotations = annotations;

        addChildren(
                documentationString,
                annotations);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STMetadataNode(
                this.documentationString,
                this.annotations,
                diagnostics);
    }

    public STMetadataNode modify(
            STNode documentationString,
            STNode annotations) {
        if (checkForReferenceEquality(
                documentationString,
                annotations)) {
            return this;
        }

        return new STMetadataNode(
                documentationString,
                annotations,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new MetadataNode(this, position, parent);
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
