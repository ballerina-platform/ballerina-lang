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
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstructorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetryTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRollback;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.runtime.util.BLangConstants.UNDERSCORE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.wso2.ballerinalang.compiler.util.Names.CLEAN_UP_TRANSACTION;
import static org.wso2.ballerinalang.compiler.util.Names.CURRENT_TRANSACTION_INFO;
import static org.wso2.ballerinalang.compiler.util.Names.END_TRANSACTION;
import static org.wso2.ballerinalang.compiler.util.Names.GET_AND_CLEAR_FAILURE_TRANSACTION;
import static org.wso2.ballerinalang.compiler.util.Names.ROLLBACK_TRANSACTION;
import static org.wso2.ballerinalang.compiler.util.Names.START_TRANSACTION;
import static org.wso2.ballerinalang.compiler.util.Names.TRANSACTION_INFO_RECORD;

/**
 * Class responsible for desugar transaction statements into actual Ballerina code.
 *
 * @since 2.0.0-preview1
 */
public class TransactionDesugar extends BLangNodeVisitor {

    private static final CompilerContext.Key<TransactionDesugar> TRANSACTION_DESUGAR_KEY = new CompilerContext.Key<>();
    private static final String SHOULD_CLEANUP_SYMBOL = "$shouldCleanUp$";
    private final Desugar desugar;
    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final Names names;

    private BSymbol transactionError;
    private BLangExpression retryStmt;
    private SymbolEnv env;

    private BLangExpression transactionBlockID;
    private BLangExpression transactionID;
    private BLangSimpleVarRef prevAttemptInfoRef;
    private BLangSimpleVarRef shouldCleanUpVariableRef;

    private String uniqueId;
    private int transactionBlockCount;

    private BLangStatementExpression result;
    private boolean onFailHandled;
    private TransactionDesugar(CompilerContext context) {
        context.put(TRANSACTION_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.names = Names.getInstance(context);
        this.desugar = Desugar.getInstance(context);
    }

    public static TransactionDesugar getInstance(CompilerContext context) {
        TransactionDesugar desugar = context.get(TRANSACTION_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new TransactionDesugar(context);
        }
        return desugar;
    }

    public BLangStatementExpression rewrite (BLangNode node, SymbolEnv env, boolean nestedTrxOnFailHandled) {
        this.onFailHandled = nestedTrxOnFailHandled;
        String id = this.uniqueId;
        BLangExpression trxId = this.transactionID;
        BLangExpression trxBlockId = this.transactionBlockID;
        BLangSimpleVarRef attemptVarRef = this.prevAttemptInfoRef;
        BLangSimpleVarRef prevShouldCleanUp = shouldCleanUpVariableRef;
        BLangExpression retryStmt = this.retryStmt;
        BSymbol errorSymbol = this.transactionError;
        SymbolEnv symbolEnv =  this.env;
        this.env = env;
        node.accept(this);
        this.uniqueId = id;
        this.transactionID = trxId;
        this.transactionBlockID = trxBlockId;
        this.prevAttemptInfoRef = attemptVarRef;
        this.shouldCleanUpVariableRef = prevShouldCleanUp;
        this.retryStmt = retryStmt;
        this.transactionError = errorSymbol;
        this.env = symbolEnv;
        return result;
    }
    public void visit(BLangTransaction transactionNode) {
        // Transaction statement desugar implementation code.

        DiagnosticPos pos = transactionNode.pos;
        BLangBlockStmt transactionBlockStmt = desugarTransactionBody(transactionNode, env, false, pos);
        BLangSimpleVarRef resultRef = ASTBuilderUtil.createVariableRef(transactionNode.pos, transactionError);
        if (transactionNode.statementBlockReturns) {
            //  returns <TypeCast>$result$;
            BLangInvokableNode encInvokable = env.enclInvokable;
            result =  ASTBuilderUtil.createStatementExpression(transactionBlockStmt,
                    desugar.addConversionExprIfRequired(resultRef, encInvokable.returnTypeNode.type));
        } else {
            result = ASTBuilderUtil.createStatementExpression(transactionBlockStmt,
                    ASTBuilderUtil.createLiteral(transactionNode.pos, symTable.nilType, Names.NIL_VALUE));
        }
    }

    private BLangBlockStmt desugarTransactionBody(BLangTransaction transactionNode, SymbolEnv env, boolean shouldRetry,
                                                  DiagnosticPos pos) {
        BLangBlockStmt transactionBlockStmt = ASTBuilderUtil.createBlockStmt(pos);

        // Create transaction block ID variable
        // string transactionBlockId = transactionBlockId1;
        uniqueId = String.valueOf(++transactionBlockCount);
        BLangLiteral transactionBlockIDLiteral = ASTBuilderUtil.createLiteral(pos, symTable.stringType,
                uniqueId);
        BType transactionBlockIDType = symTable.stringType;
        BVarSymbol transactionBlockIDVarSymbol = new BVarSymbol(0, new Name("transactionBlockId" + uniqueId),
                env.scope.owner.pkgID, transactionBlockIDType, env.scope.owner, pos, VIRTUAL);
        BLangSimpleVariable transactionBlockIDVariable = ASTBuilderUtil.createVariable(pos, "transactionBlockId"
                        + uniqueId, transactionBlockIDType, transactionBlockIDLiteral, transactionBlockIDVarSymbol);
        transactionBlockIDVariable.symbol.closure = true;
        BLangSimpleVariableDef transactionBlockIDVariableDef = ASTBuilderUtil.createVariableDef(pos,
                transactionBlockIDVariable);
        transactionBlockStmt.stmts.add(transactionBlockIDVariableDef);

        //boolean $shouldCleanUp$ = false;
        BVarSymbol shouldCleanUpSymbol = new BVarSymbol(0, new Name(SHOULD_CLEANUP_SYMBOL + UNDERSCORE + uniqueId),
                env.scope.owner.pkgID, symTable.booleanType, env.scope.owner, pos, VIRTUAL);
        BLangSimpleVariable shouldCleanUpVariable =
                ASTBuilderUtil.createVariable(pos, SHOULD_CLEANUP_SYMBOL + UNDERSCORE + uniqueId, symTable.booleanType,
                        ASTBuilderUtil.createLiteral(pos, symTable.booleanType, false), shouldCleanUpSymbol);
        shouldCleanUpVariable.symbol.closure = true;
        BLangSimpleVariableDef shouldCleanUpVariableDef = ASTBuilderUtil.createVariableDef(pos,
                shouldCleanUpVariable);
        transactionBlockStmt.stmts.add(shouldCleanUpVariableDef);
        shouldCleanUpVariableRef = ASTBuilderUtil.createVariableRef(pos, shouldCleanUpVariable.symbol);

        //transactions:Info? prevAttempt = ();
        BLangSimpleVariableDef prevAttemptVarDef = createPrevAttemptInfoVarDef(env, pos);
        transactionBlockStmt.stmts.add(prevAttemptVarDef);
        this.prevAttemptInfoRef = ASTBuilderUtil.createVariableRef(pos, prevAttemptVarDef.var.symbol);
        this.transactionBlockID = ASTBuilderUtil.createVariableRef(pos, transactionBlockIDVariable.symbol);

        // Invoke startTransaction method and get a transaction id
        //string transactionId = "";
        BType transactionIDType = symTable.stringType;
        BVarSymbol transactionIDVarSymbol = new BVarSymbol(0, new Name("transactionId" + uniqueId),
                env.scope.owner.pkgID, transactionIDType, env.scope.owner, pos, VIRTUAL);
        BLangSimpleVariable transactionIDVariable = ASTBuilderUtil.createVariable(pos, "transactionId" + uniqueId,
                transactionIDType, ASTBuilderUtil.createLiteral(pos, symTable.stringType, ""), transactionIDVarSymbol);
        BLangSimpleVariableDef transactionIDVariableDef = ASTBuilderUtil.createVariableDef(pos,
                transactionIDVariable);
        transactionBlockStmt.stmts.add(transactionIDVariableDef);

        this.transactionID = ASTBuilderUtil.createVariableRef(pos, transactionIDVariable.symbol);
        transactionIDVariable.symbol.closure = true;

        env.scope.define(transactionBlockIDVarSymbol.name, transactionBlockIDVarSymbol);
        env.scope.define(transactionIDVarSymbol.name, transactionIDVarSymbol);
        env.scope.define(prevAttemptVarDef.var.symbol.name, prevAttemptVarDef.var.symbol);
        env.scope.define(shouldCleanUpVariable.symbol.name, shouldCleanUpVariable.symbol);

        // wraps content within transaction statement inside a lambda function
        //
        //  function (transactions:Info prevAttempt) returns any|error {
        //    <"Content in transaction block goes here">
        //  };
        BLangType transactionReturnType = ASTBuilderUtil.createTypeNode(symTable.anyOrErrorType);
        BLangSimpleVariable trxMainFuncParamPrevAttempt = createPrevAttemptVariable(env, pos);
        BLangLambdaFunction trxMainFunc = desugar.createLambdaFunction(transactionNode.pos, "$trxFunc$",
                Lists.of(trxMainFuncParamPrevAttempt), transactionReturnType, transactionNode.transactionBody.stmts,
                env, transactionNode.transactionBody.scope);
        ((BLangBlockFunctionBody) trxMainFunc.function.body).isBreakable = this.onFailHandled;

        // transactionId = startTransaction(1, prevAttempt)
        BLangInvocation startTransactionInvocation = createStartTransactionInvocation(pos, transactionBlockIDLiteral,
                ASTBuilderUtil.createVariableRef(pos, trxMainFuncParamPrevAttempt.symbol));
        BLangAssignment startTrxAssignment =
                ASTBuilderUtil.createAssignmentStmt(pos, ASTBuilderUtil.createVariableRef(pos, transactionIDVarSymbol),
                startTransactionInvocation);

        //prevAttempt = info();
        BLangAssignment infoAssignment = createPrevAttemptInfoInvocation(pos);
        ((BLangBlockFunctionBody) trxMainFunc.function.body).stmts.add(0, startTrxAssignment);
        ((BLangBlockFunctionBody) trxMainFunc.function.body).stmts.add(1, infoAssignment);

        //  var $trxFunc$ = function (transactions:Info prevAttempt) returns any|error {
        //    <"Content in transaction block goes here">
        //  };
        BVarSymbol transactionVarSymbol = new BVarSymbol(0, names.fromString("$trxFunc$"),
                env.scope.owner.pkgID, trxMainFunc.type, trxMainFunc.function.symbol, pos, VIRTUAL);
        BLangSimpleVariable transactionLambdaVariable = ASTBuilderUtil.createVariable(pos, "trxFunc",
                trxMainFunc.type, trxMainFunc, transactionVarSymbol);
        BLangSimpleVariableDef transactionLambdaVariableDef = ASTBuilderUtil.createVariableDef(pos,
                transactionLambdaVariable);
        BLangSimpleVarRef transactionLambdaVarRef = new BLangSimpleVarRef.
                BLangLocalVarRef(transactionLambdaVariable.symbol);
        transactionLambdaVarRef.type = transactionLambdaVariable.symbol.type;
        transactionBlockStmt.stmts.add(transactionLambdaVariableDef);

        // Add lambda function call
        // (any|error) result = $trxFunc$(prevAttempt);
        BLangInvocation transactionLambdaInvocation = new BLangInvocation.BFunctionPointerInvocation(pos,
                transactionLambdaVarRef, transactionLambdaVariable.symbol, symTable.anyOrErrorType);
        transactionLambdaInvocation.argExprs = Lists.of(desugar.rewrite(prevAttemptInfoRef, env));
        transactionLambdaInvocation.requiredArgs = transactionLambdaInvocation.argExprs;

        trxMainFunc.capturedClosureEnv = env;

        BVarSymbol resultSymbol = new BVarSymbol(0, new Name("result" + uniqueId),
                env.scope.owner.pkgID, symTable.anyOrErrorType, env.scope.owner, pos, VIRTUAL);
        BLangSimpleVariable resultVariable = ASTBuilderUtil.createVariable(pos, "result" + uniqueId,
                symTable.anyOrErrorType, transactionLambdaInvocation, resultSymbol);
        BLangSimpleVariableDef trxFuncVarDef = ASTBuilderUtil.createVariableDef(pos,
                resultVariable);
        transactionError = resultSymbol;
        if (shouldRetry) {
            retryStmt = transactionLambdaInvocation;
        }

        transactionBlockStmt.stmts.add(trxFuncVarDef);

        // if ($shouldCleanUp$) {
        //      cleanupTransactionContext();
        // }
        BLangIf cleanValidationIf = ASTBuilderUtil.createIfStmt(pos, transactionBlockStmt);
        BLangGroupExpr cleanValidationGroupExpr = new BLangGroupExpr();
        BLangSimpleVarRef shouldCleanUpRef = ASTBuilderUtil.createVariableRef(pos, shouldCleanUpVariable.symbol);
        cleanValidationGroupExpr.expression = shouldCleanUpRef;
        cleanValidationIf.expr = cleanValidationGroupExpr;
        cleanValidationIf.body = ASTBuilderUtil.createBlockStmt(pos);
        BLangExpressionStmt stmt = ASTBuilderUtil.createExpressionStmt(pos, cleanValidationIf.body);
        stmt.expr = createCleanupTrxStmt(pos);

        //if ((result is error) && !(result is TransactionError)) {
        //   rollbackTransaction(transactionBlockId1, <error?> result1);
        // }
        createRollbackIfFailed(transactionNode.pos, transactionBlockStmt, resultSymbol);
        return transactionBlockStmt;
    }

    private BLangAssignment createPrevAttemptInfoInvocation(DiagnosticPos pos) {
        BInvokableSymbol transactionInfoInvokableSymbol =
                (BInvokableSymbol) getTransactionLibInvokableSymbol(CURRENT_TRANSACTION_INFO);
        BLangInvocation infoInvocation =
                ASTBuilderUtil.createInvocationExprForMethod(pos, transactionInfoInvokableSymbol,
                        new ArrayList<>(), symResolver);
        infoInvocation.argExprs = infoInvocation.requiredArgs;
        return ASTBuilderUtil.createAssignmentStmt(pos, prevAttemptInfoRef, infoInvocation);
    }

    private BLangInvocation createStartTransactionInvocation(DiagnosticPos pos,
            BLangLiteral transactionBlockIDLiteral, BLangSimpleVarRef prevAttempt) {
        BInvokableSymbol startTransactionInvokableSymbol =
                (BInvokableSymbol) getTransactionLibInvokableSymbol(START_TRANSACTION);
        List<BLangExpression> args = new ArrayList<>();
        args.add(transactionBlockIDLiteral);
        args.add(prevAttempt);
        BLangInvocation startTransactionInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, startTransactionInvokableSymbol, args, symResolver);
        startTransactionInvocation.argExprs = args;
        return startTransactionInvocation;
    }

    private BLangSimpleVariableDef createPrevAttemptInfoVarDef(SymbolEnv env, DiagnosticPos pos) {
        BLangLiteral nilLiteral = ASTBuilderUtil.createLiteral(pos, symTable.nilType, Names.NIL_VALUE);
        BLangSimpleVariable prevAttemptVariable = createPrevAttemptVariable(env, pos);
        prevAttemptVariable.expr = nilLiteral;
        return ASTBuilderUtil.createVariableDef(pos, prevAttemptVariable);
    }

    private BLangSimpleVariable createPrevAttemptVariable(SymbolEnv env, DiagnosticPos pos) {
        // transactions:Info? prevAttempt = ();
        BSymbol infoRecordSymbol = symResolver.
                lookupSymbolInMainSpace(symTable.pkgEnvMap.get(symTable.langTransactionModuleSymbol),
                TRANSACTION_INFO_RECORD);
        BType infoRecordType = BUnionType.create(null, infoRecordSymbol.type, symTable.nilType);
        BVarSymbol prevAttemptVarSymbol = new BVarSymbol(0, new Name("prevAttempt" + uniqueId),
                env.scope.owner.pkgID, infoRecordType, env.scope.owner, pos, VIRTUAL);
        prevAttemptVarSymbol.closure = true;
        return ASTBuilderUtil.createVariable(pos, "prevAttempt" + uniqueId, infoRecordType, null,
                prevAttemptVarSymbol);
    }

    //  commit or rollback was not executed and fail(e) or panic(e) returned, so rollback
    //    if ((result is error) && !(result is TransactionError)) {
    //        rollback result;
    //    }
    private void createRollbackIfFailed(DiagnosticPos pos, BLangBlockStmt transactionBlockStmt,
                                        BSymbol trxFuncResultSymbol) {
        BLangIf rollbackCheck = ASTBuilderUtil.createIfStmt(pos, transactionBlockStmt);
        BConstructorSymbol transactionErrorSymbol = (BConstructorSymbol) symTable.langTransactionModuleSymbol
                .scope.lookup(names.fromString("TransactionError")).symbol;
        BType errorType = transactionErrorSymbol.type;
        BLangErrorType trxErrorTypeNode = (BLangErrorType) TreeBuilder.createErrorTypeNode();
        trxErrorTypeNode.type = errorType;
        BLangSimpleVarRef result = ASTBuilderUtil.createVariableRef(pos, trxFuncResultSymbol);
        BLangTypeTestExpr testExpr = ASTBuilderUtil.createTypeTestExpr(pos, result, trxErrorTypeNode);
        testExpr.type = symTable.booleanType;
        BLangGroupExpr transactionErrorCheckGroupExpr = new BLangGroupExpr();
        transactionErrorCheckGroupExpr.type = symTable.booleanType;
        BOperatorSymbol notOperatorSymbol = new BOperatorSymbol(names.fromString(OperatorKind.NOT.value()),
                                                                symTable.rootPkgSymbol.pkgID, errorType,
                                                                symTable.rootPkgSymbol, symTable.builtinPos, VIRTUAL);
        transactionErrorCheckGroupExpr.expression = ASTBuilderUtil.createUnaryExpr(pos, testExpr,
                symTable.booleanType, OperatorKind.NOT, notOperatorSymbol);

        BLangTypeTestExpr errorCheck = desugar.createTypeCheckExpr(pos, result, desugar.getErrorTypeNode());
        rollbackCheck.expr = ASTBuilderUtil.createBinaryExpr(pos, errorCheck, transactionErrorCheckGroupExpr,
                symTable.booleanType, OperatorKind.AND, null);
        BLangRollback rollbackStmt = (BLangRollback) TreeBuilder.createRollbackNode();
        rollbackStmt.expr = desugar.addConversionExprIfRequired(result, symTable.errorOrNilType);
        rollbackCheck.body = ASTBuilderUtil.createBlockStmt(pos);
        rollbackCheck.body.stmts.add(desugar.rewrite(rollbackStmt, env));
    }

    private BLangInvocation createCleanupTrxStmt(DiagnosticPos pos) {
        List<BLangExpression> args;
        BInvokableSymbol cleanupTrxInvokableSymbol =
                (BInvokableSymbol) getTransactionLibInvokableSymbol(CLEAN_UP_TRANSACTION);
        args = new ArrayList<>();
        args.add(transactionBlockID);
        BLangInvocation cleanupTrxInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, cleanupTrxInvokableSymbol, args, symResolver);
        cleanupTrxInvocation.argExprs = args;
        return cleanupTrxInvocation;
    }

    BLangStatementExpression desugar(BLangRollback rollbackNode) {
        // Rollback desugar implementation
        DiagnosticPos pos = rollbackNode.pos;
        BLangBlockStmt rollbackBlockStmt = ASTBuilderUtil.createBlockStmt(pos);

        // rollbackTransaction(transactionBlockID);
        BInvokableSymbol rollbackTransactionInvokableSymbol =
                (BInvokableSymbol) getTransactionLibInvokableSymbol(ROLLBACK_TRANSACTION);
        List<BLangExpression> args = new ArrayList<>();
        args.add(transactionBlockID);
        if (rollbackNode.expr != null) {
            args.add(rollbackNode.expr);
        }
        BLangInvocation rollbackTransactionInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, rollbackTransactionInvokableSymbol, args, symResolver);
        rollbackTransactionInvocation.argExprs = args;
        BLangExpressionStmt rollbackStmt = ASTBuilderUtil.createExpressionStmt(pos, rollbackBlockStmt);
        rollbackStmt.expr = rollbackTransactionInvocation;
        BLangStatement shouldCleanUpStmt = ASTBuilderUtil.createAssignmentStmt(pos, shouldCleanUpVariableRef,
                ASTBuilderUtil.createLiteral(pos, symTable.booleanType, true));
        rollbackBlockStmt.addStatement(shouldCleanUpStmt);
        BLangStatementExpression rollbackStmtExpr = ASTBuilderUtil.createStatementExpression(rollbackBlockStmt,
                ASTBuilderUtil.createLiteral(pos, symTable.nilType, Names.NIL_VALUE));
        rollbackStmtExpr.type = symTable.nilType;

        //at this point,
        //
        // rollbackTransaction(transactionBlockID);
        // $shouldCleanUp$ = true;
        return rollbackStmtExpr;
    }

    BLangStatementExpression desugar(BLangCommitExpr commitExpr, SymbolEnv env) {
        DiagnosticPos pos = commitExpr.pos;
        BLangBlockStmt commitBlockStatement = ASTBuilderUtil.createBlockStmt(pos);

        // Create temp output variable
        // error? $outputVar$ = ();
        BLangSimpleVariableDef outputVariableDef = createCommitResultVarDef(env, pos);
        BLangSimpleVarRef outputVarRef = ASTBuilderUtil.createVariableRef(pos, outputVariableDef.var.symbol);
        commitBlockStatement.addStatement(outputVariableDef);

        // Clear failures
        // boolean isFailed = getAndClearFailure();
        BInvokableSymbol transactionCleanerInvokableSymbol =
                (BInvokableSymbol) getTransactionLibInvokableSymbol(GET_AND_CLEAR_FAILURE_TRANSACTION);
        BLangInvocation transactionCleanerInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, transactionCleanerInvokableSymbol, new ArrayList<>(), symResolver);
        transactionCleanerInvocation.argExprs = new ArrayList<>();

        BVarSymbol isTransactionFailedVarSymbol = new BVarSymbol(0, new Name("isFailed"),
                env.scope.owner.pkgID, symTable.booleanType, env.scope.owner, pos, VIRTUAL);
        BLangSimpleVariable isTransactionFailedVariable = ASTBuilderUtil.createVariable(pos, "isFailed",
                symTable.booleanType, transactionCleanerInvocation, isTransactionFailedVarSymbol);
        BLangSimpleVariableDef isTransactionFailedVariableDef = ASTBuilderUtil.createVariableDef(pos,
                isTransactionFailedVariable);
        commitBlockStatement.addStatement(isTransactionFailedVariableDef);

        BLangBlockStmt failureHandlerBlockStatement = ASTBuilderUtil.createBlockStmt(pos);

        // Commit expr desugar implementation
        //string|error commitResult = endTransaction(transactionID, transactionBlockID);
        BInvokableSymbol commitTransactionInvokableSymbol =
                (BInvokableSymbol) getTransactionLibInvokableSymbol(END_TRANSACTION);
        List<BLangExpression> args = new ArrayList<>();
        args.add(transactionID);
        args.add(transactionBlockID);
        BLangInvocation commitTransactionInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, commitTransactionInvokableSymbol, args, symResolver);
        commitTransactionInvocation.argExprs = args;

        BType commitReturnType = BUnionType.create(null, symTable.stringType, symTable.errorType);

        BVarSymbol commitTransactionVarSymbol = new BVarSymbol(0, new Name("commitResult"),
                env.scope.owner.pkgID, commitReturnType, env.scope.owner, pos, VIRTUAL);
        BLangSimpleVariable commitResultVariable = ASTBuilderUtil.createVariable(pos, "commitResult",
                commitReturnType, commitTransactionInvocation, commitTransactionVarSymbol);
        BLangSimpleVariableDef commitResultVariableDef = ASTBuilderUtil.createVariableDef(pos, commitResultVariable);
        BLangSimpleVarRef commitResultVarRef = ASTBuilderUtil.createVariableRef(pos, commitResultVariable.symbol);
        failureHandlerBlockStatement.addStatement(commitResultVariableDef);

        // Successful commit operation
        // if(commitResult is string) {
        //      $shouldCleanUp$ = true;
        // } else {
        //      $outputVar$ = commitResult;
        // }
        BLangIf commitResultValidationIf = ASTBuilderUtil.createIfStmt(pos, failureHandlerBlockStatement);
        BLangGroupExpr commitResultValidationGroupExpr = new BLangGroupExpr();
        commitResultValidationGroupExpr.type = symTable.booleanType;
        BLangValueType stringType = (BLangValueType) TreeBuilder.createValueTypeNode();
        stringType.type = symTable.stringType;
        stringType.typeKind = TypeKind.STRING;
        commitResultValidationGroupExpr.expression = ASTBuilderUtil.createTypeTestExpr(pos, commitResultVarRef,
                stringType);
        commitResultValidationIf.expr = commitResultValidationGroupExpr;
        commitResultValidationIf.body = ASTBuilderUtil.createBlockStmt(pos);
        BLangStatement shouldCleanUpStmt = ASTBuilderUtil.createAssignmentStmt(pos, shouldCleanUpVariableRef,
                ASTBuilderUtil.createLiteral(pos, symTable.booleanType, true));
        commitResultValidationIf.body.addStatement(shouldCleanUpStmt);
        commitResultValidationIf.elseStmt = ASTBuilderUtil.createAssignmentStmt(pos, outputVarRef, commitResultVarRef);

        // Create failure validation
        //if(!isFailed) {
        //   string|error commitResult = endTransaction(transactionID, transactionBlockID);
        //   if(commitResult is string) {
        //      $shouldCleanUp$ = true;
        //   } else {
        //      $outputVar$ = commitResult;
        //   }
        //}
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
                names.fromString(OperatorKind.NOT.value()), symTable.rootPkgSymbol.pkgID, type, symTable.rootPkgSymbol,
                symTable.builtinPos, VIRTUAL);
        failureValidationGroupExpr.expression = ASTBuilderUtil.createUnaryExpr(pos, failureValidationExprVarRef,
                symTable.booleanType, OperatorKind.NOT, notOperatorSymbol);
        failureValidationIf.expr = failureValidationGroupExpr;
        failureValidationIf.body = failureHandlerBlockStatement;

        // at this point;
        //
        // error? $outputVar$ = ();
        // boolean isFailed = getAndClearFailure();
        // if(!isFailed) {
        //   string|error commitResult = endTransaction(transactionID, transactionBlockID);
        //   if(commitResult is string) {
        //      $shouldCleanUp$ = true;
        //   } else {
        //      $outputVar$ = commitResult;
        //   }
        // }
        BLangStatementExpression stmtExpr = ASTBuilderUtil.createStatementExpression(commitBlockStatement,
                outputVarRef);
        stmtExpr.type = symTable.errorOrNilType;
        return stmtExpr;
    }

    private BLangSimpleVariableDef createCommitResultVarDef(SymbolEnv env, DiagnosticPos pos) {
        BLangExpression nilLiteral = ASTBuilderUtil.createLiteral(pos, symTable.nilType, Names.NIL_VALUE);
        BVarSymbol outputVarSymbol = new BVarSymbol(0, new Name("$outputVar$"),
                env.scope.owner.pkgID, symTable.errorOrNilType, env.scope.owner, pos, VIRTUAL);
        BLangSimpleVariable outputVariable =
                ASTBuilderUtil.createVariable(pos, "$outputVar$", symTable.errorOrNilType,
                        nilLiteral, outputVarSymbol);
        return ASTBuilderUtil.createVariableDef(pos, outputVariable);
    }

    public void visit(BLangRetryTransaction retryTrxBlock) {
        BLangBlockStmt blockStmt = desugarTransactionBody(retryTrxBlock.transaction, env, true, retryTrxBlock.pos);
        BLangSimpleVarRef resultRef = ASTBuilderUtil.createVariableRef(retryTrxBlock.pos, transactionError);
        BLangSimpleVariableDef retryMgrDef = desugar.createRetryManagerDef(retryTrxBlock.retrySpec, retryTrxBlock.pos);
        BLangTypeTestExpr isErrorCheck = desugar.createTypeCheckExpr(retryTrxBlock.pos, resultRef,
                desugar.getErrorTypeNode());
        BLangIf ifStmt = ASTBuilderUtil.createIfStmt(retryTrxBlock.pos, blockStmt);
        ifStmt.expr = isErrorCheck;
        ifStmt.body = ASTBuilderUtil.createBlockStmt(retryTrxBlock.pos);
        ifStmt.body.stmts.add(retryMgrDef);
        BLangWhile retryWhileLoop = desugar.createRetryWhileLoop(retryTrxBlock.pos, retryMgrDef, retryStmt,
                resultRef);
        createRollbackIfFailed(retryTrxBlock.pos, retryWhileLoop.body, resultRef.symbol);
        ifStmt.body.stmts.add(retryWhileLoop);
        desugar.createErrorReturn(retryTrxBlock.pos, blockStmt, resultRef);

        if (retryTrxBlock.transaction.statementBlockReturns) {
            //  returns <TypeCast>$result$;
            BLangInvokableNode encInvokable = env.enclInvokable;
            result = ASTBuilderUtil.createStatementExpression(blockStmt,
                    desugar.addConversionExprIfRequired(resultRef, encInvokable.returnTypeNode.type));
        } else {
            result = ASTBuilderUtil.createStatementExpression(blockStmt,
                    ASTBuilderUtil.createLiteral(retryTrxBlock.pos, symTable.nilType, Names.NIL_VALUE));
        }
    }

    /**
     * Load and return symbol for given name in transaction lib.
     *
     * @param name of the symbol.
     * @return symbol for the function.
     */
    public BSymbol getTransactionLibInvokableSymbol(Name name) {
        return symTable.langTransactionModuleSymbol.scope.lookup(name).symbol;
    }
}
