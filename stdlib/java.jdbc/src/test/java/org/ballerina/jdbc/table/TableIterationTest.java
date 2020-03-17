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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerina.jdbc.table;

import org.ballerina.jdbc.utils.SQLDBUtils;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;

/**
 * Class to test table iteration functionality.
 */
//TODO:#16805
public class TableIterationTest {

    private CompileResult result;
    private static final String DB_NAME = "TEST_DATA_TABLE_ITR_DB";
    private SQLDBUtils.TestDatabase testDatabase;
    private static final String JDBC_URL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + DB_NAME;
    private BValue[] args = { new BString(JDBC_URL) };

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(Paths.get("test-src", "table", "table_iteration_test.bal").toString());
        String dbScript = Paths.get("datafiles", "sql", "table", "table_iteration_test_data.sql").toString();
        testDatabase = new SQLDBUtils.FileBasedTestDatabase(SQLDBUtils.DBType.H2, dbScript,
                                                            SQLDBUtils.DB_DIRECTORY, DB_NAME);
    }

    @Test(groups = "TableIterationTest", description = "Check count operation function on table")
    public void testCountInTable() {
        BValue[] returns = BRunUtil.invoke(result, "testCountInTable", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
    }

    @Test(groups = "TableIterationTest", description = "Check filter operation")
    public void testFilterTable() {
        BValue[] returns = BRunUtil.invoke(result, "testFilterTable", args);
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 10);
    }

    @Test(groups = "TableIterationTest", description = "Check filter operation")
    public void testFilterWithAnonymousFuncOnTable() {
        BValue[] returns = BRunUtil.invoke(result, "testFilterWithAnonymousFuncOnTable", args);
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 10);
    }

    @Test(groups = "TableIterationTest", description = "Check filter and count operation")
    public void testFilterTableWithCount() {
        BValue[] returns = BRunUtil.invoke(result, "testFilterTableWithCount", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test(groups = "TableIterationTest", description = "Check accessing data using foreach iteration")
    public void testMapTable() {
        BValue[] returns = BRunUtil.invoke(result, "testMapTable", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "John");
        Assert.assertEquals(((BValueArray) returns[0]).getString(1), "Anne");
        Assert.assertEquals(((BValueArray) returns[0]).getString(2), "Mary");
        Assert.assertEquals(((BValueArray) returns[0]).getString(3), "Peter");
    }

    @Test(groups = "TableIterationTest", description = "Check map with filter operation")
    public void testMapWithFilterTable() {
        BValue[] returns = BRunUtil.invoke(result, "testMapWithFilterTable", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "Peter");
    }

    @Test(groups = "TableIterationTest", description = "Check filter with map operation")
    public void testFilterWithMapTable() {
        BValue[] returns = BRunUtil.invoke(result, "testFilterWithMapTable", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "Peter");
    }

    @Test(groups = "TableIterationTest", description = "Check filter count and map operation")
    public void testFilterWithMapAndCountTable() {
        BValue[] returns = BRunUtil.invoke(result, "testFilterWithMapAndCountTable", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = "TableIterationTest", description = "Check min operation")
    public void testMinWithTable() {
        BValue[] returns = BRunUtil.invoke(result, "testMinWithTable", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 100.25);
    }

    @Test(groups = "TableIterationTest", description = "Check max operation")
    public void testMaxWithTable() {
        BValue[] returns = BRunUtil.invoke(result, "testMaxWithTable", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 600.25);
    }

    @Test(groups = "TableIterationTest", description = "Check sum operation")
    public void testSumWithTable() {
        BValue[] returns = BRunUtil.invoke(result, "testSumWithTable", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 1701.0);
    }

    @Test(groups = "TableIterationTest", description = "Check average operation")
    public void testAverageWithTable() {
        BValue[] returns = BRunUtil.invoke(result, "testAverageWithTable", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 425.25);
    }

    @Test(dependsOnGroups = "TableIterationTest")
    public void testCloseConnectionPool() {
        BValue[] returns = BRunUtil.invoke(result, "testCloseConnectionPool", args);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @AfterSuite
    public void cleanup() {
        if (testDatabase != null) {
            testDatabase.stop();
        }
    }
}
