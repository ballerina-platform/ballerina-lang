/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.stdlib.mime;

import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityWrapper;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeConstants;
import org.ballerinalang.mime.util.MimeUtil;
import org.jvnet.mimepull.MIMEPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_XML;
import static org.ballerinalang.mime.util.MimeConstants.BODY_PARTS;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_STRUCT;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_MIXED;
import static org.ballerinalang.mime.util.MimeConstants.OCTET_STREAM;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_MIME_PKG_ID;
import static org.ballerinalang.mime.util.MimeConstants.TEXT_PLAIN;

/**
 * Contains utility functions used by mime test cases.
 *
 * @since 0.990.3
 */
public class Util {
    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    /**
     * From a given list of body parts get a ballerina value array.
     *
     * @param bodyParts List of body parts
     * @return BValueArray representing an array of entities
     */
    public static BArray getArrayOfBodyParts(ArrayList<BObject> bodyParts) {
        Type typeOfBodyPart = bodyParts.get(0).getType();
        BObject[] result = bodyParts.toArray(new BObject[bodyParts.size()]);
        return ValueCreator.createArrayValue(result, TypeCreator.createArrayType(typeOfBodyPart));
    }

    /**
     * Get a text body part from a given text content.
     *
     * @return A ballerina struct that represent a body part
     */
    public static BObject getTextBodyPart() {
        String textPayload = "Ballerina text body part";
        BObject bodyPart = createEntityObject();
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(textPayload));
        MimeUtil.setContentType(createMediaTypeObject(), bodyPart, TEXT_PLAIN);
        return bodyPart;
    }

    /**
     * Get a text body part as a file upload.
     *
     * @return A body part with text content in a file
     */
    public static BObject getTextFilePart() {
        try {
            File file = getTemporaryFile("test", ".txt", "Ballerina text as a file part");
            BObject bodyPart = createEntityObject();
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(createMediaTypeObject(), bodyPart, TEXT_PLAIN);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occurred while creating a temp file for json file part in getTextFilePart",
                      e.getMessage());
        }
        return null;
    }

    /**
     * Get a text body part from a given text content and content transfer encoding.
     *
     * @param contentTransferEncoding Content transfer encoding value
     * @param message                 String that needs to be written to temp file
     * @return A ballerina struct that represent a body part
     */
    public static BObject getTextFilePartWithEncoding(String contentTransferEncoding, String message) {
        try {
            File file = getTemporaryFile("test", ".txt", message);
            BObject bodyPart = createEntityObject();
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(createMediaTypeObject(), bodyPart, TEXT_PLAIN);
            HeaderUtil.setHeaderToEntity(bodyPart, MimeConstants.CONTENT_TRANSFER_ENCODING, contentTransferEncoding);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occurred while creating a temp file for json file part in getTextFilePart",
                      e.getMessage());
        }
        return null;
    }

    /**
     * Get a json body part from a given json content.
     *
     * @return A ballerina struct that represent a body part
     */
    public static BObject getJsonBodyPart() {
        String key = "bodyPart";
        String value = "jsonPart";
        String jsonContent = "{\"" + key + "\":\"" + value + "\"}";
        BObject bodyPart = createEntityObject();
        EntityWrapper byteChannel = EntityBodyHandler.getEntityWrapper(jsonContent);
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, byteChannel);
        MimeUtil.setContentType(createMediaTypeObject(), bodyPart, APPLICATION_JSON);
        return bodyPart;
    }

    /**
     * Get a json body part as a file upload.
     *
     * @return A body part with json content in a file
     */
    public static BObject getJsonFilePart() {
        try {
            File file = getTemporaryFile("test", ".json", "{'name':'wso2'}");
            BObject bodyPart = createEntityObject();
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(createMediaTypeObject(), bodyPart, APPLICATION_JSON);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occurred while creating a temp file for json file part in getJsonFilePart",
                      e.getMessage());
        }
        return null;
    }

    /**
     * Get a xml body part from a given xml content.
     *
     * @return A ballerina struct that represent a body part
     */
    public static BObject getXmlBodyPart() {
        BXML xmlNode = XMLFactory.parse("<name>Ballerina</name>");
        BObject bodyPart = createEntityObject();
        EntityBodyChannel byteChannel = new EntityBodyChannel(new ByteArrayInputStream(
                xmlNode.stringValue(null).getBytes(StandardCharsets.UTF_8)));
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, new EntityWrapper(byteChannel));
        MimeUtil.setContentType(createMediaTypeObject(), bodyPart, APPLICATION_XML);
        return bodyPart;
    }

    /**
     * Get a xml body part as a file upload.
     *
     * @return A body part with xml content in a file
     */
    public static BObject getXmlFilePart() {
        try {
            File file = getTemporaryFile("test", ".xml", "<name>Ballerina xml file part</name>");
            BObject bodyPart = createEntityObject();
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(createMediaTypeObject(), bodyPart, APPLICATION_XML);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occurred while creating a temp file for xml file part in getXmlFilePart",
                      e.getMessage());
        }
        return null;
    }

    /**
     * Get a binary body part from a given blob content.
     *
     * @return A ballerina struct that represent a body part
     */
    public static BObject getBinaryBodyPart() {
        BObject bodyPart = createEntityObject();
        EntityWrapper byteChannel = EntityBodyHandler.getEntityWrapper("Ballerina binary part");
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, byteChannel);
        MimeUtil.setContentType(createMediaTypeObject(), bodyPart, OCTET_STREAM);
        return bodyPart;
    }

    /**
     * Get a binary body part as a file upload.
     *
     * @return A body part with blob content in a file
     */
    public static BObject getBinaryFilePart() {
        try {
            File file = getTemporaryFile("test", ".tmp", "Ballerina binary file part");
            BObject bodyPart = createEntityObject();
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(createMediaTypeObject(), bodyPart, OCTET_STREAM);
            return bodyPart;
        } catch (IOException e) {
            LOG.error("Error occurred while creating a temp file for binary file part in getBinaryFilePart",
                      e.getMessage());
        }
        return null;
    }

    /**
     * Get a multipart entity with four different body parts included in it.
     *
     * @return A ballerina entity with four body parts in it
     */
    public static BObject getMultipartEntity() {
        BObject multipartEntity = createEntityObject();
        ArrayList<BObject> bodyParts = getMultipleBodyParts();
        multipartEntity.addNativeData(BODY_PARTS, Util.getArrayOfBodyParts(bodyParts));
        return multipartEntity;
    }

    /**
     * Get a multipart entity with four other multipart entities, each containing four different other body parts.
     *
     * @return A nested multipart entity
     */
    public static BObject getNestedMultipartEntity() {
        BObject nestedMultipartEntity = createEntityObject();
        ArrayList<BObject> bodyParts = getEmptyBodyPartList();
        for (BObject bodyPart : bodyParts) {
            MimeUtil.setContentType(createMediaTypeObject(), bodyPart, MULTIPART_MIXED);
            bodyPart.addNativeData(BODY_PARTS, Util.getArrayOfBodyParts(getMultipleBodyParts()));
        }
        nestedMultipartEntity.addNativeData(BODY_PARTS, Util.getArrayOfBodyParts(bodyParts));
        return nestedMultipartEntity;
    }

    /**
     * Get a list of four different body parts.
     *
     * @return A list of different body parts
     */
    private static ArrayList<BObject> getMultipleBodyParts() {
        ArrayList<BObject> bodyParts = new ArrayList<>();
        bodyParts.add(getJsonBodyPart());
        bodyParts.add(getXmlFilePart());
        bodyParts.add(getTextBodyPart());
        bodyParts.add(getBinaryFilePart());
        return bodyParts;
    }

    /**
     * Get an empty body part list.
     *
     * @return A list of empty body parts
     */
    private static ArrayList<BObject> getEmptyBodyPartList() {
        ArrayList<BObject> bodyParts = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            bodyParts.add(createEntityObject());
        }
        return bodyParts;
    }

    public static BObject createEntityObject() {
        return ValueCreator.createObjectValue(PROTOCOL_MIME_PKG_ID, ENTITY);
    }


    public static BObject createMediaTypeObject() {
        return ValueCreator.createObjectValue(PROTOCOL_MIME_PKG_ID, MEDIA_TYPE);
    }

    public static BObject getContentDispositionStruct() {
        return ValueCreator.createObjectValue(PROTOCOL_MIME_PKG_ID, CONTENT_DISPOSITION_STRUCT);
    }

    //@NotNull
    static File getTemporaryFile(String fileName, String fileType, String valueTobeWritten) throws IOException {
        File file = File.createTempFile(fileName, fileType);
        file.deleteOnExit();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        bufferedWriter.write(valueTobeWritten);
        bufferedWriter.close();
        return file;
    }

    /**
     * Validate that the decoded body part content matches with the encoded content.
     *
     * @param mimeParts List of decoded body parts
     * @param bodyPart  Ballerina body part
     * @throws IOException When an exception occurs during binary data decoding
     */
    public static void validateBodyPartContent(List<MIMEPart> mimeParts, BObject bodyPart)
            throws IOException {
        EntityBodyHandler.populateBodyContent(bodyPart, mimeParts.get(0));
        Object jsonData = EntityBodyHandler.constructJsonDataSource(bodyPart);
        Assert.assertNotNull(jsonData);
        Assert.assertEquals(StringUtils.getJsonString(jsonData), "{\"" + "bodyPart" + "\":\"" + "jsonPart" + "\"}");

        EntityBodyHandler.populateBodyContent(bodyPart, mimeParts.get(1));
        BXML xmlData = EntityBodyHandler.constructXmlDataSource(bodyPart);
        Assert.assertNotNull(xmlData);
        Assert.assertEquals(xmlData.stringValue(null), "<name>Ballerina xml file part</name>");

        EntityBodyHandler.populateBodyContent(bodyPart, mimeParts.get(2));
        BString textData = EntityBodyHandler.constructStringDataSource(bodyPart);
        Assert.assertNotNull(textData);
        Assert.assertEquals(textData.getValue(), "Ballerina text body part");

        EntityBodyHandler.populateBodyContent(bodyPart, mimeParts.get(3));
        BArray blobDataSource = EntityBodyHandler.constructBlobDataSource(bodyPart);
        Assert.assertNotNull(blobDataSource);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        blobDataSource.serialize(outStream);
        Assert.assertEquals(new String(outStream.toByteArray(), StandardCharsets.UTF_8), "Ballerina binary file part");
    }

    static void verifyMimeError(BValue returnValue, String errMsg) {
        Assert.assertEquals(((BError) returnValue).getMessage(), errMsg);
    }
}
