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
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.util.BLangConstants;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.values.api.BXml;
import org.ballerinalang.jvm.values.freeze.FreezeUtils;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import static org.ballerinalang.jvm.util.BLangConstants.STRING_EMPTY_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.STRING_NULL_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.XML_LANG_LIB;

/**
 * <p>
 * {@code BXMLSequence} represents a sequence of {@link XMLItem}s in Ballerina.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 * 
 * @since 0.995.0
 */
public final class XMLSequence extends XMLValue<ArrayValue> {

    ArrayValue sequence;

    /**
     * Create an empty xml sequence.
     */
    @Deprecated
    public XMLSequence() {
        sequence = new ArrayValue(new BArrayType(BTypes.typeXML), 0);
    }

    /**
     * Initialize a {@link XMLSequence} from a {@link org.apache.axiom.om.OMNode} object.
     *
     * @param sequence xml object
     */
    @Deprecated
    public XMLSequence(ArrayValue sequence) {
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
    public boolean isEmpty() {
        return sequence.size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSingleton() {
        return sequence.size() == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getItemType() {
        if (sequence.size() == 1) {
            return ((XMLItem) sequence.getRefValue(0)).getItemType();
        }

        return XMLNodeType.SEQUENCE.value();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getElementName() {
        if (sequence.size() == 1) {
            return ((XMLItem) sequence.getRefValue(0)).getElementName();
        }
        return STRING_EMPTY_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTextValue() {
        StringBuilder seqTextBuilder = new StringBuilder();
        for (int i = 0; i < sequence.size(); i++) {
            XMLItem item = (XMLItem) sequence.getRefValue(i);
            seqTextBuilder.append(item.getTextValue().toString());
        }
        return seqTextBuilder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttribute(String localName, String namespace) {
        if (sequence.size() == 1) {
            return ((XMLItem) sequence.getRefValue(0)).getAttribute(localName, namespace);
        }

        return STRING_NULL_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttribute(String localName, String namespace, String prefix) {
        if (sequence.size() == 1) {
            return ((XMLItem) sequence.getRefValue(0)).getAttribute(localName, namespace, prefix);
        }

        return STRING_NULL_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String localName, String namespace, String prefix, String value) {
        if (sequence.size() == 1) {
            ((XMLItem) sequence.getRefValue(0)).setAttribute(localName, namespace, prefix, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapValue<String, ?> getAttributesMap() {
        if (sequence.size() == 1) {
            return ((XMLItem) sequence.getRefValue(0)).getAttributesMap();
        }

        return null;
    }

    @Override
    public void setAttributes(MapValue<String, ?> attributes) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), XML_LANG_LIB);
            }
        }

        if (sequence.size() == 1) {
            ((XMLItem) sequence.getRefValue(0)).setAttributes(attributes);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> elements() {
        ArrayValue elementsSeq = new ArrayValue(new BArrayType(BTypes.typeXML));
        int j = 0;
        for (int i = 0; i < sequence.size(); i++) {
            XMLItem item = (XMLItem) sequence.getRefValue(i);
            if (item.getNodeType() == XMLNodeType.ELEMENT) {
                elementsSeq.add(j++, item);
            }
        }
        return new XMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> elements(String qname) {
        ArrayValue elementsSeq = new ArrayValue(new BArrayType(BTypes.typeXML));
        String qnameStr = getQname(qname).toString();
        int j = 0;
        for (int i = 0; i < sequence.size(); i++) {
            XMLItem item = (XMLItem) sequence.getRefValue(i);
            if (item.getNodeType() == XMLNodeType.ELEMENT && item.getElementName().toString().equals(qnameStr)) {
                elementsSeq.add(j++, item);
            }
        }
        return new XMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public XMLValue<?> children() {
        ArrayValue elementsSeq = new ArrayValue(new BArrayType(BTypes.typeXML));
        int index = 0;
        for (int i = 0; i < sequence.size(); i++) {
            XMLItem element = (XMLItem) sequence.getRefValue(i);
            if (element.getNodeType() != XMLNodeType.ELEMENT) {
                continue;
            }

            Iterator<OMNode> childrenItr = ((OMElement) element.value()).getChildren();
            while (childrenItr.hasNext()) {
                elementsSeq.add(index++, new XMLItem(childrenItr.next()));
            }
        }

        return new XMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public XMLValue<?> children(String qname) {
        ArrayValue elementsSeq = new ArrayValue(new BArrayType(BTypes.typeXML));
        QName name = getQname(qname);
        int index = 0;
        for (int i = 0; i < sequence.size(); i++) {
            XMLItem element = (XMLItem) sequence.getRefValue(i);
            if (element.getNodeType() != XMLNodeType.ELEMENT) {
                continue;
            }

            Iterator<OMNode> childrenItr = ((OMElement) element.value()).getChildrenWithName(name);
            while (childrenItr.hasNext()) {
                OMNode child = childrenItr.next();
                elementsSeq.add(index++, new XMLItem(child));
            }
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
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), XML_LANG_LIB);
            }
        }

        if (sequence.size() != 1) {
            throw BallerinaErrors.createError("not an " + XMLNodeType.ELEMENT);
        }

        ((XMLItem) sequence.getRefValue(0)).setChildren(seq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChildren(BXml<?> seq) {
        setChildren((XMLValue) seq);
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

        if (sequence.size() != 1) {
            throw BallerinaErrors.createError("not an " + XMLNodeType.ELEMENT);
        }

        ((XMLItem) sequence.getRefValue(0)).addChildren(seq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChildren(BXml<?> seq) {
        addChildren((XMLValue) seq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> strip() {
        ArrayValue elementsSeq = new ArrayValue(new BArrayType(BTypes.typeXML));
        int j = 0;
        for (int i = 0; i < sequence.size(); i++) {
            XMLItem element = (XMLItem) sequence.getRefValue(i);
            if (element.value() == null || (element.getNodeType() == XMLNodeType.TEXT &&
                    ((OMText) element.value()).getText().trim().isEmpty())) {
                continue;
            }
            elementsSeq.add(j++, element);
        }

        return new XMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> slice(long startIndex, long endIndex) {
        if (startIndex > this.sequence.size() || endIndex > this.sequence.size() || startIndex < -1 || endIndex < -1) {
            throw BallerinaErrors.createError("index out of range: [" + startIndex + "," + endIndex + "]");
        }

        if (startIndex == -1) {
            startIndex = 0;
        }

        if (endIndex == -1) {
            endIndex = sequence.size();
        }

        if (startIndex == endIndex) {
            return new XMLSequence();
        }

        if (startIndex > endIndex) {
            throw BallerinaErrors.createError("invalid indices: " + startIndex + " < " + endIndex);
        }

        int j = 0;
        ArrayValue elementsSeq = new ArrayValue(new BArrayType(BTypes.typeXML));
        for (int i = (int) startIndex; i < endIndex; i++) {
            elementsSeq.add(j++, sequence.getRefValue(i));
        }

        return new XMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> descendants(String qname) {
        List<XMLValue<?>> descendants = new ArrayList<XMLValue<?>>();
        for (int i = 0; i < sequence.size(); i++) {
            XMLItem element = (XMLItem) sequence.getRefValue(i);
            switch (element.getNodeType()) {
                case ELEMENT:
                    addDescendants(descendants, (OMElement) element.value(), getQname(qname).toString());
                    break;
                default:
                    break;
            }
        }

        XMLValue<?>[] array = descendants.toArray(new XMLValue[descendants.size()]);
        return new XMLSequence(new ArrayValue(array, new BArrayType(BTypes.typeXML)));
    }

    // Methods from Datasource impl

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(OutputStream outputStream) {
        for (int i = 0; i < sequence.size(); i++) {
            ((XMLValue<?>) sequence.getRefValue(i)).serialize(outputStream);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayValue value() {
        return sequence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sequence.size(); i++) {
                sb.append(sequence.getRefValue(i).toString());
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
    public String stringValue(Strand strand) {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sequence.size(); i++) {
                sb.append(((RefValue) sequence.getRefValue(i)).stringValue(strand));
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
    public Object copy(Map<Object, Object> refs) {
        if (isFrozen()) {
            return this;
        }

        if (refs.containsKey(this)) {
            return refs.get(this);
        }

        Object[] copiedVals = new Object[sequence.size()];
        refs.put(this, new XMLSequence(new ArrayValue(copiedVals, new BArrayType(BTypes.typeXML))));
        for (int i = 0; i < sequence.size(); i++) {
            copiedVals[i] = ((XMLValue<?>) sequence.getRefValue(i)).copy(refs);
        }
        return refs.get(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        XMLSequence copy = (XMLSequence) copy(refs);
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
        try {
            return (XMLValue<?>) this.sequence.getRefValue(index);
        } catch (Exception e) {
            throw BallerinaErrors.createError(BallerinaErrorReasons.XML_OPERATION_ERROR, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        int size = 0;
        for (int i = 0; i < this.sequence.size; i++) {
            Object refValue = sequence.getRefValue(i);
            if (refValue instanceof XMLValue) {
                XMLValue xmlItem = (XMLValue) refValue;
                size += xmlItem.size();
            } else {
                size += 1;
            }
        }
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void build() {
        for (int i = 0; i < sequence.size(); i++) {
            ((XMLValue<?>) sequence.getRefValue(i)).build();
        }
    }

    @Override
    public void removeAttribute(String qname) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), XML_LANG_LIB);
            }
        }

        if (sequence.size() != 1) {
            throw BallerinaErrors.createError("not an " + XMLNodeType.ELEMENT);
        }

        ((XMLItem) sequence.getRefValue(0)).removeAttribute(qname);
    }

    @Override
    public void removeChildren(String qname) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), XML_LANG_LIB);
            }
        }

        if (sequence.size() != 1) {
            throw BallerinaErrors.createError("not an " + XMLNodeType.ELEMENT);
        }

        ((XMLItem) sequence.getRefValue(0)).removeChildren(qname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void attemptFreeze(Status freezeStatus) {
        if (FreezeUtils.isOpenForFreeze(this.freezeStatus, freezeStatus)) {
            this.freezeStatus = freezeStatus;
            Arrays.stream(sequence.refValues).forEach(val -> ((RefValue) val).attemptFreeze(freezeStatus));
        }
    }

    @Override
    public void freezeDirect() {
        this.freezeStatus.setFrozen();
        Arrays.stream(sequence.refValues).forEach(val -> ((RefValue) val).freezeDirect());
    }

    @Override
    public IteratorValue getIterator() {
        return new XMLIterator.SequenceIterator(this);
    }
}
