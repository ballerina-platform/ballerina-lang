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

import org.ballerinalang.core.model.types.BType;
import org.ballerinalang.core.model.types.BTypes;

import java.util.Map;

/**
 * Represents a XML qualified name in ballerina.
 * 
 * @since 0.89
 */
public final class BXMLQName implements BRefType {

    private String localName;
    private String uri;
    private String prefix;

    /**
     * Create attribute map with an XML.
     * 
     * @param localName Local part of the qualified name
     * @param uri Namespace URI
     * @param prefix Namespace prefix
     */
    public BXMLQName(String localName, String uri, String prefix) {
        this.localName = localName;
        this.uri = uri;
        this.prefix = prefix;
    }

    @Override
    public String stringValue() {
        return (uri == null || uri.isEmpty()) ? localName : '{' + uri + '}' + localName;
    }

    @Override
    public BType getType() {
        return BTypes.typeString;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof BXMLQName)) {
            return false;
        }
        return ((BXMLQName) obj).stringValue().equals(localName);
    }

    @Override
    public String value() {
        return null;
    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        if (isFrozen()) {
            return this;
        }

        return new BXMLQName(localName, uri, prefix);
    }
    

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return stringValue();
    }
}
