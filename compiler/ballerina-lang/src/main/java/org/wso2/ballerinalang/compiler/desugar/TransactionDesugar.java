/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCommitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRollback;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for desugar transaction statements into actual Ballerina code.
 *
 * @since 2.0.0-preview1
 */
public class TransactionDesugar extends BLangNodeVisitor {

    private static final CompilerContext.Key<TransactionDesugar> TRANSACTION_DESUGAR_KEY = new CompilerContext.Key<>();
    private final Desugar desugar;
    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final Names names;
    private final Types types;
    private SymbolEnv env;
    private CompilerContext context;

    private BLangExpression transactionBlockID;
    private BLangExpression transactionID;

    private TransactionDesugar(CompilerContext context) {
        context.put(TRANSACTION_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.names = Names.getInstance(context);
        this.types = Types.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.context = context;
    }

    public static TransactionDesugar getInstance(CompilerContext context) {
        TransactionDesugar desugar = context.get(TRANSACTION_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new TransactionDesugar(context);
        }
        return desugar;
    }

    BLangBlockStmt desugar(BLangTransaction transactionNode, SymbolEnv env) {
        // Transaction statement desugar implementation code.

        DiagnosticPos pos = transactionNode.pos;
        BLangBlockStmt transactionBlockStmt = ASTBuilderUtil.createBlockStmt(pos);

        // Create transaction block ID variable
        BLangLiteral transactionBlockIDLiteral = ASTBuilderUtil.createLiteral(pos, symTable.stringType,
                desugar.getTransactionBlockId());
        BType transactionBlockIDType = symTable.stringType;
        BVarSymbol transactionBlockIDVarSymbol = new BVarSymbol(0, new Name("transactionBlockId"),
                env.scope.owner.pkgID, transactionBlockIDType, env.scope.owner);
        BLangSimpleVariable transactionBlockIDVariable = ASTBuilderUtil.createVariable(pos, "transactionBlockId",
                transactionBlockIDType, transactionBlockIDLiteral, transactionBlockIDVarSymbol);
        BLangSimpleVariableDef transactionBlockIDVariableDef = ASTBuilderUtil.createVariableDef(pos,
                transactionBlockIDVariable);
        transactionBlockStmt.stmts.add(transactionBlockIDVariableDef);
        this.transactionBlockID = transactionBlockIDVariable.expr;
        transactionBlockIDVariable.symbol.closure = true;

        // Invoke startTransaction method and get a transaction id
        BInvokableSymbol startTransactionInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(pos, env, Names.TRANSACTION_MODULE).
                scope.lookup(Names.START_TRANSACTION).symbol;
        List<BLangExpression> args = new ArrayList<>();
        args.add(transactionBlockIDLiteral);
        BLangInvocation startTransactionInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, startTransactionInvokableSymbol, args, symResolver);
        startTransactionInvocation.argExprs = args;
        BType transactionIDType = symTable.stringType;
        BVarSymbol transactionIDVarSymbol = new BVarSymbol(0, new Name("transactionId"),
                env.scope.owner.pkgID, transactionIDType, env.scope.owner);
        BLangSimpleVariable transactionIDVariable = ASTBuilderUtil.createVariable(pos, "transactionId",
                transactionIDType, startTransactionInvocation, transactionIDVarSymbol);
        BLangSimpleVariableDef transactionIDVariableDef = ASTBuilderUtil.createVariableDef(pos,
                transactionIDVariable);
        transactionBlockStmt.stmts.add(transactionIDVariableDef);
        this.transactionID = transactionIDVariable.expr;
        transactionIDVariable.symbol.closure = true;

        // Create transaction lambda function
        BLangType transactionLambdaReturnType = ASTBuilderUtil.createTypeNode(symTable.errorOrNilType);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();
        body.stmts = transactionNode.transactionBody.stmts;

//        if (returnNode != null) {
//            // passthrough will return same frame parameter
//            if (isPassthrough) {
//                returnNode.setExpression(frameVarRef);
//            }
//            body.addStatement(returnNode);
//        }
        BLangLambdaFunction transactionLambdaFunction = desugar.createLambdaFunction(pos, "$anonTransactionFunc$",
                new ArrayList<>(), transactionLambdaReturnType, body);
        BVarSymbol transactionLambdaVarSymbol = new BVarSymbol(0, new Name("trxFunc"),
                env.scope.owner.pkgID, transactionLambdaFunction.type, env.scope.owner);
        BLangSimpleVariable transactionLambdaVariable = ASTBuilderUtil.createVariable(pos, "trxFunc",
                transactionLambdaFunction.type, transactionLambdaFunction, transactionLambdaVarSymbol);
        BLangSimpleVariableDef transactionLambdaVariableDef = ASTBuilderUtil.createVariableDef(pos,
                transactionLambdaVariable);
        transactionBlockStmt.stmts.add(transactionLambdaVariableDef);

        // Add lambda function call
        BLangInvocation transactionLambdaInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, transactionLambdaFunction.function.symbol,
                        new ArrayList<>(), symResolver);
        transactionLambdaInvocation.argExprs = new ArrayList<>();
        BLangTrapExpr transactionFunctionTrapExpression = (BLangTrapExpr) TreeBuilder.createTrapExpressionNode();
        transactionFunctionTrapExpression.type = BUnionType.create(null, symTable.errorType, symTable.nilType);
        transactionFunctionTrapExpression.expr = transactionLambdaInvocation;

        BVarSymbol transactionFunctionVarSymbol = new BVarSymbol(0, new Name("result"),
                env.scope.owner.pkgID, symTable.errorOrNilType, env.scope.owner);
        BLangSimpleVariable transactionFunctionVariable = ASTBuilderUtil.createVariable(pos, "result",
                symTable.errorOrNilType, transactionFunctionTrapExpression, transactionFunctionVarSymbol);
        BLangSimpleVariableDef transactionFunctionVariableDef = ASTBuilderUtil.createVariableDef(pos,
                transactionFunctionVariable);
        transactionBlockStmt.stmts.add(transactionFunctionVariableDef);

        ClosureExpressionVisitor closureExpressionVisitor = new ClosureExpressionVisitor(context, env, true);
        transactionLambdaFunction.accept(closureExpressionVisitor);

        return transactionBlockStmt;
    }

    BLangStatement desugar(BLangRollback rollbackNode, SymbolEnv env) {
        // Rollback desugar implementation
        DiagnosticPos pos = rollbackNode.pos;
        BInvokableSymbol rollbackTransactionInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(pos, env, Names.TRANSACTION_MODULE).
                scope.lookup(Names.ROLLBACK_TRANSACTION).symbol;
        List<BLangExpression> args = new ArrayList<>();
        args.add(transactionBlockID);
        BLangInvocation rollbackTransactionInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, rollbackTransactionInvokableSymbol, args, symResolver);
        rollbackTransactionInvocation.argExprs = args;

        BLangTrapExpr rollbackTrapExpression = (BLangTrapExpr) TreeBuilder.createTrapExpressionNode();
        rollbackTrapExpression.type = BUnionType.create(null, symTable.errorType, symTable.nilType);
        rollbackTrapExpression.expr = rollbackTransactionInvocation;

        BVarSymbol rollbackTransactionVarSymbol = new BVarSymbol(0, new Name("rollbackResult"),
                env.scope.owner.pkgID, symTable.errorOrNilType, env.scope.owner);
        BLangSimpleVariable rollbackResultVariable = ASTBuilderUtil.createVariable(pos, "rollbackResult",
                symTable.errorOrNilType, rollbackTrapExpression, rollbackTransactionVarSymbol);
        return ASTBuilderUtil.createVariableDef(pos,
                rollbackResultVariable);
    }

    BLangStatementExpression desugar(BLangCommitExpr commitExpr, SymbolEnv env) {
        DiagnosticPos pos = commitExpr.pos;
        BLangBlockStmt commitBlockStatement = ASTBuilderUtil.createBlockStmt(pos);

        // Create temp output variable
        BLangExpression nilExpression = ASTBuilderUtil.createLiteral(pos, symTable.nilType, Names.NIL_VALUE);
        BVarSymbol outputVarSymbol = new BVarSymbol(0, new Name("$outputVar$"),
                env.scope.owner.pkgID, symTable.errorOrNilType, env.scope.owner);
        BLangSimpleVariable outputVariable =
                ASTBuilderUtil.createVariable(pos, "$outputVar$", symTable.errorOrNilType,
                        nilExpression, outputVarSymbol);
        BLangSimpleVariableDef outputVariableDef =
                ASTBuilderUtil.createVariableDef(pos, outputVariable);
        BLangSimpleVarRef outputVarRef = ASTBuilderUtil.createVariableRef(pos, outputVariable.symbol);
        commitBlockStatement.addStatement(outputVariableDef);

        // Clear failures
        BInvokableSymbol transactionCleanerInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(pos, env, Names.TRANSACTION_MODULE).
                scope.lookup(Names.GET_AND_CLEAR_FAILURE_TRANSACTION).symbol;
        BLangInvocation transactionCleanerInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, transactionCleanerInvokableSymbol, new ArrayList<>(), symResolver);
        transactionCleanerInvocation.argExprs = new ArrayList<>();

        BVarSymbol isTransactionFailedVarSymbol = new BVarSymbol(0, new Name("isFailed"),
                env.scope.owner.pkgID, symTable.booleanType, env.scope.owner);
        BLangSimpleVariable isTransactionFailedVariable = ASTBuilderUtil.createVariable(pos, "isFailed",
                symTable.booleanType, transactionCleanerInvocation, isTransactionFailedVarSymbol);
        BLangSimpleVariableDef isTransactionFailedVariableDef = ASTBuilderUtil.createVariableDef(pos,
                isTransactionFailedVariable);
        commitBlockStatement.addStatement(isTransactionFailedVariableDef);

        BLangBlockStmt failureHandlerBlockStatement = ASTBuilderUtil.createBlockStmt(pos);

        // Commit expr desugar implementation
        BInvokableSymbol commitTransactionInvokableSymbol = (BInvokableSymbol) symResolver.
                resolvePkgSymbol(pos, env, Names.TRANSACTION_MODULE).
                scope.lookup(Names.END_TRANSACTION).symbol;
        List<BLangExpression> args = new ArrayList<>();
        args.add(transactionID);
        args.add(transactionBlockID);
        BLangInvocation commitTransactionInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, commitTransactionInvokableSymbol, args, symResolver);
        commitTransactionInvocation.argExprs = args;

        BLangTrapExpr commitTrapExpression = (BLangTrapExpr) TreeBuilder.createTrapExpressionNode();
        BType commitReturnType = BUnionType.create(null, symTable.stringType, symTable.errorType);
        commitTrapExpression.type = commitReturnType;
        commitTrapExpression.expr = commitTransactionInvocation;

        BVarSymbol commitTransactionVarSymbol = new BVarSymbol(0, new Name("commitResult"),
                env.scope.owner.pkgID, commitReturnType, env.scope.owner);
        BLangSimpleVariable commitResultVariable = ASTBuilderUtil.createVariable(pos, "commitResult",
                commitReturnType, commitTrapExpression, commitTransactionVarSymbol);
        BLangSimpleVariableDef commitResultVariableDef = ASTBuilderUtil.createVariableDef(pos,
                commitResultVariable);
        failureHandlerBlockStatement.addStatement(commitResultVariableDef);

        // Successful commit operation
        BLangIf commitResultValidationIf = ASTBuilderUtil.createIfStmt(pos, failureHandlerBlockStatement);
        BLangGroupExpr commitResultValidationGroupExpr = new BLangGroupExpr();
        commitResultValidationGroupExpr.type = symTable.booleanType;
        BLangValueType stringType = (BLangValueType) TreeBuilder.createValueTypeNode();
        stringType.type = symTable.stringType;
        stringType.typeKind = TypeKind.STRING;
        commitResultValidationGroupExpr.expression = ASTBuilderUtil.createTypeTestExpr(pos, commitResultVariable.expr,
                stringType);
        commitResultValidationIf.expr = commitResultValidationGroupExpr;
        commitResultValidationIf.body = ASTBuilderUtil.createBlockStmt(pos);
        // Need to set the leaf body for this. //TODO

        // Create failure validation
        BLangIf failureValidationIf = ASTBuilderUtil.createIfStmt(pos, commitBlockStatement);
        BLangGroupExpr failureValidationGroupExpr = new BLangGroupExpr();
        failureValidationGroupExpr.type = symTable.booleanType;
        BLangSimpleVarRef failureValidationExprVarRef = ASTBuilderUtil.createVariableRef(pos,
                isTransactionFailedVariable.symbol);
        List<BType> paramTypes = new ArrayList<>();
        paramTypes.add(symTable.booleanType);
        BInvokableType type = new BInvokableType(paramTypes, symTable.booleanType,
                null);
        BOperatorSymbol notOperatorSymbol = new BOperatorSymbol(
                names.fromString(OperatorKind.NOT.value()), symTable.rootPkgSymbol.pkgID, type, symTable.rootPkgSymbol);
        failureValidationGroupExpr.expression = ASTBuilderUtil.createUnaryExpr(pos, failureValidationExprVarRef,
                symTable.booleanType, OperatorKind.NOT, notOperatorSymbol);
        failureValidationIf.expr = failureValidationGroupExpr;
        failureValidationIf.body = failureHandlerBlockStatement;

        BLangStatementExpression stmtExpr = ASTBuilderUtil.createStatementExpression(commitBlockStatement,
                outputVarRef);
        stmtExpr.type = symTable.errorOrNilType;
        return stmtExpr;
    }
}
