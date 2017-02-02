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

package org.wso2.carbon.transport.http.netty.passthrough;

import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.listener.HTTPTransportListener;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.server.HTTPServer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * A test class for passthrough transport
 */
public class PassThroughHttpPOSTMethodTestCase {

    protected static final Logger LOGGER = LoggerFactory.getLogger(PassThroughHttpPOSTMethodTestCase.class);

    private HTTPTransportListener httpTransportListener;

    private HTTPServer httpServer;

    private ListenerConfiguration listenerConfiguration;

    private SenderConfiguration senderConfiguration;

    private URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8490));

    //@BeforeClass(groups = "passthroughPost",                        dependsOnGroups = "passthroughGET")
    public void setUp() {
        listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setHost(TestUtil.TEST_HOST);
        listenerConfiguration.setId("test-listener");
        listenerConfiguration.setPort(TestUtil.TEST_ESB_PORT);
        senderConfiguration = new SenderConfiguration("passthrough-sender");
        httpTransportListener = TestUtil
                .startCarbonTransport(listenerConfiguration, senderConfiguration, new PassthroughMessageProcessor());
        httpServer = TestUtil.startHTTPServer(TestUtil.TEST_SERVER_PORT);
    }

    public PassThroughHttpPOSTMethodTestCase() {
        super();
    }

    //@Test(groups = "passthroughPost",  dependsOnGroups = "passthroughGET")
    public void passthroughPOSTTestCase() {
        String testValue = "Test Message";
        try {
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            TestUtil.writeContent(urlConn, testValue);
            String content = TestUtil.getContent(urlConn);
            assertEquals(testValue, content);
            urlConn.disconnect();
        } catch (IOException e) {
            LOGGER.error("IO Exception occurred", e);
            assertTrue(false);
        }

    }
    
    //@AfterClass(groups = "passthroughPost", dependsOnGroups = "passthroughGET")
    public void cleanUp() {
        TestUtil.cleanUp(httpTransportListener, httpServer);
    }

}
