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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamAction;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Class responsible for desugar an iterable chain into actual Ballerina code.
 *
 * @since 0.961.0
 */
public class StreamingCodeDesugar extends BLangNodeVisitor {

    private static final String FUNC_CALLER = "$lambda$streaming";
    private static final String OUTPUT_FUNC_REFERENCE = "$lambda$streaming$output$function";
    private static final String OUTPUT_PROCESS_FUNC_REFERENCE = "$lambda$streaming$output$process";
    private static final String INPUT_STREAM_PARAM_REFERENCE = "$lambda$streaming$input$variable";
    private static final String STREAM_EVENT_ARRAY_PARAM_REFERENCE = "$lambda$streaming$stream$event$variable";

    private static final CompilerContext.Key<StreamingCodeDesugar> STREAMING_DESUGAR_KEY =
            new CompilerContext.Key<>();

    private Desugar parentDesugar;
    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final SymbolEnter symbolEnter;
    private final Names names;
    private final Types types;
    private int lambdaFunctionCount = 0;
    private SymbolEnv env;
    private List<BLangStatement> stmts;
    private Set<BVarSymbol> closureVarSymbols = new LinkedHashSet<>();
    private BLangVariable outputStreamFunctionVariable;
    private BVarSymbol outputProcessInvokableTypeVarSymbol;

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

    public BLangBlockStmt desugar(BLangForever foreverStatement, Desugar desugar) {

        this.parentDesugar = desugar;
        this.env = foreverStatement.getEnv();
        stmts = new ArrayList<>();
        List<? extends StatementNode> statementNodes = foreverStatement.getStreamingQueryStatements();

        // Generate Streaming Consumer Function
        statementNodes.forEach(statementNode -> ((BLangStatement) statementNode).accept(this));
        return ASTBuilderUtil.createBlockStmt(foreverStatement.pos, stmts);
    }

    @Override
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {

        //Construct the elements to publish events to output stream
        BLangStreamAction streamAction = (BLangStreamAction) streamingQueryStatement.getStreamingAction();
        streamAction.accept(this);

        //Build elements to consume events from input stream
        BLangStreamingInput streamingInput = (BLangStreamingInput) streamingQueryStatement.getStreamingInput();
        streamingInput.accept(this);
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        BLangLambdaFunction lambdaFunction = (BLangLambdaFunction) streamAction.getInvokableBody();
        lambdaFunction.accept(this);
    }

    @Override
    public void visit(BLangLambdaFunction lambdaFunction) {

        //Create lambda function Variable
        BLangVariable oldFunctionVarible = (BLangVariable) lambdaFunction.getFunctionNode().getParameters().get(0);
        BLangVariable lambdaFunctionVariable = this.createAnyTypeVariable(oldFunctionVarible, lambdaFunction.pos, env);

        //Create new lambda function to process the output events
        BLangLambdaFunction outputLambdaFunction = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        BLangFunction outputLambdaFunctionNode = ASTBuilderUtil.createFunction(lambdaFunction.pos,
                getFunctionName(FUNC_CALLER));
        outputLambdaFunction.function = outputLambdaFunctionNode;
        BLangBlockStmt lambdaBody = ASTBuilderUtil.createBlockStmt(lambdaFunction.pos);

        //New Lambda Function
        outputLambdaFunctionNode.requiredParams.add(lambdaFunctionVariable);
        outputLambdaFunctionNode.returnTypeNode = ASTBuilderUtil.createTypeNode(symTable.nilType);
        BLangValueType returnType = new BLangValueType();
        returnType.setTypeKind(TypeKind.NIL);
        outputLambdaFunctionNode.setReturnTypeNode(returnType);
        defineFunction(outputLambdaFunctionNode, env.enclPkg);

        outputLambdaFunctionNode.body = lambdaBody;
        outputLambdaFunctionNode.closureVarSymbols = closureVarSymbols;
        outputLambdaFunctionNode.desugared = false;
        outputLambdaFunction.pos = lambdaFunction.pos;
        outputLambdaFunction.type = symTable.anyType;


        //Create type casting for the output variable
        BVarSymbol typeCastingVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(oldFunctionVarible.getName().getValue())),
                lambdaFunctionVariable.symbol.pkgID, oldFunctionVarible.type, env.scope.owner);
        BLangSimpleVarRef typeCastingSimpleVarRef = ASTBuilderUtil.createVariableRef(lambdaFunction.pos,
                lambdaFunctionVariable.symbol);
        BLangExpression typeCastingExpression = generateConversionExpr(typeCastingSimpleVarRef,
                oldFunctionVarible.type, symResolver);
        BLangVariable typeCastingVariable = ASTBuilderUtil.
                createVariable(lambdaFunction.pos, getVariableName(oldFunctionVarible.getName().getValue()),
                        oldFunctionVarible.type, typeCastingExpression, typeCastingVarSymbol);
        typeCastingVariable.typeNode = ASTBuilderUtil.createTypeNode(oldFunctionVarible.type);
        BLangVariableDef typeCastingVariableDef = ASTBuilderUtil.createVariableDef(lambdaFunction.pos,
                typeCastingVariable);

        lambdaBody.stmts.add(typeCastingVariableDef);

        //Publish events to stream
        BLangExpressionStmt eventPublisherStatement = (BLangExpressionStmt) TreeBuilder.
                createExpressionStatementNode();
        eventPublisherStatement.pos = lambdaFunction.pos;
        BInvokableSymbol publisherMethodSymbol = (BInvokableSymbol) symTable.rootScope.lookup(names.
                fromString("stream.publish")).symbol;

        List<BLangVariable> variables = new ArrayList<>(1);
        variables.add(typeCastingVariable);
        BLangInvocation invocationExpr = ASTBuilderUtil.
                createInvocationExpr(lambdaFunction.pos, publisherMethodSymbol, variables, symResolver);
        BVarSymbol varSymbol = ((BLangSimpleVarRef.BLangPackageVarRef)
                ((BLangInvocation.BLangAttachedFunctionInvocation)
                        ((BLangExpressionStmt) lambdaFunction.getFunctionNode().getBody().getStatements().get(0)).
                                expr).expr).varSymbol;
        invocationExpr.expr = ASTBuilderUtil.createVariableRef(lambdaFunction.pos, varSymbol);
        eventPublisherStatement.expr = invocationExpr;

        lambdaBody.stmts.add(eventPublisherStatement);

        //Create Function definition statement
        outputStreamFunctionVariable = ASTBuilderUtil.
                createVariable(lambdaFunction.pos, getVariableName(OUTPUT_FUNC_REFERENCE),
                        outputLambdaFunction.type, outputLambdaFunction,
                        outputLambdaFunction.function.symbol);
        outputStreamFunctionVariable.typeNode = ASTBuilderUtil.createTypeNode(outputLambdaFunctionNode.type);
        BLangVariableDef outputStreamFunctionVarDef = ASTBuilderUtil.createVariableDef(lambdaFunction.pos,
                outputStreamFunctionVariable);

        stmts.add(outputStreamFunctionVarDef);

        //Create output event process definition
        BLangSimpleVarRef outputStreamFunctionSimpleVarRef = ASTBuilderUtil.createVariableRef(lambdaFunction.pos,
                outputStreamFunctionVariable.symbol);
        BInvokableSymbol outputProcessInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(lambdaFunction.pos, env, names.fromString("streams")).
                scope.lookup(new Name("createOutputProcess")).symbol;

        BType outputProcessInvokableType = outputProcessInvokableSymbol.type.getReturnType();
        outputProcessInvokableTypeVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(OUTPUT_PROCESS_FUNC_REFERENCE)), outputProcessInvokableSymbol.pkgID,
                outputProcessInvokableType, env.scope.owner);

        List<BLangExpression> args = new ArrayList<>();
        args.add(outputStreamFunctionSimpleVarRef);
        BLangInvocation outputProcessMethodInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(lambdaFunction.pos, outputProcessInvokableSymbol, args,
                        symResolver);
        outputProcessMethodInvocation.argExprs = args;
        BLangVariable outputProcessInvokableTypeVariable = ASTBuilderUtil.
                createVariable(lambdaFunction.pos, getVariableName(OUTPUT_PROCESS_FUNC_REFERENCE),
                        outputProcessInvokableType, outputProcessMethodInvocation, outputProcessInvokableTypeVarSymbol);


        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.typeName = ASTBuilderUtil.createIdentifier(lambdaFunction.pos, "OutputProcess");
        userDefinedType.type = outputProcessInvokableType;
        outputProcessInvokableTypeVariable.setTypeNode(userDefinedType);
        BLangVariableDef outputProcessInvokableTypeVariableDef = ASTBuilderUtil.createVariableDef(lambdaFunction.pos,
                outputProcessInvokableTypeVariable);

        stmts.add(outputProcessInvokableTypeVariableDef);
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {

        //Construct lambda function which consumes events
        BLangLambdaFunction streamSubscriberLambdaFunction = (BLangLambdaFunction) TreeBuilder.
                createLambdaFunctionNode();
        BLangFunction streamSubscriberLambdaFunctionNode = ASTBuilderUtil.createFunction(streamingInput.pos,
                getFunctionName(FUNC_CALLER));
        streamSubscriberLambdaFunction.function = streamSubscriberLambdaFunctionNode;
        BLangBlockStmt lambdaBody = ASTBuilderUtil.createBlockStmt(streamingInput.pos);

        //Lambda function parameter
        BType lambdaParameterType = ((BStreamType) ((BLangSimpleVarRef) streamingInput.getStreamReference()).type).
                constraint;
        BVarSymbol lambdaParameterVarSymbol = new BVarSymbol(0, new Name(getVariableName(INPUT_STREAM_PARAM_REFERENCE)),
                lambdaParameterType.tsymbol.pkgID, lambdaParameterType, env.scope.owner);

        BLangVariable inputStreamLambdaFunctionVariable = ASTBuilderUtil.createVariable(streamingInput.pos,
                getVariableName(INPUT_STREAM_PARAM_REFERENCE), lambdaParameterType, null, lambdaParameterVarSymbol);
        inputStreamLambdaFunctionVariable.typeNode = ASTBuilderUtil.createTypeNode(lambdaParameterType);


        //New Lambda Function
        streamSubscriberLambdaFunctionNode.requiredParams.add(inputStreamLambdaFunctionVariable);
        streamSubscriberLambdaFunctionNode.returnTypeNode = ASTBuilderUtil.createTypeNode(symTable.nilType);
        BLangValueType returnType = new BLangValueType();
        returnType.setTypeKind(TypeKind.NIL);
        streamSubscriberLambdaFunction.type = streamSubscriberLambdaFunctionNode.type;
        streamSubscriberLambdaFunctionNode.setReturnTypeNode(returnType);
        defineFunction(streamSubscriberLambdaFunctionNode, env.enclPkg);

        //Implement the lambdaBody

        //----- Event conversion to StreamEvent

        BLangSimpleVarRef lambdaParameterSimpleVarRef = ASTBuilderUtil.createVariableRef(streamingInput.pos,
                inputStreamLambdaFunctionVariable.symbol);
        BInvokableSymbol streamEventBuilderInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(streamingInput.pos, env, names.fromString("streams")).
                scope.lookup(new Name("buildStreamEvent")).symbol;

        BType streamEventArrayType = streamEventBuilderInvokableSymbol.type.getReturnType();
        BVarSymbol streamEventArrayTypeVarSymbol = new BVarSymbol(0,
                new Name(getVariableName(STREAM_EVENT_ARRAY_PARAM_REFERENCE)), streamEventBuilderInvokableSymbol.pkgID,
                streamEventArrayType, env.scope.owner);
        List<BLangExpression> args = new ArrayList<>();
        args.add(lambdaParameterSimpleVarRef);


        BLangInvocation streamEventBuilderMethodInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(streamingInput.pos, streamEventBuilderInvokableSymbol, args,
                        symResolver);
        streamEventBuilderMethodInvocation.argExprs = args;

        BLangVariable streamEventArrayTypeVariable = ASTBuilderUtil.
                createVariable(streamingInput.pos, getVariableName(STREAM_EVENT_ARRAY_PARAM_REFERENCE),
                        streamEventArrayType, streamEventBuilderMethodInvocation, streamEventArrayTypeVarSymbol);

        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.typeName = ASTBuilderUtil.createIdentifier(streamingInput.pos, "StreamEvent");
        userDefinedType.type = streamEventArrayType;
        streamEventArrayTypeVariable.setTypeNode(userDefinedType);
        BLangVariableDef streamEventArrayTypeVariableDef = ASTBuilderUtil.createVariableDef(streamingInput.pos,
                streamEventArrayTypeVariable);

        lambdaBody.stmts.add(streamEventArrayTypeVariableDef);

        //-------- Function invocation to call output process

        List<BAttachedFunction> attachedFunctionsList = ((BObjectTypeSymbol)
                (outputProcessInvokableTypeVarSymbol).type.tsymbol).attachedFuncs;
        BInvokableSymbol nextProcessInvokableSymbol = null;
        for (BAttachedFunction attachedFunction : attachedFunctionsList) {
            if (attachedFunction.funcName.toString().equals("process")) {
                nextProcessInvokableSymbol = attachedFunction.symbol;
            }
        }

        BLangSimpleVarRef streamEventArrayRef = ASTBuilderUtil.createVariableRef(streamingInput.pos,
                streamEventArrayTypeVarSymbol);
        List<BLangExpression> nextProcessVariables = new ArrayList<>(1);
        nextProcessVariables.add(streamEventArrayRef);
        BLangInvocation nextProcessMethodInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(streamingInput.pos, nextProcessInvokableSymbol, nextProcessVariables,
                        symResolver);
        nextProcessMethodInvocation.argExprs = nextProcessVariables;


        nextProcessMethodInvocation.expr = ASTBuilderUtil.createVariableRef(streamingInput.pos,
                outputProcessInvokableTypeVarSymbol);
        BLangExpressionStmt nextProcessExpressionStmt = (BLangExpressionStmt) TreeBuilder.
                createExpressionStatementNode();
        nextProcessExpressionStmt.pos = streamingInput.pos;
        nextProcessExpressionStmt.expr = nextProcessMethodInvocation;
        lambdaBody.stmts.add(nextProcessExpressionStmt);

        //Set lambda body
        streamSubscriberLambdaFunctionNode.body = lambdaBody;
        streamSubscriberLambdaFunctionNode.closureVarSymbols.add(outputProcessInvokableTypeVarSymbol);
        streamSubscriberLambdaFunctionNode.closureVarSymbols.add(inputStreamLambdaFunctionVariable.symbol);
        streamSubscriberLambdaFunctionNode.desugared = false;
        streamSubscriberLambdaFunction.pos = streamingInput.pos;
        streamSubscriberLambdaFunction.type = symTable.anyType;

        //Create function call - stream1.subscribe(lambda_function)
        BLangExpressionStmt inputStreamSubscribeStatement = (BLangExpressionStmt) TreeBuilder.
                createExpressionStatementNode();
        inputStreamSubscribeStatement.pos = streamingInput.pos;
        BInvokableSymbol subscribeMethodSymbol = (BInvokableSymbol) symTable.rootScope.
                lookup(names.fromString("stream.subscribe"))
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


    //-------------------------------------- Private methods ----------------------------------------------

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

    //----------------------------------------- Util Methods ---------------------------------------------------------

    private BLangVariable createAnyTypeVariable(BLangVariable langVariable, DiagnosticPos pos, SymbolEnv env) {
        BType varType = this.symTable.anyType;
        BVarSymbol varSymbol = new BVarSymbol(0, ((BVarSymbol) langVariable.symbol).name,
                varType.tsymbol.pkgID, varType, env.scope.owner);

        BLangVariable anyTypeVariable = ASTBuilderUtil.createVariable(pos, (langVariable.symbol).name.getValue(),
                varType, null, varSymbol);
        anyTypeVariable.typeNode = ASTBuilderUtil.createTypeNode(varType);
        return anyTypeVariable;
    }


    //----------------------------------------- Static Util Methods ---------------------------------------------------

    private static BLangExpression generateConversionExpr(BLangExpression varRef, BType target,
                                                          SymbolResolver symResolver) {

        // Box value using cast expression.
        final BLangTypeConversionExpr conversion = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        conversion.pos = varRef.pos;
        conversion.expr = varRef;
        conversion.type = target;
        conversion.targetType = target;
        conversion.conversionSymbol = (BConversionOperatorSymbol) symResolver.resolveConversionOperator(varRef.type,
                target);
        return conversion;
    }

    private static BLangVariable cloneBLangVariable(BLangVariable langVariable, DiagnosticPos pos, SymbolEnv env) {
        BType varType = langVariable.type;
        BVarSymbol varSymbol = new BVarSymbol(0, ((BVarSymbol) langVariable.symbol).name,
                varType.tsymbol.pkgID, varType, env.scope.owner);

        BLangVariable clonedBLangVariable = ASTBuilderUtil.createVariable(pos, (langVariable.symbol).name.getValue(),
                varType, null, varSymbol);
        clonedBLangVariable.typeNode = langVariable.getTypeNode();
        return clonedBLangVariable;
    }
}
