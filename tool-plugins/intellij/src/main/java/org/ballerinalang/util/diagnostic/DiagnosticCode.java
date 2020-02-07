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
 *
 * @since 0.94
 */
public enum DiagnosticCode {

    UNDEFINED_MODULE("undefined.module"),
    UNUSED_IMPORT_MODULE("unused.import.module"),
    MODULE_NOT_FOUND("module.not.found"),
    REDECLARED_IMPORT_MODULE("redeclared.import.module"),
    INVALID_MODULE_DECLARATION("invalid.module.declaration"),
    MISSING_MODULE_DECLARATION("missing.module.declaration"),
    UNEXPECTED_MODULE_DECLARATION("unexpected.module.declaration"),
    REDECLARED_SYMBOL("redeclared.symbol"),
    REDECLARED_BUILTIN_SYMBOL("redeclared.builtin.symbol"),
    UNDEFINED_SYMBOL("undefined.symbol"),
    UNDEFINED_FUNCTION("undefined.function"),
    UNDEFINED_FUNCTION_IN_OBJECT("undefined.function.in.object"),
    UNDEFINED_CONNECTOR("undefined.connector"),
    UNDEFINED_TABLE_COLUMN("undefined.column.in.table"),
    TABLE_CANNOT_BE_CREATED_WITHOUT_CONSTRAINT("table.cannot.be.created.without.constraint"),
    OBJECT_TYPE_NOT_ALLOWED("object.type.not.allowed"),
    UNDEFINED_STRUCTURE_FIELD("undefined.field.in.structure"),
    CANNOT_INFER_OBJECT_TYPE_FROM_LHS("cannot.infer.object.type.from.lhs"),
    OBJECT_UN_INITIALIZABLE_FIELD("object.non.initialised.field"),
    CYCLIC_TYPE_REFERENCE("cyclic.type.reference"),
    ATTEMPT_REFER_NON_ACCESSIBLE_SYMBOL("attempt.refer.non.accessible.symbol"),
    ATTEMPT_EXPOSE_NON_PUBLIC_SYMBOL("attempt.expose.non.public.symbol"),
    UNDEFINED_PARAMETER("undefined.parameter"),
    CANNOT_FIND_MATCHING_FUNCTION("cannot.find.function.sig.for.function.in.object"),
    CANNOT_ATTACH_FUNCTIONS_TO_RECORDS("cannot.attach.functions.to.records"),
    ATTACHED_FUNC_CANT_HAVE_VISIBILITY_MODIFIERS("attached.functions.cannot.have.visibility.modifiers"),
    ATTACHED_FUNCTIONS_MUST_HAVE_BODY("attached.functions.must.have.body"),
    IMPLEMENTATION_ALREADY_EXIST("implementation.already.exist"),
    CANNOT_FIND_MATCHING_INTERFACE("cannot.find.matching.interface.function"),
    EXTERN_FUNCTION_CANNOT_HAVE_BODY("extern.function.cannot.have.body"),
    ABSTRACT_OBJECT_CONSTRUCTOR("abstract.object.constructor"),
    CANNOT_INITIALIZE_ABSTRACT_OBJECT("cannot.initialize.abstract.object"),
    INVALID_INTERFACE_ON_NON_ABSTRACT_OBJECT("invalid.interface.of.non.abstract.object"),
    CANNOT_ATTACH_FUNCTIONS_TO_ABSTRACT_OBJECT("cannot.attach.functions.to.abstract.object"),
    ABSTRACT_OBJECT_FUNCTION_CANNOT_HAVE_BODY("abstract.object.function.cannot.have.body"),

    INCOMPATIBLE_TYPES("incompatible.types"),
    INCOMPATIBLE_TYPES_EXP_TUPLE("incompatible.types.exp.tuple"),
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
    INVALID_MULTIPLE_FORK_JOIN_SEND("worker.multiple.fork.join.send"),

    INVOKABLE_MUST_RETURN("invokable.must.return"),
    MAIN_SHOULD_BE_PUBLIC("main.should.be.public"),
    INVALID_RETURN_WITH_MAIN("invalid.return.with.main"),
    ATLEAST_ONE_WORKER_MUST_RETURN("atleast.one.worker.must.return"),
    FORK_JOIN_WORKER_CANNOT_RETURN("fork.join.worker.cannot.return"),
    FORK_JOIN_INVALID_WORKER_COUNT("fork.join.invalid.worker.count"),
    UNREACHABLE_CODE("unreachable.code"),
    CONTINUE_CANNOT_BE_OUTSIDE_LOOP("continue.cannot.be.outside.loop"),
    BREAK_CANNOT_BE_OUTSIDE_LOOP("break.cannot.be.outside.loop"),

    INTEGER_TOO_LARGE("integer.too.large"),
    INTEGER_TOO_SMALL("integer.too.small"),
    HEXADECIMAL_TOO_LARGE("hexadecimal.too.large"),
    HEXADECIMAL_TOO_SMALL("hexadecimal.too.small"),
    BINARY_TOO_LARGE("binary.too.large"),
    BINARY_TOO_SMALL("binary.too.small"),

    //Transaction related error codes
    ABORT_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK("abort.cannot.be.outside.transaction.block"),
    FAIL_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK("fail.cannot.be.outside.transaction.block"),
    BREAK_CANNOT_BE_USED_TO_EXIT_TRANSACTION("break.statement.cannot.be.used.to.exit.from.a.transaction"),
    CONTINUE_CANNOT_BE_USED_TO_EXIT_TRANSACTION("continue.statement.cannot.be.used.to.exit.from.a.transaction"),
    RETURN_CANNOT_BE_USED_TO_EXIT_TRANSACTION("return.statement.cannot.be.used.to.exit.from.a.transaction"),
    DONE_CANNOT_BE_USED_TO_EXIT_TRANSACTION("done.statement.cannot.be.used.to.exit.from.a.transaction"),
    INVALID_RETRY_COUNT("invalid.retry.count"),
    INVALID_TRANSACTION_HANDLER_ARGS("invalid.transaction.handler.args"),
    INVALID_TRANSACTION_HANDLER_SIGNATURE("invalid.transaction.handler.signature"),
    LAMBDA_REQUIRED_FOR_TRANSACTION_HANDLER("lambda.required.for.transaction.handler"),
    TRANSACTION_CANNOT_BE_USED_WITHIN_HANDLER("transaction.cannot.be.used.within.handler"),
    INVALID_FUNCTION_POINTER_ASSIGNMENT_FOR_HANDLER("invalid.function.pointer.assignment.for.handler"),

    // Service, endpoint related errors codes
    SERVICE_OBJECT_TYPE_REQUIRED("service.object.type.required"),
    SERVICE_INVALID_OBJECT_TYPE("service.invalid.object.type"),
    SERVICE_INVALID_ENDPOINT_TYPE("service.invalid.endpoint.type"),
    SERVICE_SERVICE_TYPE_REQUIRED_ANONYMOUS("service.service.type.required.anonymous"),
    ENDPOINT_OBJECT_TYPE_REQUIRED("endpoint.object.type.required"),
    ENDPOINT_OBJECT_NEW_HAS_PARAM("endpoint.object.new.has.param"),
    ENDPOINT_INVALID_TYPE("endpoint.invalid.type"),
    ENDPOINT_INVALID_TYPE_NO_FUNCTION("endpoint.invalid.type.no.function"),
    ENDPOINT_SPI_INVALID_FUNCTION("endpoint.spi.invalid.function"),

    ENDPOINT_NOT_SUPPORT_INTERACTIONS("endpoint.not.support.interactions"),
    ENDPOINT_NOT_SUPPORT_REGISTRATION("endpoint.not.support.registration"),
    INVALID_ACTION_INVOCATION_SYNTAX("invalid.action.invocation.syntax"),
    INVALID_ACTION_INVOCATION("invalid.action.invocation"),
    UNDEFINED_ACTION("undefined.action"),

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
    INVALID_RECORD_LITERAL_KEY("invalid.record.literal.key"),
    INVALID_FIELD_NAME_RECORD_LITERAL("invalid.field.name.record.lit"),
    REST_FIELD_NOT_ALLOWED_IN_SEALED_RECORDS("rest.field.not.allowed"),
    OPEN_RECORD_CONSTRAINT_NOT_ALLOWED("open.record.constraint.not.allowed"),
    INVALID_RECORD_REST_DESCRIPTOR("invalid.record.rest.descriptor"),
    AMBIGUOUS_TYPES("ambiguous.type"),

    NOT_ENOUGH_ARGS_FUNC_CALL("not.enough.args.call"),
    TOO_MANY_ARGS_FUNC_CALL("too.many.args.call"),
    DEFAULTABLE_ARG_PASSED_AS_REQUIRED_ARG("defaultable.arg.passed.as.required.arg"),
    ASSIGNMENT_COUNT_MISMATCH("assignment.count.mismatch"),
    ASSIGNMENT_REQUIRED("assignment.required"),
    MULTI_VAL_IN_SINGLE_VAL_CONTEXT("multi.value.in.single.value.context"),
    MULTI_VAL_EXPR_IN_SINGLE_VAL_CONTEXT("multi.valued.expr.in.single.valued.context"),
    DOES_NOT_RETURN_VALUE("does.not.return.value"),
    FUNC_DEFINED_ON_NOT_SUPPORTED_TYPE("func.defined.on.not.supported.type"),
    FUNC_DEFINED_ON_NON_LOCAL_TYPE("func.defined.on.non.local.type"),
    OBJECT_FIELD_AND_FUNC_WITH_SAME_NAME("object.field.and.func.with.same.name"),
    INVALID_OBJECT_CONSTRUCTOR("invalid.object.constructor"),
    RECORD_INITIALIZER_INVOKED("explicit.invocation.of.record.init.is.not.allowed"),
    PKG_ALIAS_NOT_ALLOWED_HERE("pkg.alias.not.allowed.here"),
    INVALID_REST_ARGS("invalid.rest.args"),

    MULTI_VALUE_RETURN_EXPECTED("multi.value.return.expected"),
    SINGLE_VALUE_RETURN_EXPECTED("single.value.return.expected"),
    TOO_MANY_RETURN_VALUES("return.value.too.many"),
    NOT_ENOUGH_RETURN_VALUES("return.value.not.enough"),
    RETURN_STMT_NOT_VALID_IN_RESOURCE("return.stmt.not.valid.in.resource"),
    INVALID_FUNCTION_INVOCATION("invalid.function.invocation"),
    INVALID_FUNCTION_INVOCATION_WITH_NAME("invalid.function.invocation.with.name"),
    DUPLICATE_NAMED_ARGS("duplicate.named.args"),
    INVALID_DEFAULT_PARAM_VALUE("invalid.default.param.value"),

    DUPLICATED_ERROR_CATCH("duplicated.error.catch"),

    NO_NEW_VARIABLES_VAR_ASSIGNMENT("no.new.variables.var.assignment"),
    INVALID_VARIABLE_ASSIGNMENT("invalid.variable.assignment"),
    CANNOT_ASSIGN_VALUE_READONLY("cannot.assign.value.to.readonly.field"),
    CANNOT_ASSIGN_VALUE_FINAL("cannot.assign.value.to.final.field"),
    CANNOT_ASSIGN_VALUE_FUNCTION_ARGUMENT("cannot.assign.value.to.function.argument"),
    CANNOT_ASSIGN_VALUE_ENDPOINT("cannot.assign.value.to.endpoint"),
    UNDERSCORE_NOT_ALLOWED("underscore.not.allowed"),
    OPERATION_DOES_NOT_SUPPORT_INDEXING("operation.does.not.support.indexing"),
    OPERATION_DOES_NOT_SUPPORT_FIELD_ACCESS("operation.does.not.support.field.access"),
    INVALID_INDEX_EXPR_STRUCT_FIELD_ACCESS("invalid.index.expr.struct.field.access"),
    INVALID_INDEX_EXPR_TUPLE_FIELD_ACCESS("invalid.index.expr.tuple.field.access"),
    INVALID_ENUM_EXPR("invalid.enum.expr"),
    INVALID_EXPR_IN_MATCH_STMT("invalid.expr.in.match.stmt"),
    UNINITIALIZED_VARIABLE("uninitialized.variable"),
    INVALID_ANY_VAR_DEF("invalid.any.var.def"),
    INVALID_RECORD_LITERAL("invalid.record.literal"),
    DUPLICATE_KEY_IN_RECORD_LITERAL("duplicate.key.in.record.literal"),
    INVALID_ARRAY_LITERAL("invalid.array.literal"),
    INVALID_TUPLE_LITERAL("invalid.tuple.literal"),
    INVALID_LIST_CONSTRUCTOR("invalid.list.constructor"),
    MISMATCHING_ARRAY_LITERAL_VALUES("mismatching.array.literal.values"),
    SEALED_ARRAY_TYPE_NOT_INITIALIZED("sealed.array.type.not.initialized"),
    SEALED_ARRAY_TYPE_CAN_NOT_INFER_SIZE("sealed.array.type.can.not.infer.size"),
    ARRAY_INDEX_OUT_OF_RANGE("array.index.out.of.range"),
    TUPLE_INDEX_OUT_OF_RANGE("tuple.index.out.of.range"),
    INVALID_TYPE_NEW_LITERAL("invalid.type.new.literal"),
    INVALID_USAGE_OF_KEYWORD("invalid.usage.of.keyword"),

    INVALID_NAMESPACE_PREFIX("invalid.namespace.prefix"),
    XML_TAGS_MISMATCH("mismatching.xml.start.end.tags"),
    XML_ATTRIBUTE_MAP_UPDATE_NOT_ALLOWED("xml.attribute.map.update.not.allowed"),
    XML_QNAME_UPDATE_NOT_ALLOWED("xml.qname.update.not.allowed"),
    INVALID_NAMESPACE_DECLARATION("invalid.namespace.declaration"),
    CANNOT_UPDATE_XML_SEQUENCE("cannot.update.xml.sequence"),

    UNDEFINED_ANNOTATION("undefined.annotation"),
    ANNOTATION_NOT_ALLOWED("annotation.not.allowed"),
    ANNOTATION_ATTACHMENT_NO_VALUE("annotation.attachment.no.value"),
    INCOMPATIBLE_TYPES_ARRAY_FOUND("incompatible.types.array.found"),
    CANNOT_GET_ALL_FIELDS("cannot.get.all.fields"),

    UNDOCUMENTED_PARAMETER("undocumented.parameter"),
    NO_SUCH_DOCUMENTABLE_PARAMETER("no.such.documentable.parameter"),
    PARAMETER_ALREADY_DOCUMENTED("parameter.already.documented"),
    UNDOCUMENTED_FIELD("undocumented.field"),
    NO_SUCH_DOCUMENTABLE_FIELD("no.such.documentable.field"),
    FIELD_ALREADY_DOCUMENTED("field.already.documented"),
    UNDOCUMENTED_VARIABLE("undocumented.variable"),
    NO_SUCH_DOCUMENTABLE_VARIABLE("no.such.documentable.variable"),
    VARIABLE_ALREADY_DOCUMENTED("variable.already.documented"),
    UNDOCUMENTED_RETURN_PARAMETER("undocumented.return.parameter"),
    NO_DOCUMENTABLE_RETURN_PARAMETER("no.documentable.return.parameter"),

    NO_SUCH_DOCUMENTABLE_ATTRIBUTE("no.such.documentable.attribute"),
    INVALID_USE_OF_ENDPOINT_DOCUMENTATION_ATTRIBUTE("invalid.use.of.endpoint.documentation.attribute"),
    DUPLICATE_DOCUMENTED_ATTRIBUTE("duplicate.documented.attribute"),
    UNDEFINED_DOCUMENTATION_PUBLIC_FUNCTION("undefined.documentation.public.function"),
    USAGE_OF_DEPRECATED_FUNCTION("usage.of.deprecated.function"),
    OPERATOR_NOT_SUPPORTED("operator.not.supported"),
    OPERATOR_NOT_ALLOWED_VARIABLE("operator.not.allowed.variable"),

    // Error codes related to iteration.
    ITERABLE_NOT_SUPPORTED_COLLECTION("iterable.not.supported.collection"),
    ITERABLE_NOT_SUPPORTED_OPERATION("iterable.not.supported.operation"),
    ITERABLE_TOO_MANY_VARIABLES("iterable.too.many.variables"),
    ITERABLE_NOT_ENOUGH_VARIABLES("iterable.not.enough.variables"),
    ITERABLE_TOO_MANY_RETURN_VARIABLES("iterable.too.many.return.args"),
    ITERABLE_NOT_ENOUGH_RETURN_VARIABLES("iterable.not.enough.return.args"),
    ITERABLE_LAMBDA_REQUIRED("iterable.lambda.required"),
    ITERABLE_LAMBDA_TUPLE_REQUIRED("iterable.lambda.tuple.required"),
    ITERABLE_NO_ARGS_REQUIRED("iterable.no.args.required"),
    ITERABLE_LAMBDA_INCOMPATIBLE_TYPES("iterable.lambda.incompatible.types"),
    ITERABLE_RETURN_TYPE_MISMATCH("iterable.return.type.mismatch"),

    // match statement related errors
    MATCH_STMT_CANNOT_GUARANTEE_A_MATCHING_PATTERN("match.stmt.cannot.guarantee.a.matching.pattern"),
    MATCH_STMT_UNREACHABLE_PATTERN("match.stmt.unreachable.pattern"),
    MATCH_STMT_UNMATCHED_PATTERN("match.stmt.unmatched.pattern"),

    // Safe Assignment operator related errors
    SAFE_ASSIGN_STMT_INVALID_USAGE("safe.assign.stmt.invalid.usage"),

    // Safe navigation operator related errors
    SAFE_NAVIGATION_NOT_REQUIRED("safe.navigation.not.required"),
    INVALID_ERROR_LIFTING_ON_LHS("invalid.error.lifting.on.lhs"),

    // Checked expression related errors
    CHECKED_EXPR_INVALID_USAGE_NO_ERROR_TYPE_IN_RHS("checked.expr.invalid.usage.no.error.type.rhs"),
    CHECKED_EXPR_INVALID_USAGE_ALL_ERROR_TYPES_IN_RHS("checked.expr.invalid.usage.only.error.types.rhs"),

    START_REQUIRE_INVOCATION("start.require.invocation"),
    INVALID_EXPR_STATEMENT("invalid.expr.statement"),
    INVALID_ACTION_INVOCATION_AS_EXPR("invalid.action.invocation.as.expr"),

    // Parser error diagnostic codes
    INVALID_TOKEN("invalid.token"),
    MISSING_TOKEN("missing.token"),
    EXTRANEOUS_INPUT("extraneous.input"),
    MISMATCHED_INPUT("mismatched.input"),
    FAILED_PREDICATE("failed.predicate"),
    SYNTAX_ERROR("syntax.error"),
    INVALID_SHIFT_OPERATOR("invalid.shift.operator"),

    // Streaming related codes
    INVALID_STREAM_CONSTRUCTOR("invalid.stream.constructor"),

    // Taint checking related codes
    ENTRY_POINT_PARAMETERS_CANNOT_BE_SENSITIVE("entry.point.parameters.cannot.be.sensitive"),
    TAINTED_VALUE_PASSED_TO_SENSITIVE_PARAMETER("tainted.value.passed.to.sensitive.parameter"),
    TAINTED_VALUE_PASSED_TO_GLOBAL_VARIABLE("tainted.value.passed.to.global.variable"),
    UNABLE_TO_PERFORM_TAINT_CHECKING_WITH_RECURSION("unable.to.perform.taint.checking.with.recursion"),

    // Anonymous functions related codes
    ARROW_EXPRESSION_MISMATCHED_PARAMETER_LENGTH("arrow.expression.mismatched.parameter.length"),
    ARROW_EXPRESSION_CANNOT_INFER_TYPE_FROM_LHS("arrow.expression.cannot.infer.type.from.lhs"),
    ARROW_EXPRESSION_NOT_SUPPORTED_ITERABLE_OPERATION("arrow.expression.not.supported.iterable.operation")
    ;

    private String value;

    DiagnosticCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
