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

import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.values.TableValueImpl;

import java.io.InputStream;
import java.io.Reader;

/**
 * Class @{@link XMLUtils} provides APIs to handle xml values.
 *
 * @since 2.0.0
 */
public class XMLUtils {

    /**
     * Create a XML item from string literal.
     *
     * @param xmlStr String representation of the XML
     * @return XML sequence
     */
    public static BXML parse(String xmlStr) {
        return XMLFactory.parse(xmlStr);
    }

    /**
     * Create a XML item from ballerina string literal.
     *
     * @param xmlStr String representation of the XML
     * @return XML sequence
     */
    public static BXML parse(BString xmlStr) {
        return XMLFactory.parse(xmlStr.getValue());
    }

    /**
     * Create a XML sequence from string inputstream.
     *
     * @param xmlStream XML input stream
     * @return XML Sequence
     */
    public static BXML parse(InputStream xmlStream) {
        return XMLFactory.parse(xmlStream);
    }

    /**
     * Create a XML sequence from string inputstream with a given charset.
     *
     * @param xmlStream XML imput stream
     * @param charset   Charset to be used for parsing
     * @return XML Sequence
     */
    public static BXML parse(InputStream xmlStream, String charset) {
        return XMLFactory.parse(xmlStream, charset);
    }

    /**
     * Create a XML sequence from string reader.
     *
     * @param reader XML reader
     * @return XML Sequence
     */
    public static BXML parse(Reader reader) {
        return XMLFactory.parse(reader);
    }

    /**
     * Converts a {@link io.ballerina.runtime.values.TableValue} to {@link BXML}.
     *
     * @param table {@link io.ballerina.runtime.values.TableValue} to convert
     * @return converted {@link BXML}
     */
    public static BXML parse(BTable table) {
        return XMLFactory.tableToXML((TableValueImpl) table);
    }
}
