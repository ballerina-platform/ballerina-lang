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
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.XMLComment;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLPi;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BXML;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.DTD;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * XML tree builder for Ballerina xml node structure using {@code XMLStreamReader}.
 *
 * @since 1.1.0
 */
public class XMLTreeBuilder {

    // XMLInputFactory2
    private static final XMLInputFactory xmlInputFactory;

    static {
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    }

    private XMLStreamReader xmlStreamReader;
    private Map<String, String> namespaces; // xml ns declarations from Bal source [xmlns "http://ns.com" as ns]
    private Deque<XMLSequence> seqDeque;
    private Deque<List<BXML>> siblingDeque; // we can remove this by opening up children list in XMLSeq

    public XMLTreeBuilder(String str) {
        this(new StringReader(str));
    }

    public XMLTreeBuilder(Reader stringReader) {
        namespaces = new HashMap<>();
        seqDeque = new ArrayDeque<>();
        siblingDeque = new ArrayDeque<>();
        try {
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);
        } catch (XMLStreamException e) {
            handleXMLStreamException(e);
        }
    }

    private void handleXMLStreamException(Exception e) {
        // todo: do e.getMessage contain all the information? verify
        throw new BallerinaException(e.getMessage(), e);
    }

    public XMLValue parse() {
        try {
            while (xmlStreamReader.hasNext()) {
                int next = xmlStreamReader.next();
                switch (next) {
                    case START_ELEMENT:
                        readElement(xmlStreamReader);
                        break;
                    case END_ELEMENT:
                        endElement();
                        break;
                    case PROCESSING_INSTRUCTION:
                        readPI(xmlStreamReader);
                        break;
                    case COMMENT:
                        readComment(xmlStreamReader);
                        break;
                    case CDATA:
                    case CHARACTERS:
                        readText(xmlStreamReader);
                        break;
                    case END_DOCUMENT:
                        return buildDocument();
                    case DTD:
                        handleDTD(xmlStreamReader);
                        break;
                    default:
                        assert false;
                }
            }
        } catch (Exception e) {
            handleXMLStreamException(e);
        }

        return null;
    }

    private void handleDTD(XMLStreamReader xmlStreamReader) {
        // ignore
    }

    private void readPI(XMLStreamReader xmlStreamReader) {
        setupXmlDocument();
        XMLPi xmlItem = new XMLPi(
                xmlStreamReader.getPIData(), xmlStreamReader.getPITarget());
        siblingDeque.peek().add(xmlItem);
    }

    private void readText(XMLStreamReader xmlStreamReader) {
        setupXmlDocument();
        siblingDeque.peek().add(XMLFactory.createXMLText(xmlStreamReader.getText()));
    }

    private void readComment(XMLStreamReader xmlStreamReader) {
        setupXmlDocument();

        siblingDeque.peek().add(new XMLComment(xmlStreamReader.getText()));
    }

    private XMLValue buildDocument() {
        this.siblingDeque.pop();
        return this.seqDeque.pop();
    }

    private void endElement() {
        this.siblingDeque.pop();
        this.seqDeque.pop();
    }

    private void readElement(XMLStreamReader xmlStreamReader) {
        setupXmlDocument();

        QName elemName = xmlStreamReader.getName();
        ArrayList<BXML> children = new ArrayList<>();
        XMLSequence seq = new XMLSequence(children);
        seqDeque.push(seq);
        XMLItem xmlItem = new XMLItem(elemName, seq);
        siblingDeque.peek().add(xmlItem);

        addAttributesAndNamespaceDecl(xmlStreamReader, xmlItem, elemName);

        siblingDeque.push(children);
    }

    private void setupXmlDocument() {
        if (seqDeque.isEmpty()) {
            ArrayList<BXML> children = new ArrayList<>();
            siblingDeque.push(children);
            XMLSequence xmlSequence = new XMLSequence(children);
            seqDeque.push(xmlSequence);
        }
    }

    // need to duplicate the same in xmlItem.setAttribute
    private void addAttributesAndNamespaceDecl(XMLStreamReader xmlStreamReader, XMLItem xmlItem, QName elemName) {
        MapValue<String, String> attributesMap = xmlItem.getAttributesMap();
        Set<QName> usedNS = new HashSet<>();

        int count = xmlStreamReader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            QName attributeName = xmlStreamReader.getAttributeName(i);
            attributesMap.put(attributeName.toString(), xmlStreamReader.getAttributeValue(i));
            if (!attributeName.getPrefix().isEmpty()) {
                usedNS.add(attributeName);
            }
        }
        if (!elemName.getPrefix().isEmpty()) {
            usedNS.add(elemName);
        }
        for (QName qName : usedNS) {
            String prefix = qName.getPrefix();
            String namespaceURI = qName.getNamespaceURI();
            if (namespaceURI.isEmpty()) {
                namespaceURI = namespaces.getOrDefault(prefix, "");
            }

            String xmlnsPrefix = XMLItem.XMLNS_URL_PREFIX + prefix;
            attributesMap.put(xmlnsPrefix, namespaceURI);
        }

        int namespaceCount = xmlStreamReader.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            String uri = xmlStreamReader.getNamespaceURI(i);
            String prefix = xmlStreamReader.getNamespacePrefix(i);
            if (prefix == null || prefix.isEmpty()) {
                String xmlnsPrefix = XMLItem.XMLNS_URL_PREFIX;
                attributesMap.put(xmlnsPrefix, uri);
            } else {
                attributesMap.put(XMLItem.XMLNS_URL_PREFIX + prefix, uri);
            }
        }
    }
}
