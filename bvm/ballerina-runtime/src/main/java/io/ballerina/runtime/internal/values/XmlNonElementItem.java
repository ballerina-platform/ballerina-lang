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

import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlNonElementItem;
import io.ballerina.runtime.internal.BallerinaXmlSerializer;
import org.apache.axiom.om.OMNode;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

import static io.ballerina.runtime.api.constants.RuntimeConstants.STRING_NULL_VALUE;
import static io.ballerina.runtime.internal.ValueUtils.createSingletonTypedesc;

/**
 * Functionality common to PI, COMMENT and TEXT nodes.
 *
 * @since 1.2.0
 */
public abstract class XmlNonElementItem extends XmlValue implements BXmlNonElementItem {

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public String getItemType() {
        return getNodeType().value();
    }

    @Override
    public String getElementName() {
        return null;
    }

    @Override
    public BString getAttribute(String localName, String namespace) {
        return null;
    }

    @Override
    public BString getAttribute(String localName, String namespace, String prefix) {
        return null;
    }

    @Override
    public void setAttribute(String localName, String namespace, String prefix, String value) {

    }

    @Override
    public MapValue<BString, BString> getAttributesMap() {
        return null;
    }

    @Override
    public void setAttributes(BMap<BString, BString> attributes) {

    }

    @Override
    public BXml elements() {
        return new XmlSequence();
    }

    @Override
    public BXml elements(String qname) {
        return null;
    }

    @Override
    public XmlValue children() {
        return new XmlSequence();
    }

    @Override
    public XmlValue children(String qname) {
        return new XmlSequence();
    }

    @Override
    public void setChildren(BXml seq) {
    }

    @Override
    @Deprecated
    public void addChildren(BXml seq) {
    }

    @Override
    public BXml strip() {
        return new XmlText("");
    }

    @Override
    public abstract XmlNodeType getNodeType();

    @Override
    public BXml slice(long startIndex, long endIndex) {
        return null;
    }

    @Override
    public BXml descendants(List<String> qnames) {
        return new XmlSequence();
    }

    @Override
    public BXml descendants() {
        return new XmlSequence();
    }

    @Override
    public XmlValue getItem(int index) {
        if (index == 0) {
            return this;
        }
        return new XmlSequence();
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public void build() {

    }

    @Override
    public void removeAttribute(String qname) {

    }

    @Override
    @Deprecated
    public void removeChildren(String qname) {

    }

    @Override
    public abstract OMNode value();

    @Override
    public IteratorValue getIterator() {
        return new IteratorValue() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Object next() {
                throw new NoSuchElementException();
            }
        };
    }

    @Override
    public String stringValue(BLink parent) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BallerinaXmlSerializer ballerinaXMLSerializer = new BallerinaXmlSerializer(outputStream);
            ballerinaXMLSerializer.write(this);
            ballerinaXMLSerializer.flush();
            String str = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            ballerinaXMLSerializer.close();
            return str;
        } catch (Throwable t) {
            handleXmlException("failed to get xml as string: ", t);
        }
        return STRING_NULL_VALUE;
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
    public String toString() {
        return this.stringValue(null);
    }

    @Override
    public void freezeDirect() {
        this.type = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(this.type);
        this.typedesc = createSingletonTypedesc(this);
    }

    @Override
    public abstract boolean equals(Object obj);

    @Override
    protected void setAttributesOnInitialization(BMap<BString, BString> attributes) {
    }

    @Override
    protected void setAttributeOnInitialization(String localName, String namespace, String prefix, String value) {
    }
}
