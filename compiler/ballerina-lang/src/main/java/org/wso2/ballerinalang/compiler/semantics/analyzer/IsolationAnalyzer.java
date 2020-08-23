/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimitClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnConflictClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderKey;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCommitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFailExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecatedParametersDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecationDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableMultiKeyExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTransactionalExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementFilter;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLNavigationAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStreamType;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.util.Flags;

/**
 * Responsible for performing isolation analysis.
 *
 * @since Swan Lake
 */
public class IsolationAnalyzer extends BLangNodeVisitor {

    private static final CompilerContext.Key<IsolationAnalyzer> ISOLATION_ANALYZER_KEY = new CompilerContext.Key<>();
    private SymbolEnv env;
    private SymbolTable symTable;
    private Types types;
    private BLangDiagnosticLogHelper dlog;

    private boolean markedIsolated = false;
    private boolean inferredIsolated = true;

    private IsolationAnalyzer(CompilerContext context) {
        context.put(ISOLATION_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
    }

    public static IsolationAnalyzer getInstance(CompilerContext context) {
        IsolationAnalyzer isolationAnalyzer = context.get(ISOLATION_ANALYZER_KEY);
        if (isolationAnalyzer == null) {
            isolationAnalyzer = new IsolationAnalyzer(context);
        }
        return isolationAnalyzer;
    }

    private void analyzeNode(BLangNode node, SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        if (node != null) {
            node.accept(this);
        }
        this.env = prevEnv;
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        analyzeNode(pkgNode, pkgEnv);
        return pkgNode;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.ISOLATION_ANALYZE)) {
            return;
        }

        for (BLangFunction function : pkgNode.functions) {
            boolean prevMarkedIsolated = this.markedIsolated;
            boolean prevInferredIsolated = this.inferredIsolated;

            markedIsolated = Symbols.isFlagOn(function.symbol.flags, Flags.ISOLATED);
            analyzeNode(function, env);

            if (this.inferredIsolated && !Symbols.isFlagOn(function.symbol.flags, Flags.WORKER)) {
                dlog.note(function.pos, DiagnosticCode.FUNCTION_CAN_BE_MARKED_ISOLATED, function.name);
            }

            this.inferredIsolated = prevInferredIsolated;
            this.markedIsolated = prevMarkedIsolated;
        }

        pkgNode.completedPhases.add(CompilerPhase.ISOLATION_ANALYZE);
    }

    @Override
    public void visit(BLangTestablePackage testablePkgNode) {
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
    }

    @Override
    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        analyzeNode(funcNode.body, funcEnv);
    }

    @Override
    public void visit(BLangBlockFunctionBody body) {
        SymbolEnv bodyEnv = SymbolEnv.createFuncBodyEnv(body, env);
        for (BLangStatement statement : body.stmts) {
            analyzeNode(statement, bodyEnv);
        }
    }

    @Override
    public void visit(BLangExprFunctionBody body) {
        SymbolEnv bodyEnv = SymbolEnv.createFuncBodyEnv(body, env);
        analyzeNode(body.expr, bodyEnv);
    }

    @Override
    public void visit(BLangExternalFunctionBody body) {
        // do nothing
    }

    @Override
    public void visit(BLangService serviceNode) {
        throw new AssertionError();
    }


    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangConstant constant) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
//        analyzeNode(varNode.typeNode, env);

        BLangExpression expr = varNode.expr;
        if (expr == null) {
            return;
        }

        analyzeNode(expr, env);
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTableKeySpecifier tableKeySpecifierNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint tableKeyTypeConstraint) {
        throw new AssertionError();
    }

    // Statements
    @Override
    public void visit(BLangBlockStmt blockNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        BLangVariable var = varDefNode.var;
        if (var.expr == null) {
            return;
        }

        analyzeNode(var, env);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        analyzeNode(assignNode.varRef, env);
        analyzeNode(assignNode.expr, env);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRetry retryNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRetrySpec retrySpec) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangContinue continueNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangBreak breakNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangReturn returnNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangPanic panicNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        analyzeNode(exprStmtNode.expr, env);
    }

    @Override
    public void visit(BLangIf ifNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMatch matchNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangForeach foreach) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangLetClause letClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangOnClause onClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangOrderKey orderKeyClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangOrderByClause orderByClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangDoClause doClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangLimitClause limitClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangWhile whileNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangLock lockNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        throw new AssertionError();
    }

    // Expressions

    @Override
    public void visit(BLangLiteral literalExpr) {
    }

    @Override
    public void visit(BLangConstRef constRef) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        BType accessType = varRefExpr.type;

        if (!isModuleVarRef(varRefExpr)) {
            return;
        }

        if (Symbols.isFlagOn(varRefExpr.symbol.flags, Flags.FINAL) &&
                (types.isInherentlyImmutableType(accessType) || Symbols.isFlagOn(accessType.flags, Flags.READONLY))) {
            return;
        }

        inferredIsolated = false;

        if (markedIsolated) {
            dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_MUTABLE_ACCESS_IN_ISOLATED_FUNCTION);
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        if (Symbols.isFlagOn(invocationExpr.symbol.flags, Flags.ISOLATED)) {
            return;
        }

        inferredIsolated = false;

        if (markedIsolated) {
            dlog.error(invocationExpr.pos, DiagnosticCode.INVALID_NON_ISOLATED_INVOCATION_IN_ISOLATED_FUNCTION);
        }
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        if (!actionInvocationExpr.async) {
            return;
        }

        inferredIsolated = false;

        if (markedIsolated && !actionInvocationExpr.functionPointerInvocation) {
            dlog.error(actionInvocationExpr.pos, DiagnosticCode.INVALID_ASYNC_INVOCATION_IN_ISOLATED_FUNCTION);
        }
    }

    @Override
    public void visit(BLangTypeInit connectorInitExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        analyzeNode(binaryExpr.lhsExpr, env);
        analyzeNode(binaryExpr.rhsExpr, env);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangLetExpression letExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangLetVariable letVariable) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRawTemplateLiteral stringTemplateLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        if (Symbols.isFlagOn(bLangLambdaFunction.function.symbol.flags, Flags.WORKER)) {
            // TODO: 8/21/20 should the worker have a name
            inferredIsolated = false;

            if (markedIsolated) {
                dlog.error(bLangLambdaFunction.pos, DiagnosticCode.INVALID_WORKER_DECLARATION_IN_ISOLATED_FUNCTION);
            }

            return;
        }

        // TODO: 8/21/20 add analysis
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangFailExpr failExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTableMultiKeyExpr tableMultiKeyExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTransactionalExpr transactionalExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangCommitExpr commitExpr) {
        throw new AssertionError();
    }

    // Type nodes

    @Override
    public void visit(BLangValueType valueType) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangStreamType streamType) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTableTypeNode tableType) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangErrorType errorType) {
        throw new AssertionError();
    }

    // expressions that will used only from the Desugar phase

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTableAccessExpr tableKeyAccessExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlAccessExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRecordLiteral.BLangChannelLiteral channelLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMarkDownDeprecationDocumentation bLangMarkDownDeprecationDocumentation) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMarkDownDeprecatedParametersDocumentation bLangMarkDownDeprecatedParametersDocumentation) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause bLangMatchStmtStaticBindingPatternClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause
                              bLangMatchStmtStructuredBindingPatternClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValueField recordKeyValue) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOperatorField) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMarkdownReferenceDocumentation bLangMarkdownReferenceDocumentation) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLElementFilter xmlElementFilter) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        throw new AssertionError();
    }

    private boolean isModuleVarRef(BLangSimpleVarRef varRef) {
        return varRef.symbol.owner.getKind() == SymbolKind.PACKAGE;
    }
}
