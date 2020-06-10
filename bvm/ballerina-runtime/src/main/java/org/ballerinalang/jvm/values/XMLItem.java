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
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.XMLValidator;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.api.BMap;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BXML;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

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
    public static final String XMLNS = "xmlns";
    private QName name;
    private XMLSequence children;
    private AttributeMapValueImpl attributes;
    // Keep track of probable parents of xml element to detect probable cycles in xml.
    private List<WeakReference<XMLItem>> probableParents;

    public XMLItem(QName name, XMLSequence children) {
        this.name = name;
        this.children = children;
        for (BXML child : children.children) {
            addParent(child, this);
        }
        attributes = new AttributeMapValueImpl(false);
        addDefaultNamespaceAttribute(name, attributes);
        probableParents = new ArrayList<>();
        this.type = BTypes.typeElement;
    }

    /**
     * Initialize a {@link XMLItem}.
     *
     * @param name element's qualified name
     */
    public XMLItem(QName name) {
        this(name, new XMLSequence(new ArrayList<>()));
        this.type = BTypes.typeElement;
    }

    public XMLItem(QName name, boolean readonly) {
        XMLSequence children = new XMLSequence(new ArrayList<>());
        this.name = name;
        this.children = children;
        for (BXML child : children.children) {
            addParent(child, this);
        }
        attributes = new AttributeMapValueImpl(readonly);
        addDefaultNamespaceAttribute(name, attributes);
        probableParents = new ArrayList<>();

        this.type = readonly ? BTypes.typeReadonlyElement : BTypes.typeElement;
    }

    private void addDefaultNamespaceAttribute(QName name, AttributeMapValueImpl attributes) {
        String namespace = name.getNamespaceURI();
        if (namespace == null || namespace.isEmpty()) {
            return;
        }

        String prefix = name.getPrefix();
        if (prefix == null || prefix.isEmpty()) {
            prefix = XMLNS;
        }

        attributes.populateInitialValue(StringUtils.fromString(XMLNS_URL_PREFIX + prefix),
                                        StringUtils.fromString(namespace));
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
    public BString getAttribute(String localName, String namespace) {
        return getAttribute(localName, namespace, XMLConstants.DEFAULT_NS_PREFIX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BString getAttribute(String localName, String namespace, String prefix) {
        if (prefix != null && !prefix.isEmpty()) {
            String ns = attributes.get(StringUtils.fromString(XMLNS_URL_PREFIX + prefix)).getValue();
            BString attrVal = attributes.get(StringUtils.fromString("{" + ns + "}" + localName));
            if (attrVal != null) {
                return attrVal;
            }
        }
        if (namespace != null && !namespace.isEmpty()) {
            return attributes.get(StringUtils.fromString("{" + namespace + "}" + localName));
        }
        return attributes.get(StringUtils.fromString(localName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String localName, String namespaceUri, String prefix, String value) {
        if (this.type.isReadOnly()) {
            ReadOnlyUtils.handleInvalidUpdate(XML_LANG_LIB);
        }

        attributes.setAttribute(localName, namespaceUri, prefix, value, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapValue<BString, BString> getAttributesMap() {
        return this.attributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public void setAttributes(BMap<BString, ?> attributes) {
        if (this.type.isReadOnly()) {
            ReadOnlyUtils.handleInvalidUpdate(XML_LANG_LIB);
        }

        setAttributes(attributes, this::setAttribute);
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
        if (this.type.isReadOnly()) {
            ReadOnlyUtils.handleInvalidUpdate(XML_LANG_LIB);
        }

        if (seq == null) {
            return;
        }

        if (seq.getNodeType() == XMLNodeType.SEQUENCE) {
            children = (XMLSequence) seq;
            for (BXML child : children.children) {
                addParent(child);
            }
        } else {
            addParent(seq);
            children = new XMLSequence(seq);
        }
    }

    /**
     * @param seq children to add to this element.
     *
     * addChildren is only used for constructing xml tree from xml literals, and only usage is to directly codegen
     * the adding children.
     *
     * @deprecated
     */
    @Override
    @Deprecated
    public void addChildren(BXML seq) {
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
                for (BXML bxml : appendingList) {
                    addParent(bxml, this);
                }
                leftList.addAll(appendingList);
            }
        } else {
            addParent(seq, this);
            leftList.add(seq);
        }
        this.children = new XMLSequence(leftList);
    }


    private void addParent(BXML child) {
        ensureAcyclicGraph(child, this);
        addParent(child, this);
    }

    // This method does not ensure acyclicness of tree after adding the children. Hence this method shold only be
    // use in scenarios where cyclic xml construction is impossible, that is only when constructing xml tree from
    // xml literal syntax, or after ensuring the new xml tree is not cyclic.
    private void addParent(BXML child, XMLItem thisElem) {
        if (child.getNodeType() == ELEMENT) {
            ((XMLItem) child).probableParents.add(new WeakReference<>(thisElem));
        }
    }

    private void ensureAcyclicGraph(BXML newSubTree, XMLItem current) {
        for (WeakReference<XMLItem> probableParentRef : current.probableParents) {
            XMLItem parent = probableParentRef.get();
            // probable parent is the actual parent.
            if (parent.children.children.contains(current)) {
                // If new subtree is in the lineage of current node, adding this newSubTree forms a cycle.
                if (parent == newSubTree) {
                    throw createXMLCycleError();
                }
                ensureAcyclicGraph(newSubTree, parent);
            }
        }
    }

    private BallerinaException createXMLCycleError() {
        return new BallerinaException(BallerinaErrorReasons.XML_OPERATION_ERROR, "Cycle detected");
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
    @Deprecated
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

        MapValue<BString, BString> attributesMap = xmlItem.getAttributesMap();
        MapValue<BString, BString> copy = (MapValue<BString, BString>) this.getAttributesMap().copy(refs);
        if (attributesMap instanceof MapValueImpl) {
            MapValueImpl<BString, BString> map = (MapValueImpl<BString, BString>) attributesMap;
            map.putAll((Map<BString, BString>) copy);
        } else {
            for (Map.Entry<BString, BString> entry : copy.entrySet()) {
                attributesMap.put(entry.getKey(), entry.getValue());
            }
        }

        if (getAttributesMap().isFrozen()) {
            attributesMap.freezeDirect();
        }
        return xmlItem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XMLValue getItem(int index) {
        if (index != 0) {
            return new XMLSequence();
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

    @Override
    protected void setAttributesOnInitialization(BMap<BString, ?> attributes) {
        setAttributes(attributes, this::setAttributeOnInitialization);
    }

    @Override
    protected void setAttributeOnInitialization(String localName, String namespace, String prefix, String value) {
        attributes.setAttribute(localName, namespace, prefix, value, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttribute(String qname) {
        if (this.type.isReadOnly()) {
            ReadOnlyUtils.handleInvalidUpdate(XML_LANG_LIB);
        }

        attributes.remove(qname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeChildren(String qname) {
        if (this.type.isReadOnly()) {
            ReadOnlyUtils.handleInvalidUpdate(XML_LANG_LIB);
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
            BXML removed = children.remove(index.intValue());
            removeParentReference(removed);
        }
    }

    private void setAttributes(BMap<BString, ?> attributes, SetAttributeFunction func) {
        String localName, uri;
        for (BString qname : attributes.getKeys()) {
            if (qname.getValue().startsWith("{") && qname.getValue().indexOf('}') > 0) {
                localName = qname.getValue().substring(qname.getValue().indexOf('}') + 1);
                uri = qname.getValue().substring(1, qname.getValue().indexOf('}'));
            } else {
                localName = qname.getValue();
                uri = STRING_NULL_VALUE;
            }

            // Validate whether the attribute name is an XML supported qualified name,
            // according to the XML recommendation.
            XMLValidator.validateXMLName(localName);
            func.set(localName, uri, STRING_NULL_VALUE, attributes.get(qname).toString());
        }
    }

    private void removeParentReference(BXML removedItem) {
        if (removedItem.getNodeType() != ELEMENT) {
            return;
        }

        XMLItem item = (XMLItem) removedItem;
        for (Iterator<WeakReference<XMLItem>> iterator = item.probableParents.iterator(); iterator.hasNext();) {
            WeakReference<XMLItem> probableParent = iterator.next();
            XMLItem parent = probableParent.get();
            if (parent == this) {
                probableParent.clear();
                iterator.remove();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void freezeDirect() {
        this.type = ReadOnlyUtils.setImmutableTypeAndGetEffectiveType(this.type);
        this.children.freezeDirect();
        this.attributes.freezeDirect();
    }

    private QName getQName(String localName, String namespaceUri, String prefix) {
        if (namespaceUri == null || namespaceUri.isEmpty()) {
            return new QName(localName);
        } else if (prefix == null || prefix.isEmpty()) {
            return new QName(namespaceUri, localName);
        } else {
            return new QName(namespaceUri, localName, prefix);
        }
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
            return other.children.size() == 1 && this.equals(other.children.get(0));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, children, attributes, probableParents);
    }

    private interface SetAttributeFunction {
        void set(String localName, String namespace, String prefix, String value);
    }

    public static XMLItem createXMLItemWithDefaultNSAttribute(QName name, boolean readonly, String defaultNsUri) {
        XMLItem item = new XMLItem(name, readonly);

        if (!defaultNsUri.isEmpty()) {
            item.setAttributeOnInitialization(XMLConstants.XMLNS_ATTRIBUTE, null, null, defaultNsUri);
        }
        return item;
    }
}
