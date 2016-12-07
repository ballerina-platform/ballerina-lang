/*
 *   Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.transport.http.netty.passthrough.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.listener.HTTPTransportListener;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.server.HTTPServer;

/**
 * Test class for WebSocket Upgrade
 */
public class PassTroughWebSocketUpgradeTestCase {

    private ListenerConfiguration listenerConfiguration;
    private SenderConfiguration senderConfiguration;
    private HTTPTransportListener httpTransportListener;
    private HTTPServer httpServer;
    private static final String testValue = "Test Message";

    @BeforeClass(groups = "passthroughUPGRADE")
    public void setup() {
        listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setHost(TestUtil.TEST_HOST);
        listenerConfiguration.setId("test-listener");
        listenerConfiguration.setPort(TestUtil.TEST_ESB_PORT);
        senderConfiguration = new SenderConfiguration("passthrough-sender");
        httpTransportListener = TestUtil
                .startCarbonTransport(listenerConfiguration, senderConfiguration, new PassthroughMessageProcessor());
        httpServer = TestUtil.startHTTPServer(TestUtil.TEST_SERVER_PORT, testValue, Constants.TEXT_PLAIN);

    }

    @Test(groups = "passthroughUPGRADE")
    public void test() {

    }

    @AfterClass(groups = "passthroughUPGRADE")
    public void cleaUp() {
        TestUtil.cleanUp(httpTransportListener, httpServer);
    }
}
