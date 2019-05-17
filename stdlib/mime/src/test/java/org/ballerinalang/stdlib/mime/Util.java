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

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityWrapper;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeConstants;
import org.ballerinalang.mime.util.MimeEntityWrapper;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.ballerinalang.util.exceptions.BallerinaException;
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;

import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_XML;
import static org.ballerinalang.mime.util.MimeConstants.ASSIGNMENT;
import static org.ballerinalang.mime.util.MimeConstants.BODY_PARTS;
import static org.ballerinalang.mime.util.MimeConstants.CHARSET;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_FILENAME_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_FILE_NAME;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_NAME;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_NAME_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_PARA_MAP_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_STRUCT;
import static org.ballerinalang.mime.util.MimeConstants.DEFAULT_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.DEFAULT_SUB_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.DISPOSITION_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.MimeConstants.FORM_DATA_PARAM;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.MESSAGE_DATA_SOURCE;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_FORM_DATA;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_MIXED;
import static org.ballerinalang.mime.util.MimeConstants.OCTET_STREAM;
import static org.ballerinalang.mime.util.MimeConstants.PARAMETER_MAP_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.PRIMARY_TYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_PACKAGE_IO;
import static org.ballerinalang.mime.util.MimeConstants.READABLE_BYTE_CHANNEL_STRUCT;
import static org.ballerinalang.mime.util.MimeConstants.SEMICOLON;
import static org.ballerinalang.mime.util.MimeConstants.SIZE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.SUBTYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.SUFFIX_ATTACHMENT;
import static org.ballerinalang.mime.util.MimeConstants.SUFFIX_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.TEXT_PLAIN;
import static org.ballerinalang.mime.util.MimeUtil.includeQuotes;
import static org.ballerinalang.mime.util.MimeUtil.isNotNullAndEmpty;

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
        setContentType(getMediaTypeStruct(result), bodyPart, TEXT_PLAIN);
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
            setContentType(getMediaTypeStruct(result), bodyPart, TEXT_PLAIN);
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
            setContentType(getMediaTypeStruct(result), bodyPart, TEXT_PLAIN);
            setHeaderToEntity(bodyPart, HttpHeaderNames.CONTENT_TRANSFER_ENCODING.toString(),
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
        setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_JSON);
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
            setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_JSON);
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
        setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_XML);
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
            setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_XML);
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
        setContentType(getMediaTypeStruct(result), bodyPart, OCTET_STREAM);
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
            setContentType(getMediaTypeStruct(result), bodyPart, OCTET_STREAM);
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
            setContentType(getMediaTypeStruct(result), bodyPart, MULTIPART_MIXED);
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
        populateBodyContent(bodyPart, mimeParts.get(0));
        BValue jsonData = constructJsonDataSource(bodyPart);
        Assert.assertNotNull(jsonData);
        Assert.assertEquals(jsonData.stringValue(), "{\"" + "bodyPart" + "\":\"" + "jsonPart" + "\"}");

        populateBodyContent(bodyPart, mimeParts.get(1));
        BXML xmlData = constructXmlDataSource(bodyPart);
        Assert.assertNotNull(xmlData);
        Assert.assertEquals(xmlData.stringValue(), "<name>Ballerina xml file part</name>");

        populateBodyContent(bodyPart, mimeParts.get(2));
        BString textData = constructStringDataSource(bodyPart);
        Assert.assertNotNull(textData);
        Assert.assertEquals(textData.stringValue(), "Ballerina text body part");

        populateBodyContent(bodyPart, mimeParts.get(3));
        BValueArray blobDataSource = constructBlobDataSource(bodyPart);
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

    //TODO Remove all following util methods after migration : implemented using bvm values/types
    /**
     * Construct 'MediaType' struct with the given Content-Type and set it into the given 'Entity'.
     *
     * @param mediaType    Represent 'MediaType' struct
     * @param entityStruct Represent 'Entity' struct
     * @param contentType  Content-Type value in string
     */
    public static void setContentType(BMap<String, BValue> mediaType, BMap<String, BValue> entityStruct,
                                      String contentType) {
        BMap<String, BValue> mimeType = parseMediaType(mediaType, contentType);
        if (contentType == null) {
            mimeType.put(PRIMARY_TYPE_FIELD, new BString(DEFAULT_PRIMARY_TYPE));
            mimeType.put(SUBTYPE_FIELD, new BString(DEFAULT_SUB_TYPE));
        }
        entityStruct.put(MEDIA_TYPE_FIELD, mimeType);
    }

    /**
     * Parse 'MediaType' struct with the given Content-Type.
     *
     * @param mediaType   Represent 'MediaType' struct
     * @param contentType Content-Type value in string
     * @return 'MediaType' struct populated with values
     */
    private static BMap<String, BValue> parseMediaType(BMap<String, BValue> mediaType, String contentType) {
        try {
            BMap<String, BValue> parameterMap = new BMap<>();
            BString suffix, primaryType, subType;

            if (contentType != null) {
                MimeType mimeType = new MimeType(contentType);
                primaryType = new BString(mimeType.getPrimaryType());

                String subTypeStr = mimeType.getSubType();
                subType = new BString(subTypeStr);
                if (subTypeStr != null && subTypeStr.contains(SUFFIX_ATTACHMENT)) {
                    suffix = new BString(
                            subTypeStr.substring(subTypeStr.lastIndexOf(SUFFIX_ATTACHMENT) + 1));
                } else {
                    suffix = BTypes.typeString.getZeroValue();
                }

                MimeTypeParameterList parameterList = mimeType.getParameters();
                Enumeration keys = parameterList.getNames();

                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    String value = parameterList.get(key);
                    parameterMap.put(key, new BString(value));
                }
            } else {
                primaryType = suffix = subType = BTypes.typeString.getZeroValue();
            }

            mediaType.put(PRIMARY_TYPE_FIELD, primaryType);
            mediaType.put(SUBTYPE_FIELD, subType);
            mediaType.put(SUFFIX_FIELD, suffix);
            mediaType.put(PARAMETER_MAP_FIELD, parameterMap);
        } catch (MimeTypeParseException e) {
            throw new BallerinaException("Error while parsing Content-Type value: " + e.getMessage());
        }
        return mediaType;
    }

    private static void setHeaderToEntity(BMap<String, BValue> entity, String key, String value) {
        HttpHeaders httpHeaders;
        if (entity.getNativeData(ENTITY_HEADERS) != null) {
            httpHeaders = (HttpHeaders) entity.getNativeData(ENTITY_HEADERS);

        } else {
            httpHeaders = new DefaultHttpHeaders();
            entity.addNativeData(ENTITY_HEADERS, httpHeaders);
        }
        httpHeaders.set(key, value);
    }

    /**
     * Populate ballerina body parts with actual body content. Based on the memory threshhold body part's inputstream
     * can either come from memory or from a temp file maintained by mimepull library.
     *
     * @param bodyPart Represent ballerina body part
     * @param mimePart Represent decoded mime part
     */
    private static void populateBodyContent(BMap<String, BValue> bodyPart, MIMEPart mimePart) {
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, new MimeEntityWrapper(new EntityBodyChannel(mimePart.readOnce()),
                                                                          mimePart));
    }

    /**
     * Construct BlobDataSource from the underneath byte channel which is associated with the entity object.
     *
     * @param entityObj Represent an entity object
     * @return Data source for binary data which is kept in memory
     * @throws IOException In case an error occurred while creating blob data source
     */
    private static BValueArray constructBlobDataSource(BMap<String, BValue> entityObj) throws IOException {
        Channel byteChannel = getByteChannel(entityObj);
        if (byteChannel == null) {
            return new BValueArray(new byte[0]);
        }
        BValueArray byteData = constructBlobDataSource(byteChannel.getInputStream());
        byteChannel.close();
        return byteData;
    }

    /**
     * Construct BlobDataSource from the underneath byte channel which is associated with the entity object.
     *
     * @param inputStream Represent the input stream
     * @return Data source for binary data which is kept in memory
     */
    private static BValueArray constructBlobDataSource(InputStream inputStream) {
        byte[] byteData;
        try {
            byteData = MimeUtil.getByteArray(inputStream);
        } catch (IOException ex) {
            throw new BallerinaException("Error occurred while reading input stream :" + ex.getMessage());
        }
        return new BValueArray(byteData);
    }

    /**
     * Construct JsonDataSource from the underneath byte channel which is associated with the entity object.
     *
     * @param entityObj Represent an entity object
     * @return BJSON data source which is kept in memory
     */
    public static BRefType<?> constructJsonDataSource(BMap<String, BValue> entityObj) {
        try {
            Channel byteChannel = getByteChannel(entityObj);
            if (byteChannel == null) {
                return null;
            }
            BRefType<?> jsonData = constructJsonDataSource(entityObj, byteChannel.getInputStream());
            byteChannel.close();
            return jsonData;
        } catch (IOException e) {
            throw new BallerinaIOException("Error occurred while closing connection", e);
        }
    }

    /**
     * Construct JsonDataSource from the given input stream.
     *
     * @param entity      Represent an entity object
     * @param inputStream Represent the input stream
     * @return BJSON data source which is kept in memory
     */
    private static BRefType<?> constructJsonDataSource(BMap<String, BValue> entity, InputStream inputStream) {
        BRefType<?> jsonData;
        String contentTypeValue = getHeaderValue(entity, HttpHeaderNames.CONTENT_TYPE.toString());
        if (isNotNullAndEmpty(contentTypeValue)) {
            String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
            if (isNotNullAndEmpty(charsetValue)) {
                jsonData = JsonParser.parse(inputStream, charsetValue);
            } else {
                jsonData = JsonParser.parse(inputStream);
            }
        } else {
            jsonData = JsonParser.parse(inputStream);
        }
        return jsonData;
    }

    /**
     * Construct XML data source from the underneath byte channel which is associated with the entity object.
     *
     * @param entityObj Represent an entity object
     * @return BXML data source which is kept in memory
     */
    private static BXML constructXmlDataSource(BMap<String, BValue> entityObj) {
        try {
            Channel byteChannel = getByteChannel(entityObj);
            if (byteChannel == null) {
                throw new BallerinaIOException("Empty xml payload");
            }
            BXML xmlContent = constructXmlDataSource(entityObj, byteChannel.getInputStream());
            byteChannel.close();
            return xmlContent;
        } catch (IOException e) {
            throw new BallerinaIOException("Error occurred while closing the channel", e);
        }
    }

    /**
     * Construct XML data source from the given input stream.
     *
     * @param entityObj Represent an entity object
     * @param inputStream  Represent the input stream
     * @return BXML data source which is kept in memory
     */
    private static BXML constructXmlDataSource(BMap<String, BValue> entityObj, InputStream inputStream) {
        BXML xmlContent;
        String contentTypeValue = getHeaderValue(entityObj, HttpHeaderNames.CONTENT_TYPE.toString());
        if (isNotNullAndEmpty(contentTypeValue)) {
            String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
            if (isNotNullAndEmpty(charsetValue)) {
                xmlContent = XMLUtils.parse(inputStream, charsetValue);
            } else {
                xmlContent = XMLUtils.parse(inputStream);
            }
        } else {
            xmlContent = XMLUtils.parse(inputStream);
        }
        return xmlContent;
    }

    /**
     * Construct StringDataSource from the underneath byte channel which is associated with the entity object.
     *
     * @param entityObj Represent an entity object
     * @return StringDataSource which represent the entity body which is kept in memory
     */
    private static BString constructStringDataSource(BMap<String, BValue> entityObj) {
        try {
            Channel byteChannel = getByteChannel(entityObj);
            if (byteChannel == null) {
                throw new BallerinaIOException("String payload is null");
            }
            BString textContent = constructStringDataSource(entityObj, byteChannel.getInputStream());
            byteChannel.close();
            return textContent;
        } catch (IOException e) {
            throw new BallerinaIOException("Error occurred while closing the channel", e);
        }
    }

    /**
     * Construct StringDataSource from the given input stream.
     *
     * @param entity      Represent an entity object
     * @param inputStream Represent the input stream
     * @return StringDataSource which represent the entity body which is kept in memory
     */
    private static BString constructStringDataSource(BMap<String, BValue> entity, InputStream inputStream) {
        String textContent;
        String contentTypeValue = getHeaderValue(entity, HttpHeaderNames.CONTENT_TYPE.toString());
        if (isNotNullAndEmpty(contentTypeValue)) {
            String charsetValue = MimeUtil.getContentTypeParamValue(contentTypeValue, CHARSET);
            if (isNotNullAndEmpty(charsetValue)) {
                textContent = StringUtils.getStringFromInputStream(inputStream, charsetValue);
            } else {
                textContent = StringUtils.getStringFromInputStream(inputStream);
            }
        } else {
            textContent = StringUtils.getStringFromInputStream(inputStream);
        }
        return new BString(textContent);
    }

    public static Channel getByteChannel(BMap<String, BValue> entityObj) {
        return entityObj.getNativeData(ENTITY_BYTE_CHANNEL) != null ? (Channel) entityObj.getNativeData
                (ENTITY_BYTE_CHANNEL) : null;
    }

    /**
     * Extract the header value from a body part for a given header name.
     *
     * @param bodyPart   Represent a ballerina body part
     * @param headerName Represent an http header name
     * @return a header value for the given header name
     */
    public static String getHeaderValue(BMap<String, BValue> bodyPart, String headerName) {
        if (bodyPart.getNativeData(ENTITY_HEADERS) != null) {
            HttpHeaders httpHeaders = (HttpHeaders) bodyPart.getNativeData(ENTITY_HEADERS);
            return httpHeaders.get(headerName);
        }
        return null;
    }

    /**
     * Populate ContentDisposition struct and set it to body part.
     *
     * @param contentDisposition                 Represent the ContentDisposition struct that needs to be filled
     * @param bodyPart                           Represent a body part
     * @param contentDispositionHeaderWithParams Represent Content-Disposition header value with parameters
     */
    public static void setContentDisposition(BMap<String, BValue> contentDisposition, BMap<String, BValue> bodyPart,
                                             String contentDispositionHeaderWithParams) {
        populateContentDispositionObject(contentDisposition, contentDispositionHeaderWithParams);
        bodyPart.put(CONTENT_DISPOSITION_FIELD, contentDisposition);
    }

    public static void populateContentDispositionObject(BMap<String, BValue> contentDisposition,
                                                        String contentDispositionHeaderWithParams) {
        String dispositionValue;
        if (isNotNullAndEmpty(contentDispositionHeaderWithParams)) {
            if (contentDispositionHeaderWithParams.contains(SEMICOLON)) {
                dispositionValue = HeaderUtil.getHeaderValue(contentDispositionHeaderWithParams);
            } else {
                dispositionValue = contentDispositionHeaderWithParams;
            }
            contentDisposition.put(DISPOSITION_FIELD, new BString(dispositionValue));
            BMap<String, BValue> paramMap = getParamMap(contentDispositionHeaderWithParams);
            if (paramMap != null) {
                for (String key : paramMap.keys()) {
                    BString paramValue = (BString) paramMap.get(key);
                    switch (key) {
                        case CONTENT_DISPOSITION_FILE_NAME:
                            contentDisposition.put(CONTENT_DISPOSITION_FILENAME_FIELD,
                                                   new BString(MimeUtil.stripQuotes(paramValue.toString())));
                            break;
                        case CONTENT_DISPOSITION_NAME:
                            contentDisposition.put(CONTENT_DISPOSITION_NAME_FIELD,
                                                   new BString(MimeUtil.stripQuotes(paramValue.toString())));
                            break;
                        default:
                    }
                }
                paramMap.remove(CONTENT_DISPOSITION_FILE_NAME);
                paramMap.remove(CONTENT_DISPOSITION_NAME);
            }
            contentDisposition.put(CONTENT_DISPOSITION_PARA_MAP_FIELD, paramMap);
        }
    }

    /**
     * Given a header value, get it's parameters.
     *
     * @param headerValue Header value as a string
     * @return Parameter map
     */
    public static BMap<String, BValue> getParamMap(String headerValue) {
        BMap<String, BValue> paramMap = null;
        if (headerValue.contains(SEMICOLON)) {
            extractValue(headerValue);
            List<String> paramList = Arrays.stream(headerValue.substring(headerValue.indexOf(SEMICOLON) + 1)
                                                           .split(SEMICOLON)).map(String::trim).collect(Collectors.toList());
            paramMap = validateParams(paramList) ? getHeaderParamBMap(paramList) : null;
        }
        return paramMap;
    }

    /**
     * Extract header value.
     *
     * @param headerValue Header value with parameters as a string
     * @return Header value without parameters
     */
    private static String extractValue(String headerValue) {
        String value = headerValue.substring(0, headerValue.indexOf(SEMICOLON)).trim();
        if (value.isEmpty()) {
            throw new BallerinaException("invalid header value: " + headerValue);
        }
        return value;
    }

    private static boolean validateParams(List<String> paramList) {
        //validate header values which ends with semicolon without params
        return !(paramList.size() == 1 && paramList.get(0).isEmpty());
    }

    /**
     * Given a list of string parameter list, create ballerina specific header parameter map.
     *
     * @param paramList List of parameters
     * @return Ballerina map
     */
    private static BMap<String, BValue> getHeaderParamBMap(List<String> paramList) {
        BMap<String, BValue> paramMap = new BMap<>();
        for (String param : paramList) {
            if (param.contains("=")) {
                String[] keyValuePair = param.split("=", 2);
                if (keyValuePair.length != 2 || keyValuePair[0].isEmpty() || keyValuePair[1].isEmpty()) {
                    throw new BallerinaException("invalid header parameter: " + param);
                }
                paramMap.put(keyValuePair[0].trim(), new BString(keyValuePair[1].trim()));
            } else {
                //handle when parameter value is optional
                paramMap.put(param.trim(), null);
            }
        }
        return paramMap;
    }

    /**
     * Given a ballerina entity, build the content-disposition header value from 'ContentDisposition' object.
     *
     * @param entity Represent an 'Entity'
     * @return content-type in 'primarytype/subtype; key=value;' format
     */
    public static String getContentDisposition(BMap<String, BValue> entity) {
        StringBuilder dispositionBuilder = new StringBuilder();
        if (entity.get(CONTENT_DISPOSITION_FIELD) != null) {
            BMap<String, BValue> contentDispositionStruct =
                    (BMap<String, BValue>) entity.get(CONTENT_DISPOSITION_FIELD);
            if (contentDispositionStruct != null) {
                BValue disposition = contentDispositionStruct.get(DISPOSITION_FIELD);
                if (disposition == null || disposition.stringValue().isEmpty()) {
                    String contentType = getBaseType(entity);
                    if (contentType != null && contentType.equals(MULTIPART_FORM_DATA)) {
                        dispositionBuilder.append(FORM_DATA_PARAM);
                    }
                } else {
                    dispositionBuilder.append(disposition);
                }
                if (!dispositionBuilder.toString().isEmpty()) {
                    dispositionBuilder = convertDispositionObjectToString(dispositionBuilder, contentDispositionStruct);
                }
            }
        }
        return dispositionBuilder.toString();
    }

    /**
     * Given a ballerina entity, get the content-type as a base type.
     *
     * @param entity Represent an 'Entity'
     * @return content-type in 'primarytype/subtype' format
     */
    public static String getBaseType(BMap<String, BValue> entity) {
        if (entity.get(MEDIA_TYPE_FIELD) != null) {
            BMap<String, BValue> mediaType = (BMap<String, BValue>) entity.get(MEDIA_TYPE_FIELD);
            if (mediaType != null) {
                return mediaType.get(PRIMARY_TYPE_FIELD).stringValue() + "/" +
                        mediaType.get(SUBTYPE_FIELD).stringValue();
            }
        }
        return null;
    }

    public static StringBuilder convertDispositionObjectToString(StringBuilder dispositionBuilder,
                                                                 BMap<String, BValue> contentDispositionStruct) {
        BValue nameBVal = contentDispositionStruct.get(CONTENT_DISPOSITION_NAME_FIELD);
        String name = nameBVal != null ? nameBVal.stringValue() : null;

        BValue fileNameBVal = contentDispositionStruct.get(CONTENT_DISPOSITION_FILENAME_FIELD);
        String fileName = fileNameBVal != null ? fileNameBVal.stringValue() : null;

        if (isNotNullAndEmpty(name)) {
            appendSemiColon(dispositionBuilder).append(CONTENT_DISPOSITION_NAME).append(ASSIGNMENT).append(
                    includeQuotes(name)).append(SEMICOLON);
        }
        if (isNotNullAndEmpty(fileName)) {
            appendSemiColon(dispositionBuilder).append(CONTENT_DISPOSITION_FILE_NAME).append(ASSIGNMENT)
                    .append(includeQuotes(fileName)).append(SEMICOLON);
        }
        if (contentDispositionStruct.get(CONTENT_DISPOSITION_PARA_MAP_FIELD) != null) {
            BMap map = (BMap) contentDispositionStruct.get(CONTENT_DISPOSITION_PARA_MAP_FIELD);
            appendHeaderParams(appendSemiColon(dispositionBuilder), map);
        }

        if (dispositionBuilder.toString().endsWith(SEMICOLON)) {
            dispositionBuilder.setLength(dispositionBuilder.length() - 1);
        }
        return dispositionBuilder;
    }

    private static StringBuilder appendSemiColon(StringBuilder disposition) {
        if (!disposition.toString().endsWith(SEMICOLON)) {
            disposition.append(SEMICOLON);
        }
        return disposition;
    }

    /**
     * Get the header value intact with parameters.
     *
     * @param headerValue Header value as a string
     * @param map         Represent a parameter map
     * @return Header value along with it's parameters as a string
     */
    public static String appendHeaderParams(StringBuilder headerValue, BMap map) {
        int index = 0;
        if (map != null && !map.isEmpty()) {
            String[] keys = (String[]) map.keys();
            if (keys.length != 0) {
                for (String key : keys) {
                    BString paramValue = (BString) map.get(key);
                    if (index == keys.length - 1) {
                        headerValue.append(key).append(ASSIGNMENT).append(paramValue.toString());
                    } else {
                        headerValue.append(key).append(ASSIGNMENT).append(paramValue.toString()).append(SEMICOLON);
                        index = index + 1;
                    }
                }
            }
        }
        return headerValue.toString();
    }

    /**
     * Get the message data source associated with a given entity.
     *
     * @param entityObj Represent a ballerina entity
     * @return MessageDataSource which represent the entity body in memory
     */
    public static BValue getMessageDataSource(BMap<String, BValue> entityObj) {
        return entityObj.getNativeData(MESSAGE_DATA_SOURCE) != null ? (BValue) entityObj.getNativeData
                (MESSAGE_DATA_SOURCE) : null;
    }

    /**
     * Populate given 'Entity' with it's body size.
     *
     * @param entityStruct Represent 'Entity'
     * @param length       Size of the entity body
     */
    public static void setContentLength(BMap<String, BValue> entityStruct, long length) {
        entityStruct.put(SIZE_FIELD, new BInteger(length));
    }
}
