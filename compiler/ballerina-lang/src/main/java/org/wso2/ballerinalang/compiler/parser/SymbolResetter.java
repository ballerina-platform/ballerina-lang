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

    public void resetTopLevelNode(TopLevelNode node) {
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
        funcNode.returnTypeNode.type.tsymbol = null;
        funcNode.returnTypeAnnAttachments.forEach(annotations -> annotations.accept(this));
        funcNode.body.stmts.forEach(statement -> statement.accept(this));
        funcNode.annAttachments.forEach(annotation -> annotation.accept(this));
        funcNode.endpoints.forEach(endpoint -> endpoint.accept(this));
        funcNode.workers.forEach(worker -> worker.accept(this));
        funcNode.defaultableParams.forEach(parameter -> parameter.accept(this));
        funcNode.restParam.accept(this);
    }

    @Override
    public void visit(BLangService serviceNode) {
        serviceNode.symbol = null;
        serviceNode.type = null;
        serviceNode.annAttachments.forEach(annotation -> annotation.accept(this));

        serviceNode.serviceTypeDefinition.accept(this);
        serviceNode.attachedExprs.forEach(expression -> expression.accept(this));
        serviceNode.variableNode.accept(this);
        serviceNode.listenerType.tsymbol = null;
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
        typeDefinition.typeNode.type = null;
        typeDefinition.annAttachments.forEach(annotation -> annotation.accept(this));
    }

    @Override
    public void visit(BLangConstant constant) {
        constant.symbol = null;
        constant.type = null;
        constant.typeNode.type = null;
        constant.annAttachments.forEach(annotation -> annotation.accept(this));
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        varNode.symbol = null;
        varNode.type = null;
        varNode.typeNode.type = null;
        varNode.expr.accept(this);
        varNode.annAttachments.forEach(annotation -> annotation.accept(this));
    }

    @Override
    public void visit(BLangWorker workerNode) {
        workerNode.requiredParams.forEach(param -> param.accept(this));
        workerNode.returnTypeNode.type.tsymbol = null;
        workerNode.returnTypeAnnAttachments.forEach(annotations -> annotations.accept(this));
        workerNode.body.stmts.forEach(statement -> statement.accept(this));
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
        throw new AssertionError();
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        annotationNode.symbol = null;
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        throw new AssertionError();
    }

    // Statements.

    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.stmts.forEach(statement -> statement.accept(this));
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        assignNode.varRef.accept(this);
        assignNode.expr.accept(this);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangAbort abortNode) {
        // Do nothing
    }

    @Override
    public void visit(BLangRetry retryNode) {
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
    public void visit(BLangThrow throwNode) {
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
        exprStmtNode.expr.accept(this);
    }

    @Override
    public void visit(BLangIf ifNode) {
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
    public void visit(BLangTryCatchFinally tryNode) {
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
    public void visit(BLangCatch catchNode) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangLimit limit) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangHaving having) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangWhere whereClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTableQuery tableQuery) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        throw new AssertionError();
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
        throw new AssertionError();
    }

    @Override
    public void visit(BLangPatternStreamingInput patternStreamingInput) {
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
    public void visit(BLangForever foreverStatement) {
        throw new AssertionError();
    }


    // Expressions
    @Override
    public void visit(BLangLiteral literalExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangArrayLiteral arrayLiteral) {
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
        throw new AssertionError();
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
        throw new AssertionError();
    }

    @Override
    public void visit(BLangTypeInit connectorInitExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangInvocation.BLangBuiltInMethodInvocation builtInMethodInvocation) {
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
        throw new AssertionError();
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
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
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        throw new AssertionError();
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
    public void visit(BLangTableQueryExpression tableQueryExpression) {
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
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        throw new AssertionError();
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
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
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
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr arrayIndexAccessExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlIndexAccessExpr) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {
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
        throw new AssertionError();
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
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
        throw new AssertionError();
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        bLangRecordVariable.symbol = null;
        bLangRecordVariable.type = null;

        bLangRecordVariable.variableList.forEach(variable -> variable.valueBindingPattern.accept(this));
        // Todo - bLangRecordVariable.restParam

        bLangRecordVariable.typeNode.type = null;
        bLangRecordVariable.expr.accept(this);
        bLangRecordVariable.annAttachments.forEach(annotation -> annotation.accept(this));
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        throw new AssertionError();
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        bLangErrorVariable.symbol = null;
        bLangErrorVariable.type = null;

        bLangErrorVariable.reason.accept(this);
        bLangErrorVariable.detail.accept(this);

        bLangErrorVariable.typeNode.type = null;
        bLangErrorVariable.expr.accept(this);
        bLangErrorVariable.annAttachments.forEach(annotation -> annotation.accept(this));
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
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause bLangMatchStmtStructuredBindingPatternClause) {
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
}
