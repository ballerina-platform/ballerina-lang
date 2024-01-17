/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.values.XmlComment;
import io.ballerina.runtime.internal.values.XmlItem;
import io.ballerina.runtime.internal.values.XmlPi;
import io.ballerina.runtime.internal.values.XmlSequence;
import io.ballerina.runtime.internal.values.XmlText;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static io.ballerina.runtime.api.values.BXmlItem.XMLNS_NS_URI_PREFIX;

/**
 * XML Serializer for Ballerina XML value trees.
 *
 * @since 1.2.0
 */
public class BallerinaXmlSerializer extends OutputStream {
    private static final XMLOutputFactory xmlOutputFactory;
    private static final String XMLNS = "xmlns";
    private static final String XML_NAME_SPACE = "http://www.w3.org/XML/1998/namespace";
    private static final String EMPTY_STR = "";
    private static final String PARSE_XML_OP = "parse xml";
    private static final String XML = "xml";
    private static final String XML_NS_URI_PREFIX = "{" + XMLConstants.XML_NS_URI + "}";
    private static final String OUTPUT_VALIDATE_PROPERTY = "com.ctc.wstx.outputValidateStructure";
    private static final String OUTPUT_FACTORY = "com.ctc.wstx.stax.WstxOutputFactory";
    private static final String AUTOMATIC_END_ELEMENTS = "com.ctc.wstx.automaticEndElements";

    private XMLStreamWriter xmlStreamWriter;
    private Deque<Set<String>> parentNSSet;
    private int nsNumber;
    private boolean withinElement;

    static {
        xmlOutputFactory = XMLOutputFactory.newInstance();
        xmlOutputFactory.setProperty(OUTPUT_VALIDATE_PROPERTY, false);
        xmlOutputFactory.setProperty(AUTOMATIC_END_ELEMENTS, false);
    }

    public BallerinaXmlSerializer(OutputStream outputStream) {
        try {
            xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(outputStream);
            parentNSSet = new ArrayDeque<>();
        } catch (XMLStreamException e) {
            ErrorHelper.handleXMLException(PARSE_XML_OP, e);
        }
    }

    @Override
    public void write(int b) {
        assert false;
    }

    @Override
    public void flush() {
        try {
            xmlStreamWriter.flush();
        } catch (XMLStreamException e) {
            ErrorHelper.handleXMLException(PARSE_XML_OP, e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            xmlStreamWriter.close();
        } catch (XMLStreamException e) {
            ErrorHelper.handleXMLException(PARSE_XML_OP, e);
        }
    }

    public void write(BXml xmlValue) {
        if (xmlValue == null) {
            return;
        }
        try {
            switch (xmlValue.getNodeType()) {
                case SEQUENCE:
                    writeSeq((XmlSequence) xmlValue);
                    break;
                case ELEMENT:
                    writeElement((XmlItem) xmlValue);
                    break;
                case TEXT:
                    writeXMLText((XmlText) xmlValue);
                    break;
                case COMMENT:
                    writeXMLComment((XmlComment) xmlValue);
                    break;
                case PI:
                    writeXMLPI((XmlPi) xmlValue);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + xmlValue.getNodeType());
            }
        } catch (XMLStreamException e) {
            ErrorHelper.handleXMLException(PARSE_XML_OP, e);
        }
    }

    private void writeXMLPI(XmlPi xmlValue) throws XMLStreamException {
        xmlStreamWriter.writeProcessingInstruction(xmlValue.getTarget(), xmlValue.getData());
    }

    private void writeXMLComment(XmlComment xmlValue) throws XMLStreamException {
        xmlStreamWriter.writeComment(xmlValue.getTextValue());
    }

    private void writeXMLText(XmlText xmlValue) throws XMLStreamException {
        // No need to escape xml text when they are within xml element or if it is not from default stream writer.
        // It's handled by xml stream  writer.
        if (this.withinElement) {
            String textValue = xmlValue.getTextValue();
            if (!textValue.isEmpty()) {
                xmlStreamWriter.writeCharacters(textValue);
            }
        } else {
            char[] textValue = escapeCharacters(xmlValue.getTextValue());
            if (textValue.length != 0) {
                xmlStreamWriter.writeCharacters(textValue, 0, textValue.length);
            }
        }
    }

    private char[] escapeCharacters(String textValue) {
        char[] chars = textValue.toCharArray();
        int length = chars.length;
        CharArrayWriter writer = new CharArrayWriter(length);
        int i;
        for (i = 0; i < length; i++) {
            char c = chars[i];
            switch (c) {
                case '<':
                    writer.append("&lt;");
                    break;
                case '&':
                    writer.append("&amp;");
                    break;
                case '>':
                    writer.append("&gt;");
                    break;
                default:
                    writer.append(c);
            }
        }
        return writer.toCharArray();
    }

    private void writeElement(XmlItem xmlValue) throws XMLStreamException {
        // Setup namespace hierarchy
        Set<String> prevNSSet = this.parentNSSet.peek();
        HashSet<String> currentNSLevel = prevNSSet == null ? new HashSet<>() : new HashSet<>(prevNSSet);
        this.parentNSSet.push(currentNSLevel);

        Map<String, String> nsPrefixMap = new LinkedHashMap<>();
        Map<String, String> attributeMap = new LinkedHashMap<>();
        splitAttributesAndNSPrefixes(xmlValue, nsPrefixMap, attributeMap);

        QName qName = xmlValue.getQName();
        writeStartElement(qName, nsPrefixMap, currentNSLevel);
        setMissingElementPrefix(nsPrefixMap, qName);

        // Write namespaces
        writeNamespaceAttributes(currentNSLevel, nsPrefixMap);

        // Write attributes
        writeAttributes(currentNSLevel, attributeMap);

        // Track and override xml text escape when xml text is within an element.
        boolean prevWithinElementFlag = this.withinElement;
        this.withinElement = true;
        xmlValue.getChildrenSeq().serialize(this);
        this.withinElement = prevWithinElementFlag;

        xmlStreamWriter.writeEndElement();
        // Reset namespace decl hierarchy for this node.
        this.parentNSSet.pop();
    }

    private String setDefaultNamespace(Map<String, String> nsPrefixMap, QName qName, HashSet<String> currentNSLevel)
            throws XMLStreamException {
        boolean elementNSUsageFoundInAttribute = false;
        for (Map.Entry<String, String> entry : nsPrefixMap.entrySet()) {
            if (entry.getValue().equals(qName.getNamespaceURI())) {
                elementNSUsageFoundInAttribute = true;
            }
            if (entry.getKey().isEmpty()) {
                xmlStreamWriter.setDefaultNamespace(entry.getValue());
                return entry.getValue();
            }
        }
        if (!elementNSUsageFoundInAttribute && !qName.getNamespaceURI().isEmpty()) {
            xmlStreamWriter.setDefaultNamespace(qName.getNamespaceURI());
            return qName.getNamespaceURI();
        }

        String defaultNsURI = nsPrefixMap.get(XMLNS);
        if (defaultNsURI != null) {
            xmlStreamWriter.setDefaultNamespace(defaultNsURI);
            return defaultNsURI;
        }

        // Undeclare default namespace for this element, if outer elements have redefined default ns and this
        // element doesn't have NS URI in it's name.
        if ((qName.getNamespaceURI() == null || qName.getNamespaceURI().isEmpty())) {
            for (String s : currentNSLevel) {
                if (s.startsWith(XMLNS)) {
                    xmlStreamWriter.setDefaultNamespace(EMPTY_STR);
                    return EMPTY_STR;
                }
            }
        }

        return null;
    }

    private void writeStartElement(QName qName, Map<String, String> nsPrefixMap, HashSet<String> currentNSLevel)
            throws XMLStreamException {
        String defaultNamespaceUri = setDefaultNamespace(nsPrefixMap, qName, currentNSLevel);

        if (qName.getNamespaceURI().equals(defaultNamespaceUri)) {
            xmlStreamWriter.writeStartElement(qName.getLocalPart());
        } else {
            String prefix = getXmlNsUriPrefix(nsPrefixMap, qName.getNamespaceURI());
            if (prefix != null) {
                xmlStreamWriter.writeStartElement(prefix, qName.getLocalPart(), qName.getNamespaceURI());
            } else {
                xmlStreamWriter.writeStartElement(qName.getLocalPart());
            }
        }

        if (defaultNamespaceUri == null) {
            return;
        }

        String defaultNsMapEntry = concatNsPrefixURI("", defaultNamespaceUri);
        if (!currentNSLevel.contains(defaultNsMapEntry)) {
            xmlStreamWriter.writeDefaultNamespace(defaultNamespaceUri);
            currentNSLevel.add(defaultNsMapEntry);
        }
    }

    private String getXmlNsUriPrefix(Map<String, String> nsPrefixMap, String uri) {
        for (Map.Entry<String, String> entry : nsPrefixMap.entrySet()) {
            if (entry.getValue().equals(uri)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void writeAttributes(HashSet<String> curNSSet, Map<String, String> attributeMap) throws XMLStreamException {
        String defaultNS = xmlStreamWriter.getNamespaceContext().getNamespaceURI("");
        for (Map.Entry<String, String> attributeEntry : attributeMap.entrySet()) {
            String key = attributeEntry.getKey();
            int closingCurlyPos = key.lastIndexOf('}');
            // Attribute on elements default namespace
            if (closingCurlyPos == -1) {
                xmlStreamWriter.writeAttribute(key, attributeEntry.getValue());
            } else {
                String uri = key.substring(1, closingCurlyPos);

                // Prefix for the namespace is not defined.
                if (xmlStreamWriter.getNamespaceContext().getPrefix(uri) == null) {
                    generateAndAddRandomNSPrefix(curNSSet, uri);
                }
                String localName = key.substring(closingCurlyPos + 1);
                if (uri.isEmpty() || uri.equals(defaultNS)) {
                    xmlStreamWriter.writeAttribute(localName, attributeEntry.getValue());
                } else {
                    xmlStreamWriter.writeAttribute(uri, localName, attributeEntry.getValue());
                }
            }
        }
    }

    private void writeNamespaceAttributes(HashSet<String> curNSSet, Map<String, String> nsPrefixMap)
            throws XMLStreamException {
        for (Map.Entry<String, String> nsEntry : nsPrefixMap.entrySet()) {
            String prefix = nsEntry.getKey();
            if (!(prefix.isEmpty() || prefix.equals(XMLNS))) {
                // Only write the namespace decl if not in the namespace hierarchy.
                String nsUri = nsEntry.getValue();
                String nsKey = concatNsPrefixURI(prefix, nsUri);
                if (!curNSSet.contains(nsKey)) {
                    // We don't need to write the namespace prefix `xml` as it's predefined.
                    // It's legal to write this, but it adds unwanted extra text.
                    if (!prefix.equals(XML)) {
                        xmlStreamWriter.writeNamespace(prefix, nsUri);
                    }
                    xmlStreamWriter.setPrefix(prefix, nsUri);
                    curNSSet.add(nsKey);
                }
            }
        }
    }

    private void setMissingElementPrefix(Map<String, String> nsPrefixMap, QName qName)
            throws XMLStreamException {
        String namespaceURI = qName.getNamespaceURI();
        if (!namespaceURI.isEmpty() && qName.getPrefix().isEmpty() && alreadyDefinedNSPrefixNotFound(qName)) {
            for (Map.Entry<String, String> entry : nsPrefixMap.entrySet()) {
                if (entry.getValue().equals(namespaceURI) && !entry.getKey().equals(XMLNS)) {
                    xmlStreamWriter.setPrefix(entry.getKey(), entry.getValue());
                    break;
                }
            }
        }
    }

    private boolean alreadyDefinedNSPrefixNotFound(QName qName) {
        String prefix = xmlStreamWriter.getNamespaceContext().getPrefix(qName.getNamespaceURI());
        return prefix == null || prefix.isEmpty();
    }

    private void generateAndAddRandomNSPrefix(HashSet<String> curNSSet, String uri) throws XMLStreamException {
        // Namespace URI and the prefix `xml` is predefined, hence no need to generate a prefix for that
        if (uri.isEmpty() || XML_NAME_SPACE.equals(uri)) {
            return;
        }
        String randomNSPrefix = generateRandomPrefix(curNSSet, uri);
        String nsKey = concatNsPrefixURI(randomNSPrefix, uri);
        xmlStreamWriter.writeNamespace(randomNSPrefix, uri);
        xmlStreamWriter.setPrefix(randomNSPrefix, uri);
        curNSSet.add(nsKey);
    }

    private String generateRandomPrefix(HashSet<String> curNSSet, String uri) {
        nsNumber++;
        String generatedNs = "ns" + nsNumber;
        if (curNSSet.contains(concatNsPrefixURI(generatedNs, uri))) {
            return generateRandomPrefix(curNSSet, uri);
        }
        for (String nsFrag : curNSSet) {
            int end = nsFrag.indexOf("<>");
            String prefix = nsFrag.substring(0, end);
            if (prefix.equals(generatedNs)) {
                return generateRandomPrefix(curNSSet, uri);
            }
        }
        return generatedNs;
    }

    private String concatNsPrefixURI(String randStr, String nsUri) {
        return randStr + "<>" + nsUri;
    }

    private void splitAttributesAndNSPrefixes(XmlItem xmlValue,
                                              Map<String, String> nsPrefixMap,
                                              Map<String, String> attributeMap) {
        // Extract namespace entries
        for (Map.Entry<BString, BString> attributeEntry : xmlValue.getAttributesMap().entrySet()) {
            String key = attributeEntry.getKey().getValue();
            if (key.startsWith(XMLNS_NS_URI_PREFIX)) {
                int closingCurly = key.indexOf('}');
                String prefix = key.substring(closingCurly + 1);
                if (prefix.equals(XML)) {
                    continue;
                }
                nsPrefixMap.put(prefix, attributeEntry.getValue().getValue());
            } else {
                // If `xml` namespace URI is used, we need to add `xml` namespace prefix to prefixMap
                if (key.startsWith(XML_NS_URI_PREFIX)) {
                    nsPrefixMap.put(XML, XMLConstants.XML_NS_URI);
                }
                attributeMap.put(key, attributeEntry.getValue().getValue());
            }
        }

        // Remove NS prefixes which points to default NS URI
        String defaultNs = nsPrefixMap.get(EMPTY_STR);
        if (defaultNs != null) {
            List<String> alternativePrefixes = new ArrayList<>();
            for (Map.Entry<String, String> entry : nsPrefixMap.entrySet()) {
                if (!entry.getKey().isEmpty() && entry.getValue().equals(defaultNs)) {
                    alternativePrefixes.add(entry.getKey());
                }
            }
            for (String prefix : alternativePrefixes) {
                nsPrefixMap.remove(prefix);
            }
        }
    }

    private void writeSeq(XmlSequence xmlValue) {
        for (BXml value : xmlValue.getChildrenList()) {
            this.write(value);
        }
    }
}
