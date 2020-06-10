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

package org.ballerinalang.jvm.values;

import org.apache.axiom.om.OMNode;
import org.ballerinalang.jvm.BallerinaXMLSerializer;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.values.api.BMap;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BXML;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

import static org.ballerinalang.jvm.util.BLangConstants.STRING_NULL_VALUE;

/**
 * Functionality common to PI, COMMENT and TEXT nodes.
 *
 * @since 1.2.0
 */
public abstract class XMLNonElementItem extends XMLValue {

    @Override
    public boolean isSingleton() {
        return false;
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
    public void setAttributes(BMap<BString, ?> attributes) {

    }

    @Override
    public BXML elements() {
        return new XMLSequence();
    }

    @Override
    public BXML elements(String qname) {
        return null;
    }

    @Override
    public XMLValue children() {
        return new XMLSequence();
    }

    @Override
    public XMLValue children(String qname) {
        return new XMLSequence();
    }

    @Override
    public void setChildren(BXML seq) {
    }

    @Override
    @Deprecated
    public void addChildren(BXML seq) {
    }

    @Override
    public BXML strip() {
        return new XMLText("");
    }

    @Override
    public abstract XMLNodeType getNodeType();

    @Override
    public BXML slice(long startIndex, long endIndex) {
        return null;
    }

    @Override
    public BXML descendants(List<String> qnames) {
        return new XMLSequence();
    }

    @Override
    public XMLValue getItem(int index) {
        if (index == 0) {
            return this;
        }
        return new XMLSequence();
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
    public String stringValue() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BallerinaXMLSerializer ballerinaXMLSerializer = new BallerinaXMLSerializer(outputStream);
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
    public String toString() {
        return this.stringValue();
    }

    @Override
    public void freezeDirect() {
        this.type = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(this.type);
    }

    @Override
    public abstract boolean equals(Object obj);

    @Override
    protected void setAttributesOnInitialization(BMap<BString, ?> attributes) {
    }

    @Override
    protected void setAttributeOnInitialization(String localName, String namespace, String prefix, String value) {
    }
}
