/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.messaging.artemis.externimpl.consumer;


import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.MessageHandler;
import org.apache.activemq.artemis.reader.MapMessageUtil;
import org.apache.activemq.artemis.reader.TextMessageUtil;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.messaging.artemis.ArtemisConnectorException;
import org.ballerinalang.messaging.artemis.ArtemisConstants;
import org.ballerinalang.messaging.artemis.ArtemisUtils;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.util.XMLNodeType;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Handler for Artemis messages.
 *
 * @since 0.995
 */
class ArtemisMessageHandler implements MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ArtemisMessageHandler.class);

    private Resource onMessageResource;
    private BMap<String, BValue> sessionObj;
    private boolean autoAck;

    ArtemisMessageHandler(Resource onMessageResource, BMap<String, BValue> sessionObj, boolean autoAck) {
        this.onMessageResource = onMessageResource;
        this.sessionObj = sessionObj;
        this.autoAck = autoAck;
    }

    @Override
    public void onMessage(ClientMessage clientMessage) {
        List<ParamDetail> paramDetails = onMessageResource.getParamDetails();
        int paramSize = paramDetails.size();
        if (paramSize > 1) {
            dispatchResourceWithDataBinding(clientMessage, paramDetails);
        } else {
            dispatchResource(clientMessage, createAndGetMessageObj(onMessageResource, clientMessage, sessionObj));
        }

    }

    private void dispatchResourceWithDataBinding(ClientMessage clientMessage, List<ParamDetail> paramDetails) {
        BValue[] bValues = new BValue[paramDetails.size()];
        try {
            bValues[1] = getContentForType(clientMessage, paramDetails.get(1).getVarType());
            bValues[0] = createAndGetMessageObj(onMessageResource, clientMessage, sessionObj);
            dispatchResource(clientMessage, bValues);
        } catch (BallerinaException ex) {
            logger.error("The message received do not match the resource signature", ex);
        }
    }

    private void dispatchResource(ClientMessage clientMessage, BValue... bValues) {
        // A CountDownLatch is used to prevent multiple resources executing in parallel and hence preventing the use
        // of the same session in multiple threads concurrently (Error AMQ212051).
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executor.submit(onMessageResource, new ArtemisResourceCallback(clientMessage, autoAck, sessionObj,
                                                                       countDownLatch), null, null, bValues);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private BValue createAndGetMessageObj(Resource onMessageResource, ClientMessage clientMessage,
                                          BMap<String, BValue> sessionObj) {
        ProgramFile programFile = onMessageResource.getResourceInfo().getPackageInfo().getProgramFile();
        BMap<String, BValue> messageObj = BLangConnectorSPIUtil.createBStruct(
                programFile, ArtemisConstants.PROTOCOL_PACKAGE_ARTEMIS, ArtemisConstants.MESSAGE_OBJ);
        ArtemisUtils.populateMessageObj(clientMessage,
                                        sessionObj.getNativeData(ArtemisConstants.ARTEMIS_TRANSACTION_CONTEXT),
                                        messageObj);
        return messageObj;
    }

    private BValue getContentForType(ClientMessage message, BType dataType) {
        int dataTypeTag = dataType.getTag();
        byte messageType = message.getType();
        switch (dataTypeTag) {
            case TypeTags.STRING_TAG:
                return new BString(getBodyText(message, messageType));
            case TypeTags.JSON_TAG:
                return JsonParser.parse(getBodyText(message, messageType));
            case TypeTags.XML_TAG:
                BXML bxml = XMLUtils.parse(getBodyText(message, messageType));
                if (bxml.getNodeType() != XMLNodeType.ELEMENT) {
                    throw new ArtemisConnectorException("Invalid XML data");
                }
                return bxml;
            case TypeTags.RECORD_TYPE_TAG:
                return JSONUtils.convertJSONToStruct(JsonParser.parse(getBodyText(message, messageType)),
                                                     (BStructureType) dataType);
            case TypeTags.ARRAY_TAG:
                if (((BArrayType) dataType).getElementType().getTag() == TypeTags.BYTE_TAG) {
                    validateBytesContentType(messageType);
                    return ArtemisUtils.getBArrayValue(message);
                } else {
                    throw new ArtemisConnectorException("Only byte[] is supported.");
                }
            case TypeTags.MAP_TAG:
                validateMapContentType(messageType);
                int constrainedType = ((BMapType) dataType).getConstrainedType().getTag();
                return createAndGetMapContent(message, constrainedType);
            default:
                throw new ArtemisConnectorException(
                        "The content type of the message received does not match the resource signature type.");
        }
    }

    private BValue createAndGetMapContent(ClientMessage message, int constrainedType) {
        Map<String, Object> map;
        BMap<String, BValue> mapObj;
        switch (constrainedType) {
            case TypeTags.STRING_TAG:
                map = getStringObjectMap(message);
                mapObj = new BMap<>(new BMapType(BTypes.typeString));
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        mapObj.put(entry.getKey(), new BString((String) value));
                    } else {
                        throw new ArtemisConnectorException("The map has other than string data");
                    }
                }
                return mapObj;
            case TypeTags.INT_TAG:
                map = getStringObjectMap(message);
                mapObj = new BMap<>(new BMapType(BTypes.typeInt));
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof Integer || value instanceof Long || value instanceof Short) {
                        mapObj.put(entry.getKey(), new BInteger((long) value));
                    } else {
                        throw new ArtemisConnectorException("The map has other than int data");
                    }
                }
                return mapObj;
            case TypeTags.FLOAT_TAG:
                map = getStringObjectMap(message);
                mapObj = new BMap<>(new BMapType(BTypes.typeFloat));
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof Float || value instanceof Double) {
                        mapObj.put(entry.getKey(), new BFloat((double) value));
                    } else {
                        throw new ArtemisConnectorException("The map has other than float data");
                    }
                }
                return mapObj;
            case TypeTags.BYTE_TAG:
                map = getStringObjectMap(message);
                mapObj = new BMap<>(new BMapType(BTypes.typeByte));
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof Byte) {
                        mapObj.put(entry.getKey(), new BByte((byte) value));
                    } else {
                        throw new ArtemisConnectorException("The map has other than byte data");
                    }
                }
                return mapObj;
            case TypeTags.ARRAY_TAG:
                map = getStringObjectMap(message);
                mapObj = new BMap<>(new BMapType(BTypes.fromString("byte[]")));
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof byte[]) {
                        mapObj.put(entry.getKey(), new BValueArray((byte[]) value));
                    } else {
                        throw new ArtemisConnectorException("The map has other than byte[] data");
                    }
                }
                return mapObj;
            case TypeTags.BOOLEAN_TAG:
                map = getStringObjectMap(message);
                mapObj = new BMap<>(new BMapType(BTypes.typeBoolean));
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof Boolean) {
                        mapObj.put(entry.getKey(), new BBoolean((boolean) value));
                    } else {
                        throw new ArtemisConnectorException("The map has other than boolean data");
                    }
                }
                return mapObj;
            default:
                throw new ArtemisConnectorException(
                        "The content type of the message received does not match the provided type.");
        }
    }

    private Map<String, Object> getStringObjectMap(ClientMessage message) {
        return MapMessageUtil.readBodyMap(message.getBodyBuffer()).getMap();
    }

    private String getBodyText(ClientMessage message, byte messageType) {
        validateTextContentType(messageType);
        return TextMessageUtil.readBodyText(message.getBodyBuffer()).toString();
    }

    private void validateTextContentType(byte messageType) {
        if (messageType != Message.TEXT_TYPE) {
            throw new ArtemisConnectorException(
                    "Invalid resource signature. Message received does not have text content");
        }
    }

    private void validateMapContentType(byte messageType) {
        if (messageType != Message.MAP_TYPE) {
            throw new ArtemisConnectorException(
                    "Invalid resource signature. Message received does not have map content");
        }
    }

    private void validateBytesContentType(byte messageType) {
        if (messageType != Message.BYTES_TYPE && messageType != Message.DEFAULT_TYPE) {
            throw new ArtemisConnectorException(
                    "Invalid resource signature. Message received does not have map content");
        }
    }
}
