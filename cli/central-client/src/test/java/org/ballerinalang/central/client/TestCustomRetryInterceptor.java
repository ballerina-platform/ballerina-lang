/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.central.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.util.Locale;

/**
 * Test cases to test retry logic.
 */

public class TestCustomRetryInterceptor {
    public static final String BASE_PATH = "/";
    private static final Boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.getDefault())
            .contains("win");
    public static final int PORT = 57803;
    private MockWebServer mockWebServer;
    private OkHttpClient centralHttpClient;
    private static final String ACCESS_TOKEN = "273cc9f6-c333-36ab-aa2q-f08e9513ff5y";
    private ByteArrayOutputStream console;


    @BeforeClass
    public void setUp() {
        this.console = new ByteArrayOutputStream();
        PrintStream outStream = new PrintStream(this.console);
        CentralAPIClient centralAPIClient = new CentralAPIClient("https://localhost:9090/registry",
                null, ACCESS_TOKEN, true, 3, outStream);
        centralHttpClient = centralAPIClient.getClient();
    }

    private String readOutput() {
        String output;
        output = this.console.toString();
        this.console.reset();
        return output;
    }

    @Test(description = "Return failure response all the time.")
    public void testRetries() throws IOException {
        String expectedOutputPrefix =  "* Retrying request to http://localhost:57803/ " +
                "due to 500 response code. Retry attempt: ";
        mockWebServer = new MockWebServer();
        mockWebServer.start(PORT);
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR));
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR));
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR));
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR));
        Request request = new Request.Builder()
                .url(mockWebServer.url(BASE_PATH))
                .build();
        Response response = centralHttpClient.newCall(request).execute();
        String consoleOutput = readOutput();
        if (isWindows) {
            consoleOutput = consoleOutput.replaceAll(mockWebServer.url(BASE_PATH).host(), "localhost");
        }
        Assert.assertEquals(response.code(), HttpURLConnection.HTTP_INTERNAL_ERROR);
        Assert.assertTrue(consoleOutput.contains(expectedOutputPrefix + 1), consoleOutput);
        Assert.assertTrue(consoleOutput.contains(expectedOutputPrefix + 2), consoleOutput);
        Assert.assertTrue(consoleOutput.contains(expectedOutputPrefix + 3), consoleOutput);
        Assert.assertFalse(consoleOutput.contains(expectedOutputPrefix + 4), consoleOutput);
        mockWebServer.close();
    }

    @Test(description = "Return successful response before retry maximum is reached.", dependsOnMethods = "testRetries")
    public void testRetries2() throws IOException {
        String expectedOutputPrefix =  "* Retrying request to http://localhost:57803/ " +
                "due to 502 response code. Retry attempt: ";
        mockWebServer = new MockWebServer();
        mockWebServer.start(PORT);
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_GATEWAY));
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_GATEWAY));
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK));

        Request request = new Request.Builder()
                .url(mockWebServer.url(BASE_PATH))
                .build();
        Response response = centralHttpClient.newCall(request).execute();
        String consoleOutput = readOutput();
        if (isWindows) {
            consoleOutput = consoleOutput.replaceAll(mockWebServer.url(BASE_PATH).host(), "localhost");
        }
        Assert.assertEquals(response.code(), HttpURLConnection.HTTP_OK);
        Assert.assertTrue(consoleOutput.contains(expectedOutputPrefix + 1), consoleOutput);
        Assert.assertTrue(consoleOutput.contains(expectedOutputPrefix + 2), consoleOutput);
        Assert.assertFalse(consoleOutput.contains(expectedOutputPrefix + 3), consoleOutput);
        mockWebServer.close();
    }
}
