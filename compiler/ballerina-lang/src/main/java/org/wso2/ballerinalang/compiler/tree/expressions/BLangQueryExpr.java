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

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.QueryExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link QueryExpressionNode}.
 *
 * @since 1.2.0
 */
public class BLangQueryExpr extends BLangExpression implements QueryExpressionNode {

    // BLangNodes
    public List<BLangNode> queryClauseList = new ArrayList<>();
    public List<IdentifierNode> fieldNameIdentifierList = new ArrayList<>();

    // Parser Flags and Data
    public boolean isStream = false;
    public boolean isTable = false;

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
    public boolean isTable() {
        return isTable;
    }

    @Override
    public void setIsTable(boolean isTable) {
        this.isTable = isTable;
    }

    @Override
    public void addFieldNameIdentifier(IdentifierNode fieldNameIdentifier) {
        fieldNameIdentifierList.add(fieldNameIdentifier);
    }

    @Override
    public List<IdentifierNode> getFieldNameIdentifierList() {
        return fieldNameIdentifierList;
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
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
        return modifier.transform(this, props);
    }

    @Override
    public String toString() {
        return queryClauseList.stream().map(BLangNode::toString).collect(Collectors.joining("\n"));
    }
}
