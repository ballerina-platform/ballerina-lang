/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

  public void visitAnnotationAccessReference(@NotNull BallerinaAnnotationAccessReference o) {
    visitVariableReference(o);
  }

  public void visitAnnotationActionExpression(@NotNull BallerinaAnnotationActionExpression o) {
    visitExpression(o);
  }

  public void visitAnnotationAttachment(@NotNull BallerinaAnnotationAttachment o) {
    visitPsiElement(o);
  }

  public void visitAnnotationDefinition(@NotNull BallerinaAnnotationDefinition o) {
    visitTopLevelDefinition(o);
  }

  public void visitAnonymousFunctionExpr(@NotNull BallerinaAnonymousFunctionExpr o) {
    visitPsiElement(o);
  }

  public void visitAnonymousFunctionExpression(@NotNull BallerinaAnonymousFunctionExpression o) {
    visitExpression(o);
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

  public void visitArrayTypeName(@NotNull BallerinaArrayTypeName o) {
    visitTypeName(o);
  }

  public void visitAssignmentStatement(@NotNull BallerinaAssignmentStatement o) {
    visitPsiElement(o);
  }

  public void visitAttachmentPoint(@NotNull BallerinaAttachmentPoint o) {
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

  public void visitBlockFunctionBody(@NotNull BallerinaBlockFunctionBody o) {
    visitPsiElement(o);
  }

  public void visitBreakStatement(@NotNull BallerinaBreakStatement o) {
    visitPsiElement(o);
  }

  public void visitBuiltInReferenceTypeName(@NotNull BallerinaBuiltInReferenceTypeName o) {
    visitPsiElement(o);
  }

  public void visitCatchClause(@NotNull BallerinaCatchClause o) {
    visitPsiElement(o);
  }

  public void visitCatchClauses(@NotNull BallerinaCatchClauses o) {
    visitPsiElement(o);
  }

  public void visitCheckPanicExpression(@NotNull BallerinaCheckPanicExpression o) {
    visitExpression(o);
  }

  public void visitCheckedExpression(@NotNull BallerinaCheckedExpression o) {
    visitExpression(o);
  }

  public void visitClosedRecordBindingPattern(@NotNull BallerinaClosedRecordBindingPattern o) {
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

  public void visitConstAddSubExpression(@NotNull BallerinaConstAddSubExpression o) {
    visitConstantExpression(o);
  }

  public void visitConstDivMulModExpression(@NotNull BallerinaConstDivMulModExpression o) {
    visitConstantExpression(o);
  }

  public void visitConstGroupExpression(@NotNull BallerinaConstGroupExpression o) {
    visitConstantExpression(o);
  }

  public void visitConstantDefinition(@NotNull BallerinaConstantDefinition o) {
    visitTopLevelDefinition(o);
  }

  public void visitConstantExpression(@NotNull BallerinaConstantExpression o) {
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

  public void visitDoClause(@NotNull BallerinaDoClause o) {
    visitPsiElement(o);
  }

  public void visitDualAttachPoint(@NotNull BallerinaDualAttachPoint o) {
    visitPsiElement(o);
  }

  public void visitDualAttachPointIdent(@NotNull BallerinaDualAttachPointIdent o) {
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

  public void visitEntryBindingPattern(@NotNull BallerinaEntryBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitEntryRefBindingPattern(@NotNull BallerinaEntryRefBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitErrorArgListMatchPattern(@NotNull BallerinaErrorArgListMatchPattern o) {
    visitPsiElement(o);
  }

  public void visitErrorBindingPattern(@NotNull BallerinaErrorBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitErrorDestructuringStatement(@NotNull BallerinaErrorDestructuringStatement o) {
    visitPsiElement(o);
  }

  public void visitErrorDetailBindingPattern(@NotNull BallerinaErrorDetailBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitErrorFieldBindingPatterns(@NotNull BallerinaErrorFieldBindingPatterns o) {
    visitPsiElement(o);
  }

  public void visitErrorFieldMatchPatterns(@NotNull BallerinaErrorFieldMatchPatterns o) {
    visitPsiElement(o);
  }

  public void visitErrorMatchPattern(@NotNull BallerinaErrorMatchPattern o) {
    visitPsiElement(o);
  }

  public void visitErrorMatchPatternClause(@NotNull BallerinaErrorMatchPatternClause o) {
    visitPsiElement(o);
  }

  public void visitErrorNamedArgRefPattern(@NotNull BallerinaErrorNamedArgRefPattern o) {
    visitPsiElement(o);
  }

  public void visitErrorRefBindingPattern(@NotNull BallerinaErrorRefBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitErrorRefRestPattern(@NotNull BallerinaErrorRefRestPattern o) {
    visitPsiElement(o);
  }

  public void visitErrorRestBindingPattern(@NotNull BallerinaErrorRestBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitErrorTypeName(@NotNull BallerinaErrorTypeName o) {
    visitPsiElement(o);
  }

  public void visitExclusiveRecordTypeDescriptor(@NotNull BallerinaExclusiveRecordTypeDescriptor o) {
    visitTypeName(o);
  }

  public void visitExplicitAnonymousFunctionExpr(@NotNull BallerinaExplicitAnonymousFunctionExpr o) {
    visitPsiElement(o);
  }

  public void visitExprFunctionBody(@NotNull BallerinaExprFunctionBody o) {
    visitPsiElement(o);
  }

  public void visitExprFunctionBodySpec(@NotNull BallerinaExprFunctionBodySpec o) {
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

  public void visitExternalFunctionBody(@NotNull BallerinaExternalFunctionBody o) {
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

  public void visitFieldDescriptor(@NotNull BallerinaFieldDescriptor o) {
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

  public void visitForkJoinStatement(@NotNull BallerinaForkJoinStatement o) {
    visitPsiElement(o);
  }

  public void visitFormalParameterList(@NotNull BallerinaFormalParameterList o) {
    visitPsiElement(o);
  }

  public void visitFromClause(@NotNull BallerinaFromClause o) {
    visitPsiElement(o);
  }

  public void visitFunctionDefinition(@NotNull BallerinaFunctionDefinition o) {
    visitTopLevelDefinition(o);
  }

  public void visitFunctionDefinitionBody(@NotNull BallerinaFunctionDefinitionBody o) {
    visitPsiElement(o);
  }

  public void visitFunctionInvocation(@NotNull BallerinaFunctionInvocation o) {
    visitPsiElement(o);
  }

  public void visitFunctionInvocationReference(@NotNull BallerinaFunctionInvocationReference o) {
    visitVariableReference(o);
  }

  public void visitFunctionSignature(@NotNull BallerinaFunctionSignature o) {
    visitPsiElement(o);
  }

  public void visitFunctionTypeName(@NotNull BallerinaFunctionTypeName o) {
    visitPsiElement(o);
  }

  public void visitFutureTypeName(@NotNull BallerinaFutureTypeName o) {
    visitPsiElement(o);
  }

  public void visitGlobalVariableDefinition(@NotNull BallerinaGlobalVariableDefinition o) {
    visitTopLevelDefinition(o);
  }

  public void visitGroupExpression(@NotNull BallerinaGroupExpression o) {
    visitExpression(o);
  }

  public void visitGroupFieldVariableReference(@NotNull BallerinaGroupFieldVariableReference o) {
    visitVariableReference(o);
  }

  public void visitGroupInvocationReference(@NotNull BallerinaGroupInvocationReference o) {
    visitVariableReference(o);
  }

  public void visitGroupMapArrayVariableReference(@NotNull BallerinaGroupMapArrayVariableReference o) {
    visitVariableReference(o);
  }

  public void visitGroupStringFunctionInvocationReference(@NotNull BallerinaGroupStringFunctionInvocationReference o) {
    visitVariableReference(o);
  }

  public void visitGroupTypeName(@NotNull BallerinaGroupTypeName o) {
    visitTypeName(o);
  }

  public void visitHandleTypeName(@NotNull BallerinaHandleTypeName o) {
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

  public void visitInclusiveRecordTypeDescriptor(@NotNull BallerinaInclusiveRecordTypeDescriptor o) {
    visitTypeName(o);
  }

  public void visitIndex(@NotNull BallerinaIndex o) {
    visitPsiElement(o);
  }

  public void visitInferAnonymousFunctionExpr(@NotNull BallerinaInferAnonymousFunctionExpr o) {
    visitPsiElement(o);
  }

  public void visitInferParam(@NotNull BallerinaInferParam o) {
    visitPsiElement(o);
  }

  public void visitInferParamList(@NotNull BallerinaInferParamList o) {
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

  public void visitJsonTypeName(@NotNull BallerinaJsonTypeName o) {
    visitPsiElement(o);
  }

  public void visitLetClause(@NotNull BallerinaLetClause o) {
    visitPsiElement(o);
  }

  public void visitLetExpression(@NotNull BallerinaLetExpression o) {
    visitExpression(o);
  }

  public void visitLetVarDecl(@NotNull BallerinaLetVarDecl o) {
    visitPsiElement(o);
  }

  public void visitListBindingPattern(@NotNull BallerinaListBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitListConstructorExpr(@NotNull BallerinaListConstructorExpr o) {
    visitPsiElement(o);
  }

  public void visitListConstructorExpression(@NotNull BallerinaListConstructorExpression o) {
    visitExpression(o);
  }

  public void visitListDestructuringStatement(@NotNull BallerinaListDestructuringStatement o) {
    visitPsiElement(o);
  }

  public void visitListRefBindingPattern(@NotNull BallerinaListRefBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitListRefRestPattern(@NotNull BallerinaListRefRestPattern o) {
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

  public void visitMatchPatternClause(@NotNull BallerinaMatchPatternClause o) {
    visitPsiElement(o);
  }

  public void visitMatchStatement(@NotNull BallerinaMatchStatement o) {
    visitPsiElement(o);
  }

  public void visitMatchStatementBody(@NotNull BallerinaMatchStatementBody o) {
    visitPsiElement(o);
  }

  public void visitMethodDeclaration(@NotNull BallerinaMethodDeclaration o) {
    visitPsiElement(o);
  }

  public void visitMethodDefinition(@NotNull BallerinaMethodDefinition o) {
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

  public void visitNilLiteral(@NotNull BallerinaNilLiteral o) {
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

  public void visitObjectMethod(@NotNull BallerinaObjectMethod o) {
    visitPsiElement(o);
  }

  public void visitObjectTypeName(@NotNull BallerinaObjectTypeName o) {
    visitTypeName(o);
  }

  public void visitOnRetryClause(@NotNull BallerinaOnRetryClause o) {
    visitPsiElement(o);
  }

  public void visitOpenRecordBindingPattern(@NotNull BallerinaOpenRecordBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitOrgName(@NotNull BallerinaOrgName o) {
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

  public void visitParameterTypeName(@NotNull BallerinaParameterTypeName o) {
    visitPsiElement(o);
  }

  public void visitParameterTypeNameList(@NotNull BallerinaParameterTypeNameList o) {
    visitPsiElement(o);
  }

  public void visitPeerWorker(@NotNull BallerinaPeerWorker o) {
    visitPsiElement(o);
  }

  public void visitQueryActionStatement(@NotNull BallerinaQueryActionStatement o) {
    visitPsiElement(o);
  }

  public void visitQueryExpr(@NotNull BallerinaQueryExpr o) {
    visitPsiElement(o);
  }

  public void visitQueryExpression(@NotNull BallerinaQueryExpression o) {
    visitExpression(o);
  }

  public void visitQueryPipeline(@NotNull BallerinaQueryPipeline o) {
    visitPsiElement(o);
  }

  public void visitRecordBindingPattern(@NotNull BallerinaRecordBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitRecordDestructuringStatement(@NotNull BallerinaRecordDestructuringStatement o) {
    visitPsiElement(o);
  }

  public void visitRecordField(@NotNull BallerinaRecordField o) {
    visitPsiElement(o);
  }

  public void visitRecordKey(@NotNull BallerinaRecordKey o) {
    visitPsiElement(o);
  }

  public void visitRecordKeyValueField(@NotNull BallerinaRecordKeyValueField o) {
    visitPsiElement(o);
  }

  public void visitRecordLiteral(@NotNull BallerinaRecordLiteral o) {
    visitPsiElement(o);
  }

  public void visitRecordLiteralBody(@NotNull BallerinaRecordLiteralBody o) {
    visitPsiElement(o);
  }

  public void visitRecordLiteralConstExpression(@NotNull BallerinaRecordLiteralConstExpression o) {
    visitConstantExpression(o);
  }

  public void visitRecordLiteralExpression(@NotNull BallerinaRecordLiteralExpression o) {
    visitExpression(o);
  }

  public void visitRecordRefBindingPattern(@NotNull BallerinaRecordRefBindingPattern o) {
    visitPsiElement(o);
  }

  public void visitRecordRestField(@NotNull BallerinaRecordRestField o) {
    visitPsiElement(o);
  }

  public void visitRecordRestFieldDefinition(@NotNull BallerinaRecordRestFieldDefinition o) {
    visitPsiElement(o);
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

  public void visitRestMatchPattern(@NotNull BallerinaRestMatchPattern o) {
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

  public void visitSealedLiteral(@NotNull BallerinaSealedLiteral o) {
    visitPsiElement(o);
  }

  public void visitSelectClause(@NotNull BallerinaSelectClause o) {
    visitPsiElement(o);
  }

  public void visitServiceBody(@NotNull BallerinaServiceBody o) {
    visitPsiElement(o);
  }

  public void visitServiceConstructorExpression(@NotNull BallerinaServiceConstructorExpression o) {
    visitExpression(o);
  }

  public void visitServiceDefinition(@NotNull BallerinaServiceDefinition o) {
    visitTopLevelDefinition(o);
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

  public void visitSimpleLiteralConstExpression(@NotNull BallerinaSimpleLiteralConstExpression o) {
    visitConstantExpression(o);
  }

  public void visitSimpleLiteralExpression(@NotNull BallerinaSimpleLiteralExpression o) {
    visitExpression(o);
  }

  public void visitSimpleMatchPattern(@NotNull BallerinaSimpleMatchPattern o) {
    visitPsiElement(o);
  }

  public void visitSimpleTypeName(@NotNull BallerinaSimpleTypeName o) {
    visitTypeName(o);
  }

  public void visitSimpleVariableReference(@NotNull BallerinaSimpleVariableReference o) {
    visitVariableReference(o);
  }

  public void visitSourceOnlyAttachPoint(@NotNull BallerinaSourceOnlyAttachPoint o) {
    visitPsiElement(o);
  }

  public void visitSourceOnlyAttachPointIdent(@NotNull BallerinaSourceOnlyAttachPointIdent o) {
    visitPsiElement(o);
  }

  public void visitStatement(@NotNull BallerinaStatement o) {
    visitPsiElement(o);
  }

  public void visitStaticMatchIdentifierLiteral(@NotNull BallerinaStaticMatchIdentifierLiteral o) {
    visitStaticMatchLiteral(o);
  }

  public void visitStaticMatchListLiteral(@NotNull BallerinaStaticMatchListLiteral o) {
    visitStaticMatchLiteral(o);
  }

  public void visitStaticMatchLiteral(@NotNull BallerinaStaticMatchLiteral o) {
    visitPsiElement(o);
  }

  public void visitStaticMatchOrExpression(@NotNull BallerinaStaticMatchOrExpression o) {
    visitStaticMatchLiteral(o);
  }

  public void visitStaticMatchPatternClause(@NotNull BallerinaStaticMatchPatternClause o) {
    visitPsiElement(o);
  }

  public void visitStaticMatchRecordLiteral(@NotNull BallerinaStaticMatchRecordLiteral o) {
    visitStaticMatchLiteral(o);
  }

  public void visitStaticMatchSimpleLiteral(@NotNull BallerinaStaticMatchSimpleLiteral o) {
    visitStaticMatchLiteral(o);
  }

  public void visitStreamConstructorBody(@NotNull BallerinaStreamConstructorBody o) {
    visitPsiElement(o);
  }

  public void visitStreamConstructorExpr(@NotNull BallerinaStreamConstructorExpr o) {
    visitPsiElement(o);
  }

  public void visitStreamConstructorExpression(@NotNull BallerinaStreamConstructorExpression o) {
    visitExpression(o);
  }

  public void visitStreamTypeName(@NotNull BallerinaStreamTypeName o) {
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

  public void visitTableTypeName(@NotNull BallerinaTableTypeName o) {
    visitPsiElement(o);
  }

  public void visitTernaryExpression(@NotNull BallerinaTernaryExpression o) {
    visitExpression(o);
  }

  public void visitThrowStatement(@NotNull BallerinaThrowStatement o) {
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

  public void visitTupleRestDescriptor(@NotNull BallerinaTupleRestDescriptor o) {
    visitPsiElement(o);
  }

  public void visitTupleTypeName(@NotNull BallerinaTupleTypeName o) {
    visitTypeName(o);
  }

  public void visitTypeConversionExpression(@NotNull BallerinaTypeConversionExpression o) {
    visitExpression(o);
  }

  public void visitTypeDefinition(@NotNull BallerinaTypeDefinition o) {
    visitTopLevelDefinition(o);
  }

  public void visitTypeDescExprInvocationReference(@NotNull BallerinaTypeDescExprInvocationReference o) {
    visitVariableReference(o);
  }

  public void visitTypeDescExpression(@NotNull BallerinaTypeDescExpression o) {
    visitExpression(o);
  }

  public void visitTypeDescReferenceTypeName(@NotNull BallerinaTypeDescReferenceTypeName o) {
    visitPsiElement(o);
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

  public void visitVarMatchPatternClause(@NotNull BallerinaVarMatchPatternClause o) {
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

  public void visitVersionPattern(@NotNull BallerinaVersionPattern o) {
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

  public void visitWorkerBody(@NotNull BallerinaWorkerBody o) {
    visitPsiElement(o);
  }

  public void visitWorkerDefinition(@NotNull BallerinaWorkerDefinition o) {
    visitPsiElement(o);
  }

  public void visitWorkerName(@NotNull BallerinaWorkerName o) {
    visitPsiElement(o);
  }

  public void visitWorkerReceiveExpression(@NotNull BallerinaWorkerReceiveExpression o) {
    visitExpression(o);
  }

  public void visitWorkerSendAsyncStatement(@NotNull BallerinaWorkerSendAsyncStatement o) {
    visitPsiElement(o);
  }

  public void visitWorkerSendSyncExpression(@NotNull BallerinaWorkerSendSyncExpression o) {
    visitExpression(o);
  }

  public void visitWorkerWithStatementsBlock(@NotNull BallerinaWorkerWithStatementsBlock o) {
    visitPsiElement(o);
  }

  public void visitXmlAllowedText(@NotNull BallerinaXmlAllowedText o) {
    visitPsiElement(o);
  }

  public void visitXmlAttrib(@NotNull BallerinaXmlAttrib o) {
    visitPsiElement(o);
  }

  public void visitXmlAttribVariableReference(@NotNull BallerinaXmlAttribVariableReference o) {
    visitVariableReference(o);
  }

  public void visitXmlElementAccessFilter(@NotNull BallerinaXmlElementAccessFilter o) {
    visitPsiElement(o);
  }

  public void visitXmlElementFilterReference(@NotNull BallerinaXmlElementFilterReference o) {
    visitVariableReference(o);
  }

  public void visitXmlElementNames(@NotNull BallerinaXmlElementNames o) {
    visitPsiElement(o);
  }

  public void visitXmlLiteral(@NotNull BallerinaXmlLiteral o) {
    visitPsiElement(o);
  }

  public void visitXmlLiteralExpression(@NotNull BallerinaXmlLiteralExpression o) {
    visitExpression(o);
  }

  public void visitXmlStepExpression(@NotNull BallerinaXmlStepExpression o) {
    visitPsiElement(o);
  }

  public void visitXmlStepExpressionReference(@NotNull BallerinaXmlStepExpressionReference o) {
    visitVariableReference(o);
  }

  public void visitXmlTypeName(@NotNull BallerinaXmlTypeName o) {
    visitPsiElement(o);
  }

  public void visitBacktickedBlock(@NotNull BallerinaBacktickedBlock o) {
    visitPsiElement(o);
  }

  public void visitDocParameterDescription(@NotNull BallerinaDocParameterDescription o) {
    visitPsiElement(o);
  }

  public void visitDocumentationContent(@NotNull BallerinaDocumentationContent o) {
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

  public void visitDocumentationTextContent(@NotNull BallerinaDocumentationTextContent o) {
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

  public void visitParameterDescription(@NotNull BallerinaParameterDescription o) {
    visitPsiElement(o);
  }

  public void visitParameterDocumentation(@NotNull BallerinaParameterDocumentation o) {
    visitPsiElement(o);
  }

  public void visitParameterDocumentationLine(@NotNull BallerinaParameterDocumentationLine o) {
    visitPsiElement(o);
  }

  public void visitReferenceType(@NotNull BallerinaReferenceType o) {
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

  public void visitSingleBacktickedBlock(@NotNull BallerinaSingleBacktickedBlock o) {
    visitPsiElement(o);
  }

  public void visitTripleBacktickedBlock(@NotNull BallerinaTripleBacktickedBlock o) {
    visitPsiElement(o);
  }

  public void visitVariableDefinitionStatementWithAssignment(@NotNull BallerinaVariableDefinitionStatementWithAssignment o) {
    visitPsiElement(o);
  }

  public void visitVariableDefinitionStatementWithoutAssignment(@NotNull BallerinaVariableDefinitionStatementWithoutAssignment o) {
    visitPsiElement(o);
  }

  public void visitXmlElementFilter(@NotNull BallerinaXmlElementFilter o) {
    visitPsiElement(o);
  }

  public void visitTopLevelDefinition(@NotNull BallerinaTopLevelDefinition o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
