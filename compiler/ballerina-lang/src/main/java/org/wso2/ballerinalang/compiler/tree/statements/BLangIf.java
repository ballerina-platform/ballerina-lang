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
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.IfNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

/**
 * @since 0.94
 */
public class BLangIf extends BLangStatement implements IfNode {
    public BLangExpression expr;
    public BLangBlockStmt body;
    public BLangStatement elseStmt;

    @Override
    public BLangExpression getCondition() {
        return expr;
    }

    @Override
    public BLangBlockStmt getBody() {
        return body;
    }

    @Override
    public BLangStatement getElseStatement() {
        return elseStmt;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.IF;
    }

    @Override
    public void setCondition(ExpressionNode condition) {
        this.expr = (BLangExpression) condition;
    }

    @Override
    public void setBody(BlockNode body) {
        this.body = (BLangBlockStmt) body;
    }

    @Override
    public void setElseStatement(StatementNode elseStatement) {
        this.elseStmt = (BLangStatement) elseStatement;
    }

    @Override
    public String toString() {
        return "if (" + expr + ") " + body + (elseStmt != null ? (" else " + elseStmt) : "");
    }
}
