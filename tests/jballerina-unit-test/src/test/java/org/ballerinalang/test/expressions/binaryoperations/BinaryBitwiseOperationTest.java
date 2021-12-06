/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.binaryoperations;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test functionality of bitwise AND, XOR, and OR operations.
 *
 * @since 2.0.0
 */
public class BinaryBitwiseOperationTest {
    CompileResult result, negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/binary_bitwise_operation.bal");
        negativeResult = BCompileUtil.compile(
                "test-src/expressions/binaryoperations/binary_bitwise_operation_negative.bal");
    }

    @Test(dataProvider = "dataToTestBinaryBitwiseOperations", description = "Test binary bitwise operations")
    public void testBinaryBitwiseOperations(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestBinaryBitwiseOperations() {
        return new Object[]{
                "testBitwiseANDOperation",
                "testBitwiseOROperation",
                "testBitwiseXOROperation"
        };
    }

    @Test(description = "Test binary bitwise operations for nullable values")
    public void testBinaryBitwiseOperationsForNullable() {
        BRunUtil.invoke(result, "testBinaryBitwiseOperationsForNullable");
    }

    @Test(description = "Test binary bitwise operations negative scenarios")
    public void testBinaryBitwiseOperationsNegativeScenarios() {
        Assert.assertEquals(negativeResult.getErrorCount(), 25);
        int index = 0;
        BAssertUtil.validateError(negativeResult, index++, "operator '&' not defined for 'float' and 'int'",
                26, 14);
        BAssertUtil.validateError(negativeResult, index++, "operator '&' not defined for 'int' and 'A'",
                29, 14);
        BAssertUtil.validateError(negativeResult, index++, "operator '&' not defined for 'int' and '(int|float)'",
                32, 14);
        BAssertUtil.validateError(negativeResult, index++, "operator '&' not defined for 'int' and 'B'",
                35, 14);
        BAssertUtil.validateError(negativeResult, index++, "operator '&' not defined for 'int' and 'float'",
                37, 14);
        BAssertUtil.validateError(negativeResult, index++, "operator '|' not defined for 'float' and 'int'",
                39, 14);
        BAssertUtil.validateError(negativeResult, index++, "operator '|' not defined for 'int' and 'A'",
                41, 14);
        BAssertUtil.validateError(negativeResult, index++, "operator '|' not defined for 'int' and '(int|float)'",
                43, 14);
        BAssertUtil.validateError(negativeResult, index++, "operator '|' not defined for 'int' and 'B'",
                45, 14);
        BAssertUtil.validateError(negativeResult, index++, "operator '|' not defined for 'int' and 'float'",
                47, 15);
        BAssertUtil.validateError(negativeResult, index++, "operator '^' not defined for 'float' and 'int'",
                49, 15);
        BAssertUtil.validateError(negativeResult, index++, "operator '^' not defined for 'int' and 'A'",
                51, 15);
        BAssertUtil.validateError(negativeResult, index++, "operator '^' not defined for 'int' and '(int|float)'",
                53, 15);
        BAssertUtil.validateError(negativeResult, index++, "operator '^' not defined for 'int' and 'B'",
                55, 15);
        BAssertUtil.validateError(negativeResult, index++, "operator '^' not defined for 'int' and 'float'",
                57, 15);
        BAssertUtil.validateError(negativeResult, index++, "operator '&' not defined for 'float?' and 'int?'",
                62, 16);
        BAssertUtil.validateError(negativeResult, index++, "operator '|' not defined for 'float?' and 'int?'",
                63, 16);
        BAssertUtil.validateError(negativeResult, index++, "operator '^' not defined for 'float?' and 'int?'",
                64, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'int:Unsigned8?', found " +
                        "'int:Unsigned16?'", 68, 26);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'byte?', found " +
                "'int:Unsigned16?'", 72, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'byte?', found " +
                "'int:Unsigned32?'", 75, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'byte', found " +
                "'int:Unsigned32'", 81, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'byte', found " +
                "'int:Unsigned16'", 82, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'int:Unsigned8', " +
                "found 'int:Unsigned32'", 83, 25);
        BAssertUtil.validateError(negativeResult, index, "incompatible types: expected 'int:Unsigned8', " +
                "found 'int:Unsigned16'", 84, 25);
    }
}
