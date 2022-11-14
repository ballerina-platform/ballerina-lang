/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDynamicArgExpr;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangObjectConstructorExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAssertion;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAtomCharOrEscape;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAtomQuantifier;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCapturingGroups;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCharSet;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCharSetRange;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCharacterClass;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReDisjunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReFlagExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReFlagsOnOff;
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
 * The {@link BLangNodeAnalyzer} is a {@link BLangNode} visitor.
 * <p>
 * If you are looking for a {@link BLangNode} visitor that returns object with a type, see {@link BLangNodeTransformer}.
 * <p>
 *
 * @param <T> the type of data class that passed along with visit methods.
 * @since 2.0.0
 */
public abstract class BLangNodeAnalyzer<T> {

    // Base Nodes

    public abstract void visit(BLangAnnotation node, T data);

    public abstract void visit(BLangAnnotationAttachment node, T data);

    public abstract void visit(BLangBlockFunctionBody node, T data);

    public abstract void visit(BLangClassDefinition node, T data);

    public abstract void visit(BLangCompilationUnit node, T data);

    public abstract void visit(BLangErrorVariable node, T data);

    public abstract void visit(BLangErrorVariable.BLangErrorDetailEntry node, T data);

    public abstract void visit(BLangExprFunctionBody node, T data);

    public abstract void visit(BLangExternalFunctionBody node, T data);

    public abstract void visit(BLangFunction node, T data);

    public abstract void visit(BLangIdentifier node, T data);

    public abstract void visit(BLangImportPackage node, T data);

    public abstract void visit(BLangMarkdownDocumentation node, T data);

    public abstract void visit(BLangMarkdownReferenceDocumentation node, T data);

    public abstract void visit(BLangPackage node, T data);

    public abstract void visit(BLangRecordVariable node, T data);

    public abstract void visit(BLangRecordVariable.BLangRecordVariableKeyValue node, T data);

    public abstract void visit(BLangResourceFunction node, T data);

    public abstract void visit(BLangRetrySpec node, T data);

    public abstract void visit(BLangService node, T data);

    public abstract void visit(BLangSimpleVariable node, T data);

    public abstract void visit(BLangTableKeySpecifier node, T data);

    public abstract void visit(BLangTableKeyTypeConstraint node, T data);

    public abstract void visit(BLangTestablePackage node, T data);

    public abstract void visit(BLangTupleVariable node, T data);

    public abstract void visit(BLangTypeDefinition node, T data);

    public abstract void visit(BLangXMLNS node, T data);

    public abstract void visit(BLangXMLNS.BLangLocalXMLNS node, T data);

    public abstract void visit(BLangXMLNS.BLangPackageXMLNS node, T data);

    // Binding-patterns

    public abstract void visit(BLangCaptureBindingPattern node, T data);

    public abstract void visit(BLangErrorBindingPattern node, T data);

    public abstract void visit(BLangErrorCauseBindingPattern node, T data);

    public abstract void visit(BLangErrorFieldBindingPatterns node, T data);

    public abstract void visit(BLangErrorMessageBindingPattern node, T data);

    public abstract void visit(BLangFieldBindingPattern node, T data);

    public abstract void visit(BLangListBindingPattern node, T data);

    public abstract void visit(BLangMappingBindingPattern node, T data);

    public abstract void visit(BLangNamedArgBindingPattern node, T data);

    public abstract void visit(BLangRestBindingPattern node, T data);

    public abstract void visit(BLangSimpleBindingPattern node, T data);

    public abstract void visit(BLangWildCardBindingPattern node, T data);

    // Clauses

    public abstract void visit(BLangDoClause node, T data);

    public abstract void visit(BLangFromClause node, T data);

    public abstract void visit(BLangJoinClause node, T data);

    public abstract void visit(BLangLetClause node, T data);

    public abstract void visit(BLangLimitClause node, T data);

    public abstract void visit(BLangMatchClause node, T data);

    public abstract void visit(BLangOnClause node, T data);

    public abstract void visit(BLangOnConflictClause node, T data);

    public abstract void visit(BLangOnFailClause node, T data);

    public abstract void visit(BLangOrderByClause node, T data);

    public abstract void visit(BLangOrderKey node, T data);

    public abstract void visit(BLangSelectClause node, T data);

    public abstract void visit(BLangWhereClause node, T data);

    // Expressions

    public abstract void visit(BLangAnnotAccessExpr node, T data);

    public abstract void visit(BLangArrowFunction node, T data);

    public abstract void visit(BLangBinaryExpr node, T data);

    public abstract void visit(BLangCheckedExpr node, T data);

    public abstract void visit(BLangCheckPanickedExpr node, T data);

    public abstract void visit(BLangCommitExpr node, T data);

    public abstract void visit(BLangConstant node, T data);

    public abstract void visit(BLangConstRef node, T data);

    public abstract void visit(BLangDynamicArgExpr node, T data);

    public abstract void visit(BLangElvisExpr node, T data);

    public abstract void visit(BLangErrorConstructorExpr node, T data);

    public abstract void visit(BLangErrorVarRef node, T data);

    public abstract void visit(BLangFieldBasedAccess node, T data);

    public abstract void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef node, T data);

    public abstract void visit(BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess node, T data);

    public abstract void visit(BLangGroupExpr node, T data);

    public abstract void visit(BLangIgnoreExpr node, T data);

    public abstract void visit(BLangIndexBasedAccess node, T data);

    public abstract void visit(BLangIndexBasedAccess.BLangArrayAccessExpr node, T data);

    public abstract void visit(BLangIndexBasedAccess.BLangMapAccessExpr node, T data);

    public abstract void visit(BLangIndexBasedAccess.BLangJSONAccessExpr node, T data);

    public abstract void visit(BLangIndexBasedAccess.BLangTableAccessExpr node, T data);

    public abstract void visit(BLangIndexBasedAccess.BLangStringAccessExpr node, T data);

    public abstract void visit(BLangIndexBasedAccess.BLangXMLAccessExpr node, T data);

    public abstract void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr node, T data);

    public abstract void visit(BLangIndexBasedAccess.BLangTupleAccessExpr node, T data);

    public abstract void visit(BLangInferredTypedescDefaultNode node, T data);

    public abstract void visit(BLangInvocation node, T data);

    public abstract void visit(BLangInvocation.BFunctionPointerInvocation node, T data);

    public abstract void visit(BLangInvocation.BLangAttachedFunctionInvocation node, T data);

    public abstract void visit(BLangInvocation.BLangActionInvocation node, T data);
    
    public abstract void visit(BLangInvocation.BLangResourceAccessInvocation node, T data);

    public abstract void visit(BLangIsAssignableExpr node, T data);

    public abstract void visit(BLangIsLikeExpr node, T data);

    public abstract void visit(BLangLambdaFunction node, T data);

    public abstract void visit(BLangLetExpression node, T data);

    public abstract void visit(BLangListConstructorExpr node, T data);

    public abstract void visit(BLangListConstructorExpr.BLangListConstructorSpreadOpExpr node, T data);

    public abstract void visit(BLangLiteral node, T data);

    public abstract void visit(BLangMarkDownDeprecatedParametersDocumentation node, T data);

    public abstract void visit(BLangMarkDownDeprecationDocumentation node, T data);

    public abstract void visit(BLangMarkdownDocumentationLine node, T data);

    public abstract void visit(BLangMarkdownParameterDocumentation node, T data);

    public abstract void visit(BLangMarkdownReturnParameterDocumentation node, T data);

    public abstract void visit(BLangMatchGuard node, T data);

    public abstract void visit(BLangNamedArgsExpression node, T data);

    public abstract void visit(BLangObjectConstructorExpression node, T data);

    public abstract void visit(BLangQueryAction node, T data);

    public abstract void visit(BLangQueryExpr node, T data);

    public abstract void visit(BLangRawTemplateLiteral node, T data);

    public abstract void visit(BLangRecordLiteral node, T data);

    public abstract void visit(BLangRecordLiteral.BLangRecordKeyValueField node, T data);

    public abstract void visit(BLangRecordLiteral.BLangRecordVarNameField node, T data);

    public abstract void visit(BLangRecordLiteral.BLangRecordSpreadOperatorField node, T data);

    public abstract void visit(BLangRecordLiteral.BLangRecordKey node, T data);

    public abstract void visit(BLangRecordLiteral.BLangStructLiteral node, T data);

    public abstract void visit(BLangRecordLiteral.BLangMapLiteral node, T data);

    public abstract void visit(BLangRecordVarRef node, T data);

    public abstract void visit(BLangRecordVarRef.BLangRecordVarRefKeyValue node, T data);

    public abstract void visit(BLangRestArgsExpression node, T data);

    public abstract void visit(BLangServiceConstructorExpr node, T data);

    public abstract void visit(BLangSimpleVarRef node, T data);

    public abstract void visit(BLangSimpleVarRef.BLangLocalVarRef node, T data);

    public abstract void visit(BLangSimpleVarRef.BLangFieldVarRef node, T data);

    public abstract void visit(BLangSimpleVarRef.BLangPackageVarRef node, T data);

    public abstract void visit(BLangSimpleVarRef.BLangFunctionVarRef node, T data);

    public abstract void visit(BLangSimpleVarRef.BLangTypeLoad node, T data);

    public abstract void visit(BLangStatementExpression node, T data);

    public abstract void visit(BLangStringTemplateLiteral node, T data);

    public abstract void visit(BLangRegExpTemplateLiteral node, T data);

    public abstract void visit(BLangReSequence node, T data);

    public abstract void visit(BLangReAtomQuantifier node, T data);

    public abstract void visit(BLangReAtomCharOrEscape node, T data);

    public abstract void visit(BLangReQuantifier node, T data);

    public abstract void visit(BLangReCharacterClass node, T data);

    public abstract void visit(BLangReCharSet node, T data);

    public abstract void visit(BLangReCharSetRange node, T data);

    public abstract void visit(BLangReAssertion node, T data);

    public abstract void visit(BLangReCapturingGroups node, T data);

    public abstract void visit(BLangReDisjunction node, T data);

    public abstract void visit(BLangReFlagsOnOff node, T data);

    public abstract void visit(BLangReFlagExpression node, T data);

    public abstract void visit(BLangTableConstructorExpr node, T data);

    public abstract void visit(BLangTernaryExpr node, T data);

    public abstract void visit(BLangTransactionalExpr node, T data);

    public abstract void visit(BLangTrapExpr node, T data);

    public abstract void visit(BLangTupleVarRef node, T data);

    public abstract void visit(BLangTypeConversionExpr node, T data);

    public abstract void visit(BLangTypedescExpr node, T data);

    public abstract void visit(BLangTypeInit node, T data);

    public abstract void visit(BLangTypeTestExpr node, T data);

    public abstract void visit(BLangUnaryExpr node, T data);

    public abstract void visit(BLangWaitExpr node, T data);

    public abstract void visit(BLangWaitForAllExpr node, T data);

    public abstract void visit(BLangWaitForAllExpr.BLangWaitKeyValue node, T data);

    public abstract void visit(BLangWorkerFlushExpr node, T data);

    public abstract void visit(BLangWorkerReceive node, T data);

    public abstract void visit(BLangWorkerSyncSendExpr node, T data);

    public abstract void visit(BLangXMLAttribute node, T data);

    public abstract void visit(BLangXMLCommentLiteral node, T data);

    public abstract void visit(BLangXMLElementAccess node, T data);

    public abstract void visit(BLangXMLElementFilter node, T data);

    public abstract void visit(BLangXMLElementLiteral node, T data);

    public abstract void visit(BLangXMLNavigationAccess node, T data);

    public abstract void visit(BLangXMLProcInsLiteral node, T data);

    public abstract void visit(BLangXMLQName node, T data);

    public abstract void visit(BLangXMLQuotedString node, T data);

    public abstract void visit(BLangXMLSequenceLiteral node, T data);

    public abstract void visit(BLangXMLTextLiteral node, T data);

    // Match patterns

    public abstract void visit(BLangConstPattern node, T data);

    public abstract void visit(BLangErrorCauseMatchPattern node, T data);

    public abstract void visit(BLangErrorFieldMatchPatterns node, T data);

    public abstract void visit(BLangErrorMatchPattern node, T data);

    public abstract void visit(BLangErrorMessageMatchPattern node, T data);

    public abstract void visit(BLangFieldMatchPattern node, T data);

    public abstract void visit(BLangListMatchPattern node, T data);

    public abstract void visit(BLangMappingMatchPattern node, T data);

    public abstract void visit(BLangNamedArgMatchPattern node, T data);

    public abstract void visit(BLangRestMatchPattern node, T data);

    public abstract void visit(BLangSimpleMatchPattern node, T data);

    public abstract void visit(BLangVarBindingPatternMatchPattern node, T data);

    public abstract void visit(BLangWildCardMatchPattern node, T data);

    // Statements

    public abstract void visit(BLangAssignment node, T data);

    public abstract void visit(BLangBlockStmt node, T data);

    public abstract void visit(BLangBreak node, T data);

    public abstract void visit(BLangCompoundAssignment node, T data);

    public abstract void visit(BLangContinue node, T data);

    public abstract void visit(BLangDo node, T data);

    public abstract void visit(BLangErrorDestructure node, T data);

    public abstract void visit(BLangErrorVariableDef node, T data);

    public abstract void visit(BLangExpressionStmt node, T data);

    public abstract void visit(BLangFail node, T data);

    public abstract void visit(BLangForeach node, T data);

    public abstract void visit(BLangForkJoin node, T data);

    public abstract void visit(BLangIf node, T data);

    public abstract void visit(BLangLock node, T data);

    public abstract void visit(BLangLock.BLangLockStmt node, T data);

    public abstract void visit(BLangLock.BLangUnLockStmt node, T data);

    public abstract void visit(BLangMatchStatement node, T data);

    public abstract void visit(BLangPanic node, T data);

    public abstract void visit(BLangRecordDestructure node, T data);

    public abstract void visit(BLangRecordVariableDef node, T data);

    public abstract void visit(BLangRetry node, T data);

    public abstract void visit(BLangRetryTransaction node, T data);

    public abstract void visit(BLangReturn node, T data);

    public abstract void visit(BLangRollback node, T data);

    public abstract void visit(BLangSimpleVariableDef node, T data);

    public abstract void visit(BLangTransaction node, T data);

    public abstract void visit(BLangTupleDestructure node, T data);

    public abstract void visit(BLangTupleVariableDef node, T data);

    public abstract void visit(BLangWhile node, T data);

    public abstract void visit(BLangWorkerSend node, T data);

    public abstract void visit(BLangXMLNSStatement node, T data);

    // Types

    public abstract void visit(BLangArrayType node, T data);

    public abstract void visit(BLangBuiltInRefTypeNode node, T data);

    public abstract void visit(BLangConstrainedType node, T data);

    public abstract void visit(BLangErrorType node, T data);

    public abstract void visit(BLangFiniteTypeNode node, T data);

    public abstract void visit(BLangFunctionTypeNode node, T data);

    public abstract void visit(BLangIntersectionTypeNode node, T data);

    public abstract void visit(BLangLetVariable node, T data);

    public abstract void visit(BLangObjectTypeNode node, T data);

    public abstract void visit(BLangRecordTypeNode node, T data);

    public abstract void visit(BLangStreamType node, T data);

    public abstract void visit(BLangTableTypeNode node, T data);

    public abstract void visit(BLangTupleTypeNode node, T data);

    public abstract void visit(BLangUnionTypeNode node, T data);

    public abstract void visit(BLangUserDefinedType node, T data);

    public abstract void visit(BLangValueType node, T data);
}
