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

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlSequence;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.XmlComment;
import io.ballerina.runtime.internal.values.XmlItem;
import io.ballerina.runtime.internal.values.XmlPi;
import io.ballerina.runtime.internal.values.XmlQName;
import io.ballerina.runtime.internal.values.XmlSequence;
import io.ballerina.runtime.internal.values.XmlText;

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
public class XmlTreeBuilder {

    // XMLInputFactory2
    private static final XMLInputFactory xmlInputFactory;

    static {
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    }

    private XMLStreamReader xmlStreamReader;
    private Map<String, String> namespaces; // xml ns declarations from Bal source [xmlns "http://ns.com" as ns]
    private Deque<BXmlSequence> seqDeque;
    private Deque<List<BXml>> siblingDeque;

    public XmlTreeBuilder(String str) {
        this(new StringReader(str));
    }

    public XmlTreeBuilder(Reader stringReader) {
        namespaces = new HashMap<>();
        seqDeque = new ArrayDeque<>();
        siblingDeque = new ArrayDeque<>();

        ArrayList<BXml> siblings = new ArrayList<>();
        siblingDeque.push(siblings);
        seqDeque.push(new XmlSequence(siblings));

        try {
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);
        } catch (XMLStreamException e) {
            handleXMLStreamException(e);
        }
    }

    private void handleXMLStreamException(Exception e) {
        String reason = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
        if (reason == null) {
            throw ErrorCreator.createError(StringUtils.fromString(XmlFactory.PARSE_ERROR));
        }
        throw ErrorCreator.createError(StringUtils.fromString(XmlFactory.PARSE_ERROR_PREFIX + reason));
    }

    public BXml parse() {
        boolean readNext = false;
        int next;
        try {
            while (xmlStreamReader.hasNext()) {
                if (readNext) {
                    readNext = false;
                    next = xmlStreamReader.getEventType();
                } else {
                    next = xmlStreamReader.next();
                }
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
                        readCData(xmlStreamReader);
                        break;
                    case CHARACTERS:
                        readText(xmlStreamReader);
                        readNext = true;
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
        XmlPi xmlItem = (XmlPi) XmlFactory.createXMLProcessingInstruction(xmlStreamReader.getPITarget(),
                                                                          xmlStreamReader.getPIData());
        siblingDeque.peek().add(xmlItem);
    }

    private void readCData(XMLStreamReader xmlStreamReader) {
        siblingDeque.peek().add(new XmlText(xmlStreamReader.getText()));
    }

    private void readText(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        StringBuilder textBuilder = new StringBuilder();
        while (xmlStreamReader.getEventType() == CHARACTERS) {
            textBuilder.append(xmlStreamReader.getText());
            xmlStreamReader.next();
        }
        siblingDeque.peek().add(new XmlText(textBuilder.toString()));
    }

    private void readComment(XMLStreamReader xmlStreamReader) {
        XmlComment xmlComment = (XmlComment) XmlFactory.createXMLComment(xmlStreamReader.getText());
        siblingDeque.peek().add(xmlComment);
    }

    private BXmlSequence buildDocument() {
        this.siblingDeque.pop();
        return this.seqDeque.pop();
    }

    private void endElement() {
        this.siblingDeque.pop();
        this.seqDeque.pop();
    }

    private void readElement(XMLStreamReader xmlStreamReader) {
        QName elemName = xmlStreamReader.getName();
        XmlQName name = new XmlQName(elemName.getLocalPart(),
                                     elemName.getNamespaceURI(), elemName.getPrefix());
        XmlItem xmlItem = (XmlItem) XmlFactory.createXMLElement(name, name, null);

        seqDeque.push(xmlItem.getChildrenSeq());

        siblingDeque.peek().add(xmlItem);
        populateAttributeMap(xmlStreamReader, xmlItem, elemName);
        siblingDeque.push(xmlItem.getChildrenSeq().getChildrenList());
    }
    // need to duplicate the same in xmlItem.setAttribute

    // todo: need to write a comment explaining each step
    private void populateAttributeMap(XMLStreamReader xmlStreamReader, XmlItem xmlItem, QName elemName) {
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

            BString xmlnsPrefix = StringUtils.fromString(XmlItem.XMLNS_NS_URI_PREFIX + prefix);
            attributesMap.put(xmlnsPrefix, StringUtils.fromString(namespaceURI));
        }

        int namespaceCount = xmlStreamReader.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            String uri = xmlStreamReader.getNamespaceURI(i);
            String prefix = xmlStreamReader.getNamespacePrefix(i);
            if (prefix == null || prefix.isEmpty()) {
                attributesMap.put(XmlItem.XMLNS_PREFIX, StringUtils.fromString(uri));
            } else {
                attributesMap.put(StringUtils.fromString(XmlItem.XMLNS_NS_URI_PREFIX + prefix),
                                  StringUtils.fromString(uri));
            }
        }
    }
}
