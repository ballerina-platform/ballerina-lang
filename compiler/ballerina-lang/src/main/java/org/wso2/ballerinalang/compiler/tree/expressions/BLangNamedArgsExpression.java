/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.NamedArgNode;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * Implementation of var args expression.
 * eg: ...x
 *
 * @since 0.965.0
 */
public class BLangNamedArgsExpression extends BLangExpression implements NamedArgNode {

    public BLangIdentifier name;
    public BLangExpression expr;

    @Override
    public BLangIdentifier getName() {
        return name;
    }

    @Override
    public void setName(IdentifierNode name) {
        this.name = (BLangIdentifier) name;
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
    public NodeKind getKind() {
        return NodeKind.NAMED_ARGS_EXPR;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return name + "=" + expr;
    }
}
