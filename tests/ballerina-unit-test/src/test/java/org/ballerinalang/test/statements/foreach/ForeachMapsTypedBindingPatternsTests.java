/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.foreach;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for typed binding patterns in foreach.
 *
 * @since 0.985.0
 */
public class ForeachMapsTypedBindingPatternsTests {

    private CompileResult program;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-maps-typed-binding-patterns.bal");
    }

    @Test
    public void testUnconstrainedMapWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:a:A 1:b:B 2:c:C ");
    }

    @Test
    public void testUnconstrainedMapWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:a:A 1:b:B 2:c:C ");
    }

    @Test
    public void testConstrainedMapWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:a:A 1:b:B 2:c:C ");
    }

    @Test
    public void testConstrainedMapWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:a:A 1:b:B 2:c:C ");
    }

    @Test
    public void testConstrainedMapWithAnyType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithAnyType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:a:A 1:b:B 2:c:C ");
    }

    @Test
    public void testUnconstrainedMapWithTupleWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:a:(1, \"A\") 1:b:(2, \"B\") 2:c:(3, \"C\") ");
    }

    @Test
    public void testUnconstrainedMapWithTupleWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:a:(1, \"A\") 1:b:(2, \"B\") 2:c:(3, \"C\") ");
    }

    @Test
    public void testConstrainedMapWithTupleWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:a:1:A 1:b:2:B 2:c:3:C ");
    }

    @Test
    public void testConstrainedMapWithTupleWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:a:1:A 1:b:2:B 2:c:3:C ");
    }

    @Test
    public void testConstrainedMapWithTupleWithAnyType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithTupleWithAnyType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:a:(1, \"A\") 1:b:(2, \"B\") 2:c:(3, \"C\") ");
    }

    @Test
    public void testUnconstrainedMapWithTupleInTupleWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithTupleInTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:a:(1, (\"A\", 2.0)) 1:b:(2, (\"B\", 3.0)) 2:c:(3, (\"C\", 4" +
                ".0)) ");
    }

    @Test
    public void testUnconstrainedMapWithTupleInTupleWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testUnconstrainedMapWithTupleInTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:a:(1, (\"A\", 2.0)) 1:b:(2, (\"B\", 3.0)) 2:c:(3, (\"C\", 4" +
                ".0)) ");
    }

    @Test
    public void testConstrainedMapWithTupleInTupleWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithTupleInTupleWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:a:1:A:2.0 1:b:2:B:3.0 2:c:3:C:4.0 ");
    }

    @Test
    public void testConstrainedMapWithTupleInTupleWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithTupleInTupleWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:a:1:A:2.0 1:b:2:B:3.0 2:c:3:C:4.0 ");
    }

    @Test
    public void testConstrainedMapWithTupleInTupleWithAnyType() {
        BValue[] returns = BRunUtil.invoke(program, "testConstrainedMapWithTupleInTupleWithAnyType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:a:(1, (\"A\", 2.0)) 1:b:(2, (\"B\", 3.0)) 2:c:(3, (\"C\", 4" +
                ".0)) ");
    }
}
