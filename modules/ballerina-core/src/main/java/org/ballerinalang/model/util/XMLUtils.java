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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.dom.TextImpl;
import org.apache.axiom.om.impl.llom.OMDocumentImpl;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axiom.om.util.AXIOMUtil;
import org.ballerinalang.model.DataTableOMDataSource;
import org.ballerinalang.model.values.BDataTable;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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
    
    private static final String XML_ROOT = "root";

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
            // TODO: improve this logic once the xml parsing is supported from the grammar.
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
            OMDocument doc = new OMDocumentImpl();
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
     * Converts given xml object to the corresponding json.
     *
     * @param xml XML object to get the corresponding json
     * @return BJSON JSON representation of the given xml object
     */
    public static BJSON convertToJSON(BXML xml, String attributePrefix, boolean preserveNamespaces) {
        JsonNode jsonNode = null;
        if (xml instanceof BXMLItem) {
            BXMLItem xmlItem = (BXMLItem) xml;
            OMElement omElement = (OMElement) xmlItem.value();
            jsonNode = iterateNode(omElement, attributePrefix, preserveNamespaces);
        } else {
            BXMLSequence xmlSequence = (BXMLSequence) xml;
            BRefValueArray sequence = xmlSequence.value();
            long count = sequence.size();
            ArrayList<OMElement> childArray = new ArrayList<>();
            ArrayList<OMText> textArray = new ArrayList<>();
            for (long i = 0; i < count; ++i) {
                BXMLItem xmlItem = (BXMLItem) sequence.get(i);
                OMNode omNode = xmlItem.value();
                if (omNode instanceof OMElement) {
                    childArray.add((OMElement) omNode);
                } else if (omNode instanceof OMText) {
                    textArray.add((OMText) omNode);
                }
            }
            JsonNode textArrayNode = null;
            if (textArray.size() > 0) {
                textArrayNode = processTextArray(textArray);
            }
            if (childArray.size() > 0) {
                jsonNode = OBJECT_MAPPER.createObjectNode();
                processArray((ObjectNode) jsonNode, childArray, attributePrefix, preserveNamespaces);
                if (textArrayNode != null) {
                    ((ArrayNode) textArrayNode).add(jsonNode);
                }
            }
            if (textArrayNode != null) {
                jsonNode = textArrayNode;
            }
        }
        return new BJSON(jsonNode);
    }

    private static ObjectNode iterateNode(OMElement omElement, String attributePrefix, boolean preserveNamespaces) {
        ObjectNode rootNode = OBJECT_MAPPER.createObjectNode();
        ArrayList<OMAttribute> attributeArray = collectAttributes(omElement);
        ArrayList<OMNamespace> namespaceArray = collectNamespaces(omElement);
        Iterator iterator = omElement.getChildElements();
        String keyValue = getElementKey(omElement, preserveNamespaces);
        if (iterator.hasNext()) {
            ObjectNode currentRoot = OBJECT_MAPPER.createObjectNode();
            ArrayList<OMElement> childArray = new ArrayList<>();
            HashMap<String, ArrayList<JsonNode>> rootMap = new HashMap<>();

            while (iterator.hasNext()) {
                OMNode node = (OMNode) iterator.next();
                OMElement omChildElement = (OMElement) node;
                ArrayList<OMAttribute> attributeArrayChild = collectAttributes(omChildElement);
                ArrayList<OMNamespace> namespaceArrayChild = collectNamespaces(omChildElement);
                Iterator iteratorChild = omChildElement.getChildElements();
                String childKeyValue = getElementKey(omChildElement, preserveNamespaces);

                if (iteratorChild.hasNext()) {
                    ObjectNode nodeIntermediate = iterateNode(omChildElement, attributePrefix, preserveNamespaces);
                    addToRootMap(rootMap, childKeyValue, nodeIntermediate.get(childKeyValue));
                } else {
                    if (attributeArrayChild.size() > 0 || namespaceArrayChild.size() > 0) {
                        ObjectNode attrObject = OBJECT_MAPPER.createObjectNode();
                        processAttributes(attributeArrayChild, attrObject, attributePrefix);
                        processNamespace(namespaceArrayChild, attrObject, attributePrefix);
                        //Value object
                        attrObject.put("#text", omChildElement.getText());
                        addToRootMap(rootMap, childKeyValue, attrObject);
                    } else {
                        childArray.add(omChildElement);
                    }
                }
            }

            if (namespaceArray.size() > 0) {
                processNamespace(namespaceArray, currentRoot, attributePrefix);
            }
            if (attributeArray.size() > 0) {
                processAttributes(attributeArray, currentRoot, attributePrefix);
            }
            if (childArray.size() > 0) {
                processArray(currentRoot, childArray, attributePrefix, preserveNamespaces);
            }
            if (rootMap.size() > 0) {
                processRootElements(currentRoot, rootMap);
            }
            rootNode.set(keyValue, currentRoot);
        } else {
            rootNode.put(keyValue, omElement.getText());
        }
        return rootNode;
    }

    private static String getElementKey(OMElement omElement, boolean preserveNamespaces) {
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

    private static void addToRootMap(HashMap<String, ArrayList<JsonNode>> rootMap, String key, JsonNode node) {
        rootMap.putIfAbsent(key, new ArrayList<>());
        rootMap.get(key).add(node);
    }

    private static void processAttributes(ArrayList<OMAttribute> attributeArray, ObjectNode rootNode,
            String attributePrefix) {
        int attrCount = attributeArray.size();
        for (int i = 0; i < attrCount; ++i) {
            OMAttribute attribute = attributeArray.get(i);
            String key = attributePrefix + attribute.getLocalName();
            rootNode.put(key, attribute.getAttributeValue());
        }
    }

    private static ArrayList<OMAttribute> collectAttributes(OMElement element) {
        ArrayList<OMAttribute> attributeArray = new ArrayList<>();
        Iterator attributeIterator = element.getAllAttributes();
        while (attributeIterator.hasNext()) {
            OMAttribute attribute = (OMAttribute) attributeIterator.next();
            attributeArray.add(attribute);
        }
        return attributeArray;
    }

    private static ArrayList<OMNamespace> collectNamespaces(OMElement element) {
        ArrayList<OMNamespace> namespaceArray = new ArrayList<>();
        Iterator namespaceIterator = element.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            OMNamespace namespace = (OMNamespace) namespaceIterator.next();
            namespaceArray.add(namespace);
        }
        return namespaceArray;
    }

    private static void processNamespace(ArrayList<OMNamespace> namespaceArray, ObjectNode rootNode,
            String attributePrefix) {
        int attrCount = namespaceArray.size();
        for (int i = 0; i < attrCount; ++i) {
            OMNamespace namespace = namespaceArray.get(i);
            String key = attributePrefix + "xmlns:" + namespace.getPrefix();
            rootNode.put(key, namespace.getNamespaceURI());
        }
    }

    private static ArrayNode processTextArray(ArrayList<OMText> childArray) {
        ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
        int elementCount = childArray.size();
        for (int i = 0; i < elementCount; ++i) {
            arrayNode.add(childArray.get(i).getText());
        }
        return arrayNode;
    }

    private static void processArray(ObjectNode root, ArrayList<OMElement> childArray, String attributePrefix,
            boolean preserveNamespaces) {
        LinkedHashMap<String, ArrayList<OMElement>> rootMap = new LinkedHashMap<>();
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
                        JsonNode node = iterateNode(element, attributePrefix, preserveNamespaces);
                        root.set(nodeKey, node.get(nodeKey));
                    } else {
                        root.put(nodeKey, elementList.get(0).getText());
                    }
                } else {
                    ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
                    int nodeCount = elementList.size();
                    for (int i = 0; i < nodeCount; ++i) {
                        arrayNode.add(elementList.get(i).getText());
                    }
                    root.set(nodeKey, arrayNode);
                }
            }
        }
    }

    private static void processRootElements(ObjectNode root, HashMap<String, ArrayList<JsonNode>> rootMap) {
        for (Map.Entry<String, ArrayList<JsonNode>> entry : rootMap.entrySet()) {
            String key = entry.getKey();
            ArrayList<JsonNode> elementList = entry.getValue();
            int elementCount = elementList.size();
            if (elementCount == 1) {
                root.set(key, elementList.get(0));
            } else {
                ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
                for (int i = 0; i < elementCount; ++i) {
                    arrayNode.add((elementList.get(i)));
                }
                root.set(key, arrayNode);
            }

        }
    }
}
