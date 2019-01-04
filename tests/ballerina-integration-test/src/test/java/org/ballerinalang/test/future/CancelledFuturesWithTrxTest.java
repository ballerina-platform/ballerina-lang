/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.future;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.SQLDBUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static org.ballerinalang.test.util.SQLDBUtils.DB_DIRECTORY;


/**
 * Class to test functionality of transactions in SQL with cancelled futures.
 */
public class CancelledFuturesWithTrxTest extends BaseTest {

    private SQLDBUtils.TestDatabase testDatabase;

    @BeforeClass
    public void setup() {
        String queries = "CREATE TABLE IF NOT EXISTS Customers(\n" +
                "  customerId INTEGER NOT NULL IDENTITY,\n" +
                "  firstName  VARCHAR(300),\n" +
                "  lastName  VARCHAR(300),\n" +
                "  registrationID INTEGER,\n" +
                "  creditLimit DOUBLE,\n" +
                "  country  VARCHAR(300),\n" +
                "  PRIMARY KEY (customerId)\n" +
                ");";
        testDatabase = new SQLDBUtils.FileBasedTestDatabase(SQLDBUtils.DBType.H2, queries, DB_DIRECTORY,
                "TEST_SQL_CONNECTOR_TR");
    }

    @Test
    public void testTransactionRollbackOnCancelledFuture() throws BallerinaTestException {
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources" +
                File.separator + "future" + File.separator + "transaction_test.bal").getAbsolutePath();

        BMainInstance bMainInstance = new BMainInstance(balServer);
        LogLeecher logLeecher = new LogLeecher("Marked initiated transaction for abortion",
                                                LogLeecher.LeecherType.ERROR);

        bMainInstance.runMain(balFile, new String[]{"--experimental"}, new String[0], new LogLeecher[]{logLeecher});
        logLeecher.waitForText(10000);
    }

    @AfterSuite
    public void cleanup() throws BallerinaTestException {
        if (testDatabase != null) {
            testDatabase.stop();
        }
    }
}
