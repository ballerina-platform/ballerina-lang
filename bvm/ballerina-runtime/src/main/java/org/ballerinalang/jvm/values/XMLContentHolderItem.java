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
import org.apache.axiom.om.impl.llom.CharacterDataImpl;
import org.apache.axiom.om.impl.llom.OMCommentImpl;
import org.apache.axiom.om.impl.llom.OMProcessingInstructionImpl;
import org.ballerinalang.jvm.BallerinaXMLSerializer;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.api.BXml;
import org.ballerinalang.jvm.values.freeze.Status;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.ballerinalang.jvm.util.BLangConstants.STRING_NULL_VALUE;

/**
 * XML nodes containing atomic content such as text, comment and processing instructions.
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
        if (getNodeType() == XMLNodeType.TEXT) {
            return getData().isEmpty();
        }
        return false;
    }

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
        return "";
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
        return new XMLSequence();
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
        if (getNodeType() == XMLNodeType.TEXT && !getData().trim().isEmpty()) {
            return this;
        }
        return new XMLContentHolderItem("", XMLNodeType.TEXT);
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
            if (this.type == XMLNodeType.PI) {
                OMProcessingInstructionImpl pi = new OMProcessingInstructionImpl();
                pi.setTarget(this.target);
                pi.setValue(this.data);
                return pi;
            } else if (this.type == XMLNodeType.COMMENT) {
                OMCommentImpl omComment = new OMCommentImpl();
                omComment.setValue(this.data);
                return omComment;
            }
            CharacterDataImpl characterData = new CharacterDataImpl();
            characterData.data = this.data;
            return characterData;
    }

    @Override
    public IteratorValue getIterator() {
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
                    return data;
                } else {
                    throw new NoSuchElementException();
                }
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
            if (outputStream instanceof BallerinaXMLSerializer) {
                ((BallerinaXMLSerializer) outputStream).write(this);
            } else {
                (new BallerinaXMLSerializer(outputStream)).write(this);
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
