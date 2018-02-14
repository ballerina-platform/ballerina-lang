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
package org.ballerinalang.test.types.table;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Class to test table iteration functionality.
 */
public class TableIterationTest {

    CompileResult result;
    private static final String DB_NAME = "TEST_DATA_TABLE__ITR_DB";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/table-iteration.bal");
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initDatabase(SQLDBUtils.DB_DIRECTORY, DB_NAME, "datafiles/sql/TableIterationTestData.sql");
    }

    @Test(groups = "TableIterTest", description = "Check accessing data using foreach iteration")
    public void testForEachInTableWithStmt() {
        BValue[] returns = BRunUtil.invoke(result, "testForEachInTableWithStmt");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 400.25);
        Assert.assertEquals(returns[3].stringValue(), "John");
    }

    @Test(groups = "TableIterTest", description = "Check accessing data using foreach iteration")
    public void testForEachInTable() {
        BValue[] returns = BRunUtil.invoke(result, "testForEachInTable");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 400.25);
        Assert.assertEquals(returns[3].stringValue(), "John");
    }

    @Test(groups = "TableIterTest", description = "Check accessing data using foreach iteration")
    public void testCountInTable() {
        BValue[] returns = BRunUtil.invoke(result, "testCountInTable");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
    }

    @Test(groups = "TableIterTest", description = "Check accessing data using foreach iteration")
    public void testFilterTable() {
        BValue[] returns = BRunUtil.invoke(result, "testFilterTable");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 10);
    }

    @Test(groups = "TableIterTest", description = "Check accessing data using foreach iteration")
    public void testFilterTableWithCount() {
        BValue[] returns = BRunUtil.invoke(result, "testFilterTableWithCount");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test(groups = "TableIterTest", description = "Check accessing data using foreach iteration")
    public void testMapTable() {
        BValue[] returns = BRunUtil.invoke(result, "testMapTable");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "John");
        Assert.assertEquals(((BStringArray) returns[0]).get(1), "Anne");
        Assert.assertEquals(((BStringArray) returns[0]).get(2), "Mary");
        Assert.assertEquals(((BStringArray) returns[0]).get(3), "Peter");
    }

    @Test(groups = "TableIterTest", description = "Check accessing data using foreach iteration")
    public void testMapWithFilterTable() {
        BValue[] returns = BRunUtil.invoke(result, "testMapWithFilterTable");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "Peter");
    }

    @Test(groups = "TableIterTest", description = "Check accessing data using foreach iteration")
    public void testFilterWithMapTable() {
        BValue[] returns = BRunUtil.invoke(result, "testFilterWithMapTable");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "Peter");
    }

    @Test(groups = "TableIterTest", description = "Check accessing data using foreach iteration")
    public void testFilterWithMapAndCountTable() {
        BValue[] returns = BRunUtil.invoke(result, "testFilterWithMapAndCountTable");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = "TableIterTest", description = "Check accessing data using foreach iteration")
    public void testMinWithTable() {
        BValue[] returns = BRunUtil.invoke(result, "testMinWithTable");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 100.25);
    }

    @Test(groups = "TableIterTest", description = "Check accessing data using foreach iteration")
    public void testMaxWithTable() {
        BValue[] returns = BRunUtil.invoke(result, "testMaxWithTable");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 600.25);
    }

    @Test(groups = "TableIterTest", description = "Check accessing data using foreach iteration")
    public void testSumWithTable() {
        BValue[] returns = BRunUtil.invoke(result, "testSumWithTable");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 1701.0);
    }

    @Test(groups = "TableIterTest", description = "Check accessing data using foreach iteration")
    public void testAverageWithTable() {
        BValue[] returns = BRunUtil.invoke(result, "testAverageWithTable");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 425.25);
    }

    @Test(dependsOnGroups = "TableIterTest")
    public void testCloseConnectionPool() {
        BValue[] returns = BRunUtil.invoke(result, "testCloseConnectionPool");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
