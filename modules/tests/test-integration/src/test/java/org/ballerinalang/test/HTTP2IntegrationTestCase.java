/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.listener.ServerConnectorExecutionListener;
import org.ballerinalang.test.util.http2.HTTP2Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Parent test class of all http2 test and this will provide basic functionality for integration test.
 */
public abstract class HTTP2IntegrationTestCase {
    private ServerInstance serverInstance;
    private static final Logger log = LoggerFactory.getLogger(HTTP2IntegrationTestCase.class);
    public HTTP2Client http2Client = null;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        // assigning the running server instance started by ServerConnectorExecutionListener
        serverInstance = (ServerInstance) ServerConnectorExecutionListener.getServerInstance();
        try {
            http2Client = new HTTP2Client(false, "localhost", ServerConnectorExecutionListener.HTTP_PORT);
        } catch (Exception e) {
            log.error("Server failed to start. " + e.getMessage(), e);
            throw new RuntimeException("Server failed to start. " + e.getMessage(), e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() {
        serverInstance = null;
        if (http2Client != null) {
            http2Client.close();
        }
    }

    /**
     * Get content from response
     *
     * @param msg HTTP Response
     * @return content
     */
    protected String getResponse(FullHttpResponse msg) {
        ByteBuf content = msg.content();
        if (content.isReadable()) {
            int contentLength = content.readableBytes();
            byte[] arr = new byte[contentLength];
            content.readBytes(arr);
            return new String(arr, 0, contentLength, CharsetUtil.UTF_8);
        }
        return null;
    }
}
