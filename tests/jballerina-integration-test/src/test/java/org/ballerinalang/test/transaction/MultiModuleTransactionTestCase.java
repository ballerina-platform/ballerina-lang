/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.transaction;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Test transaction behavior in multi module scenario.
 */
@Test(groups = "transactions-test")
public class MultiModuleTransactionTestCase extends BaseTest {
    private BMainInstance balClient;
    private String basePath;

    @BeforeClass()
    public void setUp() throws BallerinaTestException {

        basePath = new File(Paths.get("src", "test", "resources", "transaction").toString()).getAbsolutePath();
        balClient = new BMainInstance(balServer);
    }

    @Test()
    public void testImplicitTransactionsModuleImportInMultiPackageBuild() throws BallerinaTestException {
        String module1BuildMsg = "transactionservices";
        String module2BuildMsg = "transactioninitiatordummy";
        LogLeecher module1BuildLeecher = new LogLeecher(module1BuildMsg);
        LogLeecher module2BuildLeecher = new LogLeecher(module2BuildMsg);

        balClient.runMain("build", new String[]{"-a", "--experimental"}, new HashMap<>(), new String[]{},
                new LogLeecher[]{module1BuildLeecher, module2BuildLeecher}, basePath);
        module1BuildLeecher.waitForText(5000);
        module2BuildLeecher.waitForText(5000);
    }
}
