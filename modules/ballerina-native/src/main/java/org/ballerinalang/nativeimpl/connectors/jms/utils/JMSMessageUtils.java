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

package org.ballerinalang.nativeimpl.connectors.jms.utils;

import org.ballerinalang.model.util.MessageUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.lang.utils.Constants;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.MessageDataSource;
import org.wso2.carbon.messaging.SerializableCarbonMessage;

import java.util.HashMap;
import java.util.List;

/**
 * {@code JMSMessageUtils}. Utils class for jms connector.
 */
public class JMSMessageUtils {

    /**
     * To convert a {@link BMessage} message into a {@link SerializableCarbonMessage}.
     *
     * @param message {@link BMessage} to be converted
     * @return Converted {@link SerializableCarbonMessage}
     */
    public static SerializableCarbonMessage toSerializableCarbonMessage(BMessage message) {
        SerializableCarbonMessage serializableCarbonMessage = new SerializableCarbonMessage();

        List<Header> headers = message.getHeaders();
        HashMap<String, String> headerMap = new HashMap<>();
        for (Header header : headers) {
            headerMap.put(header.getName(), header.getValue());
        }
        serializableCarbonMessage.setHeaders(headerMap);
        if (headerMap.containsKey(Constants.CONTENT_TYPE)) {
            if (headerMap.get(Constants.CONTENT_TYPE).equals(Constants.APPLICATION_JSON)) {
                serializableCarbonMessage.setPayload(getJsonPayload(message));
            } else if (headerMap.get(Constants.CONTENT_TYPE).equals(Constants.APPLICATION_XML)) {
                serializableCarbonMessage.setPayload(getXmlPayload(message));
            } else if (headerMap.get(Constants.CONTENT_TYPE).equals(Constants.TEXT_PLAIN)) {
                serializableCarbonMessage.setPayload(getStringPayload(message));
            }
        }

        return serializableCarbonMessage;
    }

    private static String getJsonPayload(BMessage message) {
        BJSON result;
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
        return result.stringValue();
    }

    private static String getXmlPayload(BMessage message) {
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
            return result.stringValue();
        } catch (Throwable e) {
            ErrorHandler.handleJsonException("get json payload", e);
        }
        return null;
    }

    private static String getStringPayload(BMessage message) {
        BString result;
        try {
            if (message.isAlreadyRead()) {
                result = new BString(message.getMessageDataSource().getMessageAsString());
            } else {
                String payload = MessageUtils.getStringFromInputStream(message.value().getInputStream());
                result = new BString(payload);
                message.setMessageDataSource(result.stringValue());
                message.setAlreadyRead(true);
            }
            return result.stringValue();
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving string payload from message: " + e.getMessage());
        }
    }

    /**
     * To convert a {@link SerializableCarbonMessage} message into a {@link BMessage}.
     *
     * @param serializableCarbonMessage {@link SerializableCarbonMessage} to be converted
     * @return Converted {@link BMessage}
     */
    public static BMessage toBallerinaMessage(SerializableCarbonMessage serializableCarbonMessage) {
        BMessage bMessage = new BMessage();
        HashMap<String, String> headerMap = serializableCarbonMessage.getHeadersMap();
        headerMap.forEach((key, value) -> {
            bMessage.getHeaders().add(new Header(key, value));
        });

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
