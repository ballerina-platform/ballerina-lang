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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.XMLEndTagNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STXMLEndTagNode extends STXMLElementTagNode {
    public final STNode ltToken;
    public final STNode slashToken;
    public final STNode name;
    public final STNode getToken;

    STXMLEndTagNode(
            STNode ltToken,
            STNode slashToken,
            STNode name,
            STNode getToken) {
        this(
                ltToken,
                slashToken,
                name,
                getToken,
                Collections.emptyList());
    }

    STXMLEndTagNode(
            STNode ltToken,
            STNode slashToken,
            STNode name,
            STNode getToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.XML_ELEMENT_END_TAG, diagnostics);
        this.ltToken = ltToken;
        this.slashToken = slashToken;
        this.name = name;
        this.getToken = getToken;

        addChildren(
                ltToken,
                slashToken,
                name,
                getToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STXMLEndTagNode(
                this.ltToken,
                this.slashToken,
                this.name,
                this.getToken,
                diagnostics);
    }

    public STXMLEndTagNode modify(
            STNode ltToken,
            STNode slashToken,
            STNode name,
            STNode getToken) {
        if (checkForReferenceEquality(
                ltToken,
                slashToken,
                name,
                getToken)) {
            return this;
        }

        return new STXMLEndTagNode(
                ltToken,
                slashToken,
                name,
                getToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new XMLEndTagNode(this, position, parent);
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
