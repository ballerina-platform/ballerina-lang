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

import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This class contains a list of diagnostic error codes.
 *
 * @since 0.94
 */
public enum DiagnosticErrorCode implements DiagnosticCode {

    UNDEFINED_MODULE("BCE2000", "undefined.module"),
    CYCLIC_MODULE_IMPORTS_DETECTED("BCE2001", "cyclic.module.imports.detected"),
    UNUSED_IMPORT_MODULE("BCE2002", "unused.import.module"),
    MODULE_NOT_FOUND("BCE2003", "module.not.found"),
    REDECLARED_IMPORT_MODULE("BCE2004", "redeclared.import.module"),
    INVALID_MODULE_DECLARATION("BCE2005", "invalid.module.declaration"),
    MISSING_MODULE_DECLARATION("BCE2006", "missing.module.declaration"),
    UNEXPECTED_MODULE_DECLARATION("BCE2007", "unexpected.module.declaration"),
    REDECLARED_SYMBOL("BCE2008", "redeclared.symbol"),
    REDECLARED_BUILTIN_SYMBOL("BCE2009", "redeclared.builtin.symbol"),
    UNDEFINED_SYMBOL("BCE2010", "undefined.symbol"),
    UNDEFINED_FUNCTION("BCE2011", "undefined.function"),
    UNDEFINED_FUNCTION_IN_TYPE("BCE2012", "undefined.function.in.type"),
    UNDEFINED_METHOD_IN_OBJECT("BCE2013", "undefined.method.in.object"),
    UNDEFINED_FIELD_IN_RECORD("BCE2014", "undefined.field.in.record"),
    UNDEFINED_CONNECTOR("BCE2015", "undefined.connector"),
    INVALID_ERROR_REASON_TYPE("BCE2016", "invalid.error.reason.type"),
    UNSUPPORTED_ERROR_REASON_CONST_MATCH(
            "BCE2017", "error.match.over.const.reason.ref.not.supported"),
    INVALID_ERROR_DETAIL_TYPE("BCE2018", "invalid.error.detail.type"),
    ERROR_DETAIL_ARG_IS_NOT_NAMED_ARG("BCE2019", "error.detail.arg.not.named.arg"),
    DIRECT_ERROR_CTOR_REASON_NOT_PROVIDED("BCE2020", "missing.error.reason"),
    OBJECT_TYPE_NOT_ALLOWED("BCE2021", "object.type.not.allowed"),
    OBJECT_TYPE_REQUIRED("BCE2022", "object.type.required"),
    UNDEFINED_STRUCTURE_FIELD_WITH_TYPE("BCE2023", "undefined.field.in.structure.with.type"),
    UNDEFINED_STRUCTURE_FIELD("BCE2024", "undefined.field.in.structure"),
    TYPE_NOT_ALLOWED_WITH_NEW("BCE2025", "type.not.allowed.with.new"),
    INVALID_INTERSECTION_TYPE("BCE2026", "invalid.intersection.type"),
    INVALID_NON_READONLY_INTERSECTION_TYPE("BCE2027", "invalid.non.readonly.intersection.type"),
    INVALID_READONLY_INTERSECTION_TYPE("BCE2028", "invalid.readonly.intersection.type"),
    INVALID_READONLY_OBJECT_INTERSECTION_TYPE("BCE2029", "invalid.readonly.object.intersection.type"),
    INVALID_READONLY_OBJECT_TYPE("BCE2030", "invalid.readonly.object.type"),
    INVALID_READONLY_MAPPING_FIELD("BCE2031", "invalid.readonly.mapping.field"),
    STREAM_INVALID_CONSTRAINT("BCE2032", "stream.invalid.constraint"),
    STREAM_INIT_NOT_ALLOWED_HERE("BCE2033", "stream.initialization.not.allowed.here"),
    CANNOT_INFER_OBJECT_TYPE_FROM_LHS("BCE2034", "cannot.infer.object.type.from.lhs"),
    OBJECT_UNINITIALIZED_FIELD("BCE2035", "object.uninitialized.field"),
    CYCLIC_TYPE_REFERENCE("BCE2036", "cyclic.type.reference"),
    ATTEMPT_REFER_NON_ACCESSIBLE_SYMBOL("BCE2037", "attempt.refer.non.accessible.symbol"),
    ATTEMPT_EXPOSE_NON_PUBLIC_SYMBOL("BCE2038", "attempt.expose.non.public.symbol"),
    UNDEFINED_PARAMETER("BCE2039", "undefined.parameter"),
    ATTACHED_FUNCTIONS_MUST_HAVE_BODY("BCE2040", "attached.functions.must.have.body"),
    ABSTRACT_OBJECT_CONSTRUCTOR("BCE2041", "abstract.object.constructor"),
    CANNOT_INITIALIZE_ABSTRACT_OBJECT("BCE2042", "cannot.initialize.abstract.object"),
    INVALID_INTERFACE_ON_NON_ABSTRACT_OBJECT("BCE2043", "invalid.interface.of.non.abstract.object"),
    UNIMPLEMENTED_REFERENCED_METHOD_IN_CLASS("BCE2044", "unimplemented.referenced.method.in.class"),
    PRIVATE_FUNCTION_VISIBILITY("BCE2045", "private.function.visibility"),
    CANNOT_ATTACH_FUNCTIONS_TO_ABSTRACT_OBJECT("BCE2046", "cannot.attach.functions.to.abstract.object"),
    ABSTRACT_OBJECT_FUNCTION_CANNOT_HAVE_BODY("BCE2047", "abstract.object.function.cannot.have.body"),
    PRIVATE_OBJECT_CONSTRUCTOR("BCE2048", "private.object.constructor"),
    PRIVATE_FIELD_ABSTRACT_OBJECT("BCE2049", "private.field.abstract.object"),
    FIELD_WITH_DEFAULT_VALUE_ABSTRACT_OBJECT("BCE2050", "field.with.default.value.abstract.object"),
    PRIVATE_FUNC_ABSTRACT_OBJECT("BCE2051", "private.function.abstract.object"),
    EXTERN_FUNC_ABSTRACT_OBJECT("BCE2052", "extern.function.abstract.object"),
    RESOURCE_FUNCTION_CANNOT_BE_EXTERN("BCE2053", "resource.function.cannot.be.extern"),
    OBJECT_INIT_FUNCTION_CANNOT_BE_EXTERN("BCE2054", "object.init.function.cannot.be.extern"),
    GLOBAL_VARIABLE_CYCLIC_DEFINITION("BCE2055", "global.variable.cyclic.reference"),
    CANNOT_FIND_ERROR_TYPE("BCE2056", "cannot.find.error.constructor.for.type"),
    INVALID_PACKAGE_NAME_QUALIFER("BCE2057", "invalid.package.name.qualifier"),
    INVALID_FIELD_ACCESS_EXPRESSION("BCE2058", "invalid.char.colon.in.field.access.expr"),
    VARIABLE_DECL_WITH_VAR_WITHOUT_INITIALIZER("BCE2059", "variable.decl.with.var.without.initializer"),

    REQUIRED_PARAM_DEFINED_AFTER_DEFAULTABLE_PARAM("BCE2060", "required.param.not.allowed.after.defaultable.param"),
    POSITIONAL_ARG_DEFINED_AFTER_NAMED_ARG("BCE2061", "positional.arg.defined.after.named.arg"),
    REST_ARG_DEFINED_AFTER_NAMED_ARG("BCE2062", "rest.arg.defined.after.named.arg"),
    MISSING_REQUIRED_PARAMETER("BCE2063", "missing.required.parameter"),
    MISSING_REQUIRED_ARG_ERROR_MESSAGE("BCE2064", "missing.required.parameter.error.message"),
    OBJECT_CTOR_INIT_CANNOT_HAVE_PARAMETERS("BCE2065", "object.constructor.init.function.cannot.have.parameters"),
    OBJECT_CTOR_DOES_NOT_SUPPORT_TYPE_REFERENCE_MEMBERS(
            "BCE2066", "object.constructor.does.not.support.type.reference.members"),

    INCOMPATIBLE_TYPES("BCE2067", "incompatible.types"),
    INCOMPATIBLE_TYPES_SPREAD_OP("BCE2068", "incompatible.types.spread.op"),
    INCOMPATIBLE_TYPES_FIELD("BCE2069", "incompatible.types.field"),
    UNKNOWN_TYPE("BCE2070", "unknown.type"),
    BINARY_OP_INCOMPATIBLE_TYPES("BCE2071", "binary.op.incompatible.types"),
    UNARY_OP_INCOMPATIBLE_TYPES("BCE2072", "unary.op.incompatible.types"),
    SELF_REFERENCE_VAR("BCE2073", "self.reference.var"),
    INVALID_WORKER_SEND_POSITION("BCE2074", "invalid.worker.send.position"),
    INVALID_WORKER_RECEIVE_POSITION("BCE2075", "invalid.worker.receive.position"),
    UNDEFINED_WORKER("BCE2076", "undefined.worker"),
    INVALID_WORKER_JOIN_RESULT_TYPE("BCE2077", "invalid.worker.join.result.type"),
    INVALID_WORKER_TIMEOUT_RESULT_TYPE("BCE2078", "invalid.worker.timeout.result.type"),
    INVALID_WORKER_REFERRENCE("BCE2079", "invalid.worker.reference"),
    INCOMPATIBLE_TYPE_CONSTRAINT("BCE2080", "incompatible.type.constraint"),
    USAGE_OF_WORKER_WITHIN_LOCK_IS_PROHIBITED("BCE2081", "usage.of.worker.within.lock.is.prohibited"),
    USAGE_OF_START_WITHIN_LOCK_IS_PROHIBITED("BCE2082", "usage.of.start.within.lock.is.prohibited"),
    WORKER_SEND_RECEIVE_PARAMETER_COUNT_MISMATCH("BCE2083", "worker.send.receive.parameter.count.mismatch"),
    INVALID_WORKER_INTERACTION("BCE2084", "worker.invalid.worker.interaction"),
    WORKER_INTERACTIONS_ONLY_ALLOWED_BETWEEN_PEERS("BCE2085", "worker.interactions.only.allowed.between.peers"),
    WORKER_SEND_AFTER_RETURN("BCE2086", "worker.send.after.return"),
    WORKER_RECEIVE_AFTER_RETURN("BCE2087", "worker.receive.after.return"),
    EXPLICIT_WORKER_CANNOT_BE_DEFAULT("BCE2088", "explicit.worker.cannot.be.default"),
    INVALID_MULTIPLE_FORK_JOIN_SEND("BCE2089", "worker.multiple.fork.join.send"),
    INCOMPATIBLE_TYPE_REFERENCE("BCE2090", "incompatible.type.reference"),
    INCOMPATIBLE_TYPE_REFERENCE_NON_PUBLIC_MEMBERS("BCE2091", "incompatible.type.reference.non.public.members"),
    INCOMPATIBLE_RECORD_TYPE_REFERENCE("BCE2092", "incompatible.record.type.reference"),
    REDECLARED_TYPE_REFERENCE("BCE2093", "redeclared.type.reference"),
    REDECLARED_FUNCTION_FROM_TYPE_REFERENCE("BCE2094", "redeclared.function.from.type.reference"),
    REFERRED_FUNCTION_SIGNATURE_MISMATCH("BCE2095", "referred.function.signature.mismatch"),

    INVOKABLE_MUST_RETURN("BCE2096", "invokable.must.return"),
    MAIN_SHOULD_BE_PUBLIC("BCE2097", "main.should.be.public"),
    MAIN_PARAMS_SHOULD_BE_ANYDATA("BCE2098", "main.params.should.be.anydata"),
    MAIN_RETURN_SHOULD_BE_ERROR_OR_NIL("BCE2099", "main.return.should.be.error.or.nil"),
    MODULE_INIT_CANNOT_BE_PUBLIC("BCE2100", "module.init.cannot.be.public"),
    MODULE_INIT_CANNOT_HAVE_PARAMS("BCE2101", "module.init.cannot.have.params"),
    MODULE_INIT_RETURN_SHOULD_BE_ERROR_OR_NIL("BCE2102", "module.init.return.should.be.error.or.nil"),
    ATLEAST_ONE_WORKER_MUST_RETURN("BCE2103", "atleast.one.worker.must.return"),
    FORK_JOIN_WORKER_CANNOT_RETURN("BCE2104", "fork.join.worker.cannot.return"),
    FORK_JOIN_INVALID_WORKER_COUNT("BCE2105", "fork.join.invalid.worker.count"),
    INVALID_FOR_JOIN_SYNTAX_EMPTY_FORK("BCE2106", "fork.join.syntax.empty.fork"),
    UNREACHABLE_CODE("BCE2107", "unreachable.code"),
    CONTINUE_CANNOT_BE_OUTSIDE_LOOP("BCE2108", "continue.cannot.be.outside.loop"),
    BREAK_CANNOT_BE_OUTSIDE_LOOP("BCE2109", "break.cannot.be.outside.loop"),

    INTEGER_TOO_LARGE("BCE2110", "integer.too.large"),
    INTEGER_TOO_SMALL("BCE2111", "integer.too.small"),
    HEXADECIMAL_TOO_LARGE("BCE2112", "hexadecimal.too.large"),
    HEXADECIMAL_TOO_SMALL("BCE2113", "hexadecimal.too.small"),

    //Transaction related error codes
    ROLLBACK_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK("BCE2300", "rollback.cannot.be.outside.transaction.block"),
    COMMIT_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK("BCE2301", "commit.cannot.be.outside.transaction.block"),
    RETRY_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK("BCE2302", "retry.cannot.be.outside.transaction.block"),
    BREAK_CANNOT_BE_USED_TO_EXIT_TRANSACTION("BCE2303", "break.statement.cannot.be.used.to.exit.from.a.transaction"),
    CONTINUE_CANNOT_BE_USED_TO_EXIT_TRANSACTION(
            "BCE2304", "continue.statement.cannot.be.used.to.exit.from.a.transaction"),
    CHECK_EXPRESSION_INVALID_USAGE_WITHIN_TRANSACTION_BLOCK(
            "BCE2305", "check.expression.invalid.usage.within.transaction.block"),
    RETURN_CANNOT_BE_USED_TO_EXIT_TRANSACTION("BCE2306", "return.statement.cannot.be.used.to.exit.from.a.transaction"),
    DONE_CANNOT_BE_USED_TO_EXIT_TRANSACTION("BCE2307", "done.statement.cannot.be.used.to.exit.from.a.transaction"),
    INVALID_RETRY_COUNT("BCE2308", "invalid.retry.count"),
    INVALID_COMMIT_COUNT("BCE2309", "invalid.commit.count"),
    INVALID_ROLLBACK_COUNT("BCE2310", "invalid.rollback.count"),
    INVALID_TRANSACTION_HANDLER_ARGS("BCE2311", "invalid.transaction.handler.args"),
    INVALID_TRANSACTION_HANDLER_SIGNATURE("BCE2312", "invalid.transaction.handler.signature"),
    LAMBDA_REQUIRED_FOR_TRANSACTION_HANDLER("BCE2313", "lambda.required.for.transaction.handler"),
    TRANSACTION_CANNOT_BE_USED_WITHIN_HANDLER("BCE2314", "transaction.cannot.be.used.within.handler"),
    TRANSACTION_CANNOT_BE_USED_WITHIN_TRANSACTIONAL_SCOPE(
            "BCE2315", "transaction.cannot.be.used.within.transactional.scope"),
    TRANSACTIONAL_FUNC_INVOKE_PROHIBITED("BCE2316", "transactional.function.prohibited.outside.transactional.scope"),
    TRANSACTIONAL_WORKER_OUT_OF_TRANSACTIONAL_SCOPE(
            "BCE2317", "transactional.worker.prohibited.outside.transactional.scope"),

    NESTED_TRANSACTIONS_ARE_INVALID("BCE2318", "nested.transactions.are.invalid"),
    INVALID_FUNCTION_POINTER_ASSIGNMENT_FOR_HANDLER("BCE2319", "invalid.function.pointer.assignment.for.handler"),
    USAGE_OF_START_WITHIN_TRANSACTION_IS_PROHIBITED("BCE2320", "usage.of.start.within.transaction.is.prohibited"),
    ROLLBACK_CANNOT_BE_WITHIN_TRANSACTIONAL_FUNCTION("BCE2321", "rollback.cannot.be.within.transactional.function"),
    COMMIT_CANNOT_BE_WITHIN_TRANSACTIONAL_FUNCTION("BCE2322", "commit.cannot.be.within.transactional.function"),
    MAX_ONE_COMMIT_ROLLBACK_ALLOWED_WITHIN_A_BRANCH("BCE2323", "max.one.commit.rollback.allowed.within.branch"),
    COMMIT_NOT_ALLOWED("BCE2324", "commit.not.allowed"),
    ROLLBACK_NOT_ALLOWED("BCE2325", "rollback.not.allowed"),

    // Service, endpoint related errors codes
    SERVICE_INVALID_OBJECT_TYPE("BCE2400", "service.invalid.object.type"),
    SERVICE_INVALID_ENDPOINT_TYPE("BCE2401", "service.invalid.endpoint.type"),
    SERVICE_FUNCTION_INVALID_MODIFIER("BCE2402", "service.function.invalid.modifier"),
    SERVICE_FUNCTION_INVALID_INVOCATION("BCE2403", "service.function.invalid.invocation"),
    SERVICE_SERVICE_TYPE_REQUIRED_ANONYMOUS("BCE2404", "service.service.type.required.anonymous"),

    ENDPOINT_OBJECT_TYPE_REQUIRED("BCE2405", "endpoint.object.type.required"),
    ENDPOINT_OBJECT_NEW_HAS_PARAM("BCE2406", "endpoint.object.new.has.param"),
    ENDPOINT_INVALID_TYPE("BCE2407", "endpoint.invalid.type"),
    ENDPOINT_INVALID_TYPE_NO_FUNCTION("BCE2408", "endpoint.invalid.type.no.function"),
    ENDPOINT_SPI_INVALID_FUNCTION("BCE2409", "endpoint.spi.invalid.function"),

    REMOTE_FUNCTION_IN_NON_CLIENT_OBJECT("BCE2410", "remote.function.in.non.client.object"),
    RESOURCE_FUNCTION_IN_NON_SERVICE_OBJECT("BCE2411", "resource.function.in.non.service.object"),
    RESOURCE_FUNCTION_INVALID_RETURN_TYPE("BCE2412", "resource.function.invalid.return.type"),
    REMOTE_IN_NON_OBJECT_FUNCTION("BCE2413", "remote.in.non.object.function"),
    INVALID_LISTENER_VARIABLE("BCE2414", "invalid.listener.var"),
    INVALID_LISTENER_ATTACHMENT("BCE2415", "invalid.listener.attachment"),

    ENDPOINT_NOT_SUPPORT_REGISTRATION("BCE2416", "endpoint.not.support.registration"),
    INVALID_ACTION_INVOCATION_SYNTAX("BCE2417", "invalid.action.invocation.syntax"),
    INVALID_METHOD_INVOCATION_SYNTAX("BCE2418", "invalid.method.invocation.syntax"),
    INVALID_INIT_INVOCATION("BCE2419", "invalid.init.invocation"),
    INVALID_RESOURCE_FUNCTION_INVOCATION("BCE2420", "invalid.resource.function.invocation"),
    INVALID_ACTION_INVOCATION("BCE2421", "invalid.action.invocation"),

    TYPE_CAST_NOT_YET_SUPPORTED("BCE2423", "type.cast.not.yet.supported.for.type"),
    EQUALITY_NOT_YET_SUPPORTED("BCE2424", "equality.not.yet.supported.for.type"),
    BINDING_PATTERN_NOT_YET_SUPPORTED("BCE2425", "binding.pattern.not.yet.supported.for.type"),
    LET_EXPRESSION_NOT_YET_SUPPORTED_RECORD_FIELD("BCE2426", "let.expression.not.yet.supported.record.field"),
    LET_EXPRESSION_NOT_YET_SUPPORTED_OBJECT_FIELD("BCE2427", "let.expression.not.yet.supported.object.field"),

    // Cast and conversion related codes
    INCOMPATIBLE_TYPES_CAST("BCE2500", "incompatible.types.cast"),
    INCOMPATIBLE_TYPES_CAST_WITH_SUGGESTION("BCE2501", "incompatible.types.cast.with.suggestion"),
    INCOMPATIBLE_TYPES_CONVERSION("BCE2502", "incompatible.types.conversion"),
    INCOMPATIBLE_TYPES_CONVERSION_WITH_SUGGESTION("BCE2503", "incompatible.types.conversion.with.suggestion"),
    UNSAFE_CAST_ATTEMPT("BCE2504", "unsafe.cast.attempt"),
    UNSAFE_CONVERSION_ATTEMPT("BCE2505", "unsafe.conversion.attempt"),

    INVALID_LITERAL_FOR_TYPE("BCE2506", "invalid.literal.for.type"),
    INCOMPATIBLE_MAPPING_CONSTRUCTOR("BCE2507", "incompatible.mapping.constructor.expression"),
    MAPPING_CONSTRUCTOR_COMPATIBLE_TYPE_NOT_FOUND("BCE2508", "mapping.constructor.compatible.type.not.found"),
    CANNOT_INFER_TYPES_FOR_TUPLE_BINDING("BCE2509", "cannot.infer.types.for.tuple.binding"),
    INVALID_LITERAL_FOR_MATCH_PATTERN("BCE2510", "invalid.literal.for.match.pattern"),
    INVALID_EXPR_WITH_TYPE_GUARD_FOR_MATCH_PATTERN("BCE2511", "invalid.expr.with.type.guard.for.match"),
    ARRAY_LITERAL_NOT_ALLOWED("BCE2512", "array.literal.not.allowed"),
    STRING_TEMPLATE_LIT_NOT_ALLOWED("BCE2513", "string.template.literal.not.allowed"),
    INVALID_RECORD_LITERAL_KEY("BCE2514", "invalid.record.literal.key"),
    INVALID_RECORD_LITERAL_IDENTIFIER_KEY("BCE2515", "invalid.record.literal.identifier.key"),
    INVALID_FIELD_NAME_RECORD_LITERAL("BCE2516", "invalid.field.name.record.lit"),
    REST_FIELD_NOT_ALLOWED_IN_SEALED_RECORDS("BCE2517", "rest.field.not.allowed"),
    OPEN_RECORD_CONSTRAINT_NOT_ALLOWED("BCE2518", "open.record.constraint.not.allowed"),
    INVALID_RECORD_REST_DESCRIPTOR("BCE2519", "invalid.record.rest.descriptor"),
    MISSING_REQUIRED_RECORD_FIELD("BCE2520", "missing.required.record.field"),
    DEFAULT_VALUES_NOT_ALLOWED_FOR_OPTIONAL_FIELDS("BCE2521", "default.values.not.allowed.for.optional.fields"),
    INVALID_FUNCTION_POINTER_INVOCATION("BCE2522", "invalid.function.pointer.invocation"),
    AMBIGUOUS_TYPES("BCE2523", "ambiguous.type"),

    TOO_MANY_ARGS_FUNC_CALL("BCE2524", "too.many.args.call"),
    ASSIGNMENT_COUNT_MISMATCH("BCE2525", "assignment.count.mismatch"),
    ASSIGNMENT_REQUIRED("BCE2526", "assignment.required"),
    MULTI_VAL_IN_SINGLE_VAL_CONTEXT("BCE2527", "multi.value.in.single.value.context"),
    MULTI_VAL_EXPR_IN_SINGLE_VAL_CONTEXT("BCE2528", "multi.valued.expr.in.single.valued.context"),
    DOES_NOT_RETURN_VALUE("BCE2529", "does.not.return.value"),
    FUNC_DEFINED_ON_NOT_SUPPORTED_TYPE("BCE2530", "func.defined.on.not.supported.type"),
    FUNC_DEFINED_ON_NON_LOCAL_TYPE("BCE2531", "func.defined.on.non.local.type"),
    INVALID_OBJECT_CONSTRUCTOR("BCE2532", "invalid.object.constructor"),
    RECORD_INITIALIZER_INVOKED("BCE2533", "explicit.invocation.of.record.init.is.not.allowed"),
    PKG_ALIAS_NOT_ALLOWED_HERE("BCE2534", "pkg.alias.not.allowed.here"),

    MULTI_VALUE_RETURN_EXPECTED("BCE2535", "multi.value.return.expected"),
    SINGLE_VALUE_RETURN_EXPECTED("BCE2536", "single.value.return.expected"),
    TOO_MANY_RETURN_VALUES("BCE2537", "return.value.too.many"),
    NOT_ENOUGH_RETURN_VALUES("BCE2538", "return.value.not.enough"),
    INVALID_FUNCTION_INVOCATION("BCE2539", "invalid.function.invocation"),
    INVALID_FUNCTION_INVOCATION_WITH_NAME("BCE2540", "invalid.function.invocation.with.name"),
    DUPLICATE_NAMED_ARGS("BCE2541", "duplicate.named.args"),
    INVALID_DEFAULT_PARAM_VALUE("BCE2542", "invalid.default.param.value"),

    DUPLICATED_ERROR_CATCH("BCE2543", "duplicated.error.catch"),

    NO_NEW_VARIABLES_VAR_ASSIGNMENT("BCE2544", "no.new.variables.var.assignment"),
    INVALID_VARIABLE_ASSIGNMENT("BCE2545", "invalid.variable.assignment"),
    INVALID_ASSIGNMENT_DECLARATION_FINAL("BCE2546", "invalid.variable.assignment.declaration.final"),
    CANNOT_ASSIGN_VALUE_FINAL("BCE2547", "cannot.assign.value.to.final.field"),
    CANNOT_ASSIGN_VALUE_TO_POTENTIALLY_INITIALIZED_FINAL(
            "BCE2548", "cannot.assign.value.to.potentially.initialized.final"),
    CANNOT_ASSIGN_VALUE_FUNCTION_ARGUMENT("BCE2549", "cannot.assign.value.to.function.argument"),
    CANNOT_ASSIGN_VALUE_ENDPOINT("BCE2550", "cannot.assign.value.to.endpoint"),
    CANNOT_UPDATE_READONLY_VALUE_OF_TYPE("BCE2551", "cannot.update.readonly.value.of.type"),
    CANNOT_UPDATE_READONLY_RECORD_FIELD("BCE2552", "cannot.update.readonly.record.field"),
    CANNOT_UPDATE_FINAL_OBJECT_FIELD("BCE2553", "cannot.update.final.object.field"),
    UNDERSCORE_NOT_ALLOWED("BCE2554", "underscore.not.allowed"),
    OPERATION_DOES_NOT_SUPPORT_INDEXING("BCE2555", "operation.does.not.support.indexing"),
    OPERATION_DOES_NOT_SUPPORT_FIELD_ACCESS("BCE2556", "operation.does.not.support.field.access"),
    OPERATION_DOES_NOT_SUPPORT_FIELD_ACCESS_FOR_ASSIGNMENT(
            "BCE2557", "operation.does.not.support.field.access.for.assignment"),
    OPERATION_DOES_NOT_SUPPORT_OPTIONAL_FIELD_ACCESS("BCE2558", "operation.does.not.support.optional.field.access"),
    OPERATION_DOES_NOT_SUPPORT_FIELD_ACCESS_FOR_NON_REQUIRED_FIELD(
            "BCE2559", "operation.does.not.support.field.access.for.non.required.field"),
    OPERATION_DOES_NOT_SUPPORT_OPTIONAL_FIELD_ACCESS_FOR_FIELD(
            "BCE2560", "operation.does.not.support.optional.field.access.for.field"),
    OPERATION_DOES_NOT_SUPPORT_INDEX_ACCESS_FOR_ASSIGNMENT(
            "BCE2561", "operation.does.not.support.index.access.for.assignment"),
    INVALID_INDEX_EXPR_STRUCT_FIELD_ACCESS("BCE2562", "invalid.index.expr.struct.field.access"),
    INVALID_INDEX_EXPR_TUPLE_FIELD_ACCESS("BCE2563", "invalid.index.expr.tuple.field.access"),
    INVALID_TUPLE_INDEX_EXPR("BCE2564", "invalid.tuple.index.expr"),
    INVALID_RECORD_INDEX_EXPR("BCE2565", "invalid.record.index.expr"),
    INVALID_ENUM_EXPR("BCE2566", "invalid.enum.expr"),
    INVALID_EXPR_IN_MATCH_STMT("BCE2567", "invalid.expr.in.match.stmt"),
    INVALID_PATTERN_CLAUSES_IN_MATCH_STMT("BCE2568", "invalid.pattern.clauses.in.match.stmt"),
    STATIC_MATCH_ONLY_SUPPORTS_ANYDATA("BCE2569", "static.value.match.only.supports.anydata"),
    USAGE_OF_UNINITIALIZED_VARIABLE("BCE2570", "usage.of.uninitialized.variable"),
    UNINITIALIZED_VARIABLE("BCE2571", "uninitialized.variable"),
    CONTAINS_UNINITIALIZED_FIELDS("BCE2572", "uninitialized.object.fields"),
    CONTAINS_UNINITIALIZED_VARIABLES("BCE2573", "uninitialized.variables"),
    INVALID_ANY_VAR_DEF("BCE2574", "invalid.any.var.def"),
    INVALID_RECORD_LITERAL("BCE2575", "invalid.record.literal"),
    INVALID_FIELD_IN_RECORD_BINDING_PATTERN("BCE2576", "invalid.field.in.record.binding.pattern"),
    INVALID_RECORD_LITERAL_BINDING_PATTERN("BCE2577", "invalid.record.literal.in.binding.pattern"),
    DUPLICATE_KEY_IN_RECORD_LITERAL("BCE2578", "duplicate.key.in.record.literal"),
    DUPLICATE_KEY_IN_TABLE_LITERAL("BCE2579", "duplicate.key.in.table.literal"),
    DUPLICATE_KEY_IN_RECORD_LITERAL_SPREAD_OP("BCE2580", "duplicate.key.in.record.literal.spread.op"),
    POSSIBLE_DUPLICATE_OF_FIELD_SPECIFIED_VIA_SPREAD_OP(
            "BCE2581", "possible.duplicate.of.field.specified.via.spread.op"),
    SPREAD_FIELD_MAY_DULPICATE_ALREADY_SPECIFIED_KEYS("BCE2582", "spread.field.may.duplicate.already.specified.keys"),
    MULTIPLE_INCLUSIVE_TYPES("BCE2583", "multiple.inclusive.types"),
    INVALID_ARRAY_LITERAL("BCE2584", "invalid.array.literal"),
    INVALID_TUPLE_LITERAL("BCE2585", "invalid.tuple.literal"),
    INVALID_LIST_CONSTRUCTOR_ELEMENT_TYPE("BCE2586", "invalid.list.constructor.type"),
    INVALID_ARRAY_ELEMENT_TYPE("BCE2587", "invalid.array.element.type"),
    INVALID_TUPLE_BINDING_PATTERN("BCE2588", "invalid.tuple.binding.pattern"),
    INVALID_TYPE_FOR_TUPLE_VAR_EXPRESSION("BCE2589", "invalid.type.for.tuple.var.expr"),
    INVALID_TUPLE_BINDING_PATTERN_DECL("BCE2590", "invalid.tuple.binding.pattern.decl"),
    INVALID_TUPLE_BINDING_PATTERN_INFERENCE("BCE2591", "invalid.tuple.binding.pattern.inference"),
    MISMATCHING_ARRAY_LITERAL_VALUES("BCE2592", "mismatching.array.literal.values"),
    SEALED_ARRAY_TYPE_NOT_INITIALIZED("BCE2593", "sealed.array.type.not.initialized"),
    INVALID_LIST_INDEX_EXPR("BCE2594", "invalid.list.index.expr"),
    INVALID_ARRAY_INDEX_EXPR("BCE2595", "invalid.array.index.expr"),
    SEALED_ARRAY_TYPE_CAN_NOT_INFER_SIZE("BCE2596", "sealed.array.type.can.not.infer.size"),
    INVALID_SORT_FUNC_RETURN_TYPE("BCE2597", "invalid.key.func.return.type"),
    INVALID_SORT_ARRAY_MEMBER_TYPE("BCE2598", "invalid.sort.array.member.type"),
    // TODO Maryam remove list array tuple and use first only
    INDEX_OUT_OF_RANGE("BCE2599", "index.out.of.range"),
    LIST_INDEX_OUT_OF_RANGE("BCE2600", "list.index.out.of.range"),
    ARRAY_INDEX_OUT_OF_RANGE("BCE2601", "array.index.out.of.range"),
    TUPLE_INDEX_OUT_OF_RANGE("BCE2602", "tuple.index.out.of.range"),
    INVALID_ARRAY_SIZE_REFERENCE("BCE2603", "invalid.array.size.reference"),
    INVALID_TYPE_FOR_REST_DESCRIPTOR("BCE2604", "invalid.type.for.rest.descriptor"),
    INVALID_TYPE_NEW_LITERAL("BCE2605", "invalid.type.new.literal"),
    INVALID_USAGE_OF_KEYWORD("BCE2606", "invalid.usage.of.keyword"),
    INVALID_TYPE_OBJECT_CONSTRUCTOR("BCE2607", "invalid.type.object.constructor"),

    INVALID_RECORD_BINDING_PATTERN("BCE2608", "invalid.record.binding.pattern"),
    NO_MATCHING_RECORD_REF_PATTERN("BCE2609", "no.matching.record.ref.found"),
    MULTIPLE_RECORD_REF_PATTERN_FOUND("BCE2610", "multiple.matching.record.ref.found"),
    NOT_ENOUGH_PATTERNS_TO_MATCH_RECORD_REF("BCE2611", "not.enough.patterns.to.match.record.ref"),
    INVALID_TYPE_DEFINITION_FOR_RECORD_VAR("BCE2612", "invalid.type.definition.for.record.var"),

    INVALID_ERROR_BINDING_PATTERN("BCE2613", "invalid.error.binding.pattern"),
    INVALID_ERROR_REASON_BINDING_PATTERN("BCE2614", "invalid.error.reason.binding.pattern"),
    INVALID_ERROR_REST_BINDING_PATTERN("BCE2615", "invalid.error.rest.binding.pattern"),
    INVALID_TYPE_DEFINITION_FOR_ERROR_VAR("BCE2616", "invalid.type.definition.for.error.var"),
    INVALID_ERROR_DESTRUCTURING_NO_REASON_GIVEN("BCE2617", "invalid.error.destructuring.reason"),
    INVALID_ERROR_MATCH_PATTERN("BCE2618", "invalid.error.match.pattern"),
    DUPLICATE_VARIABLE_IN_BINDING_PATTERN("BCE2619", "duplicate.variable.in.binding.pattern"),
    INVALID_VARIABLE_REFERENCE_IN_BINDING_PATTERN("BCE2620", "invalid.variable.reference.in.binding.pattern"),
    MISSING_REQUIRED_ARG_BINDING_PATTERN_ERROR_MESSAGE("BCE2621", "missing.error.arg.binding.pattern.error.message"),

    INVALID_NAMESPACE_PREFIX("BCE2622", "invalid.namespace.prefix"),
    XML_TAGS_MISMATCH("BCE2623", "mismatching.xml.start.end.tags"),
    XML_ATTRIBUTE_MAP_UPDATE_NOT_ALLOWED("BCE2624", "xml.attribute.map.update.not.allowed"),
    XML_QNAME_UPDATE_NOT_ALLOWED("BCE2625", "xml.qname.update.not.allowed"),
    INVALID_NAMESPACE_DECLARATION("BCE2626", "invalid.namespace.declaration"),
    CANNOT_UPDATE_XML_SEQUENCE("BCE2627", "cannot.update.xml.sequence"),
    INVALID_XML_NS_INTERPOLATION("BCE2628", "invalid.xml.ns.interpolation"),
    CANNOT_FIND_XML_NAMESPACE("BCE2629", "cannot.find.xml.namespace.prefix"),
    UNSUPPORTED_METHOD_INVOCATION_XML_NAV("BCE2630", "method.invocation.in.xml.navigation.expressions.not.supported"),
    DEPRECATED_XML_ATTRIBUTE_ACCESS("BCE2631", "deprecated.xml.attribute.access.expression"),
    UNSUPPORTED_INDEX_IN_XML_NAVIGATION("BCE2632", "indexing.within.xml.navigation.expression.not.supported"),

    UNDEFINED_ANNOTATION("BCE2633", "undefined.annotation"),
    ANNOTATION_NOT_ALLOWED("BCE2634", "annotation.not.allowed"),
    ANNOTATION_ATTACHMENT_CANNOT_HAVE_A_VALUE("BCE2635", "annotation.attachment.cannot.have.a.value"),
    ANNOTATION_ATTACHMENT_REQUIRES_A_VALUE("BCE2636", "annotation.attachment.requires.a.value"),
    ANNOTATION_ATTACHMENT_CANNOT_SPECIFY_MULTIPLE_VALUES(
            "BCE2637", "annotation.attachment.cannot.specify.multiple.values"),
    ANNOTATION_INVALID_TYPE("BCE2638", "annotation.invalid.type"),
    ANNOTATION_INVALID_CONST_TYPE("BCE2639", "annotation.invalid.const.type"),
    ANNOTATION_REQUIRES_CONST("BCE2640", "annotation.requires.const"),
    INCOMPATIBLE_TYPES_ARRAY_FOUND("BCE2641", "incompatible.types.array.found"),
    CANNOT_GET_ALL_FIELDS("BCE2642", "cannot.get.all.fields"),

    INVALID_DOCUMENTATION_IDENTIFIER("BCE2643", "invalid.documentation.identifier"),

    OPERATOR_NOT_SUPPORTED("BCE2644", "operator.not.supported"),
    OPERATOR_NOT_ALLOWED_VARIABLE("BCE2645", "operator.not.allowed.variable"),
    NEVER_TYPE_NOT_ALLOWED_FOR_REQUIRED_FIELDS("BCE2646", "never.type.not.allowed.for.required.fields"),
    INVALID_NEVER_RETURN_TYPED_FUNCTION_INVOCATION("BCE2647", "invalid.never.return.typed.function.invocation"),
    NEVER_TYPED_VAR_DEF_NOT_ALLOWED("BCE2648", "never.typed.var.def.not.allowed"),

    // Error codes related to iteration.
    ITERABLE_NOT_SUPPORTED_COLLECTION("BCE2800", "iterable.not.supported.collection"),
    INCOMPATIBLE_ITERATOR_FUNCTION_SIGNATURE("BCE2801", "incompatible.iterator.function.signature"),
    ITERABLE_NOT_SUPPORTED_OPERATION("BCE2802", "iterable.not.supported.operation"),
    ITERABLE_TOO_MANY_VARIABLES("BCE2803", "iterable.too.many.variables"),
    ITERABLE_NOT_ENOUGH_VARIABLES("BCE2804", "iterable.not.enough.variables"),
    ITERABLE_TOO_MANY_RETURN_VARIABLES("BCE2805", "iterable.too.many.return.args"),
    ITERABLE_NOT_ENOUGH_RETURN_VARIABLES("BCE2806", "iterable.not.enough.return.args"),
    ITERABLE_LAMBDA_REQUIRED("BCE2807", "iterable.lambda.required"),
    ITERABLE_LAMBDA_TUPLE_REQUIRED("BCE2808", "iterable.lambda.tuple.required"),
    ITERABLE_NO_ARGS_REQUIRED("BCE2809", "iterable.no.args.required"),
    ITERABLE_LAMBDA_INCOMPATIBLE_TYPES("BCE2810", "iterable.lambda.incompatible.types"),
    ITERABLE_RETURN_TYPE_MISMATCH("BCE2811", "iterable.return.type.mismatch"),

    // match statement related errors
    MATCH_STMT_CANNOT_GUARANTEE_A_MATCHING_PATTERN("BCE2900", "match.stmt.cannot.guarantee.a.matching.pattern"),
    MATCH_STMT_UNREACHABLE_PATTERN("BCE2901", "match.stmt.unreachable.pattern"),
    MATCH_STMT_UNMATCHED_PATTERN("BCE2902", "match.stmt.unmatched.pattern"),
    MATCH_STMT_PATTERN_ALWAYS_MATCHES("BCE2903", "match.stmt.pattern.always.matches"),
    MATCH_STMT_PATTERN_UNREACHABLE("BCE2904", "match.stmt.unreachable.pattern.available"),

    MATCH_PATTERN_NOT_SUPPORTED("BCE2905", "match.pattern.not.supported"),
    MATCH_PATTERNS_SHOULD_CONTAIN_SAME_SET_OF_VARIABLES(
            "BCE2906", "match.patterns.should.contain.same.set.of.variables"),
    MATCH_PATTERN_CANNOT_REPEAT_SAME_VARIABLE("BCE2907", "match.pattern.cannot.repeat.same.variable"),
    REST_MATCH_PATTERN_NOT_SUPPORTED("BCE2908", "rest.match.pattern.not.supported"),
    VARIABLE_SHOULD_BE_DECLARED_AS_CONSTANT("BCE2909", "match.pattern.variable.should.declared.as.constant"),
    MATCH_STMT_CONTAINS_TWO_DEFAULT_PATTERNS("BCE2910", "match.stmt.contains.two.default.patterns"),

    THROW_STMT_NOT_SUPPORTED("BCE2911", "throw.stmt.not.supported"),
    TRY_STMT_NOT_SUPPORTED("BCE2912", "try.stmt.not.supported"),

    UNKNOWN_BUILTIN_FUNCTION("BCE2913", "unknown.builtin.method"),
    UNSUPPORTED_BUILTIN_METHOD("BCE2914", "unsupported.builtin.method"),

    // Safe navigation operator related errors
    SAFE_NAVIGATION_NOT_REQUIRED("BCE3000", "safe.navigation.not.required"),
    OPTIONAL_FIELD_ACCESS_NOT_REQUIRED_ON_LHS("BCE3001", "optional.field.access.not.required.on.lhs"),

    // Checked expression related errors
    CHECKED_EXPR_INVALID_USAGE_NO_ERROR_TYPE_IN_RHS("BCE3030", "checked.expr.invalid.usage.no.error.type.rhs"),
    CHECKED_EXPR_INVALID_USAGE_ALL_ERROR_TYPES_IN_RHS("BCE3031", "checked.expr.invalid.usage.only.error.types.rhs"),
    CHECKED_EXPR_NO_MATCHING_ERROR_RETURN_IN_ENCL_INVOKABLE(
            "BCE3032", "checked.expr.no.matching.error.return.in.encl.invokable"),

    FAIL_EXPR_NO_MATCHING_ERROR_RETURN_IN_ENCL_INVOKABLE(
            "BCE3033", "fail.expr.no.matching.error.return.in.encl.invokable"),
    INCOMPATIBLE_ON_FAIL_ERROR_DEFINITION("BCE3034", "on.fail.no.matching.error"),

    START_REQUIRE_INVOCATION("BCE3035", "start.require.invocation"),
    INVALID_EXPR_STATEMENT("BCE3036", "invalid.expr.statement"),
    INVALID_ACTION_INVOCATION_AS_EXPR("BCE3037", "invalid.action.invocation.as.expr"),

    // Parser error diagnostic codes
    INVALID_TOKEN("BCE3100", "invalid.token"),
    MISSING_TOKEN("BCE3101", "missing.token"),
    EXTRANEOUS_INPUT("BCE3102", "extraneous.input"),
    MISMATCHED_INPUT("BCE3103", "mismatched.input"),
    FAILED_PREDICATE("BCE3104", "failed.predicate"),
    SYNTAX_ERROR("BCE3105", "syntax.error"),
    INVALID_SHIFT_OPERATOR("BCE3106", "invalid.shift.operator"),

    // Streaming related codes
    INVALID_STREAM_CONSTRUCTOR("BCE3200", "invalid.stream.constructor"),
    INVALID_STREAM_CONSTRUCTOR_ITERATOR("BCE3201", "invalid.stream.constructor.iterator"),
    INVALID_STREAM_CONSTRUCTOR_CLOSEABLE_ITERATOR("BCE3202", "invalid.stream.constructor.closeable.iterator"),
    INVALID_STREAM_CONSTRUCTOR_EXP_TYPE("BCE3203", "invalid.stream.constructor.expected.type"),
    NOT_ALLOWED_STREAM_USAGE_WITH_FROM("BCE3204", "invalid.stream.usage.with.from"),
    ERROR_TYPE_EXPECTED("BCE3205", "error.type.expected"),
    MISSING_REQUIRED_METHOD_NEXT("BCE3206", "missing.required.method.next"),
    ORDER_BY_NOT_SUPPORTED("BCE3207", "order.by.not.supported"),

    // Table related codes
    TABLE_CONSTRAINT_INVALID_SUBTYPE("BCE3300", "invalid.table.constraint.subtype"),
    TABLE_KEY_SPECIFIER_MISMATCH("BCE3301", "table.key.specifier.mismatch"),
    KEY_SPECIFIER_SIZE_MISMATCH_WITH_KEY_CONSTRAINT("BCE3302", "key.specifier.size.mismatch.with.key.constraint"),
    KEY_SPECIFIER_MISMATCH_WITH_KEY_CONSTRAINT("BCE3303", "key.specifier.mismatch.with.key.constraint"),
    INVALID_KEY_CONSTRAINT_PROVIDED_FOR_ACCESS("BCE3304", "invalid.key.constraint.provided.for.access"),
    MEMBER_ACCESS_NOT_SUPPORT_FOR_KEYLESS_TABLE("BCE3305", "member.access.not.supported.keyless.table"),
    INVALID_FIELD_NAMES_IN_KEY_SPECIFIER("BCE3306", "invalid.field.name.in.key.specifier"),
    MULTI_KEY_MEMBER_ACCESS_NOT_SUPPORTED("BCE3307", "multi.key.member.access.not.supported"),
    KEY_SPECIFIER_FIELD_MUST_BE_READONLY("BCE3308", "key.specifier.field.must.be.readonly"),
    KEY_SPECIFIER_FIELD_MUST_BE_REQUIRED("BCE3309", "key.specifier.field.must.be.required"),
    KEY_SPECIFIER_FIELD_MUST_BE_ANYDATA("BCE3310", "key.specifier.field.must.be.anydata"),
    KEY_SPECIFIER_FIELD_VALUE_MUST_BE_CONSTANT("BCE3311", "key.specifier.field.value.must.be.constant"),
    KEY_CONSTRAINT_NOT_SUPPORTED_FOR_TABLE_WITH_MAP_CONSTRAINT(
            "BCE3312", "key.constraint.not.supported.for.table.with.map.constraint"),
    CANNOT_INFER_MEMBER_TYPE_FOR_TABLE_DUE_AMBIGUITY("BCE3313", "cannot.infer.member.type.for.table.due.ambiguity"),
    CANNOT_INFER_MEMBER_TYPE_FOR_TABLE("BCE3314", "cannot.infer.member.type.for.table"),
    ON_CONFLICT_ONLY_WORKS_WITH_TABLES_WITH_KEY_SPECIFIER(
            "BCE3315", "on.conflict.only.works.with.tables.with.key.specifier"),
    CANNOT_UPDATE_TABLE_USING_MEMBER_ACCESS("BCE3316", "cannot.update.table.using.member.access.lvexpr"),


    // Taint checking related codes
    ENTRY_POINT_PARAMETERS_CANNOT_BE_UNTAINTED("BCE3400", "entry.point.parameters.cannot.be.untainted"),
    TAINTED_VALUE_PASSED_TO_UNTAINTED_PARAMETER("BCE3401", "tainted.value.passed.to.untainted.parameter"),
    TAINTED_VALUE_PASSED_TO_UNTAINTED_PARAMETER_ORIGINATING_AT(
            "BCE3402", "tainted.value.passed.to.untainted.param.in.obj.method"),
    TAINTED_VALUE_PASSED_TO_GLOBAL_VARIABLE("BCE3403", "tainted.value.passed.to.global.variable"),
    TAINTED_VALUE_PASSED_TO_MODULE_OBJECT("BCE3404", "tainted.value.passed.to.module.object"),
    INVOCATION_TAINT_GLOBAL_OBJECT("BCE3405", "method.invocation.taint.global.object"),
    TAINTED_VALUE_PASSED_TO_CLOSURE_VARIABLE("BCE3406", "tainted.value.passed.to.closure.variable"),
    UNABLE_TO_PERFORM_TAINT_CHECKING_WITH_RECURSION("BCE3407", "unable.to.perform.taint.checking.with.recursion"),
    UNABLE_TO_PERFORM_TAINT_CHECKING_FOR_BUILTIN_METHOD(
            "BCE3408", "unable.to.perform.taint.checking.for.builtin.method"),
    TAINTED_RETURN_NOT_ANNOTATED_TAINTED("BCE3409", "tainted.return.not.annotated.tainted"),
    TAINTED_PARAM_NOT_ANNOTATED_TAINTED("BCE3410", "tainted.param.not.annotated.tainted"),

    // Constants related codes.
    TYPE_REQUIRED_FOR_CONST_WITH_EXPRESSIONS("BCE3500", "type.required.for.const.with.expressions"),
    CANNOT_UPDATE_CONSTANT_VALUE("BCE3501", "cannot.update.constant.value"),
    CANNOT_ASSIGN_VALUE_TO_CONSTANT("BCE3502", "cannot.assign.value.to.constant"),
    CANNOT_DEFINE_CONSTANT_WITH_TYPE("BCE3503", "cannot.define.constant.with.type"),
    EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION("BCE3504", "expression.is.not.a.constant.expression"),
    INVALID_CONST_EXPRESSION("BCE3505", "invalid.const.expression"),
    CONSTANT_EXPRESSION_NOT_SUPPORTED("BCE3506", "const.expression.not.supported"),
    KEY_NOT_FOUND("BCE3507", "key.not.found"),

    // Anonymous functions related codes
    ARROW_EXPRESSION_MISMATCHED_PARAMETER_LENGTH("BCE3600", "arrow.expression.mismatched.parameter.length"),
    ARROW_EXPRESSION_CANNOT_INFER_TYPE_FROM_LHS("BCE3601", "arrow.expression.cannot.infer.type.from.lhs"),
    ARROW_EXPRESSION_NOT_SUPPORTED_ITERABLE_OPERATION("BCE3602", "arrow.expression.not.supported.iterable.operation"),

    INCOMPATIBLE_TYPE_CHECK("BCE3603", "incompatible.type.check"),
    UNNECESSARY_CONDITION("BCE3604", "unnecessary.condition"),

    INVALID_USAGE_OF_CLONE("BCE3605", "clone.invocation.invalid"),

    // Dataflow analysis related error codes
    PARTIALLY_INITIALIZED_VARIABLE("BCE3700", "partially.initialized.variable"),

    CANNOT_INFER_TYPE("BCE3701", "cannot.infer.type"),
    CANNOT_INFER_ERROR_TYPE("BCE3702", "cannot.infer.error.type"),
    INVALID_ERROR_CONSTRUCTOR_DETAIL("BCE3703", "invalid.error.detail.rec.does.not.match"),
    INDIRECT_ERROR_CTOR_REASON_NOT_ALLOWED("BCE3704", "invalid.error.reason.argument.to.indirect.error.constructor"),
    INDIRECT_ERROR_CTOR_NOT_ALLOWED_ON_NON_CONST_REASON("BCE3705", "invalid.indirect.error.constructor.invocation"),
    INVALID_FUNCTIONAL_CONSTRUCTOR_INVOCATION("BCE3706", "invalid.functional.constructor.invocation"),
    MISSING_ERROR_DETAIL_ARG("BCE3707", "missing.error.detail.arg"),
    INVALID_ERROR_DETAIL_ARG_TYPE("BCE3708", "invalid.error.detail.arg.type"),
    UNKNOWN_DETAIL_ARG_TO_SEALED_ERROR_DETAIL_REC("BCE3709", "unknown.error.detail.arg.to.sealed.detail"),
    INVALID_ERROR_DETAIL_REST_ARG_TYPE("BCE3710", "invalid.error.detail.rest.arg"),

    // Seal inbuilt function related codes
    INCOMPATIBLE_STAMP_TYPE("BCE3800", "incompatible.stamp.type"),
    NOT_SUPPORTED_SOURCE_TYPE_FOR_STAMP("BCE3801", "not.supported.source.for.stamp"),

    // Worker flush action related error codes
    INVALID_WORKER_FLUSH("BCE3820", "invalid.worker.flush.expression"),
    INVALID_WORKER_FLUSH_FOR_WORKER("BCE3821", "invalid.worker.flush.expression.for.worker"),

    // Worker receive and send related error codes
    INVALID_TYPE_FOR_RECEIVE("BCE3840", "invalid.type.for.receive"),
    INVALID_TYPE_FOR_SEND("BCE3841", "invalid.type.for.send"),

    INVALID_USAGE_OF_RECEIVE_EXPRESSION("BCE3842", "invalid.usage.of.receive.expression"),
    INVALID_USE_OF_EXPERIMENTAL_FEATURE("BCE3843", "invalid.use.of.experimental.feature"),

    INVALID_USE_OF_NULL_LITERAL("BCE3844", "invalid.use.of.null.literal"),

    // LangLib related error codes.
    TYPE_PARAM_OUTSIDE_LANG_MODULE("BCE3900", "type.param.outside.lang.module"),
    BUILTIN_SUBTYPE_OUTSIDE_LANG_MODULE("BCE3901", "builtin.subtype.outside.lang.module"),
    ISOLATED_PARAM_OUTSIDE_LANG_MODULE("BCE3902", "isolated.param.outside.lang.module"),
    ISOLATED_PARAM_USED_WITH_INVALID_TYPE("BCE3903", "isolated.param.used.with.invalid.type"),
    ISOLATED_PARAM_USED_IN_A_NON_ISOLATED_FUNCTION("BCE3904", "isolated.param.used.in.a.non.isolated.function"),

    INVALID_INVOCATION_LVALUE_ASSIGNMENT("BCE3905", "invalid.lvalue.lhs.of.assignment"),
    INVALID_INVOCATION_LVALUE_COMPOUND_ASSIGNMENT("BCE3906", "invalid.lvalue.lhs.of.compound.assignment"),

    IDENTIFIER_LITERAL_ONLY_SUPPORTS_ALPHANUMERICS("BCE3907", "identifier.literal.only.supports.alphanumerics"),
    INVALID_UNICODE("BCE3908", "invalid.unicode"),

    METHOD_TOO_LARGE("BCE3909", "method.too.large"),
    FILE_TOO_LARGE("BCE3910", "file.too.large"),
    CLASS_NOT_FOUND("BCE3911", "class.not.found"),
    METHOD_NOT_FOUND("BCE3912", "method.not.found"),
    CONSTRUCTOR_NOT_FOUND("BCE3913", "constructor.not.found"),
    FIELD_NOT_FOUND("BCE3914", "field.not.found"),
    OVERLOADED_METHODS("BCE3915", "overloaded.method"),
    UNSUPPORTED_PRIMITIVE_TYPE("BCE3916", "unsupported.primitive.type.reason"),
    METHOD_SIGNATURE_DOES_NOT_MATCH("BCE3917", "method.signature.not.match"),
    INVALID_DEPRECATION_DOCUMENTATION("BCE3918", "invalid.deprecation.documentation"),
    DEPRECATION_DOCUMENTATION_SHOULD_BE_AVAILABLE("BCE3919", "deprecation.documentation.should.available"),
    DEPRECATED_PARAMETERS_DOCUMENTATION_NOT_ALLOWED("BCE3920", "deprecated.parameters.documentation.not.allowed"),
    INVALID_ATTRIBUTE_REFERENCE("BCE3921", "invalid.attribute.reference"),

    ILLEGAL_FUNCTION_CHANGE_LIST_SIZE("BCE3922", "illegal.function.change.list.size"),
    ILLEGAL_FUNCTION_CHANGE_TUPLE_SHAPE("BCE3923", "illegal.function.change.tuple.shape"),

    INVALID_WAIT_MAPPING_CONSTRUCTORS("BCE3924", "invalid.wait.future.expr.mapping.constructors"),
    INVALID_WAIT_ACTIONS("BCE3925", "invalid.wait.future.expr.actions"),
    INVALID_SEND_EXPR("BCE3926", "invalid.send.expr"),

    DISTINCT_TYPING_ONLY_SUPPORT_OBJECTS_AND_ERRORS("BCE3927", "distinct.typing.only.support.objects.and.errors"),

    INVALID_NON_EXTERNAL_DEPENDENTLY_TYPED_FUNCTION("BCE3928", "invalid.non.external.dependently.typed.function"),
    INVALID_PARAM_TYPE_FOR_RETURN_TYPE("BCE3929", "invalid.param.type.for.return.type"),
    INVALID_TYPEDESC_PARAM("BCE3930", "invalid.typedesc.param"),

    INVALID_RAW_TEMPLATE_TYPE("BCE3931", "invalid.raw.template.type"),
    MULTIPLE_COMPATIBLE_RAW_TEMPLATE_TYPES("BCE3932", "multiple.compatible.raw.template.types"),
    INVALID_NUM_STRINGS("BCE3933", "invalid.num.of.strings"),
    INVALID_NUM_INSERTIONS("BCE3934", "invalid.num.of.insertions"),
    INVALID_RAW_TEMPLATE_ASSIGNMENT("BCE3935", "invalid.raw.template.assignment"),
    INVALID_NUM_FIELDS("BCE3936", "invalid.number.of.fields"),
    METHODS_NOT_ALLOWED("BCE3937", "methods.not.allowed"),

    INVALID_MUTABLE_ACCESS_IN_ISOLATED_FUNCTION("BCE3938", "invalid.mutable.access.in.isolated.function"),
    INVALID_MUTABLE_ACCESS_AS_RECORD_DEFAULT("BCE3939", "invalid.mutable.access.as.record.default"),
    INVALID_MUTABLE_ACCESS_AS_OBJECT_DEFAULT("BCE3940", "invalid.mutable.access.as.object.default"),

    INVALID_NON_ISOLATED_FUNCTION_AS_ARGUMENT("BCE3941", "invalid.non.isolated.function.as.argument"),

    INVALID_NON_ISOLATED_INVOCATION_IN_ISOLATED_FUNCTION(
            "BCE3942", "invalid.non.isolated.invocation.in.isolated.function"),
    INVALID_NON_ISOLATED_INVOCATION_AS_RECORD_DEFAULT("BCE3943", "invalid.non.isolated.invocation.as.record.default"),
    INVALID_NON_ISOLATED_INVOCATION_AS_OBJECT_DEFAULT("BCE3944", "invalid.non.isolated.invocation.as.object.default"),

    INVALID_NON_ISOLATED_INIT_EXPRESSION_IN_ISOLATED_FUNCTION(
            "BCE3945", "invalid.non.isolated.init.expression.in.isolated.function"),
    INVALID_NON_ISOLATED_INIT_EXPRESSION_AS_RECORD_DEFAULT(
            "BCE3946", "invalid.non.isolated.init.expression.as.record.default"),
    INVALID_NON_ISOLATED_INIT_EXPRESSION_AS_OBJECT_DEFAULT(
            "BCE3947", "invalid.non.isolated.init.expression.as.object.default"),

    INVALID_ASYNC_INVOCATION_IN_ISOLATED_FUNCTION("BCE3948", "invalid.async.invocation.in.isolated.function"),
    INVALID_WORKER_DECLARATION_IN_ISOLATED_FUNCTION("BCE3949", "invalid.worker.declaration.in.isolated.function"),
    INVALID_FORK_STATEMENT_IN_ISOLATED_FUNCTION("BCE3950", "invalid.fork.statement.in.isolated.function"),

    INVALID_NON_PRIVATE_MUTABLE_FIELD_IN_ISOLATED_OBJECT(
            "BCE3951", "invalid.non.private.mutable.field.in.isolated.object"),
    INVALID_MUTABLE_FIELD_ACCESS_IN_ISOLATED_OBJECT_OUTSIDE_LOCK(
            "BCE3952", "invalid.mutable.field.access.in.isolated.object.outside.lock"),
    INVALID_REFERENCE_TO_SELF_IN_ISOLATED_OBJECT_OUTSIDE_LOCK(
            "BCE3953", "invalid.reference.to.self.in.isolated.object.outside.lock"),
    INVALID_NON_ISOLATED_EXPRESSION_AS_INITIAL_VALUE("BCE3954", "invalid.non.isolated.expression.as.initial.value"),
    INVALID_TRANSFER_OUT_OF_LOCK_WITH_RESTRICTED_VAR_USAGE(
            "BCE3955", "invalid.transfer.out.of.lock.with.restricted.var.usage"),
    INVALID_TRANSFER_INTO_LOCK_WITH_RESTRICTED_VAR_USAGE(
            "BCE3956", "invalid.transfer.into.lock.with.restricted.var.usage"),
    INVALID_NON_ISOLATED_INVOCATION_IN_LOCK_WITH_RESTRICTED_VAR_USAGE(
            "BCE3957", "invalid.non.isolated.invocation.in.lock.with.restricted.var.usage"),
    INVALID_ISOLATED_VARIABLE_ACCESS_OUTSIDE_LOCK("BCE3958", "invalid.isolated.variable.access.outside.lock"),
    INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE(
            "BCE3959", "invalid.assignment.in.lock.with.restricted.var.usage"),
    INVALID_USAGE_OF_MULTIPLE_RESTRICTED_VARS_IN_LOCK("BCE3960", "invalid.usage.of.multiple.restricted.vars.in.lock"),

    INVALID_ISOLATED_QUALIFIER_ON_MODULE_NO_INIT_VAR_DECL(
            "BCE3961", "invalid.isolated.qualifier.on.module.no.init.var.decl"),

    BINDING_PATTERN_NOT_YET_SUPPORTED_IN_MODULE_VAR_DECL(
            "BCE3962", "binding.pattern.not.yet.supported.in.module.var.decl"),

    // Configurable var related error codes
    CONFIGURABLE_VARIABLE_CANNOT_BE_DECLARED_WITH_VAR("BCE3963",
            "configurable.variable.cannot.be.declared.with.var"),
    CONFIGURABLE_VARIABLE_MUST_BE_ANYDATA_AND_READONLY("BCE3964",
            "configurable.variable.must.be.anydata.and.readonly"),
    ONLY_SIMPLE_VARIABLES_ARE_ALLOWED_TO_BE_CONFIGURABLE("BCE3965",
            "only.simple.variables.are.allowed.to.be.configurable"),
    CONFIGURABLE_VARIABLE_CURRENTLY_NOT_SUPPORTED("BCE3966",
            "configurable.variable.currently.not.supported"),

    REMOTE_FUNCTION_IN_NON_NETWORK_OBJECT("BCE3967", "remote.function.in.non.network.object"),
    UNSUPPORTED_PATH_PARAM_TYPE("BCE3968", "unsupported.path.param.type"),
    UNSUPPORTED_REST_PATH_PARAM_TYPE("BCE3969", "unsupported.rest.path.param.type"),
    SERVICE_ABSOLUTE_PATH_OR_LITERAL_IS_REQUIRED_BY_LISTENER("BCE3970",
            "error.service.absolute.path.or.literal.required.by.listener"),
    SERVICE_PATH_LITERAL_IS_NOT_SUPPORTED_BY_LISTENER("BCE3971", "service.path.literal.is.not.supported.by.listener"),
    SERVICE_ABSOLUTE_PATH_IS_NOT_SUPPORTED_BY_LISTENER("BCE3972", "service.absolute.path.is.not.supported.by.listener"),
    SERVICE_LITERAL_REQUIRED_BY_LISTENER("BCE3973", "service.path.literal.required.by.listener"),
    SERVICE_ABSOLUTE_PATH_REQUIRED_BY_LISTENER("BCE3974", "service.absolute.path.required.by.listener"),
    ;

    private String diagnosticId;
    private String messageKey;

    DiagnosticErrorCode(String diagnosticId, String messageKey) {
        this.diagnosticId = diagnosticId;
        this.messageKey = messageKey;
    }

    @Override
    public DiagnosticSeverity severity() {
        return DiagnosticSeverity.ERROR;
    }

    @Override
    public String diagnosticId() {
        return diagnosticId;
    }

    @Override
    public String messageKey() {
        return messageKey;
    }

    public boolean equals(DiagnosticCode code) {
        return this.messageKey.equals(code.messageKey());
    }
}
