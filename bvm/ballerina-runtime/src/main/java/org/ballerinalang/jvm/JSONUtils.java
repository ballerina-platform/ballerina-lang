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

package org.ballerinalang.jvm;

import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BJSONType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeConstants;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.StreamingJsonValue;
import org.ballerinalang.jvm.values.TableValue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.ballerinalang.jvm.util.BLangConstants.MAP_LANG_LIB;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.JSON_OPERATION_ERROR;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.MAP_KEY_NOT_FOUND_ERROR;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Common utility methods used for JSON manipulation.
 * 
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class JSONUtils {

    public static final String OBJECT = "object";
    public static final String ARRAY = "array";

    /**
     * Check whether JSON has particular field.
     *
     * @param json JSON to be considered.
     * @param elementName String name json field to be considered.
     * @return Boolean 'true' if JSON has given field.
     */
    public static boolean hasElement(Object json, String elementName) {
        if (!isJSONObject(json)) {
            return false;
        }
        return ((MapValueImpl<String, ?>) json).containsKey(elementName);
    }

    /**
     * Convert {@link ArrayValue} to JSON.
     *
     * @param bArray {@link ArrayValue} to be converted to JSON
     * @return JSON representation of the provided bArray
     */
    public static ArrayValue convertArrayToJSON(ArrayValue bArray) {
        if (bArray == null) {
            return null;
        }

        BType elementType = bArray.getElementType();
        if (elementType == BTypes.typeInt) {
            return convertIntArrayToJSON(bArray);
        } else if (elementType == BTypes.typeBoolean) {
            return convertBooleanArrayToJSON(bArray);
        } else if (elementType == BTypes.typeFloat) {
            return convertFloatArrayToJSON(bArray);
        } else if (elementType == BTypes.typeString) {
            return convertStringArrayToJSON(bArray);
        } else {
            return convertRefArrayToJSON(bArray);
        }
    }

    /**
     * Convert map value to JSON.
     *
     * @param map value {@link MapValueImpl} to be converted to JSON
     * @param targetType the target JSON type to be convert to
     * @return JSON representation of the provided array
     */
    public static Object convertMapToJSON(MapValueImpl<String, ?> map, BJSONType targetType) {
        if (map == null) {
            return null;
        }

        MapValueImpl<String, Object> json = new MapValueImpl<>(targetType);
        for (Entry<String, ?> structField : map.entrySet()) {
            String key = structField.getKey();
            Object value = structField.getValue();
            populateJSON(json, key, value, BTypes.typeJSON);
        }
        return json;
    }

    /**
     * Get an element from a JSON.
     *
     * @param json JSON to get the element from
     * @param elementName Name of the element to be retrieved
     * @return Element of the JSON for the provided key, if the JSON is object type. Error if not an object or nil
     * if the object does not have the key.
     */
    public static Object getElementOrNil(Object json, String elementName) {
        return getMappingElement(json, elementName, true);
    }

    /**
     * Get an element from a JSON.
     *
     * @param json JSON to get the element from
     * @param elementName Name of the element to be retrieved
     * @return Element of the JSON for the provided key, if the JSON is object type. Error if not an object or does
     * not have the key.
     */
    public static Object getElement(Object json, String elementName) {
        return getMappingElement(json, elementName, false);
    }

    /**
     * Get an element from a JSON.
     *
     * @param json JSON object to get the element from
     * @param elementName Name of the element to be retrieved
     * @param returnNilOnMissingKey Whether to return nil on missing key instead of error
     * @return Element of JSON having the provided name, if the JSON is object type. Null otherwise.
     */
    private static Object getMappingElement(Object json, String elementName, boolean returnNilOnMissingKey) {
        if (!isJSONObject(json)) {
            return BallerinaErrors.createError(JSON_OPERATION_ERROR, "JSON value is not a mapping");
        }

        MapValueImpl<String, Object> jsonObject = (MapValueImpl<String, Object>) json;

        if (!jsonObject.containsKey(elementName)) {
            if (returnNilOnMissingKey) {
                return null;
            }

            return BallerinaErrors.createError(MAP_KEY_NOT_FOUND_ERROR,
                                               "Key '" + elementName + "' not found in JSON mapping");
        }

        try {
            return jsonObject.get(elementName);
        } catch (BallerinaException e) {
            if (e.getDetail() != null) {
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.JSON_GET_ERROR, e.getDetail());
            }
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.JSON_GET_ERROR, e.getMessage());
        } catch (Throwable t) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.JSON_GET_ERROR, t.getMessage());
        }
    }

    /**
     * Set an element in a JSON. If an element with the given name already exists,
     * this method will update the existing element. Otherwise, a new element with
     * the given name will be added. If the JSON is not object type, then this
     * operation has no effect.
     * 
     * @param json JSON object to set the element
     * @param elementName Name of the element to be set
     * @param element JSON element
     */
    public static void setElement(Object json, String elementName, Object element) {
        if (!isJSONObject(json)) {
            return;
        }

        try {
            ((MapValueImpl<String, Object>) json).put(elementName, element);
        } catch (ErrorValue e) {
            throw e;
        } catch (Throwable t) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(MAP_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                    RuntimeErrors.JSON_SET_ERROR, t.getMessage());
        }
    }

    /**
     * Check whether provided JSON object is a JSON Array.
     *
     * @param json JSON to execute array condition.
     * @return returns true if provided JSON is a JSON Array.
     */
    public static boolean isJSONArray(Object json) {
        if (!(json instanceof RefValue)) {
            return false;
        }
        return ((RefValue) json).getType().getTag() == TypeTags.ARRAY_TAG;
    }

    /**
     * Check whether provided JSON object is a JSON Object.
     *
     * @param json JSON to execute array condition.
     * @return returns true if provided JSON is a JSON Object.
     */
    public static boolean isJSONObject(Object json) {
        if (!(json instanceof RefValue)) {
            return false;
        }

        BType type = ((RefValue) json).getType();
        return type.getTag() == TypeTags.JSON_TAG || type.getTag() == TypeTags.MAP_TAG;
    }

    /**
     * Set an element in the given position of a JSON array. This method will update the existing value.
     * If the JSON is not array type, then this operation has no effect.
     * 
     * @param json JSON array to set the element
     * @param index Index of the element to be set
     * @param element Element to be set
     */
    public static void setArrayElement(Object json, long index, Object element) {
        if (!isJSONArray(json)) {
            return;
        }

        BArrayType jsonArray = (BArrayType) ((RefValue) json).getType();
        BType elementType = jsonArray.getElementType();
        if (!TypeChecker.checkIsType(element, elementType)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE, elementType,
                    (element != null) ? TypeChecker.getType(element) : BTypes.typeNull);
        }

        try {
            ((ArrayValue) json).add(index, element);
        } catch (ErrorValue e) {
            Object errorDetails = e.getDetails();
            if (errorDetails != null) {
                if (TypeChecker.getType(errorDetails).getTag() == TypeTags.MAP_TAG &&
                        ((MapValue) errorDetails).containsKey(BallerinaErrors.ERROR_MESSAGE_FIELD)) {
                    throw BLangExceptionHelper.getRuntimeException(e.getMessage(), RuntimeErrors.JSON_SET_ERROR,
                            ((MapValue) errorDetails).get(BallerinaErrors.ERROR_MESSAGE_FIELD));
                }
                throw BLangExceptionHelper.getRuntimeException(e.getMessage(), RuntimeErrors.JSON_SET_ERROR,
                        e.getDetails());
            }
            throw BLangExceptionHelper.getRuntimeException(e.getMessage(), RuntimeErrors.JSON_SET_ERROR,
                    e.getMessage());
        } catch (BallerinaException e) {
            throw BLangExceptionHelper.getRuntimeException(e.getMessage(), RuntimeErrors.JSON_SET_ERROR, e.getDetail());
        } catch (Throwable t) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.JSON_SET_ERROR, t.getMessage());
        }
    }

    /**
     * Convert a JSON node to a map.
     *
     * @param json JSON to convert
     * @param mapType MapType which the JSON is converted to.
     * @return If the provided JSON is of object-type, this method will return a {@link MapValueImpl} containing the
     *          values of the JSON object. Otherwise a {@link BallerinaException} will be thrown.
     */
    public static MapValueImpl<String, ?> jsonToMap(Object json, BMapType mapType) {
        if (json == null || !isJSONObject(json)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE,
                    getComplexObjectTypeName(OBJECT), getTypeName(json));
        }

        MapValueImpl<String, Object> map = new MapValueImpl<>(mapType);
        BType mapConstraint = mapType.getConstrainedType();
        if (mapConstraint == null || mapConstraint.getTag() == TypeTags.ANY_TAG ||
                mapConstraint.getTag() == TypeTags.JSON_TAG) {
            ((MapValueImpl<String, Object>) json).entrySet().forEach(entry -> {
                map.put(entry.getKey(), entry.getValue());
            });

            return map;
        }

        // We reach here if the map is constrained.
        ((MapValueImpl<String, Object>) json).entrySet().forEach(entry -> {
            map.put(entry.getKey(), convertJSON(entry.getValue(), mapConstraint));
        });

        return map;
    }

    /**
     * Convert a BJSON to a user defined record.
     *
     * @param json JSON to convert
     * @param structType Type (definition) of the target record
     * @return If the provided JSON is of object-type, this method will return a {@link MapValueImpl} containing the
     *         values of the JSON object. Otherwise the method will throw a {@link BallerinaException}.
     */
    public static MapValueImpl<String, Object> convertJSONToRecord(Object json, BStructureType structType) {
        if (json == null || !isJSONObject(json)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE,
                    getComplexObjectTypeName(OBJECT), getTypeName(json));
        }

        MapValueImpl<String, Object> bStruct = new MapValueImpl<>(structType);
        MapValueImpl<String, Object> jsonObject = (MapValueImpl<String, Object>) json;
        for (Map.Entry<String, BField> field : structType.getFields().entrySet()) {
            BType fieldType = field.getValue().type;
            String fieldName = field.getValue().name;
            try {
                // If the field does not exists in the JSON, set the default value for that struct field.
                if (!jsonObject.containsKey(fieldName)) {
                    bStruct.put(fieldName, fieldType.getZeroValue());
                    continue;
                }

                Object jsonValue = jsonObject.get(fieldName);
                bStruct.put(fieldName, convertJSON(jsonValue, fieldType));
            } catch (Exception e) {
                handleError(e, fieldName);
            }
        }

        return bStruct;
    }

    public static Object convertJSON(Object jsonValue, BType targetType) {
        switch (targetType.getTag()) {
            case TypeTags.INT_TAG:
                return jsonNodeToInt(jsonValue);
            case TypeTags.FLOAT_TAG:
                return jsonNodeToFloat(jsonValue);
            case TypeTags.DECIMAL_TAG:
                return jsonNodeToDecimal(jsonValue);
            case TypeTags.STRING_TAG:
                return jsonValue.toString();
            case TypeTags.BOOLEAN_TAG:
                return jsonNodeToBoolean(jsonValue);
            case TypeTags.JSON_TAG:
                if (jsonValue != null && !TypeChecker.checkIsType(jsonValue, targetType)) {
                    throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE, targetType,
                            getTypeName(jsonValue));
                }
                // fall through
            case TypeTags.ANY_TAG:
                return jsonValue;
            case TypeTags.UNION_TAG:
                BUnionType type = (BUnionType) targetType;
                if (jsonValue == null && type.isNullable()) {
                    return null;
                }
                List<BType> matchingTypes = type.getMemberTypes().stream()
                        .filter(memberType -> memberType != BTypes.typeNull).collect(Collectors.toList());
                if (matchingTypes.size() == 1) {
                    return convertJSON(jsonValue, matchingTypes.get(0));
                }
                break;
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return convertJSONToRecord(jsonValue, (BStructureType) targetType);
            case TypeTags.ARRAY_TAG:
                return convertJSONToBArray(jsonValue, (BArrayType) targetType);
            case TypeTags.MAP_TAG:
                return jsonToMap(jsonValue, (BMapType) targetType);
            case TypeTags.NULL_TAG:
                if (jsonValue == null) {
                    return null;
                }
                // fall through
            default:
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE, targetType,
                        getTypeName(jsonValue));
        }
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE, targetType,
                getTypeName(jsonValue));
    }

    /**
     * Returns the keys of a JSON as a {@link ArrayValue}.
     * 
     * @param json JSON to get the keys
     * @return Keys of the JSON as a {@link ArrayValue}
     */
    public static ArrayValue getKeys(Object json) {
        if (json == null || !isJSONObject(json)) {
            return new ArrayValueImpl(new BArrayType(BTypes.typeString));
        }

        String[] keys = ((MapValueImpl<String, ?>) json).getKeys();
        return new ArrayValueImpl(keys);
    }

    public static Object convertUnionTypeToJSON(Object source, BJSONType targetType) {
        if (source == null) {
            return null;
        }

        BType type = TypeChecker.getType(source);
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.STRING_TAG:
            case TypeTags.BOOLEAN_TAG:
                return source;
            case TypeTags.NULL_TAG:
                return null;
            case TypeTags.MAP_TAG:
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return convertMapToJSON((MapValueImpl<String, Object>) source, targetType);
            case TypeTags.JSON_TAG:
                return source;
            default:
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE, BTypes.typeJSON, type);
        }
    }

    /**
     * Remove a field from JSON. Has no effect if the JSON if not object types or if the given field doesn't exists.
     * 
     * @param json JSON object
     * @param fieldName Name of the field to remove
     */
    public static void remove(Object json, String fieldName) {
        if (!isJSONObject(json)) {
            return;
        }

        ((MapValueImpl<String, ?>) json).remove(fieldName);
    }

    public static ErrorValue getErrorIfUnmergeable(Object j1, Object j2, List<ObjectPair> visitedPairs) {
        if (j1 == null || j2 == null) {
            return null;
        }

        BType j1Type = TypeChecker.getType(j1);
        BType j2Type = TypeChecker.getType(j2);

        if (j1Type.getTag() != TypeTags.MAP_TAG || j2Type.getTag() != TypeTags.MAP_TAG) {
            return BallerinaErrors.createError(BallerinaErrorReasons.MERGE_JSON_ERROR,
                                               "Cannot merge JSON values of types '" + j1Type + "' and '" +
                                                       j2Type + "'");
        }

        ObjectPair currentPair = new ObjectPair(j1, j2);
        if (visitedPairs.contains(currentPair)) {
            return BallerinaErrors.createError(BallerinaErrorReasons.MERGE_JSON_ERROR,
                                               "Cannot merge JSON values with cyclic references");
        }
        visitedPairs.add(currentPair);

        MapValue<String, Object> m1 = (MapValue<String, Object>) j1;
        MapValue<String, Object> m2 = (MapValue<String, Object>) j2;

        for (Map.Entry<String, Object> entry : m2.entrySet()) {
            String key = entry.getKey();

            if (!m1.containsKey(key)) {
                continue;
            }

            ErrorValue elementMergeNullableError = getErrorIfUnmergeable(m1.get(key), entry.getValue(), visitedPairs);

            if (elementMergeNullableError == null) {
                continue;
            }

            MapValueImpl<String, Object> detailMap = new MapValueImpl<>(BTypes.typeErrorDetail);
            detailMap.put(TypeConstants.DETAIL_MESSAGE, "JSON Merge failed for key '" + key + "'");
            detailMap.put(TypeConstants.DETAIL_CAUSE, elementMergeNullableError);
            return BallerinaErrors.createError(BallerinaErrorReasons.MERGE_JSON_ERROR, detailMap);
        }
        return null;
    }

    public static Object mergeJson(Object j1, Object j2, boolean checkMergeability) {
        if (j1 == null) {
            return j2;
        }

        if (j2 == null) {
            return j1;
        }

        if (checkMergeability) {
            BType j1Type = TypeChecker.getType(j1);
            BType j2Type = TypeChecker.getType(j2);

            if (j1Type.getTag() != TypeTags.MAP_TAG || j2Type.getTag() != TypeTags.MAP_TAG) {
                return BallerinaErrors.createError(BallerinaErrorReasons.MERGE_JSON_ERROR,
                                                   "Cannot merge JSON values of types '" + j1Type + "' and '" +
                                                           j2Type + "'");
            }
        }

        MapValue<String, Object> m1 = (MapValue<String, Object>) j1;
        MapValue<String, Object> m2 = (MapValue<String, Object>) j2;
        return m1.merge(m2, true);
    }

    /**
     * Convert a JSON node to an array.
     *
     * @param json JSON to convert
     * @param targetArrayType Type of the target array
     * @return If the provided JSON is of array type, this method will return a {@link BArrayType} containing the values
     *         of the JSON array. Otherwise the method will throw a {@link BallerinaException}.
     */
    public static ArrayValue convertJSONToBArray(Object json, BArrayType targetArrayType) {
        if (!(json instanceof ArrayValue)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE,
                    getComplexObjectTypeName(ARRAY), getTypeName(json));
        }

        BType targetElementType = targetArrayType.getElementType();
        ArrayValue jsonArray = (ArrayValue) json;
        switch (targetElementType.getTag()) {
            case TypeTags.INT_TAG:
                return jsonArrayToBIntArray(jsonArray);
            case TypeTags.FLOAT_TAG:
                return jsonArrayToBFloatArray(jsonArray);
            case TypeTags.DECIMAL_TAG:
                return jsonArrayToBDecimalArray(jsonArray);
            case TypeTags.STRING_TAG:
                return jsonArrayToBStringArray(jsonArray);
            case TypeTags.BOOLEAN_TAG:
                return jsonArrayToBooleanArray(jsonArray);
            case TypeTags.ANY_TAG:
                ArrayValue array = new ArrayValueImpl(targetArrayType);
                for (int i = 0; i < jsonArray.size(); i++) {
                    array.add(i, jsonArray.getRefValue(i));
                }
                return array;
            default:
                array = new ArrayValueImpl(targetArrayType);
                for (int i = 0; i < jsonArray.size(); i++) {
                    array.append(convertJSON(jsonArray.getRefValue(i), targetElementType));
                }
                return array;
        }
    }

    /**
     * Convert {@link TableValue} to JSON.
     *
     * @param table {@link TableValue} to be converted to {@link StreamingJsonValue}
     * @return JSON representation of the provided table
     */
    public static Object toJSON(TableValue table) {
        TableJSONDataSource jsonDataSource = new TableJSONDataSource(table);
        if (table.isInMemoryTable()) {
            return jsonDataSource.build();
        }

        return new StreamingJsonValue(jsonDataSource);
    }

    public static ErrorValue createJsonConversionError(Throwable throwable, String prefix) {
        String detail = throwable.getMessage() != null ?
                prefix + ": " + throwable.getMessage() :
                "error occurred in JSON Conversion";
        return BallerinaErrors.createError(BallerinaErrorReasons.JSON_CONVERSION_ERROR, detail);
    }

    // Private methods

    /**
     * Convert to int.
     *
     * @param json node to be converted
     * @return BInteger value of the JSON, if its a integer or a long JSON node. Error, otherwise.
     */
    private static long jsonNodeToInt(Object json) {
        if (!(json instanceof Long)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                    BTypes.typeInt, getTypeName(json));
        }

        return ((Long) json).longValue();
    }

    /**
     * Convert to float.
     *
     * @param json node to be converted
     * @return BFloat value of the JSON, if its a double or a float JSON node. Error, otherwise.
     */
    private static double jsonNodeToFloat(Object json) {
        if (json instanceof Integer) {
            return ((Integer) json).longValue();
        } else if (json instanceof Double) {
            return ((Double) json).doubleValue();
        } else {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                    BTypes.typeFloat, getTypeName(json));
        }
    }

    /**
     * Convert JSON to decimal.
     *
     * @param json JSON to be converted
     * @return BDecimal value of the JSON, if it's a valid convertible JSON node. Error, otherwise.
     */
    private static DecimalValue jsonNodeToDecimal(Object json) {
        BigDecimal decimal = null;
        if (json instanceof Integer) {
            decimal = new BigDecimal(((Integer) json).longValue());
        } else if (json instanceof Double) {
            decimal = new BigDecimal(((Double) json).doubleValue());
        } else if (json instanceof BigDecimal) {
            decimal = (BigDecimal) json;
        } else {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                    BTypes.typeDecimal, getTypeName(json));
        }

        return new DecimalValue(decimal);
    }

    /**
     * Convert to boolean.
     *
     * @param json node to be converted
     * @return Boolean value of the JSON, if its a boolean node. Error, otherwise.
     */
    private static boolean jsonNodeToBoolean(Object json) {
        if (!(json instanceof Boolean)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                    BTypes.typeBoolean, getTypeName(json));
        }
        return ((Boolean) json).booleanValue();
    }

    private static ArrayValue jsonArrayToBIntArray(ArrayValue arrayNode) {
        ArrayValue intArray = new ArrayValueImpl(new BArrayType(BTypes.typeInt));
        for (int i = 0; i < arrayNode.size(); i++) {
            Object jsonValue = arrayNode.getRefValue(i);
            intArray.add(i, jsonNodeToInt(jsonValue));
        }
        return intArray;
    }

    private static ArrayValue jsonArrayToBFloatArray(ArrayValue arrayNode) {
        ArrayValue floatArray = new ArrayValueImpl(new BArrayType(BTypes.typeFloat));
        for (int i = 0; i < arrayNode.size(); i++) {
            Object jsonValue = arrayNode.getRefValue(i);
            floatArray.add(i, jsonNodeToFloat(jsonValue));
        }
        return floatArray;
    }

    private static ArrayValue jsonArrayToBDecimalArray(ArrayValue arrayNode) {
        ArrayValue decimalArray = new ArrayValueImpl(new BArrayType(BTypes.typeDecimal));
        for (int i = 0; i < arrayNode.size(); i++) {
            Object jsonValue = arrayNode.getRefValue(i);
            decimalArray.add(i, jsonNodeToDecimal(jsonValue));
        }
        return decimalArray;
    }

    private static ArrayValue jsonArrayToBStringArray(ArrayValue arrayNode) {
        ArrayValue stringArray = new ArrayValueImpl(new BArrayType(BTypes.typeString));
        for (int i = 0; i < arrayNode.size(); i++) {
            stringArray.add(i, arrayNode.getRefValue(i).toString());
        }
        return stringArray;
    }

    private static ArrayValue jsonArrayToBooleanArray(ArrayValue arrayNode) {
        ArrayValue booleanArray = new ArrayValueImpl(new BArrayType(BTypes.typeBoolean));
        for (int i = 0; i < arrayNode.size(); i++) {
            Object jsonValue = arrayNode.getRefValue(i);
            booleanArray.add(i, jsonNodeToBoolean(jsonValue));
        }
        return booleanArray;
    }

    /**
     * Convert {@link ArrayValue} to JSON.
     *
     * @param refValueArray {@link ArrayValue} to be converted to JSON
     * @return JSON representation of the provided refValueArray
     */
    private static ArrayValue convertRefArrayToJSON(ArrayValue refValueArray) {
        ArrayValue json = new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < refValueArray.size(); i++) {
            Object value = refValueArray.getRefValue(i);
            if (value == null) {
                json.append(null);
            }

            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
                case TypeTags.JSON_TAG:
                    json.append(value);
                    break;
                case TypeTags.MAP_TAG:
                case TypeTags.RECORD_TYPE_TAG:
                case TypeTags.OBJECT_TYPE_TAG:
                    json.append(convertMapToJSON((MapValueImpl<String, ?>) value, (BJSONType) BTypes.typeJSON));
                    break;
                case TypeTags.ARRAY_TAG:
                    json.append(convertArrayToJSON((ArrayValue) value));
                    break;
                default:
                    throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE, BTypes.typeJSON,
                            type);
            }
        }
        return json;
    }

    /**
     * Convert {@link ArrayValue} to JSON.
     *
     * @param intArray {@link ArrayValue} to be converted to JSON
     * @return JSON representation of the provided intArray
     */
    private static ArrayValue convertIntArrayToJSON(ArrayValue intArray) {
        ArrayValue json = new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < intArray.size(); i++) {
            long value = intArray.getInt(i);
            json.append(new Long(value));
        }
        return json;
    }

    /**
     * Convert {@link ArrayValue} to JSON.
     *
     * @param floatArray {@link ArrayValue} to be converted to JSON
     * @return JSON representation of the provided floatArray
     */
    private static ArrayValue convertFloatArrayToJSON(ArrayValue floatArray) {
        ArrayValue json = new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < floatArray.size(); i++) {
            double value = floatArray.getFloat(i);
            json.append(new Double(value));
        }
        return json;
    }

    /**
     * Convert {@link ArrayValue} to JSON.
     *
     * @param stringArray {@link ArrayValue} to be converted to JSON
     * @return JSON representation of the provided stringArray
     */
    private static ArrayValue convertStringArrayToJSON(ArrayValue stringArray) {
        ArrayValue json = new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < stringArray.size(); i++) {
            json.append(stringArray.getString(i));
        }
        return json;
    }

    /**
     * Convert {@link ArrayValue} to JSON.
     *
     * @param booleanArray {@link ArrayValue} to be converted to JSON
     * @return JSON representation of the provided booleanArray
     */
    private static ArrayValue convertBooleanArrayToJSON(ArrayValue booleanArray) {
        ArrayValue json = new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < booleanArray.size(); i++) {
            boolean value = booleanArray.getBoolean(i);
            json.append(new Boolean(value));
        }
        return json;
    }

    private static void populateJSON(MapValueImpl<String, Object> json, String key, Object value, BType exptType) {
        try {
            if (value == null) {
                json.put(key, null);
                return;
            }

            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
                case TypeTags.INT_TAG:
                case TypeTags.FLOAT_TAG:
                case TypeTags.DECIMAL_TAG:
                case TypeTags.STRING_TAG:
                case TypeTags.BOOLEAN_TAG:
                case TypeTags.JSON_TAG:
                    json.put(key, value);
                    break;
                case TypeTags.ARRAY_TAG:
                    json.put(key, convertArrayToJSON((ArrayValue) value));
                    break;
                case TypeTags.MAP_TAG:
                case TypeTags.RECORD_TYPE_TAG:
                case TypeTags.OBJECT_TYPE_TAG:
                    json.put(key, convertMapToJSON((MapValueImpl<String, ?>) value, (BJSONType) exptType));
                    break;
                default:
                    throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE, BTypes.typeJSON,
                            type);
            }
        } catch (Exception e) {
            handleError(e, key);
        }
    }

    private static String getTypeName(Object jsonValue) {
        if (jsonValue == null) {
            return BTypes.typeNull.toString();
        }

        return TypeChecker.getType(jsonValue).toString();
    }

    private static String getComplexObjectTypeName(String nodeType) {
        return "json-" + nodeType;
    }

    private static void handleError(Exception e, String fieldName) {
        String errorMsg = e.getCause() == null ? "error while mapping '" + fieldName + "': " : "";
        throw new BallerinaException(errorMsg + e.getMessage(), e);
    }

    private static class ObjectPair {
        Object lhsObject;
        Object rhsObject;

        public ObjectPair(Object lhsObject, Object rhsObject) {
            this.lhsObject = lhsObject;
            this.rhsObject = rhsObject;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ObjectPair)) {
                return false;
            }

            ObjectPair other = (ObjectPair) obj;
            return this.lhsObject == other.lhsObject && this.rhsObject == other.rhsObject;
        }
    }

}
