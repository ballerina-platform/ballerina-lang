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

package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.clauses.TableQuery;
import org.ballerinalang.model.tree.expressions.TableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.965.0
 *
 * The Implementation of {@link TableQueryExpression}.
 */
public class BLangTableQueryExpression extends BLangExpression implements TableQueryExpression {

    public TableQuery tableQuery;

    //This will be generated in desugar phase
    private String sqlQuery;
    private List<BLangExpression> params = new ArrayList<>();

    @Override
    public void setTableQuery(TableQuery tableQuery) {
        this.tableQuery = tableQuery;
    }

    @Override
    public TableQuery getTableQuery() {
        return this.tableQuery;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TABLE_QUERY_EXPRESSION;
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
