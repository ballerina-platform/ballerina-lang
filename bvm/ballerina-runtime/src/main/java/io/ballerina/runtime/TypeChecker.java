/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.ArrayType.ArrayState;
import io.ballerina.runtime.api.types.AttachedFunctionType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.commons.TypeValuePair;
import io.ballerina.runtime.internal.ErrorUtils;
import io.ballerina.runtime.types.BAnnotatableType;
import io.ballerina.runtime.types.BAnydataType;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.types.BErrorType;
import io.ballerina.runtime.types.BField;
import io.ballerina.runtime.types.BFiniteType;
import io.ballerina.runtime.types.BFunctionType;
import io.ballerina.runtime.types.BFutureType;
import io.ballerina.runtime.types.BIntersectionType;
import io.ballerina.runtime.types.BJSONType;
import io.ballerina.runtime.types.BMapType;
import io.ballerina.runtime.types.BObjectType;
import io.ballerina.runtime.types.BRecordType;
import io.ballerina.runtime.types.BStreamType;
import io.ballerina.runtime.types.BTableType;
import io.ballerina.runtime.types.BTupleType;
import io.ballerina.runtime.types.BType;
import io.ballerina.runtime.types.BTypeIdSet;
import io.ballerina.runtime.types.BTypedescType;
import io.ballerina.runtime.types.BUnionType;
import io.ballerina.runtime.types.BXMLType;
import io.ballerina.runtime.util.Flags;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.DecimalValue;
import io.ballerina.runtime.values.ErrorValue;
import io.ballerina.runtime.values.HandleValue;
import io.ballerina.runtime.values.MapValue;
import io.ballerina.runtime.values.MapValueImpl;
import io.ballerina.runtime.values.RefValue;
import io.ballerina.runtime.values.StreamValue;
import io.ballerina.runtime.values.TableValueImpl;
import io.ballerina.runtime.values.TypedescValue;
import io.ballerina.runtime.values.TypedescValueImpl;
import io.ballerina.runtime.values.XMLSequence;
import io.ballerina.runtime.values.XMLText;
import io.ballerina.runtime.values.XMLValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.runtime.api.PredefinedTypes.TYPE_ANY;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_ANYDATA;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_BOOLEAN;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_BYTE;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_DECIMAL;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_FLOAT;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_INT;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_INT_SIGNED_16;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_INT_SIGNED_32;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_INT_SIGNED_8;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_INT_UNSIGNED_16;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_INT_UNSIGNED_32;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_INT_UNSIGNED_8;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_JSON;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_NULL;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_READONLY_JSON;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_STRING;
import static io.ballerina.runtime.api.PredefinedTypes.isValueType;
import static io.ballerina.runtime.util.BLangConstants.BBYTE_MAX_VALUE;
import static io.ballerina.runtime.util.BLangConstants.BBYTE_MIN_VALUE;
import static io.ballerina.runtime.util.BLangConstants.SIGNED16_MAX_VALUE;
import static io.ballerina.runtime.util.BLangConstants.SIGNED16_MIN_VALUE;
import static io.ballerina.runtime.util.BLangConstants.SIGNED32_MAX_VALUE;
import static io.ballerina.runtime.util.BLangConstants.SIGNED32_MIN_VALUE;
import static io.ballerina.runtime.util.BLangConstants.SIGNED8_MAX_VALUE;
import static io.ballerina.runtime.util.BLangConstants.SIGNED8_MIN_VALUE;
import static io.ballerina.runtime.util.BLangConstants.UNSIGNED16_MAX_VALUE;
import static io.ballerina.runtime.util.BLangConstants.UNSIGNED32_MAX_VALUE;
import static io.ballerina.runtime.util.BLangConstants.UNSIGNED8_MAX_VALUE;

/**
 * Responsible for performing runtime type checking.
 *
 * @since 0.995.0
 */
@SuppressWarnings({"rawtypes"})
public class TypeChecker {

    public static Object checkCast(Object sourceVal, Type targetType) {

        if (checkIsType(sourceVal, targetType)) {
            return sourceVal;
        }

        Type sourceType = getType(sourceVal);
        if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG && targetType.getTag() <= TypeTags.BOOLEAN_TAG) {
            return TypeConverter.castValues(targetType, sourceVal);
        }

        // if the source is a numeric value and the target type is a union, try to find a matching
        // member.
        if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG && targetType.getTag() == TypeTags.UNION_TAG) {
            for (Type memberType : ((BUnionType) targetType).getMemberTypes()) {
                try {
                    return TypeConverter.castValues(memberType, sourceVal);
                } catch (Exception e) {
                    //ignore and continue
                }
            }
        }

        throw ErrorUtils.createTypeCastError(sourceVal, targetType);
    }

    public static long anyToInt(Object sourceVal) {
        return TypeConverter.anyToIntCast(sourceVal,
                () -> ErrorUtils.createTypeCastError(sourceVal, TYPE_INT));
    }

    public static long anyToSigned32(Object sourceVal) {
        return TypeConverter.anyToIntSubTypeCast(sourceVal, TYPE_INT_SIGNED_32,
                                                 () -> ErrorUtils.createTypeCastError(sourceVal,
                                                                                      TYPE_INT_SIGNED_32));
    }

    public static long anyToSigned16(Object sourceVal) {
        return TypeConverter.anyToIntSubTypeCast(sourceVal, TYPE_INT_SIGNED_16,
                                                 () -> ErrorUtils.createTypeCastError(sourceVal,
                                                                                      TYPE_INT_SIGNED_16));
    }

    public static long anyToSigned8(Object sourceVal) {
        return TypeConverter.anyToIntSubTypeCast(sourceVal, TYPE_INT_SIGNED_8,
                                                 () -> ErrorUtils.createTypeCastError(sourceVal,
                                                                                      TYPE_INT_SIGNED_8));
    }

    public static long anyToUnsigned32(Object sourceVal) {
        return TypeConverter.anyToIntSubTypeCast(sourceVal, TYPE_INT_UNSIGNED_32,
                                                 () -> ErrorUtils.createTypeCastError(sourceVal,
                                                                                      TYPE_INT_UNSIGNED_32));
    }

    public static long anyToUnsigned16(Object sourceVal) {
        return TypeConverter.anyToIntSubTypeCast(sourceVal, TYPE_INT_UNSIGNED_16,
                                                 () -> ErrorUtils
                                                         .createTypeCastError(sourceVal, TYPE_INT_UNSIGNED_16));
    }

    public static long anyToUnsigned8(Object sourceVal) {
        return TypeConverter.anyToIntSubTypeCast(sourceVal, TYPE_INT_UNSIGNED_8,
                                                 () -> ErrorUtils
                                                         .createTypeCastError(sourceVal,
                                                                              TYPE_INT_UNSIGNED_8));
    }

    public static double anyToFloat(Object sourceVal) {
        return TypeConverter.anyToFloatCast(sourceVal, () -> ErrorUtils
                .createTypeCastError(sourceVal, TYPE_FLOAT));
    }

    public static boolean anyToBoolean(Object sourceVal) {
        return TypeConverter.anyToBooleanCast(sourceVal, () -> ErrorUtils
                .createTypeCastError(sourceVal, TYPE_BOOLEAN));
    }

    public static int anyToByte(Object sourceVal) {
        return TypeConverter.anyToByteCast(sourceVal, () -> ErrorUtils.createTypeCastError(sourceVal,
                                                                                           TYPE_BYTE));
    }

    public static DecimalValue anyToDecimal(Object sourceVal) {
        return TypeConverter.anyToDecimal(sourceVal, () -> ErrorUtils.createTypeCastError(sourceVal,
                                                                                          TYPE_DECIMAL));
    }

    public static byte anyToJByte(Object sourceVal) {
        return TypeConverter.anyToJByteCast(sourceVal,
                () -> ErrorUtils.createBToJTypeCastError(sourceVal, "byte"));
    }

    public static char anyToJChar(Object sourceVal) {
        return TypeConverter.anyToJCharCast(sourceVal,
                () -> ErrorUtils.createBToJTypeCastError(sourceVal, "char"));
    }

    public static short anyToJShort(Object sourceVal) {
        return TypeConverter.anyToJShortCast(sourceVal,
                () -> ErrorUtils.createBToJTypeCastError(sourceVal, "short"));
    }

    public static int anyToJInt(Object sourceVal) {
        return TypeConverter.anyToJIntCast(sourceVal,
                () -> ErrorUtils.createBToJTypeCastError(sourceVal, "int"));
    }

    public static long anyToJLong(Object sourceVal) {
        return TypeConverter.anyToJLongCast(sourceVal,
                () -> ErrorUtils.createBToJTypeCastError(sourceVal, "long"));
    }

    public static float anyToJFloat(Object sourceVal) {
        return TypeConverter.anyToJFloatCast(sourceVal,
                () -> ErrorUtils.createBToJTypeCastError(sourceVal, "float"));
    }

    public static double anyToJDouble(Object sourceVal) {
        return TypeConverter.anyToJDoubleCast(sourceVal,
                () -> ErrorUtils.createBToJTypeCastError(sourceVal, "double"));
    }

    public static boolean anyToJBoolean(Object sourceVal) {
        return TypeConverter.anyToJBooleanCast(sourceVal,
                () -> ErrorUtils.createBToJTypeCastError(sourceVal, "boolean"));
    }

    /**
     * Check whether a given value belongs to the given type.
     *
     * @param sourceVal value to check the type
     * @param targetType type to be test against
     * @return true if the value belongs to the given type, false otherwise
     */
    public static boolean checkIsType(Object sourceVal, Type targetType) {
        return checkIsType(sourceVal, getType(sourceVal), targetType);
    }

    /**
     * Check whether a given value belongs to the given type.
     *
     * @param sourceVal value to check the type
     * @param sourceType type of the value
     * @param targetType type to be test against
     * @return true if the value belongs to the given type, false otherwise
     */
    public static boolean checkIsType(Object sourceVal, Type sourceType, Type targetType) {
        if (checkIsType(sourceVal, sourceType, targetType, new ArrayList<>())) {
            return true;
        }

        if (sourceType.getTag() == TypeTags.XML_TAG) {
            XMLValue val = (XMLValue) sourceVal;
            if (val.getNodeType() == XMLNodeType.SEQUENCE) {
                return checkIsLikeOnValue(sourceVal, sourceType, targetType, new ArrayList<>(), false);
            }
        }

        if (isMutable(sourceVal, sourceType)) {
            return false;
        }

        return checkIsLikeOnValue(sourceVal, sourceType, targetType, new ArrayList<>(), false);
    }

    /**
     * Check whether a given value has the same shape as the given type.
     *
     * @param sourceValue value to check the shape
     * @param targetType type to check the shape against
     * @return true if the value has the same shape as the given type; false otherwise
     */
    public static boolean checkIsLikeType(Object sourceValue, Type targetType) {
        return checkIsLikeType(sourceValue, targetType, false);
    }

    /**
     * Check whether a given value has the same shape as the given type.
     *
     * @param sourceValue value to check the shape
     * @param targetType type to check the shape against
     * @param allowNumericConversion whether numeric conversion is allowed to change the shape to the target type
     * @return true if the value has the same shape as the given type; false otherwise
     */
    public static boolean checkIsLikeType(Object sourceValue, Type targetType, boolean allowNumericConversion) {
        return checkIsLikeType(sourceValue, targetType, new ArrayList<>(), allowNumericConversion);
    }

    /**
     * Check whether two types are the same.
     *
     * @param sourceType type to test
     * @param targetType type to test against
     * @return true if the two types are same; false otherwise
     */
    public static boolean isSameType(Type sourceType, Type targetType) {
        // First check whether both references points to the same object.
        if (sourceType == targetType || sourceType.equals(targetType)) {
            return true;
        }

        if (sourceType.getTag() == targetType.getTag() && sourceType.getTag() == TypeTags.ARRAY_TAG) {
            return checkArrayEquivalent(sourceType, targetType);
        }

        // TODO Support function types, json/map constrained types etc.
        if (sourceType.getTag() == TypeTags.MAP_TAG && targetType.getTag() == TypeTags.MAP_TAG) {
            return targetType.equals(sourceType);
        }

        if (sourceType.getTag() == TypeTags.STREAM_TAG && targetType.getTag() == TypeTags.STREAM_TAG) {
            return targetType.equals(sourceType);
        }

        if (sourceType.getTag() == TypeTags.FINITE_TYPE_TAG && targetType.getTag() == TypeTags.FINITE_TYPE_TAG) {
            // value space should be same
            Set<Object> sourceValueSpace = ((BFiniteType) sourceType).valueSpace;
            Set<Object> targetValueSpace = ((BFiniteType) targetType).valueSpace;
            if (sourceValueSpace.size() != targetValueSpace.size()) {
                return false;
            }

            for (Object sourceVal : sourceValueSpace) {
                if (!containsType(targetValueSpace, getType(sourceVal))) {
                    return false;
                }
            }
            return true;
        }

        // all the types in a finite type may evaluate to target type
        if (sourceType.getTag() == TypeTags.FINITE_TYPE_TAG) {
            for (Object value : ((BFiniteType) sourceType).valueSpace) {
                if (!isSameType(getType(value), targetType)) {
                    return false;
                }
            }
            return true;
        }

        if (targetType.getTag() == TypeTags.FINITE_TYPE_TAG) {
            for (Object value : ((BFiniteType) targetType).valueSpace) {
                if (!isSameType(getType(value), sourceType)) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    public static Type getType(Object value) {
        if (value == null) {
            return TYPE_NULL;
        } else if (value instanceof Number) {
            if (value instanceof Long) {
                return TYPE_INT;
            } else if (value instanceof Double) {
                return TYPE_FLOAT;
            } else if (value instanceof Integer || value instanceof Byte) {
                return TYPE_BYTE;
            }
        } else if (value instanceof String || value instanceof BString) {
            return TYPE_STRING;
        } else if (value instanceof Boolean) {
            return TYPE_BOOLEAN;
        }

        return ((BValue) value).getType();
    }

    /**
     * Deep value equality check for anydata.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value on the right hand side
     * @return True if values are equal, else false.
     */
    public static boolean isEqual(Object lhsValue, Object rhsValue) {
        return isEqual(lhsValue, rhsValue, new ArrayList<>());
    }

    /**
     * Check if two decimal values are equal in value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value of the right hand side
     * @return True if values are equal, else false.
     */
    public static boolean checkDecimalEqual(DecimalValue lhsValue, DecimalValue rhsValue) {
        return isDecimalRealNumber(lhsValue) && isDecimalRealNumber(rhsValue) &&
               lhsValue.decimalValue().compareTo(rhsValue.decimalValue()) == 0;
    }

    /**
     * Check if left hand side decimal value is less than the right hand side decimal value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value of the right hand side
     * @return True if left hand value is less than right hand side value, else false.
     */
    public static boolean checkDecimalLessThan(DecimalValue lhsValue, DecimalValue rhsValue) {
        return !checkDecimalEqual(lhsValue, rhsValue) && checkDecimalGreaterThanOrEqual(rhsValue, lhsValue);
    }

    /**
     * Check if left hand side decimal value is less than or equal the right hand side decimal value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value of the right hand side
     * @return True if left hand value is less than or equal right hand side value, else false.
     */
    public static boolean checkDecimalLessThanOrEqual(DecimalValue lhsValue, DecimalValue rhsValue) {
        return checkDecimalEqual(lhsValue, rhsValue) || checkDecimalGreaterThan(rhsValue, lhsValue);
    }

    /**
     * Check if left hand side decimal value is greater than the right hand side decimal value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value of the right hand side
     * @return True if left hand value is greater than right hand side value, else false.
     */
    public static boolean checkDecimalGreaterThan(DecimalValue lhsValue, DecimalValue rhsValue) {
        switch (lhsValue.valueKind) {
            case POSITIVE_INFINITY:
                return isDecimalRealNumber(rhsValue) || rhsValue.valueKind == DecimalValueKind.NEGATIVE_INFINITY;
            case ZERO:
            case OTHER:
                return rhsValue.valueKind == DecimalValueKind.NEGATIVE_INFINITY || (isDecimalRealNumber(rhsValue) &&
                        lhsValue.decimalValue().compareTo(rhsValue.decimalValue()) > 0);
            default:
                return false;
        }
    }

    /**
     * Check if left hand side decimal value is greater than or equal the right hand side decimal value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value of the right hand side
     * @return True if left hand value is greater than or equal right hand side value, else false.
     */
    public static boolean checkDecimalGreaterThanOrEqual(DecimalValue lhsValue, DecimalValue rhsValue) {
        return checkDecimalGreaterThan(lhsValue, rhsValue) ||
               (isDecimalRealNumber(lhsValue) && isDecimalRealNumber(rhsValue) &&
                lhsValue.decimalValue().compareTo(rhsValue.decimalValue()) == 0);
    }

    /**
     * Checks if the given decimal number is a real number.
     *
     * @param decimalValue The decimal value being checked
     * @return True if the decimal value is a real number.
     */
    private static boolean isDecimalRealNumber(DecimalValue decimalValue) {
        return decimalValue.valueKind == DecimalValueKind.ZERO || decimalValue.valueKind == DecimalValueKind.OTHER;
    }

    /**
     * Reference equality check for values. If both the values are simple basic types, returns the same
     * result as {@link #isEqual(Object, Object, List)}
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value on the right hand side
     * @return True if values are reference equal or in the case of simple basic types if the values are equal,
     *         else false.
     */
    public static boolean isReferenceEqual(Object lhsValue, Object rhsValue) {
        if (lhsValue == rhsValue) {
            return true;
        }

        // if one is null, the other also needs to be null to be true
        if (lhsValue == null || rhsValue == null) {
            return false;
        }

        Type lhsType = getType(lhsValue);
        Type rhsType = getType(rhsValue);

        if (isSimpleBasicType(lhsType) && isSimpleBasicType(rhsType)) {
            return isEqual(lhsValue, rhsValue);
        }

        if (lhsType.getTag() == TypeTags.XML_TAG && rhsType.getTag() == TypeTags.XML_TAG) {
            return isXMLValueRefEqual((XMLValue) lhsValue, (XMLValue) rhsValue);
        }

        if (isHandleType(lhsType) && isHandleType(rhsType)) {
            return isHandleValueRefEqual(lhsValue, rhsValue);
        }

        return false;
    }

    private static boolean isXMLValueRefEqual(XMLValue lhsValue, XMLValue rhsValue) {
        if (lhsValue.getNodeType() != rhsValue.getNodeType()) {
            return false;
        }

        if (lhsValue.getNodeType() == XMLNodeType.SEQUENCE && rhsValue.getNodeType() == XMLNodeType.SEQUENCE) {
            return isXMLSequenceRefEqual((XMLSequence) lhsValue, (XMLSequence) rhsValue);
        }

        if (lhsValue.getNodeType() == XMLNodeType.TEXT && rhsValue.getNodeType() == XMLNodeType.TEXT) {
            return ((XMLText) lhsValue).equals(rhsValue);
        }
        return false;
    }

    private static boolean isXMLSequenceRefEqual(XMLSequence lhsValue, XMLSequence rhsValue) {
        Iterator<BXML> lhsIter = lhsValue.getChildrenList().iterator();
        Iterator<BXML> rhsIter = rhsValue.getChildrenList().iterator();
        while (lhsIter.hasNext() && rhsIter.hasNext()) {
            BXML l = lhsIter.next();
            BXML r = rhsIter.next();
            if (!(l == r || isXMLValueRefEqual((XMLValue) l, (XMLValue) r))) {
                return false;
            }
        }
        // lhs hasNext = false & rhs hasNext = false -> empty sequences, hence ref equal
        // lhs hasNext = true & rhs hasNext = true would never reach here
        // only one hasNext method returns true means requences are of different sizes, hence not ref equal
        return lhsIter.hasNext() == rhsIter.hasNext();
    }

    /**
     * Get the typedesc of a value.
     *
     * @param value Value
     * @return type desc associated with the value
     */
    public static TypedescValue getTypedesc(Object value) {
        Type type = TypeChecker.getType(value);
        if (type == null) {
            return null;
        }
        if (value instanceof MapValue) {
            TypedescValue typedesc = (TypedescValue) ((MapValue) value).getTypedesc();
            if (typedesc != null) {
                return typedesc;
            }
        }
        return new TypedescValueImpl(type);
    }

    /**
     * Get the annotation value if present.
     *
     * @param typedescValue     The typedesc value
     * @param annotTag          The annot-tag-reference
     * @return the annotation value if present, nil else
     */
    public static Object getAnnotValue(TypedescValue typedescValue, String annotTag) {
        Type describingType = typedescValue.getDescribingType();
        if (!(describingType instanceof BAnnotatableType)) {
            return null;
        }
        return ((BAnnotatableType) describingType).getAnnotation(StringUtils.fromString(annotTag));
    }

    public static Object getAnnotValue(TypedescValue typedescValue, BString annotTag) {
        Type describingType = typedescValue.getDescribingType();
        if (!(describingType instanceof BAnnotatableType)) {
            return null;
        }
        return ((BAnnotatableType) describingType).getAnnotation(annotTag);
    }

    /**
     * Check whether a given type is equivalent to a target type.
     *
     * @param sourceType type to check
     * @param targetType type to compare with
     * @return flag indicating the the equivalence of the two types
     */
    public static boolean checkIsType(Type sourceType, Type targetType) {
        return checkIsType(sourceType, targetType, (List<TypePair>) null);
    }

    @Deprecated
    public static boolean checkIsType(Type sourceType, Type targetType, List<TypePair> unresolvedTypes) {
        // First check whether both types are the same.
        if (sourceType == targetType || sourceType.equals(targetType)) {
            return true;
        }

        if (targetType.isReadOnly() && !sourceType.isReadOnly()) {
            return false;
        }

        int sourceTypeTag = sourceType.getTag();
        int targetTypeTag = targetType.getTag();

        if (sourceTypeTag == TypeTags.INTERSECTION_TAG) {
            return checkIsType(((BIntersectionType) sourceType).getEffectiveType(),
                               targetTypeTag != TypeTags.INTERSECTION_TAG ? targetType :
                                       ((BIntersectionType) targetType).getEffectiveType(), unresolvedTypes);
        }

        if (targetTypeTag == TypeTags.INTERSECTION_TAG) {
            return checkIsType(sourceType, ((BIntersectionType) targetType).getEffectiveType(), unresolvedTypes);
        }

        switch (targetTypeTag) {
            case TypeTags.BYTE_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.STRING_TAG:
            case TypeTags.XML_TEXT_TAG:
            case TypeTags.CHAR_STRING_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.NULL_TAG:
                if (sourceTypeTag == TypeTags.FINITE_TYPE_TAG) {
                    return isFiniteTypeMatch((BFiniteType) sourceType, targetType);
                }
                return sourceTypeTag == targetTypeTag;
            case TypeTags.INT_TAG:
                if (sourceTypeTag == TypeTags.FINITE_TYPE_TAG) {
                    return isFiniteTypeMatch((BFiniteType) sourceType, targetType);
                }
                return sourceTypeTag == TypeTags.BYTE_TAG || sourceTypeTag == TypeTags.INT_TAG;
            case TypeTags.ANY_TAG:
                return checkIsAnyType(sourceType);
            case TypeTags.ANYDATA_TAG:
                return sourceType.isAnydata();
            case TypeTags.SERVICE_TAG:
                return checkIsServiceType(sourceType);
            case TypeTags.HANDLE_TAG:
                return sourceTypeTag == TypeTags.HANDLE_TAG;
            case TypeTags.READONLY_TAG:
                return isInherentlyImmutableType(sourceType) || sourceType.isReadOnly();
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
                return targetTypeTag == sourceTypeTag;
            default:
                return checkIsRecursiveType(sourceType, targetType,
                        unresolvedTypes == null ? new ArrayList<>() : unresolvedTypes);
        }
    }

    private static boolean checkIsType(Object sourceVal, Type sourceType, Type targetType,
                                      List<TypePair> unresolvedTypes) {
        int sourceTypeTag = sourceType.getTag();
        int targetTypeTag = targetType.getTag();

        // If the source type is neither a record type nor an object type, check `is` type by looking only at the types.
        // Else, since records and objects may have `readonly` or `final` fields, need to use the value also.
        // e.g.,
        //      const HUNDRED = 100;
        //
        //      type Foo record {
        //          HUNDRED i;
        //      };
        //
        //      type Bar record {
        //          readonly string|int i;
        //      };
        //
        // where `Bar b = {i: 100};`, `b is Foo` should evaluate to true.
        if (sourceTypeTag != TypeTags.RECORD_TYPE_TAG && sourceTypeTag != TypeTags.OBJECT_TYPE_TAG) {
            return checkIsType(sourceType, targetType);
        }

        if (targetTypeTag == TypeTags.INTERSECTION_TAG) {
            targetType = ((BIntersectionType) targetType).getEffectiveType();
            targetTypeTag = targetType.getTag();
        }

        if (sourceType == targetType || sourceType.equals(targetType)) {
            return true;
        }

        if (targetType.isReadOnly() && !sourceType.isReadOnly()) {
            return false;
        }

        switch (targetTypeTag) {
            case TypeTags.ANY_TAG:
                return checkIsAnyType(sourceType);
            case TypeTags.ANYDATA_TAG:
                if (sourceTypeTag == TypeTags.OBJECT_TYPE_TAG) {
                    return false;
                }
                return checkRecordBelongsToAnydataType((MapValue) sourceVal, (BRecordType) sourceType, unresolvedTypes);
            case TypeTags.READONLY_TAG:
                return isInherentlyImmutableType(sourceType) || sourceType.isReadOnly();
            case TypeTags.MAP_TAG:
                return checkIsMapType(sourceVal, sourceType, (BMapType) targetType, unresolvedTypes);
            case TypeTags.JSON_TAG:
                return checkIsMapType(sourceVal, sourceType,
                                      new BMapType(targetType.isReadOnly() ? TYPE_READONLY_JSON :
                                                           TYPE_JSON), unresolvedTypes);
            case TypeTags.RECORD_TYPE_TAG:
                return checkIsRecordType(sourceVal, sourceType, (BRecordType) targetType, unresolvedTypes);
            case TypeTags.UNION_TAG:
                for (Type type : ((BUnionType) targetType).getMemberTypes()) {
                    if (checkIsType(sourceVal, sourceType, type, unresolvedTypes)) {
                        return true;
                    }
                }
                return false;
            case TypeTags.OBJECT_TYPE_TAG:
                return checkObjectEquivalency(sourceVal, sourceType, (BObjectType) targetType, unresolvedTypes);
            default:
                return false;
        }
    }

    // Private methods

    private static boolean checkTypeDescType(Type sourceType, BTypedescType targetType,
            List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.TYPEDESC_TAG) {
            return false;
        }

        BTypedescType sourceTypedesc = (BTypedescType) sourceType;
        return checkIsType(sourceTypedesc.getConstraint(), targetType.getConstraint(), unresolvedTypes);
    }

    private static boolean checkIsRecursiveType(Type sourceType, Type targetType, List<TypePair> unresolvedTypes) {
        switch (targetType.getTag()) {
            case TypeTags.MAP_TAG:
                return checkIsMapType(sourceType, (BMapType) targetType, unresolvedTypes);
            case TypeTags.STREAM_TAG:
                return checkIsStreamType(sourceType, (BStreamType) targetType, unresolvedTypes);
            case TypeTags.TABLE_TAG:
                return checkIsTableType(sourceType, (BTableType) targetType, unresolvedTypes);
            case TypeTags.JSON_TAG:
                return checkIsJSONType(sourceType, unresolvedTypes);
            case TypeTags.RECORD_TYPE_TAG:
                return checkIsRecordType(sourceType, (BRecordType) targetType, unresolvedTypes);
            case TypeTags.FUNCTION_POINTER_TAG:
                return checkIsFunctionType(sourceType, (BFunctionType) targetType);
            case TypeTags.ARRAY_TAG:
                return checkIsArrayType(sourceType, (BArrayType) targetType, unresolvedTypes);
            case TypeTags.TUPLE_TAG:
                return checkIsTupleType(sourceType, (BTupleType) targetType, unresolvedTypes);
            case TypeTags.UNION_TAG:
                return checkIsUnionType(sourceType, (BUnionType) targetType, unresolvedTypes);
            case TypeTags.OBJECT_TYPE_TAG:
                return checkObjectEquivalency(sourceType, (BObjectType) targetType, unresolvedTypes);
            case TypeTags.FINITE_TYPE_TAG:
                return checkIsFiniteType(sourceType, (BFiniteType) targetType, unresolvedTypes);
            case TypeTags.FUTURE_TAG:
                return checkIsFutureType(sourceType, (BFutureType) targetType, unresolvedTypes);
            case TypeTags.ERROR_TAG:
                return checkIsErrorType(sourceType, (BErrorType) targetType, unresolvedTypes);
            case TypeTags.TYPEDESC_TAG:
                return checkTypeDescType(sourceType, (BTypedescType) targetType, unresolvedTypes);
            case TypeTags.XML_TAG:
                return checkIsXMLType(sourceType, targetType, unresolvedTypes);
            default:
                // other non-recursive types shouldn't reach here
                return false;
        }
    }

    private static boolean isFiniteTypeMatch(BFiniteType sourceType, Type targetType) {
        for (Object bValue : sourceType.valueSpace) {
            if (!checkIsType(bValue, targetType)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isUnionTypeMatch(BUnionType sourceType, Type targetType, List<TypePair> unresolvedTypes) {
        for (Type type : sourceType.getMemberTypes()) {
            if (!checkIsType(type, targetType, unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsUnionType(Type sourceType, BUnionType targetType, List<TypePair> unresolvedTypes) {
        switch (sourceType.getTag()) {
            case TypeTags.UNION_TAG:
                return isUnionTypeMatch((BUnionType) sourceType, targetType, unresolvedTypes);
            case TypeTags.FINITE_TYPE_TAG:
                return isFiniteTypeMatch((BFiniteType) sourceType, targetType);
            default:
                for (Type type : targetType.getMemberTypes()) {
                    if (checkIsType(sourceType, type, unresolvedTypes)) {
                        return true;
                    }
                }
                return false;

        }
    }

    private static boolean checkIsMapType(Type sourceType, BMapType targetType, List<TypePair> unresolvedTypes) {
        Type targetConstrainedType = targetType.getConstrainedType();
        switch (sourceType.getTag()) {
            case TypeTags.MAP_TAG:
                return checkConstraints(((BMapType) sourceType).getConstrainedType(), targetConstrainedType,
                        unresolvedTypes);
            case TypeTags.RECORD_TYPE_TAG:
                BRecordType recType = (BRecordType) sourceType;
                BUnionType wideTypeUnion = new BUnionType(getWideTypeComponents(recType));
                return checkConstraints(wideTypeUnion, targetConstrainedType, unresolvedTypes);
            default:
                return false;
        }
    }

    private static boolean checkIsMapType(Object sourceVal, Type sourceType, BMapType targetType,
                                          List<TypePair> unresolvedTypes) {
        Type targetConstrainedType = targetType.getConstrainedType();
        switch (sourceType.getTag()) {
            case TypeTags.MAP_TAG:
                return checkConstraints(((BMapType) sourceType).getConstrainedType(), targetConstrainedType,
                                        unresolvedTypes);
            case TypeTags.RECORD_TYPE_TAG:
                return checkIsMapType((MapValue) sourceVal, (BRecordType) sourceType, unresolvedTypes,
                                      targetConstrainedType);
            default:
                return false;
        }
    }

    private static boolean checkIsMapType(MapValue sourceVal, BRecordType sourceType, List<TypePair> unresolvedTypes,
                                          Type targetConstrainedType) {
        for (Field field : sourceType.getFields().values()) {
            if (!Flags.isFlagOn(field.getFlags(), Flags.READONLY)) {
                if (!checkIsType(field.getFieldType(), targetConstrainedType, unresolvedTypes)) {
                    return false;
                }
                continue;
            }

            BString name = StringUtils.fromString(field.getFieldName());

            if (Flags.isFlagOn(field.getFlags(), Flags.OPTIONAL) && !sourceVal.containsKey(name)) {
                continue;
            }

            if (!checkIsLikeType(sourceVal.get(name), targetConstrainedType)) {
                return false;
            }
        }

        if (sourceType.sealed) {
            return true;
        }

        return checkIsType(sourceType.restFieldType, targetConstrainedType, unresolvedTypes);
    }

    private static boolean checkIsXMLType(Type sourceType, Type targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() == TypeTags.FINITE_TYPE_TAG) {
            return isFiniteTypeMatch((BFiniteType) sourceType, targetType);
        }
        BXMLType target = ((BXMLType) targetType);
        if (sourceType.getTag() == TypeTags.XML_TAG) {
            Type targetConstraint = target.constraint;
            // TODO: Revisit and check why xml<xml<constraint>>> on chained iteration
            while (target.constraint.getTag() == TypeTags.XML_TAG) {
                target = (BXMLType) target.constraint;
                targetConstraint = target.constraint;
            }
            return checkIsType(((BXMLType) sourceType).constraint, targetConstraint,
                    unresolvedTypes);
        } else if (TypeTags.isXMLTypeTag(sourceType.getTag())) {
            return checkIsType(sourceType, target.constraint, unresolvedTypes);
        }
        return false;
    }

    private static List<Type> getWideTypeComponents(BRecordType recType) {
        List<Type> types = new ArrayList<>();
        for (Field f : recType.getFields().values()) {
            types.add(f.getFieldType());
        }
        if (!recType.sealed) {
            types.add(recType.restFieldType);
        }
        return types;
    }

    private static boolean checkIsStreamType(Type sourceType, BStreamType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.STREAM_TAG) {
            return false;
        }
        return checkConstraints(((BStreamType) sourceType).getConstrainedType(), targetType.getConstrainedType(),
                               unresolvedTypes);
    }

    private static boolean checkIsTableType(Type sourceType, BTableType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.TABLE_TAG) {
            return false;
        }

        BTableType srcTableType  = (BTableType) sourceType;

        if (!checkConstraints(srcTableType.getConstrainedType(), targetType.getConstrainedType(),
                unresolvedTypes)) {
            return false;
        }

        if (targetType.getKeyType() == null && targetType.getFieldNames() == null) {
            return true;
        }

        if (targetType.getKeyType() != null) {
            if (srcTableType.getKeyType() != null &&
                    (checkConstraints(srcTableType.getKeyType(), targetType.getKeyType(), unresolvedTypes))) {
                return true;
            }

            if (srcTableType.getFieldNames() == null) {
                return false;
            }

            List<Type> fieldTypes = new ArrayList<>();
            Arrays.stream(srcTableType.getFieldNames()).forEach(field -> fieldTypes
                    .add(Objects.requireNonNull(getTableConstraintField(srcTableType.getConstrainedType(), field))
                    .type));

            if (fieldTypes.size() == 1) {
                return checkConstraints(fieldTypes.get(0), targetType.getKeyType(), unresolvedTypes);
            }

            BTupleType tupleType = new BTupleType(fieldTypes);
            return checkConstraints(tupleType, targetType.getKeyType(), unresolvedTypes);
        }

        return Arrays.equals(srcTableType.getFieldNames(), targetType.getFieldNames());
    }

    static BField getTableConstraintField(Type constraintType, String fieldName) {

        switch (constraintType.getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
                Map<String, Field> fieldList = ((BRecordType) constraintType).getFields();
                return (BField) fieldList.get(fieldName);

            case TypeTags.UNION_TAG:
                BUnionType unionType = (BUnionType) constraintType;
                List<Type> memTypes = unionType.getMemberTypes();
                List<BField> fields = memTypes.stream().map(type -> getTableConstraintField(type, fieldName))
                        .filter(Objects::nonNull).collect(Collectors.toList());

                if (fields.size() != memTypes.size()) {
                    return null;
                }

                if (fields.stream().allMatch(field -> isSameType(field.type, fields.get(0).type))) {
                    return fields.get(0);
                }
        }

        return null;
    }

    private static boolean checkIsJSONType(Type sourceType, List<TypePair> unresolvedTypes) {
        BJSONType jsonType = (BJSONType) TYPE_JSON;

        // If we encounter two types that we are still resolving, then skip it.
        // This is done to avoid recursive checking of the same type.
        TypePair pair = new TypePair(sourceType, jsonType);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);

        switch (sourceType.getTag()) {
            case TypeTags.STRING_TAG:
            case TypeTags.CHAR_STRING_TAG:
            case TypeTags.INT_TAG:
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
            case TypeTags.BYTE_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.NULL_TAG:
            case TypeTags.JSON_TAG:
                return true;
            case TypeTags.ARRAY_TAG:
                // Element type of the array should be 'is type' JSON
                return checkIsType(((BArrayType) sourceType).getElementType(), jsonType, unresolvedTypes);
            case TypeTags.FINITE_TYPE_TAG:
                return isFiniteTypeMatch((BFiniteType) sourceType, jsonType);
            case TypeTags.MAP_TAG:
                return checkIsType(((BMapType) sourceType).getConstrainedType(), jsonType, unresolvedTypes);
            case TypeTags.RECORD_TYPE_TAG:
                BRecordType recordType = (BRecordType) sourceType;
                for (Field field : recordType.getFields().values()) {
                    if (!checkIsJSONType(field.getFieldType(), unresolvedTypes)) {
                        return false;
                    }
                }

                if (!recordType.sealed) {
                    return checkIsJSONType(recordType.restFieldType, unresolvedTypes);
                }
                return true;
            case TypeTags.UNION_TAG:
                for (Type memberType : ((BUnionType) sourceType).getMemberTypes()) {
                    if (!checkIsJSONType(memberType, unresolvedTypes)) {
                        return false;
                    }
                }
                return true;
            default:
                return false;
        }
    }

    private static boolean checkIsRecordType(Type sourceType, BRecordType targetType, List<TypePair> unresolvedTypes) {
        switch (sourceType.getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
                return checkIsRecordType((BRecordType) sourceType, targetType, unresolvedTypes);
            case TypeTags.MAP_TAG:
                return checkIsRecordType((BMapType) sourceType, targetType, unresolvedTypes);
        }
        return false;
    }

    private static boolean checkIsRecordType(BRecordType sourceRecordType, BRecordType targetType,
                                             List<TypePair> unresolvedTypes) {
        // If we encounter two types that we are still resolving, then skip it.
        // This is done to avoid recursive checking of the same type.
        TypePair pair = new TypePair(sourceRecordType, targetType);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);

        // Unsealed records are not equivalent to sealed records. But vice-versa is allowed.
        if (targetType.sealed && !sourceRecordType.sealed) {
            return false;
        }

        // If both are sealed (one is sealed means other is also sealed) check the rest field type
        if (!sourceRecordType.sealed &&
                !checkIsType(sourceRecordType.restFieldType, targetType.restFieldType, unresolvedTypes)) {
            return false;
        }

        Map<String, Field> sourceFields = sourceRecordType.getFields();
        Set<String> targetFieldNames = targetType.getFields().keySet();

        for (Field targetField : targetType.getFields().values()) {
            Field sourceField = sourceFields.get(targetField.getFieldName());

            if (sourceField == null) {
                return false;
            }

            if (hasIncompatibleReadOnlyFlags(targetField, sourceField)) {
                return false;
            }

            // If the target field is required, the source field should be required as well.
            if (!Flags.isFlagOn(targetField.getFlags(), Flags.OPTIONAL)
                    && Flags.isFlagOn(sourceField.getFlags(), Flags.OPTIONAL)) {
                return false;
            }

            if (!checkIsType(sourceField.getFieldType(), targetField.getFieldType(), unresolvedTypes)) {
                return false;
            }
        }

        // If there are fields remaining in the source record, first check if it's a closed record. Closed records
        // should only have the fields specified by its type.
        if (targetType.sealed) {
            return targetFieldNames.containsAll(sourceFields.keySet());
        }

        // If it's an open record, check if they are compatible with the rest field of the target type.
        for (Field field : sourceFields.values()) {
            if (targetFieldNames.contains(field.getFieldName())) {
                continue;
            }

            if (!checkIsType(field.getFieldType(), targetType.restFieldType, unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsRecordType(BMapType sourceType, BRecordType targetType,
                                             List<TypePair> unresolvedTypes) {
        // If we encounter two types that we are still resolving, then skip it.
        // This is done to avoid recursive checking of the same type.
        TypePair pair = new TypePair(sourceType, targetType);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);

        if (targetType.sealed) {
            return false;
        }

        Type constraintType = sourceType.getConstrainedType();

        for (Field field : targetType.getFields().values()) {
            int flags = field.getFlags();
            if (!Flags.isFlagOn(flags, Flags.OPTIONAL)) {
                return false;
            }

            if (Flags.isFlagOn(flags, Flags.READONLY) && !sourceType.isReadOnly()) {
                return false;
            }

            if (!checkIsType(constraintType, field.getFieldType(), unresolvedTypes)) {
                return false;
            }
        }

        return checkIsType(constraintType, targetType.restFieldType, unresolvedTypes);
    }

    private static boolean checkRecordBelongsToAnydataType(MapValue sourceVal, BRecordType recordType,
                                                           List<TypePair> unresolvedTypes) {
        Type targetType = TYPE_ANYDATA;
        TypePair pair = new TypePair(recordType, targetType);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);

        Map<String, Field> fields = recordType.getFields();

        for (Field field : fields.values()) {
            String fieldName = field.getFieldName();

            if (Flags.isFlagOn(field.getFlags(), Flags.READONLY)) {
                BString fieldNameBString = StringUtils.fromString(fieldName);

                if (Flags.isFlagOn(field.getFlags(), Flags.OPTIONAL) && !sourceVal.containsKey(fieldNameBString)) {
                    continue;
                }

                if (!checkIsLikeType(sourceVal.get(fieldNameBString), targetType)) {
                    return false;
                }
            } else {
                if (!checkIsType(field.getFieldType(), targetType, unresolvedTypes)) {
                    return false;
                }
            }
        }

        if (recordType.sealed) {
            return true;
        }

        return checkIsType(recordType.restFieldType, targetType, unresolvedTypes);
    }

    private static boolean checkIsRecordType(Object sourceVal, Type sourceType, BRecordType targetType,
                                             List<TypePair> unresolvedTypes) {
        switch (sourceType.getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
                return checkIsRecordType((MapValue) sourceVal, (BRecordType) sourceType, targetType, unresolvedTypes);
            case TypeTags.MAP_TAG:
                return checkIsRecordType((BMapType) sourceType, targetType, unresolvedTypes);
        }

        return false;
    }

    private static boolean checkIsRecordType(MapValue sourceRecordValue, BRecordType sourceRecordType,
                                             BRecordType targetType, List<TypePair> unresolvedTypes) {
        TypePair pair = new TypePair(sourceRecordType, targetType);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);

        if (targetType.sealed && !sourceRecordType.sealed) {
            return false;
        }

        if (!sourceRecordType.sealed &&
                !checkIsType(sourceRecordType.restFieldType, targetType.restFieldType, unresolvedTypes)) {
            return false;
        }

        Map<String, Field> sourceFields = sourceRecordType.getFields();
        Set<String> targetFieldNames = targetType.getFields().keySet();

        for (Field targetField : targetType.getFields().values()) {
            String fieldName = targetField.getFieldName();
            Field sourceField = sourceFields.get(fieldName);

            if (sourceField == null) {
                return false;
            }

            if (hasIncompatibleReadOnlyFlags(targetField, sourceField)) {
                return false;
            }

            boolean optionalTargetField = Flags.isFlagOn(targetField.getFlags(), Flags.OPTIONAL);
            boolean optionalSourceField = Flags.isFlagOn(sourceField.getFlags(), Flags.OPTIONAL);

            if (Flags.isFlagOn(sourceField.getFlags(), Flags.READONLY)) {
                BString fieldNameBString = StringUtils.fromString(fieldName);

                if (optionalSourceField && !sourceRecordValue.containsKey(fieldNameBString)) {
                    if (!optionalTargetField) {
                        return false;
                    }
                    continue;
                }

                if (!checkIsLikeType(sourceRecordValue.get(fieldNameBString), targetField.getFieldType())) {
                    return false;
                }
            } else {
                if (!optionalTargetField && optionalSourceField) {
                    return false;
                }

                if (!checkIsType(sourceField.getFieldType(), targetField.getFieldType(), unresolvedTypes)) {
                    return false;
                }
            }
        }

        if (targetType.sealed) {
            return targetFieldNames.containsAll(sourceFields.keySet());
        }

        for (Field field : sourceFields.values()) {
            if (targetFieldNames.contains(field.getFieldName())) {
                continue;
            }

            if (Flags.isFlagOn(field.getFlags(), Flags.READONLY)) {
                if (!checkIsLikeType(sourceRecordValue.get(StringUtils.fromString(field.getFieldName())),
                                     targetType.restFieldType)) {
                    return false;
                }
            } else if (!checkIsType(field.getFieldType(), targetType.restFieldType, unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasIncompatibleReadOnlyFlags(Field targetField, Field sourceField) {
        return Flags.isFlagOn(targetField.getFlags(), Flags.READONLY) && !Flags.isFlagOn(sourceField.getFlags(),
                                                                                         Flags.READONLY);
    }

    private static boolean checkIsArrayType(BArrayType sourceType, BArrayType targetType,
                                            List<TypePair> unresolvedTypes) {
        switch (sourceType.getState()) {
            case OPEN:
                if (targetType.getState() != ArrayState.OPEN) {
                    return false;
                }
                break;
            case CLOSED:
                if (targetType.getState() == ArrayState.CLOSED &&
                        sourceType.getSize() != targetType.getSize()) {
                    return false;
                }
                break;
        }
        return checkIsType(sourceType.getElementType(), targetType.getElementType(), unresolvedTypes);
    }

    private static boolean checkIsArrayType(BTupleType sourceType, BArrayType targetType,
                                            List<TypePair> unresolvedTypes) {
        List<Type> tupleTypes = sourceType.getTupleTypes();
        Type sourceRestType = sourceType.getRestType();
        Type targetElementType = targetType.getElementType();

        if (targetType.getState() == ArrayState.OPEN) {
            for (Type sourceElementType : tupleTypes) {
                if (!checkIsType(sourceElementType, targetElementType, unresolvedTypes)) {
                    return false;
                }
            }
            if (sourceRestType != null) {
                return checkIsType(sourceRestType, targetElementType, unresolvedTypes);
            }
            return true;
        }
        if (sourceRestType != null) {
            return false;
        }
        if (tupleTypes.size() != targetType.getSize()) {
            return false;
        }
        for (Type sourceElementType : tupleTypes) {
            if (!checkIsType(sourceElementType, targetElementType, unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsArrayType(Type sourceType, BArrayType targetType, List<TypePair> unresolvedTypes) {
        int sourceTypeTag = sourceType.getTag();

        if (sourceTypeTag == TypeTags.UNION_TAG) {
            for (Type memberType : ((BUnionType) sourceType).getMemberTypes()) {
                if (!checkIsArrayType(memberType, targetType, unresolvedTypes)) {
                    return false;
                }
            }
            return true;
        }

        if (sourceTypeTag != TypeTags.ARRAY_TAG && sourceTypeTag != TypeTags.TUPLE_TAG) {
            return false;
        }

        if (sourceTypeTag == TypeTags.ARRAY_TAG) {
            return checkIsArrayType((BArrayType) sourceType, targetType, unresolvedTypes);
        }
        return checkIsArrayType((BTupleType) sourceType, targetType, unresolvedTypes);
    }

    private static boolean checkIsTupleType(BArrayType sourceType, BTupleType targetType,
                                            List<TypePair> unresolvedTypes) {
        Type sourceElementType = sourceType.getElementType();
        List<Type> targetTypes = targetType.getTupleTypes();
        Type targetRestType = targetType.getRestType();

        switch (sourceType.getState()) {
            case OPEN:
                if (targetRestType == null) {
                    return false;
                }
                if (targetTypes.isEmpty()) {
                    return checkIsType(sourceElementType, targetRestType, unresolvedTypes);
                }
                return false;
            case CLOSED:
                if (sourceType.getSize() < targetTypes.size()) {
                    return false;
                }
                if (targetTypes.isEmpty()) {
                    if (targetRestType != null) {
                        return checkIsType(sourceElementType, targetRestType, unresolvedTypes);
                    }
                    return sourceType.getSize() == 0;
                }

                for (Type targetElementType : targetTypes) {
                    if (!(checkIsType(sourceElementType, targetElementType, unresolvedTypes))) {
                        return false;
                    }
                }
                if (sourceType.getSize() == targetTypes.size()) {
                    return true;
                }
                if (targetRestType != null) {
                    return checkIsType(sourceElementType, targetRestType, unresolvedTypes);
                }
                return false;
            default:
                return false;
        }
    }

    private static boolean checkIsTupleType(BTupleType sourceType, BTupleType targetType,
                                            List<TypePair> unresolvedTypes) {
        List<Type> sourceTypes = sourceType.getTupleTypes();
        Type sourceRestType = sourceType.getRestType();
        List<Type> targetTypes = targetType.getTupleTypes();
        Type targetRestType = targetType.getRestType();

        if (sourceRestType != null && targetRestType == null) {
            return false;
        }
        int sourceTypeSize = sourceTypes.size();
        int targetTypeSize = targetTypes.size();

        if (sourceRestType == null && targetRestType == null && sourceTypeSize != targetTypeSize) {
            return false;
        }

        if (sourceTypeSize < targetTypeSize) {
            return false;
        }

        for (int i = 0; i < targetTypeSize; i++) {
            if (!checkIsType(sourceTypes.get(i), targetTypes.get(i), unresolvedTypes)) {
                return false;
            }
        }
        if (sourceTypeSize == targetTypeSize) {
            if (sourceRestType != null) {
                return checkIsType(sourceRestType, targetRestType, unresolvedTypes);
            }
            return true;
        }

        for (int i = targetTypeSize; i < sourceTypeSize; i++) {
            if (!checkIsType(sourceTypes.get(i), targetRestType, unresolvedTypes)) {
                return false;
            }
        }
        if (sourceRestType != null) {
            return checkIsType(sourceRestType, targetRestType, unresolvedTypes);
        }
        return true;
    }

    private static boolean checkIsTupleType(Type sourceType, BTupleType targetType, List<TypePair> unresolvedTypes) {
        int sourceTypeTag = sourceType.getTag();

        if (sourceTypeTag == TypeTags.UNION_TAG) {
            for (Type memberType : ((BUnionType) sourceType).getMemberTypes()) {
                if (!checkIsTupleType(memberType, targetType, unresolvedTypes)) {
                    return false;
                }
            }
            return true;
        }

        if (sourceTypeTag != TypeTags.ARRAY_TAG && sourceTypeTag != TypeTags.TUPLE_TAG) {
            return false;
        }

        if (sourceTypeTag == TypeTags.ARRAY_TAG) {
            return checkIsTupleType((BArrayType) sourceType, targetType, unresolvedTypes);
        }
        return checkIsTupleType((BTupleType) sourceType, targetType, unresolvedTypes);
    }

    private static boolean checkIsAnyType(Type sourceType) {
        switch (sourceType.getTag()) {
            case TypeTags.ERROR_TAG:
                return false;
            case TypeTags.UNION_TAG:
                for (Type memberType : ((BUnionType) sourceType).getMemberTypes()) {
                    if (!checkIsAnyType(memberType)) {
                        return false;
                    }
                }
                return true;
        }
        return true;
    }

    private static boolean checkIsFiniteType(Type sourceType, BFiniteType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.FINITE_TYPE_TAG) {
            return false;
        }

        BFiniteType sourceFiniteType = (BFiniteType) sourceType;
        if (sourceFiniteType.valueSpace.size() != targetType.valueSpace.size()) {
            return false;
        }

        return targetType.valueSpace.containsAll(sourceFiniteType.valueSpace);
    }

    private static boolean checkIsFutureType(Type sourceType, BFutureType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.FUTURE_TAG) {
            return false;
        }
        return checkConstraints(((BFutureType) sourceType).getConstrainedType(), targetType.getConstrainedType(),
                unresolvedTypes);
    }

    private static boolean checkObjectEquivalency(Type sourceType, BObjectType targetType,
                                                  List<TypePair> unresolvedTypes) {
        return checkObjectEquivalency(null, sourceType, targetType, unresolvedTypes);
    }

    private static boolean checkObjectEquivalency(Object sourceVal, Type sourceType, BObjectType targetType,
                                                  List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.OBJECT_TYPE_TAG) {
            return false;
        }
        // If we encounter two types that we are still resolving, then skip it.
        // This is done to avoid recursive checking of the same type.
        TypePair pair = new TypePair(sourceType, targetType);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);

        BObjectType sourceObjectType = (BObjectType) sourceType;

        if (Flags.isFlagOn(targetType.flags, Flags.ISOLATED) &&
                !Flags.isFlagOn(sourceObjectType.flags, Flags.ISOLATED)) {
            return false;
        }

        Map<String, Field> targetFields = targetType.getFields();
        Map<String, Field> sourceFields = sourceObjectType.getFields();
        AttachedFunctionType[] targetFuncs = targetType.getAttachedFunctions();
        AttachedFunctionType[] sourceFuncs = sourceObjectType.getAttachedFunctions();

        if (targetType.getFields().values().stream().anyMatch(field -> Flags.isFlagOn(field.getFlags(), Flags.PRIVATE))
                || Stream.of(targetFuncs).anyMatch(func -> Flags.isFlagOn(func.getFlags(), Flags.PRIVATE))) {
            return false;
        }

        if (targetFields.size() > sourceFields.size() || targetFuncs.length > sourceFuncs.length) {
            return false;
        }

        String targetTypeModule = Optional.ofNullable(targetType.getPackage()).map(Module::toString).orElse("");
        String sourceTypeModule = Optional.ofNullable(sourceObjectType.getPackage()).map(Module::toString).orElse("");

        if (sourceVal == null) {
            if (!checkObjectSubTypeForFields(targetFields, sourceFields, targetTypeModule, sourceTypeModule,
                                             unresolvedTypes)) {
                return false;
            }
        } else if (!checkObjectSubTypeForFieldsByValue(targetFields, sourceFields, targetTypeModule, sourceTypeModule,
                                                       (BObject) sourceVal, unresolvedTypes)) {
            return false;
        }

        return checkObjectSubTypeForMethods(unresolvedTypes, targetFuncs, sourceFuncs, targetTypeModule,
                                            sourceTypeModule, sourceObjectType, targetType);
    }

    private static boolean checkObjectSubTypeForFields(Map<String, Field> targetFields,
                                                       Map<String, Field> sourceFields, String targetTypeModule,
                                                       String sourceTypeModule, List<TypePair> unresolvedTypes) {
        for (Field lhsField : targetFields.values()) {
            Field rhsField = sourceFields.get(lhsField.getFieldName());
            if (rhsField == null ||
                    !isInSameVisibilityRegion(targetTypeModule, sourceTypeModule, lhsField.getFlags(),
                                              rhsField.getFlags()) || hasIncompatibleReadOnlyFlags(lhsField,
                                                                                                   rhsField) ||
                    !checkIsType(rhsField.getFieldType(), lhsField.getFieldType(), unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkObjectSubTypeForFieldsByValue(Map<String, Field> targetFields,
                                                              Map<String, Field> sourceFields, String targetTypeModule,
                                                              String sourceTypeModule, BObject sourceObjVal,
                                                              List<TypePair> unresolvedTypes) {
        for (Field lhsField : targetFields.values()) {
            String name = lhsField.getFieldName();
            Field rhsField = sourceFields.get(name);
            if (rhsField == null ||
                    !isInSameVisibilityRegion(targetTypeModule, sourceTypeModule, lhsField.getFlags(),
                                              rhsField.getFlags()) || hasIncompatibleReadOnlyFlags(lhsField,
                                                                                                   rhsField)) {
                return false;
            }

            if (Flags.isFlagOn(rhsField.getFlags(), Flags.FINAL)) {
                Object fieldValue = sourceObjVal.get(StringUtils.fromString(name));
                Type fieldValueType = getType(fieldValue);

                if (fieldValueType.isReadOnly()) {
                    if (!checkIsLikeType(fieldValue, lhsField.getFieldType())) {
                        return false;
                    }
                    continue;
                }

                if (!checkIsType(fieldValueType, lhsField.getFieldType(), unresolvedTypes)) {
                    return false;
                }
            } else if (!checkIsType(rhsField.getFieldType(), lhsField.getFieldType(), unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkObjectSubTypeForMethods(List<TypePair> unresolvedTypes,
                                                        AttachedFunctionType[] targetFuncs,
                                                        AttachedFunctionType[] sourceFuncs,
                                                        String targetTypeModule, String sourceTypeModule,
                                                        BObjectType sourceType, BObjectType targetType) {
        for (AttachedFunctionType lhsFunc : targetFuncs) {
            AttachedFunctionType rhsFunc = getMatchingInvokableType(sourceFuncs, lhsFunc, unresolvedTypes);
            if (rhsFunc == null ||
                    !isInSameVisibilityRegion(targetTypeModule, sourceTypeModule, lhsFunc.getFlags(),
                                              rhsFunc.getFlags())) {
                return false;
            }
            if (Flags.isFlagOn(lhsFunc.getFlags(), Flags.REMOTE) != Flags.isFlagOn(rhsFunc.getFlags(), Flags.REMOTE)) {
                return false;
            }
        }

        // Target type is not a distinct type, no need to match type-ids
        BTypeIdSet targetTypeIdSet = targetType.typeIdSet;
        if (targetTypeIdSet == null) {
            return true;
        }

        BTypeIdSet sourceTypeIdSet = sourceType.typeIdSet;
        if (sourceTypeIdSet == null) {
            return false;
        }

        return sourceTypeIdSet.containsAll(targetTypeIdSet);
    }

    private static boolean isInSameVisibilityRegion(String lhsTypePkg, String rhsTypePkg, int lhsFlags, int rhsFlags) {
        if (Flags.isFlagOn(lhsFlags, Flags.PRIVATE)) {
            return lhsTypePkg.equals(rhsTypePkg);
        } else if (Flags.isFlagOn(lhsFlags, Flags.PUBLIC)) {
            return Flags.isFlagOn(rhsFlags, Flags.PUBLIC);
        }
        return !Flags.isFlagOn(rhsFlags, Flags.PRIVATE) && !Flags.isFlagOn(rhsFlags, Flags.PUBLIC) &&
                lhsTypePkg.equals(rhsTypePkg);
    }

    private static AttachedFunctionType getMatchingInvokableType(AttachedFunctionType[] rhsFuncs,
                                                              AttachedFunctionType lhsFunc,
                                                             List<TypePair> unresolvedTypes) {
        return Arrays.stream(rhsFuncs)
                .filter(rhsFunc -> lhsFunc.getName().equals(rhsFunc.getName()))
                .filter(rhsFunc -> checkFunctionTypeEqualityForObjectType(rhsFunc.getType(), lhsFunc.getType(),
                                                                          unresolvedTypes))
                .findFirst()
                .orElse(null);
    }

    private static boolean checkFunctionTypeEqualityForObjectType(FunctionType source, FunctionType target,
                                                                  List<TypePair> unresolvedTypes) {
        if (hasIncompatibleIsolatedFlags(target, source)) {
            return false;
        }

        if (source.getParameterTypes().length != target.getParameterTypes().length) {
            return false;
        }

        for (int i = 0; i < source.getParameterTypes().length; i++) {
            if (!checkIsType(target.getParameterTypes()[i], source.getParameterTypes()[i], unresolvedTypes)) {
                return false;
            }
        }

        if (source.getReturnType() == null && target.getReturnType() == null) {
            return true;
        } else if (source.getReturnType() == null || target.getReturnType() == null) {
            return false;
        }

        return checkIsType(source.getReturnType(), target.getReturnType(), unresolvedTypes);
    }

    private static boolean checkIsFunctionType(Type sourceType, BFunctionType targetType) {
        if (sourceType.getTag() != TypeTags.FUNCTION_POINTER_TAG) {
            return false;
        }

        BFunctionType source = (BFunctionType) sourceType;
        if (hasIncompatibleIsolatedFlags(targetType, source)) {
            return false;
        }

        if (source.paramTypes.length != targetType.paramTypes.length) {
            return false;
        }

        for (int i = 0; i < source.paramTypes.length; i++) {
            if (!checkIsType(targetType.paramTypes[i], source.paramTypes[i], new ArrayList<>())) {
                return false;
            }
        }

        return checkIsType(source.retType, targetType.retType, new ArrayList<>());
    }

    private static boolean hasIncompatibleIsolatedFlags(FunctionType target, FunctionType source) {
        return Flags.isFlagOn(target.getFlags(), Flags.ISOLATED) && !Flags.isFlagOn(source.getFlags(), Flags.ISOLATED);
    }

    private static boolean checkIsServiceType(Type sourceType) {
        if (sourceType.getTag() == TypeTags.SERVICE_TAG) {
            return true;
        }

        if (sourceType.getTag() == TypeTags.OBJECT_TYPE_TAG) {
            int flags = ((BObjectType) sourceType).flags;
            return (flags & Flags.SERVICE) == Flags.SERVICE;
        }

        return false;
    }

    public static boolean isInherentlyImmutableType(Type sourceType) {
        if (isSimpleBasicType(sourceType)) {
            return true;
        }

        switch (sourceType.getTag()) {
            case TypeTags.XML_TEXT_TAG:
            case TypeTags.FINITE_TYPE_TAG: // Assuming a finite type will only have members from simple basic types.
            case TypeTags.READONLY_TAG:
            case TypeTags.NULL_TAG:
            case TypeTags.ERROR_TAG:
            case TypeTags.INVOKABLE_TAG:
            case TypeTags.SERVICE_TAG:
            case TypeTags.TYPEDESC_TAG:
            case TypeTags.FUNCTION_POINTER_TAG:
            case TypeTags.HANDLE_TAG:
                return true;
        }
        return false;
    }

    public static boolean isSelectivelyImmutableType(Type type, Set<Type> unresolvedTypes) {
        if (!unresolvedTypes.add(type)) {
            return true;
        }

        switch (type.getTag()) {
            case TypeTags.ANY_TAG:
            case TypeTags.ANYDATA_TAG:
            case TypeTags.JSON_TAG:
            case TypeTags.XML_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_PI_TAG:
                return true;
            case TypeTags.ARRAY_TAG:
                Type elementType = ((BArrayType) type).getElementType();
                return isInherentlyImmutableType(elementType) ||
                        isSelectivelyImmutableType(elementType, unresolvedTypes);
            case TypeTags.TUPLE_TAG:
                BTupleType tupleType = (BTupleType) type;
                for (Type tupMemType : tupleType.getTupleTypes()) {
                    if (!isInherentlyImmutableType(tupMemType) &&
                            !isSelectivelyImmutableType(tupMemType, unresolvedTypes)) {
                        return false;
                    }
                }

                Type tupRestType = tupleType.getRestType();
                if (tupRestType == null) {
                    return true;
                }

                return isInherentlyImmutableType(tupRestType) ||
                        isSelectivelyImmutableType(tupRestType, unresolvedTypes);
            case TypeTags.RECORD_TYPE_TAG:
                BRecordType recordType = (BRecordType) type;
                for (Field field : recordType.getFields().values()) {
                    Type fieldType = field.getFieldType();
                    if (!isInherentlyImmutableType(fieldType) &&
                            !isSelectivelyImmutableType(fieldType, unresolvedTypes)) {
                        return false;
                    }
                }

                Type recordRestType = recordType.restFieldType;
                if (recordRestType == null) {
                    return true;
                }

                return isInherentlyImmutableType(recordRestType) ||
                        isSelectivelyImmutableType(recordRestType, unresolvedTypes);
            case TypeTags.OBJECT_TYPE_TAG:
                BObjectType objectType = (BObjectType) type;

                if (Flags.isFlagOn(objectType.flags, Flags.CLASS) &&
                        !Flags.isFlagOn(objectType.flags, Flags.READONLY)) {
                    return false;
                }

                for (Field field : objectType.getFields().values()) {
                    Type fieldType = field.getFieldType();
                    if (!isInherentlyImmutableType(fieldType) &&
                            !isSelectivelyImmutableType(fieldType, unresolvedTypes)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.MAP_TAG:
                Type constraintType = ((BMapType) type).getConstrainedType();
                return isInherentlyImmutableType(constraintType) ||
                        isSelectivelyImmutableType(constraintType, unresolvedTypes);
            case TypeTags.TABLE_TAG:
                Type tableConstraintType = ((BTableType) type).getConstrainedType();
                return isInherentlyImmutableType(tableConstraintType) ||
                        isSelectivelyImmutableType(tableConstraintType, unresolvedTypes);
            case TypeTags.UNION_TAG:
                boolean readonlyIntersectionExists = false;
                for (Type memberType : ((BUnionType) type).getMemberTypes()) {
                    if (isInherentlyImmutableType(memberType) ||
                            isSelectivelyImmutableType(memberType, unresolvedTypes)) {
                        readonlyIntersectionExists = true;
                    }
                }
                return readonlyIntersectionExists;
        }
        return false;
    }

    private static boolean checkConstraints(Type sourceConstraint, Type targetConstraint,
                                            List<TypePair> unresolvedTypes) {
        if (sourceConstraint == null) {
            sourceConstraint = TYPE_ANY;
        }

        if (targetConstraint == null) {
            targetConstraint = TYPE_ANY;
        }

        return checkIsType(sourceConstraint, targetConstraint, unresolvedTypes);
    }

    private static boolean isMutable(Object value, Type sourceType) {
        // All the value types are immutable
        if (value == null || sourceType.getTag() < TypeTags.JSON_TAG || TypeTags.isIntegerTypeTag(sourceType.getTag())
                || sourceType.getTag() == TypeTags.FINITE_TYPE_TAG || TypeTags.isStringTypeTag(sourceType.getTag())) {
            return false;
        }

        return !((RefValue) value).isFrozen();
    }

    private static boolean checkArrayEquivalent(Type actualType, Type expType) {
        if (expType.getTag() == TypeTags.ARRAY_TAG && actualType.getTag() == TypeTags.ARRAY_TAG) {
            // Both types are array types
            BArrayType lhrArrayType = (BArrayType) expType;
            BArrayType rhsArrayType = (BArrayType) actualType;
            return checkIsArrayType(rhsArrayType, lhrArrayType, new ArrayList<>());
        }
        // Now one or both types are not array types and they have to be equal
        return expType == actualType;
    }


    /**
     * Check whether a given value confirms to a given type. First it checks if the type of the value, and
     * if fails then falls back to checking the value.
     *
     * @param sourceValue Value to check
     * @param targetType Target type
     * @param unresolvedValues Values that are unresolved so far
     * @param allowNumericConversion Flag indicating whether to perform numeric conversions
     * @return True if the value confirms to the provided type. False, otherwise.
     */
    private static boolean checkIsLikeType(Object sourceValue, Type targetType, List<TypeValuePair> unresolvedValues,
                                           boolean allowNumericConversion) {
        Type sourceType = getType(sourceValue);
        if (checkIsType(sourceType, targetType, new ArrayList<>())) {
            return true;
        }

        return checkIsLikeOnValue(sourceValue, sourceType, targetType, unresolvedValues, allowNumericConversion);
    }

    /**
     * Check whether a given value confirms to a given type. Strictly checks the value only, and does not consider the
     * type of the value for consideration.
     *
     * @param sourceValue Value to check
     * @param sourceType Type of the value
     * @param targetType Target type
     * @param unresolvedValues Values that are unresolved so far
     * @param allowNumericConversion Flag indicating whether to perform numeric conversions
     * @return True if the value confirms to the provided type. False, otherwise.
     */
    private static boolean checkIsLikeOnValue(Object sourceValue, Type sourceType, Type targetType,
                                              List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        int sourceTypeTag = sourceType.getTag();
        int targetTypeTag = targetType.getTag();

        if (sourceTypeTag == TypeTags.INTERSECTION_TAG) {
            return checkIsLikeOnValue(sourceValue, ((BIntersectionType) sourceType).getEffectiveType(),
                                      targetTypeTag != TypeTags.INTERSECTION_TAG ? targetType :
                                              ((BIntersectionType) targetType).getEffectiveType(),
                                      unresolvedValues, allowNumericConversion);
        }

        if (targetTypeTag == TypeTags.INTERSECTION_TAG) {
            return checkIsLikeOnValue(sourceValue, sourceType, ((BIntersectionType) targetType).getEffectiveType(),
                                      unresolvedValues, allowNumericConversion);
        }

        switch (targetTypeTag) {
            case TypeTags.READONLY_TAG:
                return true;
            case TypeTags.BYTE_TAG:
                if (TypeTags.isIntegerTypeTag(sourceTypeTag)) {
                    return isByteLiteral((Long) sourceValue);
                }
                return allowNumericConversion && TypeConverter.isConvertibleToByte(sourceValue);
            case TypeTags.INT_TAG:
                return allowNumericConversion && TypeConverter.isConvertibleToInt(sourceValue);
            case TypeTags.SIGNED32_INT_TAG:
            case TypeTags.SIGNED16_INT_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.UNSIGNED32_INT_TAG:
            case TypeTags.UNSIGNED16_INT_TAG:
            case TypeTags.UNSIGNED8_INT_TAG:
                if (TypeTags.isIntegerTypeTag(sourceTypeTag) || targetTypeTag == TypeTags.BYTE_TAG) {
                    return TypeConverter.isConvertibleToIntSubType(sourceValue, targetType);
                }
                return allowNumericConversion && TypeConverter.isConvertibleToIntSubType(sourceValue, targetType);
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
                return allowNumericConversion && TypeConverter.isConvertibleToFloatingPointTypes(sourceValue);
            case TypeTags.CHAR_STRING_TAG:
                return TypeConverter.isConvertibleToChar(sourceValue);
            case TypeTags.RECORD_TYPE_TAG:
                return checkIsLikeRecordType(sourceValue, (BRecordType) targetType, unresolvedValues,
                        allowNumericConversion);
            case TypeTags.JSON_TAG:
                return checkIsLikeJSONType(sourceValue, sourceType, (BJSONType) targetType, unresolvedValues,
                        allowNumericConversion);
            case TypeTags.MAP_TAG:
                return checkIsLikeMapType(sourceValue, (BMapType) targetType, unresolvedValues, allowNumericConversion);
            case TypeTags.STREAM_TAG:
                return checkIsLikeStreamType(sourceValue, (BStreamType) targetType);
            case TypeTags.ARRAY_TAG:
                return checkIsLikeArrayType(sourceValue, (BArrayType) targetType, unresolvedValues,
                                            allowNumericConversion);
            case TypeTags.TUPLE_TAG:
                return checkIsLikeTupleType(sourceValue, (BTupleType) targetType, unresolvedValues,
                                            allowNumericConversion);
            case TypeTags.ERROR_TAG:
                return checkIsLikeErrorType(sourceValue, (BErrorType) targetType, unresolvedValues,
                                            allowNumericConversion);
            case TypeTags.ANYDATA_TAG:
                return checkIsLikeAnydataType(sourceValue, sourceType, unresolvedValues, allowNumericConversion);
            case TypeTags.FINITE_TYPE_TAG:
                return checkFiniteTypeAssignable(sourceValue, sourceType, (BFiniteType) targetType);
            case TypeTags.XML_ELEMENT_TAG:
                if (sourceTypeTag == TypeTags.XML_TAG) {
                    XMLValue xmlSource = (XMLValue) sourceValue;
                    return xmlSource.isSingleton();
                }
                return false;
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TEXT_TAG:
                if (sourceTypeTag == TypeTags.XML_TAG) {
                    return checkIsLikeNonElementSingleton((XMLValue) sourceValue, targetType);
                }
                return false;
            case TypeTags.XML_TAG:
                if (sourceTypeTag == TypeTags.XML_TAG) {
                    return checkIsLikeXMLSequenceType((XMLValue) sourceValue, targetType);
                }
                return false;
            case TypeTags.UNION_TAG:
                if (allowNumericConversion) {
                    List<Type> compatibleTypesWithNumConversion = new ArrayList<>();
                    List<Type> compatibleTypesWithoutNumConversion = new ArrayList<>();
                    for (Type type : ((BUnionType) targetType).getMemberTypes()) {
                        List<TypeValuePair> tempList = new ArrayList<>(unresolvedValues.size());
                        tempList.addAll(unresolvedValues);

                        if (checkIsLikeType(sourceValue, type, tempList, false)) {
                            compatibleTypesWithoutNumConversion.add(type);
                        }

                        if (checkIsLikeType(sourceValue, type, unresolvedValues, true)) {
                            compatibleTypesWithNumConversion.add(type);
                        }
                    }
                    // Conversion should only be possible to one other numeric type.
                    return compatibleTypesWithNumConversion.size() != 0 &&
                            compatibleTypesWithNumConversion.size() - compatibleTypesWithoutNumConversion.size() <= 1;
                } else {
                    for (Type type : ((BUnionType) targetType).getMemberTypes()) {
                        if (checkIsLikeType(sourceValue, type, unresolvedValues, false)) {
                            return true;
                        }
                    }
                }
                return false;
            default:
                return false;
        }
    }

    private static XMLNodeType getXmlNodeType(Type type) {
        XMLNodeType nodeType = null;
        switch (type.getTag()) {
            case TypeTags.XML_ELEMENT_TAG:
                nodeType = XMLNodeType.ELEMENT;
                break;
            case TypeTags.XML_COMMENT_TAG:
                nodeType = XMLNodeType.COMMENT;
                break;
            case TypeTags.XML_PI_TAG:
                nodeType = XMLNodeType.PI;
                break;
            case TypeTags.XML_TEXT_TAG:
                nodeType = XMLNodeType.TEXT;
                break;
            default:
                return null;
        }
        return nodeType;
    }

    private static boolean checkIsLikeNonElementSingleton(XMLValue xmlSource, Type targetType) {

        XMLNodeType nodeType = getXmlNodeType(targetType);

        if (nodeType == null) {
            return false;
        }

        if (xmlSource.getNodeType() == nodeType) {
            return true;
        }

        if (xmlSource.getNodeType() == XMLNodeType.SEQUENCE) {
            XMLSequence seq = (XMLSequence) xmlSource;

            return seq.size() == 1 && seq.getChildrenList().get(0).getNodeType() == nodeType ||
                    (nodeType == XMLNodeType.TEXT && seq.isEmpty());
        }
        return false;
    }

    private static boolean checkIsLikeXMLSequenceType(XMLValue xmlSource, Type targetType) {
        if (xmlSource.getNodeType() != XMLNodeType.SEQUENCE) {
            return false;
        }
        Set<XMLNodeType> acceptedNodes = new HashSet<>();

        BXMLType target = (BXMLType) targetType;
        if (target.constraint.getTag() == TypeTags.UNION_TAG) {
            getXMLNodeOnUnion((BUnionType) target.constraint, acceptedNodes);
        } else {
            acceptedNodes.add(getXmlNodeType(((BXMLType) targetType).constraint));
        }

        XMLSequence seq = (XMLSequence) xmlSource;
        for (BXML m : seq.getChildrenList()) {
            if (!acceptedNodes.contains(m.getNodeType())) {
                return false;
            }
        }
        return true;
    }

    private static void getXMLNodeOnUnion(BUnionType unionType, Set<XMLNodeType> nodeTypes) {
        // Currently there are only 4 xml subtypes
        if (nodeTypes.size() == 4) {
            return;
        }

        for (Type memberType : unionType.getMemberTypes()) {
            if (memberType.getTag() == TypeTags.UNION_TAG) {
                getXMLNodeOnUnion((BUnionType) memberType, nodeTypes);
            } else {
               nodeTypes.add(getXmlNodeType(memberType));
            }
        }
    }
    public static boolean isNumericType(Type type) {
        return type.getTag() < TypeTags.STRING_TAG;
    }

    private static boolean checkIsLikeAnydataType(Object sourceValue, Type sourceType,
                                                  List<TypeValuePair> unresolvedValues,
                                                  boolean allowNumericConversion) {
        switch (sourceType.getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.JSON_TAG:
            case TypeTags.MAP_TAG:
                return isLikeType(((MapValueImpl) sourceValue).values().toArray(), TYPE_ANYDATA,
                                  unresolvedValues, allowNumericConversion);
            case TypeTags.ARRAY_TAG:
                ArrayValue arr = (ArrayValue) sourceValue;
                BArrayType arrayType = (BArrayType) arr.getType();
                switch (arrayType.getElementType().getTag()) {
                    case TypeTags.INT_TAG:
                    case TypeTags.FLOAT_TAG:
                    case TypeTags.DECIMAL_TAG:
                    case TypeTags.STRING_TAG:
                    case TypeTags.BOOLEAN_TAG:
                    case TypeTags.BYTE_TAG:
                        return true;
                    default:
                        return isLikeType(arr.getValues(), TYPE_ANYDATA, unresolvedValues,
                                          allowNumericConversion);
                }
            case TypeTags.TUPLE_TAG:
                return isLikeType(((ArrayValue) sourceValue).getValues(), TYPE_ANYDATA, unresolvedValues,
                                  allowNumericConversion);
            case TypeTags.ANYDATA_TAG:
                return true;
            // TODO: 8/13/19 Check if can be removed
            case TypeTags.FINITE_TYPE_TAG:
            case TypeTags.UNION_TAG:
                return checkIsLikeType(sourceValue, TYPE_ANYDATA, unresolvedValues, allowNumericConversion);
            default:
                return false;
        }
    }

    private static boolean isLikeType(Object[] objects, Type targetType, List<TypeValuePair> unresolvedValues,
                                      boolean allowNumericConversion) {
        for (Object value : objects) {
            if (!checkIsLikeType(value, targetType, unresolvedValues, allowNumericConversion)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsLikeTupleType(Object sourceValue, BTupleType targetType,
                                                List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        if (!(sourceValue instanceof ArrayValue)) {
            return false;
        }

        ArrayValue source = (ArrayValue) sourceValue;
        List<Type> targetTypes = targetType.getTupleTypes();
        int sourceTypeSize = source.size();
        int targetTypeSize = targetTypes.size();
        Type targetRestType = targetType.getRestType();

        if (sourceTypeSize < targetTypeSize) {
            return false;
        }
        if (targetRestType == null && sourceTypeSize > targetTypeSize) {
            return false;
        }

        for (int i = 0; i < targetTypeSize; i++) {
            if (!checkIsLikeType(source.getRefValue(i), targetTypes.get(i), unresolvedValues, allowNumericConversion)) {
                return false;
            }
        }
        for (int i = targetTypeSize; i < sourceTypeSize; i++) {
            if (!checkIsLikeType(source.getRefValue(i), targetRestType, unresolvedValues, allowNumericConversion)) {
                return false;
            }
        }
        return true;
    }

    static boolean isByteLiteral(long longValue) {
        return (longValue >= BBYTE_MIN_VALUE && longValue <= BBYTE_MAX_VALUE);
    }

    static boolean isSigned32LiteralValue(Long longObject) {

        return (longObject >= SIGNED32_MIN_VALUE && longObject <= SIGNED32_MAX_VALUE);
    }

    static boolean isSigned16LiteralValue(Long longObject) {

        return (longObject.intValue() >= SIGNED16_MIN_VALUE && longObject.intValue() <= SIGNED16_MAX_VALUE);
    }

    static boolean isSigned8LiteralValue(Long longObject) {

        return (longObject.intValue() >= SIGNED8_MIN_VALUE && longObject.intValue() <= SIGNED8_MAX_VALUE);
    }

    static boolean isUnsigned32LiteralValue(Long longObject) {

        return (longObject >= 0 && longObject <= UNSIGNED32_MAX_VALUE);
    }

    static boolean isUnsigned16LiteralValue(Long longObject) {

        return (longObject.intValue() >= 0 && longObject.intValue() <= UNSIGNED16_MAX_VALUE);
    }

    static boolean isUnsigned8LiteralValue(Long longObject) {

        return (longObject.intValue() >= 0 && longObject.intValue() <= UNSIGNED8_MAX_VALUE);
    }

    static boolean isCharLiteralValue(Object object) {
        String value;
        if (object instanceof BString) {
            value = ((BString) object).getValue();
        } else if (object instanceof String) {
            value = (String) object;
        } else {
            return false;
        }
        return value.codePoints().count() == 1;
    }

    private static boolean checkIsLikeArrayType(Object sourceValue, BArrayType targetType,
                                                List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        if (!(sourceValue instanceof ArrayValue)) {
            return false;
        }

        ArrayValue source = (ArrayValue) sourceValue;
        Type targetTypeElementType = targetType.getElementType();
        if (source.getType().getTag() == TypeTags.ARRAY_TAG) {
            Type sourceElementType = ((BArrayType) source.getType()).getElementType();
            if (isValueType(sourceElementType)) {
                boolean isType = checkIsType(sourceElementType, targetTypeElementType, new ArrayList<>());

                if (isType || !allowNumericConversion || !isNumericType(sourceElementType)) {
                    return isType;
                }

                if (isNumericType(targetTypeElementType)) {
                    return true;
                }

                if (targetTypeElementType.getTag() != TypeTags.UNION_TAG) {
                    return false;
                }

                List<Type> targetNumericTypes = new ArrayList<>();
                for (Type memType : ((BUnionType) targetTypeElementType).getMemberTypes()) {
                    if (isNumericType(memType) && !targetNumericTypes.contains(memType)) {
                        targetNumericTypes.add(memType);
                    }
                }
                return targetNumericTypes.size() == 1;
            }
        }

        Object[] arrayValues = source.getValues();
        for (int i = 0; i < ((ArrayValue) sourceValue).size(); i++) {
            if (!checkIsLikeType(arrayValues[i], targetTypeElementType, unresolvedValues, allowNumericConversion)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsLikeMapType(Object sourceValue, BMapType targetType,
                                              List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        if (!(sourceValue instanceof MapValueImpl)) {
            return false;
        }

        for (Object mapEntry : ((MapValueImpl) sourceValue).values()) {
            if (!checkIsLikeType(mapEntry, targetType.getConstrainedType(), unresolvedValues, allowNumericConversion)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsLikeStreamType(Object sourceValue, BStreamType targetType) {
        if (!(sourceValue instanceof StreamValue)) {
            return false;
        }

        BStreamType streamType = (BStreamType) ((StreamValue) sourceValue).getType();

        return streamType.getConstrainedType() == targetType.getConstrainedType();
    }

    private static boolean checkIsLikeJSONType(Object sourceValue, Type sourceType, BJSONType targetType,
                                               List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        if (sourceType.getTag() == TypeTags.ARRAY_TAG) {
            ArrayValue source = (ArrayValue) sourceValue;
            Type elementType = ((BArrayType) source.getType()).getElementType();
            if (isValueType(elementType)) {
                return checkIsType(elementType, targetType, new ArrayList<>());
            }

            Object[] arrayValues = source.getValues();
            for (int i = 0; i < ((ArrayValue) sourceValue).size(); i++) {
                if (!checkIsLikeType(arrayValues[i], targetType, unresolvedValues, allowNumericConversion)) {
                    return false;
                }
            }
            return true;
        } else if (sourceType.getTag() == TypeTags.MAP_TAG) {
            for (Object value : ((MapValueImpl) sourceValue).values()) {
                if (!checkIsLikeType(value, targetType, unresolvedValues, allowNumericConversion)) {
                    return false;
                }
            }
            return true;
        } else if (sourceType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            TypeValuePair typeValuePair = new TypeValuePair(sourceValue, targetType);
            if (unresolvedValues.contains(typeValuePair)) {
                return true;
            }
            unresolvedValues.add(typeValuePair);
            for (Object object : ((MapValueImpl) sourceValue).values()) {
                if (!checkIsLikeType(object, targetType, unresolvedValues, allowNumericConversion)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean checkIsLikeRecordType(Object sourceValue, BRecordType targetType,
                                                 List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        if (!(sourceValue instanceof MapValueImpl)) {
            return false;
        }

        TypeValuePair typeValuePair = new TypeValuePair(sourceValue, targetType);
        if (unresolvedValues.contains(typeValuePair)) {
            return true;
        }
        unresolvedValues.add(typeValuePair);
        Map<String, Type> targetTypeField = new HashMap<>();
        Type restFieldType = targetType.restFieldType;

        for (Field field : targetType.getFields().values()) {
            targetTypeField.put(field.getFieldName(), field.getFieldType());
        }

        for (Map.Entry targetTypeEntry : targetTypeField.entrySet()) {
            Object fieldName = StringUtils.fromString(targetTypeEntry.getKey().toString());
            if (!(((MapValueImpl) sourceValue).containsKey(fieldName)) &&
                    !Flags.isFlagOn(targetType.getFields().get(fieldName.toString()).getFlags(), Flags.OPTIONAL)) {
                return false;
            }
        }

        for (Object object : ((MapValueImpl) sourceValue).entrySet()) {
            Map.Entry valueEntry = (Map.Entry) object;
            String fieldName = valueEntry.getKey().toString();

            if (targetTypeField.containsKey(fieldName)) {
                if (!checkIsLikeType((valueEntry.getValue()), targetTypeField.get(fieldName),
                                     unresolvedValues, allowNumericConversion)) {
                    return false;
                }
            } else {
                if (!targetType.sealed) {
                    if (!checkIsLikeType((valueEntry.getValue()), restFieldType, unresolvedValues,
                                         allowNumericConversion)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkFiniteTypeAssignable(Object sourceValue, Type sourceType, BFiniteType targetType) {
        if (sourceValue == null) {
            // we should not reach here
            return false;
        }

        for (Object valueSpaceItem : targetType.valueSpace) {
            // TODO: 8/13/19 Maryam fix for conversion
            if (isFiniteTypeValue(sourceValue, sourceType, valueSpaceItem)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isFiniteTypeValue(Object sourceValue, Type sourceType, Object valueSpaceItem) {
        Type valueSpaceItemType = getType(valueSpaceItem);
        if (valueSpaceItemType.getTag() > TypeTags.FLOAT_TAG) {
            return valueSpaceItemType.getTag() == sourceType.getTag() && valueSpaceItem.equals(sourceValue);
        }

        switch (sourceType.getTag()) {
            case TypeTags.BYTE_TAG:
            case TypeTags.INT_TAG:
                return ((Number) sourceValue).longValue() == ((Number) valueSpaceItem).longValue();
            case TypeTags.FLOAT_TAG:
                if (sourceType.getTag() != valueSpaceItemType.getTag()) {
                    return false;
                }

                return ((Number) sourceValue).doubleValue() == ((Number) valueSpaceItem).doubleValue();
            case TypeTags.DECIMAL_TAG:
                // falls through
            default:
                if (sourceType.getTag() != valueSpaceItemType.getTag()) {
                    return false;
                }
                return valueSpaceItem.equals(sourceValue);
        }
    }

    private static boolean checkIsErrorType(Type sourceType, BErrorType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.ERROR_TAG) {
            return false;
        }
        // Handle recursive error types.
        TypePair pair = new TypePair(sourceType, targetType);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);
        BErrorType bErrorType = (BErrorType) sourceType;

        if (targetType.typeIdSet == null) {
            return checkIsType(bErrorType.detailType, targetType.detailType, unresolvedTypes);
        }

        BTypeIdSet sourceTypeIdSet = bErrorType.typeIdSet;
        if (sourceTypeIdSet == null) {
            return false;
        }

        return sourceTypeIdSet.containsAll(targetType.typeIdSet);
    }

    private static boolean checkIsLikeErrorType(Object sourceValue, BErrorType targetType,
                                                List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        Type sourceType = getType(sourceValue);
        if (sourceValue == null || sourceType.getTag() != TypeTags.ERROR_TAG) {
            return false;
        }

        if (targetType.typeIdSet == null) {
            return checkIsLikeType(((ErrorValue) sourceValue).getDetails(), targetType.detailType, unresolvedValues,
                    allowNumericConversion);
        }

        BTypeIdSet sourceIdSet = ((BErrorType) sourceType).typeIdSet;
        if (sourceIdSet == null) {
            return false;
        }

        return sourceIdSet.containsAll(targetType.typeIdSet);
    }

    private static boolean isSimpleBasicType(Type type) {
        return type.getTag() < TypeTags.JSON_TAG || TypeTags.isIntegerTypeTag(type.getTag());
    }

    private static boolean isHandleType(Type type) {
        return type.getTag() == TypeTags.HANDLE_TAG;
    }

    /**
     * Deep value equality check for anydata.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value on the right hand side
     * @param checkedValues Structured value pairs already compared or being compared
     * @return True if values are equal, else false.
     */
    private static boolean isEqual(Object lhsValue, Object rhsValue, List<ValuePair> checkedValues) {
        if (lhsValue == rhsValue) {
            return true;
        }

        if (null == lhsValue || null == rhsValue) {
            return false;
        }

        int lhsValTypeTag = getType(lhsValue).getTag();
        int rhsValTypeTag = getType(rhsValue).getTag();

        switch (lhsValTypeTag) {
            case TypeTags.STRING_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.BOOLEAN_TAG:
                return lhsValue.equals(rhsValue);
            case TypeTags.INT_TAG:
                if (rhsValTypeTag <= TypeTags.FLOAT_TAG) {
                    return lhsValue.equals(((Number) rhsValue).longValue());
                }

                if (rhsValTypeTag == TypeTags.DECIMAL_TAG) {
                    return DecimalValue.valueOf((long) lhsValue).equals(rhsValue);
                }

                return false;
            case TypeTags.BYTE_TAG:
                if (rhsValTypeTag <= TypeTags.FLOAT_TAG) {
                    return ((Number) lhsValue).byteValue() == ((Number) rhsValue).byteValue();
                }

                if (rhsValTypeTag == TypeTags.DECIMAL_TAG) {
                    return DecimalValue.valueOf((int) lhsValue).equals(rhsValue);
                }

                return false;
            case TypeTags.XML_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_TEXT_TAG:
            case TypeTags.XML_PI_TAG:
                return XMLFactory.isEqual((XMLValue) lhsValue, (XMLValue) rhsValue);
            case TypeTags.MAP_TAG:
            case TypeTags.JSON_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return isMappingType(rhsValTypeTag) && isEqual((MapValueImpl) lhsValue, (MapValueImpl) rhsValue,
                        checkedValues);
            case TypeTags.TUPLE_TAG:
            case TypeTags.ARRAY_TAG:
                return isListType(rhsValTypeTag) &&
                        isEqual((ArrayValue) lhsValue, (ArrayValue) rhsValue, checkedValues);
            case TypeTags.ERROR_TAG:
                return rhsValTypeTag == TypeTags.ERROR_TAG &&
                        isEqual((ErrorValue) lhsValue, (ErrorValue) rhsValue, checkedValues);
            case TypeTags.SERVICE_TAG:
                break;
            case TypeTags.TABLE_TAG:
                return rhsValTypeTag == TypeTags.TABLE_TAG &&
                        isEqual((TableValueImpl) lhsValue, (TableValueImpl) rhsValue, checkedValues);
        }
        return false;
    }

    private static boolean isListType(int typeTag) {
        return typeTag == TypeTags.ARRAY_TAG || typeTag == TypeTags.TUPLE_TAG;
    }

    private static boolean isMappingType(int typeTag) {
        return typeTag == TypeTags.MAP_TAG || typeTag == TypeTags.RECORD_TYPE_TAG || typeTag == TypeTags.JSON_TAG;
    }

    /**
     * Deep equality check for an array/tuple.
     *
     * @param lhsList The array/tuple on the left hand side
     * @param rhsList The array/tuple on the right hand side
     * @param checkedValues Structured value pairs already compared or being compared
     * @return True if the array/tuple values are equal, else false.
     */
    private static boolean isEqual(ArrayValue lhsList, ArrayValue rhsList, List<ValuePair> checkedValues) {
        ValuePair compValuePair = new ValuePair(lhsList, rhsList);
        if (checkedValues.contains(compValuePair)) {
            return true;
        }
        checkedValues.add(compValuePair);

        if (lhsList.size() != rhsList.size()) {
            return false;
        }

        for (int i = 0; i < lhsList.size(); i++) {
            if (!isEqual(lhsList.get(i), rhsList.get(i), checkedValues)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Deep equality check for a map.
     *
     * @param lhsMap Map on the left hand side
     * @param rhsMap Map on the right hand side
     * @param checkedValues Structured value pairs already compared or being compared
     * @return True if the map values are equal, else false.
     */
    private static boolean isEqual(MapValueImpl lhsMap, MapValueImpl rhsMap, List<ValuePair> checkedValues) {
        ValuePair compValuePair = new ValuePair(lhsMap, rhsMap);
        if (checkedValues.contains(compValuePair)) {
            return true;
        }
        checkedValues.add(compValuePair);

        if (lhsMap.size() != rhsMap.size()) {
            return false;
        }

        if (!lhsMap.keySet().containsAll(rhsMap.keySet())) {
            return false;
        }

        Iterator<Map.Entry<BString, Object>> mapIterator = lhsMap.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry<BString, Object> lhsMapEntry = mapIterator.next();
            if (!isEqual(lhsMapEntry.getValue(), rhsMap.get(lhsMapEntry.getKey()), checkedValues)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Deep equality check for a table.
     *
     * @param lhsTable      Table on the left hand side
     * @param rhsTable      Table on the right hand side
     * @param checkedValues Structured value pairs already compared or being compared
     * @return True if the table values are equal, else false.
     */
    private static boolean isEqual(TableValueImpl lhsTable, TableValueImpl rhsTable, List<ValuePair> checkedValues) {
        ValuePair compValuePair = new ValuePair(lhsTable, rhsTable);
        if (checkedValues.contains(compValuePair)) {
            return true;
        }
        checkedValues.add(compValuePair);

        if (lhsTable.size() != rhsTable.size()) {
            return false;
        }

        if (((BTableType) lhsTable.getType()).getFieldNames() != null &&
                ((BTableType) lhsTable.getType()).getFieldNames().length > 0) {
            for (Map.Entry<BAnydataType, Object> lhsTableEntry :
                    (Iterable<Map.Entry<BAnydataType, Object>>) lhsTable.entrySet()) {
                if (!isEqual(lhsTableEntry.getValue(), rhsTable.get(lhsTableEntry.getKey()), checkedValues)) {
                    return false;
                }
            }

            return true;
        }

        return lhsTable.entrySet().equals(rhsTable.entrySet());
    }


    /**
     * Deep equality check for error.
     *
     * @param lhsError The error on the left hand side
     * @param rhsError The error on the right hand side
     * @param checkedValues Errors already compared or being compared
     * @return True if the error values are equal, else false.
     */
    private static boolean isEqual(ErrorValue lhsError, ErrorValue rhsError, List<ValuePair> checkedValues) {
        ValuePair compValuePair = new ValuePair(lhsError, rhsError);
        if (checkedValues.contains(compValuePair)) {
            return true;
        }
        checkedValues.add(compValuePair);

        return isEqual(lhsError.getMessage(), rhsError.getMessage(), checkedValues) &&
                isEqual((MapValueImpl) lhsError.getDetails(), (MapValueImpl) rhsError.getDetails(), checkedValues) &&
                isEqual(lhsError.getCause(), rhsError.getCause(), checkedValues);
    }

    /**
     * Type vector of size two, to hold the source and the target types.
     *
     * @since 0.995.0
     */
    private static class TypePair {
        Type sourceType;
        Type targetType;

        public TypePair(Type sourceType, Type targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TypePair)) {
                return false;
            }

            TypePair other = (TypePair) obj;
            return this.sourceType.equals(other.sourceType) && this.targetType.equals(other.targetType);
        }
    }

    /**
     * Check the reference equality of handle values.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value on the right hand side
     * @return True if values are equal, else false.
     */
    private static boolean isHandleValueRefEqual(Object lhsValue, Object rhsValue) {
        HandleValue lhsHandle = (HandleValue) lhsValue;
        HandleValue rhsHandle = (HandleValue) rhsValue;
        return lhsHandle.getValue() == rhsHandle.getValue();
    }

    /**
     * Unordered value vector of size two, to hold two values being compared.
     *
     * @since 0.995.0
     */
    private static class ValuePair {
        ArrayList<Object> valueList = new ArrayList<>(2);

        ValuePair(Object valueOne, Object valueTwo) {
            valueList.add(valueOne);
            valueList.add(valueTwo);
        }

        @Override
        public boolean equals(Object otherPair) {
            if (!(otherPair instanceof ValuePair)) {
                return false;
            }

            ArrayList otherList = ((ValuePair) otherPair).valueList;
            ArrayList currentList = valueList;

            if (otherList.size() != currentList.size()) {
                return false;
            }

            for (int i = 0; i < otherList.size(); i++) {
                if (!otherList.get(i).equals(currentList.get(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Checks whether a given {@link BType} has an implicit initial value or not.
     * @param type {@link BType} to be analyzed.
     * @return whether there's an implicit initial value or not.
     */
    public static boolean hasFillerValue(Type type) {
        return hasFillerValue(type, new ArrayList<>());
    }

    private static boolean hasFillerValue(Type type, List<Type> unanalyzedTypes) {
        if (type == null) {
            return true;
        }
        if (type.getTag() < TypeTags.RECORD_TYPE_TAG || TypeTags.isIntegerTypeTag(type.getTag())) {
            return true;
        }
        switch (type.getTag()) {
            case TypeTags.STREAM_TAG:
            case TypeTags.MAP_TAG:
            case TypeTags.ANY_TAG:
                return true;
            case TypeTags.ARRAY_TAG:
                return checkFillerValue((BArrayType) type);
            case TypeTags.FINITE_TYPE_TAG:
                return checkFillerValue((BFiniteType) type);
            case TypeTags.OBJECT_TYPE_TAG:
                return checkFillerValue((BObjectType) type);
            case TypeTags.RECORD_TYPE_TAG:
                return checkFillerValue((BRecordType) type, unanalyzedTypes);
            case TypeTags.TUPLE_TAG:
                BTupleType tupleType = (BTupleType) type;
                return tupleType.getTupleTypes().stream().allMatch(TypeChecker::hasFillerValue);
            case TypeTags.UNION_TAG:
                return checkFillerValue((BUnionType) type);
            default:
                return false;
        }
    }

    private static boolean checkFillerValue(BUnionType type) {
        // NIL is a member.
        if (type.isNullable()) {
            return true;
        }
        // All members are of same type.
        Iterator<Type> iterator = type.getMemberTypes().iterator();
        Type firstMember;
        for (firstMember = iterator.next(); iterator.hasNext(); ) {
            if (!isSameType(firstMember, iterator.next())) {
                return false;
            }
        }
        // Control reaching this point means there is only one type in the union.
        return isValueType(firstMember) && hasFillerValue(firstMember);
    }

    private static boolean checkFillerValue(BRecordType type, List<Type> unAnalyzedTypes) {
        if (unAnalyzedTypes.contains(type)) {
            return true;
        }
        unAnalyzedTypes.add(type);
        for (Field field : type.getFields().values()) {
            if (Flags.isFlagOn(field.getFlags(), Flags.OPTIONAL)) {
                continue;
            }
            if (!Flags.isFlagOn(field.getFlags(), Flags.REQUIRED)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static boolean checkFillerValue(BArrayType type) {
        return type.getState() == ArrayState.OPEN || hasFillerValue(type.getElementType());
    }

    private static boolean checkFillerValue(BObjectType type) {
        if (type.getTag() == TypeTags.SERVICE_TAG) {
            return false;
        } else {
            AttachedFunctionType generatedInitializer = type.generatedInitializer;
            if (generatedInitializer == null) {
                // abstract objects doesn't have a filler value.
                return false;
            }
            FunctionType initFuncType = generatedInitializer.getType();
            // Todo: check defaultable params of the init func as well
            boolean noParams = initFuncType.getParameterTypes().length == 0;
            boolean nilReturn = initFuncType.getReturnType().getTag() == TypeTags.NULL_TAG;
            return noParams && nilReturn;
        }
    }

    private static boolean checkFillerValue(BFiniteType type) {
        // Has NIL element as a member.
        for (Object value: type.valueSpace) {
            if (value == null) {
                return true;
            }
        }

        // For singleton types, that value is the implicit initial value
        if (type.valueSpace.size() == 1) {
            return true;
        }

        Object firstElement = type.valueSpace.iterator().next();
        for (Object value : type.valueSpace) {
            if (value.getClass() != firstElement.getClass()) {
                return false;
            }
        }

        if (firstElement instanceof String) {
            // check empty string for strings, and 0.0 for decimals
            return containsElement(type.valueSpace, "\"\"");
        } else if (firstElement instanceof Byte
                || firstElement instanceof Integer
                || firstElement instanceof Long) {
            return containsElement(type.valueSpace, "0");
        } else if (firstElement instanceof Float
                || firstElement instanceof Double
                || firstElement instanceof BigDecimal) {
            return containsElement(type.valueSpace, "0.0");
        } else if (firstElement instanceof Boolean) {
            return containsElement(type.valueSpace, "false");
        } else {
            return false;
        }
    }

    private static boolean containsElement(Set<Object> valueSpace, String e) {
        for (Object value : valueSpace) {
            if (value != null && value.toString().equals(e)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsType(Set<Object> valueSpace, Type type) {
        for (Object value : valueSpace) {
            if (!isSameType(type, getType(value))) {
                return false;
            }
        }
        return true;
    }
}
