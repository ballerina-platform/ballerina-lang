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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.listener.HTTPTransportListener;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.websocket.client.WebSocketClient;

import java.net.URISyntaxException;

import static org.testng.Assert.assertTrue;

/**
 * Test class for WebSocket Upgrade
 */
public class PassTroughWebSocketUpgradeTestCase {

    private ListenerConfiguration listenerConfiguration;
    private SenderConfiguration senderConfiguration;
    private HTTPTransportListener httpTransportListener;
    private WebSocketClient client = new WebSocketClient();

    @BeforeClass(groups = "passthroughUPGRADE")
    public void setup() {
        listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setHost(TestUtil.TEST_HOST);
        listenerConfiguration.setId("test-listener");
        listenerConfiguration.setPort(TestUtil.TEST_ESB_PORT);
        senderConfiguration = new SenderConfiguration("passthrough-sender");
        httpTransportListener = TestUtil
                .startCarbonTransport(listenerConfiguration, senderConfiguration, new PassthroughMessageProcessor());
    }

    @Test(groups = "passthroughUPGRADE")
    public void testHandshake() throws URISyntaxException {
        try {
            assertTrue(client.handhshake(TestUtil.TEST_HOST, TestUtil.TEST_ESB_PORT));
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }


    @AfterClass(groups = "passthroughUPGRADE")
    public void cleaUp() {
        TestUtil.shutDownCarbonTransport(httpTransportListener);
    }
}
