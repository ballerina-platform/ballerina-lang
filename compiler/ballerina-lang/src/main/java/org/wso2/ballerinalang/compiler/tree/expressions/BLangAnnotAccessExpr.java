/*
*   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.AnnotAccessNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * @since 1.0
 */
public class BLangAnnotAccessExpr extends BLangAccessExpression implements AnnotAccessNode {

    public BLangIdentifier pkgAlias;
    public BLangIdentifier annotationName;
    public BAnnotationSymbol annotationSymbol;

    @Override
    public String toString() {
        return String.valueOf(expr) + ".@" + annotationSymbol.bvmAlias();
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.ANNOT_ACCESS_EXPRESSION;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExpressionNode getExpression() {
        return expr;
    }

    @Override
    public void setExpression(ExpressionNode expr) {
        this.expr = (BLangExpression) expr;
    }

    @Override
    public IdentifierNode getPackageAlias() {
        return this.pkgAlias;
    }

    @Override
    public void setPackageAlias(IdentifierNode pkgAlias) {
        this.pkgAlias = (BLangIdentifier) pkgAlias;
    }

    @Override
    public IdentifierNode getAnnotationName() {
        return this.annotationName;
    }

    @Override
    public void setAnnotationName(IdentifierNode name) {
        this.annotationName = (BLangIdentifier) name;
    }
}
