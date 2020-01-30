/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.clauses.FromClauseNode;
import org.ballerinalang.model.clauses.SelectClauseNode;
import org.ballerinalang.model.clauses.WhereClauseNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.QueryExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link QueryExpressionNode}.
 */
public class BLangQueryExpr extends BLangExpression implements QueryExpressionNode {

    public List<BLangFromClause> fromClauseList = new ArrayList<>();
    public BLangSelectClause selectClause;
    public BLangWhereClause whereClause;

    @Override
    public List<? extends FromClauseNode> getFromClauseNodes() {
        return fromClauseList;
    }

    @Override
    public void addFromClauseNode(FromClauseNode fromClauseNode) {
        fromClauseList.add((BLangFromClause) fromClauseNode);
    }

    @Override
    public SelectClauseNode getSelectClauseNode() {
        return selectClause;
    }

    @Override
    public void setSelectClauseNode(SelectClauseNode selectClauseNode) {
        this.selectClause = (BLangSelectClause) selectClauseNode;
    }

    @Override
    public WhereClauseNode getWhereClauseNode() {
        return whereClause;
    }

    @Override
    public void setWhereClauseNode(WhereClauseNode whereClauseNode) {
        this.whereClause = (BLangWhereClause) whereClauseNode;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.QUERY_EXPR;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

/*    @Override
    public String toString() {
        return String.valueOf(lhsExpr) + " " + String.valueOf(opKind) + " " + String.valueOf(rhsExpr);
    }*/
}
