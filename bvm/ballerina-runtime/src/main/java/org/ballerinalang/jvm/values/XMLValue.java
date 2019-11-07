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
import org.ballerinalang.jvm.TypeConverter;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.commons.TypeValuePair;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.api.BXml;
import org.ballerinalang.jvm.values.freeze.State;
import org.ballerinalang.jvm.values.freeze.Status;

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * {@code BXML} represents an XML in Ballerina. An XML could be one of:
 * <ul>
 * <li>element</li>
 * <li>text</li>
 * <li>comment</li>
 * <li>processing instruction</li>
 * <li>sequence of above</li>
 * </ul>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 * 
 * @param <T> Type of the underlying impl
 * @since 0.995.0
 */
public abstract class XMLValue<T> implements RefValue, BXml<T> {

    BType type = BTypes.typeXML;

    /**
     * Start of a XML comment.
     */
    public static final String COMMENT_START = "<!--";

    /**
     * End of a XML Comment.
     */
    public static final String COMMENT_END = "-->";

    /**
     * Start of a XML processing instruction.
     */
    public static final String PI_START = "<?";

    /**
     * End of a XML processing instruction.
     */
    public static final String PI_END = "?>";

    protected volatile Status freezeStatus = new Status(State.UNFROZEN);

    /**
     * Check whether the XML sequence is empty.
     * 
     * @return Flag indicating whether the XML sequence is empty
     */
    public abstract boolean isEmpty();

    /**
     * Check whether the XML sequence contains only a single element.
     * 
     * @return Flag indicating whether the XML sequence contains only a single element
     */
    public abstract boolean isSingleton();

    /**
     * Get the type of the XML as a {@link String}. Type can be one of "element", "text", "comment" or "pi".
     * 
     * @return Type of the XML as a {@link String}
     */
    public abstract String getItemType();

    /**
     * Get the fully qualified name of the element as a {@link String}.
     * 
     * @return fully qualified name of the element as a {@link String}.
     */
    public abstract String getElementName();

    /**
     * Get the text values in this XML.
     * 
     * @return text values in this XML.
     */
    public abstract String getTextValue();

    /**
     * Get the value of a single attribute as a string.
     * 
     * @param localName Local name of the attribute
     * @param namespace Namespace of the attribute
     * @return Value of the attribute
     */
    public abstract String getAttribute(String localName, String namespace);

    /**
     * Get the value of a single attribute as a string.
     * 
     * @param localName Local name of the attribute
     * @param namespace Namespace of the attribute
     * @param prefix Prefix of the namespace
     * @return Value of the attribute
     */
    public abstract String getAttribute(String localName, String namespace, String prefix);

    /**
     * Get the value of a single attribute as a string.
     * 
     * @param attributeName Qualified name of the attribute
     * @return Value of the attribute
     */
    public String getAttribute(XMLQName attributeName) {
        return getAttribute(attributeName.getLocalName(), attributeName.getUri(), attributeName.getPrefix());
    }

    /**
     * Set the value of a single attribute. If the attribute already exsists, then the value will be updated.
     * Otherwise a new attribute will be added.
     * 
     * @param namespace Namespace of the attribute
     * @param prefix Namespace prefix of the attribute
     * @param localName Local name of the attribute
     * @param value Value of the attribute
     */
    public abstract void setAttribute(String localName, String namespace, String prefix, String value);

    /**
     * Set the value of a single attribute. If the attribute already exsists, then the value will be updated.
     * Otherwise a new attribute will be added.
     * 
     * @param attributeName Qualified name of the attribute
     * @param value Value of the attribute
     */
    public void setAttribute(XMLQName attributeName, String value) {
        setAttribute(attributeName.getLocalName(), attributeName.getUri(), attributeName.getPrefix(), value);
    }

    /**
     * Get attributes as a {@link MapValueImpl}.
     * 
     * @return Attributes as a {@link MapValueImpl}
     */
    public abstract MapValue<String, ?> getAttributesMap();

    /**
     * Set the attributes of the XML{@link MapValueImpl}.
     * 
     * @param attributes Attributes to be set.
     */
    public abstract void setAttributes(MapValue<String, ?> attributes);

    /**
     * Get all the elements-type items, in the given sequence.
     * 
     * @return All the elements-type items, in the given sequence
     */
    public abstract XMLValue<?> elements();

    /**
     * Get all the elements-type items in the given sequence, that matches a given qualified name.
     * 
     * @param qname qualified name of the element
     * @return All the elements-type items, that matches a given qualified name, from the this sequence.
     */
    public abstract XMLValue<?> elements(String qname);

    /**
     * Selects and concatenate all the children sequences of the elements in this sequence.
     * 
     * @return All the children sequences of the elements in this sequence
     */
    public abstract XMLValue<?> children();

    /**
     * Selects and concatenate all the children sequences that matches the given qualified name,
     * in all the element-type items in this sequence. Only the children will be selected, but not
     * the nested children.
     * 
     * @param qname qualified name of the children to filter
     * @return All the children that matches the given qualified name, as a sequence
     */
    public abstract XMLValue<?> children(String qname);

    /**
     * Set the children of this XML. Any existing children will be removed.
     * 
     * @param seq XML Sequence to be set as the children
     */
    public abstract void setChildren(XMLValue<?> seq);

    /**
     * Add a XMl sequence to this XML as children.
     * 
     * @param seq XML Sequence to be added as the children
     */
    public abstract void addChildren(XMLValue<?> seq);

    /**
     * Strips any text items from the XML that are all whitespace.
     *
     * @return striped xml
     */
    public abstract XMLValue<?> strip();

    /**
     * Get the type of the XML.
     * 
     * @return Type of the XML
     */
    public abstract XMLNodeType getNodeType();

    /**
     * Slice and return a subsequence of the given XML sequence.
     * 
     * @param startIndex To slice
     * @param endIndex To slice
     * @return sliced sequence
     */
    public abstract XMLValue<?> slice(long startIndex, long endIndex);

    /**
     * Searches in children recursively for elements matching the name and returns a sequence containing them all.
     * Does not search within a matched result.
     * 
     * @param qname Qualified name of the descendants to filter
     * @return All the descendants that matches the given qualified name, as a sequence
     */
    public abstract XMLValue<?> descendants(String qname);

    /**
     * Get an item from the XML sequence, at the given index.
     * 
     * @param index Index of the item to retrieve
     * @return Item at the given index in the sequence
     */
    public abstract XMLValue<?> getItem(int index);

    /**
     * Get the length of this XML sequence.
     * 
     * @return length of this XML sequence.
     */
    public abstract int size();

    /**
     * Builds itself.
     */
    public abstract void build();

    /**
     * {@inheritDoc}
     */
    @Override
    public BType getType() {
        return type;
    }

    @Override
    public void stamp(BType type, List<TypeValuePair> unresolvedValues) {
        if (type.getTag() == TypeTags.ANYDATA_TAG) {
            type = TypeConverter.resolveMatchingTypeForUnion(this, type);
        }
        this.type = type;
    }
    // private methods

    protected static void handleXmlException(String message, Throwable t) {
        // Here local message of the cause is logged whenever possible, to avoid java class being logged
        // along with the error message.
        if (t.getCause() != null) {
            throw new BallerinaException(message + t.getCause().getMessage());
        }

        throw new BallerinaException(message + t.getMessage());
    }

    /**
     * Get the {@link QName} from {@link String}.
     *
     * @param qname String representation of qname
     * @return constructed {@link QName}
     */
    protected QName getQname(String qname) {
        String nsUri;
        String localname;
        int rParenIndex = qname.indexOf('}');

        if (qname.startsWith("{") && rParenIndex > 0) {
            localname = qname.substring(rParenIndex + 1, qname.length());
            nsUri = qname.substring(1, rParenIndex);
        } else {
            localname = qname;
            nsUri = "";
        }

        return new QName(nsUri, localname);
    }

    /**
     * Recursively traverse and add the descendant with the given name to the descendants list.
     * 
     * @param descendants List to add descendants
     * @param currentElement Current node
     * @param qname Qualified name of the descendants to search
     */
    @SuppressWarnings("unchecked")
    protected void addDescendants(List<XMLValue<?>> descendants, OMElement currentElement, String qname) {
        Iterator<OMNode> childrenItr = currentElement.getChildren();
        while (childrenItr.hasNext()) {
            OMNode child = childrenItr.next();
            if (child.getType() != OMNode.ELEMENT_NODE) {
                continue;
            }
            if (qname.equals(((OMElement) child).getQName().toString())) {
                descendants.add(new XMLItem(child));
                continue;
            }
            addDescendants(descendants, (OMElement) child, qname);
        }
    }

    /**
     * Remove an attribute from the XML.
     * 
     * @param qname Qualified name of the attribute
     */
    public abstract void removeAttribute(String qname);

    /**
     * Remove children matching the given name from an XML.
     * 
     * @param qname Namespace qualified name of the children to be removed.
     */
    public abstract void removeChildren(String qname);

    /**
     * {@inheritDoc}
     */
    public synchronized boolean isFrozen() {
        return this.freezeStatus.isFrozen();
    }

    public abstract T value();
}
