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
        Object[] args = {(0)};
        Object returns = BRunUtil.invoke(result, "testIntRanges", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 1;
        Assert.assertEquals(actual, expected);

        args = new Object[]{(50)};
        returns = BRunUtil.invoke(result, "testIntRanges", args);

        actual = (long) returns;
        expected = 2;
        Assert.assertEquals(actual, expected);

        args = new Object[]{(200)};
        returns = BRunUtil.invoke(result, "testIntRanges", args);

        actual = (long) returns;
        expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test float greater than, less than expression")
    public void testFloatRangeExpr() {
        Object[] args = {(-123.8f)};
        Object returns = BRunUtil.invoke(result, "testFloatRanges", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 1;
        Assert.assertEquals(actual, expected);

        args = new Object[]{(75.4f)};
        returns = BRunUtil.invoke(result, "testFloatRanges", args);

        actual = (long) returns;
        expected = 2;
        Assert.assertEquals(actual, expected);

        args = new Object[]{(321.45f)};
        returns = BRunUtil.invoke(result, "testFloatRanges", args);

        actual = (long) returns;
        expected = 3;
        Assert.assertEquals(actual, expected);
    }


    @Test(description = "Test binary statement with errors")
    public void testSubtractStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 145);
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
        BAssertUtil.validateError(resultNegative, index++, "incompatible types: expected 'OneOrTwo', found " +
                "'int'", 167, 21);
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
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for '(TwoInts|float)' and 'TwoInts?'", 219, 18);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for '(TwoInts|float)' and 'TwoInts?'", 220, 18);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for '(TwoInts|float)' and 'TwoInts?'", 221, 18);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>=' not defined for '(TwoInts|float)' and 'TwoInts?'", 222, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<' not defined for 'TwoInts' and 'string'",
                229, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '<=' not defined for 'TwoInts' and 'string'",
                230, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>' not defined for 'TwoInts' and 'string'",
                231, 18);
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for 'TwoInts' and 'string'",
                232, 18);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for 'string?' and '(string|int)'", 239, 18);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for 'string?' and '(string|int)'", 240, 18);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for 'string?' and '(string|int)'", 241, 18);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>=' not defined for 'string?' and '(string|int)'", 242, 18);
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
        BAssertUtil.validateError(resultNegative, index++, "operator '>=' not defined for " +
                "'[float,int,string,float...]' and '[float,int,float...]'", 272, 18);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for 'Y1' and 'X1'", 282, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for 'Y1' and 'X1'", 283, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for 'Y1' and 'X1'", 284, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>=' not defined for 'Y1' and 'X1'", 285, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for 'X1' and 'Y1'", 286, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for 'X1' and 'Y1'", 287, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for 'X1' and 'Y1'", 288, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>=' not defined for 'X1' and 'Y1'", 289, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for 'Y2' and 'X2'", 299, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for 'Y2' and 'X2'", 300, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for 'Y2' and 'X2'", 301, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>=' not defined for 'Y2' and 'X2'", 302, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for 'X2' and 'Y2'", 303, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for 'X2' and 'Y2'", 304, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for 'X2' and 'Y2'", 305, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>=' not defined for 'X2' and 'Y2'", 306, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for '[int,int,string...]' and 'int[]'", 313, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for '[int,int,string...]' and 'int[]'", 314, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for '[int,int,string...]' and 'int[]'", 315, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>=' not defined for '[int,int,string...]' and 'int[]'", 316, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for 'int[]' and '[int,int,string...]'", 317, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for 'int[]' and '[int,int,string...]'", 318, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for 'int[]' and '[int,int,string...]'", 319, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>=' not defined for 'int[]' and '[int,int,string...]'", 320, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for '[int,int,string]' and 'int[3]'", 327, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for '[int,int,string]' and 'int[3]'", 328, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for '[int,int,string]' and 'int[3]'", 329, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>=' not defined for '[int,int,string]' and 'int[3]'", 330, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for 'int[3]' and '[int,int,string]'", 331, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for 'int[3]' and '[int,int,string]'", 332, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for 'int[3]' and '[int,int,string]'", 333, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>=' not defined for 'int[3]' and '[int,int,string]'", 334, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for '(int:Signed8[2]|(int:Unsigned16[2] & readonly))' and " +
                        "'(string[1]|(string:Char[2] & readonly))'", 341, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for '(int:Signed8[2]|(int:Unsigned16[2] & readonly))' and " +
                        "'(string[1]|(string:Char[2] & readonly))'", 342, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for '(int:Signed8[2]|(int:Unsigned16[2] & readonly))' and " +
                        "'(string[1]|(string:Char[2] & readonly))'", 343, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>=' not defined for '(int:Signed8[2]|(int:Unsigned16[2] & readonly))' and " +
                        "'(string[1]|(string:Char[2] & readonly))'", 344, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for '(string[1]|(string:Char[2] & readonly))' and " +
                        "'(int:Signed8[2]|(int:Unsigned16[2] & readonly))'", 345, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for '(string[1]|(string:Char[2] & readonly))' and " +
                        "'(int:Signed8[2]|(int:Unsigned16[2] & readonly))'", 346, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for '(string[1]|(string:Char[2] & readonly))' and " +
                        "'(int:Signed8[2]|(int:Unsigned16[2] & readonly))'", 347, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>=' not defined for '(string[1]|(string:Char[2] & readonly))' and " +
                        "'(int:Signed8[2]|(int:Unsigned16[2] & readonly))'", 348, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for '(int:Unsigned8[2]|(float[2] & readonly))' and " +
                        "'(string[1]|(string:Char[2] & readonly))'", 355, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for '(int:Unsigned8[2]|(float[2] & readonly))' and " +
                        "'(string[1]|(string:Char[2] & readonly))'", 356, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for '(int:Unsigned8[2]|(float[2] & readonly))' and " +
                        "'(string[1]|(string:Char[2] & readonly))'", 357, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>=' not defined for '(int:Unsigned8[2]|(float[2] & readonly))' and " +
                        "'(string[1]|(string:Char[2] & readonly))'", 358, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for '(string[1]|(string:Char[2] & readonly))' and " +
                        "'(int:Unsigned8[2]|(float[2] & readonly))'", 359, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for '(string[1]|(string:Char[2] & readonly))' and " +
                        "'(int:Unsigned8[2]|(float[2] & readonly))'", 360, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for '(string[1]|(string:Char[2] & readonly))' and " +
                        "'(int:Unsigned8[2]|(float[2] & readonly))'", 361, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>=' not defined for '(string[1]|(string:Char[2] & readonly))' and " +
                        "'(int:Unsigned8[2]|(float[2] & readonly))'", 362, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for '(ByteArr|int:Signed16[2])' and 'string[2]'", 371, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for '(ByteArr|int:Signed16[2])' and 'string[2]'", 372, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for '(ByteArr|int:Signed16[2])' and 'string[2]'", 373, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>=' not defined for '(ByteArr|int:Signed16[2])' and 'string[2]'", 374, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<' not defined for 'string[2]' and '(ByteArr|int:Signed16[2])'", 375, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '<=' not defined for 'string[2]' and '(ByteArr|int:Signed16[2])'", 376, 17);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '>' not defined for 'string[2]' and '(ByteArr|int:Signed16[2])'", 377, 17);
        BAssertUtil.validateError(resultNegative, index,
                "operator '>=' not defined for 'string[2]' and '(ByteArr|int:Signed16[2])'", 378, 17);
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
                "testTypeComparison10",
                "testTypeComparison11",
                "testTypeComparison12",
                "testTypeComparison13",
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
                "testUnorderedTypeComparison8",
                "testUnorderedTypeComparison9",
                "testUnorderedTypeComparison10",
                "testUnorderedTypeComparison11",
                "testUnorderedTypeComparison12",
                "testUnorderedTypeComparison13",
                "testUnorderedTypeComparison14",
                "testUnorderedTypeComparison15",
                "testUnorderedTypeComparison16",
                "testUnorderedTypeComparison17",
                "testUnorderedTypeComparison18",
                "testUnorderedTypeComparison19",
                "testUnorderedTypeComparison20",
                "testUnorderedTypeComparison21",
                "testUnorderedTypeComparison22",
                "testUnorderedTypeComparison23",
                "testUnorderedTypeComparison24",
                "testUnorderedTypeComparison25",
                "testUnorderedTypeComparison26",
                "testUnorderedTypeComparison27",
                "testUnorderedTypeComparison28",
                "testUnorderedTypeComparison29",
                "testUnorderedTypeComparison30"
        };
    }
}
