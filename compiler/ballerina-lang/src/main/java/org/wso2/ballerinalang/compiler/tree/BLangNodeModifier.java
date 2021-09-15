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
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
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
 * The {@link BLangNodeModifier} transforms each {@link BLangNode} objects to another object of type T.
 * <p>
 * If you are looking for a {@link BLangNode} visitor that returns void, see {@link BLangNodeAnalyzer}.
 * <p>
 *
 * @param <T> the type of class that passed along with transform methods.
 * @param <R> the type of class that is returned by transform methods
 * @since 2.0.0
 */
public abstract class BLangNodeModifier<T, R> {

    public abstract R modifyNode(BLangNode node, T props);

    public abstract R modifyNodeEntry(BLangNodeEntry nodeEntry, T props);

    // Base Nodes

    public R modify(BLangAnnotation node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangAnnotationAttachment node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangBlockFunctionBody node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangClassDefinition node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangCompilationUnit node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangErrorVariable node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangErrorVariable.BLangErrorDetailEntry nodeEntry, T props) {
        return modifyNodeEntry(nodeEntry, props);
    }

    public R modify(BLangExprFunctionBody node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangExternalFunctionBody node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangFunction node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangIdentifier node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangImportPackage node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMarkdownDocumentation node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMarkdownReferenceDocumentation node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangPackage node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRecordVariable node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRecordVariable.BLangRecordVariableKeyValue nodeEntry, T props) {
        return modifyNodeEntry(nodeEntry, props);
    }

    public R modify(BLangResourceFunction node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRetrySpec node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangService node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangSimpleVariable node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTableKeySpecifier node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTableKeyTypeConstraint node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTestablePackage node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTupleVariable node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTypeDefinition node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLNS node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLNS.BLangLocalXMLNS node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLNS.BLangPackageXMLNS node, T props) {
        return modifyNode(node, props);
    }

    // Binding-patterns

    public R modify(BLangCaptureBindingPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangErrorBindingPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangErrorCauseBindingPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangErrorFieldBindingPatterns node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangErrorMessageBindingPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangFieldBindingPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangListBindingPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMappingBindingPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangNamedArgBindingPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRestBindingPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangSimpleBindingPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangWildCardBindingPattern node, T props) {
        return modifyNode(node, props);
    }

    // Clauses

    public R modify(BLangDoClause node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangFromClause node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangJoinClause node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangLetClause node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangLimitClause node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMatchClause node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangOnClause node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangOnConflictClause node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangOnFailClause node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangOrderByClause node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangOrderKey node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangSelectClause node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangWhereClause node, T props) {
        return modifyNode(node, props);
    }

    // Expressions

    public R modify(BLangAnnotAccessExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangArrowFunction node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangBinaryExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangCheckedExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangCheckPanickedExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangCommitExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangConstant node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangConstRef node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangDynamicArgExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangElvisExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangErrorConstructorExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangErrorVarRef node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangFieldBasedAccess node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangFieldBasedAccess.BLangStructFunctionVarRef node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangGroupExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangIgnoreExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangIndexBasedAccess node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangInferredTypedescDefaultNode node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangIntRangeExpression node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangInvocation node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangInvocation.BFunctionPointerInvocation node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangInvocation.BLangAttachedFunctionInvocation node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangInvocation.BLangActionInvocation node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangIsAssignableExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangIsLikeExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangLambdaFunction node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangLetExpression node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangListConstructorExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangLiteral node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMarkDownDeprecatedParametersDocumentation node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMarkDownDeprecationDocumentation node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMarkdownDocumentationLine node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMarkdownParameterDocumentation node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMarkdownReturnParameterDocumentation node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMatchExpression node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMatchExpression.BLangMatchExprPatternClause node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMatchGuard node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangNamedArgsExpression node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangObjectConstructorExpression node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangQueryAction node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangQueryExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRawTemplateLiteral node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRecordLiteral node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRecordLiteral.BLangRecordKeyValueField node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRecordLiteral.BLangRecordSpreadOperatorField node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRecordLiteral.BLangRecordKey node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRecordLiteral.BLangStructLiteral node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRecordLiteral.BLangMapLiteral node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRecordVarRef node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRestArgsExpression node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangServiceConstructorExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangSimpleVarRef node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangStatementExpression node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangStringTemplateLiteral node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTableConstructorExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTableMultiKeyExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTernaryExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTransactionalExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTrapExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTupleVarRef node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTypeConversionExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTypedescExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTypeInit node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTypeTestExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangUnaryExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangWaitExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangWaitForAllExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangWaitForAllExpr.BLangWaitKeyValue node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangWorkerFlushExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangWorkerReceive node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangWorkerSyncSendExpr node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLAttribute node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLAttributeAccess node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLCommentLiteral node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLElementAccess node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLElementFilter node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLElementLiteral node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLNavigationAccess node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLProcInsLiteral node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLQName node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLQuotedString node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLSequenceLiteral node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLTextLiteral node, T props) {
        return modifyNode(node, props);
    }

    // Match patterns

    public R modify(BLangConstPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangErrorCauseMatchPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangErrorFieldMatchPatterns node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangErrorMatchPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangErrorMessageMatchPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangFieldMatchPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangListMatchPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMappingMatchPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangNamedArgMatchPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRestMatchPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangSimpleMatchPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangVarBindingPatternMatchPattern node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangWildCardMatchPattern node, T props) {
        return modifyNode(node, props);
    }

    // Statements

    public R modify(BLangAssignment node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangBlockStmt node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangBreak node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangCompoundAssignment node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangContinue node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangDo node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangErrorDestructure node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangErrorVariableDef node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangExpressionStmt node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangFail node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangForeach node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangForkJoin node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangIf node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangLock node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangLock.BLangLockStmt node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangLock.BLangUnLockStmt node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMatch node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMatch.BLangMatchTypedBindingPatternClause node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMatch.BLangMatchStaticBindingPatternClause node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMatch.BLangMatchStructuredBindingPatternClause node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangMatchStatement node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangPanic node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRecordDestructure node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRecordVariableDef node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRetry node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRetryTransaction node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangReturn node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRollback node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangSimpleVariableDef node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTransaction node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTupleDestructure node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTupleVariableDef node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangWhile node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangWorkerSend node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangXMLNSStatement node, T props) {
        return modifyNode(node, props);
    }

    // Types

    public R modify(BLangArrayType node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangBuiltInRefTypeNode node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangConstrainedType node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangErrorType node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangFiniteTypeNode node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangFunctionTypeNode node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangIntersectionTypeNode node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangLetVariable nodeEntry, T props) {
        return modifyNodeEntry(nodeEntry, props);
    }

    public R modify(BLangObjectTypeNode node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangRecordTypeNode node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangStreamType node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTableTypeNode node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangTupleTypeNode node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangUnionTypeNode node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangUserDefinedType node, T props) {
        return modifyNode(node, props);
    }

    public R modify(BLangValueType node, T props) {
        return modifyNode(node, props);
    }
}
