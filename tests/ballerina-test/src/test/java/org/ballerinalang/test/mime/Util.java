/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.mime;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.EntityBody;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityWrapper;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.nativeimpl.io.channels.FileIOChannel;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.ballerinalang.mime.util.Constants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.Constants.APPLICATION_XML;
import static org.ballerinalang.mime.util.Constants.BYTE_CHANNEL_STRUCT;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_NAME;
import static org.ballerinalang.mime.util.Constants.CONTENT_DISPOSITION_STRUCT;
import static org.ballerinalang.mime.util.Constants.CONTENT_TRANSFER_ENCODING;
import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS_INDEX;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.MULTIPART_DATA_INDEX;
import static org.ballerinalang.mime.util.Constants.MULTIPART_ENCODER;
import static org.ballerinalang.mime.util.Constants.OCTET_STREAM;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_IO;
import static org.ballerinalang.mime.util.Constants.TEMP_FILE_EXTENSION;
import static org.ballerinalang.mime.util.Constants.TEMP_FILE_NAME;
import static org.ballerinalang.mime.util.Constants.TEXT_PLAIN;

/**
 * Contains utility functions used by mime test cases.
 *
 * @since 0.963.0
 */
public class Util {
    private static final Logger log = LoggerFactory.getLogger(Util.class);

    private static final String REQUEST_STRUCT = HttpConstants.IN_REQUEST;
    private static final String PROTOCOL_PACKAGE_HTTP = HttpConstants.PROTOCOL_PACKAGE_HTTP;
    private static final String PACKAGE_MIME = org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
    private static final String ENTITY_STRUCT = HttpConstants.ENTITY;
    private static final String MEDIA_TYPE_STRUCT = MEDIA_TYPE;
    private static final String CARBON_MESSAGE = "CarbonMessage";
    private static final String BALLERINA_REQUEST = "BallerinaRequest";
    private static final String MULTIPART_ENTITY = "MultipartEntity";

    private static HttpDataFactory dataFactory = null;

    /**
     * From a given list of body parts get a ballerina value array.
     *
     * @param bodyParts List of body parts
     * @return BRefValueArray representing an array of entities
     */
    static BRefValueArray getArrayOfBodyParts(ArrayList<BStruct> bodyParts) {
        BStructType typeOfBodyPart = bodyParts.get(0).getType();
        BStruct[] result = bodyParts.toArray(new BStruct[bodyParts.size()]);
        return new BRefValueArray(result, typeOfBodyPart);
    }

    /**
     * Get a text body part from a given text content.
     *
     * @return A ballerina struct that represent a body part
     */
    static BStruct getTextBodyPart(CompileResult result) {
        String textPayload = "Ballerina text body part";
        BStruct bodyPart = getEntityStruct(result);
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(textPayload));
        MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, TEXT_PLAIN);
        return bodyPart;
    }

    /**
     * Get a text body part as a file upload.
     *
     * @return a body part with text content in a file
     */
    static BStruct getTextFilePart(CompileResult result) {
        try {
            File file = File.createTempFile("test", ".txt");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("Ballerina text as a file part");
            bufferedWriter.close();
            BStruct bodyPart = getEntityStruct(result);
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, TEXT_PLAIN);
            return bodyPart;
        } catch (IOException e) {
            log.error("Error occurred while creating a temp file for json file part in getTextFilePart",
                    e.getMessage());
        }
        return null;
    }

    /**
     * Get a text body part from a given text content and content transfer encoding.
     *
     * @return A ballerina struct that represent a body part
     */
    static BStruct getTextFilePartWithEncoding(String contentTransferEncoding, String message, CompileResult result) {

        try {
            File file = File.createTempFile("test", ".txt");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(message);
            bufferedWriter.close();
            BStruct bodyPart = getEntityStruct(result);
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, TEXT_PLAIN);
            BMap<String, BValue> headerMap = new BMap<>();
            headerMap.put(CONTENT_TRANSFER_ENCODING, new BStringArray(new String[]{contentTransferEncoding}));
            bodyPart.setRefField(ENTITY_HEADERS_INDEX, headerMap);
            return bodyPart;
        } catch (IOException e) {
            log.error("Error occurred while creating a temp file for json file part in getTextFilePart",
                    e.getMessage());
        }
        return null;
    }

    /**
     * Get a json body part from a given json content.
     *
     * @return A ballerina struct that represent a body part
     */
    static BStruct getJsonBodyPart(CompileResult result) {
        String key = "bodyPart";
        String value = "jsonPart";
        String jsonContent = "{\"" + key + "\":\"" + value + "\"}";
        BStruct bodyPart = getEntityStruct(result);
        EntityWrapper byteChannel = EntityBodyHandler.getEntityWrapper(jsonContent);
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, byteChannel);
        MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_JSON);
        return bodyPart;
    }

    /**
     * Get a json body part as a file upload.
     *
     * @return a body part with json content in a file
     */
    static BStruct getJsonFilePart(CompileResult result) {
        try {
            File file = File.createTempFile("test", ".json");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("{'name':'wso2'}");
            bufferedWriter.close();
            BStruct bodyPart = getEntityStruct(result);
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_JSON);
            return bodyPart;
        } catch (IOException e) {
            log.error("Error occurred while creating a temp file for json file part in getJsonFilePart",
                    e.getMessage());
        }
        return null;
    }

    /**
     * Get a xml body part from a given xml content.
     *
     * @return A ballerina struct that represent a body part
     */
    static BStruct getXmlBodyPart(CompileResult result) {
        BXMLItem xmlContent = new BXMLItem("<name>Ballerina</name>");
        BStruct bodyPart = getEntityStruct(result);
        EntityBodyChannel byteChannel = new EntityBodyChannel(new ByteArrayInputStream(
                xmlContent.getMessageAsString().getBytes(StandardCharsets.UTF_8)));
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, new EntityWrapper(byteChannel));
        MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_XML);
        return bodyPart;
    }

    /**
     * Get a xml body part as a file upload.
     *
     * @return a body part with xml content in a file
     */
    static BStruct getXmlFilePart(CompileResult result) {
        try {
            File file = File.createTempFile("test", ".xml");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("<name>Ballerina xml file part</name>");
            bufferedWriter.close();
            BStruct bodyPart = getEntityStruct(result);
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, APPLICATION_XML);
            return bodyPart;
        } catch (IOException e) {
            log.error("Error occurred while creating a temp file for xml file part in getXmlFilePart",
                    e.getMessage());
        }
        return null;
    }

    /**
     * Get a binary body part from a given blob content.
     *
     * @return A ballerina struct that represent a body part
     */
    static BStruct getBinaryBodyPart(CompileResult result) {
        BStruct bodyPart = getEntityStruct(result);
        EntityWrapper byteChannel = EntityBodyHandler.getEntityWrapper("Ballerina binary part");
        bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, byteChannel);
        MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, OCTET_STREAM);
        return bodyPart;
    }

    /**
     * Get a binary body part as a file upload.
     *
     * @return a body part with blob content in a file
     */
    static BStruct getBinaryFilePart(CompileResult result) {
        try {
            File file = File.createTempFile("test", ".tmp");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("Ballerina binary file part");
            bufferedWriter.close();
            BStruct bodyPart = getEntityStruct(result);
            bodyPart.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getByteChannelForTempFile(
                    file.getAbsolutePath()));
            MimeUtil.setContentType(getMediaTypeStruct(result), bodyPart, OCTET_STREAM);
            return bodyPart;
        } catch (IOException e) {
            log.error("Error occurred while creating a temp file for binary file part in getBinaryFilePart",
                    e.getMessage());
        }
        return null;
    }

    /**
     * Create prerequisite messages that are needed to proceed with the test cases.
     *
     * @param path Represent path to the resource
     * @return A map of relevant messages
     */
    static Map<String, Object> createPrerequisiteMessages(String path, String topLevelContentType,
                                                          CompileResult result) {
        Map<String, Object> messageMap = new HashMap<>();
        BStruct request = getRequestStruct(result);
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessageForMultiparts(path, HttpConstants.HTTP_METHOD_POST);
        HttpUtil.addCarbonMsg(request, cMsg);
        BStruct entity = getEntityStruct(result);
        MimeUtil.setContentType(getMediaTypeStruct(result), entity, topLevelContentType);
        messageMap.put(CARBON_MESSAGE, cMsg);
        messageMap.put(BALLERINA_REQUEST, request);
        messageMap.put(MULTIPART_ENTITY, entity);
        return messageMap;
    }

    /**
     * Create multipart entity and fill the carbon message with body parts.
     *
     * @param messageMap Represent the map of prerequisite messages
     * @param bodyParts  Represent body parts that needs to be added to multipart entity
     * @return A test carbon message to be used for invoking the service with.
     */
    static HTTPTestRequest getCarbonMessageWithBodyParts(Map<String, Object> messageMap, BRefValueArray bodyParts) {
        HTTPTestRequest cMsg = (HTTPTestRequest) messageMap.get(CARBON_MESSAGE);
        BStruct request = (BStruct) messageMap.get(BALLERINA_REQUEST);
        BStruct entity = (BStruct) messageMap.get(MULTIPART_ENTITY);
        entity.setRefField(MULTIPART_DATA_INDEX, bodyParts);
        request.addNativeData(MESSAGE_ENTITY, entity);
        setCarbonMessageWithMultiparts(request, cMsg);
        return cMsg;
    }

    /**
     * Add body parts to carbon message.
     *
     * @param request Ballerina request struct
     * @param cMsg    Represent carbon message
     */
    private static void setCarbonMessageWithMultiparts(BStruct request, HTTPTestRequest cMsg) {
        prepareRequestWithMultiparts(cMsg, request);
        try {
            HttpPostRequestEncoder nettyEncoder = (HttpPostRequestEncoder) request.getNativeData(MULTIPART_ENCODER);
            addMultipartsToCarbonMessage(cMsg, nettyEncoder);
        } catch (Exception e) {
            log.error("Error occurred while adding multiparts to carbon message in setCarbonMessageWithMultiparts",
                    e.getMessage());
        }
    }

    /**
     * Read http content chunk by chunk from netty encoder and add it to carbon message.
     *
     * @param httpRequestMsg Represent carbon message that the content should be added to
     * @param nettyEncoder   Represent netty encoder that holds the actual http content
     * @throws Exception In case content cannot be read from netty encoder
     */
    private static void addMultipartsToCarbonMessage(HTTPCarbonMessage httpRequestMsg,
                                                     HttpPostRequestEncoder nettyEncoder) throws Exception {
        while (!nettyEncoder.isEndOfInput()) {
            httpRequestMsg.addHttpContent(nettyEncoder.readChunk(ByteBufAllocator.DEFAULT));
        }
        nettyEncoder.cleanFiles();
    }

    /**
     * Prepare carbon request message with multiparts.
     *
     * @param outboundRequest Represent outbound carbon request
     * @param requestStruct   Ballerina request struct which contains multipart data
     */
    private static void prepareRequestWithMultiparts(HTTPCarbonMessage outboundRequest, BStruct requestStruct) {
        BStruct entityStruct = requestStruct.getNativeData(MESSAGE_ENTITY) != null ?
                (BStruct) requestStruct.getNativeData(MESSAGE_ENTITY) : null;
        if (entityStruct != null) {
            BRefValueArray bodyParts = entityStruct.getRefField(MULTIPART_DATA_INDEX) != null ?
                    (BRefValueArray) entityStruct.getRefField(MULTIPART_DATA_INDEX) : null;
            if (bodyParts != null) {
                HttpDataFactory dataFactory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
                setDataFactory(dataFactory);
                try {
                    HttpPostRequestEncoder nettyEncoder = new HttpPostRequestEncoder(dataFactory,
                            outboundRequest.getNettyHttpRequest(), true);
                    for (int i = 0; i < bodyParts.size(); i++) {
                        BStruct bodyPart = (BStruct) bodyParts.get(i);
                        encodeBodyPart(nettyEncoder, outboundRequest.getNettyHttpRequest(),
                                bodyPart);
                    }
                    nettyEncoder.finalizeRequest();
                    requestStruct.addNativeData(MULTIPART_ENCODER, nettyEncoder);
                } catch (HttpPostRequestEncoder.ErrorDataEncoderException e) {
                    log.error("Error occurred while creating netty request encoder for multipart data binding",
                            e.getMessage());
                }
            }
        }
    }

    /**
     * Encode a given body part and add it to multipart request encoder.
     *
     * @param nettyEncoder Helps encode multipart/form-data
     * @param httpRequest  Represent top level http request that should hold multiparts
     * @param bodyPart     Represent a ballerina body part
     * @throws HttpPostRequestEncoder.ErrorDataEncoderException when an error occurs while encoding
     */
    private static void encodeBodyPart(HttpPostRequestEncoder nettyEncoder, HttpRequest httpRequest,
                                       BStruct bodyPart) throws HttpPostRequestEncoder.ErrorDataEncoderException {
        try {
            InterfaceHttpData encodedData;
            EntityBody entityBodyReader = MimeUtil.constructEntityBody(bodyPart);
            FileUploadContentHolder contentHolder = new FileUploadContentHolder();
            contentHolder.setRequest(httpRequest);
            contentHolder.setBodyPartName(getBodyPartName(bodyPart));
            contentHolder.setFileName(TEMP_FILE_NAME + TEMP_FILE_EXTENSION);
            contentHolder.setContentType(MimeUtil.getContentType(bodyPart));
            contentHolder.setBodyPartFormat(org.ballerinalang.mime.util.Constants.BodyPartForm.INPUTSTREAM);
            String contentTransferHeaderValue = HeaderUtil.getHeaderValue(bodyPart, CONTENT_TRANSFER_ENCODING);
            if (contentTransferHeaderValue != null) {
                contentHolder.setContentTransferEncoding(contentTransferHeaderValue);
            }
            if (entityBodyReader.isStream()) {
                contentHolder.setContentStream(entityBodyReader.getEntityWrapper().getInputStream());
                encodedData = getFileUpload(contentHolder);
            } else {
                FileIOChannel fileIOChannel = entityBodyReader.getFileIOChannel();
                contentHolder.setContentStream(fileIOChannel.getInputStream());
                encodedData = getFileUpload(contentHolder);
            }

            if (encodedData != null) {
                nettyEncoder.addBodyHttpData(encodedData);
            }
        } catch (IOException e) {
            log.error("Error occurred while encoding body part in ", e.getMessage());
        }
    }

    /**
     * Get a body part as a file upload.
     *
     * @param contentHolder Holds attributes required for creating a body part
     * @return InterfaceHttpData which represent an encoded file upload part for the given
     * @throws IOException In case an error occurs while creating file part
     */
    private static InterfaceHttpData getFileUpload(FileUploadContentHolder contentHolder)
            throws IOException {
        FileUpload fileUpload = dataFactory.createFileUpload(contentHolder.getRequest(), contentHolder.getBodyPartName()
                , contentHolder.getFileName(), contentHolder.getContentType(),
                contentHolder.getContentTransferEncoding(), contentHolder.getCharset(), contentHolder.getFileSize());
        switch (contentHolder.getBodyPartFormat()) {
            case INPUTSTREAM:
                fileUpload.setContent(contentHolder.getContentStream());
                break;
            case FILE:
                fileUpload.setContent(contentHolder.getFile());
                break;
        }
        return fileUpload;
    }

    /**
     * Set the data factory that needs to be used for encoding body parts.
     *
     * @param dataFactory which enables creation of InterfaceHttpData objects
     */
    private static void setDataFactory(HttpDataFactory dataFactory) {
        Util.dataFactory = dataFactory;
    }

    /**
     * Get the body part name and if the user hasn't set a name set a random string as the part name.
     *
     * @param bodyPart Represent a ballerina body part
     * @return a string denoting the body part's name
     */
    private static String getBodyPartName(BStruct bodyPart) {
        String contentDisposition = MimeUtil.getContentDisposition(bodyPart);
        if (!contentDisposition.isEmpty()) {
            BMap<String, BValue> paramMap = HeaderUtil.getParamMap(contentDisposition);
            if (paramMap != null) {
                BString bodyPartName = paramMap.get(CONTENT_DISPOSITION_NAME) != null ?
                        (BString) paramMap.get(CONTENT_DISPOSITION_NAME) : null;
                if (bodyPartName != null) {
                    return bodyPartName.toString();
                } else {
                    return getRandomString();
                }
            } else {
                return getRandomString();
            }
        } else {
            return getRandomString();
        }
    }

    private static String getRandomString() {
        return UUID.randomUUID().toString();
    }

    private static BStruct getRequestStruct(CompileResult result) {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), PROTOCOL_PACKAGE_HTTP, REQUEST_STRUCT);
    }

    static BStruct getEntityStruct(CompileResult result) {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), PACKAGE_MIME, ENTITY_STRUCT);
    }

    private static BStruct getMediaTypeStruct(CompileResult result) {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), PACKAGE_MIME,
                MEDIA_TYPE_STRUCT);
    }

    static BStruct getContentDispositionStruct(CompileResult result) {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), PACKAGE_MIME, CONTENT_DISPOSITION_STRUCT);
    }

    static BStruct getByteChannelStruct(CompileResult result) {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), PROTOCOL_PACKAGE_IO, BYTE_CHANNEL_STRUCT);
    }
}
