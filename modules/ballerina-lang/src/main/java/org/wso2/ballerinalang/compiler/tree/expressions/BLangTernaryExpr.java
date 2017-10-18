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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.TernaryExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * @since 0.94
 */
public class BLangTernaryExpr extends BLangExpression implements TernaryExpressionNode {

    public BLangExpression expr;
    public BLangExpression thenExpr;
    public BLangExpression elseExpr;

    @Override
    public ExpressionNode getCondition() {
        return expr;
    }

    @Override
    public ExpressionNode getThenExpression() {
        return thenExpr;
    }

    @Override
    public ExpressionNode getElseExpression() {
        return elseExpr;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TERNARY_EXPR;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.valueOf(expr) + "?" + String.valueOf(thenExpr) + ":" + String.valueOf(elseExpr);
    }
}
