/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.api.values.BXMLQName;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.ballerina.runtime.values.TableValueImpl;
import io.ballerina.runtime.values.XMLComment;
import io.ballerina.runtime.values.XMLItem;
import io.ballerina.runtime.values.XMLPi;
import io.ballerina.runtime.values.XMLSequence;
import io.ballerina.runtime.values.XMLText;
import io.ballerina.runtime.values.XMLValue;
import org.apache.axiom.om.DeferredParsingException;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.util.StAXParserConfiguration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static io.ballerina.runtime.values.XMLItem.createXMLItemWithDefaultNSAttribute;

/**
 * Common utility methods used for XML manipulation.
 * 
 * @since 0.995.0
 */
public class XMLFactory {
    public static final StAXParserConfiguration STAX_PARSER_CONFIGURATION = StAXParserConfiguration.STANDALONE;
    /**
     * Create a XML item from string literal.
     *
     * @param xmlStr String representation of the XML
     * @return XML sequence
     */
    public static BXML parse(String xmlStr) {
        try {
            if (xmlStr.isEmpty()) {
                return new XMLSequence();
            }

            XMLTreeBuilder treeBuilder = new XMLTreeBuilder(xmlStr);
            return treeBuilder.parse();
        } catch (BError e) {
            throw e;
        } catch (Throwable e) {
            throw ErrorCreator.createError(StringUtils.fromString(("failed to parse xml: " + e.getMessage())));
        }
    }

    /**
     * Create a XML sequence from string inputstream.
     *
     * @param xmlStream XML input stream
     * @return XML Sequence
     */
    public static BXML parse(InputStream xmlStream) {
        try {
            XMLTreeBuilder treeBuilder = new XMLTreeBuilder(new InputStreamReader(xmlStream));
            return treeBuilder.parse();
        } catch (DeferredParsingException e) {
            throw ErrorCreator.createError(StringUtils.fromString((e.getCause().getMessage())));
        } catch (Throwable e) {
            throw ErrorCreator.createError(StringUtils.fromString(("failed to create xml: " + e.getMessage())));
        }
    }

    /**
     * Create a XML sequence from string inputstream with a given charset.
     *
     * @param xmlStream XML imput stream
     * @param charset Charset to be used for parsing
     * @return XML Sequence
     */
    public static BXML parse(InputStream xmlStream, String charset) {
        try {
            XMLTreeBuilder xmlTreeBuilder = new XMLTreeBuilder(new InputStreamReader(xmlStream, charset));
            return xmlTreeBuilder.parse();
        } catch (DeferredParsingException e) {
            throw ErrorCreator.createError(StringUtils.fromString((e.getCause().getMessage())));
        } catch (Throwable e) {
            throw ErrorCreator.createError(StringUtils.fromString(("failed to create xml: " + e.getMessage())));
        }
    }

    /**
     * Create a XML sequence from string reader.
     *
     * @param reader XML reader
     * @return XML Sequence
     */
    public static BXML parse(Reader reader) {
        try {
            XMLTreeBuilder xmlTreeBuilder = new XMLTreeBuilder(reader);
            return xmlTreeBuilder.parse();
        } catch (DeferredParsingException e) {
            throw ErrorCreator.createError(StringUtils.fromString(e.getCause().getMessage()));
        } catch (Throwable e) {
            throw ErrorCreator.createError(StringUtils.fromString("failed to create xml: " + e.getMessage()));
        }
    }

    /**
     * Concatenate two XML sequences and produce a single sequence.
     *
     * @param firstSeq First XML sequence
     * @param secondSeq Second XML sequence
     * @return Concatenated XML sequence
     */
    public static XMLValue concatenate(XMLValue firstSeq, XMLValue secondSeq) {
        ArrayList<BXML> concatenatedList = new ArrayList<>();

        if (firstSeq.getNodeType() == XMLNodeType.TEXT && secondSeq.getNodeType() == XMLNodeType.TEXT) {
            return new XMLText(firstSeq.getTextValue() + secondSeq.getTextValue());
        }

        // Add all the items in the first sequence
        if (firstSeq.getNodeType() == XMLNodeType.SEQUENCE) {
            concatenatedList.addAll(((XMLSequence) firstSeq).getChildrenList());
        } else if (!firstSeq.isEmpty()) {
            concatenatedList.add(firstSeq);
        }

        // When last item of left seq and first item of right seq are both text nodes merge them into single consecutive
        // text node.
        if (!concatenatedList.isEmpty()) {
            int lastIndexOFLeftChildren = concatenatedList.size() - 1;
            BXML lastItem = concatenatedList.get(lastIndexOFLeftChildren);
            if (lastItem.getNodeType() == XMLNodeType.TEXT && secondSeq.getNodeType() == XMLNodeType.SEQUENCE) {
                List<BXML> rightChildren = ((XMLSequence) secondSeq).getChildrenList();
                if (!rightChildren.isEmpty()) {
                    BXML firsOfRightSeq = rightChildren.get(0);
                    if (firsOfRightSeq.getNodeType() == XMLNodeType.TEXT) {
                        concatenatedList.remove(lastIndexOFLeftChildren); // remove last item, from already copied list
                        concatenatedList.addAll(rightChildren);
                        String merged = ((XMLText) lastItem).getTextValue() + ((XMLText) firsOfRightSeq).getTextValue();
                        concatenatedList.set(lastIndexOFLeftChildren, new XMLText(merged));
                        return new XMLSequence(concatenatedList);
                    }
                }
            } else if (lastItem.getNodeType() == XMLNodeType.TEXT && secondSeq.getNodeType() == XMLNodeType.TEXT) {
                String merged = lastItem.getTextValue() + secondSeq.getTextValue();
                concatenatedList.set(lastIndexOFLeftChildren, new XMLText(merged));
                return new XMLSequence(concatenatedList);
            }
        }

        // Add all the items in the second sequence
        if (secondSeq.getNodeType() == XMLNodeType.SEQUENCE) {
            concatenatedList.addAll(((XMLSequence) secondSeq).getChildrenList());
        } else if (!secondSeq.isEmpty()) {
            concatenatedList.add(secondSeq);
        }

        return new XMLSequence(concatenatedList);
    }

    /**
     * Converts a {@link io.ballerina.runtime.values.TableValue} to {@link XMLValue}.
     *
     * @param table {@link io.ballerina.runtime.values.TableValue} to convert
     * @return converted {@link XMLValue}
     */
    public static BXML tableToXML(TableValueImpl table) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            XMLStreamWriter streamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
            TableOMDataSource tableOMDataSource = new TableOMDataSource(table, null, null);
            tableOMDataSource.serialize(streamWriter);
            streamWriter.flush();
            outputStream.flush();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            return parse(inputStream);
        } catch (IOException | XMLStreamException e) {
            throw new BallerinaException(e);
        }
    }

    /**
     * Create an element type XMLValue.
     *
     * @param startTagName Name of the start tag of the element
     * @param endTagName Name of the end tag of element
     * @param defaultNsUri Default namespace URI
     * @return XMLValue Element type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLElement(BXMLQName startTagName, BXMLQName endTagName, String defaultNsUri) {
        if (!isEqual(startTagName.getLocalName(), endTagName.getLocalName()) ||
                !isEqual(startTagName.getUri(), endTagName.getUri()) ||
                !isEqual(startTagName.getPrefix(), endTagName.getPrefix())) {
            throw ErrorCreator
                    .createError(StringUtils.fromString(("start and end tag names mismatch: '" + startTagName + "' " +
                            "and '" + endTagName + "'")));
        }
        return createXMLElement(startTagName, defaultNsUri);
    }

    /**
     * Create an element type XMLValue.
     *
     * @param startTagName Name of the start tag of the element
     * @param defaultNsUri Default namespace URI
     * @return XMLValue Element type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLElement(BXMLQName startTagName, String defaultNsUri) {
        return createXMLElement(startTagName, defaultNsUri, false);
    }

    public static XMLValue createXMLElement(BXMLQName startTagName, BString defaultNsUriVal) {
        return createXMLElement(startTagName,
                defaultNsUriVal == null ? XMLConstants.NULL_NS_URI : defaultNsUriVal.getValue());
    }

    /**
     * Create an element type XMLValue, specifying the type which will indicate mutability.
     *
     * @param startTagName  Name of the start tag of the element
     * @param defaultNsUri  Default namespace URI
     * @param readonly      Whether the element is immutable
     * @return XMLValue Element type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLElement(BXMLQName startTagName, String defaultNsUri, boolean readonly) {
        // Validate whether the tag names are XML supported qualified names, according to the XML recommendation.
        XMLValidator.validateXMLQName(startTagName);

        String nsUri = startTagName.getUri();
        if (defaultNsUri == null) {
            defaultNsUri = XMLConstants.NULL_NS_URI;
        }

        String prefix = startTagName.getPrefix() == null ? XMLConstants.DEFAULT_NS_PREFIX : startTagName.getPrefix();

        if (nsUri == null) {
            return new XMLItem(new QName(defaultNsUri, startTagName.getLocalName(), prefix), readonly);
        }
        return createXMLItemWithDefaultNSAttribute(new QName(nsUri, startTagName.getLocalName(), prefix), readonly,
                                                   defaultNsUri);
    }

    public static XMLValue createXMLElement(BXMLQName startTagName, BString defaultNsUriVal, boolean readonly) {
        return createXMLElement(startTagName,
                                defaultNsUriVal == null ? XMLConstants.NULL_NS_URI : defaultNsUriVal.getValue(),
                                readonly);
    }

    /**
     * Create a comment type XMLValue.
     *
     * @param content Comment content
     * @return XMLValue Comment type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLComment(String content) {
        return new XMLComment(content);
    }

    /**
     * Create a comment type XMLValue.
     *
     * @param content Comment content
     * @return XMLValue Comment type XMLValue
     */
    public static XMLValue createXMLComment(BString content) {
        return createXMLComment(content.getValue());
    }

    /**
     * Create a comment type XMLValue, specifying the type which will indicate mutability.
     *
     * @param content   Comment content
     * @param readonly  Whether the comment is immutable
     * @return XMLValue Comment type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLComment(String content, boolean readonly) {
        return new XMLComment(content, readonly);
    }

    /**
     * Create a comment type XMLValue, specifying the type which will indicate mutability.
     *
     * @param content   Comment content
     * @param readonly  Whether the comment is immutable
     * @return XMLValue Comment type XMLValue
     */
    public static XMLValue createXMLComment(BString content, boolean readonly) {
        return createXMLComment(content.getValue(), readonly);
    }

    /**
     * Create a comment type XMLValue.
     *
     * @param content Text content
     * @return XMLValue Text type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLText(String content) {
        return new XMLText(XMLTextUnescape.unescape(content));
    }

    /**
     * Create a comment type XMLValue.
     *
     * @param contentVal Text content
     * @return XMLValue Text type XMLValue
     */
    public static XMLValue createXMLText(BString contentVal) {
        return createXMLText(contentVal.getValue());
    }

    /**
     * Create a processing instruction type XMLValue.
     *
     * @param tartget PI target
     * @param data    PI data
     * @return XMLValue Processing instruction type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLProcessingInstruction(String tartget, String data) {
        return new XMLPi(data, tartget);
    }

    /**
     * Create a processing instruction type XMLValue.
     *
     * @param tartget PI target
     * @param data    PI data
     * @return XMLValue Processing instruction type XMLValue
     */
    public static XMLValue createXMLProcessingInstruction(BString tartget, BString data) {
        return createXMLProcessingInstruction(tartget.getValue(), data.getValue());
    }

    /**
     * Create a processing instruction type XMLValue, specifying the type which will indicate mutability.
     *
     * @param target    PI target
     * @param data      PI data
     * @param readonly  Whether the PI is immutable
     * @return XMLValue Processing instruction type XMLValue
     */
    @Deprecated
    public static XMLValue createXMLProcessingInstruction(String target, String data, boolean readonly) {
        return new XMLPi(data, target, readonly);
    }

    /**
     * Create a processing instruction type XMLValue, specifying the type which will indicate mutability.
     *
     * @param target    PI target
     * @param data      PI data
     * @param readonly  Whether the PI is immutable
     * @return XMLValue Processing instruction type XMLValue
     */
    public static XMLValue createXMLProcessingInstruction(BString target, BString data, boolean readonly) {
        return createXMLProcessingInstruction(target.getValue(), data.getValue(), readonly);
    }

    /**
     * Compares if two xml values are equal.
     *
     * Equality is computed as follows
     * - for XML elements: compares the canonicalized versions, including comments
     * - for non-elements (standalone text, PI, comments): a string comparison
     *
     * @param xmlOne the first XML value
     * @param xmlTwo the second XML value
     * @return true if the two are equal, false if not equal or an exception is thrown while checking equality
     */
    public static boolean isEqual(BXML xmlOne, BXML xmlTwo) {
        return xmlOne.equals(xmlTwo);
    }

    /**
     * Create an OMElement from an XML fragment given as a string.
     * Generously borrowed from Apache Axiom (org.apache.axiom.om.util.AXIOMUtil).
     *
     * @param xmlFragment the well-formed XML fragment
     * @return The OMElement created out of the string XML fragment.
     * @throws XMLStreamException when unexpected processing error occur while parsing.
     */
    public static OMElement stringToOM(String xmlFragment) throws XMLStreamException {
        return stringToOM(OMAbstractFactory.getOMFactory(), xmlFragment);
    }

    /**
     * Create an OMElement from an XML fragment given as a string.
     * Generously borrowed and improved from Apache Axiom (org.apache.axiom.om.util.AXIOMUtil).
     *
     * @param omFactory the factory used to build the object model
     * @param xmlFragment the well-formed XML fragment
     * @return The OMElement created out of the string XML fragment.
     * @throws XMLStreamException when unexpected processing error occur while parsing.
     */
    private static OMElement stringToOM(OMFactory omFactory, String xmlFragment) throws XMLStreamException {
        return xmlFragment != null
                ? OMXMLBuilderFactory
                    .createOMBuilder(omFactory, STAX_PARSER_CONFIGURATION, new StringReader(xmlFragment))
                    .getDocumentElement()
                : null;
    }

    /**
     * Replace xml text escape sequences with appropriate character.
     *
     * @since 1.2
     */
    public static class XMLTextUnescape {
        public static String unescape(String str) {
            return unescape(str.getBytes(StandardCharsets.UTF_8));
        }

        private static String unescape(byte[] bytes) {
            byte[] target = new byte[bytes.length];
            int size = bytes.length;
            int len = 0;

            for (int i = 0; i < size; i++, len++) {
                byte b = bytes[i];
                int i1 = i + 1; // index next to current index

                // Remove carriage return on windows environments to eliminate additional &#xd; being added
                if (b == '\r' && i1 < size && bytes[i1] == '\n') {
                    target[len] = '\n';
                    i += 1;
                    continue;
                }

                // &gt; &lt; and &amp; in XML literal in Ballerina lang maps to >, <, and & in XML infoset.
                if (b == '&') {
                    int i2 = i + 2; // index next next to current index
                    int i3 = i + 3; // index next next next current index
                    if (i3 < size && bytes[i1] == 'g' && bytes[i2] == 't' && bytes[i3] == ';') {
                        target[len] = '>';
                        i += 3;
                        continue;
                    }

                    if (i3 < size && bytes[i1] == 'l' && bytes[i2] == 't' && bytes[i3] ==  ';') {
                        target[len] = '<';
                        i += 3;
                        continue;
                    }

                    if (i3 + 1 < size && bytes[i1] == 'a' && bytes[i2] == 'm' && bytes[i3] == 'p'
                            && bytes[i3 + 1] == ';') {
                        target[len] = '&';
                        i += 4;
                        continue;
                    }
                }

                target[len] = b;
            }

            return new String(target, 0, len);
        }
    }

    public static boolean isEqual(String s1, String s2) {
        if (s1 == s2) {
            return true;
        } else if (s1 == null || s2 == null) {
            return false;
        } else {
            return s1.equals(s2);
        }
    }
}
