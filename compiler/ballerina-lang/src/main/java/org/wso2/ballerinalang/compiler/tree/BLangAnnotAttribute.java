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
import org.ballerinalang.model.tree.AnnotationAttributeNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 */
@Deprecated
public class BLangAnnotAttribute extends BLangNode implements AnnotationAttributeNode {

    public BLangType typeNode;
    public BLangIdentifier name;
    public BLangExpression expr;
    public Set<Flag> flags;
    public List<BLangAnnotationAttachment> annAttachments;

    public BSymbol symbol;

    public BLangAnnotAttribute() {
        this.annAttachments = new ArrayList<>();
        this.flags = EnumSet.noneOf(Flag.class);
    }

    @Override
    public BLangType getTypeNode() {
        return typeNode;
    }

    @Override
    public BLangIdentifier getName() {
        return name;
    }

    @Override
    public BLangExpression getInitialExpression() {
        return expr;
    }

    @Override
    public Set<Flag> getFlags() {
        return flags;
    }

    @Override
    public List<BLangAnnotationAttachment> getAnnotationAttachments() {
        return annAttachments;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void addFlag(Flag flag) {
        this.flags.add(flag);
    }

    @Override
    public void addAnnotationAttachment(AnnotationAttachmentNode annAttachment) {
        this.getAnnotationAttachments().add((BLangAnnotationAttachment) annAttachment);
    }

    @Override
    public void setTypeNode(TypeNode typeNode) {
        this.typeNode = (BLangType) typeNode;
    }

    @Override
    public void setName(IdentifierNode name) {
        this.name = (BLangIdentifier) name;
    }

    @Override
    public void setInitialExpression(ExpressionNode expr) {
        this.expr = (BLangExpression) expr;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.ANNOTATION_ATTRIBUTE;
    }

    @Override
    public String toString() {
        return "BLangAnnotAttribute: " + (this.getFlags().contains(Flag.FINAL) ? "const " : "") +
                (this.name != null ? this.name : "") + "[" + this.typeNode + "]" +
                (this.expr != null ? " = " + this.expr : "");
    }
}
