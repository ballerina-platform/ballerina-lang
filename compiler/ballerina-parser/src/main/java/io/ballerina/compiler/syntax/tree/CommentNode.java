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
import io.ballerina.compiler.internal.parser.tree.STNodeDiagnostic;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a comment. This is not a part of the Ballerina syntax tree.
 *
 * @since 2201.10.0
 */
public class CommentNode extends NonTerminalNode {
    protected MinutiaeList leadingMinutiaeList;
    protected MinutiaeList trailingMinutiaeList;
    private Node commentAttachedNode;
    private Minutiae lastMinutiae;
    private List<String> commentLines;

    public CommentNode(STNode commentAttachedSTNode, int position, NonTerminalNode commentAttachedNode) {
        super(commentAttachedSTNode, position, commentAttachedNode);
    }

    public Node getCommentAttachedNode() {
        return this.commentAttachedNode;
    }

    public void setCommentAttachedNode(Node commentAttachedNode) {
        this.commentAttachedNode = commentAttachedNode;
    }

    public Minutiae getLastMinutiae() {
        return this.lastMinutiae;
    }

    public void setLastMinutiae(Minutiae lastMinutiae) {
        this.lastMinutiae = lastMinutiae;
    }

    public String text() {
        return ((STToken) this.internalNode).text();
    }

    public List<String> getCommentLines() {
        return this.commentLines;
    }

    public void setCommentLines(List<String> commentLines) {
        this.commentLines = commentLines;
    }

    public boolean containsLeadingMinutiae() {
        return internalNode.leadingMinutiae().bucketCount() > 0;
    }

    @Override
    public MinutiaeList leadingMinutiae() {
        if (leadingMinutiaeList != null) {
            return leadingMinutiaeList;
        }
        // TODO: add proper node
        leadingMinutiaeList = new MinutiaeList(null, internalNode.leadingMinutiae(), this.position());
        return leadingMinutiaeList;
    }

    public boolean containsTrailingMinutiae() {
        return internalNode.trailingMinutiae().bucketCount() > 0;
    }

    @Override
    public MinutiaeList trailingMinutiae() {
        if (trailingMinutiaeList != null) {
            return trailingMinutiaeList;
        }
        int trailingMinutiaeStartPos = this.position() + internalNode.widthWithLeadingMinutiae();
        trailingMinutiaeList = new MinutiaeList(null, internalNode.trailingMinutiae(), trailingMinutiaeStartPos);
        return trailingMinutiaeList;
    }

//    public CommentNode modify(MinutiaeList leadingMinutiae, MinutiaeList trailingMinutiae) {
//        if (internalNode.leadingMinutiae() == leadingMinutiae.internalNode() &&
//                internalNode.trailingMinutiae() == trailingMinutiae.internalNode()) {
//            return this;
//        } else {
//            return NodeFactory.createToken(this.kind(), leadingMinutiae, trailingMinutiae);
//        }
//    }

    @Override
    public Iterable<Diagnostic> diagnostics() {
        if (!internalNode.hasDiagnostics()) {
            return Collections::emptyIterator;
        }

        return () -> collectDiagnostics().iterator();
    }

    @Override
    protected String[] childNames() {
        return new String[0];
    }

    private List<Diagnostic> collectDiagnostics() {
        List<Diagnostic> diagnosticList = new ArrayList<>();

        // Collect diagnostics inside invalid token minutiae
        leadingInvalidTokens().forEach(token -> token.diagnostics().forEach(diagnosticList::add));
        trailingInvalidTokens().forEach(token -> token.diagnostics().forEach(diagnosticList::add));

        // Collect token diagnostics
        for (STNodeDiagnostic stNodeDiagnostic : internalNode.diagnostics()) {
            Diagnostic syntaxDiagnostic = createSyntaxDiagnostic(stNodeDiagnostic);
            diagnosticList.add(syntaxDiagnostic);
        }

        return diagnosticList;
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
