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
package io.ballerina.plugins.idea.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.*;
import static io.ballerina.plugins.idea.parser.BallerinaParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class BallerinaParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return CompilationUnit(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(ARRAY_TYPE_NAME, DISTINCT_SIMPLE_TYPE_NAME, EXCLUSIVE_RECORD_TYPE_NAME, GROUP_TYPE_NAME,
      INCLUSIVE_RECORD_TYPE_NAME, INTERSECTION_TYPE_NAME, NULLABLE_TYPE_NAME, OBJECT_TYPE_NAME,
      TABLE_TYPE_NAME, TUPLE_TYPE_NAME, TYPE_NAME, UNION_TYPE_NAME),
  };

  /* ********************************************************** */
  // as identifier
  public static boolean Alias(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Alias")) return false;
    if (!nextTokenIs(b, AS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ALIAS, null);
    r = consumeTokens(b, 1, AS, IDENTIFIER);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // AT NameReference RecoverableBody?
  public static boolean AnnotationAttachment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationAttachment")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ANNOTATION_ATTACHMENT, null);
    r = consumeToken(b, AT);
    p = r; // pin = 1
    r = r && report_error_(b, NameReference(b, l + 1));
    r = p && AnnotationAttachment_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // RecoverableBody?
  private static boolean AnnotationAttachment_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationAttachment_2")) return false;
    RecoverableBody(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // public? const? annotation RecoverableAnnotationContent SEMICOLON
  public static boolean AnnotationDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ANNOTATION_DEFINITION, "<annotation definition>");
    r = AnnotationDefinition_0(b, l + 1);
    r = r && AnnotationDefinition_1(b, l + 1);
    r = r && consumeToken(b, ANNOTATION);
    p = r; // pin = 3
    r = r && report_error_(b, RecoverableAnnotationContent(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // public?
  private static boolean AnnotationDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  // const?
  private static boolean AnnotationDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_1")) return false;
    consumeToken(b, CONST);
    return true;
  }

  /* ********************************************************** */
  // !(SEMICOLON)
  static boolean AnnotationDefinitionRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinitionRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean AnyIdentifierName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnyIdentifierName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, ANY_IDENTIFIER_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // MapTypeName | FutureTypeName | XmlTypeName | JsonTypeName | StreamTypeName | ServiceTypeName | TypeDescReferenceTypeName | ErrorTypeName | FunctionTypeName
  public static boolean BuiltInReferenceTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BuiltInReferenceTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BUILT_IN_REFERENCE_TYPE_NAME, "<built in reference type name>");
    r = MapTypeName(b, l + 1);
    if (!r) r = FutureTypeName(b, l + 1);
    if (!r) r = XmlTypeName(b, l + 1);
    if (!r) r = JsonTypeName(b, l + 1);
    if (!r) r = StreamTypeName(b, l + 1);
    if (!r) r = ServiceTypeName(b, l + 1);
    if (!r) r = TypeDescReferenceTypeName(b, l + 1);
    if (!r) r = ErrorTypeName(b, l + 1);
    if (!r) r = FunctionTypeName(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // record LEFT_CLOSED_RECORD_DELIMITER RecoverableCloseRecordContent RIGHT_CLOSED_RECORD_DELIMITER
  public static boolean CloseRecordTypeBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CloseRecordTypeBody")) return false;
    if (!nextTokenIs(b, RECORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CLOSE_RECORD_TYPE_BODY, null);
    r = consumeTokens(b, 2, RECORD, LEFT_CLOSED_RECORD_DELIMITER);
    p = r; // pin = 2
    r = r && report_error_(b, RecoverableCloseRecordContent(b, l + 1));
    r = p && consumeToken(b, RIGHT_CLOSED_RECORD_DELIMITER) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (ImportDeclaration | NamespaceDeclaration)* (DefinitionWithoutAnnotationAttachments |  DefinitionWithMultipleAnnotationAttachments | DefinitionWithSingleAnnotationAttachment)* <<eof>>
  static boolean CompilationUnit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompilationUnit")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = CompilationUnit_0(b, l + 1);
    r = r && CompilationUnit_1(b, l + 1);
    r = r && eof(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (ImportDeclaration | NamespaceDeclaration)*
  private static boolean CompilationUnit_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompilationUnit_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!CompilationUnit_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CompilationUnit_0", c)) break;
    }
    return true;
  }

  // ImportDeclaration | NamespaceDeclaration
  private static boolean CompilationUnit_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompilationUnit_0_0")) return false;
    boolean r;
    r = ImportDeclaration(b, l + 1);
    if (!r) r = NamespaceDeclaration(b, l + 1);
    return r;
  }

  // (DefinitionWithoutAnnotationAttachments |  DefinitionWithMultipleAnnotationAttachments | DefinitionWithSingleAnnotationAttachment)*
  private static boolean CompilationUnit_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompilationUnit_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!CompilationUnit_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CompilationUnit_1", c)) break;
    }
    return true;
  }

  // DefinitionWithoutAnnotationAttachments |  DefinitionWithMultipleAnnotationAttachments | DefinitionWithSingleAnnotationAttachment
  private static boolean CompilationUnit_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompilationUnit_1_0")) return false;
    boolean r;
    r = DefinitionWithoutAnnotationAttachments(b, l + 1);
    if (!r) r = DefinitionWithMultipleAnnotationAttachments(b, l + 1);
    if (!r) r = DefinitionWithSingleAnnotationAttachment(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // PackageName (DOT PackageName)*
  public static boolean CompletePackageName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompletePackageName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, COMPLETE_PACKAGE_NAME, null);
    r = PackageName(b, l + 1);
    p = r; // pin = 1
    r = r && CompletePackageName_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (DOT PackageName)*
  private static boolean CompletePackageName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompletePackageName_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!CompletePackageName_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CompletePackageName_1", c)) break;
    }
    return true;
  }

  // DOT PackageName
  private static boolean CompletePackageName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompletePackageName_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, DOT);
    p = r; // pin = 1
    r = r && PackageName(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // public? const RecoverableConstantContent SEMICOLON
  public static boolean ConstantDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstantDefinition")) return false;
    if (!nextTokenIs(b, "<constant definition>", CONST, PUBLIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONSTANT_DEFINITION, "<constant definition>");
    r = ConstantDefinition_0(b, l + 1);
    r = r && consumeToken(b, CONST);
    p = r; // pin = 2
    r = r && report_error_(b, RecoverableConstantContent(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // public?
  private static boolean ConstantDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstantDefinition_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  /* ********************************************************** */
  // !(SEMICOLON)
  static boolean ConstantDefinitionRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstantDefinitionRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TypeDefinition | ServiceDefinition | FunctionDefinition | AnnotationDefinition | ConstantDefinition | GlobalVariableDefinition | EnumDefinition
  public static boolean Definition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Definition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DEFINITION, "<definition>");
    r = TypeDefinition(b, l + 1);
    if (!r) r = ServiceDefinition(b, l + 1);
    if (!r) r = FunctionDefinition(b, l + 1);
    if (!r) r = AnnotationDefinition(b, l + 1);
    if (!r) r = ConstantDefinition(b, l + 1);
    if (!r) r = GlobalVariableDefinition(b, l + 1);
    if (!r) r = EnumDefinition(b, l + 1);
    exit_section_(b, l, m, r, false, TopLevelDefinitionRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // documentationString? AnnotationAttachment AnnotationAttachment+ Definition
  public static boolean DefinitionWithMultipleAnnotationAttachments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithMultipleAnnotationAttachments")) return false;
    if (!nextTokenIs(b, "<definition with multiple annotation attachments>", AT, MARKDOWN_DOCUMENTATION_LINE_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DEFINITION, "<definition with multiple annotation attachments>");
    r = DefinitionWithMultipleAnnotationAttachments_0(b, l + 1);
    r = r && AnnotationAttachment(b, l + 1);
    r = r && DefinitionWithMultipleAnnotationAttachments_2(b, l + 1);
    p = r; // pin = 3
    r = r && Definition(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // documentationString?
  private static boolean DefinitionWithMultipleAnnotationAttachments_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithMultipleAnnotationAttachments_0")) return false;
    documentationString(b, l + 1);
    return true;
  }

  // AnnotationAttachment+
  private static boolean DefinitionWithMultipleAnnotationAttachments_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithMultipleAnnotationAttachments_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = AnnotationAttachment(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "DefinitionWithMultipleAnnotationAttachments_2", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // documentationString? AnnotationAttachment Definition
  public static boolean DefinitionWithSingleAnnotationAttachment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithSingleAnnotationAttachment")) return false;
    if (!nextTokenIs(b, "<definition with single annotation attachment>", AT, MARKDOWN_DOCUMENTATION_LINE_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DEFINITION, "<definition with single annotation attachment>");
    r = DefinitionWithSingleAnnotationAttachment_0(b, l + 1);
    r = r && AnnotationAttachment(b, l + 1);
    p = r; // pin = 2
    r = r && Definition(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // documentationString?
  private static boolean DefinitionWithSingleAnnotationAttachment_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithSingleAnnotationAttachment_0")) return false;
    documentationString(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // documentationString? Definition
  public static boolean DefinitionWithoutAnnotationAttachments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithoutAnnotationAttachments")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, DEFINITION, "<definition without annotation attachments>");
    r = DefinitionWithoutAnnotationAttachments_0(b, l + 1);
    r = r && Definition(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // documentationString?
  private static boolean DefinitionWithoutAnnotationAttachments_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefinitionWithoutAnnotationAttachments_0")) return false;
    documentationString(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // LEFT_BRACE RIGHT_BRACE
  public static boolean EmptyEnumBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EmptyEnumBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LEFT_BRACE, RIGHT_BRACE);
    exit_section_(b, m, EMPTY_ENUM_BODY, r);
    return r;
  }

  /* ********************************************************** */
  // MultiMemberEnumBody | SingleMemberEnumBody | EmptyEnumBody
  public static boolean EnumBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = MultiMemberEnumBody(b, l + 1);
    if (!r) r = SingleMemberEnumBody(b, l + 1);
    if (!r) r = EmptyEnumBody(b, l + 1);
    exit_section_(b, m, ENUM_BODY, r);
    return r;
  }

  /* ********************************************************** */
  // !(COMMA|RIGHT_BRACE)
  static boolean EnumContentRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumContentRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !EnumContentRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // COMMA|RIGHT_BRACE
  private static boolean EnumContentRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumContentRecover_0")) return false;
    boolean r;
    r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, RIGHT_BRACE);
    return r;
  }

  /* ********************************************************** */
  // public? enum identifier EnumBody
  public static boolean EnumDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENUM_DEFINITION, "<enum definition>");
    r = EnumDefinition_0(b, l + 1);
    r = r && consumeTokens(b, 1, ENUM, IDENTIFIER);
    p = r; // pin = 2
    r = r && EnumBody(b, l + 1);
    exit_section_(b, l, m, r, p, RecoverableBodyContentRecover_parser_);
    return r || p;
  }

  // public?
  private static boolean EnumDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumDefinition_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  /* ********************************************************** */
  // documentationString? AnnotationAttachment* identifier RecoverableEnumContent
  public static boolean EnumMember(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumMember")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENUM_MEMBER, "<enum member>");
    r = EnumMember_0(b, l + 1);
    r = r && EnumMember_1(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    p = r; // pin = 3
    r = r && RecoverableEnumContent(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // documentationString?
  private static boolean EnumMember_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumMember_0")) return false;
    documentationString(b, l + 1);
    return true;
  }

  // AnnotationAttachment*
  private static boolean EnumMember_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumMember_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "EnumMember_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // error (LT (TypeName | MUL) GT)?
  public static boolean ErrorTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorTypeName")) return false;
    if (!nextTokenIs(b, ERROR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ERROR_TYPE_NAME, null);
    r = consumeToken(b, ERROR);
    p = r; // pin = 1
    r = r && ErrorTypeName_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LT (TypeName | MUL) GT)?
  private static boolean ErrorTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorTypeName_1")) return false;
    ErrorTypeName_1_0(b, l + 1);
    return true;
  }

  // LT (TypeName | MUL) GT
  private static boolean ErrorTypeName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorTypeName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && ErrorTypeName_1_0_1(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  // TypeName | MUL
  private static boolean ErrorTypeName_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorTypeName_1_0_1")) return false;
    boolean r;
    r = TypeName(b, l + 1, -1);
    if (!r) r = consumeToken(b, MUL);
    return r;
  }

  /* ********************************************************** */
  // !(SEMICOLON)
  static boolean ExprFuncBodyRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExprFuncBodyRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // EQUAL_GT identifier
  public static boolean ExprFunctionBodySpec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExprFunctionBodySpec")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXPR_FUNCTION_BODY_SPEC, "<expr function body spec>");
    r = consumeTokens(b, 1, EQUAL_GT, IDENTIFIER);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, ExprFuncBodyRecover_parser_);
    return r || p;
  }

  /* ********************************************************** */
  // !(SEMICOLON)
  static boolean ExternalFuncBodyRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExternalFuncBodyRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ASSIGN AnnotationAttachment* external
  public static boolean ExternalFunctionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExternalFunctionBody")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXTERNAL_FUNCTION_BODY, "<external function body>");
    r = consumeToken(b, ASSIGN);
    p = r; // pin = 1
    r = r && report_error_(b, ExternalFunctionBody_1(b, l + 1));
    r = p && consumeToken(b, EXTERNAL) && r;
    exit_section_(b, l, m, r, p, ExternalFuncBodyRecover_parser_);
    return r || p;
  }

  // AnnotationAttachment*
  private static boolean ExternalFunctionBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExternalFunctionBody_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ExternalFunctionBody_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // (public|private)? remote? transactional? function AnyIdentifierName FunctionSignature FunctionDefinitionBody
  public static boolean FunctionDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_DEFINITION, "<function definition>");
    r = FunctionDefinition_0(b, l + 1);
    r = r && FunctionDefinition_1(b, l + 1);
    r = r && FunctionDefinition_2(b, l + 1);
    r = r && consumeToken(b, FUNCTION);
    p = r; // pin = 4
    r = r && report_error_(b, AnyIdentifierName(b, l + 1));
    r = p && report_error_(b, FunctionSignature(b, l + 1)) && r;
    r = p && FunctionDefinitionBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (public|private)?
  private static boolean FunctionDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_0")) return false;
    FunctionDefinition_0_0(b, l + 1);
    return true;
  }

  // public|private
  private static boolean FunctionDefinition_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_0_0")) return false;
    boolean r;
    r = consumeToken(b, PUBLIC);
    if (!r) r = consumeToken(b, PRIVATE);
    return r;
  }

  // remote?
  private static boolean FunctionDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_1")) return false;
    consumeToken(b, REMOTE);
    return true;
  }

  // transactional?
  private static boolean FunctionDefinition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_2")) return false;
    consumeToken(b, TRANSACTIONAL);
    return true;
  }

  /* ********************************************************** */
  // (ExprFunctionBodySpec SEMICOLON) | (ExternalFunctionBody SEMICOLON) | RecoverableBody
  public static boolean FunctionDefinitionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinitionBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_DEFINITION_BODY, "<function definition body>");
    r = FunctionDefinitionBody_0(b, l + 1);
    if (!r) r = FunctionDefinitionBody_1(b, l + 1);
    if (!r) r = RecoverableBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ExprFunctionBodySpec SEMICOLON
  private static boolean FunctionDefinitionBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinitionBody_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ExprFunctionBodySpec(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // ExternalFunctionBody SEMICOLON
  private static boolean FunctionDefinitionBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinitionBody_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ExternalFunctionBody(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_PARENTHESIS RecoverableParameterContent RIGHT_PARENTHESIS returns? RecoverableReturnType
  public static boolean FunctionSignature(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionSignature")) return false;
    if (!nextTokenIs(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_SIGNATURE, null);
    r = consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, RecoverableParameterContent(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RIGHT_PARENTHESIS)) && r;
    r = p && report_error_(b, FunctionSignature_3(b, l + 1)) && r;
    r = p && RecoverableReturnType(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // returns?
  private static boolean FunctionSignature_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionSignature_3")) return false;
    consumeToken(b, RETURNS);
    return true;
  }

  /* ********************************************************** */
  // !(RIGHT_PARENTHESIS)
  static boolean FunctionSignatureRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionSignatureRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // function
  public static boolean FunctionTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionTypeName")) return false;
    if (!nextTokenIs(b, FUNCTION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FUNCTION);
    exit_section_(b, m, FUNCTION_TYPE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // future (LT TypeName GT)?
  public static boolean FutureTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FutureTypeName")) return false;
    if (!nextTokenIs(b, FUTURE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUTURE_TYPE_NAME, null);
    r = consumeToken(b, FUTURE);
    p = r; // pin = 1
    r = r && FutureTypeName_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LT TypeName GT)?
  private static boolean FutureTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FutureTypeName_1")) return false;
    FutureTypeName_1_0(b, l + 1);
    return true;
  }

  // LT TypeName GT
  private static boolean FutureTypeName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FutureTypeName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // varDefinition | listenerDefinition | typedVariableDefinition
  public static boolean GlobalVariableDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GlobalVariableDefinition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, GLOBAL_VARIABLE_DEFINITION, "<global variable definition>");
    r = varDefinition(b, l + 1);
    if (!r) r = listenerDefinition(b, l + 1);
    if (!r) r = typedVariableDefinition(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(SEMICOLON)
  static boolean GlobalVariableDefinitionRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GlobalVariableDefinitionRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // import (OrgName DIV)? CompletePackageName PackageVersion? Alias? SEMICOLON
  public static boolean ImportDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImportDeclaration")) return false;
    if (!nextTokenIs(b, IMPORT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IMPORT_DECLARATION, null);
    r = consumeToken(b, IMPORT);
    p = r; // pin = 1
    r = r && report_error_(b, ImportDeclaration_1(b, l + 1));
    r = p && report_error_(b, CompletePackageName(b, l + 1)) && r;
    r = p && report_error_(b, ImportDeclaration_3(b, l + 1)) && r;
    r = p && report_error_(b, ImportDeclaration_4(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (OrgName DIV)?
  private static boolean ImportDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImportDeclaration_1")) return false;
    ImportDeclaration_1_0(b, l + 1);
    return true;
  }

  // OrgName DIV
  private static boolean ImportDeclaration_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImportDeclaration_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = OrgName(b, l + 1);
    r = r && consumeToken(b, DIV);
    exit_section_(b, m, null, r);
    return r;
  }

  // PackageVersion?
  private static boolean ImportDeclaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImportDeclaration_3")) return false;
    PackageVersion(b, l + 1);
    return true;
  }

  // Alias?
  private static boolean ImportDeclaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImportDeclaration_4")) return false;
    Alias(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // DECIMAL_INTEGER_LITERAL | HEX_INTEGER_LITERAL
  public static boolean IntegerLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IntegerLiteral")) return false;
    if (!nextTokenIs(b, "<integer literal>", DECIMAL_INTEGER_LITERAL, HEX_INTEGER_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INTEGER_LITERAL, "<integer literal>");
    r = consumeToken(b, DECIMAL_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, HEX_INTEGER_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // json
  public static boolean JsonTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JsonTypeName")) return false;
    if (!nextTokenIs(b, JSON)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, JSON);
    exit_section_(b, m, JSON_TYPE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // map LT TypeName GT
  public static boolean MapTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MapTypeName")) return false;
    if (!nextTokenIs(b, MAP)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MAP_TYPE_NAME, null);
    r = consumeTokens(b, 1, MAP, LT);
    p = r; // pin = 1
    r = r && report_error_(b, TypeName(b, l + 1, -1));
    r = p && consumeToken(b, GT) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // documentationString? NestedAnnotationAttachment* (public|private)? (remote|resource)? function AnyIdentifierName NestedFunctionSignature SEMICOLON
  public static boolean MethodDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDeclaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, METHOD_DECLARATION, "<method declaration>");
    r = MethodDeclaration_0(b, l + 1);
    r = r && MethodDeclaration_1(b, l + 1);
    r = r && MethodDeclaration_2(b, l + 1);
    r = r && MethodDeclaration_3(b, l + 1);
    r = r && consumeToken(b, FUNCTION);
    r = r && AnyIdentifierName(b, l + 1);
    r = r && NestedFunctionSignature(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // documentationString?
  private static boolean MethodDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDeclaration_0")) return false;
    documentationString(b, l + 1);
    return true;
  }

  // NestedAnnotationAttachment*
  private static boolean MethodDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDeclaration_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!NestedAnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "MethodDeclaration_1", c)) break;
    }
    return true;
  }

  // (public|private)?
  private static boolean MethodDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDeclaration_2")) return false;
    MethodDeclaration_2_0(b, l + 1);
    return true;
  }

  // public|private
  private static boolean MethodDeclaration_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDeclaration_2_0")) return false;
    boolean r;
    r = consumeToken(b, PUBLIC);
    if (!r) r = consumeToken(b, PRIVATE);
    return r;
  }

  // (remote|resource)?
  private static boolean MethodDeclaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDeclaration_3")) return false;
    MethodDeclaration_3_0(b, l + 1);
    return true;
  }

  // remote|resource
  private static boolean MethodDeclaration_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDeclaration_3_0")) return false;
    boolean r;
    r = consumeToken(b, REMOTE);
    if (!r) r = consumeToken(b, RESOURCE);
    return r;
  }

  /* ********************************************************** */
  // documentationString? NestedAnnotationAttachment* (public|private)? (remote|resource)? function AnyIdentifierName NestedFunctionSignature NestedRecoverableBody
  public static boolean MethodDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDefinition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, METHOD_DEFINITION, "<method definition>");
    r = MethodDefinition_0(b, l + 1);
    r = r && MethodDefinition_1(b, l + 1);
    r = r && MethodDefinition_2(b, l + 1);
    r = r && MethodDefinition_3(b, l + 1);
    r = r && consumeToken(b, FUNCTION);
    r = r && AnyIdentifierName(b, l + 1);
    r = r && NestedFunctionSignature(b, l + 1);
    r = r && NestedRecoverableBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // documentationString?
  private static boolean MethodDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDefinition_0")) return false;
    documentationString(b, l + 1);
    return true;
  }

  // NestedAnnotationAttachment*
  private static boolean MethodDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDefinition_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!NestedAnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "MethodDefinition_1", c)) break;
    }
    return true;
  }

  // (public|private)?
  private static boolean MethodDefinition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDefinition_2")) return false;
    MethodDefinition_2_0(b, l + 1);
    return true;
  }

  // public|private
  private static boolean MethodDefinition_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDefinition_2_0")) return false;
    boolean r;
    r = consumeToken(b, PUBLIC);
    if (!r) r = consumeToken(b, PRIVATE);
    return r;
  }

  // (remote|resource)?
  private static boolean MethodDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDefinition_3")) return false;
    MethodDefinition_3_0(b, l + 1);
    return true;
  }

  // remote|resource
  private static boolean MethodDefinition_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDefinition_3_0")) return false;
    boolean r;
    r = consumeToken(b, REMOTE);
    if (!r) r = consumeToken(b, RESOURCE);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE EnumMember (COMMA EnumMember)+ RIGHT_BRACE
  public static boolean MultiMemberEnumBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MultiMemberEnumBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MULTI_MEMBER_ENUM_BODY, null);
    r = consumeToken(b, LEFT_BRACE);
    r = r && EnumMember(b, l + 1);
    r = r && MultiMemberEnumBody_2(b, l + 1);
    p = r; // pin = 3
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA EnumMember)+
  private static boolean MultiMemberEnumBody_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MultiMemberEnumBody_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = MultiMemberEnumBody_2_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!MultiMemberEnumBody_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "MultiMemberEnumBody_2", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA EnumMember
  private static boolean MultiMemberEnumBody_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MultiMemberEnumBody_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && EnumMember(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PackageReference identifier | identifier
  public static boolean NameReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NameReference")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = NameReference_0(b, l + 1);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, NAME_REFERENCE, r);
    return r;
  }

  // PackageReference identifier
  private static boolean NameReference_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NameReference_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = PackageReference(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // xmlns QUOTED_STRING_LITERAL (as identifier)? SEMICOLON
  public static boolean NamespaceDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamespaceDeclaration")) return false;
    if (!nextTokenIs(b, XMLNS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NAMESPACE_DECLARATION, null);
    r = consumeTokens(b, 1, XMLNS, QUOTED_STRING_LITERAL);
    p = r; // pin = 1
    r = r && report_error_(b, NamespaceDeclaration_2(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (as identifier)?
  private static boolean NamespaceDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamespaceDeclaration_2")) return false;
    NamespaceDeclaration_2_0(b, l + 1);
    return true;
  }

  // as identifier
  private static boolean NamespaceDeclaration_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamespaceDeclaration_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, AS, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // AT NameReference NestedRecoverableBody?
  public static boolean NestedAnnotationAttachment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NestedAnnotationAttachment")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NESTED_ANNOTATION_ATTACHMENT, null);
    r = consumeToken(b, AT);
    p = r; // pin = 1
    r = r && report_error_(b, NameReference(b, l + 1));
    r = p && NestedAnnotationAttachment_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // NestedRecoverableBody?
  private static boolean NestedAnnotationAttachment_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NestedAnnotationAttachment_2")) return false;
    NestedRecoverableBody(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // LEFT_PARENTHESIS RecoverableParameterContent RIGHT_PARENTHESIS returns? NestedRecoverableReturnType
  public static boolean NestedFunctionSignature(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NestedFunctionSignature")) return false;
    if (!nextTokenIs(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NESTED_FUNCTION_SIGNATURE, null);
    r = consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, RecoverableParameterContent(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RIGHT_PARENTHESIS)) && r;
    r = p && report_error_(b, NestedFunctionSignature_3(b, l + 1)) && r;
    r = p && NestedRecoverableReturnType(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // returns?
  private static boolean NestedFunctionSignature_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NestedFunctionSignature_3")) return false;
    consumeToken(b, RETURNS);
    return true;
  }

  /* ********************************************************** */
  // NESTED_LEFT_BRACE NestedRecoverableBodyContent NESTED_RIGHT_BRACE
  public static boolean NestedRecoverableBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NestedRecoverableBody")) return false;
    if (!nextTokenIs(b, NESTED_LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NESTED_RECOVERABLE_BODY, null);
    r = consumeToken(b, NESTED_LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, NestedRecoverableBodyContent(b, l + 1));
    r = p && consumeToken(b, NESTED_RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // identifier
  public static boolean NestedRecoverableBodyContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NestedRecoverableBodyContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NESTED_RECOVERABLE_BODY_CONTENT, "<nested recoverable body content>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, NestedRecoverableBodyContentRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // !(NESTED_RIGHT_BRACE)
  static boolean NestedRecoverableBodyContentRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NestedRecoverableBodyContentRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, NESTED_RIGHT_BRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean NestedRecoverableReturnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NestedRecoverableReturnType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NESTED_RECOVERABLE_RETURN_TYPE, "<nested recoverable return type>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, NestedReturnParamRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // !(SEMICOLON | NESTED_LEFT_BRACE)
  static boolean NestedReturnParamRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NestedReturnParamRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !NestedReturnParamRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SEMICOLON | NESTED_LEFT_BRACE
  private static boolean NestedReturnParamRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NestedReturnParamRecover_0")) return false;
    boolean r;
    r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, NESTED_LEFT_BRACE);
    return r;
  }

  /* ********************************************************** */
  // LEFT_PARENTHESIS RIGHT_PARENTHESIS
  public static boolean NilLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NilLiteral")) return false;
    if (!nextTokenIs(b, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LEFT_PARENTHESIS, RIGHT_PARENTHESIS);
    exit_section_(b, m, NIL_LITERAL, r);
    return r;
  }

  /* ********************************************************** */
  public static boolean ObjectBody(PsiBuilder b, int l) {
    Marker m = enter_section_(b, l, _NONE_, OBJECT_BODY, null);
    exit_section_(b, l, m, true, false, ObjectBodyRecover_parser_);
    return true;
  }

  /* ********************************************************** */
  // (ObjectMethod | ObjectFieldDefinition | TypeReference)*
  public static boolean ObjectBodyContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectBodyContent")) return false;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_BODY_CONTENT, "<object body content>");
    while (true) {
      int c = current_position_(b);
      if (!ObjectBodyContent_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectBodyContent", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  // ObjectMethod | ObjectFieldDefinition | TypeReference
  private static boolean ObjectBodyContent_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectBodyContent_0")) return false;
    boolean r;
    r = ObjectMethod(b, l + 1);
    if (!r) r = ObjectFieldDefinition(b, l + 1);
    if (!r) r = TypeReference(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // !(RIGHT_BRACE)
  static boolean ObjectBodyRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectBodyRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RIGHT_BRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // documentationString? AnnotationAttachment* (public | private)? readonly? TypeName identifier ObjectFieldDefinitionContent (COMMA | SEMICOLON)
  public static boolean ObjectFieldDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_FIELD_DEFINITION, "<object field definition>");
    r = ObjectFieldDefinition_0(b, l + 1);
    r = r && ObjectFieldDefinition_1(b, l + 1);
    r = r && ObjectFieldDefinition_2(b, l + 1);
    r = r && ObjectFieldDefinition_3(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, IDENTIFIER);
    p = r; // pin = 6
    r = r && report_error_(b, ObjectFieldDefinitionContent(b, l + 1));
    r = p && ObjectFieldDefinition_7(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // documentationString?
  private static boolean ObjectFieldDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_0")) return false;
    documentationString(b, l + 1);
    return true;
  }

  // AnnotationAttachment*
  private static boolean ObjectFieldDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectFieldDefinition_1", c)) break;
    }
    return true;
  }

  // (public | private)?
  private static boolean ObjectFieldDefinition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_2")) return false;
    ObjectFieldDefinition_2_0(b, l + 1);
    return true;
  }

  // public | private
  private static boolean ObjectFieldDefinition_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_2_0")) return false;
    boolean r;
    r = consumeToken(b, PUBLIC);
    if (!r) r = consumeToken(b, PRIVATE);
    return r;
  }

  // readonly?
  private static boolean ObjectFieldDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_3")) return false;
    consumeToken(b, READONLY);
    return true;
  }

  // COMMA | SEMICOLON
  private static boolean ObjectFieldDefinition_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_7")) return false;
    boolean r;
    r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, SEMICOLON);
    return r;
  }

  /* ********************************************************** */
  // any
  public static boolean ObjectFieldDefinitionContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinitionContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_FIELD_DEFINITION_CONTENT, "<object field definition content>");
    r = consumeToken(b, ANY);
    exit_section_(b, l, m, r, false, ObjectFieldDefinitionContentRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // !(COMMA | SEMICOLON)
  static boolean ObjectFieldDefinitionContentRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinitionContentRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !ObjectFieldDefinitionContentRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // COMMA | SEMICOLON
  private static boolean ObjectFieldDefinitionContentRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinitionContentRecover_0")) return false;
    boolean r;
    r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, SEMICOLON);
    return r;
  }

  /* ********************************************************** */
  // MethodDeclaration | MethodDefinition
  public static boolean ObjectMethod(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectMethod")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_METHOD, "<object method>");
    r = MethodDeclaration(b, l + 1);
    if (!r) r = MethodDefinition(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ((client? abstract) | (abstract? client?)) object LEFT_BRACE ObjectBodyContent RIGHT_BRACE
  public static boolean ObjectTypeBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeBody")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_TYPE_BODY, "<object type body>");
    r = ObjectTypeBody_0(b, l + 1);
    r = r && consumeTokens(b, 1, OBJECT, LEFT_BRACE);
    p = r; // pin = 2
    r = r && report_error_(b, ObjectBodyContent(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (client? abstract) | (abstract? client?)
  private static boolean ObjectTypeBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeBody_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ObjectTypeBody_0_0(b, l + 1);
    if (!r) r = ObjectTypeBody_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // client? abstract
  private static boolean ObjectTypeBody_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeBody_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ObjectTypeBody_0_0_0(b, l + 1);
    r = r && consumeToken(b, ABSTRACT);
    exit_section_(b, m, null, r);
    return r;
  }

  // client?
  private static boolean ObjectTypeBody_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeBody_0_0_0")) return false;
    consumeToken(b, CLIENT);
    return true;
  }

  // abstract? client?
  private static boolean ObjectTypeBody_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeBody_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ObjectTypeBody_0_1_0(b, l + 1);
    r = r && ObjectTypeBody_0_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // abstract?
  private static boolean ObjectTypeBody_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeBody_0_1_0")) return false;
    consumeToken(b, ABSTRACT);
    return true;
  }

  // client?
  private static boolean ObjectTypeBody_0_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeBody_0_1_1")) return false;
    consumeToken(b, CLIENT);
    return true;
  }

  /* ********************************************************** */
  // record LEFT_BRACE RecoverableOpenRecordContent RIGHT_BRACE
  public static boolean OpenRecordTypeBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OpenRecordTypeBody")) return false;
    if (!nextTokenIs(b, RECORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OPEN_RECORD_TYPE_BODY, null);
    r = consumeTokens(b, 2, RECORD, LEFT_BRACE);
    p = r; // pin = 2
    r = r && report_error_(b, RecoverableOpenRecordContent(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // identifier
  public static boolean OrgName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OrgName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, ORG_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // (identifier|QUOTED_STRING_LITERAL|error) RecoverableTypeContent
  public static boolean OtherTypeBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OtherTypeBody")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OTHER_TYPE_BODY, "<other type body>");
    r = OtherTypeBody_0(b, l + 1);
    p = r; // pin = 1
    r = r && RecoverableTypeContent(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // identifier|QUOTED_STRING_LITERAL|error
  private static boolean OtherTypeBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OtherTypeBody_0")) return false;
    boolean r;
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, QUOTED_STRING_LITERAL);
    if (!r) r = consumeToken(b, ERROR);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean PackageName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PackageName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, PACKAGE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // identifier COLON
  public static boolean PackageReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PackageReference")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 2, IDENTIFIER, COLON);
    exit_section_(b, m, PACKAGE_REFERENCE, r);
    return r;
  }

  /* ********************************************************** */
  // version VersionPattern
  public static boolean PackageVersion(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PackageVersion")) return false;
    if (!nextTokenIs(b, VERSION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PACKAGE_VERSION, null);
    r = consumeToken(b, VERSION);
    p = r; // pin = 1
    r = r && VersionPattern(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // identifier
  public static boolean RecoverableAnnotationContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableAnnotationContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECOVERABLE_ANNOTATION_CONTENT, "<recoverable annotation content>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, AnnotationDefinitionRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean RecoverableAttachmentContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableAttachmentContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECOVERABLE_ATTACHMENT_CONTENT, "<recoverable attachment content>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, ServiceDefinitionRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE RecoverableBodyContent RIGHT_BRACE
  public static boolean RecoverableBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECOVERABLE_BODY, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, RecoverableBodyContent(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // identifier
  public static boolean RecoverableBodyContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableBodyContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECOVERABLE_BODY_CONTENT, "<recoverable body content>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, RecoverableBodyContentRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // !(RIGHT_BRACE)
  static boolean RecoverableBodyContentRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableBodyContentRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RIGHT_BRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // any
  public static boolean RecoverableCloseRecordContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableCloseRecordContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECOVERABLE_CLOSE_RECORD_CONTENT, "<recoverable close record content>");
    r = consumeToken(b, ANY);
    exit_section_(b, l, m, r, false, RecoverableCloseRecordContentRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // !(RIGHT_CLOSED_RECORD_DELIMITER)
  static boolean RecoverableCloseRecordContentRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableCloseRecordContentRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RIGHT_CLOSED_RECORD_DELIMITER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean RecoverableConstantContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableConstantContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECOVERABLE_CONSTANT_CONTENT, "<recoverable constant content>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, ConstantDefinitionRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // ASSIGN
  public static boolean RecoverableEnumContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableEnumContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECOVERABLE_ENUM_CONTENT, "<recoverable enum content>");
    r = consumeToken(b, ASSIGN);
    exit_section_(b, l, m, r, false, EnumContentRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // any
  public static boolean RecoverableOpenRecordContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableOpenRecordContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECOVERABLE_OPEN_RECORD_CONTENT, "<recoverable open record content>");
    r = consumeToken(b, ANY);
    exit_section_(b, l, m, r, false, RecoverableOpenRecordContentRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // !(RIGHT_BRACE)
  static boolean RecoverableOpenRecordContentRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableOpenRecordContentRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RIGHT_BRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean RecoverableParameterContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableParameterContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECOVERABLE_PARAMETER_CONTENT, "<recoverable parameter content>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, FunctionSignatureRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean RecoverableReturnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableReturnType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECOVERABLE_RETURN_TYPE, "<recoverable return type>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, ReturnParamRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // any
  public static boolean RecoverableTypeContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableTypeContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECOVERABLE_TYPE_CONTENT, "<recoverable type content>");
    r = consumeToken(b, ANY);
    exit_section_(b, l, m, r, false, SimpleTypeContentRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean RecoverableVarDefContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableVarDefContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECOVERABLE_VAR_DEF_CONTENT, "<recoverable var def content>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, VarDefExpressionRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean RecoverableVariableDefinitionContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecoverableVariableDefinitionContent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECOVERABLE_VARIABLE_DEFINITION_CONTENT, "<recoverable variable definition content>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, GlobalVariableDefinitionRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // BuiltInReferenceTypeName | UserDefineTypeName
  public static boolean ReferenceTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReferenceTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REFERENCE_TYPE_NAME, "<reference type name>");
    r = BuiltInReferenceTypeName(b, l + 1);
    if (!r) r = UserDefineTypeName(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(EQUAL_GT | ASSIGN | LEFT_BRACE)
  static boolean ReturnParamRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReturnParamRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !ReturnParamRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // EQUAL_GT | ASSIGN | LEFT_BRACE
  private static boolean ReturnParamRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReturnParamRecover_0")) return false;
    boolean r;
    r = consumeToken(b, EQUAL_GT);
    if (!r) r = consumeToken(b, ASSIGN);
    if (!r) r = consumeToken(b, LEFT_BRACE);
    return r;
  }

  /* ********************************************************** */
  // service identifier? on RecoverableAttachmentContent ServiceDefinitionBody
  public static boolean ServiceDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceDefinition")) return false;
    if (!nextTokenIs(b, SERVICE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SERVICE_DEFINITION, null);
    r = consumeToken(b, SERVICE);
    r = r && ServiceDefinition_1(b, l + 1);
    r = r && consumeToken(b, ON);
    p = r; // pin = 3
    r = r && report_error_(b, RecoverableAttachmentContent(b, l + 1));
    r = p && ServiceDefinitionBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // identifier?
  private static boolean ServiceDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceDefinition_1")) return false;
    consumeToken(b, IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // LEFT_BRACE ObjectMethod* RIGHT_BRACE
  public static boolean ServiceDefinitionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceDefinitionBody")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SERVICE_DEFINITION_BODY, "<service definition body>");
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, ServiceDefinitionBody_1(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, RecoverableBodyContentRecover_parser_);
    return r || p;
  }

  // ObjectMethod*
  private static boolean ServiceDefinitionBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceDefinitionBody_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ObjectMethod(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ServiceDefinitionBody_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // !(LEFT_BRACE)
  static boolean ServiceDefinitionRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceDefinitionRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, LEFT_BRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // service
  public static boolean ServiceTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceTypeName")) return false;
    if (!nextTokenIs(b, SERVICE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SERVICE);
    exit_section_(b, m, SERVICE_TYPE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // !(SEMICOLON)
  static boolean SimpleTypeContentRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleTypeContentRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // NULL_LITERAL | any | anydata | handle | never | readonly | ValueTypeName | ReferenceTypeName | typedesc | NilLiteral
  public static boolean SimpleTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMPLE_TYPE_NAME, "<simple type name>");
    r = consumeToken(b, NULL_LITERAL);
    if (!r) r = consumeToken(b, ANY);
    if (!r) r = consumeToken(b, ANYDATA);
    if (!r) r = consumeToken(b, HANDLE);
    if (!r) r = consumeToken(b, NEVER);
    if (!r) r = consumeToken(b, READONLY);
    if (!r) r = ValueTypeName(b, l + 1);
    if (!r) r = ReferenceTypeName(b, l + 1);
    if (!r) r = consumeToken(b, TYPEDESC);
    if (!r) r = NilLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE EnumMember RIGHT_BRACE
  public static boolean SingleMemberEnumBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SingleMemberEnumBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && EnumMember(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, SINGLE_MEMBER_ENUM_BODY, r);
    return r;
  }

  /* ********************************************************** */
  // stream (LT TypeName (COMMA TypeName)? GT)?
  public static boolean StreamTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamTypeName")) return false;
    if (!nextTokenIs(b, STREAM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, STREAM_TYPE_NAME, null);
    r = consumeToken(b, STREAM);
    p = r; // pin = 1
    r = r && StreamTypeName_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LT TypeName (COMMA TypeName)? GT)?
  private static boolean StreamTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamTypeName_1")) return false;
    StreamTypeName_1_0(b, l + 1);
    return true;
  }

  // LT TypeName (COMMA TypeName)? GT
  private static boolean StreamTypeName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamTypeName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && TypeName(b, l + 1, -1);
    r = r && StreamTypeName_1_0_2(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA TypeName)?
  private static boolean StreamTypeName_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamTypeName_1_0_2")) return false;
    StreamTypeName_1_0_2_0(b, l + 1);
    return true;
  }

  // COMMA TypeName
  private static boolean StreamTypeName_1_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamTypeName_1_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && TypeName(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // TableKeySpecifier | TableKeyTypeConstraint
  public static boolean TableKeyConstraint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableKeyConstraint")) return false;
    if (!nextTokenIs(b, KEY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TableKeySpecifier(b, l + 1);
    if (!r) r = TableKeyTypeConstraint(b, l + 1);
    exit_section_(b, m, TABLE_KEY_CONSTRAINT, r);
    return r;
  }

  /* ********************************************************** */
  // key LEFT_PARENTHESIS (identifier (COMMA identifier)*)? RIGHT_PARENTHESIS
  public static boolean TableKeySpecifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableKeySpecifier")) return false;
    if (!nextTokenIs(b, KEY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, KEY, LEFT_PARENTHESIS);
    r = r && TableKeySpecifier_2(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, TABLE_KEY_SPECIFIER, r);
    return r;
  }

  // (identifier (COMMA identifier)*)?
  private static boolean TableKeySpecifier_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableKeySpecifier_2")) return false;
    TableKeySpecifier_2_0(b, l + 1);
    return true;
  }

  // identifier (COMMA identifier)*
  private static boolean TableKeySpecifier_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableKeySpecifier_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && TableKeySpecifier_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA identifier)*
  private static boolean TableKeySpecifier_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableKeySpecifier_2_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!TableKeySpecifier_2_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TableKeySpecifier_2_0_1", c)) break;
    }
    return true;
  }

  // COMMA identifier
  private static boolean TableKeySpecifier_2_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableKeySpecifier_2_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // key LT TypeName GT
  public static boolean TableKeyTypeConstraint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableKeyTypeConstraint")) return false;
    if (!nextTokenIs(b, KEY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, KEY, LT);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, TABLE_KEY_TYPE_CONSTRAINT, r);
    return r;
  }

  /* ********************************************************** */
  // !(MARKDOWN_DOCUMENTATION_LINE_START|PARAMETER_DOCUMENTATION_START
  // |RETURN_PARAMETER_DOCUMENTATION_START|AT|external|remote|client|abstract|public|type|typedesc|service|listener
  // |function|enum|annotation|int|float|decimal|boolean|string|byte|map|xml|xmlns|json|table|any|stream|object|record
  // |channel|const|final|var|future|identifier|'{'|transactional|distinct|readonly)
  static boolean TopLevelDefinitionRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TopLevelDefinitionRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !TopLevelDefinitionRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // MARKDOWN_DOCUMENTATION_LINE_START|PARAMETER_DOCUMENTATION_START
  // |RETURN_PARAMETER_DOCUMENTATION_START|AT|external|remote|client|abstract|public|type|typedesc|service|listener
  // |function|enum|annotation|int|float|decimal|boolean|string|byte|map|xml|xmlns|json|table|any|stream|object|record
  // |channel|const|final|var|future|identifier|'{'|transactional|distinct|readonly
  private static boolean TopLevelDefinitionRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TopLevelDefinitionRecover_0")) return false;
    boolean r;
    r = consumeToken(b, MARKDOWN_DOCUMENTATION_LINE_START);
    if (!r) r = consumeToken(b, PARAMETER_DOCUMENTATION_START);
    if (!r) r = consumeToken(b, RETURN_PARAMETER_DOCUMENTATION_START);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, REMOTE);
    if (!r) r = consumeToken(b, CLIENT);
    if (!r) r = consumeToken(b, ABSTRACT);
    if (!r) r = consumeToken(b, PUBLIC);
    if (!r) r = consumeToken(b, TYPE);
    if (!r) r = consumeToken(b, TYPEDESC);
    if (!r) r = consumeToken(b, SERVICE);
    if (!r) r = consumeToken(b, LISTENER);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, ENUM);
    if (!r) r = consumeToken(b, ANNOTATION);
    if (!r) r = consumeToken(b, INT);
    if (!r) r = consumeToken(b, FLOAT);
    if (!r) r = consumeToken(b, DECIMAL);
    if (!r) r = consumeToken(b, BOOLEAN);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, BYTE);
    if (!r) r = consumeToken(b, MAP);
    if (!r) r = consumeToken(b, XML);
    if (!r) r = consumeToken(b, XMLNS);
    if (!r) r = consumeToken(b, JSON);
    if (!r) r = consumeToken(b, TABLE);
    if (!r) r = consumeToken(b, ANY);
    if (!r) r = consumeToken(b, STREAM);
    if (!r) r = consumeToken(b, OBJECT);
    if (!r) r = consumeToken(b, RECORD);
    if (!r) r = consumeToken(b, CHANNEL);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, FUTURE);
    if (!r) r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, LEFT_BRACE);
    if (!r) r = consumeToken(b, TRANSACTIONAL);
    if (!r) r = consumeToken(b, DISTINCT);
    if (!r) r = consumeToken(b, READONLY);
    return r;
  }

  /* ********************************************************** */
  // TypeName ELLIPSIS
  public static boolean TupleRestDescriptor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleRestDescriptor")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TUPLE_REST_DESCRIPTOR, "<tuple rest descriptor>");
    r = TypeName(b, l + 1, -1);
    r = r && consumeToken(b, ELLIPSIS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // CloseRecordTypeBody | OpenRecordTypeBody | ObjectTypeBody | OtherTypeBody
  public static boolean TypeBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_BODY, "<type body>");
    r = CloseRecordTypeBody(b, l + 1);
    if (!r) r = OpenRecordTypeBody(b, l + 1);
    if (!r) r = ObjectTypeBody(b, l + 1);
    if (!r) r = OtherTypeBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // public? type identifier TypeBody SEMICOLON
  public static boolean TypeDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDefinition")) return false;
    if (!nextTokenIs(b, "<type definition>", PUBLIC, TYPE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TYPE_DEFINITION, "<type definition>");
    r = TypeDefinition_0(b, l + 1);
    r = r && consumeTokens(b, 1, TYPE, IDENTIFIER);
    p = r; // pin = 2
    r = r && report_error_(b, TypeBody(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // public?
  private static boolean TypeDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDefinition_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  /* ********************************************************** */
  // typedesc (LT TypeName GT)?
  public static boolean TypeDescReferenceTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDescReferenceTypeName")) return false;
    if (!nextTokenIs(b, TYPEDESC)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TYPEDESC);
    r = r && TypeDescReferenceTypeName_1(b, l + 1);
    exit_section_(b, m, TYPE_DESC_REFERENCE_TYPE_NAME, r);
    return r;
  }

  // (LT TypeName GT)?
  private static boolean TypeDescReferenceTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDescReferenceTypeName_1")) return false;
    TypeDescReferenceTypeName_1_0(b, l + 1);
    return true;
  }

  // LT TypeName GT
  private static boolean TypeDescReferenceTypeName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDescReferenceTypeName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // MUL SimpleTypeName SEMICOLON
  public static boolean TypeReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeReference")) return false;
    if (!nextTokenIs(b, MUL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TYPE_REFERENCE, null);
    r = consumeToken(b, MUL);
    p = r; // pin = 1
    r = r && report_error_(b, SimpleTypeName(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // NameReference
  public static boolean UserDefineTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UserDefineTypeName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = NameReference(b, l + 1);
    exit_section_(b, m, USER_DEFINE_TYPE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // boolean | int | float | decimal | string | byte
  public static boolean ValueTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ValueTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VALUE_TYPE_NAME, "<value type name>");
    r = consumeToken(b, BOOLEAN);
    if (!r) r = consumeToken(b, INT);
    if (!r) r = consumeToken(b, FLOAT);
    if (!r) r = consumeToken(b, DECIMAL);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, BYTE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(SEMICOLON)
  static boolean VarDefExpressionRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarDefExpressionRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // DECIMAL_INTEGER_LITERAL | DECIMAL_FLOATING_POINT_NUMBER | DECIMAL_EXTENDED_FLOATING_POINT_NUMBER
  public static boolean VersionPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VersionPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VERSION_PATTERN, "<version pattern>");
    r = consumeToken(b, DECIMAL_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, DECIMAL_FLOATING_POINT_NUMBER);
    if (!r) r = consumeToken(b, DECIMAL_EXTENDED_FLOATING_POINT_NUMBER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // xml (LT TypeName GT)?
  public static boolean XmlTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlTypeName")) return false;
    if (!nextTokenIs(b, XML)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, XML_TYPE_NAME, null);
    r = consumeToken(b, XML);
    p = r; // pin = 1
    r = r && XmlTypeName_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LT TypeName GT)?
  private static boolean XmlTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlTypeName_1")) return false;
    XmlTypeName_1_0(b, l + 1);
    return true;
  }

  // LT TypeName GT
  private static boolean XmlTypeName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlTypeName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // tripleBacktickedBlock | doubleBacktickedBlock | singleBacktickedBlock
  public static boolean backtickedBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "backtickedBlock")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BACKTICKED_BLOCK, "<backticked block>");
    r = tripleBacktickedBlock(b, l + 1);
    if (!r) r = doubleBacktickedBlock(b, l + 1);
    if (!r) r = singleBacktickedBlock(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // MARKDOWN_DOCUMENTATION_LINE_START documentationText?
  public static boolean deprecateAnnotationDescriptionLine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecateAnnotationDescriptionLine")) return false;
    if (!nextTokenIs(b, MARKDOWN_DOCUMENTATION_LINE_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MARKDOWN_DOCUMENTATION_LINE_START);
    r = r && deprecateAnnotationDescriptionLine_1(b, l + 1);
    exit_section_(b, m, DEPRECATE_ANNOTATION_DESCRIPTION_LINE, r);
    return r;
  }

  // documentationText?
  private static boolean deprecateAnnotationDescriptionLine_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecateAnnotationDescriptionLine_1")) return false;
    documentationText(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // DEPRECATED_DOCUMENTATION
  public static boolean deprecatedAnnotationDocumentation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedAnnotationDocumentation")) return false;
    if (!nextTokenIs(b, DEPRECATED_DOCUMENTATION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DEPRECATED_DOCUMENTATION);
    exit_section_(b, m, DEPRECATED_ANNOTATION_DOCUMENTATION, r);
    return r;
  }

  /* ********************************************************** */
  // deprecatedAnnotationDocumentation deprecateAnnotationDescriptionLine*
  public static boolean deprecatedAnnotationDocumentationLine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedAnnotationDocumentationLine")) return false;
    if (!nextTokenIs(b, DEPRECATED_DOCUMENTATION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = deprecatedAnnotationDocumentation(b, l + 1);
    r = r && deprecatedAnnotationDocumentationLine_1(b, l + 1);
    exit_section_(b, m, DEPRECATED_ANNOTATION_DOCUMENTATION_LINE, r);
    return r;
  }

  // deprecateAnnotationDescriptionLine*
  private static boolean deprecatedAnnotationDocumentationLine_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedAnnotationDocumentationLine_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!deprecateAnnotationDescriptionLine(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "deprecatedAnnotationDocumentationLine_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // DEPRECATED_PARAMETER_DOCUMENTATION
  public static boolean deprecatedParametersDocumentation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedParametersDocumentation")) return false;
    if (!nextTokenIs(b, DEPRECATED_PARAMETER_DOCUMENTATION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DEPRECATED_PARAMETER_DOCUMENTATION);
    exit_section_(b, m, DEPRECATED_PARAMETERS_DOCUMENTATION, r);
    return r;
  }

  /* ********************************************************** */
  // deprecatedParametersDocumentation parameterDocumentationLine+
  public static boolean deprecatedParametersDocumentationLine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedParametersDocumentationLine")) return false;
    if (!nextTokenIs(b, DEPRECATED_PARAMETER_DOCUMENTATION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = deprecatedParametersDocumentation(b, l + 1);
    r = r && deprecatedParametersDocumentationLine_1(b, l + 1);
    exit_section_(b, m, DEPRECATED_PARAMETERS_DOCUMENTATION_LINE, r);
    return r;
  }

  // parameterDocumentationLine+
  private static boolean deprecatedParametersDocumentationLine_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deprecatedParametersDocumentationLine_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameterDocumentationLine(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!parameterDocumentationLine(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "deprecatedParametersDocumentationLine_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // documentationText?
  public static boolean docParameterDescription(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docParameterDescription")) return false;
    Marker m = enter_section_(b, l, _NONE_, DOC_PARAMETER_DESCRIPTION, "<doc parameter description>");
    documentationText(b, l + 1);
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // documentationText?
  public static boolean documentationContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationContent")) return false;
    Marker m = enter_section_(b, l, _NONE_, DOCUMENTATION_CONTENT, "<documentation content>");
    documentationText(b, l + 1);
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // MARKDOWN_DOCUMENTATION_LINE_START documentationContent
  public static boolean documentationLine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationLine")) return false;
    if (!nextTokenIs(b, MARKDOWN_DOCUMENTATION_LINE_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DOCUMENTATION_LINE, null);
    r = consumeToken(b, MARKDOWN_DOCUMENTATION_LINE_START);
    p = r; // pin = 1
    r = r && documentationContent(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // referenceType SINGLE_BACKTICK_CONTENT SINGLE_BACKTICK_MARKDOWN_END
  public static boolean documentationReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationReference")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DOCUMENTATION_REFERENCE, "<documentation reference>");
    r = referenceType(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, consumeTokens(b, -1, SINGLE_BACKTICK_CONTENT, SINGLE_BACKTICK_MARKDOWN_END));
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // documentationLine+ parameterDocumentationLine* returnParameterDocumentationLine? deprecatedParametersDocumentationLine? deprecatedAnnotationDocumentationLine?
  public static boolean documentationString(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationString")) return false;
    if (!nextTokenIs(b, MARKDOWN_DOCUMENTATION_LINE_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = documentationString_0(b, l + 1);
    r = r && documentationString_1(b, l + 1);
    r = r && documentationString_2(b, l + 1);
    r = r && documentationString_3(b, l + 1);
    r = r && documentationString_4(b, l + 1);
    exit_section_(b, m, DOCUMENTATION_STRING, r);
    return r;
  }

  // documentationLine+
  private static boolean documentationString_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationString_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = documentationLine(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!documentationLine(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "documentationString_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // parameterDocumentationLine*
  private static boolean documentationString_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationString_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameterDocumentationLine(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "documentationString_1", c)) break;
    }
    return true;
  }

  // returnParameterDocumentationLine?
  private static boolean documentationString_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationString_2")) return false;
    returnParameterDocumentationLine(b, l + 1);
    return true;
  }

  // deprecatedParametersDocumentationLine?
  private static boolean documentationString_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationString_3")) return false;
    deprecatedParametersDocumentationLine(b, l + 1);
    return true;
  }

  // deprecatedAnnotationDocumentationLine?
  private static boolean documentationString_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationString_4")) return false;
    deprecatedAnnotationDocumentationLine(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (documentationReference | referenceType | backtickedBlock | documentationTextContent)+
  public static boolean documentationText(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationText")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DOCUMENTATION_TEXT, "<documentation text>");
    r = documentationText_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!documentationText_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "documentationText", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // documentationReference | referenceType | backtickedBlock | documentationTextContent
  private static boolean documentationText_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationText_0")) return false;
    boolean r;
    r = documentationReference(b, l + 1);
    if (!r) r = referenceType(b, l + 1);
    if (!r) r = backtickedBlock(b, l + 1);
    if (!r) r = documentationTextContent(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // MARKDOWN_DOCUMENTATION_TEXT | DOCUMENTATION_ESCAPED_CHARACTERS
  public static boolean documentationTextContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationTextContent")) return false;
    if (!nextTokenIs(b, "<documentation text content>", DOCUMENTATION_ESCAPED_CHARACTERS, MARKDOWN_DOCUMENTATION_TEXT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DOCUMENTATION_TEXT_CONTENT, "<documentation text content>");
    r = consumeToken(b, MARKDOWN_DOCUMENTATION_TEXT);
    if (!r) r = consumeToken(b, DOCUMENTATION_ESCAPED_CHARACTERS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // DOUBLE_BACKTICK_MARKDOWN_START DOUBLE_BACKTICK_CONTENT? DOUBLE_BACKTICK_MARKDOWN_END
  public static boolean doubleBacktickedBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doubleBacktickedBlock")) return false;
    if (!nextTokenIs(b, DOUBLE_BACKTICK_MARKDOWN_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DOUBLE_BACKTICKED_BLOCK, null);
    r = consumeToken(b, DOUBLE_BACKTICK_MARKDOWN_START);
    p = r; // pin = 1
    r = r && report_error_(b, doubleBacktickedBlock_1(b, l + 1));
    r = p && consumeToken(b, DOUBLE_BACKTICK_MARKDOWN_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // DOUBLE_BACKTICK_CONTENT?
  private static boolean doubleBacktickedBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doubleBacktickedBlock_1")) return false;
    consumeToken(b, DOUBLE_BACKTICK_CONTENT);
    return true;
  }

  /* ********************************************************** */
  // public? listener RecoverableVariableDefinitionContent SEMICOLON
  static boolean listenerDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listenerDefinition")) return false;
    if (!nextTokenIs(b, "", LISTENER, PUBLIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = listenerDefinition_0(b, l + 1);
    r = r && consumeToken(b, LISTENER);
    p = r; // pin = 2
    r = r && report_error_(b, RecoverableVariableDefinitionContent(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // public?
  private static boolean listenerDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listenerDefinition_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  /* ********************************************************** */
  // MARKDOWN_DOCUMENTATION_LINE_START documentationText?
  public static boolean parameterDescription(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDescription")) return false;
    if (!nextTokenIs(b, MARKDOWN_DOCUMENTATION_LINE_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MARKDOWN_DOCUMENTATION_LINE_START);
    r = r && parameterDescription_1(b, l + 1);
    exit_section_(b, m, PARAMETER_DESCRIPTION, r);
    return r;
  }

  // documentationText?
  private static boolean parameterDescription_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDescription_1")) return false;
    documentationText(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // PARAMETER_DOCUMENTATION_START PARAMETER_NAME DESCRIPTION_SEPARATOR documentationText?
  public static boolean parameterDocumentation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentation")) return false;
    if (!nextTokenIs(b, PARAMETER_DOCUMENTATION_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_DOCUMENTATION, null);
    r = consumeTokens(b, 3, PARAMETER_DOCUMENTATION_START, PARAMETER_NAME, DESCRIPTION_SEPARATOR);
    p = r; // pin = 3
    r = r && parameterDocumentation_3(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // documentationText?
  private static boolean parameterDocumentation_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentation_3")) return false;
    documentationText(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // parameterDocumentation parameterDescription*
  public static boolean parameterDocumentationLine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentationLine")) return false;
    if (!nextTokenIs(b, PARAMETER_DOCUMENTATION_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameterDocumentation(b, l + 1);
    r = r && parameterDocumentationLine_1(b, l + 1);
    exit_section_(b, m, PARAMETER_DOCUMENTATION_LINE, r);
    return r;
  }

  // parameterDescription*
  private static boolean parameterDocumentationLine_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentationLine_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameterDescription(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameterDocumentationLine_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // DOCTYPE | DOCSERVICE | DOCVARIABLE | DOCVAR | DOCANNOTATION | DOCMODULE | DOCFUNCTION | DOCPARAMETER | DOCCONST
  public static boolean referenceType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REFERENCE_TYPE, "<reference type>");
    r = consumeToken(b, DOCTYPE);
    if (!r) r = consumeToken(b, DOCSERVICE);
    if (!r) r = consumeToken(b, DOCVARIABLE);
    if (!r) r = consumeToken(b, DOCVAR);
    if (!r) r = consumeToken(b, DOCANNOTATION);
    if (!r) r = consumeToken(b, DOCMODULE);
    if (!r) r = consumeToken(b, DOCFUNCTION);
    if (!r) r = consumeToken(b, DOCPARAMETER);
    if (!r) r = consumeToken(b, DOCCONST);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // MARKDOWN_DOCUMENTATION_LINE_START documentationText?
  public static boolean returnParameterDescription(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDescription")) return false;
    if (!nextTokenIs(b, MARKDOWN_DOCUMENTATION_LINE_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MARKDOWN_DOCUMENTATION_LINE_START);
    r = r && returnParameterDescription_1(b, l + 1);
    exit_section_(b, m, RETURN_PARAMETER_DESCRIPTION, r);
    return r;
  }

  // documentationText?
  private static boolean returnParameterDescription_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDescription_1")) return false;
    documentationText(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // RETURN_PARAMETER_DOCUMENTATION_START docParameterDescription
  public static boolean returnParameterDocumentation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDocumentation")) return false;
    if (!nextTokenIs(b, RETURN_PARAMETER_DOCUMENTATION_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RETURN_PARAMETER_DOCUMENTATION_START);
    r = r && docParameterDescription(b, l + 1);
    exit_section_(b, m, RETURN_PARAMETER_DOCUMENTATION, r);
    return r;
  }

  /* ********************************************************** */
  // returnParameterDocumentation returnParameterDescription*
  public static boolean returnParameterDocumentationLine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDocumentationLine")) return false;
    if (!nextTokenIs(b, RETURN_PARAMETER_DOCUMENTATION_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = returnParameterDocumentation(b, l + 1);
    r = r && returnParameterDocumentationLine_1(b, l + 1);
    exit_section_(b, m, RETURN_PARAMETER_DOCUMENTATION_LINE, r);
    return r;
  }

  // returnParameterDescription*
  private static boolean returnParameterDocumentationLine_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDocumentationLine_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!returnParameterDescription(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "returnParameterDocumentationLine_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // SINGLE_BACKTICK_MARKDOWN_START SINGLE_BACKTICK_CONTENT? SINGLE_BACKTICK_MARKDOWN_END
  public static boolean singleBacktickedBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "singleBacktickedBlock")) return false;
    if (!nextTokenIs(b, SINGLE_BACKTICK_MARKDOWN_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SINGLE_BACKTICKED_BLOCK, null);
    r = consumeToken(b, SINGLE_BACKTICK_MARKDOWN_START);
    p = r; // pin = 1
    r = r && report_error_(b, singleBacktickedBlock_1(b, l + 1));
    r = p && consumeToken(b, SINGLE_BACKTICK_MARKDOWN_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // SINGLE_BACKTICK_CONTENT?
  private static boolean singleBacktickedBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "singleBacktickedBlock_1")) return false;
    consumeToken(b, SINGLE_BACKTICK_CONTENT);
    return true;
  }

  /* ********************************************************** */
  // TRIPLE_BACKTICK_MARKDOWN_START TRIPLE_BACKTICK_CONTENT? TRIPLE_BACKTICK_MARKDOWN_END
  public static boolean tripleBacktickedBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tripleBacktickedBlock")) return false;
    if (!nextTokenIs(b, TRIPLE_BACKTICK_MARKDOWN_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRIPLE_BACKTICKED_BLOCK, null);
    r = consumeToken(b, TRIPLE_BACKTICK_MARKDOWN_START);
    p = r; // pin = 1
    r = r && report_error_(b, tripleBacktickedBlock_1(b, l + 1));
    r = p && consumeToken(b, TRIPLE_BACKTICK_MARKDOWN_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // TRIPLE_BACKTICK_CONTENT?
  private static boolean tripleBacktickedBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tripleBacktickedBlock_1")) return false;
    consumeToken(b, TRIPLE_BACKTICK_CONTENT);
    return true;
  }

  /* ********************************************************** */
  // final? TypeName identifier ASSIGN RecoverableVarDefContent SEMICOLON
  static boolean typedVariableDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typedVariableDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = typedVariableDefinition_0(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeTokens(b, 1, IDENTIFIER, ASSIGN);
    p = r; // pin = 3
    r = r && report_error_(b, RecoverableVarDefContent(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // final?
  private static boolean typedVariableDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typedVariableDefinition_0")) return false;
    consumeToken(b, FINAL);
    return true;
  }

  /* ********************************************************** */
  // !(BOOLEAN_LITERAL|DECIMAL_INTEGER_LITERAL|HEX_INTEGER_LITERAL|OCTAL_INTEGER_LITERAL
  // |BINARY_INTEGER_LITERAL|NULL_LITERAL|DECIMAL_FLOATING_POINT_NUMBER|HEXADECIMAL_FLOATING_POINT_LITERAL|int|string
  // |float|decimal|boolean|byte|any|anydata|json|xml|xmlns|map|table|function|stream|'{'|'['|'}'|';'|typedesc|future|var
  // |while|match|foreach|continue|break|fork|try|throw|return|abort|aborted|committed|retry|fail|lock|transaction|if
  // |forever|object|service|check|checkpanic|error|panic|from|worker|record|identifier|wait|with|where|TYPE_PARAMETER
  // |TYPE_FIELD|SYNCRARROW|start|source|retries|parameter|onretry|OBJECT_INIT|new|select|let|is|in|finally|ELVIS|else
  // |do|catch|typeof|trap|flush|XML_ALL_CHAR|STRING_TEMPLATE_TEXT|STRING_TEMPLATE_LITERAL_START|STRING_TEMPLATE_LITERAL_END
  // |STRING_TEMPLATE_EXPRESSION_START|STRING_TEMPLATE_EXPRESSION_END|XML_LITERAL_START|XML_LITERAL_END|BITAND|BITXOR
  // |BIT_COMPLEMENT|COMPOUND_BIT_OR|COMPOUND_BIT_AND|COMPOUND_BIT_XOR|ANNOTATION_ACCESS|OPTIONAL_FIELD_ACCESS
  // |COMPOUND_LEFT_SHIFT|COMPOUND_RIGHT_SHIFT|HALF_OPEN_RANGE|DEFAULT|BASE_16_BLOB_LITERAL|BASE_64_BLOB_LITERAL
  // |COMPOUND_LOGICAL_SHIFT|IGNORED_LEFT_BRACE|IGNORED_RIGHT_BRACE|limit|JOIN_EQUALS|commit|rollback|conflict)
  static boolean unusedTokens(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unusedTokens")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !unusedTokens_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // BOOLEAN_LITERAL|DECIMAL_INTEGER_LITERAL|HEX_INTEGER_LITERAL|OCTAL_INTEGER_LITERAL
  // |BINARY_INTEGER_LITERAL|NULL_LITERAL|DECIMAL_FLOATING_POINT_NUMBER|HEXADECIMAL_FLOATING_POINT_LITERAL|int|string
  // |float|decimal|boolean|byte|any|anydata|json|xml|xmlns|map|table|function|stream|'{'|'['|'}'|';'|typedesc|future|var
  // |while|match|foreach|continue|break|fork|try|throw|return|abort|aborted|committed|retry|fail|lock|transaction|if
  // |forever|object|service|check|checkpanic|error|panic|from|worker|record|identifier|wait|with|where|TYPE_PARAMETER
  // |TYPE_FIELD|SYNCRARROW|start|source|retries|parameter|onretry|OBJECT_INIT|new|select|let|is|in|finally|ELVIS|else
  // |do|catch|typeof|trap|flush|XML_ALL_CHAR|STRING_TEMPLATE_TEXT|STRING_TEMPLATE_LITERAL_START|STRING_TEMPLATE_LITERAL_END
  // |STRING_TEMPLATE_EXPRESSION_START|STRING_TEMPLATE_EXPRESSION_END|XML_LITERAL_START|XML_LITERAL_END|BITAND|BITXOR
  // |BIT_COMPLEMENT|COMPOUND_BIT_OR|COMPOUND_BIT_AND|COMPOUND_BIT_XOR|ANNOTATION_ACCESS|OPTIONAL_FIELD_ACCESS
  // |COMPOUND_LEFT_SHIFT|COMPOUND_RIGHT_SHIFT|HALF_OPEN_RANGE|DEFAULT|BASE_16_BLOB_LITERAL|BASE_64_BLOB_LITERAL
  // |COMPOUND_LOGICAL_SHIFT|IGNORED_LEFT_BRACE|IGNORED_RIGHT_BRACE|limit|JOIN_EQUALS|commit|rollback|conflict
  private static boolean unusedTokens_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unusedTokens_0")) return false;
    boolean r;
    r = consumeToken(b, BOOLEAN_LITERAL);
    if (!r) r = consumeToken(b, DECIMAL_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, HEX_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, OCTAL_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, BINARY_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, NULL_LITERAL);
    if (!r) r = consumeToken(b, DECIMAL_FLOATING_POINT_NUMBER);
    if (!r) r = consumeToken(b, HEXADECIMAL_FLOATING_POINT_LITERAL);
    if (!r) r = consumeToken(b, INT);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, FLOAT);
    if (!r) r = consumeToken(b, DECIMAL);
    if (!r) r = consumeToken(b, BOOLEAN);
    if (!r) r = consumeToken(b, BYTE);
    if (!r) r = consumeToken(b, ANY);
    if (!r) r = consumeToken(b, ANYDATA);
    if (!r) r = consumeToken(b, JSON);
    if (!r) r = consumeToken(b, XML);
    if (!r) r = consumeToken(b, XMLNS);
    if (!r) r = consumeToken(b, MAP);
    if (!r) r = consumeToken(b, TABLE);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, STREAM);
    if (!r) r = consumeToken(b, LEFT_BRACE);
    if (!r) r = consumeToken(b, LEFT_BRACKET);
    if (!r) r = consumeToken(b, RIGHT_BRACE);
    if (!r) r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, TYPEDESC);
    if (!r) r = consumeToken(b, FUTURE);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, MATCH);
    if (!r) r = consumeToken(b, FOREACH);
    if (!r) r = consumeToken(b, CONTINUE);
    if (!r) r = consumeToken(b, BREAK);
    if (!r) r = consumeToken(b, FORK);
    if (!r) r = consumeToken(b, TRY);
    if (!r) r = consumeToken(b, THROW);
    if (!r) r = consumeToken(b, RETURN);
    if (!r) r = consumeToken(b, ABORT);
    if (!r) r = consumeToken(b, ABORTED);
    if (!r) r = consumeToken(b, COMMITTED);
    if (!r) r = consumeToken(b, RETRY);
    if (!r) r = consumeToken(b, FAIL);
    if (!r) r = consumeToken(b, LOCK);
    if (!r) r = consumeToken(b, TRANSACTION);
    if (!r) r = consumeToken(b, IF);
    if (!r) r = consumeToken(b, FOREVER);
    if (!r) r = consumeToken(b, OBJECT);
    if (!r) r = consumeToken(b, SERVICE);
    if (!r) r = consumeToken(b, CHECK);
    if (!r) r = consumeToken(b, CHECKPANIC);
    if (!r) r = consumeToken(b, ERROR);
    if (!r) r = consumeToken(b, PANIC);
    if (!r) r = consumeToken(b, FROM);
    if (!r) r = consumeToken(b, WORKER);
    if (!r) r = consumeToken(b, RECORD);
    if (!r) r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, WAIT);
    if (!r) r = consumeToken(b, WITH);
    if (!r) r = consumeToken(b, WHERE);
    if (!r) r = consumeToken(b, TYPE_PARAMETER);
    if (!r) r = consumeToken(b, TYPE_FIELD);
    if (!r) r = consumeToken(b, SYNCRARROW);
    if (!r) r = consumeToken(b, START);
    if (!r) r = consumeToken(b, SOURCE);
    if (!r) r = consumeToken(b, RETRIES);
    if (!r) r = consumeToken(b, PARAMETER);
    if (!r) r = consumeToken(b, ONRETRY);
    if (!r) r = consumeToken(b, OBJECT_INIT);
    if (!r) r = consumeToken(b, NEW);
    if (!r) r = consumeToken(b, SELECT);
    if (!r) r = consumeToken(b, LET);
    if (!r) r = consumeToken(b, IS);
    if (!r) r = consumeToken(b, IN);
    if (!r) r = consumeToken(b, FINALLY);
    if (!r) r = consumeToken(b, ELVIS);
    if (!r) r = consumeToken(b, ELSE);
    if (!r) r = consumeToken(b, DO);
    if (!r) r = consumeToken(b, CATCH);
    if (!r) r = consumeToken(b, TYPEOF);
    if (!r) r = consumeToken(b, TRAP);
    if (!r) r = consumeToken(b, FLUSH);
    if (!r) r = consumeToken(b, XML_ALL_CHAR);
    if (!r) r = consumeToken(b, STRING_TEMPLATE_TEXT);
    if (!r) r = consumeToken(b, STRING_TEMPLATE_LITERAL_START);
    if (!r) r = consumeToken(b, STRING_TEMPLATE_LITERAL_END);
    if (!r) r = consumeToken(b, STRING_TEMPLATE_EXPRESSION_START);
    if (!r) r = consumeToken(b, STRING_TEMPLATE_EXPRESSION_END);
    if (!r) r = consumeToken(b, XML_LITERAL_START);
    if (!r) r = consumeToken(b, XML_LITERAL_END);
    if (!r) r = consumeToken(b, BITAND);
    if (!r) r = consumeToken(b, BITXOR);
    if (!r) r = consumeToken(b, BIT_COMPLEMENT);
    if (!r) r = consumeToken(b, COMPOUND_BIT_OR);
    if (!r) r = consumeToken(b, COMPOUND_BIT_AND);
    if (!r) r = consumeToken(b, COMPOUND_BIT_XOR);
    if (!r) r = consumeToken(b, ANNOTATION_ACCESS);
    if (!r) r = consumeToken(b, OPTIONAL_FIELD_ACCESS);
    if (!r) r = consumeToken(b, COMPOUND_LEFT_SHIFT);
    if (!r) r = consumeToken(b, COMPOUND_RIGHT_SHIFT);
    if (!r) r = consumeToken(b, HALF_OPEN_RANGE);
    if (!r) r = consumeToken(b, DEFAULT);
    if (!r) r = consumeToken(b, BASE_16_BLOB_LITERAL);
    if (!r) r = consumeToken(b, BASE_64_BLOB_LITERAL);
    if (!r) r = consumeToken(b, COMPOUND_LOGICAL_SHIFT);
    if (!r) r = consumeToken(b, IGNORED_LEFT_BRACE);
    if (!r) r = consumeToken(b, IGNORED_RIGHT_BRACE);
    if (!r) r = consumeToken(b, LIMIT);
    if (!r) r = consumeToken(b, JOIN_EQUALS);
    if (!r) r = consumeToken(b, COMMIT);
    if (!r) r = consumeToken(b, ROLLBACK);
    if (!r) r = consumeToken(b, CONFLICT);
    return r;
  }

  /* ********************************************************** */
  // final? var RecoverableVariableDefinitionContent SEMICOLON
  static boolean varDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDefinition")) return false;
    if (!nextTokenIs(b, "", FINAL, VAR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = varDefinition_0(b, l + 1);
    r = r && consumeToken(b, VAR);
    p = r; // pin = 2
    r = r && report_error_(b, RecoverableVariableDefinitionContent(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // final?
  private static boolean varDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDefinition_0")) return false;
    consumeToken(b, FINAL);
    return true;
  }

  /* ********************************************************** */
  // Expression root: TypeName
  // Operator priority table:
  // 0: ATOM(TupleTypeName)
  // 1: ATOM(DistinctSimpleTypeName)
  // 2: ATOM(GroupTypeName)
  // 3: POSTFIX(ArrayTypeName)
  // 4: N_ARY(UnionTypeName)
  // 5: ATOM(ObjectTypeName)
  // 6: POSTFIX(NullableTypeName)
  // 7: ATOM(InclusiveRecordTypeName)
  // 8: ATOM(ExclusiveRecordTypeName)
  // 9: BINARY(IntersectionTypeName)
  // 10: ATOM(TableTypeName)
  public static boolean TypeName(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "TypeName")) return false;
    addVariant(b, "<type name>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<type name>");
    r = TupleTypeName(b, l + 1);
    if (!r) r = DistinctSimpleTypeName(b, l + 1);
    if (!r) r = GroupTypeName(b, l + 1);
    if (!r) r = ObjectTypeName(b, l + 1);
    if (!r) r = InclusiveRecordTypeName(b, l + 1);
    if (!r) r = ExclusiveRecordTypeName(b, l + 1);
    if (!r) r = TableTypeName(b, l + 1);
    p = r;
    r = r && TypeName_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean TypeName_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "TypeName_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 3 && ArrayTypeName_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, ARRAY_TYPE_NAME, r, true, null);
      }
      else if (g < 4 && consumeTokenSmart(b, PIPE)) {
        while (true) {
          r = report_error_(b, TypeName(b, l, 4));
          if (!consumeTokenSmart(b, PIPE)) break;
        }
        exit_section_(b, l, m, UNION_TYPE_NAME, r, true, null);
      }
      else if (g < 6 && NullableTypeName_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, NULLABLE_TYPE_NAME, r, true, null);
      }
      else if (g < 9 && consumeTokenSmart(b, BIT_AND)) {
        r = TypeName(b, l, 9);
        exit_section_(b, l, m, INTERSECTION_TYPE_NAME, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // LEFT_BRACKET ((TypeName (COMMA TypeName)* (COMMA TupleRestDescriptor)?) | TupleRestDescriptor) RIGHT_BRACKET
  public static boolean TupleTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleTypeName")) return false;
    if (!nextTokenIsSmart(b, LEFT_BRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TUPLE_TYPE_NAME, null);
    r = consumeTokenSmart(b, LEFT_BRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, TupleTypeName_1(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (TypeName (COMMA TypeName)* (COMMA TupleRestDescriptor)?) | TupleRestDescriptor
  private static boolean TupleTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleTypeName_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TupleTypeName_1_0(b, l + 1);
    if (!r) r = TupleRestDescriptor(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // TypeName (COMMA TypeName)* (COMMA TupleRestDescriptor)?
  private static boolean TupleTypeName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleTypeName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeName(b, l + 1, -1);
    r = r && TupleTypeName_1_0_1(b, l + 1);
    r = r && TupleTypeName_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA TypeName)*
  private static boolean TupleTypeName_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleTypeName_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!TupleTypeName_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TupleTypeName_1_0_1", c)) break;
    }
    return true;
  }

  // COMMA TypeName
  private static boolean TupleTypeName_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleTypeName_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, COMMA);
    r = r && TypeName(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA TupleRestDescriptor)?
  private static boolean TupleTypeName_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleTypeName_1_0_2")) return false;
    TupleTypeName_1_0_2_0(b, l + 1);
    return true;
  }

  // COMMA TupleRestDescriptor
  private static boolean TupleTypeName_1_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TupleTypeName_1_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, COMMA);
    r = r && TupleRestDescriptor(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // distinct? SimpleTypeName
  public static boolean DistinctSimpleTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DistinctSimpleTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DISTINCT_SIMPLE_TYPE_NAME, "<distinct simple type name>");
    r = DistinctSimpleTypeName_0(b, l + 1);
    r = r && SimpleTypeName(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // distinct?
  private static boolean DistinctSimpleTypeName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DistinctSimpleTypeName_0")) return false;
    consumeTokenSmart(b, DISTINCT);
    return true;
  }

  // LEFT_PARENTHESIS <<isGroupType>> TypeName RIGHT_PARENTHESIS
  public static boolean GroupTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GroupTypeName")) return false;
    if (!nextTokenIsSmart(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GROUP_TYPE_NAME, null);
    r = consumeTokenSmart(b, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, isGroupType(b, l + 1));
    r = p && report_error_(b, TypeName(b, l + 1, -1)) && r;
    r = p && consumeToken(b, RIGHT_PARENTHESIS) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LEFT_BRACKET (IntegerLiteral | MUL)? RIGHT_BRACKET)+
  private static boolean ArrayTypeName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayTypeName_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ArrayTypeName_0_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!ArrayTypeName_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ArrayTypeName_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_BRACKET (IntegerLiteral | MUL)? RIGHT_BRACKET
  private static boolean ArrayTypeName_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayTypeName_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LEFT_BRACKET);
    r = r && ArrayTypeName_0_0_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // (IntegerLiteral | MUL)?
  private static boolean ArrayTypeName_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayTypeName_0_0_1")) return false;
    ArrayTypeName_0_0_1_0(b, l + 1);
    return true;
  }

  // IntegerLiteral | MUL
  private static boolean ArrayTypeName_0_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayTypeName_0_0_1_0")) return false;
    boolean r;
    r = IntegerLiteral(b, l + 1);
    if (!r) r = consumeTokenSmart(b, MUL);
    return r;
  }

  // distinct? ((client? abstract) | (abstract? client?)) readonly? object LEFT_BRACE ObjectBody RIGHT_BRACE
  public static boolean ObjectTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_TYPE_NAME, "<object type name>");
    r = ObjectTypeName_0(b, l + 1);
    r = r && ObjectTypeName_1(b, l + 1);
    r = r && ObjectTypeName_2(b, l + 1);
    r = r && consumeTokensSmart(b, 1, OBJECT, LEFT_BRACE);
    p = r; // pin = 4
    r = r && report_error_(b, ObjectBody(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // distinct?
  private static boolean ObjectTypeName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_0")) return false;
    consumeTokenSmart(b, DISTINCT);
    return true;
  }

  // (client? abstract) | (abstract? client?)
  private static boolean ObjectTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ObjectTypeName_1_0(b, l + 1);
    if (!r) r = ObjectTypeName_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // client? abstract
  private static boolean ObjectTypeName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ObjectTypeName_1_0_0(b, l + 1);
    r = r && consumeToken(b, ABSTRACT);
    exit_section_(b, m, null, r);
    return r;
  }

  // client?
  private static boolean ObjectTypeName_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_1_0_0")) return false;
    consumeTokenSmart(b, CLIENT);
    return true;
  }

  // abstract? client?
  private static boolean ObjectTypeName_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ObjectTypeName_1_1_0(b, l + 1);
    r = r && ObjectTypeName_1_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // abstract?
  private static boolean ObjectTypeName_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_1_1_0")) return false;
    consumeTokenSmart(b, ABSTRACT);
    return true;
  }

  // client?
  private static boolean ObjectTypeName_1_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_1_1_1")) return false;
    consumeTokenSmart(b, CLIENT);
    return true;
  }

  // readonly?
  private static boolean ObjectTypeName_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_2")) return false;
    consumeTokenSmart(b, READONLY);
    return true;
  }

  // <<nullableTypePredicate>> QUESTION_MARK
  private static boolean NullableTypeName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NullableTypeName_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nullableTypePredicate(b, l + 1);
    r = r && consumeToken(b, QUESTION_MARK);
    exit_section_(b, m, null, r);
    return r;
  }

  // record RecoverableBody
  public static boolean InclusiveRecordTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InclusiveRecordTypeName")) return false;
    if (!nextTokenIsSmart(b, RECORD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, RECORD);
    r = r && RecoverableBody(b, l + 1);
    exit_section_(b, m, INCLUSIVE_RECORD_TYPE_NAME, r);
    return r;
  }

  // record LEFT_CLOSED_RECORD_DELIMITER RecoverableBodyContent RIGHT_CLOSED_RECORD_DELIMITER
  public static boolean ExclusiveRecordTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExclusiveRecordTypeName")) return false;
    if (!nextTokenIsSmart(b, RECORD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, RECORD, LEFT_CLOSED_RECORD_DELIMITER);
    r = r && RecoverableBodyContent(b, l + 1);
    r = r && consumeToken(b, RIGHT_CLOSED_RECORD_DELIMITER);
    exit_section_(b, m, EXCLUSIVE_RECORD_TYPE_NAME, r);
    return r;
  }

  // table LT TypeName GT TableKeyConstraint?
  public static boolean TableTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableTypeName")) return false;
    if (!nextTokenIsSmart(b, TABLE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TABLE_TYPE_NAME, null);
    r = consumeTokensSmart(b, 1, TABLE, LT);
    p = r; // pin = 1
    r = r && report_error_(b, TypeName(b, l + 1, -1));
    r = p && report_error_(b, consumeToken(b, GT)) && r;
    r = p && TableTypeName_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // TableKeyConstraint?
  private static boolean TableTypeName_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableTypeName_4")) return false;
    TableKeyConstraint(b, l + 1);
    return true;
  }

  static final Parser AnnotationDefinitionRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return AnnotationDefinitionRecover(b, l + 1);
    }
  };
  static final Parser ConstantDefinitionRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return ConstantDefinitionRecover(b, l + 1);
    }
  };
  static final Parser EnumContentRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return EnumContentRecover(b, l + 1);
    }
  };
  static final Parser ExprFuncBodyRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return ExprFuncBodyRecover(b, l + 1);
    }
  };
  static final Parser ExternalFuncBodyRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return ExternalFuncBodyRecover(b, l + 1);
    }
  };
  static final Parser FunctionSignatureRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return FunctionSignatureRecover(b, l + 1);
    }
  };
  static final Parser GlobalVariableDefinitionRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return GlobalVariableDefinitionRecover(b, l + 1);
    }
  };
  static final Parser NestedRecoverableBodyContentRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return NestedRecoverableBodyContentRecover(b, l + 1);
    }
  };
  static final Parser NestedReturnParamRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return NestedReturnParamRecover(b, l + 1);
    }
  };
  static final Parser ObjectBodyRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return ObjectBodyRecover(b, l + 1);
    }
  };
  static final Parser ObjectFieldDefinitionContentRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return ObjectFieldDefinitionContentRecover(b, l + 1);
    }
  };
  static final Parser RecoverableBodyContentRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return RecoverableBodyContentRecover(b, l + 1);
    }
  };
  static final Parser RecoverableCloseRecordContentRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return RecoverableCloseRecordContentRecover(b, l + 1);
    }
  };
  static final Parser RecoverableOpenRecordContentRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return RecoverableOpenRecordContentRecover(b, l + 1);
    }
  };
  static final Parser ReturnParamRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return ReturnParamRecover(b, l + 1);
    }
  };
  static final Parser ServiceDefinitionRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return ServiceDefinitionRecover(b, l + 1);
    }
  };
  static final Parser SimpleTypeContentRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return SimpleTypeContentRecover(b, l + 1);
    }
  };
  static final Parser TopLevelDefinitionRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return TopLevelDefinitionRecover(b, l + 1);
    }
  };
  static final Parser VarDefExpressionRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return VarDefExpressionRecover(b, l + 1);
    }
  };
}
