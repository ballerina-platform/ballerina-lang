/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.ballerina.nativeimpl.connectors.jms.utils;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.util.MessageUtils;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.nativeimpl.lang.utils.Constants;
import org.wso2.ballerina.nativeimpl.lang.utils.ErrorHandler;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.MessageDataSource;
import org.wso2.carbon.messaging.SerializableCarbonMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@code JMSMessageUtils}.
 */
public class JMSMessageUtils {
    private static final String OPERATION = "get json payload";

    public static SerializableCarbonMessage toStorableMessage(BMessage message) {
        SerializableCarbonMessage serializableCarbonMessage = new SerializableCarbonMessage();

        List<Header> headers = message.getHeaders();
        HashMap<String, String> headerMap = new HashMap<>();
        for (Header header : headers) {
            headerMap.put(header.getName(), header.getValue());
        }
        serializableCarbonMessage.setHeaders(headerMap);
        if (headerMap.containsKey(Constants.CONTENT_TYPE)) {
            if (headerMap.get(Constants.CONTENT_TYPE).equals(Constants.APPLICATION_JSON)) {
                BJSON result;
                try {
                    if (message.isAlreadyRead()) {
                        MessageDataSource payload = message.getMessageDataSource();
                        if (payload instanceof BJSON) {
                            result = (BJSON) payload;
                        } else {
                            // else, build the JSON from the string representation of the payload.
                            result = new BJSON(message.getMessageDataSource().getMessageAsString());
                        }
                    } else {
                        result = new BJSON(message.value().getInputStream());
                        message.setMessageDataSource(result);
                        message.setAlreadyRead(true);
                    }
                    serializableCarbonMessage.setPayload(result.stringValue());
                } catch (JsonSyntaxException e) {
                    ErrorHandler.handleMalformedJson(OPERATION, e);
                } catch (JsonParseException e) {
                    ErrorHandler.handleJsonException(OPERATION, e);
                } catch (Throwable e) {
                    ErrorHandler.handleJsonException(OPERATION, e);
                }
            } else if (headerMap.get(Constants.CONTENT_TYPE).equals(Constants.APPLICATION_XML)) {
                BXML result;
                try {
                    if (message.isAlreadyRead()) {
                        MessageDataSource payload = message.getMessageDataSource();
                        if (payload instanceof BXML) {
                            // if the payload is already xml, return it as it is.
                            result = (BXML) payload;
                        } else {
                            // else, build the xml from the string representation of the payload.
                            result = new BXML(message.getMessageDataSource().getMessageAsString());
                        }
                    } else {
                        result = new BXML(message.value().getInputStream());
                        message.setMessageDataSource(result);
                        message.setAlreadyRead(true);
                    }
                    serializableCarbonMessage.setPayload(result.stringValue());
                } catch (Throwable e) {
                    ErrorHandler.handleJsonException(OPERATION, e);
                }
            } else if (headerMap.get(Constants.CONTENT_TYPE).equals(Constants.TEXT_PLAIN)) {
                BString result;
                try {
                    if (message.isAlreadyRead()) {
                        result = new BString(message.getMessageDataSource().getMessageAsString());
                    } else {
                        String payload = MessageUtils
                                .getStringFromInputStream(message.value().getInputStream());
                        result = new BString(payload);
                        message.setMessageDataSource(result.stringValue());
                        message.setAlreadyRead(true);
                    }
                    serializableCarbonMessage.setPayload(result.stringValue());
                } catch (Throwable e) {
                    throw new BallerinaException(
                            "Error while retrieving string payload from message: " +
                            e.getMessage());
                }
            }
        }

        return serializableCarbonMessage;
    }

    public static BMessage toBallerinaMessage(SerializableCarbonMessage serializableCarbonMessage) {
        BMessage bMessage = new BMessage();
        HashMap<String, String> headerMap = serializableCarbonMessage.getHeadersMap();
        Iterator iterator = headerMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            bMessage.getHeaders()
                    .add(new Header((String) entry.getKey(), (String) entry.getValue()));
        }
        if (headerMap.get(Constants.CONTENT_TYPE).equals(Constants.APPLICATION_JSON)) {
            BJSON payload = new BJSON(serializableCarbonMessage.getPayload());
            bMessage.setMessageDataSource(payload);
        } else if (headerMap.get(Constants.CONTENT_TYPE).equals(Constants.APPLICATION_XML)) {
            BXML payload = new BXML(serializableCarbonMessage.getPayload());
            bMessage.setMessageDataSource(payload);
        } else if (headerMap.get(Constants.CONTENT_TYPE).equals(Constants.TEXT_PLAIN)) {
            BString payload = new BString(serializableCarbonMessage.getPayload());
            bMessage.setMessageDataSource(payload.stringValue());
        }
        return bMessage;
    }
}
