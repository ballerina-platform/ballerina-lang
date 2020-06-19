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
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
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
import java.sql.SQLException;
import java.util.LinkedHashMap;

/**
 * This test class verifies the behaviour of the ParameterizedQuery passed into the testQuery operation.
 *
 * @since 1.3.0
 */
public class ParamsExecuteTest {
    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_PARAMS_EXECUTE";
    private static final String SQL_SCRIPT = SQLDBUtils.SQL_RESOURCE_DIR + File.separator + SQLDBUtils.EXECUTE_DIR +
            File.separator + "execute-params-test-data.sql";

    static {
        BaseTest.addDBSchema(DB_NAME, SQL_SCRIPT);
    }

    @BeforeClass
    public void setup() throws SQLException {
        result = BCompileUtil.compileOffline(SQLDBUtils.getBalFilesDir(SQLDBUtils.EXECUTE_DIR,
                "execute-params-query-test.bal"));
    }

    @Test
    public void testInsertIntoDataTable() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDataTable");
        validateResult(returns, 1, -1);
    }

    @Test
    public void testInsertIntoDataTable2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDataTable2");
        validateResult(returns, 1, -1);
    }

    @Test
    public void testInsertIntoDataTable3() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDataTable3");
        validateResult(returns, 1, -1);
    }

    @Test
    public void testInsertIntoDataTable4() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDataTable4");
        validateResult(returns, 1, -1);
    }

    @Test
    public void testDeleteDataTable() {
        BValue[] returns = BRunUtil.invokeFunction(result, "deleteDataTable1");
        validateResult(returns, 1, -1);
    }

    @Test
    public void testDeleteDataTable2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "deleteDataTable2");
        validateResult(returns, 1, -1);
    }

    @Test
    public void testDeleteDataTable3() {
        BValue[] returns = BRunUtil.invokeFunction(result, "deleteDataTable3");
        validateResult(returns, 1, -1);
    }

    @Test
    public void testInsertIntoComplexTable() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoComplexTable");
        validateResult(returns, 1, -1);
    }

    @Test
    public void testInsertIntoComplexTable2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoComplexTable2");
        validateResult(returns, 1, -1);
    }

    @Test
    public void testInsertIntoComplexTable3() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoComplexTable3");
        validateResult(returns, 1, -1);
    }

    @Test
    public void testDeleteComplexTable() {
        BValue[] returns = BRunUtil.invokeFunction(result, "deleteComplexTable");
        validateResult(returns, 1, -1);
    }

    @Test
    public void testDeleteComplexTable2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "deleteComplexTable2");
        validateResult(returns, 0, -1);
    }

    @Test
    public void testInsertIntoNumericTable() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoNumericTable");
        validateResult(returns, 1, 2);
    }

    @Test
    public void testInsertIntoNumericTable2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoNumericTable2");
        validateResult(returns, 1, 2);
    }

    @Test
    public void testInsertIntoNumericTable3() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoNumericTable3");
        validateResult(returns, 1, 2);
    }

    @Test
    public void testInsertIntoDateTimeTable() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDateTimeTable");
        validateResult(returns, 1, -1);
    }

    @Test
    public void testInsertIntoDateTimeTable2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDateTimeTable2");
        validateResult(returns, 1, -1);
    }

    @Test
    public void testInsertIntoDateTimeTable3() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDateTimeTable3");
        validateResult(returns, 1, -1);
    }

    @Test
    public void testInsertIntoDateTimeTable4() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDateTimeTable4");
        validateResult(returns, 1, -1);
    }

    private void validateResult(BValue[] returns, int rowCount, int lastId) {
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(((BByte) result.get(Constants.AFFECTED_ROW_COUNT_FIELD)).intValue(), rowCount);
        if (lastId == -1) {
            Assert.assertNull(result.get(Constants.LAST_INSERTED_ID_FIELD));
        } else {
            Assert.assertTrue(((BInteger) result.get(Constants.LAST_INSERTED_ID_FIELD)).intValue() > lastId);
        }
    }
}
