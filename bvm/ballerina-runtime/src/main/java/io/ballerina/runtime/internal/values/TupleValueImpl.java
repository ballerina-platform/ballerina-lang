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
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BListInitialValueEntry;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.CycleUtils;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.IntStream;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ARRAY_LANG_LIB;
import static io.ballerina.runtime.internal.ValueUtils.createSingletonTypedesc;
import static io.ballerina.runtime.internal.ValueUtils.getTypedescValue;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * <p>
 * Represent an array in ballerina.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 *
 * @since 0.995.0
 */
public class TupleValueImpl extends AbstractArrayValue {

    protected TupleType tupleType;
    protected Type type;
    Object[] refValues;
    private int minSize;
    private boolean hasRestElement; // cached value for ease of access
    private BTypedesc typedesc;
    // ------------------------ Constructors -------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TupleValueImpl that = (TupleValueImpl) o;
        return minSize == that.minSize &&
                hasRestElement == that.hasRestElement &&
                type.equals(that.type) &&
                Arrays.equals(refValues, that.refValues);
    }

    public TupleValueImpl(Object[] values, TupleType type) {
        this.refValues = values;
        this.type = this.tupleType = type;
        this.hasRestElement = this.tupleType.getRestType() != null;

        List<Type> memTypes = type.getTupleTypes();
        int memCount = memTypes.size();

        if (values.length < memCount) {
            this.refValues = Arrays.copyOf(refValues, memCount);
            for (int i = values.length; i < memCount; i++) {
                refValues[i] = memTypes.get(i).getZeroValue();
            }
        }
        this.minSize = memTypes.size();
        this.size = refValues.length;
        this.typedesc = getTypedescValue(type, this);
    }

    public TupleValueImpl(TupleType type) {
        this.type = this.tupleType = type;

        List<Type> memTypes = this.tupleType.getTupleTypes();
        int memTypeCount = memTypes.size();

        this.minSize = this.size = memTypeCount;
        this.hasRestElement = this.tupleType.getRestType() != null;

        if (type.getRestType() == null) {
            this.maxSize = this.size;
            this.refValues = new Object[this.size];
        } else {
            this.refValues = new Object[DEFAULT_ARRAY_SIZE];
        }

        for (int i = 0; i < memTypeCount; i++) {
            Type memType = memTypes.get(i);
            if (!TypeChecker.hasFillerValue(memType)) {
                continue;
            }
            this.refValues[i] = memType.getZeroValue();
        }
        this.typedesc = getTypedescValue(type, this);
    }

    public TupleValueImpl(TupleType type, long size, BListInitialValueEntry[] initialValues) {
        this(type, initialValues);
    }

    public TupleValueImpl(Type type, BListInitialValueEntry[] initialValues) {
        this.type = type;
        this.tupleType = (TupleType) TypeUtils.getReferredType(type);

                List<Type> memTypes = this.tupleType.getTupleTypes();
        int memCount = memTypes.size();

        if (tupleType.getRestType() != null) {
            int valueCount = 0;
            for (BListInitialValueEntry listEntry : initialValues) {
                if (listEntry instanceof ListInitialValueEntry.ExpressionEntry) {
                    valueCount++;
                } else {
                    BArray values = ((ListInitialValueEntry.SpreadEntry) listEntry).values;
                    valueCount += values.size();
                }
            }
            this.size = Math.max(valueCount, memCount);
        } else {
            this.size = memCount;
        }

        this.minSize = memCount;
        this.hasRestElement = this.tupleType.getRestType() != null;

        if (tupleType.getRestType() == null) {
            this.maxSize = this.size;
            this.refValues = new Object[this.size];
        } else {
            this.refValues = new Object[DEFAULT_ARRAY_SIZE];
        }

        int index = 0;
        for (BListInitialValueEntry listEntry : initialValues) {
            if (listEntry instanceof ListInitialValueEntry.ExpressionEntry) {
                addRefValue(index++, ((ListInitialValueEntry.ExpressionEntry) listEntry).value);
            } else {
                BArray values = ((ListInitialValueEntry.SpreadEntry) listEntry).values;
                BIterator<?> iterator = values.getIterator();
                while (iterator.hasNext()) {
                    addRefValue(index++, iterator.next());
                }
            }
        }

        if (index >= memCount) {
            this.typedesc = getTypedescValue(type, this);
            return;
        }

        for (int i = index; i < memCount; i++) {
            Type memType = memTypes.get(i);
            if (!TypeChecker.hasFillerValue(memType)) {
                continue;
            }

            this.refValues[i] = memType.getZeroValue();
        }
        this.typedesc = getTypedescValue(type, this);
    }

    public TupleValueImpl(Type type, BListInitialValueEntry[] initialValues, TypedescValueImpl inherentType) {
        this(type, initialValues);
        this.typedesc = getTypedescValue(type.isReadOnly(), this, inherentType);
    }

    @Override
    public BTypedesc getTypedesc() {
        return typedesc;
    }

    // ----------------------- get methods ----------------------------------------------------

    /**
     * Get value in the given array index.
     * 
     * @param index array index
     * @return array value
     */
    @Override
    public Object get(long index) {
        rangeCheckForGet(index, this.size);
        return this.refValues[(int) index];
    }

    /**
     * Get ref value in the given index.
     * 
     * @param index array index
     * @return array value
     */
    @Override
    public Object getRefValue(long index) {
        return get(index);
    }

    @Override
    public Object fillAndGetRefValue(long index) {
        // Need do a filling-read if index >= size
        if (index >= this.size && this.hasRestElement) {
            handleImmutableArrayValue();
            fillRead(index, refValues.length);
            return this.refValues[(int) index];
        }
        return get(index);
    }

    /**
     * Get int value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    public long getInt(long index) {
        return (Long) get(index);
    }

    /**
     * Get boolean value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    public boolean getBoolean(long index) {
        return (Boolean) get(index);
    }

    /**
     * Get byte value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    public byte getByte(long index) {
        return (Byte) get(index);
    }

    /**
     * Get float value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    public double getFloat(long index) {
        return (Double) get(index);
    }

    /**
     * Get string value in the given index.
     * 
     * @param index array index
     * @return array element
     */
    @Override
    public String getString(long index) {
        return get(index).toString();
    }

    /**
     * Get string value in the given index.
     *
     * @param index array index
     * @return array element
     */
    @Override
    public BString getBString(long index) {
        return (BString) get(index);
    }

    // ---------------------------- add methods --------------------------------------------------

    /**
     * Add ref value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, Object value) {
        handleImmutableArrayValue();
        addRefValue(index, value);
    }

    private void addRefValue(long index, Object value) {
        prepareForAdd(index, value, refValues.length);
        refValues[(int) index] = value;
    }

    /**
     * Add int value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, long value) {
        add(index, Long.valueOf(value));
    }

    /**
     * Add boolean value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, boolean value) {
        add(index, Boolean.valueOf(value));
    }

    /**
     * Add byte value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, byte value) {
        add(index, Byte.valueOf(value));
    }

    /**
     * Add double value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, double value) {
        add(index, Double.valueOf(value));
    }

    /**
     * Add string value to the given array index.
     * 
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, String value) {
        add(index, (Object) value);
    }

    /**
     * Add string value to the given array index.
     *
     * @param index array index
     * @param value value to be added
     */
    @Override
    public void add(long index, BString value) {
        add(index, (Object) value);
    }

    // -------------------------------------------------------------------------------------------------------------

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
    public Object shift(long index) {
        handleImmutableArrayValue();
        Object val = get(index);
        shiftArray((int) index);
        return val;
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

    @Override
    public void unshift(Object[] values) {
        unshift(0, values);
    }

    @Override
    public String stringValue(BLink parent) {
        StringJoiner sj = new StringJoiner(",");
        for (int i = 0; i < this.size; i++) {
            Object value = this.refValues[i];
            Type type = TypeChecker.getType(value);
            CycleUtils.Node parentNode = new CycleUtils.Node(this, parent);
            switch (type.getTag()) {
                case TypeTags.STRING_TAG:
                case TypeTags.XML_TAG:
                case TypeTags.XML_ELEMENT_TAG:
                case TypeTags.XML_ATTRIBUTES_TAG:
                case TypeTags.XML_COMMENT_TAG:
                case TypeTags.XML_PI_TAG:
                case TypeTags.XMLNS_TAG:
                case TypeTags.XML_TEXT_TAG:
                    sj.add(((BValue) value).informalStringValue(parentNode));
                    break;
                case TypeTags.NULL_TAG:
                    sj.add("null");
                    break;
                default:
                    sj.add(StringUtils.getStringValue(value, new CycleUtils.Node(this, parentNode)));
                    break;
            }
        }
        return "[" + sj + "]";
    }

    @Override
    public String expressionStringValue(BLink parent) {
        StringJoiner sj = new StringJoiner(",");
        for (int i = 0; i < this.size; i++) {
            sj.add(StringUtils.getExpressionStringValue(this.refValues[i], new CycleUtils.Node(this, parent)));
        }
        return "[" + sj + "]";
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public BArray slice(long startIndex, long endIndex) {
        return null;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        if (isFrozen()) {
            return this;
        }

        if (refs.containsKey(this)) {
            return refs.get(this);
        }

        Object[] values = new Object[this.size];
        TupleValueImpl refValueArray = new TupleValueImpl(values, this.tupleType);
        refs.put(this, refValueArray);
        IntStream.range(0, this.size).forEach(i -> {
            Object value = this.refValues[i];
            if (value instanceof RefValue) {
                values[i] = ((RefValue) value).copy(refs);
            } else {
                values[i] = value;
            }
        });

        return refValueArray;
    }

    /**
     * Get ref values array.
     * 
     * @return ref value array
     */
    @Override
    public Object[] getValues() {
        return refValues;
    }

    /**
     * Get a copy of byte array.
     * 
     * @return byte array
     */
    @Override
    public byte[] getBytes() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a copy of string array.
     * 
     * @return string array
     */
    @Override
    public String[] getStringArray() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a copy of int array.
     * 
     * @return int array
     */
    @Override
    public long[] getIntArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean[] getBooleanArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getByteArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[] getFloatArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void serialize(OutputStream outputStream) {
        try {
            outputStream.write(this.toString().getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            throw new BallerinaException("error occurred while serializing data", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freezeDirect() {
        if (tupleType.isReadOnly()) {
            return;
        }

        this.type = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(this.type);
        this.tupleType = (TupleType) TypeUtils.getReferredType(type);
        for (int i = 0; i < this.size; i++) {
            Object value = this.get(i);
            if (value instanceof RefValue) {
                ((RefValue) value).freezeDirect();
            }
        }
        this.typedesc = createSingletonTypedesc(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IteratorValue getIterator() {
        return new ArrayIterator(this);
    }

    /**
     * Get {@code BType} of the array elements.
     * 
     * @return element type
     */
    @Override
    public Type getElementType() {
        throw new UnsupportedOperationException();
    }

    // Protected methods

    @Override
    protected void resizeInternalArray(int newLength) {
        refValues = Arrays.copyOf(refValues, newLength);
    }

    @Override
    protected void fillValues(int index) {
        if (index <= size) {
            return;
        }

        Type restType = this.tupleType.getRestType();
        if (restType != null) {
            for (int i = size; i < index; i++) {
                this.refValues[i] = restType.getZeroValue();
            }
        }
    }

    @Override
    protected void rangeCheckForGet(long index, int size) {
        rangeCheck(index, size);
        if (index < 0 || index >= size) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    RuntimeErrors.TUPLE_INDEX_OUT_OF_RANGE, index, size);
        }
    }

    @Override
    protected void rangeCheck(long index, int size) {
        if (index > Integer.MAX_VALUE || index < Integer.MIN_VALUE) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    RuntimeErrors.INDEX_NUMBER_TOO_LARGE, index);
        }

        if ((this.tupleType.getRestType() == null && index >= this.maxSize) || (int) index < 0) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    RuntimeErrors.TUPLE_INDEX_OUT_OF_RANGE, index, size);
        }
    }

    @Override
    protected void fillerValueCheck(int index, int size) {
        // if there has been values added beyond the current index, that means filler values
        // has already been checked. Therefore no need to check again.
        if (this.size >= index) {
            return;
        }

        // if the elementType doesn't have an implicit initial value & if the insertion is not a consecutive append
        // to the array, then an exception will be thrown.
        if (!TypeChecker.hasFillerValue(this.tupleType.getRestType()) && (index > size)) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.ILLEGAL_LIST_INSERTION_ERROR,
                    RuntimeErrors.ILLEGAL_TUPLE_INSERTION, size, index + 1);
        }
    }

    /**
     * Same as {@code prepareForAdd}, except fillerValueCheck is not performed as we are guaranteed to add
     * elements to consecutive positions.
     *
     * @param index last index after add operation completes
     * @param currentArraySize current array size
     */
    @Override
    protected void prepareForConsecutiveMultiAdd(long index, int currentArraySize) {
        int intIndex = (int) index;
        rangeCheck(index, size);
        ensureCapacity(intIndex + 1, currentArraySize);
        resetSize(intIndex);
    }

    @Override
    protected void ensureCapacity(int requestedCapacity, int currentArraySize) {
        if (requestedCapacity <= currentArraySize) {
            return;
        }

        // Here the growth rate is 1.5. This value has been used by many other languages
        int newArraySize = currentArraySize + (currentArraySize >> 1);

        // Now get the maximum value of the calculate new array size and request capacity
        newArraySize = Math.max(newArraySize, requestedCapacity);

        // Now get the minimum value of new array size and maximum array size
        newArraySize = Math.min(newArraySize, this.maxSize);
        resizeInternalArray(newArraySize);
    }

    @Override
    protected void checkFixedLength(long length) {
        if (this.tupleType.getRestType() == null) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                    RuntimeErrors.ILLEGAL_TUPLE_SIZE, size, length);
        } else if (this.tupleType.getTupleTypes().size() > length) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                    RuntimeErrors.ILLEGAL_TUPLE_WITH_REST_TYPE_SIZE, this.tupleType.getTupleTypes().size(), length);
        }
    }

    @Override
    protected void unshift(long index, Object[] vals) {
        handleImmutableArrayValue();
        unshiftArray(index, vals.length, getCurrentArrayLength());
        addToRefArray(vals, (int) index);
    }

    // private methods

    private void prepareForAdd(long index, Object value, int currentArraySize) {
        int intIndex = (int) index;
        rangeCheck(index, size);

        // check types
        Type elemType;
        if (index >= this.minSize) {
            elemType = this.tupleType.getRestType();
        } else {
            elemType = this.tupleType.getTupleTypes().get((int) index);
        }

        if (!TypeChecker.checkIsType(value, elemType)) {
            throw ErrorCreator.createError(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INHERENT_TYPE_VIOLATION_ERROR_IDENTIFIER),
                    BLangExceptionHelper.getErrorDetails(RuntimeErrors.INCOMPATIBLE_TYPE, elemType,
                                                         TypeChecker.getType(value)));
        }

        fillerValueCheck(intIndex, size);
        ensureCapacity(intIndex + 1, currentArraySize);
        fillValues(intIndex);
        resetSize(intIndex);
    }

    private void fillRead(long index, int currentArraySize) {
        Type restType = this.tupleType.getRestType();
        if (!TypeChecker.hasFillerValue(restType)) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.ILLEGAL_LIST_INSERTION_ERROR,
                                                           RuntimeErrors.ILLEGAL_TUPLE_INSERTION, size, index + 1);
        }

        int intIndex = (int) index;
        rangeCheck(index, size);
        ensureCapacity(intIndex + 1, currentArraySize);

        for (int i = size; i <= index; i++) {
            this.refValues[i] = restType.getZeroValue();
        }

        resetSize(intIndex);
    }

    private void shiftArray(int index) {
        int nElemsToBeMoved = this.size - 1 - index;
        if (nElemsToBeMoved >= 0) {
            System.arraycopy(this.refValues, index + 1, this.refValues, index, nElemsToBeMoved);
        }
        this.size--;
    }

    private void addToRefArray(Object[] vals, int startIndex) {
        int endIndex = startIndex + vals.length;
        for (int i = startIndex, j = 0; i < endIndex; i++, j++) {
            add(i, vals[j]);
        }
    }

    private void unshiftArray(long index, int unshiftByN, int arrLength) {
        int lastIndex = size() + unshiftByN - 1;
        prepareForConsecutiveMultiAdd(lastIndex, arrLength);
        if (index > lastIndex) {
            throw BLangExceptionHelper.getRuntimeException(
                    getModulePrefixedReason(ARRAY_LANG_LIB, INDEX_OUT_OF_RANGE_ERROR_IDENTIFIER),
                    RuntimeErrors.INDEX_NUMBER_TOO_LARGE, index);
        }

        int i = (int) index;
        System.arraycopy(this.refValues, i, this.refValues, i + unshiftByN, this.size - i);
    }

    private int getCurrentArrayLength() {
        return this.refValues.length;
    }

    private void resetSize(int index) {
        if (index >= size) {
            size = index + 1;
        }
    }
}
