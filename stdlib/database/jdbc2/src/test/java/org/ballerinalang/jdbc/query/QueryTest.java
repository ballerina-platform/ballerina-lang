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
package org.ballerinalang.jdbc.query;

import org.ballerinalang.jdbc.utils.SQLDBUtils;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;

/**
 * This test class executes the query operation test on JDBC client.
 *
 * @since 1.2.0
 */
public class QueryTest {
    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_CONNECTOR_INIT";
    private static final String JDBC_URL = "jdbc:h2:file:" + SQLDBUtils.DB_DIR + DB_NAME;
    private BValue[] args = {new BString(JDBC_URL), new BString(SQLDBUtils.DB_USER),
            new BString(SQLDBUtils.DB_PASSWORD)};

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compileOffline(SQLDBUtils.getBalFilesDir("query", "query-test.bal"));
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIR), DB_NAME);
        SQLDBUtils.initH2Database(SQLDBUtils.DB_DIR, DB_NAME,
                SQLDBUtils.getSQLResourceDir("query", "query-test-data.sql"));
    }

    @Test
    public void testQuery() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testQuery", args);
        Assert.assertTrue(returnVal[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returnVal[0]).getMap();
        Assert.assertEquals(result.size(), 10);
        Assert.assertEquals(result.get("ID"), new BInteger(1));
        Assert.assertEquals(result.get("INT_TYPE"), new BInteger(2147483647));
        Assert.assertEquals(result.get("BIGINT_TYPE"), new BInteger(9223372036854774807L));
        Assert.assertEquals(result.get("SMALLINT_TYPE"), new BInteger(32767));
        Assert.assertEquals(result.get("TINYINT_TYPE"), new BInteger(127));
        Assert.assertEquals(result.get("BIT_TYPE"), new BBoolean(true));
        Assert.assertEquals(((BDecimal) result.get("DECIMAL_TYPE")).value().doubleValue(), 1234.567);
        Assert.assertEquals(((BDecimal) result.get("NUMERIC_TYPE")).value().doubleValue(), 1234.567);
        DecimalFormat df = new DecimalFormat("###.###");
        Assert.assertEquals(df.format(((BFloat) result.get("FLOAT_TYPE")).floatValue()), "1234.567");
        Assert.assertEquals(df.format(((BFloat) result.get("REAL_TYPE")).floatValue()), "1234.567");
    }

    @Test
    public void testQueryNumericTypeRecord() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testQueryNumericTypeRecord", args);
        Assert.assertTrue(returnVal[0] instanceof BMap);
        Assert.assertEquals(returnVal[0].getType().getName(), "NumericType");
        LinkedHashMap result = ((BMap) returnVal[0]).getMap();
        Assert.assertEquals(result.size(), 10);
        Assert.assertEquals(result.get("id"), new BInteger(1));
        Assert.assertEquals(result.get("int_type"), new BInteger(2147483647));
        Assert.assertEquals(result.get("bigint_type"), new BInteger(9223372036854774807L));
        Assert.assertEquals(result.get("smallint_type"), new BInteger(32767));
        Assert.assertEquals(result.get("tinyint_type"), new BInteger(127));
        Assert.assertEquals(result.get("bit_type"), new BBoolean(true));
        Assert.assertEquals(((BDecimal) result.get("decimal_type")).value().doubleValue(), 1234.567);
        Assert.assertEquals(((BDecimal) result.get("numeric_type")).value().doubleValue(), 1234.567);
        DecimalFormat df = new DecimalFormat("###.###");
        Assert.assertEquals(df.format(((BFloat) result.get("float_type")).floatValue()), "1234.567");
        Assert.assertEquals(df.format(((BFloat) result.get("real_type")).floatValue()), "1234.567");
    }

}
