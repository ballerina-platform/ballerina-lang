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

import org.ballerinalang.model.clauses.*;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.QueryExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.clauses.*;

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
    public BLangSelectClause selectClause;
    public List<BLangWhereClause> whereClauseList = new ArrayList<>();
    public List<BLangLetClause> letClausesList = new ArrayList<>();
    public BLangOnConflictClause onConflictClause;

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
    public OnConflictClauseNode getOnConflictClauseNode() { return onConflictClause; }

    @Override
    public void setOnConflictClauseNode(OnConflictClauseNode onConflictClauseNode) {
        this.onConflictClause = (BLangOnConflictClause) onConflictClauseNode;
    }

    @Override
    public List<? extends BLangWhereClause> getWhereClauseNode() {
        return whereClauseList;
    }

    @Override
    public void addWhereClauseNode(WhereClauseNode whereClauseNode) {
        this.whereClauseList.add((BLangWhereClause) whereClauseNode);
    }

    @Override
    public List<? extends LetClauseNode> getLetClauseList() {
        return letClausesList;
    }

    @Override
    public void addLetClause(LetClauseNode letClauseNode) {
        letClausesList.add((BLangLetClause) letClauseNode);
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
        return fromClauseList.stream().map(BLangFromClause::toString).collect(Collectors.joining("\n")) + "\n"
                + letClausesList.stream().map(BLangLetClause::toString).collect(Collectors.joining("\n"))
                + "\n" +
                whereClauseList.stream().map(BLangWhereClause::toString).collect(Collectors.joining("\n"))
                + "\n" +
                selectClause.toString()
                + "\n" +
                onConflictClause.toString();
    }
}
