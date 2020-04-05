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
package org.ballerinalang.mysql.query;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.mysql.BaseTest;
import org.ballerinalang.mysql.utils.SQLDBUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.TimeZone;

/**
 * This test class handles the complex sql types to ballerina type conversion for query operation.
 *
 * @since 1.2.0
 */
public class ComplexTypesQueryTest {

    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_COMPLEX_QUERY";
    private static final String SQL_SCRIPT = SQLDBUtils.SQL_RESOURCE_DIR + File.separator + SQLDBUtils.QUERY_DIR +
            File.separator + "complex-test-data.sql";

    static {
        BaseTest.addDBSchema(DB_NAME, SQL_SCRIPT);
    }

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compileOffline(SQLDBUtils.getBalFilesDir(SQLDBUtils.QUERY_DIR,
                "complex-query-test.bal"));
    }

    @Test(description = "Retrieve primitive types")
    public void testGetPrimitiveTypes() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testGetPrimitiveTypes");
        SQLDBUtils.assertNotError(returnVal[0]);
        Assert.assertTrue(returnVal[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returnVal[0]).getMap();
        Assert.assertEquals(result.size(), 6);
        Assert.assertEquals(((BInteger) result.get("INT_TYPE")).intValue(), 1);
        Assert.assertEquals(((BInteger) result.get("LONG_TYPE")).intValue(), 9223372036854774807L);
        DecimalFormat df = new DecimalFormat("###.##");
        Assert.assertEquals(df.format(((BFloat) result.get("FLOAT_TYPE")).floatValue()), "123.34");
        Assert.assertEquals(((BFloat) result.get("DOUBLE_TYPE")).floatValue(), 2139095039D);
        Assert.assertTrue(((BBoolean) result.get("BOOLEAN_TYPE")).booleanValue());
        Assert.assertEquals(((BString) result.get("STRING_TYPE")).stringValue(), "Hello");
    }

    @Test(description = "Check record to JSON conversion.")
    public void testToJson() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJson");
        Assert.assertEquals(returns.length, 1);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        String expected = "\\{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.3[0-9]+, " +
                "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}";
        Assert.assertTrue(returns[0].stringValue().matches(expected), "Found: " + returns[0].stringValue());

    }

    @Test(description = "Check retrieving blob clob binary data.")
    public void testToJsonComplexTypes() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJsonComplexTypes");
        Assert.assertEquals(returns.length, 1);
        SQLDBUtils.assertNotError(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        String blobString = new String(((BValueArray) result.get("BLOB_TYPE")).getBytes());
        String clobString = ((BString) result.get("CLOB_TYPE")).stringValue();
        String binaryString = new String(((BValueArray) result.get("BINARY_TYPE")).getBytes());
        Assert.assertEquals(blobString, "wso2 ballerina blob test.");
        Assert.assertEquals(clobString, "very long text");
        Assert.assertEquals(binaryString, "wso2 ballerina binary test.");
    }

    @Test(description = "Check retrieving nill values.")
    public void testComplexTypesNil() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testComplexTypesNil");
        Assert.assertEquals(returns.length, 1);
        SQLDBUtils.assertNotError(returns[0]);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 3);
        Assert.assertNull(result.get("BLOB_TYPE"));
        Assert.assertNull(result.get("CLOB_TYPE"));
        Assert.assertNull(result.get("BINARY_TYPE"));
    }

    @Test(description = "Check date time operation")
    public void testDateTime() throws ParseException {
        Date dateInserted = new SimpleDateFormat("yyyy-MM-dd").parse("2017-05-23");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 15);
        calendar.set(Calendar.SECOND, 23);
        Date timeInserted = calendar.getTime();

        calendar.clear();
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 33);
        calendar.set(Calendar.SECOND, 55);
        calendar.set(Calendar.YEAR, 2017);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 25);
        Date timestampInserted = calendar.getTime();

        BValue[] returns = BRunUtil.invoke(result, "testDateTime");
        Assert.assertEquals(returns.length, 1);
        SQLDBUtils.assertNotError(returns[0]);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 4);
        assertDateStringValues(result, dateInserted, timeInserted, timestampInserted);
    }

    @Test(description = "Check values retrieved with column alias.")
    public void testColumnAlias() {
        BValue[] returns = BRunUtil.invoke(result, "testColumnAlias");
        Assert.assertEquals(returns.length, 1);
        SQLDBUtils.assertNotError(returns[0]);
        LinkedHashMap result = ((BMap) returns[0]).getMap();
        Assert.assertEquals(result.size(), 7);
        Assert.assertEquals(((BInteger) result.get("INT_TYPE")).intValue(), 1);
        DecimalFormat df = new DecimalFormat("###.##");
        Assert.assertEquals(df.format(((BFloat) result.get("FLOAT_TYPE")).floatValue()), "123.34");
        Assert.assertEquals(((BInteger) result.get("DT2INT_TYPE")).intValue(), 100);
    }

    private void assertDateStringValues(LinkedHashMap returns, Date dateInserted, Date timeInserted,
                                        Date timestampInserted) {
        try {
            DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
            String dateReturnedStr = returns.get("DATE_TYPE").toString();
            Date dateReturned = dfDate.parse(dateReturnedStr);
            Assert.assertEquals(dateInserted.compareTo(dateReturned), 0);

            DateFormat dfTime = new SimpleDateFormat("HH:mm:ss");
            String timeReturnedStr = returns.get("TIME_TYPE").toString();
            Date timeReturned = dfTime.parse(timeReturnedStr);
            Assert.assertEquals(timeInserted.compareTo(timeReturned), 0);

            DateFormat dfTimestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String timestampReturnedStr = returns.get("TIMESTAMP_TYPE").toString();
            Date timestampReturned = dfTimestamp.parse(timestampReturnedStr);
            Assert.assertEquals(timestampInserted.compareTo(timestampReturned), 0);

            String datetimeReturnedStr = returns.get("DATETIME_TYPE").toString();
            Date datetimeReturned = dfTimestamp.parse(datetimeReturnedStr);
            Assert.assertEquals(timestampInserted.compareTo(datetimeReturned), 0);
        } catch (ParseException e) {
            Assert.fail("Parsing the returned date/time/timestamp value has failed", e);
        }
    }
}
