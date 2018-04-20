/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.common;

import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangDeprecatedNode;
import org.wso2.ballerinalang.compiler.tree.BLangDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFunctionClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAwaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDocumentationAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangBind;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDone;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPostIncrement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

/**
 * Common node visitor to override and remove assertion errors from BLangNodeVisitor methods.
 */
public class LSNodeVisitor extends BLangNodeVisitor {
    @Override
    public void visit(BLangPackage pkgNode) {
        // No implementation
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        // No implementation
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        // No implementation
    }

    @Override
    public void visit(BLangFunction funcNode) {
        // No implementation
    }

    @Override
    public void visit(BLangService serviceNode) {
        // No implementation
    }

    @Override
    public void visit(BLangResource resourceNode) {
        // No implementation
    }

    @Override
    public void visit(BLangConnector connectorNode) {
        // No implementation
    }

    @Override
    public void visit(BLangAction actionNode) {
        // No implementation
    }

    @Override
    public void visit(BLangStruct structNode) {
        // No implementation
    }

    @Override
    public void visit(BLangObject objectNode) {
        // No implementation
    }

    @Override
    public void visit(BLangRecord record) {
        // No implementation
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        // No implementation
    }

    @Override
    public void visit(BLangEnum enumNode) {
        // No implementation
    }

    @Override
    public void visit(BLangEnum.BLangEnumerator enumeratorNode) {
        // No implementation
    }

    @Override
    public void visit(BLangVariable varNode) {
        // No implementation
    }

    @Override
    public void visit(BLangWorker workerNode) {
        // No implementation
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
        // No implementation
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
        // No implementation
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        // No implementation
    }

    @Deprecated
    @Override
    public void visit(BLangAnnotAttribute annotationAttribute) {
        // No implementation
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        // No implementation
    }

    @Override
    public void visit(BLangAnnotAttachmentAttributeValue annotAttributeValue) {
        // No implementation
    }

    @Override
    public void visit(BLangAnnotAttachmentAttribute annotAttachmentAttribute) {
        // No implementation
    }

    @Override
    public void visit(BLangTransformer transformerNode) {
        // No implementation
    }

    @Override
    public void visit(BLangDocumentationAttribute docAttribute) {
        // No implementation
    }

    @Override
    public void visit(BLangDocumentation doc) {
        // No implementation
    }

    @Override
    public void visit(BLangDeprecatedNode deprecatedNode) {
        // No implementation
    }

    // Statements
    @Override
    public void visit(BLangBlockStmt blockNode) {
        // No implementation
    }

    @Override
    public void visit(BLangVariableDef varDefNode) {
        // No implementation
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        // No implementation
    }

    @Override
    public void visit(BLangPostIncrement increment) {
        // No implementation
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        // No implementation
    }

    @Override
    public void visit(BLangBind bindNode) {
        // No implementation
    }

    @Override
    public void visit(BLangAbort abortNode) {
        // No implementation
    }

    @Override
    public void visit(BLangDone doneNode) {
        // No implementation
    }

    @Override
    public void visit(BLangRetry retryNode) {
        // No implementation
    }

    @Override
    public void visit(BLangNext nextNode) {
        // No implementation
    }

    @Override
    public void visit(BLangBreak breakNode) {
        // No implementation
    }

    @Override
    public void visit(BLangReturn returnNode) {
        // No implementation
    }

    @Override
    public void visit(BLangThrow throwNode) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        // No implementation
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        // No implementation
    }

    @Override
    public void visit(BLangIf ifNode) {
        // No implementation
    }

    @Override
    public void visit(BLangMatch matchNode) {
        // No implementation
    }

    @Override
    public void visit(BLangMatch.BLangMatchStmtPatternClause patternClauseNode) {
        // No implementation
    }

    @Override
    public void visit(BLangForeach foreach) {
        // No implementation
    }

    @Override
    public void visit(BLangWhile whileNode) {
        // No implementation
    }

    @Override
    public void visit(BLangLock lockNode) {
        // No implementation
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        // No implementation
    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {
        // No implementation
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        // No implementation
    }

    @Override
    public void visit(BLangCatch catchNode) {
        // No implementation
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        // No implementation
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        // No implementation
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        // No implementation
    }

    @Override
    public void visit(BLangHaving having) {
        // No implementation
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        // No implementation
    }

    @Override
    public void visit(BLangWhere whereClause) {
        // No implementation
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        // No implementation
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        // No implementation
    }

    @Override
    public void visit(BLangTableQuery tableQuery) {
        // No implementation
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        // No implementation
    }

    @Override
    public void visit(BLangFunctionClause functionClause) {
        // No implementation
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {
        // No implementation
    }

    @Override
    public void visit(BLangPatternStreamingEdgeInput patternStreamingEdgeInput) {
        // No implementation
    }

    @Override
    public void visit(BLangWindow windowClause) {
        // No implementation
    }

    @Override
    public void visit(BLangPatternStreamingInput patternStreamingInput) {
        // No implementation
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        // No implementation
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        // No implementation
    }

    @Override
    public void visit(BLangForever foreverStatement) {
        // No implementation
    }


    // Expressions
    @Override
    public void visit(BLangLiteral literalExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangArrayLiteral arrayLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTypeInit connectorInitExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangAwaitExpr ternaryExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTypeCastExpr castExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        // No implementation
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        // No implementation
    }

    @Override
    public void visit(BLangWithinClause withinClause) {
        // No implementation
    }

    @Override
    public void visit(BLangOutputRateLimit outputRateLimit) {
        // No implementation
    }

    @Override
    public void visit(BLangPatternClause patternClause) {
        // No implementation
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {
        // No implementation
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        // No implementation
    }

    // Type nodes
    @Override
    public void visit(BLangValueType valueType) {
        // No implementation
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        // No implementation
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        // No implementation
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        // No implementation
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        // No implementation
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        // No implementation
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        // No implementation
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        // No implementation
    }


    // expressions that will used only from the Desugar phase
    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        // No implementation
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        // No implementation
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        // No implementation
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        // No implementation
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlIndexAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStreamLiteral streamLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        // No implementation
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangInvocation.BLangTransformerInvocation iExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangArrayLiteral.BLangJSONArrayLiteral jsonArrayLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        // No implementation
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangEnumeratorAccessExpr enumeratorAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        // No implementation
    }
}
