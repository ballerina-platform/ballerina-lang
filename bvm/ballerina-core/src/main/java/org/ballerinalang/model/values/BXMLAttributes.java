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
package org.ballerinalang.model.values;

import org.ballerinalang.bre.bvm.BVM;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

import java.util.List;
import java.util.Map;

/**
 * The {@code BString} represents a XML attributes map in ballerina, denoted by 'foo@', where foo is an xml variable.
 *
 * @since 0.89
 */
public final class BXMLAttributes implements BRefType {

    private BXML value;

    /**
     * Create attribute map with an XML.
     *
     * @param value XML associated with this attributes
     */
    public BXMLAttributes(BXML value) {
        this.value = value;
    }

    @Override
    public String stringValue() {
        return value().stringValue();
    }

    @Override
    public BType getType() {
        return BTypes.typeXMLAttributes;
    }

    @Override
    public void stamp(BType type, List<BVM.TypeValuePair> unresolvedValues) {

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof BXMLAttributes)) {
            return false;
        }
        return ((BXMLAttributes) obj).stringValue().equals(value.stringValue());
    }

    @Override
    public BMap value() {
        return this.value.getAttributesMap();
    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        if (isFrozen()) {
            return this;
        }

        return new BXMLAttributes(value);
    }
}
