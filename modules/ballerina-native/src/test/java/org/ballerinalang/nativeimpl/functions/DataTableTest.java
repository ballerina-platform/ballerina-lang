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
import org.ballerinalang.model.values.BDouble;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BLong;
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
        Assert.assertEquals(((BLong) returns[1]).longValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34f);
        Assert.assertEquals(((BDouble) returns[3]).doubleValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    @Test(description = "Check getByName methods for primitive types.")
    public void testGetXXXByName() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getXXXByName");

        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BLong) returns[1]).longValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34f);
        Assert.assertEquals(((BDouble) returns[3]).doubleValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    @Test(description = "Check toJson methods.")
    public void testToJson() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "toJson");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"INT_TYPE\":1,\"LONG_TYPE\":9223372036854774807,\"FLOAT_TYPE\":123.34,"
                        + "\"DOUBLE_TYPE\":2.139095039E9,\"BOOLEAN_TYPE\":true,\"STRING_TYPE\":\"Hello\"}]");
    }

    @Test(description = "Check toXml methods with wrapper element.")
    public void testToXmlWithWrapper() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "toXmlWithWrapper");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<types><type><INT_TYPE>1</INT_TYPE><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                        + "<FLOAT_TYPE>123.34</FLOAT_TYPE><DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE>"
                        + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE></type></types>");
    }

    @Test(description = "Check toXml methods with complex element.")
    public void testToXmlComplex() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "toXmlComplex");

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
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getByName");

        Assert.assertEquals(returns.length, 5);
        // Create text file with some content. Generate Hex value of that. Insert to database.
        // Implementation will return base64encoded value of that text content. Verify that value.
        Assert.assertEquals((returns[0]).stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals((returns[1]).stringValue(), "very long text");
        Assert.assertEquals(((BLong) returns[2]).longValue(), 21945000);
        Assert.assertEquals(((BLong) returns[3]).longValue(), 1486060200000L);
        Assert.assertEquals(((BLong) returns[4]).longValue(), 1486102980000L);
    }

    @Test(description = "Check getByName methods for complex types.")
    public void testGetByIndex() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getByIndex");

        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals((returns[0]).stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals((returns[1]).stringValue(), "very long text");
        Assert.assertEquals(((BLong) returns[2]).longValue(), 21945000);
        Assert.assertEquals(((BLong) returns[3]).longValue(), 1486060200000L);
        Assert.assertEquals(((BLong) returns[4]).longValue(), 1486102980000L);
    }

    @Test(description = "Check getObjectAsStringByName methods for complex types.")
    public void testGetObjectAsStringByName() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getObjectAsStringByName");

        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals((returns[0]).stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals((returns[1]).stringValue(), "very long text");
        Assert.assertEquals(returns[2].stringValue(), "21945000");
        Assert.assertEquals(returns[3].stringValue(), "1486060200000");
        Assert.assertEquals(returns[4].stringValue(), "1486102980000");
    }

    @Test(description = "Check getObjectAsStringByIndex methods for complex types.")
    public void testGetObjectAsStringByIndex() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getObjectAsStringByIndex");

        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals((returns[0]).stringValue(), "d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==");
        Assert.assertEquals((returns[1]).stringValue(), "very long text");
        Assert.assertEquals(returns[2].stringValue(), "21945000");
        Assert.assertEquals(returns[3].stringValue(), "1486060200000");
        Assert.assertEquals(returns[4].stringValue(), "1486102980000");
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
        BMap<BString, BLong> longArray = (BMap) returns[1];
        Assert.assertTrue(longArray.get(new BString("0")) instanceof BLong);
        Assert.assertEquals(longArray.get(new BString("0")).longValue(), 100000000);
        Assert.assertEquals(longArray.get(new BString("1")).longValue(), 200000000);
        Assert.assertEquals(longArray.get(new BString("2")).longValue(), 300000000);

        Assert.assertTrue(returns[2] instanceof BMap);
        BMap<BString, BDouble> doubleArray = (BMap) returns[2];
        Assert.assertTrue(doubleArray.get(new BString("0")) instanceof BDouble);
        Assert.assertEquals(doubleArray.get(new BString("0")).doubleValue(), 245.23);
        Assert.assertEquals(doubleArray.get(new BString("1")).doubleValue(), 5559.49);
        Assert.assertEquals(doubleArray.get(new BString("2")).doubleValue(), 8796.123);

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
        BMap<BString, BLong> longArray = (BMap) returns[1];
        Assert.assertTrue(longArray.get(new BString("0")) instanceof BLong);
        Assert.assertEquals(longArray.get(new BString("0")).longValue(), 100000000);
        Assert.assertEquals(longArray.get(new BString("1")).longValue(), 200000000);
        Assert.assertEquals(longArray.get(new BString("2")).longValue(), 300000000);

        Assert.assertTrue(returns[2] instanceof BMap);
        BMap<BString, BDouble> doubleArray = (BMap) returns[2];
        Assert.assertTrue(doubleArray.get(new BString("0")) instanceof BDouble);
        Assert.assertEquals(doubleArray.get(new BString("0")).doubleValue(), 245.23);
        Assert.assertEquals(doubleArray.get(new BString("1")).doubleValue(), 5559.49);
        Assert.assertEquals(doubleArray.get(new BString("2")).doubleValue(), 8796.123);

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

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(SQLDBUtils.DB_DIRECTORY));
    }
}
