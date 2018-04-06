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
import org.ballerinalang.model.tree.clauses.StreamingInput;
import org.ballerinalang.model.tree.clauses.WhereNode;
import org.ballerinalang.model.tree.clauses.WindowClauseNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOutputRateLimit;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingEdgeInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
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

public class SiddhiQueryBuilder extends SqlQueryBuilder {
    private static final CompilerContext.Key<SiddhiQueryBuilder> SIDDHI_QUERY_BUILDER_KEY =
            new CompilerContext.Key<>();

    private StringBuilder setExpr;
    private StringBuilder outputRateLimitClause;
    private StringBuilder windowClause;
    private StringBuilder patternStreamingClause;
    private StringBuilder streamActionClause;

    private StringBuilder streamDefinitionQuery;
    private StringBuilder siddhiQuery;

    private Set<String> streamIds;

    private List<BLangExpression> inStreamRefs;
    private List<BLangExpression> inTableRefs;
    private List<BLangExpression> outStreamRefs;
    private List<BLangExpression> outTableRefs;

    private boolean isInPatternForClause = false;

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
        addExprToClause(expr, joinStreamingInputClause, null);
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        StringBuilder ternaryExprBuilder = new StringBuilder("ifThenElse(");
        addExprToClause(ternaryExpr.expr, ternaryExprBuilder, null);
        ternaryExprBuilder.append(", ");
        addExprToClause(ternaryExpr.thenExpr, ternaryExprBuilder, null);
        ternaryExprBuilder.append(", ");
        addExprToClause(ternaryExpr.elseExpr, ternaryExprBuilder, null);
        ternaryExprBuilder.append(")");
        exprStack.push(ternaryExprBuilder.toString());
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        streamingInputClause = new StringBuilder();
        BLangExpression streamRef = (BLangExpression) streamingInput.getStreamReference();
        streamRef.accept(this);
        String streamRefName = exprStack.pop();
        streamingInputClause.append(streamRefName);
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
        streamIds.add(streamRefName);
        addInRefs(streamRef);
    }

    @Override
    public void visit(BLangWindow window) {
        windowClause = new StringBuilder();
        windowClause.append("#window.");
        addExprToClause((BLangExpression) window.getFunctionInvocation(), windowClause, null);
        windowClause.append(" ");
    }

    @Override
    public void visit(BLangWhere where) {
        whereClause = new StringBuilder();
        whereClause.append("[");
        addExprToClause((BLangExpression) where.getExpression(), whereClause, null);
        whereClause.append("]");
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

    public void visit(BLangForever foreverStatement) {
        siddhiQuery = new StringBuilder();
        streamDefinitionQuery = new StringBuilder();
        streamIds = new HashSet<>();
        inStreamRefs = new ArrayList<>();
        outStreamRefs = new ArrayList<>();
        inTableRefs = new ArrayList<>();
        outTableRefs = new ArrayList<>();
        setExpr = null;
        orderByClause = null;
        whereClause = null;
        windowClause = null;
        joinStreamingInputClause = null;
        streamingInputClause = null;
        selectExprClause = null;
        selectExpr = null;
        groupByClause = null;
        havingClause = null;
        patternStreamingClause = null;
        streamActionClause = null;

        List<VariableNode> globalVariables = foreverStatement.getGlobalVariables();
        if (globalVariables != null) {
            globalVariables.forEach(variable -> ((BLangVariable) variable).accept(this));
        }

        List<VariableSymbol> functionVariables = foreverStatement.getFunctionVariables();
        if (functionVariables != null) {
            functionVariables.stream().map(variable -> (BVarSymbol) variable)
                    .forEach(this::getStreamDefinitionForFunctionVariable);
        }

        List<? extends StatementNode> statementNodes = foreverStatement.getStreamingQueryStatements();
        statementNodes.forEach(statementNode -> ((BLangStatement) statementNode).accept(this));
        foreverStatement.setSiddhiQuery(this.getSiddhiQuery());
        foreverStatement.setStreamIdsAsString(String.join(",", streamIds));
    }

    @Override
    public void visit(BLangVariable varNode) {
        String name = varNode.name.toString();
        List<BStructType.BStructField> structFieldList = ((BStructType) ((BStreamType) (varNode).type).
                constraint).fields;
        createStreamDefinitionQuery(name, structFieldList);
    }

    private void createStreamDefinitionQuery(String name, List<BStructType.BStructField> structFieldList) {
        StringBuilder streamDefinition = new StringBuilder("define stream ");
        streamDefinition.append(name).append("( ");
        generateStreamDefinition(structFieldList, streamDefinition);
        streamDefinitionQuery.append(streamDefinition).append("\n");
    }

    private void getStreamDefinitionForFunctionVariable(BVarSymbol varSymbol) {
        String name = varSymbol.name.toString();
        List<BStructType.BStructField> structFieldList = ((BStructType) ((BStreamType) (varSymbol).type).constraint).
                fields;
        createStreamDefinitionQuery(name, structFieldList);
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
    public void visit(BLangSelectClause select) {
        super.visit(select);
        if (select.getGroupBy() != null) {
            selectExprClause.append(" ").append(groupByClause);
        }
        if (select.getHaving() != null) {
            selectExprClause.append(" ").append(havingClause);
        }
    }

    @Override
    public void visit(BLangBinaryExpr expr) {
        super.visit(expr);
        if (expr.opKind == OperatorKind.EQUAL) {
            //remove the string expression created by the base class as siddhi represents "equal" in a different way.
            exprStack.pop();
            String op = " == ";
            if (expr.rhsExpr instanceof BLangLiteral) {
                BLangLiteral literal = (BLangLiteral) expr.rhsExpr;
                if (literal.typeTag == TypeTags.NIL && literal.value == null) {
                    op = " is "; // siddhi equivalent of '==' with null on rhs ( e.g. where e2 is null)
                }
            }
            expr.lhsExpr.accept(this);
            expr.rhsExpr.accept(this);
            String rhsExpr = exprStack.pop();
            String lhsExpr = exprStack.pop();
            String sqlExpr = lhsExpr + op + rhsExpr;
            exprStack.push(sqlExpr);

        } else if (expr.opKind == OperatorKind.NOT_EQUAL) {
            if (expr.rhsExpr instanceof BLangLiteral) {
                BLangLiteral literal = (BLangLiteral) expr.rhsExpr;
                if (literal.typeTag == TypeTags.NIL && literal.value == null) {
                    exprStack.pop();
                    expr.lhsExpr.accept(this);
                    expr.rhsExpr.accept(this);
                    String rhsExpr = exprStack.pop();
                    String lhsExpr = exprStack.pop();
                    String sqlExpr = " not(" + lhsExpr + " is " + rhsExpr + ")";
                    exprStack.push(sqlExpr);
                }
            }
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
        boolean enclosedInParenthesisPattern = patternStreamingInput.enclosedInParenthesis();
        boolean onlyAndAvailable = patternStreamingInput.isAndOnly();
        boolean onlyOrAvailable = patternStreamingInput.isOrOnly();
        boolean andWithNotAvailable = patternStreamingInput.isAndWithNot();
        boolean forWithNotAvailable = patternStreamingInput.isForWithNot();

        List<PatternStreamingEdgeInputNode> patternStreamingEdgeInputs =
                patternStreamingInput.getPatternStreamingEdgeInputs();
        BLangPatternStreamingInput nestedPatternStreamingInput =
                (BLangPatternStreamingInput) patternStreamingInput.getPatternStreamingInput();

        if (isFollowedByPattern) {
            buildFollowedByPattern(patternStreamingEdgeInputs, nestedPatternStreamingInput);
            return;
        }
        if (enclosedInParenthesisPattern) {
            buildEnclosedPattern(nestedPatternStreamingInput);
            return;
        }
        if (onlyAndAvailable || andWithNotAvailable || onlyOrAvailable) {
            String op;
            if (andWithNotAvailable) {
                patternStreamingClause.append(" not ");
            }
            if (onlyAndAvailable || andWithNotAvailable) {
                op = " and ";
            } else {
                op = " or ";
            }
            buildPatternWithAndOr(patternStreamingEdgeInputs, op);
            return;
        }
        if (forWithNotAvailable) {
            buildPatternWithTimePeriod(patternStreamingInput, patternStreamingEdgeInputs);
            return;
        }
        BLangPatternStreamingEdgeInput patternStreamingEdgeInput =
                (BLangPatternStreamingEdgeInput) patternStreamingEdgeInputs.get(0);
        patternStreamingEdgeInput.accept(this);
    }

    @Override
    public void visit(BLangLiteral bLangLiteral) {
        String literal = String.valueOf(bLangLiteral.value);
        if (bLangLiteral.typeTag == TypeTags.STRING) {
            if (!isInPatternForClause) {
                literal = String.format("'%s'", literal);
            } else {
                literal = String.format("%s", literal);
                isInPatternForClause = false;
            }
        }
        exprStack.push(literal);
    }

    private void buildPatternWithTimePeriod(BLangPatternStreamingInput patternStreamingInput,
                                            List<PatternStreamingEdgeInputNode> patternStreamingEdgeInputs) {
        patternStreamingClause.append(" not ");
        BLangPatternStreamingEdgeInput patternStreamingEdgeInput = (BLangPatternStreamingEdgeInput)
                patternStreamingEdgeInputs.get(0);
        patternStreamingEdgeInput.accept(this);
        patternStreamingClause.append(" for ");
        isInPatternForClause = true;
        addExprToClause((BLangExpression) patternStreamingInput.getTimeExpr(), patternStreamingClause, null);
    }

    private void buildPatternWithAndOr(List<PatternStreamingEdgeInputNode> patternStreamingEdgeInputs, String op) {
        BLangPatternStreamingEdgeInput patternStreamingEdgeInput = (BLangPatternStreamingEdgeInput)
                patternStreamingEdgeInputs.get(0);
        patternStreamingEdgeInput.accept(this);
        patternStreamingClause.append(op);
        patternStreamingEdgeInput = (BLangPatternStreamingEdgeInput)
                patternStreamingEdgeInputs.get(1);
        patternStreamingEdgeInput.accept(this);
    }

    private void buildEnclosedPattern(BLangPatternStreamingInput nestedPatternStreamingInput) {
        patternStreamingClause.append("( ");
        nestedPatternStreamingInput.accept(this);
        patternStreamingClause.append(" ) ");
    }

    private void buildFollowedByPattern(List<PatternStreamingEdgeInputNode> patternStreamingEdgeInputs,
                                        BLangPatternStreamingInput nestedPatternStreamingInput) {
        BLangPatternStreamingEdgeInput patternStreamingEdgeInput = (BLangPatternStreamingEdgeInput)
                patternStreamingEdgeInputs.get(0);
        patternStreamingEdgeInput.accept(this);
        patternStreamingClause.append(" -> ");
        nestedPatternStreamingInput.accept(this);
    }

    @Override
    public void visit(BLangPatternStreamingEdgeInput patternStreamingEdgeInput) {
        BLangExpression streamRef = (BLangExpression) patternStreamingEdgeInput.getStreamReference();
        streamRef.accept(this);
        streamIds.add(exprStack.pop());
        addInRefs(streamRef);

        String alias = patternStreamingEdgeInput.getAliasIdentifier();
        if (alias != null) {
            patternStreamingClause.append(alias).append(" = ");
        }
        patternStreamingClause.append(patternStreamingEdgeInput.getStreamReference());
        WhereNode whereNode = patternStreamingEdgeInput.getWhereClause();
        if (whereNode != null) {
            ((BLangWhere) whereNode).accept(this);
            patternStreamingClause.append(" ").append(whereClause);
            whereClause = new StringBuilder();
        }
        ExpressionNode expression = patternStreamingEdgeInput.getExpression();
        if (expression != null) {
            ((BLangExpression) expression).accept(this);
            patternStreamingClause.append(exprStack.pop());
        }
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        StringBuilder intRangeExpr = new StringBuilder();
        intRangeExpr.append("<");
        addExprToClause(intRangeExpression.startExpr, intRangeExpr, null);
        intRangeExpr.append(":");
        BLangExpression endExpr = intRangeExpression.endExpr;
        if (endExpr != null) {
            addExprToClause(endExpr, intRangeExpr, null);
        }
        intRangeExpr.append(">");
        exprStack.push(intRangeExpr.toString());
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {
        setExpr = new StringBuilder(" ");
        addExprToClause((BLangExpression) setAssignmentClause.getVariableReference(), setExpr, null);
        setExpr.append(" = ");
        addExprToClause((BLangExpression) setAssignmentClause.getExpressionNode(), setExpr, null);
    }

    private void generateStreamDefinition(List<BStructType.BStructField> structFieldList,
                                          StringBuilder streamDefinition) {
        Iterator<BStructType.BStructField> structFieldIterator = structFieldList.iterator();
        BStructType.BStructField structField = structFieldIterator.next();
        if (structField != null) {
            addTypesToStreamDefinitionQuery(streamDefinition, structField);
        }

        while (structFieldIterator.hasNext()) {
            structField = structFieldIterator.next();
            streamDefinition.append(" , ");
            addTypesToStreamDefinitionQuery(streamDefinition, structField);
        }

        if (structField != null) {
            streamDefinition.append(" ); ");
        }
    }

    private void addTypesToStreamDefinitionQuery(StringBuilder streamDefinition, BStructType.BStructField structField) {
        streamDefinition.append(structField.getName()).append(" ");
        String type = structField.getType().toString();
        //even though, type defined as int, actual value is a long. To handle this case in Siddhi, type is defined
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

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        String sqlExpr;
        if (fieldAccessExpr.expr instanceof  BLangSimpleVarRef) {
            BLangSimpleVarRef expr = (BLangSimpleVarRef) fieldAccessExpr.expr;
            sqlExpr = expr.variableName.value + "." + fieldAccessExpr.field.value;
            exprStack.push(sqlExpr);
        } else if (fieldAccessExpr.expr instanceof BLangIndexBasedAccess) {
            sqlExpr = fieldAccessExpr.toString();
            BLangIndexBasedAccess indexBasedAccess = (BLangIndexBasedAccess) fieldAccessExpr.expr;
            String exprName = (indexBasedAccess.expr.toString() + ".length-1").replaceAll("\\s+", "");
            if (sqlExpr.replaceAll("\\s+", "").contains(exprName)) {
                sqlExpr = sqlExpr.replaceFirst(exprName, "last");
            }
            exprStack.push(sqlExpr);
        }
    }

    private String getSiddhiQuery() {
        return streamDefinitionQuery.toString() + "\n" + siddhiQuery.toString();
    }

    // adds the input streams/tables references
    private void addInRefs(BLangExpression streamReference) {
        addRefs(streamReference, inStreamRefs, inTableRefs);
    }

    private void addRefs(BLangExpression ref, List<BLangExpression> streams, List<BLangExpression> tables) {
        if (ref.type.tag == TypeTags.STREAM) {
            streams.add(ref);
        } else if (ref.type.tag == TypeTags.TABLE) {
            tables.add(ref);
        }
    }

    void addExprToClause(BLangExpression expr, StringBuilder sqlStringBuilder,
                         List<BLangExpression> params) {
        expr.accept(this);
        sqlStringBuilder.append(exprStack.pop());
    }
}
