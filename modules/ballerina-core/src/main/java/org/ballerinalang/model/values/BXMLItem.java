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

package org.ballerinalang.model.values;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMProcessingInstruction;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.common.OMNamespaceImpl;
import org.apache.axiom.om.impl.dom.CommentImpl;
import org.apache.axiom.om.impl.dom.TextImpl;
import org.apache.axiom.om.impl.llom.OMDocumentImpl;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.impl.llom.OMProcessingInstructionImpl;
import org.apache.axiom.om.util.AXIOMUtil;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.util.XMLNodeType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

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
        if (xmlValue != null) {
            try {
                omNode = AXIOMUtil.stringToOM(xmlValue);
                setXMLNodeType();
            } catch (Throwable t) {
                handleXmlException("failed to create xml: ", t);
            }
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
        // TODO
        if (inputStream != null) {
            try {
                omNode = (OMNode) new StAXOMBuilder(inputStream).getDocumentElement();
                setXMLNodeType();
            } catch (Throwable t) {
                handleXmlException("failed to create xml: ", t);
            }
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
        switch(nodeType) {
            case ELEMENT:
                StringBuilder elementTextBuilder = new StringBuilder();
                Iterator<OMNode> children = ((OMElement) omNode).getChildren();
                while (children.hasNext()) {
                    elementTextBuilder.append(getTextValue(children.next()));
                }
                return new BString(elementTextBuilder.toString());
            case TEXT:
                return new BString(((OMText) omNode).getText());
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
        if (nodeType != XMLNodeType.ELEMENT || localName.isEmpty()) {
            return ZERO_STRING_VALUE;
        }
        OMAttribute attribute = ((OMElement) omNode).getAttribute(new QName(namespace, localName, prefix));
        
        if (attribute != null) {
            return attribute.getAttributeValue();
        }
        
        OMNamespace ns = ((OMElement) omNode).findNamespaceURI(localName);
        return ns == null ? ZERO_STRING_VALUE : ns.getNamespaceURI();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String localName, String namespaceUri, String prefix, String value) {
        if (nodeType != XMLNodeType.ELEMENT) {
            return;
        }

        if (localName.isEmpty()) {
            throw new BallerinaException("localname of the attribute cannot be empty");
        }
        
        // If the attribute already exists, update the value.
        OMElement node = (OMElement) omNode;
        OMAttribute attr = node.getAttribute(new QName(namespaceUri, localName, prefix));
        if (attr != null) {
            attr.setAttributeValue(value);
            return;
        }

        if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            node.declareNamespace(value, localName);
            return;
        }

        // Attributes cannot cannot be belong to default namespace. Hence, if the current namespace is the default one,
        // treat this attribute-add operation as a namespace addition.
        if ((node.getDefaultNamespace() != null && namespaceUri.equals(node.getDefaultNamespace().getNamespaceURI()))
                || namespaceUri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {

            node.declareNamespace(value, localName);
            return;
        }

        OMNamespace ns = null;
        if (!prefix.isEmpty()) {
            OMNamespace existingNs = node.findNamespaceURI(prefix);

            // If a namespace exists with the same prefix but a different uri, then do not add the new attribute.
            if (existingNs != null && !namespaceUri.equals(existingNs.getNamespaceURI())) {
                throw new BallerinaException("failed to add attribute '" + prefix + ":" + localName + "'. prefix '" +
                        prefix + "' is already bound to namespace '" + existingNs.getNamespaceURI() + "'");
            }

            ns = new OMNamespaceImpl(namespaceUri, prefix);
            node.addAttribute(localName, value, ns);
            return;
        }
        
        // We reach here if the namespace prefix is empty, and a namespace uri exists
        if (!namespaceUri.isEmpty()) {
            prefix = null;
            // Find a prefix that has the same namespaceUri, out of the defined namespaces
            Iterator<String> prefixes = node.getNamespaceContext(false).getPrefixes(namespaceUri);
            while (prefixes.hasNext()) {
                String definedPrefix = prefixes.next();
                if (definedPrefix.isEmpty()) {
                    continue;
                }
                prefix = definedPrefix;
                break;
            }

            if (prefix != null && prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                // If found, and if its the default namespace, add a namespace decl
                node.declareNamespace(value, localName);
                return;
            }

            // else create use the prefix. If the prefix is null, it will generate a random prefix.
            ns = new OMNamespaceImpl(namespaceUri, prefix);
        }
        node.addAttribute(localName, value, ns);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BMap<?, ?> getAttributesMap() {
        BMap<String, BString> attrMap = new BMap<>();
        
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
        
        return attrMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(BMap<String, ?> attributes) {
        if (nodeType != XMLNodeType.ELEMENT) {
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
        Set<String> attributeQNames = attributes.keySet();
        for (String qname : attributeQNames) {
            if (qname.startsWith("{") && qname.indexOf('}') > 0) {
                localName = qname.substring(qname.indexOf('}') + 1, qname.length());
                uri = qname.substring(1, qname.indexOf('}'));
            } else {
                localName = qname;
                uri = "";
            }
            
            setAttribute(localName, uri, "", attributes.get(qname).stringValue());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> elements() {
        BRefValueArray elementsSeq = new BRefValueArray();
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
    public BXML<?> elements(BString qname) {
        BRefValueArray elementsSeq = new BRefValueArray();
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
        BRefValueArray elementsSeq = new BRefValueArray();
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
    public BXML<?> children(BString qname) {
        BRefValueArray elementsSeq = new BRefValueArray();
        switch (nodeType) {
            case ELEMENT:
                Iterator<OMNode> childrenItr = ((OMElement) omNode).getChildrenWithName(getQname(qname));
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
        OMElement currentNode;
        switch (nodeType) {
            case ELEMENT:
                currentNode = ((OMElement) omNode);
                break;
            default:
                throw new BallerinaException("not an " + XMLNodeType.ELEMENT);
        }
        
        currentNode.removeChildren();
        
        if (seq.getNodeType() == XMLNodeType.SEQUENCE) {
            BRefValueArray childSeq = ((BXMLSequence) seq).value();
            for (int i = 0; i < childSeq.size(); i++) {
                currentNode.addChild((OMNode) childSeq.get(i).value());
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
                ((OMText) omNode).getText().isEmpty())) {
            return new BXMLSequence();
        }
        
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> slice(long startIndex, long endIndex) {
        if (startIndex > 1 || endIndex > 1) {
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
    public BXML<?> descendants(BString qname) {
        List<BXML<?>> descendants = new ArrayList<BXML<?>>();
        switch (nodeType) {
            case ELEMENT:
                addDescendants(descendants, (OMElement) omNode, getQname(qname).toString());
                break;
            default:
                break;
        }

        return new BXMLSequence(new BRefValueArray(descendants.toArray(new BXML[descendants.size()])));
    }
    
    // Methods from Datasource impl

    /**
     * {@inheritDoc}
     */
    @Override
    public void serializeData() {
        try {
            this.omNode.serialize(this.outputStream);
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
        return ZERO_STRING_VALUE;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BXMLItem copy() {
        OMNode clonedNode = null;
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
        return new BXMLItem(clonedNode);
    }
    
    // private methods
    
    private void setXMLNodeType() {
        switch (omNode.getType()) {
            case OMNode.ELEMENT_NODE:
                nodeType = XMLNodeType.ELEMENT;
                break;
            case OMNode.TEXT_NODE:
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
                return ZERO_STRING_VALUE;
            case OMNode.PI_NODE:
                return ZERO_STRING_VALUE;
            default:
                return ZERO_STRING_VALUE;
        }
    }
}
