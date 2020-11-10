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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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

    @Test
    public void testTupleWithBasicTypes() {
        BValue[] returns = BRunUtil.invoke(program, "testTupleWithBasicTypes");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals("20stringtrue()15.520.2", returns[0].stringValue());
    }

    @Test
    public void testTupleWithBasicTypesAddingInt() {
        BValue[] returns = BRunUtil.invoke(program, "testTupleWithBasicTypesAddingInt");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 18);
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
        BValue[] returns = BRunUtil.invoke(program, "testIntTupleComplex");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), sum);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), negSum);
        Assert.assertEquals(returns[2].stringValue(), sb.toString());
    }

    @Test
    public void testTupleWithTypeAny() {
        BValue[] returns = BRunUtil.invoke(program, "testTupleWithTypeAny");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 8);
    }

    @Test
    public void testTupleWithTypeAnydata() {
        BValue[] returns = BRunUtil.invoke(program, "testTupleWithTypeAnydata");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 8);
    }

    @Test
    public void testBreak() {
        BValue[] returns = BRunUtil.invoke(program, "testBreak");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:d0 break");
    }

    @Test
    public void testContinue() {
        BValue[] returns = BRunUtil.invoke(program, "testContinue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:d0 continue 2:d2 ");
    }

    @Test
    public void testReturn() {
        BValue[] returns = BRunUtil.invoke(program, "testReturn");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:d0 ");
    }

    @Test
    public void testNestedBreakContinue() {
        BValue[] returns = BRunUtil.invoke(program, "testNestedWithBreakContinue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:d0 13 1:d1 13 2:d2 13 3:d3 13 ");
    }

    @Test
    public void testTupleWithNullElements() {
        BValue[] returns = BRunUtil.invoke(program, "testTupleWithNullElements");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:d0 1: 2:d2 3: ");
    }

    @Test
    public void testNegativeTupleForeach() {
        negative = BCompileUtil.compile("test-src/statements/foreach/foreach_tuples_negative.bal");
        Assert.assertEquals(negative.getErrorCount(), 3);
        int i = 0;
        BAssertUtil.validateError(negative, i++, "incompatible types: expected '(int|string)', found 'string'", 20, 13);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected '(int|string)', found 'int'", 23, 13);
        BAssertUtil.validateError(negative, i,
                "incompatible types: expected '(int|string|boolean)', found '(string|int)'", 31, 13);
    }
}
