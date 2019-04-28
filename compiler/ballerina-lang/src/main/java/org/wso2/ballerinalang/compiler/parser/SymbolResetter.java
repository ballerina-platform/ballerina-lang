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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
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
        importPkgNode.symbol = null;
        importPkgNode.type = null;
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        xmlnsNode.symbol = null;
        xmlnsNode.type = null;
        xmlnsNode.namespaceURI.accept(this);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        funcNode.symbol = null;
        funcNode.type = null;
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
        funcNode.defaultableParams.forEach(parameter -> parameter.accept(this));
        if (funcNode.restParam != null) {
            funcNode.restParam.accept(this);
        }
    }

    @Override
    public void visit(BLangService serviceNode) {
        serviceNode.symbol = null;
        serviceNode.type = null;
        serviceNode.annAttachments.forEach(annotation -> annotation.accept(this));

        serviceNode.serviceTypeDefinition.accept(this);
        serviceNode.attachedExprs.forEach(expression -> expression.accept(this));
        if (serviceNode.variableNode != null) {
            // Todo - Enabling this will result in a stack overflow in artemis-anycast-session-consumer example.
            // serviceNode.variableNode.accept(this);
        }
        serviceNode.listenerType = null;
        serviceNode.resourceFunctions.forEach(function -> function.accept(this));
    }

    @Override
    public void visit(BLangResource resourceNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        typeDefinition.symbol = null;
        typeDefinition.type = null;
        typeDefinition.typeNode.accept(this);
        typeDefinition.annAttachments.forEach(annotation -> annotation.accept(this));
    }

    @Override
    public void visit(BLangConstant constant) {
        constant.symbol = null;
        constant.type = null;
        if (constant.typeNode != null) {
            constant.typeNode.accept(this);
        }
        constant.annAttachments.forEach(annotation -> annotation.accept(this));
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        varNode.symbol = null;
        varNode.type = null;
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
        workerNode.requiredParams.forEach(param -> param.accept(this));
        workerNode.returnTypeNode.accept(this);
        workerNode.returnTypeAnnAttachments.forEach(annotations -> annotations.accept(this));
        workerNode.body.accept(this);
        workerNode.annAttachments.forEach(annotation -> annotation.accept(this));
        workerNode.endpoints.forEach(endpoint -> endpoint.accept(this));
        workerNode.workers.forEach(worker -> worker.accept(this));
        workerNode.defaultableParams.forEach(parameter -> parameter.accept(this));
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
        annotationNode.symbol = null;
        annotationNode.type = null;
        annotationNode.annAttachments.forEach(attachment -> attachment.accept(this));
        annotationNode.typeNode.accept(this);
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        annAttachmentNode.type = null;
        if (annAttachmentNode.expr != null) {
            annAttachmentNode.expr.accept(this);
        }
        annAttachmentNode.annotationSymbol = null;
    }

    // Statements.

    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.stmts.forEach(statement -> statement.accept(this));
        if (blockNode.scope != null) {
            blockNode.scope.entries.clear();
        }
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        varDefNode.var.accept(this);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        assignNode.varRef.accept(this);
        assignNode.expr.accept(this);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
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
        returnNode.expr.accept(this);
    }

    @Override
    public void visit(BLangThrow throwNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangPanic panicNode) {
        panicNode.expr.accept(this);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.type = null;
        xmlnsStmtNode.xmlnsDecl.accept(this);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.expr.accept(this);
    }

    @Override
    public void visit(BLangIf ifNode) {
        ifNode.expr.accept(this);
        ifNode.body.accept(this);
        if (ifNode.elseStmt != null) {
            ifNode.elseStmt.accept(this);
        }
    }

    @Override
    public void visit(BLangMatch matchNode) {
        // Do nothing.
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangForeach foreach) {
        foreach.collection.accept(this);
        foreach.body.accept(this);
        ((BLangNode) foreach.variableDefinitionNode).accept(this);
        foreach.varType = null;
        foreach.resultType = null;
        foreach.nillableResultType = null;
    }

    @Override
    public void visit(BLangWhile whileNode) {
        whileNode.expr.accept(this);
        whileNode.body.accept(this);
    }

    @Override
    public void visit(BLangLock lockNode) {
        lockNode.body.accept(this);
        lockNode.lockVariables.clear();
        lockNode.fieldVariables.clear();
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
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
        stmt.type = null;
        stmt.varRef.accept(this);
        stmt.expr.accept(this);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        stmt.varRef.accept(this);
        stmt.expr.accept(this);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        stmt.type = null;
        stmt.varRef.accept(this);
        stmt.expr.accept(this);
    }

    @Override
    public void visit(BLangCatch catchNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        forkJoin.workers.forEach(variableDef -> variableDef.accept(this));
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        orderBy.getVariables().forEach(orderByVariableNode ->
                ((BLangOrderByVariable) orderByVariableNode).accept(this));
    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {
        ((BLangExpression) orderByVariable.getVariableReference()).accept(this);
    }

    @Override
    public void visit(BLangLimit limit) {
        // Do nothing.
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        groupBy.getVariables().forEach(expressionNode -> ((BLangExpression) expressionNode).accept(this));
    }

    @Override
    public void visit(BLangHaving having) {
        ((BLangExpression) having.getExpression()).accept(this);
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
        ((BLangExpression) selectExpression.getExpression()).accept(this);
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
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
        whereClause.type = null;
        ((BLangExpression) whereClause.getExpression()).accept(this);
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
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
        if (joinStreamingInput.getStreamingInput() != null) {
            ((BLangStreamingInput) joinStreamingInput.getStreamingInput()).accept(this);
        }
        if (joinStreamingInput.getOnExpression() != null) {
            ((BLangExpression) joinStreamingInput.getOnExpression()).accept(this);
        }
    }

    @Override
    public void visit(BLangTableQuery tableQuery) {
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
        throw new AssertionError();
    }

    @Override
    public void visit(BLangWindow windowClause) {
        if (windowClause.getFunctionInvocation() != null) {
            ((BLangExpression) windowClause.getFunctionInvocation()).accept(this);
        }
    }

    @Override
    public void visit(BLangPatternStreamingInput patternStreamingInput) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.type = null;

        workerSendNode.expr.accept(this);
        if (workerSendNode.keyExpr != null) {
            workerSendNode.keyExpr.accept(this);
        }
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        workerReceiveNode.typeChecked = false;
        workerReceiveNode.type = null;
        if (workerReceiveNode.keyExpr != null) {
            workerReceiveNode.keyExpr.accept(this);
        }
        workerReceiveNode.env = null;
        workerReceiveNode.workerType = null;
        workerReceiveNode.matchingSendsError = null;
        if (workerReceiveNode.sendExpression != null) {
            workerReceiveNode.sendExpression.accept(this);
        }
    }

    @Override
    public void visit(BLangForever foreverStatement) {
        foreverStatement.type = null;

        foreverStatement.getStreamingQueryStatements().forEach(streamingQueryStatementNode ->
                ((BLangStreamingQueryStatement) streamingQueryStatementNode).accept(this));
        if (foreverStatement.params != null) {
            foreverStatement.params.forEach(variable -> variable.accept(this));
        }
    }


    // Expressions
    @Override
    public void visit(BLangLiteral literalExpr) {
        //        literalExpr.type = null;
        //        literalExpr.typeChecked = false;
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
        literalExpr.typeChecked = false;
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        tableLiteral.typeChecked = false;

        tableLiteral.type = null;
        tableLiteral.columns.forEach(bLangTableColumn -> bLangTableColumn.accept(this));
        tableLiteral.tableDataRows.forEach(bLangExpression -> bLangExpression.accept(this));
        if (tableLiteral.indexColumnsArrayLiteral != null) {
            tableLiteral.indexColumnsArrayLiteral.accept(this);
        }
        if (tableLiteral.keyColumnsArrayLiteral != null) {
            tableLiteral.keyColumnsArrayLiteral.accept(this);
        }
    }

    @Override
    public void visit(BLangArrayLiteral arrayLiteral) {
        arrayLiteral.typeChecked = false;
        arrayLiteral.exprs.forEach(expression -> expression.accept(this));
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        recordLiteral.typeChecked = false;
        recordLiteral.type = null;
        recordLiteral.keyValuePairs.forEach(keyValue -> {
            keyValue.key.accept(this);
            keyValue.valueExpr.accept(this);
        });
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        varRefExpr.typeChecked = false;

        varRefExpr.symbol = null;
        varRefExpr.type = null;

        varRefExpr.varSymbol = null;
        varRefExpr.expressions.forEach(bLangExpression -> bLangExpression.accept(this));
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        varRefExpr.typeChecked = false;

        varRefExpr.symbol = null;
        varRefExpr.type = null;

        varRefExpr.varSymbol = null;
        varRefExpr.recordRefFields.forEach(bLangRecordVarRefKeyValue -> {
            bLangRecordVarRefKeyValue.variableReference.accept(this);
            bLangRecordVarRefKeyValue.variableName.accept(this);
        });

        // Todo - varRefExpr.restParam.
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        varRefExpr.typeChecked = false;

        varRefExpr.symbol = null;
        varRefExpr.type = null;

        varRefExpr.varSymbol = null;

        varRefExpr.detail.accept(this);
        varRefExpr.reason.accept(this);
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        varRefExpr.symbol = null;
        varRefExpr.type = null;

        varRefExpr.typeChecked = false;

        varRefExpr.varSymbol = null;
        varRefExpr.pkgSymbol = null;
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        fieldAccessExpr.symbol = null;
        fieldAccessExpr.type = null;

        fieldAccessExpr.typeChecked = false;

        fieldAccessExpr.varSymbol = null;
        fieldAccessExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        indexAccessExpr.symbol = null;
        indexAccessExpr.type = null;

        indexAccessExpr.typeChecked = false;

        indexAccessExpr.expr.accept(this);
        indexAccessExpr.indexExpr.accept(this);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        invocationExpr.symbol = null;
        invocationExpr.type = null;

        invocationExpr.typeChecked = false;

        if (invocationExpr.expr != null) {
            invocationExpr.expr.accept(this);
        }
        invocationExpr.originalType = null;

        invocationExpr.argExprs.forEach(arg -> arg.accept(this));
        invocationExpr.exprSymbol = null;
        invocationExpr.requiredArgs.forEach(arg -> arg.accept(this));
        invocationExpr.namedArgs.forEach(arg -> arg.accept(this));
        invocationExpr.restArgs.forEach(arg -> arg.accept(this));
    }

    @Override
    public void visit(BLangTypeInit connectorInitExpr) {
        connectorInitExpr.type = null;

        connectorInitExpr.typeChecked = false;

        if (connectorInitExpr.userDefinedType != null) {
            connectorInitExpr.userDefinedType.accept(this);
        }
        connectorInitExpr.argsExpr.forEach(arg -> arg.accept(this));
        connectorInitExpr.initInvocation.accept(this);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        actionInvocationExpr.typeChecked = false;
        throw new AssertionError();
    }

    @Override
    public void visit(BLangInvocation.BLangBuiltInMethodInvocation builtInMethodInvocation) {
        builtInMethodInvocation.typeChecked = false;
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.typeChecked = false;

        ternaryExpr.expr.accept(this);
        ternaryExpr.thenExpr.accept(this);
        ternaryExpr.elseExpr.accept(this);
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {
        awaitExpr.typeChecked = false;
        awaitExpr.exprList.forEach(bLangExpression -> bLangExpression.accept(this));
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        trapExpr.typeChecked = false;
        trapExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.typeChecked = false;
        binaryExpr.type = null;
        binaryExpr.opSymbol = null;
        binaryExpr.rhsExpr.accept(this);
        binaryExpr.rhsExpr.accept(this);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        elvisExpr.typeChecked = false;
        elvisExpr.type = null;
        elvisExpr.lhsExpr.accept(this);
        elvisExpr.rhsExpr.accept(this);
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        bracedOrTupleExpr.type = null;
        bracedOrTupleExpr.typeChecked = false;
        bracedOrTupleExpr.expressions.forEach(expression -> expression.accept(this));
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        unaryExpr.typeChecked = false;
        unaryExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        accessExpr.typeChecked = false;
        accessExpr.type = null;
        accessExpr.resolvedType = null;
        accessExpr.typeNode.accept(this);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        conversionExpr.type = null;

        conversionExpr.typeChecked = false;

        conversionExpr.expr.accept(this);
        conversionExpr.typeNode.accept(this);
        conversionExpr.targetType = null;
        conversionExpr.conversionSymbol = null;
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        xmlQName.typeChecked = false;
        xmlQName.type = null;
        xmlQName.nsSymbol = null;
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        xmlAttribute.typeChecked = false;

        xmlAttribute.symbol = null;
        xmlAttribute.type = null;

        xmlAttribute.name.accept(this);
        xmlAttribute.value.accept(this);
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        xmlElementLiteral.typeChecked = false;

        xmlElementLiteral.type = null;

        xmlElementLiteral.startTagName.accept(this);
        if (xmlElementLiteral.endTagName != null) {
            xmlElementLiteral.endTagName.accept(this);
        }
        xmlElementLiteral.attributes.forEach(bLangXMLAttribute -> bLangXMLAttribute.accept(this));
        xmlElementLiteral.namespacesInScope.clear();
        xmlElementLiteral.inlineNamespaces.forEach(bLangXMLNS -> bLangXMLNS.accept(this));
        xmlElementLiteral.defaultNsSymbol = null;
        if (xmlElementLiteral.modifiedChildren != null) {
            xmlElementLiteral.modifiedChildren.forEach(bLangExpression -> bLangExpression.accept(this));
        }
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        xmlTextLiteral.typeChecked = false;
        xmlTextLiteral.type = null;

        xmlTextLiteral.textFragments.forEach(bLangExpression -> bLangExpression.accept(this));
        if (xmlTextLiteral.concatExpr != null) {
            xmlTextLiteral.concatExpr.accept(this);
        }
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        xmlCommentLiteral.typeChecked = false;

        xmlCommentLiteral.type = null;
        xmlCommentLiteral.textFragments.forEach(bLangExpression -> bLangExpression.accept(this));
        if (xmlCommentLiteral.concatExpr != null) {
            xmlCommentLiteral.concatExpr.accept(this);
        }
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.typeChecked = false;

        xmlProcInsLiteral.type = null;
        xmlProcInsLiteral.dataFragments.forEach(bLangExpression -> bLangExpression.accept(this));
        if (xmlProcInsLiteral.dataConcatExpr != null) {
            xmlProcInsLiteral.dataConcatExpr.accept(this);
        }
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        xmlQuotedString.typeChecked = false;

        xmlQuotedString.type = null;
        xmlQuotedString.textFragments.forEach(bLangExpression -> bLangExpression.accept(this));
        if (xmlQuotedString.concatExpr != null) {
            xmlQuotedString.concatExpr.accept(this);
        }
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.type = null;
        stringTemplateLiteral.typeChecked = false;
        stringTemplateLiteral.exprs.forEach(expression -> expression.accept(this));
        if (stringTemplateLiteral.concatExpr != null) {
            stringTemplateLiteral.concatExpr.accept(this);
        }
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        bLangLambdaFunction.type = null;
        bLangLambdaFunction.typeChecked = false;
        bLangLambdaFunction.function.accept(this);
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        bLangArrowFunction.typeChecked = false;
        bLangArrowFunction.type = null;
        bLangArrowFunction.params.forEach(variable -> variable.accept(this));

        bLangArrowFunction.expression.accept(this);
        bLangArrowFunction.funcType = null;
        if (bLangArrowFunction.function != null) {
            bLangArrowFunction.function.accept(this);
        }
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        xmlAttributeAccessExpr.typeChecked = false;

        xmlAttributeAccessExpr.symbol = null;
        xmlAttributeAccessExpr.type = null;
        xmlAttributeAccessExpr.namespaces.clear();
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        intRangeExpression.typeChecked = false;
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {
        tableQueryExpression.typeChecked = false;

        tableQueryExpression.type = null;
        ((BLangTableQuery) tableQueryExpression.getTableQuery()).accept(this);
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        bLangVarArgsExpression.typeChecked = false;

        bLangVarArgsExpression.type = null;
        bLangVarArgsExpression.expr.accept(this);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        bLangNamedArgsExpression.typeChecked = false;
        bLangNamedArgsExpression.expr.accept(this);
    }

    @Override
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
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
        throw new AssertionError();
    }

    @Override
    public void visit(BLangOutputRateLimit outputRateLimit) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangPatternClause patternClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        assignableExpr.typeChecked = false;
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        bLangMatchExpression.typeChecked = false;
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        checkedExpr.typeChecked = false;
        checkedExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        checkPanickedExpr.typeChecked = false;
        checkPanickedExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        errorConstructorExpr.typeChecked = false;
        errorConstructorExpr.type = null;
        errorConstructorExpr.reasonExpr.accept(this);
        if (errorConstructorExpr.detailsExpr != null) {
            errorConstructorExpr.detailsExpr.accept(this);
        }
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        serviceConstructorExpr.typeChecked = false;
        serviceConstructorExpr.serviceNode.accept(this);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        typeTestExpr.typeChecked = false;
        typeTestExpr.expr.accept(this);
        typeTestExpr.typeNode.accept(this);
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        typeTestExpr.typeChecked = false;
        throw new AssertionError();
    }

    // Type nodes
    @Override
    public void visit(BLangValueType valueType) {
        valueType.type = null;
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        arrayType.elemtype.accept(this);
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        // Do nothing.
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        constrainedType.type.accept(this);
        constrainedType.constraint.accept(this);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        userDefinedType.type = null;
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        functionTypeNode.type = null;
        functionTypeNode.params.forEach(variable -> variable.accept(this));
        functionTypeNode.returnTypeNode.accept(this);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        unionTypeNode.memberTypeNodes.forEach(bLangType -> bLangType.accept(this));
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        objectTypeNode.symbol = null;
        objectTypeNode.type = null;

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
        recordTypeNode.symbol = null;
        recordTypeNode.type = null;

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
        finiteTypeNode.valueSpace.forEach(bLangExpression -> bLangExpression.accept(this));
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        tupleTypeNode.memberTypeNodes.forEach(bLangType -> bLangType.accept(this));
    }

    @Override
    public void visit(BLangErrorType errorType) {
        // Do nothing
    }


    // expressions that will used only from the Desugar phase

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
    public void visit(BLangSimpleVarRef.BLangConstRef constRef) {
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
        fieldAccessExpr.typeChecked = false;
        throw new AssertionError();
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
        functionVarRef.typeChecked = false;
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        mapKeyAccessExpr.typeChecked = false;
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        arrayIndexAccessExpr.typeChecked = false;
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr arrayIndexAccessExpr) {
        arrayIndexAccessExpr.typeChecked = false;
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlIndexAccessExpr) {
        xmlIndexAccessExpr.typeChecked = false;
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {
        jsonLiteral.typeChecked = false;
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
    public void visit(BLangRecordLiteral.BLangStreamLiteral streamLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRecordLiteral.BLangChannelLiteral channelLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        bFunctionPointerInvocation.typeChecked = false;
        throw new AssertionError();
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        iExpr.typeChecked = false;
        throw new AssertionError();
    }

    @Override
    public void visit(BLangArrayLiteral.BLangJSONArrayLiteral jsonArrayLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        xmlnsNode.symbol = null;
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        xmlnsNode.symbol = null;
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
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        bLangTupleVariable.symbol = null;
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        bLangTupleVariableDef.var.accept(this);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        bLangRecordVariable.symbol = null;
        bLangRecordVariable.type = null;

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
        bLangRecordVariableDef.var.accept(this);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        bLangErrorVariable.symbol = null;
        bLangErrorVariable.type = null;

        bLangErrorVariable.reason.accept(this);
        bLangErrorVariable.detail.accept(this);

        if (bLangErrorVariable.typeNode != null) {
            bLangErrorVariable.typeNode.accept(this);
        }
        bLangErrorVariable.expr.accept(this);
        bLangErrorVariable.annAttachments.forEach(annotation -> annotation.accept(this));
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        bLangErrorVariableDef.errorVariable.accept(this);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause bLangMatchStmtStaticBindingPatternClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause matchStmtStructuredBindingPatternClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        workerFlushExpr.typeChecked = false;
        workerFlushExpr.type = null;
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        syncSendExpr.typeChecked = false;
        syncSendExpr.type = null;

        syncSendExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
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
        throw new AssertionError();
    }
}
