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

import org.ballerinalang.model.BLangProgram;
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

    BLangProgram bLangProgram;
    private static final String DB_NAME = "TEST_DATA_TABLE_DB";

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("samples/datatableTest.bal");
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initDatabase(SQLDBUtils.DB_DIRECTORY, DB_NAME, "datafiles/DataTableDataFile.sql");
    }

    @Test(description = "Check getByIndex methods for primitive types.")
    public void testGetXXXByIndex() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getXXXByIndex");

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
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getXXXByName");

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
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "toJson");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"int_type\":1,\"long_type\":9223372036854774807,\"float_type\":123.34,"
                        + "\"double_type\":2.139095039E9,\"boolean_type\":true,\"string_type\":\"Hello\"}]");
    }

    @Test(description = "Check toXml methods with wrapper element.")
    public void testToXmlWithWrapper() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "toXmlWithWrapper");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<types><type><int_type>1</int_type><long_type>9223372036854774807</long_type>"
                        + "<float_type>123.34</float_type><double_type>2.139095039E9</double_type>"
                        + "<boolean_type>true</boolean_type><string_type>Hello</string_type></type></types>");
    }

    @Test(description = "Check toXml methods with complex element.")
    public void testToXmlComplex() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "toXmlComplex");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<types><type><int_type>1</int_type><int_array><element>1</element><element>2</element>"
                        + "<element>3</element></int_array><long_type>9223372036854774807</long_type>"
                        + "<long_array><element>100000000</element><element>200000000</element>"
                        + "<element>300000000</element></long_array><float_type>123.34</float_type>"
                        + "<float_array><element>245.23</element><element>5559.49</element>"
                        + "<element>8796.123</element></float_array><double_type>2.139095039E9</double_type>"
                        + "<boolean_type>true</boolean_type><string_type>Hello</string_type><double_array>"
                        + "<element>245.23</element><element>5559.49</element><element>8796.123</element>"
                        + "</double_array><boolean_array><element>true</element><element>false</element>"
                        + "<element>true</element></boolean_array><string_array><element>Hello</element>"
                        + "<element>Ballerina</element></string_array></type></types>");
    }

    @Test(description = "Check getByName methods for complex types.")
    public void testGetByName() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getByName");

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
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getByIndex");

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
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getObjectAsStringByName");

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
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getObjectAsStringByIndex");

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
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getArrayByName");
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
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getArrayByIndex");
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

        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testDateTime", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), time);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), date);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), timestamp);
    }

    @Test(description = "Check toJson methods with null values.")
    public void testJsonWithNull() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testJsonWithNull");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"int_type\":0,\"long_type\":0,\"float_type\":0.0,\"double_type\":0.0,\"boolean_type\":false,"
                        + "\"string_type\":null}]");
    }

    @Test(description = "Check toXml method with null values.")
    public void testXmlWithNull() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testXmlWithNull");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<types><type><int_type>0</int_type><long_type>0</long_type><float_type>0.0</float_type>"
                        + "<double_type>0.0</double_type><boolean_type>false</boolean_type>"
                        + "<string_type xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>"
                        + "</type></types>");
    }

    @Test(description = "Check getByIndex methods for primitive types.")
    public void getXXXByIndexWithStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getXXXByIndexWithStruct");

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
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getObjectAsStringByNameWithStruct");

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
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testGetArrayByNameWithStruct");
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
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testtoJsonWithStruct");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"int_type\":1,\"long_type\":9223372036854774807,\"float_type\":123.34,"
                        + "\"double_type\":2.139095039E9,\"boolean_type\":true,\"string_type\":\"Hello\"}]");
    }

    @Test(description = "Check toXml methods with wrapper element.")
    public void testToXmlWithStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testToXmlWithStruct");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<results><result><int_type>1</int_type><long_type>9223372036854774807</long_type>"
                        + "<float_type>123.34</float_type><double_type>2.139095039E9</double_type>"
                        + "<boolean_type>true</boolean_type><string_type>Hello</string_type></result></results>");
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
