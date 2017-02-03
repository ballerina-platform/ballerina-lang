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

package org.wso2.carbon.transport.http.netty.encoding;

import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.listener.HTTPTransportListener;
import org.wso2.carbon.transport.http.netty.util.TestUtil;
import org.wso2.carbon.transport.http.netty.util.server.HTTPServer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import static org.testng.AssertJUnit.assertEquals;

public class ContentEncodingTestCase {

    private HTTPTransportListener httpTransportListener;

    private TransportsConfiguration configuration;

    private HTTPServer httpServer;
    private URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 8490));

    private static final Logger log = LoggerFactory.getLogger(ContentEncodingTestCase.class);

    @BeforeClass
    public void setup() {
        configuration = YAMLTransportConfigurationBuilder
                .build("src/test/resources/simple-test-config/netty-transports.yml");
        httpTransportListener = TestUtil.startCarbonTransport(configuration, new ContentReadingProcessor());
        httpServer = TestUtil.startHTTPServer(TestUtil.TEST_SERVER_PORT);
    }

    @Test
    public void messageEchoingFromProcessorTestCase() {
        String testValue = "Test Message";
        try {
            HttpURLConnection urlConn = TestUtil.request(baseURI, "/", HttpMethod.POST.name(), true);
            //TestUtil.setHeader(urlConn, Constants.ACCEPT_ENCODING, Constants.ENCODING_GZIP);
            TestUtil.writeContent(urlConn, testValue);
            assertEquals(200, urlConn.getResponseCode());
            String content = TestUtil.getContent(urlConn);
            urlConn.disconnect();
        } catch (IOException e) {
            log.error("Error while running the test", e);
        }

    }

    @AfterClass
    public void cleanUp() {
        TestUtil.cleanUp(httpTransportListener, httpServer);
    }
}
