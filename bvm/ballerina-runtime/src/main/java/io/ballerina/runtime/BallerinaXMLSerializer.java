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
package io.ballerina.runtime;

import com.ctc.wstx.api.WstxOutputProperties;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.values.XMLComment;
import io.ballerina.runtime.values.XMLItem;
import io.ballerina.runtime.values.XMLPi;
import io.ballerina.runtime.values.XMLSequence;
import io.ballerina.runtime.values.XMLText;

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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * XML Serializer for Ballerina XML value trees.
 *
 * @since 1.2.0
 */
public class BallerinaXMLSerializer extends OutputStream {
    private static final XMLOutputFactory xmlOutputFactory;
    private static final String XMLNS = "xmlns";
    private static final String EMPTY_STR = "";
    public static final String PARSE_XML_OP = "parse xml";
    public static final String XML = "xml";
    private XMLStreamWriter xmlStreamWriter;
    private Deque<Set<String>> parentNSSet;
    private int nsNumber;

    static {
        xmlOutputFactory = XMLOutputFactory.newInstance();
        if (xmlOutputFactory.getClass().getName().equals("com.ctc.wstx.stax.WstxOutputFactory")) {
            xmlOutputFactory.setProperty(WstxOutputProperties.P_OUTPUT_VALIDATE_STRUCTURE, false);
        }
    }



    public BallerinaXMLSerializer(OutputStream outputStream) {
        try {
            xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(outputStream);
            parentNSSet = new ArrayDeque<>();
        } catch (XMLStreamException e) {
            BLangExceptionHelper.handleXMLException(PARSE_XML_OP, e);
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
            BLangExceptionHelper.handleXMLException(PARSE_XML_OP, e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            xmlStreamWriter.close();
        } catch (XMLStreamException e) {
            BLangExceptionHelper.handleXMLException(PARSE_XML_OP, e);
        }
    }

    public void write(BXML xmlValue) {
        if (xmlValue == null) {
            return;
        }
        try {
            switch (xmlValue.getNodeType()) {
                case SEQUENCE:
                    writeSeq((XMLSequence) xmlValue);
                    break;
                case ELEMENT:
                    writeElement((XMLItem) xmlValue);
                    break;
                case TEXT:
                    writeXMLText((XMLText) xmlValue);
                    break;
                case COMMENT:
                    writeXMLComment((XMLComment) xmlValue);
                    break;
                case PI:
                    writeXMLPI((XMLPi) xmlValue);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + xmlValue.getNodeType());
            }
        } catch (XMLStreamException e) {
            BLangExceptionHelper.handleXMLException(PARSE_XML_OP, e);
        }
    }

    private void writeXMLPI(XMLPi xmlValue) throws XMLStreamException {
        xmlStreamWriter.writeProcessingInstruction(xmlValue.getTarget(), xmlValue.getData());
    }

    private void writeXMLComment(XMLComment xmlValue) throws XMLStreamException {
        xmlStreamWriter.writeComment(xmlValue.getTextValue());
    }

    private void writeXMLText(XMLText xmlValue) throws XMLStreamException {
        String textValue = xmlValue.getTextValue();
        if (!textValue.isEmpty()) {
            xmlStreamWriter.writeCharacters(textValue);
        }
    }

    private void writeElement(XMLItem xmlValue) throws XMLStreamException {
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

        xmlValue.getChildrenSeq().serialize(this);
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

        if (qName.getPrefix() == null || qName.getPrefix().isEmpty()) {
            xmlStreamWriter.writeStartElement(qName.getLocalPart());
        } else {
            xmlStreamWriter.writeStartElement(qName.getPrefix(), qName.getLocalPart(), qName.getNamespaceURI());
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

    private void writeAttributes(HashSet<String> curNSSet, Map<String, String> attributeMap) throws XMLStreamException {
        String defaultNS = xmlStreamWriter.getNamespaceContext().getNamespaceURI(XMLNS);
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
                    xmlStreamWriter.writeNamespace(prefix, nsUri);
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
        if (uri.isEmpty()) {
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

    private void splitAttributesAndNSPrefixes(XMLItem xmlValue,
                                              Map<String, String> nsPrefixMap,
                                              Map<String, String> attributeMap) {
        // Extract namespace entries
        for (Map.Entry<BString, BString> attributeEntry : xmlValue.getAttributesMap().entrySet()) {
            String key = attributeEntry.getKey().getValue();
            if (key.startsWith(XMLItem.XMLNS_URL_PREFIX)) {
                int closingCurly = key.indexOf('}');
                String prefix = key.substring(closingCurly + 1);
                if (prefix.equals(XML)) {
                    continue;
                }
                nsPrefixMap.put(prefix, attributeEntry.getValue().getValue());
            } else {
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

    private void writeSeq(XMLSequence xmlValue) {
        for (BXML value : xmlValue.getChildrenList()) {
            this.write(value);
        }
    }
}
