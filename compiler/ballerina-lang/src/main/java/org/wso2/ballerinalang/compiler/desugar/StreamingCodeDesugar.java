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
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.clauses.HavingNode;
import org.ballerinalang.model.tree.clauses.OrderByVariableNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeChecker;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConversionOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.FieldKind;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * Class responsible for desugar an iterable chain into actual Ballerina code.
 *
 * @since 0.980.0
 */
public class StreamingCodeDesugar extends BLangNodeVisitor {

    private static final String FUNC_CALLER = "$lambda$streaming";
    private static final String OUTPUT_FUNC_REFERENCE = "$lambda$streaming$output$function";
    private static final String OUTPUT_FUNC_VAR_ARG = "$lambda$streaming$output$function$var$arg";
    private static final String WINDOW_FUNC_REFERENCE = "$lambda$streaming$window$reference";
    private static final String OUTPUT_PROCESS_FUNC_REFERENCE = "$lambda$streaming$output$process";
    private static final String ORDER_BY_PROCESS_FUNC_REFERENCE = "$lambda$streaming$ordering$process";
    private static final String ORDERING_FUNC_ARRAY_REFERENCE = "$lambda$streaming$orderby$funcarray$variable";
    private static final String ORDERING_FUNC_VAR_ARG = "$lambda$streaming$orderby$func$var$arg";
    private static final String FILTER_FUNC_REFERENCE = "$lambda$streaming$filter";
    private static final String SIMPLE_SELECT_FUNC_REFERENCE = "$lambda$streaming$simple$select";
    private static final String SELECT_WITH_GROUP_BY_FUNC_REFERENCE = "$lambda$streaming$groupby$select";
    private static final String JOIN_PROCESS_FUNC_REFERENCE = "$lambda$streaming$join$process";
    private static final String INPUT_STREAM_PARAM_REFERENCE = "$lambda$streaming$input$variable";
    private static final String FILTER_LAMBDA_PARAM_REFERENCE = "$lambda$streaming$filter$input$variable";
    private static final String SELECT_LAMBDA_PARAM_REFERENCE = "$lambda$streaming$simple$select$input$variable";
    private static final String JOIN_CONDITION_LAMBDA_PARAM_REFERENCE =
            "$lambda$streaming$join$onCondition$input$variable";
    private static final String SELECT_WITH_GROUP_BY_LAMBDA_PARAM_REFERENCE =
            "$lambda$streaming$groupby$select$input$variable";
    private static final String STREAM_EVENT_ARRAY_PARAM_REFERENCE = "$lambda$streaming$stream$event$variable";
    private static final String OUTPUT_EVENT_SELECTOR_PARAM_REFERENCE =
            "$lambda$streaming$output$event$selector$variable";
    private static final String NEXT_PROCESS_METHOD_NAME = "process";
    private static final String STREAM_EVENT_OBJECT_NAME = "StreamEvent";
    private static final String FILTER_OBJECT_NAME = "Filter";
    private static final String WINDOW_OBJECT_NAME = "Window";
    private static final String AGGREGATOR_OBJECT_NAME = "Aggregator";
    private static final String OUTPUT_PROCESS_OBJECT_NAME = "OutputProcess";
    private static final String ORDER_BY_PROCESS_OBJECT_NAME = "OrderBy";
    private static final String CREATE_OUTPUT_PROCESS_METHOD_NAME = "createOutputProcess";
    private static final String CREATE_FILTER_METHOD_NAME = "createFilter";
    private static final String SIMPLE_SELECT_OBJECT_NAME = "SimpleSelect";
    private static final String SELECT_WITH_GROUP_BY_OBJECT_NAME = "Select";
    private static final String JOIN_PROCESS_OBJECT_NAME = "JoinProcesor";
    private static final String CREATE_SIMPLE_SELECT_METHOD_NAME = "createSimpleSelect";
    private static final String CREATE_SELECT_WITH_GROUP_BY_METHOD_NAME = "createSelect";
    private static final String CREATE_ORDER_BY_METHOD_NAME = "createOrderBy";
    private static final String CREATE_STREAM_JOIN_PROCESS_METHOD_NAME = "createStreamJoinProcessor";
    private static final String EVENT_DATA_VARIABLE_NAME = "data";
    private static final String EVENT_TYPE_VARIABLE_NAME = "eventType";
    private static final String BUILD_STREAM_EVENT_METHOD_NAME = "buildStreamEvent";
    private static final String STREAM_SUBSCRIBE_METHOD_NAME = "stream.subscribe";
    private static final String VAR_FOREACH_VAL = "val";
    private static final String VAR_OUTPUT_EVENTS = "outputEvents";

    private static final CompilerContext.Key<StreamingCodeDesugar> STREAMING_DESUGAR_KEY =
            new CompilerContext.Key<>();
    private static final String ORDER_BY_FIELD_ATTR = "fieldFuncs";
    private static final String ORDER_TYPE_ASC = "ASCENDING";
    private static final String STR_ENDING = "ENDING";
    private static final String NEXT_PROCESS_POINTER_ARG_NAME = "nextProcessPointer";
    private static final String ON_CONDITION_NAMED_ARG_NAME = "conditionFunc";

    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final SymbolEnter symbolEnter;
    private TypeChecker typeChecker;
    private BLangDiagnosticLog dlog;
    private final Names names;
    private final Types types;
    private int lambdaFunctionCount = 0;
    private SymbolEnv env;
    private List<BLangStatement> stmts;
    private BLangSimpleVarRef rhsStream, lhsStream;
    private BLangExpression conditionExpr;
    private BType outputEventType;
    private Stack<BVarSymbol> nextProcessVarSymbolStack = new Stack<>();
    private Stack<BVarSymbol> joinProcessorStack = new Stack<>();
    private boolean isInJoin = false;
    private boolean isInHaving = false;
    // Contains the StreamEvent.data variable args in conditional lambda functions like where and join on condition
    private List<BLangVariable> mapVarArgs = new ArrayList<>();
    private Map<String, String> streamAliasMap;


    private StreamingCodeDesugar(CompilerContext context) {
        context.put(STREAMING_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.types = Types.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public static StreamingCodeDesugar getInstance(CompilerContext context) {
        StreamingCodeDesugar desugar = context.get(STREAMING_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new StreamingCodeDesugar(context);
        }

        return desugar;
    }

    public BLangBlockStmt desugar(BLangForever foreverStatement) {

        this.env = foreverStatement.getEnv();
        stmts = new ArrayList<>();
        List<? extends StatementNode> statementNodes = foreverStatement.getStreamingQueryStatements();

        // Generate Streaming Consumer Function
        statementNodes.forEach(statementNode -> ((BLangStatement) statementNode).accept(this));
        return ASTBuilderUtil.createBlockStmt(foreverStatement.pos, stmts);
    }

    @Override
    public void visit(BLangStreamingQueryStatement queryStmt) {

        outputEventType = null;
        conditionExpr = null;
        rhsStream = null;
        lhsStream = null;
        joinProcessorStack.clear();
        streamAliasMap = new HashMap<>();
        //Construct the elements to publish events to output stream
        BLangStreamAction streamAction = (BLangStreamAction) queryStmt.getStreamingAction();
        streamAction.accept(this);

        BLangOrderBy orderBy = (BLangOrderBy) queryStmt.getOrderbyClause();
        if (orderBy != null) {
            orderBy.accept(this);
        }

        BLangSelectClause selectClause = (BLangSelectClause) queryStmt.getSelectClause();
        selectClause.accept(this);

        BLangJoinStreamingInput joinStreamingInput = (BLangJoinStreamingInput) queryStmt.getJoiningInput();
        if (joinStreamingInput != null) {
            isInJoin = true;
            BLangStreamingInput streamingInput = (BLangStreamingInput) joinStreamingInput.getStreamingInput();
            createStreamAliasMap(streamingInput);
            rhsStream = (BLangSimpleVarRef) joinStreamingInput.getStreamingInput().getStreamReference();
            lhsStream = (BLangSimpleVarRef) queryStmt.getStreamingInput().getStreamReference();
            joinStreamingInput.accept(this);
            isInJoin = false;
        }

        //Build elements to consume events from input stream
        BLangStreamingInput streamingInput = (BLangStreamingInput) queryStmt.getStreamingInput();
        createStreamAliasMap(streamingInput);
        streamingInput.accept(this);

    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        //creating function pointer array which represents the ordering fields
        BObjectTypeSymbol orderByObjSymbol = (BObjectTypeSymbol) symResolver.
                resolvePkgSymbol(orderBy.pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(ORDER_BY_PROCESS_OBJECT_NAME)).symbol;
        BType orderingFuncArrayType = getOrderingFuncArrayType((BObjectType) orderByObjSymbol.type);

        //create symbol representing the ordering function pointer array
        BVarSymbol orderingFuncArrayVarSymbol =
                new BVarSymbol(0, new Name(getVariableName(ORDERING_FUNC_ARRAY_REFERENCE)),
                        orderByObjSymbol.pkgID, orderingFuncArrayType, env.scope.owner);

        //create RHS expression for the ordering function array
        BLangArrayLiteral orderingFuncArrExpr = (BLangArrayLiteral) TreeBuilder.createArrayLiteralNode();
        orderingFuncArrExpr.exprs = new ArrayList<>();
        orderingFuncArrExpr.type = orderingFuncArrayType;

        BLangArrayLiteral orderingTypeArrExpr = (BLangArrayLiteral) TreeBuilder.createArrayLiteralNode();
        orderingTypeArrExpr.exprs = new ArrayList<>();
        orderingTypeArrExpr.type = new BArrayType(symTable.stringType);

        BLangVariableDef orderingFuncArrDef =
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
            orderingTypeArrExpr.exprs.add(ASTBuilderUtil.createLiteral(orderByVariable.pos, symTable.stringType,
                    fieldOrderType.toLowerCase()));
        }

        //Create OrderBy process definition
        BLangVariableDef orderByProcessInvokableTypeVariableDef =
                createOrderByProcessDef(orderBy, orderingFuncArrDef.var, orderingTypeArrExpr);
        stmts.add(orderByProcessInvokableTypeVariableDef);
    }

    private BLangVariableDef createOrderingFuncArrayVarDef(BLangOrderBy orderBy, BVarSymbol orderingFuncArrayVarSymbol,
                                                           BLangArrayLiteral orderingFuncArrExpr) {
        BLangVariable orderingFuncArrVariable = ASTBuilderUtil.
                createVariable(orderBy.pos, getVariableName(ORDERING_FUNC_ARRAY_REFERENCE), orderingFuncArrExpr.type,
                        orderingFuncArrExpr, orderingFuncArrayVarSymbol);

        orderingFuncArrVariable.typeNode = ASTBuilderUtil.createTypeNode(orderingFuncArrExpr.type);
        return ASTBuilderUtil.createVariableDef(orderBy.pos, orderingFuncArrVariable);
    }

    private BLangVariableDef createOrderByProcessDef(BLangOrderBy orderBy, BLangVariable orderingFuncArrVariable,
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
        orderByProcessMethodInvocation.argExprs = args;
        BLangVariable orderByProcessInvokableTypeVariable = ASTBuilderUtil.
                createVariable(orderBy.pos, getVariableName(ORDER_BY_PROCESS_FUNC_REFERENCE),
                        orderByProcessInvokableType, orderByProcessMethodInvocation,
                        orderByProcessInvokableTypeVarSymbol);

        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.typeName = ASTBuilderUtil.createIdentifier(orderBy.pos, ORDER_BY_PROCESS_OBJECT_NAME);
        userDefinedType.type = orderByProcessInvokableType;
        orderByProcessInvokableTypeVariable.setTypeNode(userDefinedType);
        return ASTBuilderUtil.createVariableDef(orderBy.pos, orderByProcessInvokableTypeVariable);
    }

    private BType getOrderingFuncArrayType(BObjectType orderByType) {
        BType orderingFuncArrayType;
        List<BField> fields = orderByType.fields;
        //get the ordering functions array type
        //e.g. (function (map)returns any)[] ...
        orderingFuncArrayType = fields.stream().filter(field -> field.name.value.equals(ORDER_BY_FIELD_ATTR))
                .findFirst().map(field -> field.type).orElse(null);
        return orderingFuncArrayType;
    }

    private BLangExpression createOrderingFunctionPointer(BLangExpression expr) {
        BLangVariable orderFuncMapVariable = createMapTypeVariable(getVariableName(ORDERING_FUNC_VAR_ARG), expr.pos,
                env);
        BLangLambdaFunction orderingFunc = createLambdaWithVarArg(expr.pos, new BLangVariable[]
                {orderFuncMapVariable}, TypeKind.ANY);
        BLangBlockStmt lambdaBody = orderingFunc.function.body;

        BLangExpression refactoredExpr = expr;
        mapVarArgs.add(orderFuncMapVariable);
        if (expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) expr;
            refactoredExpr = refactorVarRefAfterSelect(varRef, symTable.anyType);
        } else if (expr.getKind() == NodeKind.INVOCATION) {
            BLangInvocation invocation = (BLangInvocation) expr;
            refactoredExpr = generateConversionExpr(refactorInvocationAfterSelect(invocation), symTable.anyType,
                    symResolver);
        }

        addReturnStmt(expr.pos, lambdaBody, refactoredExpr);
        mapVarArgs.remove(mapVarArgs.size() - 1);
        return orderingFunc;
    }

    private BLangInvocation refactorInvocationAfterSelect(BLangInvocation invocation) {
        if (invocation.requiredArgs.size() > 0) {
            List<BLangExpression> expressionList = new ArrayList<>();
            List<BLangExpression> functionArgsList = invocation.requiredArgs;
            for (BLangExpression expression : functionArgsList) {
                if (expression.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                    BLangSimpleVarRef varRef = (BLangSimpleVarRef) expression;
                    expressionList.add(refactorVarRefAfterSelect(varRef, varRef.symbol.type));
                } else if (expression.getKind() == NodeKind.INVOCATION) {
                    expressionList.add(refactorInvocationAfterSelect((BLangInvocation) expression));
                } else {
                    expressionList.add(expression);
                }
            }
            invocation.argExprs = expressionList;
            invocation.requiredArgs = expressionList;
        }
        return invocation;
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        BLangLambdaFunction lambdaFunction = (BLangLambdaFunction) streamAction.getInvokableBody();
        lambdaFunction.accept(this);
    }

    //
    // This method creates the constructs to publish output events.
    //
    // eg: Below query,
    //
    //        => (TeacherOutput[] outputEvents) {
    //            foreach e in outputEvents {
    //                outputStream.publish(e);
    //            }
    //        }
    //
    // convert into below constructs.
    //
    //       function (map[]) outputFunc = function (map[] events) {
    //          foreach m in events {
    //              TeacherOutput t = check <TeacherOutput>m;
    //              outputStream.publish(t);
    //          }
    //      };
    //
    //      streams:OutputProcess outputProcess = streams:createOutputProcess(outputFunc);
    //
    //
    @Override
    public void visit(BLangLambdaFunction lambdaFunction) {
        //Create lambda function Variable
        BLangVariable outputFuncArg = (BLangVariable) lambdaFunction.getFunctionNode().getParameters().get(0);
        outputEventType = ((BArrayType) outputFuncArg.type).eType;

        //create a wrapper lambda expression to invoke the actual streamAction lambda function
        BLangLambdaFunction outputLambdaFunc = createLambdaWithVarArg(lambdaFunction.pos, new BLangVariable[]{
                ASTBuilderUtil.createVariable(outputFuncArg.pos, getVariableName(OUTPUT_FUNC_VAR_ARG),
                        new BArrayType(symTable.mapType), null, new BVarSymbol(0,
                                names.fromString(getVariableName(OUTPUT_FUNC_VAR_ARG)),
                                lambdaFunction.function.symbol.pkgID, new BArrayType(symTable.mapType),
                                lambdaFunction.function.symbol.owner))}, TypeKind.NIL);
        // create `T[] outputEvents`
        BType outputArrayType = new BArrayType(outputEventType);
        BLangArrayLiteral arrayLiteralExpr = (BLangArrayLiteral) TreeBuilder.createArrayLiteralNode();
        arrayLiteralExpr.exprs = new ArrayList<>();
        arrayLiteralExpr.type = outputArrayType;
        BLangVariable outputArrayVariable = ASTBuilderUtil.createVariable(outputLambdaFunc.function.pos,
                VAR_OUTPUT_EVENTS, outputArrayType, arrayLiteralExpr, null);
        defineVariable(outputArrayVariable, env.scope.owner.pkgID, env.scope.owner);
        BLangSimpleVarRef outputArrayRef = ASTBuilderUtil.createVariableRef(outputLambdaFunc.function.pos,
                outputArrayVariable.symbol);
        BLangVariableDef outputArrayVarDef = ASTBuilderUtil.createVariableDef(outputLambdaFunc.pos,
                outputArrayVariable);
        outputLambdaFunc.function.body.addStatement(outputArrayVarDef);
        // define k, v of `foreach k, v in mArr` expr
        final List<BLangVariable> foreachVariables = createForeachVariables(outputLambdaFunc.function);
        BLangSimpleVarRef indexVarRef = ASTBuilderUtil.createVariableRef(outputLambdaFunc.function.pos,
                foreachVariables.get(0).symbol);
        BLangSimpleVarRef mapVarRef = ASTBuilderUtil.createVariableRef(outputLambdaFunc.function.pos,
                foreachVariables.get(1).symbol);
        // define `map[] events`
        BLangVariable mapArrayVar = ((BLangVariable) outputLambdaFunc.getFunctionNode().getParameters().get(0));

        // foreach k, v in events {
        //     outputEvents[k] = check <T> v;
        // }
        BLangBlockStmt foreachBody = ASTBuilderUtil.createBlockStmt(outputLambdaFunc.function.pos);
        // create `type[i] = checked <type> v;` assignment stmt
        BLangIndexBasedAccess indexAccessExpr = ASTBuilderUtil.createIndexAccessExpr(outputArrayRef, indexVarRef);
        indexAccessExpr.type = outputEventType;
        BLangTypeConversionExpr outputTypeConversionExpr = generateConversionExpr(mapVarRef, outputEventType,
                symResolver);
        BLangCheckedExpr outputCheckedExpr = createCheckedExpr(outputTypeConversionExpr);
        BLangAssignment assignment = ASTBuilderUtil.createAssignmentStmt(outputLambdaFunc.function.pos, foreachBody);
        assignment.setExpression(outputCheckedExpr);
        assignment.varRef = indexAccessExpr;
        BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
        foreach.pos = outputLambdaFunc.function.pos;
        foreach.body = foreachBody;
        foreach.collection = ASTBuilderUtil.createVariableRef(outputLambdaFunc.function.pos, mapArrayVar.symbol);
        foreach.varRefs.addAll(ASTBuilderUtil.createVariableRefList(outputLambdaFunc.function.pos, foreachVariables));
        foreach.varTypes = Lists.of(foreachVariables.get(0).type, foreachVariables.get(1).type);
        outputLambdaFunc.function.body.stmts.add(foreach);

        // pass `T[] outputEvents` created above, to the streaming action
        BLangInvocation streamActionInvocation = ASTBuilderUtil.createInvocationExprForMethod(lambdaFunction.pos,
                lambdaFunction.function.symbol, Collections.singletonList(outputArrayRef), symResolver);
        BLangExpressionStmt streamActionInvocationStmt =
                ASTBuilderUtil.createExpressionStmt(lambdaFunction.function.pos, outputLambdaFunc.function.body);
        streamActionInvocationStmt.expr = streamActionInvocation;

        //Create wrapper lambda expression definition statement
        BLangVariable outputStreamFunctionVariable = ASTBuilderUtil.
                createVariable(outputLambdaFunc.pos, getVariableName(OUTPUT_FUNC_REFERENCE),
                        outputLambdaFunc.function.symbol.type, outputLambdaFunc, outputLambdaFunc.function.symbol);
        outputStreamFunctionVariable.typeNode = ASTBuilderUtil.createTypeNode(outputLambdaFunc.function.symbol.type);
        BLangVariableDef outputStreamFunctionVarDef = ASTBuilderUtil.createVariableDef(outputLambdaFunc.pos,
                outputStreamFunctionVariable);
        stmts.add(outputStreamFunctionVarDef);

        //Create output event process definition
        BLangSimpleVarRef outputStreamFunctionSimpleVarRef = ASTBuilderUtil.createVariableRef(outputLambdaFunc.pos,
                outputStreamFunctionVariable.symbol);
        BInvokableSymbol outputProcessInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(outputLambdaFunc.pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(CREATE_OUTPUT_PROCESS_METHOD_NAME)).symbol;
        BType outputProcessInvokableType = outputProcessInvokableSymbol.type.getReturnType();
        BVarSymbol outputProcessInvokableTypeVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(OUTPUT_PROCESS_FUNC_REFERENCE)), outputProcessInvokableSymbol.pkgID,
                outputProcessInvokableType, env.scope.owner);
        nextProcessVarSymbolStack.push(outputProcessInvokableTypeVarSymbol);

        List<BLangExpression> args = new ArrayList<>();
        args.add(outputStreamFunctionSimpleVarRef);
        BLangInvocation outputProcessMethodInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(outputLambdaFunc.pos, outputProcessInvokableSymbol, args, symResolver);
        outputProcessMethodInvocation.argExprs = args;
        BLangVariable outputProcessInvokableTypeVariable = ASTBuilderUtil.
                createVariable(lambdaFunction.pos, getVariableName(OUTPUT_PROCESS_FUNC_REFERENCE),
                        outputProcessInvokableType, outputProcessMethodInvocation, outputProcessInvokableTypeVarSymbol);

        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.typeName = ASTBuilderUtil.createIdentifier(outputLambdaFunc.pos, OUTPUT_PROCESS_OBJECT_NAME);
        userDefinedType.type = outputProcessInvokableType;
        outputProcessInvokableTypeVariable.setTypeNode(userDefinedType);
        BLangVariableDef outputProcessInvokableTypeVariableDef = ASTBuilderUtil.createVariableDef(outputLambdaFunc.pos,
                outputProcessInvokableTypeVariable);
        stmts.add(outputProcessInvokableTypeVariableDef);
    }


 /*  This method converts the select clause of the streaming query in to Ballerina native constructs.

     eg: Below query,
              select inputStream.name as teacherName, inputStream.age

     convert into below constructs.

              streams:SimpleSelect simpleSelect = streams:createSimpleSelect(outputProcess.process,
                  (streams:StreamEvent e)  => map {
                        return {
                            "name": e.data["inputStream.name"],
                            "age": e.data["inputStream.age"],
                        };
                  });

     And below query,
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

        // Create lambda function Variable
        if (selectClause.getGroupBy() != null) {
            createSelectStatementWithGroupBy(selectClause);
        } else {
            createSimpleSelectStatement(selectClause);
        }
    }

    private void createSelectStatementWithGroupBy(BLangSelectClause selectClause) {

        // 1st arg for createSelect
        BLangFieldBasedAccess nextProcessMethodAccess = createNextProcessFuncPointer(selectClause.pos);

        // [streams:sum(), streams:count(), ... etc], 2nd arg
        BLangArrayLiteral aggregateArray = createAggregatorArray(selectClause);

        // (streams:StreamEvent e) => string, 3rd arg
        BLangLambdaFunction groupingLambda = createGroupByLambda(selectClause);

        // (streams:StreamEvent e, streams:Aggregator[] aggregatorArr)  => any, 4th arg of createSelect
        BLangLambdaFunction aggregatorLambda = createAggregatorLambda(selectClause);


        BInvokableSymbol groupBySelectInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(selectClause.pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(CREATE_SELECT_WITH_GROUP_BY_METHOD_NAME)).symbol;

        BType selectWithGroupByInvokableType = groupBySelectInvokableSymbol.type.getReturnType();
        BVarSymbol selectWithGroupByInvokableTypeVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(SELECT_WITH_GROUP_BY_FUNC_REFERENCE)), groupBySelectInvokableSymbol.pkgID,
                selectWithGroupByInvokableType, env.scope.owner);
        nextProcessVarSymbolStack.push(selectWithGroupByInvokableTypeVarSymbol);

        List<BLangExpression> args = new ArrayList<>();
        args.add(nextProcessMethodAccess);
        args.add(aggregateArray);
        args.add(groupingLambda);
        args.add(aggregatorLambda);

        // streams:createSelect( ... )
        BLangInvocation selectWithGroupByInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(selectClause.pos, groupBySelectInvokableSymbol, args, symResolver);
        selectWithGroupByInvocation.argExprs = args;

        // streams:Select variable name
        BLangVariable selectWithGroupByInvokableTypeVariable = ASTBuilderUtil.
                createVariable(selectClause.pos, getVariableName(SELECT_WITH_GROUP_BY_FUNC_REFERENCE),
                        selectWithGroupByInvokableType, selectWithGroupByInvocation,
                        selectWithGroupByInvokableTypeVarSymbol);

        // streams:Select - user defined data type node
        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.typeName = ASTBuilderUtil.createIdentifier(selectClause.pos, SELECT_WITH_GROUP_BY_OBJECT_NAME);
        userDefinedType.type = selectWithGroupByInvokableType;
        selectWithGroupByInvokableTypeVariable.setTypeNode(userDefinedType);

        // streams:Select select = streams:createSelect(...);
        BLangVariableDef selectWithGroupByInvokableTypeVariableDef = ASTBuilderUtil.createVariableDef(selectClause.pos,
                selectWithGroupByInvokableTypeVariable);
        stmts.add(selectWithGroupByInvokableTypeVariableDef);
    }

    // [streams:sum(), streams:count(), .. etc ]
    private BLangArrayLiteral createAggregatorArray(BLangSelectClause selectClause) {
        BLangArrayLiteral expr = (BLangArrayLiteral) TreeBuilder.createArrayLiteralNode();
        expr.exprs = new ArrayList<>();
        expr.type = new BArrayType(symTable.anyType);

        List<SelectExpressionNode> selectExpressions = selectClause.getSelectExpressions();
        for (SelectExpressionNode select : selectExpressions) {
            ExpressionNode selectExpr = select.getExpression();
            if (selectExpr.getKind() == NodeKind.INVOCATION) {
                BLangInvocation invocation = (BLangInvocation) selectExpr;
                BInvokableSymbol aggregatorInvokableSymbol =
                        getInvokableSymbol(invocation, Names.STREAMS_MODULE);
                if (aggregatorInvokableSymbol != null) {
                    if (isReturnTypeMatching(invocation.pos, AGGREGATOR_OBJECT_NAME, aggregatorInvokableSymbol)) {
                        BLangInvocation aggregatorInvocation = ASTBuilderUtil.
                                createInvocationExprForMethod(invocation.pos, aggregatorInvokableSymbol,
                                Collections.emptyList(), symResolver);
                        expr.exprs.add(aggregatorInvocation);
                    }
                } else {
                    dlog.error(invocation.pos, DiagnosticCode.UNDEFINED_FUNCTION, invocation.name);
                }
            }
        }
        return expr;
    }

    private boolean isReturnTypeMatching(DiagnosticPos pos, String objectName, BInvokableSymbol invokableSymbol) {
        BSymbol expectedRetType = symResolver.
                resolvePkgSymbol(pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(objectName)).symbol;
        BSymbol actualRetType = invokableSymbol.getType().retType.tsymbol;
        return expectedRetType == actualRetType;
    }

    // Object.process
    private BLangFieldBasedAccess createNextProcessFuncPointer(DiagnosticPos pos) {
        BVarSymbol nextProcessInvokableTypeVarSymbol = nextProcessVarSymbolStack.pop();
        BInvokableSymbol nextProcessInvokableSymbol = getNextProcessFunctionSymbol(nextProcessInvokableTypeVarSymbol);

        BLangSimpleVarRef nextProcessSimpleVarRef = ASTBuilderUtil.createVariableRef(pos,
                nextProcessInvokableTypeVarSymbol);
        BLangFieldBasedAccess nextProcessMethodAccess = (BLangFieldBasedAccess)
                TreeBuilder.createFieldBasedAccessNode();
        nextProcessMethodAccess.expr = nextProcessSimpleVarRef;
        nextProcessMethodAccess.symbol = nextProcessInvokableSymbol;
        nextProcessMethodAccess.type = nextProcessInvokableSymbol.type;
        nextProcessMethodAccess.pos = pos;
        nextProcessMethodAccess.field = ASTBuilderUtil.createIdentifier(pos, NEXT_PROCESS_METHOD_NAME);
        return nextProcessMethodAccess;
    }

    private BLangLambdaFunction createAggregatorLambda(BLangSelectClause selectClause) {

        //streams:StreamEvent e,
        BLangVariable varSelectFnStreamEvent = this.createStreamEventArgVariable(
                getVariableName(SELECT_LAMBDA_PARAM_REFERENCE), selectClause.pos, env);

        //streams:Aggregator[] aggregatorArr
        BLangVariable varAggregatorArray =
                this.createAggregatorTypeVariable(getVariableName(SELECT_WITH_GROUP_BY_LAMBDA_PARAM_REFERENCE),
                        selectClause.pos, env);

        /* (streams:StreamEvent e, streams:Aggregator[] aggregatorArr)  => any {

            });
        */
        BLangLambdaFunction selectWithGroupBy = createAggregatorLambdaWithParams(varAggregatorArray,
                varSelectFnStreamEvent, selectClause.pos);

        //lambda function body
        createAggregatorLambdaBody(selectClause, selectWithGroupBy);

        return selectWithGroupBy;
    }

    private void createAggregatorLambdaBody(BLangSelectClause selectClause, BLangLambdaFunction selectWithGroupBy) {

        BLangBlockStmt selectLambdaBody = selectWithGroupBy.function.body;
        BLangVariable varStreamEvent = selectWithGroupBy.function.requiredParams.get(0);
        BLangVariable varAggregatorArray = selectWithGroupBy.function.requiredParams.get(1);

        /* TeacherOutput teacherOutput = {
                        name: e.data["inputStream.name"],
                        age: e.data["inputStream.age"],
                        sumAge: check <int> aggregatorArr1[0].process(e.data["inputStream.age"], e.eventType),
                        count: check <int> aggregatorArr1[1].process((), e.eventType),
                        ...
                        ...
                    };
        */
        BLangVariableDef outputEventObjectVariableDef = addOutputObjectCreationStmt(selectClause, selectLambdaBody,
                varStreamEvent, varAggregatorArray);

        // return teacherOutput;
        addReturnStmt(selectClause.pos, selectLambdaBody,
                ASTBuilderUtil.createVariableRef(selectClause.pos, outputEventObjectVariableDef.var.symbol));
    }

    private void addReturnStmt(DiagnosticPos pos, BLangBlockStmt targetBody, BLangExpression expr) {
        BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pos;
        returnStmt.expr = expr;
        targetBody.stmts.add(returnStmt);
    }

    private BLangVariableDef addOutputObjectCreationStmt(BLangSelectClause selectClause,
                                                         BLangBlockStmt selectLambdaBody, BLangVariable varStreamEvent,
                                                         BLangVariable varAggregatorArray) {

        BLangRecordLiteral outputEventRecordLiteral = ASTBuilderUtil.createEmptyRecordLiteral(selectClause.pos,
                symTable.mapType);
        /* {
             name: e.data["inputStream.name"]
                    age: e.data["inputStream.age"],
                    sumAge: check <int> aggregatorArr1[0].process(e.data["inputStream.age"], e.eventType),
                    count: check <int> aggregatorArr1[1].process((), e.eventType),
                    ...
           }
        */
        List<BLangRecordLiteral.BLangRecordKeyValue> recordKeyValueList =
                getFieldListInSelectClause(selectClause.pos, selectClause.getSelectExpressions(), varStreamEvent.symbol,
                varAggregatorArray.symbol, (BLangGroupBy) selectClause.getGroupBy());

        BVarSymbol outputEventVarSymbol =
                new BVarSymbol(0, new Name(getVariableName(OUTPUT_EVENT_SELECTOR_PARAM_REFERENCE)),
                        varStreamEvent.symbol.pkgID, symTable.mapType, env.scope.owner);

        // TeacherOutput teacherOutput;
        BLangVariable outputEventObjectVariable = ASTBuilderUtil.
                createVariable(selectClause.pos, getVariableName(OUTPUT_EVENT_SELECTOR_PARAM_REFERENCE),
                        symTable.mapType, outputEventRecordLiteral, outputEventVarSymbol);

        outputEventRecordLiteral.keyValuePairs = recordKeyValueList;

        BLangVariableDef outputEventObjectVariableDef = ASTBuilderUtil.createVariableDef(selectClause.pos,
                outputEventObjectVariable);
        selectLambdaBody.stmts.add(outputEventObjectVariableDef);

        return outputEventObjectVariableDef;
    }

    private BLangFieldBasedAccess createEventDataFieldAccessExpr(DiagnosticPos pos,
                                                                 BVarSymbol streamEventSymbol) {
        BLangFieldBasedAccess eventDataField = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
        eventDataField.expr = ASTBuilderUtil.createVariableRef(pos, streamEventSymbol);
        eventDataField.type = symTable.mapType;
        eventDataField.symbol = ((BObjectType) (streamEventSymbol).type).fields.get(2).symbol;
        eventDataField.fieldKind = FieldKind.SINGLE;
        eventDataField.pos = pos;
        eventDataField.field = ASTBuilderUtil.createIdentifier(pos, EVENT_DATA_VARIABLE_NAME);
        return eventDataField;
    }

    private BLangLambdaFunction createAggregatorLambdaWithParams(BLangVariable varAggregatorArray,
                                                                 BLangVariable varSelectFnStreamEvent,
                                                                 DiagnosticPos pos) {
        Set<BVarSymbol> selectLambdaClosureVarSymbols = new LinkedHashSet<>();
        selectLambdaClosureVarSymbols.add(varSelectFnStreamEvent.symbol);
        selectLambdaClosureVarSymbols.add(varAggregatorArray.symbol);

        BLangValueType selectLambdaReturnType = new BLangValueType();
        selectLambdaReturnType.setTypeKind(TypeKind.MAP);

        return createLambdaFunction(pos, new ArrayList<>(Arrays.asList(varSelectFnStreamEvent, varAggregatorArray)),
                selectLambdaClosureVarSymbols, selectLambdaReturnType);
    }

    //TODO: change this to pass an array of lambdas
    private BLangLambdaFunction createGroupByLambda(BLangSelectClause selectClause) {
        BLangVariable varGroupByStreamEvent = this.createStreamEventArgVariable(
                getVariableName(SELECT_LAMBDA_PARAM_REFERENCE), selectClause.pos, env);
        // (streams:StreamEvent e) => string { .. }
        BLangLambdaFunction groupingLambda = createLambdaWithVarArg(selectClause.pos, new BLangVariable[]{
                varGroupByStreamEvent}, TypeKind.STRING);
        BLangBlockStmt groupByLambda = groupingLambda.function.body;

        //e.data;
        BLangIndexBasedAccess mapFieldAccessExpr = createMapAccessExprFromStreamEvent(varGroupByStreamEvent,
                (BLangExpression) selectClause.getGroupBy().getVariables().get(0));
        // return check <string>e.data[<fieldName in string>];
        BLangTypeConversionExpr conversionExpr = generateConversionExpr(mapFieldAccessExpr, symTable.stringType,
                symResolver);
        addReturnGroupByFieldStmt(groupByLambda, conversionExpr);
        return groupingLambda;
    }

    private BLangIndexBasedAccess createMapAccessExprFromStreamEvent(BLangVariable varGroupByStreamEvent,
                                                                     BLangExpression indexExpr) {
        BLangFieldBasedAccess dataField = createStreamEventDataMapExpr(indexExpr.pos, varGroupByStreamEvent);
        return createMapVariableIndexAccessExpr((BVarSymbol) dataField.symbol, indexExpr);
    }

    private void addReturnGroupByFieldStmt(BLangBlockStmt groupByLambda, BLangExpression expr) {
        addReturnStmt(expr.pos, groupByLambda, expr);
    }

    private BLangFieldBasedAccess createStreamEventDataMapExpr(DiagnosticPos pos, BLangVariable varStreamEvent) {
        // eventStream.data
        return createEventDataFieldAccessExpr(pos, varStreamEvent.symbol);
    }

    private BLangLambdaFunction createLambdaWithVarArg(DiagnosticPos pos, BLangVariable[] varArgs, TypeKind typeKind) {
        Set<BVarSymbol> varArgClosureSymbols = Arrays.stream(varArgs).map(varArg -> varArg.symbol)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        BLangValueType returnType = new BLangValueType();
        returnType.setTypeKind(typeKind);

        return createLambdaFunction(pos, Arrays.asList(varArgs),
                varArgClosureSymbols, returnType);
    }

    private void createSimpleSelectStatement(BLangSelectClause selectClause) {
        BLangVariable streamEventVarArg =
                this.createStreamEventArgVariable(getVariableName(SELECT_LAMBDA_PARAM_REFERENCE),
                        selectClause.pos, env);
        BLangLambdaFunction simpleSelectLambdaFunction = createLambdaWithVarArg(selectClause.pos,
                new BLangVariable[]{streamEventVarArg}, TypeKind.MAP);
        BLangBlockStmt lambdaBody = simpleSelectLambdaFunction.function.body;

        //Output object creation
        BLangRecordLiteral outputEventRecordLiteral = ASTBuilderUtil.createEmptyRecordLiteral(selectClause.pos,
                symTable.mapType);
        List<BLangRecordLiteral.BLangRecordKeyValue> recordKeyValueList = getFieldListInSelectClause(selectClause.pos,
                selectClause.getSelectExpressions(), streamEventVarArg.symbol, null, null);

        BVarSymbol outputEventVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(OUTPUT_EVENT_SELECTOR_PARAM_REFERENCE)), streamEventVarArg.symbol.pkgID,
                outputEventType, env.scope.owner);
        outputEventRecordLiteral.keyValuePairs = recordKeyValueList;
        BLangVariable outputEventObjectVariable = ASTBuilderUtil.
                createVariable(selectClause.pos, getVariableName(OUTPUT_EVENT_SELECTOR_PARAM_REFERENCE),
                        symTable.mapType, outputEventRecordLiteral, outputEventVarSymbol);

        outputEventObjectVariable.typeNode = ASTBuilderUtil.createTypeNode(symTable.mapType);
        BLangVariableDef outputEventObjectVariableDef = ASTBuilderUtil.createVariableDef(selectClause.pos,
                outputEventObjectVariable);
        lambdaBody.stmts.add(outputEventObjectVariableDef);

        // Return statement with newly created output event
        addReturnStmt(selectClause.pos, lambdaBody, ASTBuilderUtil.createVariableRef
                (selectClause.pos, outputEventObjectVariableDef.var.symbol));
        //Create event simple selector definition
        BLangFieldBasedAccess nextProcessMethodAccess = createNextProcessFuncPointer(selectClause.pos);

        BInvokableSymbol simpleSelectInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(selectClause.pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(CREATE_SIMPLE_SELECT_METHOD_NAME)).symbol;

        BType simpleSelectInvokableType = simpleSelectInvokableSymbol.type.getReturnType();
        BVarSymbol simpleSelectInvokableTypeVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(SIMPLE_SELECT_FUNC_REFERENCE)), simpleSelectInvokableSymbol.pkgID,
                simpleSelectInvokableType, env.scope.owner);
        nextProcessVarSymbolStack.push(simpleSelectInvokableTypeVarSymbol);

        List<BLangExpression> args = new ArrayList<>();
        args.add(nextProcessMethodAccess);
        args.add(simpleSelectLambdaFunction);

        BLangInvocation simpleSelectMethodInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(selectClause.pos, simpleSelectInvokableSymbol, args,
                        symResolver);
        simpleSelectMethodInvocation.argExprs = args;
        BLangVariable simpleSelectInvokableTypeVariable = ASTBuilderUtil.
                createVariable(selectClause.pos, getVariableName(SIMPLE_SELECT_FUNC_REFERENCE),
                        simpleSelectInvokableType, simpleSelectMethodInvocation, simpleSelectInvokableTypeVarSymbol);

        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.typeName = ASTBuilderUtil.createIdentifier(selectClause.pos, SIMPLE_SELECT_OBJECT_NAME);
        userDefinedType.type = simpleSelectInvokableType;
        simpleSelectInvokableTypeVariable.setTypeNode(userDefinedType);
        BLangVariableDef simpleSelectInvokableTypeVariableDef = ASTBuilderUtil.createVariableDef(selectClause.pos,
                simpleSelectInvokableTypeVariable);
        stmts.add(simpleSelectInvokableTypeVariableDef);
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        BLangBinaryExpr onExpr = (BLangBinaryExpr) joinStreamingInput.getOnExpression();
        if (onExpr != null) {
            BLangVariable lhsDataMap = createMapTypeVariable(getVariableName(JOIN_CONDITION_LAMBDA_PARAM_REFERENCE),
                    onExpr.lhsExpr.pos, env);
            BLangVariable rhsDataMap = createMapTypeVariable(getVariableName(JOIN_CONDITION_LAMBDA_PARAM_REFERENCE) +
                    1, onExpr.rhsExpr.pos, env);
            BLangLambdaFunction conditionFunc = createLambdaWithVarArg(joinStreamingInput.pos, new BLangVariable[]
                    {lhsDataMap, rhsDataMap}, TypeKind.BOOLEAN);
            mapVarArgs.addAll(Arrays.asList(lhsDataMap, rhsDataMap));
            onExpr.accept(this);
            BLangBlockStmt funcBody = conditionFunc.function.body;
            addReturnStmt(onExpr.pos, funcBody, conditionExpr);
            createJoinProcessorStmt(joinStreamingInput, conditionFunc);
        } else {
            createJoinProcessorStmt(joinStreamingInput, null);
        }

        BLangStreamingInput streamingInput = (BLangStreamingInput) joinStreamingInput.getStreamingInput();
        streamingInput.accept(this);
    }

    private void createJoinProcessorStmt(BLangJoinStreamingInput joinStreamingInput,
                                         BLangLambdaFunction conditionExpr) {

        BLangFieldBasedAccess nextProcessMethodAccess = createNextProcessFuncPointer(joinStreamingInput.pos);

        BInvokableSymbol joinProcessorInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(joinStreamingInput.pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(CREATE_STREAM_JOIN_PROCESS_METHOD_NAME)).symbol;

        BType joinProcessorReturnType = joinProcessorInvokableSymbol.type.getReturnType();

        BVarSymbol joinProcessorVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(JOIN_PROCESS_FUNC_REFERENCE)), joinProcessorInvokableSymbol.pkgID,
                joinProcessorReturnType, env.scope.owner);

        joinProcessorStack.push(joinProcessorVarSymbol);

        List<BLangExpression> args = new ArrayList<>();
        args.add(nextProcessMethodAccess);
        args.add(ASTBuilderUtil.createLiteral(joinStreamingInput.pos, symTable.stringType, joinStreamingInput
                .getJoinType().toUpperCase()));

        // streams:createJoinProcess( ... )
        BLangInvocation createJoinInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(joinStreamingInput.pos, joinProcessorInvokableSymbol, args, symResolver);
        createJoinInvocation.argExprs = args;

        if (conditionExpr != null) {
            BLangNamedArgsExpression conditionNamedArgExpression = ASTBuilderUtil.
                    createNamedArg(ON_CONDITION_NAMED_ARG_NAME, conditionExpr);
            createJoinInvocation.namedArgs.add(conditionNamedArgExpression);
        }

        // streams:JoinProcess variable name
        BLangVariable joinInvokableTypeVariable = ASTBuilderUtil.
                createVariable(joinStreamingInput.pos, getVariableName(JOIN_PROCESS_FUNC_REFERENCE),
                        joinProcessorReturnType, createJoinInvocation,
                        joinProcessorVarSymbol);

        // streams:Select - user defined data type node
        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.typeName = ASTBuilderUtil.createIdentifier(joinStreamingInput.pos, JOIN_PROCESS_OBJECT_NAME);
        userDefinedType.type = joinProcessorReturnType;
        joinInvokableTypeVariable.setTypeNode(userDefinedType);

        // streams:Select select = streams:createSelect(...);
        BLangVariableDef joinProcessorInvokableTypeDef =
                ASTBuilderUtil.createVariableDef(joinStreamingInput.pos, joinInvokableTypeVariable);
        stmts.add(joinProcessorInvokableTypeDef);
    }

    //
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
    //
    @Override
    public void visit(BLangStreamingInput streamingInput) {
        //Lambda function parameter
        BType lambdaParameterType = ((BStreamType) ((BLangExpression) streamingInput.getStreamReference()).type)
                .constraint;

        BLangWhere afterWhereNode = (BLangWhere) streamingInput.getAfterStreamingCondition();
        if (afterWhereNode != null) {
            afterWhereNode.accept(this);
        }

        BLangWindow windowClauseNode = (BLangWindow) streamingInput.getWindowClause();
        if (windowClauseNode != null) {
            windowClauseNode.accept(this);
        }

        BLangWhere beforeWhereNode = (BLangWhere) streamingInput.getBeforeStreamingCondition();
        if (beforeWhereNode != null) {
            beforeWhereNode.accept(this);
        }

        if (!nextProcessVarSymbolStack.empty()) {
            BVarSymbol nextProcessInvokableTypeVarSymbol = nextProcessVarSymbolStack.pop();
            BVarSymbol lambdaParameterVarSymbol = new BVarSymbol(0,
                    new Name(getVariableName(INPUT_STREAM_PARAM_REFERENCE)), lambdaParameterType.tsymbol.pkgID,
                    lambdaParameterType, env.scope.owner);

            BLangVariable inputStreamLambdaFunctionVariable = ASTBuilderUtil.createVariable(streamingInput.pos,
                    getVariableName(INPUT_STREAM_PARAM_REFERENCE), lambdaParameterType, null, lambdaParameterVarSymbol);
            inputStreamLambdaFunctionVariable.typeNode = ASTBuilderUtil.createTypeNode(lambdaParameterType);

            Set<BVarSymbol> closureVarSymbols = new LinkedHashSet<>();
            closureVarSymbols.add(nextProcessInvokableTypeVarSymbol);
            closureVarSymbols.add(inputStreamLambdaFunctionVariable.symbol);

            BLangValueType returnType = new BLangValueType();
            returnType.setTypeKind(TypeKind.NIL);

            //Construct lambda function which consumes events
            BLangLambdaFunction streamSubscriberLambdaFunction = createLambdaFunction(streamingInput.pos,
                    new ArrayList<>(Collections.singletonList(inputStreamLambdaFunctionVariable)), closureVarSymbols,
                    returnType);
            BLangBlockStmt lambdaBody = streamSubscriberLambdaFunction.function.body;

            //Event conversion to StreamEvent
            BLangExpression eventToMapConversionExpr =
                    generateConversionExpr(ASTBuilderUtil.createVariableRef(streamingInput.pos,
                            inputStreamLambdaFunctionVariable.symbol), symTable.mapType, symResolver);
            BInvokableSymbol streamEventBuilderInvokableSymbol = (BInvokableSymbol) symResolver.
                    resolvePkgSymbol(streamingInput.pos, env, Names.STREAMS_MODULE).
                    scope.lookup(new Name(BUILD_STREAM_EVENT_METHOD_NAME)).symbol;

            BType streamEventArrayType = streamEventBuilderInvokableSymbol.type.getReturnType();
            BVarSymbol streamEventArrayTypeVarSymbol = new BVarSymbol(0,
                    new Name(getVariableName(STREAM_EVENT_ARRAY_PARAM_REFERENCE)),
                    streamEventBuilderInvokableSymbol.pkgID, streamEventArrayType, env.scope.owner);

            List<BLangExpression> args = new ArrayList<>();
            args.add(eventToMapConversionExpr);

            String streamReferenceSymbolName = ((BLangSimpleVarRef) streamingInput.getStreamReference()).symbol
                    .toString();
            if (streamAliasMap.containsKey(streamReferenceSymbolName)) {
                args.add(ASTBuilderUtil.createLiteral(streamingInput.pos, symTable.stringType,
                        streamAliasMap.get(streamReferenceSymbolName)));
            } else {
                args.add(ASTBuilderUtil.createLiteral(streamingInput.pos, symTable.stringType, ((BLangSimpleVarRef)
                        streamingInput.getStreamReference()).symbol.toString()));
            }

            BLangInvocation streamEventBuilderMethodInvocation = ASTBuilderUtil.
                    createInvocationExprForMethod(streamingInput.pos, streamEventBuilderInvokableSymbol, args,
                            symResolver);
            streamEventBuilderMethodInvocation.argExprs = args;

            BLangVariable streamEventArrayTypeVariable = ASTBuilderUtil.
                    createVariable(streamingInput.pos, getVariableName(STREAM_EVENT_ARRAY_PARAM_REFERENCE),
                            streamEventArrayType, streamEventBuilderMethodInvocation, streamEventArrayTypeVarSymbol);

            BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
            userDefinedType.typeName = ASTBuilderUtil.createIdentifier(streamingInput.pos, STREAM_EVENT_OBJECT_NAME);
            userDefinedType.type = streamEventArrayType;
            streamEventArrayTypeVariable.setTypeNode(userDefinedType);
            BLangVariableDef streamEventArrayTypeVariableDef = ASTBuilderUtil.createVariableDef(streamingInput.pos,
                    streamEventArrayTypeVariable);

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
            nextProcessMethodInvocation.argExprs = nextProcessVariables;


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
            BInvokableSymbol subscribeMethodSymbol = (BInvokableSymbol) symTable.rootScope.
                    lookup(names.fromString(STREAM_SUBSCRIBE_METHOD_NAME)).symbol;
            List<BLangExpression> variables = new ArrayList<>(1);
            variables.add(streamSubscriberLambdaFunction);
            BLangInvocation invocationExpr = ASTBuilderUtil.
                    createInvocationExprForMethod(streamingInput.pos, subscribeMethodSymbol, variables, symResolver);

            invocationExpr.argExprs = variables;
            invocationExpr.expr = ASTBuilderUtil.createVariableRef(streamingInput.pos, (BVarSymbol)
                    ((BLangSimpleVarRef) streamingInput.getStreamReference()).symbol);
            inputStreamSubscribeStatement.expr = invocationExpr;

            //Add stream subscriber function to stmts
            stmts.add(inputStreamSubscribeStatement);
        }
    }

    /*
        e.g: window lengthWindow(5) will be turned into,

        streams:LengthWindow lengthWindow = streams:lengthWindow(select.process, 5);
     */
    @Override
    public void visit(BLangWindow window) {

        BLangInvocation invocation = (BLangInvocation) window.getFunctionInvocation();

        convertFieldAccessArgsToStringLiteral(invocation);

        //checks for the symbol, if not exists, then set the pkgAlias to STREAMS_STDLIB_PACKAGE_NAME
        BInvokableSymbol windowInvokableSymbol = getInvokableSymbol(invocation, Names.STREAMS_MODULE);

        if (windowInvokableSymbol != null) {
            if (isReturnTypeMatching(invocation.pos, WINDOW_OBJECT_NAME, windowInvokableSymbol)) {
                //Create event filter definition
                BVarSymbol nextProcessInvokableTypeVarSymbol;
                if (rhsStream != null) {
                    nextProcessInvokableTypeVarSymbol = joinProcessorStack.peek();
                } else {
                    nextProcessInvokableTypeVarSymbol = nextProcessVarSymbolStack.pop();
                }
                BInvokableSymbol nextProcessInvokableSymbol =
                        getNextProcessFunctionSymbol(nextProcessInvokableTypeVarSymbol);

                BLangSimpleVarRef nextProcessSimpleVarRef = ASTBuilderUtil.createVariableRef(window.pos,
                        nextProcessInvokableTypeVarSymbol);
                BLangFieldBasedAccess nextProcessMethodAccess = createFieldBasedAccessForProcessFunc(window.pos,
                        nextProcessInvokableSymbol, nextProcessSimpleVarRef);


                BType windowInvokableType = windowInvokableSymbol.type.getReturnType();

                BVarSymbol windowInvokableTypeVarSymbol = new BVarSymbol(0,
                        new Name(getVariableName(WINDOW_FUNC_REFERENCE)), windowInvokableSymbol.pkgID,
                        windowInvokableType, env.scope.owner);
                nextProcessVarSymbolStack.push(windowInvokableTypeVarSymbol);

                BLangNamedArgsExpression nextProcPointer = ASTBuilderUtil.createNamedArg(NEXT_PROCESS_POINTER_ARG_NAME,
                        nextProcessMethodAccess);

                typeChecker.checkExpr(invocation, env);

                //these should be added after type-checking
                invocation.argExprs.add(nextProcPointer);
                invocation.namedArgs.add(nextProcPointer);

                BLangVariableDef windowDef = createVariableDef(invocation, windowInvokableType,
                        windowInvokableTypeVarSymbol, window.pos, WINDOW_FUNC_REFERENCE, WINDOW_OBJECT_NAME);
                stmts.add(windowDef);

                if (!joinProcessorStack.empty()) {
                    if (isInJoin) {
                        attachWindowToJoinProcessor(window, windowInvokableTypeVarSymbol, "setRHS", rhsStream);
                    } else {
                        attachWindowToJoinProcessor(window, windowInvokableTypeVarSymbol, "setLHS", lhsStream);
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

    private BInvokableSymbol getInvokableSymbol(BLangInvocation invocation, Name pkgName) {
        BInvokableSymbol windowInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(invocation.pos, env, names.fromString(invocation.pkgAlias.value)).
                scope.lookup(new Name(invocation.name.value)).symbol;
        if (windowInvokableSymbol == null && invocation.pkgAlias.value.isEmpty()) {
            windowInvokableSymbol = (BInvokableSymbol) symResolver.
                    resolvePkgSymbol(invocation.pos, env, pkgName).
                    scope.lookup(new Name(invocation.name.value)).symbol;
            invocation.pkgAlias.value = Names.STREAMS_MODULE.value;
        }
        return windowInvokableSymbol;
    }

    private BLangVariableDef createVariableDef(BLangExpression expr, BType exprType,
                                               BVarSymbol exprTypeSymbol,
                                               DiagnosticPos pos, String exprVarName, String objName) {

        BLangVariable windowInvokableTypeVariable =
                ASTBuilderUtil.createVariable(pos, getVariableName(exprVarName), exprType, expr,
                        exprTypeSymbol);

        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.typeName = ASTBuilderUtil.createIdentifier(pos, objName);
        userDefinedType.type = exprType;
        windowInvokableTypeVariable.setTypeNode(userDefinedType);
        return ASTBuilderUtil.createVariableDef(pos, windowInvokableTypeVariable);
    }

    private void convertFieldAccessArgsToStringLiteral(BLangInvocation invocation) {
        //converting BLangFieldBaseAccess to BLangLiteral of string type, in argExprs
        BLangLiteral streamEventParameter;
        for (int i = 0; i < invocation.argExprs.size(); i++) {
            BLangExpression exp = invocation.argExprs.get(i);
            if (exp.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {

                String variableName = ((BLangFieldBasedAccess) exp).expr.toString();
                if (streamAliasMap.containsKey(variableName)) {
                    variableName = streamAliasMap.get(variableName);
                    ((BLangSimpleVarRef) ((BLangFieldBasedAccess) exp).expr).variableName.value = variableName;
                }
                streamEventParameter = createStringLiteral(exp.pos, (exp).toString());
                invocation.argExprs.set(i, streamEventParameter);
            }
        }

        //converting BLangFieldBaseAccess to BLangLiteral of string type, in requiredArgs
        for (int i = 0; i < invocation.requiredArgs.size(); i++) {
            BLangExpression exp = invocation.requiredArgs.get(i);
            if (exp.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
                String variableName = ((BLangFieldBasedAccess) exp).expr.toString();
                if (streamAliasMap.containsKey(variableName)) {
                    variableName = streamAliasMap.get(variableName);
                    ((BLangSimpleVarRef) ((BLangFieldBasedAccess) exp).expr).variableName.value = variableName;
                }
                streamEventParameter = createStringLiteral(exp.pos, (exp).toString());
                invocation.requiredArgs.set(i, streamEventParameter);
            }
        }
    }

    private BLangLiteral createStringLiteral(DiagnosticPos pos, String value) {
        BLangLiteral stringLit = new BLangLiteral();
        stringLit.pos = pos;
        stringLit.typeTag = TypeTags.STRING;
        stringLit.value = value;
        stringLit.type = symTable.stringType;
        return stringLit;
    }

    private void attachWindowToJoinProcessor(BLangWindow window, BVarSymbol windowInvokableTypeVarSymbol,
                                             String methodName, BLangSimpleVarRef streamRef) {
        BVarSymbol joinProcessVarSymbol = joinProcessorStack.peek();
        BInvokableSymbol methodInvokableSymbol = getInvokableSymbolOfObject(joinProcessVarSymbol, methodName);
        List<BLangExpression> args = new ArrayList<>();
        args.add(ASTBuilderUtil.createVariableRef(window.pos, joinProcessVarSymbol));

        String streamReferenceSymbolName = streamRef.symbol.toString();
        args.add(ASTBuilderUtil.createLiteral(window.pos, symTable.stringType,
                streamAliasMap.getOrDefault(streamReferenceSymbolName, streamReferenceSymbolName)));
        args.add(ASTBuilderUtil.createVariableRef(window.pos, windowInvokableTypeVarSymbol));
        BLangInvocation methodInvocation = ASTBuilderUtil.createInvocationExprForMethod(window.pos,
                methodInvokableSymbol, args, symResolver);
        methodInvocation.argExprs = args;
        BLangExpressionStmt methodInvocationStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        methodInvocationStmt.pos = window.pos;
        methodInvocationStmt.expr = methodInvocation;
        stmts.add(methodInvocationStmt);
    }

    private BLangFieldBasedAccess createFieldBasedAccessForProcessFunc(DiagnosticPos pos,
                                                                       BInvokableSymbol nextProcessInvokableSymbol,
                                                                       BLangSimpleVarRef nextProcessSimpleVarRef) {
        BLangFieldBasedAccess nextProcessMethodAccess = (BLangFieldBasedAccess)
                TreeBuilder.createFieldBasedAccessNode();
        nextProcessMethodAccess.expr = nextProcessSimpleVarRef;
        nextProcessMethodAccess.symbol = nextProcessInvokableSymbol;
        nextProcessMethodAccess.type = nextProcessInvokableSymbol.type;
        nextProcessMethodAccess.pos = pos;
        nextProcessMethodAccess.field = ASTBuilderUtil.createIdentifier(pos, NEXT_PROCESS_METHOD_NAME);
        return nextProcessMethodAccess;
    }

    //
    // Below method creates the constructs to perform filtering based on the 'where' clause of the streaming query.
    //
    // eg: Below query,
    //          where inputStream.age > 25
    //
    // converts into below constructs.
    //
    //          streams:Filter filter = streams:createFilter(select.process, (any o) => boolean {
    //              Teacher teacher = check <Teacher> o;
    //              return teacher.age > 25;
    //          });
    //
    //
    @Override
    public void visit(BLangWhere where) {
        visitFilter(where.pos, (BLangBinaryExpr) where.getExpression());
    }

    //
    // Below method creates the constructs to perform filtering based on the 'having' clause of the streaming query.
    //
    // eg: Below query,
    //          having age > 25
    //
    // converts into below constructs.
    //
    //          streams:Filter filter = streams:createFilter(outputProcess.process, (any o) => boolean {
    //              Teacher teacher = check <Teacher> o;
    //              return teacher.age > 25;
    //          });
    //
    //
    @Override
    public void visit(BLangHaving having) {
        isInHaving = true;
        visitFilter(having.pos, (BLangBinaryExpr) having.getExpression());
        isInHaving = false;
    }

    //------------------------------------- Methods required for filter / having -----------------------------------
    private void visitFilter(DiagnosticPos pos, BLangBinaryExpr expression) {
        //Create lambda function Variable
        BLangVariable lambdaFunctionVariable =
                this.createMapTypeVariable(getVariableName(FILTER_LAMBDA_PARAM_REFERENCE), pos, env);

        Set<BVarSymbol> closureVarSymbols = new LinkedHashSet<>();
        closureVarSymbols.add(lambdaFunctionVariable.symbol);

        BLangValueType returnType = new BLangValueType();
        returnType.setTypeKind(TypeKind.BOOLEAN);

        //Create new lambda function to process the output events
        BLangLambdaFunction havingLambdaFunction = createLambdaFunction(pos,
                new ArrayList<>(Collections.singletonList(lambdaFunctionVariable)),
                closureVarSymbols, returnType);
        BLangBlockStmt lambdaBody = havingLambdaFunction.function.body;

        // Return statement with having condition
        BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pos;
        mapVarArgs.add(lambdaFunctionVariable);
        returnStmt.expr = getBinaryExprWithMapAccessFields(pos, expression);
        lambdaBody.stmts.add(returnStmt);

        //Create having (filter) definition
        BVarSymbol nextProcessInvokableTypeVarSymbol = nextProcessVarSymbolStack.pop();
        BInvokableSymbol nextProcessInvokableSymbol = getNextProcessFunctionSymbol(nextProcessInvokableTypeVarSymbol);

        BLangSimpleVarRef nextProcessSimpleVarRef = ASTBuilderUtil.createVariableRef(pos,
                nextProcessInvokableTypeVarSymbol);
        BLangFieldBasedAccess nextProcessMethodAccess = createFieldBasedAccessForProcessFunc(pos,
                nextProcessInvokableSymbol, nextProcessSimpleVarRef);

        // Having will also use the same filter invokable
        BInvokableSymbol havingInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(CREATE_FILTER_METHOD_NAME)).symbol;
        BType havingInvokableType = havingInvokableSymbol.type.getReturnType();
        BVarSymbol havingInvokableTypeVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(FILTER_FUNC_REFERENCE)), havingInvokableSymbol.pkgID,
                havingInvokableType, env.scope.owner);
        nextProcessVarSymbolStack.push(havingInvokableTypeVarSymbol);

        List<BLangExpression> args = new ArrayList<>();
        args.add(nextProcessMethodAccess);
        args.add(havingLambdaFunction);

        BLangInvocation havingMethodInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, havingInvokableSymbol, args, symResolver);
        havingMethodInvocation.argExprs = args;

        BLangVariableDef filterDef = createVariableDef(havingMethodInvocation, havingInvokableType,
                havingInvokableTypeVarSymbol, pos, FILTER_FUNC_REFERENCE, FILTER_OBJECT_NAME);
        stmts.add(filterDef);
        mapVarArgs.remove(mapVarArgs.size() - 1);
    }

    private BLangBinaryExpr getBinaryExprWithMapAccessFields(DiagnosticPos pos, BLangBinaryExpr expression) {
        final BLangBinaryExpr binaryExpr = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpr.pos = pos;
        binaryExpr.type = symTable.booleanType;
        binaryExpr.opKind = expression.getOperatorKind();
        expression.getLeftExpression().accept(this);
        binaryExpr.lhsExpr = conditionExpr;
        expression.getRightExpression().accept(this);
        binaryExpr.rhsExpr = conditionExpr;
        binaryExpr.opSymbol = expression.opSymbol;
        return binaryExpr;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        conditionExpr = getBinaryExprWithMapAccessFields(binaryExpr.pos, binaryExpr);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        if (fieldAccessExpr.expr.type.tag == TypeTags.STREAM && !isInHaving) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) fieldAccessExpr.expr;
            BLangSimpleVarRef mapRef;
            int mapVarArgIndex;

            //mapVarArgs can contain at most 2 map arguments required for conditional expr in join clause
            if (rhsStream != null && ((varRef.variableName.value.equals((rhsStream).symbol.toString()))
                    || (varRef.variableName.value.equals(streamAliasMap.get((rhsStream).symbol.toString()))))) {
                mapVarArgIndex = 1;
            } else {
                mapVarArgIndex = 0;
            }
            mapRef = ASTBuilderUtil.createVariableRef(fieldAccessExpr.pos, mapVarArgs.get(mapVarArgIndex).symbol);

            String variableName = ((BLangSimpleVarRef) (fieldAccessExpr).expr).variableName.value;
            if (streamAliasMap.containsKey(variableName)) {
                ((BLangSimpleVarRef) (fieldAccessExpr).expr).variableName.value = streamAliasMap.get(variableName);
            }
            String mapKey = fieldAccessExpr.toString();
            BLangExpression indexExpr = ASTBuilderUtil.createLiteral(fieldAccessExpr.field.pos, symTable.stringType,
                    mapKey);
            BLangIndexBasedAccess mapAccessExpr = ASTBuilderUtil.createIndexAccessExpr(mapRef, indexExpr);
            mapAccessExpr.pos = fieldAccessExpr.pos;
            mapAccessExpr.type = symTable.anyType;
            conditionExpr = Desugar.addConversionExprIfRequired(mapAccessExpr, fieldAccessExpr.type, types, symTable,
                    symResolver);
        } else {
            conditionExpr = fieldAccessExpr;
        }
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        conditionExpr = literalExpr;
    }

    public void visit(BLangInvocation invocationExpr) {
        conditionExpr = refactorInvocationWithIndexBasedArgs(mapVarArgs.get(mapVarArgs.size() - 1).symbol,
                invocationExpr);
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        if (isInHaving) {
            conditionExpr = refactorVarRefAfterSelect(varRefExpr, varRefExpr.symbol.type);
        } else {
            conditionExpr = varRefExpr;
        }
    }

    private BLangExpression refactorVarRefAfterSelect(BLangSimpleVarRef varRefExpr, BType expType) {
        BLangExpression refactoredVarRef;
        Map<String, BField> fields = ((BRecordType) outputEventType).fields.stream()
                .collect(Collectors.toMap(field -> field.name.getValue(), field -> field, (a, b) -> b));
        String key = varRefExpr.variableName.value;
        if (fields.containsKey(key)) {
            refactoredVarRef = createMapVariableIndexAccessExpr(mapVarArgs.get(mapVarArgs.size() - 1).symbol,
                    ASTBuilderUtil.createLiteral(varRefExpr.pos, symTable.stringType, "OUTPUT." + varRefExpr
                            .variableName.value));
            refactoredVarRef = generateConversionExpr(refactoredVarRef, expType, symResolver);
            if (refactoredVarRef.getKind() == NodeKind.TYPE_CONVERSION_EXPR) {
                refactoredVarRef = createCheckedExpr((BLangTypeConversionExpr) refactoredVarRef);
            }
        } else {
            refactoredVarRef = varRefExpr;
        }
        return refactoredVarRef;
    }

    //----------------------------------------- Util Methods ---------------------------------------------------------

    private String getFunctionName(String name) {
        return name + lambdaFunctionCount++;
    }

    private String getVariableName(String name) {
        return name + lambdaFunctionCount;
    }

    private void defineFunction(BLangFunction funcNode, BLangPackage targetPkg) {
        final BPackageSymbol packageSymbol = targetPkg.symbol;
        final SymbolEnv packageEnv = this.symTable.pkgEnvMap.get(packageSymbol);
        symbolEnter.defineNode(funcNode, packageEnv);
        packageEnv.enclPkg.functions.add(funcNode);
        packageEnv.enclPkg.topLevelNodes.add(funcNode);
    }

    private BLangVariable createMapTypeVariable(String variableName, DiagnosticPos pos, SymbolEnv env) {
        BType varType = this.symTable.mapType;
        BVarSymbol varSymbol = new BVarSymbol(0, new Name(variableName),
                varType.tsymbol.pkgID, varType, env.scope.owner);

        BLangVariable mapTypeVariable = ASTBuilderUtil.createVariable(pos, variableName,
                varType, null, varSymbol);
        mapTypeVariable.typeNode = ASTBuilderUtil.createTypeNode(varType);
        return mapTypeVariable;
    }

    private BLangVariable createStreamEventArgVariable(String variableName, DiagnosticPos pos, SymbolEnv env) {
        BObjectTypeSymbol recordTypeSymbol = (BObjectTypeSymbol) symResolver.
                resolvePkgSymbol(pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(STREAM_EVENT_OBJECT_NAME)).symbol;

        BType varType = recordTypeSymbol.type;
        BVarSymbol varSymbol = new BVarSymbol(0, new Name(variableName),
                varType.tsymbol.pkgID, varType, env.scope.owner);

        return ASTBuilderUtil.createVariable(pos, variableName, varType, null, varSymbol);
    }

    private BLangVariable createAggregatorTypeVariable(String variableName, DiagnosticPos pos, SymbolEnv env) {
        BInvokableSymbol invokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(pos, env, Names.STREAMS_MODULE).
                scope.lookup(new Name(CREATE_SELECT_WITH_GROUP_BY_METHOD_NAME)).symbol;

        BType varType = ((BInvokableType) invokableSymbol.params.get(3).type).paramTypes.get(1);
        BVarSymbol varSymbol = new BVarSymbol(0, new Name(variableName),
                varType.tsymbol.pkgID, varType, env.scope.owner);
        return ASTBuilderUtil.createVariable(pos, variableName, varType, null, varSymbol);
    }

    private static BLangTypeConversionExpr generateConversionExpr(BLangExpression expr, BType target,
                                                                  SymbolResolver symResolver) {
        // Box value using cast expression.
        final BLangTypeConversionExpr conversion = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        conversion.pos = expr.pos;
        conversion.expr = expr;

        conversion.targetType = target;
        conversion.conversionSymbol = (BConversionOperatorSymbol) symResolver.resolveConversionOperator(expr.type,
                target);
        conversion.type = ((BInvokableType) conversion.conversionSymbol.type).retType;
        return conversion;
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

    private BInvokableSymbol getInvokableSymbolOfObject(BSymbol nextProcessInvokableTypeVarSymbol, String funcName) {
        List<BAttachedFunction> attachedFunctionsList = ((BObjectTypeSymbol)
                (nextProcessInvokableTypeVarSymbol).type.tsymbol).attachedFuncs;
        for (BAttachedFunction attachedFunction : attachedFunctionsList) {
            if (attachedFunction.funcName.toString().equals(funcName)) {
                return attachedFunction.symbol;
            }
        }
        throw new IllegalStateException("Couldn't evaluate the " + funcName + "method of the next processor : " +
                (nextProcessInvokableTypeVarSymbol).type.toString());
    }

    private BInvokableSymbol getNextProcessFunctionSymbol(BSymbol nextProcessInvokableTypeVarSymbol) {
        return getInvokableSymbolOfObject(nextProcessInvokableTypeVarSymbol, NEXT_PROCESS_METHOD_NAME);
    }

    private List<BLangRecordLiteral.BLangRecordKeyValue> getFieldListInSelectClause
            (DiagnosticPos pos, List<? extends SelectExpressionNode> selectExprList,
             BVarSymbol streamEventSymbol, BVarSymbol aggregatorArraySymbol, BLangGroupBy groupBy) {
        LongAdder aggregatorIndex = new LongAdder();
        List<BLangRecordLiteral.BLangRecordKeyValue> recordKeyValueList = new ArrayList<>();

        for (SelectExpressionNode expressionNode : selectExprList) {
            BLangSelectExpression selectExpression = (BLangSelectExpression) expressionNode;
            BLangRecordLiteral.BLangRecordKeyValue recordKeyValue = (BLangRecordLiteral.BLangRecordKeyValue)
                    TreeBuilder.createRecordKeyValue();

            if (selectExpression.getIdentifier() != null) {
                BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
                varRef.variableName = ASTBuilderUtil.createIdentifier(pos,
                        selectExpression.getIdentifier());
                recordKeyValue.key = new BLangRecordLiteral.BLangRecordKey(varRef);
                recordKeyValue.key.fieldSymbol = getOutputEventFieldSymbol(outputEventType,
                        selectExpression.getIdentifier());
            } else {
                BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
                varRef.variableName = ((BLangFieldBasedAccess) selectExpression.getExpression()).field;
                recordKeyValue.key = new BLangRecordLiteral.BLangRecordKey(varRef);
                recordKeyValue.key.fieldSymbol = getOutputEventFieldSymbol(outputEventType,
                        ((BLangFieldBasedAccess) selectExpression.getExpression()).field.value);
            }

            if (selectExpression.getExpression().getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
                BLangFieldBasedAccess fieldBasedAccess = (BLangFieldBasedAccess) selectExpression.getExpression();
                recordKeyValue.valueExpr =
                        createMapVariableIndexAccessExpr((BVarSymbol) createEventDataFieldAccessExpr(fieldBasedAccess
                        .pos, streamEventSymbol).symbol, fieldBasedAccess);
            } else if (selectExpression.getExpression().getKind() == NodeKind.INVOCATION) {
                setInvocationToRecordKeyValue(recordKeyValue, streamEventSymbol, aggregatorArraySymbol,
                        aggregatorIndex, selectExpression, groupBy);
            } else {
                recordKeyValue.valueExpr = generateConversionExpr((BLangExpression) selectExpression.getExpression(),
                        symTable.anyType, symResolver);
            }
            recordKeyValueList.add(recordKeyValue);
        }
        return recordKeyValueList;
    }

    //this function will refactor the selector invocations appropriately by checking whether it is an aggregate or not
    private void setInvocationToRecordKeyValue(BLangRecordLiteral.BLangRecordKeyValue recordKeyValue,
                                               BVarSymbol streamEventSymbol, BVarSymbol aggregatorArraySymbol,
                                               LongAdder aggregatorIndex, BLangSelectExpression selectExpression,
                                               BLangGroupBy groupBy) {
        // Aggregator invocation in streaming query ( sum(..), count(..) .. etc)
        BLangInvocation invocation = (BLangInvocation) selectExpression.getExpression();

        BInvokableSymbol symbol = getInvokableSymbol(invocation, Names.STREAMS_MODULE);
        if (symbol != null) {
            if (isReturnTypeMatching(invocation.pos, AGGREGATOR_OBJECT_NAME, symbol)) {

                // aggregatorArr[0].process(e.data["inputStream.age"], e.eventType)
                recordKeyValue.valueExpr = generateAggregatorInvocation(streamEventSymbol, aggregatorArraySymbol,
                        aggregatorIndex.longValue(), invocation);
                aggregatorIndex.increment();
            } else {
                invocation = refactorInvocationWithIndexBasedArgs((BVarSymbol)
                        createEventDataFieldAccessExpr(invocation.pos, streamEventSymbol).symbol, invocation);
                recordKeyValue.valueExpr = generateConversionExpr(invocation, symTable.anyType, symResolver);
            }
        }
    }

    // func(inputStream.name) into func(check <string> e.data["inputStream.name"])
    private BLangInvocation refactorInvocationWithIndexBasedArgs(BVarSymbol mapVarSymbol, BLangInvocation invocation) {
        if (invocation.requiredArgs.size() > 0) {
            List<BLangExpression> expressionList = new ArrayList<>();
            List<BLangExpression> functionArgsList = invocation.requiredArgs;
            for (BLangExpression expression : functionArgsList) {
                if (expression.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR &&
                        (((BLangFieldBasedAccess) expression).expr.type.tag == TypeTags.STREAM)) {
                    BLangFieldBasedAccess expr = (BLangFieldBasedAccess) expression;
                    BLangExpression fieldAccessExpr = createMapVariableIndexAccessExpr(mapVarSymbol, expr);
                    expressionList.add(createCheckedExpr(generateConversionExpr(fieldAccessExpr,
                            expr.type, symResolver)));
                } else if (expression.getKind() == NodeKind.SIMPLE_VARIABLE_REF && isInHaving) {
                    BLangSimpleVarRef varRef = (BLangSimpleVarRef) expression;
                    expressionList.add(refactorVarRefAfterSelect(varRef, varRef.symbol.type));
                } else if (expression.getKind() == NodeKind.INVOCATION) {
                    expressionList.add(refactorInvocationWithIndexBasedArgs(mapVarSymbol, (BLangInvocation)
                            expression));
                } else {
                    expressionList.add(expression);
                }
            }
            invocation.argExprs = expressionList;
            invocation.requiredArgs = expressionList;
        }
        return invocation;
    }

    private BLangInvocation generateAggregatorInvocation(BVarSymbol streamEventSymbol, BVarSymbol aggregatorArraySymbol,
                                                         long aggregatorIndex, BLangInvocation invocation) {
        // aggregatorArr[0]
        BLangIndexBasedAccess indexBasedAccess = createIndexBasedAggregatorExpr(aggregatorArraySymbol,
                aggregatorIndex, invocation.pos);

        // aggregatorArr[0].process(..)
        BLangInvocation aggregatorInvocation = ASTBuilderUtil.createInvocationExpr(invocation.pos,
                getNextProcessFunctionSymbol(indexBasedAccess.type.tsymbol), Collections.emptyList(), symResolver);
        aggregatorInvocation.expr = indexBasedAccess;

        // arguments of aggregatorArr[0].process(..). e.g. (t.age, e.eventType)
        List<BVarSymbol> params = ((BInvokableSymbol) aggregatorInvocation.symbol).params;
        aggregatorInvocation.requiredArgs = generateAggregatorInvocationArgs(streamEventSymbol, invocation,
                params);

        return aggregatorInvocation;
    }

    private List<BLangExpression> generateAggregatorInvocationArgs(BVarSymbol streamEventSymbol,
                                                                   BLangInvocation funcInvocation,
                                                                   List<BVarSymbol> params) {
        // generates the fields which will be aggregated e.g. t.age
        List<BLangExpression> args = generateAggregatorInputFieldsArgs(streamEventSymbol, funcInvocation,
                params);
        // generate the EventType for the aggregation o.eventType
        BLangFieldBasedAccess streamEventFieldAccess = generateStreamEventTypeForAggregatorArg(streamEventSymbol,
                funcInvocation.pos, params);
        // (t.age, o.eventType)
        args.add(streamEventFieldAccess);
        return args;
    }

    private BLangFieldBasedAccess generateStreamEventTypeForAggregatorArg(BVarSymbol streamEventSymbol,
                                                                          DiagnosticPos pos,
                                                                          List<BVarSymbol> params) {
        BLangFieldBasedAccess streamEventFieldAccess = createFieldBasedEventTypeExpr(streamEventSymbol, pos);
        // always the 2nd parameter is the EventType, so the 2nd parameter's type should match.
        streamEventFieldAccess.type = params.get(1).type;
        return streamEventFieldAccess;
    }

    private List<BLangExpression> generateAggregatorInputFieldsArgs(BVarSymbol streamEventSymbol,
                                                                    BLangInvocation funcInvocation,
                                                                    List<BVarSymbol> params) {
        List<BLangExpression> args = new ArrayList<>();
        int i = 0;
        for (BLangExpression expr : funcInvocation.argExprs) {
            if (expr.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
                BLangExpression mapAccessExpr = createMapVariableIndexAccessExpr((BVarSymbol)
                        createEventDataFieldAccessExpr(expr.pos, streamEventSymbol).symbol, expr);
                BLangExpression castExpr = Desugar.addConversionExprIfRequired(mapAccessExpr, params.get(i).type,
                        types, symTable, symResolver);
                args.add(castExpr);
            } else {
                args.add(expr);
            }
            i++;
        }
        // handles special cases like count(), which does not need arguments.
        if (args.isEmpty()) {
            args.add(ASTBuilderUtil.createLiteral(funcInvocation.pos, symTable.nilType, Names.NIL_VALUE));
        }
        return args;
    }

    private BLangFieldBasedAccess createFieldBasedEventTypeExpr(BVarSymbol streamEventSymbol, DiagnosticPos pos) {
        // o.eventType without the type
        BLangSimpleVarRef varStreamEventRef = ASTBuilderUtil.createVariableRef(pos, streamEventSymbol);
        return ASTBuilderUtil.createFieldAccessExpr(varStreamEventRef, ASTBuilderUtil.createIdentifier(pos,
                EVENT_TYPE_VARIABLE_NAME));
    }

    private BLangIndexBasedAccess createIndexBasedAggregatorExpr(BVarSymbol aggregatorArraySymbol, long aggregatorIndex,
                                                                 DiagnosticPos pos) {
        BLangSimpleVarRef fieldVarRef = ASTBuilderUtil.createVariableRef(pos, aggregatorArraySymbol);
        BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(pos, symTable.intType, aggregatorIndex);
        BLangIndexBasedAccess indexAccessExpr = ASTBuilderUtil.createIndexAccessExpr(fieldVarRef, indexExpr);
        indexAccessExpr.type = ((BArrayType) aggregatorArraySymbol.type).eType;
        return indexAccessExpr;
    }

    private BLangCheckedExpr createCheckedExpr(BLangTypeConversionExpr conversionExpr) {
        BLangCheckedExpr checkedExpr = (BLangCheckedExpr) TreeBuilder.createCheckExpressionNode();

        checkedExpr.expr = conversionExpr;
        checkedExpr.type = conversionExpr.targetType;
        checkedExpr.equivalentErrorTypeList = Collections.singletonList(symTable.errStructType);
        checkedExpr.pos = conversionExpr.pos;
        return checkedExpr;
    }

    private BLangIndexBasedAccess createMapVariableIndexAccessExpr(BVarSymbol mapVariableSymbol,
                                                                   BLangExpression expression) {
        BLangSimpleVarRef varRef = ASTBuilderUtil.createVariableRef(expression.pos, mapVariableSymbol);
        BLangIndexBasedAccess selectFieldExpression = ASTBuilderUtil.createIndexAccessExpr(varRef,
                ASTBuilderUtil.createLiteral(expression.pos, symTable.stringType, expression.toString()));
        selectFieldExpression.type = symTable.anyType;
        selectFieldExpression.pos = expression.pos;
        return selectFieldExpression;
    }

    private BLangLambdaFunction createLambdaFunction(DiagnosticPos pos, List<BLangVariable> lambdaFunctionVariable,
                                                     Set<BVarSymbol> closureVarSymbols, BLangValueType returnType) {
        BLangLambdaFunction lambdaFunction = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        BLangFunction filterLambdaFunctionNode = ASTBuilderUtil.createFunction(pos,
                getFunctionName(FUNC_CALLER));
        lambdaFunction.function = filterLambdaFunctionNode;
        BLangBlockStmt lambdaBody = ASTBuilderUtil.createBlockStmt(pos);
        filterLambdaFunctionNode.requiredParams.addAll(lambdaFunctionVariable);
        filterLambdaFunctionNode.returnTypeNode = ASTBuilderUtil.createTypeNode(symTable.booleanType);
        filterLambdaFunctionNode.setReturnTypeNode(returnType);
        defineFunction(filterLambdaFunctionNode, env.enclPkg);

        filterLambdaFunctionNode.body = lambdaBody;
        filterLambdaFunctionNode.closureVarSymbols = closureVarSymbols;
        filterLambdaFunctionNode.desugared = false;
        lambdaFunction.pos = pos;
        lambdaFunction.type = symTable.anyType;

        return lambdaFunction;
    }

    private void createStreamAliasMap(BLangStreamingInput streamingInput) {
        //Identify the alias if there anything as such
        if (streamingInput.getAlias() != null) {
            streamAliasMap.put(((BLangSimpleVarRef) streamingInput.getStreamReference()).symbol.toString(),
                    streamingInput.getAlias());
        }
    }

    private List<BLangVariable> createForeachVariables(BLangFunction funcNode) {
        List<BLangVariable> foreachVariables = new ArrayList<>();
        final List<BType> varTypes = Lists.of(symTable.intType, symTable.mapType);
        for (int i = 0; i < varTypes.size(); i++) {
            BType type = varTypes.get(i);
            String varName = VAR_FOREACH_VAL + i;
            final BLangVariable variable = ASTBuilderUtil.createVariable(funcNode.pos, varName, type);
            foreachVariables.add(variable);
            defineVariable(variable, funcNode.symbol.pkgID, funcNode.symbol);
        }
        return foreachVariables;
    }

    private void defineVariable(BLangVariable variable, PackageID pkgID, BSymbol owner) {
        variable.symbol = new BVarSymbol(0, names.fromIdNode(variable.name), pkgID, variable.type, owner);
    }
}
