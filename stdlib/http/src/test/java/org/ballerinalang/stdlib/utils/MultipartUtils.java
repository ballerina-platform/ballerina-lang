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

package org.ballerinalang.stdlib.utils;

import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.BValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.internal.StringUtil;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityHeaderHandler;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeConstants;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.ballerinalang.mime.util.MimeConstants.BODY_PARTS;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_NAME;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_ENCODER;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_IO_PKG_ID;
import static org.ballerinalang.mime.util.MimeConstants.READABLE_BYTE_CHANNEL_STRUCT;
import static org.ballerinalang.mime.util.MimeConstants.REQUEST_ENTITY_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.TEMP_FILE_EXTENSION;
import static org.ballerinalang.mime.util.MimeConstants.TEMP_FILE_NAME;
import static org.ballerinalang.net.http.ValueCreatorUtils.createEntityObject;
import static org.ballerinalang.net.http.ValueCreatorUtils.createMediaTypeObject;
import static org.ballerinalang.net.http.ValueCreatorUtils.createRequestObject;

/**
 * Utility functions for multipart handling.
 */
public class MultipartUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MultipartUtils.class);

    private static final String CARBON_MESSAGE = "CarbonMessage";
    private static final String BALLERINA_REQUEST = "BallerinaRequest";
    private static final String MULTIPART_ENTITY = "MultipartEntity";
    private static HttpDataFactory dataFactory = null;

    /**
     * Create prerequisite messages that are needed to proceed with the test cases.
     *
     * @param path                Represent path to the ballerina resource
     * @param topLevelContentType Content type that needs to be set to the top level message
     * @return A map of relevant messages
     */
    public static Map<String, Object> createPrerequisiteMessages(String path, String topLevelContentType) {
        Map<String, Object> messageMap = new HashMap<>();
        BObject request = createRequestObject();
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessageForMultiparts(path, HttpConstants.HTTP_METHOD_POST);
        HttpUtil.addCarbonMsg(request, cMsg);
        BObject entity = createEntityObject();
        MimeUtil.setContentType(createMediaTypeObject(), entity, topLevelContentType);
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
    public static HTTPTestRequest getCarbonMessageWithBodyParts(Map<String, Object> messageMap, BArray bodyParts) {
        HTTPTestRequest cMsg = (HTTPTestRequest) messageMap.get(CARBON_MESSAGE);
        BObject request = (BObject) messageMap.get(BALLERINA_REQUEST);
        BObject entity = (BObject) messageMap.get(MULTIPART_ENTITY);
        entity.addNativeData(BODY_PARTS, bodyParts);
        request.set(REQUEST_ENTITY_FIELD, entity);
        setCarbonMessageWithMultiparts(request, cMsg);
        return cMsg;
    }

    /**
     * Add body parts to carbon message.
     *
     * @param request Ballerina request struct
     * @param cMsg    Represent carbon message
     */
    private static void setCarbonMessageWithMultiparts(BObject request, HTTPTestRequest cMsg) {
        prepareRequestWithMultiparts(cMsg, request);
        try {
            HttpPostRequestEncoder nettyEncoder = (HttpPostRequestEncoder) request.getNativeData(MULTIPART_ENCODER);
            addMultipartsToCarbonMessage(cMsg, nettyEncoder);
        } catch (Exception e) {
            LOG.error("Error occurred while adding multiparts to carbon message in setCarbonMessageWithMultiparts",
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
    private static void addMultipartsToCarbonMessage(HttpCarbonMessage httpRequestMsg,
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
    private static void prepareRequestWithMultiparts(HttpCarbonMessage outboundRequest, BObject requestStruct) {
        BObject entityStruct = requestStruct.get(REQUEST_ENTITY_FIELD) != null ?
                (BObject) requestStruct.get(REQUEST_ENTITY_FIELD) : null;
        if (entityStruct != null) {
            BArray bodyParts = entityStruct.getNativeData(BODY_PARTS) != null ?
                    (BArray) entityStruct.getNativeData(BODY_PARTS) : null;
            if (bodyParts != null) {
                HttpDataFactory dataFactory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
                setDataFactory(dataFactory);
                try {
                    HttpPostRequestEncoder nettyEncoder =
                            new HttpPostRequestEncoder(dataFactory, outboundRequest.getNettyHttpRequest(), true);
                    for (int i = 0; i < bodyParts.size(); i++) {
                        BObject bodyPart = (BObject) bodyParts.getRefValue(i);
                        encodeBodyPart(nettyEncoder, outboundRequest.getNettyHttpRequest(), bodyPart);
                    }
                    nettyEncoder.finalizeRequest();
                    requestStruct.addNativeData(MULTIPART_ENCODER, nettyEncoder);
                } catch (HttpPostRequestEncoder.ErrorDataEncoderException e) {
                    LOG.error("Error occurred while creating netty request encoder for multipart data binding",
                              e.getMessage());
                }
            }
        }
    }

    /**
     * Two body parts have been wrapped inside multipart/mixed which in turn acts as the child part for the parent
     * multipart/form-data.
     *
     * @param path Resource path
     * @return HTTPTestRequest with nested parts as the entity body
     */
    public static HTTPTestRequest createNestedPartRequest(String path) {
        HttpHeaders headers = new DefaultHttpHeaders();
        String multipartDataBoundary = MimeUtil.getNewMultipartDelimiter();
        String multipartMixedBoundary = MimeUtil.getNewMultipartDelimiter();
        headers.add(HttpHeaderNames.CONTENT_TYPE.toString(), "multipart/form-data; boundary=" +
                multipartDataBoundary);
        String multipartBodyWithNestedParts = "--" + multipartDataBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"parent1\"" + "\r\n" +
                "Content-Type: text/plain; charset=UTF-8" + "\r\n" +
                "\r\n" +
                "Parent Part" + "\r\n" +
                "--" + multipartDataBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"parent2\"" + "\r\n" +
                "Content-Type: multipart/mixed; boundary=" + multipartMixedBoundary + "\r\n" +
                "\r\n" +
                "--" + multipartMixedBoundary + "\r\n" +
                "Content-Disposition: attachment; filename=\"file-02.txt\"" + "\r\n" +
                "Content-Type: text/plain" + "\r\n" +
                "Content-Transfer-Encoding: binary" + "\r\n" +
                "\r\n" +
                "Child Part 1" + StringUtil.NEWLINE +
                "\r\n" +
                "--" + multipartMixedBoundary + "\r\n" +
                "Content-Disposition: attachment; filename=\"file-02.txt\"" + "\r\n" +
                "Content-Type: text/plain" + "\r\n" +
                "Content-Transfer-Encoding: binary" + "\r\n" +
                "\r\n" +
                "Child Part 2" + StringUtil.NEWLINE +
                "\r\n" +
                "--" + multipartMixedBoundary + "--" + "\r\n" +
                "--" + multipartDataBoundary + "--" + "\r\n";
        return MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, headers,
                                                multipartBodyWithNestedParts);
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
                                       BObject bodyPart)
            throws HttpPostRequestEncoder.ErrorDataEncoderException {
        try {
            InterfaceHttpData encodedData;
            Channel byteChannel = EntityBodyHandler.getByteChannel(bodyPart);
            FileUploadContentHolder contentHolder = new FileUploadContentHolder();
            contentHolder.setRequest(httpRequest);
            contentHolder.setBodyPartName(getBodyPartName(bodyPart));
            contentHolder.setFileName(TEMP_FILE_NAME + TEMP_FILE_EXTENSION);
            contentHolder.setContentType(MimeUtil.getBaseType(bodyPart));
            contentHolder.setBodyPartFormat(MimeConstants.BodyPartForm.INPUTSTREAM);
            String contentTransferHeaderValue =
                    EntityHeaderHandler.getHeaderValue(bodyPart, HttpHeaderNames.CONTENT_TRANSFER_ENCODING.toString());
            if (contentTransferHeaderValue != null) {
                contentHolder.setContentTransferEncoding(contentTransferHeaderValue);
            }
            if (byteChannel != null) {
                contentHolder.setContentStream(byteChannel.getInputStream());
                encodedData = getFileUpload(contentHolder);
                if (encodedData != null) {
                    nettyEncoder.addBodyHttpData(encodedData);
                }
            }
        } catch (IOException e) {
            LOG.error("Error occurred while encoding body part in ", e.getMessage());
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
        MultipartUtils.dataFactory = dataFactory;
    }

    /**
     * Get the body part name and if the user hasn't set a name set a random string as the part name.
     *
     * @param bodyPart Represent a ballerina body part
     * @return A string denoting the body part's name
     */
    private static String getBodyPartName(BObject bodyPart) {
        String contentDisposition = MimeUtil.getContentDisposition(bodyPart);
        if (!contentDisposition.isEmpty()) {
            BMap paramMap = HeaderUtil.getParamMap(contentDisposition);
            if (paramMap != null) {
                BString bodyPartName = paramMap.get(BStringUtils.fromString(CONTENT_DISPOSITION_NAME)) != null ?
                        (BString) paramMap.get(BStringUtils.fromString(CONTENT_DISPOSITION_NAME)) : null;
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

    public static BObject getByteChannelStruct() {
        return BValueCreator.createObjectValue(PROTOCOL_IO_PKG_ID, READABLE_BYTE_CHANNEL_STRUCT);
    }
}
