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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.PackageCache;
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
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCommitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTransactionalExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangFail;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRollback;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.runtime.api.constants.RuntimeConstants.UNDERSCORE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createStatementExpression;
import static org.wso2.ballerinalang.compiler.util.Names.BEGIN_REMOTE_PARTICIPANT;
import static org.wso2.ballerinalang.compiler.util.Names.CLEAN_UP_TRANSACTION;
import static org.wso2.ballerinalang.compiler.util.Names.CURRENT_TRANSACTION_INFO;
import static org.wso2.ballerinalang.compiler.util.Names.END_TRANSACTION;
import static org.wso2.ballerinalang.compiler.util.Names.GET_AND_CLEAR_FAILURE_TRANSACTION;
import static org.wso2.ballerinalang.compiler.util.Names.ROLLBACK_TRANSACTION;
import static org.wso2.ballerinalang.compiler.util.Names.START_TRANSACTION;
import static org.wso2.ballerinalang.compiler.util.Names.START_TRANSACTION_COORDINATOR;
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
    private final PackageCache packageCache;

    private BSymbol transactionError;
    private BLangExpression retryStmt;
    private SymbolEnv env;
    private BLangBlockStmt result;
    private BLangSimpleVarRef prevAttemptInfoRef;
    private BLangSimpleVarRef shouldCleanUpVariableRef;
    private BLangExpression transactionID;
    private String uniqueId;
    private BLangLiteral trxBlockId;
    private boolean transactionInternalModuleIncluded = false;
    private int trxResourceCount;

    private TransactionDesugar(CompilerContext context) {
        context.put(TRANSACTION_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.names = Names.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.packageCache = PackageCache.getInstance(context);
    //    if (this.symTable.internalTransactionModuleSymbol == null) {
    //        this.symTable.internalTransactionModuleSymbol =
    //                pkgLoader.loadPackageSymbol(PackageID.TRANSACTION_INTERNAL, null, null);
    //    }
    }

    public static TransactionDesugar getInstance(CompilerContext context) {
        TransactionDesugar desugar = context.get(TRANSACTION_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new TransactionDesugar(context);
        }
        return desugar;
    }

    public BLangBlockStmt rewrite (BLangNode node, BLangLiteral trxBlockIdDef, SymbolEnv env,
                                   String uniqueId) {
        BLangLiteral currentTrxBlockIdDef = this.trxBlockId;
        this.trxBlockId = trxBlockIdDef;
        String id = this.uniqueId;
        this.uniqueId = uniqueId;
        BLangExpression trxId = this.transactionID;
        BLangSimpleVarRef attemptVarRef = this.prevAttemptInfoRef;
        BLangSimpleVarRef prevShouldCleanUp = shouldCleanUpVariableRef;
        SymbolEnv symbolEnv =  this.env;
        this.env = env;
        node.accept(this);
        this.uniqueId = id;
        this.transactionID = trxId;
        this.prevAttemptInfoRef = attemptVarRef;
        this.shouldCleanUpVariableRef = prevShouldCleanUp;
        this.env = symbolEnv;
        this.trxBlockId = currentTrxBlockIdDef;
        return result;
    }
    public void visit(BLangTransaction transactionNode) {
        result = desugarTransactionBody(transactionNode, env, transactionNode.pos);
    }

    // Transaction statement desugar implementation code.
    private BLangBlockStmt desugarTransactionBody(BLangTransaction transactionNode, SymbolEnv env, Location pos) {
        BLangBlockStmt transactionBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
        transactionBlockStmt.scope = transactionNode.transactionBody.scope;

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

        if (transactionNode.prevAttemptInfo == null) {
            //transactions:Info? prevAttempt = ();
            BLangSimpleVariableDef prevAttemptVarDef = createPrevAttemptInfoVarDef(env, pos);
            transactionBlockStmt.stmts.add(prevAttemptVarDef);
            transactionBlockStmt.scope.define(prevAttemptVarDef.var.symbol.name, prevAttemptVarDef.var.symbol);
            this.prevAttemptInfoRef = ASTBuilderUtil.createVariableRef(pos, prevAttemptVarDef.var.symbol);
        } else {
            this.prevAttemptInfoRef = (BLangSimpleVarRef) transactionNode.prevAttemptInfo;
        }

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

        transactionBlockStmt.scope.define(transactionIDVarSymbol.name, transactionIDVarSymbol);
        transactionBlockStmt.scope.define(shouldCleanUpVariable.symbol.name, shouldCleanUpVariable.symbol);

        BType transactionReturnType = symTable.errorOrNilType;

        // wraps content within transaction body inside a statement expression
        BLangLiteral nilLiteral = ASTBuilderUtil.createLiteral(pos, symTable.nilType, Names.NIL_VALUE);
        BLangStatementExpression statementExpression =
                createStatementExpression(transactionNode.transactionBody, nilLiteral);
        statementExpression.type = symTable.nilType;

        BLangTrapExpr trapExpr = (BLangTrapExpr) TreeBuilder.createTrapExpressionNode();
        trapExpr.type = transactionReturnType;
        trapExpr.expr = statementExpression;

        //error? $trapResult = trap <Transaction Body>
        BVarSymbol nillableErrorVarSymbol = new BVarSymbol(0, names.fromString("$trapResult"),
                this.env.scope.owner.pkgID, transactionReturnType,
                this.env.scope.owner, pos, VIRTUAL);
        BLangSimpleVariable trapResultVariable = ASTBuilderUtil.createVariable(pos, "$trapResult",
                transactionReturnType, trapExpr, nillableErrorVarSymbol);
        BLangSimpleVariableDef trapResultVariableDef = ASTBuilderUtil.createVariableDef(pos, trapResultVariable);
        transactionBlockStmt.addStatement(trapResultVariableDef);
        BLangSimpleVarRef trapResultRef = ASTBuilderUtil.createVariableRef(pos, nillableErrorVarSymbol);

        BLangFail failStmt = (BLangFail) TreeBuilder.createFailNode();
        failStmt.pos = pos;
        failStmt.expr = desugar.addConversionExprIfRequired(trapResultRef, symTable.errorType);

        BLangPanic panicNode = (BLangPanic) TreeBuilder.createPanicNode();
        panicNode.pos = pos;
        panicNode.expr = failStmt.expr;
        failStmt.exprStmt = panicNode;

        BLangBlockStmt ifErrorBlock = ASTBuilderUtil.createBlockStmt(pos);
        ifErrorBlock.addStatement(failStmt);

        BLangTypeTestExpr isErrorTest =
                ASTBuilderUtil.createTypeTestExpr(pos, trapResultRef, desugar.getErrorTypeNode());
        isErrorTest.type = symTable.booleanType;

        //if($trapResult$ is error) {
        //     fail $trapResult$;
        // }
        BLangIf ifTrapResIsError = ASTBuilderUtil.createIfElseStmt(pos, isErrorTest, ifErrorBlock, null);
        transactionBlockStmt.addStatement(ifTrapResIsError);

        // transactionId = startTransaction(1, prevAttempt)
        BLangInvocation startTransactionInvocation = createStartTransactionInvocation(pos,
                ASTBuilderUtil.createLiteral(pos, symTable.stringType, uniqueId), prevAttemptInfoRef);
        BLangAssignment startTrxAssignment =
                ASTBuilderUtil.createAssignmentStmt(pos, ASTBuilderUtil.createVariableRef(pos, transactionIDVarSymbol),
                        startTransactionInvocation);

        //prevAttempt = info();
        BLangAssignment infoAssignment = createPrevAttemptInfoInvocation(pos);
        transactionNode.transactionBody.stmts.add(0, startTrxAssignment);
        transactionNode.transactionBody.stmts.add(1, infoAssignment);

        // if ($shouldCleanUp$) {
        //      cleanupTransactionContext();
        // }
        BLangIf cleanValidationIf = ASTBuilderUtil.createIfStmt(pos, transactionBlockStmt);
        BLangGroupExpr cleanValidationGroupExpr = new BLangGroupExpr();
        cleanValidationGroupExpr.expression = ASTBuilderUtil.createVariableRef(pos, shouldCleanUpVariable.symbol);
        cleanValidationIf.expr = cleanValidationGroupExpr;
        cleanValidationIf.body = ASTBuilderUtil.createBlockStmt(pos);
        BLangExpressionStmt stmt = ASTBuilderUtil.createExpressionStmt(pos, cleanValidationIf.body);
        stmt.expr = createCleanupTrxStmt(pos, this.trxBlockId);

        // at this point ;
        // boolean $shouldCleanUp$ = false;
        // transactions:Info? prevAttempt = ();
        // string transactionId = "";
        // error? $trapResult = trap {
        //                              transactionId = startTransaction(1, prevAttempt)
        //                              prevAttempt = info();
        //
        //                              <Transaction Body>
        //                            }
        // if($trapResult$ is error) {
        //     panic $trapResult$;
        // }
        // if ($shouldCleanUp$) {
        //      cleanupTransactionContext(1);
        // }
        return desugar.rewrite(transactionBlockStmt, env);
    }

    private BLangAssignment createPrevAttemptInfoInvocation(Location pos) {
        BInvokableSymbol transactionInfoInvokableSymbol =
                (BInvokableSymbol) getTransactionLibInvokableSymbol(CURRENT_TRANSACTION_INFO);
        BLangInvocation infoInvocation =
                ASTBuilderUtil.createInvocationExprForMethod(pos, transactionInfoInvokableSymbol,
                        new ArrayList<>(), symResolver);
        infoInvocation.argExprs = infoInvocation.requiredArgs;
        return ASTBuilderUtil.createAssignmentStmt(pos, prevAttemptInfoRef, infoInvocation);
    }

    private BLangInvocation createStartTransactionInvocation(Location location,
                                                             BLangLiteral transactionBlockIDLiteral,
                                                             BLangSimpleVarRef prevAttempt) {
        BInvokableSymbol startTransactionInvokableSymbol =
                (BInvokableSymbol) getInternalTransactionModuleInvokableSymbol(START_TRANSACTION);

        // Include transaction-internal module as an import if not included
        if (!transactionInternalModuleIncluded) {
            desugar.addTransactionInternalModuleImport();
            transactionInternalModuleIncluded = true;
        }

        List<BLangExpression> args = new ArrayList<>();
        args.add(transactionBlockIDLiteral);
        args.add(prevAttempt);
        BLangInvocation startTransactionInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(location, startTransactionInvokableSymbol, args, symResolver);
        startTransactionInvocation.argExprs = args;
        return startTransactionInvocation;
    }

    public BLangInvocation createBeginParticipantInvocation(Location pos) {
        BInvokableSymbol beginParticipantInvokableSymbol =
                (BInvokableSymbol) getInternalTransactionModuleInvokableSymbol(BEGIN_REMOTE_PARTICIPANT);

        // Include transaction-internal module as an import if not included
        if (!transactionInternalModuleIncluded) {
            desugar.addTransactionInternalModuleImport();
            transactionInternalModuleIncluded = true;
        }

        List<BLangExpression> args = new ArrayList<>();
        args.add(ASTBuilderUtil.createLiteral(pos, symTable.stringType, String.valueOf(++trxResourceCount)));
        BLangInvocation startTransactionInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, beginParticipantInvokableSymbol, args, symResolver);
        startTransactionInvocation.argExprs = args;
        return startTransactionInvocation;
    }

    public BLangInvocation createStartTransactionCoordinatorInvocation(Location pos) {
        BInvokableSymbol startTransactionInvokableSymbol =
                (BInvokableSymbol) getInternalTransactionModuleInvokableSymbol(START_TRANSACTION_COORDINATOR);

        // Include transaction-internal module as an import if not included
        if (!transactionInternalModuleIncluded) {
            desugar.addTransactionInternalModuleImport();
            transactionInternalModuleIncluded = true;
        }

        List<BLangExpression> args = new ArrayList<>();
        BLangInvocation startTransactionCoordinatorInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, startTransactionInvokableSymbol, args, symResolver);
        startTransactionCoordinatorInvocation.argExprs = args;
        return startTransactionCoordinatorInvocation;
    }

    BLangSimpleVariableDef createPrevAttemptInfoVarDef(SymbolEnv env, Location pos) {
        BLangLiteral nilLiteral = ASTBuilderUtil.createLiteral(pos, symTable.nilType, Names.NIL_VALUE);
        BLangSimpleVariable prevAttemptVariable = createPrevAttemptVariable(env, pos);
        prevAttemptVariable.expr = nilLiteral;
        return ASTBuilderUtil.createVariableDef(pos, prevAttemptVariable);
    }

    private BLangSimpleVariable createPrevAttemptVariable(SymbolEnv env, Location pos) {
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

    BLangBlockStmt desugar(BLangRollback rollbackNode, BLangLiteral transactionBlockID) {
        // Rollback desugar implementation
        BLangBlockStmt rollbackBlockStmt = ASTBuilderUtil.createBlockStmt(rollbackNode.pos);
        BLangStatementExpression rollbackExpr = invokeRollbackFunc(rollbackNode.pos, rollbackNode.expr,
                transactionBlockID);
        BLangExpressionStmt rollbackStmt = ASTBuilderUtil.createExpressionStmt(rollbackNode.pos, rollbackBlockStmt);
        rollbackStmt.expr = rollbackExpr;
        return rollbackBlockStmt;
    }

    //  commit or rollback was not executed and fail(e) or panic(e) returned, so rollback
    //    if (($trxError$ is error) && !($trxError$ is TransactionError) && transactional) {
    //        $shouldCleanUp$ = true;
    //        check panic rollback $trxError$;
    //    }
    void createRollbackIfFailed(Location pos, BLangBlockStmt onFailBodyBlock,
                                BSymbol trxFuncResultSymbol, BLangLiteral trxBlockId) {
        BLangIf rollbackCheck = (BLangIf) TreeBuilder.createIfElseStatementNode();
        rollbackCheck.pos = pos;
        int stmtIndex = onFailBodyBlock.stmts.isEmpty() ? 0 : 1;
        onFailBodyBlock.stmts.add(stmtIndex, rollbackCheck);

        BConstructorSymbol transactionErrorSymbol = (BConstructorSymbol) symTable.langTransactionModuleSymbol
                .scope.lookup(names.fromString("TransactionError")).symbol;
        BType errorType = transactionErrorSymbol.type;

        BLangErrorType trxErrorTypeNode = (BLangErrorType) TreeBuilder.createErrorTypeNode();
        trxErrorTypeNode.type = errorType;
        BLangSimpleVarRef trxResultRef = ASTBuilderUtil.createVariableRef(pos, trxFuncResultSymbol);

        // $trxError$ is TransactionError
        BLangTypeTestExpr testExpr = ASTBuilderUtil.createTypeTestExpr(pos, trxResultRef, trxErrorTypeNode);
        testExpr.type = symTable.booleanType;

        BLangGroupExpr transactionErrorCheckGroupExpr = new BLangGroupExpr();
        transactionErrorCheckGroupExpr.type = symTable.booleanType;
        // !($trxError$ is TransactionError)
        transactionErrorCheckGroupExpr.expression =  desugar.createNotBinaryExpression(pos, testExpr);

        // ($trxError$ is error)
        BLangTypeTestExpr errorCheck = desugar.createTypeCheckExpr(pos, trxResultRef, desugar.getErrorOrNillTypeNode());

        // ($trxError$ is error) && !($trxError$ is TransactionError)
        BLangBinaryExpr isErrorCheck = ASTBuilderUtil.createBinaryExpr(pos, errorCheck, transactionErrorCheckGroupExpr,
                symTable.booleanType, OperatorKind.AND, null);

        // transactional
        BLangTransactionalExpr isTransactionalCheck = TreeBuilder.createTransactionalExpressionNode();
        isTransactionalCheck.pos = pos;

        // if(($trxError$ is error) && !($trxError$ is TransactionError) && transactional)
        rollbackCheck.expr = ASTBuilderUtil.createBinaryExpr(pos, isErrorCheck, isTransactionalCheck,
                symTable.booleanType, OperatorKind.AND, null);
        rollbackCheck.body = ASTBuilderUtil.createBlockStmt(pos);

        // rollbackTransaction(transactionBlockID);
        BLangStatementExpression rollbackInvocation = invokeRollbackFunc(pos,
                desugar.addConversionExprIfRequired(trxResultRef, symTable.errorOrNilType), trxBlockId);

        BLangCheckedExpr checkedExpr = ASTBuilderUtil.createCheckPanickedExpr(pos, rollbackInvocation,
                symTable.nilType);
        checkedExpr.equivalentErrorTypeList.add(symTable.errorType);

        BLangExpressionStmt transactionExprStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        transactionExprStmt.pos = pos;
        transactionExprStmt.expr = checkedExpr;
        transactionExprStmt.type = symTable.nilType;
        rollbackCheck.body.stmts.add(transactionExprStmt);

        // at this point;
        //    if (($trxError$ is error) && !($trxError$ is TransactionError) && transactional) {
        //        $shouldCleanUp$ = true;
        //        check panic rollback $trxError$;
        //    }
    }

    private BLangInvocation createCleanupTrxStmt(Location pos, BLangLiteral trxBlockId) {
        List<BLangExpression> args;
        BInvokableSymbol cleanupTrxInvokableSymbol =
                (BInvokableSymbol) getInternalTransactionModuleInvokableSymbol(CLEAN_UP_TRANSACTION);
        args = new ArrayList<>();
        args.add(trxBlockId);
        BLangInvocation cleanupTrxInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, cleanupTrxInvokableSymbol, args, symResolver);
        cleanupTrxInvocation.argExprs = args;
        return cleanupTrxInvocation;
    }

    BLangStatementExpression invokeRollbackFunc(Location pos, BLangExpression rollbackExpr,
                                                BLangLiteral trxBlockId) {
        // Rollback desugar implementation
        BLangBlockStmt rollbackBlockStmt = ASTBuilderUtil.createBlockStmt(pos);

        // rollbackTransaction(transactionBlockID);
        BInvokableSymbol rollbackTransactionInvokableSymbol =
                (BInvokableSymbol) getInternalTransactionModuleInvokableSymbol(ROLLBACK_TRANSACTION);
        List<BLangExpression> args = new ArrayList<>();
        args.add(trxBlockId);
        if (rollbackExpr != null) {
            args.add(rollbackExpr);
        }
        BLangInvocation rollbackTransactionInvocation = ASTBuilderUtil.
                createInvocationExprForMethod(pos, rollbackTransactionInvokableSymbol, args, symResolver);
        rollbackTransactionInvocation.argExprs = args;
        BLangExpressionStmt rollbackStmt = ASTBuilderUtil.createExpressionStmt(pos, rollbackBlockStmt);
        rollbackStmt.expr = rollbackTransactionInvocation;

        BLangExpressionStmt cleanUpTrx = ASTBuilderUtil.createExpressionStmt(pos, rollbackBlockStmt);
        cleanUpTrx.expr = createCleanupTrxStmt(pos, trxBlockId);
        BLangStatementExpression rollbackStmtExpr = createStatementExpression(rollbackBlockStmt,
                ASTBuilderUtil.createLiteral(pos, symTable.nilType, Names.NIL_VALUE));
        rollbackStmtExpr.type = symTable.nilType;

        //at this point,
        //
        // rollbackTransaction(transactionBlockID);
        // $shouldCleanUp$ = true;
        return rollbackStmtExpr;
    }

    BLangStatementExpression desugar(BLangCommitExpr commitExpr, SymbolEnv env) {
        Location pos = commitExpr.pos;
        BLangBlockStmt commitBlockStatement = ASTBuilderUtil.createBlockStmt(pos);

        // Create temp output variable
        // error? $outputVar$ = ();
        BLangSimpleVariableDef outputVariableDef = createCommitResultVarDef(env, pos);
        BLangSimpleVarRef outputVarRef = ASTBuilderUtil.createVariableRef(pos, outputVariableDef.var.symbol);
        commitBlockStatement.addStatement(outputVariableDef);

        // Clear failures
        // boolean isFailed = getAndClearFailure();
        BInvokableSymbol transactionCleanerInvokableSymbol =
                (BInvokableSymbol) getInternalTransactionModuleInvokableSymbol(GET_AND_CLEAR_FAILURE_TRANSACTION);
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
                (BInvokableSymbol) getInternalTransactionModuleInvokableSymbol(END_TRANSACTION);
        List<BLangExpression> args = new ArrayList<>();
        args.add(transactionID);
        args.add(trxBlockId);
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
        BLangStatementExpression stmtExpr = createStatementExpression(commitBlockStatement,
                outputVarRef);
        stmtExpr.type = symTable.errorOrNilType;
        return stmtExpr;
    }

    private BLangSimpleVariableDef createCommitResultVarDef(SymbolEnv env, Location pos) {
        BLangExpression nilLiteral = ASTBuilderUtil.createLiteral(pos, symTable.nilType, Names.NIL_VALUE);
        BVarSymbol outputVarSymbol = new BVarSymbol(0, new Name("$outputVar$"),
                env.scope.owner.pkgID, symTable.errorOrNilType, env.scope.owner, pos, VIRTUAL);
        BLangSimpleVariable outputVariable =
                ASTBuilderUtil.createVariable(pos, "$outputVar$", symTable.errorOrNilType,
                        nilLiteral, outputVarSymbol);
        return ASTBuilderUtil.createVariableDef(pos, outputVariable);
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

    /**
     * Load and return symbol for given name in transaction internal module.
     *
     * @param name of the symbol.
     * @return symbol for the function.
     */
    public BSymbol getInternalTransactionModuleInvokableSymbol(Name name) {
        if (symTable.internalTransactionModuleSymbol == null) {
            symTable.internalTransactionModuleSymbol =
                    packageCache.getSymbol(PackageID.TRANSACTION_INTERNAL);
        }
        return symTable.internalTransactionModuleSymbol.scope.lookup(name).symbol;
    }
}
