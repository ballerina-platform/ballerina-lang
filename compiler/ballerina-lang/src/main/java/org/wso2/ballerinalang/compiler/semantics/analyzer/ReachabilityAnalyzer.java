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
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.ballerinalang.util.diagnostic.DiagnosticWarningCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangMatchClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.LinkedHashSet;
import java.util.List;

import static org.wso2.ballerinalang.compiler.util.Constants.WORKER_LAMBDA_VAR_PREFIX;

/**
 * Responsible for performing reachability analysis.
 *
 * @since 2.0.0
 */
public class ReachabilityAnalyzer extends BLangNodeVisitor {
    private static final CompilerContext.Key<ReachabilityAnalyzer> REACHABILITY_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private final SymbolTable symTable;
    private final Types types;
    private final BLangDiagnosticLog dlog;
    private boolean statementReturnsPanicsOrFails;
    private boolean lastStatement;
    private boolean errorThrown;
    private boolean inWhileOrIfBlock;
    private boolean inElseBlock;
    private boolean unreachableBlock;
    private boolean breakStmtFound;
    private boolean hasLastPatternInStatement;
    private boolean failureHandled;
    private boolean loopFound;
    private BooleanConst  booleanConstCondition = BooleanConst.NOT_CONST_BOOLEAN;

    private ReachabilityAnalyzer(CompilerContext context) {
        context.put(REACHABILITY_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public static ReachabilityAnalyzer getInstance(CompilerContext context) {
        ReachabilityAnalyzer reachabilityAnalyzer = context.get(REACHABILITY_ANALYZER_KEY);
        if (reachabilityAnalyzer == null) {
            reachabilityAnalyzer = new ReachabilityAnalyzer(context);
        }
        return reachabilityAnalyzer;
    }

    void analyzeReachability(BLangNode node) {
        node.accept(this);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        BooleanConst prevBoolConst = this.booleanConstCondition;
        checkBlockStmtExecutionValidity(blockNode);
        for (BLangStatement stmt : blockNode.stmts) {
            analyzeReachability(stmt);
        }
        if (booleanConstCondition != BooleanConst.TRUE) {
            resetLastStatement();
        }
        this.booleanConstCondition = prevBoolConst;
        resetUnreachableBlock();
    }

    @Override
    public void visit(BLangLock lockNode) {
        boolean failureHandled = this.failureHandled;
        checkStatementExecutionValidity(lockNode);
        if (!this.failureHandled) {
            this.failureHandled = lockNode.onFailClause != null;
        }
        for (BLangStatement stmt : lockNode.body.stmts) {
            analyzeReachability(stmt);
        }
        this.failureHandled = failureHandled;
        analyzeOnFailClause(lockNode.onFailClause);
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        checkStatementExecutionValidity(varDefNode);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        checkStatementExecutionValidity(assignNode);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignment) {
        checkStatementExecutionValidity(compoundAssignment);
    }

    @Override
    public void visit(BLangContinue continueNode) {
        checkStatementExecutionValidity(continueNode);
        this.lastStatement = this.loopFound;
    }

    @Override
    public void visit(BLangBreak breakNode) {
        checkStatementExecutionValidity(breakNode);
        this.lastStatement = this.loopFound;
        this.breakStmtFound = true;
    }

    @Override
    public void visit(BLangReturn returnStmt) {
        checkStatementExecutionValidity(returnStmt);
        this.statementReturnsPanicsOrFails = true;
    }

    @Override
    public void visit(BLangPanic panicNode) {
        checkStatementExecutionValidity(panicNode);
        this.statementReturnsPanicsOrFails = true;
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        checkStatementExecutionValidity(xmlnsStmtNode);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        checkStatementExecutionValidity(exprStmtNode);
        if (exprStmtNode.expr.getKind() == NodeKind.INVOCATION) {
            BLangInvocation invocation = (BLangInvocation) exprStmtNode.expr;
            if (invocation.symbol != null && !((invocation.symbol.flags & Flags.LANG_LIB) ==
                    Flags.LANG_LIB && invocation.name.value.equals("unreachable")) &&
                    types.isNeverTypeOrStructureTypeWithARequiredNeverMember(invocation.getBType())) {
                this.statementReturnsPanicsOrFails = true;
            }
        }
    }

    @Override
    public void visit(BLangIf ifStmt) {
        checkStatementExecutionValidity(ifStmt);
        this.inWhileOrIfBlock = true;
        this.inElseBlock = false;
        visit(ifStmt.body);
        boolean ifStmtReturnsPanicsOrFails = this.statementReturnsPanicsOrFails;
        boolean currentErrorThrown = this.errorThrown;
        if (booleanConstCondition != BooleanConst.TRUE) {
            resetStatementReturnsPanicsOrFails();
            resetErrorThrown();
            resetLastStatement();
        }
        if (ifStmt.elseStmt != null) {
            this.inWhileOrIfBlock = false;
            this.inElseBlock = ifStmt.elseStmt.getKind() != NodeKind.IF;
            analyzeReachability(ifStmt.elseStmt);
            if (booleanConstCondition == BooleanConst.NOT_CONST_BOOLEAN) {
                this.statementReturnsPanicsOrFails = ifStmtReturnsPanicsOrFails && this.statementReturnsPanicsOrFails;
                this.errorThrown = currentErrorThrown && this.errorThrown;
            }
        }
        this.inWhileOrIfBlock = false;
        this.inElseBlock = false;
    }

    @Override
    public void visit(BLangDo doNode) {
        boolean failureHandled = this.failureHandled;
        checkStatementExecutionValidity(doNode);
        if (!this.failureHandled) {
            this.failureHandled = doNode.onFailClause != null;
        }
        analyzeReachability(doNode.body);
        this.failureHandled = failureHandled;
        analyzeOnFailClause(doNode.onFailClause);
    }

    @Override
    public void visit(BLangErrorDestructure errorDestructure) {
        checkStatementExecutionValidity(errorDestructure);
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
    }

    @Override
    public void visit(BLangFail failNode) {
        checkStatementExecutionValidity(failNode);
        this.errorThrown = true;
        if (!this.failureHandled) {
            this.statementReturnsPanicsOrFails = true;
        }
    }

    @Override
    public void visit(BLangForeach foreach) {
        boolean statementReturns = this.statementReturnsPanicsOrFails;
        boolean failureHandled = this.failureHandled;
        checkStatementExecutionValidity(foreach);
        if (!this.failureHandled) {
            this.failureHandled = foreach.onFailClause != null;
        }
        this.loopFound = true;
        analyzeReachability(foreach.body);
        this.loopFound = false;
        this.failureHandled = failureHandled;
        this.statementReturnsPanicsOrFails = statementReturns;
        resetLastStatement();
        this.breakStmtFound = false;
        analyzeOnFailClause(foreach.onFailClause);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
    }

    @Override
    public void visit(BLangMatchStatement matchStatement) {
        if (!this.failureHandled) {
            this.failureHandled = matchStatement.onFailClause != null;
        }
        boolean currentErrorThrown = this.errorThrown;
        boolean hasLastPatternInStatement = this.hasLastPatternInStatement;
        this.hasLastPatternInStatement = false;
        boolean allClausesReturns = true;
        List<BLangMatchClause> matchClauses = matchStatement.matchClauses;
        for (BLangMatchClause matchClause : matchClauses) {
            resetErrorThrown();
            analyzeReachability(matchClause);
            allClausesReturns = allClausesReturns && this.statementReturnsPanicsOrFails;
            resetStatementReturnsPanicsOrFails();
        }
        this.statementReturnsPanicsOrFails = allClausesReturns && this.hasLastPatternInStatement;
        this.errorThrown = currentErrorThrown;
        analyzeOnFailClause(matchStatement.onFailClause);
        this.hasLastPatternInStatement = hasLastPatternInStatement;
    }

    @Override
    public void visit(BLangMatchClause matchClause) {
        boolean hasLastPatternInClause = false;
        List<BLangMatchPattern> matchPatterns = matchClause.matchPatterns;
        for (BLangMatchPattern matchPattern : matchPatterns) {
            if (this.hasLastPatternInStatement || (hasLastPatternInClause && matchClause.matchGuard == null)) {
                dlog.warning(matchPattern.pos, DiagnosticWarningCode.MATCH_STMT_PATTERN_UNREACHABLE);
            }
            hasLastPatternInClause = hasLastPatternInClause || matchPattern.isLastPattern;
        }
        analyzeReachability(matchClause.blockStmt);
        this.hasLastPatternInStatement = this.hasLastPatternInStatement ||
                (matchClause.matchGuard == null && hasLastPatternInClause);
    }

    @Override
    public void visit(BLangRecordDestructure recordDestructure) {
        checkStatementExecutionValidity(recordDestructure);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        checkStatementExecutionValidity(rollbackNode);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        checkStatementExecutionValidity(transactionNode);
        boolean failureHandled = this.failureHandled;
        if (!this.failureHandled) {
            this.failureHandled = transactionNode.onFailClause != null;
        }
        analyzeReachability(transactionNode.transactionBody);
        this.failureHandled = failureHandled;
        analyzeOnFailClause(transactionNode.onFailClause);
    }

    private void analyzeOnFailClause(BLangOnFailClause onFailClause) {
        if (onFailClause != null) {
            boolean currentStatementReturns = this.statementReturnsPanicsOrFails;
            this.booleanConstCondition = BooleanConst.NOT_CONST_BOOLEAN;
            resetStatementReturnsPanicsOrFails();
            resetLastStatement();
            analyzeReachability(onFailClause);
            this.statementReturnsPanicsOrFails = currentStatementReturns;
        }
    }

    @Override
    public void visit(BLangRetry retryNode) {
        boolean failureHandled = this.failureHandled;
        checkStatementExecutionValidity(retryNode);
        if (!this.failureHandled) {
            this.failureHandled = retryNode.onFailClause != null;
        }
        analyzeReachability(retryNode.retryBody);
        this.failureHandled = failureHandled;
        resetLastStatement();
        resetErrorThrown();
        analyzeOnFailClause(retryNode.onFailClause);
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        analyzeReachability(retryTransaction.transaction);
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
        resetLastStatement();
        resetErrorThrown();
        analyzeReachability(onFailClause.body);
        resetErrorThrown();
    }

    @Override
    public void visit(BLangFunction funcNode) {
        resetFunction();
        if (funcNode.flagSet.contains(Flag.NATIVE)) {
            return;
        }
        if (funcNode.body != null) {
            analyzeReachability(funcNode.body);
            boolean isNeverReturn = types.isNeverTypeOrStructureTypeWithARequiredNeverMember
                    (funcNode.symbol.type.getReturnType());
            // If the return signature is nil-able, an implicit return will be added in Desugar.
            // Hence this only checks for non-nil-able return signatures and uncertain return in the body.
            if (!funcNode.symbol.type.getReturnType().isNullable() && !isNeverReturn &&
                    !this.statementReturnsPanicsOrFails) {
                Location closeBracePos = getEndCharPos(funcNode.pos);
                this.dlog.error(closeBracePos, DiagnosticErrorCode.INVOKABLE_MUST_RETURN,
                        funcNode.getKind().toString().toLowerCase());
            } else if (isNeverReturn && !this.statementReturnsPanicsOrFails) {
                this.dlog.error(funcNode.pos, DiagnosticErrorCode.THIS_FUNCTION_SHOULD_PANIC);
            }
        }

        if (funcNode.returnTypeNode.getBType().tag == TypeTags.UNION) {
            LinkedHashSet<BType> memberTypes = ((BUnionType) funcNode.returnTypeNode.getBType()).getMemberTypes();
            if (memberTypes.contains(symTable.nilType) && !this.statementReturnsPanicsOrFails) {
                this.dlog.warning(funcNode.returnTypeNode.pos,
                        DiagnosticWarningCode.FUNCTION_SHOULD_EXPLICITLY_RETURN_A_NIL);
            }
        }
    }

    private Location getEndCharPos(Location pos) {
        LineRange lineRange = pos.lineRange();
        LinePosition endLinePos = lineRange.endLine();
        return new BLangDiagnosticLocation(lineRange.filePath(), endLinePos.line(), endLinePos.line(),
                endLinePos.offset() - 1, endLinePos.offset(),
                pos.textRange().startOffset() + pos.textRange().length() - 1, 1);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        boolean statementReturn = this.statementReturnsPanicsOrFails;
        if (bLangLambdaFunction.parent.getKind() == NodeKind.VARIABLE &&
              (((BLangSimpleVariable) bLangLambdaFunction.parent).name.value).startsWith(WORKER_LAMBDA_VAR_PREFIX)) {
            analyzeReachability(bLangLambdaFunction.function);
        }
        this.statementReturnsPanicsOrFails = statementReturn;
    }

    @Override
    public void visit(BLangExternalFunctionBody body) {
    }

    public void visit(BLangResourceFunction resourceFunction) {
        visit((BLangFunction) resourceFunction);
    }

    @Override
    public void visit(BLangTupleDestructure tupleDestructure) {
        checkStatementExecutionValidity(tupleDestructure);
    }

    @Override
    public void visit(BLangTupleVariableDef tupleVariableDef) {
    }

    @Override
    public void visit(BLangWhile whileNode) {
        boolean statementReturns = this.statementReturnsPanicsOrFails;
        boolean failureHandled = this.failureHandled;
        this.inWhileOrIfBlock = true;
        checkStatementExecutionValidity(whileNode);
        if (!this.failureHandled) {
            this.failureHandled = whileNode.onFailClause != null;
        }
        this.loopFound = true;
        analyzeReachability(whileNode.body);
        this.loopFound = false;
        this.failureHandled = failureHandled;
        if (booleanConstCondition != BooleanConst.TRUE || breakStmtFound) {
            this.statementReturnsPanicsOrFails = statementReturns;
        }
        resetLastStatement();
        this.breakStmtFound = false;
        this.inWhileOrIfBlock = false;
        analyzeOnFailClause(whileNode.onFailClause);
    }

    @Override
    public void visit(BLangBlockFunctionBody body) {
        for (BLangStatement stmt : body.stmts) {
            analyzeReachability(stmt);
        }
        resetLastStatement();
    }

    @Override
    public void visit(BLangExprFunctionBody body) {
        this.statementReturnsPanicsOrFails = true;
        resetLastStatement();
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        checkStatementExecutionValidity(workerSendNode);
    }

    @Override
    public void visit(BLangLetExpression letExpression) {
        // This is to support when let expressions are used in return statements
        // Since variable declarations are visited after return node, this stops false positive unreachable code error
        boolean returnStateBefore = this.statementReturnsPanicsOrFails;
        this.statementReturnsPanicsOrFails = false;
        for (BLangLetVariable letVariable : letExpression.letVarDeclarations) {
            analyzeReachability((BLangNode) letVariable.definitionNode);
        }
        this.statementReturnsPanicsOrFails = returnStateBefore;
    }

    private enum BooleanConst {
        TRUE,
        FALSE,
        NOT_CONST_BOOLEAN
    }

    private void checkStatementExecutionValidity(BLangStatement statement) {
        checkStatementReachability(statement);
        checkConditionInWhileOrIf(statement);
    }

    private void checkBlockStmtExecutionValidity(BLangBlockStmt blockStmt) {
        this.unreachableBlock = inWhileOrIfBlock && unreachableBlock ?
                        unreachableBlock : (booleanConstCondition == BooleanConst.FALSE && !inElseBlock
                        || booleanConstCondition == BooleanConst.TRUE && inElseBlock);

        if (blockStmt.stmts.isEmpty()) {
            checkUnreachableCode(blockStmt.pos);
        }
    }

    private void checkStatementReachability(BLangStatement statement) {
        if (booleanConstCondition == BooleanConst.NOT_CONST_BOOLEAN) {
            checkStatementReachable(statement);
            return;
        }
        if (booleanConstCondition == BooleanConst.TRUE) {
            if (unreachableBlock || inElseBlock) {
                checkStatementReachableConditionedByBooleanConst(statement);
                return;
            }
            checkStatementReachable(statement);
            return;
        }
        if (inElseBlock) {
            checkStatementReachable(statement);
            return;
        }
        checkStatementReachableConditionedByBooleanConst(statement);
    }

    private void checkConditionInWhileOrIf(BLangStatement statement) {
        switch (statement.getKind()) {
            case WHILE:
                setConstCondition(((BLangWhile) statement).expr, false);
                break;
            case IF:
                setConstCondition(((BLangIf) statement).expr, statement.parent != null &&
                        statement.parent.getKind() == NodeKind.IF);
        }
    }

    private void setConstCondition(BLangExpression condition, boolean isElseIfStmt) {
        this.unreachableBlock = isElseIfStmt && booleanConstCondition == BooleanConst.TRUE;
        switch (condition.getKind()) {
            case GROUP_EXPR:
                setConstCondition(((BLangGroupExpr) condition).expression, isElseIfStmt);
                break;
            case LITERAL:
                BLangLiteral literal = (BLangLiteral) condition;
                if (!(literal.value instanceof Boolean)) {
                    break;
                }
                this.booleanConstCondition = literal.value.equals(true) ? BooleanConst.TRUE :
                        BooleanConst.FALSE;
                break;
            case TYPE_TEST_EXPR:
                BLangTypeTestExpr typeTestExpr = (BLangTypeTestExpr) condition;
                boolean isAssignable = types.isAssignable(typeTestExpr.expr.getBType(),
                        typeTestExpr.typeNode.getBType());
                if (typeTestExpr.isNegation) {
                    this.booleanConstCondition = isAssignable ? BooleanConst.FALSE :
                            typeTestExpr.expr.getBType() == symTable.semanticError ? BooleanConst.TRUE :
                                    BooleanConst.NOT_CONST_BOOLEAN;
                } else {
                    this.booleanConstCondition = isAssignable ? BooleanConst.TRUE :
                            typeTestExpr.expr.getBType() == symTable.semanticError ? BooleanConst.FALSE :
                                    BooleanConst.NOT_CONST_BOOLEAN;
                }
                break;
            case BINARY_EXPR:
                BLangBinaryExpr binaryExpr = (BLangBinaryExpr) condition;
                if (binaryExpr.opKind != OperatorKind.AND && !(binaryExpr.lhsExpr.getKind() == NodeKind.LITERAL ||
                        binaryExpr.lhsExpr.getKind() == NodeKind.TYPE_TEST_EXPR) &&
                        !(binaryExpr.rhsExpr.getKind() == NodeKind.LITERAL ||
                                binaryExpr.rhsExpr.getKind() == NodeKind.TYPE_TEST_EXPR)) {
                    this.booleanConstCondition = BooleanConst.NOT_CONST_BOOLEAN;
                    break;
                }
                setConstCondition(binaryExpr.lhsExpr, isElseIfStmt);
                BooleanConst lhsConst = this.booleanConstCondition;
                setConstCondition(binaryExpr.rhsExpr, isElseIfStmt);
                if (lhsConst == BooleanConst.FALSE ||
                        booleanConstCondition == BooleanConst.FALSE) {
                    this.booleanConstCondition = BooleanConst.FALSE;
                } else if (lhsConst == BooleanConst.NOT_CONST_BOOLEAN ||
                        booleanConstCondition == BooleanConst.NOT_CONST_BOOLEAN) {
                    this.booleanConstCondition = BooleanConst.NOT_CONST_BOOLEAN;
                } else {
                    this.booleanConstCondition = lhsConst == booleanConstCondition && lhsConst == BooleanConst.TRUE ?
                        BooleanConst.TRUE : BooleanConst.FALSE;
                }
                break;
            case UNARY_EXPR:
                BLangUnaryExpr unaryExpr = (BLangUnaryExpr) condition;
                if (unaryExpr.operator != OperatorKind.NOT) {
                    this.booleanConstCondition = BooleanConst.NOT_CONST_BOOLEAN;
                    break;
                }
                setConstCondition(unaryExpr.expr, isElseIfStmt);
                if (this.booleanConstCondition == BooleanConst.TRUE) {
                    this.booleanConstCondition = BooleanConst.FALSE;
                } else if (this.booleanConstCondition == BooleanConst.FALSE) {
                    this.booleanConstCondition = BooleanConst.TRUE;
                }
                break;
            default:
                this.booleanConstCondition = BooleanConst.NOT_CONST_BOOLEAN;
        }
    }

    private void checkStatementReachableConditionedByBooleanConst(BLangStatement statement) {
        Location errPosition = (inWhileOrIfBlock || inElseBlock) && statement.parent != null ?
                statement.parent.getPosition() : statement.pos;
        if (statement.getKind() != NodeKind.EXPRESSION_STATEMENT ||
                (statement.getKind() == NodeKind.EXPRESSION_STATEMENT &&
                        ((BLangExpressionStmt) statement).expr.getKind() != NodeKind.INVOCATION)) {
            checkUnreachableCode(errPosition);
            return;
        }
        BLangInvocation invocationExpr = (BLangInvocation) ((BLangExpressionStmt) statement).expr;
        if (invocationExpr.symbol != null && (invocationExpr.symbol.flags & Flags.LANG_LIB) != Flags.LANG_LIB
                && !invocationExpr.name.value.equals("unreachable")) {
            checkUnreachableCode(errPosition);
            return;
        }
        checkStatementReachable(statement);
    }

    private void checkStatementReachable(BLangStatement statement) {
        if (statement.getKind() == NodeKind.EXPRESSION_STATEMENT &&
                ((BLangExpressionStmt) statement).expr.getKind() == NodeKind.INVOCATION) {
            BLangInvocation invocationExpr = (BLangInvocation) ((BLangExpressionStmt) statement).expr;
            if (invocationExpr.symbol != null && (invocationExpr.symbol.flags & Flags.LANG_LIB) ==
                    Flags.LANG_LIB && invocationExpr.name.value.equals("unreachable")) {
                if (!statementReturnsPanicsOrFails && !lastStatement && !errorThrown && !unreachableBlock) {
                    dlog.error(statement.pos, DiagnosticErrorCode.INVALID_UNREACHABLE_LANGLIB_INVOCATION);
                }
                return;
            }
        }
        checkUnreachableCode(statement.pos);
    }

    private void checkUnreachableCode(Location pos) {
        if (statementReturnsPanicsOrFails) {
            dlog.error(pos, DiagnosticErrorCode.UNREACHABLE_CODE);
            resetStatementReturnsPanicsOrFails();
        } else if (errorThrown) {
            dlog.error(pos, DiagnosticErrorCode.UNREACHABLE_CODE);
            resetErrorThrown();
        } else if (lastStatement) {
            dlog.error(pos, DiagnosticErrorCode.UNREACHABLE_CODE);
            resetLastStatement();
        } else if (unreachableBlock) {
            dlog.error(pos, DiagnosticErrorCode.UNREACHABLE_CODE);
            resetUnreachableBlock();
        }
    }

    private void resetStatementReturnsPanicsOrFails() {
        statementReturnsPanicsOrFails = false;
    }

    private void resetLastStatement() {
        lastStatement = false;
    }

    private void resetErrorThrown() {
        errorThrown = false;
    }

    private void resetUnreachableBlock() {
        unreachableBlock = false;
    }

    private void resetFunction() {
        resetStatementReturnsPanicsOrFails();
        resetErrorThrown();
        booleanConstCondition = BooleanConst.NOT_CONST_BOOLEAN;
        inWhileOrIfBlock = false;
        inElseBlock = false;
    }
}
