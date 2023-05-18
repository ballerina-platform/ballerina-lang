/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.ReUnicodePropertyEscapeNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.3.0
 */
public class STReUnicodePropertyEscapeNode extends STNode {
    public final STNode slashToken;
    public final STNode property;
    public final STNode openBraceToken;
    public final STNode reUnicodeProperty;
    public final STNode closeBraceToken;

    STReUnicodePropertyEscapeNode(
            STNode slashToken,
            STNode property,
            STNode openBraceToken,
            STNode reUnicodeProperty,
            STNode closeBraceToken) {
        this(
                slashToken,
                property,
                openBraceToken,
                reUnicodeProperty,
                closeBraceToken,
                Collections.emptyList());
    }

    STReUnicodePropertyEscapeNode(
            STNode slashToken,
            STNode property,
            STNode openBraceToken,
            STNode reUnicodeProperty,
            STNode closeBraceToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RE_UNICODE_PROPERTY_ESCAPE, diagnostics);
        this.slashToken = slashToken;
        this.property = property;
        this.openBraceToken = openBraceToken;
        this.reUnicodeProperty = reUnicodeProperty;
        this.closeBraceToken = closeBraceToken;

        addChildren(
                slashToken,
                property,
                openBraceToken,
                reUnicodeProperty,
                closeBraceToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STReUnicodePropertyEscapeNode(
                this.slashToken,
                this.property,
                this.openBraceToken,
                this.reUnicodeProperty,
                this.closeBraceToken,
                diagnostics);
    }

    public STReUnicodePropertyEscapeNode modify(
            STNode slashToken,
            STNode property,
            STNode openBraceToken,
            STNode reUnicodeProperty,
            STNode closeBraceToken) {
        if (checkForReferenceEquality(
                slashToken,
                property,
                openBraceToken,
                reUnicodeProperty,
                closeBraceToken)) {
            return this;
        }

        return new STReUnicodePropertyEscapeNode(
                slashToken,
                property,
                openBraceToken,
                reUnicodeProperty,
                closeBraceToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ReUnicodePropertyEscapeNode(this, position, parent);
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
