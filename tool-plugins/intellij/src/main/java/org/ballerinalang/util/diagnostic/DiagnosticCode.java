/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.util.diagnostic;

/**
 * This class contains a list of diagnostic codes.
 */
public enum DiagnosticCode {

    UNDEFINED_PACKAGE("undefined.package"),
    UNUSED_IMPORT_PACKAGE("unused.import.package"),
    PACKAGE_NOT_FOUND("package.not.found"),
    REDECLARED_IMPORT_PACKAGE("redeclared.import.package"),
    INVALID_PACKAGE_DECLARATION("invalid.package.declaration"),
    MISSING_PACKAGE_DECLARATION("missing.package.declaration"),
    UNEXPECTED_PACKAGE_DECLARATION("unexpected.package.declaration"),
    REDECLARED_SYMBOL("redeclared.symbol"),
    REDECLARED_BUILTIN_SYMBOL("redeclared.builtin.symbol"),
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
    INCOMPATIBLE_TYPE_CONSTRAINT("incompatible.type.constraint"),
    WORKER_SEND_RECEIVE_PARAMETER_COUNT_MISMATCH("worker.send.receive.parameter.count.mismatch"),
    INVALID_WORKER_INTERACTION("worker.invalid.worker.interaction"),

    INVOKABLE_MUST_RETURN("invokable.must.return"),
    ATLEAST_ONE_WORKER_MUST_RETURN("atleast.one.worker.must.return"),
    FORK_JOIN_WORKER_CANNOT_RETURN("fork.join.worker.cannot.return"),
    FORK_JOIN_INVALID_WORKER_COUNT("fork.join.invalid.worker.count"),
    UNREACHABLE_CODE("unreachable.code"),
    NEXT_CANNOT_BE_OUTSIDE_LOOP("next.cannot.be.outside.loop"),
    BREAK_CANNOT_BE_OUTSIDE_LOOP("break.cannot.be.outside.loop"),
    ABORT_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK("abort.cannot.be.outside.transaction.block"),
    BREAK_CANNOT_BE_USED_TO_EXIT_TRANSACTION("break.statement.cannot.be.used.to.exit.from.a.transaction"),
    NEXT_CANNOT_BE_USED_TO_EXIT_TRANSACTION("next.statement.cannot.be.used.to.exit.from.a.transaction"),
    INVALID_RETRY_COUNT("invalid.retry.count"),

    // Transformer related error codes
    UNDEFINED_TRANSFORMER("undefined.transformer"),
    TRANSFORMER_INVALID_OUTPUT_USAGE("transformer.invalid.output.usage"),
    TRANSFORMER_INVALID_INPUT_UPDATE("transformer.invalid.input.update"),
    INVALID_STATEMENT_IN_TRANSFORMER("invalid.statement.in.transformer"),
    TRANSFORMER_MUST_HAVE_OUTPUT("transformer.must.have.output"),
    TOO_MANY_OUTPUTS_FOR_TRANSFORMER("too.many.outputs.for.transformer"),
    TRANSFORMER_CONFLICTS_WITH_CONVERSION("transformer.conflicts.with.conversion"),
    TRANSFORMER_UNSUPPORTED_TYPES("transformer.unsupported.types"),

    // Cast and conversion related codes
    INCOMPATIBLE_TYPES_CAST("incompatible.types.cast"),
    INCOMPATIBLE_TYPES_CAST_WITH_SUGGESTION("incompatible.types.cast.with.suggestion"),
    INCOMPATIBLE_TYPES_CONVERSION("incompatible.types.conversion"),
    INCOMPATIBLE_TYPES_CONVERSION_WITH_SUGGESTION("incompatible.types.conversion.with.suggestion"),
    UNSAFE_CAST_ATTEMPT("unsafe.cast.attempt"),
    UNSAFE_CONVERSION_ATTEMPT("unsafe.conversion.attempt"),

    INVALID_LITERAL_FOR_TYPE("invalid.literal.for.type"),
    ARRAY_LITERAL_NOT_ALLOWED("array.literal.not.allowed"),
    STRING_TEMPLATE_LIT_NOT_ALLOWED("string.template.literal.not.allowed"),
    INVALID_FIELD_NAME_RECORD_LITERAL("invalid.field.name.record.lit"),

    NOT_ENOUGH_ARGS_FUNC_CALL("not.enough.args.call"),
    TOO_MANY_ARGS_FUNC_CALL("too.many.args.call"),
    ASSIGNMENT_COUNT_MISMATCH("assignment.count.mismatch"),
    ASSIGNMENT_REQUIRED("assignment.required"),
    MULTI_VAL_IN_SINGLE_VAL_CONTEXT("multi.value.in.single.value.context"),
    DOES_NOT_RETURN_VALUE("does.not.return.value"),
    FUNC_DEFINED_ON_NOT_SUPPORTED_TYPE("func.defined.on.not.supported.type"),
    FUNC_DEFINED_ON_NON_LOCAL_TYPE("func.defined.on.non.local.type"),
    STRUCT_FIELD_AND_FUNC_WITH_SAME_NAME("struct.field.and.func.with.same.name"),
    INVALID_STRUCT_INITIALIZER_FUNCTION("invalid.struct.initializer.function"),
    ATTEMPT_CREATE_NON_PUBLIC_INITIALIZER("attempt.to.create.struct.non.public.initializer"),
    STRUCT_INITIALIZER_INVOKED("explicit.invocation.of.struct.init.is.not.allowed"),
    PKG_ALIAS_NOT_ALLOWED_HERE("pkg.alias.not.allowed.here"),

    MULTI_VALUE_RETURN_EXPECTED("multi.value.return.expected"),
    SINGLE_VALUE_RETURN_EXPECTED("single.value.return.expected"),
    TOO_MANY_RETURN_VALUES("return.value.too.many"),
    NOT_ENOUGH_RETURN_VALUES("return.value.not.enough"),
    RETURN_VALUE_NOT_EXPECTED("return.value.not.expected"),
    INVALID_ACTION_INVOCATION("invalid.action.invocation"),
    INVALID_FUNCTION_INVOCATION("invalid.function.invocation"),

    DUPLICATED_ERROR_CATCH("duplicated.error.catch"),

    NO_NEW_VARIABLES_VAR_ASSIGNMENT("no.new.variables.var.assignment"),
    INVALID_VARIABLE_ASSIGNMENT("invalid.variable.assignment"),
    CANNOT_ASSIGN_VALUE_CONSTANT("cannot.assign.value.to.constant"),
    CANNOT_ASSIGN_VALUE_ENDPOINT("cannot.assign.value.to.endpoint"),
    UNDERSCORE_NOT_ALLOWED("underscore.not.allowed"),
    OPERATION_DOES_NOT_SUPPORT_INDEXING("operation.does.not.support.indexing"),
    OPERATION_DOES_NOT_SUPPORT_FIELD_ACCESS("operation.does.not.support.field.access"),
    INVALID_INDEX_EXPR_STRUCT_FIELD_ACCESS("invalid.index.expr.struct.field.access"),
    INVALID_ENUM_EXPR("invalid.enum.expr"),

    INVALID_NAMESPACE_PREFIX("invalid.namespace.prefix"),
    XML_TAGS_MISMATCH("mismatching.xml.start.end.tags"),
    XML_ATTRIBUTE_MAP_UPDATE_NOT_ALLOWED("xml.attribute.map.update.not.allowed"),
    XML_QNAME_UPDATE_NOT_ALLOWED("xml.qname.update.not.allowed"),
    INVALID_NAMESPACE_DECLARATION("invalid.namespace.declaration"),
    CANNOT_UPDATE_XML_SEQUENCE("cannot.update.xml.sequence"),

    UNDEFINED_ANNOTATION("undefined.annotation"),
    ANNOTATION_NOT_ALLOWED("annotation.not.allowed"),
    INVALID_OPERATION_INCOMPATIBLE_TYPES("invalid.operation.incompatible.types"),
    INVALID_DEFAULT_VALUE("invalid.default.value"),
    INVALID_ATTRIBUTE_TYPE("invalid.attribute.type"),
    NO_SUCH_ATTRIBUTE("no.such.attribute"),
    ATTRIBUTE_VAL_CANNOT_REFER_NON_CONST("annotation.attribute.value.cannot.refer.non.constant"),
    INCOMPATIBLE_TYPES_ARRAY_FOUND("incompatible.types.array.found"),

    NO_SUCH_DOCUMENTABLE_ATTRIBUTE("no.such.documentable.attribute"),
    DUPLICATE_DOCUMENTED_ATTRIBUTE("duplicate.documented.attribute"),
    UNDEFINED_DOCUMENTATION_PUBLIC_FUNCTION("undefined.documentation.public.function"),
    USAGE_OF_DEPRECATED_FUNCTION("usage.of.deprecated.function"),

    // Error codes related to iteration.
    ITERABLE_NOT_SUPPORTED_COLLECTION("iterable.not.supported.collection"),
    ITERABLE_NOT_SUPPORTED_OPERATION("iterable.not.supported.operation"),
    ITERABLE_TOO_MANY_VARIABLES("iterable.too.many.variables"),
    ITERABLE_NOT_ENOUGH_VARIABLES("iterable.not.enough.variables"),
    ITERABLE_TOO_MANY_RETURN_VARIABLES("iterable.too.many.return.args"),
    ITERABLE_NOT_ENOUGH_RETURN_VARIABLES("iterable.not.enough.return.args"),
    ITERABLE_LAMBDA_REQUIRED("iterable.lambda.required"),
    ITERABLE_NO_ARGS_REQUIRED("iterable.no.args.required"),
    ITERABLE_LAMBDA_INCOMPATIBLE_TYPES("iterable.lambda.incompatible.types"),
    ITERABLE_RETURN_TYPE_MISMATCH("iterable.return.type.mismatch"),

    // Parser error diagnostic codes
    INVALID_TOKEN("invalid.token"),
    MISSING_TOKEN("missing.token"),
    EXTRANEOUS_INPUT("extraneous.input"),
    MISMATCHED_INPUT("mismatched.input"),
    FAILED_PREDICATE("failed.predicate"),
    SYNTAX_ERROR("syntax.error"),
    ;

    private String value;

    DiagnosticCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
