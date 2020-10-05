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

import io.ballerina.compiler.syntax.tree.ListMatchPatternNode;
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
public class STListMatchPatternNode extends STNode {
    public final STNode openBracket;
    public final STNode matchPatterns;
    public final STNode restMatchPattern;
    public final STNode closeBracket;

    STListMatchPatternNode(
            STNode openBracket,
            STNode matchPatterns,
            STNode restMatchPattern,
            STNode closeBracket) {
        this(
                openBracket,
                matchPatterns,
                restMatchPattern,
                closeBracket,
                Collections.emptyList());
    }

    STListMatchPatternNode(
            STNode openBracket,
            STNode matchPatterns,
            STNode restMatchPattern,
            STNode closeBracket,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.LIST_MATCH_PATTERN, diagnostics);
        this.openBracket = openBracket;
        this.matchPatterns = matchPatterns;
        this.restMatchPattern = restMatchPattern;
        this.closeBracket = closeBracket;

        addChildren(
                openBracket,
                matchPatterns,
                restMatchPattern,
                closeBracket);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STListMatchPatternNode(
                this.openBracket,
                this.matchPatterns,
                this.restMatchPattern,
                this.closeBracket,
                diagnostics);
    }

    public STListMatchPatternNode modify(
            STNode openBracket,
            STNode matchPatterns,
            STNode restMatchPattern,
            STNode closeBracket) {
        if (checkForReferenceEquality(
                openBracket,
                matchPatterns,
                restMatchPattern,
                closeBracket)) {
            return this;
        }

        return new STListMatchPatternNode(
                openBracket,
                matchPatterns,
                restMatchPattern,
                closeBracket,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ListMatchPatternNode(this, position, parent);
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
