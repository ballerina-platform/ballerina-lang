/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.core.model.values;

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
import org.apache.axiom.om.impl.llom.CharacterDataImpl;
import org.apache.axiom.om.impl.llom.OMCommentImpl;
import org.apache.axiom.om.impl.llom.OMDocumentImpl;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.impl.llom.OMProcessingInstructionImpl;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.core.model.types.BMapType;
import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.model.util.XMLNodeType;
import org.ballerinalang.core.model.util.XMLUtils;
import org.ballerinalang.core.model.util.XMLValidationUtils;
import org.ballerinalang.core.util.exceptions.BallerinaException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import static org.ballerinalang.core.util.BLangConstants.STRING_NULL_VALUE;

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
 * @since 0.88
 */
public final class BXMLItem extends BXML<OMNode> {

    private OMNode omNode;
    private XMLNodeType nodeType;

    /**
     * Create an empty XMLValue.
     */
    public BXMLItem() {
        omNode = new OMElementImpl();
        setXMLNodeType();
    }

    /**
     * Initialize a {@link BXMLItem} from a XML string.
     *
     * @param xmlValue A XML string
     */
    public BXMLItem(String xmlValue) {
        if (xmlValue == null) {
            return;
        }

        try {
            omNode = XMLUtils.stringToOM(xmlValue);
            setXMLNodeType();
        } catch (Throwable t) {
            handleXmlException("failed to create xml: ", t);
        }
    }

    /**
     * Initialize a {@link BXMLItem} from a {@link org.apache.axiom.om.OMNode} object.
     *
     * @param value xml object
     */
    public BXMLItem(OMNode value) {
        this.omNode = value;
        setXMLNodeType();
    }

    /**
     * Create a {@link BXMLItem} from a {@link InputStream}.
     *
     * @param inputStream Input Stream
     */
    public BXMLItem(InputStream inputStream) {
        if (inputStream == null) {
            return;
        }

        try {
            omNode = OMXMLBuilderFactory.createOMBuilder(XMLUtils.STAX_PARSER_CONFIGURATION,
                    inputStream).getDocumentElement();
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
    public BBoolean isEmpty() {
        return new BBoolean(omNode == null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BBoolean isSingleton() {
        return new BBoolean(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BString getItemType() {
        return new BString(nodeType.value());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BString getElementName() {
        if (nodeType == XMLNodeType.ELEMENT) {
            return new BString(((OMElement) omNode).getQName().toString());
        }

        return BTypes.typeString.getEmptyValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BString getTextValue() {
        return new BString(getTextValue(omNode));
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
            throw new BallerinaException("localname of the attribute cannot be empty");
        }

        // Validate whether the attribute name is an XML supported qualified name, according to the XML recommendation.
        XMLValidationUtils.validateXMLName(localName);
        XMLValidationUtils.validateXMLName(prefix);

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
                throw new BallerinaException("failed to add attribute '" + prefix + ":" + localName + "'. prefix '" +
                        prefix + "' is already bound to namespace '" + existingNs.getNamespaceURI() + "'");
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

    /**
     * {@inheritDoc}
     */
    @Override
    public BMap<?, ?> getAttributesMap() {
        BXmlAttrMap attrMap = new BXmlAttrMap(this);

        if (nodeType != XMLNodeType.ELEMENT) {
            return attrMap;
        }

        OMNamespace defaultNs = ((OMElement) omNode).getDefaultNamespace();
        String namespaceOfPrefix = '{' + (defaultNs == null ? XMLConstants.XMLNS_ATTRIBUTE_NS_URI :
                defaultNs.getNamespaceURI()) + '}';

        Iterator<OMNamespace> namespaceIterator = ((OMElement) omNode).getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            OMNamespace namespace = namespaceIterator.next();
            String prefix = namespace.getPrefix();
            if (prefix.isEmpty()) {
                continue;
            }
            attrMap.put(namespaceOfPrefix + prefix, new BString(namespace.getNamespaceURI()));
        }

        Iterator<OMAttribute> attrIterator = ((OMElement) omNode).getAllAttributes();
        while (attrIterator.hasNext()) {
            OMAttribute attr = attrIterator.next();
            attrMap.put(attr.getQName().toString(), new BString(attr.getAttributeValue()));
        }

        attrMap.finishConstruction();
        return attrMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(BMap<String, ?> attributes) {

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
        for (String qname : attributes.keys()) {
            if (qname.startsWith("{") && qname.indexOf('}') > 0) {
                localName = qname.substring(qname.indexOf('}') + 1, qname.length());
                uri = qname.substring(1, qname.indexOf('}'));
            } else {
                localName = qname;
                uri = STRING_NULL_VALUE;
            }

            // Validate whether the attribute name is an XML supported qualified name,
            // according to the XML recommendation.
            XMLValidationUtils.validateXMLName(localName);
            setAttribute(localName, uri, STRING_NULL_VALUE, attributes.get(qname).stringValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> elements() {
        BValueArray elementsSeq = new BValueArray();
        switch (nodeType) {
            case ELEMENT:
                elementsSeq.add(0, this);
                break;
            default:
                break;
        }
        return new BXMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> elements(String qname) {
        BValueArray elementsSeq = new BValueArray();
        switch (nodeType) {
            case ELEMENT:
                if (getElementName().stringValue().equals(getQname(qname).toString())) {
                    elementsSeq.add(0, this);
                }
                break;
            default:
                break;
        }
        return new BXMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> children() {
        BValueArray elementsSeq = new BValueArray();
        switch (nodeType) {
            case ELEMENT:
                Iterator<OMNode> childrenItr = ((OMElement) omNode).getChildren();
                int i = 0;
                while (childrenItr.hasNext()) {
                    elementsSeq.add(i++, new BXMLItem(childrenItr.next()));
                }
                break;
            default:
                break;
        }
        
        return new BXMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> children(String qname) {
        BValueArray elementsSeq = new BValueArray();
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
                    elementsSeq.add(i++, new BXMLItem(node));
                }
                break;
            default:
                break;
        }
        return new BXMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChildren(BXML<?> seq) {
        if (seq == null) {
            return;
        }

        OMElement currentNode;
        switch (nodeType) {
            case ELEMENT:
                currentNode = ((OMElement) omNode);
                break;
            default:
                throw new BallerinaException("not an " + XMLNodeType.ELEMENT);
        }

        currentNode.removeChildren();
        addChildren(seq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChildren(BXML<?> seq) {
        if (seq == null) {
            return;
        }

        OMElement currentNode;
        switch (nodeType) {
            case ELEMENT:
                currentNode = ((OMElement) omNode);
                break;
            default:
                throw new BallerinaException("not an " + XMLNodeType.ELEMENT);
        }

        if (seq.getNodeType() == XMLNodeType.SEQUENCE) {
            BValueArray childSeq = ((BXMLSequence) seq).value();
            for (int i = 0; i < childSeq.size(); i++) {
                currentNode.addChild((OMNode) childSeq.getRefValue(i).value());
            }
        } else {
            currentNode.addChild((OMNode) seq.value());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> strip() {
        if (omNode == null || (nodeType == XMLNodeType.TEXT &&
                ((OMText) omNode).getText().trim().isEmpty())) {
            return new BXMLSequence();
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> slice(long startIndex, long endIndex) {
        if (startIndex > 1 || endIndex > 1 || startIndex < -1 || endIndex < -1) {
            throw new BallerinaException("index out of range: [" + startIndex + "," + endIndex + "]");
        }

        if (startIndex == -1) {
            startIndex = 0;
        }

        if (endIndex == -1) {
            endIndex = 1;
        }

        if (startIndex == endIndex) {
            return new BXMLSequence();
        }

        if (startIndex > endIndex) {
            throw new BallerinaException("invalid indices: " + startIndex + " < " + endIndex);
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> descendants(String qname) {
        List<BXML<?>> descendants = new ArrayList<BXML<?>>();
        switch (nodeType) {
            case ELEMENT:
                addDescendants(descendants, (OMElement) omNode, getQname(qname).toString());
                break;
            default:
                break;
        }

        return new BXMLSequence(new BValueArray(descendants.toArray(new BXML[descendants.size()]), BTypes.typeXML));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(OutputStream outputStream) {
        try {
            this.omNode.serializeAndConsume(outputStream);
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
    public BXMLItem copy(Map<BValue, BValue> refs) {
        if (isFrozen()) {
            return this;
        }

        OMNode clonedNode;
        switch (nodeType) {
            case ELEMENT:
                clonedNode = ((OMElement) omNode).cloneOMElement();
                break;
            case TEXT:
                // Temp fix to avoid package conflict in axiom.dom and axiom.impl
                CharacterDataImpl text = new CharacterDataImpl();
                text.coreSetCharacterData(((OMText) omNode).getText());
                clonedNode = text;
                break;
            case COMMENT:
                // Temp fix to avoid package conflict in axiom.dom and axiom.impl
                OMCommentImpl comment = new OMCommentImpl();
                comment.setValue(((OMComment) omNode).getValue());
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
        return new BXMLItem(clonedNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> getItem(long index) {
        if (index != 0) {
            throw new BallerinaException("index out of range: index: " + index + ", size: 1");
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public long size() {
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

    @Override
    public BIterator newIterator() {
        return new BXMLItemIterator(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeChildren(String qname) {
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
                String text = ((OMText) node).getText();
                return StringEscapeUtils.escapeXml11(text);
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

    /**
     * {@code {@link BXMLItemIterator}} provides iterator for xml items.
     *
     * @since 0.96.0
     */
    static class BXMLItemIterator implements BIterator {

        BXMLItem value;
        int cursor = 0;
        BXMLCodePointIterator codePointIterator;

        BXMLItemIterator(BXMLItem bxmlItem) {
            value = bxmlItem;
        }

        @Override
        public BValue getNext() {
            if (value.getNodeType() == XMLNodeType.TEXT) {
                if (codePointIterator == null) {
                    codePointIterator = createCodePointIterator(value);
                }
                cursor++;
                return codePointIterator.getNext();
            } else if (hasNext()) {
                cursor++;
                return value;
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            if (value.getNodeType() == XMLNodeType.TEXT) {
                if (codePointIterator == null) {
                    codePointIterator = createCodePointIterator(value);
                }
                return codePointIterator.hasNext();
            }
            return cursor == 0;
        }

        private BXMLCodePointIterator createCodePointIterator(BXMLItem value) {
            return new BXMLCodePointIterator(((OMText) value.omNode).getText());
        }
    }

    private static class BXmlAttrMap extends BMap {
        private final BXMLItem bXmlItem;
        private boolean constructed = false;

        BXmlAttrMap(BXMLItem bXmlItem) {
            super(new BMapType(BTypes.typeString));
            this.bXmlItem = bXmlItem;
        }

        void finishConstruction() {
            constructed = true;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void put(Object key, BValue value) {
            super.put(key, value);
            if (constructed) {
                setAttribute((String) key, value.stringValue());
            }
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
}
