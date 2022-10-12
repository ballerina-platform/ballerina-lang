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
import io.ballerina.compiler.syntax.tree.ReFlagExpressionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.3.0
 */
public class STReFlagExpressionNode extends STNode {
    public final STNode questionMark;
    public final STNode reFlagsOnOff;
    public final STNode colon;

    STReFlagExpressionNode(
            STNode questionMark,
            STNode reFlagsOnOff,
            STNode colon) {
        this(
                questionMark,
                reFlagsOnOff,
                colon,
                Collections.emptyList());
    }

    STReFlagExpressionNode(
            STNode questionMark,
            STNode reFlagsOnOff,
            STNode colon,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RE_FLAG_EXPR, diagnostics);
        this.questionMark = questionMark;
        this.reFlagsOnOff = reFlagsOnOff;
        this.colon = colon;

        addChildren(
                questionMark,
                reFlagsOnOff,
                colon);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STReFlagExpressionNode(
                this.questionMark,
                this.reFlagsOnOff,
                this.colon,
                diagnostics);
    }

    public STReFlagExpressionNode modify(
            STNode questionMark,
            STNode reFlagsOnOff,
            STNode colon) {
        if (checkForReferenceEquality(
                questionMark,
                reFlagsOnOff,
                colon)) {
            return this;
        }

        return new STReFlagExpressionNode(
                questionMark,
                reFlagsOnOff,
                colon,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ReFlagExpressionNode(this, position, parent);
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
