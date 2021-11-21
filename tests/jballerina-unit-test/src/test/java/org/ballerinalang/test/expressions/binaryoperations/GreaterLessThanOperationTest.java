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
package org.ballerinalang.test.expressions.binaryoperations;

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Class to test functionality of greater than and less than operators.
 */
public class GreaterLessThanOperationTest {

    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/greater-less-than-operation.bal");
        resultNegative = BCompileUtil.
         compile("test-src/expressions/binaryoperations/greater-less-than-operation-negative.bal");
    }

    @Test(description = "Test int greater than, less than expression")
    public void testIntRangeExpr() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BRunUtil.invoke(result, "testIntRanges", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[]{new BInteger(50)};
        returns = BRunUtil.invoke(result, "testIntRanges", args);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);

        args = new BValue[]{new BInteger(200)};
        returns = BRunUtil.invoke(result, "testIntRanges", args);

        actual = ((BInteger) returns[0]).intValue();
        expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test float greater than, less than expression")
    public void testFloatRangeExpr() {
        BValue[] args = {new BFloat(-123.8f)};
        BValue[] returns = BRunUtil.invoke(result, "testFloatRanges", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[]{new BFloat(75.4f)};
        returns = BRunUtil.invoke(result, "testFloatRanges", args);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);

        args = new BValue[]{new BFloat(321.45f)};
        returns = BRunUtil.invoke(result, "testFloatRanges", args);

        actual = ((BInteger) returns[0]).intValue();
        expected = 3;
        Assert.assertEquals(actual, expected);
    }


    @Test(description = "Test binary statement with errors")
    public void testSubtractStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 88);
        int index = 0;
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for 'json' and 'json'", 7, 12);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for 'json' and 'json'", 16, 12);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'json' and 'json'", 26, 12);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'json' and 'json'", 35, 12);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for 'int' and 'string'", 41, 12);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for 'int' and 'string'", 47, 12);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'int' and 'string'", 53, 12);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'int' and 'string'", 59, 12);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'Person' and 'Person'",
                72, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'Person' and 'Person'",
                73, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for 'Person' and 'Person'",
                74, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for 'Person' and 'Person'",
                75, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for '(Person|int)' and " +
                        "'(Person|int)'", 81, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for '(Person|int)' and '" +
                "(Person|int)'", 82, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for '(Person|int)' and '" +
                "(Person|int)'", 83, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for '(Person|int)' and '" +
                "(Person|int)'", 84, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'Person[]' and 'Person[]'",
                90, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'Person[]' and 'Person[]'",
                91, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for 'Person[]' and 'Person[]'",
                92, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for 'Person[]' and 'Person[]'",
                93, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for '[Person,int]' and " +
                "'[Person,int]'", 99, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for '[Person,int]' and " +
                "'[Person,int]'", 100, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for '[Person,int]' and " +
                "'[Person,int]'", 101, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for '[Person,int]' and " +
                "'[Person,int]'", 102, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for '[int,Person...]' and " +
                "'[int,Person...]'", 108, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for '[int,Person...]' and " +
                "'[int,Person...]'", 109, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for '[int,Person...]' and " +
                "'[int,Person...]'", 110, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for '[int,Person...]' and " +
                "'[int,Person...]'", 111, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'int' and " +
                "'float'", 117, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'int' and " +
                "'float'", 118, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for 'int' and " +
                "'float'", 119, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for 'int' and " +
                "'float'", 120, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'int' and " +
                "'decimal'", 126, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'int' and " +
                "'decimal'", 127, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for 'int' and " +
                "'decimal'", 128, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for 'int' and " +
                "'decimal'", 129, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'float' and " +
                "'decimal'", 135, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'float' and " +
                "'decimal'", 136, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for 'float' and " +
                "'decimal'", 137, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for 'float' and " +
                "'decimal'", 138, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for '(int|string)' " +
                "and '(int|string)'", 144, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for '(int|string)' " +
                "and '(int|string)'", 145, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for '(int|string)' " +
                "and '(int|string)'", 146, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for '(int|string)' " +
                "and '(int|string)'", 147, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'NumberSet[]' " +
                "and 'NumberSet[]'", 155, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'NumberSet[]' " +
                "and 'NumberSet[]'", 156, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for " +
                "'NumberSet[]' and 'NumberSet[]'", 157, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for " +
                "'NumberSet[]' and 'NumberSet[]'", 158, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'OneOrTwo[]' and " +
                "'OneOrTwo[]'", 169, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'OneOrTwo[]' and " +
                "'OneOrTwo[]'", 170, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for 'OneOrTwo[]' and " +
                "'OneOrTwo[]'", 171, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for 'OneOrTwo[]' and " +
                "'OneOrTwo[]'", 172, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'FloatOrString' and " +
                "'FloatOrString'", 181, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'FloatOrString' and " +
                "'FloatOrString'", 182, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for 'FloatOrString' and " +
                "'FloatOrString'", 183, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for 'FloatOrString' and " +
                "'FloatOrString'", 184, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'TwoInts' and " +
                        "'StringTenOrEleven'", 194, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'TwoInts' and " +
                        "'StringTenOrEleven'", 195, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for 'TwoInts' and " +
                        "'StringTenOrEleven'", 196, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for 'TwoInts' and " +
                        "'StringTenOrEleven'", 197, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'FiveOrSix' and " +
                        "'TwoInts'", 209, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'FiveOrSix' and " +
                        "'TwoInts'", 210, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for 'FiveOrSix' and " +
                        "'TwoInts'", 211, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for 'FiveOrSix' and " +
                        "'TwoInts'", 212, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'TwoInts' and 'TwoInts?'",
                219, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'TwoInts' and 'TwoInts?'",
                220, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for 'TwoInts' and 'TwoInts?'",
                221, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for 'TwoInts' and 'TwoInts?'",
                222, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'TwoInts' and 'string'",
                229, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'TwoInts' and 'string'",
                230, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for 'TwoInts' and 'string'",
                231, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for 'TwoInts' and 'string'",
                232, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'string?' and 'string'",
                239, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'string?' and 'string'",
                240, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for 'string?' and 'string'",
                241, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for 'string?' and 'string'",
                242, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for '[float,int,string]' " +
                        "and '[float,int,float...]'", 249, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for '[float,int,string]' and " +
                        "'[float,int,float...]'", 250, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for '[float,int,string]' and " +
                        "'[float,int,float...]'", 251, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for '[float,int,string]' and " +
                        "'[float,int,float...]'", 252, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for " +
                "'[float,int,string,int...]' and '[float,int,string,float...]'", 259, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for " +
                "'[float,int,string,int...]' and '[float,int,string,float...]'", 260, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for " +
                "'[float,int,string,int...]' and '[float,int,string,float...]'", 261, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for " +
                "'[float,int,string,int...]' and '[float,int,string,float...]'", 262, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for " +
                "'[float,int,string,float...]' and '[float,int,float...]'", 269, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for " +
                "'[float,int,string,float...]' and '[float,int,float...]'", 270, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for " +
                "'[float,int,string,float...]' and '[float,int,float...]'", 271, 18);
        BAssertUtil.validateError(resultNegative, index, "operator '>=' not defined for " +
                "'[float,int,string,float...]' and '[float,int,float...]'", 272, 18);
    }

    @Test(description = "Test byte greater than, less than expression")
    public void testByteComparison() {
        BRunUtil.invoke(result, "testByteComparison");
    }

    @Test(description = "Test decimal greater than, less than expression")
    public void testDecimalComparison() {
        BRunUtil.invoke(result, "testDecimalComparison");
    }

    @Test(dataProvider = "FunctionList")
    public void testValueComparsion(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @DataProvider(name = "FunctionList")
    public Object[] testFunctions() {
        return new Object[]{
                "testStringComparison",
                "testBooleanComparison",
                "testArrayComparison1",
                "testArrayComparison2",
                "testArrayComparison3",
                "testTupleComparison1",
                "testTupleComparison2",
                "testTupleComparison3",
                "testTupleComparison4",
                "testTypeComparison1",
                "testTypeComparison2",
                "testTypeComparison3",
                "testTypeComparison4",
                "testTypeComparison5",
                "testTypeComparison6",
                "testTypeComparison7",
                "testTypeComparison8",
                "testTypeComparison9",
                "testUnionComparison1",
                "testUnionComparison2",
                "testUnionComparison3",
                "testUnionComparison4",
                "testUnionComparison5",
                "testUnionComparison6",
                "testUnionComparison7",
                "testUnionComparison8",
                "testUnionComparison9",
                "testUnionComparison10",
                "testUnionComparison11",
                "testUnorderedTypeComparison1",
                "testUnorderedTypeComparison2",
                "testUnorderedTypeComparison3",
                "testUnorderedTypeComparison4",
                "testUnorderedTypeComparison5",
                "testUnorderedTypeComparison6",
                "testUnorderedTypeComparison7",
                "testUnorderedTypeComparison8"
        };
    }
}
