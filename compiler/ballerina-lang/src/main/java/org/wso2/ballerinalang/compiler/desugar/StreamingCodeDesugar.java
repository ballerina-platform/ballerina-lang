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
import org.ballerinalang.model.tree.clauses.StreamingInput;
import org.ballerinalang.model.tree.clauses.WhereNode;
import org.ballerinalang.model.tree.clauses.WindowClauseNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamAction;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhere;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWindow;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
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

    private static final CompilerContext.Key<StreamingCodeDesugar> STREAMING_DESUGAR_KEY =
            new CompilerContext.Key<>();

    private Desugar parentDesugar;
    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final SymbolEnter symbolEnter;
    private final Names names;
    private final Types types;
    private int lambdaFunctionCount = 0;
    private BLangFunction lambdaFunctionNode;
    private BLangIf ifNode;
    private BLangExpressionStmt foreverReplaceStatement;
    private BLangExpression conditionalExpression;
    private SymbolEnv env;
    private BLangBlockStmt lambdaBody;
    private List<BLangStatement> stmts;
    private BLangSimpleVarRef windowInvokableSimpleVarRef;
    private Set<BVarSymbol> closureVarSymbols = new LinkedHashSet<>();
    private BLangFunction newLambdaFunctionNode;
    private BVarSymbol paramVarSymbol;

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
        //generateStreamConsumerFunction(foreverStatement);

        statementNodes.forEach(statementNode -> ((BLangStatement) statementNode).accept(this));
        stmts.add(foreverReplaceStatement);
        BLangBlockStmt bLangBlockStmt = ASTBuilderUtil.createBlockStmt(foreverStatement.pos, stmts);
        newLambdaFunctionNode.enclBlockStmt = bLangBlockStmt;
        return bLangBlockStmt;
    }

    @Override
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        StreamingInput streamingInput = streamingQueryStatement.getStreamingInput();
        if (streamingInput != null) {

            BLangLambdaFunction lambdaFunction = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
            newLambdaFunctionNode = ASTBuilderUtil.createFunction(streamingQueryStatement.pos,
                    getFunctionName(FUNC_CALLER));
            lambdaFunction.function = newLambdaFunctionNode;

            //=================================================================================================

            lambdaFunctionNode = ((BLangLambdaFunction)
                    (streamingQueryStatement.getStreamingAction()).getInvokableBody()).function;

            lambdaBody = ASTBuilderUtil.createBlockStmt(streamingQueryStatement.pos);
            ((BLangStreamingInput) streamingInput).accept(this);

            WindowClauseNode windowClauseNode = streamingInput.getWindowClause();
            if (windowClauseNode != null) {
                ((BLangWindow) windowClauseNode).accept(this);
            }

            BLangStreamAction streamingAction = (BLangStreamAction) streamingQueryStatement.getStreamingAction();
            streamingAction.accept(this);

            //New Lambda Function

            newLambdaFunctionNode.requiredParams.add((BLangVariable) (streamingQueryStatement.getStreamingAction()).
                    getInvokableBody().getFunctionNode().getParameters().get(0));
            newLambdaFunctionNode.returnTypeNode = ASTBuilderUtil.createTypeNode(symTable.nilType);
            newLambdaFunctionNode.body = lambdaBody;
//            final BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
//            returnStmt.pos = streamingQueryStatement.pos;
//            returnStmt.expr = ASTBuilderUtil.createLiteral(streamingQueryStatement.pos, symTable.noType, Names.EMPTY);
//            newLambdaFunctionNode.body.stmts.add(returnStmt);
            newLambdaFunctionNode.closureVarSymbols = closureVarSymbols;
            newLambdaFunctionNode.desugared = false;
            BLangValueType returnType = new BLangValueType();
            returnType.setTypeKind(TypeKind.NIL);
            newLambdaFunctionNode.setReturnTypeNode(returnType);

            lambdaFunction.pos = streamingQueryStatement.pos;
            lambdaFunction.type = ((BLangLambdaFunction) (streamingQueryStatement.getStreamingAction()).
                    getInvokableBody()).type;
            defineFunction(newLambdaFunctionNode, env.enclPkg);

            //TODO Hack - fix this
            ifNode.parent = lambdaFunction;
//            paramVarSymbol.owner = newLambdaFunctionNode.symbol;

            //===========================================================================================

            foreverReplaceStatement = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
            foreverReplaceStatement.pos = streamingQueryStatement.pos;
            BInvokableSymbol subscribeMethodSymbol = (BInvokableSymbol) symTable.rootScope.
                    lookup(names.fromString("stream.subscribe"))
                    .symbol;

            List<BLangExpression> variables = new ArrayList<>(1);
            variables.add(lambdaFunction);

            BLangInvocation invocationExpr = ASTBuilderUtil.
                    createInvocationExprForMethod(streamingQueryStatement.pos, subscribeMethodSymbol, variables,
                            symResolver);

            BVarSymbol varSymbol = ((BVarSymbol) ((BLangSimpleVarRef) (streamingInput).getStreamReference()).symbol);
            invocationExpr.expr = ASTBuilderUtil.createVariableRef(streamingQueryStatement.pos, varSymbol);
            foreverReplaceStatement.expr = invocationExpr;
        }
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        BLangLambdaFunction lambdaFunction = (BLangLambdaFunction) streamAction.getInvokableBody();
        lambdaFunction.accept(this);
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        WhereNode beforeWhereNode = streamingInput.getBeforeStreamingCondition();
        WhereNode afterWhereNode = streamingInput.getAfterStreamingCondition();
        if (beforeWhereNode != null) {
            ((BLangWhere) beforeWhereNode).accept(this);
        }

        if (afterWhereNode != null) {
            ((BLangWhere) afterWhereNode).accept(this);
        }
    }

    @Override
    public void visit(BLangWhere where) {

        //Create IF Clause
        ifNode = ASTBuilderUtil.createIfStmt(where.pos, lambdaBody);
        final BLangBinaryExpr equality = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        equality.pos = where.pos;
        equality.type = symTable.booleanType;
        equality.opKind = ((BLangBinaryExpr) where.getExpression()).getOperatorKind();
        ((BLangBinaryExpr) where.getExpression()).getLeftExpression().accept(this);
        equality.lhsExpr = conditionalExpression;
        ((BLangBinaryExpr) where.getExpression()).getRightExpression().accept(this);
        equality.rhsExpr = conditionalExpression;
        equality.opSymbol = ((BLangBinaryExpr) where.getExpression()).opSymbol;
        ifNode.expr = equality;
        ifNode.body = ASTBuilderUtil.createBlockStmt(where.pos);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        final BLangBinaryExpr bLangBinaryExpr = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        bLangBinaryExpr.pos = binaryExpr.pos;
        bLangBinaryExpr.type = symTable.booleanType;
        bLangBinaryExpr.opKind = (binaryExpr).getOperatorKind();
        binaryExpr.getLeftExpression().accept(this);
        bLangBinaryExpr.lhsExpr = conditionalExpression;
        binaryExpr.getRightExpression().accept(this);
        bLangBinaryExpr.rhsExpr = conditionalExpression;
        bLangBinaryExpr.opSymbol = binaryExpr.opSymbol;
        conditionalExpression = bLangBinaryExpr;
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        BLangSimpleVarRef varRef = ASTBuilderUtil.createVariableRef(fieldAccessExpr.pos,
                (lambdaFunctionNode).requiredParams.get(0).symbol);
        conditionalExpression = ASTBuilderUtil.createFieldAccessExpr(varRef,
                fieldAccessExpr.field);
        ((BLangFieldBasedAccess) conditionalExpression).symbol = fieldAccessExpr.symbol;
        (conditionalExpression).type = fieldAccessExpr.type;
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        conditionalExpression = literalExpr;
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        for (int i = 0; i < bLangLambdaFunction.getFunctionNode().getBody().getStatements().size() - 1; i++) {
            StatementNode statementNode = bLangLambdaFunction.getFunctionNode().getBody().getStatements().get(i);

            if (windowInvokableSimpleVarRef != null) {
                BInvokableSymbol windowAdditionInvokableSymbol = null;
                List<BAttachedFunction> attachedFunctionsList = ((BObjectTypeSymbol) (
                        (windowInvokableSimpleVarRef).type).tsymbol).attachedFuncs;
                for (BAttachedFunction attachedFunction : attachedFunctionsList) {
                    if (attachedFunction.funcName.toString().equals("add")) {
                        windowAdditionInvokableSymbol = attachedFunction.symbol;
                    }
                }

                List<BLangExpression> variables = new ArrayList<>(1);
                BLangSimpleVarRef varRef = (BLangSimpleVarRef) ((BLangInvocation.BLangAttachedFunctionInvocation)
                        ((BLangExpressionStmt) statementNode).expr).requiredArgs.get(1);
                variables.add(varRef);
                BLangInvocation invocationExpr = ASTBuilderUtil.
                        createInvocationExprForMethod(bLangLambdaFunction.pos, windowAdditionInvokableSymbol, variables,
                                symResolver);

                BVarSymbol varSymbol = (BVarSymbol) (windowInvokableSimpleVarRef).symbol;
                invocationExpr.expr = ASTBuilderUtil.createVariableRef(bLangLambdaFunction.pos, varSymbol);

                final BLangExpressionStmt exprStmt = ASTBuilderUtil.
                        createExpressionStmt(bLangLambdaFunction.pos, ifNode.body);
                exprStmt.expr = invocationExpr;

                paramVarSymbol = varRef.varSymbol;
                closureVarSymbols.add((BVarSymbol) (windowInvokableSimpleVarRef).symbol);
                closureVarSymbols.add(paramVarSymbol);

            } else {
                final BLangExpressionStmt exprStmt = ASTBuilderUtil.
                        createExpressionStmt(bLangLambdaFunction.pos, ifNode.body);
                exprStmt.expr = ((BLangExpressionStmt) statementNode).expr;
            }
        }
    }

    public void visit(BLangWindow windowClause) {

        BLangLiteral windowSize = (BLangLiteral) ((BLangInvocation) windowClause.getFunctionInvocation()).
                argExprs.get(0);
        windowSize.type = symTable.intType;

        // Create variable for event type in window
        BTypeSymbol windowEventTypeSymbol = (BTypeSymbol) symResolver.resolvePkgSymbol((lambdaFunctionNode).pos,
                env, names.fromString("streams")).scope.lookup(new Name("EventType")).symbol;
        BType windowEventType = symResolver.resolvePkgSymbol((lambdaFunctionNode).pos, env, names.
                fromString("streams")).scope.lookup(new Name("EventType")).symbol.type;
        BVarSymbol windowEventTypeVarSymbol = new BVarSymbol(0, new Name("evType"),
                windowEventTypeSymbol.pkgID, windowEventType, env.scope.owner);

        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.setValue("ALL");
        literal.type = windowEventType;
        BLangVariable windowEventTypeVariable = ASTBuilderUtil.
                createVariable(windowClause.pos, "evType", windowEventType, literal, windowEventTypeVarSymbol);
        BLangVariableDef windowEventTypeVariableDef = ASTBuilderUtil.createVariableDef(windowClause.pos,
                windowEventTypeVariable);
        BLangSimpleVarRef windowEventTypeSimpleVarRef = ASTBuilderUtil.createVariableRef(windowClause.pos,
                windowEventTypeVarSymbol);
        stmts.add(windowEventTypeVariableDef);


        // Create variable for the window instance
        BInvokableSymbol windowInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol((lambdaFunctionNode).pos, env, names.fromString("streams")).
                scope.lookup(new Name("lengthWindow")).symbol;

        BType windowInvokableType = symResolver.resolvePkgSymbol((lambdaFunctionNode).pos, env, names.
                fromString("streams")).scope.lookup(new Name("lengthWindow")).symbol.type.getReturnType();


        BVarSymbol windowInvokableTypeVarSymbol = new BVarSymbol(0, new Name("lengthWindow"),
                windowEventTypeSymbol.pkgID, windowInvokableType, env.scope.owner);
        List<BLangExpression> args = new ArrayList<>();
        args.add(windowSize);
        args.add(windowEventTypeSimpleVarRef);

        BLangInvocation windowMethodInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(windowClause.pos, windowInvokableSymbol, args,
                        symResolver);
        windowMethodInvocation.argExprs = args;

        BLangVariable windowInvokableTypeVariable = ASTBuilderUtil.
                createVariable(windowClause.pos, "lengthWindow", windowInvokableType, windowMethodInvocation,
                        windowInvokableTypeVarSymbol);
        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.typeName = ASTBuilderUtil.createIdentifier(windowClause.pos, "LengthWindow");
        userDefinedType.type = windowInvokableType;
        windowInvokableTypeVariable.setTypeNode(userDefinedType);

        windowInvokableSimpleVarRef = ASTBuilderUtil.createVariableRef(windowClause.pos,
                windowInvokableTypeVarSymbol);

        BLangVariableDef windowInvokableTypeVariableDef = ASTBuilderUtil.createVariableDef(windowClause.pos,
                windowInvokableTypeVariable);
        stmts.add(windowInvokableTypeVariableDef);
    }


    //-------------------------------------- Private methods ----------------------------------------------

    private void generateStreamConsumerFunction(BLangForever foreverStatement) {

        // Here we generate an function and function call for the forever statement.
        // Here is an example query

        // -- Below is the sample Ballerina query
        // from employeeStream
        // where age > 30
        // select *
        // => (Teacher[] emp) {
        //      outputEmployeeStream.publish(emp);
        //}

        // Above Ballerina query is desugared to below statements

        // employeeStream.subscribe(processEmployees);

        // -- Desugared version
        // function processEmployees(Teacher e) {
        //
        //      if(e.age > 25) {
        //          outputEmployeeStream.publish(e);
        //      }
        //
        // }

        final DiagnosticPos pos = foreverStatement.pos;
        this.env = foreverStatement.getEnv();
        lambdaFunctionNode = ASTBuilderUtil.createFunction(pos, getFunctionName(FUNC_CALLER));

        lambdaFunctionNode.requiredParams.add((BLangVariable) foreverStatement.getStreamingQueryStatements().get(0).
                getStreamingAction().getInvokableBody().getFunctionNode().getParameters().get(0));
        lambdaFunctionNode.returnTypeNode = ASTBuilderUtil.createTypeNode(symTable.nilType);
        //lambdaFunctionNode.desugaredReturnType = true;
        //defineFunction(lambdaFunctionNode, foreverStatement.getEnv().enclPkg);
    }

    private String getFunctionName(String name) {
        return name + lambdaFunctionCount++;
    }

    private void defineFunction(BLangFunction funcNode, BLangPackage targetPkg) {
        final BPackageSymbol packageSymbol = targetPkg.symbol;
        final SymbolEnv packageEnv = this.symTable.pkgEnvMap.get(packageSymbol);
        symbolEnter.defineNode(funcNode, packageEnv);
        packageEnv.enclPkg.functions.add(funcNode);
        packageEnv.enclPkg.topLevelNodes.add(funcNode);
//        parentDesugar.visit(funcNode);
    }

    private void defineVariable(BLangVariable variable, PackageID pkgID, BLangFunction funcNode) {
        variable.symbol = new BVarSymbol(0, names.fromIdNode(variable.name), pkgID, variable.type, funcNode.symbol);
    }
}
