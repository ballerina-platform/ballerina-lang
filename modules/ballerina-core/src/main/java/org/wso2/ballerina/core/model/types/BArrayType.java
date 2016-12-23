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
package org.wso2.ballerina.core.model.types;

import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code BArrayType} represents a type of an array in Ballerina
 * <p>
 * Arrays are defined using the array constructor [] as follows:
 * TypeName[]
 * <p>
 * All arrays are unbounded in length and support 0 based indexing.
 *
 * @param <T> Type of the values in the array
 * @since 1.0.0
 */
public class BArrayType<T extends BType> extends BType {

    private T elementType;

    private BValue[] valueArray;

    /**
     * Create a type from the given name
     *
     * @param typeName string name of the type
     */
    @SuppressWarnings("unchecked")
    BArrayType(String typeName, String elementType) {
        super(typeName);
        this.elementType = BTypes.getType(elementType);
    }

    public BType getElementType() {
        return elementType;
    }

    @SuppressWarnings("unchecked")
    public <U extends BValue> U[] createArray() {
        // TODO
        return null;
    }

    @SuppressWarnings("unchecked")
    public <V extends BValue> V getDefaultValue() {
        valueArray = elementType.createArray();
        return (V) new BArray(valueArray);
    }

    public boolean equals(Object obj) {
        if (obj instanceof BArrayType) {
            BArrayType other = (BArrayType) obj;
            return this.elementType.equals(other.elementType);
        }

        return false;
    }

}
