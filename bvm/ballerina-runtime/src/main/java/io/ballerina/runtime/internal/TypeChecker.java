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
package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType.ArrayState;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ReferenceType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.commons.TypeValuePair;
import io.ballerina.runtime.internal.types.BAnnotatableType;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BErrorType;
import io.ballerina.runtime.internal.types.BField;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BFunctionType;
import io.ballerina.runtime.internal.types.BFutureType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BJsonType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BNetworkObjectType;
import io.ballerina.runtime.internal.types.BObjectType;
import io.ballerina.runtime.internal.types.BParameterizedType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BResourceMethodType;
import io.ballerina.runtime.internal.types.BStreamType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BTypeIdSet;
import io.ballerina.runtime.internal.types.BTypeReferenceType;
import io.ballerina.runtime.internal.types.BTypedescType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.types.BXmlType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.HandleValue;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.RefValue;
import io.ballerina.runtime.internal.values.RegExpValue;
import io.ballerina.runtime.internal.values.StreamValue;
import io.ballerina.runtime.internal.values.TableValueImpl;
import io.ballerina.runtime.internal.values.TupleValueImpl;
import io.ballerina.runtime.internal.values.TypedescValue;
import io.ballerina.runtime.internal.values.TypedescValueImpl;
import io.ballerina.runtime.internal.values.XmlComment;
import io.ballerina.runtime.internal.values.XmlItem;
import io.ballerina.runtime.internal.values.XmlPi;
import io.ballerina.runtime.internal.values.XmlSequence;
import io.ballerina.runtime.internal.values.XmlText;
import io.ballerina.runtime.internal.values.XmlValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import static io.ballerina.runtime.api.constants.RuntimeConstants.BBYTE_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BBYTE_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED8_MAX_VALUE;
import static io.ballerina.runtime.api.utils.TypeUtils.getReferredType;
import static io.ballerina.runtime.api.utils.TypeUtils.isValueType;

/**
 * Responsible for performing runtime type checking.
 *
 * @since 0.995.0
 */
@SuppressWarnings({"rawtypes"})
public class TypeChecker {

    private static final byte MAX_TYPECAST_ERROR_COUNT = 20;

    public static Object checkCast(Object sourceVal, Type targetType) {

        List<String> errors = new ArrayList<>();
        Type sourceType = TypeUtils.getReferredType(getType(sourceVal));
        if (checkIsType(errors, sourceVal, sourceType, targetType)) {
            return sourceVal;
        }

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

        throw createTypeCastError(sourceVal, targetType, errors);
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
        return TypeConverter.anyToDecimalCast(sourceVal, () -> ErrorUtils.createTypeCastError(sourceVal,
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
        return checkIsType(null, sourceVal, getType(sourceVal), targetType);
    }

    /**
     * Check whether a given value belongs to the given type.
     *
     * @param errors list to collect typecast errors
     * @param sourceVal value to check the type
     * @param sourceType type of the value
     * @param targetType type to be test against
     * @return true if the value belongs to the given type, false otherwise
     */
    public static boolean checkIsType(List<String> errors, Object sourceVal, Type sourceType, Type targetType) {
        if (checkIsType(sourceVal, sourceType, targetType, null)) {
            return true;
        }

        if (sourceType.getTag() == TypeTags.XML_TAG) {
            XmlValue val = (XmlValue) sourceVal;
            if (val.getNodeType() == XmlNodeType.SEQUENCE) {
                return checkIsLikeOnValue(errors, sourceVal, sourceType, targetType, new ArrayList<>(), false, null);
            }
        }

        if (isMutable(sourceVal, sourceType)) {
            return false;
        }

        return checkIsLikeOnValue(errors, sourceVal, sourceType, targetType, new ArrayList<>(), false, null);
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
        return checkIsLikeType(null, sourceValue, targetType, new ArrayList<>(), allowNumericConversion,
                null);
    }

    /**
     * Check whether two types are the same.
     *
     * @param sourceType type to test
     * @param targetType type to test against
     * @return true if the two types are same; false otherwise
     */
    public static boolean isSameType(Type sourceType, Type targetType) {

        int sourceTypeTag = sourceType.getTag();
        int targetTypeTag = targetType.getTag();

        if (sourceType == targetType) {
            return true;
        }
        if (sourceTypeTag == targetTypeTag) {
            if (sourceType.equals(targetType)) {
                return true;
            }
            switch (sourceTypeTag) {
                case TypeTags.ARRAY_TAG:
                    return checkArrayEquivalent(sourceType, targetType);
                case TypeTags.FINITE_TYPE_TAG:
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
                default:
                    break;

            }
        }

        // all the types in a finite type may evaluate to target type
        switch (sourceTypeTag) {
            case TypeTags.FINITE_TYPE_TAG:
                for (Object value : ((BFiniteType) sourceType).valueSpace) {
                    if (!isSameType(getType(value), targetType)) {
                        return false;
                    }
                }
                return true;
            default:
                break;
        }

        if (targetTypeTag == TypeTags.FINITE_TYPE_TAG) {
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
        } else if (value instanceof BString) {
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
     * Check if two decimal values are exactly equal.
     *
     * @param lhsValue The value on the left-hand side
     * @param rhsValue The value of the right-hand side
     * @return True if values are exactly equal, else false.
     */

    public static boolean checkDecimalExactEqual(DecimalValue lhsValue, DecimalValue rhsValue) {
        return isDecimalRealNumber(lhsValue) && isDecimalRealNumber(rhsValue)
                && lhsValue.decimalValue().equals(rhsValue.decimalValue());
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

        switch (lhsType.getTag()) {
            case TypeTags.FLOAT_TAG:
                if (rhsType.getTag() != TypeTags.FLOAT_TAG) {
                    return false;
                }
                return lhsValue.equals(((Number) rhsValue).doubleValue());
            case TypeTags.DECIMAL_TAG:
                if (rhsType.getTag() != TypeTags.DECIMAL_TAG) {
                    return false;
                }
                return checkDecimalExactEqual((DecimalValue) lhsValue, (DecimalValue) rhsValue);
            case TypeTags.INT_TAG:
            case TypeTags.BYTE_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.STRING_TAG:
                return isEqual(lhsValue, rhsValue);
            case TypeTags.XML_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TEXT_TAG:
                if (!TypeTags.isXMLTypeTag(rhsType.getTag())) {
                    return false;
                }
                return isXMLValueRefEqual((XmlValue) lhsValue, (XmlValue) rhsValue);
            case TypeTags.HANDLE_TAG:
                if (rhsType.getTag() != TypeTags.HANDLE_TAG) {
                    return false;
                }
                return isHandleValueRefEqual(lhsValue, rhsValue);
            case TypeTags.FUNCTION_POINTER_TAG:
                return lhsType.getPackage().equals(rhsType.getPackage()) &&
                        lhsType.getName().equals(rhsType.getName()) && rhsType.equals(lhsType);
            default:
                if (lhsValue instanceof RegExpValue && rhsValue instanceof RegExpValue) {
                    return isEqual((RegExpValue) lhsValue, (RegExpValue) rhsValue);
                }
                return false;
        }
    }

    private static boolean isXMLValueRefEqual(XmlValue lhsValue, XmlValue rhsValue) {
        if (lhsValue.getNodeType() == XmlNodeType.SEQUENCE && lhsValue.isSingleton()) {
            return ((XmlSequence) lhsValue).getChildrenList().get(0) == rhsValue;
        }
        if (rhsValue.getNodeType() == XmlNodeType.SEQUENCE && rhsValue.isSingleton()) {
            return ((XmlSequence) rhsValue).getChildrenList().get(0) == lhsValue;
        }
        if (lhsValue.getNodeType() != rhsValue.getNodeType()) {
            return false;
        }
        if (lhsValue.getNodeType() == XmlNodeType.SEQUENCE && rhsValue.getNodeType() == XmlNodeType.SEQUENCE) {
            return isXMLSequenceRefEqual((XmlSequence) lhsValue, (XmlSequence) rhsValue);
        }
        if (lhsValue.getNodeType() == XmlNodeType.TEXT && rhsValue.getNodeType() == XmlNodeType.TEXT) {
            return isEqual(lhsValue, rhsValue);
        }
        return false;
    }

    private static boolean isXMLSequenceRefEqual(XmlSequence lhsValue, XmlSequence rhsValue) {
        Iterator<BXml> lhsIter = lhsValue.getChildrenList().iterator();
        Iterator<BXml> rhsIter = rhsValue.getChildrenList().iterator();
        while (lhsIter.hasNext() && rhsIter.hasNext()) {
            BXml l = lhsIter.next();
            BXml r = rhsIter.next();
            if (!(l == r || isXMLValueRefEqual((XmlValue) l, (XmlValue) r))) {
                return false;
            }
        }
        // lhs hasNext = false & rhs hasNext = false -> empty sequences, hence ref equal
        // lhs hasNext = true & rhs hasNext = true would never reach here
        // only one hasNext method returns true means sequences are of different sizes, hence not ref equal
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
        if (isSimpleBasicType(type)) {
            return new TypedescValueImpl(new BFiniteType(value.toString(), Set.of(value), 0));
        }
        if (value instanceof RefValue) {
            return (TypedescValue) ((RefValue) value).getTypedesc();
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
        if (sourceType == targetType || (sourceType.getTag() == targetType.getTag() && sourceType.equals(targetType))) {
            return true;
        }

        if (checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(sourceType)) {
            return true;
        }

        if (targetType.isReadOnly() && !sourceType.isReadOnly()) {
            return false;
        }

        int sourceTypeTag = sourceType.getTag();
        int targetTypeTag = targetType.getTag();

        switch (sourceTypeTag) {
            case TypeTags.INTERSECTION_TAG:
                return checkIsType(((BIntersectionType) sourceType).getEffectiveType(),
                        targetTypeTag != TypeTags.INTERSECTION_TAG ? targetType :
                                ((BIntersectionType) targetType).getEffectiveType(), unresolvedTypes);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return checkIsType(((BTypeReferenceType) sourceType).getReferredType(),
                        targetTypeTag != TypeTags.TYPE_REFERENCED_TYPE_TAG ? targetType :
                                ((BTypeReferenceType) targetType).getReferredType(), unresolvedTypes);
            case TypeTags.PARAMETERIZED_TYPE_TAG:
                if (targetTypeTag != TypeTags.PARAMETERIZED_TYPE_TAG) {
                    return checkIsType(((BParameterizedType) sourceType).getParamValueType(), targetType,
                            unresolvedTypes);
                }
                return checkIsType(((BParameterizedType) sourceType).getParamValueType(),
                        ((BParameterizedType) targetType).getParamValueType(), unresolvedTypes);
            case TypeTags.READONLY_TAG:
                return checkIsType(PredefinedTypes.ANY_AND_READONLY_OR_ERROR_TYPE,
                        targetType, unresolvedTypes);
            case TypeTags.UNION_TAG:
                return isUnionTypeMatch((BUnionType) sourceType, targetType, unresolvedTypes);
            case TypeTags.FINITE_TYPE_TAG:
                if ((targetTypeTag == TypeTags.FINITE_TYPE_TAG || targetTypeTag <= TypeTags.NULL_TAG ||
                        targetTypeTag == TypeTags.XML_TEXT_TAG)) {
                    return isFiniteTypeMatch((BFiniteType) sourceType, targetType);
                }
                break;
            default:
                break;
        }

        switch (targetTypeTag) {
            case TypeTags.BYTE_TAG:
            case TypeTags.SIGNED8_INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.CHAR_STRING_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.NULL_TAG:
                return sourceTypeTag == targetTypeTag;
            case TypeTags.STRING_TAG:
                return TypeTags.isStringTypeTag(sourceTypeTag);
            case TypeTags.XML_TEXT_TAG:
                if (sourceTypeTag == TypeTags.XML_TAG) {
                    return ((BXmlType) sourceType).constraint.getTag() == TypeTags.NEVER_TAG;
                }
                return sourceTypeTag == targetTypeTag;
            case TypeTags.INT_TAG:
                return sourceTypeTag == TypeTags.INT_TAG || sourceTypeTag == TypeTags.BYTE_TAG ||
                        (sourceTypeTag >= TypeTags.SIGNED8_INT_TAG && sourceTypeTag <= TypeTags.UNSIGNED32_INT_TAG);
            case TypeTags.SIGNED16_INT_TAG:
                return sourceTypeTag == TypeTags.BYTE_TAG ||
                        (sourceTypeTag >= TypeTags.SIGNED8_INT_TAG && sourceTypeTag <= TypeTags.SIGNED16_INT_TAG);
            case TypeTags.SIGNED32_INT_TAG:
                return sourceTypeTag == TypeTags.BYTE_TAG ||
                        (sourceTypeTag >= TypeTags.SIGNED8_INT_TAG && sourceTypeTag <= TypeTags.SIGNED32_INT_TAG);
            case TypeTags.UNSIGNED8_INT_TAG:
                return sourceTypeTag == TypeTags.BYTE_TAG || sourceTypeTag == TypeTags.UNSIGNED8_INT_TAG;
            case TypeTags.UNSIGNED16_INT_TAG:
                return sourceTypeTag == TypeTags.BYTE_TAG || sourceTypeTag == TypeTags.UNSIGNED8_INT_TAG ||
                        sourceTypeTag == TypeTags.UNSIGNED16_INT_TAG;
            case TypeTags.UNSIGNED32_INT_TAG:
                return sourceTypeTag == TypeTags.BYTE_TAG || sourceTypeTag == TypeTags.UNSIGNED8_INT_TAG ||
                        sourceTypeTag == TypeTags.UNSIGNED16_INT_TAG || sourceTypeTag == TypeTags.UNSIGNED32_INT_TAG;
            case TypeTags.ANY_TAG:
                return checkIsAnyType(sourceType);
            case TypeTags.ANYDATA_TAG:
                return sourceType.isAnydata();
            case TypeTags.SERVICE_TAG:
                return checkIsServiceType(sourceType, targetType,
                        unresolvedTypes == null ? new ArrayList<>() : unresolvedTypes);
            case TypeTags.HANDLE_TAG:
                return sourceTypeTag == TypeTags.HANDLE_TAG;
            case TypeTags.READONLY_TAG:
                return checkIsType(sourceType, PredefinedTypes.ANY_AND_READONLY_OR_ERROR_TYPE, unresolvedTypes);
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
                return targetTypeTag == sourceTypeTag;
            case TypeTags.INTERSECTION_TAG:
                return checkIsType(sourceType, ((BIntersectionType) targetType).getEffectiveType(), unresolvedTypes);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return checkIsType(sourceType, ((BTypeReferenceType) targetType).getReferredType(), unresolvedTypes);
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

        if (targetTypeTag == TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            targetType = getReferredType(targetType);
            targetTypeTag = targetType.getTag();
        }

        if (targetTypeTag == TypeTags.INTERSECTION_TAG) {
            targetType = ((BIntersectionType) targetType).getEffectiveType();
            targetTypeTag = targetType.getTag();
        }

        if (sourceType == targetType || (sourceType.getTag() == targetType.getTag() && sourceType.equals(targetType))) {
            return true;
        }

        if (targetType.isReadOnly() && !sourceType.isReadOnly()) {
            return false;
        }

        switch (targetTypeTag) {
            case TypeTags.ANY_TAG:
                return checkIsAnyType(sourceType);
            case TypeTags.READONLY_TAG:
                return isInherentlyImmutableType(sourceType) || sourceType.isReadOnly();
            default:
                return checkIsRecursiveTypeOnValue(sourceVal, sourceType, targetType, sourceTypeTag, targetTypeTag,
                                                   unresolvedTypes == null ? new ArrayList<>() : unresolvedTypes);
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
                return checkIsFiniteType(sourceType, (BFiniteType) targetType);
            case TypeTags.FUTURE_TAG:
                return checkIsFutureType(sourceType, (BFutureType) targetType, unresolvedTypes);
            case TypeTags.ERROR_TAG:
                return checkIsErrorType(sourceType, (BErrorType) targetType, unresolvedTypes);
            case TypeTags.TYPEDESC_TAG:
                return checkTypeDescType(sourceType, (BTypedescType) targetType, unresolvedTypes);
            case TypeTags.XML_TAG:
                return checkIsXMLType(sourceType, targetType, unresolvedTypes);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return checkIsRecursiveType(sourceType, ((ReferenceType) targetType).getReferredType(),
                        unresolvedTypes);
            default:
                // other non-recursive types shouldn't reach here
                return false;
        }
    }

    private static boolean checkIsRecursiveTypeOnValue(Object sourceVal, Type sourceType, Type targetType,
                                                       int sourceTypeTag, int targetTypeTag,
                                                       List<TypePair> unresolvedTypes) {
        switch (targetTypeTag) {
            case TypeTags.ANYDATA_TAG:
                if (sourceTypeTag == TypeTags.OBJECT_TYPE_TAG) {
                    return false;
                }
                return checkRecordBelongsToAnydataType((MapValue) sourceVal, (BRecordType) sourceType, unresolvedTypes);
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
        // If we encounter two types that we are still resolving, then skip it.
        // This is done to avoid recursive checking of the same type.
        TypePair pair = new TypePair(sourceType, targetType);
        if (unresolvedTypes.contains(pair)) {
            return true;
        }
        unresolvedTypes.add(pair);

        switch (sourceType.getTag()) {
            case TypeTags.UNION_TAG:
            case TypeTags.JSON_TAG:
            case TypeTags.ANYDATA_TAG:
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
            if (!SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.READONLY)) {
                if (!checkIsType(field.getFieldType(), targetConstrainedType, unresolvedTypes)) {
                    return false;
                }
                continue;
            }

            BString name = StringUtils.fromString(field.getFieldName());

            if (SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.OPTIONAL) && !sourceVal.containsKey(name)) {
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
        int sourceTag = sourceType.getTag();
        if (sourceTag == TypeTags.FINITE_TYPE_TAG) {
            return isFiniteTypeMatch((BFiniteType) sourceType, targetType);
        }

        BXmlType target = ((BXmlType) targetType);
        if (sourceTag == TypeTags.XML_TAG) {
            Type targetConstraint = getRecursiveTargetConstraintType(target);
            BXmlType source = (BXmlType) sourceType;
            if (source.constraint.getTag() == TypeTags.NEVER_TAG) {
                if (targetConstraint.getTag() == TypeTags.UNION_TAG) {
                    return checkIsUnionType(sourceType, (BUnionType) targetConstraint, unresolvedTypes);
                }
                return targetConstraint.getTag() == TypeTags.XML_TEXT_TAG ||
                        targetConstraint.getTag() == TypeTags.NEVER_TAG;
            }
            return checkIsType(source.constraint, targetConstraint, unresolvedTypes);
        }
        if (TypeTags.isXMLTypeTag(sourceTag)) {
            return checkIsType(sourceType, target.constraint, unresolvedTypes);
        }
        return false;
    }

    private static Type getRecursiveTargetConstraintType(BXmlType target) {
        Type targetConstraint = TypeUtils.getReferredType(target.constraint);
        // TODO: Revisit and check why xml<xml<constraint>>> on chained iteration
        while (targetConstraint.getTag() == TypeTags.XML_TAG) {
            target = (BXmlType) targetConstraint;
            targetConstraint = TypeUtils.getReferredType(target.constraint);
        }
        return targetConstraint;
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
                unresolvedTypes)
                && checkConstraints(((BStreamType) sourceType).getCompletionType(), targetType.getCompletionType(),
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

        if (targetType.getKeyType() == null && targetType.getFieldNames().length == 0) {
            return true;
        }

        if (targetType.getKeyType() != null) {
            if (srcTableType.getKeyType() != null &&
                    (checkConstraints(srcTableType.getKeyType(), targetType.getKeyType(), unresolvedTypes))) {
                return true;
            }

            if (srcTableType.getFieldNames().length == 0) {
                return false;
            }

            List<Type> fieldTypes = new ArrayList<>();
            Arrays.stream(srcTableType.getFieldNames()).forEach(field -> fieldTypes
                    .add(Objects.requireNonNull(getTableConstraintField(srcTableType.getConstrainedType(), field))
                    .getFieldType()));

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
            case TypeTags.INTERSECTION_TAG:
                Type effectiveType = ((BIntersectionType) constraintType).getEffectiveType();
                return getTableConstraintField(effectiveType, fieldName);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                Type referredType = ((BTypeReferenceType) constraintType).getReferredType();
                return getTableConstraintField(referredType, fieldName);
            case TypeTags.UNION_TAG:
                BUnionType unionType = (BUnionType) constraintType;
                List<Type> memTypes = unionType.getMemberTypes();
                List<BField> fields = memTypes.stream().map(type -> getTableConstraintField(type, fieldName))
                        .filter(Objects::nonNull).collect(Collectors.toList());

                if (fields.size() != memTypes.size()) {
                    return null;
                }

                if (fields.stream().allMatch(field -> isSameType(field.getFieldType(), fields.get(0).getFieldType()))) {
                    return fields.get(0);
                }
        }

        return null;
    }

    private static boolean checkIsJSONType(Type sourceType, List<TypePair> unresolvedTypes) {
        BJsonType jsonType = (BJsonType) TYPE_JSON;

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
            case TypeTags.TUPLE_TAG:
                BTupleType sourceTupleType = (BTupleType) sourceType;
                for (Type memberType : sourceTupleType.getTupleTypes()) {
                    if (!checkIsJSONType(memberType, unresolvedTypes)) {
                        return false;
                    }
                }
                Type tupleRestType = sourceTupleType.getRestType();
                if (tupleRestType != null) {
                    return checkIsJSONType(tupleRestType, unresolvedTypes);
                }
                return true;
            case TypeTags.UNION_TAG:
                for (Type memberType : ((BUnionType) sourceType).getMemberTypes()) {
                    if (!checkIsJSONType(memberType, unresolvedTypes)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return checkIsJSONType(((ReferenceType) sourceType).getReferredType(), unresolvedTypes);
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

        // Unsealed records are not equivalent to sealed records, unless their rest field type is 'never'. But
        // vice-versa is allowed.
        if (targetType.sealed && !sourceRecordType.sealed && (sourceRecordType.restFieldType == null ||
                sourceRecordType.restFieldType.getTag() != TypeTags.NEVER_TAG)) {
            return false;
        }

        // If both are sealed check the rest field type
        if (!sourceRecordType.sealed && !targetType.sealed &&
                !checkIsType(sourceRecordType.restFieldType, targetType.restFieldType, unresolvedTypes)) {
            return false;
        }

        Map<String, Field> sourceFields = sourceRecordType.getFields();
        Set<String> targetFieldNames = targetType.getFields().keySet();

        for (Map.Entry<String, Field> targetFieldEntry : targetType.getFields().entrySet()) {
            Field targetField = targetFieldEntry.getValue();
            Field sourceField = sourceFields.get(targetFieldEntry.getKey());

            if (sourceField == null) {
                if (!SymbolFlags.isFlagOn(targetField.getFlags(), SymbolFlags.OPTIONAL)) {
                    return false;
                }
                continue;
            }

            if (hasIncompatibleReadOnlyFlags(targetField, sourceField)) {
                return false;
            }

            // If the target field is required, the source field should be required as well.
            if (!SymbolFlags.isFlagOn(targetField.getFlags(), SymbolFlags.OPTIONAL)
                    && SymbolFlags.isFlagOn(sourceField.getFlags(), SymbolFlags.OPTIONAL)) {
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
        for (Map.Entry<String, Field> sourceFieldEntry : sourceFields.entrySet()) {
            if (targetFieldNames.contains(sourceFieldEntry.getKey())) {
                continue;
            }

            if (!checkIsType(sourceFieldEntry.getValue().getFieldType(), targetType.restFieldType, unresolvedTypes)) {
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
            var flags = field.getFlags();
            if (!SymbolFlags.isFlagOn(flags, SymbolFlags.OPTIONAL)) {
                return false;
            }

            if (SymbolFlags.isFlagOn(flags, SymbolFlags.READONLY) && !sourceType.isReadOnly()) {
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

        for (Map.Entry<String, Field> fieldEntry : fields.entrySet()) {
            String fieldName = fieldEntry.getKey();
            Field field = fieldEntry.getValue();

            if (SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.READONLY)) {
                BString fieldNameBString = StringUtils.fromString(fieldName);

                if (SymbolFlags
                        .isFlagOn(field.getFlags(), SymbolFlags.OPTIONAL) && !sourceVal.containsKey(fieldNameBString)) {
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

        // Unsealed records are not equivalent to sealed records, unless their rest field type is 'never'. But
        // vice-versa is allowed.
        if (targetType.sealed && !sourceRecordType.sealed && (sourceRecordType.restFieldType == null ||
                sourceRecordType.restFieldType.getTag() != TypeTags.NEVER_TAG)) {
            return false;
        }

        // If both are sealed check the rest field type
        if (!sourceRecordType.sealed && !targetType.sealed &&
                !checkIsType(sourceRecordType.restFieldType, targetType.restFieldType, unresolvedTypes)) {
            return false;
        }

        Map<String, Field> sourceFields = sourceRecordType.getFields();
        Set<String> targetFieldNames = targetType.getFields().keySet();

        for (Map.Entry<String, Field> targetFieldEntry : targetType.getFields().entrySet()) {
            String fieldName = targetFieldEntry.getKey();
            Field targetField = targetFieldEntry.getValue();
            Field sourceField = sourceFields.get(fieldName);

            if (targetField.getFieldType().getTag() == TypeTags.NEVER_TAG && containsInvalidNeverField(sourceField,
                    sourceRecordType)) {
                return false;
            }

            if (sourceField == null) {
                if (!SymbolFlags.isFlagOn(targetField.getFlags(), SymbolFlags.OPTIONAL)) {
                    return false;
                }
                continue;
            }

            if (hasIncompatibleReadOnlyFlags(targetField, sourceField)) {
                return false;
            }

            boolean optionalTargetField = SymbolFlags.isFlagOn(targetField.getFlags(), SymbolFlags.OPTIONAL);
            boolean optionalSourceField = SymbolFlags.isFlagOn(sourceField.getFlags(), SymbolFlags.OPTIONAL);

            if (SymbolFlags.isFlagOn(sourceField.getFlags(), SymbolFlags.READONLY)) {
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
            for (String sourceFieldName : sourceFields.keySet()) {
                if (targetFieldNames.contains(sourceFieldName)) {
                    continue;
                }

                if (!checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(
                        sourceFields.get(sourceFieldName).getFieldType())) {
                    return false;
                }
            }
            return true;
        }

        for (Map.Entry<String, Field> targetFieldEntry : sourceFields.entrySet()) {
            String fieldName = targetFieldEntry.getKey();
            Field field = targetFieldEntry.getValue();
            if (targetFieldNames.contains(fieldName)) {
                continue;
            }

            if (SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.READONLY)) {
                if (!checkIsLikeType(sourceRecordValue.get(StringUtils.fromString(fieldName)),
                                     targetType.restFieldType)) {
                    return false;
                }
            } else if (!checkIsType(field.getFieldType(), targetType.restFieldType, unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsInvalidNeverField(Field sourceField, BRecordType sourceRecordType) {
        if (sourceField != null) {
            return !containsNeverType(sourceField.getFieldType());
        }
        if (sourceRecordType.isSealed()) {
            return true;
        }
        return !containsNeverType(sourceRecordType.getRestFieldType());
    }

    private static boolean containsNeverType(Type fieldType) {
        int fieldTag = fieldType.getTag();
        if (fieldTag == TypeTags.NEVER_TAG) {
            return true;
        }
        if (fieldTag == TypeTags.UNION_TAG) {
            List<Type> memberTypes = ((BUnionType) fieldType).getOriginalMemberTypes();
            for (Type member : memberTypes) {
                if (member.getTag() == TypeTags.NEVER_TAG) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasIncompatibleReadOnlyFlags(Field targetField, Field sourceField) {
        return SymbolFlags.isFlagOn(targetField.getFlags(), SymbolFlags.READONLY) && !SymbolFlags
                .isFlagOn(sourceField.getFlags(),
                          SymbolFlags.READONLY);
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
            case TypeTags.READONLY_TAG:
                return false;
            case TypeTags.UNION_TAG:
            case TypeTags.ANYDATA_TAG:
            case TypeTags.JSON_TAG:
                for (Type memberType : ((BUnionType) sourceType).getMemberTypes()) {
                    if (!checkIsAnyType(memberType)) {
                        return false;
                    }
                }
                return true;
        }
        return true;
    }

    private static boolean checkIsFiniteType(Type sourceType, BFiniteType targetType) {
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
        if (sourceType.getTag() != TypeTags.OBJECT_TYPE_TAG && sourceType.getTag() != TypeTags.SERVICE_TAG) {
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

        if (SymbolFlags.isFlagOn(targetType.flags, SymbolFlags.ISOLATED) &&
                !SymbolFlags.isFlagOn(sourceObjectType.flags, SymbolFlags.ISOLATED)) {
            return false;
        }

        Map<String, Field> targetFields = targetType.getFields();
        Map<String, Field> sourceFields = sourceObjectType.getFields();
        List<MethodType> targetFuncs = getAllFunctionsList(targetType);
        List<MethodType> sourceFuncs = getAllFunctionsList(sourceObjectType);

        if (targetType.getFields().values().stream().anyMatch(field -> SymbolFlags
                .isFlagOn(field.getFlags(), SymbolFlags.PRIVATE))
                || targetFuncs.stream().anyMatch(func -> SymbolFlags.isFlagOn(func.getFlags(),
                                                                                SymbolFlags.PRIVATE))) {
            return false;
        }

        if (targetFields.size() > sourceFields.size() || targetFuncs.size() > sourceFuncs.size()) {
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
    
    private static List<MethodType> getAllFunctionsList(BObjectType objectType) {
        List<MethodType> functionList = new ArrayList<>(Arrays.asList(objectType.getMethods()));
        if (objectType.getTag() == TypeTags.SERVICE_TAG ||
                (objectType.flags & SymbolFlags.CLIENT) == SymbolFlags.CLIENT) {
            Collections.addAll(functionList, ((BNetworkObjectType) objectType).getResourceMethods());
        }
        
        return functionList;
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

            if (SymbolFlags.isFlagOn(rhsField.getFlags(), SymbolFlags.FINAL)) {
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
                                                        List<MethodType> targetFuncs,
                                                        List<MethodType> sourceFuncs,
                                                        String targetTypeModule, String sourceTypeModule,
                                                        BObjectType sourceType, BObjectType targetType) {
        for (MethodType lhsFunc : targetFuncs) {
            Optional<MethodType> rhsFunction = getMatchingInvokableType(sourceFuncs, lhsFunc, unresolvedTypes);
            if (rhsFunction.isEmpty()) {
                return false;
            }

            MethodType rhsFunc = rhsFunction.get();
            if (rhsFunc == null ||
                    !isInSameVisibilityRegion(targetTypeModule, sourceTypeModule, lhsFunc.getFlags(),
                                              rhsFunc.getFlags())) {
                return false;
            }
            if (SymbolFlags.isFlagOn(lhsFunc.getFlags(), SymbolFlags.REMOTE) != SymbolFlags
                    .isFlagOn(rhsFunc.getFlags(), SymbolFlags.REMOTE)) {
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

    private static boolean isInSameVisibilityRegion(String lhsTypePkg, String rhsTypePkg, long lhsFlags,
                                                    long rhsFlags) {
        if (SymbolFlags.isFlagOn(lhsFlags, SymbolFlags.PRIVATE)) {
            return lhsTypePkg.equals(rhsTypePkg);
        } else if (SymbolFlags.isFlagOn(lhsFlags, SymbolFlags.PUBLIC)) {
            return SymbolFlags.isFlagOn(rhsFlags, SymbolFlags.PUBLIC);
        }
        return !SymbolFlags.isFlagOn(rhsFlags, SymbolFlags.PRIVATE) && !SymbolFlags
                .isFlagOn(rhsFlags, SymbolFlags.PUBLIC) &&
                lhsTypePkg.equals(rhsTypePkg);
    }

    private static Optional<MethodType> getMatchingInvokableType(List<MethodType> rhsFuncs,
                                                       MethodType lhsFunc,
                                                       List<TypePair> unresolvedTypes) {
        Optional<MethodType> matchingFunction = rhsFuncs.stream()
                .filter(rhsFunc -> lhsFunc.getName().equals(rhsFunc.getName()))
                .filter(rhsFunc -> checkFunctionTypeEqualityForObjectType(rhsFunc.getType(), lhsFunc.getType(),
                                                                          unresolvedTypes))
                .findFirst();

        if (matchingFunction.isEmpty()) {
            return matchingFunction;
        }
        // For resource function match, we need to check whether lhs function resource path type belongs to 
        // rhs function resource path type
        MethodType matchingFunc = matchingFunction.get();
        boolean lhsFuncIsResource = SymbolFlags.isFlagOn(lhsFunc.getFlags(), SymbolFlags.RESOURCE);
        boolean matchingFuncIsResource = SymbolFlags.isFlagOn(matchingFunc.getFlags(), SymbolFlags.RESOURCE);
        
        if (!lhsFuncIsResource && !matchingFuncIsResource) {
            return matchingFunction;
        }
        
        if ((lhsFuncIsResource && !matchingFuncIsResource) || (matchingFuncIsResource && !lhsFuncIsResource)) {
            return Optional.empty();
        }

        Type[] lhsFuncResourcePathTypes = ((BResourceMethodType) lhsFunc).pathSegmentTypes;
        Type[] rhsFuncResourcePathTypes = ((BResourceMethodType) matchingFunc).pathSegmentTypes;

        int lhsFuncResourcePathTypesSize = lhsFuncResourcePathTypes.length;
        if (lhsFuncResourcePathTypesSize != rhsFuncResourcePathTypes.length) {
            return Optional.empty();
        }

        for (int i = 0; i < lhsFuncResourcePathTypesSize; i++) {
            if (!checkIsType(lhsFuncResourcePathTypes[i], rhsFuncResourcePathTypes[i])) {
                return Optional.empty();
            }
        }

        return matchingFunction;
    }

    private static boolean checkFunctionTypeEqualityForObjectType(FunctionType source, FunctionType target,
                                                                  List<TypePair> unresolvedTypes) {
        if (hasIncompatibleIsolatedFlags(target, source)) {
            return false;
        }

        if (source.getParameters().length != target.getParameters().length) {
            return false;
        }

        for (int i = 0; i < source.getParameters().length; i++) {
            if (!checkIsType(target.getParameters()[i].type, source.getParameters()[i].type, unresolvedTypes)) {
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
        if (hasIncompatibleIsolatedFlags(targetType, source) || hasIncompatibleTransactionalFlags(targetType, source)) {
            return false;
        }

        if (SymbolFlags.isFlagOn(targetType.getFlags(), SymbolFlags.ANY_FUNCTION)) {
            return true;
        }

        if (source.parameters.length != targetType.parameters.length) {
            return false;
        }

        for (int i = 0; i < source.parameters.length; i++) {
            if (!checkIsType(targetType.parameters[i].type, source.parameters[i].type, new ArrayList<>())) {
                return false;
            }
        }

        return checkIsType(source.retType, targetType.retType, new ArrayList<>());
    }

    private static boolean hasIncompatibleIsolatedFlags(FunctionType target, FunctionType source) {
        return SymbolFlags.isFlagOn(target.getFlags(), SymbolFlags.ISOLATED) && !SymbolFlags
                .isFlagOn(source.getFlags(), SymbolFlags.ISOLATED);
    }

    private static boolean hasIncompatibleTransactionalFlags(FunctionType target, FunctionType source) {
        return SymbolFlags.isFlagOn(source.getFlags(), SymbolFlags.TRANSACTIONAL) && !SymbolFlags
                .isFlagOn(target.getFlags(), SymbolFlags.TRANSACTIONAL);
    }

    private static boolean checkIsServiceType(Type sourceType, Type targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() == TypeTags.SERVICE_TAG) {
            return checkObjectEquivalency(sourceType, (BObjectType) targetType, unresolvedTypes);
        }

        if (sourceType.getTag() == TypeTags.OBJECT_TYPE_TAG) {
            var flags = ((BObjectType) sourceType).flags;
            return (flags & SymbolFlags.SERVICE) == SymbolFlags.SERVICE;
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
            case TypeTags.NEVER_TAG:
            case TypeTags.ERROR_TAG:
            case TypeTags.INVOKABLE_TAG:
            case TypeTags.SERVICE_TAG:
            case TypeTags.TYPEDESC_TAG:
            case TypeTags.FUNCTION_POINTER_TAG:
            case TypeTags.HANDLE_TAG:
                return true;
            case TypeTags.XML_TAG:
                return ((BXmlType) sourceType).constraint.getTag() == TypeTags.NEVER_TAG;
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return isInherentlyImmutableType(((BTypeReferenceType) sourceType).getReferredType());
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

                if (SymbolFlags.isFlagOn(objectType.flags, SymbolFlags.CLASS) &&
                        !SymbolFlags.isFlagOn(objectType.flags, SymbolFlags.READONLY)) {
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
                        break;
                    }
                }
                return readonlyIntersectionExists;
            case TypeTags.INTERSECTION_TAG:
                return isSelectivelyImmutableType(((BIntersectionType) type).getEffectiveType(), unresolvedTypes);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return isSelectivelyImmutableType(((BTypeReferenceType) type).getReferredType(), unresolvedTypes);
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
        if (value == null || sourceType.getTag() < TypeTags.NULL_TAG ||
                sourceType.getTag() == TypeTags.FINITE_TYPE_TAG) {
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

    private static boolean checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(Type type) {
        Set<String> visitedTypeSet = new HashSet<>();
        return checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(type, visitedTypeSet);
    }

    private static boolean checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(Type type,
                                                                                   Set<String> visitedTypeSet) {
        switch (type.getTag()) {
            case TypeTags.NEVER_TAG:
                return true;
            case TypeTags.RECORD_TYPE_TAG:
                BRecordType recordType = (BRecordType) type;
                visitedTypeSet.add(recordType.getName());
                for (Field field : recordType.getFields().values()) {
                    // skip check for fields with self referencing type and not required fields.
                    if ((SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.REQUIRED) ||
                            !SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.OPTIONAL)) &&
                            !visitedTypeSet.contains(field.getFieldType()) &&
                            checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(field.getFieldType(),
                                    visitedTypeSet)) {
                        return true;
                    }
                }
                return false;
            case TypeTags.TUPLE_TAG:
                BTupleType tupleType = (BTupleType) type;
                visitedTypeSet.add(tupleType.getName());
                List<Type> tupleTypes = tupleType.getTupleTypes();
                for (Type mem : tupleTypes) {
                    if (!visitedTypeSet.add(mem.getName())) {
                        continue;
                    }
                    if (checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(mem, visitedTypeSet)) {
                        return true;
                    }
                }
                return false;
            case TypeTags.ARRAY_TAG:
                BArrayType arrayType = (BArrayType) type;
                visitedTypeSet.add(arrayType.getName());
                Type elemType = arrayType.getElementType();
                visitedTypeSet.add(elemType.getName());
                return arrayType.getState() != ArrayState.OPEN &&
                        checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(elemType, visitedTypeSet);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(
                        ((BTypeReferenceType) type).getReferredType(), visitedTypeSet);
            case TypeTags.INTERSECTION_TAG:
                return checkIsNeverTypeOrStructureTypeWithARequiredNeverMember(
                        ((BIntersectionType) type).getEffectiveType(), visitedTypeSet);
            default:
                return false;
        }
    }

    /**
     * Check whether a given value confirms to a given type. First it checks if the type of the value, and
     * if fails then falls back to checking the value.
     *
     * @param errors list to collect typecast errors
     * @param sourceValue Value to check
     * @param targetType Target type
     * @param unresolvedValues Values that are unresolved so far
     * @param allowNumericConversion Flag indicating whether to perform numeric conversions
     * @param varName variable name to identify the parent of a record field
     * @return True if the value confirms to the provided type. False, otherwise.
     */
    private static boolean checkIsLikeType(List<String> errors, Object sourceValue, Type targetType,
                                           List<TypeValuePair> unresolvedValues,
                                           boolean allowNumericConversion, String varName) {
        Type sourceType = getType(sourceValue);
        if (checkIsType(sourceType, targetType, new ArrayList<>())) {
            return true;
        }

        return checkIsLikeOnValue(errors, sourceValue, sourceType, targetType, unresolvedValues, allowNumericConversion,
                varName);
    }

    /**
     * Check whether a given value confirms to a given type. Strictly checks the value only, and does not consider the
     * type of the value for consideration.
     *
     * @param errors list to collect typecast errors
     * @param sourceValue Value to check
     * @param sourceType Type of the value
     * @param targetType Target type
     * @param unresolvedValues Values that are unresolved so far
     * @param allowNumericConversion Flag indicating whether to perform numeric conversions
     * @param varName variable name to identify the parent of a record field
     * @return True if the value confirms to the provided type. False, otherwise.
     */
    private static boolean checkIsLikeOnValue(List<String> errors, Object sourceValue, Type sourceType, Type targetType,
                                              List<TypeValuePair> unresolvedValues, boolean allowNumericConversion,
                                              String varName) {
        int sourceTypeTag = sourceType.getTag();
        int targetTypeTag = targetType.getTag();

        switch (sourceTypeTag) {
            case TypeTags.INTERSECTION_TAG:
                return checkIsLikeOnValue(errors, sourceValue, ((BIntersectionType) sourceType).getEffectiveType(),
                        targetTypeTag != TypeTags.INTERSECTION_TAG ? targetType :
                                ((BIntersectionType) targetType).getEffectiveType(),
                        unresolvedValues, allowNumericConversion, varName);
            case TypeTags.PARAMETERIZED_TYPE_TAG:
                if (targetTypeTag != TypeTags.PARAMETERIZED_TYPE_TAG) {
                    return checkIsLikeOnValue(errors, sourceValue,
                            ((BParameterizedType) sourceType).getParamValueType(), targetType, unresolvedValues,
                            allowNumericConversion, varName);
                }
                return checkIsLikeOnValue(errors, sourceValue, ((BParameterizedType) sourceType).getParamValueType(),
                        ((BParameterizedType) targetType).getParamValueType(), unresolvedValues,
                        allowNumericConversion, varName);
            default:
                break;
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
                        allowNumericConversion, varName, errors);
            case TypeTags.TABLE_TAG:
                return checkIsLikeTableType(sourceValue, (BTableType) targetType, unresolvedValues,
                        allowNumericConversion);
            case TypeTags.JSON_TAG:
                return checkIsLikeJSONType(sourceValue, sourceType, (BJsonType) targetType, unresolvedValues,
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
                return checkFiniteTypeAssignable(sourceValue, sourceType, (BFiniteType) targetType,
                 unresolvedValues, allowNumericConversion);
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TEXT_TAG:
                if (sourceTypeTag == TypeTags.XML_TAG) {
                    return checkIsLikeXmlValueSingleton((XmlValue) sourceValue, targetType);
                }
                return false;
            case TypeTags.XML_TAG:
                if (sourceTypeTag == TypeTags.XML_TAG) {
                    return checkIsLikeXMLSequenceType((XmlValue) sourceValue, targetType);
                }
                return false;
            case TypeTags.UNION_TAG:
                if (allowNumericConversion) {
                    List<Type> compatibleTypesWithNumConversion = new ArrayList<>();
                    List<Type> compatibleTypesWithoutNumConversion = new ArrayList<>();
                    for (Type type : ((BUnionType) targetType).getMemberTypes()) {
                        List<TypeValuePair> tempList = new ArrayList<>(unresolvedValues.size());
                        tempList.addAll(unresolvedValues);

                        if (checkIsLikeType(errors, sourceValue, type, tempList, false, varName)) {
                            compatibleTypesWithoutNumConversion.add(type);
                        }

                        if (checkIsLikeType(errors, sourceValue, type, unresolvedValues, true, varName)) {
                            compatibleTypesWithNumConversion.add(type);
                        }
                    }
                    // Conversion should only be possible to one other numeric type.
                    return compatibleTypesWithNumConversion.size() != 0 &&
                            compatibleTypesWithNumConversion.size() - compatibleTypesWithoutNumConversion.size() <= 1;
                } else {
                    for (Type type : ((BUnionType) targetType).getMemberTypes()) {
                        if (checkIsLikeType(errors, sourceValue, type, unresolvedValues, false, varName)) {
                            return true;
                        }
                    }
                }
                return false;
            case TypeTags.INTERSECTION_TAG:
                return checkIsLikeOnValue(errors, sourceValue, sourceType,
                        ((BIntersectionType) targetType).getEffectiveType(), unresolvedValues, allowNumericConversion,
                        varName);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return checkIsLikeOnValue(errors, sourceValue, sourceType,
                        ((BTypeReferenceType) targetType).getReferredType(), unresolvedValues, allowNumericConversion,
                        varName);
            default:
                return false;
        }
    }

    private static XmlNodeType getXmlNodeType(Type type) {
        XmlNodeType nodeType = null;
        switch (type.getTag()) {
            case TypeTags.XML_ELEMENT_TAG:
                nodeType = XmlNodeType.ELEMENT;
                break;
            case TypeTags.XML_COMMENT_TAG:
                nodeType = XmlNodeType.COMMENT;
                break;
            case TypeTags.XML_PI_TAG:
                nodeType = XmlNodeType.PI;
                break;
            case TypeTags.XML_TEXT_TAG:
                nodeType = XmlNodeType.TEXT;
                break;
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                nodeType = getXmlNodeType(((ReferenceType) type).getReferredType());
                break;
            default:
                return null;
        }
        return nodeType;
    }

    private static boolean checkIsLikeXmlValueSingleton(XmlValue xmlSource, Type targetType) {

        XmlNodeType nodeType = getXmlNodeType(targetType);

        if (nodeType == null) {
            return false;
        }

        if (xmlSource.getNodeType() == nodeType) {
            return true;
        }

        if (xmlSource.getNodeType() == XmlNodeType.SEQUENCE) {
            XmlSequence seq = (XmlSequence) xmlSource;

            return seq.size() == 1 && seq.getChildrenList().get(0).getNodeType() == nodeType ||
                    (nodeType == XmlNodeType.TEXT && seq.isEmpty());
        }
        return false;
    }

    private static boolean checkIsLikeXMLSequenceType(XmlValue xmlSource, Type targetType) {
        if (xmlSource.getNodeType() != XmlNodeType.SEQUENCE) {
            return false;
        }
        Set<XmlNodeType> acceptedNodes = new HashSet<>();

        BXmlType target = (BXmlType) targetType;
        if (TypeUtils.getReferredType(target.constraint).getTag() == TypeTags.UNION_TAG) {
            getXMLNodeOnUnion((BUnionType) target.constraint, acceptedNodes);
        } else {
            acceptedNodes.add(getXmlNodeType(((BXmlType) targetType).constraint));
        }

        XmlSequence seq = (XmlSequence) xmlSource;
        for (BXml m : seq.getChildrenList()) {
            if (!acceptedNodes.contains(m.getNodeType())) {
                return false;
            }
        }
        return true;
    }

    private static void getXMLNodeOnUnion(BUnionType unionType, Set<XmlNodeType> nodeTypes) {
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
        return type.getTag() < TypeTags.STRING_TAG || TypeTags.isIntegerTypeTag(type.getTag());
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
            case TypeTags.TABLE_TAG:
                return isLikeType(((TableValueImpl) sourceValue).values().toArray(), TYPE_ANYDATA,
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
                return checkIsLikeType(null, sourceValue, TYPE_ANYDATA, unresolvedValues,
                        allowNumericConversion, null);
            default:
                return false;
        }
    }

    private static boolean isLikeType(Object[] objects, Type targetType, List<TypeValuePair> unresolvedValues,
                                      boolean allowNumericConversion) {
        for (Object value : objects) {
            if (!checkIsLikeType(null, value, targetType, unresolvedValues, allowNumericConversion,
                    null)) {
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
            if (!checkIsLikeType(null, source.getRefValue(i), targetTypes.get(i), unresolvedValues,
                    allowNumericConversion, null)) {
                return false;
            }
        }
        for (int i = targetTypeSize; i < sourceTypeSize; i++) {
            if (!checkIsLikeType(null, source.getRefValue(i), targetRestType, unresolvedValues,
                    allowNumericConversion, null)) {
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

                if (checkIsType(sourceElementType, targetTypeElementType, new ArrayList<>())) {
                    return true;
                }

                if (allowNumericConversion && isNumericType(sourceElementType)) {
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

                if (targetTypeElementType.getTag() == TypeTags.FLOAT_TAG ||
                        targetTypeElementType.getTag() == TypeTags.DECIMAL_TAG) {
                    return false;
                }
            }
        }

        int sourceSize = source.size();
        if ((targetType.getState() != ArrayState.OPEN) && (sourceSize != targetType.getSize())) {
            return false;
        }
        for (int i = 0; i < sourceSize; i++) {
            if (!checkIsLikeType(null, source.get(i), targetTypeElementType, unresolvedValues,
                    allowNumericConversion, null)) {
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
            if (!checkIsLikeType(null, mapEntry, targetType.getConstrainedType(), unresolvedValues,
                    allowNumericConversion, null)) {
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

    private static boolean checkIsLikeJSONType(Object sourceValue, Type sourceType, BJsonType targetType,
                                               List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        if (sourceType.getTag() == TypeTags.ARRAY_TAG) {
            ArrayValue source = (ArrayValue) sourceValue;
            Type elementType = ((BArrayType) source.getType()).getElementType();
            if (isValueType(elementType)) {
                return checkIsType(elementType, targetType, new ArrayList<>());
            }

            Object[] arrayValues = source.getValues();
            for (int i = 0; i < ((ArrayValue) sourceValue).size(); i++) {
                if (!checkIsLikeType(null, arrayValues[i], targetType, unresolvedValues,
                        allowNumericConversion, null)) {
                    return false;
                }
            }
            return true;
        } else if (sourceType.getTag() == TypeTags.MAP_TAG) {
            for (Object value : ((MapValueImpl) sourceValue).values()) {
                if (!checkIsLikeType(null, value, targetType, unresolvedValues, allowNumericConversion,
                        null)) {
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
                if (!checkIsLikeType(null, object, targetType, unresolvedValues, allowNumericConversion,
                        null)) {
                    return false;
                }
            }
            return true;
        } else if (sourceType.getTag() == TypeTags.TUPLE_TAG) {
            for (Object obj : ((TupleValueImpl) sourceValue).getValues()) {
                if (!checkIsLikeType(null, obj, targetType, unresolvedValues, allowNumericConversion,
                        null)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean checkIsLikeRecordType(Object sourceValue, BRecordType targetType,
                                                 List<TypeValuePair> unresolvedValues, boolean allowNumericConversion,
                                                 String varName, List<String> errors) {
        if (!(sourceValue instanceof MapValueImpl)) {
            return false;
        }

        TypeValuePair typeValuePair = new TypeValuePair(sourceValue, targetType);
        if (unresolvedValues.contains(typeValuePair)) {
            return true;
        }
        unresolvedValues.add(typeValuePair);

        Map<String, Type> targetFieldTypes = new HashMap<>();
        Type restFieldType = targetType.restFieldType;
        boolean returnVal = true;

        for (Field field : targetType.getFields().values()) {
            targetFieldTypes.put(field.getFieldName(), field.getFieldType());
        }

        for (Map.Entry targetTypeEntry : targetFieldTypes.entrySet()) {
            String fieldName = targetTypeEntry.getKey().toString();
            String fieldNameLong = TypeConverter.getLongFieldName(varName, fieldName);
            Field targetField = targetType.getFields().get(fieldName);

            if (!(((MapValueImpl) sourceValue).containsKey(StringUtils.fromString(fieldName))) &&
                    !SymbolFlags.isFlagOn(targetField.getFlags(), SymbolFlags.OPTIONAL)) {
                addErrorMessage((errors == null) ? 0 : errors.size(), "missing required field '" + fieldNameLong +
                        "' of type '" + targetField.getFieldType().toString() + "' in record '" + targetType + "'",
                        errors);
                if ((errors == null) || (errors.size() >= MAX_TYPECAST_ERROR_COUNT + 1)) {
                    return false;
                }
                returnVal = false;
            }
        }

        for (Object object : ((MapValueImpl) sourceValue).entrySet()) {
            Map.Entry valueEntry = (Map.Entry) object;
            String fieldName = valueEntry.getKey().toString();
            String fieldNameLong = TypeConverter.getLongFieldName(varName, fieldName);
            int initialErrorCount = (errors == null) ? 0 : errors.size();

            if (targetFieldTypes.containsKey(fieldName)) {
                if (!checkIsLikeType(errors, (valueEntry.getValue()), targetFieldTypes.get(fieldName),
                                     unresolvedValues, allowNumericConversion, fieldNameLong)) {
                    addErrorMessage(initialErrorCount, "field '" + fieldNameLong + "' in record '" + targetType +
                            "' should be of type '" + targetFieldTypes.get(fieldName) + "', found '" +
                            TypeConverter.getShortSourceValue(valueEntry.getValue()) + "'", errors);
                    returnVal = false;
                }
            } else {
                if (!targetType.sealed) {
                    if (!checkIsLikeType(errors, (valueEntry.getValue()), restFieldType, unresolvedValues,
                                         allowNumericConversion, fieldNameLong)) {
                        addErrorMessage(initialErrorCount, "value of field '" + valueEntry.getKey() +
                                "' adding to the record '" + targetType + "' should be of type '" + restFieldType +
                                "', found '" + TypeConverter.getShortSourceValue(valueEntry.getValue()) + "'", errors);
                        returnVal = false;
                    }
                } else {
                    addErrorMessage(initialErrorCount, "field '" + fieldNameLong +
                            "' cannot be added to the closed record '" + targetType + "'", errors);
                    returnVal = false;
                }
            }
            if ((!returnVal) && ((errors == null) || (errors.size() >= MAX_TYPECAST_ERROR_COUNT + 1))) {
                return false;
            }
        }
        return returnVal;
    }

    private static void addErrorMessage(int initialErrorCount, String errorMessage, List<String> errors) {
        if ((errors != null) && (errors.size() <= MAX_TYPECAST_ERROR_COUNT) &&
                ((errors.size() - initialErrorCount) == 0)) {
            errors.add(errorMessage);
        }
    }

    private static boolean checkIsLikeTableType(Object sourceValue, BTableType targetType,
                                                List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        if (!(sourceValue instanceof TableValueImpl)) {
            return false;
        }
        TableValueImpl tableValue = (TableValueImpl) sourceValue;
        BTableType sourceType = (BTableType) tableValue.getType();
        if (targetType.getKeyType() != null && sourceType.getFieldNames().length == 0) {
            return false;
        }

        if (sourceType.getKeyType() != null && !checkIsType(tableValue.getKeyType(), targetType.getKeyType())) {
            return false;
        }

        TypeValuePair typeValuePair = new TypeValuePair(sourceValue, targetType);
        if (unresolvedValues.contains(typeValuePair)) {
            return true;
        }

        Object[] objects = tableValue.values().toArray();
        for (Object object : objects) {
            if (!checkIsLikeType(object, targetType.getConstrainedType(), allowNumericConversion)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkFiniteTypeAssignable(Object sourceValue, Type sourceType, BFiniteType targetType,
                                                     List<TypeValuePair> unresolvedValues,
                                                     boolean allowNumericConversion) {
        if (targetType.valueSpace.size() == 1) {
            Type valueType = getType(targetType.valueSpace.iterator().next());
            if (!isSimpleBasicType(valueType) && valueType.getTag() != TypeTags.NULL_TAG) {
                return checkIsLikeOnValue(null, sourceValue, sourceType, valueType, unresolvedValues,
                        allowNumericConversion, null);
            }
        }

        for (Object valueSpaceItem : targetType.valueSpace) {
            // TODO: 8/13/19 Maryam fix for conversion
            if (isFiniteTypeValue(sourceValue, sourceType, valueSpaceItem, allowNumericConversion)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isFiniteTypeValue(Object sourceValue, Type sourceType, Object valueSpaceItem,
                                               boolean allowNumericConversion) {
        Type valueSpaceItemType = getType(valueSpaceItem);
        if (valueSpaceItemType.getTag() > TypeTags.DECIMAL_TAG) {
            return valueSpaceItemType.getTag() == sourceType.getTag() &&
                    (valueSpaceItem == sourceValue || valueSpaceItem.equals(sourceValue));
        }

        switch (sourceType.getTag()) {
            case TypeTags.BYTE_TAG:
            case TypeTags.INT_TAG:
                switch (valueSpaceItemType.getTag()) {
                    case TypeTags.BYTE_TAG:
                    case TypeTags.INT_TAG:
                        return ((Number) sourceValue).longValue() == ((Number) valueSpaceItem).longValue();
                    case TypeTags.FLOAT_TAG:
                        return ((Number) sourceValue).longValue() == ((Number) valueSpaceItem).longValue() &&
                                allowNumericConversion;
                    case TypeTags.DECIMAL_TAG:
                        return ((Number) sourceValue).longValue() == ((DecimalValue) valueSpaceItem).intValue() &&
                                allowNumericConversion;
                }
            case TypeTags.FLOAT_TAG:
                switch (valueSpaceItemType.getTag()) {
                    case TypeTags.BYTE_TAG:
                    case TypeTags.INT_TAG:
                        return ((Number) sourceValue).doubleValue() == ((Number) valueSpaceItem).doubleValue()
                                && allowNumericConversion;
                    case TypeTags.FLOAT_TAG:
                        return (((Number) sourceValue).doubleValue() == ((Number) valueSpaceItem).doubleValue() ||
                                (Double.isNaN((Double) sourceValue) && Double.isNaN((Double) valueSpaceItem)));
                    case TypeTags.DECIMAL_TAG:
                        return ((Number) sourceValue).doubleValue() == ((DecimalValue) valueSpaceItem).floatValue()
                                && allowNumericConversion;
                }
            case TypeTags.DECIMAL_TAG:
                switch (valueSpaceItemType.getTag()) {
                    case TypeTags.BYTE_TAG:
                    case TypeTags.INT_TAG:
                        return checkDecimalEqual((DecimalValue) sourceValue,
                                DecimalValue.valueOf(((Number) valueSpaceItem).longValue())) && allowNumericConversion;
                    case TypeTags.FLOAT_TAG:
                        return checkDecimalEqual((DecimalValue) sourceValue,
                            DecimalValue.valueOf(((Number) valueSpaceItem).doubleValue())) && allowNumericConversion;
                    case TypeTags.DECIMAL_TAG:
                        return checkDecimalEqual((DecimalValue) sourceValue, (DecimalValue) valueSpaceItem);
                }
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

        if (!checkIsType(bErrorType.detailType, targetType.detailType, unresolvedTypes)) {
            return false;
        }

        if (targetType.typeIdSet == null) {
            return true;
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

        if (!checkIsLikeType(null, ((ErrorValue) sourceValue).getDetails(), targetType.detailType,
                unresolvedValues, allowNumericConversion, null)) {
            return false;
        }

        if (targetType.typeIdSet == null) {
            return true;
        }

        BTypeIdSet sourceIdSet = ((BErrorType) sourceType).typeIdSet;
        if (sourceIdSet == null) {
            return false;
        }

        return sourceIdSet.containsAll(targetType.typeIdSet);
    }

    static boolean isSimpleBasicType(Type type) {
        return type.getTag() < TypeTags.NULL_TAG;
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

        return checkValueEquals(lhsValue, rhsValue, checkedValues, getType(lhsValue), getType(rhsValue));
    }

    private static boolean checkValueEquals(Object lhsValue, Object rhsValue, List<ValuePair> checkedValues,
                                            Type lhsValType, Type rhsValType) {
        int lhsValTypeTag = lhsValType.getTag();
        int rhsValTypeTag = rhsValType.getTag();
        if (rhsValTypeTag == TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            rhsValType = TypeUtils.getReferredType(rhsValType);
            rhsValTypeTag = rhsValType.getTag();
        }

        switch (lhsValTypeTag) {
            case TypeTags.STRING_TAG:
            case TypeTags.BOOLEAN_TAG:
                return lhsValue.equals(rhsValue);
            case TypeTags.INT_TAG:
                if (rhsValTypeTag != TypeTags.BYTE_TAG && rhsValTypeTag != TypeTags.INT_TAG) {
                    return false;
                }
                return lhsValue.equals(((Number) rhsValue).longValue());
            case TypeTags.BYTE_TAG:
                if (rhsValTypeTag != TypeTags.BYTE_TAG && rhsValTypeTag != TypeTags.INT_TAG) {
                    return false;
                }
                return ((Number) lhsValue).byteValue() == ((Number) rhsValue).byteValue();
            case TypeTags.FLOAT_TAG:
                if (rhsValTypeTag != TypeTags.FLOAT_TAG) {
                    return false;
                }
                if (Double.isNaN((Double) lhsValue) && Double.isNaN((Double) rhsValue)) {
                    return true;
                }
                return ((Number) lhsValue).doubleValue() == ((Number) rhsValue).doubleValue();
            case TypeTags.DECIMAL_TAG:
                if (rhsValTypeTag != TypeTags.DECIMAL_TAG) {
                    return false;
                }
                return checkDecimalEqual((DecimalValue) lhsValue, (DecimalValue) rhsValue);
            case TypeTags.XML_TAG:
                // Instance of xml never
                if (lhsValue instanceof XmlText) {
                    return TypeTags.isXMLTypeTag(rhsValTypeTag) && isEqual((XmlText) lhsValue, (XmlValue) rhsValue);
                }
                return TypeTags.isXMLTypeTag(rhsValTypeTag) && isEqual((XmlSequence) lhsValue, (XmlValue) rhsValue);
            case TypeTags.XML_ELEMENT_TAG:
                return TypeTags.isXMLTypeTag(rhsValTypeTag) && isEqual((XmlItem) lhsValue, (XmlValue) rhsValue);
            case TypeTags.XML_COMMENT_TAG:
                return TypeTags.isXMLTypeTag(rhsValTypeTag) && isEqual((XmlComment) lhsValue, (XmlValue) rhsValue);
            case TypeTags.XML_TEXT_TAG:
                return TypeTags.isXMLTypeTag(rhsValTypeTag) && isEqual((XmlText) lhsValue, (XmlValue) rhsValue);
            case TypeTags.XML_PI_TAG:
                return TypeTags.isXMLTypeTag(rhsValTypeTag) && isEqual((XmlPi) lhsValue, (XmlValue) rhsValue);
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
            case TypeTags.TABLE_TAG:
                return rhsValTypeTag == TypeTags.TABLE_TAG &&
                        isEqual((TableValueImpl) lhsValue, (TableValueImpl) rhsValue, checkedValues);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return checkValueEquals(lhsValue, rhsValue, checkedValues,
                        ((BTypeReferenceType) lhsValType).getReferredType(), rhsValType);
            case TypeTags.SERVICE_TAG:
            default:
                if (lhsValue instanceof RegExpValue && rhsValue instanceof RegExpValue) {
                    return isEqual((RegExpValue) lhsValue, (RegExpValue) rhsValue);
                }
                return false;
        }
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

        boolean isLhsKeyedTable =
                ((BTableType) TypeUtils.getReferredType(lhsTable.getType())).getFieldNames().length > 0;
        boolean isRhsKeyedTable =
                ((BTableType) TypeUtils.getReferredType(rhsTable.getType())).getFieldNames().length > 0;

        Object[] lhsTableValues = lhsTable.values().toArray();
        Object[] rhsTableValues = rhsTable.values().toArray();

        if (isLhsKeyedTable == isRhsKeyedTable) {
            for (int i = 0; i < lhsTableValues.length; i++) {
                if (!isEqual(lhsTableValues[i], rhsTableValues[i], checkedValues)) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Deep equality check for regular expressions.
     *
     * @param lhsRegExp Regular expression on the left hand side
     * @param rhsRegExp Regular expression on the right hand side
     * @return True if the regular expression values are equal, else false.
     */
    private static boolean isEqual(RegExpValue lhsRegExp, RegExpValue rhsRegExp) {
        return lhsRegExp.stringValue(null).equals(rhsRegExp.stringValue(null));
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
     * Deep equality check for XML Sequence.
     *
     * @param lhsXMLSequence The XML sequence on the left hand side
     * @param rhsXml The XML on the right hand side
     * @return True if the XML values are equal, else false.
     */
    private static boolean isEqual(XmlSequence lhsXMLSequence, XmlValue rhsXml) {
        if (rhsXml instanceof XmlSequence) {
            XmlSequence rhsXMLSequence = (XmlSequence) rhsXml;
            return isXMLSequenceChildrenEqual(lhsXMLSequence.getChildrenList(), rhsXMLSequence.getChildrenList());
        }
        if (rhsXml instanceof XmlItem) {
            return lhsXMLSequence.getChildrenList().size() == 1 &&
                    isEqual(lhsXMLSequence.getChildrenList().get(0), rhsXml);
        }
        return lhsXMLSequence.getChildrenList().isEmpty() &&
                TypeUtils.getType(rhsXml) == PredefinedTypes.TYPE_XML_NEVER;
    }

    /**
     * Deep equality check for XML item.
     *
     * @param lhsXMLItem The XML item on the left hand side
     * @param rhsXml The XML on the right hand side
     * @return True if the XML values are equal, else false.
     */
    private static boolean isEqual(XmlItem lhsXMLItem, XmlValue rhsXml) {
        if (rhsXml instanceof XmlItem) {
            XmlItem rhsXMLItem = (XmlItem) rhsXml;
            if (!(rhsXMLItem.getQName().equals(lhsXMLItem.getQName()))) {
                return false;
            }
            if (!(rhsXMLItem.getAttributesMap().entrySet().equals(lhsXMLItem.getAttributesMap().entrySet()))) {
                return false;
            }
            return isEqual(rhsXMLItem.getChildrenSeq(), lhsXMLItem.getChildrenSeq());
        }
        if (rhsXml instanceof XmlSequence) {
            XmlSequence rhsXMLSequence = (XmlSequence) rhsXml;
            return rhsXMLSequence.getChildrenList().size() == 1 &&
                    isEqual(lhsXMLItem, rhsXMLSequence.getChildrenList().get(0));
        }
        return false;
    }

    /**
     * Deep equality check for XML Text.
     *
     * @param lhsXMLText The XML text on the left hand side
     * @param rhsXml The XML on the right hand side
     * @return True if the XML values are equal, else false.
     */
    private static boolean isEqual(XmlText lhsXMLText, XmlValue rhsXml) {
        if (rhsXml instanceof XmlText) {
            XmlText rhsXMLText = (XmlText) rhsXml;
            return lhsXMLText.getTextValue().equals(rhsXMLText.getTextValue());
        }
        return lhsXMLText.getType() == PredefinedTypes.TYPE_XML_NEVER && rhsXml instanceof XmlSequence &&
                ((XmlSequence) rhsXml).getChildrenList().isEmpty();
    }

    /**
     * Deep equality check for XML Comment.
     *
     * @param lhsXMLComment The XML comment on the left hand side
     * @param rhsXml The XML on the right hand side
     * @return True if the XML values are equal, else false.
     */
    private static boolean isEqual(XmlComment lhsXMLComment, XmlValue rhsXml) {
        if (!(rhsXml instanceof XmlComment)) {
            return false;
        }
        XmlComment rhXMLComment = (XmlComment) rhsXml;
        return lhsXMLComment.getTextValue().equals(rhXMLComment.getTextValue());
    }

    /**
     * Deep equality check for XML Processing Instruction.
     *
     * @param lhsXMLPi The XML processing instruction on the left hand side
     * @param rhsXml The XML on the right hand side
     * @return True if the XML values are equal, else false.
     */
    private static boolean isEqual(XmlPi lhsXMLPi, XmlValue rhsXml) {
        if (!(rhsXml instanceof XmlPi)) {
            return false;
        }
        XmlPi rhsXMLPi = (XmlPi) rhsXml;
        return lhsXMLPi.getData().equals(rhsXMLPi.getData()) && lhsXMLPi.getTarget().equals(rhsXMLPi.getTarget());
    }

    private static boolean isXMLSequenceChildrenEqual(List<BXml> lhsList, List<BXml> rhsList) {
        if (lhsList.size() != rhsList.size()) {
            return false;
        }

        for (int i = 0; i < lhsList.size(); i++) {
            if (!isEqual(lhsList.get(i), rhsList.get(i))) {
                return false;
            }
        }
        return true;
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

        int typeTag = type.getTag();
        if (TypeTags.isXMLTypeTag(typeTag)) {
            return typeTag == TypeTags.XML_TAG || typeTag == TypeTags.XML_TEXT_TAG;
        }

        if (typeTag < TypeTags.RECORD_TYPE_TAG &&
                !(typeTag == TypeTags.CHAR_STRING_TAG || typeTag == TypeTags.NEVER_TAG)) {
            return true;
        }
        switch (typeTag) {
            case TypeTags.STREAM_TAG:
            case TypeTags.MAP_TAG:
            case TypeTags.ANY_TAG:
                return true;
            case TypeTags.ARRAY_TAG:
                return checkFillerValue((BArrayType) type, unanalyzedTypes);
            case TypeTags.FINITE_TYPE_TAG:
                return checkFillerValue((BFiniteType) type);
            case TypeTags.OBJECT_TYPE_TAG:
                return checkFillerValue((BObjectType) type);
            case TypeTags.RECORD_TYPE_TAG:
                return checkFillerValue((BRecordType) type, unanalyzedTypes);
            case TypeTags.TUPLE_TAG:
                return checkFillerValue((BTupleType) type, unanalyzedTypes);
            case TypeTags.UNION_TAG:
                return checkFillerValue((BUnionType) type, unanalyzedTypes);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return hasFillerValue(((BTypeReferenceType) type).getReferredType(), unanalyzedTypes);
            case TypeTags.INTERSECTION_TAG:
                return hasFillerValue(((BIntersectionType) type).getEffectiveType(), unanalyzedTypes);
            default:
                return false;
        }
    }

    private static boolean checkFillerValue(BTupleType tupleType,  List<Type> unAnalyzedTypes) {
        if (unAnalyzedTypes.contains(tupleType)) {
            return true;
        }
        unAnalyzedTypes.add(tupleType);

        for (Type member : tupleType.getTupleTypes()) {
            if (!hasFillerValue(member, unAnalyzedTypes)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkFillerValue(BUnionType type,  List<Type> unAnalyzedTypes) {
        if (unAnalyzedTypes.contains(type)) {
            return true;
        }
        unAnalyzedTypes.add(type);

        // NIL is a member.
        if (type.isNullable()) {
            return true;
        }
        return isSameBasicTypeWithFillerValue(type.getMemberTypes());
    }

    private static boolean isSameBasicTypeWithFillerValue(List<Type> memberTypes) {

        // here finite types and non finite types are separated
        // for finite types only all their value space items are collected
        List<Type> nonFiniteTypes = new ArrayList<>();
        Set<Object> combinedValueSpace = new HashSet<>();
        for (Type memberType: memberTypes) {
            Type referredType = TypeUtils.getReferredType(memberType);
            if (referredType.getTag() == TypeTags.FINITE_TYPE_TAG) {
                combinedValueSpace.addAll(((BFiniteType) referredType).getValueSpace());
            } else {
                nonFiniteTypes.add(referredType);
            }
        }

        if (nonFiniteTypes.isEmpty()) {
            // only finite types are there, so the check narrows to one finite type like case
            return hasFillerValueInValueSpace(combinedValueSpace);
        } else {
            // non finite types are available
            Iterator<Type> iterator = nonFiniteTypes.iterator();
            Type firstMember = iterator.next();

            // non finite types are checked whether they are the same type
            Type nextMember;
            while (iterator.hasNext()) {
                nextMember = iterator.next();
                if (!isSameBasicType(firstMember, nextMember)) {
                    return false;
                }
            }

            // if no finite types the checking ends here
            if (combinedValueSpace.isEmpty()) {
                return hasFillerValue(firstMember);
            }

            // both finite and non finite types are available
            // finite types are checked whether they are the type of non finite types
            if (!containsSameBasicType(firstMember, combinedValueSpace)) {
                return false;
            }

            // all members are same basic types
            // need to check filler value is there
            if (hasFillerValue(firstMember)) {
                return true;
            }
            return combinedValueSpace.size() == 1 ?
                    isFillerValueOfFiniteTypeBasicType(combinedValueSpace.iterator().next()) :
                    hasFillerValueInValueSpace(combinedValueSpace);
        }
    }

    private static boolean isSameBasicType(Type sourceType, Type targetType) {
        if (isSameType(sourceType, targetType)) {
            return true;
        }
        int sourceTag = sourceType.getTag();
        int targetTag = targetType.getTag();
        if (TypeTags.isStringTypeTag(sourceTag) && TypeTags.isStringTypeTag(targetTag)) {
            return true;
        }
        if (TypeTags.isXMLTypeTag(sourceTag) && TypeTags.isXMLTypeTag(targetTag)) {
            return true;
        }
        return isIntegerSubTypeTag(sourceTag) && isIntegerSubTypeTag(targetTag);
    }

    private static boolean isIntegerSubTypeTag(int typeTag) {
        return TypeTags.isIntegerTypeTag(typeTag) || typeTag == TypeTags.BYTE_TAG;
    }

    private static boolean isFillerValueOfFiniteTypeBasicType(Object value) {
        switch (value.toString()) {
            case "0":
            case "0.0":
            case "false":
            case "":
                return true;
            default:
                return false;
        }
    }

    private static boolean containsSameBasicType (Type nonFiniteType, Set<Object> finiteTypeValueSpace) {
        for (Object value : finiteTypeValueSpace) {
            if (!isSameBasicType(getType(value), nonFiniteType)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkFillerValue(BRecordType type, List<Type> unAnalyzedTypes) {
        if (unAnalyzedTypes.contains(type)) {
            return true;
        }
        unAnalyzedTypes.add(type);
        for (Field field : type.getFields().values()) {
            if (SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.OPTIONAL)) {
                continue;
            }
            if (!SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.REQUIRED)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static boolean checkFillerValue(BArrayType type, List<Type> unAnalyzedTypes) {
        return type.getState() == ArrayState.OPEN || hasFillerValue(type.getElementType(), unAnalyzedTypes);
    }

    private static boolean checkFillerValue(BObjectType type) {
        if (type.getTag() == TypeTags.SERVICE_TAG) {
            return false;
        } else {
            MethodType generatedInitializer = type.generatedInitializer;
            if (generatedInitializer == null) {
                // abstract objects doesn't have a filler value.
                return false;
            }
            FunctionType initFuncType = generatedInitializer.getType();
            // Todo: check defaultable params of the init func as well
            boolean noParams = initFuncType.getParameters().length == 0;
            boolean nilReturn = initFuncType.getReturnType().getTag() == TypeTags.NULL_TAG;
            return noParams && nilReturn;
        }
    }

    private static boolean checkFillerValue(BFiniteType type) {
        return hasFillerValueInValueSpace(type.getValueSpace());
    }

    private static boolean hasFillerValueInValueSpace(Set<Object> finiteTypeValueSpace) {
        // For singleton types, that value is the implicit initial value
        if (finiteTypeValueSpace.size() == 1) {
            return true;
        }

        // Has NIL element as a member.
        for (Object value: finiteTypeValueSpace) {
            if (value == null) {
                return true;
            }
        }

        Object firstElement = finiteTypeValueSpace.iterator().next();
        for (Object value : finiteTypeValueSpace) {
            if (value.getClass() != firstElement.getClass()) {
                return false;
            }
        }

        if (firstElement instanceof BString) {
            return containsElement(finiteTypeValueSpace, "");
        } else if ((firstElement instanceof Long) || (firstElement instanceof BDecimal)) {
            return containsElement(finiteTypeValueSpace, "0");
        } else if (firstElement instanceof Double) {
            return containsElement(finiteTypeValueSpace, "0.0");
        } else if (firstElement instanceof Boolean) {
            return containsElement(finiteTypeValueSpace, "false");
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

    public static Object handleAnydataValues(Object sourceVal, Type targetType) {
        if (sourceVal != null && !(sourceVal instanceof Number) && !(sourceVal instanceof BString) &&
                !(sourceVal instanceof Boolean) && !(sourceVal instanceof BValue)) {
            throw ErrorUtils.createJToBTypeCastError(sourceVal.getClass(), targetType);
        }
        return sourceVal;
    }

    private static BError createTypeCastError(Object value, Type targetType, List<String> errors) {
        if ((errors == null) || (errors.isEmpty())) {
            return ErrorUtils.createTypeCastError(value, targetType);
        } else {
            if (errors.size() == MAX_TYPECAST_ERROR_COUNT + 1) {
                errors.remove(MAX_TYPECAST_ERROR_COUNT);
                errors.add("...");
            }
            StringBuilder errorMsg = new StringBuilder();
            for (String error : errors) {
                errorMsg.append("\n\t\t").append(error);
            }
            return ErrorUtils.createTypeCastError(value, targetType, errorMsg.toString());
        }
    }

    private TypeChecker() {
    }
}
