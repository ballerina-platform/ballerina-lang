/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.lang.typemappers;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.natives.AbstractNativeTypeMapper;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaTypeMapper;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLInputFactory;
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
 * Convert XML to JSON
 */
@BallerinaTypeMapper(
        packageName = "ballerina.lang.typemappers",
        typeMapperName = "xmlTojson",
        args = {@Argument(name = "x", type = TypeEnum.XML)},
        returnType = {@ReturnType(type = TypeEnum.JSON)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Converts XML to JSON") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "x",
        value = "XML value to be converted") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "json",
        value = "JSON representation of the given XML") })
public class XMLToJSON extends AbstractNativeTypeMapper {

    public BValue convert(Context ctx) {
        BXML msg = (BXML) getArgument(ctx, 0);
        BJSON result = convertXMLString(msg.stringValue());
        return result;
    }

    private BJSON convertXMLString(String message) throws BallerinaException {
        InputStream input = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
        InputStream results = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BJSON resultMsg = null;

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
        resultMsg = new BJSON(results);
        return resultMsg;
    }
}
