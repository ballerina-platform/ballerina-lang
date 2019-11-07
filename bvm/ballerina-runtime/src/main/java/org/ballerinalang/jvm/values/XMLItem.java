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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMDocType;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMProcessingInstruction;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.common.OMChildrenQNameIterator;
import org.apache.axiom.om.impl.common.OMNamespaceImpl;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.XMLValidator;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.freeze.FreezeUtils;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import static org.ballerinalang.jvm.XMLNodeType.ELEMENT;
import static org.ballerinalang.jvm.util.BLangConstants.STRING_NULL_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.XML_LANG_LIB;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.KEY_NOT_FOUND_ERROR_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * todo: rewrite the doc
 * {@code BXML} represents a single XML item in Ballerina.
 * XML item could be one of:
 * <ul>
 * <li>element</li>
 * <li>text</li>
 * <li>comment</li>
 * <li>processing instruction</li>
 * </ul>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p> 
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public final class XMLItem extends XMLValue<OMNode> {

    private final String XMLNS_URL_PREFIX = "{" + XMLConstants.XMLNS_ATTRIBUTE_NS_URI + "}";
    private QName name;
    XMLSequence children;
    MapValue<String, String> attributes = new MapValueImpl<>();

    /**
     * Initialize a {@link XMLItem} from a XML string.
     *
     * @param xmlValue A XML string
     */
    public XMLItem(String xmlValue) {
        if (xmlValue == null) {
            return;
        }

        try {
            // Use XMLFactory to create nodes.
            assert false;
            //omNode = XMLFactory.stringToOM(xmlValue);
//            setXMLNodeType();
        } catch (Throwable t) {
            handleXmlException("failed to create xml: ", t);
        }
    }


    public XMLItem(QName name, XMLSequence children) {
        this.name = name;
        this.children = children;
    }

    /**
     * Initialize a {@link XMLItem} from a {@link org.apache.axiom.om.OMNode} object.
     *
     * @param value xml object
     */
    public XMLItem(OMNode value) {
        assert false;
        //this.omNode = value;
//        setXMLNodeType();
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
        return children.isEmpty();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTextValue() {
        return "<emtpy/>";
//        switch (nodeType) {
//            case ELEMENT:
//                StringBuilder elementTextBuilder = new StringBuilder();
//                Iterator<OMNode> children = ((OMElement) omNode).getChildren();
//                while (children.hasNext()) {
//                    elementTextBuilder.append(getTextValue(children.next()));
//                }
//                return elementTextBuilder.toString();
//            case TEXT:
//                String text = ((OMText) omNode).getText();
//                return StringEscapeUtils.escapeXml11(text);
//            case COMMENT:
//                return BTypes.typeString.getZeroValue();
//            case PI:
//                return BTypes.typeString.getZeroValue();
//            default:
//                return BTypes.typeString.getZeroValue();
//        }
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
            return attributes.get("{" + ns + "}" + localName);
        }
        if (namespace != null && !namespace.isEmpty()) {
            return attributes.get("{" + namespace + "}" + localName);
        }
        return attributes.get(localName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String localName, String namespaceUri, String prefix, String value) {
        // todo: need to handle fronzen values


        if (localName == null || localName.isEmpty()) {
            throw BallerinaErrors.createError("localname of the attribute cannot be empty");
        }

        // Validate whether the attribute name is an XML supported qualified name, according to the XML recommendation.
        XMLValidator.validateXMLName(localName);
        XMLValidator.validateXMLName(prefix);

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
    public MapValue<String, ?> getAttributesMap() {
        return this.attributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(MapValue<String, ?> attributes) {
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
    public XMLValue<?> elements() {
        ArrayList<XMLValue<?>> children = new ArrayList<>();
        children.add(this);
        return new XMLSequence(children);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> elements(String qname) {
        ArrayList<XMLValue<?>> children = new ArrayList<>();
        if (getElementName().equals(getQname(qname).toString())) {
            children.add(this);
        }
        return new XMLSequence(children);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> children() {
        return children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> children(String qname) {
        assert false;
        return null;
//        ArrayValue elementsSeq = new ArrayValue(new BArrayType(BTypes.typeXML));
//        switch (nodeType) {
//            case ELEMENT:
//                /*
//                 * Here we are not using "((OMElement) omNode).getChildrenWithName(qname))" method, since as per the
//                 * documentation of AxiomContainer.getChildrenWithName, if the namespace part of the qname is empty, it
//                 * will look for the elements which matches only the local part and returns. i.e: It will not match the
//                 * namespace. This is not the behavior we want. Hence we are explicitly creating an iterator which
//                 * will return elements that will match both namespace and the localName, regardless whether they are
//                 * empty or not.
//                 */
//                Iterator<OMNode> childrenItr =
//                        new OMChildrenQNameIterator(((OMElement) omNode).getFirstOMChild(), getQname(qname));
//                int i = 0;
//                while (childrenItr.hasNext()) {
//                    OMNode node = childrenItr.next();
//                    elementsSeq.add(i++, new XMLItem(node));
//                }
//                break;
//            default:
//                break;
//        }
//        return new XMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    // todo: hmmm name says setChildren, but what this does is addChildren o.O
    @Override
    public void setChildren(XMLValue<?> seq) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), XML_LANG_LIB);
            }
        }

        if (seq == null) {
            return;
        }

        // todo: move not an element error to xml sequnce and holder node.
//        OMElement currentNode;
//        switch (nodeType) {
//            case ELEMENT:
//                currentNode = ((OMElement) omNode);
//                break;
//            default:
//                throw BallerinaErrors.createError("not an " + XMLNodeType.ELEMENT);
//        }

        if (seq.getNodeType() == XMLNodeType.SEQUENCE) {
            children.sequenceX.addAll(((XMLSequence) seq).sequenceX);
        } else {
            children.sequenceX.add(seq);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChildren(XMLValue<?> seq) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), XML_LANG_LIB);
            }
        }

        if (seq == null) {
            return;
        }

        if (seq.getNodeType() == XMLNodeType.SEQUENCE) {
            children.sequenceX.addAll(((XMLSequence) seq).sequenceX);
        } else {
            children.sequenceX.add(seq);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> strip() {
        children.strip();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> slice(long startIndex, long endIndex) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> descendants(String qname) {
        List<XMLValue<?>> descendants = new ArrayList<XMLValue<?>>();
        for (XMLValue<?> item : children.sequenceX) {
            if (item.getNodeType() == ELEMENT
                    && ((XMLItem) item).name.toString().equals(qname)) {
                descendants.add(item);
            }
        }

        return new XMLSequence(descendants);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(OutputStream outputStream) {
        try {
            // implement serialization
            assert false;
//            if (this.omNode.getType() == OMNode.ELEMENT_NODE) {
//                // not using the xml-factory here because of the namespace serializing issues.
//                this.omNode.serializeAndConsume(outputStream);
//            } else {
//                XMLOutputFactory factory = XMLOutputFactory.newInstance();
//                XMLStreamWriter writer = factory.createXMLStreamWriter(outputStream);
//                this.omNode.serializeAndConsume(writer);
//            }
        } catch (Throwable t) {
            handleXmlException("error occurred during writing the message to the output stream: ", t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OMNode value() {
        assert false;
        return null; //this.omNode;
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
    public String stringValue(Strand strand) {
        try {
            return "<to>string</to>";

//            switch (this.omNode.getType()) {
//                case OMNode.TEXT_NODE:
//                case OMNode.SPACE_NODE:
//                    return ((OMText) omNode).getText();
//                case OMNode.COMMENT_NODE:
//                    return COMMENT_START + ((OMComment) omNode).getValue() + COMMENT_END;
//                case OMNode.PI_NODE:
//                    return PI_START + ((OMProcessingInstruction) omNode).getTarget() + " " +
//                            ((OMProcessingInstruction) omNode).getValue() + PI_END;
//                case OMNode.DTD_NODE:
//                    return ((OMDocType) omNode).getInternalSubset();
//                case OMNode.ELEMENT_NODE:
//                    // fall through
//                default:
//                    return this.omNode.toString();
//            }
        } catch (Throwable t) {
            handleXmlException("failed to get xml as string: ", t);
        }
        return STRING_NULL_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object copy(Map<Object, Object> refs) {
        if (isFrozen()) {
            return this;
        }

        return new XMLItem(new QName(name.getNamespaceURI(), name.getLocalPart(), name.getPrefix()),
                (XMLSequence) children.copy(refs));
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
    public XMLValue<?> getItem(int index) {
        if (index != 0) {
            throw BallerinaErrors.createError("index out of range: index: " + index + ", size: 1");
        }

        return this;
    }

    public int size() {
//        if (getNodeType() == XMLNodeType.TEXT) {
//            String textContent = ((OMText) this.omNode).getText();
//            return textContent.codePointCount(0, textContent.length());
//        }
//        return this.omNode == null ? 0 : 1;
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
        //this.omNode.build();
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

        this.children.sequenceX.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void attemptFreeze(Status freezeStatus) {
        if (FreezeUtils.isOpenForFreeze(this.freezeStatus, freezeStatus)) {
            this.freezeStatus = freezeStatus;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freezeDirect() {
        this.freezeStatus.setFrozen();
    }

    // private methods
//
//    private void setXMLNodeType() {
//        switch (omNode.getType()) {
//            case OMNode.ELEMENT_NODE:
//                nodeType = XMLNodeType.ELEMENT;
//                break;
//            case OMNode.TEXT_NODE:
//            case OMNode.SPACE_NODE:
//                nodeType = XMLNodeType.TEXT;
//                break;
//            case OMNode.COMMENT_NODE:
//                nodeType = XMLNodeType.COMMENT;
//                break;
//            case OMNode.PI_NODE:
//                nodeType = XMLNodeType.PI;
//                break;
//            default:
//                nodeType = XMLNodeType.SEQUENCE;
//                break;
//        }
//    }

    private String getTextValue(OMNode node) {
        switch (node.getType()) {
            case OMNode.ELEMENT_NODE:
                StringBuilder sb = new StringBuilder();
                Iterator<OMNode> children = ((OMElement) node).getChildren();
                while (children.hasNext()) {
                    sb.append(getTextValue(children.next()));
                }
                return sb.toString();
            case OMNode.TEXT_NODE:
                return ((OMText) node).getText();
            case OMNode.COMMENT_NODE:
                return STRING_NULL_VALUE;
            case OMNode.PI_NODE:
                return STRING_NULL_VALUE;
            default:
                return STRING_NULL_VALUE;
        }
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

    private void createAttribute(String localName, String namespaceUri, String prefix, String value, OMElement node) {
        // If the namespace is null/empty, only the local part exists. Therefore add a simple attribute.
        if (namespaceUri == null || namespaceUri.isEmpty()) {
            node.addAttribute(localName, value, null);
            return;
        }

        if (!(prefix == null || prefix.isEmpty())) {
            OMNamespace existingNs = node.findNamespaceURI(prefix);

            // If a namespace exists with the same prefix but a different uri, then do not add the new attribute.
            if (existingNs != null && !namespaceUri.equals(existingNs.getNamespaceURI())) {
                throw BallerinaErrors
                        .createError("failed to add attribute '" + prefix + ":" + localName + "'. prefix '" + prefix +
                                "' is already bound to namespace '" + existingNs.getNamespaceURI() + "'");
            }

            node.addAttribute(localName, value, new OMNamespaceImpl(namespaceUri, prefix));
            return;
        }

        // We reach here if the namespace prefix is null/empty, and a namespace uri exists.
        // Find a prefix that has the same namespaceUri, out of the defined namespaces
        Iterator<String> prefixes = node.getNamespaceContext(false).getPrefixes(namespaceUri);
        if (prefixes.hasNext()) {
            prefix = prefixes.next();
            if (prefix.isEmpty()) {
                node.addAttribute(localName, value, null);
                return;
            }
            if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                // If found, and if its the default namespace, add a namespace decl
                node.declareNamespace(value, localName);
                return;
            }
        }

        // Else use the prefix. If the prefix is null, a random prefix will be generated.
        node.addAttribute(localName, value, new OMNamespaceImpl(namespaceUri, prefix));
    }

    @Override
    public IteratorValue getIterator() {
        return new XMLIterator.ItemIterator(this);
    }

//    /**
//     * A map value class implementation backed by an XML.
//     */
//    private static class XMLAttributeMap extends MapValueImpl<String, String> {
//
//        private final XMLItem bXmlItem;
//
//        XMLAttributeMap(XMLItem bXmlItem) {
//            super(new BMapType(BTypes.typeString));
//            this.bXmlItem = bXmlItem;
//        }
//
//        @Override
//        public String put(String key, String value) {
//            String url = null;
//            String localName = key;
//
//            int endOfUrl = key.lastIndexOf('}');
//            if (endOfUrl != -1) {
//                int startBrace = key.indexOf('{');
//                if (startBrace == 0) {
//                    url = key.substring(startBrace + 1, endOfUrl);
//                    localName = key.substring(endOfUrl + 1);
//                }
//            }
//
//            this.bXmlItem.setAttribute(localName, url, null, value);
//            return null;
//        }
//
//        @Override
//        public String get(Object key) {
//            String name = (String) key;
//            String namespace = null;
//            String localName = name;
//
//            int endOfUrl = name.lastIndexOf('}');
//            if (endOfUrl != -1) {
//                int startBrace = name.indexOf('{');
//                if (startBrace == 0) {
//                    namespace = name.substring(startBrace + 1, endOfUrl);
//                    localName = name.substring(endOfUrl + 1);
//                }
//            }
//            return this.bXmlItem.getAttribute(localName, namespace);
//        }
//
//        @Override
//        public Object copy(Map<Object, Object> refs) {
//            MapValue attrMap = new MapValueImpl(new BMapType(BTypes.typeString));
//            if (this.bXmlItem.nodeType != ELEMENT) {
//                return attrMap;
//            }
//
//            String namespaceOfPrefix = getNamespaceOfPrefix();
//            Iterator<OMNamespace> namespaceIterator = ((OMElement) this.bXmlItem.omNode).getAllDeclaredNamespaces();
//            while (namespaceIterator.hasNext()) {
//                OMNamespace namespace = namespaceIterator.next();
//                String prefix = namespace.getPrefix();
//                if (prefix.isEmpty()) {
//                    continue;
//                }
//                attrMap.put(namespaceOfPrefix + prefix, namespace.getNamespaceURI());
//            }
//
//            Iterator<OMAttribute> attrIterator = ((OMElement) this.bXmlItem.omNode).getAllAttributes();
//            while (attrIterator.hasNext()) {
//                OMAttribute attr = attrIterator.next();
//                attrMap.put(attr.getQName().toString(), attr.getAttributeValue());
//            }
//
//            return attrMap;
//        }
//
//        @Override
//        public String stringValue() {
//            StringJoiner sj = new StringJoiner(" ");
//            if (this.bXmlItem.nodeType != ELEMENT) {
//                return "{}";
//            }
//
//            String namespaceOfPrefix = getNamespaceOfPrefix();
//            Iterator<OMNamespace> namespaceIterator = ((OMElement) this.bXmlItem.omNode).getAllDeclaredNamespaces();
//            while (namespaceIterator.hasNext()) {
//                OMNamespace namespace = namespaceIterator.next();
//                String prefix = namespace.getPrefix();
//                if (prefix.isEmpty()) {
//                    continue;
//                }
//                sj.add(namespaceOfPrefix + prefix + "=" + namespace.getNamespaceURI());
//            }
//
//            Iterator<OMAttribute> attrIterator = ((OMElement) this.bXmlItem.omNode).getAllAttributes();
//            while (attrIterator.hasNext()) {
//                OMAttribute attr = attrIterator.next();
//                sj.add(attr.getQName().toString() + "=" + attr.getAttributeValue());
//            }
//
//            return sj.toString();
//        }
//
//        @Override
//        public Set<String> keySet() {
//            Set<String> keys = new LinkedHashSet<>();
//            if (this.bXmlItem.nodeType != ELEMENT) {
//                return keys;
//            }
//
//            String namespaceOfPrefix = getNamespaceOfPrefix();
//            Iterator<OMNamespace> namespaceIterator = ((OMElement) this.bXmlItem.omNode).getAllDeclaredNamespaces();
//            while (namespaceIterator.hasNext()) {
//                OMNamespace namespace = namespaceIterator.next();
//                String prefix = namespace.getPrefix();
//                if (prefix.isEmpty()) {
//                    continue;
//                }
//                keys.add(namespaceOfPrefix + prefix);
//            }
//
//            Iterator<OMAttribute> attrIterator = ((OMElement) this.bXmlItem.omNode).getAllAttributes();
//            while (attrIterator.hasNext()) {
//                OMAttribute attr = attrIterator.next();
//                keys.add(attr.getQName().toString());
//            }
//
//            return keys;
//        }
//
//        @Override
//        public String[] getKeys() {
//            Set<String> keys = this.keySet();
//            return keys.toArray(new String[keys.size()]);
//        }
//
//        @Override
//        public int size() {
//            int size = 0;
//            Iterator<OMNamespace> namespaceIterator = ((OMElement) this.bXmlItem.omNode).getAllDeclaredNamespaces();
//            Iterator<OMAttribute> attrIterator = ((OMElement) this.bXmlItem.omNode).getAllAttributes();
//            while (namespaceIterator.hasNext()) {
//                if (namespaceIterator.next().getPrefix().isEmpty()) {
//                    continue;
//                }
//                size++;
//            }
//            while (attrIterator.hasNext()) {
//                attrIterator.next();
//                size++;
//            }
//            return size;
//        }
//
//        @Override
//        public String getOrThrow(Object key) {
//            String value = this.get(key);
//            if (value == null) {
//                throw BallerinaErrors.createError(getModulePrefixedReason(XML_LANG_LIB, KEY_NOT_FOUND_ERROR_IDENTIFIER),
//                                                  "cannot find key '" + key + "'");
//            }
//            return value;
//        }
//
//        @Override
//        public String fillAndGet(Object key) {
//            String value = this.get(key);
//            if (value == null) {
//                value = BTypes.typeString.getZeroValue();
//                this.put((String) key, value);
//            }
//            return value;
//        }
//
//        @Override
//        public boolean containsKey(Object key) {
//            return this.get(key) == null;
//        }
//
//        @Override
//        public Collection<String> values() {
//            List<String> values = new ArrayList<>();
//            if (this.bXmlItem.nodeType != ELEMENT) {
//                return values;
//            }
//
//            Iterator<OMNamespace> namespaceIterator = ((OMElement) this.bXmlItem.omNode).getAllDeclaredNamespaces();
//            while (namespaceIterator.hasNext()) {
//                OMNamespace namespace = namespaceIterator.next();
//                String prefix = namespace.getPrefix();
//                if (prefix.isEmpty()) {
//                    continue;
//                }
//                values.add(namespace.getNamespaceURI());
//            }
//
//            Iterator<OMAttribute> attrIterator = ((OMElement) this.bXmlItem.omNode).getAllAttributes();
//            while (attrIterator.hasNext()) {
//                OMAttribute attr = attrIterator.next();
//                values.add(attr.getAttributeValue());
//            }
//
//            return values;
//        }
//
//        @Override
//        public boolean isEmpty() {
//            return this.size() == 0;
//        }
//
//        @Override
//        public String toString() {
//            return this.stringValue();
//        }
//
//        @Override
//        public String remove(Object key) {
//            String attr = this.get(key);
//            this.bXmlItem.removeAttribute((String) key);
//            return attr;
//        }
//
//        @Override
//        public Object frozenCopy(Map<Object, Object> refs) {
//            XMLAttributeMap copy = new XMLAttributeMap((XMLItem) bXmlItem.copy(refs));
//            if (!copy.isFrozen()) {
//                copy.freezeDirect();
//            }
//            return copy;
//        }
//
//        @Override
//        public String stringValue(Strand strand) {
//            return stringValue();
//        }
//
//        @Override
//        public synchronized void attemptFreeze(Status freezeStatus) {
//            this.bXmlItem.attemptFreeze(freezeStatus);
//        }
//
//        @Override
//        public void freezeDirect() {
//            this.bXmlItem.freezeDirect();
//        }
//
//        @Override
//        public synchronized boolean isFrozen() {
//            return this.bXmlItem.isFrozen();
//        }
//
//        // private methods
//        private String getNamespaceOfPrefix() {
//            OMNamespace defaultNs = ((OMElement) this.bXmlItem.omNode).getDefaultNamespace();
//            String namespaceOfPrefix =
//                    '{' + (defaultNs == null ? XMLConstants.XMLNS_ATTRIBUTE_NS_URI : defaultNs.getNamespaceURI()) + '}';
//            return namespaceOfPrefix;
//        }
//    }

}
