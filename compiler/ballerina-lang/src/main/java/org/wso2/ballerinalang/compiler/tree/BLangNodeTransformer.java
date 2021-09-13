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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchGuard;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangObjectConstructorExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableMultiKeyExpr;
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
 * TODO: Fix me.
 *
 * @since 2.0.0
 */
public abstract class BLangNodeTransformer<T, R> {

    public abstract R transformNode(BLangNode node, T props);

    // Base Nodes

    public R transform(BLangAnnotation node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangAnnotationAttachment node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangBlockFunctionBody node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangClassDefinition node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangCompilationUnit node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangErrorVariable node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangExprFunctionBody node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangExternalFunctionBody node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangFunction node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangIdentifier node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangImportPackage node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangMarkdownDocumentation node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangMarkdownReferenceDocumentation node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangPackage node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRecordVariable node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangResourceFunction node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRetrySpec node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangService node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangSimpleVariable node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTableKeySpecifier node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTableKeyTypeConstraint node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTestablePackage node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTupleVariable node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTypeDefinition node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLNS node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLNS.BLangLocalXMLNS node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLNS.BLangPackageXMLNS node, T props) {
        return transformNode(node, props);
    }

    // Binding-patterns

    public R transform(BLangCaptureBindingPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangErrorBindingPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangErrorCauseBindingPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangErrorFieldBindingPatterns node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangErrorMessageBindingPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangFieldBindingPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangListBindingPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangMappingBindingPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangNamedArgBindingPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRestBindingPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangSimpleBindingPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangWildCardBindingPattern node, T props) {
        return transformNode(node, props);
    }

    // Clauses

    public R transform(BLangDoClause node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangFromClause node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangJoinClause node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangLetClause node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangLimitClause node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangMatchClause node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangOnClause node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangOnConflictClause node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangOnFailClause node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangOrderByClause node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangOrderKey node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangSelectClause node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangWhereClause node, T props) {
        return transformNode(node, props);
    }

    // Expressions

    public R transform(BLangAnnotAccessExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangArrowFunction node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangBinaryExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangCheckedExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangCheckPanickedExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangCommitExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangConstant node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangConstRef node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangDynamicArgExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangElvisExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangErrorConstructorExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangErrorVarRef node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangFieldBasedAccess node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangFieldBasedAccess.BLangStructFunctionVarRef node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangGroupExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangIgnoreExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangIndexBasedAccess node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangInferredTypedescDefaultNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangIntRangeExpression node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangInvocation node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangInvocation.BFunctionPointerInvocation node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangInvocation.BLangAttachedFunctionInvocation node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangInvocation.BLangActionInvocation node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangIsAssignableExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangIsLikeExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangLambdaFunction node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangLetExpression node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangListConstructorExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangLiteral node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangMarkDownDeprecatedParametersDocumentation node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangMarkDownDeprecationDocumentation node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangMarkdownDocumentationLine node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangMarkdownParameterDocumentation node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangMarkdownReturnParameterDocumentation node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangMatchExpression node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangMatchExpression.BLangMatchExprPatternClause node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangMatchGuard node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangNamedArgsExpression node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangObjectConstructorExpression node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangQueryAction node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangQueryExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRawTemplateLiteral node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRecordLiteral node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRecordLiteral.BLangRecordKeyValueField node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRecordLiteral.BLangRecordSpreadOperatorField node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRecordLiteral.BLangRecordKey node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRecordLiteral.BLangStructLiteral node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRecordLiteral.BLangMapLiteral node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRecordVarRef node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRestArgsExpression node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangServiceConstructorExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangSimpleVarRef node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangStatementExpression node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangStringTemplateLiteral node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTableConstructorExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTableMultiKeyExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTernaryExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTransactionalExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTrapExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTupleVarRef node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTypeConversionExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTypedescExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTypeInit node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTypeTestExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangUnaryExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangWaitExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangWaitForAllExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangWaitForAllExpr.BLangWaitKeyValue node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangWorkerFlushExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangWorkerReceive node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangWorkerSyncSendExpr node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLAttribute node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLAttributeAccess node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLCommentLiteral node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLElementAccess node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLElementFilter node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLElementLiteral node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLNavigationAccess node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLProcInsLiteral node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLQName node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLQuotedString node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLSequenceLiteral node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangXMLTextLiteral node, T props) {
        return transformNode(node, props);
    }

    // Match patterns

    public R transform(BLangConstPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangErrorCauseMatchPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangErrorFieldMatchPatterns node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangErrorMatchPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangErrorMessageMatchPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangFieldMatchPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangListMatchPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangMappingMatchPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangNamedArgMatchPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRestMatchPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangSimpleMatchPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangVarBindingPatternMatchPattern node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangWildCardMatchPattern node, T props) {
        return transformNode(node, props);
    }

    // Types

    public R transform(BLangArrayType node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangBuiltInRefTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangConstrainedType node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangErrorType node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangFiniteTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangFunctionTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangIntersectionTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangObjectTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangRecordTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangStreamType node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTableTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangTupleTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangUnionTypeNode node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangUserDefinedType node, T props) {
        return transformNode(node, props);
    }

    public R transform(BLangValueType node, T props) {
        return transformNode(node, props);
    }
}
