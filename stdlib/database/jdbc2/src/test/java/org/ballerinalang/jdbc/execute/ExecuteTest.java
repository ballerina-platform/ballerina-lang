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
package org.ballerinalang.jdbc.execute;

import org.ballerinalang.jdbc.utils.SQLDBUtils;
import org.ballerinalang.model.values.*;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.LinkedHashMap;

/**
 * This test class includes the basic test cases related to the execute operation of jdbc client.
 *
 * @since 1.2.0
 */
public class ExecuteTest {
    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_EXCUTE_QUERY";
    private static final String JDBC_URL = "jdbc:h2:file:" + SQLDBUtils.DB_DIR + DB_NAME;
    private BValue[] args = {new BString(JDBC_URL), new BString(SQLDBUtils.DB_USER),
            new BString(SQLDBUtils.DB_PASSWORD)};

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compileOffline(SQLDBUtils.getBalFilesDir("execute", "execute-basic-test.bal"));
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIR), DB_NAME);
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIR, DB_NAME,
                SQLDBUtils.getSQLResourceDir("execute", "execute-test-data.sql"));
    }

    @Test
    public void testCreateTable() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testCreateTable", args);
        Assert.assertTrue(returnVal[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returnVal[0]).getMap();
        Assert.assertEquals(((BByte) result.get(Constants.AFFECTED_ROW_COUNT_FIELD)).intValue(), 0);
        Assert.assertNull(result.get(Constants.LAST_INSERTED_ID_FIELD));
    }

    @Test
    public void testInsertTable() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testInsertTable", args);
        Assert.assertTrue(returnVal[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returnVal[0]).getMap();
        Assert.assertEquals(((BByte) result.get(Constants.AFFECTED_ROW_COUNT_FIELD)).intValue(), 1);
        Assert.assertTrue(((BInteger) result.get(Constants.LAST_INSERTED_ID_FIELD)).intValue() > 1);
    }

    @Test
    public void testInsertTableWithoutGeneratedKeys() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testInsertTableWithoutGeneratedKeys", args);
        Assert.assertTrue(returnVal[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returnVal[0]).getMap();
        Assert.assertEquals(((BByte) result.get(Constants.AFFECTED_ROW_COUNT_FIELD)).intValue(), 1);
        Assert.assertNull(result.get(Constants.LAST_INSERTED_ID_FIELD));
    }

    @Test
    public void testInsertTableWithGeneratedKeys() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testInsertTableWithGeneratedKeys", args);
        Assert.assertTrue(returnVal[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returnVal[0]).getMap();
        Assert.assertEquals(((BByte) result.get(Constants.AFFECTED_ROW_COUNT_FIELD)).intValue(), 1);
        Assert.assertTrue(((BInteger) result.get(Constants.LAST_INSERTED_ID_FIELD)).intValue() > 1);
    }

    @Test
    public void testInsertTableWithDatabaseError() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testInsertTableWithDatabaseError", args);
        Assert.assertTrue(returnVal[0] instanceof BError);
        BError error = (BError) returnVal[0];
        Assert.assertEquals(error.getReason(), "{ballerina/sql}DatabaseError");
        Assert.assertTrue(error.getDetails() instanceof BMap);
        BMap<String, BValue> errorDetails = (BMap<String, BValue>) error.getDetails();
        Assert.assertTrue(errorDetails.get(Constants.ErrorRecordFields.MESSAGE).stringValue()
                .contains("Table \"NUMERICTYPESNONEXISTTABLE\" not found"));
        Assert.assertEquals(((BInteger) errorDetails.get(Constants.ErrorRecordFields.ERROR_CODE)).intValue(), 42102);
        Assert.assertEquals(errorDetails.get(Constants.ErrorRecordFields.SQL_STATE).stringValue(), "42S02");
    }

    @Test
    public void testInsertTableWithDataTypeError() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testInsertTableWithDataTypeError", args);
        Assert.assertTrue(returnVal[0] instanceof BError);
        BError error = (BError) returnVal[0];
        Assert.assertEquals(error.getReason(), "{ballerina/sql}DatabaseError");
        Assert.assertTrue(error.getDetails() instanceof BMap);
        BMap<String, BValue> errorDetails = (BMap<String, BValue>) error.getDetails();
        Assert.assertTrue(errorDetails.get(Constants.ErrorRecordFields.MESSAGE).stringValue()
                .contains("Data conversion error converting \"'This is wrong type' (NUMERICTYPES: \"\"INT_TYPE\"\" INT)\""));
        Assert.assertEquals(((BInteger) errorDetails.get(Constants.ErrorRecordFields.ERROR_CODE)).intValue(), 22018);
        Assert.assertEquals(errorDetails.get(Constants.ErrorRecordFields.SQL_STATE).stringValue(), "22018");
    }

    @Test
    public void testUdateData() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testUdateData", args);
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
