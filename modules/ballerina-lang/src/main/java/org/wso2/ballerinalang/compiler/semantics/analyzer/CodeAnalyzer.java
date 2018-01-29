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
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.ForkJoinNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConnectorInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangActionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
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
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * This represents the code analyzing pass of semantic analysis. 
 * 
 * The following validations are done here:-
 * 
 * (*) Loop continuation statement validation.
 * (*) Function return path existence and unreachable code validation.
 * (*) Worker send/receive validation.
 */
public class CodeAnalyzer extends BLangNodeVisitor {
    
    private static final CompilerContext.Key<CodeAnalyzer> CODE_ANALYZER_KEY =
            new CompilerContext.Key<>();
    
    private int loopCount;
    private int transactionCount;
    private boolean statementReturns;
    private boolean lastStatement;
    private int forkJoinCount;
    private int workerCount;
    private SymbolEnter symbolEnter;
    private DiagnosticLog dlog;
    private TypeChecker typeChecker;
    private Stack<WorkerActionSystem> workerActionSystemStack = new Stack<>();
    private Stack<Boolean> loopWithintransactionCheckStack = new Stack<>();

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
        this.dlog = DiagnosticLog.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);
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
        pkgNode.imports.forEach(impPkgNode -> impPkgNode.accept(this));

        pkgNode.topLevelNodes.forEach(e -> ((BLangNode) e).accept(this));
        pkgNode.completedPhases.add(CompilerPhase.CODE_ANALYZE);
    }
    
    @Override
    public void visit(BLangCompilationUnit compUnitNode) {
        compUnitNode.topLevelNodes.forEach(e -> ((BLangNode) e).accept(this));
    }

    @Override
    public void visit(BLangFunction funcNode) {
        this.visitInvocable(funcNode);
    }

    private void visitInvocable(BLangInvokableNode invNode) {
        this.resetFunction();
        this.initNewWorkerActionSystem();
        if (Symbols.isNative(invNode.symbol)) {
            return;
        }
        boolean invokableReturns = invNode.retParams.size() > 0;
        if (invNode.workers.isEmpty()) {
            invNode.body.accept(this);
            /* the function returns, but none of the statements surely returns */
            if (invokableReturns && !this.statementReturns) {
                this.dlog.error(invNode.pos, DiagnosticCode.INVOKABLE_MUST_RETURN,
                        invNode.getKind().toString().toLowerCase());
            }
        } else {
            boolean workerReturns = false;
            for (BLangWorker worker : invNode.workers) {
                worker.accept(this);
                workerReturns = workerReturns || this.statementReturns;
                this.resetStatementReturns();
            }
            if (invokableReturns && !workerReturns) {
                this.dlog.error(invNode.pos, DiagnosticCode.ATLEAST_ONE_WORKER_MUST_RETURN,
                        invNode.getKind().toString().toLowerCase());
            }
        }
        this.finalizeCurrentWorkerActionSystem();
    }
    
    @Override
    public void visit(BLangForkJoin forkJoin) {
        this.forkJoinCount++;
        this.initNewWorkerActionSystem();
        this.checkStatementExecutionValidity(forkJoin);
        forkJoin.workers.forEach(e -> e.accept(this));
        forkJoin.joinedBody.accept(this);
        if (forkJoin.timeoutBody != null) {
            boolean joinReturns = this.statementReturns;
            this.resetStatementReturns();
            forkJoin.timeoutBody.accept(this);
            this.statementReturns = joinReturns && this.statementReturns;
        }
        this.checkForkJoinWorkerCount(forkJoin);
        this.finalizeCurrentWorkerActionSystem();
        this.forkJoinCount--;
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
        worker.body.accept(this);
        this.workerActionSystemStack.peek().endWorkerActionStateMachine();
        this.workerCount--;
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        this.checkStatementExecutionValidity(transactionNode);
        this.loopWithintransactionCheckStack.push(false);
        this.transactionCount++;
        transactionNode.transactionBody.accept(this);
        this.transactionCount--;
        this.resetLastStatement();
        if (transactionNode.failedBody != null) {
            transactionNode.failedBody.accept(this);
            this.resetStatementReturns();
            this.resetLastStatement();
        }
        this.loopWithintransactionCheckStack.pop();
    }

    @Override
    public void visit(BLangAbort abortNode) {
        if (this.transactionCount == 0) {
            this.dlog.error(abortNode.pos, DiagnosticCode.ABORT_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK);
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
        blockNode.stmts.forEach(e -> {
            e.accept(this);
        });
        this.resetLastStatement();
    }
    
    @Override
    public void visit(BLangReturn returnStmt) {
        this.checkStatementExecutionValidity(returnStmt);
        if (this.inForkJoin() && this.inWorker()) {
            this.dlog.error(returnStmt.pos, DiagnosticCode.FORK_JOIN_WORKER_CANNOT_RETURN);
            return;
        }
        this.statementReturns = true;
    }
    
    @Override
    public void visit(BLangIf ifStmt) {
        this.checkStatementExecutionValidity(ifStmt);
        ifStmt.body.accept(this);
        boolean ifStmtReturns = this.statementReturns;
        this.resetStatementReturns();
        if (ifStmt.elseStmt != null) {
            ifStmt.elseStmt.accept(this);
            this.statementReturns = ifStmtReturns && this.statementReturns;
        }
    }

    @Override
    public void visit(BLangForeach foreach) {
        this.checkStatementExecutionValidity(foreach);
        foreach.body.stmts.forEach(e -> e.accept(this));
    }

    @Override
    public void visit(BLangWhile whileNode) {
        this.loopWithintransactionCheckStack.push(true);
        this.checkStatementExecutionValidity(whileNode);
        this.loopCount++;
        whileNode.body.stmts.forEach(e -> e.accept(this));
        this.loopCount--;
        this.resetLastStatement();
        this.loopWithintransactionCheckStack.pop();
    }

    @Override
    public void visit(BLangLock lockNode) {
        this.checkStatementExecutionValidity(lockNode);
        lockNode.body.stmts.forEach(e -> e.accept(this));
    }
    
    @Override
    public void visit(BLangNext continueNode) {
        this.checkStatementExecutionValidity(continueNode);
        if (this.loopCount == 0) {
            this.dlog.error(continueNode.pos, DiagnosticCode.NEXT_CANNOT_BE_OUTSIDE_LOOP);
            return;
        }
        if (checkNextBreakValidityInTransaction()) {
            this.dlog.error(continueNode.pos, DiagnosticCode.NEXT_CANNOT_BE_USED_TO_EXIT_TRANSACTION);
            return;
        }
        this.lastStatement = true;
    }

    public void visit(BLangPackageDeclaration pkgDclNode) {
        /* ignore */
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgSymbol);
        if (pkgEnv == null) {
            return;
        }

        pkgEnv.node.accept(this);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        /* ignore */
    }

    public void visit(BLangService serviceNode) {
        /* ignore */
    }

    public void visit(BLangResource resourceNode) {
        this.visitInvocable(resourceNode);
    }

    public void visit(BLangConnector connectorNode) {
        connectorNode.actions.forEach(a -> a.accept(this));
    }

    public void visit(BLangAction actionNode) {
        this.visitInvocable(actionNode);
    }

    public void visit(BLangStruct structNode) {
        /* ignore */
    }

    public void visit(BLangEnum enumNode) {
        /* ignore */
    }

    public void visit(BLangVariable varNode) {
        /* ignore */
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
        transformerNode.params.forEach(param -> inputs.add(param.symbol));

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
                    assignStmt.varRefs.forEach(varRef -> {
                        varRef.accept(new TransformerVarRefValidator(inputs,
                                DiagnosticCode.TRANSFORMER_INVALID_INPUT_UPDATE));

                        // If the stmt is declared using var, all the variable refs on lhs should be treated as inputs
                        if (assignStmt.declaredWithVar && varRef.getKind() == NodeKind.SIMPLE_VARIABLE_REF
                                && !inputs.contains(((BLangSimpleVarRef) varRef).symbol)) {
                            inputs.add(((BLangSimpleVarRef) varRef).symbol);
                        }
                    });
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
    }

    public void visit(BLangAssignment assignNode) {
        this.checkStatementExecutionValidity(assignNode);
    }

    public void visit(BLangBind bindNode) {
        this.checkStatementExecutionValidity(bindNode);
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
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        this.checkStatementExecutionValidity(xmlnsStmtNode);
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        this.checkStatementExecutionValidity(exprStmtNode);
    }

    public void visit(BLangTryCatchFinally tryNode) {
        this.checkStatementExecutionValidity(tryNode);
        tryNode.tryBody.accept(this);
        this.resetStatementReturns();
        List<BType> caughtTypes = new ArrayList<>();
        for (BLangCatch bLangCatch : tryNode.getCatchBlocks()) {
            if (caughtTypes.contains(bLangCatch.getParameter().type)) {
                dlog.error(bLangCatch.getParameter().pos, DiagnosticCode.DUPLICATED_ERROR_CATCH,
                        bLangCatch.getParameter().type);
            }
            caughtTypes.add(bLangCatch.getParameter().type);
            bLangCatch.body.accept(this);
            this.resetStatementReturns();
        }
        if (tryNode.finallyBody != null) {
            tryNode.finallyBody.accept(this);
            this.resetStatementReturns();
        }
    }

    public void visit(BLangCatch catchNode) {
        /* ignore */
    }

    public void visit(BLangLiteral literalExpr) {
        /* ignore */
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        /* ignore */
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        /* ignore */
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        /* ignore */
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        /* ignore */
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        /* ignore */
    }

    public void visit(BLangInvocation invocationExpr) {
        /* ignore */
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        /* ignore */
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        /* ignore */
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        /* ignore */
    }

    public void visit(BLangTypeCastExpr castExpr) {
        /* ignore */
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        /* ignore */
    }

    public void visit(BLangXMLQName xmlQName) {
        /* ignore */
    }

    public void visit(BLangXMLAttribute xmlAttribute) {
        /* ignore */
    }

    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        /* ignore */
    }

    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        /* ignore */
    }

    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        /* ignore */
    }

    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        /* ignore */
    }

    public void visit(BLangXMLQuotedString xmlQuotedString) {
        /* ignore */
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        /* ignore */
    }

    public void visit(BLangWorkerSend workerSendNode) {
        if (!this.inWorker()) {
            return;
        }
        this.workerActionSystemStack.peek().addWorkerAction(workerSendNode);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        if (!this.inWorker()) {
            return;
        }
        this.workerActionSystemStack.peek().addWorkerAction(workerReceiveNode);
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

    private void reportInvalidWorkerInteractionDiagnostics(WorkerActionSystem workerActionSystem) {
        this.dlog.error(workerActionSystem.getRootPosition(), DiagnosticCode.INVALID_WORKER_INTERACTION,
                workerActionSystem.toString());
    }

    private void validateWorkerActionParameters(BLangWorkerSend send, BLangWorkerReceive receive) {
        List<BType> typeList = receive.exprs.stream().map(e -> e.type).collect(Collectors.toList());
        if (send.exprs.size() != typeList.size()) {
            this.dlog.error(send.pos, DiagnosticCode.WORKER_SEND_RECEIVE_PARAMETER_COUNT_MISMATCH);
        }
        for (int i = 0; i < typeList.size(); i++) {
            this.typeChecker.checkExpr(send.exprs.get(i), send.env, Arrays.asList(typeList.get(i)));
        }
    }

    private boolean checkNextBreakValidityInTransaction() {
        return !this.loopWithintransactionCheckStack.peek() && transactionCount > 0;
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
         * @param symbols List of symbols to validate the expression against
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
                if (invocationExpr.expr.type.tag == TypeTags.CONNECTOR
                        || invocationExpr.expr.type.tag == TypeTags.ENDPOINT) {
                    dlog.error(invocationExpr.pos, DiagnosticCode.INVALID_STATEMENT_IN_TRANSFORMER,
                            "action invocation");
                }
                invocationExpr.expr.accept(this);
            }
            invocationExpr.argExprs.forEach(argExpr -> argExpr.accept(this));
        }

        @Override
        public void visit(BLangTernaryExpr ternaryExpr) {
            ternaryExpr.expr.accept(this);
            ternaryExpr.thenExpr.accept(this);
            ternaryExpr.elseExpr.accept(this);
        }

        @Override
        public void visit(BLangBinaryExpr binaryExpr) {
            binaryExpr.rhsExpr.accept(this);
            binaryExpr.lhsExpr.accept(this);
        }

        @Override
        public void visit(BLangUnaryExpr unaryExpr) {
            unaryExpr.expr.accept(this);
        }

        @Override
        public void visit(BLangTypeConversionExpr conversionExpr) {
            conversionExpr.expr.accept(this);
            if (conversionExpr.transformerInvocation != null) {
                conversionExpr.transformerInvocation.accept(this);
            }
        }

        @Override
        public void visit(BLangTypeCastExpr castExpr) {
            castExpr.expr.accept(this);
        }

        @Override
        public void visit(BLangRecordLiteral recordLiteral) {
            recordLiteral.keyValuePairs.forEach(keyVal -> keyVal.valueExpr.accept(this));
        }

        public void visit(BLangArrayLiteral arrayLiteral) {
            arrayLiteral.exprs.forEach(exprs -> exprs.accept(this));
        }

        public void visit(BLangFieldBasedAccess fieldAccessExpr) {
            fieldAccessExpr.expr.accept(this);
        }

        public void visit(BLangIndexBasedAccess indexAccessExpr) {
            indexAccessExpr.expr.accept(this);
            indexAccessExpr.indexExpr.accept(this);
        }

        public void visit(BLangConnectorInit connectorInitExpr) {
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
            xmlAttribute.name.accept(this);
            xmlAttribute.value.accept(this);
        }

        public void visit(BLangXMLElementLiteral xmlElementLiteral) {
            xmlElementLiteral.startTagName.accept(this);
            xmlElementLiteral.attributes.forEach(attribute -> attribute.accept(this));
            xmlElementLiteral.children.forEach(child -> child.accept(this));
            if (xmlElementLiteral.endTagName != null) {
                xmlElementLiteral.endTagName.accept(this);
            }
        }

        public void visit(BLangXMLTextLiteral xmlTextLiteral) {
            xmlTextLiteral.textFragments.forEach(expr -> expr.accept(this));
        }

        public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
            xmlCommentLiteral.textFragments.forEach(expr -> expr.accept(this));
        }

        public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
            xmlProcInsLiteral.dataFragments.forEach(expr -> expr.accept(this));
            xmlProcInsLiteral.target.accept(this);
        }

        public void visit(BLangXMLQuotedString xmlQuotedString) {
            xmlQuotedString.textFragments.forEach(expr -> expr.accept(this));
        }

        public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
            stringTemplateLiteral.exprs.forEach(expr -> expr.accept(this));
        }

        public void visit(BLangWorkerSend workerSendNode) {
            workerSendNode.exprs.forEach(expr -> expr.accept(this));
        }

        public void visit(BLangWorkerReceive workerReceiveNode) {
            workerReceiveNode.exprs.forEach(expr -> expr.accept(this));
        }

        public void visit(BLangLambdaFunction bLangLambdaFunction) {
            // TODO: support for lambda
        }

        public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
            xmlAttributeAccessExpr.expr.accept(this);
            if (xmlAttributeAccessExpr.indexExpr != null) {
                xmlAttributeAccessExpr.indexExpr.accept(this);
            }
        }

        public void visit(BLangLiteral literalExpr) {
            // do nothing
        }
    }
}
