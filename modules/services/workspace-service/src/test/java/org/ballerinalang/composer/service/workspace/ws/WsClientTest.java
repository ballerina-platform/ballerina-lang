/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.composer.service.workspace.ws;

import com.google.gson.JsonParser;
import org.ballerinalang.composer.service.workspace.ws.endpoint.*;
import org.ballerinalang.composer.service.workspace.ws.exception.BallerinaWebSocketException;
import org.ballerinalang.composer.service.workspace.ws.handlers.ShowPets;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.MicroservicesRunner;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;


/**
 * Test class for ws client. This test case is written against the PetStore
 *
 */
public class WsClientTest {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PetStore.class);
    private final String host = "localhost";
    private final String port = "8080";
    private final int sleepTime = 100;
    private JsonParser parser = new JsonParser();
    private String petStoreUrl = "ws://" + host + ":" + port + "/pet-store";
    private MicroservicesRunner microservicesRunner = new MicroservicesRunner();
    /**
     * Starts the Pet-Store Service.
     */
    @BeforeClass
    public void startService() {
        LOGGER.info(System.lineSeparator() +
                "--------------------------------WebSocket Deployment Test--------------------------------");
        microservicesRunner.deployWebSocketEndpoint(new PetStore() );
        microservicesRunner.start();
    }

    @Test(description = "Testing the pet-store endpoint the message sent by client for text")
    public void testReply() throws InterruptedException, SSLException, URISyntaxException, BallerinaWebSocketException {
        WebSocketClient petStoreClient = new WebSocketClient(petStoreUrl);
        //Test handshake
        Assert.assertTrue(petStoreClient.handhshake());
        // Register ShowPets class handler
        ApiHandlerRegistry.getInstance().registerHandler(ShowPets.class);
        //Test string
        String requestedContent = "{\"jsonrpc\": \"2.0\", \"method\": \"showPets\", \"params\": " +
                "{\"breed\":\"Golden Retriever\",\"color\":\"Black\"}, id: 1}";
        petStoreClient.sendText(requestedContent);
        Thread.sleep(sleepTime);
        String receivedContent = petStoreClient.getTextReceived();
        String responseContent = "{\"jsonrpc\":\"2.0\", \"id\":1,\"result\":[\"Rottweiler\",\"Labrador\", \"Golden Retriever\"]}";
        Assert.assertTrue(parser.parse(receivedContent).equals(parser.parse(responseContent)),
                "Invalid response received." +
                        "\nRequested: " + parser.parse(requestedContent).toString() +
                        "\nExpected: " + parser.parse(responseContent).toString() +
                        "\nActual: " + parser.parse(receivedContent).toString());
    }
    
    /**
     * Stops all microservices.
     */
    @AfterClass
    public void stopService() {
        microservicesRunner.stop();
    }
}
