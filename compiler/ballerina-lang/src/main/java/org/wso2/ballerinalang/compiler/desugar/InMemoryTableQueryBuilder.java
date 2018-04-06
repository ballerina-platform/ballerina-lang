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

import org.ballerinalang.model.tree.clauses.WhereNode;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangTableQuery;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhere;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @since 0.970.0
 *
 * This class will generate the SQL query for stream/tables SQLish grammar for different classes.
 */

public class InMemoryTableQueryBuilder extends SqlQueryBuilder {
    private static final CompilerContext.Key<InMemoryTableQueryBuilder> SQL_QUERY_BUILDER_KEY =
            new CompilerContext.Key<>();

    private static final String QUESTION_MARK = "?";

    private List<BLangExpression> joinOnExprParams = new ArrayList<>();

    //Keep temporary parametrized sql strings and  temporary parameters for "?"
    private List<BLangExpression> exprParams = new ArrayList<>();
    private List<BLangExpression> whereExprParams = new ArrayList<>();

    public static InMemoryTableQueryBuilder getInstance(CompilerContext context) {
        InMemoryTableQueryBuilder inMemoryTableQueryBuilder = context.get(SQL_QUERY_BUILDER_KEY);
        if (inMemoryTableQueryBuilder == null) {
            inMemoryTableQueryBuilder = new InMemoryTableQueryBuilder(context);
        }

        return inMemoryTableQueryBuilder;
    }

    private InMemoryTableQueryBuilder(CompilerContext context) {
        context.put(SQL_QUERY_BUILDER_KEY, this);
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {
        orderByClause = null;
        whereClause = null;
        joinStreamingInputClause = null;
        streamingInputClause = null;
        selectExprClause = null;
        selectExpr = null;
        groupByClause = null;
        havingClause = null;

        selectExprParams = new ArrayList<>();
        havingExprParams = new ArrayList<>();
        joinOnExprParams = new ArrayList<>();

        exprParams = new ArrayList<>();
        whereExprParams = new ArrayList<>();
        exprStack = new Stack<>();

        BLangTableQuery tableQuery = ((BLangTableQuery) tableQueryExpression.getTableQuery());
        tableQuery.accept(this);
        tableQueryExpression.setSqlQuery(tableQuery.getSqlQuery());
        tableQueryExpression.addParams(tableQuery.getParams());
    }

    @Override
    public void visit(BLangTableQuery tableQuery) {
        BLangSelectClause selectClause = (BLangSelectClause) tableQuery.getSelectClauseNode();
        BLangStreamingInput streamingInput = (BLangStreamingInput) tableQuery.getStreamingInput();
        BLangJoinStreamingInput joinStreamingInput = (BLangJoinStreamingInput) tableQuery.getJoinStreamingInput();
        BLangOrderBy orderBy = (BLangOrderBy) tableQuery.getOrderByNode();
        StringBuilder sqlTableQuery = new StringBuilder();

        /*Add each clause to the sqlQuery and add the params to TableQuery object*/
        addSelectClauseAndParams(tableQuery, selectClause, sqlTableQuery);
        addFromClauseAndParams(tableQuery, streamingInput, sqlTableQuery);
        addJoinClauseAndParams(tableQuery, joinStreamingInput, sqlTableQuery);
        addGroupByClause(selectClause, sqlTableQuery);
        addHavingClauseAndParams(tableQuery, selectClause, sqlTableQuery);
        addOrderByClause(orderBy, sqlTableQuery);

        tableQuery.setSqlQuery(sqlTableQuery.toString());
    }

    @Override
    public void visit(BLangWhere where) {
        whereClause = new StringBuilder();
        whereClause.append("where ");
        BLangBinaryExpr expr = (BLangBinaryExpr) where.getExpression();
        addExprToClause(expr, whereClause, whereExprParams);
    }

    private void addOrderByClause(BLangOrderBy orderBy, StringBuilder sqlTableQuery) {
        if (orderBy != null) {
            orderBy.accept(this);
            sqlTableQuery.append(" ").append(orderByClause);
        }
    }

    private void addHavingClauseAndParams(BLangTableQuery tableQuery, BLangSelectClause selectClause,
                                          StringBuilder sqlTableQuery) {
        if (selectClause.getHaving() != null) {
            sqlTableQuery.append(" ").append(havingClause);
            tableQuery.addParams(havingExprParams);
        }
    }

    private void addGroupByClause(BLangSelectClause selectClause, StringBuilder sqlTableQuery) {
        if (selectClause.getGroupBy() != null) {
            sqlTableQuery.append(" ").append(groupByClause);
        }
    }

    private void addJoinClauseAndParams(BLangTableQuery tableQuery, BLangJoinStreamingInput joinStreamingInput,
                                        StringBuilder sqlTableQuery) {
        if (joinStreamingInput != null) {
            sqlTableQuery.append(" ");
            joinStreamingInput.accept(this);
            sqlTableQuery.append(joinStreamingInputClause);
            tableQuery.addParams(whereExprParams);
            tableQuery.addParams(joinOnExprParams);
        }
    }

    private void addFromClauseAndParams(BLangTableQuery tableQuery, BLangStreamingInput streamingInput,
                                        StringBuilder sqlTableQuery) {
        streamingInput.accept(this);
        sqlTableQuery.append(streamingInputClause);
        tableQuery.addParams(whereExprParams);
        whereExprParams.clear();
    }

    private void addSelectClauseAndParams(BLangTableQuery tableQuery, BLangSelectClause selectClause,
                                          StringBuilder sqlTableQuery) {
        selectClause.accept(this);
        sqlTableQuery.append(selectExprClause).append(" from ");
        tableQuery.addParams(selectExprParams);
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        BLangBinaryExpr expr = (BLangBinaryExpr) joinStreamingInput.getOnExpression();
        BLangStreamingInput streamingInput = (BLangStreamingInput) joinStreamingInput.getStreamingInput();
        joinStreamingInputClause = new StringBuilder();
        streamingInput.accept(this);
        joinStreamingInputClause.append("join ").append(streamingInputClause).append(" on ");
        addExprToClause(expr, joinStreamingInputClause, joinOnExprParams);
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        streamingInputClause = new StringBuilder();
        BLangExpression tableReference = (BLangExpression) streamingInput.getStreamReference();
        tableReference.accept(this);
        exprStack.pop();
        streamingInputClause.append("(select * from [[tableName]]");
        WhereNode where = streamingInput.getBeforeStreamingCondition();
        if (where == null) {
            where = streamingInput.getAfterStreamingCondition();
        }

        /* for tables there can only be one whereClause and there is no windowClause.
         So we don't care about the windowClause. */
        if (where != null) {
            ((BLangWhere) where).accept(this);
            streamingInputClause.append(" ").append(whereClause);
        }
        streamingInputClause.append(")");
        if (streamingInput.getAlias() != null) {
            streamingInputClause.append(" as ").append(streamingInput.getAlias());
        }
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        exprStack.push(QUESTION_MARK);
        exprParams.add(literalExpr);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        BLangSimpleVarRef expr = (BLangSimpleVarRef) fieldAccessExpr.expr;
        String sqlExpr = expr.variableName.value + "." + fieldAccessExpr.field.value;
        exprStack.push(sqlExpr);
    }

    /**
     * This function will append the parametrized SQL query to given String Builder and at the same time, this
     * function will add the parametrized params to the given list and finally clears the temporary param list.
     *
     * @param expr             expression on which the SQL query is built
     * @param sqlStringBuilder StringBuilder to which the SQL query is appended
     * @param params           List to which the parameters are added
     */
    void addExprToClause(BLangExpression expr, StringBuilder sqlStringBuilder,
                         List<BLangExpression> params) {
        expr.accept(this);
        sqlStringBuilder.append(exprStack.pop());
        params.addAll(exprParams);
        exprParams.clear();
    }
}
