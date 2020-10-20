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
package io.ballerina.runtime.values;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXMLQName;

import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Represents an XML qualified name in ballerina.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 * 
 * @since 0.995.0
 */
public final class XMLQName implements RefValue, BXMLQName {

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
    @Deprecated
    public XMLQName(String localName, String uri, String prefix) {
        this.localName = localName;
        this.uri = uri;
        this.prefix = prefix;
    }

    @Deprecated
    public XMLQName(String qNameStr) {
        int parenEndIndex = qNameStr.indexOf('}');
        if (qNameStr.startsWith("{") && parenEndIndex > 0) {
            localName = qNameStr.substring(parenEndIndex + 1);
            uri = qNameStr.substring(1, parenEndIndex);
        } else {
            localName = qNameStr;
            uri = null;
        }
    }

    @Deprecated
    public XMLQName(BString localName, BString uri, BString prefix) {
        this.localName = localName.getValue();
        if (uri != null) {
            this.uri = uri.getValue();
        }
        if (prefix != null) {
            this.prefix = prefix.getValue();
        }
    }

    @Deprecated
    public XMLQName(BString qNameStrVal) {
        this(qNameStrVal.getValue());
    }

    @Override
    public String toString() {
        return stringValue(null);
    }

    @Override
    public String stringValue(BLink parent) {
        return (uri == null || uri.isEmpty()) ? localName : '{' + uri + '}' + localName;
    }

    @Override
    public String informalStringValue(BLink parent) {
        return "`" + toString() + "`";
    }

    @Override
    public String expressionStringValue(BLink parent) {
        return "xml`" + toString() + "`";
    }

    @Override
    public Type getType() {
        return PredefinedTypes.TYPE_XML_ATTRIBUTES;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof XMLQName)) {
            return false;
        }

        return obj.toString().equals(localName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localName, uri);
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        if (isFrozen()) {
            return this;
        }

        return new XMLQName(localName, uri, prefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        XMLQName copy = (XMLQName) copy(refs);
        if (!copy.isFrozen()) {
            copy.freezeDirect();
        }
        return copy;
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
}
