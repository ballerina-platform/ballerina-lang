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
import java.util.Iterator;

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
        // TODO: check for comment, pi, etc
        return new BString(((OMElement) omNode).getQName().toString());
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
    public BString getAttribute(String namespace, String localName) {
        if (nodeType != XMLNodeType.ELEMENT) {
            throw new BallerinaException("cannot get atribute from a xml " + nodeType.value());
        }
        
        if (localName.isEmpty()) {
            throw new BallerinaException("localname of the attribute cannot be empty");
        }
        
        OMAttribute attribute = ((OMElement) omNode).getAttribute(new QName(namespace, localName));
        
        if (attribute == null) {
            throw new BallerinaException("atribute not found: {" + namespace + "}" + localName);
        }
        
        return new BString(attribute.getAttributeValue());
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BString getAttribute(String namespace, String prefix, String localName) {
        if (nodeType != XMLNodeType.ELEMENT) {
            throw new BallerinaException("cannot get atribute from a xml " + nodeType.value());
        }
        OMAttribute attribute = ((OMElement) omNode).getAttribute(new QName(namespace, localName, prefix));
        return new BString(attribute.getAttributeValue());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String namespace, String prefix, String localName, String value) {
        if (nodeType != XMLNodeType.ELEMENT) {
            throw new BallerinaException("cannot set atribute to a xml " + nodeType.value());
        }
        
        if (localName.isEmpty()) {
            throw new BallerinaException("localname of the attribute cannot be empty");
        }
        
        OMNamespace ns = new OMNamespaceImpl(namespace, prefix);;
        ((OMElement) omNode).addAttribute(localName, value, ns);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public BMap<?, ?> getAttributes() {
        // TODO: need to figure out a way to maintain the reference
        return null;
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
                if (getElementName().equals(qname)) {
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
                Iterator<OMNode> childrenItr = ((OMElement) omNode).getChildren();
                int i = 0;
                while (childrenItr.hasNext()) {
                    OMNode node = childrenItr.next();
                    if (node.getType() == OMNode.ELEMENT_NODE &&
                        qname.stringValue().equals(((OMElement) node).getQName().toString())) {
                        elementsSeq.add(i++, new BXMLItem(node));
                    }
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
    public void setChildren(BXML seq) {
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
    public void setAttribute(BMap<BString, ?> attributes) {
        if (nodeType != XMLNodeType.ELEMENT) {
            throw new BallerinaException("not an " + XMLNodeType.ELEMENT);
        }
        
        for (BString key : attributes.keySet()) {
            // TODO: need to extract the namespace, and localname part from the key
            // OMNamespace ns = new OMNamespaceImpl(namespace, null);
            // ((OMElement) omNode).addAttribute(localName, value, ns);
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
        return "";
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
                return "";
            case OMNode.PI_NODE:
                return "";
            default:
                return "";
        }
    }
}
