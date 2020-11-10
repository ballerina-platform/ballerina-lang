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
public class ForeachMapTypedBindingPatternsTests {

    private CompileResult program;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-maps-typed-binding-patterns.bal");
    }

    @Test
    public void testUnconstrainedMapWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:A 1:B 2:C ");
    }

    @Test
    public void testUnconstrainedMapWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:A 1:B 2:C ");
    }

    @Test
    public void testConstrainedMapWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:A 1:B 2:C ");
    }

    @Test
    public void testConstrainedMapWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:A 1:B 2:C ");
    }

    @Test
    public void testConstrainedMapWithAnyType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithAnyType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:A 1:B 2:C ");
    }

    @Test
    public void testUnconstrainedMapWithTupleWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:1 A 1:2 B 2:3 C ");
    }

    @Test
    public void testUnconstrainedMapWithTupleWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:1 A 1:2 B 2:3 C ");
    }

    @Test
    public void testConstrainedMapWithTupleWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:1:A 1:2:B 2:3:C ");
    }

    @Test
    public void testConstrainedMapWithTupleWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:1:A 1:2:B 2:3:C ");
    }

    @Test
    public void testConstrainedMapWithTupleWithAnyType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithTupleWithAnyType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:1 A 1:2 B 2:3 C ");
    }

    @Test
    public void testUnconstrainedMapWithTupleInTupleWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithTupleInTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:1 A 2.0 1:2 B 3.0 2:3 C 4.0 ");
    }

    @Test
    public void testUnconstrainedMapWithTupleInTupleWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithTupleInTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:1 A 2.0 1:2 B 3.0 2:3 C 4.0 ");
    }

    @Test
    public void testConstrainedMapWithTupleInTupleWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithTupleInTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:1:A:2.0 1:2:B:3.0 2:3:C:4.0 ");
    }

    @Test
    public void testConstrainedMapWithTupleInTupleWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithTupleInTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:1:A:2.0 1:2:B:3.0 2:3:C:4.0 ");
    }

    @Test
    public void testConstrainedMapWithTupleInTupleWithAnyType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithTupleInTupleWithAnyType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:1 A 2.0 1:2 B 3.0 2:3 C 4.0 ");
    }

    @Test
    public void testUnconstrainedMapWithRecordInTupleWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithRecordInTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:2 {\"i\":1,\"v\":\"A\"} 1:3 {\"i\":2,\"v\":\"B\"} " +
                "2:4 {\"i\":3,\"v\":\"C\"} ");
    }

    @Test
    public void testUnconstrainedMapWithRecordInTupleWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithRecordInTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:2 {\"i\":1,\"v\":\"A\"} 1:3 {\"i\":2,\"v\":\"B\"} 2:4 " +
                "{\"i\":3,\"v\":\"C\"} ");
    }

    @Test
    public void testConstrainedMapWithRecordInTupleWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithRecordInTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:2:1:A 1:3:2:B 2:4:3:C ");
    }

    @Test
    public void testConstrainedMapWithRecordInTupleWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithRecordInTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:2:1:A 1:3:2:B 2:4:3:C ");
    }

    @Test
    public void testConstrainedMapWithRecordInTupleWithAnyType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithRecordInTupleWithAnyType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:2 {\"i\":1,\"v\":\"A\"} 1:3 {\"i\":2,\"v\":\"B\"} " +
                "2:4 {\"i\":3,\"v\":\"C\"} ");
    }

    @Test
    public void testUnconstrainedMapWithRecordWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithRecordWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:{\"i\":1,\"v\":\"A\"} 1:{\"i\":2,\"v\":\"B\"} " +
                "2:{\"i\":3,\"v\":\"C\"} ");
    }

    @Test
    public void testUnconstrainedMapWithRecordWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithRecordWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:{\"i\":1,\"v\":\"A\"} 1:{\"i\":2,\"v\":\"B\"} " +
                "2:{\"i\":3,\"v\":\"C\"} ");
    }

    @Test
    public void testConstrainedMapWithRecordWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithRecordWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:1:A 1:2:B 2:3:C ");
    }

    @Test
    public void testConstrainedMapWithRecordWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithRecordWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:1:A 1:2:B 2:3:C ");
    }

    @Test
    public void testConstrainedMapWithRecordWithAnyType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithRecordWithAnyType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:{\"i\":1,\"v\":\"A\"} 1:{\"i\":2," +
                "\"v\":\"B\"} 2:{\"i\":3,\"v\":\"C\"} ");
    }

    @Test
    public void testUnconstrainedMapWithRecordInRecordWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithRecordInRecordWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:{\"i\":2,\"v\":{\"i\":1,\"v\":\"A\"}} 1:{\"i\":3," +
                "\"v\":{\"i\":2,\"v\":\"B\"}} 2:{\"i\":4,\"v\":{\"i\":3,\"v\":\"C\"}} ");
    }

    @Test
    public void testUnconstrainedMapWithRecordInRecordWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithRecordInRecordWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:{\"i\":2,\"v\":{\"i\":1,\"v\":\"A\"}} 1:{\"i\":3," +
                "\"v\":{\"i\":2,\"v\":\"B\"}} 2:{\"i\":4,\"v\":{\"i\":3,\"v\":\"C\"}} ");
    }

    @Test
    public void testConstrainedMapWithRecordInRecordWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithRecordInRecordWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:2:1:A 1:3:2:B 2:4:3:C ");
    }

    @Test
    public void testConstrainedMapWithRecordInRecordWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithRecordInRecordWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:2:1:A 1:3:2:B 2:4:3:C ");
    }

    @Test
    public void testConstrainedMapWithRecordInRecordWithAnyType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithRecordInRecordWithAnyType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:{\"i\":2,\"v\":{\"i\":1,\"v\":\"A\"}} 1:{\"i\":3," +
                "\"v\":{\"i\":2,\"v\":\"B\"}} 2:{\"i\":4,\"v\":{\"i\":3,\"v\":\"C\"}} ");
    }

    @Test
    public void testUnconstrainedMapWithTupleInRecordWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithTupleInRecordWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:{\"i\":1,\"v\":1 A} 1:{\"i\":2,\"v\":2 B} " +
                "2:{\"i\":3,\"v\":3 C} ");
    }

    @Test
    public void testUnconstrainedMapWithTupleInRecordWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithTupleInRecordWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:{\"i\":1,\"v\":1 A} 1:{\"i\":2,\"v\":2 B} " +
                "2:{\"i\":3,\"v\":3 C} ");
    }

    @Test
    public void testConstrainedMapWithTupleInRecordWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithTupleInRecordWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:1:1:A 1:2:2:B 2:3:3:C ");
    }

    @Test
    public void testConstrainedMapWithTupleInRecordWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithTupleInRecordWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:1:1:A 1:2:2:B 2:3:3:C ");
    }

    @Test
    public void testConstrainedMapWithTupleInRecordWithAnyType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithTupleInRecordWithAnyType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:{\"i\":1,\"v\":1 A} 1:{\"i\":2,\"v\":2 B} " +
                "2:{\"i\":3,\"v\":3 C} ");
    }

    @Test
    public void testEmptyMapIteration() {
        BValue[] returns = BRunUtil.invoke(program, "testEmptyMapIteration");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "");
    }
}
