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

package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityWrapper;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDataSource;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

import static org.ballerinalang.mime.nativeimpl.AbstractGetPayloadHandler.getErrorMsg;
import static org.ballerinalang.mime.util.HeaderUtil.isMultipart;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_XML;
import static org.ballerinalang.mime.util.MimeConstants.BODY_PARTS;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.INVALID_CONTENT_TYPE_ERROR;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MESSAGE_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MESSAGE_DATA_SOURCE;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_FORM_DATA;
import static org.ballerinalang.mime.util.MimeConstants.OCTET_STREAM;
import static org.ballerinalang.mime.util.MimeConstants.PARSER_ERROR;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_IO_PKG_ID;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_MIME_PKG_ID;
import static org.ballerinalang.mime.util.MimeConstants.READABLE_BYTE_CHANNEL_STRUCT;
import static org.ballerinalang.mime.util.MimeConstants.TEXT_PLAIN;
import static org.ballerinalang.mime.util.MimeConstants.TRANSPORT_MESSAGE;
import static org.ballerinalang.mime.util.MimeUtil.getContentTypeWithParameters;
import static org.ballerinalang.mime.util.MimeUtil.getNewMultipartDelimiter;

/**
 * Utilities related to MIME entity body.
 *
 * @since 1.1.0
 */
public class MimeEntityBody {
    private static final Logger log = LoggerFactory.getLogger(MimeEntityBody.class);

    public static Object getBodyParts(ObjectValue entityObj) {
        ArrayValue partsArray;
        try {
            String baseType = HeaderUtil.getBaseType(entityObj);
            if (baseType != null && (baseType.toLowerCase(Locale.getDefault()).startsWith(MULTIPART_AS_PRIMARY_TYPE) ||
                    baseType.toLowerCase(Locale.getDefault()).startsWith(MESSAGE_AS_PRIMARY_TYPE))) {
                //Get the body parts from entity's multipart data field, if they've been already been decoded
                partsArray = EntityBodyHandler.getBodyPartArray(entityObj);
                if (partsArray == null || partsArray.size() < 1) {
                    Channel byteChannel = EntityBodyHandler.getByteChannel(entityObj);
                    if (byteChannel != null) {
                        EntityBodyHandler.decodeEntityBody(entityObj, byteChannel);
                        //Check the body part availability for the second time, since the parts will be by this
                        // time populated from bytechannel
                        partsArray = EntityBodyHandler.getBodyPartArray(entityObj);
                        //Set byte channel that belongs to parent entity to null, once the message body parts have
                        // been decoded
                        entityObj.addNativeData(ENTITY_BYTE_CHANNEL, null);
                    }
                }
                return partsArray;
            } else {
                return MimeUtil.createError(PARSER_ERROR, "Entity body is not a type of " +
                        "composite media type. Received content-type : " + baseType);
            }
        } catch (Throwable err) {
            return MimeUtil.createError(PARSER_ERROR,
                                        "Error occurred while extracting body parts from entity: " + getErrorMsg(err));
        }
    }

    public static Object getBodyPartsAsChannel(ObjectValue entityObj) {
        try {
            String contentType = getContentTypeWithParameters(entityObj);
            if (isMultipart(contentType)) {
                String boundaryValue = HeaderUtil.extractBoundaryParameter(contentType);
                String multipartDataBoundary = boundaryValue != null ? boundaryValue : getNewMultipartDelimiter();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                MultipartDataSource multipartDataSource = new MultipartDataSource(entityObj, multipartDataBoundary);
                multipartDataSource.serialize(outputStream);
                EntityBodyChannel entityBodyChannel = new EntityBodyChannel(new ByteArrayInputStream(
                        outputStream.toByteArray()));
                MimeUtil.closeOutputStream(outputStream);
                ObjectValue byteChannelObj = BallerinaValues.createObjectValue(IOConstants.IO_PACKAGE_ID,
                                                                               READABLE_BYTE_CHANNEL_STRUCT);
                byteChannelObj.addNativeData(IOConstants.BYTE_CHANNEL_NAME, new EntityWrapper(entityBodyChannel));
                return byteChannelObj;
            } else {
                return MimeUtil.createError(PARSER_ERROR, "Entity doesn't contain body parts");
            }
        } catch (Throwable err) {
            log.error("Error occurred while constructing a byte channel out of body parts", err);
            return MimeUtil.createError(PARSER_ERROR, "Error occurred while constructing a byte " +
                    "channel out of body parts : " + getErrorMsg(err));
        }
    }

    public static Object getByteChannel(ObjectValue entityObj) {
        ObjectValue byteChannelObj;
        try {
            byteChannelObj = BallerinaValues.createObjectValue(PROTOCOL_IO_PKG_ID, READABLE_BYTE_CHANNEL_STRUCT);
            populateEntityWithByteChannel(entityObj);
            Channel byteChannel = EntityBodyHandler.getByteChannel(entityObj);
            if (byteChannel != null) {
                byteChannelObj.addNativeData(IOConstants.BYTE_CHANNEL_NAME, byteChannel);
                return byteChannelObj;
            } else {
                if (EntityBodyHandler.getMessageDataSource(entityObj) != null) {
                    return MimeUtil.createError(PARSER_ERROR, "Byte channel is not available but " +
                            "payload can be obtain either as xml, json, string or byte[] " +
                            "type");
                } else if (EntityBodyHandler.getBodyPartArray(entityObj) != null && EntityBodyHandler.
                        getBodyPartArray(entityObj).size() != 0) {
                    return MimeUtil.createError(PARSER_ERROR,
                                                "Byte channel is not available since payload contains a set of body " +
                                                        "parts");
                } else {
                    return MimeUtil.createError(PARSER_ERROR, "Byte channel is not available as payload");
                }
            }
        } catch (Throwable err) {
            return MimeUtil.createError(PARSER_ERROR,
                                        "Error occurred while constructing byte channel from entity body : " +
                                                getErrorMsg(err));
        }
    }

    private static void populateEntityWithByteChannel(ObjectValue entity) {
        HttpCarbonMessage httpCarbonMessage = (HttpCarbonMessage) entity.getNativeData(TRANSPORT_MESSAGE);
        if (httpCarbonMessage == null) {
            return;
        }
        HttpMessageDataStreamer httpMessageDataStreamer = new HttpMessageDataStreamer(httpCarbonMessage);

        long contentLength = MimeUtil.extractContentLength(httpCarbonMessage);
        if (contentLength > 0) {
            entity.addNativeData(ENTITY_BYTE_CHANNEL, new EntityWrapper(
                    new EntityBodyChannel(httpMessageDataStreamer.getInputStream())));
        }
    }

    public static Object getMediaType(BString contentType) {
        try {
            ObjectValue mediaType = BallerinaValues.createObjectValue(PROTOCOL_MIME_PKG_ID, MEDIA_TYPE);
            return MimeUtil.parseMediaType(mediaType, contentType.getValue());
        } catch (Throwable err) {
            return MimeUtil.createError(INVALID_CONTENT_TYPE_ERROR, getErrorMsg(err));
        }
    }

    public static void setBodyParts(ObjectValue entityObj, ArrayValue bodyParts, BString contentType) {
        entityObj.addNativeData(BODY_PARTS, bodyParts);
        MimeUtil.setMediaTypeToEntity(entityObj, contentType != null ? contentType.getValue() : MULTIPART_FORM_DATA);
    }

    public static void setByteArray(ObjectValue entityObj, ArrayValue payload, BString contentType) {
        EntityBodyHandler.addMessageDataSource(entityObj, payload);
        MimeUtil.setMediaTypeToEntity(entityObj, contentType != null ? contentType.getValue() : OCTET_STREAM);
    }

    public static void setByteChannel(ObjectValue entityObj, ObjectValue byteChannel,
                                      BString contentType) {
        entityObj.addNativeData(ENTITY_BYTE_CHANNEL, byteChannel.getNativeData(IOConstants.BYTE_CHANNEL_NAME));
        Object dataSource = EntityBodyHandler.getMessageDataSource(entityObj);
        if (dataSource != null) { //Clear message data source when the user set a byte channel to entity
            entityObj.addNativeData(MESSAGE_DATA_SOURCE, null);
        }
        MimeUtil.setMediaTypeToEntity(entityObj, contentType != null ? contentType.getValue() : OCTET_STREAM);
    }

    public static void setJson(ObjectValue entityObj, Object jsonContent, BString contentType) {
        EntityBodyHandler.addJsonMessageDataSource(entityObj, jsonContent);
        MimeUtil.setMediaTypeToEntity(entityObj, contentType != null ? contentType.getValue() : APPLICATION_JSON);
    }

    public static void setText(ObjectValue entityObj, BString textContent, BString contentType) {
        EntityBodyHandler.addMessageDataSource(entityObj, textContent.getValue());
        MimeUtil.setMediaTypeToEntity(entityObj, contentType != null ? contentType.getValue() : TEXT_PLAIN);
    }

    public static void setXml(ObjectValue entityObj, XMLValue xmlContent, BString contentType) {
        EntityBodyHandler.addMessageDataSource(entityObj, xmlContent);
        MimeUtil.setMediaTypeToEntity(entityObj, contentType != null ? contentType.getValue() : APPLICATION_XML);
    }
}
