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
package org.ballerinalang.test.types.table;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
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
 * Class to test functionality of tables.
 */
public class TableTest {

    private CompileResult result;
    private static final String DB_NAME = "TEST_DATA_TABLE_DB";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/table_type.bal");
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initDatabase(SQLDBUtils.DB_DIRECTORY, DB_NAME, "datafiles/sql/DataTableDataFile.sql");
    }

    @Test(groups = "TableTest", description = "Check retrieving primitive types.")
    public void testGetPrimitiveTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testGetPrimitiveTypes");
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    @Test(groups = "TableTest", description = "Check table to JSON conversion.")
    public void testToJson() {
        BValue[] returns = BRunUtil.invoke(result, "testToJson");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"INT_TYPE\":1,\"LONG_TYPE\":9223372036854774807,\"FLOAT_TYPE\":123.34,"
                        + "\"DOUBLE_TYPE\":2.139095039E9,\"BOOLEAN_TYPE\":true,\"STRING_TYPE\":\"Hello\"}]");
    }

    @Test(groups = "TableTest", description = "Check table to XML conversion.")
    public void testToXml() {
        BValue[] returns = BRunUtil.invoke(result, "testToXml");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<results><result><INT_TYPE>1</INT_TYPE><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                        + "<FLOAT_TYPE>123.34</FLOAT_TYPE><DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE>"
                        + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE></result></results>");
     }

    @Test(groups = "TableTest", description = "Check xml streaming when result set consumed once.")
    public void testToXmlMultipleConsume() {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlMultipleConsume");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<results><result><INT_TYPE>1</INT_TYPE>"
                + "<LONG_TYPE>9223372036854774807</LONG_TYPE><FLOAT_TYPE>123.34</FLOAT_TYPE>"
                + "<DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE><BOOLEAN_TYPE>true</BOOLEAN_TYPE>"
                + "<STRING_TYPE>Hello</STRING_TYPE></result></results>");
    }

    @Test(groups = "TableTest", description = "Check table to XML conversion with concat operation.")
    public void testToXmlWithAdd() {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlWithAdd");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<results><result><INT_TYPE>1</INT_TYPE></result>"
                + "</results><results><result><INT_TYPE>1</INT_TYPE></result></results>");
    }

    @Test(groups = "TableTest", description = "Check xml streaming when result set consumed once.")
    public void testToJsonMultipleConsume() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonMultipleConsume");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[{\"INT_TYPE\":1,\"LONG_TYPE\":9223372036854774807,"
                + "\"FLOAT_TYPE\":123.34,\"DOUBLE_TYPE\":2.139095039E9,\"BOOLEAN_TYPE\":true,"
                + "\"STRING_TYPE\":\"Hello\"}]");
    }

    @Test(groups = "TableTest", description = "Check xml conversion with complex element.")
    public void testToXmlComplex() {
        BValue[] returns = BRunUtil.invoke(result, "toXmlComplex");
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

    @Test(groups = "TableTest", description = "Check xml conversion with complex element.")
    public void testToXmlComplexWithStructDef () {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlComplexWithStructDef");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<results><result><i>1</i><iA><element>1</element>"
                + "<element>2</element><element>3</element></iA><l>9223372036854774807</l>"
                + "<lA><element>100000000</element><element>200000000</element><element>300000000</element></lA>"
                + "<f>123.34</f><fA><element>245.23</element><element>5559.49</element><element>8796.123</element></fA>"
                + "<d>2.139095039E9</d><b>true</b><s>Hello</s>"
                + "<dA><element>245.23</element><element>5559.49</element><element>8796.123</element></dA>"
                + "<bA><element>true</element><element>false</element><element>true</element></bA>"
                + "<sA><element>Hello</element><element>Ballerina</element></sA></result></results>");
    }

    @Test(groups = "TableTest", description = "Check json conversion with complex element.")
    public void testToJsonComplex() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonComplex");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[{\"INT_TYPE\":1,\"INT_ARRAY\":[1,2,3],"
                + "\"LONG_TYPE\":9223372036854774807,\"LONG_ARRAY\":[100000000,200000000,300000000],"
                + "\"FLOAT_TYPE\":123.34,\"FLOAT_ARRAY\":[245.23,5559.49,8796.123],\"DOUBLE_TYPE\":2.139095039E9,"
                + "\"BOOLEAN_TYPE\":true,\"STRING_TYPE\":\"Hello\",\"DOUBLE_ARRAY\":[245.23,5559.49,8796.123],"
                + "\"BOOLEAN_ARRAY\":[true,false,true],\"STRING_ARRAY\":[\"Hello\",\"Ballerina\"]}]");
    }

    @Test(groups = "TableTest", description = "Check json conversion with complex element.")
    public void testToJsonComplexWithStructDef() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonComplexWithStructDef");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[{\"i\":1,\"iA\":[1,2,3],\"l\":9223372036854774807,"
                + "\"lA\":[100000000,200000000,300000000],\"f\":123.34,\"fA\":[245.23,5559.49,8796.123],"
                + "\"d\":2.139095039E9,\"b\":true,\"s\":\"Hello\",\"dA\":[245.23,5559.49,8796.123],"
                + "\"bA\":[true,false,true],\"sA\":[\"Hello\",\"Ballerina\"]}]");
    }

    @Test(groups = "TableTest",  description = "Check retrieving blob clob binary data.")
    public void testGetComplexTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testGetComplexTypes");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals((returns[0]).stringValue(), "wso2 ballerina blob test.");
        Assert.assertEquals((returns[1]).stringValue(), "very long text");
        Assert.assertEquals((returns[2]).stringValue(), "wso2 ballerina binary test.");
    }

    @Test(groups = "TableTest", description = "Check array data types.")
    public void testArrayData() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayData");
        Assert.assertEquals(returns.length, 5);

        Assert.assertTrue(returns[0] instanceof BIntArray);
        BIntArray intArray = (BIntArray) returns[0];
        Assert.assertEquals(intArray.get(0), 1);
        Assert.assertEquals(intArray.get(1), 2);
        Assert.assertEquals(intArray.get(2), 3);

        Assert.assertTrue(returns[1] instanceof BIntArray);
        BIntArray longArray = (BIntArray) returns[1];
        Assert.assertEquals(longArray.get(0), 100000000);
        Assert.assertEquals(longArray.get(1), 200000000);
        Assert.assertEquals(longArray.get(2), 300000000);

        Assert.assertTrue(returns[2] instanceof BFloatArray);
        BFloatArray doubleArray = (BFloatArray) returns[2];
        Assert.assertEquals(doubleArray.get(0), 245.23);
        Assert.assertEquals(doubleArray.get(1), 5559.49);
        Assert.assertEquals(doubleArray.get(2), 8796.123);

        Assert.assertTrue(returns[3] instanceof BStringArray);
        BStringArray stringArray = (BStringArray) returns[3];
        Assert.assertEquals(stringArray.get(0), "Hello");
        Assert.assertEquals(stringArray.get(1), "Ballerina");

        Assert.assertTrue(returns[4] instanceof BBooleanArray);
        BBooleanArray booleanArray = (BBooleanArray) returns[4];
        Assert.assertEquals(booleanArray.get(0), 1);
        Assert.assertEquals(booleanArray.get(1), 0);
        Assert.assertEquals(booleanArray.get(2), 1);
    }

    @Test(groups = "TableTest", description = "Check date time operation")
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
            Assert.assertEquals(dateReturnedEpoch, dateInserted);

            DateFormat dfTime = new SimpleDateFormat("HH:mm:ss.SSS");
            String timeReturned = returns[1].stringValue();
            long timeReturnedEpoch = dfTime.parse(timeReturned).getTime();
            Assert.assertEquals(timeReturnedEpoch, timeInserted);

            DateFormat dfTimestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            String timestampReturned = returns[2].stringValue();
            long timestampReturnedEpoch = dfTimestamp.parse(timestampReturned).getTime();
            Assert.assertEquals(timestampReturnedEpoch, timestampInserted);

            String datetimeReturned = returns[3].stringValue();
            long datetimeReturnedEpoch = dfTimestamp.parse(datetimeReturned).getTime();
            Assert.assertEquals(datetimeReturnedEpoch, timestampInserted);
        } catch (ParseException e) {
            //Ignore
        }
    }

    @Test(groups = "TableTest", description = "Check date time operation")
    public void testDateTimeAsTimeStruct() {
        BValue[] returns = BRunUtil.invoke(result,  "testDateTimeAsTimeStruct");
        Assert.assertEquals(returns.length, 8);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), ((BInteger) returns[1]).intValue());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), ((BInteger) returns[3]).intValue());
        Assert.assertEquals(((BInteger) returns[4]).intValue(), ((BInteger) returns[5]).intValue());
        Assert.assertEquals(((BInteger) returns[6]).intValue(), ((BInteger) returns[7]).intValue());
    }

    @Test(groups = "TableTest", description = "Check date time operation")
    public void testDateTimeInt() {
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

        BValue[] returns = BRunUtil.invoke(result,  "testDateTimeInt", args);
        Assert.assertEquals(returns.length, 4);

        long dateReturnedEpoch = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(dateReturnedEpoch, dateInserted);

        long timeReturnedEpoch = ((BInteger) returns[1]).intValue();
        Assert.assertEquals(timeReturnedEpoch, timeInserted);

        long timestampReturnedEpoch = ((BInteger) returns[2]).intValue();
        Assert.assertEquals(timestampReturnedEpoch, timestampInserted);

        long datetimeReturnedEpoch = ((BInteger) returns[3]).intValue();
        Assert.assertEquals(datetimeReturnedEpoch, timestampInserted);
    }

    @Test(groups = "TableTest", description = "Check JSON conversion with null values.")
    public void testJsonWithNull() {
        BValue[] returns = BRunUtil.invoke(result,  "testJsonWithNull");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"INT_TYPE\":0,\"LONG_TYPE\":0,\"FLOAT_TYPE\":0.0,\"DOUBLE_TYPE\":0.0,\"BOOLEAN_TYPE\":false,"
                        + "\"STRING_TYPE\":null}]");
    }

    @Test(groups = "TableTest", description = "Check xml conversion with null values.")
    public void testXmlWithNull() {
        BValue[] returns = BRunUtil.invoke(result, "testXmlWithNull");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<results><result><INT_TYPE>0</INT_TYPE><LONG_TYPE>0</LONG_TYPE><FLOAT_TYPE>0.0</FLOAT_TYPE>"
                        + "<DOUBLE_TYPE>0.0</DOUBLE_TYPE><BOOLEAN_TYPE>false</BOOLEAN_TYPE>"
                        + "<STRING_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>"
                        + "</result></results>");
    }

    @Test(groups = "TableTest", description = "Check xml conversion within transaction.")
    public void testToXmlWithinTransaction() {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlWithinTransaction");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals((returns[0]).stringValue(), "<results><result><INT_TYPE>1</INT_TYPE><LONG_TYPE>"
                + "9223372036854774807</LONG_TYPE></result></results>");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = "TableTest", description = "Check JSON conversion within transaction.")
    public void testToJsonWithinTransaction() {
        BValue[] returns = BRunUtil.invoke(result,  "testToJsonWithinTransaction");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals((returns[0]).stringValue(), "[{\"INT_TYPE\":1,\"LONG_TYPE\":9223372036854774807}]");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = "TableTest", description = "Check blob data support.")
    public void testBlobData() {
        BValue[] returns = BRunUtil.invoke(result,  "testBlobData");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((returns[0]).stringValue(), "wso2 ballerina blob test.");
    }

    @Test(groups = "TableTest", description = "Check values retrieved with column alias.")
    public void testColumnAlias() {
        BValue[] returns = BRunUtil.invoke(result, "testColumnAlias");
        Assert.assertEquals(returns.length, 7);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BInteger) returns[6]).intValue(), 100);
    }

    @Test(groups = "TableTest", description = "Check inserting blob data.")
    public void testBlobInsert() {
        BValue[] returns = BRunUtil.invoke(result, "testBlobInsert");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = "TableTest", description = "Check whether printing of table variables is handled properly.")
    public void testTablePrintAndPrintln() throws IOException {
        PrintStream original = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final String expected = "\n";
            BRunUtil.invoke(result, "testTablePrintAndPrintln");
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test(groups = "TableTest", description = "Check auto close resources in table.")
    public void testTableAutoClose() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAutoClose");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(returns[1].stringValue(), "[{\"INT_TYPE\":1,\"LONG_TYPE\":9223372036854774807,"
                + "\"FLOAT_TYPE\":123.34,\"DOUBLE_TYPE\":2.139095039E9,\"BOOLEAN_TYPE\":true,"
                + "\"STRING_TYPE\":\"Hello\"}]");
    }

    @Test(groups = "TableTest", description = "Check manual close resources in table.")
    public void testTableManualClose() {
        BValue[] returns = BRunUtil.invoke(result, "testTableManualClose");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(dependsOnGroups = "TableTest", description = "Check whether all sql connectors are closed properly.")
    public void testCloseConnectionPool() {
        BValue[] returns = BRunUtil.invoke(result, "testCloseConnectionPool");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = "TableTest", description = "Check select data with multiple rows for primitive types.")
    public void testMutltipleRows() {
        BValue[] returns = BRunUtil.invoke(result, "testMutltipleRows");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
    }

    @Test(groups = "TableTest", description = "Check select data with multiple rows accessing without getNext.")
    public void testMutltipleRowsWithoutLoop() {
        BValue[] returns = BRunUtil.invoke(result, "testMutltipleRowsWithoutLoop");
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 200);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 100);
        Assert.assertEquals(returns[4].stringValue(), "200_100_NOT");
        Assert.assertEquals(returns[5].stringValue(), "200_HAS_HAS_100_NO_NO");
    }

    @Test(groups = "TableTest", description = "Check select data with multiple rows accessing without getNext.")
    public void testHasNextWithoutConsume() {
        BValue[] returns = BRunUtil.invoke(result, "testHasNextWithoutConsume");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), true);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
    }

    @Test(groups = "TableTest", description = "Check get float and double types.")
    public void testGetFloatTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testGetFloatTypes");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 238999.34);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 238999.34);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 238999.34);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 238999.34);
    }

    @Test(groups = "TableTest", description = "Check array data insert and println on arrays")
    public void testArrayDataInsertAndPrint() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayDataInsertAndPrint");
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 5);
        Assert.assertEquals(((BInteger) returns[5]).intValue(), 2);
    }

    @Test(groups = "TableTest", description = "Check get float and double min and max types.")
    public void testSignedIntMaxMinValues() {
        BValue[] returns = BRunUtil.invoke(result, "testSignedIntMaxMinValues");
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

    @Test(groups = "TableTest", description = "Check blob binary and clob types types.")
    public void testComplexTypeInsertAndRetrieval() {
        BValue[] returns = BRunUtil.invoke(result, "testComplexTypeInsertAndRetrieval");
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

    @Test(groups = "TableTest", description = "Check result sets with same column name or complex name.")
    public void testJsonXMLConversionwithDuplicateColumnNames() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonXMLConversionwithDuplicateColumnNames");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals((returns[0]).stringValue(), "[{\"ROW_ID\":1,\"INT_TYPE\":1,\"DATATABLEREP.ROW_ID\":1,"
                + "\"DATATABLEREP.INT_TYPE\":100}]");
        Assert.assertEquals((returns[1]).stringValue(), "<results><result><ROW_ID>1</ROW_ID><INT_TYPE>1</INT_TYPE>"
                + "<DATATABLEREP.ROW_ID>1</DATATABLEREP.ROW_ID><DATATABLEREP.INT_TYPE>100</DATATABLEREP.INT_TYPE>"
                + "</result></results>");
    }

    @Test(groups = "TableTest", description = "Check result sets with same column name or complex name.")
    public void testStructFieldNotMatchingColumnName() {
        BValue[] returns = BRunUtil.invoke(result, "testStructFieldNotMatchingColumnName");
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 100);
    }

    @Test(groups = "TableTest", description = "Check retrieving data using foreach")
    public void testGetPrimitiveTypesWithForEach() {
        BValue[] returns = BRunUtil.invoke(result, "testGetPrimitiveTypesWithForEach");
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    @Test(groups = "TableTest", description = "Check retrieving data using foreach with multiple rows")
    public void testMutltipleRowsWithForEach() {
        BValue[] returns = BRunUtil.invoke(result, "testMutltipleRowsWithForEach");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
    }

    @Test(groups = "TableTest",
          description = "Test adding data to database table",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*message: data cannot be added to a table returned from a database.*")
    public void testTableAddInvalid() {
        BRunUtil.invoke(result, "testTableAddInvalid");
    }

    @Test(groups = "TableTest",
          description = "Test deleting data from a database table",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*message: data cannot be deleted from a table returned from a database.*")
    public void testTableRemoveInvalid() {
        BRunUtil.invoke(result, "testTableRemoveInvalid");
    }
    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
