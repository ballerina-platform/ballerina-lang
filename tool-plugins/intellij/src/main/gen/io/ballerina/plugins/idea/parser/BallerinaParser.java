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
    create_token_set_(CONSTANT_EXPRESSION, CONST_ADD_SUB_EXPRESSION, CONST_DIV_MUL_MOD_EXPRESSION, CONST_GROUP_EXPRESSION,
      RECORD_LITERAL_CONST_EXPRESSION, SIMPLE_LITERAL_CONST_EXPRESSION),
    create_token_set_(STATIC_MATCH_IDENTIFIER_LITERAL, STATIC_MATCH_LIST_LITERAL, STATIC_MATCH_LITERAL, STATIC_MATCH_OR_EXPRESSION,
      STATIC_MATCH_RECORD_LITERAL, STATIC_MATCH_SIMPLE_LITERAL),
    create_token_set_(ARRAY_TYPE_NAME, EXCLUSIVE_RECORD_TYPE_DESCRIPTOR, GROUP_TYPE_NAME, INCLUSIVE_RECORD_TYPE_DESCRIPTOR,
      NULLABLE_TYPE_NAME, OBJECT_TYPE_NAME, SIMPLE_TYPE_NAME, TUPLE_TYPE_NAME,
      TYPE_NAME, UNION_TYPE_NAME),
    create_token_set_(ANNOTATION_ACCESS_REFERENCE, FIELD_VARIABLE_REFERENCE, FUNCTION_INVOCATION_REFERENCE, GROUP_FIELD_VARIABLE_REFERENCE,
      GROUP_INVOCATION_REFERENCE, GROUP_MAP_ARRAY_VARIABLE_REFERENCE, GROUP_STRING_FUNCTION_INVOCATION_REFERENCE, INVOCATION_REFERENCE,
      MAP_ARRAY_VARIABLE_REFERENCE, SIMPLE_VARIABLE_REFERENCE, STRING_FUNCTION_INVOCATION_REFERENCE, TYPE_DESC_EXPR_INVOCATION_REFERENCE,
      VARIABLE_REFERENCE, XML_ATTRIB_VARIABLE_REFERENCE, XML_ELEMENT_FILTER_REFERENCE, XML_STEP_EXPRESSION_REFERENCE),
    create_token_set_(ACTION_INVOCATION_EXPRESSION, ANNOTATION_ACTION_EXPRESSION, ANONYMOUS_FUNCTION_EXPRESSION, BINARY_ADD_SUB_EXPRESSION,
      BINARY_AND_EXPRESSION, BINARY_COMPARE_EXPRESSION, BINARY_DIV_MUL_MOD_EXPRESSION, BINARY_EQUAL_EXPRESSION,
      BINARY_OR_EXPRESSION, BINARY_REF_EQUAL_EXPRESSION, BITWISE_EXPRESSION, BITWISE_SHIFT_EXPRESSION,
      CHECKED_EXPRESSION, CHECK_PANIC_EXPRESSION, ELVIS_EXPRESSION, EXPRESSION,
      FLUSH_WORKER_EXPRESSION, GROUP_EXPRESSION, INTEGER_RANGE_EXPRESSION, LET_EXPRESSION,
      LIST_CONSTRUCTOR_EXPRESSION, QUERY_ACTION_EXPRESSION, QUERY_EXPRESSION, RECORD_LITERAL_EXPRESSION,
      SERVICE_CONSTRUCTOR_EXPRESSION, SIMPLE_LITERAL_EXPRESSION, STRING_TEMPLATE_LITERAL_EXPRESSION, TABLE_LITERAL_EXPRESSION,
      TERNARY_EXPRESSION, TRAP_EXPRESSION, TYPE_CONVERSION_EXPRESSION, TYPE_DESC_EXPRESSION,
      TYPE_INIT_EXPRESSION, TYPE_TEST_EXPRESSION, UNARY_EXPRESSION, VARIABLE_REFERENCE_EXPRESSION,
      WAIT_EXPRESSION, WORKER_RECEIVE_EXPRESSION, WORKER_SEND_SYNC_EXPRESSION, XML_LITERAL_EXPRESSION),
  };

  /* ********************************************************** */
  // abort SEMICOLON
  public static boolean AbortStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AbortStatement")) return false;
    if (!nextTokenIs(b, ABORT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ABORT_STATEMENT, null);
    r = consumeTokens(b, 1, ABORT, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // aborted LEFT_BRACE Block RIGHT_BRACE
  public static boolean AbortedClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AbortedClause")) return false;
    if (!nextTokenIs(b, ABORTED)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ABORTED_CLAUSE, null);
    r = consumeTokens(b, 1, ABORTED, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (AnnotationAttachment* start)? VariableReference RARROW FunctionInvocation {/*pin=3 recoverWhile=StatementRecover*/}
  public static boolean ActionInvocation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ActionInvocation")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ACTION_INVOCATION, "<action invocation>");
    r = ActionInvocation_0(b, l + 1);
    r = r && VariableReference(b, l + 1, -1);
    r = r && consumeToken(b, RARROW);
    r = r && FunctionInvocation(b, l + 1);
    r = r && ActionInvocation_4(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (AnnotationAttachment* start)?
  private static boolean ActionInvocation_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ActionInvocation_0")) return false;
    ActionInvocation_0_0(b, l + 1);
    return true;
  }

  // AnnotationAttachment* start
  private static boolean ActionInvocation_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ActionInvocation_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ActionInvocation_0_0_0(b, l + 1);
    r = r && consumeToken(b, START);
    exit_section_(b, m, null, r);
    return r;
  }

  // AnnotationAttachment*
  private static boolean ActionInvocation_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ActionInvocation_0_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ActionInvocation_0_0_0", c)) break;
    }
    return true;
  }

  // {/*pin=3 recoverWhile=StatementRecover*/}
  private static boolean ActionInvocation_4(PsiBuilder b, int l) {
    return true;
  }

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
  // AT NameReference RecordLiteral?
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

  // RecordLiteral?
  private static boolean AnnotationAttachment_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationAttachment_2")) return false;
    RecordLiteral(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // public? const? annotation (TypeName identifier | identifier) (on AttachmentPoint (COMMA AttachmentPoint)*)? SEMICOLON
  public static boolean AnnotationDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ANNOTATION_DEFINITION, "<annotation definition>");
    r = AnnotationDefinition_0(b, l + 1);
    r = r && AnnotationDefinition_1(b, l + 1);
    r = r && consumeToken(b, ANNOTATION);
    p = r; // pin = 3
    r = r && report_error_(b, AnnotationDefinition_3(b, l + 1));
    r = p && report_error_(b, AnnotationDefinition_4(b, l + 1)) && r;
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

  // TypeName identifier | identifier
  private static boolean AnnotationDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = AnnotationDefinition_3_0(b, l + 1);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // TypeName identifier
  private static boolean AnnotationDefinition_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeName(b, l + 1, -1);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // (on AttachmentPoint (COMMA AttachmentPoint)*)?
  private static boolean AnnotationDefinition_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_4")) return false;
    AnnotationDefinition_4_0(b, l + 1);
    return true;
  }

  // on AttachmentPoint (COMMA AttachmentPoint)*
  private static boolean AnnotationDefinition_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ON);
    r = r && AttachmentPoint(b, l + 1);
    r = r && AnnotationDefinition_4_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA AttachmentPoint)*
  private static boolean AnnotationDefinition_4_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_4_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationDefinition_4_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "AnnotationDefinition_4_0_2", c)) break;
    }
    return true;
  }

  // COMMA AttachmentPoint
  private static boolean AnnotationDefinition_4_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_4_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && AttachmentPoint(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ExplicitAnonymousFunctionExpr | InferAnonymousFunctionExpr
  public static boolean AnonymousFunctionExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnonymousFunctionExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ANONYMOUS_FUNCTION_EXPR, "<anonymous function expr>");
    r = ExplicitAnonymousFunctionExpr(b, l + 1);
    if (!r) r = InferAnonymousFunctionExpr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // anydata
  public static boolean AnyDataTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnyDataTypeName")) return false;
    if (!nextTokenIs(b, ANYDATA)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ANYDATA);
    exit_section_(b, m, ANY_DATA_TYPE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // identifier | ReservedWord
  public static boolean AnyIdentifierName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnyIdentifierName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ANY_IDENTIFIER_NAME, "<any identifier name>");
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = ReservedWord(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // any
  public static boolean AnyTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnyTypeName")) return false;
    if (!nextTokenIs(b, ANY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ANY);
    exit_section_(b, m, ANY_TYPE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // VariableReference ASSIGN Expression SEMICOLON
  public static boolean AssignmentStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AssignmentStatement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ASSIGNMENT_STATEMENT, "<assignment statement>");
    r = VariableReference(b, l + 1, -1);
    r = r && consumeToken(b, ASSIGN);
    p = r; // pin = 2
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // DualAttachPoint | SourceOnlyAttachPoint
  public static boolean AttachmentPoint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AttachmentPoint")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ATTACHMENT_POINT, "<attachment point>");
    r = DualAttachPoint(b, l + 1);
    if (!r) r = SourceOnlyAttachPoint(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier | StructuredBindingPattern
  public static boolean BindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BindingPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BINDING_PATTERN, "<binding pattern>");
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = StructuredBindingPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // VariableReference
  //                     | StructuredRefBindingPattern
  //                     | ErrorRefBindingPattern
  public static boolean BindingRefPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BindingRefPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BINDING_REF_PATTERN, "<binding ref pattern>");
    r = VariableReference(b, l + 1, -1);
    if (!r) r = StructuredRefBindingPattern(b, l + 1);
    if (!r) r = ErrorRefBindingPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // BASE_16_BLOB_LITERAL | BASE_64_BLOB_LITERAL
  public static boolean BlobLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlobLiteral")) return false;
    if (!nextTokenIs(b, "<blob literal>", BASE_16_BLOB_LITERAL, BASE_64_BLOB_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOB_LITERAL, "<blob literal>");
    r = consumeToken(b, BASE_16_BLOB_LITERAL);
    if (!r) r = consumeToken(b, BASE_64_BLOB_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // Statement*
  public static boolean Block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Block")) return false;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, "<block>");
    while (true) {
      int c = current_position_(b);
      if (!Statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "Block", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // LEFT_BRACE (RIGHT_BRACE | Statement* WorkerWithStatementsBlock+ RIGHT_BRACE | Statement+ RIGHT_BRACE )
  public static boolean BlockFunctionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockFunctionBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, BLOCK_FUNCTION_BODY, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && BlockFunctionBody_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // RIGHT_BRACE | Statement* WorkerWithStatementsBlock+ RIGHT_BRACE | Statement+ RIGHT_BRACE
  private static boolean BlockFunctionBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockFunctionBody_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RIGHT_BRACE);
    if (!r) r = BlockFunctionBody_1_1(b, l + 1);
    if (!r) r = BlockFunctionBody_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // Statement* WorkerWithStatementsBlock+ RIGHT_BRACE
  private static boolean BlockFunctionBody_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockFunctionBody_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = BlockFunctionBody_1_1_0(b, l + 1);
    r = r && BlockFunctionBody_1_1_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // Statement*
  private static boolean BlockFunctionBody_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockFunctionBody_1_1_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!Statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "BlockFunctionBody_1_1_0", c)) break;
    }
    return true;
  }

  // WorkerWithStatementsBlock+
  private static boolean BlockFunctionBody_1_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockFunctionBody_1_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = WorkerWithStatementsBlock(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!WorkerWithStatementsBlock(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "BlockFunctionBody_1_1_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // Statement+ RIGHT_BRACE
  private static boolean BlockFunctionBody_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockFunctionBody_1_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = BlockFunctionBody_1_2_0(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // Statement+
  private static boolean BlockFunctionBody_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockFunctionBody_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Statement(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!Statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "BlockFunctionBody_1_2_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // break SEMICOLON
  public static boolean BreakStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BreakStatement")) return false;
    if (!nextTokenIs(b, BREAK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, BREAK_STATEMENT, null);
    r = consumeTokens(b, 1, BREAK, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // MapTypeName
  //                              | FutureTypeName
  //                              | XmlTypeName
  //                              | JsonTypeName
  //                              | StreamTypeName
  //                              | TableTypeName
  //                              | ServiceTypeName
  //                              | TypeDescReferenceTypeName
  //                              | ErrorTypeName
  //                              | FunctionTypeName
  public static boolean BuiltInReferenceTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BuiltInReferenceTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BUILT_IN_REFERENCE_TYPE_NAME, "<built in reference type name>");
    r = MapTypeName(b, l + 1);
    if (!r) r = FutureTypeName(b, l + 1);
    if (!r) r = XmlTypeName(b, l + 1);
    if (!r) r = JsonTypeName(b, l + 1);
    if (!r) r = StreamTypeName(b, l + 1);
    if (!r) r = TableTypeName(b, l + 1);
    if (!r) r = ServiceTypeName(b, l + 1);
    if (!r) r = TypeDescReferenceTypeName(b, l + 1);
    if (!r) r = ErrorTypeName(b, l + 1);
    if (!r) r = FunctionTypeName(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // catch (LEFT_PARENTHESIS TypeName identifier RIGHT_PARENTHESIS (LEFT_BRACE Block RIGHT_BRACE))
  public static boolean CatchClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CatchClause")) return false;
    if (!nextTokenIs(b, CATCH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CATCH_CLAUSE, null);
    r = consumeToken(b, CATCH);
    p = r; // pin = 1
    r = r && CatchClause_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_PARENTHESIS TypeName identifier RIGHT_PARENTHESIS (LEFT_BRACE Block RIGHT_BRACE)
  private static boolean CatchClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CatchClause_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, TypeName(b, l + 1, -1));
    r = p && report_error_(b, consumeTokens(b, -1, IDENTIFIER, RIGHT_PARENTHESIS)) && r;
    r = p && CatchClause_1_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean CatchClause_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CatchClause_1_4")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // CatchClause+ FinallyClause? | FinallyClause
  public static boolean CatchClauses(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CatchClauses")) return false;
    if (!nextTokenIs(b, "<catch clauses>", CATCH, FINALLY)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CATCH_CLAUSES, "<catch clauses>");
    r = CatchClauses_0(b, l + 1);
    if (!r) r = FinallyClause(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // CatchClause+ FinallyClause?
  private static boolean CatchClauses_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CatchClauses_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = CatchClauses_0_0(b, l + 1);
    r = r && CatchClauses_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // CatchClause+
  private static boolean CatchClauses_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CatchClauses_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = CatchClause(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!CatchClause(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CatchClauses_0_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // FinallyClause?
  private static boolean CatchClauses_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CatchClauses_0_1")) return false;
    FinallyClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // LEFT_CLOSED_RECORD_DELIMITER PIPE FieldBindingPattern (COMMA FieldBindingPattern)* PIPE RIGHT_CLOSED_RECORD_DELIMITER
  public static boolean ClosedRecordBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClosedRecordBindingPattern")) return false;
    if (!nextTokenIs(b, LEFT_CLOSED_RECORD_DELIMITER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CLOSED_RECORD_BINDING_PATTERN, null);
    r = consumeTokens(b, 2, LEFT_CLOSED_RECORD_DELIMITER, PIPE);
    p = r; // pin = 2
    r = r && report_error_(b, FieldBindingPattern(b, l + 1));
    r = p && report_error_(b, ClosedRecordBindingPattern_3(b, l + 1)) && r;
    r = p && report_error_(b, consumeTokens(b, -1, PIPE, RIGHT_CLOSED_RECORD_DELIMITER)) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA FieldBindingPattern)*
  private static boolean ClosedRecordBindingPattern_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClosedRecordBindingPattern_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ClosedRecordBindingPattern_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ClosedRecordBindingPattern_3", c)) break;
    }
    return true;
  }

  // COMMA FieldBindingPattern
  private static boolean ClosedRecordBindingPattern_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClosedRecordBindingPattern_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && FieldBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CommittedClause AbortedClause | AbortedClause CommittedClause | AbortedClause | CommittedClause
  public static boolean CommittedAbortedClauses(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CommittedAbortedClauses")) return false;
    if (!nextTokenIs(b, "<committed aborted clauses>", ABORTED, COMMITTED)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMMITTED_ABORTED_CLAUSES, "<committed aborted clauses>");
    r = CommittedAbortedClauses_0(b, l + 1);
    if (!r) r = CommittedAbortedClauses_1(b, l + 1);
    if (!r) r = AbortedClause(b, l + 1);
    if (!r) r = CommittedClause(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // CommittedClause AbortedClause
  private static boolean CommittedAbortedClauses_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CommittedAbortedClauses_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = CommittedClause(b, l + 1);
    r = r && AbortedClause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // AbortedClause CommittedClause
  private static boolean CommittedAbortedClauses_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CommittedAbortedClauses_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = AbortedClause(b, l + 1);
    r = r && CommittedClause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // committed LEFT_BRACE Block RIGHT_BRACE
  public static boolean CommittedClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CommittedClause")) return false;
    if (!nextTokenIs(b, COMMITTED)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, COMMITTED_CLAUSE, null);
    r = consumeTokens(b, 1, COMMITTED, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
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
  // VariableReference CompoundOperator Expression SEMICOLON
  public static boolean CompoundAssignmentStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompoundAssignmentStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_ASSIGNMENT_STATEMENT, "<compound assignment statement>");
    r = VariableReference(b, l + 1, -1);
    r = r && CompoundOperator(b, l + 1);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // COMPOUND_ADD
  //                     | COMPOUND_SUB
  //                     | COMPOUND_MUL
  //                     | COMPOUND_DIV
  //                     | COMPOUND_BIT_AND
  //                     | COMPOUND_BIT_OR
  //                     | COMPOUND_BIT_XOR
  //                     | COMPOUND_LEFT_SHIFT
  //                     | COMPOUND_RIGHT_SHIFT
  //                     | COMPOUND_LOGICAL_SHIFT
  public static boolean CompoundOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompoundOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_OPERATOR, "<compound operator>");
    r = consumeToken(b, COMPOUND_ADD);
    if (!r) r = consumeToken(b, COMPOUND_SUB);
    if (!r) r = consumeToken(b, COMPOUND_MUL);
    if (!r) r = consumeToken(b, COMPOUND_DIV);
    if (!r) r = consumeToken(b, COMPOUND_BIT_AND);
    if (!r) r = consumeToken(b, COMPOUND_BIT_OR);
    if (!r) r = consumeToken(b, COMPOUND_BIT_XOR);
    if (!r) r = consumeToken(b, COMPOUND_LEFT_SHIFT);
    if (!r) r = consumeToken(b, COMPOUND_RIGHT_SHIFT);
    if (!r) r = consumeToken(b, COMPOUND_LOGICAL_SHIFT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // public? const (TypeName identifier  | identifier) ASSIGN ConstantExpression SEMICOLON
  public static boolean ConstantDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstantDefinition")) return false;
    if (!nextTokenIs(b, "<constant definition>", CONST, PUBLIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONSTANT_DEFINITION, "<constant definition>");
    r = ConstantDefinition_0(b, l + 1);
    r = r && consumeToken(b, CONST);
    p = r; // pin = 2
    r = r && report_error_(b, ConstantDefinition_2(b, l + 1));
    r = p && report_error_(b, consumeToken(b, ASSIGN)) && r;
    r = p && report_error_(b, ConstantExpression(b, l + 1, -1)) && r;
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

  // TypeName identifier  | identifier
  private static boolean ConstantDefinition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstantDefinition_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ConstantDefinition_2_0(b, l + 1);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // TypeName identifier
  private static boolean ConstantDefinition_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstantDefinition_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeName(b, l + 1, -1);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // continue SEMICOLON
  public static boolean ContinueStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ContinueStatement")) return false;
    if (!nextTokenIs(b, CONTINUE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONTINUE_STATEMENT, null);
    r = consumeTokens(b, 1, CONTINUE, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // Parameter ASSIGN Expression
  public static boolean DefaultableParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DefaultableParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DEFAULTABLE_PARAMETER, "<defaultable parameter>");
    r = Parameter(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TypeDefinition
  //                | ServiceDefinition
  //                | GlobalVariableDefinition
  //                | FunctionDefinition
  //                | AnnotationDefinition
  //                | ConstantDefinition
  public static boolean Definition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Definition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DEFINITION, "<definition>");
    r = TypeDefinition(b, l + 1);
    if (!r) r = ServiceDefinition(b, l + 1);
    if (!r) r = GlobalVariableDefinition(b, l + 1);
    if (!r) r = FunctionDefinition(b, l + 1);
    if (!r) r = AnnotationDefinition(b, l + 1);
    if (!r) r = ConstantDefinition(b, l + 1);
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
  // do LEFT_BRACE Statement* RIGHT_BRACE
  public static boolean DoClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DoClause")) return false;
    if (!nextTokenIs(b, DO)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DO_CLAUSE, null);
    r = consumeTokens(b, 1, DO, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, DoClause_2(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // Statement*
  private static boolean DoClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DoClause_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!Statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "DoClause_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // source? DualAttachPointIdent
  public static boolean DualAttachPoint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DualAttachPoint")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DUAL_ATTACH_POINT, "<dual attach point>");
    r = DualAttachPoint_0(b, l + 1);
    r = r && DualAttachPointIdent(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // source?
  private static boolean DualAttachPoint_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DualAttachPoint_0")) return false;
    consumeToken(b, SOURCE);
    return true;
  }

  /* ********************************************************** */
  // object? type
  //                        | (object | resource)? function
  //                        | TYPE_PARAMETER
  //                        | return
  //                        | service
  //                        | (object | record)? TYPE_FIELD
  public static boolean DualAttachPointIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DualAttachPointIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DUAL_ATTACH_POINT_IDENT, "<dual attach point ident>");
    r = DualAttachPointIdent_0(b, l + 1);
    if (!r) r = DualAttachPointIdent_1(b, l + 1);
    if (!r) r = consumeToken(b, TYPE_PARAMETER);
    if (!r) r = consumeToken(b, RETURN);
    if (!r) r = consumeToken(b, SERVICE);
    if (!r) r = DualAttachPointIdent_5(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // object? type
  private static boolean DualAttachPointIdent_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DualAttachPointIdent_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = DualAttachPointIdent_0_0(b, l + 1);
    r = r && consumeToken(b, TYPE);
    exit_section_(b, m, null, r);
    return r;
  }

  // object?
  private static boolean DualAttachPointIdent_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DualAttachPointIdent_0_0")) return false;
    consumeToken(b, OBJECT);
    return true;
  }

  // (object | resource)? function
  private static boolean DualAttachPointIdent_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DualAttachPointIdent_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = DualAttachPointIdent_1_0(b, l + 1);
    r = r && consumeToken(b, FUNCTION);
    exit_section_(b, m, null, r);
    return r;
  }

  // (object | resource)?
  private static boolean DualAttachPointIdent_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DualAttachPointIdent_1_0")) return false;
    DualAttachPointIdent_1_0_0(b, l + 1);
    return true;
  }

  // object | resource
  private static boolean DualAttachPointIdent_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DualAttachPointIdent_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, OBJECT);
    if (!r) r = consumeToken(b, RESOURCE);
    return r;
  }

  // (object | record)? TYPE_FIELD
  private static boolean DualAttachPointIdent_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DualAttachPointIdent_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = DualAttachPointIdent_5_0(b, l + 1);
    r = r && consumeToken(b, TYPE_FIELD);
    exit_section_(b, m, null, r);
    return r;
  }

  // (object | record)?
  private static boolean DualAttachPointIdent_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DualAttachPointIdent_5_0")) return false;
    DualAttachPointIdent_5_0_0(b, l + 1);
    return true;
  }

  // object | record
  private static boolean DualAttachPointIdent_5_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DualAttachPointIdent_5_0_0")) return false;
    boolean r;
    r = consumeToken(b, OBJECT);
    if (!r) r = consumeToken(b, RECORD);
    return r;
  }

  /* ********************************************************** */
  // else (LEFT_BRACE Block RIGHT_BRACE)
  public static boolean ElseClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ElseClause")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ELSE_CLAUSE, null);
    r = consumeToken(b, ELSE);
    p = r; // pin = 1
    r = r && ElseClause_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean ElseClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ElseClause_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // else pinnedElseClause
  public static boolean ElseIfClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ElseIfClause")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSE);
    r = r && pinnedElseClause(b, l + 1);
    exit_section_(b, m, ELSE_IF_CLAUSE, r);
    return r;
  }

  /* ********************************************************** */
  // FieldBindingPattern (COMMA FieldBindingPattern)* (COMMA RestBindingPattern)? | RestBindingPattern
  public static boolean EntryBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryBindingPattern")) return false;
    if (!nextTokenIs(b, "<entry binding pattern>", ELLIPSIS, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENTRY_BINDING_PATTERN, "<entry binding pattern>");
    r = EntryBindingPattern_0(b, l + 1);
    if (!r) r = RestBindingPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // FieldBindingPattern (COMMA FieldBindingPattern)* (COMMA RestBindingPattern)?
  private static boolean EntryBindingPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryBindingPattern_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = FieldBindingPattern(b, l + 1);
    r = r && EntryBindingPattern_0_1(b, l + 1);
    r = r && EntryBindingPattern_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA FieldBindingPattern)*
  private static boolean EntryBindingPattern_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryBindingPattern_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!EntryBindingPattern_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "EntryBindingPattern_0_1", c)) break;
    }
    return true;
  }

  // COMMA FieldBindingPattern
  private static boolean EntryBindingPattern_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryBindingPattern_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && FieldBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA RestBindingPattern)?
  private static boolean EntryBindingPattern_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryBindingPattern_0_2")) return false;
    EntryBindingPattern_0_2_0(b, l + 1);
    return true;
  }

  // COMMA RestBindingPattern
  private static boolean EntryBindingPattern_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryBindingPattern_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && RestBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // FieldRefBindingPattern (COMMA FieldRefBindingPattern)* (COMMA RestRefBindingPattern)? | RestRefBindingPattern?
  public static boolean EntryRefBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryRefBindingPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENTRY_REF_BINDING_PATTERN, "<entry ref binding pattern>");
    r = EntryRefBindingPattern_0(b, l + 1);
    if (!r) r = EntryRefBindingPattern_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // FieldRefBindingPattern (COMMA FieldRefBindingPattern)* (COMMA RestRefBindingPattern)?
  private static boolean EntryRefBindingPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryRefBindingPattern_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = FieldRefBindingPattern(b, l + 1);
    r = r && EntryRefBindingPattern_0_1(b, l + 1);
    r = r && EntryRefBindingPattern_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA FieldRefBindingPattern)*
  private static boolean EntryRefBindingPattern_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryRefBindingPattern_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!EntryRefBindingPattern_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "EntryRefBindingPattern_0_1", c)) break;
    }
    return true;
  }

  // COMMA FieldRefBindingPattern
  private static boolean EntryRefBindingPattern_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryRefBindingPattern_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && FieldRefBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA RestRefBindingPattern)?
  private static boolean EntryRefBindingPattern_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryRefBindingPattern_0_2")) return false;
    EntryRefBindingPattern_0_2_0(b, l + 1);
    return true;
  }

  // COMMA RestRefBindingPattern
  private static boolean EntryRefBindingPattern_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryRefBindingPattern_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && RestRefBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // RestRefBindingPattern?
  private static boolean EntryRefBindingPattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EntryRefBindingPattern_1")) return false;
    RestRefBindingPattern(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // SimpleMatchPattern (COMMA ErrorDetailBindingPattern)* (COMMA RestMatchPattern)?
  //                           | ErrorDetailBindingPattern (COMMA ErrorDetailBindingPattern)* (COMMA RestMatchPattern)?
  //                           | RestMatchPattern
  public static boolean ErrorArgListMatchPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorArgListMatchPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ERROR_ARG_LIST_MATCH_PATTERN, "<error arg list match pattern>");
    r = ErrorArgListMatchPattern_0(b, l + 1);
    if (!r) r = ErrorArgListMatchPattern_1(b, l + 1);
    if (!r) r = RestMatchPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SimpleMatchPattern (COMMA ErrorDetailBindingPattern)* (COMMA RestMatchPattern)?
  private static boolean ErrorArgListMatchPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorArgListMatchPattern_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = SimpleMatchPattern(b, l + 1);
    r = r && ErrorArgListMatchPattern_0_1(b, l + 1);
    r = r && ErrorArgListMatchPattern_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ErrorDetailBindingPattern)*
  private static boolean ErrorArgListMatchPattern_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorArgListMatchPattern_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ErrorArgListMatchPattern_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ErrorArgListMatchPattern_0_1", c)) break;
    }
    return true;
  }

  // COMMA ErrorDetailBindingPattern
  private static boolean ErrorArgListMatchPattern_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorArgListMatchPattern_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ErrorDetailBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA RestMatchPattern)?
  private static boolean ErrorArgListMatchPattern_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorArgListMatchPattern_0_2")) return false;
    ErrorArgListMatchPattern_0_2_0(b, l + 1);
    return true;
  }

  // COMMA RestMatchPattern
  private static boolean ErrorArgListMatchPattern_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorArgListMatchPattern_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && RestMatchPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ErrorDetailBindingPattern (COMMA ErrorDetailBindingPattern)* (COMMA RestMatchPattern)?
  private static boolean ErrorArgListMatchPattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorArgListMatchPattern_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ErrorDetailBindingPattern(b, l + 1);
    r = r && ErrorArgListMatchPattern_1_1(b, l + 1);
    r = r && ErrorArgListMatchPattern_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ErrorDetailBindingPattern)*
  private static boolean ErrorArgListMatchPattern_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorArgListMatchPattern_1_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ErrorArgListMatchPattern_1_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ErrorArgListMatchPattern_1_1", c)) break;
    }
    return true;
  }

  // COMMA ErrorDetailBindingPattern
  private static boolean ErrorArgListMatchPattern_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorArgListMatchPattern_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ErrorDetailBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA RestMatchPattern)?
  private static boolean ErrorArgListMatchPattern_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorArgListMatchPattern_1_2")) return false;
    ErrorArgListMatchPattern_1_2_0(b, l + 1);
    return true;
  }

  // COMMA RestMatchPattern
  private static boolean ErrorArgListMatchPattern_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorArgListMatchPattern_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && RestMatchPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (error LEFT_PARENTHESIS identifier (COMMA ErrorDetailBindingPattern)* (COMMA ErrorRestBindingPattern)? RIGHT_PARENTHESIS)
  //                            | (TypeName LEFT_PARENTHESIS ErrorFieldBindingPatterns RIGHT_PARENTHESIS)
  public static boolean ErrorBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorBindingPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ERROR_BINDING_PATTERN, "<error binding pattern>");
    r = ErrorBindingPattern_0(b, l + 1);
    if (!r) r = ErrorBindingPattern_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // error LEFT_PARENTHESIS identifier (COMMA ErrorDetailBindingPattern)* (COMMA ErrorRestBindingPattern)? RIGHT_PARENTHESIS
  private static boolean ErrorBindingPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorBindingPattern_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ERROR, LEFT_PARENTHESIS, IDENTIFIER);
    r = r && ErrorBindingPattern_0_3(b, l + 1);
    r = r && ErrorBindingPattern_0_4(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ErrorDetailBindingPattern)*
  private static boolean ErrorBindingPattern_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorBindingPattern_0_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ErrorBindingPattern_0_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ErrorBindingPattern_0_3", c)) break;
    }
    return true;
  }

  // COMMA ErrorDetailBindingPattern
  private static boolean ErrorBindingPattern_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorBindingPattern_0_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ErrorDetailBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ErrorRestBindingPattern)?
  private static boolean ErrorBindingPattern_0_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorBindingPattern_0_4")) return false;
    ErrorBindingPattern_0_4_0(b, l + 1);
    return true;
  }

  // COMMA ErrorRestBindingPattern
  private static boolean ErrorBindingPattern_0_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorBindingPattern_0_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ErrorRestBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // TypeName LEFT_PARENTHESIS ErrorFieldBindingPatterns RIGHT_PARENTHESIS
  private static boolean ErrorBindingPattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorBindingPattern_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeName(b, l + 1, -1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    r = r && ErrorFieldBindingPatterns(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ErrorRefBindingPattern ASSIGN Expression SEMICOLON
  public static boolean ErrorDestructuringStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorDestructuringStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ERROR_DESTRUCTURING_STATEMENT, "<error destructuring statement>");
    r = ErrorRefBindingPattern(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier ASSIGN BindingPattern
  public static boolean ErrorDetailBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorDetailBindingPattern")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, ASSIGN);
    r = r && BindingPattern(b, l + 1);
    exit_section_(b, m, ERROR_DETAIL_BINDING_PATTERN, r);
    return r;
  }

  /* ********************************************************** */
  // ErrorDetailBindingPattern (COMMA ErrorDetailBindingPattern)* (COMMA ErrorRestBindingPattern)? | ErrorRestBindingPattern
  public static boolean ErrorFieldBindingPatterns(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorFieldBindingPatterns")) return false;
    if (!nextTokenIs(b, "<error field binding patterns>", ELLIPSIS, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ERROR_FIELD_BINDING_PATTERNS, "<error field binding patterns>");
    r = ErrorFieldBindingPatterns_0(b, l + 1);
    if (!r) r = ErrorRestBindingPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ErrorDetailBindingPattern (COMMA ErrorDetailBindingPattern)* (COMMA ErrorRestBindingPattern)?
  private static boolean ErrorFieldBindingPatterns_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorFieldBindingPatterns_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ErrorDetailBindingPattern(b, l + 1);
    r = r && ErrorFieldBindingPatterns_0_1(b, l + 1);
    r = r && ErrorFieldBindingPatterns_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ErrorDetailBindingPattern)*
  private static boolean ErrorFieldBindingPatterns_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorFieldBindingPatterns_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ErrorFieldBindingPatterns_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ErrorFieldBindingPatterns_0_1", c)) break;
    }
    return true;
  }

  // COMMA ErrorDetailBindingPattern
  private static boolean ErrorFieldBindingPatterns_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorFieldBindingPatterns_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ErrorDetailBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ErrorRestBindingPattern)?
  private static boolean ErrorFieldBindingPatterns_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorFieldBindingPatterns_0_2")) return false;
    ErrorFieldBindingPatterns_0_2_0(b, l + 1);
    return true;
  }

  // COMMA ErrorRestBindingPattern
  private static boolean ErrorFieldBindingPatterns_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorFieldBindingPatterns_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ErrorRestBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ErrorDetailBindingPattern (COMMA ErrorDetailBindingPattern)* (COMMA RestMatchPattern)? | RestMatchPattern
  public static boolean ErrorFieldMatchPatterns(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorFieldMatchPatterns")) return false;
    if (!nextTokenIs(b, "<error field match patterns>", ELLIPSIS, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ERROR_FIELD_MATCH_PATTERNS, "<error field match patterns>");
    r = ErrorFieldMatchPatterns_0(b, l + 1);
    if (!r) r = RestMatchPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ErrorDetailBindingPattern (COMMA ErrorDetailBindingPattern)* (COMMA RestMatchPattern)?
  private static boolean ErrorFieldMatchPatterns_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorFieldMatchPatterns_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ErrorDetailBindingPattern(b, l + 1);
    r = r && ErrorFieldMatchPatterns_0_1(b, l + 1);
    r = r && ErrorFieldMatchPatterns_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ErrorDetailBindingPattern)*
  private static boolean ErrorFieldMatchPatterns_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorFieldMatchPatterns_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ErrorFieldMatchPatterns_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ErrorFieldMatchPatterns_0_1", c)) break;
    }
    return true;
  }

  // COMMA ErrorDetailBindingPattern
  private static boolean ErrorFieldMatchPatterns_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorFieldMatchPatterns_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ErrorDetailBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA RestMatchPattern)?
  private static boolean ErrorFieldMatchPatterns_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorFieldMatchPatterns_0_2")) return false;
    ErrorFieldMatchPatterns_0_2_0(b, l + 1);
    return true;
  }

  // COMMA RestMatchPattern
  private static boolean ErrorFieldMatchPatterns_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorFieldMatchPatterns_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && RestMatchPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // error LEFT_PARENTHESIS ErrorArgListMatchPattern RIGHT_PARENTHESIS | TypeName LEFT_PARENTHESIS ErrorFieldMatchPatterns RIGHT_PARENTHESIS
  public static boolean ErrorMatchPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorMatchPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ERROR_MATCH_PATTERN, "<error match pattern>");
    r = ErrorMatchPattern_0(b, l + 1);
    if (!r) r = ErrorMatchPattern_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // error LEFT_PARENTHESIS ErrorArgListMatchPattern RIGHT_PARENTHESIS
  private static boolean ErrorMatchPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorMatchPattern_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ERROR, LEFT_PARENTHESIS);
    r = r && ErrorArgListMatchPattern(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  // TypeName LEFT_PARENTHESIS ErrorFieldMatchPatterns RIGHT_PARENTHESIS
  private static boolean ErrorMatchPattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorMatchPattern_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeName(b, l + 1, -1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    r = r && ErrorFieldMatchPatterns(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ErrorMatchPattern (if Expression)? EQUAL_GT LEFT_BRACE Statement* RIGHT_BRACE
  public static boolean ErrorMatchPatternClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorMatchPatternClause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ERROR_MATCH_PATTERN_CLAUSE, "<error match pattern clause>");
    r = ErrorMatchPattern(b, l + 1);
    r = r && ErrorMatchPatternClause_1(b, l + 1);
    r = r && consumeTokens(b, 0, EQUAL_GT, LEFT_BRACE);
    r = r && ErrorMatchPatternClause_4(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (if Expression)?
  private static boolean ErrorMatchPatternClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorMatchPatternClause_1")) return false;
    ErrorMatchPatternClause_1_0(b, l + 1);
    return true;
  }

  // if Expression
  private static boolean ErrorMatchPatternClause_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorMatchPatternClause_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IF);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // Statement*
  private static boolean ErrorMatchPatternClause_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorMatchPatternClause_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!Statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ErrorMatchPatternClause_4", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // identifier ASSIGN BindingRefPattern
  public static boolean ErrorNamedArgRefPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorNamedArgRefPattern")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, ASSIGN);
    r = r && BindingRefPattern(b, l + 1);
    exit_section_(b, m, ERROR_NAMED_ARG_REF_PATTERN, r);
    return r;
  }

  /* ********************************************************** */
  // (error LEFT_PARENTHESIS ((VariableReference (COMMA ErrorNamedArgRefPattern)*) | ErrorNamedArgRefPattern+) (COMMA ErrorRefRestPattern)? RIGHT_PARENTHESIS)
  //                             | (error LEFT_PARENTHESIS ErrorRefRestPattern RIGHT_PARENTHESIS)
  //                             | (TypeName LEFT_PARENTHESIS ErrorNamedArgRefPattern (COMMA ErrorNamedArgRefPattern)*  (COMMA ErrorRefRestPattern)? RIGHT_PARENTHESIS)
  public static boolean ErrorRefBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ERROR_REF_BINDING_PATTERN, "<error ref binding pattern>");
    r = ErrorRefBindingPattern_0(b, l + 1);
    if (!r) r = ErrorRefBindingPattern_1(b, l + 1);
    if (!r) r = ErrorRefBindingPattern_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // error LEFT_PARENTHESIS ((VariableReference (COMMA ErrorNamedArgRefPattern)*) | ErrorNamedArgRefPattern+) (COMMA ErrorRefRestPattern)? RIGHT_PARENTHESIS
  private static boolean ErrorRefBindingPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ERROR, LEFT_PARENTHESIS);
    r = r && ErrorRefBindingPattern_0_2(b, l + 1);
    r = r && ErrorRefBindingPattern_0_3(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  // (VariableReference (COMMA ErrorNamedArgRefPattern)*) | ErrorNamedArgRefPattern+
  private static boolean ErrorRefBindingPattern_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ErrorRefBindingPattern_0_2_0(b, l + 1);
    if (!r) r = ErrorRefBindingPattern_0_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // VariableReference (COMMA ErrorNamedArgRefPattern)*
  private static boolean ErrorRefBindingPattern_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = VariableReference(b, l + 1, -1);
    r = r && ErrorRefBindingPattern_0_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ErrorNamedArgRefPattern)*
  private static boolean ErrorRefBindingPattern_0_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_0_2_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ErrorRefBindingPattern_0_2_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ErrorRefBindingPattern_0_2_0_1", c)) break;
    }
    return true;
  }

  // COMMA ErrorNamedArgRefPattern
  private static boolean ErrorRefBindingPattern_0_2_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_0_2_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ErrorNamedArgRefPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ErrorNamedArgRefPattern+
  private static boolean ErrorRefBindingPattern_0_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_0_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ErrorNamedArgRefPattern(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!ErrorNamedArgRefPattern(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ErrorRefBindingPattern_0_2_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ErrorRefRestPattern)?
  private static boolean ErrorRefBindingPattern_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_0_3")) return false;
    ErrorRefBindingPattern_0_3_0(b, l + 1);
    return true;
  }

  // COMMA ErrorRefRestPattern
  private static boolean ErrorRefBindingPattern_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_0_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ErrorRefRestPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // error LEFT_PARENTHESIS ErrorRefRestPattern RIGHT_PARENTHESIS
  private static boolean ErrorRefBindingPattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ERROR, LEFT_PARENTHESIS);
    r = r && ErrorRefRestPattern(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  // TypeName LEFT_PARENTHESIS ErrorNamedArgRefPattern (COMMA ErrorNamedArgRefPattern)*  (COMMA ErrorRefRestPattern)? RIGHT_PARENTHESIS
  private static boolean ErrorRefBindingPattern_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeName(b, l + 1, -1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    r = r && ErrorNamedArgRefPattern(b, l + 1);
    r = r && ErrorRefBindingPattern_2_3(b, l + 1);
    r = r && ErrorRefBindingPattern_2_4(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ErrorNamedArgRefPattern)*
  private static boolean ErrorRefBindingPattern_2_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_2_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ErrorRefBindingPattern_2_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ErrorRefBindingPattern_2_3", c)) break;
    }
    return true;
  }

  // COMMA ErrorNamedArgRefPattern
  private static boolean ErrorRefBindingPattern_2_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_2_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ErrorNamedArgRefPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ErrorRefRestPattern)?
  private static boolean ErrorRefBindingPattern_2_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_2_4")) return false;
    ErrorRefBindingPattern_2_4_0(b, l + 1);
    return true;
  }

  // COMMA ErrorRefRestPattern
  private static boolean ErrorRefBindingPattern_2_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_2_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ErrorRefRestPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ELLIPSIS VariableReference
  public static boolean ErrorRefRestPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefRestPattern")) return false;
    if (!nextTokenIs(b, ELLIPSIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELLIPSIS);
    r = r && VariableReference(b, l + 1, -1);
    exit_section_(b, m, ERROR_REF_REST_PATTERN, r);
    return r;
  }

  /* ********************************************************** */
  // ELLIPSIS identifier
  public static boolean ErrorRestBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRestBindingPattern")) return false;
    if (!nextTokenIs(b, ELLIPSIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ELLIPSIS, IDENTIFIER);
    exit_section_(b, m, ERROR_REST_BINDING_PATTERN, r);
    return r;
  }

  /* ********************************************************** */
  // error (LT TypeName (COMMA TypeName)? GT)?
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

  // (LT TypeName (COMMA TypeName)? GT)?
  private static boolean ErrorTypeName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorTypeName_1")) return false;
    ErrorTypeName_1_0(b, l + 1);
    return true;
  }

  // LT TypeName (COMMA TypeName)? GT
  private static boolean ErrorTypeName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorTypeName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && TypeName(b, l + 1, -1);
    r = r && ErrorTypeName_1_0_2(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA TypeName)?
  private static boolean ErrorTypeName_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorTypeName_1_0_2")) return false;
    ErrorTypeName_1_0_2_0(b, l + 1);
    return true;
  }

  // COMMA TypeName
  private static boolean ErrorTypeName_1_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorTypeName_1_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && TypeName(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // function FunctionSignature (BlockFunctionBody | ExprFunctionBody)
  public static boolean ExplicitAnonymousFunctionExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExplicitAnonymousFunctionExpr")) return false;
    if (!nextTokenIs(b, FUNCTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXPLICIT_ANONYMOUS_FUNCTION_EXPR, null);
    r = consumeToken(b, FUNCTION);
    p = r; // pin = 1
    r = r && report_error_(b, FunctionSignature(b, l + 1));
    r = p && ExplicitAnonymousFunctionExpr_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // BlockFunctionBody | ExprFunctionBody
  private static boolean ExplicitAnonymousFunctionExpr_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExplicitAnonymousFunctionExpr_2")) return false;
    boolean r;
    r = BlockFunctionBody(b, l + 1);
    if (!r) r = ExprFunctionBody(b, l + 1);
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
  // EQUAL_GT Expression
  public static boolean ExprFunctionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExprFunctionBody")) return false;
    if (!nextTokenIs(b, EQUAL_GT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXPR_FUNCTION_BODY, null);
    r = consumeToken(b, EQUAL_GT);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // EQUAL_GT Expression
  public static boolean ExprFunctionBodySpec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExprFunctionBodySpec")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXPR_FUNCTION_BODY_SPEC, "<expr function body spec>");
    r = consumeToken(b, EQUAL_GT);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, ExprFuncBodyRecover_parser_);
    return r || p;
  }

  /* ********************************************************** */
  // Expression (COMMA Expression)*
  public static boolean ExpressionList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExpressionList")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION_LIST, "<expression list>");
    r = Expression(b, l + 1, -1);
    p = r; // pin = 1
    r = r && ExpressionList_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA Expression)*
  private static boolean ExpressionList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExpressionList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ExpressionList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ExpressionList_1", c)) break;
    }
    return true;
  }

  // COMMA Expression
  private static boolean ExpressionList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExpressionList_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // !(NULL_LITERAL|int|string|float|boolean|byte|any|map|table|typeof|function|stream|'}'|';'|var|while|match|foreach|continue|break|fork|try|throw|return|abort|aborted|committed|fail|lock|xmlns|transaction|if|forever|object|trap|wait|flush|error|check|checkpanic)
  static boolean ExpressionRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExpressionRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !ExpressionRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // NULL_LITERAL|int|string|float|boolean|byte|any|map|table|typeof|function|stream|'}'|';'|var|while|match|foreach|continue|break|fork|try|throw|return|abort|aborted|committed|fail|lock|xmlns|transaction|if|forever|object|trap|wait|flush|error|check|checkpanic
  private static boolean ExpressionRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExpressionRecover_0")) return false;
    boolean r;
    r = consumeToken(b, NULL_LITERAL);
    if (!r) r = consumeToken(b, INT);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, FLOAT);
    if (!r) r = consumeToken(b, BOOLEAN);
    if (!r) r = consumeToken(b, BYTE);
    if (!r) r = consumeToken(b, ANY);
    if (!r) r = consumeToken(b, MAP);
    if (!r) r = consumeToken(b, TABLE);
    if (!r) r = consumeToken(b, TYPEOF);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, STREAM);
    if (!r) r = consumeToken(b, RIGHT_BRACE);
    if (!r) r = consumeToken(b, SEMICOLON);
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
    if (!r) r = consumeToken(b, FAIL);
    if (!r) r = consumeToken(b, LOCK);
    if (!r) r = consumeToken(b, XMLNS);
    if (!r) r = consumeToken(b, TRANSACTION);
    if (!r) r = consumeToken(b, IF);
    if (!r) r = consumeToken(b, FOREVER);
    if (!r) r = consumeToken(b, OBJECT);
    if (!r) r = consumeToken(b, TRAP);
    if (!r) r = consumeToken(b, WAIT);
    if (!r) r = consumeToken(b, FLUSH);
    if (!r) r = consumeToken(b, ERROR);
    if (!r) r = consumeToken(b, CHECK);
    if (!r) r = consumeToken(b, CHECKPANIC);
    return r;
  }

  /* ********************************************************** */
  // Expression SEMICOLON
  public static boolean ExpressionStmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExpressionStmt")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION_STMT, "<expression stmt>");
    r = Expression(b, l + 1, -1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ASSIGN AnnotationAttachment* external
  public static boolean ExternalFunctionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExternalFunctionBody")) return false;
    if (!nextTokenIs(b, ASSIGN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASSIGN);
    r = r && ExternalFunctionBody_1(b, l + 1);
    r = r && consumeToken(b, EXTERNAL);
    exit_section_(b, m, EXTERNAL_FUNCTION_BODY, r);
    return r;
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
  // (DOT | OPTIONAL_FIELD_ACCESS) ((identifier COLON)? identifier | MUL)
  public static boolean Field(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Field")) return false;
    if (!nextTokenIs(b, "<field>", DOT, OPTIONAL_FIELD_ACCESS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FIELD, "<field>");
    r = Field_0(b, l + 1);
    r = r && Field_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // DOT | OPTIONAL_FIELD_ACCESS
  private static boolean Field_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Field_0")) return false;
    boolean r;
    r = consumeToken(b, DOT);
    if (!r) r = consumeToken(b, OPTIONAL_FIELD_ACCESS);
    return r;
  }

  // (identifier COLON)? identifier | MUL
  private static boolean Field_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Field_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Field_1_0(b, l + 1);
    if (!r) r = consumeToken(b, MUL);
    exit_section_(b, m, null, r);
    return r;
  }

  // (identifier COLON)? identifier
  private static boolean Field_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Field_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Field_1_0_0(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // (identifier COLON)?
  private static boolean Field_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Field_1_0_0")) return false;
    Field_1_0_0_0(b, l + 1);
    return true;
  }

  // identifier COLON
  private static boolean Field_1_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Field_1_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // identifier (COLON BindingPattern)?
  public static boolean FieldBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldBindingPattern")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD_BINDING_PATTERN, null);
    r = consumeToken(b, IDENTIFIER);
    p = r; // pin = 1
    r = r && FieldBindingPattern_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COLON BindingPattern)?
  private static boolean FieldBindingPattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldBindingPattern_1")) return false;
    FieldBindingPattern_1_0(b, l + 1);
    return true;
  }

  // COLON BindingPattern
  private static boolean FieldBindingPattern_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldBindingPattern_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && BindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // documentationString? AnnotationAttachment* TypeName identifier QUESTION_MARK? (ASSIGN Expression)? SEMICOLON
  public static boolean FieldDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD_DEFINITION, "<field definition>");
    r = FieldDefinition_0(b, l + 1);
    r = r && FieldDefinition_1(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    p = r; // pin = 3
    r = r && report_error_(b, consumeToken(b, IDENTIFIER));
    r = p && report_error_(b, FieldDefinition_4(b, l + 1)) && r;
    r = p && report_error_(b, FieldDefinition_5(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // documentationString?
  private static boolean FieldDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition_0")) return false;
    documentationString(b, l + 1);
    return true;
  }

  // AnnotationAttachment*
  private static boolean FieldDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "FieldDefinition_1", c)) break;
    }
    return true;
  }

  // QUESTION_MARK?
  private static boolean FieldDefinition_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition_4")) return false;
    consumeToken(b, QUESTION_MARK);
    return true;
  }

  // (ASSIGN Expression)?
  private static boolean FieldDefinition_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition_5")) return false;
    FieldDefinition_5_0(b, l + 1);
    return true;
  }

  // ASSIGN Expression
  private static boolean FieldDefinition_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // FieldDefinition | TypeReference
  public static boolean FieldDescriptor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDescriptor")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FIELD_DESCRIPTOR, "<field descriptor>");
    r = FieldDefinition(b, l + 1);
    if (!r) r = TypeReference(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier (COLON BindingRefPattern)?
  public static boolean FieldRefBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldRefBindingPattern")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD_REF_BINDING_PATTERN, null);
    r = consumeToken(b, IDENTIFIER);
    p = r; // pin = 1
    r = r && FieldRefBindingPattern_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COLON BindingRefPattern)?
  private static boolean FieldRefBindingPattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldRefBindingPattern_1")) return false;
    FieldRefBindingPattern_1_0(b, l + 1);
    return true;
  }

  // COLON BindingRefPattern
  private static boolean FieldRefBindingPattern_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldRefBindingPattern_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && BindingRefPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // finally {LEFT_BRACE Block RIGHT_BRACE}
  public static boolean FinallyClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FinallyClause")) return false;
    if (!nextTokenIs(b, FINALLY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FINALLY_CLAUSE, null);
    r = consumeToken(b, FINALLY);
    p = r; // pin = 1
    r = r && FinallyClause_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean FinallyClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FinallyClause_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // FiniteTypeUnit (PIPE FiniteTypeUnit)*
  public static boolean FiniteType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FiniteType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FINITE_TYPE, "<finite type>");
    r = FiniteTypeUnit(b, l + 1);
    r = r && FiniteType_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (PIPE FiniteTypeUnit)*
  private static boolean FiniteType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FiniteType_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FiniteType_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "FiniteType_1", c)) break;
    }
    return true;
  }

  // PIPE FiniteTypeUnit
  private static boolean FiniteType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FiniteType_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PIPE);
    r = r && FiniteTypeUnit(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SimpleLiteral | TypeName
  public static boolean FiniteTypeUnit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FiniteTypeUnit")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FINITE_TYPE_UNIT, "<finite type unit>");
    r = SimpleLiteral(b, l + 1);
    if (!r) r = TypeName(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // DECIMAL_FLOATING_POINT_NUMBER | HEXADECIMAL_FLOATING_POINT_LITERAL
  public static boolean FloatingPointLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FloatingPointLiteral")) return false;
    if (!nextTokenIs(b, "<floating point literal>", DECIMAL_FLOATING_POINT_NUMBER, HEXADECIMAL_FLOATING_POINT_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FLOATING_POINT_LITERAL, "<floating point literal>");
    r = consumeToken(b, DECIMAL_FLOATING_POINT_NUMBER);
    if (!r) r = consumeToken(b, HEXADECIMAL_FLOATING_POINT_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // flush identifier?
  public static boolean FlushWorker(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FlushWorker")) return false;
    if (!nextTokenIs(b, FLUSH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FLUSH_WORKER, null);
    r = consumeToken(b, FLUSH);
    p = r; // pin = 1
    r = r && FlushWorker_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // identifier?
  private static boolean FlushWorker_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FlushWorker_1")) return false;
    consumeToken(b, IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // foreach (LEFT_PARENTHESIS? (TypeName | var) BindingPattern in Expression RIGHT_PARENTHESIS? (LEFT_BRACE Block RIGHT_BRACE))
  public static boolean ForeachStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeachStatement")) return false;
    if (!nextTokenIs(b, FOREACH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FOREACH_STATEMENT, null);
    r = consumeToken(b, FOREACH);
    p = r; // pin = 1
    r = r && ForeachStatement_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_PARENTHESIS? (TypeName | var) BindingPattern in Expression RIGHT_PARENTHESIS? (LEFT_BRACE Block RIGHT_BRACE)
  private static boolean ForeachStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeachStatement_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = ForeachStatement_1_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, ForeachStatement_1_1(b, l + 1));
    r = p && report_error_(b, BindingPattern(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, IN)) && r;
    r = p && report_error_(b, Expression(b, l + 1, -1)) && r;
    r = p && report_error_(b, ForeachStatement_1_5(b, l + 1)) && r;
    r = p && ForeachStatement_1_6(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_PARENTHESIS?
  private static boolean ForeachStatement_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeachStatement_1_0")) return false;
    consumeToken(b, LEFT_PARENTHESIS);
    return true;
  }

  // TypeName | var
  private static boolean ForeachStatement_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeachStatement_1_1")) return false;
    boolean r;
    r = TypeName(b, l + 1, -1);
    if (!r) r = consumeToken(b, VAR);
    return r;
  }

  // RIGHT_PARENTHESIS?
  private static boolean ForeachStatement_1_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeachStatement_1_5")) return false;
    consumeToken(b, RIGHT_PARENTHESIS);
    return true;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean ForeachStatement_1_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeachStatement_1_6")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // fork LEFT_BRACE WorkerDefinition* RIGHT_BRACE
  public static boolean ForkJoinStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForkJoinStatement")) return false;
    if (!nextTokenIs(b, FORK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FORK_JOIN_STATEMENT, null);
    r = consumeTokens(b, 1, FORK, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, ForkJoinStatement_2(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // WorkerDefinition*
  private static boolean ForkJoinStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForkJoinStatement_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!WorkerDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ForkJoinStatement_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // (DefaultableParameter | Parameter) (COMMA (DefaultableParameter | Parameter))* (COMMA RestParameter)? | RestParameter
  public static boolean FormalParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FORMAL_PARAMETER_LIST, "<formal parameter list>");
    r = FormalParameterList_0(b, l + 1);
    if (!r) r = RestParameter(b, l + 1);
    exit_section_(b, l, m, r, false, FormalParameterListRecover_parser_);
    return r;
  }

  // (DefaultableParameter | Parameter) (COMMA (DefaultableParameter | Parameter))* (COMMA RestParameter)?
  private static boolean FormalParameterList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = FormalParameterList_0_0(b, l + 1);
    r = r && FormalParameterList_0_1(b, l + 1);
    r = r && FormalParameterList_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DefaultableParameter | Parameter
  private static boolean FormalParameterList_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList_0_0")) return false;
    boolean r;
    r = DefaultableParameter(b, l + 1);
    if (!r) r = Parameter(b, l + 1);
    return r;
  }

  // (COMMA (DefaultableParameter | Parameter))*
  private static boolean FormalParameterList_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FormalParameterList_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "FormalParameterList_0_1", c)) break;
    }
    return true;
  }

  // COMMA (DefaultableParameter | Parameter)
  private static boolean FormalParameterList_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && FormalParameterList_0_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DefaultableParameter | Parameter
  private static boolean FormalParameterList_0_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList_0_1_0_1")) return false;
    boolean r;
    r = DefaultableParameter(b, l + 1);
    if (!r) r = Parameter(b, l + 1);
    return r;
  }

  // (COMMA RestParameter)?
  private static boolean FormalParameterList_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList_0_2")) return false;
    FormalParameterList_0_2_0(b, l + 1);
    return true;
  }

  // COMMA RestParameter
  private static boolean FormalParameterList_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterList_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && RestParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(')')
  static boolean FormalParameterListRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterListRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // from (TypeName | var) BindingPattern IN Expression
  public static boolean FromClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FromClause")) return false;
    if (!nextTokenIs(b, FROM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FROM_CLAUSE, null);
    r = consumeToken(b, FROM);
    p = r; // pin = 1
    r = r && report_error_(b, FromClause_1(b, l + 1));
    r = p && report_error_(b, BindingPattern(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, IN)) && r;
    r = p && Expression(b, l + 1, -1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // TypeName | var
  private static boolean FromClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FromClause_1")) return false;
    boolean r;
    r = TypeName(b, l + 1, -1);
    if (!r) r = consumeToken(b, VAR);
    return r;
  }

  /* ********************************************************** */
  // (public|private)? remote? function AnyIdentifierName FunctionSignature FunctionDefinitionBody
  public static boolean FunctionDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_DEFINITION, "<function definition>");
    r = FunctionDefinition_0(b, l + 1);
    r = r && FunctionDefinition_1(b, l + 1);
    r = r && consumeToken(b, FUNCTION);
    p = r; // pin = 3
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

  /* ********************************************************** */
  // (ExprFunctionBodySpec SEMICOLON) | (ExternalFunctionBody SEMICOLON) | BlockFunctionBody
  public static boolean FunctionDefinitionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinitionBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_DEFINITION_BODY, "<function definition body>");
    r = FunctionDefinitionBody_0(b, l + 1);
    if (!r) r = FunctionDefinitionBody_1(b, l + 1);
    if (!r) r = BlockFunctionBody(b, l + 1);
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
  // functionNameReference LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS
  public static boolean FunctionInvocation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionInvocation")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_INVOCATION, "<function invocation>");
    r = functionNameReference(b, l + 1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 2
    r = r && report_error_(b, FunctionInvocation_2(b, l + 1));
    r = p && consumeToken(b, RIGHT_PARENTHESIS) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // InvocationArgList?
  private static boolean FunctionInvocation_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionInvocation_2")) return false;
    InvocationArgList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // LEFT_PARENTHESIS FormalParameterList? RIGHT_PARENTHESIS ReturnParameter?
  public static boolean FunctionSignature(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionSignature")) return false;
    if (!nextTokenIs(b, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_PARENTHESIS);
    r = r && FunctionSignature_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    r = r && FunctionSignature_3(b, l + 1);
    exit_section_(b, m, FUNCTION_SIGNATURE, r);
    return r;
  }

  // FormalParameterList?
  private static boolean FunctionSignature_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionSignature_1")) return false;
    FormalParameterList(b, l + 1);
    return true;
  }

  // ReturnParameter?
  private static boolean FunctionSignature_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionSignature_3")) return false;
    ReturnParameter(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // function LEFT_PARENTHESIS (ParameterList | ParameterTypeNameList)? RIGHT_PARENTHESIS ReturnParameter?
  public static boolean FunctionTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionTypeName")) return false;
    if (!nextTokenIs(b, FUNCTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_TYPE_NAME, null);
    r = consumeTokens(b, 0, FUNCTION, LEFT_PARENTHESIS);
    r = r && FunctionTypeName_2(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, consumeToken(b, RIGHT_PARENTHESIS));
    r = p && FunctionTypeName_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (ParameterList | ParameterTypeNameList)?
  private static boolean FunctionTypeName_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionTypeName_2")) return false;
    FunctionTypeName_2_0(b, l + 1);
    return true;
  }

  // ParameterList | ParameterTypeNameList
  private static boolean FunctionTypeName_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionTypeName_2_0")) return false;
    boolean r;
    r = ParameterList(b, l + 1);
    if (!r) r = ParameterTypeNameList(b, l + 1);
    return r;
  }

  // ReturnParameter?
  private static boolean FunctionTypeName_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionTypeName_4")) return false;
    ReturnParameter(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // future LT TypeName GT
  public static boolean FutureTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FutureTypeName")) return false;
    if (!nextTokenIs(b, FUTURE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUTURE_TYPE_NAME, null);
    r = consumeTokens(b, 1, FUTURE, LT);
    p = r; // pin = 1
    r = r && report_error_(b, TypeName(b, l + 1, -1));
    r = p && consumeToken(b, GT) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // varDefinition
  //                             | listenerDefinition
  //                             | typedVariableDefinition
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
  // handle
  public static boolean HandleTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "HandleTypeName")) return false;
    if (!nextTokenIs(b, HANDLE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HANDLE);
    exit_section_(b, m, HANDLE_TYPE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // if Expression (LEFT_BRACE Block RIGHT_BRACE)
  public static boolean IfClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfClause")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_CLAUSE, null);
    r = consumeToken(b, IF);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && IfClause_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean IfClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfClause_2")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // IfClause ElseIfClause* ElseClause?
  public static boolean IfElseStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfElseStatement")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = IfClause(b, l + 1);
    r = r && IfElseStatement_1(b, l + 1);
    r = r && IfElseStatement_2(b, l + 1);
    exit_section_(b, m, IF_ELSE_STATEMENT, r);
    return r;
  }

  // ElseIfClause*
  private static boolean IfElseStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfElseStatement_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ElseIfClause(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "IfElseStatement_1", c)) break;
    }
    return true;
  }

  // ElseClause?
  private static boolean IfElseStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfElseStatement_2")) return false;
    ElseClause(b, l + 1);
    return true;
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
  // LEFT_BRACKET Expression RIGHT_BRACKET
  public static boolean Index(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Index")) return false;
    if (!nextTokenIs(b, LEFT_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACKET);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, RIGHT_BRACKET);
    exit_section_(b, m, INDEX, r);
    return r;
  }

  /* ********************************************************** */
  // InferParamList ExprFunctionBody
  public static boolean InferAnonymousFunctionExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InferAnonymousFunctionExpr")) return false;
    if (!nextTokenIs(b, "<infer anonymous function expr>", IDENTIFIER, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INFER_ANONYMOUS_FUNCTION_EXPR, "<infer anonymous function expr>");
    r = InferParamList(b, l + 1);
    r = r && ExprFunctionBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean InferParam(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InferParam")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, INFER_PARAM, r);
    return r;
  }

  /* ********************************************************** */
  // (LEFT_PARENTHESIS (InferParam (COMMA InferParam)*)? RIGHT_PARENTHESIS) | InferParam
  public static boolean InferParamList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InferParamList")) return false;
    if (!nextTokenIs(b, "<infer param list>", IDENTIFIER, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INFER_PARAM_LIST, "<infer param list>");
    r = InferParamList_0(b, l + 1);
    if (!r) r = InferParam(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LEFT_PARENTHESIS (InferParam (COMMA InferParam)*)? RIGHT_PARENTHESIS
  private static boolean InferParamList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InferParamList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_PARENTHESIS);
    r = r && InferParamList_0_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  // (InferParam (COMMA InferParam)*)?
  private static boolean InferParamList_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InferParamList_0_1")) return false;
    InferParamList_0_1_0(b, l + 1);
    return true;
  }

  // InferParam (COMMA InferParam)*
  private static boolean InferParamList_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InferParamList_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = InferParam(b, l + 1);
    r = r && InferParamList_0_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA InferParam)*
  private static boolean InferParamList_0_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InferParamList_0_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!InferParamList_0_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "InferParamList_0_1_0_1", c)) break;
    }
    return true;
  }

  // COMMA InferParam
  private static boolean InferParamList_0_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InferParamList_0_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && InferParam(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
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
  // DOT AnyIdentifierName LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS
  public static boolean Invocation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Invocation")) return false;
    if (!nextTokenIs(b, DOT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && AnyIdentifierName(b, l + 1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    r = r && Invocation_3(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, INVOCATION, r);
    return r;
  }

  // InvocationArgList?
  private static boolean Invocation_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Invocation_3")) return false;
    InvocationArgList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // NamedArgs | RestArgs | Expression
  public static boolean InvocationArg(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InvocationArg")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INVOCATION_ARG, "<invocation arg>");
    r = NamedArgs(b, l + 1);
    if (!r) r = RestArgs(b, l + 1);
    if (!r) r = Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // InvocationArg (COMMA InvocationArg)*
  public static boolean InvocationArgList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InvocationArgList")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INVOCATION_ARG_LIST, "<invocation arg list>");
    r = InvocationArg(b, l + 1);
    p = r; // pin = 1
    r = r && InvocationArgList_1(b, l + 1);
    exit_section_(b, l, m, r, p, InvocationArgListRecover_parser_);
    return r || p;
  }

  // (COMMA InvocationArg)*
  private static boolean InvocationArgList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InvocationArgList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!InvocationArgList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "InvocationArgList_1", c)) break;
    }
    return true;
  }

  // COMMA InvocationArg
  private static boolean InvocationArgList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InvocationArgList_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && InvocationArg(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // !(')')
  static boolean InvocationArgListRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InvocationArgListRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RIGHT_PARENTHESIS);
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
  // let LetVarDecl (COMMA LetVarDecl)*
  public static boolean LetClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LetClause")) return false;
    if (!nextTokenIs(b, LET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LET_CLAUSE, null);
    r = consumeToken(b, LET);
    p = r; // pin = 1
    r = r && report_error_(b, LetVarDecl(b, l + 1));
    r = p && LetClause_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA LetVarDecl)*
  private static boolean LetClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LetClause_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!LetClause_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "LetClause_2", c)) break;
    }
    return true;
  }

  // COMMA LetVarDecl
  private static boolean LetClause_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LetClause_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && LetVarDecl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // AnnotationAttachment* (TypeName | var) BindingPattern ASSIGN Expression
  public static boolean LetVarDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LetVarDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LET_VAR_DECL, "<let var decl>");
    r = LetVarDecl_0(b, l + 1);
    r = r && LetVarDecl_1(b, l + 1);
    r = r && BindingPattern(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AnnotationAttachment*
  private static boolean LetVarDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LetVarDecl_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "LetVarDecl_0", c)) break;
    }
    return true;
  }

  // TypeName | var
  private static boolean LetVarDecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LetVarDecl_1")) return false;
    boolean r;
    r = TypeName(b, l + 1, -1);
    if (!r) r = consumeToken(b, VAR);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACKET BindingPattern (COMMA BindingPattern)+ RIGHT_BRACKET
  public static boolean ListBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListBindingPattern")) return false;
    if (!nextTokenIs(b, LEFT_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACKET);
    r = r && BindingPattern(b, l + 1);
    r = r && ListBindingPattern_2(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACKET);
    exit_section_(b, m, LIST_BINDING_PATTERN, r);
    return r;
  }

  // (COMMA BindingPattern)+
  private static boolean ListBindingPattern_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListBindingPattern_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ListBindingPattern_2_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!ListBindingPattern_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ListBindingPattern_2", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA BindingPattern
  private static boolean ListBindingPattern_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListBindingPattern_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && BindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACKET ExpressionList? RIGHT_BRACKET
  public static boolean ListConstructorExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListConstructorExpr")) return false;
    if (!nextTokenIs(b, LEFT_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACKET);
    r = r && ListConstructorExpr_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACKET);
    exit_section_(b, m, LIST_CONSTRUCTOR_EXPR, r);
    return r;
  }

  // ExpressionList?
  private static boolean ListConstructorExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListConstructorExpr_1")) return false;
    ExpressionList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ListRefBindingPattern ASSIGN Expression SEMICOLON
  public static boolean ListDestructuringStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListDestructuringStatement")) return false;
    if (!nextTokenIs(b, LEFT_BRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LIST_DESTRUCTURING_STATEMENT, null);
    r = ListRefBindingPattern(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    p = r; // pin = 2
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_BRACKET ((BindingRefPattern (COMMA BindingRefPattern)* (COMMA ListRefRestPattern)?) | ListRefRestPattern) RIGHT_BRACKET
  public static boolean ListRefBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListRefBindingPattern")) return false;
    if (!nextTokenIs(b, LEFT_BRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LIST_REF_BINDING_PATTERN, null);
    r = consumeToken(b, LEFT_BRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, ListRefBindingPattern_1(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (BindingRefPattern (COMMA BindingRefPattern)* (COMMA ListRefRestPattern)?) | ListRefRestPattern
  private static boolean ListRefBindingPattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListRefBindingPattern_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ListRefBindingPattern_1_0(b, l + 1);
    if (!r) r = ListRefRestPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // BindingRefPattern (COMMA BindingRefPattern)* (COMMA ListRefRestPattern)?
  private static boolean ListRefBindingPattern_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListRefBindingPattern_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = BindingRefPattern(b, l + 1);
    r = r && ListRefBindingPattern_1_0_1(b, l + 1);
    r = r && ListRefBindingPattern_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA BindingRefPattern)*
  private static boolean ListRefBindingPattern_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListRefBindingPattern_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ListRefBindingPattern_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ListRefBindingPattern_1_0_1", c)) break;
    }
    return true;
  }

  // COMMA BindingRefPattern
  private static boolean ListRefBindingPattern_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListRefBindingPattern_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && BindingRefPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ListRefRestPattern)?
  private static boolean ListRefBindingPattern_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListRefBindingPattern_1_0_2")) return false;
    ListRefBindingPattern_1_0_2_0(b, l + 1);
    return true;
  }

  // COMMA ListRefRestPattern
  private static boolean ListRefBindingPattern_1_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListRefBindingPattern_1_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ListRefRestPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ELLIPSIS VariableReference
  public static boolean ListRefRestPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListRefRestPattern")) return false;
    if (!nextTokenIs(b, ELLIPSIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LIST_REF_REST_PATTERN, null);
    r = consumeToken(b, ELLIPSIS);
    p = r; // pin = 1
    r = r && VariableReference(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // lock LEFT_BRACE Block RIGHT_BRACE
  public static boolean LockStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LockStatement")) return false;
    if (!nextTokenIs(b, LOCK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LOCK_STATEMENT, null);
    r = consumeTokens(b, 1, LOCK, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // VarMatchPatternClause | StaticMatchPatternClause | ErrorMatchPatternClause
  public static boolean MatchPatternClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MatchPatternClause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MATCH_PATTERN_CLAUSE, "<match pattern clause>");
    r = VarMatchPatternClause(b, l + 1);
    if (!r) r = StaticMatchPatternClause(b, l + 1);
    if (!r) r = ErrorMatchPatternClause(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // match Expression MatchStatementBody
  public static boolean MatchStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MatchStatement")) return false;
    if (!nextTokenIs(b, MATCH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MATCH_STATEMENT, null);
    r = consumeToken(b, MATCH);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && MatchStatementBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_BRACE MatchPatternClause+ RIGHT_BRACE
  public static boolean MatchStatementBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MatchStatementBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && MatchStatementBody_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, MATCH_STATEMENT_BODY, r);
    return r;
  }

  // MatchPatternClause+
  private static boolean MatchStatementBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MatchStatementBody_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = MatchPatternClause(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!MatchPatternClause(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "MatchStatementBody_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // documentationString? AnnotationAttachment* (public | private)? (remote|resource)? function AnyIdentifierName FunctionSignature SEMICOLON
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
    r = r && FunctionSignature(b, l + 1);
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

  // AnnotationAttachment*
  private static boolean MethodDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDeclaration_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "MethodDeclaration_1", c)) break;
    }
    return true;
  }

  // (public | private)?
  private static boolean MethodDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDeclaration_2")) return false;
    MethodDeclaration_2_0(b, l + 1);
    return true;
  }

  // public | private
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
  // documentationString? AnnotationAttachment* (public | private)? (remote | resource)? function AnyIdentifierName FunctionSignature FunctionDefinitionBody
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
    r = r && FunctionSignature(b, l + 1);
    r = r && FunctionDefinitionBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // documentationString?
  private static boolean MethodDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDefinition_0")) return false;
    documentationString(b, l + 1);
    return true;
  }

  // AnnotationAttachment*
  private static boolean MethodDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDefinition_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "MethodDefinition_1", c)) break;
    }
    return true;
  }

  // (public | private)?
  private static boolean MethodDefinition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDefinition_2")) return false;
    MethodDefinition_2_0(b, l + 1);
    return true;
  }

  // public | private
  private static boolean MethodDefinition_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDefinition_2_0")) return false;
    boolean r;
    r = consumeToken(b, PUBLIC);
    if (!r) r = consumeToken(b, PRIVATE);
    return r;
  }

  // (remote | resource)?
  private static boolean MethodDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDefinition_3")) return false;
    MethodDefinition_3_0(b, l + 1);
    return true;
  }

  // remote | resource
  private static boolean MethodDefinition_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MethodDefinition_3_0")) return false;
    boolean r;
    r = consumeToken(b, REMOTE);
    if (!r) r = consumeToken(b, RESOURCE);
    return r;
  }

  /* ********************************************************** */
  // <<isPackageExpected>> PackageReference identifier | identifier
  public static boolean NameReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NameReference")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NAME_REFERENCE, "<name reference>");
    r = NameReference_0(b, l + 1);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<isPackageExpected>> PackageReference identifier
  private static boolean NameReference_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NameReference_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = isPackageExpected(b, l + 1);
    r = r && PackageReference(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // identifier ASSIGN Expression
  public static boolean NamedArgs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamedArgs")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NAMED_ARGS, null);
    r = consumeTokens(b, 2, IDENTIFIER, ASSIGN);
    p = r; // pin = 2
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // NamespaceDeclaration
  public static boolean NamespaceDeclarationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamespaceDeclarationStatement")) return false;
    if (!nextTokenIs(b, XMLNS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = NamespaceDeclaration(b, l + 1);
    exit_section_(b, m, NAMESPACE_DECLARATION_STATEMENT, r);
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
  // (ObjectFieldDefinition | ObjectMethod | TypeReference)*
  public static boolean ObjectBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectBody")) return false;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_BODY, "<object body>");
    while (true) {
      int c = current_position_(b);
      if (!ObjectBody_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectBody", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  // ObjectFieldDefinition | ObjectMethod | TypeReference
  private static boolean ObjectBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectBody_0")) return false;
    boolean r;
    r = ObjectFieldDefinition(b, l + 1);
    if (!r) r = ObjectMethod(b, l + 1);
    if (!r) r = TypeReference(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // documentationString? AnnotationAttachment* (public | private)? TypeName identifier (ASSIGN Expression)? (COMMA | SEMICOLON)
  public static boolean ObjectFieldDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_FIELD_DEFINITION, "<object field definition>");
    r = ObjectFieldDefinition_0(b, l + 1);
    r = r && ObjectFieldDefinition_1(b, l + 1);
    r = r && ObjectFieldDefinition_2(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    p = r; // pin = 4
    r = r && report_error_(b, consumeToken(b, IDENTIFIER));
    r = p && report_error_(b, ObjectFieldDefinition_5(b, l + 1)) && r;
    r = p && ObjectFieldDefinition_6(b, l + 1) && r;
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

  // (ASSIGN Expression)?
  private static boolean ObjectFieldDefinition_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_5")) return false;
    ObjectFieldDefinition_5_0(b, l + 1);
    return true;
  }

  // ASSIGN Expression
  private static boolean ObjectFieldDefinition_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA | SEMICOLON
  private static boolean ObjectFieldDefinition_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_6")) return false;
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
  // onretry (LEFT_BRACE (Block RIGHT_BRACE))
  public static boolean OnRetryClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OnRetryClause")) return false;
    if (!nextTokenIs(b, ONRETRY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ON_RETRY_CLAUSE, null);
    r = consumeToken(b, ONRETRY);
    p = r; // pin = 1
    r = r && OnRetryClause_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE (Block RIGHT_BRACE)
  private static boolean OnRetryClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OnRetryClause_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && OnRetryClause_1_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // Block RIGHT_BRACE
  private static boolean OnRetryClause_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OnRetryClause_1_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = Block(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_BRACE EntryBindingPattern RIGHT_BRACE
  public static boolean OpenRecordBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OpenRecordBindingPattern")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OPEN_RECORD_BINDING_PATTERN, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, EntryBindingPattern(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // identifier {
  // }
  public static boolean OrgName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OrgName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && OrgName_1(b, l + 1);
    exit_section_(b, m, ORG_NAME, r);
    return r;
  }

  // {
  // }
  private static boolean OrgName_1(PsiBuilder b, int l) {
    return true;
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
  // panic Expression SEMICOLON
  public static boolean PanicStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PanicStatement")) return false;
    if (!nextTokenIs(b, PANIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PANIC_STATEMENT, null);
    r = consumeToken(b, PANIC);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // AnnotationAttachment* public? TypeName identifier
  public static boolean Parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Parameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER, "<parameter>");
    r = Parameter_0(b, l + 1);
    r = r && Parameter_1(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AnnotationAttachment*
  private static boolean Parameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Parameter_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "Parameter_0", c)) break;
    }
    return true;
  }

  // public?
  private static boolean Parameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Parameter_1")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  /* ********************************************************** */
  // (Parameter (COMMA Parameter)* (COMMA RestParameter)?) | RestParameter
  public static boolean ParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_LIST, "<parameter list>");
    r = ParameterList_0(b, l + 1);
    if (!r) r = RestParameter(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // Parameter (COMMA Parameter)* (COMMA RestParameter)?
  private static boolean ParameterList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Parameter(b, l + 1);
    r = r && ParameterList_0_1(b, l + 1);
    r = r && ParameterList_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA Parameter)*
  private static boolean ParameterList_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterList_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ParameterList_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ParameterList_0_1", c)) break;
    }
    return true;
  }

  // COMMA Parameter
  private static boolean ParameterList_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterList_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && Parameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA RestParameter)?
  private static boolean ParameterList_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterList_0_2")) return false;
    ParameterList_0_2_0(b, l + 1);
    return true;
  }

  // COMMA RestParameter
  private static boolean ParameterList_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterList_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && RestParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // AnnotationAttachment* TypeName ELLIPSIS?
  public static boolean ParameterTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_TYPE_NAME, "<parameter type name>");
    r = ParameterTypeName_0(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    r = r && ParameterTypeName_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AnnotationAttachment*
  private static boolean ParameterTypeName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterTypeName_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ParameterTypeName_0", c)) break;
    }
    return true;
  }

  // ELLIPSIS?
  private static boolean ParameterTypeName_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterTypeName_2")) return false;
    consumeToken(b, ELLIPSIS);
    return true;
  }

  /* ********************************************************** */
  // ParameterTypeName (COMMA ParameterTypeName)*
  public static boolean ParameterTypeNameList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterTypeNameList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_TYPE_NAME_LIST, "<parameter type name list>");
    r = ParameterTypeName(b, l + 1);
    r = r && ParameterTypeNameList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA ParameterTypeName)*
  private static boolean ParameterTypeNameList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterTypeNameList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ParameterTypeNameList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ParameterTypeNameList_1", c)) break;
    }
    return true;
  }

  // COMMA ParameterTypeName
  private static boolean ParameterTypeNameList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterTypeNameList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ParameterTypeName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // WorkerName | default
  public static boolean PeerWorker(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PeerWorker")) return false;
    if (!nextTokenIs(b, "<peer worker>", DEFAULT, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PEER_WORKER, "<peer worker>");
    r = WorkerName(b, l + 1);
    if (!r) r = consumeToken(b, DEFAULT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // QueryPipeline SelectClause
  public static boolean QueryExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "QueryExpr")) return false;
    if (!nextTokenIs(b, FROM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = QueryPipeline(b, l + 1);
    r = r && SelectClause(b, l + 1);
    exit_section_(b, m, QUERY_EXPR, r);
    return r;
  }

  /* ********************************************************** */
  // FromClause (FromClause | LetClause | WhereClause)*
  public static boolean QueryPipeline(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "QueryPipeline")) return false;
    if (!nextTokenIs(b, FROM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = FromClause(b, l + 1);
    r = r && QueryPipeline_1(b, l + 1);
    exit_section_(b, m, QUERY_PIPELINE, r);
    return r;
  }

  // (FromClause | LetClause | WhereClause)*
  private static boolean QueryPipeline_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "QueryPipeline_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!QueryPipeline_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "QueryPipeline_1", c)) break;
    }
    return true;
  }

  // FromClause | LetClause | WhereClause
  private static boolean QueryPipeline_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "QueryPipeline_1_0")) return false;
    boolean r;
    r = FromClause(b, l + 1);
    if (!r) r = LetClause(b, l + 1);
    if (!r) r = WhereClause(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // ClosedRecordBindingPattern |  OpenRecordBindingPattern
  public static boolean RecordBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordBindingPattern")) return false;
    if (!nextTokenIs(b, "<record binding pattern>", LEFT_BRACE, LEFT_CLOSED_RECORD_DELIMITER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECORD_BINDING_PATTERN, "<record binding pattern>");
    r = ClosedRecordBindingPattern(b, l + 1);
    if (!r) r = OpenRecordBindingPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // RecordRefBindingPattern ASSIGN Expression SEMICOLON
  public static boolean RecordDestructuringStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordDestructuringStatement")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_DESTRUCTURING_STATEMENT, null);
    r = RecordRefBindingPattern(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    p = r; // pin = 3
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // RecordKeyValueField
  //               | RecordRestField
  //               | identifier
  public static boolean RecordField(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordField")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECORD_FIELD, "<record field>");
    r = RecordKeyValueField(b, l + 1);
    if (!r) r = RecordRestField(b, l + 1);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACKET Expression RIGHT_BRACKET | Expression | identifier
  public static boolean RecordKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordKey")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECORD_KEY, "<record key>");
    r = RecordKey_0(b, l + 1);
    if (!r) r = Expression(b, l + 1, -1);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LEFT_BRACKET Expression RIGHT_BRACKET
  private static boolean RecordKey_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordKey_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACKET);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, RIGHT_BRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // RecordKey COLON Expression
  public static boolean RecordKeyValueField(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordKeyValueField")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_KEY_VALUE_FIELD, "<record key value field>");
    r = RecordKey(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, COLON));
    r = p && Expression(b, l + 1, -1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_BRACE RecordLiteralBody? RIGHT_BRACE
  public static boolean RecordLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteral")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_LITERAL, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, RecordLiteral_1(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // RecordLiteralBody?
  private static boolean RecordLiteral_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteral_1")) return false;
    RecordLiteralBody(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // RecordField (COMMA RecordField)*
  public static boolean RecordLiteralBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteralBody")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_LITERAL_BODY, "<record literal body>");
    r = RecordField(b, l + 1);
    p = r; // pin = 1
    r = r && RecordLiteralBody_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA RecordField)*
  private static boolean RecordLiteralBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteralBody_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!RecordLiteralBody_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "RecordLiteralBody_1", c)) break;
    }
    return true;
  }

  // COMMA RecordField
  private static boolean RecordLiteralBody_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteralBody_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && RecordField(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_BRACE EntryRefBindingPattern RIGHT_BRACE
  public static boolean RecordRefBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordRefBindingPattern")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && EntryRefBindingPattern(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, RECORD_REF_BINDING_PATTERN, r);
    return r;
  }

  /* ********************************************************** */
  // ELLIPSIS Expression
  public static boolean RecordRestField(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordRestField")) return false;
    if (!nextTokenIs(b, ELLIPSIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_REST_FIELD, null);
    r = consumeToken(b, ELLIPSIS);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // TypeName <<restDescriptorPredicate>> ELLIPSIS SEMICOLON
  public static boolean RecordRestFieldDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordRestFieldDefinition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECORD_REST_FIELD_DEFINITION, "<record rest field definition>");
    r = TypeName(b, l + 1, -1);
    r = r && restDescriptorPredicate(b, l + 1);
    r = r && consumeTokens(b, 0, ELLIPSIS, SEMICOLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // BuiltInReferenceTypeName
  //                       | UserDefineTypeName
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
  // foreach | map | start | continue | OBJECT_INIT | error
  public static boolean ReservedWord(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReservedWord")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RESERVED_WORD, "<reserved word>");
    r = consumeToken(b, FOREACH);
    if (!r) r = consumeToken(b, MAP);
    if (!r) r = consumeToken(b, START);
    if (!r) r = consumeToken(b, CONTINUE);
    if (!r) r = consumeToken(b, OBJECT_INIT);
    if (!r) r = consumeToken(b, ERROR);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ELLIPSIS Expression
  public static boolean RestArgs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RestArgs")) return false;
    if (!nextTokenIs(b, ELLIPSIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, REST_ARGS, null);
    r = consumeToken(b, ELLIPSIS);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ELLIPSIS identifier
  public static boolean RestBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RestBindingPattern")) return false;
    if (!nextTokenIs(b, ELLIPSIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ELLIPSIS, IDENTIFIER);
    exit_section_(b, m, REST_BINDING_PATTERN, r);
    return r;
  }

  /* ********************************************************** */
  // ELLIPSIS var identifier
  public static boolean RestMatchPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RestMatchPattern")) return false;
    if (!nextTokenIs(b, ELLIPSIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ELLIPSIS, VAR, IDENTIFIER);
    exit_section_(b, m, REST_MATCH_PATTERN, r);
    return r;
  }

  /* ********************************************************** */
  // AnnotationAttachment* TypeName ELLIPSIS identifier
  public static boolean RestParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RestParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REST_PARAMETER, "<rest parameter>");
    r = RestParameter_0(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeTokens(b, 2, ELLIPSIS, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AnnotationAttachment*
  private static boolean RestParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RestParameter_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "RestParameter_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ELLIPSIS VariableReference | SealedLiteral
  public static boolean RestRefBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RestRefBindingPattern")) return false;
    if (!nextTokenIs(b, "<rest ref binding pattern>", ELLIPSIS, NOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REST_REF_BINDING_PATTERN, "<rest ref binding pattern>");
    r = RestRefBindingPattern_0(b, l + 1);
    if (!r) r = SealedLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ELLIPSIS VariableReference
  private static boolean RestRefBindingPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RestRefBindingPattern_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELLIPSIS);
    r = r && VariableReference(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // retries ASSIGN Expression
  public static boolean RetriesStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RetriesStatement")) return false;
    if (!nextTokenIs(b, RETRIES)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RETRIES_STATEMENT, null);
    r = consumeTokens(b, 1, RETRIES, ASSIGN);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // retry SEMICOLON
  public static boolean RetryStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RetryStatement")) return false;
    if (!nextTokenIs(b, RETRY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RETRY_STATEMENT, null);
    r = consumeTokens(b, 1, RETRY, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // returns AnnotationAttachment* TypeName
  public static boolean ReturnParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReturnParameter")) return false;
    if (!nextTokenIs(b, RETURNS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RETURN_PARAMETER, null);
    r = consumeToken(b, RETURNS);
    p = r; // pin = 1
    r = r && report_error_(b, ReturnParameter_1(b, l + 1));
    r = p && TypeName(b, l + 1, -1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AnnotationAttachment*
  private static boolean ReturnParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReturnParameter_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ReturnParameter_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // return Expression? SEMICOLON
  public static boolean ReturnStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReturnStatement")) return false;
    if (!nextTokenIs(b, RETURN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RETURN_STATEMENT, null);
    r = consumeToken(b, RETURN);
    p = r; // pin = 1
    r = r && report_error_(b, ReturnStatement_1(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // Expression?
  private static boolean ReturnStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ReturnStatement_1")) return false;
    Expression(b, l + 1, -1);
    return true;
  }

  /* ********************************************************** */
  // NOT <<restDescriptorPredicate>> ELLIPSIS
  public static boolean SealedLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SealedLiteral")) return false;
    if (!nextTokenIs(b, NOT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NOT);
    r = r && restDescriptorPredicate(b, l + 1);
    r = r && consumeToken(b, ELLIPSIS);
    exit_section_(b, m, SEALED_LITERAL, r);
    return r;
  }

  /* ********************************************************** */
  // select Expression
  public static boolean SelectClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectClause")) return false;
    if (!nextTokenIs(b, SELECT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SELECT_CLAUSE, null);
    r = consumeToken(b, SELECT);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_BRACE ObjectMethod* RIGHT_BRACE
  public static boolean ServiceBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && ServiceBody_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, SERVICE_BODY, r);
    return r;
  }

  // ObjectMethod*
  private static boolean ServiceBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceBody_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ObjectMethod(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ServiceBody_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // service identifier? on ExpressionList ServiceBody
  public static boolean ServiceDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceDefinition")) return false;
    if (!nextTokenIs(b, SERVICE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SERVICE_DEFINITION, null);
    r = consumeToken(b, SERVICE);
    r = r && ServiceDefinition_1(b, l + 1);
    r = r && consumeToken(b, ON);
    p = r; // pin = 3
    r = r && report_error_(b, ExpressionList(b, l + 1));
    r = p && ServiceBody(b, l + 1) && r;
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
  // GT <<shiftExprPredicate>> GT
  //     | LT <<shiftExprPredicate>> LT
  //     | GT <<shiftExprPredicate>> GT <<shiftExprPredicate>> GT
  public static boolean ShiftExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ShiftExpression")) return false;
    if (!nextTokenIs(b, "<shift expression>", GT, LT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SHIFT_EXPRESSION, "<shift expression>");
    r = ShiftExpression_0(b, l + 1);
    if (!r) r = ShiftExpression_1(b, l + 1);
    if (!r) r = ShiftExpression_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // GT <<shiftExprPredicate>> GT
  private static boolean ShiftExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ShiftExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, GT);
    r = r && shiftExprPredicate(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  // LT <<shiftExprPredicate>> LT
  private static boolean ShiftExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ShiftExpression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && shiftExprPredicate(b, l + 1);
    r = r && consumeToken(b, LT);
    exit_section_(b, m, null, r);
    return r;
  }

  // GT <<shiftExprPredicate>> GT <<shiftExprPredicate>> GT
  private static boolean ShiftExpression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ShiftExpression_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, GT);
    r = r && shiftExprPredicate(b, l + 1);
    r = r && consumeToken(b, GT);
    r = r && shiftExprPredicate(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SUB? IntegerLiteral
  //                   | SUB? FloatingPointLiteral
  //                   | QUOTED_STRING_LITERAL
  //                   | BOOLEAN_LITERAL
  //                   | NilLiteral
  //                   | BlobLiteral
  //                   | NULL_LITERAL
  public static boolean SimpleLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteral")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMPLE_LITERAL, "<simple literal>");
    r = SimpleLiteral_0(b, l + 1);
    if (!r) r = SimpleLiteral_1(b, l + 1);
    if (!r) r = consumeToken(b, QUOTED_STRING_LITERAL);
    if (!r) r = consumeToken(b, BOOLEAN_LITERAL);
    if (!r) r = NilLiteral(b, l + 1);
    if (!r) r = BlobLiteral(b, l + 1);
    if (!r) r = consumeToken(b, NULL_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SUB? IntegerLiteral
  private static boolean SimpleLiteral_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteral_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = SimpleLiteral_0_0(b, l + 1);
    r = r && IntegerLiteral(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SUB?
  private static boolean SimpleLiteral_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteral_0_0")) return false;
    consumeToken(b, SUB);
    return true;
  }

  // SUB? FloatingPointLiteral
  private static boolean SimpleLiteral_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteral_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = SimpleLiteral_1_0(b, l + 1);
    r = r && FloatingPointLiteral(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SUB?
  private static boolean SimpleLiteral_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteral_1_0")) return false;
    consumeToken(b, SUB);
    return true;
  }

  /* ********************************************************** */
  // var? (identifier | QUOTED_STRING_LITERAL)
  public static boolean SimpleMatchPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleMatchPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMPLE_MATCH_PATTERN, "<simple match pattern>");
    r = SimpleMatchPattern_0(b, l + 1);
    r = r && SimpleMatchPattern_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // var?
  private static boolean SimpleMatchPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleMatchPattern_0")) return false;
    consumeToken(b, VAR);
    return true;
  }

  // identifier | QUOTED_STRING_LITERAL
  private static boolean SimpleMatchPattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleMatchPattern_1")) return false;
    boolean r;
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, QUOTED_STRING_LITERAL);
    return r;
  }

  /* ********************************************************** */
  // source SourceOnlyAttachPointIdent
  public static boolean SourceOnlyAttachPoint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SourceOnlyAttachPoint")) return false;
    if (!nextTokenIs(b, SOURCE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SOURCE_ONLY_ATTACH_POINT, null);
    r = consumeToken(b, SOURCE);
    p = r; // pin = 1
    r = r && SourceOnlyAttachPointIdent(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // annotation | external | var | const | listener | worker
  public static boolean SourceOnlyAttachPointIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SourceOnlyAttachPointIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SOURCE_ONLY_ATTACH_POINT_IDENT, "<source only attach point ident>");
    r = consumeToken(b, ANNOTATION);
    if (!r) r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, LISTENER);
    if (!r) r = consumeToken(b, WORKER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // WhileStatement
  //     |   ContinueStatement
  //     |   ForeachStatement
  //     |   MatchStatement
  //     |   BreakStatement
  //     |   ThrowStatement
  //     |   PanicStatement
  //     |   ReturnStatement
  //     |   AbortStatement
  //     |   RetryStatement
  //     |   LockStatement
  //     |   NamespaceDeclarationStatement
  //     |   TransactionStatement
  //     |   IfElseStatement
  //     |   TryCatchStatement
  //     |   ForkJoinStatement
  //     |   ListDestructuringStatement
  //     |   RecordDestructuringStatement
  //     |   WorkerSendAsyncStatement
  //     |   AssignmentStatement
  //     |   VariableDefinitionStatement
  //     |   CompoundAssignmentStatement
  //     |   ExpressionStmt
  //     |   ErrorDestructuringStatement
  public static boolean Statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT, "<statement>");
    r = WhileStatement(b, l + 1);
    if (!r) r = ContinueStatement(b, l + 1);
    if (!r) r = ForeachStatement(b, l + 1);
    if (!r) r = MatchStatement(b, l + 1);
    if (!r) r = BreakStatement(b, l + 1);
    if (!r) r = ThrowStatement(b, l + 1);
    if (!r) r = PanicStatement(b, l + 1);
    if (!r) r = ReturnStatement(b, l + 1);
    if (!r) r = AbortStatement(b, l + 1);
    if (!r) r = RetryStatement(b, l + 1);
    if (!r) r = LockStatement(b, l + 1);
    if (!r) r = NamespaceDeclarationStatement(b, l + 1);
    if (!r) r = TransactionStatement(b, l + 1);
    if (!r) r = IfElseStatement(b, l + 1);
    if (!r) r = TryCatchStatement(b, l + 1);
    if (!r) r = ForkJoinStatement(b, l + 1);
    if (!r) r = ListDestructuringStatement(b, l + 1);
    if (!r) r = RecordDestructuringStatement(b, l + 1);
    if (!r) r = WorkerSendAsyncStatement(b, l + 1);
    if (!r) r = AssignmentStatement(b, l + 1);
    if (!r) r = VariableDefinitionStatement(b, l + 1);
    if (!r) r = CompoundAssignmentStatement(b, l + 1);
    if (!r) r = ExpressionStmt(b, l + 1);
    if (!r) r = ErrorDestructuringStatement(b, l + 1);
    exit_section_(b, l, m, r, false, StatementRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // !(BOOLEAN_LITERAL|QUOTED_STRING_LITERAL|DECIMAL_INTEGER_LITERAL|HEX_INTEGER_LITERAL|OCTAL_INTEGER_LITERAL|BINARY_INTEGER_LITERAL|NULL_LITERAL|DECIMAL_FLOATING_POINT_NUMBER|HEXADECIMAL_FLOATING_POINT_LITERAL|int|string|float|decimal|boolean|byte|any|anydata|json|xml|xmlns|map|table|function|stream|'{'|'['|'}'|';'|typedesc|future|var|while|match|foreach|continue|break|fork|try|throw|return|abort|aborted|committed|retry|fail|lock|transaction|if|forever|object|service|check|checkpanic|error|panic|from|worker|record|identifier|wait)
  static boolean StatementRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StatementRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !StatementRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // BOOLEAN_LITERAL|QUOTED_STRING_LITERAL|DECIMAL_INTEGER_LITERAL|HEX_INTEGER_LITERAL|OCTAL_INTEGER_LITERAL|BINARY_INTEGER_LITERAL|NULL_LITERAL|DECIMAL_FLOATING_POINT_NUMBER|HEXADECIMAL_FLOATING_POINT_LITERAL|int|string|float|decimal|boolean|byte|any|anydata|json|xml|xmlns|map|table|function|stream|'{'|'['|'}'|';'|typedesc|future|var|while|match|foreach|continue|break|fork|try|throw|return|abort|aborted|committed|retry|fail|lock|transaction|if|forever|object|service|check|checkpanic|error|panic|from|worker|record|identifier|wait
  private static boolean StatementRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StatementRecover_0")) return false;
    boolean r;
    r = consumeToken(b, BOOLEAN_LITERAL);
    if (!r) r = consumeToken(b, QUOTED_STRING_LITERAL);
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
    return r;
  }

  /* ********************************************************** */
  // StaticMatchLiteral EQUAL_GT LEFT_BRACE Block RIGHT_BRACE
  public static boolean StaticMatchPatternClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StaticMatchPatternClause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATIC_MATCH_PATTERN_CLAUSE, "<static match pattern clause>");
    r = StaticMatchLiteral(b, l + 1, -1);
    r = r && consumeTokens(b, 0, EQUAL_GT, LEFT_BRACE);
    r = r && Block(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, l, m, r, false, null);
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
  // StringTemplateExpressionContent | StringTemplateTextContent
  public static boolean StringTemplateContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateContent")) return false;
    if (!nextTokenIs(b, "<string template content>", STRING_TEMPLATE_EXPRESSION_START, STRING_TEMPLATE_TEXT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING_TEMPLATE_CONTENT, "<string template content>");
    r = StringTemplateExpressionContent(b, l + 1);
    if (!r) r = StringTemplateTextContent(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // STRING_TEMPLATE_EXPRESSION_START Expression STRING_TEMPLATE_EXPRESSION_END
  static boolean StringTemplateExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateExpression")) return false;
    if (!nextTokenIs(b, STRING_TEMPLATE_EXPRESSION_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, STRING_TEMPLATE_EXPRESSION_START);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, STRING_TEMPLATE_EXPRESSION_END);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // StringTemplateExpression+ STRING_TEMPLATE_TEXT?
  static boolean StringTemplateExpressionContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateExpressionContent")) return false;
    if (!nextTokenIs(b, STRING_TEMPLATE_EXPRESSION_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = StringTemplateExpressionContent_0(b, l + 1);
    r = r && StringTemplateExpressionContent_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // StringTemplateExpression+
  private static boolean StringTemplateExpressionContent_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateExpressionContent_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = StringTemplateExpression(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!StringTemplateExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "StringTemplateExpressionContent_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // STRING_TEMPLATE_TEXT?
  private static boolean StringTemplateExpressionContent_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateExpressionContent_1")) return false;
    consumeToken(b, STRING_TEMPLATE_TEXT);
    return true;
  }

  /* ********************************************************** */
  // STRING_TEMPLATE_LITERAL_START StringTemplateContent? STRING_TEMPLATE_LITERAL_END
  public static boolean StringTemplateLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateLiteral")) return false;
    if (!nextTokenIs(b, STRING_TEMPLATE_LITERAL_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, STRING_TEMPLATE_LITERAL, null);
    r = consumeToken(b, STRING_TEMPLATE_LITERAL_START);
    p = r; // pin = 1
    r = r && report_error_(b, StringTemplateLiteral_1(b, l + 1));
    r = p && consumeToken(b, STRING_TEMPLATE_LITERAL_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // StringTemplateContent?
  private static boolean StringTemplateLiteral_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateLiteral_1")) return false;
    StringTemplateContent(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // STRING_TEMPLATE_TEXT
  static boolean StringTemplateTextContent(PsiBuilder b, int l) {
    return consumeToken(b, STRING_TEMPLATE_TEXT);
  }

  /* ********************************************************** */
  // ListBindingPattern | RecordBindingPattern | ErrorBindingPattern
  public static boolean StructuredBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StructuredBindingPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRUCTURED_BINDING_PATTERN, "<structured binding pattern>");
    r = ListBindingPattern(b, l + 1);
    if (!r) r = RecordBindingPattern(b, l + 1);
    if (!r) r = ErrorBindingPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ListRefBindingPattern | RecordRefBindingPattern
  public static boolean StructuredRefBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StructuredRefBindingPattern")) return false;
    if (!nextTokenIs(b, "<structured ref binding pattern>", LEFT_BRACE, LEFT_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRUCTURED_REF_BINDING_PATTERN, "<structured ref binding pattern>");
    r = ListRefBindingPattern(b, l + 1);
    if (!r) r = RecordRefBindingPattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier identifier | identifier
  public static boolean TableColumn(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableColumn")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokens(b, 0, IDENTIFIER, IDENTIFIER);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, TABLE_COLUMN, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE (TableColumn (COMMA TableColumn)*)? RIGHT_BRACE
  public static boolean TableColumnDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableColumnDefinition")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && TableColumnDefinition_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, TABLE_COLUMN_DEFINITION, r);
    return r;
  }

  // (TableColumn (COMMA TableColumn)*)?
  private static boolean TableColumnDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableColumnDefinition_1")) return false;
    TableColumnDefinition_1_0(b, l + 1);
    return true;
  }

  // TableColumn (COMMA TableColumn)*
  private static boolean TableColumnDefinition_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableColumnDefinition_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TableColumn(b, l + 1);
    r = r && TableColumnDefinition_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA TableColumn)*
  private static boolean TableColumnDefinition_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableColumnDefinition_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!TableColumnDefinition_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TableColumnDefinition_1_0_1", c)) break;
    }
    return true;
  }

  // COMMA TableColumn
  private static boolean TableColumnDefinition_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableColumnDefinition_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && TableColumn(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE ExpressionList RIGHT_BRACE
  public static boolean TableData(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableData")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && ExpressionList(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, TABLE_DATA, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACKET TableDataList? RIGHT_BRACKET
  public static boolean TableDataArray(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableDataArray")) return false;
    if (!nextTokenIs(b, LEFT_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACKET);
    r = r && TableDataArray_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACKET);
    exit_section_(b, m, TABLE_DATA_ARRAY, r);
    return r;
  }

  // TableDataList?
  private static boolean TableDataArray_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableDataArray_1")) return false;
    TableDataList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // TableData (COMMA TableData)* | ExpressionList
  public static boolean TableDataList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableDataList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TABLE_DATA_LIST, "<table data list>");
    r = TableDataList_0(b, l + 1);
    if (!r) r = ExpressionList(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // TableData (COMMA TableData)*
  private static boolean TableDataList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableDataList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TableData(b, l + 1);
    r = r && TableDataList_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA TableData)*
  private static boolean TableDataList_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableDataList_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!TableDataList_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TableDataList_0_1", c)) break;
    }
    return true;
  }

  // COMMA TableData
  private static boolean TableDataList_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableDataList_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && TableData(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // table LEFT_BRACE TableColumnDefinition? (COMMA TableDataArray)? RIGHT_BRACE
  public static boolean TableLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableLiteral")) return false;
    if (!nextTokenIs(b, TABLE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TABLE_LITERAL, null);
    r = consumeTokens(b, 1, TABLE, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, TableLiteral_2(b, l + 1));
    r = p && report_error_(b, TableLiteral_3(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // TableColumnDefinition?
  private static boolean TableLiteral_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableLiteral_2")) return false;
    TableColumnDefinition(b, l + 1);
    return true;
  }

  // (COMMA TableDataArray)?
  private static boolean TableLiteral_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableLiteral_3")) return false;
    TableLiteral_3_0(b, l + 1);
    return true;
  }

  // COMMA TableDataArray
  private static boolean TableLiteral_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableLiteral_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && TableDataArray(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // table LT TypeName GT
  public static boolean TableTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableTypeName")) return false;
    if (!nextTokenIs(b, TABLE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TABLE_TYPE_NAME, null);
    r = consumeTokens(b, 1, TABLE, LT);
    p = r; // pin = 1
    r = r && report_error_(b, TypeName(b, l + 1, -1));
    r = p && consumeToken(b, GT) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // throw Expression SEMICOLON
  public static boolean ThrowStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ThrowStatement")) return false;
    if (!nextTokenIs(b, THROW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, THROW_STATEMENT, null);
    r = consumeToken(b, THROW);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // !(MARKDOWN_DOCUMENTATION_LINE_START|PARAMETER_DOCUMENTATION_START|RETURN_PARAMETER_DOCUMENTATION_START|'@'|external|remote|client|abstract|public|type|typedesc|service|listener|function|enum|annotation|int|float|decimal|boolean|string|byte|map|xml|xmlns|json|table|any|stream|object|record|channel|const|final|var|future|identifier|'{')
  static boolean TopLevelDefinitionRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TopLevelDefinitionRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !TopLevelDefinitionRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // MARKDOWN_DOCUMENTATION_LINE_START|PARAMETER_DOCUMENTATION_START|RETURN_PARAMETER_DOCUMENTATION_START|'@'|external|remote|client|abstract|public|type|typedesc|service|listener|function|enum|annotation|int|float|decimal|boolean|string|byte|map|xml|xmlns|json|table|any|stream|object|record|channel|const|final|var|future|identifier|'{'
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
    return r;
  }

  /* ********************************************************** */
  // transaction (with TransactionPropertyInitStatementList)? (LEFT_BRACE (Block RIGHT_BRACE))
  public static boolean TransactionClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionClause")) return false;
    if (!nextTokenIs(b, TRANSACTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRANSACTION_CLAUSE, null);
    r = consumeToken(b, TRANSACTION);
    p = r; // pin = 1
    r = r && report_error_(b, TransactionClause_1(b, l + 1));
    r = p && TransactionClause_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (with TransactionPropertyInitStatementList)?
  private static boolean TransactionClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionClause_1")) return false;
    TransactionClause_1_0(b, l + 1);
    return true;
  }

  // with TransactionPropertyInitStatementList
  private static boolean TransactionClause_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionClause_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, WITH);
    p = r; // pin = 1
    r = r && TransactionPropertyInitStatementList(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE (Block RIGHT_BRACE)
  private static boolean TransactionClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionClause_2")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && TransactionClause_2_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // Block RIGHT_BRACE
  private static boolean TransactionClause_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionClause_2_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = Block(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // RetriesStatement
  public static boolean TransactionPropertyInitStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionPropertyInitStatement")) return false;
    if (!nextTokenIs(b, RETRIES)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = RetriesStatement(b, l + 1);
    exit_section_(b, m, TRANSACTION_PROPERTY_INIT_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // TransactionPropertyInitStatement (COMMA TransactionPropertyInitStatement)*
  public static boolean TransactionPropertyInitStatementList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionPropertyInitStatementList")) return false;
    if (!nextTokenIs(b, RETRIES)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRANSACTION_PROPERTY_INIT_STATEMENT_LIST, null);
    r = TransactionPropertyInitStatement(b, l + 1);
    p = r; // pin = 1
    r = r && TransactionPropertyInitStatementList_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA TransactionPropertyInitStatement)*
  private static boolean TransactionPropertyInitStatementList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionPropertyInitStatementList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!TransactionPropertyInitStatementList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TransactionPropertyInitStatementList_1", c)) break;
    }
    return true;
  }

  // COMMA TransactionPropertyInitStatement
  private static boolean TransactionPropertyInitStatementList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionPropertyInitStatementList_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && TransactionPropertyInitStatement(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // TransactionClause OnRetryClause? CommittedAbortedClauses?
  public static boolean TransactionStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionStatement")) return false;
    if (!nextTokenIs(b, TRANSACTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRANSACTION_STATEMENT, null);
    r = TransactionClause(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, TransactionStatement_1(b, l + 1));
    r = p && TransactionStatement_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // OnRetryClause?
  private static boolean TransactionStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionStatement_1")) return false;
    OnRetryClause(b, l + 1);
    return true;
  }

  // CommittedAbortedClauses?
  private static boolean TransactionStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TransactionStatement_2")) return false;
    CommittedAbortedClauses(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // try (LEFT_BRACE Block RIGHT_BRACE CatchClauses)
  public static boolean TryCatchStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TryCatchStatement")) return false;
    if (!nextTokenIs(b, TRY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRY_CATCH_STATEMENT, null);
    r = consumeToken(b, TRY);
    p = r; // pin = 1
    r = r && TryCatchStatement_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE Block RIGHT_BRACE CatchClauses
  private static boolean TryCatchStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TryCatchStatement_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RIGHT_BRACE)) && r;
    r = p && CatchClauses(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // public? type identifier FiniteType SEMICOLON
  public static boolean TypeDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDefinition")) return false;
    if (!nextTokenIs(b, "<type definition>", PUBLIC, TYPE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TYPE_DEFINITION, "<type definition>");
    r = TypeDefinition_0(b, l + 1);
    r = r && consumeTokens(b, 1, TYPE, IDENTIFIER);
    p = r; // pin = 2
    r = r && report_error_(b, FiniteType(b, l + 1));
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
  // typedesc LT TypeName GT
  public static boolean TypeDescReferenceTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDescReferenceTypeName")) return false;
    if (!nextTokenIs(b, TYPEDESC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TYPE_DESC_REFERENCE_TYPE_NAME, null);
    r = consumeTokens(b, 2, TYPEDESC, LT);
    p = r; // pin = 2
    r = r && report_error_(b, TypeName(b, l + 1, -1));
    r = p && consumeToken(b, GT) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // typedesc
  public static boolean TypeDescTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDescTypeName")) return false;
    if (!nextTokenIs(b, TYPEDESC)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TYPEDESC);
    exit_section_(b, m, TYPE_DESC_TYPE_NAME, r);
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
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, USER_DEFINE_TYPE_NAME, "<user define type name>");
    r = NameReference(b, l + 1);
    exit_section_(b, l, m, r, false, null);
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
  // var BindingPattern (if Expression)? EQUAL_GT LEFT_BRACE Block RIGHT_BRACE
  public static boolean VarMatchPatternClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarMatchPatternClause")) return false;
    if (!nextTokenIs(b, VAR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VAR_MATCH_PATTERN_CLAUSE, null);
    r = consumeToken(b, VAR);
    p = r; // pin = 1
    r = r && report_error_(b, BindingPattern(b, l + 1));
    r = p && report_error_(b, VarMatchPatternClause_2(b, l + 1)) && r;
    r = p && report_error_(b, consumeTokens(b, -1, EQUAL_GT, LEFT_BRACE)) && r;
    r = p && report_error_(b, Block(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (if Expression)?
  private static boolean VarMatchPatternClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarMatchPatternClause_2")) return false;
    VarMatchPatternClause_2_0(b, l + 1);
    return true;
  }

  // if Expression
  private static boolean VarMatchPatternClause_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarMatchPatternClause_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IF);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // variableDefinitionStatementWithAssignment | variableDefinitionStatementWithoutAssignment
  public static boolean VariableDefinitionStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableDefinitionStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_DEFINITION_STATEMENT, "<variable definition statement>");
    r = variableDefinitionStatementWithAssignment(b, l + 1);
    if (!r) r = variableDefinitionStatementWithoutAssignment(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // DECIMAL_INTEGER_LITERAL
  //     |   DECIMAL_FLOATING_POINT_NUMBER
  //     |   DECIMAL_EXTENDED_FLOATING_POINT_NUMBER
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
  // LEFT_BRACE WaitKeyValue (COMMA WaitKeyValue)* RIGHT_BRACE
  public static boolean WaitForCollection(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WaitForCollection")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WAIT_FOR_COLLECTION, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, WaitKeyValue(b, l + 1));
    r = p && report_error_(b, WaitForCollection_2(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA WaitKeyValue)*
  private static boolean WaitForCollection_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WaitForCollection_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!WaitForCollection_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "WaitForCollection_2", c)) break;
    }
    return true;
  }

  // COMMA WaitKeyValue
  private static boolean WaitForCollection_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WaitForCollection_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && WaitKeyValue(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // identifier COLON Expression | identifier
  public static boolean WaitKeyValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WaitKeyValue")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = WaitKeyValue_0(b, l + 1);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, WAIT_KEY_VALUE, r);
    return r;
  }

  // identifier COLON Expression
  private static boolean WaitKeyValue_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WaitKeyValue_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, COLON);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // where Expression
  public static boolean WhereClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WhereClause")) return false;
    if (!nextTokenIs(b, WHERE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WHERE_CLAUSE, null);
    r = consumeToken(b, WHERE);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // while Expression WhileStatementBody
  public static boolean WhileStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WhileStatement")) return false;
    if (!nextTokenIs(b, WHILE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WHILE_STATEMENT, null);
    r = consumeToken(b, WHILE);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && WhileStatementBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LEFT_BRACE Block RIGHT_BRACE
  public static boolean WhileStatementBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WhileStatementBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && Block(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, WHILE_STATEMENT_BODY, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE Block RIGHT_BRACE
  public static boolean WorkerBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && Block(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, WORKER_BODY, r);
    return r;
  }

  /* ********************************************************** */
  // AnnotationAttachment* worker identifier ReturnParameter? WorkerBody
  public static boolean WorkerDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WORKER_DEFINITION, "<worker definition>");
    r = WorkerDefinition_0(b, l + 1);
    r = r && consumeTokens(b, 1, WORKER, IDENTIFIER);
    p = r; // pin = 2
    r = r && report_error_(b, WorkerDefinition_3(b, l + 1));
    r = p && WorkerBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, WorkerDefinitionRecover_parser_);
    return r || p;
  }

  // AnnotationAttachment*
  private static boolean WorkerDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerDefinition_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "WorkerDefinition_0", c)) break;
    }
    return true;
  }

  // ReturnParameter?
  private static boolean WorkerDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerDefinition_3")) return false;
    ReturnParameter(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // StatementRecover
  static boolean WorkerDefinitionRecover(PsiBuilder b, int l) {
    return StatementRecover(b, l + 1);
  }

  /* ********************************************************** */
  // identifier
  public static boolean WorkerName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, WORKER_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // Expression RARROW PeerWorker (COMMA Expression)? SEMICOLON
  public static boolean WorkerSendAsyncStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerSendAsyncStatement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WORKER_SEND_ASYNC_STATEMENT, "<worker send async statement>");
    r = Expression(b, l + 1, -1);
    r = r && consumeToken(b, RARROW);
    p = r; // pin = 2
    r = r && report_error_(b, PeerWorker(b, l + 1));
    r = p && report_error_(b, WorkerSendAsyncStatement_3(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA Expression)?
  private static boolean WorkerSendAsyncStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerSendAsyncStatement_3")) return false;
    WorkerSendAsyncStatement_3_0(b, l + 1);
    return true;
  }

  // COMMA Expression
  private static boolean WorkerSendAsyncStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerSendAsyncStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // WorkerDefinition+ Statement*
  public static boolean WorkerWithStatementsBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerWithStatementsBlock")) return false;
    if (!nextTokenIs(b, "<worker with statements block>", AT, WORKER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, WORKER_WITH_STATEMENTS_BLOCK, "<worker with statements block>");
    r = WorkerWithStatementsBlock_0(b, l + 1);
    r = r && WorkerWithStatementsBlock_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // WorkerDefinition+
  private static boolean WorkerWithStatementsBlock_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerWithStatementsBlock_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = WorkerDefinition(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!WorkerDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "WorkerWithStatementsBlock_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // Statement*
  private static boolean WorkerWithStatementsBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerWithStatementsBlock_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!Statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "WorkerWithStatementsBlock_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // XML_ALL_CHAR*
  public static boolean XmlAllowedText(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlAllowedText")) return false;
    Marker m = enter_section_(b, l, _NONE_, XML_ALLOWED_TEXT, "<xml allowed text>");
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, XML_ALL_CHAR)) break;
      if (!empty_element_parsed_guard_(b, "XmlAllowedText", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // AT (LEFT_BRACKET Expression RIGHT_BRACKET)?
  public static boolean XmlAttrib(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlAttrib")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, XML_ATTRIB, null);
    r = consumeToken(b, AT);
    p = r; // pin = 1
    r = r && XmlAttrib_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LEFT_BRACKET Expression RIGHT_BRACKET)?
  private static boolean XmlAttrib_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlAttrib_1")) return false;
    XmlAttrib_1_0(b, l + 1);
    return true;
  }

  // LEFT_BRACKET Expression RIGHT_BRACKET
  private static boolean XmlAttrib_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlAttrib_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACKET);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, RIGHT_BRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (identifier COLON)? (identifier | MUL)
  public static boolean XmlElementAccessFilter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlElementAccessFilter")) return false;
    if (!nextTokenIs(b, "<xml element access filter>", IDENTIFIER, MUL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, XML_ELEMENT_ACCESS_FILTER, "<xml element access filter>");
    r = XmlElementAccessFilter_0(b, l + 1);
    r = r && XmlElementAccessFilter_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (identifier COLON)?
  private static boolean XmlElementAccessFilter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlElementAccessFilter_0")) return false;
    XmlElementAccessFilter_0_0(b, l + 1);
    return true;
  }

  // identifier COLON
  private static boolean XmlElementAccessFilter_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlElementAccessFilter_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // identifier | MUL
  private static boolean XmlElementAccessFilter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlElementAccessFilter_1")) return false;
    boolean r;
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, MUL);
    return r;
  }

  /* ********************************************************** */
  // LT XmlElementAccessFilter (PIPE XmlElementAccessFilter)* GT
  public static boolean XmlElementNames(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlElementNames")) return false;
    if (!nextTokenIs(b, LT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && XmlElementAccessFilter(b, l + 1);
    r = r && XmlElementNames_2(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, XML_ELEMENT_NAMES, r);
    return r;
  }

  // (PIPE XmlElementAccessFilter)*
  private static boolean XmlElementNames_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlElementNames_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!XmlElementNames_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "XmlElementNames_2", c)) break;
    }
    return true;
  }

  // PIPE XmlElementAccessFilter
  private static boolean XmlElementNames_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlElementNames_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PIPE);
    r = r && XmlElementAccessFilter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // XML_LITERAL_START XmlAllowedText XML_LITERAL_END
  public static boolean XmlLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlLiteral")) return false;
    if (!nextTokenIs(b, XML_LITERAL_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, XML_LITERAL_START);
    r = r && XmlAllowedText(b, l + 1);
    r = r && consumeToken(b, XML_LITERAL_END);
    exit_section_(b, m, XML_LITERAL, r);
    return r;
  }

  /* ********************************************************** */
  // DIV XmlElementNames Index?
  //                     | DIV MUL
  //                     | DIV MUL MUL DIV XmlElementNames
  public static boolean XmlStepExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlStepExpression")) return false;
    if (!nextTokenIs(b, DIV)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = XmlStepExpression_0(b, l + 1);
    if (!r) r = parseTokens(b, 0, DIV, MUL);
    if (!r) r = XmlStepExpression_2(b, l + 1);
    exit_section_(b, m, XML_STEP_EXPRESSION, r);
    return r;
  }

  // DIV XmlElementNames Index?
  private static boolean XmlStepExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlStepExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DIV);
    r = r && XmlElementNames(b, l + 1);
    r = r && XmlStepExpression_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // Index?
  private static boolean XmlStepExpression_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlStepExpression_0_2")) return false;
    Index(b, l + 1);
    return true;
  }

  // DIV MUL MUL DIV XmlElementNames
  private static boolean XmlStepExpression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlStepExpression_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DIV, MUL, MUL, DIV);
    r = r && XmlElementNames(b, l + 1);
    exit_section_(b, m, null, r);
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
  // <<isPackageExpected>> PackageReference? AnyIdentifierName
  public static boolean functionNameReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionNameReference")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_NAME_REFERENCE, "<function name reference>");
    r = isPackageExpected(b, l + 1);
    r = r && functionNameReference_1(b, l + 1);
    r = r && AnyIdentifierName(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // PackageReference?
  private static boolean functionNameReference_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionNameReference_1")) return false;
    PackageReference(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // new (UserDefineTypeName | StreamTypeName) LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS
  public static boolean initWithType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initWithType")) return false;
    if (!nextTokenIs(b, NEW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INIT_WITH_TYPE, null);
    r = consumeToken(b, NEW);
    r = r && initWithType_1(b, l + 1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 3
    r = r && report_error_(b, initWithType_3(b, l + 1));
    r = p && consumeToken(b, RIGHT_PARENTHESIS) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // UserDefineTypeName | StreamTypeName
  private static boolean initWithType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initWithType_1")) return false;
    boolean r;
    r = UserDefineTypeName(b, l + 1);
    if (!r) r = StreamTypeName(b, l + 1);
    return r;
  }

  // InvocationArgList?
  private static boolean initWithType_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initWithType_3")) return false;
    InvocationArgList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // new (LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS)?
  public static boolean initWithoutType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initWithoutType")) return false;
    if (!nextTokenIs(b, NEW)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NEW);
    r = r && initWithoutType_1(b, l + 1);
    exit_section_(b, m, INIT_WITHOUT_TYPE, r);
    return r;
  }

  // (LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS)?
  private static boolean initWithoutType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initWithoutType_1")) return false;
    initWithoutType_1_0(b, l + 1);
    return true;
  }

  // LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS
  private static boolean initWithoutType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initWithoutType_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_PARENTHESIS);
    r = r && initWithoutType_1_0_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  // InvocationArgList?
  private static boolean initWithoutType_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initWithoutType_1_0_1")) return false;
    InvocationArgList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // public? listener TypeName? identifier ASSIGN Expression SEMICOLON
  static boolean listenerDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listenerDefinition")) return false;
    if (!nextTokenIs(b, "", LISTENER, PUBLIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = listenerDefinition_0(b, l + 1);
    r = r && consumeToken(b, LISTENER);
    p = r; // pin = 2
    r = r && report_error_(b, listenerDefinition_2(b, l + 1));
    r = p && report_error_(b, consumeTokens(b, -1, IDENTIFIER, ASSIGN)) && r;
    r = p && report_error_(b, Expression(b, l + 1, -1)) && r;
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

  // TypeName?
  private static boolean listenerDefinition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listenerDefinition_2")) return false;
    TypeName(b, l + 1, -1);
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
  // if Expression (LEFT_BRACE Block RIGHT_BRACE)
  static boolean pinnedElseClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pinnedElseClause")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, IF);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && pinnedElseClause_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean pinnedElseClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pinnedElseClause_2")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, Block(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // DOCTYPE
  //                 | DOCSERVICE
  //                 | DOCVARIABLE
  //                 | DOCVAR
  //                 | DOCANNOTATION
  //                 | DOCMODULE
  //                 | DOCFUNCTION
  //                 | DOCPARAMETER
  //                 | DOCCONST
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
  // final? TypeName identifier ASSIGN Expression SEMICOLON
  static boolean typedVariableDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typedVariableDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = typedVariableDefinition_0(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    p = r; // pin = 2
    r = r && report_error_(b, consumeTokens(b, -1, IDENTIFIER, ASSIGN));
    r = p && report_error_(b, Expression(b, l + 1, -1)) && r;
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
  // final? var identifier ASSIGN Expression SEMICOLON
  static boolean varDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDefinition")) return false;
    if (!nextTokenIs(b, "", FINAL, VAR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = varDefinition_0(b, l + 1);
    r = r && consumeTokens(b, 1, VAR, IDENTIFIER, ASSIGN);
    p = r; // pin = 2
    r = r && report_error_(b, Expression(b, l + 1, -1));
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
  // final? (TypeName | var) BindingPattern ASSIGN Expression SEMICOLON
  public static boolean variableDefinitionStatementWithAssignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDefinitionStatementWithAssignment")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_DEFINITION_STATEMENT_WITH_ASSIGNMENT, "<variable definition statement with assignment>");
    r = variableDefinitionStatementWithAssignment_0(b, l + 1);
    r = r && variableDefinitionStatementWithAssignment_1(b, l + 1);
    r = r && BindingPattern(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    p = r; // pin = 4
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // final?
  private static boolean variableDefinitionStatementWithAssignment_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDefinitionStatementWithAssignment_0")) return false;
    consumeToken(b, FINAL);
    return true;
  }

  // TypeName | var
  private static boolean variableDefinitionStatementWithAssignment_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDefinitionStatementWithAssignment_1")) return false;
    boolean r;
    r = TypeName(b, l + 1, -1);
    if (!r) r = consumeToken(b, VAR);
    return r;
  }

  /* ********************************************************** */
  // TypeName identifier SEMICOLON
  public static boolean variableDefinitionStatementWithoutAssignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDefinitionStatementWithoutAssignment")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_DEFINITION_STATEMENT_WITHOUT_ASSIGNMENT, "<variable definition statement without assignment>");
    r = TypeName(b, l + 1, -1);
    r = r && consumeTokens(b, 1, IDENTIFIER, SEMICOLON);
    p = r; // pin = 2
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // DOT XmlElementNames
  public static boolean xmlElementFilter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "xmlElementFilter")) return false;
    if (!nextTokenIs(b, DOT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && XmlElementNames(b, l + 1);
    exit_section_(b, m, XML_ELEMENT_FILTER, r);
    return r;
  }

  /* ********************************************************** */
  // Expression root: ConstantExpression
  // Operator priority table:
  // 0: ATOM(SimpleLiteralConstExpression)
  // 1: ATOM(RecordLiteralConstExpression)
  // 2: BINARY(ConstDivMulModExpression)
  // 3: BINARY(ConstAddSubExpression)
  // 4: PREFIX(ConstGroupExpression)
  public static boolean ConstantExpression(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "ConstantExpression")) return false;
    addVariant(b, "<constant expression>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<constant expression>");
    r = SimpleLiteralConstExpression(b, l + 1);
    if (!r) r = RecordLiteralConstExpression(b, l + 1);
    if (!r) r = ConstGroupExpression(b, l + 1);
    p = r;
    r = r && ConstantExpression_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean ConstantExpression_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "ConstantExpression_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 2 && ConstDivMulModExpression_0(b, l + 1)) {
        r = ConstantExpression(b, l, 2);
        exit_section_(b, l, m, CONST_DIV_MUL_MOD_EXPRESSION, r, true, null);
      }
      else if (g < 3 && ConstAddSubExpression_0(b, l + 1)) {
        r = ConstantExpression(b, l, 3);
        exit_section_(b, l, m, CONST_ADD_SUB_EXPRESSION, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // SimpleLiteral
  public static boolean SimpleLiteralConstExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteralConstExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMPLE_LITERAL_CONST_EXPRESSION, "<simple literal const expression>");
    r = SimpleLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // RecordLiteral
  public static boolean RecordLiteralConstExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteralConstExpression")) return false;
    if (!nextTokenIsSmart(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = RecordLiteral(b, l + 1);
    exit_section_(b, m, RECORD_LITERAL_CONST_EXPRESSION, r);
    return r;
  }

  // DIV | MUL
  private static boolean ConstDivMulModExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstDivMulModExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, DIV);
    if (!r) r = consumeTokenSmart(b, MUL);
    return r;
  }

  // ADD | SUB
  private static boolean ConstAddSubExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstAddSubExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, ADD);
    if (!r) r = consumeTokenSmart(b, SUB);
    return r;
  }

  public static boolean ConstGroupExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstGroupExpression")) return false;
    if (!nextTokenIsSmart(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, LEFT_PARENTHESIS);
    p = r;
    r = p && ConstantExpression(b, l, -1);
    r = p && report_error_(b, consumeToken(b, RIGHT_PARENTHESIS)) && r;
    exit_section_(b, l, m, CONST_GROUP_EXPRESSION, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // Expression root: Expression
  // Operator priority table:
  // 0: ATOM(AnonymousFunctionExpression)
  // 1: ATOM(SimpleLiteralExpression)
  // 2: ATOM(StringTemplateLiteralExpression)
  // 3: ATOM(XmlLiteralExpression)
  // 4: ATOM(TableLiteralExpression)
  // 5: ATOM(RecordLiteralExpression)
  // 6: PREFIX(GroupExpression)
  // 7: POSTFIX(TernaryExpression)
  // 8: ATOM(ListConstructorExpression)
  // 9: ATOM(ActionInvocationExpression)
  // 10: ATOM(VariableReferenceExpression)
  // 11: ATOM(TypeInitExpression)
  // 12: ATOM(TypeConversionExpression)
  // 13: ATOM(UnaryExpression)
  // 14: BINARY(BinaryDivMulModExpression)
  // 15: BINARY(BinaryAddSubExpression)
  // 16: BINARY(BinaryCompareExpression)
  // 17: BINARY(BinaryEqualExpression)
  // 18: BINARY(BinaryAndExpression)
  // 19: BINARY(BinaryOrExpression)
  // 20: ATOM(CheckedExpression)
  // 21: ATOM(CheckPanicExpression)
  // 22: BINARY(ElvisExpression)
  // 23: ATOM(WaitExpression)
  // 24: POSTFIX(WorkerSendSyncExpression)
  // 25: ATOM(WorkerReceiveExpression)
  // 26: ATOM(FlushWorkerExpression)
  // 27: BINARY(IntegerRangeExpression)
  // 28: BINARY(BitwiseExpression)
  // 29: BINARY(BitwiseShiftExpression)
  // 30: ATOM(ServiceConstructorExpression)
  // 31: POSTFIX(TypeTestExpression)
  // 32: BINARY(BinaryRefEqualExpression)
  // 33: ATOM(TrapExpression)
  // 34: ATOM(TypeDescExpression)
  // 35: POSTFIX(AnnotationActionExpression)
  // 36: ATOM(QueryExpression)
  // 37: ATOM(QueryActionExpression)
  // 38: ATOM(LetExpression)
  public static boolean Expression(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "Expression")) return false;
    addVariant(b, "<expression>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<expression>");
    r = AnonymousFunctionExpression(b, l + 1);
    if (!r) r = SimpleLiteralExpression(b, l + 1);
    if (!r) r = StringTemplateLiteralExpression(b, l + 1);
    if (!r) r = XmlLiteralExpression(b, l + 1);
    if (!r) r = TableLiteralExpression(b, l + 1);
    if (!r) r = RecordLiteralExpression(b, l + 1);
    if (!r) r = GroupExpression(b, l + 1);
    if (!r) r = ListConstructorExpression(b, l + 1);
    if (!r) r = ActionInvocationExpression(b, l + 1);
    if (!r) r = VariableReferenceExpression(b, l + 1);
    if (!r) r = TypeInitExpression(b, l + 1);
    if (!r) r = TypeConversionExpression(b, l + 1);
    if (!r) r = UnaryExpression(b, l + 1);
    if (!r) r = CheckedExpression(b, l + 1);
    if (!r) r = CheckPanicExpression(b, l + 1);
    if (!r) r = WaitExpression(b, l + 1);
    if (!r) r = WorkerReceiveExpression(b, l + 1);
    if (!r) r = FlushWorkerExpression(b, l + 1);
    if (!r) r = ServiceConstructorExpression(b, l + 1);
    if (!r) r = TrapExpression(b, l + 1);
    if (!r) r = TypeDescExpression(b, l + 1);
    if (!r) r = QueryExpression(b, l + 1);
    if (!r) r = QueryActionExpression(b, l + 1);
    if (!r) r = LetExpression(b, l + 1);
    p = r;
    r = r && Expression_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean Expression_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "Expression_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 7 && TernaryExpression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, TERNARY_EXPRESSION, r, true, null);
      }
      else if (g < 14 && BinaryDivMulModExpression_0(b, l + 1)) {
        r = Expression(b, l, 14);
        exit_section_(b, l, m, BINARY_DIV_MUL_MOD_EXPRESSION, r, true, null);
      }
      else if (g < 15 && BinaryAddSubExpression_0(b, l + 1)) {
        r = Expression(b, l, 15);
        exit_section_(b, l, m, BINARY_ADD_SUB_EXPRESSION, r, true, null);
      }
      else if (g < 16 && BinaryCompareExpression_0(b, l + 1)) {
        r = Expression(b, l, 16);
        exit_section_(b, l, m, BINARY_COMPARE_EXPRESSION, r, true, null);
      }
      else if (g < 17 && BinaryEqualExpression_0(b, l + 1)) {
        r = Expression(b, l, 17);
        exit_section_(b, l, m, BINARY_EQUAL_EXPRESSION, r, true, null);
      }
      else if (g < 18 && BinaryAndExpression_0(b, l + 1)) {
        r = Expression(b, l, 18);
        exit_section_(b, l, m, BINARY_AND_EXPRESSION, r, true, null);
      }
      else if (g < 19 && consumeTokenSmart(b, OR)) {
        r = Expression(b, l, 19);
        exit_section_(b, l, m, BINARY_OR_EXPRESSION, r, true, null);
      }
      else if (g < 22 && consumeTokenSmart(b, ELVIS)) {
        r = Expression(b, l, 22);
        exit_section_(b, l, m, ELVIS_EXPRESSION, r, true, null);
      }
      else if (g < 24 && WorkerSendSyncExpression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, WORKER_SEND_SYNC_EXPRESSION, r, true, null);
      }
      else if (g < 27 && IntegerRangeExpression_0(b, l + 1)) {
        r = Expression(b, l, 27);
        exit_section_(b, l, m, INTEGER_RANGE_EXPRESSION, r, true, null);
      }
      else if (g < 28 && BitwiseExpression_0(b, l + 1)) {
        r = Expression(b, l, 28);
        exit_section_(b, l, m, BITWISE_EXPRESSION, r, true, null);
      }
      else if (g < 29 && BitwiseShiftExpression_0(b, l + 1)) {
        r = Expression(b, l, 29);
        exit_section_(b, l, m, BITWISE_SHIFT_EXPRESSION, r, true, null);
      }
      else if (g < 31 && TypeTestExpression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, TYPE_TEST_EXPRESSION, r, true, null);
      }
      else if (g < 32 && BinaryRefEqualExpression_0(b, l + 1)) {
        r = Expression(b, l, 32);
        exit_section_(b, l, m, BINARY_REF_EQUAL_EXPRESSION, r, true, null);
      }
      else if (g < 35 && AnnotationActionExpression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, ANNOTATION_ACTION_EXPRESSION, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // AnonymousFunctionExpr
  public static boolean AnonymousFunctionExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnonymousFunctionExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ANONYMOUS_FUNCTION_EXPRESSION, "<anonymous function expression>");
    r = AnonymousFunctionExpr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SimpleLiteral
  public static boolean SimpleLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleLiteralExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMPLE_LITERAL_EXPRESSION, "<simple literal expression>");
    r = SimpleLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // StringTemplateLiteral
  public static boolean StringTemplateLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateLiteralExpression")) return false;
    if (!nextTokenIsSmart(b, STRING_TEMPLATE_LITERAL_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = StringTemplateLiteral(b, l + 1);
    exit_section_(b, m, STRING_TEMPLATE_LITERAL_EXPRESSION, r);
    return r;
  }

  // XmlLiteral
  public static boolean XmlLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlLiteralExpression")) return false;
    if (!nextTokenIsSmart(b, XML_LITERAL_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = XmlLiteral(b, l + 1);
    exit_section_(b, m, XML_LITERAL_EXPRESSION, r);
    return r;
  }

  // TableLiteral
  public static boolean TableLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableLiteralExpression")) return false;
    if (!nextTokenIsSmart(b, TABLE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TableLiteral(b, l + 1);
    exit_section_(b, m, TABLE_LITERAL_EXPRESSION, r);
    return r;
  }

  // RecordLiteral
  public static boolean RecordLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteralExpression")) return false;
    if (!nextTokenIsSmart(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = RecordLiteral(b, l + 1);
    exit_section_(b, m, RECORD_LITERAL_EXPRESSION, r);
    return r;
  }

  public static boolean GroupExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GroupExpression")) return false;
    if (!nextTokenIsSmart(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, LEFT_PARENTHESIS);
    p = r;
    r = p && Expression(b, l, 6);
    r = p && report_error_(b, consumeToken(b, RIGHT_PARENTHESIS)) && r;
    exit_section_(b, l, m, GROUP_EXPRESSION, r, p, null);
    return r || p;
  }

  // QUESTION_MARK Expression COLON Expression
  private static boolean TernaryExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TernaryExpression_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokenSmart(b, QUESTION_MARK);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && report_error_(b, consumeToken(b, COLON)) && r;
    r = p && Expression(b, l + 1, -1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ListConstructorExpr
  public static boolean ListConstructorExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ListConstructorExpression")) return false;
    if (!nextTokenIsSmart(b, LEFT_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ListConstructorExpr(b, l + 1);
    exit_section_(b, m, LIST_CONSTRUCTOR_EXPRESSION, r);
    return r;
  }

  // ActionInvocation
  public static boolean ActionInvocationExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ActionInvocationExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ACTION_INVOCATION_EXPRESSION, "<action invocation expression>");
    r = ActionInvocation(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AnnotationAttachment* start? VariableReference
  public static boolean VariableReferenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableReferenceExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_REFERENCE_EXPRESSION, "<variable reference expression>");
    r = VariableReferenceExpression_0(b, l + 1);
    r = r && VariableReferenceExpression_1(b, l + 1);
    r = r && VariableReference(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AnnotationAttachment*
  private static boolean VariableReferenceExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableReferenceExpression_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "VariableReferenceExpression_0", c)) break;
    }
    return true;
  }

  // start?
  private static boolean VariableReferenceExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableReferenceExpression_1")) return false;
    consumeTokenSmart(b, START);
    return true;
  }

  // initWithType | initWithoutType
  public static boolean TypeInitExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeInitExpression")) return false;
    if (!nextTokenIsSmart(b, NEW)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = initWithType(b, l + 1);
    if (!r) r = initWithoutType(b, l + 1);
    exit_section_(b, m, TYPE_INIT_EXPRESSION, r);
    return r;
  }

  // LT (AnnotationAttachment+ TypeName? | TypeName) GT Expression
  public static boolean TypeConversionExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeConversionExpression")) return false;
    if (!nextTokenIsSmart(b, LT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TYPE_CONVERSION_EXPRESSION, null);
    r = consumeTokenSmart(b, LT);
    p = r; // pin = 1
    r = r && report_error_(b, TypeConversionExpression_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, GT)) && r;
    r = p && Expression(b, l + 1, -1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AnnotationAttachment+ TypeName? | TypeName
  private static boolean TypeConversionExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeConversionExpression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeConversionExpression_1_0(b, l + 1);
    if (!r) r = TypeName(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // AnnotationAttachment+ TypeName?
  private static boolean TypeConversionExpression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeConversionExpression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeConversionExpression_1_0_0(b, l + 1);
    r = r && TypeConversionExpression_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // AnnotationAttachment+
  private static boolean TypeConversionExpression_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeConversionExpression_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = AnnotationAttachment(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TypeConversionExpression_1_0_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // TypeName?
  private static boolean TypeConversionExpression_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeConversionExpression_1_0_1")) return false;
    TypeName(b, l + 1, -1);
    return true;
  }

  // (ADD | SUB | BIT_COMPLEMENT | NOT | typeof) Expression
  public static boolean UnaryExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnaryExpression")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _COLLAPSE_, UNARY_EXPRESSION, "<unary expression>");
    r = UnaryExpression_0(b, l + 1);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ADD | SUB | BIT_COMPLEMENT | NOT | typeof
  private static boolean UnaryExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnaryExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, ADD);
    if (!r) r = consumeTokenSmart(b, SUB);
    if (!r) r = consumeTokenSmart(b, BIT_COMPLEMENT);
    if (!r) r = consumeTokenSmart(b, NOT);
    if (!r) r = consumeTokenSmart(b, TYPEOF);
    return r;
  }

  // DIV | MUL | MOD
  private static boolean BinaryDivMulModExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryDivMulModExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, DIV);
    if (!r) r = consumeTokenSmart(b, MUL);
    if (!r) r = consumeTokenSmart(b, MOD);
    return r;
  }

  // ADD | SUB
  private static boolean BinaryAddSubExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryAddSubExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, ADD);
    if (!r) r = consumeTokenSmart(b, SUB);
    return r;
  }

  // LT_EQUAL | GT_EQUAL | GT | LT
  private static boolean BinaryCompareExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryCompareExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, LT_EQUAL);
    if (!r) r = consumeTokenSmart(b, GT_EQUAL);
    if (!r) r = consumeTokenSmart(b, GT);
    if (!r) r = consumeTokenSmart(b, LT);
    return r;
  }

  // EQUAL | NOT_EQUAL
  private static boolean BinaryEqualExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryEqualExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, EQUAL);
    if (!r) r = consumeTokenSmart(b, NOT_EQUAL);
    return r;
  }

  // <<isNotInStreams>> AND
  private static boolean BinaryAndExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryAndExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = isNotInStreams(b, l + 1);
    r = r && consumeToken(b, AND);
    exit_section_(b, m, null, r);
    return r;
  }

  // check Expression
  public static boolean CheckedExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CheckedExpression")) return false;
    if (!nextTokenIsSmart(b, CHECK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CHECKED_EXPRESSION, null);
    r = consumeTokenSmart(b, CHECK);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // checkpanic Expression
  public static boolean CheckPanicExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CheckPanicExpression")) return false;
    if (!nextTokenIsSmart(b, CHECKPANIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CHECK_PANIC_EXPRESSION, null);
    r = consumeTokenSmart(b, CHECKPANIC);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // wait (WaitForCollection | Expression)
  public static boolean WaitExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WaitExpression")) return false;
    if (!nextTokenIsSmart(b, WAIT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WAIT_EXPRESSION, null);
    r = consumeTokenSmart(b, WAIT);
    p = r; // pin = 1
    r = r && WaitExpression_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // WaitForCollection | Expression
  private static boolean WaitExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WaitExpression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = WaitForCollection(b, l + 1);
    if (!r) r = Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SYNCRARROW PeerWorker
  private static boolean WorkerSendSyncExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerSendSyncExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, SYNCRARROW);
    r = r && PeerWorker(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LARROW PeerWorker (COMMA Expression)?
  public static boolean WorkerReceiveExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerReceiveExpression")) return false;
    if (!nextTokenIsSmart(b, LARROW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WORKER_RECEIVE_EXPRESSION, null);
    r = consumeTokenSmart(b, LARROW);
    p = r; // pin = 1
    r = r && report_error_(b, PeerWorker(b, l + 1));
    r = p && WorkerReceiveExpression_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA Expression)?
  private static boolean WorkerReceiveExpression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerReceiveExpression_2")) return false;
    WorkerReceiveExpression_2_0(b, l + 1);
    return true;
  }

  // COMMA Expression
  private static boolean WorkerReceiveExpression_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WorkerReceiveExpression_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, COMMA);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // FlushWorker
  public static boolean FlushWorkerExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FlushWorkerExpression")) return false;
    if (!nextTokenIsSmart(b, FLUSH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = FlushWorker(b, l + 1);
    exit_section_(b, m, FLUSH_WORKER_EXPRESSION, r);
    return r;
  }

  // ELLIPSIS | HALF_OPEN_RANGE
  private static boolean IntegerRangeExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IntegerRangeExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, ELLIPSIS);
    if (!r) r = consumeTokenSmart(b, HALF_OPEN_RANGE);
    return r;
  }

  // BITAND | PIPE | BITXOR
  private static boolean BitwiseExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BitwiseExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, BITAND);
    if (!r) r = consumeTokenSmart(b, PIPE);
    if (!r) r = consumeTokenSmart(b, BITXOR);
    return r;
  }

  // (ShiftExpression)
  private static boolean BitwiseShiftExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BitwiseShiftExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ShiftExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // AnnotationAttachment* service ServiceBody
  public static boolean ServiceConstructorExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceConstructorExpression")) return false;
    if (!nextTokenIsSmart(b, AT, SERVICE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SERVICE_CONSTRUCTOR_EXPRESSION, "<service constructor expression>");
    r = ServiceConstructorExpression_0(b, l + 1);
    r = r && consumeToken(b, SERVICE);
    p = r; // pin = 2
    r = r && ServiceBody(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AnnotationAttachment*
  private static boolean ServiceConstructorExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceConstructorExpression_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ServiceConstructorExpression_0", c)) break;
    }
    return true;
  }

  // is TypeName
  private static boolean TypeTestExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeTestExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, IS);
    r = r && TypeName(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // REF_EQUAL | REF_NOT_EQUAL
  private static boolean BinaryRefEqualExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BinaryRefEqualExpression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, REF_EQUAL);
    if (!r) r = consumeTokenSmart(b, REF_NOT_EQUAL);
    return r;
  }

  // trap Expression
  public static boolean TrapExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TrapExpression")) return false;
    if (!nextTokenIsSmart(b, TRAP)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRAP_EXPRESSION, null);
    r = consumeTokenSmart(b, TRAP);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // TypeName
  public static boolean TypeDescExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDescExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_DESC_EXPRESSION, "<type desc expression>");
    r = TypeName(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ANNOTATION_ACCESS NameReference
  private static boolean AnnotationActionExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationActionExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, ANNOTATION_ACCESS);
    r = r && NameReference(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // QueryExpr
  public static boolean QueryExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "QueryExpression")) return false;
    if (!nextTokenIsSmart(b, FROM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = QueryExpr(b, l + 1);
    exit_section_(b, m, QUERY_EXPRESSION, r);
    return r;
  }

  // QueryPipeline DoClause
  public static boolean QueryActionExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "QueryActionExpression")) return false;
    if (!nextTokenIsSmart(b, FROM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = QueryPipeline(b, l + 1);
    r = r && DoClause(b, l + 1);
    exit_section_(b, m, QUERY_ACTION_EXPRESSION, r);
    return r;
  }

  // let LetVarDecl (COMMA LetVarDecl)* IN Expression
  public static boolean LetExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LetExpression")) return false;
    if (!nextTokenIsSmart(b, LET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LET_EXPRESSION, null);
    r = consumeTokenSmart(b, LET);
    p = r; // pin = 1
    r = r && report_error_(b, LetVarDecl(b, l + 1));
    r = p && report_error_(b, LetExpression_2(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, IN)) && r;
    r = p && Expression(b, l + 1, -1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA LetVarDecl)*
  private static boolean LetExpression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LetExpression_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!LetExpression_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "LetExpression_2", c)) break;
    }
    return true;
  }

  // COMMA LetVarDecl
  private static boolean LetExpression_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LetExpression_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, COMMA);
    r = r && LetVarDecl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Expression root: StaticMatchLiteral
  // Operator priority table:
  // 0: ATOM(StaticMatchSimpleLiteral)
  // 1: ATOM(StaticMatchRecordLiteral)
  // 2: ATOM(StaticMatchListLiteral)
  // 3: ATOM(StaticMatchIdentifierLiteral)
  // 4: BINARY(StaticMatchOrExpression)
  public static boolean StaticMatchLiteral(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "StaticMatchLiteral")) return false;
    addVariant(b, "<static match literal>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<static match literal>");
    r = StaticMatchSimpleLiteral(b, l + 1);
    if (!r) r = StaticMatchRecordLiteral(b, l + 1);
    if (!r) r = StaticMatchListLiteral(b, l + 1);
    if (!r) r = StaticMatchIdentifierLiteral(b, l + 1);
    p = r;
    r = r && StaticMatchLiteral_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean StaticMatchLiteral_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "StaticMatchLiteral_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 4 && consumeTokenSmart(b, PIPE)) {
        r = StaticMatchLiteral(b, l, 4);
        exit_section_(b, l, m, STATIC_MATCH_OR_EXPRESSION, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // SimpleLiteral
  public static boolean StaticMatchSimpleLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StaticMatchSimpleLiteral")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATIC_MATCH_SIMPLE_LITERAL, "<static match simple literal>");
    r = SimpleLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // RecordLiteral
  public static boolean StaticMatchRecordLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StaticMatchRecordLiteral")) return false;
    if (!nextTokenIsSmart(b, LEFT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = RecordLiteral(b, l + 1);
    exit_section_(b, m, STATIC_MATCH_RECORD_LITERAL, r);
    return r;
  }

  // ListConstructorExpr
  public static boolean StaticMatchListLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StaticMatchListLiteral")) return false;
    if (!nextTokenIsSmart(b, LEFT_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ListConstructorExpr(b, l + 1);
    exit_section_(b, m, STATIC_MATCH_LIST_LITERAL, r);
    return r;
  }

  // identifier
  public static boolean StaticMatchIdentifierLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StaticMatchIdentifierLiteral")) return false;
    if (!nextTokenIsSmart(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, IDENTIFIER);
    exit_section_(b, m, STATIC_MATCH_IDENTIFIER_LITERAL, r);
    return r;
  }

  /* ********************************************************** */
  // Expression root: TypeName
  // Operator priority table:
  // 0: ATOM(TupleTypeName)
  // 1: ATOM(SimpleTypeName)
  // 2: ATOM(GroupTypeName)
  // 3: POSTFIX(ArrayTypeName)
  // 4: N_ARY(UnionTypeName)
  // 5: ATOM(ObjectTypeName)
  // 6: POSTFIX(NullableTypeName)
  // 7: ATOM(InclusiveRecordTypeDescriptor)
  // 8: ATOM(ExclusiveRecordTypeDescriptor)
  public static boolean TypeName(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "TypeName")) return false;
    addVariant(b, "<type name>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<type name>");
    r = TupleTypeName(b, l + 1);
    if (!r) r = SimpleTypeName(b, l + 1);
    if (!r) r = GroupTypeName(b, l + 1);
    if (!r) r = ObjectTypeName(b, l + 1);
    if (!r) r = InclusiveRecordTypeDescriptor(b, l + 1);
    if (!r) r = ExclusiveRecordTypeDescriptor(b, l + 1);
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

  // NULL_LITERAL
  //                    | AnyTypeName
  //                    | AnyDataTypeName
  //                    | HandleTypeName
  //                    | ValueTypeName
  //                    | ReferenceTypeName
  //                    | TypeDescTypeName
  //                    | NilLiteral
  public static boolean SimpleTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMPLE_TYPE_NAME, "<simple type name>");
    r = consumeTokenSmart(b, NULL_LITERAL);
    if (!r) r = AnyTypeName(b, l + 1);
    if (!r) r = AnyDataTypeName(b, l + 1);
    if (!r) r = HandleTypeName(b, l + 1);
    if (!r) r = ValueTypeName(b, l + 1);
    if (!r) r = ReferenceTypeName(b, l + 1);
    if (!r) r = TypeDescTypeName(b, l + 1);
    if (!r) r = NilLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
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

  // ((client? abstract) | (abstract? client?)) object LEFT_BRACE ObjectBody RIGHT_BRACE
  public static boolean ObjectTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_TYPE_NAME, "<object type name>");
    r = ObjectTypeName_0(b, l + 1);
    r = r && consumeTokensSmart(b, 2, OBJECT, LEFT_BRACE);
    p = r; // pin = 3
    r = r && report_error_(b, ObjectBody(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (client? abstract) | (abstract? client?)
  private static boolean ObjectTypeName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ObjectTypeName_0_0(b, l + 1);
    if (!r) r = ObjectTypeName_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // client? abstract
  private static boolean ObjectTypeName_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ObjectTypeName_0_0_0(b, l + 1);
    r = r && consumeToken(b, ABSTRACT);
    exit_section_(b, m, null, r);
    return r;
  }

  // client?
  private static boolean ObjectTypeName_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_0_0_0")) return false;
    consumeTokenSmart(b, CLIENT);
    return true;
  }

  // abstract? client?
  private static boolean ObjectTypeName_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ObjectTypeName_0_1_0(b, l + 1);
    r = r && ObjectTypeName_0_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // abstract?
  private static boolean ObjectTypeName_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_0_1_0")) return false;
    consumeTokenSmart(b, ABSTRACT);
    return true;
  }

  // client?
  private static boolean ObjectTypeName_0_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectTypeName_0_1_1")) return false;
    consumeTokenSmart(b, CLIENT);
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

  // record LEFT_BRACE FieldDescriptor* RIGHT_BRACE
  public static boolean InclusiveRecordTypeDescriptor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InclusiveRecordTypeDescriptor")) return false;
    if (!nextTokenIsSmart(b, RECORD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, RECORD, LEFT_BRACE);
    r = r && InclusiveRecordTypeDescriptor_2(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, INCLUSIVE_RECORD_TYPE_DESCRIPTOR, r);
    return r;
  }

  // FieldDescriptor*
  private static boolean InclusiveRecordTypeDescriptor_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InclusiveRecordTypeDescriptor_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FieldDescriptor(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "InclusiveRecordTypeDescriptor_2", c)) break;
    }
    return true;
  }

  // record LEFT_CLOSED_RECORD_DELIMITER FieldDescriptor* RecordRestFieldDefinition? RIGHT_CLOSED_RECORD_DELIMITER
  public static boolean ExclusiveRecordTypeDescriptor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExclusiveRecordTypeDescriptor")) return false;
    if (!nextTokenIsSmart(b, RECORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXCLUSIVE_RECORD_TYPE_DESCRIPTOR, null);
    r = consumeTokensSmart(b, 2, RECORD, LEFT_CLOSED_RECORD_DELIMITER);
    p = r; // pin = 2
    r = r && report_error_(b, ExclusiveRecordTypeDescriptor_2(b, l + 1));
    r = p && report_error_(b, ExclusiveRecordTypeDescriptor_3(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_CLOSED_RECORD_DELIMITER) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // FieldDescriptor*
  private static boolean ExclusiveRecordTypeDescriptor_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExclusiveRecordTypeDescriptor_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FieldDescriptor(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ExclusiveRecordTypeDescriptor_2", c)) break;
    }
    return true;
  }

  // RecordRestFieldDefinition?
  private static boolean ExclusiveRecordTypeDescriptor_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExclusiveRecordTypeDescriptor_3")) return false;
    RecordRestFieldDefinition(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // Expression root: VariableReference
  // Operator priority table:
  // 0: POSTFIX(MapArrayVariableReference)
  // 1: ATOM(StringFunctionInvocationReference)
  // 2: POSTFIX(InvocationReference)
  // 3: POSTFIX(FieldVariableReference)
  // 4: POSTFIX(XmlAttribVariableReference)
  // 5: POSTFIX(XmlElementFilterReference)
  // 6: ATOM(FunctionInvocationReference)
  // 7: ATOM(SimpleVariableReference)
  // 8: ATOM(TypeDescExprInvocationReference)
  // 9: POSTFIX(AnnotationAccessReference)
  // 10: PREFIX(GroupFieldVariableReference)
  // 11: PREFIX(GroupInvocationReference)
  // 12: PREFIX(GroupMapArrayVariableReference)
  // 13: ATOM(GroupStringFunctionInvocationReference)
  // 14: POSTFIX(XmlStepExpressionReference)
  public static boolean VariableReference(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "VariableReference")) return false;
    addVariant(b, "<variable reference>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<variable reference>");
    r = StringFunctionInvocationReference(b, l + 1);
    if (!r) r = FunctionInvocationReference(b, l + 1);
    if (!r) r = SimpleVariableReference(b, l + 1);
    if (!r) r = TypeDescExprInvocationReference(b, l + 1);
    if (!r) r = GroupFieldVariableReference(b, l + 1);
    if (!r) r = GroupStringFunctionInvocationReference(b, l + 1);
    p = r;
    r = r && VariableReference_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean VariableReference_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "VariableReference_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 0 && Index(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, MAP_ARRAY_VARIABLE_REFERENCE, r, true, null);
      }
      else if (g < 2 && Invocation(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, INVOCATION_REFERENCE, r, true, null);
      }
      else if (g < 3 && Field(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, FIELD_VARIABLE_REFERENCE, r, true, null);
      }
      else if (g < 4 && XmlAttrib(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, XML_ATTRIB_VARIABLE_REFERENCE, r, true, null);
      }
      else if (g < 5 && xmlElementFilter(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, XML_ELEMENT_FILTER_REFERENCE, r, true, null);
      }
      else if (g < 9 && AnnotationAccessReference_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, ANNOTATION_ACCESS_REFERENCE, r, true, null);
      }
      else if (g < 14 && XmlStepExpression(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, XML_STEP_EXPRESSION_REFERENCE, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // QUOTED_STRING_LITERAL Invocation
  public static boolean StringFunctionInvocationReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringFunctionInvocationReference")) return false;
    if (!nextTokenIsSmart(b, QUOTED_STRING_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, QUOTED_STRING_LITERAL);
    r = r && Invocation(b, l + 1);
    exit_section_(b, m, STRING_FUNCTION_INVOCATION_REFERENCE, r);
    return r;
  }

  // FunctionInvocation
  public static boolean FunctionInvocationReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionInvocationReference")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_INVOCATION_REFERENCE, "<function invocation reference>");
    r = FunctionInvocation(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // NameReference
  public static boolean SimpleVariableReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleVariableReference")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMPLE_VARIABLE_REFERENCE, "<simple variable reference>");
    r = NameReference(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // TypeDescExpression Invocation
  public static boolean TypeDescExprInvocationReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDescExprInvocationReference")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_DESC_EXPR_INVOCATION_REFERENCE, "<type desc expr invocation reference>");
    r = TypeDescExpression(b, l + 1);
    r = r && Invocation(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ANNOTATION_ACCESS NameReference
  private static boolean AnnotationAccessReference_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationAccessReference_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, ANNOTATION_ACCESS);
    r = r && NameReference(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean GroupFieldVariableReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GroupFieldVariableReference")) return false;
    if (!nextTokenIsSmart(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, LEFT_PARENTHESIS);
    p = r;
    r = p && VariableReference(b, l, 10);
    r = p && report_error_(b, GroupFieldVariableReference_1(b, l + 1)) && r;
    exit_section_(b, l, m, GROUP_FIELD_VARIABLE_REFERENCE, r, p, null);
    return r || p;
  }

  // RIGHT_PARENTHESIS Field
  private static boolean GroupFieldVariableReference_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GroupFieldVariableReference_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RIGHT_PARENTHESIS);
    r = r && Field(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean GroupInvocationReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GroupInvocationReference")) return false;
    if (!nextTokenIsSmart(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, LEFT_PARENTHESIS);
    p = r;
    r = p && VariableReference(b, l, 11);
    r = p && report_error_(b, GroupInvocationReference_1(b, l + 1)) && r;
    exit_section_(b, l, m, GROUP_INVOCATION_REFERENCE, r, p, null);
    return r || p;
  }

  // RIGHT_PARENTHESIS Invocation
  private static boolean GroupInvocationReference_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GroupInvocationReference_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RIGHT_PARENTHESIS);
    r = r && Invocation(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean GroupMapArrayVariableReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GroupMapArrayVariableReference")) return false;
    if (!nextTokenIsSmart(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, LEFT_PARENTHESIS);
    p = r;
    r = p && VariableReference(b, l, 12);
    r = p && report_error_(b, GroupMapArrayVariableReference_1(b, l + 1)) && r;
    exit_section_(b, l, m, GROUP_MAP_ARRAY_VARIABLE_REFERENCE, r, p, null);
    return r || p;
  }

  // RIGHT_PARENTHESIS Index
  private static boolean GroupMapArrayVariableReference_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GroupMapArrayVariableReference_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RIGHT_PARENTHESIS);
    r = r && Index(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_PARENTHESIS QUOTED_STRING_LITERAL RIGHT_PARENTHESIS Invocation
  public static boolean GroupStringFunctionInvocationReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GroupStringFunctionInvocationReference")) return false;
    if (!nextTokenIsSmart(b, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, LEFT_PARENTHESIS, QUOTED_STRING_LITERAL, RIGHT_PARENTHESIS);
    r = r && Invocation(b, l + 1);
    exit_section_(b, m, GROUP_STRING_FUNCTION_INVOCATION_REFERENCE, r);
    return r;
  }

  static final Parser ExprFuncBodyRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return ExprFuncBodyRecover(b, l + 1);
    }
  };
  static final Parser FormalParameterListRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return FormalParameterListRecover(b, l + 1);
    }
  };
  static final Parser InvocationArgListRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return InvocationArgListRecover(b, l + 1);
    }
  };
  static final Parser StatementRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return StatementRecover(b, l + 1);
    }
  };
  static final Parser TopLevelDefinitionRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return TopLevelDefinitionRecover(b, l + 1);
    }
  };
  static final Parser WorkerDefinitionRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return WorkerDefinitionRecover(b, l + 1);
    }
  };
}
