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
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangMatchClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
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
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.wso2.ballerinalang.compiler.util.Constants.WORKER_LAMBDA_VAR_PREFIX;

/**
 * Responsible for performing reachability analysis.
 *
 * @since 2.0.0
 */
public class ReachabilityAnalyzer extends BLangNodeVisitor {
    private static final CompilerContext.Key<ReachabilityAnalyzer> REACHABILITY_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private final SymbolResolver symResolver;
    private final SymbolTable symTable;
    private final TypeNarrower typeNarrower;
    private final Types types;
    private final BLangDiagnosticLog dlog;
    private final Names names;
    private boolean statementReturnsPanicsOrFails;
    private boolean breakAsLastStatement;
    private boolean continueAsLastStatement;
    private boolean errorThrown;
    private boolean unreachableBlock;
    private boolean breakStmtFound;
    private boolean hasLastPatternInStatement;
    private boolean failureHandled;
    private int loopCount;
    private SymbolEnv env;
    private BType booleanConstCondition;

    private final Stack<SymbolEnv> loopEnvs = new Stack<>();
    private final Stack<PotentiallyInvalidAssignmentInfo> potentiallyInvalidAssignmentInLoopsInfo = new Stack<>();

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
        SymbolEnv prevEnv = this.env;
        this.env = env;
        node.accept(this);
        this.env = prevEnv;
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        final SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        BType prevBoolConst = this.booleanConstCondition;
        for (BLangStatement stmt : blockNode.stmts) {
            analyzeReachability(stmt, blockEnv);
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
            analyzeReachability(stmt, env);
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
        validateAssignmentToNarrowedVariable(assignNode.varRef, assignNode.pos, env);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignment) {
        checkStatementExecutionValidity(compoundAssignment);
        validateAssignmentToNarrowedVariable(compoundAssignment.varRef, compoundAssignment.pos, env);
    }

    @Override
    public void visit(BLangContinue continueNode) {
        checkStatementExecutionValidity(continueNode);
        this.continueAsLastStatement = this.loopCount > 0;
    }

    @Override
    public void visit(BLangBreak breakNode) {
        checkStatementExecutionValidity(breakNode);
        this.breakAsLastStatement = this.loopCount > 0;
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
        if (exprStmtNode.expr.getKind() == NodeKind.INVOCATION &&
                types.isNeverTypeOrStructureTypeWithARequiredNeverMember(exprStmtNode.expr.getBType())) {
            this.statementReturnsPanicsOrFails = true;
        }
    }

    @Override
    public void visit(BLangIf ifStmt) {
        checkStatementExecutionValidity(ifStmt);

        this.potentiallyInvalidAssignmentInLoopsInfo.add(new PotentiallyInvalidAssignmentInfo(new ArrayList<>(),
                env.enclInvokable));
        this.unreachableBlock = this.unreachableBlock || this.booleanConstCondition == symTable.falseType;
        analyzeReachability(ifStmt.body, env);
        resetUnreachableBlock();

        boolean allBranchesTerminate = this.breakAsLastStatement || this.statementReturnsPanicsOrFails;
        handlePotentiallyInvalidAssignmentsToTypeNarrowedVariablesInLoop(allBranchesTerminate,
                                                                         this.continueAsLastStatement);

        boolean ifStmtReturnsPanicsOrFails = this.statementReturnsPanicsOrFails;
        boolean currentErrorThrown = this.errorThrown;
        boolean ifStmtBreakAsLastStatement = this.breakAsLastStatement;
        boolean ifStmtContinueAsLastStatement = this.continueAsLastStatement;

        if (booleanConstCondition != symTable.trueType) {
            resetStatementReturnsPanicsOrFails();
            resetErrorThrown();
            resetLastStatement();
        }

        BLangStatement elseStmt = ifStmt.elseStmt;
        if (elseStmt != null) {
            this.potentiallyInvalidAssignmentInLoopsInfo.add(new PotentiallyInvalidAssignmentInfo(new ArrayList<>(),
                    env.enclInvokable));

            this.unreachableBlock = this.unreachableBlock || (this.booleanConstCondition == symTable.trueType &&
                    elseStmt.getKind() != NodeKind.IF);
            analyzeReachability(elseStmt, env);
            resetUnreachableBlock();

            handlePotentiallyInvalidAssignmentsToTypeNarrowedVariablesInLoop(
                    this.breakAsLastStatement || this.statementReturnsPanicsOrFails,
                    this.continueAsLastStatement);

            if (booleanConstCondition == symTable.semanticError) {
                this.statementReturnsPanicsOrFails = ifStmtReturnsPanicsOrFails && this.statementReturnsPanicsOrFails;
                this.errorThrown = currentErrorThrown && this.errorThrown;
                this.breakAsLastStatement = ifStmtBreakAsLastStatement && this.breakAsLastStatement;
                this.continueAsLastStatement = ifStmtContinueAsLastStatement && this.continueAsLastStatement;
            }
        }
    }

    @Override
    public void visit(BLangDo doNode) {
        boolean failureHandled = this.failureHandled;
        checkStatementExecutionValidity(doNode);
        if (!this.failureHandled) {
            this.failureHandled = doNode.onFailClause != null;
        }
        analyzeReachability(doNode.body, env);
        this.failureHandled = failureHandled;
        analyzeOnFailClause(doNode.onFailClause);
    }

    @Override
    public void visit(BLangErrorDestructure errorDestructureStmt) {
        checkStatementExecutionValidity(errorDestructureStmt);
        validateAssignmentToNarrowedVariables(getVarRefs(errorDestructureStmt.varRef), errorDestructureStmt.pos, env);
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
        SymbolEnv foreachEnv = SymbolEnv.createLoopEnv(foreach, env);
        this.loopEnvs.add(foreachEnv);

        this.potentiallyInvalidAssignmentInLoopsInfo.add(new PotentiallyInvalidAssignmentInfo(new ArrayList<>(),
                env.enclInvokable));

        boolean prevStatementReturnsPanicsOrFails = this.statementReturnsPanicsOrFails;
        boolean prevBreakAsLastStatement = this.breakAsLastStatement;
        boolean prevContinueAsLastStatement = this.continueAsLastStatement;
        boolean prevBreakStmtFound = this.breakStmtFound;
        boolean failureHandled = this.failureHandled;

        checkStatementExecutionValidity(foreach);

        if (!this.failureHandled) {
            this.failureHandled = foreach.onFailClause != null;
        }

        this.breakStmtFound = false;
        this.loopCount++;
        analyzeReachability(foreach.body, foreachEnv);

        handlePotentiallyInvalidAssignmentsToTypeNarrowedVariablesInLoop(
                this.breakAsLastStatement || this.statementReturnsPanicsOrFails, true);

        this.loopCount--;
        this.failureHandled = failureHandled;
        this.continueAsLastStatement = prevContinueAsLastStatement;
        this.breakAsLastStatement = prevBreakAsLastStatement;
        this.statementReturnsPanicsOrFails = prevStatementReturnsPanicsOrFails;
        this.breakStmtFound = prevBreakStmtFound;

        analyzeOnFailClause(foreach.onFailClause);

        this.loopEnvs.pop();
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
    }

    @Override
    public void visit(BLangMatchStatement matchStatement) {
        checkStatementExecutionValidity(matchStatement);

        if (!this.failureHandled) {
            this.failureHandled = matchStatement.onFailClause != null;
        }
        boolean currentErrorThrown = this.errorThrown;
        boolean hasLastPatternInStatement = this.hasLastPatternInStatement;
        this.hasLastPatternInStatement = false;
        boolean allClausesReturns = true;
        boolean allClausesBreak = true;
        boolean allClausesContinue = true;
        List<BLangMatchClause> matchClauses = matchStatement.matchClauses;
        for (BLangMatchClause matchClause : matchClauses) {
            resetErrorThrown();
            this.potentiallyInvalidAssignmentInLoopsInfo.add(new PotentiallyInvalidAssignmentInfo(new ArrayList<>(),
                    env.enclInvokable));
            analyzeReachability(matchClause, this.env);
            handlePotentiallyInvalidAssignmentsToTypeNarrowedVariablesInLoop(
                    this.breakAsLastStatement || this.statementReturnsPanicsOrFails,
                    this.continueAsLastStatement);
            allClausesReturns &= this.statementReturnsPanicsOrFails;
            allClausesBreak &= this.breakAsLastStatement;
            allClausesContinue &= this.continueAsLastStatement;
            this.resetStatementReturnsPanicsOrFails();
            this.resetLastStatement();
        }
        this.statementReturnsPanicsOrFails = allClausesReturns && this.hasLastPatternInStatement;
        this.breakAsLastStatement = allClausesBreak && this.hasLastPatternInStatement;
        this.continueAsLastStatement = allClausesContinue && this.hasLastPatternInStatement;
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
        analyzeReachability(matchClause.blockStmt, env);
        this.hasLastPatternInStatement = this.hasLastPatternInStatement ||
                (matchClause.matchGuard == null && hasLastPatternInClause);
    }

    @Override
    public void visit(BLangRecordDestructure recordDestructureStmt) {
        checkStatementExecutionValidity(recordDestructureStmt);
        validateAssignmentToNarrowedVariables(getVarRefs(recordDestructureStmt.varRef), recordDestructureStmt.pos, env);
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
        analyzeReachability(transactionNode.transactionBody, env);
        this.failureHandled = failureHandled;
        analyzeOnFailClause(transactionNode.onFailClause);
    }

    private void analyzeOnFailClause(BLangOnFailClause onFailClause) {
        if (onFailClause == null) {
            return;
        }
        boolean currentStatementReturns = this.statementReturnsPanicsOrFails;
        this.booleanConstCondition = symTable.semanticError;
        resetStatementReturnsPanicsOrFails();
        resetLastStatement();
        analyzeReachability(onFailClause, env);
        this.statementReturnsPanicsOrFails = currentStatementReturns;
    }

    @Override
    public void visit(BLangRetry retryNode) {
        boolean failureHandled = this.failureHandled;
        checkStatementExecutionValidity(retryNode);
        if (!this.failureHandled) {
            this.failureHandled = retryNode.onFailClause != null;
        }
        analyzeReachability(retryNode.retryBody, this.env);
        this.failureHandled = failureHandled;
        resetLastStatement();
        resetErrorThrown();
        analyzeOnFailClause(retryNode.onFailClause);
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        analyzeReachability(retryTransaction.transaction, env);
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
        resetLastStatement();
        resetErrorThrown();
        analyzeReachability(onFailClause.body, env);
        resetErrorThrown();
    }

    @Override
    public void visit(BLangFunction funcNode) {
        resetFunction();
        if (funcNode.flagSet.contains(Flag.NATIVE)) {
            return;
        }
        if (funcNode.body != null) {
            analyzeReachability(funcNode.body, env);
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

        BType returnType = funcNode.returnTypeNode.getBType();

        if (!funcNode.interfaceFunction && returnType.tag == TypeTags.UNION) {
            LinkedHashSet<BType> memberTypes = ((BUnionType) returnType).getMemberTypes();
            if (memberTypes.contains(symTable.nilType) &&
                    !types.isSubTypeOfErrorOrNilContainingNil((BUnionType) returnType) &&
                    !this.statementReturnsPanicsOrFails) {
                this.dlog.warning(funcNode.returnTypeNode.pos,
                        DiagnosticWarningCode.FUNCTION_SHOULD_EXPLICITLY_RETURN_A_VALUE);
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
        boolean prevStatementReturnsPanicsOrFails = this.statementReturnsPanicsOrFails;
        boolean prevBreakAsLastStatement = this.breakAsLastStatement;
        boolean prevContinueAsLastStatement = this.continueAsLastStatement;
        if (bLangLambdaFunction.parent.getKind() == NodeKind.VARIABLE &&
              (((BLangSimpleVariable) bLangLambdaFunction.parent).name.value).startsWith(WORKER_LAMBDA_VAR_PREFIX)) {
            BLangFunction function = bLangLambdaFunction.function;
            SymbolEnv invokableEnv = SymbolEnv.createFunctionEnv(function, function.symbol.scope, env);
            analyzeReachability(function, invokableEnv);
        }
        this.continueAsLastStatement = prevContinueAsLastStatement;
        this.breakAsLastStatement = prevBreakAsLastStatement;
        this.statementReturnsPanicsOrFails = prevStatementReturnsPanicsOrFails;
    }

    @Override
    public void visit(BLangExternalFunctionBody body) {
    }

    public void visit(BLangResourceFunction resourceFunction) {
        visit((BLangFunction) resourceFunction);
    }

    @Override
    public void visit(BLangTupleDestructure tupleDestructureStmt) {
        checkStatementExecutionValidity(tupleDestructureStmt);
        validateAssignmentToNarrowedVariables(getVarRefs(tupleDestructureStmt.varRef), tupleDestructureStmt.pos, env);
    }

    @Override
    public void visit(BLangTupleVariableDef tupleVariableDef) {
    }

    @Override
    public void visit(BLangWhile whileNode) {
        SymbolEnv whileEnv = SymbolEnv.createLoopEnv(whileNode, env);
        this.loopEnvs.add(whileEnv);

        this.potentiallyInvalidAssignmentInLoopsInfo.add(new PotentiallyInvalidAssignmentInfo(new ArrayList<>(),
                env.enclInvokable));

        boolean prevStatementReturnsPanicsOrFails = this.statementReturnsPanicsOrFails;
        boolean prevBreakAsLastStatement = this.breakAsLastStatement;
        boolean prevContinueAsLastStatement = this.continueAsLastStatement;
        boolean prevBreakStmtFound = this.breakStmtFound;
        boolean failureHandled = this.failureHandled;

        checkStatementExecutionValidity(whileNode);

        if (!this.failureHandled) {
            this.failureHandled = whileNode.onFailClause != null;
        }

        this.loopCount++;
        this.breakStmtFound = false;
        this.unreachableBlock = this.unreachableBlock || booleanConstCondition == symTable.falseType;
        analyzeReachability(whileNode.body, whileEnv);
        resetUnreachableBlock();

        handlePotentiallyInvalidAssignmentsToTypeNarrowedVariablesInLoop(
                this.breakAsLastStatement || this.statementReturnsPanicsOrFails, true);

        this.loopCount--;
        this.failureHandled = failureHandled;
        if (booleanConstCondition != symTable.trueType || this.breakStmtFound) {
            this.statementReturnsPanicsOrFails = prevStatementReturnsPanicsOrFails;
            this.continueAsLastStatement = prevContinueAsLastStatement;
            this.breakAsLastStatement = prevBreakAsLastStatement;
        } else {
            this.statementReturnsPanicsOrFails = true;
        }
        this.breakStmtFound = prevBreakStmtFound;

        analyzeOnFailClause(whileNode.onFailClause);

        this.loopEnvs.pop();
    }

    @Override
    public void visit(BLangBlockFunctionBody body) {
        final SymbolEnv blockEnv = SymbolEnv.createFuncBodyEnv(body, env);
        for (BLangStatement stmt : body.stmts) {
            analyzeReachability(stmt, blockEnv);
        }
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
            analyzeReachability((BLangNode) letVariable.definitionNode, env);
        }
        this.statementReturnsPanicsOrFails = returnStateBefore;
    }

    private void checkStatementExecutionValidity(BLangStatement statement) {
        checkUnreachableCode(statement.pos);
        checkConditionInWhileOrIf(statement);
    }

    private void checkConditionInWhileOrIf(BLangStatement statement) {
        switch (statement.getKind()) {
            case WHILE:
                this.booleanConstCondition = ConditionResolver.checkConstCondition(types, symTable,
                        ((BLangWhile) statement).expr);
                break;
            case IF:
                this.unreachableBlock = statement.parent != null && statement.parent.getKind() == NodeKind.IF
                        && booleanConstCondition == symTable.trueType;
                this.booleanConstCondition = ConditionResolver.checkConstCondition(types, symTable,
                        ((BLangIf) statement).expr);
                break;
        }
    }

    private void checkUnreachableCode(Location pos) {
        if (statementReturnsPanicsOrFails) {
            dlog.error(pos, DiagnosticErrorCode.UNREACHABLE_CODE);
            resetStatementReturnsPanicsOrFails();
        } else if (errorThrown) {
            dlog.error(pos, DiagnosticErrorCode.UNREACHABLE_CODE);
            resetErrorThrown();
        } else if (this.breakAsLastStatement || this.continueAsLastStatement) {
            dlog.error(pos, DiagnosticErrorCode.UNREACHABLE_CODE);
            resetLastStatement();
        } else if (unreachableBlock) {
            dlog.error(pos, DiagnosticErrorCode.UNREACHABLE_CODE);
            resetUnreachableBlock();
        }
    }

    private void resetStatementReturnsPanicsOrFails() {
        this.statementReturnsPanicsOrFails = false;
    }

    private void resetLastStatement() {
        this.breakAsLastStatement = false;
        this.continueAsLastStatement = false;
    }

    private void resetErrorThrown() {
        this.errorThrown = false;
    }

    private void resetUnreachableBlock() {
        this.unreachableBlock = false;
    }

    private void resetFunction() {
        resetStatementReturnsPanicsOrFails();
        resetErrorThrown();
        resetLastStatement();
        this.booleanConstCondition = symTable.semanticError;
    }

    private void validateAssignmentToNarrowedVariables(List<BLangExpression> exprs, Location location, SymbolEnv env) {
        for (BLangExpression expr : exprs) {
            if (expr == null) {
                continue;
            }

            switch (expr.getKind()) {
                case SIMPLE_VARIABLE_REF:
                    validateAssignmentToNarrowedVariable(expr, location, env);
                    continue;
                case RECORD_VARIABLE_REF:
                    validateAssignmentToNarrowedVariables(getVarRefs((BLangRecordVarRef) expr), location, env);
                    continue;
                case TUPLE_VARIABLE_REF:
                    validateAssignmentToNarrowedVariables(getVarRefs((BLangTupleVarRef) expr), location, env);
                    continue;
                case ERROR_VARIABLE_REF:
                    validateAssignmentToNarrowedVariables(getVarRefs((BLangErrorVarRef) expr), location, env);
            }
        }
    }

    private void validateAssignmentToNarrowedVariable(BLangExpression expr, Location location, SymbolEnv env) {
        if (expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return;
        }

        BLangSimpleVarRef varRef = (BLangSimpleVarRef) expr;
        BSymbol symbol = varRef.symbol;
        if (symbol == null || ((BVarSymbol) symbol).originalSymbol == null) {
            return;
        }

        if (this.loopCount == 0) {
            return;
        }

        validateAssignmentToNarrowedVariable(varRef, location, env);
    }

    private void validateAssignmentToNarrowedVariable(BLangSimpleVarRef varRef, Location location, SymbolEnv env) {
        Name name = names.fromIdNode(varRef.variableName);
        SymbolEnv loopEnv = this.loopEnvs.peek();
        SymbolEnv currentEnv = env;

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

        this.potentiallyInvalidAssignmentInLoopsInfo.peek().locations.add(location);
    }

    private void handleInvalidAssignmentToTypeNarrowedVariableInLoop(List<Location> locations) {
        for (Location location : locations) {
            dlog.error(location, DiagnosticErrorCode.INVALID_ASSIGNMENT_TO_NARROWED_VAR_IN_LOOP);
        }
    }

    private void handlePotentiallyInvalidAssignmentsToTypeNarrowedVariablesInLoop(
            boolean branchTerminates, boolean isLoopBodyOrBranchWithContinueAsLastStmt) {

        PotentiallyInvalidAssignmentInfo
                currentBranchInfo = this.potentiallyInvalidAssignmentInLoopsInfo.pop();

        if (branchTerminates) {
            return;
        }

        List<Location> currentBranchLocations = currentBranchInfo.locations;

        if (isLoopBodyOrBranchWithContinueAsLastStmt) {
            handleInvalidAssignmentToTypeNarrowedVariableInLoop(currentBranchLocations);
            return;
        }

        if (currentBranchLocations.isEmpty() || this.potentiallyInvalidAssignmentInLoopsInfo.empty()) {
            return;
        }

        PotentiallyInvalidAssignmentInfo prevInfo = this.potentiallyInvalidAssignmentInLoopsInfo.peek();

        if (prevInfo.enclInvokable != currentBranchInfo.enclInvokable) {
            return;
        }

        prevInfo.locations.addAll(currentBranchLocations);
    }

    private List<BLangExpression> getVarRefs(BLangRecordVarRef varRef) {
        List<BLangExpression> varRefs = varRef.recordRefFields.stream()
                .map(e -> e.variableReference).collect(Collectors.toList());
        varRefs.add((BLangExpression) varRef.restParam);
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
        varRefs.add((BLangExpression) varRef.restParam);
        return varRefs;
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
}
