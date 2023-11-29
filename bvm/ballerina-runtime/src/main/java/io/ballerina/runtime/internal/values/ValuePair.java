/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BTypedesc;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Unordered value vector of size two, to hold two values being compared.
 *
 * @since 2201.9.0
 */

public class ValuePair implements RefValue {

    Set<Object> valuePairSet = new LinkedHashSet<>(2);

    public ValuePair(Object obj1, Object obj2) {
        valuePairSet.add(obj1);
        valuePairSet.add(obj2);
    }

    @Override
    public boolean equals(Object o, Set<ValuePair> visitedValues) {
        if (!(o instanceof ValuePair valuePair)) {
            return false;
        }

        Set<Object> otherSet = valuePair.valuePairSet;
        Set<Object> currentSet = this.valuePairSet;
        if (otherSet.size() != currentSet.size()) {
            return false;
        }

        for (Object otherObj : otherSet) {
            if (!currentSet.contains(otherObj)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BTypedesc getTypedesc() {
        return null;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return null;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return null;
    }

    @Override
    public String stringValue(BLink parent) {
        return null;
    }

    @Override
    public String expressionStringValue(BLink parent) {
        return null;
    }

    @Override
    public Type getType() {
        return null;
    }
}
