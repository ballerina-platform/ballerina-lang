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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.compiler.api.impl;

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
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorCauseBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorFieldBindingPatterns;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorMessageBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangFieldBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangListBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangMappingBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangNamedArgBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangRestBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangSimpleBindingPattern;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInferredTypedescDefaultNode;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAtomCharOrEscape;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAtomQuantifier;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCharSet;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCharacterClass;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReQuantifier;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReSequence;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRegExpTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangClientDeclarationStatement;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStreamType;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

/**
 * A default visitor with empty implementations for the visit methods, to be used as the base visitor in visitors
 * implemented.
 *
 * @since 2.0.0
 */
abstract class BaseVisitor extends BLangNodeVisitor {

    @Override
    public void visit(BLangPackage pkgNode) {
    }

    @Override
    public void visit(BLangTestablePackage testablePkgNode) {
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
    }

    @Override
    public void visit(BLangResourceFunction resourceFunction) {
    }

    @Override
    public void visit(BLangBlockFunctionBody blockFuncBody) {
    }

    @Override
    public void visit(BLangExprFunctionBody exprFuncBody) {
    }

    @Override
    public void visit(BLangExternalFunctionBody externFuncBody) {
    }

    @Override
    public void visit(BLangService serviceNode) {
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
    }

    @Override
    public void visit(BLangConstant constant) {
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
    }

    @Override
    public void visit(BLangTableKeySpecifier tableKeySpecifierNode) {
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint tableKeyTypeConstraint) {
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
    }

    @Override
    public void visit(BLangAssignment assignNode) {
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
    }

    @Override
    public void visit(BLangRetry retryNode) {
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
    }

    @Override
    public void visit(BLangRetrySpec retrySpec) {
    }

    @Override
    public void visit(BLangContinue continueNode) {
    }

    @Override
    public void visit(BLangBreak breakNode) {
    }

    @Override
    public void visit(BLangReturn returnNode) {
    }

    @Override
    public void visit(BLangPanic panicNode) {
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
    }

    @Override
    public void visit(BLangClientDeclarationStatement clientDeclarationStatement) {
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
    }

    @Override
    public void visit(BLangIf ifNode) {
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
    }

    @Override
    public void visit(BLangMappingBindingPattern mappingBindingPattern) {
    }

    @Override
    public void visit(BLangFieldBindingPattern fieldBindingPattern) {
    }

    @Override
    public void visit(BLangRestBindingPattern restBindingPattern) {
    }

    @Override
    public void visit(BLangWildCardBindingPattern wildCardBindingPattern) {
    }

    @Override
    public void visit(BLangErrorBindingPattern errorBindingPattern) {
    }

    @Override
    public void visit(BLangErrorMessageBindingPattern errorMessageBindingPattern) {
    }

    @Override
    public void visit(BLangErrorCauseBindingPattern errorCauseBindingPattern) {
    }

    @Override
    public void visit(BLangErrorFieldBindingPatterns errorFieldBindingPatterns) {
    }

    @Override
    public void visit(BLangSimpleBindingPattern simpleBindingPattern) {
    }

    @Override
    public void visit(BLangNamedArgBindingPattern namedArgBindingPattern) {
    }

    @Override
    public void visit(BLangMatchStatement matchStatementNode) {
    }

    @Override
    public void visit(BLangMappingMatchPattern mappingMatchPattern) {
    }

    @Override
    public void visit(BLangFieldMatchPattern fieldMatchPattern) {
    }

    @Override
    public void visit(BLangRestMatchPattern restMatchPattern) {
    }

    @Override
    public void visit(BLangListMatchPattern listMatchPattern) {
    }

    @Override
    public void visit(BLangErrorMatchPattern errorMatchPattern) {
    }

    @Override
    public void visit(BLangErrorMessageMatchPattern errorMessageMatchPattern) {
    }

    @Override
    public void visit(BLangErrorCauseMatchPattern errorCauseMatchPattern) {
    }

    @Override
    public void visit(BLangErrorFieldMatchPatterns errorFieldMatchPatterns) {
    }

    @Override
    public void visit(BLangSimpleMatchPattern simpleMatchPattern) {
    }

    @Override
    public void visit(BLangNamedArgMatchPattern namedArgMatchPattern) {
    }

    @Override
    public void visit(BLangMatchGuard matchGuard) {
    }

    @Override
    public void visit(BLangConstPattern constMatchPattern) {
    }

    @Override
    public void visit(BLangWildCardMatchPattern wildCardMatchPattern) {
    }

    @Override
    public void visit(BLangVarBindingPatternMatchPattern varBindingPattern) {
    }

    @Override
    public void visit(BLangCaptureBindingPattern captureBindingPattern) {
    }

    @Override
    public void visit(BLangListBindingPattern listBindingPattern) {
    }

    @Override
    public void visit(BLangForeach foreach) {
    }

    @Override
    public void visit(BLangDo doNode) {
    }

    @Override
    public void visit(BLangFail failNode) {
    }

    @Override
    public void visit(BLangFromClause fromClause) {
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
    }

    @Override
    public void visit(BLangLetClause letClause) {
    }

    @Override
    public void visit(BLangOnClause onClause) {
    }

    @Override
    public void visit(BLangOrderKey orderKeyClause) {
    }

    @Override
    public void visit(BLangOrderByClause orderByClause) {
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
    }

    @Override
    public void visit(BLangDoClause doClause) {
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause) {
    }

    @Override
    public void visit(BLangLimitClause limitClause) {
    }

    @Override
    public void visit(BLangMatchClause matchClause) {
    }

    @Override
    public void visit(BLangWhile whileNode) {
    }

    @Override
    public void visit(BLangLock lockNode) {
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
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
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess nsPrefixedFieldBasedAccess) {
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
    }

    @Override
    public void visit(BLangTypeInit connectorInitExpr) {
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
    }

    @Override
    public void visit(BLangLetExpression letExpr) {
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangListConstructorSpreadOpExpr spreadOpExpr) {
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
    }

    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral) {
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
    }

    @Override
    public void visit(BLangTransactionalExpr transactionalExpr) {
    }

    @Override
    public void visit(BLangCommitExpr commitExpr) {
    }

    @Override
    public void visit(BLangObjectConstructorExpression bLangObjectConstructorExpression) {
    }

    @Override
    public void visit(BLangInferredTypedescDefaultNode inferTypedescExpr) {
    }

    @Override
    public void visit(BLangValueType valueType) {
    }

    @Override
    public void visit(BLangArrayType arrayType) {
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
    }

    @Override
    public void visit(BLangStreamType streamType) {
    }

    @Override
    public void visit(BLangTableTypeNode tableType) {
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
    }

    @Override
    public void visit(BLangErrorType errorType) {
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr arrayIndexAccessExpr) {
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTableAccessExpr tableKeyAccessExpr) {
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlAccessExpr) {
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
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
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
    public void visit(BLangTupleVariable bLangTupleVariable) {
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValueField recordKeyValue) {
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKey recordKey) {
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOperatorField) {
    }

    @Override
    public void visit(BLangMarkdownReferenceDocumentation bLangMarkdownReferenceDocumentation) {
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
    }

    @Override
    public void visit(BLangXMLElementFilter xmlElementFilter) {
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
    }

    public abstract void visit(BLangInvocation.BLangResourceAccessInvocation resourceAccessInvocation);

    @Override
    public void visit(BLangRegExpTemplateLiteral regExpTemplateLiteral) {
    }

    @Override
    public void visit(BLangReSequence reSequence) {
    }

    @Override
    public void visit(BLangReAtomQuantifier reAtomQuantifier) {
    }

    @Override
    public void visit(BLangReAtomCharOrEscape reAtomCharOrEscape) {
    }

    @Override
    public void visit(BLangReQuantifier reQuantifier) {
    }

    @Override
    public void visit(BLangReCharacterClass reCharacterClass) {
    }

    @Override
    public void visit(BLangReCharSet reCharSet) {
    }
}
