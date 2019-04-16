/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// This is a generated file. Not intended for manual editing.
package io.ballerina.plugins.idea.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import io.ballerina.plugins.idea.psi.impl.BallerinaTopLevelDefinition;

public class BallerinaVisitor extends PsiElementVisitor {

  public void visitAbortStatement(@NotNull BallerinaAbortStatement o) {
    visitPsiElement(o);
  }

  public void visitAbortedClause(@NotNull BallerinaAbortedClause o) {
    visitPsiElement(o);
  }

  public void visitActionInvocation(@NotNull BallerinaActionInvocation o) {
    visitPsiElement(o);
  }

  public void visitActionInvocationExpression(@NotNull BallerinaActionInvocationExpression o) {
    visitExpression(o);
  }

  public void visitAlias(@NotNull BallerinaAlias o) {
    visitPsiElement(o);
  }

  public void visitAnnotationAttachment(@NotNull BallerinaAnnotationAttachment o) {
    visitPsiElement(o);
  }

  public void visitAnnotationDefinition(@NotNull BallerinaAnnotationDefinition o) {
    visitTopLevelDefinition(o);
  }

  public void visitAnyDataTypeName(@NotNull BallerinaAnyDataTypeName o) {
    visitPsiElement(o);
  }

  public void visitAnyIdentifierName(@NotNull BallerinaAnyIdentifierName o) {
    visitPsiElement(o);
  }

  public void visitAnyTypeName(@NotNull BallerinaAnyTypeName o) {
    visitPsiElement(o);
  }

  public void visitArrayLiteral(@NotNull BallerinaArrayLiteral o) {
    visitPsiElement(o);
  }

  public void visitArrayLiteralExpression(@NotNull BallerinaArrayLiteralExpression o) {
    visitExpression(o);
  }

  public void visitArrayTypeName(@NotNull BallerinaArrayTypeName o) {
    visitTypeName(o);
  }

  public void visitArrowFunction(@NotNull BallerinaArrowFunction o) {
    visitPsiElement(o);
  }

  public void visitArrowFunctionExpression(@NotNull BallerinaArrowFunctionExpression o) {
    visitExpression(o);
  }

  public void visitArrowParam(@NotNull BallerinaArrowParam o) {
    visitPsiElement(o);
  }

  public void visitAssignmentStatement(@NotNull BallerinaAssignmentStatement o) {
    visitPsiElement(o);
  }

  public void visitAttachedObject(@NotNull BallerinaAttachedObject o) {
    visitPsiElement(o);
  }

  public void visitAttachmentPoint(@NotNull BallerinaAttachmentPoint o) {
    visitPsiElement(o);
  }

  public void visitAttribute(@NotNull BallerinaAttribute o) {
    visitPsiElement(o);
  }

  public void visitBinaryAddSubExpression(@NotNull BallerinaBinaryAddSubExpression o) {
    visitBinaryExpression(o);
  }

  public void visitBinaryAndExpression(@NotNull BallerinaBinaryAndExpression o) {
    visitBinaryExpression(o);
  }

  public void visitBinaryCompareExpression(@NotNull BallerinaBinaryCompareExpression o) {
    visitBinaryExpression(o);
  }

  public void visitBinaryDivMulModExpression(@NotNull BallerinaBinaryDivMulModExpression o) {
    visitBinaryExpression(o);
  }

  public void visitBinaryEqualExpression(@NotNull BallerinaBinaryEqualExpression o) {
    visitBinaryExpression(o);
  }

  public void visitBinaryExpression(@NotNull BallerinaBinaryExpression o) {
    visitExpression(o);
  }

  public void visitBinaryOrExpression(@NotNull BallerinaBinaryOrExpression o) {
    visitBinaryExpression(o);
  }

  public void visitBinaryRefEqualExpression(@NotNull BallerinaBinaryRefEqualExpression o) {
    visitExpression(o);
  }

  public void visitBindingPattern(@NotNull BallerinaBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitBindingRefPattern(@NotNull BallerinaBindingRefPattern o) {
    visitPsiElement(o);
  }

  public void visitBitwiseExpression(@NotNull BallerinaBitwiseExpression o) {
    visitExpression(o);
  }

  public void visitBitwiseShiftExpression(@NotNull BallerinaBitwiseShiftExpression o) {
    visitExpression(o);
  }

  public void visitBlobLiteral(@NotNull BallerinaBlobLiteral o) {
    visitPsiElement(o);
  }

  public void visitBlock(@NotNull BallerinaBlock o) {
    visitPsiElement(o);
  }

  public void visitBracedOrTupleExpression(@NotNull BallerinaBracedOrTupleExpression o) {
    visitExpression(o);
  }

  public void visitBreakStatement(@NotNull BallerinaBreakStatement o) {
    visitPsiElement(o);
  }

  public void visitBuiltInReferenceTypeName(@NotNull BallerinaBuiltInReferenceTypeName o) {
    visitPsiElement(o);
  }

  public void visitCallableUnitBody(@NotNull BallerinaCallableUnitBody o) {
    visitPsiElement(o);
  }

  public void visitCallableUnitSignature(@NotNull BallerinaCallableUnitSignature o) {
    visitPsiElement(o);
  }

  public void visitCatchClause(@NotNull BallerinaCatchClause o) {
    visitPsiElement(o);
  }

  public void visitCatchClauses(@NotNull BallerinaCatchClauses o) {
    visitPsiElement(o);
  }

  public void visitCheckedExpression(@NotNull BallerinaCheckedExpression o) {
    visitExpression(o);
  }

  public void visitCloseTag(@NotNull BallerinaCloseTag o) {
    visitPsiElement(o);
  }

  public void visitComment(@NotNull BallerinaComment o) {
    visitPsiElement(o);
  }

  public void visitCommittedAbortedClauses(@NotNull BallerinaCommittedAbortedClauses o) {
    visitPsiElement(o);
  }

  public void visitCommittedClause(@NotNull BallerinaCommittedClause o) {
    visitPsiElement(o);
  }

  public void visitCompletePackageName(@NotNull BallerinaCompletePackageName o) {
    visitPsiElement(o);
  }

  public void visitCompoundAssignmentStatement(@NotNull BallerinaCompoundAssignmentStatement o) {
    visitPsiElement(o);
  }

  public void visitCompoundOperator(@NotNull BallerinaCompoundOperator o) {
    visitPsiElement(o);
  }

  public void visitConstantDefinition(@NotNull BallerinaConstantDefinition o) {
    visitPsiElement(o);
  }

  public void visitContent(@NotNull BallerinaContent o) {
    visitPsiElement(o);
  }

  public void visitContinueStatement(@NotNull BallerinaContinueStatement o) {
    visitPsiElement(o);
  }

  public void visitDefaultableParameter(@NotNull BallerinaDefaultableParameter o) {
    visitPsiElement(o);
  }

  public void visitDefinition(@NotNull BallerinaDefinition o) {
    visitPsiElement(o);
  }

  public void visitElement(@NotNull BallerinaElement o) {
    visitPsiElement(o);
  }

  public void visitElseClause(@NotNull BallerinaElseClause o) {
    visitPsiElement(o);
  }

  public void visitElseIfClause(@NotNull BallerinaElseIfClause o) {
    visitPsiElement(o);
  }

  public void visitElvisExpression(@NotNull BallerinaElvisExpression o) {
    visitExpression(o);
  }

  public void visitEmptyTag(@NotNull BallerinaEmptyTag o) {
    visitPsiElement(o);
  }

  public void visitEmptyTupleLiteral(@NotNull BallerinaEmptyTupleLiteral o) {
    visitPsiElement(o);
  }

  public void visitEntryBindingPattern(@NotNull BallerinaEntryBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitEntryRefBindingPattern(@NotNull BallerinaEntryRefBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitErrorBindingPattern(@NotNull BallerinaErrorBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitErrorConstructorExpression(@NotNull BallerinaErrorConstructorExpression o) {
    visitExpression(o);
  }

  public void visitErrorDestructuringStatement(@NotNull BallerinaErrorDestructuringStatement o) {
    visitPsiElement(o);
  }

  public void visitErrorRefBindingPattern(@NotNull BallerinaErrorRefBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitErrorTypeName(@NotNull BallerinaErrorTypeName o) {
    visitPsiElement(o);
  }

  public void visitExpression(@NotNull BallerinaExpression o) {
    visitPsiElement(o);
  }

  public void visitExpressionList(@NotNull BallerinaExpressionList o) {
    visitPsiElement(o);
  }

  public void visitExpressionStmt(@NotNull BallerinaExpressionStmt o) {
    visitPsiElement(o);
  }

  public void visitField(@NotNull BallerinaField o) {
    visitPsiElement(o);
  }

  public void visitFieldBindingPattern(@NotNull BallerinaFieldBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitFieldDefinition(@NotNull BallerinaFieldDefinition o) {
    visitPsiElement(o);
  }

  public void visitFieldRefBindingPattern(@NotNull BallerinaFieldRefBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitFieldVariableReference(@NotNull BallerinaFieldVariableReference o) {
    visitVariableReference(o);
  }

  public void visitFinallyClause(@NotNull BallerinaFinallyClause o) {
    visitPsiElement(o);
  }

  public void visitFiniteType(@NotNull BallerinaFiniteType o) {
    visitPsiElement(o);
  }

  public void visitFiniteTypeUnit(@NotNull BallerinaFiniteTypeUnit o) {
    visitPsiElement(o);
  }

  public void visitFloatingPointLiteral(@NotNull BallerinaFloatingPointLiteral o) {
    visitPsiElement(o);
  }

  public void visitFlushWorker(@NotNull BallerinaFlushWorker o) {
    visitPsiElement(o);
  }

  public void visitFlushWorkerExpression(@NotNull BallerinaFlushWorkerExpression o) {
    visitExpression(o);
  }

  public void visitForeachStatement(@NotNull BallerinaForeachStatement o) {
    visitPsiElement(o);
  }

  public void visitForeverStatement(@NotNull BallerinaForeverStatement o) {
    visitPsiElement(o);
  }

  public void visitForeverStatementBody(@NotNull BallerinaForeverStatementBody o) {
    visitPsiElement(o);
  }

  public void visitForkJoinStatement(@NotNull BallerinaForkJoinStatement o) {
    visitPsiElement(o);
  }

  public void visitFormalParameterList(@NotNull BallerinaFormalParameterList o) {
    visitPsiElement(o);
  }

  public void visitFunctionDefinition(@NotNull BallerinaFunctionDefinition o) {
    visitTopLevelDefinition(o);
  }

  public void visitFunctionInvocation(@NotNull BallerinaFunctionInvocation o) {
    visitPsiElement(o);
  }

  public void visitFunctionInvocationReference(@NotNull BallerinaFunctionInvocationReference o) {
    visitVariableReference(o);
  }

  public void visitFunctionTypeName(@NotNull BallerinaFunctionTypeName o) {
    visitPsiElement(o);
  }

  public void visitFutureTypeName(@NotNull BallerinaFutureTypeName o) {
    visitPsiElement(o);
  }

  public void visitGlobalVariableDefinition(@NotNull BallerinaGlobalVariableDefinition o) {
    visitPsiElement(o);
  }

  public void visitGroupByClause(@NotNull BallerinaGroupByClause o) {
    visitPsiElement(o);
  }

  public void visitGroupTypeName(@NotNull BallerinaGroupTypeName o) {
    visitTypeName(o);
  }

  public void visitHavingClause(@NotNull BallerinaHavingClause o) {
    visitPsiElement(o);
  }

  public void visitIfClause(@NotNull BallerinaIfClause o) {
    visitPsiElement(o);
  }

  public void visitIfElseStatement(@NotNull BallerinaIfElseStatement o) {
    visitPsiElement(o);
  }

  public void visitImportDeclaration(@NotNull BallerinaImportDeclaration o) {
    visitPsiElement(o);
  }

  public void visitIndex(@NotNull BallerinaIndex o) {
    visitPsiElement(o);
  }

  public void visitIntRangeExpression(@NotNull BallerinaIntRangeExpression o) {
    visitPsiElement(o);
  }

  public void visitIntegerLiteral(@NotNull BallerinaIntegerLiteral o) {
    visitPsiElement(o);
  }

  public void visitIntegerRangeExpression(@NotNull BallerinaIntegerRangeExpression o) {
    visitExpression(o);
  }

  public void visitInvocation(@NotNull BallerinaInvocation o) {
    visitPsiElement(o);
  }

  public void visitInvocationArg(@NotNull BallerinaInvocationArg o) {
    visitPsiElement(o);
  }

  public void visitInvocationArgList(@NotNull BallerinaInvocationArgList o) {
    visitPsiElement(o);
  }

  public void visitInvocationReference(@NotNull BallerinaInvocationReference o) {
    visitVariableReference(o);
  }

  public void visitJoinStreamingInput(@NotNull BallerinaJoinStreamingInput o) {
    visitPsiElement(o);
  }

  public void visitJoinType(@NotNull BallerinaJoinType o) {
    visitPsiElement(o);
  }

  public void visitJsonTypeName(@NotNull BallerinaJsonTypeName o) {
    visitPsiElement(o);
  }

  public void visitLambdaFunction(@NotNull BallerinaLambdaFunction o) {
    visitPsiElement(o);
  }

  public void visitLambdaFunctionExpression(@NotNull BallerinaLambdaFunctionExpression o) {
    visitExpression(o);
  }

  public void visitLambdaReturnParameter(@NotNull BallerinaLambdaReturnParameter o) {
    visitPsiElement(o);
  }

  public void visitLimitClause(@NotNull BallerinaLimitClause o) {
    visitPsiElement(o);
  }

  public void visitLockStatement(@NotNull BallerinaLockStatement o) {
    visitPsiElement(o);
  }

  public void visitMapArrayVariableReference(@NotNull BallerinaMapArrayVariableReference o) {
    visitVariableReference(o);
  }

  public void visitMapTypeName(@NotNull BallerinaMapTypeName o) {
    visitPsiElement(o);
  }

  public void visitNameReference(@NotNull BallerinaNameReference o) {
    visitPsiElement(o);
  }

  public void visitNamedArgs(@NotNull BallerinaNamedArgs o) {
    visitPsiElement(o);
  }

  public void visitNamespaceDeclaration(@NotNull BallerinaNamespaceDeclaration o) {
    visitPsiElement(o);
  }

  public void visitNamespaceDeclarationStatement(@NotNull BallerinaNamespaceDeclarationStatement o) {
    visitPsiElement(o);
  }

  public void visitNullableTypeName(@NotNull BallerinaNullableTypeName o) {
    visitTypeName(o);
  }

  public void visitObjectBody(@NotNull BallerinaObjectBody o) {
    visitPsiElement(o);
  }

  public void visitObjectFieldDefinition(@NotNull BallerinaObjectFieldDefinition o) {
    visitPsiElement(o);
  }

  public void visitObjectFunctionDefinition(@NotNull BallerinaObjectFunctionDefinition o) {
    visitPsiElement(o);
  }

  public void visitObjectTypeName(@NotNull BallerinaObjectTypeName o) {
    visitTypeName(o);
  }

  public void visitOnRetryClause(@NotNull BallerinaOnRetryClause o) {
    visitPsiElement(o);
  }

  public void visitOrderByClause(@NotNull BallerinaOrderByClause o) {
    visitPsiElement(o);
  }

  public void visitOrderByType(@NotNull BallerinaOrderByType o) {
    visitPsiElement(o);
  }

  public void visitOrderByVariable(@NotNull BallerinaOrderByVariable o) {
    visitPsiElement(o);
  }

  public void visitOrgName(@NotNull BallerinaOrgName o) {
    visitPsiElement(o);
  }

  public void visitOutputRateLimit(@NotNull BallerinaOutputRateLimit o) {
    visitPsiElement(o);
  }

  public void visitPackageName(@NotNull BallerinaPackageName o) {
    visitPsiElement(o);
  }

  public void visitPackageReference(@NotNull BallerinaPackageReference o) {
    visitPsiElement(o);
  }

  public void visitPackageVersion(@NotNull BallerinaPackageVersion o) {
    visitPsiElement(o);
  }

  public void visitPanicStatement(@NotNull BallerinaPanicStatement o) {
    visitPsiElement(o);
  }

  public void visitParameter(@NotNull BallerinaParameter o) {
    visitPsiElement(o);
  }

  public void visitParameterList(@NotNull BallerinaParameterList o) {
    visitPsiElement(o);
  }

  public void visitParameterTypeNameList(@NotNull BallerinaParameterTypeNameList o) {
    visitPsiElement(o);
  }

  public void visitPatternClause(@NotNull BallerinaPatternClause o) {
    visitPsiElement(o);
  }

  public void visitPatternStreamingEdgeInput(@NotNull BallerinaPatternStreamingEdgeInput o) {
    visitPsiElement(o);
  }

  public void visitPatternStreamingInput(@NotNull BallerinaPatternStreamingInput o) {
    visitPsiElement(o);
  }

  public void visitProcIns(@NotNull BallerinaProcIns o) {
    visitPsiElement(o);
  }

  public void visitRecordBindingPattern(@NotNull BallerinaRecordBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitRecordDestructuringStatement(@NotNull BallerinaRecordDestructuringStatement o) {
    visitPsiElement(o);
  }

  public void visitRecordFieldDefinitionList(@NotNull BallerinaRecordFieldDefinitionList o) {
    visitPsiElement(o);
  }

  public void visitRecordKey(@NotNull BallerinaRecordKey o) {
    visitPsiElement(o);
  }

  public void visitRecordKeyValue(@NotNull BallerinaRecordKeyValue o) {
    visitPsiElement(o);
  }

  public void visitRecordLiteral(@NotNull BallerinaRecordLiteral o) {
    visitPsiElement(o);
  }

  public void visitRecordLiteralBody(@NotNull BallerinaRecordLiteralBody o) {
    visitPsiElement(o);
  }

  public void visitRecordLiteralExpression(@NotNull BallerinaRecordLiteralExpression o) {
    visitExpression(o);
  }

  public void visitRecordRefBindingPattern(@NotNull BallerinaRecordRefBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitRecordRestFieldDefinition(@NotNull BallerinaRecordRestFieldDefinition o) {
    visitPsiElement(o);
  }

  public void visitRecordTypeName(@NotNull BallerinaRecordTypeName o) {
    visitTypeName(o);
  }

  public void visitReferenceTypeName(@NotNull BallerinaReferenceTypeName o) {
    visitPsiElement(o);
  }

  public void visitReservedWord(@NotNull BallerinaReservedWord o) {
    visitPsiElement(o);
  }

  public void visitRestArgs(@NotNull BallerinaRestArgs o) {
    visitPsiElement(o);
  }

  public void visitRestBindingPattern(@NotNull BallerinaRestBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitRestParameter(@NotNull BallerinaRestParameter o) {
    visitPsiElement(o);
  }

  public void visitRestRefBindingPattern(@NotNull BallerinaRestRefBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitRetriesStatement(@NotNull BallerinaRetriesStatement o) {
    visitPsiElement(o);
  }

  public void visitRetryStatement(@NotNull BallerinaRetryStatement o) {
    visitPsiElement(o);
  }

  public void visitReturnParameter(@NotNull BallerinaReturnParameter o) {
    visitPsiElement(o);
  }

  public void visitReturnStatement(@NotNull BallerinaReturnStatement o) {
    visitPsiElement(o);
  }

  public void visitReturnType(@NotNull BallerinaReturnType o) {
    visitPsiElement(o);
  }

  public void visitSealedLiteral(@NotNull BallerinaSealedLiteral o) {
    visitPsiElement(o);
  }

  public void visitSelectClause(@NotNull BallerinaSelectClause o) {
    visitPsiElement(o);
  }

  public void visitSelectExpression(@NotNull BallerinaSelectExpression o) {
    visitPsiElement(o);
  }

  public void visitSelectExpressionList(@NotNull BallerinaSelectExpressionList o) {
    visitPsiElement(o);
  }

  public void visitServiceBody(@NotNull BallerinaServiceBody o) {
    visitPsiElement(o);
  }

  public void visitServiceBodyMember(@NotNull BallerinaServiceBodyMember o) {
    visitPsiElement(o);
  }

  public void visitServiceConstructorExpression(@NotNull BallerinaServiceConstructorExpression o) {
    visitExpression(o);
  }

  public void visitServiceDefinition(@NotNull BallerinaServiceDefinition o) {
    visitPsiElement(o);
  }

  public void visitServiceTypeName(@NotNull BallerinaServiceTypeName o) {
    visitPsiElement(o);
  }

  public void visitShiftExpression(@NotNull BallerinaShiftExpression o) {
    visitPsiElement(o);
  }

  public void visitSimpleLiteral(@NotNull BallerinaSimpleLiteral o) {
    visitPsiElement(o);
  }

  public void visitSimpleLiteralExpression(@NotNull BallerinaSimpleLiteralExpression o) {
    visitExpression(o);
  }

  public void visitSimpleTypeName(@NotNull BallerinaSimpleTypeName o) {
    visitTypeName(o);
  }

  public void visitSimpleVariableReference(@NotNull BallerinaSimpleVariableReference o) {
    visitVariableReference(o);
  }

  public void visitStartTag(@NotNull BallerinaStartTag o) {
    visitPsiElement(o);
  }

  public void visitStatement(@NotNull BallerinaStatement o) {
    visitPsiElement(o);
  }

  public void visitStaticMatchIdentifierLiteral(@NotNull BallerinaStaticMatchIdentifierLiteral o) {
    visitStaticMatchLiterals(o);
  }

  public void visitStaticMatchLiterals(@NotNull BallerinaStaticMatchLiterals o) {
    visitPsiElement(o);
  }

  public void visitStaticMatchOrExpression(@NotNull BallerinaStaticMatchOrExpression o) {
    visitStaticMatchLiterals(o);
  }

  public void visitStaticMatchRecordLiteral(@NotNull BallerinaStaticMatchRecordLiteral o) {
    visitStaticMatchLiterals(o);
  }

  public void visitStaticMatchSimpleLiteral(@NotNull BallerinaStaticMatchSimpleLiteral o) {
    visitStaticMatchLiterals(o);
  }

  public void visitStaticMatchTupleLiteral(@NotNull BallerinaStaticMatchTupleLiteral o) {
    visitStaticMatchLiterals(o);
  }

  public void visitStreamTypeName(@NotNull BallerinaStreamTypeName o) {
    visitPsiElement(o);
  }

  public void visitStreamingAction(@NotNull BallerinaStreamingAction o) {
    visitPsiElement(o);
  }

  public void visitStreamingInput(@NotNull BallerinaStreamingInput o) {
    visitPsiElement(o);
  }

  public void visitStreamingQueryStatement(@NotNull BallerinaStreamingQueryStatement o) {
    visitPsiElement(o);
  }

  public void visitStringFunctionInvocationReference(@NotNull BallerinaStringFunctionInvocationReference o) {
    visitVariableReference(o);
  }

  public void visitStringTemplateContent(@NotNull BallerinaStringTemplateContent o) {
    visitPsiElement(o);
  }

  public void visitStringTemplateLiteral(@NotNull BallerinaStringTemplateLiteral o) {
    visitPsiElement(o);
  }

  public void visitStringTemplateLiteralExpression(@NotNull BallerinaStringTemplateLiteralExpression o) {
    visitExpression(o);
  }

  public void visitStructuredBindingPattern(@NotNull BallerinaStructuredBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitStructuredRefBindingPattern(@NotNull BallerinaStructuredRefBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitTableColumn(@NotNull BallerinaTableColumn o) {
    visitPsiElement(o);
  }

  public void visitTableColumnDefinition(@NotNull BallerinaTableColumnDefinition o) {
    visitPsiElement(o);
  }

  public void visitTableData(@NotNull BallerinaTableData o) {
    visitPsiElement(o);
  }

  public void visitTableDataArray(@NotNull BallerinaTableDataArray o) {
    visitPsiElement(o);
  }

  public void visitTableDataList(@NotNull BallerinaTableDataList o) {
    visitPsiElement(o);
  }

  public void visitTableLiteral(@NotNull BallerinaTableLiteral o) {
    visitPsiElement(o);
  }

  public void visitTableLiteralExpression(@NotNull BallerinaTableLiteralExpression o) {
    visitExpression(o);
  }

  public void visitTableQuery(@NotNull BallerinaTableQuery o) {
    visitPsiElement(o);
  }

  public void visitTableQueryExpression(@NotNull BallerinaTableQueryExpression o) {
    visitExpression(o);
  }

  public void visitTableTypeName(@NotNull BallerinaTableTypeName o) {
    visitPsiElement(o);
  }

  public void visitTernaryExpression(@NotNull BallerinaTernaryExpression o) {
    visitExpression(o);
  }

  public void visitThrowStatement(@NotNull BallerinaThrowStatement o) {
    visitPsiElement(o);
  }

  public void visitTimeScale(@NotNull BallerinaTimeScale o) {
    visitPsiElement(o);
  }

  public void visitTransactionClause(@NotNull BallerinaTransactionClause o) {
    visitPsiElement(o);
  }

  public void visitTransactionPropertyInitStatement(@NotNull BallerinaTransactionPropertyInitStatement o) {
    visitPsiElement(o);
  }

  public void visitTransactionPropertyInitStatementList(@NotNull BallerinaTransactionPropertyInitStatementList o) {
    visitPsiElement(o);
  }

  public void visitTransactionStatement(@NotNull BallerinaTransactionStatement o) {
    visitPsiElement(o);
  }

  public void visitTrapExpression(@NotNull BallerinaTrapExpression o) {
    visitExpression(o);
  }

  public void visitTryCatchStatement(@NotNull BallerinaTryCatchStatement o) {
    visitPsiElement(o);
  }

  public void visitTupleBindingPattern(@NotNull BallerinaTupleBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitTupleLiteral(@NotNull BallerinaTupleLiteral o) {
    visitPsiElement(o);
  }

  public void visitTupleRefBindingPattern(@NotNull BallerinaTupleRefBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitTupleTypeName(@NotNull BallerinaTupleTypeName o) {
    visitTypeName(o);
  }

  public void visitTypeAccessExprInvocationReference(@NotNull BallerinaTypeAccessExprInvocationReference o) {
    visitVariableReference(o);
  }

  public void visitTypeAccessExpression(@NotNull BallerinaTypeAccessExpression o) {
    visitExpression(o);
  }

  public void visitTypeConversionExpression(@NotNull BallerinaTypeConversionExpression o) {
    visitExpression(o);
  }

  public void visitTypeDefinition(@NotNull BallerinaTypeDefinition o) {
    visitTopLevelDefinition(o);
  }

  public void visitTypeDescTypeName(@NotNull BallerinaTypeDescTypeName o) {
    visitPsiElement(o);
  }

  public void visitTypeInitExpression(@NotNull BallerinaTypeInitExpression o) {
    visitExpression(o);
  }

  public void visitTypeName(@NotNull BallerinaTypeName o) {
    visitPsiElement(o);
  }

  public void visitTypeReference(@NotNull BallerinaTypeReference o) {
    visitPsiElement(o);
  }

  public void visitTypeTestExpression(@NotNull BallerinaTypeTestExpression o) {
    visitExpression(o);
  }

  public void visitUnaryExpression(@NotNull BallerinaUnaryExpression o) {
    visitExpression(o);
  }

  public void visitUnionTypeName(@NotNull BallerinaUnionTypeName o) {
    visitTypeName(o);
  }

  public void visitUserDefineTypeName(@NotNull BallerinaUserDefineTypeName o) {
    visitPsiElement(o);
  }

  public void visitValueTypeName(@NotNull BallerinaValueTypeName o) {
    visitPsiElement(o);
  }

  public void visitVariableDefinitionStatement(@NotNull BallerinaVariableDefinitionStatement o) {
    visitPsiElement(o);
  }

  public void visitVariableReference(@NotNull BallerinaVariableReference o) {
    visitPsiElement(o);
  }

  public void visitVariableReferenceExpression(@NotNull BallerinaVariableReferenceExpression o) {
    visitExpression(o);
  }

  public void visitVariableReferenceList(@NotNull BallerinaVariableReferenceList o) {
    visitPsiElement(o);
  }

  public void visitWaitExpression(@NotNull BallerinaWaitExpression o) {
    visitExpression(o);
  }

  public void visitWaitForCollection(@NotNull BallerinaWaitForCollection o) {
    visitPsiElement(o);
  }

  public void visitWaitKeyValue(@NotNull BallerinaWaitKeyValue o) {
    visitPsiElement(o);
  }

  public void visitWhereClause(@NotNull BallerinaWhereClause o) {
    visitPsiElement(o);
  }

  public void visitWhileStatement(@NotNull BallerinaWhileStatement o) {
    visitPsiElement(o);
  }

  public void visitWhileStatementBody(@NotNull BallerinaWhileStatementBody o) {
    visitPsiElement(o);
  }

  public void visitWindowClause(@NotNull BallerinaWindowClause o) {
    visitPsiElement(o);
  }

  public void visitWithinClause(@NotNull BallerinaWithinClause o) {
    visitPsiElement(o);
  }

  public void visitWorkerBody(@NotNull BallerinaWorkerBody o) {
    visitPsiElement(o);
  }

  public void visitWorkerDefinition(@NotNull BallerinaWorkerDefinition o) {
    visitTopLevelDefinition(o);
  }

  public void visitWorkerReceiveExpression(@NotNull BallerinaWorkerReceiveExpression o) {
    visitExpression(o);
  }

  public void visitWorkerSendAsyncExpression(@NotNull BallerinaWorkerSendAsyncExpression o) {
    visitExpression(o);
  }

  public void visitWorkerSendAsyncStatement(@NotNull BallerinaWorkerSendAsyncStatement o) {
    visitPsiElement(o);
  }

  public void visitWorkerWithStatementsBlock(@NotNull BallerinaWorkerWithStatementsBlock o) {
    visitPsiElement(o);
  }

  public void visitXmlAttrib(@NotNull BallerinaXmlAttrib o) {
    visitPsiElement(o);
  }

  public void visitXmlAttribVariableReference(@NotNull BallerinaXmlAttribVariableReference o) {
    visitVariableReference(o);
  }

  public void visitXmlDoubleQuotedString(@NotNull BallerinaXmlDoubleQuotedString o) {
    visitPsiElement(o);
  }

  public void visitXmlItem(@NotNull BallerinaXmlItem o) {
    visitPsiElement(o);
  }

  public void visitXmlLiteral(@NotNull BallerinaXmlLiteral o) {
    visitPsiElement(o);
  }

  public void visitXmlLiteralExpression(@NotNull BallerinaXmlLiteralExpression o) {
    visitExpression(o);
  }

  public void visitXmlLocalName(@NotNull BallerinaXmlLocalName o) {
    visitPsiElement(o);
  }

  public void visitXmlNamespaceName(@NotNull BallerinaXmlNamespaceName o) {
    visitPsiElement(o);
  }

  public void visitXmlQualifiedName(@NotNull BallerinaXmlQualifiedName o) {
    visitPsiElement(o);
  }

  public void visitXmlQuotedString(@NotNull BallerinaXmlQuotedString o) {
    visitPsiElement(o);
  }

  public void visitXmlSingleQuotedString(@NotNull BallerinaXmlSingleQuotedString o) {
    visitPsiElement(o);
  }

  public void visitXmlText(@NotNull BallerinaXmlText o) {
    visitPsiElement(o);
  }

  public void visitXmlTypeName(@NotNull BallerinaXmlTypeName o) {
    visitPsiElement(o);
  }

  public void visitBacktickedBlock(@NotNull BallerinaBacktickedBlock o) {
    visitPsiElement(o);
  }

  public void visitChannelType(@NotNull BallerinaChannelType o) {
    visitPsiElement(o);
  }

  public void visitDefinitionReferenceType(@NotNull BallerinaDefinitionReferenceType o) {
    visitPsiElement(o);
  }

  public void visitDeprecatedAttachment(@NotNull BallerinaDeprecatedAttachment o) {
    visitPsiElement(o);
  }

  public void visitDeprecatedTemplateInlineCode(@NotNull BallerinaDeprecatedTemplateInlineCode o) {
    visitPsiElement(o);
  }

  public void visitDeprecatedText(@NotNull BallerinaDeprecatedText o) {
    visitPsiElement(o);
  }

  public void visitDocParameterDescription(@NotNull BallerinaDocParameterDescription o) {
    visitPsiElement(o);
  }

  public void visitDocumentationContent(@NotNull BallerinaDocumentationContent o) {
    visitPsiElement(o);
  }

  public void visitDocumentationDefinitionReference(@NotNull BallerinaDocumentationDefinitionReference o) {
    visitPsiElement(o);
  }

  public void visitDocumentationLine(@NotNull BallerinaDocumentationLine o) {
    visitPsiElement(o);
  }

  public void visitDocumentationReference(@NotNull BallerinaDocumentationReference o) {
    visitPsiElement(o);
  }

  public void visitDocumentationString(@NotNull BallerinaDocumentationString o) {
    visitPsiElement(o);
  }

  public void visitDocumentationText(@NotNull BallerinaDocumentationText o) {
    visitPsiElement(o);
  }

  public void visitDoubleBackTickDeprecatedInlineCode(@NotNull BallerinaDoubleBackTickDeprecatedInlineCode o) {
    visitPsiElement(o);
  }

  public void visitDoubleBacktickedBlock(@NotNull BallerinaDoubleBacktickedBlock o) {
    visitPsiElement(o);
  }

  public void visitFunctionNameReference(@NotNull BallerinaFunctionNameReference o) {
    visitPsiElement(o);
  }

  public void visitInitWithType(@NotNull BallerinaInitWithType o) {
    visitPsiElement(o);
  }

  public void visitInitWithoutType(@NotNull BallerinaInitWithoutType o) {
    visitPsiElement(o);
  }

  public void visitMatchPatternClause(@NotNull BallerinaMatchPatternClause o) {
    visitPsiElement(o);
  }

  public void visitMatchStatement(@NotNull BallerinaMatchStatement o) {
    visitPsiElement(o);
  }

  public void visitMatchStatementBody(@NotNull BallerinaMatchStatementBody o) {
    visitPsiElement(o);
  }

  public void visitParameterDescription(@NotNull BallerinaParameterDescription o) {
    visitPsiElement(o);
  }

  public void visitParameterDocumentation(@NotNull BallerinaParameterDocumentation o) {
    visitPsiElement(o);
  }

  public void visitParameterDocumentationLine(@NotNull BallerinaParameterDocumentationLine o) {
    visitPsiElement(o);
  }

  public void visitParameterTypeName(@NotNull BallerinaParameterTypeName o) {
    visitPsiElement(o);
  }

  public void visitParameterWithType(@NotNull BallerinaParameterWithType o) {
    visitPsiElement(o);
  }

  public void visitReturnParameterDescription(@NotNull BallerinaReturnParameterDescription o) {
    visitPsiElement(o);
  }

  public void visitReturnParameterDocumentation(@NotNull BallerinaReturnParameterDocumentation o) {
    visitPsiElement(o);
  }

  public void visitReturnParameterDocumentationLine(@NotNull BallerinaReturnParameterDocumentationLine o) {
    visitPsiElement(o);
  }

  public void visitSingleBackTickDeprecatedInlineCode(@NotNull BallerinaSingleBackTickDeprecatedInlineCode o) {
    visitPsiElement(o);
  }

  public void visitSingleBacktickedBlock(@NotNull BallerinaSingleBacktickedBlock o) {
    visitPsiElement(o);
  }

  public void visitStaticMatchPattern(@NotNull BallerinaStaticMatchPattern o) {
    visitPsiElement(o);
  }

  public void visitTripleBackTickDeprecatedInlineCode(@NotNull BallerinaTripleBackTickDeprecatedInlineCode o) {
    visitPsiElement(o);
  }

  public void visitTripleBacktickedBlock(@NotNull BallerinaTripleBacktickedBlock o) {
    visitPsiElement(o);
  }

  public void visitTupleDestructuringStatement(@NotNull BallerinaTupleDestructuringStatement o) {
    visitPsiElement(o);
  }

  public void visitVarMatchPattern(@NotNull BallerinaVarMatchPattern o) {
    visitPsiElement(o);
  }

  public void visitVariableDefinitionStatementWithAssignment(@NotNull BallerinaVariableDefinitionStatementWithAssignment o) {
    visitPsiElement(o);
  }

  public void visitVariableDefinitionStatementWithoutAssignment(@NotNull BallerinaVariableDefinitionStatementWithoutAssignment o) {
    visitPsiElement(o);
  }

  public void visitTopLevelDefinition(@NotNull BallerinaTopLevelDefinition o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
