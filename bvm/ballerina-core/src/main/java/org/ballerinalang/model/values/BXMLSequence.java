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
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;

import static org.ballerinalang.util.BLangConstants.STRING_NULL_VALUE;

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

        return new BString(XMLNodeType.SEQUENCE.value());
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
    public String getAttribute(String localName, String namespace) {
        if (sequence.size() == 1) {
            return ((BXMLItem) sequence.get(0)).getAttribute(localName, namespace);
        }
        
        return STRING_NULL_VALUE;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttribute(String localName, String namespace, String prefix) {
        if (sequence.size() == 1) {
            return ((BXMLItem) sequence.get(0)).getAttribute(localName, namespace, prefix);
        }
        
        return STRING_NULL_VALUE;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String localName, String namespace, String prefix, String value) {
        if (sequence.size() == 1) {
            ((BXMLItem) sequence.get(0)).setAttribute(localName, namespace, prefix, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BMap<?, ?> getAttributesMap() {
        if (sequence.size() == 1) {
            return ((BXMLItem) sequence.get(0)).getAttributesMap();
        }
        
        return BTypes.typeMap.getEmptyValue();
    }

    @Override
    public void setAttributes(BMap<String, ?> attributes) {
        if (sequence.size() == 1) {
            ((BXMLItem) sequence.get(0)).setAttributes(attributes);
        }
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
    public BXML<?> elements(String qname) {
        BRefValueArray elementsSeq = new BRefValueArray(BTypes.typeXML);
        String qnameStr = getQname(qname).toString();
        int j = 0;
        for (int i = 0; i < sequence.size(); i++) {
            BXMLItem item = (BXMLItem) sequence.get(i);
            if (item.getNodeType() == XMLNodeType.ELEMENT
                    && item.getElementName().stringValue().equals(qnameStr)) {
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
        int index = 0;
        for (int i = 0; i < sequence.size(); i++) {
            BXMLItem element = (BXMLItem) sequence.get(i);
            if (element.getNodeType() != XMLNodeType.ELEMENT) {
                continue;
            }

            Iterator<OMNode> childrenItr = ((OMElement) element.value()).getChildren();
            while (childrenItr.hasNext()) {
                elementsSeq.add(index++, new BXMLItem(childrenItr.next()));
            }
        }

        return new BXMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> children(String qname) {
        BRefValueArray elementsSeq = new BRefValueArray();
        QName name = getQname(qname);
        int index = 0;
        for (int i = 0; i < sequence.size(); i++) {
            BXMLItem element = (BXMLItem) sequence.get(i);
            if (element.getNodeType() != XMLNodeType.ELEMENT) {
                continue;
            }

            Iterator<OMNode> childrenItr = ((OMElement) element.value()).getChildrenWithName(name);
            while (childrenItr.hasNext()) {
                OMNode child = childrenItr.next();
                elementsSeq.add(index++, new BXMLItem(child));
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
    public void addChildren(BXML<?> seq) {
        if (sequence.size() != 1) {
            throw new BallerinaException("not an " + XMLNodeType.ELEMENT);
        }
        
        ((BXMLItem) sequence.get(0)).addChildren(seq);
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
        if (startIndex > this.sequence.size() || endIndex > this.sequence.size() || startIndex < -1 || endIndex < -1) {
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
    public BXML<?> descendants(String qname) {
        List<BXML<?>> descendants = new ArrayList<BXML<?>>();
        for (int i = 0; i < sequence.size(); i++) {
            BXMLItem element = (BXMLItem) sequence.get(i);
            switch (element.getNodeType()) {
                case ELEMENT:
                    addDescendants(descendants, (OMElement) element.value(), getQname(qname).toString());
                    break;
                default:
                    break;
            }
        }

        return new BXMLSequence(new BRefValueArray(descendants.toArray(new BXML[descendants.size()]), BTypes.typeXML));
    }

    // Methods from Datasource impl

    /**
     * {@inheritDoc}
     */
    @Override
    public void serializeData(OutputStream outputStream) {
        for (int i = 0; i < sequence.size(); i++) {
            ((BXML<?>) sequence.get(i)).serializeData(outputStream);
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
        return BLangConstants.STRING_NULL_VALUE;
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
        return new BXMLSequence(new BRefValueArray(copiedVals, BTypes.typeXML));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BXML<?> getItem(long index) {
        return (BXML<?>) this.sequence.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public int length() {
        return this.sequence.size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void build() {
        for (int i = 0; i < sequence.size(); i++) {
            ((BXML<?>) sequence.get(i)).build();
        }
    }

    @Override
    public void removeAttribute(String qname) {
        if (sequence.size() != 1) {
            throw new BallerinaException("not an " + XMLNodeType.ELEMENT);
        }
        
        ((BXMLItem) sequence.get(0)).removeAttribute(qname);
    }

    @Override
    public BIterator newIterator() {
        return new BXMLSequenceIterator(this);
    }

    /**
     * {@code {@link BXMLSequenceIterator }} provides iterator for xml items..
     *
     * @since 0.96.0
     */
    static class BXMLSequenceIterator implements BIterator {

        BXMLSequence value;
        int cursor = 0;

        BXMLSequenceIterator(BXMLSequence bxmlSequence) {
            value = bxmlSequence;
        }

        @Override
        public BValue[] getNext(int arity) {
            if (arity == 1) {
                return new BValue[] {value.sequence.get(cursor++)};
            }
            int cursor = this.cursor++;
            return new BValue[] {new BInteger(cursor), value.sequence.get(cursor)};
        }

        @Override
        public boolean hasNext() {
            return cursor < value.sequence.size();
        }
    }
}
