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
import io.ballerina.compiler.syntax.tree.ReCapturingGroupsNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STReCapturingGroupsNode extends STNode {
    public final STNode openParenthesis;
    public final STNode reFlagExpression;
    public final STNode reSequences;
    public final STNode closeParenthesis;

    STReCapturingGroupsNode(
            STNode openParenthesis,
            STNode reFlagExpression,
            STNode reSequences,
            STNode closeParenthesis) {
        this(
                openParenthesis,
                reFlagExpression,
                reSequences,
                closeParenthesis,
                Collections.emptyList());
    }

    STReCapturingGroupsNode(
            STNode openParenthesis,
            STNode reFlagExpression,
            STNode reSequences,
            STNode closeParenthesis,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RE_CAPTURING_GROUP, diagnostics);
        this.openParenthesis = openParenthesis;
        this.reFlagExpression = reFlagExpression;
        this.reSequences = reSequences;
        this.closeParenthesis = closeParenthesis;

        addChildren(
                openParenthesis,
                reFlagExpression,
                reSequences,
                closeParenthesis);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STReCapturingGroupsNode(
                this.openParenthesis,
                this.reFlagExpression,
                this.reSequences,
                this.closeParenthesis,
                diagnostics);
    }

    public STReCapturingGroupsNode modify(
            STNode openParenthesis,
            STNode reFlagExpression,
            STNode reSequences,
            STNode closeParenthesis) {
        if (checkForReferenceEquality(
                openParenthesis,
                reFlagExpression,
                reSequences,
                closeParenthesis)) {
            return this;
        }

        return new STReCapturingGroupsNode(
                openParenthesis,
                reFlagExpression,
                reSequences,
                closeParenthesis,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ReCapturingGroupsNode(this, position, parent);
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
