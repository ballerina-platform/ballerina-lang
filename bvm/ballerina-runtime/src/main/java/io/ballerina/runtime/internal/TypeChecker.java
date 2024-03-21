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
import io.ballerina.runtime.api.TypeBuilder;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.commons.TypeValuePair;
import io.ballerina.runtime.internal.types.BAnnotatableType;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BTypeReferenceType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.HandleValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.RegExpValue;
import io.ballerina.runtime.internal.values.TableValueImpl;
import io.ballerina.runtime.internal.values.TypedescValue;
import io.ballerina.runtime.internal.values.TypedescValueImpl;
import io.ballerina.runtime.internal.values.XmlComment;
import io.ballerina.runtime.internal.values.XmlItem;
import io.ballerina.runtime.internal.values.XmlPi;
import io.ballerina.runtime.internal.values.XmlSequence;
import io.ballerina.runtime.internal.values.XmlText;
import io.ballerina.runtime.internal.values.XmlValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BBYTE_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.BBYTE_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.REGEXP_LANG_LIB;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED8_MAX_VALUE;
import static io.ballerina.runtime.api.utils.TypeUtils.getImpliedType;
import static io.ballerina.runtime.internal.CloneUtils.getErrorMessage;

/**
 * Responsible for performing runtime type checking.
 *
 * @since 0.995.0
 */
@SuppressWarnings({"rawtypes"})
public class TypeChecker {

    protected static final byte MAX_TYPECAST_ERROR_COUNT = 20;
    private static final String REG_EXP_TYPENAME = "RegExp";

    public static Object checkCast(Object sourceVal, Type targetType) {

        List<String> errors = new ArrayList<>();
        // TODO: use the early failure mechanism here as well
        Type sourceType = getImpliedType(getType(sourceVal));
        if (checkIsType(errors, sourceVal, sourceType, targetType)) {
            return sourceVal;
        }

        if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG && targetType.getTag() <= TypeTags.BOOLEAN_TAG) {
            return TypeConverter.castValues(targetType, sourceVal);
        }

        // if the source is a numeric value and the target type is a union, try to find a matching
        // member.
        if (sourceType.getTag() <= TypeTags.BOOLEAN_TAG && targetType.getTag() == TypeTags.UNION_TAG) {
            for (Type memberType : TypeHelper.members(targetType)) {
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
        return SemanticTypeEngine.checkIsType(sourceVal, targetType);
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
        return SemanticTypeEngine.checkIsType(errors, sourceVal, sourceType, targetType);
    }

    /**
     * Check whether a given value has the same shape as the given type.
     *
     * @param sourceValue value to check the shape
     * @param targetType type to check the shape against
     * @return true if the value has the same shape as the given type; false otherwise
     */
    public static boolean checkIsLikeType(Object sourceValue, Type targetType) {
        return SemanticTypeEngine.checkIsLikeType(sourceValue, targetType);
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
        return SemanticTypeEngine.checkIsLikeType(sourceValue, targetType, allowNumericConversion);
    }

    /**
     * Check whether two types are the same.
     *
     * @param sourceType type to test
     * @param targetType type to test against
     * @return true if the two types are same; false otherwise
     */
    public static boolean isSameType(Type sourceType, Type targetType) {
        return SemanticTypeEngine.isSameType(sourceType, targetType);
    }

    public static Type getType(Object value) {
        return SemanticTypeEngine.getType(value);
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

        Type lhsType = getImpliedType(getType(lhsValue));
        Type rhsType = getImpliedType(getType(rhsValue));

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
        if (SemanticTypeEngine.isSimpleBasicType(type)) {
            return new TypedescValueImpl(new BFiniteType(value.toString(), Set.of(value), 0));
        }
        if (value instanceof BRefValue) {
            return (TypedescValue) ((BRefValue) value).getTypedesc();
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
        Type describingType = TypeBuilder.toBType(typedescValue.getDescribingType());
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
        return SemanticTypeEngine.checkIsType(sourceType, targetType);
    }

    static boolean isSimpleBasicType(Type type) {
        return SemanticTypeEngine.isSimpleBasicType(type);
    }

    static boolean isFiniteTypeValue(Object sourceValue, Type sourceType, Object valueSpaceItem,
                                     boolean allowNumericConversion) {
        return SemanticTypeEngine.isFiniteTypeValue(sourceValue, sourceType, valueSpaceItem, allowNumericConversion);
    }

    static boolean checkIsLikeUnionType(List<String> errors, Object sourceValue, Type targetType,
                                        List<TypeValuePair> unresolvedValues,
                                        boolean allowNumericConversion, String varName) {
        return SemanticTypeEngine.checkIsLikeUnionType(errors, sourceValue, targetType, unresolvedValues,
                allowNumericConversion, varName);
    }

    static boolean checkIsLikeType(List<String> errors, Object sourceValue, Type targetType,
                                   List<TypeValuePair> unresolvedValues,
                                   boolean allowNumericConversion, String varName) {
        return SemanticTypeEngine.checkIsLikeType(errors, sourceValue, targetType, unresolvedValues,
                allowNumericConversion, varName);
    }

    static boolean checkIsType(Object sourceVal, Type sourceType, Type targetType,
                               List<TypeChecker.TypePair> unresolvedTypes) {
        return SemanticTypeEngine.checkIsType(sourceVal, sourceType, targetType, unresolvedTypes);
    }

    @Deprecated
    public static boolean checkIsType(Type sourceType, Type targetType, List<TypePair> unresolvedTypes) {
        return SemanticTypeEngine.checkIsType(sourceType, targetType, unresolvedTypes);
    }


    public static boolean isInherentlyImmutableType(Type sourceType) {
        return SemanticTypeEngine.isInherentlyImmutableType(sourceType);
    }

    public static boolean isSelectivelyImmutableType(Type type, Set<Type> unresolvedTypes) {
        return SemanticTypeEngine.isSelectivelyImmutableType(type, unresolvedTypes);
    }


    public static boolean isNumericType(Type type) {
        return SemanticTypeEngine.isNumericType(type);
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

    protected static void addErrorMessage(int initialErrorCount, String errorMessage, List<String> errors) {
        if ((errors != null) && (errors.size() <= MAX_TYPECAST_ERROR_COUNT) &&
                ((errors.size() - initialErrorCount) == 0)) {
            errors.add(errorMessage);
        }
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
        lhsValType = getImpliedType(lhsValType);
        rhsValType = getImpliedType(rhsValType);
        int lhsValTypeTag = lhsValType.getTag();
        int rhsValTypeTag = rhsValType.getTag();

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
                if (lhsValue instanceof XmlText xmlText) {
                    return TypeTags.isXMLTypeTag(rhsValTypeTag) && isEqual(xmlText, (XmlValue) rhsValue);
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
                TypeHelper.tableFieldNames(getImpliedType(lhsTable.getType())).length > 0;
        boolean isRhsKeyedTable =
                TypeHelper.tableFieldNames(getImpliedType(rhsTable.getType())).length > 0;

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

    static boolean isRegExpType(Type targetType) {
        if (targetType.getTag() == TypeTags.TYPE_REFERENCED_TYPE_TAG) {
            Type referredType = ((BTypeReferenceType) targetType).getReferredType();
            Module referredTypePackage = referredType.getPackage();
            if ((referredTypePackage != null) && BALLERINA_BUILTIN_PKG_PREFIX.equals(referredTypePackage.getOrg())
                    && REGEXP_LANG_LIB.equals(referredTypePackage.getName())
                    && REG_EXP_TYPENAME.equals(referredType.getName())) {
                return true;
            }
            return isRegExpType(referredType);
        }
        return false;
    }

    static boolean isStructuredType(Type type) {
        Type referredType = getImpliedType(type);
        switch (referredType.getTag()) {
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
            case TypeTags.MAP_TAG:
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.TABLE_TAG:
                return true;
            default:
                return false;
        }
    }

    /**
     * Type vector of size two, to hold the source and the target types.
     *
     * @since 0.995.0
     */
    static class TypePair {
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
        return SemanticTypeEngine.hasFillerValue(type);
    }

    static boolean hasFillerValue(Type type, List<Type> unanalyzedTypes) {
        return SemanticTypeEngine.hasFillerValue(type, unanalyzedTypes);
    }

    private static BError createTypeCastError(Object value, Type targetType, List<String> errors) {
        if ((errors == null) || (errors.isEmpty())) {
            return ErrorUtils.createTypeCastError(value, targetType);
        } else {
            return ErrorUtils.createTypeCastError(value, targetType, getErrorMessage(errors, MAX_TYPECAST_ERROR_COUNT));
        }
    }

    private TypeChecker() {
    }

    // Needed for Syntactic TypeEngine

    static boolean isByteLiteral(long longValue) {
        return (longValue >= BBYTE_MIN_VALUE && longValue <= BBYTE_MAX_VALUE);
    }

    static boolean checkIsUnionType(Type sourceType, Type targetType,
                                    List<TypeChecker.TypePair> unresolvedTypes) {
        return SemanticTypeEngine.checkIsUnionType(sourceType, targetType, unresolvedTypes);
    }

    static boolean isUnionTypeMatch(Type sourceType, Type targetType,
                                    List<TypeChecker.TypePair> unresolvedTypes) {
        return SemanticTypeEngine.isUnionTypeMatch(sourceType, targetType, unresolvedTypes);
    }

    static boolean checkIsJSONType(Type sourceType, List<TypeChecker.TypePair> unresolvedTypes) {
        return SemanticTypeEngine.checkIsJSONType(sourceType, unresolvedTypes);
    }

    static boolean isFiniteTypeMatch(Type sourceType, Type targetType) {
        return SemanticTypeEngine.isFiniteTypeMatch(sourceType, targetType);
    }

    static boolean checkIsLikeType(Object sourceValue, Type targetType, List<TypeValuePair> unresolvedValues,
                                   boolean allowNumericConversion, String varName, List<String> errors) {
        return SemanticTypeEngine.checkIsLikeType(sourceValue, targetType, unresolvedValues, allowNumericConversion,
                varName, errors);
    }

    static boolean checkIsLikeType(Object sourceValue, Type targetType, List<TypeValuePair> unresolvedValues,
                                   boolean allowNumericConversion) {
        return SemanticTypeEngine.checkIsLikeType(sourceValue, targetType, unresolvedValues, allowNumericConversion);
    }

    static boolean checkIsLikeType(Object sourceValue, Type sourceType, Type targetType,
                                   List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        return SemanticTypeEngine.checkIsLikeType(sourceValue, sourceType, targetType, unresolvedValues,
                allowNumericConversion);
    }

    // TODO: merge with above
    static boolean checkFiniteTypeAssignable(Object sourceValue, Type sourceType, Type targetType,
                                             List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        return SemanticTypeEngine.checkFiniteTypeAssignable(sourceValue, sourceType, targetType, unresolvedValues,
                allowNumericConversion);
    }

    public static Object handleAnydataValues(Object sourceVal, Type targetType) {
        return SemanticTypeEngine.handleAnydataValues(sourceVal, targetType);
    }

    public static boolean isAnydata(Type type) {
        // TODO: maybe we should do type equality not subtype here
        return checkIsType(type, PredefinedTypes.TYPE_ANYDATA);
    }

    public static boolean isReadonly(Type type) {
        return checkIsType(type, PredefinedTypes.TYPE_READONLY);
    }

    public static boolean isNever(Type type) {
        return checkIsType(type, PredefinedTypes.TYPE_NEVER);
    }
}
