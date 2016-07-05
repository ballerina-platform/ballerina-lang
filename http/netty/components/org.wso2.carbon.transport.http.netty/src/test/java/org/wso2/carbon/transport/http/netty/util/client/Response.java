/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.util.client;

import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A class which holds the HTTPResponse
 */
public class Response {
    private static final Logger logger = LoggerFactory.getLogger(Response.class);

    public HttpResponse httpResponse;

    public Queue<ByteBuffer> contentQueue = new LinkedList<>();

    protected Response(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    protected void addContent(ByteBuffer httpContent) {
        contentQueue.add(httpContent);
    }

    public String getStringValue() {

        int size = contentQueue.stream().mapToInt(byteBuffer -> byteBuffer.limit()).sum();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        contentQueue.forEach(contentQueue -> byteBuffer.put(contentQueue));
        byteBuffer.flip();
        logger.info("MesssageReceived#################" + new String(byteBuffer.array()));
        return new String(byteBuffer.array());

    }
}
