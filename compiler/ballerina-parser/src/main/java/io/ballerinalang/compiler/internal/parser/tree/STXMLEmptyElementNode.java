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
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.XMLEmptyElementNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STXMLEmptyElementNode extends STXMLItemNode {
    public final STNode ltToken;
    public final STNode name;
    public final STNode attributes;
    public final STNode slashToken;
    public final STNode getToken;

    STXMLEmptyElementNode(
            STNode ltToken,
            STNode name,
            STNode attributes,
            STNode slashToken,
            STNode getToken) {
        this(
                ltToken,
                name,
                attributes,
                slashToken,
                getToken,
                Collections.emptyList());
    }

    STXMLEmptyElementNode(
            STNode ltToken,
            STNode name,
            STNode attributes,
            STNode slashToken,
            STNode getToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.XML_EMPTY_ELEMENT, diagnostics);
        this.ltToken = ltToken;
        this.name = name;
        this.attributes = attributes;
        this.slashToken = slashToken;
        this.getToken = getToken;

        addChildren(
                ltToken,
                name,
                attributes,
                slashToken,
                getToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STXMLEmptyElementNode(
                this.ltToken,
                this.name,
                this.attributes,
                this.slashToken,
                this.getToken,
                diagnostics);
    }

    public STXMLEmptyElementNode modify(
            STNode ltToken,
            STNode name,
            STNode attributes,
            STNode slashToken,
            STNode getToken) {
        if (checkForReferenceEquality(
                ltToken,
                name,
                attributes,
                slashToken,
                getToken)) {
            return this;
        }

        return new STXMLEmptyElementNode(
                ltToken,
                name,
                attributes,
                slashToken,
                getToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new XMLEmptyElementNode(this, position, parent);
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
