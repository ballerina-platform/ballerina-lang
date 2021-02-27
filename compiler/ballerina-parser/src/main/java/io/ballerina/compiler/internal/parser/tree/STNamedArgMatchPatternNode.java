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

import io.ballerina.compiler.syntax.tree.NamedArgMatchPatternNode;
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
public class STNamedArgMatchPatternNode extends STNode {
    public final STNode identifier;
    public final STNode equalToken;
    public final STNode matchPattern;

    STNamedArgMatchPatternNode(
            STNode identifier,
            STNode equalToken,
            STNode matchPattern) {
        this(
                identifier,
                equalToken,
                matchPattern,
                Collections.emptyList());
    }

    STNamedArgMatchPatternNode(
            STNode identifier,
            STNode equalToken,
            STNode matchPattern,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.NAMED_ARG_MATCH_PATTERN, diagnostics);
        this.identifier = identifier;
        this.equalToken = equalToken;
        this.matchPattern = matchPattern;

        addChildren(
                identifier,
                equalToken,
                matchPattern);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STNamedArgMatchPatternNode(
                this.identifier,
                this.equalToken,
                this.matchPattern,
                diagnostics);
    }

    public STNamedArgMatchPatternNode modify(
            STNode identifier,
            STNode equalToken,
            STNode matchPattern) {
        if (checkForReferenceEquality(
                identifier,
                equalToken,
                matchPattern)) {
            return this;
        }

        return new STNamedArgMatchPatternNode(
                identifier,
                equalToken,
                matchPattern,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new NamedArgMatchPatternNode(this, position, parent);
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
