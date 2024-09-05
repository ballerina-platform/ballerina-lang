/*
 * Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.array.utils;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BTupleType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A utility class containing methods needed for the sort operation on tuples and arrays.
 *
 * @since 2201.10.0
 */
public class SortUtils {

    /**
     * A private constructor to avoid code coverage warnings.
     */
    private SortUtils() {};

    /**
     * Check if the provided type is an Ordered type.
     * @param type type to be checked.
     * @return true if type is Ordered, false otherwise.
     */
    public static boolean isOrderedType(Type type) {
        type = TypeUtils.getImpliedType(type);
        switch (type.getTag()) {
            case TypeTags.UNION_TAG:
                UnionType unionType = (UnionType) type;
                if (unionType.isCyclic()) {
                    return true;
                }
                Set<Type> memberTypes = new HashSet<>(unionType.getMemberTypes());
                Type firstTypeInUnion = TypeUtils.getImpliedType(memberTypes.stream().findFirst().
                        orElse(memberTypes.iterator().next()));
                if (firstTypeInUnion.getTag() == TypeTags.NULL_TAG) {
                    // Union contains only the nil type.
                    return true;
                }
                if (!isOrderedType(firstTypeInUnion)) {
                    return false;
                }
                for (Type memType : memberTypes) {
                    memType = TypeUtils.getImpliedType(memType);
                    if (!isOrderedType(memType) || isDifferentOrderedType(memType, firstTypeInUnion)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.ARRAY_TAG:
                return isOrderedType(((BArrayType) type).getElementType());
            case TypeTags.TUPLE_TAG:
                BTupleType tupleType = (BTupleType) type;
                List<Type> tupleTypes = tupleType.getTupleTypes();
                if (tupleType.getRestType() != null) {
                    tupleTypes.add(tupleType.getRestType());
                }
                if (!isOrderedType(tupleTypes.get(0))) {
                    return false;
                }
                for (Type memType : tupleTypes) {
                    if (!isOrderedType(memType) || isDifferentOrderedType(memType, tupleTypes.get(0))) {
                        return false;
                    }
                }
                return true;
            case TypeTags.FINITE_TYPE_TAG:
                BFiniteType finiteType = (BFiniteType) type;
                Set<Object> valSpace = finiteType.getValueSpace();
                Type baseExprType = TypeUtils.getType(valSpace.iterator().next());
                if (!checkValueSpaceHasSameType(finiteType, baseExprType)) {
                    return false;
                }
                return isOrderedType(baseExprType);
            default:
                return isSimpleBasicType(type.getTag());
        }
    }

    /**
     * Check if the value space of the provided finite type belongs to the value space of the given type.
     * @param finiteType finite type to be checked.
     * @param type type to be checked against.
     * @return true if the finite type belongs to the same value space, false otherwise.
     */
    public static boolean checkValueSpaceHasSameType(BFiniteType finiteType, Type type) {
        Type baseType = TypeUtils.getImpliedType(type);
        if (baseType.getTag() == TypeTags.FINITE_TYPE_TAG) {
            return checkValueSpaceHasSameType((BFiniteType) baseType,
                    TypeUtils.getType(finiteType.getValueSpace().iterator().next()));
        }
        for (Object expr : finiteType.getValueSpace()) {
            if (isDifferentOrderedType(TypeUtils.getType(expr), baseType)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether a given type is different to a target type.
     * @param source type to check.
     * @param target type to compare with.
     * @return true if the source type does not belong to the target type, false otherwise.
     */
    public static boolean isDifferentOrderedType(Type source, Type target) {
        source = TypeUtils.getImpliedType(source);
        target = TypeUtils.getImpliedType(target);
        if (source.getTag() == TypeTags.NULL_TAG || target.getTag() == TypeTags.NULL_TAG) {
            return false;
        }
        return !TypeChecker.checkIsType(source, target);
    }

    /**
     * Check whether the given type tag belongs to a simple basic type.
     * @param tag type tag to check.
     * @return true if the tag belongs to a simple basic type, false otherwise.
     */
    public static boolean isSimpleBasicType(int tag) {
        return switch (tag) {
            case TypeTags.BYTE_TAG, TypeTags.FLOAT_TAG, TypeTags.DECIMAL_TAG, TypeTags.BOOLEAN_TAG, TypeTags.NULL_TAG ->
                    true;
            default ->  tag >= TypeTags.INT_TAG && tag <= TypeTags.CHAR_STRING_TAG;
        };
    }
}
