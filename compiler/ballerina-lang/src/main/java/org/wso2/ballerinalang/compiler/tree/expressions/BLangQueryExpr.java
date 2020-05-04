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

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.QueryExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnConflictClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link QueryExpressionNode}.
 *
 * @since 1.2.0
 */
public class BLangQueryExpr extends BLangExpression implements QueryExpressionNode {

    public List<BLangFromClause> fromClauseList = new ArrayList<>();
    public BLangOnClause onClause;
    public BLangSelectClause selectClause;
    public List<BLangWhereClause> whereClauseList = new ArrayList<>();
    public List<BLangLetClause> letClausesList = new ArrayList<>();
    public BLangOnConflictClause onConflictClause;
    public List<BLangNode> queryClauseList = new ArrayList<>();
    public boolean isStream = false;

    @Override
    public BLangSelectClause getSelectClause() {
        for (BLangNode clause : queryClauseList) {
            if (clause.getKind() == NodeKind.SELECT) {
                return (BLangSelectClause) clause;
            }
        }
        return null;
    }

    @Override
    public List<BLangNode> getQueryClauses() {
        return queryClauseList;
    }

    @Override
    public void addQueryClause(BLangNode queryClause) {
        this.queryClauseList.add(queryClause);
    }

    @Override
    public boolean isStream() {
        return isStream;
    }

    @Override
    public void setIsStream(boolean isStream) {
        this.isStream = isStream;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.QUERY_EXPR;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return queryClauseList.stream().map(BLangNode::toString).collect(Collectors.joining("\n"));
    }
}
