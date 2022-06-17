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

import io.ballerina.compiler.syntax.tree.ComputedResourceAccessSegmentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.2.0
 */
public class STComputedResourceAccessSegmentNode extends STNode {
    public final STNode openBracketToken;
    public final STNode expression;
    public final STNode closeBracketToken;

    STComputedResourceAccessSegmentNode(
            STNode openBracketToken,
            STNode expression,
            STNode closeBracketToken) {
        this(
                openBracketToken,
                expression,
                closeBracketToken,
                Collections.emptyList());
    }

    STComputedResourceAccessSegmentNode(
            STNode openBracketToken,
            STNode expression,
            STNode closeBracketToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.COMPUTED_RESOURCE_ACCESS_SEGMENT, diagnostics);
        this.openBracketToken = openBracketToken;
        this.expression = expression;
        this.closeBracketToken = closeBracketToken;

        addChildren(
                openBracketToken,
                expression,
                closeBracketToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STComputedResourceAccessSegmentNode(
                this.openBracketToken,
                this.expression,
                this.closeBracketToken,
                diagnostics);
    }

    public STComputedResourceAccessSegmentNode modify(
            STNode openBracketToken,
            STNode expression,
            STNode closeBracketToken) {
        if (checkForReferenceEquality(
                openBracketToken,
                expression,
                closeBracketToken)) {
            return this;
        }

        return new STComputedResourceAccessSegmentNode(
                openBracketToken,
                expression,
                closeBracketToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ComputedResourceAccessSegmentNode(this, position, parent);
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
