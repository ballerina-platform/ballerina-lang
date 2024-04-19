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
public class BasicTypeBitSet implements SemType {
    public final int bitset;

    private BasicTypeBitSet(int bitset) {
        this.bitset = bitset;
    }

    public static BasicTypeBitSet from(int bitset) {
        return new BasicTypeBitSet(bitset);
    }

    public static BasicTypeBitSet union(BasicTypeBitSet t1, BasicTypeBitSet t2) {
        return new BasicTypeBitSet(t1.bitset | t2.bitset);
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
}
