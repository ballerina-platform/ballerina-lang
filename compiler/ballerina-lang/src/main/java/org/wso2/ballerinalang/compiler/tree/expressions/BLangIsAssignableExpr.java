/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.expressions.IsAssignableNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;

/**
 * {@code IsAssignableNode} represents the 'isassignable' binary expression.
 * e.g.
 * boolean v = a isassignable json
 *
 * @since 0.967.0
 */
public class BLangIsAssignableExpr extends BLangExpression implements IsAssignableNode {
    public final OperatorKind opKind = OperatorKind.IS_ASSIGNABLE;
    public BLangExpression lhsExpr;
    public BType targetType;
    public BLangType typeNode;
    public BOperatorSymbol opSymbol;

    @Override
    public BLangExpression getLeftExpression() {
        return lhsExpr;
    }

    @Override
    public BLangType getTypeNode() {
        return typeNode;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.IS_ASSIGNABLE_EXPR;
    }

    @Override
    public String toString() {
        return String.valueOf(this.lhsExpr) + " " + this.opKind.value() + " " + this.targetType;
    }
}
