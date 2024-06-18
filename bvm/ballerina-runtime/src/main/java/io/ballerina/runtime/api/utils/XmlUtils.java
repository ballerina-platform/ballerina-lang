/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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
package io.ballerina.runtime.api.utils;

import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlQName;
import io.ballerina.runtime.internal.XmlFactory;
import io.ballerina.runtime.internal.XmlValidator;
import io.ballerina.runtime.internal.values.TableValueImpl;

import java.io.InputStream;
import java.io.Reader;

/**
 * Class {@link XmlUtils} provides APIs to handle xml values.
 *
 * @since 2.0.0
 */
public final class XmlUtils {

    private XmlUtils() {}

    /**
     * Create a XML item from string literal.
     *
     * @param xmlStr String representation of the XML
     * @return XML sequence
     */
    public static BXml parse(String xmlStr) {
        return XmlFactory.parse(xmlStr);
    }

    /**
     * Create a XML item from ballerina string literal.
     *
     * @param xmlStr String representation of the XML
     * @return XML sequence
     */
    public static BXml parse(BString xmlStr) {
        return XmlFactory.parse(xmlStr.getValue());
    }

    /**
     * Create a XML sequence from string input stream.
     *
     * @param xmlStream XML input stream
     * @return XML Sequence
     */
    public static BXml parse(InputStream xmlStream) {
        return XmlFactory.parse(xmlStream);
    }

    /**
     * Create a XML sequence from string input stream with a given charset.
     *
     * @param xmlStream XML input stream
     * @param charset   Charset to be used for parsing
     * @return XML Sequence
     */
    public static BXml parse(InputStream xmlStream, String charset) {
        return XmlFactory.parse(xmlStream, charset);
    }

    /**
     * Create a XML sequence from string reader.
     *
     * @param reader XML reader
     * @return XML Sequence
     */
    public static BXml parse(Reader reader) {
        return XmlFactory.parse(reader);
    }

    /**
     * Converts a {@link BTable} to {@link BXml}.
     *
     * @param table {@link BTable} to convert
     * @return converted {@link BXml}
     */
    public static BXml parse(BTable table) {
        return XmlFactory.tableToXML((TableValueImpl) table);
    }

    /**
     * <p>
     * Validate a {@link BXmlQName} against the XSD definition.
     * </p>
     * <i>
     * NCName ::= (Letter | '_') (NCNameChar)*
     * NCNameChar ::= Letter | Digit | '.' | '-' | '_' | CombiningChar | Extender
     * </i>
     *
     * @param qname {@link BXmlQName} to check the validity
     * @throws BError if invalid XML qname
     */
    public static void validateXmlQName(BXmlQName qname) throws BError {
        XmlValidator.validateXMLQName(qname);
    }

    /**
     * <p>
     * Validate a name against the XSD definition. Checks whether the provided name is
     * a valid XML qualified name.
     * </p>
     * <i>
     * NCName ::= (Letter | '_') (NCNameChar)*
     * NCNameChar ::= Letter | Digit | '.' | '-' | '_' | CombiningChar | Extender
     * </i>
     *
     * @param name Name to check the validity
     * @throws BError if invalid XML name
     */
    public static void validateXmlName(String name) throws BError {
        XmlValidator.validateXMLName(name);
    }

    /**
     * <p>
     * Validate a name against the XSD definition. Checks whether the provided name is
     * a XML supported non-colonized name.
     * </p>
     * <i>
     * NCName ::= (Letter | '_') (NCNameChar)*
     * NCNameChar ::= Letter | Digit | '.' | '-' | '_' | CombiningChar | Extender
     * </i>
     *
     * @param name Name to check the validity as an XML name
     * @return Flag indicating whether the name is a valid XML name
     */
    public static boolean isValid(String name) {
        return XmlValidator.isValid(name);
    }
}
