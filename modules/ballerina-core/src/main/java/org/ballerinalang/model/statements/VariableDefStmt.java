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
package org.ballerinalang.model.statements;

import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.expressions.Expression;

/**
 * {@code VariableDefStmt} represents variable definition statement in Ballerina.
 *
 * @since 0.8.0
 */
public class VariableDefStmt extends AbstractStatement {
    private VariableDef variableDef;
    private Expression lhrExpr;
    private Expression rhsExpr;

    public VariableDefStmt(NodeLocation location, VariableDef variableDef, Expression lExpr, Expression rExpr) {
        super(location);
        this.variableDef = variableDef;
        this.lhrExpr = lExpr;
        this.rhsExpr = rExpr;
    }

    public VariableDef getVariableDef() {
        return variableDef;
    }

    public Expression getLExpr() {
        return lhrExpr;
    }

    public Expression getRExpr() {
        return rhsExpr;
    }

    public void setRExpr(Expression rhsExpr) {
        this.rhsExpr = rhsExpr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void execute(NodeExecutor executor) {
        executor.visit(this);
    }

}
