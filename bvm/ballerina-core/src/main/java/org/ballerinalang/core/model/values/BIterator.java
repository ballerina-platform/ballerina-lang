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

import org.ballerinalang.core.model.types.BType;
import org.ballerinalang.core.model.types.BTypes;

import java.util.Map;

/**
 * {@code {@link BIterator}} represents a Iterator of a Ballerina {@code {@link BCollection}}.
 *
 * @since 0.96.0
 */
public interface BIterator extends BRefType {

    /**
     * Get the array of BValues for next element.
     *
     * @return array of BValues for next element
     */
    BValue getNext();

    /**
     * Checks collection has a next value.
     *
     * @return true, if has a next value, false otherwise
     */
    boolean hasNext();

    /* Default implementation */

    @Override
    default String stringValue() {
        return null;
    }

    @Override
    default BType getType() {
        return BTypes.typeIterator;
    }

    @Override
    default BValue copy(Map<BValue, BValue> refs) {
        return null;
    }

    @Override
    default Object value() {
        return null;
    }
}
