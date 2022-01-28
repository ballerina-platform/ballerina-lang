/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langlib.test.statements.foreach;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests for typed binding patterns in foreach.
 *
 * @since 0.985.0
 */
public class ForeachArrayTypedBindingPatternsTests {

    private CompileResult program;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-arrays-typed-binding-patterns.bal");
    }

    @Test
    public void testArrayWithSimpleVariableWithoutType() {
        Object[] returns = JvmRunUtil.invoke(program, "testArrayWithSimpleVariableWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "0:A 1:B 2:C ");
    }

    @Test
    public void testArrayWithSimpleVariableWithType() {
        Object[] returns = JvmRunUtil.invoke(program, "testArrayWithSimpleVariableWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "0:A 1:B 2:C ");
    }

    @Test
    public void testArrayWithTupleWithoutType() {
        Object[] returns = JvmRunUtil.invoke(program, "testArrayWithTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "1:A 2:B 3:C ");
    }

    @Test
    public void testArrayWithTupleWithType() {
        Object[] returns = JvmRunUtil.invoke(program, "testArrayWithTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "1:A 2:B 3:C ");
    }

    @Test
    public void testArrayWithTupleInTupleWithoutType() {
        Object[] returns = JvmRunUtil.invoke(program, "testArrayWithTupleInTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "1:A:2.0 2:B:3.0 3:C:4.0 ");
    }

    @Test
    public void testArrayWithTupleInTupleWithType() {
        Object[] returns = JvmRunUtil.invoke(program, "testArrayWithTupleInTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "1:A:2.0 2:B:3.0 3:C:4.0 ");
    }

    @Test
    public void testArrayWithRecordInTupleWithoutType() {
        Object[] returns = JvmRunUtil.invoke(program, "testArrayWithRecordInTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "1:1:A 2:2:B 3:3:C ");
    }

    @Test
    public void testArrayWithRecordInTupleWithType() {
        Object[] returns = JvmRunUtil.invoke(program, "testArrayWithRecordInTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "1:1:A 2:2:B 3:3:C ");
    }

    @Test
    public void testArrayWithRecordWithoutType() {
        Object[] returns = JvmRunUtil.invoke(program, "testArrayWithRecordWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "1:A 2:B 3:C ");
    }

    @Test
    public void testArrayWithRecordWithType() {
        Object[] returns = JvmRunUtil.invoke(program, "testArrayWithRecordWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "1:A 2:B 3:C ");
    }

    @Test
    public void testArrayWithRecordInRecordWithoutType() {
        Object[] returns = JvmRunUtil.invoke(program, "testArrayWithRecordInRecordWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "1:1:A 2:2:B 3:3:C ");
    }

    @Test
    public void testArrayWithRecordInRecordWithType() {
        Object[] returns = JvmRunUtil.invoke(program, "testArrayWithRecordInRecordWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "1:1:A 2:2:B 3:3:C ");
    }

    @Test
    public void testArrayWithTupleInRecordWithoutType() {
        Object[] returns = JvmRunUtil.invoke(program, "testArrayWithTupleInRecordWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "1:1:A 2:2:B 3:3:C ");
    }

    @Test
    public void testArrayWithTupleInRecordWithType() {
        Object[] returns = JvmRunUtil.invoke(program, "testArrayWithTupleInRecordWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "1:1:A 2:2:B 3:3:C ");
    }

    @Test
    public void testEmptyArrayIteration() {
        Object[] returns = JvmRunUtil.invoke(program, "testEmptyArrayIteration");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "");
    }

    @Test(dataProvider = "bindingPatternWithVarInForeachStatementForReadOnlyMembersOfLists")
    public void testBindingPatternWithVarInForeachStatementForReadOnlyMembersOfLists(String function) {
        JvmRunUtil.invoke(program, function);
    }

    @DataProvider
    public Object[] bindingPatternWithVarInForeachStatementForReadOnlyMembersOfLists() {
        return new String[] {
                "testForeachIterationOverReadOnlyArrayMembersOfReadOnlyArrayWithVar",
                "testForeachIterationOverReadOnlyArrayMembersOfNonReadOnlyArrayWithVar",
                "testForeachIterationOverReadOnlyTupleMembersOfReadOnlyArrayWithVar",
                "testForeachIterationOverReadOnlyTupleMembersOfReadOnlyTupleWithVar",
                "testForeachIterationOverReadOnlyArrayMembersOfReadOnlyTupleWithVar",
                "testForeachIterationOverReadOnlyMapOfReadOnlyTupleWithVar"
        };
    }

    @Test(dataProvider = "bindingPatternInForeachStatementForReadOnlyMembersOfLists")
    public void testBindingPatternInForeachStatementForReadOnlyMembersOfLists(String function) {
        JvmRunUtil.invoke(program, function);
    }

    @DataProvider
    public Object[] bindingPatternInForeachStatementForReadOnlyMembersOfLists() {
        return new String[] {
                "testForeachIterationOverReadOnlyArrayMembersOfReadOnlyArray",
                "testForeachIterationOverReadOnlyArrayMembersOfNonReadOnlyArray",
                "testForeachIterationOverReadOnlyTupleMembersOfReadOnlyArray",
                "testForeachIterationOverReadOnlyTupleMembersOfReadOnlyTuple",
                "testForeachIterationOverReadOnlyArrayMembersOfReadOnlyTuple",
                "testForeachIterationOverReadOnlyRecordOfNonReadOnlyArray"
        };
    }
}
