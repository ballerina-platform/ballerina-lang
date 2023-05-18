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
import io.ballerina.compiler.syntax.tree.ReBracedQuantifierNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.3.0
 */
public class STReBracedQuantifierNode extends STNode {
    public final STNode openBraceToken;
    public final STNode leastTimesMatchedDigit;
    public final STNode commaToken;
    public final STNode mostTimesMatchedDigit;
    public final STNode closeBraceToken;

    STReBracedQuantifierNode(
            STNode openBraceToken,
            STNode leastTimesMatchedDigit,
            STNode commaToken,
            STNode mostTimesMatchedDigit,
            STNode closeBraceToken) {
        this(
                openBraceToken,
                leastTimesMatchedDigit,
                commaToken,
                mostTimesMatchedDigit,
                closeBraceToken,
                Collections.emptyList());
    }

    STReBracedQuantifierNode(
            STNode openBraceToken,
            STNode leastTimesMatchedDigit,
            STNode commaToken,
            STNode mostTimesMatchedDigit,
            STNode closeBraceToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RE_BRACED_QUANTIFIER, diagnostics);
        this.openBraceToken = openBraceToken;
        this.leastTimesMatchedDigit = leastTimesMatchedDigit;
        this.commaToken = commaToken;
        this.mostTimesMatchedDigit = mostTimesMatchedDigit;
        this.closeBraceToken = closeBraceToken;

        addChildren(
                openBraceToken,
                leastTimesMatchedDigit,
                commaToken,
                mostTimesMatchedDigit,
                closeBraceToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STReBracedQuantifierNode(
                this.openBraceToken,
                this.leastTimesMatchedDigit,
                this.commaToken,
                this.mostTimesMatchedDigit,
                this.closeBraceToken,
                diagnostics);
    }

    public STReBracedQuantifierNode modify(
            STNode openBraceToken,
            STNode leastTimesMatchedDigit,
            STNode commaToken,
            STNode mostTimesMatchedDigit,
            STNode closeBraceToken) {
        if (checkForReferenceEquality(
                openBraceToken,
                leastTimesMatchedDigit,
                commaToken,
                mostTimesMatchedDigit,
                closeBraceToken)) {
            return this;
        }

        return new STReBracedQuantifierNode(
                openBraceToken,
                leastTimesMatchedDigit,
                commaToken,
                mostTimesMatchedDigit,
                closeBraceToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ReBracedQuantifierNode(this, position, parent);
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
