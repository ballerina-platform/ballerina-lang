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
package org.ballerinalang.jvm.types;

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.commons.ArrayState;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;

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
public class BArrayType extends BType {
    private BType elementType;
    private int dimensions = 1;
    private int size = -1;
    private boolean hasFillerValue;
    private ArrayState state = ArrayState.UNSEALED;

    private final boolean readonly;
    private BIntersectionType immutableType;

    public BArrayType(BType elementType) {
        super(null, null, ArrayValue.class);
        this.elementType = elementType;
        if (elementType instanceof BArrayType) {
            dimensions = ((BArrayType) elementType).getDimensions() + 1;
        }
        hasFillerValue = TypeChecker.hasFillerValue(this.elementType);
        this.readonly = false;
    }

    public BArrayType(BType elemType, int size) {
        this(elemType, size, false);
    }

    public BArrayType(BType elemType, int size, boolean readonly) {
        super(null, null, ArrayValue.class);
        this.elementType = elemType;
        if (elementType instanceof BArrayType) {
            dimensions = ((BArrayType) elementType).getDimensions() + 1;
        }
        if (size != -1) {
            state = ArrayState.CLOSED_SEALED;
            this.size = size;
        }
        hasFillerValue = TypeChecker.hasFillerValue(this.elementType);
        this.readonly = readonly;
    }

    public BType getElementType() {
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
            if (other.state == ArrayState.CLOSED_SEALED && this.size != other.size) {
                return false;
            }
            return this.elementType.equals(other.elementType) && this.readonly == other.readonly;
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        BType tempElementType = elementType;
        sb.append(getSizeString());
        while (tempElementType.getTag() == TypeTags.ARRAY_TAG) {
            BArrayType arrayElement = (BArrayType) tempElementType;
            sb.append(arrayElement.getSizeString());
            tempElementType = arrayElement.elementType;
        }
        if (tempElementType.getTag() == TypeTags.UNION_TAG) {
            sb.insert(0, "(" + tempElementType.toString() + ")").toString();
        } else {
            sb.insert(0, tempElementType.toString()).toString();
        }
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
        return this.elementType.isPureType();
    }

    @Override
    public boolean isReadOnly() {
        return this.readonly;
    }

    @Override
    public BType getImmutableType() {
        return this.immutableType;
    }

    @Override
    public void setImmutableType(BIntersectionType immutableType) {
        this.immutableType = immutableType;
    }
}
