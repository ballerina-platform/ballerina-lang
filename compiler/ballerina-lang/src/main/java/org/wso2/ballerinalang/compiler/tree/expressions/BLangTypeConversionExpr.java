/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.TypeConversionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @since 0.94
 */
public class BLangTypeConversionExpr extends BLangExpression implements TypeConversionNode {

    public BLangExpression expr;
    public BLangType typeNode;
    public BType targetType;
    public List<BLangAnnotationAttachment> annAttachments = new ArrayList<>();
    public Set<Flag> flagSet = EnumSet.noneOf(Flag.class);
    public boolean checkTypes = true;

    public ExpressionNode getExpression() {
        return expr;
    }

    public void setExpression(ExpressionNode expr) {
        this.expr = (BLangExpression) expr;
    }

    public BLangType getTypeNode() {
        return typeNode;
    }

    public void setTypeNode(TypeNode typeNode) {
        this.typeNode = (BLangType) typeNode;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TYPE_CONVERSION_EXPR;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("<")
                .append(annAttachments.isEmpty() ? "" : attachmentsToString())
                .append(targetType != null ? targetType.toString() : "")
                .append("> ")
                .append(String.valueOf(expr))
                .toString();
    }

    private String attachmentsToString() {
        return annAttachments.stream().map(a -> "@" + a.getAnnotationName()).collect(Collectors.joining(", "));
    }

    @Override
    public Set<? extends Flag> getFlags() {
        return flagSet;
    }

    @Override
    public void addFlag(Flag flag) {
        flagSet.add(flag);
    }

    @Override
    public List<? extends AnnotationAttachmentNode> getAnnotationAttachments() {
        return annAttachments;
    }

    @Override
    public void addAnnotationAttachment(AnnotationAttachmentNode annAttachment) {
        annAttachments.add((BLangAnnotationAttachment) annAttachment);
    }
}
