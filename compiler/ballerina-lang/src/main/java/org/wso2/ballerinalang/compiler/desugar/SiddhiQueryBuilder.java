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

import org.ballerinalang.model.tree.clauses.JoinStreamingInput;
import org.ballerinalang.model.tree.clauses.OrderByNode;
import org.ballerinalang.model.tree.clauses.SelectClauseNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.clauses.SetAssignmentNode;
import org.ballerinalang.model.tree.clauses.StreamActionNode;
import org.ballerinalang.model.tree.clauses.StreamingInput;
import org.ballerinalang.model.tree.clauses.WhereNode;
import org.ballerinalang.model.tree.clauses.WindowClauseNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangStreamlet;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectExpression;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSetAssignment;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamAction;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhere;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWindow;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangQueryStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Iterator;
import java.util.List;

/**
 * This class will generate the Siddhi query for stream SQLish grammar for different classes.
 */

public class SiddhiQueryBuilder extends BLangNodeVisitor {
    private static final CompilerContext.Key<SiddhiQueryBuilder> SIDDHI_QUERY_BUILDER_KEY =
            new CompilerContext.Key<>();

    private String varRef;
    private String binaryExpr;
    private StringBuilder orderByClause;
    private StringBuilder setExpr;
    private StringBuilder whereClause;
    private StringBuilder windowClause;
    private StringBuilder joinStreamingInputClause;
    private StringBuilder streamingInputClause;
    private StringBuilder selectExprClause;
    private StringBuilder selectExpr;
    private StringBuilder setAssignmentClause;
    private StringBuilder groupByClause;
    private StringBuilder havingClause;
    private StringBuilder streamDefinitionQuery;
    private StringBuilder siddhiQuery;
    private StringBuilder streamIdsAsString;
    private StringBuilder streamActionClause;

    public static SiddhiQueryBuilder getInstance(CompilerContext context) {
        SiddhiQueryBuilder siddhiQueryBuilder = context.get(SIDDHI_QUERY_BUILDER_KEY);
        if (siddhiQueryBuilder == null) {
            siddhiQueryBuilder = new SiddhiQueryBuilder(context);
        }

        return siddhiQueryBuilder;
    }

    private SiddhiQueryBuilder(CompilerContext context) {
        context.put(SIDDHI_QUERY_BUILDER_KEY, this);
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
        WhereNode beforeWhereNode = streamingInput.getBeforeStreamingCondition();
        WhereNode afterWhereNode = streamingInput.getAfterStreamingCondition();
        WindowClauseNode windowClauseNode = streamingInput.getWindowClause();

        if (beforeWhereNode != null) {
            ((BLangWhere) beforeWhereNode).accept(this);
            streamingInputClause.append(" ").append(whereClause);
        }

        if (windowClauseNode != null) {
            ((BLangWindow) windowClauseNode).accept(this);
            streamingInputClause.append(" ").append(windowClause);
        }

        if (afterWhereNode != null) {
            ((BLangWhere) afterWhereNode).accept(this);
            streamingInputClause.append(" ").append(whereClause);
        }

        if (streamingInput.getAlias() != null) {
            streamingInputClause.append(" as ").append(streamingInput.getAlias());
        }
    }

    public void visit(BLangWindow window) {
        windowClause = new StringBuilder();
        windowClause.append("#window.");
        windowClause.append(window.getFunctionInvocation().toString());
        windowClause.append(" ");
    }

    @Override
    public void visit(BLangWhere where) {
        whereClause = new StringBuilder();
        whereClause.append("[");
        BLangBinaryExpr expr = (BLangBinaryExpr) where.getExpression();
        expr.accept(this);
        whereClause.append(binaryExpr);
        whereClause.append("]");
    }

    @Override
    public void visit(BLangSelectClause select) {
        createSiddhiSelectExpressionClause(select);
        if (select.getGroupBy() != null) {
            createSiddhiGroupByClause(select);
            selectExprClause.append(" ").append(groupByClause);
        }
        if (select.getHaving() != null) {
            createSiddhiHavingClause(select);
            selectExprClause.append(" ").append(havingClause);
        }
    }

    private void createSiddhiHavingClause(BLangSelectClause select) {
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

    private void createSiddhiGroupByClause(BLangSelectClause select) {
        BLangGroupBy groupBy = (BLangGroupBy) select.getGroupBy();
        groupBy.accept(this);
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        List<? extends ExpressionNode> varList = groupBy.getVariables();
        Iterator<? extends ExpressionNode> iterator = varList.iterator();
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

    private void createSiddhiSelectExpressionClause(BLangSelectClause select) {
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
        String identifier = selectExpression.getIdentifier();
        if (identifier != null) {
            selectExpr.append(" as ").append(identifier);
        }
    }


    public void visit(BLangInvocation invocationExpr) {
        varRef = invocationExpr.toString();
    }

    @Override
    public void visit(BLangStreamlet streamletNode) {
        streamIdsAsString = new StringBuilder();
        siddhiQuery = new StringBuilder();
        streamDefinitionQuery = new StringBuilder();
        siddhiQuery.append("from ");
        List<? extends StatementNode> statementNodes = streamletNode.getBody().getStatements();
        for (StatementNode statementNode : statementNodes) {
            ((BLangStatement) statementNode).accept(this);
        }
        streamletNode.setSiddhiQuery(this.getSiddhiQuery());
        streamletNode.setStreamIdsAsString(streamIdsAsString.toString());
    }

    public void visit(BLangVariableDef varDefNode) {
        StringBuilder streamDefinition = new StringBuilder("define stream ");
        streamDefinition.append(varDefNode.var.name).append("( ");
        if (streamIdsAsString.length() == 0) {
            streamIdsAsString.append(varDefNode.var.name);
        } else {
            streamIdsAsString.append(",").append(varDefNode.var.name);
        }
        List<BStructType.BStructField> structFieldList = ((BStructType) ((BStreamType) varDefNode.var.type).
                constraint).fields;

        Iterator<BStructType.BStructField> structFieldIterator = structFieldList.iterator();
        BStructType.BStructField structField = structFieldIterator.next();
        if (structField != null) {
            streamDefinition.append(structField.getName()).append(" ");
            String type = structField.getType().toString();
            //Eventhough, type defined as int, actual value is a long. To handle this case in Siddhi, type is defined
            //as long.
            if (type.equalsIgnoreCase("int")) {
                type = "long";
            } else if (type.equalsIgnoreCase("float")) {
                type = "double";
            }
            streamDefinition.append(type);
        }

        while (structFieldIterator.hasNext()) {
            structField = structFieldIterator.next();
            streamDefinition.append(" , ");
            streamDefinition.append(structField.getName()).append(" ");
            String type = structField.getType().toString();
            if (type.equalsIgnoreCase("int")) {
                type = "long";
            } else if (type.equalsIgnoreCase("float")) {
                type = "double";
            }
            streamDefinition.append(type);
        }

        if (structField != null) {
            streamDefinition.append(" ); ");
        }

        streamDefinitionQuery.append(streamDefinition).append("\n");
    }

    public void visit(BLangQueryStatement queryStatement) {
        BLangStreamingQueryStatement streamingQueryStatement = (BLangStreamingQueryStatement) queryStatement.
                getStreamingQueryStatement();
        streamingQueryStatement.accept(this);
    }

    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        StreamingInput streamingInput = streamingQueryStatement.getStreamingInput();
        if (streamingInput != null) {
            ((BLangStreamingInput) streamingInput).accept(this);
            siddhiQuery.append(" ").append(streamingInputClause);
        }

        JoinStreamingInput joinStreamingInput = streamingQueryStatement.getJoiningInput();
        if (joinStreamingInput != null) {
            ((BLangJoinStreamingInput) joinStreamingInput).accept(this);
            siddhiQuery.append(" ").append(joinStreamingInputClause);
        }

        SelectClauseNode selectClauseNode = streamingQueryStatement.getSelectClause();
        if (selectClauseNode != null) {
            ((BLangSelectClause) selectClauseNode).accept(this);
            siddhiQuery.append(" ").append(selectExprClause);
        }

        OrderByNode orderByNode = streamingQueryStatement.getOrderbyClause();
        if (orderByNode != null) {
            ((BLangOrderBy) orderByNode).accept(this);
            siddhiQuery.append(" ").append(orderByClause);
        }

        StreamActionNode streamActionNode = streamingQueryStatement.getStreamingAction();
        if (streamActionNode != null) {
            ((BLangStreamAction) streamActionNode).accept(this);
            siddhiQuery.append(" ").append(streamActionClause);
        }
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        streamActionClause = new StringBuilder();
        String streamActionType = streamAction.getActionType();
        if (streamActionType.equalsIgnoreCase("insert")) {
            streamActionClause.append(" ").append("insert into");
            streamActionClause.append(" ").append(streamAction.getIdentifier());
        } else if (streamActionType.equalsIgnoreCase("update")) {
            streamActionClause.append(" ").append("update");
            streamActionClause.append(" ").append(streamAction.getIdentifier());
            List<SetAssignmentNode> setAssignments = streamAction.getSetClause();

            if (!setAssignments.isEmpty()) {
                Iterator<? extends SetAssignmentNode> iterator = setAssignments.iterator();
                setAssignmentClause = new StringBuilder(" set ");
                BLangSetAssignment setAssignment = (BLangSetAssignment) iterator.next();
                setAssignment.accept(this);
                setAssignmentClause.append(setExpr);
                while (iterator.hasNext()) {
                    setAssignment = (BLangSetAssignment) iterator.next();
                    setAssignmentClause.append(", ");
                    setAssignment.accept(this);
                    setAssignmentClause.append(setExpr);
                }
            }
            streamActionClause.append(setAssignmentClause);
        } else if (streamActionType.equalsIgnoreCase("delete")) {
            streamActionClause.append(" ").append("delete");
            streamActionClause.append(" ").append(streamAction.getIdentifier());
            ((BLangExpression) streamAction.getExpression()).accept(this);
            streamActionClause.append(" ").append(this.binaryExpr);
        }
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {
        setExpr = new StringBuilder();
        ((BLangExpression) setAssignmentClause.getVariableReference()).accept(this);
        setExpr.append(" ").append(varRef).append(" = ");
        ((BLangExpression) setAssignmentClause.getExpressionNode()).accept(this);
        setExpr.append(varRef);
    }

    public String getSiddhiQuery() {
        return streamDefinitionQuery.toString() + "\n" + siddhiQuery.toString() + " ; ";
    }
}
