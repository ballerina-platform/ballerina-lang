/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.connectors.sql;

import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

public class SQLConnectorInitTest {

    CompileResult result;
    private static final String DB_NAME = "TEST_SQL_CONNECTOR";

    @BeforeClass
    public void setup() {
        result = BTestUtils.compile("test-src/connectors/sql-connector-init.bal");
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initDatabase(SQLDBUtils.DB_DIRECTORY, DB_NAME, "datafiles/SQLConnectorDataFile.sql");
    }

    @Test(groups = "ConnectorTest", enabled = false)
    public void testConnectorWithDataSource() {
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testConnectorWithDataSource", args);
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @Test(groups = "ConnectorTest" , enabled = false)
    public void testConnectionPoolProperties() {
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testConnectionPoolProperties", args);
        BString retValue = (BString) returns[0];
        final String expected = "Peter";
        Assert.assertEquals(retValue.stringValue(), expected);
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
