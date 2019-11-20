/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.XMLContentHolderItem;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.Set;

/**
 * @since 1.1.0
 */
public class StaxXMLSink extends OutputStream {
    private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
    private final Random random = new Random();
    private XMLStreamWriter xmlStreamWriter;
    private Deque<Set<String>> parentNSSet;
    private int nsNumber;


    public StaxXMLSink(OutputStream outputStream) {
        try {
            xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(outputStream);
            parentNSSet = new ArrayDeque<>();
        } catch (XMLStreamException e) {
            // ignore
        }
    }

    @Override
    public void write(int b) throws IOException {
        assert false;
    }

    @Override
    public void flush() throws IOException {
        try {
            xmlStreamWriter.flush();
        } catch (XMLStreamException e) {
            throw new BallerinaException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            xmlStreamWriter.close();
        } catch (XMLStreamException e) {
            throw new BallerinaException(e);
        }
    }

    public void write(XMLValue<?> xmlValue) {
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
                    writeXMLText((XMLContentHolderItem) xmlValue);
                    break;
                case COMMENT:
                    writeXMLComment((XMLContentHolderItem) xmlValue);
                    break;
                case PI:
                    writeXMLPI((XMLContentHolderItem) xmlValue);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + xmlValue.getNodeType());
            }
        } catch (XMLStreamException e) {
            // is this the best way to handle this???
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    private void writeXMLPI(XMLContentHolderItem xmlValue) throws XMLStreamException {
        xmlStreamWriter.writeProcessingInstruction(xmlValue.getTarget(), xmlValue.getData());
    }

    private void writeXMLComment(XMLContentHolderItem xmlValue) throws XMLStreamException {
        xmlStreamWriter.writeComment(xmlValue.getData());

    }

    private void writeXMLText(XMLContentHolderItem xmlValue) throws XMLStreamException {
        // todo: handle just test vs cdata
        xmlStreamWriter.writeCharacters(xmlValue.getData());

    }

    private void writeElement(XMLItem xmlValue) throws XMLStreamException {
        // Setup namespace hierarchy
        Set<String> prevNSSet = this.parentNSSet.peek();
        HashSet<String> currentNSLevel = prevNSSet == null ? new HashSet<>() : new HashSet<>(prevNSSet);
        this.parentNSSet.push(currentNSLevel);

        Map<String, String> nsPrefixMap = prefixToNSUri(xmlValue);
        QName qName = xmlValue.getQName();
        writeStartElement(qName, nsPrefixMap);
        setMissingElementPrefix(currentNSLevel, nsPrefixMap, qName);

        // Write namespaces
        writeNamespaceAttributes(currentNSLevel, nsPrefixMap);

        // Write attributes
        writeAttributes(xmlValue, currentNSLevel);

        xmlValue.children().serialize(this);
        xmlStreamWriter.writeEndElement();
        // Reset namespace decl hierarchy for this node.
        this.parentNSSet.pop();
    }

    private String setDefaultNamespace(Map<String, String> nsPrefixMap, QName qName) throws XMLStreamException {
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
        return null;
    }

    private void writeStartElement(QName qName, Map<String, String> nsPrefixMap) throws XMLStreamException {
        String defaultNamespaceUri = setDefaultNamespace(nsPrefixMap, qName);

        if (qName.getPrefix() == null || qName.getPrefix().isEmpty()) {
            xmlStreamWriter.writeStartElement(qName.getLocalPart());
        } else {
            xmlStreamWriter.writeStartElement(qName.getPrefix(), qName.getLocalPart(), qName.getNamespaceURI());
        }

        if (defaultNamespaceUri != null) {
            xmlStreamWriter.writeDefaultNamespace(defaultNamespaceUri);
        }
    }

    private void writeAttributes(XMLItem xmlValue, HashSet<String> curNSSet) throws XMLStreamException {
        for (Map.Entry<String, String> attributeEntry : xmlValue.getAttributesMap().entrySet()) {
            String key = attributeEntry.getKey();
            int closingCurlyPos = key.indexOf('}');
            // Attribute on elements default namespace
            if (closingCurlyPos == -1) {
                xmlStreamWriter.writeAttribute(key, attributeEntry.getValue());
            } else if (!key.startsWith(XMLItem.XMLNS_URL_PREFIX)) {
                String uri = key.substring(1, closingCurlyPos);

                // Prefix for the namespace is not defined.
                if (xmlStreamWriter.getNamespaceContext().getPrefix(uri) == null) {
                    generateAndAddRandomNSPrefix(curNSSet, uri);
                }
                String localName = key.substring(closingCurlyPos + 1);
                xmlStreamWriter.writeAttribute(uri, localName, attributeEntry.getValue());
            }
        }
    }

    private void writeNamespaceAttributes(HashSet<String> curNSSet, Map<String, String> nsPrefixMap)
            throws XMLStreamException {
        for (Map.Entry<String, String> nsEntry : nsPrefixMap.entrySet()) {
            if (nsEntry.getKey().isEmpty()) {
                xmlStreamWriter.setDefaultNamespace(nsEntry.getValue());
                xmlStreamWriter.writeDefaultNamespace(nsEntry.getValue());
            } else {
                // Only write the namespace decl if not in the namespace hierarchy.
                String nsKey = concatNsPrefixURI(nsEntry.getKey(), nsEntry.getValue());
                if (!curNSSet.contains(nsKey)) {
                    xmlStreamWriter.writeNamespace(nsEntry.getKey(), nsEntry.getValue());
                    xmlStreamWriter.setPrefix(nsEntry.getKey(), nsEntry.getValue());
                    curNSSet.add(nsKey);
                }
            }
        }
    }

    private void setMissingElementPrefix(HashSet<String> curNSSet, Map<String, String> nsPrefixMap, QName qName)
            throws XMLStreamException {
        if (!qName.getNamespaceURI().isEmpty() && qName.getPrefix().isEmpty()
                && alreadyDefinedNSPrefixNotFound(qName)) {
            for (Map.Entry<String, String> entry : nsPrefixMap.entrySet()) {
                if (entry.getValue().equals(qName.getNamespaceURI())) {
                    xmlStreamWriter.setPrefix(entry.getKey(), entry.getValue());
                    break;
                }
                if (entry.getKey().isEmpty()) {
                    xmlStreamWriter.setDefaultNamespace(entry.getValue());
                    xmlStreamWriter.writeDefaultNamespace(entry.getValue());
                }
            }
        }
    }

    private boolean alreadyDefinedNSPrefixNotFound(QName qName) {
        String prefix = xmlStreamWriter.getNamespaceContext().getPrefix(qName.getNamespaceURI());
        return prefix == null || prefix.isEmpty();
    }

    private void generateAndAddRandomNSPrefix(HashSet<String> curNSSet, String uri) throws XMLStreamException {
        String randomNSPrefix = generateRandomPrefix(curNSSet, 4);
        String nsKey = concatNsPrefixURI(randomNSPrefix, uri);
        xmlStreamWriter.writeNamespace(randomNSPrefix, uri);
        xmlStreamWriter.setPrefix(randomNSPrefix, uri);
        curNSSet.add(nsKey);
    }

    private String generateRandomPrefix(HashSet<String> curNSSet, int numChar) {
        // Generate random int between ASCII value of 'a' to 'z' and 9 more.
        // When generated value is between a-z use that as next char in random char sequence.
        // When generated value is over a-z range, convert it to 0-9 range and use that as next char.

//        PrimitiveIterator.OfInt iterator = random.ints('a', 'z' + 10).iterator();
//        char[] randomCharacters = new char[numChar];
//        for(int i = 0; i < numChar; i++) {
//            Integer val = iterator.next();
//            if (val > 'z') {
//                // Convert to 0 to 9 range
//                randomCharacters[i] = (char) ('0' + val - 'z');
//            } else {
//                // ASCII a to z range
//                randomCharacters[i] = (char) val.intValue();
//            }
//        }
//        String randStr = "ns" + new String(randomCharacters);
//
//        // Do not shadow already defined prefixes.
//        for (String hash : curNSSet) {
//            if (hash.startsWith(randStr)) {
//                return generateRandomPrefix(curNSSet, numChar);
//            }
//        }
//        return randStr;

        nsNumber++;
        return "ns" + nsNumber;
    }

    private String concatNsPrefixURI(String randStr, String nsUri) {
        return randStr + "<>" + nsUri;
    }

    private Map<String, String> prefixToNSUri(XMLItem xmlValue) {
        Map<String, String> nsPrefixMap = new HashMap<>();
        // Extract namespace entries
        for (Map.Entry<String, String> attributeEntry : xmlValue.getAttributesMap().entrySet()) {
            String key = attributeEntry.getKey();
            if (key.startsWith(XMLItem.XMLNS_URL_PREFIX)) {
                int closingCurly = key.indexOf('}');
                String prefix = key.substring(closingCurly + 1);
                nsPrefixMap.put(prefix, attributeEntry.getValue());
            }
        }

        // Remove NS prefixes which points to default NS URI
        String defaultNs = nsPrefixMap.get("");
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
        return nsPrefixMap;
    }

    private void writeSeq(XMLSequence xmlValue) {
        for (XMLValue<?> value : xmlValue.getChildrenList()) {
            this.write(value);
        }
    }
}
