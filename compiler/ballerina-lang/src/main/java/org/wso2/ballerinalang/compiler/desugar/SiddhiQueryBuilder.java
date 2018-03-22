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

import org.ballerinalang.model.symbols.VariableSymbol;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.clauses.JoinStreamingInput;
import org.ballerinalang.model.tree.clauses.OrderByNode;
import org.ballerinalang.model.tree.clauses.OutputRateLimitNode;
import org.ballerinalang.model.tree.clauses.PatternClause;
import org.ballerinalang.model.tree.clauses.PatternStreamingEdgeInputNode;
import org.ballerinalang.model.tree.clauses.SelectClauseNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.clauses.StreamingInput;
import org.ballerinalang.model.tree.clauses.WhereNode;
import org.ballerinalang.model.tree.clauses.WindowClauseNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOutputRateLimit;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingEdgeInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectExpression;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSetAssignment;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamAction;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhere;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWindow;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWithinClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhenever;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class will generate the Siddhi query for stream SQLish grammar for different classes.
 *
 * @since 0.965.0
 */

public class SiddhiQueryBuilder extends BLangNodeVisitor {
    private static final CompilerContext.Key<SiddhiQueryBuilder> SIDDHI_QUERY_BUILDER_KEY =
            new CompilerContext.Key<>();

    private String varRef;
    private StringBuilder binaryExpr;
    private StringBuilder setExpr;
    private StringBuilder orderByClause;
    private StringBuilder outputRateLimitClause;
    private StringBuilder whereClause;
    private StringBuilder windowClause;
    private StringBuilder joinStreamingInputClause;
    private StringBuilder streamingInputClause;
    private StringBuilder selectExprClause;
    private StringBuilder selectExpr;
    private StringBuilder setAssignmentClause;
    private StringBuilder groupByClause;
    private StringBuilder havingClause;
    private StringBuilder patternStreamingClause;
    private StringBuilder streamActionClause;
    private StringBuilder intRangeExpr;

    private StringBuilder streamDefinitionQuery;
    private StringBuilder siddhiQuery;

    private Set<String> streamIds;

    private List<BLangExpression> inStreamRefs;
    private List<BLangExpression> inTableRefs;
    private List<BLangExpression> outStreamRefs;
    private List<BLangExpression> outTableRefs;

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

    public static CompilerContext.Key<SiddhiQueryBuilder> getSiddhiQueryBuilderKey() {
        return SIDDHI_QUERY_BUILDER_KEY;
    }

    List<BLangExpression> getInStreamRefs() {
        return inStreamRefs;
    }

    List<BLangExpression> getInTableRefs() {
        return inTableRefs;
    }

    List<BLangExpression> getOutStreamRefs() {
        return outStreamRefs;
    }

    List<BLangExpression> getOutTableRefs() {
        return outTableRefs;
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        List<? extends ExpressionNode> varRefs = orderBy.getVariables();
        Iterator<? extends ExpressionNode> iterator = varRefs.iterator();
        BLangSimpleVarRef variableRef = (BLangSimpleVarRef) iterator.next();
        orderByClause = new StringBuilder("order by ");
        addVarRefToClauseBuilder(variableRef, orderByClause);
        while (iterator.hasNext()) {
            orderByClause.append(",").append(" ");
            variableRef = (BLangSimpleVarRef) iterator.next();
            addVarRefToClauseBuilder(variableRef, orderByClause);
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
        if (joinStreamingInput.isUnidirectionalBeforeJoin()) {
            joinStreamingInputClause.append(" unidirectional ");
        }
        String joinType = joinStreamingInput.getJoinType();
        joinStreamingInputClause.append(" ").append(joinType).append(" ");
        if (joinStreamingInput.isUnidirectionalAfterJoin()) {
            joinStreamingInputClause.append(" unidirectional ");
        }
        joinStreamingInputClause.append(streamingInputClause).append(" on ");
        binaryExpr = new StringBuilder();
        expr.accept(this);
        joinStreamingInputClause.append(binaryExpr);
    }

    @Override
    public void visit(BLangBinaryExpr expr) {
        binaryExpr = new StringBuilder();
        ExpressionNode leftExpression = expr.getLeftExpression();
        if (leftExpression != null) {
            addVarRefToClauseBuilder((BLangExpression) leftExpression, binaryExpr);
        }

        OperatorKind operatorKind = expr.getOperatorKind();
        if (operatorKind != null) {
            binaryExpr.append(" ").append(getOperandAsString(operatorKind)).append(" ");
        }

        ExpressionNode rightExpression = expr.getRightExpression();
        if (rightExpression != null) {
            addVarRefToClauseBuilder((BLangExpression) rightExpression, binaryExpr);
        }
        varRef = binaryExpr.toString();
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        streamingInputClause = new StringBuilder();
        streamingInputClause.append(((BLangSimpleVarRef) streamingInput.getStreamReference()).getVariableName().value);
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
            streamingInputClause.append(" as ").append(streamingInput.getAlias()).append(" ");
        }

        BLangExpression streamReference = (BLangExpression) streamingInput.getStreamReference();
        if (streamReference != null) {
            streamReference.accept(this);
            streamIds.add(varRef);
            varRef = "";
            addInRefs(streamReference);
        }
    }

    @Override
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
        addVarRefToClauseBuilder(simpleVarRef, groupByClause);
        while (iterator.hasNext()) {
            simpleVarRef = (BLangSimpleVarRef) iterator.next();
            groupByClause.append(", ");
            addVarRefToClauseBuilder(simpleVarRef, groupByClause);
        }
    }

    @Override
    public void visit(BLangOutputRateLimit outputRateLimit) {
        outputRateLimitClause = new StringBuilder("output ");
        if (outputRateLimit.isSnapshot()) {
            outputRateLimitClause.append(" ").append("snapshot").append(" ").append("every");
            outputRateLimitClause.append(" ").append(outputRateLimit.getRateLimitValue());
            outputRateLimitClause.append(" ").append(outputRateLimit.getTimeScale());
        } else {
            outputRateLimitClause.append(" ").append(outputRateLimit.getOutputRateType()).append(" ").append("every");
            outputRateLimitClause.append(" ").append(outputRateLimit.getRateLimitValue());
            if (outputRateLimit.getTimeScale() != null) {
                outputRateLimitClause.append(" ").append(outputRateLimit.getTimeScale());
            } else {
                outputRateLimitClause.append(" ").append("events");
            }
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
        addVarRefToClauseBuilder(expr, selectExpr);
        String identifier = selectExpression.getIdentifier();
        if (identifier != null) {
            selectExpr.append(" as ").append(identifier);
        }
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        varRef = invocationExpr.toString();
    }

    public void visit(BLangWhenever wheneverStatement) {
        siddhiQuery = new StringBuilder();
        streamDefinitionQuery = new StringBuilder();
        streamIds = new HashSet<>();
        inStreamRefs = new ArrayList<>();
        outStreamRefs = new ArrayList<>();
        inTableRefs = new ArrayList<>();
        outTableRefs = new ArrayList<>();
        binaryExpr = null;
        setExpr = null;
        orderByClause = null;
        whereClause = null;
        windowClause = null;
        joinStreamingInputClause = null;
        streamingInputClause = null;
        selectExprClause = null;
        selectExpr = null;
        setAssignmentClause = null;
        groupByClause = null;
        havingClause = null;
        patternStreamingClause = null;
        streamActionClause = null;
        intRangeExpr = null;

        List<VariableNode> globalVariables = wheneverStatement.getGlobalVariables();
        if (globalVariables != null) {
            for (VariableNode variable : globalVariables) {
                ((BLangVariable) variable).accept(this);
            }
        }

        List<VariableSymbol> functionVariables = wheneverStatement.getFunctionVariables();
        if (functionVariables != null) {
            for (VariableSymbol variable : functionVariables) {
                getStreamDefintionForFuntionVariable((BVarSymbol) variable);
            }
        }

        List<? extends StatementNode> statementNodes = wheneverStatement.gettreamingQueryStatements();
        for (StatementNode statementNode : statementNodes) {
            ((BLangStatement) statementNode).accept(this);
        }
        wheneverStatement.setSiddhiQuery(this.getSiddhiQuery());
        wheneverStatement.setStreamIdsAsString(String.join(",", streamIds));
    }

    @Override
    public void visit(BLangVariable varNode) {
        StringBuilder streamDefinition = new StringBuilder("define stream ");
        streamDefinition.append(varNode.name).append("( ");
        List<BStructType.BStructField> structFieldList = ((BStructType) ((BStreamType) (varNode).type).
                constraint).fields;
        generateStreamDefinition(structFieldList, streamDefinition);

        streamDefinitionQuery.append(streamDefinition).append("\n");
    }

    private void getStreamDefintionForFuntionVariable(BVarSymbol varSymbol) {
        StringBuilder streamDefinition = new StringBuilder("define stream ");
        streamDefinition.append(varSymbol.name).append("( ");
        List<BStructType.BStructField> structFieldList = ((BStructType) ((BStreamType) (varSymbol).type).constraint).
                fields;
        generateStreamDefinition(structFieldList, streamDefinition);

        streamDefinitionQuery.append(streamDefinition).append("\n");
    }

    @Override
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        siddhiQuery.append("from ");
        StreamingInput streamingInput = streamingQueryStatement.getStreamingInput();
        if (streamingInput != null) {
            ((BLangStreamingInput) streamingInput).accept(this);
            siddhiQuery.append(" ").append(streamingInputClause);
        }

        PatternClause patternClause = streamingQueryStatement.getPatternClause();
        if (patternClause != null) {
            patternStreamingClause = new StringBuilder();
            ((BLangPatternClause) patternClause).accept(this);
            siddhiQuery.append(" ").append(patternStreamingClause);
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

        OutputRateLimitNode outputRateLimitNode = streamingQueryStatement.getOutputRateLimitNode();
        if (outputRateLimitNode != null) {
            ((BLangOutputRateLimit) outputRateLimitNode).accept(this);
            siddhiQuery.append(" ").append(outputRateLimitClause);
        }

        BLangStreamAction streamActionNode = (BLangStreamAction) streamingQueryStatement.getStreamingAction();
        if (streamActionNode != null) {
            streamActionNode.accept(this);
            siddhiQuery.append(" ").append(streamActionClause);
            siddhiQuery.append(" ; ");
        }
    }

    @Override
    public void visit(BLangPatternClause patternClause) {
        if (patternClause.isForAllEvents()) {
            patternStreamingClause.append("every ");
        }

        BLangPatternStreamingInput patternStreamingInput = (BLangPatternStreamingInput) patternClause
                .getPatternStreamingNode();
        patternStreamingInput.accept(this);

        BLangWithinClause withinClause = (BLangWithinClause) patternClause.getWithinClause();
        if (withinClause != null) {
            withinClause.accept(this);
        }

    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        streamActionClause = new StringBuilder("insert into ");
        String streamName = "stream" + streamAction.getInvokableBody().getFunctionNode().getName().getValue();
        streamName = streamName.replaceAll("\\$", "_");
        streamActionClause.append(streamName);
    }

    @Override
    public void visit(BLangPatternStreamingInput patternStreamingInput) {
        boolean isFollowedByPattern = patternStreamingInput.isFollowedBy();
        boolean enclosedInParanthesisPattern = patternStreamingInput.enclosedInParanthesis();
        List<PatternStreamingEdgeInputNode> patternStreamingEdgeInputs =
                patternStreamingInput.getPatternStreamingEdgeInputs();
        BLangPatternStreamingInput nestedPatternStreamingInput =
                (BLangPatternStreamingInput) patternStreamingInput.getPatternStreamingInput();

        if (isFollowedByPattern) {
            BLangPatternStreamingEdgeInput patternStreamingEdgeInput = (BLangPatternStreamingEdgeInput)
                    patternStreamingEdgeInputs.get(0);
            patternStreamingEdgeInput.accept(this);
            patternStreamingClause.append(" -> ");
            ((BLangPatternStreamingInput) patternStreamingInput.getPatternStreamingInput()).accept(this);
        }

        if (enclosedInParanthesisPattern) {
            patternStreamingClause.append("( ");
            nestedPatternStreamingInput.accept(this);
            patternStreamingClause.append(" ) ");
        }

        if (!isFollowedByPattern && !enclosedInParanthesisPattern) {
            BLangPatternStreamingEdgeInput patternStreamingEdgeInput =
                    (BLangPatternStreamingEdgeInput) patternStreamingEdgeInputs.get(0);
            patternStreamingEdgeInput.accept(this);
        }

    }

    @Override
    public void visit(BLangPatternStreamingEdgeInput patternStreamingEdgeInput) {
        BLangExpression streamRef = (BLangExpression) patternStreamingEdgeInput.getStreamReference();
        streamRef.accept(this);
        streamIds.add(varRef);
        varRef = "";
        addInRefs(streamRef);

        String alias = patternStreamingEdgeInput.getAliasIdentifier();
        patternStreamingClause.append(alias).append(" = ").append(patternStreamingEdgeInput.getStreamReference());
        WhereNode whereNode = patternStreamingEdgeInput.getWhereClause();
        if (whereNode != null) {
            ((BLangWhere) whereNode).accept(this);
            patternStreamingClause.append(" ").append(whereClause);
            whereClause = new StringBuilder();
        }
        ExpressionNode expression = patternStreamingEdgeInput.getExpression();
        if (expression != null) {
            ((BLangExpression) expression).accept(this);
            patternStreamingClause.append(intRangeExpr.toString());
        }
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        intRangeExpr = new StringBuilder();
        intRangeExpr.append("<");
        intRangeExpression.startExpr.accept(this);
        intRangeExpr.append(varRef).append(":");
        varRef = "";
        BLangExpression endExpr = intRangeExpression.endExpr;
        if (endExpr != null) {
            addVarRefToClauseBuilder(endExpr, intRangeExpr);
        }
        intRangeExpr.append(">");
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {
        setExpr = new StringBuilder(" ");
        addVarRefToClauseBuilder((BLangExpression) setAssignmentClause.getVariableReference(), setExpr);
        setExpr.append(" = ");
        addVarRefToClauseBuilder((BLangExpression) setAssignmentClause.getExpressionNode(), setExpr);
    }

    private void generateStreamDefinition(List<BStructType.BStructField> structFieldList,
                                          StringBuilder streamDefinition) {
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
            } else if (type.equalsIgnoreCase("boolean")) {
                type = "bool";
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
            } else if (type.equalsIgnoreCase("boolean")) {
                type = "bool";
            }
            streamDefinition.append(type);
        }

        if (structField != null) {
            streamDefinition.append(" ); ");
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        varRef = fieldAccessExpr.toString();
        if (fieldAccessExpr.expr instanceof BLangIndexBasedAccess) {
            BLangIndexBasedAccess indexBasedAccess = (BLangIndexBasedAccess) fieldAccessExpr.expr;
            String exprName = indexBasedAccess.expr.toString() + ".length";
            if (varRef.contains(exprName)) {
                varRef = varRef.replaceFirst(exprName, "last");
            }
        }
    }

    private String getSiddhiQuery() {
        return streamDefinitionQuery.toString() + "\n" + siddhiQuery.toString();
    }

    private String getOperandAsString(OperatorKind operatorKind) {
        String operandKindAsString = operatorKind.toString();
        if (operandKindAsString.equals("&&")) {
            return "and";
        } else if (operandKindAsString.equals("||")) {
            return "or";
        }
        return operandKindAsString;
    }

    // adds the input streams/tables references
    private void addInRefs(BLangExpression streamReference) {
        addRefs(streamReference, inStreamRefs, inTableRefs);
    }

    // adds the output streams/tables references
    private void addOutRefs(BLangExpression streamReference) {
        addRefs(streamReference, outStreamRefs, outTableRefs);
    }

    private void addRefs(BLangExpression ref, List<BLangExpression> streams, List<BLangExpression> tables) {
        if (ref.type.tag == TypeTags.STREAM) {
            streams.add(ref);
        } else if (ref.type.tag == TypeTags.TABLE) {
            tables.add(ref);
        }
    }

    private void addVarRefToClauseBuilder(BLangExpression expr, StringBuilder clauseBuilder) {
        expr.accept(this);
        clauseBuilder.append(varRef);
        varRef = "";
    }
}
