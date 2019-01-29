/*
*   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.TypeTestExpressionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;

/**
 * Represents a type test expression in Ballerina.
 * Syntax: x is T
 * 
 * @since 0.985.0
 */
public class BLangTypeTestExpr extends BLangExpression implements TypeTestExpressionNode {

    public BLangExpression expr;
    public BLangType typeNode;

    @Override
    public BLangExpression getExpression() {
        return expr;
    }

    @Override
    public void setExpression(ExpressionNode expr) {
        this.expr = (BLangExpression) expr;
    }

    @Override
    public BLangType getTypeNode() {
        return typeNode;
    }

    @Override
    public void setTypeNode(TypeNode typeNode) {
        this.typeNode = (BLangType) typeNode;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TYPE_TEST_EXPR;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.valueOf(expr) + " is " + String.valueOf(typeNode);
    }
}
