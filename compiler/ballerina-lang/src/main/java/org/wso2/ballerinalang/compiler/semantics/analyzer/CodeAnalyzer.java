/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.ForkJoinNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAwaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangActionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBind;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDone;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangFail;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStmtPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPostIncrement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * This represents the code analyzing pass of semantic analysis.
 * <p>
 * The following validations are done here:-
 * <p>
 * (*) Loop continuation statement validation.
 * (*) Function return path existence and unreachable code validation.
 * (*) Worker send/receive validation.
 */
public class CodeAnalyzer extends BLangNodeVisitor {

    private static final String MAIN_FUNC_NAME = "main";

    private static final CompilerContext.Key<CodeAnalyzer> CODE_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private int loopCount;
    private int transactionCount;
    private boolean statementReturns;
    private boolean lastStatement;
    private int forkJoinCount;
    private int workerCount;
    private SymbolEnter symbolEnter;
    private SymbolTable symTable;
    private Types types;
    private BLangDiagnosticLog dlog;
    private TypeChecker typeChecker;
    private Stack<WorkerActionSystem> workerActionSystemStack = new Stack<>();
    private Stack<Boolean> loopWithintransactionCheckStack = new Stack<>();
    private Stack<Boolean> returnWithintransactionCheckStack = new Stack<>();
    private Stack<Boolean> doneWithintransactionCheckStack = new Stack<>();
    private BLangNode parent;
    private Names names;
    private SymbolEnv env;

    public static CodeAnalyzer getInstance(CompilerContext context) {
        CodeAnalyzer codeGenerator = context.get(CODE_ANALYZER_KEY);
        if (codeGenerator == null) {
            codeGenerator = new CodeAnalyzer(context);
        }
        return codeGenerator;
    }

    public CodeAnalyzer(CompilerContext context) {
        context.put(CODE_ANALYZER_KEY, this);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);
        this.names = Names.getInstance(context);
    }

    private void resetFunction() {
        this.resetStatementReturns();
    }

    private void resetStatementReturns() {
        this.statementReturns = false;
    }

    private void resetLastStatement() {
        this.lastStatement = false;
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        pkgNode.accept(this);
        return pkgNode;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.CODE_ANALYZE)) {
            return;
        }
        parent = pkgNode;
        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(pkgNode.symbol);
        pkgNode.imports.forEach(impPkgNode -> analyzeNode(impPkgNode, pkgEnv));
        pkgNode.topLevelNodes.forEach(topLevelNode -> analyzeNode((BLangNode) topLevelNode, pkgEnv));
        pkgNode.completedPhases.add(CompilerPhase.CODE_ANALYZE);
        parent = null;
    }

    private void analyzeNode(BLangNode node, SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        BLangNode myParent = parent;
        node.parent = parent;
        parent = node;
        node.accept(this);
        parent = myParent;
        this.env = prevEnv;
    }

    @Override
    public void visit(BLangCompilationUnit compUnitNode) {
        compUnitNode.topLevelNodes.forEach(e -> analyzeNode((BLangNode) e, env));
    }

    public void visit(BLangTypeDefinition typeDefinition) {
        //TODO
    }

    private void validateMainFunction(BLangFunction funcNode) {
        if (MAIN_FUNC_NAME.equals(funcNode.name.value) && Symbols.isPublic(funcNode.symbol)) {
            this.dlog.error(funcNode.pos, DiagnosticCode.MAIN_CANNOT_BE_PUBLIC);
        }
    }
    
    @Override
    public void visit(BLangFunction funcNode) {
        this.returnWithintransactionCheckStack.push(true);
        this.doneWithintransactionCheckStack.push(true);
        this.validateMainFunction(funcNode);
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        this.visitInvocable(funcNode, funcEnv);
        this.returnWithintransactionCheckStack.pop();
        this.doneWithintransactionCheckStack.pop();
    }

    private void visitInvocable(BLangInvokableNode invNode, SymbolEnv invokableEnv) {
        this.resetFunction();
        try {
            this.initNewWorkerActionSystem();
            if (Symbols.isNative(invNode.symbol)) {
                return;
            }
            boolean invokableReturns = invNode.returnTypeNode.type != symTable.nilType;
            if (invNode.workers.isEmpty()) {
                analyzeNode(invNode.body, invokableEnv);
                /* the function returns, but none of the statements surely returns */
                if (invokableReturns && !this.statementReturns) {
                    this.dlog.error(invNode.pos, DiagnosticCode.INVOKABLE_MUST_RETURN,
                            invNode.getKind().toString().toLowerCase());
                }
            } else {
                boolean workerReturns = false;
                for (BLangWorker worker : invNode.workers) {
                    analyzeNode(worker, invokableEnv);
                    workerReturns = workerReturns || this.statementReturns;
                    this.resetStatementReturns();
                }
                if (invokableReturns && !workerReturns) {
                    this.dlog.error(invNode.pos, DiagnosticCode.ATLEAST_ONE_WORKER_MUST_RETURN,
                            invNode.getKind().toString().toLowerCase());
                }
            }
        } finally {
            this.finalizeCurrentWorkerActionSystem();
        }
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        this.forkJoinCount++;
        this.initNewWorkerActionSystem();
        this.checkStatementExecutionValidity(forkJoin);
        forkJoin.workers.forEach(e -> analyzeNode(e, env));
        analyzeNode(forkJoin.joinedBody, env);
        if (forkJoin.timeoutBody != null) {
            boolean joinReturns = this.statementReturns;
            this.resetStatementReturns();
            analyzeNode(forkJoin.timeoutBody, env);
            this.statementReturns = joinReturns && this.statementReturns;
        }
        this.checkForkJoinWorkerCount(forkJoin);
        this.finalizeCurrentWorkerActionSystem();
        this.forkJoinCount--;
        analyzeExpr(forkJoin.timeoutExpression);
    }

    private boolean inForkJoin() {
        return this.forkJoinCount > 0;
    }

    private void checkForkJoinWorkerCount(BLangForkJoin forkJoin) {
        if (forkJoin.joinType == ForkJoinNode.JoinType.SOME) {
            int wc = forkJoin.joinedWorkers.size();
            if (wc == 0) {
                wc = forkJoin.workers.size();
            }
            if (forkJoin.joinedWorkerCount > wc) {
                this.dlog.error(forkJoin.pos, DiagnosticCode.FORK_JOIN_INVALID_WORKER_COUNT);
            }
        }
    }

    private boolean inWorker() {
        return this.workerCount > 0;
    }

    @Override
    public void visit(BLangWorker worker) {
        this.workerCount++;
        this.workerActionSystemStack.peek().startWorkerActionStateMachine(worker.name.value, worker.pos);
        analyzeNode(worker.body, env);
        this.workerActionSystemStack.peek().endWorkerActionStateMachine();
        this.workerCount--;
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        this.checkStatementExecutionValidity(transactionNode);
        this.loopWithintransactionCheckStack.push(false);
        this.returnWithintransactionCheckStack.push(false);
        this.doneWithintransactionCheckStack.push(false);
        this.transactionCount++;
        analyzeNode(transactionNode.transactionBody, env);
        this.transactionCount--;
        this.resetLastStatement();
        if (transactionNode.onRetryBody != null) {
            analyzeNode(transactionNode.onRetryBody, env);
            this.resetStatementReturns();
            this.resetLastStatement();
        }
        this.returnWithintransactionCheckStack.pop();
        this.loopWithintransactionCheckStack.pop();
        this.doneWithintransactionCheckStack.pop();
        analyzeExpr(transactionNode.retryCount);
        analyzeExpr(transactionNode.onCommitFunction);
        analyzeExpr(transactionNode.onAbortFunction);
    }

    @Override
    public void visit(BLangAbort abortNode) {
        if (this.transactionCount == 0) {
            this.dlog.error(abortNode.pos, DiagnosticCode.ABORT_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK);
            return;
        }
        this.lastStatement = true;
    }

    @Override
    public void visit(BLangDone doneNode) {
        if (checkReturnValidityInTransaction()) {
            this.dlog.error(doneNode.pos, DiagnosticCode.DONE_CANNOT_BE_USED_TO_EXIT_TRANSACTION);
            return;
        }
        this.lastStatement = true;
    }

    @Override
    public void visit(BLangFail failNode) {
        if (this.transactionCount == 0) {
            this.dlog.error(failNode.pos, DiagnosticCode.FAIL_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK);
            return;
        }
        this.lastStatement = true;
    }

    private void checkUnreachableCode(BLangStatement stmt) {
        if (this.statementReturns) {
            this.dlog.error(stmt.pos, DiagnosticCode.UNREACHABLE_CODE);
            this.resetStatementReturns();
        }
        if (lastStatement) {
            this.dlog.error(stmt.pos, DiagnosticCode.UNREACHABLE_CODE);
            this.resetLastStatement();
        }
    }

    private void checkStatementExecutionValidity(BLangStatement stmt) {
        this.checkUnreachableCode(stmt);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        final SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        blockNode.stmts.forEach(e -> analyzeNode(e, blockEnv));
        this.resetLastStatement();
    }

    @Override
    public void visit(BLangReturn returnStmt) {
        this.checkStatementExecutionValidity(returnStmt);

        // Check whether this return statement is in resource
        if (this.env.enclInvokable.getKind() == NodeKind.RESOURCE) {
            this.dlog.error(returnStmt.pos, DiagnosticCode.RETURN_STMT_NOT_VALID_IN_RESOURCE);
            return;
        }

        if (this.inForkJoin() && this.inWorker()) {
            this.dlog.error(returnStmt.pos, DiagnosticCode.FORK_JOIN_WORKER_CANNOT_RETURN);
            return;
        }
        if (checkReturnValidityInTransaction()) {
            this.dlog.error(returnStmt.pos, DiagnosticCode.RETURN_CANNOT_BE_USED_TO_EXIT_TRANSACTION);
            return;
        }
        this.statementReturns = true;
        analyzeExpr(returnStmt.expr);
    }

    @Override
    public void visit(BLangIf ifStmt) {
        this.checkStatementExecutionValidity(ifStmt);
        analyzeNode(ifStmt.body, env);
        boolean ifStmtReturns = this.statementReturns;
        this.resetStatementReturns();
        if (ifStmt.elseStmt != null) {
            analyzeNode(ifStmt.elseStmt, env);
            this.statementReturns = ifStmtReturns && this.statementReturns;
        }
        analyzeExpr(ifStmt.expr);
    }

    @Override
    public void visit(BLangMatch matchStmt) {
        this.returnWithintransactionCheckStack.push(true);
        boolean unmatchedExprTypesAvailable = false;
        analyzeExpr(matchStmt.expr);

        // TODO Handle **any** as a expr type.. special case it..
        // TODO Complete the exhaustive tests with any, struct and connector types
        // TODO Handle the case where there are incompatible types. e.g. input string : pattern int and pattern string

        List<BType> unmatchedExprTypes = new ArrayList<>();
        for (BType exprType : matchStmt.exprTypes) {
            boolean assignable = false;
            for (BLangMatchStmtPatternClause pattern : matchStmt.patternClauses) {
                BType patternType = pattern.variable.type;
                if (exprType.tag == TypeTags.ERROR || patternType.tag == TypeTags.ERROR) {
                    return;
                }

                assignable = this.types.isAssignable(exprType, patternType);
                if (assignable) {
                    pattern.matchedTypesDirect.add(exprType);
                    break;
                } else if (exprType.tag == TypeTags.ANY) {
                    pattern.matchedTypesIndirect.add(exprType);
                } else if (exprType.tag == TypeTags.JSON &&
                        this.types.isAssignable(patternType, exprType)) {
                    pattern.matchedTypesIndirect.add(exprType);
                } else if (exprType.tag == TypeTags.STRUCT &&
                        this.types.isAssignable(patternType, exprType)) {
                    pattern.matchedTypesIndirect.add(exprType);
                } else {
                    // TODO Support other assignable types
                }
            }

            if (!assignable) {
                unmatchedExprTypes.add(exprType);
            }
        }

        if (!unmatchedExprTypes.isEmpty()) {
            unmatchedExprTypesAvailable = true;
            dlog.error(matchStmt.pos, DiagnosticCode.MATCH_STMT_CANNOT_GUARANTEE_A_MATCHING_PATTERN,
                    unmatchedExprTypes);
        }

        boolean matchedPatternsAvailable = false;
        for (int i = matchStmt.patternClauses.size() - 1; i >= 0; i--) {
            BLangMatchStmtPatternClause pattern = matchStmt.patternClauses.get(i);
            if (pattern.matchedTypesDirect.isEmpty() && pattern.matchedTypesIndirect.isEmpty()) {
                if (matchedPatternsAvailable) {
                    dlog.error(pattern.pos, DiagnosticCode.MATCH_STMT_UNMATCHED_PATTERN);
                } else {
                    dlog.error(pattern.pos, DiagnosticCode.MATCH_STMT_UNREACHABLE_PATTERN);
                }
            } else {
                matchedPatternsAvailable = true;
            }
        }

        // Execute the following block if there are no unmatched expression types
        if (!unmatchedExprTypesAvailable) {
            this.checkStatementExecutionValidity(matchStmt);
            boolean matchStmtReturns = true;
            for (BLangMatchStmtPatternClause patternClause : matchStmt.patternClauses) {
                analyzeNode(patternClause.body, env);
                matchStmtReturns = matchStmtReturns && this.statementReturns;
                this.resetStatementReturns();
            }

            this.statementReturns = matchStmtReturns;
        }
        this.returnWithintransactionCheckStack.pop();
    }

    @Override
    public void visit(BLangForeach foreach) {
        this.loopWithintransactionCheckStack.push(true);
        this.checkStatementExecutionValidity(foreach);
        this.loopCount++;
        foreach.body.stmts.forEach(e -> analyzeNode(e, env));
        this.loopCount--;
        this.resetLastStatement();
        this.loopWithintransactionCheckStack.pop();
        analyzeExpr(foreach.collection);
        analyzeExprs(foreach.varRefs);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        this.loopWithintransactionCheckStack.push(true);
        this.checkStatementExecutionValidity(whileNode);
        this.loopCount++;
        whileNode.body.stmts.forEach(e -> analyzeNode(e, env));
        this.loopCount--;
        this.resetLastStatement();
        this.loopWithintransactionCheckStack.pop();
        analyzeExpr(whileNode.expr);
    }

    @Override
    public void visit(BLangLock lockNode) {
        this.checkStatementExecutionValidity(lockNode);
        lockNode.body.stmts.forEach(e -> analyzeNode(e, env));
    }

    @Override
    public void visit(BLangNext nextNode) {
        this.checkStatementExecutionValidity(nextNode);
        if (this.loopCount == 0) {
            this.dlog.error(nextNode.pos, DiagnosticCode.NEXT_CANNOT_BE_OUTSIDE_LOOP);
            return;
        }
        if (checkNextBreakValidityInTransaction()) {
            this.dlog.error(nextNode.pos, DiagnosticCode.NEXT_CANNOT_BE_USED_TO_EXIT_TRANSACTION);
            return;
        }
        this.lastStatement = true;
    }

    public void visit(BLangPackageDeclaration pkgDclNode) {
        /* ignore */
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgSymbol);
        if (pkgEnv == null) {
            return;
        }

        analyzeNode(pkgEnv.node, env);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        /* ignore */
    }

    public void visit(BLangService serviceNode) {
        SymbolEnv serviceEnv = SymbolEnv.createServiceEnv(serviceNode, serviceNode.symbol.scope, env);
        serviceNode.resources.forEach(res -> analyzeNode(res, serviceEnv));
    }

    public void visit(BLangResource resourceNode) {
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode,
                resourceNode.symbol.scope, env);
        this.visitInvocable(resourceNode, resourceEnv);
    }

    public void visit(BLangConnector connectorNode) {
    }

    public void visit(BLangForever foreverStatement) {
        this.lastStatement = true;
    }

    public void visit(BLangAction actionNode) {
        /* not used, covered with functions */
    }

    public void visit(BLangStruct structNode) {
        /* ignore */
    }

    public void visit(BLangObject objectNode) {
        /* ignore */
    }

    public void visit(BLangRecord record) {
        /* ignore */
    }

    public void visit(BLangEnum enumNode) {
        /* ignore */
    }

    public void visit(BLangVariable varNode) {
        analyzeExpr(varNode.expr);
    }

    public void visit(BLangIdentifier identifierNode) {
        /* ignore */
    }

    public void visit(BLangAnnotation annotationNode) {
        /* ignore */
    }

    public void visit(BLangAnnotAttribute annotationAttribute) {
        /* ignore */
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        /* ignore */
    }

    public void visit(BLangAnnotAttachmentAttributeValue annotAttributeValue) {
        /* ignore */
    }

    public void visit(BLangAnnotAttachmentAttribute annotAttachmentAttribute) {
        /* ignore */
    }

    public void visit(BLangTransformer transformerNode) {
        List<BVarSymbol> inputs = new ArrayList<>();
        inputs.add(transformerNode.source.symbol);
        transformerNode.requiredParams.forEach(param -> inputs.add(param.symbol));

        List<BVarSymbol> outputs = new ArrayList<>();
        transformerNode.retParams.forEach(param -> outputs.add(param.symbol));

        for (BLangStatement stmt : transformerNode.body.stmts) {
            switch (stmt.getKind()) {
                case VARIABLE_DEF:
                    BLangVariableDef variableDefStmt = (BLangVariableDef) stmt;
                    variableDefStmt.var.expr.accept(
                            new TransformerVarRefValidator(outputs, DiagnosticCode.TRANSFORMER_INVALID_OUTPUT_USAGE));
                    inputs.add(variableDefStmt.var.symbol);
                    break;
                case ASSIGNMENT:
                    BLangAssignment assignStmt = (BLangAssignment) stmt;
                    assignStmt.varRef.accept(new TransformerVarRefValidator(inputs,
                            DiagnosticCode.TRANSFORMER_INVALID_INPUT_UPDATE));

                    // If the stmt is declared using var, all the variable refs on lhs should be treated as inputs
                    if (assignStmt.declaredWithVar && assignStmt.varRef.getKind() == NodeKind.SIMPLE_VARIABLE_REF
                            && !inputs.contains(((BLangSimpleVarRef) assignStmt.varRef).symbol)) {
                        inputs.add(((BLangSimpleVarRef) assignStmt.varRef).varSymbol);
                    }
                    assignStmt.expr.accept(
                            new TransformerVarRefValidator(outputs, DiagnosticCode.TRANSFORMER_INVALID_OUTPUT_USAGE));
                    break;
                case EXPRESSION_STATEMENT:
                    // Here we have assumed that the invocation expression is the only expression-statement available.
                    // TODO: support other types, once they are implemented.
                    dlog.error(stmt.pos, DiagnosticCode.INVALID_STATEMENT_IN_TRANSFORMER, "invocation");
                    break;
                default:
                    dlog.error(stmt.pos, DiagnosticCode.INVALID_STATEMENT_IN_TRANSFORMER,
                            stmt.getKind().name().toLowerCase().replace('_', ' '));
                    break;
            }
        }
    }

    public void visit(BLangVariableDef varDefNode) {
        this.checkStatementExecutionValidity(varDefNode);
        analyzeNode(varDefNode.var, env);
    }

    public void visit(BLangCompoundAssignment compoundAssignment) {
        this.checkStatementExecutionValidity(compoundAssignment);
        analyzeExpr(compoundAssignment.varRef);
        analyzeExpr(compoundAssignment.expr);
    }

    public void visit(BLangPostIncrement postIncrement) {
        this.checkStatementExecutionValidity(postIncrement);
        analyzeExpr(postIncrement.varRef);
        analyzeExpr(postIncrement.increment);
    }

    public void visit(BLangAssignment assignNode) {
        this.checkStatementExecutionValidity(assignNode);
        analyzeExpr(assignNode.varRef);
        analyzeExpr(assignNode.expr);
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        this.checkStatementExecutionValidity(stmt);
        analyzeExprs(stmt.varRefs);
        analyzeExpr(stmt.expr);
    }

    public void visit(BLangBind bindNode) {
        this.checkStatementExecutionValidity(bindNode);
        analyzeExpr(bindNode.varRef);
        analyzeExpr(bindNode.expr);
    }

    public void visit(BLangBreak breakNode) {
        this.checkStatementExecutionValidity(breakNode);
        if (this.loopCount == 0) {
            this.dlog.error(breakNode.pos, DiagnosticCode.BREAK_CANNOT_BE_OUTSIDE_LOOP);
            return;
        }
        if (checkNextBreakValidityInTransaction()) {
            this.dlog.error(breakNode.pos, DiagnosticCode.BREAK_CANNOT_BE_USED_TO_EXIT_TRANSACTION);
            return;
        }
        this.lastStatement = true;
    }

    public void visit(BLangThrow throwNode) {
        this.checkStatementExecutionValidity(throwNode);
        this.statementReturns = true;
        analyzeExpr(throwNode.expr);
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        this.checkStatementExecutionValidity(xmlnsStmtNode);
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        this.checkStatementExecutionValidity(exprStmtNode);
        analyzeExpr(exprStmtNode.expr);
        validateExprStatementExpression(exprStmtNode);
    }

    private void validateExprStatementExpression(BLangExpressionStmt exprStmtNode) {
        BLangExpression expr = exprStmtNode.expr;
        while (expr.getKind() == NodeKind.MATCH_EXPRESSION || expr.getKind() == NodeKind.CHECK_EXPR) {
            if (expr.getKind() == NodeKind.MATCH_EXPRESSION) {
                expr = ((BLangMatchExpression) expr).expr;
            } else if (expr.getKind() == NodeKind.CHECK_EXPR) {
                expr = ((BLangCheckedExpr) expr).expr;
            }
        }
        // Allowed expression kinds
        if (expr.getKind() == NodeKind.INVOCATION || expr.getKind() == NodeKind.AWAIT_EXPR) {
            return;
        }
        // For other expressions, error is logged already.
        if (expr.type == symTable.nilType) {
            dlog.error(exprStmtNode.pos, DiagnosticCode.INVALID_EXPR_STATEMENT);
        }
    }

    public void visit(BLangTryCatchFinally tryNode) {
        this.checkStatementExecutionValidity(tryNode);
        analyzeNode(tryNode.tryBody, env);
        this.resetStatementReturns();
        List<BType> caughtTypes = new ArrayList<>();
        for (BLangCatch bLangCatch : tryNode.getCatchBlocks()) {
            if (caughtTypes.contains(bLangCatch.getParameter().type)) {
                dlog.error(bLangCatch.getParameter().pos, DiagnosticCode.DUPLICATED_ERROR_CATCH,
                        bLangCatch.getParameter().type);
            }
            caughtTypes.add(bLangCatch.getParameter().type);
            analyzeNode(bLangCatch.body, env);
            this.resetStatementReturns();
        }
        if (tryNode.finallyBody != null) {
            analyzeNode(tryNode.finallyBody, env);
            this.resetStatementReturns();
        }
    }

    public void visit(BLangCatch catchNode) {
        /* ignore */
    }

    public void visit(BLangWorkerSend workerSendNode) {
        this.checkStatementExecutionValidity(workerSendNode);
        if (!this.inWorker()) {
            return;
        }
        this.workerActionSystemStack.peek().addWorkerAction(workerSendNode);
        analyzeExpr(workerSendNode.expr);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        this.checkStatementExecutionValidity(workerReceiveNode);
        if (!this.inWorker()) {
            return;
        }
        this.workerActionSystemStack.peek().addWorkerAction(workerReceiveNode);
        analyzeExpr(workerReceiveNode.expr);
    }

    public void visit(BLangLiteral literalExpr) {
        /* ignore */
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        analyzeExprs(arrayLiteral.exprs);
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        recordLiteral.keyValuePairs.forEach(kv -> {
            analyzeExpr(kv.valueExpr);
        });
        checkAccess(recordLiteral);
    }

    public void visit(BLangTableLiteral tableLiteral) {
        /* ignore */
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        /* ignore */
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        /* ignore */
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        analyzeExpr(indexAccessExpr.indexExpr);
        analyzeExpr(indexAccessExpr.expr);
    }

    public void visit(BLangInvocation invocationExpr) {
        analyzeExpr(invocationExpr.expr);
        analyzeExprs(invocationExpr.requiredArgs);
        analyzeExprs(invocationExpr.namedArgs);
        analyzeExprs(invocationExpr.restArgs);

        checkDuplicateNamedArgs(invocationExpr.namedArgs);

        // Null check is to ignore Negative path where symbol does not get resolved at TypeChecker.
        if ((invocationExpr.symbol != null) && invocationExpr.symbol.kind == SymbolKind.FUNCTION) {
            BSymbol funcSymbol = invocationExpr.symbol;
            if (Symbols.isFlagOn(funcSymbol.flags, Flags.DEPRECATED)) {
                dlog.warning(invocationExpr.pos, DiagnosticCode.USAGE_OF_DEPRECATED_FUNCTION,
                        names.fromIdNode(invocationExpr.name));
            }
        }

        if (invocationExpr.actionInvocation) {
            validateActionInvocation(invocationExpr.pos, invocationExpr);
        }
    }

    private void validateActionInvocation(DiagnosticPos pos, BLangNode bLangNode) {
        BLangNode parent = bLangNode.parent;
        while (parent != null) {
            final NodeKind kind = parent.getKind();
            // Allowed node types.
            if (kind == NodeKind.ASSIGNMENT || kind == NodeKind.EXPRESSION_STATEMENT
                    || kind == NodeKind.TUPLE_DESTRUCTURE || kind == NodeKind.VARIABLE) {
                return;
            } else if (kind == NodeKind.CHECK_EXPR || kind == NodeKind.MATCH_EXPRESSION) {
                parent = parent.parent;
                continue;
            } else if (kind == NodeKind.ELVIS_EXPR
                    && ((BLangElvisExpr) parent).lhsExpr.getKind() == NodeKind.INVOCATION
                    && ((BLangInvocation) ((BLangElvisExpr) parent).lhsExpr).actionInvocation) {
                parent = parent.parent;
                continue;
            }
            break;
        }
        dlog.error(pos, DiagnosticCode.INVALID_ACTION_INVOCATION_AS_EXPR);
    }

    public void visit(BLangTypeInit cIExpr) {
        analyzeExprs(cIExpr.argsExpr);
        analyzeExpr(cIExpr.objectInitInvocation);
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        analyzeExpr(ternaryExpr.expr);
        analyzeExpr(ternaryExpr.thenExpr);
        analyzeExpr(ternaryExpr.elseExpr);
    }

    public void visit(BLangAwaitExpr awaitExpr) {
        analyzeExpr(awaitExpr.expr);
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        analyzeExpr(binaryExpr.lhsExpr);
        analyzeExpr(binaryExpr.rhsExpr);
    }

    public void visit(BLangElvisExpr elvisExpr) {
        analyzeExpr(elvisExpr.lhsExpr);
        analyzeExpr(elvisExpr.rhsExpr);
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        analyzeExprs(bracedOrTupleExpr.expressions);
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        analyzeExpr(unaryExpr.expr);
    }

    public void visit(BLangTypedescExpr accessExpr) {
        /* ignore */
    }

    public void visit(BLangTypeCastExpr castExpr) {
        analyzeExpr(castExpr.expr);
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        analyzeExpr(conversionExpr.expr);
        analyzeExpr(conversionExpr.transformerInvocation);
    }

    public void visit(BLangXMLQName xmlQName) {
        /* ignore */
    }

    public void visit(BLangXMLAttribute xmlAttribute) {
        analyzeExpr(xmlAttribute.name);
        analyzeExpr(xmlAttribute.value);
    }

    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        analyzeExpr(xmlElementLiteral.startTagName);
        analyzeExpr(xmlElementLiteral.endTagName);
        analyzeExprs(xmlElementLiteral.attributes);
        analyzeExprs(xmlElementLiteral.children);
    }

    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        analyzeExprs(xmlTextLiteral.textFragments);
    }

    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        analyzeExprs(xmlCommentLiteral.textFragments);
    }

    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        analyzeExprs(xmlProcInsLiteral.dataFragments);
        analyzeExpr(xmlProcInsLiteral.target);
    }

    public void visit(BLangXMLQuotedString xmlQuotedString) {
        analyzeExprs(xmlQuotedString.textFragments);
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        analyzeExprs(stringTemplateLiteral.exprs);
    }

    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        /* ignore */
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        analyzeExpr(xmlAttributeAccessExpr.expr);
        analyzeExpr(xmlAttributeAccessExpr.indexExpr);
    }

    public void visit(BLangIntRangeExpression intRangeExpression) {
        analyzeExpr(intRangeExpression.startExpr);
        analyzeExpr(intRangeExpression.endExpr);
    }

    public void visit(BLangValueType valueType) {
        /* ignore */
    }

    public void visit(BLangArrayType arrayType) {
        /* ignore */
    }

    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        /* ignore */
    }

    public void visit(BLangConstrainedType constrainedType) {
        /* ignore */
    }

    public void visit(BLangUserDefinedType userDefinedType) {
        /* ignore */
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {
        /* ignore */
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        /* ignore */
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        /* ignore */
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        // TODO
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        // TODO
    }

    // private methods

    private <E extends BLangExpression> void analyzeExpr(E node) {
        if (node == null) {
            return;
        }
        BLangNode myParent = parent;
        node.parent = parent;
        parent = node;
        node.accept(this);
        parent = myParent;
        checkAccess(node);
    }

    private <E extends BLangExpression> void checkAccess(E node) {
        if (node.type == null || node.type.tsymbol == null) {
            return;
        }

        BSymbol symbol = node.type.tsymbol;

        if (!(env.enclPkg.symbol.pkgID == symbol.pkgID || (Symbols.isPublic(symbol)))) {
            dlog.error(node.pos, DiagnosticCode.ATTEMPT_REFER_NON_PUBLIC_SYMBOL, symbol.name);
        }
    }

    private <E extends BLangExpression> void analyzeExprs(List<E> nodeList) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.get(i).accept(this);
        }
    }

    private void initNewWorkerActionSystem() {
        this.workerActionSystemStack.push(new WorkerActionSystem());
    }

    private void finalizeCurrentWorkerActionSystem() {
        WorkerActionSystem was = this.workerActionSystemStack.pop();
        this.validateWorkerInteractions(was);
    }

    private static boolean isWorkerSend(BLangStatement action) {
        return action.getKind() == NodeKind.WORKER_SEND;
    }

    private static boolean isWorkerForkSend(BLangStatement action) {
        return ((BLangWorkerSend) action).isForkJoinSend;
    }

    private String extractWorkerId(BLangStatement action) {
        if (isWorkerSend(action)) {
            return ((BLangWorkerSend) action).workerIdentifier.value;
        } else {
            return ((BLangWorkerReceive) action).workerIdentifier.value;
        }
    }

    private void validateWorkerInteractions(WorkerActionSystem workerActionSystem) {
        this.validateForkJoinSendsToFork(workerActionSystem);
        BLangStatement currentAction;
        WorkerActionStateMachine currentSM;
        String currentWorkerId;
        boolean systemRunning;
        do {
            systemRunning = false;
            for (Map.Entry<String, WorkerActionStateMachine> entry : workerActionSystem.entrySet()) {
                currentWorkerId = entry.getKey();
                currentSM = entry.getValue();
                if (currentSM.done()) {
                    continue;
                }
                currentAction = currentSM.currentAction();
                if (isWorkerSend(currentAction)) {
                    if (isWorkerForkSend(currentAction)) {
                        currentSM.next();
                        systemRunning = true;
                    } else {
                        WorkerActionStateMachine otherSM = workerActionSystem.get(this.extractWorkerId(currentAction));
                        if (otherSM.currentIsReceive(currentWorkerId)) {
                            this.validateWorkerActionParameters((BLangWorkerSend) currentAction,
                                    (BLangWorkerReceive) otherSM.currentAction());
                            otherSM.next();
                            currentSM.next();
                            systemRunning = true;
                        }
                    }
                }
            }
        } while (systemRunning);
        if (!workerActionSystem.everyoneDone()) {
            this.reportInvalidWorkerInteractionDiagnostics(workerActionSystem);
        }
    }

    private void validateForkJoinSendsToFork(WorkerActionSystem workerActionSystem) {
        for (Map.Entry<String, WorkerActionStateMachine> entry : workerActionSystem.entrySet()) {
            this.validateForkJoinSendsToFork(entry.getValue());
        }
    }

    private void validateForkJoinSendsToFork(WorkerActionStateMachine sm) {
        boolean sentToFork = false;
        for (BLangStatement action : sm.actions) {
            if (isWorkerSend(action) && isWorkerForkSend(action)) {
                if (sentToFork) {
                    this.dlog.error(action.pos, DiagnosticCode.INVALID_MULTIPLE_FORK_JOIN_SEND);
                } else {
                    sentToFork = true;
                }
            }
        }
    }

    private void reportInvalidWorkerInteractionDiagnostics(WorkerActionSystem workerActionSystem) {
        this.dlog.error(workerActionSystem.getRootPosition(), DiagnosticCode.INVALID_WORKER_INTERACTION,
                workerActionSystem.toString());
    }

    private void validateWorkerActionParameters(BLangWorkerSend send, BLangWorkerReceive receive) {
        this.typeChecker.checkExpr(send.expr, send.env, receive.expr.type);
    }

    private boolean checkNextBreakValidityInTransaction() {
        return !this.loopWithintransactionCheckStack.peek() && transactionCount > 0;
    }

    private boolean checkReturnValidityInTransaction() {
        return (this.returnWithintransactionCheckStack.empty() || !this.returnWithintransactionCheckStack.peek())
                && transactionCount > 0;
    }

    private boolean checkDoneValidityInTransaction() {
        return (this.doneWithintransactionCheckStack.empty() || !this.doneWithintransactionCheckStack.peek())
                && transactionCount > 0;
    }

    private void checkDuplicateNamedArgs(List<BLangExpression> args) {
        List<BLangIdentifier> existingArgs = new ArrayList<>();
        args.forEach(arg -> {
            BLangNamedArgsExpression namedArg = (BLangNamedArgsExpression) arg;
            if (existingArgs.contains(namedArg.name)) {
                dlog.error(namedArg.pos, DiagnosticCode.DUPLICATE_NAMED_ARGS, namedArg.name);
            }
            existingArgs.add(namedArg.name);
        });
    }

    /**
     * This class contains the state machines for a set of workers.
     */
    private static class WorkerActionSystem {

        public Map<String, WorkerActionStateMachine> workerActionStateMachines = new LinkedHashMap<>();

        private WorkerActionStateMachine currentSM;

        private String currentWorkerId;

        public void startWorkerActionStateMachine(String workerId, DiagnosticPos pos) {
            this.currentWorkerId = workerId;
            this.currentSM = new WorkerActionStateMachine(pos);
        }

        public void endWorkerActionStateMachine() {
            this.workerActionStateMachines.put(this.currentWorkerId, this.currentSM);
        }

        public void addWorkerAction(BLangStatement action) {
            this.currentSM.actions.add(action);
        }

        public WorkerActionStateMachine get(String workerId) {
            return this.workerActionStateMachines.get(workerId);
        }

        public Set<Map.Entry<String, WorkerActionStateMachine>> entrySet() {
            return this.workerActionStateMachines.entrySet();
        }

        public boolean everyoneDone() {
            return this.workerActionStateMachines.values().stream().allMatch(WorkerActionStateMachine::done);
        }

        public DiagnosticPos getRootPosition() {
            return this.workerActionStateMachines.values().iterator().next().pos;
        }

        @Override
        public String toString() {
            return this.workerActionStateMachines.toString();
        }

    }

    /**
     * This class represents a state machine to maintain the state of the send/receive
     * actions of a worker.
     */
    private static class WorkerActionStateMachine {

        private static final String WORKER_SM_FINISHED = "FINISHED";

        public int currentState;

        public List<BLangStatement> actions = new ArrayList<>();

        public DiagnosticPos pos;

        public WorkerActionStateMachine(DiagnosticPos pos) {
            this.pos = pos;
        }

        public boolean done() {
            return this.actions.size() == this.currentState;
        }

        public BLangStatement currentAction() {
            return this.actions.get(this.currentState);
        }

        public boolean currentIsReceive(String sourceWorkerId) {
            if (this.done()) {
                return false;
            }
            BLangStatement action = this.currentAction();
            return !isWorkerSend(action) && ((BLangWorkerReceive) action).
                    workerIdentifier.value.equals(sourceWorkerId);
        }

        public void next() {
            this.currentState++;
        }

        @Override
        public String toString() {
            if (this.done()) {
                return WORKER_SM_FINISHED;
            } else {
                BLangStatement action = this.currentAction();
                if (isWorkerSend(action)) {
                    return ((BLangWorkerSend) action).toActionString();
                } else {
                    return ((BLangWorkerReceive) action).toActionString();
                }
            }
        }
    }

    /**
     * Visit all the nested expressions and validate variable references against a given set of symbols.
     * This visitor is used to check whether an expression contains inputs/output variables, that are not
     * suppose to be there.
     *
     * @since 0.94.2
     */
    private class TransformerVarRefValidator extends BLangNodeVisitor {

        List<BVarSymbol> symbols;
        DiagnosticCode errorCode;

        /**
         * Create a new {@link TransformerVarRefValidator}.
         *
         * @param symbols         List of symbols to validate the expression against
         * @param diagCodeOnError Diagnostic code to be logged on error
         */
        public TransformerVarRefValidator(List<BVarSymbol> symbols, DiagnosticCode diagCodeOnError) {
            this.symbols = symbols;
            this.errorCode = diagCodeOnError;
        }

        @Override
        public void visit(BLangSimpleVarRef expr) {
            if (symbols.contains(expr.symbol)) {
                dlog.error(expr.pos, errorCode, expr.symbol.name);
            }
        }

        @Override
        public void visit(BLangInvocation invocationExpr) {
            if (invocationExpr.expr != null) {
                if (invocationExpr.isActionInvocation()) {
                    dlog.error(invocationExpr.pos, DiagnosticCode.INVALID_STATEMENT_IN_TRANSFORMER,
                            "action invocation");
                }
                analyzeExpr(invocationExpr.expr);
            }
            invocationExpr.argExprs.forEach(CodeAnalyzer.this::analyzeExpr);
        }

        @Override
        public void visit(BLangTernaryExpr ternaryExpr) {
            analyzeExpr(ternaryExpr.expr);
            analyzeExpr(ternaryExpr.thenExpr);
            analyzeExpr(ternaryExpr.elseExpr);
        }

        @Override
        public void visit(BLangBinaryExpr binaryExpr) {
            analyzeExpr(binaryExpr.rhsExpr);
            analyzeExpr(binaryExpr.lhsExpr);
        }

        @Override
        public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
            bracedOrTupleExpr.expressions.forEach(CodeAnalyzer.this::analyzeExpr);
        }

        @Override
        public void visit(BLangUnaryExpr unaryExpr) {
            analyzeExpr(unaryExpr.expr);
        }

        @Override
        public void visit(BLangTypeConversionExpr conversionExpr) {
            analyzeExpr(conversionExpr.expr);
            if (conversionExpr.transformerInvocation != null) {
                analyzeExpr(conversionExpr.transformerInvocation);
            }
        }

        @Override
        public void visit(BLangTypeCastExpr castExpr) {
            analyzeExpr(castExpr.expr);
        }

        @Override
        public void visit(BLangRecordLiteral recordLiteral) {
            recordLiteral.keyValuePairs.forEach(keyVal -> analyzeExpr(keyVal.valueExpr));
        }

        public void visit(BLangArrayLiteral arrayLiteral) {
            arrayLiteral.exprs.forEach(CodeAnalyzer.this::analyzeExpr);
        }

        public void visit(BLangFieldBasedAccess fieldAccessExpr) {
            analyzeExpr(fieldAccessExpr.expr);
        }

        public void visit(BLangIndexBasedAccess indexAccessExpr) {
            analyzeExpr(indexAccessExpr.expr);
            analyzeExpr(indexAccessExpr.indexExpr);
        }

        public void visit(BLangTypeInit connectorInitExpr) {
            dlog.error(connectorInitExpr.pos, DiagnosticCode.INVALID_STATEMENT_IN_TRANSFORMER, "connector init");
        }

        public void visit(BLangActionInvocation actionInvocationExpr) {
            // We should not reach here. Action invocations are captured as a BLangInvocation expr.
            throw new IllegalStateException();
        }

        public void visit(BLangXMLQName xmlQName) {
            // do nothing
        }

        public void visit(BLangXMLAttribute xmlAttribute) {
            analyzeExpr(xmlAttribute.name);
            analyzeExpr(xmlAttribute.value);
        }

        public void visit(BLangXMLElementLiteral xmlElementLiteral) {
            analyzeExpr(xmlElementLiteral.startTagName);
            xmlElementLiteral.attributes.forEach(CodeAnalyzer.this::analyzeExpr);
            xmlElementLiteral.children.forEach(CodeAnalyzer.this::analyzeExpr);
            if (xmlElementLiteral.endTagName != null) {
                analyzeExpr(xmlElementLiteral.endTagName);
            }
        }

        public void visit(BLangXMLTextLiteral xmlTextLiteral) {
            xmlTextLiteral.textFragments.forEach(CodeAnalyzer.this::analyzeExpr);
        }

        public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
            xmlCommentLiteral.textFragments.forEach(CodeAnalyzer.this::analyzeExpr);
        }

        public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
            xmlProcInsLiteral.dataFragments.forEach(CodeAnalyzer.this::analyzeExpr);
            analyzeExpr(xmlProcInsLiteral.target);
        }

        public void visit(BLangXMLQuotedString xmlQuotedString) {
            xmlQuotedString.textFragments.forEach(CodeAnalyzer.this::analyzeExpr);
        }

        public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
            stringTemplateLiteral.exprs.forEach(CodeAnalyzer.this::analyzeExpr);
        }

        public void visit(BLangWorkerSend workerSendNode) {
            analyzeExpr(workerSendNode.expr);
        }

        public void visit(BLangWorkerReceive workerReceiveNode) {
            analyzeExpr(workerReceiveNode.expr);
        }

        public void visit(BLangLambdaFunction bLangLambdaFunction) {
            // TODO: support for lambda
        }

        public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
            analyzeExpr(xmlAttributeAccessExpr.expr);
            if (xmlAttributeAccessExpr.indexExpr != null) {
                analyzeExpr(xmlAttributeAccessExpr.indexExpr);
            }
        }

        public void visit(BLangLiteral literalExpr) {
            // do nothing
        }
    }
}
