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
import org.ballerinalang.model.clauses.OrderKeyNode;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangCaptureBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangListBindingPattern;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimitClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangMatchClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnConflictClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchGuard;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLNavigationAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangConstPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangVarBindingPatternMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangWildCardMatchPattern;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;

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
    private BLangDiagnosticLog dlog;

    private boolean inferredIsolated = true;

    private IsolationAnalyzer(CompilerContext context) {
        context.put(ISOLATION_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
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

        for (BLangTypeDefinition typeDefinition : pkgNode.typeDefinitions) {
            analyzeNode(typeDefinition.typeNode, env);
        }

        for (BLangClassDefinition classDefinition : pkgNode.classDefinitions) {
            analyzeNode(classDefinition, env);
        }

        for (BLangFunction function : pkgNode.functions) {
            analyzeNode(function, env);
        }

        for (BLangSimpleVariable globalVar : pkgNode.globalVars) {
            analyzeNode(globalVar, env);
        }

        pkgNode.completedPhases.add(CompilerPhase.ISOLATION_ANALYZE);
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
        boolean prevInferredIsolated = this.inferredIsolated;

        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        analyzeNode(funcNode.body, funcEnv);

        if (isBallerinaModule(env.enclPkg) && !isIsolated(funcNode.symbol) &&
                this.inferredIsolated && !Symbols.isFlagOn(funcNode.symbol.flags, Flags.WORKER)) {
            dlog.warning(funcNode.pos, DiagnosticCode.FUNCTION_CAN_BE_MARKED_ISOLATED, funcNode.name);
        }

        this.inferredIsolated = prevInferredIsolated;
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
    }

    @Override
    public void visit(BLangService serviceNode) {
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        analyzeNode(typeDefinition.typeNode, env);
    }

    @Override
    public void visit(BLangConstant constant) {
        BLangType typeNode = constant.typeNode;
        if (typeNode != null) {
            analyzeNode(typeNode, env);
        }

        analyzeNode(constant.expr, env);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        BLangType typeNode = varNode.typeNode;
        if (typeNode != null &&
                (typeNode.type == null || typeNode.type.tsymbol == null ||
                         typeNode.type.tsymbol.owner.getKind() != SymbolKind.PACKAGE)) {
            // Only analyze the type node if it is not available at module level, since module level type definitions
            // have already been analyzed.
            analyzeNode(typeNode, env);
        }

        BLangExpression expr = varNode.expr;
        if (expr == null) {
            return;
        }

        analyzeNode(expr, env);

        if (Symbols.isFlagOn(varNode.symbol.flags, Flags.WORKER)) {
            inferredIsolated = false;

            if (isInIsolatedFunction(env.enclInvokable)) {
                dlog.error(varNode.pos, DiagnosticCode.INVALID_WORKER_DECLARATION_IN_ISOLATED_FUNCTION);
            }
        }
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        BLangExpression expr = annAttachmentNode.expr;
        if (expr != null) {
            analyzeNode(expr, env);
        }
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        for (BLangStatement statement : blockNode.stmts) {
            analyzeNode(statement, blockEnv);
        }
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
        analyzeNode(compoundAssignNode.varRef, env);
        analyzeNode(compoundAssignNode.expr, env);
    }

    @Override
    public void visit(BLangRetry retryNode) {
        analyzeNode(retryNode.retrySpec, env);
        analyzeNode(retryNode.retryBody, env);
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        analyzeNode(retryTransaction.retrySpec, env);
        analyzeNode(retryTransaction.transaction, env);
    }

    @Override
    public void visit(BLangRetrySpec retrySpec) {
        for (BLangExpression argExpr : retrySpec.argExprs) {
            analyzeNode(argExpr, env);
        }
    }

    @Override
    public void visit(BLangContinue continueNode) {
    }

    @Override
    public void visit(BLangBreak breakNode) {
    }

    @Override
    public void visit(BLangReturn returnNode) {
        analyzeNode(returnNode.expr, env);
    }

    @Override
    public void visit(BLangPanic panicNode) {
        analyzeNode(panicNode.expr, env);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        analyzeNode(xmlnsStmtNode.xmlnsDecl, env);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        analyzeNode(exprStmtNode.expr, env);
    }

    @Override
    public void visit(BLangIf ifNode) {
        analyzeNode(ifNode.expr, env);
        analyzeNode(ifNode.body, env);
        analyzeNode(ifNode.elseStmt, env);
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        for (BLangNode clause : queryAction.getQueryClauses()) {
            analyzeNode(clause, env);
        }
        analyzeNode(queryAction.doClause, env);
    }

    @Override
    public void visit(BLangMatch matchNode) {
        analyzeNode(matchNode.expr, env);
        for (BLangMatch.BLangMatchBindingPatternClause patternClause : matchNode.patternClauses) {
            analyzeNode(patternClause, env);
        }
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        analyzeNode(patternClauseNode.variable, env);
        analyzeNode(patternClauseNode.body, env);
    }

    @Override
    public void visit(BLangMatchStatement matchStatement) {
        analyzeNode(matchStatement.expr, env);

        for (BLangMatchClause matchClause : matchStatement.matchClauses) {
            analyzeNode(matchClause, env);
        }

        if (matchStatement.onFailClause != null) {
            analyzeNode(matchStatement.onFailClause, env);
        }
    }

    @Override
    public void visit(BLangMatchGuard matchGuard) {
        analyzeNode(matchGuard.expr, env);
    }

    @Override
    public void visit(BLangConstPattern constMatchPattern) {
        analyzeNode(constMatchPattern.expr, env);
    }

    @Override
    public void visit(BLangWildCardMatchPattern wildCardMatchPattern) {
    }

    @Override
    public void visit(BLangVarBindingPatternMatchPattern varBindingPattern) {
        analyzeNode(varBindingPattern.getBindingPattern(), env);
    }

    @Override
    public void visit(BLangCaptureBindingPattern captureBindingPattern) {
    }

    @Override
    public void visit(BLangListBindingPattern listBindingPattern) {
        for (BLangBindingPattern bindingPattern : listBindingPattern.bindingPatterns) {
            analyzeNode(bindingPattern, env);
        }
    }

    @Override
    public void visit(BLangMatchClause matchClause) {
        for (BLangMatchPattern matchPattern : matchClause.matchPatterns) {
            analyzeNode(matchPattern, env);
        }

        BLangMatchGuard matchGuard = matchClause.matchGuard;
        if (matchGuard != null) {
            analyzeNode(matchGuard, env);
        }

        analyzeNode(matchClause.blockStmt, env);
    }

    @Override
    public void visit(BLangForeach foreach) {
        analyzeNode(foreach.collection, env);
        analyzeNode(foreach.body, env);
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        analyzeNode((BLangNode) fromClause.getVariableDefinitionNode(), env);
        analyzeNode(fromClause.collection, env);
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
        analyzeNode((BLangNode) joinClause.getVariableDefinitionNode(), env);
        analyzeNode(joinClause.collection, env);
    }

    @Override
    public void visit(BLangLetClause letClause) {
        for (BLangLetVariable letVarDeclaration : letClause.letVarDeclarations) {
            analyzeNode((BLangNode) letVarDeclaration.definitionNode, env);
        }
    }

    @Override
    public void visit(BLangOnClause onClause) {
        analyzeNode(onClause.lhsExpr, env);
        analyzeNode(onClause.rhsExpr, env);
    }

    @Override
    public void visit(BLangOrderKey orderKeyClause) {
        analyzeNode(orderKeyClause.expression, env);
    }

    @Override
    public void visit(BLangOrderByClause orderByClause) {
        for (OrderKeyNode orderKeyNode : orderByClause.orderByKeyList) {
            analyzeNode((BLangExpression) orderKeyNode.getOrderKey(), env);
        }
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        analyzeNode(selectClause.expression, env);
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        analyzeNode(whereClause.expression, env);
    }

    @Override
    public void visit(BLangDoClause doClause) {
        analyzeNode(doClause.body, env);
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
        analyzeNode(onFailClause.body, env);
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause) {
        analyzeNode(onConflictClause.expression, env);
    }

    @Override
    public void visit(BLangLimitClause limitClause) {
        analyzeNode(limitClause.expression, env);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        analyzeNode(whileNode.expr, env);
        analyzeNode(whileNode.body, env);
    }

    @Override
    public void visit(BLangLock lockNode) {
        analyzeNode(lockNode.body, env);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        analyzeNode(transactionNode.transactionBody, env);
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        analyzeNode(stmt.varRef, env);
        analyzeNode(stmt.expr, env);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        analyzeNode(stmt.varRef, env);
        analyzeNode(stmt.expr, env);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        analyzeNode(stmt.varRef, env);
        analyzeNode(stmt.expr, env);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        analyzeNode(rollbackNode.expr, env);
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
    }

    @Override
    public void visit(BLangConstRef constRef) {
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValuePair =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                if (keyValuePair.key.computedKey) {
                    analyzeNode(keyValuePair.key.expr, env);
                }
                analyzeNode(keyValuePair.valueExpr, env);
            } else if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                analyzeNode((BLangRecordLiteral.BLangRecordVarNameField) field, env);
            } else {
                analyzeNode(((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr, env);
            }
        }
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        for (BLangExpression expression : varRefExpr.expressions) {
            analyzeNode(expression, env);
        }

        BLangExpression restParam = (BLangExpression) varRefExpr.restParam;
        if (restParam != null) {
            analyzeNode(restParam, env);
        }
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        for (BLangRecordVarRef.BLangRecordVarRefKeyValue recordRefField : varRefExpr.recordRefFields) {
            analyzeNode(recordRefField.variableReference, env);
        }

        BLangExpression restParam = (BLangExpression) varRefExpr.restParam;
        if (restParam != null) {
            analyzeNode(restParam, env);
        }
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        analyzeNode(varRefExpr.message, env);

        BLangVariableReference cause = varRefExpr.cause;
        if (cause != null) {
            analyzeNode(cause, env);
        }

        for (BLangNamedArgsExpression namedArgsExpression : varRefExpr.detail) {
            analyzeNode(namedArgsExpression, env);
        }

        BLangVariableReference restVar = varRefExpr.restVar;
        if (restVar != null) {
            analyzeNode(restVar, env);
        }

        BLangType typeNode = varRefExpr.typeNode;
        if (typeNode != null) {
            analyzeNode(typeNode, env);
        }
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        BType accessType = varRefExpr.type;

        BSymbol symbol = varRefExpr.symbol;
        BLangInvokableNode enclInvokable = env.enclInvokable;
        BLangType enclType = env.enclType;


        if (symbol == null) {
            return;
        }

        boolean inIsolatedFunction = isInIsolatedFunction(enclInvokable);
        boolean recordFieldDefaultValue = isRecordFieldDefaultValue(enclType);
        boolean objectFieldDefaultValue = !recordFieldDefaultValue && isObjectFieldDefaultValueRequiringIsolation(env);

        if (inIsolatedFunction) {
            if (enclInvokable == null) {
                BLangArrowFunction bLangArrowFunction = (BLangArrowFunction) env.enclEnv.node;

                for (BLangSimpleVariable param : bLangArrowFunction.params) {
                    if (param.symbol == symbol) {
                        return;
                    }
                }
            }
        }

        if (!recordFieldDefaultValue && !objectFieldDefaultValue && enclInvokable != null &&
                symbol.owner == enclInvokable.symbol) {
            return;
        }

        if (Symbols.isFlagOn(symbol.flags, Flags.CONSTANT)) {
            return;
        }

        if (Symbols.isFlagOn(symbol.flags, Flags.FINAL) &&
                (types.isInherentlyImmutableType(accessType) || Symbols.isFlagOn(accessType.flags, Flags.READONLY))) {
            return;
        }

        if (isDefinitionReference(symbol)) {
            return;
        }


        inferredIsolated = false;

        if (inIsolatedFunction) {
            dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_MUTABLE_ACCESS_IN_ISOLATED_FUNCTION);
            return;
        }

        if (recordFieldDefaultValue) {
            if (isBallerinaModule(env.enclPkg)) {
                // TODO: 9/13/20 remove this error once stdlibs are migrated
                dlog.warning(varRefExpr.pos, DiagnosticCode.WARNING_INVALID_MUTABLE_ACCESS_AS_RECORD_DEFAULT);
            } else {
                dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_MUTABLE_ACCESS_AS_RECORD_DEFAULT);
            }
        }

        if (objectFieldDefaultValue) {
            dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_MUTABLE_ACCESS_AS_OBJECT_DEFAULT);
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        analyzeNode(fieldAccessExpr.expr, env);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        analyzeNode(indexAccessExpr.expr, env);
        analyzeNode(indexAccessExpr.indexExpr, env);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        List<BLangExpression> args = new ArrayList<>(invocationExpr.requiredArgs);
        args.addAll(invocationExpr.restArgs);
        for (BLangExpression argExpr : args) {
            analyzeNode(argExpr, env);
        }

        BSymbol symbol = invocationExpr.symbol;
        if (symbol == null || symbol.getKind() == SymbolKind.ERROR_CONSTRUCTOR || isIsolated(symbol)) {
            return;
        }

        inferredIsolated = false;

        if (isInIsolatedFunction(env.enclInvokable)) {
            dlog.error(invocationExpr.pos, DiagnosticCode.INVALID_NON_ISOLATED_INVOCATION_IN_ISOLATED_FUNCTION);
            return;
        }

        if (isRecordFieldDefaultValue(env.enclType)) {
            if (isBallerinaModule(env.enclPkg)) {
                // TODO: 9/13/20 remove this once stdlibs are migrated
                dlog.warning(invocationExpr.pos,
                             DiagnosticCode.WARNING_INVALID_NON_ISOLATED_INVOCATION_AS_RECORD_DEFAULT);
            } else {
                dlog.error(invocationExpr.pos, DiagnosticCode.INVALID_NON_ISOLATED_INVOCATION_AS_RECORD_DEFAULT);
            }
        }

        if (isObjectFieldDefaultValueRequiringIsolation(env)) {
            dlog.error(invocationExpr.pos, DiagnosticCode.INVALID_NON_ISOLATED_INVOCATION_AS_OBJECT_DEFAULT);
        }
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        if (!actionInvocationExpr.async) {
            return;
        }

        inferredIsolated = false;

        if (actionInvocationExpr.functionPointerInvocation) {
            return;
        }

        if (isInIsolatedFunction(env.enclInvokable)) {
            dlog.error(actionInvocationExpr.pos, DiagnosticCode.INVALID_ASYNC_INVOCATION_IN_ISOLATED_FUNCTION);
        }
    }

    @Override
    public void visit(BLangTypeInit typeInitExpr) {
        BSymbol initInvocationSymbol = typeInitExpr.initInvocation.symbol;
        if (initInvocationSymbol != null && !isIsolated(initInvocationSymbol)) {
            inferredIsolated = false;

            if (isInIsolatedFunction(env.enclInvokable)) {
                dlog.error(typeInitExpr.pos, DiagnosticCode.INVALID_NON_ISOLATED_INIT_EXPRESSION_IN_ISOLATED_FUNCTION);
            } else if (isRecordFieldDefaultValue(env.enclType)) {
                if (isBallerinaModule(env.enclPkg)) {
                    // TODO: 9/16/20 remove this once stdlibs are migrated
                    dlog.warning(typeInitExpr.pos,
                                 DiagnosticCode.WARNING_INVALID_NON_ISOLATED_INIT_EXPRESSION_AS_RECORD_DEFAULT);
                } else {
                    dlog.error(typeInitExpr.pos,
                               DiagnosticCode.INVALID_NON_ISOLATED_INIT_EXPRESSION_AS_RECORD_DEFAULT);
                }

            } else if (isObjectFieldDefaultValueRequiringIsolation(env)) {
                dlog.error(typeInitExpr.pos, DiagnosticCode.INVALID_NON_ISOLATED_INIT_EXPRESSION_AS_OBJECT_DEFAULT);
            }
        }

        for (BLangExpression expression : typeInitExpr.argsExpr) {
            analyzeNode(expression, env);
        }
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        analyzeNode(ternaryExpr.expr, env);
        analyzeNode(ternaryExpr.thenExpr, env);
        analyzeNode(ternaryExpr.elseExpr, env);
    }

    @Override
    public void visit(BLangWaitExpr waitExpr) {
        for (BLangExpression expression : waitExpr.exprList) {
            analyzeNode(expression, env);
        }
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        analyzeNode(trapExpr.expr, env);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        analyzeNode(binaryExpr.lhsExpr, env);
        analyzeNode(binaryExpr.rhsExpr, env);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        analyzeNode(elvisExpr.lhsExpr, env);
        analyzeNode(elvisExpr.rhsExpr, env);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        analyzeNode(groupExpr.expression, env);
    }

    @Override
    public void visit(BLangLetExpression letExpr) {
        for (BLangLetVariable letVarDeclaration : letExpr.letVarDeclarations) {
            analyzeNode((BLangNode) letVarDeclaration.definitionNode, env);
        }

        analyzeNode(letExpr.expr, env);
    }

    @Override
    public void visit(BLangLetVariable letVariable) {
        analyzeNode((BLangNode) letVariable.definitionNode.getVariable(), env);
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        for (BLangExpression expr : listConstructorExpr.exprs) {
            analyzeNode(expr, env);
        }
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        for (BLangRecordLiteral recordLiteral : tableConstructorExpr.recordLiteralList) {
            analyzeNode(recordLiteral, env);
        }
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        analyzeNode(unaryExpr.expr, env);
    }

    @Override
    public void visit(BLangTypedescExpr typedescExpr) {
        analyzeNode(typedescExpr.typeNode, env);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        analyzeNode(conversionExpr.typeNode, env);
        analyzeNode(conversionExpr.expr, env);
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        analyzeNode(xmlAttribute.name, env);
        analyzeNode(xmlAttribute.value, env);
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        for (BLangExpression child : xmlElementLiteral.children) {
            analyzeNode(child, env);
        }

        for (BLangXMLAttribute attribute : xmlElementLiteral.attributes) {
            analyzeNode(attribute, env);
        }

        for (BLangXMLNS inlineNamespace : xmlElementLiteral.inlineNamespaces) {
            analyzeNode(inlineNamespace, env);
        }
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        for (BLangExpression expr : xmlTextLiteral.textFragments) {
            analyzeNode(expr, env);
        }
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        for (BLangExpression textFragment : xmlCommentLiteral.textFragments) {
            analyzeNode(textFragment, env);
        }
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        for (BLangExpression dataFragment : xmlProcInsLiteral.dataFragments) {
            analyzeNode(dataFragment, env);
        }
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        for (BLangExpression textFragment : xmlQuotedString.textFragments) {
            analyzeNode(textFragment, env);
        }
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        for (BLangExpression expr : stringTemplateLiteral.exprs) {
            analyzeNode(expr, env);
        }
    }

    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral) {
        for (BLangExpression insertion : rawTemplateLiteral.insertions) {
            analyzeNode(insertion, env);
        }
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        // TODO: 8/21/20 add analysis
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        SymbolEnv arrowFunctionEnv = SymbolEnv.createArrowFunctionSymbolEnv(bLangArrowFunction, env);
        analyzeNode(bLangArrowFunction.body, arrowFunctionEnv);
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        analyzeNode(intRangeExpression.startExpr, env);
        analyzeNode(intRangeExpression.endExpr, env);
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        analyzeNode(bLangVarArgsExpression.expr, env);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        analyzeNode(bLangNamedArgsExpression.expr, env);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        analyzeNode(checkedExpr.expr, env);
    }

    @Override
    public void visit(BLangDo doNode) {
        analyzeNode(doNode.body, env);
        if (doNode.onFailClause != null) {
            analyzeNode(doNode.onFailClause, env);
        }
    }

    @Override
    public void visit(BLangFail failExpr) {
        analyzeNode(failExpr.expr, env);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        analyzeNode(checkPanickedExpr.expr, env);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        analyzeNode(serviceConstructorExpr.serviceNode, env);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        analyzeNode(typeTestExpr.expr, env);
        analyzeNode(typeTestExpr.typeNode, env);
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        analyzeNode(annotAccessExpr.expr, env);
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        for (BLangNode clause : queryExpr.getQueryClauses()) {
            analyzeNode(clause, env);
        }
    }

    @Override
    public void visit(BLangTableMultiKeyExpr tableMultiKeyExpr) {
        for (BLangExpression value : tableMultiKeyExpr.multiKeyIndexExprs) {
            analyzeNode(value, env);
        }
    }

    @Override
    public void visit(BLangTransactionalExpr transactionalExpr) {
    }

    @Override
    public void visit(BLangCommitExpr commitExpr) {
    }

    @Override
    public void visit(BLangValueType valueType) {
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        analyzeNode(arrayType.getElementType(), env);
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        analyzeNode(constrainedType.constraint, env);
    }

    @Override
    public void visit(BLangStreamType streamType) {
        analyzeNode(streamType.constraint, env);
        analyzeNode(streamType.error, env);
    }

    @Override
    public void visit(BLangTableTypeNode tableType) {
        analyzeNode(tableType.constraint, env);

        if (tableType.tableKeyTypeConstraint != null) {
            analyzeNode(tableType.tableKeyTypeConstraint.keyType, env);
        }
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        for (BLangVariable param : functionTypeNode.params) {
            analyzeNode(param.typeNode, env);
        }
        analyzeNode(functionTypeNode.returnTypeNode, env);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        for (BLangType memberTypeNode : unionTypeNode.memberTypeNodes) {
            analyzeNode(memberTypeNode, env);
        }
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
        for (BLangType constituentTypeNode : intersectionTypeNode.constituentTypeNodes) {
            analyzeNode(constituentTypeNode, env);
        }
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        SymbolEnv objectEnv = SymbolEnv.createTypeEnv(objectTypeNode, objectTypeNode.symbol.scope, env);

        for (BLangSimpleVariable field : objectTypeNode.fields) {
            analyzeNode(field, objectEnv);
        }

        for (BLangSimpleVariable referencedField : objectTypeNode.referencedFields) {
            analyzeNode(referencedField, objectEnv);
        }

        BLangFunction initFunction = objectTypeNode.initFunction;
        if (initFunction != null) {
            analyzeNode(initFunction, objectEnv);
        }

        for (BLangFunction function : objectTypeNode.functions) {
            analyzeNode(function, objectEnv);
        }
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        SymbolEnv classEnv = SymbolEnv.createClassEnv(classDefinition, classDefinition.symbol.scope, env);

        for (BLangSimpleVariable bLangSimpleVariable : classDefinition.fields) {
            analyzeNode(bLangSimpleVariable, classEnv);
        }

        for (BLangSimpleVariable field : classDefinition.referencedFields) {
            analyzeNode(field, classEnv);
        }

        BLangFunction initFunction = classDefinition.initFunction;
        if (initFunction != null) {
            analyzeNode(initFunction, classEnv);
        }

        for (BLangFunction function : classDefinition.functions) {
            analyzeNode(function, classEnv);
        }
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        SymbolEnv typeEnv = SymbolEnv.createTypeEnv(recordTypeNode, recordTypeNode.symbol.scope, env);

        for (BLangSimpleVariable field : recordTypeNode.fields) {
            analyzeNode(field, typeEnv);
        }

        for (BLangSimpleVariable referencedField : recordTypeNode.referencedFields) {
            analyzeNode(referencedField, typeEnv);
        }

        BLangType restFieldType = recordTypeNode.restFieldType;
        if (restFieldType != null) {
            analyzeNode(restFieldType, typeEnv);
        }
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        for (BLangExpression expression : finiteTypeNode.valueSpace) {
            analyzeNode(expression, env);
        }
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        for (BLangType memberTypeNode : tupleTypeNode.memberTypeNodes) {
            analyzeNode(memberTypeNode, env);
        }

        analyzeNode(tupleTypeNode.restParamType, env);
    }

    @Override
    public void visit(BLangErrorType errorTypeNode) {
        analyzeNode(errorTypeNode.detailType, env);
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        analyzeNode(bLangTupleVariable.typeNode, env);

        BLangExpression expr = bLangTupleVariable.expr;
        if (expr != null) {
            analyzeNode(expr, env);
        }
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        analyzeNode(bLangTupleVariableDef.var, env);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        analyzeNode(bLangRecordVariable.typeNode, env);
        BLangExpression expr = bLangRecordVariable.expr;
        if (expr != null) {
            analyzeNode(expr, env);
        }
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        analyzeNode(bLangRecordVariableDef.var, env);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        analyzeNode(bLangErrorVariable.typeNode, env);
        analyzeNode(bLangErrorVariable.expr, env);

        for (BLangErrorVariable.BLangErrorDetailEntry bLangErrorDetailEntry : bLangErrorVariable.detail) {
            analyzeNode(bLangErrorDetailEntry.valueBindingPattern, env);
        }
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        analyzeNode(bLangErrorVariableDef.errorVariable, env);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause matchStaticBindingPatternClause) {
        analyzeNode(matchStaticBindingPatternClause.body, env);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause matchStmtStructuredBindingPatternClause) {
        analyzeNode(matchStmtStructuredBindingPatternClause.bindingPatternVariable, env);

        BLangExpression typeGuardExpr = matchStmtStructuredBindingPatternClause.typeGuardExpr;
        if (typeGuardExpr != null) {
            analyzeNode(typeGuardExpr, env);
        }

        analyzeNode(matchStmtStructuredBindingPatternClause.body, env);
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        for (BLangWaitForAllExpr.BLangWaitKeyValue keyValuePair : waitForAllExpr.keyValuePairs) {
            analyzeNode(keyValuePair, env);
        }
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
        BLangExpression keyExpr = waitKeyValue.keyExpr;
        if (keyExpr != null) {
            analyzeNode(keyExpr, env);
        }


        BLangExpression valueExpr = waitKeyValue.valueExpr;
        if (valueExpr != null) {
            analyzeNode(valueExpr, env);
        }
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        BLangExpression childIndex = xmlNavigation.childIndex;
        if (childIndex != null) {
            analyzeNode(childIndex, env);
        }
    }

    private boolean isBallerinaModule(BLangPackage module) {
        String orgName = module.packageID.orgName.value;
        return orgName.equals("ballerina") || orgName.equals("ballerinax");
    }

    private boolean isInIsolatedFunction(BLangInvokableNode enclInvokable) {
        if (enclInvokable == null) {
            // TODO: 14/11/20 This feels hack-y but cannot think of a different approach without a class variable
            // maintaining isolated-ness.
            if (env.node.getKind() != NodeKind.EXPR_FUNCTION_BODY ||
                    env.enclEnv.node.getKind() != NodeKind.ARROW_EXPR) {
                return false;
            }
            return Symbols.isFlagOn(((BLangArrowFunction) env.enclEnv.node).funcType.flags, Flags.ISOLATED);
        }

        return isIsolated(enclInvokable.symbol);
    }

    private boolean isRecordFieldDefaultValue(BLangType enclType) {
        if (enclType == null) {
            return false;
        }

        return enclType.getKind() == NodeKind.RECORD_TYPE;
    }

    private boolean isObjectFieldDefaultValueRequiringIsolation(SymbolEnv env) {
        BLangNode node = env.node;
        NodeKind kind = node.getKind();

        if (kind != NodeKind.CLASS_DEFN) {
            return false;
        }

        BLangClassDefinition classDefinition = (BLangClassDefinition) node;

        BLangFunction initFunction = classDefinition.initFunction;
        if (initFunction == null) {
            return true;
        }

        return isIsolated(initFunction.symbol);
    }

    private boolean isDefinitionReference(BSymbol symbol) {
        return Symbols.isTagOn(symbol, SymTag.SERVICE) ||
                Symbols.isTagOn(symbol, SymTag.TYPE_DEF) ||
                Symbols.isTagOn(symbol, SymTag.FUNCTION) ||
                Symbols.isFlagOn(symbol.flags, Flags.LISTENER);
    }

    private boolean isIsolated(BSymbol symbol) {
        return Symbols.isFlagOn(symbol.flags, Flags.ISOLATED);
    }
}
