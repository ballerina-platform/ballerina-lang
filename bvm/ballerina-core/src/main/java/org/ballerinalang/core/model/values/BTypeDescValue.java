/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.core.model.values;

import org.ballerinalang.core.model.types.BArrayType;
import org.ballerinalang.core.model.types.BType;
import org.ballerinalang.core.model.types.BTypes;

import java.util.Map;

/**
 * Ballerina Value that encapsulates a Ballerina Type.
 *
 * @since 0.90
 */
public class BTypeDescValue implements BRefType<BType> {

    private BType typeValue;

    public BTypeDescValue() {
        this(null);
    }

    public BTypeDescValue(BType typeValue) {
        this.typeValue = typeValue;
    }

    @Override
    public BType value() {
        return typeValue;
    }

    @Override
    public String stringValue() {
        return typeValue.toString();
    }

    @Override
    public BType getType() {
        return BTypes.typeDesc;
    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        return new BTypeDescValue(typeValue);
    }

    public boolean equals(Object obj) {
        // if obj == 'null' or not instance of BTypeValue - return false
        if (obj == null || !(obj instanceof BTypeDescValue)) {
            return false;
        }

        BTypeDescValue typeValue = (BTypeDescValue) obj;
        if ((typeValue.value() instanceof BArrayType) &&
                (this.value() instanceof BArrayType)) {
            BArrayType objArrayType = (BArrayType) typeValue.value();
            BArrayType thisArrayType = (BArrayType) this.value();
            if (objArrayType.getDimensions() != thisArrayType.getDimensions()) {
                return false;
            }
            return (new BTypeDescValue(thisArrayType.getElementType()))
                    .equals(new BTypeDescValue(objArrayType.getElementType()));
        } else {
            return typeValue.value() == this.value();
        }
    }

}
