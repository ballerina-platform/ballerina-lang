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
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.test.utils.ResponseReader;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.ballerinalang.test.utils.SQLDBUtils.DBType;
import org.ballerinalang.test.utils.SQLDBUtils.FileBasedTestDatabase;
import org.ballerinalang.test.utils.SQLDBUtils.TestDatabase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * Class to test functionality of tables.
 */
public class TableTest {

    private CompileResult result;
    private CompileResult resultNegative;
    private CompileResult nillableMappingNegativeResult;
    private CompileResult nillableMappingResult;
    private CompileResult service;
    private static final String DB_NAME_H2 = "TEST_DATA_TABLE_H2";
    private TestDatabase testDatabase;
    private static final String TABLE_TEST = "TableTest";

    private static final String TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD =
            "Trying to assign a Nil value to a non-nillable field";
    private static final String INVALID_UNION_FIELD_ASSIGNMENT =
            "Corresponding Union type in the record is not an assignable nillable type";
    private static final String TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_ARRAY_FIELD =
            "Trying to assign an array containing NULL values to an array of a non-nillable element type";
    private static final double DELTA = 0.01;

    @BeforeClass
    public void setup() {
        testDatabase = new FileBasedTestDatabase(DBType.H2, "datafiles/sql/TableTest_H2_Data.sql",
                SQLDBUtils.DB_DIRECTORY, DB_NAME_H2);

        result = BCompileUtil.compile("test-src/types/table/table_type.bal");
        resultNegative = BCompileUtil.compile("test-src/types/table/table_type_negative.bal");
        nillableMappingNegativeResult = BCompileUtil
                .compile("test-src/types/table/table_nillable_mapping_negative.bal");
        nillableMappingResult = BCompileUtil.compile("test-src/types/table/table_nillable_mapping.bal");
        service = BServiceUtil.setupProgramFile(this, "test-src/types/table/table_to_json_service_test.bal");
    }

    @Test(groups = TABLE_TEST, description = "Check retrieving primitive types.")
    public void testGetPrimitiveTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testGetPrimitiveTypes");
        Assert.assertEquals(returns.length, 7);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BDecimal) returns[6]).decimalValue(), new BigDecimal("23.45"));
    }

    @Test(groups = TABLE_TEST, description = "Check table to JSON conversion.")
    public void testToJson() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJson");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        String expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, "
                + "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to JSON conversion.")
    public void testToJsonComplexTypes() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJsonComplexTypes");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        String expected = "[{\"BLOB_TYPE\":\"d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==\", \"CLOB_TYPE\":\"very long "
                + "text\", \"BINARY_TYPE\":\"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to JSON conversion.")
    public void testToJsonComplexTypesNil() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJsonComplexTypesNil");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        String expected = "[{\"BLOB_TYPE\":null, \"CLOB_TYPE\":null, \"BINARY_TYPE\":null}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to XML conversion.")
    public void testToXml() {
        BValue[] returns = BRunUtil.invoke(result, "testToXml");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String expected = "<results><result><INT_TYPE>1</INT_TYPE><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                + "<FLOAT_TYPE>123.34</FLOAT_TYPE><DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE>"
                + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE></result></results>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to XML conversion.")
    public void testToXmlComplexTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlComplexTypes");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String expected = "<results><result><BLOB_TYPE>d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==</BLOB_TYPE><CLOB_TYPE"
                + ">very long text</CLOB_TYPE><BINARY_TYPE>d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu</BINARY_TYPE></result"
                + "></results>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to XML conversion.")
    public void testToXmlComplexTypesNil() {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlComplexTypesNil");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String expected1 = "<results><result><BLOB_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:nil=\"true\"/><CLOB_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:nil=\"true\"/><BINARY_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:nil=\"true\"/></result></results>";
        String expected2 = "<results><result><BLOB_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:nil=\"true\"></BLOB_TYPE><CLOB_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:nil=\"true\"></CLOB_TYPE><BINARY_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:nil=\"true\"></BINARY_TYPE></result></results>";
        Assert.assertTrue(expected1.equals(returns[0].stringValue()) || expected2.equals(returns[0].stringValue()));
    }

    @Test(groups = TABLE_TEST, description = "Check xml streaming when result set consumed once.")
    public void testToXmlMultipleConsume() {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlMultipleConsume");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String expected = "<results><result><INT_TYPE>1</INT_TYPE>"
                + "<LONG_TYPE>9223372036854774807</LONG_TYPE><FLOAT_TYPE>123.34</FLOAT_TYPE>"
                + "<DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE><BOOLEAN_TYPE>true</BOOLEAN_TYPE>"
                + "<STRING_TYPE>Hello</STRING_TYPE></result></results>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to XML conversion with concat operation.")
    public void testToXmlWithAdd() {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlWithAdd");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String expected = "<results><result><INT_TYPE>1</INT_TYPE></result></results><results><result><INT_TYPE>1"
                + "</INT_TYPE></result></results>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check xml streaming when result set consumed once.")
    public void testToJsonMultipleConsume() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJsonMultipleConsume");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        String expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, "
                + "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = {TABLE_TEST}, description = "Check xml conversion with complex element.")
    public void testToXmlComplex() {
        BValue[] returns = BRunUtil.invoke(result, "toXmlComplex");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);

        String expected = "<results><result><INT_TYPE>1</INT_TYPE><INT_ARRAY><element>1</element><element>2</element>"
                + "<element>3</element></INT_ARRAY><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                + "<LONG_ARRAY><element>100000000</element><element>200000000</element>"
                + "<element>300000000</element></LONG_ARRAY><FLOAT_TYPE>123.34</FLOAT_TYPE>"
                + "<FLOAT_ARRAY><element>245.23</element><element>5559.49</element>"
                + "<element>8796.123</element></FLOAT_ARRAY><DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE>"
                + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE>"
                + "<DECIMAL_TYPE>234.56</DECIMAL_TYPE>"
                + "<DOUBLE_ARRAY><element>245.23</element><element>5559.49</element><element>8796.123</element>"
                + "</DOUBLE_ARRAY><BOOLEAN_ARRAY><element>true</element><element>false</element>"
                + "<element>true</element></BOOLEAN_ARRAY><STRING_ARRAY><element>Hello</element>"
                + "<element>Ballerina</element></STRING_ARRAY></result></results>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    // Disabling for MySQL as array types are not supported.
    @Test(groups = {TABLE_TEST}, description = "Check xml conversion with complex element.")
    public void testToXmlComplexWithStructDef () {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlComplexWithStructDef");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String expected = "<results><result><i>1</i><iA><element>1</element>"
                + "<element>2</element><element>3</element></iA><l>9223372036854774807</l>"
                + "<lA><element>100000000</element><element>200000000</element><element>300000000</element></lA>"
                + "<f>123.34</f><fA><element>245.23</element><element>5559.49</element><element>8796.123</element>"
                + "</fA><d>2.139095039E9</d><b>true</b><s>Hello</s>"
                + "<dA><element>245.23</element><element>5559.49</element><element>8796.123</element></dA>"
                + "<bA><element>true</element><element>false</element><element>true</element></bA>"
                + "<sA><element>Hello</element><element>Ballerina</element></sA></result></results>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = {TABLE_TEST}, description = "Check json conversion with complex element.")
    public void testToJsonComplex() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJsonComplex");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);

        String expected = "[{\"INT_TYPE\":1, \"INT_ARRAY\":[1, 2, 3], "
                + "\"LONG_TYPE\":9223372036854774807, \"LONG_ARRAY\":[100000000, 200000000, 300000000], "
                + "\"FLOAT_TYPE\":123.34, \"FLOAT_ARRAY\":[245.23, 5559.49, 8796.123], "
                + "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\", "
                + "\"DECIMAL_TYPE\":234.56, \"DOUBLE_ARRAY\":[245.23, 5559.49, 8796.123], "
                + "\"BOOLEAN_ARRAY\":[true, false, true], \"STRING_ARRAY\":[\"Hello\", \"Ballerina\"]}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = {TABLE_TEST}, description = "Check json conversion with complex element.")
    public void testToJsonComplexWithStructDef() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJsonComplexWithStructDef");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);

        String expected = "[{\"i\":1, \"iA\":[1, 2, 3], \"l\":9223372036854774807, "
                + "\"lA\":[100000000, 200000000, 300000000], \"f\":123.34, \"fA\":[245.23, 5559.49, 8796.123], "
                + "\"d\":2.139095039E9, \"b\":true, \"s\":\"Hello\", \"dA\":[245.23, 5559.49, 8796.123], "
                + "\"bA\":[true, false, true], \"sA\":[\"Hello\", \"Ballerina\"]}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST,  description = "Check retrieving blob clob binary data.")
    public void testGetComplexTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testGetComplexTypes");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(new String(((BValueArray) returns[0]).getBytes()), "wso2 ballerina blob test.");
        Assert.assertEquals((returns[1]).stringValue(), "very long text");
        Assert.assertEquals(new String(((BValueArray) returns[2]).getBytes()), "wso2 ballerina binary test.");
    }

    @Test(groups = {TABLE_TEST}, description = "Check array data types.")
    public void testArrayData() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayData");
        assertNonNullArray(returns);
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping array to non-nillable type with nillable element type.")
    public void testMapArrayToNonNillableTypeWithNillableElementType() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapArrayToNonNillableTypeWithNillableElementType");
        assertNonNullArray(returns);
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping array to nillable type with nillable element type.")
    public void testMapArrayToNillableTypeWithNillableElementType() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapArrayToNillableTypeWithNillableElementType");
        assertNonNullArray(returns);
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping array to nillable type with non-nillable element type.")
    public void testMapArrayToNillableTypeWithNonNillableElementType() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapArrayToNillableTypeWithNonNillableElementType");
        assertNonNullArray(returns);
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping array with nil elements to non-nillable type with nillable element type.")
    public void testMapNillIncludedArrayNonNillableTypeWithNillableElementType() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapNillIncludedArrayNonNillableTypeWithNillableElementType");
        assertNilIncludedArray(returns);
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping array with nil elements to nillable type with nillable element type.")
    public void testMapNillIncludedArrayNillableTypeWithNillableElementType() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapNillIncludedArrayNillableTypeWithNillableElementType");
        assertNilIncludedArray(returns);
    }

    @Test(groups = {TABLE_TEST},
          description = "Test array with only nil elements.")
    public void testMapNillElementsOnlyArray() {
        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMapNillElementsOnlyArray");
        Assert.assertEquals(returns.length, 5);
        for (BValue bValue : returns) {
            Assert.assertTrue(bValue instanceof BValueArray);
            BValueArray bRefValueArray = (BValueArray) bValue;
            for (int i = 0; i < bRefValueArray.size(); i++) {
                Assert.assertNull(bRefValueArray.getRefValue(i));
            }
        }
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping a null array to nillable type with nillable element type.")
    public void testMapNilArrayToNillableTypeWithNillableElementTypes() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapNilArrayToNillableTypeWithNillableElementTypes");
        Assert.assertEquals(returns.length, 5);
        for (BValue bValue : returns) {
            Assert.assertNull(bValue);
        }
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping a null array to non-nillable type with nillable element type.")
    public void testMapNilArrayToNillableTypeWithNonNillableElementTypes() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapNilArrayToNillableTypeWithNonNillableElementTypes");
        Assert.assertEquals(returns.length, 5);
        for (BValue bValue : returns) {
            Assert.assertNull(bValue);
        }
    }

    @Test(groups = TABLE_TEST, description = "Check date time operation")
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
        assertDateStringValues(returns, dateInserted, timeInserted, timestampInserted);
    }

    @Test(groups = TABLE_TEST, description = "Check date time operation")
    public void testDateTimeAsTimeStruct() {
        BValue[] returns = BRunUtil.invoke(result,  "testDateTimeAsTimeStruct");
        Assert.assertEquals(returns.length, 8);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), ((BInteger) returns[1]).intValue());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), ((BInteger) returns[3]).intValue());
        Assert.assertEquals(((BInteger) returns[4]).intValue(), ((BInteger) returns[5]).intValue());
        Assert.assertEquals(((BInteger) returns[6]).intValue(), ((BInteger) returns[7]).intValue());
    }

    @Test(groups = TABLE_TEST, description = "Check date time operation")
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

        assertDateIntValues(returns, dateInserted, timeInserted, timestampInserted);
    }

    @Test(groups = TABLE_TEST, description = "Check JSON conversion with null values.")
    public void testJsonWithNull() {
        BValue[] returns = BRunUtil.invokeFunction(result,  "testJsonWithNull");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        String expected = "[{\"INT_TYPE\":null, \"LONG_TYPE\":null, \"FLOAT_TYPE\":null, \"DOUBLE_TYPE\":null, " +
                "\"BOOLEAN_TYPE\":null, \"STRING_TYPE\":null}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check xml conversion with null values.")
    public void testXmlWithNull() {
        BValue[] returns = BRunUtil.invoke(result, "testXmlWithNull");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String expected1 = "<results><result><INT_TYPE>null</INT_TYPE><LONG_TYPE>null</LONG_TYPE>"
                + "<FLOAT_TYPE>null</FLOAT_TYPE><DOUBLE_TYPE>null</DOUBLE_TYPE><BOOLEAN_TYPE>null</BOOLEAN_TYPE>"
                + "<STRING_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\">"
                + "</STRING_TYPE></result></results>";
        String expected2 = "<results><result><INT_TYPE>null</INT_TYPE><LONG_TYPE>null</LONG_TYPE><FLOAT_TYPE>null"
                + "</FLOAT_TYPE><DOUBLE_TYPE>null</DOUBLE_TYPE><BOOLEAN_TYPE>null</BOOLEAN_TYPE><STRING_TYPE "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:nil=\"true\"/></result></results>";
        Assert.assertTrue(expected1.equals(returns[0].stringValue()) || expected2.equals(returns[0].stringValue()));
    }

    @Test(groups = TABLE_TEST, description = "Check xml conversion within transaction.")
    public void testToXmlWithinTransaction() {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlWithinTransaction");
        Assert.assertEquals(returns.length, 2);
        String expected = "<results><result><INT_TYPE>1</INT_TYPE><LONG_TYPE>9223372036854774807</LONG_TYPE></result>"
                + "</results>";
        Assert.assertEquals((returns[0]).stringValue(), expected);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = TABLE_TEST, description = "Check JSON conversion within transaction.")
    public void testToJsonWithinTransaction() {
        BValue[] returns = BRunUtil.invoke(result,  "testToJsonWithinTransaction");
        Assert.assertEquals(returns.length, 2);
        String expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807}]";
        Assert.assertEquals((returns[0]).stringValue(), expected);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = TABLE_TEST, description = "Check blob data support.")
    public void testBlobData() {
        BValue[] returns = BRunUtil.invoke(result,  "testBlobData");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(new String(((BValueArray) returns[0]).getBytes()), "wso2 ballerina blob test.");
    }

    @Test(groups = TABLE_TEST, description = "Check values retrieved with column alias.")
    public void testColumnAlias() {
        BValue[] returns = BRunUtil.invoke(result, "testColumnAlias");
        Assert.assertEquals(returns.length, 7);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BInteger) returns[6]).intValue(), 100);
    }

    @Test(groups = TABLE_TEST, description = "Check inserting blob data.")
    public void testBlobInsert() {
        BValue[] returns = BRunUtil.invoke(result, "testBlobInsert");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = TABLE_TEST, description = "Check whether printing of table variables is handled properly.")
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

    @Test(groups = TABLE_TEST, description = "Check auto close resources in table.")
    public void testTableAutoClose() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAutoClose");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        String expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, "
                + "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}]";
        Assert.assertEquals(returns[1].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check manual close resources in table.")
    public void testTableManualClose() {
        BValue[] returns = BRunUtil.invoke(result, "testTableManualClose");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(dependsOnGroups = TABLE_TEST,
          description = "Check whether all sql connectors are closed properly.", enabled = false) //Issue #9048
    public void testCloseConnectionPool() {
        BValue connectionCountQuery = new BString("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SESSIONS");
        BValue[] args = { connectionCountQuery };
        BValue[] returns = BRunUtil.invoke(result, "testCloseConnectionPool", args);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = TABLE_TEST, description = "Check select data with multiple rows for primitive types.")
    public void testMultipleRows() {
        BValue[] returns = BRunUtil.invoke(result, "testMultipleRows");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
    }

    @Test(groups = TABLE_TEST, description = "Check select data with multiple rows accessing without getNext.")
    public void testMultipleRowsWithoutLoop() {
        BValue[] returns = BRunUtil.invoke(result, "testMultipleRowsWithoutLoop");
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 200);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 100);
        Assert.assertEquals(returns[4].stringValue(), "200_100_NOT");
        Assert.assertEquals(returns[5].stringValue(), "200_HAS_HAS_100_NO_NO");
    }

    @Test(groups = TABLE_TEST, description = "Check select data with multiple rows accessing without getNext.")
    public void testHasNextWithoutConsume() {
        BValue[] returns = BRunUtil.invoke(result, "testHasNextWithoutConsume");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
    }

    @Test(groups = TABLE_TEST, description = "Check get float and double types.")
    public void testGetFloatTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testGetFloatTypes");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 238999.34, DELTA);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 238999.34, DELTA);
        Assert.assertEquals(((BDecimal) returns[2]).decimalValue(), new BigDecimal("238999.34"));
        Assert.assertEquals(((BDecimal) returns[3]).decimalValue(), new BigDecimal("238999.34"));
    }

    @Test(groups = {TABLE_TEST}, description = "Check array data insert and println on arrays")
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

    @Test(groups = TABLE_TEST, description = "Check get float and double min and max types.")
    public void testSignedIntMaxMinValues() {
        BValue[] returns = BRunUtil.invoke(result, "testSignedIntMaxMinValues");
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
        String expectedJson, expectedXML;
        expectedJson = "[{\"ID\":1, \"TINYINTDATA\":127, \"SMALLINTDATA\":32767, "
                + "\"INTDATA\":2147483647, \"BIGINTDATA\":9223372036854775807}, "
                + "{\"ID\":2, \"TINYINTDATA\":-128, \"SMALLINTDATA\":-32768, \"INTDATA\":-2147483648, "
                + "\"BIGINTDATA\":-9223372036854775808}, "
                + "{\"ID\":3, \"TINYINTDATA\":null, \"SMALLINTDATA\":null, \"INTDATA\":null, \"BIGINTDATA\":null}]";
        expectedXML = "<results><result><ID>1</ID><TINYINTDATA>127</TINYINTDATA>"
                + "<SMALLINTDATA>32767</SMALLINTDATA><INTDATA>2147483647</INTDATA>"
                + "<BIGINTDATA>9223372036854775807</BIGINTDATA></result>"
                + "<result><ID>2</ID><TINYINTDATA>-128</TINYINTDATA><SMALLINTDATA>-32768</SMALLINTDATA>"
                + "<INTDATA>-2147483648</INTDATA><BIGINTDATA>-9223372036854775808</BIGINTDATA></result>"
                + "<result><ID>3</ID><TINYINTDATA>null</TINYINTDATA><SMALLINTDATA>null</SMALLINTDATA>"
                + "<INTDATA>null</INTDATA><BIGINTDATA>null</BIGINTDATA></result></results>";
        Assert.assertEquals((returns[3]).stringValue(), expectedJson);
        Assert.assertEquals((returns[4]).stringValue(), expectedXML);
        Assert.assertEquals((returns[5]).stringValue(), "1|127|32767|2147483647|9223372036854775807#2|-128|-32768|"
                + "-2147483648|-9223372036854775808#3|-1|-1|-1|-1#");
    }

    @Test(groups = TABLE_TEST, description = "Check blob binary and clob types types.")
    public void testComplexTypeInsertAndRetrieval() {
        BValue[] returns = BRunUtil.invoke(result, "testComplexTypeInsertAndRetrieval");
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        String expectedJson, expectedXML1, expectedXML2;
        expectedJson = "[{\"ROW_ID\":100, \"BLOB_TYPE\":\"U2FtcGxlIFRleHQ=\", \"CLOB_TYPE\":\"Sample Text\", "
                + "\"BINARY_TYPE\":\"U2FtcGxlIFRleHQ=\"}, {\"ROW_ID\":200, \"BLOB_TYPE\":null, "
                + "\"CLOB_TYPE\":null, \"BINARY_TYPE\":null}]";
        expectedXML1 = "<results><result><ROW_ID>100</ROW_ID>"
                + "<BLOB_TYPE>U2FtcGxlIFRleHQ=</BLOB_TYPE><CLOB_TYPE>Sample Text</CLOB_TYPE>"
                + "<BINARY_TYPE>U2FtcGxlIFRleHQ=</BINARY_TYPE></result>"
                + "<result><ROW_ID>200</ROW_ID>"
                + "<BLOB_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></BLOB_TYPE>"
                + "<CLOB_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></CLOB_TYPE>"
                + "<BINARY_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\">"
                + "</BINARY_TYPE></result></results>";
        expectedXML2 = "<results><result><ROW_ID>100</ROW_ID>"
                + "<BLOB_TYPE>U2FtcGxlIFRleHQ=</BLOB_TYPE><CLOB_TYPE>Sample Text</CLOB_TYPE>"
                + "<BINARY_TYPE>U2FtcGxlIFRleHQ=</BINARY_TYPE></result>"
                + "<result><ROW_ID>200</ROW_ID>"
                + "<BLOB_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>"
                + "<CLOB_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>"
                + "<BINARY_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>"
                + "</result></results>";
        Assert.assertEquals((returns[2]).stringValue(), expectedJson);
        Assert.assertTrue(
                (expectedXML1.equals(returns[3].stringValue()) || expectedXML2.equals(returns[3].stringValue())));
        Assert.assertEquals((returns[4]).stringValue(), "100|nonNil|Sample Text|200|nil|nil|");
    }

    @Test(groups = TABLE_TEST, description = "Check result sets with same column name or complex name.")
    public void testJsonXMLConversionwithDuplicateColumnNames() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonXMLConversionwithDuplicateColumnNames");
        Assert.assertEquals(returns.length, 2);
        String expectedJSON, expectedXML;
        expectedJSON = "[{\"ROW_ID\":1, \"INT_TYPE\":1, \"DATATABLEREP.ROW_ID\":1, \"DATATABLEREP.INT_TYPE\":100}]";
        expectedXML = "<results><result><ROW_ID>1</ROW_ID><INT_TYPE>1</INT_TYPE>"
                + "<DATATABLEREP.ROW_ID>1</DATATABLEREP.ROW_ID><DATATABLEREP.INT_TYPE>100</DATATABLEREP.INT_TYPE>"
                + "</result></results>";
        Assert.assertEquals((returns[0]).stringValue(), expectedJSON);
        Assert.assertEquals((returns[1]).stringValue(), expectedXML);
    }

    @Test(groups = TABLE_TEST, description = "Check result sets with same column name or complex name.")
    public void testStructFieldNotMatchingColumnName() {
        BValue[] returns = BRunUtil.invoke(result, "testStructFieldNotMatchingColumnName");
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 100);
    }

    @Test(groups = TABLE_TEST, description = "Check retrieving data using foreach")
    public void testGetPrimitiveTypesWithForEach() {
        BValue[] returns = BRunUtil.invoke(result, "testGetPrimitiveTypesWithForEach");
        Assert.assertEquals(returns.length, 7);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BDecimal) returns[6]).decimalValue(), new BigDecimal("23.45"));
    }

    @Test(groups = TABLE_TEST, description = "Check retrieving data using foreach with multiple rows")
    public void testMultipleRowsWithForEach() {
        BValue[] returns = BRunUtil.invoke(result, "testMultipleRowsWithForEach");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
    }

    @Test(groups = TABLE_TEST,
          description = "Test adding data to database table")
    public void testTableAddInvalid() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAddInvalid");
        Assert.assertEquals((returns[0]).stringValue(), "data cannot be added to a table returned from a database");
    }

    @Test(groups = TABLE_TEST, description = "Test deleting data from a database table")
    public void testTableRemoveInvalid() {
        BValue[] returns = BRunUtil.invoke(result, "testTableRemoveInvalid");
        Assert.assertEquals((returns[0]).stringValue(), "data cannot be deleted from a table returned from a database");
    }

    @Test(groups = TABLE_TEST, description = "Test performing operation over a closed table")
    public void tableGetNextInvalid() {
        BValue[] returns = BRunUtil.invoke(result, "tableGetNextInvalid");
        Assert.assertTrue((returns[0]).stringValue().contains("Trying to perform an operation over a closed table"));
    }

    //Nillable mapping tests
    @Test(groups = TABLE_TEST,
          description = "Test mapping to nillable type fields")
    public void testMappingToNillableTypeFields() {
        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMappingToNillableTypeFields");
        Assert.assertNotNull(returns);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039, DELTA);
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BDecimal) returns[6]).decimalValue(), new BigDecimal("1234.567"));
        Assert.assertEquals(((BDecimal) returns[7]).decimalValue(), new BigDecimal("1234.567"));
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 1234.567, DELTA);
        Assert.assertEquals(((BInteger) returns[9]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[10]).intValue(), 5555);
        Assert.assertEquals(returns[11].stringValue(), "very long text");
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping to nillable blob field")
    public void testMappingToNillableTypeBlob() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMappingToNillableTypeFieldsBlob");
        Assert.assertNotNull(returns);
        Assert.assertEquals(new String(((BValueArray) returns[0]).getBytes()), "wso2 ballerina blob test.");
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping date to nillable Time field")
    public void testMapptingDatesToNillableTime() {
        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMappingDatesToNillableTimeType");
        Assert.assertEquals(returns.length, 8);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), ((BInteger) returns[1]).intValue());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), ((BInteger) returns[3]).intValue());
        Assert.assertEquals(((BInteger) returns[4]).intValue(), ((BInteger) returns[5]).intValue());
        Assert.assertEquals(((BInteger) returns[6]).intValue(), ((BInteger) returns[7]).intValue());
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping date to nillable int field")
    public void testMappingDatesToNillableIntType() {
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

        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMappingDatesToNillableIntType", args);
        Assert.assertEquals(returns.length, 4);

        assertDateIntValues(returns, dateInserted, timeInserted, timestampInserted);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping date to nillable string field")
    public void testMappingDatesToNillableStringType() {
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

        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMappingDatesToNillableStringType", args);
        Assert.assertEquals(returns.length, 4);
        assertDateStringValues(returns, dateInserted, timeInserted, timestampInserted);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping to nillable type fields")
    public void testMappingNullToNillableTypes() {
        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMappingNullToNillableTypes");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 17);
        for (BValue returnVal : returns) {
            Assert.assertNull(returnVal);
        }
    }

    //Nillable mapping negative tests
    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable int field")
    public void testAssignNilToNonNillableInt() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableInt");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable long field")
    public void testAssignNilToNonNillableLong() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableLong");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable float field")
    public void testAssignNilToNonNillableFloat() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableFloat");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable double field")
    public void testAssignNilToNonNillableDouble() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableDouble");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable boolean field")
    public void testAssignNilToNonNillableBoolean() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableBoolean");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable string field")
    public void testAssignNilToNonNillableString() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableString");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable numeric field")
    public void testAssignNilToNonNillableNumeric() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableNumeric");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable small-int field")
    public void testAssignNilToNonNillableSmallint() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableSmallint");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable tiny-int field")
    public void testAssignNilToNonNillableTinyInt() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableTinyInt");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable decimal field")
    public void testAssignNilToNonNillableDecimal() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableDecimal");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable real field")
    public void testAssignNilToNonNillableReal() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableReal");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable clob field")
    public void testAssignNilToNonNillableClob() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableClob");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable blob field")
    public void testAssignNilToNonNillableBlob() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableBlob");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable binary field")
    public void testAssignNilToNonNillableBinary() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableBinary");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable date field")
    public void testAssignNilToNonNillableDate() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableDate");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable time field")
    public void testAssignNilToNonNillableTime() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableTime");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable datetime field")
    public void testAssignNilToNonNillableDateTime() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableDateTime");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable timestamp field")
    public void testAssignNilToNonNillableTimeStamp() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableTimeStamp");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionInt() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionInt");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionLong() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionLong");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionFloat() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionFloat");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionDouble() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionDouble");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionBoolean() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionBoolean");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionString() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionString");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionNumeric() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionNumeric");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionSmallint() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionSmallint");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionTinyInt() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionTinyInt");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionDecimal() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionDecimal");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionReal() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionReal");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionClob() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionClob");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionBlob() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionBlob");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionBinary() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionBinary");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionDate() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionDate");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionTime() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionTime");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionDateTime() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionDateTime");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionTimeStamp() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionTimeStamp");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping a null array to non-nillable type with non-nillable element type.")
    public void testAssignNullArrayToNonNillableWithNonNillableElements() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingNegativeResult, "testAssignNullArrayToNonNillableWithNonNillableElements");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping a null array to non-nillable type with nillable element type.")
    public void testAssignNullArrayToNonNillableTypeWithNillableElements() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingNegativeResult, "testAssignNullArrayToNonNillableTypeWithNillableElements");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping a null array to non-nillable type with non-nillable element type.")
    public void testAssignNullElementArrayToNonNillableTypeWithNonNillableElements() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult,
                "testAssignNullElementArrayToNonNillableTypeWithNonNillableElements");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_ARRAY_FIELD));
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping a null element array to nillable type with non-nillable element type.")
    public void testAssignNullElementArrayToNillableTypeWithNonNillableElements() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult,
                "testAssignNullElementArrayToNillableTypeWithNonNillableElements");
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_ARRAY_FIELD));
    }

    @Test(groups = {TABLE_TEST}, description = "Test mapping an array to invalid union type.")
    public void testAssignInvalidUnionArray() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignInvalidUnionArray");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = {TABLE_TEST}, description = "Test mapping an array to invalid union type.")
    public void testAssignInvalidUnionArray2() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignInvalidUnionArray2");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = {TABLE_TEST}, description = "Test mapping an array to invalid union type.")
    public void testAssignInvalidUnionArrayElement() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignInvalidUnionArrayElement");
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @AfterSuite
    public void cleanup() {
        if (testDatabase != null) {
            testDatabase.stop();
        }
    }

    private void assertDateStringValues(BValue[] returns, long dateInserted, long timeInserted,
            long timestampInserted) {
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
            Assert.fail("Parsing the returned date/time/timestamp value has failed", e);
        }
    }

    private void assertDateIntValues(BValue[] returns, long dateInserted, long timeInserted, long timestampInserted) {
        long dateReturnedEpoch = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(dateReturnedEpoch, dateInserted);

        long timeReturnedEpoch = ((BInteger) returns[1]).intValue();
        Assert.assertEquals(timeReturnedEpoch, timeInserted);

        long timestampReturnedEpoch = ((BInteger) returns[2]).intValue();
        Assert.assertEquals(timestampReturnedEpoch, timestampInserted);

        long datetimeReturnedEpoch = ((BInteger) returns[3]).intValue();
        Assert.assertEquals(datetimeReturnedEpoch, timestampInserted);
    }

    private void assertNonNullArray(BValue[] returns) {
        Assert.assertEquals(returns.length, 5);

        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray intArray = (BValueArray) returns[0];
        Assert.assertEquals(intArray.getInt(0), 1);
        Assert.assertEquals(intArray.getInt(1), 2);
        Assert.assertEquals(intArray.getInt(2), 3);

        Assert.assertTrue(returns[1] instanceof BValueArray);
        BValueArray longArray = (BValueArray) returns[1];
        Assert.assertEquals(longArray.getInt(0), 100000000);
        Assert.assertEquals(longArray.getInt(1), 200000000);
        Assert.assertEquals(longArray.getInt(2), 300000000);

        Assert.assertTrue(returns[2] instanceof BValueArray);
        BValueArray doubleArray = (BValueArray) returns[2];
        Assert.assertEquals(doubleArray.getRefValue(0).value(), new BigDecimal("245.23"));
        Assert.assertEquals(doubleArray.getRefValue(1).value(), new BigDecimal("5559.49"));
        Assert.assertEquals(doubleArray.getRefValue(2).value(), new BigDecimal("8796.123"));

        Assert.assertTrue(returns[3] instanceof BValueArray);
        BValueArray stringArray = (BValueArray) returns[3];
        Assert.assertEquals(stringArray.getString(0), "Hello");
        Assert.assertEquals(stringArray.getString(1), "Ballerina");

        Assert.assertTrue(returns[4] instanceof BValueArray);
        BValueArray booleanArray = (BValueArray) returns[4];
        Assert.assertEquals(booleanArray.getBoolean(0), 1);
        Assert.assertEquals(booleanArray.getBoolean(1), 0);
        Assert.assertEquals(booleanArray.getBoolean(2), 1);
    }

    private void assertNilIncludedArray(BValue[] returns) {
        Assert.assertEquals(returns.length, 5);

        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray intArray = (BValueArray) returns[0];
        Assert.assertNull(intArray.getRefValue(0));
        Assert.assertEquals(((BInteger) intArray.getRefValue(1)).intValue(), 2);
        Assert.assertEquals(((BInteger) intArray.getRefValue(2)).intValue(), 3);

        Assert.assertTrue(returns[1] instanceof BValueArray);
        BValueArray longArray = (BValueArray) returns[1];
        Assert.assertEquals(((BInteger) longArray.getRefValue(0)).intValue(), 100000000);
        Assert.assertNull(longArray.getRefValue(1));
        Assert.assertEquals(((BInteger) longArray.getRefValue(2)).intValue(), 300000000);

        Assert.assertTrue(returns[2] instanceof BValueArray);
        BValueArray doubleArray = (BValueArray) returns[2];
        Assert.assertNull(doubleArray.getRefValue(0));
        Assert.assertEquals(doubleArray.getRefValue(1).value(), new BigDecimal("5559.49"));
        Assert.assertNull(doubleArray.getRefValue(2));

        Assert.assertTrue(returns[3] instanceof BValueArray);
        BValueArray stringArray = (BValueArray) returns[3];
        Assert.assertNull(stringArray.getRefValue(0));
        Assert.assertEquals(stringArray.getRefValue(1).stringValue(), "Ballerina");

        Assert.assertTrue(returns[4] instanceof BValueArray);
        BValueArray booleanArray = (BValueArray) returns[4];
        Assert.assertNull(booleanArray.getRefValue(0));
        Assert.assertNull(booleanArray.getRefValue(1));
        Assert.assertTrue(((BBoolean) booleanArray.getRefValue(2)).booleanValue());
    }

    @Test(description = "Check table to JSON conversion and streaming back to client in a service.",
          dependsOnGroups = TABLE_TEST)
    public void testTableToJsonStreamingInService() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/foo/bar1", "GET");
        HttpCarbonMessage responseMsg = Services.invokeNew(service, "testEP", requestMsg);

        String expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, " +
                "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}]";

        Assert.assertEquals(ResponseReader.getReturnValue(responseMsg), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to JSON conversion.")
    public void testToJsonAndAccessFromMiddle() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonAndAccessFromMiddle");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        String expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, " +
                "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}, " +
                "{\"INT_TYPE\":null, \"LONG_TYPE\":null, \"FLOAT_TYPE\":null, \"DOUBLE_TYPE\":null, " +
                "\"BOOLEAN_TYPE\":null, \"STRING_TYPE\":null}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test(groups = TABLE_TEST, description = "Check table to JSON conversion.")
    public void testToJsonAndIterate() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonAndIterate");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BValueArray);

        String  expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, " +
                "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}, " +
                "{\"INT_TYPE\":null, \"LONG_TYPE\":null, \"FLOAT_TYPE\":null, \"DOUBLE_TYPE\":null, " +
                "\"BOOLEAN_TYPE\":null, \"STRING_TYPE\":null}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test(groups = TABLE_TEST, description = "Check table to JSON conversion and setting as a child element")
    public void testToJsonAndSetAsChildElement() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonAndSetAsChildElement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BMap);
        String expected = "{\"status\":\"SUCCESS\", \"resp\":{\"value\":[{\"INT_TYPE\":1, " +
                "\"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, " +
                "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}, " +
                "{\"INT_TYPE\":null, \"LONG_TYPE\":null, \"FLOAT_TYPE\":null, \"DOUBLE_TYPE\":null, " +
                "\"BOOLEAN_TYPE\":null, \"STRING_TYPE\":null}]}}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to JSON conversion.")
    public void testToJsonAndLengthof() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonAndLengthof");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test(description = "Check table to JSON conversion and streaming back to client in a service.",
          dependsOnGroups = TABLE_TEST)
    public void testTableToJsonStreamingInService_2() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/foo/bar2", "GET");
        HttpCarbonMessage responseMsg = Services.invokeNew(service, "testEP", requestMsg);

        String expected = "{\"status\":\"SUCCESS\", \"resp\":{\"value\":[{\"INT_TYPE\":1, " +
                "\"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, " +
                "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}]}}";
        Assert.assertEquals(ResponseReader.getReturnValue(responseMsg), expected);
    }

    @Test
    public void testSelectQueryWithCursorTable() {
        BValue[] retVal = BRunUtil.invoke(result, "testSelectQueryWithCursorTable");
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(((BError) retVal[0]).getDetails().stringValue()
                .contains("Table query over a cursor table not supported"));
    }

    @Test
    public void testJoinQueryWithCursorTable() {
        BValue[] retVal = BRunUtil.invoke(result, "testJoinQueryWithCursorTable");
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(((BError) retVal[0]).getDetails().stringValue()
                .contains("Table query over a cursor table not supported"));
    }

    @Test(description = "Wrong order int test")
    public void testWrongOrderInt() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderInt");
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*Trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Wrong order string test")
    public void testWrongOrderString() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderString");
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*Trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Wrong order boolean test")
    public void testWrongOrderBoolean() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderBoolean");
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*Trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Wrong order float test")
    public void testWrongOrderFloat() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderFloat");
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*Trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Wrong order double test")
    public void testWrongOrderDouble() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderDouble");
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*Trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Wrong order long test")
    public void testWrongOrderLong() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderLong");
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*Trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Wrong order blob test")
    public void testWrongOrderBlob() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderBlobWrongOrder");
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*Trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Correct order but wrong type blob test")
    public void testCorrectOrderWrongTypeBlob() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testWrongOrderBlobCorrectOrderWrongType");
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(".*Trying to assign to a mismatching type.*", retVal[0].stringValue()));
    }

    @Test(description = "Greater number of parameters test")
    public void testGreaterNoOfParams() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testGreaterNoOfParams");
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(
                ".*Number of fields in the constraint type is greater than column count of the result set.*",
                retVal[0].stringValue()));
    }

    @Test(description = "Lower number of parameters test")
    public void testLowerNoOfParams() {
        BValue[] retVal = BRunUtil.invoke(resultNegative, "testLowerNoOfParams");
        Assert.assertEquals(retVal.length, 1);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(Pattern.matches(
                ".*Number of fields in the constraint type is lower than column count of the result set.*",
                retVal[0].stringValue()));
    }

    @Test(groups = TABLE_TEST,
          description = "Test type checking constrained cursor table with closed constraint")
    public void testTypeCheckingConstrainedCursorTableWithClosedConstraint() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeCheckingConstrainedCursorTableWithClosedConstraint");
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    @AfterClass(alwaysRun = true)
    public void closeConnectionPool() {
        BRunUtil.invokeStateful(service, "closeConnectionPool");
    }
}
