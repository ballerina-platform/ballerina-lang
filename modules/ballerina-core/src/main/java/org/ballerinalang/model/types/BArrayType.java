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
package org.ballerinalang.model.types;

import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BValue;

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

    /**
     * Create a type from the given name.
     *
     * @param typeName string name of the type
     */
    BArrayType(String typeName, BType elementType, String pkgPath, SymbolScope symbolScope, int dimensions) {
        super(typeName, pkgPath, symbolScope, BArray.class);
        this.elementType = elementType;
        this.dimensions = dimensions;
    }

    public BArrayType(BType elementType) {
        super(null, null, null, BArray.class);
        this.elementType = elementType;
        if (elementType instanceof BArrayType) {
            dimensions = ((BArrayType) elementType).getDimensions() + 1;
        }
    }

    public BType getElementType() {
        return elementType;
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        BArray emptyVal = new BArray<V>(elementType.getValueClass());
        emptyVal.setType(this);
        return (V) emptyVal;
    }

    @Override
    public TypeSignature getSig() {
        return new TypeSignature(TypeSignature.SIG_ARRAY, elementType.getSig());
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
            return this.elementType.equals(other.elementType);
        }

        return false;
    }

    @Override
    public String toString() {
        return elementType + "[]";
    }

    public int getDimensions() {
        return this.dimensions;
    }
}
