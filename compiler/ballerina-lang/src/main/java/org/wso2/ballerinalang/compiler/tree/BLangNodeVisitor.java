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

import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangLocalXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangPackageXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimitClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnConflictClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangStructFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangArrayAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangJSONAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangMapAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangTableAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangTupleAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangXMLAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BFunctionPointerInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangActionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangJSONArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangTupleLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecatedParametersDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecationDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression.BLangMatchExprPatternClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangChannelLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStructLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFieldVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangLocalVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangPackageVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangTypeLoad;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableMultiKeyExpr;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementFilter;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLNavigationAccess;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock.BLangLockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock.BLangUnLockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStaticBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStructuredBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
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

    public void visit(BLangTestablePackage testablePkgNode) {
        throw new AssertionError();
    }

    public void visit(BLangCompilationUnit compUnit) {
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

    public void visit(BLangBlockFunctionBody blockFuncBody) {
        throw new AssertionError();
    }

    public void visit(BLangExprFunctionBody exprFuncBody) {
        throw new AssertionError();
    }

    public void visit(BLangExternalFunctionBody externFuncBody) {
        throw new AssertionError();
    }

    public void visit(BLangService serviceNode) {
        throw new AssertionError();
    }

    public void visit(BLangResource resourceNode) {
        throw new AssertionError();
    }

    public void visit(BLangTypeDefinition typeDefinition) {
        throw new AssertionError();
    }

    public void visit(BLangConstant constant) {
        throw new AssertionError();
    }

    public void visit(BLangSimpleVariable varNode) {
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

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        throw new AssertionError();
    }

    public void visit(BLangTableKeySpecifier tableKeySpecifierNode) {
        throw new AssertionError();
    }

    public void visit(BLangTableKeyTypeConstraint tableKeyTypeConstraint) {
        throw new AssertionError();
    }

    // Statements
    public void visit(BLangBlockStmt blockNode) {
        throw new AssertionError();
    }

    public void visit(BLangLockStmt lockStmtNode) {
        throw new AssertionError();
    }

    public void visit(BLangUnLockStmt unLockNode) {
        throw new AssertionError();
    }

    public void visit(BLangSimpleVariableDef varDefNode) {
        throw new AssertionError();
    }

    public void visit(BLangAssignment assignNode) {
        throw new AssertionError();
    }

    public void visit(BLangCompoundAssignment compoundAssignNode) {
        throw new AssertionError();
    }

    public void visit(BLangAbort abortNode) {
        throw new AssertionError();
    }

    public void visit(BLangRetry retryNode) {
        throw new AssertionError();
    }

    public void visit(BLangContinue continueNode) {
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

    public void visit(BLangPanic panicNode) {
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

    public void visit(BLangQueryAction queryAction) {
        throw new AssertionError();
    }

    public void visit(BLangMatch matchNode) {
        throw new AssertionError();
    }

    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        throw new AssertionError();
    }

    public void visit(BLangForeach foreach) {
        throw new AssertionError();
    }

    public void visit(BLangFromClause fromClause) {
        throw new AssertionError();
    }

    public void visit(BLangJoinClause joinClause) {
        throw new AssertionError();
    }

    public void visit(BLangLetClause letClause) {
        throw new AssertionError();
    }

    public void visit(BLangOnClause onClause) {
        throw new AssertionError();
    }

    public void visit(BLangSelectClause selectClause) {
        throw new AssertionError();
    }

    public void visit(BLangWhereClause whereClause) {
        throw new AssertionError();
    }

    public void visit(BLangDoClause doClause) {
        throw new AssertionError();
    }

    public void visit(BLangOnConflictClause onConflictClause) {
        throw new AssertionError();
    }

    public void visit(BLangLimitClause limitClause) {
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

    public void visit(BLangRecordDestructure stmt) {
        throw new AssertionError();
    }

    public void visit(BLangErrorDestructure stmt) {
        throw new AssertionError();
    }

    public void visit(BLangCatch catchNode) {
        throw new AssertionError();
    }

    public void visit(BLangForkJoin forkJoin) {
        throw new AssertionError();
    }

    public void visit(BLangWorkerSend workerSendNode) {
        throw new AssertionError();
    }

    public void visit(BLangWorkerReceive workerReceiveNode) {
        throw new AssertionError();
    }

    // Expressions

    public void visit(BLangLiteral literalExpr) {
        throw new AssertionError();
    }

    public void visit(BLangConstRef constRef) {
        throw new AssertionError();
    }

    public void visit(BLangNumericLiteral literalExpr) {
        throw new AssertionError();
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangTupleVarRef varRefExpr) {
        throw new AssertionError();
    }

    public void visit(BLangRecordVarRef varRefExpr) {
        throw new AssertionError();
    }

    public void visit(BLangErrorVarRef varRefExpr) {
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

    public void visit(BLangWaitExpr awaitExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTrapExpr trapExpr) {
        throw new AssertionError();
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        throw new AssertionError();
    }

    public void visit(BLangElvisExpr elvisExpr) {
        throw new AssertionError();
    }

    public void visit(BLangGroupExpr groupExpr) {
        throw new AssertionError();
    }

    public void visit(BLangLetExpression letExpr) {
        throw new AssertionError();
    }

    public void visit(BLangLetVariable letVariable) {
        throw new AssertionError();
    }

    public void visit(BLangListConstructorExpr listConstructorExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTupleLiteral tupleLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTypedescExpr accessExpr) {
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

    public void visit(BLangArrowFunction bLangArrowFunction) {
        throw new AssertionError();
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangIntRangeExpression intRangeExpression) {
        throw new AssertionError();
    }

    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        throw new AssertionError();
    }

    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
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

    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        throw new AssertionError();
    }

    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTypeTestExpr typeTestExpr) {
        throw new AssertionError();
    }

    public void visit(BLangIsLikeExpr typeTestExpr) {
        throw new AssertionError();
    }

    public void visit(BLangIgnoreExpr ignoreExpr) {
        throw new AssertionError();
    }

    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangQueryExpr queryExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTableMultiKeyExpr tableMultiKeyExpr) {
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

    public void visit(BLangStreamType streamType) {
        throw new AssertionError();
    }

    public void visit(BLangTableTypeNode tableType) {
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

    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
        throw new AssertionError();
    }

    public void visit(BLangObjectTypeNode objectTypeNode) {
        throw new AssertionError();
    }

    public void visit(BLangRecordTypeNode recordTypeNode) {
        throw new AssertionError();
    }

    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        throw new AssertionError();
    }

    public void visit(BLangTupleTypeNode tupleTypeNode) {
        throw new AssertionError();
    }

    public void visit(BLangErrorType errorType) {
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

    public void visit(BLangTupleAccessExpr arrayIndexAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTableAccessExpr tableKeyAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangXMLAccessExpr xmlAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangMapLiteral mapLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangStructLiteral structLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangChannelLiteral channelLiteral) {
        throw new AssertionError();
    }

    public void visit(BFunctionPointerInvocation bFunctionPointerInvocation) {
        throw new AssertionError();
    }

    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        throw new AssertionError();
    }

    public void visit(BLangJSONArrayLiteral jsonArrayLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangJSONAccessExpr jsonAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangLocalXMLNS xmlnsNode) {
        throw new AssertionError();
    }

    public void visit(BLangPackageXMLNS xmlnsNode) {
        throw new AssertionError();
    }

    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangStatementExpression bLangStatementExpression) {
        throw new AssertionError();
    }

    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine) {
        throw new AssertionError();
    }

    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter) {
        throw new AssertionError();
    }

    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation) {
        throw new AssertionError();
    }

    public void visit(BLangMarkDownDeprecationDocumentation bLangMarkDownDeprecationDocumentation) {
        throw new AssertionError();
    }

    public void visit(BLangMarkDownDeprecatedParametersDocumentation bLangMarkDownDeprecatedParametersDocumentation) {
        throw new AssertionError();
    }

    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {
        throw new AssertionError();
    }

    public void visit(BLangTupleVariable bLangTupleVariable) {
        throw new AssertionError();
    }

    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        throw new AssertionError();
    }

    public void visit(BLangRecordVariable bLangRecordVariable) {
        throw new AssertionError();
    }

    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        throw new AssertionError();
    }

    public void visit(BLangErrorVariable bLangErrorVariable) {
        throw new AssertionError();
    }

    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        throw new AssertionError();
    }

    public void visit(BLangMatchStaticBindingPatternClause bLangMatchStmtStaticBindingPatternClause) {
        throw new AssertionError();
    }

    public void visit(BLangMatchStructuredBindingPatternClause bLangMatchStmtStructuredBindingPatternClause) {
        throw new AssertionError();
    }

    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        throw new AssertionError();
    }

    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        throw new AssertionError();
    }

    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        throw new AssertionError();
    }

    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        throw new AssertionError();
    }

    public void visit(BLangRecordLiteral.BLangRecordKeyValueField recordKeyValue) {
        throw new AssertionError();
    }

    public void visit(BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOperatorField) {
        throw new AssertionError();
    }

    public void visit(BLangMarkdownReferenceDocumentation bLangMarkdownReferenceDocumentation) {
        throw new AssertionError();
    }

    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
        throw new AssertionError();
    }

    public void visit(BLangXMLElementFilter xmlElementFilter) {
        throw new AssertionError();
    }

    public void visit(BLangXMLElementAccess xmlElementAccess) {
        throw new AssertionError();
    }

    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        throw new AssertionError();
    }
}
