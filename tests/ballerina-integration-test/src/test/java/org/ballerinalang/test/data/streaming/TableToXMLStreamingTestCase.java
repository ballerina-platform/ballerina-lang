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

import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.database.utils.SQLDBUtils;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.test.database.utils.SQLDBUtils.DB_DIRECTORY;
import static org.ballerinalang.test.database.utils.SQLDBUtils.TestDatabase;

/**
 * This test case tests the scenario of streaming the data from a table converted to XML.
 */
public class TableToXMLStreamingTestCase {
    private ServerInstance ballerinaServer;
    private TestDatabase testDatabase;
    private BValue[] connectionArgs = new BValue[3];

    @BeforeClass
    private void setup() throws Exception {
        setUpDatabase();
        ballerinaServer = ServerInstance.initBallerinaServer();
        String balFile = Paths.get("src", "test", "resources", "data", "streaming", "xml_streaming_test.bal")
                .toAbsolutePath().toString();
        Map<String, String> envProperties = new HashMap<>(1);
        envProperties.put("JAVA_OPTS", "-Xms100m -Xmx100m");
        ballerinaServer.startBallerinaServer(balFile, envProperties);
    }

    private void setUpDatabase() throws SQLException {
        testDatabase = new SQLDBUtils.FileBasedTestDatabase(SQLDBUtils.DBType.H2,
                "data/streaming/datafiles/streaming_test_data.sql",
                DB_DIRECTORY, "STREAMING_XML_TEST_DB");
        connectionArgs[0] = new BString(testDatabase.getJDBCUrl());
        connectionArgs[1] = new BString(testDatabase.getUsername());
        connectionArgs[2] = new BString(testDatabase.getPassword());
        insertDummyData(testDatabase.getJDBCUrl(), testDatabase.getUsername(), testDatabase.getPassword());
    }

    @Test(description = "Tests streaming a large amount of data from a table, converted to XML. With this test we can"
            + " only verify that the full payload is not built at server side before it is sent to the client.")
    public void testStreamingLargeXML() throws Exception {
        HttpResponse response = HttpClientRequest
                .doGet(ballerinaServer.getServiceURLHttp("dataService/getData"), 60000);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200);
    }

    @AfterClass
    private void cleanup() throws Exception {
         ballerinaServer.stopServer();
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
}
