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
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.clauses.HavingNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConversionOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
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
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
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
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Class responsible for desugar an iterable chain into actual Ballerina code.
 *
 * @since 0.980.0
 */
public class StreamingCodeDesugar extends BLangNodeVisitor {

    private static final String FUNC_CALLER = "$lambda$streaming";
    private static final String OUTPUT_FUNC_REFERENCE = "$lambda$streaming$output$function";
    private static final String OUTPUT_FUNC_VAR_ARG= "$lambda$streaming$output$function$var$arg";
    private static final String WINDOW_FUNC_REFERENCE = "$lambda$streaming$window$reference";
    private static final String OUTPUT_PROCESS_FUNC_REFERENCE = "$lambda$streaming$output$process";
    private static final String FILTER_FUNC_REFERENCE = "$lambda$streaming$filter";
    private static final String SIMPLE_SELECT_FUNC_REFERENCE = "$lambda$streaming$simple$select";
    private static final String SELECT_WITH_GROUP_BY_FUNC_REFERENCE = "$lambda$streaming$groupby$select";
    private static final String INPUT_STREAM_PARAM_REFERENCE = "$lambda$streaming$input$variable";
    private static final String FILTER_LAMBDA_PARAM_REFERENCE = "$lambda$streaming$filter$input$variable";
    private static final String SELECT_LAMBDA_PARAM_REFERENCE = "$lambda$streaming$simple$select$input$variable";
    private static final String SELECT_WITH_GROUP_BY_LAMBDA_PARAM_REFERENCE =
            "$lambda$streaming$groupby$select$input$variable";
    private static final String STREAM_EVENT_ARRAY_PARAM_REFERENCE = "$lambda$streaming$stream$event$variable";
    private static final String OUTPUT_EVENT_SELECTOR_PARAM_REFERENCE =
            "$lambda$streaming$output$event$selector$variable";
    private static final String STREAMS_STDLIB_PACKAGE_NAME = "streams";
    private static final String NEXT_PROCESS_METHOD_NAME = "process";
    private static final String STREAM_EVENT_OBJECT_NAME = "StreamEvent";
    private static final String FILTER_OBJECT_NAME = "Filter";
    private static final String WINDOW_OBJECT_NAME = "Window";
    private static final String OUTPUT_PROCESS_OBJECT_NAME = "OutputProcess";
    private static final String CREATE_OUTPUT_PROCESS_METHOD_NAME = "createOutputProcess";
    private static final String CREATE_FILTER_METHOD_NAME = "createFilter";
    private static final String SIMPLE_SELECT_OBJECT_NAME = "SimpleSelect";
    private static final String SELECT_WITH_GROUP_BY_OBJECT_NAME = "Select";
    private static final String CREATE_SIMPLE_SELECT_METHOD_NAME = "createSimpleSelect";
    private static final String CREATE_SELECT_WITH_GROUP_BY_METHOD_NAME = "createSelect";
    private static final String EVENT_DATA_VARIABLE_NAME = "data";
    private static final String EVENT_TYPE_VARIABLE_NAME = "eventType";
    private static final String BUILD_STREAM_EVENT_METHOD_NAME = "buildStreamEvent";
    private static final String STREAM_SUBSCRIBE_METHOD_NAME = "stream.subscribe";

    private static final CompilerContext.Key<StreamingCodeDesugar> STREAMING_DESUGAR_KEY =
            new CompilerContext.Key<>();

    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final SymbolEnter symbolEnter;
    private final Names names;
    private final Types types;
    private int lambdaFunctionCount = 0;
    private SymbolEnv env;
    private List<BLangStatement> stmts;
    private BType inputStreamEventType;
    private BLangExpression filterConditionalExpression;
    private BLangVariable filterTypeCastedVariable;
    private BType outputEventType;
    private Stack<BVarSymbol> nextProcessVarSymbolStack = new Stack<>();
    private BLangVariable currentMapTypeLambdaVariable;


    private StreamingCodeDesugar(CompilerContext context) {
        context.put(STREAMING_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.types = Types.getInstance(context);
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
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        inputStreamEventType = null;
        filterConditionalExpression = null;
        filterTypeCastedVariable = null;
        outputEventType = null;

        inputStreamEventType = ((BStreamType) ((BLangExpression) streamingQueryStatement.getStreamingInput().
                getStreamReference()).type).constraint;

        //Construct the elements to publish events to output stream
        BLangStreamAction streamAction = (BLangStreamAction) streamingQueryStatement.getStreamingAction();
        streamAction.accept(this);

        BLangSelectClause selectClause = (BLangSelectClause) streamingQueryStatement.getSelectClause();
        selectClause.accept(this);

        //Build elements to consume events from input stream
        BLangStreamingInput streamingInput = (BLangStreamingInput) streamingQueryStatement.getStreamingInput();
        streamingInput.accept(this);
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
    //      => (TeacherOutput[] emp) {
    //              outputStream.publish(emp);
    //      }
    //
    // convert into below constructs.
    //
    //      function (map) outputFunc = (map t) => {
    //          TeacherOutput t1 = check <TeacherOutput>t;
    //          outputStream.publish(t1);
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
        BLangLambdaFunction outputLambdaFunc = createLambdaWithVarArg(lambdaFunction.pos, ASTBuilderUtil
                .createVariable(outputFuncArg.pos, getVariableName(OUTPUT_FUNC_VAR_ARG), symTable.mapType, null, new
                        BVarSymbol(0, names.fromString(getVariableName(OUTPUT_FUNC_VAR_ARG)), lambdaFunction.function
                        .symbol.pkgID, symTable.mapType, lambdaFunction.function.symbol.owner)), TypeKind.NIL);
        BLangTypeConversionExpr outputTypeConversionExpr = generateConversionExpr(ASTBuilderUtil.createVariableRef
                (outputFuncArg.pos, ((BLangVariable) outputLambdaFunc.getFunctionNode().getParameters().get(0)).symbol),
                outputEventType, symResolver);
        BLangCheckedExpr outputCheckedExpr = createCheckedConversionExpr(outputTypeConversionExpr);
        BLangInvocation streamActionInvocation = ASTBuilderUtil.createInvocationExprForMethod(lambdaFunction.pos,
                lambdaFunction.function.symbol, Collections.singletonList(outputCheckedExpr), symResolver);
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
                resolvePkgSymbol(outputLambdaFunc.pos, env, names.fromString(STREAMS_STDLIB_PACKAGE_NAME)).
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
        BLangFieldBasedAccess nextProcessMethodAccess = createNextProcessFuncPointer(selectClause);

        // [streams:sum(), streams:count(), ... etc], 2nd arg
        BLangArrayLiteral aggregateArray = createAggregatorArray(selectClause);

        // (streams:StreamEvent e) => string, 3rd arg
        BLangLambdaFunction groupingLambda = createGroupByLambda(selectClause);

        // (streams:StreamEvent e, streams:Aggregator[] aggregatorArr)  => any, 4th arg of createSelect
        BLangLambdaFunction aggregatorLambda = createAggregatorLambda(selectClause);


        BInvokableSymbol groupBySelectInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(selectClause.pos, env, names.fromString(STREAMS_STDLIB_PACKAGE_NAME)).
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
                BInvokableSymbol aggregatorInvokableSymbol = (BInvokableSymbol) symResolver.
                        resolvePkgSymbol(selectClause.pos, env, names.fromString(STREAMS_STDLIB_PACKAGE_NAME)).
                        scope.lookup(new Name(invocation.name.value)).symbol;
                BLangInvocation aggregatorInvocation = ASTBuilderUtil.
                        createInvocationExprForMethod(selectClause.pos, aggregatorInvokableSymbol,
                                Collections.emptyList(), symResolver);
                expr.exprs.add(aggregatorInvocation);
            }
        }
        return expr;
    }

    // Object.process
    private BLangFieldBasedAccess createNextProcessFuncPointer(BLangSelectClause selectClause) {
        BVarSymbol nextProcessInvokableTypeVarSymbol = nextProcessVarSymbolStack.pop();
        BInvokableSymbol nextProcessInvokableSymbol = getNextProcessFunctionSymbol(nextProcessInvokableTypeVarSymbol);

        BLangSimpleVarRef nextProcessSimpleVarRef = ASTBuilderUtil.createVariableRef(selectClause.pos,
                nextProcessInvokableTypeVarSymbol);
        BLangFieldBasedAccess nextProcessMethodAccess = (BLangFieldBasedAccess)
                TreeBuilder.createFieldBasedAccessNode();
        nextProcessMethodAccess.expr = nextProcessSimpleVarRef;
        nextProcessMethodAccess.symbol = nextProcessInvokableSymbol;
        nextProcessMethodAccess.type = nextProcessInvokableSymbol.type;
        nextProcessMethodAccess.pos = selectClause.pos;
        nextProcessMethodAccess.field = ASTBuilderUtil.createIdentifier(selectClause.pos, NEXT_PROCESS_METHOD_NAME);
        return nextProcessMethodAccess;
    }

    private BLangLambdaFunction createAggregatorLambda(BLangSelectClause selectClause) {

        BLangVariable varSelectFnStreamEvent =
                this.createFunctionArgVariable(getVariableName(SELECT_LAMBDA_PARAM_REFERENCE),
                                               selectClause.pos, env);
        BLangVariable varAggregatorArray =
                this.createAggregatorTypeVariable(getVariableName(SELECT_WITH_GROUP_BY_LAMBDA_PARAM_REFERENCE),
                        selectClause.pos, env);


        /* (streams:StreamEvent e, streams:Aggregator[] aggregatorArr)  => any {

            });
        */
        BLangLambdaFunction selectWithGroupBy = createAggregatorLambdaWithParams(varAggregatorArray,
                varSelectFnStreamEvent, selectClause.pos);

        createAggregatorLambdaBody(selectClause, selectWithGroupBy);

        return selectWithGroupBy;
    }

    private void createAggregatorLambdaBody(BLangSelectClause selectClause, BLangLambdaFunction selectWithGroupBy) {

        BLangBlockStmt selectLambdaBody = selectWithGroupBy.function.body;
        BLangVariable varStreamEvent = selectWithGroupBy.function.requiredParams.get(0);
        BLangVariable varAggregatorArray = selectWithGroupBy.function.requiredParams.get(1);

        // Teacher t = check <Teacher> e.eventObject;
        BLangVariableDef typeCastingVariableDef = createEventObjectConversionStmt(selectClause, varStreamEvent);

        selectLambdaBody.stmts.add(typeCastingVariableDef);

        /* TeacherOutput teacherOutput = {
                        name: t.name,
                        age: t.age,
                        sumAge: check <int> aggregatorArr1[0].process(t.age, e.eventType),
                        count: check <int> aggregatorArr1[1].process((), e.eventType),
                        ...
                        ...
                    };
        */
        BLangVariableDef outputEventObjectVariableDef = addOutputObjectCreationStmt(selectClause, selectLambdaBody,
                varStreamEvent, varAggregatorArray, typeCastingVariableDef.var);

        // return teacherOutput;
        addReturnOutputStmt(selectClause, selectLambdaBody, outputEventObjectVariableDef.var);
    }

    private void addReturnOutputStmt(BLangSelectClause selectClause, BLangBlockStmt selectLambdaBody,
                                     BLangVariable var) {
        BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = selectClause.pos;
        returnStmt.expr = ASTBuilderUtil.createVariableRef(selectClause.pos, var.symbol);
        selectLambdaBody.stmts.add(returnStmt);
    }

    private BLangVariableDef addOutputObjectCreationStmt(BLangSelectClause selectClause,
                                                         BLangBlockStmt selectLambdaBody, BLangVariable varStreamEvent,
                                                         BLangVariable varAggregatorArray,
                                                         BLangVariable typeCastingVariable) {

        BLangRecordLiteral outputEventRecordLiteral = ASTBuilderUtil.createEmptyRecordLiteral(selectClause.pos,
                outputEventType);
        /* {
             name: t.name,
                    age: t.age,
                    sumAge: check <int> aggregatorArr1[0].process(t.age, e.eventType),
                    count: check <int> aggregatorArr1[1].process((), e.eventType),
                    ...
           }
        */
        List<BLangRecordLiteral.BLangRecordKeyValue> recordKeyValueList = getFieldListInSelectClause(selectClause.pos,
                selectClause.getSelectExpressions(), typeCastingVariable, true, varStreamEvent.symbol,
                varAggregatorArray.symbol);

        BVarSymbol outputEventVarSymbol =
                new BVarSymbol(0, new Name(getVariableName(OUTPUT_EVENT_SELECTOR_PARAM_REFERENCE)),
                        varStreamEvent.symbol.pkgID, outputEventType, env.scope.owner);

        // TeacherOutput teacherOutput;
        BLangVariable outputEventObjectVariable = ASTBuilderUtil.
                createVariable(selectClause.pos, getVariableName(OUTPUT_EVENT_SELECTOR_PARAM_REFERENCE),
                        outputEventType, outputEventRecordLiteral, outputEventVarSymbol);

        outputEventRecordLiteral.keyValuePairs = recordKeyValueList;

        BLangVariableDef outputEventObjectVariableDef = ASTBuilderUtil.createVariableDef(selectClause.pos,
                outputEventObjectVariable);
        selectLambdaBody.stmts.add(outputEventObjectVariableDef);

        return outputEventObjectVariableDef;
    }

    private BLangFieldBasedAccess createEventDataFieldAccessExpr(DiagnosticPos pos,
                                                                 BLangVariable varSelectFnStreamEvent) {
        BLangFieldBasedAccess eventDataField = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
        eventDataField.expr = ASTBuilderUtil.createVariableRef(pos, varSelectFnStreamEvent.symbol);
        eventDataField.type = symTable.mapType;
        eventDataField.symbol = ((BObjectType) (varSelectFnStreamEvent).type).fields.get(2).symbol;
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
        selectLambdaReturnType.setTypeKind(TypeKind.ANY);

        return createLambdaFunction(pos, new ArrayList<>(Arrays.asList(varSelectFnStreamEvent, varAggregatorArray)),
                selectLambdaClosureVarSymbols, selectLambdaReturnType);
    }

    //TODO: change this to pass an array of lambdas
    private BLangLambdaFunction createGroupByLambda(BLangSelectClause selectClause) {
        BLangVariable varGroupByStreamEvent =
                this.createFunctionArgVariable(getVariableName(SELECT_LAMBDA_PARAM_REFERENCE), selectClause.pos,
                                               env);
        // (streams:StreamEvent e) => string { .. }
        BLangLambdaFunction groupingLambda = createLambdaWithVarArg(selectClause.pos, varGroupByStreamEvent,
                                                                    TypeKind.STRING);
        BLangBlockStmt groupByLambda = groupingLambda.function.body;

        // Teacher t = check <Teacher> e.eventObject;
        BLangVariableDef typeCastingVariableDef = createEventObjectConversionStmt(selectClause,
                varGroupByStreamEvent);
        groupByLambda.stmts.add(typeCastingVariableDef);

        // return t.<attribute>
        DiagnosticPos pos = ((BLangExpression) selectClause.getGroupBy().getVariables().get(0)).pos;
        addReturnGroupByFieldStmt(pos, groupByLambda, typeCastingVariableDef);
        return groupingLambda;
    }

    private void addReturnGroupByFieldStmt(DiagnosticPos pos, BLangBlockStmt groupByLambda,
                                           BLangVariableDef typeCastingVariableDef) {
        BLangFieldBasedAccess groupingKeyExpr = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
        groupingKeyExpr.expr = ASTBuilderUtil.createVariableRef(pos, typeCastingVariableDef.var.symbol);
        groupingKeyExpr.type = symTable.stringType;
        groupingKeyExpr.symbol = ((BRecordType) (typeCastingVariableDef.var).type).fields.get(0).symbol;
        groupingKeyExpr.fieldKind = FieldKind.SINGLE;
        groupingKeyExpr.pos = pos;
        groupingKeyExpr.field = ASTBuilderUtil.createIdentifier(pos, groupingKeyExpr.symbol.name.value);

        BLangReturn returnStmt = ASTBuilderUtil.createReturnStmt(pos, groupByLambda);
        returnStmt.pos = pos;
        returnStmt.expr = groupingKeyExpr;
    }

    private BLangVariableDef createEventObjectConversionStmt(BLangSelectClause selectClause,
                                                             BLangVariable varStreamEvent) {
        String varStreamEventName = getVariableName(varStreamEvent.getName().getValue());
        BVarSymbol typeCastingVarSymbol = new BVarSymbol(0, new Name(varStreamEventName),
                varStreamEvent.symbol.pkgID, inputStreamEventType, env.scope.owner);

        // eventStream.eventObject
        BLangFieldBasedAccess eventObjectField = createEventDataFieldAccessExpr(selectClause.pos, varStreamEvent);

        //Create type casting for the streamEvent eventObject
        // check <Teacher> e.eventObject;
        BLangExpression typeCastingExpression = generateConversionExpr(eventObjectField, inputStreamEventType,
                symResolver);

        BLangVariable typeCastingVariable = ASTBuilderUtil.createVariable(selectClause.pos, varStreamEventName,
                inputStreamEventType, typeCastingExpression, typeCastingVarSymbol);

        // Teacher t = check <Teacher> e.eventObject;
        return ASTBuilderUtil.createVariableDef(selectClause.pos, typeCastingVariable);
    }

    private BLangLambdaFunction createLambdaWithVarArg(DiagnosticPos pos, BLangVariable varArg, TypeKind typeKind) {
        Set<BVarSymbol> varArgClosureSymbols = new LinkedHashSet<>();
        varArgClosureSymbols.add(varArg.symbol);

        BLangValueType returnType = new BLangValueType();
        returnType.setTypeKind(typeKind);

        return createLambdaFunction(pos, new ArrayList<>(Collections.singletonList(varArg)),
                varArgClosureSymbols, returnType);
    }

    private void createSimpleSelectStatement(BLangSelectClause selectClause) {
        BLangVariable varStreamEvent =
                this.createFunctionArgVariable(getVariableName(SELECT_LAMBDA_PARAM_REFERENCE), selectClause.pos, env);

        BLangLambdaFunction simpleSelectLambdaFunction = createLambdaWithVarArg(selectClause.pos, varStreamEvent,
                                                                                TypeKind.MAP);
        BLangBlockStmt lambdaBody = simpleSelectLambdaFunction.function.body;

        //Output object creation
        BLangRecordLiteral outputEventRecordLiteral = ASTBuilderUtil.createEmptyRecordLiteral(selectClause.pos,
                symTable.mapType);
        List<BLangRecordLiteral.BLangRecordKeyValue> recordKeyValueList = getFieldListInSelectClause(selectClause.pos,
                selectClause.getSelectExpressions(), varStreamEvent, false, null, null);

        BVarSymbol outputEventVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(OUTPUT_EVENT_SELECTOR_PARAM_REFERENCE)),
                varStreamEvent.symbol.pkgID, outputEventType, env.scope.owner);
        outputEventRecordLiteral.keyValuePairs = recordKeyValueList;
        BLangVariable outputEventObjectVariable = ASTBuilderUtil.
                createVariable(selectClause.pos, getVariableName(OUTPUT_EVENT_SELECTOR_PARAM_REFERENCE),
                        symTable.mapType, outputEventRecordLiteral, outputEventVarSymbol);

        outputEventObjectVariable.typeNode = ASTBuilderUtil.createTypeNode(symTable.mapType);
        BLangVariableDef outputEventObjectVariableDef = ASTBuilderUtil.createVariableDef(selectClause.pos,
                outputEventObjectVariable);
        lambdaBody.stmts.add(outputEventObjectVariableDef);

        // Return statement with newly created output event
        addReturnOutputStmt(selectClause, lambdaBody, outputEventObjectVariable);

        //Create event simple selector definition
        BLangFieldBasedAccess nextProcessMethodAccess = createNextProcessFuncPointer(selectClause);

        BInvokableSymbol simpleSelectInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(selectClause.pos, env, names.fromString(STREAMS_STDLIB_PACKAGE_NAME)).
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
        BType lambdaParameterType = inputStreamEventType;

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
                resolvePkgSymbol(streamingInput.pos, env, names.fromString(STREAMS_STDLIB_PACKAGE_NAME)).
                scope.lookup(new Name(BUILD_STREAM_EVENT_METHOD_NAME)).symbol;

        BType streamEventArrayType = streamEventBuilderInvokableSymbol.type.getReturnType();
        BVarSymbol streamEventArrayTypeVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(STREAM_EVENT_ARRAY_PARAM_REFERENCE)), streamEventBuilderInvokableSymbol.pkgID,
                streamEventArrayType, env.scope.owner);

        List<BLangExpression> args = new ArrayList<>();
        args.add(eventToMapConversionExpr);
        args.add(ASTBuilderUtil.createLiteral(streamingInput.pos, symTable.stringType, ((BLangSimpleVarRef)
                streamingInput.getStreamReference()).variableName.value));

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
        BInvokableSymbol nextProcessInvokableSymbol = getNextProcessFunctionSymbol(nextProcessInvokableTypeVarSymbol);

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
                lookup(names.fromString(STREAM_SUBSCRIBE_METHOD_NAME))
                .symbol;
        List<BLangExpression> variables = new ArrayList<>(1);
        variables.add(streamSubscriberLambdaFunction);
        BLangInvocation invocationExpr = ASTBuilderUtil.
                createInvocationExprForMethod(streamingInput.pos, subscribeMethodSymbol, variables,
                        symResolver);

        invocationExpr.argExprs = variables;
        invocationExpr.expr = ASTBuilderUtil.createVariableRef(streamingInput.pos, (BVarSymbol)
                ((BLangSimpleVarRef) streamingInput.getStreamReference()).symbol);
        inputStreamSubscribeStatement.expr = invocationExpr;

        //Add stream subscriber function to stmts
        stmts.add(inputStreamSubscribeStatement);
    }

    /*
        e.g: window lengthWindow(5) will be turned into,

        streams:LengthWindow lengthWindow = streams:lengthWindow(select.process, 5);
     */
    @Override
    public void visit(BLangWindow window) {
        //Create event filter definition
        BVarSymbol nextProcessInvokableTypeVarSymbol = nextProcessVarSymbolStack.pop();
        BInvokableSymbol nextProcessInvokableSymbol = getNextProcessFunctionSymbol(nextProcessInvokableTypeVarSymbol);

        BLangSimpleVarRef nextProcessSimpleVarRef = ASTBuilderUtil.createVariableRef(window.pos,
                nextProcessInvokableTypeVarSymbol);
        BLangFieldBasedAccess nextProcessMethodAccess = createFieldBasedAccessForProcessFunc(window.pos,
                nextProcessInvokableSymbol, nextProcessSimpleVarRef);

        BInvokableSymbol windowInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(window.pos, env, names.fromString(STREAMS_STDLIB_PACKAGE_NAME)).
                scope.lookup(new Name(((BLangInvocation) window.getFunctionInvocation()).name.value)).symbol;

        BType windowInvokableType = windowInvokableSymbol.type.getReturnType();

        BVarSymbol windowInvokableTypeVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(WINDOW_FUNC_REFERENCE)), windowInvokableSymbol.pkgID,
                windowInvokableType, env.scope.owner);
        nextProcessVarSymbolStack.push(windowInvokableTypeVarSymbol);

        List<BLangExpression> args = new ArrayList<>();
        args.add(nextProcessMethodAccess);
        args.addAll(((BLangInvocation) window.getFunctionInvocation()).argExprs);

        BLangInvocation windowInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(window.pos, windowInvokableSymbol, args,
                        symResolver);
        windowInvocation.argExprs = args;
        BLangVariable windowInvokableTypeVariable = ASTBuilderUtil.createVariable(window.pos,
                getVariableName(WINDOW_FUNC_REFERENCE), windowInvokableType, windowInvocation,
                windowInvokableTypeVarSymbol);

        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.typeName = ASTBuilderUtil.createIdentifier(window.pos, WINDOW_OBJECT_NAME);
        userDefinedType.type = windowInvokableType;
        windowInvokableTypeVariable.setTypeNode(userDefinedType);
        BLangVariableDef windowInvokableTypeVariableDef = ASTBuilderUtil.createVariableDef(window.pos,
                windowInvokableTypeVariable);
        stmts.add(windowInvokableTypeVariableDef);
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
        visitFilter(where.pos, (BLangBinaryExpr) where.getExpression(), inputStreamEventType);
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
        resolveBinaryExpr(having.pos, (BLangBinaryExpr) having.getExpression(), (BRecordType) outputEventType);
        visitFilter(having.pos, (BLangBinaryExpr) having.getExpression(), outputEventType);
    }

    //------------------------------------- Methods required for filter / having -----------------------------------
    private void visitFilter(DiagnosticPos pos, BLangBinaryExpr expression, BType eventType) {
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
        currentMapTypeLambdaVariable = lambdaFunctionVariable;
        final BLangBinaryExpr binaryExpr = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpr.pos = pos;
        binaryExpr.type = symTable.booleanType;
        binaryExpr.opKind = expression.getOperatorKind();
        expression.getLeftExpression().accept(this);
        binaryExpr.lhsExpr = filterConditionalExpression;
        expression.getRightExpression().accept(this);
        binaryExpr.rhsExpr = filterConditionalExpression;
        binaryExpr.opSymbol = expression.opSymbol;
        returnStmt.expr = binaryExpr;
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
                resolvePkgSymbol(pos, env, names.fromString(STREAMS_STDLIB_PACKAGE_NAME)).
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
                createInvocationExprForMethod(pos, havingInvokableSymbol, args,
                        symResolver);
        havingMethodInvocation.argExprs = args;

        BLangVariable havingInvokableTypeVariable = ASTBuilderUtil.
                createVariable(pos, getVariableName(FILTER_FUNC_REFERENCE),
                        havingInvokableType, havingMethodInvocation, havingInvokableTypeVarSymbol);

        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.typeName = ASTBuilderUtil.createIdentifier(pos, FILTER_OBJECT_NAME);
        userDefinedType.type = havingInvokableType;
        havingInvokableTypeVariable.setTypeNode(userDefinedType);
        BLangVariableDef havingInvokableTypeVariableDef = ASTBuilderUtil.createVariableDef(pos,
                havingInvokableTypeVariable);
        stmts.add(havingInvokableTypeVariableDef);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        final BLangBinaryExpr bLangBinaryExpr = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        bLangBinaryExpr.pos = binaryExpr.pos;
        bLangBinaryExpr.type = symTable.booleanType;
        bLangBinaryExpr.opKind = (binaryExpr).getOperatorKind();
        binaryExpr.getLeftExpression().accept(this);
        bLangBinaryExpr.lhsExpr = filterConditionalExpression;
        binaryExpr.getRightExpression().accept(this);
        bLangBinaryExpr.rhsExpr = filterConditionalExpression;
        bLangBinaryExpr.opSymbol = binaryExpr.opSymbol;
        filterConditionalExpression = bLangBinaryExpr;
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        BLangSimpleVarRef mapRef = ASTBuilderUtil.createVariableRef(fieldAccessExpr.pos, currentMapTypeLambdaVariable
                .symbol);
        String mapKey = fieldAccessExpr.toString();
        BLangExpression indexExpr = ASTBuilderUtil.createLiteral(fieldAccessExpr.field.pos, symTable.stringType,
                mapKey);
        BLangIndexBasedAccess mapAccessExpr = ASTBuilderUtil.createIndexAccessExpr(mapRef, indexExpr);
        mapAccessExpr.pos = fieldAccessExpr.pos;
        mapAccessExpr.type = symTable.anyType;
        BLangTypeConversionExpr conversionExpr = generateConversionExpr(mapAccessExpr, fieldAccessExpr.symbol.type,
                symResolver);
        filterConditionalExpression = createCheckedConversionExpr(conversionExpr);
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        filterConditionalExpression = literalExpr;
    }

    public void visit(BLangInvocation invocationExpr) {
        filterConditionalExpression = invocationExpr;
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        filterConditionalExpression = varRefExpr;
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

    private BLangVariable createFunctionArgVariable(String variableName, DiagnosticPos pos, SymbolEnv env) {
        BObjectTypeSymbol recordTypeSymbol = (BObjectTypeSymbol) symResolver.
                resolvePkgSymbol(pos, env, names.fromString(STREAMS_STDLIB_PACKAGE_NAME)).
                scope.lookup(new Name(STREAM_EVENT_OBJECT_NAME)).symbol;

        BType varType = recordTypeSymbol.type;
        BVarSymbol varSymbol = new BVarSymbol(0, new Name(variableName),
                varType.tsymbol.pkgID, varType, env.scope.owner);

        return ASTBuilderUtil.createVariable(pos, variableName, varType, null, varSymbol);
    }

    private BLangVariable createAggregatorTypeVariable(String variableName, DiagnosticPos pos, SymbolEnv env) {
        BInvokableSymbol invokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(pos, env, names.fromString(STREAMS_STDLIB_PACKAGE_NAME)).
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

    private BInvokableSymbol getNextProcessFunctionSymbol(BSymbol nextProcessInvokableTypeVarSymbol) {
        List<BAttachedFunction> attachedFunctionsList = ((BObjectTypeSymbol)
                (nextProcessInvokableTypeVarSymbol).type.tsymbol).attachedFuncs;
        for (BAttachedFunction attachedFunction : attachedFunctionsList) {
            if (attachedFunction.funcName.toString().equals(NEXT_PROCESS_METHOD_NAME)) {
                return attachedFunction.symbol;
            }
        }

        throw new IllegalStateException("Couldn't evaluate the process method of the next processor : " +
                (nextProcessInvokableTypeVarSymbol).type.toString());
    }

    private List<BLangRecordLiteral.BLangRecordKeyValue> getFieldListInSelectClause
            (DiagnosticPos pos, List<? extends SelectExpressionNode> selectExprList,
             BLangVariable mapVariable, boolean isGroupBy, BVarSymbol streamEventSymbol,
             BVarSymbol aggregatorArraySymbol) {
        long aggregatorIndex = 0;
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
                        .pos, mapVariable).symbol, fieldBasedAccess);
            } else if (isGroupBy && selectExpression.getExpression().getKind() == NodeKind.INVOCATION) {
                // Aggregator invocation in streaming query ( sum(..), count(..) .. etc)
                BLangInvocation selectFuncInvocation = (BLangInvocation) selectExpression.getExpression();

                // aggregatorArr[0].process(t.age, e.eventType)
                BLangInvocation aggregatorInvocation = generateAggregatorInvocation(mapVariable.symbol,
                        streamEventSymbol, aggregatorArraySymbol, aggregatorIndex, selectFuncInvocation);

                // <int> aggregatorArr[0].process(t.age, e.eventType)
                BLangExpression conversionExpr = generateConversionExpr(aggregatorInvocation,
                        recordKeyValue.key.fieldSymbol.type, symResolver);

                // check <int> aggregatorArr[0].process(t.age, e.eventType) and assign it to valueExpr
                recordKeyValue.valueExpr = createCheckedConversionExpr((BLangTypeConversionExpr) conversionExpr);

                //increment the index in aggregatorArr
                aggregatorIndex++;
            } else if (selectExpression.getExpression().getKind() == NodeKind.INVOCATION) {
                BLangInvocation selectFuncInvocation = (BLangInvocation) selectExpression.getExpression();
                if (selectFuncInvocation.requiredArgs.size() > 0) {
                    List<BLangExpression> expressionList = new ArrayList<>();
                    List<BLangExpression> functionArgsList = selectFuncInvocation.requiredArgs;
                    for (BLangExpression expression : functionArgsList) {
                        if (expression instanceof BLangFieldBasedAccess && ((BLangSimpleVarRef)
                                ((BLangFieldBasedAccess) expression).expr).type.tag == TypeTags.STREAM) {
                            BLangExpression fieldAccessExpr =
                                    createMapVariableIndexAccessExpr(createEventDataFieldAccessExpr(expression.pos,
                                                                                                    mapVariable).varSymbol, expression);
                            expressionList.add(fieldAccessExpr);
                        } else {
                            expressionList.add(expression);
                        }
                    }
                    selectFuncInvocation.argExprs = expressionList;
                    selectFuncInvocation.requiredArgs = expressionList;
                }
                recordKeyValue.valueExpr = selectFuncInvocation;

            } else {
                recordKeyValue.valueExpr = generateConversionExpr((BLangExpression) selectExpression.getExpression(),
                        symTable.anyType, symResolver);

            }

            recordKeyValueList.add(recordKeyValue);
        }

        return recordKeyValueList;
    }

    private BLangInvocation generateAggregatorInvocation(BVarSymbol typeCastedVariableSymbol,
                                                         BVarSymbol streamEventSymbol, BVarSymbol aggregatorArraySymbol,
                                                         long aggregatorIndex, BLangInvocation selectFuncInvocation) {
        // aggregatorArr[0]
        BLangIndexBasedAccess indexBasedAccess = createIndexBasedAggregatorExpr(aggregatorArraySymbol,
                aggregatorIndex, selectFuncInvocation.pos);

        // aggregatorArr[0].process(..)
        BLangInvocation aggregatorInvocation = ASTBuilderUtil.createInvocationExpr(selectFuncInvocation.pos,
                getNextProcessFunctionSymbol(indexBasedAccess.type.tsymbol), Collections.emptyList(), symResolver);
        aggregatorInvocation.expr = indexBasedAccess;

        // arguments of aggregatorArr[0].process(..). e.g. (t.age, e.eventType)
        List<BVarSymbol> params = ((BInvokableSymbol) aggregatorInvocation.symbol).params;
        aggregatorInvocation.requiredArgs = generateAggregatorInvocationArgs(typeCastedVariableSymbol,
                streamEventSymbol, selectFuncInvocation, params);

        return aggregatorInvocation;
    }

    private List<BLangExpression> generateAggregatorInvocationArgs(BVarSymbol typeCastedVariableSymbol,
                                                                   BVarSymbol streamEventSymbol,
                                                                   BLangInvocation funcInvocation,
                                                                   List<BVarSymbol> params) {
        // generates the fields which will be aggregated e.g. t.age
        List<BLangExpression> args = generateAggregatorInputFieldsArgs(typeCastedVariableSymbol, funcInvocation,
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

    private List<BLangExpression> generateAggregatorInputFieldsArgs(BVarSymbol typeCastedVariableSymbol,
                                                                    BLangInvocation funcInvocation,
                                                                    List<BVarSymbol> params) {
        List<BLangExpression> args = new ArrayList<>();
        int i = 0;
        for (BLangExpression expr : funcInvocation.argExprs) {
            if (expr.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
                BLangExpression fieldAccessExpr = createMapVariableIndexAccessExpr
                        (typeCastedVariableSymbol, expr);
                BLangExpression castExpr = Desugar.addConversionExprIfRequired(fieldAccessExpr,
                        params.get(i).type, types, symTable, symResolver);
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

    private BLangFieldBasedAccess createFieldBasedEventTypeExpr(BVarSymbol streamEventSymbol,
                                                                DiagnosticPos pos) {
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

    private BLangCheckedExpr createCheckedConversionExpr(BLangTypeConversionExpr conversionExpr) {
        BLangCheckedExpr checkedExpr = (BLangCheckedExpr) TreeBuilder.createCheckExpressionNode();

        checkedExpr.expr = conversionExpr;
        checkedExpr.type = conversionExpr.targetType;
        checkedExpr.equivalentErrorTypeList = Collections.singletonList(symTable.errStructType);
        checkedExpr.pos = conversionExpr.pos;
        return checkedExpr;
    }

    private BLangExpression createMapVariableIndexAccessExpr(BVarSymbol mapVariableSymbol,
            BLangExpression expression) {
        BLangFieldBasedAccess fieldAccessExpr = (BLangFieldBasedAccess) expression;
        BLangSimpleVarRef varRef = ASTBuilderUtil.createVariableRef(expression.pos, mapVariableSymbol);
        BLangIndexBasedAccess selectFieldExpression = ASTBuilderUtil.createIndexAccessExpr(varRef,
                ASTBuilderUtil.createLiteral(fieldAccessExpr.field.pos, symTable.stringType, fieldAccessExpr.toString()));
        selectFieldExpression.symbol = fieldAccessExpr.symbol;
        selectFieldExpression.type = symTable.anyType;
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

    private void resolveBinaryExpr(DiagnosticPos pos, BLangBinaryExpr expression, BRecordType eventType) {
        Map<String, BField> fields = eventType.fields.stream()
                .collect(Collectors.toMap(field -> field.name.getValue(), field -> field, (a, b) -> b));
        if (expression.rhsExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            String key = ((BLangSimpleVarRef) expression.rhsExpr).variableName.value;
            if (fields.containsKey(key)) {
                BField field = fields.get(key);
                expression.rhsExpr = createFieldBasedAccessExpr(pos, field);
                if (expression.lhsExpr.type == null) {
                    expression.lhsExpr.type = field.getType();
                }
            }
        }
        if (expression.lhsExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            String key = ((BLangSimpleVarRef) expression.lhsExpr).variableName.value;
            if (fields.containsKey(key)) {
                BField field = fields.get(key);
                expression.lhsExpr = createFieldBasedAccessExpr(pos, field);
                if (expression.rhsExpr.type == null) {
                    expression.rhsExpr.type = field.getType();
                }
            }
        }
        expression.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(
                expression.opKind, expression.lhsExpr.type, expression.rhsExpr.type);
    }

    private static BLangFieldBasedAccess createFieldBasedAccessExpr(DiagnosticPos pos, BField field) {
        BLangFieldBasedAccess fieldBasedAccess = (BLangFieldBasedAccess)
                TreeBuilder.createFieldBasedAccessNode();
        fieldBasedAccess.expr = ASTBuilderUtil.createVariableRef(pos, field.symbol);
        fieldBasedAccess.symbol = field.symbol;
        fieldBasedAccess.type = field.getType();
        fieldBasedAccess.pos = pos;
        fieldBasedAccess.field = ASTBuilderUtil.createIdentifier(pos, field.name.value);
        return fieldBasedAccess;
    }
}
