/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.langlib.test.statements.foreach;

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateWarning;

/**
 * TestCases for foreach with Tuples.
 *
 * @since 0.990.4
 */
public class ForeachTupleTests {

    private CompileResult program, negative;

    private int[] iValues = {1, -3, 5, -30, 4, 11, 25, 10};

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach_tuples.bal");
    }

    @AfterClass
    public void tearDown() {
        program = null;
        negative = null;
        iValues = null;
    }

    @Test
    public void testTupleWithBasicTypes() {
        Object returns = BRunUtil.invoke(program, "testTupleWithBasicTypes");
        Assert.assertEquals("20stringtrue()15.520.2", returns.toString());
    }

    @Test
    public void testTupleWithBasicTypesAddingInt() {
        Object returns = BRunUtil.invoke(program, "testTupleWithBasicTypesAddingInt");
        Assert.assertEquals(returns, 18L);
    }

    @Test
    public void testIntTupleComplex() {
        StringBuilder sb = new StringBuilder();
        int sum = 0, negSum = 0;
        for (int i = 0; i < iValues.length; i++) {
            int value = iValues[i];
            if (value > 0) {
                sum += value;
            } else {
                negSum += value;
            }
            for (int j = 0; j < i; j++) {
                sb.append(i).append(":").append(value).append(" ");
            }
        }
        Object returns = BRunUtil.invoke(program, "testIntTupleComplex");
        BArray result = (BArray) returns;
        Assert.assertEquals(result.size(), 3);
        Assert.assertEquals(result.get(0), (long) sum);
        Assert.assertEquals(result.get(1), (long) negSum);
        Assert.assertEquals(result.get(2).toString(), sb.toString());
    }

    @Test
    public void testTupleWithTypeAny() {
        Object returns = BRunUtil.invoke(program, "testTupleWithTypeAny");
        Assert.assertEquals(returns, 8L);
    }

    @Test
    public void testTupleWithTypeAnydata() {
        Object returns = BRunUtil.invoke(program, "testTupleWithTypeAnydata");
        Assert.assertEquals(returns, 8L);
    }

    @Test
    public void testBreak() {
        Object returns = BRunUtil.invoke(program, "testBreak");
        Assert.assertEquals(returns.toString(), "0:d0 break");
    }

    @Test
    public void testContinue() {
        Object returns = BRunUtil.invoke(program, "testContinue");
        Assert.assertEquals(returns.toString(), "0:d0 continue 2:d2 ");
    }

    @Test
    public void testReturn() {
        Object returns = BRunUtil.invoke(program, "testReturn");
        Assert.assertEquals(returns.toString(), "0:d0 ");
    }

    @Test
    public void testNestedBreakContinue() {
        Object returns = BRunUtil.invoke(program, "testNestedWithBreakContinue");
        Assert.assertEquals(returns.toString(), "0:d0 13 1:d1 13 2:d2 13 3:d3 13 ");
    }

    @Test
    public void testTupleWithNullElements() {
        Object returns = BRunUtil.invoke(program, "testTupleWithNullElements");
        Assert.assertEquals(returns.toString(), "0:d0 1: 2:d2 3: ");
    }

    @Test(dataProvider = "dataToTestTupleWithRestDescriptorInForeach")
    public void testTupleWithRestDescriptorInForeach(String functionName) {
        BRunUtil.invoke(program, functionName);
    }

    @DataProvider
    public Object[] dataToTestTupleWithRestDescriptorInForeach() {
        return new Object[]{
                "testTupleWithRestDescriptorInForeach1",
                "testTupleWithRestDescriptorInForeach2",
                "testTupleWithRestDescriptorInForeach3",
                "testTupleWithRestDescriptorInForeach4",
                "testTupleWithRestDescriptorInForeach5",
                "testTupleWithRestDescriptorInForeach6"
        };
    }

    @Test
    public void testIteratingEmptyTuple() {
        BRunUtil.invoke(program, "testIteratingEmptyTuple");
    }

    @Test
    public void testNegativeTupleForeach() {
        negative = BCompileUtil.compile("test-src/statements/foreach/foreach_tuples_negative.bal");
        Assert.assertEquals(negative.getErrorCount(), 7);
        int i = 0;
        BAssertUtil.validateError(negative, i++, "incompatible types: expected '(int|string)', found 'string'", 20, 13);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected '(int|string)', found 'int'", 23, 13);
        BAssertUtil.validateError(negative, i++,
                "incompatible types: expected '(int|string|boolean)', found '(string|int)'", 31, 13);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected '[int,int...]', " +
                "found '[int,int,int...]'", 38, 13);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected '([int,int...]|[int,int,int...]" +
                "|int[2])', found '[int,int,int...]'", 45, 13);
        BAssertUtil.validateError(negative, i++, "invalid list binding pattern: attempted to infer a list type, " +
                "but found '([int,int...]|[int,int,int...]|int)'", 52, 13);
        BAssertUtil.validateError(negative, i, "invalid list binding pattern; member variable " +
                "count mismatch with member type count", 58, 13);
    }

    @Test
    public void testIteratingEmptyTupleNegative() {
        CompileResult compileNegativeResult =
                BCompileUtil.compile("test-src/statements/foreach/tuple_with_no_member_test_negative.bal");
        int index = 0;
        validateError(compileNegativeResult, index++, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 19, 13);
        validateWarning(compileNegativeResult, index++, "unused variable 'x1'", 23, 9);
        validateError(compileNegativeResult, index++, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 23, 18);
        validateError(compileNegativeResult, index++, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 28, 13);
        validateError(compileNegativeResult, index++, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 33, 17);
        Assert.assertEquals(compileNegativeResult.getDiagnostics().length, index);
    }
}
