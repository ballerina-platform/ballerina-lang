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

package io.ballerina.runtime.internal.util;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BListInitialValueEntry;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.BalStringUtils;
import io.ballerina.runtime.internal.CloneUtils;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.TypeConverter;
import io.ballerina.runtime.internal.ValueConverter;
import io.ballerina.runtime.internal.ValueUtils;
import io.ballerina.runtime.internal.commons.TypeValuePair;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.errors.ErrorReasons;
import io.ballerina.runtime.internal.regexp.RegExpFactory;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;
import io.ballerina.runtime.internal.values.TableValueImpl;
import io.ballerina.runtime.internal.values.TupleValueImpl;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.runtime.api.creators.ErrorCreator.createError;
import static io.ballerina.runtime.internal.ErrorUtils.createConversionError;
import static io.ballerina.runtime.internal.ValueUtils.createRecordValueWithDefaultValues;

/**
 * This class represents a {@link InputStream} parser which creates a value of the given target type
 * which should be a subtype of {@link io.ballerina.runtime.api.types.AnydataType} type. The {@link InputStream} should
 * only contain a sequence of characters that can be parsed as {@link io.ballerina.runtime.api.types.JsonType},
 * otherwise a {@link BError} is thrown.
 *
 * @since 2201.9.0
 */
public class StreamParser {

    private static final ThreadLocal<StateMachine> tlStateMachine = ThreadLocal.withInitial(StateMachine::new);

    private StreamParser() {
    }

    /**
     * Parses the contents in the given {@link InputStream} and returns a value of the given target type.
     *
     * @param in input stream which contains the content
     * @return value of the given target type
     * @throws BError for any parsing error
     */
    public static Object parse(InputStream in, Type targetType) throws BError {
        return parse(in, Charset.defaultCharset().name(), targetType);
    }

    /**
     * Parses the contents in the given {@link InputStream} and returns a value of the given target type.
     *
     * @param in          input stream which contains the content
     * @param charsetName the character set name of the input stream
     * @return value of the given target typezzz
     * @throws BError for any parsing error
     */
    public static Object parse(InputStream in, String charsetName, Type targetType) throws BError {
        try {
            return parse(new InputStreamReader(new BufferedInputStream(in), charsetName), targetType);
        } catch (IOException e) {
            throw ErrorCreator
                    .createError(StringUtils.fromString(("error in parsing input stream: " + e.getMessage())));
        }
    }

    /**
     * Parses the contents in the given string and returns a value of the given target type.
     *
     * @param str the string which contains the content
     * @return value of the given target type
     * @throws BError for any parsing error
     */
    public static Object parse(String str, Type targetType) throws BError {
        return parse(new StringReader(str), targetType);
    }

    /**
     * Parses the contents in the given {@link Reader} and a value of the given target type.
     *
     * @param reader reader which contains the content
     * @return value of the given target type
     * @throws BError for any parsing error
     */
    private static Object parse(Reader reader, Type targetType) throws BError {
        StateMachine sm = tlStateMachine.get();
        try {
            sm.addTargetType(targetType);
            return sm.execute(reader);
        } finally {
            // Need to reset the state machine before leaving. Otherwise, references to the created
            // values will be maintained and the java GC will not happen properly.
            sm.reset();
        }
    }

    /**
     * Represents a stream parser related exception.
     */
    private static class StreamParserException extends Exception {

        public StreamParserException(String msg) {
            super(msg);
        }

    }

    /**
     * Represents the state machine used for input stream parsing.
     */
    private static class StateMachine {

        private static final char CR = 0x000D;
        private static final char NEWLINE = 0x000A;
        private static final char HZ_TAB = 0x0009;
        private static final char SPACE = 0x0020;
        private static final char BACKSPACE = 0x0008;
        private static final char FORMFEED = 0x000C;
        private static final char QUOTES = '"';
        private static final char REV_SOL = '\\';
        private static final char SOL = '/';
        private static final char EOF = (char) -1;
        private static final String NULL = "null";
        private static final String TRUE = "true";
        private static final String FALSE = "false";

        private static final State DOC_START_STATE = new DocumentStartState();
        private static final State DOC_END_STATE = new DocumentEndState();
        private static final State FIRST_FIELD_READY_STATE = new FirstFieldReadyState();
        private static final State NON_FIRST_FIELD_READY_STATE = new NonFirstFieldReadyState();
        private static final State FIELD_NAME_STATE = new FieldNameState();
        private static final State END_FIELD_NAME_STATE = new EndFieldNameState();
        private static final State FIELD_VALUE_READY_STATE = new FieldValueReadyState();
        private static final State STRING_FIELD_VALUE_STATE = new StringFieldValueState();
        private static final State NON_STRING_FIELD_VALUE_STATE = new NonStringFieldValueState();
        private static final State NON_STRING_VALUE_STATE = new NonStringValueState();
        private static final State STRING_VALUE_STATE = new StringValueState();
        private static final State FIELD_END_STATE = new FieldEndState();
        private static final State STRING_AE_ESC_CHAR_PROCESSING_STATE = new StringAEEscapedCharacterProcessingState();
        private static final State STRING_AE_PROCESSING_STATE = new StringAEProcessingState();
        private static final State FIELD_NAME_UNICODE_HEX_PROCESSING_STATE = new FieldNameUnicodeHexProcessingState();
        private static final State FIRST_ARRAY_ELEMENT_READY_STATE = new FirstArrayElementReadyState();
        private static final State NON_FIRST_ARRAY_ELEMENT_READY_STATE = new NonFirstArrayElementReadyState();
        private static final State STRING_ARRAY_ELEMENT_STATE = new StringArrayElementState();
        private static final State NON_STRING_ARRAY_ELEMENT_STATE = new NonStringArrayElementState();
        private static final State ARRAY_ELEMENT_END_STATE = new ArrayElementEndState();
        private static final State STRING_FIELD_ESC_CHAR_PROCESSING_STATE =
                new StringFieldEscapedCharacterProcessingState();
        private static final State STRING_VAL_ESC_CHAR_PROCESSING_STATE =
                new StringValueEscapedCharacterProcessingState();
        private static final State FIELD_NAME_ESC_CHAR_PROCESSING_STATE =
                new FieldNameEscapedCharacterProcessingState();
        private static final State STRING_FIELD_UNICODE_HEX_PROCESSING_STATE =
                new StringFieldUnicodeHexProcessingState();
        private static final State STRING_VALUE_UNICODE_HEX_PROCESSING_STATE =
                new StringValueUnicodeHexProcessingState();

        // targetTypes list will always have effective referred types because we add only the implied types
        // if the target type is union we put the union type inside targetTypes list and do not add more types,
        // and we create a json value and convert to the target type
        // json, finite, anydata types will be handled the same way as union types, but they do not need conversion
        List<Type> targetTypes = new ArrayList<>();
        List<Integer> listIndices = new ArrayList<>(); // we keep only the current indices of arrays and tuples
        private int nodesStackSizeWhenUnionStarts = -1; // when we come across a union target type we set this value

        private Object currentJsonNode = null;
        private final Deque<Object> nodesStack = new ArrayDeque<>();
        private final Deque<String> fieldNames = new ArrayDeque<>();

        private final StringBuilder hexBuilder = new StringBuilder(4);
        private char[] charBuff = new char[1024];
        private int charBuffIndex;

        private int index = 0;
        private int line = 1;
        private int column = 0;
        private char currentQuoteChar;

        StateMachine() {
        }

        public void reset() {
            this.index = 0;
            this.currentJsonNode = null;
            this.line = 1;
            this.column = 0;
            this.nodesStack.clear();
            this.fieldNames.clear();
            this.targetTypes.clear();
            this.nodesStackSizeWhenUnionStarts = -1;
            this.listIndices.clear();
        }

        private void addTargetType(Type type) {
            this.targetTypes.add(TypeUtils.getImpliedType(type));
        }

        private static boolean isWhitespace(char ch) {
            return ch == SPACE || ch == HZ_TAB || ch == NEWLINE || ch == CR;
        }

        private static StreamParserException getConversionError(Type targetType, String inputValue) {
            return new StreamParserException("value '" + inputValue + "' cannot be converted to '" + targetType + "'");
        }

        private static Object convertValues(Type targetType, String inputValue) throws StreamParserException {
            switch (targetType.getTag()) {
                case TypeTags.INT_TAG:
                case TypeTags.SIGNED32_INT_TAG:
                case TypeTags.SIGNED16_INT_TAG:
                case TypeTags.SIGNED8_INT_TAG:
                case TypeTags.UNSIGNED32_INT_TAG:
                case TypeTags.UNSIGNED16_INT_TAG:
                case TypeTags.UNSIGNED8_INT_TAG:
                    try {
                        return Long.parseLong(inputValue);
                    } catch (NumberFormatException e) {
                        throw getConversionError(targetType, inputValue);
                    }
                case TypeTags.DECIMAL_TAG:
                    try {
                        return new DecimalValue(inputValue);
                    } catch (NumberFormatException e) {
                        throw getConversionError(targetType, inputValue);
                    }
                case TypeTags.FLOAT_TAG:
                    try {
                        return Double.parseDouble(inputValue);
                    } catch (NumberFormatException e) {
                        throw getConversionError(targetType, inputValue);
                    }
                case TypeTags.BOOLEAN_TAG:
                    char ch = inputValue.charAt(0);
                    if (ch == 't' && StateMachine.TRUE.equals(inputValue)) {
                        return Boolean.TRUE;
                    } else if (ch == 'f' && StateMachine.FALSE.equals(inputValue)) {
                        return Boolean.FALSE;
                    } else {
                        throw getConversionError(targetType, inputValue);
                    }
                case TypeTags.NULL_TAG:
                    if (inputValue.charAt(0) == 'n' && StateMachine.NULL.equals(inputValue)) {
                        return null;
                    } else {
                        throw getConversionError(targetType, inputValue);
                    }
                case TypeTags.BYTE_TAG:
                    try {
                        return Integer.parseInt(inputValue);
                    } catch (NumberFormatException e) {
                        throw getConversionError(targetType, inputValue);
                    }
                case TypeTags.UNION_TAG, TypeTags.FINITE_TYPE_TAG:
                    Object jsonVal = getNonStringValueAsJson(inputValue);
                    return convert(jsonVal, targetType);
                case TypeTags.JSON_TAG, TypeTags.ANYDATA_TAG:
                    return  getNonStringValueAsJson(inputValue);
                default:
                    // case TypeTags.STRING_TAG cannot come inside this method, an error needs to be thrown.
                    throw getConversionError(targetType, inputValue);
            }
        }

        private static void throwExpected(String... chars) throws StreamParserException {
            throw new StreamParserException("expected '" + String.join("' or '", chars) + "'");
        }

        private void processLocation(char ch) {
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
                currentState = currentState.transition(this, new char[] { EOF }, 0, 1);
                if (currentState != DOC_END_STATE) {
                    throw ErrorCreator.createError(StringUtils.fromString("invalid content"));
                }
                return this.currentJsonNode;
            } catch (IOException e) {
                throw ErrorCreator.createError(StringUtils.fromString("error reading input stream: " + e.getMessage()));
            } catch (StreamParserException e) {
                throw ErrorCreator.createError(StringUtils.fromString(e.getMessage() + " at line: " + this.line + " " +
                        "column: " + this.column));
            }
        }

        private void append(char ch) {
            try {
                this.charBuff[this.charBuffIndex] = ch;
                this.charBuffIndex++;
            } catch (ArrayIndexOutOfBoundsException e) {
                // this approach is faster than checking for the size
                this.growCharBuff();
                this.charBuff[this.charBuffIndex++] = ch;
            }
        }

        private void growCharBuff() {
            char[] newBuff = new char[charBuff.length * 2];
            System.arraycopy(this.charBuff, 0, newBuff, 0, this.charBuff.length);
            this.charBuff = newBuff;
        }

        private State finalizeObject() throws StreamParserException {
            Type targetType = this.targetTypes.get(this.targetTypes.size() - 1);
            switch (targetType.getTag()) {
                case TypeTags.UNION_TAG, TypeTags.TABLE_TAG, TypeTags.FINITE_TYPE_TAG:
                    if (this.nodesStackSizeWhenUnionStarts == this.nodesStack.size()) {
                        this.targetTypes.remove(this.targetTypes.size() - 1);
                        this.currentJsonNode = convert(this.currentJsonNode, targetType);
                    }
                    break;
                case TypeTags.JSON_TAG, TypeTags.ANYDATA_TAG:
                    if (this.nodesStackSizeWhenUnionStarts == this.nodesStack.size()) {
                        this.targetTypes.remove(this.targetTypes.size() - 1);
                    }
                    break;
                case TypeTags.MAP_TAG:
                    this.targetTypes.remove(this.targetTypes.size() - 1);
                    break;
                case TypeTags.RECORD_TYPE_TAG:
                    this.targetTypes.remove(this.targetTypes.size() - 1);
                    BRecordType recordType = (BRecordType) targetType;
                    BMap<BString, Object> constructedMap = (BMap<BString, Object>) this.currentJsonNode;
                    List<String> notProvidedFields = new ArrayList<>();
                    for (Map.Entry<String, Field> stringFieldEntry : recordType.getFields().entrySet()) {
                        String fieldName = stringFieldEntry.getKey();
                        BString bFieldName = StringUtils.fromString(fieldName);
                        if (constructedMap.containsKey(bFieldName)) {
                            continue;
                        }
                        long fieldFlags = stringFieldEntry.getValue().getFlags();
                        if (SymbolFlags.isFlagOn(fieldFlags, SymbolFlags.REQUIRED)) {
                            throw new StreamParserException("missing required field '" + fieldName + "' of type '" +
                                    stringFieldEntry.getValue().getFieldType().toString() + "' in record '"
                                    + targetType + "'");
                        } else if (!SymbolFlags.isFlagOn(fieldFlags, SymbolFlags.OPTIONAL)) {
                            notProvidedFields.add(fieldName);
                        }
                    }
                    BMap<BString, Object> recordValue = createRecordValueWithDefaultValues(recordType.getPackage(),
                            recordType.getName(), notProvidedFields);
                    for (Map.Entry<BString, Object> fieldEntry : constructedMap.entrySet()) {
                        recordValue.populateInitialValue(fieldEntry.getKey(), fieldEntry.getValue());
                    }
                    if (recordType.isReadOnly()) {
                        recordValue.freezeDirect();
                    }
                    this.currentJsonNode = recordValue;
                    break;
                case TypeTags.ARRAY_TAG:
                    this.targetTypes.remove(this.targetTypes.size() - 1);
                    int listIndex = this.listIndices.remove(this.listIndices.size() - 1);
                    ArrayType arrayType = (ArrayType) targetType;
                    int targetSize = arrayType.getSize();
                    if (arrayType.getState() == ArrayType.ArrayState.CLOSED && targetSize > listIndex
                            && !arrayType.hasFillerValue()) {
                        throw new StreamParserException("missing required number of values for the '" + arrayType
                                + "' array which does not have a filler value");
                    }
                    break;
                case TypeTags.TUPLE_TAG:
                    this.targetTypes.remove(this.targetTypes.size() - 1);
                    int tupleListIndex = this.listIndices.remove(this.listIndices.size() - 1);
                    TupleType tupleType = (TupleType) targetType;
                    int targetTupleSize = tupleType.getTupleTypes().size();
                    if (targetTupleSize > tupleListIndex) {
                        throw new StreamParserException("missing required number of values for the '" + tupleType
                                + "' tuple");
                    }
                    break;
                default:
                    throw new StreamParserException("unsupported type: '" + targetType + "'");
            }

            if (this.nodesStack.isEmpty()) {
                return DOC_END_STATE;
            }
            Object parentNode = this.nodesStack.pop();

            Type parentTargetType = this.targetTypes.get(this.targetTypes.size() - 1);
            switch (parentTargetType.getTag()) {
                case TypeTags.RECORD_TYPE_TAG:
                case TypeTags.MAP_TAG:
                    ((MapValueImpl<BString, Object>) parentNode).putForcefully(StringUtils.fromString(fieldNames.pop()),
                            currentJsonNode);
                    this.currentJsonNode = parentNode;
                    return FIELD_END_STATE;
                case TypeTags.ARRAY_TAG:
                    int listIndex = this.listIndices.get(this.listIndices.size() - 1);
                    ((ArrayValueImpl) parentNode).addRefValue(listIndex, currentJsonNode);
                    this.listIndices.set(this.listIndices.size() - 1, listIndex + 1);
                    this.currentJsonNode = parentNode;
                    return ARRAY_ELEMENT_END_STATE;
                case TypeTags.TUPLE_TAG:
                    int tupleListIndex = this.listIndices.get(this.listIndices.size() - 1);
                    ((TupleValueImpl) parentNode).addRefValue(tupleListIndex, currentJsonNode);
                    this.listIndices.set(this.listIndices.size() - 1, tupleListIndex + 1);
                    this.currentJsonNode = parentNode;
                    return ARRAY_ELEMENT_END_STATE;
                case TypeTags.UNION_TAG, TypeTags.JSON_TAG, TypeTags.ANYDATA_TAG, TypeTags.TABLE_TAG,
                        TypeTags.FINITE_TYPE_TAG:
                    if (TypeUtils.getImpliedType(TypeChecker.getType(parentNode)).getTag() == TypeTags.MAP_TAG) {
                        ((MapValueImpl<BString, Object>) parentNode).putForcefully(
                                StringUtils.fromString(fieldNames.pop()), currentJsonNode);
                        this.currentJsonNode = parentNode;
                        return FIELD_END_STATE;
                    }
                    ArrayValueImpl arrayValue = (ArrayValueImpl) parentNode;
                    arrayValue.addRefValueForcefully(arrayValue.size(), this.currentJsonNode);
                    this.currentJsonNode = parentNode;
                    return ARRAY_ELEMENT_END_STATE;
                default:
                    throw new StreamParserException("unsupported type: '" + parentTargetType + "'");
            }
        }

        private State initNewObject() throws StreamParserException {
            if (this.currentJsonNode != null) {
                this.nodesStack.push(currentJsonNode);
                Type lastTargetType = this.targetTypes.get(this.targetTypes.size() - 1);
                switch (lastTargetType.getTag()) {
                    case TypeTags.ARRAY_TAG:
                        int listIndex = this.listIndices.get(this.listIndices.size() - 1);
                        ArrayType arrayType = (ArrayType) lastTargetType;
                        int targetSize = arrayType.getSize();
                        if (arrayType.getState() == ArrayType.ArrayState.CLOSED && targetSize <= listIndex) {
                            throw new StreamParserException("'" + arrayType
                                    + "' array size is not enough for the provided values");
                        }
                        Type elementType = TypeUtils.getImpliedType(arrayType.getElementType());
                        this.addTargetType(elementType);
                        break;
                    case TypeTags.TUPLE_TAG:
                        int tupleListIndex = this.listIndices.get(this.listIndices.size() - 1);
                        TupleType tupleType = (TupleType) lastTargetType;
                        List<Type> tupleTypes = tupleType.getTupleTypes();
                        int targetTupleSize = tupleTypes.size();
                        Type tupleRestType = tupleType.getRestType();
                        boolean noRestType = tupleRestType == null;
                        Type tupleElementType;
                        if (targetTupleSize <= tupleListIndex) {
                            if (noRestType) {
                                throw new StreamParserException("'" + tupleType
                                        + "' tuple size is not enough for the provided values");
                            } else {
                                tupleElementType = TypeUtils.getImpliedType(tupleRestType);
                            }
                        } else {
                            tupleElementType = TypeUtils.getImpliedType(tupleTypes.get(tupleListIndex));
                        }
                        this.addTargetType(tupleElementType);
                        break;
                    case TypeTags.MAP_TAG:
                        this.addTargetType(((MapType) lastTargetType).getConstrainedType());
                        break;
                    case TypeTags.RECORD_TYPE_TAG:
                        BRecordType recordType = (BRecordType) lastTargetType;
                        String fieldName = this.fieldNames.getFirst();
                        Map<String, Field> fields = recordType.getFields();
                        Field field = fields.get(fieldName);
                        if (field == null) {
                            this.addTargetType(recordType.restFieldType);
                        } else {
                            this.addTargetType(field.getFieldType());
                        }
                        break;
                    case TypeTags.UNION_TAG, TypeTags.JSON_TAG, TypeTags.ANYDATA_TAG, TypeTags.TABLE_TAG,
                            TypeTags.FINITE_TYPE_TAG:
                        break;
                    default:
                        throw new StreamParserException("unsupported type: " + lastTargetType + "'");
                }
            }
            Type targetType = this.targetTypes.get(this.targetTypes.size() - 1);
            int targetTypeTag = targetType.getTag();
            switch (targetTypeTag) {
                case TypeTags.MAP_TAG:
                case TypeTags.RECORD_TYPE_TAG:
                    this.currentJsonNode = new MapValueImpl<>(targetType);
                    break;
                case TypeTags.UNION_TAG, TypeTags.JSON_TAG, TypeTags.ANYDATA_TAG, TypeTags.TABLE_TAG,
                        TypeTags.FINITE_TYPE_TAG:
                    if (targetType.isReadOnly()
                            && (targetTypeTag == TypeTags.JSON_TAG || targetTypeTag == TypeTags.ANYDATA_TAG)) {
                        this.currentJsonNode = new MapValueImpl<>(
                                new BMapType(PredefinedTypes.TYPE_READONLY_JSON, true));
                    } else {
                        this.currentJsonNode = new MapValueImpl<>(new BMapType(PredefinedTypes.TYPE_JSON));
                    }
                    if (this.nodesStackSizeWhenUnionStarts == -1) {
                        this.nodesStackSizeWhenUnionStarts = this.nodesStack.size();
                    }
                    break;
                default:
                    throw new StreamParserException("unsupported type: " + targetType + "'");

            }
            return FIRST_FIELD_READY_STATE;
        }

        private State initNewArray() throws StreamParserException {
            if (this.currentJsonNode != null) {
                this.nodesStack.push(currentJsonNode);
                Type lastTargetType = this.targetTypes.get(this.targetTypes.size() - 1);
                switch (lastTargetType.getTag()) {
                    case TypeTags.ARRAY_TAG:
                        int listIndex = this.listIndices.get(this.listIndices.size() - 1);
                        ArrayType arrayType = (ArrayType) lastTargetType;
                        int targetSize = arrayType.getSize();
                        if (arrayType.getState() == ArrayType.ArrayState.CLOSED && targetSize <= listIndex) {
                            throw new StreamParserException("'" + arrayType
                                    + "' array size is not enough for the provided values");
                        }
                        Type elementType = TypeUtils.getImpliedType(arrayType.getElementType());
                        this.addTargetType(elementType);
                        break;
                    case TypeTags.TUPLE_TAG:
                        int tupleListIndex = this.listIndices.get(this.listIndices.size() - 1);
                        TupleType tupleType = (TupleType) lastTargetType;
                        List<Type> tupleTypes = tupleType.getTupleTypes();
                        int targetTupleSize = tupleTypes.size();
                        Type tupleRestType = tupleType.getRestType();
                        boolean noRestType = tupleRestType == null;
                        Type tupleElementType;
                        if (targetTupleSize <= tupleListIndex) {
                            if (noRestType) {
                                throw new StreamParserException("'" + tupleType
                                        + "' tuple size is not enough for the provided values");
                            } else {
                                tupleElementType = TypeUtils.getImpliedType(tupleRestType);
                            }
                        } else {
                            tupleElementType = TypeUtils.getImpliedType(tupleTypes.get(tupleListIndex));
                        }
                        this.addTargetType(tupleElementType);
                        break;
                    case TypeTags.MAP_TAG:
                        this.addTargetType(((MapType) lastTargetType).getConstrainedType());
                        break;
                    case TypeTags.RECORD_TYPE_TAG:
                        BRecordType recordType = (BRecordType) lastTargetType;
                        String fieldName = this.fieldNames.getFirst();
                        Map<String, Field> fields = recordType.getFields();
                        Field field = fields.get(fieldName);
                        if (field == null) {
                            this.addTargetType(recordType.restFieldType);
                        } else {
                            this.addTargetType(field.getFieldType());
                        }
                        break;
                    case TypeTags.UNION_TAG, TypeTags.JSON_TAG, TypeTags.ANYDATA_TAG, TypeTags.TABLE_TAG,
                            TypeTags.FINITE_TYPE_TAG:
                        break;
                    default:
                        throw new StreamParserException("unsupported type: " + lastTargetType + "'");
                }
            }
            Type targetType = this.targetTypes.get(this.targetTypes.size() - 1);
            int targetTypeTag = targetType.getTag();
            switch (targetTypeTag) {
                case TypeTags.ARRAY_TAG:
                    this.currentJsonNode = new ArrayValueImpl((ArrayType) targetType);
                    this.listIndices.add(0);
                    break;
                case TypeTags.TUPLE_TAG:
                    this.currentJsonNode = new TupleValueImpl((TupleType) targetType);
                    this.listIndices.add(0);
                    break;
                case TypeTags.UNION_TAG, TypeTags.JSON_TAG, TypeTags.ANYDATA_TAG, TypeTags.TABLE_TAG,
                        TypeTags.FINITE_TYPE_TAG:
                    if (targetType.isReadOnly()
                            && (targetTypeTag == TypeTags.JSON_TAG || targetTypeTag == TypeTags.ANYDATA_TAG)) {
                        this.currentJsonNode = new ArrayValueImpl(
                                new BArrayType(PredefinedTypes.TYPE_READONLY_JSON, true));
                    } else {
                        this.currentJsonNode = new ArrayValueImpl(new BArrayType(PredefinedTypes.TYPE_JSON));
                    }
                    if (this.nodesStackSizeWhenUnionStarts == -1) {
                        this.nodesStackSizeWhenUnionStarts = this.nodesStack.size();
                    }
                    break;
                default:
                    throw new StreamParserException("target type is not array type");

            }
            return FIRST_ARRAY_ELEMENT_READY_STATE;
        }

        /**
         * A specific state in the input stream parsing state machine.
         */
        private interface State {

            /**
             * Input given to the current state for a transition.
             *
             * @param sm the state machine
             * @param buff the input characters for the current state
             * @param i the location from the character should be read from
             * @param count the number of characters to read from the buffer
             * @return the new resulting state
             */
            State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException;

        }

        /**
         * Represents the input stream start state.
         */
        private static class DocumentStartState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
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
                        state = STRING_VALUE_STATE;
                    } else if (ch == EOF) {
                        throw new StreamParserException("empty JSON document");
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
         * Represents the input stream end state.
         */
        private static class DocumentEndState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
                char ch;
                State state = null;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (StateMachine.isWhitespace(ch) || ch == EOF) {
                        state = this;
                        continue;
                    }
                    throw new StreamParserException("input stream has already ended");
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
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
                char ch;
                State state = null;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == QUOTES) {
                        state = FIELD_NAME_STATE;
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
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (StateMachine.isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == QUOTES) {
                        state = STRING_ARRAY_ELEMENT_STATE;
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
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == QUOTES) {
                        sm.currentQuoteChar = ch;
                        state = FIELD_NAME_STATE;
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
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (StateMachine.isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == QUOTES) {
                        state = STRING_ARRAY_ELEMENT_STATE;
                        sm.currentQuoteChar = ch;
                    } else if (ch == '{') {
                        state = sm.initNewObject();
                    } else if (ch == '[') {
                        state = sm.initNewArray();
                    } else if (ch == ']') {
                        throw new StreamParserException("expected an array element");
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

        private String value() {
            String result = new String(this.charBuff, 0, this.charBuffIndex);
            this.charBuffIndex = 0;
            return result;
        }

        private void addFieldName() {
            this.fieldNames.push(this.value());
        }

        /**
         * Represents the state during a field name.
         */
        private static class FieldNameState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
                char ch;
                State state = null;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == sm.currentQuoteChar) {
                        sm.addFieldName();
                        Type parentTargetType = sm.targetTypes.get(sm.targetTypes.size() - 1);
                        switch (parentTargetType.getTag()) {
                            case TypeTags.RECORD_TYPE_TAG:
                                BRecordType recordType = (BRecordType) parentTargetType;
                                String fieldName = sm.fieldNames.getFirst();
                                Map<String, Field> fields = recordType.getFields();
                                Field field = fields.get(fieldName);
                                if (field == null && recordType.sealed) {
                                    throw new StreamParserException("field '" + fieldName
                                            + "' cannot be added to the closed record '" + recordType + "'");
                                }
                                break;
                            default:
                                // maps can have any field name
                                // for unions, json, anydata also we do nothing
                                break;
                        }
                        state = END_FIELD_NAME_STATE;
                    } else if (ch == REV_SOL) {
                        state = FIELD_NAME_ESC_CHAR_PROCESSING_STATE;
                    } else if (ch == EOF) {
                        throw new StreamParserException("unexpected end of the input stream");
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
         * Represents the state where a field name definition has ended.
         */
        private static class EndFieldNameState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
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
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (StateMachine.isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == QUOTES) {
                        state = STRING_FIELD_VALUE_STATE;
                        sm.currentQuoteChar = ch;
                    } else if (ch == '{') {
                        state = sm.initNewObject();
                    } else if (ch == '[') {
                        state = sm.initNewArray();
                    } else if (ch == ']' || ch == '}') {
                        throw new StreamParserException("expected a field value");
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
         * Represents the state during a string field value is defined.
         */
        private static class StringFieldValueState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == sm.currentQuoteChar) {
                        Type targetType = sm.targetTypes.get(sm.targetTypes.size() - 1);
                        Object bString = StringUtils.fromString(sm.value());
                        switch (targetType.getTag()) {
                            case TypeTags.MAP_TAG:
                                try {
                                    bString = ValueConverter.getConvertedStringValue((BString) bString,
                                            ((MapType) targetType).getConstrainedType());
                                } catch (BError e) {
                                    throw new StreamParserException(e.getMessage());
                                }
                                break;
                            case TypeTags.RECORD_TYPE_TAG:
                                // in records, when processing the field name, target type is added to targetTypes list.
                                BRecordType recordType = (BRecordType) targetType;
                                String fieldName = sm.fieldNames.getFirst();
                                Map<String, Field> fields = recordType.getFields();
                                Field field = fields.get(fieldName);
                                Type fieldType = field == null ? recordType.restFieldType : field.getFieldType();
                                try {
                                    bString = ValueConverter.getConvertedStringValue((BString) bString, fieldType);
                                } catch (BError e) {
                                    throw new StreamParserException(e.getMessage());
                                }
                                break;
                            case TypeTags.JSON_TAG, TypeTags.ANYDATA_TAG, TypeTags.UNION_TAG, TypeTags.TABLE_TAG,
                                    TypeTags.FINITE_TYPE_TAG:
                                break;
                            default:
                                throw new StreamParserException("unsupported type: " + targetType + "'");
                        }
                        ((MapValueImpl<BString, Object>) sm.currentJsonNode).putForcefully(
                                StringUtils.fromString(sm.fieldNames.pop()), bString);
                        state = FIELD_END_STATE;
                    } else if (ch == REV_SOL) {
                        state = STRING_FIELD_ESC_CHAR_PROCESSING_STATE;
                    } else if (ch == EOF) {
                        throw new StreamParserException("unexpected end of the input stream");
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
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == sm.currentQuoteChar) {
                        Type targetType = sm.targetTypes.get(sm.targetTypes.size() - 1);
                        BString bString = StringUtils.fromString(sm.value());
                        switch (targetType.getTag()) {
                            case TypeTags.ARRAY_TAG:
                                int listIndex = sm.listIndices.get(sm.listIndices.size() - 1);
                                try {
                                    ((ArrayValueImpl) sm.currentJsonNode).convertStringAndAddRefValue(listIndex,
                                            bString);
                                } catch (BError e) {
                                    throw new StreamParserException(e.getMessage());
                                }
                                sm.listIndices.set(sm.listIndices.size() - 1, listIndex + 1);
                                break;
                            case TypeTags.TUPLE_TAG:
                                listIndex = sm.listIndices.get(sm.listIndices.size() - 1);
                                try {
                                    ((TupleValueImpl) sm.currentJsonNode).convertStringAndAddRefValue(listIndex,
                                            bString);
                                } catch (BError e) {
                                    throw new StreamParserException(e.getMessage());
                                }
                                sm.listIndices.set(sm.listIndices.size() - 1, listIndex + 1);
                                break;
                            case TypeTags.JSON_TAG, TypeTags.ANYDATA_TAG, TypeTags.UNION_TAG, TypeTags.FINITE_TYPE_TAG:
                                ArrayValueImpl arrayValue = (ArrayValueImpl) sm.currentJsonNode;
                                arrayValue.addRefValueForcefully(arrayValue.size(), bString);
                                break;
                            default:
                                throw new StreamParserException("unsupported type: " + targetType + "'");
                        }
                        state = ARRAY_ELEMENT_END_STATE;
                    } else if (ch == REV_SOL) {
                        state = STRING_AE_ESC_CHAR_PROCESSING_STATE;
                    } else if (ch == EOF) {
                        throw new StreamParserException("unexpected end of the input stream");
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
         * Represents the state during a non-string field value is defined.
         */
        private static class NonStringFieldValueState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
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
                        throw new StreamParserException("unexpected end of the input stream");
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
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
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
                        throw new StreamParserException("unexpected end of the input stream");
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
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == sm.currentQuoteChar) {
                        Type targetType = sm.targetTypes.get(sm.targetTypes.size() - 1);
                        BString bString = StringUtils.fromString(sm.value());
                        if (sm.nodesStackSizeWhenUnionStarts == -1) {
                            try {
                                sm.currentJsonNode = ValueConverter.getConvertedStringValue(bString, targetType);
                            } catch (BError e) {
                                throw new StreamParserException(e.getMessage());
                            }
                        } else {
                            sm.currentJsonNode = bString;
                        }
                        state = DOC_END_STATE;
                    } else if (ch == REV_SOL) {
                        state = STRING_VAL_ESC_CHAR_PROCESSING_STATE;
                    } else if (ch == EOF) {
                        throw new StreamParserException("unexpected end of the input stream");
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

        private enum ValueType {
            FIELD, VALUE, ARRAY_ELEMENT
        }

        private static Object getNonStringValueAsJson(String str) throws StreamParserException {
            if (str.indexOf('.') >= 0) {
                try {
                    if (isNegativeZero(str)) {
                        return Double.parseDouble(str);
                    } else {
                        return new DecimalValue(str);
                    }
                } catch (NumberFormatException ignore) {
                    throw new StreamParserException("unrecognized token '" + str + "'");
                }
            } else {
                char ch = str.charAt(0);
                if (ch == 't' && TRUE.equals(str)) {
                    return Boolean.TRUE;
                } else if (ch == 'f' && FALSE.equals(str)) {
                    return Boolean.FALSE;
                } else if (ch == 'n' && NULL.equals(str)) {
                    return null;
                } else {
                    try {
                        if (isNegativeZero(str)) {
                            return Double.parseDouble(str);
                        } else if (isExponential(str)) {
                            return new DecimalValue(str);
                        } else {
                            return Long.parseLong(str);
                        }
                    } catch (NumberFormatException ignore) {
                        throw new StreamParserException("unrecognized token '" + str + "'");
                    }
                }
            }
        }

        private void processNonStringValueAsJson(String str, ValueType type) throws StreamParserException {
            setValueToJsonType(type, getNonStringValueAsJson(str));
        }

        private void processNonStringValue(ValueType type) throws StreamParserException {
            String str = value();
            Type targetType = this.targetTypes.get(this.targetTypes.size() - 1);
            Type referredType = TypeUtils.getImpliedType(targetType);
            switch (referredType.getTag()) {
                case TypeTags.UNION_TAG, TypeTags.FINITE_TYPE_TAG:
                    processNonStringValueAsJson(str, type);
                    if (this.nodesStackSizeWhenUnionStarts == -1) {
                        this.currentJsonNode = convert(this.currentJsonNode, targetType);
                    }
                    break;
                case TypeTags.ANYDATA_TAG, TypeTags.JSON_TAG, TypeTags.TABLE_TAG:
                    processNonStringValueAsJson(str, type);
                    break;
                case TypeTags.ARRAY_TAG:
                    if (this.currentJsonNode == null) {
                        throw new StreamParserException("unrecognized token '" + str + "'");
                    }
                    int listIndex = this.listIndices.get(this.listIndices.size() - 1);
                    ArrayType arrayType = (ArrayType) referredType;
                    Type elementType = TypeUtils.getImpliedType(arrayType.getElementType());
                    ((ArrayValueImpl) this.currentJsonNode).addRefValue(listIndex, convertValues(elementType, str));
                    this.listIndices.set(this.listIndices.size() - 1, listIndex + 1);
                    break;
                case TypeTags.TUPLE_TAG:
                    if (this.currentJsonNode == null) {
                        throw new StreamParserException("unrecognized token '" + str + "'");
                    }
                    int tupleListIndex = this.listIndices.get(this.listIndices.size() - 1);
                    TupleType tupleType = (TupleType) referredType;
                    List<Type> tupleTypes = tupleType.getTupleTypes();
                    int targetTupleSize = tupleTypes.size();
                    Type tupleRestType = tupleType.getRestType();
                    boolean noRestType = tupleRestType == null;
                    Type tupleElementType;
                    if (targetTupleSize <= tupleListIndex) {
                        if (noRestType) {
                            throw new StreamParserException("'" + tupleType
                                    + "' tuple size is not enough for the provided values");
                        } else {
                            tupleElementType = TypeUtils.getImpliedType(tupleRestType);
                        }
                    } else {
                        tupleElementType = TypeUtils.getImpliedType(tupleTypes.get(tupleListIndex));
                    }
                    ((TupleValueImpl) this.currentJsonNode).addRefValueForcefully(tupleListIndex,
                            convertValues(tupleElementType, str));
                    this.listIndices.set(this.listIndices.size() - 1, tupleListIndex + 1);
                    break;
                case TypeTags.MAP_TAG:
                    if (this.currentJsonNode == null) {
                        throw new StreamParserException("unrecognized token '" + str + "'");
                    }
                    MapType mapType = (MapType) referredType;
                    Type constrainedType = TypeUtils.getImpliedType(mapType.getConstrainedType());
                    ((MapValueImpl<BString, Object>) this.currentJsonNode).putForcefully(
                            StringUtils.fromString(this.fieldNames.pop()), convertValues(constrainedType,
                                    str));
                    break;
                case TypeTags.RECORD_TYPE_TAG:
                    if (this.currentJsonNode == null) {
                        throw new StreamParserException("unrecognized token '" + str + "'");
                    }
                    BRecordType recordType = (BRecordType) referredType;
                    String fieldName = this.fieldNames.pop();
                    Map<String, Field> fields = recordType.getFields();
                    Field field = fields.get(fieldName);
                    Type fieldType = field == null ? recordType.restFieldType : field.getFieldType();
                    ((MapValueImpl<BString, Object>) this.currentJsonNode).putForcefully(
                            StringUtils.fromString(fieldName), convertValues(TypeUtils.getImpliedType(fieldType), str));
                    break;
                default:
                    this.currentJsonNode = convertValues(referredType, str);
                    break;
            }
        }

        private void setValueToJsonType(ValueType type, Object value) {
            switch (type) {
                case ARRAY_ELEMENT:
                    ArrayValueImpl arrayValue = (ArrayValueImpl) this.currentJsonNode;
                    arrayValue.addRefValueForcefully(arrayValue.size(), value);
                    break;
                case FIELD:
                    ((MapValueImpl<BString, Object>) this.currentJsonNode).putForcefully(
                            StringUtils.fromString(this.fieldNames.pop()), value);
                    break;
                default:
                    this.currentJsonNode = value;
                    break;
            }
        }

        private static boolean isExponential(String str) {
            return str.contains("e") || str.contains("E");
        }

        private static boolean isNegativeZero(String str) {
            return '-' == str.charAt(0) && 0 == Double.parseDouble(str);
        }

        /**
         * Represents the state during a non-string value is defined.
         */
        private static class NonStringValueState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
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
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
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
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
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
         * from a object string field.
         */
        private static class StringFieldUnicodeHexProcessingState extends UnicodeHexProcessingState {

            @Override
            protected State getSourceState() {
                return STRING_FIELD_VALUE_STATE;
            }

        }

        /**
         * Represents the state where an escaped unicode character in hex format is processed
         * from an array string field.
         */
        private static class StringAEProcessingState extends UnicodeHexProcessingState {

            @Override
            protected State getSourceState() {
                return STRING_ARRAY_ELEMENT_STATE;
            }

        }

        /**
         * Represents the state where an escaped unicode character in hex format is processed
         * from a string value.
         */
        private static class StringValueUnicodeHexProcessingState extends UnicodeHexProcessingState {

            @Override
            protected State getSourceState() {
                return STRING_VALUE_STATE;
            }

        }

        /**
         * Represents the state where an escaped unicode character in hex format is processed
         * from a field name.
         */
        private static class FieldNameUnicodeHexProcessingState extends UnicodeHexProcessingState {

            @Override
            protected State getSourceState() {
                return FIELD_NAME_STATE;
            }

        }

        /**
         * Represents the state where an escaped unicode character in hex format is processed.
         */
        private abstract static class UnicodeHexProcessingState implements State {

            protected abstract State getSourceState();

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
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
                            state = this.getSourceState();
                            break;
                        }
                        state = this;
                        continue;
                    }
                    this.reset(sm);
                    throw new StreamParserException("expected the hexadecimal value of a unicode character");
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
         * Represents the state where an escaped character is processed in a object string field.
         */
        private static class StringFieldEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

            @Override
            protected State getSourceState() {
                return STRING_FIELD_VALUE_STATE;
            }

        }

        /**
         * Represents the state where an escaped character is processed in an array string field.
         */
        private static class StringAEEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

            @Override
            protected State getSourceState() {
                return STRING_ARRAY_ELEMENT_STATE;
            }

        }

        /**
         * Represents the state where an escaped character is processed in a string value.
         */
        private static class StringValueEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

            @Override
            protected State getSourceState() {
                return STRING_VALUE_STATE;
            }

        }

        /**
         * Represents the state where an escaped character is processed in a field name.
         */
        private static class FieldNameEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

            @Override
            protected State getSourceState() {
                return FIELD_NAME_STATE;
            }

        }

        /**
         * Represents the state where an escaped character is processed.
         */
        private abstract static class EscapedCharacterProcessingState implements State {

            protected abstract State getSourceState();

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws StreamParserException {
                State state = null;
                char ch;
                if (i < count) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    switch (ch) {
                        case '"':
                            sm.append(QUOTES);
                            state = this.getSourceState();
                            break;
                        case '\\':
                            sm.append(REV_SOL);
                            state = this.getSourceState();
                            break;
                        case '/':
                            sm.append(SOL);
                            state = this.getSourceState();
                            break;
                        case 'b':
                            sm.append(BACKSPACE);
                            state = this.getSourceState();
                            break;
                        case 'f':
                            sm.append(FORMFEED);
                            state = this.getSourceState();
                            break;
                        case 'n':
                            sm.append(NEWLINE);
                            state = this.getSourceState();
                            break;
                        case 'r':
                            sm.append(CR);
                            state = this.getSourceState();
                            break;
                        case 't':
                            sm.append(HZ_TAB);
                            state = this.getSourceState();
                            break;
                        case 'u':
                            if (this.getSourceState() == STRING_FIELD_VALUE_STATE) {
                                state = STRING_FIELD_UNICODE_HEX_PROCESSING_STATE;
                            } else if (this.getSourceState() == STRING_VALUE_STATE) {
                                state = STRING_VALUE_UNICODE_HEX_PROCESSING_STATE;
                            } else if (this.getSourceState() == FIELD_NAME_STATE) {
                                state = FIELD_NAME_UNICODE_HEX_PROCESSING_STATE;
                            } else if (this.getSourceState() == STRING_ARRAY_ELEMENT_STATE) {
                                state = STRING_AE_PROCESSING_STATE;
                            } else {
                                throw new StreamParserException("unknown source '" + this.getSourceState() +
                                        "' in escape char processing state");
                            }
                            break;
                        default:
                            throw new StreamParserException("expected escaped characters");
                    }
                }
                sm.index = i + 1;
                return state;
            }

        }

    }

    private static Object convert(Object value, Type targetType) {
        return convert(value, targetType, new HashSet<>());
    }

    private static Object convert(Object value, Type targetType, Set<TypeValuePair> unresolvedValues) {

        if (value == null) {
            if (TypeUtils.getImpliedType(targetType).isNilable()) {
                return null;
            }
            throw createError(ErrorReasons.BALLERINA_PREFIXED_CONVERSION_ERROR,
                    ErrorHelper.getErrorDetails(ErrorCodes.CANNOT_CONVERT_NIL, targetType));
        }

        Type sourceType = TypeUtils.getImpliedType(TypeChecker.getType(value));

        TypeValuePair typeValuePair = new TypeValuePair(value, targetType);
        if (unresolvedValues.contains(typeValuePair)) {
            throw createError(ErrorReasons.BALLERINA_PREFIXED_CYCLIC_VALUE_REFERENCE_ERROR,
                    ErrorHelper.getErrorMessage(ErrorCodes.CYCLIC_VALUE_REFERENCE, sourceType));
        }
        unresolvedValues.add(typeValuePair);

        List<String> errors = new ArrayList<>();
        Type convertibleType = TypeConverter.getConvertibleType(value, targetType,
                null, new HashSet<>(), errors, true);
        if (convertibleType == null) {
            throw CloneUtils.createConversionError(value, targetType, errors);
        }

        Object newValue;
        Type matchingType = TypeUtils.getImpliedType(convertibleType);
        switch (sourceType.getTag()) {
            case TypeTags.MAP_TAG:
                newValue = convertMap((MapValueImpl<BString, Object>) value, matchingType,
                        convertibleType, unresolvedValues);
                break;
            case TypeTags.ARRAY_TAG:
                newValue = convertArray((ArrayValueImpl) value, matchingType, convertibleType, unresolvedValues);
                break;
            case TypeTags.TABLE_TAG, TypeTags.RECORD_TYPE_TAG, TypeTags.TUPLE_TAG:
                // source type can't be tuple, record and table in the stream parser
                throw createConversionError(value, targetType);
            default:
                if (TypeChecker.isRegExpType(targetType) && matchingType.getTag() == TypeTags.STRING_TAG) {
                    try {
                        newValue = RegExpFactory.parse(((BString) value).getValue());
                        break;
                    } catch (BError e) {
                        throw createConversionError(value, targetType, e.getMessage());
                    }
                }

                if (TypeTags.isXMLTypeTag(matchingType.getTag())) {
                    String xmlString = value.toString();
                    try {
                        newValue = BalStringUtils.parseXmlExpressionStringValue(xmlString);
                    } catch (BError e) {
                        throw createConversionError(value, targetType);
                    }
                    if (matchingType.isReadOnly()) {
                        ((BRefValue) newValue).freezeDirect();
                    }
                    break;
                }

                // can't put the below, above handling xml because for selectively mutable types when the provided value
                // is readonly, if the target type is non readonly, checkIsType provides true, but we can't just clone
                // the value as it should be non readonly.
                if (TypeChecker.checkIsType(value, matchingType)) {
                    newValue = value;
                    break;
                }

                // handle primitive values
                if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG) {
                    // has to be a numeric conversion.
                    newValue = TypeConverter.convertValues(matchingType, value);
                    break;
                }
                // should never reach here
                throw createConversionError(value, targetType);
        }

        unresolvedValues.remove(typeValuePair);
        return newValue;
    }

    private static Object convertArray(ArrayValueImpl array, Type targetType, Type targetRefType,
                                       Set<TypeValuePair> unresolvedValues) {
        switch (targetType.getTag()) {
            case TypeTags.ARRAY_TAG:
                ArrayType arrayType = (ArrayType) targetType;
                ArrayValueImpl newArray = new ArrayValueImpl(targetRefType, arrayType.getSize());
                for (int i = 0; i < array.size(); i++) {
                    newArray.addRefValueForcefully(i, convert(array.getRefValue(i),
                            arrayType.getElementType(), unresolvedValues));
                }
                return newArray;
            case TypeTags.TUPLE_TAG:
                TupleType tupleType = (TupleType) targetType;
                int minLen = tupleType.getTupleTypes().size();
                BListInitialValueEntry[] tupleValues = new BListInitialValueEntry[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    Type elementType = (i < minLen) ? tupleType.getTupleTypes().get(i) : tupleType.getRestType();
                    tupleValues[i] = ValueCreator.createListInitialValueEntry(
                            convert(array.getRefValue(i), elementType, unresolvedValues));
                }
                return new TupleValueImpl(targetRefType, tupleValues);
            case TypeTags.TABLE_TAG:
                TableType tableType = (TableType) targetType;
                for (int i = 0; i < array.size(); i++) {
                    Object bMap = convert(array.get(i), tableType.getConstrainedType(), unresolvedValues);
                    array.setRefValueForcefully(i, bMap);
                }
                array.setArrayRefTypeForcefully(TypeCreator.createArrayType(tableType.getConstrainedType()),
                        array.size());
                BArray fieldNames = StringUtils.fromStringArray(tableType.getFieldNames());
                return new TableValueImpl(targetRefType, array, (ArrayValue) fieldNames);
            default:
                break;
        }
        // should never reach here
        throw createConversionError(array, targetType);
    }

    private static Object convertMap(MapValueImpl<BString, Object> map, Type targetType, Type targetRefType,
                                     Set<TypeValuePair> unresolvedValues) {
        Set<Map.Entry<BString, Object>> mapEntrySet = map.entrySet();
        switch (targetType.getTag()) {
            case TypeTags.MAP_TAG:
                Type constraintType = ((MapType) targetType).getConstrainedType();
                for (Map.Entry<BString, Object> entry : mapEntrySet) {
                    Object newValue = convert(entry.getValue(), constraintType, unresolvedValues);
                    map.putForcefully(entry.getKey(), newValue);
                }
                map.setTypeForcefully(targetRefType);
                return map;
            case TypeTags.RECORD_TYPE_TAG:
                RecordType recordType = (RecordType) targetType;
                Type restFieldType = recordType.getRestFieldType();
                Map<String, Type> targetTypeField = new HashMap<>();
                for (Field field : recordType.getFields().values()) {
                    targetTypeField.put(field.getFieldName(), field.getFieldType());
                }
                for (Map.Entry<BString, Object> entry : mapEntrySet) {
                    Type fieldType = targetTypeField.getOrDefault(entry.getKey().toString(), restFieldType);
                    Object newValue = convert(entry.getValue(), fieldType, unresolvedValues);
                    map.putForcefully(entry.getKey(), newValue);
                }
                Optional<IntersectionType> intersectionType = ((BRecordType) TypeUtils.getImpliedType(targetRefType))
                        .getIntersectionType();
                if (targetRefType.isReadOnly() && intersectionType.isPresent() && !map.getType().isReadOnly()) {
                    Type mutableType = ReadOnlyUtils.getMutableType((BIntersectionType) intersectionType.get());
                    MapValueImpl<BString, Object> bMapValue = (MapValueImpl<BString, Object>) ValueUtils
                            .createRecordValue(mutableType.getPackage(), mutableType.getName(), map);
                    bMapValue.freezeDirect();
                    return bMapValue;
                }
                return ValueUtils.createRecordValue(targetRefType.getPackage(), targetRefType.getName(), map);
            default:
                break;
        }
        // should never reach here
        throw createConversionError(map, targetType);
    }


}
