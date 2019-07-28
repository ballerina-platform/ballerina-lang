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

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.clauses.JoinStreamingInput;
import org.ballerinalang.model.tree.clauses.OrderByVariableNode;
import org.ballerinalang.model.tree.clauses.PatternStreamingEdgeInputNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.clauses.StreamActionNode;
import org.ballerinalang.model.tree.clauses.StreamingInput;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.StreamingQueryStatementNode;
import org.ballerinalang.model.types.ConstrainedType;
import org.ballerinalang.model.types.Type;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
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

    private BLangOrderBy orderBy;

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
        analyzeNode(node, env, symTable.noType);
    }

    private void analyzeNode(BLangNode node, SymbolEnv env, BType expType) {
        SymbolEnv prevEnv = this.env;
        BType preExpType = this.expType;
        DiagnosticCode preDiagCode = this.diagCode;

        // TODO Check the possibility of using a try/finally here
        this.env = env;
        this.expType = expType;
        this.diagCode = null;
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

        BLangStreamingInput streamingInput = (BLangStreamingInput) streamingQueryStatement.getStreamingInput();
        BLangJoinStreamingInput joinStreamingInput =
                (BLangJoinStreamingInput) streamingQueryStatement.getJoiningInput();
        BLangSelectClause selectClause = (BLangSelectClause) streamingQueryStatement.getSelectClause();
        orderBy = (BLangOrderBy) streamingQueryStatement.getOrderbyClause();

        SymbolEnv streamInputEnv = SymbolEnv.createTypeNarrowedEnv(streamingInput, env);
        analyzeNode(streamingInput, streamInputEnv);

        if (joinStreamingInput != null) {
            streamInputEnv = SymbolEnv.createTypeNarrowedEnv(streamingInput, streamInputEnv);
            analyzeNode(joinStreamingInput, streamInputEnv);
        }

        analyzeNode(selectClause, streamInputEnv);

        BLangStreamAction streamActionNode = (BLangStreamAction) streamingQueryStatement.getStreamingAction();
        if (streamActionNode != null) {
            analyzeNode(streamActionNode, env);
        }

        BLangPatternClause patternClause = (BLangPatternClause) streamingQueryStatement.getPatternClause();
        if (patternClause != null) {
            analyzeNode(patternClause, env);
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
        analyzeNode(streamRef, env);

        if (streamRef.symbol == null) {
            return;
        }

        if (isTableReference(streamingInput.getStreamReference())) {
            checkTableAlias(streamingInput, (BLangInvocation) streamRef);
        } else {
            defineTypeNarrowedStreamSymbol(streamingInput, streamRef);
        }

        analyzeStreamingInput(streamingInput);
        createTypeNarrowedSymbolForAlias(streamingInput, streamRef);
    }

    private void analyzeStreamingInput(BLangStreamingInput streamingInput) {
        BLangWhere beforeWhereNode = (BLangWhere) streamingInput.getBeforeStreamingCondition();
        if (beforeWhereNode != null) {
            analyzeNode(beforeWhereNode, env);
        }

        List<ExpressionNode> preInvocations = streamingInput.getPreFunctionInvocations();
        if (preInvocations != null) {
            preInvocations.stream().map(expr -> (BLangExpression) expr)
                    .forEach(expression -> analyzeNode(expression, env));
        }

        BLangWindow windowClauseNode = (BLangWindow) streamingInput.getWindowClause();
        if (windowClauseNode != null) {
            analyzeNode(windowClauseNode, env);
        }

        List<ExpressionNode> postInvocations = streamingInput.getPostFunctionInvocations();
        if (postInvocations != null) {
            postInvocations.stream().map(expressionNode -> (BLangExpression) expressionNode)
                    .forEach(expression -> analyzeNode(expression, env));
        }

        BLangWhere afterWhereNode = (BLangWhere) streamingInput.getAfterStreamingCondition();
        if (afterWhereNode != null) {
            analyzeNode(afterWhereNode, env);
        }
    }

    private void createTypeNarrowedSymbolForAlias(BLangStreamingInput streamingInput, BLangVariableReference varRef) {
        if (streamingInput.getAlias() != null) {
            //Remove older stream symbol since here after we have the alias
            env.scope.entries.remove(varRef.symbol.name);
            BVarSymbol symbol = (BVarSymbol) varRef.symbol;
            BVarSymbol aliasSymbol = null;

            if (symbol.type.tag == TypeTags.STREAM) {
                aliasSymbol = ASTBuilderUtil.duplicateVarSymbol(symbol);
                aliasSymbol.name = names.fromString(streamingInput.getAlias());
            } else if (symbol.type.tag == TypeTags.TABLE) {
                aliasSymbol = ASTBuilderUtil.duplicateVarSymbol(symbol);
                aliasSymbol.name = names.fromString(streamingInput.getAlias());
            } else if (symbol.type.tag == TypeTags.INVOKABLE &&
                       ((BInvokableSymbol) symbol).retType.tag == TypeTags.TABLE) {
                aliasSymbol = new BVarSymbol(0, names.fromString(streamingInput.getAlias()), symbol.pkgID,
                                             ((BInvokableSymbol) symbol).retType, symbol.scope.owner);
            }

            if (aliasSymbol != null) {
                Type constrainedType = ((ConstrainedType) aliasSymbol.type).getConstraint();
                defineConstraintTypeNarrowedSymbol(streamingInput, aliasSymbol, (BType) constrainedType);
            }
        }
    }

    private void defineTypeNarrowedStreamSymbol(BLangStreamingInput streamingInput, BLangVariableReference streamRef) {
        BVarSymbol streamSymbol = (BVarSymbol) streamRef.symbol;
        BType constraintType = ((BStreamType) streamSymbol.type).constraint;
        defineConstraintTypeNarrowedSymbol(streamingInput, streamSymbol, constraintType);
    }

    private void checkTableAlias(BLangStreamingInput streamingInput, BLangInvocation streamRef) {
        if (streamingInput.getAlias() == null) {
            dlog.error(streamingInput.pos, DiagnosticCode.UNDEFINED_INVOCATION_ALIAS, streamRef
                    .name.getValue());
        }
    }

    private void defineConstraintTypeNarrowedSymbol(BLangNode node, BVarSymbol symbol, BType constraintType) {
        BVarSymbol varSymbol = symbolEnter.createVarSymbol(symbol.flags, constraintType, symbol.name, env);
        varSymbol.owner = symbol.owner;
        varSymbol.originalSymbol = symbol;
        symbolEnter.defineShadowedSymbol(node.pos, varSymbol, env);
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
        analyzeNode((BLangInvocation) windowClause.getFunctionInvocation(), env);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        if (!isSiddhiRuntimeEnabled) {

            if (invocationExpr.expr != null) {
                analyzeNode(invocationExpr.expr, env);
            }

            BSymbol aggregatorTypeSymbol = symResolver.lookupSymbolInPackage(invocationExpr.pos, env,
                    Names.STREAMS_MODULE, names.fromString(AGGREGATOR_OBJECT_NAME), SymTag.OBJECT);
            BSymbol windowTypeSymbol = symResolver.lookupSymbolInPackage(invocationExpr.pos, env, Names.STREAMS_MODULE,
                    names.fromString(WINDOW_OBJECT_NAME), SymTag.OBJECT);

            if (checkInvocationExpr(invocationExpr, aggregatorTypeSymbol, windowTypeSymbol,
                    names.fromIdNode(invocationExpr.pkgAlias))) {
                return;
            }

            if (!checkInvocationExpr(invocationExpr, aggregatorTypeSymbol, windowTypeSymbol, Names.STREAMS_MODULE)) {
                invocationExpr.argExprs.forEach(argExpr -> analyzeNode(argExpr, env));
                typeChecker.checkExpr(invocationExpr, env);
            }
        }
    }

    @Override
    public void visit(BLangWhere whereClause) {
        BLangExpression expr = (BLangExpression) whereClause.getExpression();
        analyzeNode(expr, env);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        if (isSiddhiRuntimeEnabled) {
            typeTestExpr.expr.accept(this);
        } else {
            analyzeNode(typeTestExpr.expr, env);
            typeChecker.checkExpr(typeTestExpr, env);
        }
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        if (isSiddhiRuntimeEnabled) {
            annotAccessExpr.expr.accept(this);
        } else {
            analyzeNode(annotAccessExpr.expr, env);
            typeChecker.checkExpr(annotAccessExpr, env);
        }
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        if (isSiddhiRuntimeEnabled) {
            elvisExpr.lhsExpr.accept(this);
            elvisExpr.rhsExpr.accept(this);
        } else {
            analyzeNode(elvisExpr.lhsExpr, env);
            analyzeNode(elvisExpr.rhsExpr, env);
            typeChecker.checkExpr(elvisExpr, env);
        }
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        if (isSiddhiRuntimeEnabled) {
            unaryExpr.expr.accept(this);
        } else {
            analyzeNode(unaryExpr.expr, env);
            typeChecker.checkExpr(unaryExpr, env);
        }
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        if (isSiddhiRuntimeEnabled) {
            BLangExpression leftExpression = binaryExpr.getLeftExpression();
            leftExpression.accept(this);

            BLangExpression rightExpression = binaryExpr.getRightExpression();
            rightExpression.accept(this);
        } else {
            analyzeNode(binaryExpr.lhsExpr, env);
            analyzeNode(binaryExpr.rhsExpr, env);
            typeChecker.checkExpr(binaryExpr, env);
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {

        if (isSiddhiRuntimeEnabled) {
            fieldAccessExpr.expr.accept(this);
        } else {
            typeChecker.checkExpr(fieldAccessExpr, env);

            //Rollback to original type from shadowed type
            BLangVariableReference expr = (BLangVariableReference) fieldAccessExpr.expr;
            if (expr.symbol != null) {
                BVarSymbol symbol = ((BVarSymbol) expr.symbol).originalSymbol;
                if (symbol != null && (symbol.type.tag == TypeTags.STREAM || symbol.type.tag == TypeTags.TABLE)) {
                    expr.symbol = symbol;
                    expr.type = expr.symbol.type;
                }

                if (env.node != null && env.node.getKind() == NodeKind.SELECT_CLAUSE) {
                    dlog.error(fieldAccessExpr.pos, DiagnosticCode.STREAM_ATTR_NOT_ALLOWED_IN_HAVING_ORDER_BY,
                               fieldAccessExpr.field.value);
                }

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
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        if (isSiddhiRuntimeEnabled) {
            indexAccessExpr.indexExpr.accept(this);
        } else {
            analyzeNode(indexAccessExpr.indexExpr, env);
            analyzeNode(indexAccessExpr.expr, env);
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
            analyzeNode(ternaryExpr.expr, env);
            analyzeNode(ternaryExpr.thenExpr, env);
            analyzeNode(ternaryExpr.elseExpr, env);
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
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        if (!isSiddhiRuntimeEnabled) {
            listConstructorExpr.exprs.forEach(expression -> analyzeNode(expression, env));
            typeChecker.checkExpr(listConstructorExpr, env);
        }
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        if (!isSiddhiRuntimeEnabled) {
            analyzeNode(groupExpr.expression, env);
            typeChecker.checkExpr(groupExpr, env);
        }
    }

    @Override
    public void visit(BLangTypeConversionExpr typeConversionExpr) {
        if (!isSiddhiRuntimeEnabled) {
            analyzeNode(typeConversionExpr.expr, env);
            typeChecker.checkExpr(typeConversionExpr, env);
        }
    }

    @Override
    public void visit(BLangTypedescExpr typedescExpr) {
        if (!isSiddhiRuntimeEnabled) {
            typeChecker.checkExpr(typedescExpr, env);
        }
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        BLangGroupBy groupByNode = (BLangGroupBy) selectClause.getGroupBy();
        if (groupByNode != null) {
            analyzeNode(groupByNode, env);
        }

        List<? extends SelectExpressionNode> selectExpressionsList = selectClause.getSelectExpressions();
        if (selectExpressionsList != null) {
            selectExpressionsList.forEach(selectExpressionNode -> analyzeNode((BLangNode) selectExpressionNode, env));
        }

        //Define a new env for select clause. It will be used by having and orderBy clause
        SymbolEnv selectClauseEnv = SymbolEnv.createDummyEnv(selectClause, new Scope(env.scope.owner), env);
//        SymbolEnv selectClauseEnv = new SymbolEnv(selectClause, new Scope(env.scope.owner));
        selectClauseEnv.enclPkg = env.enclPkg;
        defineOutputStreamFields(selectClauseEnv);

        BLangHaving havingNode = (BLangHaving) selectClause.getHaving();
        if (havingNode != null) {
            analyzeNode(havingNode, selectClauseEnv);
        }

        //orderBy should have access to the env of select clause, hence this
        if (orderBy != null) {
            analyzeNode(orderBy, selectClauseEnv);
        }
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        List<? extends ExpressionNode> variableExpressionList = groupBy.getVariables();
        variableExpressionList.forEach(expressionNode -> analyzeNode((BLangNode) expressionNode, env));
    }

    @Override
    public void visit(BLangHaving having) {
        BLangExpression expr = (BLangExpression) having.getExpression();
        analyzeNode(expr, env);
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        List<? extends OrderByVariableNode> orderByVariableList = orderBy.getVariables();
        orderByVariableList.forEach(orderByVariableNode -> analyzeNode((BLangNode) orderByVariableNode, env));
    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {
        BLangExpression expression = (BLangExpression) orderByVariable.getVariableReference();
        analyzeNode(expression, env);
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
        BLangExpression expressionNode = (BLangExpression) selectExpression.getExpression();
        analyzeNode(expressionNode, env);
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        BLangLambdaFunction function = (BLangLambdaFunction) streamAction.getInvokableBody();
        typeChecker.checkExpr(function, env);
        validateStreamingActionFunctionParameters(streamAction);
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {

        BLangStreamingInput streamingInput = (BLangStreamingInput) joinStreamingInput.getStreamingInput();
        analyzeNode(streamingInput, env);
        BLangExpression onCondition = (BLangExpression) joinStreamingInput.getOnExpression();
        if (onCondition != null) {
            analyzeNode(onCondition, env);
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

    private void validateStreamingEventType(DiagnosticPos pos, BType actualType, String attributeName, BType expType) {
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
        dlog.error(pos, DiagnosticCode.STREAMING_INCOMPATIBLE_TYPES, expType, attributeName, actualType);
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
                            inputStructField.getType());
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
                    attributeName, structField.getType());
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
            invocationExpr.pkgAlias.value = name.value;
            BSymbol typeSymbol = symbol.type.getReturnType().tsymbol;

            if (typeSymbol == aggregatorTypeSymbol || typeSymbol == windowTypeSymbol) {
                invocationExpr.typeChecked = true;
                invocationExpr.argExprs.forEach(arg -> analyzeNode(arg, env));
                invocationExpr.requiredArgs = invocationExpr.argExprs;
                return true;
            }

            invocationExpr.argExprs.forEach(arg -> analyzeNode(arg, env));
            invocationExpr.requiredArgs = invocationExpr.argExprs;
            typeChecker.checkExpr(invocationExpr, env);
            return true;
        }
        return false;
    }
}
