/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.List;

/**
 * Represents a comment. This is not a part of the Ballerina syntax tree.
 *
 * @since 2201.10.0
 */
public class CommentNode extends NonTerminalNode {
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

    public List<String> getCommentLines() {
        return this.commentLines;
    }

    public void setCommentLines(List<String> commentLines) {
        this.commentLines = commentLines;
    }

    @Override
    protected String[] childNames() {
        return new String[0];
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
