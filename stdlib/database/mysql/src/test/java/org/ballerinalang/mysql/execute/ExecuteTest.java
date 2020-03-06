/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.mysql.execute;

import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.mysql.BaseTest;
import org.ballerinalang.mysql.utils.SQLDBUtils;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * This test class includes the basic test cases related to the execute operation of mysql client.
 *
 * @since 1.2.0
 */
public class ExecuteTest {
    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_EXCUTE_QUERY";
    private static final String SQL_SCRIPT = SQLDBUtils.SQL_RESOURCE_DIR + File.separator + SQLDBUtils.EXECUTE_DIR +
            File.separator + "execute-test-data.sql";

    static {
        BaseTest.addDBSchema(DB_NAME, SQL_SCRIPT);
    }

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compileOffline(SQLDBUtils.getBalFilesDir(SQLDBUtils.EXECUTE_DIR,
                "execute-basic-test.bal"));
    }

    @Test
    public void testCreateTable() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testCreateTable");
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returnVal[0]).getMap();
        Assert.assertEquals(((BByte) result.get(Constants.AFFECTED_ROW_COUNT_FIELD)).intValue(), 0);
        Assert.assertNull(result.get(Constants.LAST_INSERTED_ID_FIELD));
    }

    @Test
    public void testInsertTable() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testInsertTable");
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returnVal[0]).getMap();
        Assert.assertEquals(((BByte) result.get(Constants.AFFECTED_ROW_COUNT_FIELD)).intValue(), 1);
        Assert.assertTrue(((BInteger) result.get(Constants.LAST_INSERTED_ID_FIELD)).intValue() > 1);
    }

    @Test
    public void testInsertTableWithoutGeneratedKeys() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testInsertTableWithoutGeneratedKeys");
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returnVal[0]).getMap();
        Assert.assertEquals(((BByte) result.get(Constants.AFFECTED_ROW_COUNT_FIELD)).intValue(), 1);
        Assert.assertNull(result.get(Constants.LAST_INSERTED_ID_FIELD));
    }

    @Test
    public void testInsertTableWithGeneratedKeys() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testInsertTableWithGeneratedKeys");
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returnVal[0]).getMap();
        Assert.assertEquals(((BByte) result.get(Constants.AFFECTED_ROW_COUNT_FIELD)).intValue(), 1);
        Assert.assertTrue(((BInteger) result.get(Constants.LAST_INSERTED_ID_FIELD)).intValue() > 1);
    }

    @Test
    public void testInsertAndSelectTableWithGeneratedKeys() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testInsertAndSelectTableWithGeneratedKeys");
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray result = (BValueArray) returnVal[0];
        Assert.assertEquals(result.getValues().length, 2);
        for (BRefType aResult : result.getValues()) {
            Assert.assertNotNull(aResult);
        }
        BMap executionResult = (BMap) result.getValues()[0];
        BMap numericRecord = (BMap) result.getValues()[1];
        long lastInsertedId = ((BInteger) executionResult.get(Constants.LAST_INSERTED_ID_FIELD)).intValue();
        Assert.assertEquals(((BByte) executionResult.get(Constants.AFFECTED_ROW_COUNT_FIELD)).intValue(), 1);
        Assert.assertEquals(((BInteger) numericRecord.get("id")).intValue(), lastInsertedId);
        Assert.assertEquals(((BInteger) numericRecord.get("int_type")).intValue(), 31);
        numericRecord.getMap().forEach(((k, v) -> {
            String key = (String) k;
            if (!key.equalsIgnoreCase("int_type") && !key.equalsIgnoreCase("id")) {
                Assert.assertNull(v);
            }
        }));
    }

    @Test
    public void testInsertWithAllNilAndSelectTableWithGeneratedKeys() {
        BValue[] returnVal = BRunUtil.invokeFunction(result,
                "testInsertWithAllNilAndSelectTableWithGeneratedKeys");
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray result = (BValueArray) returnVal[0];
        Assert.assertEquals(result.getValues().length, 2);
        for (BRefType aResult : result.getValues()) {
            Assert.assertNotNull(aResult);
        }
        BMap executionResult = (BMap) result.getValues()[0];
        BMap numericRecord = (BMap) result.getValues()[1];
        long lastInsertedId = ((BInteger) executionResult.get(Constants.LAST_INSERTED_ID_FIELD)).intValue();
        Assert.assertEquals(((BByte) executionResult.get(Constants.AFFECTED_ROW_COUNT_FIELD)).intValue(), 1);
        Assert.assertEquals(((BInteger) numericRecord.get("id")).intValue(), lastInsertedId);
        numericRecord.getMap().forEach(((k, v) -> {
            String key = (String) k;
            if (!key.equalsIgnoreCase("id")) {
                Assert.assertNull(v);
            }
        }));
    }

    @Test
    public void testInsertWithStringAndSelectTable() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testInsertWithStringAndSelectTable");
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray result = (BValueArray) returnVal[0];
        Assert.assertEquals(result.getValues().length, 2);
        for (BRefType aResult : result.getValues()) {
            Assert.assertNotNull(aResult);
        }
        BMap executionResult = (BMap) result.getValues()[0];
        BMap numericRecord = (BMap) result.getValues()[1];
        Assert.assertEquals(((BByte) executionResult.get(Constants.AFFECTED_ROW_COUNT_FIELD)).intValue(), 1);
        Assert.assertEquals(((BInteger) numericRecord.get("id")).intValue(), 25);
        String[] charKeys = new String[]{"char_type", "character_type"};
        numericRecord.getMap().forEach(((k, v) -> {
            String key = (String) k;
            if (!key.equalsIgnoreCase("id")) {
                if (Arrays.stream(charKeys).noneMatch(n -> n.equalsIgnoreCase(key))) {
                    Assert.assertTrue(((BString) v).stringValue().startsWith("str"));
                } else {
                    Assert.assertEquals(((BString) v).stringValue(), "s");
                }
            }
        }));
    }

    @Test
    public void testInsertWithEmptyStringAndSelectTable() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testInsertWithEmptyStringAndSelectTable");
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray result = (BValueArray) returnVal[0];
        Assert.assertEquals(result.getValues().length, 2);
        for (BRefType aResult : result.getValues()) {
            Assert.assertNotNull(aResult);
        }
        BMap executionResult = (BMap) result.getValues()[0];
        BMap numericRecord = (BMap) result.getValues()[1];
        Assert.assertEquals(((BByte) executionResult.get(Constants.AFFECTED_ROW_COUNT_FIELD)).intValue(), 1);
        Assert.assertEquals(((BInteger) numericRecord.get("id")).intValue(), 35);
        numericRecord.getMap().forEach(((k, v) -> {
            String key = (String) k;
            if (!key.equalsIgnoreCase("id")) {
                Assert.assertTrue(((BString) v).stringValue().isEmpty());
            }
        }));
    }

    @Test
    public void testInsertWithNilStringAndSelectTable() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testInsertWithNilStringAndSelectTable");
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray result = (BValueArray) returnVal[0];
        Assert.assertEquals(result.getValues().length, 2);
        for (BRefType aResult : result.getValues()) {
            Assert.assertNotNull(aResult);
        }
        BMap executionResult = (BMap) result.getValues()[0];
        BMap numericRecord = (BMap) result.getValues()[1];
        Assert.assertEquals(((BByte) executionResult.get(Constants.AFFECTED_ROW_COUNT_FIELD)).intValue(), 1);
        Assert.assertEquals(((BInteger) numericRecord.get("id")).intValue(), 45);
        numericRecord.getMap().forEach(((k, v) -> {
            String key = (String) k;
            if (!key.equalsIgnoreCase("id")) {
                Assert.assertNull(v);
            }
        }));
    }

    @Test
    public void testInsertTableWithDatabaseError() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testInsertTableWithDatabaseError");
        Assert.assertTrue(returnVal[0] instanceof BError);
        BError error = (BError) returnVal[0];
        Assert.assertEquals(error.getReason(), "{ballerina/sql}DatabaseError");
        Assert.assertTrue(error.getDetails() instanceof BMap);
        BMap<String, BValue> errorDetails = (BMap<String, BValue>) error.getDetails();
        String errMessage = errorDetails.get(Constants.ErrorRecordFields.MESSAGE).stringValue();
        Assert.assertTrue(errMessage.contains("Table 'test_sql_excute_query.numerictypesnonexisttable' doesn't exist"),
                "Found error message:" + errMessage);
        Assert.assertEquals(((BInteger) errorDetails.get(Constants.ErrorRecordFields.ERROR_CODE)).intValue(),
                1146);
        Assert.assertEquals(errorDetails.get(Constants.ErrorRecordFields.SQL_STATE).stringValue(), "42S02");
    }

    @Test
    public void testInsertTableWithDataTypeError() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testInsertTableWithDataTypeError");
        Assert.assertTrue(returnVal[0] instanceof BError);
        BError error = (BError) returnVal[0];
        Assert.assertEquals(error.getReason(), "{ballerina/sql}DatabaseError");
        Assert.assertTrue(error.getDetails() instanceof BMap);
        BMap<String, BValue> errorDetails = (BMap<String, BValue>) error.getDetails();
        String errMessage = errorDetails.get(Constants.ErrorRecordFields.MESSAGE).stringValue();
        Assert.assertTrue(errMessage.contains("Incorrect integer value: 'This is wrong type' for column 'int_type'"),
                "Found error message:" + errMessage);
        Assert.assertEquals(((BInteger) errorDetails.get(Constants.ErrorRecordFields.ERROR_CODE)).intValue(), 1366);
        Assert.assertEquals(errorDetails.get(Constants.ErrorRecordFields.SQL_STATE).stringValue(), "HY000");
    }

    @Test
    public void testUdateData() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testUdateData");
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BValueArray);
        BValueArray result = (BValueArray) returnVal[0];
        Assert.assertEquals(result.getValues().length, 2);
        for (BRefType aResult : result.getValues()) {
            Assert.assertNotNull(aResult);
        }
        BMap executionResult = (BMap) result.getValues()[0];
        BMap resultCount = (BMap) result.getValues()[1];
        Assert.assertEquals(((BByte) executionResult.get(Constants.AFFECTED_ROW_COUNT_FIELD)).intValue(), 1);
        Assert.assertEquals(((BInteger) resultCount.get("countVal")).intValue(), 1);
    }
}
