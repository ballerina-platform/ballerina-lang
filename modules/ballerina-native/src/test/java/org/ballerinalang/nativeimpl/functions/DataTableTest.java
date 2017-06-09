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

    @Test(description = "Check getByIndex methods for primitive types.")
    public void testGetXXXByIndex() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getXXXByIndex");

        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    @Test(description = "Check getByName methods for primitive types.")
    public void testGetXXXByName() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getXXXByName");

        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    @Test(description = "Check toJson methods.")
    public void testToJson() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "toJson");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"INT_TYPE\":1,\"LONG_TYPE\":9223372036854774807,\"FLOAT_TYPE\":123.34,"
                        + "\"DOUBLE_TYPE\":2.139095039E9,\"BOOLEAN_TYPE\":true,\"STRING_TYPE\":\"Hello\"}]");
    }

    @Test(description = "Check toXml methods with wrapper element.")
    public void testToXmlWithWrapper() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "toXmlWithWrapper");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<types><type><INT_TYPE>1</INT_TYPE><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                        + "<FLOAT_TYPE>123.34</FLOAT_TYPE><DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE>"
                        + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE></type></types>");
    }

    @Test(description = "Check toXml methods with complex element.")
    public void testToXmlComplex() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "toXmlComplex");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<types><type><INT_TYPE>1</INT_TYPE><INT_ARRAY><element>1</element><element>2</element>"
                        + "<element>3</element></INT_ARRAY><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                        + "<LONG_ARRAY><element>100000000</element><element>200000000</element>"
                        + "<element>300000000</element></LONG_ARRAY><FLOAT_TYPE>123.34</FLOAT_TYPE>"
                        + "<FLOAT_ARRAY><element>245.23</element><element>5559.49</element>"
                        + "<element>8796.123</element></FLOAT_ARRAY><DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE>"
                        + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE><DOUBLE_ARRAY>"
                        + "<element>245.23</element><element>5559.49</element><element>8796.123</element>"
                        + "</DOUBLE_ARRAY><BOOLEAN_ARRAY><element>true</element><element>false</element>"
                        + "<element>true</element></BOOLEAN_ARRAY><STRING_ARRAY><element>Hello</element>"
                        + "<element>Ballerina</element></STRING_ARRAY></type></types>");
    }

    @Test(description = "Check getByName methods for complex types.")
    public void testGetByName() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getByName");

        Assert.assertEquals(returns.length, 5);
        // Create text file with some content. Generate Hex value of that. Insert to database.
        // Implementation will return base64encoded value of that text content. Verify that value.
        Assert.assertEquals((returns[0]).stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals((returns[1]).stringValue(), "very long text");
        //        Assert.assertEquals(((BLong) returns[2]).longValue(), 21945000);
        //        Assert.assertEquals(((BLong) returns[3]).longValue(), 1486060200000L);
        //        Assert.assertEquals(((BLong) returns[4]).longValue(), 1486102980000L);
    }

    @Test(description = "Check getByName methods for complex types.")
    public void testGetByIndex() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getByIndex");

        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals((returns[0]).stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals((returns[1]).stringValue(), "very long text");
        Assert.assertEquals((returns[5]).stringValue(), "d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu");
        //        Assert.assertEquals(((BLong) returns[2]).longValue(), 21945000);
        //        Assert.assertEquals(((BLong) returns[3]).longValue(), 1486060200000L);
        //        Assert.assertEquals(((BLong) returns[4]).longValue(), 1486102980000L);
    }

    @Test(description = "Check getObjectAsStringByName methods for complex types.")
    public void testGetObjectAsStringByName() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getObjectAsStringByName");

        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals((returns[0]).stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals((returns[1]).stringValue(), "very long text");
        Assert.assertTrue(returns[2].stringValue().contains("11:35:45"));
        Assert.assertTrue(returns[3].stringValue().contains("2017-02-03"));
        Assert.assertTrue(returns[4].stringValue().contains("2017-02-03T11:53:00"));
        Assert.assertTrue(returns[5].stringValue().contains("2017-02-03T11:53:00"));
    }

    @Test(description = "Check getObjectAsStringByIndex methods for complex types.")
    public void testGetObjectAsStringByIndex() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getObjectAsStringByIndex");

        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals((returns[0]).stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals((returns[1]).stringValue(), "very long text");
        Assert.assertTrue(returns[2].stringValue().contains("11:35:45"));
        Assert.assertTrue(returns[3].stringValue().contains("2017-02-03"));
        Assert.assertTrue(returns[4].stringValue().contains("2017-02-03T11:53:00"));
        Assert.assertTrue(returns[5].stringValue().contains("2017-02-03T11:53:00"));
    }

    @SuppressWarnings("unchecked")
    @Test(description = "Check getXXXArray methods for complex types.")
    public void testGetArrayByName() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getArrayByName");
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

    @SuppressWarnings("unchecked")
    @Test(description = "Check getXXXArray methods for complex types.")
    public void testGetArrayByIndex() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getArrayByIndex");
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

    @Test(description = "Check date time operation")
    public void testDateTime() {
        BValue[] args = new BValue[3];
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.HOUR, 14);
        cal.set(Calendar.MINUTE, 15);
        cal.set(Calendar.SECOND, 23);
        long time = cal.getTimeInMillis();
        args[0] = new BInteger(time);

        cal.clear();
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, 5);
        cal.set(Calendar.DAY_OF_MONTH, 23);
        long date = cal.getTimeInMillis();
        args[1] = new BInteger(date);

        cal.clear();
        cal.set(Calendar.HOUR, 16);
        cal.set(Calendar.MINUTE, 33);
        cal.set(Calendar.SECOND, 55);
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 25);
        long timestamp = cal.getTimeInMillis();
        args[2] = new BInteger(timestamp);

        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testDateTime", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), time);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), date);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), timestamp);
    }

    @Test(description = "Check toJson methods with null values.")
    public void testJsonWithNull() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testJsonWithNull");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"INT_TYPE\":0,\"LONG_TYPE\":0,\"FLOAT_TYPE\":0.0,\"DOUBLE_TYPE\":0.0,\"BOOLEAN_TYPE\":false,"
                        + "\"STRING_TYPE\":null}]");
    }

    @Test(description = "Check toXml method with null values.")
    public void testXmlWithNull() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testXmlWithNull");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<types><type><INT_TYPE>0</INT_TYPE><LONG_TYPE>0</LONG_TYPE><FLOAT_TYPE>0.0</FLOAT_TYPE>"
                        + "<DOUBLE_TYPE>0.0</DOUBLE_TYPE><BOOLEAN_TYPE>false</BOOLEAN_TYPE>"
                        + "<STRING_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>"
                        + "</type></types>");
    }

    @Test(description = "Check getByIndex methods for primitive types.")
    public void getXXXByIndexWithStruct() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getXXXByIndexWithStruct");

        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    @Test(description = "Check getObjectAsStringByName methods for complex types.")
    public void getObjectAsStringByNameWithStruct() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getObjectAsStringByNameWithStruct");

        Assert.assertEquals(returns.length, 7);
        Assert.assertEquals((returns[0]).stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals((returns[1]).stringValue(), "very long text");
        Assert.assertTrue(returns[2].stringValue().contains("11:35:45"));
        Assert.assertTrue(returns[3].stringValue().contains("2017-02-03"));
        Assert.assertTrue(returns[4].stringValue().contains("2017-02-03T11:53:00"));
        Assert.assertTrue(returns[5].stringValue().contains("2017-02-03T11:53:00"));
        Assert.assertEquals((returns[6]).stringValue(), "d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu");
    }

    @SuppressWarnings("unchecked")
    @Test(description = "Check getXXXArray methods for complex types.")
    public void testGetArrayByNameWithStruct() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testGetArrayByNameWithStruct");
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

    @Test(description = "Check toJson methods.")
    public void testtoJsonWithStruct() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testtoJsonWithStruct");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"INT_TYPE\":1,\"LONG_TYPE\":9223372036854774807,\"FLOAT_TYPE\":123.34,"
                        + "\"DOUBLE_TYPE\":2.139095039E9,\"BOOLEAN_TYPE\":true,\"STRING_TYPE\":\"Hello\"}]");
    }

    @Test(description = "Check toXml methods with wrapper element.")
    public void testToXmlWithStruct() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXmlWithStruct");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<results><result><INT_TYPE>1</INT_TYPE><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                        + "<FLOAT_TYPE>123.34</FLOAT_TYPE><DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE>"
                        + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE></result></results>");
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
