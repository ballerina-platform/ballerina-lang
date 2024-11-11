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

import java.util.HashSet;
import java.util.Set;

/**
 * Unordered value vector of size two, to hold two values being compared.
 *
 * @since 2201.9.0
 */

public class ValuePair {

    Set<Object> valuePairSet = new HashSet<>(2);

    public ValuePair(Object obj1, Object obj2) {
        valuePairSet.add(obj1);
        valuePairSet.add(obj2);
    }

    public boolean equals(Object o) {
        if (!(o instanceof ValuePair valuePair)) {
            return false;
        }
        Set<Object> otherSet = valuePair.valuePairSet;
        Set<Object> currentSet = this.valuePairSet;
        for (Object otherObj : otherSet) {
            if (!currentSet.contains(otherObj)) {
                return false;
            }
        }
        return true;
    }
}
