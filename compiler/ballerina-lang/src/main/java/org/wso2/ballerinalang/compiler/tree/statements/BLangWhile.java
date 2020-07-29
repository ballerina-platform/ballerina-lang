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

import org.ballerinalang.model.clauses.OnFailClauseNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.BlockStatementNode;
import org.ballerinalang.model.tree.statements.WhileNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

/**
 * @since 0.94
 */
public class BLangWhile extends BLangStatement implements WhileNode {

    public BLangExpression expr;
    public BLangBlockStmt body;
    public BLangOnFailClause onFailClause;

    public BLangWhile() {
    }

    public BLangWhile(BLangExpression expr, BLangBlockStmt body) {
        this.expr = expr;
        this.body = body;
    }

    @Override
    public BLangExpression getCondition() {
        return expr;
    }

    @Override
    public BLangBlockStmt getBody() {
        return body;
    }

    @Override
    public void setCondition(ExpressionNode condition) {
        this.expr = (BLangExpression) condition;
    }

    @Override
    public void setBody(BlockStatementNode body) {
        this.body = (BLangBlockStmt) body;
    }

    @Override
    public OnFailClauseNode getOnFailClause() {
        return this.onFailClause;
    }

    @Override
    public void setOnFailClause(OnFailClauseNode onFailClause) {
        this.onFailClause = (BLangOnFailClause) onFailClause;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.WHILE;
    }

    @Override
    public String toString() {
        return "While: (" + expr + ") "
                + (body != null ? " {" + String.valueOf(body) + "}" : "");
    }
}
