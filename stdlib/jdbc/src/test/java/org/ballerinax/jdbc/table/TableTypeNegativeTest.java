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
package org.ballerinax.jdbc.table;

import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinax.jdbc.utils.SQLDBUtils;
import org.ballerinax.jdbc.utils.SQLDBUtils.DBType;
import org.ballerinax.jdbc.utils.SQLDBUtils.FileBasedTestDatabase;
import org.ballerinax.jdbc.utils.SQLDBUtils.TestDatabase;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Class to test functionality of tables.
 */
public class TableTypeNegativeTest {

    private CompileResult resultNegative;
    private static final String DB_NAME_H2 = "TEST_DATA_TABLE_H2";
    private TestDatabase testDatabase;
    private static final String JDBC_URL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + DB_NAME_H2;
    private BValue[] args = { new BString(JDBC_URL) };

    @BeforeClass
    public void setup() {
        testDatabase = new FileBasedTestDatabase(DBType.H2,
                Paths.get("datafiles", "sql", "table", "table_type_test_data.sql").toString(), SQLDBUtils.DB_DIRECTORY,
                DB_NAME_H2);
        resultNegative = BCompileUtil
                .compile(Paths.get("test-src", "table", "table_type_test_negative.bal").toString());
    }

    @AfterSuite
    public void cleanup() {
        if (testDatabase != null) {
            testDatabase.stop();
        }
    }

    @Test(description = "Wrong order int test")
    public void testWrongOrderInt() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderInt", args);
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Wrong order string test")
    public void testWrongOrderString() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderString", args);
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Wrong order boolean test")
    public void testWrongOrderBoolean() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderBoolean", args);
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Wrong order float test")
    public void testWrongOrderFloat() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderFloat", args);
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Wrong order double test")
    public void testWrongOrderDouble() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderDouble", args);
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Wrong order long test")
    public void testWrongOrderLong() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderLong", args);
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Wrong order blob test")
    public void testWrongOrderBlob() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderBlobWrongOrder", args);
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Correct order but wrong type blob test")
    public void testCorrectOrderWrongTypeBlob() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderBlobCorrectOrderWrongType", args);
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Greater number of parameters test")
    public void testGreaterNoOfParams() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testGreaterNoOfParams", args);
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(
                ".*number of fields in the constraint type is greater than column count of the result set.*",
                retVal[0].stringValue()));
    }

    @Test(description = "Lower number of parameters test")
    public void testLowerNoOfParams() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testLowerNoOfParams", args);
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(
                ".*number of fields in the constraint type is lower than column count of the result set.*",
                retVal[0].stringValue()));
    }
}
