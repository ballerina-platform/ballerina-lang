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

package io.ballerina.runtime.internal.util.exceptions;

import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * Error codes and Error keys to represent the runtime errors.
 */
public enum RuntimeErrors implements DiagnosticCode {

    CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE("casting.any.to.wrong.value.type", "RUNTIME_0001"),
    CASTING_ANY_TYPE_WITHOUT_INIT("casting.any.without.init", "RUNTIME_0002"),
    INDEX_NUMBER_TOO_LARGE("index.number.too.large", "RUNTIME_0003"),
    ARRAY_INDEX_OUT_OF_RANGE("array.index.out.of.range", "RUNTIME_0004"),
    INCOMPATIBLE_TYPE("incompatible.types", "RUNTIME_0005"),
    CASTING_WITHOUT_REQUIRED_FIELD("casting.without.required.field", "RUNTIME_0006"),
    CASTING_FAILED_WITH_CAUSE("casting.failed.with.cause", "RUNTIME_0007"),
    MISSING_FIELD_IN_JSON("missing.field.in.json", "RUNTIME_0008"),
    CANNOT_SET_VALUE_INCOMPATIBLE_TYPES("cannot.set.value.incompatible.types", "RUNTIME_0009"),
    CANNOT_GET_VALUE_INCOMPATIBLE_TYPES("cannot.get.value.incompatible.types", "RUNTIME_0010"),
    INCOMPATIBLE_FIELD_TYPE_FOR_CASTING("incompatible.field.type.for.casting", "RUNTIME_0011"),
    INCOMPATIBLE_TYPE_FOR_CASTING_JSON("incompatible.types.in.json", "RUNTIME_0012"),
    JSON_SET_ERROR("json.set.error", "RUNTIME_0013"),
    JSON_GET_ERROR("json.get.error", "RUNTIME_0014"),
    ARRAY_TYPE_MISMATCH("from.and.to.array.type.mismatch", "RUNTIME_0015"),
    SERVER_CONNECTOR_ALREADY_EXIST("server.connector.already.exist", "RUNTIME_0016"),
    INVALID_SERVICE_PROTOCOL("invalid.service.protocol", "RUNTIME_0017"),
    CONNECTOR_INPUT_TYPES_NOT_EQUIVALENT("connector.input.types.are.not.equivalent", "RUNTIME_0018"),
    UNKNOWN_FIELD_JSON_STRUCT("unknown.field.in.json.struct", "RUNTIME_0019"),
    INVALID_RETRY_COUNT("invalid.retry.count", "RUNTIME_0020"),
    NOT_ENOUGH_FORMAT_ARGUMENTS("not.enough.format.arguments", "RUNTIME_0021"),
    INVALID_FORMAT_SPECIFIER("invalid.format.specifier", "RUNTIME_0022"),
    INVALID_MAP_INSERTION("invalid.map.insertion", "RUNTIME_0023"),
    INVALID_VALUE_LOAD("invalid.value.load", "RUNTIME_0024"),
    INVALID_TASK_CONFIG("invalid.task.config", "RUNTIME_0025"),
    TASK_ALREADY_RUNNING("task.already.running", "RUNTIME_0026"),
    TASK_NOT_RUNNING("task.not.running", "RUNTIME_0027"),
    ILLEGAL_FORMAT_CONVERSION("illegal.format.conversion", "RUNTIME_0028"),
    INCOMPATIBLE_STAMP_OPERATION("incompatible.stamp.operation", "RUNTIME_0029"),
    CANNOT_STAMP_NULL("cannot.stamp.null", "RUNTIME_0030"),
    UNSUPPORTED_CLONE_OPERATION("unsupported.clone.operation", "RUNTIME_0031"),
    INVALID_RECORD_FIELD_ACCESS("invalid.record.field.access", "RUNTIME_0032"),
    INVALID_RECORD_FIELD_ADDITION("invalid.record.field.addition", "RUNTIME_0033"),
    INVALID_OBJECT_FIELD_ADDITION("invalid.object.field.addition", "RUNTIME_0034"),
    TYPE_CAST_ERROR("incompatible.types.cannot.cast", "RUNTIME_0035"),
    J_TYPE_CAST_ERROR("incompatible.jtypes.cannot.cast", "RUNTIME_0036"),
    INVALID_DYNAMICALLY_NESTED_TRANSACTION("dynamically.nested.transactions.are.not.allowed", "RUNTIME_0037"),
    CYCLIC_VALUE_REFERENCE("cyclic.value.reference", "RUNTIME_0038"),
    CANNOT_CONVERT_NIL("cannot.convert.nil", "RUNTIME_0039"),
    INCOMPATIBLE_CONVERT_OPERATION("incompatible.convert.operation", "RUNTIME_0040"),
    J_TYPE_ASSIGNABLE_ERROR("incompatible.java.object.type.assignment", "RUNTIME_0041"),
    INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION("incompatible.simple.type.convert.operation", "RUNTIME_0042"),
    TUPLE_INDEX_OUT_OF_RANGE("tuple.index.out.of.range", "RUNTIME_0043"),
    ILLEGAL_ARRAY_INSERTION("illegal.array.insertion", "RUNTIME_0044"),
    XML_FUNC_TYPE_ERROR("unexpected.xml.type", "RUNTIME_0045"),
    JAVA_NULL_REFERENCE("java.null.reference", "RUNTIME_0046"),
    ILLEGAL_TUPLE_INSERTION("illegal.tuple.insertion", "RUNTIME_0047"),
    ILLEGAL_ARRAY_SIZE("illegal.array.size", "RUNTIME_0048"),
    ILLEGAL_TUPLE_SIZE("illegal.tuple.size", "RUNTIME_0049"),
    ILLEGAL_TUPLE_WITH_REST_TYPE_SIZE("illegal.rest.tuple.size", "RUNTIME_0050"),
    STRING_INDEX_OUT_OF_RANGE("string.index.out.of.range", "RUNTIME_0051"),
    SUBSTRING_INDEX_OUT_OF_RANGE("substring.index.out.of.range", "RUNTIME_0052"),
    CODEPOINT_INDEX_OUT_OF_RANGE("codepoint.index.out.of.range", "RUNTIME_0053"),
    INVALID_SUBSTRING_RANGE("invalid.substring.range", "RUNTIME_0054"),
    XML_SEQUENCE_INDEX_OUT_OF_RANGE("xml.index.out.of.range", "RUNTIME_0055"),
    INVALID_XML_ATTRIBUTE_ERROR("invalid.xml.attribute", "RUNTIME_0056"),
    ATTRIBUTE_NOT_FOUND_ERROR("attribute.not.found", "RUNTIME_0057"),
    RECORD_INVALID_READONLY_FIELD_UPDATE("record.invalid.readonly.field.update", "RUNTIME_0058"),
    INVALID_READONLY_VALUE_UPDATE("invalid.update.on.readonly.value", "RUNTIME_0059"),
    OBJECT_INVALID_FINAL_FIELD_UPDATE("object.invalid.final.field.update", "RUNTIME_0060"),
    UNSUPPORTED_COMPARISON_OPERATION("unsupported.comparison.operation", "RUNTIME_0061"),
    UNORDERED_TYPES_IN_COMPARISON("unordered.types.in.comparison", "RUNTIME_0062"),
    TYPE_ASSIGNABLE_ERROR("incompatible.type.assignment", "RUNTIME_0063"),
    INT_RANGE_OVERFLOW_ERROR("int.range.overflow", "RUNTIME_0064"),
    INVALID_OBJECT_FIELD_VALUE_ERROR("invalid.value.for.object.field", "RUNTIME_0065"),
    KEY_NOT_FOUND_ERROR("key.not.found", "RUNTIME_0066"),
    KEY_NOT_FOUND_IN_VALUE("key.not.found.in.value", "RUNTIME_0067"),
    TABLE_HAS_A_VALUE_FOR_KEY("table.has.a.value.for.key", "RUNTIME_0068"),
    KEY_NOT_FOUND_IN_JSON_MAPPING("key.not.found.in.json.mapping", "RUNTIME_0069"),
    MERGE_JSON_ERROR("merge.json.error", "RUNTIME_0070"),
    FIELD_REMOVAL_NOT_ALLOWED("field.removal.not.allowed", "RUNTIME_0071"),
    OPERATION_NOT_SUPPORTED_ERROR("operation.not.supported", "RUNTIME_0072"),
    CONFIG_TYPE_NOT_SUPPORTED("config.type.not.supported", "RUNTIME_0073"),
    CONFIG_INCOMPATIBLE_TYPE("config.incompatible.type", "RUNTIME_0074"),
    CONFIG_INVALID_BYTE_RANGE("config.invalid.byte.range", "RUNTIME_0075"),
    CONFIG_VALUE_NOT_PROVIDED("config.value.not.provided", "RUNTIME_0076"),
    CONFIG_TOML_INVALID_FILE("config.toml.invalid.file", "RUNTIME_0077"),
    CONFIG_UNION_VALUE_AMBIGUOUS_TARGET("config.union.value.ambiguous.target", "RUNTIME_0078"),
    CONFIG_TOML_INVALID_ADDTIONAL_RECORD_FIELD("config.toml.invalid.additional.record.field", "RUNTIME_0079"),
    CONFIG_TOML_FEILD_NOT_SUPPORTED("config.toml.field.not.supported", "RUNTIME_0080"),
    CONFIG_TOML_REQUIRED_FILED_NOT_PROVIDED("config.toml.required.field.not.provided", "RUNTIME_0081"),
    CONFIG_TOML_TABLE_KEY_NOT_PROVIDED("config.toml.table.key.not.provided", "RUNTIME_0082"),
    CONFIG_TOML_INVALID_MODULE_STRUCTURE("config.toml.invalid.module.structure", "RUNTIME_0083"),
    CONFIG_TOML_DEFAULT_FILED_NOT_SUPPORTED("config.toml.default.field.not.supported", "RUNTIME_0084"),
    CONFIG_TOML_EMPTY_FILE("config.toml.empty.file", "RUNTIME_0085"),
    CONFIG_TOML_EMPTY_CONTENT("config.toml.empty.content", "RUNTIME_0086"),
    CONFIG_TOML_FILE_NOT_FOUND("config.toml.file.not.found", "RUNTIME_0087"),
    CONFIG_TOML_READ_FAILED("config.toml.read.failed", "RUNTIME_0088"),
    CONFIG_TOML_PARSE_FAILED("config.toml.parse.failed", "RUNTIME_0089"),
    CONFIG_TOML_MODULE_AMBIGUITY("config.toml.module.ambiguity", "RUNTIME_0090"),
    CONFIG_TOML_UNUSED_VALUE("config.toml.unused.value", "RUNTIME_0091"),
    CONFIG_CLI_TYPE_NOT_SUPPORTED("config.cli.type.not.supported", "RUNTIME_0092"),
    CONFIG_CLI_VARIABLE_AMBIGUITY("config.cli.variable.ambiguity", "RUNTIME_0093"),
    CONFIG_CLI_ARGS_AMBIGUITY("config.cli.args.ambiguity", "RUNTIME_0094"),
    CONFIG_CLI_UNUSED_CLI_ARGS("config.cli.unused.args", "RUNTIME_0095"),
    LARGE_EXPONENTS_IN_DECIMAL("large.number.of.exponents.in.decimal", "RUNTIME_0096"),
    UNSUPPORTED_DECIMAL_VALUE("unsupported.decimal.value", "RUNTIME_0097"),
    CONFIG_SIZE_MISMATCH("config.size.mismatch", "RUNTIME_0098"),
    INVALID_FRACTION_DIGITS("invalid.fraction.digits", "RUNTIME_0099"),
    INVALID_UTF_8_BYTE_ARRAY_VALUE("invalid.utf8.byte.array.value", "RUNTIME_0100"),
    INCOMPATIBLE_ARGUMENTS("incompatible.arguments", "RUNTIME_0101"),
    DECIMAL_VALUE_OUT_OF_RANGE("decimal.value.out.of.range", "RUNTIME_0102"),
    NEGATIVE_REGEXP_FIND_INDEX("regexp.match.string.negative.index", "RUNTIME_0103"),
    INVALID_REGEXP_FIND_INDEX("regexp.match.string.index.out.of.range", "RUNTIME_0104");

    private String errorMsgKey;
    private String errorCode;

    RuntimeErrors(String errorMessageKey, String errorCode) {
        this.errorMsgKey = errorMessageKey;
        this.errorCode = errorCode;
    }


    @Override
    public DiagnosticSeverity severity() {
        return DiagnosticSeverity.ERROR;
    }

    @Override
    public String diagnosticId() {
        return errorCode;
    }

    @Override
    public String messageKey() {
        return errorMsgKey;
    }
}
