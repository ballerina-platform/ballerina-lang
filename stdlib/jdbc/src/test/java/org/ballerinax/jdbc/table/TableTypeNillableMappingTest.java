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
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
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

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Class to test functionality of tables.
 */
public class TableTypeNillableMappingTest {

    private CompileResult nillableMappingNegativeResult;
    private CompileResult nillableMappingResult;
    private static final String DB_NAME_H2 = "TEST_DATA_TABLE_H2";
    private TestDatabase testDatabase;
    private static final String TABLE_TEST = "TableTest";

    private static final String TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD =
            "trying to assign a Nil value to a non-nillable field";
    private static final String INVALID_UNION_FIELD_ASSIGNMENT =
            "corresponding Union type in the record is not an assignable nillable type";
    private static final String TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_ARRAY_FIELD =
            "trying to assign an array containing NULL values to an array of a non-nillable element type";
    private static final double DELTA = 0.01;

    private static final String JDBC_URL = "jdbc:h2:file:" + SQLDBUtils.DB_DIRECTORY + DB_NAME_H2;
    private BValue[] args = { new BString(JDBC_URL) };

    @BeforeClass
    public void setup() {
        testDatabase = new FileBasedTestDatabase(DBType.H2,
                Paths.get("datafiles", "sql", "table", "table_type_test_data.sql").toString(), SQLDBUtils.DB_DIRECTORY,
                DB_NAME_H2);
        nillableMappingResult = BCompileUtil
                .compile(Paths.get("test-src", "table", "table_type_test_nillable_mapping.bal").toString());
        nillableMappingNegativeResult = BCompileUtil
                .compile(Paths.get("test-src", "table", "table_type_test_nillable_mapping_negative.bal").toString());
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping array to non-nillable type with nillable element type.")
    public void testMapArrayToNonNillableTypeWithNillableElementType() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapArrayToNonNillableTypeWithNillableElementType", args);
        assertNonNullArray(returns);
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping array to nillable type with nillable element type.")
    public void testMapArrayToNillableTypeWithNillableElementType() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapArrayToNillableTypeWithNillableElementType", args);
        assertNonNullArray(returns);
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping array to nillable type with non-nillable element type.")
    public void testMapArrayToNillableTypeWithNonNillableElementType() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapArrayToNillableTypeWithNonNillableElementType", args);
        assertNonNullArray(returns);
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping array with nil elements to non-nillable type with nillable element type.")
    public void testMapNillIncludedArrayNonNillableTypeWithNillableElementType() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapNillIncludedArrayNonNillableTypeWithNillableElementType", args);
        assertNilIncludedArray(returns);
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping array with nil elements to nillable type with nillable element type.")
    public void testMapNillIncludedArrayNillableTypeWithNillableElementType() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapNillIncludedArrayNillableTypeWithNillableElementType", args);
        assertNilIncludedArray(returns);
    }

    @Test(groups = {TABLE_TEST},
          description = "Test array with only nil elements.")
    public void testMapNillElementsOnlyArray() {
        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMapNillElementsOnlyArray", args);
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
                .invoke(nillableMappingResult, "testMapNilArrayToNillableTypeWithNillableElementTypes", args);
        Assert.assertEquals(returns.length, 5);
        for (BValue bValue : returns) {
            Assert.assertNull(bValue);
        }
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping a null array to non-nillable type with nillable element type.")
    public void testMapNilArrayToNillableTypeWithNonNillableElementTypes() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingResult, "testMapNilArrayToNillableTypeWithNonNillableElementTypes", args);
        Assert.assertEquals(returns.length, 5);
        for (BValue bValue : returns) {
            Assert.assertNull(bValue);
        }
    }

    //Nillable mapping tests
    @Test(groups = TABLE_TEST,
          description = "Test mapping to nillable type fields")
    public void testMappingToNillableTypeFields() {
        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMappingToNillableTypeFields", args);
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
                .invoke(nillableMappingResult, "testMappingToNillableTypeFieldsBlob", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(new String(((BValueArray) returns[0]).getBytes()), "wso2 ballerina blob test.");
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping date to nillable Time field")
    public void testMapptingDatesToNillableTime() {
        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMappingDatesToNillableTimeType", args);
        Assert.assertEquals(returns.length, 8);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), ((BInteger) returns[1]).intValue());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), ((BInteger) returns[3]).intValue());
        Assert.assertEquals(((BInteger) returns[4]).intValue(), ((BInteger) returns[5]).intValue());
        Assert.assertEquals(((BInteger) returns[6]).intValue(), ((BInteger) returns[7]).intValue());
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping date to nillable int field")
    public void testMappingDatesToNillableIntType() {
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

        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMappingDatesToNillableIntType", args);
        Assert.assertEquals(returns.length, 4);

        assertDateIntValues(returns, dateInserted, timeInserted, timestampInserted);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping date to nillable string field")
    public void testMappingDatesToNillableStringType() {
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

        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMappingDatesToNillableStringType", args);
        Assert.assertEquals(returns.length, 4);
        assertDateStringValues(returns, dateInserted, timeInserted, timestampInserted);
    }

    @Test(groups = TABLE_TEST,
          description = "Test mapping to nillable type fields")
    public void testMappingNullToNillableTypes() {
        BValue[] returns = BRunUtil.invoke(nillableMappingResult, "testMappingNullToNillableTypes", args);
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 17);
        for (BValue returnVal : returns) {
            Assert.assertNull(returnVal);
        }
    }

    //Nillable mapping negative tests
    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable int field")
    public void testAssignNilToNonNillableInt() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableInt", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable long field")
    public void testAssignNilToNonNillableLong() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableLong", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable float field")
    public void testAssignNilToNonNillableFloat() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableFloat", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable double field")
    public void testAssignNilToNonNillableDouble() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableDouble", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable boolean field")
    public void testAssignNilToNonNillableBoolean() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableBoolean", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable string field")
    public void testAssignNilToNonNillableString() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableString", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable numeric field")
    public void testAssignNilToNonNillableNumeric() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableNumeric", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable small-int field")
    public void testAssignNilToNonNillableSmallint() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableSmallint", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable tiny-int field")
    public void testAssignNilToNonNillableTinyInt() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableTinyInt", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable decimal field")
    public void testAssignNilToNonNillableDecimal() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableDecimal", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable real field")
    public void testAssignNilToNonNillableReal() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableReal", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable clob field")
    public void testAssignNilToNonNillableClob() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableClob", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable blob field")
    public void testAssignNilToNonNillableBlob() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableBlob", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable binary field")
    public void testAssignNilToNonNillableBinary() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableBinary", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable date field")
    public void testAssignNilToNonNillableDate() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableDate", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable time field")
    public void testAssignNilToNonNillableTime() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableTime", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable datetime field")
    public void testAssignNilToNonNillableDateTime() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableDateTime", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST, description = "Test mapping nil to non-nillable timestamp field")
    public void testAssignNilToNonNillableTimeStamp() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignNilToNonNillableTimeStamp", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionInt() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionInt", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionLong() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionLong", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionFloat() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionFloat", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionDouble() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionDouble", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionBoolean() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionBoolean", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionString() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionString", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionNumeric() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionNumeric", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionSmallint() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionSmallint", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionTinyInt() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionTinyInt", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionDecimal() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionDecimal", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionReal() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionReal", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionClob() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionClob", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionBlob() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionBlob", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionBinary() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionBinary", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionDate() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionDate", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionTime() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionTime", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionDateTime() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionDateTime", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = TABLE_TEST)
    public void testAssignToInvalidUnionTimeStamp() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignToInvalidUnionTimeStamp", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping a null array to non-nillable type with non-nillable element type.")
    public void testAssignNullArrayToNonNillableWithNonNillableElements() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingNegativeResult, "testAssignNullArrayToNonNillableWithNonNillableElements", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = { TABLE_TEST },
          description = "Test mapping a null array to non-nillable type with nillable element type.")
    public void testAssignNullArrayToNonNillableTypeWithNillableElements() {
        BValue[] returns = BRunUtil
                .invoke(nillableMappingNegativeResult, "testAssignNullArrayToNonNillableTypeWithNillableElements",
                        args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_FIELD));
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping a null array to non-nillable type with non-nillable element type.")
    public void testAssignNullElementArrayToNonNillableTypeWithNonNillableElements() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult,
                "testAssignNullElementArrayToNonNillableTypeWithNonNillableElements", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_ARRAY_FIELD));
    }

    @Test(groups = {TABLE_TEST},
          description = "Test mapping a null element array to nillable type with non-nillable element type.")
    public void testAssignNullElementArrayToNillableTypeWithNonNillableElements() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult,
                "testAssignNullElementArrayToNillableTypeWithNonNillableElements", args);
        Assert.assertTrue(returns[0].stringValue().contains(TRYING_TO_ASSIGN_NIL_TO_NON_NILLABLE_ARRAY_FIELD));
    }

    @Test(groups = {TABLE_TEST}, description = "Test mapping an array to invalid union type.")
    public void testAssignInvalidUnionArray() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignInvalidUnionArray", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = {TABLE_TEST}, description = "Test mapping an array to invalid union type.")
    public void testAssignInvalidUnionArray2() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignInvalidUnionArray2", args);
        Assert.assertTrue(returns[0].stringValue().contains(INVALID_UNION_FIELD_ASSIGNMENT));
    }

    @Test(groups = {TABLE_TEST}, description = "Test mapping an array to invalid union type.")
    public void testAssignInvalidUnionArrayElement() {
        BValue[] returns = BRunUtil.invoke(nillableMappingNegativeResult, "testAssignInvalidUnionArrayElement", args);
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
        Assert.assertEquals(intArray.getRefValue(1).value(), 2L);
        Assert.assertEquals(intArray.getRefValue(2).value(), 3L);

        Assert.assertTrue(returns[1] instanceof BValueArray);
        BValueArray longArray = (BValueArray) returns[1];
        Assert.assertEquals(longArray.getRefValue(0).value(), 100000000L);
        Assert.assertNull(longArray.getRefValue(1));
        Assert.assertEquals(longArray.getRefValue(2).value(), 300000000L);

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
}
