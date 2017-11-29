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

package org.ballerinalang.composer.service.workspace.composerapi;

import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.composer.service.workspace.composerapi.endpoint.PetStoreEp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.msf4j.MicroservicesRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLException;


/**
 * Test class for ws client. This test case is written against the PetStoreEp
 *
 */
public class PetStoreWSClientTest {
    private static final Logger log = LoggerFactory.getLogger(PetStoreEp.class);
    private final String host = "localhost";
    private final String port = "8080";
    private JsonParser parser = new JsonParser();
    private String petStoreUrl = "ws://" + host + ":" + port + "/pet-store";
    private MicroservicesRunner microservicesRunner = new MicroservicesRunner();
    /**
     * Starts the Pet-Store Service.
     */
    @BeforeClass
    public void startService() {
        log.info("------------------WebSocket Deployment Test----------------------");
        microservicesRunner.deployWebSocketEndpoint(new PetStoreEp());
        microservicesRunner.start();
    }

    /**
     * Data provider for request handler of composer API test cases.
     *
     * @return List of test cases for the request handler of Composer API
     */
    @DataProvider(name = "ComposerApiSamples")
    public Object[][] composerapiSamples() throws IOException {
        String testFilesPath = "samples" + File.separator + "composer-api-ws" + File.separator;
        ClassLoader classLoader = getClass().getClassLoader();
        URL inputFolderUrl = classLoader.getResource(testFilesPath + "request" + File.separator);
        List<String> fileNames = new ArrayList<>();
        if (null != inputFolderUrl) {
            File inputFolder = new File(inputFolderUrl.getFile());
            if (null != inputFolder.listFiles()) {
                for (File balFile : inputFolder.listFiles()) {
                    fileNames.add(this.removeExtension(balFile.getName()));
                }
            }
        }

        return fileNames.stream().map(fileName -> {
            try {
                URL inputUrl = classLoader.getResource(testFilesPath + "request" + File.separator + fileName +
                        ".json");
                URL outputUrl = classLoader.getResource(testFilesPath + "response" + File.separator + fileName +
                        ".json");
                if (null != inputUrl && null != outputUrl) {
                    String inputFileContent =
                            FileUtils.readFileToString(new File(inputUrl.getFile()), Charset.defaultCharset());
                    String outputFileContent =
                            FileUtils.readFileToString(new File(outputUrl.getFile()), Charset.defaultCharset());
                    return new Object[]{inputFileContent, outputFileContent};
                } else {
                    return new Object[]{null, null};
                }
            } catch (IOException e) {
                return new Object[]{null, null};
            }
        }).filter(i -> null != i[0])
                .toArray(Object[][]::new);
    }

    /**
     * Test @JsonNotification with a valid json content
     * @throws InterruptedException Exception when invoking the web socket service
     * @throws SSLException Exception when invoking the web socket service
     * @throws URISyntaxException Exception when invoking the web socket service
     */
    @Test(description = "Testing @JsonNotification with a valid json request")
    public void executeRequestForInvalidJson() throws InterruptedException, SSLException, URISyntaxException {
        WebSocketClient petStoreClient = new WebSocketClient(petStoreUrl);
        Assert.assertTrue(petStoreClient.handhshake());
        String requestedContent = "{\"jsonrpc\": \"2.0\" , \"method\": \"dogs/notifyAvailabilityOfDogs\", " +
                "  \"params\": [Boxer, ShihTzu]}";
        petStoreClient.sendText(requestedContent);
        Thread.sleep(100);
    }

    /**
     * Test @JsonNotification with a invalid json content
     * @throws InterruptedException Exception when invoking the web socket service
     * @throws SSLException Exception when invoking the web socket service
     * @throws URISyntaxException Exception when invoking the web socket service
     */
    @Test(description = "Testing @JsonNotification with an invalid json request")
    public void executeRequestForValidJson() throws InterruptedException, SSLException, URISyntaxException {
        WebSocketClient petStoreClient = new WebSocketClient(petStoreUrl);
        Assert.assertTrue(petStoreClient.handhshake());
        String requestedContent = "{\"jsonrpc\": , \"method\": \"dogs/notifyAvailabilityOfDogs\", \"params\": " +
                "[Boxer, ShihTzu]}";
        petStoreClient.sendText(requestedContent);
        Thread.sleep(100);
        petStoreClient.getTextReceived();
    }

    /**
     * Test @JsonRequest with valid and invalid json content
     * @param requestContent The request content sent from the composer.
     * @param responseContent The response expected from the composer api service.
     * @throws InterruptedException Exception when invoking the web socket service
     * @throws SSLException Exception when invoking the web socket service
     * @throws URISyntaxException Exception when invoking the web socket service
     */
    @Test(dataProvider = "ComposerApiSamples", description = "Testing @JsonRequest which has a response")
    public void executeRequest(String requestContent, String responseContent) throws InterruptedException,
            SSLException, URISyntaxException {
        WebSocketClient petStoreClient = new WebSocketClient(petStoreUrl);
        //Test handshake
        Assert.assertTrue(petStoreClient.handhshake());
        petStoreClient.sendText(requestContent);
        Thread.sleep(100);
        String receivedContent = petStoreClient.getTextReceived();
        Assert.assertTrue(parser.parse(receivedContent).equals(parser.parse(responseContent)),
                "Invalid response received." + "\nRequested: " + requestContent + "\nExpected: " + responseContent +
                        "\nActual: " + receivedContent);
    }
    
    /**
     * Stops all microservices.
     */
    @AfterClass
    public void stopService() {
        microservicesRunner.stop();
    }

    /**
     * Removes the extension of a file name.
     * @param filename The name of the file.
     * @return The file name without the extension.
     */
    private String removeExtension(String filename) {
        int extensionPos = filename.lastIndexOf(".");
        int lastUnixPos = filename.lastIndexOf("/");
        int lastWindowsPos = filename.lastIndexOf("\\");
        int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);
        int index = lastSeparator > extensionPos ? -1 : extensionPos;

        if (-1 == index) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }
}
