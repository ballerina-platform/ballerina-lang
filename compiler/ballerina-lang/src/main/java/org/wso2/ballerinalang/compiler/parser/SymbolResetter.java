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

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangArrayLiteral;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * This class is responsible for clearing all the symbols in a compilation unit. This is used to add incremental
 * compilation support.
 *
 * @since 0.995.0
 */
public class SymbolResetter extends BLangNodeVisitor {

    private static final CompilerContext.Key<SymbolResetter> SYMBOL_CLEANER_KEY = new CompilerContext.Key<>();

    public static SymbolResetter getInstance(CompilerContext context) {
        SymbolResetter symbolResetter = context.get(SYMBOL_CLEANER_KEY);
        if (symbolResetter == null) {
            symbolResetter = new SymbolResetter(context);
        }
        return symbolResetter;
    }

    public SymbolResetter(CompilerContext context) {
        context.put(SYMBOL_CLEANER_KEY, this);
    }

    void resetTopLevelNode(TopLevelNode node) {
        NodeKind kind = node.getKind();

        switch (kind) {
            case IMPORT:
                visit((BLangImportPackage) node);
                break;
            case FUNCTION:
                visit((BLangFunction) node);
                break;
            case TYPE_DEFINITION:
                visit((BLangTypeDefinition) node);
                break;
            case SERVICE:
                visit((BLangService) node);
                break;
            case VARIABLE:
                visit((BLangSimpleVariable) node);
                break;
            case ANNOTATION:
                visit((BLangAnnotation) node);
                break;
            case XMLNS:
                visit((BLangXMLNS) node);
                break;
            case CONSTANT:
                visit((BLangConstant) node);
                break;
            default:
                throw new RuntimeException("Unexpected top level node kind");
        }
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTestablePackage testablePkgNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        importPkgNode.reset();
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        xmlnsNode.reset();
        xmlnsNode.namespaceURI.accept(this);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        funcNode.reset();
        if (funcNode.receiver != null) {
            visit(funcNode.receiver);
        }

        funcNode.requiredParams.forEach(param -> param.accept(this));
        funcNode.returnTypeNode.accept(this);
        funcNode.returnTypeAnnAttachments.forEach(annotations -> annotations.accept(this));
        if (funcNode.body != null) {
            funcNode.body.accept(this);
        }
        funcNode.annAttachments.forEach(annotation -> annotation.accept(this));
        funcNode.endpoints.forEach(endpoint -> endpoint.accept(this));
        funcNode.workers.forEach(worker -> worker.accept(this));
        if (funcNode.restParam != null) {
            funcNode.restParam.accept(this);
        }
    }

    @Override
    public void visit(BLangService serviceNode) {
        serviceNode.reset();
        serviceNode.annAttachments.forEach(annotation -> annotation.accept(this));

        serviceNode.serviceTypeDefinition.accept(this);
        serviceNode.attachedExprs.forEach(expression -> expression.accept(this));
        if (serviceNode.variableNode != null) {
            // Todo - Enabling this will result in a stack overflow in artemis-anycast-session-consumer example.
            // serviceNode.variableNode.accept(this);
        }
        serviceNode.resourceFunctions.forEach(function -> function.accept(this));
    }

    @Override
    public void visit(BLangResource resourceNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        typeDefinition.reset();
        typeDefinition.typeNode.accept(this);
        typeDefinition.annAttachments.forEach(annotation -> annotation.accept(this));
    }

    @Override
    public void visit(BLangConstant constant) {
        constant.reset();
        if (constant.typeNode != null) {
            constant.typeNode.accept(this);
        }
        constant.annAttachments.forEach(annotation -> annotation.accept(this));
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        varNode.reset();
        if (varNode.typeNode != null) {
            varNode.typeNode.accept(this);
        }
        if (varNode.expr != null) {
            varNode.expr.accept(this);
        }
        varNode.annAttachments.forEach(annotation -> annotation.accept(this));
    }

    @Override
    public void visit(BLangWorker workerNode) {
        workerNode.reset();
        workerNode.requiredParams.forEach(param -> param.accept(this));
        workerNode.returnTypeNode.accept(this);
        workerNode.returnTypeAnnAttachments.forEach(annotations -> annotations.accept(this));
        workerNode.body.accept(this);
        workerNode.annAttachments.forEach(annotation -> annotation.accept(this));
        workerNode.endpoints.forEach(endpoint -> endpoint.accept(this));
        workerNode.workers.forEach(worker -> worker.accept(this));
        workerNode.restParam.accept(this);
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
        // Do nothing.
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        annotationNode.reset();
        annotationNode.annAttachments.forEach(attachment -> attachment.accept(this));
        annotationNode.typeNode.accept(this);
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        annAttachmentNode.reset();
        if (annAttachmentNode.expr != null) {
            annAttachmentNode.expr.accept(this);
        }
    }

    // Statements.

    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.reset();
        blockNode.stmts.forEach(statement -> statement.accept(this));
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        varDefNode.reset();
        varDefNode.var.accept(this);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        assignNode.reset();
        assignNode.varRef.accept(this);
        assignNode.expr.accept(this);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        compoundAssignNode.reset();
        compoundAssignNode.varRef.accept(this);
        compoundAssignNode.expr.accept(this);
        compoundAssignNode.modifiedExpr = null;
    }

    @Override
    public void visit(BLangAbort abortNode) {
        // Do nothing
    }

    @Override
    public void visit(BLangRetry retryNode) {
        // Do nothing
    }

    @Override
    public void visit(BLangContinue continueNode) {
        // Do nothing
    }

    @Override
    public void visit(BLangBreak breakNode) {
        // Do nothing
    }

    @Override
    public void visit(BLangReturn returnNode) {
        returnNode.reset();
        returnNode.expr.accept(this);
    }

    @Override
    public void visit(BLangThrow throwNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangPanic panicNode) {
        panicNode.reset();
        panicNode.expr.accept(this);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.reset();
        xmlnsStmtNode.xmlnsDecl.accept(this);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.reset();
        exprStmtNode.expr.accept(this);
    }

    @Override
    public void visit(BLangIf ifNode) {
        ifNode.reset();
        ifNode.expr.accept(this);
        ifNode.body.accept(this);
        if (ifNode.elseStmt != null) {
            ifNode.elseStmt.accept(this);
        }
    }

    @Override
    public void visit(BLangMatch matchNode) {
        matchNode.reset();
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        throwInvalidStateException(BLangMatch.BLangMatchTypedBindingPatternClause.class);
    }

    @Override
    public void visit(BLangForeach foreach) {
        foreach.reset();
        foreach.collection.accept(this);
        foreach.body.accept(this);
        ((BLangNode) foreach.variableDefinitionNode).accept(this);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        whileNode.reset();
        whileNode.expr.accept(this);
        whileNode.body.accept(this);
    }

    @Override
    public void visit(BLangLock lockNode) {
        lockNode.reset();
        lockNode.body.accept(this);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        transactionNode.reset();
        transactionNode.transactionBody.accept(this);
        if (transactionNode.onRetryBody != null) {
            transactionNode.onRetryBody.accept(this);
        }
        if (transactionNode.committedBody != null) {
            transactionNode.committedBody.accept(this);
        }
        if (transactionNode.abortedBody != null) {
            transactionNode.abortedBody.accept(this);
        }
        if (transactionNode.retryCount != null) {
            transactionNode.retryCount.accept(this);
        }
    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        stmt.reset();
        stmt.varRef.accept(this);
        stmt.expr.accept(this);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        stmt.reset();
        stmt.varRef.accept(this);
        stmt.expr.accept(this);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        stmt.reset();
        stmt.varRef.accept(this);
        stmt.expr.accept(this);
    }

    @Override
    public void visit(BLangCatch catchNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        forkJoin.reset();
        forkJoin.workers.forEach(variableDef -> variableDef.accept(this));
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        orderBy.reset();
        orderBy.getVariables().forEach(orderByVariableNode ->
                ((BLangOrderByVariable) orderByVariableNode).accept(this));
    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {
        orderByVariable.reset();
        ((BLangExpression) orderByVariable.getVariableReference()).accept(this);
    }

    @Override
    public void visit(BLangLimit limit) {
        limit.reset();
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        groupBy.reset();
        groupBy.getVariables().forEach(expressionNode -> ((BLangExpression) expressionNode).accept(this));
    }

    @Override
    public void visit(BLangHaving having) {
        having.reset();
        ((BLangExpression) having.getExpression()).accept(this);
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
        selectExpression.reset();
        ((BLangExpression) selectExpression.getExpression()).accept(this);
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        selectClause.reset();
        if (selectClause.getSelectExpressions() != null) {
            selectClause.getSelectExpressions().forEach(selectExpressionNode ->
                    ((BLangSelectExpression) selectExpressionNode).accept(this));
        }
        if (selectClause.getGroupBy() != null) {
            ((BLangGroupBy) selectClause.getGroupBy()).accept(this);
        }
        if (selectClause.getHaving() != null) {
            ((BLangHaving) selectClause.getHaving()).accept(this);
        }
    }

    @Override
    public void visit(BLangWhere whereClause) {
        whereClause.reset();
        ((BLangExpression) whereClause.getExpression()).accept(this);
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        streamingInput.reset();
        if (streamingInput.getBeforeStreamingCondition() != null) {
            ((BLangWhere) streamingInput.getBeforeStreamingCondition()).accept(this);
        }
        if (streamingInput.getWindowClause() != null) {
            ((BLangWindow) streamingInput.getWindowClause()).accept(this);
        }
        if (streamingInput.getAfterStreamingCondition() != null) {
            ((BLangWhere) streamingInput.getAfterStreamingCondition()).accept(this);
        }
        if (streamingInput.getStreamReference() != null) {
            ((BLangExpression) streamingInput.getStreamReference()).accept(this);
        }
        if (streamingInput.getPreFunctionInvocations() != null) {
            ((BLangExpression) streamingInput.getPreFunctionInvocations()).accept(this);
        }
        if (streamingInput.getPostFunctionInvocations() != null) {
            ((BLangExpression) streamingInput.getPostFunctionInvocations()).accept(this);
        }
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        joinStreamingInput.reset();
        if (joinStreamingInput.getStreamingInput() != null) {
            ((BLangStreamingInput) joinStreamingInput.getStreamingInput()).accept(this);
        }
        if (joinStreamingInput.getOnExpression() != null) {
            ((BLangExpression) joinStreamingInput.getOnExpression()).accept(this);
        }
    }

    @Override
    public void visit(BLangTableQuery tableQuery) {
        tableQuery.reset();
        if (tableQuery.getStreamingInput() != null) {
            ((BLangStreamingInput) tableQuery.getStreamingInput()).accept(this);
        }
        if (tableQuery.getJoinStreamingInput() != null) {
            ((BLangJoinStreamingInput) tableQuery.getJoinStreamingInput()).accept(this);
        }
        if (tableQuery.getSelectClauseNode() != null) {
            ((BLangSelectClause) tableQuery.getSelectClauseNode()).accept(this);
        }
        if (tableQuery.getOrderByNode() != null) {
            ((BLangOrderBy) tableQuery.getOrderByNode()).accept(this);
        }
        if (tableQuery.getLimitClause() != null) {
            ((BLangLimit) tableQuery.getLimitClause()).accept(this);
        }
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        streamAction.reset();
        if (streamAction.getInvokableBody() != null) {
            ((BLangLambdaFunction) streamAction.getInvokableBody()).accept(this);
        }
    }

    @Override
    public void visit(BLangFunctionClause functionClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangPatternStreamingEdgeInput patternStreamingEdgeInput) {
        patternStreamingEdgeInput.reset();
    }

    @Override
    public void visit(BLangWindow windowClause) {
        windowClause.reset();
        if (windowClause.getFunctionInvocation() != null) {
            ((BLangExpression) windowClause.getFunctionInvocation()).accept(this);
        }
    }

    @Override
    public void visit(BLangPatternStreamingInput patternStreamingInput) {
        patternStreamingInput.reset();
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.reset();

        workerSendNode.expr.accept(this);
        if (workerSendNode.keyExpr != null) {
            workerSendNode.keyExpr.accept(this);
        }
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        workerReceiveNode.reset();
        if (workerReceiveNode.keyExpr != null) {
            workerReceiveNode.keyExpr.accept(this);
        }
        if (workerReceiveNode.sendExpression != null) {
            workerReceiveNode.sendExpression.accept(this);
        }
    }

    @Override
    public void visit(BLangForever foreverStatement) {
        foreverStatement.reset();

        foreverStatement.getStreamingQueryStatements().forEach(streamingQueryStatementNode ->
                ((BLangStreamingQueryStatement) streamingQueryStatementNode).accept(this));
        if (foreverStatement.params != null) {
            foreverStatement.params.forEach(variable -> variable.accept(this));
        }
    }


    // Expressions
    @Override
    public void visit(BLangLiteral literalExpr) {
        literalExpr.reset();
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
        literalExpr.reset();
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        tableLiteral.reset();
        tableLiteral.columns.forEach(bLangTableColumn -> bLangTableColumn.accept(this));
        tableLiteral.tableDataRows.forEach(bLangExpression -> bLangExpression.accept(this));
    }

    @Override
    public void visit(BLangArrayLiteral arrayLiteral) {
        arrayLiteral.reset();
        arrayLiteral.exprs.forEach(expression -> expression.accept(this));
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        recordLiteral.reset();
        recordLiteral.keyValuePairs.forEach(keyValue -> {
            keyValue.key.accept(this);
            keyValue.valueExpr.accept(this);
        });
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        varRefExpr.reset();
        varRefExpr.expressions.forEach(bLangExpression -> bLangExpression.accept(this));
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        varRefExpr.reset();

        varRefExpr.recordRefFields.forEach(bLangRecordVarRefKeyValue -> {
            bLangRecordVarRefKeyValue.variableReference.accept(this);
            bLangRecordVarRefKeyValue.variableName.accept(this);
        });

        if (varRefExpr.restParam != null) {
            varRefExpr.restParam.accept(this);
        }
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        varRefExpr.reset();
        varRefExpr.detail.forEach(detailKeyVal -> detailKeyVal.accept(this));
        varRefExpr.reason.accept(this);
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        varRefExpr.reset();
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        fieldAccessExpr.reset();
        fieldAccessExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        indexAccessExpr.reset();
        indexAccessExpr.expr.accept(this);
        indexAccessExpr.indexExpr.accept(this);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        invocationExpr.reset();

        if (invocationExpr.expr != null) {
            invocationExpr.expr.accept(this);
        }

        invocationExpr.argExprs.forEach(arg -> arg.accept(this));
        invocationExpr.requiredArgs.forEach(arg -> arg.accept(this));
        invocationExpr.restArgs.forEach(arg -> arg.accept(this));
    }

    @Override
    public void visit(BLangTypeInit typeInitExpr) {
        typeInitExpr.reset();

        if (typeInitExpr.userDefinedType != null) {
            typeInitExpr.userDefinedType.accept(this);
        }
        typeInitExpr.argsExpr.forEach(arg -> arg.accept(this));
        typeInitExpr.initInvocation.accept(this);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        actionInvocationExpr.reset();
    }

    @Override
    public void visit(BLangInvocation.BLangBuiltInMethodInvocation builtInMethodInvocation) {
        builtInMethodInvocation.reset();
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.reset();
        ternaryExpr.expr.accept(this);
        ternaryExpr.thenExpr.accept(this);
        ternaryExpr.elseExpr.accept(this);
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {
        awaitExpr.reset();
        awaitExpr.exprList.forEach(bLangExpression -> bLangExpression.accept(this));
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        trapExpr.reset();
        trapExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.reset();
        binaryExpr.rhsExpr.accept(this);
        binaryExpr.rhsExpr.accept(this);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        elvisExpr.reset();
        elvisExpr.lhsExpr.accept(this);
        elvisExpr.rhsExpr.accept(this);
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        listConstructorExpr.reset();
        listConstructorExpr.exprs.forEach(expression -> expression.accept(this));
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        unaryExpr.reset();
        unaryExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        accessExpr.reset();
        accessExpr.typeNode.accept(this);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        conversionExpr.reset();
        conversionExpr.expr.accept(this);
        conversionExpr.typeNode.accept(this);
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        xmlQName.reset();
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        xmlAttribute.reset();
        xmlAttribute.name.accept(this);
        xmlAttribute.value.accept(this);
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        xmlElementLiteral.reset();
        xmlElementLiteral.startTagName.accept(this);
        if (xmlElementLiteral.endTagName != null) {
            xmlElementLiteral.endTagName.accept(this);
        }
        xmlElementLiteral.attributes.forEach(bLangXMLAttribute -> bLangXMLAttribute.accept(this));
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        xmlTextLiteral.reset();
        xmlTextLiteral.textFragments.forEach(bLangExpression -> bLangExpression.accept(this));
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        xmlCommentLiteral.reset();
        xmlCommentLiteral.textFragments.forEach(bLangExpression -> bLangExpression.accept(this));
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.reset();
        xmlProcInsLiteral.dataFragments.forEach(bLangExpression -> bLangExpression.accept(this));
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        xmlQuotedString.reset();
        xmlQuotedString.textFragments.forEach(bLangExpression -> bLangExpression.accept(this));
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.reset();
        stringTemplateLiteral.exprs.forEach(expression -> expression.accept(this));
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        bLangLambdaFunction.reset();
        bLangLambdaFunction.function.accept(this);
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        bLangArrowFunction.reset();
        bLangArrowFunction.params.forEach(variable -> variable.accept(this));

        bLangArrowFunction.expression.accept(this);
        if (bLangArrowFunction.function != null) {
            bLangArrowFunction.function.accept(this);
        }
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        xmlAttributeAccessExpr.reset();
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        intRangeExpression.reset();
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {
        tableQueryExpression.reset();
        ((BLangTableQuery) tableQueryExpression.getTableQuery()).accept(this);
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        bLangVarArgsExpression.reset();
        bLangVarArgsExpression.expr.accept(this);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        bLangNamedArgsExpression.reset();
        bLangNamedArgsExpression.expr.accept(this);
    }

    @Override
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        streamingQueryStatement.reset();
        if (streamingQueryStatement.getStreamingInput() != null) {
            ((BLangStreamingInput) streamingQueryStatement.getStreamingInput()).accept(this);
        }
        if (streamingQueryStatement.getJoiningInput() != null) {
            ((BLangJoinStreamingInput) streamingQueryStatement.getJoiningInput()).accept(this);
        }
        if (streamingQueryStatement.getPatternClause() != null) {
            ((BLangPatternClause) streamingQueryStatement.getPatternClause()).accept(this);
        }
        if (streamingQueryStatement.getSelectClause() != null) {
            ((BLangSelectClause) streamingQueryStatement.getSelectClause()).accept(this);
        }
        if (streamingQueryStatement.getOrderbyClause() != null) {
            ((BLangOrderBy) streamingQueryStatement.getOrderbyClause()).accept(this);
        }
        if (streamingQueryStatement.getStreamingAction() != null) {
            ((BLangStreamAction) streamingQueryStatement.getStreamingAction()).accept(this);
        }
        if (streamingQueryStatement.getOutputRateLimitNode() != null) {
            ((BLangOutputRateLimit) streamingQueryStatement.getOutputRateLimitNode()).accept(this);
        }
    }

    @Override
    public void visit(BLangWithinClause withinClause) {
        withinClause.reset();
    }

    @Override
    public void visit(BLangOutputRateLimit outputRateLimit) {
        outputRateLimit.reset();
    }

    @Override
    public void visit(BLangPatternClause patternClause) {
        patternClause.reset();
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        assignableExpr.reset();
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        throw new AssertionError(bLangMatchExpression.toString());
    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {
        throw new AssertionError(bLangMatchExprPatternClause.toString());
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        checkedExpr.reset();
        checkedExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        checkPanickedExpr.reset();
        checkPanickedExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        serviceConstructorExpr.reset();
        serviceConstructorExpr.serviceNode.accept(this);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        typeTestExpr.reset();
        typeTestExpr.expr.accept(this);
        typeTestExpr.typeNode.accept(this);
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        throwInvalidStateException(BLangIsLikeExpr.class);
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        annotAccessExpr.reset();
    }

    // Type nodes
    @Override
    public void visit(BLangValueType valueType) {
        valueType.reset();
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        arrayType.reset();
        arrayType.elemtype.accept(this);
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        // Do nothing.
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        constrainedType.reset();
        constrainedType.type.accept(this);
        constrainedType.constraint.accept(this);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        userDefinedType.reset();
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        functionTypeNode.reset();
        functionTypeNode.params.forEach(variable -> variable.accept(this));
        functionTypeNode.returnTypeNode.accept(this);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        unionTypeNode.reset();
        unionTypeNode.memberTypeNodes.forEach(bLangType -> bLangType.accept(this));
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        objectTypeNode.reset();

        objectTypeNode.fields.forEach(variable -> variable.accept(this));
        objectTypeNode.typeRefs.forEach(bLangType -> bLangType.accept(this));
        objectTypeNode.functions.forEach(bLangFunction -> bLangFunction.accept(this));
        if (objectTypeNode.initFunction != null) {
            objectTypeNode.initFunction.accept(this);
        }
        if (objectTypeNode.receiver != null) {
            objectTypeNode.receiver.accept(this);
        }
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        recordTypeNode.reset();

        recordTypeNode.fields.forEach(variable -> variable.accept(this));
        if (recordTypeNode.initFunction != null) {
            recordTypeNode.initFunction.accept(this);
        }
        recordTypeNode.typeRefs.forEach(bLangType -> bLangType.accept(this));
        if (recordTypeNode.restFieldType != null) {
            recordTypeNode.restFieldType.accept(this);
        }
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        finiteTypeNode.reset();
        finiteTypeNode.valueSpace.forEach(bLangExpression -> bLangExpression.accept(this));
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        tupleTypeNode.reset();
        tupleTypeNode.memberTypeNodes.forEach(bLangType -> bLangType.accept(this));
    }

    @Override
    public void visit(BLangErrorType errorType) {
        errorType.reset();
        errorType.reasonType.accept(this);
        errorType.detailType.accept(this);
    }


    // expressions that will used only from the Desugar phase

    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        throwInvalidStateException(BLangSimpleVarRef.BLangLocalVarRef.class);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        throwInvalidStateException(BLangSimpleVarRef.BLangFieldVarRef.class);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        throwInvalidStateException(BLangSimpleVarRef.BLangPackageVarRef.class);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangConstRef constRef) {
        throwInvalidStateException(BLangSimpleVarRef.BLangConstRef.class);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        throwInvalidStateException(BLangSimpleVarRef.BLangFunctionVarRef.class);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {
        throwInvalidStateException(BLangSimpleVarRef.BLangTypeLoad.class);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        throwInvalidStateException(BLangIndexBasedAccess.BLangStructFieldAccessExpr.class);
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
        throwInvalidStateException(BLangFieldBasedAccess.BLangStructFunctionVarRef.class);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        throwInvalidStateException(BLangIndexBasedAccess.BLangMapAccessExpr.class);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        throwInvalidStateException(BLangIndexBasedAccess.BLangArrayAccessExpr.class);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr arrayIndexAccessExpr) {
        throwInvalidStateException(BLangIndexBasedAccess.BLangTupleAccessExpr.class);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlIndexAccessExpr) {
        throwInvalidStateException(BLangIndexBasedAccess.BLangXMLAccessExpr.class);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {
        throwInvalidStateException(BLangRecordLiteral.BLangJSONLiteral.class);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        throwInvalidStateException(BLangRecordLiteral.BLangMapLiteral.class);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        throwInvalidStateException(BLangRecordLiteral.BLangStructLiteral.class);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStreamLiteral streamLiteral) {
        throwInvalidStateException(BLangRecordLiteral.BLangStreamLiteral.class);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangChannelLiteral channelLiteral) {
        throwInvalidStateException(BLangRecordLiteral.BLangChannelLiteral.class);
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        throwInvalidStateException(BLangInvocation.BFunctionPointerInvocation.class);
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        throwInvalidStateException(BLangInvocation.BLangAttachedFunctionInvocation.class);
    }

    @Override
    public void visit(BLangArrayLiteral.BLangJSONArrayLiteral jsonArrayLiteral) {
        throwInvalidStateException(BLangListConstructorExpr.BLangJSONArrayLiteral.class);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        throwInvalidStateException(BLangIndexBasedAccess.BLangJSONAccessExpr.class);
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        throwInvalidStateException(BLangXMLNS.BLangLocalXMLNS.class);
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        throwInvalidStateException(BLangXMLNS.BLangPackageXMLNS.class);
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        throwInvalidStateException(BLangStatementExpression.class);
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine) {
        bLangMarkdownDocumentationLine.reset();
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter) {
        bLangDocumentationParameter.reset();
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation) {
        bLangMarkdownReturnParameterDocumentation.reset();
    }

    @Override
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {
        bLangMarkdownDocumentation.reset();
        bLangMarkdownDocumentation.documentationLines.forEach(line -> line.accept(this));
        bLangMarkdownDocumentation.parameters.forEach(param -> param.accept(this));
        if (bLangMarkdownDocumentation.returnParameter != null) {
            bLangMarkdownDocumentation.returnParameter.accept(this);
        }
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        bLangTupleVariable.reset();
        bLangTupleVariable.memberVariables.forEach(var -> var.accept(this));
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        bLangTupleVariableDef.reset();
        bLangTupleVariableDef.var.accept(this);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        bLangRecordVariable.reset();

        bLangRecordVariable.variableList.forEach(variable -> variable.valueBindingPattern.accept(this));
        if (bLangRecordVariable.restParam != null) {
            ((BLangNode) bLangRecordVariable.restParam).accept(this);
        }
        if (bLangRecordVariable.typeNode != null) {
            bLangRecordVariable.typeNode.accept(this);
        }
        if (bLangRecordVariable.expr != null) {
            bLangRecordVariable.expr.accept(this);
        }
        bLangRecordVariable.annAttachments.forEach(annotation -> annotation.accept(this));
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        bLangRecordVariableDef.reset();
        bLangRecordVariableDef.var.accept(this);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        bLangErrorVariable.reset();

        bLangErrorVariable.reason.accept(this);
        bLangErrorVariable.detail.forEach(detailEntry -> detailEntry.valueBindingPattern.accept(this));

        if (bLangErrorVariable.restDetail != null) {
            bLangErrorVariable.restDetail.accept(this);
        }

        if (bLangErrorVariable.typeNode != null) {
            bLangErrorVariable.typeNode.accept(this);
        }
        bLangErrorVariable.expr.accept(this);
        bLangErrorVariable.annAttachments.forEach(annotation -> annotation.accept(this));
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        bLangErrorVariableDef.reset();
        bLangErrorVariableDef.errorVariable.accept(this);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause bLangMatchStmtStaticBindingPatternClause) {
        bLangMatchStmtStaticBindingPatternClause.reset();
        bLangMatchStmtStaticBindingPatternClause.body.accept(this);
        bLangMatchStmtStaticBindingPatternClause.literal.accept(this);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause matchStmtStructuredBindingPatternClause) {
        matchStmtStructuredBindingPatternClause.reset();
        matchStmtStructuredBindingPatternClause.bindingPatternVariable.accept(this);
        if (matchStmtStructuredBindingPatternClause.typeGuardExpr != null) {
            matchStmtStructuredBindingPatternClause.accept(this);
        }
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        workerFlushExpr.reset();
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        syncSendExpr.reset();
        syncSendExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        waitForAllExpr.reset();
        waitForAllExpr.keyValuePairs.forEach(bLangWaitKeyValue -> {
            if (bLangWaitKeyValue.keyExpr != null) {
                bLangWaitKeyValue.keyExpr.accept(this);
            }
            if (bLangWaitKeyValue.valueExpr != null) {
                bLangWaitKeyValue.valueExpr.accept(this);
            }
        });
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        waitLiteral.reset();
        waitLiteral.accept(this);
    }

    private void throwInvalidStateException(Class clazz) {
        throw new IllegalStateException(clazz.toString() + " is only used from Desugar onwards.");
    }
}
