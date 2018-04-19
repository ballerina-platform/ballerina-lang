/*
 *
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.desugar;

import org.ballerinalang.model.tree.clauses.OrderByVariableNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Base class for both In memory H2 table sql query builder and the Siddhi query builder.
 */
public abstract class SqlQueryBuilder extends BLangNodeVisitor {

    StringBuilder orderByClause;
    StringBuilder whereClause;
    StringBuilder joinStreamingInputClause;
    StringBuilder streamingInputClause;
    StringBuilder selectExprClause;
    StringBuilder selectExpr;
    StringBuilder groupByClause;
    StringBuilder havingClause;
    StringBuilder orderByVariableClause;

    Stack<String> exprStack = new Stack<>();

    //Use these variables to fill up the parametrized locations("?") in prepared statement.
    List<BLangExpression> selectExprParams = new ArrayList<>();
    List<BLangExpression> havingExprParams = new ArrayList<>();

    @Override
    public void visit(BLangOrderBy orderBy) {
        List<? extends OrderByVariableNode> orderByVariableNodes = orderBy.getVariables();
        Iterator<? extends OrderByVariableNode> iterator = orderByVariableNodes.iterator();
        BLangOrderByVariable orderByVariable = (BLangOrderByVariable) iterator.next();
        orderByClause = new StringBuilder("order by ");
        orderByVariable.accept(this);
        orderByClause.append(orderByVariableClause);
        while (iterator.hasNext()) {
            orderByClause.append(",").append(" ");
            orderByVariable = (BLangOrderByVariable) iterator.next();
            orderByVariable.accept(this);
            orderByClause.append(orderByVariableClause);
        }
    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {
        orderByVariableClause = new StringBuilder();
        BLangExpression expr = (BLangExpression) orderByVariable.getVariableReference();
        expr.accept(this);
        orderByVariableClause.append(exprStack.pop());
        orderByVariableClause.append(" ").append(orderByVariable.getOrderByType());
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
        }
        exprStack.push(sqlExpr.toString());
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        unaryExpr.expr.accept(this);
        String expr = exprStack.pop();
        switch (unaryExpr.operator) {
            case NOT:
                expr = "not(" + expr + ")";
                break;
        }
        exprStack.push(expr);
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

    private void createSQLGroupByClause(BLangSelectClause select) {
        BLangGroupBy groupBy = (BLangGroupBy) select.getGroupBy();
        groupBy.accept(this);
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
    public void visit(BLangHaving having) {
        BLangBinaryExpr expr = (BLangBinaryExpr) having.getExpression();
        havingClause = new StringBuilder("having ");
        addExprToClause(expr, havingClause, havingExprParams);
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
    public void visit(BLangSimpleVarRef variableReference) {
        exprStack.push(variableReference.getVariableName().value);
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
        BLangExpression expr = (BLangExpression) selectExpression.getExpression();
        selectExpr = new StringBuilder();
        addExprToClause(expr, selectExpr, selectExprParams);
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

    abstract void addExprToClause(BLangExpression expr, StringBuilder sqlStringBuilder,
                                  List<BLangExpression> params);
}
