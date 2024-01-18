/*
 *
 *   Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 * /
 *
 */

package io.ballerina.runtime.api;

import io.ballerina.runtime.api.types.Type;

import java.util.List;

public class SimpleTypeBuilder {

    public static final long NONE = 0;
    public static final long ALL = (1L << (SimpleTypeTag.values().length + 1)) - 1;

    public static long basicTypeUnionBitset(SimpleTypeTag... basicTypes) {
        long bits = 0;
        for (SimpleTypeTag basicType : basicTypes) {
            bits |= (1L << basicType.ordinal());
        }
        return bits;
    }

    public static long basicTypeBitset(SimpleTypeTag basicType) {
        return 1L << basicType.ordinal();
    }

    public static SimpleType createContainerSimpleType(Type constraint, SimpleTypeTag tag) {
        SimpleType constraintSimpleType = constraint.getSimpleType();
        if (constraintSimpleType.all() == ALL) {
            // no constraint mean it is the top type
            return new SimpleType(SimpleTypeBuilder.basicTypeBitset(tag), SimpleTypeBuilder.NONE);
        }
        return new SimpleType(SimpleTypeBuilder.NONE, SimpleTypeBuilder.basicTypeBitset(tag));
    }

    public static long except(SimpleTypeTag... basicTypes) {
        long bits = ALL;
        for (SimpleTypeTag basicType : basicTypes) {
            bits &= ~(1L << basicType.ordinal());
        }
        return bits;
    }

    public static SimpleType intersection(List<Type> types) {
        if (types.isEmpty()) {
            // I assume this (and union) is because we are modifying types after the fact
            return new SimpleType(NONE, NONE);
        }
        return types.stream().skip(1).map(Type::getSimpleType)
                .reduce(types.get(0).getSimpleType(), SimpleType::intersection);
    }

    public static SimpleType union(List<Type> types) {
        if (types.isEmpty()) {
            return new SimpleType(NONE, NONE);
        }
        return types.stream().skip(1).map(Type::getSimpleType)
                .reduce(types.get(0).getSimpleType(), SimpleType::union);
    }
}

