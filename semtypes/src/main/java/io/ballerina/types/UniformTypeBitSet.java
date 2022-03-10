/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types;

/**
 * UniformTypeBitSet node.
 *
 * @since 3.0.0
 */
public class UniformTypeBitSet implements SemType {
    public final int bitset;

    private UniformTypeBitSet(int bitset) {
        this.bitset = bitset;
    }

    public static UniformTypeBitSet from(int bitset) {
        return new UniformTypeBitSet(bitset);
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
        if (!(o instanceof UniformTypeBitSet)) {
            return false;
        }
        UniformTypeBitSet s = (UniformTypeBitSet) o;
        return (s.bitset == this.bitset);
    }

    @Override
    public int hashCode() {
        return bitset;
    }
}
