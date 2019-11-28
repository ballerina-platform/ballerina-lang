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
package org.ballerinax.jdbc.table;

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
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinax.jdbc.utils.SQLDBUtils;
import org.ballerinax.jdbc.utils.SQLDBUtils.DBType;
import org.ballerinax.jdbc.utils.SQLDBUtils.FileBasedTestDatabase;
import org.ballerinax.jdbc.utils.SQLDBUtils.TestDatabase;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Class to test functionality of tables.
 */
public class TableTypeTest {

    private CompileResult result;
    private static final String DB_NAME_H2 = "TEST_DATA_TABLE_H2";
    private TestDatabase testDatabase;
    private static final String TABLE_TEST = "TableTest";
    private static final double DELTA = 0.01;
    private static final String JDBC_URL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + DB_NAME_H2;
    private BValue[] args = { new BString(JDBC_URL) };

    @BeforeClass
    public void setup() {
        testDatabase = new FileBasedTestDatabase(DBType.H2,
                Paths.get("datafiles", "sql", "table", "table_type_test_data.sql").toString(), SQLDBUtils.DB_DIRECTORY,
                DB_NAME_H2);
        result = BCompileUtil.compile(Paths.get("test-src", "table", "table_type_test.bal").toString());
    }

    @Test(groups = TABLE_TEST, description = "Check retrieving primitive types.")
    public void testGetPrimitiveTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testGetPrimitiveTypes", args);
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
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJson", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        String expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, "
                + "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to JSON conversion.")
    public void testToJsonComplexTypes() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJsonComplexTypes", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        String expected = "[{\"BLOB_TYPE\":\"d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==\", \"CLOB_TYPE\":\"very long "
                + "text\", \"BINARY_TYPE\":\"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to JSON conversion.")
    public void testToJsonComplexTypesNil() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJsonComplexTypesNil", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        String expected = "[{\"BLOB_TYPE\":null, \"CLOB_TYPE\":null, \"BINARY_TYPE\":null}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to XML conversion.")
    public void testToXml() {
        BValue[] returns = BRunUtil.invoke(result, "testToXml", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String expected = "<results><result><INT_TYPE>1</INT_TYPE><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                + "<FLOAT_TYPE>123.34</FLOAT_TYPE><DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE>"
                + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE></result></results>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to XML conversion.")
    public void testToXmlComplexTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlComplexTypes", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String expected = "<results><result><BLOB_TYPE>d3NvMiBiYWxsZXJpbmEgYmxvYiB0ZXN0Lg==</BLOB_TYPE><CLOB_TYPE"
                + ">very long text</CLOB_TYPE><BINARY_TYPE>d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu</BINARY_TYPE></result"
                + "></results>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to XML conversion.")
    public void testToXmlComplexTypesNil() {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlComplexTypesNil", args);
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
        BValue[] returns = BRunUtil.invoke(result, "testToXmlMultipleConsume", args);
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
        BValue[] returns = BRunUtil.invoke(result, "testToXmlWithAdd", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String expected = "<results><result><INT_TYPE>1</INT_TYPE></result></results><results><result><INT_TYPE>1"
                + "</INT_TYPE></result></results>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check xml streaming when result set consumed once.")
    public void testToJsonMultipleConsume() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJsonMultipleConsume", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        String expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, "
                + "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = {TABLE_TEST}, description = "Check xml conversion with complex element.")
    public void testToXmlComplex() {
        BValue[] returns = BRunUtil.invoke(result, "toXmlComplex", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);

        String expected = "<results><result><INT_TYPE>1</INT_TYPE><INT_ARRAY><element>1</element><element>2</element>"
                + "<element>3</element></INT_ARRAY><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                + "<LONG_ARRAY><element>100000000</element><element>200000000</element>"
                + "<element>300000000</element></LONG_ARRAY><FLOAT_TYPE>123.34</FLOAT_TYPE>"
                + "<FLOAT_ARRAY><element>245.23</element><element>5559.49</element>"
                + "<element>8796.123</element></FLOAT_ARRAY><DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE>"
                + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE>"
                + "<DECIMAL_TYPE>342452151425.4556</DECIMAL_TYPE>"
                + "<DOUBLE_ARRAY><element>245.23</element><element>5559.49</element><element>8796.123</element>"
                + "</DOUBLE_ARRAY><BOOLEAN_ARRAY><element>true</element><element>false</element>"
                + "<element>true</element></BOOLEAN_ARRAY><STRING_ARRAY><element>Hello</element>"
                + "<element>Ballerina</element></STRING_ARRAY></result></results>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = {TABLE_TEST}, description = "Check xml conversion with complex element.")
    public void testToXmlComplexWithStructDef () {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlComplexWithStructDef", args);
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
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJsonComplex", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);

        String expected = "[{\"INT_TYPE\":1, \"INT_ARRAY\":[1, 2, 3], "
                + "\"LONG_TYPE\":9223372036854774807, \"LONG_ARRAY\":[100000000, 200000000, 300000000], "
                + "\"FLOAT_TYPE\":123.34, \"FLOAT_ARRAY\":[245.23, 5559.49, 8796.123], "
                + "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\", "
                + "\"DECIMAL_TYPE\":342452151425.4556, \"DOUBLE_ARRAY\":[245.23, 5559.49, 8796.123], "
                + "\"BOOLEAN_ARRAY\":[true, false, true], \"STRING_ARRAY\":[\"Hello\", \"Ballerina\"]}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = {TABLE_TEST}, description = "Check json conversion with complex element.")
    public void testToJsonComplexWithStructDef() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJsonComplexWithStructDef", args);
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
        BValue[] returns = BRunUtil.invoke(result, "testGetComplexTypes", args);
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(new String(((BValueArray) returns[0]).getBytes()), "wso2 ballerina blob test.");
        Assert.assertEquals((returns[1]).stringValue(), "very long text");
        Assert.assertEquals(new String(((BValueArray) returns[2]).getBytes()), "wso2 ballerina binary test.");
    }

    @Test(groups = {TABLE_TEST}, description = "Check array data types.")
    public void testArrayData() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayData", args);
        assertNonNullArray(returns);
    }

    @Test(groups = TABLE_TEST, description = "Check date time operation")
    public void testDateTime() {
        BValue[] args = new BValue[4];
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

        args[3] = new BString(JDBC_URL);

        BValue[] returns = BRunUtil.invoke(result,  "testDateTime", args);
        Assert.assertEquals(returns.length, 4);
        assertDateStringValues(returns, dateInserted, timeInserted, timestampInserted);
    }

    @Test(groups = TABLE_TEST, description = "Check date time operation")
    public void testDateTimeAsTimeStruct() {
        BValue[] returns = BRunUtil.invoke(result,  "testDateTimeAsTimeStruct", args);
        Assert.assertEquals(returns.length, 8);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), ((BInteger) returns[1]).intValue());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), ((BInteger) returns[3]).intValue());
        Assert.assertEquals(((BInteger) returns[4]).intValue(), ((BInteger) returns[5]).intValue());
        Assert.assertEquals(((BInteger) returns[6]).intValue(), ((BInteger) returns[7]).intValue());
    }

    @Test(groups = TABLE_TEST, description = "Check date time operation")
    public void testDateTimeInt() {
        BValue[] args = new BValue[4];
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

        args[3] = new BString(JDBC_URL);

        BValue[] returns = BRunUtil.invoke(result,  "testDateTimeInt", args);
        Assert.assertEquals(returns.length, 4);

        assertDateIntValues(returns, dateInserted, timeInserted, timestampInserted);
    }

    @Test(groups = TABLE_TEST, description = "Check JSON conversion with null values.")
    public void testJsonWithNull() {
        BValue[] returns = BRunUtil.invokeFunction(result,  "testJsonWithNull", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        String expected = "[{\"INT_TYPE\":null, \"LONG_TYPE\":null, \"FLOAT_TYPE\":null, \"DOUBLE_TYPE\":null, " +
                "\"BOOLEAN_TYPE\":null, \"STRING_TYPE\":null}]";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check xml conversion with null values.")
    public void testXmlWithNull() {
        BValue[] returns = BRunUtil.invoke(result, "testXmlWithNull", args);
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
        BValue[] returns = BRunUtil.invoke(result, "testToXmlWithinTransaction", args);
        Assert.assertEquals(returns.length, 2);
        String expected = "<results><result><INT_TYPE>1</INT_TYPE><LONG_TYPE>9223372036854774807</LONG_TYPE></result>"
                + "</results>";
        Assert.assertEquals((returns[0]).stringValue(), expected);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = TABLE_TEST, description = "Check JSON conversion within transaction.")
    public void testToJsonWithinTransaction() {
        BValue[] returns = BRunUtil.invoke(result,  "testToJsonWithinTransaction", args);
        Assert.assertEquals(returns.length, 2);
        String expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807}]";
        Assert.assertEquals((returns[0]).stringValue(), expected);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = TABLE_TEST, description = "Check blob data support.")
    public void testBlobData() {
        BValue[] returns = BRunUtil.invoke(result,  "testBlobData", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(new String(((BValueArray) returns[0]).getBytes()), "wso2 ballerina blob test.");
    }

    @Test(groups = TABLE_TEST, description = "Check values retrieved with column alias.")
    public void testColumnAlias() {
        BValue[] returns = BRunUtil.invoke(result, "testColumnAlias", args);
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
        BValue[] returns = BRunUtil.invoke(result, "testBlobInsert", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = TABLE_TEST, description = "Check whether printing of table variables is handled properly.")
    public void testTablePrintAndPrintln() throws IOException {
        PrintStream original = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final String expected = "\n";
            BRunUtil.invoke(result, "testTablePrintAndPrintln", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test(groups = TABLE_TEST, description = "Check auto close resources in table.")
    public void testTableAutoClose() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAutoClose", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        String expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, "
                + "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}]";
        Assert.assertEquals(returns[1].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check manual close resources in table.")
    public void testTableManualClose() {
        BValue[] returns = BRunUtil.invoke(result, "testTableManualClose", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(dependsOnGroups = TABLE_TEST,
          description = "Check whether all sql connectors are closed properly.") //Issue #9048
    public void testCloseConnectionPool() {
        BValue[] returns = BRunUtil.invoke(result, "testCloseConnectionPool", args);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = TABLE_TEST, description = "Check select data with multiple rows for primitive types.")
    public void testMultipleRows() {
        BValue[] returns = BRunUtil.invoke(result, "testMultipleRows", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
    }

    @Test(groups = TABLE_TEST, description = "Check select data with multiple rows accessing without getNext.")
    public void testMultipleRowsWithoutLoop() {
        BValue[] returns = BRunUtil.invoke(result, "testMultipleRowsWithoutLoop", args);
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
        BValue[] returns = BRunUtil.invoke(result, "testHasNextWithoutConsume", args);
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
    }

    @Test(groups = TABLE_TEST, description = "Check get float and double types.")
    public void testGetFloatTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testGetFloatTypes", args);
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 238999.34, DELTA);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 238999.34, DELTA);
        Assert.assertEquals(((BDecimal) returns[2]).decimalValue(), new BigDecimal("238999.34"));
        Assert.assertEquals(((BDecimal) returns[3]).decimalValue(), new BigDecimal("238999.34"));
    }

    @Test(groups = {TABLE_TEST}, description = "Check array data insert and println on arrays")
    public void testArrayDataInsertAndPrint() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayDataInsertAndPrint", args);
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
        BValue[] returns = BRunUtil.invoke(result, "testSignedIntMaxMinValues", args);
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
        BValue[] returns = BRunUtil.invoke(result, "testComplexTypeInsertAndRetrieval", args);
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
        BValue[] returns = BRunUtil.invoke(result, "testJsonXMLConversionwithDuplicateColumnNames", args);
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
        BValue[] returns = BRunUtil.invoke(result, "testStructFieldNotMatchingColumnName", args);
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 100);
    }

    @Test(groups = TABLE_TEST, description = "Check retrieving data using foreach")
    public void testGetPrimitiveTypesWithForEach() {
        BValue[] returns = BRunUtil.invoke(result, "testGetPrimitiveTypesWithForEach", args);
        Assert.assertEquals(returns.length, 7);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BDecimal) returns[6]).decimalValue(), new BigDecimal("23.45"));
    }

    @Test(groups = TABLE_TEST, description = "Check retrieving data using while loop and constructFrom function")
    public void testGetPrimitiveTypesWithWhileLoopAndConstructFrom() {
        BValue[] returns = BRunUtil.invoke(result, "testGetPrimitiveTypesWithWhileLoopAndConstructFrom", args);
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
        BValue[] returns = BRunUtil.invoke(result, "testMultipleRowsWithForEach", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
    }

    @Test(groups = TABLE_TEST,
          description = "Test adding data to database table")
    public void testTableAddInvalid() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAddInvalid", args);
        Assert.assertEquals((returns[0]).stringValue(), "data cannot be added to a table returned from a database");
    }

    @Test(groups = TABLE_TEST, description = "Test deleting data from a database table")
    public void testTableRemoveInvalid() {
        BValue[] returns = BRunUtil.invoke(result, "testTableRemoveInvalid", args);
        Assert.assertEquals((returns[0]).stringValue(), "data cannot be deleted from a table returned from a database");
    }

    @Test(groups = TABLE_TEST, description = "Test performing operation over a closed table")
    public void tableGetNextInvalid() {
        BValue[] returns = BRunUtil.invoke(result, "tableGetNextInvalid", args);
        Assert.assertTrue((returns[0]).stringValue().contains("Trying to perform an operation over a closed table"));
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

    @Test(groups = TABLE_TEST, description = "Check table to JSON conversion.")
    public void testToJsonAndAccessFromMiddle() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonAndAccessFromMiddle", args);
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
        BValue[] returns = BRunUtil.invoke(result, "testToJsonAndIterate", args);
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
        BValue[] returns = BRunUtil.invoke(result, "testToJsonAndSetAsChildElement", args);
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
        BValue[] returns = BRunUtil.invoke(result, "testToJsonAndLengthof", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test
    public void testSelectQueryWithCursorTable() {
        BValue[] retVal = BRunUtil.invoke(result, "testSelectQueryWithCursorTable", args);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(((BError) retVal[0]).getDetails().stringValue()
                .contains("Table query over a cursor table not supported"));
    }

    @Test
    public void testJoinQueryWithCursorTable() {
        BValue[] retVal = BRunUtil.invoke(result, "testJoinQueryWithCursorTable", args);
        Assert.assertTrue(retVal[0] instanceof BError);
        Assert.assertTrue(((BError) retVal[0]).getDetails().stringValue()
                .contains("Table query over a cursor table not supported"));
    }

    @Test(groups = TABLE_TEST,
          description = "Test type checking constrained cursor table with closed constraint")
    public void testTypeCheckingConstrainedCursorTableWithClosedConstraint() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeCheckingConstrainedCursorTableWithClosedConstraint", args);
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    @Test(groups = TABLE_TEST,
          description = "Test assigning string value to json field in constraint")
    public void testAssignStringValueToJsonField() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testAssignStringValueToJsonField", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello");
    }

    @Test(groups = "TableIterationTest", description = "Check accessing data using foreach iteration")
    public void testForEachInTableWithStmt() {
        BValue[] returns = BRunUtil.invoke(result, "testForEachInTableWithStmt", args);
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 400.25);
        Assert.assertEquals(returns[3].stringValue(), "John");
    }

    @Test(groups = "TableIterationTest", description = "Check accessing data using foreach iteration")
    public void testForEachInTableWithIndex() {
        BValue[] returns = BRunUtil.invoke(result, "testForEachInTableWithIndex", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), ",1,2,3");
        Assert.assertEquals(returns[1].stringValue(), ",0,1,2");
    }

    @Test(groups = "TableIterationTest", description = "Check accessing data using foreach iteration")
    public void testForEachInTable() {
        BValue[] returns = BRunUtil.invoke(result, "testForEachInTable", args);
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 400.25);
        Assert.assertEquals(returns[3].stringValue(), "John");
    }
}
