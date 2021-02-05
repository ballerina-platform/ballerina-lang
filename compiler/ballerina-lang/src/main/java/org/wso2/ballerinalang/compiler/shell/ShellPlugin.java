/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.shell;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.clauses.OrderKeyNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
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
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangCaptureBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangListBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangRestBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangWildCardBindingPattern;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDynamicArgExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchGuard;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangObjectConstructorExpression;
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
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangConstPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorCauseMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorFieldMatchPatterns;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorMessageMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangFieldMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangListMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMappingMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangNamedArgMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangRestMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangSimpleMatchPattern;
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
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.compiler.CompilerOptionName.SHELL_MODE;

/**
 * Performs processing required by the Ballerina Shell.
 * If any is called an AssertionError will be thrown
 *
 * @since 2.0.0
 */
public class ShellPlugin extends BLangNodeVisitor {
    private static final CompilerContext.Key<ShellPlugin> SHELL_PLUGIN_KEY = new CompilerContext.Key<>();
    private final SymbolTable symTable;
    private final Types types;
    private final Boolean inShellMode;
    private BInvokableSymbol recallAnyFunction;
    private BInvokableSymbol recallAnyErrorFunction;
    private BInvokableSymbol memorizeFunction;
    private BLangNode result;

    private ShellPlugin(CompilerContext context) {
        context.put(SHELL_PLUGIN_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        CompilerOptions options = CompilerOptions.getInstance(context);
        this.inShellMode = options.isSet(SHELL_MODE)
                && Boolean.parseBoolean(options.get(SHELL_MODE));
    }

    public static ShellPlugin getInstance(CompilerContext context) {
        ShellPlugin shellPlugin = context.get(SHELL_PLUGIN_KEY);
        if (shellPlugin == null) {
            shellPlugin = new ShellPlugin(context);
        }

        return shellPlugin;
    }

    /**
     * Performs operations to prepare package to
     * the shell execution. Skips if {@code shellCode} flag is not set.
     *
     * @param pkgNode Package node to process.
     * @return Processed package node.
     */
    public BLangPackage perform(BLangPackage pkgNode) {
        if (!this.inShellMode) {
            return pkgNode;
        }
        return rewrite(pkgNode);
    }

    private BLangExpressionStmt memorizeInvocation(BLangSimpleVarRef.BLangPackageVarRef variable,
                                                   BLangExpression expression) {
        // Prepare parameters of the invocation
        ArrayList<BLangExpression> parameters = new ArrayList<>();
        parameters.add(NodeUtils.createStringLiteral(symTable, variable.varSymbol.name.value));
        parameters.add(NodeUtils.createTypeCastExpr(expression, symTable.anyOrErrorType));

        // Statement with the memorize invocation: MEM(a, b)
        BLangExpression memorizeExpr = NodeUtils.createInvocation(memorizeFunction, parameters);
        return NodeUtils.createStatement(memorizeExpr);
    }

    private BLangExpression recallInvocation(BInvokableSymbol invokableSymbol,
                                             BLangSimpleVarRef.BLangPackageVarRef variable) {
        // Prepare parameters of the invocation
        ArrayList<BLangExpression> parameters = new ArrayList<>();
        parameters.add(NodeUtils.createStringLiteral(symTable, variable.varSymbol.name.value));

        // Cast and return expression
        BLangExpression expression = NodeUtils.createInvocation(invokableSymbol, parameters);
        return NodeUtils.createTypeCastExpr(expression, variable.type);
    }

    // Rewrite functions

    private <E extends BLangNode> void rewrite(List<E> nodeList) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewrite(nodeList.get(i)));
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends BLangNode> E rewrite(E node) {
        if (node == null) {
            return null;
        }

        node.accept(this);

        if (this.result == null) {
            return node;
        }

        node = (E) this.result;
        this.result = null;
        return node;
    }

    // Visitor overrides

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.SHELL_PLUGIN)) {
            result = pkgNode;
            return;
        }
        // Find the built-in mem/rem functions
        for (BLangFunction function : pkgNode.functions) {
            String fnName = function.getName().getValue();
            if (fnName.equals("recall_any")) {
                recallAnyFunction = function.symbol;
            } else if (fnName.equals("recall_any_error")) {
                recallAnyErrorFunction = function.symbol;
            } else if (fnName.equals("memorize_h")) {
                memorizeFunction = function.symbol;
            }
        }

        if (recallAnyFunction == null
                || recallAnyErrorFunction == null
                || memorizeFunction == null) {
            return;
        }

        // Rewrite all places that can contain pkg var assignment
        rewrite(pkgNode.imports);
        rewrite(pkgNode.xmlnsList);
        rewrite(pkgNode.constants);
        rewrite(pkgNode.globalVars);
        rewrite(pkgNode.services);
        rewrite(pkgNode.annotations);
        rewrite(pkgNode.typeDefinitions);
        rewrite(pkgNode.classDefinitions);
        rewrite(pkgNode.functions);

        pkgNode.completedPhases.add(CompilerPhase.SHELL_PLUGIN);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        if (assignNode.varRef instanceof BLangSimpleVarRef.BLangPackageVarRef) {
            result = memorizeInvocation((BLangSimpleVarRef.BLangPackageVarRef) assignNode.varRef, assignNode.expr);
            return;
        }

        assignNode.expr = rewrite(assignNode.expr);
        assignNode.varRef = rewrite(assignNode.varRef);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        if (packageVarRef.internal || packageVarRef.symbol.name.value.equals("context_id")) {
            return;
        }
        BInvokableSymbol bInvokableSymbol = types.containsErrorType(packageVarRef.type)
                ? recallAnyErrorFunction : recallAnyFunction;
        result = recallInvocation(bInvokableSymbol, packageVarRef);
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        // Do not visit children, otherwise var ref can get written on
    }

    // Visitors to simply visit all children

    @Override
    public void visit(BLangFunction funcNode) {
        rewrite(funcNode.annAttachments);
        rewrite(funcNode.requiredParams);
        funcNode.restParam = rewrite(funcNode.restParam);
        rewrite(funcNode.returnTypeAnnAttachments);
        funcNode.returnTypeNode = rewrite(funcNode.returnTypeNode);
        funcNode.body = rewrite(funcNode.body);
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        xmlnsNode.namespaceURI = rewrite(xmlnsNode.namespaceURI);
    }

    @Override
    public void visit(BLangResourceFunction resourceFunction) {
        visit((BLangFunction) resourceFunction);
    }

    @Override
    public void visit(BLangBlockFunctionBody blockFuncBody) {
        rewrite(blockFuncBody.stmts);
    }

    @Override
    public void visit(BLangExprFunctionBody exprFuncBody) {
        exprFuncBody.expr = rewrite(exprFuncBody.expr);
    }

    @Override
    public void visit(BLangExternalFunctionBody externFuncBody) {
        rewrite(externFuncBody.annAttachments);
    }

    @Override
    public void visit(BLangService serviceNode) {
        rewrite(serviceNode.annAttachments);
        serviceNode.serviceClass = rewrite(serviceNode.serviceClass);
        rewrite(serviceNode.attachedExprs);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        typeDefinition.typeNode = rewrite(typeDefinition.typeNode);
        rewrite(typeDefinition.annAttachments);
    }

    @Override
    public void visit(BLangConstant constant) {
        constant.typeNode = rewrite(constant.typeNode);
        constant.expr = rewrite(constant.expr);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        rewrite(varNode.annAttachments);
        varNode.typeNode = rewrite(varNode.typeNode);
        varNode.expr = rewrite(varNode.expr);
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        rewrite(annotationNode.annAttachments);
        annotationNode.typeNode = rewrite(annotationNode.typeNode);
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        annAttachmentNode.expr = rewrite(annAttachmentNode.expr);
    }

    @Override
    public void visit(BLangTableKeySpecifier tableKeySpecifierNode) {
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint tableKeyTypeConstraint) {
        tableKeyTypeConstraint.keyType = rewrite(tableKeyTypeConstraint.keyType);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        rewrite(blockNode.stmts);
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
        lockStmtNode.body = rewrite(lockStmtNode.body);
        lockStmtNode.onFailClause = rewrite(lockStmtNode.onFailClause);
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
        unLockNode.body = rewrite(unLockNode.body);
        unLockNode.onFailClause = rewrite(unLockNode.onFailClause);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        compoundAssignNode.expr = rewrite(compoundAssignNode.expr);
        compoundAssignNode.varRef = rewrite(compoundAssignNode.varRef);
    }

    @Override
    public void visit(BLangRetry retryNode) {
        retryNode.retrySpec = rewrite(retryNode.retrySpec);
        retryNode.retryBody = rewrite(retryNode.retryBody);
        retryNode.onFailClause = rewrite(retryNode.onFailClause);
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        retryTransaction.retrySpec = rewrite(retryTransaction.retrySpec);
        retryTransaction.transaction = rewrite(retryTransaction.transaction);
    }

    @Override
    public void visit(BLangRetrySpec retrySpec) {
        rewrite(retrySpec.argExprs);
        retrySpec.retryManagerType = rewrite(retrySpec.retryManagerType);
    }

    @Override
    public void visit(BLangReturn returnNode) {
        returnNode.expr = rewrite(returnNode.expr);
    }

    @Override
    public void visit(BLangPanic panicNode) {
        panicNode.expr = rewrite(panicNode.expr);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl = rewrite(xmlnsStmtNode.xmlnsDecl);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.expr = rewrite(exprStmtNode.expr);
    }

    @Override
    public void visit(BLangIf ifNode) {
        ifNode.expr = rewrite(ifNode.expr);
        ifNode.body = rewrite(ifNode.body);
        ifNode.elseStmt = rewrite(ifNode.elseStmt);
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        queryAction.doClause = rewrite(queryAction.doClause);
        rewrite(queryAction.queryClauseList);
    }

    @Override
    public void visit(BLangMatch matchNode) {
        matchNode.expr = rewrite(matchNode.expr);
        rewrite(matchNode.patternClauses);
        matchNode.onFailClause = rewrite(matchNode.onFailClause);
    }

    @Override
    public void visit(BLangMatchStatement matchStatementNode) {
        matchStatementNode.expr = rewrite(matchStatementNode.expr);
        rewrite(matchStatementNode.matchClauses);
        matchStatementNode.onFailClause = rewrite(matchStatementNode.onFailClause);
    }

    @Override
    public void visit(BLangMatchGuard matchGuard) {
        rewrite(matchGuard.expr);
    }

    @Override
    public void visit(BLangConstPattern constMatchPattern) {
        constMatchPattern.expr = rewrite(constMatchPattern.expr);
        constMatchPattern.matchExpr = rewrite(constMatchPattern.matchExpr);
    }

    @Override
    public void visit(BLangWildCardMatchPattern wildCardMatchPattern) {
        rewrite(wildCardMatchPattern.matchExpr);
    }

    @Override
    public void visit(BLangVarBindingPatternMatchPattern varBindingPattern) {
        varBindingPattern.matchExpr = rewrite(varBindingPattern.matchExpr);
    }

    @Override
    public void visit(BLangCaptureBindingPattern captureBindingPattern) {
    }

    @Override
    public void visit(BLangListBindingPattern listBindingPattern) {
        rewrite(listBindingPattern.bindingPatterns);
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        patternClauseNode.body = rewrite(patternClauseNode.body);
        patternClauseNode.variable = rewrite(patternClauseNode.variable);
        patternClauseNode.matchExpr = rewrite(patternClauseNode.matchExpr);
    }

    @Override
    public void visit(BLangForeach foreach) {
        foreach.collection = rewrite(foreach.collection);
        foreach.body = rewrite(foreach.body);
        foreach.onFailClause = rewrite(foreach.onFailClause);
    }

    @Override
    public void visit(BLangDo doNode) {
        doNode.body = rewrite(doNode.body);
        doNode.onFailClause = rewrite(doNode.onFailClause);
    }

    @Override
    public void visit(BLangFail failNode) {
        rewrite(failNode.expr);
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        fromClause.collection = rewrite(fromClause.collection);
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
        joinClause.onClause = rewrite((BLangOnClause) joinClause.onClause);
        joinClause.collection = rewrite(joinClause.collection);
    }

    @Override
    public void visit(BLangLetClause letClause) {
    }

    @Override
    public void visit(BLangOnClause onClause) {
        onClause.lhsExpr = rewrite(onClause.lhsExpr);
        onClause.rhsExpr = rewrite(onClause.rhsExpr);
    }

    @Override
    public void visit(BLangOrderKey orderKeyClause) {
        rewrite(orderKeyClause.expression);
    }

    @Override
    public void visit(BLangOrderByClause orderByClause) {
        List<OrderKeyNode> orderByKeyList = orderByClause.orderByKeyList;
        for (int i = 0, orderByKeyListSize = orderByKeyList.size(); i < orderByKeyListSize; i++) {
            OrderKeyNode orderKeyNode = orderByKeyList.get(i);
            orderByClause.orderByKeyList.set(i, rewrite((BLangOrderKey) orderKeyNode));
        }
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        rewrite(selectClause.expression);
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        rewrite(whereClause.expression);
    }

    @Override
    public void visit(BLangDoClause doClause) {
        rewrite(doClause.body);
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
        onFailClause.body = rewrite(onFailClause.body);
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause) {
        rewrite(onConflictClause.expression);
    }

    @Override
    public void visit(BLangLimitClause limitClause) {
        rewrite(limitClause.expression);
    }

    @Override
    public void visit(BLangMatchClause matchClause) {
        matchClause.expr = rewrite(matchClause.expr);
        matchClause.matchGuard = rewrite(matchClause.matchGuard);
        matchClause.blockStmt = rewrite(matchClause.blockStmt);
        rewrite(matchClause.matchPatterns);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        whileNode.expr = rewrite(whileNode.expr);
        whileNode.body = rewrite(whileNode.body);
        whileNode.onFailClause = rewrite(whileNode.onFailClause);
    }

    @Override
    public void visit(BLangLock lockNode) {
        lockNode.body = rewrite(lockNode.body);
        lockNode.onFailClause = rewrite(lockNode.onFailClause);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        transactionNode.transactionBody = rewrite(transactionNode.transactionBody);
        transactionNode.onFailClause = rewrite(transactionNode.onFailClause);
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        stmt.expr = rewrite(stmt.expr);
        stmt.varRef = rewrite(stmt.varRef);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        stmt.expr = rewrite(stmt.expr);
        stmt.varRef = rewrite(stmt.varRef);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        stmt.expr = rewrite(stmt.expr);
        stmt.varRef = rewrite(stmt.varRef);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        rewrite(forkJoin.workers);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.expr = rewrite(workerSendNode.expr);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
    }

    public void visit(BLangRollback rollbackNode) {
        rewrite(rollbackNode.expr);
    }

    @Override
    public void visit(BLangConstRef constRef) {
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        rewrite(varRefExpr.expressions);
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        varRefExpr.typeNode = rewrite(varRefExpr.typeNode);
        varRefExpr.message = rewrite(varRefExpr.message);
        rewrite(varRefExpr.detail);
        varRefExpr.cause = rewrite(varRefExpr.cause);
        varRefExpr.restVar = rewrite(varRefExpr.restVar);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        fieldAccessExpr.expr = rewrite(fieldAccessExpr.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        indexAccessExpr.indexExpr = rewrite(indexAccessExpr.indexExpr);
        indexAccessExpr.expr = rewrite(indexAccessExpr.expr);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        if (!invocationExpr.langLibInvocation) {
            invocationExpr.expr = rewrite(invocationExpr.expr);
        }

        rewrite(invocationExpr.requiredArgs);
        rewrite(invocationExpr.annAttachments);
        rewrite(invocationExpr.restArgs);
    }

    @Override
    public void visit(BLangTypeInit typeInit) {
        typeInit.userDefinedType = rewrite(typeInit.userDefinedType);
        rewrite(typeInit.argsExpr);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        actionInvocationExpr.expr = rewrite(actionInvocationExpr.expr);
        rewrite(actionInvocationExpr.requiredArgs);
        rewrite(actionInvocationExpr.annAttachments);
        rewrite(actionInvocationExpr.restArgs);
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.expr = rewrite(ternaryExpr.expr);
        ternaryExpr.thenExpr = rewrite(ternaryExpr.thenExpr);
        ternaryExpr.elseExpr = rewrite(ternaryExpr.elseExpr);
    }

    @Override
    public void visit(BLangWaitExpr waitExpr) {
        rewrite(waitExpr.exprList);
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        trapExpr.expr = rewrite(trapExpr.expr);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.lhsExpr = rewrite(binaryExpr.lhsExpr);
        binaryExpr.rhsExpr = rewrite(binaryExpr.rhsExpr);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        elvisExpr.lhsExpr = rewrite(elvisExpr.lhsExpr);
        elvisExpr.rhsExpr = rewrite(elvisExpr.rhsExpr);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        groupExpr.expression = rewrite(groupExpr.expression);
    }

    @Override
    public void visit(BLangLetExpression letExpr) {
        letExpr.expr = rewrite(letExpr.expr);
    }

    @Override
    public void visit(BLangLetVariable letVariable) {
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        rewrite(listConstructorExpr.exprs);
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        rewrite(tableConstructorExpr.recordLiteralList);
        tableConstructorExpr.tableKeySpecifier = rewrite(tableConstructorExpr.tableKeySpecifier);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        unaryExpr.expr = rewrite(unaryExpr.expr);
    }

    @Override
    public void visit(BLangTypedescExpr typedescExpr) {
        typedescExpr.typeNode = rewrite(typedescExpr.typeNode);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        rewrite(conversionExpr.annAttachments);
        conversionExpr.typeNode = rewrite(conversionExpr.typeNode);
        conversionExpr.expr = rewrite(conversionExpr.expr);
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        xmlAttribute.name = rewrite(xmlAttribute.name);
        xmlAttribute.value = rewrite(xmlAttribute.value);
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        xmlElementLiteral.startTagName = rewrite(xmlElementLiteral.startTagName);
        xmlElementLiteral.endTagName = rewrite(xmlElementLiteral.endTagName);
        rewrite(xmlElementLiteral.children);
        rewrite(xmlElementLiteral.attributes);
        rewrite(xmlElementLiteral.inlineNamespaces);
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        rewrite(xmlTextLiteral.textFragments);
        xmlTextLiteral.concatExpr = rewrite(xmlTextLiteral.concatExpr);
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        rewrite(xmlCommentLiteral.textFragments);
        xmlCommentLiteral.concatExpr = rewrite(xmlCommentLiteral.concatExpr);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.target = rewrite(xmlProcInsLiteral.target);
        rewrite(xmlProcInsLiteral.dataFragments);
        xmlProcInsLiteral.dataConcatExpr = rewrite(xmlProcInsLiteral.dataConcatExpr);
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        rewrite(xmlQuotedString.textFragments);
        xmlQuotedString.concatExpr = rewrite(xmlQuotedString.concatExpr);
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        rewrite(stringTemplateLiteral.exprs);
    }

    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral) {
        rewrite(rawTemplateLiteral.insertions);
        rewrite(rawTemplateLiteral.strings);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        bLangLambdaFunction.function = rewrite(bLangLambdaFunction.function);
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        rewrite(bLangArrowFunction.params);
        bLangArrowFunction.body = rewrite(bLangArrowFunction.body);
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        intRangeExpression.startExpr = rewrite(intRangeExpression.startExpr);
        intRangeExpression.endExpr = rewrite(intRangeExpression.endExpr);
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        bLangVarArgsExpression.expr = rewrite(bLangVarArgsExpression.expr);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        bLangNamedArgsExpression.expr = rewrite(bLangNamedArgsExpression.expr);
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        assignableExpr.lhsExpr = rewrite(assignableExpr.lhsExpr);
        assignableExpr.typeNode = rewrite(assignableExpr.typeNode);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        checkedExpr.expr = rewrite(checkedExpr.expr);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        checkPanickedExpr.expr = rewrite(checkPanickedExpr.expr);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        serviceConstructorExpr.serviceNode = rewrite(serviceConstructorExpr.serviceNode);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        typeTestExpr.expr = rewrite(typeTestExpr.expr);
        typeTestExpr.typeNode = rewrite(typeTestExpr.typeNode);
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        typeTestExpr.expr = rewrite(typeTestExpr.expr);
        typeTestExpr.typeNode = rewrite(typeTestExpr.typeNode);
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        rewrite(queryExpr.queryClauseList);
    }

    @Override
    public void visit(BLangTableMultiKeyExpr tableMultiKeyExpr) {
        tableMultiKeyExpr.expr = rewrite(tableMultiKeyExpr.expr);
        rewrite(tableMultiKeyExpr.multiKeyIndexExprs);
    }

    @Override
    public void visit(BLangObjectConstructorExpression objConstructor) {
        objConstructor.classNode = rewrite(objConstructor.classNode);
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        arrayType.elemtype = rewrite(arrayType.elemtype);
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        constrainedType.type = rewrite(constrainedType.type);
        constrainedType.constraint = rewrite(constrainedType.constraint);
    }

    @Override
    public void visit(BLangStreamType streamType) {
        streamType.constraint = rewrite(streamType.constraint);
        streamType.error = rewrite(streamType.error);
    }

    @Override
    public void visit(BLangTableTypeNode tableType) {
        tableType.constraint = rewrite(tableType.constraint);
        tableType.tableKeySpecifier = rewrite(tableType.tableKeySpecifier);
        tableType.tableKeyTypeConstraint = rewrite(tableType.tableKeyTypeConstraint);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        rewrite(functionTypeNode.params);
        functionTypeNode.restParam = rewrite(functionTypeNode.restParam);
        functionTypeNode.returnTypeNode = rewrite(functionTypeNode.returnTypeNode);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        rewrite(unionTypeNode.memberTypeNodes);
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
        rewrite(intersectionTypeNode.constituentTypeNodes);
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        rewrite(objectTypeNode.fields);
        rewrite(objectTypeNode.functions);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        rewrite(recordTypeNode.fields);
        recordTypeNode.restFieldType = rewrite(recordTypeNode.restFieldType);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        rewrite(finiteTypeNode.valueSpace);
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        rewrite(tupleTypeNode.memberTypeNodes);
        tupleTypeNode.restParamType = rewrite(tupleTypeNode.restParamType);
    }

    @Override
    public void visit(BLangErrorType errorType) {
        errorType.detailType = rewrite(errorType.detailType);
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        errorConstructorExpr.errorTypeRef = rewrite(errorConstructorExpr.errorTypeRef);
        rewrite(errorConstructorExpr.positionalArgs);
        rewrite(errorConstructorExpr.namedArgs);
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        rewrite(bLangXMLSequenceLiteral.xmlItems);
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        rewrite(bLangTupleVariable.annAttachments);
        rewrite(bLangTupleVariable.memberVariables);
        bLangTupleVariable.restVariable = rewrite(bLangTupleVariable.restVariable);
        bLangTupleVariable.expr = rewrite(bLangTupleVariable.expr);
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        bLangTupleVariableDef.var = rewrite(bLangTupleVariableDef.var);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        for (BLangRecordVariable.BLangRecordVariableKeyValue variableKeyValue : bLangRecordVariable.variableList) {
            variableKeyValue.valueBindingPattern = rewrite(variableKeyValue.valueBindingPattern);
        }

        rewrite(bLangRecordVariable.annAttachments);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        bLangRecordVariableDef.var = rewrite(bLangRecordVariableDef.var);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        bLangErrorVariable.message = rewrite(bLangErrorVariable.message);
        bLangErrorVariable.restDetail = rewrite(bLangErrorVariable.restDetail);
        bLangErrorVariable.cause = rewrite(bLangErrorVariable.cause);
        bLangErrorVariable.reasonMatchConst = rewrite(bLangErrorVariable.reasonMatchConst);

        for (BLangErrorVariable.BLangErrorDetailEntry errorDetailEntry : bLangErrorVariable.detail) {
            errorDetailEntry.valueBindingPattern = rewrite(errorDetailEntry.valueBindingPattern);
        }
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        bLangErrorVariableDef.errorVariable = rewrite(bLangErrorVariableDef.errorVariable);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause clause) {
        clause.body = rewrite(clause.body);
        clause.literal = rewrite(clause.literal);
        clause.matchExpr = rewrite(clause.matchExpr);
    }

    @Override
    public void visit(
            BLangMatch.BLangMatchStructuredBindingPatternClause clause) {
        clause.body = rewrite(clause.body);
        clause.bindingPatternVariable = rewrite(clause.bindingPatternVariable);
        clause.matchExpr = rewrite(clause.matchExpr);
        clause.typeGuardExpr = rewrite(clause.typeGuardExpr);
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        syncSendExpr.expr = rewrite(syncSendExpr.expr);
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        rewrite(waitForAllExpr.keyValuePairs);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValueField recordKeyValue) {
        recordKeyValue.key = rewrite(recordKeyValue.key);
        recordKeyValue.valueExpr = rewrite(recordKeyValue.valueExpr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKey recordKey) {
        recordKey.expr = rewrite(recordKey.expr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOperatorField) {
        spreadOperatorField.expr = rewrite(spreadOperatorField.expr);
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
        waitKeyValue.keyExpr = rewrite(waitKeyValue.keyExpr);
        waitKeyValue.valueExpr = rewrite(waitKeyValue.valueExpr);
    }

    @Override
    public void visit(BLangXMLElementFilter xmlElementFilter) {
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
        xmlElementAccess.expr = rewrite(xmlElementAccess.expr);
        rewrite(xmlElementAccess.filters);
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        xmlNavigation.childIndex = rewrite(xmlNavigation.childIndex);
        rewrite(xmlNavigation.filters);
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        rewrite(classDefinition.annAttachments);
        rewrite(classDefinition.fields);
        classDefinition.initFunction = rewrite(classDefinition.initFunction);
        rewrite(classDefinition.functions);
        rewrite(classDefinition.typeRefs);
    }

    @Override
    public void visit(BLangListMatchPattern listMatchPattern) {
        listMatchPattern.matchExpr = rewrite(listMatchPattern.matchExpr);
        rewrite(listMatchPattern.matchPatterns);
        listMatchPattern.restMatchPattern = rewrite(listMatchPattern.restMatchPattern);
    }

    @Override
    public void visit(BLangRestMatchPattern restMatchPattern) {
        restMatchPattern.matchExpr = rewrite(restMatchPattern.matchExpr);
    }

    @Override
    public void visit(BLangMappingMatchPattern mappingMatchPattern) {
        rewrite(mappingMatchPattern.fieldMatchPatterns);
        mappingMatchPattern.restMatchPattern = rewrite(mappingMatchPattern.restMatchPattern);
    }

    @Override
    public void visit(BLangFieldMatchPattern fieldMatchPattern) {
        fieldMatchPattern.matchPattern = rewrite(fieldMatchPattern.matchPattern);
    }

    @Override
    public void visit(BLangErrorMatchPattern errorMatchPattern) {
        errorMatchPattern.errorCauseMatchPattern = rewrite(errorMatchPattern.errorCauseMatchPattern);
        errorMatchPattern.errorFieldMatchPatterns = rewrite(errorMatchPattern.errorFieldMatchPatterns);
        errorMatchPattern.errorMessageMatchPattern = rewrite(errorMatchPattern.errorMessageMatchPattern);
        errorMatchPattern.errorTypeReference = rewrite(errorMatchPattern.errorTypeReference);
    }

    @Override
    public void visit(BLangErrorMessageMatchPattern errorMessageMatchPattern) {
        errorMessageMatchPattern.simpleMatchPattern = rewrite(errorMessageMatchPattern.simpleMatchPattern);
    }

    @Override
    public void visit(BLangErrorCauseMatchPattern errorCauseMatchPattern) {
        errorCauseMatchPattern.simpleMatchPattern = rewrite(errorCauseMatchPattern.simpleMatchPattern);
        errorCauseMatchPattern.errorMatchPattern = rewrite(errorCauseMatchPattern.errorMatchPattern);
    }

    @Override
    public void visit(BLangErrorFieldMatchPatterns errorFieldMatchPatterns) {
        rewrite(errorFieldMatchPatterns.namedArgMatchPatterns);
        errorFieldMatchPatterns.restMatchPattern = rewrite(errorFieldMatchPatterns.restMatchPattern);
    }

    @Override
    public void visit(BLangSimpleMatchPattern simpleMatchPattern) {
        simpleMatchPattern.constPattern = rewrite(simpleMatchPattern.constPattern);
        simpleMatchPattern.varVariableName = rewrite(simpleMatchPattern.varVariableName);
        simpleMatchPattern.wildCardMatchPattern = rewrite(simpleMatchPattern.wildCardMatchPattern);
    }

    @Override
    public void visit(BLangNamedArgMatchPattern namedArgMatchPattern) {
        namedArgMatchPattern.matchPattern = rewrite(namedArgMatchPattern.matchPattern);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
        rewrite(tupleLiteral.exprs);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {
        rewrite(arrayLiteral.exprs);
    }

    @Override
    public void visit(BLangDynamicArgExpr dynamicParamExpr) {
        dynamicParamExpr.condition = rewrite(dynamicParamExpr.condition);
        dynamicParamExpr.conditionalArgument = rewrite(dynamicParamExpr.conditionalArgument);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        localVarRef.expr = rewrite(localVarRef.expr);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        fieldVarRef.expr = rewrite(fieldVarRef.expr);
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        varRefExpr.expr = rewrite(varRefExpr.expr);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        functionVarRef.expr = rewrite(functionVarRef.expr);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {
        typeLoad.expr = rewrite(typeLoad.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        fieldAccessExpr.expr = rewrite(fieldAccessExpr.expr);
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
        functionVarRef.expr = rewrite(functionVarRef.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        mapKeyAccessExpr.expr = rewrite(mapKeyAccessExpr.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        arrayIndexAccessExpr.expr = rewrite(arrayIndexAccessExpr.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlAccessExpr) {
        xmlAccessExpr.expr = rewrite(xmlAccessExpr.expr);
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        bFunctionPointerInvocation.expr = rewrite(bFunctionPointerInvocation.expr);
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        iExpr.expr = rewrite(iExpr.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        jsonAccessExpr.expr = rewrite(jsonAccessExpr.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
        stringAccessExpr.expr = rewrite(stringAccessExpr.expr);
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        bLangStatementExpression.expr = rewrite(bLangStatementExpression.expr);
        bLangStatementExpression.stmt = rewrite(bLangStatementExpression.stmt);
    }

    // Unimplemented


    @Override
    public void visit(BLangTestablePackage testablePkgNode) {
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
    }

    @Override
    public void visit(BLangContinue continueNode) {
    }

    @Override
    public void visit(BLangBreak breakNode) {
    }

    @Override
    public void visit(BLangWildCardBindingPattern wildCardBindingPattern) {
    }

    @Override
    public void visit(BLangRestBindingPattern restBindingPattern) {
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
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
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTableAccessExpr tableKeyAccessExpr) {
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
    }

    @Override
    public void visit(BLangRecordLiteral.BLangChannelLiteral channelLiteral) {
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine) {
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter) {
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation) {
    }

    @Override
    public void visit(BLangMarkDownDeprecationDocumentation bLangMarkDownDeprecationDocumentation) {
    }

    @Override
    public void visit(BLangMarkDownDeprecatedParametersDocumentation bLangMarkDownDeprecatedParametersDocumentation) {
    }

    @Override
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
    }

    @Override
    public void visit(BLangMarkdownReferenceDocumentation bLangMarkdownReferenceDocumentation) {
    }
}
