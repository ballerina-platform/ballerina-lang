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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * @since 0.965.0
 *
 * This class will generate the SQL query for stream/tables SQLish grammar for different classes.
 */

public class SqlQueryBuilder extends BLangNodeVisitor {
    private static final CompilerContext.Key<SqlQueryBuilder> SQL_QUERY_BUILDER_KEY =
            new CompilerContext.Key<>();

    private static final String QUESTION_MARK = "?";

    private StringBuilder orderByClause;
    private StringBuilder whereClause;
    private StringBuilder joinStreamingInputClause;
    private StringBuilder streamingInputClause;
    private StringBuilder selectExprClause;
    private StringBuilder selectExpr;
    private StringBuilder groupByClause;
    private StringBuilder havingClause;

    //Use these variables to fill up the parametrized locations("?") in prepared statement.
    private List<BLangExpression> selectExprParams = new ArrayList<>();
    private List<BLangExpression> havingExprParams = new ArrayList<>();
    private List<BLangExpression> joinOnExprParams = new ArrayList<>();

    //Keep temporary parametrized sql strings and  temporary parameters for "?"
    private List<BLangExpression> exprParams = new ArrayList<>();
    private List<BLangExpression> whereExprParams = new ArrayList<>();
    private Stack<String> exprStack = new Stack<>();

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
    public void visit(BLangOrderBy orderBy) {
        List<? extends ExpressionNode> varRefs = orderBy.getVariables();
        Iterator<? extends ExpressionNode> iterator = varRefs.iterator();
        BLangExpression expr = (BLangExpression) iterator.next();
        orderByClause = new StringBuilder("order by ");
        expr.accept(this);
        orderByClause.append(exprStack.pop());
        while (iterator.hasNext()) {
            orderByClause.append(",").append(" ");
            expr = (BLangExpression) iterator.next();
            expr.accept(this);
            orderByClause.append(exprStack.pop());
        }
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        BLangBinaryExpr expr = (BLangBinaryExpr) joinStreamingInput.getOnExpression();
        BLangStreamingInput streamingInput = (BLangStreamingInput) joinStreamingInput.getStreamingInput();
        joinStreamingInputClause = new StringBuilder();
        streamingInput.accept(this);
        joinStreamingInputClause.append("join ").append(streamingInputClause).append(" on ");
        addParametrizedSQL(expr, joinStreamingInputClause, joinOnExprParams);
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
    public void visit(BLangWhere where) {
        whereClause = new StringBuilder();
        whereClause.append("where ");
        BLangBinaryExpr expr = (BLangBinaryExpr) where.getExpression();
        addParametrizedSQL(expr, whereClause, whereExprParams);
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
        addParametrizedSQL(expr, havingClause, havingExprParams);
    }

    @Override
    public void visit(BLangBinaryExpr expr) {
        expr.lhsExpr.accept(this);
        expr.rhsExpr.accept(this);
        String rhsExpr = exprStack.pop();
        String lhsExpr = exprStack.pop();
        StringBuilder sqlExpr = new StringBuilder();
        switch (expr.opKind) {
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MOD:
            case NOT_EQUAL:
            case GREATER_THAN:
            case GREATER_EQUAL:
            case LESS_THAN:
            case LESS_EQUAL:
                sqlExpr.append(lhsExpr).append(String.valueOf(expr.opKind)).append(rhsExpr);
                break;
            case POW:
                sqlExpr.append("power(").append(lhsExpr).append(", ").append(rhsExpr).append(")");
                break;
            case AND:
                sqlExpr.append(lhsExpr).append(" and ").append(rhsExpr);
                break;
            case OR:
                sqlExpr.append(lhsExpr).append(" or ").append(rhsExpr);
                break;
            case EQUAL:
                sqlExpr.append(lhsExpr).append(" = ").append(rhsExpr);
                break;
            default:
                sqlExpr.append(lhsExpr).append(String.valueOf(expr.opKind)).append(rhsExpr);
        }
        exprStack.push(sqlExpr.toString());
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        exprStack.push(QUESTION_MARK);
        exprParams.add(literalExpr);
    }

    @Override
    public void visit(BLangSimpleVarRef variableReference) {
        exprStack.push(variableReference.getVariableName().value);
    }

    private void createSQLGroupByClause(BLangSelectClause select) {
        BLangGroupBy groupBy = (BLangGroupBy) select.getGroupBy();
        groupBy.accept(this);
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        List<? extends ExpressionNode> varList = groupBy.getVariables();
        Iterator<? extends ExpressionNode> iterator = varList.iterator();
        groupByClause = new StringBuilder("group by ");
        BLangExpression expr = (BLangExpression) iterator.next();
        expr.accept(this);
        groupByClause.append(exprStack.pop());
        while (iterator.hasNext()) {
            expr = (BLangExpression) iterator.next();
            groupByClause.append(", ");
            expr.accept(this);
            groupByClause.append(exprStack.pop());
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        BLangSimpleVarRef expr = (BLangSimpleVarRef) fieldAccessExpr.expr;
        String sqlExpr = expr.variableName.value + "." + fieldAccessExpr.field.value;
        exprStack.push(sqlExpr);
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
        addParametrizedSQL(expr, selectExpr, selectExprParams);
        String identifier = selectExpression.getIdentifier();
        if (identifier != null) {
            selectExpr.append(" as ").append(identifier);
        }
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        StringBuilder sqlStringBuilder = new StringBuilder(invocationExpr.getName().getValue()).append("(");
        List<String> argList = new ArrayList<>();
        for (BLangExpression arg : invocationExpr.argExprs) {
            arg.accept(this);
            argList.add(exprStack.pop());
        }
        sqlStringBuilder.append(String.join(", ", argList)).append(")");
        exprStack.push(sqlStringBuilder.toString());
    }

    /**
     * This function will append the parametrized SQL query to given String Builder and at the same time, this
     * function will add the parametrized params to the given list and finally clears the temporary param list.
     *
     * @param expr             expression on which the SQL query is built
     * @param sqlStringBuilder StringBuilder to which the SQL query is appended
     * @param params           List to which the parameters are added
     */
    private void addParametrizedSQL(BLangExpression expr, StringBuilder sqlStringBuilder,
            List<BLangExpression> params) {
        expr.accept(this);
        sqlStringBuilder.append(exprStack.pop());
        params.addAll(exprParams);
        exprParams.clear();
    }
}
