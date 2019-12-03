/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.desugar;

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.clauses.HavingNode;
import org.ballerinalang.model.tree.clauses.OrderByVariableNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.clauses.StreamingInput;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectExpression;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamAction;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhere;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWindow;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.LongAdder;

/**
 * Class responsible for desugar streaming pipeline into actual Ballerina code.
 *
 * @since 0.980.0
 */
public class StreamingCodeDesugar extends BLangNodeVisitor {

    static final String AGGREGATOR_OBJECT_NAME = "Aggregator";
    static final String GET_METHOD_NAME = "get";
    private static final String OUTPUT_FUNC_REFERENCE = "$lambda$streaming$output$function";
    private static final String ACTION_REF = "$lambda$stream$action$function";
    private static final String OUTPUT_FUNC_VAR_ARG = "$lambda$streaming$output$function$var$arg";
    private static final String WINDOW_FUNC_REFERENCE = "$lambda$streaming$window$reference";
    private static final String OUTPUT_PROCESS_FUNC_REFERENCE = "$lambda$streaming$output$process";
    private static final String ORDER_BY_PROCESS_FUNC_REFERENCE = "$lambda$streaming$ordering$process";
    private static final String ORDERING_FUNC_ARRAY_REFERENCE = "$lambda$streaming$orderby$funcarray$variable";
    private static final String ORDERING_FUNC_VAR_ARG = "$lambda$streaming$orderby$func$var$arg";
    private static final String FILTER_FUNC_REFERENCE = "$lambda$streaming$filter";
    private static final String HAVING_FUNC_REFERENCE = "$lambda$streaming$having";
    private static final String SELECT_WITH_GROUP_BY_FUNC_REFERENCE = "$lambda$streaming$groupby$select";
    private static final String JOIN_PROCESS_FUNC_REFERENCE = "$lambda$streaming$join$process";
    private static final String TABLE_JOIN_PROCESS_FUNC_REFERENCE = "$lambda$streaming$table$join$process";
    private static final String INPUT_STREAM_PARAM_REFERENCE = "$lambda$streaming$input$variable";
    private static final String FILTER_LAMBDA_PARAM_REFERENCE = "$lambda$streaming$filter$input$variable";
    private static final String HAVING_LAMBDA_PARAM_REFERENCE = "$lambda$streaming$having$input$variable";
    private static final String SELECT_LAMBDA_PARAM_REFERENCE = "$lambda$streaming$simple$select$input$variable";
    private static final String TABLE_JOIN_LAMBDA_PARAM_REFERENCE = "$lambda$streaming$table$join$input$variable";
    private static final String NEXT_PROCESS_LAMBDA_PARAM_REFERENCE = "$lambda$streaming$next$process$var$ref";
    private static final String JOIN_CONDITION_LAMBDA_PARAM_REFERENCE =
            "$lambda$streaming$join$onCondition$input$variable";
    private static final String SELECT_WITH_GROUP_BY_LAMBDA_PARAM_REFERENCE =
            "$lambda$streaming$groupby$select$input$variable";
    private static final String STREAM_EVENT_ARRAY_PARAM_REFERENCE = "$lambda$streaming$stream$event$variable";
    private static final String VAR_OUTPUT_EVENTS = "$lambda$streaming$output$process$output$events$variable";
    private static final String VAR_RESULTS_TABLE = "$lambda$streaming$table$join$on$condition$result$variable";
    private static final String VAR_FOREACH_VAL = "$lambda$streaming$foreach$key$val$variable";
    private static final String NEXT_PROCESS_METHOD_NAME = "process";
    private static final String STREAM_EVENT_OBJECT_NAME = "StreamEvent";
    private static final String WINDOW_OBJECT_NAME = "Window";
    private static final String ORDER_BY_PROCESS_OBJECT_NAME = "OrderBy";
    private static final String CREATE_OUTPUT_PROCESS_METHOD_NAME = "createOutputProcess";
    private static final String CREATE_FILTER_METHOD_NAME = "createFilter";
    private static final String CREATE_SELECT_WITH_GROUP_BY_METHOD_NAME = "createSelect";
    private static final String CREATE_ORDER_BY_METHOD_NAME = "createOrderBy";
    private static final String CREATE_STREAM_JOIN_PROCESS_METHOD_NAME = "createStreamJoinProcessor";
    private static final String CREATE_TABLE_JOIN_PROCESS_METHOD_NAME = "createTableJoinProcessor";
    private static final String SET_JOIN_PROPERTIES_METHOD_NAME = "setJoinProperties";
    private static final String SET_RHS_METHOD_NAME = "setRHS";
    private static final String SET_LHS_METHOD_NAME = "setLHS";
    private static final String LENGTH_WINDOW_METHOD_NAME = "length";
    private static final String EVENT_TYPE_VARIABLE_NAME = "eventType";
    private static final String BUILD_STREAM_EVENT_METHOD_NAME = "buildStreamEvent";
    private static final String INIT_PERSISTENCE_METHOD_NAME = "initPersistence";
    private static final String REGISTER_SNAPSHOTABLE_METHOD_NAME = "registerSnapshotable";
    private static final String STREAM_SUBSCRIBE_METHOD_NAME = "subscribe";
    private static final String JOIN_TYPE = "JoinType";
    private static final String SCOPE_NAME_ARG_NAME = "scopeName";

    private static final CompilerContext.Key<StreamingCodeDesugar> STREAMING_DESUGAR_KEY =
            new CompilerContext.Key<>();
    private static final String ORDER_BY_FIELD_ATTR = "fieldFuncs";
    private static final String STR_ENDING = "ENDING";
    private static final String ORDER_TYPE_ASC = "ASC" + STR_ENDING;
    private static final String NEXT_PROCESS_POINTER_ARG_NAME = "nextProcessPointer";
    private static final String ON_CONDITION_NAMED_ARG_NAME = "conditionFunc";

    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final SymbolEnter symbolEnter;
    private final Desugar desugar;
    private final Names names;
    private final Types types;
    private final StreamsPreSelectDesuagr preSelectDesuagr;
    private final StreamsPostSelectDesugar postSelectDesugar;
    private final StreamingAggregatorArrayBuilder aggregatorArrayBuilder;
    private final BLangAnonymousModelHelper anonymousModelHelper;
    private BLangDiagnosticLog dlog;
    private SymbolEnv env;
    private List<BLangStatement> stmts;
    private BLangVariableReference rhsStream, lhsStream;
    private Stack<BLangExpression> exprStack = new Stack<>();
    private BType outputEventType;
    private Stack<BVarSymbol> nextProcessVarSymbolStack = new Stack<>();
    private Stack<BVarSymbol> joinProcessorStack = new Stack<>();
    private boolean isJoin;
    private boolean isInJoin;
    private boolean isTableJoin;
    private long currentQueryStmt;
    // Contains the StreamEvent.data variable args in conditional lambda functions like where and join on condition
    private Map<String, String> streamAliasMap;

    private StreamingCodeDesugar(CompilerContext context) {
        context.put(STREAMING_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.preSelectDesuagr = StreamsPreSelectDesuagr.getInstance(context);
        this.postSelectDesugar = StreamsPostSelectDesugar.getInstance(context);
        this.aggregatorArrayBuilder = StreamingAggregatorArrayBuilder.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
    }

    public static StreamingCodeDesugar getInstance(CompilerContext context) {
        StreamingCodeDesugar desugar = context.get(STREAMING_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new StreamingCodeDesugar(context);
        }

        return desugar;
    }

    public BLangBlockStmt desugar(BLangForever foreverStatement) {
        this.currentQueryStmt = 0;
        stmts = new ArrayList<>();
        List<? extends StatementNode> statementNodes = foreverStatement.getStreamingQueryStatements();

        // Generate Streaming Consumer Function
        statementNodes.forEach(statementNode -> ((BLangStatement) statementNode).accept(this));
        injectInitPersistenceStmt(foreverStatement);
        return ASTBuilderUtil.createBlockStmt(foreverStatement.pos, stmts);
    }

    private void injectInitPersistenceStmt(BLangForever foreverStatement) {
        BInvokableSymbol initPersistenceInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(foreverStatement.pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(INIT_PERSISTENCE_METHOD_NAME)).symbol;
        BLangInvocation initPersistenceMethodInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(foreverStatement.pos, initPersistenceInvokableSymbol, new ArrayList<>(),
                        symResolver);
        BLangExpressionStmt initPersistenceExpressionStmt = (BLangExpressionStmt) TreeBuilder.
                createExpressionStatementNode();
        initPersistenceExpressionStmt.pos = foreverStatement.pos;
        initPersistenceExpressionStmt.expr = initPersistenceMethodInvocation;
        stmts.add(initPersistenceExpressionStmt);
    }

    private void injectRegisterSnapshotableStmt(List<BLangStatement> stmts, DiagnosticPos pos, String key,
                                                BLangSimpleVarRef varRef) {
        BInvokableSymbol registerSnapshotableInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(pos, env, Names.STREAMS_MODULE).scope
                .lookup(new Name(REGISTER_SNAPSHOTABLE_METHOD_NAME)).symbol;
        List<BLangExpression> args = new ArrayList<>();
        args.add(ASTBuilderUtil.createLiteral(pos, symTable.stringType, key));
        args.add(varRef);
        BLangInvocation registerSnapshotableMethodInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, registerSnapshotableInvokableSymbol, args, symResolver);
        BLangExpressionStmt initPersistenceExpressionStmt = (BLangExpressionStmt) TreeBuilder.
                createExpressionStatementNode();
        initPersistenceExpressionStmt.pos = pos;
        initPersistenceExpressionStmt.expr = registerSnapshotableMethodInvocation;
        stmts.add(initPersistenceExpressionStmt);
    }

    @Override
    public void visit(BLangStreamingQueryStatement queryStmt) {

        outputEventType = null;
        rhsStream = null;
        lhsStream = null;
        joinProcessorStack.clear();
        streamAliasMap = new HashMap<>();
        exprStack.empty();
        isJoin = false;
        isInJoin = false;
        isTableJoin = false;
        env = queryStmt.cachedEnv;
        //Construct the elements to publish events to output stream
        BLangStreamAction streamAction = (BLangStreamAction) queryStmt.getStreamingAction();
        streamAction.accept(this);

        BLangOrderBy orderBy = (BLangOrderBy) queryStmt.getOrderbyClause();
        if (orderBy != null) {
            orderBy.accept(this);
        }

        BLangSelectClause selectClause = (BLangSelectClause) queryStmt.getSelectClause();
        resolveSelectExpressions(selectClause, queryStmt.getStreamingInput());
        selectClause.accept(this);

        BLangJoinStreamingInput joinStreamingInput = (BLangJoinStreamingInput) queryStmt.getJoiningInput();
        if (joinStreamingInput != null) {
            isInJoin = true;
            isJoin = true;
            BLangStreamingInput streamingInput = (BLangStreamingInput) joinStreamingInput.getStreamingInput();
            createStreamAliasMap(streamingInput);
            resolveJoinProperties(queryStmt.getStreamingInput().getStreamReference(),
                                  joinStreamingInput.getStreamingInput().getStreamReference());
            joinStreamingInput.accept(this);
            isInJoin = false;
        }

        //Build elements to consume events from input stream
        BLangStreamingInput streamingInput = (BLangStreamingInput) queryStmt.getStreamingInput();
        createStreamAliasMap(streamingInput);
        streamingInput.accept(this);

        //increment the query statement counter
        currentQueryStmt++;
    }

    private void resolveSelectExpressions(BLangSelectClause selectClause, StreamingInput streamingInput) {
        if (selectClause.isSelectAll()) {
            List<SelectExpressionNode> selectExprList = new ArrayList<>();
            BLangSimpleVarRef input = (BLangSimpleVarRef) streamingInput.getStreamReference();
            List<BField> inputStructFieldList = ((BRecordType) ((BStreamType) input.type).constraint).fields;
            for (BField field : inputStructFieldList) {
                BLangSelectExpression selectExpr = (BLangSelectExpression) TreeBuilder.createSelectExpressionNode();
                BLangFieldBasedAccess expr = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
                expr.expr = ASTBuilderUtil.createVariableRef(selectClause.pos, input.symbol);
                expr.symbol = field.symbol;
                expr.type = field.symbol.type;
                expr.pos = selectClause.pos;
                expr.field = ASTBuilderUtil.createIdentifier(selectClause.pos, field.name.value);
                selectExpr.setExpression(expr);
                selectExprList.add(selectExpr);
            }
            selectClause.setSelectExpressions(selectExprList);
        }
    }

    private void resolveJoinProperties(ExpressionNode lhsStreamReference, ExpressionNode rhsStreamReference) {
        if (isTableReference(lhsStreamReference)) {
            lhsStream = (BLangVariableReference) rhsStreamReference;
            rhsStream = (BLangVariableReference) lhsStreamReference;
            exprStack.push((BLangExpression) lhsStreamReference);
            isTableJoin = true;
        } else if (isTableReference(rhsStreamReference)) {
            lhsStream = (BLangVariableReference) lhsStreamReference;
            rhsStream = (BLangVariableReference) rhsStreamReference;
            exprStack.push((BLangExpression) rhsStreamReference);
            isTableJoin = true;
        } else {
            lhsStream = (BLangVariableReference) lhsStreamReference;
            rhsStream = (BLangVariableReference) rhsStreamReference;
            isTableJoin = false;
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
    public void visit(BLangOrderBy orderBy) {
        //creating function pointer array which represents the ordering fields
        BObjectTypeSymbol orderByObjSymbol = (BObjectTypeSymbol) symResolver.
                resolvePkgSymbol(orderBy.pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(ORDER_BY_PROCESS_OBJECT_NAME)).symbol;
        BType orderingFuncArrayType = getSortFuncArrayType((BObjectType) orderByObjSymbol.type);

        //create symbol representing the ordering function pointer array
        BVarSymbol orderingFuncArrayVarSymbol =
                new BVarSymbol(0, new Name(getVariableName(ORDERING_FUNC_ARRAY_REFERENCE)),
                        orderByObjSymbol.pkgID, orderingFuncArrayType, env.scope.owner);

        //create RHS expression for the ordering function array
        BLangArrayLiteral orderingFuncArrExpr = (BLangArrayLiteral) TreeBuilder.createArrayLiteralExpressionNode();
        orderingFuncArrExpr.exprs = new ArrayList<>();
        orderingFuncArrExpr.type = orderingFuncArrayType;

        BLangArrayLiteral sortModesArrayExpr = (BLangArrayLiteral) TreeBuilder.createArrayLiteralExpressionNode();
        sortModesArrayExpr.exprs = new ArrayList<>();
        sortModesArrayExpr.type = new BArrayType(symTable.stringType);

        BLangSimpleVariableDef orderingFuncArrDef =
                createOrderingFuncArrayVarDef(orderBy, orderingFuncArrayVarSymbol, orderingFuncArrExpr);
        stmts.add(orderingFuncArrDef);

        for (OrderByVariableNode node : orderBy.getVariables()) {
            BLangOrderByVariable orderByVariable = (BLangOrderByVariable) node;
            orderingFuncArrExpr.exprs.add(createOrderingFunctionPointer((BLangExpression) orderByVariable
                    .getVariableReference()));
            String fieldOrderType = orderByVariable.getOrderByType() + STR_ENDING;
            if (fieldOrderType.isEmpty()) {
                fieldOrderType = ORDER_TYPE_ASC;
            }
            sortModesArrayExpr.exprs.add(ASTBuilderUtil.createLiteral(orderByVariable.pos, symTable.stringType,
                    fieldOrderType.toLowerCase()));
        }

        //Create OrderBy process definition
        BLangSimpleVariableDef orderByProcessInvokableTypeVariableDef =
                createOrderByProcessDef(orderBy, orderingFuncArrDef.var, sortModesArrayExpr);
        stmts.add(orderByProcessInvokableTypeVariableDef);
    }

    private BLangSimpleVariableDef createOrderingFuncArrayVarDef(BLangOrderBy orderBy,
                                                                 BVarSymbol orderingFuncArrayVarSymbol,
                                                                 BLangArrayLiteral orderingFuncArrExpr) {
        BLangSimpleVariable orderingFuncArrVariable = ASTBuilderUtil.
                createVariable(orderBy.pos, getVariableName(ORDERING_FUNC_ARRAY_REFERENCE), orderingFuncArrExpr.type,
                        orderingFuncArrExpr, orderingFuncArrayVarSymbol);

        orderingFuncArrVariable.typeNode = ASTBuilderUtil.createTypeNode(orderingFuncArrExpr.type);
        return ASTBuilderUtil.createVariableDef(orderBy.pos, orderingFuncArrVariable);
    }

    private BLangSimpleVariableDef createOrderByProcessDef(BLangOrderBy orderBy,
                                                           BLangSimpleVariable orderingFuncArrVariable,
                                                           BLangArrayLiteral orderingTypeArrExpr) {
        BLangSimpleVarRef orderingFuncPointerArr = ASTBuilderUtil.createVariableRef(orderBy.pos,
                orderingFuncArrVariable.symbol);
        List<BLangExpression> args = new ArrayList<>();
        args.add(createNextProcessFuncPointer(orderBy.pos));
        args.add(orderingFuncPointerArr);
        args.add(orderingTypeArrExpr);

        BInvokableSymbol orderByProcessInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(orderBy.pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(CREATE_ORDER_BY_METHOD_NAME)).symbol;
        BType orderByProcessInvokableType = orderByProcessInvokableSymbol.type.getReturnType();
        BVarSymbol orderByProcessInvokableTypeVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(ORDER_BY_PROCESS_FUNC_REFERENCE)), orderByProcessInvokableSymbol.pkgID,
                orderByProcessInvokableType, env.scope.owner);
        nextProcessVarSymbolStack.push(orderByProcessInvokableTypeVarSymbol);

        BLangInvocation orderByProcessMethodInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(orderBy.pos, orderByProcessInvokableSymbol, args, symResolver);
        BLangSimpleVariable orderByProcessInvokableTypeVariable = ASTBuilderUtil.
                createVariable(orderBy.pos, getVariableName(ORDER_BY_PROCESS_FUNC_REFERENCE),
                        orderByProcessInvokableType, orderByProcessMethodInvocation,
                        orderByProcessInvokableTypeVarSymbol);

        return ASTBuilderUtil.createVariableDef(orderBy.pos, orderByProcessInvokableTypeVariable);
    }

    private BType getSortFuncArrayType(BObjectType orderByType) {
        BType orderingFuncArrayType;
        List<BField> fields = orderByType.fields;
        //get the ordering functions array type
        //e.g. (function (map<anydata>)returns any)[] ...
        orderingFuncArrayType = fields.stream().filter(field -> field.name.value.equals(ORDER_BY_FIELD_ATTR))
                .findFirst().map(field -> field.type).orElse(null);
        return orderingFuncArrayType;
    }

    private BLangExpression createOrderingFunctionPointer(BLangExpression expr) {
        BLangSimpleVariable orderFuncMapVariable = createMapTypeVariable(getVariableName(ORDERING_FUNC_VAR_ARG),
                expr.pos, env);
        BLangType valueType = ASTBuilderUtil.createTypeNode(symTable.anydataType);
        BLangLambdaFunction orderingFunc = createLambdaWithVarArg(expr.pos, new BLangSimpleVariable[]
                {orderFuncMapVariable}, valueType);
        BLangBlockStmt lambdaBody = orderingFunc.function.body;

        BLangExpression refactoredExpr = desugar.addConversionExprIfRequired((BLangExpression) postSelectDesugar
                .rewrite(expr, orderFuncMapVariable.symbol, outputEventType), symTable.anydataType);

        addReturnStmt(expr.pos, lambdaBody, refactoredExpr);
        return orderingFunc;
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        BLangLambdaFunction lambdaFunction = (BLangLambdaFunction) streamAction.getInvokableBody();
        lambdaFunction.accept(this);
    }

    // This method creates the constructs to publish output events.
    //
    // eg: Below query,
    //
    //        => (TeacherOutput[] outputEvents) {
    //            foreach var e in outputEvents {
    //                outputStream.publish(e);
    //            }
    //        }
    //
    // convert into below constructs.
    //
    //       function (map<anydata>[]) outputFunc = function (map<anydata>[] events) {
    //          TeacherOutput[] outputArr = [];
    //          function(TeacherOutput[]) lambda0 = <lambda from above stream action>;
    //          foreach var m in events {
    //              TeacherOutput t = <TeacherOutput>TeacherOutput.constructFrom(m);
    //              outputArr.push(t);
    //          }
    //          lambda0(outputArr);
    //      };
    //
    //      streams:OutputProcess outputProcess = streams:createOutputProcess(outputFunc);
    @Override
    public void visit(BLangLambdaFunction lambdaFunction) {
        BLangSimpleVariable outputFuncArg =
                (BLangSimpleVariable) lambdaFunction.getFunctionNode().getParameters().get(0);
        outputEventType = ((BArrayType) outputFuncArg.type).eType;

        // Create the lambda function expr for outputFunc
        //
        //      function (map<anydata>[] events) {
        //          ....
        //          ....
        //      };
        BLangLambdaFunction outputLambdaFunc = createLambdaWithVarArg(lambdaFunction.pos, new BLangSimpleVariable[]{
                ASTBuilderUtil.createVariable(outputFuncArg.pos, getVariableName(OUTPUT_FUNC_VAR_ARG),
                symTable.anydataMapArrayType, null, new BVarSymbol(0,
                names.fromString(getVariableName(OUTPUT_FUNC_VAR_ARG)), lambdaFunction.function.symbol.pkgID,
                symTable.anydataMapArrayType, lambdaFunction.function.symbol.owner))},
                ASTBuilderUtil.createTypeNode(symTable.nilType));

        // Create the variable for outputFunc lambda to refer later
        //
        //      function (map<anydata>[]) outputFunc = function (map<anydata>[] events) {
        //          ....
        //          ....
        //      };
        BLangSimpleVariable outputFuncVariable = ASTBuilderUtil.
                createVariable(outputLambdaFunc.pos, getVariableName(OUTPUT_FUNC_REFERENCE),
                outputLambdaFunc.type, outputLambdaFunc, new BInvokableSymbol(SymTag.VARIABLE, 0,
                names.fromString(OUTPUT_FUNC_REFERENCE), lambdaFunction.function.symbol.pkgID,
                outputLambdaFunc.type, lambdaFunction.function.symbol.owner));

        outputFuncVariable.symbol.owner = env.enclInvokable.symbol;
        BLangSimpleVariableDef outputFuncVarDef = ASTBuilderUtil.createVariableDef(outputLambdaFunc.pos,
                outputFuncVariable);

        // Re-arrange the envs so that the stream action lambda has to be a child of the outputFunc lambda when
        // resolving closures
        SymbolEnv varInitEnv =
                SymbolEnv.createVarInitEnv(outputFuncVarDef.var, env, outputFuncVarDef.var.symbol);
        SymbolEnv funcEnv = SymbolEnv
                .createFunctionEnv(outputLambdaFunc.function, outputLambdaFunc.function.symbol.scope, varInitEnv);
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(outputLambdaFunc.function.body, funcEnv);

        // Fill the body of the outputFunc lambda
        generateOutputLambdaBody(lambdaFunction, outputLambdaFunc, blockEnv);

        stmts.add(outputFuncVarDef);
        stmts.add(generateOutputProcessorDef(outputLambdaFunc, outputFuncVarDef.var));
    }

    private BLangSimpleVariableDef generateOutputProcessorDef(BLangLambdaFunction outputLambdaFunc,
                                                              BLangSimpleVariable outputStreamFunctionVariable) {
        // Create reference to the outputFunc to be passed into OutputProcessor
        BLangSimpleVarRef outputFuncVarRef =
                ASTBuilderUtil.createVariableRef(outputLambdaFunc.pos, outputStreamFunctionVariable.symbol);
        BInvokableSymbol createOutputProcessInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(outputLambdaFunc.pos, env, Names.STREAMS_MODULE).scope
                .lookup(new Name(CREATE_OUTPUT_PROCESS_METHOD_NAME)).symbol;

        BType outputProcessInvokableType = createOutputProcessInvokableSymbol.type.getReturnType();
        BVarSymbol outputProcessInvokableTypeVarSymbol =
                new BVarSymbol(0, new Name(getVariableName(OUTPUT_PROCESS_FUNC_REFERENCE)),
                createOutputProcessInvokableSymbol.pkgID, outputProcessInvokableType, env.scope.owner);
        nextProcessVarSymbolStack.push(outputProcessInvokableTypeVarSymbol);

        // streams:createOutputProcess(outputFunc);
        BLangInvocation outputProcessMethodInvocation =
                ASTBuilderUtil.createInvocationExprForMethod(outputLambdaFunc.pos, createOutputProcessInvokableSymbol,
                Lists.of(outputFuncVarRef), symResolver);

        // Variable for streams:createOutputProcess(outputFunc);
        BLangSimpleVariable outputProcessInvokableTypeVariable = ASTBuilderUtil.
                createVariable(outputLambdaFunc.pos, getVariableName(OUTPUT_PROCESS_FUNC_REFERENCE),
                        outputProcessInvokableType, outputProcessMethodInvocation, outputProcessInvokableTypeVarSymbol);

        // streams:OutputProcess outputProcess = streams:createOutputProcess(outputFunc);
        return ASTBuilderUtil.
                createVariableDef(outputLambdaFunc.pos, outputProcessInvokableTypeVariable);
    }

    private void generateOutputLambdaBody(BLangLambdaFunction lambdaFunction, BLangLambdaFunction outputLambdaFunc,
                                          SymbolEnv blockEnv) {

        // function(TeacherOutput[]) lambda0 = <lambda from above stream action>;
        BLangSimpleVariable streamActionLambdaVariable = ASTBuilderUtil.
                createVariable(lambdaFunction.pos, getVariableName(ACTION_REF), lambdaFunction.type, lambdaFunction,
                new BInvokableSymbol(SymTag.VARIABLE, 0, names.fromString(ACTION_REF),
                lambdaFunction.function.symbol.pkgID, lambdaFunction.type, outputLambdaFunc.function.symbol));

        lambdaFunction.cachedEnv =
                SymbolEnv.createVarInitEnv(streamActionLambdaVariable, blockEnv, streamActionLambdaVariable.symbol);
        streamActionLambdaVariable.symbol.owner = outputLambdaFunc.function.symbol;

        // function(TeacherOutput[]) lambda0 = <lambda from above stream action>;
        BLangSimpleVariableDef streamActionLambdaVariableDef =
                ASTBuilderUtil.createVariableDef(lambdaFunction.pos, streamActionLambdaVariable);

        outputLambdaFunc.function.body.stmts.add(streamActionLambdaVariableDef);


        // TeacherOutput[] outputArr = [];
        // foreach var m in events {
        //      TeacherOutput t = <TeacherOutput>TeacherOutput.constructFrom(m);
        //      outputArr.push(t);
        // }
        BLangSimpleVarRef outputArrayRef =
                createResultArrayRefInForEach(outputLambdaFunc.function.pos, outputEventType,
                outputLambdaFunc.function.symbol, outputLambdaFunc.function.body);
        BLangForeach foreach =
                createForEachStmtForArrayConversion(outputLambdaFunc.function.pos, outputLambdaFunc.function.symbol,
                outputLambdaFunc.function.symbol.params.get(0), outputArrayRef);
        outputLambdaFunc.function.body.stmts.add(foreach);

        // lambda0(outputArr);
        BLangInvocation streamActionInvocation = ASTBuilderUtil.createInvocationExprForMethod(lambdaFunction.pos,
                (BInvokableSymbol) streamActionLambdaVariable.symbol, Lists.of(outputArrayRef), symResolver);
        streamActionInvocation.functionPointerInvocation = true;
        BLangExpressionStmt streamActionInvocationStmt =
                ASTBuilderUtil.createExpressionStmt(lambdaFunction.function.pos, outputLambdaFunc.function.body);
        streamActionInvocationStmt.expr = streamActionInvocation;
    }

    private BLangSimpleVarRef createResultArrayRefInForEach(DiagnosticPos pos, BType outputType, BSymbol symbol,
                                                            BLangBlockStmt body) {
        BType outputArrayType = new BArrayType(outputType);
        BLangArrayLiteral arrayLiteralExpr = (BLangArrayLiteral) TreeBuilder.createArrayLiteralExpressionNode();
        arrayLiteralExpr.exprs = new ArrayList<>();
        arrayLiteralExpr.type = outputArrayType;
        BLangSimpleVariable outputArrayVariable =
                ASTBuilderUtil.createVariable(pos, VAR_OUTPUT_EVENTS, outputArrayType, arrayLiteralExpr, null);
        defineVariable(outputArrayVariable, symbol.pkgID, symbol);
        BLangSimpleVariableDef outputArrayVarDef =
                ASTBuilderUtil.createVariableDef(pos, outputArrayVariable);
        body.addStatement(outputArrayVarDef);
        return ASTBuilderUtil.createVariableRef(pos, outputArrayVariable.symbol);
    }

    private BLangForeach createForEachStmtForArrayConversion(DiagnosticPos pos, BSymbol owner, BSymbol
            inputCollectionSymbol, BLangSimpleVarRef outputArrayRef) {

        BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
        foreach.pos = pos;
        foreach.collection = ASTBuilderUtil.createVariableRef(pos, inputCollectionSymbol);
        types.setForeachTypedBindingPatternType(foreach);

        final BLangSimpleVariable foreachVariable = createForeachVariable(pos, owner, foreach.varType);
        BLangSimpleVarRef foreachVarRef = ASTBuilderUtil.createVariableRef(pos, foreachVariable.symbol);
        foreach.variableDefinitionNode = ASTBuilderUtil.createVariableDef(foreachVariable.pos, foreachVariable);
        foreach.isDeclaredWithVar = true;
        // foreach v in events {
        //     outputEvents[outputEvents.length()] =  <T>T.constructFrom(v);
        // }
        BLangBlockStmt foreachBody = ASTBuilderUtil.createBlockStmt(pos);

        // Note - outputEvents.length();
        BLangInvocation lengthInvocation = createLengthInvocation(pos, (BVarSymbol) outputArrayRef.symbol);

        // create `outputEvents[outputEvents.length()] = <T>T.constructFrom(v);` assignment stmt
        BLangIndexBasedAccess indexAccessExpr = ASTBuilderUtil.createIndexAccessExpr(outputArrayRef, lengthInvocation);
        indexAccessExpr.type = ((BArrayType) outputArrayRef.type).eType;

        // e.g. TeacherOutput.constructFrom(m);
        BLangExpression constructFromInvocation = buildConstructFromInvocation(foreachVarRef, indexAccessExpr.type);

        //<TeacherOutput>TeacherOutput.constructFrom(m);
        BLangExpression outputTypeConversionExpr = desugar.addConversionExprIfRequired(constructFromInvocation,
                indexAccessExpr.type);

        //TeacherOutput t = <TeacherOutput>TeacherOutput.constructFrom(m);
        BLangAssignment assignment = ASTBuilderUtil.createAssignmentStmt(pos, foreachBody);
        assignment.setExpression(outputTypeConversionExpr);
        assignment.varRef = indexAccessExpr;
        foreach.body = foreachBody;
        return foreach;
    }

    private BLangExpression buildConstructFromInvocation(BLangSimpleVarRef mapVarRef, BType outputEventType) {
        BVarSymbol typeSymbol = new BVarSymbol(0, outputEventType.tsymbol.name, mapVarRef.symbol.pkgID,
                                               outputEventType, mapVarRef.symbol.owner);
        BLangSimpleVarRef outputTypeRef = ASTBuilderUtil.createVariableRef(mapVarRef.pos, typeSymbol);
        //special case for varRefs of Types;
        outputTypeRef.type = symTable.typeDesc;
        outputTypeRef.symbol.tag = TypeTags.TYPEDESC;
        BSymbol createMethodSymbol =
                symResolver.lookupLangLibMethod(outputTypeRef.type, names.fromString("constructFrom"));
        return ASTBuilderUtil.createInvocationExprForMethod(mapVarRef.pos, (BInvokableSymbol) createMethodSymbol,
                Lists.of(outputTypeRef, mapVarRef), symResolver);
    }


 /*  This method converts the select clause of the streaming query in to Ballerina native constructs.

     eg: Below query,
               select inputStream.name, inputStream.age, sum (inputStream.age) as sumAge, count() as count

     convert into below constructs.
                streams:Select select = streams:createSelect(outputProcess.process, [streams:sum(), streams:count()],
                    (streams:StreamEvent e) => string {
                        return <string>e.data["inputStream.name"];
                    },
                    (streams:StreamEvent e, streams:Aggregator[] aggregatorArr)  => map {
                        return {
                            "name": e.data["inputStream.name"],
                            "age": e.data["inputStream.age"],
                            "sumAge": aggregatorArr[0].process(e.data["inputStream.age"], e.eventType),
                            "count": aggregatorArr[1].process((), e.eventType)
                        };
                    });
 */

    @Override
    public void visit(BLangSelectClause selectClause) {

        // If exists, visit having node first
        HavingNode havingNode = selectClause.getHaving();
        if (havingNode != null) {
            ((BLangHaving) havingNode).accept(this);
        }

        // Create select statement
        createSelectStatement(selectClause);
    }

    private void createSelectStatement(BLangSelectClause selectClause) {

        // 1st arg for createSelect
        BLangExpression nextProcessMethodAccess = createNextProcessFuncPointer(selectClause.pos);

        // [streams:sum(), streams:count(), ... etc], 2nd arg
        BLangExpression aggregateArray = createAggregatorArray(selectClause);

        // ((streams:StreamEvent e) returns string)[], 3rd arg
        BLangExpression groupingLambda;
        if (selectClause.getGroupBy() != null) {
            groupingLambda = createGroupByLambdas(selectClause);
        } else {
            groupingLambda = ASTBuilderUtil.createLiteral(selectClause.pos, symTable.nilType, Names.NIL_VALUE);
        }

        // (streams:StreamEvent e, streams:Aggregator[] aggregatorArr)  returns map<anydata>, 4th arg of createSelect
        BLangExpression aggregatorLambda = createAggregatorLambda(selectClause);


        BInvokableSymbol groupBySelectInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(selectClause.pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(CREATE_SELECT_WITH_GROUP_BY_METHOD_NAME)).symbol;

        BType selectWithGroupByInvokableType = groupBySelectInvokableSymbol.type.getReturnType();
        BVarSymbol selectWithGroupByInvokableTypeVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(SELECT_WITH_GROUP_BY_FUNC_REFERENCE)), groupBySelectInvokableSymbol.pkgID,
                selectWithGroupByInvokableType, env.scope.owner);
        nextProcessVarSymbolStack.push(selectWithGroupByInvokableTypeVarSymbol);


        List<BLangExpression> args = Lists.of(nextProcessMethodAccess, aggregateArray, groupingLambda, aggregatorLambda,
                ASTBuilderUtil.createNamedArg(SCOPE_NAME_ARG_NAME, ASTBuilderUtil.createLiteral(selectClause.pos,
                symTable.stringType, getScopeName())));

        // streams:createSelect( ... )
        BLangInvocation selectWithGroupByInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(selectClause.pos, groupBySelectInvokableSymbol,
                                              args, symResolver);

        // streams:Select variable name
        BLangSimpleVariable selectWithGroupByInvokableTypeVariable = ASTBuilderUtil.
                createVariable(selectClause.pos, getVariableName(SELECT_WITH_GROUP_BY_FUNC_REFERENCE),
                        selectWithGroupByInvokableType, selectWithGroupByInvocation,
                        selectWithGroupByInvokableTypeVarSymbol);

        // streams:Select select = streams:createSelect(...);
        BLangSimpleVariableDef selectWithGroupByInvokableTypeVariableDef = ASTBuilderUtil.
                createVariableDef(selectClause.pos, selectWithGroupByInvokableTypeVariable);
        stmts.add(selectWithGroupByInvokableTypeVariableDef);
    }

    // [streams:sum(), streams:count(), .. etc ]
    private BLangArrayLiteral createAggregatorArray(BLangSelectClause selectClause) {
        BLangArrayLiteral expr = (BLangArrayLiteral) TreeBuilder.createArrayLiteralExpressionNode();
        expr.exprs = new ArrayList<>();

        List<SelectExpressionNode> selectExpressions = selectClause.getSelectExpressions();
        for (SelectExpressionNode select : selectExpressions) {
            BLangExpression selectExpr = (BLangExpression) select.getExpression();
            aggregatorArrayBuilder.collectAggregators(expr.exprs, selectExpr, env);
        }
        expr.type = new BArrayType(symResolver.resolvePkgSymbol(selectClause.pos, env, Names.STREAMS_MODULE)
                .scope.lookup(new Name(AGGREGATOR_OBJECT_NAME)).symbol.type);
        return expr;
    }

    boolean isReturnTypeMatching(DiagnosticPos pos, String objectName, BInvokableSymbol invokableSymbol) {
        BSymbol expectedRetType = symResolver.
                resolvePkgSymbol(pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(objectName)).symbol;
        BSymbol actualRetType = invokableSymbol.getType().retType.tsymbol;
        return expectedRetType == actualRetType;
    }

    // Object.process
    private BLangExpression createNextProcessFuncPointer(DiagnosticPos pos) {
        BVarSymbol nextProcessInvokableTypeVarSymbol = nextProcessVarSymbolStack.pop();
        return createNextProcessFuncPointer(pos, nextProcessInvokableTypeVarSymbol);
    }

    private BLangExpression createNextProcessFuncPointer(DiagnosticPos pos, BVarSymbol nextProcessVarSymbol) {
        nextProcessVarSymbol.closure = true;
        BInvokableSymbol nextProcessInvokableSymbol = getNextProcessFunctionSymbol(nextProcessVarSymbol);
        BLangSimpleVarRef nextProcessSimpleVarRef = ASTBuilderUtil.createVariableRef(pos, nextProcessVarSymbol);
        return createFieldBasedAccessForProcessFunc(pos, nextProcessInvokableSymbol, nextProcessSimpleVarRef);
    }

    private BLangLambdaFunction createAggregatorLambda(BLangSelectClause selectClause) {

        //streams:StreamEvent e,
        BLangSimpleVariable varSelectFnStreamEvent = this.createStreamEventArgVariable(
                getVariableName(SELECT_LAMBDA_PARAM_REFERENCE), selectClause.pos, env);

        //streams:Aggregator[] aggregatorArr
        BLangSimpleVariable varAggregatorArray =
                this.createAggregatorTypeVariable(getVariableName(SELECT_WITH_GROUP_BY_LAMBDA_PARAM_REFERENCE),
                        selectClause.pos, env);

        /* (streams:StreamEvent e, streams:Aggregator[] aggregatorArr)  => map<any> {

            });
        */
        BLangLambdaFunction selectWithGroupBy = createAggregatorLambdaWithParams(varAggregatorArray,
                varSelectFnStreamEvent, selectClause.pos);

        //lambda function body
        createAggregatorLambdaBody(selectClause, selectWithGroupBy);

        return selectWithGroupBy;
    }

    private void createAggregatorLambdaBody(BLangSelectClause selectClause, BLangLambdaFunction selectWithGroupBy) {
        BLangSimpleVariable varStreamEvent = selectWithGroupBy.function.requiredParams.get(0);
        BLangSimpleVariable varAggregatorArray = selectWithGroupBy.function.requiredParams.get(1);

        /* TeacherOutput teacherOutput = {
                        name: e.data["inputStream.name"],
                        age: e.data["inputStream.age"],
                        sumAge: aggregatorArr1[0].process(e.data["inputStream.age"], e.eventType),
                        count: aggregatorArr1[1].process((), e.eventType),
                        ...
                        ...
                    };
        */
        BLangExpression outputEvent = createOutputEventObj(selectClause, varStreamEvent, varAggregatorArray);

        // return teacherOutput;
        addReturnStmt(selectClause.pos, selectWithGroupBy.function.body, outputEvent);
    }

    private void addReturnStmt(DiagnosticPos pos, BLangBlockStmt targetBody, BLangExpression expr) {
        BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pos;
        returnStmt.expr = expr;
        targetBody.stmts.add(returnStmt);
    }

    private BLangExpression createOutputEventObj(BLangSelectClause selectClause,
                                                 BLangSimpleVariable varStreamEvent,
                                                 BLangSimpleVariable varAggregatorArray) {

        BLangRecordLiteral outputEventRecordLiteral = ASTBuilderUtil.createEmptyRecordLiteral(selectClause.pos,
                symTable.mapAnydataType);
        /* {
                    name: e.data["inputStream.name"]
                    age: e.data["inputStream.age"],
                    sumAge: check <int> aggregatorArr1[0].process(e.data["inputStream.age"], e.eventType),
                    count: check <int> aggregatorArr1[1].process((), e.eventType),
                    ...
           }
        */
        outputEventRecordLiteral.keyValuePairs = getFieldListInSelectClause(selectClause.pos,
                selectClause.getSelectExpressions(), varStreamEvent.symbol, varAggregatorArray.symbol);
        return outputEventRecordLiteral;
    }

    private BLangLambdaFunction createAggregatorLambdaWithParams(BLangSimpleVariable varAggregatorArray,
                                                                 BLangSimpleVariable varSelectFnStreamEvent,
                                                                 DiagnosticPos pos) {
        BLangType selectLambdaReturnType = ASTBuilderUtil.createTypeNode(symTable.mapAnydataType);

        return createLambdaFunction(pos, new ArrayList<>(Arrays.asList(varSelectFnStreamEvent, varAggregatorArray)),
                selectLambdaReturnType);
    }

    private BLangExpression createGroupByLambdas(BLangSelectClause selectClause) {
        BLangArrayLiteral arr = (BLangArrayLiteral) TreeBuilder.createArrayLiteralExpressionNode();
        arr.exprs = new ArrayList<>();
        selectClause.getGroupBy().getVariables()
                .forEach(groupingVar -> arr.exprs.add(createGroupByLambda((BLangExpression) groupingVar)));
        arr.type = new BArrayType(arr.exprs.get(0).type);
        arr.pos = ((BLangGroupBy) selectClause.getGroupBy()).pos;
        return arr;
    }

    private BLangExpression createGroupByLambda(BLangExpression expr) {
        BLangSimpleVariable varStreamEvent = this.createStreamEventArgVariable(
                getVariableName(SELECT_LAMBDA_PARAM_REFERENCE), expr.pos, env);
        BLangType typeNode = ASTBuilderUtil.createTypeNode(symTable.anydataType);
        // (streams:StreamEvent e) returns anydata { .. }
        BLangLambdaFunction groupingLambda = createLambdaWithVarArg(expr.pos, new BLangSimpleVariable[]{varStreamEvent},
                typeNode);
        BLangBlockStmt groupByLambda = groupingLambda.function.body;
        BLangExpression mapAccessExpr;

        mapAccessExpr = (BLangExpression) preSelectDesuagr.rewrite(expr, null, streamAliasMap, rhsStream,
                null, null, varStreamEvent.symbol);

        // return <anydata>e.data[<fieldName in string>];
        BLangExpression conversionExpr = desugar.addConversionExprIfRequired(mapAccessExpr, symTable.anydataType);
        addReturnGroupByFieldStmt(groupByLambda, conversionExpr);
        return groupingLambda;
    }

    private void addReturnGroupByFieldStmt(BLangBlockStmt groupByLambda, BLangExpression expr) {
        addReturnStmt(expr.pos, groupByLambda, expr);
    }

    private  BLangLambdaFunction createLambdaWithVarArg(DiagnosticPos pos, BLangSimpleVariable[] varArgs,
                                                       TypeNode typeNode) {
        return createLambdaFunction(pos, Arrays.asList(varArgs), typeNode);
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        BLangBinaryExpr onExpr = (BLangBinaryExpr) joinStreamingInput.getOnExpression();
        if (onExpr != null && !isTableJoin) {
            BLangSimpleVariable lhsDataMap =
                    createMapTypeVariable(getVariableName(JOIN_CONDITION_LAMBDA_PARAM_REFERENCE), onExpr.lhsExpr.pos,
                                          env);
            BLangSimpleVariable rhsDataMap = createMapTypeVariable(
                    getVariableName(JOIN_CONDITION_LAMBDA_PARAM_REFERENCE) + 1, onExpr.rhsExpr.pos, env);
            TypeNode typeNode = ASTBuilderUtil.createTypeNode(symTable.booleanType);
            BLangLambdaFunction conditionFunc = createLambdaWithVarArg(joinStreamingInput.pos, new BLangSimpleVariable[]
                    {lhsDataMap, rhsDataMap}, typeNode);
            BLangBlockStmt funcBody = conditionFunc.function.body;
            BType lhsType = onExpr.lhsExpr.type;
            BType rhsType = onExpr.rhsExpr.type;
            BLangBinaryExpr refactoredOnExpr = (BLangBinaryExpr) preSelectDesuagr.rewrite(onExpr,
                    new BSymbol[]{lhsDataMap.symbol, rhsDataMap.symbol}, streamAliasMap, rhsStream);

            refactoredOnExpr.lhsExpr = desugar.addConversionExprIfRequired(refactoredOnExpr.lhsExpr, lhsType);
            refactoredOnExpr.rhsExpr = desugar.addConversionExprIfRequired(refactoredOnExpr.rhsExpr, rhsType);
            onExpr = refactoredOnExpr;

            //onExpr.lhsExpr = desugar.addConversionExprIfRequired(onExpr.lhsExpr, on)
            addReturnStmt(onExpr.pos, funcBody, onExpr);
            createJoinProcessorStmt(joinStreamingInput, conditionFunc);
        } else if (onExpr == null && !isTableJoin) {
            createJoinProcessorStmt(joinStreamingInput, null);
        } else {
            createTableJoinProcessorStmt(joinStreamingInput);
        }

        BLangStreamingInput streamingInput = (BLangStreamingInput) joinStreamingInput.getStreamingInput();
        streamingInput.accept(this);
    }

    private void createTableJoinProcessorStmt(BLangJoinStreamingInput joinStreamingInput) {
        BLangExpression nextProcessMethodAccess = createNextProcessFuncPointer(joinStreamingInput.pos);
        BInvokableSymbol tableJoinProcessorInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(joinStreamingInput.pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(CREATE_TABLE_JOIN_PROCESS_METHOD_NAME)).symbol;
        BType tableJoinProcessorReturnType = tableJoinProcessorInvokableSymbol.type.getReturnType();
        BVarSymbol tableJoinProcessorVarSymbol =
                new BVarSymbol(0, new Name(getVariableName(TABLE_JOIN_PROCESS_FUNC_REFERENCE)),
                               tableJoinProcessorInvokableSymbol.pkgID, tableJoinProcessorReturnType, env.scope.owner);
        joinProcessorStack.push(tableJoinProcessorVarSymbol);
        BSymbol joinTypesSymbol = symResolver.resolvePkgSymbol(joinStreamingInput.pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(JOIN_TYPE)).symbol;
        BLangLiteral joinType =
                ASTBuilderUtil.createLiteral(joinStreamingInput.pos, symTable.stringType,
                                             joinStreamingInput.getJoinType().toUpperCase());

        List<BLangExpression> args = new ArrayList<>();
        args.add(nextProcessMethodAccess);
        args.add(desugar.addConversionExprIfRequired(joinType, joinTypesSymbol.type));
        args.add(createOnConditionLambdaStatement(exprStack.pop()));
        // streams:createTableJoinProcessor( ... )
        BLangInvocation createTableJoinInvocation = ASTBuilderUtil.createInvocationExprForMethod(
                joinStreamingInput.pos, tableJoinProcessorInvokableSymbol, args, symResolver);
        // streams:TableJoinProcessor variable name
        BLangSimpleVariable joinInvokableTypeVariable = ASTBuilderUtil.
                createVariable(joinStreamingInput.pos, getVariableName(TABLE_JOIN_PROCESS_FUNC_REFERENCE),
                               tableJoinProcessorReturnType, createTableJoinInvocation,
                               tableJoinProcessorVarSymbol);

        BLangSimpleVariableDef tableJoinProcessorInvokableTypeDef =
                ASTBuilderUtil.createVariableDef(joinStreamingInput.pos, joinInvokableTypeVariable);
        stmts.add(tableJoinProcessorInvokableTypeDef);
    }
    /* Create below lambda function.
       function (streams:StreamEvent s) returns map<anydata>[] {
           map<anydata>[] result = [];
           table<Stock> stocks = queryStocksTable(<string> s.data["twitterStream.company"], 1);
           foreach var r in stocks {
               result[result.length()] = map<anydata>map<anydata>.constructFrom(r);
           }
           return result;
       }
   */
    private BLangExpression createOnConditionLambdaStatement(BLangExpression onConditionExpr) {
        // create lambda signature and body
        BLangSimpleVariable streamEventVarArg = createStreamEventArgVariable(
                getVariableName(TABLE_JOIN_LAMBDA_PARAM_REFERENCE), onConditionExpr.pos, env);
        BLangLambdaFunction conditionFunc =
                createLambdaWithVarArg(onConditionExpr.pos, new BLangSimpleVariable[]{streamEventVarArg},
                                       ASTBuilderUtil.createTypeNode(new BArrayType(symTable.mapAnydataType)));
        BLangBlockStmt lambdaBody = conditionFunc.function.body;

        BLangSimpleVarRef outputArrayRef = createResultArrayRefInForEach(conditionFunc.function.pos,
                symTable.mapAnydataType, conditionFunc.function.symbol, lambdaBody);

        // table<Stock> stocks = queryStocksTable(<string> s.data["twitterStream.company"], 1);
        onConditionExpr = (BLangExpression) preSelectDesuagr.rewrite(onConditionExpr, null, streamAliasMap,
                rhsStream, null, null, streamEventVarArg.symbol);
        BTableType tableType = (BTableType) onConditionExpr.type;

        BLangSimpleVariable resultTableVariable = ASTBuilderUtil.createVariable(onConditionExpr.pos, VAR_RESULTS_TABLE,
                tableType, onConditionExpr, null);
        defineVariable(resultTableVariable, env.scope.owner.pkgID, env.scope.owner);
        BLangSimpleVariableDef resultTableDef =
                ASTBuilderUtil.createVariableDef(onConditionExpr.pos, resultTableVariable);
        lambdaBody.addStatement(resultTableDef);
        BLangForeach foreach = createForEachStmtForArrayConversion(conditionFunc.function.pos, conditionFunc
                .function.symbol, resultTableVariable.symbol, outputArrayRef);
        lambdaBody.addStatement(foreach);
        // return statement for result
        addReturnStmt(onConditionExpr.pos, lambdaBody, ASTBuilderUtil.createVariableRef
                (onConditionExpr.pos, outputArrayRef.symbol));
        return conditionFunc;
    }

    private void createJoinProcessorStmt(BLangJoinStreamingInput joinStreamingInput,
                                         BLangLambdaFunction conditionExpr) {

        BLangExpression nextProcessMethodAccess = createNextProcessFuncPointer(joinStreamingInput.pos);

        BInvokableSymbol joinProcessorInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(joinStreamingInput.pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(CREATE_STREAM_JOIN_PROCESS_METHOD_NAME)).symbol;

        BType joinProcessorReturnType = joinProcessorInvokableSymbol.type.getReturnType();

        BVarSymbol joinProcessorVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(JOIN_PROCESS_FUNC_REFERENCE)), joinProcessorInvokableSymbol.pkgID,
                joinProcessorReturnType, env.scope.owner);

        BSymbol joinTypesSymbol = symResolver.resolvePkgSymbol(joinStreamingInput.pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(JOIN_TYPE)).symbol;
        BLangLiteral joinType = ASTBuilderUtil.createLiteral(joinStreamingInput.pos, symTable.stringType,
                joinStreamingInput.getJoinType().toUpperCase());

        joinProcessorStack.push(joinProcessorVarSymbol);

        List<BLangExpression> args = new ArrayList<>();
        args.add(nextProcessMethodAccess);
        args.add(desugar.addConversionExprIfRequired(joinType, joinTypesSymbol.type));

        // streams:createJoinProcess( ... )
        BLangInvocation createJoinInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(joinStreamingInput.pos, joinProcessorInvokableSymbol, args, symResolver);

        if (conditionExpr != null) {
            BLangNamedArgsExpression conditionNamedArgExpression =
                    ASTBuilderUtil.createNamedArg(ON_CONDITION_NAMED_ARG_NAME, conditionExpr);
            createJoinInvocation.requiredArgs.add(conditionNamedArgExpression);
        }

        // streams:JoinProcess variable name
        BLangSimpleVariable joinInvokableTypeVariable =
                ASTBuilderUtil.createVariable(joinStreamingInput.pos, getVariableName(JOIN_PROCESS_FUNC_REFERENCE),
                joinProcessorReturnType, createJoinInvocation, joinProcessorVarSymbol);

        // streams:Select select = streams:createSelect(...);
        BLangSimpleVariableDef joinProcessorInvokableTypeDef =
                ASTBuilderUtil.createVariableDef(joinStreamingInput.pos, joinInvokableTypeVariable);
        stmts.add(joinProcessorInvokableTypeDef);
    }

    // This method create necessary Ballerina native constructs to consume events from stream based on the 'from'
    // statement of the streaming query.
    //
    // eg: Below query,
    //          from inputStream
    //
    // converts into below constructs.
    //
    //          inputStream.subscribe((Teacher t) => {
    //              streams:StreamEvent[] eventArr = streams:buildStreamEvent(t);
    //              filter.process(eventArr);
    //          });
    //
    @Override
    public void visit(BLangStreamingInput streamingInput) {
        //Lambda function parameter
        BType inputStreamType = getInputType(streamingInput);

        desugarWhereClause((BLangWhere) streamingInput.getAfterStreamingCondition());

        desugarWindowClause(streamingInput);

        desugarWhereClause((BLangWhere) streamingInput.getBeforeStreamingCondition());

        if (!nextProcessVarSymbolStack.empty()) {
            BVarSymbol nextProcessInvokableTypeVarSymbol = nextProcessVarSymbolStack.pop();
            BVarSymbol lambdaParameterVarSymbol = new BVarSymbol(0,
                    new Name(getVariableName(INPUT_STREAM_PARAM_REFERENCE)), inputStreamType.tsymbol.pkgID,
                    inputStreamType, env.scope.owner);

            BLangSimpleVariable inputStreamLambdaFunctionVariable = ASTBuilderUtil.createVariable(streamingInput.pos,
                    getVariableName(INPUT_STREAM_PARAM_REFERENCE), inputStreamType, null,
                    lambdaParameterVarSymbol);
            inputStreamLambdaFunctionVariable.typeNode = ASTBuilderUtil.createTypeNode(inputStreamType);
            // Tag variables as closures.
            nextProcessInvokableTypeVarSymbol.closure = true;
            TypeNode returnType = ASTBuilderUtil.createTypeNode(symTable.nilType);

            //Construct lambda function which consumes events
            BLangLambdaFunction streamSubscriberLambdaFunction = createLambdaFunction(streamingInput.pos,
                    new ArrayList<>(Lists.of(inputStreamLambdaFunctionVariable)), returnType);
            BLangBlockStmt lambdaBody = streamSubscriberLambdaFunction.function.body;

            //varRef to the input event
            BLangExpression inputEventRef =
                    ASTBuilderUtil.createVariableRef(streamingInput.pos, inputStreamLambdaFunctionVariable.symbol);

            BInvokableSymbol streamEventBuilderInvokableSymbol = (BInvokableSymbol) symResolver.
                    resolvePkgSymbol(streamingInput.pos, env, Names.STREAMS_MODULE).
                    scope.lookup(new Name(BUILD_STREAM_EVENT_METHOD_NAME)).symbol;

            BVarSymbol streamEventArrayTypeVarSymbol = new BVarSymbol(0,
                    new Name(getVariableName(STREAM_EVENT_ARRAY_PARAM_REFERENCE)),
                    streamEventBuilderInvokableSymbol.pkgID, streamEventBuilderInvokableSymbol.type.getReturnType(),
                    env.scope.owner);

            BLangSimpleVariable streamEventArrayTypeVariable = ASTBuilderUtil.
                    createVariable(streamingInput.pos, getVariableName(STREAM_EVENT_ARRAY_PARAM_REFERENCE),
                    streamEventArrayTypeVarSymbol.type, null, streamEventArrayTypeVarSymbol);

            List<BLangExpression> args = new ArrayList<>();
            args.add(inputEventRef);

            String streamReferenceSymbolName = ((BLangSimpleVarRef) streamingInput.getStreamReference()).symbol
                    .toString();
            if (streamAliasMap.containsKey(streamReferenceSymbolName)) {
                args.add(ASTBuilderUtil.createLiteral(streamingInput.pos, symTable.stringType,
                        streamAliasMap.get(streamReferenceSymbolName)));
            } else {
                args.add(ASTBuilderUtil.createLiteral(streamingInput.pos, symTable.stringType, ((BLangSimpleVarRef)
                        streamingInput.getStreamReference()).symbol.toString()));
            }

            streamEventArrayTypeVariable.expr = ASTBuilderUtil.
                    createInvocationExprForMethod(streamingInput.pos, streamEventBuilderInvokableSymbol, args,
                            symResolver);

            BLangSimpleVariableDef streamEventArrayTypeVariableDef =
                    ASTBuilderUtil.createVariableDef(streamingInput.pos, streamEventArrayTypeVariable);

            lambdaBody.stmts.add(streamEventArrayTypeVariableDef);

            //Function invocation to call output process
            BInvokableSymbol nextProcessInvokableSymbol =
                    getNextProcessFunctionSymbol(nextProcessInvokableTypeVarSymbol);

            BLangSimpleVarRef streamEventArrayRef = ASTBuilderUtil.createVariableRef(streamingInput.pos,
                    streamEventArrayTypeVarSymbol);
            List<BLangExpression> nextProcessVariables = new ArrayList<>(1);
            nextProcessVariables.add(streamEventArrayRef);
            BLangInvocation nextProcessMethodInvocation = ASTBuilderUtil.
                    createInvocationExprForMethod(streamingInput.pos, nextProcessInvokableSymbol, nextProcessVariables,
                            symResolver);

            nextProcessMethodInvocation.expr = ASTBuilderUtil.createVariableRef(streamingInput.pos,
                    nextProcessInvokableTypeVarSymbol);
            BLangExpressionStmt nextProcessExpressionStmt = (BLangExpressionStmt) TreeBuilder.
                    createExpressionStatementNode();
            nextProcessExpressionStmt.pos = streamingInput.pos;
            nextProcessExpressionStmt.expr = nextProcessMethodInvocation;
            lambdaBody.stmts.add(nextProcessExpressionStmt);

            //Create function call - stream1.subscribe(lambda_function)
            BLangExpressionStmt inputStreamSubscribeStatement = (BLangExpressionStmt) TreeBuilder.
                    createExpressionStatementNode();
            inputStreamSubscribeStatement.pos = streamingInput.pos;
            BInvokableSymbol subscribeMethodSymbol =
                    (BInvokableSymbol) symResolver.lookupLangLibMethod(symTable.streamType,
                    names.fromString(STREAM_SUBSCRIBE_METHOD_NAME));
            BLangSimpleVarRef inputStreamVarRef = ASTBuilderUtil.createVariableRef(streamingInput.pos,
                    ((BLangSimpleVarRef) streamingInput.getStreamReference()).symbol);
            // Note: No need to set invocationExpr.expr for langLib functions as they are in requiredArgs
            inputStreamSubscribeStatement.expr = ASTBuilderUtil.
                    createInvocationExprForMethod(streamingInput.pos, subscribeMethodSymbol,
                    Lists.of(inputStreamVarRef, streamSubscriberLambdaFunction), symResolver);

            //Add stream subscriber function to stmts
            stmts.add(inputStreamSubscribeStatement);
        }
    }

    private void desugarWindowClause(BLangStreamingInput streamingInput) {
        BLangWindow windowClauseNode = (BLangWindow) streamingInput.getWindowClause();
        if (windowClauseNode != null) {
            windowClauseNode.accept(this);
        } else if (isJoin && !isTableReference(streamingInput.getStreamReference())) {
            windowClauseNode = createDefaultLengthWindow(streamingInput);
            streamingInput.setWindowClause(windowClauseNode);
            windowClauseNode.accept(this);
        }
    }

    private void desugarWhereClause(BLangWhere whereClause) {
        if (whereClause != null) {
            whereClause.accept(this);
        }
    }

    private BType getInputType(BLangStreamingInput streamingInput) {
        BType lambdaParameterType;
        if (isTableReference(streamingInput.getStreamReference())) {
            lambdaParameterType = ((BTableType) ((BLangExpression) streamingInput.getStreamReference()).type)
                    .constraint;
        } else {
            lambdaParameterType = ((BStreamType) ((BLangExpression) streamingInput.getStreamReference()).type)
                    .constraint;
        }
        return lambdaParameterType;
    }

    private BLangWindow createDefaultLengthWindow(BLangStreamingInput streamingInput) {
        List<BLangExpression> args = new ArrayList<>();
        args.add(ASTBuilderUtil.createLiteral(streamingInput.pos, symTable.intType, 1L));

        BInvokableSymbol windowSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(streamingInput.pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(LENGTH_WINDOW_METHOD_NAME)).symbol;

        BLangInvocation lengthInvocation = ASTBuilderUtil.createInvocationExprForMethod(streamingInput.pos,
                windowSymbol, args, symResolver);
        lengthInvocation.pkgAlias = ASTBuilderUtil.createIdentifier(streamingInput.pos, Names.STREAMS_MODULE.value);
        lengthInvocation.type = windowSymbol.retType;
        lengthInvocation.argExprs = args;

        BLangWindow window = (BLangWindow) TreeBuilder.createWindowClauseNode();
        window.pos = streamingInput.pos;
        window.setFunctionInvocation(lengthInvocation);
        return window;
    }

    /*
        e.g: window lengthWindow(5) will be turned into,

        streams:LengthWindow lengthWindow = streams:lengthWindow(select.process, [5]);
     */
    @Override
    public void visit(BLangWindow window) {

        BLangInvocation invocation = (BLangInvocation) window.getFunctionInvocation();

        //converting the window parameters into an array of parameters
        BLangListConstructorExpr windowParamArrExpr =
                (BLangListConstructorExpr) TreeBuilder.createListConstructorExpressionNode();
        windowParamArrExpr.exprs = new ArrayList<>();
        windowParamArrExpr.type = new BArrayType(symTable.anyType);
        windowParamArrExpr.exprs.addAll(invocation.argExprs);
        invocation.argExprs = Lists.of(windowParamArrExpr);
        invocation.requiredArgs = invocation.argExprs;

        convertFieldAccessArgsToStringLiteral(invocation);

        //checks for the symbol, if not exists, then set the pkgAlias to STREAMS_STDLIB_PACKAGE_NAME
        BInvokableSymbol windowInvokableSymbol = getInvokableSymbol(invocation, WINDOW_OBJECT_NAME, true);

        if (windowInvokableSymbol != null) {
            if (isReturnTypeMatching(invocation.pos, WINDOW_OBJECT_NAME, windowInvokableSymbol)) {
                //Create event filter definition
                BVarSymbol nextProcessInvokableTypeVarSymbol;
                if (rhsStream != null) {
                    nextProcessInvokableTypeVarSymbol = joinProcessorStack.peek();
                } else {
                    nextProcessInvokableTypeVarSymbol = nextProcessVarSymbolStack.pop();
                }

                BLangExpression nextProcessMethodAccess = createNextProcessFuncPointer(window.pos,
                        nextProcessInvokableTypeVarSymbol);

                BType windowInvokableType = windowInvokableSymbol.type.getReturnType();

                String lhsOrRhsWindow = isInJoin ? "RHS" : "LHS";
                BVarSymbol windowInvokableTypeVarSymbol =
                        new BVarSymbol(0, new Name(getVariableName(WINDOW_FUNC_REFERENCE + lhsOrRhsWindow)),
                                       windowInvokableSymbol.pkgID, windowInvokableType, env.scope.owner);
                nextProcessVarSymbolStack.push(windowInvokableTypeVarSymbol);

                BLangNamedArgsExpression nextProcPointer =
                        ASTBuilderUtil.createNamedArg(NEXT_PROCESS_POINTER_ARG_NAME, nextProcessMethodAccess);

                invocation.type = windowInvokableType;

                //these should be added after type-checking
                invocation.requiredArgs.add(nextProcPointer);

                BLangSimpleVariableDef windowDef = createVariableDef(invocation, windowInvokableType,
                        windowInvokableTypeVarSymbol, window.pos, WINDOW_FUNC_REFERENCE + lhsOrRhsWindow);
                stmts.add(windowDef);

                BLangSimpleVarRef windowVarRef = ASTBuilderUtil.createVariableRef(window.pos,
                        windowDef.getVariable().symbol);
                injectRegisterSnapshotableStmt(stmts, window.pos, windowDef.getVariable().getName().value,
                        windowVarRef);

                if (!joinProcessorStack.empty()) {
                    if (isTableJoin) {
                        attachWindowToTableJoinProcessor(window, windowInvokableTypeVarSymbol, lhsStream, rhsStream);
                    } else {
                        if (isInJoin) {
                            attachWindowToStreamJoinProcessor(window, windowInvokableTypeVarSymbol, SET_RHS_METHOD_NAME,
                                    rhsStream);
                        } else {
                            attachWindowToStreamJoinProcessor(window, windowInvokableTypeVarSymbol, SET_LHS_METHOD_NAME,
                                    lhsStream);
                        }
                    }
                }
            } else {
                dlog.error(invocation.pos, DiagnosticCode.INCOMPATIBLE_TYPES, Names.STREAMS_MODULE.value + ":" +
                        WINDOW_OBJECT_NAME, windowInvokableSymbol.getType().retType.tsymbol);
            }
        } else {
            dlog.error(invocation.pos, DiagnosticCode.UNDEFINED_FUNCTION, invocation.name);
        }
    }

    private void attachWindowToTableJoinProcessor(BLangWindow window, BVarSymbol windowInvokableTypeVarSymbol,
                                                  BLangVariableReference stream, BLangVariableReference table) {
        BVarSymbol joinProcessVarSymbol = joinProcessorStack.peek();
        BInvokableSymbol methodInvokableSymbol =
                getInvokableSymbolOfObject(joinProcessVarSymbol, SET_JOIN_PROPERTIES_METHOD_NAME);
        List<BLangExpression> args = new ArrayList<>();
        // table name
        String tableReferenceSymbolName = table.symbol.toString();
        args.add(ASTBuilderUtil.createLiteral(window.pos, symTable.stringType, streamAliasMap
                .getOrDefault(tableReferenceSymbolName, tableReferenceSymbolName)));
        // stream name
        String streamReferenceSymbolName = stream.symbol.toString();
        args.add(ASTBuilderUtil.createLiteral(window.pos, symTable.stringType, streamAliasMap
                .getOrDefault(streamReferenceSymbolName, streamReferenceSymbolName)));
        // window instance
        args.add(ASTBuilderUtil.createVariableRef(window.pos, windowInvokableTypeVarSymbol));
        BLangInvocation methodInvocation =
                ASTBuilderUtil.createInvocationExprForMethod(window.pos, methodInvokableSymbol, args, symResolver);
        methodInvocation.expr = ASTBuilderUtil.createVariableRef(window.pos, joinProcessVarSymbol);
        BLangExpressionStmt methodInvocationStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        methodInvocationStmt.pos = window.pos;
        methodInvocationStmt.expr = methodInvocation;
        stmts.add(methodInvocationStmt);
    }

    BInvokableSymbol getInvokableSymbol(BLangInvocation invocation, String modelType, boolean logError) {
        BInvokableSymbol invokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(invocation.pos, env, names.fromString(invocation.pkgAlias.value)).
                scope.lookup(new Name(invocation.name.value)).symbol;
        if (invokableSymbol == null && invocation.pkgAlias.value.isEmpty()) {
            BSymbol symbol = symResolver.
                    resolvePkgSymbol(invocation.pos, env, Names.STREAMS_MODULE).
                    scope.lookup(new Name(invocation.name.value)).symbol;

            if (symbol != null && SymbolKind.FUNCTION.equals(symbol.kind) &&
                    isReturnTypeMatching(invocation.pos, modelType, (BInvokableSymbol) symbol)) {
                invokableSymbol = (BInvokableSymbol) symbol;
            } else {
                if (logError) {
                    dlog.error(invocation.pos, DiagnosticCode.INVALID_STREAMING_MODEL_TYPE, modelType, invocation.name);
                }
            }

            invocation.pkgAlias.value = Names.STREAMS_MODULE.value;
        }
        return invokableSymbol;
    }

    private BLangSimpleVariableDef createVariableDef(BLangExpression expr, BType exprType,
                                                     BVarSymbol exprTypeSymbol,
                                                     DiagnosticPos pos, String exprVarName) {

        BLangSimpleVariable windowInvokableTypeVariable =
                ASTBuilderUtil.createVariable(pos, getVariableName(exprVarName), exprType, expr,
                        exprTypeSymbol);
        return ASTBuilderUtil.createVariableDef(pos, windowInvokableTypeVariable);
    }

    private void convertFieldAccessArgsToStringLiteral(BLangInvocation invocation) {
        //converting BLangFieldBaseAccess to BLangLiteral of string type, in requiredArgs
        convertFieldAccessArgsToStringLiteral(invocation.requiredArgs);
    }

    private void convertFieldAccessArgsToStringLiteral(List<BLangExpression> exprs) {
        BLangLiteral streamEventParameter;
        for (int i = 0; i < exprs.size(); i++) {
            BLangExpression expr = exprs.get(i);
            if (expr.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
                String variableName = ((BLangFieldBasedAccess) expr).expr.toString();
                if (streamAliasMap.containsKey(variableName)) {
                    variableName = streamAliasMap.get(variableName);
                    ((BLangSimpleVarRef) ((BLangFieldBasedAccess) expr).expr).variableName.value = variableName;
                }
                streamEventParameter = createStringLiteral(expr.pos, expr.toString());
                exprs.set(i, desugar.addConversionExprIfRequired(streamEventParameter, symTable.anyType));
            } else if (expr.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR) {
                BLangListConstructorExpr arrayLiteral = (BLangListConstructorExpr) expr;
                convertFieldAccessArgsToStringLiteral(arrayLiteral.exprs);
            } else {
                exprs.set(i, desugar.addConversionExprIfRequired(expr, symTable.anyType));
                expr.typeChecked = false;
            }
        }
    }

    private BLangLiteral createStringLiteral(DiagnosticPos pos, String value) {
        BLangLiteral stringLit = new BLangLiteral();
        stringLit.pos = pos;
        stringLit.value = value;
        stringLit.type = symTable.stringType;
        return stringLit;
    }

    private void attachWindowToStreamJoinProcessor(BLangWindow window, BVarSymbol windowInvokableTypeVarSymbol,
                                                   String methodName, BLangVariableReference streamRef) {
        BVarSymbol joinProcessVarSymbol = joinProcessorStack.peek();
        BInvokableSymbol methodInvokableSymbol = getInvokableSymbolOfObject(joinProcessVarSymbol, methodName);
        BLangExpression joinProcessorVarRef = ASTBuilderUtil.createVariableRef(window.pos, joinProcessVarSymbol);

        List<BLangExpression> args = new ArrayList<>();
        String streamReferenceSymbolName = streamRef.symbol.toString();
        args.add(ASTBuilderUtil.createLiteral(window.pos, symTable.stringType,
                streamAliasMap.getOrDefault(streamReferenceSymbolName, streamReferenceSymbolName)));
        args.add(ASTBuilderUtil.createVariableRef(window.pos, windowInvokableTypeVarSymbol));
        BLangInvocation methodInvocation = ASTBuilderUtil.createInvocationExprForMethod(window.pos,
                methodInvokableSymbol, args, symResolver);
        methodInvocation.expr = joinProcessorVarRef;
        BLangExpressionStmt methodInvocationStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        methodInvocationStmt.pos = window.pos;
        methodInvocationStmt.expr = methodInvocation;
        stmts.add(methodInvocationStmt);
    }

    private BLangExpression createFieldBasedAccessForProcessFunc(DiagnosticPos pos,
                                                                       BInvokableSymbol nextProcessInvokableSymbol,
                                                                       BLangSimpleVarRef nextProcessSimpleVarRef) {
        BLangSimpleVariable nextProcessVar =
                this.createStreamEventArrayArgVariable(getVariableName(NEXT_PROCESS_LAMBDA_PARAM_REFERENCE), pos, env);
        BLangLambdaFunction nextProcessLambda = createLambdaWithVarArg(pos, new BLangSimpleVariable[] {nextProcessVar},
                ASTBuilderUtil.createTypeNode(symTable.nilType));
        BLangBlockStmt body = nextProcessLambda.function.body;
        BLangSimpleVarRef streamEvents = ASTBuilderUtil.createVariableRef(pos, nextProcessVar.symbol);
        BLangInvocation processInvocation = ASTBuilderUtil.createInvocationExprForMethod(pos,
                nextProcessInvokableSymbol, Collections.singletonList(streamEvents), symResolver);
        processInvocation.expr = nextProcessSimpleVarRef;
        BLangExpressionStmt stmt = ASTBuilderUtil.createExpressionStmt(pos, body);
        stmt.expr = processInvocation;
        return nextProcessLambda;
    }

    //
    // Below method creates the constructs to perform filtering based on the 'where' clause of the streaming query.
    //
    // eg: Below query,
    //          where inputStream.age > 25
    //
    // converts into below constructs.
    //
    //          streams:Filter filter = streams:createFilter(select.process, (map<anydata> o) => boolean {
    //              return <int>(o["inputStream.age"]) > 25;
    //          });
    //
    //
    @Override
    public void visit(BLangWhere where) {
        BLangLambdaFunction lambda = createConditionLambda(where.pos, FILTER_LAMBDA_PARAM_REFERENCE);
        //always the condition lambda has one required param
        BSymbol varSymbol = lambda.function.requiredParams.get(0).symbol;
        BLangExpression conditionExpr = (BLangExpression) preSelectDesuagr.rewrite((BLangNode) where.getExpression(),
                new BSymbol[]{varSymbol}, streamAliasMap, rhsStream);
        visitFilter(where.pos, conditionExpr, lambda, FILTER_FUNC_REFERENCE);
    }

    //
    // Below method creates the constructs to perform filtering based on the 'having' clause of the streaming query.
    //
    // eg: Below query,
    //          having age > 25
    //
    // converts into below constructs.
    //
    //          streams:Filter filter = streams:createFilter(outputProcess.process, (map<anydata> o) => boolean {
    //              return <int>(o["OUTPUT.age"]) > 25;
    //          });
    //
    //
    @Override
    public void visit(BLangHaving having) {
        BLangLambdaFunction lambda = createConditionLambda(having.pos, HAVING_LAMBDA_PARAM_REFERENCE);
        //always the condition lambda has one required param
        BSymbol varSymbol = lambda.function.requiredParams.get(0).symbol;
        BLangExpression conditionExpr = (BLangExpression) postSelectDesugar.rewrite((BLangNode) having.getExpression(),
                varSymbol, outputEventType);
        visitFilter(having.pos, conditionExpr, lambda, HAVING_FUNC_REFERENCE);
    }

    //------------------------------------- Methods required for filter / having -----------------------------------
    private void visitFilter(DiagnosticPos pos, BLangExpression expr, BLangLambdaFunction lambda,
            String filterVarName) {
        BLangBlockStmt lambdaBody = lambda.function.body;
        // Return statement with having condition
        BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pos;
        returnStmt.expr = expr;
        lambdaBody.stmts.add(returnStmt);

        //Create having (filter) definition
        BLangExpression nextProcessMethodAccess = createNextProcessFuncPointer(pos);

        // Having will also use the same filter invokable
        BInvokableSymbol havingInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(CREATE_FILTER_METHOD_NAME)).symbol;
        BType havingInvokableType = havingInvokableSymbol.type.getReturnType();
        BVarSymbol havingInvokableTypeVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(filterVarName)), havingInvokableSymbol.pkgID,
                havingInvokableType, env.scope.owner);
        nextProcessVarSymbolStack.push(havingInvokableTypeVarSymbol);

        List<BLangExpression> args = new ArrayList<>();
        args.add(nextProcessMethodAccess);
        args.add(lambda);

        BLangInvocation havingMethodInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, havingInvokableSymbol, args, symResolver);

        BLangSimpleVariableDef filterDef = createVariableDef(havingMethodInvocation, havingInvokableType,
                havingInvokableTypeVarSymbol, pos, filterVarName);
        stmts.add(filterDef);
    }

    private BLangLambdaFunction createConditionLambda(DiagnosticPos pos, String varName) {
        //Create lambda function Variable
        BLangSimpleVariable lambdaFunctionVariable =
                this.createMapTypeVariable(getVariableName(varName), pos, env);
        BLangType returnType = ASTBuilderUtil.createTypeNode(symTable.booleanType);

        //Create new lambda function to process the output events
        return createLambdaFunction(pos, new ArrayList<>(Lists.of(lambdaFunctionVariable)), returnType);
    }

    //----------------------------------------- Util Methods ---------------------------------------------------------

    private String getVariableName(String name) {
        return name + getScopeName();
    }

    private String getScopeName() {
        return ((env.scope.owner != null) ? "$" + env.scope.owner.name.value + "$" : "$") + currentQueryStmt;
    }

    private void defineFunction(BLangFunction funcNode, BLangPackage targetPkg) {
        final BPackageSymbol packageSymbol = targetPkg.symbol;
        final SymbolEnv packageEnv = this.symTable.pkgEnvMap.get(packageSymbol);
        symbolEnter.defineNode(funcNode, packageEnv);
        packageEnv.enclPkg.functions.add(funcNode);
        packageEnv.enclPkg.topLevelNodes.add(funcNode);
    }

    private BLangSimpleVariable createMapTypeVariable(String variableName, DiagnosticPos pos, SymbolEnv env) {
        BType varType = symTable.mapAnydataType;
        BVarSymbol varSymbol = new BVarSymbol(0, new Name(variableName),
                varType.tsymbol.pkgID, varType, env.scope.owner);

        BLangSimpleVariable mapTypeVariable = ASTBuilderUtil.createVariable(pos, variableName,
                varType, null, varSymbol);
        mapTypeVariable.typeNode = ASTBuilderUtil.createTypeNode(varType);
        return mapTypeVariable;
    }

    private BLangSimpleVariable createStreamEventArgVariable(String variableName, DiagnosticPos pos, SymbolEnv env) {
        BType varType = createStreamEventType(pos, env);
        BVarSymbol varSymbol = new BVarSymbol(0, new Name(variableName),
                varType.tsymbol.pkgID, varType, env.scope.owner);

        return ASTBuilderUtil.createVariable(pos, variableName, varType, null, varSymbol);
    }

    private BLangSimpleVariable createStreamEventArrayArgVariable(String variableName, DiagnosticPos pos,
                                                                 SymbolEnv env) {
        BType varType = createStreamEventType(pos, env);
        BType nillableStreamEventType = BUnionType.create(null, varType, symTable.nilType);
        BArrayType arrayType = new BArrayType(nillableStreamEventType);
        BVarSymbol varSymbol = new BVarSymbol(0, new Name(variableName),
                                              varType.tsymbol.pkgID, arrayType, env.scope.owner);

        return ASTBuilderUtil.createVariable(pos, variableName, arrayType, null, varSymbol);
    }

    private BType createStreamEventType(DiagnosticPos pos, SymbolEnv env) {
        BObjectTypeSymbol recordTypeSymbol = (BObjectTypeSymbol) symResolver.
                resolvePkgSymbol(pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(STREAM_EVENT_OBJECT_NAME)).symbol;

        return recordTypeSymbol.type;
    }

    private BLangSimpleVariable createAggregatorTypeVariable(String variableName, DiagnosticPos pos, SymbolEnv env) {
        BInvokableSymbol invokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(CREATE_SELECT_WITH_GROUP_BY_METHOD_NAME)).symbol;

        BType varType = ((BInvokableType) invokableSymbol.params.get(3).type).paramTypes.get(1);
        BVarSymbol varSymbol = new BVarSymbol(0, new Name(variableName),
                varType.tsymbol.pkgID, varType, env.scope.owner);
        return ASTBuilderUtil.createVariable(pos, variableName, varType, null, varSymbol);
    }

    private static BVarSymbol getOutputEventFieldSymbol(BType outputEventType, String fieldName) {
        List<BField> recordTypeFieldList = ((BRecordType) outputEventType).fields;
        for (BField field : recordTypeFieldList) {
            if (field.getName().value.equals(fieldName)) {
                return field.symbol;
            }
        }
        return null;
    }

    static BInvokableSymbol getInvokableSymbolOfObject(BSymbol nextProcessInvokableTypeVarSymbol, String funcName) {
        List<BAttachedFunction> attachedFunctionsList = ((BObjectTypeSymbol)
                (nextProcessInvokableTypeVarSymbol).type.tsymbol).attachedFuncs;
        for (BAttachedFunction attachedFunction : attachedFunctionsList) {
            if (attachedFunction.funcName.toString().equals(funcName)) {
                return attachedFunction.symbol;
            }
        }
        throw new IllegalStateException("Couldn't evaluate the " + funcName + " method of the next processor : " +
                (nextProcessInvokableTypeVarSymbol).type.toString());
    }

    BInvokableSymbol getNextProcessFunctionSymbol(BSymbol nextProcessInvokableTypeVarSymbol) {
        return getInvokableSymbolOfObject(nextProcessInvokableTypeVarSymbol, NEXT_PROCESS_METHOD_NAME);
    }

    private List<BLangRecordLiteral.BLangRecordKeyValue> getFieldListInSelectClause
            (DiagnosticPos pos, List<? extends SelectExpressionNode> selectExprList,
             BVarSymbol streamEventSymbol, BVarSymbol aggregatorArraySymbol) {
        LongAdder aggregatorIndex = new LongAdder();
        List<BLangRecordLiteral.BLangRecordKeyValue> recordKeyValueList = new ArrayList<>();

        for (SelectExpressionNode expressionNode : selectExprList) {
            BLangSelectExpression selectExpression = (BLangSelectExpression) expressionNode;
            BLangRecordLiteral.BLangRecordKeyValue recordKeyValue = (BLangRecordLiteral.BLangRecordKeyValue)
                    TreeBuilder.createRecordKeyValue();

            createOutputMapKey(pos, selectExpression, recordKeyValue);

                BLangExpression expr = (BLangExpression) selectExpression.getExpression();
                BLangExpression refactoredExpr = (BLangExpression) preSelectDesuagr.rewrite(expr, null,
                        streamAliasMap, rhsStream, aggregatorArraySymbol, aggregatorIndex, streamEventSymbol);
                recordKeyValue.valueExpr = desugar.addConversionExprIfRequired(refactoredExpr, symTable.anydataType);

            recordKeyValueList.add(recordKeyValue);
        }
        return recordKeyValueList;
    }

    private void createOutputMapKey(DiagnosticPos pos, BLangSelectExpression selectExpression,
                                    BLangRecordLiteral.BLangRecordKeyValue recordKeyValue) {
        if (selectExpression.getIdentifier() != null) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
            varRef.variableName = ASTBuilderUtil.createIdentifier(pos, selectExpression.getIdentifier());
            recordKeyValue.key = new BLangRecordLiteral.BLangRecordKey(varRef);
            BVarSymbol symbol = getOutputEventFieldSymbol(outputEventType, selectExpression.getIdentifier());
            if (symbol == null) {
                dlog.error(varRef.pos, DiagnosticCode.UNDEFINED_OUTPUT_STREAM_ATTRIBUTE, varRef);
            } else {
                recordKeyValue.key.fieldSymbol = symbol;
            }
        } else {
            if (selectExpression.getExpression().getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
                BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
                varRef.variableName = ((BLangFieldBasedAccess) selectExpression.getExpression()).field;
                recordKeyValue.key = new BLangRecordLiteral.BLangRecordKey(varRef);
                BVarSymbol symbol = getOutputEventFieldSymbol(outputEventType,
                        ((BLangFieldBasedAccess) selectExpression.getExpression()).field.value);
                if (symbol == null) {
                    dlog.error(varRef.pos, DiagnosticCode.UNDEFINED_OUTPUT_STREAM_ATTRIBUTE, varRef);
                } else {
                    recordKeyValue.key.fieldSymbol = symbol;
                }
            } else {
                BLangExpression expr = (BLangExpression) selectExpression.getExpression();
                recordKeyValue.key = new BLangRecordLiteral.BLangRecordKey(expr);
                dlog.error(expr.pos, DiagnosticCode.UNDEFINED_SELECT_EXPR_ALIAS);
            }
        }
    }

    BLangFieldBasedAccess createFieldBasedEventTypeExpr(BVarSymbol streamEventSymbol, DiagnosticPos pos) {
        // o.eventType without the type
        BLangSimpleVarRef varStreamEventRef = ASTBuilderUtil.createVariableRef(pos, streamEventSymbol);
        return ASTBuilderUtil.createFieldAccessExpr(varStreamEventRef, ASTBuilderUtil.createIdentifier(pos,
                EVENT_TYPE_VARIABLE_NAME));
    }

    private BLangLambdaFunction createLambdaFunction(DiagnosticPos pos,
                                                     List<BLangSimpleVariable> lambdaFunctionVariable,
                                                     TypeNode returnType) {
        BLangLambdaFunction lambdaFunction = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        BLangFunction func = ASTBuilderUtil.createFunction(pos,
                anonymousModelHelper.getNextAnonymousFunctionKey(env.enclPkg.packageID));
        lambdaFunction.function = func;
        BLangBlockStmt lambdaBody = ASTBuilderUtil.createBlockStmt(pos);
        func.requiredParams.addAll(lambdaFunctionVariable);
        func.setReturnTypeNode(returnType);
        func.desugaredReturnType = true;
        defineFunction(func, env.enclPkg);
        lambdaFunctionVariable = func.requiredParams;

        func.body = lambdaBody;
        func.desugared = false;
        lambdaFunction.pos = pos;
        List<BType> paramTypes = new ArrayList<>();
        lambdaFunctionVariable.forEach(variable -> paramTypes.add(variable.symbol.type));
        lambdaFunction.type = new BInvokableType(paramTypes, func.symbol.type.getReturnType(),
                null);
        return lambdaFunction;
    }

    private void createStreamAliasMap(BLangStreamingInput streamingInput) {
        //Identify the alias if there anything as such
        if (streamingInput.getAlias() != null) {
            streamAliasMap.put(((BLangVariableReference) streamingInput.getStreamReference()).symbol.toString(),
                    streamingInput.getAlias());
        }
    }

    private BLangSimpleVariable createForeachVariable(DiagnosticPos pos, BSymbol symbol, BType type) {
        final BLangSimpleVariable variable = ASTBuilderUtil.createVariable(pos, VAR_FOREACH_VAL, type);
        variable.symbol = new BVarSymbol(0, names.fromIdNode(variable.name), symbol.pkgID, variable.type, symbol);
        return variable;
    }

    private BLangInvocation createLengthInvocation(DiagnosticPos pos, BVarSymbol collectionSymbol) {
        BInvokableSymbol lengthInvokableSymbol =
                (BInvokableSymbol) symResolver.lookupLangLibMethod(collectionSymbol.type,
                                                                   names.fromString("length"));
        BLangSimpleVarRef collection = ASTBuilderUtil.createVariableRef(pos, collectionSymbol);
        BLangInvocation lengthInvocation = ASTBuilderUtil.createInvocationExprForMethod(pos, lengthInvokableSymbol,
                Lists.of(collection), symResolver);
        lengthInvocation.type = lengthInvokableSymbol.type.getReturnType();
        // Note: No need to set lengthInvocation.expr for langLib functions as they are in requiredArgs
        return lengthInvocation;
    }

    private void defineVariable(BLangSimpleVariable variable, PackageID pkgID, BSymbol owner) {
        variable.symbol = new BVarSymbol(0, names.fromIdNode(variable.name), pkgID, variable.type, owner);
    }
}
