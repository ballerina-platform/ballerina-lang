/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.MarkdownDocumentationNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 */
public abstract class BLangVariable extends BLangNode implements VariableNode {

    // BLangNodes
    public BLangType typeNode;
    public List<BLangAnnotationAttachment> annAttachments;
    public BLangMarkdownDocumentation markdownDocumentationAttachment;
    public BLangExpression expr;

    // Parser Flags
    public Set<Flag> flagSet;
    public boolean isDeclaredWithVar = false;

    // Semantic Data
    public BVarSymbol symbol;

    public BLangVariable() {
        this.annAttachments = new ArrayList<>();
        this.flagSet = EnumSet.noneOf(Flag.class);
    }

    @Override
    public BLangType getTypeNode() {
        return typeNode;
    }

    @Override
    public BLangExpression getInitialExpression() {
        return expr;
    }

    @Override
    public Set<Flag> getFlags() {
        return flagSet;
    }

    @Override
    public List<BLangAnnotationAttachment> getAnnotationAttachments() {
        return annAttachments;
    }

    @Override
    public void addFlag(Flag flag) {
        this.flagSet.add(flag);
    }

    @Override
    public void addAnnotationAttachment(AnnotationAttachmentNode annAttachment) {
        this.getAnnotationAttachments().add((BLangAnnotationAttachment) annAttachment);
    }

    @Override
    public BLangMarkdownDocumentation getMarkdownDocumentationAttachment() {
        return markdownDocumentationAttachment;
    }

    @Override
    public void setMarkdownDocumentationAttachment(MarkdownDocumentationNode documentationNode) {
        this.markdownDocumentationAttachment = (BLangMarkdownDocumentation) documentationNode;
    }

    @Override
    public void setTypeNode(TypeNode typeNode) {
        this.typeNode = (BLangType) typeNode;
    }

    @Override
    public void setInitialExpression(ExpressionNode expr) {
        this.expr = (BLangExpression) expr;
    }

    @Override
    public String toString() {
        return String.valueOf(getBType()) + " " + (expr != null ? " = " + String.valueOf(expr) : "");
    }
}
