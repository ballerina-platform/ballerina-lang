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
import io.ballerina.compiler.syntax.tree.RestMatchPatternNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STRestMatchPatternNode extends STNode {
    public final STNode ellipsisToken;
    public final STNode varKeywordToken;
    public final STNode variableName;

    STRestMatchPatternNode(
            STNode ellipsisToken,
            STNode varKeywordToken,
            STNode variableName) {
        this(
                ellipsisToken,
                varKeywordToken,
                variableName,
                Collections.emptyList());
    }

    STRestMatchPatternNode(
            STNode ellipsisToken,
            STNode varKeywordToken,
            STNode variableName,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.REST_MATCH_PATTERN, diagnostics);
        this.ellipsisToken = ellipsisToken;
        this.varKeywordToken = varKeywordToken;
        this.variableName = variableName;

        addChildren(
                ellipsisToken,
                varKeywordToken,
                variableName);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STRestMatchPatternNode(
                this.ellipsisToken,
                this.varKeywordToken,
                this.variableName,
                diagnostics);
    }

    public STRestMatchPatternNode modify(
            STNode ellipsisToken,
            STNode varKeywordToken,
            STNode variableName) {
        if (checkForReferenceEquality(
                ellipsisToken,
                varKeywordToken,
                variableName)) {
            return this;
        }

        return new STRestMatchPatternNode(
                ellipsisToken,
                varKeywordToken,
                variableName,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new RestMatchPatternNode(this, position, parent);
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
