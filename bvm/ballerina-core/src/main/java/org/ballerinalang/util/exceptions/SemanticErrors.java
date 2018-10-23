/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.util.exceptions;

/**
 * Error codes and Error keys to represent the semantic errors.
 */
public enum SemanticErrors {

    INVALID_TYPE("invalid.type", "SEMANTIC_0001"),
    INCOMPATIBLE_TYPES("incompatible.types", "SEMANTIC_0002"),
    REDECLARED_SYMBOL("redeclared.symbol", "SEMANTIC_0003"),
    INCOMPATIBLE_TYPES_CANNOT_CONVERT("incompatible.types.cannot.convert", "SEMANTIC_0004"),
    INCOMPATIBLE_TYPES_CANNOT_CAST("incompatible.types.cannot.cast", "SEMANTIC_0005"),
    UNREACHABLE_STATEMENT("unreachable.statement", "SEMANTIC_0006"),
    INCOMPATIBLE_TYPES_BOOLEAN_EXPECTED("incompatible.types.boolean.expected", "SEMANTIC_0007"),
    NO_STATEMENTS_WHILE_LOOP("no.statements.while.loop", "SEMANTIC_0008"),
    UNKNOWN_OPERATOR_IN_UNARY("unknown.operator.in.unary", "SEMANTIC_0009"),
    REF_TYPE_INTI_NOT_ALLOWED_HERE("ref.type.init.not.allowed.here", "SEMANTIC_0010"),
    STRUCT_MAP_INIT_NOT_ALLOWED("struct.map.init.not.allowed", "SEMANTIC_0011"),
    CONNECTOR_INIT_NOT_ALLOWED("connector.init.not.allowed", "SEMANTIC_0012"),
    ARRAY_INIT_NOT_ALLOWED_HERE("array.init.not.allowed.here", "SEMANTIC_0013"),
    INVALID_FIELD_NAME_STRUCT_INIT("invalid.field.name.struct.init", "SEMANTIC_0014"),
    UNKNOWN_FIELD_IN_STRUCT("unknown.field.in.struct", "SEMANTIC_0015"),
    INVALID_TYPE_IN_MAP_INDEX_EXPECTED_STRING("invalid.type.in.map.index.expected.string", "SEMANTIC_0016"),
    UNDEFINED_SYMBOL("undefined.symbol", "SEMANTIC_0017"),
    NON_INTEGER_ARRAY_INDEX("non.integer.array.index", "SEMANTIC_0018"),
    NON_STRING_MAP_INDEX("non.string.map.index", "SEMANTIC_0019"),
    INVALID_OPERATION_NOT_SUPPORT_INDEXING("invalid.operation.not.support.indexing", "SEMANTIC_0020"),
    MULTIPLE_VALUE_IN_SINGLE_VALUE_CONTEXT("multiple.value.in.single.value.context", "SEMANTIC_0021"),
    REPLY_STATEMENT_CANNOT_USED_IN_FUNCTION("reply.statement.cannot.used.in.function", "SEMANTIC_0022"),
    REPLY_STATEMENT_CANNOT_USED_IN_ACTION("reply.statement.cannot.used.in.action", "SEMANTIC_0023"),
    ACTION_INVOCATION_NOT_ALLOWED_IN_REPLY("action.invocation.not.allowed.in.reply", "SEMANTIC_0024"),
    ACTION_INVOCATION_NOT_ALLOWED_IN_RETURN("action.invocation.not.allowed.in.return", "SEMANTIC_0025"),
    RETURN_CANNOT_USED_IN_RESOURCE("return.cannot.used.in.resource",  "SEMANTIC_0026"),
    NOT_ENOUGH_ARGUMENTS_TO_RETURN("not.enough.arguments.to.return", "SEMANTIC_0027"),
    TOO_MANY_ARGUMENTS_TO_RETURN("too.many.arguments.to.return", "SEMANTIC_0028"),
    CANNOT_USE_TYPE_IN_RETURN_STATEMENT("cannot.use.type.in.return.statement", "SEMANTIC_0029"),
    CANNOT_USE_CREATE_FOR_VALUE_TYPES("cannot.use.create.for.value.types", "SEMANTIC_0030"),
    INCOMPATIBLE_TYPES_EXPECTED_XML("incompatible.types.expected.xml", "SEMANTIC_0031"),
    CANNOT_ASSIGN_VALUE_CONSTANT("cannot.assign.value.constant", "SEMANTIC_0032"),
    ASSIGNMENT_COUNT_MISMATCH("assignment.count.mismatch", "SEMANTIC_0033"),
    CANNOT_ASSIGN_IN_MULTIPLE_ASSIGNMENT("cannot.assign.in.multiple.assignment", "SEMANTIC_0034"),
    VAR_IS_REPEATED_ON_LEFT_SIDE_ASSIGNMENT("var.is.repeated.on.left.side.assignment", "SEMANTIC_0035"),
    UNDEFINED_FUNCTION("undefined.function", "SEMANTIC_0036"),
    UNDEFINED_CONNECTOR("undefined.connector", "SEMANTIC_0037"),
    UNDEFINED_ACTION("undefined.action", "SEMANTIC_0038"),
    INVALID_OPERATION_OPERATOR_NOT_DEFINED("invalid.operation.operator.not.defined", "SEMANTIC_0039"),
    STRUCT_NOT_FOUND("struct.not.found", "SEMANTIC_0040"),
    MUST_BE_STRUCT_TYPE("must.be.struct.type", "SEMANTIC_0041"),
    INVALID_OPERATION_INCOMPATIBLE_TYPES("invalid.operation.incompatible.types", "SEMANTIC_0042"),
    UNUSED_IMPORT_MODULE("unused.import.module", "SEMANTIC_0043"),
    REDECLARED_IMPORT_MODULE("redeclared.import.module", "SEMANTIC_0044"),
    UNSUPPORTED_OPERATOR("unsupported.operator", "SEMANTIC_0045"),
    ACTION_INVOCATION_NOT_ALLOWED_HERE("action.invocation.not.allowed.here", "SEMANTIC_0046"),
    UNDEFINED_MODULE("undefined.module.name", "SEMANTIC_0048"),
    TEMPLATE_EXPRESSION_NOT_ALLOWED_HERE("template.expression.not.allowed.here", "SEMANTIC_0049"),
    CONNECTOR_INIT_NOT_ALLOWED_HERE("connector.init.not.allowed.here", "SEMANTIC_0050"),
    ONLY_COUNT_1_ALLOWED_THIS_VERSION("only.count.1.allowed.this.version", "SEMANTIC_0051"),
    ONLY_ERROR_TYPE_ALLOWED_HERE("only.error.type.here", "SEMANTIC_0052"),
    BREAK_STMT_NOT_ALLOWED_HERE("break.stmt.not.allowed.here", "SEMANTIC_0053"),
    UNDEFINED_TYPE_MAPPER("undefined.type.mapper", "SEMANTIC_0054"),
    UNDEFINED_NATIVE_ACTION("undefined.native.action", "SEMANTIC_0055"),
    INCOMPATIBLE_TYPES_UNKNOWN_FOUND("incompatible.types.unknown.found", "SEMANTIC_0056"),
    BUILT_IN_TYPE_NAMES_NOT_ALLOWED_AS_IDENTIFIER("built.in.type.names.not.allowed.as.identifier", "SEMANTIC_0057"),
    INCOMPATIBLE_TYPES_CONNECTOR_EXPECTED("incompatible.types.connector.expected", "SEMANTIC_0058"),
    AMBIGUOUS_FUNCTIONS("ambiguous.functions", "SEMANTIC_0059"),
    UNSUPPORTED_ANNOTATION_ATTRIBUTE_VALUE("unsupported.annotation.attribute.value", "SEMANTIC_0060"),
    UNDEFINED_ANNOTATION("undefined.annotation", "SEMANTIC_0061"),
    ANNOTATION_NOT_ALLOWED("annotation.not.allowed", "SEMANTIC_0062"),
    NO_SUCH_ATTRIBUTE("no.such.attribute", "SEMANTIC_0063"),
    INVALID_DEFAULT_VALUE("invalid.default.value", "SEMANTIC_0064"),
    INVALID_ATTRIBUTE_TYPE("invalid.attribute.type", "SEMANTIC_0065"),
    INCOMPATIBLE_TYPES_ARRAY_FOUND("incompatible.types.array.found", "SEMANTIC_0066"),
    INCOMPATIBLE_ASSIGNMENT("incompatible.assignment", "SEMANTIC_0067"),
    INDEXING_NOT_SUPPORTED_FOR_MAP_ELEMENT("indexing.not.supported.map.element", "SEMANTIC_0068"),
    DYNAMIC_KEYS_NOT_SUPPORTED_FOR_STRUCT("dynamic.keys.not.supported.for.struct", "SEMANTIC_0069"),
    STRUCT_FIELD_CHILD_HAS_MODULE_IDENTIFIER("struct.field.child.has.module.identifier", "PARSER_0070"),
    CANNOT_ASSIGN_VALUE_ARRAY_LENGTH("cannot.assign.value.array.length", "SEMANTIC_0071"),
    MISSING_RETURN_STATEMENT("missing.return.statement", "SEMANTIC_0071"),
    TRANSFORM_STATEMENT_NO_BODY("transform.statement.no.body", "SEMANTIC_0072"),
    TRANSFORM_STATEMENT_INVALID_INPUT_OUTPUT("transform.statement.invalid.input.output", "SEMANTIC_0073"),
    ABORT_STMT_NOT_ALLOWED_HERE("abort.stmt.not.allowed.here", "SEMANTIC_0074"),
    RETURN_CANNOT_USED_IN_TRANSACTION("return.cannot.used.in.transaction",  "SEMANTIC_0075"),
    WORKER_INTERACTION_NOT_VALID("worker.interaction.not.valid", "SEMANTIC_0076"),
    INCOMPATIBLE_TYPES_IN_MULTIPLE_ASSIGNMENT("incompatible.types.in.multiple.assignment", "SEMANTIC_0077"),
    CANNOT_RESOLVE_STRUCT("cannot.resolve.struct", "SEMANTIC_0078"),
    CONTINUE_STMT_NOT_ALLOWED_HERE("continue.stmt.not.allowed.here", "SEMANTIC_0079"),
    DUPLICATED_ERROR_CATCH("duplicated.error.catch", "SEMANTIC_0080"),
    RESERVED_IDENTIFIER("reserved.identifier", "SEMANTIC_0081"),
    IGNORED_ASSIGNMENT("ignored.assignment", "SEMANTIC_0082"),
    REPLY_STMT_NOT_ALLOWED_HERE("reply.stmt.not.allowed.here", "SEMANTIC_0083"),
    RETURN_STMT_NOT_ALLOWED_HERE("return.stmt.not.allowed.here", "SEMANTIC_0084"),
    BREAK_USED_IN_TRANSACTION("break.used.in.transaction", "SEMANTIC_0085"),
    CONTINUE_USED_IN_TRANSACTION("continue.used.in.transaction", "SEMANTIC_0085"),
    REPLY_CANNOT_USED_IN_TRANSACTION("reply.cannot.used.in.transaction",  "SEMANTIC_0086"),
    UNSAFE_CAST_ATTEMPT("unsafe.cast.attempt", "SEMANTIC_0090"),
    UNSAFE_CONVERSION_ATTEMPT("unsafe.conversion.attempt", "SEMANTIC_0091"),
    INVALID_ACTION_INVOCATION("invalid.action.invocation", "SEMANTIC_0092"),
    INCORRECT_ACTION_INVOCATION("incorrect.action.invocation", "SEMANTIC_0093"),
    INVALID_VAR_ASSIGNMENT("invalid.var.assignment", "SEMANTIC_0094"),
    CANNOT_CONVERT_WITH_SUGGESTION("cannot.convert.with.suggestion", "SEMANTIC_0095"),
    CANNOT_CAST_WITH_SUGGESTION("cannot.cast.with.suggestion", "SEMANTIC_0096"),
    NO_NEW_VARIABLES_VAR_ASSIGNMENT("no.new.variables.var.assignment", "SEMANTIC_0097"),
    XML_ATTRIBUTE_MAP_UPDATE_NOT_ALLOWED("xml.attribute.map.update.not.allowed", "SEMANTIC_0098"),
    XML_QNAME_UPDATE_NOT_ALLOWED("xml.qname.update.not.allowed", "SEMANTIC_0099"),
    UNDEFINED_NAMESPACE("undefined.namespace", "SEMANTIC_0100"),
    ATTRIBUTE_VAL_CANNOT_REFER_NON_CONST("annotation.attribute.value.cannot.refer.non.constant", "SEMANTIC_0101"),
    INVALID_NAMESPACE_PREFIX("invalid.namespace.prefix", "SEMANTIC_0101"),
    XML_TAGS_MISMATCH("mismatching.xml.start.end.tags", "SEMANTIC_0102"),
    INCOMPATIBLE_TYPES_IN_XML_TEMPLATE("incompatible.types.in.xml.template", "SEMANTIC_0103"),
    INVALID_NAMESPACE_DECLARATION("invalid.namespace.declaration", "SEMANTIC_0104"),
    INCORRECT_FUNCTION_ARGUMENTS("incorrect.function.arguments", "SEMANTIC_105"),
    CONNECTOR_TYPES_NOT_EQUIVALENT("connector.types.not.equivalent", "SEMANTIC_0106"),
    FILTER_CONNECTOR_MUST_BE_A_CONNECTOR("filter.connector.must.be.a.connector", "SEMANTIC_0107"),
    UNKNOWN_FIELD_IN_JSON_STRUCT("unknown.field.in.json.struct", "SEMANTIC_0108"),
    RETRY_STMT_NOT_ALLOWED_HERE("retry.stmt.not.allowed.here", "SEMANTIC_0109"),
    INVALID_RETRY_COUNT("invalid.retry.count", "SEMANTIC_0110"),
    ARGUMENTS_COUNT_MISMATCH("arguments.count.mismatch", "SEMANTIC_0109")
    ;

    private String errorMsgKey;
    private String errorCode;

    SemanticErrors(String errorMessageKey, String errorCode) {
        this.errorMsgKey = errorMessageKey;
        this.errorCode = errorCode;
    }

    public String getErrorMsgKey() {
        return errorMsgKey;
    }

    public void setErrorMsgKey(String errorMsgKey) {
        this.errorMsgKey = errorMsgKey;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
