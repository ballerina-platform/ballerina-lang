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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.ballerinalang.jvm.StaxXMLSink;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.api.BXml;
import org.ballerinalang.jvm.values.freeze.Status;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import static org.ballerinalang.jvm.util.BLangConstants.STRING_NULL_VALUE;

/** XML nodes containing atomic content
 * such as text, comment and processing instructions.
 *
 * @since 1.1.0
 */
public class XMLContentHolderItem extends XMLValue {

    private String data;
    private String target; // only applicable for PI nodes
    private XMLNodeType type;

    public XMLContentHolderItem(String data, XMLNodeType contentType) {
        // data is the content of xml comment or text node
        this.data = data;
        this.type = contentType;
    }

    public XMLContentHolderItem(String data, String target) {
        this.data = data;
        this.target = target;
        this.type = XMLNodeType.PI;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public String getItemType() {
        return null;
    }

    @Override
    public String getElementName() {
        return null;
    }

    @Override
    public String getTextValue() {
        if (type == XMLNodeType.TEXT) {
            return getData();
        }
        return "";
    }

    @Override
    public String getAttribute(String localName, String namespace) {
        return null;
    }

    @Override
    public String getAttribute(String localName, String namespace, String prefix) {
        return null;
    }

    @Override
    public void setAttribute(String localName, String namespace, String prefix, String value) {

    }

    public String getData() {
        return data;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public MapValue<String, String> getAttributesMap() {
        return null;
    }

    @Override
    public void setAttributes(MapValue<String, ?> attributes) {

    }

    @Override
    public BXml elements() {
        return null;
    }

    @Override
    public BXml elements(String qname) {
        return null;
    }

    @Override
    public XMLValue children() {
        return new XMLSequence();
    }

    @Override
    public XMLValue children(String qname) {
        return null;
    }



    @Override
    public void setChildren(BXml seq) {
        // no op;
    }

    @Override
    public void addChildren(BXml seq) {
        // no op;
    }

    @Override
    public BXml strip() {
        return null;
    }

    @Override
    public XMLNodeType getNodeType() {
        return type;
    }

    @Override
    public BXml slice(long startIndex, long endIndex) {
        return null;
    }

    @Override
    public BXml descendants(String qname) {
        return null;
    }

    @Override
    public XMLValue getItem(int index) {
        return null;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        // XMLContentHolderItem is immutable
        return this;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void build() {

    }

    @Override
    public void removeAttribute(String qname) {

    }

    @Override
    public void removeChildren(String qname) {

    }

    @Override
    public OMNode value() {
        try {
            // todo: we gonna need to handle this as stringToOM returns a element node
            // what we need is all element, comments, pi, etc.
            String xmlFragment = this.stringValue();
            OMElement omElement = XMLFactory.stringToOM("<root>" + xmlFragment + "</root>");
            return (OMNode) omElement.getChildren().next();
        } catch (XMLStreamException e) {
            throw new BallerinaException(e);
        }
    }

    @Override
    public IteratorValue getIterator() {
        return null;
    }

    @Override
    public String stringValue() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StaxXMLSink staxXMLSink = new StaxXMLSink(outputStream);
            staxXMLSink.write(this);
            staxXMLSink.flush();
            String str = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            staxXMLSink.close();
            return str;
        } catch (Throwable t) {
            handleXmlException("failed to get xml as string: ", t);
        }
        return STRING_NULL_VALUE;
    }

    @Override
    public String stringValue(Strand strand) {
        return stringValue();
    }

    @Override
    public String toString() {
        return this.stringValue();
    }

    @Override
    public void attemptFreeze(Status freezeStatus) {

    }

    @Override
    public void freezeDirect() {

    }

    @Override
    public Object freeze() {
        return null;
    }

    @Override
    public void serialize(OutputStream outputStream) {
        try {
            if (outputStream instanceof StaxXMLSink) {
                ((StaxXMLSink) outputStream).write(this);
            } else {
                (new StaxXMLSink(outputStream)).write(this);
            }
        } catch (Throwable t) {
            handleXmlException("error occurred during writing the message to the output stream: ", t);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof XMLContentHolderItem) {
            XMLContentHolderItem that = (XMLContentHolderItem) obj;
            if (that.getNodeType() != this.getNodeType()) {
                return false;
            }
            switch (this.getNodeType()) {
                case TEXT:
                case COMMENT:
                    return this.getData().equals(that.getData());
                case PI:
                    return this.getData().equals(that.getData()) && this.getTarget().equals(that.getTarget());
            }

        }
        return false;
    }
}
