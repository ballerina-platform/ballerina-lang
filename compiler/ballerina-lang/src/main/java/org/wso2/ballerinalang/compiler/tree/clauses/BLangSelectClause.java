/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.clauses.GroupByNode;
import org.ballerinalang.model.tree.clauses.HavingNode;
import org.ballerinalang.model.tree.clauses.SelectClauseNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.965.0
 *
 * Implementation of {@link SelectClauseNode}.
 */
public class BLangSelectClause extends BLangNode implements SelectClauseNode {

    public List<SelectExpressionNode> selectExpressions = new ArrayList<>();
    public boolean isSelectAll;
    public BLangGroupBy groupBy;
    public BLangHaving having;

    @Override
    public void setSelectExpressions(List<SelectExpressionNode> selectExpressions) {
        this.selectExpressions = selectExpressions;
    }

    @Override
    public List<SelectExpressionNode> getSelectExpressions() {
        return this.selectExpressions;
    }

    @Override
    public void setSelectAll(boolean isSelectAll) {
        this.isSelectAll = isSelectAll;
    }

    @Override
    public boolean isSelectAll() {
        return this.isSelectAll;
    }

    @Override
    public void setGroupBy(GroupByNode groupByNode) {
        this.groupBy = (BLangGroupBy) groupByNode;
    }

    @Override
    public GroupByNode getGroupBy() {
        return this.groupBy;
    }

    @Override
    public void setHaving(HavingNode havingNode) {
        this.having = (BLangHaving) havingNode;
    }

    @Override
    public HavingNode getHaving() {
        return this.having;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.SELECT_CLAUSE;
    }
}
