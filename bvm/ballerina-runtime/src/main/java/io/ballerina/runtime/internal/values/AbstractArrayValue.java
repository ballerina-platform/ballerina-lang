/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.internal.IteratorUtils;
import io.ballerina.runtime.internal.JsonGenerator;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ARRAY_LANG_LIB;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.INVALID_UPDATE_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static io.ballerina.runtime.internal.util.exceptions.RuntimeErrors.INVALID_READONLY_VALUE_UPDATE;

/**
 * <p>
 * Abstract implementation of an array value in ballerina.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 * 
 * @since 1.1.0
 */
public abstract class AbstractArrayValue implements ArrayValue {

    static final int SYSTEM_ARRAY_MAX = Integer.MAX_VALUE - 8;

    /**
     * The maximum size of arrays to allocate.
     * <p>
     * This is same as Java
     */
    protected int maxSize = SYSTEM_ARRAY_MAX;
    protected static final int DEFAULT_ARRAY_SIZE = 100;
    protected int size = 0;
    protected Type iteratorNextReturnType;

    /**
     * Append value to the existing array.
     * 
     * @param value value to be appended
     */
    @Override
    public void append(Object value) {
        add(size, value);
    }

    @Override
    public Object reverse() {
        throw new UnsupportedOperationException("reverse for tuple types is not supported directly.");
    }

    /**
     * Removes and returns first member of an array.
     * 
     * @return the value that was the first member of the array
     */
    @Override
    public Object shift() {
        return shift(0);
    }

    /**
     * Adds values to the start of an array.
     *
     * @param values values to add to the start of the array
     */

    public void unshift(Object[] values) {
        unshift(0, values);
    }

    @Override
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        ArrayValue copy = (ArrayValue) copy(refs);
        if (!copy.isFrozen()) {
            copy.freezeDirect();
        }
        return copy;
    }

    @Override
    public String toString() {
        return stringValue(null);
    }

    @Override
    public String getJSONString() {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        JsonGenerator gen = new JsonGenerator(byteOut);
        try {
            gen.serialize(this);
            gen.flush();
        } catch (IOException e) {
            throw new BallerinaException("Error in converting JSON to a string: " + e.getMessage(), e);
        }
        return byteOut.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IteratorValue getIterator() {
        return new ArrayIterator(this);
    }

    @Override
    public void setLength(long length) {
        if (length == size) {
            return;
        }
        handleImmutableArrayValue();
        int newLength = (int) length;
        checkFixedLength(length);
        rangeCheck(length, size);
        fillerValueCheck(newLength, size);
        resizeInternalArray(newLength);
        fillValues(newLength);
        size = newLength;
    }

    @Override
    public long getLength() {
        return size;
    }

    protected void initializeIteratorNextReturnType() {
        Type type = TypeUtils.getReferredType(getType());
        if (type.getTag() == TypeTags.ARRAY_TAG) {
            type = getElementType();
        } else {
            BTupleType tupleType = (BTupleType) type;
            LinkedHashSet<Type> types = new LinkedHashSet<>(tupleType.getTupleTypes());
            if (tupleType.getRestType() != null) {
                types.add(tupleType.getRestType());
            }
            if (types.size() == 1) {
                type = types.iterator().next();
            } else {
                type = new BUnionType(new ArrayList<>(types));
            }
        }
        iteratorNextReturnType = IteratorUtils.createIteratorNextReturnType(type);
    }

    public Type getIteratorNextReturnType() {
        if (iteratorNextReturnType == null) {
            initializeIteratorNextReturnType();
        }

        return iteratorNextReturnType;
    }

    /*
     * helper methods that are visible to the implementation classes.
     */

    protected abstract void fillValues(int newLength);

    protected abstract void fillerValueCheck(int newLength, int size2);

    protected abstract void resizeInternalArray(int newLength);

    protected abstract void rangeCheckForGet(long index, int size);

    protected abstract void rangeCheck(long index, int size);

    /**
     * Util method to handle immutable array values.
     */
    protected void handleImmutableArrayValue() {
        if (!this.getType().isReadOnly()) {
            return;
        }

        throw ErrorCreator.createError(getModulePrefixedReason(ARRAY_LANG_LIB, INVALID_UPDATE_ERROR_IDENTIFIER),
                                       BLangExceptionHelper.getErrorDetails(INVALID_READONLY_VALUE_UPDATE));
    }

    /**
     * Same as {@code prepareForAdd}, except fillerValueCheck is not performed as we are guaranteed to add
     * elements to consecutive positions.
     *
     * @param index last index after add operation completes
     * @param currentArraySize current array size
     */
    protected void prepareForConsecutiveMultiAdd(long index, int currentArraySize) {
        int intIndex = (int) index;
        rangeCheck(index, size);
        ensureCapacity(intIndex + 1, currentArraySize);
        resetSize(intIndex);
    }

    protected abstract void ensureCapacity(int requestedCapacity, int currentArraySize);

    private void resetSize(int index) {
        if (index >= size) {
            size = index + 1;
        }
    }

    protected abstract void unshift(long index, Object[] vals);

    protected abstract void checkFixedLength(long length);

    /**
     * {@code {@link ArrayIterator}} provides iterator implementation for Ballerina array values.
     *
     * @since 0.995.0
     */
    static class ArrayIterator implements IteratorValue {
        ArrayValue array;
        long cursor = 0;
        long length;

        ArrayIterator(ArrayValue value) {
            this.array = value;
            this.length = value.size();
        }

        @Override
        public Object next() {
            long cursor = this.cursor++;
            if (cursor == length) {
                return null;
            }
            return array.get(cursor);
        }

        @Override
        public boolean hasNext() {
            return cursor < length;
        }
    }
}
