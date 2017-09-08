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

package org.ballerinalang.composer.service.workspace.tryit;

import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.example.StockQuoteService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for http try-it client. Test cases are written against
 * <a href="https://github.com/wso2/msf4j/blob/v2.1.0/samples/stockquote/fatjar/src/main/java/org/wso2/msf4j/example/
 * StockQuoteService.java">Stock Quote Service</a>.
 */
public class HttpClientTryItTest {
    private MicroservicesRunner microservicesRunner;
    private JsonParser parser = new JsonParser();
    private int microservicePort = 9595;
    
    /**
     * Data provider for try it test cases.
     *
     * @return List of try it test cases.
     */
    @DataProvider(name = "tryItSamples")
    public Object[][] tryItSamples() throws IOException {
        String testFilesPath = "samples" + File.separator + "tryit" + File.separator;
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
                URL inputBalUrl = classLoader.getResource(testFilesPath + "request" + File.separator + fileName +
                                                                                                            ".json");
                URL outputSwaggerUrl = classLoader.getResource(testFilesPath + "response" + File.separator + fileName +
                                                               ".json");
                if (null != inputBalUrl && null != outputSwaggerUrl) {
                    String inputFileContent =
                            FileUtils.readFileToString(new File(inputBalUrl.getFile()), Charset.defaultCharset());
                    String outputFileContent =
                            FileUtils.readFileToString(new File(outputSwaggerUrl.getFile()), Charset.defaultCharset());
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
     * Starts the stockquote service.
     */
    @BeforeClass
    public void startService() {
        this.microservicesRunner = new MicroservicesRunner(this.microservicePort);
        this.microservicesRunner.deploy(new StockQuoteService()).start();
    }
    
    /**
     * Execute http try-it test cases.
     * @param requestContent The request content sent from the composer.
     * @param responseContent The response expected from the try-it service.
     * @throws TryItException Error during executing try-it service.
     */
    @Test(dataProvider = "tryItSamples")
    public void executeRequest(String requestContent, String responseContent) throws TryItException {
        HttpTryItClient httpTryItClient = new HttpTryItClient("localhost:" + Integer.toString(this.microservicePort),
                                                                                                        requestContent);
        httpTryItClient.disableTimeDurationCalculation();
        String receivedResponse = httpTryItClient.execute();
    
        Assert.assertTrue(parser.parse(receivedResponse).equals(parser.parse(responseContent)),
                "Invalid response received." +
                "\nRequested: " + parser.parse(requestContent).toString() +
                "\nExpected: " + parser.parse(responseContent).toString() +
                "\nActual: " + parser.parse(receivedResponse).toString());
    }
    
    /**
     * Stops all microservices.
     */
    @AfterClass
    public void stopService() {
        this.microservicesRunner.stop();
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
