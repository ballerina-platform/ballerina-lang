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
package org.ballerinalang.plugins.idea.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.ballerinalang.plugins.idea.psi.impl.*;

public interface BallerinaTypes {

  IElementType ACTION_DEFINITION = new BallerinaElementType("ACTION_DEFINITION");
  IElementType ACTION_INVOCATION = new BallerinaElementType("ACTION_INVOCATION");
  IElementType ACTION_INVOCATION_STATEMENT = new BallerinaElementType("ACTION_INVOCATION_STATEMENT");
  IElementType ANNOTATION = new BallerinaElementType("ANNOTATION");
  IElementType ANNOTATION_NAME = new BallerinaElementType("ANNOTATION_NAME");
  IElementType ARGUMENT_LIST = new BallerinaElementType("ARGUMENT_LIST");
  IElementType ASSIGNMENT_STATEMENT = new BallerinaElementType("ASSIGNMENT_STATEMENT");
  IElementType BACKTICK_STRING = new BallerinaElementType("BACKTICK_STRING");
  IElementType BREAK_STATEMENT = new BallerinaElementType("BREAK_STATEMENT");
  IElementType CATCH_CLAUSE = new BallerinaElementType("CATCH_CLAUSE");
  IElementType COMMENT_STATEMENT = new BallerinaElementType("COMMENT_STATEMENT");
  IElementType CONNECTOR_BODY = new BallerinaElementType("CONNECTOR_BODY");
  IElementType CONNECTOR_DECLARATION = new BallerinaElementType("CONNECTOR_DECLARATION");
  IElementType CONNECTOR_DEFINITION = new BallerinaElementType("CONNECTOR_DEFINITION");
  IElementType CONSTANT_DEFINITION = new BallerinaElementType("CONSTANT_DEFINITION");
  IElementType ELEMENT_VALUE = new BallerinaElementType("ELEMENT_VALUE");
  IElementType ELEMENT_VALUE_ARRAY_INITIALIZER = new BallerinaElementType("ELEMENT_VALUE_ARRAY_INITIALIZER");
  IElementType ELEMENT_VALUE_PAIR = new BallerinaElementType("ELEMENT_VALUE_PAIR");
  IElementType ELEMENT_VALUE_PAIRS = new BallerinaElementType("ELEMENT_VALUE_PAIRS");
  IElementType ELSE_CLAUSE = new BallerinaElementType("ELSE_CLAUSE");
  IElementType ELSE_IF_CLAUSE = new BallerinaElementType("ELSE_IF_CLAUSE");
  IElementType EXPRESSION = new BallerinaElementType("EXPRESSION");
  IElementType EXPRESSION_LIST = new BallerinaElementType("EXPRESSION_LIST");
  IElementType FORK_JOIN_STATEMENT = new BallerinaElementType("FORK_JOIN_STATEMENT");
  IElementType FUNCTION_BODY = new BallerinaElementType("FUNCTION_BODY");
  IElementType FUNCTION_DEFINITION = new BallerinaElementType("FUNCTION_DEFINITION");
  IElementType FUNCTION_INVOCATION_STATEMENT = new BallerinaElementType("FUNCTION_INVOCATION_STATEMENT");
  IElementType FUNCTION_NAME = new BallerinaElementType("FUNCTION_NAME");
  IElementType IF_ELSE_IF_CLAUSE_BODY = new BallerinaElementType("IF_ELSE_IF_CLAUSE_BODY");
  IElementType IF_ELSE_STATEMENT = new BallerinaElementType("IF_ELSE_STATEMENT");
  IElementType IMPORT_DECLARATION = new BallerinaElementType("IMPORT_DECLARATION");
  IElementType ITERATE_STATEMENT = new BallerinaElementType("ITERATE_STATEMENT");
  IElementType JOIN_CLAUSE = new BallerinaElementType("JOIN_CLAUSE");
  IElementType JOIN_CONDITIONS = new BallerinaElementType("JOIN_CONDITIONS");
  IElementType LITERAL_VALUE = new BallerinaElementType("LITERAL_VALUE");
  IElementType MAP_INIT_KEY_VALUE = new BallerinaElementType("MAP_INIT_KEY_VALUE");
  IElementType MAP_INIT_KEY_VALUE_LIST = new BallerinaElementType("MAP_INIT_KEY_VALUE_LIST");
  IElementType NAMED_PARAMETER = new BallerinaElementType("NAMED_PARAMETER");
  IElementType NAMED_PARAMETER_LIST = new BallerinaElementType("NAMED_PARAMETER_LIST");
  IElementType NATIVE_FUNCTION_DEFINITION = new BallerinaElementType("NATIVE_FUNCTION_DEFINITION");
  IElementType NULL_LITERAL = new BallerinaElementType("NULL_LITERAL");
  IElementType PACKAGE_DECLARATION = new BallerinaElementType("PACKAGE_DECLARATION");
  IElementType PACKAGE_NAME = new BallerinaElementType("PACKAGE_NAME");
  IElementType PARAMETER = new BallerinaElementType("PARAMETER");
  IElementType PARAMETER_LIST = new BallerinaElementType("PARAMETER_LIST");
  IElementType QUALIFIED_REFERENCE = new BallerinaElementType("QUALIFIED_REFERENCE");
  IElementType QUALIFIED_TYPE_NAME = new BallerinaElementType("QUALIFIED_TYPE_NAME");
  IElementType REPLY_STATEMENT = new BallerinaElementType("REPLY_STATEMENT");
  IElementType RESOURCE_DEFINITION = new BallerinaElementType("RESOURCE_DEFINITION");
  IElementType RETURN_PARAMETERS = new BallerinaElementType("RETURN_PARAMETERS");
  IElementType RETURN_STATEMENT = new BallerinaElementType("RETURN_STATEMENT");
  IElementType RETURN_TYPE_LIST = new BallerinaElementType("RETURN_TYPE_LIST");
  IElementType SERVICE_BODY = new BallerinaElementType("SERVICE_BODY");
  IElementType SERVICE_BODY_DECLARATION = new BallerinaElementType("SERVICE_BODY_DECLARATION");
  IElementType SERVICE_DEFINITION = new BallerinaElementType("SERVICE_DEFINITION");
  IElementType SIMPLE_TYPE = new BallerinaElementType("SIMPLE_TYPE");
  IElementType SIMPLE_TYPE_ARRAY = new BallerinaElementType("SIMPLE_TYPE_ARRAY");
  IElementType SIMPLE_TYPE_ITERATE = new BallerinaElementType("SIMPLE_TYPE_ITERATE");
  IElementType STATEMENT = new BallerinaElementType("STATEMENT");
  IElementType STRUCT_DEFINITION = new BallerinaElementType("STRUCT_DEFINITION");
  IElementType STRUCT_DEFINITION_BODY = new BallerinaElementType("STRUCT_DEFINITION_BODY");
  IElementType THROW_STATEMENT = new BallerinaElementType("THROW_STATEMENT");
  IElementType TIMEOUT_CLAUSE = new BallerinaElementType("TIMEOUT_CLAUSE");
  IElementType TRIGGER_WORKER = new BallerinaElementType("TRIGGER_WORKER");
  IElementType TRY_CATCH_STATEMENT = new BallerinaElementType("TRY_CATCH_STATEMENT");
  IElementType TRY_CATCH_STATEMENT_BODY = new BallerinaElementType("TRY_CATCH_STATEMENT_BODY");
  IElementType TYPE_CONVERTER_TYPES = new BallerinaElementType("TYPE_CONVERTER_TYPES");
  IElementType TYPE_CONVERTOR_BODY = new BallerinaElementType("TYPE_CONVERTOR_BODY");
  IElementType TYPE_CONVERTOR_DEFINITION = new BallerinaElementType("TYPE_CONVERTOR_DEFINITION");
  IElementType TYPE_NAME = new BallerinaElementType("TYPE_NAME");
  IElementType UNQUALIFIED_TYPE_NAME = new BallerinaElementType("UNQUALIFIED_TYPE_NAME");
  IElementType VARIABLE_DECLARATION = new BallerinaElementType("VARIABLE_DECLARATION");
  IElementType VARIABLE_REFERENCE = new BallerinaElementType("VARIABLE_REFERENCE");
  IElementType VARIABLE_REFERENCE_LIST = new BallerinaElementType("VARIABLE_REFERENCE_LIST");
  IElementType WHILE_STATEMENT = new BallerinaElementType("WHILE_STATEMENT");
  IElementType WHILE_STATEMENT_BODY = new BallerinaElementType("WHILE_STATEMENT_BODY");
  IElementType WITH_FULL_SCHEMA_TYPE = new BallerinaElementType("WITH_FULL_SCHEMA_TYPE");
  IElementType WITH_FULL_SCHEMA_TYPE_ARRAY = new BallerinaElementType("WITH_FULL_SCHEMA_TYPE_ARRAY");
  IElementType WITH_FULL_SCHEMA_TYPE_ITERATE = new BallerinaElementType("WITH_FULL_SCHEMA_TYPE_ITERATE");
  IElementType WITH_SCHEAM_ID_TYPE_ARRAY = new BallerinaElementType("WITH_SCHEAM_ID_TYPE_ARRAY");
  IElementType WITH_SCHEAM_ID_TYPE_ITERATE = new BallerinaElementType("WITH_SCHEAM_ID_TYPE_ITERATE");
  IElementType WITH_SCHEAM_URL_TYPE = new BallerinaElementType("WITH_SCHEAM_URL_TYPE");
  IElementType WITH_SCHEMA_ID_TYPE = new BallerinaElementType("WITH_SCHEMA_ID_TYPE");
  IElementType WITH_SCHEMA_URL_TYPE_ARRAY = new BallerinaElementType("WITH_SCHEMA_URL_TYPE_ARRAY");
  IElementType WITH_SCHEMA_URL_TYPE_ITERATE = new BallerinaElementType("WITH_SCHEMA_URL_TYPE_ITERATE");
  IElementType WORKER_DECLARATION = new BallerinaElementType("WORKER_DECLARATION");
  IElementType WORKER_INTERACTION_STATEMENT = new BallerinaElementType("WORKER_INTERACTION_STATEMENT");
  IElementType WORKER_REPLY = new BallerinaElementType("WORKER_REPLY");

  IElementType ACTION = new BallerinaTokenType("action");
  IElementType ADD = new BallerinaTokenType("+");
  IElementType ALL = new BallerinaTokenType("all");
  IElementType AND = new BallerinaTokenType("&&");
  IElementType ANY = new BallerinaTokenType("any");
  IElementType AS = new BallerinaTokenType("as");
  IElementType ASSIGN = new BallerinaTokenType("=");
  IElementType AT = new BallerinaTokenType("@");
  IElementType BACKTICK = new BallerinaTokenType("`");
  IElementType BANG = new BallerinaTokenType("!");
  IElementType BITAND = new BallerinaTokenType("&");
  IElementType BITOR = new BallerinaTokenType("|");
  IElementType BOOLEANLITERAL = new BallerinaTokenType("BooleanLiteral");
  IElementType BREAK = new BallerinaTokenType("break");
  IElementType CARET = new BallerinaTokenType("^");
  IElementType CATCH = new BallerinaTokenType("catch");
  IElementType COLON = new BallerinaTokenType(":");
  IElementType COMMA = new BallerinaTokenType(",");
  IElementType CONNECTOR = new BallerinaTokenType("connector");
  IElementType CONST = new BallerinaTokenType("const");
  IElementType DIV = new BallerinaTokenType("/");
  IElementType DOLLAR_SIGN = new BallerinaTokenType("$");
  IElementType DOT = new BallerinaTokenType(".");
  IElementType ELSE = new BallerinaTokenType("else");
  IElementType EQUAL = new BallerinaTokenType("==");
  IElementType FLOATINGPOINTLITERAL = new BallerinaTokenType("FloatingPointLiteral");
  IElementType FORK = new BallerinaTokenType("fork");
  IElementType FUNCTION = new BallerinaTokenType("function");
  IElementType GE = new BallerinaTokenType(">=");
  IElementType GT = new BallerinaTokenType(">");
  IElementType IDENTIFIER = new BallerinaTokenType("Identifier");
  IElementType IF = new BallerinaTokenType("if");
  IElementType IMPORT = new BallerinaTokenType("import");
  IElementType INTEGERLITERAL = new BallerinaTokenType("IntegerLiteral");
  IElementType ITERATE = new BallerinaTokenType("iterate");
  IElementType JOIN = new BallerinaTokenType("join");
  IElementType LBRACE = new BallerinaTokenType("{");
  IElementType LBRACK = new BallerinaTokenType("[");
  IElementType LE = new BallerinaTokenType("<=");
  IElementType LETTER = new BallerinaTokenType("Letter");
  IElementType LETTERORDIGIT = new BallerinaTokenType("LetterOrDigit");
  IElementType LINE_COMMENT = new BallerinaTokenType("LINE_COMMENT");
  IElementType LPAREN = new BallerinaTokenType("(");
  IElementType LT = new BallerinaTokenType("<");
  IElementType MOD = new BallerinaTokenType("%");
  IElementType MUL = new BallerinaTokenType("*");
  IElementType NEW = new BallerinaTokenType("new");
  IElementType NOTEQUAL = new BallerinaTokenType("!=");
  IElementType NULLLITERAL = new BallerinaTokenType("null");
  IElementType OR = new BallerinaTokenType("||");
  IElementType PACKAGE = new BallerinaTokenType("package");
  IElementType PUBLIC = new BallerinaTokenType("public");
  IElementType QUESTION = new BallerinaTokenType("?");
  IElementType QUOTEDSTRINGLITERAL = new BallerinaTokenType("QuotedStringLiteral");
  IElementType RBRACE = new BallerinaTokenType("}");
  IElementType RBRACK = new BallerinaTokenType("]");
  IElementType RECEIVEARROW = new BallerinaTokenType("<-");
  IElementType REPLY = new BallerinaTokenType("reply");
  IElementType RESOURCE = new BallerinaTokenType("resource");
  IElementType RETURN = new BallerinaTokenType("return");
  IElementType RPAREN = new BallerinaTokenType(")");
  IElementType SEMI = new BallerinaTokenType(";");
  IElementType SENDARROW = new BallerinaTokenType("->");
  IElementType SERVICE = new BallerinaTokenType("service");
  IElementType SUB = new BallerinaTokenType("-");
  IElementType THROW = new BallerinaTokenType("throw");
  IElementType THROWS = new BallerinaTokenType("throws");
  IElementType TILDE = new BallerinaTokenType("~");
  IElementType TIMEOUT = new BallerinaTokenType("timeout");
  IElementType TRY = new BallerinaTokenType("try");
  IElementType TYPE = new BallerinaTokenType("type");
  IElementType TYPECONVERTOR = new BallerinaTokenType("typeconvertor");
  IElementType VALIDBACKTICKSTRING = new BallerinaTokenType("ValidBackTickString");
  IElementType VERSION = new BallerinaTokenType("version");
  IElementType WHILE = new BallerinaTokenType("while");
  IElementType WORKER = new BallerinaTokenType("worker");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == ACTION_DEFINITION) {
        return new BallerinaActionDefinitionImpl(node);
      }
      else if (type == ACTION_INVOCATION) {
        return new BallerinaActionInvocationImpl(node);
      }
      else if (type == ACTION_INVOCATION_STATEMENT) {
        return new BallerinaActionInvocationStatementImpl(node);
      }
      else if (type == ANNOTATION) {
        return new BallerinaAnnotationImpl(node);
      }
      else if (type == ANNOTATION_NAME) {
        return new BallerinaAnnotationNameImpl(node);
      }
      else if (type == ARGUMENT_LIST) {
        return new BallerinaArgumentListImpl(node);
      }
      else if (type == ASSIGNMENT_STATEMENT) {
        return new BallerinaAssignmentStatementImpl(node);
      }
      else if (type == BACKTICK_STRING) {
        return new BallerinaBacktickStringImpl(node);
      }
      else if (type == BREAK_STATEMENT) {
        return new BallerinaBreakStatementImpl(node);
      }
      else if (type == CATCH_CLAUSE) {
        return new BallerinaCatchClauseImpl(node);
      }
      else if (type == COMMENT_STATEMENT) {
        return new BallerinaCommentStatementImpl(node);
      }
      else if (type == CONNECTOR_BODY) {
        return new BallerinaConnectorBodyImpl(node);
      }
      else if (type == CONNECTOR_DECLARATION) {
        return new BallerinaConnectorDeclarationImpl(node);
      }
      else if (type == CONNECTOR_DEFINITION) {
        return new BallerinaConnectorDefinitionImpl(node);
      }
      else if (type == CONSTANT_DEFINITION) {
        return new BallerinaConstantDefinitionImpl(node);
      }
      else if (type == ELEMENT_VALUE) {
        return new BallerinaElementValueImpl(node);
      }
      else if (type == ELEMENT_VALUE_ARRAY_INITIALIZER) {
        return new BallerinaElementValueArrayInitializerImpl(node);
      }
      else if (type == ELEMENT_VALUE_PAIR) {
        return new BallerinaElementValuePairImpl(node);
      }
      else if (type == ELEMENT_VALUE_PAIRS) {
        return new BallerinaElementValuePairsImpl(node);
      }
      else if (type == ELSE_CLAUSE) {
        return new BallerinaElseClauseImpl(node);
      }
      else if (type == ELSE_IF_CLAUSE) {
        return new BallerinaElseIfClauseImpl(node);
      }
      else if (type == EXPRESSION) {
        return new BallerinaExpressionImpl(node);
      }
      else if (type == EXPRESSION_LIST) {
        return new BallerinaExpressionListImpl(node);
      }
      else if (type == FORK_JOIN_STATEMENT) {
        return new BallerinaForkJoinStatementImpl(node);
      }
      else if (type == FUNCTION_BODY) {
        return new BallerinaFunctionBodyImpl(node);
      }
      else if (type == FUNCTION_DEFINITION) {
        return new BallerinaFunctionDefinitionImpl(node);
      }
      else if (type == FUNCTION_INVOCATION_STATEMENT) {
        return new BallerinaFunctionInvocationStatementImpl(node);
      }
      else if (type == FUNCTION_NAME) {
        return new BallerinaFunctionNameImpl(node);
      }
      else if (type == IF_ELSE_IF_CLAUSE_BODY) {
        return new BallerinaIfElseIfClauseBodyImpl(node);
      }
      else if (type == IF_ELSE_STATEMENT) {
        return new BallerinaIfElseStatementImpl(node);
      }
      else if (type == IMPORT_DECLARATION) {
        return new BallerinaImportDeclarationImpl(node);
      }
      else if (type == ITERATE_STATEMENT) {
        return new BallerinaIterateStatementImpl(node);
      }
      else if (type == JOIN_CLAUSE) {
        return new BallerinaJoinClauseImpl(node);
      }
      else if (type == JOIN_CONDITIONS) {
        return new BallerinaJoinConditionsImpl(node);
      }
      else if (type == LITERAL_VALUE) {
        return new BallerinaLiteralValueImpl(node);
      }
      else if (type == MAP_INIT_KEY_VALUE) {
        return new BallerinaMapInitKeyValueImpl(node);
      }
      else if (type == MAP_INIT_KEY_VALUE_LIST) {
        return new BallerinaMapInitKeyValueListImpl(node);
      }
      else if (type == NAMED_PARAMETER) {
        return new BallerinaNamedParameterImpl(node);
      }
      else if (type == NAMED_PARAMETER_LIST) {
        return new BallerinaNamedParameterListImpl(node);
      }
      else if (type == NATIVE_FUNCTION_DEFINITION) {
        return new BallerinaNativeFunctionDefinitionImpl(node);
      }
      else if (type == NULL_LITERAL) {
        return new BallerinaNullLiteralImpl(node);
      }
      else if (type == PACKAGE_DECLARATION) {
        return new BallerinaPackageDeclarationImpl(node);
      }
      else if (type == PACKAGE_NAME) {
        return new BallerinaPackageNameImpl(node);
      }
      else if (type == PARAMETER) {
        return new BallerinaParameterImpl(node);
      }
      else if (type == PARAMETER_LIST) {
        return new BallerinaParameterListImpl(node);
      }
      else if (type == QUALIFIED_REFERENCE) {
        return new BallerinaQualifiedReferenceImpl(node);
      }
      else if (type == QUALIFIED_TYPE_NAME) {
        return new BallerinaQualifiedTypeNameImpl(node);
      }
      else if (type == REPLY_STATEMENT) {
        return new BallerinaReplyStatementImpl(node);
      }
      else if (type == RESOURCE_DEFINITION) {
        return new BallerinaResourceDefinitionImpl(node);
      }
      else if (type == RETURN_PARAMETERS) {
        return new BallerinaReturnParametersImpl(node);
      }
      else if (type == RETURN_STATEMENT) {
        return new BallerinaReturnStatementImpl(node);
      }
      else if (type == RETURN_TYPE_LIST) {
        return new BallerinaReturnTypeListImpl(node);
      }
      else if (type == SERVICE_BODY) {
        return new BallerinaServiceBodyImpl(node);
      }
      else if (type == SERVICE_BODY_DECLARATION) {
        return new BallerinaServiceBodyDeclarationImpl(node);
      }
      else if (type == SERVICE_DEFINITION) {
        return new BallerinaServiceDefinitionImpl(node);
      }
      else if (type == SIMPLE_TYPE) {
        return new BallerinaSimpleTypeImpl(node);
      }
      else if (type == SIMPLE_TYPE_ARRAY) {
        return new BallerinaSimpleTypeArrayImpl(node);
      }
      else if (type == SIMPLE_TYPE_ITERATE) {
        return new BallerinaSimpleTypeIterateImpl(node);
      }
      else if (type == STATEMENT) {
        return new BallerinaStatementImpl(node);
      }
      else if (type == STRUCT_DEFINITION) {
        return new BallerinaStructDefinitionImpl(node);
      }
      else if (type == STRUCT_DEFINITION_BODY) {
        return new BallerinaStructDefinitionBodyImpl(node);
      }
      else if (type == THROW_STATEMENT) {
        return new BallerinaThrowStatementImpl(node);
      }
      else if (type == TIMEOUT_CLAUSE) {
        return new BallerinaTimeoutClauseImpl(node);
      }
      else if (type == TRIGGER_WORKER) {
        return new BallerinaTriggerWorkerImpl(node);
      }
      else if (type == TRY_CATCH_STATEMENT) {
        return new BallerinaTryCatchStatementImpl(node);
      }
      else if (type == TRY_CATCH_STATEMENT_BODY) {
        return new BallerinaTryCatchStatementBodyImpl(node);
      }
      else if (type == TYPE_CONVERTER_TYPES) {
        return new BallerinaTypeConverterTypesImpl(node);
      }
      else if (type == TYPE_CONVERTOR_BODY) {
        return new BallerinaTypeConvertorBodyImpl(node);
      }
      else if (type == TYPE_CONVERTOR_DEFINITION) {
        return new BallerinaTypeConvertorDefinitionImpl(node);
      }
      else if (type == TYPE_NAME) {
        return new BallerinaTypeNameImpl(node);
      }
      else if (type == UNQUALIFIED_TYPE_NAME) {
        return new BallerinaUnqualifiedTypeNameImpl(node);
      }
      else if (type == VARIABLE_DECLARATION) {
        return new BallerinaVariableDeclarationImpl(node);
      }
      else if (type == VARIABLE_REFERENCE) {
        return new BallerinaVariableReferenceImpl(node);
      }
      else if (type == VARIABLE_REFERENCE_LIST) {
        return new BallerinaVariableReferenceListImpl(node);
      }
      else if (type == WHILE_STATEMENT) {
        return new BallerinaWhileStatementImpl(node);
      }
      else if (type == WHILE_STATEMENT_BODY) {
        return new BallerinaWhileStatementBodyImpl(node);
      }
      else if (type == WITH_FULL_SCHEMA_TYPE) {
        return new BallerinaWithFullSchemaTypeImpl(node);
      }
      else if (type == WITH_FULL_SCHEMA_TYPE_ARRAY) {
        return new BallerinaWithFullSchemaTypeArrayImpl(node);
      }
      else if (type == WITH_FULL_SCHEMA_TYPE_ITERATE) {
        return new BallerinaWithFullSchemaTypeIterateImpl(node);
      }
      else if (type == WITH_SCHEAM_ID_TYPE_ARRAY) {
        return new BallerinaWithScheamIdTypeArrayImpl(node);
      }
      else if (type == WITH_SCHEAM_ID_TYPE_ITERATE) {
        return new BallerinaWithScheamIdTypeIterateImpl(node);
      }
      else if (type == WITH_SCHEAM_URL_TYPE) {
        return new BallerinaWithScheamURLTypeImpl(node);
      }
      else if (type == WITH_SCHEMA_ID_TYPE) {
        return new BallerinaWithSchemaIdTypeImpl(node);
      }
      else if (type == WITH_SCHEMA_URL_TYPE_ARRAY) {
        return new BallerinaWithSchemaURLTypeArrayImpl(node);
      }
      else if (type == WITH_SCHEMA_URL_TYPE_ITERATE) {
        return new BallerinaWithSchemaURLTypeIterateImpl(node);
      }
      else if (type == WORKER_DECLARATION) {
        return new BallerinaWorkerDeclarationImpl(node);
      }
      else if (type == WORKER_INTERACTION_STATEMENT) {
        return new BallerinaWorkerInteractionStatementImpl(node);
      }
      else if (type == WORKER_REPLY) {
        return new BallerinaWorkerReplyImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
