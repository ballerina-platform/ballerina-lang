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

package org.wso2.ballerinalang.compiler.desugar;

import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.clauses.WhereNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectExpression;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangTableQuery;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhere;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Iterator;
import java.util.List;

/**
 * This class will generate the SQL query for stream/tables SQLish grammar for different classes.
 */

public class SqlQueryBuilder extends BLangNodeVisitor {
    private static final CompilerContext.Key<SqlQueryBuilder> SQL_QUERY_BUILDER_KEY =
            new CompilerContext.Key<>();

    private String varRef;
    private StringBuilder orderByClause;
    private String binaryExpr;
    private StringBuilder whereClause;
    private StringBuilder joinStreamingInputClause;
    private StringBuilder streamingInputClause;
    private StringBuilder selectExprClause;
    private StringBuilder selectExpr;
    private StringBuilder groupByClause;
    private StringBuilder havingClause;
    private StringBuilder sqlTableQuery;

    public static SqlQueryBuilder getInstance(CompilerContext context) {
        SqlQueryBuilder sqlQueryBuilder = context.get(SQL_QUERY_BUILDER_KEY);
        if (sqlQueryBuilder == null) {
            sqlQueryBuilder = new SqlQueryBuilder(context);
        }

        return sqlQueryBuilder;
    }

    private SqlQueryBuilder(CompilerContext context) {
        context.put(SQL_QUERY_BUILDER_KEY, this);
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {
        BLangTableQuery tableQuery = ((BLangTableQuery) tableQueryExpression.getTableQuery());
        tableQuery.accept(this);
        tableQueryExpression.setSqlQuery(tableQuery.getSqlQuery());

    }

    @Override
    public void visit(BLangTableQuery tableQuery) {
        BLangSelectClause selectClause = (BLangSelectClause) tableQuery.getSelectClauseNode();
        BLangStreamingInput streamingInput = (BLangStreamingInput) tableQuery.getStreamingInput();
        BLangJoinStreamingInput joinStreamingInput = (BLangJoinStreamingInput) tableQuery.getJoinStreamingInput();
        BLangOrderBy orderBy = (BLangOrderBy) tableQuery.getOrderByNode();

        sqlTableQuery = new StringBuilder();
        selectClause.accept(this);
        sqlTableQuery.append(selectExprClause).append(" from ");
        streamingInput.accept(this);
        sqlTableQuery.append(streamingInputClause);

        if (joinStreamingInput != null) {
            sqlTableQuery.append(" ");
            joinStreamingInput.accept(this);
            sqlTableQuery.append(joinStreamingInputClause);
        }
        if (selectClause.getGroupBy() != null) {
            sqlTableQuery.append(" ").append(groupByClause);
        }
        if (selectClause.getHaving() != null) {
            sqlTableQuery.append(" ").append(havingClause);
        }
        if (orderBy != null) {
            orderBy.accept(this);
            sqlTableQuery.append(" ").append(orderByClause);
        }

        tableQuery.setSqlQuery(sqlTableQuery.toString());
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        List<? extends ExpressionNode> varRefs = orderBy.getVariables();
        Iterator<? extends ExpressionNode> iterator = varRefs.iterator();
        BLangSimpleVarRef variableRef = (BLangSimpleVarRef) iterator.next();
        orderByClause = new StringBuilder("order by ");
        variableRef.accept(this);
        orderByClause.append(varRef);
        while (iterator.hasNext()) {
            orderByClause.append(",").append(" ");
            variableRef = (BLangSimpleVarRef) iterator.next();
            variableRef.accept(this);
            orderByClause.append(varRef);
        }
    }

    @Override
    public void visit(BLangSimpleVarRef variableReference) {
        varRef = variableReference.getVariableName().value;
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        varRef = literalExpr.toString();
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        BLangBinaryExpr expr = (BLangBinaryExpr) joinStreamingInput.getOnExpression();
        BLangStreamingInput streamingInput = (BLangStreamingInput) joinStreamingInput.getStreamingInput();
        joinStreamingInputClause = new StringBuilder();
        streamingInput.accept(this);
        joinStreamingInputClause.append("join ").append(streamingInputClause).append(" on ");
        expr.accept(this);
        joinStreamingInputClause.append(binaryExpr);
    }

    @Override
    public void visit(BLangBinaryExpr expr) {
        binaryExpr = expr.toString();
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        streamingInputClause = new StringBuilder();
        streamingInputClause.append(streamingInput.getIdentifier());
        List<? extends WhereNode> whereNodes = streamingInput.getStreamingConditions();

        /* for tables there can only be one whereClause and there is no windowClause.
         So we don't care about the windowClause. */
        if (whereNodes != null && !whereNodes.isEmpty()) {
            BLangWhere where = (BLangWhere) whereNodes.get(0);
            where.accept(this);
            streamingInputClause.append(" ").append(whereClause);
        }
        if (streamingInput.getAlias() != null) {
            streamingInputClause.append(" as ").append(streamingInput.getAlias());
        }
    }

    @Override
    public void visit(BLangWhere where) {
        whereClause = new StringBuilder();
        whereClause.append("where ");
        BLangBinaryExpr expr = (BLangBinaryExpr) where.getExpression();
        expr.accept(this);
        whereClause.append(binaryExpr);
    }

    @Override
    public void visit(BLangSelectClause select) {
        createSQLSelectExpressionClause(select);
        if (select.getGroupBy() != null) {
            createSQLGroupByClause(select);
        }
        if (select.getHaving() != null) {
            createSQLHavingClause(select);
        }
    }

    private void createSQLHavingClause(BLangSelectClause select) {
        BLangHaving having = (BLangHaving) select.getHaving();
        having.accept(this);
    }

    @Override
    public void visit(BLangHaving having) {
        BLangBinaryExpr expr = (BLangBinaryExpr) having.getExpression();
        havingClause = new StringBuilder("having ");
        expr.accept(this);
        havingClause.append(binaryExpr);
    }

    private void createSQLGroupByClause(BLangSelectClause select) {
        BLangGroupBy groupBy = (BLangGroupBy) select.getGroupBy();
        groupBy.accept(this);
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        List<? extends ExpressionNode> varList = groupBy.getVariables();
        Iterator<? extends  ExpressionNode> iterator = varList.iterator();
        groupByClause = new StringBuilder("group by ");
        BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) iterator.next();
        simpleVarRef.accept(this);
        groupByClause.append(varRef);
        while (iterator.hasNext()) {
            simpleVarRef = (BLangSimpleVarRef) iterator.next();
            groupByClause.append(", ");
            simpleVarRef.accept(this);
            groupByClause.append(varRef);
        }
    }

    private void createSQLSelectExpressionClause(BLangSelectClause select) {
        List<? extends SelectExpressionNode> selectExprList = select.getSelectExpressions();
        selectExprClause = new StringBuilder();
        selectExprClause.append("select ");
        if (selectExprList != null && !selectExprList.isEmpty()) {
            Iterator<? extends SelectExpressionNode> iterator = selectExprList.iterator();
            BLangSelectExpression selectExpression = (BLangSelectExpression) iterator.next();
            selectExpression.accept(this);
            selectExprClause.append(selectExpr);
            while (iterator.hasNext()) {
                selectExpression = (BLangSelectExpression) iterator.next();
                selectExprClause.append(", ");
                selectExpression.accept(this);
                selectExprClause.append(selectExpr);
            }
        } else if (select.isSelectAll()) {
            selectExprClause.append("* ");
        }
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
        BLangExpression expr = (BLangExpression) selectExpression.getExpression();
        selectExpr = new StringBuilder();
        expr.accept(this);
        selectExpr.append(varRef);
        String identifier  =selectExpression.getIdentifier();
        if (identifier != null) {
            selectExpr.append(" as ").append(identifier);
        }
    }

    public String getSQLTableQuery() {
        return sqlTableQuery.toString();
    }
}
