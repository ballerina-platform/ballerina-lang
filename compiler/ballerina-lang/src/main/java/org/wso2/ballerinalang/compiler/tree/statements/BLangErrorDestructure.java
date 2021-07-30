/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.ErrorDestructureNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

/**
 * Implementation of {@link ErrorDestructureNode}.
 *
 * @since 0.985.0
 */
public class BLangErrorDestructure extends BLangStatement implements ErrorDestructureNode {
    public BLangErrorVarRef varRef;
    public BLangExpression expr;

    @Override
    public BLangErrorVarRef getVarRef() {
        return this.varRef;
    }

    @Override
    public BLangExpression getExpression() {
        return this.expr;
    }

    @Override
    public void setExpression(ExpressionNode expression) {
        this.expr = (BLangExpression) expression;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.ERROR_DESTRUCTURE;
    }
}
