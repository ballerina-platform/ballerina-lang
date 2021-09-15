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

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.expressions.BinaryExpressionNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeModifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * Implementation of {@link BinaryExpressionNode}.
 *
 * @since 0.94
 */
public class BLangBinaryExpr extends BLangExpression implements BinaryExpressionNode {

    public BLangExpression lhsExpr;
    public BLangExpression rhsExpr;
    public OperatorKind opKind;
    public BOperatorSymbol opSymbol;

    @Override
    public BLangExpression getLeftExpression() {
        return lhsExpr;
    }

    @Override
    public BLangExpression getRightExpression() {
        return rhsExpr;
    }

    @Override
    public OperatorKind getOperatorKind() {
        return opKind;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.BINARY_EXPR;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R apply(BLangNodeModifier<T, R> modifier, T props) {
        return modifier.modify(this, props);
    }

    @Override
    public String toString() {
        return String.valueOf(lhsExpr) + " " + String.valueOf(opKind) + " " + String.valueOf(rhsExpr);
    }
}
