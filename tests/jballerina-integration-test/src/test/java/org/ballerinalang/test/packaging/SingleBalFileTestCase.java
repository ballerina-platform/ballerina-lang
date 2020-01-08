/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.packaging;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.SQLDBUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.ballerinalang.test.packaging.PackerinaTestUtils.deleteFiles;

/**
 * Test case for running bal files.
 */
public class SingleBalFileTestCase extends BaseTest {

    private BMainInstance balClient;
    private Path serverBreLibPath;
    private static final String DB_NAME_HSQL = "JDBC_SINGLE_BAL_FILE_TEST_DB";
    private SQLDBUtils.TestDatabase testDatabase;
    private static final String JDBC_URL = "jdbc:hsqldb:file:" + SQLDBUtils.DB_DIRECTORY + DB_NAME_HSQL +
            ";hsqldb.lock_file=false";
    private File hsqlDBJar;

    /**
     * Create ballerina client.
     *
     * @throws BallerinaTestException When creating the ballerina client.
     */
    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        balClient = new BMainInstance(balServer);
        testDatabase = new SQLDBUtils
                .FileBasedTestDatabase(SQLDBUtils.DBType.HSQLDB,
                                       Paths.get("src", "test", "resources", "packaging", "singleBalFile", "sql",
                                                 "resources", "select_test_data.sql").toString(),
                                       SQLDBUtils.DB_DIRECTORY, DB_NAME_HSQL);
        serverBreLibPath = Paths.get(balServer.getServerHome(), "bre", "lib");
        Path breLibPath = Paths.get("build", "libs");
        FilenameFilter getHsqlDBJar = new FilenameFilter() {
            public boolean accept(File directory, String filename) {
                return filename.startsWith("hsqldb");
            }
        };

        File[] files = breLibPath.toFile().listFiles(getHsqlDBJar);
        hsqlDBJar = files[0];
        Files.copy(hsqlDBJar.toPath(), Paths.get(serverBreLibPath.toString(), hsqlDBJar.getName()));
    }

    /**
     * Run bal using jdbc module.
     *
     * @throws BallerinaTestException When running commands.
     */
    @Test(description = "Test building and running TestProject")
    public void testSingleBalFileWithJDBCUsage() throws BallerinaTestException {
        String sqlBalFile = "jdbc_select.bal";
        Path testProjectPath = Paths.get("src", "test", "resources", "packaging", "singleBalFile", "sql")
                .toAbsolutePath();
        // Run and see output
        String msg = "Customer name is :Peter";
        LogLeecher fooRunLeecher = new LogLeecher(msg);
        balClient.runMain("run", new String[]{sqlBalFile, JDBC_URL}, new HashMap<>(), new String[0],
                          new LogLeecher[]{fooRunLeecher}, testProjectPath.toString());
        fooRunLeecher.waitForText(10000);
    }

    @AfterClass
    private void cleanup() throws Exception {
        if (testDatabase != null) {
            testDatabase.stop();
        }
        deleteFiles(Paths.get(serverBreLibPath.toString(), hsqlDBJar.getName()));
    }

}
