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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.xml.util.PrettyXMLEventWriter;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMProcessingInstruction;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.dom.TextImpl;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axiom.om.util.AXIOMUtil;
import org.ballerinalang.model.DataTableOMDataSource;
import org.ballerinalang.model.values.BDataTable;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLQName;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;

/**
 * Common utility methods used for XML manipulation.
 * 
 * @since 0.88
 */
public class XMLUtils {
    
    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();
    private static final String XML_ROOT = "root";
    private static final String XML_NAMESPACE_PREFIX = "xmlns:";
    private static final String XML_VALUE_TAG = "#text";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /**
     * Create a XML item from string literal.
     *
     * @param xmlStr String representation of the XML
     * @return XML sequence
     */
    public static BXML<?> parse(String xmlStr) {
        try {

            if (xmlStr.isEmpty()) {
                return new BXMLItem(new TextImpl());
            }

            // Here we add a dummy enclosing tag, and send to AXIOM to parse the XML.
            // This is to overcome the issue of axiom not allowing to parse xml-comments,
            // xml-text nodes, and pi nodes, without having a xml-element node.
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
        } catch (Throwable e) {
            throw new BallerinaException("failed to parse xml: " + e.getMessage());
        }
    }

    /**
     * Create a XML sequence from string inputstream.
     *
     * @param xmlStream XML imput stream
     * @return  XML Sequence
     */
    public static BXML<?> parse(InputStream xmlStream) {
        BRefValueArray elementsSeq = new BRefValueArray();
        OMDocument doc;
        try {
            doc = new StAXOMBuilder(xmlStream).getDocument();
            Iterator<OMNode> docChildItr = doc.getChildren();
            int i = 0;
            while (docChildItr.hasNext()) {
                elementsSeq.add(i++, new BXMLItem(docChildItr.next()));
            }
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
     * Converts a {@link BXML} to {@link BJSON}.
     * 
     * @param xml {@link BXML} to convert
     * @return converted {@link BJSON} 
     * @throws BallerinaException for conversion errors
     */
    public static BJSON toJSON(BXML<?> xml) throws BallerinaException {
        InputStream input = new ByteArrayInputStream(xml.stringValue().getBytes(StandardCharsets.UTF_8));
        InputStream results = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BJSON json = null;

        JsonXMLConfig config = new JsonXMLConfigBuilder().autoArray(true).autoPrimitive(true).prettyPrint(true).build();
        try {
            //Create source (XML).
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(input);
            Source source = new StAXSource(reader);

            //Create result (JSON).
            XMLStreamWriter writer = new JsonXMLOutputFactory(config).createXMLStreamWriter(output);
            Result result = new StAXResult(writer);

            //Copy source to result via "identity transform".
            TransformerFactory.newInstance().newTransformer().transform(source, result);

            byte[] outputByteArray = output.toByteArray();
            results = new ByteArrayInputStream(outputByteArray);

        } catch (TransformerConfigurationException e) {
            throw new BallerinaException("error in parsing the JSON Stream. transformer configuration issue", e);
        } catch (TransformerException e) {
            throw new BallerinaException("error in parsing the JSON Stream", e);
        } catch (XMLStreamException e) {
            throw new BallerinaException("error in parsing the XML Stream", e);
        } finally {
            //As per StAX specification, XMLStreamReader/Writer.close() doesn't close the underlying stream.
            try {
                output.close();
                input.close();
            } catch (IOException ignore) {

            }
        }
        json = new BJSON(results);
        return json;
    }

    /**
     * Converts a {@link BJSON} to {@link BXML}.
     * 
     * @param msg {@link BJSON} to convert
     * @return converted {@link BXML}
     */
    public static BXML<?> jsonToXML(BJSON msg) {
        InputStream input = new ByteArrayInputStream(msg.stringValue().getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).virtualRoot(XML_ROOT).build();
        BXML<?> result = null;

        XMLEventReader reader = null;
        XMLEventWriter writer = null;
        try {
            reader = new JsonXMLInputFactory(config).createXMLEventReader(input);
            writer = XMLOutputFactory.newInstance().createXMLEventWriter(output);
            writer = new PrettyXMLEventWriter(writer);
            writer.add(reader);
        } catch (XMLStreamException e) {
            throw new BallerinaException("Error in parsing the XML Stream", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }

                if (writer != null) {
                    writer.close();
                }
            } catch (XMLStreamException ignore) {
            }
            try {
                output.close();
                input.close();
            } catch (IOException ignore) {
            }
        }

        byte[] xml = output.toByteArray();
        result = new BXMLItem(new ByteArrayInputStream(xml));
        return result;
    }

    /**
     * Get the singleton value of the xml, for xpath operations.
     *
     * @param xml source xml
     * @return singleton value
     */
    public static BXML getSingletonValue(BXML xml) {
        if (xml instanceof BXMLItem) {
            return xml;
        }

        if (!xml.isSingleton().value()) {
            throw new BallerinaException("cannot execute xpath on a xml sequence");
        }

        return (BXML) ((BXMLSequence) xml).value().get(0);
    }

    /**
     * Converts a {@link BDataTable} to {@link BXML}.
     *
     * @param dataTable {@link BDataTable} to convert
     * @param isInTransaction   Within a transaction or not
     * @return converted {@link BXML}
     */
    public static BXML datatableToXML(BDataTable dataTable, boolean isInTransaction) {
        OMSourcedElementImpl omSourcedElement = new OMSourcedElementImpl();
        omSourcedElement.init(new DataTableOMDataSource(dataTable, null, null, isInTransaction));
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
        if (!startTagName.getLocalName().equals(endTagName.getLocalName())
                || !startTagName.getUri().equals(endTagName.getUri())
                || !startTagName.getPrefix().equals(endTagName.getPrefix())) {
            throw new BallerinaException(
                    "start and end tag names mismatch: '" + startTagName + "' and '" + endTagName + "'");
        }

        String nsUri = startTagName.getUri();
        OMElement omElement;
        if (nsUri.isEmpty()) {
            if (defaultNsUri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
                defaultNsUri = XMLConstants.NULL_NS_URI;
            }
            omElement = OM_FACTORY.createOMElement(startTagName.getLocalName(), defaultNsUri, startTagName.getPrefix());
        } else {
            QName qname = new QName(nsUri, startTagName.getLocalName(), startTagName.getPrefix());
            omElement = OM_FACTORY.createOMElement(qname);
            if (!defaultNsUri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
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
    public static BJSON convertToJSON(BXML xml, String attributePrefix, boolean preserveNamespaces) {
        JsonNode jsonNode = null;
        if (xml instanceof BXMLItem) {
            //Process xml item
            BXMLItem xmlItem = (BXMLItem) xml;
            OMNode omNode = xmlItem.value();
            if (OMNode.ELEMENT_NODE == omNode.getType()) {
                jsonNode = traverseXMLElement((OMElement) omNode, attributePrefix, preserveNamespaces);
            } else if (OMNode.TEXT_NODE == omNode.getType()) {
                try {
                    jsonNode = OBJECT_MAPPER.readTree("\"" + ((OMText) omNode).getText() + "\"");
                } catch (IOException e) {
                    throw new BallerinaException("error in converting string node to json");
                }
            } else {
                jsonNode = OBJECT_MAPPER.createObjectNode();
            }
        } else {
            //Process xml sequence
            BXMLSequence xmlSequence = (BXMLSequence) xml;
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
    private static ObjectNode traverseXMLElement(OMElement omElement, String attributePrefix,
            boolean preserveNamespaces) {
        ObjectNode rootNode = OBJECT_MAPPER.createObjectNode();
        LinkedHashMap<String, String> attributeMap = collectAttributesAndNamespaces(omElement, preserveNamespaces);
        Iterator iterator = omElement.getChildElements();
        String keyValue = getElementKey(omElement, preserveNamespaces);
        if (iterator.hasNext()) {
            ObjectNode currentRoot = OBJECT_MAPPER.createObjectNode();
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
                        ObjectNode nodeIntermediate = traverseXMLElement(omChildElement, attributePrefix,
                                preserveNamespaces);
                        addToRootMap(rootMap, childKeyValue, nodeIntermediate.get(childKeyValue));
                    } else {
                        //The child element is a single element with no child elements
                        if (childAttributeMap.size() > 0) {
                            ObjectNode attrObject = processAttributeAndNamespaces(null, childAttributeMap,
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
                ObjectNode attrObject = processAttributeAndNamespaces(null, attributeMap, attributePrefix,
                        omElement.getText());
                rootNode.set(keyValue, attrObject);
            } else {
                rootNode.put(keyValue, omElement.getText());
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
            jsonNode = OBJECT_MAPPER.createObjectNode();
            processChildelements((ObjectNode) jsonNode, childArray, attributePrefix, preserveNamespaces);
            if (textArrayNode != null) {
                //When text nodes and elements are mixed, they will set into an array
                ((ArrayNode) textArrayNode).add(jsonNode);
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
    private static void processChildelements(ObjectNode root, ArrayList<OMElement> childArray, String attributePrefix,
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
                        root.put(nodeKey, elementList.get(0).getText());
                    }
                } else {
                    //Child elements with similar keys are put into an array
                    ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
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
    private static void processRootNodes(ObjectNode root, LinkedHashMap<String, ArrayList<JsonNode>> rootMap) {
        for (Map.Entry<String, ArrayList<JsonNode>> entry : rootMap.entrySet()) {
            String key = entry.getKey();
            ArrayList<JsonNode> elementList = entry.getValue();
            int elementCount = elementList.size();
            if (elementCount == 1) {
                root.set(key, elementList.get(0));
            } else {
                //When there are multiple nodes with the same key they are set into an array
                ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
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
    private static ObjectNode processAttributeAndNamespaces(ObjectNode rootNode,
            LinkedHashMap<String, String> attributeMap, String attributePrefix, String singleElementValue) {
        boolean singleElement = false;
        if (rootNode == null) {
            rootNode = OBJECT_MAPPER.createObjectNode();
            singleElement = true;
        }
        //All the attributes and namesapces are set as key value pairs with given prefix
        for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
            String key = attributePrefix + entry.getKey();
            String value = entry.getValue();
            rootNode.put(key, value);
        }
        //If the single element has attributes or namespaces the text value is added with a dummy tag
        if (singleElement && !singleElementValue.isEmpty()) {
            rootNode.put(XML_VALUE_TAG, singleElementValue);
        }
        return rootNode;
    }

    /**
     * Convert a given list of XML text elements into a JSON array.
     *
     * @param childArray List of XML text elements
     * @return ArrayNode Json array node corresponding to the given text elements
     */
    private static ArrayNode processTextArray(ArrayList<OMText> childArray) {
        //Create array based on xml text elements
        ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
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
}
