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
public class ForeachMapTypedBindingPatternsTests {

    private CompileResult program;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-maps-typed-binding-patterns.bal");
    }

    @Test
    public void testUnconstrainedMapWithoutType() {
        Object returns = JvmRunUtil.invoke(program, "testUnconstrainedMapWithoutType");
        Assert.assertEquals(returns.toString(), "0:A 1:B 2:C ");
    }

    @Test
    public void testUnconstrainedMapWithType() {
        Object returns = JvmRunUtil.invoke(program, "testUnconstrainedMapWithType");
        Assert.assertEquals(returns.toString(), "0:A 1:B 2:C ");
    }

    @Test
    public void testConstrainedMapWithoutType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithoutType");
        Assert.assertEquals(returns.toString(), "0:A 1:B 2:C ");
    }

    @Test
    public void testConstrainedMapWithType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithType");
        Assert.assertEquals(returns.toString(), "0:A 1:B 2:C ");
    }

    @Test
    public void testConstrainedMapWithAnyType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithAnyType");
        Assert.assertEquals(returns.toString(), "0:A 1:B 2:C ");
    }

    @Test
    public void testUnconstrainedMapWithTupleWithoutType() {
        Object returns = JvmRunUtil.invoke(program, "testUnconstrainedMapWithTupleWithoutType");
        Assert.assertEquals(returns.toString(), "0:[1,\"A\"] 1:[2,\"B\"] 2:[3,\"C\"] ");
    }

    @Test
    public void testUnconstrainedMapWithTupleWithType() {
        Object returns = JvmRunUtil.invoke(program, "testUnconstrainedMapWithTupleWithType");
        Assert.assertEquals(returns.toString(), "0:[1,\"A\"] 1:[2,\"B\"] 2:[3,\"C\"] ");
    }

    @Test
    public void testConstrainedMapWithTupleWithoutType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithTupleWithoutType");
        Assert.assertEquals(returns.toString(), "0:1:A 1:2:B 2:3:C ");
    }

    @Test
    public void testConstrainedMapWithTupleWithType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithTupleWithType");
        Assert.assertEquals(returns.toString(), "0:1:A 1:2:B 2:3:C ");
    }

    @Test
    public void testConstrainedMapWithTupleWithAnyType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithTupleWithAnyType");
        Assert.assertEquals(returns.toString(), "0:[1,\"A\"] 1:[2,\"B\"] 2:[3,\"C\"] ");
    }

    @Test
    public void testUnconstrainedMapWithTupleInTupleWithoutType() {
        Object returns = JvmRunUtil.invoke(program, "testUnconstrainedMapWithTupleInTupleWithoutType");
        Assert.assertEquals(returns.toString(), "0:[1,[\"A\",2.0]] 1:[2,[\"B\",3.0]] 2:[3,[\"C\",4.0]] ");
    }

    @Test
    public void testUnconstrainedMapWithTupleInTupleWithType() {
        Object returns = JvmRunUtil.invoke(program, "testUnconstrainedMapWithTupleInTupleWithType");
        Assert.assertEquals(returns.toString(), "0:[1,[\"A\",2.0]] 1:[2,[\"B\",3.0]] 2:[3,[\"C\",4.0]] ");
    }

    @Test
    public void testConstrainedMapWithTupleInTupleWithoutType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithTupleInTupleWithoutType");
        Assert.assertEquals(returns.toString(), "0:1:A:2.0 1:2:B:3.0 2:3:C:4.0 ");
    }

    @Test
    public void testConstrainedMapWithTupleInTupleWithType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithTupleInTupleWithType");
        Assert.assertEquals(returns.toString(), "0:1:A:2.0 1:2:B:3.0 2:3:C:4.0 ");
    }

    @Test
    public void testConstrainedMapWithTupleInTupleWithAnyType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithTupleInTupleWithAnyType");
        Assert.assertEquals(returns.toString(), "0:[1,[\"A\",2.0]] 1:[2,[\"B\",3.0]] 2:[3,[\"C\",4.0]] ");
    }

    @Test
    public void testUnconstrainedMapWithRecordInTupleWithoutType() {
        Object returns = JvmRunUtil.invoke(program, "testUnconstrainedMapWithRecordInTupleWithoutType");
        Assert.assertEquals(returns.toString(), "0:[2,{\"i\":1,\"v\":\"A\"}] 1:[3,{\"i\":2,\"v\":\"B\"}] 2:[4," +
                "{\"i\":3,\"v\":\"C\"}] ");
    }

    @Test
    public void testUnconstrainedMapWithRecordInTupleWithType() {
        Object returns = JvmRunUtil.invoke(program, "testUnconstrainedMapWithRecordInTupleWithType");
        Assert.assertEquals(returns.toString(), "0:[2,{\"i\":1,\"v\":\"A\"}] 1:[3,{\"i\":2,\"v\":\"B\"}] 2:[4," +
                "{\"i\":3,\"v\":\"C\"}] ");
    }

    @Test
    public void testConstrainedMapWithRecordInTupleWithoutType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithRecordInTupleWithoutType");
        Assert.assertEquals(returns.toString(), "0:2:1:A 1:3:2:B 2:4:3:C ");
    }

    @Test
    public void testConstrainedMapWithRecordInTupleWithType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithRecordInTupleWithType");
        Assert.assertEquals(returns.toString(), "0:2:1:A 1:3:2:B 2:4:3:C ");
    }

    @Test
    public void testConstrainedMapWithRecordInTupleWithAnyType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithRecordInTupleWithAnyType");
        Assert.assertEquals(returns.toString(), "0:[2,{\"i\":1,\"v\":\"A\"}] 1:[3,{\"i\":2,\"v\":\"B\"}] " +
                "2:[4,{\"i\":3,\"v\":\"C\"}] ");
    }

    @Test
    public void testUnconstrainedMapWithRecordWithoutType() {
        Object returns = JvmRunUtil.invoke(program, "testUnconstrainedMapWithRecordWithoutType");
        Assert.assertEquals(returns.toString(), "0:{\"i\":1,\"v\":\"A\"} 1:{\"i\":2,\"v\":\"B\"} " +
                "2:{\"i\":3,\"v\":\"C\"} ");
    }

    @Test
    public void testUnconstrainedMapWithRecordWithType() {
        Object returns = JvmRunUtil.invoke(program, "testUnconstrainedMapWithRecordWithType");
        Assert.assertEquals(returns.toString(), "0:{\"i\":1,\"v\":\"A\"} 1:{\"i\":2,\"v\":\"B\"} " +
                "2:{\"i\":3,\"v\":\"C\"} ");
    }

    @Test
    public void testConstrainedMapWithRecordWithoutType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithRecordWithoutType");
        Assert.assertEquals(returns.toString(), "0:1:A 1:2:B 2:3:C ");
    }

    @Test
    public void testConstrainedMapWithRecordWithType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithRecordWithType");
        Assert.assertEquals(returns.toString(), "0:1:A 1:2:B 2:3:C ");
    }

    @Test
    public void testConstrainedMapWithRecordWithAnyType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithRecordWithAnyType");
        Assert.assertEquals(returns.toString(), "0:{\"i\":1,\"v\":\"A\"} 1:{\"i\":2," +
                "\"v\":\"B\"} 2:{\"i\":3,\"v\":\"C\"} ");
    }

    @Test
    public void testUnconstrainedMapWithRecordInRecordWithoutType() {
        Object returns = JvmRunUtil.invoke(program, "testUnconstrainedMapWithRecordInRecordWithoutType");
        Assert.assertEquals(returns.toString(), "0:{\"i\":2,\"v\":{\"i\":1,\"v\":\"A\"}} 1:{\"i\":3," +
                "\"v\":{\"i\":2,\"v\":\"B\"}} 2:{\"i\":4,\"v\":{\"i\":3,\"v\":\"C\"}} ");
    }

    @Test
    public void testUnconstrainedMapWithRecordInRecordWithType() {
        Object returns = JvmRunUtil.invoke(program, "testUnconstrainedMapWithRecordInRecordWithType");
        Assert.assertEquals(returns.toString(), "0:{\"i\":2,\"v\":{\"i\":1,\"v\":\"A\"}} 1:{\"i\":3," +
                "\"v\":{\"i\":2,\"v\":\"B\"}} 2:{\"i\":4,\"v\":{\"i\":3,\"v\":\"C\"}} ");
    }

    @Test
    public void testConstrainedMapWithRecordInRecordWithoutType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithRecordInRecordWithoutType");
        Assert.assertEquals(returns.toString(), "0:2:1:A 1:3:2:B 2:4:3:C ");
    }

    @Test
    public void testConstrainedMapWithRecordInRecordWithType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithRecordInRecordWithType");
        Assert.assertEquals(returns.toString(), "0:2:1:A 1:3:2:B 2:4:3:C ");
    }

    @Test
    public void testConstrainedMapWithRecordInRecordWithAnyType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithRecordInRecordWithAnyType");
        Assert.assertEquals(returns.toString(), "0:{\"i\":2,\"v\":{\"i\":1,\"v\":\"A\"}} 1:{\"i\":3," +
                "\"v\":{\"i\":2,\"v\":\"B\"}} 2:{\"i\":4,\"v\":{\"i\":3,\"v\":\"C\"}} ");
    }

    @Test
    public void testUnconstrainedMapWithTupleInRecordWithoutType() {
        Object returns = JvmRunUtil.invoke(program, "testUnconstrainedMapWithTupleInRecordWithoutType");
        Assert.assertEquals(returns.toString(), "0:{\"i\":1,\"v\":[1,\"A\"]} 1:{\"i\":2,\"v\":[2,\"B\"]} " +
                "2:{\"i\":3,\"v\":[3,\"C\"]} ");
    }

    @Test
    public void testUnconstrainedMapWithTupleInRecordWithType() {
        Object returns = JvmRunUtil.invoke(program, "testUnconstrainedMapWithTupleInRecordWithType");
        Assert.assertEquals(returns.toString(), "0:{\"i\":1,\"v\":[1,\"A\"]} 1:{\"i\":2,\"v\":[2,\"B\"]} " +
                "2:{\"i\":3,\"v\":[3,\"C\"]} ");
    }

    @Test
    public void testConstrainedMapWithTupleInRecordWithoutType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithTupleInRecordWithoutType");
        Assert.assertEquals(returns.toString(), "0:1:1:A 1:2:2:B 2:3:3:C ");
    }

    @Test
    public void testConstrainedMapWithTupleInRecordWithType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithTupleInRecordWithType");
        Assert.assertEquals(returns.toString(), "0:1:1:A 1:2:2:B 2:3:3:C ");
    }

    @Test
    public void testConstrainedMapWithTupleInRecordWithAnyType() {
        Object returns = JvmRunUtil.invoke(program, "testConstrainedMapWithTupleInRecordWithAnyType");
        Assert.assertEquals(returns.toString(), "0:{\"i\":1,\"v\":[1,\"A\"]} 1:{\"i\":2,\"v\":[2,\"B\"]} " +
                "2:{\"i\":3,\"v\":[3,\"C\"]} ");
    }

    @Test
    public void testEmptyMapIteration() {
        Object returns = JvmRunUtil.invoke(program, "testEmptyMapIteration");
        Assert.assertEquals(returns.toString(), "");
    }

    @Test(dataProvider = "bindingPatternWithVarInForeachStatementForReadOnlyMembersOfMappings")
    public void testBindingPatternWithVarInForeachStatementForReadOnlyMembersOfMappings(String function) {
        JvmRunUtil.invoke(program, function);
    }

    @DataProvider
    public Object[] bindingPatternWithVarInForeachStatementForReadOnlyMembersOfMappings() {
        return new String[] {
                "testForeachIterationOverReadOnlyTupleMembersOfNonReadOnlyRecordWithVar",
                "testForeachIterationOverReadOnlyArrayMembersOfReadOnlyMapWithVar"
        };
    }

    @Test(dataProvider = "bindingPatternInForeachStatementForReadOnlyMembersOfMappings")
    public void testBindingPatternInForeachStatementForReadOnlyMembersOfMappings(String function) {
        JvmRunUtil.invoke(program, function);
    }

    @DataProvider
    public Object[] bindingPatternInForeachStatementForReadOnlyMembersOfMappings() {
        return new String[] {
                "testForeachIterationOverReadOnlyTupleMembersOfNonReadOnlyRecord",
                "testForeachIterationOverReadOnlyTupleMembersOfReadOnlyMap"
        };
    }
}
