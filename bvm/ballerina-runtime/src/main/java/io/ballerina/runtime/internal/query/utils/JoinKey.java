/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at:
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

package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.values.DecimalValue;

import java.util.Map;
import java.util.Set;

/**
 * A join key that compares with DeepEquals, for use as a hash map key.
 *
 * @since 2201.14.2
 */
public final class JoinKey {

    // Lists and mappings nested deeper than this are hashed only by their size. This keeps hashing fast and stops
    // it from looping forever on a cyclic value, while equal values still get the same hash code.
    private static final int MAX_HASH_DEPTH = 3;
    private static final int NIL_HASH = 0;
    private static final int LIST_HASH_SEED = 17;
    private static final int MAPPING_HASH_SEED = 19;
    private static final int OTHER_HASH = 23;

    private final Object value;
    private final int hash;

    public JoinKey(Object value) {
        this.value = value;
        this.hash = hash(value, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof JoinKey other && hash == other.hash && TypeChecker.isEqual(value, other.value);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    private static int hash(Object value, int depth) {
        if (value == null) {
            return NIL_HASH;
        }
        if (value instanceof BString str) {
            return str.getValue().hashCode();
        }
        if (value instanceof Double floatValue) {
            // -0.0 == 0.0, so both must have the same hash code. Double.hashCode() is the same for every NaN.
            return floatValue == 0.0 ? 0 : Double.hashCode(floatValue);
        }
        if (value instanceof DecimalValue decimalValue) {
            // 1.0 and 1.00 are equal, so trailing zeros are removed before hashing.
            return decimalValue.decimalValue().stripTrailingZeros().hashCode();
        }
        if (value instanceof Number intValue) {
            // int and byte values
            return Long.hashCode(intValue.longValue());
        }
        if (value instanceof Boolean booleanValue) {
            return Boolean.hashCode(booleanValue);
        }
        if (value instanceof BArray list) {
            return hashList(list, depth);
        }
        if (value instanceof BMap<?, ?> mapping) {
            return hashMapping(mapping, depth);
        }
        // xml, table and regexp values. These are rarely used as join keys, so they share one hash code.
        return OTHER_HASH;
    }

    private static int hashList(BArray list, int depth) {
        int size = list.size();
        int result = 31 * LIST_HASH_SEED + size;
        if (depth >= MAX_HASH_DEPTH) {
            return result;
        }
        for (int i = 0; i < size; i++) {
            result = 31 * result + hash(list.get(i), depth + 1);
        }
        return result;
    }

    private static int hashMapping(BMap<?, ?> mapping, int depth) {
        Set<? extends Map.Entry<?, ?>> fields = mapping.entrySet();
        int result = 31 * MAPPING_HASH_SEED + fields.size();
        if (depth >= MAX_HASH_DEPTH) {
            return result;
        }
        // Mappings with the same fields in a different order are equal. Adding the field hashes together gives
        // the same result for any order.
        int fieldsHash = 0;
        for (Map.Entry<?, ?> field : fields) {
            fieldsHash += hash(field.getKey(), depth + 1) ^ hash(field.getValue(), depth + 1);
        }
        return 31 * result + fieldsHash;
    }
}
