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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityWrapper;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeConstants;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
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
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_MIXED;
import static org.ballerinalang.mime.util.MimeConstants.OCTET_STREAM;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_PACKAGE_IO;
import static org.ballerinalang.mime.util.MimeConstants.READABLE_BYTE_CHANNEL_STRUCT;
import static org.ballerinalang.mime.util.MimeConstants.TEXT_PLAIN;

/**
 * Contains utility functions used by mime test cases.
 *
 * @since 0.990.3
 */
public class Util {
    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    private static final String PACKAGE_MIME = MimeConstants.PROTOCOL_PACKAGE_MIME;
    private static final String ENTITY_STRUCT = MimeConstants.ENTITY;
    private static final String MEDIA_TYPE_STRUCT = MEDIA_TYPE;

    /**
     * From a given list of body parts get a ballerina value array.
     *
     * @param bodyParts List of body parts
     * @return BValueArray representing an array of entities
     */
    public static BValueArray getArrayOfBodyParts(ArrayList<BMap<String, BValue>> bodyParts) {
        BStructureType typeOfBodyPart = (BStructureType) bodyParts.get(0).getType();
        BMap<String, BValue>[] result = bodyParts.toArray(new BMap[bodyParts.size()]);
        return new BValueArray(result, new BArrayType(typeOfBodyPart));
    }

    /**
     * Get a text body part from a given text content.
     *
     * @param result Result of ballerina file compilation
     * @return A ballerina struct that represent a body part
     */
    public static BMap<String, BValue> getTextBodyPart(CompileResult result) {
        String textPayload = "Ballerina text body part";
        BMap<String, BValue> bodyPart = getEntityStruct(result);
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(textPayload));
        MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, TEXT_PLAIN);
        return bodyPart;
    }

    /**
     * Get a text body part as a file upload.
     *
     * @param result Result of ballerina file compilation
     * @return A body part with text content in a file
     */
    public static BMap<String, BValue> getTextFilePart(CompileResult result) {
        try {
            File file = getTemporaryFile("test", ".txt", "Ballerina text as a file part");
            BMap<String, BValue> bodyPart = getEntityStruct(result);
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, TEXT_PLAIN);
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
     * @param result                  Result of ballerina file compilation
     * @return A ballerina struct that represent a body part
     */
    public static BMap<String, BValue> getTextFilePartWithEncoding(String contentTransferEncoding, String message,
                                                            CompileResult result) {
        try {
            File file = getTemporaryFile("test", ".txt", message);
            BMap<String, BValue> bodyPart = getEntityStruct(result);
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, TEXT_PLAIN);
            HeaderUtil.setHeaderToEntity(bodyPart, HttpHeaderNames.CONTENT_TRANSFER_ENCODING.toString(),
                    contentTransferEncoding);
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
     * @param result Result of ballerina file compilation
     * @return A ballerina struct that represent a body part
     */
    public static BMap<String, BValue> getJsonBodyPart(CompileResult result) {
        String key = "bodyPart";
        String value = "jsonPart";
        String jsonContent = "{\"" + key + "\":\"" + value + "\"}";
        BMap<String, BValue> bodyPart = getEntityStruct(result);
        EntityWrapper byteChannel = EntityBodyHandler.getEntityWrapper(jsonContent);
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, byteChannel);
        MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_JSON);
        return bodyPart;
    }

    /**
     * Get a json body part as a file upload.
     *
     * @param result Result of ballerina file compilation
     * @return A body part with json content in a file
     */
    public static BMap<String, BValue> getJsonFilePart(CompileResult result) {
        try {
            File file = getTemporaryFile("test", ".json", "{'name':'wso2'}");
            BMap<String, BValue> bodyPart = getEntityStruct(result);
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_JSON);
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
     * @param result Result of ballerina file compilation
     * @return A ballerina struct that represent a body part
     */
    public static BMap<String, BValue> getXmlBodyPart(CompileResult result) {
        BXMLItem xmlContent = new BXMLItem("<name>Ballerina</name>");
        BMap<String, BValue> bodyPart = getEntityStruct(result);
        EntityBodyChannel byteChannel = new EntityBodyChannel(new ByteArrayInputStream(
                xmlContent.stringValue().getBytes(StandardCharsets.UTF_8)));
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, new EntityWrapper(byteChannel));
        MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_XML);
        return bodyPart;
    }

    /**
     * Get a xml body part as a file upload.
     *
     * @param result Result of ballerina file compilation
     * @return A body part with xml content in a file
     */
    public static BMap<String, BValue> getXmlFilePart(CompileResult result) {
        try {
            File file = getTemporaryFile("test", ".xml", "<name>Ballerina xml file part</name>");
            BMap<String, BValue> bodyPart = getEntityStruct(result);
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_XML);
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
     * @param result Result of ballerina file compilation
     * @return A ballerina struct that represent a body part
     */
    public static BMap<String, BValue> getBinaryBodyPart(CompileResult result) {
        BMap<String, BValue> bodyPart = getEntityStruct(result);
        EntityWrapper byteChannel = EntityBodyHandler.getEntityWrapper("Ballerina binary part");
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, byteChannel);
        MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, OCTET_STREAM);
        return bodyPart;
    }

    /**
     * Get a binary body part as a file upload.
     *
     * @param result Result of ballerina file compilation
     * @return A body part with blob content in a file
     */
    public static BMap<String, BValue> getBinaryFilePart(CompileResult result) {
        try {
            File file = getTemporaryFile("test", ".tmp", "Ballerina binary file part");
            BMap<String, BValue> bodyPart = getEntityStruct(result);
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, OCTET_STREAM);
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
     * @param result Result of ballerina file compilation
     * @return A ballerina entity with four body parts in it
     */
    public static BMap<String, BValue> getMultipartEntity(CompileResult result) {
        BMap<String, BValue> multipartEntity = getEntityStruct(result);
        ArrayList<BMap<String, BValue>> bodyParts = getMultipleBodyParts(result);
        multipartEntity.addNativeData(BODY_PARTS, Util.getArrayOfBodyParts(bodyParts));
        return multipartEntity;
    }

    /**
     * Get a multipart entity with four other multipart entities, each containing four different other body parts.
     *
     * @param result Result of ballerina file compilation
     * @return A nested multipart entity
     */
    public static BMap<String, BValue> getNestedMultipartEntity(CompileResult result) {
        BMap<String, BValue> nestedMultipartEntity = getEntityStruct(result);
        ArrayList<BMap<String, BValue>> bodyParts = getEmptyBodyPartList(result);
        for (BMap<String, BValue> bodyPart : bodyParts) {
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, MULTIPART_MIXED);
            bodyPart.addNativeData(BODY_PARTS, Util.getArrayOfBodyParts(getMultipleBodyParts(result)));
        }
        nestedMultipartEntity.addNativeData(BODY_PARTS, Util.getArrayOfBodyParts(bodyParts));
        return nestedMultipartEntity;
    }

    /**
     * Get a list of four different body parts.
     *
     * @param result Result of ballerina file compilation
     * @return A list of different body parts
     */
    private static ArrayList<BMap<String, BValue>> getMultipleBodyParts(CompileResult result) {
        ArrayList<BMap<String, BValue>> bodyParts = new ArrayList<>();
        bodyParts.add(getJsonBodyPart(result));
        bodyParts.add(getXmlFilePart(result));
        bodyParts.add(getTextBodyPart(result));
        bodyParts.add(getBinaryFilePart(result));
        return bodyParts;
    }

    /**
     * Get an empty body part list.
     *
     * @param result Result of ballerina file compilation
     * @return A list of empty body parts
     */
    private static ArrayList<BMap<String, BValue>> getEmptyBodyPartList(CompileResult result) {
        ArrayList<BMap<String, BValue>> bodyParts = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            bodyParts.add(getEntityStruct(result));
        }
        return bodyParts;
    }

    public static BMap<String, BValue> getEntityStruct(CompileResult result) {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), PACKAGE_MIME, ENTITY_STRUCT);
    }

    public static BMap<String, BValue> getMediaTypeStruct(CompileResult result) {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), PACKAGE_MIME,
                MEDIA_TYPE_STRUCT);
    }

    public static BMap<String, BValue> getContentDispositionStruct(CompileResult result) {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), PACKAGE_MIME, CONTENT_DISPOSITION_STRUCT);
    }

    public static BMap<String, BValue> getByteChannelStruct(CompileResult result) {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), PROTOCOL_PACKAGE_IO, READABLE_BYTE_CHANNEL_STRUCT);
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
    public static void validateBodyPartContent(List<MIMEPart> mimeParts, BMap<String, BValue> bodyPart)
            throws IOException {
        EntityBodyHandler.populateBodyContent(bodyPart, mimeParts.get(0));
        BValue jsonData = EntityBodyHandler.constructJsonDataSource(bodyPart);
        Assert.assertNotNull(jsonData);
        Assert.assertEquals(jsonData.stringValue(), "{\"" + "bodyPart" + "\":\"" + "jsonPart" + "\"}");

        EntityBodyHandler.populateBodyContent(bodyPart, mimeParts.get(1));
        BXML xmlData = EntityBodyHandler.constructXmlDataSource(bodyPart);
        Assert.assertNotNull(xmlData);
        Assert.assertEquals(xmlData.stringValue(), "<name>Ballerina xml file part</name>");

        EntityBodyHandler.populateBodyContent(bodyPart, mimeParts.get(2));
        BString textData = EntityBodyHandler.constructStringDataSource(bodyPart);
        Assert.assertNotNull(textData);
        Assert.assertEquals(textData.stringValue(), "Ballerina text body part");

        EntityBodyHandler.populateBodyContent(bodyPart, mimeParts.get(3));
        BValueArray blobDataSource = EntityBodyHandler.constructBlobDataSource(bodyPart);
        Assert.assertNotNull(blobDataSource);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        blobDataSource.serialize(outStream);
        Assert.assertEquals(new String(outStream.toByteArray(), StandardCharsets.UTF_8), "Ballerina binary file part");
    }

    public static void verifyMimeError(BValue returnValue, String errMsg) {
        Assert.assertEquals(((BError) returnValue).getReason(), MimeConstants.MIME_ERROR_CODE);
        Assert.assertEquals(((BMap) ((BError) returnValue).getDetails()).get(MimeConstants.MIME_ERROR_MESSAGE)
                .stringValue(), errMsg);
    }
}
