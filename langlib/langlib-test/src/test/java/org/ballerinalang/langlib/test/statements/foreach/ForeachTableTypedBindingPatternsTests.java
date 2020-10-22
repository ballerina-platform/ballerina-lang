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

import org.ballerinalang.core.model.values.BBoolean;
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

public class ForeachTableTypedBindingPatternsTests {

    private CompileResult program;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-table-typed-binding-patterns.bal");
    }

    @Test
    public void testTableWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testTableWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "0:{\"id\":1,\"name\":\"Mary\",\"salary\":300.5} 1:{\"id\":2,\"name\":\"John\"," +
                        "\"salary\":200.5} 2:{\"id\":3,\"name\":\"Jim\",\"salary\":330.5} ");
    }

    @Test
    public void testTableWithType() {
        BValue[] returns = BRunUtil.invoke(program, "testTableWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "0:{\"id\":1,\"name\":\"Mary\",\"salary\":300.5} " +
                        "1:{\"id\":2,\"name\":\"John\",\"salary\":200.5} " +
                        "2:{\"id\":3,\"name\":\"Jim\",\"salary\":330.5} ");
    }

    @Test
    public void testRecordInTableWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "testRecordInTableWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:1:Mary:300.5 1:2:John:200.5 2:3:Jim:330.5 ");
    }

    @Test
    public void testEmptyTableIteration() {
        BValue[] returns = BRunUtil.invoke(program, "testEmptyTableIteration");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "");
    }

    @Test
    public void testIterationOverKeylessTable() {
        BValue[] returns = BRunUtil.invoke(program, "testIterationOverKeylessTable");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }
}
