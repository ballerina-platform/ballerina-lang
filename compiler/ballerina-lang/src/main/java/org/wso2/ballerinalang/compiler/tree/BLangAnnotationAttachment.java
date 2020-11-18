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

import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @since 0.94
 */
public class BLangAnnotationAttachment extends BLangNode implements AnnotationAttachmentNode {

    public BLangExpression expr;
    public BLangIdentifier annotationName;
    public Set<AttachPoint.Point> attachPoints;
    public BLangIdentifier pkgAlias;
    public BAnnotationSymbol annotationSymbol;

    public BLangAnnotationAttachment() {
        attachPoints = new LinkedHashSet<>();
    }

    @Override
    public IdentifierNode getPackageAlias() {
        return pkgAlias;
    }

    @Override
    public void setPackageAlias(IdentifierNode pkgAlias) {
        this.pkgAlias = (BLangIdentifier) pkgAlias;
    }

    @Override
    public IdentifierNode getAnnotationName() {
        return annotationName;
    }

    @Override
    public void setAnnotationName(IdentifierNode name) {
        this.annotationName = (BLangIdentifier) name;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.ANNOTATION_ATTACHMENT;
    }
    
    @Override
    public String toString() {
        return "BLangAnnotationAttachment: " + annotationName;
    }

    @Override
    public BLangExpression getExpression() {
        return expr;
    }

    @Override
    public void setExpression(ExpressionNode expression) {
        this.expr = (BLangExpression) expression;
    }

}
