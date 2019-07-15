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
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMProcessingInstruction;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.impl.common.OMChildrenQNameIterator;
import org.apache.axiom.om.impl.common.OMNamespaceImpl;
import org.apache.axiom.om.impl.dom.CommentImpl;
import org.apache.axiom.om.impl.dom.TextImpl;
import org.apache.axiom.om.impl.llom.OMDocumentImpl;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.impl.llom.OMProcessingInstructionImpl;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.XMLValidator;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.freeze.FreezeUtils;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import static org.ballerinalang.jvm.util.BLangConstants.STRING_NULL_VALUE;

/**
 * {@code BXML} represents a single XML item in Ballerina.
 * XML item could be one of:
 * <ul>
 * <li>element</li>
 * <li>text</li>
 * <li>comment</li>
 * <li>processing instruction</li>
 * </ul>
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public final class XMLItem extends XMLValue<OMNode> {

    OMNode omNode;
    private XMLNodeType nodeType;

    /**
     * Create an empty XMLValue.
     */
    public XMLItem() {
        omNode = new OMElementImpl();
        setXMLNodeType();
    }

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
            omNode = XMLFactory.stringToOM(xmlValue);
            setXMLNodeType();
        } catch (Throwable t) {
            handleXmlException("failed to create xml: ", t);
        }
    }

    /**
     * Initialize a {@link XMLItem} from a {@link org.apache.axiom.om.OMNode} object.
     *
     * @param value xml object
     */
    public XMLItem(OMNode value) {
        this.omNode = value;
        setXMLNodeType();
    }

    /**
     * Create a {@link XMLItem} from a {@link InputStream}.
     *
     * @param inputStream Input Stream
     */
    public XMLItem(InputStream inputStream) {
        if (inputStream == null) {
            return;
        }

        try {
            omNode = OMXMLBuilderFactory.createOMBuilder(XMLFactory.STAX_PARSER_CONFIGURATION, inputStream)
                    .getDocumentElement();
            setXMLNodeType();
        } catch (Throwable t) {
            handleXmlException("failed to create xml: ", t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLNodeType getNodeType() {
        return nodeType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return omNode == null;
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
        return new String(nodeType.value());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getElementName() {
        if (nodeType == XMLNodeType.ELEMENT) {
            return new String(((OMElement) omNode).getQName().toString());
        }

        return BTypes.typeString.getEmptyValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTextValue() {
        switch (nodeType) {
            case ELEMENT:
                StringBuilder elementTextBuilder = new StringBuilder();
                Iterator<OMNode> children = ((OMElement) omNode).getChildren();
                while (children.hasNext()) {
                    elementTextBuilder.append(getTextValue(children.next()));
                }
                return elementTextBuilder.toString();
            case TEXT:
                String text = ((OMText) omNode).getText();
                return StringEscapeUtils.escapeXml11(text);
            case COMMENT:
                return BTypes.typeString.getZeroValue();
            case PI:
                return BTypes.typeString.getZeroValue();
            default:
                return BTypes.typeString.getZeroValue();
        }
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
        if (nodeType != XMLNodeType.ELEMENT || localName == null || localName.isEmpty()) {
            return STRING_NULL_VALUE;
        }
        QName attributeName = getQName(localName, namespace, prefix);
        OMAttribute attribute = ((OMElement) omNode).getAttribute(attributeName);

        if (attribute != null) {
            return attribute.getAttributeValue();
        }

        OMNamespace ns = ((OMElement) omNode).findNamespaceURI(localName);
        return ns == null ? STRING_NULL_VALUE : ns.getNamespaceURI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String localName, String namespaceUri, String prefix, String value) {
        if (nodeType != XMLNodeType.ELEMENT) {
            return;
        }

        if (localName == null || localName.isEmpty()) {
            throw BallerinaErrors.createError("localname of the attribute cannot be empty");
        }

        // Validate whether the attribute name is an XML supported qualified name, according to the XML recommendation.
        XMLValidator.validateXMLName(localName);
        XMLValidator.validateXMLName(prefix);

        // If the attribute already exists, update the value.
        OMElement node = (OMElement) omNode;
        QName qname = getQName(localName, namespaceUri, prefix);
        OMAttribute attr = node.getAttribute(qname);
        if (attr != null) {
            attr.setAttributeValue(value);
            return;
        }

        // If the prefix is 'xmlns' then this is a namespace addition
        if (prefix != null && prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            node.declareNamespace(value, localName);
            return;
        }

        createAttribute(localName, namespaceUri, prefix, value, node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapValue<String, ?> getAttributesMap() {
        BXmlAttrMap attrMap = new BXmlAttrMap(this);

        if (nodeType != XMLNodeType.ELEMENT) {
            return attrMap;
        }

        OMNamespace defaultNs = ((OMElement) omNode).getDefaultNamespace();
        String namespaceOfPrefix =
                '{' + (defaultNs == null ? XMLConstants.XMLNS_ATTRIBUTE_NS_URI : defaultNs.getNamespaceURI()) + '}';

        Iterator<OMNamespace> namespaceIterator = ((OMElement) omNode).getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            OMNamespace namespace = namespaceIterator.next();
            String prefix = namespace.getPrefix();
            if (prefix.isEmpty()) {
                continue;
            }
            attrMap.put(namespaceOfPrefix + prefix, new String(namespace.getNamespaceURI()));
        }

        Iterator<OMAttribute> attrIterator = ((OMElement) omNode).getAllAttributes();
        while (attrIterator.hasNext()) {
            OMAttribute attr = attrIterator.next();
            attrMap.put(attr.getQName().toString(), attr.getAttributeValue());
        }

        attrMap.finishConstruction();
        return attrMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(MapValue<String, ?> attributes) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState());
            }
        }

        if (nodeType != XMLNodeType.ELEMENT || attributes == null) {
            return;
        }

        // Remove existing attributes
        OMElement omElement = ((OMElement) omNode);
        Iterator<OMAttribute> attrIterator = omElement.getAllAttributes();
        while (attrIterator.hasNext()) {
            omElement.removeAttribute(attrIterator.next());
        }

        // Remove existing namespace declarations
        Iterator<OMNamespace> namespaceIterator = omElement.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            namespaceIterator.next();
            namespaceIterator.remove();
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
        ArrayValue elementsSeq = new ArrayValue(new BArrayType(BTypes.typeXML));
        switch (nodeType) {
            case ELEMENT:
                elementsSeq.add(0, this);
                break;
            default:
                break;
        }
        return new XMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> elements(String qname) {
        ArrayValue elementsSeq = new ArrayValue(new BArrayType(BTypes.typeXML));
        switch (nodeType) {
            case ELEMENT:
                if (getElementName().toString().equals(getQname(qname).toString())) {
                    elementsSeq.add(0, this);
                }
                break;
            default:
                break;
        }
        return new XMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> children() {
        ArrayValue elementsSeq = new ArrayValue(new BArrayType(BTypes.typeXML));
        switch (nodeType) {
            case ELEMENT:
                Iterator<OMNode> childrenItr = ((OMElement) omNode).getChildren();
                int i = 0;
                while (childrenItr.hasNext()) {
                    elementsSeq.add(i++, new XMLItem(childrenItr.next()));
                }
                break;
            default:
                break;
        }

        return new XMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> children(String qname) {
        ArrayValue elementsSeq = new ArrayValue(new BArrayType(BTypes.typeXML));
        switch (nodeType) {
            case ELEMENT:
                /*
                 * Here we are not using "((OMElement) omNode).getChildrenWithName(qname))" method, since as per the
                 * documentation of AxiomContainer.getChildrenWithName, if the namespace part of the qname is empty, it
                 * will look for the elements which matches only the local part and returns. i.e: It will not match the
                 * namespace. This is not the behavior we want. Hence we are explicitly creating an iterator which
                 * will return elements that will match both namespace and the localName, regardless whether they are
                 * empty or not.
                 */
                Iterator<OMNode> childrenItr =
                        new OMChildrenQNameIterator(((OMElement) omNode).getFirstOMChild(), getQname(qname));
                int i = 0;
                while (childrenItr.hasNext()) {
                    OMNode node = childrenItr.next();
                    elementsSeq.add(i++, new XMLItem(node));
                }
                break;
            default:
                break;
        }
        return new XMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChildren(XMLValue<?> seq) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState());
            }
        }

        if (seq == null) {
            return;
        }

        OMElement currentNode;
        switch (nodeType) {
            case ELEMENT:
                currentNode = ((OMElement) omNode);
                break;
            default:
                throw BallerinaErrors.createError("not an " + XMLNodeType.ELEMENT);
        }

        currentNode.removeChildren();

        if (seq.getNodeType() == XMLNodeType.SEQUENCE) {
            ArrayValue childSeq = ((XMLSequence) seq).value();
            XMLValue<OMNode> child;
            for (int i = 0; i < childSeq.size(); i++) {
                child = (XMLValue<OMNode>) childSeq.getRefValue(i);
                currentNode.addChild(child.value());
            }
        } else {
            currentNode.addChild((OMNode) seq.value());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChildren(XMLValue<?> seq) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState());
            }
        }

        if (seq == null) {
            return;
        }

        OMElement currentNode;
        switch (nodeType) {
            case ELEMENT:
                currentNode = ((OMElement) omNode);
                break;
            default:
                throw BallerinaErrors.createError("not an " + XMLNodeType.ELEMENT);
        }

        if (seq.getNodeType() == XMLNodeType.SEQUENCE) {
            ArrayValue childSeq = ((XMLSequence) seq).value();
            XMLValue<OMNode> child;
            for (int i = 0; i < childSeq.size(); i++) {
                child = (XMLValue<OMNode>) childSeq.getRefValue(i);
                currentNode.addChild(child.value());
            }
        } else {
            currentNode.addChild((OMNode) seq.value());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> strip() {
        if (omNode == null || (nodeType == XMLNodeType.TEXT && ((OMText) omNode).getText().trim().isEmpty())) {
            return new XMLSequence();
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> slice(long startIndex, long endIndex) {
        if (startIndex > 1 || endIndex > 1 || startIndex < -1 || endIndex < -1) {
            throw BallerinaErrors.createError("index out of range: [" + startIndex + "," + endIndex + "]");
        }

        if (startIndex == -1) {
            startIndex = 0;
        }

        if (endIndex == -1) {
            endIndex = 1;
        }

        if (startIndex == endIndex) {
            return new XMLSequence();
        }

        if (startIndex > endIndex) {
            throw BallerinaErrors.createError("invalid indices: " + startIndex + " < " + endIndex);
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> descendants(String qname) {
        List<XMLValue<?>> descendants = new ArrayList<XMLValue<?>>();
        switch (nodeType) {
            case ELEMENT:
                addDescendants(descendants, (OMElement) omNode, getQname(qname).toString());
                break;
            default:
                break;
        }

        XMLValue<?>[] array = descendants.toArray(new XMLValue[descendants.size()]);
        return new XMLSequence(new ArrayValue(array, new BArrayType(BTypes.typeXML)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(OutputStream outputStream) {
        try {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = factory.createXMLStreamWriter(outputStream);
            this.omNode.serializeAndConsume(writer);
        } catch (Throwable t) {
            handleXmlException("error occurred during writing the message to the output stream: ", t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OMNode value() {
        return this.omNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        try {
            switch (nodeType) {
                case COMMENT:
                    return COMMENT_START + ((OMComment) omNode).getValue() + COMMENT_END;
                case TEXT:
                    return ((OMText) omNode).getText();
                case PI:
                    return PI_START + ((OMProcessingInstruction) omNode).getTarget() + " " +
                            ((OMProcessingInstruction) omNode).getValue() + PI_END;
                default:
                    return this.omNode.toString();
            }
        } catch (Throwable t) {
            handleXmlException("failed to get xml as string: ", t);
        }
        return STRING_NULL_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stringValue() {
        try {
            switch (nodeType) {
                case COMMENT:
                    return COMMENT_START + ((OMComment) omNode).getValue() + COMMENT_END;
                case TEXT:
                    return getTextValue(omNode);
                case PI:
                    return PI_START + ((OMProcessingInstruction) omNode).getTarget() + " " +
                            ((OMProcessingInstruction) omNode).getValue() + PI_END;
                default:
                    return this.omNode.toString();
            }
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

        OMNode clonedNode;
        switch (nodeType) {
            case ELEMENT:
                clonedNode = ((OMElement) omNode).cloneOMElement();
                break;
            case TEXT:
                TextImpl text = new TextImpl();
                text.setTextContent(((OMText) omNode).getText());
                clonedNode = text;
                break;
            case COMMENT:
                CommentImpl comment = new CommentImpl();
                comment.setTextContent(((OMComment) omNode).getValue());
                clonedNode = comment;
                break;
            case PI:
                OMProcessingInstructionImpl pi = new OMProcessingInstructionImpl();
                pi.setTarget(((OMProcessingInstruction) omNode).getTarget());
                pi.setValue(((OMProcessingInstruction) omNode).getValue());
                clonedNode = pi;
                break;
            default:
                clonedNode = omNode;
                break;
        }

        // adding the document element as parent, to get xpPaths work
        OMDocument doc = new OMDocumentImpl();
        doc.addChild(clonedNode);
        return new XMLItem(clonedNode);
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
        if (getNodeType() == XMLNodeType.TEXT) {
            String textContent = ((OMText) this.omNode).getText();
            return textContent.codePointCount(0, textContent.length());
        }
        return this.omNode == null ? 0 : 1;
    }

    public int length() {
        return this.omNode == null ? 0 : 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void build() {
        this.omNode.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttribute(String qname) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState());
            }
        }

        if (nodeType != XMLNodeType.ELEMENT || qname.isEmpty()) {
            return;
        }

        OMElement omElement = (OMElement) omNode;
        OMAttribute attribute = omElement.getAttribute(getQname(qname));

        if (attribute == null) {
            return;
        }

        omElement.removeAttribute(attribute);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeChildren(String qname) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState());
            }
        }

        switch (nodeType) {
            case ELEMENT:
                /*
                 * Here we are not using "((OMElement) omNode).getChildrenWithName(qname))" method, since as per the
                 * documentation of AxiomContainer.getChildrenWithName, if the namespace part of the qname is empty, it
                 * will look for the elements which matches only the local part and returns. i.e: It will not match the
                 * namespace. This is not the behavior we want. Hence we are explicitly creating an iterator which
                 * will return elements that will match both namespace and the localName, regardless whether they are
                 * empty or not.
                 */
                Iterator<OMNode> childrenItr =
                        new OMChildrenQNameIterator(((OMElement) omNode).getFirstOMChild(), getQname(qname));
                while (childrenItr.hasNext()) {
                    childrenItr.next();
                    childrenItr.remove();
                }
                break;
            default:
                break;
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freezeDirect() {
        this.freezeStatus.setFrozen();
    }

    // private methods

    private void setXMLNodeType() {
        switch (omNode.getType()) {
            case OMNode.ELEMENT_NODE:
                nodeType = XMLNodeType.ELEMENT;
                break;
            case OMNode.TEXT_NODE:
            case OMNode.SPACE_NODE:
                nodeType = XMLNodeType.TEXT;
                break;
            case OMNode.COMMENT_NODE:
                nodeType = XMLNodeType.COMMENT;
                break;
            case OMNode.PI_NODE:
                nodeType = XMLNodeType.PI;
                break;
            default:
                nodeType = XMLNodeType.SEQUENCE;
                break;
        }
    }

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

    private static class BXmlAttrMap extends MapValueImpl<String, String> {
        
        private final XMLItem bXmlItem;
        private boolean constructed = false;

        BXmlAttrMap(XMLItem bXmlItem) {
            super(new BMapType(BTypes.typeString));
            this.bXmlItem = bXmlItem;
        }

        void finishConstruction() {
            constructed = true;
        }

        @Override
        @SuppressWarnings("unchecked")
        public String put(String key, String value) {
            super.put(key, value);
            if (constructed) {
                setAttribute(key, value);
            }
            return null;
        }

        private void setAttribute(String key, String value) {
            String url = null;
            String localName = key;

            int endOfUrl = key.lastIndexOf('}');
            if (endOfUrl != -1) {
                int startBrace = key.indexOf('{');
                if (startBrace == 0) {
                    url = key.substring(startBrace + 1, endOfUrl);
                    localName = key.substring(endOfUrl + 1);
                }
            }
            bXmlItem.setAttribute(localName, url, null, value);
        }
    }

    @Override
    public IteratorValue getIterator() {
        return new XMLIterator.ItemIterator(this);
    }
}
