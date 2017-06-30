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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.util.XMLNodeType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.OutputStream;
import java.util.Iterator;

/**
 * {@code BXMLSequence} represents a sequence of {@link BXMLItem}s in Ballerina.
 *
 * @since 0.88
 */
public final class BXMLSequence extends BXML<BRefValueArray> {

    private BRefValueArray sequence;
    
    /**
     * Create an empty xml sequence.
     */
    public BXMLSequence() {
        sequence = new BRefValueArray();
    }

    /**
     * Initialize a {@link BXMLSequence} from a {@link org.apache.axiom.om.OMNode} object.
     *
     * @param sequence xml object
     */
    public BXMLSequence(BRefValueArray sequence) {
        this.sequence = sequence;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public XMLNodeType getNodeType() {
        return XMLNodeType.SEQUENCE;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BBoolean isEmpty() {
        return new BBoolean(sequence.size() == 0);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BBoolean isSingleton() {
        return new BBoolean(sequence.size() == 1);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BString getItemType() {
        if (sequence.size() == 1) {
            return ((BXMLItem) sequence.get(0)).getItemType();
        }
        
        return BTypes.typeString.getZeroValue();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BString getElementName() {
        if (sequence.size() == 1) {
            return ((BXMLItem) sequence.get(0)).getElementName();
        }
        return BTypes.typeString.getZeroValue();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BString getTextValue() {
        StringBuilder seqTextBuilder = new StringBuilder();
        for (int i = 0; i < sequence.size(); i++) {
            BXMLItem item = (BXMLItem) sequence.get(i);
            seqTextBuilder.append(item.getTextValue().stringValue());
        }
        return new BString(seqTextBuilder.toString());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BString getAttribute(String namespace, String localName) {
        if (sequence.size() == 1) {
            ((BXMLItem) sequence.get(0)).getAttribute(namespace, localName);
        }
        throw new BallerinaException("cannot get atribute from a xml " + XMLNodeType.SEQUENCE.value());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BString getAttribute(String namespace, String prefix, String localName) {
        if (sequence.size() == 1) {
            ((BXMLItem) sequence.get(0)).getAttribute(namespace, prefix, localName);
        }
        throw new BallerinaException("cannot get atribute from a xml " + XMLNodeType.SEQUENCE.value());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String namespace, String prefix, String localName, String value) {
        if (sequence.size() == 1) {
            ((BXMLItem) sequence.get(0)).setAttribute(namespace, prefix, localName, value);
        }
        
        throw new BallerinaException("cannot set atribute to a xml " + XMLNodeType.SEQUENCE.value());
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
        BRefValueArray elementsSeq = new BRefValueArray(BTypes.typeXML);
        int j = 0;
        for (int i = 0; i < sequence.size(); i++) {
            BXMLItem item = (BXMLItem) sequence.get(i);
            if (item.getNodeType() == XMLNodeType.ELEMENT) {
                elementsSeq.add(j++, item);
            }
        }
        return new BXMLSequence(elementsSeq);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> elements(BString qname) {
        BRefValueArray elementsSeq = new BRefValueArray(BTypes.typeXML);
        int j = 0;
        for (int i = 0; i < sequence.size(); i++) {
            BXMLItem item = (BXMLItem) sequence.get(i);
            if (item.getNodeType() == XMLNodeType.ELEMENT && item.getElementName().equals(qname)) {
                elementsSeq.add(j++, item);
            }
        }
        return new BXMLSequence(elementsSeq);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> children() {
        BRefValueArray elementsSeq = new BRefValueArray(BTypes.typeXML);
        for (int i = 0; i < sequence.size(); i++) {
            BXMLItem element = (BXMLItem) sequence.get(i);
            if (element.getNodeType() != XMLNodeType.ELEMENT) {
                continue;
            }

            Iterator<OMNode> childrenItr = ((OMElement) element.value()).getChildren();
            int j = 0;
            while (childrenItr.hasNext()) {
                elementsSeq.add(j++, new BXMLItem(childrenItr.next()));
            }
        }
        
        return new BXMLSequence(elementsSeq);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> children(BString qname) {
        BRefValueArray elementsSeq = new BRefValueArray();
        for (int i = 0; i < sequence.size(); i++) {
            BXMLItem element = (BXMLItem) sequence.get(i);
            if (element.getNodeType() != XMLNodeType.ELEMENT) {
                continue;
            }

            Iterator<OMNode> childrenItr = ((OMElement) element.value()).getChildren();
            int j = 0;
            while (childrenItr.hasNext()) {
                OMNode child = childrenItr.next();
                // Add to the seq only if the name matches
                if (child.getType() == OMNode.ELEMENT_NODE &&
                    ((OMElement) child).getQName().toString().equals(qname.stringValue())) {
                    elementsSeq.add(j++, new BXMLItem(child));
                }
            }
        }
        return new BXMLSequence(elementsSeq);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setChildren(BXML<?> seq) {
        if (sequence.size() != 1) {
            throw new BallerinaException("not an " + XMLNodeType.ELEMENT);
        }
        
        ((BXMLItem) sequence.get(0)).setChildren(seq);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(BMap<BString, ?> attributes) {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> strip() {
        BRefValueArray elementsSeq = new BRefValueArray();
        int j = 0;
        for (int i = 0; i < sequence.size(); i++) {
            BXMLItem element = (BXMLItem) sequence.get(i);
            if (element.value() == null || (element.getNodeType() == XMLNodeType.TEXT && 
                    ((OMText) element.value()).getText().trim().isEmpty())) {
                continue;
            }
            elementsSeq.add(j++, element);
        }
        
        return new BXMLSequence(elementsSeq);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> slice(long startIndex, long endIndex) {
        if (startIndex > this.sequence.size() || endIndex > this.sequence.size()) {
            throw new BallerinaException("index out of range: [" + startIndex + "," + endIndex + "]");
        }
        
        if (startIndex == -1) {
            startIndex = 0;
        }
        
        if (endIndex == -1) {
            endIndex = sequence.size();
        }
        
        if (startIndex == endIndex) {
            return new BXMLSequence();
        } 
        
        if (startIndex > endIndex) {
            throw new BallerinaException("invalid indices: " + startIndex + " < " + endIndex);
        }
        
        int j = 0;
        BRefValueArray elementsSeq = new BRefValueArray();
        for (long i = startIndex; i < endIndex; i++) {
            elementsSeq.add(j++, sequence.get(i));
        }
        
        return new BXMLSequence(elementsSeq);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setOutputStream(OutputStream outputStream) {
        for (int i = 0; i < sequence.size(); i++) {
            ((BXML<?>) sequence.get(i)).setOutputStream(outputStream);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void serializeData() {
        for (int i = 0; i < sequence.size(); i++) {
            ((BXML<?>) sequence.get(i)).serializeData();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BRefValueArray value() {
        return sequence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stringValue() {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sequence.size(); i++) {
                sb.append(sequence.get(i).stringValue());
            }
            return sb.toString();
        } catch (Throwable t) {
            handleXmlException("failed to get xml as string: ", t);
        }
        return "";
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BXMLSequence copy() {
        BRefType[] copiedVals = new BRefType[(int) sequence.size()];
        for (int i = 0; i < sequence.size(); i++) {
            copiedVals[i] = ((BXML<?>) sequence.get(i)).copy();
        }
        return new BXMLSequence(new BRefValueArray(copiedVals));
    }
}
