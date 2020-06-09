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
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.commons.ArrayState;
import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.types.AnnotatableType;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BErrorType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BFiniteType;
import org.ballerinalang.jvm.types.BFunctionType;
import org.ballerinalang.jvm.types.BFutureType;
import org.ballerinalang.jvm.types.BIntersectionType;
import org.ballerinalang.jvm.types.BJSONType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BStreamType;
import org.ballerinalang.jvm.types.BTableType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypeIdSet;
import org.ballerinalang.jvm.types.BTypedescType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.BXMLType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.HandleValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.jvm.values.TableValueImpl;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.jvm.values.TypedescValueImpl;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLText;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BValue;
import org.ballerinalang.jvm.values.api.BXML;

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

import static org.ballerinalang.jvm.util.BLangConstants.BBYTE_MAX_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.BBYTE_MIN_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.SIGNED16_MAX_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.SIGNED16_MIN_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.SIGNED32_MAX_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.SIGNED32_MIN_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.SIGNED8_MAX_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.SIGNED8_MIN_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.UNSIGNED16_MAX_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.UNSIGNED32_MAX_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.UNSIGNED8_MAX_VALUE;

/**
 * Responsible for performing runtime type checking.
 *
 * @since 0.995.0
 */
@SuppressWarnings({"rawtypes"})
public class TypeChecker {

    public static Object checkCast(Object sourceVal, BType targetType) {

        if (checkIsType(sourceVal, targetType)) {
            return sourceVal;
        }

        BType sourceType = getType(sourceVal);
        if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG && targetType.getTag() <= TypeTags.BOOLEAN_TAG) {
            return TypeConverter.castValues(targetType, sourceVal);
        }

        // if the source is a numeric value and the target type is a union, try to find a matching
        // member.
        if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG && targetType.getTag() == TypeTags.UNION_TAG) {
            for (BType memberType : ((BUnionType) targetType).getMemberTypes()) {
                try {
                    return TypeConverter.castValues(memberType, sourceVal);
                } catch (Exception e) {
                    //ignore and continue
                }
            }
        }

        throw BallerinaErrors.createTypeCastError(sourceVal, targetType);
    }

    public static long anyToInt(Object sourceVal) {
        return TypeConverter.anyToIntCast(sourceVal,
                () -> BallerinaErrors.createTypeCastError(sourceVal, BTypes.typeInt));
    }

    public static long anyToSigned32(Object sourceVal) {
        return TypeConverter.anyToIntSubTypeCast(sourceVal, BTypes.typeIntSigned32,
                () -> BallerinaErrors.createTypeCastError(sourceVal, BTypes.typeIntSigned32));
    }

    public static long anyToSigned16(Object sourceVal) {
        return TypeConverter.anyToIntSubTypeCast(sourceVal, BTypes.typeIntSigned16,
                () -> BallerinaErrors.createTypeCastError(sourceVal, BTypes.typeIntSigned16));
    }

    public static long anyToSigned8(Object sourceVal) {
        return TypeConverter.anyToIntSubTypeCast(sourceVal, BTypes.typeIntSigned8,
                () -> BallerinaErrors.createTypeCastError(sourceVal, BTypes.typeIntSigned8));
    }

    public static long anyToUnsigned32(Object sourceVal) {
        return TypeConverter.anyToIntSubTypeCast(sourceVal, BTypes.typeIntUnsigned32,
                () -> BallerinaErrors.createTypeCastError(sourceVal, BTypes.typeIntUnsigned32));
    }

    public static long anyToUnsigned16(Object sourceVal) {
        return TypeConverter.anyToIntSubTypeCast(sourceVal, BTypes.typeIntUnsigned16,
                () -> BallerinaErrors.createTypeCastError(sourceVal, BTypes.typeIntUnsigned16));
    }

    public static long anyToUnsigned8(Object sourceVal) {
        return TypeConverter.anyToIntSubTypeCast(sourceVal, BTypes.typeIntUnsigned8,
                () -> BallerinaErrors.createTypeCastError(sourceVal, BTypes.typeIntUnsigned8));
    }

    public static double anyToFloat(Object sourceVal) {
        return TypeConverter.anyToFloatCast(sourceVal, () -> BallerinaErrors.createTypeCastError(sourceVal,
                BTypes.typeFloat));
    }

    public static boolean anyToBoolean(Object sourceVal) {
        return TypeConverter.anyToBooleanCast(sourceVal, () -> BallerinaErrors.createTypeCastError(sourceVal,
                BTypes.typeBoolean));
    }

    public static int anyToByte(Object sourceVal) {
        return TypeConverter.anyToByteCast(sourceVal, () -> BallerinaErrors.createTypeCastError(sourceVal,
                                                                                            BTypes.typeByte));
    }

    public static DecimalValue anyToDecimal(Object sourceVal) {
        return TypeConverter.anyToDecimal(sourceVal, () -> BallerinaErrors.createTypeCastError(sourceVal,
                                                                                               BTypes.typeDecimal));
    }

    public static byte anyToJByte(Object sourceVal) {
        return TypeConverter.anyToJByteCast(sourceVal,
                () -> BallerinaErrors.createBToJTypeCastError(sourceVal, "byte"));
    }

    public static char anyToJChar(Object sourceVal) {
        return TypeConverter.anyToJCharCast(sourceVal,
                () -> BallerinaErrors.createBToJTypeCastError(sourceVal, "char"));
    }

    public static short anyToJShort(Object sourceVal) {
        return TypeConverter.anyToJShortCast(sourceVal,
                () -> BallerinaErrors.createBToJTypeCastError(sourceVal, "short"));
    }

    public static int anyToJInt(Object sourceVal) {
        return TypeConverter.anyToJIntCast(sourceVal,
                () -> BallerinaErrors.createBToJTypeCastError(sourceVal, "int"));
    }

    public static long anyToJLong(Object sourceVal) {
        return TypeConverter.anyToJLongCast(sourceVal,
                () -> BallerinaErrors.createBToJTypeCastError(sourceVal, "long"));
    }

    public static float anyToJFloat(Object sourceVal) {
        return TypeConverter.anyToJFloatCast(sourceVal,
                () -> BallerinaErrors.createBToJTypeCastError(sourceVal, "float"));
    }

    public static double anyToJDouble(Object sourceVal) {
        return TypeConverter.anyToJDoubleCast(sourceVal,
                () -> BallerinaErrors.createBToJTypeCastError(sourceVal, "double"));
    }

    public static boolean anyToJBoolean(Object sourceVal) {
        return TypeConverter.anyToJBooleanCast(sourceVal,
                () -> BallerinaErrors.createBToJTypeCastError(sourceVal, "boolean"));
    }

    /**
     * Check whether a given value belongs to the given type.
     *
     * @param sourceVal value to check the type
     * @param targetType type to be test against
     * @return true if the value belongs to the given type, false otherwise
     */
    public static boolean checkIsType(Object sourceVal, BType targetType) {
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
    public static boolean checkIsType(Object sourceVal, BType sourceType, BType targetType) {
        if (checkIsType(sourceType, targetType)) {
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
    public static boolean checkIsLikeType(Object sourceValue, BType targetType) {
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
    public static boolean checkIsLikeType(Object sourceValue, BType targetType, boolean allowNumericConversion) {
        return checkIsLikeType(sourceValue, targetType, new ArrayList<>(), allowNumericConversion);
    }

    /**
     * Check whether two types are the same.
     *
     * @param sourceType type to test
     * @param targetType type to test against
     * @return true if the two types are same; false otherwise
     */
    public static boolean isSameType(BType sourceType, BType targetType) {
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

    public static BType getType(Object value) {
        if (value == null) {
            return BTypes.typeNull;
        } else if (value instanceof Number) {
            if (value instanceof Long) {
                return BTypes.typeInt;
            } else if (value instanceof Double) {
                return BTypes.typeFloat;
            } else if (value instanceof Integer || value instanceof Byte) {
                return BTypes.typeByte;
            }
        } else if (value instanceof String || value instanceof BString) {
            return BTypes.typeString;
        } else if (value instanceof Boolean) {
            return BTypes.typeBoolean;
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
        return checkDecimalGreaterThanOrEqual(rhsValue, lhsValue);
    }

    /**
     * Check if left hand side decimal value is less than or equal the right hand side decimal value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value of the right hand side
     * @return True if left hand value is less than or equal right hand side value, else false.
     */
    public static boolean checkDecimalLessThanOrEqual(DecimalValue lhsValue, DecimalValue rhsValue) {
        return checkDecimalGreaterThan(rhsValue, lhsValue);
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

        BType lhsType = getType(lhsValue);
        BType rhsType = getType(rhsValue);

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
        BType type = TypeChecker.getType(value);
        if (type == null) {
            return null;
        }
        if (value instanceof MapValue) {
            TypedescValue typedesc = ((MapValue) value).getTypedesc();
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
        BType describingType = typedescValue.getDescribingType();
        if (!(describingType instanceof AnnotatableType)) {
            return null;
        }
        return ((AnnotatableType) describingType).getAnnotation(StringUtils.fromString(annotTag));
    }

    public static Object getAnnotValue(TypedescValue typedescValue, BString annotTag) {
        BType describingType = typedescValue.getDescribingType();
        if (!(describingType instanceof AnnotatableType)) {
            return null;
        }
        return ((AnnotatableType) describingType).getAnnotation(annotTag);
    }

    /**
     * Check whether a given type is equivalent to a target type.
     *
     * @param sourceType type to check
     * @param targetType type to compare with
     * @return flag indicating the the equivalence of the two types
     */
    public static boolean checkIsType(BType sourceType, BType targetType) {
        return checkIsType(sourceType, targetType, (List<TypePair>) null);
    }

    @Deprecated
    public static boolean checkIsType(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
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

    // Private methods

    private static boolean checkTypeDescType(BType sourceType, BTypedescType targetType,
            List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.TYPEDESC_TAG) {
            return false;
        }

        BTypedescType sourceTypedesc = (BTypedescType) sourceType;
        return checkIsType(sourceTypedesc.getConstraint(), targetType.getConstraint(), unresolvedTypes);
    }

    private static boolean checkIsRecursiveType(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
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

    private static boolean isFiniteTypeMatch(BFiniteType sourceType, BType targetType) {
        for (Object bValue : sourceType.valueSpace) {
            if (!checkIsType(bValue, targetType)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isUnionTypeMatch(BUnionType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
        for (BType type : sourceType.getMemberTypes()) {
            if (!checkIsType(type, targetType, unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsUnionType(BType sourceType, BUnionType targetType, List<TypePair> unresolvedTypes) {
        switch (sourceType.getTag()) {
            case TypeTags.UNION_TAG:
                return isUnionTypeMatch((BUnionType) sourceType, targetType, unresolvedTypes);
            case TypeTags.FINITE_TYPE_TAG:
                return isFiniteTypeMatch((BFiniteType) sourceType, targetType);
            default:
                for (BType type : targetType.getMemberTypes()) {
                    if (checkIsType(sourceType, type, unresolvedTypes)) {
                        return true;
                    }
                }
                return false;

        }
    }

    private static boolean checkIsMapType(BType sourceType, BMapType targetType, List<TypePair> unresolvedTypes) {
        BType targetConstrainedType = targetType.getConstrainedType();
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

    private static boolean checkIsXMLType(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() == TypeTags.FINITE_TYPE_TAG) {
            return isFiniteTypeMatch((BFiniteType) sourceType, targetType);
        }
        BXMLType target = ((BXMLType) targetType);
        if (sourceType.getTag() == TypeTags.XML_TAG) {
            BType targetConstraint = target.constraint;
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

    private static List<BType> getWideTypeComponents(BRecordType recType) {
        List<BType> types = new ArrayList<>();
        for (BField f : recType.getFields().values()) {
            types.add(f.type);
        }
        if (!recType.sealed) {
            types.add(recType.restFieldType);
        }
        return types;
    }

    private static boolean checkIsStreamType(BType sourceType, BStreamType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.STREAM_TAG) {
            return false;
        }
        return checkConstraints(((BStreamType) sourceType).getConstrainedType(), targetType.getConstrainedType(),
                               unresolvedTypes);
    }

    private static boolean checkIsTableType(BType sourceType, BTableType targetType, List<TypePair> unresolvedTypes) {
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

            List<BType> fieldTypes = new ArrayList<>();
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

    static BField getTableConstraintField(BType constraintType, String fieldName) {

        switch (constraintType.getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
                Map<String, BField> fieldList = ((BRecordType) constraintType).getFields();
                return fieldList.get(fieldName);

            case TypeTags.UNION_TAG:
                BUnionType unionType = (BUnionType) constraintType;
                List<BType> memTypes = unionType.getMemberTypes();
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

    private static boolean checkIsJSONType(BType sourceType, List<TypePair> unresolvedTypes) {
        BJSONType jsonType = (BJSONType) BTypes.typeJSON;

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
                for (BField field : recordType.getFields().values()) {
                    if (!checkIsJSONType(field.type, unresolvedTypes)) {
                        return false;
                    }
                }

                if (!recordType.sealed) {
                    return checkIsJSONType(recordType.restFieldType, unresolvedTypes);
                }
                return true;
            case TypeTags.UNION_TAG:
                for (BType memberType : ((BUnionType) sourceType).getMemberTypes()) {
                    if (!checkIsJSONType(memberType, unresolvedTypes)) {
                        return false;
                    }
                }
                return true;
            default:
                return false;
        }
    }

    private static boolean checkIsRecordType(BType sourceType, BRecordType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.RECORD_TYPE_TAG) {
            return false;
        }

        // If we encounter two types that we are still resolving, then skip it.
        // This is done to avoid recursive checking of the same type.
        TypePair pair = new TypePair(sourceType, targetType);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);

        // Unsealed records are not equivalent to sealed records. But vice-versa is allowed.
        BRecordType sourceRecordType = (BRecordType) sourceType;
        if (targetType.sealed && !sourceRecordType.sealed) {
            return false;
        }

        // If both are sealed (one is sealed means other is also sealed) check the rest field type
        if (!sourceRecordType.sealed &&
                !checkIsType(sourceRecordType.restFieldType, targetType.restFieldType, unresolvedTypes)) {
            return false;
        }

        Map<String, BField> sourceFields = sourceRecordType.getFields();
        Set<String> targetFieldNames = targetType.getFields().keySet();

        for (BField targetField : targetType.getFields().values()) {
            BField sourceField = sourceFields.get(targetField.getFieldName());

            if (sourceField == null) {
                return false;
            }

            // If the target field is required, the source field should be required as well.
            if (!Flags.isFlagOn(targetField.flags, Flags.OPTIONAL)
                    && Flags.isFlagOn(sourceField.flags, Flags.OPTIONAL)) {
                return false;
            }

            if (!checkIsType(sourceField.type, targetField.type, unresolvedTypes)) {
                return false;
            }
        }

        // If there are fields remaining in the source record, first check if it's a closed record. Closed records
        // should only have the fields specified by its type.
        if (targetType.sealed) {
            return targetFieldNames.containsAll(sourceFields.keySet());
        }

        // If it's an open record, check if they are compatible with the rest field of the target type.
        for (BField field : sourceFields.values()) {
            if (targetFieldNames.contains(field.name)) {
                continue;
            }

            if (!checkIsType(field.getFieldType(), targetType.restFieldType, unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsArrayType(BType sourceType, BArrayType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() == TypeTags.UNION_TAG) {
            for (BType memberType : ((BUnionType) sourceType).getMemberTypes()) {
                if (!checkIsArrayType(memberType, targetType, unresolvedTypes)) {
                    return false;
                }
            }
            return true;
        }

        if (sourceType.getTag() != TypeTags.ARRAY_TAG && sourceType.getTag() != TypeTags.TUPLE_TAG) {
            return false;
        }

        BArrayType sourceArrayType;
        if (sourceType.getTag() == TypeTags.ARRAY_TAG) {
            sourceArrayType = (BArrayType) sourceType;
        } else {
            BTupleType sourceTupleType = (BTupleType) sourceType;
            Set<BType> tupleTypes = new HashSet<>(sourceTupleType.getTupleTypes());
            if (sourceTupleType.getRestType() != null) {
                tupleTypes.add(sourceTupleType.getRestType());
            }
            if (tupleTypes.isEmpty()) {
                return targetType.getState() == ArrayState.UNSEALED || targetType.getSize() == 0;
            }
            sourceArrayType =
                    new BArrayType(new BUnionType(new ArrayList<>(tupleTypes), sourceTupleType.getTypeFlags()));
        }

        switch (sourceArrayType.getState()) {
            case UNSEALED:
                if (targetType.getState() != ArrayState.UNSEALED) {
                    return false;
                }
                break;
            case CLOSED_SEALED:
                if (targetType.getState() == ArrayState.CLOSED_SEALED &&
                        sourceArrayType.getSize() != targetType.getSize()) {
                    return false;
                }
                break;
        }

        //If element type is a value type, then check same type
        BType targetElementType = targetType.getElementType();
        int targetElementTypeTag = targetElementType.getTag();

        BType sourceElementType = sourceArrayType.getElementType();

        if (targetElementTypeTag <= TypeTags.BOOLEAN_TAG) {
            if (targetElementTypeTag == TypeTags.INT_TAG && sourceElementType.getTag() == TypeTags.BYTE_TAG) {
                return true;
            }

            return sourceElementType.getTag() == targetElementTypeTag;
        }
        return checkIsType(sourceElementType, targetElementType, unresolvedTypes);
    }

    private static boolean checkIsTupleType(BType sourceType, BTupleType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.TUPLE_TAG) {
            return false;
        }

        List<BType> sourceTypes = new ArrayList<>(((BTupleType) sourceType).getTupleTypes());
        BType sourceRestType = ((BTupleType) sourceType).getRestType();
        if (sourceRestType != null) {
            sourceTypes.add(sourceRestType);
        }

        List<BType> targetTypes = new ArrayList<>(targetType.getTupleTypes());
        BType targetRestType = targetType.getRestType();
        if (targetRestType != null) {
            targetTypes.add(targetRestType);
        }

        if (sourceTypes.size() != targetTypes.size()) {
            return false;
        }

        for (int i = 0; i < sourceTypes.size(); i++) {
            if (!checkIsType(sourceTypes.get(i), targetTypes.get(i), unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsAnyType(BType sourceType) {
        switch (sourceType.getTag()) {
            case TypeTags.ERROR_TAG:
                return false;
            case TypeTags.UNION_TAG:
                for (BType memberType : ((BUnionType) sourceType).getMemberTypes()) {
                    if (!checkIsAnyType(memberType)) {
                        return false;
                    }
                }
                return true;
        }
        return true;
    }

    private static boolean checkIsFiniteType(BType sourceType, BFiniteType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.FINITE_TYPE_TAG) {
            return false;
        }

        BFiniteType sourceFiniteType = (BFiniteType) sourceType;
        if (sourceFiniteType.valueSpace.size() != targetType.valueSpace.size()) {
            return false;
        }

        return targetType.valueSpace.containsAll(sourceFiniteType.valueSpace);
    }

    private static boolean checkIsFutureType(BType sourceType, BFutureType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.FUTURE_TAG) {
            return false;
        }
        return checkConstraints(((BFutureType) sourceType).getConstrainedType(), targetType.getConstrainedType(),
                unresolvedTypes);
    }

    private static boolean checkObjectEquivalency(BType sourceType, BObjectType targetType,
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
        Map<String, BField> targetFields = targetType.getFields();
        Map<String, BField> sourceFields = sourceObjectType.getFields();
        AttachedFunction[] targetFuncs = targetType.getAttachedFunctions();
        AttachedFunction[] sourceFuncs = sourceObjectType.getAttachedFunctions();

        if (targetType.getFields().values().stream().anyMatch(field -> Flags.isFlagOn(field.flags, Flags.PRIVATE))
                || Stream.of(targetFuncs).anyMatch(func -> Flags.isFlagOn(func.flags, Flags.PRIVATE))) {
            return false;
        }

        if (targetFields.size() > sourceFields.size() || targetFuncs.length > sourceFuncs.length) {
            return false;
        }

        for (BField lhsField : targetFields.values()) {
            BField rhsField = sourceFields.get(lhsField.name);
            if (rhsField == null ||
                !isInSameVisibilityRegion(Optional.ofNullable(lhsField.type.getPackage()).map(BPackage::getName)
                        .orElse(""), Optional.ofNullable(rhsField.type.getPackage()).map(BPackage::getName)
                        .orElse(""), lhsField.flags, rhsField.flags) ||
                    !checkIsType(rhsField.type, lhsField.type, new ArrayList<>())) {
                return false;
            }
        }

        for (AttachedFunction lhsFunc : targetFuncs) {
            AttachedFunction rhsFunc = getMatchingInvokableType(sourceFuncs, lhsFunc, unresolvedTypes);
            if (rhsFunc == null ||
                    !isInSameVisibilityRegion(Optional.ofNullable(lhsFunc.type.getPackage())
                                    .map(BPackage::getName)
                                    .orElse(""),
                            Optional.ofNullable(rhsFunc.type.getPackage()).map(BPackage::getName).orElse(""),
                            lhsFunc.flags, rhsFunc.flags)) {
                return false;
            }
        }

        return true;
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

    private static AttachedFunction getMatchingInvokableType(AttachedFunction[] rhsFuncs, AttachedFunction lhsFunc,
                                                             List<TypePair> unresolvedTypes) {
        return Arrays.stream(rhsFuncs)
                .filter(rhsFunc -> lhsFunc.funcName.equals(rhsFunc.funcName))
                .filter(rhsFunc -> checkFunctionTypeEqualityForObjectType(rhsFunc.type, lhsFunc.type, unresolvedTypes))
                .findFirst()
                .orElse(null);
    }

    private static boolean checkFunctionTypeEqualityForObjectType(BFunctionType source, BFunctionType target,
                                                                  List<TypePair> unresolvedTypes) {
        if (source.paramTypes.length != target.paramTypes.length) {
            return false;
        }

        for (int i = 0; i < source.paramTypes.length; i++) {
            if (!checkIsType(target.paramTypes[i], source.paramTypes[i], unresolvedTypes)) {
                return false;
            }
        }

        if (source.retType == null && target.retType == null) {
            return true;
        } else if (source.retType == null || target.retType == null) {
            return false;
        }

        return checkIsType(source.retType, target.retType, unresolvedTypes);
    }

    private static boolean checkIsFunctionType(BType sourceType, BFunctionType targetType) {
        if (sourceType.getTag() != TypeTags.FUNCTION_POINTER_TAG) {
            return false;
        }

        BFunctionType source = (BFunctionType) sourceType;
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

    private static boolean checkIsServiceType(BType sourceType) {
        if (sourceType.getTag() == TypeTags.SERVICE_TAG) {
            return true;
        }

        if (sourceType.getTag() == TypeTags.OBJECT_TYPE_TAG) {
            int flags = ((BObjectType) sourceType).flags;
            return (flags & Flags.SERVICE) == Flags.SERVICE;
        }

        return false;
    }

    public static boolean isInherentlyImmutableType(BType sourceType) {
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
            case TypeTags.HANDLE_TAG:
                return true;
        }
        return false;
    }

    boolean isSelectivelyImmutableType(BType type) {
        return isSelectivelyImmutableType(type, new HashSet<>());
    }

    public static boolean isSelectivelyImmutableType(BType type, Set<BType> unresolvedTypes) {
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
                BType elementType = ((BArrayType) type).getElementType();
                return isInherentlyImmutableType(elementType) ||
                        isSelectivelyImmutableType(elementType, unresolvedTypes);
            case TypeTags.TUPLE_TAG:
                BTupleType tupleType = (BTupleType) type;
                for (BType tupMemType : tupleType.getTupleTypes()) {
                    if (!isInherentlyImmutableType(tupMemType) &&
                            !isSelectivelyImmutableType(tupMemType, unresolvedTypes)) {
                        return false;
                    }
                }

                BType tupRestType = tupleType.getRestType();
                if (tupRestType == null) {
                    return true;
                }

                return isInherentlyImmutableType(tupRestType) ||
                        isSelectivelyImmutableType(tupRestType, unresolvedTypes);
            case TypeTags.RECORD_TYPE_TAG:
                BRecordType recordType = (BRecordType) type;
                for (BField field : recordType.getFields().values()) {
                    BType fieldType = field.type;
                    if (!isInherentlyImmutableType(fieldType) &&
                            !isSelectivelyImmutableType(fieldType, unresolvedTypes)) {
                        return false;
                    }
                }

                BType recordRestType = recordType.restFieldType;
                if (recordRestType == null) {
                    return true;
                }

                return isInherentlyImmutableType(recordRestType) ||
                        isSelectivelyImmutableType(recordRestType, unresolvedTypes);
            case TypeTags.MAP_TAG:
                BType constraintType = ((BMapType) type).getConstrainedType();
                return isInherentlyImmutableType(constraintType) ||
                        isSelectivelyImmutableType(constraintType, unresolvedTypes);
            case TypeTags.TABLE_TAG:
                BType tableConstraintType = ((BTableType) type).getConstrainedType();
                return isInherentlyImmutableType(tableConstraintType) ||
                        isSelectivelyImmutableType(tableConstraintType, unresolvedTypes);
            case TypeTags.UNION_TAG:
                boolean readonlyIntersectionExists = false;
                for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                    if (isInherentlyImmutableType(memberType) ||
                            isSelectivelyImmutableType(memberType, unresolvedTypes)) {
                        readonlyIntersectionExists = true;
                    }
                }
                return readonlyIntersectionExists;
        }
        return false;
    }

    private static boolean checkConstraints(BType sourceConstraint, BType targetConstraint,
                                            List<TypePair> unresolvedTypes) {
        if (sourceConstraint == null) {
            sourceConstraint = BTypes.typeAny;
        }

        if (targetConstraint == null) {
            targetConstraint = BTypes.typeAny;
        }

        return checkIsType(sourceConstraint, targetConstraint, unresolvedTypes);
    }

    private static boolean isMutable(Object value, BType sourceType) {
        // All the value types are immutable
        if (value == null || sourceType.getTag() < TypeTags.JSON_TAG || TypeTags.isIntegerTypeTag(sourceType.getTag())
                || sourceType.getTag() == TypeTags.FINITE_TYPE_TAG || TypeTags.isStringTypeTag(sourceType.getTag())) {
            return false;
        }

        return !((RefValue) value).isFrozen();
    }

    private static boolean checkArrayEquivalent(BType actualType, BType expType) {
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
    private static boolean checkIsLikeType(Object sourceValue, BType targetType, List<TypeValuePair> unresolvedValues,
                                           boolean allowNumericConversion) {
        BType sourceType = getType(sourceValue);
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
    private static boolean checkIsLikeOnValue(Object sourceValue, BType sourceType, BType targetType,
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
                    List<BType> compatibleTypesWithNumConversion = new ArrayList<>();
                    List<BType> compatibleTypesWithoutNumConversion = new ArrayList<>();
                    for (BType type : ((BUnionType) targetType).getMemberTypes()) {
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
                    for (BType type : ((BUnionType) targetType).getMemberTypes()) {
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

    private static XMLNodeType getXmlNodeType(BType type) {
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

    private static boolean checkIsLikeNonElementSingleton(XMLValue xmlSource, BType targetType) {

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

    private static boolean checkIsLikeXMLSequenceType(XMLValue xmlSource, BType targetType) {
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

        for (BType memberType : unionType.getMemberTypes()) {
            if (memberType.getTag() == TypeTags.UNION_TAG) {
                getXMLNodeOnUnion((BUnionType) memberType, nodeTypes);
            } else {
               nodeTypes.add(getXmlNodeType(memberType));
            }
        }
    }
    public static boolean isNumericType(BType type) {
        return type.getTag() < TypeTags.STRING_TAG;
    }

    private static boolean checkIsLikeAnydataType(Object sourceValue, BType sourceType,
                                                  List<TypeValuePair> unresolvedValues,
                                                  boolean allowNumericConversion) {
        switch (sourceType.getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.JSON_TAG:
            case TypeTags.MAP_TAG:
                return isLikeType(((MapValueImpl) sourceValue).values().toArray(), BTypes.typeAnydata,
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
                        return isLikeType(arr.getValues(), BTypes.typeAnydata, unresolvedValues,
                                          allowNumericConversion);
                }
            case TypeTags.TUPLE_TAG:
                return isLikeType(((ArrayValue) sourceValue).getValues(), BTypes.typeAnydata, unresolvedValues,
                                  allowNumericConversion);
            case TypeTags.ANYDATA_TAG:
                return true;
            // TODO: 8/13/19 Check if can be removed
            case TypeTags.FINITE_TYPE_TAG:
            case TypeTags.UNION_TAG:
                return checkIsLikeType(sourceValue, BTypes.typeAnydata, unresolvedValues, allowNumericConversion);
            default:
                return false;
        }
    }

    private static boolean isLikeType(Object[] objects, BType targetType, List<TypeValuePair> unresolvedValues,
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
        if (source.size() != targetType.getTupleTypes().size()) {
            return false;
        }

        int bound = source.size();
        for (int i = 0; i < bound; i++) {
            BType elementType = getArrayElementType(source, i);
            if (BTypes.isValueType(elementType)) {
                if (!checkIsType(elementType, targetType.getTupleTypes().get(i), new ArrayList<>())) {
                    return false;
                }
            } else {
                if (!checkIsLikeType(source.getRefValue(i), targetType.getTupleTypes().get(i), unresolvedValues,
                        allowNumericConversion)) {
                    return false;
                }
            }
        }
        return true;

    }

    private static BType getArrayElementType(ArrayValue source, int elementIndex) {
        BType type = source.getType();
        switch (type.getTag()) {
            case TypeTags.TUPLE_TAG:
                BTupleType tupleType = (BTupleType) type;
                int fixedLen = tupleType.getTupleTypes().size();
                return (elementIndex < fixedLen) ? tupleType.getTupleTypes().get(elementIndex)
                        : tupleType.getRestType();
            case TypeTags.ARRAY_TAG:
                return ((BArrayType) source.getType()).getElementType();
            default:
                throw new IllegalStateException();
        }
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
        BType targetTypeElementType = targetType.getElementType();
        if (source.getType().getTag() == TypeTags.ARRAY_TAG) {
            BType sourceElementType = ((BArrayType) source.getType()).getElementType();
            if (BTypes.isValueType(sourceElementType)) {
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

                List<BType> targetNumericTypes = new ArrayList<>();
                for (BType memType : ((BUnionType) targetTypeElementType).getMemberTypes()) {
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

    private static boolean checkIsLikeJSONType(Object sourceValue, BType sourceType, BJSONType targetType,
                                               List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        if (sourceType.getTag() == TypeTags.ARRAY_TAG) {
            ArrayValue source = (ArrayValue) sourceValue;
            BType elementType = ((BArrayType) source.getType()).getElementType();
            if (BTypes.isValueType(elementType)) {
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
        Map<String, BType> targetTypeField = new HashMap<>();
        BType restFieldType = targetType.restFieldType;

        for (BField field : targetType.getFields().values()) {
            targetTypeField.put(field.getFieldName(), field.type);
        }

        for (Map.Entry targetTypeEntry : targetTypeField.entrySet()) {
            Object fieldName = StringUtils.fromString(targetTypeEntry.getKey().toString());
            if (!(((MapValueImpl) sourceValue).containsKey(fieldName)) &&
                    !Flags.isFlagOn(targetType.getFields().get(fieldName.toString()).flags, Flags.OPTIONAL)) {
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

    private static boolean checkFiniteTypeAssignable(Object sourceValue, BType sourceType, BFiniteType targetType) {
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

    protected static boolean isFiniteTypeValue(Object sourceValue, BType sourceType, Object valueSpaceItem) {
        BType valueSpaceItemType = getType(valueSpaceItem);
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

    private static boolean checkIsErrorType(BType sourceType, BErrorType targetType, List<TypePair> unresolvedTypes) {
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
        BType sourceType = getType(sourceValue);
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

    private static boolean isSimpleBasicType(BType type) {
        return type.getTag() < TypeTags.JSON_TAG || TypeTags.isIntegerTypeTag(type.getTag());
    }

    private static boolean isHandleType(BType type) {
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
        BType sourceType;
        BType targetType;

        public TypePair(BType sourceType, BType targetType) {
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
    public static boolean hasFillerValue(BType type) {
        return hasFillerValue(type, new ArrayList<>());
    }

    private static boolean hasFillerValue(BType type, List<BType> unanalyzedTypes) {
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
        Iterator<BType> iterator = type.getMemberTypes().iterator();
        BType firstMember;
        for (firstMember = iterator.next(); iterator.hasNext(); ) {
            if (!isSameType(firstMember, iterator.next())) {
                return false;
            }
        }
        // Control reaching this point means there is only one type in the union.
        return BTypes.isValueType(firstMember) && hasFillerValue(firstMember);
    }

    private static boolean checkFillerValue(BRecordType type, List<BType> unAnalyzedTypes) {
        if (unAnalyzedTypes.contains(type)) {
            return true;
        }
        unAnalyzedTypes.add(type);
        for (BField field : type.getFields().values()) {
            if (Flags.isFlagOn(field.flags, Flags.OPTIONAL)) {
                continue;
            }
            if (!Flags.isFlagOn(field.flags, Flags.REQUIRED)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static boolean checkFillerValue(BArrayType type) {
        return type.getState() == ArrayState.UNSEALED || hasFillerValue(type.getElementType());
    }

    private static boolean checkFillerValue(BObjectType type) {
        if (type.getTag() == TypeTags.SERVICE_TAG) {
            return false;
        } else {
            AttachedFunction generatedInitializer = type.generatedInitializer;
            if (generatedInitializer == null) {
                // abstract objects doesn't have a filler value.
                return false;
            }
            BFunctionType initFuncType = generatedInitializer.type;
            // Todo: check defaultable params of the init func as well
            boolean noParams = initFuncType.paramTypes.length == 0;
            boolean nilReturn = initFuncType.retType.getTag() == TypeTags.NULL_TAG;
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

    private static boolean containsType(Set<Object> valueSpace, BType type) {
        for (Object value : valueSpace) {
            if (!isSameType(type, getType(value))) {
                return false;
            }
        }
        return true;
    }
}
