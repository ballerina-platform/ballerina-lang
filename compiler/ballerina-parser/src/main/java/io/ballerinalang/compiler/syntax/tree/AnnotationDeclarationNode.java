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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;

/**
 * This is a generated syntax tree node.
 *
 * @since 1.3.0
 */
public class AnnotationDeclarationNode extends ModuleMemberDeclarationNode {

    public AnnotationDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public MetadataNode metadata() {
        return childInBucket(0);
    }

    public Token visibilityQualifier() {
        return childInBucket(1);
    }

    public Token constKeyword() {
        return childInBucket(2);
    }

    public Token annotationKeyword() {
        return childInBucket(3);
    }

    public Node typeDescriptor() {
        return childInBucket(4);
    }

    public Token annotationTag() {
        return childInBucket(5);
    }

    public Token onKeyword() {
        return childInBucket(6);
    }

    public SeparatedNodeList<Node> attachPoints() {
        return new SeparatedNodeList<>(childInBucket(7));
    }

    public Token semicolonToken() {
        return childInBucket(8);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(NodeTransformer<T> visitor) {
        return visitor.transform(this);
    }

    @Override
    protected String[] childNames() {
        return new String[]{
                "metadata",
                "visibilityQualifier",
                "constKeyword",
                "annotationKeyword",
                "typeDescriptor",
                "annotationTag",
                "onKeyword",
                "attachPoints",
                "semicolonToken"};
    }

    public AnnotationDeclarationNode modify(
            MetadataNode metadata,
            Token visibilityQualifier,
            Token constKeyword,
            Token annotationKeyword,
            Node typeDescriptor,
            Token annotationTag,
            Token onKeyword,
            SeparatedNodeList<Node> attachPoints,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                visibilityQualifier,
                constKeyword,
                annotationKeyword,
                typeDescriptor,
                annotationTag,
                onKeyword,
                attachPoints.underlyingListNode(),
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createAnnotationDeclarationNode(
                metadata,
                visibilityQualifier,
                constKeyword,
                annotationKeyword,
                typeDescriptor,
                annotationTag,
                onKeyword,
                attachPoints,
                semicolonToken);
    }
}
