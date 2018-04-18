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
import org.ballerinalang.model.tree.clauses.JoinStreamingInput;
import org.ballerinalang.model.tree.clauses.LimitNode;
import org.ballerinalang.model.tree.clauses.OrderByNode;
import org.ballerinalang.model.tree.clauses.SelectClauseNode;
import org.ballerinalang.model.tree.clauses.StreamingInput;
import org.ballerinalang.model.tree.clauses.TableQuery;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.965.0
 *
 * The implementation of {@link TableQuery}.
 */
public class BLangTableQuery extends BLangNode implements TableQuery {

    private StreamingInput streamingInput;
    private JoinStreamingInput joinStreamingInput;
    private SelectClauseNode selectClauseNode;
    private OrderByNode orderByNode;
    private LimitNode limitNode;

    //This will be generated in desugar phase
    private String sqlQuery;
    private List<BLangExpression> params = new ArrayList<>();

    @Override
    public void setStreamingInput(StreamingInput streamingInput) {
        this.streamingInput = streamingInput;
    }

    @Override
    public StreamingInput getStreamingInput() {
        return this.streamingInput;
    }

    @Override
    public void setJoinStreamingInput(JoinStreamingInput joinStreamingInput) {
        this.joinStreamingInput = joinStreamingInput;
    }

    @Override
    public JoinStreamingInput getJoinStreamingInput() {
        return this.joinStreamingInput;
    }

    @Override
    public void setSelectClause(SelectClauseNode selectClause) {
        this.selectClauseNode = selectClause;
    }

    @Override
    public SelectClauseNode getSelectClauseNode() {
        return this.selectClauseNode;
    }

    @Override
    public void setOrderByClause(OrderByNode orderByClause) {
        this.orderByNode = orderByClause;
    }

    @Override
    public OrderByNode getOrderByNode() {
        return this.orderByNode;
    }

    @Override
    public void setLimitClause(LimitNode limitClause) {
        this.limitNode = limitClause;
    }

    @Override
    public LimitNode getLimitClause() {
        return limitNode;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TABLE_QUERY;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public List<BLangExpression> getParams() {
        return params;
    }

    public void addParams(List<BLangExpression> params) {
        this.params.addAll(params);
    }
}
