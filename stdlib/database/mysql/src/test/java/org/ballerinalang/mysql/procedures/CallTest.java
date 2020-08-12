/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.ballerinalang.mysql.procedures;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.mysql.BaseTest;
import org.ballerinalang.mysql.utils.SQLDBUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

/**
 * Test class for JDBC call remote function tests.
 */
public class CallTest {

    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_CALL_QUERY";
    private static final String SQL_SCRIPT = SQLDBUtils.SQL_RESOURCE_DIR + File.separator + "procedures" +
            File.separator + "stored-procedure-test-data.sql";

    static {
        BaseTest.addDBSchema(DB_NAME, SQL_SCRIPT);
    }

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compileOffline(SQLDBUtils.getBalFilesDir("procedures", "call-procedures-test.bal"));
    }

    @Test
    public void testCallWithStringTypes() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testCallWithStringTypes");
        validateDataTableResult(returnVal);
    }

    @Test(dependsOnMethods = "testCallWithStringTypes")
    public void testCallWithStringTypesInParams() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testCallWithStringTypesInParams");
        validateDataTableResult(returnVal);
    }

    @Test(dependsOnMethods = "testCallWithStringTypesInParams")
    public void testCallWithStringTypesReturnsData() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testCallWithStringTypesReturnsData");
        validateDataTableSelectResult(returnVal);
    }

    @Test(dependsOnMethods = "testCallWithStringTypesReturnsData")
    public void testCallWithStringTypesReturnsDataMultiple() {
        BValue[] returnVal = BRunUtil.invoke(result, "testCallWithStringTypesReturnsDataMultiple");
        validateDataTableSelectResult(returnVal);
        validateSimpleStringData(returnVal, 1);
    }

    @Test(dependsOnMethods = "testCallWithStringTypesReturnsDataMultiple")
    public void testCallWithStringTypesOutParams() {
        BValue[] returns = BRunUtil.invoke(result, "testCallWithStringTypesOutParams");
        Assert.assertEquals(returns[0].stringValue(), "test0");
        Assert.assertEquals(returns[1].stringValue(), "test1");
        Assert.assertEquals(returns[2].stringValue(), "a");
        Assert.assertEquals(returns[3].stringValue(), "test2");
        Assert.assertEquals(returns[4].stringValue(), "b");
        Assert.assertEquals(returns[5].stringValue(), "test3");
    }

    @Test(dependsOnMethods = "testCallWithStringTypesOutParams")
    public void testCallWithNumericTypesOutParams() {
        BValue[] returns = BRunUtil.invoke(result, "testCallWithNumericTypesOutParams");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2147483647);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 32767);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 127);
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
        Assert.assertEquals(((BDecimal) returns[5]).decimalValue(), new BigDecimal("1234.56"));
        Assert.assertEquals(((BDecimal) returns[6]).decimalValue(), new BigDecimal("1234.56"));
        Assert.assertEquals(((BFloat) returns[7]).floatValue(), 1234.56, 0.01);
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 1234.56, 0.01);
        Assert.assertEquals(((BFloat) returns[9]).floatValue(), 1234.56, 0.01);
    }

    private void validateSimpleStringData(BValue[] returns, int index) {
        SQLDBUtils.assertNotError(returns[index]);
        Assert.assertTrue(returns[index] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[index]).getMap();
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(((BString) result.get("varchar_type")).stringValue(), "test0");
    }

    private void validateDataTableResult(BValue[] returns) {
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 6);
        Assert.assertEquals(((BString) result.get("varchar_type")).stringValue(), "test1");
        Assert.assertEquals(((BString) result.get("charmax_type")).stringValue(), "test2");
        Assert.assertEquals(((BString) result.get("char_type")).stringValue(), "c");
        Assert.assertEquals(((BString) result.get("charactermax_type")).stringValue(), "test3");
        Assert.assertEquals(((BString) result.get("character_type")).stringValue(), "d");
        Assert.assertEquals(((BString) result.get("nvarcharmax_type")).stringValue(), "test4");
    }

    private void validateDataTableSelectResult(BValue[] returns) {
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 6);
        Assert.assertEquals(((BString) result.get("varchar_type")).stringValue(), "test0");
        Assert.assertEquals(((BString) result.get("charmax_type")).stringValue(), "test1");
        Assert.assertEquals(((BString) result.get("char_type")).stringValue(), "a");
        Assert.assertEquals(((BString) result.get("charactermax_type")).stringValue(), "test2");
        Assert.assertEquals(((BString) result.get("character_type")).stringValue(), "b");
        Assert.assertEquals(((BString) result.get("nvarcharmax_type")).stringValue(), "test3");
    }

}
