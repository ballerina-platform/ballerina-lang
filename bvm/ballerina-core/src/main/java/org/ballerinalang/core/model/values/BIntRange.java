/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.core.model.values;

import org.ballerinalang.core.model.types.BArrayType;
import org.ballerinalang.core.model.types.BType;
import org.ballerinalang.core.model.types.BTypes;

import java.util.Map;

/**
 * {@code {@link BIntRange}} represents integer range in Ballerina.
 *
 * @since 0.96.0
 */
public class BIntRange implements BRefType, BCollection {

    private long startValue, endValue;

    public BIntRange(long startValue, long endValue) {
        this.startValue = startValue;
        this.endValue = endValue;
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public BIterator newIterator() {
        return new BIntRangeIterator(this);
    }

    /**
     * {@code {@link BIntRangeIterator}} implements iterator for Ballerina int range.
     *
     * @since 0.96.0
     */
    static class BIntRangeIterator implements BIterator {

        private BIntRange collection;
        long currentValue;

        BIntRangeIterator(BIntRange collection) {
            this.collection = collection;
            this.currentValue = collection.startValue;
        }

        @Override
        public BValue getNext() {
            long currentValue = this.currentValue++;
            if (hasNext()) {
                return new BInteger(currentValue);
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            return collection.startValue <= currentValue && currentValue <= collection.endValue;
        }
    }

    /* Default implementation */

    @Override
    public BType getType() {
        return new BArrayType(BTypes.typeInt);
    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        return null;
    }

    @Override
    public Object value() {
        return null;
    }
}
