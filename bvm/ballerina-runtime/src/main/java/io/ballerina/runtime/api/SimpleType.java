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
import java.util.stream.Stream;

public record SimpleType(long all, long some) {

    public SimpleType intersection(SimpleType other) {
        long all = this.all & other.all;
        long some = (this.some | this.all) & (other.some | other.all);
        some &= ~all;
        return new SimpleType(all, some);
    }

    public SimpleType union(SimpleType other) {
        long all = this.all | other.all;
        long some = this.some | other.some;
        some &= ~all;
        return new SimpleType(all, some);
    }

    public SimpleType diff(SimpleType other) {
        long all = this.all & ~(other.all | other.some);
        long some = (this.all | this.some) & ~(other.all);
        some &= ~all;
        return new SimpleType(all, some);
    }

    @Override
    public String toString() {
        Tag[] allTags =
                Stream.of(Tag.values()).filter(tag -> (all & (1L << tag.ordinal())) != 0)
                        .toArray(Tag[]::new);
        Tag[] someTags =
                Stream.of(Tag.values()).filter(tag -> (some & (1L << tag.ordinal())) != 0)
                        .toArray(Tag[]::new);
        StringBuilder sb = new StringBuilder();
        sb.append("SimpleType all: {");
        for (Tag tag : allTags) {
            sb.append(tag).append(", ");
        }
        sb.append("} some: {");
        for (Tag tag : someTags) {
            sb.append(tag).append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    public enum Tag {
        NIL,
        BOOLEAN,
        INT,
        FLOAT,
        DECIMAL,
        STRING,
        ERROR,
        TYPEDESC,
        HANDLE,
        FUNCTION,

        // Inherently mutable
        FUTURE,
        STREAM,

        // Selectively immutable
        LIST,
        MAPPING,
        TABLE,
        XML,
        OBJECT,
        CELL
    }

    public static class Builder {

        public static final long NONE = 0;
        public static final long ALL = (1L << (Tag.values().length + 1)) - 1;

        public static long basicTypeUnionBitset(Tag... basicTypes) {
            long bits = 0;
            for (Tag basicType : basicTypes) {
                bits |= (1L << basicType.ordinal());
            }
            return bits;
        }

        public static long basicTypeBitset(Tag basicType) {
            return 1L << basicType.ordinal();
        }

        public static SimpleType createContainerSimpleType(Type constraint, Tag tag) {
            SimpleType constraintSimpleType = constraint.getSimpleType();
            if (constraintSimpleType.all == ALL) {
                // no constraint mean it is the top type
                return new SimpleType(Builder.basicTypeBitset(tag), Builder.NONE);
            }
            return new SimpleType(Builder.NONE, Builder.basicTypeBitset(tag));
        }

        public static long except(Tag... basicTypes) {
            long bits = ALL;
            for (Tag basicType : basicTypes) {
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
}
