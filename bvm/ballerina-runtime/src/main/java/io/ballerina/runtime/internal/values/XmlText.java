/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.values.BXml;
import org.apache.axiom.om.OMNode;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * XML nodes containing atomic content such as text, comment and processing instructions.
 *
 * @since 1.2.0
 */
public class XmlText extends XmlNonElementItem {

    private String data;

    public XmlText(String data) {
        // data is the content of xml comment or text node
        this.data = data;
        this.type = data.isEmpty() ? PredefinedTypes.TYPE_XML_NEVER : PredefinedTypes.TYPE_TEXT;
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean isSingleton() {
        return !isEmpty();
    }

    @Override
    public String getItemType() {
        return getNodeType().value();
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public String getTextValue() {
        return data;
    }

    @Override
    public BXml strip() {
        if (!data.trim().isEmpty()) {
            return this;
        }
        return new XmlText("");
    }

    @Override
    public XmlNodeType getNodeType() {
        return XmlNodeType.TEXT;
    }

    @Override
    public OMNode value() {
        return this.factory.createOMText(this.data);
    }

    @Override
    public IteratorValue getIterator() {
        XmlText that = this;
        return new IteratorValue() {
            boolean read = false;
            @Override
            public boolean hasNext() {
                return !read;
            }

            @Override
            public Object next() {
                if (!read) {
                    this.read = true;
                    return that;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    /**
     * Deep equality check for XML Text.
     *
     * @param o The XML Text to be compared
     * @param visitedValues Visited values in previous recursive calls
     * @return True if the XML Texts are equal; False otherwise
     */
    @Override
    public boolean equals(Object o, Set<ValuePair> visitedValues) {
        if (o instanceof XmlText rhsXMLText) {
            return this.getTextValue().equals(rhsXMLText.getTextValue());
        }
        return this.getType() == PredefinedTypes.TYPE_XML_NEVER && (o instanceof XmlSequence xmlSequence) &&
                xmlSequence.getChildrenList().isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public Type getType() {
        return this.type;
    }
}
