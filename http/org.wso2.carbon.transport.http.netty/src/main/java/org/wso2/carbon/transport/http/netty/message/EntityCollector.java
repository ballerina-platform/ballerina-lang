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

    void addHttpContent(HttpContent httpContent);
    HttpContent getHttpContent();
    ByteBuffer getMessageBody();
    List<ByteBuffer> getFullMessageBody();
    boolean isEmpty();
    int getFullMessageLength();
    boolean isEndOfMsgAdded();
    void addMessageBody(ByteBuffer msgBody);
    void markMessageEnd();
    void setEndOfMsgAdded(boolean endOfMsgAdded);
    void release();
    boolean isAlreadyRead();
    void setAlreadyRead(boolean alreadyRead);
}
