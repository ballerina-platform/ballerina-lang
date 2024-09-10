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

package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlItem;
import io.ballerina.runtime.api.values.BXmlSequence;
import io.ballerina.runtime.internal.BallerinaXmlSerializer;
import io.ballerina.runtime.internal.XmlFactory;
import io.ballerina.runtime.internal.XmlValidator;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.errors.ErrorReasons;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNode;

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
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static io.ballerina.runtime.api.constants.RuntimeConstants.STRING_NULL_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.XML_LANG_LIB;
import static io.ballerina.runtime.api.types.XmlNodeType.ELEMENT;
import static io.ballerina.runtime.api.types.XmlNodeType.TEXT;
import static io.ballerina.runtime.internal.TypeChecker.isEqual;

/**
 * {@code XMLItem} represents a single XML element in Ballerina.
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 * @since 0.995.0
 */
public final class XmlItem extends XmlValue implements BXmlItem {

    private QName name;
    private XmlSequence children;
    private AttributeMapValueImpl attributes;
    // Keep track of probable parents of xml element to detect probable cycles in xml.
    private List<WeakReference<XmlItem>> probableParents;

    public XmlItem(QName name, XmlSequence children, boolean readonly) {
        this.name = name;
        this.children = children;
        for (BXml child : children.children) {
            addParent(child, this);
        }
        attributes = new AttributeMapValueImpl(false);
        addDefaultNamespaceAttribute(name, attributes);
        probableParents = new ArrayList<>();
        this.type = PredefinedTypes.TYPE_ELEMENT;
        this.type = readonly ? PredefinedTypes.TYPE_READONLY_ELEMENT : PredefinedTypes.TYPE_ELEMENT;
    }

    public XmlItem(QName name, XmlSequence children) {
        this(name, children, false);
    }

    /**
     * Initialize a {@link XmlItem}.
     *
     * @param name element's qualified name
     */
    public XmlItem(QName name) {
        this(name, new XmlSequence(new ArrayList<>()));
        this.type = PredefinedTypes.TYPE_ELEMENT;
    }

    public XmlItem(QName name, boolean readonly) {
        XmlSequence children = new XmlSequence(new ArrayList<>());
        this.name = name;
        this.children = children;
        for (BXml child : children.children) {
            addParent(child, this);
        }
        attributes = new AttributeMapValueImpl(readonly);
        addDefaultNamespaceAttribute(name, attributes);
        probableParents = new ArrayList<>();

        this.type = readonly ? PredefinedTypes.TYPE_READONLY_ELEMENT : PredefinedTypes.TYPE_ELEMENT;
    }
    private void addDefaultNamespaceAttribute(QName name, AttributeMapValueImpl attributes) {
        String namespace = name.getNamespaceURI();
        if (namespace == null || namespace.isEmpty()) {
            return;
        }

        String prefix = name.getPrefix();
        BString xmlnsPrefix;
        if (prefix == null || prefix.isEmpty() || prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            xmlnsPrefix = XMLNS_PREFIX;
        } else {
            xmlnsPrefix = StringUtils.fromString(XMLNS_NS_URI_PREFIX + prefix);
        }
        attributes.populateInitialValue(xmlnsPrefix, StringUtils.fromString(namespace));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XmlNodeType getNodeType() {
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

    @Override
    public QName getQName() {
        return this.name;
    }

    @Override
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
            BString xmlnsPrefix;
            if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                xmlnsPrefix = XMLNS_PREFIX;
            } else {
                xmlnsPrefix = StringUtils.fromString(XMLNS_NS_URI_PREFIX + prefix);
            }
            String ns = attributes.get(xmlnsPrefix).getValue();
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
    public void setAttributes(BMap<BString, BString> attributes) {
        if (this.type.isReadOnly()) {
            ReadOnlyUtils.handleInvalidUpdate(XML_LANG_LIB);
        }

        setAttributes(attributes, this::setAttribute);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XmlValue elements() {
        ArrayList<BXml> children = new ArrayList<>();
        children.add(this);
        return new XmlSequence(children);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XmlValue elements(String qname) {
        ArrayList<BXml> children = new ArrayList<>();
        if (getElementName().equals(getQname(qname).toString())) {
            children.add(this);
        }
        return new XmlSequence(children);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XmlValue children() {
        return new XmlSequence(new ArrayList<>(children.getChildrenList()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XmlValue children(String qname) {
        return children.elements(qname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChildren(BXml seq) {
        if (this.type.isReadOnly()) {
            ReadOnlyUtils.handleInvalidUpdate(XML_LANG_LIB);
        }

        if (seq == null) {
            return;
        }

        if (seq.getNodeType() == XmlNodeType.SEQUENCE) {
            children = (XmlSequence) seq;
            for (BXml child : children.children) {
                addParent(child);
            }
        } else {
            addParent(seq);
            children = new XmlSequence(seq);
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
    public void addChildren(BXml seq) {
        if (seq == null) {
            return;
        }

        List<BXml> leftList = new ArrayList<>(children.children);

        if (seq.getNodeType() == XmlNodeType.SEQUENCE) {
            List<BXml> appendingList = ((XmlSequence) seq).getChildrenList();
            if (isLastChildIsTextNode(leftList) && !appendingList.isEmpty()
                    && appendingList.get(0).getNodeType() == TEXT) {
                mergeAdjoiningTextNodesIntoList(leftList, appendingList);
            } else {
                for (BXml bxml : appendingList) {
                    addParent(bxml, this);
                }
                leftList.addAll(appendingList);
            }
        } else {
            addParent(seq, this);
            leftList.add(seq);
        }
        this.children = new XmlSequence(leftList);
    }


    private void addParent(BXml child) {
        ensureAcyclicGraph(child, this);
        addParent(child, this);
    }

    // This method does not ensure acyclicness of tree after adding the children. Hence this method shold only be
    // use in scenarios where cyclic xml construction is impossible, that is only when constructing xml tree from
    // xml literal syntax, or after ensuring the new xml tree is not cyclic.
    private void addParent(BXml child, XmlItem thisElem) {
        if (child.getNodeType() == ELEMENT) {
            ((XmlItem) child).probableParents.add(new WeakReference<>(thisElem));
        }
    }

    private void ensureAcyclicGraph(BXml newSubTree, XmlItem current) {
        for (WeakReference<XmlItem> probableParentRef : current.probableParents) {
            XmlItem parent = probableParentRef.get();
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

    private BError createXMLCycleError() {
        return ErrorCreator.createError(ErrorReasons.XML_OPERATION_ERROR,
                StringUtils.fromString("Cycle detected"));
    }

    private void mergeAdjoiningTextNodesIntoList(List leftList, List<BXml> appendingList) {
        XmlText lastChild = (XmlText) leftList.get(leftList.size() - 1);
        String firstChildContent = appendingList.get(0).getTextValue();
        String mergedTextContent = lastChild.getTextValue() + firstChildContent;
        XmlText text = new XmlText(mergedTextContent);
        leftList.set(leftList.size() - 1, text);
        for (int i = 1; i < appendingList.size(); i++) {
            leftList.add(appendingList.get(i));
        }
    }

    private boolean isLastChildIsTextNode(List<BXml> childList) {
        return !childList.isEmpty() && childList.get(childList.size() - 1).getNodeType() == TEXT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XmlValue strip() {
       return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XmlValue slice(long startIndex, long endIndex) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XmlValue descendants(List<String> qnames) {
        if (qnames.contains(getQName().toString())) {
            List<BXml> descendants = Arrays.asList(this);
            addDescendants(descendants, this, qnames);
            return new XmlSequence(descendants);
        }
        return children.descendants(qnames);
    }

    @Override
    public XmlValue descendants() {
        List<BXml> descendants = new ArrayList<>();
        addDescendants(descendants, this);
        return new XmlSequence(descendants);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OMNode value() {
        try {
            String xmlStr = this.stringValue(null);
            OMElement omElement = XmlFactory.stringToOM(xmlStr);
            return omElement;
        } catch (BError e) {
            throw e;
        } catch (OMException | XMLStreamException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw ErrorCreator.createError(StringUtils.fromString((cause.getMessage())));
        } catch (Throwable e) {
            throw ErrorCreator.createError(StringUtils.fromString((XmlFactory.PARSE_ERROR_PREFIX + e.getMessage())));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.stringValue(null);
    }

    /**
     * {@inheritDoc}
     * @param parent The link to the parent node
     */
    @Override
    @Deprecated
    public String stringValue(BLink parent) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BallerinaXmlSerializer ballerinaXMLSerializer = new BallerinaXmlSerializer(outputStream);
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

    @Override
    public String informalStringValue(BLink parent) {
        return "`" + toString() + "`";
    }

    @Override
    public String expressionStringValue(BLink parent) {
        return "xml`" + toString() + "`";
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
        XmlItem xmlItem = new XmlItem(elemName, (XmlSequence) children.copy(refs));

        MapValue<BString, BString> attributesMap = xmlItem.getAttributesMap();
        MapValue<BString, BString> copy = (MapValue<BString, BString>) this.getAttributesMap().copy(refs);
        if (attributesMap instanceof MapValueImpl<BString, BString> map) {
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
    public XmlValue getItem(int index) {
        if (index == 0) {
            return this;
        }
        if (index > 0) {
            return new XmlSequence();
        }
        throw ErrorHelper.getRuntimeException(
                ErrorCodes.XML_SEQUENCE_INDEX_OUT_OF_RANGE, 1, index);
    }

    @Override
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
    protected void setAttributesOnInitialization(BMap<BString, BString> attributes) {
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

        List<BXml> children = this.children.children;
        List<Integer> toRemove = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {
            BXml child = children.get(i);
            if (child.getNodeType() == ELEMENT && child.getElementName().equals(qname)) {
                toRemove.add(i);
            }
        }

        Collections.reverse(toRemove);
        for (Integer index : toRemove) {
            BXml removed = children.remove(index.intValue());
            removeParentReference(removed);
        }
    }

    private void setAttributes(BMap<BString, BString> attributes, SetAttributeFunction func) {
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
            XmlValidator.validateXMLName(localName);
            func.set(localName, uri, STRING_NULL_VALUE, attributes.get(qname).toString());
        }
    }

    private void removeParentReference(BXml removedItem) {
        if (removedItem.getNodeType() != ELEMENT) {
            return;
        }

        XmlItem item = (XmlItem) removedItem;
        for (Iterator<WeakReference<XmlItem>> iterator = item.probableParents.iterator(); iterator.hasNext();) {
            WeakReference<XmlItem> probableParent = iterator.next();
            XmlItem parent = probableParent.get();
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
        this.typedesc = null;
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

    @Override
    public BXmlSequence getChildrenSeq() {
        return children;
    }

    @Override
    public IteratorValue getIterator() {
        XmlItem that = this;
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
    public int hashCode() {
        return Objects.hash(name, children, attributes, probableParents);
    }

    /**
     * Deep equality check for XML Item.
     *
     * @param o The XML Item to be compared
     * @param visitedValues Visited values due to circular references
     * @return True if the XML Items are equal; False otherwise
     */
    @Override
    public boolean equals(Object o, Set<ValuePair> visitedValues) {
        if (o instanceof XmlItem rhsXMLItem) {
            if (!(rhsXMLItem.getQName().equals(this.getQName()))) {
                return false;
            }
            if (!(rhsXMLItem.getAttributesMap().entrySet().equals(this.getAttributesMap().entrySet()))) {
                return false;
            }
            return isEqual(rhsXMLItem.getChildrenSeq(), this.getChildrenSeq());
        }
        if (o instanceof XmlSequence rhsXMLSequence) {
            return rhsXMLSequence.getChildrenList().size() == 1 &&
                    isEqual(this, rhsXMLSequence.getChildrenList().get(0));
        }
        return false;
    }

    private interface SetAttributeFunction {
        void set(String localName, String namespace, String prefix, String value);
    }

    public static XmlItem createXMLItemWithDefaultNSAttribute(QName name, boolean readonly, String defaultNsUri) {
        XmlItem item = new XmlItem(name, readonly);

        if (!defaultNsUri.isEmpty()) {
            item.setAttributeOnInitialization(XMLConstants.XMLNS_ATTRIBUTE, null, null, defaultNsUri);
        }
        return item;
    }
}
