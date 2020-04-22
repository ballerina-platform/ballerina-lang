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
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 1.3.0
 */
public class STAnnotationDeclarationNode extends STModuleMemberDeclarationNode {
    public final STNode metadata;
    public final STNode visibilityQualifier;
    public final STNode constKeyword;
    public final STNode annotationKeyword;
    public final STNode typeDescriptor;
    public final STNode annotationTag;
    public final STNode onKeyword;
    public final STNode attachPoints;
    public final STNode semicolonToken;

    STAnnotationDeclarationNode(
            STNode metadata,
            STNode visibilityQualifier,
            STNode constKeyword,
            STNode annotationKeyword,
            STNode typeDescriptor,
            STNode annotationTag,
            STNode onKeyword,
            STNode attachPoints,
            STNode semicolonToken) {
        super(SyntaxKind.ANNOTATION_DECLARATION);
        this.metadata = metadata;
        this.visibilityQualifier = visibilityQualifier;
        this.constKeyword = constKeyword;
        this.annotationKeyword = annotationKeyword;
        this.typeDescriptor = typeDescriptor;
        this.annotationTag = annotationTag;
        this.onKeyword = onKeyword;
        this.attachPoints = attachPoints;
        this.semicolonToken = semicolonToken;

        addChildren(
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

    public Node createFacade(int position, NonTerminalNode parent) {
        return new AnnotationDeclarationNode(this, position, parent);
    }
}
