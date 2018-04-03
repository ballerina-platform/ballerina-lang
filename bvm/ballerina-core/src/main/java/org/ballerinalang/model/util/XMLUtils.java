/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.util;

import org.apache.axiom.om.DeferredParsingException;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMProcessingInstruction;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.impl.dom.TextImpl;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axiom.om.util.AXIOMUtil;
import org.ballerinalang.model.TableOMDataSource;
import org.ballerinalang.model.util.JsonNode.Type;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLQName;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Common utility methods used for XML manipulation.
 * 
 * @since 0.88
 */
public class XMLUtils {
    
    private static final String XML_NAMESPACE_PREFIX = "xmlns:";
    private static final String XML_VALUE_TAG = "#text";
    private static final String XML_DCLR_START = "<?xml";

    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();

    /**
     * Create a XML item from string literal.
     *
     * @param xmlStr String representation of the XML
     * @return XML sequence
     */
    @SuppressWarnings("unchecked")
    public static BXML<?> parse(String xmlStr) {
        try {

            if (xmlStr.isEmpty()) {
                return new BXMLItem(new TextImpl());
            }

            // If this is an XML document, parse it and return an element type XML.
            if (xmlStr.trim().startsWith(XML_DCLR_START)) {
                return new BXMLItem(xmlStr);
            }

            // Here we add a dummy enclosing tag, and send to AXIOM to parse the XML.
            // This is to overcome the issue of axiom not allowing to parse xml-comments,
            // xml-text nodes, and pi nodes, without having an enclosing xml-element node.
            OMElement omElement = AXIOMUtil.stringToOM("<root>" + xmlStr + "</root>");
            Iterator<OMNode> children = omElement.getChildren();
            OMNode omNode = null;
            if (children.hasNext()) {
                omNode = children.next();
            }

            if (children.hasNext()) {
                throw new BallerinaException("xml item must be one of the types: 'element', 'comment', 'text', 'pi'");
            }

            // Here the node is detached from the dummy root, and added to a
            // document element. This is to get the xpath working correctly
            omNode = omNode.detach();
            OMDocument doc = OM_FACTORY.createOMDocument();
            doc.addChild(omNode);
            return new BXMLItem(omNode);
        } catch (BallerinaException e) {
            throw e;
        } catch (OMException | XMLStreamException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new BallerinaException(cause.getMessage());
        } catch (Throwable e) {
            throw new BallerinaException("failed to parse xml: " + e.getMessage());
        }
    }

    /**
     * Create a XML sequence from string inputstream.
     *
     * @param xmlStream XML input stream
     * @return  XML Sequence
     */
    @SuppressWarnings("unchecked")
    public static BXML<?> parse(InputStream xmlStream) {
        BRefValueArray elementsSeq = new BRefValueArray();
        OMDocument doc;
        try {
            doc = OMXMLBuilderFactory.createOMBuilder(xmlStream).getDocument();
            Iterator<OMNode> docChildItr = doc.getChildren();
            int i = 0;
            while (docChildItr.hasNext()) {
                elementsSeq.add(i++, new BXMLItem(docChildItr.next()));
            }
        } catch (DeferredParsingException e) {
            throw new BallerinaException(e.getCause().getMessage());
        } catch (Throwable e) {
            throw new BallerinaException("failed to create xml: " + e.getMessage());
        }
        return new BXMLSequence(elementsSeq);
    }

    /**
     * Create a XML sequence from string reader.
     *
     * @param reader XML reader
     * @return XML Sequence
     */
    @SuppressWarnings("unchecked")
    public static BXML<?> parse(Reader reader) {
        BRefValueArray elementsSeq = new BRefValueArray();
        OMDocument doc;
        try {
            doc = OMXMLBuilderFactory.createOMBuilder(reader).getDocument();
            Iterator<OMNode> docChildItr = doc.getChildren();
            int i = 0;
            while (docChildItr.hasNext()) {
                elementsSeq.add(i++, new BXMLItem(docChildItr.next()));
            }
        } catch (DeferredParsingException e) {
            throw new BallerinaException(e.getCause().getMessage());
        } catch (Throwable e) {
            throw new BallerinaException("failed to create xml: " + e.getMessage());
        }
        return new BXMLSequence(elementsSeq);
    }

    /**
     * Concatenate two XML sequences and produce a single sequence.
     *
     * @param firstSeq First XML sequence
     * @param secondSeq Second XML sequence
     * @return Concatenated XML sequence
     */
    public static BXML<?> concatenate(BXML<?> firstSeq, BXML<?> secondSeq) {
        BRefValueArray concatSeq = new BRefValueArray();
        int j = 0;

        //Load the content fully before concat the two
        firstSeq.build();
        secondSeq.build();

        // Add all the items in the first sequence
        if (firstSeq.getNodeType() == XMLNodeType.SEQUENCE) {
            BRefValueArray seq = ((BXMLSequence) firstSeq).value();
            for (int i = 0; i < seq.size(); i++) {
                concatSeq.add(j++, seq.get(i));
            }
        } else {
            concatSeq.add(j++, firstSeq);
        }

        // Add all the items in the second sequence
        if (secondSeq.getNodeType() == XMLNodeType.SEQUENCE) {
            BRefValueArray seq = ((BXMLSequence) secondSeq).value();
            for (int i = 0; i < seq.size(); i++) {
                concatSeq.add(j++, seq.get(i));
            }
        } else {
            concatSeq.add(j++, secondSeq);
        }

        return new BXMLSequence(concatSeq);
    }

    /**
     * Converts a {@link BTable} to {@link BXML}.
     *
     * @param table {@link BTable} to convert
     * @param isInTransaction   Within a transaction or not
     * @return converted {@link BXML}
     */
    @SuppressWarnings("rawtypes")
    public static BXML tableToXML(BTable table, boolean isInTransaction) {
        OMSourcedElementImpl omSourcedElement = new OMSourcedElementImpl();
        omSourcedElement.init(new TableOMDataSource(table, null, null, isInTransaction));
        return new BXMLItem(omSourcedElement);
    }

    /**
     * Create an element type BXML.
     *
     * @param startTagName Name of the start tag of the element
     * @param endTagName Name of the end tag of element
     * @param defaultNsUri Default namespace URI
     * @return BXML Element type BXML
     */
    public static BXML<?> createXMLElement(BXMLQName startTagName, BXMLQName endTagName, String defaultNsUri) {
        if (!StringUtils.isEqual(startTagName.getLocalName(), endTagName.getLocalName()) ||
                !StringUtils.isEqual(startTagName.getUri(), endTagName.getUri()) ||
                !StringUtils.isEqual(startTagName.getPrefix(), endTagName.getPrefix())) {
            throw new BallerinaException(
                    "start and end tag names mismatch: '" + startTagName + "' and '" + endTagName + "'");
        }

        // Validate whether the tag names are XML supported qualified names, according to the XML recommendation.
        XMLValidationUtils.validateXMLQName(startTagName);

        String nsUri = startTagName.getUri();
        OMElement omElement;
        if (defaultNsUri == null) {
            defaultNsUri = XMLConstants.NULL_NS_URI;
        }

        String prefix = startTagName.getPrefix() == null ? XMLConstants.DEFAULT_NS_PREFIX : startTagName.getPrefix();

        if (nsUri == null) {
            omElement = OM_FACTORY.createOMElement(startTagName.getLocalName(), defaultNsUri, prefix);
        } else if (nsUri.isEmpty()) {
            omElement = OM_FACTORY.createOMElement(startTagName.getLocalName(), nsUri, prefix);
        } else if (nsUri.equals(defaultNsUri)) {
            omElement = OM_FACTORY.createOMElement(startTagName.getLocalName(), defaultNsUri, prefix);
        } else {
            QName qname = getQName(startTagName.getLocalName(), nsUri, prefix);
            omElement = OM_FACTORY.createOMElement(qname);
            if (!defaultNsUri.isEmpty()) {
                omElement.declareDefaultNamespace(defaultNsUri);
            }
        }

        return new BXMLItem(omElement);
    }

    /**
     * Create a comment type BXML.
     *
     * @param content Comment content
     * @return BXML Comment type BXML
     */
    public static BXML<?> createXMLComment(String content) {
        OMComment omComment = OM_FACTORY.createOMComment(OM_FACTORY.createOMDocument(), content);
        return new BXMLItem(omComment);
    }

    /**
     * Create a comment type BXML.
     *
     * @param content Text content
     * @return BXML Text type BXML
     */
    public static BXML<?> createXMLText(String content) {
        // Remove carriage return on windows environments to eliminate additional &#xd; being added
        content = content.replace("\r\n", "\n");

        OMText omText = OM_FACTORY.createOMText(content);
        return new BXMLItem(omText);
    }

    /**
     * Create a processing instruction type BXML.
     *
     * @param tartget PI target
     * @param data PI data
     * @return BXML Processing instruction type BXML
     */
    public static BXML<?> createXMLProcessingInstruction(String tartget, String data) {
        OMProcessingInstruction omText = OM_FACTORY.createOMProcessingInstruction(OM_FACTORY.createOMDocument(),
                tartget, data);
        return new BXMLItem(omText);
    }

    /**
     * Converts given xml object to the corresponding json.
     *
     * @param xml XML object to get the corresponding json
     * @param attributePrefix Prefix to use in attributes
     * @param preserveNamespaces preserve the namespaces when converting
     * @return BJSON JSON representation of the given xml object
     */
    @SuppressWarnings("rawtypes")
    public static BJSON convertToJSON(BXML xml, String attributePrefix, boolean preserveNamespaces) {
        JsonNode jsonNode = null;
        if (xml instanceof BXMLItem) {
            //Process xml item
            BXMLItem xmlItem = (BXMLItem) xml;
            OMNode omNode = xmlItem.value();
            if (OMNode.ELEMENT_NODE == omNode.getType()) {
                jsonNode = traverseXMLElement((OMElement) omNode, attributePrefix, preserveNamespaces);
            } else if (OMNode.TEXT_NODE == omNode.getType()) {
                jsonNode = JsonParser.parse("\"" + ((OMText) omNode).getText() + "\"");
            } else {
                jsonNode = new JsonNode(Type.OBJECT);
            }
        } else {
            //Process xml sequence
            BXMLSequence xmlSequence = (BXMLSequence) xml;
            if (xmlSequence.isEmpty().booleanValue()) {
                return new BJSON("[]");
            }
            jsonNode = traverseXMLSequence(xmlSequence, attributePrefix, preserveNamespaces);

        }
        return new BJSON(jsonNode);
    }

    /**
     * Converts given xml object to the corresponding json.
     *
     * @param omElement XML element to traverse
     * @param attributePrefix Prefix to use in attributes
     * @param preserveNamespaces preserve the namespaces when converting
     * @return ObjectNode Json object node corresponding to the given xml element
     */
    @SuppressWarnings("rawtypes")
    private static JsonNode traverseXMLElement(OMElement omElement, String attributePrefix,
            boolean preserveNamespaces) {
        JsonNode rootNode = new JsonNode(Type.OBJECT);
        LinkedHashMap<String, String> attributeMap = collectAttributesAndNamespaces(omElement, preserveNamespaces);
        Iterator iterator = omElement.getChildElements();
        String keyValue = getElementKey(omElement, preserveNamespaces);
        if (iterator.hasNext()) {
            JsonNode currentRoot = new JsonNode(Type.OBJECT);
            ArrayList<OMElement> childArray = new ArrayList<>();
            LinkedHashMap<String, ArrayList<JsonNode>> rootMap = new LinkedHashMap<>();
            while (iterator.hasNext()) {
                //Process all child elements
                OMNode node = (OMNode) iterator.next();
                if (OMNode.ELEMENT_NODE == node.getType()) {
                    OMElement omChildElement = (OMElement) node;
                    LinkedHashMap<String, String> childAttributeMap = collectAttributesAndNamespaces(omChildElement,
                            preserveNamespaces);
                    Iterator iteratorChild = omChildElement.getChildElements();
                    String childKeyValue = getElementKey(omChildElement, preserveNamespaces);
                    if (iteratorChild.hasNext()) {
                        //The child element itself has more child elements
                        JsonNode nodeIntermediate = traverseXMLElement(omChildElement, attributePrefix,
                                preserveNamespaces);
                        addToRootMap(rootMap, childKeyValue, nodeIntermediate.get(childKeyValue));
                    } else {
                        //The child element is a single element with no child elements
                        if (childAttributeMap.size() > 0) {
                            JsonNode attrObject = processAttributeAndNamespaces(null, childAttributeMap,
                                    attributePrefix, omChildElement.getText());
                            addToRootMap(rootMap, childKeyValue, attrObject);
                        } else {
                            childArray.add(omChildElement);
                        }
                    }
                }
            }
            //Add attributes and namespaces
            processAttributeAndNamespaces(currentRoot, attributeMap, attributePrefix, null);
            //Add child arrays to the current node
            processChildelements(currentRoot, childArray, attributePrefix, preserveNamespaces);
            //Add child objects to the current node
            processRootNodes(currentRoot, rootMap);
            //Create the outermost root node
            rootNode.set(keyValue, currentRoot);
        } else {
            //Process the single element
            if (attributeMap.size() > 0) {
                //Element has attributes or namespaces
                JsonNode attrObject = processAttributeAndNamespaces(null, attributeMap, attributePrefix,
                        omElement.getText());
                rootNode.set(keyValue, attrObject);
            } else {
                rootNode.set(keyValue, omElement.getText());
            }
        }
        return rootNode;
    }

    /**
     * Converts given xml sequence to the corresponding json.
     *
     * @param xmlSequence XML sequence to traverse
     * @param attributePrefix Prefix to use in attributes
     * @param preserveNamespaces preserve the namespaces when converting
     * @return JsonNode Json node corresponding to the given xml sequence
     */
    private static JsonNode traverseXMLSequence(BXMLSequence xmlSequence, String attributePrefix,
            boolean preserveNamespaces) {
        JsonNode jsonNode = null;
        BRefValueArray sequence = xmlSequence.value();
        long count = sequence.size();
        ArrayList<OMElement> childArray = new ArrayList<>();
        ArrayList<OMText> textArray = new ArrayList<>();
        for (long i = 0; i < count; ++i) {
            BXMLItem xmlItem = (BXMLItem) sequence.get(i);
            OMNode omNode = xmlItem.value();
            if (OMNode.ELEMENT_NODE ==  omNode.getType()) {
                childArray.add((OMElement) omNode);
            } else if (OMNode.TEXT_NODE ==  omNode.getType()) {
                textArray.add((OMText) omNode);
            }
        }
        JsonNode textArrayNode = null;
        if (textArray.size() > 0) { //Text nodes are converted into json array
            textArrayNode = processTextArray(textArray);
        }
        if (childArray.size() > 0) {
            jsonNode = new JsonNode(Type.OBJECT);
            processChildelements(jsonNode, childArray, attributePrefix, preserveNamespaces);
            if (textArrayNode != null) {
                //When text nodes and elements are mixed, they will set into an array
                textArrayNode.add(jsonNode);
            }
        }
        if (textArrayNode != null) {
            jsonNode = textArrayNode;
        }
        return jsonNode;
    }

    /**
     * Process xml child elements and create JSON node from them.
     *
     * @param root JSON root object to which children are added
     * @param childArray List of child xml elements
     * @param attributePrefix Prefix to use in attributes
     * @param preserveNamespaces preserve the namespaces when converting
     */
    private static void processChildelements(JsonNode root, ArrayList<OMElement> childArray, String attributePrefix,
            boolean preserveNamespaces) {
        LinkedHashMap<String, ArrayList<OMElement>> rootMap = new LinkedHashMap<>();
        //Check child elements and group them from the key. XML sequences contain multiple child elements with same key
        for (OMElement element : childArray) {
            String key = element.getLocalName();
            rootMap.putIfAbsent(key, new ArrayList<>());
            rootMap.get(key).add(element);
        }
        for (Map.Entry<String, ArrayList<OMElement>> entry : rootMap.entrySet()) {
            ArrayList<OMElement> elementList = entry.getValue();
            if (elementList.size() > 0) {
                String nodeKey = getElementKey(elementList.get(0), preserveNamespaces);
                if (elementList.size() == 1) {
                    OMElement element = elementList.get(0);
                    if (element.getChildElements().hasNext()) {
                        //If the element it self has child elements travers through them
                        JsonNode node = traverseXMLElement(element, attributePrefix, preserveNamespaces);
                        root.set(nodeKey, node.get(nodeKey));
                    } else {
                        root.set(nodeKey, elementList.get(0).getText());
                    }
                } else {
                    //Child elements with similar keys are put into an array
                    JsonNode arrayNode = new JsonNode(Type.ARRAY);
                    for (OMElement element : elementList) {
                        arrayNode.add(element.getText());
                    }
                    root.set(nodeKey, arrayNode);
                }
            }
        }
    }

    /**
     * Add the child json nodes in the parent node.
     *
     * @param root JSON root object to which child nodes are added
     * @param rootMap List of child JSON nodes
     */
    private static void processRootNodes(JsonNode root, LinkedHashMap<String, ArrayList<JsonNode>> rootMap) {
        for (Map.Entry<String, ArrayList<JsonNode>> entry : rootMap.entrySet()) {
            String key = entry.getKey();
            ArrayList<JsonNode> elementList = entry.getValue();
            int elementCount = elementList.size();
            if (elementCount == 1) {
                root.set(key, elementList.get(0));
            } else {
                //When there are multiple nodes with the same key they are set into an array
                JsonNode arrayNode = new JsonNode(Type.ARRAY);
                for (JsonNode node : elementList) {
                    arrayNode.add(node);
                }
                root.set(key, arrayNode);
            }

        }
    }

    /**
     * Extract attributes and namespaces from the XML element.
     *
     * @param element XML element to extract attributes and namespaces
     */
    @SuppressWarnings("rawtypes")
    private static LinkedHashMap<String, String> collectAttributesAndNamespaces(OMElement element,
            boolean preserveNamespaces) {
        //Extract namespaces from the element
        LinkedHashMap<String, String> attributeMap = new LinkedHashMap<>();
        if (preserveNamespaces) {
            Iterator namespaceIterator = element.getAllDeclaredNamespaces();
            while (namespaceIterator.hasNext()) {
                OMNamespace namespace = (OMNamespace) namespaceIterator.next();
                attributeMap.put(XML_NAMESPACE_PREFIX + namespace.getPrefix(), namespace.getNamespaceURI());
            }
        }
        //Extract attributes from the element
        Iterator attributeIterator = element.getAllAttributes();
        while (attributeIterator.hasNext()) {
            OMAttribute attribute = (OMAttribute) attributeIterator.next();
            StringBuffer key = new StringBuffer();
            if (preserveNamespaces) {
                String prefix = attribute.getPrefix();
                if (prefix != null) {
                    key.append(prefix).append(":");
                }
            }
            key.append(attribute.getLocalName());
            attributeMap.put(key.toString(), attribute.getAttributeValue());
        }
        return attributeMap;
    }

    /**
     * Set attributes and namespaces as key value pairs of the immediate parent.
     *
     * @param rootNode Parent node of the attributes and the namespaces
     * @param attributeMap Key value pairs of attributes and namespaces
     * @param attributePrefix Prefix used for attributes
     * @param singleElementValue Whether the given root is a single element
     * @return ObjectNode Json object node corresponding to the given attributes and namespaces
     */
    private static JsonNode processAttributeAndNamespaces(JsonNode rootNode,
            LinkedHashMap<String, String> attributeMap, String attributePrefix, String singleElementValue) {
        boolean singleElement = false;
        if (rootNode == null) {
            rootNode = new JsonNode(Type.OBJECT);
            singleElement = true;
        }
        //All the attributes and namesapces are set as key value pairs with given prefix
        for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
            String key = attributePrefix + entry.getKey();
            String value = entry.getValue();
            rootNode.set(key, value);
        }
        //If the single element has attributes or namespaces the text value is added with a dummy tag
        if (singleElement && !singleElementValue.isEmpty()) {
            rootNode.set(XML_VALUE_TAG, singleElementValue);
        }
        return rootNode;
    }

    /**
     * Convert a given list of XML text elements into a JSON array.
     *
     * @param childArray List of XML text elements
     * @return ArrayNode Json array node corresponding to the given text elements
     */
    private static JsonNode processTextArray(ArrayList<OMText> childArray) {
        //Create array based on xml text elements
        JsonNode arrayNode = new JsonNode(Type.ARRAY);
        for (OMText element : childArray) {
            arrayNode.add(element.getText());
        }
        return arrayNode;
    }

    /**
     * Extract the key from the element with namespace information.
     *
     * @param omElement XML element for which the key needs to be generated
     * @param preserveNamespaces Whether namespace info included in the key or not
     * @return String Element key with the namespace information
     */
    private static String getElementKey(OMElement omElement, boolean preserveNamespaces) {
        //Construct the element key based on the namespaces
        StringBuffer stringBuffer = new StringBuffer();
        if (preserveNamespaces) {
            String prefix = omElement.getPrefix();
            if (prefix != null) {
                stringBuffer.append(prefix).append(":");
            }
        }
        stringBuffer.append(omElement.getLocalName());
        return stringBuffer.toString();
    }

    /**
     * Create a collection of JSON nodes which has similary key names.
     *
     * @param rootMap Map of key and node list pairs
     * @param key Key of the JSON nodes
     * @param node JSON node to be added
     */
    private static void addToRootMap(LinkedHashMap<String, ArrayList<JsonNode>> rootMap, String key, JsonNode node) {
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
}
