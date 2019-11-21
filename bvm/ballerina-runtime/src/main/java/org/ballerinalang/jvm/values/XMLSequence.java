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

import org.apache.axiom.om.OMText;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.StaxXMLSink;
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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    List<XMLValue<?>> children;

    /**
     * Create an empty xml sequence.
     */
    public XMLSequence() {
        children = new ArrayList<>();
    }

    public XMLSequence(List<XMLValue<?>> children) {
        this();
        this.children = children;
    }

    public List<XMLValue<?>> getChildrenList() {
        return children;
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
        return children.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSingleton() {
        return children.size() == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getItemType() {
        if (isSingleton()) {
            return children.get(0).getItemType();
        }

        return XMLNodeType.SEQUENCE.value();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getElementName() {
        if (isSingleton()) {
            return children.get(0).getElementName();
        }
        return STRING_EMPTY_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTextValue() {
        StringBuilder seqTextBuilder = new StringBuilder();
        for (XMLValue<?> x : children) {
            seqTextBuilder.append(x.getTextValue());
        }
        return seqTextBuilder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttribute(String localName, String namespace) {
        if (isSingleton()) {
            return children.get(0).getAttribute(localName, namespace);
        }

        return STRING_NULL_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttribute(String localName, String namespace, String prefix) {
        if (isSingleton()) {
            return children.get(0).getAttribute(localName, namespace, prefix);
        }

        return STRING_NULL_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String localName, String namespace, String prefix, String value) {
        if (isSingleton()) {
            children.get(0).setAttribute(localName, namespace, prefix, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapValue<String, String> getAttributesMap() {
        if (isSingleton()) {
            return children.get(0).getAttributesMap();
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

        if (isSingleton()) {
            children.get(0).setAttributes(attributes);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> elements() {
        List elementsSeq = new ArrayList<XMLValue<?>>();
        int j = 0;
        for (XMLValue<?> x : children) {
            if (x.getNodeType() != XMLNodeType.ELEMENT) {
                continue;
            }
            XMLItem item = (XMLItem) x;
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
        List<XMLValue<?>> elementsSeq = new ArrayList<>();
        String qnameStr = getQname(qname).toString();
        int j = 0;
        for (XMLValue<?> x : children) {
            if (x.getNodeType() != XMLNodeType.ELEMENT) {
                continue;
            }
            XMLItem item = (XMLItem) x;
            if (item.getNodeType() == XMLNodeType.ELEMENT && item.getElementName().equals(qnameStr)) {
                elementsSeq.add(j++, item);
            }
        }
        return new XMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> children() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> children(String qname) {
        List<XMLValue<?>> selected = new ArrayList<>();
        for (XMLValue<?> elem : this.children) {
            if (elem.getNodeType() != XMLNodeType.ELEMENT) {
                continue;
            }
            if (elem.getElementName().equals(qname)) {
                selected.add(elem);
            }
        }

        return new XMLSequence(selected);
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

        if (children.size() != 1) {
            throw BallerinaErrors.createError("not an " + XMLNodeType.ELEMENT);
        }

        children.get(0).setChildren(seq);
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

        if (children.size() != 1) {
            throw BallerinaErrors.createError("not an " + XMLNodeType.ELEMENT);
        }

        children.get(0).addChildren(seq);
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
        List<XMLValue<?>> elementsSeq = new ArrayList<>();
        int j = 0;
        for (XMLValue<?> x : children) {
            XMLItem element = (XMLItem) x;
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
        if (startIndex > this.children.size() || endIndex > this.children.size() || startIndex < -1 || endIndex < -1) {
            throw BallerinaErrors.createError("index out of range: [" + startIndex + "," + endIndex + "]");
        }

        if (startIndex == -1) {
            startIndex = 0;
        }

        if (endIndex == -1) {
            endIndex = children.size();
        }

        if (startIndex == endIndex) {
            return new XMLSequence();
        }

        if (startIndex > endIndex) {
            throw BallerinaErrors.createError("invalid indices: " + startIndex + " < " + endIndex);
        }

        int j = 0;
        List<XMLValue<?>> elementsSeq = new ArrayList<>();
        for (int i = (int) startIndex; i < endIndex; i++) {
            elementsSeq.add(j++, children.get(i));
        }

        return new XMLSequence(elementsSeq);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue<?> descendants(String qname) {
        List<XMLValue<?>> descendants = new ArrayList<XMLValue<?>>();
        for (XMLValue<?> x : children) {
            XMLItem element = (XMLItem) x;
            switch (element.getNodeType()) {
                case ELEMENT:
                    addDescendants(descendants, element, getQname(qname).toString());
                    break;
                default:
                    break;
            }
        }

        return new XMLSequence(descendants);
    }

    // Methods from Datasource impl

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(OutputStream outputStream) {
        if (outputStream instanceof StaxXMLSink) {
            ((StaxXMLSink) outputStream).write(this);
        } else {
            (new StaxXMLSink(outputStream)).write(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayValue value() {
        return new ArrayValue(children.toArray(),  new BArrayType(BTypes.typeXML));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();
            for (XMLValue<?> x : children) {
                sb.append(x.toString());
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
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StaxXMLSink staxXMLSink = new StaxXMLSink(outputStream);
            staxXMLSink.write(this);
            staxXMLSink.flush();
            String str = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            staxXMLSink.close();
            return str;
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

        ArrayList<XMLValue<?>> copiedChildrenList = new ArrayList<>(children.size());
        XMLSequence copiedSeq = new XMLSequence(copiedChildrenList);
        refs.put(this, copiedSeq);
        for (XMLValue<?> child : children) {
            copiedChildrenList.add((XMLValue<?>) child.copy(refs));
        }

        return copiedSeq;
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
            return (XMLValue<?>) this.children.get(index);
        } catch (Exception e) {
            throw BallerinaErrors.createError(BallerinaErrorReasons.XML_OPERATION_ERROR, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.children.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void build() {
        for (XMLValue<?> x : children) {
            ((XMLValue<?>) x).build();
        }
    }

    @Override
    public void removeAttribute(String qname) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), XML_LANG_LIB);
            }
        }

        if (children.size() != 1) {
            throw BallerinaErrors.createError("not an " + XMLNodeType.ELEMENT);
        }

        children.get(0).removeAttribute(qname);
    }

    @Override
    public void removeChildren(String qname) {
        synchronized (this) {
            if (freezeStatus.getState() != State.UNFROZEN) {
                FreezeUtils.handleInvalidUpdate(freezeStatus.getState(), XML_LANG_LIB);
            }
        }

        if (children.size() != 1) {
            throw BallerinaErrors.createError("not an " + XMLNodeType.ELEMENT);
        }

        children.get(0).removeChildren(qname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void attemptFreeze(Status freezeStatus) {
        if (FreezeUtils.isOpenForFreeze(this.freezeStatus, freezeStatus)) {
            this.freezeStatus = freezeStatus;
            for (XMLValue<?> elem : children) {
                elem.attemptFreeze((freezeStatus));
            }
        }
    }

    @Override
    public void freezeDirect() {
        this.freezeStatus.setFrozen();
        for (XMLValue<?> elem : children) {
            elem.freezeDirect();
        }
    }

    @Override
    public IteratorValue getIterator() {
        return new XMLIterator.SequenceIterator(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof XMLSequence) {
            XMLSequence that = (XMLSequence) obj;
            return that.children.equals(this.children);
        }
        if (obj instanceof XMLItem) {
            return this.children.size() == 1 && this.children.get(0).equals(obj);
        }
        return false;
    }
}
