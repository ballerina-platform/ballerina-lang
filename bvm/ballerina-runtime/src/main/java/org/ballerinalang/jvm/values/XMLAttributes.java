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
package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;

import java.util.List;
import java.util.Map;

/**
 * The {@code BXMLAttributes} represents a XML attributes map in ballerina, denoted by 'foo@',
 * where foo is an xml variable.
 *
 * @since 0.995.0
 */
public final class XMLAttributes implements RefValue {

    private XMLValue<?> value;

    /**
     * Create attribute map with an XML.
     *
     * @param value XML associated with this attributes
     */
    public XMLAttributes(XMLValue<?> value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public String stringValue() {
        return value.stringValue();
    }

    @Override
    public BType getType() {
        return BTypes.typeXMLAttributes;
    }

    @Override
    public void stamp(BType type, List<TypeValuePair> unresolvedValues) {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof XMLAttributes)) {
            return false;
        }
        return obj.toString().equals(value.toString());
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        if (isFrozen()) {
            return this;
        }

        return new XMLAttributes(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        XMLAttributes copy = (XMLAttributes) copy(refs);
        if (!copy.isFrozen()) {
            copy.freezeDirect();
        }
        return copy;
    }
}
