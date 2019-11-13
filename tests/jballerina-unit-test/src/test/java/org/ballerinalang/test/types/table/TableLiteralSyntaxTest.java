/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.types.table;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.utils.ByteArrayUtils;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;

/**
 * Class to test table literal syntax.
 */
public class TableLiteralSyntaxTest {

    private static final String CARRIAGE_RETURN_CHAR = "\r";
    private static final String EMPTY_STRING = "";

    private CompileResult result;
    private CompileResult resultNegative;
    private CompileResult resultKeyNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/table_literal_syntax.bal");
        resultNegative = BCompileUtil.compile("test-src/types/table/table_literal_syntax_negative1.bal");
        resultKeyNegative = BCompileUtil.compile("test-src/types/table/table_literal_syntax_negative2.bal");
    }

    @Test
    public void testTableDefaultValueForLocalVariable() {
        BValue[] returns = BRunUtil.invoke(result, "testTableDefaultValueForLocalVariable");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test
    public void testTableDefaultValueForGlobalVariable() {
        BValue[] returns = BRunUtil.invoke(result, "testTableDefaultValueForGlobalVariable");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test
    public void testTableAddOnUnconstrainedTable() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAddOnUnconstrainedTable");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test
    public void testTableAddOnConstrainedTable() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAddOnConstrainedTable");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test
    public void testValidTableVariable() {
        BValue[] returns = BRunUtil.invoke(result, "testValidTableVariable");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
    }

    @Test
    public void testTableLiteralData() {
        BValue[] returns = BRunUtil.invoke(result, "testTableLiteralData");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
    }

    @Test
    public void testTableLiteralDataAndAdd() {
        BValue[] returns = BRunUtil.invoke(result, "testTableLiteralDataAndAdd");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test
    public void testTableLiteralDataAndAdd2() {
        BValue[] returns = BRunUtil.invoke(result, "testTableLiteralDataAndAdd2");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*Unique index or primary key violation:.*")
    public void testTableAddOnConstrainedTableWithViolation() {
        BRunUtil.invoke(result, "testTableAddOnConstrainedTableWithViolation");
    }

    @Test
    public void testTableAddOnConstrainedTableWithViolation2() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAddOnConstrainedTableWithViolation2");
        Assert.assertTrue((returns[0]).stringValue().contains("Unique index or primary key violation:"));
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =
                  ".*execute update failed: Unique index or primary key violation:.*")
    public void testTableAddOnConstrainedTableWithViolation3() {
        BRunUtil.invoke(result, "testTableAddOnConstrainedTableWithViolation3");
        Assert.fail("Expected exception should have been thrown by this point");
    }

    @Test
    public void testTableAddWhileIterating() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAddWhileIterating");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

    @Test
    public void testTableLiteralDataAndAddWithKey() {
        BValue[] returns = BRunUtil.invoke(result, "testTableLiteralDataAndAddWithKey");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test
    public void testTableStringPrimaryKey() {
        BValue[] returns = BRunUtil.invoke(result, "testTableStringPrimaryKey");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test(enabled = false)
    public void testTableWithDifferentDataTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithDifferentDataTypes");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 10);
        Assert.assertEquals(((BDecimal) returns[2]).decimalValue(), new BigDecimal("1000.45"));
        Assert.assertEquals(returns[3].stringValue(), "<role>Manager</role>");
        Assert.assertEquals(returns[4].stringValue(), "{\"city\":\"London\", \"country\":\"UK\"}");
    }

    @Test(enabled = false)
    public void testArrayData() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayData");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        //Int Array
        BValueArray retIntArrValue = (BValueArray) returns[1];
        Assert.assertEquals(retIntArrValue.getInt(0), 1);
        Assert.assertEquals(retIntArrValue.getInt(1), 2);
        Assert.assertEquals(retIntArrValue.getInt(2), 3);
        //String Array
        BValueArray retStrArrValue = (BValueArray) returns[2];
        Assert.assertEquals(retStrArrValue.getString(0), "test1");
        Assert.assertEquals(retStrArrValue.getString(1), "test2");
        //Float Array
        BValueArray retFloatArrValue = (BValueArray) returns[3];
        Assert.assertEquals(retFloatArrValue.getFloat(0), 1.1);
        Assert.assertEquals(retFloatArrValue.getFloat(1), 2.2);
        //Boolean Array
        BValueArray retBoolArrValue = (BValueArray) returns[4];
        Assert.assertEquals(retBoolArrValue.getBoolean(0), 1);
        Assert.assertEquals(retBoolArrValue.getBoolean(1), 0);
        //Byte Array
        String b64 = "aGVsbG8gYmFsbGVyaW5hICEhIQ==";
        byte[] byteArrExpected = ByteArrayUtils.decodeBase64(b64);
        BValueArray byteArrReturned = (BValueArray) returns[5];
        ByteArrayUtils.assertJBytesWithBBytes(byteArrExpected, byteArrReturned);
        //Decimal Array
        BValueArray retDecimalArrValue = (BValueArray) returns[6];
        Assert.assertEquals(retDecimalArrValue.getRefValue(0).value(), new BigDecimal("11.11"));
        Assert.assertEquals(retDecimalArrValue.getRefValue(1).value(), new BigDecimal("22.22"));
    }

    @Test
    public void testAddData() {
        BValue[] returns = BRunUtil.invoke(result, "testAddData");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
        Assert.assertEquals(((BValueArray) returns[3]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) returns[3]).getInt(1), 2);
        Assert.assertEquals(((BValueArray) returns[4]).getInt(0), 3);
        Assert.assertEquals(((BValueArray) returns[5]).getInt(0), 100);
    }

    @Test
    public void testMultipleAccess() {
        BValue[] returns = BRunUtil.invoke(result, "testMultipleAccess");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertEquals(((BValueArray) returns[2]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) returns[2]).getInt(1), 2);
        Assert.assertEquals(((BValueArray) returns[2]).getInt(2), 3);
        Assert.assertEquals(((BValueArray) returns[3]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) returns[3]).getInt(1), 2);
        Assert.assertEquals(((BValueArray) returns[3]).getInt(2), 3);
    }

    @Test
    public void testLoopingTable() {
        BValue[] returns = BRunUtil.invoke(result, "testLoopingTable");
        Assert.assertEquals((returns[0]).stringValue(), "jane_martin_john_");
    }

    @Test
    public void testTableWithAllDataToStruct() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithAllDataToStruct");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":30.3}");
        Assert.assertEquals(returns[1].stringValue(), "<book>The Lost World</book>");
    }

    @Test
    public void testTableWithBlobDataToStruct() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithBlobDataToStruct");
        Assert.assertEquals(new String(((BValueArray) returns[0]).getBytes()), "Sample Text");
    }

    @Test
    public void testStructWithDefaultDataToStruct() {
        BValue[] returns = BRunUtil.invoke(result, "testStructWithDefaultDataToStruct");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 0.0);
        Assert.assertEquals(returns[2].stringValue(), "empty");
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
    }

    @Test
    public void testTableWithArrayDataToStruct() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithArrayDataToStruct");
        Assert.assertEquals(((BValueArray) returns[0]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) returns[0]).getInt(1), 2);
        Assert.assertEquals(((BValueArray) returns[0]).getInt(2), 3);
        Assert.assertEquals(((BValueArray) returns[1]).getFloat(0), 11.1);
        Assert.assertEquals(((BValueArray) returns[1]).getFloat(1), 22.2);
        Assert.assertEquals(((BValueArray) returns[1]).getFloat(2), 33.3);
        Assert.assertEquals(((BValueArray) returns[2]).getString(0), "Hello");
        Assert.assertEquals(((BValueArray) returns[2]).getString(1), "World");
        Assert.assertEquals(((BValueArray) returns[3]).getBoolean(0), 1);
        Assert.assertEquals(((BValueArray) returns[3]).getBoolean(1), 0);
        Assert.assertEquals(((BValueArray) returns[3]).getBoolean(2), 1);
    }

    @Test
    public void testTableWithAllDataToXml() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithAllDataToXml");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<results><result><id>1</id><jsonData>{\"name\":\"apple\", "
                + "\"color\":\"red\", \"price\":30.3}</jsonData><xmlData>&lt;book&gt;The Lost World&lt;"
                + "/book&gt;</xmlData></result><result><id>2</id><jsonData>{\"name\":\"apple\", \"color\":\"red\", "
                + "\"price\":30.3}</jsonData><xmlData>&lt;book&gt;The Lost World&lt;/book&gt;</xmlData></result>"
                + "</results>");
    }

    @Test
    public void testTableWithArrayDataToXml() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithArrayDataToXml");
        Assert.assertEquals((returns[0]).stringValue(), "<results><result><id>1</id><intArrData><element>1</element>"
                + "<element>2</element><element>3</element></intArrData>"
                + "<floatArrData><element>11.1</element><element>22.2</element><element>33.3</element></floatArrData>"
                + "<stringArrData><element>Hello</element><element>World</element></stringArrData>"
                + "<booleanArrData><element>true</element><element>false</element><element>true</element>"
                + "</booleanArrData></result>"
                + "<result><id>2</id><intArrData><element>10</element><element>20</element><element>30</element>"
                + "</intArrData><floatArrData><element>111.1</element><element>222.2</element><element>333.3</element>"
                + "</floatArrData><stringArrData><element>Hello</element><element>World</element><element>test"
                + "</element></stringArrData><booleanArrData><element>false</element><element>false</element>"
                + "<element>true</element></booleanArrData></result></results>");
    }

    @Test(description = "Test add data with  mismatched types")
    public void testTableAddInvalid() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAddInvalid");
        Assert.assertEquals((returns[0]).stringValue(),
                "incompatible types: record of type:Company cannot be added to a table with type:Person");
    }

    @Test
    public void testToJson() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testToJson");
        Assert.assertEquals((returns[0]).stringValue(), "[{\"id\":1, \"age\":30, \"salary\":300.5, \"name\":\"jane\", "
                + "\"married\":true}, {\"id\":2, \"age\":20, \"salary\":200.5, \"name\":\"martin\", \"married\":true}, "
                + "{\"id\":3, \"age\":32, \"salary\":100.5, \"name\":\"john\", \"married\":false}]");
    }

    @Test
    public void testToXML() {
        BValue[] returns = BRunUtil.invoke(result, "testToXML");
        Assert.assertEquals((returns[0]).stringValue(), "<results><result><id>1</id><age>30</age>"
                + "<salary>300.5</salary><name>jane</name><married>true</married></result><result>"
                + "<id>2</id><age>20</age><salary>200.5</salary><name>martin</name><married>true</married></result>"
                + "<result><id>3</id><age>32</age><salary>100.5</salary><name>john</name><married>false</married>"
                + "</result></results>");
    }

    @Test
    public void testTableWithAllDataToJson() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testTableWithAllDataToJson");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].stringValue(), "[{\"id\":1, \"jsonData\":{\"name\":\"apple\", " +
                "\"color\":\"red\", \"price\":30.3}, \"xmlData\":\"<book>The Lost World</book>\"}, {\"id\":2, \""
                + "jsonData\":{\"name\":\"apple\", \"color\":\"red\", \"price\":30.3}, "
                + "\"xmlData\":\"<book>The Lost World</book>\"}]");
    }

    @Test(enabled = false)
    public void testTableWithBlobDataToJson() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testTableWithBlobDataToJson");
        Assert.assertEquals((returns[0]).stringValue(), "[{\"id\":1, \"blobData\":\"Sample Text\"}]");
    }

    @Test(enabled = false)
    public void testTableWithBlobDataToXml() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithBlobDataToXml");
        Assert.assertEquals((returns[0]).stringValue(),
                "<results><result><id>1</id><blobData>Sample Text" + "</blobData></result></results>");
    }

    @Test
    public void testTableLiteralWithDefaultableRecord() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testTableLiteralWithDefaultableRecord");
        Assert.assertEquals((returns[0]).stringValue(),
                "[{\"id\":1, \"age\":23, \"salary\":340.5, \"name\":\"John\", \"married\":false}, "
                        + "{\"id\":2, \"age\":34, \"salary\":345.32, \"name\":\"empty\", \"married\":false}]");
    }

    @Test
    public void testStructWithDefaultDataToJson() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testStructWithDefaultDataToJson");
        Assert.assertEquals((returns[0]).stringValue(),
                "[{\"id\":1, \"age\":0, \"salary\":0.0, \"name\":\"empty\", " + "\"married\":false}]");
    }

    @Test
    public void testStructWithDefaultDataToXml() {
        BValue[] returns = BRunUtil.invoke(result, "testStructWithDefaultDataToXml");
        Assert.assertEquals((returns[0]).stringValue(),
                "<results><result><id>1</id><age>0</age><salary>0.0</salary><name>empty</name>"
                        + "<married>false</married></result></results>");
    }

    @Test
    public void testTableWithArrayDataToJson() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testTableWithArrayDataToJson");
        Assert.assertEquals((returns[0]).stringValue(), "[{\"id\":1, \"intArrData\":[1, 2, 3], "
                + "\"floatArrData\":[11.1, 22.2, 33.3], \"stringArrData\":[\"Hello\", \"World\"], "
                + "\"booleanArrData\":[true, false, true]}, "
                + "{\"id\":2, \"intArrData\":[10, 20, 30], \"floatArrData\":[111.1, 222.2, 333.3], "
                + "\"stringArrData\":[\"Hello\", \"World\", \"test\"], \"booleanArrData\":[false, false, true]}]");
    }

    @Test(enabled = false)
    public void testPrintData() throws IOException {
        PrintStream original = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            BRunUtil.invoke(result, "testPrintData");
            Assert.assertEquals(outContent.toString().replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                    "table<Person> {index: [], primaryKey: [\"id\", \"age\"], data: [{id:1, age:30, "
                            + "salary:300.5, name:\"jane\", married:true}, {id:2, age:20, salary:200.5, "
                            + "name:\"martin\", married:true}, {id:3, age:32, salary:100.5, name:\"john\", "
                            + "married:false}]}\n");
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test(enabled = false)
    public void testPrintDataEmptyTable() throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            BRunUtil.invoke(result, "testPrintDataEmptyTable");
            Assert.assertEquals(outContent.toString().replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                    "table<Person> {index: [], primaryKey: [], data: []}\n");
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test
    public void testTableAddAndAccess() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAddAndAccess");
        Assert.assertEquals((returns[0]).stringValue(),
                "[{\"id\":1, \"age\":35, \"salary\":300.5, \"name\":\"jane\", \"married\":true}, "
                        + "{\"id\":2, \"age\":40, \"salary\":200.5, \"name\":\"martin\", \"married\":true}]");
        Assert.assertEquals((returns[1]).stringValue(),
                "[{\"id\":1, \"age\":35, \"salary\":300.5, \"name\":\"jane\", \"married\":true}, {\"id\":2, "
                + "\"age\":40, \"salary\":200.5, \"name\":\"martin\", \"married\":true}, {\"id\":3, \"age\":42, "
                + "\"salary\":100.5, \"name\":\"john\", \"married\":false}]");
    }

    @Test
    public void testTableRemoveSuccess() {
        BValue[] returns = BRunUtil.invoke(result, "testTableRemoveSuccess");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertEquals((returns[1]).stringValue(),
                "[{\"id\":1, \"age\":35, \"salary\":300.5, \"name\":\"jane\", \"married\":true}]");
    }

    @Test
    public void testTableRemoveSuccessMultipleMatch() {
        BValue[] returns = BRunUtil.invoke(result, "testTableRemoveSuccessMultipleMatch");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertEquals((returns[1]).stringValue(),
                "[{\"id\":2, \"age\":20, \"salary\":200.5, \"name\":\"martin\", \"married\":true}]");
    }

    @Test
    public void testTableRemoveFailed() {
        BValue[] returns = BRunUtil.invoke(result, "testTableRemoveFailed");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals((returns[1]).stringValue(), "[{\"id\":1, \"age\":35, \"salary\":300.5, "
                + "\"name\":\"jane\", \"married\":true}, {\"id\":2, \"age\":40, \"salary\":200.5, "
                + "\"name\":\"martin\", \"married\":true}, {\"id\":3, \"age\":42, \"salary\":100.5, "
                + "\"name\":\"john\", \"married\":false}]");
    }

    @Test(description = "Test remove data with mismatched record types")
    public void testRemoveWithInvalidRecordType() {
        BValue[] returns = BRunUtil.invoke(result, "testRemoveWithInvalidRecordType");
        Assert.assertEquals((returns[0]).stringValue(),
                "incompatible types: function with record type:Company cannot be used to remove records"
                        + " from a table with type:Person");
    }

    @Test(description = "Test remove data with mismatched input parameter types")
    public void testRemoveWithInvalidParamType() {
        BValue[] returns = BRunUtil.invoke(result, "testRemoveWithInvalidParamType");
        Assert.assertEquals((returns[0]).stringValue(),
                "incompatible types: function with record type:int cannot be used to remove records from a "
                        + "table with type:Person");
    }

    @Test(description = "Test removing data from a table using a given lambda as a filter")
    public void testRemoveOpAnonymousFilter() {
        BValue[] returns = BRunUtil.invoke(result, "testRemoveOpAnonymousFilter");
        Assert.assertEquals(returns[0].stringValue(), "table<Order> {index: [], primaryKey: [], data: []}");
    }

    @Test(enabled = false) //Issue #5106
    public void testEmptyTableCreate() {
        BValue[] returns = BRunUtil.invoke(result, "testEmptyTableCreate");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 2);
    }

//    @Test(priority = 2)
//    public void testTableDrop() {
//        BRunUtil.invoke(result, "testTableDrop");
//        //Table count before garbage collection happens.
//        BValue[] args = new BValue[1];
//        args[0] = new BString("TABLE_PERSON_%");
//        BValue[] returns = BRunUtil.invoke(resultHelper, "getTableCount", args);
//        long beforeCount = ((BInteger) returns[0]).intValue();
//        //Request for garbage collection process.
//        System.gc();
//        //Check whether table has drop
//        await().atMost(30, SECONDS).until(() -> {
//            BValue[] returnVal = BRunUtil.invoke(resultHelper, "getTableCount", args);
//            long afterCount = ((BInteger) returnVal[0]).intValue();
//            return afterCount < beforeCount;
//        });
//
//        returns = BRunUtil.invoke(resultHelper, "getTableCount", args);
//        long afterCount = ((BInteger) returns[0]).intValue();
//        Assert.assertTrue(beforeCount > afterCount);
//    }

    @Test(description = "Test table remove with function pointer of invalid return type")
    public void testTableReturnNegativeCases() {
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++, "object type not allowed as the constraint", 39, 11);
        BAssertUtil.validateError(resultNegative, i++,
                "undefined column 'married2' for table of type 'Person'", 46, 42);
        BAssertUtil.validateError(resultNegative, i++, "undefined field 'married2' in record 'Person'", 47, 10);
        BAssertUtil.validateError(resultNegative, i++, "undefined field 'married2' in record 'Person'", 48, 9);
        BAssertUtil.validateError(resultNegative, i++, "undefined field 'married2' in record 'Person'", 49, 9);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'Person', found 'int'", 66, 10);
        BAssertUtil.validateError(resultNegative, i++, "incompatible types: expected 'Person', found 'int'", 66, 13);
        BAssertUtil.validateError(resultNegative, i++, "object type not allowed as the constraint", 81, 5);
        BAssertUtil.validateError(resultNegative, i++, "unknown type 'Student'", 96, 11);
        BAssertUtil.validateError(resultNegative, i++, "table type constraint must be a record type", 96, 25);
        BAssertUtil.validateError(resultNegative, i++,
                                  "incompatible types: expected 'function (any) returns (boolean)', found 'function " +
                                          "(Person) returns ()'",
                                  109, 25);
        BAssertUtil.validateError(resultNegative, i++,
                                  "column 'name' of type 'float' is not allowed as key, use an 'int' or 'string' " +
                                          "column", 131, 11);
        BAssertUtil.validateError(resultNegative, i++,
                                  "column 'name' of type 'json' is not allowed as key, use an 'int' or 'string' column",
                                  137, 11);
        BAssertUtil.validateError(resultNegative, i++,
                                  "field 'salary' of type '(float|int)' is not allowed as a table column", 170, 28);
        BAssertUtil.validateError(resultNegative, i++,
                                  "field 'bar' of type 'Bar' is not allowed as a table column", 179, 31);
        BAssertUtil.validateError(resultNegative, i++,
                                  "field 'foo' of type 'Foo' is not allowed as a table column", 188, 31);
        BAssertUtil.validateError(resultNegative, i++,
                                  "field 'bar' of type 'error' is not allowed as a table column", 196, 31);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'record {| anydata...; |}', found 'ErrorInRecord'", 202, 23);
        BAssertUtil.validateError(resultNegative, i++,
                "field 'eArr' of type 'error?[]' is not allowed as a table column", 212, 29);
        BAssertUtil.validateError(resultNegative, i++,
                                  "field 'xArr' of type 'xml[]' is not allowed as a table column", 212, 29);
        BAssertUtil.validateError(resultNegative, i++,
                                  "cannot infer table type", 223, 14);
        BAssertUtil.validateError(resultNegative, i++,
                                  "table type constraint must be a record type", 233, 20);
        BAssertUtil.validateError(resultNegative, i++,
                "missing non-defaultable required record field 'name'", 251, 9);
        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }

    @Test(description = "Test table remove with function pointer of invalid return type")
    public void testTableKeyNegativeCases() {
        Assert.assertEquals(resultKeyNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resultKeyNegative, 0, "expected token 'key'", 26, 19);
        BAssertUtil.validateError(resultKeyNegative, 1, "invalid token 't1'", 34, 11);
    }
}
