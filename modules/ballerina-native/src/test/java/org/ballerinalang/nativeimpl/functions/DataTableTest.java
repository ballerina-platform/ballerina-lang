/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.functions;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.nativeimpl.util.SQLDBUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Test Native functions in ballerina.model.datatables.
 *
 * @since 0.8.0
 */
public class DataTableTest {

    private ProgramFile bLangProgram;
    private static final String DB_NAME = "TEST_DATA_TABLE_DB";

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.getProgramFile("samples/datatableTest.bal");
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initDatabase(SQLDBUtils.DB_DIRECTORY, DB_NAME, "datafiles/DataTableDataFile.sql");
    }

    @Test(groups = "DatatableTest", description = "Check getByIndex methods for primitive types.")
    public void testGetPrimitiveTypes() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testGetPrimitiveTypes");

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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToJson");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"INT_TYPE\":1,\"LONG_TYPE\":9223372036854774807,\"FLOAT_TYPE\":123.34,"
                        + "\"DOUBLE_TYPE\":2.139095039E9,\"BOOLEAN_TYPE\":true,\"STRING_TYPE\":\"Hello\"}]");
    }

    @Test(groups = "DatatableTest", description = "Check toXml methods with wrapper element.")
    public void testToXml() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXml");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<results><result><INT_TYPE>1</INT_TYPE><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                        + "<FLOAT_TYPE>123.34</FLOAT_TYPE><DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE>"
                        + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE></result></results>");
    }

    @Test(groups = "DatatableTest", description = "Check toXml methods with complex element.")
    public void testToXmlComplex() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "toXmlComplex");

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

    @Test(groups = "DatatableTest", description = "Check getObjectAsStringByName methods for complex types.")
    public void testGetComplexTypes() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testGetComplexTypes");

        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals((returns[0]).stringValue(), "wso2 ballerina blob test.");
        Assert.assertEquals((returns[1]).stringValue(), "very long text");
        Assert.assertEquals((returns[2]).stringValue(), "wso2 ballerina binary test.");
    }

    @SuppressWarnings("unchecked")
    @Test(groups = "DatatableTest", description = "Check getXXXArray methods for complex types.")
    public void testArrayData() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testArrayData");
        Assert.assertEquals(returns.length, 5);
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap<BString, BInteger> intArray = (BMap) returns[0];
        Assert.assertTrue(intArray.get(new BString("0")) instanceof BInteger);
        Assert.assertEquals(intArray.get(new BString("0")).intValue(), 1);
        Assert.assertEquals(intArray.get(new BString("1")).intValue(), 2);
        Assert.assertEquals(intArray.get(new BString("2")).intValue(), 3);

        Assert.assertTrue(returns[1] instanceof BMap);
        BMap<BString, BInteger> longArray = (BMap) returns[1];
        Assert.assertTrue(longArray.get(new BString("0")) instanceof BInteger);
        Assert.assertEquals(longArray.get(new BString("0")).intValue(), 100000000);
        Assert.assertEquals(longArray.get(new BString("1")).intValue(), 200000000);
        Assert.assertEquals(longArray.get(new BString("2")).intValue(), 300000000);

        Assert.assertTrue(returns[2] instanceof BMap);
        BMap<BString, BFloat> doubleArray = (BMap) returns[2];
        Assert.assertTrue(doubleArray.get(new BString("0")) instanceof BFloat);
        Assert.assertEquals(doubleArray.get(new BString("0")).floatValue(), 245.23);
        Assert.assertEquals(doubleArray.get(new BString("1")).floatValue(), 5559.49);
        Assert.assertEquals(doubleArray.get(new BString("2")).floatValue(), 8796.123);

        Assert.assertTrue(returns[3] instanceof BMap);
        BMap<BString, BString> stringArray = (BMap) returns[3];
        Assert.assertTrue(stringArray.get(new BString("0")) instanceof BString);
        Assert.assertEquals(stringArray.get(new BString("0")).stringValue(), "Hello");
        Assert.assertEquals(stringArray.get(new BString("1")).stringValue(), "Ballerina");

        Assert.assertTrue(returns[4] instanceof BMap);
        BMap<BString, BBoolean> booleanArray = (BMap) returns[4];
        Assert.assertTrue(booleanArray.get(new BString("0")) instanceof BBoolean);
        Assert.assertEquals(booleanArray.get(new BString("0")).booleanValue(), true);
        Assert.assertEquals(booleanArray.get(new BString("1")).booleanValue(), false);
        Assert.assertEquals(booleanArray.get(new BString("2")).booleanValue(), true);
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

        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testDateTime", args);

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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testJsonWithNull");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"INT_TYPE\":0,\"LONG_TYPE\":0,\"FLOAT_TYPE\":0.0,\"DOUBLE_TYPE\":0.0,\"BOOLEAN_TYPE\":false,"
                        + "\"STRING_TYPE\":null}]");
    }

    @Test(groups = "DatatableTest", description = "Check toXml method with null values.")
    public void testXmlWithNull() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testXmlWithNull");

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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXmlWithinTransaction");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals((returns[0]).stringValue(), "<results><result><INT_TYPE>1</INT_TYPE><LONG_TYPE>"
                + "9223372036854774807</LONG_TYPE></result></results>");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = "DatatableTest", description = "Check toJson methods within transaction.")
    public void testToJsonWithinTransaction() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToJsonWithinTransaction");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals((returns[0]).stringValue(), "[{\"INT_TYPE\":1,\"LONG_TYPE\":9223372036854774807}]");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = "DatatableTest", description = "Check blob data support.")
    public void testBlobData() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testBlobData");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((returns[0]).stringValue(), "wso2 ballerina blob test.");
    }

    @Test(groups = "DatatableTest", description = "Check getByIndex methods for primitive types.")
    public void testColumnAlias() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testColumnAlias");

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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testBlobInsert");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = "DatatableTest", description = "Check auto close resources in datatable.")
    public void testDatatableAutoClose() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testDatatableAutoClose");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(returns[1].stringValue(), "[{\"INT_TYPE\":1,\"LONG_TYPE\":9223372036854774807,"
                + "\"FLOAT_TYPE\":123.34,\"DOUBLE_TYPE\":2.139095039E9,\"BOOLEAN_TYPE\":true,"
                + "\"STRING_TYPE\":\"Hello\"}]");
    }

    @Test(groups = "DatatableTest", description = "Check manual close resources in datatable.")
    public void testDatatableManualClose() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testDatatableManualClose");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(dependsOnGroups = "DatatableTest", description = "Check all sql connectors are closed properly.")
    public void testCloseConnectionPool() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testCloseConnectionPool");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
