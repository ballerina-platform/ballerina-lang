/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.model.util;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.xml.util.PrettyXMLEventWriter;

import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.ballerinalang.model.DataTableOMDataSource;
import org.ballerinalang.model.values.BDataTable;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;

/**
 * Common utility methods used for XML manipulation.
 * 
 * @since 0.88
 */
public class XMLUtils {
    
    private static final String XML_ROOT = "root";
    
    /**
     * Converts a {@link BXML} to {@link BJSON}.
     * 
     * @param xml {@link BXML} to convert
     * @return converted {@link BJSON} 
     * @throws BallerinaException
     */
    public static BJSON toJSON(BXML xml) throws BallerinaException {
        InputStream input = new ByteArrayInputStream(xml.stringValue().getBytes(StandardCharsets.UTF_8));
        InputStream results = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BJSON json = null;

        JsonXMLConfig config = new JsonXMLConfigBuilder().autoArray(true).autoPrimitive(true).prettyPrint(true).build();
        try {
            //Create source (XML).
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(input);
            Source source = new StAXSource(reader);

            //Create result (JSON).
            XMLStreamWriter writer = new JsonXMLOutputFactory(config).createXMLStreamWriter(output);
            Result result = new StAXResult(writer);

            //Copy source to result via "identity transform".
            TransformerFactory.newInstance().newTransformer().transform(source, result);

            byte[] outputByteArray = output.toByteArray();
            results = new ByteArrayInputStream(outputByteArray);

        } catch (TransformerConfigurationException e) {
            throw new BallerinaException("Error in parsing the JSON Stream. Transformer Configuration issue", e);
        } catch (TransformerException e) {
            throw new BallerinaException("Error in parsing the JSON Stream", e);
        } catch (XMLStreamException e) {
            throw new BallerinaException("Error in parsing the XML Stream", e);
        } finally {
            //As per StAX specification, XMLStreamReader/Writer.close() doesn't close the underlying stream.
            try {
                output.close();
                input.close();
            } catch (IOException ignore) {

            }
        }
        json = new BJSON(results);
        return json;
    }
    
    /**
     * Converts a {@link BJSON} to {@link BXML}.
     * 
     * @param msg {@link BJSON} to convert
     * @return converted {@link BXML}
     */
    public static BXML jsonToXML(BJSON msg) {
        InputStream input = new ByteArrayInputStream(msg.stringValue().getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).virtualRoot(XML_ROOT).build();
        BXML result = null;

        XMLEventReader reader = null;
        XMLEventWriter writer = null;
        try {
            reader = new JsonXMLInputFactory(config).createXMLEventReader(input);
            writer = XMLOutputFactory.newInstance().createXMLEventWriter(output);
            writer = new PrettyXMLEventWriter(writer);
            writer.add(reader);
        } catch (XMLStreamException e) {
            throw new BallerinaException("Error in parsing the XML Stream", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }

                if (writer != null) {
                    writer.close();
                }
            } catch (XMLStreamException ignore) {
            }
            try {
                output.close();
                input.close();
            } catch (IOException ignore) {
            }
        }

        byte[] xml = output.toByteArray();
        result = new BXML(new ByteArrayInputStream(xml));
        return result;
    }

    /**
     * Converts a {@link BDataTable} to {@link BXML}.
     *
     * @param dataTable {@link BDataTable} to convert
     * @return converted {@link BXML}
     */
    public static BXML datatableToXML(BDataTable dataTable) {
        OMSourcedElementImpl omSourcedElement = new OMSourcedElementImpl();
        omSourcedElement.init(new DataTableOMDataSource(dataTable, null, null));
        return new BXML(omSourcedElement);
    }
}
