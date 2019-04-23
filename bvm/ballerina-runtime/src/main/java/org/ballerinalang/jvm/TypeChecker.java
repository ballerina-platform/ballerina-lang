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
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.util.exceptions.BLangRuntimeException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.RefValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        throw getTypeCastError(sourceVal, targetType);
    }

    public static long anyToInt(Object sourceVal) {
        if (sourceVal instanceof Long) {
            return (Long) sourceVal;
        } else if (sourceVal instanceof Double) {
            return ((Double) sourceVal).longValue();
        } else {
            throw getTypeCastError(sourceVal, BTypes.typeInt);
        }
    }

    public static double anyToFloat(Object sourceVal) {
        if (sourceVal instanceof Long) {
            return ((Long) sourceVal).doubleValue();
        } else if (sourceVal instanceof Double) {
            return (Double) sourceVal;
        } else {
            throw getTypeCastError(sourceVal, BTypes.typeFloat);
        }
    }

    public static boolean anyToBoolean(Object sourceVal) {
        if (sourceVal instanceof Boolean) {
            return (Boolean) sourceVal;
        }

        throw getTypeCastError(sourceVal, BTypes.typeBoolean);
    }

    public static byte anyToByte(Object sourceVal) {
        if (sourceVal instanceof Byte) {
            return (Byte) sourceVal;
        }

        throw getTypeCastError(sourceVal, BTypes.typeByte);
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
            return checkIsType(getType(sourceVal), targetType, new ArrayList<>());
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

        return false;
    }

    /**
     * This method is for use as the first check in checking for cast/assignability for two types.
     * Checks whether the source type is the same as the target type or if the target type is any type, and if true
     * the return value would be true.
     *
     * @param rhsType   the source type - the type (of the value) being cast/assigned
     * @param lhsType   the target type against which cast/assignability is checked
     * @return          true if the lhsType is any or is the same as rhsType
     */
    private static boolean isSameOrAnyType(BType rhsType, BType lhsType) {
        return (lhsType.getTag() == TypeTags.ANY_TAG && rhsType.getTag() != TypeTags.ERROR_TAG) || 
                rhsType.equals(lhsType);
    }

    public static BType getType(Object value) {
        if (value == null) {
            return BTypes.typeNull;
        } else if (value instanceof Long) {
            return BTypes.typeInt;
        } else if (value instanceof Double) {
            return BTypes.typeFloat;
        } else if (value instanceof BigDecimal) {
            return BTypes.typeDecimal;
        } else if (value instanceof String) {
            return BTypes.typeString;
        } else if (value instanceof Boolean) {
            return BTypes.typeBoolean;
        } else if (value instanceof Byte) {
            return BTypes.typeByte;
        } else {
            return ((RefValue) value).getType();
        }
    }

    // Private methods

    private static boolean checkIsType(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
        // First check whether both types are the same.
        if (sourceType == targetType || sourceType.equals(targetType)) {
            return true;
        }

        switch (targetType.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.STRING_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.BYTE_TAG:
            case TypeTags.NULL_TAG:
            case TypeTags.XML_TAG:
                if (sourceType.getTag() == TypeTags.FINITE_TYPE_TAG) {
                    return ((BFiniteType) sourceType).valueSpace.stream()
                                                                .allMatch(bValue -> checkIsType(bValue, targetType));
                }
                return sourceType.getTag() == targetType.getTag();
            case TypeTags.MAP_TAG:
                return checkIsMapType(sourceType, (BMapType) targetType, unresolvedTypes);
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
                return checkIsUnionType(sourceType, (BUnionType) targetType, unresolvedTypes);
            case TypeTags.ANY_TAG:
                return checkIsAnyType(sourceType);
            case TypeTags.ANYDATA_TAG:
            case TypeTags.OBJECT_TYPE_TAG:
                return isAssignable(sourceType, targetType, unresolvedTypes);
            case TypeTags.FINITE_TYPE_TAG:
                return checkIsFiniteType(sourceType, (BFiniteType) targetType, unresolvedTypes);
            case TypeTags.FUTURE_TAG:
                return checkIsFutureType(sourceType, (BFutureType) targetType, unresolvedTypes);
            default:
                return false;
        }
    }

    private static boolean isAssignable(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
        if (isSameOrAnyType(sourceType, targetType)) {
            return true;
        }

        if (targetType.getTag() == TypeTags.UNION_TAG) {
            return checkUnionAssignable(sourceType, targetType, unresolvedTypes);
        }

        // TODO: 6/26/18 complete impl. for JSON assignable
        if (targetType.getTag() == TypeTags.JSON_TAG && sourceType.getTag() == TypeTags.JSON_TAG) {
            return true;
        }

        if (targetType.getTag() == TypeTags.ARRAY_TAG && sourceType.getTag() == TypeTags.ARRAY_TAG) {
            if (((BArrayType) sourceType).getState() == ArrayState.CLOSED_SEALED
                    && ((BArrayType) targetType).getState() == ArrayState.CLOSED_SEALED
                    && ((BArrayType) sourceType).getSize() != ((BArrayType) targetType).getSize()) {
                return false;
            }
            return checkArrayCast(((BArrayType) sourceType).getElementType(),
                                  ((BArrayType) targetType).getElementType(), unresolvedTypes);
        }

        if (sourceType.getTag() == TypeTags.TUPLE_TAG && targetType.getTag() == TypeTags.TUPLE_TAG) {
            return checkTupleAssignable(sourceType, targetType, unresolvedTypes);
        }

        return checkCastByType(sourceType, targetType, unresolvedTypes);
    }

    private static boolean checkUnionAssignable(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() == TypeTags.UNION_TAG) {
            for (BType sourceMemberType : ((BUnionType) sourceType).getMemberTypes()) {
                if (!checkUnionAssignable(sourceMemberType, targetType, unresolvedTypes)) {
                    return false;
                }
            }
            return true;
        } else {
            BUnionType targetUnionType = (BUnionType) targetType;
            for (BType memberType : targetUnionType.getMemberTypes()) {
                if (isAssignable(sourceType, memberType, unresolvedTypes)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static boolean checkTupleAssignable(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
        List<BType> targetTupleTypes = ((BTupleType) targetType).getTupleTypes();
        List<BType> sourceTupleTypes = ((BTupleType) sourceType).getTupleTypes();

        if (sourceTupleTypes.size() != targetTupleTypes.size()) {
            return false;
        }
        for (int i = 0; i < sourceTupleTypes.size(); i++) {
            if (!isAssignable(sourceTupleTypes.get(i), targetTupleTypes.get(i), unresolvedTypes)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIsMapType(BType sourceType, BMapType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() != TypeTags.MAP_TAG) {
            return false;
        }
        return checkContraints(((BMapType) sourceType).getConstrainedType(), targetType.getConstrainedType(),
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
        if (sourceType.getTag() != TypeTags.ARRAY_TAG) {
            return false;
        }

        BArrayType sourceArrayType = (BArrayType) sourceType;
        if (sourceArrayType.getState() != targetType.getState() || sourceArrayType.getSize() != targetType.getSize()) {
            return false;
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

    private static boolean checkIsUnionType(BType sourceType, BUnionType targetType, List<TypePair> unresolvedTypes) {
        if (sourceType.getTag() == TypeTags.UNION_TAG) {
            return ((BUnionType) sourceType).getMemberTypes().stream()
                                            .allMatch(type -> checkIsType(type, targetType, unresolvedTypes));
        } else if (sourceType.getTag() == TypeTags.FINITE_TYPE_TAG) {
            return ((BFiniteType) sourceType).valueSpace.stream()
                                                        .allMatch(bValue -> checkIsType(bValue, targetType));
        }
        return targetType.getMemberTypes().stream()
                         .anyMatch(type -> checkIsType(sourceType, type, unresolvedTypes));
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

        // Both structs should be public or private.
        // Get the XOR of both flags(masks)
        // If both are public, then public bit should be 0;
        // If both are private, then public bit should be 0;
        // The public bit is on means, one is public, and the other one is private.
        BObjectType sourceObjectType = (BObjectType) sourceType;
        if (Flags.isFlagOn(targetType.flags ^ sourceObjectType.flags, Flags.PUBLIC)) {
            return false;
        }

        // If both structs are private, they should be in the same package.
        if (!Flags.isFlagOn(targetType.flags, Flags.PUBLIC) &&
                !sourceObjectType.getPackagePath().equals(targetType.getPackagePath())) {
            return false;
        }

        // Adjust the number of the attached functions of the lhs struct based on
        // the availability of the initializer function.
        int lhsAttachedFunctionCount = targetType.initializer != null ? targetType.getAttachedFunctions().length - 1
                : targetType.getAttachedFunctions().length;

        if (targetType.getFields().size() > sourceObjectType.getFields().size() ||
                lhsAttachedFunctionCount > sourceObjectType.getAttachedFunctions().length) {
            return false;
        }

        return !Flags.isFlagOn(targetType.flags, Flags.PUBLIC) &&
                sourceObjectType.getPackagePath().equals(targetType.getPackagePath())
                        ? checkPrivateObjectsEquivalency(targetType, sourceObjectType, unresolvedTypes)
                        : checkPublicObjectsEquivalency(targetType, sourceObjectType, unresolvedTypes);
    }

    private static boolean checkRecordEquivalency(BRecordType lhsType, BRecordType rhsType,
                                                  List<TypePair> unresolvedTypes) {
        // Both records should be public or private.
        // Get the XOR of both flags(masks)
        // If both are public, then public bit should be 0;
        // If both are private, then public bit should be 0;
        // The public bit is on means, one is public, and the other one is private.
        if (Flags.isFlagOn(lhsType.flags ^ rhsType.flags, Flags.PUBLIC)) {
            return false;
        }

        // If both records are private, they should be in the same package.
        if (!Flags.isFlagOn(lhsType.flags, Flags.PUBLIC) &&
                !rhsType.getPackagePath().equals(lhsType.getPackagePath())) {
            return false;
        }

        // Cannot assign open records to closed record types
        if (lhsType.sealed && !rhsType.sealed) {
            return false;
        }

        // The rest field types should match if they are open records
        if (!rhsType.sealed && !isAssignable(rhsType.restFieldType, lhsType.restFieldType, unresolvedTypes)) {
            return false;
        }

        return checkFieldEquivalency(lhsType, rhsType, unresolvedTypes);
    }

    private static boolean checkPrivateObjectsEquivalency(BObjectType lhsType, BObjectType rhsType,
                                                          List<TypePair> unresolvedTypes) {
        Map<String, BField> rhsFields = rhsType.getFields();
        for (Map.Entry<String, BField> lhsFieldEntry : lhsType.getFields().entrySet()) {
            BField rhsField = rhsFields.get(lhsFieldEntry.getKey());
            if (rhsField == null || !isSameType(rhsField.type, lhsFieldEntry.getValue().type)) {
                return false;
            }
        }

        AttachedFunction[] lhsFuncs = lhsType.getAttachedFunctions();
        AttachedFunction[] rhsFuncs = rhsType.getAttachedFunctions();
        for (AttachedFunction lhsFunc : lhsFuncs) {
            if (lhsFunc == lhsType.initializer || lhsFunc == lhsType.defaultsValuesInitFunc) {
                continue;
            }

            AttachedFunction rhsFunc = getMatchingInvokableType(rhsFuncs, lhsFunc, unresolvedTypes);
            if (rhsFunc == null) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkPublicObjectsEquivalency(BObjectType lhsType, BObjectType rhsType,
                                                         List<TypePair> unresolvedTypes) {
        // Check the whether there is any private fields in RHS type
        if (rhsType.getFields().values().stream().anyMatch(field -> !Flags.isFlagOn(field.flags, Flags.PUBLIC))) {
            return false;
        }

        Map<String, BField> rhsFields = rhsType.getFields();
        for (Map.Entry<String, BField> lhsFieldEntry : lhsType.getFields().entrySet()) {
            BField rhsField = rhsFields.get(lhsFieldEntry.getKey());
            if (rhsField == null || !Flags.isFlagOn(lhsFieldEntry.getValue().flags, Flags.PUBLIC) ||
                    !isSameType(rhsField.type, lhsFieldEntry.getValue().type)) {
                return false;
            }
        }

        AttachedFunction[] lhsFuncs = lhsType.getAttachedFunctions();
        AttachedFunction[] rhsFuncs = rhsType.getAttachedFunctions();
        for (AttachedFunction lhsFunc : lhsFuncs) {
            if (lhsFunc == lhsType.initializer || lhsFunc == lhsType.defaultsValuesInitFunc) {
                continue;
            }

            if (!Flags.isFlagOn(lhsFunc.flags, Flags.PUBLIC)) {
                return false;
            }

            AttachedFunction rhsFunc = getMatchingInvokableType(rhsFuncs, lhsFunc, unresolvedTypes);
            if (rhsFunc == null || !Flags.isFlagOn(rhsFunc.flags, Flags.PUBLIC)) {
                return false;
            }
        }

        // Check for private attached function in RHS type
        for (AttachedFunction rhsFunc : rhsFuncs) {
            if (!Flags.isFlagOn(rhsFunc.flags, Flags.PUBLIC)) {
                return false;
            }
        }

        return true;
    }

    private static boolean checkFieldEquivalency(BRecordType lhsType, BRecordType rhsType,
                                                 List<TypePair> unresolvedTypes) {
        Map<String, BField> rhsFields = rhsType.getFields();
        Set<String> lhsFieldNames = lhsType.getFields().keySet();

        for (BField lhsField : lhsType.getFields().values()) {
            BField rhsField = rhsFields.get(lhsField.getFieldName());

            // If the LHS field is a required one, there has to be a corresponding required field in the RHS record.
            if (!Flags.isFlagOn(lhsField.flags, Flags.OPTIONAL)
                    && (rhsField == null || Flags.isFlagOn(rhsField.flags, Flags.OPTIONAL))) {
                return false;
            }

            if (rhsField == null || !isAssignable(rhsField.getFieldType(), lhsField.getFieldType(), unresolvedTypes)) {
                return false;
            }
        }

        if (lhsType.sealed) {
            return lhsFieldNames.containsAll(rhsFields.keySet());
        }

        return rhsFields.values().stream()
                        .filter(field -> !lhsFieldNames.contains(field.getFieldName()))
                        .allMatch(field -> isAssignable(field.getFieldType(), lhsType.restFieldType, unresolvedTypes));
    }

    private static AttachedFunction getMatchingInvokableType(AttachedFunction[] rhsFuncs, AttachedFunction lhsFunc,
                                                             List<TypePair> unresolvedTypes) {
        return Arrays.stream(rhsFuncs).filter(rhsFunc -> lhsFunc.funcName.equals(rhsFunc.funcName))
                .filter(rhsFunc -> checkFunctionTypeEqualityForObjectType(rhsFunc.type, lhsFunc.type, unresolvedTypes))
                .findFirst().orElse(null);
    }

    private static boolean checkFunctionTypeEqualityForObjectType(BFunctionType source, BFunctionType target,
                                                                  List<TypePair> unresolvedTypes) {
        if (source.paramTypes.length != target.paramTypes.length) {
            return false;
        }

        for (int i = 0; i < source.paramTypes.length; i++) {
            if (!checkIsType(source.paramTypes[i], target.paramTypes[i], unresolvedTypes)) {
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

    private static boolean checkCastByType(BType rhsType, BType lhsType, List<TypePair> unresolvedTypes) {
        if (rhsType.getTag() == TypeTags.INT_TAG &&
                (lhsType.getTag() == TypeTags.FLOAT_TAG || lhsType.getTag() == TypeTags.DECIMAL_TAG)) {
            return true;
        } else if (rhsType.getTag() == TypeTags.FLOAT_TAG && lhsType.getTag() == TypeTags.DECIMAL_TAG) {
            return true;
        } else if (rhsType.getTag() == TypeTags.BYTE_TAG && lhsType.getTag() == TypeTags.INT_TAG) {
            return true;
        }

        if (lhsType.getTag() == TypeTags.JSON_TAG) {
            switch (rhsType.getTag()) {
                case TypeTags.INT_TAG:
                case TypeTags.FLOAT_TAG:
                case TypeTags.DECIMAL_TAG:
                case TypeTags.STRING_TAG:
                case TypeTags.BOOLEAN_TAG:
                case TypeTags.NULL_TAG:
                case TypeTags.JSON_TAG:
                    return true;
                case TypeTags.MAP_TAG:
                    return checkCastByType(((BMapType) rhsType).getConstrainedType(), lhsType, unresolvedTypes);
                case TypeTags.ARRAY_TAG:
                    return checkCastByType(((BArrayType) rhsType).getElementType(), lhsType, unresolvedTypes);
                default:
                    return false;
            }
        }

        if (rhsType.getTag() == TypeTags.OBJECT_TYPE_TAG && lhsType.getTag() == TypeTags.OBJECT_TYPE_TAG) {
            return checkObjectEquivalency(rhsType, (BObjectType) lhsType, unresolvedTypes);
        }

        if (rhsType.getTag() == TypeTags.RECORD_TYPE_TAG && lhsType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            return checkRecordEquivalency((BRecordType) lhsType, (BRecordType) rhsType, unresolvedTypes);
        }

        if (rhsType.getTag() == TypeTags.MAP_TAG && lhsType.getTag() == TypeTags.MAP_TAG) {
            return checkMapCast(rhsType, lhsType, unresolvedTypes);
        }

        if (rhsType.getTag() == TypeTags.TABLE_TAG && lhsType.getTag() == TypeTags.TABLE_TAG) {
            return true;
        }

        if (rhsType.getTag() == TypeTags.FUNCTION_POINTER_TAG && lhsType.getTag() == TypeTags.FUNCTION_POINTER_TAG) {
            return checkIsFunctionType(rhsType, (BFunctionType) lhsType);
        }

        return lhsType.getTag() == TypeTags.ANYDATA_TAG && isAnydata(rhsType);
    }

    private static boolean checkMapCast(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
        BMapType sourceMapType = (BMapType) sourceType;
        BMapType targetMapType = (BMapType) targetType;

        if (sourceMapType.equals(targetMapType)) {
            return true;
        }

        if (targetMapType.getConstrainedType().getTag() == TypeTags.ANY_TAG) {
            return true;
        }

        if (sourceMapType.getConstrainedType().getTag() == TypeTags.OBJECT_TYPE_TAG &&
                targetMapType.getConstrainedType().getTag() == TypeTags.OBJECT_TYPE_TAG) {
            return checkObjectEquivalency(sourceMapType.getConstrainedType(),
                                          (BObjectType) targetMapType.getConstrainedType(), unresolvedTypes);
        }

        if (sourceMapType.getConstrainedType().getTag() == TypeTags.RECORD_TYPE_TAG &&
                targetMapType.getConstrainedType().getTag() == TypeTags.RECORD_TYPE_TAG) {
            return checkRecordEquivalency((BRecordType) targetMapType.getConstrainedType(),
                                          (BRecordType) sourceMapType.getConstrainedType(), unresolvedTypes);
        }
        return false;
    }

    private static boolean checkArrayCast(BType sourceType, BType targetType, List<TypePair> unresolvedTypes) {
        if (targetType.getTag() == TypeTags.ARRAY_TAG && sourceType.getTag() == TypeTags.ARRAY_TAG) {
            BArrayType sourceArrayType = (BArrayType) sourceType;
            BArrayType targetArrayType = (BArrayType) targetType;
            if (targetArrayType.getDimensions() > sourceArrayType.getDimensions()) {
                return false;
            }

            return checkArrayCast(sourceArrayType.getElementType(), targetArrayType.getElementType(), unresolvedTypes);
        } else if (targetType.getTag() == TypeTags.UNION_TAG) {
            return checkUnionAssignable(sourceType, targetType, unresolvedTypes);
        }

        if (targetType.getTag() == TypeTags.ANY_TAG) {
            return true;
        }

        return sourceType.equals(targetType);
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

    private static boolean checkTableConstraints(BType sourceConstraint, BType targetConstraint,
                                                 List<TypePair> unresolvedTypes) {
        // handle unconstrained tables returned by actions
        if (sourceConstraint == null) {
            if (targetConstraint.getTag() == TypeTags.RECORD_TYPE_TAG) {
                BRecordType targetConstrRecord = (BRecordType) targetConstraint;
                return !targetConstrRecord.sealed && targetConstrRecord.restFieldType == BTypes.typeAnydata;
            }
            return false;
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

    private static boolean checkIsLikeType(Object sourceValue, BType targetType, List<TypeValuePair> unresolvedValues) {
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
                return ((MapValue) sourceValue).values().stream()
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
        if (!(sourceValue instanceof MapValue)) {
            return false;
        }

        for (Object mapEntry : ((MapValue) sourceValue).values()) {
            if (!checkIsLikeType(mapEntry, targetType.getConstrainedType(), unresolvedValues)) {
                return false;
            }
        }
        return true;
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
            for (Object value : ((MapValue) sourceValue).values()) {
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
            for (Object object : ((MapValue) sourceValue).values()) {
                if (!checkIsLikeType(object, targetType, unresolvedValues)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkIsLikeRecordType(Object sourceValue, BRecordType targetType,
                                                 List<TypeValuePair> unresolvedValues) {
        if (!(sourceValue instanceof MapValue)) {
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

            if (!(((MapValue) sourceValue).containsKey(fieldName)) &&
                    !Flags.isFlagOn(targetType.getFields().get(fieldName).flags, Flags.OPTIONAL)) {
                return false;
            }
        }

        for (Object object : ((MapValue) sourceValue).entrySet()) {
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

    private static BLangRuntimeException getTypeCastError(Object sourceVal, BType targetType) {
        return new BLangRuntimeException("incompatible types: '" + getType(sourceVal) +
                                                 "' cannot be cast to '" + targetType + "'");
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
}
