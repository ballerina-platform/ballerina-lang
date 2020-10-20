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

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.api.values.BXMLSequence;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.ballerina.runtime.values.MapValue;
import io.ballerina.runtime.values.XMLComment;
import io.ballerina.runtime.values.XMLItem;
import io.ballerina.runtime.values.XMLPi;
import io.ballerina.runtime.values.XMLQName;
import io.ballerina.runtime.values.XMLSequence;

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
 * @since 1.2.0
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
    private Deque<BXMLSequence> seqDeque;
    private Deque<List<BXML>> siblingDeque;

    public XMLTreeBuilder(String str) {
        this(new StringReader(str));
    }

    public XMLTreeBuilder(Reader stringReader) {
        namespaces = new HashMap<>();
        seqDeque = new ArrayDeque<>();
        siblingDeque = new ArrayDeque<>();

        ArrayList<BXML> siblings = new ArrayList<>();
        siblingDeque.push(siblings);
        seqDeque.push(new XMLSequence(siblings));

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

    public BXML parse() {
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
        XMLPi xmlItem = (XMLPi) XMLFactory.createXMLProcessingInstruction(xmlStreamReader.getPITarget(),
                xmlStreamReader.getPIData());
        siblingDeque.peek().add(xmlItem);
    }

    private void readText(XMLStreamReader xmlStreamReader) {
        siblingDeque.peek().add(XMLFactory.createXMLText(xmlStreamReader.getText()));
    }

    private void readComment(XMLStreamReader xmlStreamReader) {
        XMLComment xmlComment = (XMLComment) XMLFactory.createXMLComment(xmlStreamReader.getText());
        siblingDeque.peek().add(xmlComment);
    }

    private BXMLSequence buildDocument() {
        this.siblingDeque.pop();
        return this.seqDeque.pop();
    }

    private void endElement() {
        this.siblingDeque.pop();
        this.seqDeque.pop();
    }

    private void readElement(XMLStreamReader xmlStreamReader) {
        QName elemName = xmlStreamReader.getName();
        XMLQName name = new XMLQName(elemName.getLocalPart(),
                elemName.getNamespaceURI(), elemName.getPrefix());
        XMLItem xmlItem = (XMLItem) XMLFactory.createXMLElement(name, name, null);

        seqDeque.push(xmlItem.getChildrenSeq());

        siblingDeque.peek().add(xmlItem);
        populateAttributeMap(xmlStreamReader, xmlItem, elemName);
        siblingDeque.push(xmlItem.getChildrenSeq().getChildrenList());
    }
    // need to duplicate the same in xmlItem.setAttribute

    // todo: need to write a comment explaining each step
    private void populateAttributeMap(XMLStreamReader xmlStreamReader, XMLItem xmlItem, QName elemName) {
        MapValue<BString, BString> attributesMap = xmlItem.getAttributesMap();
        Set<QName> usedNS = new HashSet<>(); // Track namespace prefixes found in this element.

        int count = xmlStreamReader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            QName attributeName = xmlStreamReader.getAttributeName(i);
            attributesMap.put(StringUtils.fromString(attributeName.toString()),
                              StringUtils.fromString(xmlStreamReader.getAttributeValue(i)));
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

            BString xmlnsPrefix = StringUtils.fromString(XMLItem.XMLNS_URL_PREFIX + prefix);
            attributesMap.put(xmlnsPrefix, StringUtils.fromString(namespaceURI));
        }

        int namespaceCount = xmlStreamReader.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            String uri = xmlStreamReader.getNamespaceURI(i);
            String prefix = xmlStreamReader.getNamespacePrefix(i);
            if (prefix == null || prefix.isEmpty()) {
                attributesMap.put(StringUtils.fromString(XMLItem.XMLNS_URL_PREFIX + "xmlns"),
                                  StringUtils.fromString(uri));
            } else {
                attributesMap.put(StringUtils.fromString(XMLItem.XMLNS_URL_PREFIX + prefix),
                                  StringUtils.fromString(uri));
            }
        }
    }
}
