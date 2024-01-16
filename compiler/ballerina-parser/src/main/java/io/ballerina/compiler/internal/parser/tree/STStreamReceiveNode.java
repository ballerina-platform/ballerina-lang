/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.StreamReceiveNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2201.9.0
 */
public class STStreamReceiveNode extends STNode {
    public final STNode streamKeyword;
    public final STNode openParenthesis;
    public final STNode workers;
    public final STNode closeParenthesis;

    STStreamReceiveNode(
            STNode streamKeyword,
            STNode openParenthesis,
            STNode workers,
            STNode closeParenthesis) {
        this(
                streamKeyword,
                openParenthesis,
                workers,
                closeParenthesis,
                Collections.emptyList());
    }

    STStreamReceiveNode(
            STNode streamKeyword,
            STNode openParenthesis,
            STNode workers,
            STNode closeParenthesis,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.STREAM_RECEIVE, diagnostics);
        this.streamKeyword = streamKeyword;
        this.openParenthesis = openParenthesis;
        this.workers = workers;
        this.closeParenthesis = closeParenthesis;

        addChildren(
                streamKeyword,
                openParenthesis,
                workers,
                closeParenthesis);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STStreamReceiveNode(
                this.streamKeyword,
                this.openParenthesis,
                this.workers,
                this.closeParenthesis,
                diagnostics);
    }

    public STStreamReceiveNode modify(
            STNode streamKeyword,
            STNode openParenthesis,
            STNode workers,
            STNode closeParenthesis) {
        if (checkForReferenceEquality(
                streamKeyword,
                openParenthesis,
                workers,
                closeParenthesis)) {
            return this;
        }

        return new STStreamReceiveNode(
                streamKeyword,
                openParenthesis,
                workers,
                closeParenthesis,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new StreamReceiveNode(this, position, parent);
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
