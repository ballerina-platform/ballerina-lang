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
package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.clauses.OrderKeyNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

/**
 * Implementation of order keys specified in "order by" clause statement.
 *
 * @since Swan Lake
 */
public class BLangOrderKey extends BLangNode implements OrderKeyNode {
    public BLangExpression expression;
    public boolean isAscending = true;

    public BLangOrderKey() {

    }

    @Override
    public NodeKind getKind() {
        return NodeKind.ORDER_KEY;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setOrderKey(ExpressionNode orderVar) {
        this.expression = (BLangExpression) orderVar;
    }

    @Override
    public ExpressionNode getOrderKey() {
        return expression;
    }

    @Override
    public void setOrderDirection(boolean isAscending) {
        if (!isAscending) {
            this.isAscending = false;
        }
    }

    @Override
    public boolean getOrderDirection() {
        return isAscending;
    }

    @Override
    public String toString() {
        return expression + " " + isAscending;
    }
}
