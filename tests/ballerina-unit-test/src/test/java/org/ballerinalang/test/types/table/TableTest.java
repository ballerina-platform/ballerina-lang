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

import io.netty.handler.codec.http.HttpHeaderNames;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.MimeConstants;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.test.utils.ResponseReader;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.ballerinalang.test.utils.SQLDBUtils.ContainerizedTestDatabase;
import org.ballerinalang.test.utils.SQLDBUtils.DBType;
import org.ballerinalang.test.utils.SQLDBUtils.FileBasedTestDatabase;
import org.ballerinalang.test.utils.SQLDBUtils.TestDatabase;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.Lists;
import org.wso2.carbon.messaging.Header;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.ballerinalang.test.utils.SQLDBUtils.DBType.H2;
import static org.ballerinalang.test.utils.SQLDBUtils.DBType.MYSQL;
import static org.ballerinalang.test.utils.SQLDBUtils.DBType.POSTGRES;
import static org.ballerinalang.test.utils.SQLDBUtils.DB_DIRECTORY;

/**
 * Class to test functionality of tables.
 */
public class TableTest {

    private CompileResult result;
    private CompileResult nillableMappingNegativeResult;
    private CompileResult nillableMappingResult;
    private static final String DB_NAME = "TEST_DATA_TABLE_DB";
    private static final String DB_NAME_H2 = "TEST_DATA_TABLE_H2";
    private static final String DB_DIRECTORY_H2 = "./target/H2Client/";
    private DBType dbType;
    private TestDatabase testDatabase;
    private BValue[] connectionArgs = new BValue[3];
    private static final String TABLE_TEST = "TableTest";
    private static final String MYSQL_NOT_SUPPORTED = "MySQLNotSupported";

    private static final String TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD =
            ".*Trying to assign a Nil value to a non-nillable field.*";
    private static final String INVALID_UNION_FIELD_ASSIGNMENT =
            ".*Corresponding Union type in the record is not an assignable nillable type.*";
    private static final String TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_ARRAY_FIELD =
            ".*Trying to assign an array containing NULL values to an array of a non-nillable element type.*";
    private static final double DELTA = 0.01;

    @Parameters({ "dataClientTestDBType" })
    public TableTest(@Optional("HSQLDB") SQLDBUtils.DBType dataClientTestDBType) {
        this.dbType = dataClientTestDBType;
    }

    @BeforeClass
    public void setup() {
        switch (dbType) {
        case MYSQL:
            testDatabase = new ContainerizedTestDatabase(dbType, "datafiles/sql/TableTest_Mysql_Data.sql");
            break;
        case POSTGRES:
            testDatabase = new ContainerizedTestDatabase(dbType, "datafiles/sql/TableTest_Postgres_Data.sql");
            break;
        case HSQLDB:
            testDatabase = new FileBasedTestDatabase(dbType, "datafiles/sql/TableTest_HSQL_Data.sql",
                    DB_DIRECTORY, DB_NAME);
            break;
        case H2:
            testDatabase = new FileBasedTestDatabase(dbType, "datafiles/sql/TableTest_H2_Data.sql",
                    DB_DIRECTORY_H2, DB_NAME_H2);
            break;
        default:
            throw new UnsupportedOperationException("Unsupported database type: " + dbType);
        }

        connectionArgs[0] = new BString(testDatabase.getJDBCUrl());
        connectionArgs[1] = new BString(testDatabase.getUsername());
        connectionArgs[2] = new BString(testDatabase.getPassword());

        result = BCompileUtil.compile("test-src/types/table/table_type.bal");
        nillableMappingNegativeResult = BCompileUtil
                .compile("test-src/types/table/table_nillable_mapping_negative.bal");
        nillableMappingResult = BCompileUtil.compile("test-src/types/table/table_nillable_mapping.bal");
    }

    @Test(groups = TABLE_TEST, description = "Check retrieving primitive types.")
    public void testGetPrimitiveTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testGetPrimitiveTypes", connectionArgs);
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    @Test(groups = TABLE_TEST, description = "Check table to JSON conversion.")
    public void testToJson() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJson", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BRefValueArray);
        String expected;
        if (dbType == POSTGRES) {
            expected = "[{\"int_type\":1, \"long_type\":9223372036854774807, \"float_type\":123.339996, "
                    + "\"double_type\":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\"}]";
        } else if (dbType == MYSQL) {
            expected = "[{\"int_type\":1, \"long_type\":9223372036854774807, \"float_type\":123.34, "
                    + "\"double_type\":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\"}]";
        } else {
            expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, "
                    + "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}]";
        }
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to XML conversion.")
    public void testToXml() {
        BValue[] returns = BRunUtil.invoke(result, "testToXml", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String expected;
        if (dbType == POSTGRES) {
            expected = "<results><result><int_type>1</int_type><long_type>9223372036854774807</long_type>"
                    + "<float_type>123.339996</float_type><double_type>2.139095039E9</double_type><boolean_type>true"
                    + "</boolean_type><string_type>Hello</string_type></result></results>";
        } else if (dbType == MYSQL) {
            expected = "<results><result><int_type>1</int_type><long_type>9223372036854774807</long_type>"
                    + "<float_type>123.34</float_type><double_type>2.139095039E9</double_type><boolean_type>true"
                    + "</boolean_type><string_type>Hello</string_type></result></results>";
        } else {
            expected = "<results><result><INT_TYPE>1</INT_TYPE><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                    + "<FLOAT_TYPE>123.34</FLOAT_TYPE><DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE>"
                    + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE></result></results>";
        }
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check xml streaming when result set consumed once.")
    public void testToXmlMultipleConsume() {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlMultipleConsume", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String expected;
        if (dbType == MYSQL) {
            expected = "<results><result><int_type>1</int_type><long_type>9223372036854774807</long_type>"
                    + "<float_type>123.34</float_type><double_type>2.139095039E9</double_type>"
                    + "<boolean_type>true</boolean_type><string_type>Hello</string_type></result></results>";
        } else if (dbType == POSTGRES) {
            expected = "<results><result><int_type>1</int_type><long_type>9223372036854774807</long_type>"
                    + "<float_type>123.339996</float_type><double_type>2.139095039E9</double_type>"
                    + "<boolean_type>true</boolean_type><string_type>Hello</string_type></result></results>";
        } else {
            expected = "<results><result><INT_TYPE>1</INT_TYPE>"
                    + "<LONG_TYPE>9223372036854774807</LONG_TYPE><FLOAT_TYPE>123.34</FLOAT_TYPE>"
                    + "<DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE><BOOLEAN_TYPE>true</BOOLEAN_TYPE>"
                    + "<STRING_TYPE>Hello</STRING_TYPE></result></results>";
        }
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to XML conversion with concat operation.")
    public void testToXmlWithAdd() {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlWithAdd", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String expected;
        if (dbType == MYSQL || dbType == POSTGRES) {
            expected = "<results><result><int_type>1</int_type></result></results><results><result><int_type>1"
                    + "</int_type></result></results>";
        } else {
            expected = "<results><result><INT_TYPE>1</INT_TYPE></result></results><results><result><INT_TYPE>1"
                    + "</INT_TYPE></result></results>";
        }
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check xml streaming when result set consumed once.")
    public void testToJsonMultipleConsume() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJsonMultipleConsume", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BRefValueArray);
        String expected;
        if (dbType == MYSQL) {
            expected = "[{\"int_type\":1, \"long_type\":9223372036854774807, \"float_type\":123.34, "
                    + "\"double_type\":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\"}]";
        } else if (dbType == POSTGRES) {
            expected = "[{\"int_type\":1, \"long_type\":9223372036854774807, \"float_type\":123.339996, "
                    + "\"double_type\":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\"}]";
        } else {
            expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, "
                    + "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}]";
        }
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    // Disabling for MySQL as array types are not supported.
    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED}, description = "Check xml conversion with complex element.")
    public void testToXmlComplex() {
        BValue[] returns = BRunUtil.invoke(result, "toXmlComplex", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);

        String expected;
        if (dbType == POSTGRES) {
            expected = "<results><result><int_type>1</int_type><int_array><element>1</element><element>2</element>"
                    + "<element>3</element></int_array><long_type>9223372036854774807</long_type>"
                    + "<long_array><element>100000000</element><element>200000000</element>"
                    + "<element>300000000</element></long_array><float_type>123.339996</float_type>"
                    + "<float_array><element>245.23</element><element>5559.49</element>"
                    + "<element>8796.123</element></float_array><double_type>2.139095039E9</double_type>"
                    + "<boolean_type>true</boolean_type><string_type>Hello</string_type><double_array>"
                    + "<element>245.23</element><element>5559.49</element><element>8796.123</element>"
                    + "</double_array><boolean_array><element>true</element><element>false</element>"
                    + "<element>true</element></boolean_array><string_array><element>Hello</element>"
                    + "<element>Ballerina</element></string_array></result></results>";
        } else {
            expected = "<results><result><INT_TYPE>1</INT_TYPE><INT_ARRAY><element>1</element><element>2</element>"
                    + "<element>3</element></INT_ARRAY><LONG_TYPE>9223372036854774807</LONG_TYPE>"
                    + "<LONG_ARRAY><element>100000000</element><element>200000000</element>"
                    + "<element>300000000</element></LONG_ARRAY><FLOAT_TYPE>123.34</FLOAT_TYPE>"
                    + "<FLOAT_ARRAY><element>245.23</element><element>5559.49</element>"
                    + "<element>8796.123</element></FLOAT_ARRAY><DOUBLE_TYPE>2.139095039E9</DOUBLE_TYPE>"
                    + "<BOOLEAN_TYPE>true</BOOLEAN_TYPE><STRING_TYPE>Hello</STRING_TYPE><DOUBLE_ARRAY>"
                    + "<element>245.23</element><element>5559.49</element><element>8796.123</element>"
                    + "</DOUBLE_ARRAY><BOOLEAN_ARRAY><element>true</element><element>false</element>"
                    + "<element>true</element></BOOLEAN_ARRAY><STRING_ARRAY><element>Hello</element>"
                    + "<element>Ballerina</element></STRING_ARRAY></result></results>";
        }
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    // Disabling for MySQL as array types are not supported.
    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED}, description = "Check xml conversion with complex element.")
    public void testToXmlComplexWithStructDef () {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlComplexWithStructDef", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String expected;
        if (dbType == POSTGRES) {
            expected = "<results><result><i>1</i><iA><element>1</element>"
                    + "<element>2</element><element>3</element></iA><l>9223372036854774807</l>"
                    + "<lA><element>100000000</element><element>200000000</element><element>300000000</element></lA>"
                    + "<f>123.339996</f><fA><element>245.23</element><element>5559.49</element><element>8796.123"
                    + "</element></fA><d>2.139095039E9</d><b>true</b><s>Hello</s>"
                    + "<dA><element>245.23</element><element>5559.49</element><element>8796.123</element></dA>"
                    + "<bA><element>true</element><element>false</element><element>true</element></bA>"
                    + "<sA><element>Hello</element><element>Ballerina</element></sA></result></results>";
        } else {
            expected = "<results><result><i>1</i><iA><element>1</element>"
                    + "<element>2</element><element>3</element></iA><l>9223372036854774807</l>"
                    + "<lA><element>100000000</element><element>200000000</element><element>300000000</element></lA>"
                    + "<f>123.34</f><fA><element>245.23</element><element>5559.49</element><element>8796.123</element>"
                    + "</fA><d>2.139095039E9</d><b>true</b><s>Hello</s>"
                    + "<dA><element>245.23</element><element>5559.49</element><element>8796.123</element></dA>"
                    + "<bA><element>true</element><element>false</element><element>true</element></bA>"
                    + "<sA><element>Hello</element><element>Ballerina</element></sA></result></results>";
        }
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    // Disabling for MySQL as array types are not supported.
    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED}, description = "Check json conversion with complex element.")
    public void testToJsonComplex() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJsonComplex", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BRefValueArray);

        String expected;
        if (dbType == POSTGRES) {
            expected = "[{\"int_type\":1, \"int_array\":[1, 2, 3], "
                    + "\"long_type\":9223372036854774807, \"long_array\":[100000000, 200000000, 300000000], "
                    + "\"float_type\":123.339996, \"float_array\":[245.23, 5559.49, 8796.123], \"double_type\""
                    + ":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\", \"double_array\""
                    + ":[245.23, 5559.49, 8796.123], \"boolean_array\":[true, false, true], "
                    + "\"string_array\":[\"Hello\", \"Ballerina\"]}]";
        } else {
            expected = "[{\"INT_TYPE\":1, \"INT_ARRAY\":[1, 2, 3], "
                    + "\"LONG_TYPE\":9223372036854774807, \"LONG_ARRAY\":[100000000, 200000000, 300000000], "
                    + "\"FLOAT_TYPE\":123.34, \"FLOAT_ARRAY\":[245.23, 5559.49, 8796.123], "
                    + "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\", "
                    + "\"DOUBLE_ARRAY\":[245.23, 5559.49, 8796.123], \"BOOLEAN_ARRAY\":[true, false, true], "
                    + "\"STRING_ARRAY\":[\"Hello\", \"Ballerina\"]}]";
        }
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED}, description = "Check json conversion with complex element.")
    public void testToJsonComplexWithStructDef() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJsonComplexWithStructDef", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BRefValueArray);

        String expected;
        if (dbType == POSTGRES) {
            expected = "[{\"i\":1, \"iA\":[1, 2, 3], \"l\":9223372036854774807, "
                    + "\"lA\":[100000000, 200000000, 300000000], \"f\":123.339996, \"fA\":[245.23, 5559.49, 8796.123],"
                    + " \"d\":2.139095039E9, \"b\":true, \"s\":\"Hello\", \"dA\":[245.23, 5559.49, 8796.123], "
                    + "\"bA\":[true, false, true], \"sA\":[\"Hello\", \"Ballerina\"]}]";
        } else {
            expected = "[{\"i\":1, \"iA\":[1, 2, 3], \"l\":9223372036854774807, "
                    + "\"lA\":[100000000, 200000000, 300000000], \"f\":123.34, \"fA\":[245.23, 5559.49, 8796.123], "
                    + "\"d\":2.139095039E9, \"b\":true, \"s\":\"Hello\", \"dA\":[245.23, 5559.49, 8796.123], "
                    + "\"bA\":[true, false, true], \"sA\":[\"Hello\", \"Ballerina\"]}]";
        }
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST,  description = "Check retrieving blob clob binary data.")
    public void testGetComplexTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testGetComplexTypes", connectionArgs);
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(new String(((BByteArray) returns[0]).getBytes()), "wso2 ballerina blob test.");
        Assert.assertEquals((returns[1]).stringValue(), "very long text");
        Assert.assertEquals(new String(((BByteArray) returns[2]).getBytes()), "wso2 ballerina binary test.");
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED}, description = "Check array data types.")
    public void testArrayData() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayData", connectionArgs);
        assertNonNullArray(returns);
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED},
          description = "Test mapping array to non-nillable type with nillable element type.")
    public void testMapArrayToNonNillableTypeWithNillableElementType() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapArrayToNonNillableTypeWithNillableElementType", connectionArgs);
        assertNonNullArray(returns);
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED},
          description = "Test mapping array to nillable type with nillable element type.")
    public void testMapArrayToNillableTypeWithNillableElementType() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapArrayToNillableTypeWithNillableElementType", connectionArgs);
        assertNonNullArray(returns);
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED},
          description = "Test mapping array to nillable type with non-nillable element type.")
    public void testMapArrayToNillableTypeWithNonNillableElementType() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapArrayToNillableTypeWithNonNillableElementType", connectionArgs);
        assertNonNullArray(returns);
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED},
          description = "Test mapping array with nil elements to non-nillable type with nillable element type.")
    public void testMapNillIncludedArrayNonNillableTypeWithNillableElementType() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapNillIncludedArrayNonNillableTypeWithNillableElementType",
                        connectionArgs);
        assertNilIncludedArray(returns);
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED},
          description = "Test mapping array with nil elements to nillable type with nillable element type.")
    public void testMapNillIncludedArrayNillableTypeWithNillableElementType() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapNillIncludedArrayNillableTypeWithNillableElementType",
                        connectionArgs);
        assertNilIncludedArray(returns);
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED},
          description = "Test array with only nil elements.")
    public void testMapNillElementsOnlyArray() {
        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMapNillElementsOnlyArray", connectionArgs);
        Assert.assertEquals(returns.length, 5);
        for (BValue bValue : returns) {
            Assert.assertTrue(bValue instanceof BRefValueArray);
            BRefValueArray bRefValueArray = (BRefValueArray) bValue;
            for (int i = 0; i < bRefValueArray.size(); i++) {
                Assert.assertNull(bRefValueArray.get(i));
            }
        }
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED},
          description = "Test mapping a null array to nillable type with nillable element type.")
    public void testMapNilArrayToNillableTypeWithNillableElementTypes() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapNilArrayToNillableTypeWithNillableElementTypes", connectionArgs);
        Assert.assertEquals(returns.length, 5);
        for (BValue bValue : returns) {
            Assert.assertNull(bValue);
        }
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED},
          description = "Test mapping a null array to non-nillable type with nillable element type.")
    public void testMapNilArrayToNillableTypeWithNonNillableElementTypes() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapNilArrayToNillableTypeWithNonNillableElementTypes",
                        connectionArgs);
        Assert.assertEquals(returns.length, 5);
        for (BValue bValue : returns) {
            Assert.assertNull(bValue);
        }
    }

    @Test(groups = TABLE_TEST, description = "Check date time operation")
    public void testDateTime() {
        BValue[] args = new BValue[6];
        System.arraycopy(connectionArgs, 0, args, 0, 3);
        Calendar cal = Calendar.getInstance();

        cal.clear();
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, 5);
        cal.set(Calendar.DAY_OF_MONTH, 23);
        long dateInserted = cal.getTimeInMillis();
        args[3] = new BInteger(dateInserted);

        cal.clear();
        cal.set(Calendar.HOUR, 14);
        cal.set(Calendar.MINUTE, 15);
        cal.set(Calendar.SECOND, 23);
        long timeInserted = cal.getTimeInMillis();
        args[4] = new BInteger(timeInserted);

        cal.clear();
        cal.set(Calendar.HOUR, 16);
        cal.set(Calendar.MINUTE, 33);
        cal.set(Calendar.SECOND, 55);
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 25);
        long timestampInserted = cal.getTimeInMillis();
        args[5] = new BInteger(timestampInserted);

        BValue[] returns = BRunUtil.invoke(result,  "testDateTime", args);
        Assert.assertEquals(returns.length, 4);
        assertDateStringValues(returns, dateInserted, timeInserted, timestampInserted);
    }

    @Test(groups = TABLE_TEST, description = "Check date time operation")
    public void testDateTimeAsTimeStruct() {
        BValue[] returns = BRunUtil.invoke(result,  "testDateTimeAsTimeStruct", connectionArgs);
        Assert.assertEquals(returns.length, 8);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), ((BInteger) returns[1]).intValue());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), ((BInteger) returns[3]).intValue());
        Assert.assertEquals(((BInteger) returns[4]).intValue(), ((BInteger) returns[5]).intValue());
        Assert.assertEquals(((BInteger) returns[6]).intValue(), ((BInteger) returns[7]).intValue());
    }

    @Test(groups = TABLE_TEST, description = "Check date time operation")
    public void testDateTimeInt() {
        BValue[] args = new BValue[6];
        System.arraycopy(connectionArgs, 0, args, 0, 3);
        Calendar cal = Calendar.getInstance();

        cal.clear();
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, 5);
        cal.set(Calendar.DAY_OF_MONTH, 23);
        long dateInserted = cal.getTimeInMillis();
        args[3] = new BInteger(dateInserted);

        cal.clear();
        cal.set(Calendar.HOUR, 14);
        cal.set(Calendar.MINUTE, 15);
        cal.set(Calendar.SECOND, 23);
        long timeInserted = cal.getTimeInMillis();
        args[4] = new BInteger(timeInserted);

        cal.clear();
        cal.set(Calendar.HOUR, 16);
        cal.set(Calendar.MINUTE, 33);
        cal.set(Calendar.SECOND, 55);
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 25);
        long timestampInserted = cal.getTimeInMillis();
        args[5] = new BInteger(timestampInserted);

        BValue[] returns = BRunUtil.invoke(result,  "testDateTimeInt", args);
        Assert.assertEquals(returns.length, 4);

        assertDateIntValues(returns, dateInserted, timeInserted, timestampInserted);
    }

    @Test(groups = TABLE_TEST, description = "Check JSON conversion with null values.")
    public void testJsonWithNull() {
        BValue[] returns = BRunUtil.invokeFunction(result,  "testJsonWithNull", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BRefValueArray);
        String expected;
        if (dbType == POSTGRES || dbType == MYSQL) {
            expected = "[{\"int_type\":0, \"long_type\":0, \"float_type\":0.0, \"double_type\":0.0, "
                    + "\"boolean_type\":false, \"string_type\":null}]";
        } else {
            expected = "[{\"INT_TYPE\":0, \"LONG_TYPE\":0, \"FLOAT_TYPE\":0.0, \"DOUBLE_TYPE\":0.0, " +
                    "\"BOOLEAN_TYPE\":false, \"STRING_TYPE\":null}]";
        }
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check xml conversion with null values.")
    public void testXmlWithNull() {
        BValue[] returns = BRunUtil.invoke(result, "testXmlWithNull", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        String expected;
        if (dbType == POSTGRES || dbType == MYSQL) {
            expected = "<results><result><int_type>0</int_type><long_type>0</long_type><float_type>0.0</float_type>"
                    + "<double_type>0.0</double_type><boolean_type>false</boolean_type>"
                    + "<string_type xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\">"
                    + "</string_type></result></results>";
        } else {
            expected = "<results><result><INT_TYPE>0</INT_TYPE><LONG_TYPE>0</LONG_TYPE><FLOAT_TYPE>0.0</FLOAT_TYPE>"
                    + "<DOUBLE_TYPE>0.0</DOUBLE_TYPE><BOOLEAN_TYPE>false</BOOLEAN_TYPE>"
                    + "<STRING_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\">"
                    + "</STRING_TYPE></result></results>";
        }
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check xml conversion within transaction.")
    public void testToXmlWithinTransaction() {
        BValue[] returns = BRunUtil.invoke(result, "testToXmlWithinTransaction", connectionArgs);
        Assert.assertEquals(returns.length, 2);
        String expected;
        if (dbType == MYSQL || dbType == POSTGRES) {
            expected = "<results><result><int_type>1</int_type><long_type>9223372036854774807</long_type></result>"
                    + "</results>";
        } else {
            expected = "<results><result><INT_TYPE>1</INT_TYPE><LONG_TYPE>9223372036854774807</LONG_TYPE></result>"
                    + "</results>";
        }
        Assert.assertEquals((returns[0]).stringValue(), expected);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = TABLE_TEST, description = "Check JSON conversion within transaction.")
    public void testToJsonWithinTransaction() {
        BValue[] returns = BRunUtil.invoke(result,  "testToJsonWithinTransaction", connectionArgs);
        Assert.assertEquals(returns.length, 2);
        String expected;
        if (dbType == MYSQL || dbType == POSTGRES) {
            expected = "[{\"int_type\":1, \"long_type\":9223372036854774807}]";
        } else {
            expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807}]";
        }
        Assert.assertEquals((returns[0]).stringValue(), expected);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
    }

    @Test(groups = TABLE_TEST, description = "Check blob data support.")
    public void testBlobData() {
        BValue[] returns = BRunUtil.invoke(result,  "testBlobData", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(new String(((BByteArray) returns[0]).getBytes()), "wso2 ballerina blob test.");
    }

    @Test(groups = TABLE_TEST, description = "Check values retrieved with column alias.")
    public void testColumnAlias() {
        BValue[] returns = BRunUtil.invoke(result, "testColumnAlias", connectionArgs);
        Assert.assertEquals(returns.length, 7);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BInteger) returns[6]).intValue(), 100);
    }

    @Test(groups = TABLE_TEST, description = "Check inserting blob data.")
    public void testBlobInsert() {
        BValue[] returns = BRunUtil.invoke(result, "testBlobInsert", connectionArgs);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = TABLE_TEST, description = "Check whether printing of table variables is handled properly.")
    public void testTablePrintAndPrintln() throws IOException {
        PrintStream original = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final String expected = "\n";
            BRunUtil.invoke(result, "testTablePrintAndPrintln", connectionArgs);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test(groups = TABLE_TEST, description = "Check auto close resources in table.")
    public void testTableAutoClose() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAutoClose", connectionArgs);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        String expected;
        if (dbType == MYSQL) {
            expected = "[{\"int_type\":1, \"long_type\":9223372036854774807, \"float_type\":123.34, "
                    + "\"double_type\":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\"}]";
        } else if (dbType == POSTGRES) {
            expected = "[{\"int_type\":1, \"long_type\":9223372036854774807, \"float_type\":123.339996, "
                    + "\"double_type\":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\"}]";
        } else {
            expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, "
                    + "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}]";
        }
        Assert.assertEquals(returns[1].stringValue(), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check manual close resources in table.")
    public void testTableManualClose() {
        BValue[] returns = BRunUtil.invoke(result, "testTableManualClose", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(dependsOnGroups = TABLE_TEST, description = "Check whether all sql connectors are closed properly.")
    public void testCloseConnectionPool() {
        BValue connectionCountQuery;
        if (dbType == MYSQL) {
            connectionCountQuery = new BString("SELECT COUNT(*) FROM information_schema.PROCESSLIST");
        } else if (dbType == H2) {
            connectionCountQuery = new BString("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SESSIONS");
        } else if (dbType == POSTGRES) {
            connectionCountQuery = new BString("SELECT COUNT(*) FROM pg_stat_activity");
        } else {
            connectionCountQuery = new BString("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA"
                    + ".SYSTEM_SESSIONS");
        }
        BValue[] args = new BValue[4];
        System.arraycopy(connectionArgs, 0, args, 0, 3);
        args[3] = connectionCountQuery;
        BValue[] returns = BRunUtil.invoke(result, "testCloseConnectionPool", args);
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @Test(groups = TABLE_TEST, description = "Check select data with multiple rows for primitive types.")
    public void testMultipleRows() {
        BValue[] returns = BRunUtil.invoke(result, "testMultipleRows", connectionArgs);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
    }

    @Test(groups = TABLE_TEST, description = "Check select data with multiple rows accessing without getNext.")
    public void testMultipleRowsWithoutLoop() {
        BValue[] returns = BRunUtil.invoke(result, "testMultipleRowsWithoutLoop", connectionArgs);
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
        BValue[] returns = BRunUtil.invoke(result, "testHasNextWithoutConsume", connectionArgs);
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), true);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
    }

    @Test(groups = TABLE_TEST, description = "Check get float and double types.")
    public void testGetFloatTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testGetFloatTypes", connectionArgs);
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 238999.34, DELTA);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 238999.34, DELTA);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 238999.34, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 238999.34, DELTA);
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED}, description = "Check array data insert and println on arrays")
    public void testArrayDataInsertAndPrint() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayDataInsertAndPrint", connectionArgs);
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
        BValue[] returns = BRunUtil.invoke(result, "testSignedIntMaxMinValues", connectionArgs);
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
        String expectedJson, expectedXML;
        if (dbType == MYSQL) {
            expectedJson = "[{\"id\":1, \"tinyIntData\":127, \"smallIntData\":32767, \"intData\":2147483647, "
                    + "\"bigIntData\":9223372036854775807}, {\"id\":2, \"tinyIntData\":-128, \"smallIntData\":-32768, "
                    + "\"intData\":-2147483648, \"bigIntData\":-9223372036854775808}, {\"id\":3, \"tinyIntData\":0, "
                    + "\"smallIntData\":0, \"intData\":0, \"bigIntData\":0}]";
            expectedXML = "<results><result><id>1</id><tinyIntData>127</tinyIntData><smallIntData>32767</smallIntData>"
                    + "<intData>2147483647</intData><bigIntData>9223372036854775807</bigIntData></result><result>"
                    + "<id>2</id><tinyIntData>-128</tinyIntData><smallIntData>-32768</smallIntData>"
                    + "<intData>-2147483648</intData><bigIntData>-9223372036854775808</bigIntData></result><result>"
                    + "<id>3</id><tinyIntData>0</tinyIntData><smallIntData>0</smallIntData><intData>0</intData>"
                    + "<bigIntData>0</bigIntData></result></results>";
        } else if (dbType == POSTGRES) {
            expectedJson = "[{\"id\":1, \"tinyintdata\":127, \"smallintdata\":32767, \"intdata\":2147483647, "
                    + "\"bigintdata\":9223372036854775807}, {\"id\":2, \"tinyintdata\":-128, \"smallintdata\":-32768, "
                    + "\"intdata\":-2147483648, \"bigintdata\":-9223372036854775808}, {\"id\":3, \"tinyintdata\":0, "
                    + "\"smallintdata\":0, \"intdata\":0, \"bigintdata\":0}]";
            expectedXML = "<results><result><id>1</id><tinyintdata>127</tinyintdata><smallintdata>32767</smallintdata>"
                    + "<intdata>2147483647</intdata><bigintdata>9223372036854775807</bigintdata></result><result>"
                    + "<id>2</id><tinyintdata>-128</tinyintdata><smallintdata>-32768</smallintdata>"
                    + "<intdata>-2147483648</intdata><bigintdata>-9223372036854775808</bigintdata></result><result>"
                    + "<id>3</id><tinyintdata>0</tinyintdata><smallintdata>0</smallintdata><intdata>0</intdata>"
                    + "<bigintdata>0</bigintdata></result></results>";
        } else {
            expectedJson = "[{\"ID\":1, \"TINYINTDATA\":127, \"SMALLINTDATA\":32767, "
                    + "\"INTDATA\":2147483647, \"BIGINTDATA\":9223372036854775807}, "
                    + "{\"ID\":2, \"TINYINTDATA\":-128, \"SMALLINTDATA\":-32768, \"INTDATA\":-2147483648, "
                    + "\"BIGINTDATA\":-9223372036854775808}, "
                    + "{\"ID\":3, \"TINYINTDATA\":0, \"SMALLINTDATA\":0, \"INTDATA\":0, \"BIGINTDATA\":0}]";
            expectedXML = "<results><result><ID>1</ID><TINYINTDATA>127</TINYINTDATA>"
                    + "<SMALLINTDATA>32767</SMALLINTDATA><INTDATA>2147483647</INTDATA>"
                    + "<BIGINTDATA>9223372036854775807</BIGINTDATA></result>"
                    + "<result><ID>2</ID><TINYINTDATA>-128</TINYINTDATA><SMALLINTDATA>-32768</SMALLINTDATA>"
                    + "<INTDATA>-2147483648</INTDATA><BIGINTDATA>-9223372036854775808</BIGINTDATA></result>"
                    + "<result><ID>3</ID><TINYINTDATA>0</TINYINTDATA><SMALLINTDATA>0</SMALLINTDATA><INTDATA>0</INTDATA>"
                    + "<BIGINTDATA>0</BIGINTDATA></result></results>";
        }
        Assert.assertEquals((returns[3]).stringValue(), expectedJson);
        Assert.assertEquals((returns[4]).stringValue(), expectedXML);
        Assert.assertEquals((returns[5]).stringValue(), "1|127|32767|2147483647|9223372036854775807#2|-128|-32768|"
                + "-2147483648|-9223372036854775808#3|-1|-1|-1|-1#");
    }

    @Test(groups = TABLE_TEST, description = "Check blob binary and clob types types.")
    public void testComplexTypeInsertAndRetrieval() {
        BValue[] returns = BRunUtil.invoke(result, "testComplexTypeInsertAndRetrieval", connectionArgs);
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        String expectedJson, expectedXML;
        if (dbType == MYSQL) {
            expectedJson = "[{\"row_id\":100, \"blob_type\":\"U2FtcGxlIFRleHQ=\", \"clob_type\":\"Sample Text\", "
                    + "\"binary_type\":\"U2FtcGxlIFRleHQAAAAAAAAAAAAAAAAAAAAA\"}, {\"row_id\":200, \"blob_type\":null, "
                    + "\"clob_type\":null, \"binary_type\":null}]";
            expectedXML = "<results><result><row_id>100</row_id><blob_type>U2FtcGxlIFRleHQ=</blob_type>"
                    + "<clob_type>Sample Text</clob_type>"
                    + "<binary_type>U2FtcGxlIFRleHQAAAAAAAAAAAAAAAAAAAAA</binary_type></result><result>"
                    + "<row_id>200</row_id><blob_type xmlns:xsi=\"http://www"
                    + ".w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></blob_type><clob_type xmlns:xsi=\"http://www"
                    + ".w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></clob_type><binary_type "
                    + "xmlns:xsi=\"http://www"
                    + ".w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></binary_type></result></results>";

        } else if (dbType == H2) {
            expectedJson = "[{\"ROW_ID\":100, \"BLOB_TYPE\":\"U2FtcGxlIFRleHQ=\", \"CLOB_TYPE\":\"Sample Text\", "
                    + "\"BINARY_TYPE\":\"U2FtcGxlIFRleHQ=\"}, {\"ROW_ID\":200, \"BLOB_TYPE\":null, "
                    + "\"CLOB_TYPE\":null, \"BINARY_TYPE\":null}]";
            expectedXML = "<results><result><ROW_ID>100</ROW_ID>"
                    + "<BLOB_TYPE>U2FtcGxlIFRleHQ=</BLOB_TYPE><CLOB_TYPE>Sample Text</CLOB_TYPE>"
                    + "<BINARY_TYPE>U2FtcGxlIFRleHQ=</BINARY_TYPE></result>"
                    + "<result><ROW_ID>200</ROW_ID>"
                    + "<BLOB_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></BLOB_TYPE>"
                    + "<CLOB_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></CLOB_TYPE>"
                    + "<BINARY_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\">"
                    + "</BINARY_TYPE></result></results>";
        } else {
            expectedJson = "[{\"ROW_ID\":100, \"BLOB_TYPE\":\"U2FtcGxlIFRleHQ=\", \"CLOB_TYPE\":\"Sample Text\", "
                    + "\"BINARY_TYPE\":\"U2FtcGxlIFRleHQAAAAAAAAAAAAAAAAAAAAA\"}, {\"ROW_ID\":200, \"BLOB_TYPE\":null, "
                    + "\"CLOB_TYPE\":null, \"BINARY_TYPE\":null}]";
            expectedXML = "<results><result><ROW_ID>100</ROW_ID>"
                    + "<BLOB_TYPE>U2FtcGxlIFRleHQ=</BLOB_TYPE><CLOB_TYPE>Sample Text</CLOB_TYPE>"
                    + "<BINARY_TYPE>U2FtcGxlIFRleHQAAAAAAAAAAAAAAAAAAAAA</BINARY_TYPE></result>"
                    + "<result><ROW_ID>200</ROW_ID>"
                    + "<BLOB_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></BLOB_TYPE>"
                    + "<CLOB_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></CLOB_TYPE>"
                    + "<BINARY_TYPE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\">"
                    + "</BINARY_TYPE></result></results>";
        }
        Assert.assertEquals((returns[2]).stringValue(), expectedJson);
        Assert.assertEquals((returns[3]).stringValue(), expectedXML);
        Assert.assertEquals((returns[4]).stringValue(), "100|nonNil|Sample Text|200|nil|nil|");
    }

    @Test(groups = TABLE_TEST, description = "Check result sets with same column name or complex name.")
    public void testJsonXMLConversionwithDuplicateColumnNames() {
        BValue[] returns = BRunUtil.invoke(result, "testJsonXMLConversionwithDuplicateColumnNames", connectionArgs);
        Assert.assertEquals(returns.length, 2);
        String expectedJSON, expectedXML;
        if (dbType == POSTGRES || dbType == MYSQL) {
            expectedJSON = "[{\"row_id\":1, \"int_type\":1, \"DATATABLEREP.row_id\":1, \"DATATABLEREP.int_type\":100}]";
            expectedXML = "<results><result><row_id>1</row_id><int_type>1</int_type>"
                    + "<DATATABLEREP.row_id>1</DATATABLEREP.row_id><DATATABLEREP.int_type>100</DATATABLEREP.int_type>"
                    + "</result></results>";
        } else {
            expectedJSON = "[{\"ROW_ID\":1, \"INT_TYPE\":1, \"DATATABLEREP.ROW_ID\":1, \"DATATABLEREP.INT_TYPE\":100}]";
            expectedXML = "<results><result><ROW_ID>1</ROW_ID><INT_TYPE>1</INT_TYPE>"
                    + "<DATATABLEREP.ROW_ID>1</DATATABLEREP.ROW_ID><DATATABLEREP.INT_TYPE>100</DATATABLEREP.INT_TYPE>"
                    + "</result></results>";
        }
        Assert.assertEquals((returns[0]).stringValue(), expectedJSON);
        Assert.assertEquals((returns[1]).stringValue(), expectedXML);
    }

    @Test(groups = TABLE_TEST, description = "Check result sets with same column name or complex name.")
    public void testStructFieldNotMatchingColumnName() {
        BValue[] returns = BRunUtil.invoke(result, "testStructFieldNotMatchingColumnName", connectionArgs);
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 100);
    }

    @Test(groups = TABLE_TEST, description = "Check retrieving data using foreach")
    public void testGetPrimitiveTypesWithForEach() {
        BValue[] returns = BRunUtil.invoke(result, "testGetPrimitiveTypesWithForEach", connectionArgs);
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34D, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039D);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
    }

    @Test(groups = TABLE_TEST, description = "Check retrieving data using foreach with multiple rows")
    public void testMultipleRowsWithForEach() {
        BValue[] returns = BRunUtil.invoke(result, "testMultipleRowsWithForEach", connectionArgs);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 200);
    }

    @Test(groups = TABLE_TEST,
          description = "Test adding data to database table")
    public void testTableAddInvalid() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAddInvalid", connectionArgs);
        Assert.assertEquals((returns[0]).stringValue(), "data cannot be added to a table returned from a database");
    }

    @Test(groups = TABLE_TEST, description = "Test deleting data from a database table")
    public void testTableRemoveInvalid() {
        BValue[] returns = BRunUtil.invoke(result, "testTableRemoveInvalid", connectionArgs);
        Assert.assertEquals((returns[0]).stringValue(), "data cannot be deleted from a table returned from a database");
    }

    @Test(groups = TABLE_TEST,
          description = "Test performing operation over a closed table",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*trying to perform an operation over a closed table.*")
    public void tableGetNextInvalid() {
        BRunUtil.invoke(result, "tableGetNextInvalid", connectionArgs);
    }
    
    //Nillable mapping tests
    @Test(groups = TABLE_TEST,
          description = "Test mapping to nillable type fields")
    public void testMappingToNillableTypeFields() {
        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMappingToNillableTypeFields", connectionArgs);
        Assert.assertNotNull(returns);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9223372036854774807L);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 123.34, DELTA);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 2139095039, DELTA);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), true);
        Assert.assertEquals(returns[5].stringValue(), "Hello");
        Assert.assertEquals(((BFloat) returns[6]).floatValue(), 1234.567);
        Assert.assertEquals(((BFloat) returns[7]).floatValue(), 1234.567);
        Assert.assertEquals(((BFloat) returns[8]).floatValue(), 1234.567, DELTA);
        Assert.assertEquals(((BInteger) returns[9]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[10]).intValue(), 5555);
        Assert.assertEquals(returns[11].stringValue(), "very long text");
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping to nillable blob field")
    public void testMappingToNillableTypeBlob() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMappingToNillableTypeFieldsBlob", connectionArgs);
        Assert.assertNotNull(returns);
        Assert.assertEquals(new String(((BByteArray) returns[0]).getBytes()), "wso2 ballerina blob test.");
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping date to nillable Time field")
    public void testMapptingDatesToNillableTime() {
        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMappingDatesToNillableTimeType", connectionArgs);
        Assert.assertEquals(returns.length, 8);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), ((BInteger) returns[1]).intValue());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), ((BInteger) returns[3]).intValue());
        Assert.assertEquals(((BInteger) returns[4]).intValue(), ((BInteger) returns[5]).intValue());
        Assert.assertEquals(((BInteger) returns[6]).intValue(), ((BInteger) returns[7]).intValue());
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping date to nillable int field")
    public void testMappingDatesToNillableIntType() {
        BValue[] args = new BValue[6];
        System.arraycopy(connectionArgs, 0, args, 0, 3);
        Calendar cal = Calendar.getInstance();

        cal.clear();
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, 5);
        cal.set(Calendar.DAY_OF_MONTH, 23);
        long dateInserted = cal.getTimeInMillis();
        args[3] = new BInteger(dateInserted);

        cal.clear();
        cal.set(Calendar.HOUR, 14);
        cal.set(Calendar.MINUTE, 15);
        cal.set(Calendar.SECOND, 23);
        long timeInserted = cal.getTimeInMillis();
        args[4] = new BInteger(timeInserted);

        cal.clear();
        cal.set(Calendar.HOUR, 16);
        cal.set(Calendar.MINUTE, 33);
        cal.set(Calendar.SECOND, 55);
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 25);
        long timestampInserted = cal.getTimeInMillis();
        args[5] = new BInteger(timestampInserted);

        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMappingDatesToNillableIntType", args);
        Assert.assertEquals(returns.length, 4);
        args[2] = new BInteger(timestampInserted);

        assertDateIntValues(returns, dateInserted, timeInserted, timestampInserted);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping date to nillable string field")
    public void testMappingDatesToNillableStringType() {
        BValue[] args = new BValue[6];
        System.arraycopy(connectionArgs, 0, args, 0, 3);
        Calendar cal = Calendar.getInstance();

        cal.clear();
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, 5);
        cal.set(Calendar.DAY_OF_MONTH, 23);
        long dateInserted = cal.getTimeInMillis();
        args[3] = new BInteger(dateInserted);

        cal.clear();
        cal.set(Calendar.HOUR, 14);
        cal.set(Calendar.MINUTE, 15);
        cal.set(Calendar.SECOND, 23);
        long timeInserted = cal.getTimeInMillis();
        args[4] = new BInteger(timeInserted);

        cal.clear();
        cal.set(Calendar.HOUR, 16);
        cal.set(Calendar.MINUTE, 33);
        cal.set(Calendar.SECOND, 55);
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 25);
        long timestampInserted = cal.getTimeInMillis();
        args[5] = new BInteger(timestampInserted);

        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMappingDatesToNillableStringType", args);
        Assert.assertEquals(returns.length, 4);
        assertDateStringValues(returns, dateInserted, timeInserted, timestampInserted);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping to nillable type fields")
    public void testMappingNullToNillableTypes() {
        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMappingNullToNillableTypes", connectionArgs);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 17);
        for (BValue returnVal : returns) {
            Assert.assertNull(returnVal);
        }
    }

    //Nillable mapping negative tests
    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable int field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableInt() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableInt", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable long field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableLong() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableLong", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable float field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableFloat() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableFloat", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable double field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableDouble() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableDouble", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable boolean field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableBoolean() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableBoolean", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable string field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableString() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableString", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable numeric field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableNumeric() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableNumeric", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable small-int field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableSmallint() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableSmallint", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable tiny-int field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableTinyInt() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableTinyInt", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable decimal field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableDecimal() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableDecimal", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable real field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableReal() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableReal", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable clob field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableClob() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableClob", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable blob field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableBlob() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableBlob", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable binary field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableBinary() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableBinary", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable date field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableDate() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableDate", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable time field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableTime() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableTime", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable datetime field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableDateTime() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableDateTime", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping nil to non-nillable timestamp field",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNilToNonNillableTimeStamp() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableTimeStamp", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionInt() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionInt", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionLong() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionLong", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionFloat() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionFloat", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionDouble() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionDouble", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionBoolean() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionBoolean", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionString() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionString", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionNumeric() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionNumeric", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionSmallint() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionSmallint", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionTinyInt() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionTinyInt", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionDecimal() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionDecimal", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionReal() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionReal", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionClob() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionClob", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionBlob() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionBlob", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionBinary() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionBinary", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionDate() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionDate", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionTime() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionTime", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionDateTime() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionDateTime", connectionArgs);
    }

    @Test(groups = TABLE_TEST,
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignToInvalidUnionTimeStamp() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionTimeStamp", connectionArgs);
    }


    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED},
          description = "Test mapping a null array to non-nillable type with non-nillable element type.",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNullArrayToNonNillableWithNonNillableElements() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNullArrayToNonNillableWithNonNillableElements",
                connectionArgs);
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED},
          description = "Test mapping a null array to non-nillable type with nillable element type.",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD)
    public void testAssignNullArrayToNonNillableTypeWithNillableElements() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNullArrayToNonNillableTypeWithNillableElements",
                connectionArgs);
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED},
          description = "Test mapping a null array to non-nillable type with non-nillable element type.",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_ARRAY_FIELD)
    public void testAssignNullElementArrayToNonNillableTypeWithNonNillableElements() {
        BRunUtil.invoke(nillableMappingNegativeResult,
                "testAssignNullElementArrayToNonNillableTypeWithNonNillableElements", connectionArgs);
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED},
          description = "Test mapping a null element array to nillable type with non-nillable element type.",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_ARRAY_FIELD)
    public void testAssignNullElementArrayToNillableTypeWithNonNillableElements() {
        BRunUtil.invoke(nillableMappingNegativeResult,
                "testAssignNullElementArrayToNillableTypeWithNonNillableElements", connectionArgs);
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED},
          description = "Test mapping an array to invalid union type.",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignInvalidUnionArray() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignInvalidUnionArray", connectionArgs);
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED},
          description = "Test mapping an array to invalid union type.",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignInvalidUnionArray2() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignInvalidUnionArray2", connectionArgs);
    }

    @Test(groups = {TABLE_TEST, MYSQL_NOT_SUPPORTED},
          description = "Test mapping an array to invalid union type.",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = INVALID_UNION_FIELD_ASSIGNMENT)
    public void testAssignInvalidUnionArrayElement() {
        BRunUtil.invoke(nillableMappingNegativeResult, "testAssignInvalidUnionArrayElement", connectionArgs);
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
        Assert.assertEquals(doubleArray.get(0), 245.23, DELTA);
        Assert.assertEquals(doubleArray.get(1), 5559.49, DELTA);
        Assert.assertEquals(doubleArray.get(2), 8796.123, DELTA);

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

    private void assertNilIncludedArray(BValue[] returns) {
        Assert.assertEquals(returns.length, 5);

        Assert.assertTrue(returns[0] instanceof BRefValueArray);
        BRefValueArray intArray = (BRefValueArray) returns[0];
        Assert.assertEquals(intArray.get(0), null);
        Assert.assertEquals(((BInteger) intArray.get(1)).intValue(), 2);
        Assert.assertEquals(((BInteger) intArray.get(2)).intValue(), 3);

        Assert.assertTrue(returns[1] instanceof BRefValueArray);
        BRefValueArray longArray = (BRefValueArray) returns[1];
        Assert.assertEquals(((BInteger) longArray.get(0)).intValue(), 100000000);
        Assert.assertEquals(longArray.get(1), null);
        Assert.assertEquals(((BInteger) longArray.get(2)).intValue(), 300000000);

        Assert.assertTrue(returns[2] instanceof BRefValueArray);
        BRefValueArray doubleArray = (BRefValueArray) returns[2];
        Assert.assertEquals(doubleArray.get(0), null);
        Assert.assertEquals(((BFloat) doubleArray.get(1)).floatValue(), 5559.49, DELTA);
        Assert.assertEquals(doubleArray.get(2), null);

        Assert.assertTrue(returns[3] instanceof BRefValueArray);
        BRefValueArray stringArray = (BRefValueArray) returns[3];
        Assert.assertEquals(stringArray.get(0), null);
        Assert.assertEquals(stringArray.get(1).stringValue(), "Ballerina");

        Assert.assertTrue(returns[4] instanceof BRefValueArray);
        BRefValueArray booleanArray = (BRefValueArray) returns[4];
        Assert.assertEquals(booleanArray.get(0), null);
        Assert.assertEquals(booleanArray.get(1), null);
        Assert.assertEquals(((BBoolean) booleanArray.get(2)).booleanValue(), true);
    }

    @Test(description = "Check table to JSON conversion and streaming back to client in a service.",
            dependsOnMethods = { "testCloseConnectionPool" })
    public void testTableToJsonStreamingInService() {
        CompileResult service =
                BServiceUtil.setupProgramFile(this, "test-src/types/table/table_to_json_service_test.bal");
        String payload = "{ \"jdbcUrl\" : \"" + connectionArgs[0] + "\", \"userName\" : \"" + connectionArgs[1] +
                "\", \"password\" : \"" + connectionArgs[2] + "\"}";
        Header header = new Header(HttpHeaderNames.CONTENT_TYPE.toString(), MimeConstants.APPLICATION_JSON);
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/foo/bar1", "POST", Lists.of(header), payload);
        HttpCarbonMessage responseMsg = Services.invokeNew(service, "testEP", requestMsg);

        String expected;
        if (dbType == POSTGRES) {
            expected = "[{\"int_type\":1, \"long_type\":9223372036854774807, \"float_type\":123.339996, " +
                    "\"double_type\":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\"}]";
        } else if (dbType == MYSQL) {
            expected = "[{\"int_type\":1, \"long_type\":9223372036854774807, \"float_type\":123.34, " +
                    "\"double_type\":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\"}]";
        } else {
            expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, " +
                    "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}]";
        }

        Assert.assertEquals(ResponseReader.getReturnValue(responseMsg), expected);
    }

    @Test(groups = TABLE_TEST, description = "Check table to JSON conversion.")
    public void testToJsonAndAccessFromMiddle() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonAndAccessFromMiddle", connectionArgs);
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BRefValueArray);
        String expected;
        if (dbType == POSTGRES) {
            expected = "[{\"int_type\":1, \"long_type\":9223372036854774807, \"float_type\":123.339996, "
                    + "\"double_type\":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\"}, " +
                    "{\"int_type\":0, \"long_type\":0, \"float_type\":0, \"double_type\":0, " +
                    "\"boolean_type\":false, \"string_type\":null}]";
        } else if (dbType == MYSQL) {
            expected = "[{\"int_type\":1, \"long_type\":9223372036854774807, \"float_type\":123.34, "
                    + "\"double_type\":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\"}, "
                    + "{\"int_type\":0, \"long_type\":0, \"float_type\":0.0, \"double_type\":0.0, "
                    + "\"boolean_type\":false, \"string_type\":null}]";
        } else {
            expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, " +
                    "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}, " +
                    "{\"INT_TYPE\":0, \"LONG_TYPE\":0, \"FLOAT_TYPE\":0.0, \"DOUBLE_TYPE\":0.0, " +
                    "\"BOOLEAN_TYPE\":false, \"STRING_TYPE\":null}]";
        }
        Assert.assertEquals(returns[0].stringValue(), expected);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test(groups = TABLE_TEST, description = "Check table to JSON conversion.")
    public void testToJsonAndIterate() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonAndIterate", connectionArgs);
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BRefValueArray);
        String expected;
        if (dbType == POSTGRES) {
            expected = "[{\"int_type\":1, \"long_type\":9223372036854774807, \"float_type\":123.339996, "
                    + "\"double_type\":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\"}, " +
                    "{\"int_type\":0, \"long_type\":0, \"float_type\":0, \"double_type\":0, " +
                    "\"boolean_type\":false, \"string_type\":null}]";
        } else if (dbType == MYSQL) {
            expected = "[{\"int_type\":1, \"long_type\":9223372036854774807, \"float_type\":123.34, "
                    + "\"double_type\":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\"}, "
                    + "{\"int_type\":0, \"long_type\":0, \"float_type\":0.0, \"double_type\":0.0, "
                    + "\"boolean_type\":false, \"string_type\":null}]";
        } else {
            expected = "[{\"INT_TYPE\":1, \"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, " +
                    "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}, " +
                    "{\"INT_TYPE\":0, \"LONG_TYPE\":0, \"FLOAT_TYPE\":0.0, \"DOUBLE_TYPE\":0.0, " +
                    "\"BOOLEAN_TYPE\":false, \"STRING_TYPE\":null}]";
        }
        Assert.assertEquals(returns[0].stringValue(), expected);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test(groups = TABLE_TEST, description = "Check table to JSON conversion and setting as a child element")
    public void testToJsonAndSetAsChildElement() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonAndSetAsChildElement", connectionArgs);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BMap);
        String expected;
        if (dbType == POSTGRES) {
            expected = "{\"status\":\"SUCCESS\", \"resp\":{\"value\":[{\"int_type\":1, " +
                    "\"long_type\":9223372036854774807, \"float_type\":123.339996, " +
                    "\"double_type\":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\"}, " +
                    "{\"int_type\":0, \"long_type\":0, \"float_type\":0, \"double_type\":0, " +
                    "\"boolean_type\":false, \"string_type\":null}]}}";
        } else if (dbType == MYSQL) {
            expected = "{\"status\":\"SUCCESS\", \"resp\":{\"value\":[{\"INT_TYPE\":1, " +
                    "\"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, " +
                    "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}, " +
                    "{\"INT_TYPE\":0, \"LONG_TYPE\":0, \"FLOAT_TYPE\":0.0, \"DOUBLE_TYPE\":0.0, " +
                    "\"BOOLEAN_TYPE\":false, \"STRING_TYPE\":null}]}}";
        } else {
            expected = "{\"status\":\"SUCCESS\", \"resp\":{\"value\":[{\"INT_TYPE\":1, " +
                    "\"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, " +
                    "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}, " +
                    "{\"INT_TYPE\":0, \"LONG_TYPE\":0, \"FLOAT_TYPE\":0.0, \"DOUBLE_TYPE\":0.0, " +
                    "\"BOOLEAN_TYPE\":false, \"STRING_TYPE\":null}]}}";
        }
        Assert.assertEquals(returns[0].stringValue(), expected);
    }
    
    @Test(groups = TABLE_TEST, description = "Check table to JSON conversion.")
    public void testToJsonAndLengthof() {
        BValue[] returns = BRunUtil.invoke(result, "testToJsonAndLengthof", connectionArgs);
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test(description = "Check table to JSON conversion and streaming back to client in a service.",
            dependsOnMethods = { "testCloseConnectionPool" })
    public void testTableToJsonStreamingInService_2() {
        CompileResult service =
                BServiceUtil.setupProgramFile(this, "test-src/types/table/table_to_json_service_test.bal");
        String payload = "{ \"jdbcUrl\" : \"" + connectionArgs[0] + "\", \"userName\" : \"" + connectionArgs[1] +
                "\", \"password\" : \"" + connectionArgs[2] + "\"}";
        Header header = new Header(HttpHeaderNames.CONTENT_TYPE.toString(), MimeConstants.APPLICATION_JSON);
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/foo/bar2", "POST", Lists.of(header), payload);
        HttpCarbonMessage responseMsg = Services.invokeNew(service, "testEP", requestMsg);

        String expected;
        if (dbType == POSTGRES) {
            expected = "{\"status\":\"SUCCESS\", \"resp\":{\"value\":[{\"int_type\":1, " +
                    "\"long_type\":9223372036854774807, \"float_type\":123.339996, " +
                    "\"double_type\":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\"}]}}";
        } else if (dbType == MYSQL) {
            expected = "{\"status\":\"SUCCESS\", \"resp\":{\"value\":[{\"int_type\":1, " +
                    "\"long_type\":9223372036854774807, \"float_type\":123.34, " +
                    "\"double_type\":2.139095039E9, \"boolean_type\":true, \"string_type\":\"Hello\"}]}}";
        } else {
            expected = "{\"status\":\"SUCCESS\", \"resp\":{\"value\":[{\"INT_TYPE\":1, " +
                    "\"LONG_TYPE\":9223372036854774807, \"FLOAT_TYPE\":123.34, " +
                    "\"DOUBLE_TYPE\":2.139095039E9, \"BOOLEAN_TYPE\":true, \"STRING_TYPE\":\"Hello\"}]}}";
        }

        Assert.assertEquals(ResponseReader.getReturnValue(responseMsg), expected);
    }
}
