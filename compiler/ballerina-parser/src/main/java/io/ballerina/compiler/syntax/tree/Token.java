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
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.Collections;

/**
 * Represents a terminal node in the Ballerina syntax tree.
 * <p>
 * Always Minutiae is associated with a terminal node.
 *
 * @since 2.0.0
 */
public class Token extends Node {
    protected MinutiaeList leadingMinutiaeList;
    protected MinutiaeList trailingMinutiaeList;

    public Token(STNode token, int position, NonTerminalNode parent) {
        super(token, position, parent);
    }

    public String text() {
        return ((STToken) this.internalNode).text();
    }

    public boolean containsLeadingMinutiae() {
        return internalNode.leadingMinutiae().bucketCount() > 0;
    }

    public MinutiaeList leadingMinutiae() {
        if (leadingMinutiaeList != null) {
            return leadingMinutiaeList;
        }
        leadingMinutiaeList = new MinutiaeList(this, internalNode.leadingMinutiae(), this.position());
        return leadingMinutiaeList;
    }

    public boolean containsTrailingMinutiae() {
        return internalNode.trailingMinutiae().bucketCount() > 0;
    }

    public MinutiaeList trailingMinutiae() {
        if (trailingMinutiaeList != null) {
            return trailingMinutiaeList;
        }
        int trailingMinutiaeStartPos = this.position() + internalNode.widthWithLeadingMinutiae();
        trailingMinutiaeList = new MinutiaeList(this, internalNode.trailingMinutiae(), trailingMinutiaeStartPos);
        return trailingMinutiaeList;
    }

    public Token modify(MinutiaeList leadingMinutiae, MinutiaeList trailingMinutiae) {
        if (internalNode.leadingMinutiae() == leadingMinutiae.internalNode() &&
                internalNode.trailingMinutiae() == trailingMinutiae.internalNode()) {
            return this;
        } else {
            return NodeFactory.createToken(this.kind(), leadingMinutiae, trailingMinutiae);
        }
    }

    @Override
    public Iterable<Diagnostic> diagnostics() {
        if (!internalNode.hasDiagnostics()) {
            return Collections::emptyIterator;
        }

        return () -> internalNode.diagnostics().stream()
                .map(this::createSyntaxDiagnostic)
                .iterator();
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(NodeTransformer<T> visitor) {
        return visitor.transform(this);
    }
}
