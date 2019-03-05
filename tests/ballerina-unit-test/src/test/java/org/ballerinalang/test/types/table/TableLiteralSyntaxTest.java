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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.utils.ByteArrayUtils;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;

/**
 * Class to test table literal syntax.
 */
public class TableLiteralSyntaxTest {

    private CompileResult result;
    private CompileResult resultNegative;
    private CompileResult resultKeyNegative;
    private CompileResult resultUnConstrainedTableNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/table_literal_syntax.bal");
        resultNegative = BCompileUtil.compile("test-src/types/table/table_literal_syntax_negative.bal");
        resultKeyNegative = BCompileUtil.compile("test-src/types/table/table_literal_key_negative.bal");
        resultUnConstrainedTableNegative = BCompileUtil
                .compile("test-src/types/table/table_literal_unconstrained_table_negative.bal");
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

    @Test
    public void testTableWithDifferentDataTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testTableWithDifferentDataTypes");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 10);
        Assert.assertEquals(((BDecimal) returns[2]).decimalValue(), new BigDecimal("1000.45"));
        Assert.assertEquals(returns[3].stringValue(), "<role>Manager</role>");
        Assert.assertEquals(returns[4].stringValue(), "{\"city\":\"London\", \"country\":\"UK\"}");
    }

    @Test
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

    @Test(description = "Test table remove with function pointer of invalid return type")
    public void testTableReturnNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 18);
        BAssertUtil.validateError(resultNegative, 0, "object type not allowed as the constraint", 40, 11);
        BAssertUtil.validateError(resultNegative, 1, "undefined column 'married2' for table of type 'Person'", 47, 42);
        BAssertUtil.validateError(resultNegative, 2, "undefined field 'married2' in record 'Person'", 48, 10);
        BAssertUtil.validateError(resultNegative, 3, "undefined field 'married2' in record 'Person'", 49, 9);
        BAssertUtil.validateError(resultNegative, 4, "undefined field 'married2' in record 'Person'", 50, 9);
        BAssertUtil.validateError(resultNegative, 5, "incompatible types: expected 'Person', found 'int'", 64, 10);
        BAssertUtil.validateError(resultNegative, 6, "incompatible types: expected 'Person', found 'int'", 64, 13);
        BAssertUtil.validateError(resultNegative, 7, "object type not allowed as the constraint", 76, 5);
        BAssertUtil.validateError(resultNegative, 8, "unknown type 'Student'", 88, 11);
        BAssertUtil.validateError(resultNegative, 9,
            "incompatible types: expected 'function (any) returns (boolean)', found 'function (Person) returns (())'",
                101, 25);
        BAssertUtil.validateError(resultNegative, 10,
                "column 'name' of type 'float' is not allowed as key, use an 'int' or 'string' column", 123, 11);
        BAssertUtil.validateError(resultNegative, 11,
                "column 'name' of type 'json' is not allowed as key, use an 'int' or 'string' column", 129, 11);
        BAssertUtil.validateError(resultNegative, 12,
                "field 'salary' of type 'float|int' is not allowed as a table column", 162, 28);
        BAssertUtil.validateError(resultNegative, 13,
                "field 'bar' of type 'Bar' is not allowed as a table column", 171, 31);
        BAssertUtil.validateError(resultNegative, 14,
                "field 'foo' of type 'Foo' is not allowed as a table column", 180, 31);
        BAssertUtil.validateError(resultNegative, 15,
                "field 'bar' of type 'error' is not allowed as a table column", 188, 31);
        BAssertUtil.validateError(resultNegative, 16,
                "field 'xArr' of type 'xml[]' is not allowed as a table column", 204, 29);
        BAssertUtil.validateError(resultNegative, 17,
                "field 'eArr' of type 'error?[]' is not allowed as a table column", 204, 29);
    }

    @Test(description = "Test table remove with function pointer of invalid return type")
    public void testTableKeyNegativeCases() {
        Assert.assertEquals(resultKeyNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultKeyNegative, 0, "expected token 'key'", 27, 19);
    }

    @Test(description = "Test invalid table creation")
    public void testTableUnconstrainedNegativeCase() {
        Assert.assertEquals(resultUnConstrainedTableNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultUnConstrainedTableNegative, 0, "invalid token 't1'", 18, 11);
    }
}
