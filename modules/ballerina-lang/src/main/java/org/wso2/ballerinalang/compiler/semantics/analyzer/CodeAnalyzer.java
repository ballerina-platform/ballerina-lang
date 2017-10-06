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
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
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
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangComment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransform;
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
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
    private int failedBlockCount;
    private boolean statementReturns;
    private SymbolEnter symbolEnter;
    private DiagnosticLog dlog;
    private TypeChecker typeChecker;
    private Stack<WorkerActionSystem> workerActionSystemStack = new Stack<>();

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
        boolean functionReturns = invNode.retParams.size() > 0;
        if (invNode.workers.isEmpty()) {
            invNode.body.accept(this);
            /* the function returns, but none of the statements surely returns */
            if (functionReturns && !this.statementReturns) {
                this.dlog.error(invNode.pos, DiagnosticCode.FUNCTION_MUST_RETURN);
            }
        } else {
            boolean workerReturns = false;
            for (BLangWorker worker : invNode.workers) {
                worker.accept(this);
                workerReturns = workerReturns || this.statementReturns;
                this.resetStatementReturns();
            }
            if (functionReturns && !workerReturns) {
                this.dlog.error(invNode.pos, DiagnosticCode.ATLEAST_ONE_WORKER_MUST_RETURN);
            }
        }
        this.finalizeCurrentWorkerActionSystem();
    }
    
    @Override
    public void visit(BLangForkJoin forkJoin) {
        this.initNewWorkerActionSystem();
        this.checkUnreachableCode(forkJoin);
        forkJoin.workers.forEach(e -> e.accept(this));
        forkJoin.joinedBody.accept(this);
        if (forkJoin.timeoutBody != null) {
            forkJoin.timeoutBody.accept(this);
            this.resetStatementReturns();
        }
        this.finalizeCurrentWorkerActionSystem();
    }
    
    @Override
    public void visit(BLangWorker worker) {
        this.workerActionSystemStack.peek().startWorkerActionStateMachine(worker.name.value, worker.pos);
        worker.body.accept(this);
        this.workerActionSystemStack.peek().endWorkerActionStateMachine();
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        this.checkStatementExecutionValidity(transactionNode);
        this.transactionCount++;
        transactionNode.transactionBody.accept(this);
        this.transactionCount--;
        if (transactionNode.failedBody != null) {
            this.failedBlockCount++;
            transactionNode.failedBody.accept(this);
            this.failedBlockCount--;
        }
        if (transactionNode.committedBody != null) {
            transactionNode.committedBody.accept(this);
        }
        if (transactionNode.abortedBody != null) {
            transactionNode.abortedBody.accept(this);
        }
    }

    @Override
    public void visit(BLangAbort abortNode) {
        if (this.transactionCount == 0) {
            this.dlog.error(abortNode.pos, DiagnosticCode.ABORT_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK);
        }
    }

    @Override
    public void visit(BLangRetry abortNode) {
        if (this.failedBlockCount == 0) {
            this.dlog.error(abortNode.pos, DiagnosticCode.RETRY_CANNOT_BE_OUTSIDE_TRANSACTION_FAILED_BLOCK);
        }
    }

    private void checkUnreachableCode(BLangStatement stmt) {
        if (this.statementReturns) {
            this.dlog.error(stmt.pos, DiagnosticCode.UNREACHABLE_CODE);
            this.resetStatementReturns();
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
    }
    
    @Override
    public void visit(BLangReturn returnStmt) {
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
    public void visit(BLangWhile whileNode) {
        this.checkStatementExecutionValidity(whileNode);
        this.loopCount++;
        whileNode.body.stmts.forEach(e -> e.accept(this));
        this.loopCount--;
    }
    
    @Override
    public void visit(BLangContinue continueNode) {
        if (this.loopCount == 0) {
            this.dlog.error(continueNode.pos, DiagnosticCode.NEXT_CANNOT_BE_OUTSIDE_LOOP);
        }
    }

    @Override
    public void visit(BLangTransform transformNode) {
        if (transformNode.body.getStatements().size() == 0) {
            this.dlog.error(transformNode.pos, DiagnosticCode.TRANSFORM_STATEMENT_EMPTY_BODY);
        }
        this.checkStatementExecutionValidity(transformNode);
        Set<String> inputs = new HashSet<>();
        Set<String> outputs = new HashSet<>();
        validateTransformStatementBody(transformNode.body, inputs, outputs);
        inputs.forEach((k) -> transformNode.addInput(k));
        outputs.forEach((k) -> transformNode.addOutput(k));
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
        /* ignore */
    }

    public void visit(BLangAction actionNode) {
        /* ignore */
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

    public void visit(BLangVariableDef varDefNode) {
        this.checkStatementExecutionValidity(varDefNode);
    }

    public void visit(BLangAssignment assignNode) {
        this.checkStatementExecutionValidity(assignNode);
    }

    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    public void visit(BLangThrow throwNode) {
        /* ignore */
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        this.checkStatementExecutionValidity(xmlnsStmtNode);
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        this.checkStatementExecutionValidity(exprStmtNode);
    }

    public void visit(BLangComment commentNode) {
        /* ignore */
    }

    public void visit(BLangTryCatchFinally tryNode) {
        List<BType> caughtTypes = new ArrayList<>();
        for (BLangCatch bLangCatch : tryNode.getCatchBlocks()) {
            if (caughtTypes.contains(bLangCatch.getParameter().type)) {
                dlog.error(bLangCatch.getParameter().pos, DiagnosticCode.DUPLICATED_ERROR_CATCH,
                        bLangCatch.getParameter().type);
            }
            caughtTypes.add(bLangCatch.getParameter().type);
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
        this.workerActionSystemStack.peek().addWorkerAction(workerSendNode);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
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

    /**
     * Validates the statements in the transform statement body as explained below :
     * - Left expression of Assignment Statement becomes output of transform statement
     * - Right expressions of Assignment Statement becomes input of transform statement
     * - Variables in each of left and right expressions of all statements are extracted as input and output
     * - A variable that is used as an input cannot be used as an output in another statement
     * - If inputs and outputs are used interchangeably, a semantic error is thrown.
     *
     * @param blockStmt transform statement block statement
     * @param inputs    input variable reference expressions map
     * @param outputs   output variable reference expressions map
     */
    private void validateTransformStatementBody(BLangBlockStmt blockStmt, Set<String> inputs, Set<String> outputs) {
        Set<String> innerVars = new HashSet<>();
        for (BLangStatement statement : blockStmt.getStatements()) {
            if (statement.getKind() == NodeKind.VARIABLE_DEF) {
                BLangVariableDef variableDefStmt = (BLangVariableDef) statement;
                BLangVariable variable = variableDefStmt.var;
                String varName = variable.name.value;

                //variables defined in transform scope, cannot be used as output
                if (outputs.contains(varName)) {
                    this.dlog.error(variableDefStmt.pos, DiagnosticCode.TRANSFORM_STATEMENT_INVALID_INPUT_OUTPUT);
                    continue;
                }

                if (variable.expr.getKind() != NodeKind.LITERAL) {
                    // if the variable does not hold a constant value, it is a temporary variable and hence not an input
                    innerVars.add(varName);
                }

                //if variable has not been used as an output before
                inputs.add(varName);

                continue;
            }
            if (statement.getKind() == NodeKind.ASSIGNMENT) {
                BLangAssignment assignStmt = (BLangAssignment) statement;
                for (BLangExpression lExpr : assignStmt.varRefs) {
                    BLangExpression[] varRefExpressions = getVariableReferencesFromExpression(lExpr);
                    for (BLangExpression exp : varRefExpressions) {
                        String varName = ((BLangSimpleVarRef) exp).variableName.value;
                        if (assignStmt.declaredWithVar) {
                            innerVars.add(varName);
                        } else {
                            // if lhs is declared with var, they not considered as output variables since they are
                            // only available in transform statement scope
                            if (inputs.contains(varName)) {
                                this.dlog
                                        .error(assignStmt.pos, DiagnosticCode.TRANSFORM_STATEMENT_INVALID_INPUT_OUTPUT);
                                continue;
                            }
                            //if variable has not been used as an input before
                            outputs.add(varName);
                        }
                    }
                }
                BLangExpression rExpr = assignStmt.expr;
                BLangExpression[] varRefExpressions = getVariableReferencesFromExpression(rExpr);
                for (BLangExpression exp : varRefExpressions) {
                    String varName = ((BLangSimpleVarRef) exp).variableName.getValue();
                    if (outputs.contains(varName)) {
                        this.dlog.error(((BLangSimpleVarRef) exp).pos,
                                DiagnosticCode.TRANSFORM_STATEMENT_INVALID_INPUT_OUTPUT);
                        continue;
                    }
                    //if variable has not been used as an output before
                    inputs.add(varName);
                }
            }
        }
        innerVars.forEach(var -> inputs.remove(var));
    }

    private BLangExpression[] getVariableReferencesFromExpression(BLangExpression expression) {
        if (expression.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
            while (!(expression.getKind() == NodeKind.SIMPLE_VARIABLE_REF)) {
                if (expression.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
                    expression = ((BLangFieldBasedAccess) expression).expr;
                } else if (expression.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
                    expression = ((BLangIndexBasedAccess) expression).expr;
                }
            }
            return new BLangExpression[] { expression };
        } else if (expression.getKind() == NodeKind.INVOCATION) {
            List<BLangExpression> argExprs = ((BLangInvocation) expression).argExprs;
            List<BLangExpression> expList = new ArrayList<>();
            for (BLangExpression arg : argExprs) {
                BLangExpression[] varRefExps = getVariableReferencesFromExpression(arg);
                expList.addAll(Arrays.asList(varRefExps));
            }
            return expList.toArray(new BLangExpression[expList.size()]);
        } else if (expression.getKind() == NodeKind.BINARY_EXPR) {
                List<BLangExpression> expList = new ArrayList<>();
                expList.addAll(Arrays.asList(
                        getVariableReferencesFromExpression(((BLangBinaryExpr) expression).rhsExpr)));
                expList.addAll(Arrays.asList(
                        getVariableReferencesFromExpression(((BLangBinaryExpr) expression).lhsExpr)));
                return expList.toArray(new BLangExpression[expList.size()]);
        } else if (expression.getKind() == NodeKind.UNARY_EXPR) {
                return getVariableReferencesFromExpression(((BLangUnaryExpr) expression).expr);
        } else if (expression.getKind() == NodeKind.TYPE_CONVERSION_EXPR) {
            return getVariableReferencesFromExpression(((BLangTypeConversionExpr) expression).expr);
        } else if (expression.getKind() == NodeKind.TYPE_CAST_EXPR) {
            return getVariableReferencesFromExpression(((BLangTypeCastExpr) expression).expr);
        } else if (expression.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            return new BLangExpression[] { expression };
        }
        return new BLangExpression[] {};
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
            return this.workerActionStateMachines.values().stream().map(e -> e.done()).reduce(true, (a, b) -> a && b);
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

}
