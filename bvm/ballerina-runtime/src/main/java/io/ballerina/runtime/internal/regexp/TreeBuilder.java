/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal.regexp;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.values.RegExpAssertion;
import io.ballerina.runtime.internal.values.RegExpAtom;
import io.ballerina.runtime.internal.values.RegExpAtomQuantifier;
import io.ballerina.runtime.internal.values.RegExpCapturingGroup;
import io.ballerina.runtime.internal.values.RegExpCharSet;
import io.ballerina.runtime.internal.values.RegExpCharSetRange;
import io.ballerina.runtime.internal.values.RegExpCharacterClass;
import io.ballerina.runtime.internal.values.RegExpDisjunction;
import io.ballerina.runtime.internal.values.RegExpFlagExpression;
import io.ballerina.runtime.internal.values.RegExpFlagOnOff;
import io.ballerina.runtime.internal.values.RegExpLiteralCharOrEscape;
import io.ballerina.runtime.internal.values.RegExpQuantifier;
import io.ballerina.runtime.internal.values.RegExpSequence;
import io.ballerina.runtime.internal.values.RegExpTerm;
import io.ballerina.runtime.internal.values.RegExpValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Regular expression tree builder for Ballerina regular expression node.
 *
 * @since 2201.3.0
 */
public class TreeBuilder {

    private final TokenReader tokenReader;

    public TreeBuilder(TokenReader tokenReader) {
        this.tokenReader = tokenReader;
    }

    public RegExpValue parse() {
        RegExpDisjunction disjunction = readRegDisjunction();
        return new RegExpValue(disjunction);
    }

    public void parseInsertion() {
        readRegTerm();
    }

    /**
     * Read reDisjunction.
     *
     * @return RegExpDisjunction node
     */
    private RegExpDisjunction readRegDisjunction() {
        List<Object> reSequenceList = new ArrayList<>();
        Token nextToken = peek();
        while (!isEndOfReDisjunction(nextToken.kind)) {
            RegExpSequence reSequence = readRegSequence();
            reSequenceList.add(reSequence);
            nextToken = peek();
            if (nextToken.kind == TokenKind.PIPE_TOKEN) {
                Token pipe = consume();
                reSequenceList.add(pipe.value);
                nextToken = peek();
            }
        }
        return new RegExpDisjunction(reSequenceList.toArray());
    }

    /**
     * Read sequence of regular expression.
     *
     * @return RegExpSequence node
     */
    private RegExpSequence readRegSequence() {
        List<RegExpTerm> termsList = new ArrayList<>();
        Token nextToken = peek();
        while (!isEndOfReSequence(nextToken.kind)) {
            RegExpTerm reTerm = readRegTerm();
            termsList.add(reTerm);
            nextToken = peek();
        }
        return new RegExpSequence(termsList.toArray(new RegExpTerm[0]));
    }

    /**
     * Read term of regular expression.
     *
     * @return RegExpTerm node
     */
    private RegExpTerm readRegTerm() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.BITWISE_XOR_TOKEN || nextToken.kind == TokenKind.DOLLAR_TOKEN) {
            return readRegAssertion();
        }

        RegExpAtom reAtom;
        switch (nextToken.kind) {
            case RE_LITERAL_CHAR:
            case RE_NUMERIC_ESCAPE:
            case RE_CONTROL_ESCAPE:
            case COMMA_TOKEN:
            case DOT_TOKEN:
            case DIGIT:
            case MINUS_TOKEN:
            case COLON_TOKEN:    
                reAtom = readRegChars();
                break;
            case BACK_SLASH_TOKEN:
                reAtom = readRegEscapeChar();
                break;
            case OPEN_BRACKET_TOKEN:
                reAtom = readRegCharacterClass();
                break;
            case OPEN_PAREN_TOKEN:
                reAtom = readRegCapturingGroups();
                break;
            default:
                // Here the token is a syntax char, which is invalid. Syntax char tokens should be 
                // proceeded by backslashes.
                throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                        ErrorCodes.REGEXP_MISSING_BACKSLASH.messageKey(), nextToken.value));
        }

        RegExpQuantifier quantifier = readOptionalQuantifier();
        if (quantifier == null) {
            quantifier = new RegExpQuantifier("", "");
        }
        return new RegExpAtomQuantifier(reAtom, quantifier);
    }

    private RegExpAssertion readRegAssertion() {
        return new RegExpAssertion(consume().value);
    }

    private RegExpAtom readRegChars() {
        return new RegExpLiteralCharOrEscape(consume().value);
    }

    private RegExpLiteralCharOrEscape readRegEscapeChar() {
        Token backSlash = consume();
        return new RegExpLiteralCharOrEscape(readRegEscape(backSlash));
    }

    private String readRegEscape(Token backSlash) {
        Token nextToken = peek();
        switch (nextToken.kind) {
            case RE_PROPERTY:
                return readRegUnicodePropertyEscape(backSlash.value);
            case BITWISE_XOR_TOKEN:
            case DOLLAR_TOKEN:
            case BACK_SLASH_TOKEN:
            case DOT_TOKEN:
            case ASTERISK_TOKEN:
            case PLUS_TOKEN:
            case QUESTION_MARK_TOKEN:
            case OPEN_PAREN_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case OPEN_BRACKET_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case OPEN_BRACE_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case PIPE_TOKEN:
                return readRegQuoteEscape(backSlash.value);
            default:
                if (isReSimpleCharClassCode(nextToken)) {
                    return readRegSimpleCharClassEscape(backSlash.value);
                }
                throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                        ErrorCodes.REGEXP_INVALID_CHAR_AFTER_BACKSLASH.messageKey(), nextToken.value));
        }
    }

    /**
     * Read unicode property escape.
     *
     * @param backSlash backSlash
     * @return \p{sc=script} or \p{gc=category} etc.
     */
    private String readRegUnicodePropertyEscape(String backSlash) {
        String property = consume().value;
        String openBrace = readOpenBrace();
        String unicodeProperty = readUnicodeProperty();
        String closeBrace = readCloseBrace();
        return backSlash + property + openBrace + unicodeProperty + closeBrace;
    }

    /**
     * Read open brace.
     *
     * @return '{' character
     */
    private String readOpenBrace() {
        try {
            Token nextToken = peek();
            if (nextToken.kind == TokenKind.OPEN_BRACE_TOKEN) {
                return consume().value;
            }
        } catch (BError ignored) {
        }
        throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                ErrorCodes.REGEXP_MISSING_OPEN_BRACE.messageKey()));
    }

    /**
     * Read unicode property.
     *
     * @return sc=script or gc=category etc.
     */
    private String readUnicodeProperty() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.RE_UNICODE_SCRIPT_START) {
            return readRegUnicodeScript();
        }
        return readRegUnicodeGeneralCategory();
    }

    /**
     * Read the unicode script.
     *
     * @return sc=script
     */
    private String readRegUnicodeScript() {
        Token scriptStart = consume();
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.RE_UNICODE_PROPERTY_VALUE) {
            Token unicodePropertyValue = consume();
            return scriptStart.value + unicodePropertyValue.value;
        }
        throw ErrorCreator.createError(getErrorMsg(nextToken));
    }

    /**
     * Read the unicode general category.
     *
     * @return gc=category
     */
    private String readRegUnicodeGeneralCategory() {
        Token nextToken = peek();
        Token scriptStart = null;
        if (nextToken.kind == TokenKind.RE_UNICODE_GENERAL_CATEGORY_START) {
            scriptStart = consume();
        }
        nextToken = peek();
        Token generalCategory;
        if (nextToken.kind == TokenKind.RE_UNICODE_GENERAL_CATEGORY_NAME) {
            generalCategory = consume();
            return scriptStart != null ? scriptStart.value + generalCategory.value : generalCategory.value;
        }
        throw ErrorCreator.createError(getErrorMsg(nextToken));
    }

    /**
     * Read close brace.
     *
     * @return '}' character
     */
    private String readCloseBrace() {
        try {
            Token nextToken = peek();
            if (nextToken.kind == TokenKind.CLOSE_BRACE_TOKEN) {
                return consume().value;
            }
        } catch (BError ignored) {
        }
        throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                ErrorCodes.REGEXP_MISSING_CLOSE_BRACE.messageKey()));
    }

    /**
     * Read quote escape.
     *
     * @return \ReSyntaxChar
     */
    private String readRegQuoteEscape(String backSlash) {
        Token syntaxChar = consume();
        return backSlash + syntaxChar.value;
    }

    private String readRegSimpleCharClassEscape(String backSlash) {
        Token simpleCharClassCode = consume();
        return backSlash + simpleCharClassCode.value;
    }

    private RegExpCharacterClass readRegCharacterClass() {
        String characterClassStart = consume().value;
        // Read ^ char.
        String negation = readNegation();
        RegExpCharSet characterSet = readRegCharSet();
        String characterClassEnd = readCharacterClassEnd();
        if (negation.isEmpty() && characterSet.getCharSetAtoms().length == 0) {
            throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                    ErrorCodes.REGEXP_EMPTY_CHARACTER_CLASS_DISALLOWED.messageKey()));
        }
        return new RegExpCharacterClass(characterClassStart, negation, characterSet, characterClassEnd);
    }

    private String readNegation() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.BITWISE_XOR_TOKEN) {
            return consume().value;
        }
        // Return empty string if negation token is not there.
        return "";
    }

    private RegExpCharSet readRegCharSet() {
        Token nextToken = peek();
        if (isCharacterClassEnd(nextToken.kind)) {
            return new RegExpCharSet(new Object[]{});
        }
        String startReCharSetAtom = readCharSetAtom(nextToken);
        nextToken = peek();
        if (isCharacterClassEnd(nextToken.kind)) {
            return new RegExpCharSet(new Object[]{startReCharSetAtom});
        }
        List<Object> charSetAtoms = new ArrayList<>();
        if (nextToken.kind == TokenKind.MINUS_TOKEN) {
            Token minus = consume();
            nextToken = peek();
            if (isCharacterClassEnd(nextToken.kind)) {
                return new RegExpCharSet(new Object[]{startReCharSetAtom, minus.value});
            }
            String rhsReCharSetAtom = readCharSetAtom(nextToken);
            if (isIncorrectCharRange(startReCharSetAtom, rhsReCharSetAtom)) {
                throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                        ErrorCodes.REGEXP_INVALID_CHAR_CLASS_RANGE.messageKey(),
                        startReCharSetAtom, rhsReCharSetAtom));
            }
            RegExpCharSetRange reCharSetRange = new RegExpCharSetRange(startReCharSetAtom, minus.value,
                    rhsReCharSetAtom);
            RegExpCharSet reCharSet = readRegCharSet();
            charSetAtoms.add(reCharSetRange);
            if (reCharSet.getCharSetAtoms().length > 0) {
                charSetAtoms.addAll(Arrays.asList(reCharSet.getCharSetAtoms()));
            }

            return new RegExpCharSet(charSetAtoms.toArray());
        }
        charSetAtoms.add(startReCharSetAtom);
        RegExpCharSet reCharSetNoDash = readCharSetNoDash(nextToken);
        if (reCharSetNoDash.getCharSetAtoms().length > 0) {
            charSetAtoms.addAll(Arrays.asList(reCharSetNoDash.getCharSetAtoms()));
        }
        return new RegExpCharSet(charSetAtoms.toArray());
    }

    private RegExpCharSet readCharSetNoDash(Token nextToken) {
        String startReCharSetAtomNoDash = readCharSetAtom(nextToken);
        nextToken = peek();
        if (isCharacterClassEnd(nextToken.kind)) {
            return new RegExpCharSet(new Object[]{startReCharSetAtomNoDash});
        }
        List<Object> charSetAtoms = new ArrayList<>();
        if (nextToken.kind == TokenKind.MINUS_TOKEN) {
            Token minus = consume();
            nextToken = peek();
            if (isCharacterClassEnd(nextToken.kind)) {
                return new RegExpCharSet(new Object[]{startReCharSetAtomNoDash, minus.value});
            }
            String rhsReCharSetAtom = readCharSetAtom(nextToken);
            if (isIncorrectCharRange(startReCharSetAtomNoDash, rhsReCharSetAtom)) {
                throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                        ErrorCodes.REGEXP_INVALID_CHAR_CLASS_RANGE.messageKey(),
                        startReCharSetAtomNoDash, rhsReCharSetAtom));
            }
            RegExpCharSetRange reCharSetRange = new RegExpCharSetRange(startReCharSetAtomNoDash, minus.value,
                    rhsReCharSetAtom);
            RegExpCharSet reCharSet = readRegCharSet();
            charSetAtoms.add(reCharSetRange);
            if (reCharSet.getCharSetAtoms().length > 0) {
                charSetAtoms.addAll(Arrays.asList(reCharSet.getCharSetAtoms()));
            }
            return new RegExpCharSet(charSetAtoms.toArray());
        }
        RegExpCharSet reCharSetNoDash = readCharSetNoDash(nextToken);
        charSetAtoms.add(startReCharSetAtomNoDash);
        if (reCharSetNoDash.getCharSetAtoms().length > 0) {
            charSetAtoms.addAll(Arrays.asList(reCharSetNoDash.getCharSetAtoms()));
        }
        return new RegExpCharSet(charSetAtoms.toArray());
    }

    private String readCharSetAtom(Token nextToken) {
        switch (nextToken.kind) {
            case MINUS_TOKEN:
            case RE_NUMERIC_ESCAPE:
            case RE_CONTROL_ESCAPE:
                return consume().value;
            case BACK_SLASH_TOKEN:
                Token token = peek(2);
                if (token.kind == TokenKind.MINUS_TOKEN) {
                    return consume().value + consume().value;
                }
                return readRegEscape(consume());
            default:
                Token next = peek();
                if (isReCharSetLiteralChar(next.value)) {
                    return consume().value;
                }
                throw ErrorCreator.createError(getErrorMsg(next));
        }
    }

    private String readCharacterClassEnd() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.CLOSE_BRACKET_TOKEN) {
            Token consumedToken = consume();
            return consumedToken.value;
        }
        throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                ErrorCodes.REGEXP_MISSING_CLOSE_BRACKET.messageKey()));
    }

    private RegExpQuantifier readOptionalQuantifier() {
        Token nextToken = peek();
        switch (nextToken.kind) {
            case QUESTION_MARK_TOKEN:
            case ASTERISK_TOKEN:
            case PLUS_TOKEN:
            case OPEN_BRACE_TOKEN:
                return readReQuantifier();
            default:
                return null;
        }
    }

    private RegExpQuantifier readReQuantifier() {
        String quantifier = readBaseQuantifier();
        String nonGreedyChar = readNonGreedyChar();
        return new RegExpQuantifier(quantifier, nonGreedyChar);
    }

    private String readBaseQuantifier() {
        Token nextToken = peek();
        if (nextToken.kind != TokenKind.OPEN_BRACE_TOKEN) {
            Token consumedToken = consume();
            return consumedToken.value;
        }
        // Read the braced quantifier.
        String openBrace = readOpenBrace();
        String leastDigits = readDigits(true);
        String comma = "";
        String mostDigits = "";
        nextToken = peek();
        if (nextToken.kind == TokenKind.COMMA_TOKEN) {
            Token consumedToken = consume();
            comma = consumedToken.value;
            mostDigits = readDigits(false);
        }
        String closeBrace = readCloseBrace();
        return openBrace + leastDigits + comma + mostDigits + closeBrace;
    }

    private String readDigits(boolean isLeastDigits) {
        StringBuilder digits = new StringBuilder();
        Token nextToken = peek();
        while (!isEndOfDigits(nextToken.kind, isLeastDigits)) {
            Token digit = consume();
            digits.append(digit.value);
            nextToken = peek();
        }
        return digits.toString();
    }

    private String readNonGreedyChar() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.QUESTION_MARK_TOKEN) {
            Token consumedToken = consume();
            return consumedToken.value;
        }
        // Return empty string if there is no non greedy char.
        return "";
    }

    private RegExpCapturingGroup readRegCapturingGroups() {
        String openParenthesis = consume().value;
        Token nextToken = peek();
        RegExpFlagExpression flagExpression;
        if (nextToken.kind == TokenKind.QUESTION_MARK_TOKEN) {
            flagExpression = readFlagExpression();
        } else {
            // Create empty flag expression.
            flagExpression = new RegExpFlagExpression("", new RegExpFlagOnOff(""), "");
        }
        RegExpDisjunction reDisjunction = readRegDisjunction();
        String closeParenthesis = readCloseParenthesis();
        return new RegExpCapturingGroup(openParenthesis, flagExpression, reDisjunction, closeParenthesis);
    }

    private RegExpFlagExpression readFlagExpression() {
        Token questionMark = consume();
        Token nextToken = peek();
        RegExpFlagOnOff reFlagsOnOff;
        if (!isEndOfFlagExpression(nextToken.kind)) {
            reFlagsOnOff = readRegFlagsOnOff();
        } else {
            // Create empty flags.
            reFlagsOnOff = new RegExpFlagOnOff("");
        }
        String colon = consume().value;
        return new RegExpFlagExpression(questionMark.value, reFlagsOnOff, colon);
    }

    private RegExpFlagOnOff readRegFlagsOnOff() {
        String lhsReFlags = readRegFlags();
        Token nextToken = peek();
        String dash = "";
        String rhsFlags = "";
        if (nextToken.kind == TokenKind.MINUS_TOKEN) {
            Token minus = consume();
            dash = minus.value;
            rhsFlags = readRegFlags();
        }
        validateDuplicateFlags(lhsReFlags + rhsFlags);
        return new RegExpFlagOnOff(lhsReFlags + dash + rhsFlags);
    }

    private String readRegFlags() {
        StringBuilder flags = new StringBuilder();
        Token nextToken = peek();
        while (!isEndOfReFlags(nextToken.kind)) {
            if (!isReFlag(nextToken)) {
                throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                        ErrorCodes.REGEXP_INVALID_FLAG.messageKey(), nextToken.value));
            }
            Token reFlag = consume();
            flags.append(reFlag.value);
            nextToken = peek();
        }
        return flags.toString();
    }

    private String readCloseParenthesis() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.CLOSE_PAREN_TOKEN) {
            Token consumedToken = consume();
            return consumedToken.value;
        }
        throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                ErrorCodes.REGEXP_MISSING_CLOSE_PAREN.messageKey()));
    }

    private boolean isEndOfReDisjunction(TokenKind kind) {
        switch (kind) {
            case EOF_TOKEN:
            case CLOSE_PAREN_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private boolean isEndOfReSequence(TokenKind kind) {
        switch (kind) {
            case EOF_TOKEN:
            case PIPE_TOKEN:
            case CLOSE_PAREN_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private boolean isCharacterClassEnd(TokenKind kind) {
        switch (kind) {
            case EOF_TOKEN:
            case CLOSE_BRACKET_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private boolean isReCharSetLiteralChar(String tokenText) {
        switch (tokenText) {
            case "\\":
            case "-":
            case "]":
                return false;
            default:
                return true;
        }
    }

    private boolean isEndOfFlagExpression(TokenKind kind) {
        return kind == TokenKind.COLON_TOKEN || kind == TokenKind.EOF_TOKEN;
    }

    private boolean isEndOfReFlags(TokenKind kind) {
        return kind == TokenKind.MINUS_TOKEN || kind == TokenKind.COLON_TOKEN || kind == TokenKind.EOF_TOKEN;
    }

    private boolean isEndOfDigits(TokenKind kind, boolean isLeastDigits) {
        switch (kind) {
            case CLOSE_BRACE_TOKEN:
            case EOF_TOKEN:
                return true;
            case COMMA_TOKEN:
                return isLeastDigits;
            default:
                return false;
        }
    }

    static boolean isIncorrectCharRange(String lhsValue, String rhsValue) {
        if (lhsValue.charAt(0) != '\\' && rhsValue.charAt(0) != '\\') {
            return lhsValue.compareTo(rhsValue) > 0;
        }
        return false;
    }

    static boolean isReSimpleCharClassCode(Token token) {
        if (token.kind != TokenKind.RE_LITERAL_CHAR) {
            return false;
        }
        switch (token.value) {
            case "d":
            case "D":
            case "s":
            case "S":
            case "w":
            case "W":
                return true;
            default:
                return false;
        }
    }

    static boolean isReFlag(Token nextToken) {
        if (nextToken.kind != TokenKind.RE_LITERAL_CHAR) {
            return false;
        }
        switch (nextToken.value) {
            case "m":
            case "s":
            case "i":
            case "x":
                return true;
            default:
                return false;
        }
    }

    private BString getErrorMsg(Token nextToken) {
        if (nextToken.kind == TokenKind.EOF_TOKEN) {
            return ErrorHelper.getErrorMessage(ErrorCodes.REGEXP_INVALID_END_CHARACTER.messageKey());
        }
        return ErrorHelper.getErrorMessage(ErrorCodes.REGEXP_INVALID_CHARACTER.messageKey(),
                nextToken.value);
    }

    private Token peek() {
        return this.tokenReader.peek();
    }

    private Token peek(int n) {
        return this.tokenReader.peek(n);
    }

    private Token consume() {
        return this.tokenReader.read();
    }

    private void validateDuplicateFlags(String flags) {
        Set<Character> charList = new HashSet<>();
        for (int i = 0; i < flags.length(); i++) {
            char flag = flags.charAt(i);
            if (charList.contains(flag)) {
                throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                        ErrorCodes.REGEXP_DUPLICATE_FLAG.messageKey(), flag));
            }
            charList.add(flag);
        }
    }
}
