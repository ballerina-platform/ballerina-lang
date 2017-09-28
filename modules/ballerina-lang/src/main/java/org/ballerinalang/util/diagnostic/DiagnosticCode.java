/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.util.diagnostic;

/**
 * This class contains a list of diagnostic codes.
 *
 * @since 0.94
 */
public enum DiagnosticCode {

    UNDEFINED_PACKAGE("undefined.package"),
    UNUSED_IMPORT_PACKAGE("unused.import.package"),
    REDECLARED_IMPORT_PACKAGE("redeclared.import.package"),
    REDECLARED_SYMBOL("redeclared.symbol"),
    UNDEFINED_SYMBOL("undefined.symbol"),
    UNDEFINED_FUNCTION("undefined.function"),
    UNDEFINED_FUNCTION_IN_STRUCT("undefined.function.in.struct"),
    UNDEFINED_CONNECTOR("undefined.connector"),
    UNDEFINED_ACTION("undefined.action"),
    UNDEFINED_STRUCT_FIELD("undefined.field.in.struct"),
    ATTEMPT_REFER_NON_PUBLIC_SYMBOL("attempt.refer.non.public.symbol"),

    INCOMPATIBLE_TYPES("incompatible.types"),
    UNKNOWN_TYPE("unknown.type"),
    BINARY_OP_INCOMPATIBLE_TYPES("binary.op.incompatible.types"),
    UNARY_OP_INCOMPATIBLE_TYPES("unary.op.incompatible.types"),
    SELF_REFERENCE_VAR("self.reference.var"),
    INVALID_WORKER_SEND_POSITION("invalid.worker.send.position"),
    INVALID_WORKER_RECEIVE_POSITION("invalid.worker.receive.position"),
    UNDEFINED_WORKER("undefined.worker"),
    INVALID_WORKER_JOIN_RESULT_TYPE("invalid.worker.join.result.type"),
    INVALID_WORKER_TIMEOUT_RESULT_TYPE("invalid.worker.timeout.result.type"),
    WORKER_SEND_RECEIVE_PARAMETER_COUNT_MISMATCH("worker.send.receive.parameter.count.mismatch"),
    INVALID_WORKER_INTERACTION("worker.invalid.worker.interaction"),

    FUNCTION_MUST_RETURN("function.must.return"),
    ATLEAST_ONE_WORKER_MUST_RETURN("atleast.one.worker.must.return"),
    UNREACHABLE_CODE("unreachable.code"),
    NEXT_CANNOT_BE_OUTSIDE_LOOP("next.cannot.be.outside.loop"),
    ABORT_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK("abort.cannot.be.outside.transaction.block"),
    RETRY_CANNOT_BE_OUTSIDE_TRANSACTION_FAILED_BLOCK("retry.cannot.be.outside.transaction.failed.block"),
    TRANSFORM_STATEMENT_INVALID_INPUT_OUTPUT("transform.statement.invalid.input.output"),
    TRANSFORM_STATEMENT_EMPTY_BODY("transform.statement.empty.body"),

    // Cast and conversion related codes
    INCOMPATIBLE_TYPES_CAST("incompatible.types.cast"),
    INCOMPATIBLE_TYPES_CONVERSION("incompatible.types.conversion"),
    UNSAFE_CAST_ATTEMPT("unsafe.cast.attempt"),
    UNSAFE_CONVERSION_ATTEMPT("unsafe.conversion.attempt"),

    INVALID_LITERAL_FOR_TYPE("invalid.literal.for.type"),
    ARRAY_LITERAL_NOT_ALLOWED("array.literal.not.allowed"),
    STRING_TEMPLATE_LIT_NOT_ALLOWED("string.template.literal.not.allowed"),
    INVALID_FIELD_NAME_RECORD_LITERAL("invalid.field.name.record.lit"),

    NOT_ENOUGH_ARGS_FUNC_CALL("not.enough.args.call"),
    TOO_MANY_ARGS_FUNC_CALL("too.many.args.call"),
    ASSIGNMENT_COUNT_MISMATCH("assignment.count.mismatch"),
    MULTI_VAL_IN_SINGLE_VAL_CONTEXT("multi.value.in.single.value.context"),
    DOES_NOT_RETURN_VALUE("does.not.return.value"),
    FUNC_DEFINED_ON_NON_STRUCT_TYPE("func.defined.on.non.struct.type"),
    FUNC_DEFINED_ON_NON_LOCAL_STRUCT_TYPE("func.defined.on.non.local.struct.type"),
    STRUCT_FIELD_AND_FUNC_WITH_SAME_NAME("struct.field.and.func.with.same.name"),
    PKG_ALIAS_NOT_ALLOWED_HERE("pkg.alias.not.allowed.here"),

    MULTI_VALUE_RETURN_EXPECTED("multi.value.return.expected"),
    SINGLE_VALUE_RETURN_EXPECTED("single.value.return.expected"),
    TOO_MANY_RETURN_VALUES("return.value.too.many"),
    NOT_ENOUGH_RETURN_VALUES("return.value.not.enough"),
    RETURN_VALUE_NOT_EXPECTED("return.value.not.expected"),

    NO_NEW_VARIABLES_VAR_ASSIGNMENT("no.new.variables.var.assignment"),
    INVALID_VARIABLE_ASSIGNMENT("invalid.variable.assignment"),
    UNDERSCORE_NOT_ALLOWED("underscore.not.allowed"),
    OPERATION_DOES_NOT_SUPPORT_INDEXING("operation.does.not.support.indexing"),
    OPERATION_DOES_NOT_SUPPORT_FIELD_ACCESS("operation.does.not.support.field.access"),
    INVALID_INDEX_EXPR_STRUCT_FIELD_ACCESS("invalid.index.expr.struct.field.access"),

    INVALID_NAMESPACE_PREFIX("invalid.namespace.prefix"),
    XML_TAGS_MISMATCH("mismatching.xml.start.end.tags")
    ;

    private String value;

    DiagnosticCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
