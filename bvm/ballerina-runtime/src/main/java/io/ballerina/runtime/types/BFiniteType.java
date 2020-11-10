/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.types;

import io.ballerina.runtime.TypeChecker;
import io.ballerina.runtime.api.TypeFlags;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.FiniteType;
import io.ballerina.runtime.values.RefValue;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@code BFiniteType} represents the finite type in Ballerina.
 * 
 * @since 0.995.0
 */
public class BFiniteType extends BType implements FiniteType {

    public Set<Object> valueSpace;
    private int typeFlags;

    public BFiniteType(String typeName) {
        super(typeName, null, RefValue.class);
        this.valueSpace = new LinkedHashSet<>();
    }

    public BFiniteType(String typeName, Set<Object> values, int typeFlags) {
        super(typeName, null, RefValue.class);
        this.valueSpace = values;
        this.typeFlags = typeFlags;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        if (valueSpace.stream().anyMatch(val -> val == null || TypeChecker.getType(val).isNilable())) {
            return null;
        }

        Iterator<Object> valueIterator = valueSpace.iterator();
        Object firstVal = valueIterator.next();

        if (isSingletonType()) {
            return (V) firstVal;
        }

        Object implicitInitValOfType = TypeChecker.getType(firstVal).getZeroValue();
        if (implicitInitValOfType.equals(firstVal)) {
            return (V) implicitInitValOfType;
        }

        while (valueIterator.hasNext()) {
            Object value = valueIterator.next();
            if (implicitInitValOfType.equals(value)) {
                return (V) implicitInitValOfType;
            }
        }

        return null;
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        if (valueSpace.stream().anyMatch(val -> val == null || TypeChecker.getType(val).isNilable())) {
            return null;
        }

        Iterator<Object> valueIterator = valueSpace.iterator();
        Object firstVal = valueIterator.next();

        if (isSingletonType()) {
            return (V) firstVal;
        }

        Object implicitInitValOfType = TypeChecker.getType(firstVal).getEmptyValue();
        if (implicitInitValOfType.equals(firstVal)) {
            return (V) implicitInitValOfType;
        }

        while (valueIterator.hasNext()) {
            Object value = valueIterator.next();
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

    @Override
    public boolean isAnydata() {
        return TypeFlags.isFlagOn(this.typeFlags, TypeFlags.ANYDATA);
    }

    @Override
    public boolean isPureType() {
        return TypeFlags.isFlagOn(this.typeFlags, TypeFlags.PURETYPE);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    public Set<Object> getValueSpace() {
        return valueSpace;
    }

    public int getTypeFlags() {
        return typeFlags;
    }
}
