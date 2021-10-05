/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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
package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;

import java.util.Optional;

/**
 * {@code BArrayType} represents a type of an arrays in Ballerina.
 * <p>
 * Arrays are defined using the arrays constructor [] as follows:
 * TypeName[]
 * <p>
 * All arrays are unbounded in length and support 0 based indexing.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BArrayType extends BType implements ArrayType {
    private Type elementType;
    private int dimensions = 1;
    private int size = -1;
    private boolean hasFillerValue;
    private ArrayState state = ArrayState.OPEN;

    private final boolean readonly;
    private IntersectionType immutableType;
    private IntersectionType intersectionType = null;
    private int typeFlags;
    public BArrayType(Type elementType) {
        this(elementType, false);
    }

    public BArrayType(Type elementType, boolean readonly) {
        this(elementType, -1, readonly);
    }

    public BArrayType(Type elemType, int size) {
        this(elemType, size, false);
    }

    public BArrayType(Type elemType, int size, boolean readonly) {
        this(0, size, readonly, TypeChecker.hasFillerValue(elemType));
        setElementType(elemType);
        setFlagsBasedOnElementType();
    }

    public BArrayType(int typeFlags, int size, boolean readonly, boolean hasFillerValue) {
        super(null, null, ArrayValue.class);
        this.typeFlags = typeFlags;
        if (size != -1) {
            state = ArrayState.CLOSED;
            this.size = size;
        }
        this.readonly = readonly;
        this.hasFillerValue = hasFillerValue;
    }

    public void setElementType(Type elementType) {
        this.elementType = readonly ? ReadOnlyUtils.getReadOnlyType(elementType) : elementType;
        if (elementType instanceof BArrayType) {
            this.dimensions = ((BArrayType) elementType).getDimensions() + 1;
        }
        setFlagsBasedOnElementType();
        int elementTypeTag = elementType.getTag();
        if (elementTypeTag == TypeTags.UNION_TAG || elementTypeTag == TypeTags.FINITE_TYPE_TAG){
            this.hasFillerValue = TypeChecker.hasFillerValue(elementType);
        }
    }

    private void setFlagsBasedOnElementType() {
        if (elementType.isNilable()) {
            this.typeFlags = TypeFlags.addToMask(this.typeFlags, TypeFlags.NILABLE);
        }
        if (elementType.isAnydata()) {
            this.typeFlags = TypeFlags.addToMask(this.typeFlags, TypeFlags.ANYDATA);
        }
        if (elementType.isPureType()) {
            this.typeFlags = TypeFlags.addToMask(this.typeFlags, TypeFlags.PURETYPE);
        }
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        if (size == -1) {
            return getEmptyValue();
        }

        int tag = elementType.getTag();
        switch (tag) {
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.STRING_TAG:
            case TypeTags.BYTE_TAG:
            case TypeTags.DECIMAL_TAG:
                return (V) new ArrayValueImpl(new BArrayType(elementType), size);
            case TypeTags.ARRAY_TAG: // fall through
            default:
                return (V) new ArrayValueImpl(this);
        }
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        int tag = elementType.getTag();
        switch (tag) {
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.STRING_TAG:
            case TypeTags.BYTE_TAG:
                return (V) new ArrayValueImpl(new BArrayType(elementType));
            default:
                return (V) new ArrayValueImpl(this);
        }
    }

    @Override
    public int getTag() {
        return TypeTags.ARRAY_TAG;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BArrayType) {
            BArrayType other = (BArrayType) obj;
            if (other.state == ArrayState.CLOSED && this.size != other.size) {
                return false;
            }
            return this.elementType.equals(other.elementType) && this.readonly == other.readonly;
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Type tempElementType = elementType;
        sb.append(getSizeString());
        while (tempElementType.getTag() == TypeTags.ARRAY_TAG) {
            BArrayType arrayElement = (BArrayType) tempElementType;
            sb.append(arrayElement.getSizeString());
            tempElementType = arrayElement.elementType;
        }
        sb.insert(0, tempElementType);
        return !readonly ? sb.toString() : sb.append(" & readonly").toString();
    }

    private String getSizeString() {
        return size != -1 ? "[" + size + "]" : "[]";
    }

    public int getDimensions() {
        return this.dimensions;
    }

    public int getSize() {
        return size;
    }

    public boolean hasFillerValue() {
        return hasFillerValue;
    }

    public ArrayState getState() {
        return state;
    }

    @Override
    public boolean isAnydata() {
        return TypeFlags.isFlagOn(this.typeFlags, TypeFlags.ANYDATA);
    }

    @Override
    public boolean isReadOnly() {
        return this.readonly;
    }

    @Override
    public IntersectionType getImmutableType() {
        return this.immutableType;
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        this.immutableType = immutableType;
    }

    @Override
    public Optional<IntersectionType> getIntersectionType() {
        return this.intersectionType ==  null ? Optional.empty() : Optional.of(this.intersectionType);
    }

    @Override
    public void setIntersectionType(IntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }
}
