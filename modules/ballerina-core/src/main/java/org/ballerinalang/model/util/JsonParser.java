/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.model.util;

import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.util.JsonNode.Type;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * This class represents a JSON parser.
 * 
 * @since 0.95.5
 */
public class JsonParser {

    /**
     * Parses the contents in the given {@link InputStream} and returns a {@link JsonNode}.
     * 
     * @param in input stream which contains the JSON content
     * @return JSON structure as a {@link JsonNode} object
     * @throws BallerinaException
     */
    public static JsonNode parse(InputStream in) throws BallerinaException {
        return parse(in, Charset.defaultCharset().name());
    }
    
    /**
     * Parses the contents in the given {@link InputStream} and returns a {@link JsonNode}.
     * 
     * @param in input stream which contains the JSON content
     * @param charsetName the character set name of the input stream
     * @return JSON structure as a {@link JsonNode} object
     * @throws BallerinaException
     */
    public static JsonNode parse(InputStream in, String charsetName) throws BallerinaException {
        try {
            return parse(new InputStreamReader(new BufferedInputStream(in), charsetName));
        } catch (IOException e) {
            throw new BallerinaException("Error in parsing JSON data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parses the contents in the given string and returns a {@link JsonNode}.
     * 
     * @param jsonStr the string which contains the JSON content
     * @return JSON structure as a {@link JsonNode} object
     * @throws BallerinaException
     */
    public static JsonNode parse(String jsonStr) throws BallerinaException {
        return parse(new StringReader(jsonStr));
    }
    
    /**
     * Parses the contents in the given {@link Reader} and returns a {@link JsonNode}.
     * 
     * @param reader reader which contains the JSON content
     * @return JSON structure as a {@link JsonNode} object
     * @throws BallerinaException
     */
    public static JsonNode parse(Reader reader) throws BallerinaException {
        return new StateMachine(reader).execute();
    }
    
    /**
     * Represents a JSON parser related exception.
     */
    private static class JsonParserException extends Exception {
        
        private static final long serialVersionUID = 6359022327525293320L;

        public JsonParserException(String msg) {
            super(msg);
        }
        
    }
    
    /**
     * Represents the state machine used for JSON parsing.
     */
    private static class StateMachine {
        
        private static final char CR = 0x000D;
        private static final char NEWLINE = 0x000A;
        private static final char HZ_TAB = 0x0009;
        private static final char SPACE = 0x0020;
        private static final char BACKSPACE = 0x0008;
        private static final char FORMFEED = 0x000C;
        private static final char QUOTES = '"';
        private static final char SINGLE_QUOTES = '\'';
        private static final char REV_SOL = '\\';
        private static final char SOL = '/';
        private static final char EOF = (char) -1;
        private static final String NULL = "null";
        private static final String TRUE = "true";
        private static final String FALSE = "false";
        
        private final State docStartState = new DocumentStartState();
        private final State docEndState = new DocumentEndState();
        private final State firstFieldReadyState = new FirstFieldReadyState();
        private final State nonFirstFieldReadyState = new NonFirstFieldReadyState();
        private final State fieldNameState = new FieldNameState();
        private final State endFieldNameState = new EndFieldNameState();
        private final State fieldValueReadyState = new FieldValueReadyState();
        private final State stringFieldValueState = new StringFieldValueState();
        private final State nonStringFieldValueState = new NonStringFieldValueState();
        private final State nonStringValueState = new NonStringValueState();
        private final State stringValueState = new StringValueState();
        private final State fieldEndState = new FieldEndState();
        private final State stringFieldEscCharProcessingState = new StringFieldEscapedCharacterProcessingState();
        private final State stringAEEscCharProcessingState = new StringAEEscapedCharacterProcessingState();
        private final State stringValEscCharProcessingState = new StringValueEscapedCharacterProcessingState();
        private final State fieldNameEscCharProcessingState = new FieldNameEscapedCharacterProcessingState();
        private final State stringFieldUnicodeHexProcessingState = new StringFieldUnicodeHexProcessingState();
        private final State stringAEProcessingState = new StringAEProcessingState();
        private final State stringValueUnicodeHexProcessingState = new StringValueUnicodeHexProcessingState();
        private final State fieldNameUnicodeHexProcessingState = new FieldNameUnicodeHexProcessingState();
        private final State firstArrayElementReadyState = new FirstArrayElementReadyState();
        private final State nonFirstArrayElementReadyState = new NonFirstArrayElementReadyState();
        private final State stringArrayElementState = new StringArrayElementState();
        private final State nonStringArrayElementState = new NonStringArrayElementState();
        private final State arrayElementEndState = new ArrayElementEndState();
        
        private Reader reader;
        private JsonNode currentJsonNode;
        private char[] charBuff = new char[1024];
        private int charBuffIndex;
        
        private int index;
        private int line = 1;
        private int column = 0;
        private char currentQuoteChar;
        
        public StateMachine(Reader reader) {
            this.reader = reader;
        }
        
        private boolean isWhitespace(char ch) {
            return ch == SPACE || ch == HZ_TAB || ch == NEWLINE || ch == CR;
        }
        
        private void throwExpected(String... chars) throws JsonParserException {
            throw new JsonParserException("expected " + String.join(" or ", chars));
        }
        
        private void processLocation(char ch) {
            if (ch == '\n') {
                this.line++;
                this.column = 0;
            } else {
                this.column++;
            }
        }
        
        public JsonNode execute() throws BallerinaException {
            State currentState = docStartState;
            try {
                char[] buff = new char[1024];
                int count;
                while ((count = this.reader.read(buff)) > 0) {
                    this.index = 0;
                    while (this.index < count) {
                        currentState = currentState.transition(buff, this.index, count);
                    }
                }
                currentState = currentState.transition(new char[] { EOF }, 0, 1);
                if (currentState != docEndState) {
                    throw new BallerinaException("invalid JSON document");
                }
                return this.currentJsonNode;
            } catch (IOException e) {
                throw new BallerinaException("Error reading JSON: " + e.getMessage());
            } catch (JsonParserException e) {
                throw new BallerinaException(e.getMessage() + " at line: " + this.line + " column: " + this.column);
            }
        }
        
        private void append(char ch) {
            try {
                this.charBuff[charBuffIndex++] = ch;
            } catch (ArrayIndexOutOfBoundsException e) {
                /* this approach is faster than checking for the size by ourself */
                this.growCharBuff();
                this.charBuff[charBuffIndex++] = ch;
            }
        }
        
        private void growCharBuff() {
            char[] newBuff = new char[charBuff.length * 2];
            System.arraycopy(this.charBuff, 0, newBuff, 0, this.charBuff.length);
            this.charBuff = newBuff;
        }
        
        private State finalizeObject() {
            JsonNode parentNode = currentJsonNode.parentNode;
            if (parentNode != null) {
                if (parentNode.getType() == Type.OBJECT) {
                    parentNode.set(parentNode.fieldName, currentJsonNode);
                    currentJsonNode = parentNode;
                    return fieldEndState;
                } else {
                    parentNode.add(currentJsonNode);
                    currentJsonNode = parentNode;
                    return arrayElementEndState;
                }
            } else {
                return docEndState;
            }
        }
        
        private State initNewObject() {
            if (currentJsonNode != null) {
                JsonNode parentNode = currentJsonNode;
                currentJsonNode = new JsonNode();
                currentJsonNode.parentNode = parentNode;
            } else {
                currentJsonNode = new JsonNode();
            }
            return firstFieldReadyState;
        }
        
        private State initNewArray() {
            if (currentJsonNode != null) {
                JsonNode parentNode = currentJsonNode;
                currentJsonNode = new JsonNode(Type.ARRAY);
                currentJsonNode.parentNode = parentNode;
            } else {
                currentJsonNode = new JsonNode(Type.ARRAY);
            }
            return firstArrayElementReadyState;
        }
        
        /**
         * A specific state in the JSON parsing state machine.
         */
        private interface State {
            
            /**
             * Input given to the current state for a transition. 
             * 
             * @param buff the input characters for the current state
             * @param i the location from the character should be read from
             * @param count the number of characters to read from the buffer
             * @return the new resulting state
             */
            State transition(char[] buff, int i, int count) throws JsonParserException;
            
        }
        
        /**
         * Represents the JSON document start state.
         */
        private class DocumentStartState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                char ch;
                State state = null;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (ch == '{') {
                        state = initNewObject();
                    } else if (ch == '[') {
                        state = initNewArray();
                    } else if (isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == QUOTES || ch == SINGLE_QUOTES) {
                        currentQuoteChar = ch;
                        state = stringValueState;
                    } else if (ch == EOF) {
                        throw new JsonParserException("unexpected end of JSON document");
                    } else {
                        state = nonStringValueState;
                    }
                    break;
                }
                if (state == nonStringValueState) {
                    index = i;
                } else {
                    index = i + 1;
                }
                return state;
            }
            
        }
        
        /**
         * Represents the JSON document end state.
         */
        private class DocumentEndState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                char ch;
                State state = null;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (isWhitespace(ch) || ch == EOF) {
                        state = this;
                        continue;
                    } else {
                        throw new JsonParserException("JSON document has already ended");
                    }
                }
                index = i + 1;
                return state;
            }
            
        }
        
        /**
         * Represents the state just before the first object field is defined.
         */
        private class FirstFieldReadyState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                char ch;
                State state =  null;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (ch == QUOTES || ch == SINGLE_QUOTES) {
                        state = fieldNameState;
                        currentQuoteChar = ch;
                    } else if (isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch ==  '}') {
                        state = finalizeObject();
                    } else {
                        throwExpected("\"", "}");
                    }
                    break;
                }
                index = i + 1;
                return state;
            }
            
        }
        
        /**
         * Represents the state just before the first array element is defined.
         */
        private class FirstArrayElementReadyState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == QUOTES) {
                        state = stringArrayElementState;
                    } else if (ch == '{') {
                        state = initNewObject(); 
                    } else if (ch == '[') {
                        state = initNewArray(); 
                    } else if (ch == ']') {
                        state = finalizeObject(); 
                    } else {
                        state = nonStringArrayElementState;
                    }
                    break;
                }
                if (state == nonStringArrayElementState) {
                    index = i;
                } else {
                    index = i + 1;
                }
                return state;
            }
            
        }
        
        /**
         * Represents the state just before a non-first object field is defined.
         */
        private class NonFirstFieldReadyState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (ch == QUOTES || ch == SINGLE_QUOTES) {
                        currentQuoteChar = ch;
                        state = fieldNameState;
                    } else if (isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else {
                        throwExpected("\"");
                    }
                    break;
                }
                index = i + 1;
                return state;
            }
            
        }
        
        /**
         * Represents the state just before a non-first array element is defined.
         */
        private class NonFirstArrayElementReadyState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == QUOTES) {
                        state = stringArrayElementState;
                    } else if (ch == '{') {
                        state = initNewObject(); 
                    } else if (ch == '[') {
                        state = initNewArray(); 
                    } else {
                        state = nonStringArrayElementState;
                    }
                    break;
                }
                if (state == nonStringArrayElementState) {
                    index = i;
                } else {
                    index = i + 1;
                }
                return state;
            }
            
        }
        
        private String value() {
            String result = new String(charBuff, 0, charBuffIndex);
            charBuffIndex = 0;
            return result;
        }
        
        private void processFieldName() {
            this.currentJsonNode.fieldName = this.value();
        }
        
        /**
         * Represents the state during a field name.
         */
        private class FieldNameState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                char ch;
                State state = null;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (ch == currentQuoteChar) {
                        processFieldName();
                        state = endFieldNameState;
                    } else if (ch == REV_SOL) { 
                        state = fieldNameEscCharProcessingState;
                    } else if (ch == EOF) {
                        throw new JsonParserException("unexpected end of JSON document");
                    } else {
                        append(ch);
                        state = this;
                        continue;
                    }
                    break;
                }
                index = i + 1;
                return state;
            }
             
        }
        
        /**
         * Represents the state where a field name definition has ended.
         */
        private class EndFieldNameState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == ':') {
                        state = fieldValueReadyState;
                    } else {
                        throwExpected(":");
                    }
                    break;
                }
                index = i + 1;
                return state;
            }
            
        }
        
        /**
         * Represents the state where a field value is about to be defined.
         */
        private class FieldValueReadyState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == QUOTES || ch == SINGLE_QUOTES) {
                        state = stringFieldValueState;
                        currentQuoteChar = ch;
                    } else if (ch == '{') {
                        state = initNewObject(); 
                    } else if (ch == '[') {
                        state = initNewArray(); 
                    } else {
                        state = nonStringFieldValueState;
                    }
                    break;
                }
                if (state == nonStringFieldValueState) {
                    index = i;
                } else {
                    index = i + 1;
                }
                return state;
            }
            
        }
        
        /**
         * Represents the state during a string field value is defined.
         */
        private class StringFieldValueState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (ch == currentQuoteChar) {
                        currentJsonNode.set(currentJsonNode.fieldName, value());
                        state = fieldEndState;
                    } else if (ch == REV_SOL) { 
                        state = stringFieldEscCharProcessingState;
                    } else if (ch == EOF) {
                        throw new JsonParserException("unexpected end of JSON document");
                    } else {
                        append(ch);
                        state = this;
                        continue;
                    }
                    break;
                }
                index = i + 1;
                return state;
            }
            
        }
        
        /**
         * Represents the state during a string array element is defined.
         */
        private class StringArrayElementState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (ch == QUOTES) {
                        currentJsonNode.add(new JsonNode(value()));
                        state = arrayElementEndState;
                    } else if (ch == REV_SOL) { 
                        state = stringAEEscCharProcessingState;
                    } else if (ch == EOF) {
                        throw new JsonParserException("unexpected end of JSON document");
                    } else {
                        append(ch);
                        state = this;
                        continue;
                    }
                    break;
                }
                index = i + 1;
                return state;
            }
            
        }
        
        /**
         * Represents the state during a non-string field value is defined.
         */
        private class NonStringFieldValueState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (ch == '{') {
                        state = initNewObject();
                    } else if (ch == '[') {
                        state = initNewArray();
                    } else if (ch == '}' || ch == ']') {
                        processNonStringValue(ValueType.FIELD);
                        state = finalizeObject();
                    } else if (ch == ',') {
                        processNonStringValue(ValueType.FIELD);
                        state = nonFirstFieldReadyState;
                    } else if (isWhitespace(ch)) {
                        processNonStringValue(ValueType.FIELD);
                        state = fieldEndState;
                    } else if (ch == EOF) {
                        throw new JsonParserException("unexpected end of JSON document");
                    } else {
                        append(ch);
                        state = this;
                        continue;
                    }
                    break;
                }
                index = i + 1;
                return state;
            }
            
        }
        
        /**
         * Represents the state during a non-string array element is defined.
         */
        private class NonStringArrayElementState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (ch == '{') {
                        state = initNewObject();
                    } else if (ch == '[') {
                        state = initNewArray();
                    } else if (ch == ']') {
                        processNonStringValue(ValueType.ARRAY_ELEMENT);
                        state = finalizeObject();
                    } else if (ch == ',') {
                        processNonStringValue(ValueType.ARRAY_ELEMENT);
                        state = nonFirstArrayElementReadyState;
                    } else if (isWhitespace(ch)) {
                        processNonStringValue(ValueType.ARRAY_ELEMENT);
                        state = arrayElementEndState;
                    } else if (ch == EOF) {
                        throw new JsonParserException("unexpected end of JSON document");
                    } else {
                        append(ch);
                        state = this;
                        continue;
                    }
                    break;
                }
                index = i + 1;
                return state;
            }
            
        }
        
        /**
         * Represents the state during a string value is defined.
         */
        private class StringValueState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (ch == currentQuoteChar) {
                        currentJsonNode = new JsonNode(false);
                        currentJsonNode.setString(value());
                        state = docEndState;
                    } else if (ch == REV_SOL) { 
                        state = stringValEscCharProcessingState;
                    } else if (ch == EOF) {
                        throw new JsonParserException("unexpected end of JSON document");
                    } else {
                        append(ch);
                        state = this;
                        continue;
                    } 
                    break;
                }
                index = i + 1;
                return state;
            }
            
        }
        
        private enum ValueType {
            FIELD,
            VALUE,
            ARRAY_ELEMENT
        }
        
        private void processNonStringValue(ValueType type) throws JsonParserException {
            String str = value();
            try {
                long longValue = Long.parseLong(str);
                switch (type) {
                case ARRAY_ELEMENT:
                    currentJsonNode.add(new JsonNode(longValue));
                    break;
                case FIELD:
                    currentJsonNode.set(currentJsonNode.fieldName, longValue);
                    break;
                case VALUE:
                    currentJsonNode.setNumber(longValue);
                    break;
                default:
                    break;                
                }
            } catch (NumberFormatException ignore) {
                try {
                    double doubleValue = Double.parseDouble(str);
                    switch (type) {
                    case ARRAY_ELEMENT:
                        currentJsonNode.add(new JsonNode(doubleValue));
                        break;
                    case FIELD:
                        currentJsonNode.set(currentJsonNode.fieldName, doubleValue);
                        break;
                    case VALUE:
                        currentJsonNode.setNumber(doubleValue);
                        break;
                    default:
                        break;                
                    }
                } catch (NumberFormatException ignore2) {
                    if (NULL.equals(str)) {
                        switch (type) {
                        case ARRAY_ELEMENT:
                            currentJsonNode.add(new JsonNode(Type.NULL));
                            break;
                        case FIELD:
                            currentJsonNode.set(currentJsonNode.fieldName, (String) null);
                            break;
                        case VALUE:
                            currentJsonNode.setNull();
                            break;
                        default:
                            break;                
                        }
                    } else if (TRUE.equals(str)) {
                        switch (type) {
                        case ARRAY_ELEMENT:
                            currentJsonNode.add(new JsonNode(true));
                            break;
                        case FIELD:
                            currentJsonNode.set(currentJsonNode.fieldName, true);
                            break;
                        case VALUE:
                            currentJsonNode.setBooleanValue(true);
                            break;
                        default:
                            break;                
                        }
                    } else if (FALSE.equals(str)) {
                        switch (type) {
                        case ARRAY_ELEMENT:
                            currentJsonNode.add(new JsonNode(false));
                            break;
                        case FIELD:
                            currentJsonNode.set(currentJsonNode.fieldName, false);
                            break;
                        case VALUE:
                            currentJsonNode.setBooleanValue(false);
                            break;
                        default:
                            break;                
                        }
                    } else {
                        throw new JsonParserException("unrecognized token '" + str + "'");
                    }
                }
            }
        }
        
        /**
         * Represents the state during a non-string value is defined.
         */
        private class NonStringValueState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (isWhitespace(ch) || ch == EOF) {
                        currentJsonNode = new JsonNode(false);
                        processNonStringValue(ValueType.VALUE);
                        state = docEndState;
                    } else {
                        append(ch);
                        state = this;
                        continue;
                    }
                    break;
                }
                index = i + 1;
                return state;
            }
            
        }
        
        /**
         * Represents the state where an object field has ended.
         */
        private class FieldEndState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == ',') {
                        state = nonFirstFieldReadyState;
                    } else if (ch == '}') {
                        state = finalizeObject();
                    } else {
                        throwExpected(",", "}");
                    }
                    break;
                }
                index = i + 1;
                return state;
            }
            
        }
        
        /**
         * Represents the state where an array element has ended.
         */
        private class ArrayElementEndState implements State {

            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if (isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == ',') {
                        state = nonFirstArrayElementReadyState;
                    } else if (ch == ']') {
                        state = finalizeObject();
                    } else {
                        throwExpected(",", "]");
                    }
                    break;
                }
                index = i + 1;
                return state;
            }
            
        }
        
        /**
         * Represents the state where an escaped unicode character in hex format is processed
         * from a object string field.
         */
        private class StringFieldUnicodeHexProcessingState extends UnicodeHexProcessingState {

            @Override
            protected State getSourceState() {
                return stringFieldValueState;
            }
            
        }
        
        /**
         * Represents the state where an escaped unicode character in hex format is processed
         * from an array string field.
         */
        private class StringAEProcessingState extends UnicodeHexProcessingState {

            @Override
            protected State getSourceState() {
                return stringArrayElementState;
            }
            
        }
        
        /**
         * Represents the state where an escaped unicode character in hex format is processed
         * from a string value.
         */
        private class StringValueUnicodeHexProcessingState extends UnicodeHexProcessingState {

            @Override
            protected State getSourceState() {
                return stringValueState;
            }
            
        }
        
        /**
         * Represents the state where an escaped unicode character in hex format is processed
         * from a field name.
         */
        private class FieldNameUnicodeHexProcessingState extends UnicodeHexProcessingState {

            @Override
            protected State getSourceState() {
                return fieldNameState;
            }
            
        }
        
        /**
         * Represents the state where an escaped unicode character in hex format is processed.
         */
        private abstract class UnicodeHexProcessingState implements State {

            private StringBuilder hexBuilder = new StringBuilder(4);
            
            protected abstract State getSourceState();
                        
            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    processLocation(ch);
                    if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F')) {
                        this.hexBuilder.append(ch);
                        if (this.hexBuilder.length() >= 4) {
                            append(this.extractUnicodeChar());
                            this.reset();
                            state = this.getSourceState();
                            break;
                        } else {
                            state = this;
                            continue;
                        }
                    } else {
                        this.reset();
                        throwExpected("hexadecimal value of an unicode character");
                    }
                    break;
                }
                index = i + 1;
                return state;
            }
            
            private void reset() {
                this.hexBuilder.setLength(0);
            }
            
            private char extractUnicodeChar() {
                return StringEscapeUtils.unescapeJava("\\u" + this.hexBuilder.toString()).charAt(0);
            }
            
        }
        
        /**
         * Represents the state where an escaped character is processed in a object string field.
         */
        private class StringFieldEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

            @Override
            protected State getSourceState() {
                return stringFieldValueState;
            }
            
        }
        
        /**
         * Represents the state where an escaped character is processed in an array string field.
         */
        private class StringAEEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

            @Override
            protected State getSourceState() {
                return stringArrayElementState;
            }
            
        }
        
        /**
         * Represents the state where an escaped character is processed in a string value.
         */
        private class StringValueEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

            @Override
            protected State getSourceState() {
                return stringValueState;
            }
            
        }
        
        /**
         * Represents the state where an escaped character is processed in a field name.
         */
        private class FieldNameEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

            @Override
            protected State getSourceState() {
                return fieldNameState;
            }
            
        }
        
        /**
         * Represents the state where an escaped character is processed.
         */
        private abstract class EscapedCharacterProcessingState implements State {

            protected abstract State getSourceState();
            
            @Override
            public State transition(char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                if (i < count) {
                    ch = buff[i];
                    processLocation(ch);
                    switch (ch) {
                    case '"':
                        append(QUOTES);
                        state = this.getSourceState();
                        break;
                    case '\\':
                        append(REV_SOL);
                        state = this.getSourceState();
                        break;
                    case '/':
                        append(SOL);
                        state = this.getSourceState();
                        break;
                    case 'b':
                        append(BACKSPACE);
                        state = this.getSourceState();
                        break;
                    case 'f':
                        append(FORMFEED);
                        state = this.getSourceState();
                        break;
                    case 'n':
                        append(NEWLINE);
                        state = this.getSourceState();
                        break;
                    case 'r':
                        append(CR);
                        state = this.getSourceState();
                        break;
                    case 't':
                        append(HZ_TAB);
                        state = this.getSourceState();
                        break;
                    case 'u':
                        if (this.getSourceState() == stringFieldValueState) {
                            state = stringFieldUnicodeHexProcessingState;
                        } else if (this.getSourceState() == stringValueState) {
                            state = stringValueUnicodeHexProcessingState;
                        } else if (this.getSourceState() == fieldNameState) {
                            state = fieldNameUnicodeHexProcessingState;
                        } else if (this.getSourceState() == stringArrayElementState) {
                            state = stringAEProcessingState;
                        } else {
                            throw new JsonParserException("unknown source '" + this.getSourceState() + 
                                    "' in escape char processing state");
                        }
                        break;
                    default:
                         throwExpected("escaped characters");
                         break;
                    }
                }
                index = i + 1;
                return state;
            }
            
        }
        
    }
    
}
