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

package org.ballerinalang.jvm.values.api;

import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.values.CollectionValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.XMLQName;

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
 * @since 1.1.0
 */
public interface BXml<T> extends BRefValue, CollectionValue {

    /**
     * Start of a XML comment.
     */
    String COMMENT_START = "<!--";

    /**
     * End of a XML Comment.
     */
    String COMMENT_END = "-->";

    /**
     * Start of a XML processing instruction.
     */
    String PI_START = "<?";

    /**
     * End of a XML processing instruction.
     */
    String PI_END = "?>";

    /**
     * Check whether the XML sequence is empty.
     * 
     * @return Flag indicating whether the XML sequence is empty
     */
    boolean isEmpty();

    /**
     * Check whether the XML sequence contains only a single element.
     * 
     * @return Flag indicating whether the XML sequence contains only a single element
     */
    boolean isSingleton();

    /**
     * Get the type of the XML as a {@link String}. Type can be one of "element", "text", "comment" or "pi".
     * 
     * @return Type of the XML as a {@link String}
     */
    String getItemType();

    /**
     * Get the fully qualified name of the element as a {@link String}.
     * 
     * @return fully qualified name of the element as a {@link String}.
     */
    String getElementName();

    /**
     * Get the text values in this XML.
     * 
     * @return text values in this XML.
     */
    String getTextValue();

    /**
     * Get the value of a single attribute as a string.
     * 
     * @param localName Local name of the attribute
     * @param namespace Namespace of the attribute
     * @return Value of the attribute
     */
    String getAttribute(String localName, String namespace);

    /**
     * Get the value of a single attribute as a string.
     * 
     * @param localName Local name of the attribute
     * @param namespace Namespace of the attribute
     * @param prefix Prefix of the namespace
     * @return Value of the attribute
     */
    String getAttribute(String localName, String namespace, String prefix);

    /**
     * Get the value of a single attribute as a string.
     * 
     * @param attributeName Qualified name of the attribute
     * @return Value of the attribute
     */
    String getAttribute(XMLQName attributeName);

    /**
     * Set the value of a single attribute. If the attribute already exsists, then the value will be updated.
     * Otherwise a new attribute will be added.
     * 
     * @param namespace Namespace of the attribute
     * @param prefix Namespace prefix of the attribute
     * @param localName Local name of the attribute
     * @param value Value of the attribute
     */
    void setAttribute(String localName, String namespace, String prefix, String value);

    /**
     * Set the value of a single attribute. If the attribute already exsists, then the value will be updated.
     * Otherwise a new attribute will be added.
     * 
     * @param attributeName Qualified name of the attribute
     * @param value Value of the attribute
     */
    void setAttribute(XMLQName attributeName, String value);

    /**
     * Get attributes as a {@link MapValueImpl}.
     * 
     * @return Attributes as a {@link MapValueImpl}
     */
    MapValue<String, ?> getAttributesMap();

    /**
     * Set the attributes of the XML{@link MapValueImpl}.
     * 
     * @param attributes Attributes to be set.
     */
    void setAttributes(MapValue<String, ?> attributes);

    /**
     * Get all the elements-type items, in the given sequence.
     * 
     * @return All the elements-type items, in the given sequence
     */
    BXml<?> elements();

    /**
     * Get all the elements-type items in the given sequence, that matches a given qualified name.
     * 
     * @param qname qualified name of the element
     * @return All the elements-type items, that matches a given qualified name, from the this sequence.
     */
    BXml<?> elements(String qname);

    /**
     * Selects and concatenate all the children sequences of the elements in this sequence.
     * 
     * @return All the children sequences of the elements in this sequence
     */
    BXml<?> children();

    /**
     * Selects and concatenate all the children sequences that matches the given qualified name,
     * in all the element-type items in this sequence. Only the children will be selected, but not
     * the nested children.
     * 
     * @param qname qualified name of the children to filter
     * @return All the children that matches the given qualified name, as a sequence
     */
    BXml<?> children(String qname);

    /**
     * Set the children of this XML. Any existing children will be removed.
     * 
     * @param seq XML Sequence to be set as the children
     */
    void setChildren(BXml<?> seq);

    /**
     * Add a XMl sequence to this XML as children.
     * 
     * @param seq XML Sequence to be added as the children
     */
    void addChildren(BXml<?> seq);

    /**
     * Strips any text items from the XML that are all whitespace.
     *
     * @return striped xml
     */
    BXml<?> strip();

    /**
     * Get the type of the XML.
     * 
     * @return Type of the XML
     */
    XMLNodeType getNodeType();

    /**
     * Slice and return a subsequence of the given XML sequence.
     * 
     * @param startIndex To slice
     * @param endIndex To slice
     * @return sliced sequence
     */
    BXml<?> slice(long startIndex, long endIndex);

    /**
     * Searches in children recursively for elements matching the name and returns a sequence containing them all.
     * Does not search within a matched result.
     * 
     * @param qname Qualified name of the descendants to filter
     * @return All the descendants that matches the given qualified name, as a sequence
     */
    BXml<?> descendants(String qname);

    /**
     * Get an item from the XML sequence, at the given index.
     * 
     * @param index Index of the item to retrieve
     * @return Item at the given index in the sequence
     */
    BXml<?> getItem(int index);

    /**
     * Get the length of this XML sequence.
     * 
     * @return length of this XML sequence.
     */
    int size();

    /**
     * Builds itself.
     */
    void build();


    /**
     * Remove an attribute from the XML.
     * 
     * @param qname Qualified name of the attribute
     */
    void removeAttribute(String qname);

    /**
     * Remove children matching the given name from an XML.
     * 
     * @param qname Namespace qualified name of the children to be removed.
     */
    void removeChildren(String qname);

    /**
     * Returns the type of the underlying implementation.
     *
     * @return underlying implementation type.
     */
    T value();
}
