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

package org.wso2.carbon.transport.http.netty.util;

import org.wso2.carbon.transport.http.netty.listener.NettyListener;
import org.wso2.carbon.transport.http.netty.util.server.HTTPServer;

/**
 * A util class to be used for tests
 */
public class TestUtil {

    public static final int TEST_SERVER_PORT = 9000;
    public static final int TEST_ESB_PORT = 8080;
    public static final String TEST_HOST = "localhost";

    public static final int RESPONSE_WAIT_TIME = 10000;

    public static void cleanUp(NettyListener nettyListener, HTTPServer httpServer) {
        nettyListener.stop();
        httpServer.shutdown();
        try {
            Thread.sleep(TestUtil.RESPONSE_WAIT_TIME);
        } catch (InterruptedException e) {

        }
    }
}
