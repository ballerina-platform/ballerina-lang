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

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import io.ballerina.plugins.idea.psi.impl.*;

public interface BallerinaTypes {

  IElementType ALIAS = new BallerinaCompositeElementType("ALIAS");
  IElementType ANNOTATION_ATTACHMENT = new BallerinaCompositeElementType("ANNOTATION_ATTACHMENT");
  IElementType ANNOTATION_DEFINITION = new BallerinaCompositeElementType("ANNOTATION_DEFINITION");
  IElementType ANY_IDENTIFIER_NAME = new BallerinaCompositeElementType("ANY_IDENTIFIER_NAME");
  IElementType ARRAY_TYPE_NAME = new BallerinaCompositeElementType("ARRAY_TYPE_NAME");
  IElementType BACKTICKED_BLOCK = new BallerinaCompositeElementType("BACKTICKED_BLOCK");
  IElementType BUILT_IN_REFERENCE_TYPE_NAME = new BallerinaCompositeElementType("BUILT_IN_REFERENCE_TYPE_NAME");
  IElementType CLOSE_RECORD_TYPE_BODY = new BallerinaCompositeElementType("CLOSE_RECORD_TYPE_BODY");
  IElementType COMPLETE_PACKAGE_NAME = new BallerinaCompositeElementType("COMPLETE_PACKAGE_NAME");
  IElementType CONSTANT_DEFINITION = new BallerinaCompositeElementType("CONSTANT_DEFINITION");
  IElementType DEFINITION = new BallerinaCompositeElementType("DEFINITION");
  IElementType DEPRECATED_ANNOTATION_DOCUMENTATION = new BallerinaCompositeElementType("DEPRECATED_ANNOTATION_DOCUMENTATION");
  IElementType DEPRECATED_ANNOTATION_DOCUMENTATION_LINE = new BallerinaCompositeElementType("DEPRECATED_ANNOTATION_DOCUMENTATION_LINE");
  IElementType DEPRECATED_PARAMETERS_DOCUMENTATION = new BallerinaCompositeElementType("DEPRECATED_PARAMETERS_DOCUMENTATION");
  IElementType DEPRECATED_PARAMETERS_DOCUMENTATION_LINE = new BallerinaCompositeElementType("DEPRECATED_PARAMETERS_DOCUMENTATION_LINE");
  IElementType DEPRECATE_ANNOTATION_DESCRIPTION_LINE = new BallerinaCompositeElementType("DEPRECATE_ANNOTATION_DESCRIPTION_LINE");
  IElementType DOCUMENTATION_CONTENT = new BallerinaCompositeElementType("DOCUMENTATION_CONTENT");
  IElementType DOCUMENTATION_LINE = new BallerinaCompositeElementType("DOCUMENTATION_LINE");
  IElementType DOCUMENTATION_REFERENCE = new BallerinaCompositeElementType("DOCUMENTATION_REFERENCE");
  IElementType DOCUMENTATION_STRING = new BallerinaCompositeElementType("DOCUMENTATION_STRING");
  IElementType DOCUMENTATION_TEXT = new BallerinaCompositeElementType("DOCUMENTATION_TEXT");
  IElementType DOCUMENTATION_TEXT_CONTENT = new BallerinaCompositeElementType("DOCUMENTATION_TEXT_CONTENT");
  IElementType DOC_PARAMETER_DESCRIPTION = new BallerinaCompositeElementType("DOC_PARAMETER_DESCRIPTION");
  IElementType DOUBLE_BACKTICKED_BLOCK = new BallerinaCompositeElementType("DOUBLE_BACKTICKED_BLOCK");
  IElementType ERROR_TYPE_NAME = new BallerinaCompositeElementType("ERROR_TYPE_NAME");
  IElementType EXCLUSIVE_RECORD_TYPE_NAME = new BallerinaCompositeElementType("EXCLUSIVE_RECORD_TYPE_NAME");
  IElementType EXPR_FUNCTION_BODY_SPEC = new BallerinaCompositeElementType("EXPR_FUNCTION_BODY_SPEC");
  IElementType EXTERNAL_FUNCTION_BODY = new BallerinaCompositeElementType("EXTERNAL_FUNCTION_BODY");
  IElementType FUNCTION_DEFINITION = new BallerinaCompositeElementType("FUNCTION_DEFINITION");
  IElementType FUNCTION_DEFINITION_BODY = new BallerinaCompositeElementType("FUNCTION_DEFINITION_BODY");
  IElementType FUNCTION_SIGNATURE = new BallerinaCompositeElementType("FUNCTION_SIGNATURE");
  IElementType FUNCTION_TYPE_NAME = new BallerinaCompositeElementType("FUNCTION_TYPE_NAME");
  IElementType FUTURE_TYPE_NAME = new BallerinaCompositeElementType("FUTURE_TYPE_NAME");
  IElementType GLOBAL_VARIABLE_DEFINITION = new BallerinaCompositeElementType("GLOBAL_VARIABLE_DEFINITION");
  IElementType GROUP_TYPE_NAME = new BallerinaCompositeElementType("GROUP_TYPE_NAME");
  IElementType IMPORT_DECLARATION = new BallerinaCompositeElementType("IMPORT_DECLARATION");
  IElementType INCLUSIVE_RECORD_TYPE_NAME = new BallerinaCompositeElementType("INCLUSIVE_RECORD_TYPE_NAME");
  IElementType INTEGER_LITERAL = new BallerinaCompositeElementType("INTEGER_LITERAL");
  IElementType JSON_TYPE_NAME = new BallerinaCompositeElementType("JSON_TYPE_NAME");
  IElementType MAP_TYPE_NAME = new BallerinaCompositeElementType("MAP_TYPE_NAME");
  IElementType METHOD_DECLARATION = new BallerinaCompositeElementType("METHOD_DECLARATION");
  IElementType METHOD_DEFINITION = new BallerinaCompositeElementType("METHOD_DEFINITION");
  IElementType NAMESPACE_DECLARATION = new BallerinaCompositeElementType("NAMESPACE_DECLARATION");
  IElementType NAME_REFERENCE = new BallerinaCompositeElementType("NAME_REFERENCE");
  IElementType NESTED_ANNOTATION_ATTACHMENT = new BallerinaCompositeElementType("NESTED_ANNOTATION_ATTACHMENT");
  IElementType NESTED_FUNCTION_SIGNATURE = new BallerinaCompositeElementType("NESTED_FUNCTION_SIGNATURE");
  IElementType NESTED_RECOVERABLE_BODY = new BallerinaCompositeElementType("NESTED_RECOVERABLE_BODY");
  IElementType NESTED_RECOVERABLE_BODY_CONTENT = new BallerinaCompositeElementType("NESTED_RECOVERABLE_BODY_CONTENT");
  IElementType NESTED_RECOVERABLE_RETURN_TYPE = new BallerinaCompositeElementType("NESTED_RECOVERABLE_RETURN_TYPE");
  IElementType NIL_LITERAL = new BallerinaCompositeElementType("NIL_LITERAL");
  IElementType NULLABLE_TYPE_NAME = new BallerinaCompositeElementType("NULLABLE_TYPE_NAME");
  IElementType OBJECT_BODY = new BallerinaCompositeElementType("OBJECT_BODY");
  IElementType OBJECT_BODY_CONTENT = new BallerinaCompositeElementType("OBJECT_BODY_CONTENT");
  IElementType OBJECT_FIELD_DEFINITION = new BallerinaCompositeElementType("OBJECT_FIELD_DEFINITION");
  IElementType OBJECT_FIELD_DEFINITION_CONTENT = new BallerinaCompositeElementType("OBJECT_FIELD_DEFINITION_CONTENT");
  IElementType OBJECT_METHOD = new BallerinaCompositeElementType("OBJECT_METHOD");
  IElementType OBJECT_TYPE_BODY = new BallerinaCompositeElementType("OBJECT_TYPE_BODY");
  IElementType OBJECT_TYPE_NAME = new BallerinaCompositeElementType("OBJECT_TYPE_NAME");
  IElementType OPEN_RECORD_TYPE_BODY = new BallerinaCompositeElementType("OPEN_RECORD_TYPE_BODY");
  IElementType ORG_NAME = new BallerinaCompositeElementType("ORG_NAME");
  IElementType OTHER_TYPE_BODY = new BallerinaCompositeElementType("OTHER_TYPE_BODY");
  IElementType PACKAGE_NAME = new BallerinaCompositeElementType("PACKAGE_NAME");
  IElementType PACKAGE_REFERENCE = new BallerinaCompositeElementType("PACKAGE_REFERENCE");
  IElementType PACKAGE_VERSION = new BallerinaCompositeElementType("PACKAGE_VERSION");
  IElementType PARAMETER_DESCRIPTION = new BallerinaCompositeElementType("PARAMETER_DESCRIPTION");
  IElementType PARAMETER_DOCUMENTATION = new BallerinaCompositeElementType("PARAMETER_DOCUMENTATION");
  IElementType PARAMETER_DOCUMENTATION_LINE = new BallerinaCompositeElementType("PARAMETER_DOCUMENTATION_LINE");
  IElementType RECOVERABLE_ANNOTATION_CONTENT = new BallerinaCompositeElementType("RECOVERABLE_ANNOTATION_CONTENT");
  IElementType RECOVERABLE_ATTACHMENT_CONTENT = new BallerinaCompositeElementType("RECOVERABLE_ATTACHMENT_CONTENT");
  IElementType RECOVERABLE_BODY = new BallerinaCompositeElementType("RECOVERABLE_BODY");
  IElementType RECOVERABLE_BODY_CONTENT = new BallerinaCompositeElementType("RECOVERABLE_BODY_CONTENT");
  IElementType RECOVERABLE_CLOSE_RECORD_CONTENT = new BallerinaCompositeElementType("RECOVERABLE_CLOSE_RECORD_CONTENT");
  IElementType RECOVERABLE_CONSTANT_CONTENT = new BallerinaCompositeElementType("RECOVERABLE_CONSTANT_CONTENT");
  IElementType RECOVERABLE_OPEN_RECORD_CONTENT = new BallerinaCompositeElementType("RECOVERABLE_OPEN_RECORD_CONTENT");
  IElementType RECOVERABLE_PARAMETER_CONTENT = new BallerinaCompositeElementType("RECOVERABLE_PARAMETER_CONTENT");
  IElementType RECOVERABLE_RETURN_TYPE = new BallerinaCompositeElementType("RECOVERABLE_RETURN_TYPE");
  IElementType RECOVERABLE_TYPE_CONTENT = new BallerinaCompositeElementType("RECOVERABLE_TYPE_CONTENT");
  IElementType RECOVERABLE_VARIABLE_DEFINITION_CONTENT = new BallerinaCompositeElementType("RECOVERABLE_VARIABLE_DEFINITION_CONTENT");
  IElementType RECOVERABLE_VAR_DEF_CONTENT = new BallerinaCompositeElementType("RECOVERABLE_VAR_DEF_CONTENT");
  IElementType REFERENCE_TYPE = new BallerinaCompositeElementType("REFERENCE_TYPE");
  IElementType REFERENCE_TYPE_NAME = new BallerinaCompositeElementType("REFERENCE_TYPE_NAME");
  IElementType RETURN_PARAMETER_DESCRIPTION = new BallerinaCompositeElementType("RETURN_PARAMETER_DESCRIPTION");
  IElementType RETURN_PARAMETER_DOCUMENTATION = new BallerinaCompositeElementType("RETURN_PARAMETER_DOCUMENTATION");
  IElementType RETURN_PARAMETER_DOCUMENTATION_LINE = new BallerinaCompositeElementType("RETURN_PARAMETER_DOCUMENTATION_LINE");
  IElementType SERVICE_DEFINITION = new BallerinaCompositeElementType("SERVICE_DEFINITION");
  IElementType SERVICE_DEFINITION_BODY = new BallerinaCompositeElementType("SERVICE_DEFINITION_BODY");
  IElementType SERVICE_TYPE_NAME = new BallerinaCompositeElementType("SERVICE_TYPE_NAME");
  IElementType SIMPLE_TYPE_NAME = new BallerinaCompositeElementType("SIMPLE_TYPE_NAME");
  IElementType SINGLE_BACKTICKED_BLOCK = new BallerinaCompositeElementType("SINGLE_BACKTICKED_BLOCK");
  IElementType STREAM_TYPE_NAME = new BallerinaCompositeElementType("STREAM_TYPE_NAME");
  IElementType TABLE_TYPE_NAME = new BallerinaCompositeElementType("TABLE_TYPE_NAME");
  IElementType TRIPLE_BACKTICKED_BLOCK = new BallerinaCompositeElementType("TRIPLE_BACKTICKED_BLOCK");
  IElementType TUPLE_REST_DESCRIPTOR = new BallerinaCompositeElementType("TUPLE_REST_DESCRIPTOR");
  IElementType TUPLE_TYPE_NAME = new BallerinaCompositeElementType("TUPLE_TYPE_NAME");
  IElementType TYPE_BODY = new BallerinaCompositeElementType("TYPE_BODY");
  IElementType TYPE_DEFINITION = new BallerinaCompositeElementType("TYPE_DEFINITION");
  IElementType TYPE_DESC_REFERENCE_TYPE_NAME = new BallerinaCompositeElementType("TYPE_DESC_REFERENCE_TYPE_NAME");
  IElementType TYPE_NAME = new BallerinaCompositeElementType("TYPE_NAME");
  IElementType TYPE_REFERENCE = new BallerinaCompositeElementType("TYPE_REFERENCE");
  IElementType UNION_TYPE_NAME = new BallerinaCompositeElementType("UNION_TYPE_NAME");
  IElementType USER_DEFINE_TYPE_NAME = new BallerinaCompositeElementType("USER_DEFINE_TYPE_NAME");
  IElementType VALUE_TYPE_NAME = new BallerinaCompositeElementType("VALUE_TYPE_NAME");
  IElementType VERSION_PATTERN = new BallerinaCompositeElementType("VERSION_PATTERN");
  IElementType XML_TYPE_NAME = new BallerinaCompositeElementType("XML_TYPE_NAME");

  IElementType ABORT = new BallerinaTokenType("abort");
  IElementType ABORTED = new BallerinaTokenType("aborted");
  IElementType ABSTRACT = new BallerinaTokenType("abstract");
  IElementType ADD = new BallerinaTokenType("+");
  IElementType AND = new BallerinaTokenType("&&");
  IElementType ANNOTATION = new BallerinaTokenType("annotation");
  IElementType ANNOTATION_ACCESS = new BallerinaTokenType("ANNOTATION_ACCESS");
  IElementType ANY = new BallerinaTokenType("any");
  IElementType ANYDATA = new BallerinaTokenType("anydata");
  IElementType AS = new BallerinaTokenType("as");
  IElementType ASSIGN = new BallerinaTokenType("=");
  IElementType AT = new BallerinaTokenType("@");
  IElementType BACKTICK = new BallerinaTokenType("`");
  IElementType BASE_16_BLOB_LITERAL = new BallerinaTokenType("BASE_16_BLOB_LITERAL");
  IElementType BASE_64_BLOB_LITERAL = new BallerinaTokenType("BASE_64_BLOB_LITERAL");
  IElementType BINARY_INTEGER_LITERAL = new BallerinaTokenType("BINARY_INTEGER_LITERAL");
  IElementType BITAND = new BallerinaTokenType("BITAND");
  IElementType BITXOR = new BallerinaTokenType("BITXOR");
  IElementType BIT_COMPLEMENT = new BallerinaTokenType("BIT_COMPLEMENT");
  IElementType BOOLEAN = new BallerinaTokenType("boolean");
  IElementType BOOLEAN_LITERAL = new BallerinaTokenType("BOOLEAN_LITERAL");
  IElementType BREAK = new BallerinaTokenType("break");
  IElementType BYTE = new BallerinaTokenType("byte");
  IElementType CATCH = new BallerinaTokenType("catch");
  IElementType CHANNEL = new BallerinaTokenType("channel");
  IElementType CHECK = new BallerinaTokenType("check");
  IElementType CHECKPANIC = new BallerinaTokenType("checkpanic");
  IElementType CLIENT = new BallerinaTokenType("client");
  IElementType COLON = new BallerinaTokenType(":");
  IElementType COMMA = new BallerinaTokenType(",");
  IElementType COMMITTED = new BallerinaTokenType("committed");
  IElementType COMPOUND_ADD = new BallerinaTokenType("+=");
  IElementType COMPOUND_BIT_AND = new BallerinaTokenType("COMPOUND_BIT_AND");
  IElementType COMPOUND_BIT_OR = new BallerinaTokenType("COMPOUND_BIT_OR");
  IElementType COMPOUND_BIT_XOR = new BallerinaTokenType("COMPOUND_BIT_XOR");
  IElementType COMPOUND_DIV = new BallerinaTokenType("/=");
  IElementType COMPOUND_LEFT_SHIFT = new BallerinaTokenType("COMPOUND_LEFT_SHIFT");
  IElementType COMPOUND_LOGICAL_SHIFT = new BallerinaTokenType("COMPOUND_LOGICAL_SHIFT");
  IElementType COMPOUND_MUL = new BallerinaTokenType("*=");
  IElementType COMPOUND_RIGHT_SHIFT = new BallerinaTokenType("COMPOUND_RIGHT_SHIFT");
  IElementType COMPOUND_SUB = new BallerinaTokenType("-=");
  IElementType CONST = new BallerinaTokenType("const");
  IElementType CONTINUE = new BallerinaTokenType("continue");
  IElementType DECIMAL = new BallerinaTokenType("decimal");
  IElementType DECIMAL_EXTENDED_FLOATING_POINT_NUMBER = new BallerinaTokenType("DECIMAL_EXTENDED_FLOATING_POINT_NUMBER");
  IElementType DECIMAL_FLOATING_POINT_NUMBER = new BallerinaTokenType("DECIMAL_FLOATING_POINT_NUMBER");
  IElementType DECIMAL_INTEGER_LITERAL = new BallerinaTokenType("DECIMAL_INTEGER_LITERAL");
  IElementType DECREMENT = new BallerinaTokenType("--");
  IElementType DEFAULT = new BallerinaTokenType("DEFAULT");
  IElementType DEPRECATED_DOCUMENTATION = new BallerinaTokenType("DEPRECATED_DOCUMENTATION");
  IElementType DEPRECATED_PARAMETER_DOCUMENTATION = new BallerinaTokenType("DEPRECATED_PARAMETER_DOCUMENTATION");
  IElementType DESCRIPTION_SEPARATOR = new BallerinaTokenType("DESCRIPTION_SEPARATOR");
  IElementType DIV = new BallerinaTokenType("/");
  IElementType DO = new BallerinaTokenType("do");
  IElementType DOCANNOTATION = new BallerinaTokenType("DOCANNOTATION");
  IElementType DOCCONST = new BallerinaTokenType("DOCCONST");
  IElementType DOCFUNCTION = new BallerinaTokenType("DOCFUNCTION");
  IElementType DOCMODULE = new BallerinaTokenType("DOCMODULE");
  IElementType DOCPARAMETER = new BallerinaTokenType("DOCPARAMETER");
  IElementType DOCSERVICE = new BallerinaTokenType("DOCSERVICE");
  IElementType DOCTYPE = new BallerinaTokenType("DOCTYPE");
  IElementType DOCUMENTATION_ESCAPED_CHARACTERS = new BallerinaTokenType("DOCUMENTATION_ESCAPED_CHARACTERS");
  IElementType DOCVAR = new BallerinaTokenType("DOCVAR");
  IElementType DOCVARIABLE = new BallerinaTokenType("DOCVARIABLE");
  IElementType DOT = new BallerinaTokenType(".");
  IElementType DOUBLE_BACKTICK_CONTENT = new BallerinaTokenType("DOUBLE_BACKTICK_CONTENT");
  IElementType DOUBLE_BACKTICK_MARKDOWN_END = new BallerinaTokenType("DOUBLE_BACKTICK_MARKDOWN_END");
  IElementType DOUBLE_BACKTICK_MARKDOWN_START = new BallerinaTokenType("DOUBLE_BACKTICK_MARKDOWN_START");
  IElementType DOUBLE_COLON = new BallerinaTokenType("::");
  IElementType ELLIPSIS = new BallerinaTokenType("...");
  IElementType ELSE = new BallerinaTokenType("else");
  IElementType ELVIS = new BallerinaTokenType("ELVIS");
  IElementType ENUM = new BallerinaTokenType("enum");
  IElementType EQUAL = new BallerinaTokenType("==");
  IElementType EQUAL_GT = new BallerinaTokenType("=>");
  IElementType ERROR = new BallerinaTokenType("error");
  IElementType EXTERNAL = new BallerinaTokenType("external");
  IElementType FAIL = new BallerinaTokenType("fail");
  IElementType FINAL = new BallerinaTokenType("final");
  IElementType FINALLY = new BallerinaTokenType("finally");
  IElementType FLOAT = new BallerinaTokenType("float");
  IElementType FLUSH = new BallerinaTokenType("flush");
  IElementType FOREACH = new BallerinaTokenType("foreach");
  IElementType FOREVER = new BallerinaTokenType("forever");
  IElementType FORK = new BallerinaTokenType("fork");
  IElementType FROM = new BallerinaTokenType("from");
  IElementType FUNCTION = new BallerinaTokenType("function");
  IElementType FUTURE = new BallerinaTokenType("future");
  IElementType GT = new BallerinaTokenType(">");
  IElementType GT_EQUAL = new BallerinaTokenType(">=");
  IElementType HALF_OPEN_RANGE = new BallerinaTokenType("HALF_OPEN_RANGE");
  IElementType HANDLE = new BallerinaTokenType("handle");
  IElementType HEXADECIMAL_FLOATING_POINT_LITERAL = new BallerinaTokenType("HEXADECIMAL_FLOATING_POINT_LITERAL");
  IElementType HEX_INTEGER_LITERAL = new BallerinaTokenType("HEX_INTEGER_LITERAL");
  IElementType IDENTIFIER = new BallerinaTokenType("identifier");
  IElementType IF = new BallerinaTokenType("if");
  IElementType IGNORED_LEFT_BRACE = new BallerinaTokenType("IGNORED_LEFT_BRACE");
  IElementType IGNORED_RIGHT_BRACE = new BallerinaTokenType("IGNORED_RIGHT_BRACE");
  IElementType IMPORT = new BallerinaTokenType("import");
  IElementType IN = new BallerinaTokenType("in");
  IElementType INCREMENT = new BallerinaTokenType("++");
  IElementType INT = new BallerinaTokenType("int");
  IElementType IS = new BallerinaTokenType("is");
  IElementType JSON = new BallerinaTokenType("json");
  IElementType LARROW = new BallerinaTokenType("<-");
  IElementType LEFT_BRACE = new BallerinaTokenType("{");
  IElementType LEFT_BRACKET = new BallerinaTokenType("[");
  IElementType LEFT_CLOSED_RECORD_DELIMITER = new BallerinaTokenType("LEFT_CLOSED_RECORD_DELIMITER");
  IElementType LEFT_PARENTHESIS = new BallerinaTokenType("(");
  IElementType LET = new BallerinaTokenType("let");
  IElementType LINE_COMMENT = new BallerinaTokenType("LINE_COMMENT");
  IElementType LISTENER = new BallerinaTokenType("listener");
  IElementType LOCK = new BallerinaTokenType("lock");
  IElementType LT = new BallerinaTokenType("<");
  IElementType LT_EQUAL = new BallerinaTokenType("<=");
  IElementType MAP = new BallerinaTokenType("map");
  IElementType MARKDOWN_DOCUMENTATION_LINE_START = new BallerinaTokenType("MARKDOWN_DOCUMENTATION_LINE_START");
  IElementType MARKDOWN_DOCUMENTATION_TEXT = new BallerinaTokenType("MARKDOWN_DOCUMENTATION_TEXT");
  IElementType MATCH = new BallerinaTokenType("match");
  IElementType MOD = new BallerinaTokenType("%");
  IElementType MUL = new BallerinaTokenType("*");
  IElementType NESTED_LEFT_BRACE = new BallerinaTokenType("NESTED_LEFT_BRACE");
  IElementType NESTED_RIGHT_BRACE = new BallerinaTokenType("NESTED_RIGHT_BRACE");
  IElementType NEW = new BallerinaTokenType("new");
  IElementType NOT = new BallerinaTokenType("!");
  IElementType NOT_EQUAL = new BallerinaTokenType("!=");
  IElementType NULL_LITERAL = new BallerinaTokenType("NULL_LITERAL");
  IElementType OBJECT = new BallerinaTokenType("object");
  IElementType OBJECT_INIT = new BallerinaTokenType("OBJECT_INIT");
  IElementType OCTAL_INTEGER_LITERAL = new BallerinaTokenType("OCTAL_INTEGER_LITERAL");
  IElementType ON = new BallerinaTokenType("on");
  IElementType ONRETRY = new BallerinaTokenType("onretry");
  IElementType OPTIONAL_FIELD_ACCESS = new BallerinaTokenType("OPTIONAL_FIELD_ACCESS");
  IElementType OR = new BallerinaTokenType("||");
  IElementType PANIC = new BallerinaTokenType("panic");
  IElementType PARAMETER = new BallerinaTokenType("parameter");
  IElementType PARAMETER_DOCUMENTATION_START = new BallerinaTokenType("PARAMETER_DOCUMENTATION_START");
  IElementType PARAMETER_NAME = new BallerinaTokenType("PARAMETER_NAME");
  IElementType PIPE = new BallerinaTokenType("|");
  IElementType POW = new BallerinaTokenType("^");
  IElementType PRIVATE = new BallerinaTokenType("private");
  IElementType PUBLIC = new BallerinaTokenType("public");
  IElementType QUESTION_MARK = new BallerinaTokenType("?");
  IElementType QUOTED_STRING_LITERAL = new BallerinaTokenType("QUOTED_STRING_LITERAL");
  IElementType RANGE = new BallerinaTokenType("..");
  IElementType RARROW = new BallerinaTokenType("->");
  IElementType RECORD = new BallerinaTokenType("record");
  IElementType REF_EQUAL = new BallerinaTokenType("===");
  IElementType REF_NOT_EQUAL = new BallerinaTokenType("!==");
  IElementType REMOTE = new BallerinaTokenType("remote");
  IElementType RESOURCE = new BallerinaTokenType("resource");
  IElementType RETRIES = new BallerinaTokenType("retries");
  IElementType RETRY = new BallerinaTokenType("retry");
  IElementType RETURN = new BallerinaTokenType("return");
  IElementType RETURNS = new BallerinaTokenType("returns");
  IElementType RETURN_PARAMETER_DOCUMENTATION_START = new BallerinaTokenType("RETURN_PARAMETER_DOCUMENTATION_START");
  IElementType RIGHT_BRACE = new BallerinaTokenType("}");
  IElementType RIGHT_BRACKET = new BallerinaTokenType("]");
  IElementType RIGHT_CLOSED_RECORD_DELIMITER = new BallerinaTokenType("RIGHT_CLOSED_RECORD_DELIMITER");
  IElementType RIGHT_PARENTHESIS = new BallerinaTokenType(")");
  IElementType SAFE_ASSIGNMENT = new BallerinaTokenType("=?");
  IElementType SELECT = new BallerinaTokenType("select");
  IElementType SEMICOLON = new BallerinaTokenType(";");
  IElementType SERVICE = new BallerinaTokenType("service");
  IElementType SINGLE_BACKTICK_CONTENT = new BallerinaTokenType("SINGLE_BACKTICK_CONTENT");
  IElementType SINGLE_BACKTICK_MARKDOWN_END = new BallerinaTokenType("SINGLE_BACKTICK_MARKDOWN_END");
  IElementType SINGLE_BACKTICK_MARKDOWN_START = new BallerinaTokenType("SINGLE_BACKTICK_MARKDOWN_START");
  IElementType SOURCE = new BallerinaTokenType("source");
  IElementType START = new BallerinaTokenType("start");
  IElementType STREAM = new BallerinaTokenType("stream");
  IElementType STRING = new BallerinaTokenType("string");
  IElementType STRING_TEMPLATE_EXPRESSION_END = new BallerinaTokenType("STRING_TEMPLATE_EXPRESSION_END");
  IElementType STRING_TEMPLATE_EXPRESSION_START = new BallerinaTokenType("STRING_TEMPLATE_EXPRESSION_START");
  IElementType STRING_TEMPLATE_LITERAL_END = new BallerinaTokenType("STRING_TEMPLATE_LITERAL_END");
  IElementType STRING_TEMPLATE_LITERAL_START = new BallerinaTokenType("STRING_TEMPLATE_LITERAL_START");
  IElementType STRING_TEMPLATE_TEXT = new BallerinaTokenType("STRING_TEMPLATE_TEXT");
  IElementType SUB = new BallerinaTokenType("-");
  IElementType SYNCRARROW = new BallerinaTokenType("SYNCRARROW");
  IElementType TABLE = new BallerinaTokenType("table");
  IElementType THROW = new BallerinaTokenType("throw");
  IElementType TRANSACTION = new BallerinaTokenType("transaction");
  IElementType TRAP = new BallerinaTokenType("trap");
  IElementType TRIPLE_BACKTICK_CONTENT = new BallerinaTokenType("TRIPLE_BACKTICK_CONTENT");
  IElementType TRIPLE_BACKTICK_MARKDOWN_END = new BallerinaTokenType("TRIPLE_BACKTICK_MARKDOWN_END");
  IElementType TRIPLE_BACKTICK_MARKDOWN_START = new BallerinaTokenType("TRIPLE_BACKTICK_MARKDOWN_START");
  IElementType TRY = new BallerinaTokenType("try");
  IElementType TYPE = new BallerinaTokenType("type");
  IElementType TYPEDESC = new BallerinaTokenType("typedesc");
  IElementType TYPEOF = new BallerinaTokenType("typeof");
  IElementType TYPE_FIELD = new BallerinaTokenType("TYPE_FIELD");
  IElementType TYPE_PARAMETER = new BallerinaTokenType("TYPE_PARAMETER");
  IElementType VAR = new BallerinaTokenType("var");
  IElementType VERSION = new BallerinaTokenType("version");
  IElementType WAIT = new BallerinaTokenType("wait");
  IElementType WHERE = new BallerinaTokenType("where");
  IElementType WHILE = new BallerinaTokenType("while");
  IElementType WITH = new BallerinaTokenType("with");
  IElementType WORKER = new BallerinaTokenType("worker");
  IElementType XML = new BallerinaTokenType("xml");
  IElementType XMLNS = new BallerinaTokenType("xmlns");
  IElementType XML_ALL_CHAR = new BallerinaTokenType("XML_ALL_CHAR");
  IElementType XML_LITERAL_END = new BallerinaTokenType("XML_LITERAL_END");
  IElementType XML_LITERAL_START = new BallerinaTokenType("XML_LITERAL_START");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ALIAS) {
        return new BallerinaAliasImpl(node);
      }
      else if (type == ANNOTATION_ATTACHMENT) {
        return new BallerinaAnnotationAttachmentImpl(node);
      }
      else if (type == ANNOTATION_DEFINITION) {
        return new BallerinaAnnotationDefinitionImpl(node);
      }
      else if (type == ANY_IDENTIFIER_NAME) {
        return new BallerinaAnyIdentifierNameImpl(node);
      }
      else if (type == ARRAY_TYPE_NAME) {
        return new BallerinaArrayTypeNameImpl(node);
      }
      else if (type == BACKTICKED_BLOCK) {
        return new BallerinaBacktickedBlockImpl(node);
      }
      else if (type == BUILT_IN_REFERENCE_TYPE_NAME) {
        return new BallerinaBuiltInReferenceTypeNameImpl(node);
      }
      else if (type == CLOSE_RECORD_TYPE_BODY) {
        return new BallerinaCloseRecordTypeBodyImpl(node);
      }
      else if (type == COMPLETE_PACKAGE_NAME) {
        return new BallerinaCompletePackageNameImpl(node);
      }
      else if (type == CONSTANT_DEFINITION) {
        return new BallerinaConstantDefinitionImpl(node);
      }
      else if (type == DEFINITION) {
        return new BallerinaDefinitionImpl(node);
      }
      else if (type == DEPRECATED_ANNOTATION_DOCUMENTATION) {
        return new BallerinaDeprecatedAnnotationDocumentationImpl(node);
      }
      else if (type == DEPRECATED_ANNOTATION_DOCUMENTATION_LINE) {
        return new BallerinaDeprecatedAnnotationDocumentationLineImpl(node);
      }
      else if (type == DEPRECATED_PARAMETERS_DOCUMENTATION) {
        return new BallerinaDeprecatedParametersDocumentationImpl(node);
      }
      else if (type == DEPRECATED_PARAMETERS_DOCUMENTATION_LINE) {
        return new BallerinaDeprecatedParametersDocumentationLineImpl(node);
      }
      else if (type == DEPRECATE_ANNOTATION_DESCRIPTION_LINE) {
        return new BallerinaDeprecateAnnotationDescriptionLineImpl(node);
      }
      else if (type == DOCUMENTATION_CONTENT) {
        return new BallerinaDocumentationContentImpl(node);
      }
      else if (type == DOCUMENTATION_LINE) {
        return new BallerinaDocumentationLineImpl(node);
      }
      else if (type == DOCUMENTATION_REFERENCE) {
        return new BallerinaDocumentationReferenceImpl(node);
      }
      else if (type == DOCUMENTATION_STRING) {
        return new BallerinaDocumentationStringImpl(node);
      }
      else if (type == DOCUMENTATION_TEXT) {
        return new BallerinaDocumentationTextImpl(node);
      }
      else if (type == DOCUMENTATION_TEXT_CONTENT) {
        return new BallerinaDocumentationTextContentImpl(node);
      }
      else if (type == DOC_PARAMETER_DESCRIPTION) {
        return new BallerinaDocParameterDescriptionImpl(node);
      }
      else if (type == DOUBLE_BACKTICKED_BLOCK) {
        return new BallerinaDoubleBacktickedBlockImpl(node);
      }
      else if (type == ERROR_TYPE_NAME) {
        return new BallerinaErrorTypeNameImpl(node);
      }
      else if (type == EXCLUSIVE_RECORD_TYPE_NAME) {
        return new BallerinaExclusiveRecordTypeNameImpl(node);
      }
      else if (type == EXPR_FUNCTION_BODY_SPEC) {
        return new BallerinaExprFunctionBodySpecImpl(node);
      }
      else if (type == EXTERNAL_FUNCTION_BODY) {
        return new BallerinaExternalFunctionBodyImpl(node);
      }
      else if (type == FUNCTION_DEFINITION) {
        return new BallerinaFunctionDefinitionImpl(node);
      }
      else if (type == FUNCTION_DEFINITION_BODY) {
        return new BallerinaFunctionDefinitionBodyImpl(node);
      }
      else if (type == FUNCTION_SIGNATURE) {
        return new BallerinaFunctionSignatureImpl(node);
      }
      else if (type == FUNCTION_TYPE_NAME) {
        return new BallerinaFunctionTypeNameImpl(node);
      }
      else if (type == FUTURE_TYPE_NAME) {
        return new BallerinaFutureTypeNameImpl(node);
      }
      else if (type == GLOBAL_VARIABLE_DEFINITION) {
        return new BallerinaGlobalVariableDefinitionImpl(node);
      }
      else if (type == GROUP_TYPE_NAME) {
        return new BallerinaGroupTypeNameImpl(node);
      }
      else if (type == IMPORT_DECLARATION) {
        return new BallerinaImportDeclarationImpl(node);
      }
      else if (type == INCLUSIVE_RECORD_TYPE_NAME) {
        return new BallerinaInclusiveRecordTypeNameImpl(node);
      }
      else if (type == INTEGER_LITERAL) {
        return new BallerinaIntegerLiteralImpl(node);
      }
      else if (type == JSON_TYPE_NAME) {
        return new BallerinaJsonTypeNameImpl(node);
      }
      else if (type == MAP_TYPE_NAME) {
        return new BallerinaMapTypeNameImpl(node);
      }
      else if (type == METHOD_DECLARATION) {
        return new BallerinaMethodDeclarationImpl(node);
      }
      else if (type == METHOD_DEFINITION) {
        return new BallerinaMethodDefinitionImpl(node);
      }
      else if (type == NAMESPACE_DECLARATION) {
        return new BallerinaNamespaceDeclarationImpl(node);
      }
      else if (type == NAME_REFERENCE) {
        return new BallerinaNameReferenceImpl(node);
      }
      else if (type == NESTED_ANNOTATION_ATTACHMENT) {
        return new BallerinaNestedAnnotationAttachmentImpl(node);
      }
      else if (type == NESTED_FUNCTION_SIGNATURE) {
        return new BallerinaNestedFunctionSignatureImpl(node);
      }
      else if (type == NESTED_RECOVERABLE_BODY) {
        return new BallerinaNestedRecoverableBodyImpl(node);
      }
      else if (type == NESTED_RECOVERABLE_BODY_CONTENT) {
        return new BallerinaNestedRecoverableBodyContentImpl(node);
      }
      else if (type == NESTED_RECOVERABLE_RETURN_TYPE) {
        return new BallerinaNestedRecoverableReturnTypeImpl(node);
      }
      else if (type == NIL_LITERAL) {
        return new BallerinaNilLiteralImpl(node);
      }
      else if (type == NULLABLE_TYPE_NAME) {
        return new BallerinaNullableTypeNameImpl(node);
      }
      else if (type == OBJECT_BODY) {
        return new BallerinaObjectBodyImpl(node);
      }
      else if (type == OBJECT_BODY_CONTENT) {
        return new BallerinaObjectBodyContentImpl(node);
      }
      else if (type == OBJECT_FIELD_DEFINITION) {
        return new BallerinaObjectFieldDefinitionImpl(node);
      }
      else if (type == OBJECT_FIELD_DEFINITION_CONTENT) {
        return new BallerinaObjectFieldDefinitionContentImpl(node);
      }
      else if (type == OBJECT_METHOD) {
        return new BallerinaObjectMethodImpl(node);
      }
      else if (type == OBJECT_TYPE_BODY) {
        return new BallerinaObjectTypeBodyImpl(node);
      }
      else if (type == OBJECT_TYPE_NAME) {
        return new BallerinaObjectTypeNameImpl(node);
      }
      else if (type == OPEN_RECORD_TYPE_BODY) {
        return new BallerinaOpenRecordTypeBodyImpl(node);
      }
      else if (type == ORG_NAME) {
        return new BallerinaOrgNameImpl(node);
      }
      else if (type == OTHER_TYPE_BODY) {
        return new BallerinaOtherTypeBodyImpl(node);
      }
      else if (type == PACKAGE_NAME) {
        return new BallerinaPackageNameImpl(node);
      }
      else if (type == PACKAGE_REFERENCE) {
        return new BallerinaPackageReferenceImpl(node);
      }
      else if (type == PACKAGE_VERSION) {
        return new BallerinaPackageVersionImpl(node);
      }
      else if (type == PARAMETER_DESCRIPTION) {
        return new BallerinaParameterDescriptionImpl(node);
      }
      else if (type == PARAMETER_DOCUMENTATION) {
        return new BallerinaParameterDocumentationImpl(node);
      }
      else if (type == PARAMETER_DOCUMENTATION_LINE) {
        return new BallerinaParameterDocumentationLineImpl(node);
      }
      else if (type == RECOVERABLE_ANNOTATION_CONTENT) {
        return new BallerinaRecoverableAnnotationContentImpl(node);
      }
      else if (type == RECOVERABLE_ATTACHMENT_CONTENT) {
        return new BallerinaRecoverableAttachmentContentImpl(node);
      }
      else if (type == RECOVERABLE_BODY) {
        return new BallerinaRecoverableBodyImpl(node);
      }
      else if (type == RECOVERABLE_BODY_CONTENT) {
        return new BallerinaRecoverableBodyContentImpl(node);
      }
      else if (type == RECOVERABLE_CLOSE_RECORD_CONTENT) {
        return new BallerinaRecoverableCloseRecordContentImpl(node);
      }
      else if (type == RECOVERABLE_CONSTANT_CONTENT) {
        return new BallerinaRecoverableConstantContentImpl(node);
      }
      else if (type == RECOVERABLE_OPEN_RECORD_CONTENT) {
        return new BallerinaRecoverableOpenRecordContentImpl(node);
      }
      else if (type == RECOVERABLE_PARAMETER_CONTENT) {
        return new BallerinaRecoverableParameterContentImpl(node);
      }
      else if (type == RECOVERABLE_RETURN_TYPE) {
        return new BallerinaRecoverableReturnTypeImpl(node);
      }
      else if (type == RECOVERABLE_TYPE_CONTENT) {
        return new BallerinaRecoverableTypeContentImpl(node);
      }
      else if (type == RECOVERABLE_VARIABLE_DEFINITION_CONTENT) {
        return new BallerinaRecoverableVariableDefinitionContentImpl(node);
      }
      else if (type == RECOVERABLE_VAR_DEF_CONTENT) {
        return new BallerinaRecoverableVarDefContentImpl(node);
      }
      else if (type == REFERENCE_TYPE) {
        return new BallerinaReferenceTypeImpl(node);
      }
      else if (type == REFERENCE_TYPE_NAME) {
        return new BallerinaReferenceTypeNameImpl(node);
      }
      else if (type == RETURN_PARAMETER_DESCRIPTION) {
        return new BallerinaReturnParameterDescriptionImpl(node);
      }
      else if (type == RETURN_PARAMETER_DOCUMENTATION) {
        return new BallerinaReturnParameterDocumentationImpl(node);
      }
      else if (type == RETURN_PARAMETER_DOCUMENTATION_LINE) {
        return new BallerinaReturnParameterDocumentationLineImpl(node);
      }
      else if (type == SERVICE_DEFINITION) {
        return new BallerinaServiceDefinitionImpl(node);
      }
      else if (type == SERVICE_DEFINITION_BODY) {
        return new BallerinaServiceDefinitionBodyImpl(node);
      }
      else if (type == SERVICE_TYPE_NAME) {
        return new BallerinaServiceTypeNameImpl(node);
      }
      else if (type == SIMPLE_TYPE_NAME) {
        return new BallerinaSimpleTypeNameImpl(node);
      }
      else if (type == SINGLE_BACKTICKED_BLOCK) {
        return new BallerinaSingleBacktickedBlockImpl(node);
      }
      else if (type == STREAM_TYPE_NAME) {
        return new BallerinaStreamTypeNameImpl(node);
      }
      else if (type == TABLE_TYPE_NAME) {
        return new BallerinaTableTypeNameImpl(node);
      }
      else if (type == TRIPLE_BACKTICKED_BLOCK) {
        return new BallerinaTripleBacktickedBlockImpl(node);
      }
      else if (type == TUPLE_REST_DESCRIPTOR) {
        return new BallerinaTupleRestDescriptorImpl(node);
      }
      else if (type == TUPLE_TYPE_NAME) {
        return new BallerinaTupleTypeNameImpl(node);
      }
      else if (type == TYPE_BODY) {
        return new BallerinaTypeBodyImpl(node);
      }
      else if (type == TYPE_DEFINITION) {
        return new BallerinaTypeDefinitionImpl(node);
      }
      else if (type == TYPE_DESC_REFERENCE_TYPE_NAME) {
        return new BallerinaTypeDescReferenceTypeNameImpl(node);
      }
      else if (type == TYPE_REFERENCE) {
        return new BallerinaTypeReferenceImpl(node);
      }
      else if (type == UNION_TYPE_NAME) {
        return new BallerinaUnionTypeNameImpl(node);
      }
      else if (type == USER_DEFINE_TYPE_NAME) {
        return new BallerinaUserDefineTypeNameImpl(node);
      }
      else if (type == VALUE_TYPE_NAME) {
        return new BallerinaValueTypeNameImpl(node);
      }
      else if (type == VERSION_PATTERN) {
        return new BallerinaVersionPatternImpl(node);
      }
      else if (type == XML_TYPE_NAME) {
        return new BallerinaXmlTypeNameImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
