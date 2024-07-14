/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types;

/**
 * BasicTypeBitSet node.
 *
 * @since 2201.8.0
 */
public final class BasicTypeBitSet implements SemType {

    public final int bitset;

    private BasicTypeBitSet(int bitset) {
        this.bitset = bitset;
    }

    public static BasicTypeBitSet from(int bitset) {
        if (bitset == 0) {
            return BitSetCache.ZERO;
        }
        if (Integer.bitCount(bitset) == 1) {
            return BitSetCache.CACHE[Integer.numberOfTrailingZeros(bitset)];
        }
        return new BasicTypeBitSet(bitset);
    }

    public static BasicTypeBitSet union(BasicTypeBitSet t1, BasicTypeBitSet t2) {
        return BasicTypeBitSet.from(t1.bitset | t2.bitset);
    }

    @Override
    public String toString() {
        return PredefinedType.toString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof BasicTypeBitSet b) {
            return b.bitset == this.bitset;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return bitset;
    }

    @Override
    public int all() {
        return bitset;
    }

    private static final class BitSetCache {

        private static final int SIZE = 0x14;
        private static final BasicTypeBitSet[] CACHE = new BasicTypeBitSet[SIZE];
        private static final BasicTypeBitSet ZERO = new BasicTypeBitSet(0);

        static {
            for (int i = 0; i < SIZE; i++) {
                CACHE[i] = new BasicTypeBitSet(1 << i);
            }
        }
    }
}
