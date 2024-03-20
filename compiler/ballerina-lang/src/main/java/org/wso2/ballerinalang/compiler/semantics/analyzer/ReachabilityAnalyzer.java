/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.ballerinalang.util.diagnostic.DiagnosticWarningCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.SimpleBLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangCollectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupByClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupingKey;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangMatchClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMatchPattern;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDo;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangFail;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatchStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetryTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRollback;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.wso2.ballerinalang.compiler.util.Constants.WORKER_LAMBDA_VAR_PREFIX;

/**
 * Responsible for performing reachability analysis.
 *
 * @since 2.0.0
 */
public class ReachabilityAnalyzer extends SimpleBLangNodeAnalyzer<ReachabilityAnalyzer.AnalyzerData> {
    private static final CompilerContext.Key<ReachabilityAnalyzer> REACHABILITY_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private final SymbolResolver symResolver;
    private final SymbolTable symTable;
    private final TypeNarrower typeNarrower;
    private final Types types;
    private final BLangDiagnosticLog dlog;
    private final Names names;

    private ReachabilityAnalyzer(CompilerContext context) {
        context.put(REACHABILITY_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.typeNarrower = TypeNarrower.getInstance(context);
    }

    public static ReachabilityAnalyzer getInstance(CompilerContext context) {
        ReachabilityAnalyzer reachabilityAnalyzer = context.get(REACHABILITY_ANALYZER_KEY);
        if (reachabilityAnalyzer == null) {
            reachabilityAnalyzer = new ReachabilityAnalyzer(context);
        }
        return reachabilityAnalyzer;
    }

    void analyzeReachability(BLangNode node, SymbolEnv env) {
        final AnalyzerData data = new AnalyzerData();
        data.env = env;
        analyzeReachability(node, data);
    }

    private void analyzeReachability(BLangNode node, AnalyzerData data) {
        SymbolEnv prevEnv = data.env;
        visitNode(node, data);
        data.env = prevEnv;
    }

    private void analyzeReachabilityInExpressionIfApplicable(BLangExpression expr, AnalyzerData data) {
        if (expr == null) {
            return;
        }
        NodeKind exprKind = expr.getKind();
        switch (exprKind) {
            case DO_ACTION:
                analyzeReachability(expr, data);
                return;
            case CHECK_EXPR:
                analyzeReachabilityInExpressionIfApplicable(((BLangCheckedExpr) expr).expr, data);
                return;
            case CHECK_PANIC_EXPR:
                analyzeReachabilityInExpressionIfApplicable(((BLangCheckPanickedExpr) expr).expr, data);
                return;
            case TRAP_EXPR:
                analyzeReachabilityInExpressionIfApplicable(((BLangTrapExpr) expr).expr, data);
                return;
            case TYPE_CONVERSION_EXPR:
                analyzeReachabilityInExpressionIfApplicable(((BLangTypeConversionExpr) expr).expr, data);
                return;
            case GROUP_EXPR:
                analyzeReachabilityInExpressionIfApplicable(((BLangGroupExpr) expr).expression, data);
        }
    }

    @Override
    public void analyzeNode(BLangNode node, AnalyzerData data) {
        // Ignore
    }

    @Override
    public void visit(BLangPackage pkgNode, AnalyzerData data) {
        // Ignore
    }

    @Override
    public void visit(BLangBlockStmt blockNode, AnalyzerData data) {
        final SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, data.env);
        BType prevBoolConst = data.booleanConstCondition;
        data.isBlockUnreachable = false;
        for (BLangStatement stmt : blockNode.stmts) {
            data.env = blockEnv;
            analyzeReachability(stmt, data);
        }
        data.booleanConstCondition = prevBoolConst;
        resetUnreachableBlock(data);
        resetSkipFurtherAnalysisInUnreachableBlock(data);
    }

    @Override
    public void visit(BLangLock lockNode, AnalyzerData data) {
        boolean failureHandled = data.failureHandled;
        checkStatementExecutionValidity(lockNode, data);
        if (!data.failureHandled) {
            data.failureHandled = lockNode.onFailClause != null;
        }
        for (BLangStatement stmt : lockNode.body.stmts) {
            analyzeReachability(stmt, data);
        }
        data.failureHandled = failureHandled;
        analyzeOnFailClause(lockNode.onFailClause, data);
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode, AnalyzerData data) {
        checkStatementExecutionValidity(varDefNode, data);

        BLangExpression expr = varDefNode.var.expr;
        analyzeReachabilityInExpressionIfApplicable(expr, data);
    }

    @Override
    public void visit(BLangAssignment assignNode, AnalyzerData data) {
        checkStatementExecutionValidity(assignNode, data);
        analyzeReachabilityInExpressionIfApplicable(assignNode.expr, data);
        validateAssignmentToNarrowedVariable(assignNode.varRef, assignNode.pos, data);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignment, AnalyzerData data) {
        checkStatementExecutionValidity(compoundAssignment, data);
        validateAssignmentToNarrowedVariable(compoundAssignment.varRef, compoundAssignment.pos, data);
    }

    @Override
    public void visit(BLangContinue continueNode, AnalyzerData data) {
        checkStatementExecutionValidity(continueNode, data);
        data.continueAsLastStatement = data.loopCount > 0;
    }

    @Override
    public void visit(BLangBreak breakNode, AnalyzerData data) {
        checkStatementExecutionValidity(breakNode, data);
        data.breakAsLastStatement = data.loopCount > 0;
        data.breakStmtFound = true;
    }

    @Override
    public void visit(BLangReturn returnStmt, AnalyzerData data) {
        checkStatementExecutionValidity(returnStmt, data);
        analyzeReachabilityInExpressionIfApplicable(returnStmt.expr, data);
        data.statementReturnsPanicsOrFails = true;
        data.returnedWithinQuery = true;
    }

    @Override
    public void visit(BLangPanic panicNode, AnalyzerData data) {
        data.statementReturnsPanicsOrFails = true;
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode, AnalyzerData data) {
        checkStatementExecutionValidity(xmlnsStmtNode, data);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode, AnalyzerData data) {
        checkStatementExecutionValidity(exprStmtNode, data);
        BLangExpression expr = exprStmtNode.expr;
        analyzeReachabilityInExpressionIfApplicable(expr, data);
        if (expr.getKind() == NodeKind.INVOCATION &&
                types.isNeverTypeOrStructureTypeWithARequiredNeverMember(expr.getBType())) {
            data.statementReturnsPanicsOrFails = true;
        }
    }

    @Override
    public void visit(BLangIf ifStmt, AnalyzerData data) {
        checkStatementExecutionValidity(ifStmt, data);

        data.potentiallyInvalidAssignmentInLoopsInfo.add(new PotentiallyInvalidAssignmentInfo(new ArrayList<>(),
                data.env.enclInvokable));
        data.unreachableBlock = data.unreachableBlock || data.booleanConstCondition == symTable.falseType;
        analyzeReachability(ifStmt.body, data);

        boolean allBranchesTerminate = data.breakAsLastStatement || data.statementReturnsPanicsOrFails;
        handlePotentiallyInvalidAssignmentsToTypeNarrowedVariablesInLoop(allBranchesTerminate,
                data.continueAsLastStatement, data.potentiallyInvalidAssignmentInLoopsInfo);

        boolean ifStmtReturnsPanicsOrFails = data.statementReturnsPanicsOrFails;
        boolean currentErrorThrown = data.errorThrown;
        boolean ifStmtBreakAsLastStatement = data.breakAsLastStatement;
        boolean ifStmtContinueAsLastStatement = data.continueAsLastStatement;

        BLangStatement elseStmt = ifStmt.elseStmt;

        if (data.booleanConstCondition != symTable.trueType || (elseStmt != null &&
                elseStmt.getKind() == NodeKind.IF)) {
            resetStatementReturnsPanicsOrFails(data);
            resetErrorThrown(data);
            resetLastStatement(data);
        }

        if (elseStmt != null) {
            data.potentiallyInvalidAssignmentInLoopsInfo.add(new PotentiallyInvalidAssignmentInfo(new ArrayList<>(),
                    data.env.enclInvokable));

            data.unreachableBlock = data.unreachableBlock || (data.booleanConstCondition == symTable.trueType &&
                    elseStmt.getKind() != NodeKind.IF);
            analyzeReachability(elseStmt, data);
            resetUnreachableBlock(data);
            resetSkipFurtherAnalysisInUnreachableBlock(data);

            handlePotentiallyInvalidAssignmentsToTypeNarrowedVariablesInLoop(
                    data.breakAsLastStatement || data.statementReturnsPanicsOrFails,
                    data.continueAsLastStatement, data.potentiallyInvalidAssignmentInLoopsInfo);

            if (data.booleanConstCondition == symTable.trueType) {
                if (elseStmt.getKind() != NodeKind.IF) {
                    data.statementReturnsPanicsOrFails = checkAllBranchesTerminate(ifStmtReturnsPanicsOrFails,
                            ifStmtBreakAsLastStatement, ifStmtContinueAsLastStatement, data);
                }
                resetErrorThrown(data);
            }

            if (data.booleanConstCondition == symTable.semanticError) {
                data.statementReturnsPanicsOrFails = checkAllBranchesTerminate(ifStmtReturnsPanicsOrFails,
                        ifStmtBreakAsLastStatement, ifStmtContinueAsLastStatement, data);
                data.errorThrown = currentErrorThrown && data.errorThrown;
                data.breakAsLastStatement = ifStmtBreakAsLastStatement && data.breakAsLastStatement;
                data.continueAsLastStatement = ifStmtContinueAsLastStatement && data.continueAsLastStatement;
            }
        }
    }

    private boolean checkAllBranchesTerminate(boolean ifStmtReturnsPanicsOrFails, boolean ifStmtBreakAsLastStatement,
                                              boolean ifStmtContinueAsLastStatement, AnalyzerData data) {
        return (ifStmtReturnsPanicsOrFails || ifStmtBreakAsLastStatement
                || ifStmtContinueAsLastStatement) && (data.statementReturnsPanicsOrFails
                || data.breakAsLastStatement || data.continueAsLastStatement);
    }

    @Override
    public void visit(BLangDo doNode, AnalyzerData data) {
        boolean failureHandled = data.failureHandled;
        checkStatementExecutionValidity(doNode, data);
        if (!data.failureHandled) {
            data.failureHandled = doNode.onFailClause != null;
        }
        analyzeReachability(doNode.body, data);
        data.failureHandled = failureHandled;
        analyzeOnFailClause(doNode.onFailClause, data);
    }

    @Override
    public void visit(BLangErrorDestructure errorDestructureStmt, AnalyzerData data) {
        checkStatementExecutionValidity(errorDestructureStmt, data);
        analyzeReachabilityInExpressionIfApplicable(errorDestructureStmt.expr, data);
        validateAssignmentToNarrowedVariables(getVarRefs(errorDestructureStmt.varRef), errorDestructureStmt.pos, data);
    }

    @Override
    public void visit(BLangErrorVariableDef errorVariableDef, AnalyzerData data) {
        checkStatementExecutionValidity(errorVariableDef, data);
        BLangExpression expr = errorVariableDef.errorVariable.expr;
        analyzeReachabilityInExpressionIfApplicable(expr, data);
    }

    @Override
    public void visit(BLangFail failNode, AnalyzerData data) {
        checkStatementExecutionValidity(failNode, data);
        data.errorThrown = data.booleanConstCondition == symTable.semanticError;
        if (!data.failureHandled) {
            data.statementReturnsPanicsOrFails = true;
        }
        data.returnedWithinQuery = true;
    }

    @Override
    public void visit(BLangForeach foreach, AnalyzerData data) {
        SymbolEnv foreachEnv = SymbolEnv.createLoopEnv(foreach, data.env);
        data.loopAndDoClauseEnvs.add(foreachEnv);

        data.potentiallyInvalidAssignmentInLoopsInfo.add(new PotentiallyInvalidAssignmentInfo(new ArrayList<>(),
                data.env.enclInvokable));

        boolean prevStatementReturnsPanicsOrFails = data.statementReturnsPanicsOrFails;
        boolean prevBreakAsLastStatement = data.breakAsLastStatement;
        boolean prevContinueAsLastStatement = data.continueAsLastStatement;
        boolean prevBreakStmtFound = data.breakStmtFound;
        boolean failureHandled = data.failureHandled;

        checkStatementExecutionValidity(foreach, data);

        if (!data.failureHandled) {
            data.failureHandled = foreach.onFailClause != null;
        }

        data.breakStmtFound = false;
        incrementLoopCount(data);
        data.env = foreachEnv;
        analyzeReachability(foreach.body, data);

        handlePotentiallyInvalidAssignmentsToTypeNarrowedVariablesInLoop(
                data.breakAsLastStatement || data.statementReturnsPanicsOrFails, true,
                data.potentiallyInvalidAssignmentInLoopsInfo);

        decrementLoopCount(data);
        data.failureHandled = failureHandled;
        data.continueAsLastStatement = prevContinueAsLastStatement;
        data.breakAsLastStatement = prevBreakAsLastStatement;
        data.statementReturnsPanicsOrFails = prevStatementReturnsPanicsOrFails;
        data.breakStmtFound = prevBreakStmtFound;

        analyzeOnFailClause(foreach.onFailClause, data);

        data.loopAndDoClauseEnvs.pop();
    }

    @Override
    public void visit(BLangForkJoin forkJoin, AnalyzerData data) {
    }

    @Override
    public void visit(BLangMatchStatement matchStatement, AnalyzerData data) {
        checkStatementExecutionValidity(matchStatement, data);
        analyzeReachabilityInExpressionIfApplicable(matchStatement.expr, data);
        if (!data.failureHandled) {
            data.failureHandled = matchStatement.onFailClause != null;
        }
        boolean currentErrorThrown = data.errorThrown;
        boolean hasLastPatternInStatement = data.hasLastPatternInStatement;
        data.hasLastPatternInStatement = false;
        boolean allClausesReturns = true;
        boolean allClausesBreak = true;
        boolean allClausesContinue = true;
        List<BLangMatchClause> matchClauses = matchStatement.matchClauses;
        for (BLangMatchClause matchClause : matchClauses) {
            resetErrorThrown(data);
            data.potentiallyInvalidAssignmentInLoopsInfo.add(new PotentiallyInvalidAssignmentInfo(new ArrayList<>(),
                    data.env.enclInvokable));
            analyzeReachability(matchClause, data);
            handlePotentiallyInvalidAssignmentsToTypeNarrowedVariablesInLoop(
                    data.breakAsLastStatement || data.statementReturnsPanicsOrFails,
                    data.continueAsLastStatement, data.potentiallyInvalidAssignmentInLoopsInfo);
            allClausesReturns &= data.statementReturnsPanicsOrFails;
            allClausesBreak &= data.breakAsLastStatement;
            allClausesContinue &= data.continueAsLastStatement;
            this.resetStatementReturnsPanicsOrFails(data);
            this.resetLastStatement(data);
        }
        data.statementReturnsPanicsOrFails = allClausesReturns && data.hasLastPatternInStatement;
        data.breakAsLastStatement = allClausesBreak && data.hasLastPatternInStatement;
        data.continueAsLastStatement = allClausesContinue && data.hasLastPatternInStatement;
        data.errorThrown = currentErrorThrown;
        analyzeOnFailClause(matchStatement.onFailClause, data);
        data.hasLastPatternInStatement = hasLastPatternInStatement;
    }

    @Override
    public void visit(BLangMatchClause matchClause, AnalyzerData data) {
        boolean hasLastPatternInClause = false;
        List<BLangMatchPattern> matchPatterns = matchClause.matchPatterns;
        for (BLangMatchPattern matchPattern : matchPatterns) {
            if (data.hasLastPatternInStatement || (hasLastPatternInClause && matchClause.matchGuard == null)) {
                dlog.warning(matchPattern.pos, DiagnosticWarningCode.MATCH_STMT_PATTERN_UNREACHABLE);
            }
            hasLastPatternInClause = hasLastPatternInClause || matchPattern.isLastPattern;
        }
        analyzeReachability(matchClause.blockStmt, data);
        data.hasLastPatternInStatement = data.hasLastPatternInStatement ||
                (matchClause.matchGuard == null && hasLastPatternInClause);
    }

    @Override
    public void visit(BLangRecordDestructure recordDestructureStmt, AnalyzerData data) {
        checkStatementExecutionValidity(recordDestructureStmt, data);
        validateAssignmentToNarrowedVariables(getVarRefs(recordDestructureStmt.varRef), recordDestructureStmt.pos,
                data);
    }

    @Override
    public void visit(BLangRecordVariableDef recordVariableDef, AnalyzerData data) {
        checkStatementExecutionValidity(recordVariableDef, data);
    }

    @Override
    public void visit(BLangRollback rollbackNode, AnalyzerData data) {
        checkStatementExecutionValidity(rollbackNode, data);
    }

    @Override
    public void visit(BLangTransaction transactionNode, AnalyzerData data) {
        checkStatementExecutionValidity(transactionNode, data);
        boolean failureHandled = data.failureHandled;
        if (!data.failureHandled) {
            data.failureHandled = transactionNode.onFailClause != null;
        }
        analyzeReachability(transactionNode.transactionBody, data);
        data.failureHandled = failureHandled;
        analyzeOnFailClause(transactionNode.onFailClause, data);
    }

    private void analyzeOnFailClause(BLangOnFailClause onFailClause, AnalyzerData data) {
        if (onFailClause == null) {
            return;
        }
        boolean currentStatementReturns = data.statementReturnsPanicsOrFails;
        data.booleanConstCondition = symTable.semanticError;
        resetStatementReturnsPanicsOrFails(data);
        resetLastStatement(data);
        resetUnreachableBlock(data);
        analyzeReachability(onFailClause, data);
        data.statementReturnsPanicsOrFails = currentStatementReturns && data.statementReturnsPanicsOrFails;
    }

    @Override
    public void visit(BLangCollectClause node, AnalyzerData data) {

    }

    @Override
    public void visit(BLangRetry retryNode, AnalyzerData data) {
        boolean failureHandled = data.failureHandled;
        checkStatementExecutionValidity(retryNode, data);
        if (!data.failureHandled) {
            data.failureHandled = retryNode.onFailClause != null;
        }
        analyzeReachability(retryNode.retryBody, data);
        data.failureHandled = failureHandled;
        resetLastStatement(data);
        resetErrorThrown(data);
        analyzeOnFailClause(retryNode.onFailClause, data);
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction, AnalyzerData data) {
        analyzeReachability(retryTransaction.transaction, data);
    }

    @Override
    public void visit(BLangOnFailClause onFailClause, AnalyzerData data) {
        resetLastStatement(data);
        resetErrorThrown(data);
        analyzeReachability(onFailClause.body, data);
        resetErrorThrown(data);
    }

    @Override
    public void visit(BLangGroupByClause node, AnalyzerData data) {

    }

    @Override
    public void visit(BLangGroupingKey node, AnalyzerData data) {

    }

    @Override
    public void visit(BLangFunction funcNode, AnalyzerData data) {
        resetFunction(data);
        if (funcNode.flagSet.contains(Flag.NATIVE)) {
            return;
        }
        if (funcNode.body != null) {
            analyzeReachability(funcNode.body, data);
            boolean isNeverReturn = types.isNeverTypeOrStructureTypeWithARequiredNeverMember
                    (funcNode.symbol.type.getReturnType());
            // If the return signature is nil-able, an implicit return will be added in Desugar.
            // Hence, this only checks for non-nil-able return signatures and uncertain return in the body.
            if (!funcNode.symbol.type.getReturnType().isNullable() && !isNeverReturn &&
                    !data.hasFunctionTerminated) {
                Location closeBracePos = getEndCharPos(funcNode.pos);
                this.dlog.error(closeBracePos, DiagnosticErrorCode.INVOKABLE_MUST_RETURN,
                        funcNode.getKind().toString().toLowerCase());
            } else if (isNeverReturn && !data.hasFunctionTerminated) {
                this.dlog.error(funcNode.pos, DiagnosticErrorCode.THIS_FUNCTION_SHOULD_PANIC);
            }
        }

        BType returnType = Types.getImpliedType(funcNode.returnTypeNode.getBType());
        if (!funcNode.interfaceFunction && returnType.tag == TypeTags.UNION) {
            if (types.getAllTypes(returnType, true).contains(symTable.nilType) &&
                    !types.isSubTypeOfErrorOrNilContainingNil((BUnionType) returnType) &&
                    !data.statementReturnsPanicsOrFails) {
                this.dlog.warning(funcNode.returnTypeNode.pos,
                        DiagnosticWarningCode.FUNCTION_SHOULD_EXPLICITLY_RETURN_A_VALUE);
            }
        }
    }

    private Location getEndCharPos(Location pos) {
        LineRange lineRange = pos.lineRange();
        LinePosition endLinePos = lineRange.endLine();
        return new BLangDiagnosticLocation(lineRange.fileName(), endLinePos.line(), endLinePos.line(),
                endLinePos.offset() - 1, endLinePos.offset(),
                pos.textRange().startOffset() + pos.textRange().length() - 1, 1);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction, AnalyzerData data) {
        boolean prevStatementReturnsPanicsOrFails = data.statementReturnsPanicsOrFails;
        boolean prevBreakAsLastStatement = data.breakAsLastStatement;
        boolean prevContinueAsLastStatement = data.continueAsLastStatement;
        if (bLangLambdaFunction.parent.getKind() == NodeKind.VARIABLE &&
                (((BLangSimpleVariable) bLangLambdaFunction.parent).name.value).startsWith(WORKER_LAMBDA_VAR_PREFIX)) {
            BLangFunction function = bLangLambdaFunction.function;
            data.env = SymbolEnv.createFunctionEnv(function, function.symbol.scope, data.env);
            analyzeReachability(function, data);
        }
        data.continueAsLastStatement = prevContinueAsLastStatement;
        data.breakAsLastStatement = prevBreakAsLastStatement;
        data.statementReturnsPanicsOrFails = prevStatementReturnsPanicsOrFails;
    }

    @Override
    public void visit(BLangExternalFunctionBody body, AnalyzerData data) {
    }

    public void visit(BLangResourceFunction resourceFunction, AnalyzerData data) {
        visit((BLangFunction) resourceFunction, data);
    }

    @Override
    public void visit(BLangTupleDestructure tupleDestructureStmt, AnalyzerData data) {
        checkStatementExecutionValidity(tupleDestructureStmt, data);
        validateAssignmentToNarrowedVariables(getVarRefs(tupleDestructureStmt.varRef), tupleDestructureStmt.pos, data);
    }

    @Override
    public void visit(BLangTupleVariableDef tupleVariableDef, AnalyzerData data) {
        checkStatementExecutionValidity(tupleVariableDef, data);
    }

    @Override
    public void visit(BLangWhile whileNode, AnalyzerData data) {
        SymbolEnv whileEnv = SymbolEnv.createLoopEnv(whileNode, data.env);
        data.loopAndDoClauseEnvs.add(whileEnv);

        data.potentiallyInvalidAssignmentInLoopsInfo.add(new PotentiallyInvalidAssignmentInfo(new ArrayList<>(),
                data.env.enclInvokable));

        boolean prevStatementReturnsPanicsOrFails = data.statementReturnsPanicsOrFails;
        boolean prevBreakAsLastStatement = data.breakAsLastStatement;
        boolean prevContinueAsLastStatement = data.continueAsLastStatement;
        boolean prevBreakStmtFound = data.breakStmtFound;
        boolean failureHandled = data.failureHandled;

        checkStatementExecutionValidity(whileNode, data);

        if (!data.failureHandled) {
            data.failureHandled = whileNode.onFailClause != null;
        }

        incrementLoopCount(data);
        data.breakStmtFound = false;
        data.unreachableBlock = data.unreachableBlock || data.booleanConstCondition == symTable.falseType;
        data.env = whileEnv;
        analyzeReachability(whileNode.body, data);
        resetUnreachableBlock(data);
        resetSkipFurtherAnalysisInUnreachableBlock(data);

        handlePotentiallyInvalidAssignmentsToTypeNarrowedVariablesInLoop(
                data.breakAsLastStatement || data.statementReturnsPanicsOrFails, true,
                data.potentiallyInvalidAssignmentInLoopsInfo);

        decrementLoopCount(data);
        data.failureHandled = failureHandled;
        if (data.booleanConstCondition != symTable.trueType || data.breakStmtFound) {
            data.statementReturnsPanicsOrFails = prevStatementReturnsPanicsOrFails;
            data.continueAsLastStatement = prevContinueAsLastStatement;
            data.breakAsLastStatement = prevBreakAsLastStatement;
        } else {
            data.statementReturnsPanicsOrFails = true;
        }
        data.breakStmtFound = prevBreakStmtFound;

        analyzeOnFailClause(whileNode.onFailClause, data);

        data.loopAndDoClauseEnvs.pop();
    }

    @Override
    public void visit(BLangBlockFunctionBody body, AnalyzerData data) {
        final SymbolEnv blockEnv = SymbolEnv.createFuncBodyEnv(body, data.env);
        boolean hasFunctionTerminated = false;
        for (BLangStatement stmt : body.stmts) {
            data.env = blockEnv;
            analyzeReachability(stmt, data);
            hasFunctionTerminated |=
                    data.statementReturnsPanicsOrFails || (data.isBlockUnreachable && stmt.getKind() == NodeKind.BLOCK);
        }
        data.hasFunctionTerminated = hasFunctionTerminated;
    }

    @Override
    public void visit(BLangExprFunctionBody body, AnalyzerData data) {
        data.statementReturnsPanicsOrFails = true;
        data.hasFunctionTerminated = true;
        resetLastStatement(data);
    }

    @Override
    public void visit(BLangQueryAction queryAction, AnalyzerData data) {
        boolean prevReturnedWithinQuery = data.returnedWithinQuery;
        data.returnedWithinQuery = false;
        for (BLangNode queryClause : queryAction.queryClauseList) {
            if (queryClause.getKind() == NodeKind.WHERE) {
                BLangWhereClause whereClause = (BLangWhereClause) queryClause;
                data.unreachableBlock = ConditionResolver.checkConstCondition(types, symTable,
                        whereClause.expression) == symTable.falseType;
                if (data.unreachableBlock) {
                    break;
                }
            }
        }
        analyzeReachability(queryAction.getDoClause(), data);
        queryAction.returnsWithinDoClause = data.returnedWithinQuery;
        data.returnedWithinQuery = prevReturnedWithinQuery;
    }

    @Override
    public void visit(BLangDoClause doClause, AnalyzerData data) {
        SymbolEnv doEnv = doClause.env;
        data.loopAndDoClauseEnvs.add(doEnv);

        data.loopAndDoClauseCount++;
        data.potentiallyInvalidAssignmentInLoopsInfo.add(new PotentiallyInvalidAssignmentInfo(new ArrayList<>(),
                data.env.enclInvokable));

        BLangBlockStmt body = doClause.body;
        data.env = doEnv;
        analyzeReachability(body, data);

        handlePotentiallyInvalidAssignmentsToTypeNarrowedVariablesInLoop(
                data.statementReturnsPanicsOrFails, true,
                DiagnosticErrorCode.INVALID_ASSIGNMENT_TO_NARROWED_VAR_IN_QUERY_ACTION,
                data.potentiallyInvalidAssignmentInLoopsInfo);
        data.loopAndDoClauseCount--;
        data.loopAndDoClauseEnvs.pop();
        resetStatementReturnsPanicsOrFails(data);
        resetLastStatement(data);
    }

    @Override
    public void visit(BLangLetExpression letExpression, AnalyzerData data) {
        // This is to support when let expressions are used in return statements
        // Since variable declarations are visited after return node, this stops false positive unreachable code error
        boolean returnStateBefore = data.statementReturnsPanicsOrFails;
        data.statementReturnsPanicsOrFails = false;
        for (BLangLetVariable letVariable : letExpression.letVarDeclarations) {
            analyzeReachability((BLangNode) letVariable.definitionNode, data);
        }
        data.statementReturnsPanicsOrFails = returnStateBefore;
    }

    private void checkStatementExecutionValidity(BLangStatement statement, AnalyzerData data) {
        if (data.skipFurtherAnalysisInUnreachableBlock) {
            return;
        }
        checkUnreachableCode(statement.pos, data);
        checkConditionInWhileOrIf(statement, data);
    }

    private void checkConditionInWhileOrIf(BLangStatement statement, AnalyzerData data) {
        switch (statement.getKind()) {
            case WHILE:
                data.booleanConstCondition = ConditionResolver.checkConstCondition(types, symTable,
                        ((BLangWhile) statement).expr);
                break;
            case IF:
                data.unreachableBlock = statement.parent != null && statement.parent.getKind() == NodeKind.IF
                        && data.booleanConstCondition == symTable.trueType;
                data.booleanConstCondition = ConditionResolver.checkConstCondition(types, symTable,
                        ((BLangIf) statement).expr);
                break;
        }
    }

    private void checkUnreachableCode(Location pos, AnalyzerData data) {
        if (data.statementReturnsPanicsOrFails) {
            logUnreachableError(pos, data);
            resetStatementReturnsPanicsOrFails(data);
        } else if (data.errorThrown) {
            logUnreachableError(pos, data);
            resetErrorThrown(data);
        } else if (data.breakAsLastStatement || data.continueAsLastStatement) {
            logUnreachableError(pos, data);
            resetLastStatement(data);
        } else if (data.unreachableBlock) {
            data.skipFurtherAnalysisInUnreachableBlock = true;
            logUnreachableError(pos, data);
            resetUnreachableBlock(data);
        }
    }

    private void logUnreachableError(Location pos, AnalyzerData data) {
        dlog.error(pos, DiagnosticErrorCode.UNREACHABLE_CODE);
        data.isBlockUnreachable = true;
    }

    private void resetStatementReturnsPanicsOrFails(AnalyzerData data) {
        data.statementReturnsPanicsOrFails = false;
    }

    private void resetLastStatement(AnalyzerData data) {
        data.breakAsLastStatement = false;
        data.continueAsLastStatement = false;
    }

    private void resetErrorThrown(AnalyzerData data) {
        data.errorThrown = false;
    }

    private void resetUnreachableBlock(AnalyzerData data) {
        data.unreachableBlock = false;
    }

    private void resetFunction(AnalyzerData data) {
        resetStatementReturnsPanicsOrFails(data);
        resetErrorThrown(data);
        resetLastStatement(data);
        data.booleanConstCondition = symTable.semanticError;
    }

    private void resetSkipFurtherAnalysisInUnreachableBlock(AnalyzerData data) {
        data.skipFurtherAnalysisInUnreachableBlock = false;
    }

    private void validateAssignmentToNarrowedVariables(List<BLangExpression> exprs, Location location,
                                                       AnalyzerData data) {
        for (BLangExpression expr : exprs) {
            if (expr == null) {
                continue;
            }

            switch (expr.getKind()) {
                case SIMPLE_VARIABLE_REF:
                    validateAssignmentToNarrowedVariable(expr, location, data);
                    continue;
                case RECORD_VARIABLE_REF:
                    validateAssignmentToNarrowedVariables(getVarRefs((BLangRecordVarRef) expr), location, data);
                    continue;
                case TUPLE_VARIABLE_REF:
                    validateAssignmentToNarrowedVariables(getVarRefs((BLangTupleVarRef) expr), location, data);
                    continue;
                case ERROR_VARIABLE_REF:
                    validateAssignmentToNarrowedVariables(getVarRefs((BLangErrorVarRef) expr), location, data);
            }
        }
    }

    private void validateAssignmentToNarrowedVariable(BLangExpression expr, Location location, AnalyzerData data) {
        if (expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return;
        }

        BLangSimpleVarRef varRef = (BLangSimpleVarRef) expr;
        BSymbol symbol = varRef.symbol;
        if (symbol == null || ((BVarSymbol) symbol).originalSymbol == null) {
            return;
        }

        if (data.loopAndDoClauseCount == 0) {
            return;
        }

        validateAssignmentToNarrowedVariable(varRef, location, data);
    }

    private void validateAssignmentToNarrowedVariable(BLangSimpleVarRef varRef, Location location, AnalyzerData data) {
        if (Symbols.isFlagOn(varRef.symbol.flags, Flags.FINAL)) {
            return;
        }

        Name name = names.fromIdNode(varRef.variableName);
        SymbolEnv loopEnv = data.loopAndDoClauseEnvs.peek();
        SymbolEnv currentEnv = data.env;

        while (currentEnv != null) {
            BSymbol foundSym = symResolver.lookupSymbolInMainSpace(currentEnv, name);

            if (foundSym != symTable.notFoundSymbol && foundSym.tag == SymTag.VARIABLE &&
                    ((BVarSymbol) foundSym).originalSymbol == null) {
                return;
            }

            if (currentEnv == loopEnv) {
                BLangNode loopNode = loopEnv.node;
                if (loopNode.getKind() == NodeKind.WHILE &&
                        ((BLangWhile) loopNode).expr.narrowedTypeInfo.containsKey(
                                typeNarrower.getOriginalVarSymbol((BVarSymbol) varRef.symbol))) {
                    // A while loop may narrow an already narrowed variable, so checking specifically if the loop
                    // itself narrows the variable too.
                    return;
                }

                break;
            }

            currentEnv = currentEnv.enclEnv;
        }

        data.potentiallyInvalidAssignmentInLoopsInfo.peek().locations.add(location);
    }

    private void handleInvalidAssignmentToTypeNarrowedVariableInLoop(List<Location> locations,
                                                                     DiagnosticErrorCode errorCode) {
        for (Location location : locations) {
            dlog.error(location, errorCode);
        }
    }

    private void handlePotentiallyInvalidAssignmentsToTypeNarrowedVariablesInLoop(
            boolean branchTerminates, boolean isLoopBodyOrBranchWithContinueAsLastStmt,
            Stack<PotentiallyInvalidAssignmentInfo> potentiallyInvalidAssignmentInLoopsInfo) {
        handlePotentiallyInvalidAssignmentsToTypeNarrowedVariablesInLoop(
                branchTerminates, isLoopBodyOrBranchWithContinueAsLastStmt,
                DiagnosticErrorCode.INVALID_ASSIGNMENT_TO_NARROWED_VAR_IN_LOOP,
                potentiallyInvalidAssignmentInLoopsInfo);
    }

    private void handlePotentiallyInvalidAssignmentsToTypeNarrowedVariablesInLoop(
            boolean branchTerminates, boolean isLoopBodyOrBranchWithContinueAsLastStmt, DiagnosticErrorCode errorCode,
            Stack<PotentiallyInvalidAssignmentInfo> potentiallyInvalidAssignmentInLoopsInfo) {

        PotentiallyInvalidAssignmentInfo
                currentBranchInfo = potentiallyInvalidAssignmentInLoopsInfo.pop();

        if (branchTerminates) {
            return;
        }

        List<Location> currentBranchLocations = currentBranchInfo.locations;

        if (isLoopBodyOrBranchWithContinueAsLastStmt) {
            handleInvalidAssignmentToTypeNarrowedVariableInLoop(currentBranchLocations, errorCode);
            return;
        }

        if (currentBranchLocations.isEmpty() || potentiallyInvalidAssignmentInLoopsInfo.empty()) {
            return;
        }

        PotentiallyInvalidAssignmentInfo prevInfo = potentiallyInvalidAssignmentInLoopsInfo.peek();

        if (prevInfo.enclInvokable != currentBranchInfo.enclInvokable) {
            return;
        }

        prevInfo.locations.addAll(currentBranchLocations);
    }

    private List<BLangExpression> getVarRefs(BLangRecordVarRef varRef) {
        List<BLangExpression> varRefs = varRef.recordRefFields.stream()
                .map(e -> e.variableReference).collect(Collectors.toList());
        varRefs.add(varRef.restParam);
        return varRefs;
    }

    private List<BLangExpression> getVarRefs(BLangErrorVarRef varRef) {
        List<BLangExpression> varRefs = new ArrayList<>();
        if (varRef.message != null) {
            varRefs.add(varRef.message);
        }
        if (varRef.cause != null) {
            varRefs.add(varRef.cause);
        }
        varRefs.addAll(varRef.detail.stream().map(e -> e.expr).collect(Collectors.toList()));
        varRefs.add(varRef.restVar);
        return varRefs;
    }

    private List<BLangExpression> getVarRefs(BLangTupleVarRef varRef) {
        List<BLangExpression> varRefs = new ArrayList<>(varRef.expressions);
        varRefs.add(varRef.restParam);
        return varRefs;
    }

    private void incrementLoopCount(AnalyzerData data) {
        data.loopCount++;
        data.loopAndDoClauseCount++;
    }

    private void decrementLoopCount(AnalyzerData data) {
        data.loopCount--;
        data.loopAndDoClauseCount--;
    }

    private static class PotentiallyInvalidAssignmentInfo {
        List<Location> locations;
        BLangInvokableNode enclInvokable;

        private PotentiallyInvalidAssignmentInfo(List<Location>  locations,
                                                 BLangInvokableNode enclInvokable) {
            this.locations = locations;
            this.enclInvokable = enclInvokable;
        }
    }

    /**
     * @since 2.0.0
     */
    public static class AnalyzerData {
        SymbolEnv env;
        boolean statementReturnsPanicsOrFails;
        boolean breakAsLastStatement;
        boolean continueAsLastStatement;
        boolean errorThrown;
        boolean unreachableBlock;
        boolean breakStmtFound;
        boolean hasLastPatternInStatement;
        boolean failureHandled;
        boolean returnedWithinQuery;
        boolean skipFurtherAnalysisInUnreachableBlock;
        boolean hasFunctionTerminated;
        boolean isBlockUnreachable;
        int loopCount;
        int loopAndDoClauseCount;
        BType booleanConstCondition;

        Stack<SymbolEnv> loopAndDoClauseEnvs = new Stack<>();
        Stack<PotentiallyInvalidAssignmentInfo> potentiallyInvalidAssignmentInLoopsInfo = new Stack<>();
    }
}
