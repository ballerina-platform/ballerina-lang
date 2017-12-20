/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.connectionpool;

import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.passthrough.PassthroughMessageProcessorListener;
import org.wso2.transport.http.netty.util.TestUtil;
import org.wso2.transport.http.netty.util.server.HttpServer;
import org.wso2.transport.http.netty.util.server.initializers.SendChannelIDServerInitializer;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Tests for connection pool implementation.
 */
public class ConnectionPoolTestCase {

    private static Logger logger = LoggerFactory.getLogger(ConnectionPoolTestCase.class);

    private HttpServer httpServer;
    private List<ServerConnector> serverConnectors;
    private URI baseURI = URI.create(String.format("http://%s:%d", "localhost", TestUtil.SERVER_CONNECTOR_PORT));
    private ExecutorService executor = Executors.newFixedThreadPool(2);

    @BeforeClass
    public void setup() {
        TransportsConfiguration transportsConfiguration = TestUtil.getConfiguration(
                "/simple-test-config" + File.separator + "netty-transports.yml");

        httpServer = TestUtil.startHTTPServer(TestUtil.HTTP_SERVER_PORT, new SendChannelIDServerInitializer());
        serverConnectors = TestUtil.startConnectors(transportsConfiguration,
                new PassthroughMessageProcessorListener(transportsConfiguration));
    }

    @Test
    public void testConnectionReuseForProxy() {
        try {
            Future<String> requestOneResponse;
            Future<String> requestThreeResponse;

            ClientWorker clientWorkerOne = new ClientWorker();
            ClientWorker clientWorkerTwo = new ClientWorker();
            ClientWorker clientWorkerThree = new ClientWorker();

            requestOneResponse = executor.submit(clientWorkerOne);

            // While the fist request is being processed by the back-end,
            // we send the second request which forces the client connector to
            // create a new connection.
            Thread.sleep(2500);
            executor.submit(clientWorkerTwo);
            assertNotNull(requestOneResponse.get());

            requestThreeResponse = executor.submit(clientWorkerThree);

            assertEquals(requestOneResponse.get(), requestThreeResponse.get());
        } catch (Exception e) {
            TestUtil.handleException("IOException occurred while running passthroughPostTest", e);
        }
    }

    @AfterClass
    public void cleanUp() throws ServerConnectorException {
        TestUtil.cleanUp(serverConnectors, httpServer);
    }

    private class ClientWorker implements Callable<String> {

        private String response;

        @Override
        public String call() throws Exception {
            try {
                HttpURLConnection urlConn = TestUtil
                        .request(baseURI, "/", HttpMethod.POST.name(), true);
                response = TestUtil.getContent(urlConn);
            } catch (IOException e) {
                logger.error("Couldn't get the response", e);
            }

            return response;
        }
    }
}
