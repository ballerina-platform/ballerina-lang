/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Represents the state machine used for parsing.
 *
 * @since 2201.9.0
 */
public abstract class StateMachine {

    static final char CR = 0x000D;
    static final char NEWLINE = 0x000A;
    static final char HZ_TAB = 0x0009;
    static final char SPACE = 0x0020;
    static final char BACKSPACE = 0x0008;
    static final char FORMFEED = 0x000C;
    static final char QUOTES = '"';
    static final char REV_SOL = '\\';
    static final char SOL = '/';
    static final char EOF = (char) -1;
    static final String NULL = "null";
    static final String TRUE = "true";
    static final String FALSE = "false";
    static final State DOC_START_STATE = new DocumentStartState();
    static final State DOC_END_STATE = new DocumentEndState();
    static final State FIRST_FIELD_READY_STATE = new FirstFieldReadyState();
    static final State NON_FIRST_FIELD_READY_STATE = new NonFirstFieldReadyState();
    protected State fieldNameState;
    static final State END_FIELD_NAME_STATE = new EndFieldNameState();
    static final State FIELD_VALUE_READY_STATE = new FieldValueReadyState();
    protected State stringFieldValueState;
    static final State NON_STRING_FIELD_VALUE_STATE = new NonStringFieldValueState();
    static final State NON_STRING_VALUE_STATE = new NonStringValueState();
    protected State stringValueState;
    static final State FIELD_END_STATE = new FieldEndState();
    static final State STRING_AE_ESC_CHAR_PROCESSING_STATE = new StringAEEscapedCharacterProcessingState();
    static final State STRING_AE_PROCESSING_STATE = new StringAEProcessingState();
    static final State FIELD_NAME_UNICODE_HEX_PROCESSING_STATE = new FieldNameUnicodeHexProcessingState();
    static final State FIRST_ARRAY_ELEMENT_READY_STATE = new FirstArrayElementReadyState();
    static final State NON_FIRST_ARRAY_ELEMENT_READY_STATE = new NonFirstArrayElementReadyState();
    protected State stringArrayElementState;
    static final State NON_STRING_ARRAY_ELEMENT_STATE = new NonStringArrayElementState();
    static final State ARRAY_ELEMENT_END_STATE = new ArrayElementEndState();
    static final State STRING_FIELD_ESC_CHAR_PROCESSING_STATE = new StringFieldEscapedCharacterProcessingState();
    static final State STRING_VAL_ESC_CHAR_PROCESSING_STATE = new StringValueEscapedCharacterProcessingState();
    static final State FIELD_NAME_ESC_CHAR_PROCESSING_STATE = new FieldNameEscapedCharacterProcessingState();
    static final State STRING_FIELD_UNICODE_HEX_PROCESSING_STATE = new StringFieldUnicodeHexProcessingState();
    static final State STRING_VALUE_UNICODE_HEX_PROCESSING_STATE = new StringValueUnicodeHexProcessingState();

    protected Object currentJsonNode = null;
    protected final Deque<Object> nodesStack = new ArrayDeque<>();
    protected final Deque<String> fieldNames = new ArrayDeque<>();

    protected final StringBuilder hexBuilder = new StringBuilder(4);
    protected char[] charBuff = new char[1024];
    protected int charBuffIndex;

    protected int index = 0;
    protected int line = 1;
    protected int column = 0;
    protected char currentQuoteChar;

    String errorPrefix;

    StateMachine(String errorPrefix, State fieldNameState, State stringValueState, State stringFieldValueState,
                 State stringArrayElementState) {
        this.errorPrefix = errorPrefix;
        this.fieldNameState = fieldNameState;
        this.stringValueState = stringValueState;
        this.stringFieldValueState = stringFieldValueState;
        this.stringArrayElementState = stringArrayElementState;
    }

    public void reset() {
        this.index = 0;
        this.currentJsonNode = null;
        this.line = 1;
        this.column = 0;
        this.nodesStack.clear();
        this.fieldNames.clear();
    }

    public static boolean isWhitespace(char ch) {
        return ch == SPACE || ch == HZ_TAB || ch == NEWLINE || ch == CR;
    }

    public void processLocation(char ch) {
        if (ch == '\n') {
            this.line++;
            this.column = 0;
        } else {
            this.column++;
        }
    }

    public Object execute(Reader reader) throws BError {
        State currentState = DOC_START_STATE;
        try {
            char[] buff = new char[1024];
            int count;
            while ((count = reader.read(buff)) > 0) {
                this.index = 0;
                while (this.index < count) {
                    currentState = currentState.transition(this, buff, this.index, count);
                }
            }
            currentState = currentState.transition(this, new char[]{EOF}, 0, 1);
            if (currentState != DOC_END_STATE) {
                throw ErrorCreator.createError(StringUtils.fromString("invalid JSON document"));
            }
            return this.currentJsonNode;
        } catch (IOException e) {
            throw ErrorCreator.createError(StringUtils.fromString("Error reading JSON: " + e.getMessage()));
        } catch (ParserException e) {
            throw ErrorCreator.createError(StringUtils.fromString(e.getMessage() + " at line: " + this.line
                                                                  + " column: " + this.column));
        }
    }

    public void append(char ch) {
        try {
            this.charBuff[this.charBuffIndex] = ch;
            this.charBuffIndex++;
        } catch (ArrayIndexOutOfBoundsException e) {
            // this approach is faster than checking for the size
            this.growCharBuff();
            this.charBuff[this.charBuffIndex++] = ch;
        }
    }

    public void growCharBuff() {
        char[] newBuff = new char[charBuff.length * 2];
        System.arraycopy(this.charBuff, 0, newBuff, 0, this.charBuff.length);
        this.charBuff = newBuff;
    }

    public static void throwExpected(String... chars) throws ParserException {
        throw new ParserException("expected '" + String.join("' or '", chars) + "'");
    }

    protected abstract State finalizeObject() throws ParserException;

    protected abstract State initNewObject() throws ParserException;

    protected abstract State initNewArray() throws ParserException;

    /**
     * A specific state in the parsing state machine.
     */
    public interface State {

        /**
         * Input given to the current state for a transition.
         *
         * @param sm    the state machine
         * @param buff  the input characters for the current state
         * @param i     the location from the character should be read from
         * @param count the number of characters to read from the buffer
         * @return the new resulting state
         */
        State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException;
    }

    /**
     * Represents the input stream start state.
     */
    private static class DocumentStartState implements State {

        @Override
        public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
            char ch;
            State state = null;
            for (; i < count; i++) {
                ch = buff[i];
                sm.processLocation(ch);
                if (ch == '{') {
                    state = sm.initNewObject();
                } else if (ch == '[') {
                    state = sm.initNewArray();
                } else if (StateMachine.isWhitespace(ch)) {
                    state = this;
                    continue;
                } else if (ch == QUOTES) {
                    sm.currentQuoteChar = ch;
                    state = sm.stringValueState;
                } else if (ch == EOF) {
                    throw new ParserException("empty JSON document");
                } else {
                    state = NON_STRING_VALUE_STATE;
                }
                break;
            }
            if (state == NON_STRING_VALUE_STATE) {
                sm.index = i;
            } else {
                sm.index = i + 1;
            }
            return state;
        }
    }

    /**
     * Represents the JSON document end state.
     */
    private static class DocumentEndState implements State {

        @Override
        public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
            char ch;
            State state = null;
            for (; i < count; i++) {
                ch = buff[i];
                sm.processLocation(ch);
                if (StateMachine.isWhitespace(ch) || ch == EOF) {
                    state = this;
                    continue;
                }
                throw new ParserException(sm.errorPrefix + " has already ended");
            }
            sm.index = i + 1;
            return state;
        }
    }

    /**
     * Represents the state just before the first object field is defined.
     */
    private static class FirstFieldReadyState implements State {

        @Override
        public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
            char ch;
            State state = null;
            for (; i < count; i++) {
                ch = buff[i];
                sm.processLocation(ch);
                if (ch == QUOTES) {
                    state = sm.fieldNameState;
                    sm.currentQuoteChar = ch;
                } else if (StateMachine.isWhitespace(ch)) {
                    state = this;
                    continue;
                } else if (ch == '}') {
                    state = sm.finalizeObject();
                } else {
                    StateMachine.throwExpected("\"", "}");
                }
                break;
            }
            sm.index = i + 1;
            return state;
        }
    }

    /**
     * Represents the state just before the first array element is defined.
     */
    private static class FirstArrayElementReadyState implements State {

        @Override
        public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
            State state = null;
            char ch;
            for (; i < count; i++) {
                ch = buff[i];
                sm.processLocation(ch);
                if (StateMachine.isWhitespace(ch)) {
                    state = this;
                    continue;
                } else if (ch == QUOTES) {
                    state = sm.stringArrayElementState;
                    sm.currentQuoteChar = ch;
                } else if (ch == '{') {
                    state = sm.initNewObject();
                } else if (ch == '[') {
                    state = sm.initNewArray();
                } else if (ch == ']') {
                    state = sm.finalizeObject();
                } else {
                    state = NON_STRING_ARRAY_ELEMENT_STATE;
                }
                break;
            }
            if (state == NON_STRING_ARRAY_ELEMENT_STATE) {
                sm.index = i;
            } else {
                sm.index = i + 1;
            }
            return state;
        }
    }

    /**
     * Represents the state just before a non-first object field is defined.
     */
    private static class NonFirstFieldReadyState implements State {

        @Override
        public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
            State state = null;
            char ch;
            for (; i < count; i++) {
                ch = buff[i];
                sm.processLocation(ch);
                if (ch == QUOTES) {
                    sm.currentQuoteChar = ch;
                    state = sm.fieldNameState;
                } else if (StateMachine.isWhitespace(ch)) {
                    state = this;
                    continue;
                } else {
                    StateMachine.throwExpected("\"");
                }
                break;
            }
            sm.index = i + 1;
            return state;
        }
    }

    /**
     * Represents the state just before a non-first array element is defined.
     */
    private static class NonFirstArrayElementReadyState implements State {

        @Override
        public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
            State state = null;
            char ch;
            for (; i < count; i++) {
                ch = buff[i];
                sm.processLocation(ch);
                if (StateMachine.isWhitespace(ch)) {
                    state = this;
                    continue;
                } else if (ch == QUOTES) {
                    state = sm.stringArrayElementState;
                    sm.currentQuoteChar = ch;
                } else if (ch == '{') {
                    state = sm.initNewObject();
                } else if (ch == '[') {
                    state = sm.initNewArray();
                } else if (ch == ']') {
                    throw new ParserException("expected an array element");
                } else {
                    state = NON_STRING_ARRAY_ELEMENT_STATE;
                }
                break;
            }
            if (state == NON_STRING_ARRAY_ELEMENT_STATE) {
                sm.index = i;
            } else {
                sm.index = i + 1;
            }
            return state;
        }
    }

    String value() {
        String result = new String(this.charBuff, 0, this.charBuffIndex);
        this.charBuffIndex = 0;
        return result;
    }

    public void processFieldName() {
        this.fieldNames.push(this.value());
    }

    /**
     * Represents the state where a field name definition has ended.
     */
    private static class EndFieldNameState implements State {

        @Override
        public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
            State state = null;
            char ch;
            for (; i < count; i++) {
                ch = buff[i];
                sm.processLocation(ch);
                if (StateMachine.isWhitespace(ch)) {
                    state = this;
                    continue;
                } else if (ch == ':') {
                    state = FIELD_VALUE_READY_STATE;
                } else {
                    StateMachine.throwExpected(":");
                }
                break;
            }
            sm.index = i + 1;
            return state;
        }
    }

    /**
     * Represents the state where a field value is about to be defined.
     */
    private static class FieldValueReadyState implements State {

        @Override
        public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
            State state = null;
            char ch;
            for (; i < count; i++) {
                ch = buff[i];
                sm.processLocation(ch);
                if (StateMachine.isWhitespace(ch)) {
                    state = this;
                    continue;
                } else if (ch == QUOTES) {
                    state = sm.stringFieldValueState;
                    sm.currentQuoteChar = ch;
                } else if (ch == '{') {
                    state = sm.initNewObject();
                } else if (ch == '[') {
                    state = sm.initNewArray();
                } else if (ch == ']' || ch == '}') {
                    throw new ParserException("expected a field value");
                } else {
                    state = NON_STRING_FIELD_VALUE_STATE;
                }
                break;
            }
            if (state == NON_STRING_FIELD_VALUE_STATE) {
                sm.index = i;
            } else {
                sm.index = i + 1;
            }
            return state;
        }
    }

    /**
     * Represents the state during a non-string field value is defined.
     */
    private static class NonStringFieldValueState implements State {

        @Override
        public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
            State state = null;
            char ch;
            for (; i < count; i++) {
                ch = buff[i];
                sm.processLocation(ch);
                if (ch == '{') {
                    state = sm.initNewObject();
                } else if (ch == '[') {
                    state = sm.initNewArray();
                } else if (ch == '}' || ch == ']') {
                    sm.processNonStringValue(ValueType.FIELD);
                    state = sm.finalizeObject();
                } else if (ch == ',') {
                    sm.processNonStringValue(ValueType.FIELD);
                    state = NON_FIRST_FIELD_READY_STATE;
                } else if (StateMachine.isWhitespace(ch)) {
                    sm.processNonStringValue(ValueType.FIELD);
                    state = FIELD_END_STATE;
                } else if (ch == EOF) {
                    throw new ParserException("unexpected end of " + sm.errorPrefix);
                } else {
                    sm.append(ch);
                    state = this;
                    continue;
                }
                break;
            }
            sm.index = i + 1;
            return state;
        }
    }

    /**
     * Represents the state during a non-string array element is defined.
     */
    private static class NonStringArrayElementState implements State {

        @Override
        public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
            State state = null;
            char ch;
            for (; i < count; i++) {
                ch = buff[i];
                sm.processLocation(ch);
                if (ch == '{') {
                    state = sm.initNewObject();
                } else if (ch == '[') {
                    state = sm.initNewArray();
                } else if (ch == ']') {
                    sm.processNonStringValue(ValueType.ARRAY_ELEMENT);
                    state = sm.finalizeObject();
                } else if (ch == ',') {
                    sm.processNonStringValue(ValueType.ARRAY_ELEMENT);
                    state = NON_FIRST_ARRAY_ELEMENT_READY_STATE;
                } else if (StateMachine.isWhitespace(ch)) {
                    sm.processNonStringValue(ValueType.ARRAY_ELEMENT);
                    state = ARRAY_ELEMENT_END_STATE;
                } else if (ch == EOF) {
                    throw new ParserException("unexpected end of " + sm.errorPrefix);
                } else {
                    sm.append(ch);
                    state = this;
                    continue;
                }
                break;
            }
            sm.index = i + 1;
            return state;
        }
    }

    /**
     * Represents the state during a non-string value is defined.
     */
    private static class NonStringValueState implements State {

        @Override
        public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
            State state = null;
            char ch;
            for (; i < count; i++) {
                ch = buff[i];
                sm.processLocation(ch);
                if (StateMachine.isWhitespace(ch) || ch == EOF) {
                    sm.currentJsonNode = null;
                    sm.processNonStringValue(ValueType.VALUE);
                    state = DOC_END_STATE;
                } else {
                    sm.append(ch);
                    state = this;
                    continue;
                }
                break;
            }
            sm.index = i + 1;
            return state;
        }
    }

    /**
     * Represents the state where an object field has ended.
     */
    private static class FieldEndState implements State {

        @Override
        public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
            State state = null;
            char ch;
            for (; i < count; i++) {
                ch = buff[i];
                sm.processLocation(ch);
                if (StateMachine.isWhitespace(ch)) {
                    state = this;
                    continue;
                } else if (ch == ',') {
                    state = NON_FIRST_FIELD_READY_STATE;
                } else if (ch == '}') {
                    state = sm.finalizeObject();
                } else {
                    StateMachine.throwExpected(",", "}");
                }
                break;
            }
            sm.index = i + 1;
            return state;
        }
    }

    /**
     * Represents the state where an array element has ended.
     */
    private static class ArrayElementEndState implements State {

        @Override
        public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
            State state = null;
            char ch;
            for (; i < count; i++) {
                ch = buff[i];
                sm.processLocation(ch);
                if (StateMachine.isWhitespace(ch)) {
                    state = this;
                    continue;
                } else if (ch == ',') {
                    state = NON_FIRST_ARRAY_ELEMENT_READY_STATE;
                } else if (ch == ']') {
                    state = sm.finalizeObject();
                } else {
                    StateMachine.throwExpected(",", "]");
                }
                break;
            }
            sm.index = i + 1;
            return state;
        }
    }

    /**
     * Represents the state where an escaped unicode character in hex format is processed
     * from an object string field.
     */
    private static class StringFieldUnicodeHexProcessingState extends UnicodeHexProcessingState {

        @Override
        protected State getSourceState(StateMachine sm) {
            return sm.stringFieldValueState;
        }
    }

    /**
     * Represents the state where an escaped unicode character in hex format is processed
     * from an array string field.
     */
    private static class StringAEProcessingState extends UnicodeHexProcessingState {

        @Override
        protected State getSourceState(StateMachine sm) {
            return sm.stringArrayElementState;
        }
    }

    /**
     * Represents the state where an escaped unicode character in hex format is processed
     * from a string value.
     */
    private static class StringValueUnicodeHexProcessingState extends UnicodeHexProcessingState {

        @Override
        protected State getSourceState(StateMachine sm) {
            return sm.stringValueState;
        }
    }

    /**
     * Represents the state where an escaped unicode character in hex format is processed
     * from a field name.
     */
    private static class FieldNameUnicodeHexProcessingState extends UnicodeHexProcessingState {

        @Override
        protected State getSourceState(StateMachine sm) {
            return sm.fieldNameState;
        }
    }

    /**
     * Represents the state where an escaped unicode character in hex format is processed.
     */
    private abstract static class UnicodeHexProcessingState implements State {

        protected abstract State getSourceState(StateMachine sm);

        @Override
        public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
            State state = null;
            char ch;
            for (; i < count; i++) {
                ch = buff[i];
                sm.processLocation(ch);
                if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f')) {
                    sm.hexBuilder.append(ch);
                    if (sm.hexBuilder.length() >= 4) {
                        sm.append(this.extractUnicodeChar(sm));
                        this.reset(sm);
                        state = this.getSourceState(sm);
                        break;
                    }
                    state = this;
                    continue;
                }
                this.reset(sm);
                throw new ParserException("expected the hexadecimal value of a unicode character");
            }
            sm.index = i + 1;
            return state;
        }

        private void reset(StateMachine sm) {
            sm.hexBuilder.setLength(0);
        }

        private char extractUnicodeChar(StateMachine sm) {
            return StringEscapeUtils.unescapeJava("\\u" + sm.hexBuilder).charAt(0);
        }
    }

    /**
     * Represents the state where an escaped character is processed in an object string field.
     */
    private static class StringFieldEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

        @Override
        protected State getSourceState(StateMachine sm) {
            return sm.stringFieldValueState;
        }
    }

    /**
     * Represents the state where an escaped character is processed in an array string field.
     */
    private static class StringAEEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

        @Override
        protected State getSourceState(StateMachine sm) {
            return sm.stringArrayElementState;
        }
    }

    /**
     * Represents the state where an escaped character is processed in a string value.
     */
    private static class StringValueEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

        @Override
        protected State getSourceState(StateMachine sm) {
            return sm.stringValueState;
        }
    }

    /**
     * Represents the state where an escaped character is processed in a field name.
     */
    private static class FieldNameEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

        @Override
        protected State getSourceState(StateMachine sm) {
            return sm.fieldNameState;
        }
    }

    /**
     * Represents the state where an escaped character is processed.
     */
    private abstract static class EscapedCharacterProcessingState implements State {

        protected abstract State getSourceState(StateMachine sm);

        @Override
        public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
            State state = null;
            char ch;
            if (i < count) {
                ch = buff[i];
                sm.processLocation(ch);
                switch (ch) {
                    case '"':
                        sm.append(QUOTES);
                        state = this.getSourceState(sm);
                        break;
                    case '\\':
                        sm.append(REV_SOL);
                        state = this.getSourceState(sm);
                        break;
                    case '/':
                        sm.append(SOL);
                        state = this.getSourceState(sm);
                        break;
                    case 'b':
                        sm.append(BACKSPACE);
                        state = this.getSourceState(sm);
                        break;
                    case 'f':
                        sm.append(FORMFEED);
                        state = this.getSourceState(sm);
                        break;
                    case 'n':
                        sm.append(NEWLINE);
                        state = this.getSourceState(sm);
                        break;
                    case 'r':
                        sm.append(CR);
                        state = this.getSourceState(sm);
                        break;
                    case 't':
                        sm.append(HZ_TAB);
                        state = this.getSourceState(sm);
                        break;
                    case 'u':
                        if (this.getSourceState(sm) == sm.stringFieldValueState) {
                            state = STRING_FIELD_UNICODE_HEX_PROCESSING_STATE;
                        } else if (this.getSourceState(sm) == sm.stringValueState) {
                            state = STRING_VALUE_UNICODE_HEX_PROCESSING_STATE;
                        } else if (this.getSourceState(sm) == sm.fieldNameState) {
                            state = FIELD_NAME_UNICODE_HEX_PROCESSING_STATE;
                        } else {
                            state = STRING_AE_PROCESSING_STATE;
                        }
                        break;
                    default:
                        throw new ParserException("expected escaped characters");
                }
            }
            sm.index = i + 1;
            return state;
        }
    }

    enum ValueType {
        FIELD, VALUE, ARRAY_ELEMENT
    }

    abstract void processNonStringValue(ValueType type) throws ParserException;

    abstract void setValueToJsonType(ValueType type, Object value);

    static boolean isExponential(String str) {
        return str.contains("e") || str.contains("E");
    }

    static boolean isNegativeZero(String str) {
        return '-' == str.charAt(0) && 0 == Double.parseDouble(str);
    }

}
