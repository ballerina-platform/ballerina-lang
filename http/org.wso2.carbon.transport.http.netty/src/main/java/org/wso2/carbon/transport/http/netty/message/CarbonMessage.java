/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.message;

import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.Headers;
import org.wso2.carbon.messaging.MessageDataSource;
import org.wso2.carbon.messaging.exceptions.MessagingException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Data carrier between the components.
 */
public abstract class CarbonMessage {

    protected Headers headers = new Headers();
    protected Map<String, Object> properties = new HashMap<>();
    private MessageDataSource messageDataSource;
    MessagingException messagingException = null;
    AtomicBoolean alreadyRead = new AtomicBoolean(false);
    AtomicBoolean endOfMsgAdded = new AtomicBoolean(false);

    public CarbonMessage() {
    }

    public boolean isEndOfMsgAdded() {
        return endOfMsgAdded.get();
    }

    public Headers getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public void setHeader(String key, String value) {
        headers.set(key, value);
    }

    public void setHeaders(Map<String, String> headerMap) {
        headers.set(headerMap);
    }

    public void setHeaders(List<Header> headerList) {
        headers.set(headerList);
    }

    public Object getProperty(String key) {
        if (properties != null) {
            return properties.get(key);
        } else {
            return null;
        }
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public void removeHeader(String key) {
        headers.remove(key);
    }

    public void removeProperty(String key) {
        properties.remove(key);
    }

    /**
     * This method is used to mark the end of the message when cloning the content.
     */
    public void markMessageEnd() {
    }

    public MessageDataSource getMessageDataSource() {
        return messageDataSource;
    }

    public void setMessageDataSource(MessageDataSource messageDataSource) {
        this.messageDataSource = messageDataSource;
    }

    public boolean isAlreadyRead() {
        return alreadyRead.get();
    }

    public void setAlreadyRead(boolean alreadyRead) {
        this.alreadyRead.set(alreadyRead);
    }

    /**
     * Get CarbonMessageException
     *
     * @return CarbonMessageException instance related to faulty CarbonMessage.
     */
    public MessagingException getMessagingException() {
        return messagingException;
    }

    /**
     * Set CarbonMessageException.
     *
     * @param messagingException exception related to faulty CarbonMessage.
     */
    public void setMessagingException(MessagingException messagingException) {
        this.messagingException = messagingException;
    }
}
