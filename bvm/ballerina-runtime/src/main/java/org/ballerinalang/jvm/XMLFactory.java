/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.jvm;

import org.apache.axiom.om.DeferredParsingException;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.util.StAXParserConfiguration;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeConstants;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.XMLComment;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLPi;
import org.ballerinalang.jvm.values.XMLQName;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLText;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BXML;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static org.ballerinalang.jvm.values.XMLItem.createXMLItemWithDefaultNSAttribute;

/**
 * Common utility methods used for XML manipulation.
 * 
 * @since 0.995.0
 */
public class XMLFactory {

    private static final String XML_NAMESPACE_PREFIX = "xmlns:";
    private static final BString XML_VALUE_TAG = StringUtils.fromString("#text");
    private static final String XML_DCLR_START = "<?xml";

    private static final BType jsonMapType =
            new BMapType(TypeConstants.MAP_TNAME, BTypes.typeJSON, new BPackage(null, null, null));
    public static final StAXParserConfiguration STAX_PARSER_CONFIGURATION = StAXParserConfiguration.STANDALONE;

    /**
     * Create a XML item from string literal.
     *
     * @param xmlStr String representation of the XML
     * @return XML sequence
     */
    public static XMLValue parse(String xmlStr) {
        try {
            if (xmlStr.isEmpty()) {
                return new XMLSequence();
            }

            XMLTreeBuilder treeBuilder = new XMLTreeBuilder(xmlStr);
            return treeBuilder.parse();
        } catch (ErrorValue e) {
            throw e;
        } catch (Throwable e) {
            throw BallerinaErrors.createError("failed to parse xml: " + e.getMessage());
        }
    }

    /**
     * Create a XML sequence from string inputstream.
     *
     * @param xmlStream XML input stream
     * @return XML Sequence
     */
    public static XMLValue parse(InputStream xmlStream) {
        try {
            XMLTreeBuilder treeBuilder = new XMLTreeBuilder(new InputStreamReader(xmlStream));
            return treeBuilder.parse();
        } catch (DeferredParsingException e) {
            throw BallerinaErrors.createError(e.getCause().getMessage());
        } catch (Throwable e) {
            throw BallerinaErrors.createError("failed to create xml: " + e.getMessage());
        }
    }

    /**
     * Create a XML sequence from string inputstream with a given charset.
     *
     * @param xmlStream XML imput stream
     * @param charset Charset to be used for parsing
     * @return XML Sequence
     */
    public static XMLValue parse(InputStream xmlStream, String charset) {
        try {
            XMLTreeBuilder xmlTreeBuilder = new XMLTreeBuilder(new InputStreamReader(xmlStream, charset));
            return xmlTreeBuilder.parse();
        } catch (DeferredParsingException e) {
            throw BallerinaErrors.createError(e.getCause().getMessage());
        } catch (Throwable e) {
            throw BallerinaErrors.createError("failed to create xml: " + e.getMessage());
        }
    }

    /**
     * Create a XML sequence from string reader.
     *
     * @param reader XML reader
     * @return XML Sequence
     */
    public static XMLValue parse(Reader reader) {
        try {
            XMLTreeBuilder xmlTreeBuilder = new XMLTreeBuilder(reader);
            return xmlTreeBuilder.parse();
        } catch (DeferredParsingException e) {
            throw BallerinaErrors.createError(e.getCause().getMessage());
        } catch (Throwable e) {
            throw BallerinaErrors.createError("failed to create xml: " + e.getMessage());
        }
    }

    /**
     * Concatenate two XML sequences and produce a single sequence.
     *
     * @param firstSeq First XML sequence
     * @param secondSeq Second XML sequence
     * @return Concatenated XML sequence
     */
    public static XMLValue concatenate(XMLValue firstSeq, XMLValue secondSeq) {
        ArrayList<BXML> concatenatedList = new ArrayList<>();

        if (firstSeq.getNodeType() == XMLNodeType.TEXT && secondSeq.getNodeType() == XMLNodeType.TEXT) {
            return new XMLText(firstSeq.getTextValue() + secondSeq.getTextValue());
        }

        // Add all the items in the first sequence
        if (firstSeq.getNodeType() == XMLNodeType.SEQUENCE) {
            concatenatedList.addAll(((XMLSequence) firstSeq).getChildrenList());
        } else if (!firstSeq.isEmpty()) {
            concatenatedList.add(firstSeq);
        }

        // When last item of left seq and first item of right seq are both text nodes merge them into single consecutive
        // text node.
        if (!concatenatedList.isEmpty()) {
            int lastIndexOFLeftChildren = concatenatedList.size() - 1;
            BXML lastItem = concatenatedList.get(lastIndexOFLeftChildren);
            if (lastItem.getNodeType() == XMLNodeType.TEXT && secondSeq.getNodeType() == XMLNodeType.SEQUENCE) {
                List<BXML> rightChildren = ((XMLSequence) secondSeq).getChildrenList();
                if (!rightChildren.isEmpty()) {
                    BXML firsOfRightSeq = rightChildren.get(0);
                    if (firsOfRightSeq.getNodeType() == XMLNodeType.TEXT) {
                        concatenatedList.remove(lastIndexOFLeftChildren); // remove last item, from already copied list
                        concatenatedList.addAll(rightChildren);
                        String merged = ((XMLText) lastItem).getTextValue() + ((XMLText) firsOfRightSeq).getTextValue();
                        concatenatedList.set(lastIndexOFLeftChildren, new XMLText(merged));
                        return new XMLSequence(concatenatedList);
                    }
                }
            } else if (lastItem.getNodeType() == XMLNodeType.TEXT && secondSeq.getNodeType() == XMLNodeType.TEXT) {
                String merged = lastItem.getTextValue() + secondSeq.getTextValue();
                concatenatedList.set(lastIndexOFLeftChildren, new XMLText(merged));
                return new XMLSequence(concatenatedList);
            }
        }

        // Add all the items in the second sequence
        if (secondSeq.getNodeType() == XMLNodeType.SEQUENCE) {
            concatenatedList.addAll(((XMLSequence) secondSeq).getChildrenList());
        } else if (!secondSeq.isEmpty()) {
            concatenatedList.add(secondSeq);
        }

        return new XMLSequence(concatenatedList);
    }

    //TODO Table remove - Fix
//    /**
//     * Converts a {@link org.ballerinalang.jvm.values.TableValue} to {@link XMLValue}.
//     *
//     * @param table {@link org.ballerinalang.jvm.values.TableValue} to convert
//     * @return converted {@link XMLValue}
//     */
//    public static XMLValue tableToXML(TableValue table) {
//        // todo: Implement table to xml (issue #19910)
//
//        try {
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            XMLStreamWriter streamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
//            TableOMDataSource tableOMDataSource = new TableOMDataSource(table, null, null);
//            tableOMDataSource.serialize(streamWriter);
//            streamWriter.flush();
//            outputStream.flush();
//            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
//            return parse(inputStream);
//        } catch (IOException | XMLStreamException e) {
//            throw new BallerinaException(e);
//        }
//    }

    /**
     * Create an element type XMLValue.
     *
     * @param startTagName Name of the start tag of the element
     * @param endTagName Name of the end tag of element
     * @param defaultNsUri Default namespace URI
     * @return XMLValue Element type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLElement(XMLQName startTagName, XMLQName endTagName, String defaultNsUri) {
        if (!StringUtils.isEqual(startTagName.getLocalName(), endTagName.getLocalName()) ||
                !StringUtils.isEqual(startTagName.getUri(), endTagName.getUri()) ||
                !StringUtils.isEqual(startTagName.getPrefix(), endTagName.getPrefix())) {
            throw BallerinaErrors
                    .createError("start and end tag names mismatch: '" + startTagName + "' and '" + endTagName + "'");
        }
        return createXMLElement(startTagName, defaultNsUri);
    }

    /**
     * Create an element type XMLValue.
     *
     * @param startTagName Name of the start tag of the element
     * @param defaultNsUri Default namespace URI
     * @return XMLValue Element type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLElement(XMLQName startTagName, String defaultNsUri) {
        return createXMLElement(startTagName, defaultNsUri, false);
    }

    public static XMLValue createXMLElement(XMLQName startTagName, BString defaultNsUriVal) {
        return createXMLElement(startTagName,
                defaultNsUriVal == null ? XMLConstants.NULL_NS_URI : defaultNsUriVal.getValue());
    }

    /**
     * Create an element type XMLValue, specifying the type which will indicate mutability.
     *
     * @param startTagName  Name of the start tag of the element
     * @param defaultNsUri  Default namespace URI
     * @param readonly      Whether the element is immutable
     * @return XMLValue Element type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLElement(XMLQName startTagName, String defaultNsUri, boolean readonly) {
        // Validate whether the tag names are XML supported qualified names, according to the XML recommendation.
        XMLValidator.validateXMLQName(startTagName);

        String nsUri = startTagName.getUri();
        if (defaultNsUri == null) {
            defaultNsUri = XMLConstants.NULL_NS_URI;
        }

        String prefix = startTagName.getPrefix() == null ? XMLConstants.DEFAULT_NS_PREFIX : startTagName.getPrefix();

        if (nsUri == null) {
            return new XMLItem(new QName(defaultNsUri, startTagName.getLocalName(), prefix), readonly);
        }
        return createXMLItemWithDefaultNSAttribute(new QName(nsUri, startTagName.getLocalName(), prefix), readonly,
                                                   defaultNsUri);
    }

    public static XMLValue createXMLElement(XMLQName startTagName, BString defaultNsUriVal, boolean readonly) {
        return createXMLElement(startTagName,
                                defaultNsUriVal == null ? XMLConstants.NULL_NS_URI : defaultNsUriVal.getValue(),
                                readonly);
    }

    /**
     * Create a comment type XMLValue.
     *
     * @param content Comment content
     * @return XMLValue Comment type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLComment(String content) {
        return new XMLComment(content);
    }

    /**
     * Create a comment type XMLValue.
     *
     * @param content Comment content
     * @return XMLValue Comment type XMLValue
     */
    public static XMLValue createXMLComment(BString content) {
        return createXMLComment(content.getValue());
    }

    /**
     * Create a comment type XMLValue, specifying the type which will indicate mutability.
     *
     * @param content   Comment content
     * @param readonly  Whether the comment is immutable
     * @return XMLValue Comment type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLComment(String content, boolean readonly) {
        return new XMLComment(content, readonly);
    }

    /**
     * Create a comment type XMLValue, specifying the type which will indicate mutability.
     *
     * @param content   Comment content
     * @param readonly  Whether the comment is immutable
     * @return XMLValue Comment type XMLValue
     */
    public static XMLValue createXMLComment(BString content, boolean readonly) {
        return createXMLComment(content.getValue(), readonly);
    }

    /**
     * Create a comment type XMLValue.
     *
     * @param content Text content
     * @return XMLValue Text type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLText(String content) {
        return new XMLText(XMLTextUnescape.unescape(content));
    }

    /**
     * Create a comment type XMLValue.
     *
     * @param contentVal Text content
     * @return XMLValue Text type XMLValue
     */
    public static XMLValue createXMLText(BString contentVal) {
        return createXMLText(contentVal.getValue());
    }

    /**
     * Create a processing instruction type XMLValue.
     *
     * @param tartget PI target
     * @param data    PI data
     * @return XMLValue Processing instruction type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLProcessingInstruction(String tartget, String data) {
        return new XMLPi(data, tartget);
    }

    /**
     * Create a processing instruction type XMLValue.
     *
     * @param tartget PI target
     * @param data    PI data
     * @return XMLValue Processing instruction type XMLValue
     */
    public static XMLValue createXMLProcessingInstruction(BString tartget, BString data) {
        return createXMLProcessingInstruction(tartget.getValue(), data.getValue());
    }

    /**
     * Create a processing instruction type XMLValue, specifying the type which will indicate mutability.
     *
     * @param target    PI target
     * @param data      PI data
     * @param readonly  Whether the PI is immutable
     * @return XMLValue Processing instruction type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLProcessingInstruction(String target, String data, boolean readonly) {
        return new XMLPi(data, target, readonly);
    }

    /**
     * Create a processing instruction type XMLValue, specifying the type which will indicate mutability.
     *
     * @param target    PI target
     * @param data      PI data
     * @param readonly  Whether the PI is immutable
     * @return XMLValue Processing instruction type XMLValue
     */
    public static XMLValue createXMLProcessingInstruction(BString target, BString data, boolean readonly) {
        return createXMLProcessingInstruction(target.getValue(), data.getValue(), readonly);
    }

    /**
     * Converts given xml object to the corresponding json.
     *
     * @param xml                XML object to get the corresponding json
     * @param attributePrefix    Prefix to use in attributes
     * @param preserveNamespaces preserve the namespaces when converting
     * @return BJSON JSON representation of the given xml object
     */
    @SuppressWarnings("rawtypes")
    public static Object convertToJSON(XMLValue xml, String attributePrefix, boolean preserveNamespaces) {
        switch (xml.getNodeType()) {
            case TEXT:
                return JSONParser.parse("\"" + ((XMLText) xml).stringValue() + "\"");
            case ELEMENT:
                return traverseXMLElement((XMLItem) xml, attributePrefix, preserveNamespaces);
            case SEQUENCE:
                XMLSequence xmlSequence = (XMLSequence) xml;
                if (xmlSequence.isEmpty()) {
                    return new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
                }
                return traverseXMLSequence(xmlSequence, attributePrefix, preserveNamespaces);
            default:
                return new MapValueImpl<>(jsonMapType);
        }
    }

    /**
     * Compares if two xml values are equal.
     *
     * Equality is computed as follows
     * - for XML elements: compares the canonicalized versions, including comments
     * - for non-elements (standalone text, PI, comments): a string comparison
     *
     * @param xmlOne the first XML value
     * @param xmlTwo the second XML value
     * @return true if the two are equal, false if not equal or an exception is thrown while checking equality
     */
    public static boolean isEqual(XMLValue xmlOne, XMLValue xmlTwo) {
        return xmlOne.equals(xmlTwo);
    }

    /**
     * Converts given xml object to the corresponding json.
     *
     * @param xmlItem            XML element to traverse
     * @param attributePrefix    Prefix to use in attributes
     * @param preserveNamespaces preserve the namespaces when converting
     * @return ObjectNode Json object node corresponding to the given xml element
     */
    private static MapValueImpl<BString, Object> traverseXMLElement(XMLItem xmlItem, String attributePrefix,
                                                                    boolean preserveNamespaces) {
        MapValueImpl<BString, Object> rootNode = new MapValueImpl<>(jsonMapType);
        LinkedHashMap<String, String> attributeMap = collectAttributesAndNamespaces(xmlItem, preserveNamespaces);
        Iterator<BXML> iterator = getChildrenIterator(xmlItem);
        BString keyValue = StringUtils.fromString(getElementKey(xmlItem, preserveNamespaces));
        if (iterator.hasNext()) {
            MapValueImpl<BString, Object> currentRoot = new MapValueImpl<>(jsonMapType);
            ArrayList<XMLItem> childArray = new ArrayList<>();
            LinkedHashMap<String, ArrayList<Object>> rootMap = new LinkedHashMap<>();
            while (iterator.hasNext()) {
                // Process all child elements, skip non element children.
                BXML child = iterator.next();
                if (child.getNodeType() != XMLNodeType.ELEMENT) {
                    continue;
                }

                XMLItem item = (XMLItem) child;
                LinkedHashMap<String, String> childAttributeMap =
                        collectAttributesAndNamespaces(item, preserveNamespaces);
                String childKeyValue = getElementKey(item, preserveNamespaces);
                Iterator<BXML> childrenIterator = getChildrenIterator(item);
                if (childrenIterator.hasNext()) {
                    // The child element itself has more child elements
                    MapValueImpl<BString, ?> nodeIntermediate =
                            traverseXMLElement(item, attributePrefix, preserveNamespaces);
                    addToRootMap(rootMap, childKeyValue, nodeIntermediate.get(StringUtils.fromString(childKeyValue)));
                } else {
                    // The child element is a single element with no child elements
                    if (childAttributeMap.size() > 0) {
                        Object attrObject = processAttributeAndNamespaces(null, childAttributeMap, attributePrefix,
                                xmlItem.getTextValue());
                        addToRootMap(rootMap, childKeyValue, attrObject);
                    } else {
                        childArray.add(item);
                    }
                }
            }
            // Add attributes and namespaces
            processAttributeAndNamespaces(currentRoot, attributeMap, attributePrefix, null);
            // Add child arrays to the current node
            processChildelements(currentRoot, childArray, attributePrefix, preserveNamespaces);
            // Add child objects to the current node
            processRootNodes(currentRoot, rootMap);
            // Create the outermost root node
            rootNode.put(keyValue, currentRoot);
        } else {
            // Process the single element
            if (attributeMap.size() > 0) {
                // Element has attributes or namespaces
                MapValueImpl<BString, Object> attrObject =
                        processAttributeAndNamespaces(null, attributeMap, attributePrefix, xmlItem.getTextValue());
                rootNode.put(keyValue, attrObject);
            } else {
                rootNode.put(keyValue, StringUtils.fromString(xmlItem.getTextValue()));
            }
        }
        return rootNode;
    }

    private static Iterator<BXML> getChildrenIterator(XMLItem xmlItem) {
        return xmlItem.getChildrenSeq().getChildrenList().iterator();
    }

    /**
     * Converts given xml sequence to the corresponding json.
     *
     * @param xmlSequence XML sequence to traverse
     * @param attributePrefix Prefix to use in attributes
     * @param preserveNamespaces preserve the namespaces when converting
     * @return JsonNode Json node corresponding to the given xml sequence
     */
    private static Object traverseXMLSequence(XMLSequence xmlSequence, String attributePrefix,
                                              boolean preserveNamespaces) {
        List<BXML> sequence = xmlSequence.getChildrenList();
        long count = sequence.size();
        ArrayList<XMLItem> childArray = new ArrayList<>();
        ArrayList<XMLText> textArray = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            BXML xmlVal = sequence.get(i);
            if (xmlVal.getNodeType() == XMLNodeType.ELEMENT) {
                childArray.add((XMLItem) xmlVal);
            } else if (xmlVal.getNodeType() == XMLNodeType.TEXT) {
                textArray.add((XMLText) xmlVal);
            }
        }

        ArrayValue textArrayNode = null;
        if (!textArray.isEmpty()) { // Text nodes are converted into json array
            textArrayNode = processTextArray(textArray);
        }

        MapValueImpl<BString, Object> jsonNode = new MapValueImpl<>(jsonMapType);
        if (!childArray.isEmpty()) {
            processChildelements(jsonNode, childArray, attributePrefix, preserveNamespaces);
            if (textArrayNode != null) {
                // When text nodes and elements are mixed, they will set into an array
                textArrayNode.append(jsonNode);
            }
        }

        if (textArrayNode != null) {
            return textArrayNode;
        }

        return jsonNode;
    }

    /**
     * Process XML child elements and create JSON node from them.
     *
     * @param root               JSON root object to which children are added
     * @param childArray         List of child XML elements
     * @param attributePrefix    Prefix to use in attributes
     * @param preserveNamespaces preserve the namespaces when converting
     */
    private static void processChildelements(MapValueImpl<BString, Object> root, ArrayList<XMLItem> childArray,
                                             String attributePrefix, boolean preserveNamespaces) {
        LinkedHashMap<String, ArrayList<XMLItem>> rootMap = new LinkedHashMap<>();
        // Check child elements and group them from the key. XML sequences contain multiple child elements with same key
        for (XMLItem element : childArray) {
            String key = element.getQName().toString();
            rootMap.putIfAbsent(key, new ArrayList<>());
            rootMap.get(key).add(element);
        }
        for (Map.Entry<String, ArrayList<XMLItem>> entry : rootMap.entrySet()) {
            ArrayList<XMLItem> elementList = entry.getValue();
            if (elementList.size() > 0) {
                BString nodeKey = StringUtils.fromString(getElementKey(elementList.get(0), preserveNamespaces));
                if (elementList.size() == 1) {
                    XMLItem element = elementList.get(0);
                    if (!element.children().elements().isEmpty()) {
                        // If the element it self has child elements traverse through them
                        MapValueImpl<BString, Object> node =
                                traverseXMLElement(element, attributePrefix, preserveNamespaces);
                        root.put(nodeKey, node.get(nodeKey));
                    } else {
                        root.put(nodeKey, StringUtils.fromString(elementList.get(0).getTextValue()));
                    }
                } else {
                    // Child elements with similar keys are put into an array
                    ArrayValue arrayNode = new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
                    for (XMLItem element : elementList) {
                        arrayNode.append(element.getTextValue());
                    }
                    root.put(nodeKey, arrayNode);
                }
            }
        }
    }

    /**
     * Add the child JSON nodes in the parent node.
     *
     * @param root    JSON root object to which child nodes are added
     * @param rootMap List of child JSON nodes
     */
    private static void processRootNodes(MapValueImpl<BString, Object> root,
                                         LinkedHashMap<String, ArrayList<Object>> rootMap) {
        for (Map.Entry<String, ArrayList<Object>> entry : rootMap.entrySet()) {
            BString key = StringUtils.fromString(entry.getKey());
            ArrayList<Object> elementList = entry.getValue();
            int elementCount = elementList.size();
            if (elementCount == 1) {
                root.put(key, elementList.get(0));
            } else {
                // When there are multiple nodes with the same key they are set into an array
                ArrayValue arrayNode = new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
                for (Object node : elementList) {
                    arrayNode.append(node);
                }
                root.put(key, arrayNode);
            }

        }
    }

    /**
     * Extract attributes and namespaces from the XML element.
     *
     * @param element XML element to extract attributes and namespaces
     * @param preserveNamespaces should namespace attribute be preserved
     */
    private static LinkedHashMap<String, String> collectAttributesAndNamespaces(XMLItem element,
                                                                                boolean preserveNamespaces) {
        int nsPrefixBeginIndex = XMLItem.XMLNS_URL_PREFIX.length() - 1;
        LinkedHashMap<String, String> attributeMap = new LinkedHashMap<>();
        Map<String, String> nsPrefixMap = new HashMap<>();
        for (Map.Entry<BString, BString> entry : element.getAttributesMap().entrySet()) {
            if (entry.getKey().getValue().startsWith(XMLItem.XMLNS_URL_PREFIX)) {
                String prefix = entry.getKey().getValue().substring(nsPrefixBeginIndex);
                String ns = entry.getValue().getValue();
                nsPrefixMap.put(ns, prefix);
                if (preserveNamespaces) {
                    attributeMap.put(XML_NAMESPACE_PREFIX + prefix, ns);
                }
            }
        }
        for (Map.Entry<BString, BString> entry : element.getAttributesMap().entrySet()) {
            String key = entry.getKey().getValue();
            if (preserveNamespaces && !key.startsWith(XMLItem.XMLNS_URL_PREFIX)) {
                int nsEndIndex = key.lastIndexOf('}');
                String ns = key.substring(1, nsEndIndex);
                String local = key.substring(nsEndIndex);
                String nsPrefix = nsPrefixMap.get(ns);
                if (nsPrefix != null) {
                    attributeMap.put(nsPrefix + ":" + local, entry.getValue().getValue());
                } else {
                    attributeMap.put(local, entry.getValue().getValue());
                }
            }
        }
        return attributeMap;
    }

    /**
     * Set attributes and namespaces as key value pairs of the immediate parent.
     *
     * @param rootNode           Parent node of the attributes and the namespaces
     * @param attributeMap       Key value pairs of attributes and namespaces
     * @param attributePrefix    Prefix used for attributes
     * @param singleElementValue Whether the given root is a single element
     * @return ObjectNode Json object node corresponding to the given attributes and namespaces
     */
    private static MapValueImpl<BString, Object>
    processAttributeAndNamespaces(MapValueImpl<BString, Object> rootNode,
                                  LinkedHashMap<String, String> attributeMap,
                                  String attributePrefix, String singleElementValue) {
        boolean singleElement = false;
        if (rootNode == null) {
            rootNode = new MapValueImpl<>(jsonMapType);
            singleElement = true;
        }
        // All the attributes and namesapces are set as key value pairs with given prefix
        for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
            String key = attributePrefix + entry.getKey();
            rootNode.put(StringUtils.fromString(key), StringUtils.fromString(entry.getValue()));
        }
        // If the single element has attributes or namespaces the text value is added with a dummy tag
        if (singleElement && !singleElementValue.isEmpty()) {
            rootNode.put(XML_VALUE_TAG, StringUtils.fromString(singleElementValue));
        }
        return rootNode;
    }

    /**
     * Convert a given list of XML text elements into a JSON array.
     *
     * @param childArray List of XML text elements
     * @return ArrayNode Json array node corresponding to the given text elements
     */
    private static ArrayValue processTextArray(ArrayList<XMLText> childArray) {
        // Create array based on xml text elements
        ArrayValue arrayNode = new ArrayValueImpl(new BArrayType(BTypes.typeJSON));
        for (XMLText element : childArray) {
            arrayNode.append(element.getTextValue());
        }
        return arrayNode;
    }

    /**
     * Extract the key from the element with namespace information.
     *
     * @param xmlItem XML element for which the key needs to be generated
     * @param preserveNamespaces Whether namespace info included in the key or not
     * @return String Element key with the namespace information
     */
    private static String getElementKey(XMLItem xmlItem, boolean preserveNamespaces) {
        // Construct the element key based on the namespaces
        StringBuilder elementKey = new StringBuilder();
        QName qName = xmlItem.getQName();
        if (preserveNamespaces) {
            String prefix = qName.getPrefix();
            if (prefix != null && !prefix.isEmpty()) {
                elementKey.append(prefix).append(':');
            }
        }
        elementKey.append(qName.getLocalPart());
        return elementKey.toString();
    }

    /**
     * Create a collection of JSON nodes which has similary key names.
     *
     * @param rootMap Map of key and node list pairs
     * @param key Key of the JSON nodes
     * @param node JSON node to be added
     */
    private static void addToRootMap(LinkedHashMap<String, ArrayList<Object>> rootMap, String key, Object node) {
        rootMap.putIfAbsent(key, new ArrayList<>());
        rootMap.get(key).add(node);
    }

    private static QName getQName(String localName, String namespaceUri, String prefix) {
        QName qname;
        if (prefix != null) {
            qname = new QName(namespaceUri, localName, prefix);
        } else {
            qname = new QName(namespaceUri, localName);
        }
        return qname;
    }

    /**
     * Create an OMElement from an XML fragment given as a string.
     * Generously borrowed from Apache Axiom (org.apache.axiom.om.util.AXIOMUtil).
     *
     * @param xmlFragment the well-formed XML fragment
     * @return The OMElement created out of the string XML fragment.
     * @throws XMLStreamException when unexpected processing error occur while parsing.
     */
    public static OMElement stringToOM(String xmlFragment) throws XMLStreamException {
        return stringToOM(OMAbstractFactory.getOMFactory(), xmlFragment);
    }

    /**
     * Create an OMElement from an XML fragment given as a string.
     * Generously borrowed and improved from Apache Axiom (org.apache.axiom.om.util.AXIOMUtil).
     *
     * @param omFactory the factory used to build the object model
     * @param xmlFragment the well-formed XML fragment
     * @return The OMElement created out of the string XML fragment.
     * @throws XMLStreamException when unexpected processing error occur while parsing.
     */
    private static OMElement stringToOM(OMFactory omFactory, String xmlFragment) throws XMLStreamException {
        return xmlFragment != null
                ? OMXMLBuilderFactory
                        .createOMBuilder(omFactory, STAX_PARSER_CONFIGURATION, new StringReader(xmlFragment))
                        .getDocumentElement()
                : null;
    }


    /**
     * Replace xml text escape sequences with appropriate character.
     *
     * @since 1.2
     */
    public static class XMLTextUnescape {
        public static String unescape(String str) {
            return unescape(str.getBytes(StandardCharsets.UTF_8));
        }

        private static String unescape(byte[] bytes) {
            byte[] target = new byte[bytes.length];
            int size = bytes.length;
            int len = 0;

            for (int i = 0; i < size; i++, len++) {
                byte b = bytes[i];
                int i1 = i + 1; // index next to current index

                // Remove carriage return on windows environments to eliminate additional &#xd; being added
                if (b == '\r' && i1 < size && bytes[i1] == '\n') {
                    target[len] = '\n';
                    i += 1;
                    continue;
                }

                // &gt; &lt; and &amp; in XML literal in Ballerina lang maps to >, <, and & in XML infoset.
                if (b == '&') {
                    int i2 = i + 2; // index next next to current index
                    int i3 = i + 3; // index next next next current index
                    if (i3 < size && bytes[i1] == 'g' && bytes[i2] == 't' && bytes[i3] == ';') {
                        target[len] = '>';
                        i += 3;
                        continue;
                    }

                    if (i3 < size && bytes[i1] == 'l' && bytes[i2] == 't' && bytes[i3] ==  ';') {
                        target[len] = '<';
                        i += 3;
                        continue;
                    }

                    if (i3 + 1 < size && bytes[i1] == 'a' && bytes[i2] == 'm' && bytes[i3] == 'p'
                            && bytes[i3 + 1] == ';') {
                        target[len] = '&';
                        i += 4;
                        continue;
                    }
                }

                target[len] = b;
            }

            return new String(target, 0, len);
        }
    }
}
