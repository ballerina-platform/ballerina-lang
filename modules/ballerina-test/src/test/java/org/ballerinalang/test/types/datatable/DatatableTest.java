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
package org.ballerinalang.test.types.datatable;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Class to test functionality of datatables.
 */
public class DatatableTest {

    CompileResult result;
    private static final String DB_NAME = "TEST_DATA_TABLE_DB";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/datatable/datatable-type.bal");
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initDatabase(SQLDBUtils.DB_DIRECTORY, DB_NAME, "datafiles/DataTableDataFile.sql");
    }

    @Test(groups = "DatatableTest", description = "Check getByIndex methods for primitive types.")
    public void testGetPrimitiveTypes() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testGetPrimitiveTypes", args);

        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    @Test(groups = "DatatableTest", description = "Check toJson methods.")
    public void testToJson() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testToJson", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"INT_TYPE\":1,\"LONG_TYPE\":9223372036854774807,\"FLOAT_TYPE\":123.34,"
                        + "\"DOUBLE_TYPE\":2.139095039E9,\"BOOLEAN_TYPE\":true,\"STRING_TYPE\":\"Hello\"}]");
    }

    @Test(groups = "DatatableTest", description = "Check toXml methods with wrapper element.")
    public void testToXml() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testToXml", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<results><result><INT_TYPE>1</INT_TYPE><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                        + "<FLOAT_TYPE>123.34</FLOAT_TYPE><DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE>"
                        + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE></result></results>");
     }

    @Test(groups = "DatatableTest", description = "Check xml streaming when result set consumed once.")
    public void testToXmlMultipleConsume() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testToXmlMultipleConsume", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<results/>");
    }

    @Test(groups = "DatatableTest", description = "Check toXml methods with complex element.")
    public void testToXmlComplex() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "toXmlComplex", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<results><result><INT_TYPE>1</INT_TYPE><INT_ARRAY><element>1</element><element>2</element>"
                        + "<element>3</element></INT_ARRAY><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                        + "<LONG_ARRAY><element>100000000</element><element>200000000</element>"
                        + "<element>300000000</element></LONG_ARRAY><FLOAT_TYPE>123.34</FLOAT_TYPE>"
                        + "<FLOAT_ARRAY><element>245.23</element><element>5559.49</element>"
                        + "<element>8796.123</element></FLOAT_ARRAY><DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE>"
                        + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE><DOUBLE_ARRAY>"
                        + "<element>245.23</element><element>5559.49</element><element>8796.123</element>"
                        + "</DOUBLE_ARRAY><BOOLEAN_ARRAY><element>true</element><element>false</element>"
                        + "<element>true</element></BOOLEAN_ARRAY><STRING_ARRAY><element>Hello</element>"
                        + "<element>Ballerina</element></STRING_ARRAY></result></results>");
    }

    @Test(groups = "DatatableTest",  description = "Check getObjectAsStringByName methods for complex types.")
    public void testGetComplexTypes() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testGetComplexTypes", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals((returns[0]).stringValue(), "wso2 ballerina blob test.");
        Assert.assertEquals((returns[1]).stringValue(), "very long text");
        Assert.assertEquals((returns[2]).stringValue(), "wso2 ballerina binary test.");
    }

    @Test(groups = "DatatableTest", description = "Check getXXXArray methods for complex types.")
    public void testArrayData() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testArrayData", args);
        Assert.assertEquals(returns.length, 5);
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<String, BInteger> intArray = (BMap) returns[0];
        Assert.assertTrue(intArray.get("0") instanceof BInteger);
        Assert.assertEquals(intArray.get("0").intValue(), 1);
        Assert.assertEquals(intArray.get("1").intValue(), 2);
        Assert.assertEquals(intArray.get("2").intValue(), 3);

        Assert.assertTrue(returns[1] instanceof BMap);
        BMap<String, BInteger> longArray = (BMap) returns[1];
        Assert.assertTrue(longArray.get("0") instanceof BInteger);
        Assert.assertEquals(longArray.get("0").intValue(), 100000000);
        Assert.assertEquals(longArray.get("1").intValue(), 200000000);
        Assert.assertEquals(longArray.get("2").intValue(), 300000000);

        Assert.assertTrue(returns[2] instanceof BMap);
        BMap<String, BFloat> doubleArray = (BMap) returns[2];
        Assert.assertTrue(doubleArray.get("0") instanceof BFloat);
        Assert.assertEquals(doubleArray.get("0").floatValue(), 245.23);
        Assert.assertEquals(doubleArray.get("1").floatValue(), 5559.49);
        Assert.assertEquals(doubleArray.get("2").floatValue(), 8796.123);

        Assert.assertTrue(returns[3] instanceof BMap);
        BMap<String, BString> stringArray = (BMap) returns[3];
        Assert.assertTrue(stringArray.get("0") instanceof BString);
        Assert.assertEquals(stringArray.get("0").stringValue(), "Hello");
        Assert.assertEquals(stringArray.get("1").stringValue(), "Ballerina");

        Assert.assertTrue(returns[4] instanceof BMap);
        BMap<String, BBoolean> booleanArray = (BMap) returns[4];
        Assert.assertTrue(booleanArray.get("0") instanceof BBoolean);
        Assert.assertEquals(booleanArray.get("0").booleanValue(), true);
        Assert.assertEquals(booleanArray.get("1").booleanValue(), false);
        Assert.assertEquals(booleanArray.get("2").booleanValue(), true);
    }

    @Test(groups = "DatatableTest", description = "Check date time operation")
    public void testDateTime() {
        BValue[] args = new BValue[3];
        Calendar cal = Calendar.getInstance();

        cal.clear();
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, 5);
        cal.set(Calendar.DAY_OF_MONTH, 23);
        long dateInserted = cal.getTimeInMillis();
        args[0] = new BInteger(dateInserted);

        cal.clear();
        cal.set(Calendar.HOUR, 14);
        cal.set(Calendar.MINUTE, 15);
        cal.set(Calendar.SECOND, 23);
        long timeInserted = cal.getTimeInMillis();
        args[1] = new BInteger(timeInserted);


        cal.clear();
        cal.set(Calendar.HOUR, 16);
        cal.set(Calendar.MINUTE, 33);
        cal.set(Calendar.SECOND, 55);
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 25);
        long timestampInserted = cal.getTimeInMillis();
        args[2] = new BInteger(timestampInserted);

        BValue[] returns = BRunUtil.invoke(result,  "testDateTime", args);

        Assert.assertEquals(returns.length, 4);

        try {
            DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
            String dateReturned = returns[0].stringValue();
            long dateReturnedEpoch = dfDate.parse(dateReturned).getTime();
            Assert.assertEquals(dateInserted, dateReturnedEpoch);

            DateFormat dfTime = new SimpleDateFormat("HH:mm:ss.SSS");
            String timeReturned = returns[1].stringValue();
            long timeReturnedEpoch = dfTime.parse(timeReturned).getTime();
            Assert.assertEquals(timeInserted, timeReturnedEpoch);

            DateFormat dfTimestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            String timestampReturned = returns[2].stringValue();
            long timestampReturnedEpoch = dfTimestamp.parse(timestampReturned).getTime();
            Assert.assertEquals(timestampInserted, timestampReturnedEpoch);

            String datetimeReturned = returns[3].stringValue();
            long datetimeReturnedEpoch = dfTimestamp.parse(datetimeReturned).getTime();
            Assert.assertEquals(timestampInserted, datetimeReturnedEpoch);
        } catch (ParseException e) {
            //Ignore
        }
    }

    @Test(groups = "DatatableTest", description = "Check toJson methods with null values.")
    public void testJsonWithNull() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result,  "testJsonWithNull", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"INT_TYPE\":0,\"LONG_TYPE\":0,\"FLOAT_TYPE\":0.0,\"DOUBLE_TYPE\":0.0,\"BOOLEAN_TYPE\":false,"
                        + "\"STRING_TYPE\":null}]");
    }

    @Test(groups = "DatatableTest", description = "Check toXml method with null values.")
    public void testXmlWithNull() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testXmlWithNull", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<results><result><INT_TYPE>0</INT_TYPE><LONG_TYPE>0</LONG_TYPE><FLOAT_TYPE>0.0</FLOAT_TYPE>"
                        + "<DOUBLE_TYPE>0.0</DOUBLE_TYPE><BOOLEAN_TYPE>false</BOOLEAN_TYPE>"
                        + "<STRING_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>"
                        + "</result></results>");
    }

    @Test(groups = "DatatableTest", description = "Check toXml method within transaction.")
    public void testToXmlWithinTransaction() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testToXmlWithinTransaction", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals((returns[0]).stringValue(), "<results><result><INT_TYPE>1</INT_TYPE><LONG_TYPE>"
                + "9223372036854774807</LONG_TYPE></result></results>");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = "DatatableTest", description = "Check toJson methods within transaction.")
    public void testToJsonWithinTransaction() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result,  "testToJsonWithinTransaction", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals((returns[0]).stringValue(), "[{\"INT_TYPE\":1,\"LONG_TYPE\":9223372036854774807}]");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = "DatatableTest", description = "Check blob data support.")
    public void testBlobData() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result,  "testBlobData", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((returns[0]).stringValue(), "wso2 ballerina blob test.");
    }

    @Test(groups = "DatatableTest", description = "Check getByIndex methods for primitive types.")
    public void testColumnAlias() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testColumnAlias", args);

        Assert.assertEquals(returns.length, 7);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BInteger) returns[6]).intValue(), 100);
    }

    @Test(groups = "DatatableTest", description = "Check getByIndex methods for primitive types.")
    public void testBlobInsert() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testBlobInsert", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = "DatatablesTest", description = "Check whether printing of datatable variables is handled properly.")
    public void testDatatablePrintAndPrintln() throws IOException {
        PrintStream original = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final String expected = "\n";

            BRunUtil.invoke(result, "testPrintandPrintlnDatatable");
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test(groups = "DatatableTest", description = "Check auto close resources in datatable.")
    public void testDatatableAutoClose() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testDatatableAutoClose", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(returns[1].stringValue(), "[{\"INT_TYPE\":1,\"LONG_TYPE\":9223372036854774807,"
                + "\"FLOAT_TYPE\":123.34,\"DOUBLE_TYPE\":2.139095039E9,\"BOOLEAN_TYPE\":true,"
                + "\"STRING_TYPE\":\"Hello\"}]");
    }

    @Test(groups = "DatatableTest", description = "Check manual close resources in datatable.")
    public void testDatatableManualClose() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testDatatableManualClose", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(dependsOnGroups = "DatatableTest", description = "Check all sql connectors are closed properly.")
    public void testCloseConnectionPool() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testCloseConnectionPool", args);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "DatatableTest", description = "Check select methods for primitive types.")
    public void testMutltipleRows() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testMutltipleRows", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
    }

    @Test(groups = "DatatableTest", description = "Check get float and double types.")
    public void testGetFloatTypes() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testGetFloatTypes", args);

        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 238999.34);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 238999.34);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 238999.34);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 238999.34);
    }

    @Test(groups = "DatatableTest", description = "Check array data insert and println on arrays")
    public void testArrayDataInsertAndPrint() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testArrayDataInsertAndPrint", args);
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 5);
        Assert.assertEquals(((BInteger) returns[5]).intValue(), 2);
    }

    @Test(groups = "DatatableTest", description = "Check get float and double types.")
    public void testSignedIntMaxMinValues() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testSignedIntMaxMinValues", args);
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
        Assert.assertEquals((returns[3]).stringValue(), "[{\"ID\":1,\"TINYINTDATA\":127,\"SMALLINTDATA\":32767,"
                + "\"INTDATA\":2147483647,\"BIGINTDATA\":9223372036854775807},"
                + "{\"ID\":2,\"TINYINTDATA\":-128,\"SMALLINTDATA\":-32768,\"INTDATA\":-2147483648,"
                + "\"BIGINTDATA\":-9223372036854775808},"
                + "{\"ID\":3,\"TINYINTDATA\":0,\"SMALLINTDATA\":0,\"INTDATA\":0,\"BIGINTDATA\":0}]");
        Assert.assertEquals((returns[4]).stringValue(), "<results><result><ID>1</ID><TINYINTDATA>127</TINYINTDATA>"
                + "<SMALLINTDATA>32767</SMALLINTDATA><INTDATA>2147483647</INTDATA>"
                + "<BIGINTDATA>9223372036854775807</BIGINTDATA></result>"
                + "<result><ID>2</ID><TINYINTDATA>-128</TINYINTDATA><SMALLINTDATA>-32768</SMALLINTDATA>"
                + "<INTDATA>-2147483648</INTDATA><BIGINTDATA>-9223372036854775808</BIGINTDATA></result>"
                + "<result><ID>3</ID><TINYINTDATA>0</TINYINTDATA><SMALLINTDATA>0</SMALLINTDATA><INTDATA>0</INTDATA>"
                + "<BIGINTDATA>0</BIGINTDATA></result></results>");
        Assert.assertEquals((returns[5]).stringValue(), "1|127|32767|2147483647|9223372036854775807#2|-128|-32768|"
                + "-2147483648|-9223372036854775808#3|0|0|0|0#");
    }

    @Test(groups = "DatatableTest", description = "Check blob binary and clob types types.")
    public void testComplexTypeInsertAndRetrieval() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testComplexTypeInsertAndRetrieval", args);
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals((returns[2]).stringValue(), "[{\"ROW_ID\":100,\"BLOB_TYPE\":\"U2FtcGxlIFRleHQ=\","
                + "\"CLOB_TYPE\":\"Sample Text\",\"BINARY_TYPE\":\"U2FtcGxlIFRleHQAAAAAAAAAAAAAAAAAAAAA\"},"
                + "{\"ROW_ID\":200,\"BLOB_TYPE\":null,\"CLOB_TYPE\":null,\"BINARY_TYPE\":null}]");
        Assert.assertEquals((returns[3]).stringValue(), "<results><result><ROW_ID>100</ROW_ID>"
                + "<BLOB_TYPE>U2FtcGxlIFRleHQ=</BLOB_TYPE><CLOB_TYPE>Sample Text</CLOB_TYPE>"
                + "<BINARY_TYPE>U2FtcGxlIFRleHQAAAAAAAAAAAAAAAAAAAAA</BINARY_TYPE>" + "</result>"
                + "<result><ROW_ID>200</ROW_ID>"
                + "<BLOB_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>"
                + "<CLOB_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>"
                + "<BINARY_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>"
                + "</result></results>");
        Assert.assertEquals((returns[4]).stringValue(), "100|Sample Text|Sample Text|200||null|");
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
