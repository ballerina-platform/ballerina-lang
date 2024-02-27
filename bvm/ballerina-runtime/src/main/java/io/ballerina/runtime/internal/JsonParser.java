/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.JsonUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.MapValueImpl;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import static io.ballerina.runtime.api.utils.JsonUtils.NonStringValueProcessingMode.FROM_JSON_DECIMAL_STRING;
import static io.ballerina.runtime.api.utils.JsonUtils.NonStringValueProcessingMode.FROM_JSON_FLOAT_STRING;

/**
 * This class represents a JSON parser.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class JsonParser {

    private JsonParser() {
    }

    private static final ThreadLocal<StateMachine> tlStateMachine = ThreadLocal.withInitial(JsonStateMachine::new);

    /**
     * Parses the contents in the given {@link InputStream} and returns a json.
     *
     * @param in input stream which contains the JSON content
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(InputStream in) throws BError {
        return StreamParser.parse(in, PredefinedTypes.TYPE_JSON);
    }

    /**
     * Parses the contents in the given {@link InputStream} and returns a json.
     *
     * @param in          input stream which contains the JSON content
     * @param charsetName the character set name of the input stream
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(InputStream in, String charsetName) throws BError {
        return StreamParser.parse(in, charsetName, PredefinedTypes.TYPE_JSON);
    }

    /**
     * Parses the contents in the given string and returns a json.
     *
     * @param jsonStr the string which contains the JSON content
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(String jsonStr) throws BError {
        return StreamParser.parse(jsonStr, PredefinedTypes.TYPE_JSON);
    }

    /**
     * Parses the contents in the given string and returns a json.
     *
     * @param jsonStr the string which contains the JSON content
     * @param mode    the mode to use when processing numeric values
     * @return JSON   value if parsing is successful
     * @throws BError for any parsing error
     */
    public static Object parse(String jsonStr, JsonUtils.NonStringValueProcessingMode mode) throws BError {
        return parse(new StringReader(jsonStr), mode);
    }

    /**
     * Parses the contents in the given {@link Reader} and returns a json.
     *
     * @param reader reader which contains the JSON content
     * @param mode   the mode to use when processing numeric values
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(Reader reader, JsonUtils.NonStringValueProcessingMode mode) throws BError {
        JsonStateMachine sm = (JsonStateMachine) tlStateMachine.get();
        try {
            sm.setMode(mode);
            return sm.execute(reader);
        } finally {
            // Need to reset the state machine before leaving. Otherwise, references to the created
            // JSON values will be maintained and the java GC will not happen properly.
            sm.reset();
        }
    }

    /**
     * Represents the state machine used for JSON parsing.
     */
    private static class JsonStateMachine extends StateMachine {
        private JsonUtils.NonStringValueProcessingMode mode = JsonUtils.NonStringValueProcessingMode.FROM_JSON_STRING;
        private Type definedJsonType = PredefinedTypes.TYPE_JSON;

        JsonStateMachine() {
            super("JSON document", new FieldNameState(), new StringValueState(), new StringFieldValueState(),
                    new StringArrayElementState());
            reset();
        }

        @Override
        public void reset() {
            super.reset();
            this.setMode(JsonUtils.NonStringValueProcessingMode.FROM_JSON_STRING);
        }

        private void setMode(JsonUtils.NonStringValueProcessingMode mode) {
            this.mode = mode;
            if (this.mode == FROM_JSON_DECIMAL_STRING) {
                definedJsonType = PredefinedTypes.TYPE_JSON_DECIMAL;
            } else if (this.mode == FROM_JSON_FLOAT_STRING) {
                definedJsonType = PredefinedTypes.TYPE_JSON_FLOAT;
            } else {
                definedJsonType = PredefinedTypes.TYPE_JSON;
            }
        }

        private static Object changeForBString(Object jsonObj) {
            if (jsonObj instanceof String) {
                return StringUtils.fromString((String) jsonObj);
            }
            return jsonObj;
        }

        protected State finalizeObject() {
            if (this.nodesStack.isEmpty()) {
                return DOC_END_STATE;
            }

            Object parentNode = this.nodesStack.pop();
            if (TypeUtils.getImpliedType(TypeChecker.getType(parentNode)).getTag() == TypeTags.MAP_TAG) {
                ((MapValueImpl<BString, Object>) parentNode).put(StringUtils.fromString(fieldNames.pop()),
                                                                 currentJsonNode);
                currentJsonNode = parentNode;
                return FIELD_END_STATE;
            }
            ((ArrayValue) parentNode).append(changeForBString(currentJsonNode));
            currentJsonNode = parentNode;
            return ARRAY_ELEMENT_END_STATE;
        }

        protected State initNewObject() {
            if (currentJsonNode != null) {
                this.nodesStack.push(currentJsonNode);
            }
            currentJsonNode = new MapValueImpl<>(new BMapType(definedJsonType));
            return FIRST_FIELD_READY_STATE;
        }

        protected State initNewArray() {
            if (currentJsonNode != null) {
                this.nodesStack.push(currentJsonNode);
            }
            currentJsonNode = new ArrayValueImpl(new BArrayType(definedJsonType));
            return FIRST_ARRAY_ELEMENT_READY_STATE;
        }

        /**
         * Represents the state during a field name.
         */
        private static class FieldNameState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
                char ch;
                State state = null;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == sm.currentQuoteChar) {
                        sm.processFieldName();
                        state = END_FIELD_NAME_STATE;
                    } else if (ch == REV_SOL) {
                        state = FIELD_NAME_ESC_CHAR_PROCESSING_STATE;
                    } else if (ch == EOF) {
                        throw new ParserException("unexpected end of JSON document");
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
         * Represents the state during a string field value is defined.
         */
        private static class StringFieldValueState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == sm.currentQuoteChar) {
                        ((MapValueImpl<BString, Object>) sm.currentJsonNode).put(
                                StringUtils.fromString(sm.fieldNames.pop()), StringUtils.fromString(sm.value()));
                        state = FIELD_END_STATE;
                    } else if (ch == REV_SOL) {
                        state = STRING_FIELD_ESC_CHAR_PROCESSING_STATE;
                    } else if (ch == EOF) {
                        throw new ParserException("unexpected end of JSON document");
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
         * Represents the state during a string array element is defined.
         */
        private static class StringArrayElementState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == sm.currentQuoteChar) {
                        ((ArrayValue) sm.currentJsonNode).append(changeForBString(sm.value()));
                        state = ARRAY_ELEMENT_END_STATE;
                    } else if (ch == REV_SOL) {
                        state = STRING_AE_ESC_CHAR_PROCESSING_STATE;
                    } else if (ch == EOF) {
                        throw new ParserException("unexpected end of JSON document");
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
         * Represents the state during a string value is defined.
         */
        private static class StringValueState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws ParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == sm.currentQuoteChar) {
                        sm.currentJsonNode = changeForBString(sm.value());
                        state = DOC_END_STATE;
                    } else if (ch == REV_SOL) {
                        state = STRING_VAL_ESC_CHAR_PROCESSING_STATE;
                    } else if (ch == EOF) {
                        throw new ParserException("unexpected end of JSON document");
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

        protected void processNonStringValue(ValueType type) throws ParserException {
            String str = value();
            if (str.indexOf('.') >= 0) {
                try {
                    switch (mode) {
                        case FROM_JSON_FLOAT_STRING:
                            setValueToJsonType(type, Double.parseDouble(str));
                            break;
                        case FROM_JSON_DECIMAL_STRING:
                            setValueToJsonType(type, new DecimalValue(str));
                            break;
                        default:
                            if (isNegativeZero(str)) {
                                setValueToJsonType(type, Double.parseDouble(str));
                            } else {
                                setValueToJsonType(type, new DecimalValue(str));
                            }
                            break;
                    }
                } catch (NumberFormatException ignore) {
                    throw new ParserException("unrecognized token '" + str + "'");
                }
            } else {
                char ch = str.charAt(0);
                if (ch == 't' && TRUE.equals(str)) {
                    switch (type) {
                        case ARRAY_ELEMENT:
                            ((ArrayValue) this.currentJsonNode).append(Boolean.TRUE);
                            break;
                        case FIELD:
                            ((MapValueImpl<BString, Object>) this.currentJsonNode).put(
                                    StringUtils.fromString(this.fieldNames.pop()), Boolean.TRUE);
                            break;
                        case VALUE:
                            currentJsonNode = Boolean.TRUE;
                            break;
                        default:
                            break;
                    }
                } else if (ch == 'f' && FALSE.equals(str)) {
                    switch (type) {
                        case ARRAY_ELEMENT:
                            ((ArrayValue) this.currentJsonNode).append(Boolean.FALSE);
                            break;
                        case FIELD:
                            ((MapValueImpl<BString, Object>) this.currentJsonNode).put(
                                    StringUtils.fromString(this.fieldNames.pop()), Boolean.FALSE);
                            break;
                        case VALUE:
                            currentJsonNode = Boolean.FALSE;
                            break;
                        default:
                            break;
                    }
                } else if (ch == 'n' && NULL.equals(str)) {
                    switch (type) {
                        case ARRAY_ELEMENT:
                            ((ArrayValue) this.currentJsonNode).append(null);
                            break;
                        case FIELD:
                            ((MapValueImpl<BString, Object>) this.currentJsonNode).put(
                                    StringUtils.fromString(this.fieldNames.pop()), null);
                            break;
                        case VALUE:
                            currentJsonNode = null;
                            break;
                        default:
                            break;
                    }
                } else {
                    try {
                        switch (mode) {
                            case FROM_JSON_FLOAT_STRING:
                                setValueToJsonType(type, Double.parseDouble(str));
                                break;
                            case FROM_JSON_DECIMAL_STRING:
                                setValueToJsonType(type, new DecimalValue(str));
                                break;
                            default:
                                if (isNegativeZero(str)) {
                                    setValueToJsonType(type, Double.parseDouble(str));
                                } else if (isExponential(str)) {
                                    setValueToJsonType(type, new DecimalValue(str));
                                } else {
                                    setValueToJsonType(type, Long.parseLong(str));
                                }
                                break;
                        }
                    } catch (NumberFormatException ignore) {
                        throw new ParserException("unrecognized token '" + str + "'");
                    }
                }
            }
        }

        protected void setValueToJsonType(ValueType type, Object value) {
            switch (type) {
                case ARRAY_ELEMENT:
                    ((ArrayValue) this.currentJsonNode).append(value);
                    break;
                case FIELD:
                    ((MapValueImpl<BString, Object>) this.currentJsonNode).put(
                            StringUtils.fromString(this.fieldNames.pop()), value);
                    break;
                default:
                    currentJsonNode = value;
                    break;
            }
        }
    }
}
