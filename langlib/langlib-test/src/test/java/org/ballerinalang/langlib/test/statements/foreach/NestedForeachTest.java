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
 * Test cases for nested foreach.
 *
 * @since 0.985.0
 */
public class NestedForeachTest {

    private CompileResult program;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/nested-foreach.bal");
    }

    @Test
    public void test2LevelNestedForeachWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "test2LevelNestedForeachWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:A 1:B 1:C 2:A 2:B 2:C 3:A 3:B 3:C ");
    }

    @Test
    public void test2LevelNestedForeachWithType() {
        BValue[] returns = BRunUtil.invoke(program, "test2LevelNestedForeachWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:A 1:B 1:C 2:A 2:B 2:C 3:A 3:B 3:C ");
    }

    @Test
    public void test3LevelNestedForeachWithoutType() {
        BValue[] returns = BRunUtil.invoke(program, "test3LevelNestedForeachWithoutType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:A:10.0 1:A:11.0 1:A:12.0 1:B:10.0 1:B:11.0 1:B:12.0 1:C:10.0" +
                " 1:C:11.0 1:C:12.0 2:A:10.0 2:A:11.0 2:A:12.0 2:B:10.0 2:B:11.0 2:B:12.0 2:C:10.0 2:C:11.0 2:C:12.0 " +
                "3:A:10.0 3:A:11.0 3:A:12.0 3:B:10.0 3:B:11.0 3:B:12.0 3:C:10.0 3:C:11.0 3:C:12.0 ");
    }

    @Test
    public void test3LevelNestedForeachWithType() {
        BValue[] returns = BRunUtil.invoke(program, "test3LevelNestedForeachWithType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "1:A:10.0 1:A:11.0 1:A:12.0 1:B:10.0 1:B:11.0 1:B:12.0 1:C:10.0" +
                " 1:C:11.0 1:C:12.0 2:A:10.0 2:A:11.0 2:A:12.0 2:B:10.0 2:B:11.0 2:B:12.0 2:C:10.0 2:C:11.0 2:C:12.0 " +
                "3:A:10.0 3:A:11.0 3:A:12.0 3:B:10.0 3:B:11.0 3:B:12.0 3:C:10.0 3:C:11.0 3:C:12.0 ");
    }

    @Test
    public void testNestedForeachWithBreak1() {
        BValue[] returns = BRunUtil.invoke(program, "testNestedForeachWithBreak1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "innerouter");
    }

    @Test
    public void testNestedForeachWithBreak2() {
        BValue[] returns = BRunUtil.invoke(program, "testNestedForeachWithBreak2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "level4level3level2level1");
    }
}
