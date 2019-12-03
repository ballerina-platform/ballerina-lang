/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.data.streaming;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.SQLDBUtils;
import org.ballerinalang.test.util.SQLDBUtils.FileBasedTestDatabase;
import org.ballerinalang.test.util.SQLDBUtils.TestDatabase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This test case tests the scenario of streaming the data from a table converted to XML.
 */
public class TableDataStreamingTestCase extends BaseTest {
    private static BServerInstance serverInstance;
    private final int servicePort = Constant.DEFAULT_HTTP_PORT;
    private TestDatabase testDatabase;

    @BeforeClass(alwaysRun = true)
    private void setup() throws Exception {
        System.setProperty("enableJBallerinaTests", "true");
        setUpDatabase();
        String balFile = Paths.get("src", "test", "resources", "data", "streaming", "streaming_test.bal")
                .toAbsolutePath().toString();
        Map<String, String> envProperties = new HashMap<>(1);
        // Had to increase this to 150 from 100 which worked with BVM. Created an issue: #16846
        envProperties.put("JAVA_OPTS", "-Xms150m -Xmx150m");
        int[] requiredPorts = {9090};
        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(balFile, null, null, envProperties, requiredPorts);
    }

    private void setUpDatabase() throws SQLException {
        String dbScriptPath = Paths
                .get("data", "streaming", "datafiles", "streaming_test_data.sql").toString();
        testDatabase = new FileBasedTestDatabase(SQLDBUtils.DBType.H2, dbScriptPath, SQLDBUtils.DB_DIRECTORY,
                "STREAMING_TEST_DB");
        insertDummyData(testDatabase.getJDBCUrl(), testDatabase.getUsername(), testDatabase.getPassword());
    }

    @Test(description = "Tests streaming a large amount of data from a table, converted to XML")
    public void testStreamingLargeXml() throws Exception {
        HttpResponse response = HttpClientRequest
                .doGet(serverInstance.getServiceURLHttp(servicePort, "dataService/getXmlData"), 60000, responseBuilder);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200);
        Assert.assertEquals(Integer.parseInt(response.getData()), 211288909);
    }

    @Test(description = "Tests streaming a large amount of data from a table, converted to JSON")
    public void testStreamingLargeJson() throws Exception {
        HttpResponse response = HttpClientRequest
                .doGet(serverInstance.getServiceURLHttp(servicePort, "dataService/getJsonData"), 60000,
                        responseBuilder);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200);
        Assert.assertEquals(Integer.parseInt(response.getData()), 208788890);
    }

    @Test(description = "Tests streaming a large amount of data from a table, converted to JSON")
    public void testStreamingLargeJsonAppended() throws Exception {
        HttpResponse response = HttpClientRequest
                .doGet(serverInstance.getServiceURLHttp(servicePort, "dataService/getJsonDataAppended"), 60000,
                        responseBuilder);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200);
        Assert.assertEquals(Integer.parseInt(response.getData()), 208788925);
    }

    @Test(description = "Tests streaming a large amount of data from a table, using setJsonPayload method")
    public void getJosnViaSetJsonPayloadMethod() throws Exception {
        HttpResponse response = HttpClientRequest
                .doGet(serverInstance.getServiceURLHttp(servicePort, "dataService/getJosnViaSetJsonPayloadMethod"),
                        60000, responseBuilder);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200);
        Assert.assertEquals(Integer.parseInt(response.getData()), 208788890);
    }

    @Test(description = "Tests getJsonString method on json taken from a data table")
    public void getJosnViaGetJsonStringMethod() throws Exception {
        HttpResponse response = HttpClientRequest
                .doGet(serverInstance.getServiceURLHttp(servicePort, "dataService/getJosnViaGetJsonStringMethod"),
                        60000, responseBuilder);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200);
        Assert.assertEquals(Integer.parseInt(response.getData()), 20840);
    }

    @Test(description = "Tests the outbound throttling scenario with a slow client")
    public void testStreamingLargeXMLWithSlowClient() throws Exception {
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(servicePort, "dataService/getXmlData"), 60000, slowResponseBuilder);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200);
        Assert.assertEquals(Integer.parseInt(response.getData()), 211288909);
    }

    @Test(description = "Tests the outbound throttling scenario with a slow client")
    public void testStreamingLargeJsonWithSlowClient() throws Exception {
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(servicePort, "dataService/getJsonData"), 60000, slowResponseBuilder);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200);
        Assert.assertEquals(Integer.parseInt(response.getData()), 208788890);
    }

    @AfterClass(alwaysRun = true)
    private void cleanup() throws Exception {
        serverInstance.shutdownServer();
        if (testDatabase != null) {
            testDatabase.stop();
        }
    }

    // This inserts about 212MB of data to the database.
    private static void insertDummyData(String jdbcUrl, String userName, String password) throws SQLException {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, userName, password)) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < 1024; i++) {
                stringBuilder.append("x");
            }
            String strDataEntry = stringBuilder.toString();
            String query = "INSERT INTO Data VALUES (?, ?, ?);";
            conn.setAutoCommit(true);
            PreparedStatement ps = conn.prepareStatement(query);
            for (int i = 0; i < 100000; i++) {
                ps.setInt(1, i);
                ps.setString(2, strDataEntry);
                ps.setString(3, strDataEntry);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * This reads a buffered stream and returns the number of characters.
     */
    private static HttpClientRequest.CheckedFunction<BufferedReader, String> responseBuilder = ((bufferedReader) -> {
        int count = 0;
        while (bufferedReader.read() != -1) {
            count++;
        }
        return String.valueOf(count);
    });

    /**
     * This reads a buffered stream and returns the number of characters.
     */
    private static HttpClientRequest.CheckedFunction<BufferedReader, String> slowResponseBuilder =
            ((bufferedReader) -> {
                int count = 0;
                while (bufferedReader.read() != -1) {
                    if (count % 400000 == 0) {
                        try {
                            new CountDownLatch(1).await(100, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    count++;
                }
                return String.valueOf(count);
            });
}
