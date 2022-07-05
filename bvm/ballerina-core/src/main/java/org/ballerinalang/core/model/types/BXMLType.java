/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.core.model.types;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BXML;
import org.ballerinalang.core.model.values.BXMLSequence;

/**
 * {@code BXMLType} represents an XML Element.
 *
 * @since 0.8.0
 */
public class BXMLType extends BType {

    /**
     * Create a {@code BXMLType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    BXMLType(String typeName, String pkgPath) {
        super(typeName, pkgPath, BXML.class);
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return (V) new BXMLSequence();
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return (V) new BXMLSequence();
    }

    @Override
    public int getTag() {
        return TypeTags.XML_TAG;
    }
}
