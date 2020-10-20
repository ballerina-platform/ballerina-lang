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
package io.ballerina.toml.internal.diagnostics;

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
    ERROR_MISSING_CLOSE_BRACE_TOKEN("BCE0007", "error.missing.close.brace.token"),
    ERROR_MISSING_OPEN_BRACKET_TOKEN("BCE0008", "error.missing.open.bracket.token"),
    ERROR_MISSING_CLOSE_BRACKET_TOKEN("BCE0009", "error.missing.close.bracket.token"),
    ERROR_MISSING_EQUAL_TOKEN("BCE0010", "error.missing.equal.token"),
    ERROR_MISSING_COMMA_TOKEN("BCE00011", "error.missing.comma.token"),
    ERROR_MISSING_PLUS_TOKEN("BCE00012", "error.missing.plus.token"),

    ERROR_MISSING_DOUBLE_QUOTE_TOKEN("BCE00023", "error.missing.double.quote.token"),
    ERROR_MISSING_DOT_TOKEN("BCE00029", "error.missing.dot.token"),

    // Keywords
    ERROR_MISSING_TRUE_KEYWORD("BCE02014", "error.missing.true.keyword"),
    ERROR_MISSING_FALSE_KEYWORD("BCE02015", "error.missing.false.keyword"),

    //Separators
    ERROR_MISSING_HASH_TOKEN("BCE02202", "error.missing.hash.token"),
    ERROR_MISSING_SINGLE_QUOTE_TOKEN("BCE02203", "error.missing.single.quote.token"),

    // Operators
    ERROR_MISSING_MINUS_TOKEN("BCE02303", "error.missing.minus.token"),

    // literals
    ERROR_MISSING_IDENTIFIER("BCE02500", "error.missing.identifier"),
    ERROR_MISSING_STRING_LITERAL("BCE02501", "error.missing.string.literal"),
    ERROR_MISSING_DECIMAL_INTEGER_LITERAL("BCE02502", "error.missing.decimal.integer.literal"),
    ERROR_MISSING_HEX_INTEGER_LITERAL("BCE02503", "error.missing.hex.integer.literal"),
    ERROR_MISSING_DECIMAL_FLOATING_POINT_LITERAL("BCE02504", "error.missing.decimal.floating.point.literal"),
    ERROR_MISSING_HEX_FLOATING_POINT_LITERAL("BCE02505", "error.missing.hex.floating.point.literal"),

    //miscellaneous
    ERROR_INVALID_METADATA("BCE218", "error.invalid.metadata"),
    ERROR_INVALID_TOKEN("BCE404", "error.invalid.token"),

    // Lexer errors
    ERROR_LEADING_ZEROS_IN_NUMERIC_LITERALS("BCE1000", "error.leading.zeros.in.numeric.literals"),
    ERROR_MISSING_DIGIT_AFTER_EXPONENT_INDICATOR("BCE1001", "error.missing.digit.after.exponent.indicator"),
    ERROR_INVALID_STRING_NUMERIC_ESCAPE_SEQUENCE("BCE1002", "error.invalid.string.numeric.escape.sequence"),
    ERROR_INVALID_ESCAPE_SEQUENCE("BCE1003", "error.invalid.escape.sequence"),
    ERROR_MISSING_DOUBLE_QUOTE("BCE1004", "error.missing.double.quote"),
    ERROR_MISSING_HEX_DIGIT_AFTER_DOT("BCE1005", "error.missing.hex.digit.after.dot"),
    ERROR_INVALID_WHITESPACE_BEFORE("BCE1006", "error.invalid.whitespace.before"),
    ERROR_INVALID_WHITESPACE_AFTER("BCE1006", "error.invalid.whitespace.after"),

    ERROR_MISSING_KEY("BCE1500",
            "error.missing.key"),
    ERROR_MISSING_VALUE("BCE1501", "error.missing.value"),

    ERROR_MISSING_OPEN_DOUBLE_BRACKET("BCE1502", "error.missing.open.double.bracket"),
    ERROR_MISSING_CLOSE_DOUBLE_BRACKET("BCE1503", "error.missing.close.double.bracket"),

    ERROR_EXISTING_NODE("BCE1504", "error.existing.node"),
    ERROR_EMPTY_QUOTED_STRING("BCE1505", "error.empty.quoted.string");
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
