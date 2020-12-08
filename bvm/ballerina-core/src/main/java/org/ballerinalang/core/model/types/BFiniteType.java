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

package org.ballerinalang.core.model.types;

import org.ballerinalang.core.model.values.BValue;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@code BFiniteType} represents the finite type in Ballerina.
 */
public class BFiniteType extends BType {

    public Set<BValue> valueSpace;

    public BFiniteType(String typeName, String pkgPath) {
        super(typeName, pkgPath, BValue.class);
        this.valueSpace = new LinkedHashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BFiniteType)) {
            return false;
        }
        BFiniteType that = (BFiniteType) o;
        return this.valueSpace.size() == that.valueSpace.size() && this.valueSpace.containsAll(that.valueSpace);
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        if (valueSpace.stream().anyMatch(val -> val == null || val.getType().isNilable())) {
            return null;
        }

        Iterator<BValue> valueIterator = valueSpace.iterator();
        BValue firstVal = valueIterator.next();

        if (isSingletonType()) {
            return (V) firstVal;
        }

        BValue implicitInitValOfType = firstVal.getType().getZeroValue();
        if (implicitInitValOfType.equals(firstVal)) {
            return (V) implicitInitValOfType;
        }

        while (valueIterator.hasNext()) {
            BValue value = valueIterator.next();
            if (implicitInitValOfType.equals(value)) {
                return (V) implicitInitValOfType;
            }
        }

        return null;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        if (valueSpace.stream().anyMatch(val -> val == null || val.getType().isNilable())) {
            return null;
        }

        Iterator<BValue> valueIterator = valueSpace.iterator();
        BValue firstVal = valueIterator.next();

        if (isSingletonType()) {
            return (V) firstVal;
        }

        BValue implicitInitValOfType = firstVal.getType().getEmptyValue();
        if (implicitInitValOfType.equals(firstVal)) {
            return (V) implicitInitValOfType;
        }

        while (valueIterator.hasNext()) {
            BValue value = valueIterator.next();
            if (implicitInitValOfType.equals(value)) {
                return (V) implicitInitValOfType;
            }
        }

        return null;
    }

    @Override
    public int getTag() {
        return TypeTags.FINITE_TYPE_TAG;
    }

    private boolean isSingletonType() {
        return valueSpace.size() == 1;
    }
}
