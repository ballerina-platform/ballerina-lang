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

  public void visitAlias(@NotNull BallerinaAlias o) {
    visitPsiElement(o);
  }

  public void visitAnnotationAttachment(@NotNull BallerinaAnnotationAttachment o) {
    visitPsiElement(o);
  }

  public void visitAnnotationDefinition(@NotNull BallerinaAnnotationDefinition o) {
    visitTopLevelDefinition(o);
  }

  public void visitAnyIdentifierName(@NotNull BallerinaAnyIdentifierName o) {
    visitPsiElement(o);
  }

  public void visitArrayTypeName(@NotNull BallerinaArrayTypeName o) {
    visitTypeName(o);
  }

  public void visitBuiltInReferenceTypeName(@NotNull BallerinaBuiltInReferenceTypeName o) {
    visitPsiElement(o);
  }

  public void visitCloseRecordTypeBody(@NotNull BallerinaCloseRecordTypeBody o) {
    visitPsiElement(o);
  }

  public void visitCompletePackageName(@NotNull BallerinaCompletePackageName o) {
    visitPsiElement(o);
  }

  public void visitConstantDefinition(@NotNull BallerinaConstantDefinition o) {
    visitTopLevelDefinition(o);
  }

  public void visitDefinition(@NotNull BallerinaDefinition o) {
    visitPsiElement(o);
  }

  public void visitErrorTypeName(@NotNull BallerinaErrorTypeName o) {
    visitPsiElement(o);
  }

  public void visitExclusiveRecordTypeName(@NotNull BallerinaExclusiveRecordTypeName o) {
    visitTypeName(o);
  }

  public void visitExprFunctionBodySpec(@NotNull BallerinaExprFunctionBodySpec o) {
    visitPsiElement(o);
  }

  public void visitExternalFunctionBody(@NotNull BallerinaExternalFunctionBody o) {
    visitPsiElement(o);
  }

  public void visitFunctionDefinition(@NotNull BallerinaFunctionDefinition o) {
    visitTopLevelDefinition(o);
  }

  public void visitFunctionDefinitionBody(@NotNull BallerinaFunctionDefinitionBody o) {
    visitPsiElement(o);
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

  public void visitGroupTypeName(@NotNull BallerinaGroupTypeName o) {
    visitTypeName(o);
  }

  public void visitImportDeclaration(@NotNull BallerinaImportDeclaration o) {
    visitPsiElement(o);
  }

  public void visitInclusiveRecordTypeName(@NotNull BallerinaInclusiveRecordTypeName o) {
    visitTypeName(o);
  }

  public void visitIntegerLiteral(@NotNull BallerinaIntegerLiteral o) {
    visitPsiElement(o);
  }

  public void visitJsonTypeName(@NotNull BallerinaJsonTypeName o) {
    visitPsiElement(o);
  }

  public void visitMapTypeName(@NotNull BallerinaMapTypeName o) {
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

  public void visitNamespaceDeclaration(@NotNull BallerinaNamespaceDeclaration o) {
    visitPsiElement(o);
  }

  public void visitNestedAnnotationAttachment(@NotNull BallerinaNestedAnnotationAttachment o) {
    visitPsiElement(o);
  }

  public void visitNestedFunctionSignature(@NotNull BallerinaNestedFunctionSignature o) {
    visitPsiElement(o);
  }

  public void visitNestedRecoverableBody(@NotNull BallerinaNestedRecoverableBody o) {
    visitPsiElement(o);
  }

  public void visitNestedRecoverableBodyContent(@NotNull BallerinaNestedRecoverableBodyContent o) {
    visitPsiElement(o);
  }

  public void visitNestedRecoverableReturnType(@NotNull BallerinaNestedRecoverableReturnType o) {
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

  public void visitObjectMethod(@NotNull BallerinaObjectMethod o) {
    visitPsiElement(o);
  }

  public void visitObjectTypeName(@NotNull BallerinaObjectTypeName o) {
    visitTypeName(o);
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

  public void visitRecoverableAnnotationContent(@NotNull BallerinaRecoverableAnnotationContent o) {
    visitPsiElement(o);
  }

  public void visitRecoverableAttachmentContent(@NotNull BallerinaRecoverableAttachmentContent o) {
    visitPsiElement(o);
  }

  public void visitRecoverableBody(@NotNull BallerinaRecoverableBody o) {
    visitPsiElement(o);
  }

  public void visitRecoverableBodyContent(@NotNull BallerinaRecoverableBodyContent o) {
    visitPsiElement(o);
  }

  public void visitRecoverableCloseRecordContent(@NotNull BallerinaRecoverableCloseRecordContent o) {
    visitPsiElement(o);
  }

  public void visitRecoverableConstantContent(@NotNull BallerinaRecoverableConstantContent o) {
    visitPsiElement(o);
  }

  public void visitRecoverableParameterContent(@NotNull BallerinaRecoverableParameterContent o) {
    visitPsiElement(o);
  }

  public void visitRecoverableReturnType(@NotNull BallerinaRecoverableReturnType o) {
    visitPsiElement(o);
  }

  public void visitRecoverableTypeBody(@NotNull BallerinaRecoverableTypeBody o) {
    visitPsiElement(o);
  }

  public void visitRecoverableTypeContent(@NotNull BallerinaRecoverableTypeContent o) {
    visitPsiElement(o);
  }

  public void visitRecoverableVarDefContent(@NotNull BallerinaRecoverableVarDefContent o) {
    visitPsiElement(o);
  }

  public void visitRecoverableVariableDefinitionContent(@NotNull BallerinaRecoverableVariableDefinitionContent o) {
    visitPsiElement(o);
  }

  public void visitReferenceTypeName(@NotNull BallerinaReferenceTypeName o) {
    visitPsiElement(o);
  }

  public void visitServiceDefinition(@NotNull BallerinaServiceDefinition o) {
    visitTopLevelDefinition(o);
  }

  public void visitServiceDefinitionBody(@NotNull BallerinaServiceDefinitionBody o) {
    visitPsiElement(o);
  }

  public void visitServiceTypeName(@NotNull BallerinaServiceTypeName o) {
    visitPsiElement(o);
  }

  public void visitSimpleTypeName(@NotNull BallerinaSimpleTypeName o) {
    visitTypeName(o);
  }

  public void visitStreamTypeName(@NotNull BallerinaStreamTypeName o) {
    visitPsiElement(o);
  }

  public void visitTableTypeName(@NotNull BallerinaTableTypeName o) {
    visitPsiElement(o);
  }

  public void visitTupleRestDescriptor(@NotNull BallerinaTupleRestDescriptor o) {
    visitPsiElement(o);
  }

  public void visitTupleTypeName(@NotNull BallerinaTupleTypeName o) {
    visitTypeName(o);
  }

  public void visitTypeDefinition(@NotNull BallerinaTypeDefinition o) {
    visitTopLevelDefinition(o);
  }

  public void visitTypeDescReferenceTypeName(@NotNull BallerinaTypeDescReferenceTypeName o) {
    visitPsiElement(o);
  }

  public void visitTypeName(@NotNull BallerinaTypeName o) {
    visitPsiElement(o);
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

  public void visitVersionPattern(@NotNull BallerinaVersionPattern o) {
    visitPsiElement(o);
  }

  public void visitXmlTypeName(@NotNull BallerinaXmlTypeName o) {
    visitPsiElement(o);
  }

  public void visitBacktickedBlock(@NotNull BallerinaBacktickedBlock o) {
    visitPsiElement(o);
  }

  public void visitDeprecateAnnotationDescriptionLine(@NotNull BallerinaDeprecateAnnotationDescriptionLine o) {
    visitPsiElement(o);
  }

  public void visitDeprecatedAnnotationDocumentation(@NotNull BallerinaDeprecatedAnnotationDocumentation o) {
    visitPsiElement(o);
  }

  public void visitDeprecatedAnnotationDocumentationLine(@NotNull BallerinaDeprecatedAnnotationDocumentationLine o) {
    visitPsiElement(o);
  }

  public void visitDeprecatedParametersDocumentation(@NotNull BallerinaDeprecatedParametersDocumentation o) {
    visitPsiElement(o);
  }

  public void visitDeprecatedParametersDocumentationLine(@NotNull BallerinaDeprecatedParametersDocumentationLine o) {
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

  public void visitTopLevelDefinition(@NotNull BallerinaTopLevelDefinition o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
