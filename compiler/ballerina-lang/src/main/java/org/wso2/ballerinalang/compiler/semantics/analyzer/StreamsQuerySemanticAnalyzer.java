/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.clauses.GroupByNode;
import org.ballerinalang.model.tree.clauses.HavingNode;
import org.ballerinalang.model.tree.clauses.JoinStreamingInput;
import org.ballerinalang.model.tree.clauses.OrderByNode;
import org.ballerinalang.model.tree.clauses.OrderByVariableNode;
import org.ballerinalang.model.tree.clauses.PatternStreamingEdgeInputNode;
import org.ballerinalang.model.tree.clauses.SelectClauseNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.clauses.StreamActionNode;
import org.ballerinalang.model.tree.clauses.StreamingInput;
import org.ballerinalang.model.tree.clauses.WhereNode;
import org.ballerinalang.model.tree.clauses.WindowClauseNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.StreamingQueryStatementNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByVariable;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class checks semantics of streaming queries.
 *
 * @since 0.990.0
 */
public class StreamsQuerySemanticAnalyzer extends BLangNodeVisitor {
    private static final CompilerContext.Key<StreamsQuerySemanticAnalyzer> SYMBOL_ANALYZER_KEY =
            new CompilerContext.Key<>();
    private static final String AGGREGATOR_OBJECT_NAME = "Aggregator";
    private static final String WINDOW_OBJECT_NAME = "Window";
    private SymbolTable symTable;
    private SymbolEnter symbolEnter;
    private Names names;
    private SymbolResolver symResolver;
    private TypeChecker typeChecker;
    private Types types;
    private BLangDiagnosticLog dlog;

    private SymbolEnv env;
    private BType expType;
    private DiagnosticCode diagCode;
    private boolean isSiddhiRuntimeEnabled;
    private List<BField> outputStreamFieldList;

    private StreamsQuerySemanticAnalyzer(CompilerContext context) {
        context.put(SYMBOL_ANALYZER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public static StreamsQuerySemanticAnalyzer getInstance(CompilerContext context) {
        StreamsQuerySemanticAnalyzer semAnalyzer = context.get(SYMBOL_ANALYZER_KEY);
        if (semAnalyzer == null) {
            semAnalyzer = new StreamsQuerySemanticAnalyzer(context);
        }

        return semAnalyzer;
    }

    public void analyze(BLangForever forever, SymbolEnv env) {
        analyzeNode(forever, env);
    }

    private void analyzeStmt(BLangStatement stmtNode, SymbolEnv env) {
        analyzeNode(stmtNode, env);
    }

    private void analyzeNode(BLangNode node, SymbolEnv env) {
        analyzeNode(node, env, symTable.noType, null);
    }

    private void analyzeNode(BLangNode node, SymbolEnv env, BType expType, DiagnosticCode diagCode) {
        SymbolEnv prevEnv = this.env;
        BType preExpType = this.expType;
        DiagnosticCode preDiagCode = this.diagCode;

        // TODO Check the possibility of using a try/finally here
        this.env = env;
        this.expType = expType;
        this.diagCode = diagCode;
        node.accept(this);
        this.env = prevEnv;
        this.expType = preExpType;
        this.diagCode = preDiagCode;
    }

    @Override
    public void visit(BLangForever foreverStatement) {

        isSiddhiRuntimeEnabled = foreverStatement.isSiddhiRuntimeEnabled();
        foreverStatement.setEnv(env);
        for (StreamingQueryStatementNode streamingQueryStatement : foreverStatement.getStreamingQueryStatements()) {
            SymbolEnv stmtEnv = SymbolEnv.createStreamingQueryEnv(
                    (BLangStreamingQueryStatement) streamingQueryStatement, env);
            analyzeStmt((BLangStatement) streamingQueryStatement, stmtEnv);
        }

        //Validate output attribute names with stream/struct
        for (StreamingQueryStatementNode streamingQueryStatement : foreverStatement.getStreamingQueryStatements()) {
            if (isSiddhiRuntimeEnabled) {
                checkOutputAttributesWithOutputConstraintForSiddhi((BLangStatement) streamingQueryStatement);
            } else {
                checkOutputAttributesWithOutputConstraint((BLangStatement) streamingQueryStatement);
            }
            validateOutputAttributeTypes((BLangStatement) streamingQueryStatement);
        }
    }

    @Override
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        //Keep output stream fields to be later declared in the env
        retainOutputStreamFields(streamingQueryStatement.getStreamingAction());

        StreamingInput streamingInput = streamingQueryStatement.getStreamingInput();
        if (streamingInput != null) {
            ((BLangStreamingInput) streamingInput).accept(this);
            JoinStreamingInput joinStreamingInput = streamingQueryStatement.getJoiningInput();
            if (joinStreamingInput != null) {
                ((BLangJoinStreamingInput) joinStreamingInput).accept(this);
            }
        }

        SelectClauseNode selectClauseNode = streamingQueryStatement.getSelectClause();
        if (selectClauseNode != null) {
            ((BLangSelectClause) selectClauseNode).accept(this);
        }


        OrderByNode orderByNode = streamingQueryStatement.getOrderbyClause();
        if (orderByNode != null) {
            ((BLangOrderBy) orderByNode).accept(this);
        }

        StreamActionNode streamActionNode = streamingQueryStatement.getStreamingAction();
        if (streamActionNode != null) {
            ((BLangStreamAction) streamActionNode).accept(this);
        }

        BLangPatternClause patternClause = (BLangPatternClause) streamingQueryStatement.getPatternClause();
        if (patternClause != null) {
            patternClause.accept(this);
        }
    }

    @Override
    public void visit(BLangPatternClause patternClause) {
        BLangPatternStreamingInput patternStreamingInput = (BLangPatternStreamingInput) patternClause
                .getPatternStreamingNode();
        patternStreamingInput.accept(this);
    }

    @Override
    public void visit(BLangPatternStreamingInput patternStreamingInput) {
        List<PatternStreamingEdgeInputNode> patternStreamingEdgeInputs = patternStreamingInput
                .getPatternStreamingEdgeInputs();
        for (PatternStreamingEdgeInputNode inputNode : patternStreamingEdgeInputs) {
            BLangPatternStreamingEdgeInput streamingInput = (BLangPatternStreamingEdgeInput) inputNode;
            streamingInput.accept(this);
        }

        BLangPatternStreamingInput nestedPatternStreamingInput = (BLangPatternStreamingInput) patternStreamingInput
                .getPatternStreamingInput();
        if (nestedPatternStreamingInput != null) {
            nestedPatternStreamingInput.accept(this);
        }
    }

    @Override
    public void visit(BLangPatternStreamingEdgeInput patternStreamingEdgeInput) {
        BLangVariableReference streamRef = (BLangVariableReference) patternStreamingEdgeInput.getStreamReference();
        typeChecker.checkExpr(streamRef, env);

        BLangWhere where = (BLangWhere) patternStreamingEdgeInput.getWhereClause();
        if (where != null) {
            where.accept(this);
        }
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        BLangVariableReference streamRef = (BLangVariableReference) streamingInput.getStreamReference();
        typeChecker.checkExpr(streamRef, env);

        if (streamRef.symbol == null) {
            return;
        }

        WhereNode beforeWhereNode = streamingInput.getBeforeStreamingCondition();
        if (beforeWhereNode != null) {
            ((BLangWhere) beforeWhereNode).accept(this);
        }

        List<ExpressionNode> preInvocations = streamingInput.getPreFunctionInvocations();
        if (preInvocations != null) {
            preInvocations.stream().map(expr -> (BLangExpression) expr)
                    .forEach(expression -> expression.accept(this));
        }

        WindowClauseNode windowClauseNode = streamingInput.getWindowClause();
        if (windowClauseNode != null) {
            ((BLangWindow) windowClauseNode).accept(this);
        }

        List<ExpressionNode> postInvocations = streamingInput.getPostFunctionInvocations();
        if (postInvocations != null) {
            postInvocations.stream().map(expressionNode -> (BLangExpression) expressionNode)
                    .forEach(expression -> expression.accept(this));
        }

        WhereNode afterWhereNode = streamingInput.getAfterStreamingCondition();
        if (afterWhereNode != null) {
            ((BLangWhere) afterWhereNode).accept(this);
        }

        if (isTableReference(streamingInput.getStreamReference())) {
            if (streamingInput.getAlias() == null) {
                dlog.error(streamingInput.pos, DiagnosticCode.UNDEFINED_INVOCATION_ALIAS,
                        ((BLangInvocation) streamRef).name.getValue());
            }
            if (streamingInput.getStreamReference().getKind() == NodeKind.INVOCATION) {
                BInvokableSymbol functionSymbol = (BInvokableSymbol) ((BLangInvocation) streamRef).symbol;
                symbolEnter.defineVarSymbol(streamingInput.pos, EnumSet.noneOf(Flag.class),
                        functionSymbol.retType, names.fromString(streamingInput.getAlias()),
                        env);
            } else {
                BType constraint = (((BLangVariableReference) streamingInput
                        .getStreamReference()).type);
                symbolEnter.defineVarSymbol(streamingInput.pos, EnumSet.noneOf(Flag.class), constraint,
                        names.fromString(streamingInput.getAlias()), env);
            }
        } else {
            //Create duplicate symbol for stream alias
            if (streamingInput.getAlias() != null) {
                BVarSymbol streamSymbol = (BVarSymbol) streamRef.symbol;
                BVarSymbol streamAliasSymbol = ASTBuilderUtil.duplicateVarSymbol(streamSymbol);
                streamAliasSymbol.name = names.fromString(streamingInput.getAlias());
                symbolEnter.defineSymbol(streamingInput.pos, streamAliasSymbol, env);
            }
        }
    }

    private boolean isTableReference(ExpressionNode streamReference) {
        if (streamReference.getKind() == NodeKind.INVOCATION) {
            return ((BLangInvocation) streamReference).type.tsymbol.type == symTable.tableType;
        } else {
            return ((BLangVariableReference) streamReference).type.tsymbol.type == symTable.tableType;
        }
    }

    @Override
    public void visit(BLangWindow windowClause) {
        for (BLangExpression expr : ((BLangInvocation) windowClause.getFunctionInvocation()).argExprs) {
            expr.accept(this);
        }
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        if (!isSiddhiRuntimeEnabled) {
            BSymbol aggregatorTypeSymbol = symResolver.lookupSymbolInPackage(invocationExpr.pos, env,
                    Names.STREAMS_MODULE, names.fromString(AGGREGATOR_OBJECT_NAME), SymTag.OBJECT);
            BSymbol windowTypeSymbol = symResolver.lookupSymbolInPackage(invocationExpr.pos, env, Names.STREAMS_MODULE,
                    names.fromString(WINDOW_OBJECT_NAME), SymTag.OBJECT);

            if (checkInvocationExpr(invocationExpr, aggregatorTypeSymbol, windowTypeSymbol,
                    names.fromIdNode(invocationExpr.pkgAlias))) {
                return;
            }

            if (!checkInvocationExpr(invocationExpr, aggregatorTypeSymbol, windowTypeSymbol, Names.STREAMS_MODULE)) {
                typeChecker.checkExpr(invocationExpr, env);
            }
        }
    }

    @Override
    public void visit(BLangWhere whereClause) {
        ExpressionNode expressionNode = whereClause.getExpression();
        ((BLangExpression) expressionNode).accept(this);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        if (isSiddhiRuntimeEnabled) {
            typeTestExpr.expr.accept(this);
        } else {
            typeChecker.checkExpr(typeTestExpr, env);
        }
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        if (isSiddhiRuntimeEnabled) {
            elvisExpr.lhsExpr.accept(this);
            elvisExpr.rhsExpr.accept(this);
        } else {
            typeChecker.checkExpr(elvisExpr, env);
        }
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        if (isSiddhiRuntimeEnabled) {
            unaryExpr.expr.accept(this);
        } else {
            typeChecker.checkExpr(unaryExpr, env);
        }
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        if (isSiddhiRuntimeEnabled) {
            ExpressionNode leftExpression = binaryExpr.getLeftExpression();
            ((BLangExpression) leftExpression).accept(this);

            ExpressionNode rightExpression = binaryExpr.getRightExpression();
            ((BLangExpression) rightExpression).accept(this);
        } else {
            typeChecker.checkExpr(binaryExpr, env);
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        if (isSiddhiRuntimeEnabled) {
            fieldAccessExpr.expr.accept(this);
        } else {
            typeChecker.checkExpr(fieldAccessExpr, env);
            if (fieldAccessExpr.expr.type.tag == TypeTags.STREAM) {
                BRecordType streamType = (BRecordType) ((BStreamType) fieldAccessExpr.expr.type).constraint;
                if (streamType.fields.stream()
                        .noneMatch(bField -> bField.name.value.equals(fieldAccessExpr.field.value))) {
                    dlog.error(fieldAccessExpr.pos, DiagnosticCode.UNDEFINED_STREAM_ATTRIBUTE, fieldAccessExpr.field
                            .value);
                }
            }
        }
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        if (isSiddhiRuntimeEnabled) {
            indexAccessExpr.indexExpr.accept(this);
        } else {
            typeChecker.checkExpr(indexAccessExpr, env);
        }
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        if (!isSiddhiRuntimeEnabled) {
            typeChecker.checkExpr(varRefExpr, env);
        }
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        if (!isSiddhiRuntimeEnabled) {
            typeChecker.checkExpr(literalExpr, env);
        }
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        if (!isSiddhiRuntimeEnabled) {
            typeChecker.checkExpr(ternaryExpr, env);
        }
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        if (!isSiddhiRuntimeEnabled) {
            typeChecker.checkExpr(tableLiteral, env);
        }

    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        if (!isSiddhiRuntimeEnabled) {
            typeChecker.checkExpr(bracedOrTupleExpr, env);
        }

    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        GroupByNode groupByNode = selectClause.getGroupBy();
        if (groupByNode != null) {
            ((BLangGroupBy) groupByNode).accept(this);
        }

        List<? extends SelectExpressionNode> selectExpressionsList = selectClause.getSelectExpressions();
        if (selectExpressionsList != null) {
            for (SelectExpressionNode selectExpressionNode : selectExpressionsList) {
                ((BLangSelectExpression) selectExpressionNode).accept(this);
            }
        }

        //Having requires output stream fields to be available in the env.
        defineOutputStreamFields(this.env);

        HavingNode havingNode = selectClause.getHaving();
        if (havingNode != null) {
            ((BLangHaving) havingNode).accept(this);
        }
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        List<? extends ExpressionNode> variableExpressionList = groupBy.getVariables();
        for (ExpressionNode expressionNode : variableExpressionList) {
            checkExpr((BLangExpression) expressionNode);
        }
    }

    @Override
    public void visit(BLangHaving having) {
        ExpressionNode expressionNode = having.getExpression();
        checkExpr((BLangExpression) expressionNode);
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        List<? extends OrderByVariableNode> orderByVariableList = orderBy.getVariables();
        for (OrderByVariableNode orderByVariableNode : orderByVariableList) {
            ((BLangOrderByVariable) orderByVariableNode).accept(this);
        }
    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {
        BLangExpression expression = (BLangExpression) orderByVariable.getVariableReference();
        checkExpr(expression);
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
        BLangExpression expressionNode = (BLangExpression) selectExpression.getExpression();
        expressionNode.accept(this);
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        BLangLambdaFunction function = (BLangLambdaFunction) streamAction.getInvokableBody();
        typeChecker.checkExpr(function, env);
        validateStreamingActionFunctionParameters(streamAction);
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        StreamingInput streamingInput = joinStreamingInput.getStreamingInput();
        if (streamingInput != null) {
            ((BLangStreamingInput) streamingInput).accept(this);
        }

        ExpressionNode expressionNode = joinStreamingInput.getOnExpression();
        if (expressionNode != null) {
            ((BLangExpression) expressionNode).accept(this);
        }
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {
        ExpressionNode expressionNode = setAssignmentClause.getExpressionNode();
        ((BLangExpression) expressionNode).accept(this);

        ExpressionNode variableReference = setAssignmentClause.getVariableReference();
        ((BLangExpression) variableReference).accept(this);
    }

    //------------- private methods ---------------------------------------------------------

    private void checkOutputAttributesWithOutputConstraintForSiddhi(BLangStatement streamingQueryStatement) {
        List<? extends SelectExpressionNode> selectExpressions =
                ((BLangStreamingQueryStatement) streamingQueryStatement).getSelectClause().getSelectExpressions();

        List<String> variableList = new ArrayList<>();
        boolean isSelectAll = true;
        if (!((BLangStreamingQueryStatement) streamingQueryStatement).getSelectClause().isSelectAll()) {
            isSelectAll = false;
            for (SelectExpressionNode expressionNode : selectExpressions) {
                String variableName;
                if (expressionNode.getIdentifier() != null) {
                    variableName = expressionNode.getIdentifier();
                } else {
                    if (expressionNode.getExpression() instanceof BLangFieldBasedAccess) {
                        variableName = ((BLangFieldBasedAccess) expressionNode.getExpression()).field.value;
                    } else {
                        variableName = ((BLangSimpleVarRef) (expressionNode).getExpression()).variableName.value;
                    }
                }
                variableList.add(variableName);
            }
        }

        // Validate whether input stream constraint type only contains attribute type that can be processed by Siddhi
        if (((BLangStreamingQueryStatement) streamingQueryStatement).getStreamingInput() != null) {
            List<BField> fields = ((BStructureType) ((BStreamType) ((BLangExpression)
                    (((BLangStreamingQueryStatement) streamingQueryStatement).getStreamingInput()).
                            getStreamReference()).type).constraint).fields;

            for (BField structField : fields) {
                validateStreamEventType(((BLangStreamingQueryStatement) streamingQueryStatement).pos, structField);
                if (isSelectAll) {
                    //create the variable list to validate when select * clause is used in query
                    variableList.add(structField.name.value);
                }
            }
        }

        BType streamActionArgumentType = ((BInvokableType) ((BLangLambdaFunction) (((BLangStreamingQueryStatement)
                streamingQueryStatement).getStreamingAction()).getInvokableBody()).type).paramTypes.get(0);

        if (streamActionArgumentType.tag == TypeTags.ARRAY) {
            BType structType = (((BArrayType) streamActionArgumentType).eType);

            if (structType.tag == TypeTags.OBJECT || structType.tag == TypeTags.RECORD) {
                List<BField> structFieldList = ((BStructureType) structType).fields;
                List<String> structFieldNameList = new ArrayList<>();
                for (BField structField : structFieldList) {
                    validateStreamEventType(((BLangStreamAction) ((BLangStreamingQueryStatement)
                            streamingQueryStatement).getStreamingAction()).pos, structField);
                    structFieldNameList.add(structField.name.value);
                }

                if (!variableList.equals(structFieldNameList)) {
                    dlog.error(((BLangStreamAction) ((BLangStreamingQueryStatement) streamingQueryStatement).
                            getStreamingAction()).pos, DiagnosticCode.INCOMPATIBLE_STREAM_ACTION_ARGUMENT, structType);
                }
            }
        }
    }

    private void checkOutputAttributesWithOutputConstraint(BLangStatement streamingQueryStatement) {
        List<? extends SelectExpressionNode> selectExpressions =
                ((BLangStreamingQueryStatement) streamingQueryStatement).getSelectClause().getSelectExpressions();

        Map<String, BType> selectClauseAttributeMap = new HashMap<>();
        if (!((BLangStreamingQueryStatement) streamingQueryStatement).getSelectClause().isSelectAll()) {
            for (SelectExpressionNode expressionNode : selectExpressions) {
                String variableName = resolveSelectFieldName(expressionNode);

                if (variableName == null) {
                    continue;
                }

                BType variableType = resolveSelectFieldType(expressionNode);
                selectClauseAttributeMap.put(variableName, variableType);
            }
        } else {
            List<BField> inputStructFieldList = ((BRecordType) ((BStreamType) ((BLangSimpleVarRef) (
                    ((BLangStreamingQueryStatement) streamingQueryStatement).getStreamingInput())
                    .getStreamReference()).type).constraint).fields;
            for (BField field : inputStructFieldList) {
                selectClauseAttributeMap.put(field.name.value, field.type);
            }
        }

        BType streamActionArgumentType = ((BInvokableType) ((BLangLambdaFunction) (((BLangStreamingQueryStatement)
                streamingQueryStatement).getStreamingAction()).getInvokableBody()).type).paramTypes.get(0);

        if (streamActionArgumentType.tag != TypeTags.ARRAY) {
            return;
        }

        BType structType = (((BArrayType) streamActionArgumentType).eType);

        if (structType.tag == TypeTags.OBJECT || structType.tag == TypeTags.RECORD) {
            List<BField> structFieldList = ((BStructureType) structType).fields;
            if (structFieldList.stream().anyMatch(bField -> !(selectClauseAttributeMap.containsKey(bField.name.value) &&
                    (selectClauseAttributeMap.get(bField.name.value) == null ||
                    types.isAssignable(selectClauseAttributeMap.get(bField.name.value), bField.type))))) {
                String[] fieldNames = structFieldList.stream().map(field -> field.name.value)
                        .collect(Collectors.toList()).toArray(new String[]{});
                String[] selectExprs = selectClauseAttributeMap.keySet().toArray(new String[]{});
                dlog.error(((BLangStreamAction) ((BLangStreamingQueryStatement) streamingQueryStatement).
                        getStreamingAction()).pos, DiagnosticCode.INCOMPATIBLE_FIELDS_IN_SELECT_CLAUSE, structType,
                           Arrays.toString(fieldNames), Arrays.toString(selectExprs));
            }
        }
    }

    private BType resolveSelectFieldType(SelectExpressionNode expressionNode) {
        return ((BLangExpression) (expressionNode).getExpression()).type;
    }

    private String resolveSelectFieldName(SelectExpressionNode expressionNode) {
        if (expressionNode.getIdentifier() != null) {
            return expressionNode.getIdentifier();
        } else {
            BLangExpression expr = (BLangExpression) expressionNode.getExpression();
            if (expr.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
                return ((BLangFieldBasedAccess) expr).field.value;
            } else {
                dlog.error(expr.pos, DiagnosticCode.SELECT_EXPR_ALIAS_NOT_FOUND);
                return null;
            }
        }
    }

    private void validateStreamEventType(DiagnosticPos pos, BField field) {
        if (!(field.type.tag == TypeTags.INT || field.type.tag == TypeTags.BOOLEAN || field.type.tag == TypeTags.STRING
                || field.type.tag == TypeTags.FLOAT)) {
            dlog.error(pos, DiagnosticCode.INVALID_STREAM_ATTRIBUTE_TYPE);
        }
    }

    private void validateStreamingEventType(DiagnosticPos pos, BType actualType, String attributeName, BType expType,
                                            DiagnosticCode diagCode) {
        if (expType.tag == TypeTags.SEMANTIC_ERROR) {
            return;
        } else if (expType.tag == TypeTags.NONE) {
            return;
        } else if (actualType.tag == TypeTags.SEMANTIC_ERROR) {
            return;
        } else if (this.types.isAssignable(expType, actualType)) {
            return;
        }

        // e.g. incompatible types: expected 'int' for attribute 'name', found 'string'
        dlog.error(pos, diagCode, expType, attributeName, actualType);
    }

    private void validateOutputAttributeTypes(BLangStatement streamingQueryStatement) {
        StreamingInput streamingInput = ((BLangStreamingQueryStatement) streamingQueryStatement).getStreamingInput();
        JoinStreamingInput joinStreamingInput = ((BLangStreamingQueryStatement) streamingQueryStatement).
                getJoiningInput();

        if (streamingInput != null &&
                ((BLangExpression) streamingInput.getStreamReference()).type.tag != TypeTags.SEMANTIC_ERROR) {
            Map<String, List<BField>> inputStreamSpecificFieldMap =
                    createInputStreamSpecificFieldMap(streamingInput, joinStreamingInput);
            BType streamActionArgumentType = ((BInvokableType) ((BLangLambdaFunction) (((BLangStreamingQueryStatement)
                    streamingQueryStatement).getStreamingAction()).getInvokableBody()).type).paramTypes.get(0);

            if (streamActionArgumentType.tag != TypeTags.ARRAY) {
                return;
            }

            BType structType = (((BArrayType) streamActionArgumentType).eType);
            if (structType.tag == TypeTags.OBJECT || structType.tag == TypeTags.RECORD) {
                List<BField> outputStreamFieldList = ((BStructureType) structType).fields;
                List<? extends SelectExpressionNode> selectExpressions = ((BLangStreamingQueryStatement)
                        streamingQueryStatement).getSelectClause().getSelectExpressions();

                if (!((BLangStreamingQueryStatement) streamingQueryStatement).getSelectClause().isSelectAll()) {
                    for (int i = 0; i < selectExpressions.size(); i++) {
                        SelectExpressionNode expressionNode = selectExpressions.get(i);
                        BField structField = null;
                        if ((isSiddhiRuntimeEnabled && expressionNode.getExpression().getKind() ==
                                                       NodeKind.FIELD_BASED_ACCESS_EXPR) ||
                            (expressionNode.getExpression().getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR &&
                            ((BLangFieldBasedAccess) expressionNode.getExpression()).expr.type.tag ==
                                    TypeTags.STREAM)) {
                            String attributeName =
                                    ((BLangFieldBasedAccess) expressionNode.getExpression()).field.value;
                            String streamIdentifier = ((BLangSimpleVarRef) ((BLangFieldBasedAccess) expressionNode.
                                    getExpression()).expr).variableName.value;

                            List<BField> streamFieldList = inputStreamSpecificFieldMap.
                                    get(streamIdentifier);
                            if (streamFieldList == null) {
                                dlog.error(((BLangSelectClause) ((BLangStreamingQueryStatement)
                                                streamingQueryStatement).getSelectClause()).pos,
                                        DiagnosticCode.UNDEFINED_STREAM_REFERENCE, streamIdentifier);
                            } else {
                                structField = getStructField(streamFieldList, attributeName);
                                validateAttributeWithOutputStruct(structField, attributeName,
                                        streamingQueryStatement, outputStreamFieldList.get(i));
                            }
                        } else if (expressionNode.getExpression() instanceof BLangSimpleVarRef) {
                            String attributeName = ((BLangSimpleVarRef) expressionNode.getExpression()).
                                    variableName.getValue();

                            for (List<BField> streamFieldList :
                                    inputStreamSpecificFieldMap.values()) {
                                structField = getStructField(streamFieldList, attributeName);
                                if (structField != null) {
                                    break;
                                }
                            }
                            validateAttributeWithOutputStruct(structField, attributeName, streamingQueryStatement,
                                    outputStreamFieldList.get(i));
                        }
                    }
                    return;
                }

                List<BField> inputStreamFields = ((BStructureType) ((BStreamType) ((BLangExpression)
                        (((BLangStreamingQueryStatement) streamingQueryStatement).getStreamingInput()).
                                getStreamReference()).type).constraint).fields;

                for (int i = 0; i < inputStreamFields.size(); i++) {
                    BField inputStructField = inputStreamFields.get(i);
                    BField outputStructField = outputStreamFieldList.get(i);
                    validateStreamingEventType(((BLangStreamAction) ((BLangStreamingQueryStatement)
                                    streamingQueryStatement).getStreamingAction()).pos,
                            outputStructField.getType(), outputStructField.getName().getValue(),
                            inputStructField.getType(), DiagnosticCode.STREAMING_INCOMPATIBLE_TYPES);
                }
            }
        }
    }

    private List<BField> getFieldListFromStreamInput(StreamingInput streamingInput) {
        BType inputReferenceType = ((BLangExpression) streamingInput.getStreamReference()).type;
        if (inputReferenceType.tag == TypeTags.STREAM) {
            return ((BStructureType) ((BStreamType) inputReferenceType).constraint).fields;
        }

        return ((BStructureType) ((BTableType) inputReferenceType).constraint).fields;
    }

    private String getStreamIdentifier(StreamingInput streamingInput) {
        String streamIdentifier = streamingInput.getAlias();
        if (streamIdentifier == null) {
            streamIdentifier = ((BLangSimpleVarRef) streamingInput.getStreamReference()).variableName.value;
        }

        return streamIdentifier;
    }

    private BField getStructField(List<BField> fieldList, String fieldName) {
        for (BField structField : fieldList) {
            String structFieldName = structField.name.getValue();
            if (structFieldName.equalsIgnoreCase(fieldName)) {
                return structField;
            }
        }

        return null;
    }

    private void validateAttributeWithOutputStruct(BField structField, String attributeName,
                                                   BLangStatement streamingQueryStatement,
                                                   BField outputStructField) {
        if (structField != null) {
            validateStreamingEventType(((BLangStreamAction) ((BLangStreamingQueryStatement)
                            streamingQueryStatement).getStreamingAction()).pos, outputStructField.getType(),
                    attributeName, structField.getType(), DiagnosticCode.STREAMING_INCOMPATIBLE_TYPES);
        }
    }

    private Map<String, List<BField>> createInputStreamSpecificFieldMap
            (StreamingInput streamingInput, JoinStreamingInput joinStreamingInput) {
        Map<String, List<BField>> inputStreamSpecificFieldMap = new HashMap<>();
        String firstStreamIdentifier = getStreamIdentifier(streamingInput);
        List<BField> firstInputStreamFieldList = getFieldListFromStreamInput(streamingInput);
        inputStreamSpecificFieldMap.put(firstStreamIdentifier, firstInputStreamFieldList);

        if (joinStreamingInput != null) {
            List<BField> secondInputStreamFieldList =
                    getFieldListFromStreamInput(joinStreamingInput.getStreamingInput());
            String secondStreamIdentifier = getStreamIdentifier(joinStreamingInput.getStreamingInput());
            inputStreamSpecificFieldMap.put(secondStreamIdentifier, secondInputStreamFieldList);
        }

        return inputStreamSpecificFieldMap;
    }

    private void validateStreamingActionFunctionParameters(BLangStreamAction streamAction) {
        List<BLangSimpleVariable> functionParameters = ((BLangFunction) streamAction.getInvokableBody().
                getFunctionNode()).requiredParams;
        if (functionParameters == null || functionParameters.size() != 1) {
            dlog.error((streamAction).pos,
                    DiagnosticCode.INVALID_STREAM_ACTION_ARGUMENT_COUNT,
                    functionParameters == null ? 0 : functionParameters.size());
            return;
        }

        if (functionParameters.get(0).type.tag != TypeTags.ARRAY ||
                (!((((BArrayType) functionParameters.get(0).type).eType.tag == TypeTags.OBJECT)
                        || (((BArrayType) functionParameters.get(0).type).eType.tag == TypeTags.RECORD)))) {
            dlog.error((streamAction).pos, DiagnosticCode.INVALID_STREAM_ACTION_ARGUMENT_TYPE);
        }
    }

    private void retainOutputStreamFields(StreamActionNode streamAction) {
        if (streamAction == null) {
            return;
        }
        BType streamActionArgumentType = ((BLangLambdaFunction) streamAction
                .getInvokableBody()).function.requiredParams.get(0).type;
        if (streamActionArgumentType.tag != TypeTags.ARRAY) {
            return;
        }
        BType structType = (((BArrayType) streamActionArgumentType).eType);
        if (structType.tag == TypeTags.OBJECT || structType.tag == TypeTags.RECORD) {
            this.outputStreamFieldList = ((BStructureType) structType).fields;
        }
    }

    private void defineOutputStreamFields(SymbolEnv stmtEnv) {
        if (this.outputStreamFieldList != null) {
            for (BField field : this.outputStreamFieldList) {
                stmtEnv.scope.define(field.name, field.symbol);
            }
        }
    }

    private boolean checkInvocationExpr(BLangInvocation invocationExpr, BSymbol aggregatorTypeSymbol,
                                        BSymbol windowTypeSymbol, Name name) {
        BSymbol symbol = symResolver.lookupSymbolInPackage(invocationExpr.pos, env, name,
                names.fromIdNode(invocationExpr.name), SymTag.INVOKABLE);
        if (symbol != symTable.notFoundSymbol) {
            invocationExpr.symbol = symbol;
            BSymbol typeSymbol = symbol.type.getReturnType().tsymbol;
            if (typeSymbol == aggregatorTypeSymbol || typeSymbol == windowTypeSymbol) {
                invocationExpr.typeChecked = true;
                invocationExpr.argExprs.forEach(arg -> arg.accept(this));
                invocationExpr.requiredArgs = invocationExpr.argExprs;
                return true;
            }

            invocationExpr.argExprs.forEach(arg -> arg.accept(this));
            invocationExpr.requiredArgs = invocationExpr.argExprs;
            typeChecker.checkExpr(invocationExpr, env);
            return true;
        }
        return false;
    }

    private void checkExpr(BLangExpression expr) {
        if (isSiddhiRuntimeEnabled) {
            expr.accept(this);
            return;
        }
        typeChecker.checkExpr(expr, env);
    }
}
