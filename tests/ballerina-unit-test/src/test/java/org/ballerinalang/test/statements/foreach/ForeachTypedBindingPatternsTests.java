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
public class ForeachTypedBindingPatternsTests {

    private CompileResult program;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-typed-binding-patterns.bal");
    }

    @Test
    public void testArrayWithSimpleVariable() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithSimpleVariable");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:A 1:B 2:C ");
    }

    @Test
    public void testArrayWithTuple() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithTuple");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:A 2:B 3:C ");
    }

    @Test
    public void testArrayWithTupleInTuple() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithTupleInTuple");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:A:2.0 2:B:3.0 3:C:4.0 ");
    }

    @Test
    public void testArrayWithRecordInTuple() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithRecordInTuple");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:1:A 2:2:B 3:3:C ");
    }

    @Test
    public void testArrayWithRecord() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithRecord");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:A 2:B 3:C ");
    }

    @Test
    public void testArrayWithRecordInRecord() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithRecordInRecord");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:1:A 2:2:B 3:3:C ");
    }

    @Test
    public void testArrayWithTupleInRecord() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayWithTupleInRecord");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:1:A 2:2:B 3:3:C ");
    }
}
