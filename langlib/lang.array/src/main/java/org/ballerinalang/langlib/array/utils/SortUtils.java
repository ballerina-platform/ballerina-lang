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
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BTupleType;

import java.util.*;

public class SortUtils {

    public static boolean isOrderedType(Type type) {
        type = TypeUtils.getImpliedType(type);
        switch (type.getTag()) {
            case TypeTags.UNION_TAG:
                UnionType unionType = (UnionType) type;
                if (unionType.isCyclic()) {
                    return true;
                }
                Set<Type> memberTypes = new HashSet<>(unionType.getMemberTypes());
                Type firstTypeInUnion = getTypeWithEffectiveIntersectionTypes(TypeUtils.getImpliedType(
                        memberTypes.stream().filter(m -> m.getTag() != TypeTags.NULL_TAG).findFirst().
                                orElse(memberTypes.iterator().next())));
                if (firstTypeInUnion.getTag() == TypeTags.NULL_TAG) {
                    // Union contains only the nil type.
                    return true;
                }
                if(!isOrderedType(firstTypeInUnion)) {
                    return false;
                }
                for (Type memType : memberTypes) {
                    memType = TypeUtils.getImpliedType(memType);
                    if(!isOrderedType(memType) || isDifferentOrderedType(memType, firstTypeInUnion)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.ARRAY_TAG:
                return isOrderedType(((BArrayType) type).getElementType());
            case TypeTags.TUPLE_TAG:
                List<Type> tupleTypes = ((BTupleType) type).getTupleTypes();
                if (((BTupleType) type).getRestType() != null) tupleTypes.add(((BTupleType) type).getRestType());
                if(!isOrderedType(tupleTypes.get(0))) {
                    return false;
                }
                for (Type memType : tupleTypes) {
                    if (!isOrderedType(memType) || isDifferentOrderedType(memType, tupleTypes.get(0))) {
                        return false;
                    }
                }
                return true;
            case TypeTags.FINITE_TYPE_TAG:
                Set<Object> valSpace = ((BFiniteType) type).getValueSpace();
                Type baseExprType = TypeUtils.getType(valSpace.iterator().next());
                if (!checkValueSpaceHasSameType((BFiniteType) type, baseExprType)) {
                    return false;
                }
                return isOrderedType(baseExprType);
            default:
                return isSimpleBasicType(type.getTag());
        }
    }

    public static Type getTypeWithEffectiveIntersectionTypes(Type bType) {
        Type type = TypeUtils.getReferredType(bType);
        Type effectiveType = null;
        if (type.getTag() == TypeTags.INTERSECTION_TAG) {
            effectiveType = ((IntersectionType)type).getEffectiveType();
            type = effectiveType;
        }

        if (type.getTag() != TypeTags.UNION_TAG) {
            return Objects.requireNonNullElse(effectiveType, bType);
        }

        LinkedHashSet<Type> members = new LinkedHashSet<>();
        boolean hasDifferentMember = false;

        for (Type memberType : ((UnionType) type).getMemberTypes()) {
            effectiveType = getTypeWithEffectiveIntersectionTypes(memberType);
            effectiveType = TypeUtils.getImpliedType(effectiveType);
            if (effectiveType != memberType) {
                hasDifferentMember = true;
            }
            members.add(effectiveType);
        }

        if (hasDifferentMember) {
            return TypeCreator.createUnionType(members.stream().toList());
        }
        return bType;
    }

    public static boolean checkValueSpaceHasSameType(BFiniteType finiteType, Type type) {
        Type baseType = TypeUtils.getImpliedType(type);
        if (baseType.getTag() == TypeTags.FINITE_TYPE_TAG) {
            return checkValueSpaceHasSameType((BFiniteType) baseType,
                    TypeUtils.getType(finiteType.getValueSpace().iterator().next()));
        }
        for (Object expr : finiteType.getValueSpace()) {
            if(isDifferentOrderedType(TypeUtils.getType(expr), baseType)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDifferentOrderedType(Type source, Type target) {
        source = TypeUtils.getImpliedType(source);
        target = TypeUtils.getImpliedType(target);
        if (source.getTag() == TypeTags.NULL_TAG || target.getTag() == TypeTags.NULL_TAG) {
            return false;
        }
        if(checkIfDifferent(source)) {
            return true;
        }
        return !TypeChecker.checkIsType(source, target);
    }

    public static boolean isSimpleBasicType(int tag) {
        return switch (tag) {
            case TypeTags.BYTE_TAG, TypeTags.FLOAT_TAG, TypeTags.DECIMAL_TAG, TypeTags.BOOLEAN_TAG, TypeTags.NULL_TAG ->
                    true;
            default ->  tag >= TypeTags.INT_TAG && tag <= TypeTags.CHAR_STRING_TAG;
        };
    }

    public static boolean checkIfDifferent(Type s) {
        return switch (s.getTag()) {
            case TypeTags.TYPE_REFERENCED_TYPE_TAG, TypeTags.ANY_TAG, TypeTags.ANYDATA_TAG, TypeTags.MAP_TAG,
                 TypeTags.FUTURE_TAG, TypeTags.XML_TAG, TypeTags.JSON_TAG, TypeTags.OBJECT_TYPE_TAG,
                 TypeTags.RECORD_TYPE_TAG, TypeTags.STREAM_TAG, TypeTags.TABLE_TAG, TypeTags.INVOKABLE_TAG,
                 TypeTags.ERROR_TAG -> true;
            default -> false;
        };
    }
}
