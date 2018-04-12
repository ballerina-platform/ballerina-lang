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
package org.wso2.ballerinalang.compiler.tree;

import org.wso2.ballerinalang.compiler.tree.BLangEnum.BLangEnumerator;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangLocalXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangPackageXMLNS;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral.BLangJSONArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAwaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDocumentationAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangEnumeratorAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangStructFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangArrayAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangJSONAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangMapAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangXMLAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BFunctionPointerInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangActionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangTransformerInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression.BLangMatchExprPatternClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangJSONLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStreamLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStructLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFieldVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangLocalVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangPackageVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangTypeLoad;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangFail;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPostIncrement;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangSingletonTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

/**
 * @since 0.94
 */
public abstract class BLangNodeVisitor {

    public void visit(BLangPackage pkgNode) {
        throw new AssertionError();
    }

    public void visit(BLangCompilationUnit compUnit) {
        throw new AssertionError();
    }

    public void visit(BLangPackageDeclaration pkgDclNode) {
        throw new AssertionError();
    }

    public void visit(BLangImportPackage importPkgNode) {
        throw new AssertionError();
    }

    public void visit(BLangXMLNS xmlnsNode) {
        throw new AssertionError();
    }

    public void visit(BLangFunction funcNode) {
        throw new AssertionError();
    }

    public void visit(BLangService serviceNode) {
        throw new AssertionError();
    }

    public void visit(BLangResource resourceNode) {
        throw new AssertionError();
    }

    public void visit(BLangConnector connectorNode) {
        throw new AssertionError();
    }

    public void visit(BLangAction actionNode) {
        throw new AssertionError();
    }

    public void visit(BLangStruct structNode) {
        throw new AssertionError();
    }

    public void visit(BLangObject objectNode) {
        throw new AssertionError();
    }

    public void visit(BLangRecord record) {
        throw new AssertionError();
    }

    public void visit(BLangTypeDefinition typeDefinition) {
        throw new AssertionError();
    }

    public void visit(BLangSingleton singleton) {
        throw new AssertionError();
    }

    public void visit(BLangEnum enumNode) {
        throw new AssertionError();
    }

    public void visit(BLangEnumerator enumeratorNode) {
        throw new AssertionError();
    }

    public void visit(BLangVariable varNode) {
        throw new AssertionError();
    }

    public void visit(BLangWorker workerNode) {
        throw new AssertionError();
    }

    public void visit(BLangEndpoint endpointNode) {
        throw new AssertionError();
    }

    public void visit(BLangIdentifier identifierNode) {
        throw new AssertionError();
    }

    public void visit(BLangAnnotation annotationNode) {
        throw new AssertionError();
    }

    @Deprecated
    public void visit(BLangAnnotAttribute annotationAttribute) {
        throw new AssertionError();
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        throw new AssertionError();
    }

    public void visit(BLangAnnotAttachmentAttributeValue annotAttributeValue) {
        throw new AssertionError();
    }

    public void visit(BLangAnnotAttachmentAttribute annotAttachmentAttribute) {
        throw new AssertionError();
    }

    public void visit(BLangTransformer transformerNode) {
        throw new AssertionError();
    }

    public void visit(BLangDocumentationAttribute docAttribute) {
        throw new AssertionError();
    }

    public void visit(BLangDocumentation doc) {
        throw new AssertionError();
    }

    public void visit(BLangDeprecatedNode deprecatedNode) {
        throw new AssertionError();
    }

    // Statements
    public void visit(BLangBlockStmt blockNode) {
        throw new AssertionError();
    }

    public void visit(BLangVariableDef varDefNode) {
        throw new AssertionError();
    }

    public void visit(BLangAssignment assignNode) {
        throw new AssertionError();
    }

    public void visit(BLangPostIncrement increment) {
        throw new AssertionError();
    }

    public void visit(BLangCompoundAssignment compoundAssignNode) {
        throw new AssertionError();
    }

    public void visit(BLangBind bindNode) {
        throw new AssertionError();
    }

    public void visit(BLangAbort abortNode) {
        throw new AssertionError();
    }
    
    public void visit(BLangDone doneNode) {
        throw new AssertionError();
    }

    public void visit(BLangFail failNode) {
        throw new AssertionError();
    }

    public void visit(BLangNext nextNode) {
        throw new AssertionError();
    }

    public void visit(BLangBreak breakNode) {
        throw new AssertionError();
    }

    public void visit(BLangReturn returnNode) {
        throw new AssertionError();
    }

    public void visit(BLangThrow throwNode) {
        throw new AssertionError();
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        throw new AssertionError();
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        throw new AssertionError();
    }

    public void visit(BLangIf ifNode) {
        throw new AssertionError();
    }

    public void visit(BLangMatch matchNode) {
        throw new AssertionError();
    }

    public void visit(BLangMatch.BLangMatchStmtPatternClause patternClauseNode) {
        throw new AssertionError();
    }

    public void visit(BLangForeach foreach) {
        throw new AssertionError();
    }

    public void visit(BLangWhile whileNode) {
        throw new AssertionError();
    }

    public void visit(BLangLock lockNode) {
        throw new AssertionError();
    }

    public void visit(BLangTransaction transactionNode) {
        throw new AssertionError();
    }

    public void visit(BLangTryCatchFinally tryNode) {
        throw new AssertionError();
    }

    public void visit(BLangTupleDestructure stmt) {
        throw new AssertionError();
    }

    public void visit(BLangCatch catchNode) {
        throw new AssertionError();
    }

    public void visit(BLangForkJoin forkJoin) {
        throw new AssertionError();
    }

    public void visit(BLangOrderBy orderBy) {
        throw new AssertionError();
    }

    public void visit(BLangGroupBy groupBy) {
        throw new AssertionError();
    }

    public void visit(BLangHaving having) {
        throw new AssertionError();
    }

    public void visit(BLangSelectExpression selectExpression) {
        throw new AssertionError();
    }

    public void visit(BLangSelectClause selectClause) {
        throw new AssertionError();
    }

    public void visit(BLangWhere whereClause) {
        throw new AssertionError();
    }

    public void visit(BLangStreamingInput streamingInput) {
        throw new AssertionError();
    }

    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        throw new AssertionError();
    }

    public void visit(BLangTableQuery tableQuery) {
        throw new AssertionError();
    }

    public void visit(BLangStreamAction streamAction) {
        throw new AssertionError();
    }

    public void visit(BLangFunctionClause functionClause) {
        throw new AssertionError();
    }

    public void visit(BLangSetAssignment setAssignmentClause) {
        throw new AssertionError();
    }

    public void visit(BLangPatternStreamingEdgeInput patternStreamingEdgeInput) {
        throw new AssertionError();
    }

    public void visit(BLangWindow windowClause) {
        throw new AssertionError();
    }

    public void visit(BLangPatternStreamingInput patternStreamingInput) {
        throw new AssertionError();
    }

    public void visit(BLangWorkerSend workerSendNode) {
        throw new AssertionError();
    }

    public void visit(BLangWorkerReceive workerReceiveNode) {
        throw new AssertionError();
    }

    public void visit(BLangForever foreverStatement) {
        throw new AssertionError();
    }


    // Expressions

    public void visit(BLangLiteral literalExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTableLiteral tableLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        throw new AssertionError();
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangInvocation invocationExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTypeInit connectorInitExpr) {
        throw new AssertionError();
    }

    public void visit(BLangActionInvocation actionInvocationExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        throw new AssertionError();
    }

    public void visit(BLangAwaitExpr ternaryExpr) {
        throw new AssertionError();
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        throw new AssertionError();
    }

    public void visit(BLangElvisExpr elvisExpr) {
        throw new AssertionError();
    }

    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        throw new AssertionError();
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTypedescExpr accessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTypeCastExpr castExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        throw new AssertionError();
    }

    public void visit(BLangXMLQName xmlQName) {
        throw new AssertionError();
    }

    public void visit(BLangXMLAttribute xmlAttribute) {
        throw new AssertionError();
    }

    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangXMLQuotedString xmlQuotedString) {
        throw new AssertionError();
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        throw new AssertionError();
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangIntRangeExpression intRangeExpression) {
        throw new AssertionError();
    }

    public void visit(BLangTableQueryExpression tableQueryExpression) {
        throw new AssertionError();
    }

    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        throw new AssertionError();
    }

    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        throw new AssertionError();
    }

    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        throw new AssertionError();
    }

    public void visit(BLangWithinClause withinClause) {
        throw new AssertionError();
    }

    public void visit(BLangOutputRateLimit outputRateLimit) {
        throw new AssertionError();
    }

    public void visit(BLangPatternClause patternClause) {
        throw new AssertionError();
    }

    public void visit(BLangIsAssignableExpr assignableExpr) {
        throw new AssertionError();
    }

    public void visit(BLangMatchExpression bLangMatchExpression) {
        throw new AssertionError();
    }

    public void visit(BLangMatchExprPatternClause bLangMatchExprPatternClause) {
        throw new AssertionError();
    }

    public void visit(BLangCheckedExpr checkedExpr) {
        throw new AssertionError();
    }

    // Type nodes

    public void visit(BLangValueType valueType) {
        throw new AssertionError();
    }

    public void visit(BLangArrayType arrayType) {
        throw new AssertionError();
    }

    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        throw new AssertionError();
    }

    public void visit(BLangConstrainedType constrainedType) {
        throw new AssertionError();
    }

    public void visit(BLangUserDefinedType userDefinedType) {
        throw new AssertionError();
    }

    public void visit(BLangFunctionTypeNode functionTypeNode) {
        throw new AssertionError();
    }

    public void visit(BLangUnionTypeNode unionTypeNode) {
        throw new AssertionError();
    }

    public void visit(BLangTupleTypeNode tupleTypeNode) {
        throw new AssertionError();
    }

    public void visit(BLangSingletonTypeNode singletonType) {
        throw new AssertionError();
    }


    // expressions that will used only from the Desugar phase

    public void visit(BLangLocalVarRef localVarRef) {
        throw new AssertionError();
    }

    public void visit(BLangFieldVarRef fieldVarRef) {
        throw new AssertionError();
    }

    public void visit(BLangPackageVarRef packageVarRef) {
        throw new AssertionError();
    }

    public void visit(BLangFunctionVarRef functionVarRef) {
        throw new AssertionError();
    }

    public void visit(BLangTypeLoad typeLoad) {
        throw new AssertionError();
    }

    public void visit(BLangStructFieldAccessExpr fieldAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangStructFunctionVarRef functionVarRef) {
        throw new AssertionError();
    }

    public void visit(BLangMapAccessExpr mapKeyAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangArrayAccessExpr arrayIndexAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangXMLAccessExpr xmlIndexAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangJSONLiteral jsonLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangMapLiteral mapLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangStructLiteral structLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangStreamLiteral streamLiteral) {
        throw new AssertionError();
    }

    public void visit(BFunctionPointerInvocation bFunctionPointerInvocation) {
        throw new AssertionError();
    }

    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTransformerInvocation iExpr) {
        throw new AssertionError();
    }

    public void visit(BLangJSONArrayLiteral jsonArrayLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangJSONAccessExpr jsonAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangLocalXMLNS xmlnsNode) {
        throw new AssertionError();
    }

    public void visit(BLangPackageXMLNS xmlnsNode) {
        throw new AssertionError();
    }

    public void visit(BLangEnumeratorAccessExpr enumeratorAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangStatementExpression bLangStatementExpression) {
        throw new AssertionError();
    }
}
