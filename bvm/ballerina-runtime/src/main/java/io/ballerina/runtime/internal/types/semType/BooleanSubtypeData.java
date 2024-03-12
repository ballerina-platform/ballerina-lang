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

package io.ballerina.runtime.internal.types.semType;

public class BooleanSubtypeData implements ProperSubTypeData {

    final boolean value;

    public BooleanSubtypeData(boolean value) {
        this.value = value;
    }

    private BooleanSubtypeData() {
        this.value = false;
    }

    @Override
    public ProperSubTypeData union(ProperSubTypeData other) {
        if (other instanceof BooleanSubtypeData otherBoolean) {
            return new BooleanSubtypeData(value || otherBoolean.value);
        } else {
            throw new UnsupportedOperationException("union of different subtypes");
        }
    }

    @Override
    public ProperSubTypeData intersect(ProperSubTypeData other) {
        if (other instanceof BooleanSubtypeData otherBoolean) {
            if (otherBoolean.value == value) {
                return new BooleanSubtypeData(value);
            } else {
                return new BooleanSubtypeData();
            }
        } else {
            throw new UnsupportedOperationException("intersection of different subtypes");
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isSubType(ProperSubTypeData other) {
        if (other instanceof BooleanSubtypeData otherBoolean) {
            return otherBoolean.isEmpty || otherBoolean.value == value;
        } else {
            throw new UnsupportedOperationException("subtypes check of different subtypes");
        }
    }
}
