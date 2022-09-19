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
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
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
import java.util.List;

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
        RegExpDisjunction disjunction = readReDisjunction();
        return new RegExpValue(disjunction);
    }

    private RegExpDisjunction readReDisjunction() {
        List<Object> reSequenceList = new ArrayList<>();
        Token nextToken = peek();
        while (!isEndOfReDisjunction(nextToken.kind)) {
            RegExpSequence reSequence = readReSequence();
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

    private RegExpSequence readReSequence() {
        List<RegExpTerm> termsList = new ArrayList<>();
        Token nextToken = peek();
        while (!isEndOfReSequence(nextToken.kind)) {
            RegExpTerm reTerm = readReTerm();
            termsList.add(reTerm);
            nextToken = peek();
        }
        return new RegExpSequence(termsList.toArray(new RegExpTerm[0]));
    }

    private RegExpTerm readReTerm() {
        Token nextToken = peek();
        TokenKind tokenKind = nextToken.kind;
        if (tokenKind == TokenKind.RE_ASSERTION_VALUE) {
            return readReAssertion();
        }

        RegExpAtom reAtom;
        switch (nextToken.kind) {
            case BACK_SLASH_TOKEN:
                reAtom = readEscapeChar();
                break;
            case OPEN_BRACKET_TOKEN:
                reAtom = readCharacterClass();
                break;
            case OPEN_PAREN_TOKEN:
                reAtom = readCapturingGroups();
                break;
            default:
                // Read chars in ReLiteralChar, . or ReEscape.
                reAtom = readChars();
                break;
        }

        nextToken = peek();
        RegExpQuantifier quantifier;
        if (nextToken.kind == TokenKind.RE_BASE_QUANTIFIER_VALUE ||
                nextToken.kind == TokenKind.OPEN_BRACE_TOKEN) {
            quantifier = readReQuantifier();
        } else {
            // If there isn't a quantifier, create an empty quantifier.
            quantifier = new RegExpQuantifier("", "");
        }
        return new RegExpAtomQuantifier(reAtom, quantifier);
    }
    
    private RegExpAssertion readReAssertion() {
        Token assertion = consume();
        return new RegExpAssertion(assertion.value);
    }

    private RegExpAtom readChars() {
        Token chars = consume();
        return new RegExpLiteralCharOrEscape(chars.value);
    }

    private RegExpLiteralCharOrEscape readEscapeChar() {
        return new RegExpLiteralCharOrEscape(readReEscape());
    }

    private String readReEscape() {
        Token backSlash = consume();
        Token nextToken = peek();
        switch (nextToken.kind) {
            case RE_PROPERTY:
                return readReUnicodePropertyEscape(backSlash.value);
            case RE_SYNTAX_CHAR:
                return readReQuoteEscape(backSlash.value);
            case RE_SIMPLE_CHAR_CLASS_CODE:
                return readReSimpleCharClassEscape(backSlash.value);
        }
        Token consumedToken = consume();
        return consumedToken.value;
    }
    
    private String readReUnicodePropertyEscape(String backSlash) {
        Token consumedPropertyToken = consume();
        String property = consumedPropertyToken.value;
        String openBrace = readOpenBrace();
        String unicodeProperty = readUnicodeProperty();
        String closeBrace = readCloseBrace();
        return backSlash + property + openBrace + unicodeProperty + closeBrace;
    }
    
    private String readOpenBrace() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.OPEN_BRACE_TOKEN) {
            Token consumedToken = consume();
            return consumedToken.value;
        }
        throw new BallerinaException("Invalid character '" + nextToken.value + "'");
    }


    private String readUnicodeProperty() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.RE_UNICODE_SCRIPT_START) {
            return readReUnicodeScript();
        }
        return readReUnicodeGeneralCategory();
    }

    private String readReUnicodeScript() {
        Token scriptStart = consume();
        Token nextToken = peek();
        Token unicodePropertyValue;
        if (nextToken.kind == TokenKind.RE_UNICODE_PROPERTY_VALUE) {
            unicodePropertyValue = consume();
        } else {
            throw new BallerinaException("Invalid character '" + nextToken.value + "'");
        }
        return scriptStart.value + unicodePropertyValue.value;
    }

    private String readReUnicodeGeneralCategory() {
        Token nextToken = peek();
        Token scriptStart = null;
        if (nextToken.kind == TokenKind.RE_UNICODE_GENERAL_CATEGORY_START) {
            scriptStart = consume();
        }
        nextToken = peek();
        Token generalCategory;
        if (nextToken.kind == TokenKind.RE_UNICODE_GENERAL_CATEGORY_NAME) {
            generalCategory = consume();
        } else {
            throw new BallerinaException("Invalid character '" + nextToken.value + "'");
        }
        return scriptStart != null ? scriptStart.value + generalCategory.value : generalCategory.value;
    }

    private String readReQuoteEscape(String backSlash) {
        Token syntaxChar = consume();
        return backSlash + syntaxChar.value;
    }

    private String readReSimpleCharClassEscape(String backSlash) {
        Token simpleCharClassCode = consume();
        return backSlash + simpleCharClassCode.value;
    }
    
    private RegExpCharacterClass readCharacterClass() {
        String characterClassStart = readCharacterClassStart();
        // Read ^ char.
        String negation = readNegation();
        RegExpCharSet characterSet = readReCharSet();
        String characterClassEnd = readCharacterClassEnd();
        return new RegExpCharacterClass(characterClassStart, negation, characterSet, characterClassEnd);
    }
    
    private String readCharacterClassStart() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.OPEN_BRACKET_TOKEN) {
            Token consumedToken = consume();
            return consumedToken.value;
        }
        throw new BallerinaException("Invalid character '" + nextToken.value + "'");
    }
    
    private String readNegation() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.BITWISE_XOR_TOKEN) {
            Token consumedToken = consume();
            return consumedToken.value;
        }
        // Return empty string if negation token is not there.
        return "";
    }

    private RegExpCharSet readReCharSet() {
        Token nextToken = peek();
        if (isCharacterClassEnd(nextToken.kind)) {
            return new RegExpCharSet(new Object[]{});
        }
        String startReCharSetAtom = readCharSetAtom(nextToken);
        nextToken = peek();
        if (isCharacterClassEnd(nextToken.kind)) {
            return new RegExpCharSet(new Object[]{startReCharSetAtom});
        }
        if (nextToken.kind == TokenKind.MINUS_TOKEN) {
            Token minus = consume();
            nextToken = peek();
            if (isCharacterClassEnd(nextToken.kind)) {
                return new RegExpCharSet(new Object[]{startReCharSetAtom, minus.value});
            }
            String rhsReCharSetAtom = readCharSetAtom(nextToken);
            RegExpCharSetRange reCharSetRange = new RegExpCharSetRange(startReCharSetAtom, minus.value,
                    rhsReCharSetAtom);
            RegExpCharSet reCharSet = readReCharSet();
            return new RegExpCharSet(new Object[]{reCharSetRange, reCharSet});
        }
        RegExpCharSet reCharSetNoDash = readCharSetNoDash(nextToken);
        return new RegExpCharSet(new Object[]{startReCharSetAtom, reCharSetNoDash});
    }
    
    private RegExpCharSet readCharSetNoDash(Token nextToken) {
        String startReCharSetAtomNoDash = readCharSetAtom(nextToken);
        nextToken = peek();
        if (isCharacterClassEnd(nextToken.kind)) {
            return new RegExpCharSet(new Object[]{startReCharSetAtomNoDash});
        }
        if (nextToken.kind == TokenKind.MINUS_TOKEN) {
            Token minus = consume();
            nextToken = peek();
            if (isCharacterClassEnd(nextToken.kind)) {
                return new RegExpCharSet(new Object[]{startReCharSetAtomNoDash, minus.value});
            }
            String rhsReCharSetAtom = readCharSetAtom(nextToken);
            RegExpCharSetRange reCharSetRange = new RegExpCharSetRange(startReCharSetAtomNoDash, minus.value,
                    rhsReCharSetAtom);
            RegExpCharSet reCharSet = readReCharSet();
            return new RegExpCharSet(new Object[]{reCharSetRange, reCharSet});
        }
        RegExpCharSet reCharSetNoDash = readCharSetNoDash(nextToken);
        return new RegExpCharSet(new Object[]{startReCharSetAtomNoDash, reCharSetNoDash});
    }

    private String readCharSetAtom(Token nextToken) {
        if (nextToken.kind == TokenKind.BACK_SLASH_TOKEN) {
            return readReEscape();
        }
        Token consumedToken = consume();
        return consumedToken.value;
    }

    private String readCharacterClassEnd() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.CLOSE_BRACKET_TOKEN) {
            Token consumedToken = consume();
            return consumedToken.value;
        }
        throw new BallerinaException("Invalid character '" + nextToken.value + "'");
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
    
    private String readCloseBrace() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.CLOSE_BRACE_TOKEN) {
            Token consumedToken = consume();
            return consumedToken.value;
        }
        throw new BallerinaException("Invalid character '" + nextToken.value + "'");
    }
    
    private String readNonGreedyChar() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.QUESTION_MARK_TOKEN) {
            Token consumedToken = consume();
            return consumedToken.value;
        }
        throw new BallerinaException("Invalid character '" + nextToken.value + "'");
    }
    
    private RegExpCapturingGroup readCapturingGroups() {
        String openParenthesis = readOpenParenthesis();
        Token nextToken = peek();
        RegExpFlagExpression flagExpression;
        if (nextToken.kind == TokenKind.QUESTION_MARK_TOKEN) {
            flagExpression = readFlagExpression();
        } else {
            // Create empty flag expression.
            flagExpression = new RegExpFlagExpression("", new RegExpFlagOnOff(""), "");
        }
        RegExpDisjunction reDisjunction = readReDisjunction();
        String closeParenthesis = readCloseParenthesis();
        return new RegExpCapturingGroup(openParenthesis, flagExpression, reDisjunction, closeParenthesis);
    }

    private String readOpenParenthesis() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.OPEN_PAREN_TOKEN) {
            Token consumedToken = consume();
            return consumedToken.value;
        }
        throw new BallerinaException("Invalid character '" + nextToken.value + "'");
    }

    private RegExpFlagExpression readFlagExpression() {
        Token questionMark = consume();
        Token nextToken = peek();
        RegExpFlagOnOff reFlagsOnOff;
        if (!isEndOfFlagExpression(nextToken.kind)) {
            reFlagsOnOff = readReFlagsOnOff();
        } else {
            // Create empty flags.
            reFlagsOnOff = new RegExpFlagOnOff("");
        }
        String colon = readColon();
        return new RegExpFlagExpression(questionMark.value, reFlagsOnOff, colon);
    }

    private RegExpFlagOnOff readReFlagsOnOff() {
        String lhsReFlags = readReFlags();
        Token nextToken = peek();
        String dash = "";
        String rhsFlags = "";
        if (nextToken.kind == TokenKind.MINUS_TOKEN) {
            Token minus = consume();
            dash = minus.value;
            rhsFlags = readReFlags();
        }
        return new RegExpFlagOnOff(lhsReFlags + dash + rhsFlags);
    }

    private String readReFlags() {
        StringBuilder flags = new StringBuilder();
        Token nextToken = peek();
        while (!isEndOfReFlags(nextToken.kind)) {
            Token reFlag = consume();
            flags.append(reFlag.value);
            nextToken = peek();
        }
        return flags.toString();
    }

    private String readColon() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.COLON_TOKEN) {
            Token consumedToken = consume();
            return consumedToken.value;
        }
        throw new BallerinaException("Invalid character '" + nextToken.value + "'");
    }

    private String readCloseParenthesis() {
        Token nextToken = peek();
        if (nextToken.kind == TokenKind.CLOSE_PAREN_TOKEN) {
            Token consumedToken = consume();
            return consumedToken.value;
        }
        throw new BallerinaException("Invalid character '" + nextToken.value + "'");
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

    private Token peek() {
        return this.tokenReader.peek();
    }

    private Token consume() {
        return this.tokenReader.read();
    }
}
