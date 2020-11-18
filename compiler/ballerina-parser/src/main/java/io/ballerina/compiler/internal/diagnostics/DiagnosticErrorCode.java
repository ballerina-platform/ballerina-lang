/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.internal.diagnostics;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * Represents a diagnostic error code.
 *
 * @since 2.0.0
 */
public enum DiagnosticErrorCode implements DiagnosticCode {
    // TODO figure out an order of these error codes

    // The member represents a generic syntax error
    // We should use this only when we can't figure out the exact error
    ERROR_SYNTAX_ERROR("BCE0000", "error.syntax.error"),

    // Tokens
    ERROR_MISSING_TOKEN("BCE0001", "error.missing.token"),
    ERROR_MISSING_SEMICOLON_TOKEN("BCE0002", "error.missing.semicolon.token"),
    ERROR_MISSING_COLON_TOKEN("BCE0003", "error.missing.colon.token"),
    ERROR_MISSING_OPEN_PAREN_TOKEN("BCE0004", "error.missing.open.paren.token"),
    ERROR_MISSING_CLOSE_PAREN_TOKEN("BCE0005", "error.missing.close.paren.token"),
    ERROR_MISSING_OPEN_BRACE_TOKEN("BCE0006", "error.missing.open.brace.token"),
    ERROR_MISSING_CLOSE_BRACE_TOKEN("BCE0007", "error.missing.close.brace.token"),
    ERROR_MISSING_OPEN_BRACKET_TOKEN("BCE0008", "error.missing.open.bracket.token"),
    ERROR_MISSING_CLOSE_BRACKET_TOKEN("BCE0009", "error.missing.close.bracket.token"),
    ERROR_MISSING_EQUAL_TOKEN("BCE0010", "error.missing.equal.token"),
    ERROR_MISSING_COMMA_TOKEN("BCE00011", "error.missing.comma.token"),
    ERROR_MISSING_PLUS_TOKEN("BCE00012", "error.missing.plus.token"),
    ERROR_MISSING_SLASH_TOKEN("BCE00013", "error.missing.slash.token"),
    ERROR_MISSING_AT_TOKEN("BCE00014", "error.missing.at.token"),
    ERROR_MISSING_QUESTION_MARK_TOKEN("BCE00015", "error.missing.question.mark.token"),
    ERROR_MISSING_GT_TOKEN("BCE00016", "error.missing.gt.token"),
    ERROR_MISSING_GT_EQUAL_TOKEN("BCE00017", "error.missing.gt.equal.token"),
    ERROR_MISSING_LT_TOKEN("BCE00018", "error.missing.lt.token"),
    ERROR_MISSING_LT_EQUAL_TOKEN("BCE00019", "error.missing.lt.equal.token"),
    ERROR_MISSING_RIGHT_DOUBLE_ARROW_TOKEN("BCE00020", "error.missing.right.double.arrow.token"),
    ERROR_MISSING_XML_COMMENT_END_TOKEN("BCE00021", "error.missing.xml.comment.end.token"),
    ERROR_MISSING_XML_PI_END_TOKEN("BCE00022", "error.missing.xml.pi.end.token"),
    ERROR_MISSING_DOUBLE_QUOTE_TOKEN("BCE00023", "error.missing.double.quote.token"),
    ERROR_MISSING_BACKTICK_TOKEN("BCE00024", "error.missing.backtick.token"),
    ERROR_MISSING_OPEN_BRACE_PIPE_TOKEN("BCE00025", "error.missing.open.brace.pipe.token"),
    ERROR_MISSING_CLOSE_BRACE_PIPE_TOKEN("BCE00026", "error.missing.close.brace.pipe.token"),
    ERROR_MISSING_ASTERISK_TOKEN("BCE00027", "error.missing.asterisk.token"),
    ERROR_MISSING_PIPE_TOKEN("BCE00028", "error.missing.pipe.token"),
    ERROR_MISSING_DOT_TOKEN("BCE00029", "error.missing.dot.token"),

    // Keywords
    ERROR_MISSING_PUBLIC_KEYWORD("BCE02001", "error.missing.public.keyword"),
    ERROR_MISSING_PRIVATE_KEYWORD("BCE02002", "error.missing.private.keyword"),
    ERROR_MISSING_REMOTE_KEYWORD("BCE02003", "error.missing.remote.keyword"),
    ERROR_MISSING_ABSTRACT_KEYWORD("BCE02004", "error.missing.abstract.keyword"),
    ERROR_MISSING_CLIENT_KEYWORD("BCE02005", "error.missing.client.keyword"),
    ERROR_MISSING_LISTENER_KEYWORD("BCE02006", "error.missing.listener.keyword"),
    ERROR_MISSING_XMLNS_KEYWORD("BCE02007", "error.missing.xmlns.keyword"),
    ERROR_MISSING_RESOURCE_KEYWORD("BCE02008", "error.missing.resource.keyword"),
    ERROR_MISSING_FINAL_KEYWORD("BCE02009", "error.missing.final.keyword"),
    ERROR_MISSING_WORKER_KEYWORD("BCE02010", "error.missing.worker.keyword"),
    ERROR_MISSING_PARAMETER_KEYWORD("BCE02011", "error.missing.parameter.keyword"),
    ERROR_MISSING_RETURNS_KEYWORD("BCE02012", "error.missing.returns.keyword"),
    ERROR_MISSING_RETURN_KEYWORD("BCE02013", "error.missing.return.keyword"),
    ERROR_MISSING_TRUE_KEYWORD("BCE02014", "error.missing.true.keyword"),
    ERROR_MISSING_FALSE_KEYWORD("BCE02015", "error.missing.false.keyword"),
    ERROR_MISSING_ELSE_KEYWORD("BCE02016", "error.missing.else.keyword"),
    ERROR_MISSING_WHILE_KEYWORD("BCE02017", "error.missing.while.keyword"),
    ERROR_MISSING_CHECK_KEYWORD("BCE02018", "error.missing.check.keyword"),
    ERROR_MISSING_CHECKPANIC_KEYWORD("BCE02019", "error.missing.checkpanic.keyword"),
    ERROR_MISSING_PANIC_KEYWORD("BCE02020", "error.missing.panic.keyword"),
    ERROR_MISSING_CONTINUE_KEYWORD("BCE02021", "error.missing.continue.keyword"),
    ERROR_MISSING_BREAK_KEYWORD("BCE02022", "error.missing.break.keyword"),
    ERROR_MISSING_TYPEOF_KEYWORD("BCE020023", "error.missing.typeof.keyword"),
    ERROR_MISSING_IS_KEYWORD("BCE02024", "error.missing.is.keyword"),
    ERROR_MISSING_NULL_KEYWORD("BCE02025", "error.missing.null.keyword"),
    ERROR_MISSING_LOCK_KEYWORD("BCE02026", "error.missing.lock.keyword"),
    ERROR_MISSING_FORK_KEYWORD("BCE02027", "error.missing.fork.keyword"),
    ERROR_MISSING_TRAP_KEYWORD("BCE02028", "error.missing.trap.keyword"),
    ERROR_MISSING_FOREACH_KEYWORD("BCE02029", "error.missing.foreach.keyword"),
    ERROR_MISSING_NEW_KEYWORD("BCE02030", "error.missing.new.keyword"),
    ERROR_MISSING_WHERE_KEYWORD("BCE02031", "error.missing.where.keyword"),
    ERROR_MISSING_SELECT_KEYWORD("BCE02032", "error.missing.select.keyword"),
    ERROR_MISSING_START_KEYWORD("BCE02033", "error.missing.start.keyword"),
    ERROR_MISSING_FLUSH_KEYWORD("BCE02034", "error.missing.flush.keyword"),
    ERROR_MISSING_WAIT_KEYWORD("BCE02035", "error.missing.wait.keyword"),
    ERROR_MISSING_DO_KEYWORD("BCE02036", "error.missing.do.keyword"),
    ERROR_MISSING_TRANSACTION_KEYWORD("BCE02037", "error.missing.transaction.keyword"),
    ERROR_MISSING_TRANSACTIONAL_KEYWORD("BCE02038", "error.missing.transactional.keyword"),
    ERROR_MISSING_COMMIT_KEYWORD("BCE02039", "error.missing.commit.keyword"),
    ERROR_MISSING_ROLLBACK_KEYWORD("BCE02040", "error.missing.rollback.keyword"),
    ERROR_MISSING_RETRY_KEYWORD("BCE02041", "error.missing.retry.keyword"),
    ERROR_MISSING_BASE16_KEYWORD("BCE02042", "error.missing.base16.keyword"),
    ERROR_MISSING_BASE64_KEYWORD("BCE02043", "error.missing.base64.keyword"),
    ERROR_MISSING_MATCH_KEYWORD("BCE02044", "error.missing.match.keyword"),
    ERROR_MISSING_DEFAULT_KEYWORD("BCE02045", "error.missing.default.keyword"),
    ERROR_MISSING_TYPE_KEYWORD("BCE02046", "error.missing.type.keyword"),
    ERROR_MISSING_ON_KEYWORD("BCE02047", "error.missing.on.keyword"),
    ERROR_MISSING_ANNOTATION_KEYWORD("BCE02048", "error.missing.annotation.keyword"),
    ERROR_MISSING_FUNCTION_KEYWORD("BCE02049", "error.missing.function.keyword"),
    ERROR_MISSING_SOURCE_KEYWORD("BCE02050", "error.missing.source.keyword"),
    ERROR_MISSING_ENUM_KEYWORD("BCE02051", "error.missing.enum.keyword"),
    ERROR_MISSING_FIELD_KEYWORD("BCE02052", "error.missing.field.keyword"),
    ERROR_MISSING_VERSION_KEYWORD("BCE02053", "error.missing.version.keyword"),
    ERROR_MISSING_OBJECT_KEYWORD("BCE02054", "error.missing.object.keyword"),
    ERROR_MISSING_RECORD_KEYWORD("BCE02055", "error.missing.record.keyword"),
    ERROR_MISSING_SERVICE_KEYWORD("BCE02056", "error.missing.service.keyword"),
    ERROR_MISSING_AS_KEYWORD("BCE02057", "error.missing.as.keyword"),
    ERROR_MISSING_LET_KEYWORD("BCE02058", "error.missing.let.keyword"),
    ERROR_MISSING_TABLE_KEYWORD("BCE02059", "error.missing.table.keyword"),
    ERROR_MISSING_KEY_KEYWORD("BCE02060", "error.missing.key.keyword"),
    ERROR_MISSING_FROM_KEYWORD("BCE02061", "error.missing.from.keyword"),
    ERROR_MISSING_IN_KEYWORD("BCE02062", "error.missing.in.keyword"),
    ERROR_MISSING_IF_KEYWORD("BCE02063", "error.missing.if.keyword"),
    ERROR_MISSING_IMPORT_KEYWORD("BCE02064", "error.missing.import.keyword"),
    ERROR_MISSING_CONST_KEYWORD("BCE02065", "error.missing.const.keyword"),
    ERROR_MISSING_EXTERNAL_KEYWORD("BCE02066", "error.missing.external.keyword"),
    ERROR_MISSING_ORDER_KEYWORD("BCE02067", "error.missing.order.keyword"),
    ERROR_MISSING_BY_KEYWORD("BCE02068", "error.missing.by.keyword"),
    ERROR_MISSING_CONFLICT_KEYWORD("BCE02069", "error.missing.conflict.keyword"),
    ERROR_MISSING_LIMIT_KEYWORD("BCE02070", "error.missing.limit.keyword"),
    ERROR_MISSING_ASCENDING_KEYWORD("BCE02071", "error.missing.ascending.keyword"),
    ERROR_MISSING_DESCENDING_KEYWORD("BCE02072", "error.missing.descending.keyword"),
    ERROR_MISSING_JOIN_KEYWORD("BCE02073", "error.missing.join.keyword"),
    ERROR_MISSING_OUTER_KEYWORD("BCE02074", "error.missing.outer.keyword"),
    ERROR_MISSING_CLASS_KEYWORD("BCE02075", "error.missing.class.keyword"),
    ERROR_MISSING_FAIL_KEYWORD("BCE02075", "error.missing.fail.keyword"),
    ERROR_MISSING_EQUALS_KEYWORD("BCE02076", "error.missing.equals.keyword"),

    // Type keywords
    ERROR_MISSING_INT_KEYWORD("BCE02101", "error.missing.int.keyword"),
    ERROR_MISSING_BYTE_KEYWORD("BCE02102", "error.missing.byte.keyword"),
    ERROR_MISSING_FLOAT_KEYWORD("BCE02103", "error.missing.float.keyword"),
    ERROR_MISSING_DECIMAL_KEYWORD("BCE02104", "error.missing.decimal.keyword"),
    ERROR_MISSING_STRING_KEYWORD("BCE02105", "error.missing.string.keyword"),
    ERROR_MISSING_BOOLEAN_KEYWORD("BCE02106", "error.missing.boolean.keyword"),
    ERROR_MISSING_XML_KEYWORD("BCE02107", "error.missing.xml.keyword"),
    ERROR_MISSING_JSON_KEYWORD("BCE02108", "error.missing.json.keyword"),
    ERROR_MISSING_HANDLE_KEYWORD("BCE02109", "error.missing.handle.keyword"),
    ERROR_MISSING_ANY_KEYWORD("BCE02110", "error.missing.any.keyword"),
    ERROR_MISSING_ANYDATA_KEYWORD("BCE02111", "error.missing.anydata.keyword"),
    ERROR_MISSING_NEVER_KEYWORD("BCE02112", "error.missing.never.keyword"),
    ERROR_MISSING_VAR_KEYWORD("BCE02113", "error.missing.var.keyword"),
    ERROR_MISSING_MAP_KEYWORD("BCE02114", "error.missing.map.keyword"),
    ERROR_MISSING_FUTURE_KEYWORD("BCE02115", "error.missing.future.keyword"),
    ERROR_MISSING_TYPEDESC_KEYWORD("BCE02116", "error.missing.typedesc.keyword"),
    ERROR_MISSING_ERROR_KEYWORD("BCE02117", "error.missing.error.keyword"),
    ERROR_MISSING_STREAM_KEYWORD("BCE02118", "error.missing.stream.keyword"),
    ERROR_MISSING_READONLY_KEYWORD("BCE02119", "error.missing.readonly.keyword"),
    ERROR_MISSING_DISTINCT_KEYWORD("BCE02120", "error.missing.distinct.keyword"),

    //Separators
    ERROR_MISSING_ELLIPSIS_TOKEN("BCE02201", "error.missing.ellipsis.token"),
    ERROR_MISSING_HASH_TOKEN("BCE02202", "error.missing.hash.token"),
    ERROR_MISSING_SINGLE_QUOTE_TOKEN("BCE02203", "error.missing.single.quote.token"),

    // Operators
    ERROR_MISSING_DOUBLE_EQUAL_TOKEN("BCE02301", "error.missing.double.equal.token"),
    ERROR_MISSING_TRIPPLE_EQUAL_TOKEN("BCE02302", "error.missing.tripple.equal.token"),
    ERROR_MISSING_MINUS_TOKEN("BCE02303", "error.missing.minus.token"),
    ERROR_MISSING_PERCENT_TOKEN("BCE02304", "error.missing.percent.token"),
    ERROR_MISSING_EXCLAMATION_MARK_TOKEN("BCE02305", "error.missing.exclamation.mark.token"),
    ERROR_MISSING_NOT_EQUAL_TOKEN("BCE02306", "error.missing.not.equal.token"),
    ERROR_MISSING_NOT_DOUBLE_EQUAL_TOKEN("BCE02307", "error.missing.not.double.equal.token"),
    ERROR_MISSING_BITWISE_AND_TOKEN("BCE02308", "error.missing.bitwise.and.token"),
    ERROR_MISSING_BITWISE_XOR_TOKEN("BCE02309", "error.missing.bitwise.xor.token"),
    ERROR_MISSING_LOGICAL_AND_TOKEN("BCE02310", "error.missing.logical.and.token"),
    ERROR_MISSING_LOGICAL_OR_TOKEN("BCE02311", "error.missing.logical.or.token"),
    ERROR_MISSING_NEGATION_TOKEN("BCE02312", "error.missing.negation.token"),
    ERROR_MISSING_RIGHT_ARROW_TOKEN("BCE02313", "error.missing.right.arrow.token"),
    ERROR_MISSING_INTERPOLATION_START_TOKEN("BCE02314", "error.missing.interpolation.start.token"),
    ERROR_MISSING_XML_PI_START_TOKEN("BCE02315", "error.missing.xml.pi.start.token"),
    ERROR_MISSING_XML_COMMENT_START_TOKEN("BCE02316", "error.missing.xml.comment.start.token"),
    ERROR_MISSING_SYNC_SEND_TOKEN("BCE02317", "error.missing.sync.send.token"),
    ERROR_MISSING_LEFT_ARROW_TOKEN("BCE02318", "error.missing.left.arrow.token"),
    ERROR_MISSING_DOUBLE_DOT_LT_TOKEN("BCE02319", "error.missing.double.dot.lt.token"),
    ERROR_MISSING_DOUBLE_LT_TOKEN("BCE02320", "error.missing.double.lt.token"),
    ERROR_MISSING_ANNOT_CHAINING_TOKEN("BCE02321", "error.missing.annot.chaining.token"),
    ERROR_MISSING_OPTIONAL_CHAINING_TOKEN("BCE02322", "error.missing.optional.chaining.token"),
    ERROR_MISSING_ELVIS_TOKEN("BCE02323", "error.missing.elvis.token"),
    ERROR_MISSING_DOT_LT_TOKEN("BCE02324", "error.missing.dot.lt.token"),
    ERROR_MISSING_SLASH_LT_TOKEN("BCE02325", "error.missing.slash.lt.token"),
    ERROR_MISSING_DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN("BCE02326",
            "error.missing.double.slash.double.asterisk.lt.token"),
    ERROR_MISSING_SLASH_ASTERISK_TOKEN("BCE02327", "error.missing.slash.asterisk.token"),
    ERROR_MISSING_DOUBLE_GT_TOKEN("BCE02328", "error.missing.double.gt.token"),
    ERROR_MISSING_TRIPPLE_GT_TOKEN("BCE02329", "error.missing.tripple.gt.token"),

    // literals
    ERROR_MISSING_IDENTIFIER("BCE02500", "error.missing.identifier"),
    ERROR_MISSING_STRING_LITERAL("BCE02501", "error.missing.string.literal"),
    ERROR_MISSING_DECIMAL_INTEGER_LITERAL("BCE02502", "error.missing.decimal.integer.literal"),
    ERROR_MISSING_HEX_INTEGER_LITERAL("BCE02503", "error.missing.hex.integer.literal"),
    ERROR_MISSING_DECIMAL_FLOATING_POINT_LITERAL("BCE02504", "error.missing.decimal.floating.point.literal"),
    ERROR_MISSING_HEX_FLOATING_POINT_LITERAL("BCE02505", "error.missing.hex.floating.point.literal"),
    ERROR_MISSING_XML_TEXT_CONTENT("BCE02506", "error.missing.xml.text.content"),
    ERROR_MISSING_TEMPLATE_STRING("BCE02507", "error.missing.template.string"),
    ERROR_MISSING_BYTE_ARRAY_CONTENT("BCE02508", "error.missing.byte.array.content"),

    //miscellaneous
    ERROR_MISSING_FUNCTION_NAME("BCE0060", "error.missing.function.name"),

    ERROR_MISSING_TYPE_DESC("BCE0100", "error.missing.type.desc"),
    ERROR_MISSING_EXPRESSION("BCE0101", "error.missing.expression"),
    ERROR_MISSING_SELECT_CLAUSE("BCE0102", "error.missing.select.clause"),
    ERROR_MISSING_RECEIVE_FIELD_IN_RECEIVE_ACTION("BCE103", "error.missing.receive.field.in.receive.action"),
    ERROR_MISSING_WAIT_FIELD_IN_WAIT_ACTION("BCE104", "error.missing.wait.field.in.wait.action"),
    ERROR_MISSING_WAIT_FUTURE_EXPRESSION("BCE105", "error.missing.wait.future.expression"),
    ERROR_MISSING_ENUM_MEMBER("BCE106", "error.missing.enum.member"),
    ERROR_MISSING_XML_ATOMIC_NAME_PATTERN("BCE107", "error.missing.xml.atomic.name.pattern"),
    ERROR_MISSING_TUPLE_MEMBER("BCE108", "error.missing.tuple.member"),
    ERROR_EXPRESSION_EXPECTED_ACTION_FOUND("BCE109", "error.expression.expected.action.found"),
    ERROR_MISSING_KEY_EXPR_IN_MEMBER_ACCESS_EXPR("BCE109", "error.missing.key.expr.in.member.access.expr"),
    ONLY_TYPE_REFERENCE_ALLOWED_HERE_AS_TYPE_INCLUSIONS("BCE110",
            "error.only.type.reference.allowed.here.as.type.inclusions"),
    ERROR_MISSING_ORDER_KEY("BCE110", "error.missing.order.key"),

    ERROR_MISSING_ANNOTATION_ATTACH_POINT("BCE200", "error.missing.annotation.attach.point"),
    ERROR_MISSING_LET_VARIABLE_DECLARATION("BCE201", "error.missing.let.variable.declaration"),
    ERROR_MISSING_NAMED_WORKER_DECLARATION_IN_FORK_STMT("BCE202",
            "error.missing.named.worker.declaration.in.fork.stmt"),
    ERROR_NAMED_WORKER_NOT_ALLOWED_HERE("BCE203", "error.named.worker.not.allowed.here"),
    ERROR_ONLY_NAMED_WORKERS_ALLOWED_HERE("BCE204", "error.only.named.workers.allowed.here"),
    ERROR_IMPORT_DECLARATION_AFTER_OTHER_DECLARATIONS("BCE205",
            "error.import.declaration.after.other.declarations"),
    // Annotations are not supported for expressions
    ERROR_ANNOTATIONS_ATTACHED_TO_EXPRESSION("BCE206", "error.annotations.attached.to.expression"),
    // Expression followed by the start keyword must be a func-call, a method-call or a remote-method-call
    ERROR_INVALID_EXPRESSION_IN_START_ACTION("BCE207", "error.invalid.expression.in.start.action"),
    // Cannot have the same qualifier twice
    ERROR_DUPLICATE_QUALIFIER("BCE208", "error.duplicate.qualifier"),
    // Cannot apply a particular qualifier in a certain context
    ERROR_QUALIFIER_NOT_ALLOWED("BCE209", "error.qualifier.not.allowed"),
    // Cannot have type inclusions in object constructor
    ERROR_TYPE_INCLUSION_IN_OBJECT_CONSTRUCTOR("BCE214", "error.type.inclusion.in.object.constructor"),
    // Mapping constructor expression cannot be used as a wait expression
    ERROR_MAPPING_CONSTRUCTOR_EXPR_AS_A_WAIT_EXPR("BCE217", "error.mapping.constructor.expr.as.a.wait.expr"),
    // lhs must be an identifier or a param list
    ERROR_INVALID_PARAM_LIST_IN_INFER_ANONYMOUS_FUNCTION_EXPR("BCE218",
            "error.invalid.param.list.in.infer.anonymous.function.expr"),
    // Cannot have more fields after the rest type descriptor
    ERROR_MORE_RECORD_FIELDS_AFTER_REST_FIELD("BCE219", "error.more.record.fields.after.rest.field"),
    ERROR_INVALID_XML_NAMESPACE_URI("BCE220", "error.invalid.xml.namespace.uri"),
    ERROR_INTERPOLATION_IS_NOT_ALLOWED_FOR_XML_TAG_NAMES("BCE221",
            "error.interpolation.is.not.allowed.for.xml.tag.names"),
    ERROR_INTERPOLATION_IS_NOT_ALLOWED_WITHIN_ELEMENT_TAGS("BCE222",
            "error.interpolation.is.not.allowed.within.element.tags"),
    ERROR_INTERPOLATION_IS_NOT_ALLOWED_WITHIN_XML_COMMENTS("BCE223",
            "error.interpolation.is.not.allowed.within.xml.comments"),
    ERROR_INTERPOLATION_IS_NOT_ALLOWED_WITHIN_XML_PI("BCE224",
            "error.interpolation.is.not.allowed.within.xml.pi"),
    ERROR_INVALID_EXPR_IN_ASSIGNMENT_LHS("BCE225", "error.invalid.expr.in.assignment.lhs"),
    ERROR_INVALID_EXPR_IN_COMPOUND_ASSIGNMENT_LHS("BCE226",
            "error.invalid.expr.in.compound.assignment.lhs"),
    ERROR_INVALID_METADATA("BCE227", "error.invalid.metadata"),
    ERROR_INVALID_QUALIFIER("BCE228", "error.invalid.qualifier"),
    ERROR_INVALID_ANNOTATIONS("BCE229", "error.invalid.annotations"),
    ERROR_MORE_FIELD_MATCH_PATTERNS_AFTER_REST_FIELD("BCE230",
            "error.more.field.match.patterns.after.rest.field"),
    ERROR_ACTION_AS_A_WAIT_EXPR("BCE231", "error.action.as.a.wait.expr"),
    ERROR_INVALID_USAGE_OF_VAR("BCE232", "error.invalid.usage.of.var"),
    ERROR_MORE_MATCH_PATTERNS_AFTER_REST_MATCH_PATTERN("BCE233",
            "error.more.match.patterns.after.rest.match.pattern"),
    ERROR_MATCH_PATTERN_NOT_ALLOWED("BCE234", "error.match.pattern.not.allowed"),
    ERROR_MATCH_STATEMENT_SHOULD_HAVE_ONE_OR_MORE_MATCH_CLAUSES("BCE235",
            "error.match.statement.should.have.one.or.more.match.clauses"),

    ERROR_PARAMETER_AFTER_THE_REST_PARAMETER("BCE300", "error.parameter.after.the.rest.parameter"),
    ERROR_REQUIRED_PARAMETER_AFTER_THE_DEFAULTABLE_PARAMETER("BCE301",
            "error.required.parameter.after.the.defaultable.parameter"),
    ERROR_NAMED_ARG_FOLLOWED_BY_POSITIONAL_ARG("BCE302", "error.named.arg.followed.by.positional.arg"),
    ERROR_ARG_FOLLOWED_BY_REST_ARG("BCE303", "error.arg.followed.by.rest.arg"),
    ERROR_BINDING_PATTERN_NOT_ALLOWED("BCE304", "error.binding.pattern.not.allowed"),

    ERROR_INVALID_BASE16_CONTENT_IN_BYTE_ARRAY_LITERAL("BCE401",
            "error.invalid.base16.content.in.byte.array.literal"),
    ERROR_INVALID_BASE64_CONTENT_IN_BYTE_ARRAY_LITERAL("BCE402",
            "error.invalid.base64.content.in.byte.array.literal"),
    ERROR_INVALID_CONTENT_IN_BYTE_ARRAY_LITERAL("BCE403", "error.invalid.content.in.byte.array.literal"),
    ERROR_INVALID_TOKEN("BCE404", "error.invalid.token"),

    ERROR_INVALID_EXPRESSION_STATEMENT("BCE500", "error.invalid.expression.statement"),
    ERROR_INVALID_ARRAY_LENGTH("BCE501", "error.invalid.array.length"),
    ERROR_SELECT_CLAUSE_IN_QUERY_ACTION("BCE502", "error.select.clause.in.query.action"),
    ERROR_MORE_CLAUSES_AFTER_SELECT_CLAUSE("BCE503", "error.more.clauses.after.select.clause"),
    ERROR_QUERY_CONSTRUCT_TYPE_IN_QUERY_ACTION("BCE504", "error.query.construct.type.in.query.action"),

    ERROR_NO_WHITESPACES_ALLOWED_IN_RIGHT_SHIFT_OP("BCE601", "error.no.whitespaces.allowed.in.right.shift.op"),
    ERROR_NO_WHITESPACES_ALLOWED_IN_UNSIGNED_RIGHT_SHIFT_OP("BCE602",
            "error.no.whitespaces.allowed.in.unsigned.right.shift.op"),
    ERROR_INVALID_WHITESPACE_IN_SLASH_LT_TOKEN("BCE603", "error.invalid.whitespace.in.slash.lt.token"),
    ERROR_LOCAL_TYPE_DEFINITION_NOT_ALLOWED("BCE604", "error.local.type.definition.not.allowed"),

    // Lexer errors
    ERROR_LEADING_ZEROS_IN_NUMERIC_LITERALS("BCE1000", "error.leading.zeros.in.numeric.literals"),
    ERROR_MISSING_DIGIT_AFTER_EXPONENT_INDICATOR("BCE1001", "error.missing.digit.after.exponent.indicator"),
    ERROR_INVALID_STRING_NUMERIC_ESCAPE_SEQUENCE("BCE1002", "error.invalid.string.numeric.escape.sequence"),
    ERROR_INVALID_ESCAPE_SEQUENCE("BCE1003", "error.invalid.escape.sequence"),
    ERROR_MISSING_DOUBLE_QUOTE("BCE1004", "error.missing.double.quote"),
    ERROR_MISSING_HEX_DIGIT_AFTER_DOT("BCE1005", "error.missing.hex.digit.after.dot"),
    ERROR_INVALID_WHITESPACE_BEFORE("BCE1006", "error.invalid.whitespace.before"),
    ERROR_INVALID_WHITESPACE_AFTER("BCE1006", "error.invalid.whitespace.after"),
    ERROR_INVALID_XML_NAME("BCE1007", "error.invalid.xml.name"),
    ERROR_INVALID_CHARACTER_IN_XML_ATTRIBUTE_VALUE("BCE1008",
            "error.invalid.character.in.xml.attribute.value"),
    ERROR_MISSING_ENTITY_REFERENCE_NAME("BCE1009", "error.missing.entity.reference.name"),
    ERROR_MISSING_SEMICOLON_IN_XML_REFERENCE("BCE1010", "error.missing.semicolon.in.xml.reference"),
    ERROR_INVALID_ENTITY_REFERENCE_NAME_START("BCE1011", "error.invalid.entity.reference.name.start"),
    ERROR_DOUBLE_HYPHEN_NOT_ALLOWED_WITHIN_XML_COMMENT("BCE1012",
            "error.double.hyphen.not.allowed.within.xml.comment"),
    ;

    String diagnosticId;
    String messageKey;

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
}
