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

import io.ballerina.compiler.syntax.tree.FieldMatchPatternNode;
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
public class STFieldMatchPatternNode extends STNode {
    public final STNode fieldNameNode;
    public final STNode colonToken;
    public final STNode matchPattern;

    STFieldMatchPatternNode(
            STNode fieldNameNode,
            STNode colonToken,
            STNode matchPattern) {
        this(
                fieldNameNode,
                colonToken,
                matchPattern,
                Collections.emptyList());
    }

    STFieldMatchPatternNode(
            STNode fieldNameNode,
            STNode colonToken,
            STNode matchPattern,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.FIELD_MATCH_PATTERN, diagnostics);
        this.fieldNameNode = fieldNameNode;
        this.colonToken = colonToken;
        this.matchPattern = matchPattern;

        addChildren(
                fieldNameNode,
                colonToken,
                matchPattern);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STFieldMatchPatternNode(
                this.fieldNameNode,
                this.colonToken,
                this.matchPattern,
                diagnostics);
    }

    public STFieldMatchPatternNode modify(
            STNode fieldNameNode,
            STNode colonToken,
            STNode matchPattern) {
        if (checkForReferenceEquality(
                fieldNameNode,
                colonToken,
                matchPattern)) {
            return this;
        }

        return new STFieldMatchPatternNode(
                fieldNameNode,
                colonToken,
                matchPattern,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new FieldMatchPatternNode(this, position, parent);
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
