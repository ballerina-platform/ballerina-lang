/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.plugins.idea.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.ballerinalang.plugins.idea.psi.BallerinaTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
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
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == NULL_LITERAL) {
      r = NullLiteral(b, 0);
    }
    else if (t == ACTION_DEFINITION) {
      r = actionDefinition(b, 0);
    }
    else if (t == ACTION_INVOCATION) {
      r = actionInvocation(b, 0);
    }
    else if (t == ACTION_INVOCATION_STATEMENT) {
      r = actionInvocationStatement(b, 0);
    }
    else if (t == ANNOTATION) {
      r = annotation(b, 0);
    }
    else if (t == ANNOTATION_NAME) {
      r = annotationName(b, 0);
    }
    else if (t == ARGUMENT_LIST) {
      r = argumentList(b, 0);
    }
    else if (t == ASSIGNMENT_STATEMENT) {
      r = assignmentStatement(b, 0);
    }
    else if (t == BACKTICK_STRING) {
      r = backtickString(b, 0);
    }
    else if (t == BREAK_STATEMENT) {
      r = breakStatement(b, 0);
    }
    else if (t == CATCH_CLAUSE) {
      r = catchClause(b, 0);
    }
    else if (t == COMMENT_STATEMENT) {
      r = commentStatement(b, 0);
    }
    else if (t == CONNECTOR_BODY) {
      r = connectorBody(b, 0);
    }
    else if (t == CONNECTOR_DECLARATION) {
      r = connectorDeclaration(b, 0);
    }
    else if (t == CONNECTOR_DEFINITION) {
      r = connectorDefinition(b, 0);
    }
    else if (t == CONSTANT_DEFINITION) {
      r = constantDefinition(b, 0);
    }
    else if (t == ELEMENT_VALUE) {
      r = elementValue(b, 0);
    }
    else if (t == ELEMENT_VALUE_ARRAY_INITIALIZER) {
      r = elementValueArrayInitializer(b, 0);
    }
    else if (t == ELEMENT_VALUE_PAIR) {
      r = elementValuePair(b, 0);
    }
    else if (t == ELEMENT_VALUE_PAIRS) {
      r = elementValuePairs(b, 0);
    }
    else if (t == ELSE_CLAUSE) {
      r = elseClause(b, 0);
    }
    else if (t == ELSE_IF_CLAUSE) {
      r = elseIfClause(b, 0);
    }
    else if (t == EXPRESSION) {
      r = expression(b, 0);
    }
    else if (t == EXPRESSION_LIST) {
      r = expressionList(b, 0);
    }
    else if (t == FORK_JOIN_STATEMENT) {
      r = forkJoinStatement(b, 0);
    }
    else if (t == FUNCTION_BODY) {
      r = functionBody(b, 0);
    }
    else if (t == FUNCTION_DEFINITION) {
      r = functionDefinition(b, 0);
    }
    else if (t == FUNCTION_INVOCATION_STATEMENT) {
      r = functionInvocationStatement(b, 0);
    }
    else if (t == FUNCTION_NAME) {
      r = functionName(b, 0);
    }
    else if (t == IF_ELSE_IF_CLAUSE_BODY) {
      r = ifElseIfClauseBody(b, 0);
    }
    else if (t == IF_ELSE_STATEMENT) {
      r = ifElseStatement(b, 0);
    }
    else if (t == IMPORT_DECLARATION) {
      r = importDeclaration(b, 0);
    }
    else if (t == ITERATE_STATEMENT) {
      r = iterateStatement(b, 0);
    }
    else if (t == JOIN_CLAUSE) {
      r = joinClause(b, 0);
    }
    else if (t == JOIN_CONDITIONS) {
      r = joinConditions(b, 0);
    }
    else if (t == LITERAL_VALUE) {
      r = literalValue(b, 0);
    }
    else if (t == MAP_INIT_KEY_VALUE) {
      r = mapInitKeyValue(b, 0);
    }
    else if (t == MAP_INIT_KEY_VALUE_LIST) {
      r = mapInitKeyValueList(b, 0);
    }
    else if (t == NAMED_PARAMETER) {
      r = namedParameter(b, 0);
    }
    else if (t == NAMED_PARAMETER_LIST) {
      r = namedParameterList(b, 0);
    }
    else if (t == PACKAGE_DECLARATION) {
      r = packageDeclaration(b, 0);
    }
    else if (t == PACKAGE_NAME) {
      r = packageName(b, 0);
    }
    else if (t == PARAMETER) {
      r = parameter(b, 0);
    }
    else if (t == PARAMETER_LIST) {
      r = parameterList(b, 0);
    }
    else if (t == QUALIFIED_REFERENCE) {
      r = qualifiedReference(b, 0);
    }
    else if (t == QUALIFIED_TYPE_NAME) {
      r = qualifiedTypeName(b, 0);
    }
    else if (t == REPLY_STATEMENT) {
      r = replyStatement(b, 0);
    }
    else if (t == RESOURCE_DEFINITION) {
      r = resourceDefinition(b, 0);
    }
    else if (t == RETURN_PARAMETERS) {
      r = returnParameters(b, 0);
    }
    else if (t == RETURN_STATEMENT) {
      r = returnStatement(b, 0);
    }
    else if (t == RETURN_TYPE_LIST) {
      r = returnTypeList(b, 0);
    }
    else if (t == SERVICE_BODY) {
      r = serviceBody(b, 0);
    }
    else if (t == SERVICE_BODY_DECLARATION) {
      r = serviceBodyDeclaration(b, 0);
    }
    else if (t == SERVICE_DEFINITION) {
      r = serviceDefinition(b, 0);
    }
    else if (t == SIMPLE_TYPE) {
      r = simpleType(b, 0);
    }
    else if (t == SIMPLE_TYPE_ARRAY) {
      r = simpleTypeArray(b, 0);
    }
    else if (t == SIMPLE_TYPE_ITERATE) {
      r = simpleTypeIterate(b, 0);
    }
    else if (t == STATEMENT) {
      r = statement(b, 0);
    }
    else if (t == STRUCT_DEFINITION) {
      r = structDefinition(b, 0);
    }
    else if (t == STRUCT_DEFINITION_BODY) {
      r = structDefinitionBody(b, 0);
    }
    else if (t == THROW_STATEMENT) {
      r = throwStatement(b, 0);
    }
    else if (t == TIMEOUT_CLAUSE) {
      r = timeoutClause(b, 0);
    }
    else if (t == TRIGGER_WORKER) {
      r = triggerWorker(b, 0);
    }
    else if (t == TRY_CATCH_STATEMENT) {
      r = tryCatchStatement(b, 0);
    }
    else if (t == TYPE_CONVERTER_TYPES) {
      r = typeConverterTypes(b, 0);
    }
    else if (t == TYPE_CONVERTOR_BODY) {
      r = typeConvertorBody(b, 0);
    }
    else if (t == TYPE_CONVERTOR_DEFINITION) {
      r = typeConvertorDefinition(b, 0);
    }
    else if (t == TYPE_NAME) {
      r = typeName(b, 0);
    }
    else if (t == UNQUALIFIED_TYPE_NAME) {
      r = unqualifiedTypeName(b, 0);
    }
    else if (t == VARIABLE_DECLARATION) {
      r = variableDeclaration(b, 0);
    }
    else if (t == VARIABLE_REFERENCE) {
      r = variableReference(b, 0);
    }
    else if (t == VARIABLE_REFERENCE_LIST) {
      r = variableReferenceList(b, 0);
    }
    else if (t == WHILE_STATEMENT) {
      r = whileStatement(b, 0);
    }
    else if (t == WHILE_STATEMENT_BODY) {
      r = whileStatementBody(b, 0);
    }
    else if (t == WITH_FULL_SCHEMA_TYPE) {
      r = withFullSchemaType(b, 0);
    }
    else if (t == WITH_FULL_SCHEMA_TYPE_ARRAY) {
      r = withFullSchemaTypeArray(b, 0);
    }
    else if (t == WITH_FULL_SCHEMA_TYPE_ITERATE) {
      r = withFullSchemaTypeIterate(b, 0);
    }
    else if (t == WITH_SCHEAM_ID_TYPE_ARRAY) {
      r = withScheamIdTypeArray(b, 0);
    }
    else if (t == WITH_SCHEAM_ID_TYPE_ITERATE) {
      r = withScheamIdTypeIterate(b, 0);
    }
    else if (t == WITH_SCHEAM_URL_TYPE) {
      r = withScheamURLType(b, 0);
    }
    else if (t == WITH_SCHEMA_ID_TYPE) {
      r = withSchemaIdType(b, 0);
    }
    else if (t == WITH_SCHEMA_URL_TYPE_ARRAY) {
      r = withSchemaURLTypeArray(b, 0);
    }
    else if (t == WITH_SCHEMA_URL_TYPE_ITERATE) {
      r = withSchemaURLTypeIterate(b, 0);
    }
    else if (t == WORKER_DECLARATION) {
      r = workerDeclaration(b, 0);
    }
    else if (t == WORKER_INTERACTION_STATEMENT) {
      r = workerInteractionStatement(b, 0);
    }
    else if (t == WORKER_REPLY) {
      r = workerReply(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return compilationUnit(b, l + 1);
  }

  /* ********************************************************** */
  // 'null'
  public static boolean NullLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NullLiteral")) return false;
    if (!nextTokenIs(b, NULLLITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NULLLITERAL);
    exit_section_(b, m, NULL_LITERAL, r);
    return r;
  }

  /* ********************************************************** */
  // annotation* 'action' Identifier '(' parameterList ')' returnParameters?  ('throws' Identifier)? '{' functionBody '}'
  public static boolean actionDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "actionDefinition")) return false;
    if (!nextTokenIs(b, "<action definition>", AT, ACTION)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ACTION_DEFINITION, "<action definition>");
    r = actionDefinition_0(b, l + 1);
    r = r && consumeTokens(b, 0, ACTION, IDENTIFIER, LPAREN);
    r = r && parameterList(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && actionDefinition_6(b, l + 1);
    r = r && actionDefinition_7(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    r = r && functionBody(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // annotation*
  private static boolean actionDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "actionDefinition_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!annotation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "actionDefinition_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // returnParameters?
  private static boolean actionDefinition_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "actionDefinition_6")) return false;
    returnParameters(b, l + 1);
    return true;
  }

  // ('throws' Identifier)?
  private static boolean actionDefinition_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "actionDefinition_7")) return false;
    actionDefinition_7_0(b, l + 1);
    return true;
  }

  // 'throws' Identifier
  private static boolean actionDefinition_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "actionDefinition_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, THROWS, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // packageName ':' Identifier '.' Identifier
  public static boolean actionInvocation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "actionInvocation")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = packageName(b, l + 1);
    r = r && consumeTokens(b, 0, COLON, IDENTIFIER, DOT, IDENTIFIER);
    exit_section_(b, m, ACTION_INVOCATION, r);
    return r;
  }

  /* ********************************************************** */
  // actionInvocation argumentList ';'
  public static boolean actionInvocationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "actionInvocationStatement")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = actionInvocation(b, l + 1);
    r = r && argumentList(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, ACTION_INVOCATION_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // '@' annotationName ( '(' ( elementValuePairs | elementValue )? ')' )?
  public static boolean annotation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotation")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AT);
    r = r && annotationName(b, l + 1);
    r = r && annotation_2(b, l + 1);
    exit_section_(b, m, ANNOTATION, r);
    return r;
  }

  // ( '(' ( elementValuePairs | elementValue )? ')' )?
  private static boolean annotation_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotation_2")) return false;
    annotation_2_0(b, l + 1);
    return true;
  }

  // '(' ( elementValuePairs | elementValue )? ')'
  private static boolean annotation_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotation_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && annotation_2_0_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( elementValuePairs | elementValue )?
  private static boolean annotation_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotation_2_0_1")) return false;
    annotation_2_0_1_0(b, l + 1);
    return true;
  }

  // elementValuePairs | elementValue
  private static boolean annotation_2_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotation_2_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = elementValuePairs(b, l + 1);
    if (!r) r = elementValue(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier
  public static boolean annotationName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotationName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, ANNOTATION_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // '(' expressionList? ')'
  public static boolean argumentList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && argumentList_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, ARGUMENT_LIST, r);
    return r;
  }

  // expressionList?
  private static boolean argumentList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // variableReferenceList '=' expression ';'
  public static boolean assignmentStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignmentStatement")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variableReferenceList(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, ASSIGNMENT_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // ValidBackTickString
  public static boolean backtickString(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "backtickString")) return false;
    if (!nextTokenIs(b, VALIDBACKTICKSTRING)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VALIDBACKTICKSTRING);
    exit_section_(b, m, BACKTICK_STRING, r);
    return r;
  }

  /* ********************************************************** */
  // backtickString                                                      // templateExpression
  //     |   functionName argumentList                                           // functionInvocationExpression
  //     |   actionInvocation argumentList                                       // actionInvocationExpression
  //     |   '[' expressionList ']'                                              // arrayInitializerExpression
  //     |   '{' mapInitKeyValueList '}'                                         // mapInitializerExpression
  //     |   'new' (packageName ':' )? Identifier ('(' expressionList? ')')?     // structInitializeExpression
  //     |    literalValue                                                        // literalExpression
  //     |   variableReference
  //     |   '(' typeName ')' basicExpression
  //     |   ('+'|'-'|'!') basicExpression
  //     |   '(' basicExpression ')'
  static boolean basicExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicExpression")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = backtickString(b, l + 1);
    if (!r) r = basicExpression_1(b, l + 1);
    if (!r) r = basicExpression_2(b, l + 1);
    if (!r) r = basicExpression_3(b, l + 1);
    if (!r) r = basicExpression_4(b, l + 1);
    if (!r) r = basicExpression_5(b, l + 1);
    if (!r) r = literalValue(b, l + 1);
    if (!r) r = variableReference(b, l + 1);
    if (!r) r = basicExpression_8(b, l + 1);
    if (!r) r = basicExpression_9(b, l + 1);
    if (!r) r = basicExpression_10(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // functionName argumentList
  private static boolean basicExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicExpression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionName(b, l + 1);
    r = r && argumentList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // actionInvocation argumentList
  private static boolean basicExpression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicExpression_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = actionInvocation(b, l + 1);
    r = r && argumentList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '[' expressionList ']'
  private static boolean basicExpression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicExpression_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACK);
    r = r && expressionList(b, l + 1);
    r = r && consumeToken(b, RBRACK);
    exit_section_(b, m, null, r);
    return r;
  }

  // '{' mapInitKeyValueList '}'
  private static boolean basicExpression_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicExpression_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && mapInitKeyValueList(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'new' (packageName ':' )? Identifier ('(' expressionList? ')')?
  private static boolean basicExpression_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicExpression_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NEW);
    r = r && basicExpression_5_1(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    r = r && basicExpression_5_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (packageName ':' )?
  private static boolean basicExpression_5_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicExpression_5_1")) return false;
    basicExpression_5_1_0(b, l + 1);
    return true;
  }

  // packageName ':'
  private static boolean basicExpression_5_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicExpression_5_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = packageName(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('(' expressionList? ')')?
  private static boolean basicExpression_5_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicExpression_5_3")) return false;
    basicExpression_5_3_0(b, l + 1);
    return true;
  }

  // '(' expressionList? ')'
  private static boolean basicExpression_5_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicExpression_5_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && basicExpression_5_3_0_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean basicExpression_5_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicExpression_5_3_0_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  // '(' typeName ')' basicExpression
  private static boolean basicExpression_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicExpression_8")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && typeName(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && basicExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('+'|'-'|'!') basicExpression
  private static boolean basicExpression_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicExpression_9")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = basicExpression_9_0(b, l + 1);
    r = r && basicExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '+'|'-'|'!'
  private static boolean basicExpression_9_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicExpression_9_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ADD);
    if (!r) r = consumeToken(b, SUB);
    if (!r) r = consumeToken(b, BANG);
    exit_section_(b, m, null, r);
    return r;
  }

  // '(' basicExpression ')'
  private static boolean basicExpression_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basicExpression_10")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && basicExpression(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'break' ';'
  public static boolean breakStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "breakStatement")) return false;
    if (!nextTokenIs(b, BREAK)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BREAK, SEMI);
    exit_section_(b, m, BREAK_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // 'catch' '(' typeName Identifier ')' '{' statement+ '}'
  public static boolean catchClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "catchClause")) return false;
    if (!nextTokenIs(b, CATCH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, CATCH, LPAREN);
    r = r && typeName(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, RPAREN, LBRACE);
    r = r && catchClause_6(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, CATCH_CLAUSE, r);
    return r;
  }

  // statement+
  private static boolean catchClause_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "catchClause_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statement(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "catchClause_6", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LINE_COMMENT
  public static boolean commentStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "commentStatement")) return false;
    if (!nextTokenIs(b, LINE_COMMENT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LINE_COMMENT);
    exit_section_(b, m, COMMENT_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // packageDeclaration?
  //         importDeclaration*
  //     (   serviceDefinition
  //     |   functionDefinition
  //     |   connectorDefinition
  //     |   structDefinition
  //     |   typeConvertorDefinition
  //     |   constantDefinition
  //     )+
  static boolean compilationUnit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compilationUnit")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = compilationUnit_0(b, l + 1);
    r = r && compilationUnit_1(b, l + 1);
    r = r && compilationUnit_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // packageDeclaration?
  private static boolean compilationUnit_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compilationUnit_0")) return false;
    packageDeclaration(b, l + 1);
    return true;
  }

  // importDeclaration*
  private static boolean compilationUnit_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compilationUnit_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!importDeclaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "compilationUnit_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (   serviceDefinition
  //     |   functionDefinition
  //     |   connectorDefinition
  //     |   structDefinition
  //     |   typeConvertorDefinition
  //     |   constantDefinition
  //     )+
  private static boolean compilationUnit_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compilationUnit_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = compilationUnit_2_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!compilationUnit_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "compilationUnit_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // serviceDefinition
  //     |   functionDefinition
  //     |   connectorDefinition
  //     |   structDefinition
  //     |   typeConvertorDefinition
  //     |   constantDefinition
  private static boolean compilationUnit_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compilationUnit_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = serviceDefinition(b, l + 1);
    if (!r) r = functionDefinition(b, l + 1);
    if (!r) r = connectorDefinition(b, l + 1);
    if (!r) r = structDefinition(b, l + 1);
    if (!r) r = typeConvertorDefinition(b, l + 1);
    if (!r) r = constantDefinition(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // connectorDeclaration* variableDeclaration* actionDefinition+
  public static boolean connectorBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connectorBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONNECTOR_BODY, "<connector body>");
    r = connectorBody_0(b, l + 1);
    r = r && connectorBody_1(b, l + 1);
    r = r && connectorBody_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // connectorDeclaration*
  private static boolean connectorBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connectorBody_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!connectorDeclaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "connectorBody_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // variableDeclaration*
  private static boolean connectorBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connectorBody_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!variableDeclaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "connectorBody_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // actionDefinition+
  private static boolean connectorBody_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connectorBody_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = actionDefinition(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!actionDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "connectorBody_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // qualifiedReference Identifier '=' 'new' qualifiedReference '(' expressionList? ')'';'
  public static boolean connectorDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connectorDeclaration")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifiedReference(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, ASSIGN, NEW);
    r = r && qualifiedReference(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    r = r && connectorDeclaration_6(b, l + 1);
    r = r && consumeTokens(b, 0, RPAREN, SEMI);
    exit_section_(b, m, CONNECTOR_DECLARATION, r);
    return r;
  }

  // expressionList?
  private static boolean connectorDeclaration_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connectorDeclaration_6")) return false;
    expressionList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // annotation* 'connector' Identifier '(' parameterList ')' '{' connectorBody '}'
  public static boolean connectorDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connectorDefinition")) return false;
    if (!nextTokenIs(b, "<connector definition>", AT, CONNECTOR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONNECTOR_DEFINITION, "<connector definition>");
    r = connectorDefinition_0(b, l + 1);
    r = r && consumeTokens(b, 0, CONNECTOR, IDENTIFIER, LPAREN);
    r = r && parameterList(b, l + 1);
    r = r && consumeTokens(b, 0, RPAREN, LBRACE);
    r = r && connectorBody(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // annotation*
  private static boolean connectorDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connectorDefinition_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!annotation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "connectorDefinition_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // 'const' typeName Identifier '=' literalValue ';'
  public static boolean constantDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantDefinition")) return false;
    if (!nextTokenIs(b, CONST)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CONST);
    r = r && typeName(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, ASSIGN);
    r = r && literalValue(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, CONSTANT_DEFINITION, r);
    return r;
  }

  /* ********************************************************** */
  // expression
  //      |   annotation
  //      |   elementValueArrayInitializer
  public static boolean elementValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValue")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ELEMENT_VALUE, "<element value>");
    r = expression(b, l + 1);
    if (!r) r = annotation(b, l + 1);
    if (!r) r = elementValueArrayInitializer(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '{' (elementValue (',' elementValue)*)? (',')? '}'
  public static boolean elementValueArrayInitializer(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValueArrayInitializer")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && elementValueArrayInitializer_1(b, l + 1);
    r = r && elementValueArrayInitializer_2(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, ELEMENT_VALUE_ARRAY_INITIALIZER, r);
    return r;
  }

  // (elementValue (',' elementValue)*)?
  private static boolean elementValueArrayInitializer_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValueArrayInitializer_1")) return false;
    elementValueArrayInitializer_1_0(b, l + 1);
    return true;
  }

  // elementValue (',' elementValue)*
  private static boolean elementValueArrayInitializer_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValueArrayInitializer_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = elementValue(b, l + 1);
    r = r && elementValueArrayInitializer_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' elementValue)*
  private static boolean elementValueArrayInitializer_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValueArrayInitializer_1_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!elementValueArrayInitializer_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "elementValueArrayInitializer_1_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' elementValue
  private static boolean elementValueArrayInitializer_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValueArrayInitializer_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && elementValue(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',')?
  private static boolean elementValueArrayInitializer_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValueArrayInitializer_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // Identifier '=' elementValue
  public static boolean elementValuePair(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValuePair")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, ASSIGN);
    r = r && elementValue(b, l + 1);
    exit_section_(b, m, ELEMENT_VALUE_PAIR, r);
    return r;
  }

  /* ********************************************************** */
  // elementValuePair (',' elementValuePair)*
  public static boolean elementValuePairs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValuePairs")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = elementValuePair(b, l + 1);
    r = r && elementValuePairs_1(b, l + 1);
    exit_section_(b, m, ELEMENT_VALUE_PAIRS, r);
    return r;
  }

  // (',' elementValuePair)*
  private static boolean elementValuePairs_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValuePairs_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!elementValuePairs_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "elementValuePairs_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' elementValuePair
  private static boolean elementValuePairs_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValuePairs_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && elementValuePair(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'else' '{' ifElseIfClauseBody
  public static boolean elseClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseClause")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ELSE, LBRACE);
    r = r && ifElseIfClauseBody(b, l + 1);
    exit_section_(b, m, ELSE_CLAUSE, r);
    return r;
  }

  /* ********************************************************** */
  // 'else' 'if' '(' expression ')' '{' ifElseIfClauseBody
  public static boolean elseIfClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elseIfClause")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ELSE, IF, LPAREN);
    r = r && expression(b, l + 1);
    r = r && consumeTokens(b, 0, RPAREN, LBRACE);
    r = r && ifElseIfClauseBody(b, l + 1);
    exit_section_(b, m, ELSE_IF_CLAUSE, r);
    return r;
  }

  /* ********************************************************** */
  // '(' typeName ')' basicExpression
  //     |   ('+'|'-'|'!') basicExpression
  //     |   '(' basicExpression ')'
  //     |   basicExpression (('^' | '/' | '*' | '%' | '&&' | '+' | '-' | '||' | '>' | '>='| '<' | '<=' | '==' | '!=') basicExpression ) *
  public static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION, "<expression>");
    r = expression_0(b, l + 1);
    if (!r) r = expression_1(b, l + 1);
    if (!r) r = expression_2(b, l + 1);
    if (!r) r = expression_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '(' typeName ')' basicExpression
  private static boolean expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && typeName(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && basicExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('+'|'-'|'!') basicExpression
  private static boolean expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression_1_0(b, l + 1);
    r = r && basicExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '+'|'-'|'!'
  private static boolean expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ADD);
    if (!r) r = consumeToken(b, SUB);
    if (!r) r = consumeToken(b, BANG);
    exit_section_(b, m, null, r);
    return r;
  }

  // '(' basicExpression ')'
  private static boolean expression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && basicExpression(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // basicExpression (('^' | '/' | '*' | '%' | '&&' | '+' | '-' | '||' | '>' | '>='| '<' | '<=' | '==' | '!=') basicExpression ) *
  private static boolean expression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = basicExpression(b, l + 1);
    r = r && expression_3_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (('^' | '/' | '*' | '%' | '&&' | '+' | '-' | '||' | '>' | '>='| '<' | '<=' | '==' | '!=') basicExpression ) *
  private static boolean expression_3_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_3_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!expression_3_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expression_3_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ('^' | '/' | '*' | '%' | '&&' | '+' | '-' | '||' | '>' | '>='| '<' | '<=' | '==' | '!=') basicExpression
  private static boolean expression_3_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_3_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression_3_1_0_0(b, l + 1);
    r = r && basicExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '^' | '/' | '*' | '%' | '&&' | '+' | '-' | '||' | '>' | '>='| '<' | '<=' | '==' | '!='
  private static boolean expression_3_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_3_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CARET);
    if (!r) r = consumeToken(b, DIV);
    if (!r) r = consumeToken(b, MUL);
    if (!r) r = consumeToken(b, MOD);
    if (!r) r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, ADD);
    if (!r) r = consumeToken(b, SUB);
    if (!r) r = consumeToken(b, OR);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, GE);
    if (!r) r = consumeToken(b, LT);
    if (!r) r = consumeToken(b, LE);
    if (!r) r = consumeToken(b, EQUAL);
    if (!r) r = consumeToken(b, NOTEQUAL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // expression (',' expression)*
  public static boolean expressionList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION_LIST, "<expression list>");
    r = expression(b, l + 1);
    r = r && expressionList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' expression)*
  private static boolean expressionList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!expressionList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expressionList_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' expression
  private static boolean expressionList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'fork' '(' typeName Identifier ')' '{' workerDeclaration+ '}' joinClause? timeoutClause?
  public static boolean forkJoinStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forkJoinStatement")) return false;
    if (!nextTokenIs(b, FORK)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, FORK, LPAREN);
    r = r && typeName(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, RPAREN, LBRACE);
    r = r && forkJoinStatement_6(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    r = r && forkJoinStatement_8(b, l + 1);
    r = r && forkJoinStatement_9(b, l + 1);
    exit_section_(b, m, FORK_JOIN_STATEMENT, r);
    return r;
  }

  // workerDeclaration+
  private static boolean forkJoinStatement_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forkJoinStatement_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = workerDeclaration(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!workerDeclaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "forkJoinStatement_6", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // joinClause?
  private static boolean forkJoinStatement_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forkJoinStatement_8")) return false;
    joinClause(b, l + 1);
    return true;
  }

  // timeoutClause?
  private static boolean forkJoinStatement_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forkJoinStatement_9")) return false;
    timeoutClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // connectorDeclaration* variableDeclaration* workerDeclaration* statement+
  public static boolean functionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_BODY, "<function body>");
    r = functionBody_0(b, l + 1);
    r = r && functionBody_1(b, l + 1);
    r = r && functionBody_2(b, l + 1);
    r = r && functionBody_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // connectorDeclaration*
  private static boolean functionBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBody_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!connectorDeclaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionBody_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // variableDeclaration*
  private static boolean functionBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBody_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!variableDeclaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionBody_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // workerDeclaration*
  private static boolean functionBody_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBody_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!workerDeclaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionBody_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // statement+
  private static boolean functionBody_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBody_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statement(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionBody_3", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // annotation* 'public'? 'function' Identifier '(' parameterList? ')' returnParameters? ('throws' Identifier)? '{' functionBody '}'
  public static boolean functionDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDefinition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_DEFINITION, "<function definition>");
    r = functionDefinition_0(b, l + 1);
    r = r && functionDefinition_1(b, l + 1);
    r = r && consumeTokens(b, 0, FUNCTION, IDENTIFIER, LPAREN);
    r = r && functionDefinition_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && functionDefinition_7(b, l + 1);
    r = r && functionDefinition_8(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    r = r && functionBody(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // annotation*
  private static boolean functionDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDefinition_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!annotation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionDefinition_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'public'?
  private static boolean functionDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDefinition_1")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  // parameterList?
  private static boolean functionDefinition_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDefinition_5")) return false;
    parameterList(b, l + 1);
    return true;
  }

  // returnParameters?
  private static boolean functionDefinition_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDefinition_7")) return false;
    returnParameters(b, l + 1);
    return true;
  }

  // ('throws' Identifier)?
  private static boolean functionDefinition_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDefinition_8")) return false;
    functionDefinition_8_0(b, l + 1);
    return true;
  }

  // 'throws' Identifier
  private static boolean functionDefinition_8_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDefinition_8_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, THROWS, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // functionName argumentList ';'
  public static boolean functionInvocationStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionInvocationStatement")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionName(b, l + 1);
    r = r && argumentList(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, FUNCTION_INVOCATION_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // (packageName ':')? Identifier
  public static boolean functionName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionName_0(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, FUNCTION_NAME, r);
    return r;
  }

  // (packageName ':')?
  private static boolean functionName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionName_0")) return false;
    functionName_0_0(b, l + 1);
    return true;
  }

  // packageName ':'
  private static boolean functionName_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionName_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = packageName(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // statement*
  public static boolean ifElseIfClauseBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseIfClauseBody")) return false;
    Marker m = enter_section_(b, l, _NONE_, IF_ELSE_IF_CLAUSE_BODY, "<if else if clause body>");
    int c = current_position_(b);
    while (true) {
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifElseIfClauseBody", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // 'if' '(' expression ')' '{' ifElseIfClauseBody '}' (elseIfClause '}')* (elseClause '}')?
  public static boolean ifElseStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IF, LPAREN);
    r = r && expression(b, l + 1);
    r = r && consumeTokens(b, 0, RPAREN, LBRACE);
    r = r && ifElseIfClauseBody(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    r = r && ifElseStatement_7(b, l + 1);
    r = r && ifElseStatement_8(b, l + 1);
    exit_section_(b, m, IF_ELSE_STATEMENT, r);
    return r;
  }

  // (elseIfClause '}')*
  private static boolean ifElseStatement_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_7")) return false;
    int c = current_position_(b);
    while (true) {
      if (!ifElseStatement_7_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifElseStatement_7", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // elseIfClause '}'
  private static boolean ifElseStatement_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = elseIfClause(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // (elseClause '}')?
  private static boolean ifElseStatement_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_8")) return false;
    ifElseStatement_8_0(b, l + 1);
    return true;
  }

  // elseClause '}'
  private static boolean ifElseStatement_8_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElseStatement_8_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = elseClause(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'import' packageName ('as' Identifier)? ';'
  public static boolean importDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importDeclaration")) return false;
    if (!nextTokenIs(b, IMPORT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IMPORT);
    r = r && packageName(b, l + 1);
    r = r && importDeclaration_2(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, IMPORT_DECLARATION, r);
    return r;
  }

  // ('as' Identifier)?
  private static boolean importDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importDeclaration_2")) return false;
    importDeclaration_2_0(b, l + 1);
    return true;
  }

  // 'as' Identifier
  private static boolean importDeclaration_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importDeclaration_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, AS, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'iterate' '(' typeName Identifier ':' expression ')' '{' statement+ '}'
  public static boolean iterateStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "iterateStatement")) return false;
    if (!nextTokenIs(b, ITERATE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ITERATE, LPAREN);
    r = r && typeName(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, COLON);
    r = r && expression(b, l + 1);
    r = r && consumeTokens(b, 0, RPAREN, LBRACE);
    r = r && iterateStatement_8(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, ITERATE_STATEMENT, r);
    return r;
  }

  // statement+
  private static boolean iterateStatement_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "iterateStatement_8")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statement(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "iterateStatement_8", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'join' '(' joinConditions ')' '(' typeName Identifier ')'  '{' statement+ '}'
  public static boolean joinClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "joinClause")) return false;
    if (!nextTokenIs(b, JOIN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, JOIN, LPAREN);
    r = r && joinConditions(b, l + 1);
    r = r && consumeTokens(b, 0, RPAREN, LPAREN);
    r = r && typeName(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, RPAREN, LBRACE);
    r = r && joinClause_9(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, JOIN_CLAUSE, r);
    return r;
  }

  // statement+
  private static boolean joinClause_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "joinClause_9")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statement(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "joinClause_9", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'any' IntegerLiteral (Identifier (',' Identifier)*)?
  //     |   'all' (Identifier (',' Identifier)*)?
  public static boolean joinConditions(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "joinConditions")) return false;
    if (!nextTokenIs(b, "<join conditions>", ALL, ANY)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, JOIN_CONDITIONS, "<join conditions>");
    r = joinConditions_0(b, l + 1);
    if (!r) r = joinConditions_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'any' IntegerLiteral (Identifier (',' Identifier)*)?
  private static boolean joinConditions_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "joinConditions_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ANY, INTEGERLITERAL);
    r = r && joinConditions_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (Identifier (',' Identifier)*)?
  private static boolean joinConditions_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "joinConditions_0_2")) return false;
    joinConditions_0_2_0(b, l + 1);
    return true;
  }

  // Identifier (',' Identifier)*
  private static boolean joinConditions_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "joinConditions_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && joinConditions_0_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' Identifier)*
  private static boolean joinConditions_0_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "joinConditions_0_2_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!joinConditions_0_2_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "joinConditions_0_2_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' Identifier
  private static boolean joinConditions_0_2_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "joinConditions_0_2_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'all' (Identifier (',' Identifier)*)?
  private static boolean joinConditions_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "joinConditions_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ALL);
    r = r && joinConditions_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (Identifier (',' Identifier)*)?
  private static boolean joinConditions_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "joinConditions_1_1")) return false;
    joinConditions_1_1_0(b, l + 1);
    return true;
  }

  // Identifier (',' Identifier)*
  private static boolean joinConditions_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "joinConditions_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && joinConditions_1_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' Identifier)*
  private static boolean joinConditions_1_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "joinConditions_1_1_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!joinConditions_1_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "joinConditions_1_1_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' Identifier
  private static boolean joinConditions_1_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "joinConditions_1_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IntegerLiteral
  //     |   FloatingPointLiteral
  //     |   QuotedStringLiteral
  //     |   BooleanLiteral
  //     |   NullLiteral
  public static boolean literalValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalValue")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL_VALUE, "<literal value>");
    r = consumeToken(b, INTEGERLITERAL);
    if (!r) r = consumeToken(b, FLOATINGPOINTLITERAL);
    if (!r) r = consumeToken(b, QUOTEDSTRINGLITERAL);
    if (!r) r = consumeToken(b, BOOLEANLITERAL);
    if (!r) r = NullLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // QuotedStringLiteral ':' expression
  public static boolean mapInitKeyValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapInitKeyValue")) return false;
    if (!nextTokenIs(b, QUOTEDSTRINGLITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, QUOTEDSTRINGLITERAL, COLON);
    r = r && expression(b, l + 1);
    exit_section_(b, m, MAP_INIT_KEY_VALUE, r);
    return r;
  }

  /* ********************************************************** */
  // mapInitKeyValue (',' mapInitKeyValue)*
  public static boolean mapInitKeyValueList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapInitKeyValueList")) return false;
    if (!nextTokenIs(b, QUOTEDSTRINGLITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = mapInitKeyValue(b, l + 1);
    r = r && mapInitKeyValueList_1(b, l + 1);
    exit_section_(b, m, MAP_INIT_KEY_VALUE_LIST, r);
    return r;
  }

  // (',' mapInitKeyValue)*
  private static boolean mapInitKeyValueList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapInitKeyValueList_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!mapInitKeyValueList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "mapInitKeyValueList_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' mapInitKeyValue
  private static boolean mapInitKeyValueList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapInitKeyValueList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && mapInitKeyValue(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // typeName Identifier
  public static boolean namedParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedParameter")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = typeName(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, NAMED_PARAMETER, r);
    return r;
  }

  /* ********************************************************** */
  // namedParameter (',' namedParameter)*
  public static boolean namedParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedParameterList")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = namedParameter(b, l + 1);
    r = r && namedParameterList_1(b, l + 1);
    exit_section_(b, m, NAMED_PARAMETER_LIST, r);
    return r;
  }

  // (',' namedParameter)*
  private static boolean namedParameterList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedParameterList_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!namedParameterList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "namedParameterList_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' namedParameter
  private static boolean namedParameterList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedParameterList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && namedParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'package' packageName ';'
  public static boolean packageDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "packageDeclaration")) return false;
    if (!nextTokenIs(b, PACKAGE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PACKAGE);
    r = r && packageName(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, PACKAGE_DECLARATION, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier ('.' Identifier)*
  public static boolean packageName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "packageName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && packageName_1(b, l + 1);
    exit_section_(b, m, PACKAGE_NAME, r);
    return r;
  }

  // ('.' Identifier)*
  private static boolean packageName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "packageName_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!packageName_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "packageName_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // '.' Identifier
  private static boolean packageName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "packageName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOT, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // annotation* typeName Identifier
  public static boolean parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter")) return false;
    if (!nextTokenIs(b, "<parameter>", AT, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER, "<parameter>");
    r = parameter_0(b, l + 1);
    r = r && typeName(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // annotation*
  private static boolean parameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!annotation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameter_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // parameter  (',' parameter )*
  public static boolean parameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterList")) return false;
    if (!nextTokenIs(b, "<parameter list>", AT, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_LIST, "<parameter list>");
    r = parameter(b, l + 1);
    r = r && parameterList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' parameter )*
  private static boolean parameterList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterList_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!parameterList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameterList_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' parameter
  private static boolean parameterList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // packageName ':' Identifier
  public static boolean qualifiedReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedReference")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = packageName(b, l + 1);
    r = r && consumeTokens(b, 0, COLON, IDENTIFIER);
    exit_section_(b, m, QUALIFIED_REFERENCE, r);
    return r;
  }

  /* ********************************************************** */
  // packageName ':' unqualifiedTypeName
  public static boolean qualifiedTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedTypeName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = packageName(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && unqualifiedTypeName(b, l + 1);
    exit_section_(b, m, QUALIFIED_TYPE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // 'reply' expression ';'
  public static boolean replyStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "replyStatement")) return false;
    if (!nextTokenIs(b, REPLY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, REPLY);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, REPLY_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // annotation* 'resource' Identifier '(' parameterList ')' '{' functionBody '}'
  public static boolean resourceDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "resourceDefinition")) return false;
    if (!nextTokenIs(b, "<resource definition>", AT, RESOURCE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RESOURCE_DEFINITION, "<resource definition>");
    r = resourceDefinition_0(b, l + 1);
    r = r && consumeTokens(b, 0, RESOURCE, IDENTIFIER, LPAREN);
    r = r && parameterList(b, l + 1);
    r = r && consumeTokens(b, 0, RPAREN, LBRACE);
    r = r && functionBody(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // annotation*
  private static boolean resourceDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "resourceDefinition_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!annotation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "resourceDefinition_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // '(' (namedParameterList | returnTypeList) ')'
  public static boolean returnParameters(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameters")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && returnParameters_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, RETURN_PARAMETERS, r);
    return r;
  }

  // namedParameterList | returnTypeList
  private static boolean returnParameters_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnParameters_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = namedParameterList(b, l + 1);
    if (!r) r = returnTypeList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'return' expressionList? ';'
  public static boolean returnStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement")) return false;
    if (!nextTokenIs(b, RETURN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RETURN);
    r = r && returnStatement_1(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, RETURN_STATEMENT, r);
    return r;
  }

  // expressionList?
  private static boolean returnStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // typeName (',' typeName)*
  public static boolean returnTypeList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnTypeList")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = typeName(b, l + 1);
    r = r && returnTypeList_1(b, l + 1);
    exit_section_(b, m, RETURN_TYPE_LIST, r);
    return r;
  }

  // (',' typeName)*
  private static boolean returnTypeList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnTypeList_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!returnTypeList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "returnTypeList_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' typeName
  private static boolean returnTypeList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnTypeList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && typeName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // serviceBodyDeclaration
  public static boolean serviceBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "serviceBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SERVICE_BODY, "<service body>");
    r = serviceBodyDeclaration(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // connectorDeclaration* variableDeclaration* resourceDefinition+
  public static boolean serviceBodyDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "serviceBodyDeclaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SERVICE_BODY_DECLARATION, "<service body declaration>");
    r = serviceBodyDeclaration_0(b, l + 1);
    r = r && serviceBodyDeclaration_1(b, l + 1);
    r = r && serviceBodyDeclaration_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // connectorDeclaration*
  private static boolean serviceBodyDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "serviceBodyDeclaration_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!connectorDeclaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "serviceBodyDeclaration_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // variableDeclaration*
  private static boolean serviceBodyDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "serviceBodyDeclaration_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!variableDeclaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "serviceBodyDeclaration_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // resourceDefinition+
  private static boolean serviceBodyDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "serviceBodyDeclaration_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = resourceDefinition(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!resourceDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "serviceBodyDeclaration_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // annotation* 'service' Identifier '{' serviceBody '}'
  public static boolean serviceDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "serviceDefinition")) return false;
    if (!nextTokenIs(b, "<service definition>", AT, SERVICE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SERVICE_DEFINITION, "<service definition>");
    r = serviceDefinition_0(b, l + 1);
    r = r && consumeTokens(b, 0, SERVICE, IDENTIFIER, LBRACE);
    r = r && serviceBody(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // annotation*
  private static boolean serviceDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "serviceDefinition_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!annotation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "serviceDefinition_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // Identifier
  public static boolean simpleType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleType")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, SIMPLE_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier '[' ']'
  public static boolean simpleTypeArray(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleTypeArray")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, LBRACK, RBRACK);
    exit_section_(b, m, SIMPLE_TYPE_ARRAY, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier '~'
  public static boolean simpleTypeIterate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleTypeIterate")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, TILDE);
    exit_section_(b, m, SIMPLE_TYPE_ITERATE, r);
    return r;
  }

  /* ********************************************************** */
  // functionInvocationStatement
  //     |   ifElseStatement
  //     |   iterateStatement
  //     |   whileStatement
  //     |   breakStatement
  //     |   forkJoinStatement
  //     |   tryCatchStatement
  //     |   throwStatement
  //     |   returnStatement
  //     |   replyStatement
  //     |   workerInteractionStatement
  //     |   commentStatement
  //     |   actionInvocationStatement
  //     |   assignmentStatement
  public static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT, "<statement>");
    r = functionInvocationStatement(b, l + 1);
    if (!r) r = ifElseStatement(b, l + 1);
    if (!r) r = iterateStatement(b, l + 1);
    if (!r) r = whileStatement(b, l + 1);
    if (!r) r = breakStatement(b, l + 1);
    if (!r) r = forkJoinStatement(b, l + 1);
    if (!r) r = tryCatchStatement(b, l + 1);
    if (!r) r = throwStatement(b, l + 1);
    if (!r) r = returnStatement(b, l + 1);
    if (!r) r = replyStatement(b, l + 1);
    if (!r) r = workerInteractionStatement(b, l + 1);
    if (!r) r = commentStatement(b, l + 1);
    if (!r) r = actionInvocationStatement(b, l + 1);
    if (!r) r = assignmentStatement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'public'? 'type' Identifier structDefinitionBody
  public static boolean structDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDefinition")) return false;
    if (!nextTokenIs(b, "<struct definition>", PUBLIC, TYPE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRUCT_DEFINITION, "<struct definition>");
    r = structDefinition_0(b, l + 1);
    r = r && consumeTokens(b, 0, TYPE, IDENTIFIER);
    r = r && structDefinitionBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'public'?
  private static boolean structDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDefinition_0")) return false;
    consumeToken(b, PUBLIC);
    return true;
  }

  /* ********************************************************** */
  // '{' (typeName Identifier ';')+ '}'
  public static boolean structDefinitionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDefinitionBody")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && structDefinitionBody_1(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, STRUCT_DEFINITION_BODY, r);
    return r;
  }

  // (typeName Identifier ';')+
  private static boolean structDefinitionBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDefinitionBody_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = structDefinitionBody_1_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!structDefinitionBody_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "structDefinitionBody_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // typeName Identifier ';'
  private static boolean structDefinitionBody_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structDefinitionBody_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = typeName(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, SEMI);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'throw' expression ';'
  public static boolean throwStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "throwStatement")) return false;
    if (!nextTokenIs(b, THROW)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, THROW);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, THROW_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // 'timeout' '(' expression ')' '(' typeName Identifier ')'  '{' statement+ '}'
  public static boolean timeoutClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "timeoutClause")) return false;
    if (!nextTokenIs(b, TIMEOUT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, TIMEOUT, LPAREN);
    r = r && expression(b, l + 1);
    r = r && consumeTokens(b, 0, RPAREN, LPAREN);
    r = r && typeName(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, RPAREN, LBRACE);
    r = r && timeoutClause_9(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, TIMEOUT_CLAUSE, r);
    return r;
  }

  // statement+
  private static boolean timeoutClause_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "timeoutClause_9")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statement(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "timeoutClause_9", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier '->' Identifier ';'
  public static boolean triggerWorker(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "triggerWorker")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, SENDARROW, IDENTIFIER, SEMI);
    exit_section_(b, m, TRIGGER_WORKER, r);
    return r;
  }

  /* ********************************************************** */
  // 'try' '{' statement+ '}' catchClause
  public static boolean tryCatchStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryCatchStatement")) return false;
    if (!nextTokenIs(b, TRY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, TRY, LBRACE);
    r = r && tryCatchStatement_2(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    r = r && catchClause(b, l + 1);
    exit_section_(b, m, TRY_CATCH_STATEMENT, r);
    return r;
  }

  // statement+
  private static boolean tryCatchStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryCatchStatement_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statement(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "tryCatchStatement_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // simpleType
  //     |   withFullSchemaType
  //     |   withSchemaIdType
  //     |   withScheamURLType
  public static boolean typeConverterTypes(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeConverterTypes")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpleType(b, l + 1);
    if (!r) r = withFullSchemaType(b, l + 1);
    if (!r) r = withSchemaIdType(b, l + 1);
    if (!r) r = withScheamURLType(b, l + 1);
    exit_section_(b, m, TYPE_CONVERTER_TYPES, r);
    return r;
  }

  /* ********************************************************** */
  // '{' variableDeclaration* statement+ '}'
  public static boolean typeConvertorBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeConvertorBody")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && typeConvertorBody_1(b, l + 1);
    r = r && typeConvertorBody_2(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, TYPE_CONVERTOR_BODY, r);
    return r;
  }

  // variableDeclaration*
  private static boolean typeConvertorBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeConvertorBody_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!variableDeclaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeConvertorBody_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // statement+
  private static boolean typeConvertorBody_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeConvertorBody_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statement(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeConvertorBody_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'typeconvertor' Identifier '(' typeConverterTypes Identifier ')' '('typeConverterTypes')' typeConvertorBody
  public static boolean typeConvertorDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeConvertorDefinition")) return false;
    if (!nextTokenIs(b, TYPECONVERTOR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, TYPECONVERTOR, IDENTIFIER, LPAREN);
    r = r && typeConverterTypes(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, RPAREN, LPAREN);
    r = r && typeConverterTypes(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && typeConvertorBody(b, l + 1);
    exit_section_(b, m, TYPE_CONVERTOR_DEFINITION, r);
    return r;
  }

  /* ********************************************************** */
  // unqualifiedTypeName
  //     |   qualifiedTypeName
  public static boolean typeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unqualifiedTypeName(b, l + 1);
    if (!r) r = qualifiedTypeName(b, l + 1);
    exit_section_(b, m, TYPE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // simpleTypeArray
  //     |   simpleTypeIterate
  //     |   withFullSchemaType
  //     |   withFullSchemaTypeArray
  //     |   withFullSchemaTypeIterate
  //     |   withScheamURLType
  //     |   withSchemaURLTypeArray
  //     |   withSchemaURLTypeIterate
  //     |   withSchemaIdType
  //     |   withScheamIdTypeArray
  //     |   withScheamIdTypeIterate
  //     |   simpleType
  public static boolean unqualifiedTypeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unqualifiedTypeName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpleTypeArray(b, l + 1);
    if (!r) r = simpleTypeIterate(b, l + 1);
    if (!r) r = withFullSchemaType(b, l + 1);
    if (!r) r = withFullSchemaTypeArray(b, l + 1);
    if (!r) r = withFullSchemaTypeIterate(b, l + 1);
    if (!r) r = withScheamURLType(b, l + 1);
    if (!r) r = withSchemaURLTypeArray(b, l + 1);
    if (!r) r = withSchemaURLTypeIterate(b, l + 1);
    if (!r) r = withSchemaIdType(b, l + 1);
    if (!r) r = withScheamIdTypeArray(b, l + 1);
    if (!r) r = withScheamIdTypeIterate(b, l + 1);
    if (!r) r = simpleType(b, l + 1);
    exit_section_(b, m, UNQUALIFIED_TYPE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // typeName Identifier ';'
  public static boolean variableDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableDeclaration")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = typeName(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, SEMI);
    exit_section_(b, m, VARIABLE_DECLARATION, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier '['expression']'                 // mapArrayVariableIdentifier// array and map reference
  // //    |   variableReference ('.' variableReference)+  // structFieldIdentifier// struct field reference
  //     |   Identifier
  public static boolean variableReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableReference")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variableReference_0(b, l + 1);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, VARIABLE_REFERENCE, r);
    return r;
  }

  // Identifier '['expression']'
  private static boolean variableReference_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableReference_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, LBRACK);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, RBRACK);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // variableReference (',' variableReference)*
  public static boolean variableReferenceList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableReferenceList")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variableReference(b, l + 1);
    r = r && variableReferenceList_1(b, l + 1);
    exit_section_(b, m, VARIABLE_REFERENCE_LIST, r);
    return r;
  }

  // (',' variableReference)*
  private static boolean variableReferenceList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableReferenceList_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!variableReferenceList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "variableReferenceList_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' variableReference
  private static boolean variableReferenceList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableReferenceList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && variableReference(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'while' '(' expression ')' '{' whileStatementBody '}'
  public static boolean whileStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whileStatement")) return false;
    if (!nextTokenIs(b, WHILE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, WHILE, LPAREN);
    r = r && expression(b, l + 1);
    r = r && consumeTokens(b, 0, RPAREN, LBRACE);
    r = r && whileStatementBody(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, WHILE_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // statement+
  public static boolean whileStatementBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whileStatementBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, WHILE_STATEMENT_BODY, "<while statement body>");
    r = statement(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "whileStatementBody", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // Identifier '<' '{' QuotedStringLiteral '}' Identifier '>'
  public static boolean withFullSchemaType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withFullSchemaType")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, LT, LBRACE, QUOTEDSTRINGLITERAL, RBRACE, IDENTIFIER, GT);
    exit_section_(b, m, WITH_FULL_SCHEMA_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier '<' '{' QuotedStringLiteral '}' Identifier '>' '[' ']'
  public static boolean withFullSchemaTypeArray(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withFullSchemaTypeArray")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, LT, LBRACE, QUOTEDSTRINGLITERAL, RBRACE, IDENTIFIER, GT, LBRACK, RBRACK);
    exit_section_(b, m, WITH_FULL_SCHEMA_TYPE_ARRAY, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier '<' '{' QuotedStringLiteral '}' Identifier '>' '~'
  public static boolean withFullSchemaTypeIterate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withFullSchemaTypeIterate")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, LT, LBRACE, QUOTEDSTRINGLITERAL, RBRACE, IDENTIFIER, GT, TILDE);
    exit_section_(b, m, WITH_FULL_SCHEMA_TYPE_ITERATE, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier '<' Identifier '>' '[' ']'
  public static boolean withScheamIdTypeArray(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withScheamIdTypeArray")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, LT, IDENTIFIER, GT, LBRACK, RBRACK);
    exit_section_(b, m, WITH_SCHEAM_ID_TYPE_ARRAY, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier '<' Identifier '>' '~'
  public static boolean withScheamIdTypeIterate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withScheamIdTypeIterate")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, LT, IDENTIFIER, GT, TILDE);
    exit_section_(b, m, WITH_SCHEAM_ID_TYPE_ITERATE, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier '<' '{' QuotedStringLiteral '}' '>'
  public static boolean withScheamURLType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withScheamURLType")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, LT, LBRACE, QUOTEDSTRINGLITERAL, RBRACE, GT);
    exit_section_(b, m, WITH_SCHEAM_URL_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier '<' Identifier '>'
  public static boolean withSchemaIdType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withSchemaIdType")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, LT, IDENTIFIER, GT);
    exit_section_(b, m, WITH_SCHEMA_ID_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier '<' '{' QuotedStringLiteral '}' '>' '[' ']'
  public static boolean withSchemaURLTypeArray(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withSchemaURLTypeArray")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, LT, LBRACE, QUOTEDSTRINGLITERAL, RBRACE, GT, LBRACK, RBRACK);
    exit_section_(b, m, WITH_SCHEMA_URL_TYPE_ARRAY, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier '<' '{' QuotedStringLiteral '}' '>' '~'
  public static boolean withSchemaURLTypeIterate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withSchemaURLTypeIterate")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, LT, LBRACE, QUOTEDSTRINGLITERAL, RBRACE, GT, TILDE);
    exit_section_(b, m, WITH_SCHEMA_URL_TYPE_ITERATE, r);
    return r;
  }

  /* ********************************************************** */
  // 'worker' Identifier '(' typeName Identifier ')'  '{' variableDeclaration* statement+ '}'
  public static boolean workerDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "workerDeclaration")) return false;
    if (!nextTokenIs(b, WORKER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, WORKER, IDENTIFIER, LPAREN);
    r = r && typeName(b, l + 1);
    r = r && consumeTokens(b, 0, IDENTIFIER, RPAREN, LBRACE);
    r = r && workerDeclaration_7(b, l + 1);
    r = r && workerDeclaration_8(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, WORKER_DECLARATION, r);
    return r;
  }

  // variableDeclaration*
  private static boolean workerDeclaration_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "workerDeclaration_7")) return false;
    int c = current_position_(b);
    while (true) {
      if (!variableDeclaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "workerDeclaration_7", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // statement+
  private static boolean workerDeclaration_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "workerDeclaration_8")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statement(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "workerDeclaration_8", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // triggerWorker
  //     |   workerReply
  public static boolean workerInteractionStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "workerInteractionStatement")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = triggerWorker(b, l + 1);
    if (!r) r = workerReply(b, l + 1);
    exit_section_(b, m, WORKER_INTERACTION_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // Identifier '<-' Identifier ';'
  public static boolean workerReply(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "workerReply")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, RECEIVEARROW, IDENTIFIER, SEMI);
    exit_section_(b, m, WORKER_REPLY, r);
    return r;
  }

}
