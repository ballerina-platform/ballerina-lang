/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.parser;

import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.clauses.PatternStreamingEdgeInputNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.StreamingQueryStatementNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
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
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFunctionClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimit;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOutputRateLimit;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingEdgeInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectExpression;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSetAssignment;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamAction;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangTableQuery;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhere;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWindow;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWithinClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeMap;

/**
 * Node Visitor for cleanup type-checked information in AST nodes.
 *
 * @since 1.1
 */
class ASTCleaner extends BLangNodeVisitor {

    private static final CompilerContext.Key<ASTCleaner> AST_CLEANER_KEY =
            new CompilerContext.Key<>();

    ASTCleaner(CompilerContext context) {

    }

    public static ASTCleaner getInstance(CompilerContext context) {

        ASTCleaner cleaner = context.get(AST_CLEANER_KEY);
        if (cleaner == null) {
            cleaner = new ASTCleaner(context);
        }
        return cleaner;
    }

    /**
     * Reset attributes in expression node list.
     *
     * @param nodes the list of AST nodes to be reset
     */
    private void clearNodeList(List<? extends BLangNode> nodes) {

        for (BLangNode node : nodes) {
            clearNode(node);
        }
    }

    /**
     * Reset common attributes in an AST node.
     *
     * @param node the AST node to be reset
     */
    private void clearNode(BLangNode node) {

        if (node == null) {
            return;
        }
        node.accept(this);
    }

    /* Helper methods */
    private void resetNode(BLangNode node) {

        node.type = null;
        node.parent = null;
    }

    private void resetBLangVariable(BLangVariable variable) {

        clearNode(variable.typeNode);
        clearNode(variable.expr);
        clearNodeList(variable.annAttachments);
        clearNode(variable.markdownDocumentationAttachment);
        variable.symbol = null;
        resetNode(variable);
    }

    private void resetBLangInvokableNode(BLangInvokableNode node) {

        // TODO : Check for Flags.
        node.symbol = null;
        node.clonedEnv = null;
        node.desugaredReturnType = false;
        clearNodeList(node.requiredParams);
        clearNode(node.returnTypeNode);
        clearNodeList(node.returnTypeAnnAttachments);
        clearNodeList(node.externalAnnAttachments);
        clearNode(node.body);
        clearNodeList(node.annAttachments);
        clearNode(node.markdownDocumentationAttachment);
        clearNode(node.restParam);
        resetNode(node);
    }

    private void resetBLangVariableReference(BLangVariableReference varRef) {

        varRef.symbol = null;
        varRef.pkgSymbol = null;
        resetBLangExpression(varRef);
    }

    private void resetBLangAccessExpression(BLangAccessExpression accessExpression) {

        accessExpression.originalType = null;
        clearNode(accessExpression.expr);
        resetBLangExpression(accessExpression);
    }

    private void resetBLangStatement(BLangStatement statement) {

        statement.statementLink = null;
        resetNode(statement);
    }

    private void resetBLangExpression(BLangExpression expression) {

        expression.typeChecked = false;
        expression.expectedType = null;
        expression.impConversionExpr = null;
        expression.narrowedTypeInfo = null;
        resetNode(expression);
    }

    private void resetBLangStructureTypeNode(BLangStructureTypeNode node) {

        clearNodeList(node.fields);
        clearNode(node.initFunction);
        clearNodeList(node.typeRefs);
        node.symbol = null;
        node.referencedFields = new ArrayList<>();
        resetNode(node);
    }

    private void resetBLangMatchBindingPatternClause(BLangMatch.BLangMatchBindingPatternClause node) {

        clearNode(node.body);
        clearNode(node.matchExpr);
        resetNode(node);
    }

    /* Visitor Methods */

    @Override
    public void visit(BLangPackage pkgNode) {
        // Ignore.
    }

    @Override
    public void visit(BLangTestablePackage testablePkgNode) {
        // Ignore.
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {

        for (TopLevelNode node : compUnit.topLevelNodes) {
            clearNode((BLangNode) node);
        }
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {

        importPkgNode.symbol = null;
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {

        xmlnsNode.symbol = null;
        clearNode(xmlnsNode.namespaceURI);
    }

    @Override
    public void visit(BLangFunction funcNode) {

        funcNode.receiver = null;
        funcNode.paramClosureMap = new TreeMap<>();
        funcNode.mapSymbol = null;
        funcNode.initFunctionStmts = new LinkedHashMap<>();
        funcNode.closureVarSymbols = new LinkedHashSet<>();
        resetBLangInvokableNode(funcNode);
    }

    @Override
    public void visit(BLangService serviceNode) {

        serviceNode.symbol = null;
        serviceNode.listenerType = null;
        serviceNode.resourceFunctions = new ArrayList<>();
        clearNodeList(serviceNode.annAttachments);
        clearNode(serviceNode.markdownDocumentationAttachment);
        clearNode(serviceNode.serviceTypeDefinition);
        clearNodeList(serviceNode.attachedExprs);
        clearNode(serviceNode.variableNode);
    }

    @Override
    public void visit(BLangResource resourceNode) {
        // Ignore
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {

        typeDefinition.precedence = 0;
        typeDefinition.symbol = null;
        clearNode(typeDefinition.typeNode);
        clearNodeList(typeDefinition.annAttachments);
        clearNode(typeDefinition.markdownDocumentationAttachment);
    }

    @Override
    public void visit(BLangConstant constant) {

        constant.symbol = null;
        clearNode(constant.associatedTypeDefinition);
        resetBLangVariable(constant);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {

        resetBLangVariable(varNode);
    }

    @Override
    public void visit(BLangWorker workerNode) {

        resetBLangInvokableNode(workerNode);
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {

        // Ignore.
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {

        // Ignore.
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {

        annotationNode.symbol = null;
        clearNodeList(annotationNode.annAttachments);
        clearNode(annotationNode.markdownDocumentationAttachment);
        clearNode(annotationNode.typeNode);
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {

        annAttachmentNode.annotationSymbol = null;
        clearNode(annAttachmentNode.expr);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {

        blockNode.mapSymbol = null;
        blockNode.scope = null;
        resetBLangStatement(blockNode);
        clearNodeList(blockNode.stmts);
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {

        // Ignore.
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {

        // Ignore.
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {

        clearNode(varDefNode.var);
        resetBLangStatement(varDefNode);
    }

    @Override
    public void visit(BLangAssignment assignNode) {

        clearNode(assignNode.varRef);
        clearNode(assignNode.expr);
        resetBLangStatement(assignNode);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {

        clearNode(compoundAssignNode.varRef);
        clearNode(compoundAssignNode.expr);
        clearNode(compoundAssignNode.modifiedExpr);
        resetBLangStatement(compoundAssignNode);
    }

    @Override
    public void visit(BLangAbort abortNode) {

        resetBLangStatement(abortNode);
    }

    @Override
    public void visit(BLangRetry retryNode) {

        resetBLangStatement(retryNode);
    }

    @Override
    public void visit(BLangContinue continueNode) {

        resetBLangStatement(continueNode);
    }

    @Override
    public void visit(BLangBreak breakNode) {

        resetBLangStatement(breakNode);
    }

    @Override
    public void visit(BLangReturn returnNode) {

        clearNode(returnNode.expr);
        resetBLangStatement(returnNode);
    }

    @Override
    public void visit(BLangPanic panicNode) {

        clearNode(panicNode.expr);
        resetBLangStatement(panicNode);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {

        clearNode(xmlnsStmtNode.xmlnsDecl);
        resetBLangStatement(xmlnsStmtNode);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {

        clearNode(exprStmtNode.expr);
        resetBLangStatement(exprStmtNode);
    }

    @Override
    public void visit(BLangIf ifNode) {

        clearNode(ifNode.expr);
        clearNode(ifNode.body);
        clearNode(ifNode.elseStmt);
        resetBLangStatement(ifNode);
    }

    @Override
    public void visit(BLangMatch matchNode) {

        matchNode.exprTypes = new ArrayList<>();
        clearNode(matchNode.expr);
        clearNodeList(matchNode.patternClauses);
        resetBLangStatement(matchNode);
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {

        clearNode(patternClauseNode.body);
        clearNode(patternClauseNode.matchExpr);
        patternClauseNode.isLastPattern = false;
        clearNode(patternClauseNode.variable);
    }

    @Override
    public void visit(BLangForeach foreach) {

        clearNode(foreach.collection);
        clearNode(foreach.body);
        clearNode((BLangNode) foreach.variableDefinitionNode);
        foreach.varType = null;
        foreach.resultType = null;
        foreach.nillableResultType = null;
        resetBLangStatement(foreach);
    }

    @Override
    public void visit(BLangWhile whileNode) {

        clearNode(whileNode.expr);
        clearNode(whileNode.body);
        resetBLangStatement(whileNode);
    }

    @Override
    public void visit(BLangLock lockNode) {

        clearNode(lockNode.body);
        lockNode.lockVariables = new HashSet<>();
        lockNode.fieldVariables = new LinkedHashMap<>();
        resetBLangStatement(lockNode);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {

        clearNode(transactionNode.transactionBody);
        clearNode(transactionNode.onRetryBody);
        clearNode(transactionNode.committedBody);
        clearNode(transactionNode.abortedBody);
        clearNode(transactionNode.retryCount);
        resetBLangStatement(transactionNode);
    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {
        // Ignore.
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {

        clearNode(stmt.varRef);
        clearNode(stmt.expr);
        resetBLangStatement(stmt);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {

        clearNode(stmt.varRef);
        clearNode(stmt.expr);
        resetBLangStatement(stmt);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {

        clearNode(stmt.varRef);
        clearNode(stmt.expr);
        resetBLangStatement(stmt);
    }

    @Override
    public void visit(BLangCatch catchNode) {
        // Ignore
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {

        clearNodeList(forkJoin.workers);
        resetBLangStatement(forkJoin);
    }

    @Override
    public void visit(BLangOrderBy orderBy) {

        clearNodeList(orderBy.varRefs);
    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {

        clearNode((BLangNode) orderByVariable.varRef);
    }

    @Override
    public void visit(BLangLimit limit) {
        // Nothing.
    }

    @Override
    public void visit(BLangGroupBy groupBy) {

        clearNodeList(groupBy.varRefs);
    }

    @Override
    public void visit(BLangHaving having) {

        clearNode(having.expression);
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {

        clearNode(selectExpression.expression);
    }

    @Override
    public void visit(BLangSelectClause selectClause) {

        for (SelectExpressionNode e : selectClause.selectExpressions) {
            clearNode((BLangSelectExpression) e);
        }
        clearNode(selectClause.groupBy);
        clearNode(selectClause.having);
    }

    @Override
    public void visit(BLangWhere whereClause) {

        clearNode(whereClause.expression);
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {

        clearNode(streamingInput.beforeStreamingCondition);
        clearNode(streamingInput.windowClause);
        clearNode(streamingInput.afterStreamingCondition);
        clearNode(streamingInput.streamReference);
        for (ExpressionNode e : streamingInput.preInvocations) {
            clearNode((BLangNode) e);
        }
        for (ExpressionNode e : streamingInput.postInvocations) {
            clearNode((BLangNode) e);
        }
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {

        clearNode(joinStreamingInput.streamingInput);
        clearNode(joinStreamingInput.onExpression);
    }

    @Override
    public void visit(BLangTableQuery tableQuery) {

        clearNode(tableQuery.streamingInput);
        clearNode(tableQuery.joinStreamingInput);
        clearNode(tableQuery.selectClauseNode);
        clearNode(tableQuery.orderByNode);
        clearNode(tableQuery.limitNode);
    }

    @Override
    public void visit(BLangStreamAction streamAction) {

        clearNode(streamAction.lambdaFunction);
    }

    @Override
    public void visit(BLangFunctionClause functionClause) {

        clearNode(functionClause.functionInvocation);
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {

        clearNode(setAssignmentClause.variableReferenceNode);
        clearNode(setAssignmentClause.expressionNode);
    }

    @Override
    public void visit(BLangPatternStreamingEdgeInput patternStreamingEdgeInput) {

        clearNode(patternStreamingEdgeInput.streamRef);
        clearNode(patternStreamingEdgeInput.expressionNode);
        clearNode(patternStreamingEdgeInput.whereNode);
    }

    @Override
    public void visit(BLangWindow windowClause) {

        clearNode(windowClause.functionInvocation);
    }

    @Override
    public void visit(BLangPatternStreamingInput patternStreamingInput) {

        clearNode(patternStreamingInput.patternStreamingInput);
        for (PatternStreamingEdgeInputNode node : patternStreamingInput.patternStreamingEdgeInputNodeList) {
            clearNode((BLangPatternStreamingEdgeInput) node);
        }
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {

        clearNode(workerSendNode.expr);
        clearNode(workerSendNode.keyExpr);
        workerSendNode.env = null;
        resetBLangStatement(workerSendNode);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {

        clearNode(workerReceiveNode.keyExpr);
        clearNode(workerReceiveNode.sendExpression);
        workerReceiveNode.env = null;
        workerReceiveNode.workerIdentifier = null;
        workerReceiveNode.matchingSendsError = null;
        resetBLangExpression(workerReceiveNode);
    }

    @Override
    public void visit(BLangForever foreverStatement) {

        for (StreamingQueryStatementNode node : foreverStatement.streamingQueryStatementNodeList) {
            clearNode((BLangStreamingQueryStatement) node);
        }
        clearNodeList(foreverStatement.params);
        resetBLangStatement(foreverStatement);
    }

    @Override
    public void visit(BLangLiteral literalExpr) {

        // literalExpr is not handled properly in the type checker. Hence, need to preserver original type.
        BType originalType = literalExpr.type;
        literalExpr.isJSONContext = false;
        literalExpr.isFiniteContext = false;
        resetBLangExpression(literalExpr);
        literalExpr.type = originalType;
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {

        visit((BLangLiteral) literalExpr);
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {

        clearNodeList(tableLiteral.tableDataRows);
        clearNode(tableLiteral.indexColumnsArrayLiteral);
        clearNode(tableLiteral.keyColumnsArrayLiteral);
        resetBLangExpression(tableLiteral);
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {

        clearNodeList(recordLiteral.keyValuePairs);
        resetBLangExpression(recordLiteral);
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {

        varRefExpr.symbol = null;
        clearNodeList(varRefExpr.expressions);
        clearNode((BLangNode) varRefExpr.restParam);
        resetBLangVariableReference(varRefExpr);
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {

        varRefExpr.varSymbol = null;
        for (BLangRecordVarRef.BLangRecordVarRefKeyValue field : varRefExpr.recordRefFields) {
            clearNode(field.variableReference);
        }
        clearNode((BLangNode) varRefExpr.restParam);
        resetBLangVariableReference(varRefExpr);
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {

        varRefExpr.varSymbol = null;
        clearNode(varRefExpr.reason);
        clearNodeList(varRefExpr.detail);
        clearNode(varRefExpr.restVar);
        clearNode(varRefExpr.typeNode);
        resetBLangVariableReference(varRefExpr);
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {

        varRefExpr.varSymbol = null;
        resetBLangVariableReference(varRefExpr);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {

        fieldAccessExpr.varSymbol = null;
        resetBLangAccessExpression(fieldAccessExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {

        clearNode(indexAccessExpr.indexExpr);
        resetBLangAccessExpression(indexAccessExpr);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {

        clearNodeList(invocationExpr.argExprs);
        invocationExpr.exprSymbol = null;
        clearNodeList(invocationExpr.annAttachments);
        invocationExpr.requiredArgs = new ArrayList<>();
        invocationExpr.restArgs = new ArrayList<>();
        resetBLangAccessExpression(invocationExpr);
    }

    @Override
    public void visit(BLangTypeInit typeInit) {

        clearNode(typeInit.userDefinedType);
        clearNodeList(typeInit.argsExpr);
        clearNode(typeInit.initInvocation);
        resetBLangExpression(typeInit);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {

        // Ignore
    }

    @Override
    public void visit(BLangInvocation.BLangBuiltInMethodInvocation builtInMethodInvocation) {

        // Ignore
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {

        clearNode(ternaryExpr.expr);
        clearNode(ternaryExpr.thenExpr);
        clearNode(ternaryExpr.elseExpr);
        resetBLangExpression(ternaryExpr);
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {

        clearNodeList(awaitExpr.exprList);
        resetBLangExpression(awaitExpr);
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {

        clearNode(trapExpr.expr);
        resetBLangExpression(trapExpr);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {

        clearNode(binaryExpr.lhsExpr);
        clearNode(binaryExpr.rhsExpr);
        binaryExpr.opSymbol = null;
        resetBLangExpression(binaryExpr);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {

        clearNode(elvisExpr.lhsExpr);
        clearNode(elvisExpr.rhsExpr);
        resetBLangExpression(elvisExpr);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {

        clearNode(groupExpr.expression);
        groupExpr.typedescType = null;
        groupExpr.isTypedescExpr = false;
        resetBLangExpression(groupExpr);
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {

        clearNodeList(listConstructorExpr.exprs);
        listConstructorExpr.typedescType = null;
        listConstructorExpr.isTypedescExpr = false;
        resetBLangExpression(listConstructorExpr);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {

        // Ignore
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {

        // Ignore
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {

        clearNode(unaryExpr.expr);
        resetBLangExpression(unaryExpr);
    }

    @Override
    public void visit(BLangTypedescExpr expr) {

        clearNode(expr.typeNode);
        expr.resolvedType = null;
        resetBLangExpression(expr);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {

        clearNode(conversionExpr.expr);
        clearNode(conversionExpr.typeNode);
        conversionExpr.targetType = null;
        conversionExpr.conversionSymbol = null;
        clearNodeList(conversionExpr.annAttachments);
        conversionExpr.checkTypes = true;
        resetBLangExpression(conversionExpr);
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {

        xmlQName.nsSymbol = null;
        resetBLangExpression(xmlQName);
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {

        clearNode(xmlAttribute.name);
        clearNode(xmlAttribute.value);
        xmlAttribute.isNamespaceDeclr = false;
        xmlAttribute.symbol = null;
        resetBLangExpression(xmlAttribute);
    }

    @Override
    public void visit(BLangXMLElementLiteral expr) {

        clearNode(expr.startTagName);
        clearNode(expr.endTagName);
        clearNodeList(expr.attributes);
        clearNodeList(expr.children);
        expr.namespacesInScope = new LinkedHashMap<>();
        expr.inlineNamespaces = new ArrayList<>();
        expr.defaultNsSymbol = null;
        expr.scope = null;
        expr.modifiedChildren = null;
        resetBLangExpression(expr);
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {

        clearNodeList(xmlTextLiteral.textFragments);
        clearNode(xmlTextLiteral.concatExpr);
        resetBLangExpression(xmlTextLiteral);
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {

        clearNodeList(xmlCommentLiteral.textFragments);
        clearNode(xmlCommentLiteral.concatExpr);
        resetBLangExpression(xmlCommentLiteral);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {

        clearNode(xmlProcInsLiteral.target);
        clearNodeList(xmlProcInsLiteral.dataFragments);
        clearNode(xmlProcInsLiteral.dataConcatExpr);
        resetBLangExpression(xmlProcInsLiteral);
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {

        clearNodeList(xmlQuotedString.textFragments);
        xmlQuotedString.concatExpr = null;
        resetBLangExpression(xmlQuotedString);
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {

        clearNodeList(stringTemplateLiteral.exprs);
        resetBLangExpression(stringTemplateLiteral);
    }

    @Override
    public void visit(BLangLambdaFunction lambdaFunction) {

        clearNode(lambdaFunction.function);
        lambdaFunction.cachedEnv = null;
        lambdaFunction.paramMapSymbolsOfEnclInvokable = new TreeMap<>();
        lambdaFunction.enclMapSymbols = new TreeMap<>();
        resetBLangExpression(lambdaFunction);
    }

    @Override
    public void visit(BLangArrowFunction arrowFunction) {

        clearNodeList(arrowFunction.params);
        clearNode(arrowFunction.expression);
        arrowFunction.type = null;
        arrowFunction.function = null;
        arrowFunction.closureVarSymbols = new LinkedHashSet<>();
        resetBLangExpression(arrowFunction);
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {

        xmlAttributeAccessExpr.namespaces = new LinkedHashMap<>();
        visit((BLangIndexBasedAccess) xmlAttributeAccessExpr);
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {

        clearNode(intRangeExpression.startExpr);
        clearNode(intRangeExpression.endExpr);
        resetBLangExpression(intRangeExpression);
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {

        clearNode((BLangTableQuery) tableQueryExpression.tableQuery);
        resetBLangExpression(tableQueryExpression);
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {

        clearNode(bLangVarArgsExpression.expr);
        resetBLangExpression(bLangVarArgsExpression);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {

        clearNode(bLangNamedArgsExpression.expr);
        resetBLangExpression(bLangNamedArgsExpression);
    }

    @Override
    public void visit(BLangStreamingQueryStatement stmt) {

        clearNode((BLangStreamingInput) stmt.streamingInput);
        clearNode((BLangJoinStreamingInput) stmt.joinStreamingInput);
        clearNode((BLangPatternClause) stmt.patternClause);
        clearNode((BLangSelectClause) stmt.selectClauseNode);
        clearNode((BLangOrderBy) stmt.orderByNode);
        clearNode((BLangStreamAction) stmt.streamActionNode);
        clearNode((BLangOutputRateLimit) stmt.outputRateLimitNode);
        stmt.cachedEnv = null;
        resetBLangStatement(stmt);
    }

    @Override
    public void visit(BLangWithinClause withinClause) {

        // Ignore.
    }

    @Override
    public void visit(BLangOutputRateLimit outputRateLimit) {

        // Ignore.
    }

    @Override
    public void visit(BLangPatternClause patternClause) {

        clearNode((BLangPatternStreamingInput) patternClause.patternStreamingInput);
        clearNode((BLangWithinClause) patternClause.withinClause);
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {

        clearNode(assignableExpr.lhsExpr);
        assignableExpr.targetType = null;
        clearNode(assignableExpr.typeNode);
        assignableExpr.opSymbol = null;
        resetBLangExpression(assignableExpr);
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {

        // Ignore
    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {

        // Ignore
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {

        clearNode(checkedExpr.expr);
        checkedExpr.equivalentErrorTypeList = null;
        resetBLangExpression(checkedExpr);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {

        visit((BLangCheckedExpr) checkPanickedExpr);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {

        clearNode(serviceConstructorExpr.serviceNode);
        resetBLangExpression(serviceConstructorExpr);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {

        clearNode(typeTestExpr.expr);
        clearNode(typeTestExpr.typeNode);
        resetBLangExpression(typeTestExpr);
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {

        clearNode(typeTestExpr.expr);
        clearNode(typeTestExpr.typeNode);
        resetBLangExpression(typeTestExpr);
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {

        resetBLangExpression(ignoreExpr);
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {

        annotAccessExpr.annotationSymbol = null;
        resetBLangAccessExpression(annotAccessExpr);
    }

    @Override
    public void visit(BLangValueType valueType) {

        // Ignore
    }

    @Override
    public void visit(BLangArrayType arrayType) {

        clearNode(arrayType.elemtype);
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {

        // Ignore
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {

        clearNode(constrainedType.type);
        clearNode(constrainedType.constraint);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {

        // Ignore
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {

        clearNodeList(functionTypeNode.params);
        clearNode(functionTypeNode.returnTypeNode);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {

        clearNodeList(unionTypeNode.memberTypeNodes);
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {

        clearNodeList(objectTypeNode.functions);
        clearNode(objectTypeNode.initFunction);
        clearNode(objectTypeNode.receiver);
        resetBLangStructureTypeNode(objectTypeNode);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {

        clearNode(recordTypeNode.restFieldType);
        resetBLangStructureTypeNode(recordTypeNode);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {

        clearNodeList(finiteTypeNode.valueSpace);
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {

        clearNodeList(tupleTypeNode.memberTypeNodes);
        clearNode(tupleTypeNode.restParamType);
    }

    @Override
    public void visit(BLangErrorType errorType) {

        clearNode(errorType.reasonType);
        clearNode(errorType.detailType);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {

        // Ignore
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {

        // Ignore
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {

        // Ignore
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangConstRef constRef) {

        // Ignore
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {

        // Ignore
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {

        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {

        // Ignore
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {

        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {

        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {

        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr arrayIndexAccessExpr) {

        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlAccessExpr) {

        // Ignore
    }

    @Override
    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {

        // Ignore
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {

        // Ignore
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {

        // Ignore
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStreamLiteral streamLiteral) {

        // Ignore
    }

    @Override
    public void visit(BLangRecordLiteral.BLangChannelLiteral channelLiteral) {

        // Ignore
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {

        // Ignore
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {

        // Ignore
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {

        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {

        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {

        // Ignore
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
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {

        clearNodeList(bLangXMLSequenceLiteral.xmlItems);
        resetBLangExpression(bLangXMLSequenceLiteral);
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {

        clearNode(bLangStatementExpression.expr);
        clearNode(bLangStatementExpression.stmt);
        resetBLangExpression(bLangStatementExpression);
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine) {

        resetBLangExpression(bLangMarkdownDocumentationLine);
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter) {

        bLangDocumentationParameter.symbol = null;
        resetBLangExpression(bLangDocumentationParameter);
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation) {

        bLangMarkdownReturnParameterDocumentation.type = null;
        resetBLangExpression(bLangMarkdownReturnParameterDocumentation);
    }

    @Override
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {

        clearNodeList(bLangMarkdownDocumentation.documentationLines);
        clearNodeList(bLangMarkdownDocumentation.parameters);
        clearNodeList(bLangMarkdownDocumentation.references);
        clearNode(bLangMarkdownDocumentation.returnParameter);
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {

        clearNodeList(bLangTupleVariable.memberVariables);
        clearNode(bLangTupleVariable.restVariable);
        resetBLangVariable(bLangTupleVariable);
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {

        clearNode(bLangTupleVariableDef.var);
        resetBLangStatement(bLangTupleVariableDef);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {

        for (BLangRecordVariable.BLangRecordVariableKeyValue keyValue : bLangRecordVariable.variableList) {
            clearNode(keyValue.valueBindingPattern);
        }
        clearNode((BLangVariable) bLangRecordVariable.restParam);
        resetBLangVariable(bLangRecordVariable);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {

        clearNode(bLangRecordVariableDef.var);
        resetBLangStatement(bLangRecordVariableDef);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {

        clearNode(bLangErrorVariable.reason);
        for (BLangErrorVariable.BLangErrorDetailEntry entry : bLangErrorVariable.detail) {
            clearNode(entry.valueBindingPattern);
        }
        clearNode(bLangErrorVariable.restDetail);
        bLangErrorVariable.detailExpr = null;
        resetBLangVariable(bLangErrorVariable);
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {

        clearNode(bLangErrorVariableDef.errorVariable);
        resetBLangStatement(bLangErrorVariableDef);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause node) {

        clearNode(node.literal);
        resetBLangMatchBindingPatternClause(node);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause node) {

        clearNode(node.bindingPatternVariable);
        clearNode(node.typeGuardExpr);
        resetBLangMatchBindingPatternClause(node);
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {

        workerFlushExpr.cachedWorkerSendStmts = new ArrayList<>();
        resetBLangExpression(workerFlushExpr);
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {

        clearNode(syncSendExpr.expr);
        syncSendExpr.receive = null;
        syncSendExpr.env = null;
        syncSendExpr.workerType = null;
        resetBLangExpression(syncSendExpr);
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {

        for (BLangWaitForAllExpr.BLangWaitKeyValue keyValue : waitForAllExpr.keyValuePairs) {
            clearNode(keyValue.keyExpr);
            clearNode(keyValue.valueExpr);
        }
        resetBLangExpression(waitForAllExpr);
    }

    @Override
    public void visit(BLangMarkdownReferenceDocumentation bLangMarkdownReferenceDocumentation) {

        // Ignore
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {

        // Ignore
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValue recordKeyValue) {

        clearNode(recordKeyValue.key.expr);
        recordKeyValue.key.fieldSymbol = null;
        clearNode(recordKeyValue.valueExpr);
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {

        clearNode(waitKeyValue.valueExpr);
        clearNode(waitKeyValue.keyExpr);
    }
}
