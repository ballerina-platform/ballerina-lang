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
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType.ArrayState;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ParameterizedType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.semtype.BasicTypeCode;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.types.BAnnotatableType;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BBooleanType;
import io.ballerina.runtime.internal.types.BByteType;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BFloatType;
import io.ballerina.runtime.internal.types.BIntegerType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BObjectType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BTypeReferenceType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.types.BXmlType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.HandleValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.RegExpValue;
import io.ballerina.runtime.internal.values.TableValueImpl;
import io.ballerina.runtime.internal.values.TypedescValue;
import io.ballerina.runtime.internal.values.TypedescValueImpl;
import io.ballerina.runtime.internal.values.ValuePair;
import io.ballerina.runtime.internal.values.XmlComment;
import io.ballerina.runtime.internal.values.XmlItem;
import io.ballerina.runtime.internal.values.XmlPi;
import io.ballerina.runtime.internal.values.XmlSequence;
import io.ballerina.runtime.internal.values.XmlText;
import io.ballerina.runtime.internal.values.XmlValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_NULL;
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
import static io.ballerina.runtime.api.types.semtype.Core.B_TYPE_TOP;
import static io.ballerina.runtime.api.types.semtype.Core.SEMTYPE_TOP;
import static io.ballerina.runtime.api.utils.TypeUtils.getImpliedType;
import static io.ballerina.runtime.internal.CloneUtils.getErrorMessage;

/**
 * Responsible for performing runtime type checking.
 *
 * @since 0.995.0
 */
@SuppressWarnings({"rawtypes"})
public class TypeChecker {

    private static final String REG_EXP_TYPENAME = "RegExp";
    private static final Map<Long, Context> contexts = new ConcurrentHashMap<>(10);

    public static Object checkCast(Object sourceVal, Type targetType) {

        List<String> errors = new ArrayList<>();
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

    private static Context context() {
        // We are pinning each context to thread. This depends on the assumption physical thread is not going to
        // get switched while type checking
        Thread currentThread = Thread.currentThread();
        long threadID = currentThread.getId();
        Context cx = contexts.get(threadID);
        if (cx != null) {
            return cx;
        }
        cx = Context.from(Env.getInstance());
        contexts.put(threadID, cx);
        return cx;
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
        SemType targetSemType = Builder.from(targetType);
        SemType targetBasicTypeUnion = Core.widenToBasicTypeUnion(targetSemType);
        SemType valueBasicType = basicType(sourceVal);
        if (!Core.isSubtypeSimple(valueBasicType, targetBasicTypeUnion)) {
            return false;
        }
        if (targetBasicTypeUnion == targetSemType) {
            return true;
        }
        SemType sourceSemType = Builder.from(getType(sourceVal));
        return switch (isSubTypeInner(sourceVal, sourceSemType, targetSemType)) {
            case TRUE -> true;
            case FALSE -> false;
            case MAYBE -> FallbackTypeChecker.checkIsType(null, sourceVal, bTypePart(sourceSemType),
                    bTypePart(targetSemType));
        };
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
        return switch (isSubType(sourceVal, sourceType, targetType)) {
            case TRUE -> true;
            case FALSE -> false;
            case MAYBE ->
                    FallbackTypeChecker.checkIsType(errors, sourceVal, bTypePart(sourceType), bTypePart(targetType));
        };
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
        return FallbackTypeChecker.checkIsLikeType(null, sourceValue, targetType, new ArrayList<>(),
                allowNumericConversion,
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
        return sourceType == targetType || sourceType.equals(targetType);
    }

    public static Type getType(Object value) {
        if (value == null) {
            return TYPE_NULL;
        } else if (value instanceof Number number) {
            if (value instanceof Double) {
                return BFloatType.singletonType(number.doubleValue());
            }
            long numberValue =
                    number instanceof Byte byteValue ? Byte.toUnsignedLong(byteValue) : number.longValue();
            if (value instanceof Long) {
                return BIntegerType.singletonType(numberValue);
            } else if (value instanceof Integer || value instanceof Byte) {
                return BByteType.singletonType(numberValue);
            }
        } else if (value instanceof Boolean booleanValue) {
            return BBooleanType.singletonType(booleanValue);
        } else if (value instanceof BObject) {
            return ((BObject) value).getOriginalType();
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
        return isEqual(lhsValue, rhsValue, new HashSet<>());
    }

    /**
     * Check if two decimal values are exactly equal.
     *
     * @param lhsValue The value on the left-hand side
     * @param rhsValue The value of the right-hand side
     * @return True if values are exactly equal, else false.
     */

    public static boolean checkDecimalExactEqual(DecimalValue lhsValue, DecimalValue rhsValue) {
        return FallbackTypeChecker.isDecimalRealNumber(lhsValue) && FallbackTypeChecker.isDecimalRealNumber(rhsValue)
                && lhsValue.decimalValue().equals(rhsValue.decimalValue());
    }

    /**
     * Reference equality check for values. If both the values are simple basic types, returns the same
     * result as {@link #isEqual(Object, Object, Set)}
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
                return FallbackTypeChecker.isXMLValueRefEqual((XmlValue) lhsValue, (XmlValue) rhsValue);
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
                    return ((RegExpValue) lhsValue).equals(rhsValue, new HashSet<>());
                }
                return false;
        }
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
        if (FallbackTypeChecker.isSimpleBasicType(type)) {
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
     * @return flag indicating the equivalence of the two types
     */
    public static boolean checkIsType(Type sourceType, Type targetType) {
        return switch (isSubType(sourceType, targetType)) {
            case TRUE -> true;
            case FALSE -> false;
            case MAYBE -> FallbackTypeChecker.checkIsType(bTypePart(sourceType), bTypePart(targetType), null);
        };
    }

    @Deprecated
    public static boolean checkIsType(Type sourceType, Type targetType, List<TypePair> unresolvedTypes) {
        return switch (isSubType(sourceType, targetType)) {
            case TRUE -> true;
            case FALSE -> false;
            case MAYBE ->
                    FallbackTypeChecker.checkIsType(bTypePart(sourceType), bTypePart(targetType), unresolvedTypes);
        };
    }

    static boolean checkIsType(Object sourceVal, Type sourceType, Type targetType, List<TypePair> unresolvedTypes) {
        return switch (isSubType(sourceVal, sourceType, targetType)) {
            case TRUE -> true;
            case FALSE -> false;
            case MAYBE -> FallbackTypeChecker.checkIsType(sourceVal, bTypePart(sourceType), bTypePart(targetType),
                    unresolvedTypes);
        };
    }

    /**
     * Check if two decimal values are equal in value.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value of the right hand side
     * @return True if values are equal, else false.
     */
    public static boolean checkDecimalEqual(DecimalValue lhsValue, DecimalValue rhsValue) {
        return FallbackTypeChecker.isDecimalRealNumber(lhsValue) && FallbackTypeChecker.isDecimalRealNumber(rhsValue) &&
                lhsValue.decimalValue().compareTo(rhsValue.decimalValue()) == 0;
    }

    public static boolean isNumericType(Type type) {
        type = getImpliedType(type);
        return type.getTag() < TypeTags.STRING_TAG || TypeTags.isIntegerTypeTag(type.getTag());
    }

    static boolean isByteLiteral(long longValue) {
        return (longValue >= BBYTE_MIN_VALUE && longValue <= BBYTE_MAX_VALUE);
    }

    // Private methods

    private enum TypeCheckResult {
        TRUE,
        FALSE,
        MAYBE
    }

    private static TypeCheckResult isSubType(Object sourceValue, Type source, Type target) {
        TypeCheckResult result = isSubType(source, target);
        if (result != TypeCheckResult.FALSE || !source.isReadOnly()) {
            return result;
        }
        return isSubTypeImmutableValue(sourceValue, Builder.from(target));
    }

    private static TypeCheckResult isSubType(Type source, Type target) {
        if (source instanceof ParameterizedType sourceParamType) {
            if (target instanceof ParameterizedType targetParamType) {
                return isSubType(sourceParamType.getParamValueType(), targetParamType.getParamValueType());
            }
            return isSubType(sourceParamType.getParamValueType(), target);
        }
        return isSubTypeInner(Builder.from(source), Builder.from(target));
    }

    private static TypeCheckResult isSubTypeInner(Object sourceValue, SemType source, SemType target) {
        TypeCheckResult result = isSubTypeInner(source, target);
        if (result != TypeCheckResult.FALSE ||
                !Core.isSubType(context(), Core.intersect(source, SEMTYPE_TOP), Builder.readonlyType())) {
            return result;
        }
        return isSubTypeImmutableValue(sourceValue, target);
    }

    private static TypeCheckResult isSubTypeImmutableValue(Object sourceValue, SemType target) {
        Optional<SemType> sourceSingletonType = Builder.typeOf(sourceValue);
        if (sourceSingletonType.isEmpty()) {
            return Core.containsBasicType(target, B_TYPE_TOP) ? TypeCheckResult.MAYBE : TypeCheckResult.FALSE;
        }
        SemType singletonType = sourceSingletonType.get();
        return isSubTypeInner(singletonType, target);
    }

    private static TypeCheckResult isSubTypeInner(SemType source, SemType target) {
        Context cx = context();
        if (!Core.containsBasicType(source, B_TYPE_TOP)) {
            return Core.isSubType(cx, source, target) ? TypeCheckResult.TRUE : TypeCheckResult.FALSE;
        }
        if (!Core.containsBasicType(target, B_TYPE_TOP)) {
            return TypeCheckResult.FALSE;
        }
        SemType sourcePureSemType = Core.intersect(source, SEMTYPE_TOP);
        SemType targetPureSemType = Core.intersect(target, SEMTYPE_TOP);
        return Core.isSubType(cx, sourcePureSemType, targetPureSemType) ? TypeCheckResult.MAYBE : TypeCheckResult.FALSE;
    }

    private static SemType basicType(Object value) {
        if (value == null) {
            return Builder.nilType();
        } else if (value instanceof Double) {
            return Builder.floatType();
        } else if (value instanceof Number) {
            return Builder.intType();
        } else if (value instanceof BString) {
            return Builder.stringType();
        } else if (value instanceof Boolean) {
            return Builder.booleanType();
        } else if (value instanceof DecimalValue) {
            return Builder.decimalType();
        } else if (value instanceof ArrayValue) {
            return Builder.listType();
        } else {
            return Builder.bType();
        }
    }

    private static BType bTypePart(Type t) {
        return bTypePart(Builder.from(t));
    }

    private static BType bTypePart(SemType t) {
        return (BType) Core.subTypeData(t, BasicTypeCode.BT_B_TYPE);
    }

    public static boolean isInherentlyImmutableType(Type sourceType) {
        sourceType = getImpliedType(sourceType);
        if (FallbackTypeChecker.isSimpleBasicType(sourceType)) {
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
            case TypeTags.REG_EXP_TYPE_TAG:    
                return true;
            case TypeTags.XML_TAG:
                return ((BXmlType) sourceType).constraint.getTag() == TypeTags.NEVER_TAG;
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return isInherentlyImmutableType(((BTypeReferenceType) sourceType).getReferredType());
            default:
                return false;
        }
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
            default:
                return false;
        }
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

    /**
     * Deep value equality check for anydata.
     *
     * @param lhsValue The value on the left hand side
     * @param rhsValue The value on the right hand side
     * @param checkedValues Structured value pairs already compared or being compared
     * @return True if values are equal, else false.
     */
    public static boolean isEqual(Object lhsValue, Object rhsValue, Set<ValuePair> checkedValues) {
        if (lhsValue == rhsValue) {
            return true;
        }

        if (null == lhsValue || null == rhsValue) {
            return false;
        }

        return checkValueEquals(lhsValue, rhsValue, checkedValues, getType(lhsValue), getType(rhsValue));
    }

    private static boolean checkValueEquals(Object lhsValue, Object rhsValue, Set<ValuePair> checkedValues,
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
                    return TypeTags.isXMLTypeTag(rhsValTypeTag) && xmlText.equals(rhsValue, checkedValues);
                }
                return TypeTags.isXMLTypeTag(rhsValTypeTag) && ((XmlSequence) lhsValue).equals(rhsValue, checkedValues);
            case TypeTags.XML_ELEMENT_TAG:
                return TypeTags.isXMLTypeTag(rhsValTypeTag) && ((XmlItem) lhsValue).equals(rhsValue, checkedValues);
            case TypeTags.XML_COMMENT_TAG:
                return TypeTags.isXMLTypeTag(rhsValTypeTag) && ((XmlComment) lhsValue).equals(rhsValue, checkedValues);
            case TypeTags.XML_TEXT_TAG:
                return TypeTags.isXMLTypeTag(rhsValTypeTag) && ((XmlText) lhsValue).equals(rhsValue, checkedValues);
            case TypeTags.XML_PI_TAG:
                return TypeTags.isXMLTypeTag(rhsValTypeTag) && ((XmlPi) lhsValue).equals(rhsValue, checkedValues);
            case TypeTags.MAP_TAG:
            case TypeTags.JSON_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return isMappingType(rhsValTypeTag) && ((MapValueImpl) lhsValue).equals(rhsValue, checkedValues);
            case TypeTags.TUPLE_TAG:
            case TypeTags.ARRAY_TAG:
                return isListType(rhsValTypeTag) && ((ArrayValue) lhsValue).equals(rhsValue, checkedValues);
            case TypeTags.ERROR_TAG:
                return rhsValTypeTag == TypeTags.ERROR_TAG && ((ErrorValue) lhsValue).equals(rhsValue, checkedValues);
            case TypeTags.TABLE_TAG:
                return rhsValTypeTag == TypeTags.TABLE_TAG &&
                        ((TableValueImpl) lhsValue).equals(rhsValue, checkedValues);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG:
                return checkValueEquals(lhsValue, rhsValue, checkedValues,
                        ((BTypeReferenceType) lhsValType).getReferredType(), rhsValType);
            case TypeTags.SERVICE_TAG:
            default:
                if (lhsValue instanceof RegExpValue) {
                    return ((RegExpValue) lhsValue).equals(rhsValue, checkedValues);
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

    public static boolean isRegExpType(Type targetType) {
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
        return switch (referredType.getTag()) {
            case TypeTags.ARRAY_TAG,
                    TypeTags.TUPLE_TAG,
                    TypeTags.MAP_TAG,
                    TypeTags.RECORD_TYPE_TAG,
                    TypeTags.TABLE_TAG ->
                    true;
            default -> false;
        };
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
            if (!(obj instanceof TypePair other)) {
                return false;
            }
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
            case TypeTags.SERVICE_TAG:
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

        // here finite types and non-finite types are separated
        // for finite types only all their value space items are collected
        List<Type> nonFiniteTypes = new ArrayList<>();
        Set<Object> combinedValueSpace = new HashSet<>();
        for (Type memberType: memberTypes) {
            Type referredType = getImpliedType(memberType);
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
            // non-finite types are available
            Iterator<Type> iterator = nonFiniteTypes.iterator();
            Type firstMember = iterator.next();

            // non-finite types are checked whether they are the same type
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

            // both finite and non-finite types are available
            // finite types are checked whether they are the type of non-finite types
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
        int sourceTag = getImpliedType(sourceType).getTag();
        int targetTag = getImpliedType(targetType).getTag();
        if (TypeTags.isStringTypeTag(sourceTag) && TypeTags.isStringTypeTag(targetTag)) {
            return true;
        }
        if (TypeTags.isXMLTypeTag(sourceTag) && TypeTags.isXMLTypeTag(targetTag)) {
            return true;
        }
        return isIntegerSubTypeTag(sourceTag) && isIntegerSubTypeTag(targetTag);
    }

    private static boolean isIntegerSubTypeTag(int typeTag) {
        return TypeTags.isIntegerTypeTag(typeTag);
    }

    private static boolean isFillerValueOfFiniteTypeBasicType(Object value) {
        return switch (value.toString()) {
            case "0", "0.0", "false", "" -> true;
            default -> false;
        };
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
        MethodType generatedInitMethod = type.getGeneratedInitMethod();
        if (generatedInitMethod == null) {
            // abstract objects doesn't have a filler value.
            return false;
        }
        FunctionType initFuncType = generatedInitMethod.getType();
        boolean noParams = initFuncType.getParameters().length == 0;
        boolean nilReturn = getImpliedType(initFuncType.getReturnType()).getTag() == TypeTags.NULL_TAG;
        return noParams && nilReturn;

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
        } else if ((firstElement instanceof Integer) || (firstElement instanceof Long) ||
                (firstElement instanceof BDecimal)) {
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
            return ErrorUtils.createTypeCastError(value, targetType,
                    getErrorMessage(errors, FallbackTypeChecker.MAX_TYPECAST_ERROR_COUNT));
        }
    }

    private TypeChecker() {
    }
}
