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
import java.util.Set;

/**
 * @since 1.1.0
 */
public class StaxXMLSink extends OutputStream {
    private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
    private XMLStreamWriter xmlStreamWriter;
    private Deque<Set<String>> parentNSSet;


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

    public void write(XMLValue<?> xmlValue) {
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
        QName qName = xmlValue.getQName();
        xmlStreamWriter.writeStartElement(qName.getPrefix(), qName.getLocalPart(), qName.getNamespaceURI());

        HashSet<String> curNSSet = setupNSHierarchy();

        Map<String, String> nsPrefixMap = prefixToNSUri(xmlValue);
        // Write namespaces
        for (Map.Entry<String, String> nsEntry : nsPrefixMap.entrySet()) {
            if (nsEntry.getKey().isEmpty()) {
                xmlStreamWriter.setDefaultNamespace(nsEntry.getValue());
                xmlStreamWriter.writeDefaultNamespace(nsEntry.getValue());
            } else {
                // Only write the namespace decl if not in the namespace hierarchy.
                String nsKey = nsEntry.getKey() + "<>" + nsEntry.getValue();
                if (!curNSSet.contains(nsKey)) {
                    xmlStreamWriter.writeNamespace(nsEntry.getKey(), nsEntry.getValue());
                    xmlStreamWriter.setPrefix(nsEntry.getKey(), nsEntry.getValue());
                    curNSSet.add(nsKey);
                }
            }

        }
        // Write attributes
        for (Map.Entry<String, String> attributeEntry : xmlValue.getAttributesMap().entrySet()) {
            String key = attributeEntry.getKey();
            int closingCurlyPos = key.indexOf('}');
            // Attribute on elements default namespace
            if (closingCurlyPos == -1) {
                xmlStreamWriter.writeAttribute(key, attributeEntry.getValue());
            } else if (!key.startsWith(XMLItem.XMLNS_URL_PREFIX)) {
                String uri = key.substring(1, closingCurlyPos);
                String localName = key.substring(closingCurlyPos + 1);
                xmlStreamWriter.writeAttribute(uri, localName, attributeEntry.getValue());
            }
        }

        xmlValue.children().serialize(this);
        xmlStreamWriter.writeEndElement();
        // Reset namesapce decl hierrarchy for this node.
        this.parentNSSet.pop();
    }

    private HashSet<String> setupNSHierarchy() {
        Set<String> prevNSSet = this.parentNSSet.peek();

        HashSet<String> curNSSet;
        if (prevNSSet == null) {
            curNSSet = new HashSet<>();
        } else {
            curNSSet = new HashSet<>(prevNSSet);
        }

        this.parentNSSet.push(curNSSet);
        return curNSSet;
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
