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

package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;

import java.util.List;
import java.util.Map;

/**
 * Ballerina Value that encapsulates a Ballerina Type.
 *
 * @since 0.995.0
 */
public class BTypeDescValue implements RefValue {

    private BType typeValue;

    public BTypeDescValue() {
        this(null);
    }

    public BTypeDescValue(BType typeValue) {
        this.typeValue = typeValue;
    }

    public BType value() {
        return typeValue;
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public BType getType() {
        return BTypes.typeDesc;
    }

    @Override
    public void stamp(BType type, List<TypeValuePair> unresolvedValues) {
        
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return new BTypeDescValue(typeValue);
    }

    public boolean equals(Object obj) {
        // if obj == 'null' or not instance of BTypeValue - return false
        if (!(obj instanceof BTypeDescValue)) {
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
