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

    UNDEFINED_MODULE("undefined.module"),
    CYCLIC_MODULE_IMPORTS_DETECTED("cyclic.module.imports.detected"),
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
    INVALID_ERROR_REASON_TYPE("invalid.error.reason.type"),
    UNSUPPORTED_ERROR_REASON_CONST_MATCH("error.match.over.const.reason.ref.not.supported"),
    NON_MODULE_QUALIFIED_ERROR_REASON("non.module.qualified.error.reason"),
    INVALID_ERROR_DETAIL_TYPE("invalid.error.detail.type"),
    ERROR_DETAIL_ARG_IS_NOT_NAMED_ARG("error.detail.arg.not.named.arg"),
    DIRECT_ERROR_CTOR_REASON_NOT_PROVIDED("missing.error.reason"),
    UNDEFINED_TABLE_COLUMN("undefined.column.in.table"),
    TYPE_NOT_ALLOWED_WITH_PRIMARYKEY("type.not.allowed.with.primarykey"),
    FIELD_NOT_ALLOWED_WITH_TABLE_COLUMN("field.not.allowed.with.table.column"),
    TABLE_CANNOT_BE_CREATED_WITHOUT_CONSTRAINT("table.cannot.be.created.without.constraint"),
    TABLE_CONSTRAINT_MUST_BE_A_RECORD_TYPE("table.constraint.must.be.a.record"),
    CANNOT_INFER_TABLE_TYPE("cannot.infer.table.type"),
    TABLE_KEY_EXPECTED("table.key.expected"),
    OBJECT_TYPE_NOT_ALLOWED("object.type.not.allowed"),
    UNDEFINED_STRUCTURE_FIELD_WITH_TYPE("undefined.field.in.structure.with.type"), // TODO: remove Maryam
    UNDEFINED_STRUCTURE_FIELD("undefined.field.in.structure"),
    TYPE_NOT_ALLOWED_WITH_NEW("type.not.allowed.with.new"),
    STREAM_INVALID_CONSTRAINT("stream.invalid.constraint"),
    STREAM_INIT_NOT_ALLOWED_HERE("stream.initialization.not.allowed.here"),
    CANNOT_INFER_OBJECT_TYPE_FROM_LHS("cannot.infer.object.type.from.lhs"),
    OBJECT_UNINITIALIZED_FIELD("object.uninitialized.field"),
    CYCLIC_TYPE_REFERENCE("cyclic.type.reference"),
    ATTEMPT_REFER_NON_ACCESSIBLE_SYMBOL("attempt.refer.non.accessible.symbol"),
    ATTEMPT_EXPOSE_NON_PUBLIC_SYMBOL("attempt.expose.non.public.symbol"),
    UNDEFINED_PARAMETER("undefined.parameter"),
    ATTACHED_FUNCTIONS_MUST_HAVE_BODY("attached.functions.must.have.body"),
    EXTERN_FUNCTION_CANNOT_HAVE_BODY("extern.function.cannot.have.body"),
    ABSTRACT_OBJECT_CONSTRUCTOR("abstract.object.constructor"),
    CANNOT_INITIALIZE_ABSTRACT_OBJECT("cannot.initialize.abstract.object"),
    INVALID_INTERFACE_ON_NON_ABSTRACT_OBJECT("invalid.interface.of.non.abstract.object"),
    PRIVATE_FUNCTION_VISIBILITY("private.function.visibility"),
    CANNOT_ATTACH_FUNCTIONS_TO_ABSTRACT_OBJECT("cannot.attach.functions.to.abstract.object"),
    ABSTRACT_OBJECT_FUNCTION_CANNOT_HAVE_BODY("abstract.object.function.cannot.have.body"),
    PRIVATE_OBJECT_CONSTRUCTOR("private.object.constructor"),
    PRIVATE_FIELD_ABSTRACT_OBJECT("private.field.abstract.object"),
    FIELD_WITH_DEFAULT_VALUE_ABSTRACT_OBJECT("field.with.default.value.abstract.object"),
    PRIVATE_FUNC_ABSTRACT_OBJECT("private.function.abstract.object"),
    EXTERN_FUNC_ABSTRACT_OBJECT("extern.function.abstract.object"),
    RESOURCE_FUNCTION_CANNOT_BE_EXTERN("resource.function.cannot.be.extern"),
    OBJECT_INIT_FUNCTION_CANNOT_BE_EXTERN("object.init.function.cannot.be.extern"),
    GLOBAL_VARIABLE_CYCLIC_DEFINITION("global.variable.cyclic.reference"),
    CANNOT_FIND_ERROR_TYPE("cannot.find.error.constructor.for.type"),
    INVALID_PACKAGE_NAME_QUALIFER("invalid.package.name.qualifier"),

    REQUIRED_PARAM_DEFINED_AFTER_DEFAULTABLE_PARAM("required.param.not.allowed.after.defaultable.param"),
    POSITIONAL_ARG_DEFINED_AFTER_NAMED_ARG("positional.arg.defined.after.named.arg"),
    REST_ARG_DEFINED_AFTER_NAMED_ARG("rest.arg.defined.after.named.arg"),
    MISSING_REQUIRED_PARAMETER("missing.required.parameter"),

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
    INVALID_WORKER_REFERRENCE("invalid.worker.reference"),
    INCOMPATIBLE_TYPE_CONSTRAINT("incompatible.type.constraint"),
    WORKER_SEND_RECEIVE_PARAMETER_COUNT_MISMATCH("worker.send.receive.parameter.count.mismatch"),
    INVALID_WORKER_INTERACTION("worker.invalid.worker.interaction"),
    WORKER_INTERACTIONS_ONLY_ALLOWED_BETWEEN_PEERS("worker.interactions.only.allowed.between.peers"),
    WORKER_SEND_AFTER_RETURN("worker.send.after.return"),
    WORKER_RECEIVE_AFTER_RETURN("worker.receive.after.return"),
    EXPLICIT_WORKER_CANNOT_BE_DEFAULT("explicit.worker.cannot.be.default"),
    INVALID_MULTIPLE_FORK_JOIN_SEND("worker.multiple.fork.join.send"),
    INCOMPATIBLE_TYPE_REFERENCE("incompatible.type.reference"),
    INCOMPATIBLE_TYPE_REFERENCE_NON_PUBLIC_MEMBERS("incompatible.type.reference.non.public.members"),
    INCOMPATIBLE_RECORD_TYPE_REFERENCE("incompatible.record.type.reference"),
    REDECLARED_TYPE_REFERENCE("redeclared.type.reference"),
    REDECLARED_FUNCTION_FROM_TYPE_REFERENCE("redeclared.function.from.type.reference"),
    REFERRED_FUNCTION_SIGNATURE_MISMATCH("referred.function.signature.mismatch"),

    INVOKABLE_MUST_RETURN("invokable.must.return"),
    MAIN_SHOULD_BE_PUBLIC("main.should.be.public"),
    MAIN_PARAMS_SHOULD_BE_ANYDATA("main.params.should.be.anydata"),
    MAIN_RETURN_SHOULD_BE_ERROR_OR_NIL("main.return.should.be.error.or.nil"),
    MODULE_INIT_CANNOT_BE_PUBLIC("module.init.cannot.be.public"),
    MODULE_INIT_CANNOT_HAVE_PARAMS("module.init.cannot.have.params"),
    MODULE_INIT_RETURN_SHOULD_BE_ERROR_OR_NIL("module.init.return.should.be.error.or.nil"),
    ATLEAST_ONE_WORKER_MUST_RETURN("atleast.one.worker.must.return"),
    FORK_JOIN_WORKER_CANNOT_RETURN("fork.join.worker.cannot.return"),
    FORK_JOIN_INVALID_WORKER_COUNT("fork.join.invalid.worker.count"),
    INVALID_FOR_JOIN_SYNTAX_EMPTY_FORK("fork.join.syntax.empty.fork"),
    UNREACHABLE_CODE("unreachable.code"),
    CONTINUE_CANNOT_BE_OUTSIDE_LOOP("continue.cannot.be.outside.loop"),
    BREAK_CANNOT_BE_OUTSIDE_LOOP("break.cannot.be.outside.loop"),

    INTEGER_TOO_LARGE("integer.too.large"),
    INTEGER_TOO_SMALL("integer.too.small"),
    HEXADECIMAL_TOO_LARGE("hexadecimal.too.large"),
    HEXADECIMAL_TOO_SMALL("hexadecimal.too.small"),

    //Transaction related error codes
    ABORT_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK("abort.cannot.be.outside.transaction.block"),
    RETRY_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK("retry.cannot.be.outside.transaction.block"),
    BREAK_CANNOT_BE_USED_TO_EXIT_TRANSACTION("break.statement.cannot.be.used.to.exit.from.a.transaction"),
    CONTINUE_CANNOT_BE_USED_TO_EXIT_TRANSACTION("continue.statement.cannot.be.used.to.exit.from.a.transaction"),
    RETURN_CANNOT_BE_USED_TO_EXIT_TRANSACTION("return.statement.cannot.be.used.to.exit.from.a.transaction"),
    DONE_CANNOT_BE_USED_TO_EXIT_TRANSACTION("done.statement.cannot.be.used.to.exit.from.a.transaction"),
    INVALID_RETRY_COUNT("invalid.retry.count"),
    INVALID_TRANSACTION_HANDLER_ARGS("invalid.transaction.handler.args"),
    INVALID_TRANSACTION_HANDLER_SIGNATURE("invalid.transaction.handler.signature"),
    LAMBDA_REQUIRED_FOR_TRANSACTION_HANDLER("lambda.required.for.transaction.handler"),
    TRANSACTION_CANNOT_BE_USED_WITHIN_HANDLER("transaction.cannot.be.used.within.handler"),
    NESTED_TRANSACTIONS_ARE_INVALID("nested.transactions.are.invalid"),
    INVALID_FUNCTION_POINTER_ASSIGNMENT_FOR_HANDLER("invalid.function.pointer.assignment.for.handler"),

    // Service, endpoint related errors codes
    SERVICE_OBJECT_TYPE_REQUIRED("service.object.type.required"),
    SERVICE_INVALID_OBJECT_TYPE("service.invalid.object.type"),
    SERVICE_INVALID_ENDPOINT_TYPE("service.invalid.endpoint.type"),
    SERVICE_FUNCTION_INVALID_MODIFIER("service.function.invalid.modifier"),
    SERVICE_FUNCTION_INVALID_INVOCATION("service.function.invalid.invocation"),
    SERVICE_SERVICE_TYPE_REQUIRED_ANONYMOUS("service.service.type.required.anonymous"),
    ENDPOINT_OBJECT_TYPE_REQUIRED("endpoint.object.type.required"),
    ENDPOINT_OBJECT_NEW_HAS_PARAM("endpoint.object.new.has.param"),
    ENDPOINT_INVALID_TYPE("endpoint.invalid.type"),
    ENDPOINT_INVALID_TYPE_NO_FUNCTION("endpoint.invalid.type.no.function"),
    ENDPOINT_SPI_INVALID_FUNCTION("endpoint.spi.invalid.function"),

    REMOTE_FUNCTION_IN_NON_CLIENT_OBJECT("remote.function.in.non.client.object"),
    RESOURCE_FUNCTION_IN_NON_SERVICE_OBJECT("resource.function.in.non.service.object"),
    RESOURCE_FUNCTION_INVALID_RETURN_TYPE("resource.function.invalid.return.type"),
    REMOTE_IN_NON_OBJECT_FUNCTION("remote.in.non.object.function"),
    INVALID_LISTENER_VARIABLE("invalid.listener.var"),
    INVALID_LISTENER_ATTACHMENT("invalid.listener.attachment"),

    ENDPOINT_NOT_SUPPORT_REGISTRATION("endpoint.not.support.registration"),
    INVALID_ACTION_INVOCATION_SYNTAX("invalid.action.invocation.syntax"),
    INVALID_INIT_INVOCATION("invalid.init.invocation"),
    INVALID_RESOURCE_FUNCTION_INVOCATION("invalid.resource.function.invocation"),
    INVALID_ACTION_INVOCATION("invalid.action.invocation"),
    UNDEFINED_ACTION("undefined.action"),

    TYPE_CAST_NOT_YET_SUPPORTED("type.cast.not.yet.supported.for.type"),
    EQUALITY_NOT_YET_SUPPORTED("equality.not.yet.supported.for.type"),
    BINDING_PATTERN_NOT_YET_SUPPORTED("binding.pattern.not.yet.supported.for.type"),

    // Cast and conversion related codes
    INCOMPATIBLE_TYPES_CAST("incompatible.types.cast"),
    INCOMPATIBLE_TYPES_CAST_WITH_SUGGESTION("incompatible.types.cast.with.suggestion"),
    INCOMPATIBLE_TYPES_CONVERSION("incompatible.types.conversion"),
    INCOMPATIBLE_TYPES_CONVERSION_WITH_SUGGESTION("incompatible.types.conversion.with.suggestion"),
    UNSAFE_CAST_ATTEMPT("unsafe.cast.attempt"),
    UNSAFE_CONVERSION_ATTEMPT("unsafe.conversion.attempt"),

    INVALID_LITERAL_FOR_TYPE("invalid.literal.for.type"),
    INVALID_LITERAL_FOR_MATCH_PATTERN("invalid.literal.for.match.pattern"),
    INVALID_EXPR_WITH_TYPE_GUARD_FOR_MATCH_PATTERN("invalid.expr.with.type.guard.for.match"),
    ARRAY_LITERAL_NOT_ALLOWED("array.literal.not.allowed"),
    STRING_TEMPLATE_LIT_NOT_ALLOWED("string.template.literal.not.allowed"),
    INVALID_RECORD_LITERAL_KEY("invalid.record.literal.key"),
    INVALID_RECORD_LITERAL_IDENTIFIER_KEY("invalid.record.literal.identifier.key"),
    INVALID_FIELD_NAME_RECORD_LITERAL("invalid.field.name.record.lit"),
    REST_FIELD_NOT_ALLOWED_IN_SEALED_RECORDS("rest.field.not.allowed"),
    OPEN_RECORD_CONSTRAINT_NOT_ALLOWED("open.record.constraint.not.allowed"),
    INVALID_RECORD_REST_DESCRIPTOR("invalid.record.rest.descriptor"),
    MISSING_REQUIRED_RECORD_FIELD("missing.required.record.field"),
    DEFAULT_VALUES_NOT_ALLOWED_FOR_OPTIONAL_FIELDS("default.values.not.allowed.for.optional.fields"),
    INVALID_FUNCTION_POINTER_INVOCATION("invalid.function.pointer.invocation"),
    AMBIGUOUS_TYPES("ambiguous.type"),

    TOO_MANY_ARGS_FUNC_CALL("too.many.args.call"),
    NON_PUBLIC_ARG_ACCESSED_WITH_NAMED_ARG("non.public.arg.accessed.with.named.arg"),
    ASSIGNMENT_COUNT_MISMATCH("assignment.count.mismatch"),
    ASSIGNMENT_REQUIRED("assignment.required"),
    MULTI_VAL_IN_SINGLE_VAL_CONTEXT("multi.value.in.single.value.context"),
    MULTI_VAL_EXPR_IN_SINGLE_VAL_CONTEXT("multi.valued.expr.in.single.valued.context"),
    DOES_NOT_RETURN_VALUE("does.not.return.value"),
    FUNC_DEFINED_ON_NOT_SUPPORTED_TYPE("func.defined.on.not.supported.type"),
    FUNC_DEFINED_ON_NON_LOCAL_TYPE("func.defined.on.non.local.type"),
    INVALID_OBJECT_CONSTRUCTOR("invalid.object.constructor"),
    RECORD_INITIALIZER_INVOKED("explicit.invocation.of.record.init.is.not.allowed"),
    PKG_ALIAS_NOT_ALLOWED_HERE("pkg.alias.not.allowed.here"),
    INVALID_REST_ARGS("invalid.rest.args"),

    MULTI_VALUE_RETURN_EXPECTED("multi.value.return.expected"),
    SINGLE_VALUE_RETURN_EXPECTED("single.value.return.expected"),
    TOO_MANY_RETURN_VALUES("return.value.too.many"),
    NOT_ENOUGH_RETURN_VALUES("return.value.not.enough"),
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
    OPERATION_DOES_NOT_SUPPORT_FIELD_ACCESS_FOR_ASSIGNMENT("operation.does.not.support.field.access.for.assignment"),
    OPERATION_DOES_NOT_SUPPORT_OPTIONAL_FIELD_ACCESS("operation.does.not.support.optional.field.access"),
    OPERATION_DOES_NOT_SUPPORT_FIELD_ACCESS_FOR_NON_REQUIRED_FIELD(
            "operation.does.not.support.field.access.for.non.required.field"),
    OPERATION_DOES_NOT_SUPPORT_OPTIONAL_FIELD_ACCESS_FOR_FIELD(
            "operation.does.not.support.optional.field.access.for.field"),
    OPERATION_DOES_NOT_SUPPORT_INDEX_ACCESS_FOR_ASSIGNMENT("operation.does.not.support.index.access.for.assignment"),
    INVALID_INDEX_EXPR_STRUCT_FIELD_ACCESS("invalid.index.expr.struct.field.access"),
    INVALID_INDEX_EXPR_TUPLE_FIELD_ACCESS("invalid.index.expr.tuple.field.access"),
    INVALID_TUPLE_INDEX_EXPR("invalid.tuple.index.expr"),
    INVALID_RECORD_INDEX_EXPR("invalid.record.index.expr"),
    INVALID_ENUM_EXPR("invalid.enum.expr"),
    INVALID_EXPR_IN_MATCH_STMT("invalid.expr.in.match.stmt"),
    INVALID_PATTERN_CLAUSES_IN_MATCH_STMT("invalid.pattern.clauses.in.match.stmt"),
    STATIC_MATCH_ONLY_SUPPORTS_ANYDATA("static.value.match.only.supports.anydata"),
    UNINITIALIZED_VARIABLE("uninitialized.variable"),
    INVALID_ANY_VAR_DEF("invalid.any.var.def"),
    INVALID_RECORD_LITERAL("invalid.record.literal"),
    INVALID_FIELD_IN_RECORD_BINDING_PATTERN("invalid.field.in.record.binding.pattern"),
    INVALID_RECORD_LITERAL_BINDING_PATTERN("invalid.record.literal.in.binding.pattern"),
    DUPLICATE_KEY_IN_RECORD_LITERAL("duplicate.key.in.record.literal"),
    INVALID_ARRAY_LITERAL("invalid.array.literal"),
    INVALID_TUPLE_LITERAL("invalid.tuple.literal"),
    INVALID_LIST_CONSTRUCTOR("invalid.list.constructor"),
    INVALID_ARRAY_ELEMENT_TYPE("invalid.array.element.type"),
    INVALID_TUPLE_BINDING_PATTERN("invalid.tuple.binding.pattern"),
    INVALID_TYPE_FOR_TUPLE_VAR_EXPRESSION("invalid.type.for.tuple.var.expr"),
    INVALID_TYPE_DEFINITION_FOR_TUPLE_VAR("invalid.type.definition.for.tuple.var"),
    MISMATCHING_ARRAY_LITERAL_VALUES("mismatching.array.literal.values"),
    SEALED_ARRAY_TYPE_NOT_INITIALIZED("sealed.array.type.not.initialized"),
    INVALID_LIST_INDEX_EXPR("invalid.list.index.expr"),
    INVALID_ARRAY_INDEX_EXPR("invalid.array.index.expr"),
    SEALED_ARRAY_TYPE_CAN_NOT_INFER_SIZE("sealed.array.type.can.not.infer.size"),
    // TODO Maryam remove list array tuple and use first only
    INDEX_OUT_OF_RANGE("index.out.of.range"),
    LIST_INDEX_OUT_OF_RANGE("list.index.out.of.range"),
    ARRAY_INDEX_OUT_OF_RANGE("array.index.out.of.range"),
    TUPLE_INDEX_OUT_OF_RANGE("tuple.index.out.of.range"),
    INVALID_TYPE_FOR_REST_DESCRIPTOR("invalid.type.for.rest.descriptor"),
    INVALID_TYPE_NEW_LITERAL("invalid.type.new.literal"),
    INVALID_USAGE_OF_KEYWORD("invalid.usage.of.keyword"),

    INVALID_RECORD_BINDING_PATTERN("invalid.record.binding.pattern"),
    NO_MATCHING_RECORD_REF_PATTERN("no.matching.record.ref.found"),
    MULTIPLE_RECORD_REF_PATTERN_FOUND("multiple.matching.record.ref.found"),
    NOT_ENOUGH_PATTERNS_TO_MATCH_RECORD_REF("not.enough.patterns.to.match.record.ref"),
    INVALID_TYPE_DEFINITION_FOR_RECORD_VAR("invalid.type.definition.for.record.var"),

    INVALID_ERROR_BINDING_PATTERN("invalid.error.binding.pattern"),
    INVALID_ERROR_REASON_BINDING_PATTERN("invalid.error.reason.binding.pattern"),
    INVALID_ERROR_REST_BINDING_PATTERN("invalid.error.rest.binding.pattern"),
    INVALID_TYPE_DEFINITION_FOR_ERROR_VAR("invalid.type.definition.for.error.var"),
    INVALID_ERROR_LITERAL_BINDING_PATTERN("invalid.error.literal.in.binding.pattern"),
    INVALID_ERROR_DESTRUCTURING_NO_REASON_GIVEN("invalid.error.destructuring.reason"),
    INVALID_ERROR_MATCH_PATTERN("invalid.error.match.pattern"),
    DUPLICATE_VARIABLE_IN_BINDING_PATTERN("duplicate.variable.in.binding.pattern"),
    INVALID_VARIABLE_REFERENCE_IN_BINDING_PATTERN("invalid.variable.reference.in.binding.pattern"),

    INVALID_NAMESPACE_PREFIX("invalid.namespace.prefix"),
    XML_TAGS_MISMATCH("mismatching.xml.start.end.tags"),
    XML_ATTRIBUTE_MAP_UPDATE_NOT_ALLOWED("xml.attribute.map.update.not.allowed"),
    XML_QNAME_UPDATE_NOT_ALLOWED("xml.qname.update.not.allowed"),
    INVALID_NAMESPACE_DECLARATION("invalid.namespace.declaration"),
    CANNOT_UPDATE_XML_SEQUENCE("cannot.update.xml.sequence"),
    INVALID_XML_NS_INTERPOLATION("invalid.xml.ns.interpolation"),

    UNDEFINED_ANNOTATION("undefined.annotation"),
    ANNOTATION_NOT_ALLOWED("annotation.not.allowed"),
    ANNOTATION_ATTACHMENT_CANNOT_HAVE_A_VALUE("annotation.attachment.cannot.have.a.value"),
    ANNOTATION_ATTACHMENT_REQUIRES_A_VALUE("annotation.attachment.requires.a.value"),
    ANNOTATION_ATTACHMENT_CANNOT_SPECIFY_MULTIPLE_VALUES("annotation.attachment.cannot.specify.multiple.values"),
    ANNOTATION_INVALID_TYPE("annotation.invalid.type"),
    ANNOTATION_INVALID_CONST_TYPE("annotation.invalid.const.type"),
    ANNOTATION_REQUIRES_CONST("annotation.requires.const"),
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
    MATCH_STMT_PATTERN_ALWAYS_MATCHES("match.stmt.pattern.always.matches"),
    MATCH_STMT_CONTAINS_TWO_DEFAULT_PATTERNS("match.stmt.contains.two.default.patterns"),

    THROW_STMT_NOT_SUPPORTED("throw.stmt.not.supported"),
    TRY_STMT_NOT_SUPPORTED("try.stmt.not.supported"),

    UNKNOWN_BUILTIN_FUNCTION("unknown.builtin.method"),
    UNSUPPORTED_BUILTIN_METHOD("unsupported.builtin.method"),

    // Safe navigation operator related errors
    SAFE_NAVIGATION_NOT_REQUIRED("safe.navigation.not.required"),
    OPTIONAL_FIELD_ACCESS_NOT_REQUIRED_ON_LHS("optional.field.access.not.required.on.lhs"),

    // Checked expression related errors
    CHECKED_EXPR_INVALID_USAGE_NO_ERROR_TYPE_IN_RHS("checked.expr.invalid.usage.no.error.type.rhs"),
    CHECKED_EXPR_INVALID_USAGE_ALL_ERROR_TYPES_IN_RHS("checked.expr.invalid.usage.only.error.types.rhs"),
    CHECKED_EXPR_NO_ERROR_RETURN_IN_ENCL_INVOKABLE("checked.expr.no.error.return.in.encl.invokable"),
    CHECKED_EXPR_NO_MATCHING_ERROR_RETURN_IN_ENCL_INVOKABLE("checked.expr.no.matching.error.return.in.encl.invokable"),

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
    UNDEFINED_STREAM_REFERENCE("undefined.stream.reference"),
    UNDEFINED_STREAM_ATTRIBUTE("undefined.stream.attribute"),
    UNDEFINED_OUTPUT_STREAM_ATTRIBUTE("undefined.output.stream.attribute"),
    UNDEFINED_SELECT_EXPR_ALIAS("alias.not.defined"),
    SELECT_EXPR_ALIAS_NOT_FOUND("alias.not.found"),
    OUTPUT_FIELD_VISIBLE_IN_HAVING_ORDER_BY("output.fields.allowed.in.having.and.orderby.only"),
    STREAM_ATTR_NOT_ALLOWED_IN_HAVING_ORDER_BY("stream.attributes.not.allowed.in.having.and.orderby"),
    INCOMPATIBLE_FIELDS_IN_SELECT_CLAUSE("incompatible.fields.in.select.clause"),
    INCOMPATIBLE_STREAM_ACTION_ARGUMENT("incompatible.stream.action.argument"),
    INVALID_STREAM_ACTION_ARGUMENT_COUNT("invalid.stream.action.argument.count"),
    INVALID_STREAM_ACTION_ARGUMENT_TYPE("invalid.stream.action.argument.type"),
    INVALID_STREAM_ATTRIBUTE_TYPE("invalid.stream.attribute.type"),
    STREAMING_INCOMPATIBLE_TYPES("streaming.incompatible.types"),
    UNDEFINED_INVOCATION_ALIAS("undefined.invocation.alias"),
    INVALID_STREAMING_MODEL_TYPE("invalid.streaming.model.type"),
    PATTERNS_NOT_SUPPORTED("patterns.not.supported"),

    // Taint checking related codes
    ENTRY_POINT_PARAMETERS_CANNOT_BE_UNTAINTED("entry.point.parameters.cannot.be.untainted"),
    TAINTED_VALUE_PASSED_TO_UNTAINTED_PARAMETER("tainted.value.passed.to.untainted.parameter"),
    TAINTED_VALUE_PASSED_TO_UNTAINTED_PARAMETER_ORIGINATING_AT(
            "tainted.value.passed.to.untainted.param.in.obj.method"),
    TAINTED_VALUE_PASSED_TO_GLOBAL_VARIABLE("tainted.value.passed.to.global.variable"),
    TAINTED_VALUE_PASSED_TO_MODULE_OBJECT("tainted.value.passed.to.module.object"),
    INVOCATION_TAINT_GLOBAL_OBJECT("method.invocation.taint.global.object"),
    TAINTED_VALUE_PASSED_TO_CLOSURE_VARIABLE("tainted.value.passed.to.closure.variable"),
    UNABLE_TO_PERFORM_TAINT_CHECKING_WITH_RECURSION("unable.to.perform.taint.checking.with.recursion"),
    UNABLE_TO_PERFORM_TAINT_CHECKING_FOR_BUILTIN_METHOD("unable.to.perform.taint.checking.for.builtin.method"),
    TAINTED_RETURN_NOT_ANNOTATED_TAINTED("tainted.return.not.annotated.tainted"),
    TAINTED_PARAM_NOT_ANNOTATED_TAINTED("tainted.param.not.annotated.tainted"),

    // Constants related codes.
    TYPE_REQUIRED_FOR_CONST_WITH_EXPRESSIONS("type.required.for.const.with.expressions"),
    CANNOT_UPDATE_CONSTANT_VALUE("cannot.update.constant.value"),
    CANNOT_ASSIGN_VALUE_TO_CONSTANT("cannot.assign.value.to.constant"),
    CANNOT_DEFINE_CONSTANT_WITH_TYPE("cannot.define.constant.with.type"),
    EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION("expression.is.not.a.constant.expression"),
    INVALID_CONST_EXPRESSION("invalid.const.expression"),
    CONSTANT_EXPRESSION_NOT_SUPPORTED("const.expression.not.supported"),
    KEY_NOT_FOUND("key.not.found"),

    // Anonymous functions related codes
    ARROW_EXPRESSION_MISMATCHED_PARAMETER_LENGTH("arrow.expression.mismatched.parameter.length"),
    ARROW_EXPRESSION_CANNOT_INFER_TYPE_FROM_LHS("arrow.expression.cannot.infer.type.from.lhs"),
    ARROW_EXPRESSION_NOT_SUPPORTED_ITERABLE_OPERATION("arrow.expression.not.supported.iterable.operation"),

    INCOMPATIBLE_TYPE_CHECK("incompatible.type.check"),
    UNNECESSARY_CONDITION("unnecessary.condition"),

    INVALID_USAGE_OF_CLONE("clone.invocation.invalid"),

    // Dataflow analysis related error codes
    PARTIALLY_INITIALIZED_VARIABLE("partially.initialized.variable"),

    CANNOT_INFER_TYPE("cannot.infer.type"),
    CANNOT_INFER_ERROR_TYPE("cannot.infer.error.type"),
    INVALID_ERROR_CONSTRUCTOR_DETAIL("invalid.error.detail.rec.does.not.match"),
    INDIRECT_ERROR_CTOR_REASON_NOT_ALLOWED("invalid.error.reason.argument.to.indirect.error.constructor"),
    INDIRECT_ERROR_CTOR_NOT_ALLOWED_ON_NON_CONST_REASON("invalid.indirect.error.constructor.invocation"),

    // Seal inbuilt function related codes
    INCOMPATIBLE_STAMP_TYPE("incompatible.stamp.type"),
    NOT_SUPPORTED_SOURCE_TYPE_FOR_STAMP("not.supported.source.for.stamp"),

    // Worker flush action related error codes
    INVALID_WORKER_FLUSH("invalid.worker.flush.expression"),
    INVALID_WORKER_FLUSH_FOR_WORKER("invalid.worker.flush.expression.for.worker"),

    // Worker receive and send related error codes
    INVALID_TYPE_FOR_RECEIVE("invalid.type.for.receive"),
    INVALID_TYPE_FOR_SEND("invalid.type.for.send"),

    INVALID_USAGE_OF_RECEIVE_EXPRESSION("invalid.usage.of.receive.expression"),
    INVALID_USE_OF_EXPERIMENTAL_FEATURE("invalid.use.of.experimental.feature"),

    INVALID_USE_OF_NULL_LITERAL("invalid.use.of.null.literal"),

    // Type Param related error codes.
    TYPE_PARAM_OUTSIDE_LANG_MODULE("type.param.outside.lang.module"),

    INVALID_INVOCATION_LVALUE_ASSIGNMENT("invalid.lvalue.lhs.of.assignment"),
    INVALID_INVOCATION_LVALUE_COMPOUND_ASSIGNMENT("invalid.lvalue.lhs.of.compound.assignment"),

    IDENTIFIER_LITERAL_ONLY_SUPPORTS_ALPHANUMERICS("identifier.literal.only.supports.alphanumerics"),

    // Warnings
    MULTI_LINE_STRINGS_NOT_ALLOWED("multi.line.strings.not.allowed"),
    SEND_RECEIVE_TYPE_MISMATCH("send.receive.type.mismatch")

    ;
    private String value;

    DiagnosticCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
