/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.jvm.values;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNode;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaXMLSerializer;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.XMLValidator;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.api.BMap;
import org.ballerinalang.jvm.values.api.BXML;
import org.ballerinalang.jvm.values.freeze.FreezeUtils;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static org.ballerinalang.jvm.XMLNodeType.ELEMENT;
import static org.ballerinalang.jvm.XMLNodeType.TEXT;
import static org.ballerinalang.jvm.util.BLangConstants.STRING_NULL_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.XML_LANG_LIB;

/**
 * {@code XMLItem} represents a single XML element in Ballerina.
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 * @since 0.995.0
 */
public final class XMLItem extends XMLValue {

    public static final String XMLNS_URL_PREFIX = "{" + XMLConstants.XMLNS_ATTRIBUTE_NS_URI + "}";
    private QName name;
    private XMLSequence children;
    private MapValue<String, String> attributes;

    public XMLItem(QName name, XMLSequence children) {
        this.name = name;
        this.children = children;
        attributes = new MapValueImpl<>(new BMapType(BTypes.typeString));
    }

    /**
     * Initialize a {@link XMLItem} from a {@link org.apache.axiom.om.OMNode} object.
     *
     * @param name element's qualified name
     */
    public XMLItem(QName name) {
        this(name, new XMLSequence(new ArrayList<>()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLNodeType getNodeType() {
        return ELEMENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getItemType() {
        return getNodeType().value();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getElementName() {
        return name.toString();
    }

    public QName getQName() {
        return this.name;
    }

    public void setQName(QName name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTextValue() {
        return children.getTextValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttribute(String localName, String namespace) {
        return getAttribute(localName, namespace, XMLConstants.DEFAULT_NS_PREFIX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttribute(String localName, String namespace, String prefix) {
        if (prefix != null && !prefix.isEmpty()) {
            String ns = attributes.get(XMLNS_URL_PREFIX + prefix);
            String attrVal = attributes.get("{" + ns + "}" + localName);
            if (attrVal != null) {
                return attrVal;
            }
        }
        if (namespace != null && !namespace.isEmpty()) {
            return attributes.get("{" + namespace + "}" + localName);
        }
        String defaultNS = attributes.get("{http://www.w3.org/2000/xmlns/}xmlns");
        if (defaultNS != null) {
            return attributes.get("{" + defaultNS + "}" + localName);
        }
        return attributes.get(localName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String localName, String namespaceUri, String prefix, String value) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), XML_LANG_LIB);
            }
        }

        if (localName == null || localName.isEmpty()) {
            throw BallerinaErrors.createError("localname of the attribute cannot be empty");
        }

        // Validate whether the attribute name is an XML supported qualified name, according to the XML recommendation.
        XMLValidator.validateXMLName(localName);
        XMLValidator.validateXMLName(prefix);

        // JVM codegen uses prefix == 'xmlns' and namespaceUri == null to denote namespace decl at runtime.
        // 'localName' will contain the namespace name where as 'value' will contain the namespace URI
        // todo: Fix this so that namespaceURI points to XMLConstants.XMLNS_ATTRIBUTE_NS_URI
        //  and remove this special case
        if ((namespaceUri == null && prefix != null && prefix.equals(XMLConstants.XMLNS_ATTRIBUTE))
            || localName.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            String nsNameDecl = "{" + XMLConstants.XMLNS_ATTRIBUTE_NS_URI + "}" + localName;
            attributes.put(nsNameDecl, value);
            return;
        }

        String nsOfPrefix = attributes.get(XMLNS_URL_PREFIX + prefix);
        if (namespaceUri != null && nsOfPrefix != null && !namespaceUri.equals(nsOfPrefix)) {
            String errorMsg = String.format(
                    "failed to add attribute '%s:%s'. prefix '%s' is already bound to namespace '%s'",
                    prefix, localName, prefix, nsOfPrefix);
            throw BallerinaErrors.createError(errorMsg);
        }

        if ((namespaceUri == null || namespaceUri.isEmpty())) {
            String ns = attributes.get("{" + XMLConstants.XMLNS_ATTRIBUTE_NS_URI + "}" + XMLConstants.XMLNS_ATTRIBUTE);
            if (ns != null) {
                namespaceUri = ns;
            }
        }

        // If the attribute already exists, update the value.
        QName qname = getQName(localName, namespaceUri, prefix);
        attributes.put(qname.toString(), value);


        // If the prefix is 'xmlns' then this is a namespace addition
        if (prefix != null && prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            String xmlnsPrefix = "{" + XMLConstants.XMLNS_ATTRIBUTE_NS_URI + "}" + prefix;
            attributes.put(xmlnsPrefix, namespaceUri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapValue<String, String> getAttributesMap() {
        return this.attributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(BMap<String, ?> attributes) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), XML_LANG_LIB);
            }
        }

        String localName, uri;
        for (String qname : attributes.getKeys()) {
            if (qname.startsWith("{") && qname.indexOf('}') > 0) {
                localName = qname.substring(qname.indexOf('}') + 1, qname.length());
                uri = qname.substring(1, qname.indexOf('}'));
            } else {
                localName = qname;
                uri = STRING_NULL_VALUE;
            }

            // Validate whether the attribute name is an XML supported qualified name,
            // according to the XML recommendation.
            XMLValidator.validateXMLName(localName);
            setAttribute(localName, uri, STRING_NULL_VALUE, attributes.get(qname).toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue elements() {
        ArrayList<BXML> children = new ArrayList<>();
        children.add(this);
        return new XMLSequence(children);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue elements(String qname) {
        ArrayList<BXML> children = new ArrayList<>();
        if (getElementName().equals(getQname(qname).toString())) {
            children.add(this);
        }
        return new XMLSequence(children);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue children() {
        return new XMLSequence(new ArrayList<>(children.getChildrenList()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue children(String qname) {
        return children.elements(qname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChildren(BXML seq) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), XML_LANG_LIB);
            }
        }

        if (seq == null) {
            return;
        }

        if (seq.getNodeType() == XMLNodeType.SEQUENCE) {
            children = (XMLSequence) seq;
        } else {
            children = new XMLSequence();
            children.children.add(seq);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public void addChildren(BXML seq) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN || children.freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), XML_LANG_LIB);
            }
        }

        if (seq == null) {
            return;
        }

        List<BXML> leftList = new ArrayList<>(children.children);

        if (seq.getNodeType() == XMLNodeType.SEQUENCE) {
            List<BXML> appendingList = ((XMLSequence) seq).getChildrenList();
            if (isLastChildIsTextNode(leftList) && !appendingList.isEmpty()
                    && appendingList.get(0).getNodeType() == TEXT) {
                mergeAdjoiningTextNodesIntoList(leftList, appendingList);
            } else {
                leftList.addAll(appendingList);
            }
        } else {
            leftList.add(seq);
        }
        this.children = new XMLSequence(leftList);
    }

    private void mergeAdjoiningTextNodesIntoList(List leftList, List<BXML> appendingList) {
        XMLPi lastChild = (XMLPi) leftList.get(leftList.size() - 1);
        String firstChildContent = ((XMLPi) appendingList.get(0)).getData();
        String mergedTextContent = lastChild.getData() + firstChildContent;
        XMLText text = new XMLText(mergedTextContent);
        leftList.set(leftList.size() - 1, text);
        for (int i = 1; i < appendingList.size(); i++) {
            leftList.add(appendingList.get(i));
        }
    }

    private boolean isLastChildIsTextNode(List<BXML> childList) {
        return !childList.isEmpty() && childList.get(childList.size() - 1).getNodeType() == TEXT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue strip() {
        children.strip();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue slice(long startIndex, long endIndex) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue descendants(List<String> qnames) {
        if (qnames.contains(getQName().toString())) {
            List<BXML> descendants = Arrays.asList(this);
            addDescendants(descendants, this, qnames);
            return new XMLSequence(descendants);
        }
        return children.descendants(qnames);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public OMNode value() {
        try {
            String xmlStr = this.stringValue();
            OMElement omElement = XMLFactory.stringToOM(xmlStr);
            return omElement;
        } catch (ErrorValue e) {
            throw e;
        } catch (OMException | XMLStreamException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw BallerinaErrors.createError(cause.getMessage());
        } catch (Throwable e) {
            throw BallerinaErrors.createError("failed to parse xml: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.stringValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stringValue() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BallerinaXMLSerializer ballerinaXMLSerializer = new BallerinaXMLSerializer(outputStream);
            ballerinaXMLSerializer.write(this);
            ballerinaXMLSerializer.flush();
            String xml = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            ballerinaXMLSerializer.close();
            return xml;
        } catch (Throwable t) {
            handleXmlException("failed to get xml as string: ", t);
        }
        return STRING_NULL_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object copy(Map<Object, Object> refs) {
        if (isFrozen()) {
            return this;
        }

        QName elemName = new QName(this.name.getNamespaceURI(), this.name.getLocalPart(), this.name.getPrefix());
        XMLItem xmlItem = new XMLItem(elemName, (XMLSequence) children.copy(refs));

        MapValue<String, String> attributesMap = xmlItem.getAttributesMap();
        MapValue<String, String> copy = (MapValue<String, String>) this.getAttributesMap().copy(refs);
        if (attributesMap instanceof MapValueImpl) {
            MapValueImpl<String, String> map = (MapValueImpl<String, String>) attributesMap;
            map.putAll((Map<String, String>) copy);
        } else {
            for (Map.Entry<String, String> entry : copy.entrySet()) {
                attributesMap.put(entry.getKey(), entry.getValue());
            }
        }

        if (getAttributesMap().isFrozen()) {
            attributesMap.freeze();
        }
        return xmlItem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        XMLItem copy = (XMLItem) copy(refs);
        if (!copy.isFrozen()) {
            copy.freezeDirect();
        }
        return copy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue getItem(int index) {
        if (index != 0) {
            throw BallerinaErrors.createError("index out of range: index: " + index + ", size: 1");
        }

        return this;
    }

    public int size() {
        return 1;
    }

    public int length() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void build() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttribute(String qname) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), XML_LANG_LIB);
            }
        }

        attributes.remove(qname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeChildren(String qname) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), XML_LANG_LIB);
            }
        }

        List<BXML> children = this.children.children;
        List<Integer> toRemove = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {
            BXML child = children.get(i);
            if (child.getNodeType() == ELEMENT && ((XMLItem) child).getElementName().equals(qname)) {
                toRemove.add(i);
            }
        }

        Collections.reverse(toRemove);
        for (Integer index : toRemove) {
            children.remove(index.intValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void attemptFreeze(Status freezeStatus) {
        if (FreezeUtils.isOpenForFreeze(this.freezeStatus, freezeStatus)) {
            this.freezeStatus = freezeStatus;
        }
        this.attributes.attemptFreeze(freezeStatus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freezeDirect() {
        this.freezeStatus.setFrozen();
        this.attributes.freezeDirect();
    }

    private QName getQName(String localName, String namespaceUri, String prefix) {
        QName qname;
        if (prefix != null) {
            qname = new QName(namespaceUri, localName, prefix);
        } else {
            qname = new QName(namespaceUri, localName);
        }
        return qname;
    }

    public XMLSequence getChildrenSeq() {
        return children;
    }

    @Override
    public IteratorValue getIterator() {
        XMLItem that = this;
        return new IteratorValue() {
            boolean read = false;

            @Override
            public boolean hasNext() {
                return !read;
            }

            @Override
            public Object next() {
                if (read) {
                    throw new NoSuchElementException();
                }
                read = true;
                return that;
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof XMLItem) {
            XMLItem that = (XMLItem) obj;
            boolean qNameEquals = that.getQName().equals(this.getQName());
            if (!qNameEquals) {
                return false;
            }

            boolean attrMapEquals = that.attributes.entrySet().equals(this.attributes.entrySet());
            if (!attrMapEquals) {
                return false;
            }

            return that.children.equals(this.children);
        }
        if (obj instanceof XMLSequence) {
            XMLSequence other = (XMLSequence) obj;
            if (other.children.size() == 1 && this.equals(other.children.get(0))) {
                return true;
            }
        }
        return false;
    }
}
