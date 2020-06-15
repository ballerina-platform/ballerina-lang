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
package org.ballerinalang.sql.batchexecute;

import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.sql.Constants;
import org.ballerinalang.sql.utils.SQLDBUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.ballerinalang.sql.Constants.ErrorRecordFields.EXECUTION_RESULTS;

/**
 * This test class verifies the behaviour of the batch execute.
 */
public class BatchExecuteTest {
    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_BATCH_EXECUTE";
    private static final String URL = SQLDBUtils.URL_PREFIX + DB_NAME;
    private BValue[] args = {new BString(URL), new BString(SQLDBUtils.DB_USER), new BString(SQLDBUtils.DB_PASSWORD)};

    @BeforeClass
    public void setup() throws SQLException {
        result = BCompileUtil.compile(SQLDBUtils.getMockModuleDir(), "batchexecute");
        SQLDBUtils.initHsqlDatabase(DB_NAME, SQLDBUtils.getSQLResourceDir("batchexecute",
                "batch-execute-test-data.sql"));
    }

    @Test
    public void testInsertIntoDataTable() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDataTable", args);
        int[] affectedRow = new int[]{1, 1, 1};
        int[] genId = new int[]{2, 3, 4};
        validateResult(returns, affectedRow, genId);
    }

    @Test
    public void testInsertIntoDataTable2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDataTable2", args);
        int[] affectedRow = new int[]{1};
        int[] genId = new int[]{5};
        validateResult(returns, affectedRow, genId);
    }

    @Test
    public void testInsertIntoDataTableFailure() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDataTableFailure", args);
        Assert.assertTrue(returns[0] instanceof BError);
        BMap<String, BValue> errorDetails = (BMap<String, BValue>) ((BError) returns[0]).getDetails();
        Assert.assertTrue(errorDetails.get(EXECUTION_RESULTS) instanceof BValueArray);
        BValueArray executionResults = (BValueArray) errorDetails.get(EXECUTION_RESULTS);
        Assert.assertEquals(executionResults.size(), 1);
        BMap<String, BValueType> result = (BMap<String, BValueType>) executionResults.getValues()[0];
        Assert.assertEquals(result.get(Constants.AFFECTED_ROW_COUNT_FIELD).intValue(), 1);
    }

    private void validateResult(BValue[] returns, int[] rowCount, int[] lastId) {
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        long count = returns[0].size();
        Assert.assertEquals(count, rowCount.length);
        Object[] results = ((BValueArray) returns[0]).getValues();

        for (int i = 0; i < count; i++) {
            BMap<String, BValueType> result = (BMap<String, BValueType>) results[i];
            Assert.assertEquals(result.get(Constants.AFFECTED_ROW_COUNT_FIELD).intValue(), rowCount[i]);
            if (lastId[i] == -1) {
                Assert.assertNull(result.get(Constants.LAST_INSERTED_ID_FIELD));
            } else {
                Assert.assertEquals(result.get(Constants.LAST_INSERTED_ID_FIELD).intValue(), lastId[i]);
            }
        }
    }
}
