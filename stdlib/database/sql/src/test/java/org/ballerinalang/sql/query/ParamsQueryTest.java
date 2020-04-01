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
package org.ballerinalang.sql.query;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.sql.utils.SQLDBUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.LinkedHashMap;

/**
 * This test class verifies the behaviour of the ParamterizedString passed into the query operation.
 *
 * @since 1.3.0
 */
public class ParamsQueryTest {
    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_PARAMS_QUERY";
    private static final String URL = SQLDBUtils.URL_PREFIX + DB_NAME;
    private BValue[] args = {new BString(URL), new BString(SQLDBUtils.DB_USER), new BString(SQLDBUtils.DB_PASSWORD)};

    @BeforeClass
    public void setup() throws SQLException {
        result = BCompileUtil.compile(SQLDBUtils.getMockModuleDir(), "query");
        SQLDBUtils.initHsqlDatabase(DB_NAME, SQLDBUtils.getSQLResourceDir("query",
                "simple-params-test-data.sql"));
    }

    @Test
    public void testQuerySingleIntParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "querySingleIntParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryDoubleIntParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDoubleIntParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryIntAndLongParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryIntAndLongParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryStringParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryStringParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryIntAndStringParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryIntAndStringParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryDoubleParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDoubleParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryFloatParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryFloatParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryDoubleAndFloatParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDoubleAndFloatParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryDecimalParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDecimalParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryDecimalAnFloatParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryDecimalAnFloatParam", args);
        validateDataTableResult(returns);
    }

    @Test
    public void testQueryByteArrayParam() {
        BValue[] returns = BRunUtil.invokeFunction(result, "queryByteArrayParam", args);
        validateComplexTableResult(returns);
    }

    private void validateDataTableResult(BValue[] returns) {
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 8);
        Assert.assertEquals(((BInteger) result.get("ROW_ID")).intValue(), 1);
        Assert.assertEquals(((BInteger) result.get("INT_TYPE")).intValue(), 1);
        Assert.assertEquals(((BInteger) result.get("LONG_TYPE")).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) result.get("FLOAT_TYPE")).floatValue(), 123.34);
        Assert.assertEquals(((BFloat) result.get("DOUBLE_TYPE")).floatValue(), 2139095039D);
        Assert.assertTrue(((BBoolean) result.get("BOOLEAN_TYPE")).booleanValue());
        Assert.assertEquals(((BDecimal) result.get("DECIMAL_TYPE")).decimalValue().doubleValue(), 23.45);
        Assert.assertEquals(((BString) result.get("STRING_TYPE")).stringValue(), "Hello");
    }

    private void validateComplexTableResult(BValue[] returns) {
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 4);
        Assert.assertEquals(((BInteger) result.get("ROW_ID")).intValue(), 1);
        Assert.assertEquals(result.get("CLOB_TYPE").toString(), "very long text");
    }
}
