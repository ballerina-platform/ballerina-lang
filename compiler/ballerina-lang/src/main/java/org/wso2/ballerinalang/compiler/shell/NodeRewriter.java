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

import org.ballerinalang.model.clauses.OnClauseNode;
import org.ballerinalang.model.clauses.OrderKeyNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
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
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.util.LinkedList;
import java.util.List;

/**
 * Class that supports rewriting semantic tree.
 */
public abstract class NodeRewriter extends BLangNodeVisitor {
    protected BLangNode result;

    /* Rewrite functions */

    @SuppressWarnings("unchecked")
    protected <E extends BLangNode> E rewrite(E node) {
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

    protected <E extends BLangNode> List<E> rewrite(List<E> nodeList) {
        // Rewrite all the nodes in place
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewrite(nodeList.get(i)));
        }
        return nodeList;
    }

    /* Helpers */

    private void rewriteBLangInvokableNode(BLangInvokableNode node) {
        node.returnTypeNode = rewrite(node.returnTypeNode);
        node.body = rewrite(node.body);
        node.markdownDocumentationAttachment = rewrite(node.markdownDocumentationAttachment);
        node.restParam = rewrite(node.restParam);
    }

    private void rewriteBLangVariable(BLangVariable node) {
        node.typeNode = rewrite(node.typeNode);
        node.expr = rewrite(node.expr);
        node.markdownDocumentationAttachment = rewrite(node.markdownDocumentationAttachment);
    }

    private void rewriteBLangAccessExpression(BLangAccessExpression node) {
        node.expr = rewrite(node.expr);
    }

    private void rewriteBLangIndexBasedAccess(BLangIndexBasedAccess node) {
        node.indexExpr = rewrite(node.indexExpr);
        rewriteBLangAccessExpression(node);
    }

    private void rewriteBLangStructureTypeNode(BLangStructureTypeNode node) {
        node.initFunction = rewrite(node.initFunction);
    }

    private void rewriteBLangMatchBindingPatternClause(BLangMatch.BLangMatchBindingPatternClause node) {
        node.body = rewrite(node.body);
        node.matchExpr = rewrite(node.matchExpr);
    }

    private void rewriteFunctionNode(BLangFunction node) {
        rewriteBLangInvokableNode(node);
    }

    /* Visitors */

    @Override
    public void visit(BLangPackage node) {
        node.imports = rewrite(node.imports);
        node.xmlnsList = rewrite(node.xmlnsList);
        node.constants = rewrite(node.constants);
        node.globalVars = rewrite(node.globalVars);
        node.services = rewrite(node.services);
        node.annotations = rewrite(node.annotations);
        node.typeDefinitions = rewrite(node.typeDefinitions);
        node.functions = rewrite(node.functions);
        node.classDefinitions = rewrite(node.classDefinitions);
    }

    @Override
    public void visit(BLangTestablePackage node) {
        // Ignore
    }

    @Override
    public void visit(BLangCompilationUnit node) {
        // Ignore
    }

    @Override
    public void visit(BLangImportPackage node) {
        // Ignore
    }

    @Override
    public void visit(BLangXMLNS node) {
        node.namespaceURI = rewrite(node.namespaceURI);
    }

    @Override
    public void visit(BLangFunction node) {
        rewriteFunctionNode(node);
    }

    @Override
    public void visit(BLangBlockFunctionBody node) {
        rewrite(node.stmts);
    }

    @Override
    public void visit(BLangExprFunctionBody node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangExternalFunctionBody node) {
        node.annAttachments = rewrite(node.annAttachments);
    }

    @Override
    public void visit(BLangService node) {
        node.annAttachments = rewrite(node.annAttachments);
        node.markdownDocumentationAttachment = rewrite(node.markdownDocumentationAttachment);
        node.serviceClass = rewrite(node.serviceClass);
        node.attachedExprs = rewrite(node.attachedExprs);
        node.serviceVariable = rewrite(node.serviceVariable);
        node.serviceNameLiteral = rewrite(node.serviceNameLiteral);
        node.variableNode = rewrite(node.variableNode);
    }

    @Override
    public void visit(BLangResource node) {
        // Ignore
    }

    @Override
    public void visit(BLangTypeDefinition node) {
        node.typeNode = rewrite(node.typeNode);
        node.annAttachments = rewrite(node.annAttachments);
        node.markdownDocumentationAttachment = rewrite(node.markdownDocumentationAttachment);
    }

    @Override
    public void visit(BLangConstant node) {
        node.associatedTypeDefinition = rewrite(node.associatedTypeDefinition);
        rewriteBLangVariable(node);
    }

    @Override
    public void visit(BLangSimpleVariable node) {
        rewriteBLangVariable(node);
    }

    @Override
    public void visit(BLangWorker workerNode) {
        // Ignore
    }

    @Override
    public void visit(BLangIdentifier node) {
        // Ignore
    }

    @Override
    public void visit(BLangAnnotation node) {
        node.annAttachments = rewrite(node.annAttachments);
        node.markdownDocumentationAttachment = rewrite(node.markdownDocumentationAttachment);
        node.typeNode = rewrite(node.typeNode);
    }

    @Override
    public void visit(BLangAnnotationAttachment node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangBlockStmt node) {
        node.stmts = rewrite(node.stmts);
    }

    @Override
    public void visit(BLangLock.BLangLockStmt node) {
        node.body = rewrite(node.body);
        node.onFailClause = rewrite(node.onFailClause);
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt node) {
        node.body = rewrite(node.body);
        node.onFailClause = rewrite(node.onFailClause);
    }

    @Override
    public void visit(BLangSimpleVariableDef node) {
        node.var = rewrite(node.var);
    }

    @Override
    public void visit(BLangAssignment node) {
        node.varRef = rewrite(node.varRef);
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangCompoundAssignment node) {
        node.varRef = rewrite(node.varRef);
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangRetrySpec node) {
        node.retryManagerType = rewrite(node.retryManagerType);
        node.argExprs = rewrite(node.argExprs);
    }

    @Override
    public void visit(BLangRetry node) {
        node.retrySpec = rewrite(node.retrySpec);
        node.retryBody = rewrite(node.retryBody);
        node.onFailClause = rewrite(node.onFailClause);
    }

    @Override
    public void visit(BLangRetryTransaction node) {
        node.retrySpec = rewrite(node.retrySpec);
        node.transaction = rewrite(node.transaction);
    }

    @Override
    public void visit(BLangContinue node) {
        // Ignore
    }

    @Override
    public void visit(BLangBreak node) {
        // Ignore
    }

    @Override
    public void visit(BLangReturn node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangPanic node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangXMLNSStatement node) {
        node.xmlnsDecl = rewrite(node.xmlnsDecl);
    }

    @Override
    public void visit(BLangExpressionStmt node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangIf node) {
        node.expr = rewrite(node.expr);
        node.body = rewrite(node.body);
        node.elseStmt = rewrite(node.elseStmt);
    }

    @Override
    public void visit(BLangMatch node) {
        node.expr = rewrite(node.expr);
        node.patternClauses = rewrite(node.patternClauses);
        node.onFailClause = rewrite(node.onFailClause);
    }

    @Override
    public void visit(BLangMatchStatement node) {
        node.matchClauses = rewrite(node.matchClauses);
    }

    @Override
    public void visit(BLangMatchClause node) {
        node.matchPatterns = rewrite(node.matchPatterns);
        node.matchGuard = rewrite(node.getMatchGuard());
        node.blockStmt = rewrite(node.blockStmt);
    }

    @Override
    public void visit(BLangMatchGuard node) {
        // Ignore
    }

    @Override
    public void visit(BLangWildCardMatchPattern node) {
        // Ignore
    }

    @Override
    public void visit(BLangConstPattern node) {
        // Ignore
    }

    @Override
    public void visit(BLangVarBindingPatternMatchPattern node) {
        // Ignore
    }

    @Override
    public void visit(BLangListMatchPattern node) {
        node.restMatchPattern = rewrite(node.restMatchPattern);
        node.matchPatterns = rewrite(node.matchPatterns);
    }

    @Override
    public void visit(BLangCaptureBindingPattern node) {
        // Ignore
    }

    @Override
    public void visit(BLangMappingMatchPattern node) {
        node.fieldMatchPatterns = rewrite(node.fieldMatchPatterns);
        node.restMatchPattern = rewrite(node.restMatchPattern);
    }

    @Override
    public void visit(BLangRestMatchPattern node) {
        // Ignore
    }

    @Override
    public void visit(BLangWildCardBindingPattern node) {
        // Ignore
    }

    @Override
    public void visit(BLangErrorMatchPattern node) {
        node.errorMessageMatchPattern = rewrite(node.errorMessageMatchPattern);
        node.errorFieldMatchPatterns = rewrite(node.errorFieldMatchPatterns);
        node.errorCauseMatchPattern = rewrite(node.errorCauseMatchPattern);
    }

    @Override
    public void visit(BLangErrorMessageMatchPattern node) {
        node.simpleMatchPattern = rewrite(node.simpleMatchPattern);
    }

    @Override
    public void visit(BLangErrorCauseMatchPattern node) {
        node.errorMatchPattern = rewrite(node.errorMatchPattern);
        node.simpleMatchPattern = rewrite(node.simpleMatchPattern);
    }

    @Override
    public void visit(BLangNamedArgMatchPattern node) {
        node.matchPattern = rewrite(node.matchPattern);
    }

    @Override
    public void visit(BLangErrorFieldMatchPatterns node) {
        node.namedArgMatchPatterns = rewrite(node.namedArgMatchPatterns);
        node.restMatchPattern = rewrite(node.restMatchPattern);
    }

    @Override
    public void visit(BLangSimpleMatchPattern node) {
        node.wildCardMatchPattern = rewrite(node.wildCardMatchPattern);
        node.constPattern = rewrite(node.constPattern);
        node.varVariableName = rewrite(node.varVariableName);
    }

    @Override
    public void visit(BLangFieldMatchPattern node) {
        node.matchPattern = rewrite(node.matchPattern);
    }

    @Override
    public void visit(BLangListBindingPattern node) {
        node.bindingPatterns = rewrite(node.bindingPatterns);
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause node) {
        node.variable = rewrite(node.variable);
        rewriteBLangMatchBindingPatternClause(node);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause node) {
        node.literal = rewrite(node.literal);
        rewriteBLangMatchBindingPatternClause(node);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause node) {
        node.bindingPatternVariable = rewrite(node.bindingPatternVariable);
        node.typeGuardExpr = rewrite(node.typeGuardExpr);
        rewriteBLangMatchBindingPatternClause(node);
    }

    @Override
    public void visit(BLangForeach node) {
        node.collection = rewrite(node.collection);
        node.body = rewrite(node.body);
        node.variableDefinitionNode = (VariableDefinitionNode) rewrite((BLangNode) node.variableDefinitionNode);
        node.onFailClause = rewrite(node.onFailClause);
    }

    @Override
    public void visit(BLangWhile node) {
        node.expr = rewrite(node.expr);
        node.body = rewrite(node.body);
        node.onFailClause = rewrite(node.onFailClause);
    }

    @Override
    public void visit(BLangDo node) {
        node.onFailClause = rewrite(node.onFailClause);
        node.body = rewrite(node.body);
    }

    public void visit(BLangFail node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangLock node) {
        node.body = rewrite(node.body);
        node.onFailClause = rewrite(node.onFailClause);
    }

    @Override
    public void visit(BLangTransaction node) {
        node.onFailClause = rewrite(node.onFailClause);
        node.transactionBody = rewrite(node.transactionBody);
    }

    @Override
    public void visit(BLangRollback node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangTryCatchFinally node) {
        // Ignore
    }

    @Override
    public void visit(BLangTupleDestructure node) {
        node.varRef = rewrite(node.varRef);
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangRecordDestructure node) {
        node.varRef = rewrite(node.varRef);
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangErrorDestructure node) {
        node.varRef = rewrite(node.varRef);
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangCatch catchNode) {
        // Ignore
    }

    @Override
    public void visit(BLangForkJoin node) {
        node.workers = rewrite(node.workers);
    }

    @Override
    public void visit(BLangWorkerSend node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangWorkerReceive node) {
        // Ignore
    }

    @Override
    public void visit(BLangLiteral node) {
        // Ignore
    }

    @Override
    public void visit(BLangNumericLiteral node) {
        // Ignore
    }

    @Override
    public void visit(BLangRecordLiteral node) {
        // Ignore
    }

    @Override
    public void visit(BLangTupleVarRef node) {
        node.expressions = rewrite(node.expressions);
        node.restParam = (ExpressionNode) rewrite((BLangNode) node.restParam);
    }

    @Override
    public void visit(BLangRecordVarRef node) {
        node.recordRefFields.forEach(field -> field.variableReference = rewrite(field.variableReference));
        node.restParam = (ExpressionNode) rewrite((BLangNode) node.restParam);
    }

    @Override
    public void visit(BLangErrorVarRef node) {
        node.message = rewrite(node.message);
        node.cause = rewrite(node.cause);
        node.detail = rewrite(node.detail);
        node.restVar = rewrite(node.restVar);
        node.typeNode = rewrite(node.typeNode);
    }

    @Override
    public void visit(BLangSimpleVarRef node) {
        // Ignore
    }

    @Override
    public void visit(BLangFieldBasedAccess node) {
        rewriteBLangAccessExpression(node);
    }

    @Override
    public void visit(BLangIndexBasedAccess node) {
        rewriteBLangIndexBasedAccess(node);
    }

    @Override
    public void visit(BLangTableMultiKeyExpr node) {
        node.multiKeyIndexExprs = rewrite(node.multiKeyIndexExprs);
    }

    @Override
    public void visit(BLangInvocation node) {
        node.argExprs = rewrite(node.argExprs);
        node.annAttachments = rewrite(node.annAttachments);
        node.requiredArgs = rewrite(node.requiredArgs);
        node.restArgs = rewrite(node.restArgs);
        rewriteBLangAccessExpression(node);
    }

    @Override
    public void visit(BLangTypeInit node) {
        node.userDefinedType = rewrite(node.userDefinedType);
        node.argsExpr = rewrite(node.argsExpr);
        node.initInvocation = rewrite(node.initInvocation);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation node) {
        node.argExprs = rewrite(node.argExprs);
        node.annAttachments = rewrite(node.annAttachments);
        node.requiredArgs = rewrite(node.requiredArgs);
        rewriteBLangAccessExpression(node);
    }

    @Override
    public void visit(BLangTernaryExpr node) {
        node.expr = rewrite(node.expr);
        node.thenExpr = rewrite(node.thenExpr);
        node.elseExpr = rewrite(node.elseExpr);
    }

    @Override
    public void visit(BLangWaitExpr node) {
        node.exprList = rewrite(node.exprList);
    }

    @Override
    public void visit(BLangTrapExpr node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangBinaryExpr node) {
        node.lhsExpr = rewrite(node.lhsExpr);
        node.rhsExpr = rewrite(node.rhsExpr);
    }

    @Override
    public void visit(BLangElvisExpr node) {
        node.lhsExpr = rewrite(node.lhsExpr);
        node.rhsExpr = rewrite(node.rhsExpr);
    }

    @Override
    public void visit(BLangGroupExpr node) {
        node.expression = rewrite(node.expression);
    }

    @Override
    public void visit(BLangLetExpression node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangListConstructorExpr node) {
        node.exprs = rewrite(node.exprs);
    }

    public void visit(BLangTableConstructorExpr node) {
        node.recordLiteralList = rewrite(node.recordLiteralList);
        node.tableKeySpecifier = rewrite(node.tableKeySpecifier);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral node) {
        rewrite(node.exprs);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral node) {
        rewrite(node.exprs);
    }

    @Override
    public void visit(BLangUnaryExpr node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangTypedescExpr node) {
        node.typeNode = rewrite(node.typeNode);
    }

    @Override
    public void visit(BLangTypeConversionExpr node) {
        node.expr = rewrite(node.expr);
        node.typeNode = rewrite(node.typeNode);
        node.annAttachments = rewrite(node.annAttachments);
    }

    @Override
    public void visit(BLangXMLQName node) {
        // Ignore
    }

    @Override
    public void visit(BLangXMLAttribute node) {
        node.name = rewrite(node.name);
        node.value = rewrite(node.value);
    }

    @Override
    public void visit(BLangXMLElementLiteral node) {
        node.startTagName = rewrite(node.startTagName);
        node.endTagName = rewrite(node.endTagName);
        node.attributes = rewrite(node.attributes);
        node.children = rewrite(node.children);
        node.inlineNamespaces = rewrite(node.inlineNamespaces);
    }

    @Override
    public void visit(BLangXMLTextLiteral node) {
        node.textFragments = rewrite(node.textFragments);
        node.concatExpr = rewrite(node.concatExpr);
    }

    @Override
    public void visit(BLangXMLCommentLiteral node) {
        node.textFragments = rewrite(node.textFragments);
        node.concatExpr = rewrite(node.concatExpr);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral node) {
        node.target = rewrite(node.target);
        node.dataFragments = rewrite(node.dataFragments);
        node.dataConcatExpr = rewrite(node.dataConcatExpr);
    }

    @Override
    public void visit(BLangXMLQuotedString node) {
        node.textFragments = rewrite(node.textFragments);
        node.concatExpr = rewrite(node.concatExpr);
    }

    @Override
    public void visit(BLangStringTemplateLiteral node) {
        node.exprs = rewrite(node.exprs);
    }

    @Override
    public void visit(BLangRawTemplateLiteral node) {
        node.strings = rewrite(node.strings);
        node.insertions = rewrite(node.insertions);
    }

    @Override
    public void visit(BLangLambdaFunction node) {
        node.function = rewrite(node.function);
    }

    @Override
    public void visit(BLangArrowFunction node) {
        node.params = rewrite(node.params);
        node.body = rewrite(node.body);
    }

    @Override
    public void visit(BLangXMLAttributeAccess node) {
        rewriteBLangIndexBasedAccess(node);
    }

    @Override
    public void visit(BLangIntRangeExpression node) {
        node.startExpr = rewrite(node.startExpr);
        node.endExpr = rewrite(node.endExpr);
    }

    @Override
    public void visit(BLangRestArgsExpression node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangNamedArgsExpression node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangIsAssignableExpr node) {
        node.lhsExpr = rewrite(node.lhsExpr);
        node.typeNode = rewrite(node.typeNode);
    }

    @Override
    public void visit(BLangMatchExpression node) {
        // Ignore
    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause node) {
        // Ignore
    }

    @Override
    public void visit(BLangCheckedExpr node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangCheckPanickedExpr node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangErrorConstructorExpr node) {
        node.namedArgs = rewrite(node.namedArgs);
        node.positionalArgs = rewrite(node.positionalArgs);
    }

    @Override
    public void visit(BLangServiceConstructorExpr node) {
        node.serviceNode = rewrite(node.serviceNode);
    }

    @Override
    public void visit(BLangTypeTestExpr node) {
        node.expr = rewrite(node.expr);
        node.typeNode = rewrite(node.typeNode);
    }

    @Override
    public void visit(BLangIsLikeExpr node) {
        node.expr = rewrite(node.expr);
        node.typeNode = rewrite(node.typeNode);
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
        // Ignore
    }

    @Override
    public void visit(BLangAnnotAccessExpr node) {
        rewriteBLangAccessExpression(node);
    }

    @Override
    public void visit(BLangQueryAction node) {
        node.queryClauseList = rewrite(node.queryClauseList);
    }

    @Override
    public void visit(BLangQueryExpr node) {
        node.queryClauseList = rewrite(node.queryClauseList);
    }

    @Override
    public void visit(BLangFromClause node) {
        node.variableDefinitionNode = (VariableDefinitionNode) rewrite((BLangNode) node.variableDefinitionNode);
        node.collection = rewrite(node.collection);
    }

    @Override
    public void visit(BLangJoinClause node) {
        node.variableDefinitionNode = (VariableDefinitionNode) rewrite((BLangNode) node.variableDefinitionNode);
        node.collection = rewrite(node.collection);
        node.onClause = (OnClauseNode) rewrite((BLangNode) node.onClause);

    }

    @Override
    public void visit(BLangLetClause node) {
        // Ignore
    }

    @Override
    public void visit(BLangOnClause node) {
        node.lhsExpr = rewrite(node.lhsExpr);
        node.rhsExpr = rewrite(node.rhsExpr);
    }

    @Override
    public void visit(BLangOrderKey node) {
        node.expression = rewrite(node.expression);
    }

    @Override
    public void visit(BLangOrderByClause node) {
        List<OrderKeyNode> orderByKeyList = node.orderByKeyList;
        for (int i = 0, orderByKeyListSize = orderByKeyList.size(); i < orderByKeyListSize; i++) {
            OrderKeyNode orderKeyNode = orderByKeyList.get(i);
            node.orderByKeyList.set(i, rewrite((BLangOrderKey) orderKeyNode));
        }
    }

    @Override
    public void visit(BLangSelectClause node) {
        node.expression = rewrite(node.expression);
    }

    @Override
    public void visit(BLangOnConflictClause node) {
        node.expression = rewrite(node.expression);
    }

    @Override
    public void visit(BLangLimitClause node) {
        node.expression = rewrite(node.expression);
    }

    @Override
    public void visit(BLangWhereClause node) {
        node.expression = rewrite(node.expression);
    }

    @Override
    public void visit(BLangDoClause node) {
        node.body = rewrite(node.body);
    }

    @Override
    public void visit(BLangOnFailClause node) {
        node.body = rewrite(node.body);
    }


    @Override
    public void visit(BLangValueType node) {
        // Ignore
    }

    @Override
    public void visit(BLangArrayType node) {
        node.elemtype = rewrite(node.elemtype);
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode node) {
        // Ignore
    }

    @Override
    public void visit(BLangConstrainedType node) {
        node.type = rewrite(node.type);
        node.constraint = rewrite(node.constraint);
    }

    @Override
    public void visit(BLangStreamType node) {
        node.type = rewrite(node.type);
        node.constraint = rewrite(node.constraint);
        node.error = rewrite(node.error);
    }

    @Override
    public void visit(BLangUserDefinedType node) {
        // Ignore
    }

    @Override
    public void visit(BLangFunctionTypeNode node) {
        node.params = rewrite(node.params);
        node.restParam = rewrite(node.restParam);
        node.returnTypeNode = rewrite(node.returnTypeNode);
    }

    @Override
    public void visit(BLangUnionTypeNode node) {
        node.memberTypeNodes = rewrite(node.memberTypeNodes);
    }

    @Override
    public void visit(BLangIntersectionTypeNode node) {
        node.constituentTypeNodes = rewrite(node.constituentTypeNodes);
    }

    @Override
    public void visit(BLangObjectTypeNode node) {
        node.functions = rewrite(node.functions);
        node.initFunction = rewrite(node.initFunction);
        rewriteBLangStructureTypeNode(node);
    }

    @Override
    public void visit(BLangRecordTypeNode node) {
        node.restFieldType = rewrite(node.restFieldType);
        rewriteBLangStructureTypeNode(node);
    }

    @Override
    public void visit(BLangTableTypeNode node) {
        node.type = rewrite(node.type);
        node.tableKeySpecifier = rewrite(node.tableKeySpecifier);
        node.tableKeyTypeConstraint = rewrite(node.tableKeyTypeConstraint);
        node.constraint = rewrite(node.constraint);
    }

    @Override
    public void visit(BLangTableKeySpecifier node) {
        // Ignore
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint node) {
        node.keyType = rewrite(node.keyType);
    }

    @Override
    public void visit(BLangFiniteTypeNode node) {
        node.valueSpace = rewrite(node.valueSpace);
    }

    @Override
    public void visit(BLangTupleTypeNode node) {
        node.memberTypeNodes = rewrite(node.memberTypeNodes);
        node.restParamType = rewrite(node.restParamType);
    }

    @Override
    public void visit(BLangErrorType node) {
        node.detailType = rewrite(node.detailType);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef node) {
        // Ignore
    }

    @Override
    public void visit(BLangConstRef node) {
        // Ignore
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr node) {
        node.expr = rewrite(node.expr);
        node.indexExpr = rewrite(node.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr node) {
        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTableAccessExpr node) {
        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral node) {
        // Ignore
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral node) {
        // Ignore
    }

    @Override
    public void visit(BLangRecordLiteral.BLangChannelLiteral node) {
        // Ignore
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral node) {
        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        // Ignore
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        // Ignore
    }

    @Override
    public void visit(BLangXMLSequenceLiteral node) {
        rewrite(node.xmlItems);
    }

    @Override
    public void visit(BLangStatementExpression node) {
        node.expr = rewrite(node.expr);
        node.stmt = rewrite(node.stmt);
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine node) {
        // Ignore
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation node) {
        // Ignore
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation node) {
        // Ignore
    }

    @Override
    public void visit(BLangMarkDownDeprecationDocumentation node) {
        // Ignore
    }

    @Override
    public void visit(BLangMarkDownDeprecatedParametersDocumentation node) {
        // Ignore
    }

    @Override
    public void visit(BLangMarkdownDocumentation node) {
        node.documentationLines = new LinkedList<>(rewrite(node.documentationLines));
        node.parameters = new LinkedList<>(rewrite(node.parameters));
        node.references = new LinkedList<>(rewrite(node.references));
        node.returnParameter = rewrite(node.returnParameter);
        node.deprecationDocumentation = rewrite(node.deprecationDocumentation);
        node.deprecatedParametersDocumentation = rewrite(node.deprecatedParametersDocumentation);
    }

    @Override
    public void visit(BLangTupleVariable node) {
        node.memberVariables = rewrite(node.memberVariables);
        node.restVariable = rewrite(node.restVariable);
        rewriteBLangVariable(node);
    }

    @Override
    public void visit(BLangTupleVariableDef node) {
        node.var = rewrite(node.var);
    }

    @Override
    public void visit(BLangRecordVariable node) {
        node.variableList.forEach(keyValue -> keyValue.valueBindingPattern = rewrite(keyValue.valueBindingPattern));
        node.restParam = rewrite((BLangVariable) node.restParam);
        rewriteBLangVariable(node);
    }

    @Override
    public void visit(BLangRecordVariableDef node) {
        node.var = rewrite(node.var);
    }

    @Override
    public void visit(BLangErrorVariable node) {
        node.message = rewrite(node.message);
        node.detail.forEach(entry -> entry.valueBindingPattern = rewrite(entry.valueBindingPattern));
        node.restDetail = rewrite(node.restDetail);
        node.detailExpr = rewrite(node.detailExpr);
        node.cause = rewrite(node.cause);
        rewriteBLangVariable(node);
    }

    @Override
    public void visit(BLangErrorVariableDef node) {
        node.errorVariable = rewrite(node.errorVariable);
    }

    @Override
    public void visit(BLangWorkerFlushExpr node) {
        // Ignore
    }

    @Override
    public void visit(BLangCommitExpr node) {
    }

    @Override
    public void visit(BLangTransactionalExpr node) {
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangWaitForAllExpr node) {
        node.keyValuePairs.forEach(keyValue -> node.keyValuePairs.add(rewrite(keyValue)));
    }

    @Override
    public void visit(BLangMarkdownReferenceDocumentation node) {
        // Ignore
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral node) {
        // Ignore
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValueField node) {
        node.key = rewrite(node.key);
        node.valueExpr = rewrite(node.valueExpr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordSpreadOperatorField node) {
        node.expr = rewrite(node.expr);
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue node) {
        node.valueExpr = rewrite(node.valueExpr);
        node.keyExpr = rewrite(node.keyExpr);
    }

    @Override
    public void visit(BLangXMLElementFilter node) {
        // Ignore
    }

    @Override
    public void visit(BLangXMLElementAccess node) {
        // Ignore
    }

    @Override
    public void visit(BLangXMLNavigationAccess node) {
        // Ignore
    }

    @Override
    public void visit(BLangClassDefinition node) {
        node.annAttachments = rewrite(node.annAttachments);
        node.markdownDocumentationAttachment = rewrite(node.markdownDocumentationAttachment);
        node.name = rewrite(node.name);
        node.functions = rewrite(node.functions);
        node.fields = rewrite(node.fields);
        node.typeRefs = rewrite(node.typeRefs);
        node.initFunction = rewrite(node.initFunction);
        node.generatedInitFunction = rewrite(node.generatedInitFunction);
        node.receiver = rewrite(node.receiver);
    }

    @Override
    public void visit(BLangResourceFunction node) {
        rewriteFunctionNode(node);
        node.resourcePath = rewrite(node.resourcePath);
        node.methodName = rewrite(node.methodName);
        node.restPathParam = rewrite(node.restPathParam);
        node.pathParams = rewrite(node.pathParams);
    }

    @Override
    public void visit(BLangObjectConstructorExpression node) {
        node.classNode = rewrite(node.classNode);
        node.typeInit = rewrite(node.typeInit);
        node.referenceType = rewrite(node.referenceType);
    }

    @Override
    public void visit(BLangThrow throwNode) {
        // Ignore
    }

    @Override
    public void visit(BLangRestBindingPattern node) {
        // Ignore
    }

    @Override
    public void visit(BLangLetVariable node) {
        // Ignore
    }

    @Override
    public void visit(BLangDynamicArgExpr node) {
        node.condition = rewrite(node.condition);
        node.conditionalArgument = rewrite(node.conditionalArgument);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKey node) {
        node.expr = rewrite(node.expr);
    }
}
