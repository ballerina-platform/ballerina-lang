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

    /**
     * Create a type from the given name.
     *
     * @param typeName string name of the type
     */
    BArrayType(String typeName, BType elementType, String pkgPath, SymbolScope symbolScope) {
        super(typeName, pkgPath, symbolScope, BArray.class);
        this.elementType = elementType;
    }

    public BType getElementType() {
        return elementType;
    }

    @SuppressWarnings("unchecked")
    public <V extends BValue> V getDefaultValue() {
        return (V) new BArray<V>(elementType.getValueClass());
    }

    public boolean equals(Object obj) {
        if (obj instanceof BArrayType) {
            BArrayType other = (BArrayType) obj;
            return this.typeName.equals(other.typeName);
        }

        return false;
    }
}
