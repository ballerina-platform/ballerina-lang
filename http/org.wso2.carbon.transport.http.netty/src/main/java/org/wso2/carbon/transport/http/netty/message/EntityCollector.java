/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.netty.handler.codec.http.HttpContent;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Collects entity of the request/response
 */
public interface EntityCollector {

    /**
     * Add httpContent to the queue.
     * @param httpContent httpContent
     */
    void addHttpContent(HttpContent httpContent);

    /**
     * Get the first httpContent from the queue.
     * @return HttpContent
     */
    HttpContent getHttpContent();

    /**
     * Get the first ByteBuffer version of the HttpContent from the queue.
     * @return ByteBuffer
     */
    ByteBuffer getMessageBody();

    /**
     * Add the ByteBuffer version the httpContent to the queue.
     * @param msgBody ByteBuffer version of the httpContent.
     */
    void addMessageBody(ByteBuffer msgBody);

    /**
     * Get the full message body
     * @return complete message body
     */
    List<ByteBuffer> getFullMessageBody();

    /**
     * Check if the queue is empty.
     * @return true or false
     */
    boolean isEmpty();

    /**
     * Get the full message length
     * @return message length
     */
    int getFullMessageLength();

    /**
     * Check if the last message is added to the queue.
     * @return true or false
     */
    boolean isEndOfMsgAdded();

    /**
     * Mark end of message.
     */
    void markMessageEnd();

    /**
     * Set end of message
     * @param endOfMsgAdded indicated using true or false
     */
    void setEndOfMsgAdded(boolean endOfMsgAdded);

    /**
     * Release the allocated netty buffers
     */
    void release();

    /**
     * Allows check if the stream is consumed or not.
     * @return true or false
     */
    boolean isAlreadyRead();

    /**
     * Update the state if already read.
     * @param alreadyRead indicated using true or false
     */
    void setAlreadyRead(boolean alreadyRead);
}
