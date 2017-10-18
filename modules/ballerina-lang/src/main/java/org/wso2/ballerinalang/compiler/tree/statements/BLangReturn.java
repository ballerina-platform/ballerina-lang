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
package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.ReturnNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.94
 */
public class BLangReturn extends BLangStatement implements ReturnNode {
    public List<BLangExpression> exprs;

    // Hold variable for Desuger phase, if this a named return.
    public List<BLangVariable> namedReturnVariables;

    public BLangReturn() {
        this.exprs = new ArrayList<>();
    }

    public BLangReturn(List<BLangExpression> exprs) {
        this.exprs = exprs;
    }

    @Override
    public List<BLangExpression> getExpressions() {
        return exprs;
    }

    @Override
    public void addExpression(ExpressionNode expressionNode) {
        this.exprs.add((BLangExpression) expressionNode);
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.RETURN;
    }

    @Override
    public String toString() {
        return "Return: " +  (this.exprs != null ? this.exprs : "");
    }

    /**
     * Represents return statement inside a worker.
     *
     * @since 0.94
     */
    public static class BLangWorkerReturn extends BLangReturn {

        public BLangWorkerReturn(List<BLangExpression> exprs) {
            this.exprs = exprs;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }
}
