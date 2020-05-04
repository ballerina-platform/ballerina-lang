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

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithSimpleVariableWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:A 1:B 2:C ");
    }

    @Test
    public void testArrayWithSimpleVariableWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithSimpleVariableWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:A 1:B 2:C ");
    }

    @Test
    public void testArrayWithTupleWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:A 2:B 3:C ");
    }

    @Test
    public void testArrayWithTupleWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:A 2:B 3:C ");
    }

    @Test
    public void testArrayWithTupleInTupleWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithTupleInTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:A:2.0 2:B:3.0 3:C:4.0 ");
    }

    @Test
    public void testArrayWithTupleInTupleWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithTupleInTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:A:2.0 2:B:3.0 3:C:4.0 ");
    }

    @Test
    public void testArrayWithRecordInTupleWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithRecordInTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:1:A 2:2:B 3:3:C ");
    }

    @Test
    public void testArrayWithRecordInTupleWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithRecordInTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:1:A 2:2:B 3:3:C ");
    }

    @Test
    public void testArrayWithRecordWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithRecordWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:A 2:B 3:C ");
    }

    @Test
    public void testArrayWithRecordWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithRecordWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:A 2:B 3:C ");
    }

    @Test
    public void testArrayWithRecordInRecordWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithRecordInRecordWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:1:A 2:2:B 3:3:C ");
    }

    @Test
    public void testArrayWithRecordInRecordWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithRecordInRecordWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:1:A 2:2:B 3:3:C ");
    }

    @Test
    public void testArrayWithTupleInRecordWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithTupleInRecordWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:1:A 2:2:B 3:3:C ");
    }

    @Test
    public void testArrayWithTupleInRecordWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithTupleInRecordWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:1:A 2:2:B 3:3:C ");
    }

    @Test
    public void testEmptyArrayIteration() {
        BValue[] returns = BRunUtil.invoke(program, "testEmptyArrayIteration");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "");
    }
}
