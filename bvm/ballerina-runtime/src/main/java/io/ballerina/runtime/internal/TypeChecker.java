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
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType.ArrayState;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ReadonlyType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.CacheableTypeDescriptor;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.ShapeAnalyzer;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.types.BAnnotatableType;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BBooleanType;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BObjectType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BTypeReferenceType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.types.TypeWithShape;
import io.ballerina.runtime.internal.utils.ErrorUtils;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.DecimalValueKind;
import io.ballerina.runtime.internal.values.HandleValue;
import io.ballerina.runtime.internal.values.RefValue;
import io.ballerina.runtime.internal.values.RegExpValue;
import io.ballerina.runtime.internal.values.TypedescValue;
import io.ballerina.runtime.internal.values.TypedescValueImpl;
import io.ballerina.runtime.internal.values.ValuePair;
import io.ballerina.runtime.internal.values.XmlSequence;
import io.ballerina.runtime.internal.values.XmlValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_BOOLEAN;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_BYTE;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_DECIMAL;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_FLOAT;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_INT;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_INT_SIGNED_16;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_INT_SIGNED_32;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_INT_SIGNED_8;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_INT_UNSIGNED_16;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_INT_UNSIGNED_32;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_INT_UNSIGNED_8;
import static io.ballerina.runtime.api.types.PredefinedTypes.TYPE_NULL;
import static io.ballerina.runtime.api.utils.TypeUtils.getImpliedType;
import static io.ballerina.runtime.internal.utils.CloneUtils.getErrorMessage;

/**
 * Responsible for performing runtime type checking.
 *
 * @since 0.995.0
 */
@SuppressWarnings({"rawtypes"})
public final class TypeChecker {

    private static final byte MAX_TYPECAST_ERROR_COUNT = 20;
    private static final String REG_EXP_TYPENAME = "RegExp";
    private static final ThreadLocal<Context> threadContext =
            ThreadLocal.withInitial(() -> Context.from(Env.getInstance()));

    public static Object checkCast(Object sourceVal, Type targetType) {

        List<String> errors = new ArrayList<>();
        if (checkIsType(sourceVal, targetType)) {
            return sourceVal;
        }
        Type sourceType = getType(sourceVal);
        Context cx = context();
        if (Core.containsBasicType(SemType.tryInto(cx, sourceType), ConvertibleCastMaskHolder.CONVERTIBLE_CAST_MASK) &&
                Core.containsBasicType(SemType.tryInto(cx, targetType),
                        ConvertibleCastMaskHolder.CONVERTIBLE_CAST_MASK)) {
            // We need to maintain order for these?
            if (targetType instanceof BUnionType unionType) {
                for (Type memberType : unionType.getMemberTypes()) {
                    try {
                        return TypeConverter.castValues(memberType, sourceVal);
                    } catch (Exception e) {
                        //ignore and continue
                    }
                }
            } else {
                return TypeConverter.castValues(targetType, sourceVal);
            }
        }
        throw createTypeCastError(sourceVal, targetType, errors);
    }

    public static Context context() {
        // We are pinning each context to thread. We can't use the same context with multiple type checks concurrently
        return threadContext.get();
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
        Context cx = context();
        Type sourceType = getType(sourceVal);
        if (isSubType(cx, sourceType, targetType)) {
            return true;
        }
        SemType sourceSemType = SemType.tryInto(cx, sourceType);
        return couldInherentTypeBeDifferent(sourceSemType) &&
                isSubTypeWithInherentType(cx, sourceVal, SemType.tryInto(cx, targetType));
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
        return checkIsType(sourceVal, targetType);
    }

    // This is just an optimization since shapes are not cached, when in doubt return false
    private static boolean couldInherentTypeBeDifferent(SemType type) {
        if (type instanceof TypeWithShape typeWithShape) {
            return typeWithShape.couldInherentTypeBeDifferent();
        }
        return true;
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
        Context cx = context();
        SemType shape = ShapeAnalyzer.shapeOf(cx, sourceValue).orElseThrow();
        SemType targetSemType = ShapeAnalyzer.acceptedTypeOf(cx, targetType).orElseThrow();
        if (Core.isSubType(cx, shape, NumericTypeHolder.NUMERIC_TYPE) && allowNumericConversion) {
            targetSemType = appendNumericConversionTypes(targetSemType);
        }
        return Core.isSubType(cx, shape, targetSemType);
    }

    private static SemType appendNumericConversionTypes(SemType semType) {
        SemType result = semType;
        // We can represent any int value as a float or a decimal. This is to avoid the overhead of creating
        // enumerable semtypes for them
        if (Core.containsBasicType(semType, Builder.getIntType())) {
            result = Core.union(Core.union(Builder.getDecimalType(), Builder.getFloatType()), result);
        }
        result = Core.union(result, Core.floatToInt(semType));
        result = Core.union(result, Core.floatToDecimal(semType));
        result = Core.union(result, Core.decimalToInt(semType));
        result = Core.union(result, Core.decimalToFloat(semType));
        return result;
    }

    /**
     * Check whether two types are the same.
     *
     * @param sourceType type to test
     * @param targetType type to test against
     * @return true if the two types are same; false otherwise
     */
    public static boolean isSameType(Type sourceType, Type targetType) {
        Context cx = context();
        return Core.isSameType(cx, SemType.tryInto(cx, sourceType), SemType.tryInto(cx, targetType));
    }

    public static Type getType(Object value) {
        if (value instanceof BValue bValue) {
            if (!(value instanceof BObject bObject)) {
                return bValue.getType();
            }
            return bObject.getOriginalType();
        }
        if (value == null) {
            return TYPE_NULL;
        } else if (value instanceof Number number) {
            return getNumberType(number);
        } else if (value instanceof Boolean booleanValue) {
            return BBooleanType.singletonType(booleanValue);
        }
        throw new IllegalArgumentException("unexpected value type");
    }

    private static Type getNumberType(Number number) {
        if (number instanceof Double) {
            return TYPE_FLOAT;
        }
        if (number instanceof Integer || number instanceof Byte) {
            return TYPE_BYTE;
        }
        return TYPE_INT;
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
        return isDecimalRealNumber(lhsValue) && isDecimalRealNumber(rhsValue)
                && lhsValue.decimalValue().equals(rhsValue.decimalValue());
    }

    /**
     * Checks if the given decimal number is a real number.
     *
     * @param decimalValue The decimal value being checked
     * @return True if the decimal value is a real number.
     */
    static boolean isDecimalRealNumber(DecimalValue decimalValue) {
        return decimalValue.valueKind == DecimalValueKind.ZERO || decimalValue.valueKind == DecimalValueKind.OTHER;
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

        Context cx = context();
        SemType lhsType = widenedType(cx, lhsValue);
        SemType rhsType = widenedType(cx, rhsValue);
        if (isSimpleBasicSemType(cx, lhsType)) {
            return isSimpleBasicValuesEqual(cx, lhsValue, rhsValue);
        }
        Predicate<SemType> basicTypePredicate =
                (basicType) -> Core.isSubType(cx, lhsType, basicType) && Core.isSubType(cx, rhsType, basicType);
        if (basicTypePredicate.test(Builder.getStringType())) {
            return isEqual(lhsValue, rhsValue);
        }
        if (basicTypePredicate.test(Builder.getXmlType())) {
            return isXMLValueRefEqual((XmlValue) lhsValue, (XmlValue) rhsValue);
        }
        if (basicTypePredicate.test(Builder.getHandleType())) {
            return isHandleValueRefEqual(lhsValue, rhsValue);
        }
        if (basicTypePredicate.test(Builder.getFunctionType())) {
            return isFunctionPointerEqual(getImpliedType(getType(lhsValue)), getImpliedType(getType(rhsValue)));
        }
        if (basicTypePredicate.test(Builder.getRegexType())) {
            RegExpValue lhsReg = (RegExpValue) lhsValue;
            RegExpValue rhsReg = (RegExpValue) rhsValue;
            return lhsReg.equals(rhsReg, new HashSet<>());
        }
        // Other types have storage identity so == test should have passed
        return false;
    }

    static boolean isXMLValueRefEqual(XmlValue lhsValue, XmlValue rhsValue) {
        boolean isLhsXmlSequence = lhsValue.getNodeType() == XmlNodeType.SEQUENCE;
        boolean isRhsXmlSequence = rhsValue.getNodeType() == XmlNodeType.SEQUENCE;

        if (isLhsXmlSequence && isRhsXmlSequence) {
            return isXMLSequenceRefEqual((XmlSequence) lhsValue, (XmlSequence) rhsValue);
        }
        if (isLhsXmlSequence && lhsValue.isSingleton()) {
            return ((XmlSequence) lhsValue).getChildrenList().get(0) == rhsValue;
        }
        if (isRhsXmlSequence && rhsValue.isSingleton()) {
            return ((XmlSequence) rhsValue).getChildrenList().get(0) == lhsValue;
        }
        if (lhsValue.getNodeType() != rhsValue.getNodeType()) {
            return false;
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

    private static boolean isFunctionPointerEqual(Type lhsType, Type rhsType) {
        return lhsType.getPackage().equals(rhsType.getPackage()) &&
                lhsType.getName().equals(rhsType.getName()) && rhsType.equals(lhsType);
    }

    private static boolean isSimpleBasicValuesEqual(Context cx, Object v1, Object v2) {
        SemType v1Ty = widenedType(cx, v1);
        if (!isSimpleBasicSemType(cx, v1Ty)) {
            return false;
        }

        SemType v2Ty = widenedType(cx, v2);
        if (!isSimpleBasicSemType(cx, v2Ty)) {
            return false;
        }

        if (!Core.isSameType(cx, v1Ty, v2Ty)) {
            return false;
        }

        if (Core.isSubType(cx, v1Ty, Builder.getDecimalType())) {
            return checkDecimalExactEqual((DecimalValue) v1, (DecimalValue) v2);
        }
        if (Core.isSubType(cx, v1Ty, Builder.getIntType())) {
            Number n1 = (Number) v1;
            Number n2 = (Number) v2;
            return n1.longValue() == n2.longValue();
        }
        return v1.equals(v2);
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
        Context cx = context();
        if (belongToSingleBasicTypeOrString(cx, type)) {
            return new TypedescValueImpl(new BFiniteType(value.toString(), Set.of(value), 0));
        }
        if (value instanceof BRefValue bRefValue) {
            return (TypedescValue) bRefValue.getTypedesc();
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
        if (!(describingType instanceof BAnnotatableType annotatableType)) {
            return null;
        }
        return annotatableType.getAnnotation(annotTag);
    }

    /**
     * Check whether a given type is equivalent to a target type.
     *
     * @param sourceType type to check
     * @param targetType type to compare with
     * @return flag indicating the equivalence of the two types
     */
    public static boolean checkIsType(Type sourceType, Type targetType) {
        return isSubType(context(), sourceType, targetType);
    }

    @Deprecated
    public static boolean checkIsType(Type sourceType, Type targetType, List<TypePair> unresolvedTypes) {
        return isSubType(context(), sourceType, targetType);
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

    public static boolean isNumericType(Type type) {
        Context cx = context();
        return Core.isSubType(cx, SemType.tryInto(cx, type), NumericTypeHolder.NUMERIC_TYPE);
    }

    public static boolean isByteLiteral(long longValue) {
        return (longValue >= BBYTE_MIN_VALUE && longValue <= BBYTE_MAX_VALUE);
    }

    // Private methods

    private static boolean isSubTypeWithInherentType(Context cx, Object sourceValue, SemType target) {
        return ShapeAnalyzer.inherentTypeOf(cx, sourceValue)
                .map(source -> !Core.isEmpty(cx, source) && Core.isSubType(cx, source, target))
                .orElse(false);
    }

    private static boolean isSubType(Context cx, Type source, Type target) {
        if (source instanceof CacheableTypeDescriptor sourceCacheableType &&
                target instanceof CacheableTypeDescriptor targetCacheableType) {
            return isSubTypeWithCache(cx, sourceCacheableType, targetCacheableType);
        }
        return isSubTypeInner(cx, source, target);
    }

    private static boolean isSubTypeInner(Context cx, Type source, Type target) {
        SemType sourceSemType = SemType.tryInto(cx, source);
        SemType targetSemType = SemType.tryInto(cx, target);
        return Core.isSubType(cx, sourceSemType, targetSemType);
    }

    private static boolean isSubTypeWithCache(Context cx, CacheableTypeDescriptor source,
                                              CacheableTypeDescriptor target) {
        if (!source.shouldCache() || !target.shouldCache()) {
            return isSubTypeInner(cx, source, target);
        }
        Optional<Boolean> cachedResult = source.cachedTypeCheckResult(cx, target);
        if (cachedResult.isPresent()) {
            assert cachedResult.get() == isSubTypeInner(cx, source, target);
            return cachedResult.get();
        }
        boolean result = isSubTypeInner(cx, source, target);
        source.cacheTypeCheckResult(target, result);
        return result;
    }

    private static SemType widenedType(Context cx, Object value) {
        if (value instanceof BValue bValue) {
            return bValue.widenedType(cx);
        }
        if (value == null) {
            return Builder.getNilType();
        } else if (value instanceof Double) {
            return Builder.getFloatType();
        } else if (value instanceof Number) {
            return Builder.getIntType();
        } else if (value instanceof BString) {
            return Builder.getStringType();
        } else if (value instanceof Boolean) {
            return Builder.getBooleanType();
        }
        throw new IllegalArgumentException("Unexpected object type");
    }

    public static boolean isInherentlyImmutableType(Type sourceType) {
        // readonly part is there to match to old API
        Context cx = context();
        return
                Core.isSubType(cx, SemType.tryInto(cx, sourceType),
                        InherentlyImmutableTypeHolder.INHERENTLY_IMMUTABLE_TYPE) ||
                        sourceType instanceof ReadonlyType;
    }

    // NOTE: this is not the same as selectively immutable as it stated in the spec
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
            case TypeTags.READONLY_TAG:
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
                Type constraintType = ((MapType) type).getConstrainedType();
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

        return checkValueEqual(context(), lhsValue, rhsValue, new HashSet<>(checkedValues));
    }

    private static boolean checkValueEqual(Context cx, Object lhsValue, Object rhsValue, Set<ValuePair> checkedValues) {
        SemType lhsShape = ShapeAnalyzer.inherentTypeOf(cx, lhsValue).orElseThrow();
        SemType rhsShape = ShapeAnalyzer.inherentTypeOf(cx, rhsValue).orElseThrow();
        Predicate<SemType> belongToSameBasicType = (basicType) -> Core.containsBasicType(lhsShape, basicType) &&
                Core.containsBasicType(rhsShape, basicType);
        if (belongToSameBasicType.test(Builder.getStringType()) ||
                belongToSameBasicType.test(Builder.getBooleanType())) {
            return lhsValue.equals(rhsValue);
        }
        if (belongToSameBasicType.test(Builder.getIntType())) {
            // TODO: is this correct if one of the values are bytes (shouldn't we check of unsigned etc)
            return ((Number) lhsValue).longValue() == ((Number) rhsValue).longValue();
        }
        if (belongToSameBasicType.test(Builder.getFloatType())) {
            Double lhs = (Double) lhsValue;
            Double rhs = (Double) rhsValue;
            // directly doing equals don't work with -0 and 0
            return (Double.isNaN(lhs) && Double.isNaN(rhs)) || lhs.doubleValue() == rhs.doubleValue();
        }
        if (belongToSameBasicType.test(Builder.getDecimalType())) {
            return checkDecimalEqual((DecimalValue) lhsValue, (DecimalValue) rhsValue);
        }
        if (belongToSameBasicType.test(RefValueTypeMaskHolder.REF_TYPE_MASK)) {
            RefValue lhs = (RefValue) lhsValue;
            return lhs.equals(rhsValue, checkedValues);
        }
        return false;
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

    static boolean isFiniteTypeValue(Object sourceValue, Type sourceType, Object valueSpaceItem,
                                     boolean allowNumericConversion) {
        Type valueSpaceItemType = getType(valueSpaceItem);
        int sourceTypeTag = getImpliedType(sourceType).getTag();
        int valueSpaceItemTypeTag = getImpliedType(valueSpaceItemType).getTag();
        if (valueSpaceItemTypeTag > TypeTags.DECIMAL_TAG) {
            return valueSpaceItemTypeTag == sourceTypeTag &&
                    (valueSpaceItem == sourceValue || valueSpaceItem.equals(sourceValue));
        }

        switch (sourceTypeTag) {
            case TypeTags.BYTE_TAG:
            case TypeTags.INT_TAG:
                switch (valueSpaceItemTypeTag) {
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
                switch (valueSpaceItemTypeTag) {
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
                switch (valueSpaceItemTypeTag) {
                    case TypeTags.BYTE_TAG:
                    case TypeTags.INT_TAG:
                        return checkDecimalEqual((DecimalValue) sourceValue,
                                DecimalValue.valueOf(((Number) valueSpaceItem).longValue())) && allowNumericConversion;
                    case TypeTags.FLOAT_TAG:
                        return checkDecimalEqual((DecimalValue) sourceValue,
                                DecimalValue.valueOf(((Number) valueSpaceItem).doubleValue())) &&
                                allowNumericConversion;
                    case TypeTags.DECIMAL_TAG:
                        return checkDecimalEqual((DecimalValue) sourceValue, (DecimalValue) valueSpaceItem);
                }
            default:
                if (sourceTypeTag != valueSpaceItemTypeTag) {
                    return false;
                }
                return valueSpaceItem.equals(sourceValue);
        }
    }

    public static Env getEnv() {
        return Env.getInstance();
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
        return hasFillerValue(context(), type, new ArrayList<>());
    }

    private enum FillerValueResult {
        TRUE, FALSE, MAYBE
    }

    private static FillerValueResult hasFillerValueSemType(Context cx, SemType type) {
        if (Core.containsBasicType(type, Builder.getNilType())) {
            return FillerValueResult.TRUE;
        }
        if (Integer.bitCount(type.all() | type.some()) > 1) {
            return FillerValueResult.FALSE;
        }
        if (type.some() != 0) {
            return FillerValueResult.MAYBE;
        }
        return Core.containsBasicType(type, TopTypesWithFillValueMaskHolder.TOP_TYPES_WITH_ALWAYS_FILLING) ?
                FillerValueResult.TRUE :
                FillerValueResult.FALSE;
    }

    private static boolean hasFillerValue(Context cx, Type type, List<Type> unanalyzedTypes) {
        if (type == null) {
            return true;
        }

        FillerValueResult fastResult = hasFillerValueSemType(cx, SemType.tryInto(cx, type));
        if (fastResult != FillerValueResult.MAYBE) {
            return fastResult == FillerValueResult.TRUE;
        }

        int typeTag = type.getTag();
        if (TypeTags.isXMLTypeTag(typeTag)) {
            return typeTag == TypeTags.XML_TAG || typeTag == TypeTags.XML_TEXT_TAG;
        }

        if (typeTag < TypeTags.RECORD_TYPE_TAG &&
                !(typeTag == TypeTags.CHAR_STRING_TAG || typeTag == TypeTags.NEVER_TAG)) {
            return true;
        }
        return switch (typeTag) {
            case TypeTags.STREAM_TAG,
                 TypeTags.MAP_TAG,
                 TypeTags.ANY_TAG -> true;
            case TypeTags.ARRAY_TAG -> checkFillerValue(cx, (BArrayType) type, unanalyzedTypes);
            case TypeTags.FINITE_TYPE_TAG -> checkFillerValue((BFiniteType) type);
            case TypeTags.OBJECT_TYPE_TAG,
                 TypeTags.SERVICE_TAG -> checkFillerValue(cx, (BObjectType) type);
            case TypeTags.RECORD_TYPE_TAG -> checkFillerValue(cx, (BRecordType) type, unanalyzedTypes);
            case TypeTags.TUPLE_TAG -> checkFillerValue(cx, (BTupleType) type, unanalyzedTypes);
            case TypeTags.UNION_TAG -> checkFillerValue((BUnionType) type, unanalyzedTypes);
            case TypeTags.TYPE_REFERENCED_TYPE_TAG ->
                    hasFillerValue(cx, ((BTypeReferenceType) type).getReferredType(), unanalyzedTypes);
            case TypeTags.INTERSECTION_TAG ->
                    hasFillerValue(cx, ((BIntersectionType) type).getEffectiveType(), unanalyzedTypes);
            default -> false;
        };
    }

    private static boolean checkFillerValue(Context cx, BTupleType tupleType, List<Type> unAnalyzedTypes) {
        if (unAnalyzedTypes.contains(tupleType)) {
            return true;
        }
        unAnalyzedTypes.add(tupleType);

        for (Type member : tupleType.getTupleTypes()) {
            if (!hasFillerValue(cx, member, unAnalyzedTypes)) {
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

    private static boolean checkFillerValue(Context cx, BRecordType type, List<Type> unAnalyzedTypes) {
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

    private static boolean checkFillerValue(Context cx, BArrayType type, List<Type> unAnalyzedTypes) {
        return type.getState() == ArrayState.OPEN || hasFillerValue(cx, type.getElementType(), unAnalyzedTypes);
    }

    private static boolean checkFillerValue(Context cx, BObjectType type) {
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
            return ErrorUtils.createTypeCastError(value, targetType, getErrorMessage(errors, MAX_TYPECAST_ERROR_COUNT));
        }
    }

    static boolean isSimpleBasicSemType(Context cx, SemType semType) {
        return Core.isSubType(cx, semType, SimpleBasicTypeHolder.SIMPLE_BASIC_TYPE);
    }

    static boolean belongToSingleBasicTypeOrString(Context cx, Type type) {
        SemType semType = SemType.tryInto(cx, type);
        return isSingleBasicType(semType) && Core.isSubType(cx, semType, Builder.getSimpleOrStringType()) &&
                !Core.isSubType(cx, semType, Builder.getNilType());
    }

    private static boolean isSingleBasicType(SemType semType) {
        return Integer.bitCount(semType.all()) + Integer.bitCount(semType.some()) == 1;
    }

    private TypeChecker() {
    }

    private static final class SimpleBasicTypeHolder {

        static final SemType SIMPLE_BASIC_TYPE = createSimpleBasicType();

        private static SemType createSimpleBasicType() {
            return Stream.of(Builder.getNilType(), Builder.getBooleanType(), Builder.getIntType(),
                    Builder.getFloatType(),
                    Builder.getDecimalType()).reduce(Builder.getNeverType(), Core::union);
        }
    }

    private static final class NumericTypeHolder {

        static final SemType NUMERIC_TYPE = createNumericType();

        private static SemType createNumericType() {
            return Stream.of(Builder.getIntType(), Builder.getFloatType(), Builder.getDecimalType())
                    .reduce(Builder.getNeverType(), Core::union);
        }

    }

    private static final class InherentlyImmutableTypeHolder {

        static final SemType INHERENTLY_IMMUTABLE_TYPE = createInherentlyImmutableType();

        private static SemType createInherentlyImmutableType() {
            return Stream.of(SimpleBasicTypeHolder.SIMPLE_BASIC_TYPE, Builder.getStringType(), Builder.getErrorType(),
                            Builder.getFunctionType(),
                            Builder.getTypeDescType(), Builder.getHandleType(), Builder.getXmlTextType(),
                            Builder.getXmlNeverType(),
                            Builder.getRegexType())
                    .reduce(Builder.getNeverType(), Core::union);
        }
    }

    private static final class RefValueTypeMaskHolder {

        static final SemType REF_TYPE_MASK = createRefValueMask();

        private static SemType createRefValueMask() {
            return Stream.of(Builder.getXmlType(), Builder.getMappingType(), Builder.getListType(),
                            Builder.getErrorType(),
                            Builder.getTableType(), Builder.getRegexType())
                    .reduce(Builder.getNeverType(), Core::union);
        }
    }

    private static final class ConvertibleCastMaskHolder {

        private static final SemType CONVERTIBLE_CAST_MASK = createConvertibleCastMask();

        private static SemType createConvertibleCastMask() {
            return Stream.of(Builder.getIntType(), Builder.getFloatType(), Builder.getDecimalType(),
                            Builder.getStringType(),
                            Builder.getBooleanType())
                    .reduce(Builder.getNeverType(), Core::union);
        }

    }

    private static final class TopTypesWithFillValueMaskHolder {

        static final SemType TOP_TYPES_WITH_ALWAYS_FILLING = createTopTypesWithFillerValues();

        private static SemType createTopTypesWithFillerValues() {
            return Stream.of(Builder.getIntType(), Builder.getFloatType(), Builder.getDecimalType(),
                    Builder.getStringType(),
                    Builder.getBooleanType(), Builder.getNilType(), Builder.getTableType(), Builder.getMappingType(),
                    Builder.getListType()).reduce(Builder.getNeverType(), Core::union);
        }

    }
}
