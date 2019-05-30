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
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BFiniteType;
import org.ballerinalang.jvm.types.BFunctionType;
import org.ballerinalang.jvm.types.BFutureType;
import org.ballerinalang.jvm.types.BJSONType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BTableType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.jvm.values.XMLValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Responsible for performing runtime type checking.
 *
 * @since 0.995.0
 */
@SuppressWarnings({ "rawtypes" })
public class TypeChecker {

    public static Object checkCast(Object sourceVal, BType targetType) {
        if (checkIsType(sourceVal, targetType)) {
            return sourceVal;
        }

        throw BallerinaErrors.createTypeCastError(sourceVal, targetType);
    }

    public static long anyToInt(Object sourceVal) {
        return TypeConverter.anyToInt(sourceVal, () -> BallerinaErrors.createTypeCastError(sourceVal, BTypes.typeInt));
    }

    public static double anyToFloat(Object sourceVal) {
        return TypeConverter.anyToFloat(sourceVal, () -> BallerinaErrors.createTypeCastError(sourceVal,
                                                                                             BTypes.typeFloat));
    }

    public static boolean anyToBoolean(Object sourceVal) {
        return TypeConverter.anyToBoolean(sourceVal, () -> BallerinaErrors.createTypeCastError(sourceVal,
                                                                                               BTypes.typeBoolean));
    }

    public static int anyToByte(Object sourceVal) {
        return TypeConverter.anyToByte(sourceVal, () -> BallerinaErrors.createTypeCastError(sourceVal,
                                                                                            BTypes.typeByte));
    }

    public static DecimalValue anyToDecimal(Object sourceVal) {
        return TypeConverter.anyToDecimal(sourceVal, () -> BallerinaErrors.createTypeCastError(sourceVal,
                                                                                               BTypes.typeDecimal));
    }

    /**
     * Check whether a given value belongs to the given type.
     *
     * @param sourceVal value to check the type
     * @param targetType type to be test against
     * @return true if the value belongs to the given type, false otherwise
     */
    public static boolean checkIsType(Object sourceVal, BType targetType) {
        BType sourceType = getType(sourceVal);
        if (isMutable(sourceVal, sourceType)) {
            return checkIsType(sourceType, targetType, new ArrayList<>());
        }

        return checkIsLikeType(sourceVal, targetType, new ArrayList<>());
    }

    /**
     * Check whether a given value has the same shape as the given type.
     *
     * @param sourceValue value to check the shape
     * @param targetType type to check the shape against
     * @return true if the value has the same shape as the given type; false otherwise
     */
    public static boolean checkIsLikeType(Object sourceValue, BType targetType) {
        return checkIsLikeType(sourceValue, targetType, new ArrayList<>());
    }

    /**
     * Check whether two types are the same.
     *
     * @param sourceType type to test
     * @param targetType type to test against
     * @return true if the two types are same; false otherwise
     */
    private static boolean isSameType(BType sourceType, BType targetType) {
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

        if (sourceType.getTag() == TypeTags.TABLE_TAG && targetType.getTag() == TypeTags.TABLE_TAG) {
            return targetType.equals(sourceType);
        }

        return false;
    }

    public static BType getType(Object value) {
        if (value == null) {
            return BTypes.typeNull;
        } else if (value instanceof Long) {
            return BTypes.typeInt;
        } else if (value instanceof Double) {
            return BTypes.typeFloat;
        } else if (value instanceof DecimalValue) {
            return BTypes.typeDecimal;
        } else if (value instanceof String) {
            return BTypes.typeString;
        } else if (value instanceof Boolean) {
            return BTypes.typeBoolean;
        } else if (value instanceof Byte || value instanceof Integer) {
            return BTypes.typeByte;
        } else {
            return ((RefValue) value).getType();
        }
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

        if (isSimpleBasicType(getType(lhsValue)) && isSimpleBasicType(getType(rhsValue))) {
            return isEqual(lhsValue, rhsValue);
        }

        return false;
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
        return new TypedescValue(type);
    }

    // Private methods

    private static boolean checkIsType(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
        // First check whether both types are the same.
        if (sourceType == targetType || sourceType.equals(targetType)) {
            return true;
        }

        switch (targetType.getTag()) {
            case TypeTags.BYTE_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.STRING_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.NULL_TAG:
            case TypeTags.XML_TAG:
            case TypeTags.SERVICE_TAG:
                if (sourceType.getTag() == TypeTags.FINITE_TYPE_TAG) {
                    return ((BFiniteType) sourceType).valueSpace.stream()
                                                                .allMatch(bValue -> checkIsType(bValue, targetType));
                }
                return sourceType.getTag() == targetType.getTag();
            case TypeTags.INT_TAG:
                return sourceType.getTag() == TypeTags.BYTE_TAG || sourceType.getTag() == TypeTags.INT_TAG;
            case TypeTags.MAP_TAG:
                return checkIsMapType(sourceType, (BMapType) targetType, unresolvedTypes);
            case TypeTags.TABLE_TAG:
                return checkIsTableType(sourceType, (BTableType) targetType, unresolvedTypes);
            case TypeTags.JSON_TAG:
                return checkIsJSONType(sourceType, (BJSONType) targetType, unresolvedTypes);
            case TypeTags.RECORD_TYPE_TAG:
                return checkIsRecordType(sourceType, (BRecordType) targetType, unresolvedTypes);
            case TypeTags.FUNCTION_POINTER_TAG:
                return checkIsFunctionType(sourceType, (BFunctionType) targetType);
            case TypeTags.ARRAY_TAG:
                return checkIsArrayType(sourceType, (BArrayType) targetType, unresolvedTypes);
            case TypeTags.TUPLE_TAG:
                return checkIsTupleType(sourceType, (BTupleType) targetType, unresolvedTypes);
            case TypeTags.UNION_TAG:
                return ((BUnionType) targetType).getMemberTypes().stream()
                        .anyMatch(type -> checkIsType(sourceType, type, unresolvedTypes));
            case TypeTags.ANY_TAG:
                return checkIsAnyType(sourceType);
            case TypeTags.ANYDATA_TAG:
                return isAnydata(sourceType);
            case TypeTags.OBJECT_TYPE_TAG:
                return checkObjectEquivalency(sourceType, (BObjectType) targetType, unresolvedTypes);
            case TypeTags.FINITE_TYPE_TAG:
                return checkIsFiniteType(sourceType, (BFiniteType) targetType, unresolvedTypes);
            case TypeTags.FUTURE_TAG:
                return checkIsFutureType(sourceType, (BFutureType) targetType, unresolvedTypes);
            default:
                return false;
        }
    }

    private static boolean checkIsMapType(BType sourceType, BMapType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.MAP_TAG) {
            return false;
        }
        return checkContraints(((BMapType) sourceType).getConstrainedType(), targetType.getConstrainedType(),
                unresolvedTypes);
    }

    private static boolean checkIsTableType(BType sourceType, BTableType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.TABLE_TAG) {
            return false;
        }
        return checkContraints(((BTableType) sourceType).getConstrainedType(), targetType.getConstrainedType(),
                               unresolvedTypes);
    }

    private static boolean checkIsJSONType(BType sourceType, BJSONType targetType, List<TypePair> unresolvedTypes) {
        switch (sourceType.getTag()) {
            case TypeTags.STRING_TAG:
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.NULL_TAG:
            case TypeTags.JSON_TAG:
                return true;
            case TypeTags.ARRAY_TAG:
                // Element type of the array should be 'is type' JSON
                return checkIsType(((BArrayType) sourceType).getElementType(), targetType, unresolvedTypes);
            case TypeTags.MAP_TAG:
                return checkIsType(((BMapType) sourceType).getConstrainedType(), targetType, unresolvedTypes);
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

            // If the LHS field is a required one, there has to be a corresponding required field in the RHS record.
            if (!Flags.isFlagOn(targetField.flags, Flags.OPTIONAL) &&
                    (sourceField == null || Flags.isFlagOn(sourceField.flags, Flags.OPTIONAL))) {
                return false;
            }

            if (sourceField == null || !checkIsType(sourceField.type, targetField.type, unresolvedTypes)) {
                return false;
            }
        }

        // If there are fields remaining in the source record, first check if it's a closed record. Closed records
        // should only have the fields specified by its type.
        if (targetType.sealed) {
            return targetFieldNames.containsAll(sourceFields.keySet());
        }

        // If it's an open record, check if they are compatible with the rest field of the target type.
        return sourceFields.values().stream().filter(field -> !targetFieldNames.contains(field.name))
                .allMatch(field -> checkIsType(field.getFieldType(), targetType.restFieldType, unresolvedTypes));
    }

    private static boolean checkIsArrayType(BType sourceType, BArrayType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() == TypeTags.UNION_TAG) {
            return ((BUnionType) sourceType).getMemberTypes().stream()
                    .allMatch(memberType -> {
                        return checkIsArrayType(memberType, targetType, unresolvedTypes);
                    });
        }

        if (sourceType.getTag() != TypeTags.ARRAY_TAG) {
            return false;
        }

        BArrayType sourceArrayType = (BArrayType) sourceType;

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

        return checkIsType(sourceArrayType.getElementType(), targetType.getElementType(), unresolvedTypes);
    }

    private static boolean checkIsTupleType(BType sourceType, BTupleType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.TUPLE_TAG) {
            return false;
        }

        List<BType> sourceTypes = ((BTupleType) sourceType).getTupleTypes();
        List<BType> targetTypes = targetType.getTupleTypes();
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
                return ((BUnionType) sourceType).getMemberTypes().stream()
                                                .allMatch(TypeChecker::checkIsAnyType);
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
        return checkContraints(((BFutureType) sourceType).getConstrainedType(), targetType.getConstrainedType(),
                unresolvedTypes);
    }

    private static boolean checkObjectEquivalency(BType sourceType, BObjectType targetType,
                                                  List<TypePair> unresolvedTypes) {
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

        // Adjust the number of the attached functions of the lhs struct based on
        //  the availability of the initializer function.
        int targetAttachedFunctionCount = targetType.initializer != null ? targetFuncs.length - 1 : targetFuncs.length;
        int sourceAttachedFunctionCount =
                sourceObjectType.initializer != null ? sourceFuncs.length - 1 : sourceFuncs.length;

        if (targetFields.size() > sourceFields.size() || targetAttachedFunctionCount > sourceAttachedFunctionCount) {
            return false;
        }

        for (BField lhsField : targetFields.values()) {
            BField rhsField = sourceFields.get(lhsField.name);
            if (rhsField == null ||
                    !isInSameVisibilityRegion(Optional.ofNullable(lhsField.type.getPackagePath()).orElse(""),
                                              Optional.ofNullable(rhsField.type.getPackagePath()).orElse(""),
                                              lhsField.flags, rhsField.flags) ||
                    !checkIsType(rhsField.type, lhsField.type, new ArrayList<>())) {
                return false;
            }
        }

        for (AttachedFunction lhsFunc : targetFuncs) {
            if (lhsFunc == targetType.initializer || lhsFunc == targetType.defaultsValuesInitFunc) {
                continue;
            }

            AttachedFunction rhsFunc = getMatchingInvokableType(sourceFuncs, lhsFunc, unresolvedTypes);
            if (rhsFunc == null ||
                    !isInSameVisibilityRegion(Optional.ofNullable(lhsFunc.type.getPackagePath()).orElse(""),
                                              Optional.ofNullable(rhsFunc.type.getPackagePath()).orElse(""),
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
            if (!isSameType(source.paramTypes[i], targetType.paramTypes[i])) {
                return false;
            }
        }

        return isSameType(source.retType, targetType.retType);
    }

    private static boolean checkContraints(BType sourceConstraint, BType targetConstraint,
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
        if (value == null || sourceType.getTag() < TypeTags.JSON_TAG ||
                sourceType.getTag() == TypeTags.FINITE_TYPE_TAG) {
            return false;
        }

        return !((RefValue) value).isFrozen();
    }

    private static boolean checkArrayEquivalent(BType actualType, BType expType) {
        if (expType.getTag() == TypeTags.ARRAY_TAG && actualType.getTag() == TypeTags.ARRAY_TAG) {
            // Both types are array types
            BArrayType lhrArrayType = (BArrayType) expType;
            BArrayType rhsArrayType = (BArrayType) actualType;
            return checkArrayEquivalent(lhrArrayType.getElementType(), rhsArrayType.getElementType());
        }
        // Now one or both types are not array types and they have to be equal
        if (expType == actualType) {
            return true;
        }
        return false;
    }

    public static boolean checkIsLikeType(Object sourceValue, BType targetType, List<TypeValuePair> unresolvedValues) {
        BType sourceType = getType(sourceValue);
        if (checkIsType(sourceType, targetType, new ArrayList<>())) {
            return true;
        }

        switch (targetType.getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
                return checkIsLikeRecordType(sourceValue, (BRecordType) targetType, unresolvedValues);
            case TypeTags.JSON_TAG:
                return checkIsLikeJSONType(sourceValue, sourceType, (BJSONType) targetType, unresolvedValues);
            case TypeTags.MAP_TAG:
                return checkIsLikeMapType(sourceValue, (BMapType) targetType, unresolvedValues);
            case TypeTags.TABLE_TAG:
                return checkIsLikeTableType(sourceValue, (BTableType) targetType, unresolvedValues);
            case TypeTags.ARRAY_TAG:
                return checkIsLikeArrayType(sourceValue, (BArrayType) targetType, unresolvedValues);
            case TypeTags.TUPLE_TAG:
                return checkIsLikeTupleType(sourceValue, (BTupleType) targetType, unresolvedValues);
            case TypeTags.ANYDATA_TAG:
                return checkIsLikeAnydataType(sourceValue, sourceType, unresolvedValues);
            case TypeTags.FINITE_TYPE_TAG:
                return checkFiniteTypeAssignable(sourceValue, sourceType, (BFiniteType) targetType);
            case TypeTags.UNION_TAG:
                return ((BUnionType) targetType).getMemberTypes().stream()
                        .anyMatch(type -> checkIsLikeType(sourceValue, type, unresolvedValues));
            default:
                return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean checkIsLikeAnydataType(Object sourceValue, BType sourceType,
                                                  List<TypeValuePair> unresolvedValues) {
        switch (sourceType.getTag()) {
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.JSON_TAG:
            case TypeTags.MAP_TAG:
                return ((MapValueImpl) sourceValue).values().stream()
                        .allMatch(value -> checkIsLikeType(value, BTypes.typeAnydata, unresolvedValues));
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
                        return Arrays.stream(arr.getValues())
                                .allMatch(value -> checkIsLikeType(value, BTypes.typeAnydata, unresolvedValues));
                }
            case TypeTags.TUPLE_TAG:
                return Arrays.stream(((ArrayValue) sourceValue).getValues())
                        .allMatch(value -> checkIsLikeType(value, BTypes.typeAnydata, unresolvedValues));
            case TypeTags.ANYDATA_TAG:
                return true;
            case TypeTags.FINITE_TYPE_TAG:
            case TypeTags.UNION_TAG:
                return checkIsLikeType(sourceValue, BTypes.typeAnydata, unresolvedValues);
            default:
                return false;
        }
    }

    private static boolean checkIsLikeTupleType(Object sourceValue, BTupleType targetType,
                                                List<TypeValuePair> unresolvedValues) {
        if (!(sourceValue instanceof ArrayValue)) {
            return false;
        }

        ArrayValue source = (ArrayValue) sourceValue;
        if (source.size() != targetType.getTupleTypes().size()) {
            return false;
        }

        if (BTypes.isValueType(source.elementType)) {
            int bound = source.size();
            for (int i = 0; i < bound; i++) {
                if (!checkIsType(source.elementType, targetType.getTupleTypes().get(i), new ArrayList<>())) {
                    return false;
                }
            }
            return true;
        }

        int bound = source.size();
        for (int i = 0; i < bound; i++) {
            if (!checkIsLikeType(source.getRefValue(i), targetType.getTupleTypes().get(i), unresolvedValues)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsLikeArrayType(Object sourceValue, BArrayType targetType,
                                                List<TypeValuePair> unresolvedValues) {
        if (!(sourceValue instanceof ArrayValue)) {
            return false;
        }

        ArrayValue source = (ArrayValue) sourceValue;
        if (BTypes.isValueType(source.elementType)) {
            return checkIsType(source.elementType, targetType.getElementType(), new ArrayList<>());
        }

        BType arrayElementType = targetType.getElementType();
        Object[] arrayValues = source.getValues();
        for (int i = 0; i < ((ArrayValue) sourceValue).size(); i++) {
            if (!checkIsLikeType(arrayValues[i], arrayElementType, unresolvedValues)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsLikeMapType(Object sourceValue, BMapType targetType,
                                              List<TypeValuePair> unresolvedValues) {
        if (!(sourceValue instanceof MapValueImpl)) {
            return false;
        }

        for (Object mapEntry : ((MapValueImpl) sourceValue).values()) {
            if (!checkIsLikeType(mapEntry, targetType.getConstrainedType(), unresolvedValues)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsLikeTableType(Object sourceValue, BTableType targetType,
                                              List<TypeValuePair> unresolvedValues) {
        if (!(sourceValue instanceof TableValue)) {
            return false;
        }

        BTableType tableType = (BTableType) ((TableValue) sourceValue).getType();

        return tableType.getConstrainedType() == targetType.getConstrainedType();
    }

    private static boolean checkIsLikeJSONType(Object sourceValue, BType sourceType, BJSONType targetType,
                                               List<TypeValuePair> unresolvedValues) {
        if (sourceType.getTag() == TypeTags.ARRAY_TAG) {
            ArrayValue source = (ArrayValue) sourceValue;
            if (BTypes.isValueType(source.elementType)) {
                return checkIsType(source.elementType, targetType, new ArrayList<>());
            }

            Object[] arrayValues = source.getValues();
            for (int i = 0; i < ((ArrayValue) sourceValue).size(); i++) {
                if (!checkIsLikeType(arrayValues[i], targetType, unresolvedValues)) {
                    return false;
                }
            }
        } else if (sourceType.getTag() == TypeTags.MAP_TAG) {
            for (Object value : ((MapValueImpl) sourceValue).values()) {
                if (!checkIsLikeType(value, targetType, unresolvedValues)) {
                    return false;
                }
            }
        } else if (sourceType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            TypeValuePair typeValuePair = new TypeValuePair(sourceValue, targetType);
            if (unresolvedValues.contains(typeValuePair)) {
                return true;
            }
            unresolvedValues.add(typeValuePair);
            for (Object object : ((MapValueImpl) sourceValue).values()) {
                if (!checkIsLikeType(object, targetType, unresolvedValues)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkIsLikeRecordType(Object sourceValue, BRecordType targetType,
                                                 List<TypeValuePair> unresolvedValues) {
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
            String fieldName = targetTypeEntry.getKey().toString();

            if (!(((MapValueImpl) sourceValue).containsKey(fieldName)) &&
                    !Flags.isFlagOn(targetType.getFields().get(fieldName).flags, Flags.OPTIONAL)) {
                return false;
            }
        }

        for (Object object : ((MapValueImpl) sourceValue).entrySet()) {
            Map.Entry valueEntry = (Map.Entry) object;
            String fieldName = valueEntry.getKey().toString();

            if (targetTypeField.containsKey(fieldName)) {
                if (!checkIsLikeType((valueEntry.getValue()), targetTypeField.get(fieldName), unresolvedValues)) {
                    return false;
                }
            } else {
                if (!targetType.sealed) {
                    if (!checkIsLikeType((valueEntry.getValue()), restFieldType, unresolvedValues)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkFiniteTypeAssignable(Object bRefTypeValue, BType sourceType, BFiniteType lhsType) {
        if (bRefTypeValue == null) {
            // we should not reach here
            return false;
        }

        for (Object valueSpaceItem : lhsType.valueSpace) {
            if (getType(valueSpaceItem).getTag() == sourceType.getTag() && valueSpaceItem.equals(bRefTypeValue)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAnydata(BType type) {
        return isAnydata(type, new HashSet<>());
    }

    private static boolean isAnydata(BType type, Set<BType> unresolvedTypes) {
        if (type.getTag() <= TypeTags.ANYDATA_TAG) {
            return true;
        }

        switch (type.getTag()) {
            case TypeTags.MAP_TAG:
                return isPureType(((BMapType) type).getConstrainedType(), unresolvedTypes);
            case TypeTags.RECORD_TYPE_TAG:
                if (unresolvedTypes.contains(type)) {
                    return true;
                }
                unresolvedTypes.add(type);
                BRecordType recordType = (BRecordType) type;
                List<BType> fieldTypes = recordType.getFields().values().stream()
                                                   .map(BField::getFieldType)
                                                   .collect(Collectors.toList());
                return isPureType(fieldTypes, unresolvedTypes) &&
                        (recordType.sealed || isPureType(recordType.restFieldType, unresolvedTypes));
            case TypeTags.UNION_TAG:
                return isAnydata(((BUnionType) type).getMemberTypes(), unresolvedTypes);
            case TypeTags.TUPLE_TAG:
                return isPureType(((BTupleType) type).getTupleTypes(), unresolvedTypes);
            case TypeTags.ARRAY_TAG:
                return isPureType(((BArrayType) type).getElementType(), unresolvedTypes);
            case TypeTags.FINITE_TYPE_TAG:
                Set<BType> valSpaceTypes = ((BFiniteType) type).valueSpace.stream()
                        .map(TypeChecker::getType)
                        .collect(Collectors.toSet());
                return isAnydata(valSpaceTypes, unresolvedTypes);
            default:
                return false;
        }
    }

    private static boolean isAnydata(Collection<BType> types, Set<BType> unresolvedTypes) {
        return types.stream().allMatch(bType -> isAnydata(bType, unresolvedTypes));
    }

    private static boolean isPureType(BType type, Set<BType> unresolvedTypes) {
        if (type.getTag() == TypeTags.UNION_TAG) {
            return ((BUnionType) type).getMemberTypes().stream()
                                      .allMatch(memType -> isPureType(memType, unresolvedTypes));
        }

        return type.getTag() == TypeTags.ERROR_TAG || isAnydata(type, unresolvedTypes);
    }

    private static boolean isPureType(Collection<BType> types, Set<BType> unresolvedTypes) {
        return types.stream().allMatch(bType -> isPureType(bType, unresolvedTypes));
    }

    private static boolean isSimpleBasicType(BType type) {
        return type.getTag() < TypeTags.JSON_TAG;
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
                if (rhsValTypeTag != TypeTags.BYTE_TAG && rhsValTypeTag != TypeTags.INT_TAG) {
                    return false;
                }
                return lhsValue.equals(((Number) rhsValue).longValue());
            case TypeTags.BYTE_TAG:
                if (rhsValTypeTag != TypeTags.BYTE_TAG && rhsValTypeTag != TypeTags.INT_TAG) {
                    return false;
                }
                return ((Number) lhsValue).byteValue() == ((Number) rhsValue).byteValue();
            case TypeTags.XML_TAG:
                return XMLFactory.isEqual((XMLValue) lhsValue, (XMLValue) rhsValue);
            case TypeTags.TABLE_TAG:
                // TODO: 10/8/18
                break;
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
            if (!isEqual(lhsList.getValue(i), rhsList.getValue(i), checkedValues)) {
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

        Iterator<Map.Entry<String, Object>> mapIterator = lhsMap.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry<String, Object> lhsMapEntry = mapIterator.next();
            if (!isEqual(lhsMapEntry.getValue(), rhsMap.get(lhsMapEntry.getKey()), checkedValues)) {
                return false;
            }
        }
        return true;
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

        return isEqual(lhsError.getReason(), rhsError.getReason(), checkedValues) &&
                isEqual((MapValueImpl) lhsError.getDetails(), (MapValueImpl) rhsError.getDetails(), checkedValues);
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
}
