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
 * The {@link BLangNodeTransformer} transforms each {@link BLangNode} objects to another object of type R.
 * <p>
 * If you are looking for a {@link BLangNode} visitor that returns void, see {@link BLangNodeAnalyzer}.
 * <p>
 *
 * @param <T> the type of data class that passed along with transform methods.
 * @param <R> the type of class that is returned by transform methods
 * @since 2.0.0
 */
public abstract class BLangNodeTransformer<T, R> {

    public abstract R transformNode(BLangNode node, T data);

    // Base Nodes

    public R transform(BLangAnnotation node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangAnnotationAttachment node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangBlockFunctionBody node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangClassDefinition node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangCompilationUnit node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangErrorVariable node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangErrorVariable.BLangErrorDetailEntry node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangExprFunctionBody node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangExternalFunctionBody node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangFunction node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangIdentifier node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangImportPackage node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangMarkdownDocumentation node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangMarkdownReferenceDocumentation node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangPackage node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRecordVariable node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRecordVariable.BLangRecordVariableKeyValue node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangResourceFunction node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRetrySpec node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangService node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangSimpleVariable node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTableKeySpecifier node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTableKeyTypeConstraint node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTestablePackage node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTupleVariable node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTypeDefinition node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangXMLNS node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangXMLNS.BLangLocalXMLNS node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangXMLNS.BLangPackageXMLNS node, T data) {
        return transformNode(node, data);
    }

    // Binding-patterns

    public R transform(BLangCaptureBindingPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangErrorBindingPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangErrorCauseBindingPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangErrorFieldBindingPatterns node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangErrorMessageBindingPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangFieldBindingPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangListBindingPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangMappingBindingPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangNamedArgBindingPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRestBindingPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangSimpleBindingPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangWildCardBindingPattern node, T data) {
        return transformNode(node, data);
    }

    // Clauses

    public R transform(BLangDoClause node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangFromClause node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangJoinClause node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangLetClause node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangLimitClause node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangMatchClause node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangOnClause node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangOnConflictClause node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangOnFailClause node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangOrderByClause node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangOrderKey node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangSelectClause node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangWhereClause node, T data) {
        return transformNode(node, data);
    }

    // Expressions

    public R transform(BLangAnnotAccessExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangArrowFunction node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangBinaryExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangCheckedExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangCheckPanickedExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangCommitExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangConstant node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangConstRef node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangDynamicArgExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangElvisExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangErrorConstructorExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangErrorVarRef node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangFieldBasedAccess node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangFieldBasedAccess.BLangStructFunctionVarRef node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangGroupExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangIgnoreExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangIndexBasedAccess node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangIndexBasedAccess.BLangArrayAccessExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangIndexBasedAccess.BLangMapAccessExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangIndexBasedAccess.BLangJSONAccessExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangIndexBasedAccess.BLangTableAccessExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangIndexBasedAccess.BLangStringAccessExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangIndexBasedAccess.BLangXMLAccessExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangIndexBasedAccess.BLangStructFieldAccessExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangIndexBasedAccess.BLangTupleAccessExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangInferredTypedescDefaultNode node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangInvocation node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangInvocation.BFunctionPointerInvocation node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangInvocation.BLangAttachedFunctionInvocation node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangInvocation.BLangActionInvocation node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangIsAssignableExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangIsLikeExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangLambdaFunction node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangLetExpression node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangListConstructorExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangListConstructorExpr.BLangListConstructorSpreadOpExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangLiteral node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangMarkDownDeprecatedParametersDocumentation node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangMarkDownDeprecationDocumentation node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangMarkdownDocumentationLine node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangMarkdownParameterDocumentation node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangMarkdownReturnParameterDocumentation node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangMatchGuard node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangNamedArgsExpression node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangObjectConstructorExpression node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangQueryAction node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangQueryExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRawTemplateLiteral node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRecordLiteral node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRecordLiteral.BLangRecordKeyValueField node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRecordLiteral.BLangRecordVarNameField node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRecordLiteral.BLangRecordSpreadOperatorField node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRecordLiteral.BLangRecordKey node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRecordLiteral.BLangStructLiteral node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRecordLiteral.BLangMapLiteral node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRecordVarRef node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRecordVarRef.BLangRecordVarRefKeyValue node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRestArgsExpression node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangServiceConstructorExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangSimpleVarRef node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangSimpleVarRef.BLangLocalVarRef node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangSimpleVarRef.BLangFieldVarRef node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangSimpleVarRef.BLangPackageVarRef node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangSimpleVarRef.BLangFunctionVarRef node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangSimpleVarRef.BLangTypeLoad node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangStatementExpression node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangStringTemplateLiteral node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRegExpTemplateLiteral node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangReSequence node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangReAtomQuantifier node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangReAtomCharOrEscape node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangReQuantifier node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangReCharacterClass node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangReCharSet node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangReCharSetRange node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangReAssertion node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangReCapturingGroups node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangReDisjunction node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangReFlagsOnOff node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangReFlagExpression node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTableConstructorExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTernaryExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTransactionalExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTrapExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTupleVarRef node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTypeConversionExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTypedescExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTypeInit node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTypeTestExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangUnaryExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangWaitExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangWaitForAllExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangWaitForAllExpr.BLangWaitKeyValue node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangWorkerFlushExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangWorkerReceive node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangWorkerSyncSendExpr node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangXMLAttribute node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangXMLCommentLiteral node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangXMLElementAccess node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangXMLElementFilter node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangXMLElementLiteral node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangXMLNavigationAccess node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangXMLProcInsLiteral node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangXMLQName node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangXMLQuotedString node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangXMLSequenceLiteral node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangXMLTextLiteral node, T data) {
        return transformNode(node, data);
    }

    // Match patterns

    public R transform(BLangConstPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangErrorCauseMatchPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangErrorFieldMatchPatterns node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangErrorMatchPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangErrorMessageMatchPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangFieldMatchPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangListMatchPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangMappingMatchPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangNamedArgMatchPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRestMatchPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangSimpleMatchPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangVarBindingPatternMatchPattern node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangWildCardMatchPattern node, T data) {
        return transformNode(node, data);
    }

    // Statements

    public R transform(BLangAssignment node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangBlockStmt node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangBreak node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangCompoundAssignment node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangContinue node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangDo node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangErrorDestructure node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangErrorVariableDef node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangExpressionStmt node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangFail node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangForeach node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangForkJoin node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangIf node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangLock node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangLock.BLangLockStmt node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangLock.BLangUnLockStmt node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangMatchStatement node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangPanic node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRecordDestructure node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRecordVariableDef node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRetry node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRetryTransaction node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangReturn node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRollback node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangSimpleVariableDef node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTransaction node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTupleDestructure node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTupleVariableDef node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangWhile node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangWorkerSend node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangXMLNSStatement node, T data) {
        return transformNode(node, data);
    }

    // Types

    public R transform(BLangArrayType node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangBuiltInRefTypeNode node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangConstrainedType node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangErrorType node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangFiniteTypeNode node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangFunctionTypeNode node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangIntersectionTypeNode node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangLetVariable node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangObjectTypeNode node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangRecordTypeNode node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangStreamType node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTableTypeNode node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangTupleTypeNode node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangUnionTypeNode node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangUserDefinedType node, T data) {
        return transformNode(node, data);
    }

    public R transform(BLangValueType node, T data) {
        return transformNode(node, data);
    }
}
