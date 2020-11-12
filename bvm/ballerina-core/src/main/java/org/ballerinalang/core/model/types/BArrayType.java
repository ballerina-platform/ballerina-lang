/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.core.model.types;

import org.ballerinalang.core.model.values.BNewArray;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.wso2.ballerinalang.compiler.util.BArrayState;

/**
 * {@code BArrayType} represents a type of an arrays in Ballerina.
 * <p>
 * Arrays are defined using the arrays constructor [] as follows:
 * TypeName[]
 * <p>
 * All arrays are unbounded in length and support 0 based indexing.
 *
 * @since 0.8.0
 */
public class BArrayType extends BType implements BIndexedType {
    private BType elementType;
    private int dimensions = 1;
    private int size = -1;
    private BArrayState state = BArrayState.OPEN;

    public BArrayType(BType elementType) {
        super(null, null, BNewArray.class);
        this.elementType = elementType;
        if (elementType instanceof BArrayType) {
            dimensions = ((BArrayType) elementType).getDimensions() + 1;
        }
    }

    public BArrayType(BType elemType, int size) {
        super(null, null, BNewArray.class);
        this.elementType = elemType;
        if (elementType instanceof BArrayType) {
            dimensions = ((BArrayType) elementType).getDimensions() + 1;
        }
        if (size != -1) {
            state = BArrayState.CLOSED;
            this.size = size;
        }
    }

    public BType getElementType() {
        return elementType;
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        if (size != -1) {
            int tag = elementType.getTag();
            switch (tag) {
                case TypeTags.INT_TAG:
                case TypeTags.FLOAT_TAG:
                case TypeTags.DECIMAL_TAG:
                case TypeTags.BOOLEAN_TAG:
                case TypeTags.STRING_TAG:
                case TypeTags.BYTE_TAG:
                    return (V) new BValueArray(elementType, size);
                case TypeTags.ARRAY_TAG: // fall through
                default:
                    return (V) new BValueArray(this);
            }
        } else {
            return getEmptyValue();
        }
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        int tag = elementType.getTag();
        switch (tag) {
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.STRING_TAG:
            case TypeTags.BYTE_TAG:
                return (V) new BValueArray(elementType);
            default:
                return (V) new BValueArray(this);
        }
    }

    @Override
    public int getTag() {
        return TypeTags.ARRAY_TAG;
    }

    @Override
    public int hashCode() {
        return super.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BArrayType) {
            BArrayType other = (BArrayType) obj;
            if (other.state == BArrayState.CLOSED && this.size != other.size) {
                return false;
            }
            return this.elementType.equals(other.elementType);
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(elementType.toString());
        if (sb.indexOf("[") != -1) {
            return size != -1 ?
                    sb.insert(sb.indexOf("["), "[" + size + "]").toString() :
                    sb.insert(sb.indexOf("["), "[]").toString();
        } else {
            return size != -1 ? sb.append("[").append(size).append("]").toString() : sb.append("[]").toString();
        }
    }

    public int getDimensions() {
        return this.dimensions;
    }

    public int getSize() {
        return size;
    }

    public BArrayState getState() {
        return state;
    }
}
