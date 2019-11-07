package org.ballerinalang.jvm;

import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.XMLContentHolderItem;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
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

public class StaxXMLSource {

    // XMLInputFactory2
    private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    private XMLStreamReader xmlStreamReader;
    private Map<String, String> namespaces; // xml ns declarations from Bal source [xmlns "http://ns.com" as ns]
    private Deque<XMLSequence> seqDeque;
    private Deque<List<XMLValue<?>>> siblingDeque; // we can remove this by opening up children list in XMLSeq

    public StaxXMLSource(String str) {
        this(new StringReader(str));
    }

    public StaxXMLSource(Reader stringReader) {
        namespaces = new HashMap<>();
        seqDeque = new ArrayDeque<>();
        siblingDeque = new ArrayDeque<>();
        try {
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);
        } catch (XMLStreamException e) {
            handleXMLStreamException(e);
        }
    }

    private void handleXMLStreamException(XMLStreamException e) {
        // todo: handle properly
        e.printStackTrace();
    }

    // todo: wrong method name. This is not iterative.
    public XMLValue next() {
        try {
            while (xmlStreamReader.hasNext()) {
                int next = xmlStreamReader.next();
                switch (next) {
                    case XMLEvent.START_ELEMENT:
                        readElement(xmlStreamReader);
                        break;
                    case XMLEvent.END_ELEMENT:
                        endElement();
                        break;
                    case XMLEvent.PROCESSING_INSTRUCTION:
                        readPI(xmlStreamReader);
                        break;
                    case XMLEvent.END_DOCUMENT:
                        return buildDocument();
                    case XMLEvent.COMMENT:
                        readComment(xmlStreamReader);
                        break;
                    case XMLEvent.CDATA:
                    case XMLEvent.CHARACTERS:
                        readCDATA(xmlStreamReader);
                        break;
                    case XMLEvent.NAMESPACE:
                        assert false;
                    default:
                        assert false;
                }
            }
        } catch (XMLStreamException e) {
            handleXMLStreamException(e);
        }

        return null;
    }

    private void readPI(XMLStreamReader xmlStreamReader) {
        setupXmlDocument();
        XMLContentHolderItem xmlItem = new XMLContentHolderItem(
                xmlStreamReader.getPIData(), xmlStreamReader.getPITarget());
        siblingDeque.peek().add(xmlItem);
    }

    private void readCDATA(XMLStreamReader xmlStreamReader) {
        // we don't support CDATA?
//        setupXmlDocument();
        XMLContentHolderItem xmlItem = new XMLContentHolderItem(xmlStreamReader.getText(), XMLNodeType.CDATA);
        siblingDeque.peek().add(xmlItem);
    }

    private void readComment(XMLStreamReader xmlStreamReader) {
        setupXmlDocument();

        XMLContentHolderItem xmlItem = new XMLContentHolderItem(xmlStreamReader.getText(), XMLNodeType.COMMENT);
        siblingDeque.peek().add(xmlItem);
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
        ArrayList<XMLValue<?>> children = new ArrayList<>();
        XMLSequence seq = new XMLSequence(children);
        seqDeque.push(seq);
        XMLItem xmlItem = new XMLItem(elemName, seq);
        siblingDeque.peek().add(xmlItem);

        addAttributesAndNamespaceDecl(xmlStreamReader, xmlItem, elemName);

        siblingDeque.push(children);
    }

    private void setupXmlDocument() {
        if (seqDeque.isEmpty()) {
            ArrayList<XMLValue<?>> children = new ArrayList<>();
            siblingDeque.push(children);
            XMLSequence xmlSequence = new XMLSequence(children);
            seqDeque.push(xmlSequence);
        }
    }

    // need to duplicate the same in xmlItem.setAttribute
    private void addAttributesAndNamespaceDecl(XMLStreamReader xmlStreamReader, XMLItem xmlItem, QName elemName) {
        @SuppressWarnings("unchecked")
        MapValue<String, String> attributesMap = (MapValue<String, String>) xmlItem.getAttributesMap();
        Set<QName> usedNS = new HashSet<>();

        int count = xmlStreamReader.getAttributeCount();
        for(int i = 0; i < count; i++) {
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

            String xmlnsPrefix = "{" + XMLConstants.XMLNS_ATTRIBUTE_NS_URI + "}" + prefix;
            attributesMap.put(xmlnsPrefix, namespaceURI);
        }
    }
}
