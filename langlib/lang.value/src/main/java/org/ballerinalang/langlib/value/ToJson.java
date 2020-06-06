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
package org.ballerinalang.langlib.value;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.JSONUtils;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.TypeConverter;
import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.TableValueImpl;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.jvm.BallerinaErrors.createError;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.CONSTRUCT_FROM_CONVERSION_ERROR;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.CONSTRUCT_FROM_CYCLIC_VALUE_REFERENCE_ERROR;
import static org.ballerinalang.jvm.util.exceptions.RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION;
import static org.ballerinalang.util.BLangCompilerConstants.VALUE_VERSION;

/**
 * Extern function lang.values:toJson.
 * Converts a value of type `anydata` to `json`.
 *
 * @since 2.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "lang.value", version = VALUE_VERSION,
        functionName = "toJson",
        args = {
                @Argument(name = "v", type = TypeKind.ANYDATA),
        },
        returnType = {
                @ReturnType(type = TypeKind.JSON),
                @ReturnType(type = TypeKind.ERROR)
        },
        isPublic = true
)
public class ToJson {

    private static final String AMBIGUOUS_TARGET = "ambiguous target type";

    public static Object toJson(Strand strand, Object value) {
        if (value == null) {
            return null;
        }

        BType sourceType = TypeChecker.getType(value);
        BType targetJsonType = BTypes.typeJSON;
        if (!isConvertibleToJson(value, new ArrayList<>())) {
            if ((sourceType.getTag() >= TypeTags.XML_ELEMENT_TAG && sourceType.getTag() <= TypeTags.XML_TEXT_TAG) ||
                    sourceType.getTag() == TypeTags.XML_TAG) {
                return ToString.toString(strand, value);
            } else {
                throw createConversionError(value, targetJsonType);
            }
        }

        // handle primitive values
        if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG) {
            if (TypeChecker.checkIsType(value, targetJsonType)) {
                return value;
            } else {
                // Has to be a numeric conversion.
                return TypeConverter.convertValues(targetJsonType, value);
            }
        }

        return convert((RefValue) value, targetJsonType, new ArrayList<>(), strand);
    }

    private static Object convert(RefValue value, BType targetType, List<TypeValuePair> unresolvedValues,
                                  Strand strand) {
        TypeValuePair typeValuePair = new TypeValuePair(value, targetType);

        if (unresolvedValues.contains(typeValuePair)) {
            throw new BallerinaException(CONSTRUCT_FROM_CYCLIC_VALUE_REFERENCE_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CYCLIC_VALUE_REFERENCE, value.getType()));
        }

        unresolvedValues.add(typeValuePair);

        Object newValue;
        switch (value.getType().getTag()) {
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                newValue = convertMapToJson((MapValue<?, ?>) value, targetType, unresolvedValues, strand);
                break;
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
                newValue = convertArrayToJson((ArrayValue) value, targetType, unresolvedValues, strand);
                break;
            case TypeTags.XML_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TEXT_TAG:
                newValue = ToString.toString(strand, value);
                break;
            case TypeTags.TABLE_TAG:
                try {
                    newValue = JSONUtils.toJSON((TableValueImpl) value);
                } catch (Exception e) {
                    throw  createConversionError(value, targetType, e.getMessage());
                }
                break;
            case TypeTags.ERROR_TAG:
                newValue = value.copy(new HashMap<>());
                break;
            default:
                // should never reach here
                throw BallerinaErrors.createConversionError(value, targetType);
        }

        unresolvedValues.remove(typeValuePair);
        return newValue;
    }

    private static Object convert(Object value, BType targetType, List<TypeValuePair> unresolvedValues,
                                  boolean allowAmbiguity, Strand strand) {
        if (value == null) {
            if (targetType.isNilable()) {
                return null;
            }
            return createError(CONSTRUCT_FROM_CONVERSION_ERROR,
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CANNOT_CONVERT_NIL, targetType));
        }
        List<BType> convertibleTypes;
        convertibleTypes = getJsonConvertibleTypes(value);
        if (convertibleTypes.isEmpty()) {
            throw createConversionError(value, targetType);
        } else if (!allowAmbiguity && convertibleTypes.size() > 1) {
            throw createConversionError(value, targetType, AMBIGUOUS_TARGET);
        }

        BType sourceType = TypeChecker.getType(value);
        BType matchingType = convertibleTypes.get(0);
        // handle primitive values
        if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG) {
            if (TypeChecker.checkIsType(value, matchingType)) {
                return value;
            } else {
                // Has to be a numeric conversion.
                return TypeConverter.convertValues(matchingType, value);
            }
        }

        return convert((RefValue) value, matchingType, unresolvedValues, strand);
    }

    private static Object convertMapToJson(MapValue<?, ?> map, BType targetType, List<TypeValuePair> unresolvedValues,
                                           Strand strand) {
        switch (targetType.getTag()) {
            case TypeTags.MAP_TAG:
                MapValueImpl<BString, Object> newMap = new MapValueImpl<>(targetType);
                for (Map.Entry entry : map.entrySet()) {
                    BType constraintType = ((BMapType) targetType).getConstrainedType();
                    putToMap(newMap, entry, constraintType, unresolvedValues, strand);
                }
                return newMap;
            case TypeTags.JSON_TAG:
                BType matchingType = resolveMatchingTypeForJson(map);
                return convert(map, matchingType, unresolvedValues, strand);
            default:
                break;
        }

        // should never reach here
        throw BallerinaErrors.createConversionError(map, targetType);
    }

    private static Object convertArrayToJson(ArrayValue array, BType targetType, List<TypeValuePair> unresolvedValues,
                                             Strand strand) {
        ArrayValueImpl newArray = new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < array.size(); i++) {
            Object newValue = convert(array.get(i), BTypes.typeJSON, unresolvedValues, false, strand);
            newArray.add(i, newValue);
        }
        return newArray;
    }

    private static boolean isConvertibleToJson(Object sourceValue, List<TypeValuePair> unresolvedValues) {

        BType sourceType = TypeChecker.getType(sourceValue);

        if (TypeChecker.checkIsLikeType(sourceValue, BTypes.typeJSON, true)) {
            return true;
        }

        if (sourceType.getTag() == TypeTags.ARRAY_TAG) {
            ArrayValue source = (ArrayValue) sourceValue;
            BType elementType = ((BArrayType) source.getType()).getElementType();
            if (BTypes.isValueType(elementType)) {
                return TypeChecker.checkIsType(elementType, BTypes.typeJSON, new ArrayList<>());
            }

            Object[] arrayValues = source.getValues();
            for (int i = 0; i < ((ArrayValue) sourceValue).size(); i++) {
                if (!isConvertibleToJson(arrayValues[i], unresolvedValues)) {
                    return false;
                }
            }
            return true;
        } else if (sourceType.getTag() == TypeTags.MAP_TAG) {
            for (Object value : ((MapValueImpl) sourceValue).values()) {
                if (!isConvertibleToJson(value, unresolvedValues)) {
                    return false;
                }
            }
            return true;
        } else if (sourceType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            TypeValuePair typeValuePair = new TypeValuePair(sourceValue, BTypes.typeJSON);
            if (unresolvedValues.contains(typeValuePair)) {
                return true;
            }
            unresolvedValues.add(typeValuePair);
            for (Object object : ((MapValueImpl) sourceValue).values()) {
                if (!isConvertibleToJson(object, unresolvedValues)) {
                    return false;
                }
            }
            return true;
        } else if (sourceType.getTag() == TypeTags.TABLE_TAG) {
            return true;
        } else if ((sourceType.getTag() >= TypeTags.XML_ELEMENT_TAG && sourceType.getTag() <= TypeTags.XML_TEXT_TAG) ||
                sourceType.getTag() == TypeTags.XML_TAG) {
            return true;
        }

        return false;
    }

    private static List<BType> getJsonConvertibleTypes(Object value) {
        List<BType> convertibleTypes = new ArrayList<>();

        if (isConvertibleToJson(value, new ArrayList<>())) {

            convertibleTypes.addAll(TypeConverter.getConvertibleTypes(value, BTypes.typeJSON));

            if (convertibleTypes.size() == 0) {
                switch (TypeChecker.getType(value).getTag()) {
                    case TypeTags.XML_TAG:
                    case TypeTags.XML_ELEMENT_TAG:
                    case TypeTags.XML_COMMENT_TAG:
                    case TypeTags.XML_PI_TAG:
                    case TypeTags.XML_TEXT_TAG:
                    case TypeTags.ARRAY_TAG:
                        convertibleTypes.add(BTypes.typeJSON);
                        break;
                    case TypeTags.RECORD_TYPE_TAG:
                    case TypeTags.MAP_TAG:
                        convertibleTypes.add(new BMapType(BTypes.typeJSON));
                        break;
                    case TypeTags.TABLE_TAG:
                    default:
                        break;
                }
            }
        }
        return convertibleTypes;
    }

    private static BType resolveMatchingTypeForJson(Object value) {

        BType matchingType = TypeConverter.resolveMatchingTypeForUnion(value, BTypes.typeJSON);

        if (matchingType == null) {
            switch (TypeChecker.getType(value).getTag()) {
                case TypeTags.ARRAY_TAG:
                    return BTypes.typeJSON;
                case TypeTags.RECORD_TYPE_TAG:
                case TypeTags.MAP_TAG:
                    return new BMapType(BTypes.typeJSON);
                case TypeTags.TABLE_TAG:
                default:
                    break;
            }
            // should not come here
            throw createConversionError(value, BTypes.typeJSON);
        } else {
            if (matchingType.getTag() == TypeTags.MAP_TAG &&
                    ((BMapType) matchingType).getConstrainedType().getTag() != TypeTags.JSON_TAG) {
                matchingType = new BMapType(BTypes.typeJSON);
            }
        }
        return matchingType;
    }

    private static void putToMap(MapValue<BString, Object> map, Map.Entry entry, BType fieldType,
                                 List<TypeValuePair> unresolvedValues, Strand strand) {
        Object newValue = convert(entry.getValue(), fieldType, unresolvedValues, true, strand);
        map.put(StringUtils.fromString(entry.getKey().toString()), newValue);
    }

    private static ErrorValue createConversionError(Object inputValue, BType targetType) {
        return createError(StringUtils.fromString(CONSTRUCT_FROM_CONVERSION_ERROR), StringUtils.fromString(
                BLangExceptionHelper.getErrorMessage(INCOMPATIBLE_CONVERT_OPERATION,
                        TypeChecker.getType(inputValue), targetType)));
    }

    private static ErrorValue createConversionError(Object inputValue, BType targetType, String detailMessage) {
        return createError(StringUtils.fromString(CONSTRUCT_FROM_CONVERSION_ERROR),
                StringUtils.fromString(BLangExceptionHelper.getErrorMessage(
                        INCOMPATIBLE_CONVERT_OPERATION, TypeChecker.getType(inputValue), targetType)
                        .concat(": ".concat(detailMessage))));
    }
}
