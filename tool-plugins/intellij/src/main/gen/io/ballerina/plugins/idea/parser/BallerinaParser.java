/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import com.intellij.psi.tree.IFileElementType;
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
    if (t instanceof IFileElementType) {
      r = parse_root_(t, b, 0);
    }
    else {
      r = false;
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return CompilationUnit(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(STATIC_MATCH_IDENTIFIER_LITERAL, STATIC_MATCH_LIST_LITERAL, STATIC_MATCH_LITERAL, STATIC_MATCH_OR_EXPRESSION,
      STATIC_MATCH_RECORD_LITERAL, STATIC_MATCH_SIMPLE_LITERAL),
    create_token_set_(FIELD_VARIABLE_REFERENCE, FUNCTION_INVOCATION_REFERENCE, INVOCATION_REFERENCE, MAP_ARRAY_VARIABLE_REFERENCE,
      SIMPLE_VARIABLE_REFERENCE, STRING_FUNCTION_INVOCATION_REFERENCE, TYPE_ACCESS_EXPR_INVOCATION_REFERENCE, VARIABLE_REFERENCE,
      XML_ATTRIB_VARIABLE_REFERENCE),
    create_token_set_(ARRAY_TYPE_NAME, EXCLUSIVE_RECORD_TYPE_DESCRIPTOR, GROUP_TYPE_NAME, INCLUSIVE_RECORD_TYPE_DESCRIPTOR,
      NULLABLE_TYPE_NAME, OBJECT_TYPE_NAME, SIMPLE_TYPE_NAME, TUPLE_TYPE_NAME,
      TYPE_NAME, UNION_TYPE_NAME),
    create_token_set_(ACTION_INVOCATION_EXPRESSION, ANNOTATION_ACTION_EXPRESSION, ARROW_FUNCTION_EXPRESSION, BINARY_ADD_SUB_EXPRESSION,
      BINARY_AND_EXPRESSION, BINARY_COMPARE_EXPRESSION, BINARY_DIV_MUL_MOD_EXPRESSION, BINARY_EQUAL_EXPRESSION,
      BINARY_OR_EXPRESSION, BINARY_REF_EQUAL_EXPRESSION, BITWISE_EXPRESSION, BITWISE_SHIFT_EXPRESSION,
      CHECKED_EXPRESSION, CHECK_PANIC_EXPRESSION, ELVIS_EXPRESSION, EXPRESSION,
      FLUSH_WORKER_EXPRESSION, GROUP_EXPRESSION, INTEGER_RANGE_EXPRESSION, LAMBDA_FUNCTION_EXPRESSION,
      LIST_CONSTRUCTOR_EXPRESSION, RECORD_LITERAL_EXPRESSION, SERVICE_CONSTRUCTOR_EXPRESSION, SIMPLE_LITERAL_EXPRESSION,
      STRING_TEMPLATE_LITERAL_EXPRESSION, TABLE_LITERAL_EXPRESSION, TABLE_QUERY_EXPRESSION, TERNARY_EXPRESSION,
      TRAP_EXPRESSION, TYPE_ACCESS_EXPRESSION, TYPE_CONVERSION_EXPRESSION, TYPE_INIT_EXPRESSION,
      TYPE_TEST_EXPRESSION, UNARY_EXPRESSION, VARIABLE_REFERENCE_EXPRESSION, WAIT_EXPRESSION,
      WORKER_RECEIVE_EXPRESSION, WORKER_SEND_SYNC_EXPRESSION, XML_LITERAL_EXPRESSION),
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
  // public? const? annotation TypeName? identifier (on AttachmentPoint (COMMA AttachmentPoint)*)? SEMICOLON
  public static boolean AnnotationDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ANNOTATION_DEFINITION, "<annotation definition>");
    r = AnnotationDefinition_0(b, l + 1);
    r = r && AnnotationDefinition_1(b, l + 1);
    r = r && consumeToken(b, ANNOTATION);
    p = r; // pin = 3
    r = r && report_error_(b, AnnotationDefinition_3(b, l + 1));
    r = p && report_error_(b, consumeToken(b, IDENTIFIER)) && r;
    r = p && report_error_(b, AnnotationDefinition_5(b, l + 1)) && r;
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

  // TypeName?
  private static boolean AnnotationDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_3")) return false;
    TypeName(b, l + 1, -1);
    return true;
  }

  // (on AttachmentPoint (COMMA AttachmentPoint)*)?
  private static boolean AnnotationDefinition_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_5")) return false;
    AnnotationDefinition_5_0(b, l + 1);
    return true;
  }

  // on AttachmentPoint (COMMA AttachmentPoint)*
  private static boolean AnnotationDefinition_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ON);
    r = r && AttachmentPoint(b, l + 1);
    r = r && AnnotationDefinition_5_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA AttachmentPoint)*
  private static boolean AnnotationDefinition_5_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_5_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationDefinition_5_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "AnnotationDefinition_5_0_2", c)) break;
    }
    return true;
  }

  // COMMA AttachmentPoint
  private static boolean AnnotationDefinition_5_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AnnotationDefinition_5_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && AttachmentPoint(b, l + 1);
    exit_section_(b, m, null, r);
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
  // ArrowFunctionWithoutParenthesis | ArrowFunctionWithParenthesis
  public static boolean ArrowFunction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunction")) return false;
    if (!nextTokenIs(b, "<arrow function>", IDENTIFIER, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARROW_FUNCTION, "<arrow function>");
    r = ArrowFunctionWithoutParenthesis(b, l + 1);
    if (!r) r = ArrowFunctionWithParenthesis(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LEFT_PARENTHESIS (ArrowParam (COMMA ArrowParam)*)? RIGHT_PARENTHESIS EQUAL_GT Expression
  static boolean ArrowFunctionWithParenthesis(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunctionWithParenthesis")) return false;
    if (!nextTokenIs(b, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_PARENTHESIS);
    r = r && ArrowFunctionWithParenthesis_1(b, l + 1);
    r = r && consumeTokens(b, 2, RIGHT_PARENTHESIS, EQUAL_GT);
    p = r; // pin = 4
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (ArrowParam (COMMA ArrowParam)*)?
  private static boolean ArrowFunctionWithParenthesis_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunctionWithParenthesis_1")) return false;
    ArrowFunctionWithParenthesis_1_0(b, l + 1);
    return true;
  }

  // ArrowParam (COMMA ArrowParam)*
  private static boolean ArrowFunctionWithParenthesis_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunctionWithParenthesis_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ArrowParam(b, l + 1);
    r = r && ArrowFunctionWithParenthesis_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ArrowParam)*
  private static boolean ArrowFunctionWithParenthesis_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunctionWithParenthesis_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ArrowFunctionWithParenthesis_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ArrowFunctionWithParenthesis_1_0_1", c)) break;
    }
    return true;
  }

  // COMMA ArrowParam
  private static boolean ArrowFunctionWithParenthesis_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunctionWithParenthesis_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ArrowParam(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ArrowParam EQUAL_GT Expression
  static boolean ArrowFunctionWithoutParenthesis(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunctionWithoutParenthesis")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = ArrowParam(b, l + 1);
    r = r && consumeToken(b, EQUAL_GT);
    p = r; // pin = 2
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // identifier
  public static boolean ArrowParam(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowParam")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, ARROW_PARAM, r);
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
  // identifier
  public static boolean AttachedObject(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AttachedObject")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, ATTACHED_OBJECT, r);
    return r;
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
  // LEFT_BRACE (RIGHT_BRACE | Statement* WorkerWithStatementsBlock+ RIGHT_BRACE | Statement+ RIGHT_BRACE )
  public static boolean CallableUnitBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitBody")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CALLABLE_UNIT_BODY, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && CallableUnitBody_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // RIGHT_BRACE | Statement* WorkerWithStatementsBlock+ RIGHT_BRACE | Statement+ RIGHT_BRACE
  private static boolean CallableUnitBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitBody_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RIGHT_BRACE);
    if (!r) r = CallableUnitBody_1_1(b, l + 1);
    if (!r) r = CallableUnitBody_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // Statement* WorkerWithStatementsBlock+ RIGHT_BRACE
  private static boolean CallableUnitBody_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitBody_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = CallableUnitBody_1_1_0(b, l + 1);
    r = r && CallableUnitBody_1_1_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // Statement*
  private static boolean CallableUnitBody_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitBody_1_1_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!Statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CallableUnitBody_1_1_0", c)) break;
    }
    return true;
  }

  // WorkerWithStatementsBlock+
  private static boolean CallableUnitBody_1_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitBody_1_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = WorkerWithStatementsBlock(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!WorkerWithStatementsBlock(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CallableUnitBody_1_1_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // Statement+ RIGHT_BRACE
  private static boolean CallableUnitBody_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitBody_1_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = CallableUnitBody_1_2_0(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // Statement+
  private static boolean CallableUnitBody_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitBody_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Statement(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!Statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CallableUnitBody_1_2_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // AnyIdentifierName LEFT_PARENTHESIS FormalParameterList? RIGHT_PARENTHESIS ReturnParameter?
  public static boolean CallableUnitSignature(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitSignature")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CALLABLE_UNIT_SIGNATURE, "<callable unit signature>");
    r = AnyIdentifierName(b, l + 1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 2
    r = r && report_error_(b, CallableUnitSignature_2(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RIGHT_PARENTHESIS)) && r;
    r = p && CallableUnitSignature_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // FormalParameterList?
  private static boolean CallableUnitSignature_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitSignature_2")) return false;
    FormalParameterList(b, l + 1);
    return true;
  }

  // ReturnParameter?
  private static boolean CallableUnitSignature_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CallableUnitSignature_4")) return false;
    ReturnParameter(b, l + 1);
    return true;
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
  // constWithoutType | constWithType
  public static boolean ConstantDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstantDefinition")) return false;
    if (!nextTokenIs(b, "<constant definition>", CONST, PUBLIC)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONSTANT_DEFINITION, "<constant definition>");
    r = constWithoutType(b, l + 1);
    if (!r) r = constWithType(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // SimpleLiteral | RecordLiteral
  public static boolean ConstantExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstantExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONSTANT_EXPRESSION, "<constant expression>");
    r = SimpleLiteral(b, l + 1);
    if (!r) r = RecordLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
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
  public static boolean DualAttachPointIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "DualAttachPointIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DUAL_ATTACH_POINT_IDENT, "<dual attach point ident>");
    r = DualAttachPointIdent_0(b, l + 1);
    if (!r) r = DualAttachPointIdent_1(b, l + 1);
    if (!r) r = consumeToken(b, TYPE_PARAMETER);
    if (!r) r = consumeToken(b, RETURN);
    if (!r) r = consumeToken(b, SERVICE);
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
  // error LEFT_PARENTHESIS identifier (COMMA ErrorDetailBindingPattern)* (COMMA ErrorRestBindingPattern)? RIGHT_PARENTHESIS
  public static boolean ErrorBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorBindingPattern")) return false;
    if (!nextTokenIs(b, ERROR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ERROR_BINDING_PATTERN, null);
    r = consumeTokens(b, 1, ERROR, LEFT_PARENTHESIS, IDENTIFIER);
    p = r; // pin = 1
    r = r && report_error_(b, ErrorBindingPattern_3(b, l + 1));
    r = p && report_error_(b, ErrorBindingPattern_4(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_PARENTHESIS) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA ErrorDetailBindingPattern)*
  private static boolean ErrorBindingPattern_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorBindingPattern_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ErrorBindingPattern_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ErrorBindingPattern_3", c)) break;
    }
    return true;
  }

  // COMMA ErrorDetailBindingPattern
  private static boolean ErrorBindingPattern_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorBindingPattern_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ErrorDetailBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ErrorRestBindingPattern)?
  private static boolean ErrorBindingPattern_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorBindingPattern_4")) return false;
    ErrorBindingPattern_4_0(b, l + 1);
    return true;
  }

  // COMMA ErrorRestBindingPattern
  private static boolean ErrorBindingPattern_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorBindingPattern_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ErrorRestBindingPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ErrorRefBindingPattern ASSIGN Expression SEMICOLON
  public static boolean ErrorDestructuringStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorDestructuringStatement")) return false;
    if (!nextTokenIs(b, ERROR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ErrorRefBindingPattern(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, ERROR_DESTRUCTURING_STATEMENT, r);
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
  // TYPE_ERROR LEFT_PARENTHESIS ErrorArgListMatchPattern RIGHT_PARENTHESIS | TypeName LEFT_PARENTHESIS ErrorFieldMatchPatterns RIGHT_PARENTHESIS
  public static boolean ErrorMatchPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorMatchPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ERROR_MATCH_PATTERN, "<error match pattern>");
    r = ErrorMatchPattern_0(b, l + 1);
    if (!r) r = ErrorMatchPattern_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // TYPE_ERROR LEFT_PARENTHESIS ErrorArgListMatchPattern RIGHT_PARENTHESIS
  private static boolean ErrorMatchPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorMatchPattern_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, TYPE_ERROR, LEFT_PARENTHESIS);
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
  // ErrorMatchPattern (if Expression)? EQUAL_GT (Statement | (LEFT_BRACE Statement* RIGHT_BRACE))
  public static boolean ErrorMatchPatternClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorMatchPatternClause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ERROR_MATCH_PATTERN_CLAUSE, "<error match pattern clause>");
    r = ErrorMatchPattern(b, l + 1);
    r = r && ErrorMatchPatternClause_1(b, l + 1);
    r = r && consumeToken(b, EQUAL_GT);
    r = r && ErrorMatchPatternClause_3(b, l + 1);
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

  // Statement | (LEFT_BRACE Statement* RIGHT_BRACE)
  private static boolean ErrorMatchPatternClause_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorMatchPatternClause_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Statement(b, l + 1);
    if (!r) r = ErrorMatchPatternClause_3_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_BRACE Statement* RIGHT_BRACE
  private static boolean ErrorMatchPatternClause_3_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorMatchPatternClause_3_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && ErrorMatchPatternClause_3_1_1(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // Statement*
  private static boolean ErrorMatchPatternClause_3_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorMatchPatternClause_3_1_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!Statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ErrorMatchPatternClause_3_1_1", c)) break;
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
  // error LEFT_PARENTHESIS ((VariableReference (COMMA ErrorNamedArgRefPattern)*) | ErrorNamedArgRefPattern+) (COMMA ErrorRefRestPattern)? RIGHT_PARENTHESIS
  public static boolean ErrorRefBindingPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern")) return false;
    if (!nextTokenIs(b, ERROR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ERROR, LEFT_PARENTHESIS);
    r = r && ErrorRefBindingPattern_2(b, l + 1);
    r = r && ErrorRefBindingPattern_3(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, ERROR_REF_BINDING_PATTERN, r);
    return r;
  }

  // (VariableReference (COMMA ErrorNamedArgRefPattern)*) | ErrorNamedArgRefPattern+
  private static boolean ErrorRefBindingPattern_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ErrorRefBindingPattern_2_0(b, l + 1);
    if (!r) r = ErrorRefBindingPattern_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // VariableReference (COMMA ErrorNamedArgRefPattern)*
  private static boolean ErrorRefBindingPattern_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = VariableReference(b, l + 1, -1);
    r = r && ErrorRefBindingPattern_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ErrorNamedArgRefPattern)*
  private static boolean ErrorRefBindingPattern_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_2_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ErrorRefBindingPattern_2_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ErrorRefBindingPattern_2_0_1", c)) break;
    }
    return true;
  }

  // COMMA ErrorNamedArgRefPattern
  private static boolean ErrorRefBindingPattern_2_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_2_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ErrorNamedArgRefPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ErrorNamedArgRefPattern+
  private static boolean ErrorRefBindingPattern_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ErrorNamedArgRefPattern(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!ErrorNamedArgRefPattern(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ErrorRefBindingPattern_2_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA ErrorRefRestPattern)?
  private static boolean ErrorRefBindingPattern_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_3")) return false;
    ErrorRefBindingPattern_3_0(b, l + 1);
    return true;
  }

  // COMMA ErrorRefRestPattern
  private static boolean ErrorRefBindingPattern_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ErrorRefBindingPattern_3_0")) return false;
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
    Marker m = enter_section_(b);
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
    exit_section_(b, m, null, r);
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
  // (DOT | OPTIONAL_FIELD_ACCESS) (identifier | MUL)
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

  // identifier | MUL
  private static boolean Field_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Field_1")) return false;
    boolean r;
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, MUL);
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
  // AnnotationAttachment* TypeName identifier QUESTION_MARK? (ASSIGN Expression)? SEMICOLON
  public static boolean FieldDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD_DEFINITION, "<field definition>");
    r = FieldDefinition_0(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, IDENTIFIER);
    p = r; // pin = 3
    r = r && report_error_(b, FieldDefinition_3(b, l + 1));
    r = p && report_error_(b, FieldDefinition_4(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AnnotationAttachment*
  private static boolean FieldDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "FieldDefinition_0", c)) break;
    }
    return true;
  }

  // QUESTION_MARK?
  private static boolean FieldDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition_3")) return false;
    consumeToken(b, QUESTION_MARK);
    return true;
  }

  // (ASSIGN Expression)?
  private static boolean FieldDefinition_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition_4")) return false;
    FieldDefinition_4_0(b, l + 1);
    return true;
  }

  // ASSIGN Expression
  private static boolean FieldDefinition_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FieldDefinition_4_0")) return false;
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
  // forever (LEFT_BRACE ForeverStatementBody RIGHT_BRACE)
  public static boolean ForeverStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeverStatement")) return false;
    if (!nextTokenIs(b, FOREVER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FOREVER_STATEMENT, null);
    r = consumeToken(b, FOREVER);
    p = r; // pin = 1
    r = r && ForeverStatement_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACE ForeverStatementBody RIGHT_BRACE
  private static boolean ForeverStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeverStatement_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, ForeverStatementBody(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // StreamingQueryStatement+
  public static boolean ForeverStatementBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForeverStatementBody")) return false;
    if (!nextTokenIs(b, FROM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = StreamingQueryStatement(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!StreamingQueryStatement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ForeverStatementBody", c)) break;
    }
    exit_section_(b, m, FOREVER_STATEMENT_BODY, r);
    return r;
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
  // (public|private)? remote? function ((AttachedObject|TypeName) DOT)? CallableUnitSignature (CallableUnitBody | ExternalFunctionBody SEMICOLON)
  public static boolean FunctionDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_DEFINITION, "<function definition>");
    r = FunctionDefinition_0(b, l + 1);
    r = r && FunctionDefinition_1(b, l + 1);
    r = r && consumeToken(b, FUNCTION);
    p = r; // pin = 3
    r = r && report_error_(b, FunctionDefinition_3(b, l + 1));
    r = p && report_error_(b, CallableUnitSignature(b, l + 1)) && r;
    r = p && FunctionDefinition_5(b, l + 1) && r;
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

  // ((AttachedObject|TypeName) DOT)?
  private static boolean FunctionDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_3")) return false;
    FunctionDefinition_3_0(b, l + 1);
    return true;
  }

  // (AttachedObject|TypeName) DOT
  private static boolean FunctionDefinition_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = FunctionDefinition_3_0_0(b, l + 1);
    r = r && consumeToken(b, DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  // AttachedObject|TypeName
  private static boolean FunctionDefinition_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_3_0_0")) return false;
    boolean r;
    r = AttachedObject(b, l + 1);
    if (!r) r = TypeName(b, l + 1, -1);
    return r;
  }

  // CallableUnitBody | ExternalFunctionBody SEMICOLON
  private static boolean FunctionDefinition_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = CallableUnitBody(b, l + 1);
    if (!r) r = FunctionDefinition_5_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ExternalFunctionBody SEMICOLON
  private static boolean FunctionDefinition_5_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDefinition_5_1")) return false;
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
  //                             | channelDefinition
  //                             | typedVariableDefinition
  public static boolean GlobalVariableDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GlobalVariableDefinition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, GLOBAL_VARIABLE_DEFINITION, "<global variable definition>");
    r = varDefinition(b, l + 1);
    if (!r) r = listenerDefinition(b, l + 1);
    if (!r) r = channelDefinition(b, l + 1);
    if (!r) r = typedVariableDefinition(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // group by VariableReferenceList
  public static boolean GroupByClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GroupByClause")) return false;
    if (!nextTokenIs(b, GROUP)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GROUP_BY_CLAUSE, null);
    r = consumeTokens(b, 1, GROUP, BY);
    p = r; // pin = 1
    r = r && VariableReferenceList(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // having Expression
  public static boolean HavingClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "HavingClause")) return false;
    if (!nextTokenIs(b, HAVING)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, HAVING_CLAUSE, null);
    r = consumeToken(b, HAVING);
    p = r; // pin = 1
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // openRange | closedRange
  public static boolean IntRangeExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IntRangeExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INT_RANGE_EXPRESSION, "<int range expression>");
    r = openRange(b, l + 1);
    if (!r) r = closedRange(b, l + 1);
    exit_section_(b, l, m, r, false, null);
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
  // (DOT | NOT) AnyIdentifierName LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS
  public static boolean Invocation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Invocation")) return false;
    if (!nextTokenIs(b, "<invocation>", DOT, NOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INVOCATION, "<invocation>");
    r = Invocation_0(b, l + 1);
    r = r && AnyIdentifierName(b, l + 1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    r = r && Invocation_3(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // DOT | NOT
  private static boolean Invocation_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Invocation_0")) return false;
    boolean r;
    r = consumeToken(b, DOT);
    if (!r) r = consumeToken(b, NOT);
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
  // (unidirectional JoinType | JoinType unidirectional | JoinType) StreamingInput (on Expression)?
  public static boolean JoinStreamingInput(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinStreamingInput")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, JOIN_STREAMING_INPUT, "<join streaming input>");
    r = JoinStreamingInput_0(b, l + 1);
    r = r && StreamingInput(b, l + 1);
    r = r && JoinStreamingInput_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // unidirectional JoinType | JoinType unidirectional | JoinType
  private static boolean JoinStreamingInput_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinStreamingInput_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = JoinStreamingInput_0_0(b, l + 1);
    if (!r) r = JoinStreamingInput_0_1(b, l + 1);
    if (!r) r = JoinType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // unidirectional JoinType
  private static boolean JoinStreamingInput_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinStreamingInput_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, UNIDIRECTIONAL);
    r = r && JoinType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // JoinType unidirectional
  private static boolean JoinStreamingInput_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinStreamingInput_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = JoinType(b, l + 1);
    r = r && consumeToken(b, UNIDIRECTIONAL);
    exit_section_(b, m, null, r);
    return r;
  }

  // (on Expression)?
  private static boolean JoinStreamingInput_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinStreamingInput_2")) return false;
    JoinStreamingInput_2_0(b, l + 1);
    return true;
  }

  // on Expression
  private static boolean JoinStreamingInput_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinStreamingInput_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ON);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // left outer join | right outer join | full outer join | outer join | inner? join
  public static boolean JoinType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, JOIN_TYPE, "<join type>");
    r = parseTokens(b, 0, LEFT, OUTER, JOIN);
    if (!r) r = parseTokens(b, 0, RIGHT, OUTER, JOIN);
    if (!r) r = parseTokens(b, 0, FULL, OUTER, JOIN);
    if (!r) r = parseTokens(b, 0, OUTER, JOIN);
    if (!r) r = JoinType_4(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // inner? join
  private static boolean JoinType_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinType_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = JoinType_4_0(b, l + 1);
    r = r && consumeToken(b, JOIN);
    exit_section_(b, m, null, r);
    return r;
  }

  // inner?
  private static boolean JoinType_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "JoinType_4_0")) return false;
    consumeToken(b, INNER);
    return true;
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
  // function LEFT_PARENTHESIS FormalParameterList? RIGHT_PARENTHESIS (returns LambdaReturnParameter)? CallableUnitBody
  public static boolean LambdaFunction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaFunction")) return false;
    if (!nextTokenIs(b, FUNCTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LAMBDA_FUNCTION, null);
    r = consumeTokens(b, 1, FUNCTION, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, LambdaFunction_2(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RIGHT_PARENTHESIS)) && r;
    r = p && report_error_(b, LambdaFunction_4(b, l + 1)) && r;
    r = p && CallableUnitBody(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // FormalParameterList?
  private static boolean LambdaFunction_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaFunction_2")) return false;
    FormalParameterList(b, l + 1);
    return true;
  }

  // (returns LambdaReturnParameter)?
  private static boolean LambdaFunction_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaFunction_4")) return false;
    LambdaFunction_4_0(b, l + 1);
    return true;
  }

  // returns LambdaReturnParameter
  private static boolean LambdaFunction_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaFunction_4_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, RETURNS);
    p = r; // pin = 1
    r = r && LambdaReturnParameter(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // AnnotationAttachment* TypeName
  public static boolean LambdaReturnParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaReturnParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LAMBDA_RETURN_PARAMETER, "<lambda return parameter>");
    r = LambdaReturnParameter_0(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AnnotationAttachment*
  private static boolean LambdaReturnParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaReturnParameter_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "LambdaReturnParameter_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // limit DECIMAL_INTEGER_LITERAL
  public static boolean LimitClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LimitClause")) return false;
    if (!nextTokenIs(b, LIMIT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LIMIT_CLAUSE, null);
    r = consumeTokens(b, 1, LIMIT, DECIMAL_INTEGER_LITERAL);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // (ObjectFieldDefinition | ObjectFunctionDefinition | TypeReference)*
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

  // ObjectFieldDefinition | ObjectFunctionDefinition | TypeReference
  private static boolean ObjectBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectBody_0")) return false;
    boolean r;
    r = ObjectFieldDefinition(b, l + 1);
    if (!r) r = ObjectFunctionDefinition(b, l + 1);
    if (!r) r = TypeReference(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // AnnotationAttachment* (public | private)? TypeName identifier (ASSIGN Expression)? (COMMA | SEMICOLON)
  public static boolean ObjectFieldDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_FIELD_DEFINITION, "<object field definition>");
    r = ObjectFieldDefinition_0(b, l + 1);
    r = r && ObjectFieldDefinition_1(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeToken(b, IDENTIFIER);
    p = r; // pin = 4
    r = r && report_error_(b, ObjectFieldDefinition_4(b, l + 1));
    r = p && ObjectFieldDefinition_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AnnotationAttachment*
  private static boolean ObjectFieldDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectFieldDefinition_0", c)) break;
    }
    return true;
  }

  // (public | private)?
  private static boolean ObjectFieldDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_1")) return false;
    ObjectFieldDefinition_1_0(b, l + 1);
    return true;
  }

  // public | private
  private static boolean ObjectFieldDefinition_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_1_0")) return false;
    boolean r;
    r = consumeToken(b, PUBLIC);
    if (!r) r = consumeToken(b, PRIVATE);
    return r;
  }

  // (ASSIGN Expression)?
  private static boolean ObjectFieldDefinition_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_4")) return false;
    ObjectFieldDefinition_4_0(b, l + 1);
    return true;
  }

  // ASSIGN Expression
  private static boolean ObjectFieldDefinition_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA | SEMICOLON
  private static boolean ObjectFieldDefinition_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFieldDefinition_5")) return false;
    boolean r;
    r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, SEMICOLON);
    return r;
  }

  /* ********************************************************** */
  // documentationString? AnnotationAttachment* (public|private)? (remote|resource)? function CallableUnitSignature (CallableUnitBody | ExternalFunctionBody? SEMICOLON)
  public static boolean ObjectFunctionDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_FUNCTION_DEFINITION, "<object function definition>");
    r = ObjectFunctionDefinition_0(b, l + 1);
    r = r && ObjectFunctionDefinition_1(b, l + 1);
    r = r && ObjectFunctionDefinition_2(b, l + 1);
    r = r && ObjectFunctionDefinition_3(b, l + 1);
    r = r && consumeToken(b, FUNCTION);
    p = r; // pin = 5
    r = r && report_error_(b, CallableUnitSignature(b, l + 1));
    r = p && ObjectFunctionDefinition_6(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // documentationString?
  private static boolean ObjectFunctionDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_0")) return false;
    documentationString(b, l + 1);
    return true;
  }

  // AnnotationAttachment*
  private static boolean ObjectFunctionDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ObjectFunctionDefinition_1", c)) break;
    }
    return true;
  }

  // (public|private)?
  private static boolean ObjectFunctionDefinition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_2")) return false;
    ObjectFunctionDefinition_2_0(b, l + 1);
    return true;
  }

  // public|private
  private static boolean ObjectFunctionDefinition_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_2_0")) return false;
    boolean r;
    r = consumeToken(b, PUBLIC);
    if (!r) r = consumeToken(b, PRIVATE);
    return r;
  }

  // (remote|resource)?
  private static boolean ObjectFunctionDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_3")) return false;
    ObjectFunctionDefinition_3_0(b, l + 1);
    return true;
  }

  // remote|resource
  private static boolean ObjectFunctionDefinition_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_3_0")) return false;
    boolean r;
    r = consumeToken(b, REMOTE);
    if (!r) r = consumeToken(b, RESOURCE);
    return r;
  }

  // CallableUnitBody | ExternalFunctionBody? SEMICOLON
  private static boolean ObjectFunctionDefinition_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = CallableUnitBody(b, l + 1);
    if (!r) r = ObjectFunctionDefinition_6_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ExternalFunctionBody? SEMICOLON
  private static boolean ObjectFunctionDefinition_6_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_6_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ObjectFunctionDefinition_6_1_0(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // ExternalFunctionBody?
  private static boolean ObjectFunctionDefinition_6_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectFunctionDefinition_6_1_0")) return false;
    ExternalFunctionBody(b, l + 1);
    return true;
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
  // order by OrderByVariable (COMMA OrderByVariable)*
  public static boolean OrderByClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OrderByClause")) return false;
    if (!nextTokenIs(b, ORDER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ORDER_BY_CLAUSE, null);
    r = consumeTokens(b, 1, ORDER, BY);
    p = r; // pin = 1
    r = r && report_error_(b, OrderByVariable(b, l + 1));
    r = p && OrderByClause_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA OrderByVariable)*
  private static boolean OrderByClause_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OrderByClause_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!OrderByClause_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "OrderByClause_3", c)) break;
    }
    return true;
  }

  // COMMA OrderByVariable
  private static boolean OrderByClause_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OrderByClause_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && OrderByVariable(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ascending | descending
  public static boolean OrderByType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OrderByType")) return false;
    if (!nextTokenIs(b, "<order by type>", ASCENDING, DESCENDING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ORDER_BY_TYPE, "<order by type>");
    r = consumeToken(b, ASCENDING);
    if (!r) r = consumeToken(b, DESCENDING);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // VariableReference OrderByType?
  public static boolean OrderByVariable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OrderByVariable")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ORDER_BY_VARIABLE, "<order by variable>");
    r = VariableReference(b, l + 1, -1);
    r = r && OrderByVariable_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // OrderByType?
  private static boolean OrderByVariable_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OrderByVariable_1")) return false;
    OrderByType(b, l + 1);
    return true;
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
  // output ((all | last | first) every DECIMAL_INTEGER_LITERAL (TimeScale | events)
  //     | snapshot every DECIMAL_INTEGER_LITERAL TimeScale)
  public static boolean OutputRateLimit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OutputRateLimit")) return false;
    if (!nextTokenIs(b, OUTPUT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OUTPUT_RATE_LIMIT, null);
    r = consumeToken(b, OUTPUT);
    p = r; // pin = 1
    r = r && OutputRateLimit_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (all | last | first) every DECIMAL_INTEGER_LITERAL (TimeScale | events)
  //     | snapshot every DECIMAL_INTEGER_LITERAL TimeScale
  private static boolean OutputRateLimit_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OutputRateLimit_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = OutputRateLimit_1_0(b, l + 1);
    if (!r) r = OutputRateLimit_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (all | last | first) every DECIMAL_INTEGER_LITERAL (TimeScale | events)
  private static boolean OutputRateLimit_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OutputRateLimit_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = OutputRateLimit_1_0_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, consumeTokens(b, -1, EVERY, DECIMAL_INTEGER_LITERAL));
    r = p && OutputRateLimit_1_0_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // all | last | first
  private static boolean OutputRateLimit_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OutputRateLimit_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, ALL);
    if (!r) r = consumeToken(b, LAST);
    if (!r) r = consumeToken(b, FIRST);
    return r;
  }

  // TimeScale | events
  private static boolean OutputRateLimit_1_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OutputRateLimit_1_0_3")) return false;
    boolean r;
    r = TimeScale(b, l + 1);
    if (!r) r = consumeToken(b, EVENTS);
    return r;
  }

  // snapshot every DECIMAL_INTEGER_LITERAL TimeScale
  private static boolean OutputRateLimit_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OutputRateLimit_1_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokens(b, 1, SNAPSHOT, EVERY, DECIMAL_INTEGER_LITERAL);
    p = r; // pin = 1
    r = r && TimeScale(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // version identifier
  public static boolean PackageVersion(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PackageVersion")) return false;
    if (!nextTokenIs(b, VERSION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PACKAGE_VERSION, null);
    r = consumeTokens(b, 1, VERSION, IDENTIFIER);
    p = r; // pin = 1
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
  // Parameter (COMMA Parameter)*
  public static boolean ParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterList")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_LIST, "<parameter list>");
    r = Parameter(b, l + 1);
    p = r; // pin = 1
    r = r && ParameterList_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA Parameter)*
  private static boolean ParameterList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ParameterList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ParameterList_1", c)) break;
    }
    return true;
  }

  // COMMA Parameter
  private static boolean ParameterList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterList_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && Parameter(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // parameterTypeName (COMMA parameterTypeName)*
  public static boolean ParameterTypeNameList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterTypeNameList")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_TYPE_NAME_LIST, "<parameter type name list>");
    r = parameterTypeName(b, l + 1);
    p = r; // pin = 1
    r = r && ParameterTypeNameList_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA parameterTypeName)*
  private static boolean ParameterTypeNameList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterTypeNameList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ParameterTypeNameList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ParameterTypeNameList_1", c)) break;
    }
    return true;
  }

  // COMMA parameterTypeName
  private static boolean ParameterTypeNameList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParameterTypeNameList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameterTypeName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PatternStreamingEdgeInput (followed by | COMMA) PatternStreamingInput
  static boolean Pattern1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = PatternStreamingEdgeInput(b, l + 1);
    r = r && Pattern1_1(b, l + 1);
    p = r; // pin = 2
    r = r && PatternStreamingInput(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // followed by | COMMA
  private static boolean Pattern1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokens(b, 0, FOLLOWED, BY);
    if (!r) r = consumeToken(b, COMMA);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_PARENTHESIS PatternStreamingInput RIGHT_PARENTHESIS
  static boolean Pattern2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern2")) return false;
    if (!nextTokenIs(b, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_PARENTHESIS);
    r = r && PatternStreamingInput(b, l + 1);
    r = r && consumeToken(b, RIGHT_PARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // foreach PatternStreamingInput
  static boolean Pattern3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern3")) return false;
    if (!nextTokenIs(b, FOREACH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, FOREACH);
    p = r; // pin = 1
    r = r && PatternStreamingInput(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // NOT PatternStreamingEdgeInput (AND PatternStreamingEdgeInput | for DECIMAL_INTEGER_LITERAL TimeScale)
  static boolean Pattern4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern4")) return false;
    if (!nextTokenIs(b, NOT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, NOT);
    p = r; // pin = 1
    r = r && report_error_(b, PatternStreamingEdgeInput(b, l + 1));
    r = p && Pattern4_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AND PatternStreamingEdgeInput | for DECIMAL_INTEGER_LITERAL TimeScale
  private static boolean Pattern4_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern4_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Pattern4_2_0(b, l + 1);
    if (!r) r = Pattern4_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // AND PatternStreamingEdgeInput
  private static boolean Pattern4_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern4_2_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, AND);
    p = r; // pin = 1
    r = r && PatternStreamingEdgeInput(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // for DECIMAL_INTEGER_LITERAL TimeScale
  private static boolean Pattern4_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern4_2_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokens(b, 1, FOR, DECIMAL_INTEGER_LITERAL);
    p = r; // pin = 1
    r = r && TimeScale(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // PatternStreamingEdgeInput (AND | OR) PatternStreamingEdgeInput
  static boolean Pattern5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern5")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = PatternStreamingEdgeInput(b, l + 1);
    r = r && Pattern5_1(b, l + 1);
    p = r; // pin = 2
    r = r && PatternStreamingEdgeInput(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // AND | OR
  private static boolean Pattern5_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Pattern5_1")) return false;
    boolean r;
    r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, OR);
    return r;
  }

  /* ********************************************************** */
  // every? PatternStreamingInput WithinClause?
  public static boolean PatternClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternClause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PATTERN_CLAUSE, "<pattern clause>");
    r = PatternClause_0(b, l + 1);
    r = r && PatternStreamingInput(b, l + 1);
    r = r && PatternClause_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // every?
  private static boolean PatternClause_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternClause_0")) return false;
    consumeToken(b, EVERY);
    return true;
  }

  // WithinClause?
  private static boolean PatternClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternClause_2")) return false;
    WithinClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // VariableReference WhereClause? IntRangeExpression? (as identifier)?
  public static boolean PatternStreamingEdgeInput(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternStreamingEdgeInput")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PATTERN_STREAMING_EDGE_INPUT, "<pattern streaming edge input>");
    r = VariableReference(b, l + 1, -1);
    p = r; // pin = 1
    r = r && report_error_(b, PatternStreamingEdgeInput_1(b, l + 1));
    r = p && report_error_(b, PatternStreamingEdgeInput_2(b, l + 1)) && r;
    r = p && PatternStreamingEdgeInput_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // WhereClause?
  private static boolean PatternStreamingEdgeInput_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternStreamingEdgeInput_1")) return false;
    WhereClause(b, l + 1);
    return true;
  }

  // IntRangeExpression?
  private static boolean PatternStreamingEdgeInput_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternStreamingEdgeInput_2")) return false;
    IntRangeExpression(b, l + 1);
    return true;
  }

  // (as identifier)?
  private static boolean PatternStreamingEdgeInput_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternStreamingEdgeInput_3")) return false;
    PatternStreamingEdgeInput_3_0(b, l + 1);
    return true;
  }

  // as identifier
  private static boolean PatternStreamingEdgeInput_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternStreamingEdgeInput_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, AS, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Pattern4
  //     |   Pattern5
  //     |   Pattern1
  //     |   Pattern2
  //     |   Pattern3
  //     |   PatternStreamingEdgeInput
  public static boolean PatternStreamingInput(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PatternStreamingInput")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PATTERN_STREAMING_INPUT, "<pattern streaming input>");
    r = Pattern4(b, l + 1);
    if (!r) r = Pattern5(b, l + 1);
    if (!r) r = Pattern1(b, l + 1);
    if (!r) r = Pattern2(b, l + 1);
    if (!r) r = Pattern3(b, l + 1);
    if (!r) r = PatternStreamingEdgeInput(b, l + 1);
    exit_section_(b, l, m, r, false, null);
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
  public static boolean RecordKeyValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordKeyValue")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_KEY_VALUE, "<record key value>");
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
  // RecordKeyValue (COMMA RecordKeyValue)*
  public static boolean RecordLiteralBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteralBody")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_LITERAL_BODY, "<record literal body>");
    r = RecordKeyValue(b, l + 1);
    p = r; // pin = 1
    r = r && RecordLiteralBody_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA RecordKeyValue)*
  private static boolean RecordLiteralBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteralBody_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!RecordLiteralBody_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "RecordLiteralBody_1", c)) break;
    }
    return true;
  }

  // COMMA RecordKeyValue
  private static boolean RecordLiteralBody_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordLiteralBody_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && RecordKeyValue(b, l + 1);
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
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, REST_PARAMETER, "<rest parameter>");
    r = RestParameter_0(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeTokens(b, 1, ELLIPSIS, IDENTIFIER);
    p = r; // pin = 3
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // select (MUL| SelectExpressionList) GroupByClause? HavingClause?
  public static boolean SelectClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectClause")) return false;
    if (!nextTokenIs(b, SELECT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SELECT_CLAUSE, null);
    r = consumeToken(b, SELECT);
    p = r; // pin = 1
    r = r && report_error_(b, SelectClause_1(b, l + 1));
    r = p && report_error_(b, SelectClause_2(b, l + 1)) && r;
    r = p && SelectClause_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // MUL| SelectExpressionList
  private static boolean SelectClause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectClause_1")) return false;
    boolean r;
    r = consumeToken(b, MUL);
    if (!r) r = SelectExpressionList(b, l + 1);
    return r;
  }

  // GroupByClause?
  private static boolean SelectClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectClause_2")) return false;
    GroupByClause(b, l + 1);
    return true;
  }

  // HavingClause?
  private static boolean SelectClause_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectClause_3")) return false;
    HavingClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // Expression (as identifier)?
  public static boolean SelectExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SELECT_EXPRESSION, "<select expression>");
    r = Expression(b, l + 1, -1);
    r = r && SelectExpression_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (as identifier)?
  private static boolean SelectExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectExpression_1")) return false;
    SelectExpression_1_0(b, l + 1);
    return true;
  }

  // as identifier
  private static boolean SelectExpression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectExpression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, AS, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SelectExpression (COMMA SelectExpression)*
  public static boolean SelectExpressionList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectExpressionList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SELECT_EXPRESSION_LIST, "<select expression list>");
    r = SelectExpression(b, l + 1);
    r = r && SelectExpressionList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA SelectExpression)*
  private static boolean SelectExpressionList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectExpressionList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!SelectExpressionList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "SelectExpressionList_1", c)) break;
    }
    return true;
  }

  // COMMA SelectExpression
  private static boolean SelectExpressionList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SelectExpressionList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && SelectExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACE ServiceBodyMember* RIGHT_BRACE
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

  // ServiceBodyMember*
  private static boolean ServiceBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceBody_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ServiceBodyMember(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ServiceBody_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ObjectFieldDefinition | ObjectFunctionDefinition
  public static boolean ServiceBodyMember(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ServiceBodyMember")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SERVICE_BODY_MEMBER, "<service body member>");
    r = ObjectFieldDefinition(b, l + 1);
    if (!r) r = ObjectFunctionDefinition(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
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
  // annotation | external | var | const | listener
  public static boolean SourceOnlyAttachPointIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SourceOnlyAttachPointIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SOURCE_ONLY_ATTACH_POINT_IDENT, "<source only attach point ident>");
    r = consumeToken(b, ANNOTATION);
    if (!r) r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, LISTENER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // WhileStatement
  //     |   ForeverStatement
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
  //     |   StreamingQueryStatement
  //     |   ErrorDestructuringStatement
  public static boolean Statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT, "<statement>");
    r = WhileStatement(b, l + 1);
    if (!r) r = ForeverStatement(b, l + 1);
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
    if (!r) r = StreamingQueryStatement(b, l + 1);
    if (!r) r = ErrorDestructuringStatement(b, l + 1);
    exit_section_(b, l, m, r, false, StatementRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // !(BOOLEAN_LITERAL|QUOTED_STRING_LITERAL|DECIMAL_INTEGER_LITERAL|HEX_INTEGER_LITERAL|OCTAL_INTEGER_LITERAL|BINARY_INTEGER_LITERAL|NULL_LITERAL|DECIMAL_FLOATING_POINT_NUMBER|HEXADECIMAL_FLOATING_POINT_LITERAL|int|string|float|decimal|boolean|byte|any|anydata|json|xml|xmlns|map|table|function|stream|'{'|'['|'}'|';'|typedesc|future|var|while|match|foreach|continue|break|fork|try|throw|return|abort|aborted|committed|retry|fail|lock|transaction|if|forever|object|service|check|error|panic|from|worker|record|identifier|wait)
  static boolean StatementRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StatementRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !StatementRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // BOOLEAN_LITERAL|QUOTED_STRING_LITERAL|DECIMAL_INTEGER_LITERAL|HEX_INTEGER_LITERAL|OCTAL_INTEGER_LITERAL|BINARY_INTEGER_LITERAL|NULL_LITERAL|DECIMAL_FLOATING_POINT_NUMBER|HEXADECIMAL_FLOATING_POINT_LITERAL|int|string|float|decimal|boolean|byte|any|anydata|json|xml|xmlns|map|table|function|stream|'{'|'['|'}'|';'|typedesc|future|var|while|match|foreach|continue|break|fork|try|throw|return|abort|aborted|committed|retry|fail|lock|transaction|if|forever|object|service|check|error|panic|from|worker|record|identifier|wait
  private static boolean StatementRecover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StatementRecover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
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
    if (!r) r = consumeToken(b, ERROR);
    if (!r) r = consumeToken(b, PANIC);
    if (!r) r = consumeToken(b, FROM);
    if (!r) r = consumeToken(b, WORKER);
    if (!r) r = consumeToken(b, RECORD);
    if (!r) r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, WAIT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // StaticMatchLiteral EQUAL_GT (LEFT_BRACE Block RIGHT_BRACE | Statement)
  public static boolean StaticMatchPatternClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StaticMatchPatternClause")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATIC_MATCH_PATTERN_CLAUSE, "<static match pattern clause>");
    r = StaticMatchLiteral(b, l + 1, -1);
    r = r && consumeToken(b, EQUAL_GT);
    r = r && StaticMatchPatternClause_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LEFT_BRACE Block RIGHT_BRACE | Statement
  private static boolean StaticMatchPatternClause_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StaticMatchPatternClause_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = StaticMatchPatternClause_2_0(b, l + 1);
    if (!r) r = Statement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean StaticMatchPatternClause_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StaticMatchPatternClause_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && Block(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // stream LT TypeName GT
  public static boolean StreamTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamTypeName")) return false;
    if (!nextTokenIs(b, STREAM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, STREAM_TYPE_NAME, null);
    r = consumeTokens(b, 1, STREAM, LT);
    p = r; // pin = 1
    r = r && report_error_(b, TypeName(b, l + 1, -1));
    r = p && consumeToken(b, GT) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // EQUAL_GT LEFT_PARENTHESIS Parameter RIGHT_PARENTHESIS LEFT_BRACE Block RIGHT_BRACE
  public static boolean StreamingAction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingAction")) return false;
    if (!nextTokenIs(b, EQUAL_GT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, STREAMING_ACTION, null);
    r = consumeTokens(b, 1, EQUAL_GT, LEFT_PARENTHESIS);
    p = r; // pin = 1
    r = r && report_error_(b, Parameter(b, l + 1));
    r = p && report_error_(b, consumeTokens(b, -1, RIGHT_PARENTHESIS, LEFT_BRACE)) && r;
    r = p && report_error_(b, Block(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // VariableReference WhereClause? FunctionInvocation* WindowClause? FunctionInvocation* WhereClause? (as identifier)?
  public static boolean StreamingInput(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, STREAMING_INPUT, "<streaming input>");
    r = VariableReference(b, l + 1, -1);
    p = r; // pin = 1
    r = r && report_error_(b, StreamingInput_1(b, l + 1));
    r = p && report_error_(b, StreamingInput_2(b, l + 1)) && r;
    r = p && report_error_(b, StreamingInput_3(b, l + 1)) && r;
    r = p && report_error_(b, StreamingInput_4(b, l + 1)) && r;
    r = p && report_error_(b, StreamingInput_5(b, l + 1)) && r;
    r = p && StreamingInput_6(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // WhereClause?
  private static boolean StreamingInput_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput_1")) return false;
    WhereClause(b, l + 1);
    return true;
  }

  // FunctionInvocation*
  private static boolean StreamingInput_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FunctionInvocation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "StreamingInput_2", c)) break;
    }
    return true;
  }

  // WindowClause?
  private static boolean StreamingInput_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput_3")) return false;
    WindowClause(b, l + 1);
    return true;
  }

  // FunctionInvocation*
  private static boolean StreamingInput_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FunctionInvocation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "StreamingInput_4", c)) break;
    }
    return true;
  }

  // WhereClause?
  private static boolean StreamingInput_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput_5")) return false;
    WhereClause(b, l + 1);
    return true;
  }

  // (as identifier)?
  private static boolean StreamingInput_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput_6")) return false;
    StreamingInput_6_0(b, l + 1);
    return true;
  }

  // as identifier
  private static boolean StreamingInput_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingInput_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, AS, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // from (StreamingInput (JoinStreamingInput)? | PatternClause)
  //         SelectClause?
  //         OrderByClause?
  //         OutputRateLimit?
  //         StreamingAction
  public static boolean StreamingQueryStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement")) return false;
    if (!nextTokenIs(b, FROM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, STREAMING_QUERY_STATEMENT, null);
    r = consumeToken(b, FROM);
    p = r; // pin = 1
    r = r && report_error_(b, StreamingQueryStatement_1(b, l + 1));
    r = p && report_error_(b, StreamingQueryStatement_2(b, l + 1)) && r;
    r = p && report_error_(b, StreamingQueryStatement_3(b, l + 1)) && r;
    r = p && report_error_(b, StreamingQueryStatement_4(b, l + 1)) && r;
    r = p && StreamingAction(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // StreamingInput (JoinStreamingInput)? | PatternClause
  private static boolean StreamingQueryStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = StreamingQueryStatement_1_0(b, l + 1);
    if (!r) r = PatternClause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // StreamingInput (JoinStreamingInput)?
  private static boolean StreamingQueryStatement_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = StreamingInput(b, l + 1);
    r = r && StreamingQueryStatement_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (JoinStreamingInput)?
  private static boolean StreamingQueryStatement_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement_1_0_1")) return false;
    StreamingQueryStatement_1_0_1_0(b, l + 1);
    return true;
  }

  // (JoinStreamingInput)
  private static boolean StreamingQueryStatement_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = JoinStreamingInput(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SelectClause?
  private static boolean StreamingQueryStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement_2")) return false;
    SelectClause(b, l + 1);
    return true;
  }

  // OrderByClause?
  private static boolean StreamingQueryStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement_3")) return false;
    OrderByClause(b, l + 1);
    return true;
  }

  // OutputRateLimit?
  private static boolean StreamingQueryStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StreamingQueryStatement_4")) return false;
    OutputRateLimit(b, l + 1);
    return true;
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
  // STRING_TEMPLATE_EXPRESSION_START Expression RIGHT_BRACE
  static boolean StringTemplateExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringTemplateExpression")) return false;
    if (!nextTokenIs(b, STRING_TEMPLATE_EXPRESSION_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, STRING_TEMPLATE_EXPRESSION_START);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, RIGHT_BRACE);
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
  // from StreamingInput JoinStreamingInput? SelectClause? OrderByClause? LimitClause?
  public static boolean TableQuery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableQuery")) return false;
    if (!nextTokenIs(b, FROM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TABLE_QUERY, null);
    r = consumeToken(b, FROM);
    p = r; // pin = 1
    r = r && report_error_(b, StreamingInput(b, l + 1));
    r = p && report_error_(b, TableQuery_2(b, l + 1)) && r;
    r = p && report_error_(b, TableQuery_3(b, l + 1)) && r;
    r = p && report_error_(b, TableQuery_4(b, l + 1)) && r;
    r = p && TableQuery_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // JoinStreamingInput?
  private static boolean TableQuery_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableQuery_2")) return false;
    JoinStreamingInput(b, l + 1);
    return true;
  }

  // SelectClause?
  private static boolean TableQuery_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableQuery_3")) return false;
    SelectClause(b, l + 1);
    return true;
  }

  // OrderByClause?
  private static boolean TableQuery_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableQuery_4")) return false;
    OrderByClause(b, l + 1);
    return true;
  }

  // LimitClause?
  private static boolean TableQuery_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableQuery_5")) return false;
    LimitClause(b, l + 1);
    return true;
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
  // second | seconds
  //             | minute | minutes
  //             | hour | hours
  //             | day | days
  //             | month | months
  //             | year | years
  public static boolean TimeScale(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TimeScale")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TIME_SCALE, "<time scale>");
    r = consumeToken(b, SECOND);
    if (!r) r = consumeToken(b, SECONDS);
    if (!r) r = consumeToken(b, MINUTE);
    if (!r) r = consumeToken(b, MINUTES);
    if (!r) r = consumeToken(b, HOUR);
    if (!r) r = consumeToken(b, HOURS);
    if (!r) r = consumeToken(b, DAY);
    if (!r) r = consumeToken(b, DAYS);
    if (!r) r = consumeToken(b, MONTH);
    if (!r) r = consumeToken(b, MONTHS);
    if (!r) r = consumeToken(b, YEAR);
    if (!r) r = consumeToken(b, YEARS);
    exit_section_(b, l, m, r, false, null);
    return r;
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
    Marker m = enter_section_(b);
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
    exit_section_(b, m, null, r);
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
    r = consumeTokens(b, 1, TYPEDESC, LT);
    p = r; // pin = 1
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
  // var BindingPattern (if Expression)? EQUAL_GT (LEFT_BRACE Block RIGHT_BRACE | Statement)
  public static boolean VarMatchPatternClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarMatchPatternClause")) return false;
    if (!nextTokenIs(b, VAR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VAR_MATCH_PATTERN_CLAUSE, null);
    r = consumeToken(b, VAR);
    p = r; // pin = 1
    r = r && report_error_(b, BindingPattern(b, l + 1));
    r = p && report_error_(b, VarMatchPatternClause_2(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, EQUAL_GT)) && r;
    r = p && VarMatchPatternClause_4(b, l + 1) && r;
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

  // LEFT_BRACE Block RIGHT_BRACE | Statement
  private static boolean VarMatchPatternClause_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarMatchPatternClause_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = VarMatchPatternClause_4_0(b, l + 1);
    if (!r) r = Statement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LEFT_BRACE Block RIGHT_BRACE
  private static boolean VarMatchPatternClause_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarMatchPatternClause_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LEFT_BRACE);
    r = r && Block(b, l + 1);
    r = r && consumeToken(b, RIGHT_BRACE);
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
  // VariableReference (COMMA VariableReference)*
  public static boolean VariableReferenceList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableReferenceList")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_REFERENCE_LIST, "<variable reference list>");
    r = VariableReference(b, l + 1, -1);
    p = r; // pin = 1
    r = r && VariableReferenceList_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA VariableReference)*
  private static boolean VariableReferenceList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableReferenceList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!VariableReferenceList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "VariableReferenceList_1", c)) break;
    }
    return true;
  }

  // COMMA VariableReference
  private static boolean VariableReferenceList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VariableReferenceList_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && VariableReference(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // window FunctionInvocation
  public static boolean WindowClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WindowClause")) return false;
    if (!nextTokenIs(b, WINDOW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WINDOW_CLAUSE, null);
    r = consumeToken(b, WINDOW);
    p = r; // pin = 1
    r = r && FunctionInvocation(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // within DECIMAL_INTEGER_LITERAL TimeScale
  public static boolean WithinClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WithinClause")) return false;
    if (!nextTokenIs(b, WITHIN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WITHIN_CLAUSE, null);
    r = consumeTokens(b, 1, WITHIN, DECIMAL_INTEGER_LITERAL);
    p = r; // pin = 1
    r = r && TimeScale(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // xml
  public static boolean XmlTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "XmlTypeName")) return false;
    if (!nextTokenIs(b, XML)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, XML);
    exit_section_(b, m, XML_TYPE_NAME, r);
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
  // channelType identifier ASSIGN Expression SEMICOLON
  static boolean channelDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "channelDefinition")) return false;
    if (!nextTokenIs(b, CHANNEL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = channelType(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, ASSIGN);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // channel LT TypeName GT
  public static boolean channelType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "channelType")) return false;
    if (!nextTokenIs(b, CHANNEL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CHANNEL_TYPE, null);
    r = consumeTokens(b, 1, CHANNEL, LT);
    p = r; // pin = 1
    r = r && report_error_(b, TypeName(b, l + 1, -1));
    r = p && consumeToken(b, GT) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (LEFT_BRACKET|LEFT_PARENTHESIS) Expression RANGE Expression (RIGHT_BRACKET|RIGHT_PARENTHESIS)
  static boolean closedRange(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "closedRange")) return false;
    if (!nextTokenIs(b, "", LEFT_BRACKET, LEFT_PARENTHESIS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = closedRange_0(b, l + 1);
    r = r && Expression(b, l + 1, -1);
    r = r && consumeToken(b, RANGE);
    p = r; // pin = 3
    r = r && report_error_(b, Expression(b, l + 1, -1));
    r = p && closedRange_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // LEFT_BRACKET|LEFT_PARENTHESIS
  private static boolean closedRange_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "closedRange_0")) return false;
    boolean r;
    r = consumeToken(b, LEFT_BRACKET);
    if (!r) r = consumeToken(b, LEFT_PARENTHESIS);
    return r;
  }

  // RIGHT_BRACKET|RIGHT_PARENTHESIS
  private static boolean closedRange_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "closedRange_4")) return false;
    boolean r;
    r = consumeToken(b, RIGHT_BRACKET);
    if (!r) r = consumeToken(b, RIGHT_PARENTHESIS);
    return r;
  }

  /* ********************************************************** */
  // PUBLIC? CONST TypeName identifier ASSIGN ConstantExpression SEMICOLON
  static boolean constWithType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constWithType")) return false;
    if (!nextTokenIs(b, "", CONST, PUBLIC)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = constWithType_0(b, l + 1);
    r = r && consumeToken(b, CONST);
    r = r && TypeName(b, l + 1, -1);
    r = r && consumeTokens(b, 0, IDENTIFIER, ASSIGN);
    r = r && ConstantExpression(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // PUBLIC?
  private static boolean constWithType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constWithType_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  /* ********************************************************** */
  // PUBLIC? CONST identifier ASSIGN ConstantExpression SEMICOLON
  static boolean constWithoutType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constWithoutType")) return false;
    if (!nextTokenIs(b, "", CONST, PUBLIC)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = constWithoutType_0(b, l + 1);
    r = r && consumeTokens(b, 0, CONST, IDENTIFIER, ASSIGN);
    r = r && ConstantExpression(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // PUBLIC?
  private static boolean constWithoutType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constWithoutType_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  /* ********************************************************** */
  // DEFINITION_REFERENCE
  public static boolean definitionReferenceType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "definitionReferenceType")) return false;
    if (!nextTokenIs(b, DEFINITION_REFERENCE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DEFINITION_REFERENCE);
    exit_section_(b, m, DEFINITION_REFERENCE_TYPE, r);
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
  // definitionReferenceType singleBacktickedBlock
  public static boolean documentationDefinitionReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationDefinitionReference")) return false;
    if (!nextTokenIs(b, DEFINITION_REFERENCE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = definitionReferenceType(b, l + 1);
    r = r && singleBacktickedBlock(b, l + 1);
    exit_section_(b, m, DOCUMENTATION_DEFINITION_REFERENCE, r);
    return r;
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
  // documentationDefinitionReference
  public static boolean documentationReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationReference")) return false;
    if (!nextTokenIs(b, DEFINITION_REFERENCE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = documentationDefinitionReference(b, l + 1);
    exit_section_(b, m, DOCUMENTATION_REFERENCE, r);
    return r;
  }

  /* ********************************************************** */
  // documentationLine+ parameterDocumentationLine* returnParameterDocumentationLine?
  public static boolean documentationString(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationString")) return false;
    if (!nextTokenIs(b, MARKDOWN_DOCUMENTATION_LINE_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = documentationString_0(b, l + 1);
    r = r && documentationString_1(b, l + 1);
    r = r && documentationString_2(b, l + 1);
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

  /* ********************************************************** */
  // (MARKDOWN_DOCUMENTATION_TEXT | REFERENCE_TYPE | DOCUMENTATION_ESCAPED_CHARACTERS | documentationReference | backtickedBlock)+
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

  // MARKDOWN_DOCUMENTATION_TEXT | REFERENCE_TYPE | DOCUMENTATION_ESCAPED_CHARACTERS | documentationReference | backtickedBlock
  private static boolean documentationText_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentationText_0")) return false;
    boolean r;
    r = consumeToken(b, MARKDOWN_DOCUMENTATION_TEXT);
    if (!r) r = consumeToken(b, REFERENCE_TYPE);
    if (!r) r = consumeToken(b, DOCUMENTATION_ESCAPED_CHARACTERS);
    if (!r) r = documentationReference(b, l + 1);
    if (!r) r = backtickedBlock(b, l + 1);
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
  // new UserDefineTypeName LEFT_PARENTHESIS InvocationArgList? RIGHT_PARENTHESIS
  public static boolean initWithType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initWithType")) return false;
    if (!nextTokenIs(b, NEW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INIT_WITH_TYPE, null);
    r = consumeToken(b, NEW);
    r = r && UserDefineTypeName(b, l + 1);
    r = r && consumeToken(b, LEFT_PARENTHESIS);
    p = r; // pin = 3
    r = r && report_error_(b, initWithType_3(b, l + 1));
    r = p && consumeToken(b, RIGHT_PARENTHESIS) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // public? listener TypeName identifier ASSIGN Expression SEMICOLON
  static boolean listenerDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listenerDefinition")) return false;
    if (!nextTokenIs(b, "", LISTENER, PUBLIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = listenerDefinition_0(b, l + 1);
    r = r && consumeToken(b, LISTENER);
    p = r; // pin = 2
    r = r && report_error_(b, TypeName(b, l + 1, -1));
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

  /* ********************************************************** */
  // Expression RANGE Expression
  static boolean openRange(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "openRange")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = Expression(b, l + 1, -1);
    r = r && consumeToken(b, RANGE);
    p = r; // pin = 2
    r = r && Expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // documentationText?
  public static boolean parameterDescription(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDescription")) return false;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_DESCRIPTION, "<parameter description>");
    documentationText(b, l + 1);
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // PARAMETER_NAME DESCRIPTION_SEPARATOR documentationText?
  public static boolean parameterDocumentation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentation")) return false;
    if (!nextTokenIs(b, PARAMETER_NAME)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_DOCUMENTATION, null);
    r = consumeTokens(b, 2, PARAMETER_NAME, DESCRIPTION_SEPARATOR);
    p = r; // pin = 2
    r = r && parameterDocumentation_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // documentationText?
  private static boolean parameterDocumentation_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentation_2")) return false;
    documentationText(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (PARAMETER_DOCUMENTATION_START parameterDocumentation) (MARKDOWN_DOCUMENTATION_LINE_START parameterDescription)*
  public static boolean parameterDocumentationLine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentationLine")) return false;
    if (!nextTokenIs(b, PARAMETER_DOCUMENTATION_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameterDocumentationLine_0(b, l + 1);
    r = r && parameterDocumentationLine_1(b, l + 1);
    exit_section_(b, m, PARAMETER_DOCUMENTATION_LINE, r);
    return r;
  }

  // PARAMETER_DOCUMENTATION_START parameterDocumentation
  private static boolean parameterDocumentationLine_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentationLine_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PARAMETER_DOCUMENTATION_START);
    r = r && parameterDocumentation(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (MARKDOWN_DOCUMENTATION_LINE_START parameterDescription)*
  private static boolean parameterDocumentationLine_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentationLine_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameterDocumentationLine_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameterDocumentationLine_1", c)) break;
    }
    return true;
  }

  // MARKDOWN_DOCUMENTATION_LINE_START parameterDescription
  private static boolean parameterDocumentationLine_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterDocumentationLine_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MARKDOWN_DOCUMENTATION_LINE_START);
    r = r && parameterDescription(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // AnnotationAttachment* TypeName
  public static boolean parameterTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_TYPE_NAME, "<parameter type name>");
    r = parameterTypeName_0(b, l + 1);
    r = r && TypeName(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AnnotationAttachment*
  private static boolean parameterTypeName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterTypeName_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AnnotationAttachment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameterTypeName_0", c)) break;
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
  // documentationText?
  public static boolean returnParameterDescription(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDescription")) return false;
    Marker m = enter_section_(b, l, _NONE_, RETURN_PARAMETER_DESCRIPTION, "<return parameter description>");
    documentationText(b, l + 1);
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // docParameterDescription
  public static boolean returnParameterDocumentation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDocumentation")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RETURN_PARAMETER_DOCUMENTATION, "<return parameter documentation>");
    r = docParameterDescription(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (RETURN_PARAMETER_DOCUMENTATION_START returnParameterDocumentation) (MARKDOWN_DOCUMENTATION_LINE_START returnParameterDescription)*
  public static boolean returnParameterDocumentationLine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDocumentationLine")) return false;
    if (!nextTokenIs(b, RETURN_PARAMETER_DOCUMENTATION_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = returnParameterDocumentationLine_0(b, l + 1);
    r = r && returnParameterDocumentationLine_1(b, l + 1);
    exit_section_(b, m, RETURN_PARAMETER_DOCUMENTATION_LINE, r);
    return r;
  }

  // RETURN_PARAMETER_DOCUMENTATION_START returnParameterDocumentation
  private static boolean returnParameterDocumentationLine_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDocumentationLine_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RETURN_PARAMETER_DOCUMENTATION_START);
    r = r && returnParameterDocumentation(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (MARKDOWN_DOCUMENTATION_LINE_START returnParameterDescription)*
  private static boolean returnParameterDocumentationLine_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDocumentationLine_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!returnParameterDocumentationLine_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "returnParameterDocumentationLine_1", c)) break;
    }
    return true;
  }

  // MARKDOWN_DOCUMENTATION_LINE_START returnParameterDescription
  private static boolean returnParameterDocumentationLine_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameterDocumentationLine_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MARKDOWN_DOCUMENTATION_LINE_START);
    r = r && returnParameterDescription(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
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
  // Expression root: Expression
  // Operator priority table:
  // 0: ATOM(LambdaFunctionExpression)
  // 1: ATOM(ArrowFunctionExpression)
  // 2: ATOM(SimpleLiteralExpression)
  // 3: ATOM(StringTemplateLiteralExpression)
  // 4: ATOM(XmlLiteralExpression)
  // 5: ATOM(TableLiteralExpression)
  // 6: ATOM(RecordLiteralExpression)
  // 7: PREFIX(GroupExpression)
  // 8: POSTFIX(TernaryExpression)
  // 9: ATOM(ListConstructorExpression)
  // 10: ATOM(ActionInvocationExpression)
  // 11: ATOM(VariableReferenceExpression)
  // 12: ATOM(TypeInitExpression)
  // 13: ATOM(TypeConversionExpression)
  // 14: ATOM(UnaryExpression)
  // 15: BINARY(BinaryDivMulModExpression)
  // 16: BINARY(BinaryAddSubExpression)
  // 17: BINARY(BinaryCompareExpression)
  // 18: BINARY(BinaryEqualExpression)
  // 19: BINARY(BinaryAndExpression)
  // 20: BINARY(BinaryOrExpression)
  // 21: ATOM(TableQueryExpression)
  // 22: ATOM(CheckedExpression)
  // 23: ATOM(CheckPanicExpression)
  // 24: BINARY(ElvisExpression)
  // 25: ATOM(WaitExpression)
  // 26: POSTFIX(WorkerSendSyncExpression)
  // 27: ATOM(WorkerReceiveExpression)
  // 28: ATOM(FlushWorkerExpression)
  // 29: BINARY(IntegerRangeExpression)
  // 30: BINARY(BitwiseExpression)
  // 31: BINARY(BitwiseShiftExpression)
  // 32: ATOM(ServiceConstructorExpression)
  // 33: POSTFIX(TypeTestExpression)
  // 34: BINARY(BinaryRefEqualExpression)
  // 35: ATOM(TrapExpression)
  // 36: ATOM(TypeAccessExpression)
  // 37: POSTFIX(AnnotationActionExpression)
  public static boolean Expression(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "Expression")) return false;
    addVariant(b, "<expression>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<expression>");
    r = LambdaFunctionExpression(b, l + 1);
    if (!r) r = ArrowFunctionExpression(b, l + 1);
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
    if (!r) r = TableQueryExpression(b, l + 1);
    if (!r) r = CheckedExpression(b, l + 1);
    if (!r) r = CheckPanicExpression(b, l + 1);
    if (!r) r = WaitExpression(b, l + 1);
    if (!r) r = WorkerReceiveExpression(b, l + 1);
    if (!r) r = FlushWorkerExpression(b, l + 1);
    if (!r) r = ServiceConstructorExpression(b, l + 1);
    if (!r) r = TrapExpression(b, l + 1);
    if (!r) r = TypeAccessExpression(b, l + 1);
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
      if (g < 8 && TernaryExpression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, TERNARY_EXPRESSION, r, true, null);
      }
      else if (g < 15 && BinaryDivMulModExpression_0(b, l + 1)) {
        r = Expression(b, l, 15);
        exit_section_(b, l, m, BINARY_DIV_MUL_MOD_EXPRESSION, r, true, null);
      }
      else if (g < 16 && BinaryAddSubExpression_0(b, l + 1)) {
        r = Expression(b, l, 16);
        exit_section_(b, l, m, BINARY_ADD_SUB_EXPRESSION, r, true, null);
      }
      else if (g < 17 && BinaryCompareExpression_0(b, l + 1)) {
        r = Expression(b, l, 17);
        exit_section_(b, l, m, BINARY_COMPARE_EXPRESSION, r, true, null);
      }
      else if (g < 18 && BinaryEqualExpression_0(b, l + 1)) {
        r = Expression(b, l, 18);
        exit_section_(b, l, m, BINARY_EQUAL_EXPRESSION, r, true, null);
      }
      else if (g < 19 && BinaryAndExpression_0(b, l + 1)) {
        r = Expression(b, l, 19);
        exit_section_(b, l, m, BINARY_AND_EXPRESSION, r, true, null);
      }
      else if (g < 20 && consumeTokenSmart(b, OR)) {
        r = Expression(b, l, 20);
        exit_section_(b, l, m, BINARY_OR_EXPRESSION, r, true, null);
      }
      else if (g < 24 && consumeTokenSmart(b, ELVIS)) {
        r = Expression(b, l, 24);
        exit_section_(b, l, m, ELVIS_EXPRESSION, r, true, null);
      }
      else if (g < 26 && WorkerSendSyncExpression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, WORKER_SEND_SYNC_EXPRESSION, r, true, null);
      }
      else if (g < 29 && IntegerRangeExpression_0(b, l + 1)) {
        r = Expression(b, l, 29);
        exit_section_(b, l, m, INTEGER_RANGE_EXPRESSION, r, true, null);
      }
      else if (g < 30 && BitwiseExpression_0(b, l + 1)) {
        r = Expression(b, l, 30);
        exit_section_(b, l, m, BITWISE_EXPRESSION, r, true, null);
      }
      else if (g < 31 && BitwiseShiftExpression_0(b, l + 1)) {
        r = Expression(b, l, 31);
        exit_section_(b, l, m, BITWISE_SHIFT_EXPRESSION, r, true, null);
      }
      else if (g < 33 && TypeTestExpression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, TYPE_TEST_EXPRESSION, r, true, null);
      }
      else if (g < 34 && BinaryRefEqualExpression_0(b, l + 1)) {
        r = Expression(b, l, 34);
        exit_section_(b, l, m, BINARY_REF_EQUAL_EXPRESSION, r, true, null);
      }
      else if (g < 37 && AnnotationActionExpression_0(b, l + 1)) {
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

  // LambdaFunction
  public static boolean LambdaFunctionExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LambdaFunctionExpression")) return false;
    if (!nextTokenIsSmart(b, FUNCTION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = LambdaFunction(b, l + 1);
    exit_section_(b, m, LAMBDA_FUNCTION_EXPRESSION, r);
    return r;
  }

  // ArrowFunction
  public static boolean ArrowFunctionExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrowFunctionExpression")) return false;
    if (!nextTokenIsSmart(b, IDENTIFIER, LEFT_PARENTHESIS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARROW_FUNCTION_EXPRESSION, "<arrow function expression>");
    r = ArrowFunction(b, l + 1);
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
    r = p && Expression(b, l, 7);
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

  // TableQuery
  public static boolean TableQueryExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TableQueryExpression")) return false;
    if (!nextTokenIsSmart(b, FROM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TableQuery(b, l + 1);
    exit_section_(b, m, TABLE_QUERY_EXPRESSION, r);
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
  public static boolean TypeAccessExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeAccessExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_ACCESS_EXPRESSION, "<type access expression>");
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
  //                    | TypeDescTypeName
  //                    | ValueTypeName
  //                    | ReferenceTypeName
  //                    | NilLiteral
  public static boolean SimpleTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SimpleTypeName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMPLE_TYPE_NAME, "<simple type name>");
    r = consumeTokenSmart(b, NULL_LITERAL);
    if (!r) r = AnyTypeName(b, l + 1);
    if (!r) r = AnyDataTypeName(b, l + 1);
    if (!r) r = HandleTypeName(b, l + 1);
    if (!r) r = TypeDescTypeName(b, l + 1);
    if (!r) r = ValueTypeName(b, l + 1);
    if (!r) r = ReferenceTypeName(b, l + 1);
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
  // 5: ATOM(FunctionInvocationReference)
  // 6: ATOM(SimpleVariableReference)
  // 7: ATOM(TypeAccessExprInvocationReference)
  public static boolean VariableReference(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "VariableReference")) return false;
    addVariant(b, "<variable reference>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<variable reference>");
    r = StringFunctionInvocationReference(b, l + 1);
    if (!r) r = FunctionInvocationReference(b, l + 1);
    if (!r) r = SimpleVariableReference(b, l + 1);
    if (!r) r = TypeAccessExprInvocationReference(b, l + 1);
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

  // TypeAccessExpression Invocation
  public static boolean TypeAccessExprInvocationReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeAccessExprInvocationReference")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_ACCESS_EXPR_INVOCATION_REFERENCE, "<type access expr invocation reference>");
    r = TypeAccessExpression(b, l + 1);
    r = r && Invocation(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

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
