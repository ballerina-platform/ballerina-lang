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
package org.ballerinalang.jvm.types;

import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;

/**
 * {@code BXMLType} represents an XML Element.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BXMLType extends BType {

    private final int tag;

    /**
     * Create a {@code BXMLType} which represents the boolean type.
     *
     * @param typeName string name of the type
     */
    BXMLType(String typeName, BPackage pkg) {
        super(typeName, pkg, XMLValue.class);
        this.tag = TypeTags.XML_TAG;
    }

    BXMLType(String typeName, BPackage pkg, int tag) {
        super(typeName, pkg, XMLValue.class);
        this.tag = tag;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return (V) new XMLSequence();
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return (V) new XMLSequence();
    }

    @Override
    public int getTag() {
        return this.tag;
    }

    @Override
    public boolean isAnydata() {
        return true;
    }
}
