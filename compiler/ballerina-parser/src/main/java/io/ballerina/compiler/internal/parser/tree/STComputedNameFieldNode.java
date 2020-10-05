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

import io.ballerina.compiler.syntax.tree.ComputedNameFieldNode;
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
public class STComputedNameFieldNode extends STMappingFieldNode {
    public final STNode openBracket;
    public final STNode fieldNameExpr;
    public final STNode closeBracket;
    public final STNode colonToken;
    public final STNode valueExpr;

    STComputedNameFieldNode(
            STNode openBracket,
            STNode fieldNameExpr,
            STNode closeBracket,
            STNode colonToken,
            STNode valueExpr) {
        this(
                openBracket,
                fieldNameExpr,
                closeBracket,
                colonToken,
                valueExpr,
                Collections.emptyList());
    }

    STComputedNameFieldNode(
            STNode openBracket,
            STNode fieldNameExpr,
            STNode closeBracket,
            STNode colonToken,
            STNode valueExpr,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.COMPUTED_NAME_FIELD, diagnostics);
        this.openBracket = openBracket;
        this.fieldNameExpr = fieldNameExpr;
        this.closeBracket = closeBracket;
        this.colonToken = colonToken;
        this.valueExpr = valueExpr;

        addChildren(
                openBracket,
                fieldNameExpr,
                closeBracket,
                colonToken,
                valueExpr);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STComputedNameFieldNode(
                this.openBracket,
                this.fieldNameExpr,
                this.closeBracket,
                this.colonToken,
                this.valueExpr,
                diagnostics);
    }

    public STComputedNameFieldNode modify(
            STNode openBracket,
            STNode fieldNameExpr,
            STNode closeBracket,
            STNode colonToken,
            STNode valueExpr) {
        if (checkForReferenceEquality(
                openBracket,
                fieldNameExpr,
                closeBracket,
                colonToken,
                valueExpr)) {
            return this;
        }

        return new STComputedNameFieldNode(
                openBracket,
                fieldNameExpr,
                closeBracket,
                colonToken,
                valueExpr,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ComputedNameFieldNode(this, position, parent);
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
