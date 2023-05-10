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
import io.ballerina.compiler.syntax.tree.OnFailCheckNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STOnFailCheckNode extends STNode {
    public final STNode onKeyword;
    public final STNode failKeyword;
    public final STNode errorExpr;
    public final STNode rightArrowToken;
    public final STNode errorConstructor;

    STOnFailCheckNode(
            SyntaxKind kind,
            STNode onKeyword,
            STNode failKeyword,
            STNode errorExpr,
            STNode rightArrowToken,
            STNode errorConstructor) {
        this(
                kind,
                onKeyword,
                failKeyword,
                errorExpr,
                rightArrowToken,
                errorConstructor,
                Collections.emptyList());
    }

    STOnFailCheckNode(
            SyntaxKind kind,
            STNode onKeyword,
            STNode failKeyword,
            STNode errorExpr,
            STNode rightArrowToken,
            STNode errorConstructor,
            Collection<STNodeDiagnostic> diagnostics) {
        super(kind, diagnostics);
        this.onKeyword = onKeyword;
        this.failKeyword = failKeyword;
        this.errorExpr = errorExpr;
        this.rightArrowToken = rightArrowToken;
        this.errorConstructor = errorConstructor;

        addChildren(
                onKeyword,
                failKeyword,
                errorExpr,
                rightArrowToken,
                errorConstructor);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STOnFailCheckNode(
                this.kind,
                this.onKeyword,
                this.failKeyword,
                this.errorExpr,
                this.rightArrowToken,
                this.errorConstructor,
                diagnostics);
    }

    public STOnFailCheckNode modify(
            SyntaxKind kind,
            STNode onKeyword,
            STNode failKeyword,
            STNode errorExpr,
            STNode rightArrowToken,
            STNode errorConstructor) {
        if (checkForReferenceEquality(
                onKeyword,
                failKeyword,
                errorExpr,
                rightArrowToken,
                errorConstructor)) {
            return this;
        }

        return new STOnFailCheckNode(
                kind,
                onKeyword,
                failKeyword,
                errorExpr,
                rightArrowToken,
                errorConstructor,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new OnFailCheckNode(this, position, parent);
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
