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
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
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
 * This test class verifies the behaviour of the ParameterizedString passed into the testQuery operation.
 *
 * @since 1.3.0
 */
public class ParamsExecuteTest {
    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_PARAMS_EXECUTE";
    private static final String JDBC_URL = "jdbc:h2:file:" + SQLDBUtils.DB_DIR + DB_NAME;
    private BValue[] args = {new BString(JDBC_URL), new BString(SQLDBUtils.DB_USER),
            new BString(SQLDBUtils.DB_PASSWORD)};

    @BeforeClass
    public void setup() throws SQLException {
        result = BCompileUtil.compile(SQLDBUtils.getBalFilesDir("execute",
                "execute-params-query-test.bal"));
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIR), DB_NAME);
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIR, DB_NAME, SQLDBUtils.getSQLResourceDir("execute",
                "execute-params-test-data.sql"));
    }

    @Test
    public void testInsertIntoDataTable() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDataTable", args);
        validateResult(returns, 1, 1);
    }

    @Test
    public void testInsertIntoDataTable2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDataTable2", args);
        validateResult(returns, 1, 1);
    }

    @Test
    public void testInsertIntoDataTable3() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDataTable3", args);
        validateResult(returns, 1, 1);
    }

    @Test
    public void testInsertIntoDataTable4() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDataTable4", args);
        validateResult(returns, 1, 1);
    }

    @Test
    public void testDeleteDataTable() {
        BValue[] returns = BRunUtil.invokeFunction(result, "deleteDataTable1", args);
        validateResult(returns, 1, -1);
    }

    @Test
    public void testDeleteDataTable2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "deleteDataTable2", args);
        validateResult(returns, 1, -1);
    }

    @Test
    public void testDeleteDataTable3() {
        BValue[] returns = BRunUtil.invokeFunction(result, "deleteDataTable3", args);
        validateResult(returns, 1, -1);
    }

    @Test
    public void testInsertIntoComplexTable() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoComplexTable", args);
        validateResult(returns, 1, 1);
    }

    @Test
    public void testInsertIntoComplexTable2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoComplexTable2", args);
        validateResult(returns, 1, 1);
    }

    @Test
    public void testInsertIntoComplexTable3() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoComplexTable3", args);
        validateResult(returns, 1, 1);
    }

    @Test
    public void testDeleteComplexTable() {
        BValue[] returns = BRunUtil.invokeFunction(result, "deleteComplexTable", args);
        validateResult(returns, 1, -1);
    }

    @Test
    public void testDeleteComplexTable2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "deleteComplexTable2", args);
        validateResult(returns, 0, -1);
    }

    @Test
    public void testInsertIntoNumericTable() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoNumericTable", args);
        validateResult(returns, 1, 2);
    }

    @Test
    public void testInsertIntoNumericTable2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoNumericTable2", args);
        validateResult(returns, 1, 2);
    }

    @Test
    public void testInsertIntoNumericTable3() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoNumericTable3", args);
        validateResult(returns, 1, 2);
    }

    @Test
    public void testInsertIntoDateTimeTable() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDateTimeTable", args);
        validateResult(returns, 1, 1);
    }

    @Test
    public void testInsertIntoDateTimeTable2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDateTimeTable2", args);
        validateResult(returns, 1, 1);
    }

    @Test
    public void testInsertIntoDateTimeTable3() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDateTimeTable3", args);
        validateResult(returns, 1, 1);
    }

    @Test
    public void testInsertIntoDateTimeTable4() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoDateTimeTable4", args);
        validateResult(returns, 1, 1);
    }

    @Test
    public void testInsertIntoArrayTable() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoArrayTable", args);
        validateResult(returns, 1, 1);
    }

    @Test
    public void testInsertIntoArrayTable2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "insertIntoArrayTable2", args);
        validateResult(returns, 1, 1);
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
